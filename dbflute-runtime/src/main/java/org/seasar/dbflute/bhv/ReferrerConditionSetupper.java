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

import org.seasar.dbflute.cbean.ConditionBean;

/**
 * The set-upper of condition-bean for referrer.
 * <pre>
 * loader.<span style="color: #DD4747">loadPurchaseList</span>(memberList, referrerCB -&gt; {
 *     cb.setupSelect_Product();
 *     cb.query().setPurchasePrice_GreaterEqual(2000);
 *     cb.query().addOrderBy_PurchaseDatetime_Desc();
 *     ...
 * }); <span style="color: #3F7E5E">// you can also load nested referrer from here</span>
 * <span style="color: #3F7E5E">//}).withNestedList(purchaseLoader -&gt {</span>
 * <span style="color: #3F7E5E">//    purchaseLoader.loadPurchasePaymentList(...);</span>
 * <span style="color: #3F7E5E">//});</span>
 * for (Member member : memberList) {
 *     List&lt;Purchase&gt; purchaseList = member.<span style="color: #DD4747">getPurchaseList()</span>;
 *     for (Purchase purchase : purchaseList) {
 *         ...
 *     }
 * }
 * </pre>
 * @param <CB> The type of condition-bean for referrer.
 * @author jflute
 * @since 1.0.5F (2014/05/12 Monday)
 */
public interface ReferrerConditionSetupper<CB extends ConditionBean> {

    /**
     * Set up condition-bean for referrer. <br />
     * You can call SetupSelect, Query, OrderBy... <br />
     * Ordering by FK is already set up so you should add next order condition here.
     * <pre>
     * loader.<span style="color: #DD4747">loadPurchaseList</span>(memberList, referrerCB -&gt; {
     *     cb.setupSelect_Product();
     *     cb.query().setPurchasePrice_GreaterEqual(2000);
     *     cb.query().addOrderBy_PurchaseDatetime_Desc();
     *     ...
     * }); <span style="color: #3F7E5E">// you can also load nested referrer from here</span>
     * <span style="color: #3F7E5E">//}).withNestedList(purchaseLoader -&gt {</span>
     * <span style="color: #3F7E5E">//    purchaseLoader.loadPurchasePaymentList(...);</span>
     * <span style="color: #3F7E5E">//});</span>
     * for (Member member : memberList) {
     *     List&lt;Purchase&gt; purchaseList = member.<span style="color: #DD4747">getPurchaseList()</span>;
     *     for (Purchase purchase : purchaseList) {
     *         ...
     *     }
     * }
     * </pre>
     * @param refCB The prepared instance of condition-bean for referrer table. (NotNull)
     */
    void setup(CB refCB);
}
