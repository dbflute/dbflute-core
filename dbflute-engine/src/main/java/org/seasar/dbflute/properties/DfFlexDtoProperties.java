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
package org.seasar.dbflute.properties;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.seasar.dbflute.util.DfCollectionUtil;

/**
 * @author jflute
 */
public final class DfFlexDtoProperties extends DfAbstractHelperProperties {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfFlexDtoProperties(Properties prop) {
        super(prop);
    }

    // ===================================================================================
    //                                                                      Definition Map
    //                                                                      ==============
    protected Map<String, Object> _flexDtoDefinitionMap;

    protected Map<String, Object> getFlexDtoDefinitionMap() {
        if (_flexDtoDefinitionMap == null) {
            final Map<String, Object> map = mapProp("torque.flexDtoDefinitionMap", DEFAULT_EMPTY_MAP);
            _flexDtoDefinitionMap = newLinkedHashMap();
            _flexDtoDefinitionMap.putAll(map);
        }
        return _flexDtoDefinitionMap;
    }

    // ===================================================================================
    //                                                                       Determination
    //                                                                       =============
    public boolean hasFlexDtoDefinition() {
        return !getFlexDtoDefinitionMap().isEmpty();
    }

    // ===================================================================================
    //                                                                    Output Directory
    //                                                                    ================
    /**
     * @return The directory for output. (NotNull)
     */
    public String getOutputDirectory() {
        final String baseDir = getBasicProperties().getGenerateOutputDirectory();
        final String value = (String) getFlexDtoDefinitionMap().get("outputDirectory");
        return value != null && value.trim().length() > 0 ? baseDir + "/" + value : baseDir;
    }

    // ===================================================================================
    //                                                                          Native Map
    //                                                                          ==========
    protected Map<String, String> _javaToFlexNativeMap;

    public Map<String, String> getJavaToFlexNativeMap() {
        if (_javaToFlexNativeMap != null) {
            return _javaToFlexNativeMap;
        }
        final Map<String, Object> map = getDtoPropertyMap("javaToFlexNativeMap");
        final Map<String, String> resultMap = newLinkedHashMap();
        if (map != null) {
            for (Entry<String, Object> entry : map.entrySet()) {
                resultMap.put(entry.getKey(), (String) entry.getValue());
            }
        }
        _javaToFlexNativeMap = resultMap;
        return _javaToFlexNativeMap;
    }

    // ===================================================================================
    //                                                                       Target/Except
    //                                                                       =============
    protected List<String> _bindableTableTargetList;

    public List<String> getBindableTableTargetList() {
        if (_bindableTableTargetList != null) {
            return _bindableTableTargetList;
        }
        final List<String> targetList = getDtoPropertyList("bindableTableTargetList");
        final List<String> resultList = DfCollectionUtil.newArrayList();
        if (targetList != null) {
            resultList.addAll(targetList);
        }
        _bindableTableTargetList = resultList;
        return _bindableTableTargetList;
    }

    protected List<String> _bindableTableExceptList;

    public List<String> getBindableTableExceptList() {
        if (_bindableTableExceptList != null) {
            return _bindableTableExceptList;
        }
        final List<String> exceptList = getDtoPropertyList("bindableTableExceptList");
        final List<String> resultList = DfCollectionUtil.newArrayList();
        if (exceptList != null) {
            resultList.addAll(exceptList);
        }
        _bindableTableExceptList = resultList;
        return _bindableTableExceptList;
    }

    protected boolean isBindableTableExcept(final String tableName) {
        final List<String> targetList = getBindableTableTargetList();
        final List<String> exceptList = getBindableTableExceptList();
        return !isTargetByHint(tableName, targetList, exceptList);
    }

    // ===================================================================================
    //                                                                            DTO Info
    //                                                                            ========
    public String getBaseDtoPackage() {
        return getPropertyAsRequired("baseDtoPackage");
    }

    public String getExtendedDtoPackage() {
        return getPropertyAsRequired("extendedDtoPackage");
    }

    public String getBaseDtoPrefix() {
        return getPropertyIfNullEmpty("baseDtoPrefix");
    }

    public String getBaseDtoSuffix() {
        return getPropertyIfNullEmpty("baseDtoSuffix");
    }

    public String getExtendedDtoPrefix() {
        return getPropertyIfNullEmpty("extendedDtoPrefix");
    }

    public String getExtendedDtoSuffix() {
        return getPropertyIfNullEmpty("extendedDtoSuffix");
    }

    public boolean isOverrideExtended() {
        return isProperty("isOverrideExtended", false);
    }

    public boolean isBindable(String tableName) {
        return isProperty("isBindable", false) && !isBindableTableExcept(tableName);
    }

    // ===================================================================================
    //                                                                     Property Helper
    //                                                                     ===============
    protected String getPropertyAsRequired(String key) {
        final String value = getProperty(key);
        if (value == null || value.trim().length() == 0) {
            String msg = "The property '" + key + "' should not be null or empty:";
            msg = msg + " flexDtoDefinitionMap=" + getFlexDtoDefinitionMap();
            throw new IllegalStateException(msg);
        }
        return value;
    }

    protected String getPropertyIfNullEmpty(String key) {
        final String value = getProperty(key);
        if (value == null) {
            return "";
        }
        return value;
    }

    protected String getProperty(String key) {
        return (String) getFlexDtoDefinitionMap().get(key);
    }

    protected boolean isProperty(String key, boolean defaultValue) {
        return isProperty(key, defaultValue, getFlexDtoDefinitionMap());
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> getDtoPropertyMap(String key) {
        return (Map<String, Object>) getFlexDtoDefinitionMap().get(key);
    }

    @SuppressWarnings("unchecked")
    protected List<String> getDtoPropertyList(String key) {
        return (List<String>) getFlexDtoDefinitionMap().get(key);
    }
}