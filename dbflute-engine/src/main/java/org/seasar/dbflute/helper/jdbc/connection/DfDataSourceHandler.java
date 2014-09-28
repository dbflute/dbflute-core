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
package org.seasar.dbflute.helper.jdbc.connection;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.dbflute.exception.DfJDBCException;
import org.seasar.dbflute.helper.jdbc.context.DfDataSourceContext;

/**
 * The handler of data source basically for main schema.
 * @author jflute
 */
public class DfDataSourceHandler implements DfConnectionProvider {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance. */
    private static Log _log = LogFactory.getLog(DfDataSourceHandler.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** DB driver. */
    protected String _driver;

    /** DB URL. */
    protected String _url;

    /** User name. */
    protected String _user;

    /** Password */
    protected String _password;

    /** Connection properties. */
    protected Properties _connectionProperties;

    /** Is the mode auto commit? */
    protected boolean _autoCommit;

    /** Cached connection object. */
    protected Connection _cachedConnection;

    /** The meta information of connected database. (lazy load) */
    protected DfConnectionMetaInfo _connectionMetaInfo;

    /** The list of connection hook. (NotNull) */
    protected final List<DfConnectionCreationHook> _hookList = new ArrayList<DfConnectionCreationHook>();

    // ===================================================================================
    //                                                                              Create
    //                                                                              ======
    public void prepare() throws SQLException {
        if (!DfDataSourceContext.isExistDataSource()) {
            _log.info("...Preparing data source:");
            _log.info("  driver = " + _driver);
            _log.info("  url    = " + _url);
            _log.info("  user   = " + _user);
            DfDataSourceContext.setDataSource(new DfFittingDataSource(this));
        }
    }

    public void commit() throws SQLException {
        Connection conn = null;
        try {
            conn = getCachedConnection();
            if (conn == null) {
                return; // if no cache, do nothing
            }
            if (!conn.getAutoCommit()) {
                _log.info("...commit()");
                conn.commit();
            }
        } catch (SQLException e) {
            String msg = "Failed to commit the conection: conn=" + conn;
            throw new DfJDBCException(msg, e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    public void destroy() throws SQLException {
        try {
            final Connection conn = getCachedConnection();
            if (conn == null) {
                return; // if no cache, do nothing
            }
            if (!conn.getAutoCommit()) {
                _log.info("...rollback()");
                conn.rollback();
            }
            if (conn instanceof DfFittingConnection) {
                _log.info("...closeReally()");
                ((DfFittingConnection) conn).closeReally();
            } else {
                _log.info("...close()");
                conn.close();
            }
        } catch (SQLException ignored) {
        } finally {
            if (DfDataSourceContext.isExistDataSource()) {
                DfDataSourceContext.clearDataSource();
            }
        }
    }

    // ===================================================================================
    //                                                             Provider Implementation
    //                                                             =======================
    public Connection getConnection() throws SQLException { // main use
        return processCachedConnection();
    }

    public Connection newConnection() throws SQLException { // special use
        final Connection conn = createConnection();
        hookConnectionCreation(conn);
        return conn;
    }

    public Connection getCachedConnection() {
        return _cachedConnection;
    }

    protected Connection processCachedConnection() throws SQLException {
        if (_cachedConnection != null) {
            return _cachedConnection;
        }
        final Connection conn = createConnection();
        _cachedConnection = new DfFittingConnection(conn);
        setupConnectionMetaInfo(conn);
        hookConnectionCreation(conn);
        return _cachedConnection;
    }

    protected Connection createConnection() throws SQLException {
        final Driver driverInstance = newDriver();
        final Properties info = prepareConnectionProperties();
        Connection conn = null;
        try {
            _log.info("...Connecting to database by data source:");
            conn = driverInstance.connect(_url, info);
        } catch (SQLException e) {
            String msg = "Failed to connect: url=" + _url + " user=" + _user;
            throw new DfJDBCException(msg, e);
        }
        if (conn == null) {
            String msg = "The driver didn't understand the URL: " + _url;
            throw new DfJDBCException(msg);
        }
        try {
            conn.setAutoCommit(_autoCommit);
        } catch (SQLException e) {
            String msg = "Failed to set auto commit: autocommit=" + _autoCommit;
            throw new DfJDBCException(msg, e);
        }
        return conn;
    }

    protected Driver newDriver() {
        final String driver = _driver;
        final Driver driverInstance;
        try {
            final Class<?> dc = Class.forName(driver);
            driverInstance = (Driver) dc.newInstance();
        } catch (ClassNotFoundException e) {
            String msg = "Class Not Found: JDBC driver " + driver + " could not be loaded.";
            throw new IllegalStateException(msg, e);
        } catch (IllegalAccessException e) {
            String msg = "Illegal Access: JDBC driver " + driver + " could not be loaded.";
            throw new IllegalStateException(msg, e);
        } catch (InstantiationException e) {
            String msg = "Instantiation Exception: JDBC driver " + driver + " could not be loaded.";
            throw new IllegalStateException(msg, e);
        }
        return driverInstance;
    }

    protected Properties prepareConnectionProperties() {
        final Properties info = new Properties();
        if (_connectionProperties != null && !_connectionProperties.isEmpty()) {
            info.putAll(_connectionProperties);
        }
        if (_user == null) {
            String msg = "The database user should not be null.";
            throw new IllegalStateException(msg);
        }
        info.put("user", _user);
        if (_password == null) {
            String msg = "The database password should not be null (but empty allowed).";
            throw new IllegalStateException(msg);
        }
        info.put("password", _password);
        return info;
    }

    protected void setupConnectionMetaInfo(Connection conn) throws SQLException {
        try {
            final DfConnectionMetaInfo metaInfo = new DfConnectionMetaInfo();
            final DatabaseMetaData metaData = conn.getMetaData();
            metaInfo.setProductName(metaData.getDatabaseProductName());
            metaInfo.setProductVersion(metaData.getDatabaseProductVersion());
            metaInfo.setDriverName(metaData.getDriverName());
            metaInfo.setDriverVersion(metaData.getDriverVersion());
            final int majorVersion = metaData.getJDBCMajorVersion();
            final int minorVersion = metaData.getJDBCMinorVersion();
            metaInfo.setJdbcVersion(majorVersion + "." + minorVersion);
            _log.info("  product = " + metaInfo.getProductDisp());
            _log.info("  driver  = " + metaInfo.getDriverDisp());
            _connectionMetaInfo = metaInfo;
        } catch (SQLException continued) {
            _log.info("*Failed to get connection meta: " + continued.getMessage());
            _connectionMetaInfo = null;
        }
    }

    protected void hookConnectionCreation(Connection conn) throws SQLException {
        for (DfConnectionCreationHook hook : _hookList) {
            hook.hook(conn);
        }
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return "{url=" + _url + ", user=" + _user + ", prop=" + _connectionProperties + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    /**
     * Set the JDBC driver to be used.
     * @param driver driver class name
     */
    public void setDriver(String driver) {
        this._driver = driver;
    }

    /**
     * Set the DB connection URL.
     * @param url connection URL
     */
    public void setUrl(String url) {
        this._url = url;
    }

    /**
     * Set the user name for the DB connection.
     * @param user database user
     */
    public void setUser(String user) {
        this._user = user;
    }

    /**
     * Set the password for the DB connection.
     * @param password database password
     */
    public void setPassword(String password) {
        this._password = password;
    }

    /**
     * Set the connection properties for the DB connection.
     * @param connectionProperties The connection properties.
     */
    public void setConnectionProperties(Properties connectionProperties) {
        this._connectionProperties = connectionProperties;
    }

    /**
     * Set the autoCommit for the DB connection.
     * @param autoCommit Is auto commit?
     */
    public void setAutoCommit(boolean autoCommit) {
        this._autoCommit = autoCommit;
    }

    /**
     * Get the meta information of connected database.
     * @return The instance of meta information. (NullAllowed)
     */
    public DfConnectionMetaInfo getConnectionMetaInfo() {
        return _connectionMetaInfo;
    }

    /**
     * Add the hook of connection creation.
     * @param hook The implementation instance to hook. (NotNull)
     */
    public void addConnectionCreationHook(DfConnectionCreationHook hook) {
        _hookList.add(hook);
    }
}
