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
package org.dbflute.cbean.chelper.dbms;

import org.dbflute.cbean.ConditionBean;
import org.dbflute.cbean.ConditionQuery;
import org.dbflute.cbean.chelper.HpSDRFunction;
import org.dbflute.cbean.chelper.HpSDRSetupper;
import org.dbflute.cbean.coption.DerivedReferrerOptionFactory;
import org.dbflute.dbmeta.DBMetaProvider;

/**
 * The Oracle function for (Specify)DerivedReferrer. <br>
 * <span style="color: #CC4747">This is base implementation for #future needs.</span>
 * @param <REFERRER_CB> The type of referrer condition-bean.
 * @param <LOCAL_CQ> The type of local condition-query.
 * @author jflute
 * @since 1.1.0 (2014/10/15 Wednesday)
 */
public class HpSDRFunctionOracle<REFERRER_CB extends ConditionBean, LOCAL_CQ extends ConditionQuery>
        extends HpSDRFunction<REFERRER_CB, LOCAL_CQ> {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public HpSDRFunctionOracle(ConditionBean baseCB, LOCAL_CQ localCQ, HpSDRSetupper<REFERRER_CB, LOCAL_CQ> querySetupper,
            DBMetaProvider dbmetaProvider, DerivedReferrerOptionFactory sdrOpFactory) {
        super(baseCB, localCQ, querySetupper, dbmetaProvider, sdrOpFactory);
    }

    // ===================================================================================
    //                                                                            Function
    //                                                                            ========
    // nothing for now
}
