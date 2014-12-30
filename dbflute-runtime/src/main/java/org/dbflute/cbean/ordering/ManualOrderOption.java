/*
 * Copyright 2014-2015 the original author or authors.
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
package org.dbflute.cbean.ordering;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.dbflute.cbean.ConditionBean;
import org.dbflute.cbean.chelper.HpCalcSpecification;
import org.dbflute.cbean.chelper.HpManualOrderThemeListHandler;
import org.dbflute.cbean.chelper.HpMobCaseWhenElement;
import org.dbflute.cbean.chelper.HpMobConnectedBean;
import org.dbflute.cbean.chelper.HpMobConnectionMode;
import org.dbflute.cbean.ckey.ConditionKey;
import org.dbflute.cbean.coption.ColumnConversionOption;
import org.dbflute.cbean.coption.ConditionOptionCall;
import org.dbflute.cbean.coption.FromToOption;
import org.dbflute.cbean.coption.FunctionFilterOptionCall;
import org.dbflute.cbean.dream.ColumnCalculator;
import org.dbflute.cbean.dream.SpecifiedColumn;
import org.dbflute.cbean.scoping.SpecifyQuery;
import org.dbflute.dbmeta.info.ColumnInfo;
import org.dbflute.exception.IllegalConditionBeanOperationException;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.jdbc.Classification;
import org.dbflute.jdbc.ClassificationCodeType;
import org.dbflute.system.DBFluteSystem;
import org.dbflute.twowaysql.DisplaySqlBuilder;
import org.dbflute.util.DfTypeUtil;

/**
 * The option for manual order.
 * <pre>
 * MemberCB cb = new MemberCB();
 * ManualOrderOption mob = new ManualOrderOption();
 * mob.<span style="color: #CC4747">when_GreaterEqual</span>(priorityDate); <span style="color: #3F7E5E">// e.g. 2000/01/01</span>
 * cb.query().addOrderBy_Birthdate_Asc().<span style="color: #CC4747">withManualOrder(mob)</span>;
 * <span style="color: #3F7E5E">// order by </span>
 * <span style="color: #3F7E5E">//   case</span>
 * <span style="color: #3F7E5E">//     when BIRTHDATE &gt;= '2000/01/01' then 0</span>
 * <span style="color: #3F7E5E">//     else 1</span>
 * <span style="color: #3F7E5E">//   end asc, ...</span>
 *
 * MemberCB cb = new MemberCB();
 * ManualOrderOption mob = new ManualOrderOption();
 * mob.<span style="color: #CC4747">when_Equal</span>(CDef.MemberStatus.Withdrawal);
 * mob.<span style="color: #CC4747">when_Equal</span>(CDef.MemberStatus.Formalized);
 * mob.<span style="color: #CC4747">when_Equal</span>(CDef.MemberStatus.Provisional);
 * cb.query().addOrderBy_MemberStatusCode_Asc().<span style="color: #CC4747">withManualOrder(mob)</span>;
 * <span style="color: #3F7E5E">// order by </span>
 * <span style="color: #3F7E5E">//   case</span>
 * <span style="color: #3F7E5E">//     when MEMBER_STATUS_CODE = 'WDL' then 0</span>
 * <span style="color: #3F7E5E">//     when MEMBER_STATUS_CODE = 'FML' then 1</span>
 * <span style="color: #3F7E5E">//     when MEMBER_STATUS_CODE = 'PRV' then 2</span>
 * <span style="color: #3F7E5E">//     else 3</span>
 * <span style="color: #3F7E5E">//   end asc, ...</span>
 * </pre>
 * <p>This function with Union is unsupported!</p>
 * <p>The order values are bound (treated as bind parameter).</p>
 * @author jflute
 * @since 0.9.8.2 (2011/04/08 Friday)
 */
public class ManualOrderOption implements ColumnCalculator {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String THEME_KEY = "ManualOrder";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final List<HpMobCaseWhenElement> _caseWhenAcceptedList = new ArrayList<HpMobCaseWhenElement>();
    protected final List<HpMobCaseWhenElement> _caseWhenBoundList = new ArrayList<HpMobCaseWhenElement>();
    protected HpCalcSpecification<ConditionBean> _calcSpecification;
    protected HpMobConnectionMode _connectionMode; // null means no connection

    // basically for switch order
    protected Object _elseAcceptedValue;
    protected Object _elseBoundValue;

    // for DBMS that doesn't support binding there
    protected boolean _suppressThenBinding;
    protected boolean _suppressElseBinding;

    // ===================================================================================
    //                                                                           Case When
    //                                                                           =========
    // -----------------------------------------------------
    //                                              when_...
    //                                              --------
    /**
     * Add 'when' element for 'case' statement as Equal(=).
     * @param orderValue The value for ordering. (NullAllowed: if null, means invalid condition)
     * @return The bean for connected order, which you can set second or more conditions by. (NotNull)
     */
    public HpMobConnectedBean when_Equal(Object orderValue) {
        return doWhen(ConditionKey.CK_EQUAL, orderValue);
    }

    /**
     * Add 'when' element for 'case' statement as NotEqual(&lt;&gt;).
     * @param orderValue The value for ordering. (NullAllowed: if null, means invalid condition)
     * @return The bean for connected order, which you can set second or more conditions by. (NotNull)
     */
    public HpMobConnectedBean when_NotEqual(Object orderValue) {
        return doWhen(ConditionKey.CK_NOT_EQUAL_STANDARD, orderValue);
    }

    /**
     * Add 'when' element for 'case' statement as GreaterThan(&gt;).
     * @param orderValue The value for ordering. (NullAllowed: if null, means invalid condition)
     * @return The bean for connected order, which you can set second or more conditions by. (NotNull)
     */
    public HpMobConnectedBean when_GreaterThan(Object orderValue) {
        return doWhen(ConditionKey.CK_GREATER_THAN, orderValue);
    }

    /**
     * Add 'when' element for 'case' statement as LessThan(&lt;).
     * @param orderValue The value for ordering. (NullAllowed: if null, means invalid condition)
     * @return The bean for connected order, which you can set second or more conditions by. (NotNull)
     */
    public HpMobConnectedBean when_LessThan(Object orderValue) {
        return doWhen(ConditionKey.CK_LESS_THAN, orderValue);
    }

    /**
     * Add 'when' element for 'case' statement as GreaterEqual(&gt;=).
     * @param orderValue The value for ordering. (NullAllowed: if null, means invalid condition)
     * @return The bean for connected order, which you can set second or more conditions by. (NotNull)
     */
    public HpMobConnectedBean when_GreaterEqual(Object orderValue) {
        return doWhen(ConditionKey.CK_GREATER_EQUAL, orderValue);
    }

    /**
     * Add 'when' element for 'case' statement as LessEqual(&lt;=).
     * @param orderValue The value for ordering. (NullAllowed: if null, means invalid condition)
     * @return The bean for connected order, which you can set second or more conditions by. (NotNull)
     */
    public HpMobConnectedBean when_LessEqual(Object orderValue) {
        return doWhen(ConditionKey.CK_LESS_EQUAL, orderValue);
    }

    /**
     * Add 'when' element for 'case' statement as IsNull.
     * @return The bean for connected order, which you can set second or more conditions by. (NotNull)
     */
    public HpMobConnectedBean when_IsNull() {
        return doWhen(ConditionKey.CK_IS_NULL, null);
    }

    /**
     * Add 'when' element for 'case' statement as IsNotNull.
     * @return The bean for connected order, which you can set second or more conditions by. (NotNull)
     */
    public HpMobConnectedBean when_IsNotNull() {
        return doWhen(ConditionKey.CK_IS_NOT_NULL, null);
    }

    /**
     * Add 'when' element for 'case' statement as FromTo using local date. <br>
     * You can set various from-to patterns by the from-to option. <br>
     * compareAsDate(), compareAsMonth(), compareAsYear(), and so on... <br>
     * See the {@link FromToOption} class for the details.
     * @param fromDate The local date as from-date. (basically NotNull: null allowed if one-side allowed)
     * @param toDate The local date as to-date. (basically NotNull: null allowed if one-side allowed)
     * @param opLambda The callback for option of from-to. (NotNull)
     * @return The bean for connected order, which you can set second or more conditions by. (NotNull)
     */
    public HpMobConnectedBean when_FromTo(LocalDate fromDate, LocalDate toDate, ConditionOptionCall<FromToOption> opLambda) { // #dateParade
        return doWhen_FromTo(toDate(fromDate), toDate(toDate), createFromToOption(opLambda));
    }

    /**
     * Add 'when' element for 'case' statement as FromTo using local date-time. <br>
     * You can set various from-to patterns by the from-to option. <br>
     * compareAsDate(), compareAsMonth(), compareAsYear(), and so on... <br>
     * See the {@link FromToOption} class for the details.
     * @param fromDate The local date-time as from-date. (basically NotNull: null allowed if one-side allowed)
     * @param toDate The local date-time as to-date. (basically NotNull: null allowed if one-side allowed)
     * @param opLambda The callback for option of from-to. (NotNull)
     * @return The bean for connected order, which you can set second or more conditions by. (NotNull)
     */
    public HpMobConnectedBean when_FromTo(LocalDateTime fromDate, LocalDateTime toDate, ConditionOptionCall<FromToOption> opLambda) {
        return doWhen_FromTo(toDate(fromDate), toDate(toDate), createFromToOption(opLambda));
    }

    /**
     * Add 'when' element for 'case' statement as FromTo. <br>
     * You can set various from-to patterns by the from-to option. <br>
     * compareAsDate(), compareAsMonth(), compareAsYear(), and so on... <br>
     * See the {@link FromToOption} class for the details.
     * @param fromDate The date as from-date. (basically NotNull: null allowed if one-side allowed)
     * @param toDate The date as to-date. (basically NotNull: null allowed if one-side allowed)
     * @param opLambda The callback for option of from-to. (NotNull)
     * @return The bean for connected order, which you can set second or more conditions by. (NotNull)
     */
    public HpMobConnectedBean when_FromTo(Date fromDate, Date toDate, ConditionOptionCall<FromToOption> opLambda) {
        return doWhen_FromTo(fromDate, toDate, createFromToOption(opLambda));
    }

    protected FromToOption createFromToOption(ConditionOptionCall<FromToOption> opLambda) {
        assertFromToOptionCall(opLambda);
        final FromToOption op = newFromToOption();
        opLambda.callback(op);
        return op;
    }

    protected void assertFromToOptionCall(ConditionOptionCall<FromToOption> opLambda) {
        if (opLambda == null) {
            String msg = "The argument 'opLambda' for from-to option of ManualOrder should not be null.";
            throw new IllegalConditionBeanOperationException(msg);
        }
    }

    protected FromToOption newFromToOption() {
        return new FromToOption();
    }

    protected HpMobConnectedBean doWhen_FromTo(Date fromDate, Date toDate, FromToOption option) {
        assertFromToOption(option);
        assertFromToDateBothExistsOrOneSideAllowed(fromDate, toDate, option);
        final ConditionKey fromDateConditionKey = option.getFromDateConditionKey();
        final ConditionKey toDateConditionKey = option.getToDateConditionKey();
        final Date filteredFromDate = option.filterFromDate(fromDate);
        final Date filteredToDate = option.filterToDate(toDate);
        return doWhen(fromDateConditionKey, filteredFromDate).doAnd(toDateConditionKey, filteredToDate);
    }

    protected void assertFromToOption(FromToOption option) {
        if (option == null) {
            String msg = "The argument 'option' for from-to should not be null.";
            throw new IllegalConditionBeanOperationException(msg);
        }
    }

    protected void assertFromToDateBothExistsOrOneSideAllowed(Date fromDate, Date toDate, FromToOption option) {
        final boolean oneSideAllowed = option.isOneSideAllowed();
        if (fromDate == null && toDate == null) {
            String msg = "The both arguments of from-to for ManualOrder were null: " + option;
            throw new IllegalConditionBeanOperationException(msg);
        } else if (fromDate == null && !oneSideAllowed) {
            String msg = "The argument 'fromDate' of from-to for ManualOrder was null:";
            msg = msg + " toDate=" + toDate + " option=" + option;
            throw new IllegalConditionBeanOperationException(msg);
        } else if (toDate == null && !oneSideAllowed) {
            String msg = "The argument 'toDate' of from-to for ManualOrder was null:";
            msg = msg + " fromDate=" + fromDate + " option=" + option;
            throw new IllegalConditionBeanOperationException(msg);
        }
    }

    // -----------------------------------------------------
    //                                           Order Value
    //                                           -----------
    /**
     * Accept the list of order value as equal condition.
     * <pre>
     * List&lt;CDef.MemberStatus&gt; orderValueList = new ArrayList&lt;CDef.MemberStatus&gt;();
     * orderValueList.add(CDef.MemberStatus.Withdrawal);
     * orderValueList.add(CDef.MemberStatus.Formalized);
     * orderValueList.add(CDef.MemberStatus.Provisional);
     * cb.query().addOrderBy_MemberStatusCode_Asc().<span style="color: #CC4747">withManualOrder</span>(op -&gt; {
     *     op.<span style="color: #CC4747">acceptOrderValueList</span>(orderValueList);
     * });
     * <span style="color: #3F7E5E">// order by </span>
     * <span style="color: #3F7E5E">//   case</span>
     * <span style="color: #3F7E5E">//     when MEMBER_STATUS_CODE = 'WDL' then 0</span>
     * <span style="color: #3F7E5E">//     when MEMBER_STATUS_CODE = 'FML' then 1</span>
     * <span style="color: #3F7E5E">//     when MEMBER_STATUS_CODE = 'PRV' then 2</span>
     * <span style="color: #3F7E5E">//     else 3</span>
     * <span style="color: #3F7E5E">//   end asc, ...</span>
     * </pre>
     * @param orderValueList The list of order value. (NotNull)
     */
    public void acceptOrderValueList(List<? extends Object> orderValueList) {
        if (orderValueList == null) {
            String msg = "The argument 'orderValueList' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        for (Object orderValue : orderValueList) {
            when_Equal(orderValue);
        }
    }

    // -----------------------------------------------------
    //                                         Assist Helper
    //                                         -------------
    protected HpMobConnectedBean doWhen(ConditionKey conditionKey, Object orderValue) {
        if (orderValue == null && !isManualOrderConditionKeyNullHandling(conditionKey)) {
            String msg = "The argument 'orderValue' should not be null: conditionKey=" + conditionKey;
            throw new IllegalArgumentException(msg);
        }
        final HpMobCaseWhenElement addedElement = createElement(conditionKey, orderValue);
        if (_connectionMode != null) {
            if (_caseWhenAcceptedList.isEmpty()) {
                throwManualOrderPreviousConditionNotFoundException(_connectionMode, conditionKey, orderValue);
            }
            addedElement.setConnectionMode(_connectionMode);
            final HpMobCaseWhenElement lastElement = getAcceptedLastElement();
            final List<HpMobCaseWhenElement> connectedElementList = lastElement.getConnectedElementList();
            if (!connectedElementList.isEmpty()) { // check same connectors
                final HpMobCaseWhenElement previousConnected = connectedElementList.get(connectedElementList.size() - 1);
                final HpMobConnectionMode previousMode = previousConnected.getConnectionMode();
                if (previousMode != null && !previousMode.equals(addedElement.getConnectionMode())) {
                    throwManualOrderTwoConnectorUnsupportedException(conditionKey, orderValue, lastElement);
                }
            }
            lastElement.addConnectedElement(addedElement);
        } else {
            _caseWhenAcceptedList.add(addedElement);
        }
        return createConnectedBean();
    }

    protected boolean isManualOrderConditionKeyNullHandling(ConditionKey conditionKey) {
        return conditionKey.equals(ConditionKey.CK_IS_NULL) || conditionKey.equals(ConditionKey.CK_IS_NOT_NULL);
    }

    protected HpMobCaseWhenElement getAcceptedLastElement() {
        return _caseWhenAcceptedList.get(_caseWhenAcceptedList.size() - 1);
    }

    protected HpMobConnectedBean createConnectedBean() {
        return new HpMobConnectedBean(this);
    }

    protected void throwManualOrderPreviousConditionNotFoundException(HpMobConnectionMode mode, ConditionKey conditionKey, Object orderValue) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found previous condition of 'case when' for connecting next condition.");
        br.addItem("Advice");
        br.addElement("You should set first condition before setting next condition.");
        br.addItem("Connection Mode");
        br.addElement(mode);
        br.addItem("Added ConnectionKey");
        br.addElement(conditionKey);
        br.addItem("Added OrderValue");
        br.addElement(orderValue);
        final String msg = br.buildExceptionMessage();
        throw new IllegalConditionBeanOperationException(msg);
    }

    protected void throwManualOrderTwoConnectorUnsupportedException(ConditionKey conditionKey, Object orderValue,
            HpMobCaseWhenElement lastElement) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("both two connectors and/or were set.");
        br.addItem("Advice");
        br.addElement("Unsupported using both two connectors and/or in one case.");
        br.addElement("For example:");
        br.addElement("  (o): when FOO > 1 and FOO < 9 then ...");
        br.addElement("  (o): when FOO >= 1 or FOO >= 9 then ...");
        br.addElement("  (x): when FOO >= 1 and FOO >= 9 or FOO = 20 then ...");
        br.addItem("Added ConditionKey");
        br.addElement(conditionKey);
        br.addItem("Added OrderValue");
        br.addElement(orderValue);
        br.addItem("Fixed ConnectionMode");
        br.addElement(lastElement.getConnectionMode());
        final String msg = br.buildExceptionMessage();
        throw new IllegalConditionBeanOperationException(msg);
    }

    // ===================================================================================
    //                                                                           Then/Else
    //                                                                           =========
    public void xregisterThenValueToLastElement(Object thenValue) {
        if (thenValue == null) {
            String msg = "The argument 'thenValue' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        if (_caseWhenAcceptedList.isEmpty()) {
            throwManualOrderThenValueCaseWhenElementNotFoundException(thenValue);
        }
        final HpMobCaseWhenElement lastElement = getAcceptedLastElement();
        lastElement.setThenValue(thenValue);
    }

    protected void throwManualOrderThenValueCaseWhenElementNotFoundException(Object thenValue) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found 'case when' element for 'then' value.");
        br.addItem("Advice");
        br.addElement("You should set 'case when' element before setting 'then' value.");
        br.addItem("Added ThenValue");
        br.addElement(thenValue);
        final String msg = br.buildExceptionMessage();
        throw new IllegalConditionBeanOperationException(msg);
    }

    /**
     * Add 'else' value. (Basically for SwitchOrder) <br>
     * You should set 'then' values before calling this.
     * @param elseValue The value for 'else', String, Integer, Date, DreamCruiseTicket... (NotNull)
     */
    public void elseEnd(Object elseValue) { // cannot be 'else()' for reservation word
        if (elseValue == null) {
            String msg = "The argument 'elseValue' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        if (_caseWhenAcceptedList.isEmpty()) {
            throwManualOrderElseValueCaseWhenElementNotFoundException(elseValue);
        }
        _elseAcceptedValue = elseValue;
    }

    protected void throwManualOrderElseValueCaseWhenElementNotFoundException(Object elseValue) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found 'case when' element for 'else' value.");
        br.addItem("Advice");
        br.addElement("You should set 'case when' element before setting 'else' value.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    ManualOrderOption mob = new ManualOrderOption();");
        br.addElement("    mob.elseEnd(0); // *NG");
        br.addElement("  (o):");
        br.addElement("    ManualOrderOption mob = new ManualOrderOption();");
        br.addElement("    mob.when_Equal(...); // *Don't forget here");
        br.addElement("    mob.elseEnd(0); // OK");
        br.addItem("Added ThenValue");
        br.addElement(elseValue);
        final String msg = br.buildExceptionMessage();
        throw new IllegalConditionBeanOperationException(msg);
    }

    // ===================================================================================
    //                                                                         Calculation
    //                                                                         ===========
    /**
     * {@inheritDoc}
     */
    public ColumnCalculator plus(Number plusValue) {
        assertObjectNotNull("plusValue", plusValue);
        initializeCalcSpecificationIfNeeds();
        return _calcSpecification.plus(plusValue);
    }

    /**
     * {@inheritDoc}
     */
    public ColumnCalculator plus(SpecifiedColumn plusColumn) {
        assertObjectNotNull("plusColumn", plusColumn);
        assertCalculationColumnNumber(plusColumn);
        assertSpecifiedDreamCruiseTicket(plusColumn);
        initializeCalcSpecificationIfNeeds();
        return _calcSpecification.plus(plusColumn);
    }

    /**
     * {@inheritDoc}
     */
    public ColumnCalculator minus(Number minusValue) {
        assertObjectNotNull("minusValue", minusValue);
        initializeCalcSpecificationIfNeeds();
        return _calcSpecification.minus(minusValue);
    }

    /**
     * {@inheritDoc}
     */
    public ColumnCalculator minus(SpecifiedColumn minusColumn) {
        assertObjectNotNull("minusColumn", minusColumn);
        assertCalculationColumnNumber(minusColumn);
        assertSpecifiedDreamCruiseTicket(minusColumn);
        initializeCalcSpecificationIfNeeds();
        return _calcSpecification.minus(minusColumn);
    }

    /**
     * {@inheritDoc}
     */
    public ColumnCalculator multiply(Number multiplyValue) {
        assertObjectNotNull("multiplyValue", multiplyValue);
        initializeCalcSpecificationIfNeeds();
        return _calcSpecification.multiply(multiplyValue);
    }

    /**
     * {@inheritDoc}
     */
    public ColumnCalculator multiply(SpecifiedColumn multiplyColumn) {
        assertObjectNotNull("multiplyColumn", multiplyColumn);
        assertCalculationColumnNumber(multiplyColumn);
        assertSpecifiedDreamCruiseTicket(multiplyColumn);
        initializeCalcSpecificationIfNeeds();
        return _calcSpecification.multiply(multiplyColumn);
    }

    /**
     * {@inheritDoc}
     */
    public ColumnCalculator divide(Number divideValue) {
        assertObjectNotNull("divideValue", divideValue);
        initializeCalcSpecificationIfNeeds();
        return _calcSpecification.divide(divideValue);
    }

    /**
     * {@inheritDoc}
     */
    public ColumnCalculator divide(SpecifiedColumn divideColumn) {
        assertObjectNotNull("divideColumn", divideColumn);
        assertCalculationColumnNumber(divideColumn);
        assertSpecifiedDreamCruiseTicket(divideColumn);
        initializeCalcSpecificationIfNeeds();
        return _calcSpecification.divide(divideColumn);
    }

    /**
     * {@inheritDoc}
     */
    public ColumnCalculator convert(FunctionFilterOptionCall<ColumnConversionOption> opLambda) {
        assertObjectNotNull("opLambda", opLambda);
        initializeCalcSpecificationIfNeeds();
        return _calcSpecification.convert(opLambda);
    }

    /**
     * {@inheritDoc}
     */
    public ColumnCalculator left() {
        initializeCalcSpecificationIfNeeds();
        return _calcSpecification.left();
    }

    /**
     * {@inheritDoc}
     */
    public ColumnCalculator right() {
        initializeCalcSpecificationIfNeeds();
        return _calcSpecification.right();
    }

    protected void initializeCalcSpecificationIfNeeds() {
        if (_calcSpecification == null) {
            _calcSpecification = createEmptyCalcSpecification();
        }
    }

    protected HpCalcSpecification<ConditionBean> createEmptyCalcSpecification() {
        final SpecifyQuery<ConditionBean> emptySpecifyQuery = createEmptySpecifyQuery();
        final HpCalcSpecification<ConditionBean> spec = newCalcSpecification(emptySpecifyQuery);
        spec.synchronizeSetupSelectByJourneyLogBook();
        return spec;
    }

    protected SpecifyQuery<ConditionBean> createEmptySpecifyQuery() {
        return new SpecifyQuery<ConditionBean>() {
            public void specify(ConditionBean cb) {
            }
        };
    }

    protected HpCalcSpecification<ConditionBean> newCalcSpecification(SpecifyQuery<ConditionBean> emptySpecifyQuery) {
        return new HpCalcSpecification<ConditionBean>(emptySpecifyQuery);
    }

    public boolean hasOrderByCalculation() {
        return _calcSpecification != null;
    }

    public HpCalcSpecification<ConditionBean> getOrderByCalculation() {
        return _calcSpecification;
    }

    public void xinitOrderByCalculation(ConditionBean baseCB, ConditionBean dreamCruiseCB) {
        if (!dreamCruiseCB.xisDreamCruiseShip()) {
            String msg = "The CB was not dream cruise: " + dreamCruiseCB.getClass();
            throw new IllegalConditionBeanOperationException(msg);
        }
        _calcSpecification.setBaseCB(baseCB);
        _calcSpecification.specify(dreamCruiseCB);
    }

    // ===================================================================================
    //                                                                     Connected Order
    //                                                                     ===============
    public void toBeConnectionModeAsAnd() {
        _connectionMode = HpMobConnectionMode.AND;
    }

    public void toBeConnectionModeAsOr() {
        _connectionMode = HpMobConnectionMode.OR;
    }

    public void clearConnectionMode() {
        _connectionMode = null;
    }

    // ===================================================================================
    //                                                                    CaseWhen Element 
    //                                                                    ================
    protected HpMobCaseWhenElement createElement(ConditionKey conditionKey, Object orderValue) {
        return new HpMobCaseWhenElement(conditionKey, orderValue);
    }

    // ===================================================================================
    //                                                                     Binding Process
    //                                                                     ===============
    /**
     * Bind parameters for manual order. <br>
     * It is called from DBFlute runtime internally.
     * @param handler The handler for free parameters. (NotNull)
     */
    public void bind(HpManualOrderThemeListHandler handler) { // called when set to query
        if (!hasManualOrder()) {
            return;
        }
        for (HpMobCaseWhenElement topElement : _caseWhenAcceptedList) {
            final HpMobCaseWhenElement boundTopElement = doBindCaseWhen(handler, topElement);
            final List<HpMobCaseWhenElement> connectedList = topElement.getConnectedElementList();
            for (HpMobCaseWhenElement connectedElement : connectedList) {
                final HpMobCaseWhenElement boundConnectedElement = doBindCaseWhen(handler, connectedElement);
                boundTopElement.addConnectedElement(boundConnectedElement);
            }
            _caseWhenBoundList.add(boundTopElement);
        }
        doBindElseEnd(handler);
    }

    protected HpMobCaseWhenElement doBindCaseWhen(HpManualOrderThemeListHandler handler, HpMobCaseWhenElement element) {
        final ConditionKey conditionKey = element.getConditionKey();
        final Object orderValue = resolveBoundValue(handler, element.getOrderValue(), false);
        final HpMobCaseWhenElement boundElement = createElement(conditionKey, orderValue);
        boundElement.setConnectionMode(element.getConnectionMode());
        boundElement.setThenValue(resolveBoundValue(handler, element.getThenValue(), _suppressThenBinding));
        return boundElement;
    }

    protected void doBindElseEnd(HpManualOrderThemeListHandler handler) {
        if (_elseAcceptedValue != null) {
            _elseBoundValue = resolveBoundValue(handler, _elseAcceptedValue, _suppressElseBinding);
        }
    }

    // -----------------------------------------------------
    //                                         Resolve Bound
    //                                         -------------
    protected Object resolveBoundValue(HpManualOrderThemeListHandler handler, Object plainValue, boolean suppressBinding) {
        if (plainValue == null) {
            return null;
        }
        if (plainValue instanceof SpecifiedColumn) {
            return resolveDreamCruiseExp(plainValue);
        }
        ClassificationCodeType codeType = null;
        if (plainValue instanceof Classification) {
            final Classification cls = (Classification) plainValue;
            plainValue = handleClassificationOrderValue(cls);
            codeType = cls.meta().codeType();
        }
        final Object boundExp;
        if (suppressBinding) {
            if (plainValue instanceof String) {
                if (canBeLiteralClassificationCodeType(codeType)) {
                    boundExp = plainValue;
                } else {
                    String notice = "The binding of string value is unsupported on the DBMS.";
                    throwUnsupportedTypeSpecifiedException(notice, plainValue);
                    boundExp = null; // unreachable
                }
            } else if (plainValue instanceof Number) {
                boundExp = buildLiteralNumberExpression(plainValue);
            } else if (isAnyLocalDate(plainValue) || plainValue instanceof Date) { // #dateParade
                boundExp = buildLiteralDateExpression(plainValue);
            } else {
                String notice = "The binding of the type is unsupported on the DBMS.";
                throwUnsupportedTypeSpecifiedException(notice, plainValue);
                boundExp = null; // unreachable
            }
        } else {
            boundExp = handler.register(THEME_KEY, plainValue);
        }
        return boundExp;
    }

    protected Object resolveDreamCruiseExp(Object plainValue) {
        final SpecifiedColumn specifiedColumn = (SpecifiedColumn) plainValue;
        final String columnExp = specifiedColumn.toColumnRealName().toString();
        if (specifiedColumn.hasSpecifyCalculation()) {
            specifiedColumn.xinitSpecifyCalculation();
            final HpCalcSpecification<ConditionBean> calcSpecification = specifiedColumn.getSpecifyCalculation();
            return calcSpecification.buildStatementToSpecifidName(columnExp);
        }
        return columnExp;
    }

    // -----------------------------------------------------
    //                                        Classification
    //                                        --------------
    protected Object handleClassificationOrderValue(Classification cls) {
        final Object orderValue;
        final String plainCode = cls.code();
        final ClassificationCodeType codeType = cls.meta().codeType();
        if (ClassificationCodeType.Number.equals(codeType)) {
            if ("true".equalsIgnoreCase(plainCode) || "false".equalsIgnoreCase(plainCode)) {
                // true or false of Number, e.g. MySQL's Boolean
                orderValue = toClassificationBooleanValue(plainCode);
            } else {
                orderValue = toClassificationIntegerValue(plainCode);
            }
        } else if (ClassificationCodeType.Boolean.equals(codeType)) {
            orderValue = toClassificationBooleanValue(plainCode);
        } else {
            orderValue = plainCode;
        }
        return orderValue;
    }

    protected Integer toClassificationIntegerValue(String plainCode) {
        return (Integer) DfTypeUtil.toNumber(plainCode, Integer.class);
    }

    protected Boolean toClassificationBooleanValue(String plainCode) {
        return DfTypeUtil.toBoolean(plainCode);
    }

    protected boolean canBeLiteralClassificationCodeType(ClassificationCodeType codeType) {
        return codeType != null && codeType.equals(ClassificationCodeType.Number) && codeType.equals(ClassificationCodeType.Boolean);
    }

    // -----------------------------------------------------
    //                                       Number Handling
    //                                       ---------------
    protected String buildLiteralNumberExpression(Object plainValue) {
        return plainValue.toString();
    }

    // -----------------------------------------------------
    //                                         Date Handling
    //                                         -------------
    protected boolean isAnyLocalDate(Object plainValue) {
        return DfTypeUtil.isAnyLocalDate(plainValue);
    }

    protected String buildLiteralDateExpression(Object plainValue) {
        if (plainValue instanceof LocalDate) { // local date cannot use time-part so check it
            final String pattern = DisplaySqlBuilder.DEFAULT_DATE_FORMAT;
            return doBuildLiteralDateExpression(DfTypeUtil.toStringDate((LocalDate) plainValue, pattern));
        } else if (plainValue instanceof LocalDateTime) {
            final String pattern = DisplaySqlBuilder.DEFAULT_TIMESTAMP_FORMAT;
            return doBuildLiteralDateExpression(DfTypeUtil.toStringDate((LocalDateTime) plainValue, pattern));
        } else if (plainValue instanceof LocalTime) {
            final String pattern = DisplaySqlBuilder.DEFAULT_TIME_FORMAT;
            return doBuildLiteralDateExpression(DfTypeUtil.toStringDate((LocalTime) plainValue, pattern));
        } else { // instance of util.Date, as time-stamp fixedly (since 1.0.x)
            final String pattern = DisplaySqlBuilder.DEFAULT_TIMESTAMP_FORMAT;
            return doBuildLiteralDateExpression(DfTypeUtil.toString(plainValue, pattern));
        }
    }

    protected String doBuildLiteralDateExpression(String formatted) {
        return "'" + formatted + "'";
    }

    // -----------------------------------------------------
    //                                    Exception Handling
    //                                    ------------------
    protected void throwUnsupportedTypeSpecifiedException(String notice, Object plainValue) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice(notice);
        br.addItem("Advice");
        br.addElement("The binding for the part, 'when' or 'then' or 'else',");
        br.addElement("is unsupported with the value type.");
        br.addItem("Specified Value");
        br.addElement(plainValue.getClass());
        br.addElement(plainValue);
        final String msg = br.buildExceptionMessage();
        throw new IllegalConditionBeanOperationException(msg);
    }

    // -----------------------------------------------------
    //                                     Then/Else Binding
    //                                     -----------------
    public ManualOrderOption suppressThenBinding() {
        _suppressThenBinding = true;
        return this;
    }

    public ManualOrderOption suppressElseBinding() {
        _suppressElseBinding = true;
        return this;
    }

    // ===================================================================================
    //                                                                          Validation
    //                                                                          ==========
    /**
     * Validate case-when constraints. <br>
     * It is called from DBFlute runtime internally.
     */
    public void validate() { // called when set to query
        doValidateCaseWhenConstraint();
    }

    protected void doValidateCaseWhenConstraint() {
        if (_caseWhenAcceptedList.isEmpty()) {
            return;
        }
        final HpMobCaseWhenElement first = _caseWhenAcceptedList.get(0);
        final boolean firstThenExists = first.getThenValue() != null;
        for (HpMobCaseWhenElement current : _caseWhenAcceptedList) {
            final boolean currentThenExists = current.getThenValue() != null;
            if (firstThenExists && !currentThenExists) {
                throwManualOrderRequiredThenNotFoundException(current);
            } else if (!firstThenExists && currentThenExists) {
                throwManualOrderUnnecessaryThenFoundException(current);
            }
        }
        final boolean elseExists = _elseAcceptedValue != null;
        if (firstThenExists && !elseExists) { // e.g. SwitchOrder
            throwManualOrderRequiredElseNotFoundException();
        } else if (!firstThenExists && elseExists) { // e.g. PriorityOrder
            String msg = "Found unnecessary 'else', it doesn't need it if PriorityOrder: " + toString();
            throw new IllegalConditionBeanOperationException(msg);
        }
    }

    protected void throwManualOrderRequiredThenNotFoundException(HpMobCaseWhenElement current) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found 'then', all elements need it (if SwitchOrder).");
        br.addItem("Advice");
        br.addElement("You should set 'then' value to all case-when elements");
        br.addElement("if you want to use SwitchOrder.");
        br.addElement("(settings for 'then' value means SwitchOrder)");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    ManualOrderOption mob = new ManualOrderOption();");
        br.addElement("    mob.when_GreaterThan(7).then(...);");
        br.addElement("    mob.when_LessThan(3); // *NG");
        br.addElement("  (o):");
        br.addElement("    ManualOrderOption mob = new ManualOrderOption();");
        br.addElement("    mob.when_GreaterThan(7).then(...);");
        br.addElement("    mob.when_LessThan(3).then(...); // OK");
        br.addItem("Target Element");
        br.addElement(current);
        final String msg = br.buildExceptionMessage();
        throw new IllegalConditionBeanOperationException(msg);
    }

    protected void throwManualOrderUnnecessaryThenFoundException(HpMobCaseWhenElement current) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Found unnecessary 'then', all elements doesn't need it (if PriorityOrder).");
        br.addItem("Advice");
        br.addElement("You should NOT set 'then' value to all case-when elements");
        br.addElement("if you want to use PriorityOrder.");
        br.addElement("(No 'then' value means PriorityOrder)");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    ManualOrderOption mob = new ManualOrderOption();");
        br.addElement("    mob.when_GreaterThan(7);");
        br.addElement("    mob.when_LessThan(3).then(...); // *NG");
        br.addElement("  (o):");
        br.addElement("    ManualOrderOption mob = new ManualOrderOption();");
        br.addElement("    mob.when_GreaterThan(7);");
        br.addElement("    mob.when_LessThan(3); // OK");
        br.addItem("Target Element");
        br.addElement(current);
        final String msg = br.buildExceptionMessage();
        throw new IllegalConditionBeanOperationException(msg);
    }

    protected void throwManualOrderRequiredElseNotFoundException() {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found 'else', it needs it (if SwitchOrder).");
        br.addItem("Advice");
        br.addElement("You should set 'else' value if you want to use SwitchOrder.");
        br.addElement("(settings for 'then' value means SwitchOrder)");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    ManualOrderOption mob = new ManualOrderOption();");
        br.addElement("    mob.when_GreaterThan(7).then(...);");
        br.addElement("    mob.when_LessThan(3).then(...);");
        br.addElement("    cb.query().addOrderBy_...().withManualOrder(mob); // *NG");
        br.addElement("  (o):");
        br.addElement("    ManualOrderOption mob = new ManualOrderOption();");
        br.addElement("    mob.when_GreaterThan(7).then(...);");
        br.addElement("    mob.when_LessThan(3).then(...);");
        br.addElement("    mob.elseEnd(3); // OK");
        br.addElement("    cb.query().addOrderBy_...().withManualOrder(mob);");
        br.addItem("CaseWhen Element");
        for (HpMobCaseWhenElement element : _caseWhenAcceptedList) {
            br.addElement(element);
        }
        final String msg = br.buildExceptionMessage();
        throw new IllegalConditionBeanOperationException(msg);
    }

    protected void throwManualOrderUnnecessaryElseNotFoundException() {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Found unnecessary 'else', it doesn't need it (if PriorityOrder).");
        br.addItem("Advice");
        br.addElement("You should NOT set 'else' value if you want to use PriorityOrder.");
        br.addElement("(No 'then' value means PriorityOrder)");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    ManualOrderOption mob = new ManualOrderOption();");
        br.addElement("    mob.when_GreaterThan(7);");
        br.addElement("    mob.when_LessThan(3);");
        br.addElement("    mob.elseEnd(3); // *NG");
        br.addElement("  (o):");
        br.addElement("    ManualOrderOption mob = new ManualOrderOption();");
        br.addElement("    mob.when_GreaterThan(7);");
        br.addElement("    mob.when_LessThan(3);");
        br.addElement("    cb.query().addOrderBy_...().withManualOrder(mob); // OK");
        br.addItem("CaseWhen Element");
        for (HpMobCaseWhenElement element : _caseWhenAcceptedList) {
            br.addElement(element);
        }
        final String msg = br.buildExceptionMessage();
        throw new IllegalConditionBeanOperationException(msg);
    }

    // ===================================================================================
    //                                                                       Determination
    //                                                                       =============
    public boolean hasManualOrder() {
        return !_caseWhenAcceptedList.isEmpty() || _calcSpecification != null;
    }

    // ===================================================================================
    //                                                                       Assert Helper
    //                                                                       =============
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

    protected void assertCalculationColumnNumber(SpecifiedColumn specifiedColumn) {
        final ColumnInfo columnInfo = specifiedColumn.getColumnInfo();
        if (columnInfo == null) { // basically not null but just in case
            return;
        }
        if (!columnInfo.isObjectNativeTypeNumber()) {
            String msg = "The type of the calculation column should be Number: " + specifiedColumn;
            throw new IllegalArgumentException(msg);
        }
    }

    protected void assertSpecifiedDreamCruiseTicket(SpecifiedColumn column) {
        if (!column.isDreamCruiseTicket()) {
            final String msg = "The specified column was not dream cruise ticket: " + column;
            throw new IllegalConditionBeanOperationException(msg);
        }
    }

    // ===================================================================================
    //                                                                     Time Management
    //                                                                     ===============
    protected Date toDate(Object obj) {
        return DfTypeUtil.toDate(obj, getFromToConversionTimeZone());
    }

    protected TimeZone getFromToConversionTimeZone() {
        return getDBFluteSystemFinalTimeZone();
    }

    protected TimeZone getDBFluteSystemFinalTimeZone() {
        return DBFluteSystem.getFinalTimeZone();
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    // *instance match so does not override equals()
    @Override
    public String toString() {
        final String title = DfTypeUtil.toClassTitle(this);
        return title + ":{case-when=" + _caseWhenAcceptedList.size() + ", calc=" + _calcSpecification + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public List<HpMobCaseWhenElement> getCaseWhenAcceptedList() {
        return _caseWhenAcceptedList;
    }

    public List<HpMobCaseWhenElement> getCaseWhenBoundList() {
        return _caseWhenBoundList;
    }

    /**
     * Get the 'else' value, which is bound. <br>
     * It returns null if you set only 'else' value but not binding.
     * @return The value for 'else'. (NullAllowed)
     */
    public Object getElseValue() {
        return _elseBoundValue;
    }
}
