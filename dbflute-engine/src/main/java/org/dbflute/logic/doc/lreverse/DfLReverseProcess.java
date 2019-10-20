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
package org.dbflute.logic.doc.lreverse;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.torque.engine.database.model.AppData;
import org.apache.torque.engine.database.model.Database;
import org.apache.torque.engine.database.model.Table;
import org.dbflute.DfBuildProperties;
import org.dbflute.exception.DfLReverseProcessFailureException;
import org.dbflute.exception.DfXlsReaderReadFailureException;
import org.dbflute.helper.StringKeyMap;
import org.dbflute.helper.StringSet;
import org.dbflute.helper.dataset.DfDataSet;
import org.dbflute.helper.dataset.DfDataTable;
import org.dbflute.helper.io.compress.DfZipArchiver;
import org.dbflute.helper.io.xls.DfTableXlsReader;
import org.dbflute.helper.io.xls.DfXlsFactory;
import org.dbflute.helper.jdbc.context.DfSchemaSource;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.logic.jdbc.schemaxml.DfSchemaXmlReader;
import org.dbflute.logic.jdbc.schemaxml.DfSchemaXmlSerializer;
import org.dbflute.logic.replaceschema.loaddata.impl.dataprop.DfTableNameProp;
import org.dbflute.properties.DfDocumentProperties;
import org.dbflute.properties.DfReplaceSchemaProperties;
import org.dbflute.system.DBFluteSystem;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.DfTypeUtil;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 0.9.8.3 (2011/04/23 Saturday)
 */
public class DfLReverseProcess {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The logger instance for this class. (NotNull) */
    private static final Logger _log = LoggerFactory.getLogger(DfLReverseProcess.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfSchemaSource _dataSource;
    protected final DfLReverseOutputHandler _outputHandler;
    protected final DfTableOrderAnalyzer _tableOrderAnalyzer;
    protected final DfTableNameProp _tableNameProp = new DfTableNameProp();
    protected final List<Table> _skippedTableList = DfCollectionUtil.newArrayList(); // initialize in filter

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfLReverseProcess(DfSchemaSource dataSource) {
        _dataSource = dataSource;
        _outputHandler = createLReverseOutputHandler();
        _tableOrderAnalyzer = createTableOrderAnalyzer();
    }

    protected DfLReverseOutputHandler createLReverseOutputHandler() {
        final DfLReverseOutputHandler handler = new DfLReverseOutputHandler(_dataSource);
        handler.setContainsCommonColumn(isContainsCommonColumn());
        final Integer xlsLimit = getXlsLimit(); // if null, default limit
        if (xlsLimit != null) {
            handler.setXlsLimit(xlsLimit);
        }
        if (isSuppressLargeDataHandling()) {
            handler.setSuppressLargeDataHandling(true);
        }
        if (isSuppressQuoteEmptyString()) {
            handler.setSuppressQuoteEmptyString(true);
        }
        final Integer cellLengthLimit = getCellLengthLimit();
        if (cellLengthLimit != null) {
            handler.setCellLengthLimit(cellLengthLimit);
        }
        handler.setDelimiterDataDir(getDelimiterDataDir());
        // changes to TSV for compatibility of copy and paste to excel @since 0.9.8.3
        //handler.setDelimiterDataTypeCsv(true);
        return handler;
    }

    protected DfTableOrderAnalyzer createTableOrderAnalyzer() {
        return new DfTableOrderAnalyzer();
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    public void execute() {
        final Database database = prepareDatabase();
        final List<Table> tableList = filterTableList(database);
        final List<String> sectionInfoList = prepareTitleSection(tableList);
        final File baseDir = prepareBaseDir();
        final Map<File, DfLReverseOutputResource> orderedMap = prepareOrderedMap(tableList, baseDir);
        reverseTableData(orderedMap, baseDir, sectionInfoList);
        outputTableNameMap();
        synchronizeOriginDateIfNeeds(sectionInfoList);
        outputResultMark(sectionInfoList);
    }

    // ===================================================================================
    //                                                                             Prepare
    //                                                                             =======
    protected Database prepareDatabase() {
        final String schemaXml = getReverseSchemaXml();
        final DfSchemaXmlSerializer serializer = createSchemaXmlSerializer(schemaXml);
        serializer.serialize();
        final DfSchemaXmlReader reader = createSchemaXmlReader(schemaXml);
        final AppData appData = reader.read();
        return appData.getDatabase();
    }

    protected DfSchemaXmlSerializer createSchemaXmlSerializer(String schemaXml) {
        return DfSchemaXmlSerializer.createAsManage(_dataSource, schemaXml, /*historyFile*/null);
    }

    protected DfSchemaXmlReader createSchemaXmlReader(String schemaXml) {
        return DfSchemaXmlReader.createAsFlexibleToManage(schemaXml);
    }

    protected List<String> prepareTitleSection(final List<Table> tableList) {
        final List<String> sectionInfoList = new ArrayList<String>();
        sectionInfoList.add("...Outputting load data: tables=" + tableList.size());
        final Integer recordLimit = getRecordLimit();
        if (recordLimit != null) {
            sectionInfoList.add("  recordLimit = " + recordLimit);
        }
        sectionInfoList.add("  isReplaceSchemaDirectUse = " + isReplaceSchemaDirectUse());
        sectionInfoList.add("  isOverrideExistingDataFile = " + isOverrideExistingDataFile());
        sectionInfoList.add("  isSynchronizeOriginDate = " + isSynchronizeOriginDate());
        final Integer xlsLimit = getXlsLimit();
        if (xlsLimit != null) { // e.g. xlsLimit = 0 (all TSV mode)
            sectionInfoList.add("  xlsLimit = " + xlsLimit);
        }
        for (String sectionInfo : sectionInfoList) {
            _log.info(sectionInfo);
        }
        return sectionInfoList;
    }

    protected File prepareBaseDir() {
        final File baseDir = new File(getReverseXlsDataDir());
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
        return baseDir;
    }

    protected Map<File, DfLReverseOutputResource> prepareOrderedMap(List<Table> tableList, File baseDir) {
        final List<List<Table>> orderedList = analyzeOrder(tableList);
        final Map<File, DfLReverseOutputResource> orderedMap;
        if (isOverrideExistingDataFile()) {
            orderedMap = toOverrideReverseOrderedMap(orderedList, baseDir);
        } else {
            orderedMap = toReplaceReverseOrderedMap(orderedList);
        }
        return orderedMap;
    }

    protected List<List<Table>> analyzeOrder(List<Table> tableList) {
        return _tableOrderAnalyzer.analyzeOrder(tableList, _skippedTableList);
    }

    // ===================================================================================
    //                                                                          Table List
    //                                                                          ==========
    protected List<Table> filterTableList(Database database) {
        final List<Table> tableList = database.getTableList();
        final Set<String> commonExistingTableSet = getCommonExistingTableSet();
        final List<Table> filteredList = DfCollectionUtil.newArrayListSized(tableList.size());
        _skippedTableList.clear();
        final List<Table> commonSkippedList = DfCollectionUtil.newArrayList();
        final List<Table> exceptSkippedList = DfCollectionUtil.newArrayList();
        _log.info("...Filtering reversed table: " + tableList.size());
        for (Table table : tableList) {
            if (table.isTypeView() || table.isAdditionalSchema()) {
                // fixedly out of target
                //   view object - view is not an object which has own data
                //   additional schema - tables on main schema only are target
                continue;
            }
            if (commonExistingTableSet.contains(table.getTableDbName())) {
                commonSkippedList.add(table);
                continue;
            }
            if (!isTargetTable(table)) {
                exceptSkippedList.add(table);
                continue;
            }
            filteredList.add(table);
        }
        if (!commonSkippedList.isEmpty()) {
            _log.info("[Common Table] *skipped");
            for (Table table : commonSkippedList) {
                _log.info("  " + table.getTableDbName());
                _skippedTableList.add(table);
            }
        }
        if (!exceptSkippedList.isEmpty()) {
            _log.info("[Except Table] *skipped");
            for (Table table : exceptSkippedList) {
                _log.info("  " + table.getTableDbName());
                _skippedTableList.add(table);
            }
        }
        return filteredList;
    }

    protected Set<String> getCommonExistingTableSet() {
        if (!isReplaceSchemaDirectUse()) {
            return DfCollectionUtil.emptySet();
        }
        final Set<String> tableSet = StringSet.createAsFlexible();
        tableSet.addAll(extractCommonExistingXlsTableSet(getMainCommonFirstXlsDataDir()));
        tableSet.addAll(extractCommonExistingXlsTableSet(getMainCommonReverseXlsDataDir()));
        tableSet.addAll(extractCommonExistingXlsTableSet(getMainCommonXlsDataDir()));
        return tableSet;
    }

    protected Set<String> extractCommonExistingXlsTableSet(String dataDir) {
        return extractExistingXlsInfo(new File(dataDir)).getTableExistingXlsMap().keySet();
    }

    protected boolean isTargetTable(Table table) {
        return isReverseTableTarget(table.getTableDbName());
    }

    // ===================================================================================
    //                                                                         Ordered Map
    //                                                                         ===========
    // -----------------------------------------------------
    //                                      Override Reverse
    //                                      ----------------
    protected Map<File, DfLReverseOutputResource> toOverrideReverseOrderedMap(List<List<Table>> orderedList, File baseDir) {
        final DfLReverseExistingXlsInfo existingXlsInfo = extractExistingXlsInfo(baseDir);
        final Map<File, DfLReverseOutputResource> orderedMap = createOrderedMap();
        final String dataDirPath = resolvePath(baseDir);
        final Map<String, String> tableNameMap = _tableNameProp.getTableNameMap(dataDirPath);
        final Map<String, File> translatedXlsMap = prepareTranslatedXlsMap(existingXlsInfo, tableNameMap);
        final List<Table> addedTableList = DfCollectionUtil.newArrayList();
        int sectionNo = 1;
        for (List<Table> nestedList : orderedList) {
            for (Table table : nestedList) {
                final File existingXls = translatedXlsMap.get(table.getTableDbName());
                if (existingXls == null) {
                    addedTableList.add(table);
                    continue;
                }
                DfLReverseOutputResource resource = orderedMap.get(existingXls);
                if (resource == null) {
                    final String mainName = extractMainName(nestedList);
                    final List<Table> initialList = new ArrayList<Table>();
                    resource = createOutputResource(existingXls, initialList, sectionNo, mainName);
                    orderedMap.put(existingXls, resource);
                    ++sectionNo;
                }
                resource.addTable(table);
            }
        }
        registerAddedTableIfExists(orderedMap, addedTableList, sectionNo);
        orderTableByExistingOrder(orderedMap, existingXlsInfo);
        return orderedMap;
    }

    protected Map<File, DfLReverseOutputResource> createOrderedMap() {
        return new TreeMap<File, DfLReverseOutputResource>(new Comparator<File>() {
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName()); // ordered by existing file name
            }
        });
    }

    protected Map<String, File> prepareTranslatedXlsMap(DfLReverseExistingXlsInfo existingXlsInfo, Map<String, String> tableNameMap) {
        final Map<String, File> existingXlsMap = existingXlsInfo.getTableExistingXlsMap();
        final Map<String, File> translatedXlsMap = StringKeyMap.createAsFlexible();
        for (Entry<String, File> entry : existingXlsMap.entrySet()) {
            final String tableName = entry.getKey();
            if (tableName.startsWith("$")) {
                final String translated = tableNameMap.get(tableName);
                if (translated != null) {
                    translatedXlsMap.put(translated, entry.getValue());
                }
            }
        }
        translatedXlsMap.putAll(existingXlsMap);
        return translatedXlsMap;
    }

    protected File createAddedTableXlsFile() {
        final String fileExtension = DfXlsFactory.instance().getDefaultFileExtension();
        return new File(getReverseXlsDataDir() + "/" + getReverseFileTitle() + "-99-added-table" + fileExtension);
    }

    protected DfLReverseOutputResource createOutputResource(File xlsFile, List<Table> tableList, int sectionNo, String mainName) {
        return new DfLReverseOutputResource(xlsFile, tableList, sectionNo, mainName);
    }

    protected void registerAddedTableIfExists(Map<File, DfLReverseOutputResource> orderedMap, List<Table> addedTableList, int sectionNo) {
        if (!addedTableList.isEmpty()) {
            final String mainName = extractMainName(addedTableList);
            final File addedTableXlsFile = createAddedTableXlsFile();
            orderedMap.put(addedTableXlsFile, createOutputResource(addedTableXlsFile, addedTableList, sectionNo, mainName));
        }
    }

    protected void orderTableByExistingOrder(Map<File, DfLReverseOutputResource> orderedMap, DfLReverseExistingXlsInfo existingXlsInfo) {
        final Map<File, List<String>> existingXlsTableListMap = existingXlsInfo.getExistingXlsTableListMap();
        for (Entry<File, DfLReverseOutputResource> entry : orderedMap.entrySet()) {
            final File existingXls = entry.getKey();
            final List<String> tableNameList = existingXlsTableListMap.get(existingXls);
            if (tableNameList != null) {
                final DfLReverseOutputResource resource = entry.getValue();
                resource.acceptTableOrder(tableNameList);
            }
        }
    }

    protected String extractMainName(List<Table> tableList) {
        return _tableOrderAnalyzer.extractMainName(tableList);
    }

    // -----------------------------------------------------
    //                                       Replace Reverse
    //                                       ---------------
    protected Map<File, DfLReverseOutputResource> toReplaceReverseOrderedMap(List<List<Table>> orderedList) {
        final Map<File, DfLReverseOutputResource> orderedMap = DfCollectionUtil.newLinkedHashMap();
        int sectionNo = 1;
        for (List<Table> nestedList : orderedList) {
            final String number = (sectionNo < 10 ? "0" + sectionNo : String.valueOf(sectionNo));
            final String mainName = extractMainName(nestedList);
            final File xlsFile = new File(buildXlsFilePath(number, mainName));
            orderedMap.put(xlsFile, createOutputResource(xlsFile, nestedList, sectionNo, mainName));
            ++sectionNo;
        }
        return orderedMap;
    }

    // ===================================================================================
    //                                                                       Reverse Table
    //                                                                       =============
    protected void reverseTableData(Map<File, DfLReverseOutputResource> orderedMap, File baseDir, List<String> sectionInfoList) {
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

    protected String buildXlsFilePath(String number, String mainName) {
        final String fileTitle = getReverseFileTitle();
        final String fileExtension = DfXlsFactory.instance().getDefaultFileExtension();
        return getReverseXlsDataDir() + "/" + fileTitle + "-" + number + "-" + mainName + fileExtension;
    }

    // ===================================================================================
    //                                                                        Existing Xls
    //                                                                        ============
    protected DfLReverseExistingXlsInfo extractExistingXlsInfo(File baseDir) {
        final List<File> existingXlsList = extractExistingXlsList(baseDir);
        final Map<File, List<String>> existingXlsTableListMap = DfCollectionUtil.newLinkedHashMap();
        final Map<String, File> tableExistingXlsMap = StringKeyMap.createAsFlexible();
        final String dataDirPath = resolvePath(baseDir);
        final Map<String, String> tableNameMap = _tableNameProp.getTableNameMap(dataDirPath);
        for (File existingXls : existingXlsList) {
            final DfTableXlsReader reader = createTableXlsReader(baseDir, existingXls, tableNameMap);
            final DfDataSet dataSet = reader.read();
            final List<String> tableList = new ArrayList<String>();
            for (int i = 0; i < dataSet.getTableSize(); i++) {
                final DfDataTable dataTable = dataSet.getTable(i);
                final String tableDbName = dataTable.getTableDbName();
                tableList.add(tableDbName);
                if (tableExistingXlsMap.containsKey(tableDbName)) {
                    throwLoadDataReverseDuplicateTableException(tableExistingXlsMap, tableDbName);
                }
                tableExistingXlsMap.put(tableDbName, existingXls);
            }
            existingXlsTableListMap.put(existingXls, tableList);
        }
        return new DfLReverseExistingXlsInfo(existingXlsTableListMap, tableExistingXlsMap);
    }

    protected DfTableXlsReader createTableXlsReader(File baseDir, File existingXls, Map<String, String> tableNameMap) {
        try {
            return new DfTableXlsReader(existingXls, tableNameMap, null, null, null, false);
        } catch (DfXlsReaderReadFailureException e) {
            final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
            br.addNotice("Failed to create xls reader for LoadDataReverse.");
            br.addItem("Base Dir");
            br.addElement(resolvePath(baseDir));
            br.addItem("Xls File");
            br.addElement(resolvePath(existingXls));
            final String msg = br.buildExceptionMessage();
            throw new DfLReverseProcessFailureException(msg, e);
        }
    }

    protected void throwLoadDataReverseDuplicateTableException(Map<String, File> existingXlsMap, String tableDbName) {
        final File existingXls = existingXlsMap.get(tableDbName);
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Duplicate table in the existing xls file.");
        br.addItem("Advice");
        br.addElement("The existing xls files should have unique table sheet");
        br.addElement("if you use override-reverse mode of LoadDataReverse.");
        br.addItem("Xls File");
        br.addElement(resolvePath(existingXls));
        br.addItem("Table");
        br.addElement(tableDbName);
        final String msg = br.buildExceptionMessage();
        throw new DfLReverseProcessFailureException(msg);
    }

    protected List<File> extractExistingXlsList(File baseDir) {
        final FileFilter fileFilter = DfXlsFactory.instance().createXlsFileFilter();
        final File[] listFiles = baseDir.listFiles(fileFilter);
        return DfCollectionUtil.newArrayList(listFiles != null ? listFiles : new File[] {});
    }

    // ===================================================================================
    //                                                                      Table Name Map
    //                                                                      ==============
    protected void outputTableNameMap() {
        final Map<String, Table> tableNameMap = _outputHandler.getTableNameMap();
        if (!tableNameMap.isEmpty()) {
            doOutputTableNameMap(tableNameMap);
        }
    }

    protected void doOutputTableNameMap(Map<String, Table> tableNameMap) {
        _log.info("...Outputting table name map for reversed tables");
        _tableNameProp.outputTableNameMap(getReverseXlsDataDir(), tableNameMap);
    }

    // ===================================================================================
    //                                                              Synchronize OriginDate
    //                                                              ======================
    protected void synchronizeOriginDateIfNeeds(List<String> sectionInfoList) {
        if (!isSynchronizeOriginDate()) {
            return;
        }
        _log.info("...Synchronizing origin date for date adjustment");
        final String dataDir = getReverseXlsDataDir();
        final DfLReverseOriginDateSynchronizer synchronizer = new DfLReverseOriginDateSynchronizer();
        final String syncResult = synchronizer.synchronizeOriginDate(dataDir);
        _log.info("  df:originDate: " + syncResult);
        sectionInfoList.add("");
        sectionInfoList.add("[loadingControlMap.dataprop]");
        sectionInfoList.add("df:originDate: " + syncResult);
    }

    // ===================================================================================
    //                                                                         Result Mark
    //                                                                         ===========
    protected void outputResultMark(List<String> sectionInfoList) {
        _log.info("...Outputting result mark for reversed data");
        final StringBuilder sb = new StringBuilder();
        sb.append(ln()).append("* * * * * * * * * * *");
        sb.append(ln()).append("*                   *");
        sb.append(ln()).append("* Load Data Reverse *");
        sb.append(ln()).append("*                   *");
        sb.append(ln()).append("* * * * * * * * * * *");
        for (String sectionInfo : sectionInfoList) {
            sb.append(ln()).append(sectionInfo);
        }
        final Date currentDate = DfTypeUtil.toDate(DBFluteSystem.currentTimeMillis());
        final String currentExp = DfTypeUtil.toString(currentDate, "yyyy/MM/dd HH:mm:ss");
        sb.append(ln()).append(ln()).append("Output Date: ").append(currentExp);
        final File dataPropFile = new File(getReverseXlsDataDir() + "/reverse-data-result.dfmark");
        if (dataPropFile.exists()) {
            dataPropFile.delete();
        }
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dataPropFile), "UTF-8"));
            bw.write(sb.toString());
            bw.flush();
        } catch (IOException e) {
            String msg = "Failed to write reverse-data-result.dfmark: " + dataPropFile;
            throw new DfLReverseProcessFailureException(msg, e);
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ignored) {}
            }
        }
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

    protected DfReplaceSchemaProperties getReplaceSchemaProperties() {
        return getProperties().getReplaceSchemaProperties();
    }

    // -----------------------------------------------------
    //                                         File Resource
    //                                         -------------
    protected Integer getRecordLimit() {
        return getDocumentProperties().getLoadDataReverseRecordLimit();
    }

    protected String getReverseSchemaXml() {
        return getDocumentProperties().getLoadDataReverseSchemaXml();
    }

    protected String getReverseXlsDataDir() {
        return getDocumentProperties().getLoadDataReverseXlsDataDir();
    }

    protected String getReverseFileTitle() {
        return getDocumentProperties().getLoadDataReverseFileTitle();
    }

    // -----------------------------------------------------
    //                                          Basic Option
    //                                          ------------
    protected boolean isReplaceSchemaDirectUse() {
        return getDocumentProperties().isLoadDataReverseReplaceSchemaDirectUse();
    }

    protected boolean isOverrideExistingDataFile() {
        return getDocumentProperties().isLoadDataReverseOverrideExistingDataFile();
    }

    protected boolean isSynchronizeOriginDate() {
        return getDocumentProperties().isLoadDataReverseSynchronizeOriginDate();
    }

    // -----------------------------------------------------
    //                                          Table Except
    //                                          ------------
    protected boolean isReverseTableTarget(String name) {
        return getDocumentProperties().isLoadDataReverseTableTarget(name);
    }

    // -----------------------------------------------------
    //                                        Output Handler
    //                                        --------------
    protected boolean isContainsCommonColumn() {
        return getDocumentProperties().isLoadDataReverseContainsCommonColumn();
    }

    protected Integer getXlsLimit() {
        return getDocumentProperties().getLoadDataReverseXlsLimit();
    }

    protected boolean isSuppressLargeDataHandling() {
        return getDocumentProperties().isLoadDataReverseSuppressLargeDataHandling();
    }

    protected boolean isSuppressQuoteEmptyString() {
        return getDocumentProperties().isLoadDataReverseSuppressQuoteEmptyString();
    }

    protected Integer getCellLengthLimit() {
        return getDocumentProperties().getLoadDataReverseCellLengthLimit();
    }

    protected String getDelimiterDataDir() {
        return getDocumentProperties().getLoadDataReverseDelimiterDataDir();
    }

    // -----------------------------------------------------
    //                                         ReplaceSchema
    //                                         -------------
    protected String getMainCommonFirstXlsDataDir() {
        return getReplaceSchemaProperties().getMainCommonFirstXlsDataDir();
    }

    protected String getMainCommonReverseXlsDataDir() {
        return getReplaceSchemaProperties().getMainCommonReverseXlsDataDir();
    }

    protected String getMainCommonXlsDataDir() {
        return getReplaceSchemaProperties().getMainCommonXlsDataDir();
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String ln() {
        return DBFluteSystem.ln();
    }

    protected String resolvePath(File file) {
        return Srl.replace(file.getPath(), "\\", "/");
    }
}
