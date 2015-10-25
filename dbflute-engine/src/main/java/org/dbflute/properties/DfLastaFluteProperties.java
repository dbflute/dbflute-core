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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.dbflute.exception.DfIllegalPropertySettingException;
import org.dbflute.logic.generate.language.DfLanguageDependency;
import org.dbflute.properties.assistant.lastaflute.DfLastaFluteFreeGenReflector;
import org.dbflute.properties.assistant.lastaflute.DfLastaFlutePropertiesHtmlReflector;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 1.1.0-sp3 (2015/04/26 Sunday)
 */
public final class DfLastaFluteProperties extends DfAbstractHelperProperties {

    private static final Logger logger = LoggerFactory.getLogger(DfLastaFluteProperties.class);

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfLastaFluteProperties(Properties prop) {
        super(prop);
    }

    // ===================================================================================
    //                                                                      Definition Map
    //                                                                      ==============
    // map:{
    //     ; serviceName = maihama
    //     ; domainPackage = org.docksidestage
    //     ; environmentList = list:{ integration ; production }
    //     ; isUseLastaEnv = true
    //     ; commonMap = map:{
    //         ; path = ../
    //         ; freeGenList = list:{ env ; config ; label ; message ; jsp }
    //         ; propertiesHtmlList = list:{ env ; config ; label ; message }
    //     }
    //     ; appMap = map:{
    //         ; dockside = map:{
    //             ; path = ../../maihama-dockside
    //             ; freeGenList = list:{ env ; config ; label ; message ; jsp }
    //             ; propertiesHtmlList = list:{ env ; config ; label ; message }
    //         }
    //         ; hanger = map:{
    //             ; path = ../../maihama-hanger
    //             ; freeGenList = list:{ env ; config ; label ; message ; jsp }
    //             ; propertiesHtmlList = list:{ env ; config ; label ; message }
    //         }
    //     }
    // }
    protected Map<String, Object> _lastafluteMap;

    protected Map<String, Object> getLastafluteMap() { // closet
        if (_lastafluteMap == null) {
            final Map<String, Object> map = mapProp("torque.lastafluteMap", DEFAULT_EMPTY_MAP);
            _lastafluteMap = newLinkedHashMap();
            _lastafluteMap.putAll(map);
        }
        return _lastafluteMap;
    }

    // ===================================================================================
    //                                                                       Determination
    //                                                                       =============
    public boolean hasLastafluteDefinition() {
        return !getLastafluteMap().isEmpty();
    }

    // ===================================================================================
    //                                                                        Service Name
    //                                                                        ============
    protected String findServiceName(final Map<String, Object> lastafluteMap) {
        return (String) lastafluteMap.get("serviceName");
    }

    // ===================================================================================
    //                                                                             FreeGen
    //                                                                             =======
    public void reflectFreeGenMap(Map<String, Object> freeGenMap) {
        final Map<String, Object> lastafluteMap = getLastafluteMap();
        final String serviceName = findServiceName(lastafluteMap);
        if (Srl.is_Null_or_TrimmedEmpty(serviceName)) { // no use
            return;
        }
        logger.info("...Loading freeGen settings from lastafluteMap: " + serviceName);
        final String domainPackage = (String) lastafluteMap.get("domainPackage");
        if (domainPackage == null) {
            throw new DfIllegalPropertySettingException("The property 'domainPackage' is required: " + lastafluteMap.keySet());
        }
        final String lastaDocDir = getLastaDocOutputDirectory();
        newFreeGenReflector(freeGenMap, serviceName, domainPackage).reflectFrom(lastafluteMap, lastaDocDir);
    }

    protected DfLastaFluteFreeGenReflector newFreeGenReflector(Map<String, Object> freeGenMap, String serviceName, String domainPackage) {
        return new DfLastaFluteFreeGenReflector(freeGenMap, serviceName, domainPackage);
    }

    // ===================================================================================
    //                                                                      PropertiesHtml
    //                                                                      ==============
    public void reflectPropertiesHtmlMap(Map<String, Map<String, Object>> propHtmlMap) {
        final Map<String, Object> lastafluteMap = getLastafluteMap();
        final String serviceName = findServiceName(lastafluteMap);
        if (Srl.is_Null_or_TrimmedEmpty(serviceName)) { // no use
            return;
        }
        logger.info("...Loading propertiesHtml settings from lastafluteMap: " + serviceName);
        final List<String> environmentList = getEnvironmentList(lastafluteMap);
        newPropertiesHtmlReflector(propHtmlMap, serviceName, environmentList).reflectFrom(lastafluteMap);
    }

    @SuppressWarnings("unchecked")
    protected List<String> getEnvironmentList(Map<String, Object> lastafluteMap) {
        return (List<String>) lastafluteMap.getOrDefault("environmentList", DfCollectionUtil.emptyList());
    }

    protected DfLastaFlutePropertiesHtmlReflector newPropertiesHtmlReflector(Map<String, Map<String, Object>> propHtmlMap, String serviceName,
            List<String> environmentList) {
        return new DfLastaFlutePropertiesHtmlReflector(propHtmlMap, serviceName, environmentList);
    }

    // ===================================================================================
    //                                                              Application OutsideSql
    //                                                              ======================
    public void reflectApplicationOutsideSqlMap(Map<String, Map<String, String>> appOutqlMap) {
        final Map<String, Object> lastafluteMap = getLastafluteMap();
        @SuppressWarnings("unchecked")
        final Map<String, Map<String, Object>> appMap = (Map<String, Map<String, Object>>) lastafluteMap.get("appMap");
        if (appMap == null) {
            return;
        }
        logger.info("...Loading application outsideSql settings from lastafluteMap.");
        final DfLanguageDependency lang = getBasicProperties().getLanguageDependency();
        final String resourceDirectory = lang.getMainResourceDirectory();
        final String programDirectory = lang.getMainProgramDirectory();
        for (Entry<String, Map<String, Object>> entry : appMap.entrySet()) {
            final Map<String, Object> map = entry.getValue();
            if (map != null) {
                final String path = (String) map.get("path");
                if (path != null) {
                    final Map<String, String> elementMap = newLinkedHashMap();
                    elementMap.put("sqlDirectory", resourceDirectory);
                    elementMap.put("sql2EntityOutputDirectory", programDirectory);
                    logger.info("...Reflecting application outsideSql: " + path + ", " + elementMap);
                    appOutqlMap.put(path, elementMap);
                }
            }
        }
    }

    // ===================================================================================
    //                                                                 Refresh ProjectName
    //                                                                 ===================
    public void reflectRefreshProjectList(List<String> projectNameList) {
        final Map<String, Object> lastafluteMap = getLastafluteMap();
        @SuppressWarnings("unchecked")
        final Map<String, Map<String, Object>> appMap = (Map<String, Map<String, Object>>) lastafluteMap.get("appMap");
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

    // ===================================================================================
    //                                                                            LastaDoc
    //                                                                            ========
    public String getLastaDocOutputDirectory() {
        return getDocumentProperties().getDocumentOutputDirectory();
    }

    public boolean isLastaDocMavenGeared() {
        return isProperty("isLastaDocMavenGeared", false, getLastafluteMap());
    }

    public boolean isLastaDocGradleGeared() {
        return isProperty("isLastaDocGradleGeared", false, getLastafluteMap());
    }
}