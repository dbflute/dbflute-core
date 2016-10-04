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
package org.dbflute.logic.manage.freegen;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import org.apache.torque.engine.database.model.AppData;
import org.apache.torque.engine.database.model.Database;
import org.dbflute.DfBuildProperties;
import org.dbflute.exception.DfIllegalPropertySettingException;
import org.dbflute.exception.DfIllegalPropertyTypeException;
import org.dbflute.helper.StringKeyMap;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.logic.generate.packagepath.DfPackagePathHandler;
import org.dbflute.logic.jdbc.schemaxml.DfSchemaXmlReader;
import org.dbflute.logic.manage.freegen.exception.DfFreeGenCancelException;
import org.dbflute.properties.DfBasicProperties;
import org.dbflute.properties.DfFreeGenProperties;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 */
public class DfFreeGenInitializer {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger _log = LoggerFactory.getLogger(DfFreeGenInitializer.class);
    protected static final DfFreeGenManager _manager = new DfFreeGenManager();

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected List<DfFreeGenRequest> _freeGenRequestList;

    // ===================================================================================
    //                                                                          Initialize
    //                                                                          ==========
    public List<DfFreeGenRequest> initialize(Predicate<String> requestDeterminer) {
        if (_freeGenRequestList != null) {
            return _freeGenRequestList;
        }
        _freeGenRequestList = new ArrayList<DfFreeGenRequest>();
        final Map<String, Object> freeGenMap = getFreeGenProperties().getFreeGenMap();
        final Map<String, DfFreeGenRequest> requestMap = StringKeyMap.createAsCaseInsensitive(); // for correlation relation
        for (Entry<String, Object> entry : freeGenMap.entrySet()) {
            final String requestName = entry.getKey();
            if (!requestDeterminer.test(requestName)) {
                continue;
            }
            final Object obj = entry.getValue();
            if (!(obj instanceof Map<?, ?>)) {
                String msg = "The property 'freeGenDefinitionMap.value' should be Map: " + obj.getClass();
                throw new DfIllegalPropertyTypeException(msg);
            }
            @SuppressWarnings("unchecked")
            final Map<String, Object> elementMap = (Map<String, Object>) obj;
            final DfFreeGenRequest request = createFreeGenerateRequest(requestName, elementMap);

            final Map<String, Object> optionMap = extractOptionMap(elementMap);
            setupReservedOption(requestName, optionMap);
            final Map<String, Map<String, String>> mappingMap = extractMappingMap(optionMap);
            final DfFreeGenMapProp mapProp = new DfFreeGenMapProp(optionMap, mappingMap, requestMap);
            final DfFreeGenResource resource = request.getResource();

            final DfFreeGenTableLoader tableLoader = DfFreeGenResourceType.tableLoaderMap.get(request.getResource().getResourceType());
            if (tableLoader == null) {
                throwFreeGenResourceTypeUnknownException(requestName, resource);
            }
            final DfFreeGenMetaData metaData;
            try {
                metaData = tableLoader.loadTable(requestName, resource, mapProp);
            } catch (DfFreeGenCancelException continued) {
                showCancelledRequest(requestName, continued);
                continue;
            }
            request.setMetaData(metaData);
            request.setPackagePathHandler(new DfPackagePathHandler(getBasicProperties()));
            _freeGenRequestList.add(request);
            requestMap.put(requestName, request);
        }
        return _freeGenRequestList;
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> extractOptionMap(Map<String, Object> elementMap) {
        Object obj = elementMap.get("optionMap");
        if (obj == null) {
            obj = elementMap.get("tableMap"); // for compatible
            if (obj == null) {
                return new LinkedHashMap<String, Object>(); // may be put later
            }
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
            final String outputDirectory = resource.resolveBaseDir(outputMap.get("outputDirectory"));
            final String pkg = outputMap.get("package");
            final String templateFile = resource.resolveBaseDir(outputMap.get("templateFile"));
            final String className = outputMap.get("className");
            final String fileExt = outputMap.getOrDefault("fileExt", getLanguageClassExt());
            output = new DfFreeGenOutput(outputDirectory, pkg, templateFile, className, fileExt);
        }
        return new DfFreeGenRequest(_manager, requestName, resource, output);
    }

    protected String getLanguageClassExt() {
        final DfBasicProperties basicProp = getBasicProperties();
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

    protected void showCancelledRequest(final String requestName, DfFreeGenCancelException continued) {
        // e.g.
        // - *Cancelled the freeGen request: ESFluteFessConfig
        // -   |-DfFreeGenCancelException: Cannot access to the URL: http://localhost:8080/fess_config
        // -       |-DfJsonUrlCannotRequestException: Failed to access to the URL: http://localhost:8080/fess_config
        // -           |-ConnectException: Connection refused
        _log.info("*Cancelled the freeGen request: " + requestName);
        _log.info("  |-" + continued.getClass().getSimpleName() + ": " + continued.getMessage());
        final Throwable cause = continued.getCause();
        if (cause != null && !cause.equals(continued)) {
            _log.info("      |-" + cause.getClass().getSimpleName() + ": " + cause.getMessage());
            final Throwable more = cause.getCause();
            if (more != null && !more.equals(cause)) {
                _log.info("          |-" + more.getClass().getSimpleName() + ": " + more.getMessage());
            }
        }
    }

    // ===================================================================================
    //                                                                     Reserved Option
    //                                                                     ===============
    protected void setupReservedOption(String requestName, Map<String, Object> optionMap) {
        @SuppressWarnings("unchecked")
        final Map<String, Object> databaseMap = (Map<String, Object>) optionMap.get("databaseMap");
        if (databaseMap == null) {
            return;
        }
        for (Entry<String, Object> entry : databaseMap.entrySet()) {
            final String databaseName = entry.getKey();
            @SuppressWarnings("unchecked")
            final Map<String, Object> settingsMap = (Map<String, Object>) entry.getValue();
            final String schemaDir = (String) settingsMap.get("schemaDir");
            if (Srl.is_Null_or_TrimmedEmpty(schemaDir)) {
                String msg = "Not found the schemaDir property in the " + databaseName + " for " + requestName;
                throw new DfIllegalPropertySettingException(msg);
            }
            final String schemaXml = schemaDir + "/project-schema-" + databaseName + ".xml";
            final DfSchemaXmlReader reader = DfSchemaXmlReader.createAsFlexibleToManage(schemaXml);
            final Database database = prepareDatabase(reader);
            settingsMap.put("instance", database);
        }
    }

    protected Database prepareDatabase(DfSchemaXmlReader reader) {
        final AppData appData = reader.read();
        final Database database = appData.getDatabase();

        // same as ControlGenerateJava.vm
        database.initializeVersion(90);
        database.initializeAdditionalPrimaryKey();
        database.initializeAdditionalUniqueKey();
        database.initializeAdditionalForeignKey();
        database.initializeClassificationDeployment();
        database.initializeIncludeQuery();
        database.checkProperties();
        return database;
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    private DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    protected DfBasicProperties getBasicProperties() {
        return getProperties().getBasicProperties();
    }

    protected DfFreeGenProperties getFreeGenProperties() {
        return getProperties().getFreeGenProperties();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public static DfFreeGenManager getManager() {
        return _manager;
    }
}
