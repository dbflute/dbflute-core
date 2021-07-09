/*

 * Copyright 2014-2021 the original author or authors.
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dbflute.DfBuildProperties;
import org.dbflute.exception.DfIllegalPropertySettingException;
import org.dbflute.logic.manage.freegen.DfFreeGenResourceType;
import org.dbflute.properties.DfLittleAdjustmentProperties;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.DfCollectionUtil.AccordingToOrderResource;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @author p1us2er0
 * @since 1.1.0-sp3 (2015/04/26 Sunday)
 */
public final class DfLastaFluteFreeGenReflector {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger _log = LoggerFactory.getLogger(DfLastaFluteFreeGenReflector.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Map<String, Object> _freeGenMap;
    protected final String _capServiceName;
    protected final String _uncapServiceName;
    protected final String _domainPackage;
    protected final String _appPackage;
    protected final String _mylastaPackage;

    // -----------------------------------------------------
    //                                         Global Option
    //                                         -------------
    protected boolean _useDefaultConfigAtGeneration;

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

    public DfLastaFluteFreeGenReflector useDefaultConfigAtGeneration() {
        _useDefaultConfigAtGeneration = true;
        return this;
    }

    // ===================================================================================
    //                                                                     Prepare FreeGen
    //                                                                     ===============
    public void reflectFrom(Map<String, Object> lastafluteMap, String lastaDocOutputDirectory) {
        _log.info("Before LastaFlute refecting, existing freeGen settigs: " + _freeGenMap.keySet());
        boolean hasCommonEnv = false;
        boolean hasCommonConfig = false;
        boolean hasCommonLabel = false;
        boolean hasCommonMessage = false;
        @SuppressWarnings("unchecked")
        final Map<String, Object> commonMap = (Map<String, Object>) lastafluteMap.get("commonMap");
        if (commonMap != null) {
            final String path = (String) commonMap.get("path");
            final List<String> freeGenList = extractCommonFreeGenList(lastafluteMap, commonMap);
            for (String freeGen : freeGenList) {
                _log.info("...Reflecting common freeGen settigs: " + freeGen + ", " + path);
                if ("env".equals(freeGen)) {
                    setupEnvGen(_uncapServiceName, path, false, false);
                    hasCommonEnv = true;
                } else if ("config".equals(freeGen)) {
                    final String pluginInterface = (String) commonMap.get("configPluginInterface");
                    setupConfigGen(_uncapServiceName, path, hasCommonEnv, false, false, pluginInterface);
                    hasCommonConfig = true;
                } else if ("label".equals(freeGen)) {
                    setupLabelGen(_uncapServiceName, path, false, false, lastafluteMap);
                    hasCommonLabel = true;
                } else if ("message".equals(freeGen)) {
                    setupMessageGen(_uncapServiceName, path, hasCommonLabel, false, false, lastafluteMap);
                    hasCommonMessage = true;
                } else if ("mail".equals(freeGen)) {
                    final String pluginInterface = (String) commonMap.get("mailPluginInterface");
                    setupMailFluteGen(_uncapServiceName, path, lastafluteMap, pluginInterface);
                } else if ("template".equals(freeGen)) {
                    setupPmTemplateGen(_uncapServiceName, path, lastafluteMap);
                } else if ("doc".equals(freeGen)) {
                    final String mailPluginInterface = (String) commonMap.get("mailPluginInterface");
                    setupDocGen(_uncapServiceName, path, lastafluteMap, lastaDocOutputDirectory, freeGenList, mailPluginInterface);
                } else if ("namedcls".equals(freeGen)) {
                    setupNamedClsGen(_uncapServiceName, path, lastafluteMap);
                } else {
                    String msg = "Unkonwn type for commonMap's freeGen: " + freeGen;
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
                final List<String> freeGenList = extractAppFreeGenList(lastafluteMap, defMap);
                for (String freeGen : freeGenList) {
                    _log.info("...Reflecting application freeGen settigs: " + appName + "." + freeGen);
                    if ("env".equals(freeGen)) { // should be before config, related each other
                        setupEnvGen(appName, path, hasCommonEnv, hasCommonConfig);
                        hasAppEnv = true;
                    } else if ("config".equals(freeGen)) {
                        final String pluginInterface = (String) defMap.get("configPluginInterface");
                        setupConfigGen(appName, path, hasCommonEnv, hasCommonConfig, hasAppEnv, pluginInterface);
                    } else if ("label".equals(freeGen)) {
                        setupLabelGen(appName, path, hasCommonLabel, hasCommonMessage, lastafluteMap);
                        hasAppLabel = true;
                    } else if ("message".equals(freeGen)) {
                        setupMessageGen(appName, path, hasCommonLabel, hasCommonMessage, hasAppLabel, lastafluteMap);
                    } else if ("mail".equals(freeGen)) {
                        final String pluginInterface = (String) defMap.get("mailPluginInterface");
                        setupMailFluteGen(appName, path, lastafluteMap, pluginInterface);
                    } else if ("template".equals(freeGen)) {
                        setupPmTemplateGen(appName, path, lastafluteMap);
                    } else if ("jsp".equals(freeGen)) {
                        setupJspPathGen(appName, path, lastafluteMap);
                    } else if ("html".equals(freeGen)) {
                        setupHtmlPathGen(appName, path, lastafluteMap);
                    } else if ("doc".equals(freeGen)) {
                        final String mailPluginInterface = (String) defMap.get("mailPluginInterface");
                        setupDocGen(appName, path, lastafluteMap, lastaDocOutputDirectory, freeGenList, mailPluginInterface);
                    } else if ("appcls".equals(freeGen)) {
                        setupAppClsGen(appName, path, lastafluteMap);
                    } else if ("webcls".equals(freeGen)) {
                        setupWebClsGen(appName, path, lastafluteMap);
                    } else if ("namedcls".equals(freeGen)) {
                        setupNamedClsGen(appName, path, lastafluteMap);
                    } else if ("clientcls".equals(freeGen)) {
                        setupClientClsGen(appName, path, lastafluteMap);
                    } else {
                        String msg = "Unkonwn type for appMap's freeGen: " + freeGen;
                        throw new DfIllegalPropertySettingException(msg);
                    }
                }
            }
        }
        showFreeGenSettings();
    }

    protected List<String> extractCommonFreeGenList(Map<String, Object> lastafluteMap, Map<String, Object> commonMap) {
        final String key = "freeGenList";
        @SuppressWarnings("unchecked")
        final List<String> freeGenList = (List<String>) commonMap.get(key); // no needs to order because of no appcls
        if (freeGenList == null) {
            throw new IllegalStateException(key + " is required for commonMap of lastafluteMap.dfprop: " + lastafluteMap);
        }
        return freeGenList;
    }

    protected List<String> extractAppFreeGenList(Map<String, Object> lastafluteMap, Map<String, Object> defMap) {
        final String key = "freeGenList";
        @SuppressWarnings("unchecked")
        final List<String> freeGenList = orderAppFreeGenList((List<String>) defMap.get(key));
        if (freeGenList == null) {
            throw new IllegalStateException(key + " is required for appMap of lastafluteMap.dfprop: " + lastafluteMap);
        }
        return freeGenList;
    }

    protected List<String> orderAppFreeGenList(List<String> freeGenList) { // for refCls
        if (freeGenList == null) {
            return freeGenList;
        }
        final List<String> idList = newBaseFreeGenList();
        idList.add("namedcls"); // should be before appcls, webcls for refCls
        idList.add("appcls");
        idList.add("webcls");
        idList.add("clientcls");
        idList.add("doc"); // last, may depend on appcls as refCls reference registry, just in case
        final List<String> orderedList = new ArrayList<>(freeGenList);
        final AccordingToOrderResource<String, String> resource = new AccordingToOrderResource<>();
        resource.setupResource(idList, el -> el);
        DfCollectionUtil.orderAccordingTo(orderedList, resource);
        return orderedList;
    }

    protected List<String> newBaseFreeGenList() {
        // env should be before config, related each other
        return DfCollectionUtil.newArrayList("env", "config", "label", "message", "mail", "template", "jsp", "html");
    }

    protected void showFreeGenSettings() {
        final StringBuilder sb = new StringBuilder();
        sb.append("After LastaFlute reflecting, existing freeGen settigs: ").append(_freeGenMap.keySet());
        for (Entry<String, Object> entry : _freeGenMap.entrySet()) {
            sb.append("\n ").append(entry.getKey()).append(": ").append(entry.getValue());
        }
        _log.info(sb.toString());
    }

    // ===================================================================================
    //                                                                       Configuration
    //                                                                       =============
    protected void setupEnvGen(String appName, String path, boolean hasCommonEnv, boolean hasCommonConfig) {
        final Map<String, Map<String, Object>> elementMap = new LinkedHashMap<String, Map<String, Object>>();
        final String theme = "env";
        registerFreeGen(initCap(appName) + buildTitleSuffix(theme), elementMap);
        doSetupResourceMap(appName, path, elementMap, theme);
        doSetupOutputConfigMap(appName, elementMap, theme);
        final Map<String, Object> optionMap = createOptionMap();
        elementMap.put("optionMap", optionMap);
        if (!hasCommonEnv && !hasCommonConfig) { // root
            doSetupConfigTableMapRoot(optionMap);
        } else {
            final String parentName = _uncapServiceName;
            final String parentTheme = hasCommonConfig ? "config" : "env";
            doSetupConfigTableMapInheritance(optionMap, parentName, parentTheme);
        }
    }

    protected void setupConfigGen(String appName, String path, boolean hasCommonEnv, boolean hasCommonConfig, boolean hasAppEnv,
            String pluginInterface) {
        final Map<String, Map<String, Object>> elementMap = new LinkedHashMap<String, Map<String, Object>>();
        final String capAppName = initCap(appName);
        final String theme = "config";
        registerFreeGen(capAppName + buildTitleSuffix(theme), elementMap);
        doSetupResourceMap(appName, path, elementMap, theme);
        doSetupOutputConfigMap(appName, elementMap, theme);
        final Map<String, Object> optionMap = createOptionMap();
        elementMap.put("optionMap", optionMap);
        if (pluginInterface != null) {
            optionMap.put("pluginInterface", pluginInterface);
        }
        if (!hasCommonEnv && !hasCommonConfig && !hasAppEnv) { // root
            doSetupConfigTableMapRoot(optionMap);
        } else {
            final String parentName = hasAppEnv ? appName : _uncapServiceName;
            final String parentTheme = hasAppEnv ? "env" : (hasCommonConfig ? "config" : "env");
            doSetupConfigTableMapInheritance(optionMap, parentName, parentTheme);
        }
    }

    protected void doSetupOutputConfigMap(String appName, Map<String, Map<String, Object>> elementMap, String theme) {
        final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
        elementMap.put("outputMap", outputMap);
        outputMap.put("outputDirectory", "$$baseDir$$/java");
        outputMap.put("package", _mylastaPackage + ".direction");
        outputMap.put("templateFile", "LaSystemConfig.vm");
        outputMap.put("className", initCap(appName) + initCap(theme));
    }

    protected void doSetupConfigTableMapRoot(Map<String, Object> optionMap) {
        doSetupConfigTableMapBasic(optionMap);
        optionMap.put("superClassPackage", "org.lastaflute.core.direction");
        optionMap.put("superClassSimpleName", "ObjectiveConfig");
    }

    protected void doSetupConfigTableMapInheritance(Map<String, Object> optionMap, String appName, String theme) {
        doSetupConfigTableMapBasic(optionMap);
        optionMap.put("extendsPropRequest", initCap(appName) + buildTitleSuffix(theme));
        optionMap.put("isCheckImplicitOverride", getTrueLiteral());
        optionMap.put("interfacePackage", _mylastaPackage + ".direction");
        optionMap.put("interfaceSimpleName", initCap(appName) + initCap(theme));
        optionMap.put("superClassPackage", _mylastaPackage + ".direction");
        optionMap.put("superClassSimpleName", initCap(appName) + initCap(theme) + ".SimpleImpl");
    }

    protected void doSetupConfigTableMapBasic(Map<String, Object> optionMap) {
        optionMap.put("isUseDefaultConfigAtGeneration", _useDefaultConfigAtGeneration); // direct use so not literal
    }

    // ===================================================================================
    //                                                                            Messages
    //                                                                            ========
    protected void setupLabelGen(String appName, String path, boolean hasCommonLabel, boolean hasCommonMessage,
            Map<String, Object> lastafluteMap) {
        final Map<String, Map<String, Object>> elementMap = new LinkedHashMap<String, Map<String, Object>>();
        final String theme = "label";
        registerFreeGen(initCap(appName) + buildTitleSuffix(theme), elementMap);
        doSetupResourceMap(appName, path, elementMap, theme);
        doSetupMessageOutputMap(appName, elementMap, theme, lastafluteMap);
        final Map<String, Object> optionMap = createOptionMap();
        elementMap.put("optionMap", optionMap);
        optionMap.put("groupingKeyMap", DfCollectionUtil.newLinkedHashMap(theme, "prefix:labels."));
        if (hasCommonLabel || hasCommonMessage) {
            final String parentName = _uncapServiceName;
            final String parentTheme = hasCommonMessage ? "message" : "label";
            doSetupMessageTableMapInheritance(optionMap, parentName, parentTheme, lastafluteMap);
        }
    }

    protected void setupMessageGen(String appName, String path, boolean hasCommonLabel, boolean hasCommonMessage, boolean hasAppLabel,
            Map<String, Object> lastafluteMap) {
        final Map<String, Map<String, Object>> elementMap = new LinkedHashMap<String, Map<String, Object>>();
        final String theme = "message";
        registerFreeGen(initCap(appName) + buildTitleSuffix(theme), elementMap);
        doSetupResourceMap(appName, path, elementMap, theme);
        doSetupMessageOutputMap(appName, elementMap, theme, lastafluteMap);
        final Map<String, Object> optionMap = createOptionMap();
        elementMap.put("optionMap", optionMap);
        optionMap.put("groupingKeyMap", DfCollectionUtil.newLinkedHashMap("label", "prefix:labels."));
        if (hasCommonLabel || hasCommonMessage || hasAppLabel) {
            final String parentName = hasAppLabel ? appName : _uncapServiceName;
            final String parentTheme = hasAppLabel ? "label" : (hasCommonMessage ? "message" : "label");
            doSetupMessageTableMapInheritance(optionMap, parentName, parentTheme, lastafluteMap);
        }
    }

    protected void doSetupMessageOutputMap(String appName, Map<String, Map<String, Object>> elementMap, String theme,
            Map<String, Object> lastafluteMap) {
        final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
        elementMap.put("outputMap", outputMap);
        outputMap.put("outputDirectory", "$$baseDir$$/java");
        outputMap.put("package", buildMessagesPackage(appName, lastafluteMap));
        outputMap.put("templateFile", "LaUserMessages.vm");
        outputMap.put("className", initCap(appName) + initCap(theme) + "s");
    }

    protected void doSetupMessageTableMapInheritance(Map<String, Object> optionMap, String appName, String theme,
            Map<String, Object> lastafluteMap) {
        optionMap.put("extendsPropRequest", initCap(appName) + buildTitleSuffix(theme));
        optionMap.put("isCheckImplicitOverride", getTrueLiteral());
        optionMap.put("isUseNonNumberVariable", getTrueLiteral());
        optionMap.put("variableExceptList", DfCollectionUtil.newArrayList("item")); // e.g. {item} (is subject)
        if (getLittleAdjustmentProperties().isCompatibleUserMessagesVariableNotOrdered()) { // emergency option
            optionMap.put("isSuppressVariableOrder", getTrueLiteral());
        }
        optionMap.put("superClassPackage", buildMessagesPackage(appName, lastafluteMap));
        optionMap.put("superClassSimpleName", initCap(appName) + initCap(theme) + "s");
    }

    protected String buildMessagesPackage(String appName, Map<String, Object> lastafluteMap) {
        final String messagesPackage = _mylastaPackage + ".action";
        return filterOverridden(messagesPackage, lastafluteMap, appName, "message", "package");
    }

    // ===================================================================================
    //                                                                           MailFlute
    //                                                                           =========
    protected void setupMailFluteGen(String appName, String path, Map<String, Object> lastafluteMap, String pluginInterface) {
        doSetupMailFluteGen(appName, path, "$$baseDir$$/resources/mail", "dfmail", lastafluteMap, pluginInterface);
    }

    protected void doSetupMailFluteGen(String appName, String path, String targetDir, String ext, Map<String, Object> lastafluteMap,
            String pluginInterface) {
        final Map<String, Map<String, Object>> pathMap = new LinkedHashMap<String, Map<String, Object>>();
        final String capAppName = initCap(appName);
        registerFreeGen(capAppName + "MailFlute", pathMap);
        final Map<String, Object> resourceMap = new LinkedHashMap<String, Object>();
        pathMap.put("resourceMap", resourceMap);
        resourceMap.put("baseDir", path + "/src/main");
        resourceMap.put("resourceType", DfFreeGenResourceType.MAIL_FLUTE.name());
        final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
        pathMap.put("outputMap", outputMap);
        outputMap.put("outputDirectory", "$$baseDir$$/java");
        outputMap.put("package", buildMailPostcardPackage(capAppName, lastafluteMap));
        outputMap.put("templateFile", "LaMailFlute.vm");
        outputMap.put("className", initCap(appName) + "Postcard");
        final Map<String, Object> optionMap = createOptionMap();
        pathMap.put("optionMap", optionMap);
        optionMap.put("targetDir", filterOverridden(targetDir, lastafluteMap, appName, "mail", "targetDir"));
        optionMap.put("targetExt", filterOverridden("." + ext, lastafluteMap, appName, "mail", "targetExt"));
        final List<String> exceptPathList = DfCollectionUtil.newArrayList("contain:/mail/common/");
        optionMap.put("exceptPathList", filterOverridden(exceptPathList, lastafluteMap, appName, "mail", "exceptPathList"));
        if (pluginInterface != null) {
            optionMap.put("pluginInterface", pluginInterface);
        }
    }

    protected String buildMailPostcardPackage(String appName, Map<String, Object> lastafluteMap) {
        final String generatedPackage = _mylastaPackage + ".mail";
        return filterOverridden(generatedPackage, lastafluteMap, appName, "mail", "package");
    }

    // ===================================================================================
    //                                                                          PmTemplate
    //                                                                          ==========
    protected void setupPmTemplateGen(String appName, String path, Map<String, Object> lastafluteMap) {
        doSetupPmTemplateGen(appName, path, "$$baseDir$$/resources", "dfpm", lastafluteMap);
    }

    protected void doSetupPmTemplateGen(String appName, String path, String targetDir, String ext, Map<String, Object> lastafluteMap) {
        final Map<String, Map<String, Object>> pathMap = new LinkedHashMap<String, Map<String, Object>>();
        final String capAppName = initCap(appName);
        registerFreeGen(capAppName + "PmTemplate", pathMap);
        final Map<String, Object> resourceMap = new LinkedHashMap<String, Object>();
        pathMap.put("resourceMap", resourceMap);
        resourceMap.put("baseDir", path + "/src/main");
        resourceMap.put("resourceType", DfFreeGenResourceType.PM_FILE.name());
        final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
        pathMap.put("outputMap", outputMap);
        outputMap.put("outputDirectory", "$$baseDir$$/java");
        outputMap.put("package", buildPmTemplateBeanPackage(capAppName, lastafluteMap));
        outputMap.put("templateFile", "LaPmTemplate.vm");
        outputMap.put("className", "unused");
        final Map<String, Object> optionMap = createOptionMap();
        pathMap.put("optionMap", optionMap);
        optionMap.put("targetDir", filterOverridden(targetDir, lastafluteMap, appName, "template", "targetDir"));
        optionMap.put("targetExt", filterOverridden("." + ext, lastafluteMap, appName, "template", "targetExt"));
        optionMap.put("isConventionSuffix", getTrueLiteral());
        optionMap.put("isLastaTemplate", getTrueLiteral());
    }

    protected String buildPmTemplateBeanPackage(String appName, Map<String, Object> lastafluteMap) {
        final String generatedPackage = _mylastaPackage; // template package is added later if convention
        return filterOverridden(generatedPackage, lastafluteMap, appName, "template", "package");
    }

    // ===================================================================================
    //                                                                       HTML Template
    //                                                                       =============
    protected void setupJspPathGen(String appName, String path, Map<String, Object> lastafluteMap) {
        final String targetDir = "$$baseDir$$/webapp/WEB-INF/view";
        doSetupHtmlTemplatePathGen(appName, path, targetDir, "jsp", lastafluteMap);
    }

    protected void setupHtmlPathGen(String appName, String path, Map<String, Object> lastafluteMap) {
        // daone jflute lastaflute: thinking Thymeleaf localtion => same as JSP for now
        //final String targetDir = "$$baseDir$$/resources/templates";
        final String targetDir = "$$baseDir$$/webapp/WEB-INF/view";
        doSetupHtmlTemplatePathGen(appName, path, targetDir, "html", lastafluteMap);
    }

    protected void doSetupHtmlTemplatePathGen(String appName, String path, String targetDir, String ext,
            Map<String, Object> lastafluteMap) {
        final Map<String, Map<String, Object>> pathMap = new LinkedHashMap<String, Map<String, Object>>();
        final String capAppName = initCap(appName);
        registerFreeGen(capAppName + initCap(ext) + "Path", pathMap);
        final Map<String, Object> resourceMap = new LinkedHashMap<String, Object>();
        pathMap.put("resourceMap", resourceMap);
        resourceMap.put("baseDir", path + "/src/main");
        resourceMap.put("resourceType", DfFreeGenResourceType.FILE_PATH.name());
        final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
        pathMap.put("outputMap", outputMap);
        outputMap.put("outputDirectory", "$$baseDir$$/java");
        outputMap.put("package", buildHtmlTemplatePackage(capAppName, lastafluteMap, ext));
        outputMap.put("templateFile", "LaHtmlPath.vm");
        outputMap.put("className", initCap(appName) + "HtmlPath");
        final Map<String, Object> optionMap = createOptionMap();
        pathMap.put("optionMap", optionMap);
        optionMap.put("targetDir", filterOverridden(targetDir, lastafluteMap, appName, ext, "targetDir"));
        final String targetExt = ext.equals("html") ? ".html" : ("." + ext + "|.html");
        optionMap.put("targetExt", filterOverridden(targetExt, lastafluteMap, appName, ext, "targetExt"));
        final List<String> exceptPathList = DfCollectionUtil.newArrayList("contain:/view/common/");
        optionMap.put("exceptPathList", filterOverridden(exceptPathList, lastafluteMap, appName, ext, "exceptPathList"));
    }

    protected String buildHtmlTemplatePackage(String appName, Map<String, Object> lastafluteMap, String ext) {
        // for simple rule "generate and reboot", making HTML file not always need hot-deploy
        //final String generatedPackage = _appPackage + ".web.base.html";
        final String generatedPackage = _mylastaPackage + ".action";
        return filterOverridden(generatedPackage, lastafluteMap, appName, ext, "package");
    }

    // ===================================================================================
    //                                                                            Document
    //                                                                            ========
    // last process recommended for generated classes by others
    protected void setupDocGen(String appName, String path, Map<String, Object> lastafluteMap, String lastaDocOutputDirectory,
            List<String> freeGenList, String mailPluginInterface) {
        final Map<String, Map<String, Object>> docMap = new LinkedHashMap<String, Map<String, Object>>();
        final String theme = "lastaDoc";
        final String capAppName = initCap(appName);
        registerFreeGen(capAppName + buildTitleSuffix(theme), docMap);
        final Map<String, Object> resourceMap = new LinkedHashMap<String, Object>();
        docMap.put("resourceMap", resourceMap);
        resourceMap.put("baseDir", path + "/src/main");
        resourceMap.put("resourceType", DfFreeGenResourceType.LASTA_DOC.name());
        final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
        docMap.put("outputMap", outputMap);
        outputMap.put("outputDirectory", lastaDocOutputDirectory);
        outputMap.put("package", ""); // flat documents e.g. ./output/doc/lastadoc-harbor.html
        outputMap.put("templateFile", "LaDocHtml.vm");
        outputMap.put("className", "lastadoc-" + appName);
        outputMap.put("fileExt", "html");
        final Map<String, Object> optionMap = createOptionMap();
        docMap.put("optionMap", optionMap);
        optionMap.put("isLastaDoc", true); // for e.g. reference control
        optionMap.put("path", path);
        optionMap.put("targetDir", "$$baseDir$$/java");
        optionMap.put("appName", appName);

        if (freeGenList.contains("mail")) {
            optionMap.put("mailPackage", buildMailPostcardPackage(capAppName, lastafluteMap));
            optionMap.put("mailTargetDir", filterOverridden("$$baseDir$$/resources/mail", lastafluteMap, appName, "mail", "targetDir"));
            optionMap.put("mailTargetExt", filterOverridden(".dfmail", lastafluteMap, appName, "mail", "targetExt"));
            final List<String> mailExceptPathList = DfCollectionUtil.newArrayList("contain:/mail/common/");
            optionMap.put("mailExceptPathList", filterOverridden(mailExceptPathList, lastafluteMap, appName, "mail", "exceptPathList"));
            if (mailPluginInterface != null) {
                optionMap.put("mailPluginInterface", mailPluginInterface);
            }
        }

        if (freeGenList.contains("template")) {
            optionMap.put("templatePackage", buildPmTemplateBeanPackage(capAppName, lastafluteMap));
            optionMap.put("templateTargetDir", filterOverridden("$$baseDir$$/resources", lastafluteMap, appName, "template", "targetDir"));
            optionMap.put("templateTargetExt", filterOverridden("." + "dfpm", lastafluteMap, appName, "template", "targetExt"));
            optionMap.put("templateIsConventionSuffix", getTrueLiteral());
            optionMap.put("templateIsLastaTemplate", getTrueLiteral());
        }

        if (freeGenList.contains("appcls")) {
            final String appClsResourceFile = path + "/src/main/resources/" + appName + "_appcls.dfprop";
            optionMap.put("appclsResourceFile", filterOverridden(appClsResourceFile, lastafluteMap, appName, "appcls", "resourceFile"));
            optionMap.put("appclsPackage", buildCDefPackage("appcls", lastafluteMap, "appcls", "appcls"));
            optionMap.put("appclsClassName", buildCDefClassName("AppCDef", "appcls", lastafluteMap, "appcls"));
        }

        if (freeGenList.contains("webcls")) {
            final String webclsResourceFile = path + "/src/main/resources/" + appName + "_webcls.dfprop";
            optionMap.put("webclsResourceFile", filterOverridden(webclsResourceFile, lastafluteMap, appName, "webcls", "resourceFile"));
            optionMap.put("webclsPackage", buildCDefPackage(appName, lastafluteMap, "webcls", "webcls"));
            optionMap.put("webclsClassName", buildCDefClassName("AppCDef", "appcls", lastafluteMap, "appcls"));
        }

        if (freeGenList.contains("namedcls")) {
            doSetupDocNamedClsGen(appName, path, lastafluteMap, optionMap);
        }
    }

    protected void doSetupDocNamedClsGen(String appName, String path, Map<String, Object> lastafluteMap, Map<String, Object> optionMap) {
        final List<Map<String, Object>> namedclsList = new ArrayList<Map<String, Object>>();
        optionMap.put("namedclsList", namedclsList);
        final String baseDir = path + "/src/main";
        final String middlePath = "resources/namedcls";
        final String namedclsPath = baseDir + "/" + middlePath;
        final File namedclsDir = new File(namedclsPath);
        if (!namedclsDir.exists()) {
            return;
        }
        final String filePrefix = appName + "_";
        final String fileSuffix = "_cls.dfprop";
        final File[] dfpropFiles = namedclsDir.listFiles(file -> {
            return file.isFile() && file.getName().startsWith(filePrefix) && file.getName().endsWith(fileSuffix);
        });
        if (dfpropFiles == null || dfpropFiles.length == 0) {
            return;
        }
        for (File dfpropFile : dfpropFiles) {
            final Map<String, Object> namedclsMap = new LinkedHashMap<String, Object>();
            final String dfpropName = dfpropFile.getName();
            final String clsDomain = Srl.extractScopeFirst(dfpropName, filePrefix, fileSuffix).getContent();
            final String clsTheme = clsDomain + "_cls"; // e.g. vinci_cls
            namedclsMap.put("clsDomain", clsDomain);
            namedclsMap.put("clsTheme", clsTheme);

            final String clsResourceFile = namedclsPath + "/" + dfpropName;
            namedclsMap.put(clsTheme + "ResourceFile", filterOverridden(clsResourceFile, lastafluteMap, appName, clsTheme, "resourceFile"));
            namedclsMap.put(clsTheme + "Package", buildCDefPackage(appName, lastafluteMap, "namedcls", clsTheme));

            namedclsList.add(namedclsMap);
        }
    }

    // ===================================================================================
    //                                                                      Classification
    //                                                                      ==============
    // -----------------------------------------------------
    //                                                AppCls
    //                                                ------
    // cannot use in common, application project only: use named classification instead if you need
    protected void setupAppClsGen(String appName, String path, Map<String, Object> lastafluteMap) {
        final Map<String, Map<String, Object>> pathMap = new LinkedHashMap<String, Map<String, Object>>();
        registerFreeGen(initCap(appName) + "AppCls", pathMap);
        final Map<String, Object> resourceMap = new LinkedHashMap<String, Object>();
        pathMap.put("resourceMap", resourceMap);
        resourceMap.put("baseDir", path + "/src/main");
        final String resourceFile = "$$baseDir$$/resources/" + appName + "_appcls.dfprop";
        final String clsTheme = "appcls";
        resourceMap.put("resourceFile", filterOverridden(resourceFile, lastafluteMap, appName, clsTheme, "resourceFile"));
        resourceMap.put("resourceType", DfFreeGenResourceType.APP_CLS.name());
        final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
        pathMap.put("outputMap", outputMap);
        outputMap.put("outputDirectory", "$$baseDir$$/java");
        final String cdefPackage = buildCDefPackage(appName, lastafluteMap, clsTheme, clsTheme);
        outputMap.put("package", cdefPackage);
        outputMap.put("templateFile", "LaAppCDef.vm");
        final String cdefClassName = buildCDefClassName("AppCDef", appName, lastafluteMap, clsTheme);
        outputMap.put("className", cdefClassName);
        final Map<String, Object> optionMap = createOptionMap();
        pathMap.put("optionMap", optionMap);
        optionMap.put("clsDomain", "app");
        optionMap.put("clsTitle", "application"); // same as old javadoc
        optionMap.put("clsTheme", clsTheme);
        optionMap.put("cdefPackage", cdefPackage); // for refCls reference
        optionMap.put("cdefClassName", cdefClassName); // me too
        doSetupSuppressDBClsCollaboration(lastafluteMap, appName, clsTheme, optionMap);
        doSetupSuppressRedundantCommentStop(lastafluteMap, appName, clsTheme, optionMap);
    }

    // -----------------------------------------------------
    //                                                WebCls
    //                                                ------
    // cannot use in common, application project only: use named classification instead if you need
    protected void setupWebClsGen(String appName, String path, Map<String, Object> lastafluteMap) { // cannot use in common
        final Map<String, Map<String, Object>> pathMap = new LinkedHashMap<String, Map<String, Object>>();
        final String capAppName = initCap(appName);
        registerFreeGen(capAppName + "WebCls", pathMap);
        final Map<String, Object> resourceMap = new LinkedHashMap<String, Object>();
        pathMap.put("resourceMap", resourceMap);
        resourceMap.put("baseDir", path + "/src/main");
        final String resourceFile = "$$baseDir$$/resources/" + appName + "_webcls.dfprop";
        final String clsTheme = "webcls";
        resourceMap.put("resourceFile", filterOverridden(resourceFile, lastafluteMap, appName, clsTheme, "resourceFile"));
        resourceMap.put("resourceType", DfFreeGenResourceType.WEB_CLS.name());
        final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
        pathMap.put("outputMap", outputMap);
        outputMap.put("outputDirectory", "$$baseDir$$/java");
        final String cdefPackage = buildCDefPackage(appName, lastafluteMap, clsTheme, clsTheme);
        outputMap.put("package", cdefPackage);
        outputMap.put("templateFile", "LaAppCDef.vm");
        final String cdefClassName = buildCDefClassName("WebCDef", capAppName, lastafluteMap, clsTheme);
        outputMap.put("className", cdefClassName);
        final Map<String, Object> optionMap = createOptionMap();
        pathMap.put("optionMap", optionMap);
        optionMap.put("clsDomain", "web");
        optionMap.put("clsTitle", "web");
        optionMap.put("clsTheme", clsTheme);
        optionMap.put("cdefPackage", cdefPackage); // for refCls reference
        optionMap.put("cdefClassName", cdefClassName); // me too
        doSetupSuppressDBClsCollaboration(lastafluteMap, appName, clsTheme, optionMap);
        doSetupSuppressRedundantCommentStop(lastafluteMap, appName, clsTheme, optionMap);
    }

    // -----------------------------------------------------
    //                                              NamedCls
    //                                              --------
    // can be used in both common and application freely
    protected void setupNamedClsGen(String appName, String path, Map<String, Object> lastafluteMap) {
        final String baseDir = path + "/src/main";
        final String middlePath = "resources/namedcls";
        final String namedclsPath = baseDir + "/" + middlePath;
        final File namedclsDir = new File(namedclsPath);
        if (!namedclsDir.exists()) {
            _log.info("*No namedcls directory so skip it: namedclsPath=" + namedclsPath);
            return;
        }
        final String filePrefix = appName + "_";
        final String fileSuffix = "_cls.dfprop";
        final File[] dfpropFiles = namedclsDir.listFiles(file -> {
            return file.isFile() && file.getName().startsWith(filePrefix) && file.getName().endsWith(fileSuffix);
        });
        if (dfpropFiles == null || dfpropFiles.length == 0) {
            _log.info("*No namedcls dfprop file in the directory so skip it: " + namedclsPath);
            return;
        }
        for (File dfpropFile : dfpropFiles) {
            final String dfpropName = dfpropFile.getName();
            final String clsDomain = Srl.extractScopeFirst(dfpropName, filePrefix, fileSuffix).getContent();
            doSetupNamedClsGen(appName, clsDomain, lastafluteMap, baseDir, middlePath, dfpropName);
        }
    }

    protected void doSetupNamedClsGen(String appName, String clsDomain, Map<String, Object> lastafluteMap, String baseDir,
            String middlePath, String dfpropName) {
        final String clsTheme = clsDomain + "_cls"; // e.g. vinci_cls
        final Map<String, Map<String, Object>> pathMap = new LinkedHashMap<String, Map<String, Object>>();
        registerFreeGen(initCap(appName) + initCap(clsDomain) + "Cls", pathMap);
        final Map<String, Object> resourceMap = new LinkedHashMap<String, Object>();
        pathMap.put("resourceMap", resourceMap);
        resourceMap.put("baseDir", baseDir);
        final String resourceFile = "$$baseDir$$/" + middlePath + "/" + dfpropName;
        resourceMap.put("resourceFile", filterOverridden(resourceFile, lastafluteMap, appName, clsTheme, "resourceFile"));
        resourceMap.put("resourceType", DfFreeGenResourceType.APP_CLS.name());
        final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
        pathMap.put("outputMap", outputMap);
        outputMap.put("outputDirectory", filterOverridden("$$baseDir$$/java", lastafluteMap, appName, clsTheme, "outputDirectory"));
        final String cdefPackage = buildCDefPackage(appName, lastafluteMap, "namedcls", clsTheme);
        outputMap.put("package", cdefPackage);
        outputMap.put("templateFile", "LaAppCDef.vm"); // borrow application classification's template
        final String cdefClassName = buildCDefClassName(initCap(clsDomain) + "CDef", appName, lastafluteMap, clsTheme);
        outputMap.put("className", cdefClassName);
        final Map<String, Object> optionMap = createOptionMap();
        pathMap.put("optionMap", optionMap);
        optionMap.put("clsDomain", clsDomain);
        optionMap.put("clsTitle", clsDomain); // same as domain
        optionMap.put("clsTheme", clsTheme);
        optionMap.put("cdefPackage", cdefPackage); // for refCls reference
        optionMap.put("cdefClassName", cdefClassName); // me too
        doSetupSuppressDBClsCollaboration(lastafluteMap, appName, clsTheme, optionMap);
        doSetupSuppressRedundantCommentStop(lastafluteMap, appName, clsTheme, optionMap);
    }

    // -----------------------------------------------------
    //                                    CDef Package/Class
    //                                    ------------------
    private String buildCDefPackage(String appName, Map<String, Object> lastafluteMap, String nearPackage, String clsTheme) {
        return filterOverridden(_mylastaPackage + "." + nearPackage, lastafluteMap, appName, clsTheme, "package");
    }

    protected String buildCDefClassName(String className, String appName, Map<String, Object> lastafluteMap, String clsTheme) {
        return filterOverridden(className, lastafluteMap, appName, clsTheme, "className");
    }

    // -----------------------------------------------------
    //                                   DBCls Collaboration
    //                                   -------------------
    protected void doSetupSuppressDBClsCollaboration(Map<String, Object> lastafluteMap, String appName, String clsTheme,
            Map<String, Object> optionMap) {
        // #for_now jflute should rename DBCls to RefCls, but very rare option (2021/07/08)
        final String key = "isSuppressDBClsCollaboration";
        final boolean overriddenValue = false; // used in template so you can use boolean directly
        optionMap.put(key, filterOverridden(overriddenValue, lastafluteMap, appName, clsTheme, key));
    }

    // -----------------------------------------------------
    //                                 RedundantComment Stop
    //                                 ---------------------
    protected void doSetupSuppressRedundantCommentStop(Map<String, Object> lastafluteMap, String appName, String clsTheme,
            Map<String, Object> optionMap) {
        final String key = "isSuppressRedundantCommentStop";
        final boolean overriddenValue = false; // used in template so you can use boolean directly
        optionMap.put(key, filterOverridden(overriddenValue, lastafluteMap, appName, clsTheme, key));
    }

    // -----------------------------------------------------
    //                                             ClientCls
    //                                             ---------
    // @since 1.2.5
    // cannot use in common, application project only, depending on appcls that is generation resource
    protected void setupClientClsGen(String appName, String path, Map<String, Object> lastafluteMap) {
        final Map<String, Map<String, Object>> pathMap = new LinkedHashMap<String, Map<String, Object>>();
        registerFreeGen(initCap(appName) + "ClientCls", pathMap);
        final Map<String, Object> resourceMap = new LinkedHashMap<String, Object>();
        pathMap.put("resourceMap", resourceMap);
        resourceMap.put("baseDir", path + "/src/main");
        final String clsTheme = "clientcls";
        final String resourceFile = prepareClientClsResourceFile(appName, lastafluteMap, clsTheme);
        resourceMap.put("resourceFile", filterOverridden(resourceFile, lastafluteMap, appName, clsTheme, "resourceFile"));
        resourceMap.put("resourceType", DfFreeGenResourceType.APP_CLS.name());
        final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
        pathMap.put("outputMap", outputMap);
        outputMap.put("outputDirectory", "./output/shared"); // as default
        final String outputPackage = filterOverridden("clientcls", lastafluteMap, appName, clsTheme, "package");
        outputMap.put("package", outputPackage);
        outputMap.put("templateFile", "./shared/LaClientClsDfProp.vm");
        final String outputFileName = filterOverridden("clientnamehere_" + appName + "_cls", lastafluteMap, appName, clsTheme, "className");
        outputMap.put("className", outputFileName);
        outputMap.put("fileExt", "dfprop");
        final Map<String, Object> optionMap = createOptionMap();
        pathMap.put("optionMap", optionMap);
        optionMap.put("serverServiceName", appName); // for comment
        optionMap.put("cdefPackage", outputPackage); // required dummy for loader, so unused in this template
        optionMap.put("cdefClassName", outputFileName); // me too
        doSetupSuppressDBClsCollaboration(lastafluteMap, appName, clsTheme, optionMap);
        doSetupSuppressRedundantCommentStop(lastafluteMap, appName, clsTheme, optionMap);
    }

    protected String prepareClientClsResourceFile(String appName, Map<String, Object> lastafluteMap, final String clsTheme) {
        // #hope jflute also DB cls (action when requested from application) (2021/07/06)
        final String defaultResourceCls = "appcls"; // as default
        final String resourceClsTheme = filterOverridden(defaultResourceCls, lastafluteMap, appName, clsTheme, "resourceClsTheme");
        return "$$baseDir$$/resources/" + appName + "_" + resourceClsTheme + ".dfprop"; // e.g. hangar_appcls.dfprop
    }

    // ===================================================================================
    //                                                                           Core Info
    //                                                                           =========
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
        resourceMap.put("resourceType", DfFreeGenResourceType.PROP.name());
        resourceMap.put("resourceFile", "$$baseDir$$/resources/" + appName + "_" + theme + ".properties");
    }

    protected Map<String, Object> createOptionMap() {
        final Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("isLastaFlute", true); // direct use so not literal
        return map;
    }

    // ===================================================================================
    //                                                                          Overridden
    //                                                                          ==========
    protected <VALUE> VALUE filterOverridden(VALUE overriddenValue, Map<String, Object> lastafluteMap, String appName, String title,
            String key) {
        // e.g. lastafluteMap.dfprop
        //  ; overrideMap = map:{
        //      ; hangar.freeGen.appcls.isSuppressRedundantCommentStop = true
        //  }
        @SuppressWarnings("unchecked")
        final Map<String, VALUE> overrideMap = (Map<String, VALUE>) lastafluteMap.get("overrideMap");
        if (overrideMap == null) {
            return overriddenValue;
        }
        final String fullKey = appName + ".freeGen." + title + "." + key;
        return (VALUE) overrideMap.getOrDefault(fullKey, overriddenValue);
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    public DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    public DfLittleAdjustmentProperties getLittleAdjustmentProperties() {
        return getProperties().getLittleAdjustmentProperties();
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected String buildTitleSuffix(String theme) {
        return initCap(theme);
    }

    protected String getTrueLiteral() {
        // for DfPropTableLoader@isProperty(), so not use this if direct use by jflute (2017/06/21)
        // can fix it? later I will think...
        // (while, you can use boolean if it's used in template directly)
        return "true";
    }

    protected String initCap(String project) {
        return Srl.initCap(project);
    }
}