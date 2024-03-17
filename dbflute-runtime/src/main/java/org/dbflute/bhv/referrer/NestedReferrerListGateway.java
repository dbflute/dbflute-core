/*
 * Copyright 2014-2024 the original author or authors.
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
package org.dbflute.bhv.referrer;

import org.dbflute.Entity;

/**
 * The gateway of list handling for nested referrer.
 * <pre>
 * List&lt;Member&gt; memberList = <span style="color: #0000C0">memberBhv</span>.selectList(cb <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
 *     cb.query().set...
 * });
 * <span style="color: #0000C0">memberBhv</span>.loadPurchaseList(memberList, new ReferrerConditionSetupper&lt;PurchaseCB&gt;() {
 *     public void setup(PurchaseCB cb) {
 *         cb.query().addOrderBy_PurchaseDatetime_Asc();
 *     }
 * }).<span style="color: #CC4747">withNestedReferrer</span>(new ReferrerListHandler&lt;Purchase&gt;() {
 *     public void <span style="color: #CC4747">handle</span>(List&lt;Purchase&gt; referrerList) {
 *         <span style="color: #3F7E5E">// you can call LoadReferrer here for nested referrer as you like it</span>
 *         <span style="color: #0000C0">purchaseBhv</span>.loadPurchasePaymentList(referrerList, new ReferrerConditionSetupper&lt;PurchasePaymentCB&gt;() {
 *             public void setup(PurchasePaymentCB cb) {
 *                 ...
 *             }
 *         });
 *         ...
 *     }
 * }
 * </pre>
 * @param <REFERRER> The type of referrer entity.
 * @author jflute
 * @since 1.0.5J (2014/06/14 Saturday)
 */
@FunctionalInterface
public interface NestedReferrerListGateway<REFERRER extends Entity> {

    /**
     * Set up nested referrer by the handler.
     * <pre>
     * List&lt;Member&gt; <span style="color: #553000">memberList</span> = <span style="color: #0000C0">memberBhv</span>.selectList(<span style="color: #553000">cb</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">cb</span>.query().set...
     * });
     * <span style="color: #0000C0">memberBhv</span>.<span style="color: #994747">loadPurchaseList</span>(<span style="color: #553000">memberList</span>, <span style="color: #553000">purchaseCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">purchaseCB</span>.query().addOrderBy_PurchaseDatetime_Asc();
     * }).<span style="color: #CC4747">withNestedReferrer</span>(<span style="color: #553000">purchaseList</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #3F7E5E">// you can call LoadReferrer here for nested referrer as you like it</span>
     *     <span style="color: #0000C0">purchaseBhv</span>.<span style="color: #994747">loadPurchasePaymentList</span>(<span style="color: #553000">purchaseList</span>, <span style="color: #553000">paymentCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *         <span style="color: #553000">paymentCB</span>.query().addOrderBy_PaymentDatetime_Desc();
     *     });
     *     ...
     * });
     * </pre>
     * @param entityListLambda The callback for handler of referrer list to load nested referrer. (NotNull)
     */
    void withNestedReferrer(ReferrerListHandler<REFERRER> entityListLambda);
}
