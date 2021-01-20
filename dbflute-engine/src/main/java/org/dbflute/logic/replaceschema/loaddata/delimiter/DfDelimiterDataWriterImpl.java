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
import java.io.FileNotFoundException;
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
        FileInputStream fis = null;
        InputStreamReader ir = null;
        BufferedReader br = null;

        final String dataDirectory = Srl.substringLastFront(_filePath, "/");
        final LoggingInsertType loggingInsertType = getLoggingInsertType(dataDirectory);
        final String tableDbName = extractTableDbName();
        final Map<String, DfColumnMeta> columnMetaMap = getColumnMetaMap(tableDbName);
        if (columnMetaMap.isEmpty()) {
            throwTableNotFoundException(_filePath, tableDbName);
        }

        // process before handling table
        beforeHandlingTable(tableDbName, columnMetaMap);

        String lineString = null;
        String preContinueString = null;
        String executedSql = null;
        final List<String> columnNameList = new ArrayList<String>();
        final List<String> valueList = new ArrayList<String>();
        final boolean canBatchUpdate = !isMergedSuppressBatchUpdate(dataDirectory);

        final File dataFile = new File(_filePath);
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            fis = new FileInputStream(dataFile);
            ir = new InputStreamReader(fis, _encoding);
            br = new BufferedReader(ir);

            DfDelimiterDataFirstLineInfo firstLineInfo = null;
            int loopIndex = -1;
            int rowNumber = 0;
            int addedBatchSize = 0;
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
                        valueList.addAll(ls);
                        continue;
                    }
                    valueList.addAll(ls);
                }
                // *one record is prepared here

                // /- - - - - - - - - - - - - -
                // check definition differences
                // - - - - - - - - - -/
                if (isDifferentColumnValueCount(firstLineInfo, valueList)) {
                    handleDifferentColumnValueCount(resultInfo, dataDirectory, tableDbName, firstLineInfo, valueList);

                    // clear temporary variables
                    valueList.clear();
                    preContinueString = null;
                    continue;
                }
                // *valid record is prepared here
                ++rowNumber;

                // /- - - - - - - - - - - - - - - -
                // process registration to database
                // - - - - - - - - - -/
                final DfDelimiterDataWriteSqlBuilder sqlBuilder =
                        createSqlBuilder(resultInfo, tableDbName, columnMetaMap, columnNameList, valueList);
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
                if (isBatchSizeLimit(addedBatchSize)) { // transaction scope
                    if (canBatchUpdate) { // mainly here
                        // this is supported in only delimiter data writer
                        // because delimiter data can treat large data
                        ps.executeBatch(); // to avoid OutOfMemory
                    }
                    commitTransaction(conn);
                    addedBatchSize = 0;
                    close(ps);
                    ps = null;
                }
                // *one record is finished here

                // clear temporary variables
                // if an exception occurs from execute() or addBatch(),
                // this valueList is to be information for debug
                valueList.clear();
                preContinueString = null;
            }
            if (ps != null && addedBatchSize > 0) {
                if (canBatchUpdate) { // mainly here
                    ps.executeBatch();
                }
                commitTransaction(conn);
            }
            noticeLoadedRowSize(tableDbName, rowNumber);
            resultInfo.registerLoadedMeta(dataDirectory, _filePath, rowNumber);
            checkImplicitClassification(dataFile, tableDbName, columnNameList);
        } catch (FileNotFoundException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (SQLException e) {
            DfJDBCException wrapped = DfJDBCException.voice(e);
            String msg = buildRegExpMessage(_filePath, tableDbName, executedSql, valueList, wrapped);
            throw new DfDelimiterDataRegistrationFailureException(msg, wrapped.getNextException());
        } catch (RuntimeException e) {
            String msg = buildRegExpMessage(_filePath, tableDbName, executedSql, valueList, null);
            throw new DfDelimiterDataRegistrationFailureException(msg, e);
        } finally {
            closeStream(fis, ir, br);
            commitJustInCase(conn);
            close(ps);
            close(conn);
            // process after (finally) handling table
            finallyHandlingTable(tableDbName, columnMetaMap);
        }
    }

    protected String extractTableDbName() {
        return new DfDelimiterDataTableDbNameExtractor(_filePath).extractTableDbName();
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

    protected boolean isBatchSizeLimit(int addedBatchSize) {
        return _jdbcHandler.isBatchSizeLimit(addedBatchSize);
    }

    protected void commitTransaction(Connection conn) throws SQLException {
        _jdbcHandler.commitTransaction(conn);
    }

    protected void commitJustInCase(Connection conn) {
        _jdbcHandler.commitJustInCase(conn);
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

    protected String buildRegExpMessage(String fileName, String tableDbName, String executedSql, List<String> valueList,
            SQLException sqlEx) {
        final DfDelimiterDataWritingExceptionThrower wer = new DfDelimiterDataWritingExceptionThrower();
        return wer.buildRegExpMessage(fileName, tableDbName, executedSql, valueList, _bindTypeCacheMap, _stringProcessorCacheMap, sqlEx);
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
