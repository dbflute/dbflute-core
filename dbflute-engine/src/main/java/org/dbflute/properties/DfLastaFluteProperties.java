/*
 * Copyright 2014-2018 the original author or authors.
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

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

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

    private static final String LASTADOC_HTML_PREFIX = "lastadoc-";
    private static final String LASTADOC_HTML_SUFFIX = ".html";
    private static final Logger _log = LoggerFactory.getLogger(DfLastaFluteProperties.class);

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
    //             ; configPluginInterface = org.docksidestage.mylasta.direction.MyProp
    //             ; mailPluginInterface = org.docksidestage.mylasta.direction.MyMail
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
    protected String findServiceName(Map<String, Object> lastafluteMap) {
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
        show("...Loading freeGen settings from lastafluteMap: " + serviceName);
        final String domainPackage = (String) lastafluteMap.get("domainPackage");
        if (domainPackage == null) {
            throw new DfIllegalPropertySettingException("The property 'domainPackage' is required: " + lastafluteMap.keySet());
        }
        final String lastaDocDir = getLastaDocOutputDirectory();
        final DfLastaFluteFreeGenReflector reflector = createFreeGenReflector(lastafluteMap, freeGenMap, serviceName, domainPackage);
        reflector.reflectFrom(lastafluteMap, lastaDocDir);
    }

    protected DfLastaFluteFreeGenReflector createFreeGenReflector(Map<String, Object> lastafluteMap, Map<String, Object> freeGenMap,
            String serviceName, String domainPackage) {
        final DfLastaFluteFreeGenReflector reflector = newFreeGenReflector(freeGenMap, serviceName, domainPackage);
        if (isProperty("isUseDefaultConfigAtGeneration", false, lastafluteMap)) {
            reflector.useDefaultConfigAtGeneration();
        }
        return reflector;
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
        show("...Loading propertiesHtml settings from lastafluteMap: " + serviceName);
        final List<String> environmentList = getEnvironmentList(lastafluteMap);
        newPropertiesHtmlReflector(propHtmlMap, serviceName, environmentList).reflectFrom(lastafluteMap);
    }

    @SuppressWarnings("unchecked")
    protected List<String> getEnvironmentList(Map<String, Object> lastafluteMap) {
        return (List<String>) lastafluteMap.getOrDefault("environmentList", DfCollectionUtil.emptyList());
    }

    protected DfLastaFlutePropertiesHtmlReflector newPropertiesHtmlReflector(Map<String, Map<String, Object>> propHtmlMap,
            String serviceName, List<String> environmentList) {
        return new DfLastaFlutePropertiesHtmlReflector(propHtmlMap, serviceName, environmentList);
    }

    // ===================================================================================
    //                                                              Application OutsideSql
    //                                                              ======================
    public void reflectApplicationOutsideSqlMap(Map<String, Map<String, String>> appOutqlMap) {
        final Map<String, Object> lastafluteMap = getLastafluteMap();
        @SuppressWarnings("unchecked")
        final Map<String, Map<String, Object>> appMap = (Map<String, Map<String, Object>>) lastafluteMap.get("appMap");
        if (appMap == null) { // just in case
            return;
        }
        show("/===========================================================================");
        show("...Loading application outsideSql settings from lastafluteMap.");
        final DfLanguageDependency lang = getBasicProperties().getLanguageDependency();
        final String resourceDirectory = lang.getMainResourceDirectory();
        final String programDirectory = lang.getMainProgramDirectory();
        for (Entry<String, Map<String, Object>> entry : appMap.entrySet()) {
            final Map<String, Object> map = entry.getValue();
            if (map != null) {
                final String path = (String) map.get("path");
                if (path != null && !path.equals("..")) { // e.g. except DBFlute central project e.g. harbor
                    final Map<String, String> elementMap = newLinkedHashMap();
                    elementMap.put("sqlDirectory", resourceDirectory);
                    elementMap.put("sql2EntityOutputDirectory", programDirectory);
                    elementMap.put("isSuppressDirectoryCheck", "true"); // because of automatic settings
                    show("...Reflecting application outsideSql: " + path + ", " + elementMap);
                    appOutqlMap.put(path, elementMap);
                }
            }
        }
        show("==========/");
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

    public List<String> getLastaDocHtmlNameList() {
        final String outputDir = getLastaDocOutputDirectory();
        return findExistingLastaDocHtmlNameList(outputDir);
    }

    public List<String> getLastaDocHtmlPathList() {
        final String outputDir = getLastaDocOutputDirectory();
        final List<String> htmlNameList = findExistingLastaDocHtmlNameList(outputDir);
        return htmlNameList.stream().map(name -> outputDir + "/" + name).collect(Collectors.toList());
    }

    public String buildLastaDocHtmlPath(String appName) {
        return getLastaDocOutputDirectory() + "/" + buildLastaDocHtmlName(appName);
    }

    public String buildLastaDocHtmlName(String appName) {
        return LASTADOC_HTML_PREFIX + appName + LASTADOC_HTML_SUFFIX;
    }

    protected List<String> findExistingLastaDocHtmlNameList(String outputDir) {
        final String[] docList = new File(outputDir).list((dir, name) -> {
            return name.startsWith(LASTADOC_HTML_PREFIX) && name.endsWith(LASTADOC_HTML_SUFFIX);
        });
        return docList != null ? Arrays.asList(docList) : Collections.emptyList();
    }

    public boolean isSuppressLastaDocSchemaHtmlLink() {
        return isProperty("isSuppressLastaDocSchemaHtmlLink", false, getLastafluteMap());
    }

    public boolean isLastaDocMavenGeared() {
        return isProperty("isLastaDocMavenGeared", false, getLastafluteMap());
    }

    public boolean isLastaDocGradleGeared() {
        return isProperty("isLastaDocGradleGeared", false, getLastafluteMap());
    }

    public String getLastaDocHtmlMarkFreeGenDocNaviLink() {
        return "<!-- df:markFreeGenDocNaviLink -->";
    }

    public String getLastaDocHtmlMarkFreeGenDocBody() {
        return "<!-- df:markFreeGenDocBody -->";
    }

    // ===================================================================================
    //                                                                        Small Helper
    //                                                                        ============
    protected void show(String msg) {
        _log.info(msg);
    }
}