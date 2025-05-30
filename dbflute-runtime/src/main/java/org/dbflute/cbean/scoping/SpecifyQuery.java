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
package org.dbflute.cbean.scoping;

import org.dbflute.cbean.ConditionBean;

/**
 * The interface of specify-query.
 * <pre>
 * cb.columnQuery(new SpecifyQuery&lt;LandCB&gt;() {
 *     public void query(LandCB spCB) {
 *         spCB.specify().columnLand...();
 *     }
 * }).lessThan(new SpecifyQuery&lt;SeaCB&gt;() {
 *     public void query(SeaCB spCB) {
 *         spCB.specify().columnSea...();
 *     }
 * });
 * </pre>
 * @param <CB> The type of condition-bean for specification.
 * @author jflute
 */
@FunctionalInterface
public interface SpecifyQuery<CB extends ConditionBean> {

    /**
     * Specify your column for query. <br>
     * Don't call the method 'setupSelect_Xxx()' and 'query()' and 'addOrderBy_Xxx...()'
     * and they are ignored if you call.
     * @param spCB The condition-bean for specification. (NotNull)
     */
    void specify(CB spCB);
}
