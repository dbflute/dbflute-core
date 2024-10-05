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
package org.dbflute.logic.doc.lreverse.order;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.torque.engine.database.model.Table;
import org.dbflute.DfBuildProperties;
import org.dbflute.helper.StringKeyMap;
import org.dbflute.helper.io.xls.DfXlsFactory;
import org.dbflute.helper.jdbc.context.DfSchemaSource;
import org.dbflute.logic.doc.lreverse.DfLReverseOutputResource;
import org.dbflute.logic.doc.lreverse.existing.DfLReverseExistingFileInfo;
import org.dbflute.logic.doc.lreverse.existing.DfLReverseExistingFileProvider;
import org.dbflute.logic.replaceschema.loaddata.xls.dataprop.DfTableNameProp;
import org.dbflute.properties.DfDocumentProperties;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.2.5 as split (2021/01/04 Monday at roppongi japanese)
 */
public class DfLReverseFileOrder {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfSchemaSource _dataSource;
    protected final DfTableNameProp _tableNameProp;
    protected final List<Table> _skippedTableList;
    protected final DfLReverseTableOrder _tableOrder;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfLReverseFileOrder(DfSchemaSource dataSource, DfTableNameProp tableNameProp, List<Table> skippedTableList) {
        _dataSource = dataSource;
        _tableNameProp = tableNameProp;
        _skippedTableList = skippedTableList;
        _tableOrder = createTableOrder();
    }

    protected DfLReverseTableOrder createTableOrder() {
        return new DfLReverseTableOrder();
    }

    // ===================================================================================
    //                                                                         Ordered Map
    //                                                                         ===========
    public Map<File, DfLReverseOutputResource> prepareOrderedMap(List<Table> tableList, File baseDir) {
        final List<List<Table>> orderedList = analyzeOrder(tableList);
        final Map<File, DfLReverseOutputResource> orderedMap;
        if (isOverrideExistingDataFile()) {
            orderedMap = toOverrideReverseOrderedMap(orderedList, baseDir);
        } else {
            orderedMap = toReplaceReverseOrderedMap(orderedList, baseDir);
        }
        return orderedMap;
    }

    protected List<List<Table>> analyzeOrder(List<Table> tableList) {
        return _tableOrder.analyzeOrder(tableList, _skippedTableList);
    }

    // ===================================================================================
    //                                                                    Override Reverse
    //                                                                    ================
    protected Map<File, DfLReverseOutputResource> toOverrideReverseOrderedMap(List<List<Table>> orderedList, File baseDir) {
        final DfLReverseExistingFileInfo existingFileInfo = extractExistingFileInfo(baseDir);
        final Map<File, DfLReverseOutputResource> orderedMap = createOrderedMap();
        final Map<String, File> existingFileMap; // already resolve short name
        {
            final String dataDirPath = resolvePath(baseDir);
            final Map<String, String> tableNameMap = _tableNameProp.findTableNameMap(dataDirPath);
            existingFileMap = prepareExistingFileMap(existingFileInfo, tableNameMap);
        }
        final List<Table> addedTableList = DfCollectionUtil.newArrayList();
        int sectionNo = 1;
        for (List<Table> nestedList : orderedList) {
            for (Table table : nestedList) {
                final File existingFile = existingFileMap.get(table.getTableDbName());
                if (existingFile == null) {
                    addedTableList.add(table);
                    continue;
                }
                // existing here
                DfLReverseOutputResource resource = orderedMap.get(existingFile);
                if (resource == null) { // table of new section
                    final String mainName = extractMainName(nestedList);
                    final List<Table> initialList = new ArrayList<Table>();
                    resource = createOutputResource(existingFile, initialList, sectionNo, mainName);
                    orderedMap.put(existingFile, resource);
                    ++sectionNo;
                }
                resource.addTable(table);
            }
        }
        registerAddedTableIfExists(orderedMap, addedTableList, sectionNo, baseDir);
        orderTableByExistingOrder(orderedMap, existingFileInfo);
        return orderedMap;
    }

    protected DfLReverseExistingFileInfo extractExistingFileInfo(File baseDir) {
        final DfLReverseExistingFileProvider provider = new DfLReverseExistingFileProvider(_tableNameProp);
        if (isDelimiterDataBasis()) { // @since 1.2.9
            return provider.extractExistingTsvInfo(baseDir);
        } else { // traditional
            return provider.extractExistingXlsInfo(baseDir);
        }
    }

    protected Map<File, DfLReverseOutputResource> createOrderedMap() {
        return new TreeMap<File, DfLReverseOutputResource>(new Comparator<File>() {
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName()); // ordered by existing file name
            }
        });
    }

    protected Map<String, File> prepareExistingFileMap(DfLReverseExistingFileInfo existingFileInfo, Map<String, String> tableNameMap) {
        // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
        // use first existing file fixedly (same as existing specification of xls) by jflute (2024/10/05)
        // it is precondition that duplicate table definition in data files when LoadDataReverse
        // _/_/_/_/_/_/_/_/_/_/
        final Map<String, File> firstExistingFileMap = existingFileInfo.getTableFirstExistingFileMap();
        final Map<String, File> translatedXlsMap = StringKeyMap.createAsFlexible();
        for (Entry<String, File> entry : firstExistingFileMap.entrySet()) {
            final String tableName = entry.getKey();
            if (tableName.startsWith("$")) { // short name mark, basically xls only
                final String translated = tableNameMap.get(tableName);
                if (translated != null) {
                    translatedXlsMap.put(translated, entry.getValue()); // short-name resolved
                }
            }
        }
        translatedXlsMap.putAll(firstExistingFileMap);
        return translatedXlsMap;
    }

    // -----------------------------------------------------
    //                                           Added Table
    //                                           -----------
    // added table means no existing table in existing data files
    protected void registerAddedTableIfExists(Map<File, DfLReverseOutputResource> orderedMap, List<Table> addedTableList, int sectionNo,
            File baseDir) {
        if (addedTableList.isEmpty()) {
            return;
        }
        final String mainName = extractMainName(addedTableList);
        if (isDelimiterDataBasis()) {
            for (Table table : addedTableList) {
                final File addedFile = createAddedTableDelimiterFile(table, baseDir);
                final DfLReverseOutputResource resource = createOutputResource(addedFile, addedTableList, sectionNo, mainName);
                orderedMap.put(addedFile, resource);
            }
        } else {
            final File addedFile = createAddedTableXlsFile(baseDir);
            final DfLReverseOutputResource resource = createOutputResource(addedFile, addedTableList, sectionNo, mainName);
            orderedMap.put(addedFile, resource);
        }
    }

    protected File createAddedTableDelimiterFile(Table table, File baseDir) {
        final String dataDir = resolvePath(baseDir);
        final String reverseFileTitle = getReverseDelimiterFileTitle();
        final String addedFileKeyword = "_99_99_added_table-" + table.getTableDispName(); // fitting with standard format
        final String fileExtension = ".tsv"; // fixed
        return new File(dataDir + "/" + reverseFileTitle + addedFileKeyword + fileExtension);
    }

    protected File createAddedTableXlsFile(File baseDir) {
        final String dataDir = resolvePath(baseDir);
        final String reverseFileTitle = getReverseXlsFileTitle();
        final String addedFileKeyword = "-99-added-table";
        final String fileExtension = DfXlsFactory.instance().getDefaultFileExtension();
        return new File(dataDir + "/" + reverseFileTitle + addedFileKeyword + fileExtension);
    }

    // -----------------------------------------------------
    //                                           Table Order
    //                                           -----------
    protected void orderTableByExistingOrder(Map<File, DfLReverseOutputResource> orderedMap, DfLReverseExistingFileInfo existingFileInfo) {
        final Map<File, List<String>> existingFileTableListMap = existingFileInfo.getExistingFileTableListMap();
        for (Entry<File, DfLReverseOutputResource> entry : orderedMap.entrySet()) {
            final File existingXls = entry.getKey();
            final List<String> tableNameList = existingFileTableListMap.get(existingXls);
            if (tableNameList != null) {
                final DfLReverseOutputResource resource = entry.getValue();
                resource.acceptTableOrder(tableNameList);
            }
        }
    }

    // -----------------------------------------------------
    //                               Main Name of Table List
    //                               -----------------------
    protected String extractMainName(List<Table> tableList) {
        return _tableOrder.extractMainName(tableList);
    }

    // ===================================================================================
    //                                                                     Replace Reverse
    //                                                                     ===============
    protected Map<File, DfLReverseOutputResource> toReplaceReverseOrderedMap(List<List<Table>> orderedList, File baseDir) {
        final Map<File, DfLReverseOutputResource> orderedMap = DfCollectionUtil.newLinkedHashMap();
        int sectionNo = 1;
        for (List<Table> tableList : orderedList) {
            final String sectionExp = (sectionNo < 10 ? "0" + sectionNo : String.valueOf(sectionNo));
            final String mainName = extractMainName(tableList);
            if (isDelimiterDataBasis()) { // @since 1.2.9
                int sheetNumber = 1;
                for (Table table : tableList) {
                    final File dataFile = createOutputDelimiterFile(sectionExp, sheetNumber, table, baseDir);
                    orderedMap.put(dataFile, createOutputResource(dataFile, tableList, sectionNo, mainName));
                    ++sheetNumber;
                }
            } else {
                final File dataFile = createOutputXlsFile(sectionExp, mainName, baseDir);
                orderedMap.put(dataFile, createOutputResource(dataFile, tableList, sectionNo, mainName));
            }
            ++sectionNo;
        }
        return orderedMap;
    }

    // -----------------------------------------------------
    //                                        Delimiter File
    //                                        --------------
    protected File createOutputDelimiterFile(String sectionExp, int sheetNumber, Table table, File baseDir) {
        final String ext = "tsv"; // fixed
        return new File(buildDelimiterFilePath(table, sectionExp, sheetNumber, baseDir, ext));
    }

    // copied from output handler's large data handling
    protected String buildDelimiterFilePath(Table table, String sectionExp, int sheetNumber, File baseDir, String ext) {
        return resolvePath(baseDir) + "/" + (buildDelimiterFilePrefix(sectionExp, sheetNumber) + table.getTableDispName() + "." + ext);
    }

    protected String buildDelimiterFilePrefix(String sectionExp, int sheetNumber) {
        final String sheetPrefix = sheetNumber < 10 ? "0" + sheetNumber : String.valueOf(sheetNumber);
        return "cyclic_" + sectionExp + "_" + sheetPrefix + "-";
    }

    // -----------------------------------------------------
    //                                              XLS File
    //                                              --------
    protected File createOutputXlsFile(String sectionExp, String mainName, File baseDir) {
        final String xlsFilePath = buildXlsFilePath(sectionExp, mainName, baseDir);
        return new File(xlsFilePath);
    }

    protected String buildXlsFilePath(String sectionExp, String mainName, File baseDir) {
        final String fileTitle = getReverseXlsFileTitle();
        final String fileExtension = DfXlsFactory.instance().getDefaultFileExtension();
        return resolvePath(baseDir) + "/" + fileTitle + "-" + sectionExp + "-" + mainName + fileExtension;
    }

    // ===================================================================================
    //                                                                        Small Helper
    //                                                                        ============
    protected DfLReverseOutputResource createOutputResource(File dataFile, List<Table> tableList, int sectionNo, String mainName) {
        return new DfLReverseOutputResource(dataFile, tableList, sectionNo, mainName);
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
    protected String getReverseDelimiterFileTitle() {
        return getDocumentProperties().getLoadDataReverseDelimiterFileTitle();
    }

    protected String getReverseXlsFileTitle() {
        return getDocumentProperties().getLoadDataReverseXlsFileTitle();
    }

    // -----------------------------------------------------
    //                                          Basic Option
    //                                          ------------
    protected boolean isOverrideExistingDataFile() {
        return getDocumentProperties().isLoadDataReverseOverrideExistingDataFile();
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
