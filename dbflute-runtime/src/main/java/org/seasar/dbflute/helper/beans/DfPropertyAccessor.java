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
package org.seasar.dbflute.helper.beans;

import org.seasar.dbflute.helper.beans.exception.DfBeanIllegalPropertyException;

/**
 * @author jflute
 */
public interface DfPropertyAccessor {

    /**
     * Get the name of the property.
     * @return The string expression of the property. (NotNull)
     */
    String getPropertyName();

    /**
     * Get the type of the property.
     * @return The class type of the property. (NotNull)
     */
    Class<?> getPropertyType();

    /**
     * Get the (first) generic type of the property type if it exists.
     * @return The class type for the generic type. (NullAllowed: when no generic or unknown)
     */
    Class<?> getGenericType();

    /**
     * @param target The target instance. (NullAllowed)
     * @return The value of the property. (NullAllowed)
     * @throws DfBeanIllegalPropertyException When the property of bean is illegal to get value.
     */
    Object getValue(Object target);

    /**
     * @param target The target instance. (NullAllowed)
     * @param value The value of the property. (NullAllowed)
     * @throws DfBeanIllegalPropertyException When the property of bean is illegal to set value.
     */
    void setValue(Object target, Object value);

    /**
     * Is the property readable?
     * @return The determination, true or false.
     */
    boolean isReadable();

    /**
     * Is the property writable?
     * @return The determination, true or false.
     */
    boolean isWritable();
}
