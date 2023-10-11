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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dbflute.exception.DfAlterCheckSavePreviousFailureException;
import org.dbflute.exception.DfAlterCheckSavePreviousInvalidStatusException;
import org.dbflute.helper.io.compress.DfZipArchiver;
import org.dbflute.helper.jdbc.context.DfSchemaSource;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.logic.replaceschema.finalinfo.DfAlterCheckFinalInfo;
import org.dbflute.logic.replaceschema.process.altercheck.player.DfAlterCoreProcessPlayer;
import org.dbflute.system.DBFluteSystem;
import org.dbflute.util.DfTypeUtil;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 1.2.1 (2019/09/12 Thursday) from DfAlterCheckProcess
 */
public class DfSavePreviousProcess extends DfAbstractAlterProcess {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The logger instance for this class. (NotNull) */
    private static final Logger _log = LoggerFactory.getLogger(DfSavePreviousProcess.class);

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    protected DfSavePreviousProcess(DfSchemaSource dataSource, DfAlterCoreProcessPlayer coreProcessPlayer) {
        super(dataSource, coreProcessPlayer);
    }

    public static DfSavePreviousProcess createAsMain(DfSchemaSource dataSource, DfAlterCoreProcessPlayer coreProcessPlayer) {
        return new DfSavePreviousProcess(dataSource, coreProcessPlayer);
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
        finalInfo.setResultMessage("Save Previous");
        if (!checkSavePreviousInvalidStatus(finalInfo)) {
            return finalInfo;
        }

        final long before = System.currentTimeMillis();
        _unreleasedAlterAgent.finishReleasedAlterSql();
        deleteExtractedPreviousResource();
        final List<File> copyToFileList = copyToPreviousResource();
        compressPreviousResource();
        finalInfo.setResultMessage(finalInfo.getResultMessage() + ": saved=" + copyToFileList.size() + " file(s)");
        final long after = System.currentTimeMillis();
        finalInfo.setProcessPerformanceMillis(after - before); // except ReplaceSchema process

        if (!checkSavedPreviousResource(finalInfo)) { // ReplaceSchema for previous
            return finalInfo; // failure
        }
        markPreviousOK(copyToFileList);
        deleteSavePreviousMark();
        finalInfo.addDetailMessage("o (all resources saved)");
        return finalInfo;
    }

    // ===================================================================================
    //                                                                        Check Status
    //                                                                        ============
    protected boolean checkSavePreviousInvalidStatus(DfAlterCheckFinalInfo finalInfo) {
        final List<File> alterSqlFileList = findMigrationAlterSqlFileList();
        if (!alterSqlFileList.isEmpty()) {
            setupAlterCheckSavePreviousInvalidStatusException(finalInfo);
        }
        if (finalInfo.isFailure()) {
            finalInfo.addDetailMessage("x (save failure)");
            return false;
        }
        return true;
    }

    protected List<File> findMigrationAlterSqlFileList() {
        return createBasicAlterSqlFileFinder().findResourceFileList(getMigrationAlterDirectory());
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

    // ===================================================================================
    //                                                                   Compress Previous
    //                                                                   =================
    protected void compressPreviousResource() {
        deleteExistingPreviousZip();
        final File previousZip = getCurrentTargetPreviousZip();
        _log.info("...Compressing previous resources to zip: " + resolvePath(previousZip));
        final DfZipArchiver archiver = new DfZipArchiver(previousZip);
        archiver.compress(new File(getMigrationPreviousDir()), file -> {
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
        });
    }

    protected File getCurrentTargetPreviousZip() {
        final String date = DfTypeUtil.toString(DBFluteSystem.currentDate(), "yyyyMMdd-HHmm");
        return new File(getMigrationPreviousDir() + "/previous-" + date + ".zip");
    }

    protected void deleteExistingPreviousZip() {
        final List<File> zipFiles = _previousDBAgent.findPreviousZipList();
        for (File zipFile : zipFiles) {
            zipFile.delete();
        }
    }

    // ===================================================================================
    //                                                                    Copy to Previous
    //                                                                    ================
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
        final List<File> dataFileList = _alterControlAgent.findHierarchyFileList(getSchemaDataDir());
        for (File dataFile : dataFileList) {
            doCopyToPreviousResource(dataFile, previousDir, playSqlDirSymbol, copyToFileList);
        }
        return copyToFileList;
    }

    protected void doCopyToPreviousResource(File mainFile, String previousDir, String playSqlDirSymbol, List<File> copyToFileList) {
        final String relativePath = Srl.substringLastRear(resolvePath(mainFile), playSqlDirSymbol);
        final File copyToFile = new File(previousDir + "/" + relativePath);
        final File copyToDir = new File(Srl.substringLastFront(resolvePath(copyToFile), "/"));
        if (!copyToDir.exists()) {
            copyToDir.mkdirs();
        }
        if (copyToFile.exists()) {
            copyToFile.delete();
        }
        _log.info("...Saving replace-schema file to " + resolvePath(copyToFile));
        _alterControlAgent.copyFile(mainFile, copyToFile);
        copyToFileList.add(copyToFile);
    }

    // ===================================================================================
    //                                                                     Previous Schema
    //                                                                     ===============
    protected boolean checkSavedPreviousResource(DfAlterCheckFinalInfo finalInfo) {
        final boolean unzipped = extractPreviousResource();
        _log.info("...Checking previous resources by replacing");
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

    protected void setupAlterCheckSavePreviousFailureException(DfAlterCheckFinalInfo finalInfo, RuntimeException threwLater) {
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

    // ===================================================================================
    //                                                                          Mark Logic
    //                                                                          ==========
    protected void markPreviousOK(List<File> copyToFileList) {
        final String okMark = getMigrationPreviousOKMark();
        try {
            final File markFile = new File(okMark);
            _log.info("...Marking previous-OK: " + okMark);
            markFile.createNewFile();
            final StringBuilder sb = new StringBuilder();
            sb.append("[Saved Previous Resources]: " + _alterControlAgent.currentDateExp());
            for (File moveToFile : copyToFileList) {
                sb.append(ln()).append(resolvePath(moveToFile));
            }
            sb.append(ln()).append("(").append(copyToFileList.size()).append(" files)");
            sb.append(ln());
            writeControlLogRoad(markFile, sb.toString());
        } catch (IOException e) {
            String msg = "Failed to create a file for previous-OK mark: " + okMark;
            throw new IllegalStateException(msg, e);
        }
    }

    protected void deleteSavePreviousMark() {
        final String mark = getMigrationSavePreviousMark();
        deleteControlFile(new File(mark), "...Deleting save-previous mark");
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    // -----------------------------------------------------
    //                                         ReplaceSchema
    //                                         -------------
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
    //                                     Previous Resource
    //                                     -----------------
    protected String getMigrationPreviousDir() {
        return getReplaceSchemaProperties().getMigrationPreviousDir();
    }

    // -----------------------------------------------------
    //                                         Mark Resource
    //                                         -------------
    protected String getMigrationSavePreviousMark() {
        return getReplaceSchemaProperties().getMigrationSavePreviousMark();
    }

    protected String getMigrationPreviousOKMark() {
        return getReplaceSchemaProperties().getMigrationPreviousOKMark();
    }
}
