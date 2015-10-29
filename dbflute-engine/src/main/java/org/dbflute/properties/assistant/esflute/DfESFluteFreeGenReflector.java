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
package org.dbflute.properties.assistant.esflute;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.dbflute.exception.DfIllegalPropertySettingException;
import org.dbflute.logic.manage.freegen.DfFreeGenResourceType;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 1.1.0-sp9 (2015/10/29 Thursday)
 */
public final class DfESFluteFreeGenReflector {

    private static final Logger logger = LoggerFactory.getLogger(DfESFluteFreeGenReflector.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Map<String, Object> _freeGenMap;
    protected final String _outputDirectory;
    protected final String _basePackage;
    protected final String _basePath;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfESFluteFreeGenReflector(Map<String, Object> freeGenMap, String outputDirectory, String basePackage, String basePath) {
        _freeGenMap = freeGenMap;
        _outputDirectory = outputDirectory;
        _basePackage = basePackage;
        _basePath = basePath;
    }

    // ===================================================================================
    //                                                                     Prepare FreeGen
    //                                                                     ===============
    public void reflectFrom(Map<String, Object> esfluteMap) {
        logger.info("Before ESFlute refecting, existing freeGen settigs: " + _freeGenMap.keySet());
        // ; indexMap = map:{
        //     ; fess_user = map:{
        //         ; package = user
        //     }
        //     ; fess_config = map:{
        //         ; package = config
        //     }
        // }
        @SuppressWarnings("unchecked")
        final Map<String, Object> indexMap = (Map<String, Object>) esfluteMap.get("indexMap");
        if (indexMap == null) {
            return;
        }
        for (Entry<String, Object> entry : indexMap.entrySet()) {
            final String indexName = entry.getKey();
            @SuppressWarnings("unchecked")
            final Map<String, Object> optionMap = (Map<String, Object>) entry.getValue();
            final Map<String, Map<String, Object>> elementMap = new LinkedHashMap<String, Map<String, Object>>();
            final String indexPackage = (String) optionMap.get("package");
            registerFreeGen("ESFlute" + Srl.initCap(Srl.camelize(indexName)), elementMap);
            setupResourceMap(elementMap, indexName);
            setupOutputMap(elementMap, indexName, indexPackage);
            setupTableMap(elementMap, indexName);
        }
        showFreeGenSettings();
    }

    protected void showFreeGenSettings() {
        final StringBuilder sb = new StringBuilder();
        sb.append("After ESFlute refecting, existing freeGen settigs: ").append(_freeGenMap.keySet());
        for (Entry<String, Object> entry : _freeGenMap.entrySet()) {
            sb.append("\n ").append(entry.getKey()).append(": ").append(entry.getValue());
        }
        logger.info(sb.toString());
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected void registerFreeGen(String key, Map<String, Map<String, Object>> elementMap) {
        final Object existing = _freeGenMap.get(key);
        if (existing != null) {
            String msg = "Found the existing same-name setting: key=" + key + " existing=" + existing;
            throw new DfIllegalPropertySettingException(msg);
        }
        _freeGenMap.put(key, elementMap);
    }

    protected void setupResourceMap(Map<String, Map<String, Object>> elementMap, String indexName) {
        final Map<String, Object> resourceMap = new LinkedHashMap<String, Object>();
        elementMap.put("resourceMap", resourceMap);
        resourceMap.put("resourceType", DfFreeGenResourceType.ELASTICSEARCH.name());
        resourceMap.put("resourceFile", _basePath + "/" + indexName + (_basePath.startsWith("http:") ? "" : ".json"));
    }

    protected void setupOutputMap(Map<String, Map<String, Object>> elementMap, String indexName, String indexPackage) {
        final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
        elementMap.put("outputMap", outputMap);
        outputMap.put("templateFile", "unused");
        outputMap.put("outputDirectory", _outputDirectory);
        outputMap.put("package", _basePackage + "." + indexPackage);
        outputMap.put("className", "unused");
    }

    protected void setupTableMap(Map<String, Map<String, Object>> elementMap, String indexName) {
        final Map<String, Object> tableMap = createTableMap();
        elementMap.put("tableMap", tableMap);
        tableMap.put("tablePath", "." + indexName + " -> mappings -> map");
        final Map<String, Object> mappingMap = new LinkedHashMap<String, Object>();
        tableMap.put("mappingMap", mappingMap);
        final Map<String, String> typeMap = new HashMap<String, String>();
        mappingMap.put("type", typeMap);
        typeMap.put("string", String.class.getSimpleName());
        typeMap.put("integer", Integer.class.getSimpleName());
        typeMap.put("long", Long.class.getSimpleName());
        typeMap.put("float", Float.class.getSimpleName());
        typeMap.put("double", Double.class.getSimpleName());
        typeMap.put("boolean", Boolean.class.getSimpleName());
        typeMap.put("date", LocalDateTime.class.getSimpleName());
    }

    protected Map<String, Object> createTableMap() {
        final Map<String, Object> tableMap = new LinkedHashMap<String, Object>();
        tableMap.put("isESFlute", true);
        return tableMap;
    }

    protected String getTrueLiteral() {
        return "true";
    }
}