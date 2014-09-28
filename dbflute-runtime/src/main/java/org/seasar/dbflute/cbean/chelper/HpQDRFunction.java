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
package org.seasar.dbflute.cbean.chelper;

import org.seasar.dbflute.cbean.ConditionBean;
import org.seasar.dbflute.cbean.SubQuery;
import org.seasar.dbflute.cbean.coption.DerivedReferrerOption;

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

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public HpQDRFunction(HpQDRSetupper<CB> setupper) {
        _setupper = setupper;
    }

    // ===================================================================================
    //                                                                            Function
    //                                                                            ========
    /**
     * Set up the sub query of referrer for the scalar 'count'.
     * <pre>
     * cb.query().derivedPurchaseList().<span style="color: #DD4747">count</span>(new SubQuery&lt;PurchaseCB&gt;() {
     *     public void query(PurchaseCB subCB) {
     *         subCB.specify().<span style="color: #DD4747">columnPurchaseId</span>(); <span style="color: #3F7E5E">// *Point</span>
     *         subCB.query().setPaymentCompleteFlg_Equal_True();
     *     }
     * }).<span style="color: #DD4747">greaterEqual</span>(123); <span style="color: #3F7E5E">// *Don't forget the parameter</span>
     * </pre> 
     * @param subQuery The sub query of referrer. (NotNull)
     * @return The parameter for comparing with scalar. (NotNull)
     */
    public HpQDRParameter<CB, Integer> count(SubQuery<CB> subQuery) {
        return doCount(subQuery, null);
    }

    /**
     * An overload method for count(). So refer to the method's java-doc about basic info.
     * <pre>
     * cb.query().derivedPurchaseList().<span style="color: #DD4747">count</span>(new SubQuery&lt;PurchaseCB&gt;() {
     *     public void query(PurchaseCB subCB) {
     *         ...
     *     }
     * }).<span style="color: #DD4747">greaterEqual</span>(123, new DerivedReferrerOption().<span style="color: #DD4747">coalesce</span>(0));
     * </pre> 
     * @param subQuery The sub query of referrer. (NotNull)
     * @param option The option for DerivedReferrer. For example, you can use a coalesce function. (NotNull)
     * @return The parameter for comparing with scalar. (NotNull)
     */
    public HpQDRParameter<CB, Integer> count(SubQuery<CB> subQuery, DerivedReferrerOption option) {
        assertDerivedReferrerOption(option);
        return doCount(subQuery, option);
    }

    protected HpQDRParameter<CB, Integer> doCount(SubQuery<CB> subQuery, DerivedReferrerOption option) {
        assertSubQuery(subQuery);
        return createQDRParameter("count", subQuery, option);
    }

    /**
     * Set up the sub query of referrer for the scalar 'count(with distinct)'.
     * <pre>
     * cb.query().derivedPurchaseList().<span style="color: #DD4747">countDistinct</span>(new SubQuery&lt;PurchaseCB&gt;() {
     *     public void query(PurchaseCB subCB) {
     *         subCB.specify().<span style="color: #DD4747">columnPurchasePrice</span>(); <span style="color: #3F7E5E">// *Point</span>
     *         subCB.query().setPaymentCompleteFlg_Equal_True();
     *     }
     * }).<span style="color: #DD4747">greaterEqual</span>(123); <span style="color: #3F7E5E">// *Don't forget the parameter</span>
     * </pre> 
     * @param subQuery The sub query of referrer. (NotNull)
     * @return The parameter for comparing with scalar. (NotNull)
     */
    public HpQDRParameter<CB, Integer> countDistinct(SubQuery<CB> subQuery) {
        return doCountDistinct(subQuery, null);
    }

    /**
     * An overload method for countDistinct(). So refer to the method's java-doc about basic info.
     * <pre>
     * cb.query().derivedPurchaseList().<span style="color: #DD4747">countDistinct</span>(new SubQuery&lt;PurchaseCB&gt;() {
     *     public void query(PurchaseCB subCB) {
     *         ...
     *     }
     * }).<span style="color: #DD4747">greaterEqual</span>(123, new DerivedReferrerOption().<span style="color: #DD4747">coalesce</span>(0));
     * </pre> 
     * @param subQuery The sub query of referrer. (NotNull)
     * @param option The option for DerivedReferrer. For example, you can use a coalesce function. (NotNull)
     * @return The parameter for comparing with scalar. (NotNull)
     */
    public HpQDRParameter<CB, Integer> countDistinct(SubQuery<CB> subQuery, DerivedReferrerOption option) {
        assertDerivedReferrerOption(option);
        return doCountDistinct(subQuery, option);
    }

    protected HpQDRParameter<CB, Integer> doCountDistinct(SubQuery<CB> subQuery, DerivedReferrerOption option) {
        assertSubQuery(subQuery);
        return createQDRParameter("count(distinct", subQuery, option);
    }

    /**
     * Set up the sub query of referrer for the scalar 'max'.
     * <pre>
     * cb.query().derivedPurchaseList().<span style="color: #DD4747">max</span>(new SubQuery&lt;PurchaseCB&gt;() {
     *     public void query(PurchaseCB subCB) {
     *         subCB.specify().<span style="color: #DD4747">columnPurchasePrice</span>(); <span style="color: #3F7E5E">// *Point</span>
     *         subCB.query().setPaymentCompleteFlg_Equal_True();
     *     }
     * }).<span style="color: #DD4747">greaterEqual</span>(123); <span style="color: #3F7E5E">// *Don't forget the parameter</span>
     * </pre> 
     * @param subQuery The sub query of referrer. (NotNull)
     * @return The parameter for comparing with scalar. (NotNull)
     */
    public HpQDRParameter<CB, Object> max(SubQuery<CB> subQuery) {
        return doMax(subQuery, null);
    }

    /**
     * An overload method for max(). So refer to the method's java-doc about basic info.
     * <pre>
     * cb.query().derivedPurchaseList().<span style="color: #DD4747">max</span>(new SubQuery&lt;PurchaseCB&gt;() {
     *     public void query(PurchaseCB subCB) {
     *         ...
     *     }
     * }).<span style="color: #DD4747">greaterEqual</span>(123, new DerivedReferrerOption().<span style="color: #DD4747">coalesce</span>(0));
     * </pre> 
     * @param subQuery The sub query of referrer. (NotNull)
     * @param option The option for DerivedReferrer. For example, you can use a coalesce function. (NotNull)
     * @return The parameter for comparing with scalar. (NotNull)
     */
    public HpQDRParameter<CB, Object> max(SubQuery<CB> subQuery, DerivedReferrerOption option) {
        assertDerivedReferrerOption(option);
        return doMax(subQuery, option);
    }

    protected HpQDRParameter<CB, Object> doMax(SubQuery<CB> subQuery, DerivedReferrerOption option) {
        assertSubQuery(subQuery);
        return createQDRParameter("max", subQuery, option);
    }

    /**
     * Set up the sub query of referrer for the scalar 'min'.
     * <pre>
     * cb.query().derivedPurchaseList().<span style="color: #DD4747">min</span>(new SubQuery&lt;PurchaseCB&gt;() {
     *     public void query(PurchaseCB subCB) {
     *         subCB.specify().<span style="color: #DD4747">columnPurchasePrice</span>(); <span style="color: #3F7E5E">// *Point</span>
     *         subCB.query().setPaymentCompleteFlg_Equal_True();
     *     }
     * }).<span style="color: #DD4747">greaterEqual</span>(123); <span style="color: #3F7E5E">// *Don't forget the parameter</span>
     * </pre> 
     * @param subQuery The sub query of referrer. (NotNull)
     * @return The parameter for comparing with scalar. (NotNull)
     */
    public HpQDRParameter<CB, Object> min(SubQuery<CB> subQuery) {
        return doMin(subQuery, null);
    }

    /**
     * An overload method for min(). So refer to the method's java-doc about basic info.
     * <pre>
     * cb.query().derivedPurchaseList().<span style="color: #DD4747">min</span>(new SubQuery&lt;PurchaseCB&gt;() {
     *     public void query(PurchaseCB subCB) {
     *         ...
     *     }
     * }).<span style="color: #DD4747">greaterEqual</span>(123, new DerivedReferrerOption().<span style="color: #DD4747">coalesce</span>(0));
     * </pre> 
     * @param subQuery The sub query of referrer. (NotNull)
     * @param option The option for DerivedReferrer. For example, you can use a coalesce function. (NotNull)
     * @return The parameter for comparing with scalar. (NotNull)
     */
    public HpQDRParameter<CB, Object> min(SubQuery<CB> subQuery, DerivedReferrerOption option) {
        return doMin(subQuery, option);
    }

    protected HpQDRParameter<CB, Object> doMin(SubQuery<CB> subQuery, DerivedReferrerOption option) {
        assertSubQuery(subQuery);
        return createQDRParameter("min", subQuery, option);
    }

    /**
     * Set up the sub query of referrer for the scalar 'sum'.
     * <pre>
     * cb.query().derivedPurchaseList().<span style="color: #DD4747">sum</span>(new SubQuery&lt;PurchaseCB&gt;() {
     *     public void query(PurchaseCB subCB) {
     *         subCB.specify().<span style="color: #DD4747">columnPurchasePrice</span>(); <span style="color: #3F7E5E">// *Point</span>
     *         subCB.query().setPaymentCompleteFlg_Equal_True();
     *     }
     * }).<span style="color: #DD4747">greaterEqual</span>(123); <span style="color: #3F7E5E">// *Don't forget the parameter</span>
     * </pre> 
     * @param subQuery The sub query of referrer. (NotNull)
     * @return The parameter for comparing with scalar. (NotNull)
     */
    public HpQDRParameter<CB, Number> sum(SubQuery<CB> subQuery) {
        return doSum(subQuery, null);
    }

    /**
     * An overload method for sum(). So refer to the method's java-doc about basic info.
     * <pre>
     * cb.query().derivedPurchaseList().<span style="color: #DD4747">sum</span>(new SubQuery&lt;PurchaseCB&gt;() {
     *     public void query(PurchaseCB subCB) {
     *         ...
     *     }
     * }).<span style="color: #DD4747">greaterEqual</span>(123, new DerivedReferrerOption().<span style="color: #DD4747">coalesce</span>(0));
     * </pre> 
     * @param subQuery The sub query of referrer. (NotNull)
     * @param option The option for DerivedReferrer. For example, you can use a coalesce function. (NotNull)
     * @return The parameter for comparing with scalar. (NotNull)
     */
    public HpQDRParameter<CB, Number> sum(SubQuery<CB> subQuery, DerivedReferrerOption option) {
        assertDerivedReferrerOption(option);
        return doSum(subQuery, option);
    }

    protected HpQDRParameter<CB, Number> doSum(SubQuery<CB> subQuery, DerivedReferrerOption option) {
        assertSubQuery(subQuery);
        return createQDRParameter("sum", subQuery, option);
    }

    /**
     * Set up the sub query of referrer for the scalar 'avg'.
     * <pre>
     * cb.query().derivedPurchaseList().<span style="color: #DD4747">avg</span>(new SubQuery&lt;PurchaseCB&gt;() {
     *     public void query(PurchaseCB subCB) {
     *         subCB.specify().<span style="color: #DD4747">columnPurchasePrice</span>(); <span style="color: #3F7E5E">// *Point</span>
     *         subCB.query().setPaymentCompleteFlg_Equal_True();
     *     }
     * }).<span style="color: #DD4747">greaterEqual</span>(123); <span style="color: #3F7E5E">// *Don't forget the parameter</span>
     * </pre> 
     * @param subQuery The sub query of referrer. (NotNull)
     * @return The parameter for comparing with scalar. (NotNull)
     */
    public HpQDRParameter<CB, Number> avg(SubQuery<CB> subQuery) {
        return doAvg(subQuery, null);
    }

    /**
     * An overload method for avg(). So refer to the method's java-doc about basic info.
     * <pre>
     * cb.query().derivedPurchaseList().<span style="color: #DD4747">avg</span>(new SubQuery&lt;PurchaseCB&gt;() {
     *     public void query(PurchaseCB subCB) {
     *         ...
     *     }
     * }).<span style="color: #DD4747">greaterEqual</span>(123, new DerivedReferrerOption().<span style="color: #DD4747">coalesce</span>(0));
     * </pre> 
     * @param subQuery The sub query of referrer. (NotNull)
     * @param option The option for DerivedReferrer. For example, you can use a coalesce function. (NotNull)
     * @return The parameter for comparing with scalar. (NotNull)
     */
    public HpQDRParameter<CB, Number> avg(SubQuery<CB> subQuery, DerivedReferrerOption option) {
        return doAvg(subQuery, option);
    }

    protected HpQDRParameter<CB, Number> doAvg(SubQuery<CB> subQuery, DerivedReferrerOption option) {
        assertSubQuery(subQuery);
        return createQDRParameter("avg", subQuery, option);
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
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

    protected void assertDerivedReferrerOption(DerivedReferrerOption option) {
        if (option == null) {
            String msg = "The argument 'option' for DerivedReferrer should not be null.";
            throw new IllegalArgumentException(msg);
        }
    }
}
