/*
 * Copyright 2014-2025 the original author or authors.
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
package org.dbflute.cbean.chelper.dbms;

import org.dbflute.cbean.ConditionBean;
import org.dbflute.cbean.ConditionQuery;
import org.dbflute.cbean.chelper.HpSDRFunction;
import org.dbflute.cbean.chelper.HpSDRSetupper;
import org.dbflute.cbean.coption.DerivedReferrerOption;
import org.dbflute.cbean.coption.DerivedReferrerOptionFactory;
import org.dbflute.cbean.coption.FunctionFilterOptionCall;
import org.dbflute.cbean.scoping.SubQuery;
import org.dbflute.dbmeta.DBMetaProvider;

/**
 * The MySQL function for (Specify)DerivedReferrer. <br>
 * <span style="color: #CC4747">This is base implementation for #future needs.</span>
 * @param <REFERRER_CB> The type of referrer condition-bean.
 * @param <LOCAL_CQ> The type of local condition-query.
 * @author jflute
 * @since 1.1.0 (2014/10/15 Wednesday)
 */
public class HpSDRFunctionMySql<REFERRER_CB extends ConditionBean, LOCAL_CQ extends ConditionQuery>
        extends HpSDRFunction<REFERRER_CB, LOCAL_CQ> {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public HpSDRFunctionMySql(ConditionBean baseCB, LOCAL_CQ localCQ, HpSDRSetupper<REFERRER_CB, LOCAL_CQ> querySetupper,
            DBMetaProvider dbmetaProvider, DerivedReferrerOptionFactory sdrOpFactory) {
        super(baseCB, localCQ, querySetupper, dbmetaProvider, sdrOpFactory);
    }

    // ===================================================================================
    //                                                                            Function
    //                                                                            ========
    // -----------------------------------------------------
    //                                          Group Concat
    //                                          ------------
    // cannot specify separator and order-by
    // because only simple functions are supported in ConditionBean
    /**
     * Set up the sub query of referrer for the scalar 'group_concat'.
     * @param derivedCBLambda The callback for sub-query of referrer. (NotNull)
     * @param aliasName The alias of the name. The property should exists on the entity. (NotNull)
     */
    public void groupConcat(SubQuery<REFERRER_CB> derivedCBLambda, String aliasName) {
        doGroupConcat(derivedCBLambda, aliasName, null);
    }

    /**
     * Set up the sub query of referrer for the scalar 'group_concat'.
     * @param derivedCBLambda The callback for sub-query of referrer. (NotNull)
     * @param aliasName The alias of the name. The property should exists on the entity. (NotNull)
     * @param opLambda The callback for option of DerivedReferrer. e.g. you can use a coalesce function. (NotNull)
     */
    public void groupConcat(SubQuery<REFERRER_CB> derivedCBLambda, String aliasName,
            FunctionFilterOptionCall<DerivedReferrerOption> opLambda) {
        doGroupConcat(derivedCBLambda, aliasName, createDerivedReferrerOption(opLambda));
    }

    protected void doGroupConcat(SubQuery<REFERRER_CB> subQuery, String aliasName, DerivedReferrerOption option) {
        doUserDef(subQuery, aliasName, "group_concat", option);
    }
}
