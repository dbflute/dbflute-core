/*
 * Copyright 2014-2025 the original author or authors.
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    protected final DfESFluteSupportContainer _supportContainer;
    protected final String _elasticsearchVersion; // not null, default is 9.9.9

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfESFluteFreeGenReflector(Map<String, Object> freeGenMap, String outputDirectory, String basePackage, String basePath,
            DfESFluteSupportContainer supportContainer, String elasticsearchVersion) {
        _freeGenMap = freeGenMap;
        _outputDirectory = outputDirectory;
        _basePackage = basePackage;
        _basePath = basePath;
        _supportContainer = supportContainer;
        _elasticsearchVersion = elasticsearchVersion;
    }

    public static enum DfESFluteSupportContainer {
        LASTA_DI, NONE
    }

    // ===================================================================================
    //                                                                     Prepare FreeGen
    //                                                                     ===============
    public void reflectFrom(Map<String, Object> esfluteMap) {
        logger.info("Before ESFlute refecting, existing freeGen settigs: " + _freeGenMap.keySet());
        // ; indexMap = map:{
        //     ; .fess_user = map:{
        //         ; package = user
        //     }
        //     ; .fess_config = map:{
        //         ; package = config
        //         ; esclientDiFile = esclient.xml
        //         ; esfluteDiFile = esflute_fess_config.xml
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
            final String esclientDiFile = (String) optionMap.get("esclientDiFile"); // null allowed
            final String esfluteDiFile = (String) optionMap.get("esfluteDiFile"); // null allowed
            registerFreeGen("ESFlute" + Srl.initCap(Srl.camelize(indexName)), elementMap);
            setupResourceMap(elementMap, indexName);
            setupOutputMap(elementMap, indexName, indexPackage);
            setupTableMap(elementMap, indexName, indexPackage, esclientDiFile, esfluteDiFile);
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
        final String resourceFile;
        if (_basePath.startsWith("http:")) {
            resourceFile = _basePath + "/" + indexName;
        } else { // relative path
            resourceFile = _basePath + "/" + buildIndexSmartName(indexName) + ".json";
        }
        resourceMap.put("resourceFile", resourceFile);
    }

    protected void setupOutputMap(Map<String, Map<String, Object>> elementMap, String indexName, String indexPackage) {
        final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
        elementMap.put("outputMap", outputMap);
        outputMap.put("templateFile", "unused");
        outputMap.put("outputDirectory", _outputDirectory);
        outputMap.put("package", buildIndexPackage(indexPackage));
        outputMap.put("className", "unused");
    }

    protected String buildIndexPackage(String indexPackage) {
        return _basePackage + "." + indexPackage;
    }

    protected void setupTableMap(Map<String, Map<String, Object>> elementMap, String indexName, String indexPackage, String esclientDiFile,
            String esfluteDiFile) {
        final Map<String, Object> tableMap = createTableMap();
        elementMap.put("tableMap", tableMap);
        tableMap.put("tablePath", indexName + " -> mappings -> map");
        final Map<String, Object> mappingMap = new LinkedHashMap<String, Object>();
        tableMap.put("mappingMap", mappingMap);
        mappingMap.put("type", prepareTypeMap());
        tableMap.put("resourcesDir", "../resources");
        tableMap.put("namespace", buildIndexSmartName(indexName));
        tableMap.put("exbhvPackage", buildIndexPackage(indexPackage) + ".exbhv");
        tableMap.put("indexName", indexName);
        tableMap.put("esclientDiFile", esclientDiFile != null ? esclientDiFile : deriveESClientDiFile(indexName));
        tableMap.put("esfluteDiFile", esfluteDiFile != null ? esfluteDiFile : deriveESFluteDiFile(indexName));
    }

    protected Map<String, Object> createTableMap() {
        final Map<String, Object> tableMap = new LinkedHashMap<String, Object>();
        tableMap.put("isESFlute", true);
        tableMap.put("isUseLastaDi", isUseLastaDi());
        tableMap.put("esVersion", new DfElasticsearchVersion());
        return tableMap;
    }

    protected boolean isUseLastaDi() {
        return DfESFluteSupportContainer.LASTA_DI.equals(_supportContainer);
    }

    public class DfElasticsearchVersion {

        public boolean isGreaterThan(String version) {
            return comparedVersion().compareTo(version) > 0;
        }

        public boolean isGreaterEqual(String version) {
            return comparedVersion().compareTo(version) >= 0;
        }

        public boolean isLessThan(String version) {
            return comparedVersion().compareTo(version) < 0;
        }

        public boolean isLessEqual(String version) {
            return comparedVersion().compareTo(version) <= 0;
        }

        protected String comparedVersion() {
            return _elasticsearchVersion != null ? _elasticsearchVersion : "9.9.9"; // super latest if null
        }

        public String getVersionExp() {
            return _elasticsearchVersion != null ? _elasticsearchVersion : "*no specified";
        }
    }

    protected Map<String, String> prepareTypeMap() {
        final Map<String, String> typeMap = new HashMap<String, String>();
        typeMap.put("string", String.class.getSimpleName());
        typeMap.put("integer", Integer.class.getSimpleName());
        typeMap.put("long", Long.class.getSimpleName());
        typeMap.put("float", Float.class.getSimpleName());
        typeMap.put("double", Double.class.getSimpleName());
        typeMap.put("boolean", Boolean.class.getSimpleName());
        typeMap.put("date", LocalDateTime.class.getSimpleName());
        typeMap.put("date@date", LocalDate.class.getSimpleName());
        typeMap.put("date@date_time", LocalDateTime.class.getSimpleName());
        typeMap.put("date@time", LocalTime.class.getSimpleName());
        // elasticsearch may return both pattern
        typeMap.put("date@dateOptionalTime", LocalDateTime.class.getSimpleName());
        typeMap.put("date@date_optional_time", LocalDateTime.class.getSimpleName());
        return typeMap;
    }

    protected boolean isVersionGreaterEqual(String specifiedVersion) {
        String version = "1.1.1";
        return specifiedVersion.compareTo(version) >= 0;
    }

    protected String deriveESClientDiFile(String indexName) {
        final String esclientDiFile;
        if (isUseLastaDi()) {
            esclientDiFile = "esclient.xml";
        } else { // no use
            esclientDiFile = "";
        }
        return esclientDiFile;
    }

    protected String deriveESFluteDiFile(String indexName) {
        final String esfluteDiFile;
        if (isUseLastaDi()) {
            // e.g. .fess.user -> esflute_fess_user.xml
            esfluteDiFile = "esflute_" + buildIndexSmartName(indexName) + ".xml";
        } else { // no use
            esfluteDiFile = "";
        }
        return esfluteDiFile;
    }

    protected String buildIndexSmartName(String indexName) {
        return Srl.replace(Srl.ltrim(indexName, "."), ".", "_");
    }
}