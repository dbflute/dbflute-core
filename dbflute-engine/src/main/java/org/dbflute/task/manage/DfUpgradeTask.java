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
package org.dbflute.task.manage;

import java.io.File;
import java.io.FileFilter;

import org.dbflute.helper.filesystem.FileTextIO;
import org.dbflute.helper.filesystem.FileTextLineFilter;
import org.dbflute.helper.io.compress.DfZipArchiver;
import org.dbflute.infra.dfprop.DfPublicProperties;
import org.dbflute.task.DfDBFluteTaskStatus;
import org.dbflute.task.DfDBFluteTaskStatus.TaskType;
import org.dbflute.task.bs.DfAbstractTask;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 1.0.5K (2014/08/15 Friday)
 */
public class DfUpgradeTask extends DfAbstractTask {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger _log = LoggerFactory.getLogger(DfUpgradeTask.class);
    protected static final String DEFAULT_LOCATION_PATH = "../mydbflute/";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String _version; // null allowed (latest version if null)
    protected String _actualVersion; // basically for final info

    // ===================================================================================
    //                                                                           Beginning
    //                                                                           =========
    @Override
    protected boolean begin() {
        _log.info("+------------------------------------------+");
        _log.info("|                                          |");
        _log.info("|                 Upgrade                  |");
        _log.info("|                                          |");
        _log.info("+------------------------------------------+");
        DfDBFluteTaskStatus.getInstance().setTaskType(TaskType.Upgrade);
        return true;
    }

    // ===================================================================================
    //                                                                          DataSource
    //                                                                          ==========
    @Override
    protected boolean isUseDataSource() {
        return false;
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    @Override
    protected void doExecute() {
        final DfPublicProperties dfprop = preparePublicProperties();
        final String upgradeVersion = findUpgradeVersion(dfprop);
        final String archivePath = findArchivePath(dfprop);
        downloadEngine(dfprop, upgradeVersion, archivePath);
        extractEngine(upgradeVersion, archivePath);
        deleteDownloadedArchive(archivePath);
        replaceDBFluteClientReference(upgradeVersion);
        refreshResources();
        _actualVersion = upgradeVersion;
    }

    // ===================================================================================
    //                                                                        Archive Path
    //                                                                        ============
    protected String findArchivePath(DfPublicProperties dfprop) {
        final String mydbfluteDir = getMyDBFluteDir();
        if (mydbfluteDir == null) { // basically no way (just in case)
            String msg = "Not found the mydbflute directory: DBFLUTE_HOME=" + getDBFluteHome();
            throw new IllegalStateException(msg);
        }
        final String version = findUpgradeVersion(dfprop);
        return mydbfluteDir + "/dbflute-" + version + ".zip";
    }

    // ===================================================================================
    //                                                                     Download Engine
    //                                                                     ===============
    protected void downloadEngine(DfPublicProperties dfprop, String upgradeVersion, String archivePath) {
        final String downloadUrl = findDownloadUrl(dfprop, upgradeVersion);
        checkAlreadyExists(archivePath);
        _log.info("...Downloading DBFlute Engine to " + archivePath);
        _log.info("    from " + downloadUrl);
        download(downloadUrl, archivePath);
    }

    protected String findDownloadUrl(DfPublicProperties dfprop, String upgradeVersion) {
        return dfprop.getDBFluteDownloadUrl(upgradeVersion);
    }

    protected void checkAlreadyExists(String archivePath) {
        if (new File(archivePath).exists()) {
            String msg = "The version already exists: " + archivePath;
            throw new IllegalStateException(msg);
        }
    }

    protected String findUpgradeVersion(DfPublicProperties dfprop) {
        if (isVersionSpecified()) {
            return _version;
        }
        final String latestVersion = dfprop.getDBFluteLatestReleaseVersion();
        if (latestVersion == null) {
            String msg = "Not found the latest version for DBFlute in publicMap.";
            throw new IllegalStateException(msg);
        }
        return latestVersion;
    }

    protected boolean isVersionSpecified() {
        return Srl.is_NotNull_and_NotTrimmedEmpty(_version) && !_version.trim().equalsIgnoreCase("new");
    }

    // ===================================================================================
    //                                                                      Extract Engine
    //                                                                      ==============
    protected void extractEngine(String upgradeVersion, String archivePath) {
        final DfZipArchiver archiver = new DfZipArchiver(new File(archivePath));
        final String mydbfluteDir = getMyDBFluteDir();
        final String baseDir;
        if (determineSameVersionUpgrading(upgradeVersion)) { // patch
            // cannot override engine myself in execution so extract to temporary directory 
            // the current engine will be switched to this patched engine after the task
            // in shell or windows script e.g. _df-upgrade.sh
            baseDir = mydbfluteDir + "/working_patched_dbflute"; // synchronized with script
        } else { // upgrade, normally here
            baseDir = mydbfluteDir + "/dbflute-" + upgradeVersion;
        }
        _log.info("...Extracting zip archive to " + baseDir);
        archiver.extract(new File(baseDir), new FileFilter() {
            public boolean accept(File file) {
                return true;
            }
        });
    }

    protected boolean determineSameVersionUpgrading(String upgradeVersion) {
        final String dbfluteHome = getDBFluteHome(); // basically not null (as unknown if null)
        return dbfluteHome != null && dbfluteHome.contains("dbflute-" + upgradeVersion);
    }

    protected void deleteDownloadedArchive(String archivePath) {
        _log.info("...Deleting zip archive to " + archivePath);
        new File(archivePath).deleteOnExit();
    }

    // ===================================================================================
    //                                                                   Replace Reference
    //                                                                   =================
    protected void replaceDBFluteClientReference(final String upgradeVersion) {
        final FileTextIO textIO = new FileTextIO().encodeAsUTF8();
        doReplaceProjectBat(upgradeVersion, textIO);
        doReplaceProjectSh(upgradeVersion, textIO);
    }

    protected void doReplaceProjectBat(final String upgradeVersion, FileTextIO textIO) {
        doReplaceVersionScript(upgradeVersion, textIO, "./_project.bat", "\\dbflute-");
    }

    protected void doReplaceProjectSh(final String upgradeVersion, final FileTextIO textIO) {
        doReplaceVersionScript(upgradeVersion, textIO, "./_project.sh", "/dbflute-");
    }

    protected void doReplaceVersionScript(final String upgradeVersion, FileTextIO textIO, String scriptPath, final String versionKeyword) {
        final File versionScript = new File(scriptPath);
        if (versionScript.exists()) { // basically true (just in case)
            _log.info("...Replacing version script: " + scriptPath);
            textIO.rewriteFilteringLine(scriptPath, new FileTextLineFilter() {
                public String filter(String line) {
                    return filterVersionLine(upgradeVersion, line, versionKeyword);
                }
            });
        }
    }

    protected String filterVersionLine(String upgradeVersion, String line, String versionKeyword) {
        if (line.contains("DBFLUTE_HOME") && line.contains(versionKeyword)) {
            line = Srl.substringLastFront(line, versionKeyword) + versionKeyword + upgradeVersion;
        }
        return line;
    }

    // ===================================================================================
    //                                                                          Final Info
    //                                                                          ==========
    @Override
    protected void showFinalMessage(long before, long after, boolean abort) {
        super.showFinalMessage(before, after, abort);
        waitAfterFinalMessage();
    }

    protected void waitAfterFinalMessage() {
        try {
            final long waitMillis = getInfraProperties().getDBFluteUpgradeMessageWaitMillis();
            Thread.sleep(waitMillis); // to get runtime version notification looked at
        } catch (InterruptedException ignored) {}
    }

    @Override
    protected String getFinalInformation() {
        return buildFinalMessage();
    }

    protected String buildFinalMessage() {
        final StringBuilder sb = new StringBuilder();
        sb.append(ln()).append("  *you need to upgrade also DBFlute Runtime");
        sb.append(ln()).append("");
        sb.append(ln()).append("    e.g. pom.xml:");
        sb.append(ln()).append("     <dbflute.version>").append(_actualVersion).append("</dbflute.version>");
        sb.append(ln()).append("");
        return sb.toString();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setVersion(String version) {
        if (Srl.is_Null_or_TrimmedEmpty(version)) {
            return;
        }
        if (version.equals("${dfver}")) {
            return;
        }
        _version = version;
    }
}
