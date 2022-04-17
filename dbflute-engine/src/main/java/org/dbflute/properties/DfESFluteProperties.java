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
package org.dbflute.properties;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.dbflute.properties.assistant.esflute.DfESFluteFreeGenReflector;
import org.dbflute.properties.assistant.esflute.DfESFluteFreeGenReflector.DfESFluteSupportContainer;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 1.1.0-sp9 (2015/10/29 Thursday)
 */
public final class DfESFluteProperties extends DfAbstractDBFluteProperties {

    private static final Logger logger = LoggerFactory.getLogger(DfESFluteProperties.class);

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfESFluteProperties(Properties prop) {
        super(prop);
    }

    // ===================================================================================
    //                                                                      Definition Map
    //                                                                      ==============
    // map:{
    //     ; outputDirectory = ../src/main/java
    //     ; basePackage = org.docksidestage.es
    //     ; basePath = http://localhost:9200
    //     ; indexMap = map:{
    //         ; fess_user = map:{
    //             ; package = user
    //         }
    //         ; fess_config = map:{
    //             ; package = config
    //         }
    //     }
    //     # not required
    //     ; elasticsearchVersion = 1.0
    //     ; isContinueIfUrlFailure = false
    // }
    protected Map<String, Object> _esfluteMap;

    protected Map<String, Object> getEsfluteMap() { // closet
        if (_esfluteMap == null) {
            final Map<String, Object> map = mapProp("torque.esfluteMap", DEFAULT_EMPTY_MAP);
            _esfluteMap = newLinkedHashMap();
            _esfluteMap.putAll(map);
        }
        return _esfluteMap;
    }

    // ===================================================================================
    //                                                                       Determination
    //                                                                       =============
    public boolean hasEsfluteDefinition() {
        return !getEsfluteMap().isEmpty();
    }

    // ===================================================================================
    //                                                                         Core Option
    //                                                                         ===========
    protected String getOutputDirectory(Map<String, Object> esfluteMap) {
        return (String) esfluteMap.getOrDefault("outputDirectory", "../src/main/java");
    }

    protected String getBasePackage(Map<String, Object> esfluteMap) {
        return (String) esfluteMap.get("basePackage"); // no use if not null
    }

    protected String getBasePath(Map<String, Object> esfluteMap) {
        final String basePath = (String) esfluteMap.get("basePath");
        if (basePath == null) {
            throw new IllegalStateException("Not found the required property 'basePath': " + esfluteMap);
        }
        return basePath;
    }

    // ===================================================================================
    //                                                                             FreeGen
    //                                                                             =======
    public void reflectFreeGenMap(Map<String, Object> freeGenMap) {
        final Map<String, Object> esfluteMap = getEsfluteMap();
        final String basePackage = getBasePackage(esfluteMap);
        if (Srl.is_Null_or_TrimmedEmpty(basePackage)) { // no use
            return;
        }
        logger.info("...Loading freeGen settings from lastafluteMap: " + basePackage);
        final String outputDirectory = getOutputDirectory(esfluteMap);
        final String basePath = getBasePath(esfluteMap);
        createFreeGenReflector(freeGenMap, outputDirectory, basePackage, basePath).reflectFrom(esfluteMap);
    }

    protected DfESFluteFreeGenReflector createFreeGenReflector(Map<String, Object> freeGenMap, String outputDirectory, String basePackage,
            String basePath) {
        final DfESFluteSupportContainer supportContainer = deriveSupportContainer();
        final String elasticsearchVersion = getElasticsearchVersion();
        return new DfESFluteFreeGenReflector(freeGenMap, outputDirectory, basePackage, basePath, supportContainer, elasticsearchVersion);
    }

    protected DfESFluteSupportContainer deriveSupportContainer() {
        final DfESFluteSupportContainer supportContainer;
        if (getBasicProperties().isTargetContainerLastaDi()) {
            supportContainer = DfESFluteSupportContainer.LASTA_DI;
        } else {
            supportContainer = DfESFluteSupportContainer.NONE;
        }
        return supportContainer;
    }

    // ===================================================================================
    //                                                                              Option
    //                                                                              ======
    public String getElasticsearchVersion() {
        return getProperty("elasticsearchVersion", null, getEsfluteMap());
    }

    public boolean isContinueIfUrlFailure() {
        return isProperty("isContinueIfUrlFailure", false, getEsfluteMap());
    }

    // ===================================================================================
    //                                                                 Refresh ProjectName
    //                                                                 ===================
    public void reflectRefreshProjectList(List<String> projectNameList) {
        final Map<String, Object> esfluteMap = getEsfluteMap();
        @SuppressWarnings("unchecked")
        final Map<String, Map<String, Object>> appMap = (Map<String, Map<String, Object>>) esfluteMap.get("appMap");
        if (appMap == null) {
            return;
        }
        for (Entry<String, Map<String, Object>> entry : appMap.entrySet()) {
            final Map<String, Object> map = entry.getValue();
            if (map != null) {
                final String path = (String) map.get("path"); // e.g. ../../maihama-dockside
                if (path != null) {
                    final String projectName = Srl.substringLastRear(path, "/"); // e.g. maihama-dockside
                    projectNameList.add(projectName);
                }
            }
        }
    }
}