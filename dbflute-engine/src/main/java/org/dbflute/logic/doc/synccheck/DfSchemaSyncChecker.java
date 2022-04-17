/*
 * Copyright 2014-2022 the original author or authors.
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
package org.dbflute.logic.doc.synccheck;

import java.io.File;
import java.util.List;

import javax.sql.DataSource;

import org.apache.torque.engine.database.model.UnifiedSchema;
import org.dbflute.DfBuildProperties;
import org.dbflute.exception.DfSchemaSyncCheckGhastlyTragedyException;
import org.dbflute.helper.jdbc.connection.DfDataSourceHandler;
import org.dbflute.helper.jdbc.connection.DfFittingDataSource;
import org.dbflute.helper.jdbc.context.DfSchemaSource;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.logic.doc.craftdiff.DfCraftDiffAssertDirection;
import org.dbflute.logic.jdbc.schemadiff.DfSchemaDiff;
import org.dbflute.logic.jdbc.schemaxml.DfSchemaXmlSerializer;
import org.dbflute.properties.DfDatabaseProperties;
import org.dbflute.properties.DfDocumentProperties;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 0.9.8.4 (2011/05/29 Sunday)
 */
public class DfSchemaSyncChecker {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The logger instance for this class. (NotNull) */
    private static final Logger _log = LoggerFactory.getLogger(DfSchemaSyncChecker.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfSchemaSource _mainSource;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfSchemaSyncChecker(DfSchemaSource mainSource) {
        _mainSource = mainSource;
    }

    // ===================================================================================
    //                                                                          Check Sync
    //                                                                          ==========
    public void checkSync() {
        clearOutputResource();
        final DfSchemaXmlSerializer serializer = diffSchema();

        _log.info("");
        _log.info("* * * * * * * * * * * * * * * * *");
        _log.info("*                               *");
        _log.info("*   Check Schema Synchronized   *");
        _log.info("*                               *");
        _log.info("* * * * * * * * * * * * * * * * *");
        final DfSchemaDiff schemaDiff = serializer.getSchemaDiff();
        if (schemaDiff.hasDiff()) {
            _log.info(" -> the schema has differences");
            throwSchemaSyncCheckTragedyResultException();
        } else { // synchronized
            _log.info(" -> the schema is synchronized");
            clearOutputResource();
        }
    }

    protected void clearOutputResource() {
        final File schemaXml = new File(getSchemaXml());
        if (schemaXml.exists()) {
            schemaXml.delete();
        }
        final File diffMapFile = new File(getDiffMapFile());
        if (diffMapFile.exists()) {
            diffMapFile.delete();
        }
        final File resultFile = new File(getResultFilePath());
        if (resultFile.exists()) {
            resultFile.delete();
        }
        final String craftMetaDir = getSchemaSyncCheckCraftMetaDir();
        if (craftMetaDir != null) {
            final List<File> metaFileList = getCraftMetaFileList(craftMetaDir);
            for (File metaFile : metaFileList) {
                metaFile.delete();
            }
        }
    }

    protected DfSchemaXmlSerializer diffSchema() {
        _log.info("");
        _log.info("* * * * * * * * * * * * * * * * *");
        _log.info("*                               *");
        _log.info("*    Target Schema (previous)   *");
        _log.info("*                               *");
        _log.info("* * * * * * * * * * * * * * * * *");
        serializeTargetSchema(); // should be before main schema, making only schemaXml

        _log.info("");
        _log.info("* * * * * * * * * * * * * * * * *");
        _log.info("*                               *");
        _log.info("*       Main Schema (next)      *");
        _log.info("*                               *");
        _log.info("* * * * * * * * * * * * * * * * *");
        return serializeMainSchema(); // with diff process
    }

    protected DfSchemaXmlSerializer serializeTargetSchema() { // as previous
        final DataSource targetDs = prepareTargetDataSource();
        final DfSchemaXmlSerializer targetSerializer = createTargetSerializer(targetDs);
        targetSerializer.suppressSchemaDiff(); // same reason as main schema
        targetSerializer.serialize();
        return targetSerializer;
    }

    protected DfSchemaXmlSerializer serializeMainSchema() { // as next
        final DfSchemaXmlSerializer mainSerializer = createMainSerializer();
        mainSerializer.suppressSchemaDiff(); // because of comparison with other schema
        mainSerializer.serialize();
        return mainSerializer;
    }

    protected DataSource prepareTargetDataSource() {
        final DfDataSourceHandler handler = new DfDataSourceHandler();
        handler.setDriver(getDatabaseProperties().getDatabaseDriver()); // inherit
        final String url = getDocumentProperties().getSchemaSyncCheckDatabaseUrl();
        handler.setUrl(url); // may inherit
        final String user = getDocumentProperties().getSchemaSyncCheckDatabaseUser();
        if (Srl.is_Null_or_TrimmedEmpty(user)) { // just in case
            String msg = "The user for sync target schema was not found: " + user;
            throw new IllegalStateException(msg);
        }
        handler.setUser(user);
        handler.setPassword(getDocumentProperties().getSchemaSyncCheckDatabasePassword());
        handler.setConnectionProperties(getDatabaseProperties().getConnectionProperties()); // inherit
        handler.setAutoCommit(true);
        _log.info("...Preparing data source for SchemaSyncCheck target:");
        _log.info("  url  = " + url);
        _log.info("  user = " + user);
        return new DfFittingDataSource(handler);
    }

    protected void throwSchemaSyncCheckTragedyResultException() {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The schema was not synchronized with another schema.");
        br.addItem("Advice");
        br.addElement("You can see the details at");
        br.addElement(" '" + getResultFilePath() + "'.");
        br.addElement("");
        br.addElement("'Previous' means the sync-check schema, defined at schemaSyncCheckMap property.");
        br.addElement("'Next' means the main schema, defined at databaseInfoMap.dfprop.");
        br.addElement("");
        br.addElement("e.g. Add Table: FOO_TABLE");
        br.addElement("create the table on the sync-check schema to synchronize with main schema.");
        final String msg = br.buildExceptionMessage();
        throw new DfSchemaSyncCheckGhastlyTragedyException(msg);
    }

    // ===================================================================================
    //                                                                          Serializer
    //                                                                          ==========
    protected DfSchemaXmlSerializer createTargetSerializer(DataSource targetDs) { // as previous
        final UnifiedSchema targetSchema = getDocumentProperties().getSchemaSyncCheckDatabaseSchema();
        _log.info("schema: " + targetSchema);
        final String historyFile = /*historyFile*/null; // to make only schemaXml as target (previous)
        return doCreateSerializer(new DfSchemaSource(targetDs, targetSchema), historyFile);
    }

    protected DfSchemaXmlSerializer createMainSerializer() { // as next
        _log.info("schema: " + _mainSource.getSchema());
        final String historyFile = getDiffMapFile(); // needs to make for comparing with target (previous)
        return doCreateSerializer(_mainSource, historyFile);
    }

    protected DfSchemaXmlSerializer doCreateSerializer(DfSchemaSource dataSource, String historyFile) {
        final String schemaXml = getSchemaXml();
        final DfSchemaXmlSerializer serializer = DfSchemaXmlSerializer.createAsManage(dataSource, schemaXml, historyFile);
        final String craftMetaDir = getSchemaSyncCheckCraftMetaDir();
        if (!getDocumentProperties().isSchemaSyncCheckSuppressCraftDiff()) {
            serializer.enableCraftDiff(dataSource, craftMetaDir, DfCraftDiffAssertDirection.ROLLING_NEXT);
        }
        return serializer;
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    protected DfDatabaseProperties getDatabaseProperties() {
        return getProperties().getDatabaseProperties();
    }

    protected DfDocumentProperties getDocumentProperties() {
        return getProperties().getDocumentProperties();
    }

    protected String getSchemaXml() {
        return getDocumentProperties().getSchemaSyncCheckSchemaXml();
    }

    protected String getDiffMapFile() {
        return getDocumentProperties().getSchemaSyncCheckDiffMapFile();
    }

    protected String getResultFilePath() {
        return getDocumentProperties().getSchemaSyncCheckResultFilePath();
    }

    protected String getSchemaSyncCheckCraftMetaDir() {
        return getDocumentProperties().getSchemaSyncCheckCraftMetaDir();
    }

    protected List<File> getCraftMetaFileList(String craftMetaDir) {
        return getDocumentProperties().getCraftMetaFileList(craftMetaDir);
    }
}
