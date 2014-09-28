/*
 * Copyright 2004-2014 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.dbflute.twowaysql;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;

import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.util.DfTypeUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author modified by jflute (originated in S2Dao)
 */
public class DisplaySqlBuilder {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";
    public static final String DEFAULT_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String NULL = "null";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _logDateFormat;
    protected final String _logTimestampFormat;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DisplaySqlBuilder(String logDateFormat, String logTimestampFormat) {
        _logDateFormat = logDateFormat;
        _logTimestampFormat = logTimestampFormat;
    }

    // ===================================================================================
    //                                                                         Display SQL
    //                                                                         ===========
    public String buildDisplaySql(String sql, Object[] args) {
        final EmbeddingProcessor processor = new EmbeddingProcessor();
        return processor.embed(sql, args);
    }

    protected class EmbeddingProcessor { // non-thread-safe

        // scanning elements
        protected int _processPointer = 0;
        protected int _loopIndex = 0;

        // temporary variable for question mark
        protected int _questionMarkIndex = 0;

        // temporary variable for quotation scope begin/end
        protected int _quotationScopeBeginIndex = 0;
        protected int _quotationScopeEndIndex = 0;

        // temporary variable for block comment begin/end
        protected int _blockCommentBeginIndex = 0;
        protected int _blockCommentEndIndex = 0;

        public String embed(String sql, Object[] args) {
            if (args == null || args.length == 0) {
                return sql;
            }
            final StringBuilder sb = new StringBuilder(sql.length() + args.length * 15);

            while (true) {
                _questionMarkIndex = sql.indexOf('?', _processPointer);
                setupQuestionMarkIndex(sql);
                if (_questionMarkIndex < 0) {
                    processLastPart(sb, sql, args);
                    break;
                }
                if (_questionMarkIndex == 0) {
                    processBindVariable(sb, sql, args);
                    continue;
                }
                setupBlockCommentIndex(sql);
                if (hasBlockComment()) {
                    if (isBeforeBlockComment()) {
                        processQuotationScope(sb, sql, args);
                    } else { // in or after the block comment
                        processBlockComment(sb, sql, args);
                    }
                } else { // means no more block comment
                    processQuotationScope(sb, sql, args);
                }
            }
            return sb.toString();
        }

        protected void processBindVariable(StringBuilder sb, String sql, Object[] args) {
            assertArgumentSize(args, sql);
            final String beforeParameter = sql.substring(_processPointer, _questionMarkIndex);
            final String bindVariableText = getBindVariableText(args[_loopIndex]);
            sb.append(beforeParameter).append(bindVariableText);
            _processPointer = _questionMarkIndex + 1;
            ++_loopIndex;
        }

        protected void assertArgumentSize(Object[] args, String sql) {
            if (args.length <= _loopIndex) {
                final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
                br.addNotice("The count of bind arguments is illegal for DisplaySql.");
                br.addItem("ExecutedSql");
                br.addElement(sql);
                br.addItem("Arguments");
                br.addElement(Arrays.asList(args));
                br.addElement("count=" + args.length);
                final String msg = br.buildExceptionMessage();
                throw new IllegalStateException(msg);
            }
        }

        protected void processBlockComment(StringBuilder sb, String sql, Object[] args) {
            final int nextPointer = _blockCommentEndIndex + 1;
            final String beforeCommentEnd = sql.substring(_processPointer, nextPointer);
            sb.append(beforeCommentEnd);
            _processPointer = nextPointer;
        }

        protected void processQuotationScope(StringBuilder sb, String sql, Object[] args) {
            setupQuotationScopeIndex(sql);
            if (isQuotationScopeOverBlockComment()) {
                // means the quotation end in or after the comment (invalid scope)
                _quotationScopeBeginIndex = -1;
                _quotationScopeEndIndex = -1;
            }
            if (hasQuotationScope()) {
                if (isInQuotationScope()) {
                    final int nextPointer = _quotationScopeEndIndex + 1;
                    final String beforeScopeEnd = sql.substring(_processPointer, nextPointer);
                    sb.append(beforeScopeEnd);
                    _processPointer = nextPointer;
                } else {
                    processBindVariable(sb, sql, args);
                }
            } else {
                processBindVariable(sb, sql, args);
            }
        }

        protected void processLastPart(StringBuilder sb, String sql, Object[] args) {
            final String lastPart = sql.substring(_processPointer);
            sb.append(lastPart);
            _processPointer = 0;
            _loopIndex = 0;
        }

        protected void setupQuestionMarkIndex(String sql) {
            _questionMarkIndex = sql.indexOf('?', _processPointer);
        }

        protected void setupBlockCommentIndex(String sql) {
            _blockCommentBeginIndex = sql.indexOf("/*", _processPointer);
            _blockCommentEndIndex = sql.indexOf("*/", _blockCommentBeginIndex + 1);
        }

        protected void setupQuotationScopeIndex(String sql) {
            _quotationScopeBeginIndex = sql.indexOf('\'', _processPointer);
            _quotationScopeEndIndex = sql.indexOf('\'', _quotationScopeBeginIndex + 1);
        }

        protected boolean hasBlockComment() {
            return _blockCommentBeginIndex >= 0 && _blockCommentEndIndex >= 0;
        }

        protected boolean hasQuotationScope() {
            return _quotationScopeBeginIndex >= 0 && _quotationScopeEndIndex >= 0;
        }

        protected boolean isBeforeBlockComment() {
            if (!hasBlockComment()) {
                return false;
            }
            return _questionMarkIndex < _blockCommentBeginIndex;
        }

        protected boolean isInBlockComment() {
            if (!hasBlockComment()) {
                return false;
            }
            return _blockCommentBeginIndex < _questionMarkIndex && _questionMarkIndex < _blockCommentEndIndex;
        }

        protected boolean isInQuotationScope() {
            if (!hasQuotationScope()) {
                return false;
            }
            return _quotationScopeBeginIndex < _questionMarkIndex && _questionMarkIndex < _quotationScopeEndIndex;
        }

        protected boolean isQuotationScopeOverBlockComment() {
            return hasBlockComment() && hasQuotationScope() && _quotationScopeEndIndex > _blockCommentBeginIndex;
        }
    }

    // ===================================================================================
    //                                                                        BindVariable
    //                                                                        ============
    public String getBindVariableText(Object bindVariable) {
        if (bindVariable instanceof String) {
            return quote(bindVariable.toString());
        } else if (bindVariable instanceof Number) {
            return bindVariable.toString();
        } else if (bindVariable instanceof Timestamp) {
            return buildTimestampText(bindVariable);
        } else if (bindVariable instanceof Time) {
            return buildTimeText(bindVariable);
        } else if (bindVariable instanceof java.util.Date) {
            return buildDateText(bindVariable);
        } else if (bindVariable instanceof Boolean) {
            return bindVariable.toString();
        } else if (bindVariable == null) {
            return NULL;
        } else {
            return quote(bindVariable.toString());
        }
    }

    // ===================================================================================
    //                                                                       Date Handling
    //                                                                       =============
    protected String buildTimestampText(Object bindVariable) {
        final String format = _logTimestampFormat != null ? _logTimestampFormat : DEFAULT_TIMESTAMP_FORMAT;
        final java.util.Date date = (java.util.Date) bindVariable;
        return processDateDisplay(date, format);
    }

    protected String buildTimeText(Object bindVariable) { // Time type has no option
        final String format = DEFAULT_TIME_FORMAT;
        final java.util.Date date = (java.util.Date) bindVariable;
        return quote(DfTypeUtil.toString(date, format));
    }

    protected String buildDateText(Object bindVariable) {
        final String format = _logDateFormat != null ? _logDateFormat : DEFAULT_DATE_FORMAT;
        final java.util.Date date = (java.util.Date) bindVariable;
        return processDateDisplay(date, format);
    }

    protected String processDateDisplay(java.util.Date date, String format) {
        final DateFormatResource resource = analyzeDateFormat(format);
        String disp = DfTypeUtil.toString(date, resource.getFormat());
        if (isBCPrefixTarget(date, resource)) {
            // fixed specification, basically not use 'G'
            // in pattern at least default pattern
            // because it should be displayed only when BC date
            disp = "BC" + disp;
        }
        return quote(disp, resource);
    }

    protected boolean isBCPrefixTarget(java.util.Date date, DateFormatResource resource) {
        final String format = resource.getFormat();
        return DfTypeUtil.isDateBC(date) && format.startsWith("yyyy") && !format.contains("G");
    }

    /**
     * Analyze date format.
     * <pre>
     * e.g.
     * o yyyy-MM-dd = '2009-10-27'
     * o date $df:{yyyy-MM-dd} = date '2009-10-27'
     * o prefix$df:{yyyy-MM-dd}suffix = prefix'2009-10-27'suffix
     * </pre>
     * @param format The string of format. (NotNull)
     * @return The resource of date format. (NotNull)
     */
    protected DateFormatResource analyzeDateFormat(String format) {
        final DateFormatResource resource = new DateFormatResource();
        final String dfMark = "$df:{";
        final int dfMarkBeginIndex = format.indexOf(dfMark);
        if (dfMarkBeginIndex >= 0) {
            final String rear = format.substring(dfMarkBeginIndex + dfMark.length());
            final int dfMarkEndIndex = rear.indexOf("}");
            if (dfMarkEndIndex >= 0) {
                resource.setFormat(rear.substring(0, dfMarkEndIndex));
                resource.setPrefix(format.substring(0, dfMarkBeginIndex));
                resource.setSuffix(rear.substring(dfMarkEndIndex + "}".length()));
                return resource;
            }
        }
        resource.setFormat(format);
        return resource;
    }

    protected static class DateFormatResource {
        protected String _format;
        protected String _prefix;
        protected String _suffix;

        public String getFormat() {
            return _format;
        }

        public void setFormat(String format) {
            _format = format;
        }

        public String getPrefix() {
            return _prefix;
        }

        public void setPrefix(String prefix) {
            _prefix = prefix;
        }

        public String getSuffix() {
            return _suffix;
        }

        public void setSuffix(String suffix) {
            _suffix = suffix;
        }
    }

    // ===================================================================================
    //                                                                        Quote Helper
    //                                                                        ============
    protected String quote(String text) {
        return Srl.quoteSingle(text);
    }

    protected String quote(String text, DateFormatResource resource) {
        String result = quote(text);
        result = Srl.connectPrefix(result, resource.getPrefix(), "");
        result = Srl.connectSuffix(result, resource.getSuffix(), "");
        return result;
    }
}
