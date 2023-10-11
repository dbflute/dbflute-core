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
package org.dbflute.logic.replaceschema.process.altercheck.agent;

import java.io.File;
import java.util.List;

import org.dbflute.DfBuildProperties;
import org.dbflute.properties.DfReplaceSchemaProperties;
import org.dbflute.util.DfCollectionUtil;

/**
 * @author jflute
 * @since 1.2.1 (2019/09/12 Thursday at sky-high) from DfAlterCheckProcess
 */
public class DfHistoryZipAgent {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                             Supporter
    //                                             ---------
    protected final DfAlterControlAgent _alterControlAgent;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfHistoryZipAgent() {
        _alterControlAgent = new DfAlterControlAgent();
    }

    // ===================================================================================
    //                                                                   AlterDDL Resource
    //                                                                   =================
    // -----------------------------------------------------
    //                                     Checked-Alter ZIP
    //                                     -----------------
    public String buildCheckedAlterZipName(String previousDate) {
        return doBuildAlterZipName(getMigrationCheckedAlterMarkBasicName(), previousDate);
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

    public File findLatestCheckedAlterZip(String previousDate) { // null allowed (when no checked-alter)
        final List<File> fisrtLevelDateDirList = findHistoryFirstLevelDateDirList();
        final File latestFisrtLevelDateDir = _alterControlAgent.findLatestNameFile(fisrtLevelDateDirList);
        if (latestFisrtLevelDateDir == null) {
            return null;
        }
        final List<File> secondLevelDateDirList = findHistorySecondLevelDateDirList(latestFisrtLevelDateDir);
        final File latestSecondLevelDateDir = _alterControlAgent.findLatestNameFile(secondLevelDateDirList);
        if (latestSecondLevelDateDir == null) {
            return null;
        }
        return doFindCheckedAlterZip(latestSecondLevelDateDir, previousDate);
    }

    protected File doFindCheckedAlterZip(File secondLevelDir, String previousDate) { // null allowed (when not found)
        if (secondLevelDir == null) {
            return null;
        }
        final String checkedAlterZipName = buildCheckedAlterZipName(previousDate);
        final File[] listFiles = secondLevelDir.listFiles((file, name) -> name.equals(checkedAlterZipName));
        if (listFiles == null || listFiles.length == 0) {
            return null;
        }
        return listFiles[0]; // must be only one
    }

    // -----------------------------------------------------
    //                                    Finished-Alter ZIP
    //                                    ------------------
    public String buildFinishedAlterZipName(String previousDate) { // for renewal logic
        return doBuildAlterZipName(getMigrationFinishedAlterMarkBasicName(), previousDate);
    }

    // -----------------------------------------------------
    //                                             Alter ZIP
    //                                             ---------
    protected String doBuildAlterZipName(String basicName, String previousDate) {
        return basicName + "-to-" + previousDate + ".zip";
    }

    // -----------------------------------------------------
    //                                    First/Second Level
    //                                    ------------------
    public List<File> findHistoryFirstLevelDateDirList() { // except unreleased directory
        final File historyDir = new File(getMigrationHistoryDir());
        if (!historyDir.isDirectory() || !historyDir.exists()) {
            return DfCollectionUtil.newArrayList();
        }
        final File[] firstLevelDateDirList = historyDir.listFiles(file -> { // needs to avoid unreleased directory
            return file.isDirectory() && !file.getName().equals(getMigrationHistoryUnreleasedDirPureName());
        });
        if (firstLevelDateDirList == null) {
            return DfCollectionUtil.newArrayList();
        }
        return DfCollectionUtil.newArrayList(firstLevelDateDirList);
    }

    public List<File> findHistorySecondLevelDateDirList(File firstLevelDateDir) {
        if (firstLevelDateDir == null) {
            return DfCollectionUtil.newArrayList();
        }
        final File[] secondLevelDateDirList = firstLevelDateDir.listFiles(file -> file.isDirectory());
        if (secondLevelDateDirList == null) {
            return DfCollectionUtil.newArrayList();
        }
        return DfCollectionUtil.newArrayList(secondLevelDateDirList);
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
    //                                      History Resource
    //                                      ----------------
    protected String getMigrationHistoryDir() {
        return getReplaceSchemaProperties().getMigrationHistoryDir();
    }

    protected String getMigrationHistoryUnreleasedDirPureName() {
        return getReplaceSchemaProperties().getMigrationHistoryUnreleasedDirPureName();
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
}
