/*
 * Copyright 2014-2014 the original author or authors.
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
package org.dbflute.dbmeta.property;

import org.dbflute.Entity;

/**
 * The interface of property writing.
 * @author jflute
 * @since 1.1.0 (2014/10/24 Friday)
 */
@FunctionalInterface
public interface PropertyWriter {

    /**
     * Write the property value to the entity.
     * @param entity The target entity to write. (NotNull)
     * @param value The written value. (NullAllowed)
     */
    void write(Entity entity, Object value);
}
