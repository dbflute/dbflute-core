/*
 * Copyright 2014-2020 the original author or authors.
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
package org.dbflute.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.torque.engine.database.model.AppData;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.dbflute.exception.DfCreateSchemaFailureException;
import org.dbflute.exception.DfTakeFinallyAssertionFailureException;
import org.dbflute.exception.DfTakeFinallyFailureException;
import org.dbflute.exception.SQLFailureException;
import org.dbflute.friends.velocity.DfVelocityContextFactory;
import org.dbflute.logic.doc.spolicy.reps.DfSPolicyInRepsChecker;
import org.dbflute.logic.replaceschema.finalinfo.DfAbstractSchemaTaskFinalInfo;
import org.dbflute.logic.replaceschema.finalinfo.DfAlterCheckFinalInfo;
import org.dbflute.logic.replaceschema.finalinfo.DfCreateSchemaFinalInfo;
import org.dbflute.logic.replaceschema.finalinfo.DfLoadDataFinalInfo;
import org.dbflute.logic.replaceschema.finalinfo.DfReplaceSchemaFinalInfo;
import org.dbflute.logic.replaceschema.finalinfo.DfTakeFinallyFinalInfo;
import org.dbflute.logic.replaceschema.process.DfCreateSchemaProcess;
import org.dbflute.logic.replaceschema.process.DfCreateSchemaProcess.CreatingDataSourcePlayer;
import org.dbflute.logic.replaceschema.process.DfLoadDataProcess;
import org.dbflute.logic.replaceschema.process.DfTakeFinallyProcess;
import org.dbflute.logic.replaceschema.process.altercheck.DfAlterCheckProcess;
import org.dbflute.logic.replaceschema.process.altercheck.DfSavePreviousProcess;
import org.dbflute.logic.replaceschema.process.altercheck.player.DfAlterCoreProcessPlayer;
import org.dbflute.logic.replaceschema.process.arrangebefore.DfArrangeBeforeRepsProcess;
import org.dbflute.properties.DfReplaceSchemaProperties;
import org.dbflute.task.DfDBFluteTaskStatus.TaskType;
import org.dbflute.task.bs.DfAbstractTexenTask;
import org.dbflute.task.bs.assistant.DfDocumentSelector;
import org.dbflute.util.DfTraceViewUtil;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 0.9.8.3 (2011/04/29 Friday)
 */
public class DfReplaceSchemaTask extends DfAbstractTexenTask {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The logger instance for this class. (NotNull) */
    private static final Logger _log = LoggerFactory.getLogger(DfReplaceSchemaTask.class);

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
    protected final DfDocumentSelector _documentSelector = new DfDocumentSelector(); // e.g. AlterCheck, SchemaPolicy

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
        } else { // normally here
            processReplaceSchema();
        }
    }

    protected void arrangeBeforeReps() {
        final DfArrangeBeforeRepsProcess process = new DfArrangeBeforeRepsProcess();
        process.arrangeBeforeReps();
    }

    // ===================================================================================
    //                                                                          AlterCheck
    //                                                                          ==========
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
        try {
            if (isAlterCheck()) {
                final DfAlterCheckProcess process = createAlterCheckProcess();
                _alterCheckFinalInfo = process.checkAlter();
                if (_alterCheckFinalInfo.hasAlterCheckDiff()) {
                    outputAlterCheckResultHtml();
                }
            } else if (isSavePrevious()) {
                final DfSavePreviousProcess process = createSavePreviousProcess();
                _alterCheckFinalInfo = process.savePrevious();
            }
            _alterCheckFinalInfo.throwAlterCheckExceptionIfExists();
        } finally {
            refreshResources(); // for output files by alter check
        }
    }

    protected DfAlterCheckProcess createAlterCheckProcess() {
        return DfAlterCheckProcess.createAsMain(getDataSource(), createDBMigrationProcessPlayer());
    }

    protected DfSavePreviousProcess createSavePreviousProcess() {
        return DfSavePreviousProcess.createAsMain(getDataSource(), createDBMigrationProcessPlayer());
    }

    protected DfAlterCoreProcessPlayer createDBMigrationProcessPlayer() {
        final boolean schemaOnly = isSchemaOnlyAlterCheck();
        return new DfAlterCoreProcessPlayer() {
            public void playNext(String sqlRootDirectory) {
                executeCoreProcess(sqlRootDirectory, new DfRepsCoreProcessSelector().schemaOnly(schemaOnly));
            }

            public void playPrevious(String sqlRootDirectory) {
                executeCoreProcess(sqlRootDirectory, new DfRepsCoreProcessSelector().asPrevious().schemaOnly(schemaOnly));
            }
        };
    }

    protected void outputAlterCheckResultHtml() {
        _documentSelector.selectAlterCheckResultHtml();
        fireVelocityProcess();
    }

    // ===================================================================================
    //                                                                       ReplaceSchema
    //                                                                       =============
    // _/_/_/_/_/_/_/_/_/_/_/_/_/_/
    // <<< main process here! >>>
    // _/_/_/_/_/_/_/_/_/_/
    protected void processReplaceSchema() {
        final boolean dataOnly = isUseRepsAsDataManager();
        executeCoreProcess(getPlaySqlDir(), new DfRepsCoreProcessSelector().dataOnly(dataOnly));
    }

    // ===================================================================================
    //                                                                        Core Process
    //                                                                        ============
    // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
    // both ReplaceSchema, AlterCheck use this
    // _/_/_/_/_/_/_/_/_/_/
    protected void executeCoreProcess(String sqlRootDir, DfRepsCoreProcessSelector selector) {
        // ReplaceSchema flow here
        try {
            final boolean asPrevious = selector.isAsPrevious();
            if (!selector.isDataOnly()) { // basically here
                createSchema(sqlRootDir, asPrevious);
            } else { // e.g. using data manager option
                _log.info("*Skipped create schema because of dataOnly option.");
            }
            if (!selector.isSchemaOnly()) { // basically here
                loadData(sqlRootDir, asPrevious);
                takeFinally(sqlRootDir, asPrevious);
            } else { // e.g. using schema-only option for alter check
                _log.info("*Skipped load data and take-finally because of schemaOnly option.");
            }
        } finally {
            setupReplaceSchemaFinalInfo();
        }
        handleSchemaContinuedFailure();
    }

    protected static class DfRepsCoreProcessSelector {

        protected boolean _asPrevious;
        protected boolean _dataOnly;
        protected boolean _schemaOnly;

        public DfRepsCoreProcessSelector asPrevious() {
            _asPrevious = true;
            return this;
        }

        public DfRepsCoreProcessSelector dataOnly(boolean dataOnly) {
            _dataOnly = dataOnly;
            return this;
        }

        public DfRepsCoreProcessSelector schemaOnly(boolean schemaOnly) {
            _schemaOnly = schemaOnly;
            return this;
        }

        public boolean isAsPrevious() {
            return _asPrevious;
        }

        public boolean isDataOnly() {
            return _dataOnly;
        }

        public boolean isSchemaOnly() {
            return _schemaOnly;
        }
    }

    // -----------------------------------------------------
    //                                         Create Schema
    //                                         -------------
    protected void createSchema(String sqlRootDir, boolean previous) {
        final DfCreateSchemaProcess process = createCreateSchemaProcess(sqlRootDir);
        _createSchemaFinalInfo = process.execute();
        final SQLFailureException breakCause = _createSchemaFinalInfo.getBreakCause();
        if (breakCause != null) { // high priority exception
            throw breakCause;
        }
        if (!_createSchemaFinalInfo.isFailure()) { // because it may have low priority failure
            if (!previous) { // because previous is immutable
                checkSchemaPolicyInRepsIfNeeds();
            }
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

    protected void checkSchemaPolicyInRepsIfNeeds() {
        final long before = System.currentTimeMillis(); // no-if to be simple
        final DfSPolicyInRepsChecker checker = new DfSPolicyInRepsChecker(getDataSource(), _documentSelector);
        final boolean executed = checker.checkSchemaPolicyInRepsIfNeeds();
        if (executed) {
            final long after = System.currentTimeMillis();
            final long preformanceMillis = after - before;
            final Long originalMillis = _createSchemaFinalInfo.getProcessPerformanceMillis();
            if (originalMillis != null) {
                // because schema policy process is treated as create schema process in display
                // (hard to move schema policy process to create schema process so adjust it by logging logic)
                _createSchemaFinalInfo.setProcessPerformanceMillis(originalMillis + preformanceMillis);
            }
            final String performanceView = DfTraceViewUtil.convertToPerformanceView(preformanceMillis);
            _createSchemaFinalInfo.addDetailMessage("(Schema Policy) - " + performanceView);
        }
    }

    // -----------------------------------------------------
    //                                             Load Data
    //                                             ---------
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

    // -----------------------------------------------------
    //                                          Take Finally
    //                                          ------------
    protected void takeFinally(String sqlRootDir, boolean previous) {
        final DfTakeFinallyProcess process = createTakeFinallyProcess(sqlRootDir, previous);
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

    protected DfTakeFinallyProcess createTakeFinallyProcess(String sqlRootDir, boolean previous) {
        if (previous) {
            return DfTakeFinallyProcess.createAsPrevious(sqlRootDir, getDataSource());
        } else {
            return DfTakeFinallyProcess.createAsCore(sqlRootDir, getDataSource(), _documentSelector);
        }
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
                } catch (IOException ignored) {}
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

        // AlterCheck (AlterSchema, SavePrevious)
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
        final Long processPerformanceMillis = finalInfo.getProcessPerformanceMillis();
        if (processPerformanceMillis != null) { // almost true (except e.g. save failure)
            sb.append(" - ").append(DfTraceViewUtil.convertToPerformanceView(processPerformanceMillis));
        }
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
        return factory.createAsCore(appData, _documentSelector);
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

    protected boolean isSchemaOnlyAlterCheck() {
        return getReplaceSchemaProperties().isSchemaOnlyAlterCheck();
    }

    protected boolean isUseRepsAsDataManager() {
        return getReplaceSchemaProperties().isUseRepsAsDataManager();
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
