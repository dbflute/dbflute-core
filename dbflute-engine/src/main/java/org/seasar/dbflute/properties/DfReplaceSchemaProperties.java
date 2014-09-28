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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.engine.database.model.UnifiedSchema;
import org.seasar.dbflute.exception.DfIllegalPropertySettingException;
import org.seasar.dbflute.exception.DfIllegalPropertyTypeException;
import org.seasar.dbflute.exception.DfRequiredPropertyNotFoundException;
import org.seasar.dbflute.helper.process.SystemScript;
import org.seasar.dbflute.infra.reps.DfRepsSchemaSqlDir;
import org.seasar.dbflute.logic.jdbc.urlanalyzer.DfUrlAnalyzer;
import org.seasar.dbflute.logic.jdbc.urlanalyzer.factory.DfUrlAnalyzerFactory;
import org.seasar.dbflute.properties.assistant.DfConnectionProperties;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.DfStringUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public final class DfReplaceSchemaProperties extends DfAbstractHelperProperties {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log _log = LogFactory.getLog(DfReplaceSchemaProperties.class);

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfReplaceSchemaProperties(Properties prop) {
        super(prop);
    }

    // ===================================================================================
    //                                                          replaceSchemaDefinitionMap
    //                                                          ==========================
    public static final String KEY_replaceSchemaDefinitionMap = "replaceSchemaDefinitionMap";
    protected Map<String, Object> _replaceSchemaDefinitionMap;

    public Map<String, Object> getReplaceSchemaDefinitionMap() {
        if (_replaceSchemaDefinitionMap == null) {
            final Map<String, Object> map = mapProp("torque." + KEY_replaceSchemaDefinitionMap, DEFAULT_EMPTY_MAP);
            _replaceSchemaDefinitionMap = newLinkedHashMap();
            _replaceSchemaDefinitionMap.putAll(map);
        }
        return _replaceSchemaDefinitionMap;
    }

    // ===================================================================================
    //                                                                      Base Directory
    //                                                                      ==============
    public String getPlaySqlDir() {
        return doGetPlaySqlDirectory(); // path (basically) relative from DBFlute client
    }

    public String getPlaySqlDirPureName() {
        final String playSqlDir = doGetPlaySqlDirectory();
        return Srl.substringLastRear(playSqlDir, "/");
    }

    protected String doGetPlaySqlDirectory() {
        final String prop = (String) getReplaceSchemaDefinitionMap().get("playSqlDirectory");
        return Srl.is_NotNull_and_NotTrimmedEmpty(prop) ? prop : "playsql";
    }

    // ===================================================================================
    //                                                                          Schema SQL
    //                                                                          ==========
    // -----------------------------------------------------
    //                                         Create Schema
    //                                         -------------
    public List<File> getReplaceSchemaSqlFileList(String sqlRootDir) {
        final DfRepsSchemaSqlDir schemaSqlDir = createRepsSchemaSqlDir(sqlRootDir);
        return schemaSqlDir.collectReplaceSchemaSqlFileList();
    }

    public Map<String, File> getReplaceSchemaSqlFileMap(String sqlRootDir) {
        return convertToSchemaSqlFileMap(getReplaceSchemaSqlFileList(sqlRootDir));
    }

    protected DfRepsSchemaSqlDir createRepsSchemaSqlDir(String sqlRootDir) {
        return new DfRepsSchemaSqlDir(sqlRootDir);
    }

    protected Map<String, File> convertToSchemaSqlFileMap(List<File> sqlFileList) {
        final Map<String, File> resultMap = new LinkedHashMap<String, File>();
        for (File sqlFile : sqlFileList) {
            // Schema SQL files are located in the same directory
            final String uniqueKey = sqlFile.getName();
            resultMap.put(uniqueKey, sqlFile);
        }
        return resultMap;
    }

    // -----------------------------------------------------
    //                                          Take Finally
    //                                          ------------
    public List<File> getTakeFinallySqlFileList(String sqlRootDir) {
        final DfRepsSchemaSqlDir schemaSqlDir = createRepsSchemaSqlDir(sqlRootDir);
        return schemaSqlDir.collectTakeFinallySqlFileList();
    }

    public Map<String, File> getTakeFinallySqlFileMap(String sqlRootDir) {
        return convertToSchemaSqlFileMap(getTakeFinallySqlFileList(sqlRootDir));
    }

    // ===================================================================================
    //                                                                         Schema Data
    //                                                                         ===========
    public String getSchemaDataDir(String baseDir) {
        return baseDir + "/data";
    }

    public String getCommonDataDir(String baseDir, String typeName) {
        return getSchemaDataDir(baseDir) + "/common/" + typeName;
    }

    public String getLoadTypeDataDir(String baseDir, String loadType, String typeName) {
        return getSchemaDataDir(baseDir) + "/" + loadType + "/" + typeName;
    }

    // non-ApplicationPlaySql below

    public String getMainCommonDataDir() {
        final String playSqlDirectory = getPlaySqlDir();
        return playSqlDirectory + "/data/common";
    }

    public String getMainCommonFirstXlsDataDir() {
        return getMainCommonDataDir() + "/firstxls";
    }

    public String getMainCommonReverseXlsDataDir() {
        return getMainCommonDataDir() + "/reversexls";
    }

    public String getMainCommonXlsDataDir() {
        return getMainCommonDataDir() + "/xls";
    }

    protected String getMainCurrentLoadTypeDataDir() {
        final String playSqlDirectory = getPlaySqlDir();
        final String dataLoadingType = getRepsEnvType();
        return playSqlDirectory + "/data/" + dataLoadingType;
    }

    public String getMainCurrentLoadTypeDataDir(String fileType) {
        return getMainCurrentLoadTypeDataDir() + "/" + fileType;
    }

    public String getMainCurrentLoadTypeFirstXlsDataDir() {
        return getMainCurrentLoadTypeDataDir() + "/firstxls";
    }

    public String getMainCurrentLoadTypeReverseXlsDataDir() {
        return getMainCurrentLoadTypeDataDir() + "/reversexls";
    }

    public String getMainCurrentLoadTypeReverseTsvDataDir() {
        return getMainCurrentLoadTypeDataDir() + "/reversetsv";
    }

    public String getMainCurrentLoadTypeReverseTsvUTF8DataDir() {
        return getMainCurrentLoadTypeReverseTsvDataDir() + "/UTF-8";
    }

    public String getMainCurrentLoadTypeTsvDataDir() {
        return getMainCurrentLoadTypeDataDir() + "/tsv";
    }

    public String getMainCurrentLoadTypeTsvUTF8DataDir() {
        return getMainCurrentLoadTypeTsvDataDir() + "/UTF-8";
    }

    public String getMainCurrentLoadTypeCsvDataDir() {
        return getMainCurrentLoadTypeDataDir() + "/csv";
    }

    public String getMainCurrentLoadTypeCsvUTF8DataDir() {
        return getMainCurrentLoadTypeCsvDataDir() + "/csv/UTF-8";
    }

    public String getMainCurrentLoadTypeXlsDataDir() {
        return getMainCurrentLoadTypeDataDir() + "/xls";
    }

    public List<File> findSchemaDataAllList(String sqlRootDir) { // contains data-prop
        final File sqlRoot = new File(sqlRootDir);
        final List<File> fileList = new ArrayList<File>();
        doFindHierarchyFileList(fileList, sqlRoot);
        return fileList;
    }

    protected void doFindHierarchyFileList(final List<File> fileList, File baseDir) {
        if (baseDir.getName().startsWith(".")) { // closed directory
            return; // e.g. .svn
        }
        final File[] listFiles = baseDir.listFiles(new FileFilter() {
            public boolean accept(File subFile) {
                if (subFile.isDirectory()) {
                    doFindHierarchyFileList(fileList, subFile);
                    return false;
                }
                return true;
            }
        });
        if (listFiles != null) {
            fileList.addAll(Arrays.asList(listFiles));
        }
    }

    // ===================================================================================
    //                                                                    Environment Type
    //                                                                    ================
    public String getRepsEnvType() { // not null
        final String dataLoadingType = getDataLoadingType();
        if (Srl.is_NotNull_and_NotTrimmedEmpty(dataLoadingType)) {
            return dataLoadingType;
        }
        final String propString = (String) getReplaceSchemaDefinitionMap().get("repsEnvType");
        if (propString == null) {
            if (isSpecifiedEnvironmentType()) {
                return getEnvironmentType();
            } else {
                return "ut"; // final default
            }
        }
        return propString;
    }

    protected String getDataLoadingType() { // old style
        return (String) getReplaceSchemaDefinitionMap().get("dataLoadingType");
    }

    public boolean isTargetRepsFile(String sql) { // for ReplaceSchema
        return checkTargetEnvType(sql, getRepsEnvType());
    }

    public boolean isTargetEnvTypeFile(String sql) { // for general purpose
        return checkTargetEnvType(sql, getEnvironmentTypeMightBeDefault());
    }

    protected boolean checkTargetEnvType(String sql, String envType) {
        final String envExp = analyzeCheckEnvType(sql);
        if (Srl.is_Null_or_TrimmedEmpty(envExp)) {
            return true; // no check means target
        }
        final List<String> envList = Srl.splitListTrimmed(envExp, ",");
        _log.info("...Checking envType: " + envType + " in " + envList);
        return envList.contains(envType);
    }

    protected String analyzeCheckEnvType(String sql) {
        final String beginMark = "#df:checkEnv(";
        final int markIndex = sql.indexOf(beginMark);
        if (markIndex < 0) {
            return null;
        }
        final String rear = sql.substring(markIndex + beginMark.length());
        final int endIndex = rear.indexOf(")");
        if (endIndex < 0) {
            String msg = "The command checkEnv should have its end mark ')':";
            msg = msg + " example=[#df:checkEnv(ut)#], sql=" + sql;
            throw new IllegalStateException(msg);
        }
        return rear.substring(0, endIndex).trim();
    }

    // ===================================================================================
    //                                                                Filter Variables Map
    //                                                                ====================
    protected Map<String, String> _filterVariablesMap;

    @SuppressWarnings("unchecked")
    protected Map<String, String> getFilterVariablesMap() {
        if (_filterVariablesMap != null) {
            return _filterVariablesMap;
        }
        _filterVariablesMap = (Map<String, String>) getReplaceSchemaDefinitionMap().get("filterVariablesMap");
        if (_filterVariablesMap == null) {
            _filterVariablesMap = new LinkedHashMap<String, String>();
        }
        setupDefaultFilterVariables(_filterVariablesMap);
        return _filterVariablesMap;
    }

    protected void setupDefaultFilterVariables(Map<String, String> filterVariablesMap) {
        final DfDatabaseProperties prop = getDatabaseProperties();
        filterVariablesMap.put("dfprop.mainCatalog", prop.getDatabaseCatalog());
        filterVariablesMap.put("dfprop.mainSchema", prop.getDatabaseSchema().getPureSchema());
        filterVariablesMap.put("dfprop.mainUser", prop.getDatabaseUser());
        filterVariablesMap.put("dfprop.mainPassword", prop.getDatabasePassword());

        try {
            // absolute path of DBFlute client
            filterVariablesMap.put("sys.basedir", new File(".").getCanonicalPath());
        } catch (IOException e) {
            String msg = "File.getCanonicalPath() threw the exception.";
            throw new IllegalStateException(msg, e);
        }
    }

    protected String getFilterVariablesBeginMark() {
        return "/*$";
    }

    protected String getFilterVariablesEndMark() {
        return "*/";
    }

    public String resolveFilterVariablesIfNeeds(String sql) {
        final String beginMark = getFilterVariablesBeginMark();
        final String endMark = getFilterVariablesEndMark();
        final String directMark = "direct:";
        final Map<String, String> filterVariablesMap = getFilterVariablesMap();
        if (filterVariablesMap.isEmpty()) {
            return sql;
        }
        boolean existsDirect = false;
        for (String key : filterVariablesMap.keySet()) {
            if (Srl.startsWithIgnoreCase(key, directMark)) {
                existsDirect = true;
                break;
            }
        }
        if (existsDirect || (sql.contains(beginMark) && sql.contains(endMark))) {
            for (Entry<String, String> entry : filterVariablesMap.entrySet()) {
                final String key = entry.getKey();
                if (Srl.startsWithIgnoreCase(key, directMark)) { // direct
                    final String fromText = Srl.substringFirstRearIgnoreCase(key, directMark);
                    sql = replaceString(sql, fromText, entry.getValue());
                } else { // embedded
                    final String variableMark = beginMark + key + endMark;
                    if (sql.contains(variableMark)) {
                        sql = replaceString(sql, variableMark, entry.getValue());
                    }
                }
            }
        }
        return sql;
    }

    // ===================================================================================
    //                                                                             Logging
    //                                                                             =======
    public boolean isLoggingInsertSql() {
        return isProperty("isLoggingInsertSql", true, getReplaceSchemaDefinitionMap());
    }

    protected boolean isLoggingReplaceSql() {
        return isProperty("isLoggingReplaceSql", true, getReplaceSchemaDefinitionMap());
    }

    public boolean isSuppressLoggingReplaceSql() {
        return !isLoggingReplaceSql();
    }

    // ===================================================================================
    //                                                                            Continue
    //                                                                            ========
    public boolean isErrorSqlContinue() {
        // default is false (at an old time, true)
        // though DBFlute task returns failure when this property is true,
        // load data may have big cost so change the default value
        return isProperty("isErrorSqlContinue", false, getReplaceSchemaDefinitionMap());
    }

    // ===================================================================================
    //                                                                   SQL File Encoding
    //                                                                   =================
    public String getSqlFileEncoding() {
        final String sqlFileEncoding = (String) getReplaceSchemaDefinitionMap().get("sqlFileEncoding");
        if (sqlFileEncoding != null && sqlFileEncoding.trim().length() != 0) {
            return sqlFileEncoding;
        } else {
            return "UTF-8";
        }
    }

    // ===================================================================================
    //                                                                          Skip Sheet
    //                                                                          ==========
    public String getSkipSheet() {
        final String skipSheet = (String) getReplaceSchemaDefinitionMap().get("skipSheet");
        if (skipSheet != null && skipSheet.trim().length() != 0) {
            return skipSheet;
        } else {
            return null;
        }
    }

    // ===================================================================================
    //                                                                  Increment Sequence
    //                                                                  ==================
    public boolean isIncrementSequenceToDataMax() {
        return isProperty("isIncrementSequenceToDataMax", false, getReplaceSchemaDefinitionMap());
    }

    // ===================================================================================
    //                                                               Suppress Batch Update
    //                                                               =====================
    public boolean isSuppressBatchUpdate() {
        return isProperty("isSuppressBatchUpdate", false, getReplaceSchemaDefinitionMap());
    }

    // ===================================================================================
    //                                                             Object Type Target List
    //                                                             =======================
    protected List<String> _objectTypeTargetList;

    public List<String> getObjectTypeTargetList() { // overrides the property of databaseInfoMap 
        final Object obj = getReplaceSchemaDefinitionMap().get("objectTypeTargetList");
        if (obj != null && !(obj instanceof List<?>)) {
            String msg = "The type of the property 'objectTypeTargetList' should be List: " + obj;
            throw new DfIllegalPropertyTypeException(msg);
        }
        final List<String> defaultObjectTypeTargetList = getDefaultObjectTypeTargetList();
        if (obj == null) {
            _objectTypeTargetList = defaultObjectTypeTargetList;
        } else {
            @SuppressWarnings("unchecked")
            final List<String> list = (List<String>) obj;
            _objectTypeTargetList = !list.isEmpty() ? list : defaultObjectTypeTargetList;
        }
        return _objectTypeTargetList;
    }

    protected List<String> getDefaultObjectTypeTargetList() {
        return getDatabaseProperties().getObjectTypeTargetList(); // inherit
    }

    // ===================================================================================
    //                                                                     Additional User
    //                                                                     ===============
    protected Map<String, Map<String, String>> _additionalUesrMap;

    protected Map<String, Map<String, String>> getAdditionalUserMap() {
        if (_additionalUesrMap != null) {
            return _additionalUesrMap;
        }
        final Object obj = getReplaceSchemaDefinitionMap().get("additionalUserMap");
        if (obj != null && !(obj instanceof Map<?, ?>)) {
            String msg = "The type of the property 'additionalUserMap' should be Map: " + obj;
            throw new DfIllegalPropertyTypeException(msg);
        }
        if (obj == null) {
            _additionalUesrMap = DfCollectionUtil.emptyMap();
        } else {
            @SuppressWarnings("unchecked")
            final Map<String, Map<String, String>> map = (Map<String, Map<String, String>>) obj;
            _additionalUesrMap = map;
        }
        return _additionalUesrMap;
    }

    protected Map<String, String> getAdditionalUserPropertyMap(String additonalUser) {
        return getAdditionalUserMap().get(additonalUser);
    }

    public Connection createAdditionalUserConnection(String additonalUser) {
        final Map<String, String> propertyMap = getAdditionalUserPropertyMap(additonalUser);
        if (propertyMap == null) {
            return null;
        }
        final String title = "replaceSchemaDefinitionMap#additionalUserMap$";
        final String driver = getDatabaseProperties().getDatabaseDriver();
        final String url;
        {
            String property = resolveDispatchVariable(title + "url", propertyMap.get("url"));
            if (property != null && property.trim().length() > 0) {
                url = property;
            } else {
                url = getDatabaseProperties().getDatabaseUrl();
            }
        }
        final DfUrlAnalyzerFactory factory = new DfUrlAnalyzerFactory(getBasicProperties(), url);
        final DfUrlAnalyzer analyzer = factory.createAnalyzer();
        final String catalog = analyzer.extractCatalog();
        final String schema = propertyMap.get("schema");
        final UnifiedSchema unifiedSchema = UnifiedSchema.createAsDynamicSchema(catalog, schema);
        final String user = resolveDispatchVariable(title + "user", propertyMap.get("user"));
        final String password = resolvePasswordVariable(title + "password", additonalUser, propertyMap.get("password"));
        final Properties prop = getDatabaseProperties().getConnectionProperties();
        prop.setProperty("user", user);
        prop.setProperty("password", password);
        _log.info("...Creating a connection for additional user: " + user);
        return createConnection(driver, url, unifiedSchema, prop);
    }

    protected final Map<String, Boolean> _additionalUserSkipIfNotFoundPasswordFileAndDefaultMap = newLinkedHashMap();

    public boolean isAdditionalUserSkipIfNotFoundPasswordFileAndDefault(String additonalUser) {
        Boolean result = _additionalUserSkipIfNotFoundPasswordFileAndDefaultMap.get(additonalUser);
        if (result != null) {
            return result;
        }
        result = false;
        final String key = "isSkipIfNotFoundPasswordFileAndDefault";
        final Map<String, String> propertyMap = getAdditionalUserPropertyMap(additonalUser);
        if (propertyMap != null && isProperty(key, false, propertyMap)) {
            final String password = propertyMap.get("password");
            if (Srl.is_NotNull_and_NotTrimmedEmpty(password)) {
                final DfDispatchVariableInfo pwdInfo = analyzeDispatchVariable(password);
                if (!pwdInfo.getDispatchFile().exists() && pwdInfo.getDefaultValue() == null) { // both not found
                    result = true;
                }
            }
        }
        _additionalUserSkipIfNotFoundPasswordFileAndDefaultMap.put(additonalUser, result);
        return _additionalUserSkipIfNotFoundPasswordFileAndDefaultMap.get(additonalUser);
    }

    // ===================================================================================
    //                                                                     Additional Drop
    //                                                                     ===============
    protected List<Map<String, Object>> _additionalDropMapList;

    public List<Map<String, Object>> getAdditionalDropMapList() {
        if (_additionalDropMapList != null) {
            return _additionalDropMapList;
        }
        final Object obj = getReplaceSchemaDefinitionMap().get("additionalDropMapList");
        if (obj == null) {
            _additionalDropMapList = DfCollectionUtil.emptyList();
        } else {
            _additionalDropMapList = castToList(obj, "additionalDropMapList");
        }
        return _additionalDropMapList;
    }

    public String getAdditionalDropUrl(Map<String, Object> additionalDropMap) {
        final Object obj = additionalDropMap.get("url");
        if (obj == null) {
            return null;
        }
        return castToString(obj, "additionalDropMapList.url");
    }

    public String getAdditionalDropUser(Map<String, Object> additionalDropMap) {
        final Object obj = additionalDropMap.get("user");
        if (obj == null) {
            return null;
        }
        return castToString(obj, "additionalDropMapList.user");
    }

    public String getAdditionalDropPassword(Map<String, Object> additionalDropMap) {
        final Object obj = additionalDropMap.get("password");
        if (obj == null) {
            return null;
        }
        return castToString(obj, "additionalDropMapList.password");
    }

    @SuppressWarnings("unchecked")
    public Properties getAdditionalDropPropertiesMap(Map<String, Object> additionalDropMap) {
        Object obj = additionalDropMap.get("propertiesMap");
        if (obj == null) {
            return new Properties();
        }
        if (!(obj instanceof Map)) {
            String msg = "The schema should be Map<String, String>:";
            msg = msg + " propertiesMap=" + obj + " type=" + obj.getClass();
            throw new DfIllegalPropertyTypeException(msg);
        }
        final Properties prop = new Properties();
        prop.putAll((Map<String, String>) obj);
        return prop;
    }

    public UnifiedSchema getAdditionalDropSchema(Map<String, Object> additionalDropMap) {
        final String url = getAdditionalDropUrl(additionalDropMap);
        final String catalog;
        if (Srl.is_NotNull_and_NotTrimmedEmpty(url)) {
            final DfUrlAnalyzerFactory factory = new DfUrlAnalyzerFactory(getBasicProperties(), url);
            final DfUrlAnalyzer analyzer = factory.createAnalyzer();
            catalog = analyzer.extractCatalog();
        } else {
            catalog = getDatabaseProperties().getDatabaseCatalog();
        }
        final Object obj = additionalDropMap.get("schema");
        if (obj == null) {
            if (!isDatabaseAsSchemaSpecificationOmittable()) {
                String msg = "The schema is required:";
                msg = msg + " additionalDropMap=" + additionalDropMap;
                throw new DfRequiredPropertyNotFoundException(msg);
            }
            return null;
        }
        final String schema = castToString(obj, "additionalDropMapList.schema");
        final UnifiedSchema unifiedSchema = UnifiedSchema.createAsDynamicSchema(catalog, schema);
        return unifiedSchema;
    }

    protected boolean isDatabaseAsSchemaSpecificationOmittable() {
        return getBasicProperties().isDatabaseAsSchemaSpecificationOmittable();
    }

    public List<String> getAdditionalDropObjectTypeList(Map<String, Object> additionalDropMap) {
        Object obj = additionalDropMap.get("objectTypeTargetList");
        if (obj == null) {
            obj = additionalDropMap.get("objectTypeList"); // old style
            if (obj == null) {
                final List<String> defaultList = new ArrayList<String>();
                defaultList.add(DfConnectionProperties.OBJECT_TYPE_TABLE);
                defaultList.add(DfConnectionProperties.OBJECT_TYPE_VIEW);
                return defaultList;
            }
        }
        return castToList(obj, "additionalDropMapList.objectTypeTargetList");
    }

    public Connection createAdditionalDropConnection(Map<String, Object> additionalDropMap) {
        final String driver = getDatabaseProperties().getDatabaseDriver();
        String url = getAdditionalDropUrl(additionalDropMap);
        url = url != null && url.trim().length() > 0 ? url : getDatabaseProperties().getDatabaseUrl();
        String user = getAdditionalDropUser(additionalDropMap);
        String password;
        if (user != null && user.trim().length() > 0) {
            password = getAdditionalDropPassword(additionalDropMap);
            if (password == null || password.trim().length() == 0) {
                String msg = "The password is required when the user is specified:";
                msg = msg + " user=" + user + " additionalDropMap=" + additionalDropMap;
                throw new DfIllegalPropertySettingException(msg);
            }
        } else {
            user = getDatabaseProperties().getDatabaseUser();
            password = getDatabaseProperties().getDatabasePassword();
        }
        final Properties prop = getAdditionalDropPropertiesMap(additionalDropMap);
        Properties info = new Properties();
        info.putAll(prop);
        info.put("user", user);
        info.put("password", password);
        _log.info("...Creating a connection for additional drop");
        return createConnection(driver, url, getAdditionalDropSchema(additionalDropMap), info);
    }

    // ===================================================================================
    //                                                                 Application PlaySql
    //                                                                 ===================
    public String getApplicationPlaySqlDirectory() {
        return getProperty("applicationPlaySqlDirectory", null, getReplaceSchemaDefinitionMap());
    }

    public List<File> getApplicationReplaceSchemaSqlFileList() {
        final String targetDir = getApplicationPlaySqlDirectory();
        if (targetDir == null) {
            return DfCollectionUtil.emptyList();
        }
        final DfRepsSchemaSqlDir schemaSqlDir = createRepsSchemaSqlDir(targetDir);
        return schemaSqlDir.collectReplaceSchemaSqlFileList();
    }

    public List<File> getAppcalitionTakeFinallySqlFileList() {
        final String targetDir = getApplicationPlaySqlDirectory();
        if (targetDir == null) {
            return DfCollectionUtil.emptyList();
        }
        final DfRepsSchemaSqlDir schemaSqlDir = createRepsSchemaSqlDir(targetDir);
        return schemaSqlDir.collectTakeFinallySqlFileList();
    }

    // ===================================================================================
    //                                                                 Arrange before Reps
    //                                                                 ===================
    // #; arrangeBeforeRepsMap = map:{
    // #    ; define = map:{
    // #        ; $$SrcDir$$ = ../../foo/dbflute_foodb/playsql/
    // #    }
    // #    ; copy = map:{
    // #        ; ../erd/*.ddl = ./playsql/replace-schema-10-basic.sql
    // #        ; $$SrcDir$$/data/common/xls/*.xls = ./playsql/data/common/xls
    // #        ; $$SrcDir$$/data/ut/xls/*.xls = ./playsql/data/ut/xls df:clean
    // #    }
    // #    ; script = map:{
    // #        ; ../maven-install.sh = dummy
    // #    }
    // #}
    protected Map<String, Map<String, Object>> _arrangeBeforeRepsMap;

    protected Map<String, Map<String, Object>> getArrangeBeforeRepsMap() {
        if (_arrangeBeforeRepsMap != null) {
            return _arrangeBeforeRepsMap;
        }
        final Object obj = getReplaceSchemaDefinitionMap().get("arrangeBeforeRepsMap");
        if (obj == null) {
            _arrangeBeforeRepsMap = DfCollectionUtil.emptyMap();
        } else {
            @SuppressWarnings("unchecked")
            final Map<String, Map<String, Object>> arrangeMap = (Map<String, Map<String, Object>>) obj;
            checkArrangeUnsupportedManupulation(arrangeMap);
            _arrangeBeforeRepsMap = arrangeMap;
        }
        return _arrangeBeforeRepsMap;
    }

    protected void checkArrangeUnsupportedManupulation(Map<String, Map<String, Object>> arrangeMap) {
        if (arrangeMap.size() >= 4) { // may support other manipulations
            String msg = "The arrangeBeforeReps supports only 'define' and 'copy' and 'script' now: "
                    + arrangeMap.keySet();
            throw new DfIllegalPropertySettingException(msg);
        }
    }

    protected Map<String, String> getArrangeBeforeRepsDefineMap() {
        final Map<String, Map<String, Object>> repsMap = getArrangeBeforeRepsMap();
        final Map<String, Object> elementMap = repsMap.get("define");
        if (elementMap == null) {
            return DfCollectionUtil.emptyMap();
        }
        final Map<String, String> defineMap = new LinkedHashMap<String, String>();
        for (Entry<String, Object> entry : elementMap.entrySet()) {
            defineMap.put(entry.getKey(), (String) entry.getValue());
        }
        return defineMap;
    }

    public Map<String, String> getArrangeBeforeRepsCopyMap() {
        final Map<String, String> defineMap = getArrangeBeforeRepsDefineMap();
        final Map<String, Map<String, Object>> repsMap = getArrangeBeforeRepsMap();
        final Map<String, Object> elementMap = repsMap.get("copy");
        if (elementMap == null) {
            return DfCollectionUtil.emptyMap();
        }
        final Map<String, String> copyMap = new LinkedHashMap<String, String>();
        for (Entry<String, Object> entry : elementMap.entrySet()) {
            final String key = Srl.replaceBy(entry.getKey(), defineMap);
            final String value = Srl.replaceBy((String) entry.getValue(), defineMap);
            copyMap.put(key, value);
        }
        return copyMap;
    }

    public Map<String, String> getArrangeBeforeRepsScriptMap() {
        final Map<String, String> defineMap = getArrangeBeforeRepsDefineMap();
        final Map<String, Map<String, Object>> repsMap = getArrangeBeforeRepsMap();
        final Map<String, Object> elementMap = repsMap.get("script");
        if (elementMap == null) {
            return DfCollectionUtil.emptyMap();
        }
        final Map<String, String> scriptMap = new LinkedHashMap<String, String>();
        for (Entry<String, Object> entry : elementMap.entrySet()) {
            final String key = Srl.replaceBy(entry.getKey(), defineMap);
            // value is dummy for now
            //final String value = Srl.replaceBy((String) entry.getValue(), defineMap);
            scriptMap.put(key, (String) entry.getValue());
        }
        return scriptMap;
    }

    // ===================================================================================
    //                                                        Suppress Initializing Schema
    //                                                        ============================
    public boolean isSuppressTruncateTable() {
        return isProperty("isSuppressTruncateTable", false, getReplaceSchemaDefinitionMap());
    }

    public boolean isSuppressDropForeignKey() {
        return isProperty("isSuppressDropForeignKey", false, getReplaceSchemaDefinitionMap());
    }

    public boolean isSuppressDropTable() {
        return isProperty("isSuppressDropTable", false, getReplaceSchemaDefinitionMap());
    }

    public boolean isSuppressDropSequence() {
        return isProperty("isSuppressDropSequence", false, getReplaceSchemaDefinitionMap());
    }

    public boolean isSuppressDropProcedure() {
        return isProperty("isSuppressDropProcedure", false, getReplaceSchemaDefinitionMap());
    }

    public boolean isSuppressDropDBLink() {
        return isProperty("isSuppressDropDBLink", false, getReplaceSchemaDefinitionMap());
    }

    // ===================================================================================
    //                                                                           Migration
    //                                                                           =========
    // -----------------------------------------------------
    //                                        Basic Resource
    //                                        --------------
    protected String getMigrationDir() {
        final String playSqlDirectory = getPlaySqlDir();
        return playSqlDirectory + "/" + getMigrationDirPureName();
    }

    protected String getMigrationDirPureName() {
        return "migration";
    }

    // -----------------------------------------------------
    //                                          Alter Schema
    //                                          ------------
    public String getMigrationAlterDirectory() {
        return getMigrationDir() + "/alter";
    }

    public List<File> getMigrationAlterSqlFileList() { // contains script files
        final String targetDir = getMigrationAlterDirectory();
        final String sqlTitle = getMigrationAlterSchemaSqlTitle();
        final List<String> suffixList = new ArrayList<String>();
        suffixList.add(".sql");
        suffixList.addAll(SystemScript.getSupportedExtList());
        return findSchemaResourceFileList(targetDir, sqlTitle, suffixList.toArray(new String[] {}));
    }

    public String getMigrationAlterSchemaSqlTitle() {
        return "alter-schema";
    }

    public boolean hasMigrationAlterSqlResource() {
        return !getMigrationAlterSqlFileList().isEmpty();
    }

    public File getMigrationSimpleAlterSqlFile() {
        final String alterDirPath = getMigrationAlterDirectory();
        final String sqlTitle = getMigrationAlterSchemaSqlTitle();
        return new File(alterDirPath + "/" + sqlTitle + ".sql");
    }

    // -----------------------------------------------------
    //                                     Alter TakeFinally
    //                                     -----------------
    public List<File> getMigrationAlterTakeFinallySqlFileList(String sqlRootDir) {
        final DfRepsSchemaSqlDir schemaSqlDir = createRepsSchemaSqlDir(sqlRootDir);
        return schemaSqlDir.collectAlterTakeFinallySqlFileList();
    }

    // -----------------------------------------------------
    //                                           Alter Draft
    //                                           -----------
    public List<File> getMigrationDraftAlterSqlFileList() { // contains script files
        final String targetDir = getMigrationDraftAlterDirectory();
        final String sqlTitle = getMigrationAlterSchemaSqlTitle();
        final List<String> suffixList = new ArrayList<String>();
        suffixList.add(".sql");
        suffixList.addAll(SystemScript.getSupportedExtList());
        final String[] suffixes = suffixList.toArray(new String[] {});
        final List<File> fileList = findSchemaResourceFileList(targetDir, sqlTitle, suffixes);
        fileList.addAll(findSchemaResourceFileList(targetDir, "draft-" + sqlTitle, suffixes));
        return fileList;
    }

    public List<File> getMigrationDraftTakeFinallySqlFileList() {
        final String targetDir = getMigrationDraftAlterDirectory();
        final String sqlTitle = getMigrationAlterTakeFinallySqlTitle();
        final List<String> suffixList = new ArrayList<String>();
        suffixList.add(".sql");
        final String[] suffixes = suffixList.toArray(new String[] {});
        final List<File> fileList = findSchemaResourceFileList(targetDir, sqlTitle, suffixes);
        fileList.addAll(findSchemaResourceFileList(targetDir, "draft-" + sqlTitle, suffixes));
        return fileList;
    }

    protected String getMigrationDraftAlterDirectory() {
        return getMigrationDir() + "/alter/draft";
    }

    protected String getMigrationAlterTakeFinallySqlTitle() {
        return DfRepsSchemaSqlDir.ALTER_TAKE_FINALLY_SQL_TITLE; // same as normal
    }

    // -----------------------------------------------------
    //                                     Previous Resource
    //                                     -----------------
    public String getMigrationPreviousDir() {
        return getMigrationDir() + "/previous";
    }

    public boolean hasMigrationPreviousResource() {
        return !getMigrationPreviousReplaceSchemaSqlFileList().isEmpty();
    }

    protected List<File> _migrationPreviousReplaceSchemaSqlFileList;

    public List<File> getMigrationPreviousReplaceSchemaSqlFileList() {
        if (_migrationPreviousReplaceSchemaSqlFileList != null) {
            return _migrationPreviousReplaceSchemaSqlFileList;
        }
        final DfRepsSchemaSqlDir schemaSqlDir = createRepsSchemaSqlDir(getMigrationPreviousDir());
        _migrationPreviousReplaceSchemaSqlFileList = schemaSqlDir.collectReplaceSchemaSqlFileList();
        return _migrationPreviousReplaceSchemaSqlFileList;
    }

    public Map<String, File> getMigrationPreviousReplaceSchemaSqlFileMap() {
        return convertToSchemaSqlFileMap(getMigrationPreviousReplaceSchemaSqlFileList());
    }

    protected List<File> _migrationPreviousTakeFinallySqlFileList;

    public List<File> getMigrationPreviousTakeFinallySqlFileList() {
        if (_migrationPreviousTakeFinallySqlFileList != null) {
            return _migrationPreviousTakeFinallySqlFileList;
        }
        final DfRepsSchemaSqlDir schemaSqlDir = createRepsSchemaSqlDir(getMigrationPreviousDir());
        _migrationPreviousTakeFinallySqlFileList = schemaSqlDir.collectTakeFinallySqlFileList();
        return _migrationPreviousTakeFinallySqlFileList;
    }

    public Map<String, File> getMigrationPreviousTakeFinallySqlFileMap() {
        return convertToSchemaSqlFileMap(getMigrationPreviousTakeFinallySqlFileList());
    }

    // -----------------------------------------------------
    //                                      History Resource
    //                                      ----------------
    public String getMigrationHistoryDir() {
        final String baseDirectory = getMigrationDir();
        return baseDirectory + "/history";
    }

    // -----------------------------------------------------
    //                                       Schema Resource
    //                                       ---------------
    public String getMigrationSchemaDirectory() {
        return getMigrationDir() + "/schema";
    }

    public String getMigrationAlterCheckPreviousSchemaXml() {
        final String baseDirectory = getMigrationSchemaDirectory();
        return baseDirectory + "/migration-previous-schema.xml";
    }

    public String getMigrationAlterCheckNextSchemaXml() {
        final String baseDirectory = getMigrationSchemaDirectory();
        return baseDirectory + "/migration-next-schema.xml";
    }

    public String getMigrationAlterCheckDiffMapFile() {
        final String baseDirectory = getMigrationSchemaDirectory();
        return baseDirectory + "/migration-alter-check.diffmap";
    }

    public String getMigrationAlterCheckResultFileName() {
        return "alter-check-result.html";
    }

    public String getMigrationAlterCheckResultFilePath() {
        final String baseDirectory = getMigrationSchemaDirectory();
        return baseDirectory + "/" + getMigrationAlterCheckResultFileName();
    }

    public String getMigrationAlterCheckCraftMetaDir() {
        if (!getDocumentProperties().isCheckCraftDiff()) {
            return null;
        }
        final String baseDirectory = getMigrationSchemaDirectory();
        return baseDirectory + "/craftdiff";
    }

    // -----------------------------------------------------
    //                                         Mark Resource
    //                                         -------------
    public String getMigrationAlterCheckMark() {
        return doGetMigrationMark("alter-check.dfmark");
    }

    public boolean hasMigrationAlterCheckMark() {
        return doHasMigrationMark(getMigrationAlterCheckMark());
    }

    public String getMigrationSavePreviousMark() {
        return doGetMigrationMark("save-previous.dfmark");
    }

    public boolean hasMigrationSavePreviousMark() {
        return doHasMigrationMark(getMigrationSavePreviousMark());
    }

    public String getMigrationPreviousOKMark() {
        return doGetMigrationMark("previous-OK.dfmark");
    }

    public boolean hasMigrationPreviousOKMark() {
        return doHasMigrationMark(getMigrationPreviousOKMark());
    }

    public String getMigrationNextNGMark() {
        return doGetMigrationMark("next-NG.dfmark");
    }

    public boolean hasMigrationNextNGMark() {
        return doHasMigrationMark(getMigrationNextNGMark());
    }

    public String getMigrationAlterNGMark() {
        return doGetMigrationMark("alter-NG.dfmark");
    }

    public boolean hasMigrationAlterNGMark() {
        return doHasMigrationMark(getMigrationAlterNGMark());
    }

    public String getMigrationPreviousNGMark() {
        return doGetMigrationMark("previous-NG.dfmark");
    }

    public boolean hasMigrationPreviousNGMark() {
        return doHasMigrationMark(getMigrationPreviousNGMark());
    }

    public String getMigrationCheckedAlterMarkBasicName() {
        return "checked-alter";
    }

    public String getMigrationSkippedAlterMarkBasicName() {
        return "skipped-alter";
    }

    public String getMigrationFinishedAlterMarkBasicName() {
        return "finished-alter";
    }

    protected String doGetMigrationMark(String pureName) {
        return getMigrationDir() + "/" + pureName;
    }

    protected boolean doHasMigrationMark(String markPath) {
        return new File(markPath).exists();
    }

    // ===================================================================================
    //                                                                  InitializeFirstSql
    //                                                                  ==================
    public List<String> getInitializeFirstSqlList() {
        final Object obj = getReplaceSchemaDefinitionMap().get("initializeFirstSqlList");
        if (obj == null) {
            return DfCollectionUtil.emptyList();
        }
        if (!(obj instanceof List<?>)) {
            String msg = "The property 'initializeFirstSqlList' should be List<String>:";
            msg = msg + " type=" + obj.getClass();
            throw new DfIllegalPropertyTypeException(msg);
        }
        @SuppressWarnings("unchecked")
        final List<String> strList = (List<String>) obj;
        return strList;
    }

    // ===================================================================================
    //                                                              Drop Table Except List
    //                                                              ======================
    protected List<String> _dropTableExceptList;

    @SuppressWarnings("unchecked")
    public List<String> getDropTableExceptList() { // closet
        if (_dropTableExceptList != null) {
            return _dropTableExceptList;
        }
        _dropTableExceptList = (List<String>) getReplaceSchemaDefinitionMap().get("dropTableExceptList");
        if (_dropTableExceptList == null) {
            _dropTableExceptList = new ArrayList<String>();
        }
        return _dropTableExceptList;
    }

    protected List<String> _dropSequenceExceptList;

    @SuppressWarnings("unchecked")
    public List<String> getDropSequenceExceptList() { // closet
        if (_dropSequenceExceptList != null) {
            return _dropSequenceExceptList;
        }
        _dropSequenceExceptList = (List<String>) getReplaceSchemaDefinitionMap().get("dropSequenceExceptList");
        if (_dropSequenceExceptList == null) {
            _dropSequenceExceptList = new ArrayList<String>();
        }
        return _dropSequenceExceptList;
    }

    protected List<String> _dropProcedureExceptList;

    @SuppressWarnings("unchecked")
    public List<String> getDropProcedureExceptList() { // closet
        if (_dropProcedureExceptList != null) {
            return _dropProcedureExceptList;
        }
        _dropProcedureExceptList = (List<String>) getReplaceSchemaDefinitionMap().get("dropProcedureExceptList");
        if (_dropProcedureExceptList == null) {
            _dropProcedureExceptList = new ArrayList<String>();
        }
        return _dropProcedureExceptList;
    }

    // ===================================================================================
    //                                                                     Tool Terminator
    //                                                                     ===============
    public String resolveTerminator4Tool() {
        return getBasicProperties().isDatabaseOracle() ? "/" : null;
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String replaceString(String text, String fromText, String toText) {
        return DfStringUtil.replace(text, fromText, toText);
    }
}