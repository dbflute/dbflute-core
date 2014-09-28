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
package org.seasar.dbflute.helper.jdbc.facade;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.dbflute.exception.SQLFailureException;
import org.seasar.dbflute.helper.StringKeyMap;
import org.seasar.dbflute.jdbc.ValueType;
import org.seasar.dbflute.util.DfCollectionUtil;

/**
 * Super simple facade for JDBC.
 * @author jflute
 */
public class DfJdbcFacade {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log _log = LogFactory.getLog(DfJdbcFacade.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DataSource _dataSource;
    protected final Connection _conn;
    protected boolean _useTransaction;

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
        return selectStringList(sql, columnList, -1);
    }

    /**
     * Select the list for records as string value simply.
     * @param sql The SQL string. (NotNull)
     * @param columnList The list of selected columns. (NotNull)
     * @param limit The limit size for fetching. (MinusAllowed: means no limit)
     * @return The list for result. (NotNull)
     */
    public List<Map<String, String>> selectStringList(String sql, List<String> columnList, int limit) {
        final Map<String, ValueType> columnValueTypeMap = new LinkedHashMap<String, ValueType>();
        for (String column : columnList) {
            columnValueTypeMap.put(column, null);
        }
        return selectStringList(sql, columnValueTypeMap, null, limit);
    }

    /**
     * Select the list for records as string value using value types.
     * @param sql The SQL string. (NotNull)
     * @param columnValueTypeMap The map of selected columns to value types. (NotNull, ValueTypeNullAllowed)
     * @param converter The converter to convert to string value. (NullAllowed: means no conversion)
     * @return The list for result. (NotNull)
     */
    public List<Map<String, String>> selectStringList(String sql, Map<String, ValueType> columnValueTypeMap,
            DfJFadStringConverter converter) {
        return selectStringList(sql, columnValueTypeMap, converter, -1);
    }

    /**
     * Select the list for records as string value using value types.
     * @param sql The SQL string. (NotNull)
     * @param columnValueTypeMap The map of selected columns to value types. (NotNull, ValueTypeNullAllowed)
     * @param converter The converter to convert to string value. (NullAllowed: means no conversion)
     * @param limit The limit size for fetching. (MinusAllowed: means no limit)
     * @return The list for result. (NotNull)
     */
    public List<Map<String, String>> selectStringList(String sql, Map<String, ValueType> columnValueTypeMap,
            DfJFadStringConverter converter, int limit) {
        // [ATTENTION]: no use bind variables
        final List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            beginTransactionIfNeeds(conn);
            st = conn.createStatement();
            rs = st.executeQuery(sql);
            final DfJFadResultSetWrapper wrapper = new DfJFadResultSetWrapper(rs, columnValueTypeMap, converter);
            int count = 0;
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
            handleSQLException(sql, e);
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
    public DfJFadCursorCallback selectCursor(final String sql, final Map<String, ValueType> columnValueTypeMap,
            final DfJFadStringConverter stringConverter) {
        return new DfJFadCursorCallback() {
            public void select(DfJFadCursorHandler handler) {
                Connection conn = null;
                Statement st = null;
                ResultSet rs = null;
                try {
                    conn = _dataSource.getConnection();
                    st = conn.createStatement();
                    rs = st.executeQuery(sql);
                    handler.handle(new DfJFadResultSetWrapper(rs, columnValueTypeMap, stringConverter));
                } catch (SQLException e) {
                    handleSQLException(sql, e);
                } finally {
                    closeResultSet(rs);
                    closeStatement(st);
                    closeConnection(conn);
                }
            }
        };
    }

    // -----------------------------------------------------
    //                                           Transaction
    //                                           -----------
    public DfJdbcFacade useTransaction() {
        _useTransaction = true;
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
            } catch (SQLException ignored) {
            }
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
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
    protected void handleSQLException(String sql, SQLException e) {
        String msg = "Failed to execute the SQL:" + ln();
        msg = msg + "/- - - - - - - - - - - - - - - - - - - - - - - - - - - - " + ln();
        msg = msg + "[SQL]" + ln() + sql + ln();
        msg = msg + ln();
        msg = msg + "[Exception]" + ln();
        msg = msg + e.getClass() + ln();
        msg = msg + e.getMessage() + ln();
        msg = msg + "- - - - - - - - - -/";
        throw new SQLFailureException(msg, e);
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String ln() {
        return "\n";
    }
}
