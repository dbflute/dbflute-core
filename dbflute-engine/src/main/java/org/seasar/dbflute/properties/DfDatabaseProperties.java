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

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
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
import org.seasar.dbflute.exception.DfRequiredPropertyNotFoundException;
import org.seasar.dbflute.helper.StringKeyMap;
import org.seasar.dbflute.logic.jdbc.urlanalyzer.DfUrlAnalyzer;
import org.seasar.dbflute.logic.jdbc.urlanalyzer.factory.DfUrlAnalyzerFactory;
import org.seasar.dbflute.properties.assistant.DfAdditionalSchemaInfo;
import org.seasar.dbflute.properties.assistant.DfConnectionProperties;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.DfTypeUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public final class DfDatabaseProperties extends DfAbstractHelperProperties {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log _log = LogFactory.getLog(DfDatabaseProperties.class);
    public static final String NO_NAME_SCHEMA = "$$NoNameSchema$$"; // basically for MySQL

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DatabaseInfo _databaseInfo = new DatabaseInfo();

    // cache
    protected String _cacheDriver;
    protected String _cacheUrl;
    protected String _cacheMainCatalog;
    protected boolean _catalogCacheDone;
    protected UnifiedSchema _cacheMainSchema;
    protected String _cacheUser;
    protected String _cachePassword;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * Constructor.
     * @param prop Properties. (NotNull)
     */
    public DfDatabaseProperties(Properties prop) {
        super(prop);
    }

    // ===================================================================================
    //                                                                     Connection Info
    //                                                                     ===============
    // -----------------------------------------------------
    //                                          Driver & URL
    //                                          ------------
    public String getDatabaseDriver() {
        if (_cacheDriver != null) {
            return _cacheDriver;
        }
        _cacheDriver = _databaseInfo.getDatabaseDriver();
        return _cacheDriver;
    }

    public String getDatabaseUrl() {
        if (_cacheUrl != null) {
            return _cacheUrl;
        }
        final String propTitle = "databaseInfoMap#url";
        _cacheUrl = resolveDispatchVariable(propTitle, _databaseInfo.getDatabaseUrl());
        return _cacheUrl;
    }

    // -----------------------------------------------------
    //                                               Catalog
    //                                               -------
    public String getDatabaseCatalog() { // as main catalog (closet)
        if (_catalogCacheDone) {
            return _cacheMainCatalog;
        }
        _catalogCacheDone = true;
        final String propTitle = "databaseInfoMap#catalog";
        final String catalog = resolveDispatchVariable(propTitle, _databaseInfo.getDatabaseCatalog());
        _cacheMainCatalog = prepareMainCatalog(catalog, getDatabaseUrl());
        return _cacheMainCatalog;
    }

    public String prepareMainCatalog(String catalog, String url) {
        if (Srl.is_Null_or_TrimmedEmpty(catalog)) {
            catalog = extractCatalogFromUrl(url); // second way
        }
        return filterDatabaseCatalog(catalog);
    }

    public String extractCatalogFromUrl(String url) {
        final DfUrlAnalyzerFactory factory = new DfUrlAnalyzerFactory(getBasicProperties(), url);
        final DfUrlAnalyzer analyzer = factory.createAnalyzer();
        final String extracted = analyzer.extractCatalog();
        return Srl.is_NotNull_and_NotTrimmedEmpty(extracted) ? extracted : null;
    }

    protected String filterDatabaseCatalog(String catalog) {
        if (isDatabaseH2()) {
            if (Srl.is_NotNull_and_NotTrimmedEmpty(catalog)) {
                catalog = catalog.toUpperCase();
            }
        }
        return catalog;
    }

    // -----------------------------------------------------
    //                                                Schema
    //                                                ------
    /**
     * @return The unified schema. (NotNull)
     */
    public UnifiedSchema getDatabaseSchema() { // as main schema
        if (_cacheMainSchema != null) {
            return _cacheMainSchema;
        }
        final String propTitle = "databaseInfoMap#schema";
        final String schema = resolveDispatchVariable(propTitle, _databaseInfo.getDatabaseSchema());
        _cacheMainSchema = prepareMainUnifiedSchema(getDatabaseCatalog(), schema);
        return _cacheMainSchema;
    }

    public UnifiedSchema prepareMainUnifiedSchema(String catalog, String schema) {
        schema = filterDatabaseSchema(schema);
        return createAsMainSchema(catalog, schema);
    }

    protected String filterDatabaseSchema(String schema) {
        if (isDatabasePostgreSQL()) {
            if (Srl.is_Null_or_TrimmedEmpty(schema)) {
                schema = "public";
            }
        } else if (isDatabaseOracle()) {
            if (Srl.is_NotNull_and_NotTrimmedEmpty(schema)) {
                schema = schema.toUpperCase();
            }
        } else if (isDatabaseDB2()) {
            if (Srl.is_NotNull_and_NotTrimmedEmpty(schema)) {
                schema = schema.toUpperCase();
            }
        } else if (isDatabaseH2()) {
            if (Srl.is_Null_or_TrimmedEmpty(schema)) {
                schema = "PUBLIC";
            }
        } else if (isDatabaseDerby()) {
            if (Srl.is_NotNull_and_NotTrimmedEmpty(schema)) {
                schema = schema.toUpperCase();
            }
        }
        return schema;
    }

    protected UnifiedSchema createAsMainSchema(String catalog, String schema) {
        return UnifiedSchema.createAsMainSchema(catalog, schema);
    }

    public boolean isDifferentUserSchema() {
        final String databaseUser = getDatabaseUser();
        final UnifiedSchema databaseSchema = getDatabaseSchema();
        return !databaseUser.equalsIgnoreCase(databaseSchema.getPureSchema());
    }

    // -----------------------------------------------------
    //                                       User & Password
    //                                       ---------------
    public String getDatabaseUser() {
        if (_cacheUser != null) {
            return _cacheUser;
        }
        final String propTitle = "databaseInfoMap#user";
        _cacheUser = resolveDispatchVariable(propTitle, _databaseInfo.getDatabaseUser());
        return _cacheUser;
    }

    public String getDatabasePassword() {
        if (_cachePassword != null) {
            return _cachePassword;
        }
        final String propTitle = "databaseInfoMap#password";
        final String user = getDatabaseUser();
        _cachePassword = resolvePasswordVariable(propTitle, user, _databaseInfo.getDatabasePassword());
        return _cachePassword;
    }

    // ===================================================================================
    //                                                                         Option Info
    //                                                                         ===========
    // -----------------------------------------------------
    //                                 Connection Properties
    //                                 ---------------------
    protected Properties _connectionProperties;

    public Properties getConnectionProperties() {
        if (_connectionProperties != null) {
            return _connectionProperties;
        }
        _connectionProperties = _databaseInfo.getConnectionProperties();
        return _connectionProperties;
    }

    // -----------------------------------------------------
    //                               Object Type Target List
    //                               -----------------------
    protected List<String> _objectTypeTargetList;

    public List<String> getObjectTypeTargetList() {
        if (_objectTypeTargetList != null) {
            return _objectTypeTargetList;
        }
        final String key = "objectTypeTargetList";
        _objectTypeTargetList = getVairousStringList(key, getDefaultObjectTypeTargetList());
        return _objectTypeTargetList;
    }

    public boolean hasObjectTypeSynonym() {
        return DfConnectionProperties.hasObjectTypeSynonym(getObjectTypeTargetList());
    }

    protected List<String> getDefaultObjectTypeTargetList() {
        final List<Object> defaultList = new ArrayList<Object>();
        defaultList.add(DfConnectionProperties.OBJECT_TYPE_TABLE);
        defaultList.add(DfConnectionProperties.OBJECT_TYPE_VIEW);
        final List<String> resultList = new ArrayList<String>();
        final List<Object> listProp = listProp("torque.database.type.list", defaultList); // old style
        for (Object object : listProp) {
            resultList.add((String) object);
        }
        return resultList;
    }

    // -----------------------------------------------------
    //                                     Table Except List
    //                                     -----------------
    protected List<String> _tableExceptList;
    protected List<String> _tableExceptGenOnlyList; // getting meta data but no generating classes

    public List<String> getTableExceptList() { // for main schema
        if (_tableExceptList != null) {
            return _tableExceptList;
        }
        final List<String> plainList = getVairousStringList("tableExceptList");
        final List<String> resultList;
        if (!plainList.isEmpty()) {
            resultList = plainList;
        } else {
            resultList = new ArrayList<String>();
            final List<Object> listProp = listProp("torque.table.except.list", DEFAULT_EMPTY_LIST);
            for (Object object : listProp) {
                resultList.add((String) object);
            }
        }
        _tableExceptList = new ArrayList<String>();
        _tableExceptGenOnlyList = new ArrayList<String>();
        setupTableOrColumnExceptList(plainList, _tableExceptList, _tableExceptGenOnlyList);
        return _tableExceptList;
    }

    public List<String> getTableExceptGenOnlyList() { // for main schema
        if (_tableExceptGenOnlyList != null) {
            return _tableExceptGenOnlyList;
        }
        getTableExceptList(); // initialize
        return _tableExceptGenOnlyList;
    }

    protected void setupTableOrColumnExceptList(List<String> plainList, List<String> exceptList,
            List<String> exceptGenOnlyList) {
        final String genOnlySuffix = "@gen";
        for (String element : plainList) {
            if (Srl.endsWithIgnoreCase(element, genOnlySuffix)) {
                exceptGenOnlyList.add(Srl.substringLastFrontIgnoreCase(element, genOnlySuffix));
            } else {
                exceptList.add(element);
            }
        }
    }

    // -----------------------------------------------------
    //                                     Table Target List
    //                                     -----------------
    protected List<String> _tableTargetList;

    public List<String> getTableTargetList() { // for main schema
        if (_tableTargetList != null) {
            return _tableTargetList;
        }
        final List<String> plainList = getVairousStringList("tableTargetList");
        final List<String> resultList;
        if (!plainList.isEmpty()) {
            resultList = plainList;
        } else {
            resultList = new ArrayList<String>();
            final List<Object> listProp = listProp("torque.table.target.list", DEFAULT_EMPTY_LIST);
            for (Object object : listProp) {
                resultList.add((String) object);
            }
        }
        _tableTargetList = resultList;
        return _tableTargetList;
    }

    // -----------------------------------------------------
    //                                     Column Except Map
    //                                     -----------------
    protected Map<String, List<String>> _columnExceptMap;
    protected Map<String, List<String>> _columnExceptGenOnlyMap; // getting meta data but no generating classes

    public Map<String, List<String>> getColumnExceptMap() { // for main schema
        if (_columnExceptMap != null) {
            return _columnExceptMap;
        }
        final List<String> oldStyleList = getVairousStringList("columnExceptList");
        if (!oldStyleList.isEmpty()) {
            String msg = "You should migrate 'columnExceptList' to 'columnExceptMap'";
            msg = msg + " in databaseInfoMap.dfprop: columnExceptList=" + oldStyleList;
            throw new IllegalStateException(msg);
        }
        _columnExceptMap = StringKeyMap.createAsFlexible();
        _columnExceptGenOnlyMap = StringKeyMap.createAsFlexible();
        final Map<String, Object> keyMap = getVairousStringKeyMap("columnExceptMap");
        for (Entry<String, Object> entry : keyMap.entrySet()) {
            final String tableName = entry.getKey();
            final Object obj = entry.getValue();
            if (!(obj instanceof List<?>)) {
                String msg = "The type of element in the property 'columnExceptMap' should be List:";
                msg = msg + " type=" + DfTypeUtil.toClassTitle(obj) + " value=" + obj;
                throw new DfIllegalPropertyTypeException(msg);
            }
            @SuppressWarnings("unchecked")
            final List<String> plainList = (List<String>) obj;
            final List<String> exceptList = new ArrayList<String>();
            final List<String> exceptGenOnlyList = new ArrayList<String>();
            setupTableOrColumnExceptList(plainList, exceptList, exceptGenOnlyList);
            _columnExceptMap.put(tableName, exceptList);
            _columnExceptGenOnlyMap.put(tableName, exceptGenOnlyList);
        }
        return _columnExceptMap;
    }

    public Map<String, List<String>> getColumnExceptGenOnlyMap() { // for main schema
        if (_columnExceptGenOnlyMap != null) {
            return _columnExceptGenOnlyMap;
        }
        getColumnExceptMap(); // initialize
        return _columnExceptGenOnlyMap;
    }

    // ===================================================================================
    //                                                                   Additional Schema
    //                                                                   =================
    // -----------------------------------------------------
    //                                 Additional Schema Map
    //                                 ---------------------
    // key is unique-schema
    protected Map<String, DfAdditionalSchemaInfo> _additionalSchemaMap;

    protected void assertOldStyleAdditionalSchema() {
        // Check old style existence
        final Object oldStyle = getVariousObject("additionalSchemaList");
        if (oldStyle != null) {
            String msg = "The property 'additionalSchemaList' have been unsupported!";
            msg = msg + " Please use the property 'additionalSchemaMap'.";
            throw new IllegalStateException(msg);
        }
    }

    protected Map<String, DfAdditionalSchemaInfo> getAdditionalSchemaMap() {
        if (_additionalSchemaMap != null) {
            return _additionalSchemaMap;
        }
        assertOldStyleAdditionalSchema();
        _additionalSchemaMap = StringKeyMap.createAsCaseInsensitive();
        final Map<String, Object> additionalSchemaMap = getVairousStringKeyMap("additionalSchemaMap");
        if (additionalSchemaMap == null) {
            return _additionalSchemaMap;
        }
        final Set<Entry<String, Object>> entrySet = additionalSchemaMap.entrySet();
        for (Entry<String, Object> entry : entrySet) {
            final String identifiedSchema = entry.getKey();
            final Object obj = entry.getValue();
            if (obj == null) {
                String msg = "The value of schema in the property 'additionalSchemaMap' should be required:";
                msg = msg + " identifiedSchema=" + identifiedSchema;
                msg = msg + " additionalSchemaMap=" + additionalSchemaMap;
                throw new DfRequiredPropertyNotFoundException(msg);
            }
            if (!(obj instanceof Map<?, ?>)) {
                String msg = "The type of schema value in the property 'additionalSchemaMap' should be Map:";
                msg = msg + " type=" + DfTypeUtil.toClassTitle(obj) + " value=" + obj;
                throw new DfIllegalPropertyTypeException(msg);
            }
            @SuppressWarnings("unchecked")
            final Map<String, Object> elementMap = (Map<String, Object>) obj;

            final DfAdditionalSchemaInfo info = new DfAdditionalSchemaInfo();
            final String catalog;
            final boolean explicitCatalog;
            if (identifiedSchema.contains(".")) {
                catalog = Srl.substringFirstFront(identifiedSchema, ".");
                explicitCatalog = true;
            } else {
                catalog = getDatabaseCatalog(); // as main catalog
                explicitCatalog = false;
            }
            final String schema = filterDatabaseSchema(Srl.substringFirstRear(identifiedSchema, "."));
            final UnifiedSchema unifiedSchema = createAsAdditionalSchema(catalog, schema, explicitCatalog);
            info.setUnifiedSchema(unifiedSchema);
            setupAdditionalSchemaObjectTypeTargetList(info, elementMap);
            setupAdditionalSchemaTableExceptList(info, elementMap);
            setupAdditionalSchemaTableTargetList(info, elementMap);
            setupAdditionalSchemaColumnExceptList(info, elementMap);
            info.setSuppressCommonColumn(isProperty("isSuppressCommonColumn", false, elementMap));
            info.setSuppressProcedure(isProperty("isSuppressProcedure", false, elementMap));
            _additionalSchemaMap.put(unifiedSchema.getIdentifiedSchema(), info);
        }
        return _additionalSchemaMap;
    }

    protected UnifiedSchema createAsAdditionalSchema(String catalog, String schema, boolean explicitCatalog) {
        return UnifiedSchema.createAsAdditionalSchema(catalog, schema, explicitCatalog);
    }

    // -----------------------------------------------------
    //                              Additional Schema Option
    //                              ------------------------
    protected void setupAdditionalSchemaObjectTypeTargetList(DfAdditionalSchemaInfo info, Map<String, Object> elementMap) {
        final Object obj = elementMap.get("objectTypeTargetList");
        if (obj == null) {
            @SuppressWarnings("unchecked")
            final List<String> objectTypeTargetList = Collections.EMPTY_LIST;
            info.setObjectTypeTargetList(objectTypeTargetList);
        } else if (!(obj instanceof List<?>)) {
            String msg = "The type of objectTypeTargetList in the property 'additionalSchemaMap' should be List:";
            msg = msg + " type=" + DfTypeUtil.toClassTitle(obj) + " value=" + obj;
            throw new DfIllegalPropertyTypeException(msg);
        } else {
            @SuppressWarnings("unchecked")
            final List<String> objectTypeTargetList = (List<String>) obj;
            info.setObjectTypeTargetList(objectTypeTargetList);
        }
    }

    protected void setupAdditionalSchemaTableExceptList(DfAdditionalSchemaInfo info, Map<String, Object> elementMap) {
        final Object obj = elementMap.get("tableExceptList");
        if (obj == null) {
            final List<String> tableExceptList = DfCollectionUtil.emptyList();
            final List<String> tableExceptGenOnlyList = DfCollectionUtil.emptyList();
            info.setTableExceptList(tableExceptList);
            info.setTableExceptGenOnlyList(tableExceptGenOnlyList);
        } else if (!(obj instanceof List<?>)) {
            String msg = "The type of tableExceptList in the property 'additionalSchemaMap' should be List:";
            msg = msg + " type=" + DfTypeUtil.toClassTitle(obj) + " value=" + obj;
            throw new DfIllegalPropertyTypeException(msg);
        } else {
            @SuppressWarnings("unchecked")
            final List<String> plainList = (List<String>) obj;
            final List<String> tableExceptList = new ArrayList<String>();
            final List<String> tableExceptGenOnlyList = new ArrayList<String>();
            setupTableOrColumnExceptList(plainList, tableExceptList, tableExceptGenOnlyList);
            info.setTableExceptList(tableExceptList);
            info.setTableExceptGenOnlyList(tableExceptGenOnlyList);
        }
    }

    protected void setupAdditionalSchemaTableTargetList(DfAdditionalSchemaInfo info, Map<String, Object> elementMap) {
        final Object obj = elementMap.get("tableTargetList");
        if (obj == null) {
            final List<String> tableTargetList = DfCollectionUtil.emptyList();
            info.setTableTargetList(tableTargetList);
        } else if (!(obj instanceof List<?>)) {
            String msg = "The type of tableTargetList in the property 'additionalSchemaMap' should be List:";
            msg = msg + " type=" + DfTypeUtil.toClassTitle(obj) + " value=" + obj;
            throw new DfIllegalPropertyTypeException(msg);
        } else {
            @SuppressWarnings("unchecked")
            final List<String> tableTargetList = (List<String>) obj;
            info.setTableTargetList(tableTargetList);
        }
    }

    protected void setupAdditionalSchemaColumnExceptList(DfAdditionalSchemaInfo info, Map<String, Object> elementMap) {
        final Object obj = elementMap.get("columnExceptMap");
        if (obj == null) {
            final Map<String, List<String>> columnExceptMap = DfCollectionUtil.emptyMap();
            final Map<String, List<String>> columnExceptGenOnlyMap = DfCollectionUtil.emptyMap();
            info.setColumnExceptMap(columnExceptMap);
            info.setColumnExceptGenOnlyMap(columnExceptGenOnlyMap);
        } else if (!(obj instanceof Map<?, ?>)) {
            String msg = "The type of columnExceptMap in the property 'additionalSchemaMap' should be Map:";
            msg = msg + " type=" + DfTypeUtil.toClassTitle(obj) + " value=" + obj;
            throw new DfIllegalPropertyTypeException(msg);
        } else {
            @SuppressWarnings("unchecked")
            final Map<String, List<String>> plainMap = (Map<String, List<String>>) obj;
            final Map<String, List<String>> columnExceptMap = StringKeyMap.createAsFlexible();
            final Map<String, List<String>> columnExceptGenOnlyMap = StringKeyMap.createAsFlexible();
            for (Entry<String, List<String>> entry : plainMap.entrySet()) {
                final String key = entry.getKey();
                final List<String> plainList = entry.getValue();
                final List<String> colummExceptList = new ArrayList<String>();
                final List<String> columnExceptGenOnlyList = new ArrayList<String>();
                setupTableOrColumnExceptList(plainList, colummExceptList, columnExceptGenOnlyList);
                columnExceptMap.put(key, colummExceptList);
                columnExceptGenOnlyMap.put(key, columnExceptGenOnlyList);
            }
            info.setColumnExceptMap(columnExceptMap);
            info.setColumnExceptGenOnlyMap(columnExceptGenOnlyMap);
        }
    }

    // -----------------------------------------------------
    //                            Additional Schema Accessor
    //                            --------------------------
    public List<UnifiedSchema> getAdditionalSchemaList() {
        final Map<String, DfAdditionalSchemaInfo> schemaMap = getAdditionalSchemaMap();
        final Set<Entry<String, DfAdditionalSchemaInfo>> entrySet = schemaMap.entrySet();
        final List<UnifiedSchema> schemaList = new ArrayList<UnifiedSchema>();
        for (Entry<String, DfAdditionalSchemaInfo> entry : entrySet) {
            final DfAdditionalSchemaInfo info = entry.getValue();
            final UnifiedSchema unifiedSchema = info.getUnifiedSchema();
            schemaList.add(unifiedSchema);
        }
        return schemaList;
    }

    public boolean hasAdditionalSchema() {
        return !getAdditionalSchemaMap().isEmpty();
    }

    public boolean hasCatalogAdditionalSchema() {
        final List<UnifiedSchema> additionalSchemaList = getAdditionalSchemaList();
        for (UnifiedSchema unifiedSchema : additionalSchemaList) {
            if (unifiedSchema.isCatalogAdditionalSchema()) {
                return true;
            }
        }
        return false;
    }

    public DfAdditionalSchemaInfo getAdditionalSchemaInfo(UnifiedSchema unifiedSchema) {
        if (unifiedSchema == null) {
            return null;
        }
        final Map<String, DfAdditionalSchemaInfo> map = getAdditionalSchemaMap();
        final String identifiedSchema = unifiedSchema.getIdentifiedSchema();
        return map.get(identifiedSchema);
    }

    // ===================================================================================
    //                                                              CountDownRace MetaData
    //                                                              ======================
    public int getMetaDataCountDownRaceRunnerCount() {
        final int defaultValue = isMetaDataCountDownRaceRunnerDefaultValid() ? 5 : -1;
        final int count = getVariousInteger("metaDataCountDownRaceRunnerCount", defaultValue);
        if (count > 20) {
            String msg = "The property 'metaDataCountDownRaceRunnerCount' should be until 20: " + count;
            throw new DfIllegalPropertySettingException(msg);
        }
        return count; // minus means single thread
    }

    protected boolean isMetaDataCountDownRaceRunnerDefaultValid() {
        // Oracle causes a trouble of performance (very heavy)
        return isDatabaseOracle();
    }

    // ===================================================================================
    //                                                              Retry Case Insensitive
    //                                                              ======================
    public boolean isRetryCaseInsensitiveColumn() {
        return isVariousBoolean("isRetryCaseInsensitiveColumn", isRetryCaseInsensitiveDefaultValid());
    }

    public boolean isRetryCaseInsensitivePrimaryKey() {
        return isVariousBoolean("isRetryCaseInsensitivePrimaryKey", isRetryCaseInsensitiveDefaultValid());
    }

    public boolean isRetryCaseInsensitiveForeignKey() {
        return isVariousBoolean("isRetryCaseInsensitiveForeignKey", isRetryCaseInsensitiveDefaultValid());
    }

    public boolean isRetryCaseInsensitiveUniqueKey() {
        return isVariousBoolean("isRetryCaseInsensitiveUniqueKey", isRetryCaseInsensitiveDefaultValid());
    }

    public boolean isRetryCaseInsensitiveIndex() {
        return isVariousBoolean("isRetryCaseInsensitiveIndex", isRetryCaseInsensitiveDefaultValid());
    }

    protected boolean isRetryCaseInsensitiveDefaultValid() {
        // /- - - - - - - - - - - - - - - - - - - - - - - - - -
        // JDBC task specifies table case name from meta data
        // so it does not need case insensitive
        //
        // ReplaceSchema task has user favorite table case name
        // but it translates the name so safety (not need this) since 1.0.5A
        //
        // MySQL causes a trouble by setting a name only differed in case as parameter
        // when Windows and lower_case_table_names = 0
        //  -> Can't create table '.\exampledb\Foo.frm' (errno: 121)
        // and other modes do not need to retry
        //
        // while, Oracle causes a trouble of performance (very heavy)
        // anyway, various problems exist so default is false since 1.0.5A
        //
        // JDBC driver specification:
        //  MySQL: case insensitive (depends on mode!?)
        //  PostgreSQL: case sensitive
        //  Oracle: case sensitive
        //  DB2: case sensitive
        //  SQLServer: case insensitive
        // - - - - - - - - - -/
        return false;
    }

    // ===================================================================================
    //                                                                   VariousMap Helper
    //                                                                   =================
    @SuppressWarnings("unchecked")
    protected List<String> getVairousStringList(String key) {
        return getVairousStringList(key, Collections.EMPTY_LIST);
    }

    @SuppressWarnings("unchecked")
    protected List<String> getVairousStringList(String key, List<String> defaultList) {
        final Object value = getVariousObject(key);
        if (value == null) {
            return defaultList != null ? defaultList : Collections.EMPTY_LIST;
        }
        assertVariousPropertyList(key, value);
        return (List<String>) value;
    }

    protected void assertVariousPropertyList(String name, Object value) {
        if (!(value instanceof List<?>)) {
            String msg = "The property '" + name + "' should be List: " + value;
            throw new DfIllegalPropertyTypeException(msg);
        }
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> getVairousStringKeyMap(String key) {
        return getVairousStringKeyMap(key, Collections.EMPTY_MAP);
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> getVairousStringKeyMap(String key, Map<String, Object> defaultMap) {
        final Object value = getVariousObject(key);
        if (value == null) {
            return defaultMap != null ? defaultMap : Collections.EMPTY_MAP;
        }
        assertVariousPropertyMap(key, value);
        return (Map<String, Object>) value;
    }

    protected void assertVariousPropertyMap(String name, Object value) {
        if (!(value instanceof Map<?, ?>)) {
            String msg = "The property '" + name + "' should be Map: " + value;
            throw new IllegalStateException(msg);
        }
    }

    protected int getVariousInteger(String key, int defaultValue) {
        final Map<String, Object> variousMap = _databaseInfo.getDatabaseVariousMap();
        final Object obj = variousMap.get(key);
        return obj != null ? Integer.parseInt(obj.toString()) : defaultValue;
    }

    protected boolean isVariousBoolean(String key, boolean defaultValue) {
        final Map<String, Object> variousMap = _databaseInfo.getDatabaseVariousMap();
        final Object obj = variousMap.get(key);
        return obj != null ? obj.toString().equalsIgnoreCase("true") : defaultValue;
    }

    protected Object getVariousObject(String key) {
        final Map<String, Object> variousMap = _databaseInfo.getDatabaseVariousMap();
        return variousMap.get(key);
    }

    // ===================================================================================
    //                                                                  Information Object
    //                                                                  ==================
    public class DatabaseInfo {

        private static final String KEY_DRIVER = "driver";
        private static final String KEY_URL = "url";
        private static final String KEY_CATALOG = "catalog";
        private static final String KEY_SCHEMA = "schema";
        private static final String KEY_USER = "user";
        private static final String KEY_PASSWORD = "password";
        private static final String KEY_PROPERTIES_MAP = "propertiesMap";
        private static final String KEY_VARIOUS_MAP = "variousMap";

        /** Database info map. (for cache) */
        protected Map<String, Object> _databaseInfoMap;

        public String getDatabaseDriver() {
            initializeDatabaseInfoMap();
            final String key = KEY_DRIVER;
            final String databaseInfoElement = getDatabaseInfoElement(key);
            if (databaseInfoElement != null) {
                return databaseInfoElement;
            }
            return stringProp("torque.database.driver");
        }

        public String getDatabaseUrl() {
            initializeDatabaseInfoMap();
            final String key = KEY_URL;
            final String databaseInfoElement = getDatabaseInfoElement(key);
            if (databaseInfoElement != null) {
                return databaseInfoElement + getDatabaseUriProperty();
            }
            return stringProp("torque.database.url");
        }

        private String getDatabaseUriProperty() {
            initializeDatabaseInfoMap();

            final StringBuilder sb = new StringBuilder();
            final Set<String> keySet = _databaseInfoMap.keySet();
            for (String key : keySet) {
                if (equalsKeys(key, KEY_DRIVER, KEY_URL, KEY_CATALOG, KEY_SCHEMA, KEY_USER, KEY_PASSWORD,
                        KEY_PROPERTIES_MAP, KEY_VARIOUS_MAP)) {
                    continue;
                }
                final Object value = _databaseInfoMap.get(key);
                sb.append(";").append(key).append("=").append(value);
            }
            return sb.toString();
        }

        private boolean equalsKeys(String target, String... keys) {
            for (String key : keys) {
                if (target.equals(key)) {
                    return true;
                }
            }
            return false;
        }

        public String getDatabaseCatalog() {
            initializeDatabaseInfoMap();
            final String key = KEY_CATALOG;
            final String databaseInfoElement = getDatabaseInfoElement(key);
            if (databaseInfoElement != null) {
                return databaseInfoElement;
            }
            return stringProp("torque.database.catalog", "");
        }

        public String getDatabaseSchema() {
            initializeDatabaseInfoMap();
            final String key = KEY_SCHEMA;
            final String databaseInfoElement = getDatabaseInfoElement(key);
            if (databaseInfoElement != null) {
                return databaseInfoElement;
            }
            return stringProp("torque.database.schema", "");
        }

        /**
         * @return The string of user. (NotNull)
         */
        public String getDatabaseUser() {
            initializeDatabaseInfoMap();
            final String key = KEY_USER;
            final String databaseInfoElement = getDatabaseInfoElement(key);
            if (databaseInfoElement != null) {
                return databaseInfoElement;
            }
            return stringProp("torque.database.user");
        }

        /**
         * @return The string of password. (NotNull)
         */
        public String getDatabasePassword() {
            initializeDatabaseInfoMap();
            final String key = KEY_PASSWORD;
            final String databaseInfoElement = getDatabaseInfoElement(key);
            if (databaseInfoElement != null) {
                return databaseInfoElement;
            }
            return stringProp("torque.database.password");
        }

        public Properties getConnectionProperties() {
            initializeDatabaseInfoMap();
            final String key = KEY_PROPERTIES_MAP;
            final Map<String, String> propertiesMap = getDatabaseInfoElementAsPropertiesMap(key);
            final Properties props = new Properties();
            if (propertiesMap.isEmpty()) {
                return props;
            }
            final Set<String> keySet = propertiesMap.keySet();
            for (String propKey : keySet) {
                final String propValue = propertiesMap.get(propKey);
                props.setProperty(propKey, propValue);
            }
            return props;
        }

        public Map<String, Object> getDatabaseVariousMap() {
            initializeDatabaseInfoMap();
            final String key = KEY_VARIOUS_MAP;
            final Map<String, Object> variousMap = getDatabaseInfoElementAsVariousMap(key);
            return variousMap;
        }

        protected void initializeDatabaseInfoMap() {
            if (_databaseInfoMap == null) {
                Map<String, Object> databaseInfoMap = getOutsideMapProp("databaseInfo");
                if (databaseInfoMap.isEmpty()) {
                    databaseInfoMap = getOutsideMapProp("databaseInfoMap");
                }
                if (!databaseInfoMap.isEmpty()) {
                    _databaseInfoMap = databaseInfoMap;
                }
            }
        }

        protected boolean hasDatabaseInfoMap() {
            return _databaseInfoMap != null;
        }

        protected String getDatabaseInfoElement(final String key) {
            if (_databaseInfoMap != null) {
                if (!_databaseInfoMap.containsKey(key)) {
                    return "";
                }
                final String value = (String) _databaseInfoMap.get(key);
                return value != null ? value : "";
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        protected Map<String, String> getDatabaseInfoElementAsPropertiesMap(final String key) {
            if (_databaseInfoMap != null) {
                if (!_databaseInfoMap.containsKey(key)) {
                    return new LinkedHashMap<String, String>();
                }
                final Map<String, String> valueList = (Map<String, String>) _databaseInfoMap.get(key);
                return valueList != null ? valueList : new LinkedHashMap<String, String>();
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        protected Map<String, Object> getDatabaseInfoElementAsVariousMap(final String key) {
            if (_databaseInfoMap != null) {
                if (!_databaseInfoMap.containsKey(key)) {
                    return new LinkedHashMap<String, Object>();
                }
                final Map<String, Object> valueList = (Map<String, Object>) _databaseInfoMap.get(key);
                return valueList != null ? valueList : new LinkedHashMap<String, Object>();
            }
            return null;
        }
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    public boolean isDatabaseMySQL() {
        return getBasicProperties().isDatabaseMySQL();
    }

    public boolean isDatabasePostgreSQL() {
        return getBasicProperties().isDatabasePostgreSQL();
    }

    public boolean isDatabaseOracle() {
        return getBasicProperties().isDatabaseOracle();
    }

    public boolean isDatabaseDB2() {
        return getBasicProperties().isDatabaseDB2();
    }

    public boolean isDatabaseSQLServer() {
        return getBasicProperties().isDatabaseSQLServer();
    }

    public boolean isDatabaseH2() {
        return getBasicProperties().isDatabaseH2();
    }

    public boolean isDatabaseDerby() {
        return getBasicProperties().isDatabaseDerby();
    }

    public boolean isDatabaseSQLite() {
        return getBasicProperties().isDatabaseSQLite();
    }

    public boolean isDatabaseMSAccess() {
        return getBasicProperties().isDatabaseMSAccess();
    }

    // ===================================================================================
    //                                                                   Connection Helper
    //                                                                   =================
    public Connection createMainSchemaConnection() {
        final String driver = getDatabaseDriver();
        final String url = getDatabaseUrl();
        final UnifiedSchema schema = getDatabaseSchema();
        final String user = getDatabaseUser();
        final String password = getDatabasePassword();
        final Properties prop = getConnectionProperties();
        prop.setProperty("user", user);
        prop.setProperty("password", password);
        _log.info("...Creating connection to main schema: " + schema);
        return createConnection(driver, url, schema, prop);
    }
}