/*
 * Copyright 2014-2019 the original author or authors.
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tools.ant.util.FileUtils;
import org.dbflute.exception.DfAlterCheckDataSourceNotFoundException;
import org.dbflute.exception.DfAlterCheckRollbackSchemaFailureException;
import org.dbflute.helper.io.compress.DfZipArchiver;
import org.dbflute.helper.jdbc.context.DfSchemaSource;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.logic.replaceschema.process.DfAbstractRepsProcess;
import org.dbflute.system.DBFluteSystem;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.DfStringUtil;
import org.dbflute.util.DfTypeUtil;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 0.9.8.3 (2011/04/29 Friday)
 */
public abstract class DfAbstractDBMigrationProcess extends DfAbstractRepsProcess {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The logger instance for this class. (NotNull) */
    private static final Logger _log = LoggerFactory.getLogger(DfAbstractDBMigrationProcess.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                        Basic Resource
    //                                        --------------
    protected final DfSchemaSource _dataSource;
    protected final CoreProcessPlayer _coreProcessPlayer;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    protected DfAbstractDBMigrationProcess(DfSchemaSource dataSource, CoreProcessPlayer coreProcessPlayer) {
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

    public static interface CoreProcessPlayer {

        void playNext(String sqlRootDir);

        void playPrevious(String sqlRootDir);
    }

    // ===================================================================================
    //                                                                   AlterDDL Resource
    //                                                                   =================
    // -----------------------------------------------------
    //                                     Checked-Alter ZIP
    //                                     -----------------
    protected String buildCheckedAlterZipName(String previousDate) {
        return getMigrationCheckedAlterMarkBasicName() + "-to-" + previousDate + ".zip";
    }

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

    // latest-checked only used at old DBFlute so parallel work problem causes, however renewal can resolve it
    //protected List<File> findCheckedAlterZipList(String previousDate) {
    //    final List<File> fisrtLevelDirList = findHistoryFirstLevelDirList();
    //    final List<File> checkedAlterZipList = DfCollectionUtil.newArrayList();
    //    for (File fisrtLevelDir : fisrtLevelDirList) {
    //        final List<File> secondLevelDirList = findHistorySecondLevelDirList(fisrtLevelDir);
    //        for (File secondLevelDir : secondLevelDirList) {
    //            final File checkedAlter = findCheckedAlterZip(secondLevelDir, previousDate);
    //            if (checkedAlter != null) {
    //                checkedAlterZipList.add(checkedAlter);
    //            }
    //        }
    //    }
    //    return checkedAlterZipList;
    //}

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

    // -----------------------------------------------------
    //                                    First/Second Level
    //                                    ------------------
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
    //                                                                  PreviousDB Command
    //                                                                  ==================
    // -----------------------------------------------------
    //                                       Previous Schema
    //                                       ---------------
    protected void playPreviousSchema() {
        _coreProcessPlayer.playPrevious(getMigrationPreviousDir());
    }

    // -----------------------------------------------------
    //                                       PreviousNG Mark
    //                                       ---------------
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

    // -----------------------------------------------------
    //                                         Previous Date
    //                                         -------------
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
    //                                                                 PreviousDB Resource
    //                                                                 ===================
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

    // -----------------------------------------------------
    //                                     Compress Resource
    //                                     -----------------
    protected File findLatestPreviousZip() {
        final List<File> previousZipList = findPreviousZipList();
        if (previousZipList.isEmpty()) {
            return null;
        }
        return findLatestNameFile(previousZipList);
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

    // ===================================================================================
    //                                                                    Control Resource
    //                                                                    ================
    // -----------------------------------------------------
    //                                          Current Date
    //                                          ------------
    protected String currentDate() {
        return DfTypeUtil.toString(DBFluteSystem.currentDate(), "yyyy/MM/dd HH:mm:ss");
    }

    // -----------------------------------------------------
    //                                           Delete Mark
    //                                           -----------
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

    // -----------------------------------------------------
    //                                        Hierarchy File
    //                                        --------------
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

    // -----------------------------------------------------
    //                                        File Operation
    //                                        --------------
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
                } catch (IOException ignored) {}
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
    //                                                                      General Helper
    //                                                                      ==============
    protected String replaceString(String text, String fromText, String toText) {
        return DfStringUtil.replace(text, fromText, toText);
    }

    protected String resolvePath(File file) {
        return Srl.replace(file.getPath(), "\\", "/");
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
}
