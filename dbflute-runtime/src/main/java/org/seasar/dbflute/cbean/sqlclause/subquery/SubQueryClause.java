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
package org.seasar.dbflute.cbean.sqlclause.subquery;

import org.seasar.dbflute.cbean.sqlclause.SqlClause;
import org.seasar.dbflute.dbmeta.name.ColumnRealName;
import org.seasar.dbflute.dbmeta.name.ColumnSqlName;
import org.seasar.dbflute.resource.DBFluteSystem;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.9.7.2 (2010/06/20 Sunday)
 */
public class SubQueryClause {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final SubQueryPath _subQueryPath;
    protected final String _selectClause; // needed for union
    protected final SqlClause _subQuerySqlClause;
    protected final String _localAliasName;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * @param subQueryPath The property path of sub-query. (NotNull)
     * @param selectClause The select clause of sub-query. (NotNull)
     * @param subQuerySqlClause The SQL clause for sub-query. (NotNull)
     * @param localAliasName The alias name of sub-query local table. (NullAllowed: if plain)
     */
    public SubQueryClause(SubQueryPath subQueryPath, String selectClause, SqlClause subQuerySqlClause,
            String localAliasName) {
        _subQueryPath = subQueryPath;
        _selectClause = selectClause;
        _subQuerySqlClause = subQuerySqlClause;
        _localAliasName = localAliasName;
    }

    // ===================================================================================
    //                                                                               Plain
    //                                                                               =====
    public String buildPlainSubQueryFromWhereClause(String correlatedFixedCondition) {
        if (correlatedFixedCondition == null) { // basically here
            String clause = _subQuerySqlClause.getClauseFromWhereWithUnionTemplate();
            clause = resolveParameterLocationPath(clause, _subQueryPath);
            clause = replaceString(clause, getUnionSelectClauseMark(), _selectClause);
            clause = replaceString(clause, getUnionWhereClauseMark(), "");
            clause = replaceString(clause, getUnionWhereFirstConditionMark(), "");
            return clause;
        }
        // e.g. biz-many-to-one
        final String correlationCondition = correlatedFixedCondition; // only fixed condition
        final String firstConditionAfter = ln() + "   and ";
        String clause = _subQuerySqlClause.getClauseFromWhereWithWhereUnionTemplate();
        clause = resolveParameterLocationPath(clause, _subQueryPath);
        clause = replaceString(clause, getWhereClauseMark(), ln() + " where " + correlationCondition);
        clause = replaceString(clause, getWhereFirstConditionMark(), correlationCondition + firstConditionAfter);
        clause = replaceString(clause, getUnionSelectClauseMark(), _selectClause);
        clause = replaceString(clause, getUnionWhereClauseMark(), ln() + " where " + correlationCondition);
        clause = replaceString(clause, getUnionWhereFirstConditionMark(), correlationCondition + firstConditionAfter);
        return clause;
    }

    // ===================================================================================
    //                                                                         Correlation
    //                                                                         ===========
    /**
     * Build the clause of correlation sub-query from from-where clause.
     * @param correlatedColumnRealName The real name of correlated column that is main-query table's column. (NotNull)
     * @param relatedColumnSqlName The real name of related column that is sub-query table's column. (NotNull)
     * @param correlatedFixedCondition The fixed condition as correlated condition. (NullAllowed)
     * @return The clause string of correlation sub-query. (NotNull)
     */
    public String buildCorrelationSubQueryFromWhereClause(ColumnRealName correlatedColumnRealName,
            ColumnSqlName relatedColumnSqlName, String correlatedFixedCondition) {
        final String clause = xprepareCorrelationSubQueryFromWhereClause();
        final String joinCondition = _localAliasName + "." + relatedColumnSqlName + " = " + correlatedColumnRealName;
        return xreplaceCorrelationSubQueryFromWhereClause(clause, joinCondition, correlatedFixedCondition);
    }

    /**
     * Build the clause of correlation sub-query from from-where clause.
     * @param correlatedColumnRealNames The real names of correlated column that is main-query table's column. (NotNull)
     * @param relatedColumnSqlNames The real names of related column that is sub-query table's column. (NotNull)
     * @param correlatedFixedCondition The fixed condition as correlated condition. (NullAllowed)
     * @return The clause string of correlation sub-query. (NotNull)
     */
    public String buildCorrelationSubQueryFromWhereClause(ColumnRealName[] correlatedColumnRealNames,
            ColumnSqlName[] relatedColumnSqlNames, String correlatedFixedCondition) {
        String clause = xprepareCorrelationSubQueryFromWhereClause();

        final String joinCondition;
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < relatedColumnSqlNames.length; i++) {
            if (sb.length() > 0) {
                sb.append(ln()).append("   and ");
            }
            sb.append(_localAliasName).append(".").append(relatedColumnSqlNames[i]);
            sb.append(" = ").append(correlatedColumnRealNames[i]);
        }
        joinCondition = sb.toString();

        clause = xreplaceCorrelationSubQueryFromWhereClause(clause, joinCondition, correlatedFixedCondition);
        return clause;
    }

    protected String xprepareCorrelationSubQueryFromWhereClause() {
        final String clause = _subQuerySqlClause.getClauseFromWhereWithWhereUnionTemplate();
        return resolveParameterLocationPath(clause, _subQueryPath);
    }

    protected String xreplaceCorrelationSubQueryFromWhereClause(String clause, String joinCondition,
            String fixedCondition) {
        final String correlationCondition;
        if (fixedCondition != null && fixedCondition.trim().length() > 0) {
            correlationCondition = joinCondition + ln() + "   and " + fixedCondition;
        } else {
            correlationCondition = joinCondition;
        }
        final String firstConditionAfter = ln() + "   and ";
        clause = replaceString(clause, getWhereClauseMark(), ln() + " where " + correlationCondition);
        clause = replaceString(clause, getWhereFirstConditionMark(), correlationCondition + firstConditionAfter);
        clause = replaceString(clause, getUnionSelectClauseMark(), _selectClause);
        clause = replaceString(clause, getUnionWhereClauseMark(), ln() + " where " + correlationCondition);
        clause = replaceString(clause, getUnionWhereFirstConditionMark(), correlationCondition + firstConditionAfter);
        return clause;
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected String resolveParameterLocationPath(String clause, SubQueryPath subQueryPath) {
        return subQueryPath.resolveParameterLocationPath(clause);
    }

    // ===================================================================================
    //                                                                          Alias Name
    //                                                                          ==========
    protected String getBasePointAliasName() {
        return _subQuerySqlClause.getBasePointAliasName();
    }

    // ===================================================================================
    //                                                                       Template Mark
    //                                                                       =============
    protected String getWhereClauseMark() {
        return _subQuerySqlClause.getWhereClauseMark();
    }

    protected String getWhereFirstConditionMark() {
        return _subQuerySqlClause.getWhereFirstConditionMark();
    }

    protected String getUnionSelectClauseMark() {
        return _subQuerySqlClause.getUnionSelectClauseMark();
    }

    protected String getUnionWhereClauseMark() {
        return _subQuerySqlClause.getUnionWhereClauseMark();
    }

    protected String getUnionWhereFirstConditionMark() {
        return _subQuerySqlClause.getUnionWhereFirstConditionMark();
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected final String replaceString(String text, String fromText, String toText) {
        return Srl.replace(text, fromText, toText);
    }

    protected String initCap(String str) {
        return Srl.initCap(str);
    }

    protected String initUncap(String str) {
        return Srl.initUncap(str);
    }

    protected String ln() {
        return DBFluteSystem.getBasicLn();
    }
}
