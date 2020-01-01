/*
 * Copyright 2014-2020 the original author or authors.
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
package org.dbflute.twowaysql;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Locale;
import java.util.TimeZone;
import java.util.function.BooleanSupplier;

import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.system.DBFluteSystem;
import org.dbflute.twowaysql.style.BoundDateDisplayStyle;
import org.dbflute.twowaysql.style.BoundDateDisplayTimeZoneProvider;
import org.dbflute.util.DfTypeUtil;
import org.dbflute.util.Srl;

/**
 * @author modified by jflute (originated in S2Dao)
 */
public class DisplaySqlBuilder {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String DEFAULT_DATE_FORMAT = DfTypeUtil.HYPHENED_DATE_PATTERN;
    public static final String DEFAULT_TIMESTAMP_FORMAT = DfTypeUtil.HYPHENED_TIMESTAMP_PATTERN;
    public static final String DEFAULT_TIME_FORMAT = DfTypeUtil.COLONED_TIME_PATTERN;
    public static final String NULL = "null";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final BoundDateDisplayStyle _dateDisplayStyle; // not null

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DisplaySqlBuilder(BoundDateDisplayStyle dateDisplayStyle) {
        if (dateDisplayStyle == null) {
            String msg = "The argument 'dateDisplayStyle' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        _dateDisplayStyle = dateDisplayStyle;
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
        } else if (bindVariable instanceof LocalDate) { // #date_parade
            return buildLocalDateText(bindVariable);
        } else if (bindVariable instanceof LocalDateTime) {
            return buildLocalDateTimeText(bindVariable);
        } else if (bindVariable instanceof LocalTime) {
            return buildLocalTimeText(bindVariable);
        } else if (bindVariable instanceof Timestamp) { // should be before util.Date
            return buildTimestampText(bindVariable);
        } else if (bindVariable instanceof Time) { // should be before util.Date
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
    protected String buildLocalDateText(Object bindVariable) {
        return processLocalDateDisplay((LocalDate) bindVariable, getLocalDatePattern());
    }

    protected String buildLocalDateTimeText(Object bindVariable) {
        return processLocalDateTimeDisplay((LocalDateTime) bindVariable, getLocalDateTimePattern());
    }

    protected String buildLocalTimeText(Object bindVariable) {
        return processLocalTimeDisplay((LocalTime) bindVariable, getLocalTimePattern());
    }

    protected String buildDateText(Object bindVariable) {
        return processDateDisplay((java.util.Date) bindVariable, getDatePattern());
    }

    protected String buildTimestampText(Object bindVariable) {
        return processDateDisplay((java.util.Date) bindVariable, getTimestampPattern());
    }

    protected String buildTimeText(Object bindVariable) {
        return quote(DfTypeUtil.toString((java.util.Date) bindVariable, getTimePattern()));
    }

    protected String processLocalDateDisplay(LocalDate date, String format) {
        final DateFormatResource resource = analyzeDateFormat(format);
        String disp = DfTypeUtil.toStringDate(date, resource.getFormat());
        disp = filterBCPrefix(disp, () -> isBCPrefixTarget(date, resource));
        return quote(disp, resource);
    }

    protected String processLocalDateTimeDisplay(LocalDateTime date, String format) {
        final DateFormatResource resource = analyzeDateFormat(format);
        String disp = DfTypeUtil.toStringDate(date, resource.getFormat());
        disp = filterBCPrefix(disp, () -> isBCPrefixTarget(date, resource));
        return quote(disp, resource);
    }

    protected String processLocalTimeDisplay(LocalTime date, String format) {
        final DateFormatResource resource = analyzeDateFormat(format);
        final String disp = DfTypeUtil.toStringDate(date, resource.getFormat());
        return quote(disp, resource);
    }

    protected String processDateDisplay(java.util.Date date, String format) {
        final DateFormatResource resource = analyzeDateFormat(format);
        final TimeZone realZone = getRealTimeZone();
        final Locale realLocale = getRealLocale();
        String disp = DfTypeUtil.toStringDate(date, realZone, resource.getFormat(), realLocale);
        disp = filterBCPrefix(disp, () -> isBCPrefixTarget(date, resource));
        return quote(disp, resource);
    }

    protected String filterBCPrefix(String disp, BooleanSupplier noArgLambda) {
        // fixed specification, basically not use 'G'
        // in pattern at least default pattern
        // because it should be displayed only when BC date
        return noArgLambda.getAsBoolean() ? ("BC" + disp) : disp;
    }

    // -----------------------------------------------------
    //                                         Style Pattern
    //                                         -------------
    protected String getLocalDatePattern() {
        final String datePattern = _dateDisplayStyle.getDatePattern();
        return datePattern != null ? datePattern : DEFAULT_DATE_FORMAT;
    }

    protected String getLocalDateTimePattern() {
        final String datePattern = _dateDisplayStyle.getTimestampPattern();
        return datePattern != null ? datePattern : DEFAULT_TIMESTAMP_FORMAT;
    }

    protected String getLocalTimePattern() {
        final String datePattern = _dateDisplayStyle.getTimePattern();
        return datePattern != null ? datePattern : DEFAULT_TIME_FORMAT;
    }

    protected String getDatePattern() {
        final String datePattern = _dateDisplayStyle.getDatePattern();
        return datePattern != null ? datePattern : DEFAULT_DATE_FORMAT;
    }

    protected String getTimestampPattern() {
        final String timestampPattern = _dateDisplayStyle.getTimestampPattern();
        return timestampPattern != null ? timestampPattern : DEFAULT_TIMESTAMP_FORMAT;
    }

    protected String getTimePattern() {
        final String timePattern = _dateDisplayStyle.getTimePattern();
        return timePattern != null ? timePattern : DEFAULT_TIME_FORMAT;
    }

    protected TimeZone getRealTimeZone() {
        final BoundDateDisplayTimeZoneProvider provider = _dateDisplayStyle.getTimeZoneProvider();
        return provider != null ? provider.provide() : getDBFluteSystemFinalTimeZone();
    }

    protected TimeZone getDBFluteSystemFinalTimeZone() {
        return DBFluteSystem.getFinalTimeZone();
    }

    protected Locale getRealLocale() {
        return getDBFluteSystemFinalLocale(); // no provider because of basically debug string
    }

    protected Locale getDBFluteSystemFinalLocale() {
        return DBFluteSystem.getFinalLocale();
    }

    // -----------------------------------------------------
    //                                              AD or BC
    //                                              --------
    protected boolean isBCPrefixTarget(java.util.Date date, DateFormatResource resource) {
        return DfTypeUtil.isDateBC(date) && judgeBCPrefixTargetFormat(resource.getFormat());
    }

    protected boolean isBCPrefixTarget(LocalDate date, DateFormatResource resource) {
        return DfTypeUtil.isLocalDateBC(date) && judgeBCPrefixTargetFormat(resource.getFormat());
    }

    protected boolean isBCPrefixTarget(LocalDateTime date, DateFormatResource resource) {
        return DfTypeUtil.isLocalDateBC(date.toLocalDate()) && judgeBCPrefixTargetFormat(resource.getFormat());
    }

    protected boolean judgeBCPrefixTargetFormat(String format) {
        return format.startsWith("yyyy") && !format.contains("G");
    }

    // -----------------------------------------------------
    //                                    Analyze DateFormat
    //                                    ------------------
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
        final int dfmarkBeginIndex = format.indexOf(dfMark);
        if (dfmarkBeginIndex >= 0) {
            final String rear = format.substring(dfmarkBeginIndex + dfMark.length());
            final int dfMarkEndIndex = rear.indexOf("}");
            if (dfMarkEndIndex >= 0) {
                resource.setFormat(rear.substring(0, dfMarkEndIndex));
                resource.setPrefix(format.substring(0, dfmarkBeginIndex));
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
