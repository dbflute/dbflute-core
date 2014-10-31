/*
 * Copyright 2014-2014 the original author or authors.
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
package org.dbflute.cbean.chelper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.dbflute.cbean.ConditionBean;
import org.dbflute.cbean.ConditionQuery;
import org.dbflute.cbean.dream.SpecifiedColumn;
import org.dbflute.cbean.exception.ConditionBeanExceptionThrower;
import org.dbflute.cbean.sqlclause.SqlClause;
import org.dbflute.cbean.sqlclause.join.InnerJoinNoWaySpeaker;
import org.dbflute.cbean.sqlclause.query.QueryUsedAliasInfo;
import org.dbflute.dbmeta.DBMeta;
import org.dbflute.dbmeta.DBMetaProvider;
import org.dbflute.dbmeta.info.ColumnInfo;
import org.dbflute.system.DBFluteSystem;

/**
 * @param <CQ> The type of condition-query.
 * @author jflute
 */
public abstract class HpAbstractSpecification<CQ extends ConditionQuery> implements HpColumnSpHandler {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final ConditionBean _baseCB;
    protected final HpSpQyCall<CQ> _qyCall;
    protected HpSpQyCall<CQ> _syncQyCall;
    protected final HpCBPurpose _purpose;
    protected final DBMetaProvider _dbmetaProvider;
    protected final HpSDRFunctionFactory _sdrFuncFactory;
    protected CQ _query; // lazy-loaded
    protected boolean _alreadySpecifiedRequiredColumn; // also means specification existence
    protected Map<String, SpecifiedColumn> _specifiedColumnMap; // saves specified columns (lazy-loaded)
    protected boolean _alreadySpecifiedEveryColumn;
    protected boolean _alreadySpecifiedExceptColumn;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * @param baseCB The condition-bean of base level. (NotNull)
     * @param qyCall The call-back for condition-query. (NotNull)
     * @param purpose The purpose of condition-bean. (NotNull)
     * @param dbmetaProvider The provider of DB meta. (NotNull)
     * @param sdrFuncFactory The factory of (specify) derived-referrer function. (NotNull)
     */
    protected HpAbstractSpecification(ConditionBean baseCB, HpSpQyCall<CQ> qyCall, HpCBPurpose purpose, DBMetaProvider dbmetaProvider,
            HpSDRFunctionFactory sdrFuncFactory) {
        _baseCB = baseCB;
        _qyCall = qyCall;
        _purpose = purpose;
        _dbmetaProvider = dbmetaProvider;
        _sdrFuncFactory = sdrFuncFactory;
    }

    // ===================================================================================
    //                                                                Column Specification
    //                                                                ====================
    public SpecifiedColumn xspecifyColumn(String columnName) { // for interface
        return doColumn(columnName);
    }

    protected SpecifiedColumn doColumn(String columnName) { // for extended class
        checkSpecifiedThemeColumnStatus(columnName);
        if (isSpecifiedColumn(columnName)) {
            // returns the same instance as the specified before
            return getSpecifiedColumn(columnName);
        }
        assertColumn(columnName);
        callQuery();
        if (isRequiredColumnSpecificationEnabled()) {
            _alreadySpecifiedRequiredColumn = true;
            doSpecifyRequiredColumn();
        }
        final SqlClause sqlClause = _baseCB.getSqlClause();
        final String tableAliasName;
        if (_query.isBaseQuery()) {
            tableAliasName = sqlClause.getBasePointAliasName();
        } else {
            final String relationPath = _query.xgetRelationPath();
            final int nestLevel = _query.xgetNestLevel();
            tableAliasName = sqlClause.resolveJoinAliasName(relationPath, nestLevel);
            keepDreamCruiseJourneyLogBookIfNeeds(relationPath, tableAliasName);
            reflectDreamCruiseWhereUsedToJoin(relationPath, tableAliasName);
        }
        final SpecifiedColumn specifiedColumn = createSpecifiedColumn(columnName, tableAliasName);
        sqlClause.specifySelectColumn(specifiedColumn);
        saveSpecifiedColumn(columnName, specifiedColumn);
        return specifiedColumn;
    }

    protected void checkSpecifiedThemeColumnStatus(String columnName) {
        if (_alreadySpecifiedEveryColumn) {
            throwSpecifyColumnAlreadySpecifiedEveryColumnException(columnName);
        }
        if (_alreadySpecifiedExceptColumn) {
            throwSpecifyColumnAlreadySpecifiedExceptColumnException(columnName);
        }
    }

    protected void callQuery() {
        if (_query == null) {
            _query = qyCall().qy();
        }
    }

    /**
     * Get the query call with sync. <br>
     * This method is basically for SpecifyColumn.
     * Don't set this (or call-back that uses this) to other objects.
     * @return The instance of query call. (NotNull)
     */
    protected HpSpQyCall<CQ> qyCall() { // basically for SpecifyColumn (NOT DerivedReferrer)
        return _syncQyCall != null ? _syncQyCall : _qyCall;
    }

    protected boolean isRequiredColumnSpecificationEnabled() {
        if (_alreadySpecifiedRequiredColumn) {
            return false;
        }
        return isNormalUse(); // only normal purpose needs
    }

    protected abstract void doSpecifyRequiredColumn();

    protected abstract String getTableDbName();

    protected SpecifiedColumn createSpecifiedColumn(String columnName, String tableAliasName) {
        final DBMeta dbmeta = _dbmetaProvider.provideDBMetaChecked(_query.getTableDbName());
        final ColumnInfo columnInfo = dbmeta.findColumnInfo(columnName);
        return new SpecifiedColumn(tableAliasName, columnInfo, _baseCB);
    }

    // -----------------------------------------------------
    //                             Specified Column Handling
    //                             -------------------------
    public SpecifiedColumn getSpecifiedColumn(String columnName) {
        return _specifiedColumnMap != null ? _specifiedColumnMap.get(columnName) : null;
    }

    public boolean hasSpecifiedColumn() {
        return _specifiedColumnMap != null && !_specifiedColumnMap.isEmpty();
    }

    public boolean isSpecifiedColumn(String columnName) {
        return _specifiedColumnMap != null && _specifiedColumnMap.containsKey(columnName);
    }

    protected void saveSpecifiedColumn(String columnName, SpecifiedColumn specifiedColumn) {
        if (_specifiedColumnMap == null) {
            _specifiedColumnMap = new LinkedHashMap<String, SpecifiedColumn>();
        }
        _specifiedColumnMap.put(columnName, specifiedColumn);
    }

    // -----------------------------------------------------
    //                                          Dream Cruise
    //                                          ------------
    protected void keepDreamCruiseJourneyLogBookIfNeeds(String relationPath, String tableAliasName) {
        if (!_baseCB.xisDreamCruiseShip()) {
            return;
        }
        _baseCB.xkeepDreamCruiseJourneyLogBook(relationPath);
    }

    protected void reflectDreamCruiseWhereUsedToJoin(String relationPath, String tableAliasName) {
        if (!_baseCB.xisDreamCruiseShip()) {
            return;
        }
        // to suppress CountLeastJoin of the relation
        // the DreamCruise might be used in where clause (not correctly but safety logic)
        final ConditionBean portCB = _baseCB.xgetDreamCruiseDeparturePort();
        final QueryUsedAliasInfo usedAliasInfo = new QueryUsedAliasInfo(tableAliasName, new InnerJoinNoWaySpeaker() {
            public boolean isNoWayInner() {
                return true; // non fact of inner-join, because judge is so difficult when DreamCruise
            }
        });
        portCB.getSqlClause().reflectWhereUsedToJoin(usedAliasInfo);
    }

    // ===================================================================================
    //                                                                        Theme Column
    //                                                                        ============
    // -----------------------------------------------------
    //                                          Every Column
    //                                          ------------
    protected void doEveryColumn() {
        if (hasSpecifiedColumn()) {
            throwSpecifyEveryColumnAlreadySpecifiedColumnException();
        }
        callQuery();
        final boolean specifiedUpdateUse = isSpecifiedUpdateUse();
        final List<ColumnInfo> columnInfoList = getColumnInfoList();
        for (ColumnInfo columnInfo : columnInfoList) {
            // primary key specification in BatchUpdate is not allowed
            if (!(specifiedUpdateUse && columnInfo.isPrimary())) {
                doColumn(columnInfo.getColumnDbName());
            }
        }
        _alreadySpecifiedEveryColumn = true;
    }

    public boolean isSpecifiedEveryColumn() { // for e.g. UpdateOption's check
        return _alreadySpecifiedEveryColumn;
    }

    // -----------------------------------------------------
    //                                         Except Column
    //                                         -------------
    protected void doExceptRecordMetaColumn() {
        if (hasSpecifiedColumn()) {
            throwSpecifyExceptColumnAlreadySpecifiedColumnException();
        }
        callQuery();
        final boolean specifiedUpdateUse = isSpecifiedUpdateUse();
        final List<ColumnInfo> columnInfoList = getColumnInfoList();
        for (ColumnInfo columnInfo : columnInfoList) {
            // this specification in BatchUpdate is non-sense but just in case
            if (!isRecordMetaColumn(columnInfo) && !(specifiedUpdateUse && columnInfo.isPrimary())) {
                doColumn(columnInfo.getColumnDbName());
            }
        }
        _alreadySpecifiedExceptColumn = true;
    }

    public boolean isSpecifiedExceptColumn() { // for e.g. UpdateOption's check
        return _alreadySpecifiedExceptColumn;
    }

    protected boolean isRecordMetaColumn(ColumnInfo columnInfo) {
        return columnInfo.isCommonColumn() || columnInfo.isOptimisticLock();
    }

    // -----------------------------------------------------
    //                                         Assist Helper
    //                                         -------------
    protected List<ColumnInfo> getColumnInfoList() {
        final String tableDbName = _query.getTableDbName();
        final DBMeta dbmeta = _dbmetaProvider.provideDBMeta(tableDbName);
        return dbmeta.getColumnInfoList();
    }

    protected boolean isSpecifiedUpdateUse() {
        return HpCBPurpose.SPECIFIED_UPDATE.equals(_purpose);
    }

    // ===================================================================================
    //                                                                      Purpose Assert
    //                                                                      ==============
    protected void assertColumn(String columnName) {
        if (_purpose.isNoSpecifyColumnTwoOrMore()) {
            if (_specifiedColumnMap != null && _specifiedColumnMap.size() > 0) {
                throwSpecifyColumnTwoOrMoreColumnException(columnName);
            }
            // no specification is checked at an other timing
        }
        if (_purpose.isNoSpecifyColumnWithDerivedReferrer()) {
            if (hasDerivedReferrer()) {
                throwSpecifyColumnWithDerivedReferrerException(columnName, null);
            }
        }
        if (isNormalUse()) { // only normal purpose needs
            if (_query == null && !qyCall().has()) { // setupSelect check!
                throwSpecifyColumnNotSetupSelectColumnException(columnName);
            }
        }
    }

    protected void assertRelation(String relationName) {
        if (_purpose.isNoSpecifyRelation()) {
            throwSpecifyRelationIllegalPurposeException(relationName);
        }
    }

    protected void assertDerived(String referrerName) {
        if (_purpose.isNoSpecifyDerivedReferrer()) {
            throwSpecifyDerivedReferrerIllegalPurposeException(referrerName);
        }
        if (_purpose.isNoSpecifyDerivedReferrerTwoOrMore()) {
            if (hasDerivedReferrer()) {
                throwSpecifyDerivedReferrerTwoOrMoreException(referrerName);
            }
        }
        if (_purpose.isNoSpecifyColumnWithDerivedReferrer()) {
            if (_specifiedColumnMap != null && _specifiedColumnMap.size() > 0) {
                throwSpecifyColumnWithDerivedReferrerException(null, referrerName);
            }
        }
    }

    protected boolean isNormalUse() {
        return HpCBPurpose.NORMAL_USE.equals(_purpose);
    }

    // ===================================================================================
    //                                                                       Determination
    //                                                                       =============
    public boolean isAlreadySpecifiedRequiredColumn() {
        return _alreadySpecifiedRequiredColumn;
    }

    protected boolean hasDerivedReferrer() {
        return !_baseCB.getSqlClause().getSpecifiedDerivingAliasList().isEmpty();
    }

    // ===================================================================================
    //                                                                     QyCall Handling
    //                                                                     ===============
    public HpSpQyCall<CQ> xsyncQyCall() { // synchronize Query(Relation)
        return _syncQyCall;
    }

    public void xsetSyncQyCall(HpSpQyCall<CQ> qyCall) {
        _syncQyCall = qyCall;
    }

    public boolean xhasSyncQyCall() {
        return _syncQyCall != null;
    }

    protected <MYCQ extends ConditionQuery> HpSpQyCall<MYCQ> xcreateSpQyCall(HpSpQyHas<MYCQ> has, HpSpQyQy<MYCQ> qy) {
        // might be called as relation
        return new HpSpQyDelegatingCall<MYCQ>(has, qy);
    }

    // ===================================================================================
    //                                                                  Exception Throwing
    //                                                                  ==================
    protected void throwSpecifyColumnTwoOrMoreColumnException(String columnName) {
        createCBExThrower().throwSpecifyColumnTwoOrMoreColumnException(_purpose, _baseCB, columnName);
    }

    protected void throwSpecifyColumnNotSetupSelectColumnException(String columnName) {
        createCBExThrower().throwSpecifyColumnNotSetupSelectColumnException(_baseCB, columnName);
    }

    protected void throwSpecifyColumnWithDerivedReferrerException(String columnName, String referrerName) {
        createCBExThrower().throwSpecifyColumnWithDerivedReferrerException(_purpose, _baseCB, columnName, referrerName);
    }

    protected void throwSpecifyColumnAlreadySpecifiedEveryColumnException(String columnName) {
        final String tableDbName = _baseCB.getTableDbName();
        createCBExThrower().throwSpecifyColumnAlreadySpecifiedEveryColumnException(tableDbName, columnName);
    }

    protected void throwSpecifyColumnAlreadySpecifiedExceptColumnException(String columnName) {
        final String tableDbName = _baseCB.getTableDbName();
        createCBExThrower().throwSpecifyColumnAlreadySpecifiedExceptColumnException(tableDbName, columnName);
    }

    protected void throwSpecifyEveryColumnAlreadySpecifiedColumnException() {
        final String tableDbName = _baseCB.getTableDbName();
        createCBExThrower().throwSpecifyEveryColumnAlreadySpecifiedColumnException(tableDbName, _specifiedColumnMap);
    }

    protected void throwSpecifyExceptColumnAlreadySpecifiedColumnException() {
        final String tableDbName = _baseCB.getTableDbName();
        createCBExThrower().throwSpecifyExceptColumnAlreadySpecifiedColumnException(tableDbName, _specifiedColumnMap);
    }

    protected void throwSpecifyRelationIllegalPurposeException(String relationName) {
        createCBExThrower().throwSpecifyRelationIllegalPurposeException(_purpose, _baseCB, relationName);
    }

    protected void throwSpecifyDerivedReferrerIllegalPurposeException(String referrerName) {
        createCBExThrower().throwSpecifyDerivedReferrerIllegalPurposeException(_purpose, _baseCB, referrerName);
    }

    protected void throwSpecifyDerivedReferrerTwoOrMoreException(String referrerName) {
        createCBExThrower().throwSpecifyDerivedReferrerTwoOrMoreException(_purpose, _baseCB, referrerName);
    }

    // ===================================================================================
    //                                                                    Derived Referrer
    //                                                                    ================
    // creator for sub-class
    @SuppressWarnings("unchecked")
    protected <FUNC extends HpSDRFunction<REFERRER_CB, LOCAL_CQ>, REFERRER_CB extends ConditionBean, LOCAL_CQ extends ConditionQuery> FUNC cHSDRF(
            ConditionBean baseCB, LOCAL_CQ localCQ, HpSDRSetupper<REFERRER_CB, LOCAL_CQ> querySetupper, DBMetaProvider dbmetaProvider) {
        // might be database dependency so cast it
        return (FUNC) newSDRFunction(baseCB, localCQ, querySetupper, dbmetaProvider, _sdrFuncFactory);
    }

    protected <REFERRER_CB extends ConditionBean, LOCAL_CQ extends ConditionQuery> HpSDRFunction<REFERRER_CB, LOCAL_CQ> newSDRFunction(
            ConditionBean baseCB, LOCAL_CQ localCQ, HpSDRSetupper<REFERRER_CB, LOCAL_CQ> querySetupper, DBMetaProvider dbmetaProvider,
            HpSDRFunctionFactory sdrOpFactory) {
        return _sdrFuncFactory.create(baseCB, localCQ, querySetupper, dbmetaProvider);
    }

    public HpSDRFunctionFactory xgetSDRFnFc() { // to put to relation specification 
        return _sdrFuncFactory;
    }

    // ===================================================================================
    //                                                                    Exception Helper
    //                                                                    ================
    protected ConditionBeanExceptionThrower createCBExThrower() {
        return new ConditionBeanExceptionThrower();
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String ln() {
        return DBFluteSystem.ln();
    }
}