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
package org.dbflute.helper.jdbc.connection;

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
 * @author jflute
 */
public class DfFittingConnection implements Connection {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Connection _realConnection;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfFittingConnection(Connection realConnection) {
        _realConnection = realConnection;
    }

    // ===================================================================================
    //                                                                Java6 Implementation
    //                                                                ====================
    public void clearWarnings() throws SQLException {
        _realConnection.clearWarnings();
    }

    public void close() throws SQLException {
        // not close here to reuse
        //_realConnection.close();
    }

    public void closeReally() throws SQLException {
        _realConnection.close();
    }

    public void commit() throws SQLException {
        _realConnection.commit();
    }

    public Statement createStatement() throws SQLException {
        return _realConnection.createStatement();
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return _realConnection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        return _realConnection.createStatement(resultSetType, resultSetConcurrency);
    }

    public boolean getAutoCommit() throws SQLException {
        return _realConnection.getAutoCommit();
    }

    public String getCatalog() throws SQLException {
        return _realConnection.getCatalog();
    }

    public int getHoldability() throws SQLException {
        return _realConnection.getHoldability();
    }

    public DatabaseMetaData getMetaData() throws SQLException {
        return _realConnection.getMetaData();
    }

    public int getTransactionIsolation() throws SQLException {
        return _realConnection.getTransactionIsolation();
    }

    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return _realConnection.getTypeMap();
    }

    public SQLWarning getWarnings() throws SQLException {
        return _realConnection.getWarnings();
    }

    public boolean isClosed() throws SQLException {
        return _realConnection.isClosed();
    }

    public boolean isReadOnly() throws SQLException {
        return _realConnection.isReadOnly();
    }

    public String nativeSQL(String sql) throws SQLException {
        return _realConnection.nativeSQL(sql);
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        return _realConnection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return _realConnection.prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    public CallableStatement prepareCall(String sql) throws SQLException {
        return _realConnection.prepareCall(sql);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        return _realConnection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return _realConnection.prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return _realConnection.prepareStatement(sql, autoGeneratedKeys);
    }

    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return _realConnection.prepareStatement(sql, columnIndexes);
    }

    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return _realConnection.prepareStatement(sql, columnNames);
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return _realConnection.prepareStatement(sql);
    }

    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        _realConnection.releaseSavepoint(savepoint);
    }

    public void rollback() throws SQLException {
        _realConnection.rollback();
    }

    public void rollback(Savepoint savepoint) throws SQLException {
        _realConnection.rollback(savepoint);
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
        _realConnection.setAutoCommit(autoCommit);
    }

    public void setCatalog(String catalog) throws SQLException {
        _realConnection.setCatalog(catalog);
    }

    public void setHoldability(int holdability) throws SQLException {
        _realConnection.setHoldability(holdability);
    }

    public void setReadOnly(boolean readOnly) throws SQLException {
        _realConnection.setReadOnly(readOnly);
    }

    public Savepoint setSavepoint() throws SQLException {
        return _realConnection.setSavepoint();
    }

    public Savepoint setSavepoint(String name) throws SQLException {
        return _realConnection.setSavepoint(name);
    }

    public void setTransactionIsolation(int level) throws SQLException {
        _realConnection.setTransactionIsolation(level);
    }

    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        _realConnection.setTypeMap(map);
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return _realConnection.isWrapperFor(iface);
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return _realConnection.unwrap(iface);
    }

    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return _realConnection.createArrayOf(typeName, elements);
    }

    public Blob createBlob() throws SQLException {
        return _realConnection.createBlob();
    }

    public Clob createClob() throws SQLException {
        return _realConnection.createClob();
    }

    public NClob createNClob() throws SQLException {
        return _realConnection.createNClob();
    }

    public SQLXML createSQLXML() throws SQLException {
        return _realConnection.createSQLXML();
    }

    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        return _realConnection.createStruct(typeName, attributes);
    }

    public Properties getClientInfo() throws SQLException {
        return _realConnection.getClientInfo();
    }

    public String getClientInfo(String name) throws SQLException {
        return _realConnection.getClientInfo(name);
    }

    public boolean isValid(int timeout) throws SQLException {
        return _realConnection.isValid(timeout);
    }

    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        _realConnection.setClientInfo(properties);
    }

    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        _realConnection.setClientInfo(name, value);
    }

    // ===================================================================================
    //                                                                Java8 Implementation
    //                                                                ====================
    public void setSchema(String schema) throws SQLException {
        _realConnection.setSchema(schema);
    }

    public String getSchema() throws SQLException {
        return _realConnection.getSchema();
    }

    public void abort(Executor executor) throws SQLException {
        _realConnection.abort(executor);
    }

    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        _realConnection.setNetworkTimeout(executor, milliseconds);
    }

    public int getNetworkTimeout() throws SQLException {
        return _realConnection.getNetworkTimeout();
    }
}
