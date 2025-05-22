/*
 * Copyright 2014-2025 the original author or authors.
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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.dbflute.util.DfCollectionUtil;

/**
 * The modified properties of entity. (basically for Framework)
 * @author jflute
 * @since 1.1.0 (2014/10/29 Monday)
 */
public class EntityModifiedProperties implements Serializable, Cloneable {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The serial version UID for object serialization. (Default) */
    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The set of property names. (NullAllowed: lazy-loaded) */
    protected Set<String> _propertyNameSet;

    // ===================================================================================
    //                                                                   Property Handling
    //                                                                   =================
    /**
     * Add property name. (according to Java Beans rule)
     * @param propertyName The string for name. (NotNull)
     */
    public void addPropertyName(String propertyName) {
        assertPropertyNameNotNull(propertyName);
        getPropertyNameSet().add(propertyName);
    }

    protected void assertPropertyNameNotNull(String propertyName) {
        if (propertyName == null) {
            throw new IllegalArgumentException("The argument 'propertyName' should not be null.");
        }
    }

    /**
     * Get the set of properties.
     * @return The set of properties, read-only. (NotNull)
     */
    public Set<String> getPropertyNames() {
        if (_propertyNameSet != null) {
            return Collections.unmodifiableSet(_propertyNameSet);
        }
        return DfCollectionUtil.emptySet();
    }

    /**
     * Is the property modified?
     * @param propertyName The name of property. (NotNull)
     * @return The determination, true or false.
     */
    public boolean isModifiedProperty(String propertyName) {
        return _propertyNameSet != null && _propertyNameSet.contains(propertyName);
    }

    /**
     * Is the set of properties empty?
     * @return The determination, true or false.
     */
    public boolean isEmpty() {
        return _propertyNameSet == null || getPropertyNameSet().isEmpty();
    }

    /**
     * Clear the set of properties.
     */
    public void clear() {
        if (_propertyNameSet != null) {
            getPropertyNameSet().clear();
        }
    }

    /**
     * Remove property name from the set. (according to Java Beans rule)
     * @param propertyName The string for name. (NotNull)
     */
    public void remove(String propertyName) {
        assertPropertyNameNotNull(propertyName);
        if (_propertyNameSet != null) {
            getPropertyNameSet().remove(propertyName);
        }
    }

    /**
     * Accept specified properties. (after clearing this properties)
     * @param properties The properties as copy-resource. (NotNull)
     */
    public void accept(EntityModifiedProperties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("The argument 'properties' should not be null.");
        }
        clear();
        for (String propertyName : properties.getPropertyNames()) {
            addPropertyName(propertyName);
        }
    }

    protected Set<String> getPropertyNameSet() {
        if (_propertyNameSet == null) {
            _propertyNameSet = newPropertyNameSet();
        }
        return _propertyNameSet;
    }

    protected LinkedHashSet<String> newPropertyNameSet() {
        return new LinkedHashSet<String>();
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return "modifiedProp:" + _propertyNameSet;
    }

    @Override
    public EntityModifiedProperties clone() { // deep copy
        try {
            final EntityModifiedProperties cloned = (EntityModifiedProperties) super.clone();
            if (_propertyNameSet != null) {
                final Set<String> copied = newPropertyNameSet();
                copied.addAll(_propertyNameSet);
                cloned._propertyNameSet = copied;
            }
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Failed to clone the properties: " + toString(), e);
        }
    }
}
