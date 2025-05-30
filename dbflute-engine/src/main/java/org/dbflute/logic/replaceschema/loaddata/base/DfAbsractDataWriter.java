/*
 * Copyright 2014-2025 the original author or authors.
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
package org.dbflute.logic.replaceschema.loaddata.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.torque.engine.database.model.TypeMap;
import org.apache.torque.engine.database.model.UnifiedSchema;
import org.dbflute.DfBuildProperties;
import org.dbflute.exception.DfJDBCException;
import org.dbflute.exception.DfLoadDataRegistrationFailureException;
import org.dbflute.helper.StringKeyMap;
import org.dbflute.helper.filesystem.FileTextIO;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.jdbc.ValueType;
import org.dbflute.logic.jdbc.metadata.basic.DfColumnExtractor;
import org.dbflute.logic.jdbc.metadata.basic.DfTableExtractor;
import org.dbflute.logic.jdbc.metadata.info.DfColumnMeta;
import org.dbflute.logic.jdbc.metadata.info.DfTableMeta;
import org.dbflute.logic.replaceschema.loaddata.base.dataprop.DfConvertValueProp;
import org.dbflute.logic.replaceschema.loaddata.base.dataprop.DfDefaultValueProp;
import org.dbflute.logic.replaceschema.loaddata.base.dataprop.DfLoadingControlProp;
import org.dbflute.logic.replaceschema.loaddata.base.dataprop.DfLoadingControlProp.LoggingInsertType;
import org.dbflute.logic.replaceschema.loaddata.base.interceptor.DfDataWritingInterceptor;
import org.dbflute.logic.replaceschema.loaddata.base.secretary.DfColumnBindTypeProvider;
import org.dbflute.logic.replaceschema.loaddata.base.secretary.DfImplicitClassificationChecker;
import org.dbflute.logic.replaceschema.loaddata.base.secretary.DfLoadedClassificationLazyChecker;
import org.dbflute.logic.replaceschema.loaddata.base.secretary.DfRelativeDateResolver;
import org.dbflute.properties.DfBasicProperties;
import org.dbflute.properties.DfClassificationProperties;
import org.dbflute.s2dao.valuetype.TnValueTypes;
import org.dbflute.system.DBFluteSystem;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.DfTypeUtil;
import org.dbflute.util.DfTypeUtil.ParseBooleanException;
import org.dbflute.util.DfTypeUtil.ParseTimeException;
import org.dbflute.util.DfTypeUtil.ParseTimestampException;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @author tty (pull request for processBinary(), thanks)
 * @since 0.9.4 (2009/03/25 Wednesday)
 */
public abstract class DfAbsractDataWriter {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The logger instance for this class. (NotNull) */
    private static final Logger _log = LoggerFactory.getLogger(DfAbsractDataWriter.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The data source. (NotNull) */
    protected final DataSource _dataSource;

    /** The unified schema as main schema (for getting database meta data). (NotNull) */
    protected final UnifiedSchema _unifiedSchema;

    /** Does it output the insert SQLs as logging? */
    protected boolean _loggingInsertSql;

    /** Does it suppress batch updates? */
    protected boolean _suppressBatchUpdate;

    /** Does it suppress check for column definition? */
    protected boolean _suppressCheckColumnDef;

    /** Does it suppress check for classification implicit set? */
    protected boolean _suppressCheckImplicitSet;

    /** The intercepter of data writing. (NullAllowed) */
    protected DfDataWritingInterceptor _dataWritingInterceptor;

    /** The handler of tables for getting column meta information(as helper). */
    protected final DfTableExtractor _tableHandler = new DfTableExtractor();

    /** The handler of columns for getting column meta information(as helper). */
    protected final DfColumnExtractor _columnHandler = new DfColumnExtractor();

    /** The cache map of meta info. The key is table name. (ordered for display) */
    protected final Map<String, Map<String, DfColumnMeta>> _columnMetaCacheMap = StringKeyMap.createAsFlexibleOrdered();

    /** The cache map of bind type. The key is table name. (ordered for display) */
    protected final Map<String, Map<String, Class<?>>> _bindTypeCacheMap = StringKeyMap.createAsFlexibleOrdered();

    /** The cache map of string processor. The key is table name. (ordered for display) */
    protected final Map<String, Map<String, StringProcessor>> _stringProcessorCacheMap = StringKeyMap.createAsFlexibleOrdered();

    /** The definition list of string processor instances. (NotNull, ReadOnly) */
    protected final List<StringProcessor> _stringProcessorList = DfCollectionUtil.newArrayList();
    {
        // order has meaning if meta information does not exist
        // (but basically (always) meta information exists)
        _stringProcessorList.add(new DateStringProcessor());
        _stringProcessorList.add(new BooleanStringProcessor());
        _stringProcessorList.add(new NumberStringProcessor());
        _stringProcessorList.add(new UUIDStringProcessor());
        _stringProcessorList.add(new ArrayStringProcessor());
        _stringProcessorList.add(new XmlStringProcessor());
        _stringProcessorList.add(new LargeTextFileStringProcessor());
        _stringProcessorList.add(new BinaryFileStringProcessor());
        _stringProcessorList.add(new RealStringProcessor());
    }

    /** The cache map of null type. The key is table name. (ordered for display) */
    protected final Map<String, Map<String, Integer>> _nullTypeCacheMap = StringKeyMap.createAsFlexibleOrdered();

    /** The resolver of relative date. (NotNull) */
    protected final DfRelativeDateResolver _relativeDateResolver = new DfRelativeDateResolver();

    /** The list of lazy checker for loaded classification. (NotNull) */
    protected final List<DfLoadedClassificationLazyChecker> _implicitClassificationLazyCheckerList =
            new ArrayList<DfLoadedClassificationLazyChecker>();

    /** The data-prop of column value map. (NotNull: after initialization) */
    protected DfConvertValueProp _convertValueProp;

    /** The data-prop of default value map. (NotNull: after initialization) */
    protected DfDefaultValueProp _defaultValueProp;

    /** The data-prop of loading control map. (NotNull: after initialization) */
    protected DfLoadingControlProp _loadingControlProp;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfAbsractDataWriter(DataSource dataSource, UnifiedSchema unifiedSchema) {
        _dataSource = dataSource;
        _unifiedSchema = unifiedSchema;
    }

    // ===================================================================================
    //                                                                     Process Binding
    //                                                                     ===============
    // -----------------------------------------------------
    //                                            Null Value
    //                                            ----------
    protected boolean processNull(String dataDirectory, DfLoadedSchemaTable schemaTable, String columnName, Object value,
            PreparedStatement ps, int bindCount, Map<String, DfColumnMeta> columnMetaMap, int rowNumber) throws SQLException {
        if (!isNullValue(value)) {
            return false;
        }

        final String onfileTableName = schemaTable.getOnfileTableName();
        Map<String, Integer> cacheMap = _nullTypeCacheMap.get(onfileTableName);
        if (cacheMap == null) {
            cacheMap = StringKeyMap.createAsFlexibleOrdered();
            _nullTypeCacheMap.put(onfileTableName, cacheMap);
        }
        final Integer cachedType = cacheMap.get(columnName);
        if (cachedType != null) { // cache hit
            ps.setNull(bindCount, cachedType); // basically no exception
            return true;
        }
        final DfColumnMeta columnMeta = columnMetaMap.get(columnName);
        if (columnMeta != null) {
            // use mapped type at first
            final String mappedJdbcType = _columnHandler.getColumnJdbcType(columnMeta);
            final Integer mappedJdbcDefValue = TypeMap.getJdbcDefValueByJdbcType(mappedJdbcType);
            try {
                ps.setNull(bindCount, mappedJdbcDefValue);
                cacheMap.put(columnName, mappedJdbcDefValue);
            } catch (SQLException e) {
                // retry by plain type
                final int plainJdbcDefValue = columnMeta.getJdbcDefValue();
                try {
                    ps.setNull(bindCount, plainJdbcDefValue);
                    cacheMap.put(columnName, plainJdbcDefValue);
                } catch (SQLException ignored) {
                    final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
                    br.addNotice("Failed to execute setNull(bindCount, jdbcDefValue).");
                    br.addItem("Column");
                    br.addElement(schemaTable + "." + columnName);
                    br.addElement(columnMeta.toString());
                    br.addItem("Mapped JDBC Type");
                    br.addElement(mappedJdbcType);
                    br.addItem("First JDBC Def-Value");
                    br.addElement(mappedJdbcDefValue);
                    br.addItem("Retry JDBC Def-Value");
                    br.addElement(plainJdbcDefValue);
                    br.addItem("Retry Message");
                    br.addElement(ignored.getMessage());
                    String msg = br.buildExceptionMessage();
                    throw new DfJDBCException(msg, e);
                }
            }
        } else { // basically no way
            Integer tryType = Types.VARCHAR; // as default
            try {
                ps.setNull(bindCount, tryType);
                cacheMap.put(columnName, tryType);
            } catch (SQLException e) {
                tryType = Types.NUMERIC;
                try {
                    ps.setNull(bindCount, tryType);
                    cacheMap.put(columnName, tryType);
                } catch (SQLException ignored) {
                    tryType = Types.TIMESTAMP;
                    try {
                        ps.setNull(bindCount, tryType);
                        cacheMap.put(columnName, tryType);
                    } catch (SQLException iignored) {
                        tryType = Types.OTHER;
                        try {
                            ps.setNull(bindCount, tryType); // last try
                            cacheMap.put(columnName, tryType);
                        } catch (SQLException iiignored) {
                            throw e;
                        }
                    }
                }
            }
        }
        return true;
    }

    protected boolean isNullValue(Object value) {
        return value == null;
    }

    // -----------------------------------------------------
    //                                     NotNull NotString
    //                                     -----------------
    protected boolean processNotNullNotString(String dataDirectory, DfLoadedSchemaTable schemaTable, String columnName, Object obj,
            Connection conn, PreparedStatement ps, int bindCount, Map<String, DfColumnMeta> columnInfoMap, int rowNumber)
            throws SQLException {
        if (!isNotNullNotString(obj)) {
            return false;
        }
        final DfColumnMeta columnInfo = columnInfoMap.get(columnName);
        if (columnInfo != null) {
            final Class<?> columnType = getBindType(schemaTable, columnInfo);
            if (columnType != null) {
                bindNotNullValueByColumnType(schemaTable, columnName, conn, ps, bindCount, obj, columnType, rowNumber);
                return true;
            }
        }
        bindNotNullValueByInstance(schemaTable, columnName, conn, ps, bindCount, obj, rowNumber);
        return true;
    }

    protected boolean isNotNullNotString(Object obj) {
        return obj != null && !(obj instanceof String);
    }

    // -----------------------------------------------------
    //                                        NotNull String
    //                                        --------------
    protected void processNotNullString(String dataDirectory, File dataFile, DfLoadedSchemaTable schemaTable, String columnName,
            String value, Connection conn, PreparedStatement ps, int bindCount, Map<String, DfColumnMeta> columnInfoMap, int rowNumber)
            throws SQLException {
        if (value == null) { // just in case
            String msg = "The argument 'value' should not be null.";
            throw new IllegalStateException(msg);
        }

        // treat both-side double quotation as meta control characters
        value = Srl.unquoteDouble(value);

        final String onfileTableName = schemaTable.getOnfileTableName();
        Map<String, StringProcessor> cacheMap = _stringProcessorCacheMap.get(onfileTableName);
        if (cacheMap == null) {
            cacheMap = StringKeyMap.createAsFlexibleOrdered();
            _stringProcessorCacheMap.put(onfileTableName, cacheMap);
        }
        final StringProcessor processor = cacheMap.get(columnName);
        if (processor != null) { // cache hit
            final boolean processed = processor.process(dataDirectory, dataFile, schemaTable, columnName, value, conn, ps, bindCount,
                    columnInfoMap, rowNumber);
            if (!processed) {
                throwColumnValueProcessingFailureException(processor, schemaTable, columnName, value);
            }
            return;
        }
        for (StringProcessor tryProcessor : _stringProcessorList) {
            // processing and searching target processor
            if (tryProcessor.process(dataDirectory, dataFile, schemaTable, columnName, value, conn, ps, bindCount, columnInfoMap,
                    rowNumber)) {
                cacheMap.put(columnName, tryProcessor); // use cache next times
                break;
            }
        }
        // must be bound here
        // (_stringProcessorList has processor for real string)
    }

    protected void throwColumnValueProcessingFailureException(StringProcessor processor, DfLoadedSchemaTable schemaTable, String columnName,
            String value) throws SQLException {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The column value could not be treated by the processor.");
        br.addItem("Advice");
        br.addElement("The column has string expressions judging the type of the column");
        br.addElement("by analyzing the value of first record.");
        br.addElement("But the value of second or more record did not match the type.");
        br.addElement("So confirm your expressions.");
        br.addItem("Table Name");
        br.addElement(schemaTable);
        br.addItem("Column Name");
        br.addElement(columnName);
        br.addItem("String Expression");
        br.addElement(value);
        br.addItem("Processor");
        br.addElement(processor);
        final String msg = br.buildExceptionMessage();
        throw new DfJDBCException(msg);
    }

    public static interface StringProcessor {
        boolean process(String dataDirectory, File dataFile, DfLoadedSchemaTable schemaTable, String columnName, String value,
                Connection conn, PreparedStatement ps, int bindCount, Map<String, DfColumnMeta> columnInfoMap, int rowNumber)
                throws SQLException;
    }

    protected class DateStringProcessor implements StringProcessor {

        public boolean process(String dataDirectory, File dataFile, DfLoadedSchemaTable schemaTable, String columnName, String value,
                Connection conn, PreparedStatement ps, int bindCount, Map<String, DfColumnMeta> columnInfoMap, int rowNumber)
                throws SQLException {
            return processDate(dataDirectory, schemaTable, columnName, value, conn, ps, bindCount, columnInfoMap, rowNumber);
        }

        @Override
        public String toString() {
            return buildProcessorToString(this);
        }
    }

    protected class BooleanStringProcessor implements StringProcessor {

        public boolean process(String dataDirectory, File dataFile, DfLoadedSchemaTable schemaTable, String columnName, String value,
                Connection conn, PreparedStatement ps, int bindCount, Map<String, DfColumnMeta> columnInfoMap, int rowNumber)
                throws SQLException {
            return processBoolean(schemaTable, columnName, value, conn, ps, bindCount, columnInfoMap, rowNumber);
        }

        @Override
        public String toString() {
            return buildProcessorToString(this);
        }
    }

    protected class NumberStringProcessor implements StringProcessor {

        public boolean process(String dataDirectory, File dataFile, DfLoadedSchemaTable schemaTable, String columnName, String value,
                Connection conn, PreparedStatement ps, int bindCount, Map<String, DfColumnMeta> columnInfoMap, int rowNumber)
                throws SQLException {
            return processNumber(schemaTable, columnName, value, conn, ps, bindCount, columnInfoMap, rowNumber);
        }

        @Override
        public String toString() {
            return buildProcessorToString(this);
        }
    }

    protected class UUIDStringProcessor implements StringProcessor {

        public boolean process(String dataDirectory, File dataFile, DfLoadedSchemaTable schemaTable, String columnName, String value,
                Connection conn, PreparedStatement ps, int bindCount, Map<String, DfColumnMeta> columnInfoMap, int rowNumber)
                throws SQLException {
            return processUUID(schemaTable, columnName, value, conn, ps, bindCount, columnInfoMap, rowNumber);
        }

        @Override
        public String toString() {
            return buildProcessorToString(this);
        }
    }

    protected class ArrayStringProcessor implements StringProcessor {

        public boolean process(String dataDirectory, File dataFile, DfLoadedSchemaTable schemaTable, String columnName, String value,
                Connection conn, PreparedStatement ps, int bindCount, Map<String, DfColumnMeta> columnInfoMap, int rowNumber)
                throws SQLException {
            return processArray(schemaTable, columnName, value, ps, bindCount, columnInfoMap, rowNumber);
        }

        @Override
        public String toString() {
            return buildProcessorToString(this);
        }
    }

    protected class XmlStringProcessor implements StringProcessor {

        public boolean process(String dataDirectory, File dataFile, DfLoadedSchemaTable schemaTable, String columnName, String value,
                Connection conn, PreparedStatement ps, int bindCount, Map<String, DfColumnMeta> columnInfoMap, int rowNumber)
                throws SQLException {
            return processXml(schemaTable, columnName, value, ps, bindCount, columnInfoMap, rowNumber);
        }

        @Override
        public String toString() {
            return buildProcessorToString(this);
        }
    }

    protected class LargeTextFileStringProcessor implements StringProcessor {

        public boolean process(String dataDirectory, File dataFile, DfLoadedSchemaTable schemaTable, String columnName, String value,
                Connection conn, PreparedStatement ps, int bindCount, Map<String, DfColumnMeta> columnInfoMap, int rowNumber)
                throws SQLException {
            return processLargeTextFile(dataDirectory, dataFile, schemaTable, columnName, value, ps, bindCount, columnInfoMap, rowNumber);
        }

        @Override
        public String toString() {
            return buildProcessorToString(this);
        }
    }

    protected class BinaryFileStringProcessor implements StringProcessor {

        public boolean process(String dataDirectory, File dataFile, DfLoadedSchemaTable schemaTable, String columnName, String value,
                Connection conn, PreparedStatement ps, int bindCount, Map<String, DfColumnMeta> columnInfoMap, int rowNumber)
                throws SQLException {
            return processBinary(dataDirectory, dataFile, schemaTable, columnName, value, ps, bindCount, columnInfoMap, rowNumber);
        }

        @Override
        public String toString() {
            return buildProcessorToString(this);
        }
    }

    protected class RealStringProcessor implements StringProcessor {

        public boolean process(String dataDirectory, File dataFile, DfLoadedSchemaTable schemaTable, String columnName, String value,
                Connection conn, PreparedStatement ps, int bindCount, Map<String, DfColumnMeta> columnInfoMap, int rowNumber)
                throws SQLException {
            ps.setString(bindCount, value);
            return true;
        }

        @Override
        public String toString() {
            return buildProcessorToString(this);
        }
    }

    protected String buildProcessorToString(StringProcessor processor) {
        // e.g. com.example...FooWriter$RealStringProcessor -> RealStringProcessor
        return Srl.substringLastRear(DfTypeUtil.toClassTitle(processor), "$");
    }

    // -----------------------------------------------------
    //                                                  Date
    //                                                  ----
    protected boolean processDate(String dataDirectory, DfLoadedSchemaTable schemaTable, String columnName, String value, Connection conn,
            PreparedStatement ps, int bindCount, Map<String, DfColumnMeta> columnInfoMap, int rowNumber) throws SQLException {
        if (value == null || value.trim().length() == 0) { // cannot be date
            return false;
        }
        final DfColumnMeta columnMeta = columnInfoMap.get(columnName);
        if (columnMeta != null) {
            final Class<?> columnType = getBindType(schemaTable, columnMeta);
            if (columnType != null) {
                if (!java.util.Date.class.isAssignableFrom(columnType)) {
                    return false;
                }
                final String resolved = resolveRelativeSysdate(dataDirectory, schemaTable, columnName, value); // only when column type specified
                bindNotNullValueByColumnType(schemaTable, columnName, conn, ps, bindCount, resolved, columnType, rowNumber);
                return true;
            }
        }
        // if meta data is not found (basically no way)
        try {
            final Timestamp timestamp = DfTypeUtil.toTimestamp(value);
            ps.setTimestamp(bindCount, timestamp);
            return true;
        } catch (ParseTimestampException ignored) {
            // retry as time
            try {
                Time time = DfTypeUtil.toTime(value);
                ps.setTime(bindCount, time);
                return true;
            } catch (ParseTimeException ignored2) {}
            return false; // couldn't parse as timestamp and time
        }
    }

    protected String resolveRelativeSysdate(String dataDirectory, DfLoadedSchemaTable schemaTable, String columnName, String value) {
        if (value.startsWith(DfRelativeDateResolver.CURRENT_MARK)) {
            return _relativeDateResolver.resolveRelativeSysdate(schemaTable, columnName, value);
        }
        return value;
    }

    // -----------------------------------------------------
    //                                               Boolean
    //                                               -------
    protected boolean processBoolean(DfLoadedSchemaTable schemaTable, String columnName, String value, Connection conn,
            PreparedStatement ps, int bindCount, Map<String, DfColumnMeta> columnInfoMap, int rowNumber) throws SQLException {
        if (value == null || value.trim().length() == 0) { // cannot be boolean
            return false;
        }
        final DfColumnMeta columnInfo = columnInfoMap.get(columnName);
        if (columnInfo != null) {
            final Class<?> columnType = getBindType(schemaTable, columnInfo);
            if (columnType != null) {
                if (!Boolean.class.isAssignableFrom(columnType)) {
                    return false;
                }
                bindNotNullValueByColumnType(schemaTable, columnName, conn, ps, bindCount, value, columnType, rowNumber);
                return true;
            }
        }
        // if meta data is not found (basically no way)
        try {
            final Boolean booleanValue = DfTypeUtil.toBoolean(value);
            ps.setBoolean(bindCount, booleanValue);
            return true;
        } catch (ParseBooleanException ignored) {
            return false; // couldn't parse as boolean
        }
    }

    // -----------------------------------------------------
    //                                                Number
    //                                                ------
    protected boolean processNumber(DfLoadedSchemaTable schemaTable, String columnName, String value, Connection conn, PreparedStatement ps,
            int bindCount, Map<String, DfColumnMeta> columnInfoMap, int rowNumber) throws SQLException {
        if (value == null || value.trim().length() == 0) { // cannot be number
            return false;
        }
        final DfColumnMeta columnInfo = columnInfoMap.get(columnName);
        if (columnInfo != null) {
            final Class<?> columnType = getBindType(schemaTable, columnInfo);
            if (columnType != null) {
                if (!Number.class.isAssignableFrom(columnType)) {
                    return false;
                }
                bindNotNullValueByColumnType(schemaTable, columnName, conn, ps, bindCount, value, columnType, rowNumber);
                return true;
            }
        }
        // if meta data is not found (basically no way)
        value = filterBigDecimalValue(value);
        if (!isBigDecimalValue(value)) {
            return false;
        }
        final BigDecimal bigDecimalValue = getBigDecimalValue(columnName, value);
        try {
            final long longValue = bigDecimalValue.longValueExact();
            ps.setLong(bindCount, longValue);
            return true;
        } catch (ArithmeticException ignored) {
            ps.setBigDecimal(bindCount, bigDecimalValue);
            return true;
        }
    }

    protected String filterBigDecimalValue(String value) {
        if (value == null) {
            return null;
        }
        value = value.trim();
        return value;
    }

    protected boolean isBigDecimalValue(String value) {
        if (value == null) {
            return false;
        }
        try {
            new BigDecimal(value);
            return true;
        } catch (NumberFormatException ignored) {}
        return false;
    }

    protected BigDecimal getBigDecimalValue(String columnName, String value) {
        try {
            return new BigDecimal(value);
        } catch (RuntimeException e) {
            String msg = "The value should be big decimal: columnName=" + columnName + " value=" + value;
            throw new IllegalStateException(msg, e);
        }
    }

    // -----------------------------------------------------
    //                                                  UUID
    //                                                  ----
    protected boolean processUUID(DfLoadedSchemaTable schemaTable, String columnName, String value, Connection conn, PreparedStatement ps,
            int bindCount, Map<String, DfColumnMeta> columnInfoMap, int rowNumber) throws SQLException {
        if (value == null || value.trim().length() == 0) { // cannot be UUID
            return false;
        }
        final DfColumnMeta columnInfo = columnInfoMap.get(columnName);
        if (columnInfo != null) {
            final Class<?> columnType = getBindType(schemaTable, columnInfo);
            if (columnType != null) {
                if (!UUID.class.isAssignableFrom(columnType)) {
                    return false;
                }
                bindNotNullValueByColumnType(schemaTable, columnName, conn, ps, bindCount, value, columnType, rowNumber);
                return true;
            }
        }
        // unsupported when meta data is not found
        return false;
    }

    protected String filterUUIDValue(String value) {
        if (value == null) {
            return null;
        }
        value = value.trim();
        return value;
    }

    // -----------------------------------------------------
    //                                                 ARRAY
    //                                                 -----
    protected boolean processArray(DfLoadedSchemaTable schemaTable, String columnName, String value, PreparedStatement ps, int bindCount,
            Map<String, DfColumnMeta> columnInfoMap, int rowNumber) throws SQLException {
        if (value == null || value.trim().length() == 0) { // cannot be array
            return false;
        }
        final DfColumnMeta columnInfo = columnInfoMap.get(columnName);
        if (columnInfo != null) {
            if (getBasicProperties().isDatabasePostgreSQL()) {
                //rsMeta#getColumnTypeName() returns value starts with "_" if
                //rsMeta#getColumnType() returns Types.ARRAY in PostgreSQL.
                //  e.g. UUID[] -> _uuid
                final int jdbcDefValue = columnInfo.getJdbcDefValue();
                final String dbTypeName = columnInfo.getDbTypeName();
                if (jdbcDefValue != Types.ARRAY || !dbTypeName.startsWith("_")) {
                    return false;
                }
                value = filterArrayValue(value);
                ps.setObject(bindCount, value, Types.OTHER);
                return true;
            }
        }
        // unsupported when meta data is not found
        return false;
    }

    protected String filterArrayValue(String value) {
        if (value == null) {
            return null;
        }
        value = value.trim();
        return value;
    }

    // -----------------------------------------------------
    //                                                   XML
    //                                                   ---
    protected boolean processXml(DfLoadedSchemaTable schemaTable, String columnName, String value, PreparedStatement ps, int bindCount,
            Map<String, DfColumnMeta> columnInfoMap, int rowNumber) throws SQLException {
        if (value == null || value.trim().length() == 0) { // cannot be XML
            return false;
        }
        final DfColumnMeta columnInfo = columnInfoMap.get(columnName);
        if (columnInfo != null) {
            if (getBasicProperties().isDatabasePostgreSQL()) {
                final String dbTypeName = columnInfo.getDbTypeName();
                if (!dbTypeName.startsWith("xml")) {
                    return false;
                }
                value = filterXmlValue(value);
                ps.setObject(bindCount, value, Types.OTHER);
                return true;
            }
        }
        // unsupported when meta data is not found
        return false;
    }

    protected String filterXmlValue(String value) {
        if (value == null) {
            return null;
        }
        value = value.trim();
        return value;
    }

    // -----------------------------------------------------
    //                                       Large Text File
    //                                       ---------------
    // contributed by awaawa, thanks!
    protected boolean processLargeTextFile(String dataDirectory, File dataFile, DfLoadedSchemaTable schemaTable, String columnName,
            String value, PreparedStatement ps, int bindCount, Map<String, DfColumnMeta> columnInfoMap, int rowNumber) throws SQLException {
        if (value == null || value.trim().length() == 0) { // cannot be binary
            return false;
        }
        final DfColumnMeta columnInfo = columnInfoMap.get(columnName);
        if (columnInfo == null) {
            return false; // unsupported when meta data is not found
        }
        final Class<?> columnType = getBindType(schemaTable, columnInfo);
        if (columnType == null) {
            return false; // unsupported too
        }
        if (!isLargeTextFile(dataDirectory, schemaTable, columnName)) {
            return false; // not target as large text file
        }
        // the value should be a path to a text file
        // from data file's current directory
        final String path;
        final String trimmedValue = value.trim();
        if (trimmedValue.startsWith("/")) { // means absolute path
            path = trimmedValue;
        } else {
            final String dataFilePath = Srl.replace(dataFile.getAbsolutePath(), "\\", "/");
            final String baseDirPath = Srl.substringLastFront(dataFilePath, "/");
            path = baseDirPath + "/" + trimmedValue;
        }
        final File textFile = new File(path);
        if (!textFile.exists()) {
            throwLoadDataTextFileReadFailureException(schemaTable, columnName, path, rowNumber);
        }
        try {
            final String read = new FileTextIO().encodeAsUTF8().removeUTF8Bom().read(path);
            ps.setString(bindCount, read);
        } catch (RuntimeException e) {
            throwLoadDataTextFileReadFailureException(schemaTable, columnName, path, rowNumber, e);
        }
        return true;
    }

    // -----------------------------------------------------
    //                                                Binary
    //                                                ------
    // -----------------------------------------------------
    //                                                Binary
    //                                                ------
    protected boolean processBinary(String dataDirectory, File dataFile, DfLoadedSchemaTable schemaTable, String columnName, String value,
            PreparedStatement ps, int bindCount, Map<String, DfColumnMeta> columnInfoMap, int rowNumber) throws SQLException {
        if (value == null || value.trim().length() == 0) { // cannot be binary
            return false;
        }
        final DfColumnMeta columnInfo = columnInfoMap.get(columnName);
        if (columnInfo == null) {
            return false; // unsupported when meta data is not found
        }
        final Class<?> columnType = getBindType(schemaTable, columnInfo);
        if (columnType == null) {
            return false; // unsupported too
        }
        if (!byte[].class.isAssignableFrom(columnType)) { // not binary
            return false;
        }
        // the value should be a path to a binary file
        // from data file's current directory
        final String path;
        final String trimmedValue = value.trim();
        if (trimmedValue.startsWith("/")) { // means absolute path
            path = trimmedValue;
        } else {
            final String dataFilePath = Srl.replace(dataFile.getAbsolutePath(), "\\", "/");
            final String baseDirPath = Srl.substringLastFront(dataFilePath, "/");
            path = baseDirPath + "/" + trimmedValue;
        }
        final File binaryFile = new File(path);
        if (!binaryFile.exists()) {
            throwLoadDataBinaryFileNotFoundException(schemaTable, columnName, path, rowNumber);
        }
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(binaryFile);
            final int fileSize = (int) binaryFile.length();
            final byte[] bytes = new byte[fileSize];
            fis.read(bytes);
            ps.setBytes(bindCount, bytes);
        } catch (IOException e) {
            throwLoadDataBinaryFileReadFailureException(schemaTable, columnName, path, rowNumber, e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ignored) {}
            }
        }
        return true;
    }

    protected void throwLoadDataBinaryFileNotFoundException(DfLoadedSchemaTable schemaTable, String columnName, String path,
            int rowNumber) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The binary file specified at delimiter data was not found.");
        br.addItem("Advice");
        br.addElement("Make sure your path to a binary file is correct.");
        setupOutsideFileBasicItem(schemaTable, columnName, path, rowNumber, br);
        final String msg = br.buildExceptionMessage();
        throw new DfLoadDataRegistrationFailureException(msg);
    }

    protected void throwLoadDataBinaryFileReadFailureException(DfLoadedSchemaTable schemaTable, String columnName, String path,
            int rowNumber, IOException cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to read the binary file.");
        setupOutsideFileBasicItem(schemaTable, columnName, path, rowNumber, br);
        br.addItem("IOException");
        br.addElement(cause.getClass());
        br.addElement(cause.getMessage());
        final String msg = br.buildExceptionMessage();
        throw new DfLoadDataRegistrationFailureException(msg, cause);
    }

    protected void throwLoadDataTextFileReadFailureException(DfLoadedSchemaTable schemaTable, String columnName, String path,
            int rowNumber) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to read the text file.");
        br.addItem("Advice");
        br.addElement("Make sure your path to a text file is correct.");
        setupOutsideFileBasicItem(schemaTable, columnName, path, rowNumber, br);
        final String msg = br.buildExceptionMessage();
        throw new DfLoadDataRegistrationFailureException(msg);
    }

    protected void throwLoadDataTextFileReadFailureException(DfLoadedSchemaTable schemaTable, String columnName, String path, int rowNumber,
            RuntimeException cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to read the text file.");
        setupOutsideFileBasicItem(schemaTable, columnName, path, rowNumber, br);
        br.addItem("Runtime Exception");
        br.addElement(cause.getClass());
        br.addElement(cause.getMessage());
        final String msg = br.buildExceptionMessage();
        throw new DfLoadDataRegistrationFailureException(msg, cause);
    }

    protected void setupOutsideFileBasicItem(DfLoadedSchemaTable schemaTable, String columnName, String path, int rowNumber,
            ExceptionMessageBuilder br) {
        br.addItem("Table");
        br.addElement(schemaTable);
        br.addItem("Column");
        br.addElement(columnName);
        br.addItem("File Path");
        br.addElement(path);
        br.addItem("Row Number");
        br.addElement(rowNumber);
    }

    // ===================================================================================
    //                                                                          Bind Value
    //                                                                          ==========
    /**
     * Bind not null value by bind type of column. <br>
     * This contains type conversion of value.
     * @param schemaTable The name of table with schema. (NotNull)
     * @param columnName The name of column. (NotNull)
     * @param conn The connection for the database. (NotNull)
     * @param ps The prepared statement. (NotNull)
     * @param bindCount The count of binding.
     * @param value The bound value. (NotNull)
     * @param bindType The bind type of the column. (NotNull)
     * @param rowNumber The row number of the current value.
     * @throws SQLException When it fails to handle the SQL.
     */
    protected void bindNotNullValueByColumnType(DfLoadedSchemaTable schemaTable, String columnName, Connection conn, PreparedStatement ps,
            int bindCount, Object value, Class<?> bindType, int rowNumber) throws SQLException {
        final ValueType valueType = TnValueTypes.getValueType(bindType);
        try {
            valueType.bindValue(conn, ps, bindCount, value);
        } catch (RuntimeException e) {
            throwColumnValueBindingFailureException(schemaTable, columnName, value, bindType, valueType, rowNumber, e);
        } catch (SQLException e) {
            throwColumnValueBindingSQLException(schemaTable, columnName, value, bindType, valueType, rowNumber, e);
        }
    }

    protected void throwColumnValueBindingFailureException(DfLoadedSchemaTable schemaTable, String columnName, Object value,
            Class<?> bindType, ValueType valueType, int rowNumber, RuntimeException cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to bind the value with ValueType for the column type.");
        br.addItem("Advice");
        br.addElement("Confirm the nested RuntimeException's message.");
        br.addElement("The bound value might not be to match the type.");
        br.addItem("Table Name");
        br.addElement(schemaTable);
        br.addItem("Column Name");
        br.addElement(columnName);
        br.addItem("Bind Type");
        br.addElement(bindType);
        br.addItem("Value Type");
        br.addElement(valueType);
        br.addItem("Bound Value");
        br.addElement(value);
        br.addItem("Row Number");
        br.addElement(rowNumber);
        br.addItem("RuntimeException");
        br.addElement(cause.getClass());
        br.addElement(cause.getMessage());
        final String msg = br.buildExceptionMessage();
        throw new DfLoadDataRegistrationFailureException(msg, cause);
    }

    protected void throwColumnValueBindingSQLException(DfLoadedSchemaTable schemaTable, String columnName, Object value, Class<?> bindType,
            ValueType valueType, int rowNumber, SQLException cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to bind the value with ValueType for the column type.");
        br.addItem("Advice");
        br.addElement("Confirm the nested SQLException's message.");
        br.addElement("The bound value might not be to match the type.");
        br.addItem("Table Name");
        br.addElement(schemaTable);
        br.addItem("Column Name");
        br.addElement(columnName);
        br.addItem("Bind Type");
        br.addElement(bindType);
        br.addItem("Value Type");
        br.addElement(valueType);
        br.addItem("Bound Value");
        br.addElement(value);
        br.addItem("Row Number");
        br.addElement(rowNumber);
        br.addItem("SQLException");
        br.addElement(cause.getClass());
        br.addElement(cause.getMessage());
        final String msg = br.buildExceptionMessage();
        throw new DfLoadDataRegistrationFailureException(msg, cause);
    }

    /**
     * Bind not null value by instance.
     * @param schemaTable The name of the target table with schema. (NotNull)
     * @param columnName The name of the target column. (NotNull)
     * @param conn The connection to the database. (NotNull)
     * @param ps The prepared statement. (NotNull)
     * @param bindCount The count of binding.
     * @param obj The bound value. (NotNull)
     * @param rowNumber The row number of the current value.
     * @throws SQLException When it fails to handle the SQL.
     */
    protected void bindNotNullValueByInstance(DfLoadedSchemaTable schemaTable, String columnName, Connection conn, PreparedStatement ps,
            int bindCount, Object obj, int rowNumber) throws SQLException {
        bindNotNullValueByColumnType(schemaTable, columnName, conn, ps, bindCount, obj, obj.getClass(), rowNumber);
    }

    // ===================================================================================
    //                                                                    Column Bind Type
    //                                                                    ================
    /**
     * Get the bind type to find a value type.
     * @param schemaTable The name of table corresponding to column with schema. (NotNull)
     * @param columnMeta The meta info of column. (NotNull)
     * @return The type of column. (NullAllowed: However Basically NotNull)
     */
    protected Class<?> getBindType(DfLoadedSchemaTable schemaTable, DfColumnMeta columnMeta) {
        final String onfileTableName = schemaTable.getOnfileTableName();
        Map<String, Class<?>> cacheMap = _bindTypeCacheMap.get(onfileTableName);
        if (cacheMap == null) {
            cacheMap = StringKeyMap.createAsFlexibleOrdered();
            _bindTypeCacheMap.put(onfileTableName, cacheMap);
        }
        final String columnName = columnMeta.getColumnName();
        Class<?> bindType = cacheMap.get(columnName);
        if (bindType != null) { // cache hit
            return bindType;
        }

        // use mapped JDBC defined value if found (basically found)
        // because it has already been resolved about JDBC specification per DBMS
        final String jdbcType = _columnHandler.getColumnJdbcType(columnMeta);
        Integer jdbcDefValue = TypeMap.getJdbcDefValueByJdbcType(jdbcType);
        if (jdbcDefValue == null) { // basically no way
            jdbcDefValue = columnMeta.getJdbcDefValue(); // as plain
        }

        // ReplaceSchema uses an own original mapping way
        // (not uses Generate mapping)
        // it's simple mapping (for string processor)
        if (jdbcDefValue == Types.CHAR || jdbcDefValue == Types.VARCHAR || jdbcDefValue == Types.LONGVARCHAR
                || jdbcDefValue == Types.CLOB) {
            bindType = String.class;
        } else if (jdbcDefValue == Types.TINYINT || jdbcDefValue == Types.SMALLINT || jdbcDefValue == Types.INTEGER) {
            bindType = Integer.class;
        } else if (jdbcDefValue == Types.BIGINT) {
            bindType = Long.class;
        } else if (jdbcDefValue == Types.DECIMAL || jdbcDefValue == Types.NUMERIC) {
            bindType = BigDecimal.class;
        } else if (jdbcDefValue == Types.REAL || jdbcDefValue == Types.FLOAT || jdbcDefValue == Types.DOUBLE) {
            bindType = BigDecimal.class;
        } else if (jdbcDefValue == Types.TIMESTAMP) {
            bindType = Timestamp.class;
        } else if (jdbcDefValue == Types.TIME) {
            bindType = Time.class;
        } else if (jdbcDefValue == Types.DATE) {
            // it depends on value type settings
            // that which is bound java.sql.Date or java.sql.Timestamp
            bindType = java.util.Date.class;
        } else if (jdbcDefValue == Types.BIT || jdbcDefValue == Types.BOOLEAN) {
            bindType = Boolean.class;
        } else if (jdbcDefValue == Types.BINARY || jdbcDefValue == Types.VARBINARY || jdbcDefValue == Types.LONGVARBINARY
                || jdbcDefValue == Types.BLOB) {
            bindType = byte[].class;
        } else if (jdbcDefValue == Types.OTHER && TypeMap.UUID.equalsIgnoreCase(jdbcType)) {
            // [UUID Headache]: The reason why UUID type has not been supported yet on JDBC.
            bindType = UUID.class;
        } else {
            bindType = Object.class;
        }
        cacheMap.put(columnName, bindType);
        return bindType;
    }

    // ===================================================================================
    //                                                                         Column Meta
    //                                                                         ===========
    protected Map<String, DfColumnMeta> prepareColumnMetaMap(DfLoadedSchemaTable schemaTable) {
        final String onfileTableName = schemaTable.getOnfileTableName();
        if (_columnMetaCacheMap.containsKey(onfileTableName)) {
            return _columnMetaCacheMap.get(onfileTableName);
        }
        prepareTableCaseTranslationIfNeeds(); // because the name might be user favorite case name
        final Map<String, DfColumnMeta> columnMetaMap = StringKeyMap.createAsFlexible();
        Connection conn = null;
        try {
            conn = _dataSource.getConnection();
            final DatabaseMetaData metaData = conn.getMetaData();
            final UnifiedSchema unifiedSchema = schemaTable.getUnifiedSchema();
            final String tablePureName = schemaTable.getTablePureName();
            final List<DfColumnMeta> columnList;
            columnList = _columnHandler.getColumnList(metaData, unifiedSchema, tablePureName);
            for (DfColumnMeta columnMeta : columnList) {
                columnMetaMap.put(columnMeta.getColumnName(), columnMeta);
            }
            _columnMetaCacheMap.put(onfileTableName, columnMetaMap);
            return columnMetaMap;
        } catch (SQLException e) {
            String msg = "Failed to get column meta informations: table=" + schemaTable;
            throw new IllegalStateException(msg, e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    protected void prepareTableCaseTranslationIfNeeds() {
        if (_columnHandler.isEnableTableCaseTranslation()) {
            return;
        }
        Connection conn = null;
        try {
            conn = _dataSource.getConnection();
            final DatabaseMetaData metaData = conn.getMetaData();
            final List<DfTableMeta> tableList = _tableHandler.getTableList(metaData, _unifiedSchema);
            final List<String> tableNameList = new ArrayList<String>();
            for (DfTableMeta meta : tableList) {
                tableNameList.add(meta.getTableDbName());
            }
            _columnHandler.enableTableCaseTranslation(tableNameList);
        } catch (SQLException e) {
            String msg = "Failed to get meta data of tables.";
            throw new IllegalStateException(msg, e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    // ===================================================================================
    //                                                                        Log Handling
    //                                                                        ============
    protected void handleLoggingInsert(DfLoadedSchemaTable schemaTable, Map<String, Object> columnValueMap,
            LoggingInsertType loggingInsertType, int rowNumber, String preparedSql) {
        if (rowNumber == 1) { // first line
            _log.info(preparedSql); // to confirm insert SQL (e.g. schema prefix)
        }
        boolean logging = false;
        if (LoggingInsertType.ALL.equals(loggingInsertType)) {
            logging = true;
        } else if (LoggingInsertType.PART.equals(loggingInsertType)) {
            if (rowNumber <= 10) { // first 10 lines
                logging = true;
            } else if (rowNumber == 11) {
                _log.info(schemaTable.getOnfileTableName() + ":{... more several records}");
            }
        }
        if (logging) {
            final List<Object> valueList = new ArrayList<Object>(columnValueMap.values());
            _log.info(buildLoggingInsert(schemaTable, valueList));
        }
    }

    protected String buildLoggingInsert(DfLoadedSchemaTable schemaTable, final List<? extends Object> bindParameters) {
        final StringBuilder sb = new StringBuilder();
        for (Object parameter : bindParameters) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(parameter);
        }
        return schemaTable.getOnfileTableName() + ":{" + sb.toString() + "}";
    }

    protected void noticeLoadedRowSize(DfLoadedSchemaTable schemaTable, int rowSize) {
        _log.info(" -> " + rowSize + " rows are loaded to " + schemaTable.getOnfileTableName());
    }

    // ===================================================================================
    //                                                             Implicit Classification
    //                                                             =======================
    protected void checkImplicitClassification(File file, DfLoadedSchemaTable schemaTable, List<String> columnNameList) {
        if (_suppressCheckImplicitSet) {
            return;
        }
        // lazy because classification might have no classification data on table
        // (initialized with table and implicit classification at the same timing)
        _implicitClassificationLazyCheckerList.add(new DfLoadedClassificationLazyChecker() {
            public void check(Connection conn) throws SQLException {
                final DfClassificationProperties prop = getClassificationProperties();
                if (!prop.isCheckReplaceSchemaImplicitClassificationCode()) {
                    return;
                }
                final DfImplicitClassificationChecker checker = new DfImplicitClassificationChecker();
                for (String columnName : columnNameList) {
                    final String onfileTableName = schemaTable.getOnfileTableName();
                    if (prop.hasImplicitClassification(onfileTableName, columnName)) {
                        checker.check(file, schemaTable, columnName, conn, onfileTableName);
                    } else { // retry
                        if (onfileTableName.contains(".")) { // has schema prefix on file name
                            // #for_now jflute pure searching is only for main schema (2025/04/28)
                            // additional schema may have same name in other schema so ambiguous
                            if (schemaTable.getUnifiedSchema().isMainSchema()) {
                                final String tablePureName = schemaTable.getTablePureName();
                                if (prop.hasImplicitClassification(tablePureName, columnName)) {
                                    checker.check(file, schemaTable, columnName, conn, tablePureName);
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    // ===================================================================================
    //                                                                       Data Property
    //                                                                       =============
    // -----------------------------------------------------
    //                                                 Basic
    //                                                 -----
    protected Map<String, Map<String, String>> getConvertValueMap(String dataDirectory) {
        return _convertValueProp.findConvertValueMap(dataDirectory);
    }

    protected Map<String, String> getDefaultValueMap(String dataDirectory) {
        return _defaultValueProp.findDefaultValueMap(dataDirectory);
    }

    // -----------------------------------------------------
    //                                       Loading Control
    //                                       ---------------
    protected LoggingInsertType getLoggingInsertType(String dataDirectory) {
        return _loadingControlProp.getLoggingInsertType(dataDirectory, _loggingInsertSql);
    }

    protected boolean isMergedSuppressBatchUpdate(String dataDirectory) {
        return _loadingControlProp.isMergedSuppressBatchUpdate(dataDirectory, _suppressBatchUpdate);
    }

    protected boolean isCheckColumnDef(String dataDirectory) {
        if (isSuppressCheckColumnDef()) { // basically for SavePrevious
            return false;
        }
        return _loadingControlProp.isCheckColumnDef(dataDirectory);
    }

    protected void checkColumnDef(File dataFile, DfLoadedSchemaTable schemaTable, List<String> columnDefNameList,
            Map<String, DfColumnMeta> columnMetaMap) {
        _loadingControlProp.checkColumnDef(dataFile, schemaTable, columnDefNameList, columnMetaMap);
    }

    protected void resolveRelativeDate(String dataDirectory, DfLoadedSchemaTable schemaTable, Map<String, Object> columnValueMap,
            Map<String, DfColumnMeta> columnMetaMap, Set<String> sysdateColumnSet, int rowNumber) {
        _loadingControlProp.resolveRelativeDate(dataDirectory, schemaTable, columnValueMap, columnMetaMap, sysdateColumnSet,
                createBindTypeProvider(), rowNumber);
    }

    protected DfColumnBindTypeProvider createBindTypeProvider() {
        return new DfColumnBindTypeProvider() {
            public Class<?> provide(DfLoadedSchemaTable schemaTable, DfColumnMeta columnMeta) {
                return getBindType(schemaTable, columnMeta);
            }
        };
    }

    protected boolean isRTrimCellValue(String dataDirectory) {
        return _loadingControlProp.isRTrimCellValue(dataDirectory);
    }

    protected boolean isLargeTextFile(String dataDirectory, DfLoadedSchemaTable schemaTable, String columnName) {
        return _loadingControlProp.isLargeTextFile(dataDirectory, schemaTable, columnName);
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

    protected DfClassificationProperties getClassificationProperties() {
        return getProperties().getClassificationProperties();
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String ln() {
        return DBFluteSystem.ln();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public boolean isLoggingInsertSql() {
        return _loggingInsertSql;
    }

    public void setLoggingInsertSql(boolean loggingInsertSql) {
        _loggingInsertSql = loggingInsertSql;
    }

    public boolean isSuppressBatchUpdate() {
        return _suppressBatchUpdate;
    }

    public void setSuppressBatchUpdate(boolean suppressBatchUpdate) {
        _suppressBatchUpdate = suppressBatchUpdate;
    }

    public boolean isSuppressCheckColumnDef() {
        return _suppressCheckColumnDef;
    }

    public void setSuppressCheckColumnDef(boolean suppressCheckColumnDef) {
        _suppressCheckColumnDef = suppressCheckColumnDef;
    }

    public boolean isSuppressCheckImplicitSet() {
        return _suppressCheckImplicitSet;
    }

    public void setSuppressCheckImplicitSet(boolean suppressCheckImplicitSet) {
        _suppressCheckImplicitSet = suppressCheckImplicitSet;
    }

    public DfDataWritingInterceptor getDataWritingInterceptor() {
        return _dataWritingInterceptor;
    }

    public void setDataWritingInterceptor(DfDataWritingInterceptor dataWritingInterceptor) {
        _dataWritingInterceptor = dataWritingInterceptor;
    }

    public List<DfLoadedClassificationLazyChecker> getImplicitClassificationLazyCheckerList() {
        return _implicitClassificationLazyCheckerList;
    }

    public DfConvertValueProp getConvertValueProp() {
        return _convertValueProp;
    }

    public void setConvertValueProp(DfConvertValueProp convertValueProp) {
        _convertValueProp = convertValueProp;
    }

    public DfDefaultValueProp getDefaultValueProp() {
        return _defaultValueProp;
    }

    public void setDefaultValueProp(DfDefaultValueProp defaultValueProp) {
        _defaultValueProp = defaultValueProp;
    }

    public DfLoadingControlProp getLoadingControlProp() {
        return _loadingControlProp;
    }

    public void setLoadingControlProp(DfLoadingControlProp loadingControlProp) {
        _loadingControlProp = loadingControlProp;
    }
}
