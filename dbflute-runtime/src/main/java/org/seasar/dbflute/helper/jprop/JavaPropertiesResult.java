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
package org.seasar.dbflute.helper.jprop;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.DfTypeUtil;

/**
 * @author jflute
 * @since 1.0.1 (2012/12/21 Friday)
 */
public class JavaPropertiesResult {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Properties _plainProp;
    protected final List<JavaPropertiesProperty> _propertyList; // merged list
    protected final Map<String, JavaPropertiesProperty> _propertyMap;
    protected final List<JavaPropertiesProperty> _propertyBasePointOnlyList;
    protected final List<JavaPropertiesProperty> _propertyExtendsOnlyList;
    protected final List<String> _duplicateKeyList;
    protected final JavaPropertiesResult _extendsPropResult;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * @param plainProp The plain properties as base point properties. (NotNull)
     * @param propertyList The list of property merged with extends-properties. (NotNull)
     * @param duplicateKeyList The list of duplicate property keys. (NotNull)
     */
    public JavaPropertiesResult(Properties plainProp, List<JavaPropertiesProperty> propertyList,
            List<String> duplicateKeyList) {
        this(plainProp, propertyList, duplicateKeyList, null);
    }

    /**
     * @param plainProp The plain properties as base point properties. (NotNull)
     * @param propertyList The list of property merged with extends-properties. (NotNull)
     * @param duplicateKeyList The list of duplicate property keys. (NotNull)
     * @param extendsPropResult The result for extends-properties. (NullAllowed: if null, no extends)
     */
    public JavaPropertiesResult(Properties plainProp, List<JavaPropertiesProperty> propertyList,
            List<String> duplicateKeyList, JavaPropertiesResult extendsPropResult) {
        _plainProp = plainProp;
        _propertyList = propertyList;
        _propertyMap = DfCollectionUtil.newLinkedHashMapSized(propertyList.size());
        _propertyBasePointOnlyList = DfCollectionUtil.newArrayList();
        _propertyExtendsOnlyList = DfCollectionUtil.newArrayList();
        for (JavaPropertiesProperty property : propertyList) {
            _propertyMap.put(property.getPropertyKey(), property);
            if (property.isExtends()) {
                _propertyExtendsOnlyList.add(property);
            } else {
                _propertyBasePointOnlyList.add(property);
            }
        }
        _duplicateKeyList = duplicateKeyList;
        _extendsPropResult = extendsPropResult;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof JavaPropertiesResult)) {
            return false;
        }
        final JavaPropertiesResult another = (JavaPropertiesResult) obj;
        return _propertyList.equals(another._propertyList);
    }

    @Override
    public int hashCode() {
        return _propertyList.hashCode();
    }

    @Override
    public String toString() {
        return DfTypeUtil.toClassTitle(this) + ":{" + _propertyMap.keySet() + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    /**
     * Get the plain properties as base point properties.
     * @return The properties object of Java standard. (NotNull)
     */
    public Properties getPlainProp() {
        return _plainProp;
    }

    /**
     * Get the property by the key.
     * @param propertyKey The key string of property. (NotNull)
     * @return The property object for Java properties. (NullAllowed: if null, means not found)
     */
    public JavaPropertiesProperty getProperty(String propertyKey) {
        return _propertyMap.get(propertyKey);
    }

    /**
     * Get the property list merged with extends-properties.
     * @return The list of property object. (NotNull)
     */
    public List<JavaPropertiesProperty> getPropertyList() {
        return _propertyList;
    }

    /**
     * Get the property map merged with extends-properties.
     * @return The map of property object, the key of map is property key. (NotNull)
     */
    public Map<String, JavaPropertiesProperty> getPropertyMap() {
        return _propertyMap;
    }

    public List<JavaPropertiesProperty> getPropertyBasePointOnlyList() {
        return _propertyBasePointOnlyList;
    }

    public List<JavaPropertiesProperty> getPropertyExtendsOnlyList() {
        return _propertyExtendsOnlyList;
    }

    /**
     * Get the list of duplicate property keys.
     * @return The list of duplicate property keys. (NotNull, EmptyAllowed)
     */
    public List<String> getDuplicateKeyList() {
        return _duplicateKeyList;
    }

    public JavaPropertiesResult getExtendsPropResult() {
        return _extendsPropResult;
    }
}
