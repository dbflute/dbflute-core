/*
 * Copyright 2014-2025 the original author or authors.
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
package org.dbflute.cbean.sqlclause.subquery;

import java.io.Serializable;

import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.system.DBFluteSystem;
import org.dbflute.util.Srl;
import org.dbflute.util.Srl.ScopeInfo;

/**
 * @author jflute
 * @since 0.9.7.2 (2010/06/20 Sunday)
 */
public class SubQueryIndentProcessor implements Serializable {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The serial version UID for object serialization. (Default) */
    private static final long serialVersionUID = 1L;

    public static final String BEGIN_MARK_PREFIX = "--#df:sqbegin#";
    public static final String END_MARK_PREFIX = "--#df:sqend#";
    public static final String IDENTITY_TERMINAL = "#df:idterm#";

    // ===================================================================================
    //                                                                    Resolve Identity
    //                                                                    ================
    public String resolveSubQueryBeginMark(String subQueryIdentity) {
        return BEGIN_MARK_PREFIX + subQueryIdentity + IDENTITY_TERMINAL;
    }

    public String resolveSubQueryEndMark(String subQueryIdentity) {
        return END_MARK_PREFIX + subQueryIdentity + IDENTITY_TERMINAL;
    }

    // ===================================================================================
    //                                                                      Process Indent
    //                                                                      ==============
    public String processSubQueryIndent(final String sql, final String preIndent, final String originalSql) {
        // /= = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
        // it's a super core logic for formatting SQL generated by ConditionBean
        // = = = = = = = = = =/
        final String beginMarkPrefix = BEGIN_MARK_PREFIX;
        if (!sql.contains(beginMarkPrefix)) {
            return sql;
        }
        final String[] lines = sql.split(ln());
        final String endMarkPrefix = END_MARK_PREFIX;
        final String identityTerminal = IDENTITY_TERMINAL;
        final int terminalLength = identityTerminal.length();
        final StringBuilder mainSb = new StringBuilder();
        StringBuilder subSb = null;
        boolean throughBegin = false;
        boolean throughBeginFirst = false;
        String subQueryIdentity = null;
        String indent = null;
        for (String line : lines) {
            if (!throughBegin) {
                if (line.contains(beginMarkPrefix)) { // begin line
                    throughBegin = true;
                    subSb = new StringBuilder();
                    final int markIndex = line.indexOf(beginMarkPrefix);
                    final int terminalIndex = line.indexOf(identityTerminal);
                    if (terminalIndex < 0) {
                        String msg = "Identity terminal was not found at the begin line: [" + line + "]";
                        throw new SubQueryIndentFailureException(msg);
                    }
                    final String clause = line.substring(0, markIndex) + line.substring(terminalIndex + terminalLength);
                    subQueryIdentity = line.substring(markIndex + beginMarkPrefix.length(), terminalIndex);
                    subSb.append(clause);
                    indent = buildSpaceBar(markIndex - preIndent.length());
                } else { // normal line
                    if (needsLineConnection(mainSb)) {
                        mainSb.append(ln());
                    }
                    mainSb.append(line).append(ln());
                }
            } else {
                // - - - - - - - -
                // In begin to end
                // - - - - - - - -
                if (line.contains(endMarkPrefix + subQueryIdentity)) { // end line
                    final int markIndex = line.indexOf(endMarkPrefix);
                    final int terminalIndex = line.indexOf(identityTerminal);
                    if (terminalIndex < 0) {
                        String msg = "Identity terminal was not found at the begin line: [" + line + "]";
                        throw new SubQueryIndentFailureException(msg);
                    }
                    final String clause = line.substring(0, markIndex);
                    // e.g. " + 1" of ColumnQuery calculation for right column
                    final String preRemainder = line.substring(terminalIndex + terminalLength);
                    subSb.append(clause);
                    final String subQuerySql = subSb.toString();
                    final String nestedPreIndent = preIndent + indent;
                    final String currentSql = processSubQueryIndent(subQuerySql, nestedPreIndent, originalSql);
                    if (needsLineConnection(mainSb)) {
                        mainSb.append(ln());
                    }
                    mainSb.append(currentSql);
                    if (Srl.is_NotNull_and_NotTrimmedEmpty(preRemainder)) {
                        mainSb.append(preRemainder);
                    }
                    throughBegin = false;
                    throughBeginFirst = false;
                } else { // scope line
                    if (!throughBeginFirst) {
                        subSb.append(line.trim()).append(ln());
                        throughBeginFirst = true;
                    } else {
                        subSb.append(indent).append(line).append(ln());
                    }
                }
            }
        }
        final String filteredSql = Srl.rtrim(mainSb.toString()); // removed latest line separator
        if (throughBegin) {
            throwSubQueryNotFoundEndMarkException(subQueryIdentity, sql, filteredSql, originalSql);
        }
        if (filteredSql.contains(beginMarkPrefix)) {
            throwSubQueryAnyBeginMarkNotHandledException(subQueryIdentity, sql, filteredSql, originalSql);
        }
        return filteredSql;
    }

    protected boolean needsLineConnection(StringBuilder sb) {
        final int length = sb.length();
        if (length == 0) {
            return false;
        }
        final String lastStr = sb.substring(length - 1, length);
        if (lastStr.equals(ln())) {
            return false;
        }
        // if a previous line is sub-query,
        // it may not end with a line separator
        return true;
    }

    protected void throwSubQueryNotFoundEndMarkException(String subQueryIdentity, String sql, String filteredSql, String originalSql) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the end mark for sub-query.");
        br.addItem("SubQueryIdentity");
        br.addElement(subQueryIdentity);
        br.addItem("Before Filter");
        br.addElement(sql);
        br.addItem("After Filter");
        br.addElement(filteredSql);
        br.addItem("Original SQL");
        br.addElement(originalSql);
        final String msg = br.buildExceptionMessage();
        throw new SubQueryIndentFailureException(msg);
    }

    protected void throwSubQueryAnyBeginMarkNotHandledException(String subQueryIdentity, String sql, String filteredSql,
            String originalSql) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Any begin marks are not handled.");
        br.addItem("SubQueryIdentity");
        br.addElement(subQueryIdentity);
        br.addItem("Before Filter");
        br.addElement(sql);
        br.addItem("After Filter");
        br.addElement(filteredSql);
        br.addItem("Original SQL");
        br.addElement(originalSql);
        final String msg = br.buildExceptionMessage();
        throw new SubQueryIndentFailureException(msg);
    }

    public static class SubQueryIndentFailureException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public SubQueryIndentFailureException(String msg) {
            super(msg);
        }
    }

    // ===================================================================================
    //                                                                        Space Helper
    //                                                                        ============
    protected String buildSpaceBar(int size) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            sb.append(" ");
        }
        return sb.toString();
    }

    // ===================================================================================
    //                                                                       Determination
    //                                                                       =============
    public static boolean hasSubQueryBeginOnFirstLine(String exp) {
        final String sqbegin = BEGIN_MARK_PREFIX;
        if (exp.contains(ln())) {
            final String firstLine = Srl.substringFirstFront(exp, ln());
            if (firstLine.contains(sqbegin)) {
                return true; // a first line has sub-query end mark
            }
        }
        return false;
    }

    public static boolean hasSubQueryEndOnLastLine(String exp) {
        final String sqend = END_MARK_PREFIX;
        if (exp.contains(ln())) {
            final String lastLine = Srl.substringLastRear(exp, ln());
            if (lastLine.contains(sqend)) {
                return true; // a last line has sub-query end mark
            }
        }
        return false;
    }

    // should be checked before calling if on last line
    public static String moveSubQueryEndToRear(String exp) {
        final String sqend = END_MARK_PREFIX;
        final String idterm = IDENTITY_TERMINAL;
        final ScopeInfo lastScope = Srl.extractScopeLast(exp, sqend, idterm);
        final String scopeStr = lastScope.getScope();
        final String front = exp.substring(0, lastScope.getBeginIndex());
        final String rear = exp.substring(lastScope.getEndIndex());
        return front + rear + scopeStr;
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String replaceString(String text, String fromText, String toText) {
        return Srl.replace(text, fromText, toText);
    }

    protected static String ln() {
        return DBFluteSystem.ln();
    }
}
