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
package org.dbflute.properties.assistant.lastaflute;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.1.0-sp3 (2015/04/26 Sunday)
 */
public final class DfLastaFluteFreeGenReflector {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Map<String, Object> _freeGenMap;
    protected final String _projectName;
    protected final String _domainPackage;
    protected final String _capProjectName;
    protected final String _appPackage;
    protected final String _mylastaPackage;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfLastaFluteFreeGenReflector(Map<String, Object> freeGenMap, String projectName, String domainPackage) {
        _freeGenMap = freeGenMap;
        _projectName = projectName;
        _domainPackage = domainPackage;
        _capProjectName = Srl.initCap(projectName);
        _appPackage = domainPackage + ".app";
        _mylastaPackage = domainPackage + ".mylasta";
    }

    // ===================================================================================
    //                                                                     Prepare FreeGen
    //                                                                     ===============
    public void reflectFrom(Map<String, Object> lastafluteMap) {
        boolean hasCommonEnv = false;
        boolean hasCommonMessage = false;
        @SuppressWarnings("unchecked")
        final Map<String, Object> commonMap = (Map<String, Object>) lastafluteMap.get("commonMap");
        if (commonMap != null) {
            final String path = (String) commonMap.get("path");
            @SuppressWarnings("unchecked")
            final List<String> freeGenList = (List<String>) commonMap.get("freeGenList");
            for (String freeGen : freeGenList) {
                if ("env".equals(freeGen)) {
                    setupEnvGen(_projectName, path, true);
                    hasCommonEnv = true;
                } else if ("config".equals(freeGen)) {
                    setupConfigGen(_projectName, path);
                } else if ("label".equals(freeGen)) {
                    setupLabelGen(_projectName, path, true);
                } else if ("message".equals(freeGen)) {
                    setupMessageGen(_projectName, path);
                    hasCommonMessage = true;
                }
            }
        }
        @SuppressWarnings("unchecked")
        final Map<String, Map<String, Object>> applicationMap = (Map<String, Map<String, Object>>) lastafluteMap.get("applicationMap");
        if (applicationMap != null) {
            for (Entry<String, Map<String, Object>> entry : applicationMap.entrySet()) {
                final String appName = entry.getKey();
                final Map<String, Object> appMap = entry.getValue();
                final String path = (String) appMap.get("path");
                @SuppressWarnings("unchecked")
                final List<String> freeGenList = (List<String>) appMap.get("freeGenList");
                for (String freeGen : freeGenList) {
                    if ("env".equals(freeGen)) {
                        setupEnvGen(appName, path, !hasCommonEnv);
                    } else if ("config".equals(freeGen)) {
                        setupConfigGen(appName, path);
                    } else if ("label".equals(freeGen)) {
                        setupLabelGen(appName, path, !hasCommonMessage);
                    } else if ("message".equals(freeGen)) {
                        setupMessageGen(appName, path);
                    }
                }
            }
        }
    }

    // ===================================================================================
    //                                                                       Configuration
    //                                                                       =============
    protected void setupEnvGen(String appName, String path, boolean root) {
        final Map<String, Map<String, Object>> envMap = new LinkedHashMap<String, Map<String, Object>>();
        _freeGenMap.put(_capProjectName + "EnvGen", envMap);
        doSetupResourceMap(appName, path, envMap, "env");
        doSetupOutputConfigMap(appName, envMap, "env");
        final Map<String, Object> tableMap = new LinkedHashMap<String, Object>();
        envMap.put("tableMap", tableMap);
        if (root) {
            tableMap.put("superClassPackage", "org.dbflute.lastaflute.core.direction");
            tableMap.put("superClassSimpleName", "ObjectiveConfig");
        } else {
            tableMap.put("extendsPropRequest", _capProjectName + "ConfigGen");
            tableMap.put("isCheckImplicitOverride", true);
            tableMap.put("interfacePackage", _mylastaPackage + ".direction");
            tableMap.put("interfaceSimpleName", _capProjectName + "Config");
            tableMap.put("superClassPackage", _mylastaPackage + ".direction");
            tableMap.put("superClassSimpleName", _capProjectName + "Config.SimpleImpl");
        }
    }

    protected void setupConfigGen(String appName, String path) {
        final Map<String, Map<String, Object>> configMap = new LinkedHashMap<String, Map<String, Object>>();
        _freeGenMap.put(initCap(appName) + "ConfigGen", configMap);
        doSetupResourceMap(appName, path, configMap, "config");
        doSetupOutputConfigMap(appName, configMap, "config");
        final Map<String, Object> tableMap = new LinkedHashMap<String, Object>();
        configMap.put("tableMap", tableMap);
        tableMap.put("extendsPropRequest", _capProjectName + "EnvGen");
        tableMap.put("isCheckImplicitOverride", true);
        tableMap.put("interfacePackage", _mylastaPackage + ".direction");
        tableMap.put("interfaceSimpleName", _capProjectName + "Env");
        tableMap.put("superClassPackage", _mylastaPackage + ".direction");
        tableMap.put("superClassSimpleName", _capProjectName + "Env.SimpleImpl");
    }

    protected void doSetupOutputConfigMap(String appName, Map<String, Map<String, Object>> map, String theme) {
        final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
        map.put("outputMap", outputMap);
        outputMap.put("templateFile", "SystemConfig.vm");
        outputMap.put("outputDirectory", "$$baseDir$$/java");
        outputMap.put("package", _mylastaPackage + ".direction");
        outputMap.put("className", initCap(appName) + initCap(theme));
    }

    // ===================================================================================
    //                                                                            Messages
    //                                                                            ========
    protected void setupLabelGen(String appName, String path, boolean root) {
        final Map<String, Map<String, Object>> labelMap = new LinkedHashMap<String, Map<String, Object>>();
        _freeGenMap.put(initCap(appName) + "LabelGen", labelMap);
        doSetupResourceMap(appName, path, labelMap, "label");
        doSetupMessageOutputMap(appName, labelMap, "labels");
        final Map<String, Object> tableMap = new LinkedHashMap<String, Object>();
        labelMap.put("tableMap", tableMap);
        tableMap.put("groupingKeyMap", DfCollectionUtil.newLinkedHashMap("label", "prefix:labels."));
        if (!root) {
            doSetupMessageTableMapInheritance(tableMap, "message");
        }
    }

    protected void setupMessageGen(String appName, String path) {
        final Map<String, Map<String, Object>> labelMap = new LinkedHashMap<String, Map<String, Object>>();
        _freeGenMap.put(initCap(appName) + "MessageGen", labelMap);
        doSetupResourceMap(appName, path, labelMap, "message");
        doSetupMessageOutputMap(appName, labelMap, "messages");
        final Map<String, Object> tableMap = new LinkedHashMap<String, Object>();
        labelMap.put("tableMap", tableMap);
        tableMap.put("groupingKeyMap", DfCollectionUtil.newLinkedHashMap("label", "prefix:labels."));
        doSetupMessageTableMapInheritance(tableMap, "label");
    }

    protected void doSetupMessageOutputMap(String appName, Map<String, Map<String, Object>> map, String theme) {
        final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
        map.put("outputMap", outputMap);
        outputMap.put("templateFile", "UserMessages.vm");
        outputMap.put("outputDirectory", "$$baseDir$$/java");
        outputMap.put("package", buildMessagesPackage());
        outputMap.put("className", initCap(appName) + initCap(theme));
    }

    protected void doSetupMessageTableMapInheritance(Map<String, Object> tableMap, String theme) {
        tableMap.put("extendsPropRequest", _capProjectName + initCap(theme) + "Gen");
        tableMap.put("isCheckImplicitOverride", "true");
        tableMap.put("isUseNonNumberVariable", "true");
        tableMap.put("superClassPackage", buildMessagesPackage());
        tableMap.put("superClassSimpleName", _capProjectName + initCap(theme) + "s");
    }

    protected String buildMessagesPackage() {
        return _appPackage + ".web.base.messages";
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected void doSetupResourceMap(String appName, String path, Map<String, Map<String, Object>> map, String theme) {
        final Map<String, Object> resourceMap = new LinkedHashMap<String, Object>();
        map.put("resourceMap", resourceMap);
        resourceMap.put("baseDir", path + "/src/main");
        resourceMap.put("resourceType", "PROP");
        resourceMap.put("resourceFile", "$$baseDir$$/resources/" + appName + "_" + theme + ".properties");
    }

    protected String initCap(String project) {
        return Srl.initCap(project);
    }
}