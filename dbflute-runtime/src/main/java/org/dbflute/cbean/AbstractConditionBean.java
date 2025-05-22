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
package org.dbflute.cbean;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.dbflute.Entity;
import org.dbflute.cbean.chelper.HpCBPurpose;
import org.dbflute.cbean.chelper.HpCalcSpecification;
import org.dbflute.cbean.chelper.HpColQyHandler;
import org.dbflute.cbean.chelper.HpColQyOperand;
import org.dbflute.cbean.chelper.HpDerivingSubQueryInfo;
import org.dbflute.cbean.chelper.HpSDRFunction;
import org.dbflute.cbean.chelper.HpSDRFunctionFactory;
import org.dbflute.cbean.chelper.HpSDRSetupper;
import org.dbflute.cbean.chelper.HpSpQyCall;
import org.dbflute.cbean.chelper.HpSpQyDelegatingCall;
import org.dbflute.cbean.chelper.HpSpQyHas;
import org.dbflute.cbean.chelper.HpSpQyQy;
import org.dbflute.cbean.cipher.ColumnFunctionCipher;
import org.dbflute.cbean.coption.CursorSelectOption;
import org.dbflute.cbean.coption.DerivedReferrerOption;
import org.dbflute.cbean.coption.DerivedReferrerOptionFactory;
import org.dbflute.cbean.coption.SVOptionCall;
import org.dbflute.cbean.coption.ScalarSelectOption;
import org.dbflute.cbean.coption.StatementConfigCall;
import org.dbflute.cbean.dream.ColumnCalculator;
import org.dbflute.cbean.dream.SpecifiedColumn;
import org.dbflute.cbean.exception.ConditionBeanExceptionThrower;
import org.dbflute.cbean.garnish.SpecifyColumnRequiredChecker;
import org.dbflute.cbean.garnish.SpecifyColumnRequiredExceptDeterminer;
import org.dbflute.cbean.garnish.invoking.InvokingCBeanAgent;
import org.dbflute.cbean.ordering.OrderByBean;
import org.dbflute.cbean.paging.PagingBean;
import org.dbflute.cbean.paging.PagingInvoker;
import org.dbflute.cbean.scoping.AndQuery;
import org.dbflute.cbean.scoping.ModeQuery;
import org.dbflute.cbean.scoping.OrQuery;
import org.dbflute.cbean.scoping.SpecifyQuery;
import org.dbflute.cbean.scoping.UnionQuery;
import org.dbflute.cbean.sqlclause.SqlClause;
import org.dbflute.cbean.sqlclause.clause.ClauseLazyReflector;
import org.dbflute.cbean.sqlclause.clause.SelectClauseType;
import org.dbflute.cbean.sqlclause.orderby.OrderByClause;
import org.dbflute.cbean.sqlclause.query.ColumnQueryClauseCreator;
import org.dbflute.cbean.sqlclause.query.QueryClause;
import org.dbflute.cbean.sqlclause.query.QueryClauseFilter;
import org.dbflute.cbean.sqlclause.select.DreamSetupSelectSynchronizer;
import org.dbflute.dbmeta.DBMeta;
import org.dbflute.dbmeta.DBMetaProvider;
import org.dbflute.dbmeta.accessory.DerivedTypeHandler;
import org.dbflute.dbmeta.info.ColumnInfo;
import org.dbflute.dbmeta.name.ColumnRealName;
import org.dbflute.dbmeta.name.ColumnSqlName;
import org.dbflute.exception.IllegalConditionBeanOperationException;
import org.dbflute.exception.OrScopeQueryAndPartUnsupportedOperationException;
import org.dbflute.jdbc.StatementConfig;
import org.dbflute.system.DBFluteSystem;
import org.dbflute.twowaysql.SqlAnalyzer;
import org.dbflute.twowaysql.factory.SqlAnalyzerFactory;
import org.dbflute.twowaysql.style.BoundDateDisplayStyle;
import org.dbflute.twowaysql.style.BoundDateDisplayTimeZoneProvider;
import org.dbflute.util.DfTypeUtil;
import org.dbflute.util.Srl;

/**
 * The condition-bean as abstract for generated classes. <br>
 * This defines both CB architecture methods and facade methods for generated classes.
 * @author jflute
 */
public abstract class AbstractConditionBean implements ConditionBean {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                             SqlClause
    //                                             ---------
    /** The SQL clause that saves condition-bean SQL parts. (NotNull) */
    protected final SqlClause _sqlClause;
    {
        _sqlClause = createSqlClause();
    }

    // -----------------------------------------------------
    //                                                Paging
    //                                                ------
    /** Is the count executed later? {Internal} */
    protected boolean _pagingCountLater; // the default is on the DBFlute generator (true @since 0.9...)

    /** Can the paging re-select? {Internal} */
    protected boolean _pagingReSelect = true; // fixedly true as default

    /** Does it split SQL execution as select and query? {Internal} */
    protected boolean _pagingSelectAndQuerySplit;

    // -----------------------------------------------------
    //                                                 Union
    //                                                 -----
    /** The list of condition-bean for union. {Internal} (NullAllowed) */
    protected List<ConditionBean> _unionCBeanList;

    /** The synchronizer of union query. {Internal} (NullAllowed) */
    protected UnionQuery<ConditionBean> _unionQuerySynchronizer;

    // -----------------------------------------------------
    //                                          Purpose Type
    //                                          ------------
    /** The purpose of condition-bean. (NotNull) */
    protected HpCBPurpose _purpose = HpCBPurpose.NORMAL_USE; // as default

    /** Is the condition-bean locked? e.g. true if in sub-query process */
    protected boolean _locked;

    // -----------------------------------------------------
    //                                          Dream Cruise
    //                                          ------------
    /** Is this condition-bean departure port for dream cruise? */
    protected boolean _departurePortForDreamCruise;

    /** The departure port (base point condition-bean) of dream cruise. (used when dream cruise) (NullAllowed) */
    protected ConditionBean _dreamCruiseDeparturePort;

    /** The ticket (specified column) of dream cruise. (used when dream cruise) (NullAllowed) */
    protected SpecifiedColumn _dreamCruiseTicket;

    /** The journey log book (relation path) of dream cruise. (used when dream cruise) (NullAllowed) */
    protected List<String> _dreamCruiseJourneyLogBook;

    /** The binding value or dream cruise ticket for mystic binding. (NullAllowed) */
    protected Object _mysticBinding;

    // -----------------------------------------------------
    //                                        Various Option
    //                                        --------------
    /** The max result size of safety select. {Internal} */
    protected int _safetyMaxResultSize;

    /** The option of cursor select. {Internal} (NullAllowed) */
    protected CursorSelectOption _cursorSelectOption; // set by sub-class

    /** The configuration of statement. {Internal} (NullAllowed) */
    protected StatementConfig _statementConfig;

    /** Can the relation mapping (entity instance) be cached? {Internal} */
    protected boolean _canRelationMappingCache = true; // fixedly true as default

    /** Does it allow access to non-specified column? {Internal} */
    protected boolean _nonSpecifiedColumnAccessAllowed; // the default is on the DBFlute generator (false @since 1.1)

    /** Is SpecifyColumn required? (both local and relation) {Internal} */
    protected boolean _specifyColumnRequired;

    /** The determiner to except SpecifyColumn required. (NullAllowed) {Internal} */
    protected SpecifyColumnRequiredExceptDeterminer _specifyColumnRequiredExceptDeterminer;

    /** Is violation of SpecifyColumn required warning only? (both local and relation) {Internal} */
    protected boolean _specifyColumnRequiredWarningOnly; // basically for production

    /** Does it allow selecting undefined classification code? {Internal} */
    protected boolean _undefinedClassificationSelectAllowed;

    /** Does it check record count before QueryUpdate? (contains QueryDelete) {Internal} */
    protected boolean _queryUpdateCountPreCheck;

    /** The handler of derived type. {Internal} (NullAllowed: lazy-loaded) */
    protected DerivedTypeHandler _derivedTypeHandler;

    /** The display style of date for logging, overriding default style. (NullAllowed: configured default style) */
    protected BoundDateDisplayStyle _logDateDisplayStyle;

    // ===================================================================================
    //                                                                           SqlClause
    //                                                                           =========
    /** {@inheritDoc} */
    public SqlClause getSqlClause() {
        return _sqlClause;
    }

    /**
     * @return The created SQL clause, which saves SQL parts of condition-bean. (NotNull)
     */
    protected abstract SqlClause createSqlClause();

    // ===================================================================================
    //                                                                             DB Meta
    //                                                                             =======
    /** {@inheritDoc} */
    public DBMeta asDBMeta() { // not to depend on concrete entity (but little merit any more?)
        return getDBMetaProvider().provideDBMetaChecked(asTableDbName());
    }

    /**
     * @return The provider of DB meta, which you can get DB meta by e.g. table name. (NotNull)
     */
    protected abstract DBMetaProvider getDBMetaProvider();

    // ===================================================================================
    //                                                                        Setup Select
    //                                                                        ============
    protected void doSetupSelect(SsCall callback) {
        final String foreignPropertyName = callback.qf().xgetForeignPropertyName();
        // allowed since 0.9.9.4C but basically SetupSelect should be called before Union
        // (basically for DBFlute internal operation, Dream Cruise)
        //assertSetupSelectBeforeUnion(foreignPropertyName);
        final String foreignTableAliasName = callback.qf().xgetAliasName();
        final String localRelationPath = localCQ().xgetRelationPath();
        final String foreignRelationPath = callback.qf().xgetRelationPath();
        getSqlClause().registerSelectedRelation(foreignTableAliasName, asTableDbName(), foreignPropertyName, localRelationPath,
                foreignRelationPath);
    }

    @FunctionalInterface
    protected static interface SsCall {
        public ConditionQuery qf();
    }

    protected void assertSetupSelectPurpose(String foreignPropertyName) { // called by setupSelect_...() of sub-class
        if (_purpose.isNoSetupSelect()) {
            final String titleName = DfTypeUtil.toClassTitle(this);
            if (HpCBPurpose.OR_SCOPE_QUERY.equals(_purpose) && getSqlClause().isOrScopeQueryPurposeCheckWarningOnly()) {
                showSetupSelectIllegalPurposeWarning(titleName, foreignPropertyName);
            } else {
                throwSetupSelectIllegalPurposeException(titleName, foreignPropertyName);
            }
        }
        if (isLocked()) { // detected
            if (getSqlClause().isThatsBadTimingWarningOnly()) { // in migration
                showSetupSelectThatsBadTimingWarning(foreignPropertyName);
            } else { // basically here
                throwSetupSelectThatsBadTimingException(foreignPropertyName);
            }
        }
    }

    protected void showSetupSelectIllegalPurposeWarning(String className, String foreignPropertyName) {
        createCBExThrower().showSetupSelectIllegalPurposeWarning(_purpose, this, foreignPropertyName);
    }

    protected void throwSetupSelectIllegalPurposeException(String className, String foreignPropertyName) {
        createCBExThrower().throwSetupSelectIllegalPurposeException(_purpose, this, foreignPropertyName);
    }

    protected void showSetupSelectThatsBadTimingWarning(String foreignPropertyName) {
        createCBExThrower().showSetupSelectThatsBadTimingWarning(this, foreignPropertyName);
    }

    protected void throwSetupSelectThatsBadTimingException(String foreignPropertyName) {
        createCBExThrower().throwSetupSelectThatsBadTimingException(this, foreignPropertyName);
    }

    // unused because it has been allowed
    //protected void assertSetupSelectBeforeUnion(String foreignPropertyName) {
    //    if (hasUnionQueryOrUnionAllQuery()) {
    //        throwSetupSelectAfterUnionException(foreignPropertyName);
    //    }
    //}
    //
    //protected void throwSetupSelectAfterUnionException(String foreignPropertyName) {
    //    createCBExThrower().throwSetupSelectAfterUnionException(this, foreignPropertyName);
    //}

    // [DBFlute-0.9.5.3]
    // ===================================================================================
    //                                                                             Specify
    //                                                                             =======
    protected <CQ extends ConditionQuery> HpSpQyCall<CQ> xcreateSpQyCall(HpSpQyHas<CQ> has, HpSpQyQy<CQ> qy) {
        return new HpSpQyDelegatingCall<CQ>(has, qy);
    }

    protected void assertSpecifyPurpose() { // called by specify() of sub-class
        if (_purpose.isNoSpecify()) {
            if (HpCBPurpose.OR_SCOPE_QUERY.equals(_purpose) && getSqlClause().isOrScopeQueryPurposeCheckWarningOnly()) {
                showSpecifyIllegalPurposeWarning();
            } else {
                throwSpecifyIllegalPurposeException();
            }
        }
        if (isLocked() && !xisDreamCruiseShip()) { // DreamCruise might call specify() and query()
            if (getSqlClause().isThatsBadTimingWarningOnly()) { // in migration
                showSpecifyThatsBadTimingWarning();
            } else { // basically here
                throwSpecifyThatsBadTimingException();
            }
        }
    }

    protected void showSpecifyIllegalPurposeWarning() {
        createCBExThrower().showSpecifyIllegalPurposeWarning(_purpose, this);
    }

    protected void throwSpecifyIllegalPurposeException() {
        createCBExThrower().throwSpecifyIllegalPurposeException(_purpose, this);
    }

    protected void showSpecifyThatsBadTimingWarning() {
        createCBExThrower().showSpecifyThatsBadTimingWarning(this);
    }

    protected void throwSpecifyThatsBadTimingException() {
        createCBExThrower().throwSpecifyThatsBadTimingException(this);
    }

    @Deprecated
    public boolean hasSpecifiedColumn() {
        return hasSpecifiedLocalColumn();
    }

    // ===================================================================================
    //                                                                               Query
    //                                                                               =====
    protected void assertQueryPurpose() { // called by query() of sub-class and other queries
        if (_purpose.isNoQuery()) {
            throwQueryIllegalPurposeException();
        }
        if (isLocked()) { // detected
            if (getSqlClause().isThatsBadTimingWarningOnly()) { // in migration
                showQueryThatsBadTimingWarning();
            } else { // basically here
                throwQueryThatsBadTimingException();
            }
        }
    }

    protected void throwQueryIllegalPurposeException() {
        createCBExThrower().throwQueryIllegalPurposeException(_purpose, this);
    }

    protected void showQueryThatsBadTimingWarning() {
        createCBExThrower().showQueryThatsBadTimingWarning(this);
    }

    protected void throwQueryThatsBadTimingException() {
        createCBExThrower().throwQueryThatsBadTimingException(this);
    }

    // -----------------------------------------------------
    //                                  InnerJoin AutoDetect
    //                                  --------------------
    /** {@inheritDoc} */
    public void enableInnerJoinAutoDetect() {
        getSqlClause().enableInnerJoinAutoDetect();
    }

    /** {@inheritDoc} */
    public void disableInnerJoinAutoDetect() {
        getSqlClause().disableInnerJoinAutoDetect();
    }

    // [DBFlute-0.9.5.3]
    // ===================================================================================
    //                                                                        Column Query
    //                                                                        ============
    protected <CB extends ConditionBean> ColumnCalculator xcolqy(CB leftCB, CB rightCB, SpecifyQuery<CB> leftSp, SpecifyQuery<CB> rightSp,
            final String operand) {
        assertQueryPurpose();

        final HpCalcSpecification<CB> leftCalcSp = xcreateCalcSpecification(leftSp);
        leftCalcSp.specify(leftCB);
        final String leftColumn = xbuildColQyLeftColumn(leftCB, leftCalcSp);

        final HpCalcSpecification<CB> rightCalcSp = xcreateCalcSpecification(rightSp);
        rightCalcSp.specify(rightCB);
        final String rightColumn = xbuildColQyRightColumn(rightCB, rightCalcSp);
        rightCalcSp.setLeftCalcSp(leftCalcSp);

        final QueryClause queryClause = xcreateColQyClause(leftColumn, operand, rightColumn, rightCalcSp);
        xregisterColQyClause(queryClause, leftCalcSp, rightCalcSp);
        return rightCalcSp;
    }

    // -----------------------------------------------------
    //                                   Create ColQyOperand
    //                                   -------------------
    protected <CB extends ConditionBean> HpColQyOperand<CB> xcreateColQyOperand(HpColQyHandler<CB> handler) {
        return new HpColQyOperand<CB>(handler);
    }

    protected <CB extends ConditionBean> HpColQyOperand.HpExtendedColQyOperandMySql<CB> xcreateColQyOperandMySql(
            HpColQyHandler<CB> handler) {
        return new HpColQyOperand.HpExtendedColQyOperandMySql<CB>(handler);
    }

    // -----------------------------------------------------
    //                                     Build ColQyColumn
    //                                     -----------------
    protected <CB extends ConditionBean> String xbuildColQyLeftColumn(CB leftCB, HpCalcSpecification<CB> leftCalcSp) {
        final ColumnRealName realName = xextractColQyColumnRealName(leftCB, leftCalcSp);
        return xbuildColQyColumn(leftCB, realName.toString(), "left");
    }

    protected <CB extends ConditionBean> String xbuildColQyRightColumn(CB rightCB, HpCalcSpecification<CB> rightCalcSp) {
        final ColumnRealName realName = xextractColQyColumnRealName(rightCB, rightCalcSp);
        return xbuildColQyColumn(rightCB, realName.toString(), "right");
    }

    protected <CB extends ConditionBean> ColumnRealName xextractColQyColumnRealName(CB cb, HpCalcSpecification<CB> calcSp) {
        final Object mysticBinding = cb.xgetMysticBinding();
        if (mysticBinding != null) {
            calcSp.setMysticBindingSnapshot(mysticBinding);
            return xdoExtractColQyColumnMysticBinding(cb, mysticBinding);
        }
        return xdoExtractColQyColumnSpecifiedColumn(calcSp);
    }

    protected <CB extends ConditionBean> ColumnRealName xdoExtractColQyColumnMysticBinding(CB cb, final Object mysticBinding) {
        final String exp = cb.getSqlClause().registerFreeParameterToThemeList("mystic", mysticBinding);
        return ColumnRealName.create(null, new ColumnSqlName(exp));
    }

    protected <CB extends ConditionBean> ColumnRealName xdoExtractColQyColumnSpecifiedColumn(HpCalcSpecification<CB> calcSp) {
        final ColumnRealName realName = calcSp.getResolvedSpecifiedColumnRealName();
        if (realName == null) {
            createCBExThrower().throwColumnQueryInvalidColumnSpecificationException(this);
        }
        return realName;
    }

    protected <CB extends ConditionBean> String xbuildColQyColumn(CB cb, String source, String themeKey) {
        final String bindingExp = getSqlClause().registerColumnQueryObjectToThemeList(themeKey, cb);
        return Srl.replace(source, "/*pmb.conditionQuery.", bindingExp);
    }

    protected <CB extends ConditionBean> HpCalcSpecification<CB> xcreateCalcSpecification(SpecifyQuery<CB> calcSp) {
        return xnewCalcSpecification(calcSp, this);
    }

    protected <CB extends ConditionBean> HpCalcSpecification<CB> xnewCalcSpecification(SpecifyQuery<CB> calcSp, ConditionBean baseCB) {
        return new HpCalcSpecification<CB>(calcSp, baseCB);
    }

    // -----------------------------------------------------
    //                                    Create ColQyClause
    //                                    ------------------
    protected <CB extends ConditionBean> QueryClause xcreateColQyClause(String leftColumn, String operand, String rightColumn,
            HpCalcSpecification<CB> rightCalcSp) {
        final ColumnQueryClauseCreator creator = xcreateColumnQueryClauseCreator();
        return creator.createColumnQueryClause(leftColumn, operand, rightColumn, rightCalcSp, (columnInfo, valueExp) -> {
            return decryptIfNeeds(columnInfo, valueExp);
        });
    }

    protected <CB extends ConditionBean> void xregisterColQyClause(QueryClause queryClause, HpCalcSpecification<CB> leftCalcSp,
            HpCalcSpecification<CB> rightCalcSp) {
        final ColumnQueryClauseCreator creator = xcreateColumnQueryClauseCreator();
        creator.registerColumnQueryClause(getSqlClause(), queryClause, leftCalcSp, rightCalcSp);
    }

    protected ColumnQueryClauseCreator xcreateColumnQueryClauseCreator() {
        return new ColumnQueryClauseCreator();
    }

    // [DBFlute-0.9.9.4C]
    // ===================================================================================
    //                                                                        Dream Cruise
    //                                                                        ============
    /** {@inheritDoc} */
    public void overTheWaves(SpecifiedColumn dreamCruiseTicket) {
        if (dreamCruiseTicket == null) {
            String msg = "The argument 'dreamCruiseColumn' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        if (_dreamCruiseTicket != null) {
            String msg = "The other dream cruise ticket already exists: " + _dreamCruiseTicket;
            throw new IllegalConditionBeanOperationException(msg);
        }
        if (!dreamCruiseTicket.isDreamCruiseTicket()) {
            String msg = "The specified column was not dream cruise ticket: " + dreamCruiseTicket;
            throw new IllegalConditionBeanOperationException(msg);
        }
        _dreamCruiseTicket = dreamCruiseTicket;
    }

    /** {@inheritDoc} */
    public SpecifiedColumn inviteDerivedToDreamCruise(String derivedAlias) {
        if (!xisDreamCruiseShip()) {
            String msg = "This invitation is only allowed by Dream Cruise Ship: " + derivedAlias;
            throw new IllegalConditionBeanOperationException(msg);
        }
        final SqlClause portClause = xgetDreamCruiseDeparturePort().getSqlClause();
        if (!portClause.hasSpecifiedDerivingSubQuery(derivedAlias)) {
            String msg = "Not found the derived info by the argument 'derivedAlias': " + derivedAlias;
            throw new IllegalArgumentException(msg);
        }
        final ColumnInfo columnInfo = portClause.getSpecifiedDerivingColumnInfo(derivedAlias);
        if (columnInfo == null) {
            String msg = "Not found the derived column by the argument 'derivedAlias': " + derivedAlias;
            throw new IllegalArgumentException(msg);
        }
        return new SpecifiedColumn(null, columnInfo, this, derivedAlias, true);
    }

    /** {@inheritDoc} */
    public ConditionBean xcreateDreamCruiseCB() {
        return xdoCreateDreamCruiseCB();
    }

    protected abstract ConditionBean xdoCreateDreamCruiseCB();

    /** {@inheritDoc} */
    public void xmarkAsDeparturePortForDreamCruise() {
        _departurePortForDreamCruise = true;
    }

    /** {@inheritDoc} */
    public boolean xisDreamCruiseDeparturePort() {
        return _departurePortForDreamCruise;
    }

    /** {@inheritDoc} */
    public boolean xisDreamCruiseShip() {
        return HpCBPurpose.DREAM_CRUISE.equals(getPurpose());
    }

    /** {@inheritDoc} */
    public ConditionBean xgetDreamCruiseDeparturePort() {
        return _dreamCruiseDeparturePort;
    }

    /** {@inheritDoc} */
    public boolean xhasDreamCruiseTicket() {
        return _dreamCruiseTicket != null;
    }

    /** {@inheritDoc} */
    public SpecifiedColumn xshowDreamCruiseTicket() {
        return _dreamCruiseTicket;
    }

    /** {@inheritDoc} */
    public void xkeepDreamCruiseJourneyLogBook(String relationPath) {
        xassertDreamCruiseShip();
        if (_dreamCruiseJourneyLogBook == null) {
            _dreamCruiseJourneyLogBook = new ArrayList<String>();
        }
        _dreamCruiseJourneyLogBook.add(relationPath);
    }

    /** {@inheritDoc} */
    public void xsetupSelectDreamCruiseJourneyLogBook() {
        xassertDreamCruiseShip();
        if (_dreamCruiseJourneyLogBook == null) {
            return;
        }
        xgetDreamCruiseDeparturePort().getSqlClause().registerClauseLazyReflector(new ClauseLazyReflector() {
            public void reflect() {
                xdoSetupSelectDreamCruiseJourneyLogBook();
            }
        });
    }

    /** {@inheritDoc} */
    public void xsetupSelectDreamCruiseJourneyLogBookIfUnionExists() {
        xassertDreamCruiseShip();
        if (_dreamCruiseJourneyLogBook == null) {
            return;
        }
        if (xgetDreamCruiseDeparturePort().hasUnionQueryOrUnionAllQuery()) {
            xsetupSelectDreamCruiseJourneyLogBook();
        }
    }

    protected void xdoSetupSelectDreamCruiseJourneyLogBook() {
        final DreamSetupSelectSynchronizer synchronizer = createDreamSetupSelectSynchronizer();
        synchronizer.setupSelectDreamCruiseJourneyLogBook(asDBMeta(), xgetDreamCruiseDeparturePort(), _dreamCruiseJourneyLogBook);
    }

    protected DreamSetupSelectSynchronizer createDreamSetupSelectSynchronizer() {
        return new DreamSetupSelectSynchronizer();
    }

    protected void xassertDreamCruiseShip() {
        if (!xisDreamCruiseShip()) {
            String msg = "The operation is only allowed at Dream Cruise.";
            throw new IllegalConditionBeanOperationException(msg);
        }
    }

    /** {@inheritDoc} */
    public void mysticRhythms(Object mysticBinding) {
        if (mysticBinding == null) {
            String msg = "The argument 'mysticBinding' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        if (_mysticBinding != null) {
            String msg = "The other mystic binding already exists: " + mysticBinding;
            throw new IllegalConditionBeanOperationException(msg);
        }
        if (mysticBinding instanceof SpecifiedColumn) {
            String msg = "The mystic binding should be bound value: " + mysticBinding;
            throw new IllegalConditionBeanOperationException(msg);
        }
        _mysticBinding = mysticBinding;
    }

    /** {@inheritDoc} */
    public Object xgetMysticBinding() {
        return _mysticBinding;
    }

    // [DBFlute-0.9.6.3]
    // ===================================================================================
    //                                                                       OrScope Query
    //                                                                       =============
    protected <CB extends ConditionBean> void xorSQ(CB cb, OrQuery<CB> orQuery) {
        assertQueryPurpose();
        if (getSqlClause().isOrScopeQueryAndPartEffective()) {
            // limit because of so complex
            String msg = "The OrScopeQuery in and-part is unsupported: " + asTableDbName();
            throw new OrScopeQueryAndPartUnsupportedOperationException(msg);
        }
        xdoOrSQ(cb, orQuery);
    }

    protected <CB extends ConditionBean> void xdoOrSQ(CB cb, OrQuery<CB> orQuery) {
        getSqlClause().beginOrScopeQuery();
        final HpCBPurpose originalPurpose = xhandleOrSQPurposeChange();
        try {
            // cannot lock base condition-bean for now
            // because it uses same instance in or-scope query
            orQuery.query(cb);
        } finally {
            xhandleOrSQPurposeClose(originalPurpose);
            getSqlClause().endOrScopeQuery();
        }
    }

    protected HpCBPurpose xhandleOrSQPurposeChange() { // might be overridden before Java8
        final HpCBPurpose originalPurpose = getPurpose();
        xsetupForOrScopeQuery();
        return originalPurpose;
    }

    protected void xhandleOrSQPurposeClose(HpCBPurpose originalPurpose) {
        if (originalPurpose != null) { // because it might be overridden before Java8
            xchangePurposeSqlClause(originalPurpose, null);
        }
    }

    protected <CB extends ConditionBean> void xorSQAP(CB cb, AndQuery<CB> andQuery) {
        assertQueryPurpose();
        if (!getSqlClause().isOrScopeQueryEffective()) {
            createCBExThrower().throwOrScopeQueryAndPartNotOrScopeException(cb);
        }
        if (getSqlClause().isOrScopeQueryAndPartEffective()) {
            createCBExThrower().throwOrScopeQueryAndPartAlreadySetupException(cb);
        }
        getSqlClause().beginOrScopeQueryAndPart();
        try {
            andQuery.query(cb);
        } finally {
            getSqlClause().endOrScopeQueryAndPart();
        }
    }

    // ===================================================================================
    //                                                                       Invalid Query
    //                                                                       =============
    // -----------------------------------------------------
    //                                         Null or Empty
    //                                         -------------
    /** {@inheritDoc} */
    public void ignoreNullOrEmptyQuery() {
        assertOptionThatBadTiming("ignoreNullOrEmptyQuery()");
        getSqlClause().ignoreNullOrEmptyQuery();
    }

    /** {@inheritDoc} */
    public void checkNullOrEmptyQuery() {
        assertOptionThatBadTiming("checkNullOrEmptyQuery()");
        getSqlClause().checkNullOrEmptyQuery();
    }

    // -----------------------------------------------------
    //                                          Empty String
    //                                          ------------
    /** {@inheritDoc} */
    public void enableEmptyStringQuery(ModeQuery noArgInLambda) {
        assertOptionThatBadTiming("enableEmptyStringQuery()");
        assertObjectNotNull("noArgInLambda", noArgInLambda);
        final boolean originallyAllowed = getSqlClause().isEmptyStringQueryAllowed();
        if (!originallyAllowed) {
            doEnableEmptyStringQuery();
        }
        try {
            noArgInLambda.query();
        } finally {
            if (!originallyAllowed) {
                disableEmptyStringQuery();
            }
        }
    }

    protected void doEnableEmptyStringQuery() {
        getSqlClause().enableEmptyStringQuery();
    }

    /** {@inheritDoc} */
    public void disableEmptyStringQuery() {
        assertOptionThatBadTiming("disableEmptyStringQuery()");
        getSqlClause().disableEmptyStringQuery();
    }

    // -----------------------------------------------------
    //                                            Overriding
    //                                            ----------
    /** {@inheritDoc} */
    public void enableOverridingQuery(ModeQuery noArgInLambda) {
        assertOptionThatBadTiming("enableOverridingQuery()");
        assertObjectNotNull("noArgInLambda", noArgInLambda);
        final boolean originallyAllowed = getSqlClause().isOverridingQueryAllowed();
        if (!originallyAllowed) {
            doEnableOverridingQuery();
        }
        try {
            noArgInLambda.query();
        } finally {
            if (!originallyAllowed) {
                disableOverridingQuery();
            }
        }
    }

    protected void doEnableOverridingQuery() {
        getSqlClause().enableOverridingQuery();
    }

    /** {@inheritDoc} */
    public void disableOverridingQuery() {
        assertOptionThatBadTiming("disableOverridingQuery()");
        getSqlClause().disableOverridingQuery();
    }

    // ===================================================================================
    //                                                                   Accept PrimaryKey
    //                                                                   =================
    /** {@inheritDoc} */
    public void acceptPrimaryKeyMap(Map<String, ? extends Object> primaryKeyMap) {
        if (!asDBMeta().hasPrimaryKey()) {
            String msg = "The table has no primary-keys: " + asTableDbName();
            throw new IllegalConditionBeanOperationException(msg);
        }
        final Entity entity = asDBMeta().newEntity();
        asDBMeta().acceptPrimaryKeyMap(entity, primaryKeyMap);
        final Map<String, Object> filteredMap = asDBMeta().extractPrimaryKeyMap(entity);
        for (Entry<String, Object> entry : filteredMap.entrySet()) {
            localCQ().invokeQuery(entry.getKey(), "equal", entry.getValue());
        }
    }

    // ===================================================================================
    //                                                        Implementation of PagingBean
    //                                                        ============================
    // -----------------------------------------------------
    //                                  Paging Determination
    //                                  --------------------
    /** {@inheritDoc} */
    public boolean isPaging() { // for parameter comment
        String msg = "This method is unsupported on ConditionBean!";
        throw new IllegalConditionBeanOperationException(msg);
    }

    /** {@inheritDoc} */
    public boolean canPagingCountLater() { // for framework
        return _pagingCountLater;
    }

    /** {@inheritDoc} */
    public boolean canPagingReSelect() { // for framework
        return _pagingReSelect;
    }

    // -----------------------------------------------------
    //                                        Paging Setting
    //                                        --------------
    /** {@inheritDoc} */
    public void paging(int pageSize, int pageNumber) {
        assertOptionThatBadTiming("paging()");
        if (pageSize <= 0) {
            throwPagingPageSizeNotPlusException(pageSize, pageNumber);
        }
        fetchFirst(pageSize);
        xfetchPage(pageNumber);
    }

    protected void throwPagingPageSizeNotPlusException(int pageSize, int pageNumber) {
        createCBExThrower().throwPagingPageSizeNotPlusException(this, pageSize, pageNumber);
    }

    /** {@inheritDoc} */
    public void xsetPaging(boolean paging) {
        // Do nothing because this is unsupported on ConditionBean.
        // And it is possible that this method is called by PagingInvoker.
    }

    /** {@inheritDoc} */
    public void enablePagingCountLater() {
        assertOptionThatBadTiming("enablePagingCountLater()");
        _pagingCountLater = true;
        getSqlClause().enablePagingCountLater(); // tell her about it
    }

    /** {@inheritDoc} */
    public void disablePagingCountLater() {
        assertOptionThatBadTiming("disablePagingCountLater()");
        _pagingCountLater = false;
        getSqlClause().disablePagingCountLater(); // tell her about it
    }

    /** {@inheritDoc} */
    public void enablePagingReSelect() {
        assertOptionThatBadTiming("enablePagingReSelect()");
        _pagingReSelect = true;
    }

    /** {@inheritDoc} */
    public void disablePagingReSelect() {
        assertOptionThatBadTiming("disablePagingReSelect()");
        _pagingReSelect = false;
    }

    // ConditionBean original
    /** {@inheritDoc} */
    public void enablePagingCountLeastJoin() {
        assertOptionThatBadTiming("enablePagingCountLeastJoin()");
        getSqlClause().enablePagingCountLeastJoin();
    }

    /** {@inheritDoc} */
    public void disablePagingCountLeastJoin() {
        assertOptionThatBadTiming("disablePagingCountLeastJoin()");
        getSqlClause().disablePagingCountLeastJoin();
    }

    /** {@inheritDoc} */
    public boolean canPagingSelectAndQuerySplit() {
        return _pagingSelectAndQuerySplit;
    }

    /**
     * Enable that it splits the SQL execute select and query of paging. <br>
     * You should confirm that the executed SQL on log matches with your expectation. <br>
     * It is very difficult internal logic so it also has simplistic logic. Be careful!
     * <pre>
     * Cannot use this:
     *  o if no PK or compound PK table (exception is thrown)
     *  o if SpecifiedDerivedOrderBy or not Paging (but no exception)
     *
     * Automatically Changed:
     *  o disable PagingCountLater (to suppress rows calculation)
     * </pre>
     * @deprecated This is rare handling for performance tuning so don't use this easily.
     */
    public void enablePagingSelectAndQuerySplit() {
        assertOptionThatBadTiming("enablePagingSelectAndQuerySplit()");
        final DBMeta dbmeta = asDBMeta();
        if (!dbmeta.hasPrimaryKey() || dbmeta.getPrimaryInfo().isCompoundKey()) {
            String msg = "The PagingSelectAndQuerySplit needs only-one column key table: " + asTableDbName();
            throw new IllegalConditionBeanOperationException(msg);
        }
        // MySQL's rows calculation is not fit with this function
        // e.g.
        //  paging : select PK only by business condition with sql_calc_found_rows
        //  paging : select business data by PK without no sql_calc_found_rows
        //  count  : select found_rows() -> returns latest count, why?  
        disablePagingCountLater();
        _pagingSelectAndQuerySplit = true;
    }

    public void disablePagingSelectAndQuerySplit() {
        assertOptionThatBadTiming("disablePagingSelectAndQuerySplit()");
        _pagingSelectAndQuerySplit = false;
    }

    // -----------------------------------------------------
    //                                         Fetch Setting
    //                                         -------------
    /** {@inheritDoc} */
    public PagingBean fetchFirst(int fetchSize) {
        assertOptionThatBadTiming("fetchFirst()");
        getSqlClause().fetchFirst(fetchSize);
        return this;
    }

    /** {@inheritDoc} */
    public PagingBean xfetchScope(int fetchStartIndex, int fetchSize) {
        assertOptionThatBadTiming("xfetchScope()");
        getSqlClause().fetchScope(fetchStartIndex, fetchSize);
        return this;
    }

    /** {@inheritDoc} */
    public PagingBean xfetchPage(int fetchPageNumber) {
        assertOptionThatBadTiming("xfetchPage()");
        getSqlClause().fetchPage(fetchPageNumber);
        return this;
    }

    // -----------------------------------------------------
    //                                       Paging Resource
    //                                       ---------------
    /** {@inheritDoc} */
    public <ENTITY> PagingInvoker<ENTITY> createPagingInvoker(String tableDbName) {
        return new PagingInvoker<ENTITY>(tableDbName);
    }

    // -----------------------------------------------------
    //                                        Fetch Property
    //                                        --------------
    /** {@inheritDoc} */
    public int getFetchStartIndex() {
        return getSqlClause().getFetchStartIndex();
    }

    /** {@inheritDoc} */
    public int getFetchSize() {
        return getSqlClause().getFetchSize();
    }

    /** {@inheritDoc} */
    public int getFetchPageNumber() {
        return getSqlClause().getFetchPageNumber();
    }

    /** {@inheritDoc} */
    public int getPageStartIndex() {
        return getSqlClause().getPageStartIndex();
    }

    /** {@inheritDoc} */
    public int getPageEndIndex() {
        return getSqlClause().getPageEndIndex();
    }

    /** {@inheritDoc} */
    public boolean isFetchScopeEffective() {
        return getSqlClause().isFetchScopeEffective();
    }

    // -----------------------------------------------------
    //                                         Hint Property
    //                                         -------------
    /**
     * Get select-hint. {select [select-hint] * from table...}
     * @return select-hint. (NotNull)
     */
    public String getSelectHint() {
        return getSqlClause().getSelectHint();
    }

    /**
     * Get from-base-table-hint. {select * from table [from-base-table-hint] where ...}
     * @return from-base-table-hint. (NotNull)
     */
    public String getFromBaseTableHint() {
        return getSqlClause().getFromBaseTableHint();
    }

    /**
     * Get from-hint. {select * from table left outer join ... on ... [from-hint] where ...}
     * @return from-hint. (NotNull)
     */
    public String getFromHint() {
        return getSqlClause().getFromHint();
    }

    /**
     * Get sql-suffix. {select * from table where ... order by ... [sql-suffix]}
     * @return Sql-suffix.  (NotNull)
     */
    public String getSqlSuffix() {
        return getSqlClause().getSqlSuffix();
    }

    // ===================================================================================
    //                                                         Implementation of FetchBean
    //                                                         ===========================
    /** {@inheritDoc} */
    public void checkSafetyResult(int safetyMaxResultSize) {
        assertOptionThatBadTiming("checkSafetyResult()");
        _safetyMaxResultSize = safetyMaxResultSize;
    }

    /** {@inheritDoc} */
    public int getSafetyMaxResultSize() {
        return _safetyMaxResultSize;
    }

    // ===================================================================================
    //                                                Implementation of FetchNarrowingBean
    //                                                ====================================
    /** {@inheritDoc} */
    public int getFetchNarrowingSkipStartIndex() {
        return getSqlClause().getFetchNarrowingSkipStartIndex();
    }

    /** {@inheritDoc} */
    public int getFetchNarrowingLoopCount() {
        return getSqlClause().getFetchNarrowingLoopCount();
    }

    /** {@inheritDoc} */
    public boolean isFetchNarrowingSkipStartIndexEffective() {
        return !getSqlClause().isFetchStartIndexSupported();
    }

    /** {@inheritDoc} */
    public boolean isFetchNarrowingLoopCountEffective() {
        return !getSqlClause().isFetchSizeSupported();
    }

    /** {@inheritDoc} */
    public boolean isFetchNarrowingEffective() {
        return getSqlClause().isFetchNarrowingEffective();
    }

    /** {@inheritDoc} */
    public void xdisableFetchNarrowing() {
        // no need to disable in ConditionBean, basically for OutsideSql
        String msg = "This method is unsupported on ConditionBean!";
        throw new UnsupportedOperationException(msg);
    }

    /** {@inheritDoc} */
    public void xenableIgnoredFetchNarrowing() {
        // do nothing
    }

    // ===================================================================================
    //                                                       Implementation of OrderByBean
    //                                                       =============================
    /** {@inheritDoc} */
    public String getOrderByClause() {
        return _sqlClause.getOrderByClause();
    }

    /** {@inheritDoc} */
    public OrderByClause getOrderByComponent() {
        return getSqlClause().getOrderByComponent();
    }

    /** {@inheritDoc} */
    public OrderByBean clearOrderBy() {
        getSqlClause().clearOrderBy();
        return this;
    }

    // ===================================================================================
    //                                                                        Lock Setting
    //                                                                        ============
    /** {@inheritDoc} */
    public ConditionBean lockForUpdate() {
        assertOptionThatBadTiming("lockForUpdate()");
        getSqlClause().lockForUpdate();
        return this;
    }

    // ===================================================================================
    //                                                                        Select Count
    //                                                                        ============
    /** {@inheritDoc} */
    public ConditionBean xsetupSelectCountIgnoreFetchScope(boolean uniqueCount) {
        _isSelectCountIgnoreFetchScope = true;

        final SelectClauseType clauseType;
        if (uniqueCount) {
            clauseType = SelectClauseType.UNIQUE_COUNT;
        } else {
            clauseType = SelectClauseType.PLAIN_COUNT;
        }
        getSqlClause().classifySelectClauseType(clauseType);
        getSqlClause().suppressOrderBy();
        getSqlClause().suppressFetchScope();
        return this;
    }

    /** {@inheritDoc} */
    public ConditionBean xafterCareSelectCountIgnoreFetchScope() {
        _isSelectCountIgnoreFetchScope = false;

        getSqlClause().rollbackSelectClauseType();
        getSqlClause().reviveOrderBy();
        getSqlClause().reviveFetchScope();
        return this;
    }

    /** Is set up various things for select-count-ignore-fetch-scope? */
    protected boolean _isSelectCountIgnoreFetchScope;

    /** {@inheritDoc} */
    public boolean isSelectCountIgnoreFetchScope() {
        return _isSelectCountIgnoreFetchScope;
    }

    // ===================================================================================
    //                                                                       Cursor Select
    //                                                                       =============
    /** {@inheritDoc} */
    public CursorSelectOption getCursorSelectOption() {
        return _cursorSelectOption;
    }

    protected void doAcceptCursorSelectOption(SVOptionCall<CursorSelectOption> opLambda) {
        final CursorSelectOption op = newCursorSelectOption();
        opLambda.callback(op);
        _cursorSelectOption = op;
    }

    protected CursorSelectOption newCursorSelectOption() {
        return new CursorSelectOption();
    }

    // ===================================================================================
    //                                                                       Scalar Select
    //                                                                       =============
    /** {@inheritDoc} */
    public void xacceptScalarSelectOption(ScalarSelectOption option) {
        getSqlClause().acceptScalarSelectOption(option);
    }

    // ===================================================================================
    //                                                             Statement Configuration
    //                                                             =======================
    /** {@inheritDoc} */
    public void configure(StatementConfigCall<StatementConfig> confLambda) {
        assertOptionThatBadTiming("configure()");
        assertStatementConfigNotDuplicated(confLambda);
        _statementConfig = createStatementConfig(confLambda);
    }

    protected void assertStatementConfigNotDuplicated(StatementConfigCall<StatementConfig> configCall) {
        if (_statementConfig != null) {
            String msg = "Already registered the configuration: existing=" + _statementConfig + ", new=" + configCall;
            throw new IllegalConditionBeanOperationException(msg);
        }
    }

    protected StatementConfig createStatementConfig(StatementConfigCall<StatementConfig> configCall) {
        if (configCall == null) {
            throw new IllegalArgumentException("The argument 'confLambda' should not be null.");
        }
        final StatementConfig config = newStatementConfig();
        configCall.callback(config);
        return config;
    }

    protected StatementConfig newStatementConfig() {
        return new StatementConfig();
    }

    /** {@inheritDoc} */
    public StatementConfig getStatementConfig() {
        return _statementConfig;
    }

    // ===================================================================================
    //                                                                      Entity Mapping
    //                                                                      ==============
    // -----------------------------------------------------
    //                                Relation Mapping Cache
    //                                ----------------------
    /**
     * Disable (entity instance) cache of relation mapping. <br>
     * Basically you don't need this. This is for accidents.
     * @deprecated You should not use this easily. It's a dangerous function.
     */
    public void disableRelationMappingCache() {
        assertOptionThatBadTiming("disableRelationMappingCache()");
        // deprecated methods from the beginning are not defined as interface methods
        _canRelationMappingCache = false;
    }

    /** {@inheritDoc} */
    public boolean canRelationMappingCache() {
        return _canRelationMappingCache;
    }

    // -----------------------------------------------------
    //                           Non-Specified Column Access
    //                           ---------------------------
    /** {@inheritDoc} */
    public void enableNonSpecifiedColumnAccess() {
        _nonSpecifiedColumnAccessAllowed = true;
    }

    /** {@inheritDoc} */
    public void disableNonSpecifiedColumnAccess() {
        _nonSpecifiedColumnAccessAllowed = false;
    }

    /** {@inheritDoc} */
    public boolean isNonSpecifiedColumnAccessAllowed() {
        return _nonSpecifiedColumnAccessAllowed;
    }

    // -----------------------------------------------------
    //                                SpecifyColumn Required
    //                                ----------------------
    /** {@inheritDoc} */
    public void enableSpecifyColumnRequired() {
        _specifyColumnRequired = true;
    }

    /** {@inheritDoc} */
    public void disableSpecifyColumnRequired() {
        _specifyColumnRequired = false;
    }

    protected void xenableSpecifyColumnRequiredWarningOnly() { // since 1.2.0
        _specifyColumnRequiredWarningOnly = true;
    }

    /** {@inheritDoc} */
    public void xcheckSpecifyColumnRequiredIfNeeds() {
        if (!_specifyColumnRequired || xisExceptSpecifyColumnRequired()) {
            return;
        }
        xcreateSpecifyColumnRequiredChecker().checkSpecifyColumnRequiredIfNeeds(this, nonSpecifiedAliasSet -> {
            createCBExThrower().throwRequiredSpecifyColumnNotFoundException(this, nonSpecifiedAliasSet);
        });
    }

    protected boolean xisExceptSpecifyColumnRequired() {
        if (_specifyColumnRequiredExceptDeterminer != null && _specifyColumnRequiredExceptDeterminer.isExcept(this)) {
            return true;
        } else {
            return SpecifyColumnRequiredExceptDeterminer.Bowgun.getDefaultDeterminer().isExcept(this);
        }
    }

    protected SpecifyColumnRequiredChecker xcreateSpecifyColumnRequiredChecker() {
        final SpecifyColumnRequiredChecker checker = new SpecifyColumnRequiredChecker();
        if (xisSpecifyColumnRequiredWarningOnly()) {
            checker.warningOnly();
        }
        return checker;
    }

    protected boolean xisSpecifyColumnRequiredWarningOnly() { // since 1.2.0
        return _specifyColumnRequiredWarningOnly;
    }

    protected void xsetSpecifyColumnRequiredExceptDeterminer(SpecifyColumnRequiredExceptDeterminer exceptDeterminer) {
        _specifyColumnRequiredExceptDeterminer = exceptDeterminer;
    }

    // ===================================================================================
    //                                                            Undefined Classification
    //                                                            ========================
    /** {@inheritDoc} */
    public void enableUndefinedClassificationSelect() {
        _undefinedClassificationSelectAllowed = true;
    }

    /** {@inheritDoc} */
    public void disableUndefinedClassificationSelect() {
        _undefinedClassificationSelectAllowed = false;
    }

    /** {@inheritDoc} */
    public boolean isUndefinedClassificationSelectAllowed() {
        return _undefinedClassificationSelectAllowed;
    }

    // ===================================================================================
    //                                                                   Column NullObject
    //                                                                   =================
    /** {@inheritDoc} */
    public void enableColumnNullObject() {
        assertOptionThatBadTiming("enableColumnNullObject()");
        getSqlClause().enableColumnNullObject();
    }

    /** {@inheritDoc} */
    public void disableColumnNullObject() { // e.g. called by cache method in application
        assertOptionThatBadTiming("disableColumnNullObject()");
        getSqlClause().disableColumnNullObject();
    }

    // ===================================================================================
    //                                                                        Query Update
    //                                                                        ============
    /** {@inheritDoc} */
    public void enableQueryUpdateCountPreCheck() {
        assertOptionThatBadTiming("enableQueryUpdateCountPreCheck()");
        _queryUpdateCountPreCheck = true;
    }

    /** {@inheritDoc} */
    public void disableQueryUpdateCountPreCheck() {
        assertOptionThatBadTiming("disableQueryUpdateCountPreCheck()");
        _queryUpdateCountPreCheck = false;
    }

    /** {@inheritDoc} */
    public boolean isQueryUpdateCountPreCheck() {
        return _queryUpdateCountPreCheck;
    }

    // ===================================================================================
    //                                                                     Embed Condition
    //                                                                     ===============
    /**
     * Embed conditions in their variables on where clause (and 'on' clause). <br>
     * You should not use this normally. It's a final weapon! <br>
     * And that this method is not perfect so be attention! <br>
     * If the same-name-columns exist in your conditions, both are embedded. <br>
     * And an empty set means that all conditions are target.
     * @param embeddedColumnInfoSet The set of embedded target column information. (NotNull)
     * @param quote Should the conditions value be quoted?
     * @deprecated You should not use this easily. It's a dangerous function.
     */
    public void embedCondition(Set<ColumnInfo> embeddedColumnInfoSet, boolean quote) {
        // deprecated methods from the beginning are not defined as interface methods
        assertOptionThatBadTiming("embedCondition()");
        if (embeddedColumnInfoSet == null) {
            String msg = "The argument 'embedCondition' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        if (quote) {
            addWhereClauseSimpleFilter(newToEmbeddedQuotedSimpleFilter(embeddedColumnInfoSet));
        } else {
            addWhereClauseSimpleFilter(newToEmbeddedSimpleFilter(embeddedColumnInfoSet));
        }
    }

    private QueryClauseFilter newToEmbeddedQuotedSimpleFilter(Set<ColumnInfo> embeddedColumnInfoSet) {
        return new QueryClauseFilter.QueryClauseToEmbeddedQuotedSimpleFilter(embeddedColumnInfoSet);
    }

    private QueryClauseFilter newToEmbeddedSimpleFilter(Set<ColumnInfo> embeddedColumnInfoSet) {
        return new QueryClauseFilter.QueryClauseToEmbeddedSimpleFilter(embeddedColumnInfoSet);
    }

    private void addWhereClauseSimpleFilter(QueryClauseFilter whereClauseSimpleFilter) {
        _sqlClause.addWhereClauseSimpleFilter(whereClauseSimpleFilter);
    }

    // ===================================================================================
    //                                                                          DisplaySQL
    //                                                                          ==========
    /** {@inheritDoc} */
    public String toDisplaySql() {
        final SqlAnalyzerFactory factory = getSqlAnalyzerFactory();
        final BoundDateDisplayStyle realStyle;
        final BoundDateDisplayStyle specifiedStyle = getLogDateDisplayStyle();
        if (specifiedStyle != null) {
            realStyle = specifiedStyle;
        } else {
            realStyle = createConfiguredBoundDateDisplayStyle();
        }
        return convertConditionBean2DisplaySql(factory, this, realStyle);
    }

    // to get from assistant
    protected abstract SqlAnalyzerFactory getSqlAnalyzerFactory();

    protected BoundDateDisplayStyle createConfiguredBoundDateDisplayStyle() {
        final String datePattern = getConfiguredLogDatePattern();
        final String timestampPattern = getConfiguredLogTimestampPattern();
        final String timePattern = getConfiguredLogTimePattern();
        final BoundDateDisplayTimeZoneProvider timeZoneProvider = getConfiguredLogTimeZoneProvider();
        return new BoundDateDisplayStyle(datePattern, timestampPattern, timePattern, timeZoneProvider);
    }

    // to get from DBFluteConfig
    protected abstract String getConfiguredLogDatePattern();

    protected abstract String getConfiguredLogTimestampPattern();

    protected abstract String getConfiguredLogTimePattern();

    protected abstract BoundDateDisplayTimeZoneProvider getConfiguredLogTimeZoneProvider();

    protected static String convertConditionBean2DisplaySql(SqlAnalyzerFactory factory, ConditionBean cb,
            BoundDateDisplayStyle dateDisplayStyle) {
        final String twoWaySql = cb.getSqlClause().getClause();
        return SqlAnalyzer.convertTwoWaySql2DisplaySql(factory, twoWaySql, cb, dateDisplayStyle);
    }

    /** {@inheritDoc} */
    public void styleLogDateDisplay(BoundDateDisplayStyle logDateDisplayStyle) {
        assertOptionThatBadTiming("styleLogDateDisplay()");
        _logDateDisplayStyle = logDateDisplayStyle;
    }

    /** {@inheritDoc} */
    public BoundDateDisplayStyle getLogDateDisplayStyle() {
        return _logDateDisplayStyle;
    }

    // [DBFlute-0.9.5.2]
    // ===================================================================================
    //                                                                       Meta Handling
    //                                                                       =============
    /** {@inheritDoc} */
    public boolean hasWhereClauseOnBaseQuery() {
        return getSqlClause().hasWhereClauseOnBaseQuery();
    }

    /** {@inheritDoc} */
    public void clearWhereClauseOnBaseQuery() {
        getSqlClause().clearWhereClauseOnBaseQuery();
    }

    /** {@inheritDoc} */
    public boolean hasSelectAllPossible() {
        if (!getSqlClause().hasWhereClauseOnBaseQuery() && !getSqlClause().hasBaseTableInlineWhereClause()) {
            return true;
        }
        // mainCB has clauses here
        if (_unionCBeanList == null || _unionCBeanList.isEmpty()) {
            return false; // no union
        }
        // mainCB has unions
        for (ConditionBean unionCB : _unionCBeanList) {
            if (unionCB.hasSelectAllPossible()) {
                return true;
            }
        }
        return false; // means all unions have clauses
    }

    /** {@inheritDoc} */
    public boolean hasOrderByClause() {
        return getSqlClause().hasOrderByClause();
    }

    // ===================================================================================
    //                                                                 Reflection Invoking
    //                                                                 ===================
    /** {@inheritDoc} */
    public void invokeSetupSelect(String foreignPropertyNamePath) {
        createInvokingCBeanAgent().invokeSetupSelect(foreignPropertyNamePath);
    }

    /** {@inheritDoc} */
    public SpecifiedColumn invokeSpecifyColumn(String columnPropertyPath) {
        return createInvokingCBeanAgent().invokeSpecifyColumn(localSp(), columnPropertyPath);
    }

    protected InvokingCBeanAgent createInvokingCBeanAgent() {
        return new InvokingCBeanAgent(this);
    }

    /** {@inheritDoc} */
    public void invokeOrScopeQuery(OrQuery<ConditionBean> orQuery) {
        xorSQ(this, orQuery);
    }

    /** {@inheritDoc} */
    public void invokeOrScopeQueryAndPart(AndQuery<ConditionBean> andQuery) {
        xorSQAP(this, andQuery);
    }

    // ===================================================================================
    //                                                                         Union Query
    //                                                                         ===========
    protected void xsaveUCB(ConditionBean unionCB) {
        if (_unionCBeanList == null) {
            _unionCBeanList = new ArrayList<ConditionBean>();
        }
        // save for, for example, hasWhereClause()
        _unionCBeanList.add(unionCB);
    }

    /**
     * Synchronize union-query. {Internal}
     * @param unionCB The condition-bean for union. (NotNull)
     */
    protected void xsyncUQ(final ConditionBean unionCB) { // synchronizeUnionQuery()
        if (_unionQuerySynchronizer != null) {
            _unionQuerySynchronizer.query(unionCB); // no lazy allowed
        }
    }

    /** {@inheritDoc} */
    public void xregisterUnionQuerySynchronizer(UnionQuery<ConditionBean> unionQuerySynchronizer) {
        _unionQuerySynchronizer = unionQuerySynchronizer;
    }

    // [DBFlute-0.9.8.4]
    // ===================================================================================
    //                                                                       Geared Cipher
    //                                                                       =============
    protected String decryptIfNeeds(ColumnInfo columnInfo, String valueExp) {
        final ColumnFunctionCipher cipher = getSqlClause().findColumnFunctionCipher(columnInfo);
        return cipher != null ? cipher.decrypt(valueExp) : valueExp;
    }

    // ===================================================================================
    //                                                                    Derived Mappable
    //                                                                    ================
    /** {@inheritDoc} */
    public DerivedTypeHandler xgetDerivedTypeHandler() {
        if (_derivedTypeHandler != null) {
            return _derivedTypeHandler;
        }
        _derivedTypeHandler = xcreateDerivedTypeHandler();
        return _derivedTypeHandler;
    }

    protected DerivedTypeHandler xcreateDerivedTypeHandler() {
        return new DerivedTypeHandler() {
            public Class<?> findMappingType(HpDerivingSubQueryInfo derivingInfo) {
                // [default]
                // count   : Integer
                // max/min : (column type)
                // sum/avg : BigDecimal
                final Class<?> mappingType;
                if (derivingInfo.isFunctionCountFamily()) { // count() or count(distinct)
                    mappingType = xfindDerivedMappingTypeOfCount(derivingInfo);
                } else if (derivingInfo.isFunctionMax()) {
                    mappingType = xfindDerivedMappingTypeOfMax(derivingInfo);
                } else if (derivingInfo.isFunctionMin()) {
                    mappingType = xfindDerivedMappingTypeOfMin(derivingInfo);
                } else if (derivingInfo.isFunctionSum()) {
                    mappingType = xfindDerivedMappingTypeOfSum(derivingInfo);
                } else if (derivingInfo.isFunctionAvg()) {
                    mappingType = xfindDerivedMappingTypeOfAvg(derivingInfo);
                } else { // basically no way, just in case
                    mappingType = xfindDerivedMappingTypeOfDefault(derivingInfo);
                }
                return mappingType;
            }

            public Object convertToMapValue(HpDerivingSubQueryInfo derivingInfo, Object selectedValue) {
                return xconvertToDerivedMapValue(derivingInfo, selectedValue);
            }
        };
    }

    protected Class<?> xfindDerivedMappingTypeOfCount(HpDerivingSubQueryInfo derivingInfo) {
        return Integer.class; // fixedly
    }

    protected Class<?> xfindDerivedMappingTypeOfMax(HpDerivingSubQueryInfo derivingInfo) {
        return derivingInfo.extractDerivingColumnInfo().getObjectNativeType(); // plainly
    }

    protected Class<?> xfindDerivedMappingTypeOfMin(HpDerivingSubQueryInfo derivingInfo) {
        return derivingInfo.extractDerivingColumnInfo().getObjectNativeType(); // plainly
    }

    protected Class<?> xfindDerivedMappingTypeOfSum(HpDerivingSubQueryInfo derivingInfo) {
        return BigDecimal.class; // fixedly
    }

    protected Class<?> xfindDerivedMappingTypeOfAvg(HpDerivingSubQueryInfo derivingInfo) {
        return BigDecimal.class; // fixedly
    }

    protected Class<?> xfindDerivedMappingTypeOfDefault(HpDerivingSubQueryInfo derivingInfo) {
        return String.class; // fixedly
    }

    public Object xconvertToDerivedMapValue(HpDerivingSubQueryInfo derivingInfo, Object value) {
        return value; // no convert as default
    }

    // [DBFlute-1.1.0]
    // ===================================================================================
    //                                                              DerivedReferrer Object
    //                                                              ======================
    protected HpSDRFunctionFactory xcSDRFnFc() { // xcreateFactoryOfSpecifyDerivedReferrerOption()
        return new HpSDRFunctionFactory() {
            public <REFERRER_CB extends ConditionBean, LOCAL_CQ extends ConditionQuery> HpSDRFunction<REFERRER_CB, LOCAL_CQ> create(
                    ConditionBean baseCB, LOCAL_CQ localCQ //
            , HpSDRSetupper<REFERRER_CB, LOCAL_CQ> querySetupper //
            , DBMetaProvider dbmetaProvider) {
                final DerivedReferrerOptionFactory optionFactory = createSpecifyDerivedReferrerOptionFactory();
                return newSDFFunction(baseCB, localCQ, querySetupper, dbmetaProvider, optionFactory);
            }
        };
    }

    /**
     * New-create the function handler of (specify) derived-referrer as plain.
     * @param <REFERRER_CB> The type of referrer condition-bean.
     * @param <LOCAL_CQ> The type of local condition-query.
     * @param baseCB The condition-bean of base table. (NotNull)
     * @param localCQ The condition-query of local table. (NotNull)
     * @param querySetupper The set-upper of sub-query for (specify) derived-referrer. (NotNull)
     * @param dbmetaProvider The provider of DB meta. (NotNull)
     * @param optionFactory The factory of option for (specify) derived-referrer. (NotNull)
     * @return The new-created option of (specify) derived-referrer. (NotNull)
     */
    protected <LOCAL_CQ extends ConditionQuery, REFERRER_CB extends ConditionBean> HpSDRFunction<REFERRER_CB, LOCAL_CQ> newSDFFunction(
            ConditionBean baseCB, LOCAL_CQ localCQ //
            , HpSDRSetupper<REFERRER_CB, LOCAL_CQ> querySetupper //
            , DBMetaProvider dbmetaProvider //
            , DerivedReferrerOptionFactory optionFactory) {
        return new HpSDRFunction<REFERRER_CB, LOCAL_CQ>(baseCB, localCQ, querySetupper, dbmetaProvider, optionFactory);
    }

    protected DerivedReferrerOptionFactory createSpecifyDerivedReferrerOptionFactory() {
        return new DerivedReferrerOptionFactory() {
            public DerivedReferrerOption create() {
                return newSpecifyDerivedReferrerOption();
            }
        };
    }

    /**
     * New-create the option of (specify) derived-referrer as plain.
     * @return The new-created option of (specify) derived-referrer. (NotNull)
     */
    protected DerivedReferrerOption newSpecifyDerivedReferrerOption() {
        return new DerivedReferrerOption();
    }

    // [DBFlute-1.1.0]
    // ===================================================================================
    //                                                                  ExistsReferrer Way
    //                                                                  ==================
    /**
     * Use in-scope sub-query for exists-referrer, basically for performance tuning. <br>
     * The exists-referrer uses plain sub-query way instead of correlation way. <br>
     * <pre>
     * cb.query().existsPurchase(purchaseCB -&gt; {
     *     purchaseCB.<span style="color: #CC4747">useInScopeSubQuery()</span>;
     *     purchaseCB.query().set...
     *     purchaseCB.query().set...
     * });
     * </pre>
     */
    public void useInScopeSubQuery() {
        assertOptionThatBadTiming("useInScopeSubQuery()");
        final HpCBPurpose purpose = getPurpose();
        if (!purpose.isAny(HpCBPurpose.EXISTS_REFERRER, HpCBPurpose.MYSELF_EXISTS)) {
            String msg = "The method 'useInScopeSubQuery()' can be called only when ExistsReferrer.";
            throw new IllegalConditionBeanOperationException(msg);
        }
        getSqlClause().useInScopeSubQueryForExistsReferrer();
    }

    // [DBFlute-0.7.4]
    // ===================================================================================
    //                                                                        Purpose Type
    //                                                                        ============
    /** {@inheritDoc} */
    public HpCBPurpose getPurpose() {
        return _purpose;
    }

    // -----------------------------------------------------
    //                                        Internal Setup
    //                                        --------------
    // /- - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // very internal (super very important)
    // these are called immediate after creation of condition-bean
    // because there are important initializations here
    // - - - - - - - - - -/
    public void xsetupForUnion(ConditionBean mainCB) {
        xinheritSubQueryInfo(mainCB.localCQ());
        xchangePurposeSqlClause(HpCBPurpose.UNION_QUERY, mainCB.localCQ());
    }

    public void xsetupForExistsReferrer(ConditionQuery mainCQ) {
        xprepareSubQueryInfo(mainCQ);
        xchangePurposeSqlClause(HpCBPurpose.EXISTS_REFERRER, mainCQ);
    }

    public void xsetupForInScopeRelation(ConditionQuery mainCQ) {
        xprepareSubQueryInfo(mainCQ);
        xchangePurposeSqlClause(HpCBPurpose.IN_SCOPE_RELATION, mainCQ);
    }

    public void xsetupForDerivedReferrer(ConditionQuery mainCQ) {
        xprepareSubQueryInfo(mainCQ);
        xchangePurposeSqlClause(HpCBPurpose.DERIVED_REFERRER, mainCQ);
    }

    public void xsetupForScalarSelect() { // not sub-query (used independently)
        xchangePurposeSqlClause(HpCBPurpose.SCALAR_SELECT, null);
    }

    public void xsetupForScalarCondition(ConditionQuery mainCQ) {
        xprepareSubQueryInfo(mainCQ);
        xchangePurposeSqlClause(HpCBPurpose.SCALAR_CONDITION, mainCQ);
    }

    public void xsetupForScalarConditionPartitionBy(ConditionQuery mainCQ) {
        xprepareSubQueryInfo(mainCQ);
        xchangePurposeSqlClause(HpCBPurpose.SCALAR_CONDITION_PARTITION_BY, mainCQ);
    }

    public void xsetupForMyselfExists(ConditionQuery mainCQ) {
        xprepareSubQueryInfo(mainCQ);
        xchangePurposeSqlClause(HpCBPurpose.MYSELF_EXISTS, mainCQ);
    }

    public void xsetupForMyselfInScope(ConditionQuery mainCQ) {
        xprepareSubQueryInfo(mainCQ);
        xchangePurposeSqlClause(HpCBPurpose.MYSELF_IN_SCOPE, mainCQ);
    }

    public void xsetupForQueryInsert() { // not sub-query (used independently)
        xchangePurposeSqlClause(HpCBPurpose.QUERY_INSERT, null);
        getSqlClause().disableSelectColumnCipher(); // suppress cipher for values from DB to DB
    }

    public void xsetupForColumnQuery(ConditionBean mainCB) {
        xinheritSubQueryInfo(mainCB.localCQ());
        xchangePurposeSqlClause(HpCBPurpose.COLUMN_QUERY, mainCB.localCQ());

        // inherits a parent query to synchronize real name
        // (and also for suppressing query check) 
        xprepareSyncQyCall(mainCB);
    }

    public void xsetupForOrScopeQuery() {
        xchangePurposeSqlClause(HpCBPurpose.OR_SCOPE_QUERY, null);
    }

    public void xsetupForDreamCruise(ConditionBean mainCB) {
        mainCB.xmarkAsDeparturePortForDreamCruise();
        xinheritSubQueryInfo(mainCB.localCQ());
        xchangePurposeSqlClause(HpCBPurpose.DREAM_CRUISE, mainCB.localCQ());
        _dreamCruiseDeparturePort = mainCB;

        // inherits a parent query to synchronize real name
        // (and also for suppressing query check) 
        xprepareSyncQyCall(mainCB);
    }

    /** {@inheritDoc} */
    public void xsetupForVaryingUpdate() {
        xchangePurposeSqlClause(HpCBPurpose.VARYING_UPDATE, null);
        xprepareSyncQyCall(null); // for suppressing query check
    }

    /** {@inheritDoc} */
    public void xsetupForSpecifiedUpdate() {
        xchangePurposeSqlClause(HpCBPurpose.SPECIFIED_UPDATE, null);
        xprepareSyncQyCall(null); // for suppressing query check
    }

    protected void xinheritSubQueryInfo(ConditionQuery mainCQ) {
        if (mainCQ.xgetSqlClause().isForSubQuery()) {
            getSqlClause().setupForSubQuery(mainCQ.xgetSqlClause().getSubQueryLevel()); // inherited
        }
    }

    protected void xprepareSubQueryInfo(ConditionQuery mainCQ) {
        final int nextSubQueryLevel = mainCQ.xgetSqlClause().getSubQueryLevel() + 1;
        getSqlClause().setupForSubQuery(nextSubQueryLevel); // incremented
    }

    protected void xchangePurposeSqlClause(HpCBPurpose purpose, ConditionQuery mainCQ) {
        _purpose = purpose;
        getSqlClause().setPurpose(purpose); // synchronize
        if (mainCQ != null) {
            // all sub condition-query are target
            // (purposes not allowed to use query() also may have nested query())
            xinheritInvalidQueryInfo(mainCQ);

            // and also inherits inner-join and "that's bad timing"
            xinheritStructurePossibleInnerJoin(mainCQ);
            xinheritWhereUsedInnerJoin(mainCQ);
            xinheritThatsBadTiming(mainCQ);
        }
    }

    protected void xinheritInvalidQueryInfo(ConditionQuery mainCQ) {
        if (mainCQ.xgetSqlClause().isNullOrEmptyQueryChecked()) {
            checkNullOrEmptyQuery();
        } else {
            ignoreNullOrEmptyQuery();
        }
        if (mainCQ.xgetSqlClause().isEmptyStringQueryAllowed()) {
            doEnableEmptyStringQuery();
        } else {
            disableEmptyStringQuery();
        }
        if (mainCQ.xgetSqlClause().isOverridingQueryAllowed()) {
            doEnableOverridingQuery();
        } else {
            disableOverridingQuery();
        }
    }

    protected void xinheritStructurePossibleInnerJoin(ConditionQuery mainCQ) {
        // inherited
        if (mainCQ.xgetSqlClause().isStructuralPossibleInnerJoinEnabled()) { // DBFlute default
            getSqlClause().enableStructuralPossibleInnerJoin();
        } else { // e.g. if it suppresses it by DBFlute property
            getSqlClause().disableStructuralPossibleInnerJoin();
        }
    }

    protected void xinheritWhereUsedInnerJoin(ConditionQuery mainCQ) {
        if (mainCQ.xgetSqlClause().isWhereUsedInnerJoinEnabled()) { // DBFlute default
            getSqlClause().enableWhereUsedInnerJoin();
        } else { // e.g. if it suppresses it by DBFlute property
            getSqlClause().disableWhereUsedInnerJoin();
        }
    }

    protected void xinheritThatsBadTiming(ConditionQuery mainCQ) {
        if (mainCQ.xgetSqlClause().isThatsBadTimingDetectAllowed()) { // DBFlute default
            getSqlClause().enableThatsBadTimingDetect();
        } else { // e.g. if it suppresses it by DBFlute property
            getSqlClause().disableThatsBadTimingDetect();
        }
    }

    protected abstract void xprepareSyncQyCall(ConditionBean mainCB);

    // -----------------------------------------------------
    //                                                  Lock
    //                                                  ----
    protected boolean isLocked() {
        if (xisDreamCruiseDeparturePort()) {
            return false; // dream cruise might call everywhere
        }
        return getSqlClause().isLocked();
    }

    protected void lock() {
        getSqlClause().lock();
    }

    protected void unlock() {
        getSqlClause().unlock();
    }

    /** {@inheritDoc} */
    public void enableThatsBadTiming() {
        assertOptionThatBadTiming("enableThatsBadTiming()");
        getSqlClause().enableThatsBadTimingDetect();
    }

    /** {@inheritDoc} */
    public void disableThatsBadTiming() {
        assertOptionThatBadTiming("disableThatsBadTiming()");
        getSqlClause().disableThatsBadTimingDetect();
    }

    protected void assertOptionThatBadTiming(String optionName) {
        if (isLocked()) { // detected
            if (getSqlClause().isThatsBadTimingWarningOnly()) { // in migration
                createCBExThrower().showOptionThatsBadTimingWarning(this, optionName);
            } else { // basically here
                createCBExThrower().throwOptionThatsBadTimingException(this, optionName);
            }
        }
    }

    // ===================================================================================
    //                                                                    Exception Helper
    //                                                                    ================
    protected ConditionBeanExceptionThrower createCBExThrower() {
        return new ConditionBeanExceptionThrower();
    }

    // ===================================================================================
    //                                                                       Assert Helper
    //                                                                       =============
    /**
     * Assert that the object is not null.
     * @param variableName The check name of variable for message. (NotNull)
     * @param value The checked value. (NotNull)
     * @throws IllegalArgumentException When the argument is null.
     */
    protected void assertObjectNotNull(String variableName, Object value) {
        if (variableName == null) {
            String msg = "The value should not be null: variableName=null value=" + value;
            throw new IllegalArgumentException(msg);
        }
        if (value == null) {
            String msg = "The value should not be null: variableName=" + variableName;
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Assert that the string is not null and not trimmed empty.
     * @param variableName The check name of variable for message. (NotNull)
     * @param value The checked value. (NotNull)
     * @throws IllegalArgumentException When the argument is null or empty.
     */
    protected void assertStringNotNullAndNotTrimmedEmpty(String variableName, String value) {
        assertObjectNotNull("variableName", variableName);
        assertObjectNotNull("value", value);
        if (value.trim().length() == 0) {
            String msg = "The value should not be empty: variableName=" + variableName + " value=" + value;
            throw new IllegalArgumentException(msg);
        }
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String initCap(String str) {
        return Srl.initCap(str);
    }

    protected String ln() {
        return DBFluteSystem.ln();
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        final String titleName = DfTypeUtil.toClassTitle(this);
        sb.append(titleName).append(":");
        try {
            final String displaySql = toDisplaySql();
            sb.append(ln()).append(displaySql);
        } catch (RuntimeException e) {
            sb.append("{toDisplaySql() failed}");
        }
        return sb.toString();
    }
}
