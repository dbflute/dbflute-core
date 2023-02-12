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
package org.dbflute.logic.doc.lreverse.existing;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
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
import org.dbflute.logic.replaceschema.loaddata.delimiter.secretary.DfDelimiterDataTableDbNameExtractor;
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
     * @param tsvDataDir The base directory that has TSV encoding directory and TSV files, e.g. "./playsql/data/tsv/reversetsv". (NotNull)
     * @return The information of existing TSV files for table name handling. (NotNull) 
     */
    public DfLReverseExistingTsvInfo extractExistingTsvInfo(File tsvDataDir) {
        final List<File> existingTsvList = findExistingTsvList(tsvDataDir);
        final Map<File, String> existingTsvTableMap = DfCollectionUtil.newLinkedHashMap();
        final Map<String, List<File>> tableExistingTsvListMap = StringKeyMap.createAsFlexible();
        for (File existingTsv : existingTsvList) {
            final String fileName = existingTsv.getName(); // e.g. cyclic_07_02-PURCHASE_PAYMENT.tsv, PURCHASE_PAYMENT.tsv
            final String tableDbName = new DfDelimiterDataTableDbNameExtractor(fileName).extractTableDbName();

            // one TSV file always has only one table
            existingTsvTableMap.put(existingTsv, tableDbName);

            // one table can be related to plural files (e.g. per encoding)
            List<File> tableExistingTsvList = tableExistingTsvListMap.get(tableDbName);
            if (tableExistingTsvList == null) {
                tableExistingTsvList = DfCollectionUtil.newArrayList();
                tableExistingTsvListMap.put(tableDbName, tableExistingTsvList);
            }
            tableExistingTsvList.add(existingTsv);
        }
        return new DfLReverseExistingTsvInfo(existingTsvTableMap, tableExistingTsvListMap);
    }

    protected List<File> findExistingTsvList(File tsvDataDir) {
        List<File> tsvList = DfCollectionUtil.newArrayList();
        final DfDelimiterDataEncodingDirectoryExtractor extractor = new DfDelimiterDataEncodingDirectoryExtractor(tsvDataDir);
        final List<String> encodingDirectoryList = extractor.extractEncodingDirectoryList(); // e.g. UTF-8, Shift_JIS
        for (String encoding : encodingDirectoryList) {
            final File encodingDir = new File(resolvePath(tsvDataDir) + "/" + encoding); // e.g. .../tsv/reversetsv/UTF-8
            final File[] tsvFiles = encodingDir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".tsv");
                }
            });
            if (tsvFiles != null) {
                for (File tsvFile : tsvFiles) {
                    tsvList.add(tsvFile);
                }
            }
        }
        return tsvList;
    }

    // ===================================================================================
    //                                                                        Existing Xls
    //                                                                        ============
    /**
     * @param xlsDataDir The base directory that has xls files, e.g. "./playsql/data/ut/reversexls". (NotNull)
     * @return The information of existing xls files for table name handling. (NotNull) 
     */
    public DfLReverseExistingXlsInfo extractExistingXlsInfo(File xlsDataDir) {
        final List<File> existingXlsList = findExistingXlsList(xlsDataDir);
        final Map<File, List<String>> existingXlsTableListMap = DfCollectionUtil.newLinkedHashMap();
        final Map<String, File> tableExistingXlsMap = StringKeyMap.createAsFlexible();
        final Map<String, String> tableNameMap = _tableNameProp.findTableNameMap(resolvePath(xlsDataDir));
        for (File existingXls : existingXlsList) {
            final DfTableXlsReader reader = createTableXlsReader(xlsDataDir, existingXls, tableNameMap);
            final DfDataSet dataSet = reader.read();
            final List<String> tableList = new ArrayList<String>();
            for (int i = 0; i < dataSet.getTableSize(); i++) {
                final DfDataTable dataTable = dataSet.getTable(i);
                final String tableDbName = dataTable.getTableDbName();
                tableList.add(tableDbName);
                if (tableExistingXlsMap.containsKey(tableDbName)) {
                    // #for_now jflute when common determination, unneeded? (2023/02/06)
                    throwLoadDataReverseDuplicateTableException(tableExistingXlsMap, tableDbName);
                }
                tableExistingXlsMap.put(tableDbName, existingXls);
            }
            existingXlsTableListMap.put(existingXls, tableList); // xls may have plural tables
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
