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
import org.seasar.dbflute.helper.StringSet;
import org.seasar.dbflute.logic.jdbc.metadata.basic.DfAutoIncrementExtractor;
import org.seasar.dbflute.logic.jdbc.metadata.basic.DfColumnExtractor;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfColumnMeta;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfPrimaryKeyMeta;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfTableMeta;

/**
 * @author jflute
 * @since 0.9.5.2 (2009/07/09 Thursday)
 */
public class DfSequenceHandlerPostgreSQL extends DfSequenceHandlerJdbc {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log _log = LogFactory.getLog(DfSequenceHandlerPostgreSQL.class);

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfSequenceHandlerPostgreSQL(DataSource dataSource, List<UnifiedSchema> unifiedSchemaList) {
        super(dataSource, unifiedSchemaList);
    }

    // ===================================================================================
    //                                                            Serial Sequence Handling
    //                                                            ========================
    @Override
    public void incrementSequenceToDataMax(Map<String, String> tableSequenceMap) {
        super.incrementSequenceToDataMax(tableSequenceMap);
        handleSerialTypeSequence(tableSequenceMap);
    }

    protected void handleSerialTypeSequence(Map<String, String> tableSequenceMap) {
        final StringSet doneSequenceSet = StringSet.createAsFlexibleOrdered();
        doneSequenceSet.addAll(tableSequenceMap.values());
        DfTableMeta tableInfo = null;
        DfPrimaryKeyMeta pkInfo = null;
        String sequenceName = null;
        String tableSqlName = null;
        Integer actualValue = null;
        String sequenceSqlName = null;
        Connection conn = null;
        Statement st = null;
        try {
            conn = _dataSource.getConnection();
            st = conn.createStatement();
            final DatabaseMetaData metaData = conn.getMetaData();
            final DfColumnExtractor columnHandler = new DfColumnExtractor();
            final DfAutoIncrementExtractor autoIncrementHandler = new DfAutoIncrementExtractor();
            _log.info("...Incrementing serial type sequence");
            final Set<Entry<String, DfTableMeta>> entrySet = _tableMap.entrySet();
            for (Entry<String, DfTableMeta> entry : entrySet) {
                // clear elements that are also used exception message
                tableInfo = null;
                pkInfo = null;
                sequenceName = null;
                tableSqlName = null;
                actualValue = null;
                sequenceSqlName = null;

                tableInfo = entry.getValue();
                pkInfo = _uniqueKeyHandler.getPrimaryKey(metaData, tableInfo);
                final List<String> pkList = pkInfo.getPrimaryKeyList();
                if (pkList.size() != 1) {
                    continue;
                }
                final String primaryKeyColumnName = pkList.get(0);
                if (!autoIncrementHandler.isAutoIncrementColumn(conn, tableInfo, primaryKeyColumnName)) {
                    continue;
                }
                final Map<String, DfColumnMeta> columnMap = columnHandler.getColumnMap(metaData, tableInfo);
                final DfColumnMeta columnInfo = columnMap.get(primaryKeyColumnName);
                if (columnInfo == null) {
                    continue;
                }
                final String defaultValue = columnInfo.getDefaultValue();
                if (defaultValue == null) {
                    continue;
                }
                final String prefix = "nextval('";
                if (!defaultValue.startsWith(prefix)) {
                    continue;
                }
                final String excludedPrefixString = defaultValue.substring(prefix.length());
                final int endIndex = excludedPrefixString.indexOf("'");
                if (endIndex < 0) {
                    continue;
                }
                sequenceName = excludedPrefixString.substring(0, endIndex);
                if (doneSequenceSet.contains(sequenceName)) {
                    continue; // already done
                }
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
                // because sequence names of other schemas have already been qualified
                //sequenceSqlName = tableInfo.getUnifiedSchema().buildSqlName(sequenceName);
                sequenceSqlName = sequenceName;
                callSequenceLoop(st, sequenceSqlName, actualValue);
            }
        } catch (SQLException e) {
            throwSerialTypeSequenceHandlingFailureException(tableInfo, pkInfo, sequenceName, tableSqlName, actualValue,
                    sequenceSqlName, e);
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
    }

    protected void throwSerialTypeSequenceHandlingFailureException(DfTableMeta tableInfo, DfPrimaryKeyMeta pkInfo,
            String sequenceName, String tableSqlName, Integer actualValue, String sequenceSqlName, SQLException e) {
        String msg = "Look! Read the message below." + ln();
        msg = msg + "/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *" + ln();
        msg = msg + "Failed to handle serial type sequence!" + ln();
        msg = msg + ln();
        msg = msg + "[Table Info]" + ln() + tableInfo + ln();
        msg = msg + ln();
        msg = msg + "[Primary Key Info]" + ln() + pkInfo + ln();
        msg = msg + ln();
        msg = msg + "[Sequence Name]" + ln() + sequenceName + ln();
        msg = msg + ln();
        msg = msg + "[Table SQL Name]" + ln() + tableSqlName + ln();
        msg = msg + ln();
        msg = msg + "[Table Data Max]" + ln() + actualValue + ln();
        msg = msg + ln();
        msg = msg + "[Sequence SQL Name]" + ln() + sequenceSqlName + ln();
        msg = msg + "* * * * * * * * * */";
        throw new IllegalStateException(msg);
    }

    // ===================================================================================
    //                                                                          Next Value
    //                                                                          ==========
    @Override
    protected Integer selectNextVal(Statement st, String sequenceName) throws SQLException {
        ResultSet rs = null;
        try {
            rs = st.executeQuery("select nextval ('" + sequenceName + "')");
            rs.next();
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
}