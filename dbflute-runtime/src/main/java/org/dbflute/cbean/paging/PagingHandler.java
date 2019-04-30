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
package org.dbflute.cbean.paging;

import java.util.List;

/**
 * The handler of paging.
 * @param <ENTITY> The type of entity.
 * @author jflute
 */
public interface PagingHandler<ENTITY> {

    /**
     * Get the bean of paging.
     * @return The bean of paging. (NotNull)
     */
    PagingBean getPagingBean();

    /**
     * Execute SQL for count.
     * @return The count of execution.
     */
    int count();

    /**
     * Execute SQL for paging.
     * @return The list of entity. (NotNull)
     */
    List<ENTITY> paging();
}
