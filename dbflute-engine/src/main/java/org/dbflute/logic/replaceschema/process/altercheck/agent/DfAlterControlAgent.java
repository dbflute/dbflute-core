/*
 * Copyright 2014-2022 the original author or authors.
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
import java.util.Map;

import org.apache.tools.ant.util.FileUtils;
import org.dbflute.DfBuildProperties;
import org.dbflute.helper.dfmap.DfMapFile;
import org.dbflute.helper.dfmap.DfMapStyle;
import org.dbflute.properties.DfReplaceSchemaProperties;
import org.dbflute.system.DBFluteSystem;
import org.dbflute.util.DfCollectionUtil;
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
    //                                                                     Current Control
    //                                                                     ===============
    // -----------------------------------------------------
    //                                          Current Date
    //                                          ------------
    public Date currentDate() {
        return DBFluteSystem.currentDate();
    }

    public String currentDateExp() {
        return DfTypeUtil.toString(currentDate(), "yyyy/MM/dd HH:mm:ss");
    }

    // ===================================================================================
    //                                                                        File Control
    //                                                                        ============
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
    //                                                                        Mark Control
    //                                                                        ============
    // -----------------------------------------------------
    //                                           Delete Mark
    //                                           -----------
    public void deleteAllNGMark() {
        deleteNextNGMark();
        deleteAlterNGMark();
        deletePreviousNGMark();
        deleteWholeNGStateMap();
    }

    protected void deleteNextNGMark() {
        final String replaceNGMark = getMigrationNextNGMark();
        deleteMarkFile(new File(replaceNGMark), "...Deleting next-NG mark");
    }

    protected void deleteAlterNGMark() {
        deleteMarkFile(new File(getMigrationAlterNGMark()), "...Deleting alter-NG mark");
    }

    protected void deletePreviousNGMark() {
        final String previousNGMark = getMigrationPreviousNGMark();
        deleteMarkFile(new File(previousNGMark), "...Deleting previous-NG mark");
    }

    protected void deleteWholeNGStateMap() {
        deleteMarkFile(new File(getMigrationWholeNGStateMap()), "...Deleting whole-NG-state map");
    }

    public void deletePreviousOKMark() { // called by agent
        final String previousOKMark = getMigrationPreviousOKMark();
        deleteMarkFile(new File(previousOKMark), "...Deleting previous-OK mark");
    }

    public void deleteMarkFile(File file, String msg) {
        if (file.exists()) {
            if (msg != null) {
                _log.info(msg + ": " + resolvePath(file));
            }
            file.delete();
        }
    }

    // -----------------------------------------------------
    //                                            Write Mark
    //                                            ----------
    public void writeMarkLogRoad(File file, String notice) throws IOException {
        writeMarkSimple(file, buildMarkLogRoadBase(notice));
    }

    public void writeMarkLogRoad(File file, String notice, Map<String, Object> metaMap) throws IOException {
        final String roadBase = buildMarkLogRoadBase(notice);
        final StringBuilder sb = new StringBuilder();
        sb.append(roadBase);
        sb.append(ln()).append("---").append(ln()); // simple delimiter
        sb.append(new DfMapStyle().toMapString(metaMap));
        writeMarkSimple(file, sb.toString());
    }

    protected String buildMarkLogRoadBase(String notice) {
        return notice + ln() + "Look at the log for detail.";
    }

    public void writeMarkSimple(File file, String notice) throws IOException {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
            bw.write(notice);
            bw.flush();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ignored) {}
            }
        }
    }

    public void makeWholeNGStateMapFile(String domainCode, String stateCode) {
        final String stateMap = getMigrationWholeNGStateMap();
        try {
            final File mapFile = new File(stateMap);
            if (mapFile.exists()) { // basically already deleted here
                mapFile.delete(); // just in case
            }
            _log.info("...Marking whole-NG-state: " + stateMap);
            mapFile.createNewFile();
            final Map<String, Object> metaMap // e.g. map:{domainCode=ALT;stateCode=DIF}
                    = DfCollectionUtil.newLinkedHashMap("domainCode", domainCode, "stateCode", stateCode);
            new DfMapFile().writeMap(new FileOutputStream(mapFile), metaMap);
        } catch (IOException e) {
            String msg = "Failed to create a file for alter-state map: " + stateMap;
            throw new IllegalStateException(msg, e);
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

    protected String getMigrationWholeNGStateMap() {
        return getReplaceSchemaProperties().getMigrationWholeNGStateMap();
    }

    protected String getMigrationPreviousNGMark() {
        return getReplaceSchemaProperties().getMigrationPreviousNGMark();
    }
}
