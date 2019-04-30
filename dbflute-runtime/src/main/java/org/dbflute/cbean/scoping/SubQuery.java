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
package org.dbflute.cbean.scoping;

import org.dbflute.cbean.ConditionBean;

/**
 * The interface of sub-query.
 * <pre>
 * cb.query.existsPurchase(purchaseCB <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
 *     purchaseCB.query().set...
 * }
 * </pre>
 * @author jflute
 * @param <SUB_CB> The type of condition-bean for sub-query.
 */
@FunctionalInterface
public interface SubQuery<SUB_CB extends ConditionBean> {

    /**
     * Set up your query condition for sub-query. <br>
     * Don't call the method 'setupSelect_Xxx()' and 'addOrderBy_Xxx...()'
     * and they are ignored if you call.
     * @param subCB The condition-bean for sub-query. (NotNull)
     */
    void query(SUB_CB subCB);
}
