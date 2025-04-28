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
package org.dbflute.logic.doc.lreverse.existing;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dbflute.exception.DfLReverseProcessFailureException;
import org.dbflute.exception.DfXlsReaderReadFailureException;
import org.dbflute.helper.StringKeyMap;
import org.dbflute.helper.dataset.DfDataSet;
import org.dbflute.helper.dataset.DfDataTable;
import org.dbflute.helper.io.xls.DfTableXlsReader;
import org.dbflute.helper.io.xls.DfXlsFactory;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.logic.replaceschema.loaddata.delimiter.secretary.DfDelimiterDataEncodingDirectoryExtractor;
import org.dbflute.logic.replaceschema.loaddata.delimiter.secretary.DfDelimiterDataTableNameExtractor;
import org.dbflute.logic.replaceschema.loaddata.xls.dataprop.DfTableNameProp;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.2.5 as split (2021/01/04 Monday at roppongi japanese)
 */
public class DfLReverseExistingFileProvider {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfTableNameProp _tableNameProp;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfLReverseExistingFileProvider(DfTableNameProp tableNameProp) {
        _tableNameProp = tableNameProp;
    }

    // ===================================================================================
    //                                                                        Existing TSV
    //                                                                        ============
    /**
     * @param tsvDataDir The base directory that has TSV encoding directory and TSV files,
     *  e.g. "./playsql/data/tsv/reversetsv" or "...reversetsv/UTF-8". (NotNull)
     * @return The information of existing TSV files for table name handling. (NotNull) 
     */
    public DfLReverseExistingTsvInfo extractExistingTsvInfo(File tsvDataDir) {
        final List<File> existingTsvList = findExistingTsvList(tsvDataDir);
        final Map<File, String> existingTsvTableMap = DfCollectionUtil.newLinkedHashMap();
        final Map<String, List<File>> tableAllExistingTsvListMap = StringKeyMap.createAsFlexible();
        final Map<String, File> tableFirstExistingTsvMap = StringKeyMap.createAsFlexible();
        for (File existingTsv : existingTsvList) {
            final String fileName = existingTsv.getName(); // e.g. cyclic_07_02-PURCHASE_PAYMENT.tsv, PURCHASE_PAYMENT.tsv
            final String onfileTableName = extractOnfileTableName(fileName); // e.g. MEMBER, nextschema.MEMBER

            // one TSV file always has only one table
            existingTsvTableMap.put(existingTsv, onfileTableName);

            // one table can be related to plural files (e.g. per encoding)
            List<File> allExistingTsvList = tableAllExistingTsvListMap.get(onfileTableName);
            if (allExistingTsvList == null) {
                allExistingTsvList = DfCollectionUtil.newArrayList();
                tableAllExistingTsvListMap.put(onfileTableName, allExistingTsvList);
            }
            allExistingTsvList.add(existingTsv);

            // first file only for simple operation
            if (!tableFirstExistingTsvMap.containsKey(onfileTableName)) {
                tableFirstExistingTsvMap.put(onfileTableName, existingTsv);
            }
        }
        return new DfLReverseExistingTsvInfo(existingTsvTableMap, tableAllExistingTsvListMap, tableFirstExistingTsvMap);
    }

    protected String extractOnfileTableName(String fileName) {
        return new DfDelimiterDataTableNameExtractor(fileName).extractOnfileTableName();
    }

    protected List<File> findExistingTsvList(File tsvDataDir) {
        final List<File> tsvList = DfCollectionUtil.newArrayList();
        if (isTsvUpperBaseDirectory(tsvDataDir)) { // not encoding directory now e.g. reversetsv, tsv
            // needs to search each encoding directoiries
            final DfDelimiterDataEncodingDirectoryExtractor extractor = new DfDelimiterDataEncodingDirectoryExtractor(tsvDataDir);
            final List<String> encodingDirectoryList = extractor.extractEncodingDirectoryList(); // e.g. UTF-8, Shift_JIS
            for (String encoding : encodingDirectoryList) {
                final File encodingDir = new File(resolvePath(tsvDataDir) + "/" + encoding); // e.g. .../tsv/reversetsv/UTF-8
                final File[] tsvFiles = listTsvFiles(encodingDir);
                if (tsvFiles != null) {
                    for (File tsvFile : tsvFiles) {
                        tsvList.add(tsvFile);
                    }
                }
            }
        } else { // encoding directory now e.g. reversetsv/UTF-8
            final File[] tsvFiles = listTsvFiles(tsvDataDir);
            if (tsvFiles != null) {
                for (File tsvFile : tsvFiles) {
                    tsvList.add(tsvFile);
                }
            }
        }
        return tsvList;
    }

    protected boolean isTsvUpperBaseDirectory(File tsvDataDir) { // e.g. reversetsv, tsv
        return tsvDataDir.getName().endsWith("tsv");
    }

    protected File[] listTsvFiles(File encodingDir) { // null allowed
        return encodingDir.listFiles((dir, name) -> name.endsWith(".tsv"));
    }

    // ===================================================================================
    //                                                                        Existing Xls
    //                                                                        ============
    /**
     * It is precondition that tables on xls files are unique.
     * @param xlsDataDir The base directory that has xls files, e.g. "./playsql/data/ut/reversexls". (NotNull)
     * @return The information of existing xls files for table name handling. (NotNull) 
     */
    public DfLReverseExistingXlsInfo extractExistingXlsInfo(File xlsDataDir) {
        final List<File> existingXlsList = findExistingXlsList(xlsDataDir);
        final Map<File, List<String>> existingXlsTableListMap = DfCollectionUtil.newLinkedHashMap();
        final Map<String, List<File>> tableAllExistingXlsListMap = StringKeyMap.createAsFlexible();
        final Map<String, File> tableFirstExistingXlsMap = StringKeyMap.createAsFlexible();
        final Map<String, String> tableNameMap = _tableNameProp.findTableNameMap(resolvePath(xlsDataDir));
        for (File existingXls : existingXlsList) {
            final DfTableXlsReader reader = createTableXlsReader(xlsDataDir, existingXls, tableNameMap);
            final DfDataSet dataSet = reader.read();
            final List<String> tableList = new ArrayList<String>();
            for (int i = 0; i < dataSet.getTableSize(); i++) {
                final DfDataTable dataTable = dataSet.getTable(i);
                final String tableDbName = dataTable.getTableDbName();
                tableList.add(tableDbName);

                // one table can be related to plural files (e.g. in other xls files)
                List<File> allExistingXlsList = tableAllExistingXlsListMap.get(tableDbName);
                if (allExistingXlsList == null) {
                    allExistingXlsList = DfCollectionUtil.newArrayList();
                    tableAllExistingXlsListMap.put(tableDbName, allExistingXlsList);
                }
                allExistingXlsList.add(existingXls);

                // first file only for simple operation
                if (!tableFirstExistingXlsMap.containsKey(tableDbName)) {
                    tableFirstExistingXlsMap.put(tableDbName, existingXls);
                }

                // yes by jflute (2024/10/05)
                //if (tableFirstExistingXlsMap.containsKey(tableDbName)) {
                //    // for_now jflute when common determination, unneeded? (2023/02/06)
                //    throwLoadDataReverseDuplicateTableException(tableFirstExistingXlsMap, tableDbName);
                //}
            }
            existingXlsTableListMap.put(existingXls, tableList); // xls may have plural tables
        }
        return new DfLReverseExistingXlsInfo(existingXlsTableListMap, tableAllExistingXlsListMap, tableFirstExistingXlsMap);
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

    protected List<File> findExistingXlsList(File baseDir) {
        final FileFilter fileFilter = DfXlsFactory.instance().createXlsFileFilter();
        final File[] listFiles = baseDir.listFiles(fileFilter);
        return DfCollectionUtil.newArrayList(listFiles != null ? listFiles : new File[] {});
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String resolvePath(File file) {
        return Srl.replace(file.getPath(), "\\", "/");
    }
}
