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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.dbflute.dbway.DBDef;
import org.dbflute.exception.DfIllegalPropertySettingException;
import org.dbflute.exception.DfIllegalPropertyTypeException;
import org.dbflute.exception.DfRequiredPropertyNotFoundException;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.infra.core.DfDatabaseNameMapping;
import org.dbflute.logic.generate.language.DfLanguageDependency;
import org.dbflute.logic.generate.language.DfLanguageDependencyCSharp;
import org.dbflute.logic.generate.language.DfLanguageDependencyJava;
import org.dbflute.logic.generate.language.DfLanguageDependencyPhp;
import org.dbflute.logic.generate.language.DfLanguageDependencyScala;
import org.dbflute.logic.generate.language.pkgstyle.DfLanguageClassPackage;
import org.dbflute.properties.facade.DfDatabaseTypeFacadeProp;
import org.dbflute.properties.facade.DfSchemaXmlFacadeProp;
import org.dbflute.util.Srl;

/**
 * The basic properties for DBFlute property. <br>
 * This class is very important for DBFlute.
 * @author jflute
 */
public final class DfBasicProperties extends DfAbstractDBFluteProperties {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected DfLanguageDependency _languageDependency;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * @param prop Properties. (NotNull)
     */
    public DfBasicProperties(Properties prop) {
        super(prop);
    }

    // ===================================================================================
    //                                                                      Basic Info Map
    //                                                                      ==============
    public static final String KEY_basicInfoMap = "basicInfoMap";
    protected Map<String, Object> _basicInfoMap;

    public Map<String, Object> getBasicInfoMap() {
        if (_basicInfoMap == null) {
            final Map<String, Object> map = mapProp("torque." + KEY_basicInfoMap, DEFAULT_EMPTY_MAP);
            _basicInfoMap = newLinkedHashMap();
            _basicInfoMap.putAll(map);
        }
        return _basicInfoMap;
    }

    public String getProperty(String key, String defaultValue) {
        return _propertyValueHandler.getPropertyIfNotBuildProp(key, defaultValue, getBasicInfoMap());
    }

    public boolean isProperty(String key, boolean defaultValue) {
        return isProperty(key, defaultValue, getBasicInfoMap());
    }

    public void checkBasicInfo() {
        getTargetDatabase(); // checked in the method
    }

    // ===================================================================================
    //                                                                             Project
    //                                                                             =======
    public static final String KEY_project = "project";

    public String getProjectName() {
        final String property = getProperty(KEY_project, null);
        if (property != null) {
            return property; // can be switched for environments
        }
        return stringProp("torque.project", ""); // from build-properties!
    }

    // ===================================================================================
    //                                                                            Database
    //                                                                            ========
    protected DfDatabaseTypeFacadeProp _databaseTypeFacadeProp;

    public DfDatabaseTypeFacadeProp getDatabaseTypeFacadeProp() {
        if (_databaseTypeFacadeProp != null) {
            return _databaseTypeFacadeProp;
        }
        _databaseTypeFacadeProp = new DfDatabaseTypeFacadeProp(this);
        return _databaseTypeFacadeProp;
    }

    public String getTargetDatabase() {
        final String database = getProperty("database", null);
        if (database == null || database.trim().length() == 0) {
            throwBasicInfoDatabaseNotFoundException(database);
        }
        return database;
    }

    protected void throwBasicInfoDatabaseNotFoundException(String returnedValue) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the property 'database' in basicInfoMap.dfprop.");
        br.addItem("Returned Value");
        br.addElement(returnedValue);
        br.addItem("Map");
        final Map<String, Object> basicInfoMap = getBasicInfoMap();
        for (Entry<String, Object> entry : basicInfoMap.entrySet()) {
            br.addElement(entry.getKey() + " = " + entry.getValue());
        }
        final String msg = br.buildExceptionMessage();
        throw new DfRequiredPropertyNotFoundException(msg);
    }

    protected DBDef _currentDBDef;

    public DBDef getCurrentDBDef() {
        if (_currentDBDef != null) {
            return _currentDBDef;
        }
        final DfDatabaseNameMapping databaseNameMapping = DfDatabaseNameMapping.getInstance();
        _currentDBDef = databaseNameMapping.findDBDef(getTargetDatabase());
        return _currentDBDef;
    }

    public boolean isDatabaseMySQL() {
        return getTargetDatabase().equalsIgnoreCase("mysql");
    }

    public boolean isDatabasePostgreSQL() {
        return getTargetDatabase().equalsIgnoreCase("postgresql");
    }

    public boolean isDatabaseOracle() {
        return getTargetDatabase().equalsIgnoreCase("oracle");
    }

    public boolean isDatabaseDB2() {
        return getTargetDatabase().equalsIgnoreCase("db2");
    }

    public boolean isDatabaseSQLServer() {
        final String database = getTargetDatabase();
        if (database.equalsIgnoreCase("sqlserver")) {
            return true;
        }
        return database.equalsIgnoreCase("mssql"); // for compatible
    }

    public boolean isDatabaseH2() {
        return getTargetDatabase().equalsIgnoreCase("h2");
    }

    public boolean isDatabaseDerby() {
        return getTargetDatabase().equalsIgnoreCase("derby");
    }

    public boolean isDatabaseSQLite() { // sub supported
        return getTargetDatabase().equalsIgnoreCase("sqlite");
    }

    public boolean isDatabaseMSAccess() { // sub supported
        return getTargetDatabase().equalsIgnoreCase("msaccess");
    }

    public boolean isDatabaseFirebird() { // a-little-bit supported
        return getTargetDatabase().equalsIgnoreCase("firebird");
    }

    public boolean isDatabaseSybase() { // a-little-bit supported
        return getTargetDatabase().equalsIgnoreCase("sybase");
    }

    public boolean isDatabase_Supported() {
        if (isDatabaseAsMainSupported() || isDatabaseAsSubSupported()) {
            return true;
        }
        return false;
    }

    public boolean isDatabaseAsMainSupported() {
        if (isDatabaseMySQL() || isDatabasePostgreSQL() || isDatabaseOracle() || isDatabaseDB2() || isDatabaseSQLServer() || isDatabaseH2()
                || isDatabaseDerby()) {
            return true;
        }
        return false;
    }

    public boolean isDatabaseAsSubSupported() {
        if (isDatabaseSQLite() || isDatabaseMSAccess()) {
            return true;
        }
        return false;
    }

    // /- - - - - - - - - - - - - - - - - - - -
    // simple DBMS definition about generating
    // - - - - - - - - - -/
    public boolean isDatabaseAsSchemaSpecificationOmittable() {
        return isDatabaseMySQL() || isDatabaseAsUnifiedSchemaUnsupported();
    }

    public boolean isDatabaseAsUnifiedSchemaUnsupported() {
        return isDatabaseSQLite() || isDatabaseMSAccess();
    }

    public boolean isDatabaseAsPrimaryKeyExtractingUnsupported() {
        return isDatabaseMSAccess();
    }

    public boolean isDatabaseAsForeignKeyExtractingUnsupported() {
        return isDatabaseMSAccess();
    }

    // -----------------------------------------------------
    //                                              Sub Type
    //                                              --------
    public DfSubTypeOnDatabase getSubTypeOnDatabase() {
        final String subType = getProperty("subTypeOnDatabase", null);
        if (subType == null) {
            return DfSubTypeOnDatabase.NO_TYPE;
        }
        if (isDatabaseSQLServer() && "localdb".equalsIgnoreCase(subType)) {
            return DfSubTypeOnDatabase.SQLSERVER_LOCALDB;
        }
        throwBasicInfoSubTypeOnDatabaseUnknownException(subType);
        return null; // unreachable
    }

    public static enum DfSubTypeOnDatabase {
        SQLSERVER_LOCALDB, NO_TYPE
    }

    protected void throwBasicInfoSubTypeOnDatabaseUnknownException(String subType) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Unknown value of the property 'subTypeOnDatabase' in basicInfoMap.dfprop.");
        br.addItem("Advice");
        br.addElement("Only supported types can be set.");
        br.addElement("For example, if database is 'sqlserver', databaseSubType can be 'localdb'.");
        br.addItem("Target Database");
        br.addElement(getTargetDatabase());
        br.addItem("SubType on Database");
        br.addElement(subType);
        final String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg);
    }

    public boolean isSubTypeOnDatabaseSQLServerLocalDB() {
        return DfSubTypeOnDatabase.SQLSERVER_LOCALDB.equals(getSubTypeOnDatabase());
    }

    // ===================================================================================
    //                                                                            Language
    //                                                                            ========
    public String getTargetLanguage() {
        return getProperty("targetLanguage", DEFAULT_targetLanguage);
    }

    public String getResourceDirectory() {
        return getTargetLanguage(); // same as language
    }

    public DfLanguageDependency getLanguageDependency() {
        if (_languageDependency != null) {
            return _languageDependency;
        }
        final DfLanguageDependency lang;
        if (isTargetLanguageJava()) {
            lang = new DfLanguageDependencyJava();
        } else if (isTargetLanguageCSharp()) {
            lang = new DfLanguageDependencyCSharp();
        } else if (isTargetLanguagePhp()) {
            lang = new DfLanguageDependencyPhp();
        } else if (isTargetLanguageScala()) {
            lang = new DfLanguageDependencyScala();
        } else {
            String msg = "The language is unsupported: " + getTargetLanguage();
            throw new IllegalStateException(msg);
        }
        _languageDependency = lang;
        return _languageDependency;
    }

    protected boolean isTargetLanguageJava() {
        return JAVA_targetLanguage.equals(getTargetLanguage());
    }

    protected boolean isTargetLanguageCSharp() {
        return CSHARP_targetLanguage.equals(getTargetLanguage());
    }

    protected boolean isTargetLanguagePhp() {
        return PHP_targetLanguage.equals(getTargetLanguage());
    }

    protected boolean isTargetLanguageScala() {
        return SCALA_targetLanguage.equals(getTargetLanguage());
    }

    // ===================================================================================
    //                                                                           Container
    //                                                                           =========
    public String getTargetContainerName() {
        final String containerName = getProperty("targetContainer", "lasta_di");
        checkContainer(containerName);
        return containerName;
    }

    public boolean isTargetContainerSeasar() {
        return getTargetContainerName().trim().equalsIgnoreCase("seasar");
    }

    public boolean isTargetContainerSpring() {
        return getTargetContainerName().trim().equalsIgnoreCase("spring");
    }

    public boolean isTargetContainerGuice() {
        return getTargetContainerName().trim().equalsIgnoreCase("guice");
    }

    public boolean isTargetContainerCDI() {
        return getTargetContainerName().trim().equalsIgnoreCase("cdi");
    }

    public boolean isTargetContainerLastaDi() {
        return getTargetContainerName().trim().equalsIgnoreCase("lasta_di");
    }

    public boolean isTargetContainerMicronaut() {
        return getTargetContainerName().trim().equalsIgnoreCase("micronaut");
    }

    protected void checkContainer(String containerName) {
        containerName = containerName.toLowerCase();
        if (Srl.equalsPlain(containerName, "seasar", "spring", "guice", "cdi", "lasta_di", "micronaut")) {
            return;
        }
        String msg = "The targetContainer is unknown: targetContainer=" + containerName;
        throw new IllegalStateException(msg);
    }

    // ===================================================================================
    //                                                                           SchemaXML
    //                                                                           =========
    protected DfSchemaXmlFacadeProp _schemaXmlFacadeProp;

    public DfSchemaXmlFacadeProp getSchemaXmlFacadeProp() {
        if (_schemaXmlFacadeProp != null) {
            return _schemaXmlFacadeProp;
        }
        _schemaXmlFacadeProp = new DfSchemaXmlFacadeProp(this);
        return _schemaXmlFacadeProp;
    }

    public String getProejctSchemaXMLFile() { // closet
        return getProperty("projectSchemaXMLFilePath", buildDefaultSchemaXMLFilePath());
    }

    protected String buildDefaultSchemaXMLFilePath() {
        final StringBuilder sb = new StringBuilder();
        final String projectName = getBasicProperties().getProjectName();
        sb.append("./schema/project-schema-").append(projectName).append(".xml");
        return sb.toString();
    }

    public String getProejctSchemaXMLEncoding() { // closet
        return getProperty("projectSchemaXMLEncoding", DEFAULT_projectSchemaXMLEncoding);
    }

    // -----------------------------------------------------
    //                                            Diff Piece
    //                                            ----------
    public String getProjectSchemaHistoryDiffPieceEnvType() { // closet, @since 1.3.0
        return getProperty("projectSchemaHistoryDiffPieceEnvType", null);
    }

    // -----------------------------------------------------
    //                                       Monolithic Diff
    //                                       ---------------
    public String getProjectSchemaHistoryFile() { // closet
        return getProperty("projectSchemaHistoryFilePath", buildDefaultSchemaHistoryFilePath());
    }

    protected String buildDefaultSchemaHistoryFilePath() {
        return "./schema/project-history-" + getBasicProperties().getProjectName() + ".diffmap";
    }

    // ===================================================================================
    //                                                                           Extension
    //                                                                           =========
    public String getTemplateFileExtension() { // It's not property!
        return getLanguageDependency().getTemplateFileExtension();
    }

    public String getClassFileExtension() { // It's not property!
        return getLanguageDependency().getLanguageGrammar().getClassFileExtension();
    }

    // ===================================================================================
    //                                                                    Generate Package
    //                                                                    ================
    public String getPackageBase() { // [packageBase].[bsbhv/exbhv/exentity...]
        return getProperty("packageBase", "");
    }

    public String getBaseClassPackage() { // [packageBase].[baseClassPackage].[bsbhv/bsentity...]
        return getProperty("baseClassPackage", "");
    }

    public String getExtendedClassPackage() { // [packageBase].[extendedClassPackage].[exbhv/exentity...]
        return getProperty("extendedClassPackage", "");
    }

    public String getBaseCommonPackage() {
        final String key = "baseCommonPackage";
        final String baseCommonPackage = getProperty(key, getPackageInfo().getBaseCommonPackage());
        if (isApplicationBehaviorProject()) {
            return getLibraryAllCommonPackage(); // basically for Sql2Entity task at BhvAp mode
        } else {
            return filterPackageBaseForBase(baseCommonPackage);
        }
    }

    public String getBaseBehaviorPackage() {
        return filterPackageBaseForBase(getProperty("baseBehaviorPackage", getPackageInfo().getBaseBehaviorPackage()));
    }

    public String getReferrerLoaderPackage() {
        return getBaseBehaviorPackage() + "." + getPackageInfo().getReferrerLoaderSimplePackage();
    }

    public String getBaseDaoPackage() {
        return filterPackageBaseForBase(getProperty("baseDaoPackage", getPackageInfo().getBaseDaoPackage()));
    }

    public String getBaseEntityPackage() {
        return filterPackageBaseForBase(getProperty("baseEntityPackage", getPackageInfo().getBaseEntityPackage()));
    }

    public String getDBMetaPackage() {
        return getBaseEntityPackage() + "." + getPackageInfo().getDBMetaSimplePackage();
    }

    public String getConditionBeanPackage() {
        return filterPackageBaseForBase(getPureConditionBeanPackage());
    }

    public String getExtendedConditionBeanPackage() {
        final String pkg = getProperty("extendedConditionBeanPackage", null);
        if (pkg != null) {
            return filterPackageBaseForExtended(pkg);
        }
        final String extendedClassPackage = getExtendedClassPackage();
        if (Srl.is_NotNull_and_NotTrimmedEmpty(extendedClassPackage)) {
            return filterPackageBaseForExtended(getPureConditionBeanPackage());
        }
        return getConditionBeanPackage(); // as default
    }

    public String getPureConditionBeanPackage() {
        return getProperty("conditionBeanPackage", getPackageInfo().getConditionBeanPackage());
    }

    public String getExtendedBehaviorPackage() {
        final String packageString = getProperty("extendedBehaviorPackage", getPackageInfo().getExtendedBehaviorPackage());
        return filterPackageBaseForExtended(packageString);
    }

    public String getExtendedDaoPackage() {
        final String packageString = getProperty("extendedDaoPackage", getPackageInfo().getExtendedDaoPackage());
        return filterPackageBaseForExtended(packageString);
    }

    public String getExtendedEntityPackage() {
        final String packageString = getProperty("extendedEntityPackage", getPackageInfo().getExtendedEntityPackage());
        return filterPackageBaseForExtended(packageString);
    }

    protected String filterPackageBaseForBase(String packageString) {
        final String packageBase = getPackageBase();
        final String baseClassPackage = getBaseClassPackage();
        return filterBase(packageString, packageBase, baseClassPackage);
    }

    protected String filterPackageBaseForExtended(String packageString) {
        final String packageBase = getPackageBase();
        final String extendedClassPackage = getExtendedClassPackage();
        return filterBase(packageString, packageBase, extendedClassPackage);
    }

    protected String filterBase(String packageString, String packageBase, String middleBase) {
        boolean hasMiddle = middleBase.trim().length() > 0;
        if (packageBase.trim().length() > 0) {
            if (hasMiddle) {
                return packageBase + "." + middleBase + "." + packageString;
            } else {
                return packageBase + "." + packageString;
            }
        } else {
            if (hasMiddle) {
                return middleBase + "." + packageString;
            } else {
                return packageString;
            }
        }
    }

    protected DfLanguageClassPackage getPackageInfo() {
        final DfLanguageDependency languageDependencyInfo = getBasicProperties().getLanguageDependency();
        return languageDependencyInfo.getLanguageClassPackage();
    }

    // ===================================================================================
    //                                                                    Output Directory
    //                                                                    ================
    public String getGenerateOutputDirectory() {
        final String property = getProperty("generateOutputDirectory", null);
        if (property != null) {
            return property;
        }
        final String defaultDirectory = getLanguageDependency().getGenerateOutputDirectory();
        return getProperty("java.dir", defaultDirectory); // old style or default
    }

    public String getResourceOutputDirectory() {
        return getProperty("resourceOutputDirectory", null);
    }

    public String getDefaultResourceOutputDirectory() {
        return getLanguageDependency().getResourceOutputDirectory();
    }

    // ===================================================================================
    //                                                                 Class/Method Naming
    //                                                                 ===================
    public boolean isTableNameCamelCase() {
        final boolean defaultProperty = false;
        final boolean property = isProperty("isTableNameCamelCase", defaultProperty);
        if (property) {
            return true;
        }
        return isProperty("isJavaNameOfTableSameAsDbName", defaultProperty); // old style or default
    }

    public boolean isColumnNameCamelCase() {
        final boolean defaultProperty = false;
        final boolean property = isProperty("isColumnNameCamelCase", defaultProperty);
        if (property) {
            return true;
        }
        return isProperty("isJavaNameOfColumnSameAsDbName", defaultProperty); // old style or default
    }

    public String getProjectPrefix() {
        return getProperty("projectPrefix", "");
    }

    public String getAllcommonPrefix() { // since 1.1.2 patch 2017/03/04
        return getProperty("allcommonPrefix", "");
    }

    public String getBasePrefix() { // non property
        return "Bs"; // for generation gap
    }

    // -----------------------------------------------------
    //                                                 CDef
    //                                                ------
    public String getCDefPureName() { // e.g. CDef
        final String projectPrefix = getProjectPrefix();
        final String allcommonPrefix = getAllcommonPrefix();
        final DfLanguageClassPackage classPackage = getLanguageDependency().getLanguageClassPackage();
        return classPackage.buildCDefPureClassName(projectPrefix, allcommonPrefix);
    }

    // -----------------------------------------------------
    //                                        Component Name
    //                                        --------------
    public String filterComponentNameWithProjectPrefix(String componentName) { // called from Table
        return doFilterComponentNameWithPrefix(componentName, getProjectPrefix());
    }

    public String filterComponentNameWithAllcommonPrefix(String componentName) {
        return doFilterComponentNameWithPrefix(componentName, getAllcommonPrefix());
    }

    protected String doFilterComponentNameWithPrefix(String componentName, String prefix) {
        if (prefix == null || prefix.trim().length() == 0) {
            return componentName;
        }
        final String filteredPrefix = prefix.substring(0, 1).toLowerCase() + prefix.substring(1);
        return filteredPrefix + adjustPrefixFilteredComponentName(componentName);
    }

    protected String adjustPrefixFilteredComponentName(String componentName) {
        if (componentName.isEmpty()) { // e.g. runtime components on Google Guice
            return componentName;
        }
        return componentName.substring(0, 1).toUpperCase() + componentName.substring(1);
    }

    // ===================================================================================
    //                                                                   Source & Template
    //                                                                   =================
    public String getClassAuthor() {
        return getProperty("classAuthor", "DBFlute(AutoGenerator)");
    }

    public String getSourceFileEncoding() {
        return getProperty("sourceFileEncoding", DEFAULT_sourceFileEncoding);
    }

    protected String _sourceCodeLineSeparator;
    protected boolean _convertSourceCodeLineSeparator;

    public String getSourceCodeLineSeparator() {
        if (_sourceCodeLineSeparator != null) {
            return _sourceCodeLineSeparator;
        }
        final String prop = getProperty("sourceCodeLineSeparator", null);
        if (prop != null) {
            _convertSourceCodeLineSeparator = true; // convert if specified
            if ("LF".equalsIgnoreCase(prop)) {
                _sourceCodeLineSeparator = "\n";
            } else if ("CRLF".equalsIgnoreCase(prop)) {
                _sourceCodeLineSeparator = "\r\n";
            } else {
                String msg = "Unknown line separator (only supported LF or CRLF): " + prop;
                throw new DfIllegalPropertySettingException(msg);
            }
        } else { // null
            final String defaultSeparator = getLanguageDependency().getSourceCodeLineSeparator();
            _sourceCodeLineSeparator = defaultSeparator; // as default but no convert
        }
        return _sourceCodeLineSeparator;
    }

    public boolean isConvertSourceCodeLineSeparator() {
        return _convertSourceCodeLineSeparator;
    }

    public boolean isSourceCodeLineSeparatorLf() {
        return "\n".equals(getSourceCodeLineSeparator());
    }

    public boolean isSourceCodeLineSeparatorCrLf() {
        return "\r\n".equals(getSourceCodeLineSeparator());
    }

    public String getTemplateFileEncoding() { // closet
        return getProperty("templateFileEncoding", DEFAULT_templateFileEncoding);
    }

    // ===================================================================================
    //                                                                Application Behavior
    //                                                                ====================
    protected Map<String, String> _applicationBehaviorMap;

    public Map<String, String> getApplicationBehaviorMap() {
        if (_applicationBehaviorMap != null) {
            return _applicationBehaviorMap;
        }
        final Object obj = getBasicInfoMap().get("applicationBehaviorMap");
        if (obj != null && !(obj instanceof Map<?, ?>)) {
            String msg = "The type of the property 'applicationBehaviorMap' should be Map: " + obj;
            throw new DfIllegalPropertyTypeException(msg);
        }
        if (obj == null) {
            _applicationBehaviorMap = new HashMap<String, String>();
        } else {
            @SuppressWarnings("unchecked")
            final Map<String, String> map = (Map<String, String>) obj;
            _applicationBehaviorMap = map;
        }
        return _applicationBehaviorMap;
    }

    public boolean isApplicationBehaviorProject() {
        return isProperty("isApplicationBehaviorProject", false, getApplicationBehaviorMap());
    }

    public String getLibraryProjectPackageBase() {
        final String defaultBase = getPackageBase();
        final Map<String, String> map = getApplicationBehaviorMap();
        return getProperty("libraryProjectPackageBase", defaultBase, map);
    }

    public String getLibraryAllCommonPackage() {
        final String packageBase = getLibraryProjectPackageBase();
        final String allcommonSimplePackage = getPackageInfo().getBaseCommonPackage();
        return filterBase(allcommonSimplePackage, packageBase, "");
    }

    public String getLibraryBehaviorPackage() {
        final String packageBase = getLibraryProjectPackageBase();
        final String exbhvSimplePackage = getPackageInfo().getExtendedBehaviorPackage();
        return filterBase(exbhvSimplePackage, packageBase, "");
    }

    public String getLibraryEntityPackage() {
        final String packageBase = getLibraryProjectPackageBase();
        final String entitySimplePackage = getPackageInfo().getExtendedEntityPackage();
        return filterBase(entitySimplePackage, packageBase, "");
    }

    public String getApplicationAllCommonPackage() {
        return filterPackageBaseForBase(getPackageInfo().getBaseCommonPackage());
    }

    public String getLibraryProjectPrefix() {
        return getProjectPrefix();
        // *conldn't achieve
        //final Map<String, String> map = getApplicationBehaviorMap();
        //return getProperty("libraryProjectPrefix", "", map);

        // #   o libraryProjectPrefix: (NotRequired - Default '' (means library has no prefix))
        // #    If application project prefix is different from library's one,
        // #    set the property a value 'library's one'.
        // #    If a prefix is valid and both have a same prefix, you need to set this.
    }

    public String getApplicationBehaviorAdditionalSuffix() { // It's closet!
        // but does not work well because other tools handle fixed 'AP'
        return getProperty("applicationBehaviorAdditionalSuffix", "Ap", getApplicationBehaviorMap());
    }

    public String getBhvApResolvedProjectPrefix() {
        if (isApplicationBehaviorProject()) {
            return getLibraryProjectPrefix();
        } else {
            return getProjectPrefix();
        }
    }

    public String getBhvApResolvedBehaviorSuffix() {
        if (isApplicationBehaviorProject()) {
            return "Bhv" + getApplicationBehaviorAdditionalSuffix();
        } else {
            return "Bhv";
        }
    }

    // ===================================================================================
    //                                                                    GenerationGapile
    //                                                                    ================
    // generationGapileMap = map:{
    //     ; gapileDirectory = ../../maihama-dfgenerated/src/main/java
    // }
    // @since 1.1.2 (2016/08/25)
    protected Map<String, Object> _generationGapileMap;

    protected Map<String, Object> getGenerationGapileMap() { // closet
        if (_generationGapileMap != null) {
            return _generationGapileMap;
        }
        @SuppressWarnings("unchecked")
        final Map<String, Object> map = (Map<String, Object>) getBasicInfoMap().get("generationGapileMap");
        _generationGapileMap = map != null ? map : Collections.emptyMap();
        return _generationGapileMap;
    }

    public boolean isGenerationGapileValid() {
        return getGapileDirectory() != null;
    }

    public String getGapileDirectory() { // null allowed
        return getProperty("gapileDirectory", null, getGenerationGapileMap());
    }

    // ===================================================================================
    //                                                         Flat/Omit Directory Package
    //                                                         ===========================
    protected Map<String, String> _outputPackageAdjustmentMap;

    public Map<String, String> getOutputPackageAdjustmentMap() {
        if (_outputPackageAdjustmentMap != null) {
            return _outputPackageAdjustmentMap;
        }
        final Object obj = getBasicInfoMap().get("outputPackageAdjustmentMap");
        if (obj != null && !(obj instanceof Map<?, ?>)) {
            String msg = "The type of the property 'outputPackageAdjustmentMap' should be Map: " + obj;
            throw new DfIllegalPropertyTypeException(msg);
        }
        if (obj == null) {
            _outputPackageAdjustmentMap = new HashMap<String, String>();
        } else {
            @SuppressWarnings("unchecked")
            final Map<String, String> map = (Map<String, String>) obj;
            _outputPackageAdjustmentMap = map;
        }
        return _outputPackageAdjustmentMap;
    }

    public boolean isFlatDirectoryPackageValid() { // CSharp Only
        final String str = getFlatDirectoryPackage();
        return str != null && str.trim().length() > 0 && !str.trim().equals("null");
    }

    /**
     * Get the package for flat directory. Normally, this property is only for C#.
     * @return The package for flat directory. (NullAllowed)
     */
    public String getFlatDirectoryPackage() { // CSharp Only
        final String key = "flatDirectoryPackage";
        final String defaultProp = getProperty(key, null); // for compatibility
        return getProperty(key, defaultProp, getOutputPackageAdjustmentMap());
    }

    public boolean isOmitDirectoryPackageValid() { // CSharp Only
        final String str = getOmitDirectoryPackage();
        return str != null && str.trim().length() > 0 && !str.trim().equals("null");
    }

    /**
     * Get the package for omit directory. Normally, this property is only for C#.
     * @return The package for omit directory. (NullAllowed)
     */
    public String getOmitDirectoryPackage() { // CSharp Only
        final String key = "omitDirectoryPackage";
        final String defaultProp = getProperty(key, null); // for compatibility
        return getProperty(key, defaultProp, getOutputPackageAdjustmentMap());
    }

    public void checkDirectoryPackage() {
        final String flatDirectoryPackage = getFlatDirectoryPackage();
        final String omitDirectoryPackage = getOmitDirectoryPackage();
        if (flatDirectoryPackage == null && omitDirectoryPackage == null) {
            return;
        }
        final DfLanguageDependency languageDependencyInfo = getBasicProperties().getLanguageDependency();
        if (!languageDependencyInfo.isFlatOrOmitDirectorySupported()) {
            String msg = "The language does not support flatDirectoryPackage or omitDirectoryPackage:";
            msg = msg + " language=" + getBasicProperties().getTargetLanguage();
            throw new IllegalStateException(msg);
        }
    }

    // ===================================================================================
    //                                                                      Final TimeZone
    //                                                                      ==============
    public boolean hasDBFluteSystemFinalTimeZone() {
        return getDBFluteSystemFinalTimeZone() != null;
    }

    public String getDBFluteSystemFinalTimeZone() {
        return getProperty("dbfluteSystemFinalTimeZone", null);
    }

    // ===================================================================================
    //                                                                         Super Debug
    //                                                                         ===========
    public boolean isSuperDebug() { // closet (very internal)
        return isProperty("isSuperDebug", false);
    }

    // ===================================================================================
    //                                                                       Suppress Task
    //                                                                       =============
    public boolean isSuppressJDBCTask() { // closet (very internal)
        return isProperty("isSuppressJDBCTask", false);
    }

    public boolean isSuppressDocTask() { // closet (very internal)
        return isProperty("isSuppressDocTask", false);
    }

    public boolean isSuppressGenerateTask() { // closet (very internal)
        return isProperty("isSuppressGenerateTask", false);
    }

    public boolean isSuppressSql2EntityTask() { // closet (very internal)
        return isProperty("isSuppressSql2EntityTask", false);
    }

    public boolean isSuppressOutsideSqlTestTask() { // closet (very internal)
        return isProperty("isSuppressOutsideSqlTestTask", false);
    }

    public boolean isSuppressReplaceSchemaTask() { // closet (very internal)
        return isProperty("isSuppressReplaceSchemaTask", false);
    }

    // ===================================================================================
    //                                                                             Friends
    //                                                                             =======
    public boolean isFriendsHibernate() {
        return handler().getHibernateProperties(getNativeProperties()).hasHibernateDefinition();
    }

    // ===================================================================================
    //                                                                      Begin/End Mark
    //                                                                      ==============
    // these are not properties
    public String getBehaviorQueryPathBeginMark() {
        return "/*df:beginQueryPath*/";
    }

    public String getBehaviorQueryPathEndMark() {
        return "/*df:endQueryPath*/";
    }

    public String getExtendedClassDescriptionBeginMark() {
        return "<!-- df:beginClassDescription -->";
    }

    public String getExtendedClassDescriptionEndMark() {
        return "<!-- df:endClassDescription -->";
    }

    // ===================================================================================
    //                                                                      Generic Helper
    //                                                                      ==============
    // It's not property!
    public String filterGenericsParamOutput(String variableName, String description) {
        return filterGenericsGeneralOutput("@param " + variableName + " " + description);
    }

    public String filterGenericsGeneralOutput(String genericsGeneralOutput) {
        return genericsGeneralOutput;
    }

    public String filterGenericsGeneralOutputAfterNewLineOutput(String genericsGeneralOutput) {
        return getLineSeparator() + filterGenericsGeneralOutput(genericsGeneralOutput);
    }

    public String outputOverrideAnnotation() {
        return filterGenericsGeneralOutput("@Override()");
    }

    public String outputOverrideAnnotationAfterNewLineOutput() {
        return filterGenericsGeneralOutputAfterNewLineOutput("    @Override()");
    }

    public String outputSuppressWarningsAfterLineSeparator() {
        return filterGenericsGeneralOutputAfterNewLineOutput("@SuppressWarnings(\"unchecked\")");
    }

    protected String getLineSeparator() {
        // return System.getProperty("line.separator");
        return "\n";// For to resolve environment dependency!
    }
}