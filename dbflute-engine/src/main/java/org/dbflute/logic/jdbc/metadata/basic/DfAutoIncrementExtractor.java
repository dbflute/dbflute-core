/*
 * Copyright 2014-2018 the original author or authors.
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
package org.dbflute.logic.jdbc.metadata.basic;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.dbflute.exception.DfJDBCException;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.logic.jdbc.metadata.info.DfColumnMeta;
import org.dbflute.logic.jdbc.metadata.info.DfTableMeta;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The handler of auto increment. 
 * @author jflute
 */
public class DfAutoIncrementExtractor extends DfAbstractMetaDataBasicExtractor {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger _log = LoggerFactory.getLogger(DfAutoIncrementExtractor.class);

    // ===================================================================================
    //                                                                Column Determination
    //                                                                ====================
    /**
     * Is the column auto-increment?
     * @param conn database connection for meta data. (NotNull)
     * @param tableInfo The meta information of table from which to retrieve PK information. (NotNull)
     * @param primaryKeyColumnInfo The meta information of primary-key column. (NotNull)
     * @return The determination, true or false.
     */
    public boolean isAutoIncrementColumn(Connection conn, DfTableMeta tableInfo, DfColumnMeta primaryKeyColumnInfo) throws SQLException {
        if (analyzeByDatabaseDependencyMeta(tableInfo, primaryKeyColumnInfo)) {
            return true;
        }
        final String primaryKeyColumnName = primaryKeyColumnInfo.getColumnName();
        return isAutoIncrementColumn(conn, tableInfo, primaryKeyColumnName);
    }

    /**
     * Is the column auto-increment?
     * @param conn database connection for meta data. (NotNull)
     * @param tableInfo The meta information of table from which to retrieve PK information. (NotNull)
     * @param primaryKeyColumnName The name of primary-key column. (NotNull)
     * @return The determination, true or false.
     */
    public boolean isAutoIncrementColumn(Connection conn, DfTableMeta tableInfo, String primaryKeyColumnName) throws SQLException {
        return analyzeByResultSetMeta(conn, tableInfo, primaryKeyColumnName);
    }

    // ===================================================================================
    //                                                       Analyze by DatabaseDependency
    //                                                       =============================
    protected boolean analyzeByDatabaseDependencyMeta(DfTableMeta tableInfo, DfColumnMeta primaryKeyColumnInfo) {
        if (isDatabaseSybase()) {
            return primaryKeyColumnInfo.isSybaseAutoIncrement();
        } else {
            return false;
        }
    }

    // ===================================================================================
    //                                                                Analyze by ResultSet
    //                                                                ====================
    protected boolean analyzeByResultSetMeta(Connection conn, DfTableMeta tableInfo, String primaryKeyColumnName) throws SQLException {
        final String tableSqlName = tableInfo.getTableSqlName();
        final String sql = buildMetaDataSql(primaryKeyColumnName, tableSqlName);
        return executeAutoIncrementQuery(conn, tableInfo, primaryKeyColumnName, tableSqlName, sql);
    }

    protected boolean executeAutoIncrementQuery(Connection conn, DfTableMeta tableInfo, String primaryKeyColumnName, String tableSqlName,
            String sql) throws DfJDBCException {
        try {
            return doExecuteAutoIncrementQuery(conn, tableInfo, primaryKeyColumnName, sql);
        } catch (SQLException e) {
            if (isDatabasePostgreSQL()) { // the table name needs quote e.g. upper case
                final String retrySql = buildMetaDataSql(primaryKeyColumnName, Srl.quoteDouble(tableSqlName));
                try {
                    return doExecuteAutoIncrementQuery(conn, tableInfo, primaryKeyColumnName, retrySql);
                } catch (SQLException continued) {
                    _log.info("Failed to retry auto-increment query: sql=" + retrySql + ", msg=" + continued.getMessage());
                }
            }
            throwAutoIncrementDeterminationFailureException(tableInfo, primaryKeyColumnName, sql, e);
            return false; // unreachable
        }
    }

    protected String buildMetaDataSql(String pkName, String tableName) {
        return "select " + quoteColumnNameIfNeedsDirectUse(pkName) + " from " + tableName + " where 0 = 1";
    }

    protected String quoteColumnNameIfNeedsDirectUse(String pkName) {
        return getProperties().getLittleAdjustmentProperties().quoteColumnNameIfNeedsDirectUse(pkName);
    }

    protected boolean doExecuteAutoIncrementQuery(Connection conn, DfTableMeta tableInfo, String primaryKeyColumnName, String sql)
            throws SQLException {
        Statement st = null;
        ResultSet rs = null;
        try {
            st = conn.createStatement();
            rs = st.executeQuery(sql);
            final ResultSetMetaData md = rs.getMetaData();
            for (int i = 1; i <= md.getColumnCount(); i++) {
                final String currentColumnName = md.getColumnName(i);
                if (primaryKeyColumnName.equals(currentColumnName)) {
                    return md.isAutoIncrement(i);
                }
            }
            throwPrimaryKeyColumnNotFoundException(primaryKeyColumnName, tableInfo);
            return false; // unreachable
        } finally {
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException ignored) {}
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    protected void throwPrimaryKeyColumnNotFoundException(String primaryKeyColumnName, DfTableMeta tableMeta) {
        String msg = "The primaryKeyColumnName is not found in the table: " + tableMeta.getTableDbName() + "." + primaryKeyColumnName;
        throw new IllegalStateException(msg); // unreachable
    }

    protected void throwAutoIncrementDeterminationFailureException(DfTableMeta tableInfo, String primaryKeyColumnName, String sql,
            SQLException e) throws DfJDBCException {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to execute the SQL for getting auto-increment");
        br.addItem("Advice");
        br.addElement("DBFlute executes the SQL to get auto-increment meta data.");
        br.addElement("The table might not exist on your schema. Or the schema");
        br.addElement("to be set at 'dfprop' might be mistake in the first place.");
        br.addElement("(and other points can be causes, for example, authentication)");
        br.addElement("So check your settings and environments.");
        br.addItem("Table");
        br.addElement(tableInfo.getTableFullQualifiedName());
        br.addItem("PrimaryKey");
        br.addElement(primaryKeyColumnName);
        br.addItem("SQL for getting");
        br.addElement(sql);
        final String msg = br.buildExceptionMessage();
        throw new DfJDBCException(msg, e);
    }
}