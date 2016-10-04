/*
 * Copyright 2014-2016 the original author or authors.
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
package org.dbflute.logic.manage.freegen.table.elasticsearch;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dbflute.DfBuildProperties;
import org.dbflute.exception.DfIllegalPropertySettingException;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.logic.manage.freegen.DfFreeGenMapProp;
import org.dbflute.logic.manage.freegen.DfFreeGenResource;
import org.dbflute.logic.manage.freegen.DfFreeGenMetaData;
import org.dbflute.logic.manage.freegen.exception.DfFreeGenCancelException;
import org.dbflute.logic.manage.freegen.reflector.DfFreeGenLazyReflector;
import org.dbflute.logic.manage.freegen.reflector.DfFreeGenMethodConverter;
import org.dbflute.logic.manage.freegen.table.json.DfJsonFreeAgent;
import org.dbflute.logic.manage.freegen.table.json.DfJsonFreeAgent.DfJsonUrlCannotRequestException;
import org.dbflute.properties.DfESFluteProperties;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfElasticsearchLoadingAgent {

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
    public DfElasticsearchLoadingAgent(String requestName, DfFreeGenResource resource, DfFreeGenMapProp mapProp) {
        _requestName = requestName;
        _resource = resource;
        _mapProp = mapProp;
    }

    // ===================================================================================
    //                                                                          Load Table
    //                                                                          ==========
    // ; resourceMap = map:{
    //     ; resourceType = ELASTICSEARCH
    //     ; resourceFile = ../../../foo.json
    // }
    // ; outputMap = map:{
    //     ; templateFile = unused
    //     ; outputDirectory = ../src/main/java
    //     ; package = org.dbflute...
    //     ; className = unused
    // }
    // ; tableMap = map:{
    //     ; tablePath = .fess_config -> mappings -> map
    //     ; mappingMap = map:{
    //         ; type = map:{
    //             ; INTEGER = Integer
    //             ; VARCHAR = String
    //         }
    //     }
    // }
    // 
    // e.g. fess_schema.json
    // {
    //   ".fess_config" : {
    //     "aliases" : { },
    //     "mappings" : {
    //       "user_info" : {
    //         "_all" : { "enabled" : false },
    //         "_id" : { "path" : "id" },
    //         "properties" : {
    //           "code" : {
    //             "type" : "string",
    //             "index" : "not_analyzed"
    //           },
    //           "createdTime" : {
    //             "type" : "long"
    //           },
    //           "id" : {
    //             "type" : "string",
    //             "index" : "not_analyzed"
    //           },
    //           "updatedTime" : {
    //             "type" : "long"
    //           }
    //         }
    //       },
    //       "web_config_to_label" : {
    //         "_all" : { "enabled" : false },
    //         ...
    //       },
    //       ...
    //     }
    //   }
    // }
    public DfFreeGenMetaData loadTable() {
        final Map<String, Object> tableMap = _mapProp.getOptionMap();
        prepareReflectorList();
        final Map<String, Map<String, Object>> schemaMap = DfCollectionUtil.newLinkedHashMap();
        final String resourceFile = _resource.getResourceFile();
        final Map<String, Object> rootMap;
        try {
            rootMap = decodeJsonMap(resourceFile);
        } catch (DfJsonUrlCannotRequestException e) {
            if (getESFluteProperties().isContinueIfUrlFailure()) {
                throw new DfFreeGenCancelException("Cannot access to the URL: " + resourceFile, e);
            } else {
                throw e;
            }
        }
        final String tablePath = (String) tableMap.get("tablePath");
        if (Srl.is_Null_or_TrimmedEmpty(tablePath)) {
            throwJsonTablePathPropertyNotFoundException();
        }
        final Map<String, Object> esMap = DfCollectionUtil.newHashMap();
        esMap.put("index", rootMap.keySet().iterator().next());
        @SuppressWarnings("unchecked")
        final Map<String, Object> indexSettingsMap = (Map<String, Object>) rootMap.values().iterator().next();
        esMap.putAll(indexSettingsMap);
        final Map<String, Object> traceMap = traceMap(rootMap, tablePath);
        for (Entry<String, Object> traceEntry : traceMap.entrySet()) {
            final String tableName = traceEntry.getKey();
            @SuppressWarnings("unchecked")
            final Map<String, Object> tableAttrMap = (Map<String, Object>) traceEntry.getValue();
            final Map<String, Object> beanMap = prepareTableBeanMap(schemaMap, tableName, tableAttrMap);
            beanMap.put("indexSettings", esMap);
            schemaMap.put(tableName, beanMap);
        }
        reflectLazyProcess();
        prepareFinalDetermination(schemaMap);
        return new DfFreeGenMetaData(tableMap, schemaMap);
    }

    protected DfESFluteProperties getESFluteProperties() {
        return DfBuildProperties.getInstance().getESFluteProperties();
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
        final String columnMarkKey = "properties";
        for (Entry<String, Object> entry : tableAttrMap.entrySet()) {
            final String key = entry.getKey();
            if (columnMarkKey.equals(key)) { // is column
                continue;
            }
            tableBeanMap.put(key, entry.getValue());
        }
        final List<Map<String, Object>> columnList = DfCollectionUtil.newArrayList();
        @SuppressWarnings("unchecked")
        final Map<String, Object> columnMap = (Map<String, Object>) tableAttrMap.get(columnMarkKey);
        for (Entry<String, Object> columnEntry : columnMap.entrySet()) {
            final String columnName = columnEntry.getKey();
            @SuppressWarnings("unchecked")
            final Map<String, Object> columnAttrMap = (Map<String, Object>) columnEntry.getValue();
            adjustDateFormat(columnAttrMap);
            columnList.add(prepareColumnBeanMap(schemaMap, tableName, columnName, columnAttrMap));
        }
        tableBeanMap.put("columnList", columnList);
        markTableOrColumn(tableBeanMap, true, false, false);
        return tableBeanMap;
    }

    protected void adjustDateFormat(Map<String, Object> columnAttrMap) {
        String formatName = null;
        for (Entry<String, Object> attrEntry : columnAttrMap.entrySet()) {
            final String key = attrEntry.getKey();
            if ("format".equals(key) && "date".equals(columnAttrMap.get("type"))) {
                formatName = (String) attrEntry.getValue();
                break;
            }
        }
        if (formatName != null) {
            final Map<String, Object> tableMap = _mapProp.getOptionMap();
            @SuppressWarnings("unchecked")
            final Map<String, Object> mappingMap = (Map<String, Object>) tableMap.get("mappingMap");
            @SuppressWarnings("unchecked")
            final Map<String, String> typeMapping = (Map<String, String>) mappingMap.get("type");
            final String formattedType = "date@" + formatName;
            if (typeMapping.containsKey(formattedType)) {
                columnAttrMap.put("type", formattedType); // override
            }
        }
    }

    protected void markTableOrColumn(Map<String, Object> beanMap, boolean table, boolean column, boolean refColumn) {
        beanMap.put("isTable", table);
        beanMap.put("isColumn", column);
        beanMap.put("isNormalColumn", !refColumn);
        beanMap.put("isRefColumn", refColumn);
    }

    protected void prepareTableName(String tableName, Map<String, Object> tableBeanMap) {
        tableBeanMap.put("name", tableName);
        convertByMethod(tableBeanMap, "className", "df:camelize(name)"); // used as output file name
        convertByMethod(tableBeanMap, "camelizedName", "df:camelize(name)");
        convertByMethod(tableBeanMap, "capCamelName", "df:capCamel(name)");
        convertByMethod(tableBeanMap, "uncapCamelName", "df:uncapCamel(name)");
    }

    // -----------------------------------------------------
    //                                           Column Bean
    //                                           -----------
    protected Map<String, Object> prepareColumnBeanMap(Map<String, Map<String, Object>> schemaMap, String tableName, String columnName,
            Map<String, Object> columnAttrMap) {
        final Map<String, Object> columnBeanMap = DfCollectionUtil.newLinkedHashMap();
        // unsupported for now
        //final boolean refColumn = isRefColumn(columnAttrMap);
        //if (refColumn) {
        //    prepareRefColumn(schemaMap, tableName, columnName, columnAttrMap, columnBeanMap);
        //}
        final boolean refColumn = false;
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

    protected void prepareRefColumn(Map<String, Map<String, Object>> schemaMap, final String tableName, String columnName,
            Map<String, Object> columnAttrMap, Map<String, Object> columnBeanMap) {
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

    protected void prepareAttrValue(String columnName, Map<String, Object> columnBeanMap, String attrKey, Object attrValue) {
        if (attrValue instanceof String) {
            if (convertByMethod(columnBeanMap, attrKey, (String) attrValue)) {
                return;
            }
            Object resultValue = attrValue;
            final Map<String, Map<String, String>> mappingMap = _mapProp.getMappingMap();
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
        br.addElement(_mapProp.getOptionMap());
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
    protected Map<String, Object> decodeJsonMap(String resourceFile) {
        return new DfJsonFreeAgent().decodeJsonMap(_requestName, resourceFile);
    }

    // ===================================================================================
    //                                                                           Trace Map
    //                                                                           =========
    protected Map<String, Object> traceMap(Map<String, Object> rootMap, String tracePath) {
        return new DfJsonFreeAgent().traceMap(_requestName, _resource, rootMap, tracePath);
    }
}
