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
import org.seasar.dbflute.cbean.SpecifyQuery;

/**
 * The decorator for ScalarCondition (the old name: ScalarSubQuery).
 * @param <CB> The type of condition-bean.
 * @author jflute
 */
public class HpSSQDecorator<CB extends ConditionBean> {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final HpSSQOption<CB> _option;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public HpSSQDecorator(HpSSQOption<CB> option) {
        _option = option;
    }

    // ===================================================================================
    //                                                                          Decoration
    //                                                                          ==========
    /**
     * Partition the scope of condition by the specified query. <br />
     * You can add a correlation condition to the sub-query.
     * <pre>
     * cb.query().scalar_Equal().<span style="color: #DD4747">max</span>(new SubQuery&lt;PurchaseCB&gt;() {
     *     public void query(PurchaseCB subCB) {
     *         subCB.specify().<span style="color: #DD4747">columnPurchasePrice</span>(); <span style="color: #3F7E5E">// *Point!</span>
     *         subCB.query().setPaymentCompleteFlg_Equal_True();
     *     }
     * }).<span style="color: #DD4747">partitionBy</span>(new SpecifyQuery&lt;PurchaseCB&gt;() {
     *     public void specify(PurchaseCB cb) {
     *         cb.specify().<span style="color: #DD4747">columnMemberId</span>(); <span style="color: #3F7E5E">// *Point!</span>
     *     }
     * });
     * </pre>
     * @param specifyQuery The query to specify the partition. (NotNull)
     */
    public void partitionBy(SpecifyQuery<CB> specifyQuery) {
        assertSpecifyQuery(specifyQuery);
        _option.setPartitionBySpecify(specifyQuery);

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
