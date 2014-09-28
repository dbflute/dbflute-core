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
package org.seasar.dbflute.logic.replaceschema.takefinally.sequence;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.engine.database.model.UnifiedSchema;
import org.seasar.dbflute.exception.DfPropertySettingTableNotFoundException;
import org.seasar.dbflute.helper.StringKeyMap;
import org.seasar.dbflute.logic.jdbc.metadata.basic.DfTableExtractor;
import org.seasar.dbflute.logic.jdbc.metadata.basic.DfUniqueKeyExtractor;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfPrimaryKeyMeta;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfTableMeta;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.9.5.2 (2009/07/09 Thursday)
 */
public abstract class DfSequenceHandlerJdbc implements DfSequenceHandler {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log _log = LogFactory.getLog(DfSequenceHandlerJdbc.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected DataSource _dataSource;
    protected List<UnifiedSchema> _unifiedSchemaList;
    protected final DfUniqueKeyExtractor _uniqueKeyHandler = new DfUniqueKeyExtractor();
    protected Map<String, DfTableMeta> _tableMap;

    protected void initializeTableInfo(Connection conn) throws SQLException {
        if (_tableMap != null) {
            return;
        }
        _tableMap = StringKeyMap.createAsFlexible();
        final DfTableExtractor tableHandler = new DfTableExtractor();
        final DatabaseMetaData metaData = conn.getMetaData();
        final List<UnifiedSchema> unifiedSchemaList = _unifiedSchemaList;
        for (UnifiedSchema unifiedSchema : unifiedSchemaList) {
            // same-name tables between different schemas are unsupported
            // so put all directly here
            _tableMap.putAll(tableHandler.getTableMap(metaData, unifiedSchema));
        }
    }

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfSequenceHandlerJdbc(DataSource dataSource, List<UnifiedSchema> unifiedSchemaList) {
        _dataSource = dataSource;
        _unifiedSchemaList = unifiedSchemaList;
    }

    // ===================================================================================
    //                                                                  Increment Sequence
    //                                                                  ==================
    public void incrementSequenceToDataMax(Map<String, String> tableSequenceMap) {
        final Map<String, List<String>> skippedMap = DfCollectionUtil.newLinkedHashMap();
        _log.info("...Incrementing sequences to max value of table data");
        String tableName = null;
        String sequenceName = null;
        DfTableMeta tableInfo = null;
        DfPrimaryKeyMeta pkInfo = null;
        String tableSqlName = null;
        Integer actualValue = null;
        Connection conn = null;
        Statement st = null;
        try {
            conn = _dataSource.getConnection();
            initializeTableInfo(conn);
            st = conn.createStatement();
            final Set<Entry<String, String>> entrySet = tableSequenceMap.entrySet();
            for (Entry<String, String> entry : entrySet) {
                // clear elements that are also used exception message
                tableName = null;
                sequenceName = null;
                tableInfo = null;
                pkInfo = null;
                tableSqlName = null;
                actualValue = null;

                tableName = entry.getKey();
                sequenceName = entry.getValue();
                assertValidSequence(sequenceName, tableName);
                tableInfo = findTableInfo(conn, tableName);
                pkInfo = findPrimaryKeyInfo(conn, tableInfo);
                final List<String> pkList = pkInfo.getPrimaryKeyList();
                if (pkList.size() != 1) {
                    skippedMap.put(tableName, pkList);
                    continue;
                }
                final String primaryKeyColumnName = pkList.get(0);
                tableSqlName = tableInfo.getTableSqlName();
                final Integer count = selectCount(st, tableSqlName);
                if (count == null || count == 0) {
                    // It is not necessary to increment because the table has no data.
                    continue;
                }
                actualValue = selectDataMax(st, tableInfo, primaryKeyColumnName);
                if (actualValue == null) {
                    // It is not necessary to increment because the table has no data.
                    continue;
                }
                callSequenceLoop(st, sequenceName, actualValue);
            }
        } catch (SQLException e) {
            throwIncrementSequenceToDataMaxFailureException(tableName, sequenceName, tableInfo, pkInfo, tableSqlName,
                    actualValue, e);
        } finally {
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException ignored) {
                    _log.info("Statement.close() threw the exception!", ignored);
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignored) {
                    _log.info("Connection.close() threw the exception!", ignored);
                }
            }
        }
        if (!skippedMap.isEmpty()) {
            _log.info("*Unsupported incrementing sequences(multiple-PK):");
            final Set<Entry<String, List<String>>> skippedEntrySet = skippedMap.entrySet();
            for (Entry<String, List<String>> skippedEntry : skippedEntrySet) {
                final String skippedTableName = skippedEntry.getKey();
                final List<String> pkList = skippedEntry.getValue();
                _log.info("    " + skippedTableName + ": pk=" + pkList);
            }
        }
    }

    protected void assertValidSequence(String sequenceName, String tableName) {
        if (Srl.is_Null_or_TrimmedEmpty(sequenceName)) {
            String msg = "Not found the sequence name of the table:";
            msg = msg + " tableName=" + tableName;
            throw new IllegalStateException(msg); // basically unreachable
        }
    }

    protected DfTableMeta findTableInfo(Connection conn, String tableName) throws SQLException {
        final DfTableMeta table = _tableMap.get(tableName);
        if (table == null) {
            String msg = "Failed to find the table in generated target tables:";
            msg = msg + " table=" + tableName + " target=" + _tableMap.keySet();
            throw new DfPropertySettingTableNotFoundException(msg);
        }
        return table;
    }

    protected DfPrimaryKeyMeta findPrimaryKeyInfo(Connection conn, DfTableMeta tableInfo) throws SQLException {
        final DatabaseMetaData metaData = conn.getMetaData();
        return _uniqueKeyHandler.getPrimaryKey(metaData, tableInfo);
    }

    protected void callSequenceLoop(Statement st, String sequenceName, Integer actualValue) throws SQLException {
        Integer sequenceValue = selectNextVal(st, sequenceName); // first next value
        final Integer startPoint = sequenceValue; // save start point
        boolean decrementChecked = false;
        while (actualValue > sequenceValue) {
            sequenceValue = selectNextVal(st, sequenceName); // second or more next value
            if (decrementChecked) {
                continue;
            }
            // first loop only here
            if (startPoint >= sequenceValue) { // if decrement or no change
                String msg = "    " + sequenceName + ": " + startPoint + " to " + sequenceValue;
                msg = msg + " (unsupported for decrement)";
                _log.info(msg);
                return;
            }
            decrementChecked = true;
        }
        _log.info("    " + sequenceName + ": " + startPoint + " to " + sequenceValue);
    }

    protected Integer selectCount(Statement statement, String tableName) throws SQLException {
        ResultSet rs = null;
        try {
            rs = statement.executeQuery("select count(*) from " + tableName);
            if (!rs.next()) {
                return null;
            }
            return rs.getInt(1);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignored) {
                    _log.info("ResultSet.close() threw the exception!", ignored);
                }
            }
        }
    }

    protected Integer selectDataMax(Statement statement, DfTableMeta tableInfo, String primaryKeyColumnName)
            throws SQLException {
        final String tableSqlName = tableInfo.getTableSqlName();
        final String sql = "select max(" + primaryKeyColumnName + ") as MAX_VALUE from " + tableSqlName;
        ResultSet rs = null;
        try {
            rs = statement.executeQuery(sql);
            if (!rs.next()) {
                return null;
            }
            String value = rs.getString(1);
            if (value == null) {
                return null;
            }
            Integer actualValue;
            try {
                actualValue = Integer.valueOf(value);
            } catch (NumberFormatException e) {
                String msg = "The type of primary key related to sequece should be Number:";
                msg = msg + " table=" + tableSqlName + " primaryKey=" + primaryKeyColumnName;
                msg = msg + " value=" + value;
                throw new IllegalStateException(msg);
            }
            return actualValue;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignored) {
                    _log.info("ResultSet.close() threw the exception!", ignored);
                }
            }
        }
    }

    protected abstract Integer selectNextVal(Statement statement, String sequenceName) throws SQLException;

    protected void throwIncrementSequenceToDataMaxFailureException(String tableName, String sequenceName,
            DfTableMeta tableInfo, DfPrimaryKeyMeta pkInfo, String tableSqlName, Integer actualValue, SQLException e) {
        String msg = "Look! Read the message below." + ln();
        msg = msg + "/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *" + ln();
        msg = msg + "Failed to handle serial type sequence!" + ln();
        msg = msg + ln();
        msg = msg + "[Table Name]" + ln() + tableName + ln();
        msg = msg + ln();
        msg = msg + "[Sequence Name]" + ln() + sequenceName + ln();
        msg = msg + ln();
        msg = msg + "[Table Info]" + ln() + tableInfo + ln();
        msg = msg + ln();
        msg = msg + "[Primary Key Info]" + ln() + pkInfo + ln();
        msg = msg + ln();
        msg = msg + "[Table SQL Name]" + ln() + tableSqlName + ln();
        msg = msg + ln();
        msg = msg + "[Table Data Max]" + ln() + actualValue + ln();
        msg = msg + "* * * * * * * * * */";
        throw new IllegalStateException(msg);
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String ln() {
        return "\n";
    }
}