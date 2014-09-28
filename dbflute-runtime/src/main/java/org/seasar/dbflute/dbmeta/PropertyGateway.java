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
package org.seasar.dbflute.dbmeta;

import org.seasar.dbflute.Entity;

/**
 * The interface of property gateway.
 * @author jflute
 * @since 0.9.9.3D (2012/03/26 Monday)
 */
public interface PropertyGateway {

    /**
     * Read the property value from the entity.
     * @param entity The target entity to read. (NotNull)
     * @return The read value. (NullAllowed)
     */
    Object read(Entity entity);

    /**
     * Write the property value to the entity.
     * @param entity The target entity to write. (NotNull)
     * @param value The written value. (NullAllowed)
     */
    void write(Entity entity, Object value);
}
