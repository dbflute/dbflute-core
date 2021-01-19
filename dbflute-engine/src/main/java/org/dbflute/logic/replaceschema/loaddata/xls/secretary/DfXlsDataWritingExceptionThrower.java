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
package org.dbflute.logic.replaceschema.loaddata.xls.secretary;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.dbflute.DfBuildProperties;
import org.dbflute.bhv.exception.SQLExceptionAdviser;
import org.dbflute.exception.DfJDBCException;
import org.dbflute.exception.DfXlsDataEmptyColumnDefException;
import org.dbflute.exception.DfXlsDataEmptyRowDataException;
import org.dbflute.exception.DfXlsDataRegistrationFailureException;
import org.dbflute.exception.DfXlsDataTableNotFoundException;
import org.dbflute.helper.dataset.DfDataColumn;
import org.dbflute.helper.dataset.DfDataRow;
import org.dbflute.helper.dataset.DfDataTable;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.logic.replaceschema.loaddata.base.DfAbsractDataWriter.StringProcessor;
import org.dbflute.properties.DfBasicProperties;

/**
 * @author jflute
 * @since 1.2.5 extracted from DfXlsDataHandlerImpl (2021/01/18 Monday at roppongi japanese)
 */
public class DfXlsDataWritingExceptionThrower {

    protected final SQLExceptionAdviser _adviser = new SQLExceptionAdviser();

    // ===================================================================================
    //                                                                               Throw
    //                                                                               =====
    // -----------------------------------------------------
    //                                             Not Found
    //                                             ---------
    public void throwTableNotFoundException(File file, String tableDbName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The table specified on the xls file was not found in the schema.");
        br.addItem("Advice");
        br.addElement("Please confirm the name about its spelling.");
        br.addElement("And confirm that whether the DLL executions have errors.");
        br.addItem("Xls File");
        br.addElement(file);
        br.addItem("Table");
        br.addElement(tableDbName);
        final String msg = br.buildExceptionMessage();
        throw new DfXlsDataTableNotFoundException(msg);
    }

    // -----------------------------------------------------
    //                                             Empty Row
    //                                             ---------
    public void throwXlsDataEmptyRowDataException(String dataDirectory, File file, DfDataTable dataTable, int rowNumber) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The empty row data on the xls file was found.");
        br.addItem("Advice");
        br.addElement("Please remove the empty row.");
        br.addElement("ReplaceSchema does not allow empty row on xls data.");
        // suppress duplicated info (show these elements in failure exception later)
        //br.addItem("Data Directory");
        //br.addElement(dataDirectory);
        //br.addItem("Xls File");
        //br.addElement(file);
        br.addItem("Table");
        br.addElement(dataTable.getTableDbName());
        br.addItem("Row Number");
        br.addElement(rowNumber);
        final String msg = br.buildExceptionMessage();
        throw new DfXlsDataEmptyRowDataException(msg);
    }

    // -----------------------------------------------------
    //                                         Write Failure
    //                                         -------------
    public void handleWriteTableFailureException(String dataDirectory, File file, String tableDbName, RuntimeException e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to register the xls data for ReplaceSchema.");
        br.addItem("Advice");
        br.addElement("Please confirm the exception message.");
        br.addItem("Data Directory");
        br.addElement(dataDirectory);
        br.addItem("Xls File");
        br.addElement(file);
        br.addItem("Table");
        br.addElement(tableDbName);
        final String msg = br.buildExceptionMessage();
        throw new DfXlsDataRegistrationFailureException(msg, e);
    }

    // -----------------------------------------------------
    //                                    Write SQLException
    //                                    ------------------
    public void handleWriteTableSQLException(String dataDirectory, File file, DfDataTable dataTable // basic
            , SQLException mainEx // an exception of main process
            , SQLException retryEx, DfDataRow retryDataRow // retry
            , List<String> columnNameList // supplement
            , Map<String, Map<String, Class<?>>> bindTypeCacheMap // cache map of bind type
            , Map<String, Map<String, StringProcessor>> stringProcessorCacheMap // cache map of string processor
    ) {
        final DfJDBCException wrappedEx = DfJDBCException.voice(mainEx);
        final String tableDbName = dataTable.getTableDbName();
        final String msg = buildWriteFailureMessage(dataDirectory, file, tableDbName, wrappedEx, retryEx, retryDataRow, columnNameList,
                bindTypeCacheMap, stringProcessorCacheMap);
        throw new DfXlsDataRegistrationFailureException(msg, wrappedEx);
    }

    protected String buildWriteFailureMessage(String dataDirectory, File file, String tableDbName // basic
            , DfJDBCException mainEx // an exception of main process
            , SQLException retryEx, DfDataRow retryDataRow // retry
            , List<String> columnNameList // supplement
            , Map<String, Map<String, Class<?>>> bindTypeCacheMap // cache map of bind type
            , Map<String, Map<String, StringProcessor>> stringProcessorCacheMap // cache map of string processor
    ) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to register the table data.");
        br.addItem("Advice");
        br.addElement("Please confirm the SQLException message.");
        final String advice = _adviser.askAdvice(mainEx, getBasicProperties().getCurrentDBDef());
        if (advice != null && advice.trim().length() > 0) {
            br.addElement("*" + advice);
        }
        br.addItem("Data Directory");
        br.addElement(dataDirectory);
        br.addItem("Xls File");
        br.addElement(file.getName());
        br.addItem("Table");
        br.addElement(tableDbName);
        br.addItem("SQLException");
        br.addElement(mainEx.getClass().getName());
        br.addElement(mainEx.getMessage());
        if (retryEx != null) {
            br.addItem("Non-Batch Retry");
            br.addElement(retryEx.getClass().getName());
            br.addElement(retryEx.getMessage());
            br.addElement(columnNameList.toString());
            br.addElement(retryDataRow.toString());
            br.addElement("Row Number: " + retryDataRow.getRowNumber());
        }
        final Map<String, Class<?>> bindTypeMap = bindTypeCacheMap.get(tableDbName);
        final Map<String, StringProcessor> stringProcessorMap = stringProcessorCacheMap.get(tableDbName);
        if (bindTypeMap != null) {
            br.addItem("Bind Type");
            final Set<Entry<String, Class<?>>> entrySet = bindTypeMap.entrySet();
            for (Entry<String, Class<?>> entry : entrySet) {
                final String columnName = entry.getKey();
                StringProcessor processor = null;
                if (stringProcessorMap != null) {
                    processor = stringProcessorMap.get(columnName);
                }
                final String bindType = entry.getValue().getName();
                final String processorExp = (processor != null ? " (" + processor + ")" : "");
                br.addElement(columnName + " = " + bindType + processorExp);
            }
        }
        return br.buildExceptionMessage();
    }

    // -----------------------------------------------------
    //                                     Column Definition
    //                                     -----------------
    public void throwXlsDataColumnDefFailureException(String dataDirectory, File file, DfDataTable dataTable) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The table specified on the xls file does not have (writable) columns.");
        br.addItem("Advice");
        br.addElement("Please confirm the column names about their spellings.");
        br.addElement("And confirm the column definition of the table.");
        // suppress duplicated info (show these elements in failure exception later)
        //br.addItem("Data Directory");
        //br.addElement(dataDirectory);
        //br.addItem("Xls File");
        //br.addElement(file);
        br.addItem("Table");
        br.addElement(dataTable.getTableDbName());
        br.addItem("Defined Column");
        final int columnSize = dataTable.getColumnSize();
        if (columnSize > 0) {
            for (int i = 0; i < dataTable.getColumnSize(); i++) {
                final DfDataColumn dataColumn = dataTable.getColumn(i);
                br.addElement(dataColumn.getColumnDbName());
            }
        } else {
            br.addElement("(no column)");
        }
        final String msg = br.buildExceptionMessage();
        throw new DfXlsDataEmptyColumnDefException(msg);
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    protected DfBasicProperties getBasicProperties() {
        return getProperties().getBasicProperties();
    }
}
