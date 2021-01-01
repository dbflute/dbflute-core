/*
 * Copyright 2014-2021 the original author or authors.
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
package org.dbflute.properties.assistant.base;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.dbflute.infra.core.DfEnvironmentType;
import org.dbflute.properties.propreader.DfOutsideListPropReader;
import org.dbflute.properties.propreader.DfOutsideMapPropReader;
import org.dbflute.properties.propreader.DfOutsideStringPropReader;
import org.dbflute.util.DfPropertyUtil;
import org.dbflute.util.DfPropertyUtil.PropertyBooleanFormatException;
import org.dbflute.util.DfPropertyUtil.PropertyIntegerFormatException;
import org.dbflute.util.DfPropertyUtil.PropertyNotFoundException;
import org.dbflute.util.DfStringUtil;
import org.dbflute.util.DfTypeUtil;

/**
 * @author jflute
 * @since 1.1.7 (2018/03/17 Saturday)
 */
public class DfPropertyValueHandler {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Properties _buildProperties;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfPropertyValueHandler(Properties buildProperties) {
        _buildProperties = buildProperties;
    }

    // ===================================================================================
    //                                                                    TopLevel Handler
    //                                                                    ================
    // -----------------------------------------------------
    //                                                String
    //                                                ------
    /**
     * Get property as string. {Delegate method}
     * @param key Property-key. (NotNull)
     * @return Property as string. (NotNull, EmptyAllowed: if not found)
     */
    public String stringProp(String key) {
        final String outsidePropString = getOutsideStringProp(key);
        if (outsidePropString != null && outsidePropString.trim().length() > 0) {
            return outsidePropString;
        }
        return DfPropertyUtil.stringProp(_buildProperties, key);
    }

    /**
     * Get property as string. {Delegate method}
     * @param key Property-key. (NotNull)
     * @param defaultValue Default value. (NullAllowed)
     * @return Property as string. (NullAllowed: If the default-value is null)
     */
    public String stringProp(String key, String defaultValue) {
        try {
            final String outsidePropString = getOutsideStringProp(key);
            if (outsidePropString != null && outsidePropString.trim().length() > 0) {
                return outsidePropString;
            }
            return DfPropertyUtil.stringProp(_buildProperties, key);
        } catch (PropertyNotFoundException e) {
            return defaultValue;
        }
    }

    /**
     * Get property as string. {Delegate method}
     * @param key Property-key. (NotNull)
     * @param defaultValue Default value. (NullAllowed)
     * @return Property as string. (NullAllowed: If the default-value is null)
     */
    public String stringPropNoEmpty(String key, String defaultValue) {
        try {
            final String outsidePropString = getOutsideStringProp(key);
            if (outsidePropString != null && outsidePropString.trim().length() > 0) {
                return outsidePropString;
            }
            final String value = DfPropertyUtil.stringProp(_buildProperties, key);
            if (value != null && value.trim().length() != 0) {
                return value;
            }
            return defaultValue;
        } catch (PropertyNotFoundException e) {
            return defaultValue;
        }
    }

    // -----------------------------------------------------
    //                                               Boolean
    //                                               -------
    /**
     * Get property as boolean. {Delegate method}
     * @param key Property-key. (NotNull)
     * @return Property as boolean.
     */
    public boolean booleanProp(String key) {
        return DfPropertyUtil.booleanProp(_buildProperties, key);
    }

    /**
     * Get property as boolean. {Delegate method}
     * @param key Property-key. (NotNull)
     * @param defaultValue Default value.
     * @return Property as boolean.
     */
    public boolean booleanProp(String key, boolean defaultValue) {
        try {
            return DfPropertyUtil.booleanProp(_buildProperties, key);
        } catch (PropertyNotFoundException e) {
            return defaultValue;
        } catch (PropertyBooleanFormatException e) {
            return defaultValue;
        }
    }

    // -----------------------------------------------------
    //                                               Integer
    //                                               -------
    /**
     * Get property as integer. {Delegate method}
     * @param key Property-key. (NotNull)
     * @return Property as integer.
     */
    public int intProp(String key) {
        return DfPropertyUtil.intProp(_buildProperties, key);
    }

    /**
     * Get property as integer. {Delegate method}
     * @param key Property-key. (NotNull)
     * @param defaultValue Default value.
     * @return Property as integer.
     */
    public int intProp(String key, int defaultValue) {
        try {
            return DfPropertyUtil.intProp(_buildProperties, key);
        } catch (PropertyNotFoundException e) {
            return defaultValue;
        } catch (PropertyIntegerFormatException e) {
            return defaultValue;
        }
    }

    // -----------------------------------------------------
    //                                                  List
    //                                                  ----
    /**
     * Get property as list. {Delegate method}
     * @param key Property-key. (NotNull)
     * @return Property as list. (NotNull)
     */
    public List<Object> listProp(String key) {
        final List<Object> outsidePropList = getOutsideListProp(key);
        if (!outsidePropList.isEmpty()) {
            return outsidePropList;
        }
        return DfPropertyUtil.listProp(_buildProperties, key);
    }

    /**
     * Get property as list. {Delegate method}
     * @param key Property-key. (NotNull)
     * @param defaultValue Default value. (NullAllowed)
     * @return Property as list. (NullAllowed: If the default-value is null)
     */
    public List<Object> listProp(String key, List<Object> defaultValue) {
        try {
            final List<Object> outsidePropList = getOutsideListProp(key);
            if (!outsidePropList.isEmpty()) {
                return outsidePropList;
            }
            final List<Object> result = DfPropertyUtil.listProp(_buildProperties, key);
            if (result.isEmpty()) {
                return defaultValue;
            } else {
                return result;
            }
        } catch (PropertyNotFoundException ignored) {
            return defaultValue;
        }
    }

    // -----------------------------------------------------
    //                                                   Map
    //                                                   ---
    /**
     * Get property as map. {Delegate method}
     * @param key Property-key. (NotNull)
     * @return Property as map. (NotNull)
     */
    public Map<String, Object> mapProp(String key) {
        final Map<String, Object> outsidePropMap = getOutsideMapProp(key);
        if (!outsidePropMap.isEmpty()) {
            return outsidePropMap;
        }
        return DfPropertyUtil.mapProp(_buildProperties, key);
    }

    /**
     * Get property as map. {Delegate method}
     * @param key Property-key. (NotNull)
     * @param defaultValue Default value. (NullAllowed)
     * @return Property as map. (NullAllowed: If the default-value is null)
     */
    public Map<String, Object> mapProp(String key, Map<String, Object> defaultValue) {
        try {
            final Map<String, Object> outsidePropMap = getOutsideMapProp(key);
            if (!outsidePropMap.isEmpty()) {
                return outsidePropMap;
            }
            final Map<String, Object> result = DfPropertyUtil.mapProp(_buildProperties, key);
            if (result.isEmpty()) {
                return defaultValue;
            } else {
                return result;
            }
        } catch (PropertyNotFoundException ignored) {
            return defaultValue;
        }
    }

    // ===================================================================================
    //                                                                     Outside Handler
    //                                                                     ===============
    public String getOutsideStringProp(String key) {
        final DfOutsideStringPropReader reader = createOutsideStringPropReader();
        final String propName = DfStringUtil.replace(key, "torque.", "");
        final String path = "./dfprop/" + propName + ".dfprop";
        return reader.readString(path, getEnvironmentType());
    }

    protected DfOutsideStringPropReader createOutsideStringPropReader() {
        return new DfOutsideStringPropReader();
    }

    public Map<String, Object> getOutsideMapProp(String key) { // used by outer
        final DfOutsideMapPropReader reader = createOutsideMapPropReader();
        final String propName = DfStringUtil.replace(key, "torque.", "");
        final String path = "./dfprop/" + propName + ".dfprop";
        return reader.readMap(path, getEnvironmentType());
    }

    protected DfOutsideMapPropReader createOutsideMapPropReader() {
        return new DfOutsideMapPropReader();
    }

    public List<Object> getOutsideListProp(String key) {
        final DfOutsideListPropReader reader = createOutsideListPropReader();
        final String propName = DfStringUtil.replace(key, "torque.", "");
        final String path = "./dfprop/" + propName + ".dfprop";
        return reader.readList(path, getEnvironmentType());
    }

    protected DfOutsideListPropReader createOutsideListPropReader() {
        return new DfOutsideListPropReader();
    }

    /**
     * @return The type of environment. (NullAllowed: if null, means non-specified type)
     */
    protected String getEnvironmentType() {
        return DfEnvironmentType.getInstance().getEnvironmentType();
    }

    // ===================================================================================
    //                                                                    Flexible Handler
    //                                                                    ================
    // -----------------------------------------------------
    //                                                String
    //                                                ------
    public String getProperty(String key, String defaultValue, Map<String, ? extends Object> map) {
        final Object obj = map.get(key);
        if (obj != null) {
            if (!(obj instanceof String)) {
                String msg = "The key's value should be string:";
                msg = msg + " " + DfTypeUtil.toClassTitle(obj) + "=" + obj;
                throw new IllegalStateException(msg);
            }
            String value = (String) obj;
            if (value.trim().length() > 0) {
                return value;
            } else {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public String getPropertyIfNotBuildProp(String key, String defaultValue, Map<String, ? extends Object> map) {
        final Object obj = map.get(key);
        if (obj != null) {
            if (!(obj instanceof String)) {
                String msg = "The key's value should be string:";
                msg = msg + " " + DfTypeUtil.toClassTitle(obj) + "=" + obj;
                throw new IllegalStateException(msg);
            }
            String value = (String) obj;
            if (value.trim().length() > 0) {
                return value;
            } else {
                return defaultValue;
            }
        }
        return stringProp("torque." + key, defaultValue);
    }

    // -----------------------------------------------------
    //                                               Boolean
    //                                               -------
    public boolean isProperty(String key, boolean defaultValue, Map<String, ? extends Object> map) {
        Object obj = map.get(key);
        if (obj == null) {
            final String anotherKey = deriveBooleanAnotherKey(key);
            if (anotherKey != null) {
                obj = map.get(anotherKey);
            }
        }
        if (obj != null) {
            if (!(obj instanceof String)) {
                String msg = "The key's value should be boolean:";
                msg = msg + " " + DfTypeUtil.toClassTitle(obj) + "=" + obj;
                throw new IllegalStateException(msg);
            }
            String value = (String) obj;
            if (value.trim().length() > 0) {
                return value.trim().equalsIgnoreCase("true");
            } else {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public boolean isPropertyIfNotExistsFromBuildProp(String key, boolean defaultValue, Map<String, ? extends Object> map) {
        Object obj = map.get(key);
        if (obj == null) {
            final String anotherKey = deriveBooleanAnotherKey(key);
            if (anotherKey != null) {
                obj = map.get(anotherKey);
            }
        }
        if (obj != null) {
            if (!(obj instanceof String)) {
                String msg = "The key's value should be boolean:";
                msg = msg + " " + DfTypeUtil.toClassTitle(obj) + "=" + obj;
                throw new IllegalStateException(msg);
            }
            String value = (String) obj;
            if (value.trim().length() > 0) {
                return value.trim().equalsIgnoreCase("true");
            } else {
                return defaultValue;
            }
        }
        return booleanProp("torque." + key, defaultValue);
    }

    protected String deriveBooleanAnotherKey(String key) {
        return DfPropertyUtil.deriveBooleanAnotherKey(key);
    }
}
