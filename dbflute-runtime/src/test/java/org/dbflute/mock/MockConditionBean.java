/*
 * Copyright 2014-2019 the original author or authors.
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
package org.dbflute.mock;

import java.util.Map;

import org.dbflute.cbean.ConditionBean;
import org.dbflute.cbean.ConditionQuery;
import org.dbflute.cbean.chelper.HpCBPurpose;
import org.dbflute.cbean.chelper.HpColumnSpHandler;
import org.dbflute.cbean.coption.CursorSelectOption;
import org.dbflute.cbean.coption.ScalarSelectOption;
import org.dbflute.cbean.coption.StatementConfigCall;
import org.dbflute.cbean.dream.SpecifiedColumn;
import org.dbflute.cbean.ordering.OrderByBean;
import org.dbflute.cbean.paging.PagingBean;
import org.dbflute.cbean.paging.PagingInvoker;
import org.dbflute.cbean.scoping.AndQuery;
import org.dbflute.cbean.scoping.ModeQuery;
import org.dbflute.cbean.scoping.OrQuery;
import org.dbflute.cbean.scoping.UnionQuery;
import org.dbflute.cbean.sqlclause.SqlClause;
import org.dbflute.cbean.sqlclause.orderby.OrderByClause;
import org.dbflute.dbmeta.DBMeta;
import org.dbflute.dbmeta.accessory.DerivedTypeHandler;
import org.dbflute.jdbc.StatementConfig;
import org.dbflute.twowaysql.style.BoundDateDisplayStyle;

/**
 * @author jflute
 */
public class MockConditionBean implements ConditionBean {

    public DBMeta asDBMeta() {
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

    public void configure(StatementConfigCall<StatementConfig> confCall) {
    }

    public SqlClause getSqlClause() {
        return null;
    }

    public StatementConfig getStatementConfig() {
        return null;
    }

    public String asTableDbName() {
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

    public boolean hasSpecifiedLocalColumn() {
        return false;
    }

    @Deprecated
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

    public void styleLogDateDisplay(BoundDateDisplayStyle logDateDisplayStyle) {
    }

    public BoundDateDisplayStyle getLogDateDisplayStyle() {
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

    public PagingBean xfetchPage(int fetchPageNumber) {
        return null;
    }

    public PagingBean xfetchScope(int fetchStartIndex, int fetchSize) {
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

    public SpecifiedColumn invokeSpecifyColumn(String columnNamePath) {
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

    public void enableEmptyStringQuery(ModeQuery modeQuery) {
    }

    public void disableEmptyStringQuery() {
    }

    public void enableOverridingQuery(ModeQuery modeQuery) {
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

    public void enableNonSpecifiedColumnAccess() {
    }

    public void disableNonSpecifiedColumnAccess() {
    }

    public boolean isNonSpecifiedColumnAccessAllowed() {
        return false;
    }

    public void enableSpecifyColumnRequired() {
    }

    public void disableSpecifyColumnRequired() {
    }

    public void xcheckSpecifyColumnRequiredIfNeeds() {
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

    public void overTheWaves(SpecifiedColumn dreamCruiseTicket) {
    }

    public SpecifiedColumn inviteDerivedToDreamCruise(String derivedAlias) {
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

    public SpecifiedColumn xshowDreamCruiseTicket() {
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

    public void enableUndefinedClassificationSelect() {
    }

    public void disableUndefinedClassificationSelect() {
    }

    public boolean isUndefinedClassificationSelectAllowed() {
        return false;
    }

    public void enableColumnNullObject() {
    }

    public void disableColumnNullObject() {
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
