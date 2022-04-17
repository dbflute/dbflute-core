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
package org.dbflute.cbean.scoping;

import org.dbflute.cbean.ConditionBean;

/**
 * The interface of and-query.
 * <pre>
 * cb.orScopeQuery(new OrQuery&lt;FooCB&gt;() {
 *     public void query(FooCB orCB) {
 *         orCB.query().setFoo...
 *         orCB.<span style="color: #CC4747">orScopeQueryAndPart</span>(new AndQuery&lt;FooCB&gt;() {
 *             public void query(FooCB andCB) {
 *                 andCB.query().setBar...
 *                 andCB.query().setQux...
 *             }
 *         });
 *     }
 * }
 * </pre>
 * @author jflute
 * @param <AND_CB> The type of condition-bean for and-query.
 */
@FunctionalInterface
public interface AndQuery<AND_CB extends ConditionBean> {

    /**
     * Set up your query condition for and-query. <br>
     * Don't call the method 'setupSelect_Xxx()' and 'addOrderBy_Xxx...()'
     * and they are ignored if you call.
     * @param andCB The condition-bean for and-query. (NotNull)
     */
    void query(AND_CB andCB);
}
