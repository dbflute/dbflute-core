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
package org.seasar.dbflute.properties.assistant.freegen.json;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.seasar.dbflute.exception.DfIllegalPropertySettingException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.properties.assistant.freegen.DfFreeGenResource;
import org.seasar.dbflute.properties.assistant.freegen.DfFreeGenTable;
import org.seasar.dbflute.properties.assistant.freegen.reflector.DfFreeGenLazyReflector;
import org.seasar.dbflute.properties.assistant.freegen.reflector.DfFreeGenMethodConverter;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfJsonSchemaTableLoader {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfFreeGenMethodConverter _methodConverter = new DfFreeGenMethodConverter();

    protected final String _requestName;
    protected final DfFreeGenResource _resource;
    protected final Map<String, Object> _tableMap;
    protected final Map<String, Map<String, String>> _mappingMap;
    protected final List<DfFreeGenLazyReflector> _reflectorList = DfCollectionUtil.newArrayList();

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfJsonSchemaTableLoader(String requestName, DfFreeGenResource resource, Map<String, Object> tableMap,
            Map<String, Map<String, String>> mappingMap) {
        _requestName = requestName;
        _resource = resource;
        _tableMap = tableMap;
        _mappingMap = mappingMap;
    }

    // ===================================================================================
    //                                                                          Load Table
    //                                                                          ==========
    // ; resourceMap = map:{
    //     ; resourceType = JSON_SCHEMA
    //     ; resourceFile = ../../../foo.json
    // }
    // ; outputMap = map:{
    //     ; templateFile = unused
    //     ; outputDirectory = ../src/main/java
    //     ; package = org.seasar.dbflute...
    //     ; className = unused
    // }
    // ; tableMap = map:{
    //     ; tablePath = map
    //     ; mappingMap = map:{
    //         ; type = map:{
    //             ; INTEGER = Integer
    //             ; VARCHAR = String
    //         }
    //     }
    // }
    // 
    // e.g. json_schema.json
    // MEMBER: {
    //     $comment : "member table"
    //     , MEMBER_ID : { type: "numeric", comment: "identity" }
    //     , MEMBER_NAME : { type: "varchar" }
    //     , MEMBER_STATUS_CODE : { type: "varchar" }
    //     , MEMBER_STATUS : { type: "ref" }
    // },
    // PURCHASE: {
    //     $comment : "purchase table"
    //     , PURCHASE_ID : { type: "numeric" }
    //     , MEMBER_ID : { type: "numeric" }
    //     , MEMBER : { type: "ref" }
    // },
    // MEMBER_STATUS: {
    //     $comment : "member table"
    //     , MEMBER_STATUS_CODE : { type: "varchar", comment: "code" }
    //     , MEMBER_STATUS_NAME : { type: "varchar" }
    // }
    public DfFreeGenTable loadTable() {
        prepareReflectorList();
        final Map<String, Map<String, Object>> schemaMap = DfCollectionUtil.newLinkedHashMap();
        final Map<String, Object> rootMap = decodeJsonMap();
        final String tablePath = (String) _tableMap.get("tablePath");
        if (Srl.is_Null_or_TrimmedEmpty(tablePath)) {
            throwJsonTablePathPropertyNotFoundException();
        }
        final Map<String, Object> traceMap = traceMap(rootMap, tablePath);
        for (Entry<String, Object> traceEntry : traceMap.entrySet()) {
            final String tableName = traceEntry.getKey();
            @SuppressWarnings("unchecked")
            final Map<String, Object> tableAttrMap = (Map<String, Object>) traceEntry.getValue();
            final Map<String, Object> beanMap = prepareTableBeanMap(schemaMap, tableName, tableAttrMap);
            schemaMap.put(tableName, beanMap);
        }
        reflectLazyProcess();
        prepareFinalDetermination(schemaMap);
        return new DfFreeGenTable(_tableMap, schemaMap);
    }

    protected void prepareFinalDetermination(final Map<String, Map<String, Object>> schemaMap) {
        for (Map<String, Object> tableBeanMap : schemaMap.values()) {
            @SuppressWarnings("unchecked")
            final List<Object> referrerList = (List<Object>) tableBeanMap.get("referrerList");
            if (referrerList == null) {
                tableBeanMap.put("referrerList", DfCollectionUtil.newArrayList());
            }
        }
        for (Entry<String, Map<String, Object>> entry : schemaMap.entrySet()) {
            final Map<String, Object> tableBeanMap = entry.getValue();
            final Collection<Object> tableElementList = tableBeanMap.values();
            TABLE_ELEMENT_LOOP: // to break searching
            for (Object tableElement : tableElementList) {
                if (!(tableElement instanceof List<?>)) {
                    continue;
                }
                @SuppressWarnings("unchecked")
                final List<Map<String, Object>> columnList = (List<Map<String, Object>>) tableElement;
                for (Map<String, Object> columnBeanMap : columnList) {
                    final Object refColumn = columnBeanMap.get("isRefColumn");
                    if (!refColumn.toString().equalsIgnoreCase("true")) {
                        continue;
                    }
                    tableBeanMap.put("hasRefColumn", true);
                    final String columnName = (String) columnBeanMap.get("name"); // exists
                    final Map<String, Object> refTableBeanMap = schemaMap.get(columnName); // exists
                    @SuppressWarnings("unchecked")
                    final List<Object> referrerList = (List<Object>) refTableBeanMap.get("referrerList"); // exists
                    referrerList.add(tableBeanMap);
                    break TABLE_ELEMENT_LOOP;
                }
            }
        }
    }

    // -----------------------------------------------------
    //                                            Table Bean
    //                                            ----------
    protected Map<String, Object> prepareTableBeanMap(Map<String, Map<String, Object>> schemaMap, String tableName,
            Map<String, Object> tableAttrMap) {
        final Map<String, Object> tableBeanMap = DfCollectionUtil.newLinkedHashMap();
        prepareTableName(tableName, tableBeanMap);
        final List<Map<String, Object>> columnList = DfCollectionUtil.newArrayList();
        for (Entry<String, Object> columnEntry : tableAttrMap.entrySet()) {
            final String columnName = columnEntry.getKey();
            if (columnName.startsWith("$")) { // means table attributes
                final String tableAttrKey = Srl.substringFirstRear(columnName, "$");
                tableBeanMap.put(tableAttrKey, columnEntry.getValue());
            } else {
                @SuppressWarnings("unchecked")
                final Map<String, Object> columnAttrMap = (Map<String, Object>) columnEntry.getValue();
                columnList.add(prepareColumnBeanMap(schemaMap, tableName, columnName, columnAttrMap));
            }
        }
        tableBeanMap.put("columnList", columnList);
        markTableOrColumn(tableBeanMap, true, false, false);
        return tableBeanMap;
    }

    protected void markTableOrColumn(Map<String, Object> beanMap, boolean table, boolean column, boolean refColumn) {
        beanMap.put("isTable", table);
        beanMap.put("isColumn", column);
        beanMap.put("isNormalColumn", !refColumn);
        beanMap.put("isRefColumn", refColumn);
    }

    protected void prepareTableName(String tableName, Map<String, Object> tableBeanMap) {
        tableBeanMap.put("name", tableName);
        convertByMethod(tableBeanMap, "camelizedName", "df:camelize(name)");
        convertByMethod(tableBeanMap, "capCamelName", "df:capCamel(name)");
        convertByMethod(tableBeanMap, "uncapCamelName", "df:uncapCamel(name)");
    }

    // -----------------------------------------------------
    //                                           Column Bean
    //                                           -----------
    protected Map<String, Object> prepareColumnBeanMap(final Map<String, Map<String, Object>> schemaMap,
            final String tableName, String columnName, Map<String, Object> columnAttrMap) {
        final Map<String, Object> columnBeanMap = DfCollectionUtil.newLinkedHashMap();
        final boolean refColumn = isRefColumn(columnAttrMap);
        if (refColumn) {
            prepareRefColumn(schemaMap, tableName, columnName, columnAttrMap, columnBeanMap);
        }
        prepareColumnName(columnName, columnBeanMap);
        for (Entry<String, Object> attrEntry : columnAttrMap.entrySet()) {
            final String attrKey = attrEntry.getKey();
            final Object attrValue = attrEntry.getValue();
            prepareAttrValue(columnName, columnBeanMap, attrKey, attrValue);
        }
        markTableOrColumn(columnBeanMap, false, true, refColumn);
        return columnBeanMap;
    }

    protected boolean isRefColumn(Map<String, Object> columnAttrMap) {
        final String type = (String) columnAttrMap.get("type");
        return "ref".equals(type);
    }

    protected void prepareRefColumn(final Map<String, Map<String, Object>> schemaMap, final String tableName,
            String columnName, Map<String, Object> columnAttrMap, final Map<String, Object> columnBeanMap) {
        final String refTableName = columnName; // same as column name
        addLazyReflector(new DfFreeGenLazyReflector() {
            public void reflect() {
                final Map<String, Object> tableBeanMap = schemaMap.get(refTableName);
                if (tableBeanMap == null) {
                    throwJsonTableReferenceNotFoundException(tableName, refTableName);
                }
                columnBeanMap.put("entity", tableBeanMap);
            }
        });
    }

    protected void prepareColumnName(String columnName, Map<String, Object> columnBeanMap) {
        columnBeanMap.put("name", columnName);
        convertByMethod(columnBeanMap, "camelizedName", "df:camelize(name)");
        convertByMethod(columnBeanMap, "capCamelName", "df:capCamel(name)");
        convertByMethod(columnBeanMap, "uncapCamelName", "df:uncapCamel(name)");
    }

    protected void prepareAttrValue(String columnName, Map<String, Object> columnBeanMap, String attrKey,
            Object attrValue) {
        if (attrValue instanceof String) {
            if (convertByMethod(columnBeanMap, attrKey, (String) attrValue)) {
                return;
            }
            Object resultValue = attrValue;
            final Map<String, String> mapping = _mappingMap.get(attrKey);
            if (mapping != null) {
                final String mappingValue = mapping.get(resultValue);
                if (mappingValue != null) {
                    resultValue = mappingValue;
                }
            }
            columnBeanMap.put(attrKey, resultValue);
        } else {
            columnBeanMap.put(attrKey, attrValue);
        }
    }

    // -----------------------------------------------------
    //                                         Assist Helper
    //                                         -------------
    protected boolean convertByMethod(Map<String, Object> beanMap, String key, String value) {
        return _methodConverter.processConvertMethod(_requestName, beanMap, key, value, _reflectorList);
    }

    protected void throwJsonTableReferenceNotFoundException(String currentTableName, String specifiedTableName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the table by the reference at the type. (FreeGen)");
        br.addItem("Request Name");
        br.addElement(_requestName);
        br.addItem("JSON File");
        br.addElement(_resource.getResourceFile());
        br.addItem("Current Table");
        br.addElement(currentTableName);
        br.addItem("Specified Table");
        br.addElement(specifiedTableName);
        final String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg);
    }

    protected void throwJsonTablePathPropertyNotFoundException() {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the table path in the tableMap property. (FreeGen)");
        br.addItem("Request Name");
        br.addElement(_requestName);
        br.addItem("JSON File");
        br.addElement(_resource.getResourceFile());
        br.addItem("tableMap");
        br.addElement(_tableMap);
        final String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg);
    }

    // ===================================================================================
    //                                                                      Lazy Reflector
    //                                                                      ==============
    protected void prepareReflectorList() {
        _reflectorList.clear();
    }

    protected void addLazyReflector(DfFreeGenLazyReflector reflector) {
        _reflectorList.add(reflector);
    }

    protected void reflectLazyProcess() {
        for (DfFreeGenLazyReflector reflector : _reflectorList) {
            reflector.reflect();
        }
        _reflectorList.clear();
    }

    // ===================================================================================
    //                                                                         Decode JSON
    //                                                                         ===========
    protected Map<String, Object> decodeJsonMap() {
        final String resourceFile = _resource.getResourceFile();
        return new DfJsonFreeAgent().decodeJsonMap(_requestName, resourceFile);
    }

    // ===================================================================================
    //                                                                           Trace Map
    //                                                                           =========
    protected Map<String, Object> traceMap(Map<String, Object> rootMap, String tracePath) {
        return new DfJsonFreeAgent().traceMap(_requestName, _resource, rootMap, tracePath);
    }
}
