/*
 * Copyright 2014-2019 the original author or authors.
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
package org.dbflute.logic.replaceschema.loaddata.impl;

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
import org.dbflute.exception.DfDelimiterDataColumnDefNotFoundException;
import org.dbflute.exception.DfDelimiterDataRegistrationFailureException;
import org.dbflute.exception.DfDelimiterDataTableNotFoundException;
import org.dbflute.exception.DfJDBCException;
import org.dbflute.helper.StringKeyMap;
import org.dbflute.helper.StringSet;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.logic.jdbc.metadata.info.DfColumnMeta;
import org.dbflute.logic.replaceschema.loaddata.DfColumnBindTypeProvider;
import org.dbflute.logic.replaceschema.loaddata.DfDelimiterDataResultInfo;
import org.dbflute.logic.replaceschema.loaddata.DfDelimiterDataWriter;
import org.dbflute.logic.replaceschema.loaddata.impl.dataprop.DfLoadingControlProp.LoggingInsertType;
import org.dbflute.util.DfCollectionUtil;
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
    protected String _fileName; // contains path
    protected String _encoding;
    protected String _delimiter;
    protected Map<String, Map<String, String>> _convertValueMap;
    protected Map<String, String> _defaultValueMap;

    /** The cache map of meta info. The key is table name. */
    protected final Map<String, Map<String, DfColumnMeta>> _metaInfoCacheMap = StringKeyMap.createAsFlexible();

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
        _log.info("writeData(" + _fileName + ")");
        _log.info("= = = = = = =/");
        FileInputStream fis = null;
        InputStreamReader ir = null;
        BufferedReader br = null;

        final String dataDirectory = Srl.substringLastFront(_fileName, "/");
        final LoggingInsertType loggingInsertType = getLoggingInsertType(dataDirectory);
        final String tableDbName = extractTableDbName();
        final Map<String, DfColumnMeta> columnMetaMap = getColumnMetaMap(tableDbName);
        if (columnMetaMap.isEmpty()) {
            throwTableNotFoundException(_fileName, tableDbName);
        }

        // process before handling table
        beforeHandlingTable(tableDbName, columnMetaMap);

        String lineString = null;
        String preContinueString = null;
        String executedSql = null;
        final List<String> columnNameList = new ArrayList<String>();
        final List<String> valueList = new ArrayList<String>();
        final boolean canBatchUpdate = !isMergedSuppressBatchUpdate(dataDirectory);

        final File dataFile = new File(_fileName);
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            fis = new FileInputStream(dataFile);
            ir = new InputStreamReader(fis, _encoding);
            br = new BufferedReader(ir);

            FirstLineInfo firstLineInfo = null;
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
                    firstLineInfo = analyzeFirstLineInfo(_delimiter, lineString);
                    setupColumnNameList(dataDirectory, dataFile, tableDbName, columnMetaMap, firstLineInfo, columnNameList);
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
                    final ValueLineInfo valueLineInfo = arrangeValueList(lineString, _delimiter);
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
            resultInfo.registerLoadedMeta(dataDirectory, _fileName, rowNumber);
            checkImplicitClassification(dataFile, tableDbName, columnNameList);
        } catch (FileNotFoundException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (SQLException e) {
            DfJDBCException wrapped = DfJDBCException.voice(e);
            String msg = buildRegExpMessage(_fileName, tableDbName, executedSql, valueList, wrapped);
            throw new DfDelimiterDataRegistrationFailureException(msg, wrapped.getNextException());
        } catch (RuntimeException e) {
            String msg = buildRegExpMessage(_fileName, tableDbName, executedSql, valueList, null);
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
        final String tableDbName;
        {
            String tmp = _fileName.substring(_fileName.lastIndexOf("/") + 1, _fileName.lastIndexOf("."));
            if (tmp.indexOf("-") >= 0) {
                tmp = tmp.substring(tmp.indexOf("-") + "-".length());
            }
            tableDbName = tmp;
        }
        return tableDbName;
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
    @Override
    protected boolean isNullValue(Object value) {
        return super.isNullValue(value);

        // *This process was moved to DfDelimiterDataWriteSqlBuilder
        //if (!(value instanceof String)) {
        //    return false;
        //}
        //String str = (String) value;
        //return str.length() == 0 || str.equals("\"\"");
    }

    // ===================================================================================
    //                                                                     First Line Info
    //                                                                     ===============
    protected FirstLineInfo analyzeFirstLineInfo(String delimiter, final String lineString) {
        List<String> columnNameList;
        columnNameList = new ArrayList<String>();
        final String[] values = lineString.split(delimiter);
        int count = 0;
        boolean quotated = false;
        for (String value : values) {
            if (count == 0) {
                if (value != null && value.startsWith("\"") && value.endsWith("\"")) {
                    quotated = true;
                }
            }
            addValueToList(columnNameList, value);
            count++;
        }
        final FirstLineInfo firstLineInformation = new FirstLineInfo();
        firstLineInformation.setColumnNameList(columnNameList);
        firstLineInformation.setQuotated(quotated);
        return firstLineInformation;
    }

    protected void addValueToList(List<String> ls, String value) {
        if (value != null && value.startsWith("\"") && value.endsWith("\"")) {
            ls.add(value.substring(1, value.length() - 1));
        } else {
            ls.add(value != null ? value : "");
        }
    }

    // ===================================================================================
    //                                                                    Column Name List
    //                                                                    ================
    protected void setupColumnNameList(String dataDirectory, File dataFile, String tableDbName, Map<String, DfColumnMeta> columnMetaMap,
            FirstLineInfo firstLineInfo, List<String> columnNameList) {
        columnNameList.addAll(firstLineInfo.getColumnNameList());
        if (columnNameList.isEmpty()) {
            throwDelimiterDataColumnDefNotFoundException(_fileName, tableDbName);
        }
        if (isCheckColumnDef(dataDirectory)) {
            checkColumnDef(dataFile, tableDbName, columnNameList, columnMetaMap);
        }
        final StringSet columnSet = StringSet.createAsFlexible();
        columnSet.addAll(columnNameList);
        final List<String> additionalColumnList = new ArrayList<String>();
        for (String defaultColumn : _defaultValueMap.keySet()) {
            if (columnSet.contains(defaultColumn)) {
                continue;
            }
            if (columnMetaMap.containsKey(defaultColumn)) { // only existing column in DB
                additionalColumnList.add(defaultColumn);
            }
        }
        columnNameList.addAll(additionalColumnList); // defined columns + default columns (existing in DB)
    }

    // ===================================================================================
    //                                                                          Value List
    //                                                                          ==========
    protected ValueLineInfo arrangeValueList(String lineString, String delimiter) {
        // Don't use String.split() because the method
        // does not match this (detail) specification!
        //final String[] values = lineString.split(delimiter);
        final List<String> valueList = Srl.splitList(lineString, delimiter);
        return arrangeValueList(valueList, delimiter);
    }

    protected ValueLineInfo arrangeValueList(List<String> valueList, String delimiter) {
        final ValueLineInfo valueLineInfo = new ValueLineInfo();
        final ArrayList<String> resultList = new ArrayList<String>();
        String preString = "";
        for (int i = 0; i < valueList.size(); i++) {
            final String value = valueList.get(i);
            if (value == null) { // basically no way (valueList does not contain null)
                continue;
            }
            if (i == valueList.size() - 1) { // last loop at the line
                if (preString.equals("")) {
                    if (isFrontQOnly(value)) {
                        valueLineInfo.setContinueNextLine(true);
                        resultList.add(value);
                    } else if (isRearQOnly(value)) {
                        resultList.add(value);
                    } else if (isNotBothQ(value)) {
                        resultList.add(value);
                    } else {
                        resultList.add(resolveDoubleQuotation(value));
                    }
                } else { // continued
                    if (endsQuote(value, false)) {
                        resultList.add(resolveDoubleQuotation(connectPreString(preString, delimiter, value)));
                    } else {
                        valueLineInfo.setContinueNextLine(true);
                        resultList.add(connectPreString(preString, delimiter, value));
                    }
                }
                break; // because it's the last loop
            }

            if (preString.equals("")) {
                if (isFrontQOnly(value)) {
                    preString = value;
                    continue;
                } else if (isRearQOnly(value)) {
                    preString = value;
                    continue;
                } else if (isNotBothQ(value)) {
                    resultList.add(value);
                } else {
                    resultList.add(resolveDoubleQuotation(value));
                }
            } else { // continued
                if (endsQuote(value, false)) {
                    resultList.add(resolveDoubleQuotation(connectPreString(preString, delimiter, value)));
                } else {
                    preString = connectPreString(preString, delimiter, value);
                    continue;
                }
            }
            preString = "";
        }
        valueLineInfo.setValueList(resultList);
        return valueLineInfo;
    }

    protected String connectPreString(String preString, String delimiter, String value) {
        if (preString.equals("")) {
            return value;
        } else {
            return preString + delimiter + value;
        }
    }

    protected boolean isNotBothQ(final String value) {
        return !isQQ(value) && !value.startsWith("\"") && !endsQuote(value, false);
    }

    protected boolean isRearQOnly(final String value) {
        return !isQQ(value) && !value.startsWith("\"") && (endsQuote(value, false));
    }

    protected boolean isFrontQOnly(final String value) {
        return !isQQ(value) && value.startsWith("\"") && !endsQuote(value, true);
    }

    protected boolean isQQ(final String value) {
        return value.equals("\"\"");
    }

    protected boolean endsQuote(String value, boolean startsQuote) {
        value = startsQuote ? value.substring(1) : value;
        final int length = value.length();
        int count = 0;
        for (int i = 0; i < length; i++) {
            char ch = value.charAt(length - (i + 1));
            if (ch == '\"') {
                ++count;
            } else {
                break;
            }
        }
        return count > 0 && isOddNumber(count);
    }

    protected boolean isOddNumber(int number) {
        return (number % 2) != 0;
    }

    protected String resolveDoubleQuotation(String value) {
        if (value.length() > 1 && value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1);
            value = value.substring(0, value.length() - 1);
            // resolve escaped quotation : "" -> "
            value = Srl.replace(value, "\"\"", "\"");
        }
        return value;
    }

    public static class FirstLineInfo {
        protected List<String> columnNameList;
        protected boolean quotated;

        public List<String> getColumnNameToLowerList() {
            final ArrayList<String> ls = new ArrayList<String>();
            for (String columnName : columnNameList) {
                ls.add(columnName.toLowerCase());
            }
            return ls;
        }

        public List<String> getColumnNameList() {
            return columnNameList;
        }

        public void setColumnNameList(List<String> columnNameList) {
            this.columnNameList = columnNameList;
        }

        public boolean isQuotated() {
            return quotated;
        }

        public void setQuotated(boolean quotated) {
            this.quotated = quotated;
        }
    }

    public static class ValueLineInfo {
        protected List<String> valueList;
        protected boolean continueNextLine;

        public List<String> getValueList() {
            return valueList;
        }

        public void setValueList(List<String> valueList) {
            this.valueList = valueList;
        }

        public boolean isContinueNextLine() {
            return continueNextLine;
        }

        public void setContinueNextLine(boolean continueNextLine) {
            this.continueNextLine = continueNextLine;
        }
    }

    // ===================================================================================
    //                                                         Different ColumnValue Count
    //                                                         ===========================
    protected boolean isDifferentColumnValueCount(FirstLineInfo firstLineInfo, List<String> valueList) {
        return valueList.size() < firstLineInfo.getColumnNameList().size();
    }

    protected void handleDifferentColumnValueCount(DfDelimiterDataResultInfo resultInfo, String dataDirectory, String tableDbName,
            FirstLineInfo firstLineInfo, List<String> valueList) {
        _loadingControlProp.handleDifferentColumnValueCount(resultInfo, dataDirectory, _fileName, tableDbName, firstLineInfo, valueList);
    }

    // ===================================================================================
    //                                                                       Convert Value
    //                                                                       =============
    protected String filterLineString(String lineString) {
        final Map<String, String> lineMapping = findConvertLineMapping();
        if (lineMapping != null) {
            final Set<Entry<String, String>> entrySet = lineMapping.entrySet();
            for (Entry<String, String> entry : entrySet) {
                final String before = entry.getKey();
                final String after = entry.getValue();
                lineString = Srl.replace(lineString, before, after);
            }
        }
        return lineString;
    }

    protected Map<String, String> _convertLineMap;

    protected Map<String, String> findConvertLineMapping() {
        if (_convertLineMap != null) {
            return _convertLineMap;
        }
        if (_convertValueMap != null) {
            _convertLineMap = _convertValueMap.get("$$LINE$$");
        }
        if (_convertLineMap == null) {
            _convertLineMap = DfCollectionUtil.emptyMap();
        }
        return _convertLineMap;
    }

    // ===================================================================================
    //                                                                   Batch Transaction
    //                                                                   =================
    protected void beginTransaction(Connection conn) throws SQLException {
        conn.setAutoCommit(false);
    }

    protected PreparedStatement prepareStatement(Connection conn, String executedSql) throws SQLException {
        return conn.prepareStatement(executedSql);
    }

    protected boolean isBatchSizeLimit(int addedBatchSize) {
        return addedBatchSize == 100000;
    }

    protected void commitTransaction(Connection conn) throws SQLException {
        conn.commit();
    }

    protected void commitJustInCase(Connection conn) {
        final Boolean autoCommit = getAutoCommit(conn);
        if (autoCommit != null && !autoCommit) { // basically no way, just in case
            try {
                commitTransaction(conn);
            } catch (SQLException continued) {
                _log.warn("Failed to commit the transaction.", continued);
            }
        }
    }

    protected Boolean getAutoCommit(Connection conn) {
        Boolean autoCommit = null;
        try {
            autoCommit = conn != null ? conn.getAutoCommit() : null;
        } catch (SQLException continued) {
            // because it is possible that the connection would have already closed
            _log.warn("Connection#getAutoCommit() said: " + continued.getMessage());
        }
        return autoCommit;
    }

    // ===================================================================================
    //                                                                              Closer
    //                                                                              ======
    protected void closeStream(FileInputStream fis, InputStreamReader ir, BufferedReader br) {
        try {
            if (fis != null) {
                fis.close();
            }
            if (ir != null) {
                ir.close();
            }
            if (br != null) {
                br.close();
            }
        } catch (IOException continued) {
            _log.warn("File-close threw the exception: ", continued);
        }
    }

    protected void close(PreparedStatement ps) {
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException ignored) {
                _log.info("Statement.close() threw the exception!", ignored);
            }
        }
    }

    protected void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ignored) {
                _log.info("Connection.close() threw the exception!", ignored);
            }
        }
    }

    // ===================================================================================
    //                                                                   Exception Thrower
    //                                                                   =================
    protected void throwTableNotFoundException(String fileName, String tableDbName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The table specified on the delimiter file was not found in the schema.");
        br.addItem("Advice");
        br.addElement("Please confirm the name about its spelling.");
        br.addElement("And confirm that whether the DLL executions have errors.");
        br.addItem("Delimiter File");
        br.addElement(fileName);
        br.addItem("Table");
        br.addElement(tableDbName);
        final String msg = br.buildExceptionMessage();
        throw new DfDelimiterDataTableNotFoundException(msg);
    }

    protected String buildRegExpMessage(String fileName, String tableDbName, String executedSql, List<String> valueList,
            SQLException sqlEx) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to register the table data.");
        br.addItem("Advice");
        br.addElement("If you don't know the cause, suppress batch update and retry it.");
        br.addElement("Change the property isSuppressBatchUpdate");
        br.addElement("of loadingControlMap.dataprop to false temporarily,");
        br.addElement("and loading process is executed per one record,");
        br.addElement("and you can find a record that causes the exception with logs.");
        br.addItem("Delimiter File");
        br.addElement(fileName);
        br.addItem("Table");
        br.addElement(tableDbName);
        if (sqlEx != null) {
            br.addItem("SQLException");
            br.addElement(sqlEx.getClass().getName());
            br.addElement(sqlEx.getMessage());
        }
        // #hope show non-batch retry by jflute
        br.addItem("Executed SQL");
        br.addElement(executedSql);
        if (!valueList.isEmpty()) { // basically when batch update is suppressed
            br.addItem("Bound Values");
            br.addElement(valueList);
        }
        final Map<String, Class<?>> bindTypeCacheMap = _bindTypeCacheMap.get(tableDbName);
        if (bindTypeCacheMap != null) {
            br.addItem("Bind Type");
            final Set<Entry<String, Class<?>>> entrySet = bindTypeCacheMap.entrySet();
            for (Entry<String, Class<?>> entry : entrySet) {
                br.addElement(entry.getKey() + " = " + entry.getValue());
            }
        }
        final Map<String, StringProcessor> stringProcessorCacheMap = _stringProcessorCacheMap.get(tableDbName);
        if (bindTypeCacheMap != null) {
            br.addItem("String Processor");
            final Set<Entry<String, StringProcessor>> entrySet = stringProcessorCacheMap.entrySet();
            for (Entry<String, StringProcessor> entry : entrySet) {
                br.addElement(entry.getKey() + " = " + entry.getValue());
            }
        }
        return br.buildExceptionMessage();
    }

    protected void throwDelimiterDataColumnDefNotFoundException(String fileName, String tableDbName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The column definition on the delimiter file was not found.");
        br.addItem("Advice");
        br.addElement("Make sure the header definition of the delimiter file exists.");
        br.addItem("Delimiter File");
        br.addElement(fileName);
        br.addItem("Table");
        br.addElement(tableDbName);
        final String msg = br.buildExceptionMessage();
        throw new DfDelimiterDataColumnDefNotFoundException(msg);
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getDelimiter() {
        return _delimiter;
    }

    public void setDelimiter(String delimiter) {
        this._delimiter = delimiter;
    }

    public String getEncoding() {
        return _encoding;
    }

    public void setEncoding(String encoding) {
        this._encoding = encoding;
    }

    public String getFileName() {
        return _fileName;
    }

    public void setFileName(String fileName) {
        this._fileName = fileName;
    }

    public Map<String, Map<String, String>> getConvertValueMap() {
        return _convertValueMap;
    }

    public void setConvertValueMap(Map<String, Map<String, String>> convertValueMap) {
        this._convertValueMap = convertValueMap;
    }

    public Map<String, String> getDefaultValueMap() {
        return _defaultValueMap;
    }

    public void setDefaultValueMap(Map<String, String> defaultValueMap) {
        this._defaultValueMap = defaultValueMap;
    }
}
