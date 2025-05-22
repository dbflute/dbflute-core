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
package org.dbflute.logic.replaceschema.loaddata.delimiter.secretary;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 1.2.5 extracted from DfDelimiterDataWriterImpl (2021/01/20 Wednesday at roppongi japanese)
 */
public class DfDelimiterDataJdbcHandler {

    private static final Logger _log = LoggerFactory.getLogger(DfDelimiterDataJdbcHandler.class);

    // ===================================================================================
    //                                                                   Batch Transaction
    //                                                                   =================
    public void beginTransaction(Connection conn) throws SQLException {
        conn.setAutoCommit(false);
    }

    public PreparedStatement prepareStatement(Connection conn, String executedSql) throws SQLException {
        return conn.prepareStatement(executedSql);
    }

    public void commitTransaction(Connection conn) throws SQLException {
        conn.commit();
    }

    public void rollbackTransaction(Connection conn) throws SQLException {
        if (conn != null) { // possible when rollback 
            conn.rollback();
        }
    }

    public void commitJustInCase(Connection conn) {
        final Boolean autoCommit = getAutoCommit(conn);
        if (autoCommit != null && !autoCommit) { // basically no way, just in case
            try {
                commitTransaction(conn);
            } catch (SQLException continued) {
                _log.warn("Failed to commit the transaction.", continued);
            }
        }
    }

    public Boolean getAutoCommit(Connection conn) {
        Boolean autoCommit = null;
        try {
            autoCommit = conn != null ? conn.getAutoCommit() : null;
        } catch (SQLException continued) {
            // because it is possible that the connection would have already closed
            _log.warn("Connection#getAutoCommit() said: " + continued.getMessage());
        }
        return autoCommit;
    }

    // ===================================================================================
    //                                                                              Closer
    //                                                                              ======
    public void closeStream(FileInputStream fis, InputStreamReader ir, BufferedReader br) {
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
    }

    public void close(PreparedStatement ps) {
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException ignored) {
                _log.info("Statement.close() threw the exception!", ignored);
            }
        }
    }

    public void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ignored) {
                _log.info("Connection.close() threw the exception!", ignored);
            }
        }
    }
}
