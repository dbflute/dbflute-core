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

import org.dbflute.exception.DfIllegalPropertySettingException;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 1.1.0-sp3 (2015/04/26 Sunday)
 */
public final class DfLastaFluteFreeGenReflector {

    private static final Logger logger = LoggerFactory.getLogger(DfLastaFluteFreeGenReflector.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Map<String, Object> _freeGenMap;
    protected final String _capServiceName;
    protected final String _uncapServiceName;
    protected final String _domainPackage;
    protected final String _appPackage;
    protected final String _mylastaPackage;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfLastaFluteFreeGenReflector(Map<String, Object> freeGenMap, String serviceName, String domainPackage) {
        _freeGenMap = freeGenMap;
        _capServiceName = Srl.initCap(serviceName);
        _uncapServiceName = Srl.initUncap(serviceName);
        _domainPackage = domainPackage;
        _appPackage = domainPackage + ".app";
        _mylastaPackage = domainPackage + ".mylasta";
    }

    // ===================================================================================
    //                                                                     Prepare FreeGen
    //                                                                     ===============
    public void reflectFrom(Map<String, Object> lastafluteMap) {
        logger.info("Before existing freeGen settigs: " + _freeGenMap.keySet());
        boolean hasCommonEnv = false;
        boolean hasCommonMessage = false;
        @SuppressWarnings("unchecked")
        final Map<String, Object> commonMap = (Map<String, Object>) lastafluteMap.get("commonMap");
        if (commonMap != null) {
            final String path = (String) commonMap.get("path");
            @SuppressWarnings("unchecked")
            final List<String> freeGenList = (List<String>) commonMap.get("freeGenList");
            for (String freeGen : freeGenList) {
                logger.info("...Reflecting common freeGen settigs: " + freeGen + ", " + path);
                if ("env".equals(freeGen)) {
                    setupEnvGen(_uncapServiceName, path, true);
                    hasCommonEnv = true;
                } else if ("config".equals(freeGen)) {
                    setupConfigGen(_uncapServiceName, path);
                } else if ("label".equals(freeGen)) {
                    setupLabelGen(_uncapServiceName, path, true);
                } else if ("message".equals(freeGen)) {
                    setupMessageGen(_uncapServiceName, path);
                    hasCommonMessage = true;
                }
            }
        }
        @SuppressWarnings("unchecked")
        final Map<String, Map<String, Object>> appMap = (Map<String, Map<String, Object>>) lastafluteMap.get("appMap");
        if (appMap != null) {
            for (Entry<String, Map<String, Object>> entry : appMap.entrySet()) {
                final String appName = entry.getKey();
                final Map<String, Object> defMap = entry.getValue();
                final String path = (String) defMap.get("path");
                @SuppressWarnings("unchecked")
                final List<String> freeGenList = (List<String>) defMap.get("freeGenList");
                for (String freeGen : freeGenList) {
                    logger.info("...Reflecting application freeGen settigs: " + appName + "." + freeGen);
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
        logger.info("After existing freeGen settigs: " + _freeGenMap.keySet());
    }

    // ===================================================================================
    //                                                                       Configuration
    //                                                                       =============
    protected void setupEnvGen(String appName, String path, boolean root) {
        final Map<String, Map<String, Object>> envMap = new LinkedHashMap<String, Map<String, Object>>();
        registerFreeGen(initCap(appName) + "EnvGen", envMap);
        doSetupResourceMap(appName, path, envMap, "env");
        doSetupOutputConfigMap(appName, envMap, "env");
        final Map<String, Object> tableMap = createTableMap();
        envMap.put("tableMap", tableMap);
        if (root) {
            tableMap.put("superClassPackage", "org.dbflute.lastaflute.core.direction");
            tableMap.put("superClassSimpleName", "ObjectiveConfig");
        } else {
            tableMap.put("extendsPropRequest", _capServiceName + "ConfigGen");
            tableMap.put("isCheckImplicitOverride", getTrueLiteral());
            tableMap.put("interfacePackage", _mylastaPackage + ".direction");
            tableMap.put("interfaceSimpleName", _capServiceName + "Config");
            tableMap.put("superClassPackage", _mylastaPackage + ".direction");
            tableMap.put("superClassSimpleName", _capServiceName + "Config.SimpleImpl");
        }
    }

    protected void setupConfigGen(String appName, String path) {
        final Map<String, Map<String, Object>> configMap = new LinkedHashMap<String, Map<String, Object>>();
        final String capAppName = initCap(appName);
        registerFreeGen(capAppName + "ConfigGen", configMap);
        doSetupResourceMap(appName, path, configMap, "config");
        doSetupOutputConfigMap(appName, configMap, "config");
        final Map<String, Object> tableMap = createTableMap();
        configMap.put("tableMap", tableMap);
        tableMap.put("extendsPropRequest", capAppName + "EnvGen");
        tableMap.put("isCheckImplicitOverride", getTrueLiteral());
        tableMap.put("interfacePackage", _mylastaPackage + ".direction");
        tableMap.put("interfaceSimpleName", capAppName + "Env");
        tableMap.put("superClassPackage", _mylastaPackage + ".direction");
        tableMap.put("superClassSimpleName", capAppName + "Env.SimpleImpl");
    }

    protected void doSetupOutputConfigMap(String appName, Map<String, Map<String, Object>> map, String theme) {
        final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
        map.put("outputMap", outputMap);
        outputMap.put("templateFile", "LaSystemConfig.vm");
        outputMap.put("outputDirectory", "$$baseDir$$/java");
        outputMap.put("package", _mylastaPackage + ".direction");
        outputMap.put("className", initCap(appName) + initCap(theme));
    }

    // ===================================================================================
    //                                                                            Messages
    //                                                                            ========
    protected void setupLabelGen(String appName, String path, boolean root) {
        final Map<String, Map<String, Object>> labelMap = new LinkedHashMap<String, Map<String, Object>>();
        registerFreeGen(initCap(appName) + "LabelGen", labelMap);
        doSetupResourceMap(appName, path, labelMap, "label");
        doSetupMessageOutputMap(appName, labelMap, "labels");
        final Map<String, Object> tableMap = createTableMap();
        labelMap.put("tableMap", tableMap);
        tableMap.put("groupingKeyMap", DfCollectionUtil.newLinkedHashMap("label", "prefix:labels."));
        if (!root) {
            doSetupMessageTableMapInheritance(_uncapServiceName, tableMap, "message");
        }
    }

    protected void setupMessageGen(String appName, String path) {
        final Map<String, Map<String, Object>> labelMap = new LinkedHashMap<String, Map<String, Object>>();
        registerFreeGen(initCap(appName) + "MessageGen", labelMap);
        doSetupResourceMap(appName, path, labelMap, "message");
        doSetupMessageOutputMap(appName, labelMap, "messages");
        final Map<String, Object> tableMap = createTableMap();
        labelMap.put("tableMap", tableMap);
        tableMap.put("groupingKeyMap", DfCollectionUtil.newLinkedHashMap("label", "prefix:labels."));
        doSetupMessageTableMapInheritance(appName, tableMap, "label");
    }

    protected void doSetupMessageOutputMap(String appName, Map<String, Map<String, Object>> map, String theme) {
        final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
        map.put("outputMap", outputMap);
        outputMap.put("templateFile", "LaUserMessages.vm");
        outputMap.put("outputDirectory", "$$baseDir$$/java");
        outputMap.put("package", buildMessagesPackage());
        outputMap.put("className", initCap(appName) + initCap(theme));
    }

    protected void doSetupMessageTableMapInheritance(String appName, Map<String, Object> tableMap, String theme) {
        tableMap.put("extendsPropRequest", initCap(appName) + initCap(theme) + "Gen");
        tableMap.put("isCheckImplicitOverride", getTrueLiteral());
        tableMap.put("isUseNonNumberVariable", getTrueLiteral());
        tableMap.put("superClassPackage", buildMessagesPackage());
        tableMap.put("superClassSimpleName", initCap(appName) + initCap(theme) + "s");
    }

    protected String buildMessagesPackage() {
        return _appPackage + ".web.base.messages";
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected void registerFreeGen(String key, Map<String, Map<String, Object>> map) {
        final Object existing = _freeGenMap.get(key);
        if (existing != null) {
            String msg = "Found the existing same-name setting: key=" + key + " existing=" + existing;
            throw new DfIllegalPropertySettingException(msg);
        }
        _freeGenMap.put(key, map);
    }

    protected void doSetupResourceMap(String appName, String path, Map<String, Map<String, Object>> map, String theme) {
        final Map<String, Object> resourceMap = new LinkedHashMap<String, Object>();
        map.put("resourceMap", resourceMap);
        resourceMap.put("baseDir", path + "/src/main");
        resourceMap.put("resourceType", "PROP");
        resourceMap.put("resourceFile", "$$baseDir$$/resources/" + appName + "_" + theme + ".properties");
    }

    protected Map<String, Object> createTableMap() {
        final Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("isLastaFlute", true);
        return map;
    }

    protected String getTrueLiteral() {
        return "true";
    }

    protected String initCap(String project) {
        return Srl.initCap(project);
    }
}