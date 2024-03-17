/*
 * Copyright 2014-2024 the original author or authors.
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
package org.dbflute.jdbc;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

/**
 * The wrapper of data source with handling.
 * @author jflute
 * @since 0.9.5 (2009/04/29 Wednesday)
 */
public class HandlingDataSourceWrapper implements DataSource {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    private final DataSource _dataSource;
    private final DataSourceHandler _dataSourceHandler;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public HandlingDataSourceWrapper(DataSource dataSource, DataSourceHandler dataSourceHandler) {
        if (dataSource == null) {
            String msg = "The argument 'dataSource' should not be null!";
            throw new IllegalArgumentException(msg);
        }
        if (dataSourceHandler == null) {
            String msg = "The argument 'dataSourceHandler' should not be null!";
            throw new IllegalArgumentException(msg);
        }
        _dataSource = dataSource;
        _dataSourceHandler = dataSourceHandler;
    }

    // ===================================================================================
    //                                                                Java6 Implementation
    //                                                                ====================
    public Connection getConnection() throws SQLException {
        return _dataSourceHandler.getConnection(_dataSource);
    }

    public Connection getConnection(String username, String password) throws SQLException {
        return _dataSource.getConnection(username, password);
    }

    public PrintWriter getLogWriter() throws SQLException {
        return _dataSource.getLogWriter();
    }

    public int getLoginTimeout() throws SQLException {
        return _dataSource.getLoginTimeout();
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
        _dataSource.setLogWriter(out);
    }

    public void setLoginTimeout(int seconds) throws SQLException {
        _dataSource.setLoginTimeout(seconds);
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return _dataSource.unwrap(iface);
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return _dataSource.isWrapperFor(iface);
    }

    // ===================================================================================
    //                                                                Java8 Implementation
    //                                                                ====================
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return super.toString() + ":{" + _dataSource + "}";
    }
}
