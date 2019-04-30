/*
 * Copyright 2014-2019 the original author or authors.
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

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * The connection wrapper that is not closed in the method 'close()'. <br>
 * The method 'close()' do not close really. Only gets out an actual connection.
 * @author jflute
 * @since 0.9.5 (2009/04/29 Wednesday)
 */
public class NotClosingConnectionWrapper implements Connection {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected Connection _actualConnection;
    protected boolean _keepActualIfClosed;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public NotClosingConnectionWrapper(Connection actualConnection) {
        _actualConnection = actualConnection;
    }

    // ===================================================================================
    //                                                                   Actual Connection
    //                                                                   =================
    /**
     * Get the wrapped actual connection.
     * @return The instance of connection to be wrapped. (NullAllowed: after closing and not be kept)
     */
    public Connection getActualConnection() {
        return _actualConnection;
    }

    /**
     * Keep the actual connection in this wrapper even if closed. <br>
     * You can use the connection after closing.
     */
    public void keepActualIfClosed() {
        _keepActualIfClosed = true;
    }

    /**
     * Close the actual connection really if exists.
     * @throws SQLException When it fails to handle the SQL.
     */
    public void closeActualReally() throws SQLException {
        if (_actualConnection != null) {
            _actualConnection.close();
            _actualConnection = null;
        }
    }

    // ===================================================================================
    //                                                                Java6 Implementation
    //                                                                ====================
    public void clearWarnings() throws SQLException {
        _actualConnection.clearWarnings();
    }

    public void close() throws SQLException {
        if (!_keepActualIfClosed) { // normally here
            _actualConnection = null;
        }

        // *Point
        //_actualConnection.close();
    }

    public void commit() throws SQLException {
        _actualConnection.commit();
    }

    public Statement createStatement() throws SQLException {
        return _actualConnection.createStatement();
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return _actualConnection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        return _actualConnection.createStatement(resultSetType, resultSetConcurrency);
    }

    public boolean getAutoCommit() throws SQLException {
        return _actualConnection.getAutoCommit();
    }

    public String getCatalog() throws SQLException {
        return _actualConnection.getCatalog();
    }

    public int getHoldability() throws SQLException {
        return _actualConnection.getHoldability();
    }

    public DatabaseMetaData getMetaData() throws SQLException {
        return _actualConnection.getMetaData();
    }

    public int getTransactionIsolation() throws SQLException {
        return _actualConnection.getTransactionIsolation();
    }

    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return _actualConnection.getTypeMap();
    }

    public SQLWarning getWarnings() throws SQLException {
        return _actualConnection.getWarnings();
    }

    public boolean isClosed() throws SQLException {
        return _actualConnection.isClosed();
    }

    public boolean isReadOnly() throws SQLException {
        return _actualConnection.isReadOnly();
    }

    public String nativeSQL(String sql) throws SQLException {
        return _actualConnection.nativeSQL(sql);
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        return _actualConnection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return _actualConnection.prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    public CallableStatement prepareCall(String sql) throws SQLException {
        return _actualConnection.prepareCall(sql);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        return _actualConnection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return _actualConnection.prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return _actualConnection.prepareStatement(sql, autoGeneratedKeys);
    }

    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return _actualConnection.prepareStatement(sql, columnIndexes);
    }

    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return _actualConnection.prepareStatement(sql, columnNames);
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return _actualConnection.prepareStatement(sql);
    }

    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        _actualConnection.releaseSavepoint(savepoint);
    }

    public void rollback() throws SQLException {
        _actualConnection.rollback();
    }

    public void rollback(Savepoint savepoint) throws SQLException {
        _actualConnection.rollback(savepoint);
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
        _actualConnection.setAutoCommit(autoCommit);
    }

    public void setCatalog(String catalog) throws SQLException {
        _actualConnection.setCatalog(catalog);
    }

    public void setHoldability(int holdability) throws SQLException {
        _actualConnection.setHoldability(holdability);
    }

    public void setReadOnly(boolean readOnly) throws SQLException {
        _actualConnection.setReadOnly(readOnly);
    }

    public Savepoint setSavepoint() throws SQLException {
        return _actualConnection.setSavepoint();
    }

    public Savepoint setSavepoint(String name) throws SQLException {
        return _actualConnection.setSavepoint(name);
    }

    public void setTransactionIsolation(int level) throws SQLException {
        _actualConnection.setTransactionIsolation(level);
    }

    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        _actualConnection.setTypeMap(map);
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return _actualConnection.unwrap(iface);
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return _actualConnection.isWrapperFor(iface);
    }

    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return _actualConnection.createArrayOf(typeName, elements);
    }

    public Blob createBlob() throws SQLException {
        return _actualConnection.createBlob();
    }

    public Clob createClob() throws SQLException {
        return _actualConnection.createClob();
    }

    public NClob createNClob() throws SQLException {
        return _actualConnection.createNClob();
    }

    public SQLXML createSQLXML() throws SQLException {
        return _actualConnection.createSQLXML();
    }

    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        return _actualConnection.createStruct(typeName, attributes);
    }

    public Properties getClientInfo() throws SQLException {
        return _actualConnection.getClientInfo();
    }

    public String getClientInfo(String name) throws SQLException {
        return _actualConnection.getClientInfo(name);
    }

    public boolean isValid(int timeout) throws SQLException {
        return _actualConnection.isValid(timeout);
    }

    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        _actualConnection.setClientInfo(properties);
    }

    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        _actualConnection.setClientInfo(name, value);
    }

    // ===================================================================================
    //                                                                Java8 Implementation
    //                                                                ====================
    public void setSchema(String schema) throws SQLException {
        _actualConnection.setSchema(schema);
    }

    public String getSchema() throws SQLException {
        return _actualConnection.getSchema();
    }

    public void abort(Executor executor) throws SQLException {
        _actualConnection.abort(executor);
    }

    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        _actualConnection.setNetworkTimeout(executor, milliseconds);
    }

    public int getNetworkTimeout() throws SQLException {
        return _actualConnection.getNetworkTimeout();
    }
}
