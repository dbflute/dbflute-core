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
package org.seasar.dbflute.task.bs;

import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.Task;
import org.apache.torque.engine.database.model.UnifiedSchema;
import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.helper.filesystem.FileURL;
import org.seasar.dbflute.helper.jdbc.connection.DfConnectionMetaInfo;
import org.seasar.dbflute.helper.jdbc.connection.DfDataSourceHandler;
import org.seasar.dbflute.helper.jdbc.context.DfSchemaSource;
import org.seasar.dbflute.infra.dfprop.DfPropPublicMap;
import org.seasar.dbflute.logic.DfDBFluteTaskUtil;
import org.seasar.dbflute.logic.sql2entity.analyzer.DfOutsideSqlPack;
import org.seasar.dbflute.properties.DfBasicProperties;
import org.seasar.dbflute.properties.DfDatabaseProperties;
import org.seasar.dbflute.properties.DfInfraProperties;
import org.seasar.dbflute.properties.DfLittleAdjustmentProperties;
import org.seasar.dbflute.properties.DfRefreshProperties;
import org.seasar.dbflute.properties.facade.DfDatabaseTypeFacadeProp;
import org.seasar.dbflute.task.bs.assistant.DfTaskBasicController;
import org.seasar.dbflute.task.bs.assistant.DfTaskControlCallback;
import org.seasar.dbflute.task.bs.assistant.DfTaskControlLogic;
import org.seasar.dbflute.task.bs.assistant.DfTaskDatabaseResource;
import org.seasar.dbflute.util.Srl;

/**
 * The abstract task.
 * @author jflute
 */
public abstract class DfAbstractTask extends Task {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance. */
    private static final Log _log = LogFactory.getLog(DfAbstractTask.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The resource of database info for the task. (NotNull) */
    protected final DfTaskDatabaseResource _databaseResource = new DfTaskDatabaseResource();

    /** The basic controller of task process. (NotNull) */
    protected final DfTaskBasicController _controller = createBasicTaskController(_databaseResource);

    /** The logic of task control. (NotNull) */
    protected final DfTaskControlLogic _controlLogic = createTaskControlLogic(_databaseResource);

    // ===================================================================================
    //                                                                     Task Controller
    //                                                                     ===============
    protected DfTaskBasicController createBasicTaskController(DfTaskDatabaseResource databaseResource) {
        return new DfTaskBasicController(createTaskControlCallback(), databaseResource);
    }

    protected DfTaskControlCallback createTaskControlCallback() {
        return new DfTaskControlCallback() {

            public boolean callBegin() {
                return begin();
            }

            public void callInitializeDatabaseInfo() {
                initializeDatabaseInfo();
            }

            public void callInitializeVariousEnvironment() {
                initializeVariousEnvironment();
            }

            public boolean callUseDataSource() {
                return isUseDataSource();
            }

            public void callSetupDataSource() throws SQLException {
                setupDataSource();
            }

            public void callCommitDataSource() throws SQLException {
                commitDataSource();
            }

            public void callDestroyDataSource() throws SQLException {
                destroyDataSource();
            }

            public DfConnectionMetaInfo callGetConnectionMetaInfo() {
                return getConnectionMetaInfo();
            }

            public void callActualExecute() {
                doExecute();
            }

            public void callShowFinalMessage(long before, long after, boolean abort) {
                showFinalMessage(before, after, abort);
            }

            public String callGetTaskName() {
                return getTaskName();
            }
        };
    }

    protected DfTaskControlLogic createTaskControlLogic(DfTaskDatabaseResource databaseResource) {
        return new DfTaskControlLogic(databaseResource);
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    @Override
    public final void execute() { // completely override
        _controller.execute();
    }

    // ===================================================================================
    //                                                                   Prepare Execution
    //                                                                   =================
    protected abstract boolean begin();

    protected void initializeDatabaseInfo() {
        _controlLogic.initializeDatabaseInfo();
    }

    protected void initializeVariousEnvironment() {
        _controlLogic.initializeVariousEnvironment();
    }

    // ===================================================================================
    //                                                                         Data Source
    //                                                                         ===========
    protected abstract boolean isUseDataSource();

    protected void setupDataSource() throws SQLException {
        _controlLogic.setupDataSource();
    }

    protected void commitDataSource() throws SQLException {
        _controlLogic.commitDataSource();
    }

    protected void destroyDataSource() throws SQLException {
        _controlLogic.destroyDataSource();
    }

    /**
     * @return The data source with schema. (NullAllowed: when data source does not exist on thread, e.g. lazy connection)
     */
    protected DfSchemaSource getDataSource() {
        return _controlLogic.getDataSource();
    }

    protected DfConnectionMetaInfo getConnectionMetaInfo() {
        return _controlLogic.getConnectionMetaInfo();
    }

    // ===================================================================================
    //                                                                    Actual Execution
    //                                                                    ================
    protected abstract void doExecute();

    // ===================================================================================
    //                                                                       Final Message
    //                                                                       =============
    protected void showFinalMessage(long before, long after, boolean abort) {
        _controlLogic.showFinalMessage(before, after, abort, getTaskName(), getFinalInformation());
    }

    protected String getFinalInformation() {
        return null; // as default
    }

    // ===================================================================================
    //                                                                 SQL File Collecting
    //                                                                 ===================
    /**
     * Collect outside-SQL containing its file info as pack with directory check.
     * @return The pack object for outside-SQL files. (NotNull)
     */
    protected DfOutsideSqlPack collectOutsideSqlChecked() {
        return _controlLogic.collectOutsideSqlChecked();
    }

    // ===================================================================================
    //                                                                    Refresh Resource
    //                                                                    ================
    /**
     * Refresh resources of Eclipse projects.
     */
    protected void refreshResources() {
        _controlLogic.refreshResources();
    }

    // ===================================================================================
    //                                                                  Context Properties
    //                                                                  ==================
    public void setContextProperties(String file) { // called by ANT
        try {
            final Properties prop = DfDBFluteTaskUtil.getBuildProperties(file, getProject());
            DfBuildProperties.getInstance().setProperties(prop);
        } catch (RuntimeException e) {
            String msg = "Failed to set context properties:";
            msg = msg + " file=" + file;
            _log.warn(msg, e); // logging because it throws to ANT world
            throw e;
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
    //                                                                  Environment Helper
    //                                                                  ==================
    protected String getDBFluteHome() {
        final Map<String, String> envMap = new ProcessBuilder().environment();
        return envMap != null ? envMap.get("DBFLUTE_HOME") : null; // null check just in case
    }

    protected String getMyDBFluteDir() {
        final String dbfluteHome = getDBFluteHome(); // e.g. ../mydbflute/dbflute-1.0.5K
        if (dbfluteHome == null) { // basically no way (just in case)
            return null;
        }
        final String filtered = Srl.replace(dbfluteHome, "\\", "/");
        if (!filtered.contains("/")) { // basically no way (just in case)
            return null;
        }
        return Srl.substringLastFront(filtered, "/"); // e.g. ../mydbflute
    }

    protected DfPropPublicMap preparePublicMap() {
        final DfInfraProperties prop = getInfraProperties();
        final String publicMapUrl = prop.getPublicMapUrl();
        final DfPropPublicMap dfprop = new DfPropPublicMap().specifyUrl(publicMapUrl);
        dfprop.loadMap();
        return dfprop;
    }

    protected void download(String downloadUrl, String locationPath) {
        new FileURL(downloadUrl).download(locationPath);
    }

    protected DfInfraProperties getInfraProperties() {
        return DfBuildProperties.getInstance().getInfraProperties();
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String ln() {
        return "\n";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    protected String getDriver() {
        return _databaseResource.getDriver();
    }

    protected String getUrl() {
        return _databaseResource.getUrl();
    }

    protected UnifiedSchema getMainSchema() {
        return _databaseResource.getMainSchema();
    }

    protected String getUser() {
        return _databaseResource.getUser();
    }

    protected String getPassword() {
        return _databaseResource.getPassword();
    }

    protected DfDataSourceHandler getDataSourceHandler() {
        return _databaseResource.getDataSourceHandler();
    }

    public void setEnvironmentType(String environmentType) {
        _controlLogic.acceptEnvironmentType(environmentType);
    }
}