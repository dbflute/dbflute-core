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
package org.seasar.dbflute.logic.sql2entity.analyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.seasar.dbflute.helper.StringKeyMap;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.DfStringUtil;
import org.seasar.dbflute.util.Srl;
import org.seasar.dbflute.util.Srl.ScopeInfo;

/**
 * @author jflute
 * @since 0.9.5 (2009/04/10 Friday)
 */
public class DfSql2EntityMarkAnalyzer {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final String TITLE_MARK = "[df:title]";
    protected static final String DESCRIPTION_MARK = "[df:description]";

    // ===================================================================================
    //                                                                     CustomizeEntity
    //                                                                     ===============
    /**
     * @param sql The string of SQL. (NotNull)
     * @return The name of entity. (NullAllowed: If it's not found, this returns null)
     */
    public String getCustomizeEntityName(String sql) {
        return getMarkString(sql, "#");
    }

    public boolean isDomain(final String sql) {
        final String targetString = getMarkString(sql, "+");
        return targetString != null && Srl.containsAnyIgnoreCase(targetString, "domain");
    }

    public boolean isCursor(final String sql) {
        final String targetString = getMarkString(sql, "+");
        return targetString != null && Srl.containsAnyIgnoreCase(targetString, "cursor", "cursol");
        // "cursol" is spell-miss but for compatibility with old versions
    }

    public boolean isScalar(final String sql) {
        final String targetString = getMarkString(sql, "+");
        return targetString != null && Srl.containsIgnoreCase(targetString, "scalar");
    }

    public List<DfSql2EntityMark> getCustomizeEntityPropertyTypeList(final String sql) {
        return getMarkList(sql, "##");
    }

    public List<String> getPrimaryKeyColumnNameList(final String sql) {
        if (sql == null || sql.trim().length() == 0) {
            String msg = "The sql is invalid: " + sql;
            throw new IllegalArgumentException(msg);
        }
        final List<String> retLs = new ArrayList<String>();
        final String primaryKeyColumnNameSeparatedString = getMarkString(sql, "*");
        if (Srl.is_NotNull_and_NotTrimmedEmpty(primaryKeyColumnNameSeparatedString)) {
            final StringTokenizer st = new StringTokenizer(primaryKeyColumnNameSeparatedString, ",;/\t");
            while (st.hasMoreTokens()) {
                final String nextToken = st.nextToken();
                retLs.add(nextToken.trim());
            }
        }
        return retLs;
    }

    // ===================================================================================
    //                                                                       ParameterBean
    //                                                                       =============
    /**
     * @param sql The string of SQL. (NotNull)
     * @return The name of parameter-bean. (NullAllowed: If it's not found, this returns null)
     */
    public String getParameterBeanName(final String sql) {
        final String filtered = Srl.replace(sql, "!!", ""); // remove (confusing) property mark
        return getMarkString(filtered, "!");
    }

    /**
     * @param sql The string of SQL. (NotNull)
     * @return The list of Sql2Entity mark of parameter-bean. (NotNull)
     */
    public List<DfSql2EntityMark> getParameterBeanPropertyTypeList(final String sql) {
        return getMarkList(sql, "!!");
    }

    // ===================================================================================
    //                                                                   Title/Description
    //                                                                   =================
    public String getTitle(String sql) {
        final String titleMark = TITLE_MARK;
        final String descriptionMark = DESCRIPTION_MARK;
        final int markIndex = sql.indexOf(titleMark);
        if (markIndex < 0) {
            return null;
        }
        String peace = sql.substring(markIndex + titleMark.length());
        final int descriptionIndex = peace.indexOf(descriptionMark);
        final int commentEndIndex = peace.indexOf("*/");
        if (descriptionIndex < 0 && commentEndIndex < 0) {
            String msg = "Title needs '*/' or '" + descriptionMark + "' as closing mark: " + sql;
            throw new IllegalStateException(msg);
        }
        final int titleEndIndex;
        if (descriptionIndex < 0) {
            titleEndIndex = commentEndIndex;
        } else if (commentEndIndex < 0) {
            titleEndIndex = descriptionIndex;
        } else {
            titleEndIndex = commentEndIndex < descriptionIndex ? commentEndIndex : descriptionIndex;
        }
        peace = peace.substring(0, titleEndIndex);
        peace = DfStringUtil.replace(peace, "\r\n", "\n");
        peace = DfStringUtil.replace(peace, "\n", "");
        return peace.trim();
    }

    public String getDescription(String sql) {
        final String descriptionMark = DESCRIPTION_MARK;
        final int markIndex = sql.indexOf(descriptionMark);
        if (markIndex < 0) {
            return null;
        }
        String peace = sql.substring(markIndex + descriptionMark.length());
        final int commentEndIndex = peace.indexOf("*/");
        if (commentEndIndex < 0) {
            String msg = "Description needs '*/' as closing mark: " + sql;
            throw new IllegalStateException(msg);
        }
        peace = peace.substring(0, commentEndIndex);
        peace = DfStringUtil.replace(peace, "\r\n", "\n");
        final int firstLnIndex = peace.indexOf("\n");
        if (firstLnIndex < 0) { // only one line
            return peace.trim();
        }
        final String firstLine = peace.substring(0, firstLnIndex);
        if (firstLine.trim().length() == 0) { // first line has spaces only
            // trims spaces before line separator.
            peace = peace.substring(firstLnIndex + "\n".length());
        }
        return DfStringUtil.rtrim(peace);
    }

    // ===================================================================================
    //                                                                 SelectColumnComment
    //                                                                 ===================
    public Map<String, String> getSelectColumnCommentMap(String sql) {
        final Map<String, String> commentMap = StringKeyMap.createAsFlexible();
        final List<String> splitList = Srl.splitList(sql, "\n");
        final String lineCommentMark = " --";
        final String columnCommentMark = " //";
        final String asMark = " as ";
        final String dot = ".";
        final String comma = ",";
        final String selectMark = "select ";
        for (String line : splitList) {
            if (!line.contains(lineCommentMark) || !line.contains(columnCommentMark)) {
                continue;
            }
            final String clause = Srl.substringFirstFront(line, lineCommentMark);
            final String column; // space check is later
            if (Srl.containsIgnoreCase(clause, asMark)) { // "as" exists
                // ... as MEMBER_NAME -- // ...
                // ... as MEMBER_NAME, -- // ...
                // 0 as DUMMY, -- // ...
                column = Srl.substringFirstFront(Srl.substringLastRearIgnoreCase(clause, asMark), ",");
            } else {
                if (Srl.containsAny(clause, "(", ")")) {
                    continue; // e.g. max(member.BIRTHDATE)
                }
                if (Srl.containsAny(clause, "--", "//", "/*", "*/")) {
                    continue; // irregular comment
                }
                if (clause.contains(dot)) { // "." exists
                    // , member.MEMBER_NAME -- // ...
                    // member.MEMBER_NAME, -- // ...
                    // member.MEMBER_NAME , -- // ...
                    column = Srl.substringFirstFront(Srl.substringLastRear(clause, dot), ",");
                } else if (clause.contains(comma)) { // "," exists
                    final String candidate = Srl.substringLastRear(clause, comma);
                    if (Srl.is_NotNull_and_NotTrimmedEmpty(candidate)) {
                        // , MEMBER_NAME -- // ...
                        column = candidate;
                    } else {
                        // /- - - - - - - - - - - - -
                        // , MEMBER_NAME, -- // ...
                        //  MEMBER_NAME, -- // ...
                        // - - - - - - - - - -/

                        // , MEMBER_NAME
                        //  MEMBER_NAME
                        final String removedLastComma = Srl.rtrim(Srl.substringLastFront(clause, comma));

                        // MEMBER_NAME
                        column = Srl.substringLastRear(removedLastComma, ",", " ");
                    }
                } else if (Srl.containsIgnoreCase(clause, selectMark)) { // first column
                    // select MEMBER_NAME -- // ...
                    column = Srl.substringLastRearIgnoreCase(clause, selectMark);
                } else { // may be last column
                    // MEMBER_NAME -- // ...
                    column = clause;
                }
                // small noises are allowed
                // because this map is merely for reference
            }
            if (Srl.is_Null_or_TrimmedEmpty(column)) {
                continue;
            }
            final String lineComment = Srl.substringFirstRear(line, lineCommentMark);
            if (!lineComment.contains(columnCommentMark)) {
                continue;
            }
            final String columnComment = Srl.substringFirstRear(lineComment, columnCommentMark);
            if (Srl.is_Null_or_TrimmedEmpty(columnComment)) {
                continue;
            }
            final String trimmedColumn = column.trim();
            if (!trimmedColumn.contains(" ")) { // last check
                commentMap.put(column.trim(), columnComment.trim());
            }
        }
        return commentMap;
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected String getMarkString(final String sql, final String mark) {
        final List<DfSql2EntityMark> targetList = getMarkList(sql, mark);
        return !targetList.isEmpty() ? targetList.get(0).getContent() : null;
    }

    protected List<DfSql2EntityMark> getMarkList(final String sql, final String mark) {
        if (sql == null || sql.trim().length() == 0) {
            String msg = "The sql is invalid: " + sql;
            throw new IllegalArgumentException(msg);
        }
        final List<DfSql2EntityMark> markList = getListBetweenBeginEndMark(sql, "--" + mark, mark);
        if (!markList.isEmpty()) {
            return markList;
        } else {
            // for MySQL. 
            return getListBetweenBeginEndMark(sql, "-- " + mark, mark);
        }
    }

    protected List<DfSql2EntityMark> getListBetweenBeginEndMark(String targetStr, String beginMark, String endMark) {
        final List<DfSql2EntityMark> markList = DfCollectionUtil.newArrayList();
        final List<String> lineList = Srl.splitListTrimmed(targetStr, "\n");
        final String outsideCommentMark = "//";
        for (String line : lineList) {
            final ScopeInfo scopeInfo = Srl.extractScopeFirst(line, beginMark, endMark);
            if (scopeInfo == null) {
                continue;
            }
            final DfSql2EntityMark markInfo = new DfSql2EntityMark();
            markInfo.setContent(scopeInfo.getContent());
            final int endIndex = scopeInfo.getEndIndex();
            String outsideComment = null;
            final String rearAll = line.substring(endIndex);
            if (rearAll.contains(outsideCommentMark)) {
                outsideComment = Srl.substringFirstRear(rearAll, outsideCommentMark);
            }
            markInfo.setComment(outsideComment);
            markList.add(markInfo);
        }
        return markList;
    }
}
