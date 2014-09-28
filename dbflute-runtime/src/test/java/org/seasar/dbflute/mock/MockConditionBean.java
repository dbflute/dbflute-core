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
package org.seasar.dbflute.mock;

import java.util.Map;

import org.seasar.dbflute.cbean.AndQuery;
import org.seasar.dbflute.cbean.ConditionBean;
import org.seasar.dbflute.cbean.ConditionQuery;
import org.seasar.dbflute.cbean.OrQuery;
import org.seasar.dbflute.cbean.OrderByBean;
import org.seasar.dbflute.cbean.PagingBean;
import org.seasar.dbflute.cbean.PagingInvoker;
import org.seasar.dbflute.cbean.UnionQuery;
import org.seasar.dbflute.cbean.chelper.HpCBPurpose;
import org.seasar.dbflute.cbean.chelper.HpColumnSpHandler;
import org.seasar.dbflute.cbean.chelper.HpSpecifiedColumn;
import org.seasar.dbflute.cbean.coption.CursorSelectOption;
import org.seasar.dbflute.cbean.coption.ScalarSelectOption;
import org.seasar.dbflute.cbean.sqlclause.SqlClause;
import org.seasar.dbflute.cbean.sqlclause.orderby.OrderByClause;
import org.seasar.dbflute.dbmeta.DBMeta;
import org.seasar.dbflute.dbmeta.DerivedTypeHandler;
import org.seasar.dbflute.jdbc.StatementConfig;

/**
 * @author jflute
 */
public class MockConditionBean implements ConditionBean {

    public DBMeta getDBMeta() {
        return null;
    }

    public void acceptPrimaryKeyMap(Map<String, ? extends Object> primaryKeyMap) {
    }

    public ConditionBean addOrderBy_PK_Asc() {
        return null;
    }

    public ConditionBean addOrderBy_PK_Desc() {
        return null;
    }

    public void configure(StatementConfig statementConfig) {
    }

    public SqlClause getSqlClause() {
        return null;
    }

    public StatementConfig getStatementConfig() {
        return null;
    }

    public String getTableDbName() {
        return null;
    }

    public String getTableSqlName() {
        return null;
    }

    public boolean hasUnionQueryOrUnionAllQuery() {
        return false;
    }

    public boolean isSelectCountIgnoreFetchScope() {
        return false;
    }

    public HpColumnSpHandler localSp() {
        return null;
    }

    public boolean hasSpecifiedColumn() {
        return false;
    }

    public ConditionQuery localCQ() {
        return null;
    }

    public ConditionBean lockForUpdate() {
        return null;
    }

    public String toDisplaySql() {
        return null;
    }

    public ConditionBean xafterCareSelectCountIgnoreFetchScope() {
        return null;
    }

    public ConditionBean xsetupSelectCountIgnoreFetchScope(boolean uniqueCount) {
        return null;
    }

    public void xacceptScalarSelectOption(ScalarSelectOption option) {
    }

    public boolean canPagingCountLater() {
        return false;
    }

    public void enablePagingCountLater() {
    }

    public void disablePagingCountLater() {
    }

    public boolean canPagingReSelect() {
        return false;
    }

    public void disablePagingReSelect() {
    }

    public void enablePagingReSelect() {
    }

    public boolean canPagingSelectAndQuerySplit() {
        return false;
    }

    public PagingBean fetchFirst(int fetchSize) {
        return null;
    }

    public PagingBean fetchPage(int fetchPageNumber) {
        return null;
    }

    public PagingBean fetchScope(int fetchStartIndex, int fetchSize) {
        return null;
    }

    public <ENTITY> PagingInvoker<ENTITY> createPagingInvoker(String tableDbName) {
        return null;
    }

    public int getFetchPageNumber() {
        return 0;
    }

    public int getFetchSize() {
        return 0;
    }

    public int getFetchStartIndex() {
        return 0;
    }

    public int getPageEndIndex() {
        return 0;
    }

    public int getPageStartIndex() {
        return 0;
    }

    public boolean isFetchScopeEffective() {
        return false;
    }

    public boolean isPaging() {
        return false;
    }

    public void paging(int pageSize, int pageNumber) {
    }

    public void xsetPaging(boolean paging) {
    }

    public int getFetchNarrowingLoopCount() {
        return 0;
    }

    public int getFetchNarrowingSkipStartIndex() {
        return 0;
    }

    public int getSafetyMaxResultSize() {
        return 0;
    }

    public void xdisableFetchNarrowing() {
    }

    public boolean isFetchNarrowingEffective() {
        return false;
    }

    public boolean isFetchNarrowingLoopCountEffective() {
        return false;
    }

    public boolean isFetchNarrowingSkipStartIndexEffective() {
        return false;
    }

    public void xenableIgnoredFetchNarrowing() {
    }

    public OrderByBean clearOrderBy() {
        return null;
    }

    public String getOrderByClause() {
        return null;
    }

    public OrderByClause getOrderByComponent() {
        return null;
    }

    public OrderByBean disableOrderBy() {
        return null;
    }

    public OrderByBean enableOrderBy() {
        return null;
    }

    public void checkSafetyResult(int safetyMaxResultSize) {
    }

    public boolean hasOrderByClause() {
        return false;
    }

    public void invokeSetupSelect(String foreignPropertyNamePath) {
    }

    public HpSpecifiedColumn invokeSpecifyColumn(String columnNamePath) {
        return null;
    }

    public void invokeOrScopeQuery(OrQuery<ConditionBean> orQuery) {
    }

    public void invokeOrScopeQueryAndPart(AndQuery<ConditionBean> andQuery) {
    }

    public void xregisterUnionQuerySynchronizer(UnionQuery<ConditionBean> unionQuerySynchronizer) {
    }

    public void checkNullOrEmptyQuery() {
    }

    public void ignoreNullOrEmptyQuery() {
    }

    public void enableEmptyStringQuery() {
    }

    public void disableEmptyStringQuery() {
    }

    public void enableOverridingQuery() {
    }

    public void disableOverridingQuery() {
    }

    public void enableInnerJoinAutoDetect() {
    }

    public void disableInnerJoinAutoDetect() {
    }

    public void enablePagingCountLeastJoin() {
    }

    public void disablePagingCountLeastJoin() {
    }

    public boolean canRelationMappingCache() {
        return false;
    }

    public boolean hasWhereClauseOnBaseQuery() {
        return false;
    }

    public void clearWhereClauseOnBaseQuery() {
    }

    public boolean hasSelectAllPossible() {
        return false;
    }

    public boolean xhasDreamCruiseTicket() {
        return false;
    }

    public void overTheWaves(HpSpecifiedColumn dreamCruiseTicket) {
    }

    public HpSpecifiedColumn inviteDerivedToDreamCruise(String derivedAlias) {
        return null;
    }

    public ConditionBean xcreateDreamCruiseCB() {
        return null;
    }

    public void xmarkAsDeparturePortForDreamCruise() {
    }

    public boolean xisDreamCruiseDeparturePort() {
        return false;
    }

    public boolean xisDreamCruiseShip() {
        return false;
    }

    public ConditionBean xgetDreamCruiseDeparturePort() {
        return null;
    }

    public HpSpecifiedColumn xshowDreamCruiseTicket() {
        return null;
    }

    public void xkeepDreamCruiseJourneyLogBook(String relationPath) {
    }

    public void xsetupSelectDreamCruiseJourneyLogBook() {
    }

    public void xsetupSelectDreamCruiseJourneyLogBookIfUnionExists() {
    }

    public void mysticRhythms(Object mysticBinding) {
    }

    public Object xgetMysticBinding() {
        return null;
    }

    public CursorSelectOption getCursorSelectOption() {
        return null;
    }

    public void enableQueryUpdateCountPreCheck() {
    }

    public void disableQueryUpdateCountPreCheck() {
    }

    public boolean isQueryUpdateCountPreCheck() {
        return false;
    }

    public DerivedTypeHandler xgetDerivedTypeHandler() {
        return null;
    }

    // ===================================================================================
    //                                                                        Purpose Type
    //                                                                        ============
    public HpCBPurpose getPurpose() {
        return null;
    }

    public void xsetupForScalarSelect() {
    }

    public void xsetupForQueryInsert() {
    }

    public void xsetupForSpecifiedUpdate() {
    }

    public void xsetupForVaryingUpdate() {
    }

    public void enableThatsBadTiming() {
    }

    public void disableThatsBadTiming() {
    }
}
