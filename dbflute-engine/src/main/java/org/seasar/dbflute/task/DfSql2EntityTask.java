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
package org.seasar.dbflute.task;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.engine.database.model.AppData;
import org.apache.torque.engine.database.model.Column;
import org.apache.torque.engine.database.model.Database;
import org.apache.torque.engine.database.model.Table;
import org.apache.torque.engine.database.model.TypeMap;
import org.apache.torque.engine.database.model.UnifiedSchema;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.seasar.dbflute.exception.DfCustomizeEntityMarkInvalidException;
import org.seasar.dbflute.exception.DfJDBCException;
import org.seasar.dbflute.exception.DfProcedureSetupFailureException;
import org.seasar.dbflute.exception.IllegalOutsideSqlOperationException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.friends.velocity.DfVelocityContextFactory;
import org.seasar.dbflute.helper.StringKeyMap;
import org.seasar.dbflute.helper.jdbc.DfRunnerInformation;
import org.seasar.dbflute.helper.jdbc.sqlfile.DfSqlFileFireMan;
import org.seasar.dbflute.helper.jdbc.sqlfile.DfSqlFileRunner;
import org.seasar.dbflute.logic.generate.language.DfLanguageDependency;
import org.seasar.dbflute.logic.jdbc.metadata.basic.DfColumnExtractor;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfColumnMeta;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfProcedureColumnMeta;
import org.seasar.dbflute.logic.jdbc.schemaxml.DfSchemaXmlReader;
import org.seasar.dbflute.logic.sql2entity.analyzer.DfOutsideSqlAnalyzer;
import org.seasar.dbflute.logic.sql2entity.analyzer.DfOutsideSqlFile;
import org.seasar.dbflute.logic.sql2entity.analyzer.DfOutsideSqlPack;
import org.seasar.dbflute.logic.sql2entity.analyzer.DfSql2EntityMarkAnalyzer;
import org.seasar.dbflute.logic.sql2entity.analyzer.DfSql2EntityMeta;
import org.seasar.dbflute.logic.sql2entity.bqp.DfBehaviorQueryPathSetupper;
import org.seasar.dbflute.logic.sql2entity.cmentity.DfCustomizeEntityInfo;
import org.seasar.dbflute.logic.sql2entity.pmbean.DfPmbCommentSetupper;
import org.seasar.dbflute.logic.sql2entity.pmbean.DfPmbMetaData;
import org.seasar.dbflute.logic.sql2entity.pmbean.DfProcedurePmbSetupper;
import org.seasar.dbflute.properties.DfCommonColumnProperties;
import org.seasar.dbflute.properties.DfDocumentProperties;
import org.seasar.dbflute.properties.DfLittleAdjustmentProperties;
import org.seasar.dbflute.properties.DfOutsideSqlProperties;
import org.seasar.dbflute.task.DfDBFluteTaskStatus.TaskType;
import org.seasar.dbflute.task.bs.DfAbstractTexenTask;
import org.seasar.dbflute.task.bs.assistant.DfSpecifiedSqlFile;
import org.seasar.dbflute.util.Srl;
import org.seasar.dbflute.util.Srl.IndexOfInfo;

/**
 * @author jflute
 */
public class DfSql2EntityTask extends DfAbstractTexenTask {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance. */
    private static final Log _log = LogFactory.getLog(DfSql2EntityTask.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfSql2EntityMeta _sql2entityMeta = new DfSql2EntityMeta(); // has all meta data

    // helper
    protected final DfColumnExtractor _columnHandler = new DfColumnExtractor();
    protected final DfSql2EntityMarkAnalyzer _markAnalyzer = new DfSql2EntityMarkAnalyzer();

    // for getting schema
    protected AppData _schemaData;

    // to use same process as generating here
    protected final Database _database = new Database();

    // ===================================================================================
    //                                                                           Beginning
    //                                                                           =========
    @Override
    protected boolean begin() {
        if (getBasicProperties().isSuppressSql2EntityTask()) {
            _log.info("...Suppressing Sql2Entity task as basicInfoMap.dfprop");
            return false;
        }
        _log.info("+------------------------------------------+");
        _log.info("|                                          |");
        _log.info("|                Sql2Entity                |");
        _log.info("|                                          |");
        _log.info("+------------------------------------------+");
        DfDBFluteTaskStatus.getInstance().setTaskType(TaskType.Sql2Entity);
        return true;
    }

    // ===================================================================================
    //                                                                          DataSource
    //                                                                          ==========
    @Override
    protected boolean isUseDataSource() {
        return true;
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    @Override
    protected void doExecute() {
        setupControlTemplate();
        setupSchemaInformation();

        final DfRunnerInformation runInfo = new DfRunnerInformation();
        runInfo.setDriver(getDriver());
        runInfo.setUrl(getUrl());
        runInfo.setUser(getUser());
        runInfo.setPassword(getPassword());

        // Sql2Entity task immediately breaks the process by error
        runInfo.setBreakCauseThrow(true);
        runInfo.setErrorContinue(false);

        runInfo.setAutoCommit(false);
        runInfo.setRollbackOnly(true); // this task does not commit
        runInfo.setIgnoreTxError(false); // requires to be roll-backed correctly
        runInfo.setEncoding(getOutsideSqlProperties().getSqlFileEncoding());

        // FireMan's fire result is ignored here because runner's option breakCauseThrow=true
        final DfSqlFileFireMan fireMan = new DfSqlFileFireMan();
        final DfOutsideSqlPack outsideSqlPack = getTargetSqlFileList();
        final DfSqlFileRunner runner = createSqlFileRunner(runInfo, outsideSqlPack);
        fireMan.fire(runner, outsideSqlPack.getPhysicalFileList());

        setupProcedure();

        fireVelocityProcess();
        setupBehaviorQueryPath();
        setupExtendedClassDescription();

        showTargetSqlFileInformation(outsideSqlPack);
        showSkippedFileInformation();
        handleException();
        refreshResources();
    }

    protected void setupControlTemplate() {
        final DfLittleAdjustmentProperties littleProp = getLittleAdjustmentProperties();
        final String title;
        final String controlPath;
        if (littleProp.isAlternateSql2EntityControlValid()) {
            title = "alternate control";
            controlPath = littleProp.getAlternateSql2EntityControl();
        } else {
            final DfLanguageDependency lang = getBasicProperties().getLanguageDependency();
            title = lang.getLanguageTitle();
            controlPath = lang.getSql2EntityControl();
        }
        _log.info("");
        _log.info("...Using " + title + " control: " + controlPath);
        setControlTemplate(controlPath);
    }

    protected void setupSchemaInformation() {
        final DfSchemaXmlReader schemaFileReader = createSchemaFileReader();
        _schemaData = schemaFileReader.read();
    }

    protected DfSchemaXmlReader createSchemaFileReader() {
        return DfSchemaXmlReader.createAsCoreToGenerate(); // same as Generate task's one
    }

    // ===================================================================================
    //                                                                   Executing Element
    //                                                                   =================
    protected DfOutsideSqlPack getTargetSqlFileList() {
        final DfOutsideSqlPack sqlFileList = collectOutsideSqlChecked();
        final String specifiedSqlFile = DfSpecifiedSqlFile.getInstance().getSpecifiedSqlFile();
        if (specifiedSqlFile != null) {
            final DfOutsideSqlPack filteredList = new DfOutsideSqlPack();
            for (DfOutsideSqlFile outsideSqlFile : sqlFileList.getOutsideSqlFileList()) {
                final String fileName = outsideSqlFile.getPhysicalFile().getName();
                if (specifiedSqlFile.equals(fileName)) {
                    filteredList.add(outsideSqlFile);
                }
            }
            return filteredList;
        } else {
            return sqlFileList;
        }
    }

    /**
     * Create SQL file runner.
     * @param runInfo Run information. (NotNull)
     * @return SQL file runner. (NotNull)
     */
    protected DfSqlFileRunner createSqlFileRunner(DfRunnerInformation runInfo, DfOutsideSqlPack outsideSqlPack) {
        return new DfOutsideSqlAnalyzer(runInfo, getDataSource(), _sql2entityMeta, outsideSqlPack, _schemaData);
    }

    protected void handleException() {
        final Map<String, String> exceptionInfoMap = _sql2entityMeta.getExceptionInfoMap();
        if (exceptionInfoMap.isEmpty()) {
            return;
        }
        final Set<String> nameSet = exceptionInfoMap.keySet();
        final StringBuilder sb = new StringBuilder();
        for (String name : nameSet) {
            final String exceptionInfo = exceptionInfoMap.get(name);
            sb.append("[" + name + "]");
            final boolean containsLn = Srl.contains(exceptionInfo, ln());
            sb.append(containsLn ? ln() : " ");
            sb.append(exceptionInfo);
            sb.append(containsLn ? ln() : "").append(ln());
        }
        _log.warn("/* * * * * * * * * * * * * * * * * {Warning Exception}");
        _log.warn(ln() + sb.toString().trim());
        _log.warn("* * * * * * * * * */");
        _log.warn(" ");
    }

    // ===================================================================================
    //                                                                           Procedure
    //                                                                           =========
    protected void setupProcedure() {
        try {
            final DfProcedurePmbSetupper setupper = createProcedurePmbSetupper();
            setupper.setupProcedure();
            final Map<String, String> exceptionInfoMap = _sql2entityMeta.getExceptionInfoMap();
            exceptionInfoMap.putAll(setupper.getContinuedFailureMessageMap());
        } catch (SQLException e) {
            throwProcedureSetupFailureException(e);
        }
    }

    protected DfProcedurePmbSetupper createProcedurePmbSetupper() {
        final Map<String, DfCustomizeEntityInfo> entityInfoMap = _sql2entityMeta.getEntityInfoMap();
        final Map<String, DfPmbMetaData> pmbMetaDataMap = _sql2entityMeta.getPmbMetaDataMap();
        return new DfProcedurePmbSetupper(getDataSource(), entityInfoMap, pmbMetaDataMap, _database);
    }

    protected void throwProcedureSetupFailureException(SQLException e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to set up procedures.");
        br.addItem("SQL Exception");
        br.addElement(DfJDBCException.extractMessage(e));
        SQLException nextEx = e.getNextException();
        if (nextEx != null) {
            br.addElement(DfJDBCException.extractMessage(nextEx));
        }
        String msg = br.buildExceptionMessage();
        throw new DfProcedureSetupFailureException(msg, e);
    }

    // ===================================================================================
    //                                                                 Behavior Query Path
    //                                                                 ===================
    protected void setupBehaviorQueryPath() {
        final DfOutsideSqlPack sqlFileList = collectOutsideSqlChecked();
        final DfBehaviorQueryPathSetupper setupper = new DfBehaviorQueryPathSetupper();
        setupper.setupBehaviorQueryPath(sqlFileList);
    }

    protected void setupExtendedClassDescription() {
        final DfPmbCommentSetupper reflector = new DfPmbCommentSetupper(_database.getPmbMetaDataList());
        reflector.setupExtendedClassDescription();
    }

    // ===================================================================================
    //                                                                SQL File Information
    //                                                                ====================
    protected void showTargetSqlFileInformation(DfOutsideSqlPack outsideSqlPack) {
        final StringBuilder sb = new StringBuilder();
        sb.append(ln()).append("/- - - - - - - - - - - - - - - - - - - - - - - -");
        sb.append(ln()).append("Target SQL files: ").append(outsideSqlPack.size());
        sb.append(ln());
        for (DfOutsideSqlFile sqlFile : outsideSqlPack.getOutsideSqlFileList()) {
            sb.append(ln()).append("  ").append(sqlFile.getPhysicalFile().getName());
        }
        sb.append(ln()).append("- - - - - - - - - -/");
        _log.info(sb);
    }

    // ===================================================================================
    //                                                                  Prepare Generation
    //                                                                  ==================
    @Override
    public Context initControlContext() throws Exception {
        _log.info("");
        _log.info("...Preparing generation of customize-entities and parameter-beans");
        _log.info("* * * * * * * * * *");
        _log.info("* CustomizeEntity *");
        _log.info("* * * * * * * * * *");
        final StringBuilder logSb = new StringBuilder();

        final Database database = _database;
        database.setSql2EntitySchemaData(_schemaData);
        database.setPmbMetaDataMap(_sql2entityMeta.getPmbMetaDataMap());
        database.setSkipDeleteOldClass(isSkipDeleteOldClass());

        final Map<String, DfCustomizeEntityInfo> entityInfoMap = _sql2entityMeta.getEntityInfoMap();
        final Set<String> entityNameSet = entityInfoMap.keySet();
        for (String entityName : entityNameSet) {
            final DfCustomizeEntityInfo entityInfo = entityInfoMap.get(entityName);
            final Map<String, DfColumnMeta> metaMap = entityInfo.getColumnMap();
            final DfOutsideSqlFile outsideSqlFile = entityInfo.getOutsideSqlFile();

            final Table tbl = new Table();
            tbl.setSql2EntityCustomize(true);
            if (outsideSqlFile != null) { // basically true but checked just in case
                tbl.setSql2EntitySqlFile(outsideSqlFile);
            }
            tbl.setName(entityInfo.getTableDbName());
            if (!entityInfo.needsJavaNameConvert()) {
                tbl.suppressJavaNameConvert(); // basically here (except STRUCT type)
            }
            if (entityInfo.hasNestedCustomizeEntity()) {
                tbl.setSql2EntityCustomizeHasNested(true); // basically when STRUCT type
            }
            if (entityInfo.isAdditionalSchema()) {
                tbl.setUnifiedSchema(entityInfo.getAdditionalSchema()); // basically when STRUCT type
            }
            tbl.setSql2EntityTypeSafeCursor(entityInfo.isCursorHandling());
            buildCustomizeEntityTitle(logSb, entityName, entityInfo);

            final StringKeyMap<String> pkMap = getPrimaryKeyMap(entityInfo);
            final boolean allCommonColumn = hasAllCommonColumn(metaMap);
            final Set<String> columnNameSet = metaMap.keySet();
            for (String columnName : columnNameSet) {
                final Column column = new Column();
                setupColumnName(columnName, column);

                // an element removed from pkMap if true
                // and a table name related to primary key is returned
                final String pkRelatedTableName = setupPrimaryKey(pkMap, entityName, columnName, column);

                setupTorqueType(metaMap, columnName, column, allCommonColumn);
                setupDbType(metaMap, columnName, column);
                setupNotNull(metaMap, columnName, column);
                setupColumnSizeContainsDigit(metaMap, columnName, column);
                setupColumnComment(metaMap, columnName, column);
                setupSql2EntityElement(entityName, metaMap, columnName, column, pkRelatedTableName, logSb);
                tbl.addColumn(column);
            }
            if (!pkMap.isEmpty()) { // if not-removed columns exist
                throwPrimaryKeyNotFoundException(entityName, pkMap, columnNameSet);
            }

            if (entityInfo.isScalarHandling()) {
                // it does not generate an only-one-column entity
                tbl.setDatabase(database); // one-way love for utility (just in case)
                processScalarHandling(entityInfo, tbl);
            } else if (entityInfo.isDomainHandling()) {
                // it does not generate an customize-entity
                tbl.setDatabase(database); // one-way love for utility (just in case)
                processDomainHandling(entityInfo, tbl);
            } else {
                // initialize a class name of the entity for typed parameter-bean
                database.addTable(tbl); // should be before getting names
                entityInfo.setEntityClassName(tbl.getExtendedEntityClassName());
                entityInfo.setImmutableClassName(tbl.getImmutableExtendedEntityClassName());
            }
            logSb.append(ln());
        }
        final String databaseType = getDatabaseTypeFacadeProp().getTargetDatabase();
        final AppData appData = new AppData(databaseType);
        appData.addDatabase(database);

        showCustomizeEntity(logSb);
        showParameterBean();

        final VelocityContext context = createVelocityContext(appData);
        return context;
    }

    protected boolean isSkipDeleteOldClass() {
        return DfSpecifiedSqlFile.getInstance().getSpecifiedSqlFile() != null;
    }

    protected StringKeyMap<String> getPrimaryKeyMap(DfCustomizeEntityInfo entityInfo) {
        final StringKeyMap<String> pkMap = StringKeyMap.createAsFlexibleOrdered();
        final List<String> pkList = entityInfo.getPrimaryKeyList();
        if (pkList == null || pkList.isEmpty()) {
            return pkMap;
        }
        for (String pk : pkList) {
            if (Srl.contains(pk, ".")) {
                final IndexOfInfo info = Srl.indexOfFirst(pk, ".");
                String tableName = info.substringFrontTrimmed();
                String pkName = info.substringRearTrimmed();
                pkMap.put(pkName, tableName);
            } else {
                pkMap.put(pk, null); // no specified related table
            }
        }
        return pkMap;
    }

    protected boolean hasAllCommonColumn(Map<String, DfColumnMeta> columnJdbcTypeMap) {
        final Map<String, String> commonColumnMap = getCommonColumnMap();
        if (commonColumnMap.isEmpty()) {
            return false;
        }
        final Set<String> commonColumnSet = commonColumnMap.keySet();
        for (String commonColumnName : commonColumnSet) {
            if (!columnJdbcTypeMap.containsKey(commonColumnName)) {
                return false; // Not All!
            }
        }
        return true;
    }

    protected void throwPrimaryKeyNotFoundException(String entityName, StringKeyMap<String> pkMap,
            Set<String> columnNameSet) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The primary keys were not found in selected columns.");
        br.addItem("Advice");
        br.addElement("Make sure your primary key settings in the Sql2Entity mark.");
        br.addElement("For example:");
        br.addElement("  (x): -- *MEMBER_IT*");
        br.addElement("  (o): -- *MEMBER_ID*");
        br.addElement("Or '--*', '-- *' may be on your SQL comment.");
        br.addElement("The mark is reserved by DBFlute as Sql2Entity mark.");
        br.addItem("Entity");
        br.addElement(entityName);
        br.addItem("Selected Column");
        br.addElement(columnNameSet);
        br.addItem("Specified PK");
        br.addElement(pkMap.keySet());
        final String msg = br.buildExceptionMessage();
        throw new IllegalOutsideSqlOperationException(msg);
    }

    // -----------------------------------------------------
    //                                         Setup Element
    //                                         -------------
    protected void setupColumnName(String columnName, final Column col) {
        if (needsConvertToJavaName(columnName)) {
            col.setName(columnName);
        } else {
            col.setupNeedsJavaNameConvertFalse();
            col.setName(Srl.initCap(columnName));
        }
    }

    protected String setupPrimaryKey(StringKeyMap<String> pkMap, String entityName, String columnName, final Column col) {
        if (pkMap.containsKey(columnName)) {
            col.setPrimaryKey(true);
            return pkMap.remove(columnName); // returns related table
        }
        return null;
    }

    protected void setupTorqueType(Map<String, DfColumnMeta> metaMap, String columnName, Column column,
            boolean allCommonColumn) {
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        // If the select columns have common columns, 
        // The types of common column are set up from common column properties.
        // - - - - - - - - - -/
        if (allCommonColumn) {
            final String commonColumnTorqueType = getCommonColumnTorqueType(columnName);
            if (commonColumnTorqueType != null) {
                column.setJdbcType(commonColumnTorqueType);
                return;
            }
        }
        final DfColumnMeta columnMeta = metaMap.get(columnName);
        final String columnTorqueType = getColumnTorqueType(columnMeta);
        column.setJdbcType(columnTorqueType);
    }

    protected void setupDbType(Map<String, DfColumnMeta> metaMap, String columnName, Column column) {
        final DfColumnMeta columnMeta = metaMap.get(columnName);
        final String dbTypeName;
        final String plainName = columnMeta.getDbTypeName();
        if (Srl.contains(plainName, ".")) { // basically for ARRAY and STRUCT type
            final String catalogSchema = Srl.substringLastFront(plainName, ".");
            final UnifiedSchema unifiedSchema = UnifiedSchema.createAsDynamicSchema(catalogSchema);
            if (unifiedSchema.isMainSchema()) {
                dbTypeName = Srl.substringLastRear(plainName, ".");
            } else {
                dbTypeName = plainName;
            }
        } else {
            dbTypeName = plainName;
        }
        column.setDbType(dbTypeName);
    }

    protected String getCommonColumnTorqueType(String columnName) {
        return getCommonColumnMap().get(columnName);
    }

    protected Map<String, String> getCommonColumnMap() {
        final DfCommonColumnProperties prop = getProperties().getCommonColumnProperties();
        return prop.getCommonColumnMap();
    }

    protected String getColumnTorqueType(DfColumnMeta columnMeta) {
        if (columnMeta.isProcedureParameter() && !_columnHandler.hasMappingJdbcType(columnMeta)) {
            // unknown type of procedure parameter should be treated as Object
            return TypeMap.OTHER;
        } else {
            return _columnHandler.getColumnJdbcType(columnMeta);
        }
    }

    protected void setupNotNull(Map<String, DfColumnMeta> metaMap, String columnName, Column column) {
        // basically not meta data so false, true might be from select column specification
        final DfColumnMeta columnMeta = metaMap.get(columnName);
        column.setNotNull(columnMeta.isRequired());
    }

    protected void setupColumnSizeContainsDigit(Map<String, DfColumnMeta> metaMap, String columnName, Column column) {
        final DfColumnMeta columnMeta = metaMap.get(columnName);
        final int columnSize = columnMeta.getColumnSize();
        final int decimalDigits = columnMeta.getDecimalDigits();
        column.setupColumnSize(columnSize, decimalDigits);
    }

    protected void setupColumnComment(Map<String, DfColumnMeta> metaMap, String columnName, Column column) {
        final DfColumnMeta columnMeta = metaMap.get(columnName);
        final String sql2EntityRelatedTableName = columnMeta.getSql2EntityRelatedTableName();
        final Table relatedTable = getRelatedTable(sql2EntityRelatedTableName);
        String relatedComment = null;
        if (relatedTable != null) {
            final String relatedColumnName = columnMeta.getSql2EntityRelatedColumnName();
            final Column relatedColumn = relatedTable.getColumn(relatedColumnName);
            if (relatedColumn != null) {
                relatedComment = relatedColumn.getPlainComment();
            }
        }
        // the meta has its select column comment
        final String selectColumnComment = columnMeta.getColumnComment();
        final String commentMark = "// ";
        final String delimiter = getAliasDelimiterInDbComment();
        final StringBuilder sb = new StringBuilder();
        if (Srl.is_NotNull_and_NotTrimmedEmpty(relatedComment)) {
            sb.append(relatedComment);
            if (Srl.is_NotNull_and_NotTrimmedEmpty(selectColumnComment)) { // both exist
                if (Srl.is_NotNull_and_NotTrimmedEmpty(delimiter)) { // use alias option
                    if (relatedComment.contains(delimiter)) { // resolved in related comment
                        sb.append(ln()).append(commentMark).append(selectColumnComment);
                    } else { // unresolved yet
                        if (isDbCommentOnAliasBasis()) { // related comment is alias
                            sb.append(delimiter);
                        } else { // related comment is description
                            sb.append(ln());
                        }
                        sb.append(commentMark).append(selectColumnComment);
                    }
                } else { // no alias option
                    sb.append(ln()).append(commentMark).append(selectColumnComment);
                }
            }
        } else { // not found related comment
            if (Srl.is_NotNull_and_NotTrimmedEmpty(selectColumnComment)) {
                if (Srl.is_NotNull_and_NotTrimmedEmpty(delimiter)) { // use alias option
                    if (isDbCommentOnAliasBasis()) {
                        // select column comment is treated as description
                        sb.append(delimiter);
                    }
                }
                sb.append(commentMark).append(selectColumnComment);
            }
        }
        column.setPlainComment(sb.toString());
    }

    protected String getAliasDelimiterInDbComment() {
        return getDocumentProperties().getAliasDelimiterInDbComment();
    }

    protected boolean isDbCommentOnAliasBasis() {
        return getDocumentProperties().isDbCommentOnAliasBasis();
    }

    protected void setupSql2EntityElement(String entityName, Map<String, DfColumnMeta> metaMap, String columnName,
            Column column, String pkRelatedTableName, StringBuilder logSb) {
        final Table relatedTable = setupSql2EntityRelatedTable(entityName, metaMap, columnName, column,
                pkRelatedTableName);
        final Column relatedColumn = setupSql2EntityRelatedColumn(relatedTable, metaMap, columnName, column);
        final String forcedJavaNative = setupSql2EntityForcedJavaNative(metaMap, columnName, column);

        buildCustomizeEntityColumnInfo(logSb, columnName, column, relatedTable, relatedColumn, forcedJavaNative);
    }

    protected Table setupSql2EntityRelatedTable(String entityName, Map<String, DfColumnMeta> metaMap,
            String columnName, Column column, String pkRelatedTableName) {
        final DfColumnMeta columnMeta = metaMap.get(columnName);
        final String sql2EntityRelatedTableName = columnMeta.getSql2EntityRelatedTableName();
        Table relatedTable = getRelatedTable(sql2EntityRelatedTableName); // first attack
        if (relatedTable == null) {
            if (pkRelatedTableName != null) { // second attack using PK-related
                relatedTable = getRelatedTable(pkRelatedTableName);
                if (relatedTable == null) {
                    throwTableRelatedPrimaryKeyNotFoundException(entityName, pkRelatedTableName, columnName);
                }
            } else {
                return null;
            }
        } else {
            if (pkRelatedTableName != null) {
                if (!Srl.equalsFlexible(sql2EntityRelatedTableName, pkRelatedTableName)) {
                    throwTableRelatedPrimaryKeyDifferentException(entityName, sql2EntityRelatedTableName,
                            pkRelatedTableName, columnName);
                }
            }
        }
        column.setSql2EntityRelatedTable(relatedTable);
        return relatedTable;
    }

    protected void throwTableRelatedPrimaryKeyNotFoundException(String entityName, String tableName, String columnName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The table name related to the primary key is not found.");
        br.addItem("Entity");
        br.addElement(entityName);
        br.addItem("Table Name");
        br.addElement(tableName);
        br.addItem("Primary Key");
        br.addElement(columnName);
        final String msg = br.buildExceptionMessage();
        throw new IllegalOutsideSqlOperationException(msg);
    }

    protected void throwTableRelatedPrimaryKeyDifferentException(String entityName, String realTable,
            String differentTable, String columnName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The table name related to the primary key is different.");
        br.addItem("Entity");
        br.addElement(entityName);
        br.addItem("Real Table");
        br.addElement(realTable);
        br.addItem("Different Table");
        br.addElement(differentTable);
        br.addItem("Primary Key");
        br.addElement(columnName);
        final String msg = br.buildExceptionMessage();
        throw new IllegalOutsideSqlOperationException(msg);
    }

    protected Column setupSql2EntityRelatedColumn(Table relatedTable, Map<String, DfColumnMeta> metaMap,
            String columnName, Column column) {
        if (relatedTable == null) {
            return null;
        }
        final DfColumnMeta metaInfo = metaMap.get(columnName);
        final String sql2EntityRelatedColumnName = metaInfo.getSql2EntityRelatedColumnName();
        final Column relatedColumn = relatedTable.getColumn(sql2EntityRelatedColumnName);
        if (relatedColumn == null) {
            return null;
        }
        column.setSql2EntityRelatedColumn(relatedColumn);
        return column;
    }

    protected Table getRelatedTable(String sql2EntityRelatedTableName) {
        if (_schemaData == null) {
            return null;
        }
        final Table relatedTable = _schemaData.getDatabase().getTable(sql2EntityRelatedTableName);
        return relatedTable;
    }

    protected String setupSql2EntityForcedJavaNative(final Map<String, DfColumnMeta> metaMap, String columnName,
            final Column column) {
        final DfColumnMeta metaInfo = metaMap.get(columnName);
        final String sql2EntityForcedJavaNative = metaInfo.getSql2EntityForcedJavaNative();
        column.setSql2EntityForcedJavaNative(sql2EntityForcedJavaNative);
        return sql2EntityForcedJavaNative;
    }

    // -----------------------------------------------------
    //                                       Result Handling
    //                                       ---------------
    protected void processDomainHandling(DfCustomizeEntityInfo entityInfo, Table tbl) {
        final DfPmbMetaData pmbMetaData = entityInfo.getPmbMetaData();
        if (pmbMetaData == null || !pmbMetaData.isTypedReturnEntityPmb()) {
            final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
            br.addNotice("The 'domain' option was not related to a typed parameter-bean.");
            br.addItem("Advice");
            br.addElement("A 'domain' option should be defined with a typed parameter-bean");
            br.addElement("that is typed to things returning an entity.");
            br.addElement("For example:");
            br.addElement("  (x): selectDomainMember.sql");
            br.addElement("  (o): MemberBhv_selectDomainMember.sql");
            br.addElement("  (x):");
            br.addElement("    -- #df:entity#");
            br.addElement("    -- +domain+");
            br.addElement("");
            br.addElement("    select MEMBER_ID, MEMBER_NAME, ... from MEMBER");
            br.addElement("  (o):");
            br.addElement("    -- #df:entity#");
            br.addElement("    -- +domain+");
            br.addElement("");
            br.addElement("    -- !df:pmb!");
            br.addElement("");
            br.addElement("    select MEMBER_ID, MEMBER_NAME, ... from MEMBER");
            br.addItem("SQL File");
            br.addElement(entityInfo.getSqlFile());
            final String msg = br.buildExceptionMessage();
            throw new DfCustomizeEntityMarkInvalidException(msg);
        }
        final String entityClassName = pmbMetaData.getEntityClassName();
        if (Srl.is_Null_or_TrimmedEmpty(entityClassName)) {
            String msg = "The entity class name should not be null: " + entityInfo.getSqlFile();
            throw new IllegalStateException(msg); // no way
        }
        final Database database = _schemaData.getDatabase();
        Table domainTable = database.getTable(entityClassName);
        if (domainTable == null) { // retry without project-prefix for a class name
            final String projectPrefix = getBasicProperties().getProjectPrefix();
            domainTable = database.getTable(Srl.substringFirstFront(entityClassName, projectPrefix));
        }
        if (domainTable == null) {
            final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
            br.addNotice("The table of the behavior query was not found.");
            br.addItem("Advice");
            br.addElement("A 'domain' option should be defined under behavior query.");
            br.addElement("And behavior query should have an existing table.");
            br.addElement("For example:");
            br.addElement("  (x): MembooBhv_selectDomainMember.sql");
            br.addElement("  (o): MemberBhv_selectDomainMember.sql");
            br.addItem("SQL File");
            br.addElement(entityInfo.getSqlFile());
            final String msg = br.buildExceptionMessage();
            throw new DfCustomizeEntityMarkInvalidException(msg); // basically no way
        }
        final List<Column> columnList = tbl.getColumnList();
        for (Column column : columnList) {
            final Column found = domainTable.getColumn(column.getName());
            if (found == null) {
                final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
                br.addNotice("The selected column was not a column of domain table.");
                br.addItem("Advice");
                br.addElement("A selected column with a 'domain' option");
                br.addElement("should be one of domain table.");
                br.addElement("For example:");
                br.addElement("  (x):");
                br.addElement("    select MEMBER_ID, 'noexist' as NO_EXIST from MEMBER");
                br.addElement("  (o):");
                br.addElement("    select MEMBER_ID, MEMBER_NAME from MEMBER");
                br.addElement("  (o):");
                br.addElement("    select member.* from MEMBER member");
                br.addItem("SQL File");
                br.addElement(entityInfo.getSqlFile());
                br.addItem("Unknown Column");
                br.addElement(column.getName());
                br.addItem("Domain Table");
                br.addElement(domainTable.getTableDbName());
                final String msg = br.buildExceptionMessage();
                throw new DfCustomizeEntityMarkInvalidException(msg);
            }
        }
        entityInfo.setEntityClassName(domainTable.getExtendedEntityClassName());
        entityInfo.setEntityClassName(domainTable.getImmutableExtendedEntityClassName());
    }

    protected void processScalarHandling(DfCustomizeEntityInfo entityInfo, Table tbl) {
        final DfPmbMetaData pmbMetaData = entityInfo.getPmbMetaData(); // for check only
        if (pmbMetaData == null || !pmbMetaData.isTypedSelectPmb()) { // not pinpoint (but enough)
            final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
            br.addNotice("The 'scalar' option was not related to a typed parameter-bean.");
            br.addItem("Advice");
            br.addElement("A 'scalar' option should be defined with a typed parameter-bean");
            br.addElement("that is typed to things returning an scalar value.");
            br.addElement("For example:");
            br.addElement("  (x): selectMemberName.sql");
            br.addElement("  (o): MemberBhv_selectMemberName.sql");
            br.addElement("  (x):");
            br.addElement("    -- #df:entity#");
            br.addElement("    -- +scalar+");
            br.addElement("");
            br.addElement("    select MEMBER_NAME from MEMBER");
            br.addElement("  (o):");
            br.addElement("    -- #df:entity#");
            br.addElement("    -- +scalar+");
            br.addElement("");
            br.addElement("    -- !df:pmb!");
            br.addElement("");
            br.addElement("    select MEMBER_NAME from MEMBER");
            br.addItem("SQL File");
            br.addElement(entityInfo.getSqlFile());
            final String msg = br.buildExceptionMessage();
            throw new DfCustomizeEntityMarkInvalidException(msg);
        }
        final List<Column> columnList = tbl.getColumnList();
        if (columnList.size() != 1) {
            final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
            br.addNotice("The 'scalar' option was related to non-only-one-column SQL.");
            br.addItem("Advice");
            br.addElement("A 'scalar' option should be defined on only-one-column SQL.");
            br.addElement("For example:");
            br.addElement("  (x):");
            br.addElement("    -- #df:entity#");
            br.addElement("    -- +scalar+");
            br.addElement("  ");
            br.addElement("    -- !df:pmb!");
            br.addElement("  ");
            br.addElement("    select MEMBER_NAME, BIRTHDATE from MEMBER");
            br.addElement("  (o):");
            br.addElement("    -- #df:entity#");
            br.addElement("    -- +scalar+");
            br.addElement("");
            br.addElement("    -- !df:pmb!");
            br.addElement("");
            br.addElement("    select BIRTHDATE from MEMBER");
            br.addItem("SQL File");
            br.addElement(entityInfo.getSqlFile());
            br.addItem("Selected Column");
            if (!columnList.isEmpty()) {
                for (Column column : columnList) {
                    br.addElement(column.getName());
                }
            } else {
                br.addElement("(empty)");
            }
            final String msg = br.buildExceptionMessage();
            throw new DfCustomizeEntityMarkInvalidException(msg);
        }
        final Column column = columnList.get(0);
        entityInfo.setScalarJavaNative(column.getJavaNative());
        entityInfo.setScalarColumnDisp(column.getColumnDefinitionLineDisp());
    }

    // -----------------------------------------------------
    //                                               Logging
    //                                               -------
    protected void buildCustomizeEntityTitle(StringBuilder logSb, String entityName, DfCustomizeEntityInfo entityInfo) {
        logSb.append(entityName);
        final String handlingDisp = entityInfo.buildHandlingDisp();
        if (Srl.is_NotNull_and_NotTrimmedEmpty(handlingDisp)) {
            logSb.append(" ").append(handlingDisp);
        }
        logSb.append(ln());
    }

    protected void buildCustomizeEntityColumnInfo(StringBuilder logSb, String columnName, Column column,
            Table relatedTable, Column relatedColumn, String forcedJavaNatice) {
        final StringBuilder sb = new StringBuilder();
        sb.append(" ").append(column.isPrimaryKey() ? "*" : " ");
        sb.append(columnName);
        sb.append(" ");
        sb.append(column.getDbTypeExpression());
        final String columnSize = column.getColumnSize();
        if (Srl.is_NotNull_and_NotTrimmedEmpty(columnSize)) {
            sb.append("(").append(columnSize).append(")");
        }
        if (relatedColumn != null) {
            sb.append(" related to ").append(relatedTable.getTableDbName());
            sb.append(".").append(relatedColumn.getName());
        }
        if (Srl.is_NotNull_and_NotTrimmedEmpty(forcedJavaNatice)) {
            sb.append(" forced to ").append(forcedJavaNatice);
        }
        logSb.append(sb).append(ln());
    }

    protected void showCustomizeEntity(StringBuilder logSb) {
        if (logSb.length() > 0) {
            _log.info(ln() + logSb.toString().trim());
        }
    }

    protected void showParameterBean() {
        _log.info("* * * * * * * * *");
        _log.info("* ParameterBean *");
        _log.info("* * * * * * * * *");
        final StringBuilder logSb = new StringBuilder();
        final Map<String, DfPmbMetaData> pmbMetaDataMap = _sql2entityMeta.getPmbMetaDataMap();
        for (Entry<String, DfPmbMetaData> pmbEntry : pmbMetaDataMap.entrySet()) {
            final DfPmbMetaData pmbMetaData = pmbEntry.getValue();
            logSb.append(pmbMetaData.getClassName());
            if (pmbMetaData.hasSuperClassDefinition()) {
                logSb.append(" extends ").append(pmbMetaData.getSuperClassName());
            }
            if (pmbMetaData.isRelatedToProcedure()) {
                logSb.append(" (procedure");
                if (pmbMetaData.isProcedureRefCustomizeEntity()) {
                    logSb.append(" with customize-entity");
                }
                logSb.append(")").append(ln());
                final Map<String, DfProcedureColumnMeta> propertyNameColumnInfoMap = pmbMetaData
                        .getPropertyNameColumnInfoMap();
                for (Entry<String, DfProcedureColumnMeta> columnEntry : propertyNameColumnInfoMap.entrySet()) {
                    final DfProcedureColumnMeta columnInfo = columnEntry.getValue();
                    logSb.append("  ").append(columnInfo.getColumnNameDisp());
                    logSb.append(ln());
                }
            } else {
                if (pmbMetaData.isTypedParameterBean()) {
                    logSb.append(" ").append(pmbMetaData.buildTypedDisp());
                }
                logSb.append(ln());
                final Map<String, String> propertyNameTypeMap = pmbMetaData.getPropertyNameTypeMap();
                final Map<String, String> propertyOptionMap = pmbMetaData.getPropertyNameOptionMap();
                for (Entry<String, String> propEntry : propertyNameTypeMap.entrySet()) {
                    final String propertyName = propEntry.getKey();
                    final String propertyType = propEntry.getValue();
                    logSb.append("  ").append(propertyType).append(" ").append(propertyName);
                    final String optionDef = propertyOptionMap.get(propertyName);
                    if (Srl.is_NotNull_and_NotTrimmedEmpty(optionDef)) {
                        logSb.append(":").append(optionDef);
                    }
                    logSb.append(ln());
                }
            }
            logSb.append(ln());
        }
        if (logSb.length() > 0) {
            _log.info(ln() + logSb.toString().trim());
        }
    }

    // -----------------------------------------------------
    //                                         Assist Helper
    //                                         -------------
    protected VelocityContext createVelocityContext(final AppData appData) {
        final DfVelocityContextFactory factory = createVelocityContextFactory();
        return factory.createAsCore(appData, _selector);
    }

    protected boolean needsConvertToJavaName(String columnName) {
        if (columnName == null || columnName.trim().length() == 0) {
            String msg = "The columnName is invalid: " + columnName;
            throw new IllegalArgumentException(msg);
        }
        if (columnName.contains("_")) {
            return true; // contains (supported) connector!
        }
        // here 'BIRHDATE' or 'birthdate' or 'Birthdate'
        // or 'memberStatus' or 'MemberStatus'
        final char[] columnCharArray = columnName.toCharArray();
        boolean existsUpper = false;
        boolean existsLower = false;
        for (char ch : columnCharArray) {
            if (Character.isDigit(ch)) {
                continue;
            }
            if (Character.isUpperCase(ch)) {
                existsUpper = true;
                continue;
            }
            if (Character.isLowerCase(ch)) {
                existsLower = true;
                continue;
            }
        }
        final boolean camelCase = existsUpper && existsLower;
        // if it's camelCase, no needs to convert
        // (all characters that are upper or lower case needs to convert)
        return !camelCase;
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfOutsideSqlProperties getOutsideSqlProperties() {
        return getProperties().getOutsideSqlProperties();
    }

    protected DfDocumentProperties getDocumentProperties() {
        return getProperties().getDocumentProperties();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setSpecifiedSqlFile(String specifiedSqlFile) {
        DfSpecifiedSqlFile.getInstance().setSpecifiedSqlFile(specifiedSqlFile);
    }
}