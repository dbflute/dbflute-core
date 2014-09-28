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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.engine.database.model.UnifiedSchema;
import org.seasar.dbflute.exception.DfDelimiterDataColumnDefNotFoundException;
import org.seasar.dbflute.exception.DfDelimiterDataRegistrationFailureException;
import org.seasar.dbflute.exception.DfDelimiterDataTableNotFoundException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.helper.StringKeyMap;
import org.seasar.dbflute.helper.StringSet;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfColumnMeta;
import org.seasar.dbflute.logic.replaceschema.loaddata.DfColumnBindTypeProvider;
import org.seasar.dbflute.logic.replaceschema.loaddata.DfDelimiterDataResultInfo;
import org.seasar.dbflute.logic.replaceschema.loaddata.DfDelimiterDataWriter;
import org.seasar.dbflute.logic.replaceschema.loaddata.impl.dataprop.DfLoadingControlProp.LoggingInsertType;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfDelimiterDataWriterImpl extends DfAbsractDataWriter implements DfDelimiterDataWriter {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance. */
    private static final Log _log = LogFactory.getLog(DfDelimiterDataWriterImpl.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String _fileName;
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
        final String tableDbName;
        {
            String tmp = _fileName.substring(_fileName.lastIndexOf("/") + 1, _fileName.lastIndexOf("."));
            if (tmp.indexOf("-") >= 0) {
                tmp = tmp.substring(tmp.indexOf("-") + "-".length());
            }
            tableDbName = tmp;
        }
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
        final List<String> additionalColumnList = new ArrayList<String>();
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
                    firstLineInfo = getFirstLineInfo(_delimiter, lineString);
                    columnNameList.addAll(firstLineInfo.getColumnNameList());
                    if (columnNameList.isEmpty()) {
                        throwDelimiterDataColumnDefNotFoundException(_fileName, tableDbName);
                    }
                    if (isCheckColumnDefExistence(dataDirectory)) { // should be before default process
                        checkColumnDefExistence(dataDirectory, dataFile, tableDbName, columnNameList, columnMetaMap);
                    }
                    final StringSet columnSet = StringSet.createAsFlexible();
                    columnSet.addAll(columnNameList);
                    for (String defaultColumn : _defaultValueMap.keySet()) {
                        if (columnSet.contains(defaultColumn)) {
                            continue;
                        }
                        additionalColumnList.add(defaultColumn);
                    }
                    columnNameList.addAll(additionalColumnList); // no DB column is ignored later
                    continue;
                }

                // /- - - - - - - - - - - - - - -
                // analyze values in line strings
                // - - - - - - - - - -/
                lineString = filterLineString(lineString);
                {
                    if (preContinueString != null && !preContinueString.equals("")) {
                        lineString = preContinueString + "\n" + lineString;
                    }
                    final ValueLineInfo valueLineInfo = arrangeValueList(lineString, _delimiter);
                    final List<String> ls = valueLineInfo.getValueList();
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
                    String msg = "The count of values wasn't correct:";
                    msg = msg + " column=" + firstLineInfo.getColumnNameList().size();
                    msg = msg + " value=" + valueList.size();
                    msg = msg + " -> " + valueList;
                    resultInfo.registerWarningFile(_fileName, msg);

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
                resolveRelativeDate(dataDirectory, tableDbName, columnValueMap, columnMetaMap, sysdateColumnSet,
                        rowNumber);
                handleLoggingInsert(tableDbName, columnValueMap, loggingInsertType, rowNumber);

                int bindCount = 1;
                final Set<Entry<String, Object>> entrySet = columnValueMap.entrySet();
                for (Entry<String, Object> entry : entrySet) {
                    final String columnName = entry.getKey();
                    final Object obj = entry.getValue();

                    // /- - - - - - - - - - - - - - - - - -
                    // process Null (against Null Headache)
                    // - - - - - - - - - -/
                    if (processNull(dataDirectory, tableDbName, columnName, obj, ps, bindCount, columnMetaMap,
                            rowNumber)) {
                        bindCount++;
                        continue;
                    }

                    // /- - - - - - - - - - - - - - -
                    // process NotNull and NotString
                    // - - - - - - - - - -/
                    // If the value is not null and the value has the own type except string,
                    // It registers the value to statement by the type.
                    if (processNotNullNotString(dataDirectory, tableDbName, columnName, obj, conn, ps, bindCount,
                            columnMetaMap, rowNumber)) {
                        bindCount++;
                        continue;
                    }

                    // /- - - - - - - - - - - - - - - - - -
                    // process NotNull and StringExpression
                    // - - - - - - - - - -/
                    final String value = (String) obj;
                    processNotNullString(dataDirectory, dataFile, tableDbName, columnName, value, conn, ps, bindCount,
                            columnMetaMap, rowNumber);
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
            checkImplicitClassification(dataFile, tableDbName, columnNameList);
        } catch (FileNotFoundException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (SQLException e) {
            final SQLException nextEx = e.getNextException();
            if (nextEx != null && !e.equals(nextEx)) { // focus on next exception
                _log.warn("*Failed to register: " + e.getMessage());
                String msg = buildRegExpMessage(_fileName, tableDbName, executedSql, valueList, nextEx);
                throw new DfDelimiterDataRegistrationFailureException(msg, nextEx); // switch!
            } else {
                String msg = buildRegExpMessage(_fileName, tableDbName, executedSql, valueList, e);
                throw new DfDelimiterDataRegistrationFailureException(msg, e);
            }
        } catch (RuntimeException e) {
            String msg = buildRegExpMessage(_fileName, tableDbName, executedSql, valueList, e);
            throw new DfDelimiterDataRegistrationFailureException(msg, e);
        } finally {
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
            commitJustInCase(conn);
            close(ps);
            close(conn);
            // process after (finally) handling table
            finallyHandlingTable(tableDbName, columnMetaMap);
        }
    }

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

    protected String buildRegExpMessage(String fileName, String tableDbName, String executedSql,
            List<String> valueList, Exception e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to register the table data.");
        br.addItem("Advice");
        br.addElement("If you don't know the cause, suppress batch update and retry it.");
        br.addElement("Change the property 'isSuppressBatchUpdate' to false temporarily,");
        br.addElement("and loading process is executed per one record,");
        br.addElement("and you can find a record that causes the exception with logs.");
        br.addItem("Delimiter File");
        br.addElement(fileName);
        br.addItem("Table");
        br.addElement(tableDbName);
        br.addItem("Executed SQL");
        br.addElement(executedSql);
        if (!valueList.isEmpty()) { // basically when batch update is suppressed
            br.addItem("Bound Values");
            br.addElement(valueList);
        }
        br.addItem("Message");
        br.addElement(e.getMessage());
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
    protected FirstLineInfo getFirstLineInfo(String delimiter, final String lineString) {
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

    protected boolean isDifferentColumnValueCount(FirstLineInfo firstLineInfo, List<String> valueList) {
        if (valueList.size() < firstLineInfo.getColumnNameList().size()) {
            return true;
        }
        return false;
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
