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
package org.dbflute.cbean.sqlclause.subquery;

import java.util.List;

import org.dbflute.cbean.cipher.GearedCipherManager;
import org.dbflute.cbean.coption.ScalarConditionOption;
import org.dbflute.cbean.sqlclause.SqlClause;
import org.dbflute.dbmeta.DBMeta;
import org.dbflute.dbmeta.info.ColumnInfo;
import org.dbflute.dbmeta.name.ColumnRealName;
import org.dbflute.dbmeta.name.ColumnRealNameProvider;
import org.dbflute.dbmeta.name.ColumnSqlName;
import org.dbflute.dbmeta.name.ColumnSqlNameProvider;

/**
 * @author jflute
 * @since 0.9.7.2 (2010/06/20 Sunday)
 */
public class ScalarCondition extends AbstractSubQuery {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _mainSubQueryIdentity; // NotNull
    protected final String _operand; // NotNull
    protected final PartitionByProvider _partitionByProvider; // NotNull

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public ScalarCondition(SubQueryPath subQueryPath, ColumnRealNameProvider localRealNameProvider,
            ColumnSqlNameProvider subQuerySqlNameProvider, int subQueryLevel, SqlClause subQuerySqlClause, String subQueryIdentity,
            DBMeta subQueryDBMeta, GearedCipherManager cipherManager, String mainSubQueryIdentity, String operand,
            PartitionByProvider partitionByProvider) {
        super(subQueryPath, localRealNameProvider, subQuerySqlNameProvider, subQueryLevel, subQuerySqlClause, subQueryIdentity,
                subQueryDBMeta, cipherManager);
        _mainSubQueryIdentity = mainSubQueryIdentity;
        _operand = operand;
        _partitionByProvider = partitionByProvider;
    }

    public static interface PartitionByProvider {
        SqlClause provideSqlClause();
    }

    // ===================================================================================
    //                                                                        Build Clause
    //                                                                        ============
    public String buildScalarCondition(String function, ScalarConditionOption option) {
        setupOptionAttribute(option);
        final ColumnRealName columnRealName; // get the specified column before it disappears at sub-query making.
        {
            final String columnDbName = _subQuerySqlClause.getSpecifiedColumnDbNameAsOne();
            if (columnDbName == null || columnDbName.trim().length() == 0) {
                throwScalarConditionInvalidColumnSpecificationException(function);
            }
            columnRealName = _localRealNameProvider.provide(columnDbName);
        }
        final String subQueryClause = buildSubQueryClause(function, option);
        final String beginMark = resolveSubQueryBeginMark(_subQueryIdentity) + ln();
        final String endMark = resolveSubQueryEndMark(_subQueryIdentity);
        final String endIndent = "       ";
        final ColumnInfo columnInfo = _subQuerySqlClause.getSpecifiedColumnInfoAsOne();
        final String specifiedExp = decrypt(columnInfo, columnRealName.toString());
        return specifiedExp + " " + _operand // left and operand
                + " (" + beginMark + subQueryClause + ln() + endIndent + ") " + endMark; // right
    }

    protected void setupOptionAttribute(ScalarConditionOption option) {
        ColumnInfo columnInfo = _subQuerySqlClause.getSpecifiedColumnInfoAsOne();
        if (columnInfo == null) {
            columnInfo = _subQuerySqlClause.getSpecifiedDerivingColumnInfoAsOne();
        }
        option.xsetTargetColumnInfo(columnInfo); // basically not null (checked before)
        option.xjudgeDatabase(_subQuerySqlClause);
    }

    protected String buildSubQueryClause(String function, ScalarConditionOption option) {
        // release ScalarCondition for compound PK
        // (compound PK restricted until 1.0.5G without special reason)
        //if (!_subQueryDBMeta.hasPrimaryKey() || _subQueryDBMeta.hasCompoundPrimaryKey()) {
        //    String msg = "The scalar-condition is unsupported when no primary key or compound primary key:";
        //    msg = msg + " table=" + _subQueryDBMeta.getTableDbName();
        //    throw new IllegalConditionBeanOperationException(msg);
        //}
        final String tableAliasName = getSubQueryLocalAliasName();
        final String derivedColumnDbName = _subQuerySqlClause.getSpecifiedColumnDbNameAsOne();
        if (derivedColumnDbName == null) {
            throwScalarConditionInvalidColumnSpecificationException(function);
        }
        final ColumnSqlName derivedColumnSqlName = getDerivedColumnSqlName();
        final ColumnRealName derivedColumnRealName = getDerivedColumnRealName();
        assertScalarConditionColumnType(function, derivedColumnDbName);
        ColumnRealName partitionByCorrelatedColumnRealName = null;
        ColumnSqlName partitionByRelatedColumnSqlName = null;
        final SqlClause partitionBySqlClause = _partitionByProvider.provideSqlClause();
        if (partitionBySqlClause != null) {
            final String partitionByColumnDbName = partitionBySqlClause.getSpecifiedColumnDbNameAsOne();
            if (partitionByColumnDbName == null) { // means empty specify or duplicate specify
                throwScalarConditionPartitionByInvalidColumnSpecificationException(function);
            }
            partitionByCorrelatedColumnRealName = _localRealNameProvider.provide(partitionByColumnDbName);
            partitionByRelatedColumnSqlName = _subQuerySqlNameProvider.provide(partitionByColumnDbName);
        }
        final String subQueryClause;
        if (_subQuerySqlClause.hasUnionQuery()) {
            subQueryClause = buildUnionSubQuerySql(function, tableAliasName, derivedColumnSqlName, derivedColumnRealName,
                    partitionByCorrelatedColumnRealName, partitionByRelatedColumnSqlName, option);
        } else {
            final String selectClause = "select " + buildFunctionPart(function, derivedColumnRealName, option, false);
            final String fromWhereClause = buildFromWhereClause(selectClause, tableAliasName, partitionByCorrelatedColumnRealName,
                    partitionByRelatedColumnSqlName);
            subQueryClause = selectClause + " " + fromWhereClause;
        }
        return resolveSubQueryLevelVariable(subQueryClause);
    }

    protected ColumnSqlName getDerivedColumnSqlName() {
        return _subQuerySqlClause.getSpecifiedResolvedColumnSqlNameAsOne();
    }

    protected ColumnRealName getDerivedColumnRealName() {
        return _subQuerySqlClause.getSpecifiedResolvedColumnRealNameAsOne(); // resolved calculation
    }

    protected String buildFromWhereClause(String selectClause, String tableAliasName, ColumnRealName partitionByCorrelatedColumnRealName,
            ColumnSqlName partitionByRelatedColumnSqlName) {
        final String fromWhereClause;
        if (partitionByCorrelatedColumnRealName != null) {
            fromWhereClause = buildCorrelationFromWhereClause(selectClause, tableAliasName, partitionByCorrelatedColumnRealName,
                    partitionByRelatedColumnSqlName, null);
        } else {
            fromWhereClause = buildPlainFromWhereClause(selectClause, tableAliasName, null);
        }
        return fromWhereClause;
    }

    // ===================================================================================
    //                                                                      Union Handling
    //                                                                      ==============
    protected String buildUnionSubQuerySql(String function, String tableAliasName // basic
            , ColumnSqlName derivedColumnSqlName, ColumnRealName derivedColumnRealName // derived
            , ColumnRealName partitionByCorrelatedColumnRealName, ColumnSqlName partitionByRelatedColumnSqlName // partition-by
            , ScalarConditionOption option) {
        final String beginMark = resolveSubQueryBeginMark(_mainSubQueryIdentity) + ln();
        final String endMark = resolveSubQueryEndMark(_mainSubQueryIdentity);
        final String mainSql =
                buildUnionMainPartClause(partitionByRelatedColumnSqlName, tableAliasName, derivedColumnRealName, derivedColumnSqlName);
        final String mainAlias = buildSubQueryMainAliasName();
        String whereJoinCondition = "";
        if (partitionByRelatedColumnSqlName != null) {
            final ColumnRealName relatedColumnRealName = ColumnRealName.create(mainAlias, partitionByRelatedColumnSqlName);
            final StringBuilder sb = new StringBuilder();
            sb.append(ln()).append(" where ");
            sb.append(relatedColumnRealName).append(" = ").append(partitionByCorrelatedColumnRealName);
            whereJoinCondition = sb.toString(); // correlation
        }
        // before making scalar condition option
        //{
        //    final ColumnSqlName pkSqlName = _subQueryDBMeta.getPrimaryInfo().getFirstColumn().getColumnSqlName();
        //    final ColumnRealName pkRealName = ColumnRealName.create(tableAliasName, pkSqlName);
        //    final String selectClause = "select " + pkRealName + ", " + derivedColumnRealName;
        //    final String fromWhereClause =
        //            buildFromWhereClause(selectClause, tableAliasName, partitionByCorrelatedColumnRealName, partitionByRelatedColumnSqlName);
        //    mainSql = selectClause + " " + fromWhereClause;
        //}
        final ColumnRealName mainDerivedColumnRealName = ColumnRealName.create(mainAlias, derivedColumnSqlName);
        return "select " + buildFunctionPart(function, mainDerivedColumnRealName, option, true) + ln() // select
                + "  from (" + beginMark + mainSql + ln() + "       ) " + mainAlias + endMark // from
                + whereJoinCondition;
    }

    // almost same as derived-referrer's logic
    protected String buildUnionMainPartClause(ColumnSqlName relatedColumnSqlName, String tableAliasName,
            ColumnRealName derivedColumnRealName, ColumnSqlName derivedColumnSqlName) {
        // derivedColumnSqlName : e.g. PURCHASE_PRICE
        // derivedRealSqlName   : might be sub-query
        final ColumnSqlName derivedRealSqlName = derivedColumnRealName.getColumnSqlName();
        final StringBuilder keySb = new StringBuilder();
        final List<ColumnInfo> pkList = _subQueryDBMeta.getPrimaryInfo().getPrimaryColumnList();
        for (ColumnInfo pk : pkList) {
            final ColumnSqlName pkSqlName = pk.getColumnSqlName();
            if (pkSqlName.equals(derivedRealSqlName) || pkSqlName.equals(relatedColumnSqlName)) {
                continue; // to suppress same columns selected
            }
            keySb.append(keySb.length() > 0 ? ", " : "");
            keySb.append(ColumnRealName.create(tableAliasName, pk.getColumnSqlName()));
        }
        if (relatedColumnSqlName != null && !relatedColumnSqlName.equals(derivedRealSqlName)) { // to suppress same columns selected
            keySb.append(keySb.length() > 0 ? ", " : "");
            keySb.append(ColumnRealName.create(tableAliasName, relatedColumnSqlName));
        }
        setupUnionMainForDerivedColumn(keySb, derivedColumnRealName, derivedColumnSqlName, derivedRealSqlName);
        return completeUnionMainWholeClause(tableAliasName, keySb);
    }

    protected void setupUnionMainForDerivedColumn(StringBuilder keySb, ColumnRealName derivedColumnRealName,
            ColumnSqlName derivedColumnSqlName, ColumnSqlName derivedRealSqlName) {
        // derivedColumnSqlName : e.g. PURCHASE_PRICE
        // derivedRealSqlName   : might be sub-query
        if (mightBeSubQueryOrCalculation(derivedRealSqlName)) { // nested sub-query or calculation
            if (!isNestedDerivedReferrer(derivedRealSqlName)) { // might be calculation (needs to resolve location)
                // #hope if correlation column is same as derived column with calculation, duplicate column error
                keySb.append(keySb.length() > 0 ? ", " : "");
                final String realNameExp = derivedColumnRealName.toString();
                final String locationResolved = _subQueryPath.resolveParameterLocationPath(realNameExp);
                keySb.append(locationResolved).append(" as ").append(derivedColumnSqlName);
            }
            // *skip here if nested sub-query, handled at function part in select clause
        } else {
            keySb.append(keySb.length() > 0 ? ", " : "");
            keySb.append(derivedColumnRealName);
        }
    }

    protected boolean mightBeSubQueryOrCalculation(ColumnSqlName derivedRealSqlName) {
        final String exp = derivedRealSqlName.toString();
        return exp.contains(" ") || exp.contains("("); // not accurate but small problem
    }

    protected String completeUnionMainWholeClause(String tableAliasName, StringBuilder keySb) {
        final String selectClause = "select " + keySb.toString();
        final String fromWhereClause = buildPlainFromWhereClause(selectClause, tableAliasName, null);
        return selectClause + " " + fromWhereClause;
    }

    // ===================================================================================
    //                                                                       Function Part
    //                                                                       =============
    protected String buildFunctionPart(String function, ColumnRealName columnRealName, ScalarConditionOption option, boolean union) {
        final String columnWithEndExp;
        {
            final String aliasDef = getDerivedReferrerNestedAliasDef();
            final ColumnSqlName columnSqlName = columnRealName.getColumnSqlName();
            if (isNestedDerivedReferrer(columnSqlName)) { // e.g. select max((select ... from ...))
                // needs to resolve location path on nested query, connect it to base location
                final String sqlNameExp = columnSqlName.toString(); // no use tableAlias here because of sub-query
                final String localtionResolved = _subQueryPath.resolveParameterLocationPath(sqlNameExp);
                final String aliasResolved = resolveNestedDerivedReferrerAliasDef(localtionResolved, aliasDef);
                columnWithEndExp = union ? resolveUnionCorrelation(aliasResolved) : aliasResolved;
            } else { // normal column derived
                // might be calculation e.g. sub1loc.DOCKSIDE_PRICE + coalesce(HUNGER_PRICE, 0)
                // so also needs to resolve location here
                final ColumnInfo derivedColumnInfo = _subQuerySqlClause.getSpecifiedColumnInfoAsOne();
                final String localtionResolved = _subQueryPath.resolveParameterLocationPath(columnRealName.toString());
                columnWithEndExp = decrypt(derivedColumnInfo, localtionResolved) + ")";
            }
        }
        return option.filterFunction(function + "(" + columnWithEndExp);
    }

    protected String resolveNestedDerivedReferrerAliasDef(String derivedExp, String aliasDef) {
        return replace(derivedExp, aliasDef, ")"); // ' as dfrelview' to ')'
    }

    protected String resolveUnionCorrelation(String derivedExp) {
        // replace e.g. ... = sub1loc.MEMBER_ID to ... = sub1main.MEMBER_ID
        final String basePointAlias = _subQuerySqlClause.getBasePointAliasName();
        final String mainAlias = buildSubQueryMainAliasName();
        return replace(derivedExp, basePointAlias, mainAlias);
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected boolean isNestedDerivedReferrer(String name) {
        return name.contains(getDerivedReferrerNestedAliasDef());
    }

    protected boolean isNestedDerivedReferrer(ColumnSqlName name) {
        return name.toString().contains(getDerivedReferrerNestedAliasDef());
    }

    protected String getDerivedReferrerNestedAliasDef() {
        return " as " + getDerivedReferrerNestedAlias();
    }

    protected String getDerivedReferrerNestedAlias() {
        return _subQuerySqlClause.getDerivedReferrerNestedAlias();
    }

    // ===================================================================================
    //                                                                    Assert/Exception
    //                                                                    ================
    protected void throwScalarConditionInvalidColumnSpecificationException(String function) {
        createCBExThrower().throwScalarConditionInvalidColumnSpecificationException(function);
    }

    protected void throwScalarConditionPartitionByInvalidColumnSpecificationException(String function) {
        createCBExThrower().throwScalarConditionPartitionByInvalidColumnSpecificationException(function);
    }

    protected void assertScalarConditionColumnType(String function, String derivedColumnDbName) {
        if ("sum".equalsIgnoreCase(function) || "avg".equalsIgnoreCase(function)) {
            final ColumnInfo columnInfo = _subQueryDBMeta.findColumnInfo(derivedColumnDbName);
            final Class<?> deriveColumnType = columnInfo.getObjectNativeType();
            if (!columnInfo.isObjectNativeTypeNumber()) {
                throwScalarConditionUnmatchedColumnTypeException(function, derivedColumnDbName, deriveColumnType);
            }
        }
    }

    protected void throwScalarConditionUnmatchedColumnTypeException(String function, String derivedColumnDbName,
            Class<?> derivedColumnType) {
        createCBExThrower().throwScalarConditionUnmatchedColumnTypeException(function, derivedColumnDbName, derivedColumnType);
    }
}
