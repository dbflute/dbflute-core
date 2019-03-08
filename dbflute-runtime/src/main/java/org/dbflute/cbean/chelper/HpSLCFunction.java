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
import org.dbflute.cbean.coption.FunctionFilterOptionCall;
import org.dbflute.cbean.coption.ScalarConditionOption;
import org.dbflute.cbean.scoping.SubQuery;

/**
 * The function for ScalarCondition (the old name: ScalarSubQuery).
 * @param <CB> The type of condition-bean.
 * @author jflute
 */
public class HpSLCFunction<CB extends ConditionBean> {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final HpSLCSetupper<CB> _setupper;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public HpSLCFunction(HpSLCSetupper<CB> setupper) {
        _setupper = setupper;
    }

    // ===================================================================================
    //                                                                            Function
    //                                                                            ========
    // -----------------------------------------------------
    //                                               Maximum
    //                                               -------
    /**
     * Set up the sub query of myself for the scalar 'max'.
     * <pre>
     * cb.query().scalar_Equal().<span style="color: #CC4747">max</span>(<span style="color: #553000">purchaseCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">purchaseCB</span>.specify().<span style="color: #CC4747">columnPurchasePrice</span>(); <span style="color: #3F7E5E">// *Point!</span>
     *     <span style="color: #553000">purchaseCB</span>.query().setPaymentCompleteFlg_Equal_True();
     * });
     * </pre> 
     * @param scalarCBLambda The callback for sub-query of myself. (NotNull)
     * @return The decorator of ScalarCondition. e.g. you can use partition-by. (NotNull)
     */
    public HpSLCDecorator<CB> max(SubQuery<CB> scalarCBLambda) {
        return doMax(scalarCBLambda, null);
    }

    /**
     * Set up the sub query of myself for the scalar 'max'.
     * <pre>
     * cb.query().scalar_Equal().<span style="color: #CC4747">max</span>(<span style="color: #553000">purchaseCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">purchaseCB</span>.specify().<span style="color: #CC4747">columnPurchasePrice</span>(); <span style="color: #3F7E5E">// *Point!</span>
     *     <span style="color: #553000">purchaseCB</span>.query().setPaymentCompleteFlg_Equal_True();
     * }, <span style="color: #553000">op</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> <span style="color: #553000">op</span>.<span style="color: #CC4747">coalesce</span>(0));
     * </pre> 
     * @param scalarCBLambda The callback for sub-query of myself. (NotNull)
     * @param opLambda The callback for option of scalar. (NotNull)
     * @return The decorator of ScalarCondition. e.g. you can use partition-by. (NotNull)
     */
    public HpSLCDecorator<CB> max(SubQuery<CB> scalarCBLambda, FunctionFilterOptionCall<ScalarConditionOption> opLambda) {
        return doMax(scalarCBLambda, prepareOption(opLambda));
    }

    protected HpSLCDecorator<CB> doMax(SubQuery<CB> scalarCBLambda, ScalarConditionOption option) {
        final HpSLCCustomized<CB> customized = createCustomized();
        setupScalarCondition("max", scalarCBLambda, customized, option);
        return createDecorator(customized);
    }

    // -----------------------------------------------------
    //                                               Minimum
    //                                               -------
    /**
     * Set up the sub query of myself for the scalar 'min'.
     * <pre>
     * cb.query().scalar_Equal().<span style="color: #CC4747">min</span>(<span style="color: #553000">purchaseCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">purchaseCB</span>.specify().<span style="color: #CC4747">columnPurchasePrice</span>(); <span style="color: #3F7E5E">// *Point!</span>
     *     <span style="color: #553000">purchaseCB</span>.query().setPaymentCompleteFlg_Equal_True();
     * });
     * </pre> 
     * @param scalarCBLambda The callback for sub-query of myself. (NotNull)
     * @return The decorator of ScalarCondition. e.g. you can use partition-by. (NotNull)
     */
    public HpSLCDecorator<CB> min(SubQuery<CB> scalarCBLambda) {
        return doMin(scalarCBLambda, null);
    }

    /**
     * Set up the sub query of myself for the scalar 'min'.
     * <pre>
     * cb.query().scalar_Equal().<span style="color: #CC4747">min</span>(<span style="color: #553000">purchaseCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">purchaseCB</span>.specify().<span style="color: #CC4747">columnPurchasePrice</span>(); <span style="color: #3F7E5E">// *Point!</span>
     *     <span style="color: #553000">purchaseCB</span>.query().setPaymentCompleteFlg_Equal_True();
     * }, <span style="color: #553000">op</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> <span style="color: #553000">op</span>.<span style="color: #CC4747">coalesce</span>(0));
     * </pre> 
     * @param scalarCBLambda The callback for sub-query of myself. (NotNull)
     * @param opLambda The callback for option of scalar. (NotNull)
     * @return The decorator of ScalarCondition. e.g. you can use partition-by. (NotNull)
     */
    public HpSLCDecorator<CB> min(SubQuery<CB> scalarCBLambda, FunctionFilterOptionCall<ScalarConditionOption> opLambda) {
        return doMin(scalarCBLambda, prepareOption(opLambda));
    }

    protected HpSLCDecorator<CB> doMin(SubQuery<CB> scalarCBLambda, ScalarConditionOption option) {
        final HpSLCCustomized<CB> customized = createCustomized();
        setupScalarCondition("min", scalarCBLambda, customized, option);
        return createDecorator(customized);
    }

    // -----------------------------------------------------
    //                                               Summary
    //                                               -------
    /**
     * Set up the sub query of myself for the scalar 'sum'.
     * <pre>
     * cb.query().scalar_Equal().<span style="color: #CC4747">sum</span>(<span style="color: #553000">purchaseCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">purchaseCB</span>.specify().<span style="color: #CC4747">columnPurchasePrice</span>(); <span style="color: #3F7E5E">// *Point!</span>
     *     <span style="color: #553000">purchaseCB</span>.query().setPaymentCompleteFlg_Equal_True();
     * });
     * </pre> 
     * @param scalarCBLambda The callback for sub-query of myself. (NotNull)
     * @return The decorator of ScalarCondition. e.g. you can use partition-by. (NotNull)
     */
    public HpSLCDecorator<CB> sum(SubQuery<CB> scalarCBLambda) {
        return doSum(scalarCBLambda, null);
    }

    /**
     * Set up the sub query of myself for the scalar 'sum'.
     * <pre>
     * cb.query().scalar_Equal().<span style="color: #CC4747">sum</span>(<span style="color: #553000">purchaseCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">purchaseCB</span>.specify().<span style="color: #CC4747">columnPurchasePrice</span>(); <span style="color: #3F7E5E">// *Point!</span>
     *     <span style="color: #553000">purchaseCB</span>.query().setPaymentCompleteFlg_Equal_True();
     * }, <span style="color: #553000">op</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> <span style="color: #553000">op</span>.<span style="color: #CC4747">coalesce</span>(0));
     * </pre> 
     * @param scalarCBLambda The callback for sub-query of myself. (NotNull)
     * @param opLambda The callback for option of scalar. (NotNull)
     * @return The decorator of ScalarCondition. e.g. you can use partition-by. (NotNull)
     */
    public HpSLCDecorator<CB> sum(SubQuery<CB> scalarCBLambda, FunctionFilterOptionCall<ScalarConditionOption> opLambda) {
        return doSum(scalarCBLambda, prepareOption(opLambda));
    }

    protected HpSLCDecorator<CB> doSum(SubQuery<CB> scalarCBLambda, ScalarConditionOption option) {
        final HpSLCCustomized<CB> customized = createCustomized();
        setupScalarCondition("sum", scalarCBLambda, customized, option);
        return createDecorator(customized);
    }

    // -----------------------------------------------------
    //                                               Average
    //                                               -------
    /**
     * Set up the sub query of myself for the scalar 'avg'.
     * <pre>
     * cb.query().scalar_Equal().<span style="color: #CC4747">avg</span>(<span style="color: #553000">purchaseCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">purchaseCB</span>.specify().<span style="color: #CC4747">columnPurchasePrice</span>(); <span style="color: #3F7E5E">// *Point!</span>
     *     <span style="color: #553000">purchaseCB</span>.query().setPaymentCompleteFlg_Equal_True();
     * });
     * </pre> 
     * @param scalarCBLambda The callback for sub-query of myself. (NotNull)
     * @return The decorator of ScalarCondition. e.g. you can use partition-by. (NotNull)
     */
    public HpSLCDecorator<CB> avg(SubQuery<CB> scalarCBLambda) {
        return doAvg(scalarCBLambda, null);
    }

    /**
     * Set up the sub query of myself for the scalar 'avg'.
     * <pre>
     * cb.query().scalar_Equal().<span style="color: #CC4747">avg</span>(<span style="color: #553000">purchaseCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">purchaseCB</span>.specify().<span style="color: #CC4747">columnPurchasePrice</span>(); <span style="color: #3F7E5E">// *Point!</span>
     *     <span style="color: #553000">purchaseCB</span>.query().setPaymentCompleteFlg_Equal_True();
     * }, <span style="color: #553000">op</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> <span style="color: #553000">op</span>.<span style="color: #CC4747">coalesce</span>(0));
     * </pre> 
     * @param scalarCBLambda The callback for sub-query of myself. (NotNull)
     * @param opLambda The callback for option of scalar. (NotNull)
     * @return The decorator of ScalarCondition. e.g. you can use partition-by. (NotNull)
     */
    public HpSLCDecorator<CB> avg(SubQuery<CB> scalarCBLambda, FunctionFilterOptionCall<ScalarConditionOption> opLambda) {
        return doAvg(scalarCBLambda, prepareOption(opLambda));
    }

    protected HpSLCDecorator<CB> doAvg(SubQuery<CB> scalarCBLambda, ScalarConditionOption option) {
        final HpSLCCustomized<CB> customized = createCustomized();
        setupScalarCondition("avg", scalarCBLambda, customized, option);
        return createDecorator(customized);
    }

    // ===================================================================================
    //                                                                              Set up
    //                                                                              ======
    protected void setupScalarCondition(String function, SubQuery<CB> scalarCBLambda, HpSLCCustomized<CB> customized,
            ScalarConditionOption option) {
        assertSubQuery(scalarCBLambda);
        _setupper.setup(function, scalarCBLambda, customized, option);
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected HpSLCCustomized<CB> createCustomized() {
        return new HpSLCCustomized<CB>();
    }

    protected HpSLCDecorator<CB> createDecorator(HpSLCCustomized<CB> option) {
        return new HpSLCDecorator<CB>(option);
    }

    protected ScalarConditionOption prepareOption(FunctionFilterOptionCall<ScalarConditionOption> opLambda) {
        assertObjectNotNull("opLambda", opLambda);
        final ScalarConditionOption option = createScalarConditionOption();
        opLambda.callback(option);
        assertScalarConditionOption(option);
        return option;
    }

    protected ScalarConditionOption createScalarConditionOption() {
        return newScalarConditionOption();
    }

    protected ScalarConditionOption newScalarConditionOption() {
        return new ScalarConditionOption();
    }

    protected void assertSubQuery(SubQuery<?> scalarCBLambda) {
        if (scalarCBLambda == null) {
            String msg = "The argument 'scalarCBLambda' for ScalarCondition should not be null.";
            throw new IllegalArgumentException(msg);
        }
    }

    protected void assertScalarConditionOption(ScalarConditionOption option) {
        if (option == null) {
            String msg = "The argument 'option' for (Myself)ScalarCondition should not be null.";
            throw new IllegalArgumentException(msg);
        }
    }

    protected void assertObjectNotNull(String variableName, Object value) {
        if (variableName == null) {
            String msg = "The value should not be null: variableName=null value=" + value;
            throw new IllegalArgumentException(msg);
        }
        if (value == null) {
            String msg = "The value should not be null: variableName=" + variableName;
            throw new IllegalArgumentException(msg);
        }
    }
}
