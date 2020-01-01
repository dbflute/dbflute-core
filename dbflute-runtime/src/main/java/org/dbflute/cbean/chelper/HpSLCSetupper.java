/*
 * Copyright 2014-2020 the original author or authors.
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
import org.dbflute.cbean.coption.ScalarConditionOption;
import org.dbflute.cbean.scoping.SubQuery;

/**
 * The set-upper for ScalarCondition (the old name: ScalarSubQuery).
 * @param <CB> The type of condition-bean.
 * @author jflute
 */
public interface HpSLCSetupper<CB extends ConditionBean> {

    /**
     * Set up the scalar condition.
     * @param function The expression of function to derive the scalar value. (NotNull)
     * @param subQuery The sub query of myself. (NotNull)
     * @param customized The customized info of ScalarCondition. (NotNull)
     * @param option The option of ScalarCondition. (NotNull)
     */
    void setup(String function, SubQuery<CB> subQuery, HpSLCCustomized<CB> customized, ScalarConditionOption option);
}
