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
import org.dbflute.cbean.scoping.SpecifyQuery;

/**
 * The decorator for ScalarCondition (the old name: ScalarSubQuery).
 * @param <CB> The type of condition-bean.
 * @author jflute
 */
public class HpSLCDecorator<CB extends ConditionBean> {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final HpSLCCustomized<CB> _option;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public HpSLCDecorator(HpSLCCustomized<CB> option) {
        _option = option;
    }

    // ===================================================================================
    //                                                                          Decoration
    //                                                                          ==========
    /**
     * Partition the scope of condition by the specified query. <br>
     * You can add a correlation condition to the sub-query.
     * <pre>
     * cb.query().scalar_Equal().<span style="color: #994747">max</span>(<span style="color: #553000">purchaseCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">purchaseCB</span>.specify().<span style="color: #994747">columnPurchasePrice</span>();
     *     <span style="color: #553000">purchaseCB</span>.query().setPaymentCompleteFlg_Equal_True();
     * }).<span style="color: #CC4747">partitionBy</span>(<span style="color: #553000">colCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">colCB</span>.specify().<span style="color: #CC4747">columnMemberId</span>(); <span style="color: #3F7E5E">// *Point!</span>
     * });
     * </pre>
     * @param colCBLambda The callback for query to specify the partition column. (NotNull)
     */
    public void partitionBy(SpecifyQuery<CB> colCBLambda) {
        assertSpecifyQuery(colCBLambda);
        _option.setPartitionBySpecify(colCBLambda);

        // It's difficult for using relation in partition-by so unsupported.
        // The alias-name problem occurs so if you try, check ColumnQuery way.
        // (You need to synchronize QyCall...)
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected void assertSpecifyQuery(SpecifyQuery<?> specifyQuery) {
        if (specifyQuery == null) {
            String msg = "The argument 'specifyQuery' for ScalarCondition should not be null.";
            throw new IllegalArgumentException(msg);
        }
    }
}
