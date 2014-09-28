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
package org.seasar.dbflute.helper.jdbc.generatedsql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author jflute
 */
public class DfGeneratedSqlExecutorImpl implements DfGeneratedSqlExecutor {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance. */
    private static Log _log = LogFactory.getLog(DfGeneratedSqlExecutorImpl.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected DataSource _dataSource;

    // ===================================================================================
    //                                                                                Main
    //                                                                                ====
    public void execute(String sql, String aliasName) {
        final DfGeneratedSqlExecuteOption option = new DfGeneratedSqlExecuteOption();
        option.setErrorContinue(false);
        execute(sql, aliasName, option);
    }

    public void execute(String sql, String aliasName, DfGeneratedSqlExecuteOption option) {
        final String lineSeparator = System.getProperty("line.separator");
        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        String currentGeneratedSql = null;
        try {
            connection = _dataSource.getConnection();
            statement = connection.createStatement();
            _log.info("...Generating SQL: " + lineSeparator + sql);
            rs = statement.executeQuery(sql);
            final List<String> generatedSqlList = new ArrayList<String>();
            while (rs.next()) {
                final String generatedSql = rs.getString(aliasName);
                generatedSqlList.add(generatedSql);
            }
            for (String generatedSql : generatedSqlList) {
                currentGeneratedSql = generatedSql;
                try {
                    statement.execute(generatedSql);
                    _log.info(generatedSql);
                } catch (RuntimeException e) {
                    if (option.isErrorContinue()) {
                        continue;
                    }
                    _log.warn(generatedSql);
                    throw e;
                } catch (SQLException e) {
                    if (option.isErrorContinue()) {
                        continue;
                    }
                    _log.warn(generatedSql);
                    throw e;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("DfGeneratedSqlExecutorImpl.execute() threw the exception: baseSql=" + sql
                    + " generatedSql=" + currentGeneratedSql, e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ignored) {
                _log.warn("ResultSet#close() threw the exception!", ignored);
            }
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException ignored) {
                _log.warn("Statement#close() threw the exception!", ignored);
            }
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ignored) {
                _log.warn("Connection#close() threw the exception!", ignored);
            }
        }
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setDataSource(DataSource dataSource) {
        _dataSource = dataSource;
    }
}
