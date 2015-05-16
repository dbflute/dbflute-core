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
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 1.1.0-sp3 (2015/04/26 Sunday)
 */
public final class DfLastaFlutePropertiesHtmlReflector {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger logger = LoggerFactory.getLogger(DfLastaFlutePropertiesHtmlReflector.class);
    protected static final String PROPERTIES_HTML_KEY = "propertiesHtmlList";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Map<String, Object> _propHtmlMap;
    protected final String _capServiceName;
    protected final String _uncapServiceName;
    protected final List<String> _environmentList;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfLastaFlutePropertiesHtmlReflector(Map<String, Object> propHtmlMap, String serviceName, List<String> environmentList) {
        _propHtmlMap = propHtmlMap;
        _capServiceName = Srl.initCap(serviceName);
        _uncapServiceName = Srl.initUncap(serviceName);
        _environmentList = environmentList;
    }

    // ===================================================================================
    //                                                                     Prepare FreeGen
    //                                                                     ===============
    public void reflectFrom(Map<String, Object> lastafluteMap) {
        logger.info("Before refecting, existing propertiesHtml settigs: " + _propHtmlMap.keySet());
        final boolean envSuffixOnFile = isUseEnvSuffixOnFile(lastafluteMap);
        boolean hasCommonEnv = false;
        boolean hasCommonMessage = false;
        @SuppressWarnings("unchecked")
        final Map<String, Object> commonMap = (Map<String, Object>) lastafluteMap.get("commonMap");
        if (commonMap != null) {
            final String path = (String) commonMap.get("path");
            @SuppressWarnings("unchecked")
            final List<String> propertiesHtmlList = (List<String>) commonMap.get(PROPERTIES_HTML_KEY);
            for (String propertiesHtml : propertiesHtmlList) {
                logger.info("...Reflecting common propertiesHtml settigs: " + propertiesHtml + ", " + path);
                if ("env".equals(propertiesHtml)) {
                    setupEnv(_uncapServiceName, path, true, envSuffixOnFile);
                    hasCommonEnv = true;
                } else if ("config".equals(propertiesHtml)) {
                    setupConfig(_uncapServiceName, path);
                } else if ("label".equals(propertiesHtml)) {
                    setupLabel(_uncapServiceName, path, true);
                } else if ("message".equals(propertiesHtml)) {
                    setupMessage(_uncapServiceName, path);
                    hasCommonMessage = true;
                } else {
                    String msg = "Unkonwn type for commonMap's propertiesHtml: " + propertiesHtml;
                    throw new DfIllegalPropertySettingException(msg);
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
                final List<String> propertiesHtmlList = (List<String>) defMap.get(PROPERTIES_HTML_KEY);
                for (String propertiesHtml : propertiesHtmlList) {
                    logger.info("...Reflecting application propertiesHtml settigs: " + appName + "." + propertiesHtml);
                    if ("env".equals(propertiesHtml)) {
                        setupEnv(appName, path, !hasCommonEnv, envSuffixOnFile);
                    } else if ("config".equals(propertiesHtml)) {
                        setupConfig(appName, path);
                    } else if ("label".equals(propertiesHtml)) {
                        setupLabel(appName, path, !hasCommonMessage);
                    } else if ("message".equals(propertiesHtml)) {
                        setupMessage(appName, path);
                    } else {
                        String msg = "Unkonwn type for appMap's propertiesHtml: " + propertiesHtml;
                        throw new DfIllegalPropertySettingException(msg);
                    }
                }
            }
        }
        logger.info("After refecting, existing propertiesHtml settigs: " + _propHtmlMap.keySet());
    }

    protected boolean isUseEnvSuffixOnFile(Map<String, Object> lastafluteMap) {
        final Object obj = lastafluteMap.get("isUseLastaEnv");
        return obj != null && obj.toString().equalsIgnoreCase("true");
    }

    // ===================================================================================
    //                                                                       Configuration
    //                                                                       =============
    protected void setupEnv(String appName, String path, boolean root, boolean envSuffixOnFile) {
        final Map<String, Object> map = new LinkedHashMap<String, Object>();
        final String theme = "env";
        registerBasicItem(appName, path, map, theme);
        registerEnvironmentMap(appName, map, theme, envSuffixOnFile);
        if (!root) {
            registerExtendsProp(map, _uncapServiceName, "config");
        }
    }

    protected void setupConfig(String appName, String path) {
        final Map<String, Object> map = new LinkedHashMap<String, Object>();
        registerBasicItem(appName, path, map, "config");
        registerExtendsProp(map, appName, "env");
    }

    // ===================================================================================
    //                                                                            Messages
    //                                                                            ========
    protected void setupLabel(String appName, String path, boolean root) {
        final Map<String, Object> map = new LinkedHashMap<String, Object>();
        registerBasicItem(appName, path, map, "label");
        if (!root) {
            registerExtendsProp(map, _uncapServiceName, "message");
        }
    }

    protected void setupMessage(String appName, String path) {
        final Map<String, Object> map = new LinkedHashMap<String, Object>();
        registerBasicItem(appName, path, map, "message");
        registerExtendsProp(map, appName, "label");
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected void registerPropertiesHtml(String key, Map<String, Object> map) {
        final Object existing = _propHtmlMap.get(key);
        if (existing != null) {
            String msg = "Found the existing same-name setting: key=" + key + " existing=" + existing;
            throw new DfIllegalPropertySettingException(msg);
        }
        _propHtmlMap.put(key, map);
    }

    protected String buildTitleSuffix(String theme) {
        return initCap(theme);
    }

    protected void registerBasicItem(String appName, String path, Map<String, Object> map, String theme) {
        final String capAppName = initCap(appName);
        registerPropertiesHtml(capAppName + buildTitleSuffix(theme), map);
        registerBaseDir(path, map);
        registerRootFile(appName, map, theme);
    }

    protected void registerBaseDir(String path, final Map<String, Object> map) {
        map.put("baseDir", path + "/src");
    }

    protected void registerRootFile(String appName, Map<String, Object> map, String theme) {
        map.put("rootFile", getMainResourcesDir() + buildPropertiesFileName(appName, theme));
    }

    protected String getMainResourcesDir() {
        return "$$baseDir$$/main/resources/";
    }

    protected String buildPropertiesFileName(String appName, String theme) {
        return appName + "_" + theme + ".properties";
    }

    protected void registerEnvironmentMap(String appName, Map<String, Object> map, String theme, boolean envSuffixOnFile) {
        final Map<String, Object> environmentMap = new LinkedHashMap<String, Object>();
        for (String envKey : _environmentList) {
            final String path;
            if (envSuffixOnFile) { // e.g. maihama_env_production.properties
                path = getMainResourcesDir() + buildPropertiesFileName(appName, theme + "_" + envKey);
            } else { // maven profile way
                path = "$$baseDir$$/" + envKey + "/resources";
            }
            environmentMap.put(envKey, path);
        }
        map.put("environmentMap", environmentMap);
    }

    protected void registerExtendsProp(Map<String, Object> map, String parentName, String parentTheme) {
        map.put("extendsPropRequest", initCap(parentName) + buildTitleSuffix(parentTheme));
        map.put("isCheckImplicitOverride", getTrueLiteral());
    }

    protected String getTrueLiteral() {
        return "true";
    }

    protected String initCap(String project) {
        return Srl.initCap(project);
    }
}