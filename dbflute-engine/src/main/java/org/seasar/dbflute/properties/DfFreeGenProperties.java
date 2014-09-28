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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.seasar.dbflute.exception.DfIllegalPropertyTypeException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.helper.StringKeyMap;
import org.seasar.dbflute.logic.generate.packagepath.DfPackagePathHandler;
import org.seasar.dbflute.properties.assistant.freegen.DfFreeGenManager;
import org.seasar.dbflute.properties.assistant.freegen.DfFreeGenOutput;
import org.seasar.dbflute.properties.assistant.freegen.DfFreeGenRequest;
import org.seasar.dbflute.properties.assistant.freegen.DfFreeGenRequest.DfFreeGenerateResourceType;
import org.seasar.dbflute.properties.assistant.freegen.DfFreeGenResource;
import org.seasar.dbflute.properties.assistant.freegen.DfFreeGenTable;
import org.seasar.dbflute.properties.assistant.freegen.filepath.DfFilePathTableLoader;
import org.seasar.dbflute.properties.assistant.freegen.json.DfJsonKeyTableLoader;
import org.seasar.dbflute.properties.assistant.freegen.json.DfJsonSchemaTableLoader;
import org.seasar.dbflute.properties.assistant.freegen.prop.DfPropTableLoader;
import org.seasar.dbflute.properties.assistant.freegen.solr.DfSolrXmlTableLoader;
import org.seasar.dbflute.properties.assistant.freegen.xls.DfXlsTableLoader;
import org.seasar.dbflute.util.DfCollectionUtil;

/**
 * @author jflute
 */
public final class DfFreeGenProperties extends DfAbstractHelperProperties {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfFreeGenProperties(Properties prop) {
        super(prop);
    }

    // ===================================================================================
    //                                                                             Manager
    //                                                                             =======
    protected final DfFreeGenManager _manager = new DfFreeGenManager();

    public DfFreeGenManager getFreeGenManager() {
        return _manager;
    }

    // ===================================================================================
    //                                                                              Loader
    //                                                                              ======
    protected final DfPropTableLoader _propTableLoader = new DfPropTableLoader();
    protected final DfXlsTableLoader _xlsTableLoader = new DfXlsTableLoader();
    protected final DfFilePathTableLoader _filePathTableLoader = new DfFilePathTableLoader();
    protected final DfJsonKeyTableLoader _jsonKeyTableLoader = new DfJsonKeyTableLoader();
    protected final DfSolrXmlTableLoader _solrXmlTableLoader = new DfSolrXmlTableLoader();

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
    //     ; package = org.seasar.dbflute...
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
    //     ; package = org.seasar.dbflute...
    //     ; className = FooDto
    // }
    // ; tableMap = map:{
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
    protected Map<String, Object> _freeGenDefinitionMap;

    protected Map<String, Object> getFreeGenDefinitionMap() {
        if (_freeGenDefinitionMap == null) {
            final Map<String, Object> map = mapProp("torque.freeGenDefinitionMap", DEFAULT_EMPTY_MAP);
            _freeGenDefinitionMap = newLinkedHashMap();
            _freeGenDefinitionMap.putAll(map);
        }
        return _freeGenDefinitionMap;
    }

    protected List<DfFreeGenRequest> _freeGenRequestList;

    public List<DfFreeGenRequest> getFreeGenRequestList() {
        if (_freeGenRequestList != null) {
            return _freeGenRequestList;
        }
        _freeGenRequestList = new ArrayList<DfFreeGenRequest>();
        final Map<String, Object> definitionMap = getFreeGenDefinitionMap();
        final Map<String, DfFreeGenRequest> requestMap = StringKeyMap.createAsCaseInsensitive(); // for correlation relation
        for (Entry<String, Object> entry : definitionMap.entrySet()) {
            final String requestName = entry.getKey();
            final Object obj = entry.getValue();
            if (!(obj instanceof Map<?, ?>)) {
                String msg = "The property 'freeGenDefinitionMap.value' should be Map: " + obj.getClass();
                throw new DfIllegalPropertyTypeException(msg);
            }
            @SuppressWarnings("unchecked")
            final Map<String, Object> elementMap = (Map<String, Object>) obj;
            final DfFreeGenRequest request = createFreeGenerateRequest(requestName, elementMap);

            try {
                final Map<String, Object> tableMap = extractTableMap(elementMap);
                final Map<String, Map<String, String>> mappingMap = extractMappingMap(tableMap);
                final DfFreeGenResource resource = request.getResource();
                if (resource.isResourceTypeProp()) {
                    request.setTable(loadTableFromProp(requestName, resource, tableMap, mappingMap, requestMap));
                } else if (resource.isResourceTypeXls()) {
                    request.setTable(loadTableFromXls(requestName, resource, tableMap, mappingMap));
                } else if (resource.isResourceTypeFilePath()) {
                    request.setTable(loadTableFromFilePath(requestName, resource, tableMap, mappingMap));
                } else if (resource.isResourceTypeJsonKey()) {
                    request.setTable(loadTableFromJsonKey(requestName, resource, tableMap, mappingMap));
                } else if (resource.isResourceTypeJsonSchema()) {
                    request.setTable(loadTableFromJsonSchema(requestName, resource, tableMap, mappingMap));
                } else if (resource.isResourceTypeSolr()) {
                    request.setTable(loadTableFromSolrXml(requestName, resource, tableMap, mappingMap));
                } else {
                    throwFreeGenResourceTypeUnknownException(requestName, resource);
                }
            } catch (IOException e) {
                String msg = "Failed to load table: request=" + request;
                throw new IllegalStateException(msg, e);
            }

            final DfPackagePathHandler packagePathHandler = new DfPackagePathHandler(getBasicProperties());
            request.setPackagePathHandler(packagePathHandler);
            _freeGenRequestList.add(request);
            requestMap.put(requestName, request);
        }
        return _freeGenRequestList;
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> extractTableMap(Map<String, Object> elementMap) {
        final Object obj = elementMap.get("tableMap");
        if (obj == null) {
            return DfCollectionUtil.emptyMap();
        }
        return (Map<String, Object>) obj;
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Map<String, String>> extractMappingMap(Map<String, Object> tableMap) {
        final Object obj = tableMap.get("mappingMap");
        if (obj == null) {
            return DfCollectionUtil.emptyMap();
        }
        return (Map<String, Map<String, String>>) obj;
    }

    protected DfFreeGenRequest createFreeGenerateRequest(String requestName, Map<String, Object> elementMap) {
        final DfFreeGenResource resource;
        {
            @SuppressWarnings("unchecked")
            final Map<String, String> resourceMap = (Map<String, String>) elementMap.get("resourceMap");
            final String baseDir = resourceMap.get("baseDir");
            final String resourceTypeStr = resourceMap.get("resourceType"); // required
            final DfFreeGenerateResourceType resourceType = DfFreeGenerateResourceType.valueOf(resourceTypeStr);
            final String resourceFile = resourceMap.get("resourceFile");
            final String encoding = resourceMap.get("encoding");
            resource = new DfFreeGenResource(baseDir, resourceType, resourceFile, encoding);
        }
        final DfFreeGenOutput output;
        {
            @SuppressWarnings("unchecked")
            final Map<String, String> outputMap = (Map<String, String>) elementMap.get("outputMap");
            final String templateFile = resource.resolveBaseDir(outputMap.get("templateFile"));
            final String outputDirectory = resource.resolveBaseDir(outputMap.get("outputDirectory"));
            final String pkg = outputMap.get("package");
            final String className = outputMap.get("className");
            output = new DfFreeGenOutput(templateFile, outputDirectory, pkg, className);
        }
        return new DfFreeGenRequest(_manager, requestName, resource, output);
    }

    protected DfFreeGenTable loadTableFromProp(String requestName, DfFreeGenResource resource,
            Map<String, Object> tableMap, Map<String, Map<String, String>> mappingMap,
            Map<String, DfFreeGenRequest> requestMap) throws IOException {
        return _propTableLoader.loadTable(requestName, resource, tableMap, mappingMap, requestMap);
    }

    protected DfFreeGenTable loadTableFromXls(String requestName, DfFreeGenResource resource,
            Map<String, Object> tableMap, Map<String, Map<String, String>> mappingMap) throws IOException {
        return _xlsTableLoader.loadTable(requestName, resource, tableMap, mappingMap);
    }

    protected DfFreeGenTable loadTableFromFilePath(String requestName, DfFreeGenResource resource,
            Map<String, Object> tableMap, Map<String, Map<String, String>> mappingMap) throws IOException {
        return _filePathTableLoader.loadTable(requestName, resource, tableMap, mappingMap);
    }

    protected DfFreeGenTable loadTableFromJsonKey(String requestName, DfFreeGenResource resource,
            Map<String, Object> tableMap, Map<String, Map<String, String>> mappingMap) throws IOException {
        return _jsonKeyTableLoader.loadTable(requestName, resource, tableMap, mappingMap);
    }

    protected DfFreeGenTable loadTableFromJsonSchema(String requestName, DfFreeGenResource resource,
            Map<String, Object> tableMap, Map<String, Map<String, String>> mappingMap) throws IOException {
        final DfJsonSchemaTableLoader loader = new DfJsonSchemaTableLoader(requestName, resource, tableMap, mappingMap);
        return loader.loadTable();
    }

    protected DfFreeGenTable loadTableFromSolrXml(String requestName, DfFreeGenResource resource,
            Map<String, Object> tableMap, Map<String, Map<String, String>> mappingMap) throws IOException {
        return _solrXmlTableLoader.loadTable(requestName, resource, tableMap, mappingMap);
    }

    protected void throwFreeGenResourceTypeUnknownException(String requestName, DfFreeGenResource resource) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Unknown resource type for FreeGen.");
        br.addItem("Request Name");
        br.addElement(requestName);
        br.addItem("Resource Type");
        br.addElement(resource.getResourceType());
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg);
    }

    // ===================================================================================
    //                                                                     Property Helper
    //                                                                     ===============
    protected String getPropertyRequired(String key) {
        final String value = getProperty(key);
        if (value == null || value.trim().length() == 0) {
            String msg = "The property '" + key + "' should not be null or empty:";
            msg = msg + " simpleDtoDefinitionMap=" + getFreeGenDefinitionMap();
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
        return (String) getFreeGenDefinitionMap().get(key);
    }

    protected boolean isProperty(String key, boolean defaultValue) {
        return isProperty(key, defaultValue, getFreeGenDefinitionMap());
    }
}