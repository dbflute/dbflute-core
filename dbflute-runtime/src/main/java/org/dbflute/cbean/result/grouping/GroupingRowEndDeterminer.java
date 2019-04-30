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
package org.dbflute.cbean.result.grouping;

/**
 * The determiner of grouping end (switch point).
 * @param <ENTITY> The type of entity.
 * @author jflute
 */
public interface GroupingRowEndDeterminer<ENTITY> {

    /**
     * Determine whether the grouping row is end.
     * @param rowResource The resource of grouping row. (NotNull and the property 'groupingRowList' is not empty and the property 'currentEntity' is not null)
     * @param nextEntity The entity of next element. (NotNull and the rowResource does not contain yet)
     * @return Whether the grouping row is end. (If the value is true, break grouping row and the nextEntity is registered to next row)
     */
    boolean determine(GroupingRowResource<ENTITY> rowResource, ENTITY nextEntity);
}
