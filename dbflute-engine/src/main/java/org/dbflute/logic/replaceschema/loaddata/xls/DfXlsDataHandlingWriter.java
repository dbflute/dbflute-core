/*
 * Copyright 2014-2023 the original author or authors.
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
package org.dbflute.logic.replaceschema.loaddata.xls;

import java.io.File;
import java.io.FileFilter;
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

import org.apache.torque.engine.database.model.UnifiedSchema;
import org.dbflute.helper.dataset.DfDataColumn;
import org.dbflute.helper.dataset.DfDataRow;
import org.dbflute.helper.dataset.DfDataSet;
import org.dbflute.helper.dataset.DfDataTable;
import org.dbflute.helper.dataset.states.DfDtsCreatedState;
import org.dbflute.helper.dataset.states.DfDtsSqlContext;
import org.dbflute.helper.dataset.types.DfDtsColumnType;
import org.dbflute.helper.dataset.types.DfDtsColumnTypes;
import org.dbflute.helper.io.xls.DfTableXlsReader;
import org.dbflute.helper.io.xls.DfXlsFactory;
import org.dbflute.logic.jdbc.metadata.info.DfColumnMeta;
import org.dbflute.logic.replaceschema.loaddata.base.DfAbsractDataWriter;
import org.dbflute.logic.replaceschema.loaddata.base.DfLoadedDataInfo;
import org.dbflute.logic.replaceschema.loaddata.base.dataprop.DfLoadingControlProp.LoggingInsertType;
import org.dbflute.logic.replaceschema.loaddata.base.secretary.DfColumnBindTypeProvider;
import org.dbflute.logic.replaceschema.loaddata.base.secretary.DfColumnValueConverter;
import org.dbflute.logic.replaceschema.loaddata.xls.dataprop.DfEmptyStringColumnProp;
import org.dbflute.logic.replaceschema.loaddata.xls.dataprop.DfNotTrimTableColumnProp;
import org.dbflute.logic.replaceschema.loaddata.xls.dataprop.DfTableNameProp;
import org.dbflute.logic.replaceschema.loaddata.xls.secretary.DfXlsDataOutputResultMarker;
import org.dbflute.logic.replaceschema.loaddata.xls.secretary.DfXlsDataWritingExceptionThrower;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The XLS data writer with basic handling, which can be handler.
 * @author jflute
 * @author p1us2er0
 */
public class DfXlsDataHandlingWriter extends DfAbsractDataWriter implements DfXlsDataHandler {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The logger instance for this class. (NotNull) */
    private static final Logger _log = LoggerFactory.getLogger(DfXlsDataHandlingWriter.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The pattern of skip sheet. (NullAllowed) */
    protected Pattern _skipSheetPattern;

    protected final DfTableNameProp _tableNameProp = new DfTableNameProp(); // xls only
    protected final DfNotTrimTableColumnProp _notTrimTableColumnProp = new DfNotTrimTableColumnProp(); // xls only
    protected final DfEmptyStringColumnProp _emptyStringColumnProp = new DfEmptyStringColumnProp(); // xls only

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfXlsDataHandlingWriter(DataSource dataSource, UnifiedSchema unifiedSchema) {
        super(dataSource, unifiedSchema);
    }

    public void acceptSkipSheet(String skipSheet) {
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

    // ===================================================================================
    //                                                                                Read
    //                                                                                ====
    // unused so comment out by jflute (2021/01/18)
    //public List<DfDataSet> readSeveralData(DfXlsDataResource resource) {
    //    final String dataDirectory = resource.getDataDirectory();
    //    final List<File> xlsList = findXlsList(resource);
    //    final List<DfDataSet> ls = new ArrayList<DfDataSet>();
    //    for (File file : xlsList) {
    //        final DfTableXlsReader xlsReader = createTableXlsReader(dataDirectory, file);
    //        ls.add(xlsReader.read());
    //    }
    //    return ls;
    //}

    // ===================================================================================
    //                                                                               Write
    //                                                                               =====
    public void writeSeveralData(DfXlsDataResource resource, DfLoadedDataInfo loadedDataInfo) {
        final String dataDirectory = resource.getDataDirectory();
        final List<File> xlsList = findXlsList(resource);
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
        markOutputResult(resource.getDataDirectory(), msgSb.toString());
    }

    protected void prepareImplicitClassificationLazyCheck(DfLoadedDataInfo info) {
        info.acceptImplicitClassificationLazyCheck(getImplicitClassificationLazyCheckerList());
    }

    // -----------------------------------------------------
    //                                               DataSet
    //                                               -------
    protected void doWriteDataSet(DfXlsDataResource resource, File file, DfDataSet dataSet, StringBuilder msgSb) {
        msgSb.append(ln()).append(ln()).append("[").append(file.getName()).append("]");
        for (int i = 0; i < dataSet.getTableSize(); i++) {
            final DfDataTable dataTable = dataSet.getTable(i);
            final int loadedCount = doWriteDataTable(resource, file, dataTable);
            msgSb.append(ln()).append("  ").append(dataTable.getTableDbName()).append(" (").append(loadedCount).append(")");
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
                    } catch (SQLException ignored) {}
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
            handleWriteTableFailureException(dataDirectory, file, tableDbName, e);
            return -1; // unreachable
        } catch (SQLException e) {
            handleWriteTableSQLException(dataDirectory, file, dataTable, e, retryEx, retryDataRow, columnNameList);
            return -1; // unreachable
        } finally {
            closeResource(conn, ps);

            // process after (finally) handling table
            finallyHandlingTable(tableDbName, columnMetaMap);
        }
    }

    protected void throwTableNotFoundException(File file, String tableDbName) {
        createThrower().throwTableNotFoundException(file, tableDbName);
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
        if (!isCheckColumnDef(dataDirectory)) {
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
        checkColumnDef(file, dataTable.getTableDbName(), columnDefNameList, columnMetaMap);
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

    protected void throwXlsDataEmptyRowDataException(String dataDirectory, File file, DfDataTable dataTable, int rowNumber) {
        createThrower().throwXlsDataEmptyRowDataException(dataDirectory, file, dataTable, rowNumber);
    }

    protected void handleWriteTableFailureException(String dataDirectory, File file, String tableDbName, RuntimeException e) {
        createThrower().handleWriteTableFailureException(dataDirectory, file, tableDbName, e);
    }

    protected void handleWriteTableSQLException(String dataDirectory, File file, DfDataTable dataTable // basic
            , SQLException mainEx // an exception of main process
            , SQLException retryEx, DfDataRow retryDataRow // retry
            , List<String> columnNameList) { // supplement
        createThrower().handleWriteTableSQLException(dataDirectory, file, dataTable, mainEx, retryEx, retryDataRow, columnNameList,
                _bindTypeCacheMap, _stringProcessorCacheMap);
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
            Map<String, DfColumnMeta> columnMetaMap, Connection conn, PreparedStatement ps, LoggingInsertType loggingInsertType,
            boolean suppressBatchUpdate) throws SQLException {
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
            processBindColumnValue(resource, dataDirectory, file, tableDbName, columnName, obj, conn, ps, bindCount, columnMetaMap,
                    rowNumber);
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
        createThrower().throwXlsDataColumnDefFailureException(dataDirectory, file, dataTable);
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

    protected void processBindColumnValue(DfXlsDataResource resource, String dataDirectory, File file, String tableDbName,
            String columnName, Object obj, Connection conn, PreparedStatement ps, int bindCount, Map<String, DfColumnMeta> columnMetaMap,
            int rowNumber) throws SQLException {
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
        if (processNotNullNotString(dataDirectory, tableDbName, columnName, obj, conn, ps, bindCount, columnMetaMap, rowNumber)) {
            return;
        }

        // - - - - - - - - - - - - - - - - - - -
        // Process NotNull and StringExpression
        // - - - - - - - - - - - - - - - - - - -
        final String value = (String) obj;
        processNotNullString(dataDirectory, file, tableDbName, columnName, value, conn, ps, bindCount, columnMetaMap, rowNumber);
    }

    // ===================================================================================
    //                                                                        Xls Handling
    //                                                                        ============
    protected DfTableXlsReader createTableXlsReader(String dataDirectory, File file) {
        final Map<String, String> tableNameMap = getTableNameMap(dataDirectory);
        final Map<String, List<String>> notTrimTableColumnMap = getNotTrimTableColumnMap(dataDirectory);
        final Map<String, List<String>> emptyStringTableColumnMap = getEmptyStringTableColumnMap(dataDirectory);
        final boolean rtrimCellValue = isRTrimCellValue(dataDirectory);
        return new DfTableXlsReader(file, tableNameMap, notTrimTableColumnMap, emptyStringTableColumnMap, _skipSheetPattern,
                rtrimCellValue);
    }

    protected List<File> findXlsList(DfXlsDataResource resource) {
        final Comparator<File> fileNameAscComparator = new Comparator<File>() {
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        };
        final SortedSet<File> sortedFileSet = new TreeSet<File>(fileNameAscComparator);

        final String dataDirectory = resource.getDataDirectory();
        final File dir = new File(dataDirectory);

        final FileFilter fileFilter = DfXlsFactory.instance().createXlsFileFilter();
        final File[] listFiles = dir.listFiles(fileFilter);
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
    protected void convertColumnValueIfNeeds(String dataDirectory, String tableName, Map<String, Object> columnValueMap,
            Map<String, DfColumnMeta> columnMetaMap) {
        // handling both convertValueMap and defaultValueMap
        final Map<String, Map<String, String>> convertValueMap = getConvertValueMap(dataDirectory);
        final Map<String, String> defaultValueMap = getDefaultValueMap(dataDirectory);
        // empty string has already been converted to null basically
        // so it does not need to convert here
        if ((convertValueMap == null || convertValueMap.isEmpty()) // no convert
                && (defaultValueMap == null || defaultValueMap.isEmpty())) { // and no default
            return;
        }
        final DfColumnBindTypeProvider bindTypeProvider = new DfColumnBindTypeProvider() {
            public Class<?> provide(String tableName, DfColumnMeta columnMeta) {
                return getBindType(tableName, columnMeta);
            }
        };
        final DfColumnValueConverter converter = new DfColumnValueConverter(convertValueMap, defaultValueMap, bindTypeProvider);
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
    //                                                                       Output Result
    //                                                                       =============
    protected void markOutputResult(String outputDir, String outputMsg) {
        new DfXlsDataOutputResultMarker().markOutputResult(outputDir, outputMsg);
    }

    // ===================================================================================
    //                                                                   Exception Thrower
    //                                                                   =================
    protected DfXlsDataWritingExceptionThrower createThrower() {
        return new DfXlsDataWritingExceptionThrower();
    }

    // ===================================================================================
    //                                                                       Data Property
    //                                                                       =============
    protected Map<String, String> getTableNameMap(String dataDirectory) {
        return _tableNameProp.findTableNameMap(dataDirectory);
    }

    protected Map<String, List<String>> getNotTrimTableColumnMap(String dataDirectory) {
        return _notTrimTableColumnProp.findNotTrimTableColumnMap(dataDirectory);
    }

    protected Map<String, List<String>> getEmptyStringTableColumnMap(String dataDirectory) {
        return _emptyStringColumnProp.findEmptyStringTableColumnMap(dataDirectory);
    }
}
