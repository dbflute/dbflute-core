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
package org.dbflute.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.dbflute.exception.DfIllegalPropertyTypeException;
import org.dbflute.friends.velocity.DfGenerator;
import org.dbflute.logic.generate.language.DfLanguageDependency;
import org.dbflute.logic.sql2entity.analyzer.DfOutsideSqlLocation;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.DfStringUtil;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 0.7.5 (2008/06/25 Wednesday)
 */
public final class DfOutsideSqlProperties extends DfAbstractHelperProperties {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The logger instance for this class. (NotNull) */
    private static final Logger _log = LoggerFactory.getLogger(DfOutsideSqlProperties.class);

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfOutsideSqlProperties(Properties prop) {
        super(prop);
    }

    // ===================================================================================
    //                                                             outsideSqlDefinitionMap
    //                                                             =======================
    public static final String KEY_outsideSqlMap = "outsideSqlMap";
    public static final String KEY_oldOutsideSqlMap = "outsideSqlDefinitionMap";
    protected Map<String, Object> _outsideSqlDefinitionMap;

    protected Map<String, Object> getOutsideSqlDefinitionMap() {
        if (_outsideSqlDefinitionMap == null) {
            Map<String, Object> map = mapProp("torque." + KEY_outsideSqlMap, null);
            if (map == null) {
                map = mapProp("torque." + KEY_oldOutsideSqlMap, DEFAULT_EMPTY_MAP); // for compatible
            }
            _outsideSqlDefinitionMap = newLinkedHashMap();
            _outsideSqlDefinitionMap.putAll(map);
        }
        return _outsideSqlDefinitionMap;
    }

    public boolean isProperty(String key, boolean defaultValue) {
        return isProperty(key, defaultValue, getOutsideSqlDefinitionMap());
    }

    // ===================================================================================
    //                                                             Procedure ParameterBean
    //                                                             =======================
    public boolean isGenerateProcedureParameterBean() {
        return isProperty("isGenerateProcedureParameterBean", false);
    }

    public boolean isGenerateProcedureCustomizeEntity() {
        return isProperty("isGenerateProcedureCustomizeEntity", false);
    }

    protected List<String> _targetProcedureCatalogList;

    protected List<String> getTargetProcedureCatalogList() {
        if (_targetProcedureCatalogList != null) {
            return _targetProcedureCatalogList;
        }
        _targetProcedureCatalogList = getOutsideSqlPropertyAsList("targetProcedureCatalogList");
        if (_targetProcedureCatalogList == null) {
            _targetProcedureCatalogList = DfCollectionUtil.emptyList();
        }
        return _targetProcedureCatalogList;
    }

    public boolean isTargetProcedureCatalog(String procedureCatalog) {
        final List<String> targetProcedureList = getTargetProcedureCatalogList();
        if (targetProcedureList == null || targetProcedureList.isEmpty()) {
            return true;
        }
        if (procedureCatalog == null || procedureCatalog.trim().length() == 0) {
            if (targetProcedureList.contains("$$DEFAULT$$")) {
                return true;
            } else {
                return false;
            }
        }
        for (String catalogHint : targetProcedureList) {
            if (isHitByTheHint(procedureCatalog, catalogHint)) {
                return true;
            }
        }
        return false;
    }

    protected List<String> _targetProcedureSchemaList;

    protected List<String> getTargetProcedureSchemaList() {
        if (_targetProcedureSchemaList != null) {
            return _targetProcedureSchemaList;
        }
        _targetProcedureSchemaList = getOutsideSqlPropertyAsList("targetProcedureSchemaList");
        if (_targetProcedureSchemaList == null) {
            _targetProcedureSchemaList = DfCollectionUtil.emptyList();
        }
        return _targetProcedureSchemaList;
    }

    public boolean isTargetProcedureSchema(String procedureSchema) {
        final List<String> targetProcedureList = getTargetProcedureSchemaList();
        if (targetProcedureList == null || targetProcedureList.isEmpty()) {
            return true;
        }
        if (procedureSchema == null || procedureSchema.trim().length() == 0) {
            if (targetProcedureList.contains("$$DEFAULT$$")) {
                return true;
            } else {
                return false;
            }
        }
        for (String schemaHint : targetProcedureList) {
            if (isHitByTheHint(procedureSchema, schemaHint)) {
                return true;
            }
        }
        return false;
    }

    protected List<String> _targetProcedureNameList;
    protected List<String> _targetProcedureNameToDBLinkList;

    protected List<String> getTargetProcedureNameList() {
        if (_targetProcedureNameList != null) {
            return _targetProcedureNameList;
        }
        final List<String> propertyList = getOutsideSqlPropertyAsList("targetProcedureNameList");
        if (propertyList != null) {
            _targetProcedureNameList = DfCollectionUtil.newArrayList();
            _targetProcedureNameToDBLinkList = DfCollectionUtil.newArrayList();
            for (String property : propertyList) {
                if (isTargetProcedureToDBLink(property)) {
                    _targetProcedureNameToDBLinkList.add(property);
                } else {
                    _targetProcedureNameList.add(property);
                }
            }
        } else {
            _targetProcedureNameList = DfCollectionUtil.emptyList();
            _targetProcedureNameToDBLinkList = DfCollectionUtil.emptyList();
        }
        return _targetProcedureNameList;
    }

    public boolean isTargetProcedureName(String procedureName) {
        final String filteredName = filterMatchProcedureName(procedureName);
        final List<String> targetProcedureList = getTargetProcedureNameList();
        if (targetProcedureList == null || targetProcedureList.isEmpty()) {
            return true;
        }
        for (String procedureNameHint : targetProcedureList) {
            if (isHitByTheHint(filteredName, procedureNameHint)) {
                return true;
            }
        }
        return false;
    }

    public String filterMatchProcedureName(String procedureName) {
        final String filteredName;
        if (getBasicProperties().isDatabaseSQLServer()) {
            // SQLServer returns 'sp_foo;1'
            filteredName = Srl.substringLastFront(procedureName, ";");
        } else {
            filteredName = procedureName;
        }
        return filteredName;
    }

    public List<String> getTargetProcedureNameToDBLinkList() {
        getTargetProcedureNameList(); // initialize
        return _targetProcedureNameToDBLinkList;
    }

    protected boolean isTargetProcedureToDBLink(String name) {
        if (getBasicProperties().isDatabaseOracle()) {
            return name.contains("@") && !name.startsWith("@") && !name.endsWith("@");
        }
        return false;
    }

    protected List<String> _executionMetaProcedureNameList;

    protected List<String> getExecutionMetaProcedureNameList() {
        if (_executionMetaProcedureNameList != null) {
            return _executionMetaProcedureNameList;
        }
        _executionMetaProcedureNameList = getOutsideSqlPropertyAsList("executionMetaProcedureNameList");
        if (_executionMetaProcedureNameList == null) {
            _executionMetaProcedureNameList = DfCollectionUtil.emptyList();
        }
        return _executionMetaProcedureNameList;
    }

    public boolean hasSpecifiedExecutionMetaProcedure() {
        return !getExecutionMetaProcedureNameList().isEmpty();
    }

    public boolean isExecutionMetaProcedureName(String procedureName) {
        final String filteredName = filterMatchProcedureName(procedureName);
        final List<String> executionMetaProcedureList = getExecutionMetaProcedureNameList();
        if (executionMetaProcedureList == null || executionMetaProcedureList.isEmpty()) {
            return true;
        }
        for (String procedureNameHint : executionMetaProcedureList) {
            if (isHitByTheHint(filteredName, procedureNameHint)) {
                return true;
            }
        }
        return false;
    }

    public ProcedureSynonymHandlingType getProcedureSynonymHandlingType() {
        final String key = "procedureSynonymHandlingType";
        final String property = getProperty(key, ProcedureSynonymHandlingType.NONE.name(), getOutsideSqlDefinitionMap());
        if (property.equalsIgnoreCase(ProcedureSynonymHandlingType.NONE.name())) {
            return ProcedureSynonymHandlingType.NONE;
        } else if (property.equalsIgnoreCase(ProcedureSynonymHandlingType.INCLUDE.name())) {
            return ProcedureSynonymHandlingType.INCLUDE;
        } else if (property.equalsIgnoreCase(ProcedureSynonymHandlingType.SWITCH.name())) {
            return ProcedureSynonymHandlingType.SWITCH;
        } else {
            String msg = "The property was out of scope for " + key + ": value=" + property;
            throw new DfIllegalPropertyTypeException(msg);
        }
    }

    public static enum ProcedureSynonymHandlingType {
        NONE, INCLUDE, SWITCH
    }

    // ===================================================================================
    //                                                                      OutsideSqlTest
    //                                                                      ==============
    public boolean isCheckRequiredSqlCommentAlsoSql2Entity() { // closet
        final boolean defaultValue = !isCompatibleOutsideSqlSqlCommentCheckDefault();
        return isProperty("isCheckRequiredSqlCommentAlsoSql2Entity", defaultValue);
    }

    public boolean isRequiredSqlTitle() {
        final boolean defaultValue = !isCompatibleOutsideSqlSqlCommentCheckDefault();
        return isProperty("isRequiredSqlTitle", defaultValue);
    }

    public boolean isSuppressSqlTitleUniqueCheck() { // closet
        return isProperty("isSuppressSqlTitleUniqueCheck", false);
    }

    public boolean isRequiredSqlDescription() {
        final boolean defaultValue = !isCompatibleOutsideSqlSqlCommentCheckDefault();
        return isProperty("isRequiredSqlDescription", defaultValue);
    }

    public boolean isSuppressSqlDescriptionUniqueCheck() { // closet
        return isProperty("isSuppressSqlDescriptionUniqueCheck", false);
    }

    protected boolean isCompatibleOutsideSqlSqlCommentCheckDefault() {
        return getLittleAdjustmentProperties().isCompatibleOutsideSqlSqlCommentCheckDefault();
    }

    public boolean isSuppressParameterCommentCheck() { // closet
        return isProperty("isSuppressParameterCommentCheck", false);
    }

    // ===================================================================================
    //                                                                     SqlFileEncoding
    //                                                                     ===============
    public boolean hasSqlFileEncoding() {
        final String encoding = getSqlFileEncoding();
        return encoding != null && encoding.trim().length() > 0 && !encoding.trim().equalsIgnoreCase("null");
    }

    public String getSqlFileEncoding() {
        final String value = (String) getOutsideSqlDefinitionMap().get("sqlFileEncoding");
        if (value != null && value.trim().length() > 0 && !value.trim().equalsIgnoreCase("null")) {
            return value;
        }
        return "UTF-8";
    }

    // ===================================================================================
    //                                                                         SqlLocation
    //                                                                         ===========
    protected List<DfOutsideSqlLocation> _outsideSqlLocationList;

    public List<DfOutsideSqlLocation> getSqlLocationList() {
        if (_outsideSqlLocationList != null) {
            return _outsideSqlLocationList;
        }
        _outsideSqlLocationList = new ArrayList<DfOutsideSqlLocation>();
        final String mainProjectName = "main"; // basically unused
        final String mainDir = getMainSqlDirectory();
        final String mainOutput = getSql2EntityOutputDirectory();
        _outsideSqlLocationList.add(createOutsideSqlLocation(mainProjectName, mainDir, mainOutput, false, false));
        final Object obj = getApplicationOutsideSqlMap();
        if (!(obj instanceof Map<?, ?>)) {
            String msg = "The property 'applicationOutsideSqlMap' should be Map: " + obj.getClass();
            throw new DfIllegalPropertyTypeException(msg);
        }
        @SuppressWarnings("unchecked")
        final Map<String, Map<String, String>> sqlApMap = (Map<String, Map<String, String>>) obj;
        if (sqlApMap.isEmpty()) {
            return _outsideSqlLocationList;
        }
        final DfLanguageDependency lang = getBasicProperties().getLanguageDependency();
        final String defaultSqlDirectory = lang.getOutsideSqlDirectory();
        final String defaultMainProgramDirectory = lang.getMainProgramDirectory();
        for (Entry<String, Map<String, String>> entry : sqlApMap.entrySet()) {
            final String applicationDir = entry.getKey();
            final Map<String, String> elementMap = entry.getValue();

            // basically for display
            final String projectName;
            {
                final String lastName = Srl.substringLastRear(applicationDir, "/");
                if (Srl.is_Null_or_TrimmedEmpty(lastName) || Srl.equalsPlain(lastName, ".", "..")) {
                    projectName = applicationDir;
                } else {
                    projectName = lastName;
                }
            }

            final String sqlDirectory;
            {
                String plainDir = elementMap.get("sqlDirectory");
                if (Srl.is_Null_or_TrimmedEmpty(plainDir)) {
                    plainDir = defaultSqlDirectory;
                }
                sqlDirectory = doGetSqlDirectory(applicationDir + "/" + plainDir);
            }

            final String sql2EntityOutputDirectory;
            {
                String plainDir = elementMap.get("sql2EntityOutputDirectory");
                if (Srl.is_Null_or_TrimmedEmpty(plainDir)) {
                    plainDir = defaultMainProgramDirectory;
                }
                sql2EntityOutputDirectory = applicationDir + "/" + plainDir;
            }
            final boolean suppressDirectoryCheck;
            {
                String value = elementMap.get("isSuppressDirectoryCheck");
                if (Srl.is_NotNull_and_NotTrimmedEmpty(value) && value.trim().equalsIgnoreCase("true")) {
                    suppressDirectoryCheck = true;
                } else {
                    suppressDirectoryCheck = false;
                }
            }

            final DfOutsideSqlLocation sqlApLocation =
                    createOutsideSqlLocation(projectName, sqlDirectory, sql2EntityOutputDirectory, true, suppressDirectoryCheck);
            _outsideSqlLocationList.add(sqlApLocation);
        }
        return _outsideSqlLocationList;
    }

    protected Object getApplicationOutsideSqlMap() {
        final String key = "applicationOutsideSqlMap";
        @SuppressWarnings("unchecked")
        Map<String, Map<String, String>> map = (Map<String, Map<String, String>>) getOutsideSqlDefinitionMap().get(key);
        if (map == null) {
            map = newLinkedHashMap();
        }
        getLastaFluteProperties().reflectApplicationOutsideSqlMap(map);
        return map;
    }

    protected String getMainSqlDirectory() { // for main
        final String plainDir = (String) getOutsideSqlDefinitionMap().get("sqlDirectory");
        return doGetSqlDirectory(plainDir);
    }

    protected String doGetSqlDirectory(String plainDir) {
        if (plainDir == null || plainDir.trim().length() == 0) {
            plainDir = getDefaultSqlDirectory();
        }
        plainDir = removeEndSeparatorIfNeeds(plainDir);
        String sqlPackage = getSqlPackage();
        if (sqlPackage != null && sqlPackage.trim().length() > 0) {
            String sqlPackageDirectory = resolveSqlPackageFileSeparator(sqlPackage);
            plainDir = plainDir + "/" + removeStartSeparatorIfNeeds(sqlPackageDirectory);
        }
        return plainDir;
    }

    protected DfOutsideSqlLocation createOutsideSqlLocation(String projectName, String sqlDirectory, String sql2EntityOutputDirectory,
            boolean sqlAp, boolean suppressDirectoryCheck) {
        return new DfOutsideSqlLocation(projectName, sqlDirectory, sql2EntityOutputDirectory, sqlAp, suppressDirectoryCheck);
    }

    // -----------------------------------------------------
    //                                      Remove Separator
    //                                      ----------------
    protected String removeStartSeparatorIfNeeds(String path) {
        if (path.startsWith("/")) {
            return path.substring("/".length());
        }
        return path;
    }

    protected String removeEndSeparatorIfNeeds(String path) {
        if (path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }

    // -----------------------------------------------------
    //                                  Default SqlDirectory
    //                                  --------------------
    /**
     * @return The default directory of SQL. (NotNull)
     */
    protected String getDefaultSqlDirectory() {
        return getBasicProperties().getGenerateOutputDirectory();
    }

    // -----------------------------------------------------
    //                               Resolve SqlPackage Path
    //                               -----------------------
    protected String resolveSqlPackageFileSeparator(String sqlPackage) {
        final DfBasicProperties prop = getBasicProperties();
        if (!prop.isFlatDirectoryPackageValid()) {
            return replaceDotToSeparator(sqlPackage);
        }
        final String flatDirectoryPackage = prop.getFlatDirectoryPackage();
        if (!sqlPackage.contains(flatDirectoryPackage)) {
            return replaceDotToSeparator(sqlPackage);
        }
        return resolveSqlPackageFileSeparatorWithFlatDirectory(sqlPackage, flatDirectoryPackage);
    }

    protected String resolveSqlPackageFileSeparatorWithFlatDirectory(String sqlPackage, String flatDirectoryPackage) {
        final int startIndex = sqlPackage.indexOf(flatDirectoryPackage);
        String front = sqlPackage.substring(0, startIndex);
        String rear = sqlPackage.substring(startIndex + flatDirectoryPackage.length());
        front = replaceDotToSeparator(front);
        rear = replaceDotToSeparator(rear);
        return front + flatDirectoryPackage + rear;
    }

    protected String replaceDotToSeparator(String sqlPackage) {
        return DfStringUtil.replace(sqlPackage, ".", "/");
    }

    // -----------------------------------------------------
    //                                       OutputDirectory
    //                                       ---------------
    public String getSql2EntityOutputDirectory() { // for main
        final String defaultDir = getBasicProperties().getGenerateOutputDirectory();
        final String value = (String) getOutsideSqlDefinitionMap().get("sql2EntityOutputDirectory");
        return value != null && value.trim().length() > 0 ? value : defaultDir;
    }

    public void switchSql2EntityOutputDirectory(String outputDirectory) {
        final DfGenerator generator = getGeneratorInstance();
        final String outputPath = generator.getOutputPath();
        if (outputDirectory == null) { // means back to Sql2Entity output directory
            final String mainOutDir = getSql2EntityOutputDirectory();
            if (!outputPath.equals(mainOutDir)) { // if different
                _log.info("...Switching sql2EntityOutputDirectory: " + mainOutDir);
                generator.setOutputPath(mainOutDir); // back to library project
            }
        } else if (!outputPath.equals(outputDirectory)) { // if different
            _log.info("...Switching sql2EntityOutputDirectory: " + outputDirectory);
            generator.setOutputPath(outputDirectory);
        }
    }

    protected DfGenerator getGeneratorInstance() {
        return DfGenerator.getInstance();
    }

    // ===================================================================================
    //                                                                          SqlPackage
    //                                                                          ==========
    public boolean isSqlPackageValid() {
        final String sqlPackage = getSqlPackage();
        return sqlPackage != null && sqlPackage.trim().length() > 0 && !sqlPackage.trim().equalsIgnoreCase("null");
    }

    public String getSqlPackage() {
        String sqlPackage = (String) getOutsideSqlDefinitionMap().get("sqlPackage");
        if (sqlPackage == null || sqlPackage.trim().length() == 0) {
            sqlPackage = getDefaultSqlPackage();
        }
        return resolvePackageBaseMarkIfNeeds(sqlPackage);
    }

    protected String getDefaultSqlPackage() {
        return "";
    }

    protected String resolvePackageBaseMarkIfNeeds(String sqlPackage) {
        String packageBase = getBasicProperties().getPackageBase();
        return DfStringUtil.replace(sqlPackage, "$$PACKAGE_BASE$$", packageBase);
    }

    // ===================================================================================
    //                                                                      Package Detail
    //                                                                      ==============
    protected String getSpecifiedBaseCustomizeEntityPackage() { // It's closet!
        final String value = (String) getOutsideSqlDefinitionMap().get("baseCustomizeEntityPackage");
        return (value != null && value.trim().length() > 0) ? value : null;
    }

    protected String getSpecifiedExtendedCustomizeEntityPackage() { // It's closet!
        final String value = (String) getOutsideSqlDefinitionMap().get("extendedCustomizeEntityPackage");
        return (value != null && value.trim().length() > 0) ? value : null;
    }

    protected String getSpecifiedBaseParameterBeanPackage() { // It's closet!
        final String value = (String) getOutsideSqlDefinitionMap().get("baseParameterBeanPackage");
        return (value != null && value.trim().length() > 0) ? value : null;
    }

    protected String getSpecifiedExtendedParameterBeanPackage() { // It's closet!
        final String value = (String) getOutsideSqlDefinitionMap().get("extendedParameterBeanPackage");
        return (value != null && value.trim().length() > 0) ? value : null;
    }

    public String getBaseEntityPackage() {
        final String specifiedPackage = getSpecifiedBaseCustomizeEntityPackage();
        if (specifiedPackage != null && specifiedPackage.trim().length() != 0) {
            return specifiedPackage;
        }
        final String baseEntityPackage = getBasicProperties().getBaseEntityPackage();
        final String defaultPackage = baseEntityPackage + "." + getCustomizeEntitySimplePackage();
        if (defaultPackage != null && defaultPackage.trim().length() != 0) {
            return defaultPackage;
        } else {
            String msg = "Both packageString in sql2entity-property and baseEntityPackage are null.";
            throw new IllegalStateException(msg);
        }
    }

    public String getDBMetaPackage() {
        final DfLanguageDependency lang = getBasicProperties().getLanguageDependency();
        final String dbmetaPackage = lang.getLanguageClassPackage().getDBMetaSimplePackage();
        return getBaseEntityPackage() + "." + dbmetaPackage;
    }

    public String getExtendedEntityPackage() {
        final String specifiedPackage = getSpecifiedExtendedCustomizeEntityPackage();
        if (specifiedPackage != null && specifiedPackage.trim().length() != 0) {
            return specifiedPackage;
        }
        final String extendedEntityPackage = getBasicProperties().getExtendedEntityPackage();
        final String defaultPackage = extendedEntityPackage + "." + getCustomizeEntitySimplePackage();
        if (defaultPackage != null && defaultPackage.trim().length() != 0) {
            return defaultPackage;
        } else {
            String msg = "Both packageString in sql2entity-property and extendedEntityPackage are null.";
            throw new IllegalStateException(msg);
        }
    }

    protected String getCustomizeEntitySimplePackage() {
        return getBasicProperties().getLanguageDependency().getLanguageClassPackage().getCustomizeEntitySimplePackage();
    }

    public String getBaseCursorPackage() {
        if (isMakeDaoInterface()) {
            return getBasicProperties().getBaseDaoPackage() + "." + getCursorSimplePackage();
        } else {
            return getBasicProperties().getBaseBehaviorPackage() + "." + getCursorSimplePackage();
        }
    }

    public String getExtendedCursorPackage() {
        if (isMakeDaoInterface()) {
            return getBasicProperties().getExtendedDaoPackage() + "." + getCursorSimplePackage();
        } else {
            return getBasicProperties().getExtendedBehaviorPackage() + "." + getCursorSimplePackage();
        }
    }

    protected String getCursorSimplePackage() {
        return getBasicProperties().getLanguageDependency().getLanguageClassPackage().getCursorSimplePackage();
    }

    public String getBaseParameterBeanPackage() {
        final String specifiedPackage = getSpecifiedBaseParameterBeanPackage();
        if (specifiedPackage != null && specifiedPackage.trim().length() != 0) {
            return specifiedPackage;
        }

        final String defaultPackage;
        if (isMakeDaoInterface()) {
            defaultPackage = getBasicProperties().getBaseDaoPackage() + "." + getPmbeanPackageName();
        } else {
            defaultPackage = getBasicProperties().getBaseBehaviorPackage() + "." + getPmbeanPackageName();
        }
        if (defaultPackage != null && defaultPackage.trim().length() != 0) {
            return defaultPackage;
        } else {
            String msg = "Both packageString in sql2entity-property and baseEntityPackage are null.";
            throw new IllegalStateException(msg);
        }
    }

    public String getExtendedParameterBeanPackage() {
        final String specifiedPackage = getSpecifiedExtendedParameterBeanPackage();
        if (specifiedPackage != null && specifiedPackage.trim().length() != 0) {
            return specifiedPackage;
        }
        final String defaultPackage;
        if (isMakeDaoInterface()) {
            defaultPackage = getBasicProperties().getExtendedDaoPackage() + "." + getPmbeanPackageName();
        } else {
            defaultPackage = getBasicProperties().getExtendedBehaviorPackage() + "." + getPmbeanPackageName();
        }
        if (defaultPackage != null && defaultPackage.trim().length() != 0) {
            return defaultPackage;
        } else {
            String msg = "Both packageString in sql2entity-property and extendedEntityPackage are null.";
            throw new IllegalStateException(msg);
        }
    }

    protected String getPmbeanPackageName() {
        return getBasicProperties().getLanguageDependency().getLanguageClassPackage().getParameterBeanSimplePackage();
    }

    protected boolean isMakeDaoInterface() {
        return getLittleAdjustmentProperties().isMakeDaoInterface();
    }

    // ===================================================================================
    //                                                                   BehaviorQueryPath
    //                                                                   =================
    public boolean isSuppressBehaviorQueryPath() { // It's closet!
        return isProperty("isSuppressBehaviorQueryPath", false);
    }

    // ===================================================================================
    //                                                                      DefaultPackage
    //                                                                      ==============
    public boolean isDefaultPackageValid() { // C# only
        return getDefaultPackage() != null && getDefaultPackage().trim().length() > 0
                && !getDefaultPackage().trim().equalsIgnoreCase("null");
    }

    public String getDefaultPackage() { // C# only
        return (String) getOutsideSqlDefinitionMap().get("defaultPackage");
    }

    // ===================================================================================
    //                                                             OmitResourcePathPackage
    //                                                             =======================
    public boolean isOmitResourcePathPackageValid() { // C# only
        return getOmitResourcePathPackage() != null && getOmitResourcePathPackage().trim().length() > 0
                && !getOmitResourcePathPackage().trim().equalsIgnoreCase("null");
    }

    public String getOmitResourcePathPackage() { // C# only
        return (String) getOutsideSqlDefinitionMap().get("omitResourcePathPackage");
    }

    // ===================================================================================
    //                                                           OmitFileSystemPathPackage
    //                                                           =========================
    public boolean isOmitFileSystemPathPackageValid() { // C# only
        return getOmitFileSystemPathPackage() != null && getOmitFileSystemPathPackage().trim().length() > 0
                && !getOmitFileSystemPathPackage().trim().equalsIgnoreCase("null");
    }

    public String getOmitFileSystemPathPackage() { // C# only
        return (String) getOutsideSqlDefinitionMap().get("omitFileSystemPathPackage");
    }

    // ===================================================================================
    //                                                                     Property Helper
    //                                                                     ===============
    @SuppressWarnings("unchecked")
    protected List<String> getOutsideSqlPropertyAsList(String key) {
        return (List<String>) getOutsideSqlDefinitionMap().get(key);
    }
}