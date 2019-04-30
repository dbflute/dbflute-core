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
package org.dbflute.s2dao.jdbc;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.dbflute.bhv.core.BehaviorCommand;
import org.dbflute.bhv.core.context.ConditionBeanContext;
import org.dbflute.bhv.core.context.InternalMapContext;
import org.dbflute.bhv.core.context.ResourceContext;
import org.dbflute.bhv.exception.SQLExceptionHandler;
import org.dbflute.bhv.exception.SQLExceptionResource;
import org.dbflute.cbean.ConditionBean;
import org.dbflute.cbean.sqlclause.SqlClause;
import org.dbflute.jdbc.FetchBean;
import org.dbflute.jdbc.StatementConfig;
import org.dbflute.jdbc.StatementFactory;
import org.dbflute.outsidesql.OutsideSqlContext;
import org.dbflute.outsidesql.typed.AutoPagingHandlingPmb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author modified by jflute (originated in S2Dao)
 */
public class TnStatementFactoryImpl implements StatementFactory {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The logger instance for this class. (NotNull) */
    private static final Logger _log = LoggerFactory.getLogger(TnStatementFactoryImpl.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected StatementConfig _defaultStatementConfig;
    protected boolean _internalDebug;
    protected Integer _cursorSelectFetchSize;
    protected Integer _entitySelectFetchSize;
    protected boolean _usePagingByCursorSkipSynchronizedFetchSize;
    protected Integer _fixedPagingByCursorSkipSynchronizedFetchSize;

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

    protected PreparedStatement prepareStatement(Connection conn, String sql, int resultSetType, int resultSetConcurrency) {
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

    // ===================================================================================
    //                                                                Statement Reflection
    //                                                                ====================
    // deep logic here
    protected void reflectStatementOptions(PreparedStatement ps, StatementConfig config) {
        final StatementConfig actualConfig = getActualStatementConfig(config);
        doReflectStatementOptions(ps, actualConfig);
    }

    protected StatementConfig getActualStatementConfig(StatementConfig config) {
        final StatementConfig defaultConfig = getActualDefaultConfig(config);
        final Integer queryTimeout = getActualQueryTimeout(config, defaultConfig);
        final Integer fetchSize = getActualFetchSize(config, defaultConfig);
        final Integer maxRows = getActualMaxRows(config, defaultConfig);
        if (queryTimeout == null && fetchSize == null && maxRows == null) {
            return null;
        }

        final StatementConfig actualConfig = new StatementConfig();
        actualConfig.queryTimeout(queryTimeout).fetchSize(fetchSize).maxRows(maxRows);
        return actualConfig;
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

    // -----------------------------------------------------
    //                                 Default Configuration
    //                                 ---------------------
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

    // -----------------------------------------------------
    //                                  Actual Query Timeout
    //                                  --------------------
    protected Integer getActualQueryTimeout(StatementConfig config, StatementConfig defaultConfig) {
        final Integer queryTimeout;
        if (config != null && config.hasQueryTimeout()) { // priority 1
            queryTimeout = config.getQueryTimeout();
        } else if (defaultConfig != null && defaultConfig.hasQueryTimeout()) { // priority 2
            queryTimeout = defaultConfig.getQueryTimeout();
        } else {
            queryTimeout = null;
        }
        return queryTimeout;
    }

    // -----------------------------------------------------
    //                                     Actual Fetch Size
    //                                     -----------------
    protected Integer getActualFetchSize(StatementConfig config, StatementConfig defaultConfig) {
        if (config != null && config.hasFetchSize()) { // priority 1
            return config.getFetchSize();
        }
        final Integer nextToRequestPriorityFetchSize = getNextToRequestPriorityFetchSize();
        if (nextToRequestPriorityFetchSize != null) { // priority 2
            return nextToRequestPriorityFetchSize;
        }
        final Integer commandFetchSize = deriveCommandFetchSize(config);
        if (commandFetchSize != null) { // priority 3
            return commandFetchSize;
        }
        if (defaultConfig != null && defaultConfig.hasFetchSize()) { // priority 4
            return defaultConfig.getFetchSize();
        }
        return null;
    }

    protected Integer deriveCommandFetchSize(StatementConfig config) {
        final BehaviorCommand<?> command = getBehaviorCommand();
        final Integer cursorSelectFetchSize = chooseCursorSelectFetchSize(config);
        if (cursorSelectFetchSize != null && isSelectCursorFetchSizeCommand(command)) {
            return cursorSelectFetchSize;
        }
        final Integer entitySelectFetchSize = chooseEntitySelectFetchSize(config);
        if (entitySelectFetchSize != null && canUseEntitySelectFetchSizeCommand(command)) {
            return entitySelectFetchSize;
        }
        final Integer pagingByCursorSkipSynchronizedFetchSize = extractPagingByCursorSkipSynchronizedFetchSize(config, command);
        if (pagingByCursorSkipSynchronizedFetchSize != null) {
            return pagingByCursorSkipSynchronizedFetchSize;
        }
        return null;
    }

    protected Integer getNextToRequestPriorityFetchSize() { // customize point
        return null;
    }

    // -----------------------------------------------------
    //                                       Actual Max Rows
    //                                       ---------------
    protected Integer getActualMaxRows(StatementConfig config, StatementConfig defaultConfig) {
        final Integer maxRows;
        if (config != null && config.hasMaxRows()) { // priority 1
            maxRows = config.getMaxRows();
        } else if (defaultConfig != null && defaultConfig.hasMaxRows()) { // priority 2
            maxRows = defaultConfig.getMaxRows();
        } else {
            maxRows = null;
        }
        return maxRows;
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
    //                                                                   Command FetchSize
    //                                                                   =================
    protected BehaviorCommand<?> getBehaviorCommand() {
        return ResourceContext.isExistResourceContextOnThread() ? ResourceContext.behaviorCommand() : null;
    }

    // -----------------------------------------------------
    //                                         Cursor Select
    //                                         -------------
    protected Integer chooseCursorSelectFetchSize(StatementConfig config) {
        if (config != null && config.isSuppressDefault()) {
            return null; // suppressed
        }
        return _cursorSelectFetchSize;
    }

    protected boolean isSelectCursorFetchSizeCommand(BehaviorCommand<?> command) {
        return command.isSelectCursor();
    }

    // -----------------------------------------------------
    //                                         Entity Select
    //                                         -------------
    protected Integer chooseEntitySelectFetchSize(StatementConfig config) {
        if (config != null && config.isSuppressDefault()) {
            return null; // suppressed
        }
        return _entitySelectFetchSize;
    }

    protected boolean canUseEntitySelectFetchSizeCommand(BehaviorCommand<?> command) {
        return isFetchBeanSafetyMaxOneSelectCommand(command);
    }

    protected boolean isFetchBeanSafetyMaxOneSelectCommand(BehaviorCommand<?> command) {
        if (isConditionBeanSelectRows(command)) {
            if (ConditionBeanContext.isExistConditionBeanOnThread()) { // basically true, just in case
                final ConditionBean cb = ConditionBeanContext.getConditionBeanOnThread();
                return judgeFetchBeanSafetyMaxOneSelectCommand(command, cb);
            }
        } else if (command.isOutsideSql()) {
            final Object pmb = command.getParameterBean();
            if (pmb instanceof FetchBean) { // basically true if generated parameter-bean
                return judgeFetchBeanSafetyMaxOneSelectCommand(command, ((FetchBean) pmb));
            }
        }
        return false;
    }

    protected boolean judgeFetchBeanSafetyMaxOneSelectCommand(BehaviorCommand<?> command, FetchBean fetchBean) {
        // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
        // cannot determine entity or list by command so it determines from safety max result size
        // the logic is not bad, should be checked if size is one
        //
        // if condition-bean:
        //  selectEntity() or can be treated as selectEntity()
        //  selectByPK(), selectByUniqueOf() calls selectEntity() internally so OK
        //
        // if outsideSql:
        //  selectEntity()
        //
        // or specified as one in other commands
        // _/_/_/_/_/_/_/_/_/_/
        final int safetyMaxResultSize = fetchBean.getSafetyMaxResultSize();
        return safetyMaxResultSize == 1;
    }

    protected boolean isConditionBeanSelectRows(BehaviorCommand<?> command) {
        return command.isConditionBean() && command.isSelect() && !command.isSelectCount();
    }

    // -----------------------------------------------------
    //                                         Paging Select
    //                                         -------------
    protected Integer extractPagingByCursorSkipSynchronizedFetchSize(StatementConfig config, BehaviorCommand<?> command) {
        if (config != null && config.isSuppressDefault()) {
            return null; // suppressed
        }
        if (!isUsePagingByCursorSkipSynchronizedFetchSize()) { // e.g. Oracle, DB2, SQLServer
            return null; // no need to set
        }
        // e.g. MySQL, PostgreSQL here, set if cursor-skip
        Integer cursorSkipFetchSize = null;
        if (isConditionBeanSelectRows(command)) {
            if (ConditionBeanContext.isExistConditionBeanOnThread()) { // basically true, just in case
                final ConditionBean cb = ConditionBeanContext.getConditionBeanOnThread();
                if (mightBeCursorSkipConditionBean(cb)) {
                    cursorSkipFetchSize = cb.getFetchSize();
                }
            }
        } else if (command.isOutsideSql()) {
            final Object pmb = command.getParameterBean();
            if (pmb instanceof AutoPagingHandlingPmb) { // using cursor-skip
                cursorSkipFetchSize = ((AutoPagingHandlingPmb<?, ?>) pmb).getFetchSize();
            }
        }
        if (cursorSkipFetchSize != null && cursorSkipFetchSize > 0) { // means paging
            final Integer fixedSize = getFixedPagingByCursorSkipSynchronizedFetchSize();
            return fixedSize != null ? fixedSize : cursorSkipFetchSize;
        }
        return null;
    }

    protected boolean mightBeCursorSkipConditionBean(ConditionBean cb) {
        final SqlClause sqlClause = cb.getSqlClause();
        return !sqlClause.isFetchStartIndexSupported() || !sqlClause.isFetchSizeSupported();
    }

    protected boolean isUsePagingByCursorSkipSynchronizedFetchSize() { // e.g. MySQL, PostgreSQL
        return _usePagingByCursorSkipSynchronizedFetchSize;
    }

    protected Integer getFixedPagingByCursorSkipSynchronizedFetchSize() { // e.g. Integer.MIN_VALUE (of MySQL)
        return _fixedPagingByCursorSkipSynchronizedFetchSize;
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

    public void setEntitySelectFetchSize(Integer entitySelectFetchSize) {
        _entitySelectFetchSize = entitySelectFetchSize;
    }

    public void setUsePagingByCursorSkipSynchronizedFetchSize(boolean usePagingByCursorSkipSynchronizedFetchSize) {
        _usePagingByCursorSkipSynchronizedFetchSize = usePagingByCursorSkipSynchronizedFetchSize;
    }

    public void setFixedPagingByCursorSkipSynchronizedFetchSize(Integer fixedPagingByCursorSkipSynchronizedFetchSize) {
        _fixedPagingByCursorSkipSynchronizedFetchSize = fixedPagingByCursorSkipSynchronizedFetchSize;
    }
}
