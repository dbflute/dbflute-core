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
import org.dbflute.util.DfTypeUtil;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.2.5 as split (2021/01/04 Monday at roppongi japanese)
 */
public class DfLReverseFileOrder {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfSchemaSource _dataSource; // not null
    protected final DfTableNameProp _tableNameProp; // not null
    protected final List<Table> _skippedTableList; // not null
    protected final DfLReverseTableOrder _tableOrder; // not null

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
        return new DfLReverseTableOrder(prepareSectionTableGuidelineLimit());
    }

    protected Integer prepareSectionTableGuidelineLimit() {
        Integer sectionTableGuidelineLimit = getSectionTableGuidelineLimit();
        if (sectionTableGuidelineLimit == null) {
            sectionTableGuidelineLimit = 9; // fixed traditional parameter
        }
        return sectionTableGuidelineLimit;
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
        final List<Table> newTableList = DfCollectionUtil.newArrayList();
        int failoverSectionNo = 1;
        for (List<Table> nestedList : orderedList) {
            for (Table table : nestedList) {
                final File existingFile = existingFileMap.get(table.getTableDbName());
                if (existingFile == null) { // new table
                    newTableList.add(table);
                    continue;
                }
                // existing table
                DfLReverseOutputResource resource = orderedMap.get(existingFile);
                if (resource == null) { // table of new section, always here if delimiter basis
                    final String mainName = extractMainName(nestedList);
                    final int currentSectionNo = extractCurrentSectionNo(existingFile, failoverSectionNo);
                    if (isDelimiterDataBasis()) {
                        resource = createOutputResource(existingFile, table, currentSectionNo, mainName);
                    } else {
                        final List<Table> tableList = DfCollectionUtil.newArrayList(table);
                        resource = createOutputResource(existingFile, tableList, currentSectionNo, mainName);
                    }
                    orderedMap.put(existingFile, resource);
                    ++failoverSectionNo;
                } else { // second or more table of the existing section
                    resource.addTable(table);
                }
            }
        }
        registerNewTableIfExists(orderedMap, newTableList, baseDir);
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
            final String tableDbName = entry.getKey(); // e.g. MEMBER, nextschema.MEMBER
            if (tableDbName.startsWith("$")) { // short name mark, basically xls only
                final String translated = tableNameMap.get(tableDbName);
                if (translated != null) {
                    translatedXlsMap.put(translated, entry.getValue()); // short-name resolved
                }
            }
        }
        translatedXlsMap.putAll(firstExistingFileMap);
        return translatedXlsMap;
    }

    // -----------------------------------------------------
    //                                    Current Section No
    //                                    ------------------
    protected int extractCurrentSectionNo(File existingFile, int failoverSectionNo) {
        final String fileName = existingFile.getName(); // e.g. cyclic_01_01-MEMBER.tsv, cyclic-data-01-MEMBER.xls
        String sectionExp = null;
        if (fileName.endsWith(".tsv")) {
            final String delimiter = "_";
            if (Srl.count(fileName, delimiter) >= 2) { // e.g. cyclic_01_01-MEMBER.tsv
                final String rear = Srl.substringFirstRear(fileName, delimiter); // e.g. 01_01-MEMBER.tsv
                sectionExp = Srl.substringFirstFront(rear, delimiter); // e.g. 01
            }
        } else if (fileName.endsWith(".xls")) {
            final String delimiter = "-";
            if (Srl.count(fileName, delimiter) >= 3) { // cyclic-data-01-MEMBER.xls
                final String rear = Srl.substringFirstRear(fileName, delimiter); // e.g. data-01-MEMBER.xls
                final String nextRear = Srl.substringFirstRear(rear, delimiter); // e.g. 01-MEMBER.xls
                sectionExp = Srl.substringFirstFront(nextRear, delimiter); // e.g. 01
            }
        }
        if (sectionExp != null && Srl.isNumberHarfAll(sectionExp)) {
            try {
                final Integer extracted = DfTypeUtil.toInteger(sectionExp);
                if (extracted != null) {
                    return extracted;
                }
            } catch (NumberFormatException ignored) {}
        }
        return failoverSectionNo;
    }

    // -----------------------------------------------------
    //                                             New Table
    //                                             ---------
    // new table means no existing table in existing data files
    protected void registerNewTableIfExists(Map<File, DfLReverseOutputResource> orderedMap, List<Table> newTableList, File baseDir) {
        if (newTableList.isEmpty()) {
            return;
        }
        final int newTableSectionNo = 99; // fixed
        final String mainName = extractMainName(newTableList);
        if (isDelimiterDataBasis()) {
            for (Table table : newTableList) {
                final File newFile = createNewTableDelimiterFile(table, baseDir);
                final DfLReverseOutputResource resource = createOutputResource(newFile, table, newTableSectionNo, mainName);
                orderedMap.put(newFile, resource);
            }
        } else {
            final File newFile = createNewTableXlsFile(baseDir);
            final DfLReverseOutputResource resource = createOutputResource(newFile, newTableList, newTableSectionNo, mainName);
            orderedMap.put(newFile, resource);
        }
    }

    protected File createNewTableDelimiterFile(Table table, File baseDir) {
        final String dataDir = resolvePath(baseDir);
        final String reverseFileTitle = getReverseDelimiterFileTitle();
        final String newFileKeyword = "_99_99_new_table-" + table.getTableDispName(); // fitting with standard format
        final String fileExtension = ".tsv"; // fixed
        return new File(dataDir + "/" + reverseFileTitle + newFileKeyword + fileExtension);
    }

    protected File createNewTableXlsFile(File baseDir) {
        final String dataDir = resolvePath(baseDir);
        final String reverseFileTitle = getReverseXlsFileTitle();
        final String newFileKeyword = "-99-new-table";
        final String fileExtension = DfXlsFactory.instance().getDefaultFileExtension();
        return new File(dataDir + "/" + reverseFileTitle + newFileKeyword + fileExtension);
    }

    // -----------------------------------------------------
    //                                           Table Order
    //                                           -----------
    protected void orderTableByExistingOrder(Map<File, DfLReverseOutputResource> orderedMap, DfLReverseExistingFileInfo existingFileInfo) {
        final Map<File, List<String>> existingFileTableListMap = existingFileInfo.getExistingFileTableListMap();
        for (Entry<File, DfLReverseOutputResource> entry : orderedMap.entrySet()) {
            final File existingFile = entry.getKey();
            final List<String> tableNameList = existingFileTableListMap.get(existingFile);
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
                    orderedMap.put(dataFile, createOutputResource(dataFile, table, sectionNo, mainName));
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
        final String reverseFileTitle = getReverseDelimiterFileTitle();
        final String sheetPrefix = sheetNumber < 10 ? "0" + sheetNumber : String.valueOf(sheetNumber);
        return reverseFileTitle + "_" + sectionExp + "_" + sheetPrefix + "-";
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
    //                                                                     Output Resource
    //                                                                     ===============
    protected DfLReverseOutputResource createOutputResource(File dataFile, Table table, int sectionNo, String mainName) {
        return new DfLReverseOutputResource(dataFile, table, sectionNo, mainName);
    }

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

    // -----------------------------------------------------
    //                         Section Table Guideline Limit
    //                         -----------------------------
    protected Integer getSectionTableGuidelineLimit() { // null allowed
        return getProperties().getDocumentProperties().getLoadDataReverseSectionTableGuidelineLimit();
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String resolvePath(File file) {
        return Srl.replace(file.getPath(), "\\", "/");
    }
}
