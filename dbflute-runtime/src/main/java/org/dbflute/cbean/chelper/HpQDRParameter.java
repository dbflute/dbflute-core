/*
 * Copyright 2014-2020 the original author or authors.
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

import java.sql.Timestamp;
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
import org.dbflute.helper.message.ExceptionMessageBuilder;
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
        assertRangeOfNumberBothNullOrOneSideAllowed(minNumber, maxNumber, option);
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
    public void fromTo(LocalDate fromDate, LocalDate toDate, ConditionOptionCall<FromToOption> opLambda) { // #date_parade
        final FromToOption option = createFromToOption(opLambda);
        if (option.isUsePattern()) {
            // to avoid mismatch type of date-time and date, e.g. ...DATE_TIME <= '2015-06-30'
            // however ... really want to check specified column's type
            doFromTo(toTimestamp(fromDate), toTimestamp(toDate), option);
        } else {
            doFromTo(toDate(fromDate), toDate(toDate), option);
        }
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
        doFromTo(toTimestamp(fromDate), toTimestamp(toDate), createFromToOption(opLambda));
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
        assertFromToDateBothNullOrOneSideAllowed(fromDate, toDate, option);
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

    protected Timestamp toTimestamp(Object obj) {
        return DfTypeUtil.toTimestamp(obj, getFromToConversionTimeZone());
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

    // -----------------------------------------------------
    //                                               RangeOf
    //                                               -------
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
        if (option.isLessThan()) {
            throwRangeOfUnsupportedOptionException(minNumber, maxNumber, option, "lessThan");
        }
        if (option.isGreaterThan()) {
            throwRangeOfUnsupportedOptionException(minNumber, maxNumber, option, "greaterThan");
        }
        if (option.isOrIsNull()) {
            throwRangeOfUnsupportedOptionException(minNumber, maxNumber, option, "greaterThan");
        }
        if (option.hasCalculationRange()) {
            throwRangeOfUnsupportedOptionException(minNumber, maxNumber, option, "calculation");
        }
    }

    protected void throwRangeOfUnsupportedOptionException(Number minNumber, Number maxNumber, RangeOfOption option, String keyword) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Unsupported option of range-of option.");
        br.addItem("Advice");
        br.addElement("Cannot use the option '" + keyword + "'");
        br.addElement(" of the range-of for (Query)DerivedReferrer.");
        br.addItem("Max/Min Number");
        br.addElement(minNumber + " / " + maxNumber);
        br.addItem("RangeOfOption");
        br.addElement(option);
        final String msg = br.buildExceptionMessage();
        throw new IllegalConditionBeanOperationException(msg);
    }

    protected void assertRangeOfNumberBothNullOrOneSideAllowed(Number minNumber, Number maxNumber, RangeOfOption option) {
        final boolean oneSideAllowed = option.isOneSideAllowed();
        if (minNumber == null && maxNumber == null) {
            throwRangeOfNumberBothNullException(option);
        } else if (minNumber == null && !oneSideAllowed) {
            throwRangeOfMinNumberOnlyNullNotAllowedException(maxNumber, option);
        } else if (maxNumber == null && !oneSideAllowed) {
            throwRangeOfMaxNumberOnlyNullNotAllowedException(minNumber, option);
        }
    }

    protected void throwRangeOfNumberBothNullException(RangeOfOption option) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The both arguments of from-to for (Query)DerivedReferrer were null.");
        br.addItem("Advice");
        br.addElement("Basically it cannot allow double null");
        br.addElement("of the range-of method, even if allowOneSide().");
        br.addItem("FromToOption");
        br.addElement(option);
        final String msg = br.buildExceptionMessage();
        throw new IllegalConditionBeanOperationException(msg);
    }

    protected void throwRangeOfMinNumberOnlyNullNotAllowedException(Number maxNumber, RangeOfOption option) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The min-number of range-of for (Query)DerivedReferrer were null.");
        br.addItem("Advice");
        br.addElement("Basically it cannot allow min-mumber to be null.");
        br.addElement("If you need to specify null, use allowOneSide() option.");
        br.addItem("maxNumber");
        br.addElement(maxNumber);
        br.addItem("RangeOfOption");
        br.addElement(option);
        final String msg = br.buildExceptionMessage();
        throw new IllegalConditionBeanOperationException(msg);
    }

    protected void throwRangeOfMaxNumberOnlyNullNotAllowedException(Number minNumber, RangeOfOption option) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The max-mumber of range-of for (Query)DerivedReferrer were null.");
        br.addItem("Advice");
        br.addElement("Basically it cannot allow max-mumber to be null.");
        br.addElement("If you need to specify null, use allowOneSide() option.");
        br.addItem("minNumber");
        br.addElement(minNumber);
        br.addItem("RangeOfOption");
        br.addElement(option);
        final String msg = br.buildExceptionMessage();
        throw new IllegalConditionBeanOperationException(msg);
    }

    // -----------------------------------------------------
    //                                                FromTo
    //                                                ------
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
        // allow to use e.g. compareAsDate() using DateBetweenWay of from-to option
        //if (option.isUsePattern()) {
        //    String msg = "Cannot use the pattern option .e.g compareAsDate() of the from-to for (Query)DerivedReferrer:";
        //    msg = msg + " from=" + fromDate + ", to=" + toDate + ", option=" + option;
        //    throw new IllegalConditionBeanOperationException(msg);
        //}
        if (!option.isUsePattern() && option.isLessThan()) {
            throwFromToUnsupportedOptionException(fromDate, toDate, option, "lessThan");
        }
        if (!option.isUsePattern() && option.isGreaterThan()) {
            throwFromToUnsupportedOptionException(fromDate, toDate, option, "greaterThan");
        }
        if (option.isOrIsNull()) {
            throwFromToUnsupportedOptionException(fromDate, toDate, option, "osIsNull");
        }
    }

    protected void throwFromToUnsupportedOptionException(Date fromDate, Date toDate, FromToOption option, String keyword) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Unsupported option of from-to option.");
        br.addItem("Advice");
        br.addElement("Cannot use the option '" + keyword + "'");
        br.addElement(" of the from-to for (Query)DerivedReferrer.");
        br.addItem("From/To Date");
        br.addElement(fromDate + " / " + toDate);
        br.addItem("FromToOption");
        br.addElement(option);
        final String msg = br.buildExceptionMessage();
        throw new IllegalConditionBeanOperationException(msg);
    }

    protected void assertFromToDateBothNullOrOneSideAllowed(Date fromDate, Date toDate, FromToOption option) {
        final boolean oneSideAllowed = option.isOneSideAllowed();
        if (fromDate == null && toDate == null) {
            throwFromToDateBothNullException(option);
        } else if (fromDate == null && !oneSideAllowed) {
            throwFromToFromDateOnlyNullNotAllowedException(toDate, option);
        } else if (toDate == null && !oneSideAllowed) {
            throwFromToToDateOnlyNullNotAllowedException(fromDate, option);
        }
    }

    protected void throwFromToDateBothNullException(FromToOption option) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The both arguments of from-to for (Query)DerivedReferrer were null.");
        br.addItem("Advice");
        br.addElement("Basically it cannot allow double null");
        br.addElement("of the from-to method, even if allowOneSide().");
        br.addItem("FromToOption");
        br.addElement(option);
        final String msg = br.buildExceptionMessage();
        throw new IllegalConditionBeanOperationException(msg);
    }

    protected void throwFromToFromDateOnlyNullNotAllowedException(Date toDate, FromToOption option) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The from-date of from-to for (Query)DerivedReferrer were null.");
        br.addItem("Advice");
        br.addElement("Basically it cannot allow from-date to be null.");
        br.addElement("If you need to specify null, use allowOneSide() option.");
        br.addItem("toDate");
        br.addElement(toDate);
        br.addItem("FromToOption");
        br.addElement(option);
        final String msg = br.buildExceptionMessage();
        throw new IllegalConditionBeanOperationException(msg);
    }

    protected void throwFromToToDateOnlyNullNotAllowedException(Date fromDate, FromToOption option) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The to-date of from-to for (Query)DerivedReferrer were null.");
        br.addItem("Advice");
        br.addElement("Basically it cannot allow to-date to be null.");
        br.addElement("If you need to specify null, use allowOneSide() option.");
        br.addItem("fromDate");
        br.addElement(fromDate);
        br.addItem("FromToOption");
        br.addElement(option);
        final String msg = br.buildExceptionMessage();
        throw new IllegalConditionBeanOperationException(msg);
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
