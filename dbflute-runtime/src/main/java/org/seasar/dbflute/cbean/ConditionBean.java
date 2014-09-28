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
package org.seasar.dbflute.cbean;

import java.util.Map;

import org.seasar.dbflute.cbean.chelper.HpCBPurpose;
import org.seasar.dbflute.cbean.chelper.HpColumnSpHandler;
import org.seasar.dbflute.cbean.chelper.HpSpecifiedColumn;
import org.seasar.dbflute.cbean.coption.CursorSelectOption;
import org.seasar.dbflute.cbean.coption.ScalarSelectOption;
import org.seasar.dbflute.cbean.sqlclause.SqlClause;
import org.seasar.dbflute.dbmeta.DBMeta;
import org.seasar.dbflute.dbmeta.DerivedTypeHandler;
import org.seasar.dbflute.exception.ConditionInvokingFailureException;
import org.seasar.dbflute.jdbc.StatementConfig;

/**
 * The bean for condition.
 * @author jflute
 */
public interface ConditionBean extends PagingBean {

    // ===================================================================================
    //                                                                          Table Name
    //                                                                          ==========
    /**
     * Get table DB-name.
     * @return The DB-name of the table. (NotNull)
     */
    String getTableDbName();

    // ===================================================================================
    //                                                                              DBMeta
    //                                                                              ======
    /**
     * Get the instance of DBMeta.
     * @return The instance of DBMeta. (NotNull)
     */
    DBMeta getDBMeta();

    // ===================================================================================
    //                                                                           SqlClause
    //                                                                           =========
    /**
     * Get SQL clause instance. {Internal}<br />
     * @return The object for SQL clause. (NotNull)
     */
    SqlClause getSqlClause();

    // ===================================================================================
    //                                                                 PrimaryKey Handling
    //                                                                 ===================
    /**
     * Accept the map of primary-keys. map:{[column-name] = [value]}
     * @param primaryKeyMap The map of primary-keys. (NotNull and NotEmpty)
     */
    void acceptPrimaryKeyMap(Map<String, ? extends Object> primaryKeyMap);

    /**
     * Add order-by PrimaryKey asc. {order by primaryKey1 asc, primaryKey2 asc...}
     * @return this. (NotNull)
     */
    ConditionBean addOrderBy_PK_Asc();

    /**
     * Add order-by PrimaryKey desc. {order by primaryKey1 desc, primaryKey2 desc...}
     * @return this. (NotNull)
     */
    ConditionBean addOrderBy_PK_Desc();

    // ===================================================================================
    //                                                                             Specify
    //                                                                             =======
    /**
     * Get the handler of the column specification as interface.
     * @return The instance of column specification. (NotNull: instance is created if null)
     */
    HpColumnSpHandler localSp();

    /**
     * Does it have specified columns at least one? (without new-creation of specification instance)
     * @return The determination, true or false.
     */
    boolean hasSpecifiedColumn();

    // ===================================================================================
    //                                                                               Query
    //                                                                               =====
    /**
     * Get the conditionQuery of the local table as interface.
     * @return The instance of conditionQuery. (NotNull: instance is created if null)
     */
    ConditionQuery localCQ();

    /**
     * Enable to auto-detect joins that can be inner-join. (back to default) <br />
     * <pre>
     * o You should call this before registrations of where clause.
     * o Union and SubQuery and other sub condition-bean inherit this.
     * o You should confirm your SQL on the log to be tuned by inner-join correctly.
     * </pre>
     */
    void enableInnerJoinAutoDetect();

    /**
     * Disable auto-detecting inner-join. (default is enabled) <br />
     * You should call this before registrations of where clause.
     */
    void disableInnerJoinAutoDetect();

    // ===================================================================================
    //                                                                        Dream Cruise
    //                                                                        ============
    /**
     * DBFlute Dreams.
     * <pre>
     * e.g. member that purchases products only purchased by the member
     *  MemberCB cb = new MemberCB();
     *  cb.specify().columnBirthdate();
     *  final MemberCB dreamCruiseCB = cb.<span style="color: #DD4747">dreamCruiseCB()</span>;
     *  cb.query().existsPurchaseList(new SubQuery&lt;PurchaseCB&gt;() {
     *      public void query(PurchaseCB subCB) {
     *          subCB.query().queryProduct().notExistsPurchaseList(new SubQuery&lt;PurchaseCB&gt;() {
     *              public void query(PurchaseCB subCB) {
     *                  subCB.columnQuery(new SpecifyQuery&lt;PurchaseCB&gt;() {
     *                      public void specify(PurchaseCB cb) {
     *                          cb.specify().columnMemberId();
     *                      }
     *                  }).notEqual(new SpecifyQuery&lt;PurchaseCB&gt;() {
     *                      public void specify(PurchaseCB cb) {
     *                          cb.<span style="color: #DD4747">overTheWaves</span>(dreamCruiseCB.specify().columnMemberId());
     *                      }
     *                  });
     *              }
     *          });
     *      }
     *  });
     * </pre>
     * @param dreamCruiseTicket The ticket column specified by your Dream Cruise. (NotNull)
     */
    void overTheWaves(HpSpecifiedColumn dreamCruiseTicket);

    /**
     * Invite the derived column to dream cruise. (returns the ticket)
     * @param derivedAlias The alias name for derived column. (NotNull)
     * @return The ticket column specified by your Dream Cruise. (NotNull)
     */
    HpSpecifiedColumn inviteDerivedToDreamCruise(String derivedAlias);

    /**
     * Create condition-bean for dream cruise.
     * @return The created condition-bean for Dream Cruise. (NotNull)
     */
    ConditionBean xcreateDreamCruiseCB();

    /**
     * Mark as departure port for dream cruise.
     */
    void xmarkAsDeparturePortForDreamCruise();

    /**
     * Is this condition-bean departure port for dream cruise?
     * @return The determination, true or false.
     */
    boolean xisDreamCruiseDeparturePort();

    /**
     * Is this condition-bean for dream cruise?
     * @return The determination, true or false.
     */
    boolean xisDreamCruiseShip();

    /**
     * Get the departure port of dream cruise? <br />
     * (condition-bean creating the condition-bean)
     * @return The base condition-bean for Dream Cruise. (NullAllowed: when not dream cruise)
     */
    ConditionBean xgetDreamCruiseDeparturePort();

    /**
     * Do you have a Dream Cruise ticket? <br />
     * (whether this CB has the specified column by dream cruise or not)
     * @return The determination, true or false.
     */
    boolean xhasDreamCruiseTicket();

    /**
     * Show me your Dream Cruise ticket. <br />
     * (get the specified column by Dream Cruise)
     * @return The information of specified column. (NullAllowed)
     */
    HpSpecifiedColumn xshowDreamCruiseTicket();

    /**
     * Keep journey log-book of Dream Cruise. <br /> 
     * (save the relation trace by Dream Cruise)
     * @param relationPath The path of relation. (NotNull)
     */
    void xkeepDreamCruiseJourneyLogBook(String relationPath);

    /**
     * Set up select for journey log-book of Dream Cruise.
     */
    void xsetupSelectDreamCruiseJourneyLogBook();

    /**
     * Set up select for journey log-book of Dream Cruise if union query exists.
     */
    void xsetupSelectDreamCruiseJourneyLogBookIfUnionExists();

    /**
     * DBFlute Dreams.
     * <pre>
     * e.g. ColumnQuery: ... > '2015/04/05' + (PURCHASE_COUNT days)
     *  PurchaseCB cb = new PurchaseCB();
     *  cb.columnQuery(new SpecifyQuery() {
     *      public void specify(Purchase cb) {
     *          cb.column...();
     *      }
     *  }).greaterThan(new SpecifyQuery() {
     *      public void specify(Purchase cb) {
     *          cb.<span style="color: #DD4747">mysticRhythms</span>(toDate("2015/04/05"));
     *      }
     *  }).convert(new ColumnConversionOption()
     *          .<span style="color: #DD4747">addDay</span>(cb.<span style="color: #DD4747">dreamCruiseCB()</span>.specify().columnPurchaseCount());
     * </pre>
     * @param mysticBinding
     */
    void mysticRhythms(Object mysticBinding);

    /**
     * Get the value of mystic binding.
     * @return The object value for mystic binding. (NullAllowed: if null, no mystic)  
     */
    Object xgetMysticBinding();

    // ===================================================================================
    //                                                                       Invalid Query
    //                                                                       =============
    /**
     * Check null or empty value for query when query is set. (back to default) <br />
     * (it throws exception if set query is invalid, e.g. null, empty string, empty list) <br />
     * You should call this before registrations of where clause and other queries. <br />
     * Union and SubQuery and other sub condition-bean inherit this.
     */
    void checkNullOrEmptyQuery();

    /**
     * Ignore null-or-empty check for query when query is set. (default is checked) <br />
     * (no condition if set query is invalid)
     * <pre>
     * MemberCB cb = new MemberCB();
     * cb.ignoreNullOrEmptyQuery();
     * cb.query().setMemberName_PrefixSearch(null); // no condition
     * cb.query().setMemberName_PrefixSearch(""); // no condition
     * </pre>
     * You should call this before registrations of where clause and other queries. <br />
     * And Union and SubQuery and other sub condition-bean inherit this.
     */
    void ignoreNullOrEmptyQuery();

    /**
     * Enable empty string for query. (default is disabled) <br />
     * (you can use an empty string as condition) <br />
     * You should call this before registrations of where clause and other queries. <br />
     * Union and SubQuery and other sub condition-bean inherit this.
     */
    void enableEmptyStringQuery();

    /**
     * Disable empty string for query. (back to default) <br />
     * The empty string for query is treated as invalid data, like null.
     */
    void disableEmptyStringQuery();

    /**
     * Enable overriding query. (default is disabled) <br />
     * (you can override existing value as condition) <br />
     * You should call this before registrations of where clause and other queries. <br />
     * Union and SubQuery and other sub condition-bean inherit this.
     */
    void enableOverridingQuery();

    /**
     * Disable overriding query. (back to default) <br />
     * You should set query to same column and same condition once.
     */
    void disableOverridingQuery();

    // ===================================================================================
    //                                                                      Paging Setting
    //                                                                      ==============
    /**
     * Enable paging count-least-join, which means least joined on count select. (back to default) <br />
     * You can use it by default on DBFlute so you don't need to call this basically.
     * If you've suppressed it by settings of DBFlute property, you can use it by calling. <br />
     * You should call this before execution of selectPage().
     */
    void enablePagingCountLeastJoin();

    /**
     * Disable paging count-least-join, which means least joined on count select. (default is enabled) <br />
     * You should call this before execution of selectPage().
     */
    void disablePagingCountLeastJoin();

    /**
     * Can the SQL execution be split by select and query?
     * @return The determination, true or false.
     */
    boolean canPagingSelectAndQuerySplit();

    // ===================================================================================
    //                                                                        Lock Setting
    //                                                                        ============
    /**
     * Lock for update. <br />
     * If you call this, your SQL lock target records for update. <br />
     * It depends whether this method supports this on the database type.
     * @return this. (NotNull)
     */
    ConditionBean lockForUpdate();

    // ===================================================================================
    //                                                                        Select Count
    //                                                                        ============
    /**
     * Set up various things for select-count-ignore-fetch-scope. {Internal}
     * This method is for INTERNAL. Don't call this!
     * @param uniqueCount Is it unique-count select?
     * @return this. (NotNull)
     */
    ConditionBean xsetupSelectCountIgnoreFetchScope(boolean uniqueCount);

    /**
     * Do after-care for select-count-ignore-fetch-scope. {Internal}
     * This method is for INTERNAL. Don't call this!
     * @return this. (NotNull)
     */
    ConditionBean xafterCareSelectCountIgnoreFetchScope();

    /**
     * Is set up various things for select-count-ignore-fetch-scope? {Internal}
     * This method is for INTERNAL. Don't call this!
     * @return The determination, true or false.
     */
    boolean isSelectCountIgnoreFetchScope();

    // ===================================================================================
    //                                                                       Cursor Select
    //                                                                       =============
    /**
     * Get the option of cursor select.
     * @return The option of cursor select. (NullAllowed: when no option)
     */
    CursorSelectOption getCursorSelectOption();

    // the customizeCursorSelect() method is generated at sub-class
    // because the method is generated only when allowed DBMS

    // ===================================================================================
    //                                                                       Scalar Select
    //                                                                       =============
    /**
     * Accept the option for ScalarSelect.
     * @param option The option for ScalarSelect. (NullAllowed)
     */
    void xacceptScalarSelectOption(ScalarSelectOption option);

    // ===================================================================================
    //                                                                        Query Update
    //                                                                        ============
    /**
     * Enable checking record count before QueryUpdate (contains QueryDelete). (default is disabled) <br />
     * No query update if zero count. (basically for MySQL's deadlock by next-key lock)
     */
    void enableQueryUpdateCountPreCheck();

    /**
     * Disable checking record count before QueryUpdate (contains QueryDelete). (back to default) <br />
     * Executes query update even if zero count. (normal specification)
     */
    void disableQueryUpdateCountPreCheck();

    /**
     * Does it check record count before QueryUpdate (contains QueryDelete)?
     * @return The determination, true or false.
     */
    boolean isQueryUpdateCountPreCheck();

    // ===================================================================================
    //                                                                     StatementConfig
    //                                                                     ===============
    /**
     * Configure statement JDBC options. (For example, queryTimeout, fetchSize, ...)
     * @param statementConfig The configuration of statement. (NullAllowed)
     */
    void configure(StatementConfig statementConfig);

    /**
     * Get the configuration of statement that is set through configure().
     * @return The configuration of statement. (NullAllowed)
     */
    StatementConfig getStatementConfig();

    // ===================================================================================
    //                                                                      Entity Mapping
    //                                                                      ==============
    // no need to use it as interface method so comment out
    ///**
    // * Disable (entity instance) cache of relation mapping. <br />
    // * Basically you don't need this. This is for accidents.
    // */
    //void disableRelationMappingCache();

    /**
     * Can the relation mapping (entity instance) cache?
     * @return The determination, true or false.
     */
    boolean canRelationMappingCache();

    // ===================================================================================
    //                                                                         Display SQL
    //                                                                         ===========
    /**
     * Convert this conditionBean to SQL for display.
     * @return SQL for display. (NotNull and NotEmpty)
     */
    String toDisplaySql();

    // ===================================================================================
    //                                                                       Meta Handling
    //                                                                       =============
    /**
     * Does it have where clause on the base query? <br />
     * Clauses on union queries and in-line views are not concerned.
     * @return The determination, true or false. 
     */
    boolean hasWhereClauseOnBaseQuery();

    /**
     * Clear where clauses where clause on the base query. <br />
     * Clauses on union queries and in-line views are not concerned.
     */
    void clearWhereClauseOnBaseQuery();

    /**
     * Does it have select-all possible? <br />
     * The elements for possible are:
     * <pre>
     * o no where clause on base query
     * o no where clause in base table in-line view
     * o union queries with select-all possible
     * </pre>
     * @return The determination, true or false.
     */
    boolean hasSelectAllPossible();

    /**
     * Does it have order-by clauses? <br />
     * Whether that order-by is effective or not has no influence.
     * @return The determination, true or false.
     */
    boolean hasOrderByClause();

    // clearOrderBy() is defined at OrderByBean

    /**
     * Has union query or union all query?
     * @return The determination, true or false.
     */
    boolean hasUnionQueryOrUnionAllQuery();

    // ===================================================================================
    //                                                                 Reflection Invoking
    //                                                                 ===================
    /**
     * Invoke the method 'setupSelect_Xxx()' and 'withXxx()' by the path of foreign property name. <br />
     * For example, if this is based on PURCHASE, 'member.memberStatus' means as follows:
     * <pre>
     * PurchaseCB cb = new PurchaseCB();
     * cb.setupSelect_Member().withMemberStatus();
     * </pre>
     * A method with parameters (using fixed condition) is unsupported.
     * @param foreignPropertyNamePath The path string. (NotNull, NotTrimmedEmpty)
     * @throws ConditionInvokingFailureException When the method to the property is not found and the method is failed.
     */
    void invokeSetupSelect(String foreignPropertyNamePath);

    /**
     * Invoke the method 'specify().columnXxx()' by the path of column name. <br />
     * For example, if this is based on PURCHASE, 'purchaseDatetime' means as follows:
     * <pre>
     * PurchaseCB cb = new PurchaseCB();
     * cb.specify().columnPurchaseDatetime();
     * </pre>
     * And if this is based on PURCHASE, 'member.birthdate' means as follows:
     * <pre>
     * PurchaseCB cb = new PurchaseCB();
     * cb.specify().specifyMember().columnBirthdate();
     * </pre>
     * @param columnNamePath The path string. (NotNull, NotTrimmedEmpty)
     * @return The info of specified column. (NotNull)
     * @throws ConditionInvokingFailureException When the method to the property is not found and the method is failed.
     */
    HpSpecifiedColumn invokeSpecifyColumn(String columnNamePath);

    /**
     * Invoke the method 'orScopeQuery()' by the query callback. <br />
     * @param orQuery The callback for or-query. (NotNull)
     * @throws ConditionInvokingFailureException When the method to the property is not found and the method is failed.
     */
    void invokeOrScopeQuery(OrQuery<ConditionBean> orQuery);

    /**
     * Invoke the method 'orScopeQueryAndPart()' by the query callback. <br />
     * @param andQuery The callback for and-query. (NotNull)
     * @throws ConditionInvokingFailureException When the method to the property is not found and the method is failed.
     */
    void invokeOrScopeQueryAndPart(AndQuery<ConditionBean> andQuery);

    // ===================================================================================
    //                                                                  Query Synchronizer
    //                                                                  ==================
    /**
     * Register union-query synchronizer. {Internal} <br />
     * Basically for reflecting LoadReferrer's InScope condition to union-queries in condition-bean set-upper.
     * @param unionQuerySynchronizer The synchronizer of union query. (NullAllowed)
     */
    void xregisterUnionQuerySynchronizer(UnionQuery<ConditionBean> unionQuerySynchronizer);

    // ===================================================================================
    //                                                                    Derived Mappable
    //                                                                    ================
    /**
     * Get the handler of derived type for derived mappable entity (for (Specify)DerivedReferrer). <br />
     * Called by internal mapping process, so should be fixed instance.
     * @return The handler of derived type. (NotNull)
     */
    DerivedTypeHandler xgetDerivedTypeHandler();

    // ===================================================================================
    //                                                                        Purpose Type
    //                                                                        ============
    /**
     * Get the purpose of the condition-bean. e.g. NORMAL_USE, EXISTS_REFERRER
     * @return The instance of purpose object for condition-bean. (NotNull)
     */
    HpCBPurpose getPurpose();

    // -----------------------------------------------------
    //                                        Internal Setup
    //                                        --------------
    // only methods called by interface are defined
    /**
     * Set up condition-bean for ScalarSelect.
     */
    void xsetupForScalarSelect();

    /**
     * Set up condition-bean for QueryInsert.
     */
    void xsetupForQueryInsert();

    /**
     * Set up condition-bean for SpecifiedUpdate.
     */
    void xsetupForSpecifiedUpdate();

    /**
     * Set up condition-bean for VaryingUpdate.
     */
    void xsetupForVaryingUpdate();

    // -----------------------------------------------------
    //                                                  Lock
    //                                                  ----
    /**
     * Enable "that's bad timing" check. (back to default)
     */
    void enableThatsBadTiming();

    /**
     * Disable "that's bad timing" check. (default is enabled)
     */
    void disableThatsBadTiming();
}
