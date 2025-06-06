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
package org.dbflute.logic.jdbc.metadata.basic;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.torque.engine.database.model.UnifiedSchema;
import org.dbflute.DfBuildProperties;
import org.dbflute.exception.DfIllegalPropertySettingException;
import org.dbflute.exception.DfJDBCException;
import org.dbflute.exception.DfProcedureListGettingFailureException;
import org.dbflute.helper.StringKeyMap;
import org.dbflute.helper.jdbc.context.DfSchemaSource;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.logic.jdbc.metadata.info.DfProcedureColumnMeta;
import org.dbflute.logic.jdbc.metadata.info.DfProcedureColumnMeta.DfProcedureColumnType;
import org.dbflute.logic.jdbc.metadata.info.DfProcedureMeta;
import org.dbflute.logic.jdbc.metadata.info.DfProcedureMeta.DfProcedureType;
import org.dbflute.logic.jdbc.metadata.info.DfProcedureSourceInfo;
import org.dbflute.logic.jdbc.metadata.info.DfProcedureSynonymMeta;
import org.dbflute.logic.jdbc.metadata.info.DfSynonymMeta;
import org.dbflute.logic.jdbc.metadata.info.DfTypeArrayInfo;
import org.dbflute.logic.jdbc.metadata.info.DfTypeStructInfo;
import org.dbflute.logic.jdbc.metadata.procedure.DfProcedureNativeTranslatorOracle;
import org.dbflute.logic.jdbc.metadata.procedure.DfProcedureSupplementExtractor;
import org.dbflute.logic.jdbc.metadata.procedure.DfProcedureSupplementExtractorDB2;
import org.dbflute.logic.jdbc.metadata.procedure.DfProcedureSupplementExtractorH2;
import org.dbflute.logic.jdbc.metadata.procedure.DfProcedureSupplementExtractorMySQL;
import org.dbflute.logic.jdbc.metadata.procedure.DfProcedureSupplementExtractorOracle;
import org.dbflute.logic.jdbc.metadata.procedure.DfProcedureSupplementExtractorPostgreSQL;
import org.dbflute.logic.jdbc.metadata.synonym.DfProcedureSynonymExtractor;
import org.dbflute.logic.jdbc.metadata.synonym.factory.DfProcedureSynonymExtractorFactory;
import org.dbflute.properties.DfDatabaseProperties;
import org.dbflute.properties.DfOutsideSqlProperties;
import org.dbflute.properties.DfOutsideSqlProperties.ProcedureSynonymHandlingType;
import org.dbflute.properties.assistant.database.DfAdditionalSchemaInfo;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 0.7.5 (2008/06/28 Saturday)
 */
public class DfProcedureExtractor extends DfAbstractMetaDataBasicExtractor {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger _log = LoggerFactory.getLogger(DfProcedureExtractor.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected boolean _suppressAdditionalSchema;
    protected boolean _suppressFilterByProperty;
    protected boolean _suppressGenerationRestriction;
    protected boolean _suppressLogging;
    protected DfSchemaSource _procedureSynonymDataSource;
    protected DfSchemaSource _procedureToDBLinkDataSource;

    // key is data source because it may be schema diff
    protected final Map<Integer, Map<String, DfProcedureSupplementExtractor>> _supplementExtractorMap = newHashMap();
    protected final Map<Integer, DfProcedureSupplementExtractorPostgreSQL> _supplementExtractorPostgreSQLMap = newHashMap();
    protected final Map<Integer, DfProcedureSupplementExtractorMySQL> _supplementExtractorMySQLMap = newHashMap();
    protected final Map<Integer, DfProcedureSupplementExtractorOracle> _supplementExtractorOracleMap = newHashMap();

    // ===================================================================================
    //                                                                 Available Procedure
    //                                                                 ===================
    /**
     * Get the list of available meta information. <br>
     * The list is ordered per schema, as main schema first.
     * @param dataSource The data source for getting meta data. (NotNull)
     * @return The list of available procedure meta informations. (NotNull)
     * @throws SQLException When it fails to handle the SQL.
     */
    public List<DfProcedureMeta> getAvailableProcedureList(DfSchemaSource dataSource) throws SQLException {
        return new ArrayList<DfProcedureMeta>(getAvailableProcedureMap(dataSource).values());
    }

    /**
     * Get the map of available meta information. <br>
     * The map key is procedure name that contains package prefix).
     * @param dataSource The data source for getting meta data. (NotNull)
     * @return The map of available procedure meta informations. The key is full-qualified name. (NotNull)
     * @throws SQLException When it fails to handle the SQL.
     */
    public Map<String, DfProcedureMeta> getAvailableProcedureMap(DfSchemaSource dataSource) throws SQLException {
        if (isQuitByGenerateProp()) {
            return newLinkedHashMap();
        }
        final List<DfProcedureMeta> procedureList = setupAvailableProcedureList(dataSource);

        // arrange handling (also duplicate check)
        final Map<String, DfProcedureMeta> procedureHandilngMap = arrangeProcedureHandilng(procedureList);

        // arrange order (additional schema after main schema)
        return arrangeProcedureOrder(procedureHandilngMap);
    }

    protected boolean isQuitByGenerateProp() {
        final DfOutsideSqlProperties prop = getOutsideSqlProperties();
        if (prop.isForcedlySuppressProcedureMetaData()) { // emergency option
            return true;
        }
        if (_suppressGenerationRestriction) {
            return false;
        }
        return !prop.isGenerateProcedureParameterBean();
    }

    protected List<DfProcedureMeta> setupAvailableProcedureList(DfSchemaSource dataSource) throws SQLException {
        // main schema
        final List<DfProcedureMeta> procedureList = getPlainProcedureList(dataSource, dataSource.getSchema());

        // additional schema
        setupAdditionalSchemaProcedure(dataSource, procedureList);

        // procedure synonym
        setupProcedureSynonym(procedureList);

        // included procedure to DB link
        setupProcedureToDBLinkIncluded(procedureList);

        // resolve overload and great walls...
        resolveAssistInfo(dataSource, procedureList);

        // filter the list of procedure by DBFlute property
        return filterByProperty(procedureList);
    }

    protected Map<String, DfProcedureMeta> arrangeProcedureHandilng(List<DfProcedureMeta> procedureList) {
        final Map<String, DfProcedureMeta> procedureHandlingMap = newLinkedHashMap();
        for (DfProcedureMeta metaInfo : procedureList) {
            if (handleDuplicateProcedure(metaInfo, procedureHandlingMap)) {
                continue;
            }
            procedureHandlingMap.put(metaInfo.buildProcedureKeyName(), metaInfo);
        }
        return procedureHandlingMap;
    }

    protected Map<String, DfProcedureMeta> arrangeProcedureOrder(Map<String, DfProcedureMeta> procedureHandlingMap) {
        final Map<String, DfProcedureMeta> procedureOrderedMap = newLinkedHashMap();
        final Map<String, DfProcedureMeta> additionalSchemaProcedureMap = newLinkedHashMap();
        final Set<Entry<String, DfProcedureMeta>> entrySet = procedureHandlingMap.entrySet();
        for (Entry<String, DfProcedureMeta> entry : entrySet) {
            final String key = entry.getKey();
            final DfProcedureMeta metaInfo = entry.getValue();
            if (metaInfo.getProcedureSchema().isAdditionalSchema()) {
                additionalSchemaProcedureMap.put(key, metaInfo);
            } else {
                procedureOrderedMap.put(key, metaInfo); // main schema
            }
        }
        procedureOrderedMap.putAll(additionalSchemaProcedureMap);
        return procedureOrderedMap;
    }

    // -----------------------------------------------------
    //                                     Additional Schema
    //                                     -----------------
    protected void setupAdditionalSchemaProcedure(DfSchemaSource dataSource, List<DfProcedureMeta> procedureList) throws SQLException {
        if (_suppressAdditionalSchema) {
            return;
        }
        final DfDatabaseProperties databaseProp = getProperties().getDatabaseProperties();
        final List<UnifiedSchema> additionalSchemaList = databaseProp.getAdditionalSchemaList();
        for (UnifiedSchema additionalSchema : additionalSchemaList) {
            final DfAdditionalSchemaInfo schemaInfo = databaseProp.getAdditionalSchemaInfo(additionalSchema);
            if (schemaInfo.isSuppressProcedure()) {
                continue;
            }
            final List<DfProcedureMeta> additionalProcedureList = getPlainProcedureList(dataSource, additionalSchema);
            procedureList.addAll(additionalProcedureList);
        }
    }

    // -----------------------------------------------------
    //                                     Procedure Synonym
    //                                     -----------------
    protected void setupProcedureSynonym(List<DfProcedureMeta> procedureList) {
        if (_procedureSynonymDataSource == null) {
            return;
        }
        final DfOutsideSqlProperties prop = getOutsideSqlProperties();
        final ProcedureSynonymHandlingType handlingType = prop.getProcedureSynonymHandlingType();
        if (handlingType.equals(ProcedureSynonymHandlingType.NONE)) {
            return;
        }
        final DfProcedureSynonymExtractor extractor = createProcedureSynonymExtractor();
        if (extractor == null) {
            return; // unsupported at the database
        }
        final Map<String, DfProcedureSynonymMeta> procedureSynonymMap = extractor.extractProcedureSynonymMap();
        if (handlingType.equals(ProcedureSynonymHandlingType.INCLUDE)) {
            // only add procedure synonyms to the procedure list
        } else if (handlingType.equals(ProcedureSynonymHandlingType.SWITCH)) {
            log("...Clearing normal procedures: count=" + procedureList.size());
            procedureList.clear(); // because of switch
        } else {
            String msg = "Unexpected handling type of procedure sysnonym: " + handlingType;
            throw new IllegalStateException(msg);
        }
        log("...Adding procedure synonyms as procedure: count=" + procedureSynonymMap.size());
        final List<DfProcedureMeta> procedureSynonymList = new ArrayList<DfProcedureMeta>();
        for (Entry<String, DfProcedureSynonymMeta> entry : procedureSynonymMap.entrySet()) {
            final DfProcedureSynonymMeta metaInfo = entry.getValue();
            if (!isSynonymAllowedSchema(metaInfo)) {
                continue;
            }

            // merge synonym to procedure (create copied instance)
            final String beforeName = metaInfo.getProcedureMetaInfo().buildProcedureLoggingName();
            final DfProcedureMeta mergedProcedure = metaInfo.createMergedProcedure();
            final String afterName = mergedProcedure.buildProcedureLoggingName();
            log("  " + beforeName + " to " + afterName);

            procedureSynonymList.add(mergedProcedure);
        }
        procedureList.addAll(procedureSynonymList);
    }

    protected boolean isSynonymAllowedSchema(DfProcedureSynonymMeta procedureSynonymMetaInfo) {
        final DfSynonymMeta synonymMetaInfo = procedureSynonymMetaInfo.getSynonymMetaInfo();
        final UnifiedSchema synonymOwner = synonymMetaInfo.getSynonymOwner();
        final DfDatabaseProperties databaseProperties = getProperties().getDatabaseProperties();
        final DfAdditionalSchemaInfo additionalSchemaInfo = databaseProperties.getAdditionalSchemaInfo(synonymOwner);
        if (additionalSchemaInfo != null) {
            return additionalSchemaInfo.hasObjectTypeSynonym();
        } else {
            return databaseProperties.hasObjectTypeSynonym(); // as main schema
        }
    }

    /**
     * @return The extractor of procedure synonym. (NullAllowed)
     */
    protected DfProcedureSynonymExtractor createProcedureSynonymExtractor() {
        final DfProcedureSynonymExtractorFactory factory =
                new DfProcedureSynonymExtractorFactory(_procedureSynonymDataSource, getDatabaseTypeFacadeProp(), getDatabaseProperties());
        return factory.createSynonymExtractor();
    }

    protected DfDatabaseProperties getDatabaseProperties() {
        return DfBuildProperties.getInstance().getDatabaseProperties();
    }

    // -----------------------------------------------------
    //                          Included Procedure to DBLink
    //                          ----------------------------
    protected void setupProcedureToDBLinkIncluded(List<DfProcedureMeta> procedureList) {
        if (_procedureToDBLinkDataSource == null) {
            return;
        }
        final DfProcedureNativeTranslatorOracle translator = new DfProcedureNativeTranslatorOracle(_procedureToDBLinkDataSource);
        final DfOutsideSqlProperties prop = getOutsideSqlProperties();
        final List<String> procedureNameToDBLinkList = prop.getTargetProcedureNameToDBLinkList();
        for (String propertyName : procedureNameToDBLinkList) {
            final String packageName;
            final String procedureName;
            final String dbLinkName;
            final String nameResource;
            if (propertyName.contains(".")) {
                packageName = Srl.substringLastFront(propertyName, ".");
                nameResource = Srl.substringLastRear(propertyName, ".");
            } else {
                packageName = null;
                nameResource = propertyName;
            }
            procedureName = Srl.substringLastFront(nameResource, "@");
            dbLinkName = Srl.substringLastRear(nameResource, "@");
            final DfProcedureMeta meta = translator.translateProcedureToDBLink(packageName, procedureName, dbLinkName, this);
            if (meta == null) {
                throwProcedureToDBLinkTranslationFailureException(propertyName, packageName, procedureName, dbLinkName);
            }
            meta.setIncludedProcedureToDBLink(true);
            procedureList.add(meta);
        }
    }

    protected void throwProcedureToDBLinkTranslationFailureException(String propertyName, String packageName, String procedureName,
            String dbLinkName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to translate the procedure to DB link.");
        br.addItem("Advice");
        br.addElement("Make sure your procedure name is correct.");
        br.addElement("Does the DBLink name exist on the schema?");
        br.addItem("Specified Property");
        br.addElement(propertyName);
        br.addItem("Package Name");
        br.addElement(packageName);
        br.addItem("Procedure Name");
        br.addElement(procedureName);
        br.addItem("DBLink Name");
        br.addElement(dbLinkName);
        final String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg);
    }

    // -----------------------------------------------------
    //                                    Filter by Property
    //                                    ------------------
    protected List<DfProcedureMeta> filterByProperty(List<DfProcedureMeta> procedureList) {
        if (_suppressFilterByProperty) {
            return procedureList;
        }
        final DfOutsideSqlProperties prop = getOutsideSqlProperties();
        final List<DfProcedureMeta> resultList = new ArrayList<DfProcedureMeta>();
        log("...Filtering procedures by the property: before=" + procedureList.size());
        int passedCount = 0;
        for (DfProcedureMeta meta : procedureList) {
            if (isTargetByProperty(meta, prop)) {
                resultList.add(meta);
            } else {
                ++passedCount;
            }
        }
        if (passedCount == 0) {
            log(" -> All procedures are target: count=" + procedureList.size());
        }
        return resultList;
    }

    protected boolean isTargetByProperty(DfProcedureMeta meta, DfOutsideSqlProperties prop) {
        if (meta.isIncludedProcedureToDBLink()) { // is fixed setting
            return true;
        }
        final String procedureLoggingName = meta.buildProcedureLoggingName();
        final String procedureCatalog = meta.getProcedureCatalog();
        if (!prop.isTargetProcedureCatalog(procedureCatalog)) {
            log("  passed: non-target catalog - " + procedureLoggingName);
            return false;
        }
        final UnifiedSchema procedureSchema = meta.getProcedureSchema();
        if (!prop.isTargetProcedureSchema(procedureSchema.getPureSchema())) {
            log("  passed: non-target schema - " + procedureLoggingName);
            return false;
        }
        final String procedureFullQualifiedName = meta.getProcedureFullQualifiedName();
        final String procedureSchemaQualifiedName = Srl.substringFirstFront(procedureFullQualifiedName, ".");
        final String procedureName = meta.getProcedureName();
        if (!prop.isTargetProcedureName(procedureFullQualifiedName) && !prop.isTargetProcedureName(procedureSchemaQualifiedName)
                && !prop.isTargetProcedureName(procedureName)) {
            log("  passed: non-target name - " + procedureLoggingName);
            return false;
        }
        return true;
    }

    // -----------------------------------------------------
    //                                   Duplicate Procedure
    //                                   -------------------
    /**
     * @param second The second procedure being processed current loop. (NotNull)
     * @param procedureHandlingMap The handling map of procedure. (NotNull)
     * @return Does it skip to register the second procedure?
     */
    protected boolean handleDuplicateProcedure(DfProcedureMeta second, Map<String, DfProcedureMeta> procedureHandlingMap) {
        final String procedureKeyName = second.buildProcedureKeyName();
        final DfProcedureMeta first = procedureHandlingMap.get(procedureKeyName);
        if (first == null) {
            return false; // not duplicate
        }
        final UnifiedSchema firstSchema = first.getProcedureSchema();
        final UnifiedSchema secondSchema = second.getProcedureSchema();
        // basically select the one of main schema.
        if (!firstSchema.equals(secondSchema)) {
            if (firstSchema.isMainSchema()) {
                showDuplicateProcedure(first, second, true, "main schema");
                return true; // use first so skip
            } else if (secondSchema.isMainSchema()) {
                procedureHandlingMap.remove(procedureKeyName);
                showDuplicateProcedure(first, second, false, "main schema");
                return false; // use second so NOT skip (override)
            }
        }
        // if both are additional schema or main schema, it selects first. 
        showDuplicateProcedure(first, second, true, "first one");
        return true;
    }

    protected void showDuplicateProcedure(DfProcedureMeta first, DfProcedureMeta second, boolean electFirst, String reason) {
        final String firstName = first.buildProcedureLoggingName();
        final String secondName = second.buildProcedureLoggingName();
        final String firstType = first.isProcedureSynonym() ? "(synonym)" : "";
        final String secondType = second.isProcedureSynonym() ? "(synonym)" : "";
        String msg = "*Found the same-name procedure, so elects " + reason + ":";
        if (electFirst) {
            msg = msg + " elect=" + firstName + firstType + " skipped=" + secondName + secondType;
        } else {
            msg = msg + " elect=" + secondName + secondType + " skipped=" + firstName + firstType;
        }
        log(msg);
    }

    // ===================================================================================
    //                                                                     Plain Procedure
    //                                                                     ===============
    /**
     * Get the list of plain procedures. <br>
     * It selects procedures of specified schema only.
     * @param dataSource Data source. (NotNull)
     * @param unifiedSchema The unified schema that can contain catalog name and no-name mark. (NullAllowed)
     * @return The list of procedure meta information. (NotNull)
     */
    public List<DfProcedureMeta> getPlainProcedureList(DataSource dataSource, UnifiedSchema unifiedSchema) throws SQLException {
        final List<DfProcedureMeta> procedureMetaList = new ArrayList<DfProcedureMeta>();
        String procedureName = null;
        Connection conn = null;
        ResultSet procedureRs = null;
        try {
            conn = dataSource.getConnection();
            final DatabaseMetaData metaData = conn.getMetaData();
            procedureRs = doGetProcedures(metaData, unifiedSchema);
            setupProcedureMeta(procedureMetaList, procedureRs, unifiedSchema);
            for (DfProcedureMeta procedureMeta : procedureMetaList) {
                procedureName = procedureMeta.getProcedureName();
                ResultSet columnRs = null;
                try {
                    columnRs = doGetProcedureColumns(metaData, procedureMeta, /*retry*/false);
                    setupProcedureColumnMeta(procedureMeta, columnRs);
                } finally {
                    closeResult(columnRs);
                }
                retryGetProcedureColumns(metaData, procedureMeta);
                adjustProcedureColumnList(procedureMeta);
            }
        } catch (SQLException e) {
            throwProcedureListGettingFailureException(unifiedSchema, procedureName, e);
            return null; // unreachable
        } catch (RuntimeException e) { // for an unexpected exception from JDBC driver
            throwProcedureListGettingFailureException(unifiedSchema, procedureName, e);
            return null; // unreachable
        } finally {
            closeResult(procedureRs);
            closeConnection(conn);
        }
        return procedureMetaList;
    }

    protected void retryGetProcedureColumns(DatabaseMetaData metaData, DfProcedureMeta procedureMeta) throws SQLException {
        if (isDatabaseMySQL() && procedureMeta.getProcedureColumnList().isEmpty()) {
            ResultSet columnRs = null;
            try {
                // to avoid MySQL JDBC-8.0.x headache, see this method for the detail
                columnRs = doGetProcedureColumns(metaData, procedureMeta, /*retry*/true);
                if (columnRs != null) { // null allowed if retry
                    setupProcedureColumnMeta(procedureMeta, columnRs);
                }
            } finally {
                closeResult(columnRs);
            }
        }
    }

    // -----------------------------------------------------
    //                                            Procedures
    //                                            ----------
    protected ResultSet doGetProcedures(DatabaseMetaData metaData, UnifiedSchema unifiedSchema) throws SQLException {
        final String catalogName = unifiedSchema.getPureCatalog();
        final String schemaName = unifiedSchema.getPureSchema();
        return metaData.getProcedures(catalogName, schemaName, null);
    }

    // -----------------------------------------------------
    //                                        Procedure Meta
    //                                        --------------
    protected void setupProcedureMeta(List<DfProcedureMeta> procedureMetaList, ResultSet procedureRs, UnifiedSchema unifiedSchema)
            throws SQLException {
        boolean skippedPgCatalog = false;
        while (procedureRs.next()) {
            // /- - - - - - - - - - - - - - - - - - - - - - - -
            // same policy as table process about JDBC handling
            // (see DfTableHandler.java)
            // - - - - - - - - - -/

            final String procedureSchema = procedureRs.getString("PROCEDURE_SCHEM");
            if (isDatabasePostgreSQL()) {
                if (procedureSchema != null && procedureSchema.equals("pg_catalog")) {
                    // skip pg_catalog's procedures, that are embedded in PostgreSQL
                    // (they can be here when the user is 'postgres')
                    skippedPgCatalog = true;
                    continue;
                }
            }
            final String procedurePackage;
            final String procedureCatalog;
            final String procedureName;
            {
                final String plainCatalog = procedureRs.getString("PROCEDURE_CAT");
                if (isDatabaseOracle()) {
                    // because Oracle treats catalog as package
                    if (Srl.is_NotNull_and_NotTrimmedEmpty(plainCatalog)) {
                        procedurePackage = plainCatalog;
                    } else {
                        procedurePackage = null;
                    }
                    procedureCatalog = null;
                } else {
                    procedurePackage = null;
                    if (Srl.is_NotNull_and_NotTrimmedEmpty(plainCatalog)) {
                        procedureCatalog = plainCatalog;
                    } else {
                        procedureCatalog = unifiedSchema.getPureCatalog();
                    }
                }
                final String plainName = procedureRs.getString("PROCEDURE_NAME");
                if (Srl.is_NotNull_and_NotTrimmedEmpty(procedurePackage)) {
                    procedureName = procedurePackage + "." + plainName;
                } else {
                    procedureName = plainName;
                }
            }
            final Integer procedureType = Integer.valueOf(procedureRs.getString("PROCEDURE_TYPE"));
            final String procedureComment = procedureRs.getString("REMARKS");
            final String procedureSpecificName = prepareProcedureSpecificName(procedureRs, procedureName); // null allowed

            final DfProcedureMeta procedureMeta = new DfProcedureMeta();
            procedureMeta.setProcedureCatalog(procedureCatalog);
            procedureMeta.setProcedureSchema(createAsDynamicSchema(procedureCatalog, procedureSchema));
            procedureMeta.setProcedureName(procedureName);
            if (procedureType == DatabaseMetaData.procedureResultUnknown) {
                procedureMeta.setProcedureType(DfProcedureType.procedureResultUnknown);
            } else if (procedureType == DatabaseMetaData.procedureNoResult) {
                procedureMeta.setProcedureType(DfProcedureType.procedureNoResult);
            } else if (procedureType == DatabaseMetaData.procedureReturnsResult) {
                procedureMeta.setProcedureType(DfProcedureType.procedureReturnsResult);
            } else {
                String msg = "Unknown procedureType: type=" + procedureType + " procedure=" + procedureName;
                throw new IllegalStateException(msg);
            }
            procedureMeta.setProcedureComment(procedureComment);
            procedureMeta.setProcedurePackage(procedurePackage);
            procedureMeta.setProcedureSpecificName(procedureSpecificName);
            procedureMeta.setProcedureFullQualifiedName(buildProcedureFullQualifiedName(procedureMeta));
            procedureMeta.setProcedureSchemaQualifiedName(buildProcedureSchemaQualifiedName(procedureMeta));
            procedureMetaList.add(procedureMeta);
        }
        if (skippedPgCatalog) {
            _log.info("*Skipped pg_catalog's procedures");
        }
    }

    protected String prepareProcedureSpecificName(ResultSet procedureRs, String procedureName) {
        final String specificNameLabel = "SPECIFIC_NAME";
        String procedureSpecificName = null; // not required
        try {
            // at least MySQL, PostgreSQL always return valid names (2023/10/30)
            procedureSpecificName = procedureRs.getString(specificNameLabel);
        } catch (Exception e) { // just in case, needs confirmation per DBMSs
            final String causeMsg = e.getMessage();
            _log.debug("Cannot get the value of " + specificNameLabel + " for the procedure: " + procedureName + " // " + causeMsg);
        }
        return procedureSpecificName;
    }

    protected String buildProcedureFullQualifiedName(DfProcedureMeta procedureMeta) {
        return procedureMeta.getProcedureSchema().buildFullQualifiedName(procedureMeta.getProcedureName());
    }

    protected String buildProcedureSchemaQualifiedName(DfProcedureMeta procedureMeta) {
        return procedureMeta.getProcedureSchema().buildSchemaQualifiedName(procedureMeta.getProcedureName());
    }

    // -----------------------------------------------------
    //                                     Procedure Columns
    //                                     -----------------
    protected ResultSet doGetProcedureColumns(DatabaseMetaData metaData, DfProcedureMeta procedureMeta, boolean retry) throws SQLException {
        final String catalogName = procedureMeta.getProcedureCatalog();
        final String schemaName = procedureMeta.getProcedureSchema().getPureSchema();
        final String procedurePureName = procedureMeta.buildProcedurePureName();
        final String catalogArgName;
        final String procedureArgName;
        if (isDatabaseMySQL() && Srl.is_NotNull_and_NotTrimmedEmpty(catalogName)) {
            // getProcedureColumns() of MySQL requires qualified procedure name when other catalog
            // however, MySQL JDBC-8.0.x headache is here: https://github.com/dbflute/dbflute-core/issues/208
            catalogArgName = catalogName;
            if (retry) {
                // since (maybe) MySQL JDBC-8.0.x + MySQL-8.0.x, needs to use pure name
                procedureArgName = procedurePureName;
            } else {
                // as default (basically for under JDBC-5.x or MySQL-5.7.x)
                procedureArgName = Srl.connectPrefix(procedurePureName, catalogName, ".");
            }
        } else if (isDatabaseOracle() && procedureMeta.isPackageProcdure()) {
            catalogArgName = procedureMeta.getProcedurePackage();
            procedureArgName = procedurePureName; // needs to use pure name
        } else {
            catalogArgName = catalogName;
            procedureArgName = procedurePureName;
        }
        try {
            return metaData.getProcedureColumns(catalogArgName, schemaName, procedureArgName, null);
        } catch (RuntimeException | SQLException e) {
            if (retry) {
                _log.debug("*Failed to retry getting procedure columns: procedure=" + procedureMeta, e);
                return null; // null allowed if retry
            } else {
                throw e;
            }
        }
    }

    // -----------------------------------------------------
    //                                           Column Meta
    //                                           -----------
    protected void setupProcedureColumnMeta(DfProcedureMeta procedureMeta, ResultSet columnRs) throws SQLException {
        final Set<String> uniqueSet = new HashSet<String>();
        while (columnRs.next()) {
            // /- - - - - - - - - - - - - - - - - - - - - - - -
            // same policy as table process about JDBC handling
            // (see DfTableHandler.java)
            // - - - - - - - - - -/

            // _/_/_/_/_/_/_/_/_/_/_/_/
            // prepare basic attributes
            // _/_/_/_/_/
            final String columnName = columnRs.getString("COLUMN_NAME");

            final Integer procedureColumnType;
            {
                final String columnType = columnRs.getString("COLUMN_TYPE");
                final int unknowType = DatabaseMetaData.procedureColumnUnknown;
                if (Srl.is_NotNull_and_NotTrimmedEmpty(columnType)) {
                    procedureColumnType = toInt("columnType", columnType);
                } else {
                    procedureColumnType = unknowType;
                }
            }

            final int jdbcType;
            {
                int tmpJdbcType = Types.OTHER;
                String dataType = null;
                try {
                    dataType = columnRs.getString("DATA_TYPE");
                } catch (RuntimeException ignored) { // pinpoint patch
                    // for example, SQLServer throws an exception
                    // if the procedure is a function that returns table type
                    final String procdureName = procedureMeta.getProcedureFullQualifiedName();
                    log("*Failed to get data type: " + procdureName + "." + columnName);
                    tmpJdbcType = Types.OTHER;
                }
                if (Srl.is_NotNull_and_NotTrimmedEmpty(dataType)) {
                    tmpJdbcType = toInt("dataType", dataType);
                }
                jdbcType = tmpJdbcType;
            }

            final String dbTypeName = columnRs.getString("TYPE_NAME");

            // uses getString() to get null value
            // (getInt() returns zero when a value is no defined)
            final Integer columnSize;
            {
                final String precision = columnRs.getString("PRECISION");
                if (Srl.is_NotNull_and_NotTrimmedEmpty(precision)) {
                    columnSize = toInt("precision", precision);
                } else {
                    final String length = columnRs.getString("LENGTH");
                    if (Srl.is_NotNull_and_NotTrimmedEmpty(length)) {
                        columnSize = toInt("length", length);
                    } else {
                        columnSize = null;
                    }
                }
            }
            final Integer decimalDigits;
            {
                final String scale = columnRs.getString("SCALE");
                if (Srl.is_NotNull_and_NotTrimmedEmpty(scale)) {
                    decimalDigits = toInt("scale", scale);
                } else {
                    decimalDigits = null;
                }
            }
            final String columnComment = columnRs.getString("REMARKS");
            final String columnSpecificName = prepareColumnSpecificName(columnRs, procedureMeta, columnName); // null allowed

            // _/_/_/_/_/_/_/_/_/_/_/_/
            // filtering arguments
            // _/_/_/_/_/
            if (determineOverloadDifferentColumn(procedureMeta, columnSpecificName)) {
                // filter other overload procedure's parameters
                // (because JDBC method parameters are only name search)
                continue;
            }
            if (uniqueSet.contains(columnName)) {
                // filter duplicated informations because Oracle package procedure may return them
                // should be after specificName determination for no-name argument overload
                continue;
            }
            uniqueSet.add(columnName);

            // _/_/_/_/_/_/_/_/_/_/_/_/
            // setting to meta object
            // _/_/_/_/_/
            final DfProcedureColumnMeta procedureColumnMeta = new DfProcedureColumnMeta();
            procedureColumnMeta.setColumnName(columnName);
            if (procedureColumnType == DatabaseMetaData.procedureColumnUnknown) {
                procedureColumnMeta.setProcedureColumnType(DfProcedureColumnType.procedureColumnUnknown);
            } else if (procedureColumnType == DatabaseMetaData.procedureColumnIn) {
                procedureColumnMeta.setProcedureColumnType(DfProcedureColumnType.procedureColumnIn);
            } else if (procedureColumnType == DatabaseMetaData.procedureColumnInOut) {
                procedureColumnMeta.setProcedureColumnType(DfProcedureColumnType.procedureColumnInOut);
            } else if (procedureColumnType == DatabaseMetaData.procedureColumnOut) {
                procedureColumnMeta.setProcedureColumnType(DfProcedureColumnType.procedureColumnOut);
            } else if (procedureColumnType == DatabaseMetaData.procedureColumnReturn) {
                procedureColumnMeta.setProcedureColumnType(DfProcedureColumnType.procedureColumnReturn);
            } else if (procedureColumnType == DatabaseMetaData.procedureColumnResult) {
                procedureColumnMeta.setProcedureColumnType(DfProcedureColumnType.procedureColumnResult);
            } else {
                throw new IllegalStateException("Unknown procedureColumnType: " + procedureColumnType);
            }
            procedureColumnMeta.setJdbcDefType(jdbcType);
            procedureColumnMeta.setDbTypeName(dbTypeName);
            procedureColumnMeta.setColumnSize(columnSize);
            procedureColumnMeta.setDecimalDigits(decimalDigits);
            procedureColumnMeta.setColumnComment(columnComment);
            procedureColumnMeta.setColumnSpecificName(columnSpecificName);
            procedureMeta.addProcedureColumn(procedureColumnMeta);
        }
    }

    protected int toInt(String title, String value) {
        try {
            return Integer.valueOf(value).intValue();
        } catch (NumberFormatException e) {
            String msg = "Failed to convert the value to integer:";
            msg = msg + " title=" + title + " value=" + value;
            throw new IllegalStateException(msg, e);
        }
    }

    // -----------------------------------------------------
    //                             Overload Different Column
    //                             -------------------------
    protected String prepareColumnSpecificName(ResultSet columnRs, DfProcedureMeta procedureMeta, String columnName) {
        // the JDBC internal attribute to identify overload procedures by jflute (2023/10/30)
        // it depends on DBMS so it needs confirmation
        final String specificNameLabel = "SPECIFIC_NAME";
        String columnSpecificName = null; // not required
        try {
            // at least MySQL, PostgreSQL always return valid names (2023/10/30)
            columnSpecificName = columnRs.getString(specificNameLabel);
        } catch (Exception e) { // just in case, needs confirmation per DBMSs
            final String exceptionMessage = e.getMessage();
            _log.debug("Cannot get the value of " + specificNameLabel + " for the procedure column: " + procedureMeta.getProcedureName()
                    + "." + columnName + " // " + exceptionMessage);
        }
        return columnSpecificName;
    }

    protected boolean determineOverloadDifferentColumn(DfProcedureMeta procedureMeta, String columnSpecificName) {
        if (columnSpecificName == null) {
            return false;
        }
        final String procedureSpecificName = procedureMeta.getProcedureSpecificName();
        if (procedureSpecificName == null) {
            return false;
        }
        if (isSpecificNameOverloadIdentification()) {
            if (!procedureSpecificName.equals(columnSpecificName)) {
                // e.g. sp_in_out_parameter_24581, sp_in_out_parameter_24582
                // different column for other overload procedures
                return true;
            }
        }
        return false;
    }

    protected boolean isSpecificNameOverloadIdentification() {
        // #for_now jflute confirmed DBMS and overload-supported DBMS only (2023/10/30)
        return isDatabasePostgreSQL();
    }

    // -----------------------------------------------------
    //                                 ColumnList Adjustment
    //                                 ---------------------
    protected void adjustProcedureColumnList(DfProcedureMeta procedureMeta) {
        adjustPostgreSQLResultSetParameter(procedureMeta);
        adjustPostgreSQLVoidReturn(procedureMeta);
    }

    protected void adjustPostgreSQLResultSetParameter(DfProcedureMeta procedureMeta) {
        if (!isDatabasePostgreSQL()) {
            return;
        }
        final List<DfProcedureColumnMeta> columnMetaList = procedureMeta.getProcedureColumnList();
        boolean existsResultSetParameter = false;
        boolean existsResultSetReturn = false;
        int resultSetReturnIndex = 0;
        String resultSetReturnName = null;
        int index = 0;
        for (DfProcedureColumnMeta columnMeta : columnMetaList) {
            final DfProcedureColumnType procedureColumnType = columnMeta.getProcedureColumnType();
            final String dbTypeName = columnMeta.getDbTypeName();
            if (procedureColumnType.equals(DfProcedureColumnType.procedureColumnOut)) {
                if ("refcursor".equalsIgnoreCase(dbTypeName)) {
                    existsResultSetParameter = true;
                }
            }
            if (procedureColumnType.equals(DfProcedureColumnType.procedureColumnReturn)) {
                if ("refcursor".equalsIgnoreCase(dbTypeName)) {
                    existsResultSetReturn = true;
                    resultSetReturnIndex = index;
                    resultSetReturnName = columnMeta.getColumnName();
                }
            }
            ++index;
        }
        if (existsResultSetParameter && existsResultSetReturn) {
            // It is a precondition that PostgreSQL does not allow functions to have a result set return
            // when it also has result set parameters (as an out parameter).
            removePostgreSQLUnnecessaryColumn(procedureMeta, columnMetaList, resultSetReturnIndex, resultSetReturnName);
        }
    }

    protected void adjustPostgreSQLVoidReturn(DfProcedureMeta procedureMeta) {
        if (!isDatabasePostgreSQL()) {
            return;
        }
        final List<DfProcedureColumnMeta> columnMetaList = procedureMeta.getProcedureColumnList();
        boolean existsVoidReturn = false;
        int resultSetReturnIndex = 0;
        String resultSetReturnName = null;
        int index = 0;
        for (DfProcedureColumnMeta columnMeta : columnMetaList) {
            final DfProcedureColumnType procedureColumnType = columnMeta.getProcedureColumnType();
            final String dbTypeName = columnMeta.getDbTypeName();
            if (procedureColumnType.equals(DfProcedureColumnType.procedureColumnReturn)) {
                if ("void".equalsIgnoreCase(dbTypeName)) {
                    existsVoidReturn = true;
                    resultSetReturnIndex = index;
                    resultSetReturnName = columnMeta.getColumnName();
                }
            }
            ++index;
        }
        if (existsVoidReturn) {
            // PostgreSQL (real) procedure may have "void" type return by jflute (2022/04/10)
            removePostgreSQLUnnecessaryColumn(procedureMeta, columnMetaList, resultSetReturnIndex, resultSetReturnName);
        }
    }

    protected void removePostgreSQLUnnecessaryColumn(DfProcedureMeta procedureMeta, List<DfProcedureColumnMeta> columnMetaList,
            int resultSetReturnIndex, String resultSetReturnName) {
        String name = procedureMeta.buildProcedureLoggingName() + "." + resultSetReturnName;
        log("...Removing the result set return which is unnecessary: " + name);
        columnMetaList.remove(resultSetReturnIndex);
    }

    // -----------------------------------------------------
    //                                          Assist Logic
    //                                          ------------
    protected void throwProcedureListGettingFailureException(UnifiedSchema unifiedSchema, String procedureName, Exception e)
            throws SQLException {
        final boolean forSqlEx = e instanceof SQLException;
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to get a list of procedures.");
        br.addItem("Unified Schema");
        br.addElement(unifiedSchema);
        br.addItem("Current Procedure");
        br.addElement(procedureName);
        br.addItem(forSqlEx ? "Caused SQLException" : "Unexpected Exception");
        br.addElement(e.getClass().getName());
        br.addElement(e.getMessage());
        final String msg = br.buildExceptionMessage();
        if (forSqlEx) {
            throw new DfJDBCException(msg, (SQLException) e);
        } else {
            throw new DfProcedureListGettingFailureException(msg, e);
        }
    }

    protected void closeResult(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ignored) {}
        }
    }

    protected void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ignored) {}
        }
    }

    // ===================================================================================
    //                                                                         Assist Info
    //                                                                         ===========
    protected void resolveAssistInfo(DfSchemaSource dataSource, List<DfProcedureMeta> metaInfoList) {
        if (isDatabaseMySQL()) {
            doResolveAssistInfoMySQL(dataSource, metaInfoList);
        } else if (isDatabasePostgreSQL()) {
            doResolveAssistInfoPostgreSQL(dataSource, metaInfoList);
        } else if (isDatabaseOracle()) {
            doResolveAssistInfoOracle(dataSource, metaInfoList);
        } else if (isDatabaseDB2()) {
            doResolveAssistInfoDB2(dataSource, metaInfoList);
        } else if (isDatabaseH2()) {
            doResolveAssistInfoH2(dataSource, metaInfoList);
        }
    }

    // -----------------------------------------------------
    //                                                 MySQL
    //                                                 -----
    protected void doResolveAssistInfoMySQL(DfSchemaSource dataSource, List<DfProcedureMeta> metaInfoList) {
        final DfProcedureSupplementExtractor extractor = getSupplementExtractorMySQL(dataSource);

        // source info
        final boolean reflectParamsToHash = true; // cannot get parameter info from MySQL
        doSetupSourceInfo(dataSource, metaInfoList, extractor, dataSource.getSchema(), reflectParamsToHash);
        final List<UnifiedSchema> additionalSchemaList = getDatabaseProperties().getAdditionalSchemaList();
        for (UnifiedSchema additionalSchema : additionalSchemaList) {
            doSetupSourceInfo(dataSource, metaInfoList, extractor, additionalSchema, reflectParamsToHash);
        }
    }

    // -----------------------------------------------------
    //                                            PostgreSQL
    //                                            ----------
    protected void doResolveAssistInfoPostgreSQL(DfSchemaSource dataSource, List<DfProcedureMeta> metaInfoList) {
        final DfProcedureSupplementExtractor extractor = getSupplementExtractorPostgreSQL(dataSource);

        // source info
        final boolean reflectParamsToHash = true; // cannot get parameter info from MySQL
        doSetupSourceInfo(dataSource, metaInfoList, extractor, dataSource.getSchema(), reflectParamsToHash);
        final List<UnifiedSchema> additionalSchemaList = getDatabaseProperties().getAdditionalSchemaList();
        for (UnifiedSchema additionalSchema : additionalSchemaList) {
            doSetupSourceInfo(dataSource, metaInfoList, extractor, additionalSchema, reflectParamsToHash);
        }
    }

    // -----------------------------------------------------
    //                                                Oracle
    //                                                ------
    protected void doResolveAssistInfoOracle(DfSchemaSource dataSource, List<DfProcedureMeta> metaInfoList) {
        final UnifiedSchema mainSchema = dataSource.getSchema();
        final List<UnifiedSchema> additionalSchemaList = getDatabaseProperties().getAdditionalSchemaList();

        // overload
        final DfProcedureSupplementExtractorOracle extractor = getSupplementExtractorOracle(dataSource);
        final Map<UnifiedSchema, Map<String, Integer>> overloadInfoMapMap = newHashMap();
        overloadInfoMapMap.put(mainSchema, extractor.extractParameterOverloadInfoMap(mainSchema));
        for (UnifiedSchema additionalSchema : additionalSchemaList) {
            overloadInfoMapMap.put(additionalSchema, extractor.extractParameterOverloadInfoMap(additionalSchema));
        }
        doSetupOverloadInfoOracle(overloadInfoMapMap, metaInfoList, extractor);

        // great wall
        // get all available schema's info to use other schema's type
        // same-name type between schema is unsupported
        final StringKeyMap<DfTypeArrayInfo> arrayInfoMap = extractor.extractParameterArrayInfoMap(mainSchema);
        for (UnifiedSchema additionalSchema : additionalSchemaList) {
            arrayInfoMap.putAll(extractor.extractParameterArrayInfoMap(additionalSchema));
        }
        final StringKeyMap<DfTypeStructInfo> structInfoMap = extractor.extractStructInfoMap(mainSchema);
        for (UnifiedSchema additionalSchema : additionalSchemaList) {
            structInfoMap.putAll(extractor.extractStructInfoMap(additionalSchema));
        }
        doSetupGreatWallOracle(arrayInfoMap, structInfoMap, metaInfoList, extractor);

        // source info
        final boolean reflectParamsToHash = false; // can get parameter definition code from Oracle
        doSetupSourceInfo(dataSource, metaInfoList, extractor, mainSchema, reflectParamsToHash);
        for (UnifiedSchema additionalSchema : additionalSchemaList) {
            doSetupSourceInfo(dataSource, metaInfoList, extractor, additionalSchema, reflectParamsToHash);
        }
    }

    protected void doSetupOverloadInfoOracle(Map<UnifiedSchema, Map<String, Integer>> parameterOverloadInfoMapMap,
            List<DfProcedureMeta> metaInfoList, DfProcedureSupplementExtractorOracle extractor) {
        for (DfProcedureMeta metaInfo : metaInfoList) {
            final String catalog = metaInfo.getProcedureCatalog();
            final String procedureName = metaInfo.getProcedureName();
            final List<DfProcedureColumnMeta> columnList = metaInfo.getProcedureColumnList();
            for (DfProcedureColumnMeta columnInfo : columnList) {
                final String columnName = columnInfo.getColumnName();
                final String key = extractor.generateParameterInfoMapKey(catalog, procedureName, columnName);

                // Overload
                if (columnInfo.getOverloadNo() == null) { // if not exists (it might be set by other processes)
                    final UnifiedSchema procedureSchema = metaInfo.getProcedureSchema();
                    final Map<String, Integer> overloadMap = parameterOverloadInfoMapMap.get(procedureSchema);
                    if (overloadMap != null) {
                        final Integer overloadNo = overloadMap.get(key);
                        if (overloadNo != null) {
                            columnInfo.setOverloadNo(overloadNo);
                        }
                    }
                }
            }
        }
    }

    // -----------------------------------------------------
    //                                                   DB2
    //                                                   ---
    protected void doResolveAssistInfoDB2(DfSchemaSource dataSource, List<DfProcedureMeta> metaInfoList) {
        final DfProcedureSupplementExtractor extractor = getSupplementExtractorDB2(dataSource);

        // source info
        final boolean reflectParamsToHash = false; // can get parameter definition code from DB2
        doSetupSourceInfo(dataSource, metaInfoList, extractor, dataSource.getSchema(), reflectParamsToHash);
        final List<UnifiedSchema> additionalSchemaList = getDatabaseProperties().getAdditionalSchemaList();
        for (UnifiedSchema additionalSchema : additionalSchemaList) {
            doSetupSourceInfo(dataSource, metaInfoList, extractor, additionalSchema, reflectParamsToHash);
        }
    }

    // -----------------------------------------------------
    //                                           H2 Database
    //                                           -----------
    protected void doResolveAssistInfoH2(DfSchemaSource dataSource, List<DfProcedureMeta> metaInfoList) {
        final DfProcedureSupplementExtractor extractor = getSupplementExtractorH2(dataSource);

        // source info
        final boolean reflectParamsToHash = false; // can get parameter definition code from H2
        doSetupSourceInfo(dataSource, metaInfoList, extractor, dataSource.getSchema(), reflectParamsToHash);
        final List<UnifiedSchema> additionalSchemaList = getDatabaseProperties().getAdditionalSchemaList();
        for (UnifiedSchema additionalSchema : additionalSchemaList) {
            doSetupSourceInfo(dataSource, metaInfoList, extractor, additionalSchema, reflectParamsToHash);
        }
    }

    // ===================================================================================
    //                                                                             DB Link
    //                                                                             =======
    protected void resolveAssistInfoToDBLink(DfSchemaSource dataSource, List<DfProcedureMeta> metaInfoList, String dbLinkName) {
        if (isDatabaseOracle()) {
            doResolveAssistInfoOracleToDBLink(dataSource, metaInfoList, dbLinkName);
        }
    }

    protected void doResolveAssistInfoOracleToDBLink(DfSchemaSource dataSource, List<DfProcedureMeta> metaInfoList, String dbLinkName) {
        final DfProcedureSupplementExtractorOracle extractor = getSupplementExtractorOracle(dataSource);

        // Overload
        final Map<String, Integer> overloadInfoMapMap = extractor.extractParameterOverloadInfoToDBLinkMap(dbLinkName);
        doSetupOverloadInfoOracleToDBLink(overloadInfoMapMap, metaInfoList, extractor);

        // GreatWall
        // DBLink procedure's GreatWalls are unsupported yet
        //final StringKeyMap<DfTypeArrayInfo> parameterArrayInfoMap = extractor.extractParameterArrayInfoToDBLinkMap();
        //final StringKeyMap<DfTypeStructInfo> structInfoMap = extractor.extractStructInfoToDBLinkMap();
        final StringKeyMap<DfTypeArrayInfo> parameterArrayInfoMap = StringKeyMap.createAsFlexible(); // empty
        final StringKeyMap<DfTypeStructInfo> structInfoMap = StringKeyMap.createAsFlexible(); // empty
        doSetupGreatWallOracle(parameterArrayInfoMap, structInfoMap, metaInfoList, extractor);
    }

    protected void doSetupOverloadInfoOracleToDBLink(Map<String, Integer> parameterOverloadInfoMap, List<DfProcedureMeta> metaInfoList,
            DfProcedureSupplementExtractorOracle extractor) {
        for (DfProcedureMeta metaInfo : metaInfoList) {
            final String catalog = metaInfo.getProcedureCatalog();
            final String procedureName = metaInfo.getProcedureName();
            final List<DfProcedureColumnMeta> columnList = metaInfo.getProcedureColumnList();
            for (DfProcedureColumnMeta columnInfo : columnList) {
                final String columnName = columnInfo.getColumnName();
                final String key = extractor.generateParameterInfoMapKey(catalog, procedureName, columnName);

                // Overload
                if (columnInfo.getOverloadNo() == null) { // if not exists (it might be set by other processes)
                    final Integer overloadNo = parameterOverloadInfoMap.get(key);
                    if (overloadNo != null) {
                        columnInfo.setOverloadNo(overloadNo);
                    }
                }
            }
        }
    }

    // ===================================================================================
    //                                                                          Great Wall
    //                                                                          ==========
    protected void doSetupGreatWallOracle(StringKeyMap<DfTypeArrayInfo> parameterArrayInfoMap, StringKeyMap<DfTypeStructInfo> structInfoMap,
            List<DfProcedureMeta> metaInfoList, DfProcedureSupplementExtractorOracle extractor) {
        final Set<String> resolvedArrayDispSet = new LinkedHashSet<String>();
        final Set<String> resolvedStructDispSet = new LinkedHashSet<String>();
        for (DfProcedureMeta metaInfo : metaInfoList) {
            final String catalog = metaInfo.getProcedureCatalog();
            final String procedureName = metaInfo.getProcedureName();
            final List<DfProcedureColumnMeta> columnList = metaInfo.getProcedureColumnList();
            for (DfProcedureColumnMeta columnInfo : columnList) {
                final String columnName = columnInfo.getColumnName();
                final String key = extractor.generateParameterInfoMapKey(catalog, procedureName, columnName);

                // Array
                final DfTypeArrayInfo arrayInfo = parameterArrayInfoMap.get(key);
                if (arrayInfo != null) {
                    resolvedArrayDispSet.add(arrayInfo.toString());
                    columnInfo.setTypeArrayInfo(arrayInfo);
                }

                // Struct
                final String dbTypeName = columnInfo.getDbTypeName();
                final DfTypeStructInfo structInfo = structInfoMap.get(dbTypeName);
                if (structInfo != null) {
                    resolvedStructDispSet.add(structInfo.toString());
                    columnInfo.setTypeStructInfo(structInfo);
                }
            }
        }
        if (!resolvedArrayDispSet.isEmpty()) {
            log("Array related to parameter: " + resolvedArrayDispSet.size());
            for (String arrayInfo : resolvedArrayDispSet) {
                log("  " + arrayInfo);
            }
        }
        if (!resolvedStructDispSet.isEmpty()) {
            log("Struct related to parameter: " + resolvedStructDispSet.size());
            for (String structInfo : resolvedStructDispSet) {
                log("  " + structInfo);
            }
        }
    }

    // ===================================================================================
    //                                                                         Source Info
    //                                                                         ===========
    protected void doSetupSourceInfo(DataSource dataSource, List<DfProcedureMeta> metaInfoList, DfProcedureSupplementExtractor extractor,
            UnifiedSchema unifiedSchema, boolean reflectParamsToHash) {
        final Map<String, DfProcedureSourceInfo> sourceInfoMap = extractor.extractProcedureSourceInfo(unifiedSchema);
        if (sourceInfoMap == null) {
            return;
        }
        log("...Reflecting source info to procedure meta: schema=" + unifiedSchema.getCatalogSchema());
        for (DfProcedureMeta procedureMeta : metaInfoList) {
            final UnifiedSchema procedureSchema = procedureMeta.getProcedureSchema();
            if (!unifiedSchema.equals(procedureSchema)) {
                continue;
            }
            final DfProcedureSourceInfo sourceInfo = sourceInfoMap.get(procedureMeta.getProcedureName());
            if (sourceInfo == null) {
                continue;
            }
            if (reflectParamsToHash) {
                sourceInfo.setSupplementCode(procedureMeta.getColumnDefinitionIndentity());
            }
            showReflectedSourceInfo(procedureMeta, sourceInfo);
            procedureMeta.setProcedureSourceInfo(sourceInfo);
        }
    }

    protected void showReflectedSourceInfo(DfProcedureMeta procedureMeta, DfProcedureSourceInfo sourceInfo) {
        final String procedureDisplayName = procedureMeta.getProcedureDisplayName();
        if (getBasicProperties().isSuperDebug()) {
            final StringBuilder sb = new StringBuilder();
            sb.append("[").append(procedureDisplayName).append("]: ").append(sourceInfo);
            sb.append(ln()).append("<source code>");
            sb.append(ln()).append(sourceInfo.getSourceCode());
            final String supplementCode = sourceInfo.getSupplementCode();
            if (supplementCode != null) {
                sb.append(ln()).append("<supplement code>");
                sb.append(ln()).append(supplementCode);
            }
            log(sb.toString());
        } else {
            log("  " + procedureDisplayName + ":" + sourceInfo);
        }
    }

    // ===================================================================================
    //                                                                  (Cached) Extractor
    //                                                                  ==================
    protected DfProcedureSupplementExtractorMySQL getSupplementExtractorMySQL(DfSchemaSource dataSource) {
        return doGetSupplementExtractor(dataSource, new DfProcedureSupplementExtractorCreator() {
            public DfProcedureSupplementExtractor create(DfSchemaSource dataSource) {
                return new DfProcedureSupplementExtractorMySQL(dataSource);
            }
        });
    }

    protected DfProcedureSupplementExtractorPostgreSQL getSupplementExtractorPostgreSQL(DfSchemaSource dataSource) {
        return doGetSupplementExtractor(dataSource, new DfProcedureSupplementExtractorCreator() {
            public DfProcedureSupplementExtractor create(DfSchemaSource dataSource) {
                return new DfProcedureSupplementExtractorPostgreSQL(dataSource);
            }
        });
    }

    protected DfProcedureSupplementExtractorOracle getSupplementExtractorOracle(DfSchemaSource dataSource) {
        return doGetSupplementExtractor(dataSource, new DfProcedureSupplementExtractorCreator() {
            public DfProcedureSupplementExtractor create(DfSchemaSource dataSource) {
                return new DfProcedureSupplementExtractorOracle(dataSource);
            }
        });
    }

    protected DfProcedureSupplementExtractorDB2 getSupplementExtractorDB2(DfSchemaSource dataSource) {
        return doGetSupplementExtractor(dataSource, new DfProcedureSupplementExtractorCreator() {
            public DfProcedureSupplementExtractor create(DfSchemaSource dataSource) {
                return new DfProcedureSupplementExtractorDB2(dataSource);
            }
        });
    }

    protected DfProcedureSupplementExtractorH2 getSupplementExtractorH2(DfSchemaSource dataSource) {
        return doGetSupplementExtractor(dataSource, new DfProcedureSupplementExtractorCreator() {
            public DfProcedureSupplementExtractor create(DfSchemaSource dataSource) {
                return new DfProcedureSupplementExtractorH2(dataSource);
            }
        });
    }

    protected <EXTRACTOR extends DfProcedureSupplementExtractor> EXTRACTOR doGetSupplementExtractor(DfSchemaSource dataSource,
            DfProcedureSupplementExtractorCreator creator) {
        final int dataSourceKey = dataSource.hashCode();
        Map<String, DfProcedureSupplementExtractor> cacheMap = _supplementExtractorMap.get(dataSourceKey);
        if (cacheMap == null) {
            cacheMap = newHashMap();
            _supplementExtractorMap.put(dataSourceKey, cacheMap);
        }
        final String databaseKey = getBasicProperties().getTargetDatabase();
        DfProcedureSupplementExtractor extractor = cacheMap.get(databaseKey);
        if (extractor == null) {
            extractor = creator.create(dataSource);
            cacheMap.put(databaseKey, extractor);
        }
        if (_suppressLogging) {
            extractor.suppressLogging();
        }
        @SuppressWarnings("unchecked")
        final EXTRACTOR castExtractor = (EXTRACTOR) extractor;
        return castExtractor;
    }

    protected static interface DfProcedureSupplementExtractorCreator {
        DfProcedureSupplementExtractor create(DfSchemaSource dataSource);
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfOutsideSqlProperties getOutsideSqlProperties() {
        return getProperties().getOutsideSqlProperties();
    }

    // ===================================================================================
    //                                                                             Logging
    //                                                                             =======
    protected void log(String msg) {
        if (_suppressLogging) {
            return;
        }
        _log.info(msg);
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected <ELEMENT> ArrayList<ELEMENT> newArrayList() {
        return DfCollectionUtil.newArrayList();
    }

    protected <KEY, VALUE> HashMap<KEY, VALUE> newHashMap() {
        return DfCollectionUtil.newHashMap();
    }

    // ===================================================================================
    //                                                                              Option
    //                                                                              ======
    public void suppressAdditionalSchema() {
        _suppressAdditionalSchema = true;
    }

    public void suppressFilterByProperty() {
        _suppressFilterByProperty = true;
    }

    public void suppressGenerationRestriction() {
        _suppressGenerationRestriction = true;
    }

    public void suppressLogging() {
        _suppressLogging = true;
    }

    public void includeProcedureSynonym(DfSchemaSource dataSource) {
        _procedureSynonymDataSource = dataSource;
    }

    public void includeProcedureToDBLink(DfSchemaSource dataSource) {
        _procedureToDBLinkDataSource = dataSource;
    }
}