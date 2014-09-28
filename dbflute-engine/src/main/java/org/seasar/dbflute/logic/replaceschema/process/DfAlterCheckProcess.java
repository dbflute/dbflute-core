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
package org.seasar.dbflute.logic.replaceschema.process;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.util.FileUtils;
import org.seasar.dbflute.exception.DfAlterCheckAlterScriptSQLException;
import org.seasar.dbflute.exception.DfAlterCheckAlterSqlNotFoundException;
import org.seasar.dbflute.exception.DfAlterCheckDataSourceNotFoundException;
import org.seasar.dbflute.exception.DfAlterCheckDifferenceFoundException;
import org.seasar.dbflute.exception.DfAlterCheckEmptyAlterSqlSuccessException;
import org.seasar.dbflute.exception.DfAlterCheckReplaceSchemaFailureException;
import org.seasar.dbflute.exception.DfAlterCheckRollbackSchemaFailureException;
import org.seasar.dbflute.exception.DfAlterCheckSavePreviousFailureException;
import org.seasar.dbflute.exception.DfAlterCheckSavePreviousInvalidStatusException;
import org.seasar.dbflute.exception.DfTakeFinallyAssertionFailureException;
import org.seasar.dbflute.exception.SQLFailureException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.helper.io.compress.DfZipArchiver;
import org.seasar.dbflute.helper.jdbc.DfRunnerInformation;
import org.seasar.dbflute.helper.jdbc.context.DfSchemaSource;
import org.seasar.dbflute.helper.jdbc.sqlfile.DfSqlFileFireMan;
import org.seasar.dbflute.helper.jdbc.sqlfile.DfSqlFileFireResult;
import org.seasar.dbflute.helper.jdbc.sqlfile.DfSqlFileRunner;
import org.seasar.dbflute.helper.jdbc.sqlfile.DfSqlFileRunnerDispatcher;
import org.seasar.dbflute.helper.jdbc.sqlfile.DfSqlFileRunnerExecute;
import org.seasar.dbflute.helper.jdbc.sqlfile.DfSqlFileRunnerExecute.DfRunnerDispatchResult;
import org.seasar.dbflute.helper.jdbc.sqlfile.DfSqlFileRunnerResult;
import org.seasar.dbflute.helper.process.ProcessResult;
import org.seasar.dbflute.helper.process.SystemScript;
import org.seasar.dbflute.helper.process.exception.SystemScriptUnsupportedScriptException;
import org.seasar.dbflute.logic.doc.craftdiff.DfCraftDiffAssertDirection;
import org.seasar.dbflute.logic.doc.historyhtml.DfSchemaHistory;
import org.seasar.dbflute.logic.jdbc.schemadiff.DfNextPreviousDiff;
import org.seasar.dbflute.logic.jdbc.schemadiff.DfSchemaDiff;
import org.seasar.dbflute.logic.jdbc.schemaxml.DfSchemaXmlSerializer;
import org.seasar.dbflute.logic.replaceschema.dataassert.DfDataAssertHandler;
import org.seasar.dbflute.logic.replaceschema.dataassert.DfDataAssertProvider;
import org.seasar.dbflute.logic.replaceschema.finalinfo.DfAlterCheckFinalInfo;
import org.seasar.dbflute.logic.replaceschema.finalinfo.DfTakeFinallyFinalInfo;
import org.seasar.dbflute.resource.DBFluteSystem;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.DfStringUtil;
import org.seasar.dbflute.util.DfTypeUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.9.8.3 (2011/04/29 Friday)
 */
public class DfAlterCheckProcess extends DfAbstractReplaceSchemaProcess {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance. */
    private static final Log _log = LogFactory.getLog(DfAlterCheckProcess.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                        Basic Resource
    //                                        --------------
    protected final DfSchemaSource _dataSource;
    protected final CoreProcessPlayer _coreProcessPlayer;

    // -----------------------------------------------------
    //                                                 Trace
    //                                                 -----
    protected final List<File> _executedAlterSqlFileList = DfCollectionUtil.newArrayList();

    // -----------------------------------------------------
    //                                                Option
    //                                                ------
    protected boolean _useDraftSpace; // old style so basically unused

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    protected DfAlterCheckProcess(DfSchemaSource dataSource, CoreProcessPlayer coreProcessPlayer) {
        if (dataSource == null) { // for example, ReplaceSchema may have lazy connection
            throwAlterCheckDataSourceNotFoundException();
        }
        _dataSource = dataSource;
        _coreProcessPlayer = coreProcessPlayer;
    }

    protected void throwAlterCheckDataSourceNotFoundException() {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the data source for AlterCheck.");
        br.addItem("Advice");
        br.addElement("Make sure your database process works");
        br.addElement("or your connection settings are correct.");
        String msg = br.buildExceptionMessage();
        throw new DfAlterCheckDataSourceNotFoundException(msg);
    }

    public static DfAlterCheckProcess createAsMain(DfSchemaSource dataSource, CoreProcessPlayer coreProcessPlayer) {
        return new DfAlterCheckProcess(dataSource, coreProcessPlayer);
    }

    public static interface CoreProcessPlayer {
        void playNext(String sqlRootDir);

        void playPrevious(String sqlRootDir);
    }

    // ===================================================================================
    //                                                                SavePrevious Process
    //                                                                ====================
    public DfAlterCheckFinalInfo savePrevious() {
        _log.info("");
        _log.info("+-------------------+");
        _log.info("|                   |");
        _log.info("|   Save Previous   |");
        _log.info("|                   |");
        _log.info("+-------------------+");
        deleteAllNGMark();
        final DfAlterCheckFinalInfo finalInfo = new DfAlterCheckFinalInfo();
        finalInfo.setResultMessage("{Save Previous}");
        if (!checkSavePreviousInvalidStatus(finalInfo)) {
            return finalInfo;
        }
        finishPreviousCheckedAlter();
        deleteExtractedPreviousResource();
        final List<File> copyToFileList = copyToPreviousResource();
        compressPreviousResource();
        if (!checkSavedPreviousResource(finalInfo)) {
            return finalInfo; // failure
        }
        markPreviousOK(copyToFileList);
        deleteSavePreviousMark();
        finalInfo.addDetailMessage("o (all resources saved)");
        return finalInfo;
    }

    // -----------------------------------------------------
    //                                          Check Status
    //                                          ------------
    protected boolean checkSavePreviousInvalidStatus(DfAlterCheckFinalInfo finalInfo) {
        final List<File> alterSqlFileList = getMigrationAlterSqlFileList();
        if (!alterSqlFileList.isEmpty()) {
            setupAlterCheckSavePreviousInvalidStatusException(finalInfo);
        }
        if (finalInfo.isFailure()) {
            finalInfo.addDetailMessage("x (save failure)");
            return false;
        }
        return true;
    }

    protected void setupAlterCheckSavePreviousInvalidStatusException(DfAlterCheckFinalInfo finalInfo) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice(getAlterCheckSavePreviousInvalidStatusNotice());
        br.addItem("Advice");
        br.addElement("Make sure your 'alter' directory is empty.");
        br.addElement("It might be miss choise if alter file exists.");
        br.addElement("Do you want to execute SavePrevious really?");
        String msg = br.buildExceptionMessage();
        finalInfo.setSavePreviousFailureEx(new DfAlterCheckSavePreviousInvalidStatusException(msg));
        finalInfo.setFailure(true);
    }

    protected String getAlterCheckSavePreviousInvalidStatusNotice() {
        return "Invalid status for SavePrevious.";
    }

    // -----------------------------------------------------
    //                                   Finish CheckedAlter
    //                                   -------------------
    protected void finishPreviousCheckedAlter() {
        final String previousDate = findLatestPreviousDate();
        if (previousDate == null) {
            return;
        }
        final File checkedAlterZip = findLatestCheckedAlterZip(previousDate);
        if (checkedAlterZip == null) {
            return;
        }
        final String checkedAlterMarkBasicName = getMigrationCheckedAlterMarkBasicName();
        final String finishedAlterMarkBasicName = getMigrationFinishedAlterMarkBasicName();
        final String path = resolvePath(checkedAlterZip);
        final String baseDir = Srl.substringLastFront(path, "/");
        final String pureName = Srl.substringLastRear(path, "/");
        final String renamedName = Srl.replace(pureName, checkedAlterMarkBasicName, finishedAlterMarkBasicName);
        final File renamedFile = new File(baseDir + "/" + renamedName);
        _log.info("...Finishing the previous history (renamed to): " + resolvePath(renamedFile));
        checkedAlterZip.renameTo(renamedFile);
    }

    // -----------------------------------------------------
    //                                     Compress Resource
    //                                     -----------------
    protected void compressPreviousResource() {
        deleteExistingPreviousZip();
        final File previousZip = getCurrentTargetPreviousZip();
        _log.info("...Compressing the previous resources to zip: " + resolvePath(previousZip));
        final DfZipArchiver archiver = new DfZipArchiver(previousZip);
        archiver.compress(new File(getMigrationPreviousDir()), new FileFilter() {
            public boolean accept(File file) {
                final String name = file.getName();
                final boolean result;
                if (file.isDirectory()) {
                    result = !name.startsWith(".");
                } else {
                    result = !name.endsWith(".zip");
                }
                if (result) {
                    _log.info("  " + resolvePath(file));
                }
                return result;
            }
        });
    }

    protected File getCurrentTargetPreviousZip() {
        final String date = DfTypeUtil.toString(DBFluteSystem.currentDate(), "yyyyMMdd-HHmm");
        return new File(getMigrationPreviousDir() + "/previous-" + date + ".zip");
    }

    protected void deleteExistingPreviousZip() {
        final List<File> zipFiles = findPreviousZipList();
        for (File zipFile : zipFiles) {
            zipFile.delete();
        }
    }

    protected List<File> findPreviousZipList() {
        final File previousDir = new File(getMigrationPreviousDir());
        final File[] zipFiles = previousDir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return isPreviousZip(file);
            }
        });
        final List<File> fileList;
        if (zipFiles != null) {
            fileList = Arrays.asList(zipFiles);
        } else {
            fileList = DfCollectionUtil.emptyList();
        }
        return fileList;
    }

    protected boolean isPreviousZip(File file) {
        final String name = file.getName();
        return name.startsWith("previous-") && name.endsWith(".zip");
    }

    // -----------------------------------------------------
    //                                       Delete Resource
    //                                       ---------------
    protected void deleteExtractedPreviousResource() {
        final List<File> previousFileList = findHierarchyFileList(getMigrationPreviousDir());
        if (previousFileList.isEmpty()) {
            return;
        }
        _log.info("...Deleting the extracted previous resources");
        for (File previousFile : previousFileList) {
            if (isPreviousZip(previousFile)) {
                continue;
            }
            deleteFile(previousFile, null);
        }
    }

    // -----------------------------------------------------
    //                                         Copy Resource
    //                                         -------------
    protected List<File> copyToPreviousResource() {
        final List<File> copyToFileList = new ArrayList<File>();
        final String previousDir = getMigrationPreviousDir();
        final String playSqlDirSymbol = getPlaySqlDirPureName() + "/";
        final Map<String, File> replaceSchemaSqlFileMap = getReplaceSchemaSqlFileMap();
        for (File mainFile : replaceSchemaSqlFileMap.values()) {
            doCopyToPreviousResource(mainFile, previousDir, playSqlDirSymbol, copyToFileList);
        }
        final Map<String, File> takeFinallySqlFileMap = getTakeFinallySqlFileMap();
        for (File mainFile : takeFinallySqlFileMap.values()) {
            doCopyToPreviousResource(mainFile, previousDir, playSqlDirSymbol, copyToFileList);
        }
        final List<File> dataFileList = findHierarchyFileList(getSchemaDataDir());
        for (File dataFile : dataFileList) {
            doCopyToPreviousResource(dataFile, previousDir, playSqlDirSymbol, copyToFileList);
        }
        return copyToFileList;
    }

    protected void doCopyToPreviousResource(File mainFile, String previousDir, String playSqlDirSymbol,
            List<File> copyToFileList) {
        final String relativePath = Srl.substringLastRear(resolvePath(mainFile), playSqlDirSymbol);
        final File copyToFile = new File(previousDir + "/" + relativePath);
        final File copyToDir = new File(Srl.substringLastFront(resolvePath(copyToFile), "/"));
        if (!copyToDir.exists()) {
            copyToDir.mkdirs();
        }
        if (copyToFile.exists()) {
            copyToFile.delete();
        }
        _log.info("...Saving the file to " + resolvePath(copyToFile));
        copyFile(mainFile, copyToFile);
        copyToFileList.add(copyToFile);
    }

    // -----------------------------------------------------
    //                                        Check Resource
    //                                        --------------
    protected boolean checkSavedPreviousResource(DfAlterCheckFinalInfo finalInfo) {
        final boolean unzipped = extractPreviousResource();
        _log.info("...Checking the previous resources by replacing");
        try {
            playPreviousSchema();
        } catch (RuntimeException threwLater) { // basically no way because of checked before saving
            markPreviousNG(getAlterCheckSavePreviousFailureNotice());
            setupAlterCheckSavePreviousFailureException(finalInfo, threwLater);
        }
        if (finalInfo.isFailure()) {
            finalInfo.addDetailMessage("x (save failure)");
            return false;
        }
        if (unzipped) {
            deleteExtractedPreviousResource();
        }
        return true;
    }

    protected void setupAlterCheckSavePreviousFailureException(DfAlterCheckFinalInfo finalInfo,
            RuntimeException threwLater) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice(getAlterCheckSavePreviousFailureNotice());
        br.addItem("Advice");
        br.addElement("Make sure your DDL and data for ReplaceSchema,");
        br.addElement("resources just below 'playsql' directory, are correct.");
        br.addElement("and after that, execute SavePrevious again.");
        String msg = br.buildExceptionMessage();
        finalInfo.setSavePreviousFailureEx(new DfAlterCheckSavePreviousFailureException(msg, threwLater));
        finalInfo.setFailure(true);
    }

    protected String getAlterCheckSavePreviousFailureNotice() {
        return "Failed to replace by saved resources for previous schema.";
    }

    // -----------------------------------------------------
    //                                      Extract Resource
    //                                      ----------------
    protected boolean extractPreviousResource() {
        final File previousZip = findLatestPreviousZip();
        if (previousZip == null) {
            _log.info("*Not found the zip for previous resources");
            return false;
        }
        deleteExtractedPreviousResource();
        _log.info("...Extracting the previous resources from zip: " + resolvePath(previousZip));
        final DfZipArchiver archiver = new DfZipArchiver(previousZip);
        final Set<String> traceSet = new HashSet<String>();
        archiver.extract(new File(getMigrationPreviousDir()), new FileFilter() {
            public boolean accept(File file) {
                final String path = resolvePath(file);
                traceSet.add(path);
                _log.info("  " + path);
                return true;
            }
        });
        if (traceSet.isEmpty()) {
            String msg = "Not found the files in the zip: " + resolvePath(previousZip);
            throw new IllegalStateException(msg);
        }
        return true;
    }

    protected File findLatestPreviousZip() {
        final List<File> previousZipList = findPreviousZipList();
        if (previousZipList.isEmpty()) {
            return null;
        }
        return findLatestNameFile(previousZipList);
    }

    // -----------------------------------------------------
    //                                                  Mark
    //                                                  ----
    protected void markPreviousOK(List<File> copyToFileList) {
        final String okMark = getMigrationPreviousOKMark();
        try {
            final File markFile = new File(okMark);
            _log.info("...Marking previous-OK: " + okMark);
            markFile.createNewFile();
            final StringBuilder sb = new StringBuilder();
            sb.append("[Saved Previous Resources]: " + currentDate());
            for (File moveToFile : copyToFileList) {
                sb.append(ln()).append(resolvePath(moveToFile));
            }
            sb.append(ln()).append("(").append(copyToFileList.size()).append(" files)");
            sb.append(ln());
            writeNotice(markFile, sb.toString());
        } catch (IOException e) {
            String msg = "Failed to create a file for previous-OK mark: " + okMark;
            throw new IllegalStateException(msg, e);
        }
    }

    protected void deleteSavePreviousMark() {
        final String mark = getMigrationSavePreviousMark();
        deleteFile(new File(mark), "...Deleting the save-previous mark");
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

        replaceSchema(finalInfo); // to be next DB
        if (finalInfo.isFailure()) {
            return finalInfo;
        }
        serializeNextSchema();

        rollbackSchema(); // to be previous DB
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

        deleteSubmittedDraftFile(finalInfo);
        deleteSchemaXml(); // not finally because of trace when abort
        return finalInfo;
    }

    // ===================================================================================
    //                                                                       ReplaceSchema
    //                                                                       =============
    protected void replaceSchema(DfAlterCheckFinalInfo finalInfo) {
        _log.info("");
        _log.info("+--------------------+");
        _log.info("|                    |");
        _log.info("|   Replace Schema   |");
        _log.info("|                    |");
        _log.info("+--------------------+");
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
        final String ngMark = getMigrationNextNGMark();
        try {
            final File markFile = new File(ngMark);
            if (!markFile.exists()) {
                _log.info("...Marking next-NG: " + ngMark);
                markFile.createNewFile();
                writeNotice(markFile, notice);
            }
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
    //                                                                    Roll-back Schema
    //                                                                    ================
    protected void rollbackSchema() {
        _log.info("");
        _log.info("+----------------------+");
        _log.info("|                      |");
        _log.info("|   Roll-back Schema   |");
        _log.info("|                      |");
        _log.info("+----------------------+");
        try {
            final boolean unzipped = extractPreviousResource();
            playPreviousSchema();
            if (unzipped) {
                deleteExtractedPreviousResource();
            }
        } catch (RuntimeException e) { // basically no way because of checked before saving
            markPreviousNG(getAlterCheckRollbackSchemaFailureNotice());
            throwAlterCheckRollbackSchemaFailureException(e);
        }
    }

    protected void playPreviousSchema() {
        _coreProcessPlayer.playPrevious(getMigrationPreviousDir());
    }

    protected void markPreviousNG(String notice) {
        deletePreviousOKMark();
        final String ngMark = getMigrationPreviousNGMark();
        try {
            final File markFile = new File(ngMark);
            if (!markFile.exists()) {
                _log.info("...Marking previous-NG: " + ngMark);
                markFile.createNewFile();
                writeNotice(markFile, notice);
            }
        } catch (IOException e) {
            String msg = "Failed to create a file for previous-NG mark: " + ngMark;
            throw new IllegalStateException(msg, e);
        }
    }

    protected void throwAlterCheckRollbackSchemaFailureException(RuntimeException e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice(getAlterCheckRollbackSchemaFailureNotice());
        br.addItem("Advice");
        br.addElement("The AlterCheck requires that PreviousDDL are correct.");
        br.addElement("So you should prepare the PreviousDDL again.");
        final String msg = br.buildExceptionMessage();
        throw new DfAlterCheckRollbackSchemaFailureException(msg, e);
    }

    protected String getAlterCheckRollbackSchemaFailureNotice() {
        return "Failed to rollback the schema to previous state.";
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
        submitDraftFile(finalInfo);
        executeAlterSql(finalInfo);
        if (finalInfo.isFailure()) {
            markAlterNG(getAlterCheckAlterSqlFailureNotice());
            deleteSubmittedDraftFile(finalInfo);
        } else {
            takeFinally(finalInfo);
            if (finalInfo.isFailure()) {
                markAlterNG(getAlterCheckTakeFinallySqlFailureNotice());
                deleteSubmittedDraftFile(finalInfo);
            }
        }
    }

    // -----------------------------------------------------
    //                                            Draft File
    //                                            ----------
    protected void submitDraftFile(DfAlterCheckFinalInfo finalInfo) {
        if (!_useDraftSpace) {
            return;
        }
        final List<File> submittedFileList = new ArrayList<File>();
        final List<File> draftAlterSqlFileList = getMigrationDraftAlterSqlFileList();
        draftAlterSqlFileList.addAll(getMigrationDraftTakeFinallySqlFileList());
        for (File draftAlterSqlFile : draftAlterSqlFileList) {
            final String draftFilePath = resolvePath(draftAlterSqlFile);
            final String dirBase = Srl.substringLastFront(draftFilePath, "/alter/draft/");
            String pureFileName = Srl.substringLastRear(draftFilePath, "/draft/");
            if (pureFileName.startsWith("draft-")) {
                pureFileName = Srl.substringFirstRear(pureFileName, "draft-");
            }
            final File dest = new File(dirBase + "/alter/" + pureFileName);
            submittedFileList.add(dest);
            _log.info("...Submitting the draft file to " + resolvePath(dest));
            copyFile(draftAlterSqlFile, dest);
        }
        finalInfo.addSubmittedDraftFileAll(submittedFileList);
    }

    protected void deleteSubmittedDraftFile(DfAlterCheckFinalInfo finalInfo) {
        final List<File> submittedFileList = finalInfo.getSubmittedDraftFileList();
        if (submittedFileList == null) {
            return;
        }
        for (File submittedFile : submittedFileList) {
            deleteFile(submittedFile, "...Deleting the submitted draft file");
        }
    }

    // -----------------------------------------------------
    //                                            Alter Fire
    //                                            ----------
    protected void executeAlterSql(DfAlterCheckFinalInfo finalInfo) {
        List<File> alterSqlFileList = getMigrationAlterSqlFileList();
        if (alterSqlFileList.isEmpty()) {
            restoreLatestCheckedAlterSql();
            alterSqlFileList = getMigrationAlterSqlFileList();
            if (alterSqlFileList.isEmpty()) {
                createEmptyAlterSqlFileIfNotExists();
                alterSqlFileList = getMigrationAlterSqlFileList();
                if (alterSqlFileList.isEmpty()) { // no way
                    throwAlterCheckAlterSqlNotFoundException();
                }
            }
        }
        final DfRunnerInformation runInfo = createRunnerInformation();
        final DfSqlFileFireMan fireMan = createSqlFileFireMan();
        fireMan.setExecutorName("Alter Schema");
        final DfSqlFileRunner runner = createSqlFileRunner(runInfo);
        finalInfo.addAlterSqlFileAll(alterSqlFileList);
        try {
            final DfSqlFileFireResult result = fireMan.fire(runner, alterSqlFileList);
            reflectAlterResultToFinalInfo(finalInfo, result);
        } catch (DfTakeFinallyAssertionFailureException e) {
            handleTakeFinallyAssertionFailureException(finalInfo, e);
        }
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
                _log.info("...Creating the alter SQL file as empty to " + resolvePath(alterSqlFile));
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

    protected void restoreLatestCheckedAlterSql() {
        final String previousDate = findLatestPreviousDate();
        if (previousDate == null) {
            return;
        }
        final File checkedAlterZip = findLatestCheckedAlterZip(previousDate);
        if (checkedAlterZip == null) {
            return;
        }
        _log.info("...Restoring the latest checked-alter: " + resolvePath(checkedAlterZip));
        final DfZipArchiver archiver = new DfZipArchiver(checkedAlterZip);
        archiver.extract(new File(getMigrationAlterDirectory()), new FileFilter() {
            public boolean accept(File file) {
                _log.info("  " + resolvePath(file));
                return true;
            }
        });
    }

    protected DfSqlFileFireMan createSqlFileFireMan() {
        final String[] scriptExtAry = SystemScript.getSupportedExtList().toArray(new String[] {});
        final SystemScript script = new SystemScript();
        return new DfSqlFileFireMan() {
            @Override
            protected DfSqlFileRunnerResult processSqlFile(DfSqlFileRunner runner, File sqlFile) {
                _executedAlterSqlFileList.add(sqlFile);
                final String path = resolvePath(sqlFile);
                if (!Srl.endsWith(path, scriptExtAry)) { // SQL file
                    return super.processSqlFile(runner, sqlFile);
                }
                // script file
                final String baseDir = Srl.substringLastFront(path, "/");
                final String scriptName = Srl.substringLastRear(path, "/");
                _log.info("...Executing the script: " + path);
                final ProcessResult processResult;
                try {
                    processResult = script.execute(new File(baseDir), scriptName);
                } catch (SystemScriptUnsupportedScriptException ignored) {
                    _log.info("Skipped the script for system mismatch: " + scriptName);
                    return null;
                }
                final String console = processResult.getConsole();
                if (Srl.is_NotNull_and_NotTrimmedEmpty(console)) {
                    _log.info("Catched the console for " + scriptName + ":" + ln() + console);
                }
                final DfSqlFileRunnerResult runnerResult = new DfSqlFileRunnerResult(sqlFile);
                runnerResult.setTotalSqlCount(1);
                final int exitCode = processResult.getExitCode();
                if (exitCode != 0) {
                    final String msg = "The script failed: " + scriptName + " exitCode=" + exitCode;
                    final SQLException sqlEx = new DfAlterCheckAlterScriptSQLException(msg);
                    final String sqlExp = "(commands on the script)";
                    runnerResult.addErrorContinuedSql(sqlExp, sqlEx);
                    return runnerResult;
                } else {
                    runnerResult.setGoodSqlCount(1);
                    return runnerResult;
                }
            }
        };
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

    protected void handleTakeFinallyAssertionFailureException(DfAlterCheckFinalInfo finalInfo,
            DfTakeFinallyAssertionFailureException e) {
        finalInfo.setResultMessage("{Alter Schema}: *asserted");
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
    protected void takeFinally(DfAlterCheckFinalInfo finalInfo) {
        final String sqlRootDir = getMigrationAlterDirectory();
        final DfTakeFinallyProcess process = DfTakeFinallyProcess.createAsAlterCheck(sqlRootDir, _dataSource);
        final DfTakeFinallyFinalInfo takeFinally = process.execute();
        finalInfo.addAlterSqlFileAll(takeFinally.getTakeFinallySqlFileList());
        reflectTakeFinallyResultToFinalInfo(finalInfo, takeFinally);
    }

    protected void reflectTakeFinallyResultToFinalInfo(DfAlterCheckFinalInfo finalInfo,
            DfTakeFinallyFinalInfo takeFinally) {
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
        deleteFile(new File(mark), "...Deleting the alter-check mark");
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
        final String historyFile = null; // no use history here (use SchemaDiff directly later)
        final DfSchemaXmlSerializer serializer = DfSchemaXmlSerializer.createAsManage(_dataSource, schemaXml,
                historyFile);
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
        schemaDiff.loadNextSchema();
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
        markAlterNG(getAlterDiffNotice());
        handleAlterDiff(finalInfo, schemaDiff);
    }

    protected void serializeSchemaDiff(DfSchemaDiff schemaDiff) {
        final String diffMapFile = getMigrationAlterCheckDiffMapFile();
        final DfSchemaHistory schemaHistory = DfSchemaHistory.createAsPlain(diffMapFile);
        try {
            _log.info("...Serializing schema diff: " + diffMapFile);
            schemaHistory.serializeSchemaDiff(schemaDiff);
        } catch (IOException e) {
            String msg = "Failed to serialize schema diff: file=" + diffMapFile;
            throw new IllegalStateException(msg, e);
        }
    }

    protected void markAlterNG(String notice) {
        final String ngMark = getMigrationAlterNGMark();
        try {
            final File markFile = new File(ngMark);
            if (!markFile.exists()) {
                _log.info("...Marking alter-NG: " + ngMark);
                markFile.createNewFile();
                writeNotice(markFile, notice);
            }
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
        saveHistory(finalInfo);
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
        br.addNotice("The AlterDDL was empty so unnecessary success.");
        br.addItem("Advice");
        br.addElement("You should check after changing schema.");
        final String msg = br.buildExceptionMessage();
        throw new DfAlterCheckEmptyAlterSqlSuccessException(msg);
    }

    protected void saveHistory(DfAlterCheckFinalInfo finalInfo) {
        final String previousDate = findLatestPreviousDate(); // may be null
        final List<File> alterSqlFileList = finalInfo.getAlterSqlFileList();
        if (alterSqlFileList == null) { // just in case
            return;
        }
        final String checkedAlterZipName;
        if (previousDate != null) {
            checkedAlterZipName = buildCheckedAlterZipName(previousDate);
        } else {
            checkedAlterZipName = getMigrationCheckedAlterMarkBasicName() + ".zip";
        }
        skipSameStoryHistory(checkedAlterZipName, previousDate);
        compressCheckedAlterZip(checkedAlterZipName);
        deleteAlterSqlFile(alterSqlFileList);
    }

    protected String buildCheckedAlterZipName(String previousDate) {
        return getMigrationCheckedAlterMarkBasicName() + "-to-" + previousDate + ".zip";
    }

    protected String getHistoryCurrentDir() {
        final String historyDir = getMigrationHistoryDir();
        final Date currentDate = new Date();
        final String middleDir = DfTypeUtil.toString(currentDate, "yyyyMM");
        mkdirsDirIfNotExists(historyDir + "/" + middleDir);
        // e.g. history/201104/20110429_2247
        final String yyyyMMddHHmm = DfTypeUtil.toString(currentDate, "yyyyMMdd_HHmm");
        final String currentDir = historyDir + "/" + middleDir + "/" + yyyyMMddHHmm;
        mkdirsDirIfNotExists(currentDir);
        return currentDir;
    }

    protected void skipSameStoryHistory(String checkedAlterZipName, String previousDate) {
        if (previousDate == null) { // basically for compatible
            return;
        }
        final List<File> firstLevelDirList = findHistoryFirstLevelDirList();
        final String markBasicName = getMigrationSkippedAlterMarkBasicName();
        for (File firstLevelDir : firstLevelDirList) {
            final List<File> secondLevelDirList = findHistorySecondLevelDirList(firstLevelDir);
            for (File secondLevelDir : secondLevelDirList) {
                final String basePath = resolvePath(secondLevelDir);
                final File successStoryFile = new File(basePath + "/" + checkedAlterZipName);
                if (successStoryFile.exists()) {
                    _log.info("...Skipping the same story: " + basePath);
                    final String skippedZipName = basePath + "/" + markBasicName + "-to-" + previousDate + ".zip";
                    successStoryFile.renameTo(new File(skippedZipName));
                }
            }
        }
    }

    protected void compressCheckedAlterZip(final String checkedAlterZipName) {
        final String checkedAlterZipPath = getHistoryCurrentDir() + "/" + checkedAlterZipName;
        _log.info("...Saving the history to " + checkedAlterZipPath);
        final DfZipArchiver archiver = new DfZipArchiver(new File(checkedAlterZipPath));
        archiver.suppressCompressSubDir();
        archiver.compress(new File(getMigrationAlterDirectory()), new FileFilter() {
            public boolean accept(File file) {
                _log.info("  " + resolvePath(file));
                return true;
            }
        });
    }

    protected void deleteAlterSqlFile(final List<File> alterSqlFileList) {
        for (File alterSqlFile : alterSqlFileList) {
            deleteFile(alterSqlFile, "...Deleting the executed alterSqlFile");
        }
    }

    // ===================================================================================
    //                                                                   Previous Resource
    //                                                                   =================
    protected String findLatestPreviousDate() {
        return doExtractPreviousDate(findLatestPreviousZip());
    }

    protected String doExtractPreviousDate(File previousZip) {
        if (previousZip == null) {
            return null;
        }
        final String previousName = Srl.substringLastFront(previousZip.getName(), ".");
        return Srl.substringFirstRear(previousName, "previous-");
    }

    // ===================================================================================
    //                                                                    History Resource
    //                                                                    ================
    protected File findCheckedAlterZip(File secondLevelDir, String previousDate) {
        if (secondLevelDir == null) {
            return null;
        }
        final String checkedAlterZipName = buildCheckedAlterZipName(previousDate);
        final File[] listFiles = secondLevelDir.listFiles(new FilenameFilter() {
            public boolean accept(File file, String name) {
                return name.equals(checkedAlterZipName);
            }
        });
        if (listFiles == null || listFiles.length == 0) {
            return null;
        }
        return listFiles[0]; // must be only one
    }

    protected List<File> findCheckedAlterZipList(String previousDate) {
        final List<File> fisrtLevelDirList = findHistoryFirstLevelDirList();
        final List<File> checkedAlterZipList = DfCollectionUtil.newArrayList();
        for (File fisrtLevelDir : fisrtLevelDirList) {
            final List<File> secondLevelDirList = findHistorySecondLevelDirList(fisrtLevelDir);
            for (File secondLevelDir : secondLevelDirList) {
                final File checkedAlter = findCheckedAlterZip(secondLevelDir, previousDate);
                if (checkedAlter != null) {
                    checkedAlterZipList.add(checkedAlter);
                }
            }
        }
        return checkedAlterZipList;
    }

    protected File findLatestCheckedAlterZip(String previousDate) {
        final List<File> fisrtLevelDirList = findHistoryFirstLevelDirList();
        final File latestFisrtLevelDir = findLatestNameFile(fisrtLevelDirList);
        if (latestFisrtLevelDir == null) {
            return null;
        }
        final List<File> secondLevelDirList = findHistorySecondLevelDirList(latestFisrtLevelDir);
        final File latestSecondLevelDir = findLatestNameFile(secondLevelDirList);
        if (latestSecondLevelDir == null) {
            return null;
        }
        return findCheckedAlterZip(latestSecondLevelDir, previousDate);
    }

    protected List<File> findHistoryFirstLevelDirList() {
        final File historyDir = new File(getMigrationHistoryDir());
        if (!historyDir.isDirectory() || !historyDir.exists()) {
            return DfCollectionUtil.newArrayList();
        }
        final File[] firstLevelDirList = historyDir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });
        if (firstLevelDirList == null) {
            return DfCollectionUtil.newArrayList();
        }
        return DfCollectionUtil.newArrayList(firstLevelDirList);
    }

    protected List<File> findHistorySecondLevelDirList(File firstLevelDir) {
        if (firstLevelDir == null) {
            return DfCollectionUtil.newArrayList();
        }
        final File[] secondLevelDirList = firstLevelDir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });
        if (secondLevelDirList == null) {
            return DfCollectionUtil.newArrayList();
        }
        return DfCollectionUtil.newArrayList(secondLevelDirList);
    }

    // ===================================================================================
    //                                                                         Delete Mark
    //                                                                         ===========
    protected void deleteAllNGMark() {
        deleteNextNGMark();
        deleteAlterNGMark();
        deletePreviousNGMark();
    }

    protected void deletePreviousOKMark() {
        final String previousOKMark = getMigrationPreviousOKMark();
        deleteFile(new File(previousOKMark), "...Deleting the previous-OK mark");
    }

    protected void deleteNextNGMark() {
        final String replaceNGMark = getMigrationNextNGMark();
        deleteFile(new File(replaceNGMark), "...Deleting the next-NG mark");
    }

    protected void deleteAlterNGMark() {
        final String alterNGMark = getMigrationAlterNGMark();
        deleteFile(new File(alterNGMark), "...Deleting the alter-NG mark");
    }

    protected void deletePreviousNGMark() {
        final String previousNGMark = getMigrationPreviousNGMark();
        deleteFile(new File(previousNGMark), "...Deleting the previous-NG mark");
    }

    protected void deleteDiffResult() {
        deleteAlterCheckResultDiff();
    }

    protected void deleteAlterCheckResultDiff() {
        final String diffMap = getMigrationAlterCheckDiffMapFile();
        deleteFile(new File(diffMap), "...Deleting the AlterCheck diffmap file");
        final String resultFile = getMigrationAlterCheckResultFilePath();
        deleteFile(new File(resultFile), "...Deleting the AlterCheck result file");
    }

    // ===================================================================================
    //                                                                             Closing
    //                                                                             =======
    protected void deleteSchemaXml() {
        final String previousXml = getMigrationAlterCheckPreviousSchemaXml();
        final String nextXml = getMigrationAlterCheckNextSchemaXml();
        deleteFile(new File(previousXml), "...Deleting the SchemaXml file for previous schema");
        deleteFile(new File(nextXml), "...Deleting the SchemaXml file for next schema");
    }

    protected void deleteCraftMeta() {
        final String craftMetaDir = getMigrationAlterCheckCraftMetaDir();
        if (craftMetaDir != null) {
            final List<File> metaFileList = getCraftMetaFileList(craftMetaDir);
            for (File metaFile : metaFileList) {
                deleteFile(metaFile, "...Deleting the craft meta");
            }
        }
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected String currentDate() {
        return DfTypeUtil.toString(DBFluteSystem.currentDate(), "yyyy/MM/dd HH:mm:ss");
    }

    protected List<File> findHierarchyFileList(String rootDir) {
        final List<File> fileList = new ArrayList<File>();
        doFindHierarchyFileList(fileList, new File(rootDir));
        return fileList;
    }

    protected void doFindHierarchyFileList(final List<File> fileList, File baseDir) {
        if (baseDir.getName().startsWith(".")) { // closed directory
            return; // e.g. .svn
        }
        final File[] listFiles = baseDir.listFiles(new FileFilter() {
            public boolean accept(File subFile) {
                if (subFile.isDirectory()) {
                    doFindHierarchyFileList(fileList, subFile);
                    return false;
                }
                return true;
            }
        });
        if (listFiles != null) {
            fileList.addAll(Arrays.asList(listFiles));
        }
    }

    protected File findLatestNameFile(List<File> fileList) {
        File latestFile = null;
        for (File currentFile : fileList) {
            if (latestFile == null || latestFile.getName().compareTo(currentFile.getName()) < 0) {
                latestFile = currentFile;
            }
        }
        return latestFile;
    }

    protected void deleteFile(File file, String msg) {
        if (file.exists()) {
            if (msg != null) {
                _log.info(msg + ": " + resolvePath(file));
            }
            file.delete();
        }
    }

    protected void copyFile(File src, File dest) {
        try {
            FileUtils.getFileUtils().copyFile(src, dest);
        } catch (IOException e) {
            String msg = "Failed to copy file: " + src + " to " + dest;
            throw new IllegalStateException(msg, e);
        }
    }

    protected void writeNotice(File file, String notice) throws IOException {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
            bw.write(notice + ln() + "Look at the log for detail.");
            bw.flush();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    protected void mkdirsDirIfNotExists(String dirPath) {
        final File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    // -----------------------------------------------------
    //                                         ReplaceSchema
    //                                         -------------
    protected String getPlaySqlDir() {
        return getReplaceSchemaProperties().getPlaySqlDir();
    }

    protected String getPlaySqlDirPureName() {
        return getReplaceSchemaProperties().getPlaySqlDirPureName();
    }

    protected Map<String, File> getReplaceSchemaSqlFileMap() {
        return getReplaceSchemaProperties().getReplaceSchemaSqlFileMap(getPlaySqlDir());
    }

    protected Map<String, File> getTakeFinallySqlFileMap() {
        return getReplaceSchemaProperties().getTakeFinallySqlFileMap(getPlaySqlDir());
    }

    protected String getSchemaDataDir() {
        return getReplaceSchemaProperties().getSchemaDataDir(getPlaySqlDir());
    }

    // -----------------------------------------------------
    //                                        Alter Resource
    //                                        --------------
    protected String getMigrationAlterDirectory() {
        return getReplaceSchemaProperties().getMigrationAlterDirectory();
    }

    protected List<File> getMigrationAlterSqlFileList() {
        return getReplaceSchemaProperties().getMigrationAlterSqlFileList();
    }

    protected File getMigrationSimpleAlterSqlFile() {
        return getReplaceSchemaProperties().getMigrationSimpleAlterSqlFile();
    }

    protected List<File> getMigrationDraftAlterSqlFileList() {
        return getReplaceSchemaProperties().getMigrationDraftAlterSqlFileList();
    }

    protected List<File> getMigrationDraftTakeFinallySqlFileList() {
        return getReplaceSchemaProperties().getMigrationDraftTakeFinallySqlFileList();
    }

    // -----------------------------------------------------
    //                                     Previous Resource
    //                                     -----------------
    protected String getMigrationPreviousDir() {
        return getReplaceSchemaProperties().getMigrationPreviousDir();
    }

    protected Map<String, File> getMigrationPreviousReplaceSchemaSqlFileMap() {
        return getReplaceSchemaProperties().getMigrationPreviousReplaceSchemaSqlFileMap();
    }

    protected Map<String, File> getMigrationPreviousTakeFinallySqlFileMap() {
        return getReplaceSchemaProperties().getMigrationPreviousTakeFinallySqlFileMap();
    }

    // -----------------------------------------------------
    //                                      History Resource
    //                                      ----------------
    protected String getMigrationHistoryDir() {
        return getReplaceSchemaProperties().getMigrationHistoryDir();
    }

    // -----------------------------------------------------
    //                                       Schema Resource
    //                                       ---------------
    protected String getMigrationAlterCheckDiffMapFile() {
        return getReplaceSchemaProperties().getMigrationAlterCheckDiffMapFile();
    }

    protected String getMigrationAlterCheckResultFileName() {
        return getReplaceSchemaProperties().getMigrationAlterCheckResultFileName();
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

    protected String getMigrationSavePreviousMark() {
        return getReplaceSchemaProperties().getMigrationSavePreviousMark();
    }

    protected String getMigrationPreviousOKMark() {
        return getReplaceSchemaProperties().getMigrationPreviousOKMark();
    }

    protected String getMigrationNextNGMark() {
        return getReplaceSchemaProperties().getMigrationNextNGMark();
    }

    protected String getMigrationAlterNGMark() {
        return getReplaceSchemaProperties().getMigrationAlterNGMark();
    }

    protected String getMigrationPreviousNGMark() {
        return getReplaceSchemaProperties().getMigrationPreviousNGMark();
    }

    public String getMigrationCheckedAlterMarkBasicName() {
        return getReplaceSchemaProperties().getMigrationCheckedAlterMarkBasicName();
    }

    public String getMigrationSkippedAlterMarkBasicName() {
        return getReplaceSchemaProperties().getMigrationSkippedAlterMarkBasicName();
    }

    public String getMigrationFinishedAlterMarkBasicName() {
        return getReplaceSchemaProperties().getMigrationFinishedAlterMarkBasicName();
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String replaceString(String text, String fromText, String toText) {
        return DfStringUtil.replace(text, fromText, toText);
    }

    protected String resolvePath(File file) {
        return Srl.replace(file.getPath(), "\\", "/");
    }

    // ===================================================================================
    //                                                                              Option
    //                                                                              ======
    public void useDraftSpace() {
        _useDraftSpace = true;
    }
}
