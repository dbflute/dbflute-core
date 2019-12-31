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
package org.dbflute.task.manage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.dbflute.infra.dfprop.DfPublicProperties;
import org.dbflute.properties.DfInfraProperties;
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
public class DfIntroTask extends DfAbstractTask {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger _log = LoggerFactory.getLogger(DfIntroTask.class);
    protected static final String DEFAULT_LOCATION_PATH = "../dbflute-intro.jar";

    // ===================================================================================
    //                                                                           Beginning
    //                                                                           =========
    @Override
    protected boolean begin() {
        _log.info("+------------------------------------------+");
        _log.info("|                                          |");
        _log.info("|                   Intro                  |");
        _log.info("|                                          |");
        _log.info("+------------------------------------------+");
        DfDBFluteTaskStatus.getInstance().setTaskType(TaskType.Intro);
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
        final String locationPath = findLocationPath();
        final File jarFile = new File(locationPath);
        if (jarFile.exists()) { // basically no way because of script control
            if (determineExistingIntroLatestVersion(jarFile, dfprop)) {
                _log.info("*The jar file of DBFlute Intro already exists: " + locationPath);
                return;
            } else {
                _log.info("*The existing DBFlute Intro is old version so override it: " + locationPath);
                jarFile.delete();
            }
        }
        final String downloadUrl = findDownloadUrl(dfprop);
        _log.info("...Downloading DBFlute Intro to " + locationPath);
        _log.info("    from " + downloadUrl);
        download(downloadUrl, locationPath);
        refreshResources();
    }

    protected String findLocationPath() {
        final String specified = getInfraProperties().getDBFluteIntroLocationPath();
        return specified != null ? specified : DEFAULT_LOCATION_PATH;
    }

    protected boolean determineExistingIntroLatestVersion(File jarFile, DfPublicProperties dfprop) {
        final String existingJarVersion;
        try {
            existingJarVersion = extractIntroJarVersion(jarFile);
        } catch (RuntimeException e) { // unknown, continue because of sub process
            _log.info("*Failed to extract the existing intro version from jar file: " + jarFile);
            return false;
        }
        if (existingJarVersion == null) { // unknown
            return false;
        }
        // over version treated as latest just in case (might forget to update public.properties)
        return existingJarVersion.compareTo(getIntroLatestVersion(dfprop)) >= 0;
    }

    protected String findDownloadUrl(DfPublicProperties dfprop) {
        final DfInfraProperties prop = getInfraProperties();
        final String specified = prop.getDBFluteIntroDownloadUrl();
        if (specified != null) {
            return specified;
        }
        // defined download URL may not contain version but just in case (for future)
        return getIntroDownloadUrl(dfprop, dfprop.getIntroLatestVersion());
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    protected String getIntroLatestVersion(DfPublicProperties dfprop) {
        final String latestVersion = dfprop.getIntroLatestVersion();
        if (latestVersion == null) {
            String msg = "Not found the latest version for DBFlute Intro in public properties: " + dfprop;
            throw new IllegalStateException(msg);
        }
        return latestVersion;
    }

    protected String getIntroDownloadUrl(DfPublicProperties dfprop, final String latestVersion) {
        final String downloadUrl = dfprop.getIntroDownloadUrl(latestVersion);
        if (downloadUrl == null) {
            String msg = "Not found the download URL for DBFlute Intro in public properties: " + dfprop;
            throw new IllegalStateException(msg);
        }
        return downloadUrl;
    }

    protected String extractIntroJarVersion(File jarFile) { // null allowed: when not found
        ZipInputStream ins = null;
        try {
            ins = new ZipInputStream(new FileInputStream(jarFile));
            final String manifestText = readManifestText(ins);
            final List<String> lineList = Srl.splitListTrimmed(manifestText, "\n");
            final String versionKey = "Implementation-Version:";
            String introVersion = null;
            for (String line : lineList) {
                if (line.startsWith(versionKey)) {
                    introVersion = Srl.substringFirstRear(line, versionKey).trim();
                }
            }
            return introVersion;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read intro version: jarFile=" + jarFile, e);
        } finally {
            if (ins != null) {
                try {
                    ins.close();
                } catch (IOException ignored) {}
            }
        }
    }

    protected String readManifestText(ZipInputStream ins) throws IOException, UnsupportedEncodingException {
        ZipEntry entry = null;
        while ((entry = ins.getNextEntry()) != null) {
            if (entry.getName().endsWith("MANIFEST.MF")) {
                break;
            }
            ins.closeEntry();
        }
        final byte[] buf = new byte[4096]; // enough to get intro version
        ins.read(buf);
        final String manifestText = new String(buf, "UTF-8");
        ins.closeEntry();
        return manifestText;
    }
}
