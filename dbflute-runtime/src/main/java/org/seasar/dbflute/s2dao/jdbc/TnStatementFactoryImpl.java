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
package org.seasar.dbflute.s2dao.jdbc;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.dbflute.cbean.ConditionBean;
import org.seasar.dbflute.cbean.ConditionBeanContext;
import org.seasar.dbflute.exception.handler.SQLExceptionHandler;
import org.seasar.dbflute.exception.handler.SQLExceptionResource;
import org.seasar.dbflute.jdbc.StatementConfig;
import org.seasar.dbflute.jdbc.StatementFactory;
import org.seasar.dbflute.outsidesql.OutsideSqlContext;
import org.seasar.dbflute.resource.InternalMapContext;
import org.seasar.dbflute.resource.ResourceContext;

/**
 * @author modified by jflute (originated in S2Dao)
 */
public class TnStatementFactoryImpl implements StatementFactory {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log-instance. */
    private static final Log _log = LogFactory.getLog(TnStatementFactoryImpl.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected StatementConfig _defaultStatementConfig;
    protected boolean _internalDebug;
    protected Integer _cursorSelectFetchSize;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public TnStatementFactoryImpl() {
    }

    // ===================================================================================
    //                                                                   PreparedStatement
    //                                                                   =================
    public PreparedStatement createPreparedStatement(Connection conn, String sql) {
        final StatementConfig config = findStatementConfigOnThread();
        final int resultSetType = getResultSetType(config);
        final int resultSetConcurrency = getResultSetConcurrency(config);
        if (isInternalDebugEnabled()) {
            _log.debug("...Preparing statement:(sql, " + resultSetType + ", " + resultSetConcurrency + ")");
        }
        final PreparedStatement ps = prepareStatement(conn, sql, resultSetType, resultSetConcurrency);
        reflectStatementOptions(ps, config);
        return ps;
    }

    protected PreparedStatement prepareStatement(Connection conn, String sql, int resultSetType,
            int resultSetConcurrency) {
        try {
            return conn.prepareStatement(sql, resultSetType, resultSetConcurrency);
        } catch (SQLException e) {
            final SQLExceptionResource resource = createSQLExceptionResource();
            resource.setNotice("Failed to prepare the SQL statement.");
            handleSQLException(e, resource);
            return null; // unreachable
        }
    }

    // -----------------------------------------------------
    //                                       StatementConfig
    //                                       ---------------
    protected StatementConfig findStatementConfigOnThread() {
        final StatementConfig config;
        if (ConditionBeanContext.isExistConditionBeanOnThread()) {
            final ConditionBean cb = ConditionBeanContext.getConditionBeanOnThread();
            config = cb.getStatementConfig();
        } else if (OutsideSqlContext.isExistOutsideSqlContextOnThread()) {
            final OutsideSqlContext context = OutsideSqlContext.getOutsideSqlContextOnThread();
            config = context.getStatementConfig();
        } else { // update or no exist
            config = InternalMapContext.getUpdateStatementConfig();
        }
        return config;
    }

    // -----------------------------------------------------
    //                                      ResultSet Option
    //                                      ----------------
    protected int getResultSetType(StatementConfig config) {
        final int resultSetType;
        if (config != null && config.hasResultSetType()) {
            resultSetType = config.getResultSetType();
        } else {
            final int defaultType = ResultSet.TYPE_FORWARD_ONLY;
            if (_defaultStatementConfig != null && _defaultStatementConfig.hasResultSetType()) {
                if (config != null && config.isSuppressDefault()) {
                    resultSetType = defaultType;
                } else {
                    resultSetType = _defaultStatementConfig.getResultSetType();
                }
            } else {
                resultSetType = defaultType;
            }
        }
        return resultSetType;
    }

    protected int getResultSetConcurrency(StatementConfig config) {
        return ResultSet.CONCUR_READ_ONLY;
    }

    // -----------------------------------------------------
    //                                  Statement Reflection
    //                                  --------------------
    protected void reflectStatementOptions(PreparedStatement ps, StatementConfig config) {
        final StatementConfig actualConfig = getActualStatementConfig(config);
        doReflectStatementOptions(ps, actualConfig);
    }

    protected StatementConfig getActualStatementConfig(StatementConfig config) {
        final boolean existsRequest = config != null;

        final StatementConfig defaultConfig = getActualDefaultConfig(config);
        final boolean existsDefault = defaultConfig != null;

        final Integer cursorSelectFetchSize = getActualCursorSelectFetchSize(config);
        final boolean existsCursor = cursorSelectFetchSize != null;

        final Integer queryTimeout = getActualQueryTimeout(config, existsRequest, defaultConfig, existsDefault);
        final Integer fetchSize = getActualFetchSize(config, existsRequest, cursorSelectFetchSize, existsCursor,
                defaultConfig, existsDefault);
        final Integer maxRows = getActualMaxRows(config, existsRequest, defaultConfig, existsDefault);
        if (queryTimeout == null && fetchSize == null && maxRows == null) {
            return null;
        }

        final StatementConfig actualConfig = new StatementConfig();
        actualConfig.queryTimeout(queryTimeout).fetchSize(fetchSize).maxRows(maxRows);
        return actualConfig;
    }

    protected StatementConfig getActualDefaultConfig(StatementConfig config) {
        final StatementConfig defaultConfig;
        if (_defaultStatementConfig != null) {
            if (config != null && config.isSuppressDefault()) {
                defaultConfig = null; // suppressed
            } else {
                defaultConfig = _defaultStatementConfig.createSnapshot(); // snapshot just in case
            }
        } else {
            defaultConfig = null;
        }
        return defaultConfig;
    }

    protected Integer getActualCursorSelectFetchSize(StatementConfig config) {
        if (config != null && config.isSuppressDefault()) {
            return null; // suppressed
        }
        return _cursorSelectFetchSize;
    }

    protected Integer getActualQueryTimeout(StatementConfig config, boolean existsRequest,
            StatementConfig defaultConfig, boolean existsDefault) {
        final Integer queryTimeout;
        if (existsRequest && config.hasQueryTimeout()) { // priority 1
            queryTimeout = config.getQueryTimeout();
        } else if (existsDefault && defaultConfig.hasQueryTimeout()) { // priority 2
            queryTimeout = defaultConfig.getQueryTimeout();
        } else {
            queryTimeout = null;
        }
        return queryTimeout;
    }

    protected Integer getActualFetchSize(StatementConfig config, boolean existsRequest, Integer cursorSelectFetchSize,
            boolean existsCursor, StatementConfig defaultConfig, boolean existsDefault) {
        final Integer fetchSize;
        if (existsRequest && config.hasFetchSize()) { // priority 1
            fetchSize = config.getFetchSize();
        } else if (existsCursor && isSelectCursorCommand()) { // priority 2
            fetchSize = cursorSelectFetchSize;
        } else if (existsDefault && defaultConfig.hasFetchSize()) { // priority 3
            fetchSize = defaultConfig.getFetchSize();
        } else {
            fetchSize = null;
        }
        return fetchSize;
    }

    protected Integer getActualMaxRows(StatementConfig config, boolean existsRequest, StatementConfig defaultConfig,
            boolean existsDefault) {
        final Integer maxRows;
        if (existsRequest && config.hasMaxRows()) { // priority 1
            maxRows = config.getMaxRows();
        } else if (existsDefault && defaultConfig.hasMaxRows()) { // priority 2
            maxRows = defaultConfig.getMaxRows();
        } else {
            maxRows = null;
        }
        return maxRows;
    }

    protected void doReflectStatementOptions(PreparedStatement ps, StatementConfig actualConfig) {
        if (actualConfig == null || !actualConfig.hasStatementOptions()) {
            return;
        }
        try {
            if (actualConfig.hasQueryTimeout()) {
                final Integer queryTimeout = actualConfig.getQueryTimeout();
                if (isInternalDebugEnabled()) {
                    _log.debug("...Setting queryTimeout of statement: " + queryTimeout);
                }
                ps.setQueryTimeout(queryTimeout);
            }
            if (actualConfig.hasFetchSize()) {
                final Integer fetchSize = actualConfig.getFetchSize();
                if (isInternalDebugEnabled()) {
                    _log.debug("...Setting fetchSize of statement: " + fetchSize);
                }
                ps.setFetchSize(fetchSize);
            }
            if (actualConfig.hasMaxRows()) {
                final Integer maxRows = actualConfig.getMaxRows();
                if (isInternalDebugEnabled()) {
                    _log.debug("...Setting maxRows of statement: " + maxRows);
                }
                ps.setMaxRows(maxRows);
            }
        } catch (SQLException e) {
            final SQLExceptionResource resource = createSQLExceptionResource();
            resource.setNotice("Failed to set the JDBC parameter.");
            handleSQLException(e, resource);
        }
    }

    // ===================================================================================
    //                                                                   CallableStatement
    //                                                                   =================
    public CallableStatement createCallableStatement(Connection conn, String sql) {
        final StatementConfig config = findStatementConfigOnThread();
        final int resultSetType = getResultSetType(config);
        final int resultSetConcurrency = getResultSetConcurrency(config);
        if (isInternalDebugEnabled()) {
            _log.debug("...Preparing callable:(sql, " + resultSetType + ", " + resultSetConcurrency + ")");
        }
        final CallableStatement cs = prepareCall(conn, sql, resultSetType, resultSetConcurrency);
        reflectStatementOptions(cs, config);
        return cs;
    }

    protected CallableStatement prepareCall(Connection conn, String sql, int resultSetType, int resultSetConcurrency) {
        try {
            return conn.prepareCall(sql, resultSetType, resultSetConcurrency);
        } catch (SQLException e) {
            final SQLExceptionResource resource = createSQLExceptionResource();
            resource.setNotice("Failed to prepare the procedure statement.");
            handleSQLException(e, resource);
            return null;// unreachable
        }
    }

    // ===================================================================================
    //                                                               SQLException Handling
    //                                                               =====================
    protected void handleSQLException(SQLException e, SQLExceptionResource resource) {
        createSQLExceptionHandler().handleSQLException(e, resource);
    }

    protected SQLExceptionHandler createSQLExceptionHandler() {
        return ResourceContext.createSQLExceptionHandler();
    }

    protected SQLExceptionResource createSQLExceptionResource() {
        return new SQLExceptionResource();
    }

    // ===================================================================================
    //                                                                        Command Info
    //                                                                        ============
    protected boolean isSelectCursorCommand() {
        if (!ResourceContext.isExistResourceContextOnThread()) {
            return false;
        }
        return ResourceContext.behaviorCommand().isSelectCursor();
    }

    // ===================================================================================
    //                                                                      Internal Debug
    //                                                                      ==============
    private boolean isInternalDebugEnabled() { // because log instance is private
        return _internalDebug && _log.isDebugEnabled();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setDefaultStatementConfig(StatementConfig defaultStatementConfig) {
        _defaultStatementConfig = defaultStatementConfig;
    }

    public void setInternalDebug(boolean internalDebug) {
        _internalDebug = internalDebug;
    }

    public void setCursorSelectFetchSize(Integer cursorSelectFetchSize) {
        _cursorSelectFetchSize = cursorSelectFetchSize;
    }
}
