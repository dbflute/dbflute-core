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
package org.dbflute.logic.replaceschema.process.altercheck.agent;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.tools.ant.util.FileUtils;
import org.dbflute.DfBuildProperties;
import org.dbflute.helper.io.compress.DfZipArchiver;
import org.dbflute.infra.core.logic.DfSchemaResourceFinder;
import org.dbflute.logic.replaceschema.finalinfo.DfAlterCheckFinalInfo;
import org.dbflute.logic.replaceschema.process.altercheck.player.DfAlterCoreProcessPlayer;
import org.dbflute.properties.DfReplaceSchemaProperties;
import org.dbflute.util.DfTypeUtil;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 1.2.1 (2019/09/12 Thursday at sky-high) from DfAlterCheckProcess
 */
public class DfUnreleasedAlterAgent {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The logger instance for this class. (NotNull) */
    private static final Logger _log = LoggerFactory.getLogger(DfUnreleasedAlterAgent.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                             Supporter
    //                                             ---------
    protected final DfAlterControlAgent _alterControlAgent;
    protected final DfHistoryZipAgent _historyZipAgent;
    protected final DfPreviousDBAgent _previousDBAgent;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfUnreleasedAlterAgent(DfAlterCoreProcessPlayer coreProcessPlayer) {
        _alterControlAgent = new DfAlterControlAgent();
        _historyZipAgent = new DfHistoryZipAgent();
        _previousDBAgent = new DfPreviousDBAgent(coreProcessPlayer);
    }

    // e.g. directory structure for renewal2019
    //  |-playsql
    //  |  |
    //  |  |-migration
    //  |  |  |
    //  |  |  |-alter
    //  |  |  |
    //  |  |  |-history
    //  |  |  |  |-[date directory]
    //  |  |  |  |  |-[date-time directory] e.g. 20190415_0123
    //  |  |  |  |  |   |-finished-alter-...zip
    //  |  |  |  |
    //  |  |  |  |-unreleased-checked
    //  |  |  |  |  |-DONT_EDIT_HERE.dfmark
    //  |  |  |  |  |-for-previous-20190712-2222.dfmark
    //  |  |  |  |  |-READONLY_alter-schema-ABC001.sql
    //  |  |  |  |  |-READONLY_alter-schema-ABC002.sql
    //  |  |  |
    //  |  |  |-previous
    //  |  |  |  |-previous-20190712-2222.zip

    // ===================================================================================
    //                                                            Restore Unreleased Alter
    //                                                            ========================
    public void restoreUnreleasedAlterSql() { // for AlterCheck first
        doRestoreAlterFromOldStyleZip(); // old style, ZIP operation is kept for compatible
        doRestoreAlterFromUnreleasedDir(); // renewal2019 for parallel work
    }

    // -----------------------------------------------------
    //                                             Old Style
    //                                             ---------
    protected void doRestoreAlterFromOldStyleZip() {
        final String previousDate = _previousDBAgent.findLatestPreviousDate();
        if (previousDate == null) { // not found the previous (operation mistake)
            return;
        }
        final File checkedAlterZip = _historyZipAgent.findLatestCheckedAlterZip(previousDate);
        if (checkedAlterZip != null) { // second or more checks in the phase
            _log.info("...Restoring latest checked-alter: " + resolvePath(checkedAlterZip));
            final DfZipArchiver archiver = new DfZipArchiver(checkedAlterZip);
            archiver.extract(new File(getMigrationAlterDirectory()), file -> {
                _log.info("  " + resolvePath(file));
                return true;
            });
        }
    }

    // -----------------------------------------------------
    //                                           Renewal2019
    //                                           -----------
    protected void doRestoreAlterFromUnreleasedDir() {
        final File unreleasedDir = new File(getMigrationHistoryUnreleasedDir());
        if (!unreleasedDir.exists()) { // e.g. first time
            return;
        }
        final List<File> unreleasedSqlFileList = findUnreleasedSqlFileList();
        _log.info("...Restoring AlterDDL from unreleased directory to alter directory: " + unreleasedSqlFileList.size());
        for (File unreleasedSqlFile : unreleasedSqlFileList) {
            final String editingName = Srl.removePrefix(unreleasedSqlFile.getName(), getMigrationHistoryUnreleasedFilePrefix());
            final File editingSqlFile = new File(getMigrationAlterDirectory() + "/" + editingName);
            if (editingSqlFile.exists()) { // e.g. same-name file in old style checked-ZIP, rare case so just in case
                editingSqlFile.delete(); // overwrite if exists
            }
            _log.info("  to " + resolvePath(editingSqlFile));
            _alterControlAgent.copyFile(unreleasedSqlFile, editingSqlFile);
        }
    }

    // ===================================================================================
    //                                                            Save Alter as Unreleased
    //                                                            ========================
    public void saveAlterAsUnreleased(DfAlterCheckFinalInfo finalInfo) { // for AlterCheck success
        final List<File> alterSqlFileList = finalInfo.getAlterSqlFileList();
        if (alterSqlFileList == null) { // basically no way here, just in case
            return;
        }
        final String previousDate = _previousDBAgent.findLatestPreviousDate(); // not null here
        if (isCompatibleMigrationHistoryCheckedZip()) {
            doSaveAlterInCheckedZip(previousDate); // old style
        } else {
            doSaveAlterInUnreleasedDir(previousDate, alterSqlFileList); // renewal2019 for parallel work 
        }
        deleteAlterSqlFile(alterSqlFileList);
    }

    // -----------------------------------------------------
    //                                             Old Style
    //                                             ---------
    // for maintenance, removed at future
    protected void doSaveAlterInCheckedZip(String previousDate) {
        final String checkedAlterZipName;
        if (previousDate != null) { // basically true, already SavePrevious
            checkedAlterZipName = _historyZipAgent.buildCheckedAlterZipName(previousDate);
        } else { // no way here, just in case
            checkedAlterZipName = getMigrationCheckedAlterMarkBasicName() + ".zip";
        }
        skipSameStoryHistory(checkedAlterZipName, previousDate); // rename old checked-ZIP files as skipped
        compressCheckedAlterZip(checkedAlterZipName);
    }

    protected void skipSameStoryHistory(String checkedAlterZipName, String previousDate) {
        if (previousDate == null) { // basically no way, for compatible
            return;
        }
        final List<File> firstLevelDirList = _historyZipAgent.findHistoryFirstLevelDirList();
        final String markBasicName = getMigrationSkippedAlterMarkBasicName();
        for (File firstLevelDir : firstLevelDirList) {
            final List<File> secondLevelDirList = _historyZipAgent.findHistorySecondLevelDirList(firstLevelDir);
            for (File secondLevelDir : secondLevelDirList) {
                final String basePath = resolvePath(secondLevelDir);
                final File successStoryFile = new File(basePath + "/" + checkedAlterZipName);
                if (successStoryFile.exists()) {
                    _log.info("...Skipping same story: " + basePath);
                    final String skippedZipName = basePath + "/" + markBasicName + "-to-" + previousDate + ".zip";
                    successStoryFile.renameTo(new File(skippedZipName));
                }
            }
        }
    }

    protected void compressCheckedAlterZip(String checkedAlterZipName) {
        final String checkedAlterZipPath = getHistoryCurrentDir() + "/" + checkedAlterZipName;
        _log.info("...Compressing already-checked AlterDDL to " + checkedAlterZipPath);
        final DfZipArchiver archiver = new DfZipArchiver(new File(checkedAlterZipPath));
        archiver.suppressCompressSubDir();
        archiver.compress(new File(getMigrationAlterDirectory()), file -> {
            _log.info("  " + resolvePath(file));
            return true;
        });
    }

    // -----------------------------------------------------
    //                                           Renewal2019
    //                                           -----------
    protected void doSaveAlterInUnreleasedDir(String previousDate, List<File> alterSqlFileList) {
        deleteExistingUnreleasedAlter();
        moveAlterSqlFileToUnreleasedDir(alterSqlFileList);
        makeUnreleasedNoticeMark();
        markUnreleasedPreviousMark(previousDate);
    }

    protected void deleteExistingUnreleasedAlter() {
        final List<File> unreleasedSqlFileList = findUnreleasedSqlFileList();
        _log.info("...Deleting existing unreleased AlterDDL to replace: " + unreleasedSqlFileList.size());
        for (File existingUnreleasedSqlFile : unreleasedSqlFileList) {
            if (existingUnreleasedSqlFile.exists()) { // just in case
                existingUnreleasedSqlFile.delete();
            }
        }
    }

    protected void moveAlterSqlFileToUnreleasedDir(List<File> alterSqlFileList) {
        _log.info("...Moving alter SQL files to unreleased directory: " + alterSqlFileList.size());
        final String unreleasedDir = getMigrationHistoryUnreleasedDir();
        for (File alterSqlFile : alterSqlFileList) {
            final File unreleasedSqlFile = new File(unreleasedDir + "/" + alterSqlFile.getName());
            _log.info("  to " + resolvePath(unreleasedSqlFile));
            alterSqlFile.renameTo(unreleasedSqlFile);
        }
    }

    protected void makeUnreleasedNoticeMark() {
        final File noticeMark = new File(getMigrationHistoryUnreleasedNoticeMark());
        if (noticeMark.exists()) {
            noticeMark.delete(); // to overwrite
        }
        _log.info("...Marking notice in unreleased directory: " + resolvePath(noticeMark));
        try {
            _alterControlAgent.writeControlNotice(noticeMark, "Read the AlterCheck document.");
        } catch (IOException e) {
            String msg = "Failed to write notice mark file in unreleased directory: " + resolvePath(noticeMark);
            throw new IllegalStateException(msg, e);
        }
    }

    protected void markUnreleasedPreviousMark(String previousDate) {
        final File previousMark = new File(getMigrationHistoryUnreleasedPreviousMark(previousDate));
        if (previousMark.exists()) {
            previousMark.delete();
        }
        _log.info("...Marking previous in unreleased directory: " + resolvePath(previousMark));
        try {
            _alterControlAgent.writeControlNotice(previousMark, "The AlterDDLs are for the PreviousDB.");
        } catch (IOException e) {
            String msg = "Failed to write previous mark file in unreleased directory: " + resolvePath(previousMark);
            throw new IllegalStateException(msg, e);
        }
    }

    // -----------------------------------------------------
    //                                               Closing
    //                                               -------
    protected void deleteAlterSqlFile(List<File> alterSqlFileList) {
        _log.info("...Deleting AlterDDL in alter directory: " + alterSqlFileList.size());
        for (File alterSqlFile : alterSqlFileList) {
            if (alterSqlFile.exists()) { // just in case
                alterSqlFile.delete();
            }
        }
    }

    // ===================================================================================
    //                                                               Finish Released Alter
    //                                                               =====================
    public void finishReleasedAlterSql() { // for SavePrevious
        final String previousDate = _previousDBAgent.findLatestPreviousDate();
        if (previousDate == null) { // not found the previous (operation mistake)
            return;
        }
        doFinishCheckedZipToFinishedZip(previousDate); // old style, ZIP operation is kept for compatible
        doFinishUnreleasedDirAsReleased(previousDate); // renewal2019 for parallel work
    }

    // -----------------------------------------------------
    //                                             Old Style
    //                                             ---------
    protected void doFinishCheckedZipToFinishedZip(String previousDate) {
        final File checkedAlterZip = _historyZipAgent.findLatestCheckedAlterZip(previousDate);
        if (checkedAlterZip != null) {
            final String checkedAlterMarkBasicName = getMigrationCheckedAlterMarkBasicName();
            final String finishedAlterMarkBasicName = getMigrationFinishedAlterMarkBasicName();
            final String path = resolvePath(checkedAlterZip);
            final String baseDir = Srl.substringLastFront(path, "/");
            final String pureName = Srl.substringLastRear(path, "/");
            final String renamedName = Srl.replace(pureName, checkedAlterMarkBasicName, finishedAlterMarkBasicName);
            final File renamedFile = new File(baseDir + "/" + renamedName);
            _log.info("...Finishing previous history (renamed to): " + resolvePath(renamedFile));
            checkedAlterZip.renameTo(renamedFile);
        }
    }

    // -----------------------------------------------------
    //                                           Renewal2019
    //                                           -----------
    protected void doFinishUnreleasedDirAsReleased(String previousDate) {
        final List<File> unreleasedSqlFileList = findUnreleasedSqlFileList();
        if (unreleasedSqlFileList.isEmpty()) { // e.g. DBFlute upgraded but not AlterCheck yet or nonsense SavePrevious
            return;
        }
        final String finishedAlterZipName = _historyZipAgent.buildFinishedAlterZipName(previousDate);
        compressFinishedAlterAsZip(finishedAlterZipName, unreleasedSqlFileList);
        deleteFinishedUnreleasedSqlFile(unreleasedSqlFileList);
        deleteUnreleasedPreviousMark(previousDate);
    }

    protected void compressFinishedAlterAsZip(String finishedAlterZipName, List<File> unreleasedSqlFileList) {
        final String checkedAlterZipPath = getHistoryCurrentDir() + "/" + finishedAlterZipName;
        _log.info("...Compressing already-released AlterDDL to " + checkedAlterZipPath);
        final DfZipArchiver archiver = new DfZipArchiver(new File(checkedAlterZipPath));
        archiver.suppressCompressSubDir();
        final Set<File> unreleasedSqlFileSet = new HashSet<>(unreleasedSqlFileList);
        archiver.compress(new File(getMigrationHistoryUnreleasedDir()), file -> {
            if (unreleasedSqlFileSet.contains(file)) { // as same-path instance
                _log.info("  " + resolvePath(file));
                return true;
            } else {
                return false;
            }
        });
    }

    protected void deleteFinishedUnreleasedSqlFile(List<File> unreleasedSqlFileList) {
        _log.info("...Deleting already-released AlterDDL in unreleased directory: " + unreleasedSqlFileList.size());
        for (File unreleasedSqlFile : unreleasedSqlFileList) {
            if (unreleasedSqlFile.exists()) { // just in case
                unreleasedSqlFile.delete();
            }
        }
    }

    protected void deleteUnreleasedPreviousMark(String previousDate) {
        final File previousMark = new File(getMigrationHistoryUnreleasedPreviousMark(previousDate));
        _alterControlAgent.deleteControlFile(previousMark, "...Deleting already-released previous mark");
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    // -----------------------------------------------------
    //                                   Unreleased SQL File
    //                                   -------------------
    protected List<File> findUnreleasedSqlFileList() {
        final DfSchemaResourceFinder resourceFinder = createMigrationUnreleasedAlterSqlFileFinder();
        return resourceFinder.findResourceFileList(getMigrationHistoryUnreleasedDir());
    }

    // -----------------------------------------------------
    //                                       Current History
    //                                       ---------------
    protected String getHistoryCurrentDir() {
        final String historyDir = getMigrationHistoryDir();
        final Date currentDate = _alterControlAgent.currentDate();
        final String middleDir = DfTypeUtil.toString(currentDate, "yyyyMM");
        _alterControlAgent.mkdirsDirIfNotExists(historyDir + "/" + middleDir);

        // e.g. history/201104/20110429_2247
        final String yyyyMMddHHmm = DfTypeUtil.toString(currentDate, "yyyyMMdd_HHmm");
        final String currentDir = historyDir + "/" + middleDir + "/" + yyyyMMddHHmm;
        _alterControlAgent.mkdirsDirIfNotExists(currentDir);
        return currentDir;
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String resolvePath(File file) {
        return Srl.replace(file.getPath(), "\\", "/");
    }

    public void copyFile(File src, File dest) {
        try {
            FileUtils.getFileUtils().copyFile(src, dest);
        } catch (IOException e) {
            String msg = "Failed to copy file: " + src + " to " + dest;
            throw new IllegalStateException(msg, e);
        }
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    protected DfReplaceSchemaProperties getReplaceSchemaProperties() {
        return getProperties().getReplaceSchemaProperties();
    }

    // -----------------------------------------------------
    //                                          Alter Finder
    //                                          ------------
    public DfSchemaResourceFinder createMigrationUnreleasedAlterSqlFileFinder() {
        return getReplaceSchemaProperties().createMigrationUnreleasedAlterSqlFileFinder();
    }

    // -----------------------------------------------------
    //                                          Alter Schema
    //                                          ------------
    protected String getMigrationAlterDirectory() {
        return getReplaceSchemaProperties().getMigrationAlterDirectory();
    }

    public String getMigrationAlterSchemaSqlTitle() {
        return getReplaceSchemaProperties().getMigrationAlterSchemaSqlTitle();
    }

    // -----------------------------------------------------
    //                                      History Resource
    //                                      ----------------
    protected String getMigrationHistoryDir() {
        return getReplaceSchemaProperties().getMigrationHistoryDir();
    }

    protected String getMigrationHistoryUnreleasedDir() {
        return getReplaceSchemaProperties().getMigrationHistoryUnreleasedDir();
    }

    public String getMigrationHistoryUnreleasedFilePrefix() {
        return getReplaceSchemaProperties().getMigrationHistoryUnreleasedFilePrefix();
    }

    public String getMigrationHistoryUnreleasedNoticeMark() {
        return getReplaceSchemaProperties().getMigrationHistoryUnreleasedNoticeMark();
    }

    public String getMigrationHistoryUnreleasedPreviousMark(String previousDate) {
        return getReplaceSchemaProperties().getMigrationHistoryUnreleasedPreviousMark(previousDate);
    }

    public boolean isCompatibleMigrationHistoryCheckedZip() {
        return getReplaceSchemaProperties().isCompatibleMigrationHistoryCheckedZip();
    }

    // -----------------------------------------------------
    //                                         Mark Resource
    //                                         -------------
    public String getMigrationCheckedAlterMarkBasicName() {
        return getReplaceSchemaProperties().getMigrationCheckedAlterMarkBasicName();
    }

    public String getMigrationFinishedAlterMarkBasicName() {
        return getReplaceSchemaProperties().getMigrationFinishedAlterMarkBasicName();
    }

    public String getMigrationSkippedAlterMarkBasicName() {
        return getReplaceSchemaProperties().getMigrationSkippedAlterMarkBasicName();
    }
}
