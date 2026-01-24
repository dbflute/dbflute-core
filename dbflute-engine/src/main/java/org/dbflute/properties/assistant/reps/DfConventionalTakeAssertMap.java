/*
 * Copyright 2014-2026 the original author or authors.
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

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.dbflute.exception.DfIllegalPropertySettingException;
import org.dbflute.exception.ParseDateExpressionFailureException;
import org.dbflute.helper.HandyDate;
import org.dbflute.helper.dfmap.DfMapStyle;
import org.dbflute.helper.message.ExceptionMessageBuilder;
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
    protected static final String KEY_conventionalTakeAssertMap = "conventionalTakeAssertMap";
    protected static final String EXAMPLE_DATE_EXPRESSION = "2018/05/03";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _currentRepsEnvType; // not null
    protected final Map<String, Object> _replaceSchemaMap; // not null
    protected final DfPropertyValueHandler _propertyValueHandler; // not null

    protected Map<String, Map<String, Object>> _conventionalTakeAssertMap; // cache, null allowed, lazy-loaded

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
    //     ; nullOnlyColumnMap = map:{
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
        final String mapString = new DfMapStyle().toMapString(getConventionalTakeAssertMap());
        return "; " + KEY_conventionalTakeAssertMap + " = " + mapString;
    }

    // ===================================================================================
    //                                                                         Empty Table
    //                                                                         ===========
    // ; emptyTableMap = map:{
    //     ; isFailure = true
    //     ; workableRepsEnvTypeList = list:{ut}
    //     ; tableExceptList = list:{}
    //     ; tableTargetList = list:{}
    //     ; errorIfFirstDateAfter = 2018/05/18
    //     ; isFrameworkDebug = false
    // }
    public boolean isEmptyTableFailure() {
        return _propertyValueHandler.isProperty("isFailure", false, getEmptyTableMap());
    }

    public boolean isEmptyTableWorkableEnv() {
        return determineWorkableRepsEnvTypeList("emptyTableMap", getEmptyTableMap());
    }

    public boolean isEmptyTableTarget(String tableDbName) {
        return determineTableTarget(tableDbName, getEmptyTableMap());
    }

    public Date getEmptyTableErrorIfFirstDateAfter() { // null allowed
        return prepareErrorIfFirstDateAfter("errorIfFirstDateAfter", getEmptyTableMap());
    }

    public String getEmptyTableErrorMessage() { // null allowed
        return _propertyValueHandler.getProperty("errorMessage", null, getEmptyTableMap());
    }

    public boolean isEmptyTableFrameworkDebug() {
        return _propertyValueHandler.isProperty("isFrameworkDebug", false, getEmptyTableMap());
    }

    protected Map<String, Object> getEmptyTableMap() {
        final Map<String, Object> emptyTableMap = getConventionalTakeAssertMap().get("emptyTableMap");
        return emptyTableMap != null ? emptyTableMap : DfCollectionUtil.emptyMap();
    }

    // ===================================================================================
    //                                                                     nullOnly Column
    //                                                                     ===============
    // ; nullOnlyColumnMap = map:{
    //     ; isFailure = true
    //     ; workableRepsEnvTypeList = list:{ut}
    //     ; tableExceptList = list:{}
    //     ; tableTargetList = list:{}
    //     ; isSkipIfEmptyTable = false
    //     ; errorIfTableFirstDateAfter = 2025/12/18
    //     ; errorIfColumnFirstDateAfter = 2026/01/22
    //     ; isFrameworkDebug = false
    // }
    public boolean isNullOnlyColumnFailure() {
        return _propertyValueHandler.isProperty("isFailure", false, getNullOnlyColumnMap());
    }

    public boolean isNullOnlyColumnWorkableEnv() {
        return determineWorkableRepsEnvTypeList("nullOnlyColumnMap", getNullOnlyColumnMap());
    }

    public boolean isNullOnlyColumnTarget(String tableDbName) {
        return determineTableTarget(tableDbName, getNullOnlyColumnMap());
    }

    public boolean isSkipIfEmptyTable() {
        return _propertyValueHandler.isProperty("isSkipIfEmptyTable", false, getNullOnlyColumnMap());
    }

    public Date getNullOnlyColumnErrorIfTableFirstDateAfter() { // null allowed
        return prepareErrorIfFirstDateAfter("errorIfTableFirstDateAfter", getNullOnlyColumnMap());
    }

    public Date getNullOnlyColumnErrorIfColumnFirstDateAfter() { // null allowed
        return prepareErrorIfFirstDateAfter("errorIfColumnFirstDateAfter", getNullOnlyColumnMap());
    }

    public String getNullOnlyColumnErrorMessage() {
        return _propertyValueHandler.getProperty("errorMessage", null, getNullOnlyColumnMap());
    }

    public boolean isNullOnlyColumnFrameworkDebug() {
        return _propertyValueHandler.isProperty("isFrameworkDebug", false, getNullOnlyColumnMap());
    }

    protected Map<String, Object> getNullOnlyColumnMap() {
        final Map<String, Object> nullOnlyColumnMap = getConventionalTakeAssertMap().get("nullOnlyColumnMap");
        return nullOnlyColumnMap != null ? nullOnlyColumnMap : DfCollectionUtil.emptyMap();
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    protected boolean determineWorkableRepsEnvTypeList(String mapTitle, Map<String, Object> emptyTableMap) {
        @SuppressWarnings("unchecked")
        final List<String> workableRepsEnvTypeList = (List<String>) emptyTableMap.get("workableRepsEnvTypeList");
        if (workableRepsEnvTypeList != null) {
            return Srl.containsElementAnyIgnoreCase(workableRepsEnvTypeList, "$$ALL$$", _currentRepsEnvType);
        } else { // no property
            String msg = "Not found the workableRepsEnvTypeList in " + mapTitle + ": " + emptyTableMap;
            throw new DfIllegalPropertySettingException(msg);
        }
    }

    protected boolean determineTableTarget(String tableDbName, Map<String, Object> targetMap) {
        @SuppressWarnings("unchecked")
        final List<String> tableTargetList = (List<String>) targetMap.getOrDefault("tableTargetList", DfCollectionUtil.emptyList());
        @SuppressWarnings("unchecked")
        final List<String> tableExceptList = (List<String>) targetMap.getOrDefault("tableExceptList", DfCollectionUtil.emptyList());
        return DfNameHintUtil.isTargetByHint(tableDbName, tableTargetList, tableExceptList);
    }

    protected Date prepareErrorIfFirstDateAfter(String key, Map<String, Object> emptyTableMap) {
        final String prop = _propertyValueHandler.getProperty(key, null, emptyTableMap);
        if (prop != null && prop.length() > EXAMPLE_DATE_EXPRESSION.length()) { // e.g. has time part
            throwConventionalTakeAssertIllegalDateExpressionFormatException(key, prop, null);
        }
        try {
            return prop != null ? new HandyDate(prop).getDate() : null;
        } catch (ParseDateExpressionFailureException e) {
            throwConventionalTakeAssertIllegalDateExpressionFormatException(key, prop, e);
            return null; // unreachable
        }
    }

    // -----------------------------------------------------
    //                                             Exception
    //                                             ---------
    protected void throwConventionalTakeAssertIllegalDateExpressionFormatException(String key, String prop, RuntimeException cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Illegal format of the date expression for firstDate.");
        br.addItem("Advice");
        br.addElement("The date expression for firstDate should be e.g. 2018/05/03");
        br.addElement("(slash separator, no time part)");
        br.addElement("Confirm your " + KEY_conventionalTakeAssertMap + ".");
        br.addElement("For example:");
        br.addElement("  (x): 2018/ab/cd");
        br.addElement("  (x): 2018/05/03 12:34:56");
        br.addElement("  (o): 2018/05/03");
        br.addItem("Property Key");
        br.addElement(key);
        br.addItem("Property Value");
        br.addElement(prop);
        final String msg = br.buildExceptionMessage();
        if (cause != null) {
            throw new IllegalStateException(msg, cause);
        } else {
            throw new IllegalStateException(msg);
        }
    }
}
