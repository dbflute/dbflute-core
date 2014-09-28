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

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.exception.DfDBFluteTaskCancelledException;
import org.seasar.dbflute.helper.jdbc.connection.DfConnectionMetaInfo;
import org.seasar.dbflute.logic.DfDBFluteTaskUtil;
import org.seasar.dbflute.properties.DfBasicProperties;
import org.seasar.dbflute.properties.DfDatabaseProperties;
import org.seasar.dbflute.properties.facade.DfDatabaseTypeFacadeProp;
import org.seasar.dbflute.resource.DBFluteSystem;
import org.seasar.dbflute.task.bs.DfAbstractTask;

/**
 * @author jflute
 */
public class DfTaskBasicController {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance. */
    private static final Log _log = LogFactory.getLog(DfAbstractTask.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The call-back of task control. (NotNull) */
    protected final DfTaskControlCallback _controlCallback;

    /** The resource of database info for the task. (NotNull) */
    protected final DfTaskDatabaseResource _databaseResource;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfTaskBasicController(DfTaskControlCallback controlCallback, DfTaskDatabaseResource databaseResource) {
        _controlCallback = controlCallback;
        _databaseResource = databaseResource;
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    public final void execute() {
        Throwable cause = null;
        long before = getTaskBeforeTimeMillis();
        try {
            final boolean letsGo = begin();
            if (!letsGo) {
                cause = createTaskCancelledException();
                return;
            }
            initializeDatabaseInfo();
            if (isUseDataSource()) {
                setupDataSource();
            }
            initializeVariousEnvironment();
            doExecute();
        } catch (Exception e) {
            cause = e;
            try {
                logException(e);
            } catch (Throwable ignored) {
                _log.warn("*Ignored exception occured!", ignored);
                _log.error("*Failed to execute DBFlute Task!", e);
            }
        } catch (Error e) {
            cause = e;
            try {
                logError(e);
            } catch (Throwable ignored) {
                _log.warn("*Ignored exception occured!", ignored);
                _log.error("*Failed to execute DBFlute Task!", e);
            }
        } finally {
            if (isUseDataSource()) {
                try {
                    commitDataSource();
                } catch (SQLException ignored) {
                } finally {
                    try {
                        destroyDataSource();
                    } catch (Exception ignored) {
                        _log.warn("*Failed to destroy data source: " + ignored.getMessage());
                    }
                }
            }
            if (isValidTaskEndInformation() || cause != null) {
                try {
                    long after = getTaskAfterTimeMillis();
                    showFinalMessage(before, after, cause != null);
                } catch (RuntimeException e) {
                    _log.warn("*Failed to show final message!", e);
                }
            }
            if (cause != null) {
                if (cause instanceof DfDBFluteTaskCancelledException) {
                    throw (DfDBFluteTaskCancelledException) cause;
                } else {
                    throwTaskFailure();
                }
            }
        }
    }

    protected boolean begin() {
        return _controlCallback.callBegin();
    }

    protected void initializeDatabaseInfo() {
        _controlCallback.callInitializeDatabaseInfo();
    }

    protected void initializeVariousEnvironment() {
        _controlCallback.callInitializeVariousEnvironment();
    }

    protected long getTaskBeforeTimeMillis() {
        return DBFluteSystem.currentTimeMillis();
    }

    protected long getTaskAfterTimeMillis() {
        return DBFluteSystem.currentTimeMillis();
    }

    protected void logException(Exception e) {
        DfDBFluteTaskUtil.logException(e, getDisplayTaskName(), getConnectionMetaInfo());
    }

    protected void logError(Error e) {
        DfDBFluteTaskUtil.logError(e, getDisplayTaskName(), getConnectionMetaInfo());
    }

    protected boolean isValidTaskEndInformation() {
        return true;
    }

    // ===================================================================================
    //                                                                         Data Source
    //                                                                         ===========
    protected boolean isUseDataSource() {
        return _controlCallback.callUseDataSource();
    }

    protected void setupDataSource() throws SQLException {
        _controlCallback.callSetupDataSource();
    }

    protected void commitDataSource() throws SQLException {
        _controlCallback.callCommitDataSource();
    }

    protected void destroyDataSource() throws SQLException {
        _controlCallback.callDestroyDataSource();
    }

    protected DfConnectionMetaInfo getConnectionMetaInfo() {
        return _controlCallback.callGetConnectionMetaInfo();
    }

    // ===================================================================================
    //                                                                    Actual Execution
    //                                                                    ================
    protected void doExecute() {
        _controlCallback.callActualExecute();
    }

    // ===================================================================================
    //                                                                       Final Message
    //                                                                       =============
    protected void showFinalMessage(long before, long after, boolean abort) {
        _controlCallback.callShowFinalMessage(before, after, abort);
    }

    // ===================================================================================
    //                                                                    Failure Handling
    //                                                                    ================
    protected DfDBFluteTaskCancelledException createTaskCancelledException() {
        return DfDBFluteTaskUtil.createTaskCancelledException(getDisplayTaskName());
    }

    protected void throwTaskFailure() {
        DfDBFluteTaskUtil.throwTaskFailure(getDisplayTaskName());
    }

    protected String getDisplayTaskName() {
        final String taskName = _controlCallback.callGetTaskName();
        return DfDBFluteTaskUtil.getDisplayTaskName(taskName);
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

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public DfTaskDatabaseResource getDatabaseResource() {
        return _databaseResource;
    }
}
