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
package org.dbflute.properties.assistant.lastaflute;

import java.io.File;
import java.io.FilenameFilter;
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
    protected final Map<String, Map<String, Object>> _propHtmlMap; // not null
    protected final String _capServiceName; // not null
    protected final String _uncapServiceName; // not null
    protected final List<String> _environmentList; // not null, empty allowed

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfLastaFlutePropertiesHtmlReflector(Map<String, Map<String, Object>> propHtmlMap, String serviceName,
            List<String> environmentList) {
        _propHtmlMap = propHtmlMap;
        _capServiceName = Srl.initCap(serviceName);
        _uncapServiceName = Srl.initUncap(serviceName);
        _environmentList = environmentList;
    }

    // ===================================================================================
    //                                                                     Prepare FreeGen
    //                                                                     ===============
    public void reflectFrom(Map<String, Object> lastafluteMap) {
        logger.info("Before LastaFlute refecting, existing propertiesHtml settigs: " + _propHtmlMap.keySet());
        final boolean lastaEnv = isUseLastaEnv(lastafluteMap);
        boolean hasCommonEnv = false;
        boolean hasCommonConfig = false;
        boolean hasCommonLabel = false;
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
                    setupEnv(_uncapServiceName, path, false, false, lastaEnv);
                    hasCommonEnv = true;
                } else if ("config".equals(propertiesHtml)) {
                    setupConfig(_uncapServiceName, path, hasCommonEnv, false, false);
                    hasCommonConfig = true;
                } else if ("label".equals(propertiesHtml)) {
                    setupLabel(_uncapServiceName, path, false, false);
                    hasCommonLabel = true;
                } else if ("message".equals(propertiesHtml)) {
                    setupMessage(_uncapServiceName, path, hasCommonLabel, false, false);
                    hasCommonMessage = true;
                } else {
                    String msg = "Unkonwn type for commonMap's propertiesHtml: " + propertiesHtml;
                    throw new DfIllegalPropertySettingException(msg);
                }
            }
        }
        boolean hasAppEnv = false;
        boolean hasAppLabel = false;
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
                        setupEnv(appName, path, hasCommonEnv, hasCommonConfig, lastaEnv);
                        hasAppEnv = true;
                    } else if ("config".equals(propertiesHtml)) {
                        setupConfig(appName, path, hasCommonEnv, hasCommonConfig, hasAppEnv);
                    } else if ("label".equals(propertiesHtml)) {
                        setupLabel(appName, path, hasCommonLabel, hasCommonMessage);
                        hasAppLabel = true;
                    } else if ("message".equals(propertiesHtml)) {
                        setupMessage(appName, path, hasCommonLabel, hasCommonMessage, hasAppLabel);
                    } else {
                        String msg = "Unkonwn type for appMap's propertiesHtml: " + propertiesHtml;
                        throw new DfIllegalPropertySettingException(msg);
                    }
                }
            }
        }
        showPropertiesHtmlSettings();
    }

    protected boolean isUseLastaEnv(Map<String, Object> lastafluteMap) { // mainly true
        final Object obj = lastafluteMap.getOrDefault("isUseLastaEnv", getTrueLiteral());
        return obj != null && ((String) obj).equalsIgnoreCase("true");
    }

    protected void showPropertiesHtmlSettings() {
        final StringBuilder sb = new StringBuilder();
        sb.append("After LastaFlute refecting, existing propertiesHtml settigs: ").append(_propHtmlMap.keySet());
        for (Entry<String, Map<String, Object>> entry : _propHtmlMap.entrySet()) {
            sb.append("\n ").append(entry.getKey()).append(": ").append(entry.getValue());
        }
        logger.info(sb.toString());
    }

    // ===================================================================================
    //                                                                       Configuration
    //                                                                       =============
    protected void setupEnv(String appName, String path, boolean hasCommonEnv, boolean hasCommonConfig, boolean lastaEnv) {
        final Map<String, Object> elementMap = new LinkedHashMap<String, Object>();
        final String theme = "env";
        registerBasicItem(appName, path, elementMap, theme);
        registerEnvironmentMap(appName, path, elementMap, theme, lastaEnv);
        if (hasCommonEnv || hasCommonConfig) { // not root
            final String parentName = _uncapServiceName;
            final String parentTheme = hasCommonConfig ? "config" : "env";
            registerExtendsProp(elementMap, parentName, parentTheme);
        }
    }

    protected void setupConfig(String appName, String path, boolean hasCommonEnv, boolean hasCommonConfig, boolean hasAppEnv) {
        final Map<String, Object> elementMap = new LinkedHashMap<String, Object>();
        registerBasicItem(appName, path, elementMap, "config");
        if (hasCommonEnv || hasCommonConfig || hasAppEnv) { // not root
            final String parentName = hasAppEnv ? appName : _uncapServiceName;
            final String parentTheme = hasAppEnv ? "env" : (hasCommonConfig ? "config" : "env");
            registerExtendsProp(elementMap, parentName, parentTheme);
        }
    }

    // ===================================================================================
    //                                                                            Messages
    //                                                                            ========
    protected void setupLabel(String appName, String path, boolean hasCommonLabel, boolean hasCommonMessage) {
        final Map<String, Object> elementMap = new LinkedHashMap<String, Object>();
        registerBasicItem(appName, path, elementMap, "label");
        if (hasCommonLabel || hasCommonMessage) {
            final String parentName = _uncapServiceName;
            final String parentTheme = hasCommonMessage ? "message" : "label";
            registerExtendsProp(elementMap, parentName, parentTheme);
        }
    }

    protected void setupMessage(String appName, String path, boolean hasCommonLabel, boolean hasCommonMessage, boolean hasAppLabel) {
        final Map<String, Object> elementMap = new LinkedHashMap<String, Object>();
        registerBasicItem(appName, path, elementMap, "message");
        if (hasCommonLabel || hasCommonMessage || hasAppLabel) {
            final String parentName = hasAppLabel ? appName : _uncapServiceName;
            final String parentTheme = hasAppLabel ? "label" : (hasCommonMessage ? "message" : "label");
            registerExtendsProp(elementMap, parentName, parentTheme);
        }
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============

    protected void registerBasicItem(String appName, String path, Map<String, Object> elementMap, String theme) {
        final String capAppName = initCap(appName);
        registerPropertiesHtml(capAppName + buildTitleSuffix(theme), elementMap);
        registerBaseDir(path, elementMap);
        registerRootFile(appName, elementMap, theme);
    }

    protected String buildTitleSuffix(String theme) {
        return initCap(theme);
    }

    protected void registerPropertiesHtml(String key, Map<String, Object> elementMap) {
        final Map<String, Object> existing = _propHtmlMap.get(key);
        if (existing != null) {
            String msg = "Found the existing same-name setting: key=" + key + " existing=" + existing;
            throw new DfIllegalPropertySettingException(msg);
        }
        _propHtmlMap.put(key, elementMap);
    }

    protected void registerBaseDir(String path, Map<String, Object> elementMap) {
        elementMap.put("baseDir", buildBaseDir(path));
    }

    protected String buildBaseDir(String path) {
        return path + "/src";
    }

    protected void registerRootFile(String appName, Map<String, Object> elementMap, String theme) {
        elementMap.put("rootFile", getMainResourcesDir() + "/" + buildPropertiesFileName(appName, theme));
    }

    protected void registerEnvironmentMap(String appName, String path, Map<String, Object> elementMap, String theme, boolean lastaEnv) {
        final Map<String, Object> environmentMap = new LinkedHashMap<String, Object>();
        final String mainResourcesDir = getMainResourcesDir();
        final String rootName = buildPropertiesFileName(appName, theme);
        final String envFilePrefix = doBuildPropertiesFileNameNoExt(appName, theme) + "_";
        final String extSuffix = "." + getPropertiesFileExt();
        if (_environmentList.isEmpty()) { // implicit environment, mainly here
            if (lastaEnv) { // e.g. maihama_env_production.properties
                final String realResourcesDir = Srl.replace(mainResourcesDir, "$$baseDir$$", buildBaseDir(path));
                final File[] envFiles = new File(realResourcesDir).listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return !name.equals(rootName) && name.startsWith(envFilePrefix) && name.endsWith(extSuffix);
                    }
                });
                if (envFiles != null) {
                    for (File envFile : envFiles) { // derive elements actual environment files
                        final String envName = envFile.getName();
                        final String envType = Srl.extractScopeFirst(envName, envFilePrefix, extSuffix).getContent();
                        environmentMap.put(envType, mainResourcesDir + "/" + envName);
                    }
                }
            }
        } else { // explicit environment
            for (String envType : _environmentList) {
                final String envPath;
                if (lastaEnv) { // e.g. maihama_env_production.properties
                    envPath = mainResourcesDir + "/" + buildPropertiesFileName(appName, theme + "_" + envType);
                } else { // maven profile way
                    envPath = "$$baseDir$$/" + envType + "/resources";
                }
                environmentMap.put(envType, envPath);
            }
        }
        elementMap.put("environmentMap", environmentMap);
        if (lastaEnv) {
            // cannot support language type, because cannot detect correctly
            // no problem because of no environment with language
            elementMap.put("isSuppressLangFileDetect", getTrueLiteral());
        }
    }

    protected String getMainResourcesDir() {
        return "$$baseDir$$/main/resources";
    }

    protected String buildPropertiesFileName(String appName, String theme) {
        return doBuildPropertiesFileNameNoExt(appName, theme) + "." + getPropertiesFileExt();
    }

    protected String doBuildPropertiesFileNameNoExt(String appName, String theme) {
        return appName + "_" + theme;
    }

    protected String getPropertiesFileExt() {
        return "properties";
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