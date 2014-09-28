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
package org.seasar.dbflute.bhv;

import java.util.List;

import org.seasar.dbflute.Entity;

/**
 * The handler of referrer list to load nested referrer.
 * <pre>
 * MemberCB cb = new MemberCB();
 * cb.query().set...
 * List&lt;Member&gt; statusList = memberBhv.selectList(cb);
 * memberBhv.loadPurchaseList(statusList, new ReferrerConditionSetupper&lt;PurchaseCB&gt;() {
 *     public void setup(PurchaseCB cb) {
 *         cb.query().set...
 *         cb.query().addOrderBy_PurchasePrice_Desc();
 *     }
 * }).withNestedReferrer(new <span style="color: #DD4747">ReferrerListHandler</span>&lt;Purchase&gt;() {
 *     public void <span style="color: #DD4747">handle</span>(List&lt;Purchase&gt; referrerList) {
 *         <span style="color: #3F7E5E">// you can call LoadReferrer here for nested referrer as you like it</span>
 *         memberBhv.loadPurchasePaymentList(referrerList, new ReferrerConditionSetupper&lt;PurchasePaymentCB&gt;() {
 *             public void setup(PurchasePaymentCB cb) {
 *                 cb.query().set...
 *                 cb.query().addOrderBy_PaymentAmount_Desc();
 *             }
 *         });
 *         ...
 *     }
 * }
 * </pre>
 * @param <REFERRER> The type of referrer entity.
 * @author jflute
 */
public interface ReferrerListHandler<REFERRER extends Entity> {

    /**
     * Handle the list of referrer to load nested referrer. <br />
     * You can call LoadReferrer for nested table.
     * <pre>
     * MemberCB cb = new MemberCB();
     * cb.query().set...
     * List&lt;Member&gt; statusList = memberBhv.selectList(cb);
     * memberBhv.loadPurchaseList(statusList, new ReferrerConditionSetupper&lt;PurchaseCB&gt;() {
     *     public void setup(PurchaseCB cb) {
     *         cb.query().set...
     *         cb.query().addOrderBy_PurchasePrice_Desc();
     *     }
     * }).withNestedReferrer(new <span style="color: #DD4747">ReferrerListHandler</span>&lt;Purchase&gt;() {
     *     public void <span style="color: #DD4747">handle</span>(List&lt;Purchase&gt; referrerList) {
     *         <span style="color: #3F7E5E">// you can call LoadReferrer here for nested referrer as you like it</span>
     *         memberBhv.loadPurchasePaymentList(referrerList, new ReferrerConditionSetupper&lt;PurchasePaymentCB&gt;() {
     *             public void setup(PurchasePaymentCB cb) {
     *                 cb.query().set...
     *                 cb.query().addOrderBy_PaymentAmount_Desc();
     *             }
     *         });
     *         ...
     *     }
     * }
     * </pre>
     * @param referrerList The entity list of referrer. (NotNull: might be empty)
     */
    void handle(List<REFERRER> referrerList);
}
