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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.seasar.dbflute.cbean.chelper.HpCalcSpecification;
import org.seasar.dbflute.cbean.chelper.HpCalculator;
import org.seasar.dbflute.cbean.chelper.HpManualOrderThemeListHandler;
import org.seasar.dbflute.cbean.chelper.HpMobCaseWhenElement;
import org.seasar.dbflute.cbean.chelper.HpMobConnectedBean;
import org.seasar.dbflute.cbean.chelper.HpMobConnectionMode;
import org.seasar.dbflute.cbean.chelper.HpSpecifiedColumn;
import org.seasar.dbflute.cbean.ckey.ConditionKey;
import org.seasar.dbflute.cbean.coption.ColumnConversionOption;
import org.seasar.dbflute.cbean.coption.DateFromToOption;
import org.seasar.dbflute.cbean.coption.FromToOption;
import org.seasar.dbflute.dbmeta.info.ColumnInfo;
import org.seasar.dbflute.exception.IllegalConditionBeanOperationException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.jdbc.Classification;
import org.seasar.dbflute.jdbc.ClassificationCodeType;
import org.seasar.dbflute.util.DfTypeUtil;

/**
 * The bean for manual order.
 * <pre>
 * MemberCB cb = new MemberCB();
 * ManualOrderBean mob = new ManualOrderBean();
 * mob.<span style="color: #DD4747">when_GreaterEqual</span>(priorityDate); <span style="color: #3F7E5E">// e.g. 2000/01/01</span>
 * cb.query().addOrderBy_Birthdate_Asc().<span style="color: #DD4747">withManualOrder(mob)</span>;
 * <span style="color: #3F7E5E">// order by </span>
 * <span style="color: #3F7E5E">//   case</span>
 * <span style="color: #3F7E5E">//     when BIRTHDATE &gt;= '2000/01/01' then 0</span>
 * <span style="color: #3F7E5E">//     else 1</span>
 * <span style="color: #3F7E5E">//   end asc, ...</span>
 *
 * MemberCB cb = new MemberCB();
 * ManualOrderBean mob = new ManualOrderBean();
 * mob.<span style="color: #DD4747">when_Equal</span>(CDef.MemberStatus.Withdrawal);
 * mob.<span style="color: #DD4747">when_Equal</span>(CDef.MemberStatus.Formalized);
 * mob.<span style="color: #DD4747">when_Equal</span>(CDef.MemberStatus.Provisional);
 * cb.query().addOrderBy_MemberStatusCode_Asc().<span style="color: #DD4747">withManualOrder(mob)</span>;
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
public class ManualOrderBean implements HpCalculator {

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
     * Add 'when' element for 'case' statement as FromTo. <br />
     * You can set various from-to patterns by the from-to option. <br />
     * compareAsDate(), compareAsMonth(), compareAsYear(), and so on... <br />
     * See the {@link FromToOption} class for the details.
     * @param fromDate The from-date for ordering. (NullAllowed: if null, means invalid from-condition)
     * @param toDate The to-date for ordering. (NullAllowed: if null, means invalid to-condition)
     * @param option The option of from-to. (NotNull)
     * @return The bean for connected order, which you can set second or more conditions by. (NotNull)
     */
    public HpMobConnectedBean when_FromTo(Date fromDate, Date toDate, FromToOption option) {
        return doWhen_FromTo(fromDate, toDate, option);
    }

    /**
     * Add 'when' element for 'case' statement as DateFromTo.
     * <pre>
     * e.g. from:{<span style="color: #DD4747">2007/04/10</span> 08:24:53} to:{<span style="color: #DD4747">2007/04/16</span> 14:36:29}
     * 
     *   mob.when_DateFromTo(fromDate, toDate);
     *     --&gt; column &gt;= '2007/04/10 00:00:00'
     *     and column &lt; '2007/04/17 00:00:00'
     * </pre>
     * @param fromDate The from-date for ordering. (NullAllowed: if null, means invalid from-condition)
     * @param toDate The to-date for ordering. (NullAllowed: if null, means invalid to-condition)
     * @return The bean for connected order, which you can set second or more conditions by. (NotNull)
     */
    public HpMobConnectedBean when_DateFromTo(Date fromDate, Date toDate) {
        return doWhen_FromTo(fromDate, toDate, new DateFromToOption());
    }

    protected HpMobConnectedBean doWhen_FromTo(Date fromDate, Date toDate, FromToOption option) {
        if (option == null) {
            String msg = "The argument 'option' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        final ConditionKey fromDateConditionKey = option.getFromDateConditionKey();
        final ConditionKey toDateConditionKey = option.getToDateConditionKey();
        final Date filteredFromDate = option.filterFromDate(fromDate);
        final Date filteredToDate = option.filterToDate(toDate);
        return doWhen(fromDateConditionKey, filteredFromDate).doAnd(toDateConditionKey, filteredToDate);
    }

    // -----------------------------------------------------
    //                                           Order Value
    //                                           -----------
    /**
     * Accept the list of order value as equal condition.
     * <pre>
     * MemberCB cb = new MemberCB();
     * List&lt;CDef.MemberStatus&gt; orderValueList = new ArrayList&lt;CDef.MemberStatus&gt;();
     * orderValueList.add(CDef.MemberStatus.Withdrawal);
     * orderValueList.add(CDef.MemberStatus.Formalized);
     * orderValueList.add(CDef.MemberStatus.Provisional);
     * ManualOrderBean mob = new ManualOrderBean();
     * mob.<span style="color: #DD4747">acceptOrderValueList</span>(orderValueList);
     * cb.query().addOrderBy_MemberStatusCode_Asc().<span style="color: #DD4747">withManualOrder(mob)</span>;
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
                final HpMobCaseWhenElement previousConnected = connectedElementList
                        .get(connectedElementList.size() - 1);
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

    protected void throwManualOrderPreviousConditionNotFoundException(HpMobConnectionMode mode,
            ConditionKey conditionKey, Object orderValue) {
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
     * Add 'else' value. (Basically for SwitchOrder) <br />
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
        br.addElement("    ManualOrderBean mob = new ManualOrderBean();");
        br.addElement("    mob.elseEnd(0); // *NG");
        br.addElement("  (o):");
        br.addElement("    ManualOrderBean mob = new ManualOrderBean();");
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
    public HpCalculator plus(Number plusValue) {
        assertObjectNotNull("plusValue", plusValue);
        initializeCalcSpecificationIfNeeds();
        return _calcSpecification.plus(plusValue);
    }

    /**
     * {@inheritDoc}
     */
    public HpCalculator plus(HpSpecifiedColumn plusColumn) {
        assertObjectNotNull("plusColumn", plusColumn);
        assertCalculationColumnNumber(plusColumn);
        assertSpecifiedDreamCruiseTicket(plusColumn);
        initializeCalcSpecificationIfNeeds();
        return _calcSpecification.plus(plusColumn);
    }

    /**
     * {@inheritDoc}
     */
    public HpCalculator minus(Number minusValue) {
        assertObjectNotNull("minusValue", minusValue);
        initializeCalcSpecificationIfNeeds();
        return _calcSpecification.minus(minusValue);
    }

    /**
     * {@inheritDoc}
     */
    public HpCalculator minus(HpSpecifiedColumn minusColumn) {
        assertObjectNotNull("minusColumn", minusColumn);
        assertCalculationColumnNumber(minusColumn);
        assertSpecifiedDreamCruiseTicket(minusColumn);
        initializeCalcSpecificationIfNeeds();
        return _calcSpecification.minus(minusColumn);
    }

    /**
     * {@inheritDoc}
     */
    public HpCalculator multiply(Number multiplyValue) {
        assertObjectNotNull("multiplyValue", multiplyValue);
        initializeCalcSpecificationIfNeeds();
        return _calcSpecification.multiply(multiplyValue);
    }

    /**
     * {@inheritDoc}
     */
    public HpCalculator multiply(HpSpecifiedColumn multiplyColumn) {
        assertObjectNotNull("multiplyColumn", multiplyColumn);
        assertCalculationColumnNumber(multiplyColumn);
        assertSpecifiedDreamCruiseTicket(multiplyColumn);
        initializeCalcSpecificationIfNeeds();
        return _calcSpecification.multiply(multiplyColumn);
    }

    /**
     * {@inheritDoc}
     */
    public HpCalculator divide(Number divideValue) {
        assertObjectNotNull("divideValue", divideValue);
        initializeCalcSpecificationIfNeeds();
        return _calcSpecification.divide(divideValue);
    }

    /**
     * {@inheritDoc}
     */
    public HpCalculator divide(HpSpecifiedColumn divideColumn) {
        assertObjectNotNull("divideColumn", divideColumn);
        assertCalculationColumnNumber(divideColumn);
        assertSpecifiedDreamCruiseTicket(divideColumn);
        initializeCalcSpecificationIfNeeds();
        return _calcSpecification.divide(divideColumn);
    }

    /**
     * {@inheritDoc}
     */
    public HpCalculator convert(ColumnConversionOption option) {
        assertObjectNotNull("option", option);
        initializeCalcSpecificationIfNeeds();
        return _calcSpecification.convert(option);
    }

    /**
     * {@inheritDoc}
     */
    public HpCalculator left() {
        initializeCalcSpecificationIfNeeds();
        return _calcSpecification.left();
    }

    /**
     * {@inheritDoc}
     */
    public HpCalculator right() {
        initializeCalcSpecificationIfNeeds();
        return _calcSpecification.right();
    }

    protected void initializeCalcSpecificationIfNeeds() {
        if (_calcSpecification == null) {
            _calcSpecification = createCalcSpecification();
        }
    }

    protected HpCalcSpecification<ConditionBean> createCalcSpecification() {
        SpecifyQuery<ConditionBean> emptySpecifyQuery = createEmptySpecifyQuery();
        final HpCalcSpecification<ConditionBean> spec = new HpCalcSpecification<ConditionBean>(emptySpecifyQuery);
        spec.synchronizeSetupSelectByJourneyLogBook();
        return spec;
    }

    protected SpecifyQuery<ConditionBean> createEmptySpecifyQuery() {
        return new SpecifyQuery<ConditionBean>() {
            public void specify(ConditionBean cb) {
            }
        };
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
     * Bind parameters for manual order. <br />
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

    protected Object resolveBoundValue(HpManualOrderThemeListHandler handler, Object plainValue, boolean suppressBinding) {
        if (plainValue == null) {
            return null;
        }
        if (plainValue instanceof HpSpecifiedColumn) {
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
            } else if (plainValue instanceof Date) {
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
        final HpSpecifiedColumn specifiedColumn = (HpSpecifiedColumn) plainValue;
        final String columnExp = specifiedColumn.toColumnRealName().toString();
        if (specifiedColumn.hasSpecifyCalculation()) {
            specifiedColumn.xinitSpecifyCalculation();
            final HpCalcSpecification<ConditionBean> calcSpecification = specifiedColumn.getSpecifyCalculation();
            return calcSpecification.buildStatementToSpecifidName(columnExp);
        }
        return columnExp;
    }

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
        return codeType != null && codeType.equals(ClassificationCodeType.Number)
                && codeType.equals(ClassificationCodeType.Boolean);
    }

    protected String buildLiteralNumberExpression(Object plainValue) {
        return plainValue.toString();
    }

    protected String buildLiteralDateExpression(Object plainValue) {
        return "'" + DfTypeUtil.toString(plainValue, "yyyy-MM-dd HH:mm:ss.SSS") + "'";
    }

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

    public ManualOrderBean suppressThenBinding() {
        _suppressThenBinding = true;
        return this;
    }

    public ManualOrderBean suppressElseBinding() {
        _suppressElseBinding = true;
        return this;
    }

    // ===================================================================================
    //                                                                          Validation
    //                                                                          ==========
    /**
     * Validate case-when constraints. <br />
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
        br.addElement("    ManualOrderBean mob = new ManualOrderBean();");
        br.addElement("    mob.when_GreaterThan(7).then(...);");
        br.addElement("    mob.when_LessThan(3); // *NG");
        br.addElement("  (o):");
        br.addElement("    ManualOrderBean mob = new ManualOrderBean();");
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
        br.addElement("    ManualOrderBean mob = new ManualOrderBean();");
        br.addElement("    mob.when_GreaterThan(7);");
        br.addElement("    mob.when_LessThan(3).then(...); // *NG");
        br.addElement("  (o):");
        br.addElement("    ManualOrderBean mob = new ManualOrderBean();");
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
        br.addElement("    ManualOrderBean mob = new ManualOrderBean();");
        br.addElement("    mob.when_GreaterThan(7).then(...);");
        br.addElement("    mob.when_LessThan(3).then(...);");
        br.addElement("    cb.query().addOrderBy_...().withManualOrder(mob); // *NG");
        br.addElement("  (o):");
        br.addElement("    ManualOrderBean mob = new ManualOrderBean();");
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
        br.addElement("    ManualOrderBean mob = new ManualOrderBean();");
        br.addElement("    mob.when_GreaterThan(7);");
        br.addElement("    mob.when_LessThan(3);");
        br.addElement("    mob.elseEnd(3); // *NG");
        br.addElement("  (o):");
        br.addElement("    ManualOrderBean mob = new ManualOrderBean();");
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

    protected void assertCalculationColumnNumber(HpSpecifiedColumn specifiedColumn) {
        final ColumnInfo columnInfo = specifiedColumn.getColumnInfo();
        if (columnInfo == null) { // basically not null but just in case
            return;
        }
        if (!columnInfo.isObjectNativeTypeNumber()) {
            String msg = "The type of the calculation column should be Number: " + specifiedColumn;
            throw new IllegalArgumentException(msg);
        }
    }

    protected void assertSpecifiedDreamCruiseTicket(HpSpecifiedColumn column) {
        if (!column.isDreamCruiseTicket()) {
            final String msg = "The specified column was not dream cruise ticket: " + column;
            throw new IllegalConditionBeanOperationException(msg);
        }
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
     * Get the 'else' value, which is bound. <br />
     * It returns null if you set only 'else' value but not binding.
     * @return The value for 'else'. (NullAllowed)
     */
    public Object getElseValue() {
        return _elseBoundValue;
    }
}
