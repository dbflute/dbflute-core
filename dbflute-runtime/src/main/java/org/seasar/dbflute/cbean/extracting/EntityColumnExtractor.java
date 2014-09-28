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
package org.seasar.dbflute.cbean.extracting;

/**
 * The extractor of column from entity.
 * @param <ENTITY> The type of entity.
 * @param <COLUMN> The type of column.
 * @author jflute
 */
public interface EntityColumnExtractor<ENTITY, COLUMN> {

    /**
     * Extract the column value from the entity.
     * @param entity Entity. (NotNull)
     * @return The value of the column. (NullAllowed: if null, skip the element)
     */
    COLUMN extract(ENTITY entity);
}
