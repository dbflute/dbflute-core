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

/**
 * The loader of nested referrer.
 * <pre>
 * MemberCB cb = new MemberCB();
 * cb.query().set...
 * List&lt;Member&gt; memberList = memberBhv.selectList(cb);
 * memberBhv.<span style="color: #DD4747">load</span>(memberList, loader -&gt; {
 *     loader.<span style="color: #DD4747">loadPurchaseList</span>(purchaseCB -&gt; {
 *         purchaseCB.query().set...
 *         purchaseCB.query().addOrderBy_PurchasePrice_Desc();
 *     }); <span style="color: #3F7E5E">// you can also load nested referrer from here</span>
 *     <span style="color: #3F7E5E">//}).withNestedList(purchaseLoader -&gt {</span>
 *     <span style="color: #3F7E5E">//    purchaseLoader.loadPurchasePaymentList(...);</span>
 *     <span style="color: #3F7E5E">//});</span>
 *
 *     <span style="color: #3F7E5E">// you can also pull out foreign table and load its referrer</span>
 *     <span style="color: #3F7E5E">// (setupSelect of the foreign table should be called)</span>
 *     <span style="color: #3F7E5E">//loader.pulloutMemberStatus().loadMemberLoginList(...)</span>
 * }
 * for (Member member : memberList) {
 *     List&lt;Purchase&gt; purchaseList = member.<span style="color: #DD4747">getPurchaseList()</span>;
 *     for (Purchase purchase : purchaseList) {
 *         ...
 *     }
 * }
 * </pre>
 * @param <LOADER> The type of referrer loader.
 * @author jflute
 * @since 1.0.6A (2014/06/14 Saturday)
 */
public interface ReferrerLoaderHandler<LOADER> {

    /**
     * Provide referrer loader for LoadReferrer.
     * <pre>
     * MemberCB cb = new MemberCB();
     * cb.query().set...
     * List&lt;Member&gt; memberList = memberBhv.selectList(cb);
     * memberBhv.<span style="color: #DD4747">load</span>(memberList, loader -&gt; {
     *     loader.<span style="color: #DD4747">loadPurchaseList</span>(purchaseCB -&gt; {
     *         purchaseCB.query().set...
     *         purchaseCB.query().addOrderBy_PurchasePrice_Desc();
     *     }); <span style="color: #3F7E5E">// you can also load nested referrer from here</span>
     *     <span style="color: #3F7E5E">//}).withNestedList(purchaseLoader -&gt {</span>
     *     <span style="color: #3F7E5E">//    purchaseLoader.loadPurchasePaymentList(...);</span>
     *     <span style="color: #3F7E5E">//});</span>
     *
     *     <span style="color: #3F7E5E">// you can also pull out foreign table and load its referrer</span>
     *     <span style="color: #3F7E5E">// (setupSelect of the foreign table should be called)</span>
     *     <span style="color: #3F7E5E">//loader.pulloutMemberStatus().loadMemberLoginList(...)</span>
     * }
     * for (Member member : memberList) {
     *     List&lt;Purchase&gt; purchaseList = member.<span style="color: #DD4747">getPurchaseList()</span>;
     *     for (Purchase purchase : purchaseList) {
     *         ...
     *     }
     * }
     * </pre>
     * @param loader The loader of referrer. (NotNull)
     */
    void handle(LOADER loader);
}
