/*
 * Copyright 2014-2023 the original author or authors.
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
package org.dbflute.logic.replaceschema.process.altercheck;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.dbflute.exception.DfAlterCheckAlterSqlNotFoundException;
import org.dbflute.exception.DfAlterCheckDifferenceFoundException;
import org.dbflute.exception.DfAlterCheckEmptyAlterSqlSuccessException;
import org.dbflute.exception.DfAlterCheckReplaceSchemaFailureException;
import org.dbflute.exception.DfTakeFinallyAssertionFailureException;
import org.dbflute.exception.SQLFailureException;
import org.dbflute.helper.jdbc.DfRunnerInformation;
import org.dbflute.helper.jdbc.context.DfSchemaSource;
import org.dbflute.helper.jdbc.sqlfile.DfSqlFileFireMan;
import org.dbflute.helper.jdbc.sqlfile.DfSqlFileFireResult;
import org.dbflute.helper.jdbc.sqlfile.DfSqlFileRunner;
import org.dbflute.helper.jdbc.sqlfile.DfSqlFileRunnerDispatcher;
import org.dbflute.helper.jdbc.sqlfile.DfSqlFileRunnerExecute;
import org.dbflute.helper.jdbc.sqlfile.DfSqlFileRunnerExecute.DfRunnerDispatchResult;
import org.dbflute.helper.jdbc.sqlfile.DfSqlFileRunnerResult;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.helper.process.SystemScript;
import org.dbflute.logic.doc.craftdiff.DfCraftDiffAssertDirection;
import org.dbflute.logic.doc.historyhtml.DfSchemaHistory;
import org.dbflute.logic.jdbc.schemadiff.DfNextPreviousDiff;
import org.dbflute.logic.jdbc.schemadiff.DfSchemaDiff;
import org.dbflute.logic.jdbc.schemaxml.DfSchemaXmlSerializer;
import org.dbflute.logic.replaceschema.dataassert.DfDataAssertHandler;
import org.dbflute.logic.replaceschema.dataassert.DfDataAssertProvider;
import org.dbflute.logic.replaceschema.finalinfo.DfAlterCheckFinalInfo;
import org.dbflute.logic.replaceschema.finalinfo.DfTakeFinallyFinalInfo;
import org.dbflute.logic.replaceschema.process.DfTakeFinallyProcess;
import org.dbflute.logic.replaceschema.process.altercheck.player.DfAlterCoreProcessPlayer;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.DfTypeUtil;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 0.9.8.3 (2011/04/29 Friday)
 */
public class DfAlterCheckProcess extends DfAbstractAlterProcess {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The logger instance for this class. (NotNull) */
    private static final Logger _log = LoggerFactory.getLogger(DfAlterCheckProcess.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                                 Trace
    //                                                 -----
    protected final List<File> _executedAlterSqlFileList = DfCollectionUtil.newArrayList();

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    protected DfAlterCheckProcess(DfSchemaSource dataSource, DfAlterCoreProcessPlayer coreProcessPlayer) {
        super(dataSource, coreProcessPlayer);
    }

    public static DfAlterCheckProcess createAsMain(DfSchemaSource dataSource, DfAlterCoreProcessPlayer coreProcessPlayer) {
        return new DfAlterCheckProcess(dataSource, coreProcessPlayer);
    }

    // ===================================================================================
    //                                                                  AlterCheck Process
    //                                                                  ==================
    public DfAlterCheckFinalInfo checkAlter() {
        deleteAllNGMark();
        deleteSchemaXml();
        deleteCraftMeta();

        final DfAlterCheckFinalInfo finalInfo = new DfAlterCheckFinalInfo();

        // after AlterCheck, the database has altered schema
        // so you can check your application on the environment

        becomeNextSchema(finalInfo); // to be next DB
        if (finalInfo.isFailure()) {
            return finalInfo;
        }
        serializeNextSchema();

        becomePreviousSchema(); // to be previous DB
        alterSchema(finalInfo);
        if (finalInfo.isFailure()) {
            return finalInfo;
        }
        serializePreviousSchema();

        deleteAlterCheckResultDiff(); // to replace the result file
        final DfSchemaDiff schemaDiff = schemaDiff();
        if (schemaDiff.hasDiff()) {
            processDifference(finalInfo, schemaDiff);
        } else {
            processSuccess(finalInfo);
            deleteAlterCheckMark();
            deleteCraftMeta();
        }

        deleteSchemaXml(); // not finally because of trace when abort
        return finalInfo;
    }

    // ===================================================================================
    //                                                                         Next Schema
    //                                                                         ===========
    protected void becomeNextSchema(DfAlterCheckFinalInfo finalInfo) {
        _log.info("");
        _log.info("+---------------------+");
        _log.info("|                     |");
        _log.info("|     Next Schema     |");
        _log.info("|   (ReplaceSchema)   |");
        _log.info("|                     |");
        _log.info("+---------------------+");
        try {
            playMainProcess();
        } catch (RuntimeException threwLater) {
            markNextNG(getAlterCheckReplaceSchemaFailureNotice());
            setupAlterCheckReplaceSchemaFailureException(finalInfo, threwLater);
        }
    }

    protected void playMainProcess() {
        _coreProcessPlayer.playNext(getPlaySqlDir());
    }

    protected void markNextNG(String notice) {
        makeNextNGMarkFile(notice);
        // no needed now so code design is still hard, will implement at future
        //_alterControlAgent.makeWholeNGStateMapFile(...);
    }

    protected void makeNextNGMarkFile(String notice) {
        final String ngMark = getMigrationNextNGMark();
        try {
            final File ngFile = new File(ngMark);
            if (ngFile.exists()) { // basically already deleted here 
                ngFile.delete(); // overwrite just in case
            }
            _log.info("...Marking next-NG: " + ngMark);
            ngFile.createNewFile();
            writeControlLogRoad(ngFile, notice);
        } catch (IOException e) {
            String msg = "Failed to create a file for next-NG mark: " + ngMark;
            throw new IllegalStateException(msg, e);
        }
    }

    protected void setupAlterCheckReplaceSchemaFailureException(DfAlterCheckFinalInfo finalInfo, RuntimeException e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice(getAlterCheckReplaceSchemaFailureNotice());
        br.addItem("Advice");
        br.addElement("Make sure your NextDDL or data for ReplaceSchema are correct,");
        br.addElement("resources just below 'playsql' directory, are correct.");
        br.addElement("and after that, execute AlterCheck again.");
        String msg = br.buildExceptionMessage();
        finalInfo.setReplaceSchemaFailureEx(new DfAlterCheckReplaceSchemaFailureException(msg, e));
        finalInfo.setFailure(true);
        finalInfo.addDetailMessage("x (replace failure)");
    }

    protected String getAlterCheckReplaceSchemaFailureNotice() {
        return "Failed to replace the schema using NextDDL.";
    }

    // ===================================================================================
    //                                                                     Previous Schema
    //                                                                     ===============
    protected void becomePreviousSchema() {
        _log.info("");
        _log.info("+----------------------+");
        _log.info("|                      |");
        _log.info("|   Previous Schema    |");
        _log.info("|   (ReplaceSchema)    |");
        _log.info("|                      |");
        _log.info("+----------------------+");
        try {
            final boolean unzipped = extractPreviousResource();
            playPreviousSchema();
            if (unzipped) {
                deleteExtractedPreviousResource();
            }
        } catch (RuntimeException e) { // basically no way because of checked before saving
            markPreviousNG(_previousDBAgent.getAlterCheckRollbackSchemaFailureNotice());
            _previousDBAgent.throwAlterCheckRollbackSchemaFailureException(e);
        }
    }

    // ===================================================================================
    //                                                                         AlterSchema
    //                                                                         ===========
    protected void alterSchema(DfAlterCheckFinalInfo finalInfo) {
        _log.info("");
        _log.info("+------------------+");
        _log.info("|                  |");
        _log.info("|   Alter Schema   |");
        _log.info("|                  |");
        _log.info("+------------------+");
        final long before = System.currentTimeMillis();
        executeAlterSql(finalInfo);
        if (finalInfo.isFailure()) {
            markAlterNG(getAlterCheckAlterSqlFailureNotice(), "ALF");
        } else {
            takeFinally(finalInfo);
            if (finalInfo.isFailure()) {
                markAlterNG(getAlterCheckTakeFinallySqlFailureNotice(), "TFF");
            }
        }
        final long after = System.currentTimeMillis();
        finalInfo.setProcessPerformanceMillis(after - before);
    }

    // -----------------------------------------------------
    //                                            Alter Fire
    //                                            ----------
    protected void executeAlterSql(DfAlterCheckFinalInfo finalInfo) {
        List<File> alterSqlFileList = findMigrationAlterSqlFileList();
        if (alterSqlFileList.isEmpty()) {
            _unreleasedAlterAgent.restoreUnreleasedAlterSql();
            alterSqlFileList = findMigrationAlterSqlFileList();
            if (alterSqlFileList.isEmpty()) {
                createEmptyAlterSqlFileIfNotExists();
                alterSqlFileList = findMigrationAlterSqlFileList();
                if (alterSqlFileList.isEmpty()) { // no way
                    throwAlterCheckAlterSqlNotFoundException();
                }
            }
        }
        final DfRunnerInformation runInfo = createRunnerInformation();
        final DfSqlFileFireMan fireMan = createSqlFileFireMan();
        final DfSqlFileRunner runner = createSqlFileRunner(runInfo);
        finalInfo.addAlterSqlFileAll(alterSqlFileList);
        try {
            final DfSqlFileFireResult result = fireMan.fire(runner, alterSqlFileList);
            reflectAlterResultToFinalInfo(finalInfo, result);
        } catch (DfTakeFinallyAssertionFailureException e) {
            handleTakeFinallyAssertionFailureException(finalInfo, e);
        }
    }

    protected List<File> findMigrationAlterSqlFileList() {
        return createBasicAlterSqlFileFinder().findResourceFileList(getMigrationAlterDirectory());
    }

    protected File createEmptyAlterSqlFileIfNotExists() {
        final String alterDirPath = getMigrationAlterDirectory();
        {
            final File alterDir = new File(alterDirPath);
            if (!alterDir.exists()) {
                alterDir.mkdirs();
            }
        }
        final File alterSqlFile = getMigrationSimpleAlterSqlFile();
        if (!alterSqlFile.exists()) {
            try {
                _log.info("...Creating alter SQL file as empty to " + resolvePath(alterSqlFile));
                alterSqlFile.createNewFile();
            } catch (IOException e) {
                String msg = "Failed to create new file: " + alterSqlFile;
                throw new IllegalStateException(msg);
            }
        }
        return alterSqlFile;
    }

    protected void throwAlterCheckAlterSqlNotFoundException() {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the AlterDDL under the alter directory.");
        br.addItem("Advice");
        br.addElement("You should put AlterDDL under the alter directory like this:");
        br.addElement("  playsql");
        br.addElement("   |-data");
        br.addElement("   |-migration");
        br.addElement("   |  |-alter");
        br.addElement("   |  |  |-alter-schema.sql");
        br.addElement("   |  |-previous");
        br.addElement("   |  |-schema");
        final String msg = br.buildExceptionMessage();
        throw new DfAlterCheckAlterSqlNotFoundException(msg);
    }

    protected DfSqlFileFireMan createSqlFileFireMan() {
        final String[] scriptExtAry = SystemScript.getSupportedExtList().toArray(new String[] {});
        final SystemScript script = new SystemScript();
        final DfSqlFileFireMan fireMan = new DfSqlFileFireMan() {
            @Override
            protected DfSqlFileRunnerResult processSqlFile(DfSqlFileRunner runner, File sqlFile) {
                _executedAlterSqlFileList.add(sqlFile);
                if (isScriptFile(sqlFile, scriptExtAry)) {
                    return processScriptFile(runner, script, sqlFile);
                } else { // mainly here
                    return super.processSqlFile(runner, sqlFile);
                }
            }
        };
        fireMan.setExecutorName("Alter Schema");
        return fireMan;
    }

    protected DfSqlFileRunner createSqlFileRunner(final DfRunnerInformation runInfo) {
        final String loadType = getReplaceSchemaProperties().getRepsEnvType();
        final DfDataAssertProvider dataAssertProvider = new DfDataAssertProvider(loadType);
        final DfSqlFileRunnerExecute runnerExecute = new DfSqlFileRunnerExecute(runInfo, _dataSource);
        runnerExecute.setDispatcher(new DfSqlFileRunnerDispatcher() {
            public DfRunnerDispatchResult dispatch(File sqlFile, Statement st, String sql) throws SQLException {
                final DfDataAssertHandler dataAssertHandler = dataAssertProvider.provideDataAssertHandler(sql);
                if (dataAssertHandler == null) {
                    return DfRunnerDispatchResult.NONE;
                }
                dataAssertHandler.handle(sqlFile, st, sql);
                return DfRunnerDispatchResult.DISPATCHED;
            }
        });
        return runnerExecute;
    }

    protected void reflectAlterResultToFinalInfo(DfAlterCheckFinalInfo finalInfo, DfSqlFileFireResult fireResult) {
        finalInfo.setResultMessage(fireResult.getResultMessage());
        final List<String> detailMessageList = extractDetailMessageList(fireResult);
        for (String detailMessage : detailMessageList) {
            finalInfo.addDetailMessage(detailMessage);
        }
        finalInfo.setAlterSqlCount(fireResult.getTotalSqlCount());
        finalInfo.setBreakCause(fireResult.getBreakCause());
        finalInfo.setFailure(fireResult.isExistsError());
    }

    protected String getAlterCheckAlterSqlFailureNotice() {
        return "Failed to execute the AlterDDL statements.";
    }

    protected void handleTakeFinallyAssertionFailureException(DfAlterCheckFinalInfo finalInfo, DfTakeFinallyAssertionFailureException e) {
        finalInfo.setResultMessage("Alter Check: *asserted");
        final int fileListSize = _executedAlterSqlFileList.size();
        int index = 0;
        for (File executedAlterSqlFile : _executedAlterSqlFileList) {
            final StringBuilder sb = new StringBuilder();
            final String pureFileName = Srl.substringLastRear(resolvePath(executedAlterSqlFile), "/");
            if (index == fileListSize - 1) { // last loop
                sb.append("x ");
            } else {
                sb.append("o ");
            }
            sb.append(pureFileName);
            finalInfo.addDetailMessage(sb.toString());
            ++index;
        }
        finalInfo.addDetailMessage(" >> " + DfTypeUtil.toClassTitle(e));
        finalInfo.addDetailMessage(" (Look at the exception message: console or dbflute.log)");
        finalInfo.setTakeFinallyAssertionEx(e);
        finalInfo.setFailure(true);
    }

    // -----------------------------------------------------
    //                                          Take Finally
    //                                          ------------
    protected void takeFinally(DfAlterCheckFinalInfo finalInfo) { // alter-take-finally.sql for assertion
        final String sqlRootDir = getMigrationAlterDirectory();
        final DfTakeFinallyProcess process = DfTakeFinallyProcess.createAsAlterSchema(sqlRootDir, _dataSource);
        final DfTakeFinallyFinalInfo takeFinally = process.execute();
        finalInfo.addAlterSqlFileAll(takeFinally.getTakeFinallySqlFileList());
        reflectTakeFinallyResultToFinalInfo(finalInfo, takeFinally);
    }

    protected void reflectTakeFinallyResultToFinalInfo(DfAlterCheckFinalInfo finalInfo, DfTakeFinallyFinalInfo takeFinally) {
        final List<String> detailMessageList = takeFinally.getDetailMessageList();
        for (String detailMessage : detailMessageList) {
            finalInfo.addDetailMessage(detailMessage);
        }
        final SQLFailureException breakCause = takeFinally.getBreakCause();
        if (breakCause != null) {
            finalInfo.setBreakCause(breakCause);
        }
        final DfTakeFinallyAssertionFailureException assertionEx = takeFinally.getAssertionEx();
        if (assertionEx != null) {
            finalInfo.setTakeFinallyAssertionEx(assertionEx);
        }
        if (takeFinally.isFailure()) {
            finalInfo.setFailure(true);
        }
    }

    protected String getAlterCheckTakeFinallySqlFailureNotice() {
        return "Failed to assert the AlterDDL's TakeFinally statements.";
    }

    protected void deleteAlterCheckMark() {
        final String mark = getMigrationAlterCheckMark();
        deleteControlFile(new File(mark), "...Deleting alter-check mark");
    }

    // ===================================================================================
    //                                                                    Serialize Schema
    //                                                                    ================
    protected void serializeNextSchema() {
        final String nextXml = getMigrationAlterCheckNextSchemaXml();
        final DfCraftDiffAssertDirection direction = DfCraftDiffAssertDirection.DIRECT_NEXT;
        final DfSchemaXmlSerializer serializer = createSchemaXmlSerializer(nextXml, direction);
        serializer.serialize();
    }

    protected void serializePreviousSchema() {
        final String previousXml = getMigrationAlterCheckPreviousSchemaXml();
        final DfCraftDiffAssertDirection direction = DfCraftDiffAssertDirection.DIRECT_PREVIOUS;
        final DfSchemaXmlSerializer serializer = createSchemaXmlSerializer(previousXml, direction);
        serializer.serialize();
    }

    protected DfSchemaXmlSerializer createSchemaXmlSerializer(String schemaXml, DfCraftDiffAssertDirection direction) {
        // no use history here (use SchemaDiff directly later)
        final DfSchemaXmlSerializer serializer = DfSchemaXmlSerializer.createAsManage(_dataSource, schemaXml);
        final String craftMetaDir = getMigrationAlterCheckCraftMetaDir();
        serializer.enableCraftDiff(_dataSource, craftMetaDir, direction);
        return serializer;
    }

    // ===================================================================================
    //                                                                          SchemaDiff
    //                                                                          ==========
    protected DfSchemaDiff schemaDiff() {
        _log.info("");
        _log.info("+-----------------+");
        _log.info("|                 |");
        _log.info("|   Schema Diff   |");
        _log.info("|                 |");
        _log.info("+-----------------|");
        final String previousXml = getMigrationAlterCheckPreviousSchemaXml();
        final String nextXml = getMigrationAlterCheckNextSchemaXml();
        final DfSchemaDiff schemaDiff = DfSchemaDiff.createAsAlterCheck(previousXml, nextXml);
        schemaDiff.enableCraftDiff(getMigrationAlterCheckCraftMetaDir());
        schemaDiff.loadPreviousSchema();
        schemaDiff.loadNextSchema(); // always can read here so no check
        schemaDiff.analyzeDiff();
        return schemaDiff;
    }

    // ===================================================================================
    //                                                                     Different Story
    //                                                                     ===============
    protected void processDifference(DfAlterCheckFinalInfo finalInfo, DfSchemaDiff schemaDiff) {
        _log.info("");
        _log.info("+---------------------+");
        _log.info("|                     |");
        _log.info("|   Different Story   |");
        _log.info("|                     |");
        _log.info("+---------------------+");
        serializeSchemaDiff(schemaDiff);
        markAlterNG(getAlterDiffNotice(), "DIF");
        handleAlterDiff(finalInfo, schemaDiff);
    }

    protected void serializeSchemaDiff(DfSchemaDiff schemaDiff) {
        final String diffMapFile = getMigrationAlterCheckDiffMapFile();
        final DfSchemaHistory schemaHistory = DfSchemaHistory.createAsMonolithic(diffMapFile);
        try {
            _log.info("...Serializing schema diff: " + diffMapFile);
            schemaHistory.serializeSchemaDiff(schemaDiff);
        } catch (IOException e) {
            String msg = "Failed to serialize schema diff: file=" + diffMapFile;
            throw new IllegalStateException(msg, e);
        }
    }

    protected void markAlterNG(String notice, String alterNgCode) { // e.g. DIF, ALF, TFF
        makeAlterNGMarkFile(notice);
        _alterControlAgent.makeWholeNGStateMapFile("ALT", alterNgCode);
    }

    protected void makeAlterNGMarkFile(String notice) {
        final String ngMark = getMigrationAlterNGMark();
        try {
            final File ngFile = new File(ngMark);
            if (ngFile.exists()) { // basically already deleted here 
                ngFile.delete(); // overwrite just in case
            }
            _log.info("...Marking alter-NG: " + ngMark);
            ngFile.createNewFile();
            writeControlLogRoad(ngFile, notice);
        } catch (IOException e) {
            String msg = "Failed to create a file for alter-NG mark: " + ngMark;
            throw new IllegalStateException(msg, e);
        }
    }

    protected void handleAlterDiff(DfAlterCheckFinalInfo finalInfo, DfSchemaDiff schemaDiff) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice(getAlterDiffNotice());
        br.addItem("Advice");
        setupFixedAlterAdviceMessage(br);
        br.addElement("");
        br.addElement("You can see the details at");
        br.addElement(" '" + getMigrationAlterCheckResultFilePath() + "'.");
        br.addItem("Diff Date");
        br.addElement(schemaDiff.getDiffDate());
        final DfNextPreviousDiff tableCountDiff = schemaDiff.getTableCount();
        if (tableCountDiff != null && tableCountDiff.hasDiff()) {
            br.addItem("Table Count");
            br.addElement(tableCountDiff.getPrevious() + " to " + tableCountDiff.getNext());
        }
        final String msg = br.buildExceptionMessage();
        finalInfo.setDiffFoundEx(new DfAlterCheckDifferenceFoundException(msg));
        finalInfo.setFailure(true);
        finalInfo.addDetailMessage("x (found alter diff)");
    }

    protected String getAlterDiffNotice() {
        return "Found the differences between AlterDDL and NextDDL.";
    }

    protected void setupFixedAlterAdviceMessage(ExceptionMessageBuilder br) {
        br.addElement("Make sure your AlterDDL are correct,");
        br.addElement("and after that, execute AlterCheck again.");
    }

    // ===================================================================================
    //                                                                       Success Story
    //                                                                       =============
    protected void processSuccess(DfAlterCheckFinalInfo finalInfo) {
        _log.info("");
        _log.info("+-------------------+");
        _log.info("|                   |");
        _log.info("|   Success Story   |");
        _log.info("|                   |");
        _log.info("+-------------------+");
        checkEmptyAlterSuccess(finalInfo);
        _unreleasedAlterAgent.saveAlterAsUnreleased(finalInfo);
        deleteAllNGMark();
        deleteDiffResult();
    }

    protected void checkEmptyAlterSuccess(DfAlterCheckFinalInfo finalInfo) {
        if (!finalInfo.hasAlterSqlExecution()) {
            throwAlterCheckEmptyAlterSqlSuccessException();
        }
    }

    protected void throwAlterCheckEmptyAlterSqlSuccessException() {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("No DB change. (no difference but empty AlterDDL)");
        br.addItem("Advice");
        br.addElement("This is unneccessary success.");
        br.addElement("You should check after changing schema.");
        final String msg = br.buildExceptionMessage();
        throw new DfAlterCheckEmptyAlterSqlSuccessException(msg);
    }

    // ===================================================================================
    //                                                                       Delete Result
    //                                                                       =============
    protected void deleteDiffResult() {
        deleteAlterCheckResultDiff();
    }

    protected void deleteAlterCheckResultDiff() {
        final String diffMap = getMigrationAlterCheckDiffMapFile();
        deleteControlFile(new File(diffMap), "...Deleting AlterCheck diffmap file");
        final String resultFile = getMigrationAlterCheckResultFilePath();
        deleteControlFile(new File(resultFile), "...Deleting AlterCheck result file");
    }

    // ===================================================================================
    //                                                                         Delete Meta
    //                                                                         ===========
    protected void deleteSchemaXml() {
        final String previousXml = getMigrationAlterCheckPreviousSchemaXml();
        final String nextXml = getMigrationAlterCheckNextSchemaXml();
        deleteControlFile(new File(previousXml), "...Deleting SchemaXml file for previous schema");
        deleteControlFile(new File(nextXml), "...Deleting SchemaXml file for next schema");
    }

    protected void deleteCraftMeta() {
        final String craftMetaDir = getMigrationAlterCheckCraftMetaDir();
        if (craftMetaDir != null) {
            final List<File> metaFileList = getCraftMetaFileList(craftMetaDir);
            for (File metaFile : metaFileList) {
                deleteControlFile(metaFile, "...Deleting craft meta");
            }
        }
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    // -----------------------------------------------------
    //                                        Alter Resource
    //                                        --------------
    protected File getMigrationSimpleAlterSqlFile() {
        return getReplaceSchemaProperties().getMigrationSimpleAlterSqlFile();
    }

    // -----------------------------------------------------
    //                                       Schema Resource
    //                                       ---------------
    protected String getMigrationAlterCheckDiffMapFile() {
        return getReplaceSchemaProperties().getMigrationAlterCheckDiffMapFile();
    }

    protected String getMigrationAlterCheckResultFilePath() {
        return getReplaceSchemaProperties().getMigrationAlterCheckResultFilePath();
    }

    protected String getMigrationAlterCheckPreviousSchemaXml() {
        return getReplaceSchemaProperties().getMigrationAlterCheckPreviousSchemaXml();
    }

    protected String getMigrationAlterCheckNextSchemaXml() {
        return getReplaceSchemaProperties().getMigrationAlterCheckNextSchemaXml();
    }

    protected String getMigrationAlterCheckCraftMetaDir() {
        return getReplaceSchemaProperties().getMigrationAlterCheckCraftMetaDir();
    }

    protected List<File> getCraftMetaFileList(String craftMetaDir) {
        return getDocumentProperties().getCraftMetaFileList(craftMetaDir);
    }

    // -----------------------------------------------------
    //                                         Mark Resource
    //                                         -------------
    protected String getMigrationAlterCheckMark() {
        return getReplaceSchemaProperties().getMigrationAlterCheckMark();
    }

    protected String getMigrationNextNGMark() {
        return getReplaceSchemaProperties().getMigrationNextNGMark();
    }

    protected String getMigrationAlterNGMark() {
        return getReplaceSchemaProperties().getMigrationAlterNGMark();
    }
}
