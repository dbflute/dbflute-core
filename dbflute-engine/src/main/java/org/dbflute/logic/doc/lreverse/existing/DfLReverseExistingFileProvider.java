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
    //                                                                        Existing Xls
    //                                                                        ============
    public DfLReverseExistingXlsInfo extractExistingXlsInfo(File baseDir) {
        final List<File> existingXlsList = findExistingXlsList(baseDir);
        final Map<File, List<String>> existingXlsTableListMap = DfCollectionUtil.newLinkedHashMap();
        final Map<String, File> tableExistingXlsMap = StringKeyMap.createAsFlexible();
        final String dataDirPath = resolvePath(baseDir);
        final Map<String, String> tableNameMap = _tableNameProp.findTableNameMap(dataDirPath);
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
