/*
 * Copyright 2014-2014 the original author or authors.
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
import org.dbflute.cbean.scoping.SubQuery;

/**
 * The function for ScalarCondition (the old name: ScalarSubQuery).
 * @param <CB> The type of condition-bean.
 * @author jflute
 */
public class HpSSQFunction<CB extends ConditionBean> {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final HpSSQSetupper<CB> _setupper;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public HpSSQFunction(HpSSQSetupper<CB> setupper) {
        _setupper = setupper;
    }

    // ===================================================================================
    //                                                                            Function
    //                                                                            ========
    /**
     * Set up the sub query of myself for the scalar 'max'.
     * <pre>
     * cb.query().scalar_Equal().<span style="color: #CC4747">max</span>(new SubQuery&lt;PurchaseCB&gt;() {
     *     public void query(PurchaseCB subCB) {
     *         subCB.specify().<span style="color: #CC4747">columnPurchasePrice</span>(); <span style="color: #3F7E5E">// *Point!</span>
     *         subCB.query().setPaymentCompleteFlg_Equal_True();
     *     }
     * });
     * </pre> 
     * @param scalarCBLambda The callback for sub-query of myself. (NotNull)
     * @return The decorator of ScalarCondition. e.g. you can use partition-by. (NotNull)
     */
    public HpSSQDecorator<CB> max(SubQuery<CB> scalarCBLambda) {
        assertSubQuery(scalarCBLambda);
        final HpSSQOption<CB> option = createOption();
        _setupper.setup("max", scalarCBLambda, option);
        return createDecorator(option);
    }

    /**
     * Set up the sub query of myself for the scalar 'min'.
     * <pre>
     * cb.query().scalar_Equal().<span style="color: #CC4747">min</span>(new SubQuery&lt;PurchaseCB&gt;() {
     *     public void query(PurchaseCB subCB) {
     *         subCB.specify().<span style="color: #CC4747">columnPurchasePrice</span>(); <span style="color: #3F7E5E">// *Point!</span>
     *         subCB.query().setPaymentCompleteFlg_Equal_True();
     *     }
     * });
     * </pre> 
     * @param scalarCBLambda The callback for sub-query of myself. (NotNull)
     * @return The decorator of ScalarCondition. e.g. you can use partition-by. (NotNull)
     */
    public HpSSQDecorator<CB> min(SubQuery<CB> scalarCBLambda) {
        assertSubQuery(scalarCBLambda);
        final HpSSQOption<CB> option = createOption();
        _setupper.setup("min", scalarCBLambda, option);
        return createDecorator(option);
    }

    /**
     * Set up the sub query of myself for the scalar 'sum'.
     * <pre>
     * cb.query().scalar_Equal().<span style="color: #CC4747">sum</span>(new SubQuery&lt;PurchaseCB&gt;() {
     *     public void query(PurchaseCB subCB) {
     *         subCB.specify().<span style="color: #CC4747">columnPurchasePrice</span>(); <span style="color: #3F7E5E">// *Point!</span>
     *         subCB.query().setPaymentCompleteFlg_Equal_True();
     *     }
     * });
     * </pre> 
     * @param scalarCBLambda The callback for sub-query of myself. (NotNull)
     * @return The decorator of ScalarCondition. e.g. you can use partition-by. (NotNull)
     */
    public HpSSQDecorator<CB> sum(SubQuery<CB> scalarCBLambda) {
        assertSubQuery(scalarCBLambda);
        final HpSSQOption<CB> option = createOption();
        _setupper.setup("sum", scalarCBLambda, option);
        return createDecorator(option);
    }

    /**
     * Set up the sub query of myself for the scalar 'avg'.
     * <pre>
     * cb.query().scalar_Equal().<span style="color: #CC4747">avg</span>(new SubQuery&lt;PurchaseCB&gt;() {
     *     public void query(PurchaseCB subCB) {
     *         subCB.specify().<span style="color: #CC4747">columnPurchasePrice</span>(); <span style="color: #3F7E5E">// *Point!</span>
     *         subCB.query().setPaymentCompleteFlg_Equal_True();
     *     }
     * });
     * </pre> 
     * @param scalarCBLambda The callback for sub-query of myself. (NotNull)
     * @return The decorator of ScalarCondition. e.g. you can use partition-by. (NotNull)
     */
    public HpSSQDecorator<CB> avg(SubQuery<CB> scalarCBLambda) {
        assertSubQuery(scalarCBLambda);
        final HpSSQOption<CB> option = createOption();
        _setupper.setup("avg", scalarCBLambda, option);
        return createDecorator(option);
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected HpSSQOption<CB> createOption() {
        return new HpSSQOption<CB>();
    }

    protected HpSSQDecorator<CB> createDecorator(HpSSQOption<CB> option) {
        return new HpSSQDecorator<CB>(option);
    }

    protected void assertSubQuery(SubQuery<?> scalarCBLambda) {
        if (scalarCBLambda == null) {
            String msg = "The argument 'scalarCBLambda' for ScalarCondition should not be null.";
            throw new IllegalArgumentException(msg);
        }
    }
}
