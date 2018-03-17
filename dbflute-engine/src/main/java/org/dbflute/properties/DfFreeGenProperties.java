/*
 * Copyright 2014-2018 the original author or authors.
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
package org.dbflute.properties;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.dbflute.exception.DfIllegalPropertySettingException;

/**
 * @author jflute
 */
public final class DfFreeGenProperties extends DfAbstractDBFluteProperties {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfFreeGenProperties(Properties prop) {
        super(prop);
    }

    // ===================================================================================
    //                                                                      Definition Map
    //                                                                      ==============
    // - - - - - - - - - - - - - - - - - - - - - - - - - - PROP
    // ; resourceMap = map:{
    //     ; baseDir = ../..
    //     ; resourceType = PROP
    //     ; resourceFile = $$baseDir$$/.../foo.properties
    // }
    // ; outputMap = map:{
    //     ; templateFile = MessageDef.vm
    //     ; outputDirectory = ../src/main/java
    //     ; package = org.dbflute...
    //     ; className = MessageDef
    // }
    // - - - - - - - - - - - - - - - - - - - - - - - - - - XLS
    // ; resourceMap = map:{
    //     ; resourceType = XLS
    //     ; resourceFile = ../../...
    // }
    // ; outputMap = map:{
    //     ; templateFile = CsvDto.vm
    //     ; outputDirectory = ../src/main/java
    //     ; package = org.dbflute...
    //     ; className = FooDto
    // }
    // ; optionMap = map:{
    //     ; sheetName = [sheet-name]
    //     ; rowBeginNumber = 3
    //     ; columnMap = map:{
    //         ; name = 3
    //         ; capName = df:cap(name)
    //         ; uncapName = df:uncap(name)
    //         ; capCamelName = df:capCamel(name)
    //         ; uncapCamelName = df:uncapCamel(name)
    //         ; type = 4
    //     }
    //     ; mappingMap = map:{
    //         ; type = map:{
    //             ; INTEGER = Integer
    //             ; VARCHAR = String
    //         }
    //     }
    // }
    protected Map<String, Object> _freeGenMap;

    public Map<String, Object> getFreeGenMap() {
        if (_freeGenMap == null) {
            Map<String, Object> specifiedMap = mapProp("torque.freeGenMap", null);
            if (specifiedMap == null) {
                specifiedMap = mapProp("torque.freeGenDefinitionMap", DEFAULT_EMPTY_MAP); // for compatible
            }
            _freeGenMap = newLinkedHashMap();
            reflectEmbeddedProperties();
            reflectSpecifiedProperties(specifiedMap);
        }
        return _freeGenMap;
    }

    protected void reflectEmbeddedProperties() {
        getESFluteProperties().reflectFreeGenMap(_freeGenMap);
        getLastaFluteProperties().reflectFreeGenMap(_freeGenMap);
    }

    protected void reflectSpecifiedProperties(Map<String, Object> specifiedMap) {
        for (Entry<String, Object> entry : specifiedMap.entrySet()) {
            final String key = entry.getKey();
            final Object value = entry.getValue();
            if (_freeGenMap.containsKey(key)) {
                String msg = "Already embedded the freeGen setting: " + key + ", " + value;
                throw new DfIllegalPropertySettingException(msg);
            }
            _freeGenMap.put(key, value);
        }
    }

    // ===================================================================================
    //                                                                     Property Helper
    //                                                                     ===============
    protected String getPropertyRequired(String key) {
        final String value = getProperty(key);
        if (value == null || value.trim().length() == 0) {
            String msg = "The property '" + key + "' should not be null or empty:";
            msg = msg + " simpleDtoDefinitionMap=" + getFreeGenMap();
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
        return (String) getFreeGenMap().get(key);
    }

    protected boolean isProperty(String key, boolean defaultValue) {
        return isProperty(key, defaultValue, getFreeGenMap());
    }
}