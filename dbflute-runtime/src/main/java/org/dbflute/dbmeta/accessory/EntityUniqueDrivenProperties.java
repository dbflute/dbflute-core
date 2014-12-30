/*
 * Copyright 2014-2015 the original author or authors.
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
package org.dbflute.dbmeta.accessory;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * The unique-driven properties of entity. (basically for Framework)
 * @author jflute
 * @since 1.1.0 (2014/10/29 Monday)
 */
public class EntityUniqueDrivenProperties implements Serializable {

    /** The serial version UID for object serialization. (Default) */
    private static final long serialVersionUID = 1L;

    /** The set of property names. (NullAllowed: lazy-loaded) */
    protected Set<String> _propertyNameSet;

    /**
     * Add property name. (according to Java Beans rule)
     * @param propertyName The string for name. (NotNull)
     */
    public void addPropertyName(String propertyName) {
        getPropertyNameSet().add(propertyName);
    }

    /**
     * Get the set of properties.
     * @return The set of properties. (NotNull)
     */
    public Set<String> getPropertyNames() {
        return getPropertyNameSet();
    }

    /**
     * Is the set of properties empty?
     * @return The determination, true or false.
     */
    public boolean isEmpty() {
        return getPropertyNameSet().isEmpty();
    }

    /**
     * Clear the set of properties.
     */
    public void clear() {
        getPropertyNameSet().clear();
    }

    /**
     * Remove property name from the set. (according to Java Beans rule)
     * @param propertyName The string for name. (NotNull)
     */
    public void remove(String propertyName) {
        getPropertyNameSet().remove(propertyName);
    }

    /**
     * Accept specified properties. (after clearing this properties)
     * @param properties The properties as copy-resource. (NotNull)
     */
    public void accept(EntityModifiedProperties properties) {
        clear();
        for (String propertyName : properties.getPropertyNames()) {
            addPropertyName(propertyName);
        }
    }

    protected Set<String> getPropertyNameSet() {
        if (_propertyNameSet == null) {
            _propertyNameSet = new LinkedHashSet<String>(2);
        }
        return _propertyNameSet;
    }

    @Override
    public String toString() {
        return "uniqueDriven:" + _propertyNameSet;
    }
}
