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
package org.dbflute.properties.assistant.reps;

import java.util.List;
import java.util.Map;

import org.dbflute.exception.DfIllegalPropertySettingException;
import org.dbflute.helper.mapstring.MapListString;
import org.dbflute.properties.assistant.base.DfPropertyValueHandler;
import org.dbflute.system.DBFluteSystem;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.DfNameHintUtil;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 1.1.7 (2018/03/17 Saturday)
 */
public class DfConventionalTakeAssertMap {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger _log = LoggerFactory.getLogger(DfConventionalTakeAssertMap.class);
    private static final String KEY_conventionalTakeAssertMap = "conventionalTakeAssertMap";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _currentRepsEnvType;
    protected final Map<String, Object> _replaceSchemaMap;
    protected final DfPropertyValueHandler _propertyValueHandler;

    protected Map<String, Map<String, Object>> _conventionalTakeAssertMap; // cache

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfConventionalTakeAssertMap(String currentRepsEnvType, Map<String, Object> replaceSchemaMap,
            DfPropertyValueHandler propertyValueHandler) {
        _currentRepsEnvType = currentRepsEnvType;
        _replaceSchemaMap = replaceSchemaMap;
        _propertyValueHandler = propertyValueHandler;
    }

    // ===================================================================================
    //                                                                            Base Map
    //                                                                            ========
    // ; conventionalTakeAssertMap = map:{
    //     ; emptyTableMap = map:{
    //         ; isFailure = true
    //         ; tableExceptList = list:{}
    //         ; tableTargetList = list:{}
    //     }
    // }
    protected Map<String, Map<String, Object>> getConventionalTakeAssertMap() {
        if (_conventionalTakeAssertMap != null) {
            return _conventionalTakeAssertMap;
        }
        final Object obj = _replaceSchemaMap.get(KEY_conventionalTakeAssertMap);
        if (obj == null) {
            _conventionalTakeAssertMap = DfCollectionUtil.emptyMap();
        } else {
            @SuppressWarnings("unchecked")
            final Map<String, Map<String, Object>> repsMap = (Map<String, Map<String, Object>>) obj;
            _conventionalTakeAssertMap = repsMap;
        }
        return _conventionalTakeAssertMap;
    }

    public boolean hasConventionalTakeAssert() {
        return isEmptyTableFailure(); // increment if other convention is added
    }

    public void showProperties() {
        final String msg = KEY_conventionalTakeAssertMap + " in replaceSchemaMap.dfprop:";
        final String prop = buildDispProperties();
        _log.info(msg + DBFluteSystem.ln() + prop);
    }

    public String buildDispProperties() {
        final String mapString = new MapListString().buildMapString(getConventionalTakeAssertMap());
        return "; " + KEY_conventionalTakeAssertMap + " = " + mapString;
    }

    // ===================================================================================
    //                                                                         Empty Table
    //                                                                         ===========
    public boolean isEmptyTableFailure() {
        return _propertyValueHandler.isProperty("isFailure", false, getEmptyTableMap());
    }

    public boolean isEmptyTableWorkableEnv() {
        @SuppressWarnings("unchecked")
        final List<String> workableRepsEnvTypeList = (List<String>) getEmptyTableMap().get("workableRepsEnvTypeList");
        if (workableRepsEnvTypeList != null) {
            return Srl.containsElementAnyIgnoreCase(workableRepsEnvTypeList, "$$ALL$$", _currentRepsEnvType);
        } else { // no property
            String msg = "Not found the workableRepsEnvTypeList in emptyTableMap: " + getEmptyTableMap();
            throw new DfIllegalPropertySettingException(msg);
        }
    }

    public boolean isEmptyTableTarget(String tableDbName) {
        final Map<String, Object> emptyTableMap = getEmptyTableMap();
        @SuppressWarnings("unchecked")
        final List<String> tableTargetList = (List<String>) emptyTableMap.getOrDefault("tableTargetList", DfCollectionUtil.emptyMap());
        @SuppressWarnings("unchecked")
        final List<String> tableExceptList = (List<String>) emptyTableMap.getOrDefault("tableExceptList", DfCollectionUtil.emptyMap());
        return DfNameHintUtil.isTargetByHint(tableDbName, tableTargetList, tableExceptList);
    }

    protected Map<String, Object> getEmptyTableMap() {
        final Map<String, Object> emptyTableMap = getConventionalTakeAssertMap().get("emptyTableMap");
        return emptyTableMap != null ? emptyTableMap : DfCollectionUtil.emptyMap();
    }
}
