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

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.seasar.dbflute.util.DfStringUtil;

/**
 * @author jflute
 */
public final class DfBehaviorFilterProperties extends DfAbstractHelperProperties {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfBehaviorFilterProperties(Properties prop) {
        super(prop);
    }

    // ===================================================================================
    //                                                                     Behavior Filter
    //                                                                     ===============
    public static final String KEY_behaviorFilterMap = "behaviorFilterMap";
    protected Map<String, Object> _behaviorFilterMap;

    public Map<String, Object> getBehaviorFilterMap() {
        if (_behaviorFilterMap == null) {
            final Map<String, Object> map = mapProp("torque." + KEY_behaviorFilterMap, DEFAULT_EMPTY_MAP);
            _behaviorFilterMap = newLinkedHashMap();
            _behaviorFilterMap.putAll(map);
        }
        return _behaviorFilterMap;
    }

    // ===================================================================================
    //                                                                 Common Column Setup
    //                                                                 ===================
    public boolean isExistCommonColumnSetupElement() {
        final Map<String, Object> insertElementMap = getBeforeInsertMap();
        final Map<String, Object> updateElementMap = getBeforeUpdateMap();
        final Map<String, Object> deleteElementMap = getBeforeDeleteMap();
        if (insertElementMap.isEmpty() && updateElementMap.isEmpty() && deleteElementMap.isEmpty()) {
            return false;
        }
        return true;
    }

    // ===================================================================================
    //                                                                    Intercept Insert
    //                                                                    ================
    protected Map<String, Object> _beforeInsertMap;

    @SuppressWarnings("unchecked")
    public Map<String, Object> getBeforeInsertMap() {
        if (_beforeInsertMap == null) {
            getBehaviorFilterMap();// For initialization of behaviorFilterMap.
            if (_behaviorFilterMap != null && _behaviorFilterMap.containsKey("beforeInsertMap")) {
                // For the way by dfprop-setting.
                // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                // ; beforeInsertMap = map:{
                //     ; REGISTER_DATETIME = $$AccessContext$$.getAccessTimestampOnThread()
                //     ; REGISTER_USER     = $$AccessContext$$.getAccessUserOnThread()
                //     ; REGISTER_PROCESS  = $$AccessContext$$.getAccessProcessOnThread()
                //     ; UPDATE_DATETIME   = entity.getRegisterDatetime()
                //     ; UPDATE_USER       = entity.getRegisterUser()
                //     ; UPDATE_PROCESS    = entity.getRegisterProcess()
                // }
                // - - - - - - - - - -/ 
                _beforeInsertMap = (Map<String, Object>) _behaviorFilterMap.get("beforeInsertMap");
            } else {
                _beforeInsertMap = newLinkedHashMap();
            }
            filterCommonColumnSetupValue(_beforeInsertMap);
        }
        return _beforeInsertMap;
    }

    public boolean containsValidColumnNameKeyCommonColumnSetupBeforeInsertInterceptorLogicMap(String columnName) {
        final Map<String, Object> map = getBeforeInsertMap();
        final String logic = (String) map.get(columnName);
        if (logic != null && logic.trim().length() != 0) {
            return true;
        } else {
            return false;
        }
    }

    public String getCommonColumnSetupBeforeInsertInterceptorLogicByColumnName(String columnName) {
        final Map<String, Object> map = getBeforeInsertMap();
        return (String) map.get(columnName);
    }

    // ===================================================================================
    //                                                                    Intercept Update
    //                                                                    ================
    protected Map<String, Object> _beforeUpdateMap;

    @SuppressWarnings("unchecked")
    public Map<String, Object> getBeforeUpdateMap() {
        if (_beforeUpdateMap == null) {
            getBehaviorFilterMap();// For initialization of behaviorFilterMap.
            if (_behaviorFilterMap != null && _behaviorFilterMap.containsKey("beforeUpdateMap")) {
                // For the way by dfprop-setting.
                // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                // ; beforeUpdateMap = map:{
                //     ; REGISTER_DATETIME = $$AccessContext$$.getAccessTimestampOnThread()
                //     ; REGISTER_USER     = $$AccessContext$$.getAccessUserOnThread()
                //     ; REGISTER_PROCESS  = $$AccessContext$$.getAccessProcessOnThread()
                // }
                // - - - - - - - - - -/ 
                _beforeUpdateMap = (Map<String, Object>) _behaviorFilterMap.get("beforeUpdateMap");
            } else {
                _beforeUpdateMap = newLinkedHashMap();
            }
            filterCommonColumnSetupValue(_beforeUpdateMap);
        }
        return _beforeUpdateMap;
    }

    public boolean containsValidColumnNameKeyCommonColumnSetupBeforeUpdateInterceptorLogicMap(String columnName) {
        final Map<String, Object> map = getBeforeUpdateMap();
        final String logic = (String) map.get(columnName);
        if (logic != null && logic.trim().length() != 0) {
            return true;
        } else {
            return false;
        }
    }

    public String getCommonColumnSetupBeforeUpdateInterceptorLogicByColumnName(String columnName) {
        final Map<String, Object> map = getBeforeUpdateMap();
        return (String) map.get(columnName);
    }

    // ===================================================================================
    //                                                                    Intercept Delete
    //                                                                    ================
    protected Map<String, Object> _beforeDeleteMap;

    @SuppressWarnings("unchecked")
    public Map<String, Object> getBeforeDeleteMap() {
        if (_beforeDeleteMap == null) {
            getBehaviorFilterMap();// For initialization of behaviorFilterMap.
            if (_behaviorFilterMap != null && _behaviorFilterMap.containsKey("beforeDeleteMap")) {
                // For the way by dfprop-setting.
                // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                // ; beforeDeleteMap = map:{
                //     ; REGISTER_DATETIME = $$AccessContext$$.getAccessTimestampOnThread()
                //     ; REGISTER_USER     = $$AccessContext$$.getAccessUserOnThread()
                //     ; REGISTER_PROCESS  = $$AccessContext$$.getAccessProcessOnThread()
                //     ; UPDATE_DATETIME   = entity.getRegisterDatetime()
                //     ; UPDATE_USER       = entity.getRegisterUser()
                //     ; UPDATE_PROCESS    = entity.getRegisterProcess()
                // }
                // - - - - - - - - - -/ 
                _beforeDeleteMap = (Map<String, Object>) _behaviorFilterMap.get("beforeDeleteMap");
            } else {
                _beforeDeleteMap = newLinkedHashMap();
            }
            filterCommonColumnSetupValue(_beforeDeleteMap);
        }
        return _beforeDeleteMap;
    }

    public boolean containsValidColumnNameKeyCommonColumnSetupBeforeDeleteInterceptorLogicMap(String columnName) {
        final Map<String, Object> map = getBeforeDeleteMap();
        final String logic = (String) map.get(columnName);
        if (logic != null && logic.trim().length() != 0) {
            return true;
        } else {
            return false;
        }
    }

    public String getCommonColumnSetupBeforeDeleteInterceptorLogicByColumnName(String columnName) {
        final Map<String, Object> map = getBeforeDeleteMap();
        return (String) map.get(columnName);
    }

    // ===================================================================================
    //                                                                    Intercept Common
    //                                                                    ================
    // -----------------------------------------------------
    //                                                filter
    //                                                ------
    protected void filterCommonColumnSetupValue(Map<String, Object> map) {
        final String baseCommonPackage = getBasicProperties().getBaseCommonPackage();
        final Set<String> keySet = map.keySet();
        for (String key : keySet) {
            String value = (String) map.get(key);
            if (value != null && value.contains("$$allcommon$$")) {
                value = DfStringUtil.replace(value, "$$allcommon$$", baseCommonPackage);
            }
            if (value != null && value.contains("$$AccessContext$$")) {
                final String accessContext = getCommonColumnProperties().getAccessContextFqcn();
                value = DfStringUtil.replace(value, "$$AccessContext$$", accessContext);
            }
            map.put(key, value);
        }
    }
}