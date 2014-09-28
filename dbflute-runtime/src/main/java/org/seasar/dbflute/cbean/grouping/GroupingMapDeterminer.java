/*
 * Copyright 2004-2014 the Seasar Foundation and the Others.
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
package org.seasar.dbflute.cbean.grouping;

/**
 * The determiner of grouping map.
 * @param <ENTITY> The type of entity.
 * @author jflute
 */
public interface GroupingMapDeterminer<ENTITY> {

    /**
     * Provide grouping key for the entity.
     * @param entity The entity for grouping key. (NotNull)
     * @return The grouping key for the entity. (NotNull)
     */
    String provideKey(ENTITY entity);
}
