/*
 * Copyright 2014-2024 the original author or authors.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.dbflute.exception.DfIllegalPropertySettingException;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.logic.generate.language.DfLanguageDependency;
import org.dbflute.logic.generate.language.framework.DfLanguageFramework;
import org.dbflute.util.DfTypeUtil;

/**
 * @author jflute
 * @since 0.8.8.1 (2009/01/07 Wednesday)
 */
public final class DfDependencyInjectionProperties extends DfAbstractDBFluteProperties {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfDependencyInjectionProperties(Properties prop) {
        super(prop);
    }

    // ===================================================================================
    //                                                            Dependency Injection Map
    //                                                            ========================
    public static final String KEY_dependencyInjectionMap = "dependencyInjectionMap";
    protected Map<String, Object> _dependencyInjectionMap;

    public Map<String, Object> getDependencyInjectionMap() {
        if (_dependencyInjectionMap == null) {
            final Map<String, Object> map = mapProp("torque." + KEY_dependencyInjectionMap, DEFAULT_EMPTY_MAP);
            _dependencyInjectionMap = newLinkedHashMap();
            _dependencyInjectionMap.putAll(map);
        }
        return _dependencyInjectionMap;
    }

    public String getProperty(String key, String defaultValue) {
        Map<String, Object> map = getDependencyInjectionMap();
        Object obj = map.get(key);
        if (obj != null) {
            if (!(obj instanceof String)) {
                String msg = "The key's value should be string:";
                msg = msg + " " + DfTypeUtil.toClassTitle(obj) + "=" + obj;
                throw new IllegalStateException(msg);
            }
            String value = (String) obj;
            if (value.trim().length() > 0) {
                return value;
            } else {
                return defaultValue;
            }
        }
        return stringProp("torque." + key, defaultValue);
    }

    public boolean isProperty(String key, boolean defaultValue) {
        Map<String, Object> map = getDependencyInjectionMap();
        Object obj = map.get(key);
        if (obj != null) {
            if (!(obj instanceof String)) {
                String msg = "The key's value should be boolean:";
                msg = msg + " " + DfTypeUtil.toClassTitle(obj) + "=" + obj;
                throw new IllegalStateException(msg);
            }
            String value = (String) obj;
            if (value.trim().length() > 0) {
                return value.trim().equalsIgnoreCase("true");
            } else {
                return defaultValue;
            }
        }
        return booleanProp("torque." + key, defaultValue);
    }

    public Map<String, Object> getPropertyAsMap(String key, Map<String, Object> defaultValue) {
        Map<String, Object> map = getDependencyInjectionMap();
        Object obj = map.get(key);
        if (obj != null) {
            if (!(obj instanceof Map<?, ?>)) {
                String msg = "The key's value should be map:";
                msg = msg + " " + DfTypeUtil.toClassTitle(obj) + "=" + obj;
                throw new IllegalStateException(msg);
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> value = (Map<String, Object>) obj;
            if (!value.isEmpty()) {
                return value;
            } else {
                return defaultValue;
            }
        }
        return mapProp("torque." + key, defaultValue);
    }

    // ===================================================================================
    //                                                           DBFluteBeans(Spring/Lucy)
    //                                                           =========================
    public List<String> getDBFluteBeansPackageNameList() { // Java Only
        final String prop = getProperty("dbfluteBeansPackageName", null);
        if (prop == null) {
            return new ArrayList<String>();
        }
        final String[] array = prop.split(";");
        final List<String> ls = new ArrayList<String>();
        for (String string : array) {
            ls.add(string.trim());
        }
        return ls;
    }

    public String getDBFluteBeansFileName() { // Java Only
        return getProperty("dbfluteBeansFileName", "dbfluteBeans.xml");
    }

    public String getDBFluteBeansDataSourceName() { // Java Only
        return getProperty("dbfluteBeansDataSourceName", "dataSource");
    }

    public String getDBFluteBeansDefaultAttribute() { // Java Only
        final String prop = getProperty("dbfluteBeansDefaultAttribute", null);
        return prop != null ? prop : "";
    }

    public boolean isDBFluteBeansGeneratedAsJavaConfig() { // Java Only
        final DfLittleAdjustmentProperties prop = getLittleAdjustmentProperties();
        return isProperty("isDBFluteBeansGeneratedAsJavaConfig", !prop.isCompatibleBeforeJava8());
    }

    protected boolean isDBFluteBeansHybritScanConfig() { // closet, Java Only, for compatible
        return isProperty("isDBFluteBeansHybritScanConfig", true); // default: use
    }

    public boolean needsDBFluteBeansHybritScanConfig() {
        final DfBasicProperties prop = getBasicProperties();
        return prop.isTargetContainerSpring() && isDBFluteBeansGeneratedAsJavaConfig() && isDBFluteBeansHybritScanConfig();
    }

    public boolean needsBehaviorSpringAutowired() {
        return isDBFluteBeansGeneratedAsJavaConfig() && needsDBFluteBeansHybritScanConfig();
    }

    public boolean isDBFluteBeansJavaConfigLazy() { // closet, Java Only, for compatible
        return isProperty("isDBFluteBeansJavaConfigLazy", true); // default: lazy
    }

    protected String getDBFluteBeansRuntimeComponentPrefix() { // closet, Java Only
        return getProperty("dbfluteBeansRuntimeComponentPrefix", null);
    }

    public boolean hasDBFluteBeansTransactionalDataSourcePackage() { // Java Only
        return getDBFluteBeansTransactionalDataSourcePackage() != null;
    }

    public String getDBFluteBeansTransactionalDataSourcePackage() { // Java Only
        // you can add needsSpringTransactionalDataSource() determination for e.g. AbstractRoutingDataSource
        return getProperty("dbfluteBeansTransactionalDataSourcePackage", null);
    }

    // ===================================================================================
    //                                                            DBFluteModule(Guice/CDI)
    //                                                            ========================
    public boolean isDBFluteModuleGuiceRuntimeComponentByName() { // Java Only
        return isProperty("isDBFluteModuleGuiceRuntimeComponentByName", false); // basically for multiple DB
    }

    // ===================================================================================
    //                                                                       Dicon(Seasar)
    //                                                                       =============
    public String getDBFluteDiconNamespace() { // Java Only
        return getProperty("dbfluteDiconNamespace", getLanguageFramework().getDBFluteDiconNamespace());
    }

    public List<String> getDBFluteDiconPackageNameList() { // Java Only
        final String prop = getProperty("dbfluteDiconPackageName", null);
        if (prop == null) {
            return new ArrayList<String>();
        }
        final String[] array = prop.split(";");
        final List<String> ls = new ArrayList<String>();
        for (String string : array) {
            ls.add(string.trim());
        }
        return ls;
    }

    public String getDBFluteDiconFileName() { // Java Only
        return getProperty("dbfluteDiconFileName", getLanguageFramework().getDBFluteDiconFileName());
    }

    public String getDBFluteCreatorDiconFileName() { // It's closet! Java Only
        return getProperty("dbfluteCreatorDiconFileName", "dbflute-creator.dicon");
    }

    public String getDBFluteCustomizerDiconFileName() { // It's closet! Java Only
        return getProperty("dbfluteCustomizerDiconFileName", "dbflute-customizer.dicon");
    }

    public String getJ2eeDiconResourceName() { // Java Only
        return getProperty("j2eeDiconResourceName", getLanguageFramework().getJ2eeDiconResourceName());
    }

    protected DfLanguageDependency getLanguageDependencyInfo() {
        return getBasicProperties().getLanguageDependency();
    }

    public static final String KEY_dbfluteDiconBeforeJ2eeIncludeDefinitionMap = "dbfluteDiconBeforeJ2eeIncludeDefinitionMap";
    protected Map<String, Object> _dbfluteDiconBeforeJ2eeIncludeDefinitionMap;

    public Map<String, Object> getDBFluteDiconBeforeJ2eeIncludeDefinitionMap() {
        if (_dbfluteDiconBeforeJ2eeIncludeDefinitionMap != null) {
            return _dbfluteDiconBeforeJ2eeIncludeDefinitionMap;
        }
        String key = KEY_dbfluteDiconBeforeJ2eeIncludeDefinitionMap;
        final Map<String, Object> map = getPropertyAsMap(key, DEFAULT_EMPTY_MAP);
        _dbfluteDiconBeforeJ2eeIncludeDefinitionMap = newLinkedHashMap();
        _dbfluteDiconBeforeJ2eeIncludeDefinitionMap.putAll(map);
        return _dbfluteDiconBeforeJ2eeIncludeDefinitionMap;
    }

    public List<String> getDBFluteDiconBeforeJ2eeIncludePathList() {
        return new ArrayList<String>(getDBFluteDiconBeforeJ2eeIncludeDefinitionMap().keySet());
    }

    public static final String KEY_dbfluteDiconOtherIncludeDefinitionMap = "dbfluteDiconOtherIncludeDefinitionMap";
    protected Map<String, Object> _dbfluteDiconOtherIncludeDefinitionMap;

    public Map<String, Object> getDBFluteDiconOtherIncludeDefinitionMap() {
        if (_dbfluteDiconOtherIncludeDefinitionMap != null) {
            return _dbfluteDiconOtherIncludeDefinitionMap;
        }
        String key = KEY_dbfluteDiconOtherIncludeDefinitionMap;
        final Map<String, Object> map = getPropertyAsMap(key, DEFAULT_EMPTY_MAP);
        _dbfluteDiconOtherIncludeDefinitionMap = newLinkedHashMap();
        _dbfluteDiconOtherIncludeDefinitionMap.putAll(map);
        return _dbfluteDiconOtherIncludeDefinitionMap;
    }

    public List<String> getDBFluteDiconOtherIncludePathList() {
        return new ArrayList<String>(getDBFluteDiconOtherIncludeDefinitionMap().keySet());
    }

    public boolean isSuppressDiconBehaviorDefinition() { // closet, Java Only
        return isProperty("isSuppressDiconBehaviorDefinition", false); // basically for HotDeploy
    }

    // ===================================================================================
    //                                                             Quill DataSource(Quill)
    //                                                             =======================
    public boolean isQuillDataSourceNameValid() {
        String name = getQuillDataSourceName();
        return name != null && name.trim().length() > 0 && !name.trim().equalsIgnoreCase("null");
    }

    public String getQuillDataSourceName() { // CSharp Only
        return getProperty("quillDataSourceName", null);
    }

    // ===================================================================================
    //                                                                            Lasta Di
    //                                                                            ========
    public String getDBFluteDiXmlNamespace() { // Java Only
        return getProperty("dbfluteDiXmlNamespace", getLanguageFramework().getDBFluteDiXmlNamespace());
    }

    public List<String> getDBFluteDiXmlPackageNameList() { // Java Only
        final String prop = getProperty("dbfluteDiXmlPackageName", null);
        if (prop == null) {
            return new ArrayList<String>();
        }
        final String[] array = prop.split(";");
        final List<String> ls = new ArrayList<String>();
        for (String string : array) {
            ls.add(string.trim());
        }
        return ls;
    }

    public String getDBFluteDiXmlFileName() { // Java Only
        return getProperty("dbfluteDiXmlFileName", getLanguageFramework().getDBFluteDiXmlFileName());
    }

    public String getRdbDiXmlResourceName() { // Java Only
        return getProperty("rdbDiXmlResourceName", getLanguageFramework().getRdbDiXmlResourceName());
    }

    // ===================================================================================
    //                                                                      Component Name
    //                                                                      ==============
    // /= = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
    // These methods for runtime components are used when it needs to identity their components.
    // For example when the DI container is Seasar, These methods are not used
    // because S2Container has name-space in the DI architecture.
    // = = = = = = = = = =/
    public String getDBFluteInitializerComponentName() {
        return filterRuntimeComponentPrefix("introduction");
    }

    public String getInvokerAssistantComponentName() {
        return filterRuntimeComponentPrefix("invokerAssistant");
    }

    public String getCommonColumnAutoSetupperComponentName() {
        return filterRuntimeComponentPrefix("commonColumnAutoSetupper");
    }

    public String getBehaviorSelectorComponentName() {
        return filterRuntimeComponentPrefix("behaviorSelector");
    }

    public String getBehaviorCommandInvokerComponentName() {
        return filterRuntimeComponentPrefix("behaviorCommandInvoker");
    }

    protected String filterRuntimeComponentPrefix(String componentName) {
        final DfBasicProperties basicProp = getBasicProperties();
        String filtered = resolveDIContainerRuntimeComponentNameSpec(componentName);
        filtered = basicProp.filterComponentNameWithAllcommonPrefix(filtered);
        filtered = basicProp.filterComponentNameWithProjectPrefix(filtered);
        return filtered;
    }

    protected String resolveDIContainerRuntimeComponentNameSpec(String componentName) {
        if (getBasicProperties().isTargetContainerSpring()) {
            final String prefix = getDBFluteBeansRuntimeComponentPrefix();
            if (prefix == null || prefix.trim().length() == 0) {
                return componentName; // basically here
            }
            final String filteredPrefix = prefix.substring(0, 1).toLowerCase() + prefix.substring(1);
            return filteredPrefix + componentName.substring(0, 1).toUpperCase() + componentName.substring(1);
        } else if (getBasicProperties().isTargetContainerGuice()) {
            if (isDBFluteModuleGuiceRuntimeComponentByName()) {
                final DfBasicProperties basicProp = getBasicProperties();
                if (basicProp.getProjectPrefix().isEmpty() && basicProp.getAllcommonPrefix().isEmpty()) {
                    final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
                    br.addNotice("Guice byName requires projectPrefix or allcommonPrefix.");
                    br.addItem("Advice");
                    br.addElement("If isDBFluteModuleGuiceRuntimeComponentByName=true,");
                    br.addElement("then use projectPrefix or allcommonPrefix.");
                    br.addElement("");
                    br.addElement("isDBFluteModuleGuiceRuntimeComponentByName is on dependencyInjectionMap.dfprop.");
                    br.addElement("projectPrefix, allcommonPrefix are on basicInfoMap.dfprop.");
                    br.addItem("componentName (for debug)");
                    br.addElement(componentName);
                    final String msg = br.buildExceptionMessage();
                    throw new DfIllegalPropertySettingException(msg);
                }
            }
            // https://github.com/dbflute/dbflute-core/issues/144
            // component name is only supplement identity of type on Guice
            // so component name needs only prefix
            //  e.g. Named("resola") BehaviorSelector resolaBehaviorSelector
            return "";
        } else {
            return componentName;
        }
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    protected DfLanguageFramework getLanguageFramework() {
        return getLanguageDependencyInfo().getLanguageFramework();
    }
}