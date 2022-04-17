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
package org.dbflute.logic.replaceschema.loaddata.delimiter.line;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.dbflute.exception.DfDelimiterDataColumnDefNotFoundException;
import org.dbflute.helper.StringSet;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.logic.jdbc.metadata.info.DfColumnMeta;

/**
 * @author jflute
 * @since 1.2.5 extracted from DfDelimiterDataWriterImpl (2021/01/20 Wednesday at roppongi japanese)
 */
public class DfDelimiterDataListedColumnHandler {

    // basic attribute
    protected final String _dataDirectory;
    protected final File _dataFile;
    protected final String _tableDbName;

    public DfDelimiterDataListedColumnHandler(String dataDirectory, File dataFile, String tableDbName) {
        _dataDirectory = dataDirectory;
        _dataFile = dataFile;
        _tableDbName = tableDbName;
    }

    public void setupColumnNameList(List<String> columnNameList, DfDelimiterDataFirstLineInfo firstLineInfo, Map<String, DfColumnMeta> columnMetaMap,
            Map<String, String> defaultValueMap, Predicate<String> needsCheckingColumnDef, Consumer<List<String>> columnDefChecker) {
        columnNameList.addAll(firstLineInfo.getColumnNameList());
        if (columnNameList.isEmpty()) {
            throwDelimiterDataColumnDefNotFoundException(_dataFile, _tableDbName);
        }
        if (needsCheckingColumnDef.test(_dataDirectory)) {
            columnDefChecker.accept(columnNameList);
        }
        final StringSet columnSet = StringSet.createAsFlexible();
        columnSet.addAll(columnNameList);
        final List<String> additionalColumnList = new ArrayList<String>();
        for (String defaultColumn : defaultValueMap.keySet()) {
            if (columnSet.contains(defaultColumn)) {
                continue;
            }
            if (columnMetaMap.containsKey(defaultColumn)) { // only existing column in DB
                additionalColumnList.add(defaultColumn);
            }
        }
        columnNameList.addAll(additionalColumnList); // defined columns + default columns (existing in DB)
    }

    protected void throwDelimiterDataColumnDefNotFoundException(File dataFile, String tableDbName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The column definition on the delimiter file was not found.");
        br.addItem("Advice");
        br.addElement("Make sure the header definition of the delimiter file exists.");
        br.addItem("Delimiter File");
        br.addElement(dataFile);
        br.addItem("Table");
        br.addElement(tableDbName);
        final String msg = br.buildExceptionMessage();
        throw new DfDelimiterDataColumnDefNotFoundException(msg);
    }
}
