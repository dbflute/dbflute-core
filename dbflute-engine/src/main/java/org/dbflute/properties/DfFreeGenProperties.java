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
package org.dbflute.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.dbflute.DfBuildProperties;
import org.dbflute.exception.DfIllegalPropertySettingException;
import org.dbflute.exception.DfIllegalPropertyTypeException;
import org.dbflute.helper.StringKeyMap;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.logic.generate.packagepath.DfPackagePathHandler;
import org.dbflute.logic.manage.freegen.DfFreeGenManager;
import org.dbflute.logic.manage.freegen.DfFreeGenMapProp;
import org.dbflute.logic.manage.freegen.DfFreeGenOutput;
import org.dbflute.logic.manage.freegen.DfFreeGenRequest;
import org.dbflute.logic.manage.freegen.DfFreeGenResource;
import org.dbflute.logic.manage.freegen.DfFreeGenResourceType;
import org.dbflute.logic.manage.freegen.DfFreeGenTableLoader;
import org.dbflute.util.DfCollectionUtil;

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
    protected Map<String, Object> _freeGenMap;

    protected Map<String, Object> getFreeGenMap() {
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

    protected List<DfFreeGenRequest> _freeGenRequestList;

    public List<DfFreeGenRequest> getFreeGenRequestList() {
        if (_freeGenRequestList != null) {
            return _freeGenRequestList;
        }
        _freeGenRequestList = new ArrayList<DfFreeGenRequest>();
        final Map<String, Object> freeGenMap = getFreeGenMap();
        final Map<String, DfFreeGenRequest> requestMap = StringKeyMap.createAsCaseInsensitive(); // for correlation relation
        for (Entry<String, Object> entry : freeGenMap.entrySet()) {
            final String requestName = entry.getKey();
            final Object obj = entry.getValue();
            if (!(obj instanceof Map<?, ?>)) {
                String msg = "The property 'freeGenDefinitionMap.value' should be Map: " + obj.getClass();
                throw new DfIllegalPropertyTypeException(msg);
            }
            @SuppressWarnings("unchecked")
            final Map<String, Object> elementMap = (Map<String, Object>) obj;
            final DfFreeGenRequest request = createFreeGenerateRequest(requestName, elementMap);

            final Map<String, Object> tableMap = extractTableMap(elementMap);
            final Map<String, Map<String, String>> mappingMap = extractMappingMap(tableMap);
            final DfFreeGenMapProp mapProp = new DfFreeGenMapProp(tableMap, mappingMap, requestMap);

            final DfFreeGenResource resource = request.getResource();
            final DfFreeGenTableLoader tableLoader = DfFreeGenResourceType.tableLoaderMap.get(request.getResourceType());
            if (tableLoader == null) {
                throwFreeGenResourceTypeUnknownException(requestName, resource);
            }
            request.setTable(tableLoader.loadTable(requestName, resource, mapProp));

            request.setPackagePathHandler(new DfPackagePathHandler(getBasicProperties()));
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
            final DfFreeGenResourceType resourceType = DfFreeGenResourceType.valueOf(resourceTypeStr);
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
            final String fileExt = outputMap.getOrDefault("fileExt", getLanguageClassExt());
            output = new DfFreeGenOutput(templateFile, outputDirectory, pkg, className, fileExt);
        }
        return new DfFreeGenRequest(_manager, requestName, resource, output);
    }

    protected String getLanguageClassExt() {
        final DfBasicProperties basicProp = DfBuildProperties.getInstance().getBasicProperties();
        return basicProp.getLanguageDependency().getLanguageGrammar().getClassFileExtension();
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