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
package org.seasar.dbflute.task.bs.assistant;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.engine.database.model.UnifiedSchema;
import org.seasar.dbflute.DBDef;
import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.helper.jdbc.connection.DfConnectionCreationHook;
import org.seasar.dbflute.helper.jdbc.connection.DfConnectionMetaInfo;
import org.seasar.dbflute.helper.jdbc.connection.DfDataSourceHandler;
import org.seasar.dbflute.helper.jdbc.context.DfDataSourceContext;
import org.seasar.dbflute.helper.jdbc.context.DfSchemaSource;
import org.seasar.dbflute.infra.core.DfEnvironmentType;
import org.seasar.dbflute.logic.DfDBFluteTaskUtil;
import org.seasar.dbflute.logic.generate.refresh.DfRefreshResourceProcess;
import org.seasar.dbflute.logic.jdbc.connection.DfCurrentSchemaConnector;
import org.seasar.dbflute.logic.sql2entity.analyzer.DfOutsideSqlCollector;
import org.seasar.dbflute.logic.sql2entity.analyzer.DfOutsideSqlPack;
import org.seasar.dbflute.properties.DfBasicProperties;
import org.seasar.dbflute.properties.DfDatabaseProperties;
import org.seasar.dbflute.properties.DfLittleAdjustmentProperties;
import org.seasar.dbflute.properties.DfRefreshProperties;
import org.seasar.dbflute.properties.DfReplaceSchemaProperties;
import org.seasar.dbflute.properties.facade.DfDatabaseTypeFacadeProp;
import org.seasar.dbflute.resource.ResourceContext;
import org.seasar.dbflute.s2dao.valuetype.TnValueTypes;
import org.seasar.dbflute.util.DfTraceViewUtil;

/**
 * @author jflute
 */
public class DfTaskControlLogic {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance. */
    private static final Log _log = LogFactory.getLog(DfTaskControlLogic.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfTaskDatabaseResource _databaseResource;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfTaskControlLogic(DfTaskDatabaseResource databaseResource) {
        _databaseResource = databaseResource;
    }

    // ===================================================================================
    //                                                                   Prepare Execution
    //                                                                   =================
    public void initializeDatabaseInfo() {
        _databaseResource.setDriver(getDatabaseProperties().getDatabaseDriver());
        _databaseResource.setUrl(getDatabaseProperties().getDatabaseUrl());
        _databaseResource.setUser(getDatabaseProperties().getDatabaseUser());
        _databaseResource.setMainSchema(getDatabaseProperties().getDatabaseSchema());
        _databaseResource.setPassword(getDatabaseProperties().getDatabasePassword());
        _databaseResource.setConnectionProperties(getDatabaseProperties().getConnectionProperties());

        final ResourceContext context = new ResourceContext();
        context.setCurrentDBDef(getBasicProperties().getCurrentDBDef());
        ResourceContext.setResourceContextOnThread(context); // no need to clear because of one thread
    }

    public void initializeVariousEnvironment() {
        if (getDatabaseTypeFacadeProp().isDatabaseOracle()) {
            // basically for data loading of ReplaceSchema
            final DBDef currentDBDef = ResourceContext.currentDBDef();
            TnValueTypes.registerBasicValueType(currentDBDef, java.util.Date.class, TnValueTypes.UTILDATE_AS_TIMESTAMP);
        }
    }

    // ===================================================================================
    //                                                                         Data Source
    //                                                                         ===========
    public void setupDataSource() throws SQLException {
        final DfDataSourceHandler dataSourceHandler = _databaseResource.getDataSourceHandler();
        dataSourceHandler.setUser(_databaseResource.getUser());
        dataSourceHandler.setPassword(_databaseResource.getPassword());
        dataSourceHandler.setDriver(_databaseResource.getDriver());
        dataSourceHandler.setUrl(_databaseResource.getUrl());
        dataSourceHandler.setConnectionProperties(_databaseResource.getConnectionProperties());
        dataSourceHandler.setAutoCommit(true);
        dataSourceHandler.addConnectionCreationHook(new DfConnectionCreationHook() {
            public void hook(Connection conn) throws SQLException {
                connectMainSchema(conn);
            }
        });
        dataSourceHandler.prepare();
    }

    protected void connectMainSchema(Connection conn) throws SQLException {
        final UnifiedSchema mainSchema = _databaseResource.getMainSchema();
        final DfCurrentSchemaConnector connector = new DfCurrentSchemaConnector(mainSchema, getDatabaseTypeFacadeProp());
        connector.connectSchema(conn);
    }

    public void commitDataSource() throws SQLException {
        final DfDataSourceHandler dataSourceHandler = _databaseResource.getDataSourceHandler();
        dataSourceHandler.commit();
    }

    public void destroyDataSource() throws SQLException {
        final DfDataSourceHandler dataSourceHandler = _databaseResource.getDataSourceHandler();
        dataSourceHandler.destroy();

        if (getDatabaseTypeFacadeProp().isDatabaseDerby()) {
            // Derby(Embedded) needs an original shutdown for destroying a connection
            DfDBFluteTaskUtil.shutdownIfDerbyEmbedded(_databaseResource.getDriver());
        }
    }

    /**
     * Get data source for main connection. <br />
     * It returns valid data source after setupDataSource() success. <br />
     * Basically not null but when data source does not exist on thread, it returns null.
     * @return The data source with schema. (NullAllowed: when data source does not exist on thread, e.g. lazy connection)
     */
    public DfSchemaSource getDataSource() {
        return getSchemaSource();
    }

    /**
     * Get schema source for main connection. <br />
     * It returns valid data source after setupDataSource() success. <br />
     * Basically not null but when data source does not exist on thread, it returns null.
     * @return The schema source. (NullAllowed: when data source does not exist on thread, e.g. lazy connection)
     */
    protected DfSchemaSource getSchemaSource() {
        final DataSource dataSource = DfDataSourceContext.getDataSource();
        if (dataSource == null) {
            return null; // basically when lazy connection
        }
        final UnifiedSchema mainSchema = _databaseResource.getMainSchema();
        return new DfSchemaSource(dataSource, mainSchema);
    }

    public DfConnectionMetaInfo getConnectionMetaInfo() {
        final DfDataSourceHandler dataSourceHandler = _databaseResource.getDataSourceHandler();
        return dataSourceHandler.getConnectionMetaInfo();
    }

    // ===================================================================================
    //                                                                       Final Message
    //                                                                       =============
    public void showFinalMessage(long before, long after, boolean abort, String taskName, String finalInformation) {
        final String displayTaskName = getDisplayTaskName(taskName);
        final String envType = DfEnvironmentType.getInstance().getEnvironmentType();
        final StringBuilder sb = new StringBuilder();
        final String ln = ln();
        sb.append(ln).append("_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/");
        sb.append(ln).append("[Final Message]: ").append(getPerformanceView(after - before));
        if (abort) {
            sb.append(" *Abort");
        }
        sb.append(ln);

        final DfConnectionMetaInfo metaInfo = getConnectionMetaInfo();
        final String productDisp = metaInfo != null ? " (" + metaInfo.getProductDisp() + ")" : "";
        final String databaseType = getDatabaseTypeFacadeProp().getTargetDatabase() + productDisp;
        sb.append(ln).append("  DBFLUTE_CLIENT: {").append(getBasicProperties().getProjectName()).append("}");
        sb.append(ln).append("    database  = " + databaseType);
        sb.append(ln).append("    language  = " + getBasicProperties().getTargetLanguage());
        sb.append(ln).append("    container = " + getBasicProperties().getTargetContainerName());
        sb.append(ln).append("    package   = " + getBasicProperties().getPackageBase());
        sb.append(ln);
        sb.append(ln).append("  DBFLUTE_ENVIRONMENT_TYPE: {").append(envType != null ? envType : "").append("}");
        final String driver = _databaseResource.getDriver();
        if (driver != null) { // basically true except cancelled
            sb.append(ln).append("    driver = ").append(driver);
            sb.append(ln).append("    url    = ").append(_databaseResource.getUrl());
            sb.append(ln).append("    schema = ").append(_databaseResource.getMainSchema());
            sb.append(ln).append("    user   = ").append(_databaseResource.getUser());
            sb.append(ln).append("    props  = ").append(_databaseResource.getConnectionProperties());
        }

        final String additionalSchemaDisp = buildAdditionalSchemaDisp();
        sb.append(ln).append("    additionalSchema = ").append(additionalSchemaDisp);
        final DfReplaceSchemaProperties replaceSchemaProp = getProperties().getReplaceSchemaProperties();
        sb.append(ln).append("    repsEnvType      = ").append(replaceSchemaProp.getRepsEnvType());
        final String refreshProjectDisp = buildRefreshProjectDisp();
        sb.append(ln).append("    refreshProject   = ").append(refreshProjectDisp);

        if (finalInformation != null) {
            sb.append(ln).append(ln);
            sb.append(finalInformation);
        }
        sb.append(ln).append("_/_/_/_/_/_/_/_/_/_/ {").append(displayTaskName).append("}");
        DfDBFluteTaskUtil.logFinalMessage(sb.toString());
    }

    protected String buildAdditionalSchemaDisp() {
        final DfDatabaseProperties databaseProp = getDatabaseProperties();
        final List<UnifiedSchema> additionalSchemaList = databaseProp.getAdditionalSchemaList();
        String disp;
        if (additionalSchemaList.size() == 1) {
            final UnifiedSchema unifiedSchema = additionalSchemaList.get(0);
            final String identifiedSchema = unifiedSchema.getIdentifiedSchema();
            disp = identifiedSchema;
            if (unifiedSchema.isCatalogAdditionalSchema()) {
                disp = disp + "(catalog)";
            } else if (unifiedSchema.isMainSchema()) { // should NOT be true
                disp = disp + "(main)";
            } else if (unifiedSchema.isUnknownSchema()) { // should NOT be true
                disp = disp + "(unknown)";
            }
        } else {
            final StringBuilder sb = new StringBuilder();
            for (UnifiedSchema unifiedSchema : additionalSchemaList) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                final String identifiedSchema = unifiedSchema.getIdentifiedSchema();
                sb.append(identifiedSchema);
                if (unifiedSchema.isCatalogAdditionalSchema()) {
                    sb.append("(catalog)");
                } else if (unifiedSchema.isMainSchema()) { // should NOT be true
                    sb.append("(main)");
                } else if (unifiedSchema.isUnknownSchema()) { // should NOT be true
                    sb.append("(unknown)");
                }
            }
            disp = sb.toString();
        }
        return disp;
    }

    protected String buildRefreshProjectDisp() {
        final DfRefreshProperties refreshProp = getProperties().getRefreshProperties();
        if (!refreshProp.hasRefreshDefinition()) {
            return "";
        }
        final List<String> refreshProjectList = refreshProp.getProjectNameList();
        final String disp;
        if (refreshProjectList.size() == 1) {
            disp = refreshProjectList.get(0);
        } else {
            final StringBuilder sb = new StringBuilder();
            for (String refreshProject : refreshProjectList) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(refreshProject);
            }
            disp = sb.toString();
        }
        return disp;
    }

    protected String getDisplayTaskName(String taskName) {
        return DfDBFluteTaskUtil.getDisplayTaskName(taskName);
    }

    protected String getPerformanceView(long mil) {
        return DfTraceViewUtil.convertToPerformanceView(mil);
    }

    // ===================================================================================
    //                                                                 SQL File Collecting
    //                                                                 ===================
    /**
     * Collect outside-SQL containing its file info as pack with directory check.
     * @return The pack object for outside-SQL files. (NotNull)
     */
    public DfOutsideSqlPack collectOutsideSqlChecked() {
        final DfOutsideSqlCollector sqlFileCollector = new DfOutsideSqlCollector();
        return sqlFileCollector.collectOutsideSql(); // not suppress check
    }

    // ===================================================================================
    //                                                                    Refresh Resource
    //                                                                    ================
    public void refreshResources() {
        final DfRefreshProperties prop = getRefreshProperties();
        final List<String> projectNameList = prop.getProjectNameList();
        final String requestUrl = prop.getRequestUrl();
        new DfRefreshResourceProcess(projectNameList, requestUrl).refreshResources();
    }

    // ===================================================================================
    //                                                                    Environment Type
    //                                                                    ================
    public void acceptEnvironmentType(String environmentType) {
        if (environmentType == null || environmentType.trim().length() == 0) {
            return;
        }
        if (environmentType.startsWith("${dfenv}")) {
            return;
        }
        DfEnvironmentType.getInstance().setEnvironmentType(environmentType);
        if (DfEnvironmentType.getInstance().isSpecifiedType()) {
            _log.info("...Setting environmentType '" + environmentType + "'");
        }
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    protected DfBasicProperties getBasicProperties() {
        return getProperties().getBasicProperties();
    }

    protected DfDatabaseTypeFacadeProp getDatabaseTypeFacadeProp() {
        return getBasicProperties().getDatabaseTypeFacadeProp();
    }

    protected DfDatabaseProperties getDatabaseProperties() {
        return getProperties().getDatabaseProperties();
    }

    protected DfLittleAdjustmentProperties getLittleAdjustmentProperties() {
        return getProperties().getLittleAdjustmentProperties();
    }

    protected DfRefreshProperties getRefreshProperties() {
        return getProperties().getRefreshProperties();
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String ln() {
        return "\n";
    }
}
