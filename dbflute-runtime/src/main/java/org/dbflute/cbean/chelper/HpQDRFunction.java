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
package org.dbflute.cbean.chelper;

import org.dbflute.cbean.ConditionBean;
import org.dbflute.cbean.coption.DerivedReferrerOption;
import org.dbflute.cbean.coption.DerivedReferrerOptionFactory;
import org.dbflute.cbean.coption.FunctionFilterOptionCall;
import org.dbflute.cbean.scoping.SubQuery;

/**
 * The function of (Query)DerivedReferrer.
 * @param <CB> The type of condition-bean.
 * @author jflute
 */
public class HpQDRFunction<CB extends ConditionBean> {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final HpQDRSetupper<CB> _setupper;
    protected final DerivedReferrerOptionFactory _derivedReferrerOptionFactory;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public HpQDRFunction(HpQDRSetupper<CB> setupper, DerivedReferrerOptionFactory derivedReferrerOptionFactory) {
        _setupper = setupper;
        _derivedReferrerOptionFactory = derivedReferrerOptionFactory;
    }

    // ===================================================================================
    //                                                                            Function
    //                                                                            ========
    /**
     * Set up the sub query of referrer for the scalar 'count'.
     * <pre>
     * cb.query().derivedPurchaseList().<span style="color: #CC4747">count</span>(<span style="color: #553000">purchaseCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">purchaseCB</span>.specify().<span style="color: #CC4747">columnPurchaseId</span>(); <span style="color: #3F7E5E">// *Point</span>
     *     <span style="color: #553000">purchaseCB</span>.query().setPaymentCompleteFlg_Equal_True();
     * }).<span style="color: #CC4747">greaterEqual</span>(123); <span style="color: #3F7E5E">// *Don't forget the parameter</span>
     * </pre> 
     * @param derivedCBLambda The callback for sub-query of referrer. (NotNull)
     * @return The parameter for comparing with scalar. (NotNull)
     */
    public HpQDRParameter<CB, Integer> count(SubQuery<CB> derivedCBLambda) {
        return doCount(derivedCBLambda, null);
    }

    /**
     * An overload method for count(). So refer to the method's java-doc about basic info.
     * <pre>
     * cb.query().derivedPurchaseList().<span style="color: #CC4747">count</span>(<span style="color: #553000">purchaseCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">purchaseCB</span>.specify().<span style="color: #994747">columnPurchasePrice</span>(); <span style="color: #3F7E5E">// *Point</span>
     *     <span style="color: #553000">purchaseCB</span>.query().setPaymentCompleteFlg_Equal_True();
     * }).<span style="color: #994747">greaterEqual</span>(123, <span style="color: #553000">op</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> <span style="color: #553000">op</span>.<span style="color: #CC4747">coalesce</span>(0));
     * </pre> 
     * @param derivedCBLambda The callback for sub-query of referrer. (NotNull)
     * @param opLambda The callback for option of DerivedReferrer. For example, you can use a coalesce function. (NotNull)
     * @return The parameter for comparing with scalar. (NotNull)
     */
    public HpQDRParameter<CB, Integer> count(SubQuery<CB> derivedCBLambda, FunctionFilterOptionCall<DerivedReferrerOption> opLambda) {
        assertDerivedReferrerOption(opLambda);
        final DerivedReferrerOption option = createDerivedReferrerOption();
        opLambda.callback(option);
        return doCount(derivedCBLambda, option);
    }

    protected HpQDRParameter<CB, Integer> doCount(SubQuery<CB> subQuery, DerivedReferrerOption option) {
        assertSubQuery(subQuery);
        return createQDRParameter("count", subQuery, option);
    }

    /**
     * Set up the sub query of referrer for the scalar 'count(with distinct)'.
     * <pre>
     * cb.query().derivedPurchaseList().<span style="color: #CC4747">countDistinct</span>(<span style="color: #553000">purchaseCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">purchaseCB</span>.specify().<span style="color: #CC4747">columnPurchasePrice</span>(); <span style="color: #3F7E5E">// *Point</span>
     *     <span style="color: #553000">purchaseCB</span>.query().setPaymentCompleteFlg_Equal_True();
     * }).<span style="color: #CC4747">greaterEqual</span>(123); <span style="color: #3F7E5E">// *Don't forget the parameter</span>
     * </pre> 
     * @param derivedCBLambda The callback for sub-query of referrer. (NotNull)
     * @return The parameter for comparing with scalar. (NotNull)
     */
    public HpQDRParameter<CB, Integer> countDistinct(SubQuery<CB> derivedCBLambda) {
        return doCountDistinct(derivedCBLambda, null);
    }

    /**
     * An overload method for countDistinct(). So refer to the method's java-doc about basic info.
     * <pre>
     * cb.query().derivedPurchaseList().<span style="color: #CC4747">countDistinct</span>(<span style="color: #553000">purchaseCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">purchaseCB</span>.specify().<span style="color: #994747">columnPurchasePrice</span>(); <span style="color: #3F7E5E">// *Point</span>
     *     <span style="color: #553000">purchaseCB</span>.query().setPaymentCompleteFlg_Equal_True();
     * }).<span style="color: #994747">greaterEqual</span>(123, <span style="color: #553000">op</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> <span style="color: #553000">op</span>.<span style="color: #CC4747">coalesce</span>(0));
     * </pre> 
     * @param derivedCBLambda The callback for sub-query of referrer. (NotNull)
     * @param opLambda The callback for option of DerivedReferrer. For example, you can use a coalesce function. (NotNull)
     * @return The parameter for comparing with scalar. (NotNull)
     */
    public HpQDRParameter<CB, Integer> countDistinct(SubQuery<CB> derivedCBLambda,
            FunctionFilterOptionCall<DerivedReferrerOption> opLambda) {
        assertDerivedReferrerOption(opLambda);
        final DerivedReferrerOption option = createDerivedReferrerOption();
        opLambda.callback(option);
        return doCountDistinct(derivedCBLambda, option);
    }

    protected HpQDRParameter<CB, Integer> doCountDistinct(SubQuery<CB> subQuery, DerivedReferrerOption option) {
        assertSubQuery(subQuery);
        return createQDRParameter("count(distinct", subQuery, option);
    }

    /**
     * Set up the sub query of referrer for the scalar 'max'.
     * <pre>
     * cb.query().derivedPurchaseList().<span style="color: #CC4747">max</span>(<span style="color: #553000">purchaseCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">purchaseCB</span>.specify().<span style="color: #CC4747">columnPurchasePrice</span>(); <span style="color: #3F7E5E">// *Point</span>
     *     <span style="color: #553000">purchaseCB</span>.query().setPaymentCompleteFlg_Equal_True();
     * }).<span style="color: #CC4747">greaterEqual</span>(123); <span style="color: #3F7E5E">// *Don't forget the parameter</span>
     * </pre> 
     * @param derivedCBLambda The callback for sub-query of referrer. (NotNull)
     * @return The parameter for comparing with scalar. (NotNull)
     */
    public HpQDRParameter<CB, Object> max(SubQuery<CB> derivedCBLambda) {
        return doMax(derivedCBLambda, null);
    }

    /**
     * An overload method for max(). So refer to the method's java-doc about basic info.
     * <pre>
     * cb.query().derivedPurchaseList().<span style="color: #CC4747">max</span>(<span style="color: #553000">purchaseCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">purchaseCB</span>.specify().<span style="color: #994747">columnPurchasePrice</span>(); <span style="color: #3F7E5E">// *Point</span>
     *     <span style="color: #553000">purchaseCB</span>.query().setPaymentCompleteFlg_Equal_True();
     * }).<span style="color: #994747">greaterEqual</span>(123, <span style="color: #553000">op</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> <span style="color: #553000">op</span>.<span style="color: #CC4747">coalesce</span>(0));
     * </pre> 
     * @param derivedCBLambda The callback for sub-query of referrer. (NotNull)
     * @param opLambda The callback for option of DerivedReferrer. For example, you can use a coalesce function. (NotNull)
     * @return The parameter for comparing with scalar. (NotNull)
     */
    public HpQDRParameter<CB, Object> max(SubQuery<CB> derivedCBLambda, FunctionFilterOptionCall<DerivedReferrerOption> opLambda) {
        assertDerivedReferrerOption(opLambda);
        final DerivedReferrerOption option = createDerivedReferrerOption();
        opLambda.callback(option);
        return doMax(derivedCBLambda, option);
    }

    protected HpQDRParameter<CB, Object> doMax(SubQuery<CB> subQuery, DerivedReferrerOption option) {
        assertSubQuery(subQuery);
        return createQDRParameter("max", subQuery, option);
    }

    /**
     * Set up the sub query of referrer for the scalar 'min'.
     * <pre>
     * cb.query().derivedPurchaseList().<span style="color: #CC4747">min</span>(<span style="color: #553000">purchaseCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">purchaseCB</span>.specify().<span style="color: #CC4747">columnPurchasePrice</span>(); <span style="color: #3F7E5E">// *Point</span>
     *     <span style="color: #553000">purchaseCB</span>.query().setPaymentCompleteFlg_Equal_True();
     * }).<span style="color: #CC4747">greaterEqual</span>(123); <span style="color: #3F7E5E">// *Don't forget the parameter</span>
     * </pre> 
     * @param derivedCBLambda The callback for sub-query of referrer. (NotNull)
     * @return The parameter for comparing with scalar. (NotNull)
     */
    public HpQDRParameter<CB, Object> min(SubQuery<CB> derivedCBLambda) {
        return doMin(derivedCBLambda, null);
    }

    /**
     * An overload method for min(). So refer to the method's java-doc about basic info.
     * <pre>
     * cb.query().derivedPurchaseList().<span style="color: #CC4747">min</span>(<span style="color: #553000">purchaseCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">purchaseCB</span>.specify().<span style="color: #994747">columnPurchasePrice</span>(); <span style="color: #3F7E5E">// *Point</span>
     *     <span style="color: #553000">purchaseCB</span>.query().setPaymentCompleteFlg_Equal_True();
     * }).<span style="color: #994747">greaterEqual</span>(123, <span style="color: #553000">op</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> <span style="color: #553000">op</span>.<span style="color: #CC4747">coalesce</span>(0));
     * </pre> 
     * @param derivedCBLambda The callback for sub-query of referrer. (NotNull)
     * @param opLambda The callback for option of DerivedReferrer. For example, you can use a coalesce function. (NotNull)
     * @return The parameter for comparing with scalar. (NotNull)
     */
    public HpQDRParameter<CB, Object> min(SubQuery<CB> derivedCBLambda, FunctionFilterOptionCall<DerivedReferrerOption> opLambda) {
        assertDerivedReferrerOption(opLambda);
        final DerivedReferrerOption option = createDerivedReferrerOption();
        opLambda.callback(option);
        return doMin(derivedCBLambda, option);
    }

    protected HpQDRParameter<CB, Object> doMin(SubQuery<CB> subQuery, DerivedReferrerOption option) {
        assertSubQuery(subQuery);
        return createQDRParameter("min", subQuery, option);
    }

    /**
     * Set up the sub query of referrer for the scalar 'sum'.
     * <pre>
     * cb.query().derivedPurchaseList().<span style="color: #CC4747">sum</span>(<span style="color: #553000">purchaseCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">purchaseCB</span>.specify().<span style="color: #CC4747">columnPurchasePrice</span>(); <span style="color: #3F7E5E">// *Point</span>
     *     <span style="color: #553000">purchaseCB</span>.query().setPaymentCompleteFlg_Equal_True();
     * }).<span style="color: #CC4747">greaterEqual</span>(123); <span style="color: #3F7E5E">// *Don't forget the parameter</span>
     * </pre> 
     * @param derivedCBLambda The callback for sub-query of referrer. (NotNull)
     * @return The parameter for comparing with scalar. (NotNull)
     */
    public HpQDRParameter<CB, Number> sum(SubQuery<CB> derivedCBLambda) {
        return doSum(derivedCBLambda, null);
    }

    /**
     * An overload method for sum(). So refer to the method's java-doc about basic info.
     * <pre>
     * cb.query().derivedPurchaseList().<span style="color: #CC4747">sum</span>(<span style="color: #553000">purchaseCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">purchaseCB</span>.specify().<span style="color: #994747">columnPurchasePrice</span>(); <span style="color: #3F7E5E">// *Point</span>
     *     <span style="color: #553000">purchaseCB</span>.query().setPaymentCompleteFlg_Equal_True();
     * }).<span style="color: #994747">greaterEqual</span>(123, <span style="color: #553000">op</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> <span style="color: #553000">op</span>.<span style="color: #CC4747">coalesce</span>(0));
     * </pre> 
     * @param derivedCBLambda The callback for sub-query of referrer. (NotNull)
     * @param opLambda The callback for option of DerivedReferrer. For example, you can use a coalesce function. (NotNull)
     * @return The parameter for comparing with scalar. (NotNull)
     */
    public HpQDRParameter<CB, Number> sum(SubQuery<CB> derivedCBLambda, FunctionFilterOptionCall<DerivedReferrerOption> opLambda) {
        assertDerivedReferrerOption(opLambda);
        final DerivedReferrerOption option = createDerivedReferrerOption();
        opLambda.callback(option);
        return doSum(derivedCBLambda, option);
    }

    protected HpQDRParameter<CB, Number> doSum(SubQuery<CB> subQuery, DerivedReferrerOption option) {
        assertSubQuery(subQuery);
        return createQDRParameter("sum", subQuery, option);
    }

    /**
     * Set up the sub query of referrer for the scalar 'avg'.
     * <pre>
     * cb.query().derivedPurchaseList().<span style="color: #CC4747">avg</span>(<span style="color: #553000">purchaseCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">purchaseCB</span>.specify().<span style="color: #CC4747">columnPurchasePrice</span>(); <span style="color: #3F7E5E">// *Point</span>
     *     <span style="color: #553000">purchaseCB</span>.query().setPaymentCompleteFlg_Equal_True();
     * }).<span style="color: #CC4747">greaterEqual</span>(123); <span style="color: #3F7E5E">// *Don't forget the parameter</span>
     * </pre> 
     * @param derivedCBLambda The callback for sub-query of referrer. (NotNull)
     * @return The parameter for comparing with scalar. (NotNull)
     */
    public HpQDRParameter<CB, Number> avg(SubQuery<CB> derivedCBLambda) {
        return doAvg(derivedCBLambda, null);
    }

    /**
     * An overload method for avg(). So refer to the method's java-doc about basic info.
     * <pre>
     * cb.query().derivedPurchaseList().<span style="color: #CC4747">avg</span>(<span style="color: #553000">purchaseCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">purchaseCB</span>.specify().<span style="color: #994747">columnPurchasePrice</span>(); <span style="color: #3F7E5E">// *Point</span>
     *     <span style="color: #553000">purchaseCB</span>.query().setPaymentCompleteFlg_Equal_True();
     * }).<span style="color: #994747">greaterEqual</span>(123, <span style="color: #553000">op</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> <span style="color: #553000">op</span>.<span style="color: #CC4747">coalesce</span>(0));
     * </pre> 
     * @param derivedCBLambda The callback for sub-query of referrer. (NotNull)
     * @param opLambda The callback for option of DerivedReferrer. For example, you can use a coalesce function. (NotNull)
     * @return The parameter for comparing with scalar. (NotNull)
     */
    public HpQDRParameter<CB, Number> avg(SubQuery<CB> derivedCBLambda, FunctionFilterOptionCall<DerivedReferrerOption> opLambda) {
        assertDerivedReferrerOption(opLambda);
        final DerivedReferrerOption option = createDerivedReferrerOption();
        opLambda.callback(option);
        return doAvg(derivedCBLambda, option);
    }

    protected HpQDRParameter<CB, Number> doAvg(SubQuery<CB> subQuery, DerivedReferrerOption option) {
        assertSubQuery(subQuery);
        return createQDRParameter("avg", subQuery, option);
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected DerivedReferrerOption createDerivedReferrerOption() {
        return _derivedReferrerOptionFactory.create();
    }

    protected <PARAMETER> HpQDRParameter<CB, PARAMETER> createQDRParameter(String fuction, SubQuery<CB> subQuery,
            DerivedReferrerOption option) {
        return new HpQDRParameter<CB, PARAMETER>(fuction, subQuery, option, _setupper);
    }

    protected void assertSubQuery(SubQuery<?> subQuery) {
        if (subQuery == null) {
            String msg = "The argument 'subQuery' for DerivedReferrer should not be null.";
            throw new IllegalArgumentException(msg);
        }
    }

    protected void assertDerivedReferrerOption(FunctionFilterOptionCall<DerivedReferrerOption> opLambda) {
        if (opLambda == null) {
            String msg = "The argument 'opLambda' for DerivedReferrer should not be null.";
            throw new IllegalArgumentException(msg);
        }
    }
}
