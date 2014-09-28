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
package org.seasar.dbflute.logic.sql2entity.cmentity;

import java.io.UnsupportedEncodingException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.engine.database.model.TypeMap;
import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.exception.DfJDBCException;
import org.seasar.dbflute.exception.DfProcedureExecutionMetaGettingFailureException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.jdbc.ValueType;
import org.seasar.dbflute.logic.jdbc.metadata.basic.DfColumnExtractor;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfColumnMeta;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfProcedureColumnMeta;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfProcedureColumnMeta.DfProcedureColumnType;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfProcedureMeta;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfProcedureNotParamResultMeta;
import org.seasar.dbflute.properties.DfBasicProperties;
import org.seasar.dbflute.properties.DfOutsideSqlProperties;
import org.seasar.dbflute.properties.DfTypeMappingProperties;
import org.seasar.dbflute.s2dao.valuetype.TnValueTypes;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.DfTypeUtil;

/**
 * @author jflute
 * @since 0.7.5 (2008/06/28 Saturday)
 */
public class DfProcedureExecutionMetaExtractor {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log _log = LogFactory.getLog(DfProcedureExecutionMetaExtractor.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfCustomizeEntityMetaExtractor _extractor = new DfCustomizeEntityMetaExtractor();
    protected final DfColumnExtractor _columnHandler = new DfColumnExtractor();
    protected final ValueType _stringType = TnValueTypes.STRING;
    protected final ValueType _stringClobType = TnValueTypes.STRING_CLOB;
    protected final ValueType _bytesOidType = TnValueTypes.BYTES_OID;
    protected final ValueType _fixedLengthStringType = TnValueTypes.FIXED_LENGTH_STRING;
    protected final ValueType _objectBindingBigDecimalType = TnValueTypes.OBJECT_BINDING_BIGDECIMAL;
    protected final ValueType _uuidAsDirectType = TnValueTypes.UUID_AS_DIRECT;
    protected final ValueType _uuidAsStringType = TnValueTypes.UUID_AS_STRING;
    protected final ValueType _postgreSqlResultSetType = TnValueTypes.POSTGRESQL_RESULT_SET;
    protected final ValueType _oracleResultSetType = TnValueTypes.ORACLE_RESULT_SET;
    protected final Map<String, String> _continuedFailureMessageMap = DfCollectionUtil.newLinkedHashMap();

    // ===================================================================================
    //                                                                             Process
    //                                                                             =======
    public void extractExecutionMetaData(DataSource dataSource, List<DfProcedureMeta> procedureList)
            throws SQLException {
        final DfOutsideSqlProperties prop = getProperties().getOutsideSqlProperties();
        for (DfProcedureMeta procedure : procedureList) {
            final String procedureFullQualifiedName = procedure.getProcedureFullQualifiedName();
            final String procedureSchemaQualifiedName = procedure.getProcedureSchemaQualifiedName();
            final String procedureName = procedure.getProcedureName();
            if (prop.isExecutionMetaProcedureName(procedureFullQualifiedName)
                    || prop.isExecutionMetaProcedureName(procedureSchemaQualifiedName)
                    || prop.isExecutionMetaProcedureName(procedureName)) {
                doExtractExecutionMetaData(dataSource, procedure);
            }
        }
    }

    protected void doExtractExecutionMetaData(DataSource dataSource, DfProcedureMeta procedure) throws SQLException {
        final List<DfProcedureColumnMeta> columnList = procedure.getProcedureColumnList();
        if (!needsToCall(columnList)) {
            final String name = procedure.buildProcedureLoggingName();
            _log.info("...Skipping unneeded call: " + name + " params=" + buildParameterTypeView(columnList));
            return;
        }
        final List<Object> testValueList = DfCollectionUtil.newArrayList();
        setupTestValueList(columnList, testValueList);
        final boolean existsReturn = existsReturnValue(columnList);
        final String sql = createSql(procedure, existsReturn, true);
        Connection conn = null;
        CallableStatement cs = null;
        boolean beginTransaction = false;
        try {
            _log.info("...Calling: " + sql);
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            beginTransaction = true;
            cs = conn.prepareCall(sql);
            final List<DfProcedureColumnMeta> boundColumnList = DfCollectionUtil.newArrayList();
            setupBindParameter(conn, cs, columnList, testValueList, boundColumnList);

            boolean executed;
            try {
                executed = cs.execute();
            } catch (SQLException e) { // retry without escape because Oracle sometimes hates escape
                final String retrySql = createSql(procedure, existsReturn, false);
                try {
                    try {
                        cs.close();
                    } catch (SQLException ignored) {
                    }
                    cs = conn.prepareCall(retrySql);
                    setupBindParameter(conn, cs, columnList, testValueList, boundColumnList);
                    executed = cs.execute();
                    _log.info("  (o) retry: " + retrySql);
                } catch (SQLException ignored) {
                    _log.info("  (x) retry: " + retrySql);
                    throw e;
                }
            }
            if (executed) {
                int closetIndex = 0;
                do {
                    ResultSet rs = null;
                    try {
                        rs = cs.getResultSet();
                        if (rs == null) {
                            break;
                        }
                        final Map<String, DfColumnMeta> columnMetaInfoMap = extractColumnMetaInfoMap(rs, sql);
                        final DfProcedureNotParamResultMeta notParamResult = new DfProcedureNotParamResultMeta();
                        final String propertyName;
                        if (procedure.isCalledBySelect() && closetIndex == 0) {
                            // for example, table valued function
                            // if the procedure of this type does not have
                            // second or more result set basically
                            // but checks closetIndex just in case
                            propertyName = "returnResult";
                        } else { // basically here
                            propertyName = "notParamResult" + (closetIndex + 1);
                        }
                        notParamResult.setPropertyName(propertyName);
                        notParamResult.setResultSetColumnInfoMap(columnMetaInfoMap);
                        procedure.addNotParamResult(notParamResult);
                        ++closetIndex;
                    } finally {
                        closeResult(rs);
                    }
                } while (cs.getMoreResults());
            }
            int index = 0;
            for (DfProcedureColumnMeta column : boundColumnList) {
                final DfProcedureColumnType columnType = column.getProcedureColumnType();
                if (DfProcedureColumnType.procedureColumnIn.equals(columnType)) {
                    ++index;
                    continue;
                }
                final int paramIndex = (index + 1);
                final Object obj;
                if (column.isPostgreSQLCursor()) {
                    obj = _postgreSqlResultSetType.getValue(cs, paramIndex);
                } else if (column.isOracleCursor()) {
                    obj = _oracleResultSetType.getValue(cs, paramIndex);
                } else {
                    obj = cs.getObject(paramIndex); // as default
                }
                if (obj instanceof ResultSet) {
                    ResultSet rs = null;
                    try {
                        rs = (ResultSet) obj;
                        final Map<String, DfColumnMeta> columnMetaInfoMap = extractColumnMetaInfoMap(rs, sql);
                        column.setResultSetColumnInfoMap(columnMetaInfoMap);
                    } finally {
                        closeResult(rs);
                    }
                }
                ++index;
            }
        } catch (SQLException e) {
            final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
            br.addNotice("Failed to execute the procedure for getting meta data.");
            br.addItem("SQL");
            br.addElement(sql);
            br.addItem("Parameter");
            for (DfProcedureColumnMeta column : columnList) {
                br.addElement(column.getColumnDisplayName());
            }
            br.addItem("Test Value");
            br.addElement(buildTestValueDisp(testValueList));
            br.addItem("Exception Message");
            br.addElement(DfJDBCException.extractMessage(e));
            final SQLException nextEx = e.getNextException();
            if (nextEx != null) {
                br.addElement(DfJDBCException.extractMessage(nextEx));
            }
            final String msg = br.buildExceptionMessage();
            final DfOutsideSqlProperties prop = getProperties().getOutsideSqlProperties();
            if (prop.hasSpecifiedExecutionMetaProcedure()) {
                throw new DfProcedureExecutionMetaGettingFailureException(msg, e);
            } else { // if no specified, it continues
                _continuedFailureMessageMap.put(procedure.getProcedureFullQualifiedName(), msg);
                _log.info("*Failed to call so read the warning message diplayed later");
            }
        } finally {
            if (cs != null) {
                cs.close();
            }
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException continued) { // one day Oracle suddenly threw it (by socket trouble?)
                    final String exp = DfJDBCException.extractMessage(continued);
                    _log.info("*Failed to roll-back the procedure call but continued: " + exp);
                }
                if (beginTransaction) {
                    try {
                        conn.setAutoCommit(true);
                    } catch (SQLException continued) {
                        final String exp = DfJDBCException.extractMessage(continued);
                        _log.info("*Failed to set auto-commit true: " + exp);
                    }
                }
            }
        }
    }

    protected String buildTestValueDisp(List<Object> testValueList) {
        final StringBuilder sb = new StringBuilder();
        sb.append("{");
        int index = 0;
        for (Object value : testValueList) {
            if (index > 0) {
                sb.append(", ");
            }
            if (value instanceof String) {
                sb.append("\"").append(value).append("\"");
            } else {
                sb.append(value);
            }
            ++index;
        }
        sb.append("}");
        return sb.toString();
    }

    protected boolean existsReturnValue(List<DfProcedureColumnMeta> columnList) {
        for (DfProcedureColumnMeta column : columnList) {
            final DfProcedureColumnType columnType = column.getProcedureColumnType();
            if (DfProcedureColumnType.procedureColumnReturn.equals(columnType)) {
                return true;
            }
        }
        return false;
    }

    protected boolean needsToCall(List<DfProcedureColumnMeta> columnList) {
        if (!isOracle() && !isPostgreSQL()) {
            return true; // because of for getting notParamResult
        }
        // Here Oracle or PostgreSQL (that don't support notParamResult)
        for (DfProcedureColumnMeta column : columnList) {
            final DfProcedureColumnType columnType = column.getProcedureColumnType();
            if (DfProcedureColumnType.procedureColumnOut.equals(columnType)
                    || DfProcedureColumnType.procedureColumnInOut.equals(columnType)
                    || DfProcedureColumnType.procedureColumnReturn.equals(columnType)) {
                return true;
            }
        }
        return false;
    }

    protected String buildParameterTypeView(List<DfProcedureColumnMeta> columnList) {
        final StringBuilder sb = new StringBuilder();
        final String prefix = "procedureColumn";
        for (DfProcedureColumnMeta column : columnList) {
            String name = column.getProcedureColumnType().name();
            if (name.startsWith(prefix)) {
                name = name.substring(prefix.length());
            }
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(name);
        }
        sb.insert(0, "{").append("}");
        return sb.toString();
    }

    protected void setupTestValueList(List<DfProcedureColumnMeta> columnList, List<Object> testValueList) {
        for (DfProcedureColumnMeta column : columnList) {
            doSetupTestValueList(column, testValueList);
        }
    }

    protected void doSetupTestValueList(DfProcedureColumnMeta column, List<Object> testValueList) {
        if (!column.isInputParameter()) {
            return;
        }
        // mapping by DB type name as pinpoint patch
        if (column.isPostgreSQLUuid() || column.isSQLServerUniqueIdentifier()) {
            testValueList.add("FD8C7155-3A0A-DB11-BAC4-0011F5099158");
            return;
        }

        // mapping by JDBC type
        final String stringValue = "0";
        final String jdbcType = findJdbcType(column);
        final String javaNative = findNativeType(jdbcType, column);
        final Object testValue; // cannot be null
        if (isJavaNativeStringObject(javaNative)) {
            testValue = stringValue;
        } else if (isJavaNativeNumberObject(javaNative)) {
            testValue = 0;
        } else if (isJavaNativeDateObject(javaNative)) {
            if (TypeMap.isJdbcTypeDate(jdbcType)) {
                // Oracle date is mapped to java.util.Date in Generate task
                // but this ignores it because of execution only here
                testValue = DfTypeUtil.toSqlDate("2006-09-26");
            } else if (TypeMap.isJdbcTypeTime(jdbcType)) {
                testValue = DfTypeUtil.toTime("18:21:00");
            } else {
                testValue = DfTypeUtil.toTimestamp("2006-09-26 18:21:00");
            }
        } else if (isJavaNativeBooleanObject(javaNative)) {
            testValue = Boolean.FALSE;
        } else if (isJavaNativeBinaryObject(javaNative)) {
            final String encoding = "UTF-8";
            try {
                testValue = stringValue.getBytes(encoding);
            } catch (UnsupportedEncodingException e) {
                String msg = "Unsupported encoding: " + encoding;
                throw new IllegalStateException(msg, e);
            }
        } else { // as string
            testValue = stringValue;
        }
        testValueList.add(testValue);
    }

    protected String findJdbcType(DfProcedureColumnMeta column) {
        final int jdbcDefType = column.getJdbcDefType();
        final String dbTypeName = column.getDbTypeName();
        return _columnHandler.getColumnJdbcType(jdbcDefType, dbTypeName);
    }

    protected String findNativeType(String jdbcType, DfProcedureColumnMeta column) {
        final Integer columnSize = column.getColumnSize();
        final Integer decimalDigits = column.getDecimalDigits();
        return TypeMap.findJavaNativeByJdbcType(jdbcType, columnSize, decimalDigits);
    }

    protected boolean isJavaNativeStringObject(String javaNative) {
        return getTypeMappingProperties().isJavaNativeStringObject(javaNative);
    }

    protected boolean isJavaNativeNumberObject(String javaNative) {
        return getTypeMappingProperties().isJavaNativeNumberObject(javaNative);
    }

    protected boolean isJavaNativeDateObject(String javaNative) {
        return getTypeMappingProperties().isJavaNativeDateObject(javaNative);
    }

    protected boolean isJavaNativeBooleanObject(String javaNative) {
        return getTypeMappingProperties().isJavaNativeBooleanObject(javaNative);
    }

    protected boolean isJavaNativeBinaryObject(String javaNative) {
        return getTypeMappingProperties().isJavaNativeBinaryObject(javaNative);
    }

    public String createSql(DfProcedureMeta procedure, boolean existsReturn, boolean escape) {
        final boolean calledBySelect = procedure.isCalledBySelect();
        if (calledBySelect) {
            existsReturn = false;
            escape = false;
        }
        final String procedureSqlName = procedure.buildProcedureSqlName();
        final int bindSize = procedure.getBindParameterCount();
        final StringBuilder sb = new StringBuilder();
        if (escape) {
            sb.append("{");
        }
        final int argSize;
        {
            if (existsReturn) {
                sb.append("? = ");
                argSize = bindSize - 1;
            } else {
                argSize = bindSize;
            }
        }
        if (calledBySelect) {
            sb.append("select * from ");
        } else {
            sb.append("call ");
        }
        sb.append(procedureSqlName).append("(");
        for (int i = 0; i < argSize; i++) {
            sb.append("?, ");
        }
        if (argSize > 0) {
            sb.setLength(sb.length() - 2);
        }
        sb.append(")");
        if (escape) {
            sb.append("}");
        }
        return sb.toString();
    }

    protected void setupBindParameter(Connection conn, CallableStatement cs, List<DfProcedureColumnMeta> columnList,
            List<Object> testValueList, List<DfProcedureColumnMeta> boundColumnList) throws SQLException {
        boundColumnList.clear();
        int index = 0;
        int testValueIndex = 0;
        for (DfProcedureColumnMeta column : columnList) {
            if (!column.isBindParameter()) {
                continue; // must be no increment
            }
            final int paramIndex = (index + 1);
            final DfProcedureColumnType columnType = column.getProcedureColumnType();
            final int jdbcDefType = column.getJdbcDefType();
            if (DfProcedureColumnType.procedureColumnReturn.equals(columnType)) {
                registerOutParameter(conn, cs, paramIndex, jdbcDefType, column);
                boundColumnList.add(column);
            } else if (DfProcedureColumnType.procedureColumnIn.equals(columnType)) {
                bindObject(conn, cs, paramIndex, jdbcDefType, testValueList.get(testValueIndex), column);
                ++testValueIndex;
                boundColumnList.add(column);
            } else if (DfProcedureColumnType.procedureColumnOut.equals(columnType)) {
                registerOutParameter(conn, cs, paramIndex, jdbcDefType, column);
                boundColumnList.add(column);
            } else if (DfProcedureColumnType.procedureColumnInOut.equals(columnType)) {
                registerOutParameter(conn, cs, paramIndex, jdbcDefType, column);
                bindObject(conn, cs, paramIndex, jdbcDefType, testValueList.get(testValueIndex), column);
                ++testValueIndex;
                boundColumnList.add(column);
            }
            ++index;
        }
    }

    protected void registerOutParameter(Connection conn, CallableStatement cs, int paramIndex, int jdbcDefType,
            DfProcedureColumnMeta column) throws SQLException {
        final ValueType valueType;
        {
            final ValueType forcedType = getForcedValueType(column);
            if (forcedType != null) {
                valueType = forcedType;
            } else {
                if (column.isPostgreSQLCursor()) {
                    valueType = _postgreSqlResultSetType;
                } else if (column.isOracleCursor()) {
                    valueType = _oracleResultSetType;
                } else {
                    valueType = TnValueTypes.getValueType(jdbcDefType);
                }
            }
        }
        try {
            if (column.isOracleTreatedAsArray() && column.hasTypeArrayInfo()) {
                cs.registerOutParameter(paramIndex, Types.ARRAY, column.getTypeArrayInfo().getTypeSqlName());
            } else if (column.isOracleStruct() && column.hasTypeStructInfo()) {
                cs.registerOutParameter(paramIndex, Types.STRUCT, column.getTypeStructInfo().getTypeSqlName());
            } else {
                valueType.registerOutParameter(conn, cs, paramIndex);
            }
        } catch (SQLException e) {
            String msg = buildOutParameterExceptionMessage(paramIndex, jdbcDefType, column, valueType);
            throw new DfJDBCException(msg, e);
        } catch (RuntimeException e) {
            String msg = buildOutParameterExceptionMessage(paramIndex, jdbcDefType, column, valueType);
            throw new IllegalStateException(msg, e);
        }
    }

    protected String buildOutParameterExceptionMessage(int paramIndex, int jdbcDefType, DfProcedureColumnMeta column,
            ValueType valueType) {
        String msg = "Failed to register OUT parameter(" + paramIndex + "|" + jdbcDefType + "):";
        msg = msg + " " + column.getColumnNameDisp() + " - " + column.getColumnDefinitionLineDisp();
        msg = msg + " :: " + valueType.getClass().getName();
        return msg;
    }

    protected void bindObject(Connection conn, CallableStatement cs, int paramIndex, int jdbcDefType, Object value,
            DfProcedureColumnMeta column) throws SQLException {
        final ValueType valueType;
        {
            final ValueType forcedType = getForcedValueType(column);
            if (forcedType != null) {
                valueType = forcedType;
            } else {
                valueType = TnValueTypes.findByValueOrJdbcDefType(value, jdbcDefType);
            }
        }
        try {
            if (column.isOracleTreatedAsArray() && column.hasTypeArrayInfo()) {
                cs.setNull(paramIndex, Types.ARRAY, column.getTypeArrayInfo().getTypeSqlName());
            } else if (column.isOracleStruct() && column.hasTypeStructInfo()) {
                cs.setNull(paramIndex, Types.STRUCT, column.getTypeStructInfo().getTypeSqlName());
            } else {
                valueType.bindValue(conn, cs, paramIndex, value);
            }
        } catch (SQLException e) {
            String msg = buildBindingExceptionMessage(paramIndex, jdbcDefType, value, column, valueType);
            throw new DfJDBCException(msg, e);
        } catch (RuntimeException e) {
            String msg = buildBindingExceptionMessage(paramIndex, jdbcDefType, value, column, valueType);
            throw new IllegalStateException(msg, e);
        }
    }

    protected String buildBindingExceptionMessage(int paramIndex, int jdbcDefType, Object value,
            DfProcedureColumnMeta column, ValueType valueType) {
        String msg = "Failed to bind parameter(" + paramIndex + "|" + jdbcDefType + "):";
        msg = msg + " " + column.getColumnNameDisp() + " - " + column.getColumnDefinitionLineDisp();
        msg = msg + " :: " + value + ", " + valueType.getClass().getName();
        return msg;
    }

    protected ValueType getForcedValueType(DfProcedureColumnMeta column) {
        final ValueType valueType;
        if (column.isOracleNCharOrNVarchar()) { // just in case
            valueType = _stringType;
        } else if (column.isConceptTypeStringClob()) {
            valueType = _stringClobType;
        } else if (column.isConceptTypeBytesOid()) {
            valueType = _bytesOidType;
        } else if (column.isConceptTypeFixedLengthString()) {
            valueType = _fixedLengthStringType;
        } else if (column.isConceptTypeObjectBindingBigDecimal()) {
            valueType = _objectBindingBigDecimalType;
        } else if (column.isPostgreSQLUuid()) { // needs to switch
            valueType = _uuidAsDirectType;
        } else if (column.isSQLServerUniqueIdentifier()) { // needs to switch
            valueType = _uuidAsStringType;
        } else {
            valueType = null;
        }
        return valueType;
    }

    protected void closeResult(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ignored) {
            }
        }
    }

    // ===================================================================================
    //                                                                    Column Meta Info
    //                                                                    ================
    protected Map<String, DfColumnMeta> extractColumnMetaInfoMap(ResultSet rs, String sql) throws SQLException {
        return _extractor.extractColumnMetaInfoMap(rs, sql, null);
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

    protected DfTypeMappingProperties getTypeMappingProperties() {
        return getProperties().getTypeMappingProperties();
    }

    protected boolean isOracle() {
        return getBasicProperties().isDatabaseOracle();
    }

    protected boolean isPostgreSQL() {
        return getBasicProperties().isDatabasePostgreSQL();
    }

    protected boolean isDB2() {
        return getBasicProperties().isDatabaseDB2();
    }

    protected boolean isSQLServer() {
        return getBasicProperties().isDatabaseSQLServer();
    }

    protected boolean isSQLite() {
        return getBasicProperties().isDatabaseSQLite();
    }

    protected boolean isMsAccess() {
        return getBasicProperties().isDatabaseMSAccess();
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String ln() {
        return "\n";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public Map<String, String> getContinuedFailureMessageMap() {
        return _continuedFailureMessageMap;
    }
}