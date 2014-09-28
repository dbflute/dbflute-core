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
package org.seasar.dbflute.helper.dataset.writers;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.torque.engine.database.model.UnifiedSchema;
import org.seasar.dbflute.exception.SQLFailureException;
import org.seasar.dbflute.helper.dataset.DfDataRow;
import org.seasar.dbflute.helper.dataset.DfDataTable;
import org.seasar.dbflute.helper.dataset.states.DfDtsRowState;

/**
 * @author modified by jflute (originated in Seasar2)
 * @since 0.8.3 (2008/10/28 Tuesday)
 */
public class DfDtsSqlTableWriter implements DfDtsTableWriter {

    protected DataSource _dataSource;
    protected UnifiedSchema _unifiedSchema;

    public DfDtsSqlTableWriter(DataSource dataSource, UnifiedSchema unifiedSchema) {
        _dataSource = dataSource;
        _unifiedSchema = unifiedSchema;
    }

    public DataSource getDataSource() {
        return _dataSource;
    }

    public void write(DfDataTable table) {
        try {
            if (!table.hasMetaData()) {
                setupMetaData(table);
            }
        } catch (SQLException e) {
            String msg = "Failed to set up meta data: " + table;
            throw new SQLFailureException(msg, e);
        }
        doWrite(table);
    }

    protected void doWrite(DfDataTable table) {
        for (int i = 0; i < table.getRowSize(); ++i) {
            DfDataRow row = table.getRow(i);
            DfDtsRowState state = row.getState();
            state.update(_dataSource, row);
        }
    }

    private void setupMetaData(DfDataTable table) throws SQLException {
        Connection con = getConnection(_dataSource);
        try {
            table.setupMetaData(getMetaData(con), _unifiedSchema);
        } finally {
            close(con);
        }
    }

    private static Connection getConnection(DataSource dataSource) {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private static DatabaseMetaData getMetaData(Connection conn) {
        try {
            return conn.getMetaData();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void close(Connection conn) {
        if (conn == null)
            return;
        try {
            conn.close();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}
