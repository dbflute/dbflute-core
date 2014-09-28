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

import org.seasar.dbflute.helper.jdbc.connection.DfConnectionMetaInfo;

/**
 * @author jflute
 */
public interface DfTaskControlCallback {

    // ===================================================================================
    //                                                                   Prepare Execution
    //                                                                   =================
    boolean callBegin();

    void callInitializeDatabaseInfo();

    void callInitializeVariousEnvironment();

    // ===================================================================================
    //                                                                         Data Source
    //                                                                         ===========
    boolean callUseDataSource();

    void callSetupDataSource() throws SQLException;

    void callCommitDataSource() throws SQLException;

    void callDestroyDataSource() throws SQLException;

    DfConnectionMetaInfo callGetConnectionMetaInfo();

    // ===================================================================================
    //                                                                      Actual Execute
    //                                                                      ==============
    void callActualExecute();

    // ===================================================================================
    //                                                                       Final Message
    //                                                                       =============
    void callShowFinalMessage(long before, long after, boolean abort);

    String callGetTaskName();
}
