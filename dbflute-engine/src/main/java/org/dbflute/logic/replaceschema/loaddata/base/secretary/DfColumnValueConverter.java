/*
 * Copyright 2014-2022 the original author or authors.
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
package org.dbflute.logic.replaceschema.loaddata.base.secretary;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.torque.engine.database.model.TypeMap;
import org.dbflute.helper.StringKeyMap;
import org.dbflute.logic.jdbc.metadata.info.DfColumnMeta;
import org.dbflute.system.DBFluteSystem;
import org.dbflute.util.DfNameHintUtil;
import org.dbflute.util.DfTypeUtil;
import org.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfColumnValueConverter {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Map<String, Map<String, String>> _convertValueMap;
    protected final Map<String, String> _defaultValueMap;
    protected final DfColumnBindTypeProvider _bindTypeProvider;
    protected Map<String, String> _allColumnConvertMap; // derived lazily
    protected Map<String, Map<String, String>> _typedColumnConvertMap; // derived lazily
    protected boolean emptyBeforeAsNull; // for compatible with old TSV settings e.g. $$empty$$ = $$empty$$

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfColumnValueConverter(Map<String, Map<String, String>> convertValueMap, Map<String, String> defaultValueMap,
            DfColumnBindTypeProvider bindTypeProvider) {
        _convertValueMap = convertValueMap;
        _defaultValueMap = defaultValueMap;
        _bindTypeProvider = bindTypeProvider;
    }

    public void treatEmptyBeforeAsNull() {
        emptyBeforeAsNull = true;
    }

    // ===================================================================================
    //                                                                             Convert
    //                                                                             =======
    public void convert(String tableName, Map<String, Object> columnValueMap, Map<String, DfColumnMeta> columnMetaMap) {
        final Map<String, Object> resolvedMap = new LinkedHashMap<String, Object>(columnValueMap.size());
        final Set<String> convertedSet = new HashSet<String>(1);
        for (Entry<String, Object> entry : columnValueMap.entrySet()) {
            final String columnName = entry.getKey();
            final Object plainValue = entry.getValue(); // null allowed
            Object resolvedValue = resolveConvertValue(tableName, columnName, plainValue, convertedSet, columnMetaMap);
            if (convertedSet.isEmpty()) { // if no convert
                resolvedValue = resolveDefaultValue(columnName, resolvedValue);
            } else {
                convertedSet.clear(); // recycle
            }
            resolvedMap.put(columnName, resolvedValue);
        }
        for (Entry<String, Object> entry : resolvedMap.entrySet()) { // to keep original map instance
            columnValueMap.put(entry.getKey(), entry.getValue());
        }
    }

    // ===================================================================================
    //                                                                       Convert Value
    //                                                                       =============
    protected Object resolveConvertValue(String tableName, String columnName, Object plainValue, Set<String> convertedSet,
            Map<String, DfColumnMeta> columnMetaMap) {
        if (_convertValueMap == null || _convertValueMap.isEmpty()) {
            return plainValue;
        }
        final Map<String, String> valueMapping = findConvertValueMapping(columnName, columnMetaMap);
        if (valueMapping == null || valueMapping.isEmpty()) {
            return plainValue;
        }
        String filteredValue = prepareStringPlainValue(plainValue); // null allowed
        boolean converted = false;
        final String containMark = DfNameHintUtil.CONTAIN_MARK;
        for (Entry<String, String> entry : valueMapping.entrySet()) {
            final String before = entry.getKey();
            final String after = resolveVariableAsAfter(entry.getValue());

            final String typed = processType(tableName, columnName, columnMetaMap, filteredValue, before, after);
            if (typed != null) {
                filteredValue = typed;
                converted = true;
                continue;
            }

            if (Srl.startsWithIgnoreCase(before, containMark)) {
                final String realBefore = resolveVariableAsBefore(Srl.substringFirstRear(before, containMark));
                if (realBefore == null) {
                    throw new IllegalStateException("Cannot use contain:$$null$$: " + tableName + "." + columnName);
                }
                if (filteredValue != null && filteredValue.contains(realBefore)) {
                    filteredValue = Srl.replace(filteredValue, realBefore, (after != null ? after : ""));
                    converted = true;
                }
            } else {
                final String realBefore = resolveVariableAsBefore(before);
                if (filteredValue != null && filteredValue.equals(realBefore)) {
                    filteredValue = after;
                    converted = true;
                } else if (filteredValue == null && realBefore == null) {
                    filteredValue = after;
                    converted = true;
                }
            }
        }
        if (converted) {
            convertedSet.add("converted");
            return filteredValue; // null allowed
        } else {
            return plainValue; // null allowed
        }
    }

    protected String prepareStringPlainValue(Object plainValue) {
        if (plainValue == null) {
            return null;
        }
        if (plainValue instanceof Time) {
            return DfTypeUtil.toString(plainValue, "HH:mm:ss");
        } else if (plainValue instanceof Date) {
            return DfTypeUtil.toString(plainValue, "yyyy-MM-dd HH:mm:ss.SSS");
        }
        return plainValue.toString();
    }

    protected String resolveVariableAsAfter(String value) {
        if ("$$empty$$".equalsIgnoreCase(value)) {
            return "";
        }
        if ("$$null$$".equalsIgnoreCase(value)) {
            return null;
        }
        return value;
    }

    protected String resolveVariableAsBefore(String value) {
        if ("$$empty$$".equalsIgnoreCase(value)) {
            return emptyBeforeAsNull ? null : "";
        }
        if ("$$null$$".equalsIgnoreCase(value)) {
            return null;
        }
        return value;
    }

    // ===================================================================================
    //                                                                       Value Mapping
    //                                                                       =============
    protected Map<String, String> findConvertValueMapping(String columnName, Map<String, DfColumnMeta> columnMetaMap) {
        final Map<String, String> allMap = findeAllColumnConvertMap();
        final Map<String, String> typedMap = findTypedColumnConvertMap(columnName, columnMetaMap);
        final Map<String, String> columnMap = _convertValueMap.getOrDefault(columnName, Collections.emptyMap());
        return inheritMap(columnMap, inheritMap(typedMap, allMap)); // should be case sensitive map
    }

    protected Map<String, String> findeAllColumnConvertMap() {
        if (_allColumnConvertMap != null) {
            return _allColumnConvertMap;
        }
        _allColumnConvertMap = _convertValueMap.getOrDefault("$$ALL$$", Collections.emptyMap());
        return _allColumnConvertMap;
    }

    protected Map<String, String> findTypedColumnConvertMap(String columnName, Map<String, DfColumnMeta> columnMetaMap) {
        final DfColumnMeta meta = columnMetaMap.get(columnName);
        if (meta == null) { // no way, just in case
            throw new IllegalStateException("Not found the column meta: " + columnName);
        }
        final String jdbcType = TypeMap.findJdbcTypeByJdbcDefValue(meta.getJdbcDefValue());
        if (jdbcType == null) { // basically no way!?, just in case
            return Collections.emptyMap(); // as not found
        }
        if (_typedColumnConvertMap != null) {
            final Map<String, String> existingMap = _typedColumnConvertMap.get(jdbcType);
            if (existingMap != null) {
                return existingMap;
            }
        } else {
            _typedColumnConvertMap = StringKeyMap.createAsCaseInsensitive();
        }
        final String typedKey = "$$type(" + jdbcType + ")$$"; // e.g. $$type(VARCHAR)$$
        final Map<String, String> valueMap = _convertValueMap.getOrDefault(typedKey, Collections.emptyMap());
        _typedColumnConvertMap.put(jdbcType, valueMap);
        return _typedColumnConvertMap.get(jdbcType);
    }

    protected Map<String, String> inheritMap(Map<String, String> subMap, Map<String, String> superMap) {
        final Map<String, String> mergedMap = new LinkedHashMap<String, String>(); // case sensitive and ordered
        mergedMap.putAll(superMap);
        mergedMap.putAll(subMap); // override if same value
        return mergedMap;
    }

    // ===================================================================================
    //                                                                        Type Process
    //                                                                        ============
    protected String processType(String tableName, String columnName, Map<String, DfColumnMeta> columnMetaMap, String filteredValue,
            String before, String after) {
        String processed = null;
        processed = processString(tableName, columnName, columnMetaMap, filteredValue, before, after);
        if (processed != null) {
            return processed;
        }
        processed = processTimestamp(tableName, columnName, columnMetaMap, filteredValue, before, after);
        if (processed != null) {
            return processed;
        }
        return null;
    }

    protected String processString(String tableName, String columnName, Map<String, DfColumnMeta> columnMetaMap, String filteredValue,
            String before, String after) {
        if (!"$$String$$".equalsIgnoreCase(before)) {
            return null; // no converted
        }
        final DfColumnMeta columnMeta = columnMetaMap.get(columnName);
        final Class<?> boundType = _bindTypeProvider.provide(tableName, columnMeta);
        if (!String.class.isAssignableFrom(boundType)) {
            return null; // no converted
        }
        if (after.equalsIgnoreCase("$$NullToEmpty$$")) {
            if (filteredValue == null) {
                return "";
            }
        }
        return null; // no converted
    }

    protected String processTimestamp(String tableName, String columnName, Map<String, DfColumnMeta> columnMetaMap, String filteredValue,
            String before, String after) {
        if (!"$$Timestamp$$".equalsIgnoreCase(before)) {
            return null; // no converted
        }
        final DfColumnMeta columnMeta = columnMetaMap.get(columnName);
        final Class<?> boundType = _bindTypeProvider.provide(tableName, columnMeta);
        if (!Timestamp.class.isAssignableFrom(boundType)) {
            return null; // no converted
        }
        // process target here
        if (after.equalsIgnoreCase("$$ZeroPrefixMillis$$")) { // DBFlute default
            if (filteredValue != null && filteredValue.contains(".")) {
                final String front = Srl.substringLastFront(filteredValue, ".");
                final String millis = Srl.substringLastRear(filteredValue, ".");
                if (millis.length() == 1) {
                    filteredValue = front + ".00" + millis;
                } else if (millis.length() == 2) {
                    filteredValue = front + ".0" + millis;
                }
                return filteredValue; // processed
            }
        } else if (after.equalsIgnoreCase("$$ZeroSuffixMillis$$")) {
            if (filteredValue != null && filteredValue.contains(".")) {
                final String millis = Srl.substringLastRear(filteredValue, ".");
                if (millis.length() == 1) {
                    filteredValue = filteredValue + "00";
                } else if (millis.length() == 2) {
                    filteredValue = filteredValue + "0";
                }
                return filteredValue; // processed
            }
        }
        return null; // no converted
    }

    // ===================================================================================
    //                                                                       Default Value
    //                                                                       =============
    protected Object resolveDefaultValue(String columnName, Object plainValue) {
        if (_defaultValueMap == null || _defaultValueMap.isEmpty()) {
            return plainValue;
        }
        if (plainValue != null && !plainValue.equals("")) {
            return plainValue;
        }
        // null or empty here
        if (!hasDefaultValue(columnName)) {
            return plainValue;
        }
        final String defaultValue = findDefaultValue(columnName);
        if (defaultValue == null) { // not found
            return plainValue;
        }
        if (defaultValue.equals("")) { // default is empty
            return defaultValue;
        }
        Object resolvedValue = plainValue;
        if (defaultValue.equalsIgnoreCase("sysdate")) {
            resolvedValue = DBFluteSystem.currentTimestamp();
        } else {
            resolvedValue = defaultValue;
        }
        return resolvedValue;
    }

    private boolean hasDefaultValue(String columnName) {
        return _defaultValueMap.containsKey(columnName);
    }

    private String findDefaultValue(String columnName) {
        // defaultValueMap should be case insensitive (or flexible) map
        // (must be already resolved here)
        return _defaultValueMap.get(columnName);
    }
}
