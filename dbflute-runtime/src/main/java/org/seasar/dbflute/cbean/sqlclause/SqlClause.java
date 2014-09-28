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
package org.seasar.dbflute.cbean.sqlclause;

import java.util.List;
import java.util.Map;

import org.seasar.dbflute.cbean.ManualOrderBean;
import org.seasar.dbflute.cbean.chelper.HpCBPurpose;
import org.seasar.dbflute.cbean.chelper.HpDerivingSubQueryInfo;
import org.seasar.dbflute.cbean.chelper.HpInvalidQueryInfo;
import org.seasar.dbflute.cbean.chelper.HpSpecifiedColumn;
import org.seasar.dbflute.cbean.cipher.ColumnFunctionCipher;
import org.seasar.dbflute.cbean.cipher.GearedCipherManager;
import org.seasar.dbflute.cbean.ckey.ConditionKey;
import org.seasar.dbflute.cbean.coption.ConditionOption;
import org.seasar.dbflute.cbean.coption.LikeSearchOption;
import org.seasar.dbflute.cbean.coption.ScalarSelectOption;
import org.seasar.dbflute.cbean.cvalue.ConditionValue;
import org.seasar.dbflute.cbean.sqlclause.clause.ClauseLazyReflector;
import org.seasar.dbflute.cbean.sqlclause.clause.SelectClauseType;
import org.seasar.dbflute.cbean.sqlclause.join.FixedConditionLazyChecker;
import org.seasar.dbflute.cbean.sqlclause.join.FixedConditionResolver;
import org.seasar.dbflute.cbean.sqlclause.join.LeftOuterJoinInfo;
import org.seasar.dbflute.cbean.sqlclause.orderby.OrderByClause;
import org.seasar.dbflute.cbean.sqlclause.orderby.OrderByElement;
import org.seasar.dbflute.cbean.sqlclause.query.QueryClause;
import org.seasar.dbflute.cbean.sqlclause.query.QueryClauseFilter;
import org.seasar.dbflute.cbean.sqlclause.query.QueryUsedAliasInfo;
import org.seasar.dbflute.cbean.sqlclause.select.SelectedRelationColumn;
import org.seasar.dbflute.cbean.sqlclause.select.SpecifiedSelectColumnHandler;
import org.seasar.dbflute.cbean.sqlclause.union.UnionClauseProvider;
import org.seasar.dbflute.dbmeta.info.ColumnInfo;
import org.seasar.dbflute.dbmeta.info.ForeignInfo;
import org.seasar.dbflute.dbmeta.name.ColumnRealName;
import org.seasar.dbflute.dbmeta.name.ColumnSqlName;
import org.seasar.dbflute.dbway.DBWay;

/**
 * The interface of SQL clause. <br />
 *
 * <p>And this also has a role of a container for common info
 * between the top level condition-bean and related condition-queries.</p>
 *
 * <p>It has many histories... e.g. structures, method names,
 * it might be hard to read but no big refactoring
 * because the histories and memories are also documents.</p>
 *
 * @author jflute
 */
public interface SqlClause {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The delimiter of relation path. The relation path is e.g. _0_3  */
    String RELATION_PATH_DELIMITER = "_";

    /** The alias name of base point table for on-query. */
    String BASE_POINT_ALIAS_NAME = "dfloc";

    /** The entity number of base point table for internal handling. */
    String BASE_POINT_HANDLING_ENTITY_NO = "loc00";

    // ===================================================================================
    //                                                                      SubQuery Level
    //                                                                      ==============
    /**
     * Get the hierarchy level of sub-query.
     * @return The hierarchy level of sub-query. (NotMinus: if zero, not for sub-query)
     */
    int getSubQueryLevel();

    /**
     * Set up this SQL for sub-query.
     * @param subQueryLevel The hierarchy level of sub-query. (NotMinus: if zero, not for sub-query)
     */
    void setupForSubQuery(int subQueryLevel);

    /**
     * Is this SQL for sub-query?
     * @return The determination, true or false.
     */
    boolean isForSubQuery();

    // ===================================================================================
    //                                                                        Whole Clause
    //                                                                        ============
    // -----------------------------------------------------
    //                                       Complete Clause
    //                                       ---------------
    /**
     * Get the clause of all parts.
     * <pre>
     * select [base-table-columns], [join-table-columns]
     *   from [base-table] left outer join [join-table] [join-alias] on [join-condition]
     *  where [base-table].[column] = [value] and [join-alias].[column] is null
     *  order by [base-table].[column] asc, [join-alias].[column] desc
     *  for update
     * </pre>
     * @return The clause of all parts. (NotNull)
     */
    String getClause();

    // -----------------------------------------------------
    //                                       Fragment Clause
    //                                       ---------------
    /**
     * Get from-where clause without select and orderBy and sqlSuffix. <br />
     * Basically for subQuery and selectCount. <br />
     * You should handle UnionSelectClauseMark and UnionWhereClauseMark and UnionWhereFirstConditionMark in clause.
     * @return The 'from-where' clause(contains union) without 'select' and 'orderBy' and 'sqlSuffix'. (NotNull)
     */
    String getClauseFromWhereWithUnionTemplate();

    /**
     * Get from-where clause without select and orderBy and sqlSuffix as template. <br />
     * Basically for subQuery and selectCount. <br />
     * You should handle UnionSelectClauseMark and UnionWhereClauseMark and UnionWhereFirstConditionMark
     * and WhereClauseMark and WhereFirstConditionMark in clause.
     * @return The 'from-where' clause(contains union) without 'select' and 'orderBy' and 'sqlSuffix'. (NotNull)
     */
    String getClauseFromWhereWithWhereUnionTemplate();

    // ===================================================================================
    //                                                                        Clause Parts
    //                                                                        ============
    /**
     * Get the clause of 'select'. This is an internal method.
     * @return The clause of select. {[select ...] from table...} (NotNull)
     */
    String getSelectClause();

    /**
     * Get the map of select index by key name of select column. <br />
     * map:{entityNo(e.g. loc00 or _0_3) = map:{selectColumnKeyName = selectIndex}}
     * @return The map of select index. (NullAllowed: null means lazy-loaded not yet or select index is disabled)
     */
    Map<String, Map<String, Integer>> getSelectIndexMap();

    /**
     * Get the map of key name of select column by on-query name. <br />
     * map:{onQueryAlias = selectColumnKeyName}}
     * @return The map of key name. (NullAllowed: null means lazy-loaded not yet or select index is disabled)
     */
    Map<String, String> getSelectColumnKeyNameMap();

    /**
     * Change limit size of alias name.
     * @param aliasNameLimitSize The limit size of alias name. (NotMinus, NotZero)
     */
    void changeAliasNameLimitSize(int aliasNameLimitSize);

    /**
     * Disable select index.
     */
    void disableSelectIndex();

    /**
     * Get the hint of 'select'. This is an internal method.
     * @return The hint of 'select'. {select [select-hint] * from table...} (NotNull)
     */
    String getSelectHint();

    /**
     * Get the clause of 'from'. This is an internal method.
     * @return The clause of 'from'. (NotNull)
     */
    String getFromClause();

    /**
     * Get the clause of from-base-table. This is an internal method.
     * @return The hint of from-base-table. {select * from table [from-base-table-hint] where ...} (NotNull)
     */
    String getFromBaseTableHint();

    /**
     * Get the hint of 'from'. This is an internal method.
     * @return The hint of 'from'. {select * from table left outer join ... on ... [from-hint] where ...} (NotNull)
     */
    String getFromHint();

    /**
     * Get the clause of 'where'. This is an internal method.
     * @return The clause of 'where'. (NotNull)
     */
    String getWhereClause();

    /**
     * Get the clause of 'order-by'. This is an internal method.
     * @return The clause of 'order-by'. (NotNull)
     */
    String getOrderByClause();

    /**
     * Get the suffix of SQL. This is an internal method.
     * @return The suffix of SQL. {select * from table where ... order by ... [sql-suffix]} (NotNull)
     */
    String getSqlSuffix();

    // ===================================================================================
    //                                                                   Selected Relation
    //                                                                   =================
    /**
     * Register selected relation.
     * @param foreignTableAliasName The alias name of foreign table. (NotNull)
     * @param localTableDbName The table DB name of local. (NotNull)
     * @param foreignPropertyName The property name of foreign table. (NotNull)
     * @param localRelationPath The path of local relation. (NullAllowed)
     * @param foreignRelationPath The path of foreign relation, same as relation No suffix. e.g. _3, _7_2 (NotNull)
     */
    void registerSelectedRelation(String foreignTableAliasName, String localTableDbName, String foreignPropertyName,
            String localRelationPath, String foreignRelationPath);

    /**
     * Get the count of selected relation.
     * @return The integer of count. (NotMinus)
     */
    int getSelectedRelationCount();

    /**
     * Is the selected relation empty?
     * @return The determination, true or false.
     */
    boolean isSelectedRelationEmpty();

    /**
     * Is the relation selected?
     * @param foreignRelationPath The path of foreign relation, same as relation No suffix. e.g. _3, _7_2 (NotNull)
     * @return The determination, true or false.
     */
    boolean hasSelectedRelation(String foreignRelationPath);

    /**
     * Get the map of selected relation column. <br />
     * Basically internal but public for analyzing.
     * @return The map of selected relation column. map:{foreignTableAliasName : map:{columnName : selectedRelationColumn}} (NotNull)
     */
    Map<String, Map<String, SelectedRelationColumn>> getSelectedRelationColumnMap();

    /**
     * Does the relation connect to selected next relation?
     * @param foreignRelationPath The path of foreign relation, same as relation No suffix. e.g. _3, _7_2 (NotNull)
     * @return The determination, true or false.
     */
    boolean isSelectedNextConnectingRelation(String foreignRelationPath);

    // ===================================================================================
    //                                                                           OuterJoin
    //                                                                           =========
    // -----------------------------------------------------
    //                                          Registration
    //                                          ------------
    /**
     * Register outer-join. <br />
     * The fixed-conditions are located on on-clause.
     * @param foreignAliasName The alias name of foreign table. {left outer join [foreignTableDbName] [foreignAliasName]} (NotNull, Unique)
     * @param foreignTableDbName The DB name of foreign table. {left outer join [foreignTableDbName] [foreignAliasName]} (NotNull)
     * @param localAliasName The alias name of local table. {[localTableDbName] [localAliasName] left outer join} (NotNull)
     * @param localTableDbName The DB name of local table. {[localTableDbName] [localAliasName] left outer join} (NotNull)
     * @param joinOnMap The map of join condition on on-clause. (NotNull)
     * @param relationPath The path of relation. e.g. _1_3 (NotNull)
     * @param foreignInfo The information of foreign relation corresponding to this join. (NotNull)
     * @param fixedCondition The fixed condition on on-clause. (NullAllowed: if null, means no fixed condition)
     * @param fixedConditionResolver The resolver for variables on fixed-condition. (NullAllowed) 
     */
    void registerOuterJoin(String foreignAliasName, String foreignTableDbName, String localAliasName,
            String localTableDbName, Map<ColumnRealName, ColumnRealName> joinOnMap, String relationPath,
            ForeignInfo foreignInfo, String fixedCondition, FixedConditionResolver fixedConditionResolver);

    /**
     * Register outer-join using in-line view for fixed-conditions. <br />
     * The fixed-conditions are located on in-line view.
     * @param foreignAliasName The alias name of foreign table. {left outer join [foreignTableDbName] [foreignAliasName]} (NotNull, Unique)
     * @param foreignTableDbName The DB name of foreign table. {left outer join [foreignTableDbName] [foreignAliasName]} (NotNull)
     * @param localAliasName The alias name of local table. {[localTableDbName] [localAliasName] left outer join} (NotNull)
     * @param localTableDbName The DB name of local table. {[localTableDbName] [localAliasName] left outer join} (NotNull)
     * @param joinOnMap The map of join condition on on-clause. (NotNull)
     * @param relationPath The path of relation. e.g. _1_3 (NotNull)
     * @param foreignInfo The information of foreign relation corresponding to this join. (NotNull)
     * @param fixedCondition The fixed condition on in-line view. (NullAllowed: if null, means no fixed condition)
     * @param fixedConditionResolver The resolver for variables on fixed-condition. (NullAllowed) 
     */
    void registerOuterJoinFixedInline(String foreignAliasName, String foreignTableDbName, String localAliasName,
            String localTableDbName, Map<ColumnRealName, ColumnRealName> joinOnMap, String relationPath,
            ForeignInfo foreignInfo, String fixedCondition, FixedConditionResolver fixedConditionResolver);

    /**
     * Register the lazy checker for the fixed condition. <br />
     * This is called when building SQL clause.
     * @param checker The callback instance of checker. (NotNull)
     */
    void registerFixedConditionLazyChecker(FixedConditionLazyChecker checker);

    // -----------------------------------------------------
    //                                   OuterJoin Attribute
    //                                   -------------------
    /**
     * Get the information of left-outer-join. <br />
     * Basically internal but public for analyzing.
     * @return The map of left-outer-join info. map:{ foreignAliasName : leftOuterJoinInfo } (NotNull)
     */
    Map<String, LeftOuterJoinInfo> getOuterJoinMap();

    /**
     * Does outer-join (at least one) exist? (contains inner-join)
     * @return The determination, true or false.
     */
    boolean hasOuterJoin();

    /**
     * Can it use the relation cache for entity mapping?
     * @param relationPath The path of relation. e.g. _1_3 (NotNull)
     * @return The determination, true or false.
     */
    boolean canUseRelationCache(String relationPath);

    /**
     * Is the relation under over-relation?
     * @param relationPath The path of relation. e.g. _1_3 (NotNull)
     * @return The determination, true or false.
     */
    boolean isUnderOverRelation(String relationPath);

    // -----------------------------------------------------
    //                                    InnerJoin Handling
    //                                    ------------------
    /**
     * Change the join type for the relation to inner join manually.
     * @param foreignAliasName The foreign alias name of join table. (NotNull and Unique per invoking method)
     */
    void changeToInnerJoin(String foreignAliasName);

    // -----------------------------------------------------
    //                                  InnerJoin AutoDetect
    //                                  --------------------
    // has several items of inner-join auto-detected
    /**
     * Enable to auto-detect joins that can be (all type) inner-join. <br />
     * You should call this before registrations of where clause.
     * (actually you can call before selecting but it's a fixed specification for user)
     */
    void enableInnerJoinAutoDetect();

    /**
     * Disable auto-detecting inner-join. <br />
     * You should call this before registrations of where clause.
     */
    void disableInnerJoinAutoDetect();

    // -----------------------------------------------------
    //                          StructuralPossible InnerJoin
    //                          ----------------------------
    // one of inner-join auto-detect
    /**
     * Enable to auto-detect joins that can be structure-possible inner-join. <br />
     * You should call this before registrations of where clause.
     * (actually you can call before selecting but it's a fixed specification for user)
     */
    void enableStructuralPossibleInnerJoin();

    /**
     * Disable auto-detecting structural-possible inner-join. <br />
     * You should call this before registrations of where clause.
     */
    void disableStructuralPossibleInnerJoin();

    /**
     * Does it allow to auto-detect structure-possible inner-join? 
     * @return Determination. (true or false)
     */
    boolean isStructuralPossibleInnerJoinEnabled();

    // -----------------------------------------------------
    //                                   WhereUsed InnerJoin
    //                                   -------------------
    // one of inner-join auto-detect
    /**
     * Enable to auto-detect joins that can be where-used inner-join. <br />
     * You should call this before registrations of where clause.
     */
    void enableWhereUsedInnerJoin();

    /**
     * Disable auto-detecting where-used inner-join.
     * You should call this before registrations of where clause.
     */
    void disableWhereUsedInnerJoin();

    /**
     * Does it allow to auto-detect where-used inner-join? 
     * @return Determination. (true or false)
     */
    boolean isWhereUsedInnerJoinEnabled();

    // ===================================================================================
    //                                                                               Where
    //                                                                               =====
    // -----------------------------------------------------
    //                                          Registration
    //                                          ------------
    /**
     * Register 'where' clause.
     * @param columnRealName The real name of column. {[alias-name].[column-name]}. (NotNull)
     * @param key The key of condition. (NotNull)
     * @param value The value of condition. (NotNull)
     * @param cipher The cipher of column by function. (NullAllowed)
     * @param option The option of condition. (NullAllowed)
     * @param usedAliasName The alias name of table used on the where clause. (NotNull)
     */
    void registerWhereClause(ColumnRealName columnRealName, ConditionKey key, ConditionValue value,
            ColumnFunctionCipher cipher, ConditionOption option, String usedAliasName);

    /**
     * Register 'where' clause. <br />
     * The join of the alias, if it's a relation condition, may have a chance to be inner-join.
     * @param clause The string clause of 'where'. (NotNull)
     * @param usedAliasName The alias name of table used on the where clause. (NotNull)
     */
    void registerWhereClause(String clause, String usedAliasName);

    /**
     * Register 'where' clause. <br />
     * You can control the inner-join possibility.
     * @param clause The string clause of 'where'. (NotNull)
     * @param usedAliasName The alias name of table used on the where clause. (NotNull)
     * @param noWayInner No way, to be inner-join for the join of the alias?
     */
    void registerWhereClause(String clause, String usedAliasName, boolean noWayInner);

    /**
     * Register 'where' clause. <br />
     * You can control the inner-join possibility.
     * @param clause The string clause of 'where'. (NotNull)
     * @param usedAliasInfos The array of information of used alias, contains no-way-inner determination. (NotNull, NotEmpty)
     */
    void registerWhereClause(QueryClause clause, QueryUsedAliasInfo... usedAliasInfos);

    // -----------------------------------------------------
    //                                        WhereUsed Join
    //                                        --------------
    /**
     * Reflect the information of where-used-to join. <br />
     * Basically interface for DreamCruise.
     * @param usedAliasInfo The information of used alias, contains no-way-inner determination. (NotNull, NotEmpty)
     */
    void reflectWhereUsedToJoin(QueryUsedAliasInfo usedAliasInfo);

    // -----------------------------------------------------
    //                                       Where Attribute
    //                                       ---------------
    /**
     * Exchange first The clause of 'where' for last one.
     */
    void exchangeFirstWhereClauseForLastOne();

    /**
     * Does it have where clauses on the base query? <br />
     * Clauses on union queries and in-line views are not concerned.
     * @return The determination, true or false.
     */
    boolean hasWhereClauseOnBaseQuery();

    /**
     * Back up where clause on base query. <br />
     * You can restore it later.
     */
    void backupWhereClauseOnBaseQuery();

    /**
     * Restore where clause on base query if backup exists. <br />
     * You should call this after backup.
     */
    void restoreWhereClauseOnBaseQuery();

    /**
     * Clear where clauses on the base query. <br />
     * Clauses on union queries and in-line views are not concerned.
     */
    void clearWhereClauseOnBaseQuery();

    // ===================================================================================
    //                                                                       In-line Where
    //                                                                       =============
    // -----------------------------------------------------
    //                                In-line for Base Table
    //                                ----------------------
    void registerBaseTableInlineWhereClause(ColumnSqlName columnSqlName, ConditionKey key, ConditionValue value,
            ColumnFunctionCipher cipher, ConditionOption option);

    void registerBaseTableInlineWhereClause(String value);

    boolean hasBaseTableInlineWhereClause();

    void clearBaseTableInlineWhereClause();

    // -----------------------------------------------------
    //                                In-line for Outer Join
    //                                ----------------------
    void registerOuterJoinInlineWhereClause(String foreignAliasName, ColumnSqlName columnSqlName, ConditionKey key,
            ConditionValue value, ColumnFunctionCipher cipher, ConditionOption option, boolean onClause);

    void registerOuterJoinInlineWhereClause(String foreignAliasName, String clause, boolean onClause);

    boolean hasOuterJoinInlineWhereClause();

    void clearOuterJoinInlineWhereClause();

    // ===================================================================================
    //                                                                        OrScopeQuery
    //                                                                        ============
    /**
     * Begin or-scope query.
     */
    void beginOrScopeQuery();

    /**
     * End or-scope query.
     */
    void endOrScopeQuery();

    /**
     * Begin or-scope query to and-part.
     */
    void beginOrScopeQueryAndPart();

    /**
     * End or-scope query to and-part.
     */
    void endOrScopeQueryAndPart();

    /**
     * Is or-scope query effective?
     * @return The determination, true or false.
     */
    boolean isOrScopeQueryEffective();

    /**
     * Is and-part of or-scope effective?
     * @return The determination, true or false.
     */
    boolean isOrScopeQueryAndPartEffective();

    // ===================================================================================
    //                                                                             OrderBy
    //                                                                             =======
    /**
     * @return The object of order-by clause. (NotNull)
     */
    OrderByClause getOrderByComponent();

    /**
     * Get the last element of order-by.
     * @return The order-by element object. (NullAllowed: when no order-by)
     */
    OrderByElement getOrderByLastElement();

    /**
     * Clear order-by information in this clause.
     */
    void clearOrderBy();

    /**
     * Suppress order-by temporarily.
     */
    void suppressOrderBy();

    /**
     * Revive order-by from suppressed status. <br />
     * You can call when not suppressed, only reloaded.
     */
    void reviveOrderBy();

    /**
     * @param orderByProperty Order-by-property. 'aliasName.columnSqlName/aliasName.columnSqlName/...' (NotNull)
     * @param ascOrDesc Is it ascend or descend?
     * @param columnInfo The information of the column for the order. (NotNull)
     */
    void registerOrderBy(String orderByProperty, boolean ascOrDesc, ColumnInfo columnInfo);

    /**
     * @param orderByProperty Order-by-property. 'aliasName.columnSqlName/aliasName.columnSqlName/...' (NotNull)
     * @param ascOrDesc Is it ascend or descend?
     */
    void registerSpecifiedDerivedOrderBy(String orderByProperty, boolean ascOrDesc);

    void addNullsFirstToPreviousOrderBy();

    void addNullsLastToPreviousOrderBy();

    void addManualOrderToPreviousOrderByElement(ManualOrderBean manualOrderBean);

    /**
     * Does it have order-by clauses? <br />
     * Whether effective or not has no influence.
     * @return The determination, true or false.
     */
    boolean hasOrderByClause();

    /**
     * Does it have order-by clauses as specified-derived order-by? <br />
     * Whether effective or not has no influence.
     * @return The determination, true or false.
     */
    boolean hasSpecifiedDerivedOrderByClause();

    // ===================================================================================
    //                                                                               Union
    //                                                                               =====
    void registerUnionQuery(UnionClauseProvider unionClauseProvider, boolean unionAll);

    boolean hasUnionQuery();

    void clearUnionQuery();

    // ===================================================================================
    //                                                                          FetchScope
    //                                                                          ==========
    /**
     * Fetch first several rows only.
     * @param fetchSize The size of fetching. (NotMinus)
     */
    void fetchFirst(int fetchSize);

    /**
     * Fetch scope (skip first several rows, and fetch first rows).
     * @param fetchStartIndex The index of fetch-start. 0 origin. (NotMinus)
     * @param fetchSize The size of fetching from start index. (NotMinus)
     */
    void fetchScope(int fetchStartIndex, int fetchSize);

    /**
     * Fetch page.
     * <p>
     * When you invoke this, it is normally necessary to invoke 'fetchFirst()' or 'fetchScope()' ahead of that.
     * But you also can use default-fetch-size without invoking 'fetchFirst()' or 'fetchScope()'.
     * If you invoke this, your SQL returns [fetch-size] records from [fetch-start-index] calculated by [fetch-page-number].
     * </p>
     * @param fetchPageNumber The number of fetch page. 1 origin. (NotMinus & NotZero: if minus or zero, set one)
     */
    void fetchPage(int fetchPageNumber);

    /**
     * Get fetch start index.
     * @return Fetch start index.
     */
    int getFetchStartIndex();

    /**
     * Get fetch size.
     * @return Fetch size.
     */
    int getFetchSize();

    /**
     * Get fetch page number.
     * @return Fetch page number.
     */
    int getFetchPageNumber();

    /**
     * Get page start index.
     * @return Page start index. 0 origin. (NotMinus)
     */
    int getPageStartIndex();

    /**
     * Get page end index.
     * @return Page end index. 0 origin. (NotMinus)
     */
    int getPageEndIndex();

    /**
     * Suppress fetch-scope.
     */
    void suppressFetchScope();

    /**
     * Revive fetch-scope from suppressed status. <br />
     * You can call when not suppressed, only reloaded.
     */
    void reviveFetchScope();

    /**
     * Is fetch scope effective?
     * @return The determination, true or false.
     */
    boolean isFetchScopeEffective();

    /**
     * Is fetch start index supported?
     * @return The determination, true or false.
     */
    boolean isFetchStartIndexSupported();

    /**
     * Is fetch size supported?
     * @return The determination, true or false.
     */
    boolean isFetchSizeSupported();

    // ===================================================================================
    //                                                                     Fetch Narrowing
    //                                                                     ===============
    /**
     * Is fetch-narrowing effective?
     * @return Determiantion.
     */
    boolean isFetchNarrowingEffective();

    /**
     * Get fetch-narrowing skip-start-index.
     * @return Skip-start-index.
     */
    int getFetchNarrowingSkipStartIndex();

    /**
     * Get fetch-narrowing loop-count.
     * @return Loop-count.
     */
    int getFetchNarrowingLoopCount();

    // ===================================================================================
    //                                                                                Lock
    //                                                                                ====
    /**
     * Lock selected records for update.
     * <p>
     * If you invoke this, your SQL lock target records for update.
     * It depends whether this method supports this on the database type.
     * </p>
     */
    void lockForUpdate();

    // ===================================================================================
    //                                                                    Table Alias Info
    //                                                                    ================
    /**
     * Get the alias name for base point table. <br />
     * @return The string name for alias. (NotNull)
     */
    String getBasePointAliasName();

    /**
     * Resolve alias name for join table.
     * @param relationPath The path of relation. e.g. _1_3 (NotNull)
     * @param nestLevel The nest level of condition query.
     * @return The resolved name. (NotNull)
     */
    String resolveJoinAliasName(String relationPath, int nestLevel);

    /**
     * Resolve relation no.
     * @param localTableName The name of local table. (NotNull)
     * @param foreignPropertyName The property name of foreign relation. (NotNull)
     * @return The resolved relation No.
     */
    int resolveRelationNo(String localTableName, String foreignPropertyName);

    /**
     * Get the alias name for base point table on in-line view.
     * @return The string name for alias. (NotNull)
     */
    String getInlineViewBasePointAlias();

    /**
     * Get the alias name for in-line view of union-query.
     * @return The string name for alias. (NotNull)
     */
    String getUnionQueryInlineViewAlias();

    /**
     * Get the alias name for derived column of nested DerivedReferrer.
     * @return The string name for alias. (NotNull)
     */
    String getDerivedReferrerNestedAlias();

    /**
     * Get the alias name for specified column of scalar-select.
     * @return The string name for alias. (NotNull)
     */
    String getScalarSelectColumnAlias();

    // ===================================================================================
    //                                                                       Template Mark
    //                                                                       =============
    String getWhereClauseMark();

    String getWhereFirstConditionMark();

    String getUnionSelectClauseMark();

    String getUnionWhereClauseMark();

    String getUnionWhereFirstConditionMark();

    // ===================================================================================
    //                                                          Where Clause Simple Filter
    //                                                          ==========================
    void addWhereClauseSimpleFilter(QueryClauseFilter whereClauseSimpleFilter);

    // ===================================================================================
    //                                                                    Sub Query Indent
    //                                                                    ================
    String resolveSubQueryBeginMark(String subQueryIdentity);

    String resolveSubQueryEndMark(String subQueryIdentity);

    String processSubQueryIndent(String sql);

    // [DBFlute-0.7.4]
    // ===================================================================================
    //                                                                       Specification
    //                                                                       =============
    // -----------------------------------------------------
    //                                        Specify Column
    //                                        --------------
    /**
     * Specify select columns. <br />
     * It is overridden when the specified column has already been specified.
     * @param specifiedColumn The info about column specification. (NotNull)
     */
    void specifySelectColumn(HpSpecifiedColumn specifiedColumn);

    /**
     * Does it have specified select columns?
     * @param tableAliasName The alias name of table. (NotNull)
     * @return The determination, true or false.
     */
    boolean hasSpecifiedSelectColumn(String tableAliasName);

    /**
     * Does it have the specified select column?
     * @param tableAliasName The alias name of table. (NotNull)
     * @param columnDbName The DB name of column. (NotNull)
     * @return The determination, true or false.
     */
    boolean hasSpecifiedSelectColumn(String tableAliasName, String columnDbName);

    /**
     * Handle the specified select column in the table.
     * @param tableAliasName The alias name of table. (NotNull)
     * @param columnHandler The handler of the specified column. (NotNull)
     */
    void handleSpecifiedSelectColumn(String tableAliasName, SpecifiedSelectColumnHandler columnHandler);

    /**
     * Back up specified select columns. <br />
     * You can restore it later.
     */
    void backupSpecifiedSelectColumn();

    /**
     * Restore specified select columns if backup exists. <br />
     * You should call this after backup.
     */
    void restoreSpecifiedSelectColumn();

    /**
     * Clear specified select columns.
     */
    void clearSpecifiedSelectColumn();

    // -----------------------------------------------------
    //                                      Specified as One
    //                                      ----------------
    /**
     * Get the only one specified column.
     * @return The instance as specified column. (NullAllowed: if not found or duplicated, returns null)
     */
    HpSpecifiedColumn getSpecifiedColumnAsOne();

    /**
     * Get the DB name of only one specified column.
     * @return The instance as string. (NullAllowed: if not found or duplicated, returns null)
     */
    String getSpecifiedColumnDbNameAsOne();

    /**
     * Get the information of only one specified column.
     * @return The instance as type for information of column. (NullAllowed: if not found or duplicated, returns null)
     */
    ColumnInfo getSpecifiedColumnInfoAsOne();

    /**
     * Get the real name of only one specified column.
     * @return The instance as type for real name of column. (NullAllowed: if not found or duplicated, returns null)
     */
    ColumnRealName getSpecifiedColumnRealNameAsOne();

    /**
     * Get the SQL name of only one specified column.
     * @return The instance as type for SQL name of column. (NullAllowed: if not found or duplicated, returns null)
     */
    ColumnSqlName getSpecifiedColumnSqlNameAsOne();

    // -----------------------------------------------------
    //                                      Specify Deriving
    //                                      ----------------
    /**
     * Specify deriving sub-query for DerivedReferrer. <br />
     * It is overridden when the specified column has already been specified. <br />
     * The aliasName is allowed to be null for (Specify)DerivedReferrer to be used in other functions.
     * @param subQueryInfo The info about deriving sub-query. (NotNull: aliasName is allowed to be null)
     */
    void specifyDerivingSubQuery(HpDerivingSubQueryInfo subQueryInfo);

    /**
     * Does it have the specified deriving sub-query at least one?
     * @return the determination, true or false.
     */
    boolean hasSpecifiedDerivingSubQuery();

    /**
     * Does it have the specified deriving sub-query by the alias name?
     * @param aliasName The alias name of specified deriving sub-query. (NotNull)
     * @return the determination, true or false.
     */
    boolean hasSpecifiedDerivingSubQuery(String aliasName);

    /**
     * Get the list of alias for specified deriving sub-query.
     * @return The list of alias. (NotNull: if no deriving, empty list)
     */
    List<String> getSpecifiedDerivingAliasList();

    /**
     * Get the info of specified deriving sub-query by the alias name.
     * @param aliasName The alias name of specified deriving sub-query. (NotNull)
     * @return The info of specified deriving sub-query. (NullAlowed: if not found)
     */
    HpDerivingSubQueryInfo getSpecifiedDerivingInfo(String aliasName);

    /**
     * Get the info of column for specified deriving sub-query by the alias name.
     * @param aliasName The alias name of specified deriving sub-query. (NotNull)
     * @return The info of column. (NullAlowed: if not found)
     */
    ColumnInfo getSpecifiedDerivingColumnInfo(String aliasName);

    /**
     * Clear specified deriving sub-query.
     */
    void clearSpecifiedDerivingSubQuery();

    // -----------------------------------------------------
    //                                       Deriving as One
    //                                       ---------------
    /**
     * Get the specified column for specified deriving sub-query as specified one.
     * @return The instance as specified column. (NullAlowed: if not found or not one)
     */
    HpSpecifiedColumn getSpecifiedDerivingColumnAsOne();

    /**
     * Get the info of column for specified deriving sub-query as specified one.
     * @return The instance as column info. (NullAlowed: if not found or not one)
     */
    ColumnInfo getSpecifiedDerivingColumnInfoAsOne();

    /**
     * Get the alias name for specified deriving sub-query as specified one.
     * @return The string for the alias name. (NullAlowed: if not found or not one)
     */
    String getSpecifiedDerivingAliasNameAsOne();

    /**
     * Get the clause of specified deriving sub-query as specified one.
     * @return The string for the clause. (NullAlowed: if not found or not one)
     */
    String getSpecifiedDerivingSubQueryAsOne();

    // -----------------------------------------------------
    //                                       Resolved as One
    //                                       ---------------
    /**
     * Get the SQL name of definition resolved column (specified or deriving) as specified one.
     * @return The object of SQL name. (NullAllowed: if not found or not one)
     */
    ColumnSqlName getSpecifiedResolvedColumnSqlNameAsOne();

    /**
     * Get the real name of sub-query resolved column (specified or deriving) as specified one. <br />
     * And the SpecifyCalculation is resolved here.
     * @return The object of real name. (NullAllowed: if not found or not one)
     */
    ColumnRealName getSpecifiedResolvedColumnRealNameAsOne();

    // ===================================================================================
    //                                                                  Invalid Query Info
    //                                                                  ==================
    // -----------------------------------------------------
    //                                     NullOrEmpty Query
    //                                     -----------------
    /**
     * Check null-or-empty query. <br />
     * The default is ignored, but public default is checked by DBFluteConfig
     */
    void checkNullOrEmptyQuery();

    /**
     * Ignore null-or-empty query.
     */
    void ignoreNullOrEmptyQuery();

    /**
     * Is null-or-empty query checked?
     * @return The determination, true or false.
     */
    boolean isNullOrEmptyQueryChecked();

    /**
     * Get the list of invalid query. (basically for logging)
     * @return The list of invalid query. (NotNull, ReadOnly)
     */
    List<HpInvalidQueryInfo> getInvalidQueryList();

    /**
     * Save the invalid query.
     * @param invalidQueryInfo The information of invalid query. (NotNull)
     */
    void saveInvalidQuery(HpInvalidQueryInfo invalidQueryInfo);

    // -----------------------------------------------------
    //                                          Empty String
    //                                          ------------
    /**
     * Enable empty string query. (default is disabled)
     */
    void enableEmptyStringQuery();

    /**
     * Disable empty string query. (back to default)
     */
    void disableEmptyStringQuery();

    /**
     * Is empty string checked?
     * @return The determination, true or false.
     */
    boolean isEmptyStringQueryEnabled();

    // -----------------------------------------------------
    //                                      Overriding Query
    //                                      ----------------
    /**
     * Enable overriding query. (default is disabled)
     */
    void enableOverridingQuery();

    /**
     * Disable overriding query. (back to default)
     */
    void disableOverridingQuery();

    /**
     * Is overriding query checked?
     * @return The determination, true or false.
     */
    boolean isOverridingQueryEnabled();

    // [DBFlute-0.8.6]
    // ===================================================================================
    //                                                                  Select Clause Type
    //                                                                  ==================
    /**
     * Classify the type of select clause into specified type.
     * @param selectClauseType The type of select clause. (NotNull)
     */
    void classifySelectClauseType(SelectClauseType selectClauseType);

    /**
     * Roll-back the type of select clause into previous one.
     * If it has no change, classify its type into default type.
     */
    void rollbackSelectClauseType();

    // [DBFlute-0.9.8.6]
    // ===================================================================================
    //                                                                  ColumnQuery Object
    //                                                                  ==================
    /**
     * Get the map for ColumnQuery objects for parameter comment. {Internal}
     * @return The map for ColumnQuery objects. (NullAllowed: if null, means no object)
     */
    Map<String, Object> getColumnQueryObjectMap();

    /**
     * Register ColumnQuery object to theme list. {Internal}
     * @param themeKey The key for the object. (NotNull)
     * @param addedValue The value added to theme list for the object. (NotNull)
     * @return The expression for binding. (NotNull)
     */
    String registerColumnQueryObjectToThemeList(String themeKey, Object addedValue);

    // [DBFlute-0.9.8.6]
    // ===================================================================================
    //                                                               ManualOrder Parameter
    //                                                               =====================
    /**
     * Get the map for ManualOrder parameters for parameter comment. {Internal}
     * @return The map for ManualOrder parameters. (NullAllowed: if null, means no parameter)
     */
    Map<String, Object> getManualOrderParameterMap();

    /**
     * Register ManualOrder parameter to theme list. {Internal}
     * @param themeKey The theme as key for the parameter. (NotNull)
     * @param addedValue The value added to theme list for the parameter. (NullAllowed)
     * @return The expression for binding. (NotNull)
     */
    String registerManualOrderParameterToThemeList(String themeKey, Object addedValue);

    // [DBFlute-0.9.8.2]
    // ===================================================================================
    //                                                                      Free Parameter
    //                                                                      ==============
    /**
     * Get the map for free parameters for parameter comment. {Internal}
     * @return The map for free parameters. (NullAllowed: if null, means no parameter)
     */
    Map<String, Object> getFreeParameterMap();

    /**
     * Register free parameter to theme list. {Internal}
     * @param themeKey The theme as key for the parameter. (NotNull)
     * @param addedValue The value added to theme list for the parameter. (NullAllowed)
     * @return The expression for binding. (NotNull)
     */
    String registerFreeParameterToThemeList(String themeKey, Object addedValue);

    // [DBFlute-0.9.8.4]
    // ===================================================================================
    //                                                                       Geared Cipher
    //                                                                       =============
    /**
     * Get the manager of geared cipher.
     * @return The instance of manager. (NullAllowed: when no geared cipher)
     */
    GearedCipherManager getGearedCipherManager();

    /**
     * Find the cipher for the column.
     * @param columnInfo The column info for cipher. (NotNull)
     * @return The cipher for the column. (NullAllowed: when no geared cipher or the column is not cipher target)
     */
    ColumnFunctionCipher findColumnFunctionCipher(ColumnInfo columnInfo);

    /**
     * Enable select column cipher effective. <br />
     * The default is enabled (if cipher manager is set) so this method is called after disabling.
     */
    void enableSelectColumnCipher();

    /**
     * Disable select column cipher effective. <br />
     * basically for queryInsert().
     */
    void disableSelectColumnCipher();

    // [DBFlute-0.9.8.4]
    // ===================================================================================
    //                                                                 ScalarSelect Option
    //                                                                 ===================
    /**
     * Accept the option of scalar-select.
     * @param option The instance of option object. (NullAllowed: if null, also clear existing option)
     */
    void acceptScalarSelectOption(ScalarSelectOption option);

    // [DBFlute-0.9.8.8]
    // ===================================================================================
    //                                                                       Paging Select
    //                                                                       =============
    // -----------------------------------------------------
    //                                     Paging Adjustment
    //                                     -----------------
    /**
     * Enable paging adjustment, e.g. PagingCountLater, PagingCountLeastJoin, effective. <br />
     * The options might be on by default so the adjustments are off normally.
     */
    void enablePagingAdjustment();

    /**
     * Disable paging adjustment.
     */
    void disablePagingAdjustment();

    // -----------------------------------------------------
    //                                           Count Later
    //                                           -----------
    /**
     * Enable paging count-later that means counting after selecting. <br />
     * And you should also make paging adjustment effective to enable this. <br />
     * This option is copy of condition-bean's one for clause adjustment, e.g. MySQL found_rows(). <br />
     * The default is disabled, but public default is enabled by DBFluteConfig.
     */
    void enablePagingCountLater();

    /**
     * Disable paging count-later that means counting after selecting. <br />
     * You should call this before execution of selectPage().
     */
    void disablePagingCountLater();

    // -----------------------------------------------------
    //                                       Count LeastJoin
    //                                       ---------------
    /**
     * Enable paging count-least-join, which means least joined on count select. <br />
     * And you should also make paging adjustment effective to enable this. <br />
     * The default is disabled, but public default is enabled by DBFluteConfig.
     */
    void enablePagingCountLeastJoin();

    /**
     * Disable paging count-least-join, which means least joined on count select. <br />
     * You should call this before execution of selectPage().
     */
    void disablePagingCountLeastJoin();

    /**
     * Can it be paging count least join?
     * @return The determination, true or false.
     */
    boolean canPagingCountLeastJoin();

    // [DBFlute-1.0.5G]
    // -----------------------------------------------------
    //                                        PK Only Select
    //                                        --------------
    /**
     * Enable PK only select forcedly effective, ignoring select clause setting and derived referrer. <br />
     * Basically for PagingSelectAndQuerySplit.
     */
    void enablePKOnlySelectForcedly();

    /**
     * Disable PK only select forcedly. <br />
     * Basically for PagingSelectAndQuerySplit.
     */
    void disablePKOnlySelectForcedly();

    // [DBFlute-0.9.9.4C]
    // ===================================================================================
    //                                                                      Lazy Reflector
    //                                                                      ==============
    /**
     * Register the lazy reflector of clause.
     * @param clauseLazyReflector The instance of reflector. (NotNull)
     */
    void registerClauseLazyReflector(ClauseLazyReflector clauseLazyReflector);

    // [DBFlute-0.7.5]
    // ===================================================================================
    //                                                                        Query Update
    //                                                                        ============
    /**
     * @param fixedValueQueryExpMap The map of query expression for fixed values. (NotNull)
     * @param resourceSqlClause The SQL clause for resource. (NotNull)
     * @return The clause of query-insert. (NotNull)
     */
    String getClauseQueryInsert(Map<String, String> fixedValueQueryExpMap, SqlClause resourceSqlClause);

    /**
     * @param columnParameterMap The map of column parameters. The parameter may be handler. (NotNull)
     * @return The clause of query-update. (NullAllowed: If columnParameterMap is empty, return null)
     */
    String getClauseQueryUpdate(Map<String, Object> columnParameterMap);

    /**
     * The handler of calculation on set clause of query-update. <br />
     * This is set on column parameter map as value.
     */
    interface QueryUpdateSetCalculationHandler {

        /**
         * @param aliasName The alias name of calculation column. (NullAllowed)
         * @return The statement string. (NotNull)
         */
        String buildStatement(String aliasName);
    }

    /**
     * @return The clause of query-delete. (NotNull)
     */
    String getClauseQueryDelete();

    /**
     * Enable to use direct clause in query update forcedly (contains query delete).
     * You cannot use join, sub-query, union and so on, by calling this. <br />
     * So you may have the painful SQLException by this, attention!
     */
    void enableQueryUpdateForcedDirect();

    // [DBFlute-0.9.7.2]
    // ===================================================================================
    //                                                                        Purpose Type
    //                                                                        ============
    /**
     * Get the purpose of the condition-bean. e.g. NORMAL_USE, EXISTS_REFERRER
     * @return The instance of purpose object for condition-bean. (NotNull)
     */
    HpCBPurpose getPurpose();

    /**
     * Set the purpose of the condition-bean. e.g. NORMAL_USE, EXISTS_REFERRER
     * @param purpose The instance of purpose object for condition-bean. (NotNull)
     */
    void setPurpose(HpCBPurpose purpose);

    /**
     * Is the clause object locked? e.g. true if in sub-query process and check allowed <br />
     * Java8 cannot use the same name as lambda argument with already existing name in the scope.
     * <pre>
     * cb.query().existsPurchaseList(subCB -&gt; {
     *     subCB.query().existsPurchaseDetailList(<span style="color: #DD4747">subCB</span> -&gt; { <span style="color: #3F7E5E">// *NG</span>
     *     });
     * });
     * </pre>
     * <p>You should rename it, however the other condition-bean might be called.
     * So it is necessary to check it, and condition-bean has lock.</p>
     *
     * <p>While, you can suppress it by option for compatible. (if suppressed, always returns false)</p>
     *
     * @return The determination, true or false.
     */
    boolean isLocked();

    /**
     * Lock the clause object. <br />
     * Only saving lock status here, you should rightly check by this status.
     */
    void lock();

    /**
     * Unlock the clause object.
     */
    void unlock();

    /**
     * Enable "that's-bad-timing" detect.
     */
    void enableThatsBadTimingDetect();

    /**
     * Disable "that's-bad-timing" detect for compatible. <br />
     * If disabled, isLocked() always returns false.
     */
    void disableThatsBadTimingDetect();

    /**
     * Does it allow "that's-bad-timing" detect?
     * @return The determination, true or false.
     */
    boolean isThatsBadTimingDetectAllowed();

    // [DBFlute-0.9.4]
    // ===================================================================================
    //                                                                       InScope Limit
    //                                                                       =============
    /**
     * Get the limit of inScope.
     * @return The limit of inScope. (If it's zero or minus, it means no limit)
     */
    int getInScopeLimit();

    // [DBFlute-0.9.8.4]
    // ===================================================================================
    //                                                               LikeSearch Adjustment
    //                                                               =====================
    /**
     * Adjust like-search of DB way.
     * @param option The option of like-search to adjust. (NotNull)
     */
    void adjustLikeSearchDBWay(LikeSearchOption option);

    // [DBFlute-1.0.3.1]
    // ===================================================================================
    //                                                                 CursorSelect Option
    //                                                                 ===================
    /**
     * Is cursor select by paging allowed?
     * @return The determination, true or false.
     */
    boolean isCursorSelectByPagingAllowed();

    // [DBFlute-0.9.8.4]
    // ===================================================================================
    //                                                                               DBWay
    //                                                                               =====
    /**
     * Get the DB way for this SQL clause.
     * @return The instance of DB way. (NotNull)
     */
    DBWay dbway();
}
