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
package org.seasar.dbflute.resource;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.seasar.dbflute.jdbc.DataSourceHandler;
import org.seasar.dbflute.jdbc.NotClosingConnectionWrapper;

/**
 * The data source of manual thread handling.
 * @author jflute
 * @since 0.9.8.8 (2011/07/21 Thursday)
 */
public class ManualThreadDataSourceHandler implements DataSourceHandler {

    // ===================================================================================
    //                                                                        Thread Local
    //                                                                        ============
    private static final ThreadLocal<ManualThreadDataSourceHandler> _handlerLocal = new ThreadLocal<ManualThreadDataSourceHandler>();

    /**
     * Get the handler of data source.
     * @return The handler instance. (NullAllowed: if null, means no handling)
     */
    public static ManualThreadDataSourceHandler getDataSourceHandler() {
        return _handlerLocal.get();
    }

    /**
     * Prepare data source handler on thread. 
     */
    public static void prepareDataSourceHandler() {
        if (_handlerLocal.get() != null) {
            return; // already prepared
        }
        final ManualThreadDataSourceHandler handler = new ManualThreadDataSourceHandler();
        _handlerLocal.set(handler);
    }

    /**
     * Close data source handler on thread. 
     */
    public static void closeDataSourceHandler() {
        final ManualThreadDataSourceHandler handler = _handlerLocal.get();
        if (handler == null) {
            return; // already closed
        }
        try {
            handler.close();
        } catch (SQLException e) {
            String msg = "Failed to close the data source handler of manual thread: " + handler;
            throw new IllegalStateException(msg, e);
        }
        _handlerLocal.set(null);
    }

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected NotClosingConnectionWrapper _connectionWrapper;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public ManualThreadDataSourceHandler() {
    }

    // ===================================================================================
    //                                                                      Implementation
    //                                                                      ==============
    /**
     * Get the connection instance on thread.
     * @param actualDs The actual data source. (NotNull)
     * @return The wrapped connection which cannot be closed really. (NotNull)
     * @throws java.sql.SQLException
     */
    public Connection getConnection(DataSource actualDs) throws SQLException {
        if (_connectionWrapper != null) {
            return _connectionWrapper;
        }
        final Connection actualConnection = actualDs.getConnection();
        _connectionWrapper = new NotClosingConnectionWrapper(actualConnection);
        _connectionWrapper.keepActualIfClosed(); // to use the connection until handler's close()
        return _connectionWrapper;
    }

    // methods below should be unused

    // ===================================================================================
    //                                                                             Destroy
    //                                                                             =======
    public void close() throws SQLException {
        if (_connectionWrapper != null) {
            _connectionWrapper.closeActualReally();
            _connectionWrapper = null;
        }
    }
}
