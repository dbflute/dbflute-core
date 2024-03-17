/*
 * Copyright 2014-2024 the original author or authors.
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

import org.apache.torque.engine.database.model.Table;
import org.dbflute.DfBuildProperties;
import org.dbflute.helper.io.compress.DfZipArchiver;
import org.dbflute.helper.io.xls.DfXlsFactory;
import org.dbflute.helper.jdbc.context.DfSchemaSource;
import org.dbflute.logic.doc.lreverse.DfLReverseOutputResource;
import org.dbflute.logic.doc.lreverse.output.DfLReverseOutputHandler;
import org.dbflute.properties.DfDocumentProperties;
import org.dbflute.util.DfCollectionUtil;
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
        final Integer limit = getRecordLimit();
        for (Entry<File, DfLReverseOutputResource> entry : orderedMap.entrySet()) {
            final File xlsFile = entry.getKey();
            final DfLReverseOutputResource resource = entry.getValue();
            final List<Table> tableList = resource.getTableList();
            final Map<String, Table> tableInfoMap = DfCollectionUtil.newLinkedHashMap();
            for (Table table : tableList) {
                tableInfoMap.put(table.getTableDbName(), table);
            }
            final String sectionTitle = "[" + xlsFile.getName() + "]: tables=" + tableList.size();
            _log.info("");
            _log.info(sectionTitle);
            sectionInfoList.add("");
            sectionInfoList.add(sectionTitle);
            _outputHandler.outputData(tableInfoMap, limit, xlsFile, resource, sectionInfoList);
        }
    }

    protected void deletePreviousDataFile(File baseDir) {
        backupExistingXlsFile(baseDir);
        doDeletePreviousDataFile(baseDir, createXlsFileFilter());
        final String delimiterDataDir = _outputHandler.getDelimiterDataDir();
        if (delimiterDataDir != null) {
            doDeletePreviousDataFile(new File(delimiterDataDir), createTsvFileFilter());
        }
    }

    protected void backupExistingXlsFile(File baseDir) {
        final FileFilter filter = createXlsFileFilter();
        final File[] listFiles = baseDir.listFiles(filter);
        if (listFiles == null || listFiles.length == 0) {
            return;
        }
        final String backupDirPath = getReverseXlsDataDir() + "/backup";
        final File backupDir = new File(backupDirPath);
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
        final String backupFilePath = backupDirPath + "/latest-data.zip";
        final File backupFile = new File(backupFilePath);
        if (backupFile.exists()) {
            backupFile.delete();
        }
        _log.info("...Compressing latest data as zip: " + backupFilePath);
        final DfZipArchiver zipArchiver = new DfZipArchiver(backupFile);
        zipArchiver.compress(baseDir, filter);
    }

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

    protected FileFilter createXlsFileFilter() {
        return DfXlsFactory.instance().createXlsFileFilter();
    }

    protected FileFilter createTsvFileFilter() {
        return new FileFilter() {
            public boolean accept(File file) {
                return file.getName().endsWith(".tsv");
            }
        };
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

    protected String getReverseXlsDataDir() {
        return getDocumentProperties().getLoadDataReverseXlsDataDir();
    }

    protected String getDelimiterDataDir() {
        return getDocumentProperties().getLoadDataReverseDelimiterDataDir();
    }
}
