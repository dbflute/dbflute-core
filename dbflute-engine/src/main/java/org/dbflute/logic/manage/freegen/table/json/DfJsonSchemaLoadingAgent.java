/*
 * Copyright 2014-2015 the original author or authors.
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
package org.dbflute.logic.manage.freegen.table.json;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dbflute.exception.DfIllegalPropertySettingException;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.logic.manage.freegen.DfFreeGenMapProp;
import org.dbflute.logic.manage.freegen.DfFreeGenResource;
import org.dbflute.logic.manage.freegen.DfFreeGenTable;
import org.dbflute.logic.manage.freegen.reflector.DfFreeGenLazyReflector;
import org.dbflute.logic.manage.freegen.reflector.DfFreeGenMethodConverter;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfJsonSchemaLoadingAgent {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _requestName;
    protected final DfFreeGenResource _resource;
    protected final DfFreeGenMapProp _mapProp;
    protected final DfFreeGenMethodConverter _methodConverter = new DfFreeGenMethodConverter();
    protected final List<DfFreeGenLazyReflector> _reflectorList = DfCollectionUtil.newArrayList();

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfJsonSchemaLoadingAgent(String requestName, DfFreeGenResource resource, DfFreeGenMapProp mapProp) {
        this._requestName = requestName;
        this._resource = resource;
        this._mapProp = mapProp;
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
    //     ; package = org.dbflute...
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
        final Map<String, Object> tableMap = _mapProp.getTableMap();
        prepareReflectorList();
        final Map<String, Map<String, Object>> schemaMap = DfCollectionUtil.newLinkedHashMap();
        final Map<String, Object> rootMap = decodeJsonMap(_requestName, _resource);
        final String tablePath = (String) tableMap.get("tablePath");
        if (Srl.is_Null_or_TrimmedEmpty(tablePath)) {
            throwJsonTablePathPropertyNotFoundException(_requestName, _resource, _mapProp);
        }
        final Map<String, Object> traceMap = traceMap(_requestName, _resource, rootMap, tablePath);
        for (Entry<String, Object> traceEntry : traceMap.entrySet()) {
            final String tableName = traceEntry.getKey();
            @SuppressWarnings("unchecked")
            final Map<String, Object> tableAttrMap = (Map<String, Object>) traceEntry.getValue();
            final Map<String, Object> beanMap = prepareTableBeanMap(_requestName, schemaMap, tableName, tableAttrMap, _mapProp);
            schemaMap.put(tableName, beanMap);
        }
        reflectLazyProcess();
        prepareFinalDetermination(schemaMap);
        return new DfFreeGenTable(tableMap, schemaMap);
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
    protected Map<String, Object> prepareTableBeanMap(String requestName, Map<String, Map<String, Object>> schemaMap, String tableName,
            Map<String, Object> tableAttrMap, DfFreeGenMapProp mapProp) {
        final Map<String, Object> tableBeanMap = DfCollectionUtil.newLinkedHashMap();
        prepareTableName(requestName, tableName, tableBeanMap);
        final List<Map<String, Object>> columnList = DfCollectionUtil.newArrayList();
        for (Entry<String, Object> columnEntry : tableAttrMap.entrySet()) {
            final String columnName = columnEntry.getKey();
            if (columnName.startsWith("$")) { // means table attributes
                final String tableAttrKey = Srl.substringFirstRear(columnName, "$");
                tableBeanMap.put(tableAttrKey, columnEntry.getValue());
            } else {
                @SuppressWarnings("unchecked")
                final Map<String, Object> columnAttrMap = (Map<String, Object>) columnEntry.getValue();
                columnList.add(prepareColumnBeanMap(requestName, mapProp, schemaMap, tableName, columnName, columnAttrMap));
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

    protected void prepareTableName(String requestName, String tableName, Map<String, Object> tableBeanMap) {
        tableBeanMap.put("name", tableName);
        convertByMethod(requestName, tableBeanMap, "camelizedName", "df:camelize(name)");
        convertByMethod(requestName, tableBeanMap, "capCamelName", "df:capCamel(name)");
        convertByMethod(requestName, tableBeanMap, "uncapCamelName", "df:uncapCamel(name)");
    }

    // -----------------------------------------------------
    //                                           Column Bean
    //                                           -----------
    protected Map<String, Object> prepareColumnBeanMap(String requestName, DfFreeGenMapProp mapProp,
            Map<String, Map<String, Object>> schemaMap, String tableName, String columnName, Map<String, Object> columnAttrMap) {
        final Map<String, Object> columnBeanMap = DfCollectionUtil.newLinkedHashMap();
        final boolean refColumn = isRefColumn(columnAttrMap);
        if (refColumn) {
            prepareRefColumn(requestName, schemaMap, tableName, columnName, columnAttrMap, columnBeanMap);
        }
        prepareColumnName(requestName, columnName, columnBeanMap);
        for (Entry<String, Object> attrEntry : columnAttrMap.entrySet()) {
            final String attrKey = attrEntry.getKey();
            final Object attrValue = attrEntry.getValue();
            prepareAttrValue(requestName, mapProp, columnName, columnBeanMap, attrKey, attrValue);
        }
        markTableOrColumn(columnBeanMap, false, true, refColumn);
        return columnBeanMap;
    }

    protected boolean isRefColumn(Map<String, Object> columnAttrMap) {
        final String type = (String) columnAttrMap.get("type");
        return "ref".equals(type);
    }

    protected void prepareRefColumn(String requestName, Map<String, Map<String, Object>> schemaMap, final String tableName,
            String columnName, Map<String, Object> columnAttrMap, Map<String, Object> columnBeanMap) {
        final String refTableName = columnName; // same as column name
        addLazyReflector(new DfFreeGenLazyReflector() {
            public void reflect() {
                final Map<String, Object> tableBeanMap = schemaMap.get(refTableName);
                if (tableBeanMap == null) {
                    throwJsonTableReferenceNotFoundException(requestName, tableName, refTableName);
                }
                columnBeanMap.put("entity", tableBeanMap);
            }
        });
    }

    protected void prepareColumnName(String requestName, String columnName, Map<String, Object> columnBeanMap) {
        columnBeanMap.put("name", columnName);
        convertByMethod(requestName, columnBeanMap, "camelizedName", "df:camelize(name)");
        convertByMethod(requestName, columnBeanMap, "capCamelName", "df:capCamel(name)");
        convertByMethod(requestName, columnBeanMap, "uncapCamelName", "df:uncapCamel(name)");
    }

    protected void prepareAttrValue(String requestName, DfFreeGenMapProp mapProp, String columnName, Map<String, Object> columnBeanMap,
            String attrKey, Object attrValue) {
        if (attrValue instanceof String) {
            if (convertByMethod(requestName, columnBeanMap, attrKey, (String) attrValue)) {
                return;
            }
            Object resultValue = attrValue;
            final Map<String, Map<String, String>> mappingMap = mapProp.getMappingMap();
            final Map<String, String> mapping = mappingMap.get(attrKey);
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
    protected boolean convertByMethod(String requestName, Map<String, Object> beanMap, String key, String value) {
        return _methodConverter.processConvertMethod(requestName, beanMap, key, value, _reflectorList);
    }

    protected void throwJsonTableReferenceNotFoundException(String requestName, String currentTableName, String specifiedTableName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the table by the reference at the type. (FreeGen)");
        br.addItem("Request Name");
        br.addElement(requestName);
        br.addItem("Current Table");
        br.addElement(currentTableName);
        br.addItem("Specified Table");
        br.addElement(specifiedTableName);
        final String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg);
    }

    protected void throwJsonTablePathPropertyNotFoundException(String requestName, DfFreeGenResource resource, DfFreeGenMapProp mapProp) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the table path in the tableMap property. (FreeGen)");
        br.addItem("Request Name");
        br.addElement(requestName);
        br.addItem("JSON File");
        br.addElement(resource.getResourceFile());
        br.addItem("tableMap");
        br.addElement(mapProp.getTableMap());
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
    protected Map<String, Object> decodeJsonMap(String requestName, DfFreeGenResource resource) {
        final String resourceFile = resource.getResourceFile();
        return new DfJsonFreeAgent().decodeJsonMap(requestName, resourceFile);
    }

    // ===================================================================================
    //                                                                           Trace Map
    //                                                                           =========
    protected Map<String, Object> traceMap(String requestName, DfFreeGenResource resource, Map<String, Object> rootMap, String tracePath) {
        return new DfJsonFreeAgent().traceMap(requestName, resource, rootMap, tracePath);
    }
}
