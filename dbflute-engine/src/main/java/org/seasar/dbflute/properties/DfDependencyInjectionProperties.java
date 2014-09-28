/*
 * Copyright 2004-2014 the Seasar Foundation and the Others.
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
package org.seasar.dbflute.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.seasar.dbflute.logic.generate.language.DfLanguageDependency;
import org.seasar.dbflute.logic.generate.language.framework.DfLanguageFramework;
import org.seasar.dbflute.util.DfTypeUtil;

/**
 * @author jflute
 * @since 0.8.8.1 (2009/01/07 Wednesday)
 */
public final class DfDependencyInjectionProperties extends DfAbstractHelperProperties {

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
    //                                                                       Dicon(Seasar)
    //                                                                       =============
    public String getDBFluteDiconNamespace() { // Java Only
        return getProperty("dbfluteDiconNamespace", getDefaultDBFluteDicon().getDBFluteDiconNamespace());
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
        return getProperty("dbfluteDiconFileName", getDefaultDBFluteDicon().getDBFluteDiconFileName());
    }

    public String getDBFluteCreatorDiconFileName() { // It's closet! Java Only
        return getProperty("dbfluteCreatorDiconFileName", "dbflute-creator.dicon");
    }

    public String getDBFluteCustomizerDiconFileName() { // It's closet! Java Only
        return getProperty("dbfluteCustomizerDiconFileName", "dbflute-customizer.dicon");
    }

    public String getJ2eeDiconResourceName() { // Java Only
        return getProperty("j2eeDiconResourceName", getDefaultDBFluteDicon().getJ2eeDiconResourceName());
    }

    protected DfLanguageDependency getLanguageDependencyInfo() {
        return getBasicProperties().getLanguageDependency();
    }

    protected DfLanguageFramework getDefaultDBFluteDicon() {
        return getLanguageDependencyInfo().getLanguageFramework();
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
}