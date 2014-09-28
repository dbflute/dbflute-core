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

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.seasar.dbflute.Entity;
import org.seasar.dbflute.cbean.chelper.HpCBPurpose;
import org.seasar.dbflute.cbean.chelper.HpCalcSpecification;
import org.seasar.dbflute.cbean.chelper.HpCalculator;
import org.seasar.dbflute.cbean.chelper.HpColQyHandler;
import org.seasar.dbflute.cbean.chelper.HpColQyOperand;
import org.seasar.dbflute.cbean.chelper.HpDerivingSubQueryInfo;
import org.seasar.dbflute.cbean.chelper.HpSpecifiedColumn;
import org.seasar.dbflute.cbean.cipher.ColumnFunctionCipher;
import org.seasar.dbflute.cbean.coption.CursorSelectOption;
import org.seasar.dbflute.cbean.coption.ScalarSelectOption;
import org.seasar.dbflute.cbean.sqlclause.SqlClause;
import org.seasar.dbflute.cbean.sqlclause.clause.ClauseLazyReflector;
import org.seasar.dbflute.cbean.sqlclause.clause.SelectClauseType;
import org.seasar.dbflute.cbean.sqlclause.join.InnerJoinNoWaySpeaker;
import org.seasar.dbflute.cbean.sqlclause.orderby.OrderByClause;
import org.seasar.dbflute.cbean.sqlclause.query.QueryClause;
import org.seasar.dbflute.cbean.sqlclause.query.QueryClauseFilter;
import org.seasar.dbflute.cbean.sqlclause.query.QueryUsedAliasInfo;
import org.seasar.dbflute.cbean.sqlclause.subquery.SubQueryIndentProcessor;
import org.seasar.dbflute.dbmeta.DBMeta;
import org.seasar.dbflute.dbmeta.DBMetaProvider;
import org.seasar.dbflute.dbmeta.DerivedTypeHandler;
import org.seasar.dbflute.dbmeta.info.ColumnInfo;
import org.seasar.dbflute.dbmeta.info.ForeignInfo;
import org.seasar.dbflute.dbmeta.name.ColumnRealName;
import org.seasar.dbflute.dbmeta.name.ColumnSqlName;
import org.seasar.dbflute.exception.ColumnQueryCalculationUnsupportedColumnTypeException;
import org.seasar.dbflute.exception.ConditionInvokingFailureException;
import org.seasar.dbflute.exception.IllegalConditionBeanOperationException;
import org.seasar.dbflute.exception.OrScopeQueryAndPartUnsupportedOperationException;
import org.seasar.dbflute.exception.thrower.ConditionBeanExceptionThrower;
import org.seasar.dbflute.helper.beans.DfBeanDesc;
import org.seasar.dbflute.helper.beans.factory.DfBeanDescFactory;
import org.seasar.dbflute.jdbc.StatementConfig;
import org.seasar.dbflute.resource.DBFluteSystem;
import org.seasar.dbflute.twowaysql.factory.SqlAnalyzerFactory;
import org.seasar.dbflute.util.DfReflectionUtil;
import org.seasar.dbflute.util.DfReflectionUtil.ReflectionFailureException;
import org.seasar.dbflute.util.DfTypeUtil;
import org.seasar.dbflute.util.Srl;

/**
 * The condition-bean as abstract.
 * @author jflute
 */
public abstract class AbstractConditionBean implements ConditionBean {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                             SqlClause
    //                                             ---------
    /** SQL clause instance. */
    protected final SqlClause _sqlClause;
    {
        _sqlClause = createSqlClause();
    }

    // -----------------------------------------------------
    //                                                Paging
    //                                                ------
    /** Is the count executed later? {Internal} */
    protected boolean _pagingCountLater; // the default value is on the DBFlute generator (true @since 0.9...)

    /** Can the paging re-select? {Internal} */
    protected boolean _pagingReSelect = true;

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
    protected boolean _locked = false;

    // -----------------------------------------------------
    //                                          Dream Cruise
    //                                          ------------
    /** Is this condition-bean departure port for dream cruise? */
    protected boolean _departurePortForDreamCruise;

    /** The departure port (base point condition-bean) of dream cruise. (used when dream cruise) (NullAllowed) */
    protected ConditionBean _dreamCruiseDeparturePort;

    /** The ticket (specified column) of dream cruise. (used when dream cruise) (NullAllowed) */
    protected HpSpecifiedColumn _dreamCruiseTicket;

    /** The journey log book (relation path) of dream cruise. (used when dream cruise) (NullAllowed) */
    protected List<String> _dreamCruiseJourneyLogBook;

    /** The binding value or dream cruise ticket for mystic binding. (NullAllowed) */
    protected Object _mysticBinding;

    // -----------------------------------------------------
    //                                        Various Option
    //                                        --------------
    /** The max result size of safety select. {Internal} */
    protected int _safetyMaxResultSize;

    /** Does it check record count before QueryUpdate? (contains QueryDelete) {Internal} */
    protected boolean _queryUpdateCountPreCheck;

    /** The configuration of statement. {Internal} (NullAllowed) */
    protected StatementConfig _statementConfig;

    /** Does it cache of relation entity instance? {Internal} */
    protected boolean _relationMappingCache = true;

    /** The option of cursor select. {Internal} (NullAllowed) */
    protected CursorSelectOption _cursorSelectOption; // set by sub-class

    /** The handler of derived type. {Internal} (NullAllowed: lazy-loaded) */
    protected DerivedTypeHandler _derivedTypeHandler;

    // ===================================================================================
    //                                                                              DBMeta
    //                                                                              ======
    /**
     * {@inheritDoc}
     */
    public DBMeta getDBMeta() {
        return getDBMetaProvider().provideDBMetaChecked(getTableDbName());
    }

    // ===================================================================================
    //                                                                           SqlClause
    //                                                                           =========
    /**
     * {@inheritDoc}
     */
    public SqlClause getSqlClause() {
        return _sqlClause;
    }

    /**
     * Create SQL clause. {for condition-bean}
     * @return SQL clause. (NotNull)
     */
    protected abstract SqlClause createSqlClause();

    // ===================================================================================
    //                                                                     DBMeta Provider
    //                                                                     ===============
    /**
     * Get the provider of DB meta.
     * @return The provider of DB meta. (NotNull)
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
        getSqlClause().registerSelectedRelation(foreignTableAliasName, getTableDbName(), foreignPropertyName,
                localRelationPath, foreignRelationPath);
    }

    protected static interface SsCall {
        public ConditionQuery qf();
    }

    protected void assertSetupSelectPurpose(String foreignPropertyName) { // called by setupSelect_...() of sub-class
        if (_purpose.isNoSetupSelect()) {
            final String titleName = DfTypeUtil.toClassTitle(this);
            throwSetupSelectIllegalPurposeException(titleName, foreignPropertyName);
        }
        if (isLocked()) {
            createCBExThrower().throwSetupSelectThatsBadTimingException(this, foreignPropertyName);
        }
    }

    protected void throwSetupSelectIllegalPurposeException(String className, String foreignPropertyName) {
        createCBExThrower().throwSetupSelectIllegalPurposeException(_purpose, this, foreignPropertyName);
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
    protected void assertSpecifyPurpose() { // called by specify() of sub-class
        if (_purpose.isNoSpecify()) {
            throwSpecifyIllegalPurposeException();
        }
        if (isLocked() && !xisDreamCruiseShip()) { // DreamCruise might call specify() and query()
            createCBExThrower().throwSpecifyThatsBadTimingException(this);
        }
    }

    protected void throwSpecifyIllegalPurposeException() {
        createCBExThrower().throwSpecifyIllegalPurposeException(_purpose, this);
    }

    // ===================================================================================
    //                                                                               Query
    //                                                                               =====
    protected void assertQueryPurpose() { // called by query() of sub-class and other queries
        if (_purpose.isNoQuery()) {
            throwQueryIllegalPurposeException();
        }
        if (isLocked()) {
            createCBExThrower().throwQueryThatsBadTimingException(this);
        }
    }

    protected void throwQueryIllegalPurposeException() {
        createCBExThrower().throwQueryIllegalPurposeException(_purpose, this);
    }

    // -----------------------------------------------------
    //                                  InnerJoin AutoDetect
    //                                  --------------------
    /**
     * {@inheritDoc}
     */
    public void enableInnerJoinAutoDetect() {
        getSqlClause().enableInnerJoinAutoDetect();
    }

    /**
     * {@inheritDoc}
     */
    public void disableInnerJoinAutoDetect() {
        getSqlClause().disableInnerJoinAutoDetect();
    }

    // [DBFlute-0.9.5.3]
    // ===================================================================================
    //                                                                        Column Query
    //                                                                        ============
    protected <CB extends ConditionBean> HpCalculator xcolqy(CB leftCB, CB rightCB, SpecifyQuery<CB> leftSp,
            SpecifyQuery<CB> rightSp, final String operand) {
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

    protected <CB extends ConditionBean> ColumnRealName xextractColQyColumnRealName(CB cb,
            HpCalcSpecification<CB> calcSp) {
        final Object mysticBinding = cb.xgetMysticBinding();
        if (mysticBinding != null) {
            calcSp.setMysticBindingSnapshot(mysticBinding);
            return xdoExtractColQyColumnMysticBinding(cb, mysticBinding);
        }
        return xdoExtractColQyColumnSpecifiedColumn(calcSp);
    }

    protected <CB extends ConditionBean> ColumnRealName xdoExtractColQyColumnMysticBinding(CB cb,
            final Object mysticBinding) {
        final String exp = cb.getSqlClause().registerFreeParameterToThemeList("mystic", mysticBinding);
        return ColumnRealName.create(null, new ColumnSqlName(exp));
    }

    protected <CB extends ConditionBean> ColumnRealName xdoExtractColQyColumnSpecifiedColumn(
            HpCalcSpecification<CB> calcSp) {
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
        return new HpCalcSpecification<CB>(calcSp, this);
    }

    // -----------------------------------------------------
    //                                    Create ColQyClause
    //                                    ------------------
    protected <CB extends ConditionBean> QueryClause xcreateColQyClause(final String leftColumn, final String operand,
            final String rightColumn, final HpCalcSpecification<CB> rightCalcSp) {
        return new QueryClause() {
            @Override
            public String toString() {
                final String leftExp = resolveColumnExp(rightCalcSp.getLeftCalcSp(), leftColumn);
                final String rightExp = resolveColumnExp(rightCalcSp, rightColumn);
                return xbuildColQyClause(leftExp, operand, rightExp);
            }

            protected String resolveColumnExp(HpCalcSpecification<CB> calcSp, String columnExp) {
                final String resolvedExp;
                if (calcSp != null) {
                    final String statement = calcSp.buildStatementToSpecifidName(columnExp);
                    if (statement != null) { // exists calculation
                        assertCalculationColumnType(calcSp);
                        resolvedExp = statement; // cipher already resolved
                    } else {
                        final ColumnInfo columnInfo = calcSp.getSpecifiedColumnInfo();
                        if (columnInfo != null) { // means plain column
                            resolvedExp = decryptIfNeeds(columnInfo, columnExp);
                        } else { // deriving sub-query
                            resolvedExp = columnExp;
                        }
                    }
                } else {
                    resolvedExp = columnExp;
                }
                return resolvedExp;
            }

            protected void assertCalculationColumnType(HpCalcSpecification<CB> calcSp) {
                if (calcSp.hasConvert()) {
                    return; // because it may be Date type
                }
                final ColumnInfo columnInfo = calcSp.getResolvedSpecifiedColumnInfo();
                if (columnInfo != null) { // basically true but checked just in case
                    if (!columnInfo.isObjectNativeTypeNumber()) {
                        // *simple message because other types may be supported at the future
                        String msg = "Not number column specified: " + columnInfo;
                        throw new ColumnQueryCalculationUnsupportedColumnTypeException(msg);
                    }
                }
            }
        };
    }

    protected String xbuildColQyClause(String leftExp, String operand, String rightExp) { // can be overridden just in case
        final StringBuilder sb = new StringBuilder();
        if (hasSubQueryEndOnLastLine(leftExp)) {
            if (hasSubQueryEndOnLastLine(rightExp)) { // (sub-query = sub-query)
                // add line separator before right expression
                // because of independent format for right query
                sb.append(reflectToSubQueryEndOnLastLine(leftExp, " " + operand + " "));
                sb.append(ln()).append("       ").append(rightExp);
            } else { // (sub-query = column)
                sb.append(reflectToSubQueryEndOnLastLine(leftExp, " " + operand + " " + rightExp));
            }
        } else { // (column = sub-query) or (column = column) 
            sb.append(leftExp).append(" ").append(operand).append(" ").append(rightExp);
        }
        return sb.toString();
    }

    protected boolean hasSubQueryEndOnLastLine(String columnExp) {
        return SubQueryIndentProcessor.hasSubQueryEndOnLastLine(columnExp);
    }

    protected String reflectToSubQueryEndOnLastLine(String columnExp, String inserted) {
        return SubQueryIndentProcessor.moveSubQueryEndToRear(columnExp + inserted);
    }

    protected <CB extends ConditionBean> void xregisterColQyClause(QueryClause queryClause,
            final HpCalcSpecification<CB> leftCalcSp, final HpCalcSpecification<CB> rightCalcSp) {
        // /= = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
        // may null-revived -> no way to be inner-join
        // (DerivedReferrer or conversion's coalesce)
        // 
        // for example, the following SQL is no way to be inner
        // (suppose if PURCHASE refers WITHDRAWAL)
        // 
        // select mb.MEMBER_ID, mb.MEMBER_NAME
        //      , mb.MEMBER_STATUS_CODE, wd.MEMBER_ID as WD_MEMBER_ID
        //   from MEMBER mb
        //     left outer join MEMBER_SERVICE ser on mb.MEMBER_ID = ser.MEMBER_ID
        //     left outer join MEMBER_WITHDRAWAL wd on mb.MEMBER_ID = wd.MEMBER_ID
        //  where (select coalesce(max(pc.PURCHASE_PRICE), 0)
        //           from PURCHASE pc
        //          where pc.MEMBER_ID = wd.MEMBER_ID -- may null
        //        ) < ser.SERVICE_POINT_COUNT
        //  order by mb.MEMBER_ID
        // 
        // it has a possible to be inner-join in various case
        // but it is hard to analyze in detail so simplify it
        // = = = = = = = = = =/
        final QueryUsedAliasInfo leftInfo = xcreateColQyAliasInfo(leftCalcSp);
        final QueryUsedAliasInfo rightInfo = xcreateColQyAliasInfo(rightCalcSp);
        getSqlClause().registerWhereClause(queryClause, leftInfo, rightInfo);
    }

    protected <CB extends ConditionBean> QueryUsedAliasInfo xcreateColQyAliasInfo(final HpCalcSpecification<CB> calcSp) {
        final String usedAliasName = calcSp.getResolvedSpecifiedTableAliasName();
        return new QueryUsedAliasInfo(usedAliasName, new InnerJoinNoWaySpeaker() {
            public boolean isNoWayInner() {
                return calcSp.mayNullRevived();
            }
        });
    }

    // [DBFlute-0.9.9.4C]
    // ===================================================================================
    //                                                                        Dream Cruise
    //                                                                        ============
    /**
     * {@inheritDoc}
     */
    public void overTheWaves(HpSpecifiedColumn dreamCruiseTicket) {
        if (dreamCruiseTicket == null) {
            String msg = "The argument 'dreamCruiseColumn' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        if (!dreamCruiseTicket.isDreamCruiseTicket()) {
            String msg = "The specified column was not dream cruise ticket: " + dreamCruiseTicket;
            throw new IllegalConditionBeanOperationException(msg);
        }
        _dreamCruiseTicket = dreamCruiseTicket;
    }

    /**
     * {@inheritDoc}
     */
    public HpSpecifiedColumn inviteDerivedToDreamCruise(String derivedAlias) {
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
        return new HpSpecifiedColumn(null, columnInfo, this, derivedAlias, true);
    }

    /**
     * {@inheritDoc}
     */
    public ConditionBean xcreateDreamCruiseCB() {
        return xdoCreateDreamCruiseCB();
    }

    protected abstract ConditionBean xdoCreateDreamCruiseCB();

    /**
     * {@inheritDoc}
     */
    public void xmarkAsDeparturePortForDreamCruise() {
        _departurePortForDreamCruise = true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean xisDreamCruiseDeparturePort() {
        return _departurePortForDreamCruise;
    }

    /**
     * {@inheritDoc}
     */
    public boolean xisDreamCruiseShip() {
        return HpCBPurpose.DREAM_CRUISE.equals(getPurpose());
    }

    /**
     * {@inheritDoc}
     */
    public ConditionBean xgetDreamCruiseDeparturePort() {
        return _dreamCruiseDeparturePort;
    }

    /**
     * {@inheritDoc}
     */
    public boolean xhasDreamCruiseTicket() {
        return _dreamCruiseTicket != null;
    }

    /**
     * {@inheritDoc}
     */
    public HpSpecifiedColumn xshowDreamCruiseTicket() {
        return _dreamCruiseTicket;
    }

    /**
     * {@inheritDoc}
     */
    public void xkeepDreamCruiseJourneyLogBook(String relationPath) {
        xassertDreamCruiseShip();
        if (_dreamCruiseJourneyLogBook == null) {
            _dreamCruiseJourneyLogBook = new ArrayList<String>();
        }
        _dreamCruiseJourneyLogBook.add(relationPath);
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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
        // small waste exists but simple logic is best here
        final ConditionBean departurePort = xgetDreamCruiseDeparturePort();
        for (String relationPath : _dreamCruiseJourneyLogBook) {
            final List<String> splitList = Srl.splitList(relationPath, "_"); // e.g. _2_5
            final StringBuilder sb = new StringBuilder();
            DBMeta currentMeta = getDBMeta();
            int index = 0;
            for (String element : splitList) {
                if ("".equals(element)) {
                    continue;
                }
                final Integer relationNo = Integer.valueOf(element);
                final ForeignInfo foreignInfo = currentMeta.findForeignInfo(relationNo);
                final String foreignPropertyName = foreignInfo.getForeignPropertyName();
                if (index > 0) {
                    sb.append(".");
                }
                sb.append(foreignPropertyName);
                currentMeta = foreignInfo.getForeignDBMeta();
                ++index;
            }
            departurePort.invokeSetupSelect(sb.toString());
        }
    }

    protected void xassertDreamCruiseShip() {
        if (!xisDreamCruiseShip()) {
            String msg = "The operation is only allowed at Dream Cruise.";
            throw new IllegalConditionBeanOperationException(msg);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void mysticRhythms(Object mysticBinding) {
        if (mysticBinding == null) {
            String msg = "The argument 'mysticBinding' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        if (_mysticBinding != null) {
            String msg = "The other mystic binding already exists: " + mysticBinding;
            throw new IllegalConditionBeanOperationException(msg);
        }
        if (mysticBinding instanceof HpSpecifiedColumn) {
            String msg = "The mystic binding should be bound value: " + mysticBinding;
            throw new IllegalConditionBeanOperationException(msg);
        }
        _mysticBinding = mysticBinding;
    }

    /**
     * {@inheritDoc}
     */
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
            String msg = "The OrScopeQuery in and-part is unsupported: " + getTableDbName();
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
    /**
     * {@inheritDoc}
     */
    public void checkNullOrEmptyQuery() {
        getSqlClause().checkNullOrEmptyQuery();
    }

    /**
     * {@inheritDoc}
     */
    public void ignoreNullOrEmptyQuery() {
        getSqlClause().ignoreNullOrEmptyQuery();
    }

    /**
     * {@inheritDoc}
     */
    public void enableEmptyStringQuery() {
        getSqlClause().enableEmptyStringQuery();
    }

    /**
     * {@inheritDoc}
     */
    public void disableEmptyStringQuery() {
        getSqlClause().disableEmptyStringQuery();
    }

    /**
     * {@inheritDoc}
     */
    public void enableOverridingQuery() {
        getSqlClause().enableOverridingQuery();
    }

    /**
     * {@inheritDoc}
     */
    public void disableOverridingQuery() {
        getSqlClause().disableOverridingQuery();
    }

    // ===================================================================================
    //                                                                   Accept PrimaryKey
    //                                                                   =================
    /**
     * {@inheritDoc}
     */
    public void acceptPrimaryKeyMap(Map<String, ? extends Object> primaryKeyMap) {
        if (!getDBMeta().hasPrimaryKey()) {
            String msg = "The table has no primary-keys: " + getTableDbName();
            throw new UnsupportedOperationException(msg);
        }
        final Entity entity = getDBMeta().newEntity();
        getDBMeta().acceptPrimaryKeyMap(entity, primaryKeyMap);
        final Map<String, Object> filteredMap = getDBMeta().extractPrimaryKeyMap(entity);
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
    /**
     * {@inheritDoc}
     */
    public boolean isPaging() { // for parameter comment
        String msg = "This method is unsupported on ConditionBean!";
        throw new UnsupportedOperationException(msg);
    }

    /**
     * {@inheritDoc}
     */
    public boolean canPagingCountLater() { // for framework
        return _pagingCountLater;
    }

    /**
     * {@inheritDoc}
     */
    public boolean canPagingReSelect() { // for framework
        return _pagingReSelect;
    }

    // -----------------------------------------------------
    //                                        Paging Setting
    //                                        --------------
    /**
     * {@inheritDoc}
     */
    public void paging(int pageSize, int pageNumber) {
        if (pageSize <= 0) {
            throwPagingPageSizeNotPlusException(pageSize, pageNumber);
        }
        fetchFirst(pageSize);
        fetchPage(pageNumber);
    }

    protected void throwPagingPageSizeNotPlusException(int pageSize, int pageNumber) {
        createCBExThrower().throwPagingPageSizeNotPlusException(this, pageSize, pageNumber);
    }

    /**
     * {@inheritDoc}
     */
    public void xsetPaging(boolean paging) {
        // Do nothing because this is unsupported on ConditionBean.
        // And it is possible that this method is called by PagingInvoker.
    }

    /**
     * {@inheritDoc}
     */
    public void enablePagingCountLater() {
        _pagingCountLater = true;
        getSqlClause().enablePagingCountLater(); // tell her about it
    }

    /**
     * {@inheritDoc}
     */
    public void disablePagingCountLater() {
        _pagingCountLater = false;
        getSqlClause().disablePagingCountLater(); // tell her about it
    }

    /**
     * {@inheritDoc}
     */
    public void enablePagingReSelect() {
        _pagingReSelect = true;
    }

    /**
     * {@inheritDoc}
     */
    public void disablePagingReSelect() {
        _pagingReSelect = false;
    }

    // ConditionBean original
    /**
     * {@inheritDoc}
     */
    public void enablePagingCountLeastJoin() {
        getSqlClause().enablePagingCountLeastJoin();
    }

    /**
     * {@inheritDoc}
     */
    public void disablePagingCountLeastJoin() {
        getSqlClause().disablePagingCountLeastJoin();
    }

    /**
     * {@inheritDoc}
     */
    public boolean canPagingSelectAndQuerySplit() {
        return _pagingSelectAndQuerySplit;
    }

    /**
     * Enable that it splits the SQL execute select and query of paging. <br />
     * You should confirm that the executed SQL on log matches with your expectation. <br />
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
        final DBMeta dbmeta = getDBMeta();
        if (!dbmeta.hasPrimaryKey() || dbmeta.getPrimaryUniqueInfo().isTwoOrMore()) {
            String msg = "The PagingSelectAndQuerySplit needs only-one column key table: " + getTableDbName();
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
        _pagingSelectAndQuerySplit = false;
    }

    // -----------------------------------------------------
    //                                         Fetch Setting
    //                                         -------------
    /**
     * {@inheritDoc}
     */
    public PagingBean fetchFirst(int fetchSize) {
        getSqlClause().fetchFirst(fetchSize);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public PagingBean fetchScope(int fetchStartIndex, int fetchSize) {
        getSqlClause().fetchScope(fetchStartIndex, fetchSize);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public PagingBean fetchPage(int fetchPageNumber) {
        getSqlClause().fetchPage(fetchPageNumber);
        return this;
    }

    // -----------------------------------------------------
    //                                       Paging Resource
    //                                       ---------------
    /**
     * {@inheritDoc}
     */
    public <ENTITY> PagingInvoker<ENTITY> createPagingInvoker(String tableDbName) {
        return new PagingInvoker<ENTITY>(tableDbName);
    }

    // -----------------------------------------------------
    //                                        Fetch Property
    //                                        --------------
    /**
     * {@inheritDoc}
     */
    public int getFetchStartIndex() {
        return getSqlClause().getFetchStartIndex();
    }

    /**
     * {@inheritDoc}
     */
    public int getFetchSize() {
        return getSqlClause().getFetchSize();
    }

    /**
     * {@inheritDoc}
     */
    public int getFetchPageNumber() {
        return getSqlClause().getFetchPageNumber();
    }

    /**
     * {@inheritDoc}
     */
    public int getPageStartIndex() {
        return getSqlClause().getPageStartIndex();
    }

    /**
     * {@inheritDoc}
     */
    public int getPageEndIndex() {
        return getSqlClause().getPageEndIndex();
    }

    /**
     * {@inheritDoc}
     */
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
    /**
     * {@inheritDoc}
     */
    public void checkSafetyResult(int safetyMaxResultSize) {
        _safetyMaxResultSize = safetyMaxResultSize;
    }

    /**
     * {@inheritDoc}
     */
    public int getSafetyMaxResultSize() {
        return _safetyMaxResultSize;
    }

    // ===================================================================================
    //                                                Implementation of FetchNarrowingBean
    //                                                ====================================
    /**
     * {@inheritDoc}
     */
    public int getFetchNarrowingSkipStartIndex() {
        return getSqlClause().getFetchNarrowingSkipStartIndex();
    }

    /**
     * {@inheritDoc}
     */
    public int getFetchNarrowingLoopCount() {
        return getSqlClause().getFetchNarrowingLoopCount();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isFetchNarrowingSkipStartIndexEffective() {
        return !getSqlClause().isFetchStartIndexSupported();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isFetchNarrowingLoopCountEffective() {
        return !getSqlClause().isFetchSizeSupported();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isFetchNarrowingEffective() {
        return getSqlClause().isFetchNarrowingEffective();
    }

    /**
     * {@inheritDoc}
     */
    public void xdisableFetchNarrowing() {
        // no need to disable in ConditionBean, basically for OutsideSql
        String msg = "This method is unsupported on ConditionBean!";
        throw new UnsupportedOperationException(msg);
    }

    /**
     * {@inheritDoc}
     */
    public void xenableIgnoredFetchNarrowing() {
        // do nothing
    }

    // ===================================================================================
    //                                                       Implementation of OrderByBean
    //                                                       =============================
    /**
     * {@inheritDoc}
     */
    public String getOrderByClause() {
        return _sqlClause.getOrderByClause();
    }

    /**
     * {@inheritDoc}
     */
    public OrderByClause getOrderByComponent() {
        return getSqlClause().getOrderByComponent();
    }

    /**
     * {@inheritDoc}
     */
    public OrderByBean clearOrderBy() {
        getSqlClause().clearOrderBy();
        return this;
    }

    // ===================================================================================
    //                                                                        Lock Setting
    //                                                                        ============
    /**
     * {@inheritDoc}
     */
    public ConditionBean lockForUpdate() {
        getSqlClause().lockForUpdate();
        return this;
    }

    // ===================================================================================
    //                                                                        Select Count
    //                                                                        ============
    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    public ConditionBean xafterCareSelectCountIgnoreFetchScope() {
        _isSelectCountIgnoreFetchScope = false;

        getSqlClause().rollbackSelectClauseType();
        getSqlClause().reviveOrderBy();
        getSqlClause().reviveFetchScope();
        return this;
    }

    /** Is set up various things for select-count-ignore-fetch-scope? */
    protected boolean _isSelectCountIgnoreFetchScope;

    /**
     * {@inheritDoc}
     */
    public boolean isSelectCountIgnoreFetchScope() {
        return _isSelectCountIgnoreFetchScope;
    }

    // ===================================================================================
    //                                                                       Cursor Select
    //                                                                       =============
    /**
     * {@inheritDoc}
     */
    public CursorSelectOption getCursorSelectOption() {
        return _cursorSelectOption;
    }

    // ===================================================================================
    //                                                                       Scalar Select
    //                                                                       =============
    /**
     * {@inheritDoc}
     */
    public void xacceptScalarSelectOption(ScalarSelectOption option) {
        getSqlClause().acceptScalarSelectOption(option);
    }

    // ===================================================================================
    //                                                                        Query Update
    //                                                                        ============
    /**
     * {@inheritDoc}
     */
    public void enableQueryUpdateCountPreCheck() {
        _queryUpdateCountPreCheck = true;
    }

    /**
     * {@inheritDoc}
     */
    public void disableQueryUpdateCountPreCheck() {
        _queryUpdateCountPreCheck = false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isQueryUpdateCountPreCheck() {
        return _queryUpdateCountPreCheck;
    }

    // ===================================================================================
    //                                                                     StatementConfig
    //                                                                     ===============
    /**
     * {@inheritDoc}
     */
    public void configure(StatementConfig statementConfig) {
        _statementConfig = statementConfig;
    }

    /**
     * {@inheritDoc}
     */
    public StatementConfig getStatementConfig() {
        return _statementConfig;
    }

    // ===================================================================================
    //                                                                      Entity Mapping
    //                                                                      ==============
    /**
     * Disable (entity instance) cache of relation mapping. <br />
     * Basically you don't need this. This is for accidents.
     * @deprecated You should not use this easily. It's a dangerous function.
     */
    public void disableRelationMappingCache() {
        // deprecated methods from the beginning are not defined as interface methods
        _relationMappingCache = false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean canRelationMappingCache() {
        return _relationMappingCache;
    }

    // ===================================================================================
    //                                                                     Embed Condition
    //                                                                     ===============
    /**
     * Embed conditions in their variables on where clause (and 'on' clause). <br />
     * You should not use this normally. It's a final weapon! <br />
     * And that this method is not perfect so be attention! <br />
     * If the same-name-columns exist in your conditions, both are embedded. <br />
     * And an empty set means that all conditions are target.
     * @param embeddedColumnInfoSet The set of embedded target column information. (NotNull)
     * @param quote Should the conditions value be quoted?
     * @deprecated You should not use this easily. It's a dangerous function.
     */
    public void embedCondition(Set<ColumnInfo> embeddedColumnInfoSet, boolean quote) {
        // deprecated methods from the beginning are not defined as interface methods
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
        this._sqlClause.addWhereClauseSimpleFilter(whereClauseSimpleFilter);
    }

    // ===================================================================================
    //                                                                          DisplaySQL
    //                                                                          ==========
    /**
     * {@inheritDoc}
     */
    public String toDisplaySql() {
        final SqlAnalyzerFactory factory = getSqlAnalyzerFactory();
        final String dateFormat = getLogDateFormat();
        final String timestampFormat = getLogTimestampFormat();
        return ConditionBeanContext.convertConditionBean2DisplaySql(factory, this, dateFormat, timestampFormat);
    }

    protected abstract SqlAnalyzerFactory getSqlAnalyzerFactory();

    protected abstract String getLogDateFormat();

    protected abstract String getLogTimestampFormat();

    // [DBFlute-0.9.5.2]
    // ===================================================================================
    //                                                                       Meta Handling
    //                                                                       =============
    /**
     * {@inheritDoc}
     */
    public boolean hasWhereClauseOnBaseQuery() {
        return getSqlClause().hasWhereClauseOnBaseQuery();
    }

    /**
     * {@inheritDoc}
     */
    public void clearWhereClauseOnBaseQuery() {
        getSqlClause().clearWhereClauseOnBaseQuery();
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    public boolean hasOrderByClause() {
        return getSqlClause().hasOrderByClause();
    }

    // ===================================================================================
    //                                                                 Reflection Invoking
    //                                                                 ===================
    /**
     * {@inheritDoc}
     */
    public void invokeSetupSelect(String foreignPropertyNamePath) {
        assertStringNotNullAndNotTrimmedEmpty("foreignPropertyNamePath", foreignPropertyNamePath);
        final String delimiter = ".";
        Object currentObj = this;
        String remainder = foreignPropertyNamePath;
        int count = 0;
        boolean last = false;
        while (true) {
            final int deimiterIndex = remainder.indexOf(delimiter);
            final String propertyName;
            if (deimiterIndex < 0) {
                propertyName = remainder;
                last = true;
            } else {
                propertyName = remainder.substring(0, deimiterIndex);
                remainder = remainder.substring(deimiterIndex + delimiter.length(), remainder.length());
            }
            final Class<?> targetType = currentObj.getClass();
            final String methodName = (count == 0 ? "setupSelect_" : "with") + initCap(propertyName);
            final Method method = xhelpGettingCBChainMethod(targetType, methodName, (Class<?>[]) null);
            if (method == null) {
                String msg = "Not found the method for setupSelect:";
                msg = msg + " foreignPropertyNamePath=" + foreignPropertyNamePath;
                msg = msg + " targetType=" + targetType + " methodName=" + methodName;
                throw new ConditionInvokingFailureException(msg);
            }
            try {
                currentObj = DfReflectionUtil.invoke(method, currentObj, (Object[]) null);
            } catch (ReflectionFailureException e) {
                String msg = "Failed to invoke the method:";
                msg = msg + " foreignPropertyNamePath=" + foreignPropertyNamePath;
                msg = msg + " targetType=" + targetType + " methodName=" + methodName;
                throw new ConditionInvokingFailureException(msg, e);
            }
            ++count;
            if (last) {
                break;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public HpSpecifiedColumn invokeSpecifyColumn(String columnNamePath) {
        final String delimiter = ".";
        Object currentObj = localSp();
        String remainder = columnNamePath;
        boolean last = false;
        while (true) {
            final int deimiterIndex = remainder.indexOf(delimiter);
            final String propertyName;
            if (deimiterIndex < 0) {
                propertyName = remainder;
                last = true;
            } else {
                propertyName = remainder.substring(0, deimiterIndex);
                remainder = remainder.substring(deimiterIndex + delimiter.length(), remainder.length());
            }
            final Class<?> targetType = currentObj.getClass();
            final String methodName = (last ? "column" : "specify") + initCap(propertyName);
            final Method method = xhelpGettingCBChainMethod(targetType, methodName, (Class<?>[]) null);
            if (method == null) {
                String msg = "Not found the method for SpecifyColumn:";
                msg = msg + " columnNamePath=" + columnNamePath;
                msg = msg + " targetType=" + targetType + " methodName=" + methodName;
                throw new ConditionInvokingFailureException(msg);
            }
            try {
                currentObj = DfReflectionUtil.invoke(method, currentObj, (Object[]) null);
            } catch (ReflectionFailureException e) {
                String msg = "Failed to invoke the method:";
                msg = msg + " columnNamePath=" + columnNamePath;
                msg = msg + " targetType=" + targetType + " methodName=" + methodName;
                throw new ConditionInvokingFailureException(msg, e);
            }
            if (last) {
                break;
            }
        }
        return (HpSpecifiedColumn) currentObj;
    }

    /**
     * {@inheritDoc}
     */
    public void invokeOrScopeQuery(OrQuery<ConditionBean> orQuery) {
        xorSQ(this, orQuery);
    }

    /**
     * {@inheritDoc}
     */
    public void invokeOrScopeQueryAndPart(AndQuery<ConditionBean> andQuery) {
        xorSQAP(this, andQuery);
    }

    protected Method xhelpGettingCBChainMethod(Class<?> type, String methodName, Class<?>[] argTypes) {
        final DfBeanDesc beanDesc = DfBeanDescFactory.getBeanDesc(type);
        return beanDesc.getMethodNoException(methodName, argTypes);
    }

    protected Object xhelpInvokingCBChainMethod(Class<?> type, Method method, Object[] args) {
        return DfReflectionUtil.invokeForcedly(method, type, args);
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

    /**
     * {@inheritDoc}
     */
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
    /**
     * {@inheritDoc}
     */
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

    // [DBFlute-0.7.4]
    // ===================================================================================
    //                                                                        Purpose Type
    //                                                                        ============
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

    public void xsetupForVaryingUpdate() {
        xchangePurposeSqlClause(HpCBPurpose.VARYING_UPDATE, null);
        xprepareSyncQyCall(null); // for suppressing query check
    }

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
        if (mainCQ.xgetSqlClause().isEmptyStringQueryEnabled()) {
            enableEmptyStringQuery();
        } else {
            disableEmptyStringQuery();
        }
        if (mainCQ.xgetSqlClause().isOverridingQueryEnabled()) {
            enableOverridingQuery();
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

    /**
     * {@inheritDoc}
     */
    public void enableThatsBadTiming() {
        getSqlClause().enableThatsBadTimingDetect();
    }

    /**
     * {@inheritDoc}
     */
    public void disableThatsBadTiming() {
        getSqlClause().disableThatsBadTimingDetect();
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
     * @param variableName Variable name. (NotNull)
     * @param value Value. (NotNull)
     * @exception IllegalArgumentException
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
     * @param variableName Variable name. (NotNull)
     * @param value Value. (NotNull)
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
        return DBFluteSystem.getBasicLn();
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
