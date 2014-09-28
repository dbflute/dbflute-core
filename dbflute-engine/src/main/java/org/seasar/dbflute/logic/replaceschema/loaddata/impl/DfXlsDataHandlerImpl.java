/*
 * Copyright 2004-2014 the Seasar Foundation and the Others.
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
package org.seasar.dbflute.logic.replaceschema.loaddata.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.engine.database.model.UnifiedSchema;
import org.seasar.dbflute.DBDef;
import org.seasar.dbflute.exception.DfXlsDataEmptyColumnDefException;
import org.seasar.dbflute.exception.DfXlsDataEmptyRowDataException;
import org.seasar.dbflute.exception.DfXlsDataRegistrationFailureException;
import org.seasar.dbflute.exception.DfXlsDataTableNotFoundException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.exception.handler.SQLExceptionAdviser;
import org.seasar.dbflute.helper.StringKeyMap;
import org.seasar.dbflute.helper.dataset.DfDataColumn;
import org.seasar.dbflute.helper.dataset.DfDataRow;
import org.seasar.dbflute.helper.dataset.DfDataSet;
import org.seasar.dbflute.helper.dataset.DfDataTable;
import org.seasar.dbflute.helper.dataset.states.DfDtsCreatedState;
import org.seasar.dbflute.helper.dataset.states.DfDtsSqlContext;
import org.seasar.dbflute.helper.dataset.types.DfDtsColumnType;
import org.seasar.dbflute.helper.dataset.types.DfDtsColumnTypes;
import org.seasar.dbflute.helper.io.xls.DfTableXlsReader;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfColumnMeta;
import org.seasar.dbflute.logic.replaceschema.loaddata.DfColumnBindTypeProvider;
import org.seasar.dbflute.logic.replaceschema.loaddata.DfLoadedDataInfo;
import org.seasar.dbflute.logic.replaceschema.loaddata.DfXlsDataHandler;
import org.seasar.dbflute.logic.replaceschema.loaddata.DfXlsDataResource;
import org.seasar.dbflute.logic.replaceschema.loaddata.impl.dataprop.DfLoadingControlProp.LoggingInsertType;
import org.seasar.dbflute.logic.replaceschema.loaddata.impl.dataprop.DfTableNameProp;
import org.seasar.dbflute.properties.propreader.DfOutsideMapPropReader;
import org.seasar.dbflute.resource.DBFluteSystem;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.Srl;

/**
 * The implementation of xls data handler. And also of writer.
 * @author jflute
 */
public class DfXlsDataHandlerImpl extends DfAbsractDataWriter implements DfXlsDataHandler {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance. */
    private static final Log _log = LogFactory.getLog(DfXlsDataHandlerImpl.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The pattern of skip sheet. (NullAllowed) */
    protected Pattern _skipSheetPattern;

    protected final SQLExceptionAdviser _adviser = new SQLExceptionAdviser();

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfXlsDataHandlerImpl(DataSource dataSource, UnifiedSchema unifiedSchema) {
        super(dataSource, unifiedSchema);
    }

    // ===================================================================================
    //                                                                                Read
    //                                                                                ====
    public List<DfDataSet> readSeveralData(DfXlsDataResource resource) {
        final String dataDirectory = resource.getDataDirectory();
        final List<File> xlsList = getXlsList(resource);
        final List<DfDataSet> ls = new ArrayList<DfDataSet>();
        for (File file : xlsList) {
            final DfTableXlsReader xlsReader = createTableXlsReader(dataDirectory, file);
            ls.add(xlsReader.read());
        }
        return ls;
    }

    // ===================================================================================
    //                                                                               Write
    //                                                                               =====
    public void writeSeveralData(DfXlsDataResource resource, DfLoadedDataInfo loadedDataInfo) {
        final String dataDirectory = resource.getDataDirectory();
        final List<File> xlsList = getXlsList(resource);
        if (xlsList.isEmpty()) {
            return;
        }
        final StringBuilder msgSb = new StringBuilder();
        for (File file : xlsList) {
            _log.info("/= = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = ");
            _log.info("writeData(" + file + ")");
            _log.info("= = = = = = =/");
            final DfTableXlsReader xlsReader = createTableXlsReader(dataDirectory, file);
            final DfDataSet dataSet = xlsReader.read();
            filterValidColumn(dataSet);
            setupDefaultValue(dataDirectory, dataSet);
            doWriteDataSet(resource, file, dataSet, msgSb);
            final boolean warned = false; // this has no warning fixedly
            loadedDataInfo.addLoadedFile(resource.getEnvType(), "xls", null, file.getName(), warned);
        }
        prepareImplicitClassificationLazyCheck(loadedDataInfo);
        outputResultMark(resource.getDataDirectory(), msgSb.toString());
    }

    protected void prepareImplicitClassificationLazyCheck(DfLoadedDataInfo info) {
        info.acceptImplicitClassificationLazyCheck(getImplicitClassificationLazyCheckerList());
    }

    // -----------------------------------------------------
    //                                               DataSet
    //                                               -------
    protected void doWriteDataSet(DfXlsDataResource resource, File file, DfDataSet dataSet, StringBuilder msgSb) {
        msgSb.append(ln()).append(ln()).append("[" + file.getName() + "]");
        for (int i = 0; i < dataSet.getTableSize(); i++) {
            final DfDataTable dataTable = dataSet.getTable(i);
            final int loadedCount = doWriteDataTable(resource, file, dataTable);
            msgSb.append(ln()).append("  " + dataTable.getTableDbName() + " (" + loadedCount + ")");
        }
    }

    // -----------------------------------------------------
    //                                             DataTable
    //                                             ---------
    protected int doWriteDataTable(DfXlsDataResource resource, File file, DfDataTable dataTable) {
        final String tableDbName = dataTable.getTableDbName();
        if (dataTable.getRowSize() == 0) {
            _log.info("*Not found row at the table: " + tableDbName);
            return 0;
        }

        final Map<String, DfColumnMeta> columnMetaMap = getColumnMetaMap(tableDbName);
        if (columnMetaMap.isEmpty()) {
            throwTableNotFoundException(file, tableDbName);
        }

        beforeHandlingTable(tableDbName, columnMetaMap);
        checkHeaderColumnIfNeeds(resource, file, dataTable, columnMetaMap);
        final List<String> columnNameList = extractColumnNameList(dataTable);

        final String dataDirectory = resource.getDataDirectory();
        final LoggingInsertType loggingInsertType = getLoggingInsertType(dataDirectory);
        final boolean suppressBatchUpdate = isMergedSuppressBatchUpdate(resource.getDataDirectory());
        Connection conn = null;
        PreparedStatement ps = null;
        String preparedSql = null;
        SQLException retryEx = null;
        DfDataRow retryDataRow = null;
        try {
            conn = _dataSource.getConnection();
            int loadedRowCount = 0;
            final int rowSize = dataTable.getRowSize();
            boolean existsEmptyRow = false;
            for (int i = 0; i < rowSize; i++) {
                final DfDataRow dataRow = dataTable.getRow(i);
                if (ps == null) {
                    final MyCreatedState myCreatedState = new MyCreatedState();
                    preparedSql = myCreatedState.buildPreparedSql(dataRow);
                    ps = conn.prepareStatement(preparedSql);
                }
                if (doWriteDataRow(resource, file, dataTable, dataRow // basic resources
                        , columnMetaMap // meta data
                        , conn, ps // JDBC resources
                        , loggingInsertType, suppressBatchUpdate)) { // option
                    ++loadedRowCount;
                    if (existsEmptyRow) {
                        final int emptyRowNumber = dataRow.getRowNumber() - 1;
                        throwXlsDataEmptyRowDataException(dataDirectory, file, dataTable, emptyRowNumber);
                    }
                } else {
                    existsEmptyRow = true;
                }
            }
            if (existsEmptyRow) {
                _log.info("...Skipping the terminal garbage row");
            }
            if (!suppressBatchUpdate) {
                boolean beginTransaction = false;
                boolean transactionClosed = false;
                try {
                    conn.setAutoCommit(false); // transaction to retry after
                    beginTransaction = true;
                    ps.executeBatch();
                    conn.commit();
                    transactionClosed = true;
                } catch (SQLException e) {
                    conn.rollback();
                    transactionClosed = true;
                    if (!(e instanceof BatchUpdateException)) {
                        throw e;
                    }
                    _log.info("...Retrying by suppressing batch update: " + tableDbName);
                    final PreparedStatement retryPs = conn.prepareStatement(preparedSql);
                    for (int i = 0; i < rowSize; i++) {
                        final DfDataRow dataRow = dataTable.getRow(i);
                        try {
                            doWriteDataRow(resource, file, dataTable, dataRow // basic resources
                                    , columnMetaMap // meta data
                                    , conn, retryPs // JDBC resources
                                    , LoggingInsertType.NONE, true); // option (no logging and suppress batch)
                        } catch (SQLException rowEx) {
                            retryEx = rowEx;
                            retryDataRow = dataRow;
                            break;
                        }
                    }
                    try {
                        retryPs.close();
                    } catch (SQLException ignored) {
                    }
                    throw e;
                } finally {
                    if (!transactionClosed) {
                        conn.rollback(); // for other exceptions
                    }
                    if (beginTransaction) {
                        conn.setAutoCommit(true);
                    }
                }
            }
            noticeLoadedRowSize(tableDbName, loadedRowCount);
            checkImplicitClassification(file, tableDbName, columnNameList);
            return loadedRowCount;
        } catch (RuntimeException e) {
            handleXlsDataRegistartionFailureException(dataDirectory, file, tableDbName, e);
            return -1; // unreachable
        } catch (SQLException e) {
            handleWriteTableException(dataDirectory, file, dataTable, e, retryEx, retryDataRow, columnNameList);
            return -1; // unreachable
        } finally {
            closeResource(conn, ps);

            // process after (finally) handling table
            finallyHandlingTable(tableDbName, columnMetaMap);
        }
    }

    protected void throwTableNotFoundException(File file, String tableDbName) {
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

    protected List<String> extractColumnNameList(DfDataTable dataTable) {
        final List<String> columnNameList = new ArrayList<String>(); // for small function
        for (int i = 0; i < dataTable.getColumnSize(); i++) {
            final DfDataColumn dataColumn = dataTable.getColumn(i);
            if (!dataColumn.isWritable()) {
                continue;
            }
            final String columnName = dataColumn.getColumnDbName();
            columnNameList.add(columnName);
        }
        return columnNameList;
    }

    protected void checkHeaderColumnIfNeeds(DfXlsDataResource resource, File file, DfDataTable dataTable,
            Map<String, DfColumnMeta> columnMetaMap) {
        final String dataDirectory = resource.getDataDirectory();
        if (!isCheckColumnDefExistence(dataDirectory)) {
            return;
        }
        final List<String> columnDefNameList = new ArrayList<String>();
        for (int i = 0; i < dataTable.getColumnSize(); i++) { // all columns are target
            final DfDataColumn dataColumn = dataTable.getColumn(i);
            final String columnName = dataColumn.getColumnDbName();
            columnDefNameList.add(columnName);
        }
        // use columnMetaMap to check (not use DataTable's meta data here)
        // at old age, columnMetaMap is not required but required now
        final String tableDbName = dataTable.getTableDbName();
        checkColumnDefExistence(dataDirectory, file, tableDbName, columnDefNameList, columnMetaMap);
    }

    protected void beforeHandlingTable(String tableDbName, Map<String, DfColumnMeta> columnInfoMap) {
        if (_dataWritingInterceptor != null) {
            _dataWritingInterceptor.processBeforeHandlingTable(tableDbName, columnInfoMap);
        }
    }

    protected void finallyHandlingTable(String tableDbName, Map<String, DfColumnMeta> columnInfoMap) {
        if (_dataWritingInterceptor != null) {
            _dataWritingInterceptor.processFinallyHandlingTable(tableDbName, columnInfoMap);
        }
    }

    protected void throwXlsDataEmptyRowDataException(String dataDirectory, File file, DfDataTable dataTable,
            int rowNumber) {
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

    protected void handleXlsDataRegistartionFailureException(String dataDirectory, File file, String tableDbName,
            RuntimeException e) {
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

    protected void handleWriteTableException(String dataDirectory, File file, DfDataTable dataTable // basic
            , SQLException mainEx // an exception of main process
            , SQLException retryEx, DfDataRow retryDataRow // retry
            , List<String> columnNameList) { // supplement
        final SQLException nextEx = mainEx.getNextException();
        if (nextEx != null && !mainEx.equals(nextEx)) { // focus on next exception
            _log.warn("*Failed to register the xls data: " + mainEx.getMessage()); // trace just in case
            mainEx = nextEx; // switch
        }
        final String tableDbName = dataTable.getTableDbName();
        final String msg = buildWriteFailureMessage(dataDirectory, file, tableDbName, mainEx, retryEx, retryDataRow,
                columnNameList);
        throw new DfXlsDataRegistrationFailureException(msg, mainEx);
    }

    protected String buildWriteFailureMessage(String dataDirectory, File file, String tableDbName // basic
            , SQLException mainEx // an exception of main process
            , SQLException retryEx, DfDataRow retryDataRow // retry
            , List<String> columnNameList) { // supplement
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to register the table data.");
        br.addItem("Advice");
        br.addElement("Please confirm the SQLException message.");
        final String advice = askAdvice(mainEx, getBasicProperties().getCurrentDBDef());
        if (advice != null && advice.trim().length() > 0) {
            br.addElement("*" + advice);
        }
        br.addItem("Data Directory");
        br.addElement(dataDirectory);
        br.addItem("Xls File");
        br.addElement(file);
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
        final Map<String, Class<?>> bindTypeCacheMap = _bindTypeCacheMap.get(tableDbName);
        final Map<String, StringProcessor> stringProcessorCacheMap = _stringProcessorCacheMap.get(tableDbName);
        if (bindTypeCacheMap != null) {
            br.addItem("Bind Type");
            final Set<Entry<String, Class<?>>> entrySet = bindTypeCacheMap.entrySet();
            for (Entry<String, Class<?>> entry : entrySet) {
                final String columnName = entry.getKey();
                StringProcessor processor = null;
                if (stringProcessorCacheMap != null) {
                    processor = stringProcessorCacheMap.get(columnName);
                }
                final String bindType = entry.getValue().getName();
                final String processorExp = (processor != null ? " (" + processor + ")" : "");
                br.addElement(columnName + " = " + bindType + processorExp);
            }
        }
        return br.buildExceptionMessage();
    }

    protected String askAdvice(SQLException sqlEx, DBDef dbdef) {
        return _adviser.askAdvice(sqlEx, dbdef);
    }

    protected void closeResource(Connection conn, PreparedStatement ps) {
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException ignored) {
                _log.info("Statement#close() threw the exception!", ignored);
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ignored) {
                _log.info("Connection#close() threw the exception!", ignored);
            }
        }
    }

    // -----------------------------------------------------
    //                                               DataRow
    //                                               -------
    protected boolean doWriteDataRow(DfXlsDataResource resource, File file, DfDataTable dataTable, DfDataRow dataRow,
            Map<String, DfColumnMeta> columnMetaMap, Connection conn, PreparedStatement ps,
            LoggingInsertType loggingInsertType, boolean suppressBatchUpdate) throws SQLException {
        final String tableDbName = dataTable.getTableDbName();
        final ColumnContainer columnContainer = createColumnContainer(dataTable, dataRow);
        final String dataDirectory = resource.getDataDirectory();
        final Map<String, Object> columnValueMap = columnContainer.getColumnValueMap();
        if (columnValueMap.isEmpty()) {
            throwXlsDataColumnDefFailureException(dataDirectory, file, dataTable);
        }
        if (isColumnValueAllNullOrEmpty(columnValueMap)) { // against Excel Devil
            return false;
        }
        final Set<String> sysdateColumnSet = extractSysdateColumnSet(dataDirectory, columnValueMap);
        convertColumnValueIfNeeds(dataDirectory, tableDbName, columnValueMap, columnMetaMap);
        final int rowNumber = dataRow.getRowNumber();
        resolveRelativeDate(dataDirectory, tableDbName, columnValueMap, columnMetaMap, sysdateColumnSet, rowNumber);
        handleLoggingInsert(tableDbName, columnValueMap, loggingInsertType, rowNumber);

        int bindCount = 1;
        for (Entry<String, Object> entry : columnValueMap.entrySet()) {
            final String columnName = entry.getKey();
            final Object obj = entry.getValue();
            processBindColumnValue(resource, dataDirectory, file, tableDbName, columnName, obj, conn, ps, bindCount,
                    columnMetaMap, rowNumber);
            bindCount++;
        }
        if (suppressBatchUpdate) {
            ps.execute();
        } else {
            ps.addBatch();
        }
        return true;
    }

    protected void throwXlsDataColumnDefFailureException(String dataDirectory, File file, DfDataTable dataTable) {
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

    protected Set<String> extractSysdateColumnSet(String dataDirectory, Map<String, Object> columnValueMap) { // should be called before convert
        final Map<String, String> defaultValueMap = getDefaultValueMap(dataDirectory);
        return _defaultValueProp.extractSysdateColumnSet(columnValueMap, defaultValueMap);
    }

    protected boolean isColumnValueAllNullOrEmpty(Map<String, Object> plainMap) { // default columns should not be contained
        for (Object value : plainMap.values()) {
            if (value != null && !value.equals("")) {
                return false;
            }
        }
        return true; // all columns are null or empty so invalid row
    }

    protected void processBindColumnValue(DfXlsDataResource resource, String dataDirectory, File file,
            String tableDbName, String columnName, Object obj, Connection conn, PreparedStatement ps, int bindCount,
            Map<String, DfColumnMeta> columnMetaMap, int rowNumber) throws SQLException {
        // - - - - - - - - - - - - - - - - - - -
        // Process Null (against Null Headache)
        // - - - - - - - - - - - - - - - - - - -
        if (processNull(dataDirectory, tableDbName, columnName, obj, ps, bindCount, columnMetaMap, rowNumber)) {
            return;
        }

        // - - - - - - - - - - - - - - -
        // Process NotNull and NotString
        // - - - - - - - - - - - - - - -
        // If the value is not null and the value has the own type except string,
        // It registers the value to statement by the type.
        if (processNotNullNotString(dataDirectory, tableDbName, columnName, obj, conn, ps, bindCount, columnMetaMap,
                rowNumber)) {
            return;
        }

        // - - - - - - - - - - - - - - - - - - -
        // Process NotNull and StringExpression
        // - - - - - - - - - - - - - - - - - - -
        final String value = (String) obj;
        processNotNullString(dataDirectory, file, tableDbName, columnName, value, conn, ps, bindCount, columnMetaMap,
                rowNumber);
    }

    // ===================================================================================
    //                                                                        Xls Handling
    //                                                                        ============
    protected DfTableXlsReader createTableXlsReader(String dataDirectory, File file) {
        final Map<String, String> tableNameMap = getTableNameMap(dataDirectory);
        final Map<String, List<String>> notTrimTableColumnMap = getNotTrimTableColumnMap(dataDirectory);
        final Map<String, List<String>> emptyStringTableColumnMap = getEmptyStringTableColumnMap(dataDirectory);
        final boolean rtrimCellValue = isRTrimCellValue(dataDirectory);
        return new DfTableXlsReader(file, tableNameMap, notTrimTableColumnMap, emptyStringTableColumnMap,
                _skipSheetPattern, rtrimCellValue);
    }

    protected List<File> getXlsList(DfXlsDataResource resource) {
        final Comparator<File> fileNameAscComparator = new Comparator<File>() {
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        };
        final SortedSet<File> sortedFileSet = new TreeSet<File>(fileNameAscComparator);

        final String dataDirectory = resource.getDataDirectory();
        final File dir = new File(dataDirectory);
        final FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".xls");
            }
        };
        final File[] listFiles = dir.listFiles(filter);
        if (listFiles == null) {
            return new ArrayList<File>();
        }
        for (File file : listFiles) {
            sortedFileSet.add(file);
        }
        return new ArrayList<File>(sortedFileSet);
    }

    protected void filterValidColumn(final DfDataSet dataSet) {
        for (int i = 0; i < dataSet.getTableSize(); i++) {
            final DfDataTable table = dataSet.getTable(i);
            final String tableName = table.getTableDbName();

            final Map<String, DfColumnMeta> metaMetaMap = getColumnMetaMap(tableName);
            for (int j = 0; j < table.getColumnSize(); j++) {
                final DfDataColumn dataColumn = table.getColumn(j);
                if (!metaMetaMap.containsKey(dataColumn.getColumnDbName())) {
                    dataColumn.setWritable(false);
                }
            }
        }
    }

    // ===================================================================================
    //                                                                 Column Value Filter
    //                                                                 ===================
    protected void convertColumnValueIfNeeds(String dataDirectory, String tableName,
            Map<String, Object> columnValueMap, Map<String, DfColumnMeta> columnMetaMap) {
        // handling both convertValueMap and defaultValueMap
        final Map<String, Map<String, String>> convertValueMap = getConvertValueMap(dataDirectory);
        final Map<String, String> defaultValueMap = getDefaultValueMap(dataDirectory);
        // empty string has already been converted to null basically
        // so it does not need to convert here
        if ((convertValueMap == null || convertValueMap.isEmpty()) // no convert
                && (defaultValueMap == null || defaultValueMap.isEmpty())) { // and no default
            return;
        }
        final DfColumnValueConverter converter = new DfColumnValueConverter(convertValueMap, defaultValueMap,
                new DfColumnBindTypeProvider() {
                    public Class<?> provide(String tableName, DfColumnMeta columnMeta) {
                        return getBindType(tableName, columnMeta);
                    }
                });
        converter.convert(tableName, columnValueMap, columnMetaMap);
    }

    protected void setupDefaultValue(String dataDirectory, final DfDataSet dataSet) {
        final Map<String, String> defaultValueMap = getDefaultValueMap(dataDirectory);
        for (int i = 0; i < dataSet.getTableSize(); i++) {
            final DfDataTable table = dataSet.getTable(i);
            final Set<String> defaultValueMapKeySet = defaultValueMap.keySet();
            final String tableName = table.getTableDbName();

            final Map<String, DfColumnMeta> metaMetaMap = getColumnMetaMap(tableName);
            for (String defaultTargetColumnName : defaultValueMapKeySet) {
                final String defaultValue = defaultValueMap.get(defaultTargetColumnName);

                if (metaMetaMap.containsKey(defaultTargetColumnName) && !table.hasColumn(defaultTargetColumnName)) {
                    // values are resolved later so resolve type only here
                    final DfDtsColumnType columnType;
                    if (defaultValue.equalsIgnoreCase("sysdate")) {
                        columnType = DfDtsColumnTypes.TIMESTAMP;
                    } else {
                        columnType = DfDtsColumnTypes.STRING;
                    }
                    table.addColumn(defaultTargetColumnName, columnType);

                    for (int j = 0; j < table.getRowSize(); j++) {
                        final DfDataRow row = table.getRow(j);
                        row.addValue(defaultTargetColumnName, null); // value is set later
                    }
                }
            }
        }
    }

    // ===================================================================================
    //                                                              Convert Value Property
    //                                                              ======================
    protected Map<String, Map<String, Map<String, String>>> _convertValueMapMap = DfCollectionUtil.newHashMap();

    protected Map<String, Map<String, String>> getConvertValueMap(String dataDirectory) {
        final Map<String, Map<String, String>> cachedMap = _convertValueMapMap.get(dataDirectory);
        if (cachedMap != null) {
            return cachedMap;
        }
        _convertValueMapMap.put(dataDirectory, doGetConvertValueMap(dataDirectory));
        return _convertValueMapMap.get(dataDirectory);
    }

    public static Map<String, Map<String, String>> doGetConvertValueMap(String dataDirectory) {
        final DfOutsideMapPropReader reader = new DfOutsideMapPropReader();
        String path = dataDirectory + "/convertValueMap.dataprop";
        final Map<String, Map<String, String>> resultMap = StringKeyMap.createAsFlexibleOrdered();
        Map<String, Map<String, String>> readMap = reader.readMapAsStringMapValue(path);
        if (readMap != null && !readMap.isEmpty()) {
            resultMap.putAll(readMap);
        } else {
            path = dataDirectory + "/convert-value.txt";
            readMap = reader.readMapAsStringMapValue(path);
            resultMap.putAll(readMap);
        }
        return resolveControlCharacter(resultMap);
    }

    protected static Map<String, Map<String, String>> resolveControlCharacter(
            Map<String, Map<String, String>> convertValueMap) {
        final Map<String, Map<String, String>> resultMap = StringKeyMap.createAsFlexibleOrdered();
        for (Entry<String, Map<String, String>> entry : convertValueMap.entrySet()) {
            final Map<String, String> elementMap = DfCollectionUtil.newLinkedHashMap();
            for (Entry<String, String> nextEntry : entry.getValue().entrySet()) {
                final String key = resolveControlCharacter(nextEntry.getKey());
                final String value = resolveControlCharacter(nextEntry.getValue());
                elementMap.put(key, value);
            }
            resultMap.put(entry.getKey(), elementMap);
        }
        return resultMap;
    }

    protected static String resolveControlCharacter(String value) {
        if (value == null) {
            return null;
        }
        final String tmp = "${df:temporaryVariable}";
        value = Srl.replace(value, "\\\\", tmp); // "\\" to "\" later

        // e.g. pure string "\n" to (real) line separator
        value = Srl.replace(value, "\\r", "\r");
        value = Srl.replace(value, "\\n", "\n");
        value = Srl.replace(value, "\\t", "\t");

        value = Srl.replace(value, tmp, "\\");
        return value;
    }

    // ===================================================================================
    //                                                              Default Value Property
    //                                                              ======================
    // map for delimiter data is defined at the handler
    protected Map<String, Map<String, String>> _defaultValueMapMap = DfCollectionUtil.newHashMap();

    protected Map<String, String> getDefaultValueMap(String dataDirectory) {
        final Map<String, String> cachedMap = _defaultValueMapMap.get(dataDirectory);
        if (cachedMap != null) {
            return cachedMap;
        }
        final StringKeyMap<String> flmap = doGetDefaultValueMap(dataDirectory);
        _defaultValueMapMap.put(dataDirectory, flmap);
        return _defaultValueMapMap.get(dataDirectory);
    }

    public static StringKeyMap<String> doGetDefaultValueMap(String dataDirectory) { // recycle
        final DfOutsideMapPropReader reader = new DfOutsideMapPropReader();
        String path = dataDirectory + "/defaultValueMap.dataprop";
        Map<String, String> resultMap = reader.readMapAsStringValue(path);
        if (resultMap == null || resultMap.isEmpty()) {
            path = dataDirectory + "/default-value.txt"; // old style
            resultMap = reader.readMapAsStringValue(path);
        }
        // ordered here because of columns added
        final StringKeyMap<String> flmap = StringKeyMap.createAsFlexibleOrdered();
        flmap.putAll(resultMap);
        return flmap;
    }

    // ===================================================================================
    //                                                                 Table Name Property
    //                                                                 ===================
    protected final DfTableNameProp _tableNameProp = new DfTableNameProp();

    protected Map<String, String> getTableNameMap(String dataDirectory) {
        return _tableNameProp.getTableNameMap(dataDirectory);
    }

    // ===================================================================================
    //                                                                   Not Trim Property
    //                                                                   =================
    protected Map<String, Map<String, List<String>>> _notTrimTableColumnMapMap = DfCollectionUtil.newHashMap();

    protected Map<String, List<String>> getNotTrimTableColumnMap(String dataDirectory) {
        final Map<String, List<String>> cachedMap = _notTrimTableColumnMapMap.get(dataDirectory);
        if (cachedMap != null) {
            return cachedMap;
        }
        final DfOutsideMapPropReader reader = new DfOutsideMapPropReader();
        String path = dataDirectory + "/notTrimColumnMap.dataprop";
        Map<String, List<String>> resultMap = reader.readMapAsStringListValue(path);
        if (resultMap == null || resultMap.isEmpty()) {
            path = dataDirectory + "/not-trim-column.txt"; // old style
            resultMap = reader.readMapAsStringListValue(path);
        }
        final Set<Entry<String, List<String>>> entrySet = resultMap.entrySet();
        final StringKeyMap<List<String>> flmap = StringKeyMap.createAsFlexible();
        for (Entry<String, List<String>> entry : entrySet) {
            flmap.put(entry.getKey(), entry.getValue());
        }
        _notTrimTableColumnMapMap.put(dataDirectory, flmap);
        return _notTrimTableColumnMapMap.get(dataDirectory);
    }

    // ===================================================================================
    //                                                               Empty String Property
    //                                                               =====================
    protected Map<String, Map<String, List<String>>> _emptyStringColumnMapMap = DfCollectionUtil.newHashMap();

    protected Map<String, List<String>> getEmptyStringTableColumnMap(String dataDirectory) {
        final Map<String, List<String>> cachedMap = _emptyStringColumnMapMap.get(dataDirectory);
        if (cachedMap != null) {
            return cachedMap;
        }
        final DfOutsideMapPropReader reader = new DfOutsideMapPropReader();
        String path = dataDirectory + "/emptyStringColumnMap.dataprop";
        Map<String, List<String>> resultMap = reader.readMapAsStringListValue(path);
        if (resultMap == null || resultMap.isEmpty()) {
            path = dataDirectory + "/empty-string-column.txt"; // old style
            resultMap = reader.readMapAsStringListValue(path);
        }
        final Set<Entry<String, List<String>>> entrySet = resultMap.entrySet();
        final StringKeyMap<List<String>> flmap = StringKeyMap.createAsFlexible();
        for (Entry<String, List<String>> entry : entrySet) {
            flmap.put(entry.getKey(), entry.getValue());
        }
        _emptyStringColumnMapMap.put(dataDirectory, flmap);
        return _emptyStringColumnMapMap.get(dataDirectory);
    }

    // ===================================================================================
    //                                                                    Column Container
    //                                                                    ================
    protected ColumnContainer createColumnContainer(DfDataTable dataTable, DfDataRow dataRow) {
        final ColumnContainer container = new ColumnContainer();
        for (int i = 0; i < dataTable.getColumnSize(); i++) {
            final DfDataColumn dataColumn = dataTable.getColumn(i);
            if (!dataColumn.isWritable()) {
                continue;
            }
            final Object columnValue = dataRow.getValue(i);
            final String columnName = dataColumn.getColumnDbName();
            container.addColumnValue(columnName, columnValue);
            container.addColumnObject(columnName, dataColumn);
        }
        return container;
    }

    protected static class ColumnContainer {
        protected final Map<String, Object> _columnValueMap = new LinkedHashMap<String, Object>();
        protected final Map<String, DfDataColumn> _columnObjectMap = new LinkedHashMap<String, DfDataColumn>();

        public Map<String, Object> getColumnValueMap() {
            return _columnValueMap;
        }

        public void addColumnValue(String columnName, Object columnValue) {
            _columnValueMap.put(columnName, columnValue);
        }

        public Map<String, DfDataColumn> getColumnObjectMap() {
            return _columnObjectMap;
        }

        public void addColumnObject(String columnName, DfDataColumn columnObject) {
            _columnObjectMap.put(columnName, columnObject);
        }
    }

    // ===================================================================================
    //                                                                     State Extension
    //                                                                     ===============
    protected static class MyCreatedState {
        public String buildPreparedSql(final DfDataRow row) {
            final DfDtsCreatedState createdState = new DfDtsCreatedState() {
                public String toString() {
                    final DfDtsSqlContext sqlContext = getSqlContext(row);
                    return sqlContext.getSql();
                }
            };
            return createdState.toString();
        }
    }

    // ===================================================================================
    //                                                                         Result Mark
    //                                                                         ===========
    protected void outputResultMark(String outputDir, String outputMsg) {
        final StringBuilder sb = new StringBuilder();
        sb.append(ln()).append("* * * * * * * * * *");
        sb.append(ln()).append("*                 *");
        sb.append(ln()).append("* Xls Data Result *");
        sb.append(ln()).append("*                 *");
        sb.append(ln()).append("* * * * * * * * * *");
        sb.append(ln()).append("data-directory: ").append(outputDir);
        sb.append(ln());
        sb.append(ln()).append(Srl.ltrim(outputMsg));
        final File dataPropFile = new File(outputDir + "/xls-data-result.dfmark");
        if (dataPropFile.exists()) {
            dataPropFile.delete();
        }
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dataPropFile), "UTF-8"));
            bw.write(sb.toString());
            bw.flush();
        } catch (IOException e) {
            String msg = "Failed to write xls-data-result.dfmark: " + dataPropFile;
            throw new IllegalStateException(msg, e);
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    protected String ln() {
        return DBFluteSystem.getBasicLn();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setSkipSheet(String skipSheet) {
        if (skipSheet == null || skipSheet.trim().length() == 0) {
            return;
        }
        try {
            _skipSheetPattern = Pattern.compile(skipSheet);
        } catch (PatternSyntaxException e) {
            String msg = "The pattern syntax for skip-sheet was wrong: " + skipSheet;
            throw new IllegalStateException(msg, e);
        }
    }
}
