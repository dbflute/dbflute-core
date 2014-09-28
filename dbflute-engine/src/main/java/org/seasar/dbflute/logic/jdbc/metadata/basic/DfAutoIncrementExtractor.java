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
package org.seasar.dbflute.logic.jdbc.metadata.basic;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.seasar.dbflute.exception.DfJDBCException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfColumnMeta;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfTableMeta;

/**
 * The handler of auto increment. 
 * @author jflute
 */
public class DfAutoIncrementExtractor extends DfAbstractMetaDataBasicExtractor {

    /**
     * Is the column auto-increment?
     * @param conn Connection.
     * @param tableInfo The meta information of table from which to retrieve PK information.
     * @param primaryKeyColumnInfo The meta information of primary-key column.
     * @return The determination, true or false.
     */
    public boolean isAutoIncrementColumn(Connection conn, DfTableMeta tableInfo, DfColumnMeta primaryKeyColumnInfo)
            throws SQLException {
        if (analyzeByDatabaseDependencyMeta(tableInfo, primaryKeyColumnInfo)) {
            return true;
        }
        final String primaryKeyColumnName = primaryKeyColumnInfo.getColumnName();
        return isAutoIncrementColumn(conn, tableInfo, primaryKeyColumnName);
    }

    protected boolean analyzeByDatabaseDependencyMeta(DfTableMeta tableInfo, DfColumnMeta primaryKeyColumnInfo) {
        if (isDatabaseSybase()) {
            return primaryKeyColumnInfo.isSybaseAutoIncrement();
        } else {
            return false;
        }
    }

    /**
     * Is the column auto-increment?
     * @param conn Connection.
     * @param tableInfo The meta information of table from which to retrieve PK information.
     * @param primaryKeyColumnName The name of primary-key column.
     * @return The determination, true or false.
     */
    public boolean isAutoIncrementColumn(Connection conn, DfTableMeta tableInfo, String primaryKeyColumnName)
            throws SQLException {
        return analyzeByResultSetMeta(conn, tableInfo, primaryKeyColumnName);
    }

    protected boolean analyzeByResultSetMeta(Connection conn, DfTableMeta tableInfo, String primaryKeyColumnName)
            throws SQLException {
        final String tableSqlName = tableInfo.getTableSqlName();
        final String sql = buildMetaDataSql(primaryKeyColumnName, tableSqlName);
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
            String msg = "The primaryKeyColumnName is not found in the table: ";
            msg = msg + tableSqlName + "." + primaryKeyColumnName;
            throw new IllegalStateException(msg); // unreachable
        } catch (SQLException e) {
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
        } finally {
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException ignored) {
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    protected String buildMetaDataSql(String pkName, String tableName) {
        pkName = getProperties().getLittleAdjustmentProperties().quoteColumnNameIfNeedsDirectUse(pkName);
        return "select " + pkName + " from " + tableName + " where 0 = 1";
    }
}