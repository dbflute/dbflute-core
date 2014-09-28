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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.engine.database.model.UnifiedSchema;
import org.seasar.dbflute.exception.DfIllegalPropertySettingException;
import org.seasar.dbflute.exception.DfIllegalPropertyTypeException;
import org.seasar.dbflute.exception.DfJDBCException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.infra.core.DfEnvironmentType;
import org.seasar.dbflute.infra.core.logic.DfSchemaResourceFinder;
import org.seasar.dbflute.logic.jdbc.connection.DfCurrentSchemaConnector;
import org.seasar.dbflute.properties.facade.DfDatabaseTypeFacadeProp;
import org.seasar.dbflute.properties.handler.DfPropertiesHandler;
import org.seasar.dbflute.properties.propreader.DfOutsideListPropReader;
import org.seasar.dbflute.properties.propreader.DfOutsideMapPropReader;
import org.seasar.dbflute.properties.propreader.DfOutsideStringPropReader;
import org.seasar.dbflute.resource.DBFluteSystem;
import org.seasar.dbflute.task.DfDBFluteTaskStatus;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.DfNameHintUtil;
import org.seasar.dbflute.util.DfPropertyUtil;
import org.seasar.dbflute.util.DfPropertyUtil.PropertyBooleanFormatException;
import org.seasar.dbflute.util.DfPropertyUtil.PropertyIntegerFormatException;
import org.seasar.dbflute.util.DfPropertyUtil.PropertyNotFoundException;
import org.seasar.dbflute.util.DfStringUtil;
import org.seasar.dbflute.util.DfTypeUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public abstract class DfAbstractHelperProperties {

    // ===============================================================================
    //                                                                      Definition
    //                                                                      ==========
    private static final Log _log = LogFactory.getLog(DfAbstractHelperProperties.class);

    // -----------------------------------------------------
    //                                         Default Value
    //                                         -------------
    public static final String JAVA_targetLanguage = "java";
    public static final String CSHARP_targetLanguage = "csharp";
    public static final String PHP_targetLanguage = "php";
    public static final String SCALA_targetLanguage = "scala";
    public static final String CSHARPOLD_targetLanguage = "csharpold";
    public static final String DEFAULT_targetLanguage = JAVA_targetLanguage;

    public static final String DEFAULT_templateFileEncoding = "UTF-8";
    public static final String DEFAULT_sourceFileEncoding = "UTF-8";
    public static final String DEFAULT_projectSchemaXMLEncoding = "UTF-8";

    // -----------------------------------------------------
    //                                   Empty Default Value
    //                                   -------------------
    public static final Map<String, Object> DEFAULT_EMPTY_MAP = DfCollectionUtil.emptyMap();
    public static final List<Object> DEFAULT_EMPTY_LIST = DfCollectionUtil.emptyList();
    public static final String DEFAULT_EMPTY_MAP_STRING = "map:{}";
    public static final String DEFAULT_EMPTY_LIST_STRING = "list:{}";

    // ===============================================================================
    //                                                                       Attribute
    //                                                                       =========
    /** TorqueContextProperties */
    protected Properties _buildProperties;

    // ===============================================================================
    //                                                                     Constructor
    //                                                                     ===========
    /**
     * Constructor.
     * @param prop Build-properties. (NotNull)
     */
    public DfAbstractHelperProperties(Properties prop) {
        if (prop == null) {
            String msg = "Look! Read the message below." + ln();
            msg = msg + "/- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -" + ln();
            msg = msg + "The build-properties is required!" + ln();
            msg = msg + ln();
            msg = msg + "[Advice]" + ln();
            msg = msg + "Check your environment of DBFlute client and module!" + ln();
            msg = msg + "And set up from first again after confirmation of correct procedure." + ln();
            msg = msg + ln();
            msg = msg + "[Properties]" + ln() + null + ln();
            msg = msg + "- - - - - - - - - -/";
            throw new IllegalStateException(msg);
        }
        _buildProperties = prop;
    }

    // ===============================================================================
    //                                                                        Accessor
    //                                                                        ========
    protected Properties getProperties() {
        return _buildProperties;
    }

    // ===============================================================================
    //                                                                      Properties
    //                                                                      ==========
    // -----------------------------------------------------
    //                                              Accessor
    //                                              --------
    public String getProperty(String key, String defaultValue, Map<String, ? extends Object> map) {
        final Object obj = map.get(key);
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
        return defaultValue;
    }

    public String getPropertyIfNotBuildProp(String key, String defaultValue, Map<String, ? extends Object> map) {
        final Object obj = map.get(key);
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

    public boolean isProperty(String key, boolean defaultValue, Map<String, ? extends Object> map) {
        Object obj = map.get(key);
        if (obj == null) {
            final String anotherKey = deriveBooleanAnotherKey(key);
            if (anotherKey != null) {
                obj = map.get(anotherKey);
            }
        }
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
        return defaultValue;
    }

    public boolean isPropertyIfNotExistsFromBuildProp(String key, boolean defaultValue,
            Map<String, ? extends Object> map) {
        Object obj = map.get(key);
        if (obj == null) {
            final String anotherKey = deriveBooleanAnotherKey(key);
            if (anotherKey != null) {
                obj = map.get(anotherKey);
            }
        }
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

    static String deriveBooleanAnotherKey(String key) {
        return DfPropertyUtil.deriveBooleanAnotherKey(key);
    }

    // -----------------------------------------------------
    //                                                String
    //                                                ------
    /**
     * Get property as string. {Delegate method}
     * @param key Property-key. (NotNull)
     * @return Property as string. (NotNull)
     */
    final protected String stringProp(String key) {
        final String outsidePropString = getOutsideStringProp(key);
        if (outsidePropString != null && outsidePropString.trim().length() > 0) {
            return outsidePropString;
        }
        return DfPropertyUtil.stringProp(_buildProperties, key);
    }

    /**
     * Get property as string. {Delegate method}
     * @param key Property-key. (NotNull)
     * @param defaultValue Default value. (NullAllowed)
     * @return Property as string. (NullAllowed: If the default-value is null)
     */
    final protected String stringProp(String key, String defaultValue) {
        try {
            final String outsidePropString = getOutsideStringProp(key);
            if (outsidePropString != null && outsidePropString.trim().length() > 0) {
                return outsidePropString;
            }
            return DfPropertyUtil.stringProp(_buildProperties, key);
        } catch (PropertyNotFoundException e) {
            return defaultValue;
        }
    }

    /**
     * Get property as string. {Delegate method}
     * @param key Property-key. (NotNull)
     * @param defaultValue Default value. (NullAllowed)
     * @return Property as string. (NullAllowed: If the default-value is null)
     */
    final protected String stringPropNoEmpty(String key, String defaultValue) {
        try {
            final String outsidePropString = getOutsideStringProp(key);
            if (outsidePropString != null && outsidePropString.trim().length() > 0) {
                return outsidePropString;
            }
            final String value = DfPropertyUtil.stringProp(_buildProperties, key);
            if (value != null && value.trim().length() != 0) {
                return value;
            }
            return defaultValue;
        } catch (PropertyNotFoundException e) {
            return defaultValue;
        }
    }

    // -----------------------------------------------------
    //                                               Boolean
    //                                               -------
    /**
     * Get property as boolean. {Delegate method}
     * @param key Property-key. (NotNull)
     * @return Property as boolean.
     */
    final protected boolean booleanProp(String key) {
        return DfPropertyUtil.booleanProp(_buildProperties, key);
    }

    /**
     * Get property as boolean. {Delegate method}
     * @param key Property-key. (NotNull)
     * @param defaultValue Default value.
     * @return Property as boolean.
     */
    final protected boolean booleanProp(String key, boolean defaultValue) {
        try {
            return DfPropertyUtil.booleanProp(_buildProperties, key);
        } catch (PropertyNotFoundException e) {
            return defaultValue;
        } catch (PropertyBooleanFormatException e) {
            return defaultValue;
        }
    }

    // -----------------------------------------------------
    //                                               Integer
    //                                               -------
    /**
     * Get property as integer. {Delegate method}
     * @param key Property-key. (NotNull)
     * @return Property as integer.
     */
    final protected int intProp(String key) {
        return DfPropertyUtil.intProp(_buildProperties, key);
    }

    /**
     * Get property as integer. {Delegate method}
     * @param key Property-key. (NotNull)
     * @param defaultValue Default value.
     * @return Property as integer.
     */
    final protected int intProp(String key, int defaultValue) {
        try {
            return DfPropertyUtil.intProp(_buildProperties, key);
        } catch (PropertyNotFoundException e) {
            return defaultValue;
        } catch (PropertyIntegerFormatException e) {
            return defaultValue;
        }
    }

    // -----------------------------------------------------
    //                                                  List
    //                                                  ----
    /**
     * Get property as list. {Delegate method}
     * @param key Property-key. (NotNull)
     * @return Property as list. (NotNull)
     */
    final protected List<Object> listProp(String key) {
        final List<Object> outsidePropList = getOutsideListProp(key);
        if (!outsidePropList.isEmpty()) {
            return outsidePropList;
        }
        return DfPropertyUtil.listProp(_buildProperties, key, ";");
    }

    /**
     * Get property as list. {Delegate method}
     * @param key Property-key. (NotNull)
     * @param defaultValue Default value. (NullAllowed)
     * @return Property as list. (NullAllowed: If the default-value is null)
     */
    final protected List<Object> listProp(String key, List<Object> defaultValue) {
        try {
            final List<Object> outsidePropList = getOutsideListProp(key);
            if (!outsidePropList.isEmpty()) {
                return outsidePropList;
            }
            final List<Object> result = DfPropertyUtil.listProp(_buildProperties, key, ";");
            if (result.isEmpty()) {
                return defaultValue;
            } else {
                return result;
            }
        } catch (PropertyNotFoundException e) {
            return defaultValue;
        }
    }

    // -----------------------------------------------------
    //                                                   Map
    //                                                   ---
    /**
     * Get property as map. {Delegate method}
     * @param key Property-key. (NotNull)
     * @return Property as map. (NotNull)
     */
    final protected Map<String, Object> mapProp(String key) {
        final Map<String, Object> outsidePropMap = getOutsideMapProp(key);
        if (!outsidePropMap.isEmpty()) {
            return outsidePropMap;
        }
        return DfPropertyUtil.mapProp(_buildProperties, key, ";");
    }

    /**
     * Get property as map. {Delegate method}
     * @param key Property-key. (NotNull)
     * @param defaultValue Default value. (NullAllowed)
     * @return Property as map. (NullAllowed: If the default-value is null)
     */
    final protected Map<String, Object> mapProp(String key, Map<String, Object> defaultValue) {
        try {
            final Map<String, Object> outsidePropMap = getOutsideMapProp(key);
            if (!outsidePropMap.isEmpty()) {
                return outsidePropMap;
            }
            final Map<String, Object> result = DfPropertyUtil.mapProp(_buildProperties, key, ";");
            if (result.isEmpty()) {
                return defaultValue;
            } else {
                return result;
            }
        } catch (PropertyNotFoundException e) {
            return defaultValue;
        }
    }

    // ===============================================================================
    //                                                              Outside Properties
    //                                                              ==================
    protected String getOutsideStringProp(String key) {
        final DfOutsideStringPropReader reader = createOutsideStringPropReader();
        final String propName = DfStringUtil.replace(key, "torque.", "");
        final String path = "./dfprop/" + propName + ".dfprop";
        return reader.readString(path, getEnvironmentType());
    }

    protected DfOutsideStringPropReader createOutsideStringPropReader() {
        return new DfOutsideStringPropReader();
    }

    protected Map<String, Object> getOutsideMapProp(String key) {
        final DfOutsideMapPropReader reader = createOutsideMapPropReader();
        final String propName = DfStringUtil.replace(key, "torque.", "");
        final String path = "./dfprop/" + propName + ".dfprop";
        return reader.readMap(path, getEnvironmentType());
    }

    protected DfOutsideMapPropReader createOutsideMapPropReader() {
        return new DfOutsideMapPropReader();
    }

    protected List<Object> getOutsideListProp(String key) {
        final DfOutsideListPropReader reader = createOutsideListPropReader();
        final String propName = DfStringUtil.replace(key, "torque.", "");
        final String path = "./dfprop/" + propName + ".dfprop";
        return reader.readList(path, getEnvironmentType());
    }

    protected DfOutsideListPropReader createOutsideListPropReader() {
        return new DfOutsideListPropReader();
    }

    // ===============================================================================
    //                                                               Dispatch Variable
    //                                                               =================
    protected String resolveDispatchVariable(final String propTitle, String plainValue) {
        return doResolveDispatchVariable(plainValue, new DfDispatchVariableCallback() {
            public void throwNotFoundException(String plainValue, File dispatchFile) {
                throwDispatchFileNotFoundException(propTitle, plainValue, dispatchFile);
            }
        });
    }

    protected void throwDispatchFileNotFoundException(String propTitle, String plainValue, File dispatchFile) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The dispatch file was not found.");
        br.addItem("Advice");
        br.addElement("Check your dispatch file existing.");
        br.addElement("And check the setting in DBFlute property.");
        br.addItem("Property");
        br.addElement(propTitle);
        br.addItem("Dispatch Setting");
        br.addElement(plainValue);
        br.addItem("Dispatch File");
        br.addElement(dispatchFile);
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg);
    }

    protected String resolvePasswordVariable(final String propTitle, final String user, String password) {
        final String resolved = doResolveDispatchVariable(password, new DfDispatchVariableCallback() {
            public void throwNotFoundException(String plainValue, File dispatchFile) {
                throwDatabaseUserPasswordFileNotFoundException(propTitle, user, plainValue, dispatchFile);
            }
        });
        return resolved != null ? resolved : ""; // password not allowed to be null
    }

    protected void throwDatabaseUserPasswordFileNotFoundException(String propTitle, String user, String password,
            File pwdFile) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The password file for the user was not found.");
        br.addItem("Advice");
        br.addElement("Check your password file existing.");
        br.addElement("And check the setting in DBFlute property.");
        br.addItem("Property");
        br.addElement(propTitle);
        br.addItem("Database User");
        br.addElement(user);
        br.addItem("Password Setting");
        br.addElement(password);
        br.addItem("Password File");
        br.addElement(pwdFile);
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg);
    }

    protected String doResolveDispatchVariable(String plainValue, DfDispatchVariableCallback callback) {
        if (Srl.is_Null_or_TrimmedEmpty(plainValue)) {
            return plainValue;
        }
        final DfDispatchVariableInfo variableInfo = analyzeDispatchVariable(plainValue);
        if (variableInfo == null) {
            return plainValue;
        }
        final File dispatchFile = variableInfo.getDispatchFile();
        final String defaultValue = variableInfo.getDefaultValue();
        if (!dispatchFile.exists()) {
            if (defaultValue == null) {
                callback.throwNotFoundException(plainValue, dispatchFile);
            }
            return defaultValue; // no dispatch file
        }
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(dispatchFile), "UTF-8"));
            final String line = br.readLine();
            return line; // first line in the dispatch file is value
        } catch (Exception continued) {
            _log.info("Failed to read the dispatch file: " + dispatchFile);
            return defaultValue; // no password
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    protected static interface DfDispatchVariableCallback {
        void throwNotFoundException(String plainValue, File dispatchFile);
    }

    protected DfDispatchVariableInfo analyzeDispatchVariable(String password) {
        final String prefix = "df:dfprop/";
        if (!password.startsWith(prefix)) {
            return null;
        }
        final String fileName;
        final String defaultValue;
        {
            final String content = Srl.substringFirstRear(password, prefix);
            if (content.contains("|")) {
                fileName = Srl.substringFirstFront(content, "|");
                defaultValue = Srl.substringFirstRear(content, "|");
            } else {
                fileName = content;
                defaultValue = null;
            }
        }
        final File dispatchFile = new File("./dfprop/" + fileName);
        final DfDispatchVariableInfo variableInfo = new DfDispatchVariableInfo();
        variableInfo.setDispatchFile(dispatchFile);
        variableInfo.setDefaultValue(defaultValue);
        return variableInfo;
    }

    protected static class DfDispatchVariableInfo {
        protected File _dispatchFile;
        protected String _defaultValue;

        public File getDispatchFile() {
            return _dispatchFile;
        }

        public void setDispatchFile(File dispatchFile) {
            this._dispatchFile = dispatchFile;
        }

        public String getDefaultValue() {
            return _defaultValue;
        }

        public void setDefaultValue(String defaultValue) {
            this._defaultValue = defaultValue;
        }
    }

    // ===================================================================================
    //                                                                        Split DfProp
    //                                                                        ============
    protected Map<String, Object> resolveSplit(String mapName, Map<String, Object> plainDefinitionMap) {
        Map<String, Object> splitDefinitionMap = null;
        for (Entry<String, Object> entry : plainDefinitionMap.entrySet()) {
            final String classificationName = entry.getKey();
            if (isSplitMark(classificationName)) {
                @SuppressWarnings("unchecked")
                final Map<String, Object> splitMap = (Map<String, Object>) entry.getValue();
                splitDefinitionMap = handleSplitDefinition(mapName, classificationName, splitMap);
            }
        }
        if (splitDefinitionMap == null) {
            return plainDefinitionMap;
        }
        final Map<String, Object> resolvedMap = new LinkedHashMap<String, Object>();
        for (Entry<String, Object> entry : plainDefinitionMap.entrySet()) {
            final String classificationName = entry.getKey();
            if (isSplitMark(classificationName)) {
                continue;
            }
            resolvedMap.put(classificationName, entry.getValue());
        }
        if (splitDefinitionMap != null) {
            for (Entry<String, Object> entry : splitDefinitionMap.entrySet()) {
                final String elementName = entry.getKey();
                if (resolvedMap.containsKey(elementName)) {
                    throwDfPropDefinitionDuplicateDefinitionException(mapName, elementName, null);
                }
                resolvedMap.put(elementName, entry.getValue());
            }
        }
        return resolvedMap;
    }

    protected boolean isSplitMark(String classificationName) {
        return classificationName.equalsIgnoreCase("$$split$$");
    }

    protected Map<String, Object> handleSplitDefinition(String mapName, String classificationName, Map<?, ?> splitMap) {
        final Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
        @SuppressWarnings("unchecked")
        final Set<String> keywordSet = (Set<String>) splitMap.keySet();
        for (String keyword : keywordSet) {
            final Map<String, Object> splitProp = getOutsideMapProp(mapName + "_" + keyword);
            if (splitProp.isEmpty()) {
                throwClassificationSplitDefinitionNotFoundException(mapName, keyword);
            }
            for (Entry<String, Object> entry : splitProp.entrySet()) {
                final String elementName = entry.getKey();
                if (resultMap.containsKey(elementName)) {
                    throwDfPropDefinitionDuplicateDefinitionException(mapName, elementName, keyword);
                }
                resultMap.put(elementName, entry.getValue());
            }
        }
        return resultMap;
    }

    protected void throwClassificationSplitDefinitionNotFoundException(String mapName, String keyword) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the split definition of DBFlute property.");
        br.addItem("Advice");
        br.addElement("Make sure the file name of your split dfprop");
        br.addElement("or define at least one definition in the split file.");
        br.addElement("");
        br.addElement("For example:");
        br.addElement("    $$split$$ = map:{");
        br.addElement("        ; land = dummy");
        br.addElement("        ; sea  = dummy");
        br.addElement("    }");
        br.addElement("");
        br.addElement("The following files should exist:");
        br.addElement("    dfprop/" + mapName + "_land.dfprop");
        br.addElement("    dfprop/" + mapName + "_sea.dfprop");
        br.addItem("DBFlute Property");
        br.addElement(mapName);
        br.addItem("Split Keyword");
        br.addElement(keyword);
        final String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg);
    }

    protected void throwDfPropDefinitionDuplicateDefinitionException(String mapName, String elementName, String keyword) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Found the duplicate definition.");
        br.addItem("Advice");
        br.addElement("The element names should be unique.");
        br.addElement("(in all split files if split)");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    Sea = map:{");
        br.addElement("        ; ...");
        br.addElement("    }");
        br.addElement("    Sea = map:{");
        br.addElement("        ; ...");
        br.addElement("    }");
        br.addElement("  (o):");
        br.addElement("    Land = map:{");
        br.addElement("        ; ...");
        br.addElement("    }");
        br.addElement("    Sea = map:{");
        br.addElement("        ; ...");
        br.addElement("    }");
        br.addItem("DBFlute Property");
        br.addElement(mapName);
        if (keyword != null) {
            br.addItem("Duplicate Found Location");
            br.addElement(keyword);
        }
        br.addItem("Duplicate Name");
        br.addElement(elementName);
        final String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg);
    }

    // ===============================================================================
    //                                                            Other Property Entry
    //                                                            ====================
    public DfPropertiesHandler handler() {
        return DfPropertiesHandler.getInstance();
    }

    protected DfBasicProperties getBasicProperties() {
        return handler().getBasicProperties(getProperties());
    }

    protected DfDatabaseProperties getDatabaseProperties() {
        return handler().getDatabaseProperties(getProperties());
    }

    protected DfDocumentProperties getDocumentProperties() {
        return handler().getDocumentProperties(getProperties());
    }

    protected DfAdditionalForeignKeyProperties getAdditionalForeignKeyProperties() {
        return handler().getAdditionalForeignKeyProperties(getProperties());
    }

    protected DfClassificationProperties getClassificationProperties() {
        return handler().getClassificationProperties(getProperties());
    }

    protected DfCommonColumnProperties getCommonColumnProperties() {
        return handler().getCommonColumnProperties(getProperties());
    }

    protected DfLittleAdjustmentProperties getLittleAdjustmentProperties() {
        return handler().getLittleAdjustmentProperties(getProperties());
    }

    protected DfReplaceSchemaProperties getReplaceSchemaProperties() {
        return handler().getReplaceSchemaProperties(getProperties());
    }

    // ===============================================================================
    //                                                                  Target by Hint 
    //                                                                  ==============
    protected boolean isTargetByHint(String name, List<String> targetList, List<String> exceptList) {
        return DfNameHintUtil.isTargetByHint(name, targetList, exceptList);
    }

    protected boolean isHitByTheHint(final String name, final String hint) {
        return DfNameHintUtil.isHitByTheHint(name, hint);
    }

    // ===============================================================================
    //                                                                Environment Type
    //                                                                ================
    protected final boolean isSpecifiedEnvironmentType() {
        return DfEnvironmentType.getInstance().isSpecifiedType();
    }

    /**
     * @return The type of environment. (NullAllowed: if null, means non-specified type)
     */
    protected final String getEnvironmentType() {
        return DfEnvironmentType.getInstance().getEnvironmentType();
    }

    /**
     * @return The type of environment. (NotNull: if no specified environment type, returns default control mark)
     */
    protected final String getEnvironmentTypeMightBeDefault() {
        return DfEnvironmentType.getInstance().getEnvironmentTypeMightBeDefault();
    }

    // ===============================================================================
    //                                                                 Schema Resource
    //                                                                 ===============
    protected List<File> findSchemaResourceFileList(String targetDir, String prefix, String... suffixes) {
        final DfSchemaResourceFinder finder = new DfSchemaResourceFinder();
        finder.addPrefix(prefix);
        for (String suffix : suffixes) {
            finder.addSuffix(suffix);
        }
        return finder.findResourceFileList(targetDir);
    }

    // ===============================================================================
    //                                                                     Task Status
    //                                                                     ===========
    protected boolean isDocOnlyTask() {
        final DfDBFluteTaskStatus instance = DfDBFluteTaskStatus.getInstance();
        return instance.isDocTask() || instance.isReplaceSchema();
    }

    // ===============================================================================
    //                                                               Connection Helper
    //                                                               =================
    protected Connection createConnection(String driver, String url, UnifiedSchema unifiedSchema, Properties info) {
        setupConnectionDriver(driver);
        try {
            final Connection conn = DriverManager.getConnection(url, info);
            setupConnectionVariousSetting(unifiedSchema, conn);
            return conn;
        } catch (SQLException e) {
            String msg = "Failed to connect: url=" + url + " info=" + info;
            throw new IllegalStateException(msg, e);
        }
    }

    private void setupConnectionDriver(String driver) {
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void setupConnectionVariousSetting(UnifiedSchema unifiedSchema, Connection conn) throws SQLException {
        conn.setAutoCommit(true);
        if (unifiedSchema.existsPureSchema()) {
            final DfDatabaseTypeFacadeProp facadeProp = getBasicProperties().getDatabaseTypeFacadeProp();
            final DfCurrentSchemaConnector connector = new DfCurrentSchemaConnector(unifiedSchema, facadeProp);
            connector.connectSchema(conn);
        }
    }

    protected String getConnectedCatalog(String driver, String url, String user, String password) throws SQLException {
        setupConnectionDriver(driver);
        try {
            final Connection conn = DriverManager.getConnection(url, user, password);
            return conn.getCatalog();
        } catch (SQLException e) {
            String msg = "Failed to connect: url=" + url + " user=" + user;
            throw new DfJDBCException(msg, e);
        }
    }

    // ===============================================================================
    //                                                                     Cast Helper
    //                                                                     ===========
    protected String castToString(Object obj, String property) {
        if (!(obj instanceof String)) {
            String msg = "The type of the property '" + property + "' should be String:";
            msg = msg + " obj=" + obj + " type=" + (obj != null ? obj.getClass() : null);
            throw new DfIllegalPropertyTypeException(msg);
        }
        return (String) obj;
    }

    @SuppressWarnings("unchecked")
    protected <ELEMENT> List<ELEMENT> castToList(Object obj, String property) {
        if (!(obj instanceof List<?>)) {
            String msg = "The type of the property '" + property + "' should be List:";
            msg = msg + " obj=" + obj + " type=" + (obj != null ? obj.getClass() : null);
            throw new DfIllegalPropertyTypeException(msg);
        }
        return (List<ELEMENT>) obj;
    }

    @SuppressWarnings("unchecked")
    protected <KEY, VALUE> Map<KEY, VALUE> castToMap(Object obj, String property) {
        if (!(obj instanceof Map<?, ?>)) {
            String msg = "The type of the property '" + property + "' should be Map:";
            msg = msg + " obj=" + obj + " type=" + (obj != null ? obj.getClass() : null);
            throw new DfIllegalPropertyTypeException(msg);
        }
        return (Map<KEY, VALUE>) obj;
    }

    // ===============================================================================
    //                                                                  General Helper
    //                                                                  ==============
    protected String ln() {
        return DBFluteSystem.getBasicLn();
    }

    protected <KEY, VALUE> LinkedHashMap<KEY, VALUE> newLinkedHashMap() {
        return new LinkedHashMap<KEY, VALUE>();
    }

    protected <ELEMENT> LinkedHashSet<ELEMENT> newLinkedHashSet() {
        return new LinkedHashSet<ELEMENT>();
    }

    protected String filterDoubleQuotation(String str) {
        return DfPropertyUtil.convertAll(str, "\"", "'");
    }

    protected String removeLineSeparator(String str) {
        str = removeCR(str);
        str = removeLF(str);
        return str;
    }

    protected String removeLF(String str) {
        return str.replaceAll("\n", "");
    }

    protected String removeCR(String str) {
        return str.replaceAll("\r", "");
    }

    protected boolean processBooleanString(String value) {
        return value != null && value.trim().equalsIgnoreCase("true");
    }
}