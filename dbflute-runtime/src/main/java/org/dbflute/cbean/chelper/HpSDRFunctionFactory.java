/*
 * Copyright 2014-2017 the original author or authors.
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
import org.dbflute.cbean.ConditionQuery;
import org.dbflute.dbmeta.DBMetaProvider;

/**
 * @author jflute
 * @since 1.1.0 (2014/10/15 Wednesday)
 */
@FunctionalInterface
public interface HpSDRFunctionFactory {

    /**
     * Create the function handler for (specify) derived-referrer.
     * @param <REFERRER_CB> The type of condition-bean for referrer.
     * @param <LOCAL_CQ> The type of condition-query for local table.
     * @param baseCB The condition-bean of base table. (NotNull)
     * @param localCQ The condition-query of local table. (NotNull)
     * @param querySetupper The set-upper of sub-query for (specify) derived-referrer. (NotNull)
     * @param dbmetaProvider The provider of DB meta. (NotNull)
     * @return The new-created function handler. (NotNull)
     */
    <REFERRER_CB extends ConditionBean, LOCAL_CQ extends ConditionQuery> HpSDRFunction<REFERRER_CB, LOCAL_CQ> create( //
            ConditionBean baseCB, LOCAL_CQ localCQ //
            , HpSDRSetupper<REFERRER_CB, LOCAL_CQ> querySetupper //
            , DBMetaProvider dbmetaProvider);
}
