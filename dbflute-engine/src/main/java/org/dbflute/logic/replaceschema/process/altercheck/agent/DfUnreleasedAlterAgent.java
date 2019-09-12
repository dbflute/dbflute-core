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
import java.io.FileFilter;
import java.util.Date;
import java.util.List;

import org.dbflute.DfBuildProperties;
import org.dbflute.helper.io.compress.DfZipArchiver;
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

    // ===================================================================================
    //                                                            Restore Unreleased Alter
    //                                                            ========================
    public void restoreUnreleasedAlterSql() { // for AlterCheck first
        final String previousDate = _previousDBAgent.findLatestPreviousDate();
        if (previousDate == null) {
            return;
        }
        final File checkedAlterZip = _historyZipAgent.findLatestCheckedAlterZip(previousDate);
        if (checkedAlterZip == null) {
            return;
        }
        _log.info("...Restoring the latest checked-alter: " + resolvePath(checkedAlterZip));
        final DfZipArchiver archiver = new DfZipArchiver(checkedAlterZip);
        archiver.extract(new File(getMigrationAlterDirectory()), file -> {
            _log.info("  " + resolvePath(file));
            return true;
        });
    }

    // ===================================================================================
    //                                                               Save Unreleased Alter
    //                                                               =====================
    public void saveUnreleasedAlterSql(DfAlterCheckFinalInfo finalInfo) { // for AlterCheck sucess
        final String previousDate = _previousDBAgent.findLatestPreviousDate(); // may be null
        final List<File> alterSqlFileList = finalInfo.getAlterSqlFileList();
        if (alterSqlFileList == null) { // just in case
            return;
        }
        final String checkedAlterZipName;
        if (previousDate != null) {
            checkedAlterZipName = _historyZipAgent.buildCheckedAlterZipName(previousDate);
        } else {
            checkedAlterZipName = getMigrationCheckedAlterMarkBasicName() + ".zip";
        }
        skipSameStoryHistory(checkedAlterZipName, previousDate);
        compressCheckedAlterZip(checkedAlterZipName);
        deleteAlterSqlFile(alterSqlFileList);
    }

    protected void skipSameStoryHistory(String checkedAlterZipName, String previousDate) {
        if (previousDate == null) { // basically for compatible
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

    protected String getHistoryCurrentDir() {
        final String historyDir = getMigrationHistoryDir();
        final Date currentDate = new Date();
        final String middleDir = DfTypeUtil.toString(currentDate, "yyyyMM");
        _alterControlAgent.mkdirsDirIfNotExists(historyDir + "/" + middleDir);
        // e.g. history/201104/20110429_2247
        final String yyyyMMddHHmm = DfTypeUtil.toString(currentDate, "yyyyMMdd_HHmm");
        final String currentDir = historyDir + "/" + middleDir + "/" + yyyyMMddHHmm;
        _alterControlAgent.mkdirsDirIfNotExists(currentDir);
        return currentDir;
    }

    protected void deleteAlterSqlFile(final List<File> alterSqlFileList) {
        for (File alterSqlFile : alterSqlFileList) {
            _alterControlAgent.deleteFile(alterSqlFile, "...Deleting the executed alterSqlFile");
        }
    }

    // ===================================================================================
    //                                                               Finish Released Alter
    //                                                               =====================
    public void finishReleasedAlterSql() { // for SavePrevious
        final String previousDate = _previousDBAgent.findLatestPreviousDate();
        if (previousDate == null) {
            return;
        }
        final File checkedAlterZip = _historyZipAgent.findLatestCheckedAlterZip(previousDate);
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

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String resolvePath(File file) {
        return Srl.replace(file.getPath(), "\\", "/");
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected static DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    protected static DfReplaceSchemaProperties getReplaceSchemaProperties() {
        return getProperties().getReplaceSchemaProperties();
    }

    // -----------------------------------------------------
    //                                          Alter Schema
    //                                          ------------
    protected String getMigrationAlterDirectory() {
        return getReplaceSchemaProperties().getMigrationAlterDirectory();
    }

    // -----------------------------------------------------
    //                                      History Resource
    //                                      ----------------
    protected String getMigrationHistoryDir() {
        return getReplaceSchemaProperties().getMigrationHistoryDir();
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
