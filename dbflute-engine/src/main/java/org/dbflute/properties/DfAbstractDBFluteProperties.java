/*
 * Copyright 2014-2025 the original author or authors.
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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.torque.engine.database.model.UnifiedSchema;
import org.dbflute.exception.DfIllegalPropertyTypeException;
import org.dbflute.exception.DfJDBCException;
import org.dbflute.infra.core.DfEnvironmentType;
import org.dbflute.infra.core.logic.DfSchemaResourceFinder;
import org.dbflute.logic.jdbc.connection.DfCurrentSchemaConnector;
import org.dbflute.properties.assistant.base.DfPropertyValueHandler;
import org.dbflute.properties.assistant.base.DfSplittingDfpropHandler;
import org.dbflute.properties.assistant.base.dispatch.DfDispatchVariableResolver;
import org.dbflute.properties.assistant.base.dispatch.DfOutsideFileVariableInfo;
import org.dbflute.properties.core.DfPropertiesHandler;
import org.dbflute.properties.facade.DfDatabaseTypeFacadeProp;
import org.dbflute.system.DBFluteSystem;
import org.dbflute.task.DfDBFluteTaskStatus;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.DfNameHintUtil;
import org.dbflute.util.DfPropertyUtil;

/**
 * @author jflute
 */
public abstract class DfAbstractDBFluteProperties {

    // ===============================================================================
    //                                                                      Definition
    //                                                                      ==========
    // -----------------------------------------------------
    //                                         Default Value
    //                                         -------------
    protected static final String JAVA_targetLanguage = "java";
    protected static final String CSHARP_targetLanguage = "csharp";
    protected static final String PHP_targetLanguage = "php";
    protected static final String SCALA_targetLanguage = "scala";
    protected static final String CSHARPOLD_targetLanguage = "csharpold";
    protected static final String DEFAULT_targetLanguage = JAVA_targetLanguage;

    protected static final String DEFAULT_templateFileEncoding = "UTF-8";
    protected static final String DEFAULT_sourceFileEncoding = "UTF-8";
    protected static final String DEFAULT_projectSchemaXMLEncoding = "UTF-8";

    // -----------------------------------------------------
    //                                   Empty Default Value
    //                                   -------------------
    protected static final Map<String, Object> DEFAULT_EMPTY_MAP = DfCollectionUtil.emptyMap();
    protected static final List<Object> DEFAULT_EMPTY_LIST = DfCollectionUtil.emptyList();
    protected static final String DEFAULT_EMPTY_MAP_STRING = "map:{}";

    // ===============================================================================
    //                                                                       Attribute
    //                                                                       =========
    protected final Properties _nativeProperties; // for build.properties
    protected final DfPropertyValueHandler _propertyValueHandler; // has e.g. isProeprty()
    protected final DfSplittingDfpropHandler _splittingDfpropHandler; // has e.g. resolveSplit()

    // ===============================================================================
    //                                                                     Constructor
    //                                                                     ===========
    /**
     * @param nativeProperties The properties for build.properties (means native). (NotNull)
     */
    public DfAbstractDBFluteProperties(Properties nativeProperties) {
        if (nativeProperties == null) {
            throw new IllegalArgumentException("The argument 'prop' should not be null.");
        }
        _nativeProperties = nativeProperties;
        _propertyValueHandler = new DfPropertyValueHandler(nativeProperties);
        _splittingDfpropHandler = new DfSplittingDfpropHandler(_propertyValueHandler);
    }

    // ===================================================================================
    //                                                                   TopLevel Property
    //                                                                   =================
    /**
     * Get property as string. {Delegate method}
     * @param key Property-key. (NotNull)
     * @return Property as string. (NotNull, EmptyAllowed: if not found)
     */
    protected String stringProp(String key) {
        return _propertyValueHandler.stringProp(key);
    }

    /**
     * Get property as string. {Delegate method}
     * @param key Property-key. (NotNull)
     * @param defaultValue Default value. (NullAllowed)
     * @return Property as string. (NullAllowed: If the default-value is null)
     */
    protected String stringProp(String key, String defaultValue) {
        return _propertyValueHandler.stringProp(key, defaultValue);
    }

    // -----------------------------------------------------
    //                                               Boolean
    //                                               -------
    /**
     * Get property as boolean. {Delegate method}
     * @param key Property-key. (NotNull)
     * @param defaultValue Default value.
     * @return Property as boolean.
     */
    protected boolean booleanProp(String key, boolean defaultValue) {
        return _propertyValueHandler.booleanProp(key, defaultValue);
    }

    // -----------------------------------------------------
    //                                                  List
    //                                                  ----
    /**
     * Get property as list. {Delegate method}
     * @param key Property-key. (NotNull)
     * @param defaultValue Default value. (NullAllowed)
     * @return Property as list. (NullAllowed: If the default-value is null)
     */
    protected List<Object> listProp(String key, List<Object> defaultValue) {
        return _propertyValueHandler.listProp(key, defaultValue);
    }

    // -----------------------------------------------------
    //                                                   Map
    //                                                   ---
    /**
     * Get property as map. {Delegate method}
     * @param key Property-key. (NotNull)
     * @param defaultValue Default value. (NullAllowed)
     * @return Property as map. (NullAllowed: If the default-value is null)
     */
    protected Map<String, Object> mapProp(String key, Map<String, Object> defaultValue) {
        return _propertyValueHandler.mapProp(key, defaultValue);
    }

    // ===================================================================================
    //                                                                  Outside Properties
    //                                                                  ==================
    protected Map<String, Object> getOutsideMapProp(String key) {
        return _propertyValueHandler.getOutsideMapProp(key);
    }

    // ===================================================================================
    //                                                                    Flexible Handler
    //                                                                    ================
    public String getProperty(String key, String defaultValue, Map<String, ? extends Object> map) {
        return _propertyValueHandler.getProperty(key, defaultValue, map);
    }

    public boolean isProperty(String key, boolean defaultValue, Map<String, ? extends Object> map) {
        return _propertyValueHandler.isProperty(key, defaultValue, map);
    }

    public boolean isPropertyIfNotExistsFromBuildProp(String key, boolean defaultValue, Map<String, ? extends Object> map) {
        return _propertyValueHandler.isPropertyIfNotExistsFromBuildProp(key, defaultValue, map);
    }

    // ===================================================================================
    //                                                                   Dispatch Variable
    //                                                                   =================
    protected String resolveDispatchVariable(String propTitle, String plainValue) {
        final DfDispatchVariableResolver resolver = createDispatchVariableResolver();
        return resolver.resolveDispatchVariable(propTitle, plainValue);
    }

    protected String resolvePasswordVariable(String propTitle, String user, String password) {
        final DfDispatchVariableResolver resolver = createDispatchVariableResolver();
        return resolver.resolvePasswordVariable(propTitle, user, password);
    }

    public DfOutsideFileVariableInfo analyzeOutsideFileVariable(String plainValue) {
        final DfDispatchVariableResolver resolver = createDispatchVariableResolver();
        return resolver.analyzeOutsideFileVariable(plainValue);
    }

    protected DfDispatchVariableResolver createDispatchVariableResolver() {
        return new DfDispatchVariableResolver();
    }

    // ===================================================================================
    //                                                                        Split DfProp
    //                                                                        ============
    protected Map<String, Object> resolveSplit(String mapName, Map<String, Object> plainDefinitionMap) {
        return _splittingDfpropHandler.resolveSplit(mapName, plainDefinitionMap);
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    public DfPropertiesHandler handler() {
        return DfPropertiesHandler.getInstance();
    }

    protected DfBasicProperties getBasicProperties() {
        return handler().getBasicProperties(getNativeProperties());
    }

    protected DfDatabaseProperties getDatabaseProperties() {
        return handler().getDatabaseProperties(getNativeProperties());
    }

    protected DfDocumentProperties getDocumentProperties() {
        return handler().getDocumentProperties(getNativeProperties());
    }

    protected DfAdditionalForeignKeyProperties getAdditionalForeignKeyProperties() {
        return handler().getAdditionalForeignKeyProperties(getNativeProperties());
    }

    protected DfClassificationProperties getClassificationProperties() {
        return handler().getClassificationProperties(getNativeProperties());
    }

    protected DfCommonColumnProperties getCommonColumnProperties() {
        return handler().getCommonColumnProperties(getNativeProperties());
    }

    protected DfLittleAdjustmentProperties getLittleAdjustmentProperties() {
        return handler().getLittleAdjustmentProperties(getNativeProperties());
    }

    protected DfReplaceSchemaProperties getReplaceSchemaProperties() {
        return handler().getReplaceSchemaProperties(getNativeProperties());
    }

    protected DfESFluteProperties getESFluteProperties() {
        return handler().getESFluteProperties(getNativeProperties());
    }

    protected DfLastaFluteProperties getLastaFluteProperties() {
        return handler().getLastaFluteProperties(getNativeProperties());
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
        return DBFluteSystem.ln();
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
        return removeLF(removeCR(str));
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

    // ===============================================================================
    //                                                                        Accessor
    //                                                                        ========
    protected Properties getNativeProperties() {
        return _nativeProperties;
    }
}