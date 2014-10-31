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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.dbflute.cbean.ConditionBean;
import org.dbflute.cbean.ckey.ConditionKey;
import org.dbflute.cbean.coption.ConditionOptionCall;
import org.dbflute.cbean.coption.DerivedReferrerOption;
import org.dbflute.cbean.coption.FromToOption;
import org.dbflute.cbean.coption.RangeOfOption;
import org.dbflute.cbean.scoping.SubQuery;
import org.dbflute.exception.IllegalConditionBeanOperationException;
import org.dbflute.system.DBFluteSystem;
import org.dbflute.util.DfTypeUtil;

/**
 * The parameter of (Query)DerivedReferrer.
 * @param <CB> The type of condition-bean.
 * @param <PARAMETER> The type of parameter.
 * @author jflute
 */
public class HpQDRParameter<CB extends ConditionBean, PARAMETER> {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _function;
    protected final SubQuery<CB> _subQuery;
    protected final DerivedReferrerOption _option;
    protected final HpQDRSetupper<CB> _setupper;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public HpQDRParameter(String function, SubQuery<CB> subQuery, DerivedReferrerOption option, HpQDRSetupper<CB> setupper) {
        _function = function;
        _subQuery = subQuery;
        _option = option;
        _setupper = setupper;
    }

    // ===================================================================================
    //                                                                           Condition
    //                                                                           =========
    /**
     * Set up the operand 'equal' and the value of parameter. <br>
     * The type of the parameter should be same as the type of target column. <br>
     * If the specified column is date type and has time-parts, you should use java.sql.Timestamp type.
     * <pre>
     * cb.query().derivedPurchaseList().max(new SubQuery&lt;PurchaseCB&gt;() {
     *     protected void query(PurchaseCB subCB) {
     *         subCB.specify().columnPurchasePrice(); <span style="color: #3F7E5E">// If the type is Integer...</span>
     *         subCB.query().setPaymentCompleteFlg_Equal_True();
     *     }
     * }).equal(123); <span style="color: #3F7E5E">// This parameter should be Integer!</span>
     * </pre>
     * @param value The value of parameter for the specified column. (NotNull)
     */
    public void equal(PARAMETER value) {
        assertParameterNotNull(value);
        _setupper.setup(_function, _subQuery, ConditionKey.CK_EQUAL.getOperand(), value, _option);
    }

    /**
     * Set up the operand 'notEqual' and the value of parameter. <br>
     * The type of the parameter should be same as the type of target column. <br>
     * If the specified column is date type and has time-parts, you should use java.sql.Timestamp type.
     * <pre>
     * cb.query().derivedPurchaseList().max(new SubQuery&lt;PurchaseCB&gt;() {
     *     protected void query(PurchaseCB subCB) {
     *         subCB.specify().columnPurchasePrice(); <span style="color: #3F7E5E">// If the type is Integer...</span>
     *         subCB.query().setPaymentCompleteFlg_Equal_True();
     *     }
     * }).notEqual(123); <span style="color: #3F7E5E">// This parameter should be Integer!</span>
     * </pre>
     * @param value The value of parameter for the specified column. (NotNull)
     */
    public void notEqual(PARAMETER value) {
        assertParameterNotNull(value);
        _setupper.setup(_function, _subQuery, ConditionKey.CK_NOT_EQUAL_STANDARD.getOperand(), value, _option);
    }

    /**
     * Set up the operand 'greaterThan' and the value of parameter. <br>
     * The type of the parameter should be same as the type of target column. <br>
     * If the specified column is date type and has time-parts, you should use java.sql.Timestamp type.
     * <pre>
     * cb.query().derivedPurchaseList().max(new SubQuery&lt;PurchaseCB&gt;() {
     *     protected void query(PurchaseCB subCB) {
     *         subCB.specify().columnPurchasePrice(); <span style="color: #3F7E5E">// If the type is Integer...</span>
     *         subCB.query().setPaymentCompleteFlg_Equal_True();
     *     }
     * }).greaterThan(123); <span style="color: #3F7E5E">// This parameter should be Integer!</span>
     * </pre>
     * @param value The value of parameter for the specified column. (NotNull)
     */
    public void greaterThan(PARAMETER value) {
        assertParameterNotNull(value);
        _setupper.setup(_function, _subQuery, ConditionKey.CK_GREATER_THAN.getOperand(), value, _option);
    }

    /**
     * Set up the operand 'lessThan' and the value of parameter. <br>
     * The type of the parameter should be same as the type of target column. <br>
     * If the specified column is date type and has time-parts, you should use java.sql.Timestamp type.
     * <pre>
     * cb.query().derivedPurchaseList().max(new SubQuery&lt;PurchaseCB&gt;() {
     *     protected void query(PurchaseCB subCB) {
     *         subCB.specify().columnPurchasePrice(); <span style="color: #3F7E5E">// If the type is Integer...</span>
     *         subCB.query().setPaymentCompleteFlg_Equal_True();
     *     }
     * }).lessThan(123); <span style="color: #3F7E5E">// This parameter should be Integer!</span>
     * </pre>
     * @param value The value of parameter for the specified column. (NotNull)
     */
    public void lessThan(PARAMETER value) {
        assertParameterNotNull(value);
        _setupper.setup(_function, _subQuery, ConditionKey.CK_LESS_THAN.getOperand(), value, _option);
    }

    /**
     * Set up the operand 'greaterEqual' and the value of parameter. <br>
     * The type of the parameter should be same as the type of target column. <br>
     * If the specified column is date type and has time-parts, you should use java.sql.Timestamp type.
     * <pre>
     * cb.query().derivedPurchaseList().max(new SubQuery&lt;PurchaseCB&gt;() {
     *     protected void query(PurchaseCB subCB) {
     *         subCB.specify().columnPurchasePrice(); <span style="color: #3F7E5E">// If the type is Integer...</span>
     *         subCB.query().setPaymentCompleteFlg_Equal_True();
     *     }
     * }).greaterEqual(123); <span style="color: #3F7E5E">// This parameter should be Integer!</span>
     * </pre>
     * @param value The value of parameter for the specified column. (NotNull)
     */
    public void greaterEqual(PARAMETER value) {
        assertParameterNotNull(value);
        _setupper.setup(_function, _subQuery, ConditionKey.CK_GREATER_EQUAL.getOperand(), value, _option);
    }

    /**
     * Set up the operand 'lessEqual' and the value of parameter. <br>
     * The type of the parameter should be same as the type of target column. <br>
     * If the specified column is date type and has time-parts, you should use java.sql.Timestamp type.
     * <pre>
     * cb.query().derivedPurchaseList().max(new SubQuery&lt;PurchaseCB&gt;() {
     *     protected void query(PurchaseCB subCB) {
     *         subCB.specify().columnPurchasePrice(); <span style="color: #3F7E5E">// If the type is Integer...</span>
     *         subCB.query().setPaymentCompleteFlg_Equal_True();
     *     }
     * }).lessEqual(123); <span style="color: #3F7E5E">// This parameter should be Integer!</span>
     * </pre>
     * @param value The value of parameter for the specified column. (NotNull)
     */
    public void lessEqual(PARAMETER value) {
        assertParameterNotNull(value);
        _setupper.setup(_function, _subQuery, ConditionKey.CK_LESS_EQUAL.getOperand(), value, _option);
    }

    /**
     * Set up the operand 'isNull' and the value of parameter. <br>
     * The type of the parameter should be same as the type of target column. 
     * <pre>
     * cb.query().derivedPurchaseList().max(new SubQuery&lt;PurchaseCB&gt;() {
     *     protected void query(PurchaseCB subCB) {
     *         subCB.specify().columnPurchasePrice();
     *         subCB.query().setPaymentCompleteFlg_Equal_True();
     *     }
     * }).isNull(); <span style="color: #3F7E5E">// no parameter</span>
     * </pre>
     */
    public void isNull() {
        _setupper.setup(_function, _subQuery, ConditionKey.CK_IS_NULL.getOperand(), null, _option);
    }

    /**
     * Set up the operand 'isNull' and the value of parameter. <br>
     * The type of the parameter should be same as the type of target column. 
     * <pre>
     * cb.query().derivedPurchaseList().max(new SubQuery&lt;PurchaseCB&gt;() {
     *     protected void query(PurchaseCB subCB) {
     *         subCB.specify().columnPurchasePrice();
     *         subCB.query().setPaymentCompleteFlg_Equal_True();
     *     }
     * }).isNotNull(); <span style="color: #3F7E5E">// no parameter</span>
     * </pre>
     */
    public void isNotNull() {
        _setupper.setup(_function, _subQuery, ConditionKey.CK_IS_NOT_NULL.getOperand(), null, _option);
    }

    // -----------------------------------------------------
    //                                               RangeOf
    //                                               -------
    /**
     * Set up the comparison 'RangeOf' and the values of parameter. <br>
     * The type of the parameter should be same as the type of target column. <br>
     * <pre>
     * cb.query().derivedPurchaseList().max(<span style="color: #553000">purchasCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">purchasCB</span>.specify().columnPurchasePrice();
     *     <span style="color: #553000">purchasCB</span>.query().setPaymentCompleteFlg_Equal_True();
     * }).<span style="color: #CC4747">rangeOf</span>(2000, 2999, <span style="color: #553000">op</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {});
     * <span style="color: #3F7E5E">// PURCHASE_PRICE between 2000 and 2999</span>
     * </pre>
     * <p>The option is allowed to be set only allowOneSide().
     * You cannot use greaterThan() and orIsNull() and so on...</p>
     * @param minNumber The minimum number of parameter for the specified column. (basically NotNull: null allowed if one-side allowed)
     * @param maxNumber The maximum number of parameter for the specified column. (basically NotNull: null allowed if one-side allowed)
     * @param opLambda The callback for option of range-of. (NotNull)
     */
    public void rangeOf(Number minNumber, Number maxNumber, ConditionOptionCall<RangeOfOption> opLambda) {
        doRangeOf(minNumber, maxNumber, createRangeOfOption(opLambda));
    }

    protected void doRangeOf(Number minNumber, Number maxNumber, RangeOfOption option) {
        assertRangeOfOption(option);
        assertRangeOfNotCalledUnsupported(minNumber, maxNumber, option);
        assertRangeOfNumberBothExistsOrOneSideAllowed(minNumber, maxNumber, option);
        @SuppressWarnings("unchecked")
        final PARAMETER fromValue = (PARAMETER) minNumber;
        @SuppressWarnings("unchecked")
        final PARAMETER toValue = (PARAMETER) maxNumber;
        dispatchFromTo(fromValue, toValue);
    }

    protected RangeOfOption createRangeOfOption(ConditionOptionCall<RangeOfOption> opLambda) {
        assertRangeOfOptionCall(opLambda);
        final RangeOfOption op = newRangeOfOption();
        opLambda.callback(op);
        return op;
    }

    protected RangeOfOption newRangeOfOption() {
        return new RangeOfOption();
    }

    // -----------------------------------------------------
    //                                                FromTo
    //                                                ------
    /**
     * Set up the comparison 'FromTo' and the values of parameter. <br>
     * The type of the parameter should be same as the type of target column.
     * <pre>
     * cb.query().<span style="color: #994747">derivedPurchaseList()</span>.max(<span style="color: #553000">purchasCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">purchasCB</span>.specify().columnPurchaseDatetime();
     *     <span style="color: #553000">purchasCB</span>.query().setPaymentCompleteFlg_Equal_True();
     * }).<span style="color: #CC4747">fromTo</span>(toLocalDate(<span style="color: #2A00FF">"2012/02/05"</span>), toLocalDate(<span style="color: #2A00FF">"2012/04/07"</span>), <span style="color: #553000">op</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> <span style="color: #553000">op</span>.<span style="color: #994747">compareAsMonth()</span>);
     * <span style="color: #3F7E5E">// PURCHASE_DATETIME between 2012/02/01 00:00:00 and 2012/04/30 23:59:59.999</span>
     * </pre>
     * @param fromDate The 'from' local date of parameter for the specified column. (basically NotNull: null allowed if one-side allowed) 
     * @param toDate The 'to' local date of parameter for the specified column. (basically NotNull: null allowed if one-side allowed) 
     * @param opLambda The callback for option of from-to. (NotNull)
     */
    public void fromTo(LocalDate fromDate, LocalDate toDate, ConditionOptionCall<FromToOption> opLambda) { // #dateParade
        doFromTo(toDate(fromDate), toDate(toDate), createFromToOption(opLambda));
    }

    /**
     * Set up the comparison 'FromTo' and the values of parameter. <br>
     * The type of the parameter should be same as the type of target column.
     * <pre>
     * cb.query().<span style="color: #994747">derivedPurchaseList()</span>.max(<span style="color: #553000">purchasCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">purchasCB</span>.specify().columnPurchaseDatetime();
     *     <span style="color: #553000">purchasCB</span>.query().setPaymentCompleteFlg_Equal_True();
     * }).<span style="color: #CC4747">fromTo</span>(toLocalDateTime(<span style="color: #2A00FF">"2012/02/05"</span>), toLocalDateTime(<span style="color: #2A00FF">"2012/04/07"</span>), <span style="color: #553000">op</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> <span style="color: #553000">op</span>.<span style="color: #994747">compareAsMonth()</span>);
     * <span style="color: #3F7E5E">// PURCHASE_DATETIME between 2012/02/01 00:00:00 and 2012/04/30 23:59:59.999</span>
     * </pre>
     * @param fromDate The 'from' local date-time of parameter for the specified column. (basically NotNull: null allowed if one-side allowed) 
     * @param toDate The 'to' local date-time of parameter for the specified column. (basically NotNull: null allowed if one-side allowed) 
     * @param opLambda The callback for option of from-to. (NotNull)
     */
    public void fromTo(LocalDateTime fromDate, LocalDateTime toDate, ConditionOptionCall<FromToOption> opLambda) {
        doFromTo(toDate(fromDate), toDate(toDate), createFromToOption(opLambda));
    }

    /**
     * Set up the comparison 'FromTo' and the values of parameter. <br>
     * The type of the parameter should be same as the type of target column. <br>
     * If the specified column is date type and has time-parts, you should use java.sql.Timestamp type.
     * <pre>
     * cb.query().<span style="color: #994747">derivedPurchaseList()</span>.max(<span style="color: #553000">purchasCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">purchasCB</span>.specify().columnPurchaseDatetime();
     *     <span style="color: #553000">purchasCB</span>.query().setPaymentCompleteFlg_Equal_True();
     * }).<span style="color: #CC4747">fromTo</span>(toTimestamp(<span style="color: #2A00FF">"2012/02/05"</span>), toTimestamp(<span style="color: #2A00FF">"2012/04/07"</span>), <span style="color: #553000">op</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> <span style="color: #553000">op</span>.<span style="color: #994747">compareAsMonth()</span>);
     * <span style="color: #3F7E5E">// PURCHASE_DATETIME between 2012/02/01 00:00:00 and 2012/04/30 23:59:59.999</span>
     * </pre>
     * @param fromDate The 'from' date of parameter for the specified column. (basically NotNull: null allowed if one-side allowed) 
     * @param toDate The 'to' date of parameter for the specified column. (basically NotNull: null allowed if one-side allowed) 
     * @param opLambda The callback for option of from-to. (NotNull)
     */
    public void fromTo(Date fromDate, Date toDate, ConditionOptionCall<FromToOption> opLambda) {
        doFromTo(fromDate, toDate, createFromToOption(opLambda));
    }

    protected FromToOption createFromToOption(ConditionOptionCall<FromToOption> opLambda) {
        assertFromToOptionCall(opLambda);
        final FromToOption op = newFromToOption();
        opLambda.callback(op);
        return op;
    }

    protected FromToOption newFromToOption() {
        return new FromToOption();
    }

    protected void doFromTo(Date fromDate, Date toDate, FromToOption option) {
        assertFromToOption(option);
        assertFromToNotCalledUnsupported(fromDate, toDate, option);
        assertFromToDateBothExistsOrOneSideAllowed(fromDate, toDate, option);
        if (fromDate != null) {
            fromDate = option.filterFromDate(fromDate);
        }
        if (toDate != null) {
            toDate = option.xfilterToDateBetweenWay(toDate);
        }
        @SuppressWarnings("unchecked")
        final PARAMETER fromValue = (PARAMETER) fromDate;
        @SuppressWarnings("unchecked")
        final PARAMETER toValue = (PARAMETER) toDate;
        dispatchFromTo(fromValue, toValue);
    }

    protected void dispatchFromTo(PARAMETER fromValue, PARAMETER toValue) { // shared with rangeOf()
        if (fromValue != null && toValue != null) {
            doBetween(fromValue, toValue);
        } else if (fromValue != null) {
            greaterEqual(fromValue);
        } else if (toValue != null) {
            lessEqual(toValue);
        }
    }

    protected void doBetween(PARAMETER fromValue, PARAMETER toValue) {
        assertParameterFromNotNull(fromValue);
        assertParameterToNotNull(toValue);
        final List<PARAMETER> fromToValueList = new ArrayList<PARAMETER>(2);
        fromToValueList.add(fromValue);
        fromToValueList.add(toValue);
        _setupper.setup(_function, _subQuery, getBetweenKeyword(), fromToValueList, _option);
    }

    protected String getBetweenKeyword() {
        return "between";
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
    //                                                                       Assert Helper
    //                                                                       =============
    protected void assertParameterNotNull(Object value) {
        if (value == null) {
            String msg = "The argument 'value' of parameter for DerivedReferrer should not be null.";
            throw new IllegalArgumentException(msg);
        }
    }

    protected void assertRangeOfOptionCall(ConditionOptionCall<RangeOfOption> opLambda) {
        if (opLambda == null) {
            String msg = "The argument 'opLambda' for range-of option of (Query)DerivedReferrer should not be null.";
            throw new IllegalConditionBeanOperationException(msg);
        }
    }

    protected void assertRangeOfOption(RangeOfOption option) {
        if (option == null) {
            String msg = "The argument 'option' of range-of for (Query)DerivedReferrer should not be null.";
            throw new IllegalConditionBeanOperationException(msg);
        }
    }

    protected void assertRangeOfNotCalledUnsupported(Number minNumber, Number maxNumber, RangeOfOption option) {
        if (option.isGreaterThan() || option.isLessThan() || option.isOrIsNull()) {
            String msg = "Cannot use the options of the range-of for (Query)DerivedReferrer:";
            msg = msg + " min=" + minNumber + ", max=" + maxNumber + ", option=" + option;
            throw new IllegalConditionBeanOperationException(msg);
        }
        if (option.hasCalculationRange()) {
            String msg = "Cannot use the calculation option of the range-of for (Query)DerivedReferrer:";
            msg = msg + " min=" + minNumber + ", max=" + maxNumber + ", option=" + option;
            throw new IllegalConditionBeanOperationException(msg);
        }
    }

    protected void assertRangeOfNumberBothExistsOrOneSideAllowed(Number minNumber, Number maxNumber, RangeOfOption option) {
        final boolean oneSideAllowed = option.isOneSideAllowed();
        if (minNumber == null && maxNumber == null) {
            String msg = "The both arguments of range-of for (Query)DerivedReferrer were null: " + option;
            throw new IllegalConditionBeanOperationException(msg);
        } else if (minNumber == null && !oneSideAllowed) {
            String msg = "The argument 'minNumber' of range-of for (Query)DerivedReferrer was null:";
            msg = msg + " maxNumber=" + maxNumber + " option=" + option;
            throw new IllegalConditionBeanOperationException(msg);
        } else if (maxNumber == null && !oneSideAllowed) {
            String msg = "The argument 'maxNumber' of range-of for (Query)DerivedReferrer was null:";
            msg = msg + " minNumber=" + minNumber + " option=" + option;
            throw new IllegalConditionBeanOperationException(msg);
        }
    }

    protected void assertFromToOptionCall(ConditionOptionCall<FromToOption> opLambda) {
        if (opLambda == null) {
            String msg = "The argument 'opLambda' for from-to option of (Query)DerivedReferrer should not be null.";
            throw new IllegalConditionBeanOperationException(msg);
        }
    }

    protected void assertFromToOption(FromToOption option) {
        if (option == null) {
            String msg = "The argument 'option' of from-to for (Query)DerivedReferrer should not be null.";
            throw new IllegalConditionBeanOperationException(msg);
        }
    }

    protected void assertFromToNotCalledUnsupported(Date fromDate, Date toDate, FromToOption option) {
        if (option.isGreaterThan() || option.isLessThan() || option.isOrIsNull()) {
            String msg = "Cannot use the options of the from-to for (Query)DerivedReferrer:";
            msg = msg + " from=" + fromDate + ", to=" + toDate + ", option=" + option;
            throw new IllegalConditionBeanOperationException(msg);
        }
        if (option.isUsePattern()) {
            String msg = "Cannot use the pattern option .e.g compareAsDate() of the from-to for (Query)DerivedReferrer:";
            msg = msg + " from=" + fromDate + ", to=" + toDate + ", option=" + option;
            throw new IllegalConditionBeanOperationException(msg);
        }
    }

    protected void assertFromToDateBothExistsOrOneSideAllowed(Date fromDate, Date toDate, FromToOption option) {
        final boolean oneSideAllowed = option.isOneSideAllowed();
        if (fromDate == null && toDate == null) {
            String msg = "The both arguments of from-to for (Query)DerivedReferrer were null: " + option;
            throw new IllegalConditionBeanOperationException(msg);
        } else if (fromDate == null && !oneSideAllowed) {
            String msg = "The argument 'fromDate' of from-to for (Query)DerivedReferrer was null:";
            msg = msg + " toDate=" + toDate + " option=" + option;
            throw new IllegalConditionBeanOperationException(msg);
        } else if (toDate == null && !oneSideAllowed) {
            String msg = "The argument 'toDate' of from-to for (Query)DerivedReferrer was null:";
            msg = msg + " fromDate=" + fromDate + " option=" + option;
            throw new IllegalConditionBeanOperationException(msg);
        }
    }

    protected void assertParameterFromNotNull(Object fromValue) {
        if (fromValue == null) {
            String msg = "The argument 'fromValue' of parameter for DerivedReferrer should not be null.";
            throw new IllegalArgumentException(msg);
        }
    }

    protected void assertParameterToNotNull(Object toValue) {
        if (toValue == null) {
            String msg = "The argument 'toValue' of parameter for DerivedReferrer should not be null.";
            throw new IllegalArgumentException(msg);
        }
    }

    // ===================================================================================
    //                                                                       Determination
    //                                                                       =============
    public static boolean isOperandIsNull(String operand) { // basically for auto-detect of inner-join
        return ConditionKey.CK_IS_NULL.getOperand().equals(operand);
    }
}
