/*
 * Copyright 2014-2021 the original author or authors.
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
import org.dbflute.logic.doc.lreverse.existing.DfLReverseExistingFileProvider;
import org.dbflute.logic.doc.lreverse.existing.DfLReverseExistingXlsInfo;
import org.dbflute.logic.replaceschema.loaddata.impl.dataprop.DfTableNameProp;
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
            orderedMap = toReplaceReverseOrderedMap(orderedList);
        }
        return orderedMap;
    }

    protected List<List<Table>> analyzeOrder(List<Table> tableList) {
        return _tableOrder.analyzeOrder(tableList, _skippedTableList);
    }

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
        return _tableOrder.extractMainName(tableList);
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

    protected String buildXlsFilePath(String number, String mainName) {
        final String fileTitle = getReverseFileTitle();
        final String fileExtension = DfXlsFactory.instance().getDefaultFileExtension();
        return getReverseXlsDataDir() + "/" + fileTitle + "-" + number + "-" + mainName + fileExtension;
    }

    // ===================================================================================
    //                                                                        Existing Xls
    //                                                                        ============
    protected DfLReverseExistingXlsInfo extractExistingXlsInfo(File baseDir) {
        return new DfLReverseExistingFileProvider(_tableNameProp).extractExistingXlsInfo(baseDir);
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
    protected String getReverseXlsDataDir() {
        return getDocumentProperties().getLoadDataReverseXlsDataDir();
    }

    protected String getReverseFileTitle() {
        return getDocumentProperties().getLoadDataReverseFileTitle();
    }

    // -----------------------------------------------------
    //                                          Basic Option
    //                                          ------------
    protected boolean isOverrideExistingDataFile() {
        return getDocumentProperties().isLoadDataReverseOverrideExistingDataFile();
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String resolvePath(File file) {
        return Srl.replace(file.getPath(), "\\", "/");
    }
}
