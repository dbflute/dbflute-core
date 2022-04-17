/*
 * Copyright 2014-2022 the original author or authors.
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
package org.dbflute.cbean.dream;

/**
 * Welcome to DreamCruise, providing DBFlute Dreams.
 * @author jflute
 * @since 1.1.0 (2014/10/17 Friday)
 */
public interface WelcomeToDreamCruise {

    /**
     * DBFlute Dreams.
     * <pre>
     * e.g. member that purchases products only purchased by the member
     *  cb.query().existsPurchaseList(purchaseCB -&gt; {
     *      purchaseCB.query().queryProduct().notExistsPurchaseList(productPurchaseCB -&gt; {
     *          productPurchaseCB.columnQuery(colCB -&gt; {
     *              colCB.specify().columnMemberId();
     *          }).notEqual(colCB -&gt; {
     *              colCB.<span style="color: #CC4747">overTheWaves</span>(cb.<span style="color: #994747">dreamCruiseCB()</span>.specify().columnMemberId());
     *          });
     *      });
     *  });
     * </pre>
     * @param dreamCruiseTicket The ticket column specified by your Dream Cruise. (NotNull)
     */
    void overTheWaves(SpecifiedColumn dreamCruiseTicket); // #dream

    /**
     * DBFlute Dreams.
     * <pre>
     * e.g. ColumnQuery: ... &gt; '2015/04/05' + (PURCHASE_COUNT days)
     *  cb.columnQuery(colCB -&gt; {
     *      colCB.column...();
     *  }).greaterThan(colCB -&gt; {
     *      colCB.<span style="color: #CC4747">mysticRhythms</span>(toDate("2015/04/05"));
     *  }).convert(op -&gt; op.<span style="color: #994747">addDay</span>(cb.<span style="color: #994747">dreamCruiseCB()</span>.specify().columnPurchaseCount());
     * </pre>
     * @param mysticBinding The mystic value of the column e.g. to compare with other column. (NotNull)
     */
    void mysticRhythms(Object mysticBinding); // #dream
}
