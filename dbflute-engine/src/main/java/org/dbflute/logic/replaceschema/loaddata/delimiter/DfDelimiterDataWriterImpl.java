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
package org.dbflute.logic.replaceschema.loaddata.delimiter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.torque.engine.database.model.UnifiedSchema;
import org.dbflute.exception.DfDelimiterDataRegistrationFailureException;
import org.dbflute.exception.DfDelimiterDataRegistrationFailureException.DfDelimiterDataRegistrationRetryResource;
import org.dbflute.exception.DfDelimiterDataRegistrationFailureException.DfDelimiterDataRegistrationRowSnapshot;
import org.dbflute.exception.DfJDBCException;
import org.dbflute.helper.StringKeyMap;
import org.dbflute.logic.jdbc.metadata.info.DfColumnMeta;
import org.dbflute.logic.replaceschema.loaddata.base.DfAbsractDataWriter;
import org.dbflute.logic.replaceschema.loaddata.base.dataprop.DfLoadingControlProp.LoggingInsertType;
import org.dbflute.logic.replaceschema.loaddata.base.secretary.DfColumnBindTypeProvider;
import org.dbflute.logic.replaceschema.loaddata.delimiter.line.DfDelimiterDataFirstLineAnalyzer;
import org.dbflute.logic.replaceschema.loaddata.delimiter.line.DfDelimiterDataFirstLineInfo;
import org.dbflute.logic.replaceschema.loaddata.delimiter.line.DfDelimiterDataLineDirectFilter;
import org.dbflute.logic.replaceschema.loaddata.delimiter.line.DfDelimiterDataListedColumnHandler;
import org.dbflute.logic.replaceschema.loaddata.delimiter.line.DfDelimiterDataValueLineAnalyzer;
import org.dbflute.logic.replaceschema.loaddata.delimiter.line.DfDelimiterDataValueLineInfo;
import org.dbflute.logic.replaceschema.loaddata.delimiter.secretary.DfDelimiterDataJdbcHandler;
import org.dbflute.logic.replaceschema.loaddata.delimiter.secretary.DfDelimiterDataTableDbNameExtractor;
import org.dbflute.logic.replaceschema.loaddata.delimiter.secretary.DfDelimiterDataWritingExceptionThrower;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 */
public class DfDelimiterDataWriterImpl extends DfAbsractDataWriter implements DfDelimiterDataWriter {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The logger instance for this class. (NotNull) */
    private static final Logger _log = LoggerFactory.getLogger(DfDelimiterDataWriterImpl.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // these are not null after initialization
    protected String _filePath;
    protected String _encoding;
    protected String _delimiter;
    protected Map<String, Map<String, String>> _convertValueMap;
    protected Map<String, String> _defaultValueMap;

    /** The cache map of meta info. The key is table name. (NotNull) */
    protected final Map<String, Map<String, DfColumnMeta>> _metaInfoCacheMap = StringKeyMap.createAsFlexible();

    // cached, only objects repeatable called
    protected final DfDelimiterDataJdbcHandler _jdbcHandler = new DfDelimiterDataJdbcHandler();
    protected final DfDelimiterDataLineDirectFilter _lineDirectFilter = new DfDelimiterDataLineDirectFilter();
    protected final DfDelimiterDataValueLineAnalyzer _valueLineAnalyzer = new DfDelimiterDataValueLineAnalyzer();

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfDelimiterDataWriterImpl(DataSource dataSource, UnifiedSchema unifiedSchema) {
        super(dataSource, unifiedSchema);
    }

    // ===================================================================================
    //                                                                               Write
    //                                                                               =====
    public void writeData(DfDelimiterDataResultInfo resultInfo) throws IOException {
        _log.info("/= = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = ");
        _log.info("writeData(" + _filePath + ")");
        _log.info("= = = = = = =/");
        try {
            doWriteData(resultInfo, /*forcedlySuppressBatch*/false, /*offsetRowCount*/0);
        } catch (DfDelimiterDataRegistrationFailureException e) {
            if (needsRetry(e)) {
                performNonBatchRetry(resultInfo, e);
                throw e;
            } else {
                throw e;
            }
        }
    }

    // -----------------------------------------------------
    //                                                 Retry
    //                                                 -----
    protected boolean needsRetry(DfDelimiterDataRegistrationFailureException failureEx) {
        final DfDelimiterDataRegistrationRetryResource resource = failureEx.getRetryResource();
        return resource != null && resource.canBatchUpdate() && failureEx.getCause() instanceof SQLException;
    }

    protected void performNonBatchRetry(DfDelimiterDataResultInfo resultInfo, DfDelimiterDataRegistrationFailureException failureEx) {
        final int committedRowCount = failureEx.getRetryResource().getCommittedRowCount(); // as offsetRowCount
        try {
            _log.info("...Retrying as non-batch to derive target row");
            doWriteData(resultInfo, /*forcedlySuppressBatch*/true, committedRowCount); // always throw
            // basically no way here, however retry success might not be real success
            // (e.g. error of retry itself) so throw it
        } catch (Exception somethingEx) {
            if (somethingEx instanceof DfDelimiterDataRegistrationFailureException) {
                final DfDelimiterDataRegistrationFailureException retryEx = (DfDelimiterDataRegistrationFailureException) somethingEx;
                final Throwable retryCause = retryEx.getCause();
                if (retryCause instanceof SQLException) {
                    handleRetryStory(failureEx, retryEx, retryCause);
                }
            }
        }
    }

    protected void handleRetryStory(DfDelimiterDataRegistrationFailureException failureEx,
            DfDelimiterDataRegistrationFailureException retryEx, Throwable retryCause) {
        final StringBuilder sb = new StringBuilder();
        final DfDelimiterDataRegistrationRowSnapshot retrySnapshot = retryEx.getRowSnapshot();
        sb.append(retryCause.getClass().getName());
        sb.append(ln()).append(retryCause.getMessage());
        if (retrySnapshot != null) { // basically exists, but just in case
            sb.append(ln()).append("/- - - - - - - - - - - - - - - - - - - -");
            sb.append(ln()).append("Column Def: ").append(retrySnapshot.getColumnNameList());
            sb.append(ln()).append("Row Values: ").append(retrySnapshot.getColumnValueList());
            sb.append(ln()).append("Row Number: ").append(retrySnapshot.getCurrentRowNumber());
            sb.append(ln()).append("- - - - - - - - - -/");
        }
        failureEx.tellRetryStory(sb.toString()); // reflect retry information to exception message
    }

    // -----------------------------------------------------
    //                                            Write Data
    //                                            ----------
    protected void doWriteData(DfDelimiterDataResultInfo resultInfo, boolean forcedlySuppressBatch, int offsetRowCount) throws IOException {
        final String dataDirectory = Srl.substringLastFront(_filePath, "/");
        final LoggingInsertType loggingInsertType = getLoggingInsertType(dataDirectory);
        final String tableDbName = extractTableDbName();
        final Map<String, DfColumnMeta> columnMetaMap = getColumnMetaMap(tableDbName);
        if (columnMetaMap.isEmpty()) {
            throwTableNotFoundException(_filePath, tableDbName);
        }

        // process before handling table
        beforeHandlingTable(tableDbName, columnMetaMap);

        final File dataFile = new File(_filePath);
        final boolean canBatchUpdate = canBatchUpdate(forcedlySuppressBatch, dataDirectory);

        String lineString = null;
        String preContinueString = null;

        final List<String> columnNameList = new ArrayList<String>();
        final List<String> columnValueList = new ArrayList<String>();
        List<String> valueListSnapshot = null;

        int rowNumber = 0; // not line on file, as registered record
        String executedSql = null;
        int committedRowCount = 0; // may committed per limit size, for skip in retry

        FileInputStream fis = null;
        InputStreamReader ir = null;
        BufferedReader br = null;
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            fis = new FileInputStream(dataFile);
            ir = new InputStreamReader(fis, _encoding);
            br = new BufferedReader(ir);

            DfDelimiterDataFirstLineInfo firstLineInfo = null;
            int loopIndex = -1;
            int addedBatchSize = 0; // current registered size to prepared statement
            while (true) {
                ++loopIndex;

                lineString = br.readLine();
                if (lineString == null) {
                    break;
                }

                // /- - - - - - - - - - - - - - - - - - - - - -
                // initialize column definition from first line
                // - - - - - - - - - -/
                if (loopIndex == 0) {
                    firstLineInfo = analyzeFirstLine(lineString, _delimiter);
                    setupColumnNameList(columnNameList, dataDirectory, dataFile, tableDbName, firstLineInfo, columnMetaMap);
                    continue;
                }

                // /- - - - - - - - - - - - - - -
                // analyze values in line strings
                // - - - - - - - - - -/
                lineString = filterLineString(lineString);
                {
                    if (preContinueString != null && !preContinueString.equals("")) {
                        // #hope performance tuning, suppress incremental strings from many line separators by jflute (2018/03/02)
                        // it needs to change lineString, preContinueString to StringBuilder type...
                        lineString = preContinueString + "\n" + lineString;
                    }
                    final DfDelimiterDataValueLineInfo valueLineInfo = analyzeValueLine(lineString, _delimiter);
                    final List<String> ls = valueLineInfo.getValueList(); // empty string resolved later
                    if (valueLineInfo.isContinueNextLine()) {
                        preContinueString = ls.remove(ls.size() - 1);
                        columnValueList.addAll(ls);
                        continue; // keeping valueList that has previous values
                    }
                    columnValueList.addAll(ls);
                }
                // *one record is prepared here

                // /- - - - - - - - - - - - - -
                // check definition differences
                // - - - - - - - - - -/
                if (isDifferentColumnValueCount(firstLineInfo, columnValueList)) {
                    handleDifferentColumnValueCount(resultInfo, dataDirectory, tableDbName, firstLineInfo, columnValueList);

                    // clear temporary variables
                    preContinueString = null;
                    columnValueList.clear();
                    valueListSnapshot = null;
                    continue;
                }
                // *valid record is prepared here
                ++rowNumber;
                valueListSnapshot = columnValueList;

                if (rowNumber <= offsetRowCount) { // basically only when retry
                    // clear temporary variables
                    preContinueString = null;
                    columnValueList.clear();
                    valueListSnapshot = null;
                    continue; // e.g. 1 ~ 100000 rows if 100000 already committed
                }

                // /- - - - - - - - - - - - - - - -
                // process registration to database
                // - - - - - - - - - -/
                final DfDelimiterDataWriteSqlBuilder sqlBuilder =
                        createSqlBuilder(resultInfo, tableDbName, columnMetaMap, columnNameList, columnValueList);
                if (conn == null) {
                    conn = _dataSource.getConnection();
                }
                if (ps == null) {
                    beginTransaction(conn); // for performance (suppress implicit transaction per SQL)
                    executedSql = sqlBuilder.buildSql();
                    ps = prepareStatement(conn, executedSql);
                }
                final Map<String, Object> columnValueMap = sqlBuilder.setupParameter();
                final Set<String> sysdateColumnSet = sqlBuilder.getSysdateColumnSet();
                resolveRelativeDate(dataDirectory, tableDbName, columnValueMap, columnMetaMap, sysdateColumnSet, rowNumber);
                handleLoggingInsert(tableDbName, columnValueMap, loggingInsertType, rowNumber);

                int bindCount = 1;
                for (Entry<String, Object> entry : columnValueMap.entrySet()) {
                    final String columnName = entry.getKey();
                    final Object obj = entry.getValue();

                    // /- - - - - - - - - - - - - - - - - -
                    // process Null (against Null Headache)
                    // - - - - - - - - - -/
                    if (processNull(dataDirectory, tableDbName, columnName, obj, ps, bindCount, columnMetaMap, rowNumber)) {
                        bindCount++;
                        continue;
                    }

                    // /- - - - - - - - - - - - - - -
                    // process NotNull and NotString
                    // - - - - - - - - - -/
                    // If the value is not null and the value has the own type except string,
                    // It registers the value to statement by the type.
                    if (processNotNullNotString(dataDirectory, tableDbName, columnName, obj, conn, ps, bindCount, columnMetaMap,
                            rowNumber)) {
                        bindCount++;
                        continue;
                    }

                    // /- - - - - - - - - - - - - - - - - -
                    // process NotNull and StringExpression
                    // - - - - - - - - - -/
                    final String value = (String) obj;
                    processNotNullString(dataDirectory, dataFile, tableDbName, columnName, value, conn, ps, bindCount, columnMetaMap,
                            rowNumber);
                    bindCount++;
                }
                if (canBatchUpdate) { // mainly here
                    ps.addBatch();
                } else {
                    ps.execute();
                }
                ++addedBatchSize;
                if (isBatchLimit(dataDirectory, addedBatchSize)) { // transaction scope
                    if (canBatchUpdate) { // mainly here
                        // this is supported in only delimiter data writer because delimiter data can treat large data
                        // (actually needed, GC overhead limit exceeded when 1000000 records to MySQL, 2021/01/20)
                        ps.executeBatch(); // to avoid OutOfMemory
                    }
                    commitTransaction(conn);
                    committedRowCount = committedRowCount + addedBatchSize;
                    addedBatchSize = 0;
                    close(ps);
                    ps = null;
                }
                // *one record is finished here

                // clear temporary variables
                // if an exception occurs from execute() or addBatch(),
                // this valueList is to be information for debug
                preContinueString = null;
                columnValueList.clear();
                // keep here for retry
                //valueListSnapshot = null;
            }
            if (ps != null && addedBatchSize > 0) {
                if (canBatchUpdate) { // mainly here
                    ps.executeBatch();
                }
                commitTransaction(conn);
                committedRowCount = committedRowCount + addedBatchSize;
            }
            noticeLoadedRowSize(tableDbName, rowNumber);
            resultInfo.registerLoadedMeta(dataDirectory, _filePath, rowNumber);
            checkImplicitClassification(dataFile, tableDbName, columnNameList);
        } catch (SQLException e) {
            // request retry if it needs (e.g. execution exception of batch insert)
            // the snapshot is used only when retry failure basically
            final DfJDBCException wrapped = DfJDBCException.voice(e);
            final String msg = buildFailureMessage(_filePath, tableDbName, executedSql, columnValueList, wrapped);
            throw new DfDelimiterDataRegistrationFailureException(msg, wrapped.getNextException())
                    .retryIfNeeds(createRetryResource(canBatchUpdate, committedRowCount))
                    .snapshotRow(createRowSnapshot(columnNameList, valueListSnapshot, rowNumber));
        } catch (RuntimeException e) {
            // unneeded snapshot at this side but just in case (or changing determination future)
            final String msg = buildFailureMessage(_filePath, tableDbName, executedSql, columnValueList, null);
            throw new DfDelimiterDataRegistrationFailureException(msg, e)
                    .snapshotRow(createRowSnapshot(columnNameList, valueListSnapshot, rowNumber));
        } finally {
            closeStream(fis, ir, br);
            try {
                rollbackTransaction(conn);
            } catch (SQLException continued) {
                _log.info("Failed to rollback the delimiter data transaction.", continued);
            }
            close(ps);
            close(conn);
            finallyHandlingTable(tableDbName, columnMetaMap); // process after (finally) handling table
        }
    }

    protected String extractTableDbName() {
        return new DfDelimiterDataTableDbNameExtractor(_filePath).extractTableDbName();
    }

    protected boolean canBatchUpdate(boolean forcedlySuppressBatch, String dataDirectory) {
        return !forcedlySuppressBatch && !isMergedSuppressBatchUpdate(dataDirectory);
    }

    protected boolean isBatchLimit(String dataDirectory, int addedBatchSize) {
        return addedBatchSize == getBatchLimit(dataDirectory);
    }

    protected int getBatchLimit(String dataDirectory) {
        final Integer propLimit = _loadingControlProp.getDelimiterDataBatchLimit(dataDirectory);
        return propLimit != null ? propLimit : 100000; // as default
    }

    protected DfDelimiterDataWriteSqlBuilder createSqlBuilder(DfDelimiterDataResultInfo resultInfo, String tableDbName,
            final Map<String, DfColumnMeta> columnMetaMap, List<String> columnNameList, List<String> valueList) {
        final DfDelimiterDataWriteSqlBuilder sqlBuilder = new DfDelimiterDataWriteSqlBuilder();
        sqlBuilder.setTableDbName(tableDbName);
        sqlBuilder.setColumnMetaMap(columnMetaMap);
        sqlBuilder.setColumnNameList(columnNameList);
        sqlBuilder.setValueList(valueList);
        sqlBuilder.setNotFoundColumnMap(resultInfo.getNotFoundColumnMap());
        sqlBuilder.setConvertValueMap(_convertValueMap);
        sqlBuilder.setDefaultValueMap(_defaultValueMap);
        sqlBuilder.setBindTypeProvider(new DfColumnBindTypeProvider() {
            public Class<?> provide(String tableName, DfColumnMeta columnMeta) {
                return getBindType(tableName, columnMeta);
            }
        });
        sqlBuilder.setDefaultValueProp(_defaultValueProp);
        return sqlBuilder;
    }

    protected DfDelimiterDataRegistrationRetryResource createRetryResource(final boolean canBatchUpdate, int committedRowCount) {
        return new DfDelimiterDataRegistrationRetryResource(canBatchUpdate, committedRowCount);
    }

    protected DfDelimiterDataRegistrationRowSnapshot createRowSnapshot(List<String> columnNameList, List<String> valueSnapshot,
            int rowNumber) {
        return new DfDelimiterDataRegistrationRowSnapshot(columnNameList, valueSnapshot, rowNumber);
    }

    // ===================================================================================
    //                                                                      Before/Finally
    //                                                                      ==============
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

    // ===================================================================================
    //                                                                    Process per Type
    //                                                                    ================
    // *overridden isNullValue() process was moved to DfDelimiterDataWriteSqlBuilder
    //return str.length() == 0 || str.equals("\"\"");

    // ===================================================================================
    //                                                                          First Line
    //                                                                          ==========
    protected DfDelimiterDataFirstLineInfo analyzeFirstLine(String lineString, String delimiter) {
        return new DfDelimiterDataFirstLineAnalyzer().analyzeFirstLineInfo(lineString, delimiter);
    }

    // ===================================================================================
    //                                                                       Listed Column
    //                                                                       =============
    protected void setupColumnNameList(List<String> columnNameList, String dataDirectory, File dataFile, String tableDbName,
            DfDelimiterDataFirstLineInfo firstLineInfo, Map<String, DfColumnMeta> columnMetaMap) {
        final DfDelimiterDataListedColumnHandler handler = new DfDelimiterDataListedColumnHandler(dataDirectory, dataFile, tableDbName);
        handler.setupColumnNameList(columnNameList, firstLineInfo, columnMetaMap, _defaultValueMap,
                ldataDirectory -> isCheckColumnDef(ldataDirectory),
                lcolumnNameList -> checkColumnDef(dataFile, tableDbName, lcolumnNameList, columnMetaMap));
    }

    // ===================================================================================
    //                                                                          Value Line
    //                                                                          ==========
    protected DfDelimiterDataValueLineInfo analyzeValueLine(String lineString, String delimiter) {
        return _valueLineAnalyzer.analyzeValueLine(lineString, delimiter);
    }

    // ===================================================================================
    //                                                         Different ColumnValue Count
    //                                                         ===========================
    protected boolean isDifferentColumnValueCount(DfDelimiterDataFirstLineInfo firstLineInfo, List<String> valueList) {
        return valueList.size() < firstLineInfo.getColumnNameList().size();
    }

    protected void handleDifferentColumnValueCount(DfDelimiterDataResultInfo resultInfo, String dataDirectory, String tableDbName,
            DfDelimiterDataFirstLineInfo firstLineInfo, List<String> valueList) {
        _loadingControlProp.handleDifferentColumnValueCount(resultInfo, dataDirectory, _filePath, tableDbName, firstLineInfo, valueList);
    }

    // ===================================================================================
    //                                                                         Filter Line
    //                                                                         ===========
    protected String filterLineString(String lineString) {
        return _lineDirectFilter.filterLineString(lineString, _convertValueMap);
    }

    // ===================================================================================
    //                                                                   Batch Transaction
    //                                                                   =================
    protected void beginTransaction(Connection conn) throws SQLException {
        _jdbcHandler.beginTransaction(conn);
    }

    protected PreparedStatement prepareStatement(Connection conn, String executedSql) throws SQLException {
        return _jdbcHandler.prepareStatement(conn, executedSql);
    }

    protected void commitTransaction(Connection conn) throws SQLException {
        _jdbcHandler.commitTransaction(conn);
    }

    protected void rollbackTransaction(Connection conn) throws SQLException {
        _jdbcHandler.rollbackTransaction(conn);
    }

    protected Boolean getAutoCommit(Connection conn) {
        return _jdbcHandler.getAutoCommit(conn);
    }

    // ===================================================================================
    //                                                                              Closer
    //                                                                              ======
    protected void closeStream(FileInputStream fis, InputStreamReader ir, BufferedReader br) {
        _jdbcHandler.closeStream(fis, ir, br);
    }

    protected void close(PreparedStatement ps) {
        _jdbcHandler.close(ps);
    }

    protected void close(Connection conn) {
        _jdbcHandler.close(conn);
    }

    // ===================================================================================
    //                                                                   Exception Thrower
    //                                                                   =================
    protected void throwTableNotFoundException(String fileName, String tableDbName) {
        new DfDelimiterDataWritingExceptionThrower().throwTableNotFoundException(fileName, tableDbName);
    }

    protected String buildFailureMessage(String fileName, String tableDbName, String executedSql, List<String> valueList,
            SQLException sqlEx) {
        final DfDelimiterDataWritingExceptionThrower wer = new DfDelimiterDataWritingExceptionThrower();
        return wer.buildFailureMessage(fileName, tableDbName, executedSql, valueList, _bindTypeCacheMap, _stringProcessorCacheMap, sqlEx);
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getDelimiter() {
        return _delimiter;
    }

    public void setDelimiter(String delimiter) {
        _delimiter = delimiter;
    }

    public String getEncoding() {
        return _encoding;
    }

    public void setEncoding(String encoding) {
        _encoding = encoding;
    }

    public String getFilePath() {
        return _filePath;
    }

    public void setFilePath(String filePath) {
        _filePath = filePath;
    }

    public Map<String, Map<String, String>> getConvertValueMap() {
        return _convertValueMap;
    }

    public void setConvertValueMap(Map<String, Map<String, String>> convertValueMap) {
        _convertValueMap = convertValueMap;
    }

    public Map<String, String> getDefaultValueMap() {
        return _defaultValueMap;
    }

    public void setDefaultValueMap(Map<String, String> defaultValueMap) {
        _defaultValueMap = defaultValueMap;
    }
}
