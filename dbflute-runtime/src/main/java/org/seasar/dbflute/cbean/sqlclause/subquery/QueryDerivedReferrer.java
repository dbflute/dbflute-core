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

import java.util.List;

import org.seasar.dbflute.cbean.ConditionBean;
import org.seasar.dbflute.cbean.chelper.HpCalcSpecification;
import org.seasar.dbflute.cbean.chelper.HpSpecifiedColumn;
import org.seasar.dbflute.cbean.cipher.GearedCipherManager;
import org.seasar.dbflute.cbean.sqlclause.SqlClause;
import org.seasar.dbflute.dbmeta.DBMeta;
import org.seasar.dbflute.dbmeta.name.ColumnRealName;
import org.seasar.dbflute.dbmeta.name.ColumnRealNameProvider;
import org.seasar.dbflute.dbmeta.name.ColumnSqlName;
import org.seasar.dbflute.dbmeta.name.ColumnSqlNameProvider;

/**
 * @author jflute
 * @since 0.9.7.2 (2010/06/20 Sunday)
 */
public class QueryDerivedReferrer extends DerivedReferrer {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _operand;
    protected final Object _value; // null allowed: when IsNull or IsNotNull
    protected final String _parameterPath;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public QueryDerivedReferrer(SubQueryPath subQueryPath, ColumnRealNameProvider localRealNameProvider,
            ColumnSqlNameProvider subQuerySqlNameProvider, int subQueryLevel, SqlClause subQuerySqlClause,
            String subQueryIdentity, DBMeta subQueryDBMeta, GearedCipherManager cipherManager,
            String mainSubQueryIdentity, String operand, Object value, String parameterPath) {
        super(subQueryPath, localRealNameProvider, subQuerySqlNameProvider, subQueryLevel, subQuerySqlClause,
                subQueryIdentity, subQueryDBMeta, cipherManager, mainSubQueryIdentity);
        _operand = operand;
        _value = value;
        _parameterPath = parameterPath;
    }

    // ===================================================================================
    //                                                                        Build Clause
    //                                                                        ============
    @Override
    protected String doBuildDerivedReferrer(String function, ColumnRealName columnRealName,
            ColumnSqlName relatedColumnSqlName, String subQueryClause, String beginMark, String endMark,
            String endIndent) {
        return buildCompleteClause(subQueryClause, beginMark, endMark, endIndent);
    }

    @Override
    protected String doBuildDerivedReferrer(String function, ColumnRealName[] correlatedColumnRealNames,
            ColumnSqlName[] relatedColumnSqlNames, String subQueryClause, String beginMark, String endMark,
            String endIndent) {
        return buildCompleteClause(subQueryClause, beginMark, endMark, endIndent);
    }

    protected String buildCompleteClause(String subQueryClause, String beginMark, String endMark, String endIndent) {
        final StringBuilder sb = new StringBuilder();
        sb.append("(").append(beginMark).append(subQueryClause);
        sb.append(ln()).append(endIndent).append(") ");
        sb.append(_operand); // e.g. "(select max(...) from ...) >"
        if (_value != null) {
            sb.append(" "); // e.g. "(select max(...) from ...) > "
            if (_value instanceof HpSpecifiedColumn) { // DreamCruise
                // e.g. "(select max(...) from ...) > ZAMBINI_PRICE"
                buildRightClauseDreamCruiseExp(sb);
            } else { // normally here
                // e.g. "(select max(...) from ...) > 3"
                buildRightClauseNormalValue(sb);
            }
        }
        sb.append(endMark);
        return sb.toString();
    }

    protected void buildRightClauseDreamCruiseExp(StringBuilder sb) {
        final HpSpecifiedColumn specifiedColumn = (HpSpecifiedColumn) _value;
        final String columnExp = specifiedColumn.toColumnRealName().toString();
        final String appended;
        if (specifiedColumn.hasSpecifyCalculation()) {
            specifiedColumn.xinitSpecifyCalculation();
            final HpCalcSpecification<ConditionBean> calcSpecification = specifiedColumn.getSpecifyCalculation();
            appended = calcSpecification.buildStatementToSpecifidName(columnExp);
        } else {
            appended = columnExp;
        }
        sb.append(appended);
    }

    protected void buildRightClauseNormalValue(final StringBuilder sb) {
        final String prefix = "/*pmb.";
        final String suffix = "*/null";
        final String parameter;
        if (isOperandBetween() && isValueListType()) {
            final String fromParameter = buildListParameter(prefix, 0, suffix);
            final String toParameter = buildListParameter(prefix, 1, suffix);
            parameter = fromParameter + " and " + toParameter;
        } else {
            parameter = prefix + _parameterPath + suffix;
        }
        sb.append(parameter);
    }

    protected boolean isOperandBetween() {
        return "between".equalsIgnoreCase(_operand);
    }

    protected boolean isValueListType() {
        return _value instanceof List<?>;
    }

    protected String buildListParameter(String prefix, int index, String suffix) {
        return prefix + _parameterPath + ".get(" + index + ")" + suffix;
    }

    @Override
    protected void throwDerivedReferrerInvalidColumnSpecificationException(String function) {
        createCBExThrower().throwQueryDerivedReferrerInvalidColumnSpecificationException(function);
    }

    @Override
    protected void doAssertDerivedReferrerColumnType(String function, String derivedColumnDbName,
            Class<?> derivedColumnType) {
        final Object value = _value;
        if ("sum".equalsIgnoreCase(function) || "avg".equalsIgnoreCase(function)) {
            if (!Number.class.isAssignableFrom(derivedColumnType)) {
                throwQueryDerivedReferrerUnmatchedColumnTypeException(function, derivedColumnDbName, derivedColumnType);
            }
        }
        if (value != null) {
            final Class<?> parameterType = value.getClass();
            if (String.class.isAssignableFrom(derivedColumnType)) {
                if (!String.class.isAssignableFrom(parameterType)) {
                    throwQueryDerivedReferrerUnmatchedColumnTypeException(function, derivedColumnDbName,
                            derivedColumnType);
                }
            }
            if (Number.class.isAssignableFrom(derivedColumnType)) {
                if (!Number.class.isAssignableFrom(parameterType)) {
                    throwQueryDerivedReferrerUnmatchedColumnTypeException(function, derivedColumnDbName,
                            derivedColumnType);
                }
            }
            if (java.util.Date.class.isAssignableFrom(derivedColumnType)) {
                if (!java.util.Date.class.isAssignableFrom(parameterType)) {
                    throwQueryDerivedReferrerUnmatchedColumnTypeException(function, derivedColumnDbName,
                            derivedColumnType);
                }
            }
        }
    }

    protected void throwQueryDerivedReferrerUnmatchedColumnTypeException(String function, String derivedColumnDbName,
            Class<?> derivedColumnType) {
        createCBExThrower().throwQueryDerivedReferrerUnmatchedColumnTypeException(function, derivedColumnDbName,
                derivedColumnType, _value);
    }
}
