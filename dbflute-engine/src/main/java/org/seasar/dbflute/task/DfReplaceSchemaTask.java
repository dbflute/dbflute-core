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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.engine.database.model.AppData;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.seasar.dbflute.exception.DfCreateSchemaFailureException;
import org.seasar.dbflute.exception.DfTakeFinallyAssertionFailureException;
import org.seasar.dbflute.exception.DfTakeFinallyFailureException;
import org.seasar.dbflute.exception.SQLFailureException;
import org.seasar.dbflute.friends.velocity.DfVelocityContextFactory;
import org.seasar.dbflute.logic.replaceschema.finalinfo.DfAbstractSchemaTaskFinalInfo;
import org.seasar.dbflute.logic.replaceschema.finalinfo.DfAlterCheckFinalInfo;
import org.seasar.dbflute.logic.replaceschema.finalinfo.DfCreateSchemaFinalInfo;
import org.seasar.dbflute.logic.replaceschema.finalinfo.DfLoadDataFinalInfo;
import org.seasar.dbflute.logic.replaceschema.finalinfo.DfReplaceSchemaFinalInfo;
import org.seasar.dbflute.logic.replaceschema.finalinfo.DfTakeFinallyFinalInfo;
import org.seasar.dbflute.logic.replaceschema.process.DfAlterCheckProcess;
import org.seasar.dbflute.logic.replaceschema.process.DfAlterCheckProcess.CoreProcessPlayer;
import org.seasar.dbflute.logic.replaceschema.process.DfArrangeBeforeRepsProcess;
import org.seasar.dbflute.logic.replaceschema.process.DfCreateSchemaProcess;
import org.seasar.dbflute.logic.replaceschema.process.DfCreateSchemaProcess.CreatingDataSourcePlayer;
import org.seasar.dbflute.logic.replaceschema.process.DfLoadDataProcess;
import org.seasar.dbflute.logic.replaceschema.process.DfTakeFinallyProcess;
import org.seasar.dbflute.properties.DfReplaceSchemaProperties;
import org.seasar.dbflute.task.DfDBFluteTaskStatus.TaskType;
import org.seasar.dbflute.task.bs.DfAbstractTexenTask;
import org.seasar.dbflute.task.bs.assistant.DfDocumentSelector;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.9.8.3 (2011/04/29 Friday)
 */
public class DfReplaceSchemaTask extends DfAbstractTexenTask {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance. */
    private static final Log _log = LogFactory.getLog(DfReplaceSchemaTask.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected boolean _lazyConnection;
    protected DfReplaceSchemaFinalInfo _replaceSchemaFinalInfo;
    protected DfCreateSchemaFinalInfo _createSchemaFinalInfo;
    protected DfLoadDataFinalInfo _loadDataFinalInfo;
    protected DfTakeFinallyFinalInfo _takeFinallyFinalInfo;
    protected DfAlterCheckFinalInfo _alterCheckFinalInfo;
    protected String _areYouReadyAnswer; // from environment variable
    protected boolean _cancelled;
    protected String _varyingArg;
    protected final DfDocumentSelector _selector = new DfDocumentSelector(); // e.g. AlterCheck

    // ===================================================================================
    //                                                                           Beginning
    //                                                                           =========
    @Override
    protected boolean begin() {
        if (getBasicProperties().isSuppressReplaceSchemaTask()) {
            _log.info("...Suppressing ReplaceSchema task as basicInfoMap.dfprop");
            return false;
        }
        {
            _log.info("+------------------------------------------+");
            _log.info("|                                          |");
            _log.info("|              ReplaceSchema               |");
        }
        if (isAlterCheck()) {
            _log.info("|               (AlterCheck)               |");
        } else if (isSavePrevious()) {
            _log.info("|              (SavePrevious)              |");
        }
        {
            _log.info("|                                          |");
            _log.info("+------------------------------------------+");
        }
        DfDBFluteTaskStatus.getInstance().setTaskType(TaskType.ReplaceSchema);
        final boolean letsGo = waitBeforeReps();
        if (!letsGo) {
            _log.info("*The execution of ReplaceSchema was cancelled.");
            _cancelled = true;
            return false;
        }
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
    //                                                                         Change User
    //                                                                         ===========
    @Override
    protected void setupDataSource() throws SQLException {
        try {
            super.setupDataSource();
            getDataSource().getConnection(); // check
        } catch (SQLException e) {
            setupLazyConnection(e);
        }
    }

    protected void setupLazyConnection(SQLException e) throws SQLException {
        if (_lazyConnection) { // already lazy
            throw e;
        }
        String msg = e.getMessage();
        if (msg.length() > 50) {
            msg = msg.substring(0, 47) + "...";
        }
        _log.info("...Being a lazy connection: " + msg);
        destroyDataSource();
        _lazyConnection = true;
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    @Override
    protected void doExecute() {
        arrangeBeforeReps();
        if (isAlterProcess()) {
            processAlterCheck();
        } else {
            processMain();
        }
    }

    // -----------------------------------------------------
    //                          Arrange before ReplaceSchema
    //                          ----------------------------
    protected void arrangeBeforeReps() {
        final DfArrangeBeforeRepsProcess process = new DfArrangeBeforeRepsProcess();
        process.arrangeBeforeReps();
    }

    // -----------------------------------------------------
    //                                            AlterCheck
    //                                            ----------
    protected boolean isAlterProcess() {
        return isAlterCheck() || isSavePrevious();
    }

    protected boolean isAlterCheck() {
        return hasMigrationAlterCheckMark() || isForcedAlterCheck();
    }

    protected boolean isSavePrevious() {
        return hasMigrationSavePreviousMark() || isForcedSavePrevious();
    }

    protected void processAlterCheck() {
        doProcessAlterCheck();
    }

    protected void doProcessAlterCheck() {
        final DfAlterCheckProcess process = createAlterCheckProcess();
        try {
            if (isAlterCheck()) {
                if (isForcedAlterCheck()) {
                    process.useDraftSpace();
                }
                _alterCheckFinalInfo = process.checkAlter();
                if (_alterCheckFinalInfo.hasAlterCheckDiff()) {
                    outputAlterCheckResultHtml();
                }
            } else if (isSavePrevious()) {
                _alterCheckFinalInfo = process.savePrevious();
            }
            _alterCheckFinalInfo.throwAlterCheckExceptionIfExists();
        } finally {
            refreshResources(); // for output files by alter check
        }
    }

    protected DfAlterCheckProcess createAlterCheckProcess() {
        return DfAlterCheckProcess.createAsMain(getDataSource(), new CoreProcessPlayer() {
            public void playNext(String sqlRootDirectory) {
                executeCoreProcess(sqlRootDirectory, false);
            }

            public void playPrevious(String sqlRootDirectory) {
                executeCoreProcess(sqlRootDirectory, true);
            }
        });
    }

    protected void outputAlterCheckResultHtml() {
        _selector.selectAlterCheckResultHtml();
        fireVelocityProcess();
    }

    // -----------------------------------------------------
    //                                          Main Process
    //                                          ------------
    protected void processMain() {
        executeCoreProcess(getPlaySqlDir(), false);
    }

    // ===================================================================================
    //                                                                        Core Process
    //                                                                        ============
    protected void executeCoreProcess(String sqlRootDir, boolean previous) {
        doExecuteCoreProcess(sqlRootDir, previous);
    }

    protected void doExecuteCoreProcess(String sqlRootDir, boolean previous) {
        try {
            createSchema(sqlRootDir, previous);
            loadData(sqlRootDir, previous);
            takeFinally(sqlRootDir, previous);
        } finally {
            setupReplaceSchemaFinalInfo();
        }
        handleSchemaContinuedFailure();
    }

    protected void createSchema(String sqlRootDir, boolean previous) {
        final DfCreateSchemaProcess process = createCreateSchemaProcess(sqlRootDir);
        _createSchemaFinalInfo = process.execute();
        final SQLFailureException breakCause = _createSchemaFinalInfo.getBreakCause();
        if (breakCause != null) { // high priority exception
            throw breakCause;
        }
    }

    protected DfCreateSchemaProcess createCreateSchemaProcess(String sqlRootDir) {
        final CreatingDataSourcePlayer player = createCreatingDataSourcePlayer();
        return DfCreateSchemaProcess.createAsCore(sqlRootDir, player, _lazyConnection);
    }

    protected CreatingDataSourcePlayer createCreatingDataSourcePlayer() {
        return new CreatingDataSourcePlayer() {
            public DataSource callbackGetDataSource() {
                return getDataSource();
            }

            public void callbackSetupDataSource() throws SQLException {
                setupDataSource();
            }
        };
    }

    protected void loadData(String sqlRootDir, boolean previous) {
        final DfLoadDataProcess process = createLoadDataProcess(sqlRootDir, previous);
        _loadDataFinalInfo = process.execute();
        final RuntimeException loadEx = _loadDataFinalInfo.getLoadEx();
        if (loadEx != null) { // high priority exception
            throw loadEx;
        }
    }

    protected DfLoadDataProcess createLoadDataProcess(String sqlRootDir, boolean previous) {
        return DfLoadDataProcess.createAsCore(sqlRootDir, getDataSource(), previous);
    }

    protected void takeFinally(String sqlRootDir, boolean previous) {
        final DfTakeFinallyProcess process = createTakeFinallyProcess(sqlRootDir);
        _takeFinallyFinalInfo = process.execute();
        final SQLFailureException breakCause = _takeFinallyFinalInfo.getBreakCause();
        if (breakCause != null) { // high priority exception
            throw breakCause;
        }
        final DfTakeFinallyAssertionFailureException assertionEx = _takeFinallyFinalInfo.getAssertionEx();
        if (assertionEx != null) { // high priority exception
            throw assertionEx;
        }
    }

    protected DfTakeFinallyProcess createTakeFinallyProcess(String sqlRootDir) {
        return DfTakeFinallyProcess.createAsCore(sqlRootDir, getDataSource());
    }

    protected void setupReplaceSchemaFinalInfo() {
        _replaceSchemaFinalInfo = createReplaceSchemaFinalInfo();
    }

    protected DfReplaceSchemaFinalInfo createReplaceSchemaFinalInfo() {
        return new DfReplaceSchemaFinalInfo(_createSchemaFinalInfo, _loadDataFinalInfo, _takeFinallyFinalInfo);
    }

    protected void handleSchemaContinuedFailure() { // means continued errors
        final DfReplaceSchemaFinalInfo finalInfo = _replaceSchemaFinalInfo;
        if (finalInfo.isCreateSchemaFailure()) {
            String msg = "Failed to create schema (Look at the final info)";
            throw new DfCreateSchemaFailureException(msg);
        }
        if (finalInfo.isTakeFinallyFailure()) {
            String msg = "Failed to take finally (Look at the final info)";
            throw new DfTakeFinallyFailureException(msg);
        }
    }

    // ===================================================================================
    //                                                           Wait before ReplaceSchema 
    //                                                           =========================
    protected boolean waitBeforeReps() {
        if (_areYouReadyAnswer != null && "y".equals(_areYouReadyAnswer)) {
            return true;
        }
        _log.info("...Waiting for your GO SIGN from stdin before ReplaceSchema:");
        systemOutPrintLn("/- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
        systemOutPrintLn("Database: " + getDatabaseProperties().getDatabaseUrl());
        systemOutPrintLn("Schema: " + getDatabaseProperties().getDatabaseSchema().getLoggingSchema());
        systemOutPrintLn("- - - - - - - - - -/");
        systemOutPrintLn("ReplaceSchema will");
        systemOutPrintLn("");
        systemOutPrintLn("DDD   EEEEE L     EEEEE TTTTT EEEEE");
        systemOutPrintLn("D  D  E     L     E       T   E");
        systemOutPrintLn("D   D E     L     E       T   E");
        systemOutPrintLn("D   D EEEEE L     EEEEE   T   EEEEE");
        systemOutPrintLn("D   D E     L     E       T   E");
        systemOutPrintLn("D  D  E     L     E       T   E");
        systemOutPrintLn("DDD   EEEEE LLLLL EEEEE   T   EEEEE your all data!");
        systemOutPrintLn("");
        systemOutPrintLn("<Process Flow>");
        systemOutPrintLn("1. initialize your schema (*droping all existing tables)");
        systemOutPrintLn("2. create tables as your DDL");
        systemOutPrintLn("3. load your test data, e.g. excel files");
        systemOutPrintLn("");
        systemOutPrintLn("(input on your console)");
        systemOutPrint("Are you ready? (y or n): ");
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
            final String line = br.readLine();
            return line != null && "y".equals(line);
        } catch (IOException e) {
            String msg = "Failed to read system input.";
            throw new IllegalStateException(msg, e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    protected void systemOutPrint(Object msg) {
        System.out.print(msg);
    }

    protected void systemOutPrintLn(Object msg) {
        System.out.println(msg);
    }

    // ===================================================================================
    //                                                                          Final Info
    //                                                                          ==========
    @Override
    public String getFinalInformation() {
        return buildReplaceSchemaFinalMessage();
    }

    protected String buildReplaceSchemaFinalMessage() {
        final StringBuilder sb = new StringBuilder();
        if (_cancelled) {
            sb.append("    * * * * * * *").append(ln());
            sb.append("    * Cancelled *").append(ln());
            sb.append("    * * * * * * *");
            return sb.toString();
        }
        final DfReplaceSchemaFinalInfo finalInfo = _replaceSchemaFinalInfo; // null allowed
        boolean firstDone = false;

        // AlterFailure
        boolean alterFailure = false;
        {
            final DfAlterCheckFinalInfo alterCheckFinalInfo = _alterCheckFinalInfo;
            if (alterCheckFinalInfo != null && alterCheckFinalInfo.isValidInfo()) {
                alterFailure = alterCheckFinalInfo.isFailure();
            }
        }

        // CreateSchema
        if (finalInfo != null) {
            final DfCreateSchemaFinalInfo createSchemaFinalInfo = finalInfo.getCreateSchemaFinalInfo();
            if (createSchemaFinalInfo != null && createSchemaFinalInfo.isValidInfo()) {
                if (!alterFailure || createSchemaFinalInfo.isFailure()) {
                    if (firstDone) {
                        sb.append(ln()).append(ln());
                    }
                    firstDone = true;
                    buildSchemaTaskContents(sb, createSchemaFinalInfo);
                }
            }
        }

        // LoadData
        if (finalInfo != null) {
            final DfLoadDataFinalInfo loadDataFinalInfo = finalInfo.getLoadDataFinalInfo();
            if (loadDataFinalInfo != null && loadDataFinalInfo.isValidInfo()) {
                if (!alterFailure || loadDataFinalInfo.isFailure()) {
                    if (firstDone) {
                        sb.append(ln()).append(ln());
                    }
                    firstDone = true;
                    buildSchemaTaskContents(sb, loadDataFinalInfo);
                }
            }
        }

        // TakeFinally
        boolean assertionFailure = false;
        if (finalInfo != null) {
            final DfTakeFinallyFinalInfo takeFinallyFinalInfo = finalInfo.getTakeFinallyFinalInfo();
            if (takeFinallyFinalInfo != null) {
                assertionFailure = (takeFinallyFinalInfo.getAssertionEx() != null);
                if (takeFinallyFinalInfo.isValidInfo()) {
                    if (!alterFailure || takeFinallyFinalInfo.isFailure()) {
                        if (firstDone) {
                            sb.append(ln()).append(ln());
                        }
                        firstDone = true;
                        buildSchemaTaskContents(sb, takeFinallyFinalInfo);
                    }
                }
            }
        }

        // AlterSchema
        {
            final DfAlterCheckFinalInfo alterCheckFinalInfo = _alterCheckFinalInfo;
            if (alterCheckFinalInfo != null && alterCheckFinalInfo.isValidInfo()) {
                if (firstDone) {
                    sb.append(ln()).append(ln());
                }
                firstDone = true;
                buildSchemaTaskContents(sb, alterCheckFinalInfo);
            }
        }

        if (alterFailure) { // alter or create in AlterCheck
            sb.append(ln()).append("    * * * * * * * * * * *");
            sb.append(ln()).append("    * Migration Failure *");
            sb.append(ln()).append("    * * * * * * * * * * *");
        } else if (assertionFailure) { // assertion in normal time
            sb.append(ln()).append("    * * * * * * * * * * *");
            sb.append(ln()).append("    * Assertion Failure *");
            sb.append(ln()).append("    * * * * * * * * * * *");
        } else if (finalInfo != null && finalInfo.hasFailure()) { // as default
            sb.append(ln()).append("    * * * * * *");
            sb.append(ln()).append("    * Failure *");
            sb.append(ln()).append("    * * * * * *");
        }
        return sb.toString();
    }

    protected void buildSchemaTaskContents(StringBuilder sb, DfAbstractSchemaTaskFinalInfo finalInfo) {
        sb.append(" ").append(finalInfo.getResultMessage());
        final List<String> detailMessageList = finalInfo.getDetailMessageList();
        for (String detailMessage : detailMessageList) {
            sb.append(ln()).append("  ").append(detailMessage);
        }
    }

    // ===================================================================================
    //                                                                  Prepare Generation
    //                                                                  ==================
    @Override
    public Context initControlContext() throws Exception {
        _log.info("");
        _log.info("...Preparing generation of alter check");
        return createVelocityContext();
    }

    protected VelocityContext createVelocityContext() {
        final DfVelocityContextFactory factory = createVelocityContextFactory();
        final AppData appData = AppData.createAsEmpty();
        return factory.createAsCore(appData, _selector);
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfReplaceSchemaProperties getReplaceSchemaProperties() {
        return getProperties().getReplaceSchemaProperties();
    }

    protected String getPlaySqlDir() {
        return getReplaceSchemaProperties().getPlaySqlDir();
    }

    public boolean hasMigrationAlterCheckMark() {
        return getReplaceSchemaProperties().hasMigrationAlterCheckMark();
    }

    public boolean hasMigrationSavePreviousMark() {
        return getReplaceSchemaProperties().hasMigrationSavePreviousMark();
    }

    // ===================================================================================
    //                                                                      Varying Option
    //                                                                      ==============
    protected boolean isForcedAlterCheck() {
        return _varyingArg != null && _varyingArg.equals("alter-check");
    }

    protected boolean isForcedSavePrevious() {
        return _varyingArg != null && _varyingArg.equals("save-previous");
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setAreYouReadyAnswer(String areYouReadyAnswer) {
        _areYouReadyAnswer = areYouReadyAnswer;
    }

    public void setVaryingArg(String varyingArg) {
        if (Srl.is_Null_or_TrimmedEmpty(varyingArg)) {
            return;
        }
        _varyingArg = varyingArg;
    }
}
