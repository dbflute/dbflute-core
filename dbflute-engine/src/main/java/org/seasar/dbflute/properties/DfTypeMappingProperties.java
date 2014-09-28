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

import org.seasar.dbflute.helper.StringKeyMap;
import org.seasar.dbflute.logic.generate.language.typemapping.DfLanguageTypeMapping;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.5.8 (2007/11/27 Tuesday)
 */
public final class DfTypeMappingProperties extends DfAbstractHelperProperties {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfTypeMappingProperties(Properties prop) {
        super(prop);
    }

    // ===================================================================================
    //                                                                    Type Mapping Map
    //                                                                    ================
    public static final String KEY_typeMappingMap = "typeMappingMap";
    protected Map<String, Object> _typeMappingMap;

    protected Map<String, Object> getTypeMappingMap() {
        if (_typeMappingMap == null) {
            final Map<String, Object> map = mapProp("torque." + KEY_typeMappingMap, DEFAULT_EMPTY_MAP);
            _typeMappingMap = newLinkedHashMap();
            _typeMappingMap.putAll(map);
        }
        return _typeMappingMap;
    }

    // -----------------------------------------------------
    //                                 JDBC Type Mapping Map
    //                                 ---------------------
    protected Map<String, String> _jdbcTypeMappingMap;

    protected Map<String, String> getJdbcTypeMappingMap() {
        if (_jdbcTypeMappingMap != null) {
            return _jdbcTypeMappingMap;
        }
        final Map<String, Object> typeMappingMap = getTypeMappingMap();
        final Map<String, String> jdbcTypeMappingMap = newLinkedHashMap();
        for (Entry<String, Object> entry : typeMappingMap.entrySet()) {
            final String key = entry.getKey();
            if (isJdbcTypeMappingKey(key)) {
                jdbcTypeMappingMap.put(key, (String) entry.getValue());
            }
        }
        _jdbcTypeMappingMap = jdbcTypeMappingMap;
        return _jdbcTypeMappingMap;
    }

    protected static boolean isJdbcTypeMappingKey(String key) {
        return !isNameTypeMappingKey(key) && !isPointTypeMappingKey(key);
    }

    // -----------------------------------------------------
    //                                 Name Type Mapping Map
    //                                 ---------------------
    protected Map<String, String> _nameTypeMappingMap;

    protected Map<String, String> getNameTypeMappingMap() {
        if (_nameTypeMappingMap != null) {
            return _nameTypeMappingMap;
        }
        final Map<String, Object> typeMappingMap = getTypeMappingMap();
        final Map<String, String> nameTypeMappingMap = newLinkedHashMap();
        for (Entry<String, Object> entry : typeMappingMap.entrySet()) {
            final String key = entry.getKey();
            if (isNameTypeMappingKey(key)) {
                nameTypeMappingMap.put(extractDbTypeName(key), (String) entry.getValue());
            }
        }
        _nameTypeMappingMap = nameTypeMappingMap;
        return _nameTypeMappingMap;
    }

    protected static boolean isNameTypeMappingKey(String key) {
        if (isPointTypeMappingKey(key)) {
            return false;
        }
        return key.startsWith("$$") && key.endsWith("$$") && key.length() > "$$$$".length();
    }

    protected static String extractDbTypeName(String key) {
        final String realKey = key.substring("$$".length());
        return realKey.substring(0, realKey.length() - "$$".length());
    }

    // -----------------------------------------------------
    //                                Point Type Mapping Map
    //                                ----------------------
    protected Map<String, Map<String, String>> getPointTypeMappingMap() {
        final Map<String, Object> typeMappingMap = getTypeMappingMap();
        final Map<String, Map<String, String>> pointTypeMappingMap = StringKeyMap.createAsFlexibleOrdered();
        for (Entry<String, Object> entry : typeMappingMap.entrySet()) {
            final String key = entry.getKey();
            if (!isPointTypeMappingKey(key)) {
                continue;
            }
            final Object obj = entry.getValue();
            @SuppressWarnings("unchecked")
            final Map<String, Map<String, String>> pointMap = (Map<String, Map<String, String>>) obj;
            for (Entry<String, Map<String, String>> pointEntry : pointMap.entrySet()) {
                final String pointKey = pointEntry.getKey();
                final Map<String, String> pointElementMap = pointEntry.getValue();
                final Map<String, String> flexibleMap = StringKeyMap.createAsFlexibleOrdered();
                flexibleMap.putAll(pointElementMap);
                pointTypeMappingMap.put(pointKey, pointElementMap);
            }
        }
        return pointTypeMappingMap;
    }

    protected static boolean isPointTypeMappingKey(String key) {
        return key.startsWith("$$df:point$$");
    }

    // ===================================================================================
    //                                                                 JDBC to Java Native
    //                                                                 ===================
    protected Map<String, String> _jdbcToJavaNativeMap;

    public Map<String, String> getJdbcToJavaNativeMap() {
        if (_jdbcToJavaNativeMap != null) {
            return _jdbcToJavaNativeMap;
        }
        final Map<String, String> jdbcToJavaNativeMap = newLinkedHashMap();
        jdbcToJavaNativeMap.putAll(getLanguageTypeMapping().getJdbcToJavaNativeMap()); // language definition at first

        // Java8-Time and JodaTime support
        prepareJava8OrJodaTimeMappingIfNeeds(jdbcToJavaNativeMap);

        for (Entry<String, String> entry : getJdbcTypeMappingMap().entrySet()) {
            jdbcToJavaNativeMap.put(entry.getKey(), entry.getValue()); // override by specified types in property
        }
        _jdbcToJavaNativeMap = jdbcToJavaNativeMap;
        return _jdbcToJavaNativeMap;
    }

    protected void prepareJava8OrJodaTimeMappingIfNeeds(final Map<String, String> jdbcToJavaNativeMap) {
        if (getLittleAdjustmentProperties().isAvailableJava8TimeLocalDateEntity()) {
            jdbcToJavaNativeMap.put("DATE", "java.time.LocalDate");
            jdbcToJavaNativeMap.put("TIMESTAMP", "java.time.LocalDateTime");
            jdbcToJavaNativeMap.put("TIME", "java.time.LocalTime");
        }
        if (getLittleAdjustmentProperties().isAvailableJodaTimeLocalDateEntity()) {
            jdbcToJavaNativeMap.put("DATE", "org.joda.time.LocalDate");
            jdbcToJavaNativeMap.put("TIMESTAMP", "org.joda.time.LocalDateTime");
            jdbcToJavaNativeMap.put("TIME", "org.joda.time.LocalTime");
        }
    }

    // ===================================================================================
    //                                                                   Name to JDBC Type
    //                                                                   =================
    protected Map<String, String> _nameToJdbcTypeMap;

    public Map<String, String> getNameToJdbcTypeMap() {
        if (_nameToJdbcTypeMap != null) {
            return _nameToJdbcTypeMap;
        }
        _nameToJdbcTypeMap = getNameTypeMappingMap();
        return _nameToJdbcTypeMap;
    }

    // ===================================================================================
    //                                                                  Point to JDBC Type
    //                                                                  ==================
    protected Map<String, Map<String, String>> _pointToJdbcTypeMap;

    public Map<String, Map<String, String>> getPointToJdbcTypeMap() {
        if (_pointToJdbcTypeMap != null) {
            return _pointToJdbcTypeMap;
        }
        _pointToJdbcTypeMap = getPointTypeMappingMap();
        return _pointToJdbcTypeMap;
    }

    // ===================================================================================
    //                                                               Java Native Type List
    //                                                               =====================
    public List<String> getJavaNativeStringList() { // not property
        return getLanguageTypeMapping().getStringList();
    }

    public boolean isJavaNativeStringObject(String javaNative) {
        return containsAsEndsWith(javaNative, getJavaNativeStringList());
    }

    public List<String> getJavaNativeNumberList() { // not property
        return getLanguageTypeMapping().getNumberList();
    }

    public boolean isJavaNativeNumberObject(String javaNative) {
        return containsAsEndsWith(javaNative, getJavaNativeNumberList());
    }

    public List<String> getJavaNativeDateList() { // not property
        return getLanguageTypeMapping().getDateList();
    }

    public boolean isJavaNativeDateObject(String javaNative) {
        return containsAsEndsWith(javaNative, getJavaNativeDateList());
    }

    public List<String> getJavaNativeBooleanList() { // not property
        return getLanguageTypeMapping().getBooleanList();
    }

    public boolean isJavaNativeBooleanObject(String javaNative) {
        return containsAsEndsWith(javaNative, getJavaNativeBooleanList());
    }

    public List<String> getJavaNativeBinaryList() { // not property
        return getLanguageTypeMapping().getBinaryList();
    }

    public boolean isJavaNativeBinaryObject(String javaNative) {
        return containsAsEndsWith(javaNative, getJavaNativeBinaryList());
    }

    protected boolean containsAsEndsWith(String str, List<String> suffixList) {
        return Srl.endsWithIgnoreCase(str, suffixList.toArray(new String[] {}));
    }

    // ===================================================================================
    //                                                                  Language Meta Data
    //                                                                  ==================
    protected DfLanguageTypeMapping getLanguageTypeMapping() {
        return getBasicProperties().getLanguageDependency().getLanguageTypeMapping();
    }
}