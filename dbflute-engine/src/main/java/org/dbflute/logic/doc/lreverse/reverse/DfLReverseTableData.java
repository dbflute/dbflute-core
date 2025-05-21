/*
 * Copyright 2014-2025 the original author or authors.
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
package org.dbflute.logic.doc.lreverse.reverse;

import java.io.File;
import java.io.FileFilter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.torque.engine.database.model.Table;
import org.dbflute.DfBuildProperties;
import org.dbflute.helper.io.compress.DfZipArchiver;
import org.dbflute.helper.io.xls.DfXlsFactory;
import org.dbflute.helper.jdbc.context.DfSchemaSource;
import org.dbflute.logic.doc.lreverse.DfLReverseOutputResource;
import org.dbflute.logic.doc.lreverse.output.DfLReverseOutputHandler;
import org.dbflute.properties.DfDocumentProperties;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 1.2.5 as split (2021/01/04 Monday at roppongi japanese)
 */
public class DfLReverseTableData {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The logger instance for this class. (NotNull) */
    private static final Logger _log = LoggerFactory.getLogger(DfLReverseTableData.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfSchemaSource _dataSource;
    protected final DfLReverseOutputHandler _outputHandler;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfLReverseTableData(DfSchemaSource dataSource, DfLReverseOutputHandler outputHandler) {
        _dataSource = dataSource;
        _outputHandler = outputHandler;
    }

    // ===================================================================================
    //                                                                       Reverse Table
    //                                                                       =============
    public void reverseTableData(Map<File, DfLReverseOutputResource> orderedMap, File baseDir, List<String> sectionInfoList) {
        deletePreviousDataFile(baseDir);
        final Integer recordLimit = getRecordLimit();
        final Set<String> alreadySectionTitleSet = DfCollectionUtil.newHashSet();
        for (Entry<File, DfLReverseOutputResource> entry : orderedMap.entrySet()) {
            final File outputDataFile = entry.getKey();
            final DfLReverseOutputResource resource = entry.getValue();
            final List<Table> tableList = resource.getTableList();
            final Map<String, Table> tableMap = DfCollectionUtil.newLinkedHashMap();
            for (Table table : tableList) {
                tableMap.put(table.getTableDbName(), table); // e.g. MEMBER, nextschema.MEMBER
            }
            final String sectionTitle;
            if (resource.isOneToOneFile()) {
                final String sectionExp = Srl.lfill(String.valueOf(resource.getSectionNo()), 2, '0');
                sectionTitle = "[section: " + sectionExp + "_" + resource.getMainName() + "]";
            } else {
                sectionTitle = "[" + outputDataFile.getName() + "]: tables=" + tableList.size();
            }
            if (!alreadySectionTitleSet.contains(sectionTitle)) { // for delimiter basis structure
                _log.info("");
                _log.info(sectionTitle);
                sectionInfoList.add("");
                sectionInfoList.add(sectionTitle);
            }
            alreadySectionTitleSet.add(sectionTitle);
            _outputHandler.outputData(tableMap, recordLimit, outputDataFile, resource, sectionInfoList);
        }
    }

    protected void deletePreviousDataFile(File baseDir) {
        if (isDelimiterDataBasis()) {
            // #for_now jflute only current baseDir is target, should other type be also deleted? (2024/10/05)
            backupExistingDataFile(baseDir, createTsvFileFilter());
            doDeletePreviousDataFile(baseDir, createTsvFileFilter());
        } else {
            backupExistingDataFile(baseDir, createXlsFileFilter());
            doDeletePreviousDataFile(baseDir, createXlsFileFilter());
            final String delimiterDataDir = _outputHandler.getLargeDataDir(); // null allowed
            if (delimiterDataDir != null) { // basically true
                doDeletePreviousDataFile(new File(delimiterDataDir), createTsvFileFilter());
            }
        }
    }

    // -----------------------------------------------------
    //                                                Backup
    //                                                ------
    protected void backupExistingDataFile(File baseDir, FileFilter filter) {
        final File[] listFiles = baseDir.listFiles(filter);
        if (listFiles == null || listFiles.length == 0) {
            return;
        }
        final String backupDirPath = resolvePath(baseDir) + "/backup";
        final File backupDir = new File(backupDirPath);
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
        final String backupFilePath = backupDirPath + "/previous-data-gitignore-recommended.zip";
        final File backupFile = new File(backupFilePath);
        if (backupFile.exists()) {
            backupFile.delete();
        }
        _log.info("...Compressing previous data as zip: " + backupFilePath);
        final DfZipArchiver zipArchiver = new DfZipArchiver(backupFile);
        zipArchiver.compress(baseDir, filter);
    }

    // -----------------------------------------------------
    //                                         Previous Data
    //                                         -------------
    protected void doDeletePreviousDataFile(File baseDir, FileFilter filter) {
        final File[] listFiles = baseDir.listFiles(filter);
        if (listFiles == null) {
            return;
        }
        for (File previousFile : listFiles) {
            if (previousFile.exists()) {
                previousFile.delete();
            }
        }
    }

    // -----------------------------------------------------
    //                                           File Filter
    //                                           -----------
    protected FileFilter createTsvFileFilter() {
        return new FileFilter() {
            public boolean accept(File file) {
                return file.getName().endsWith(".tsv");
            }
        };
    }

    protected FileFilter createXlsFileFilter() {
        return DfXlsFactory.instance().createXlsFileFilter();
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    protected DfDocumentProperties getDocumentProperties() {
        return getProperties().getDocumentProperties();
    }

    // -----------------------------------------------------
    //                                         File Resource
    //                                         -------------
    protected Integer getRecordLimit() {
        return getDocumentProperties().getLoadDataReverseRecordLimit();
    }

    // -----------------------------------------------------
    //                                        Delimiter Data
    //                                        --------------
    protected boolean isDelimiterDataBasis() {
        return getDocumentProperties().isLoadDataReverseDelimiterDataBasis();
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String resolvePath(File file) {
        return Srl.replace(file.getPath(), "\\", "/");
    }
}
