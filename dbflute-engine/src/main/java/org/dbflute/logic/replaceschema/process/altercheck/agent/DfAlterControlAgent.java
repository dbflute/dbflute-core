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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.tools.ant.util.FileUtils;
import org.dbflute.DfBuildProperties;
import org.dbflute.properties.DfReplaceSchemaProperties;
import org.dbflute.system.DBFluteSystem;
import org.dbflute.util.DfTypeUtil;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 1.2.1 (2019/09/12 Thursday at sky-high) from DfAlterCheckProcess
 */
public class DfAlterControlAgent {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The logger instance for this class. (NotNull) */
    private static final Logger _log = LoggerFactory.getLogger(DfAlterControlAgent.class);

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfAlterControlAgent() {
    }

    // ===================================================================================
    //                                                                    Control Resource
    //                                                                    ================
    // -----------------------------------------------------
    //                                          Current Date
    //                                          ------------
    public Date currentDate() {
        return DBFluteSystem.currentDate();
    }
    
    public String currentDateExp() {
        return DfTypeUtil.toString(currentDate(), "yyyy/MM/dd HH:mm:ss");
    }

    // -----------------------------------------------------
    //                                           Delete Mark
    //                                           -----------
    public void deleteAllNGMark() {
        deleteNextNGMark();
        deleteAlterNGMark();
        deletePreviousNGMark();
    }

    public void deletePreviousOKMark() {
        final String previousOKMark = getMigrationPreviousOKMark();
        deleteControlFile(new File(previousOKMark), "...Deleting previous-OK mark");
    }

    public void deleteNextNGMark() {
        final String replaceNGMark = getMigrationNextNGMark();
        deleteControlFile(new File(replaceNGMark), "...Deleting next-NG mark");
    }

    public void deleteAlterNGMark() {
        final String alterNGMark = getMigrationAlterNGMark();
        deleteControlFile(new File(alterNGMark), "...Deleting alter-NG mark");
    }

    public void deletePreviousNGMark() {
        final String previousNGMark = getMigrationPreviousNGMark();
        deleteControlFile(new File(previousNGMark), "...Deleting previous-NG mark");
    }

    // -----------------------------------------------------
    //                                        Hierarchy File
    //                                        --------------
    public List<File> findHierarchyFileList(String rootDir) {
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

    public File findLatestNameFile(List<File> fileList) {
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
    public void deleteControlFile(File file, String msg) {
        if (file.exists()) {
            if (msg != null) {
                _log.info(msg + ": " + resolvePath(file));
            }
            file.delete();
        }
    }

    public void writeControlNotice(File file, String notice) throws IOException {
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

    public void copyFile(File src, File dest) {
        try {
            FileUtils.getFileUtils().copyFile(src, dest);
        } catch (IOException e) {
            String msg = "Failed to copy file: " + src + " to " + dest;
            throw new IllegalStateException(msg, e);
        }
    }

    public void mkdirsDirIfNotExists(String dirPath) {
        final File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String resolvePath(File file) {
        return Srl.replace(file.getPath(), "\\", "/");
    }

    protected String ln() {
        return "\n";
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

    protected String getMigrationPreviousDir() {
        return getReplaceSchemaProperties().getMigrationPreviousDir();
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
}
