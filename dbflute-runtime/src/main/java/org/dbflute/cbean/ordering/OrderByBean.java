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
package org.dbflute.cbean.ordering;

import org.dbflute.cbean.sqlclause.orderby.OrderByClause;

/**
 * The bean for order-by.
 * @author jflute
 */
public interface OrderByBean {

    /**
     * Get order-by clause.
     * @return Order-by clause. (NotNull)
     */
    String getOrderByClause();

    /**
     * Get the component of order-by clause.
     * @return The component of order-by clause. (NotNull)
     */
    OrderByClause getOrderByComponent();

    /**
     * Clear order-by.
     * @return this. (NotNull)
     */
    OrderByBean clearOrderBy();
}
