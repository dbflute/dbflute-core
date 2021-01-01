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
package org.dbflute.helper.jdbc.facade;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.sql.DataSource;

import org.dbflute.exception.SQLFailureException;
import org.dbflute.helper.StringKeyMap;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.jdbc.ValueType;
import org.dbflute.util.DfCollectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Super simple facade for JDBC.
 * @author jflute
 * @author p1us2er0
 */
public class DfJdbcFacade {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger _log = LoggerFactory.getLogger(DfJdbcFacade.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DataSource _dataSource;
    protected final Connection _conn;
    protected boolean _useTransaction;
    protected boolean _debugRetryable;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfJdbcFacade(DataSource dataSource) {
        _dataSource = dataSource;
        _conn = null;
    }

    public DfJdbcFacade(Connection conn) {
        _dataSource = null;
        _conn = conn;
    }

    // ===================================================================================
    //                                                                              Select
    //                                                                              ======
    // -----------------------------------------------------
    //                                           String List
    //                                           -----------
    /**
     * Select the list for records as string value simply.
     * @param sql The SQL string. (NotNull)
     * @param columnList The list of selected columns. (NotNull)
     * @return The list for result. (NotNull)
     */
    public List<Map<String, String>> selectStringList(String sql, List<String> columnList) {
        final Map<String, ValueType> columnValueTypeMap = new LinkedHashMap<String, ValueType>();
        for (String column : columnList) {
            columnValueTypeMap.put(column, null);
        }
        return selectStringList(DfCollectionUtil.newArrayList(sql), columnValueTypeMap, null, -1);
    }

    /**
     * Select the list for records as string value using value types.
     * @param trySqlList The try SQL strings. (NotNull)
     * @param columnValueTypeMap The map of selected columns to value types. (NotNull, ValueTypeNullAllowed)
     * @param converter The converter to convert to string value. (NullAllowed: means no conversion)
     * @param limit The limit size for fetching. (MinusAllowed: means no limit)
     * @return The list for result. (NotNull)
     */
    public List<Map<String, String>> selectStringList(List<String> trySqlList, Map<String, ValueType> columnValueTypeMap,
            DfJFadStringConverter converter, int limit) {
        if (trySqlList == null || trySqlList.isEmpty()) {
            throw new IllegalArgumentException("The argument 'trySqlList' should not be null and empty: " + trySqlList);
        }
        // [ATTENTION]: no use bind variables because of framework internal use
        final List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        String currentSql = null;
        try {
            conn = getConnection();
            beginTransactionIfNeeds(conn);
            st = conn.createStatement();

            final DfCurrentSqlResult result = retryableExecuteQuery(trySqlList, st);
            currentSql = result.getCurrentSql(); // not null, keep for later SQLException
            rs = result.getResultSet(); // not null

            int count = 0;
            final DfJFadResultSetWrapper wrapper = new DfJFadResultSetWrapper(rs, columnValueTypeMap, converter);
            while (wrapper.next()) {
                if (isOverLimit(limit, count)) {
                    break;
                }
                final Map<String, String> recordMap = StringKeyMap.createAsFlexibleOrdered();
                final Set<Entry<String, ValueType>> entrySet = columnValueTypeMap.entrySet();
                for (Entry<String, ValueType> entry : entrySet) {
                    final String columnName = entry.getKey();
                    final String value = wrapper.getString(columnName);
                    recordMap.put(columnName, value);
                }
                resultList.add(recordMap);
                ++count;
            }
            commitTrasactionIfNeeds(conn);
        } catch (SQLException e) {
            handleSQLException(currentSql, e);
            return null; // unreachable
        } finally {
            rollbackTransactionIfNeeds(conn);
            closeResultSet(rs);
            closeStatement(st);
            closeConnection(conn);
        }
        return resultList;
    }

    // -----------------------------------------------------
    //                                                 Count
    //                                                 -----
    public int selectCountAll(String tableSqlName) {
        final List<String> columnList = DfCollectionUtil.newArrayList("cnt");
        final String sql = "select count(*) as cnt from " + tableSqlName;
        final String cntStr = selectStringList(sql, columnList).get(0).get("cnt").trim();
        return Integer.valueOf(cntStr);
    }

    // -----------------------------------------------------
    //                                                Cursor
    //                                                ------
    public DfJFadCursorCallback selectCursor(List<String> trySqlList, Map<String, ValueType> columnValueTypeMap,
            DfJFadStringConverter stringConverter) {
        if (trySqlList == null || trySqlList.isEmpty()) {
            throw new IllegalArgumentException("The argument 'trySqlList' should not be null and empty: " + trySqlList);
        }
        return new DfJFadCursorCallback() {
            public void select(DfJFadCursorHandler handler) {
                Connection conn = null;
                Statement st = null;
                ResultSet rs = null;
                String currentSql = null;
                try {
                    conn = _dataSource.getConnection();
                    st = conn.createStatement();

                    final DfCurrentSqlResult result = retryableExecuteQuery(trySqlList, st);
                    currentSql = result.getCurrentSql(); // not null, keep for later SQLException
                    rs = result.getResultSet(); // not null

                    final DfJFadResultSetWrapper wrapper = new DfJFadResultSetWrapper(rs, columnValueTypeMap, stringConverter);
                    handler.handle(wrapper);
                } catch (SQLException e) {
                    handleSQLException(currentSql, e);
                } finally {
                    closeResultSet(rs);
                    closeStatement(st);
                    closeConnection(conn);
                }
            }
        };
    }

    // -----------------------------------------------------
    //                                            Retry-able
    //                                            ----------
    protected DfCurrentSqlResult retryableExecuteQuery(List<String> trySqlList, Statement st) throws SQLException {
        ResultSet rs = null;
        String currentSql = null;
        SQLException latestEx = null;
        for (String trySql : trySqlList) {
            currentSql = trySql;
            try {
                rs = st.executeQuery(trySql);
                if (latestEx != null) { // retry now
                    if (_debugRetryable) {
                        _log.info("retry SQL success: {}", trySql);
                    }
                }
                latestEx = null;
                break;
            } catch (SQLException continued) {
                latestEx = continued;
                if (_debugRetryable) {
                    final SQLFailureException failureEx = createSQLFailureException(currentSql, continued);
                    _log.info("try SQL failure: {}", failureEx.getMessage());
                }
            }
        }
        if (rs == null) {
            if (latestEx != null) { // basically here
                throw latestEx;
            } else { // basically no way (executeQuery() does not return null), just in case
                throw new IllegalStateException("Cannot make result set by the SQL: currentSql=" + currentSql);
            }
        }
        return new DfCurrentSqlResult(currentSql, rs);
    }

    protected static class DfCurrentSqlResult {

        protected final String _currentSql;
        protected final ResultSet _resultSet;

        public DfCurrentSqlResult(String currentSql, ResultSet resultSet) {
            _currentSql = currentSql;
            _resultSet = resultSet;
        }

        public String getCurrentSql() {
            return _currentSql;
        }

        public ResultSet getResultSet() {
            return _resultSet;
        }
    }

    // -----------------------------------------------------
    //                                           Transaction
    //                                           -----------
    public DfJdbcFacade useTransaction() {
        _useTransaction = true;
        return this;
    }

    public DfJdbcFacade debugRetryable() {
        _debugRetryable = true;
        return this;
    }

    protected void beginTransactionIfNeeds(Connection conn) throws SQLException {
        if (_useTransaction) {
            conn.setAutoCommit(false);
        }
    }

    protected void commitTrasactionIfNeeds(Connection conn) throws SQLException {
        if (_useTransaction) {
            conn.commit();
        }
    }

    protected void rollbackTransactionIfNeeds(Connection conn) {
        if (_useTransaction) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {}
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ignored) {}
        }
    }

    // -----------------------------------------------------
    //                                                 Limit
    //                                                 -----
    protected boolean isOverLimit(int limit, int count) {
        return limit >= 0 && limit <= count;
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    public boolean execute(String sql) {
        // [ATTENTION]: no use bind variables
        Connection conn = null;
        Statement st = null;
        try {
            conn = getConnection();
            st = conn.createStatement();
            return st.execute(sql);
        } catch (SQLException e) {
            handleSQLException(sql, e);
            return false; // unreachable
        } finally {
            closeStatement(st);
            closeConnection(conn);
        }
    }

    // ===================================================================================
    //                                                                          Connection
    //                                                                          ==========
    protected Connection getConnection() throws SQLException {
        if (_dataSource != null) {
            return _dataSource.getConnection();
        } else {
            return _conn;
        }
    }

    // ===================================================================================
    //                                                                               Close
    //                                                                               =====
    protected void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ignored) {
                _log.info("ResultSet.close() threw the exception!", ignored);
            }
        }
    }

    protected void closeStatement(Statement st) {
        if (st != null) {
            try {
                st.close();
            } catch (SQLException ignored) {
                _log.info("Statement.close() threw the exception!", ignored);
            }
        }
    }

    protected void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ignored) {
                _log.info("Connection.close() threw the exception!", ignored);
            }
        }
    }

    // ===================================================================================
    //                                                                  Exception Handling
    //                                                                  ==================
    protected SQLFailureException createSQLFailureException(String sql, SQLException e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to execute the SQL");
        br.addItem("Executed SQL");
        br.addElement(sql); // may be null when e.g. connection failure
        br.addItem("SQLException");
        br.addElement(e.getClass());
        br.addElement(e.getMessage());
        final String msg = br.buildExceptionMessage();
        return new SQLFailureException(msg, e);
    }

    protected void handleSQLException(String sql, SQLException e) {
        throw createSQLFailureException(sql, e);
    }
}
