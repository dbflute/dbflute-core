/*
 * Copyright 2014-2015 the original author or authors.
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
package org.dbflute.s2dao.sqlhandler;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.sql.DataSource;

import org.dbflute.bhv.core.context.ConditionBeanContext;
import org.dbflute.bhv.core.context.InternalMapContext;
import org.dbflute.bhv.core.context.ResourceContext;
import org.dbflute.bhv.exception.SQLExceptionHandler;
import org.dbflute.bhv.exception.SQLExceptionResource;
import org.dbflute.cbean.ConditionBean;
import org.dbflute.hook.CallbackContext;
import org.dbflute.hook.SqlFireHook;
import org.dbflute.hook.SqlFireReadyInfo;
import org.dbflute.hook.SqlFireResultInfo;
import org.dbflute.hook.SqlLogHandler;
import org.dbflute.hook.SqlLogInfo;
import org.dbflute.hook.SqlLogInfo.SqlLogDisplaySqlBuilder;
import org.dbflute.hook.SqlResultHandler;
import org.dbflute.jdbc.DataSourceHandler;
import org.dbflute.jdbc.ExecutionTimeInfo;
import org.dbflute.jdbc.HandlingDataSourceWrapper;
import org.dbflute.jdbc.ManualThreadDataSourceHandler;
import org.dbflute.jdbc.NotClosingConnectionWrapper;
import org.dbflute.jdbc.StatementFactory;
import org.dbflute.jdbc.ValueType;
import org.dbflute.s2dao.valuetype.TnValueTypes;
import org.dbflute.system.DBFluteSystem;
import org.dbflute.system.QLog;
import org.dbflute.twowaysql.DisplaySqlBuilder;
import org.dbflute.twowaysql.style.BoundDateDisplayStyle;
import org.dbflute.twowaysql.style.BoundDateDisplayTimeZoneProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The basic handler to execute SQL. <br>
 * All SQL executions of DBFlute are under this handler. <br>
 * This is always created when executing so it's non thread safe.
 * @author modified by jflute (originated in S2Dao)
 */
public abstract class TnAbstractBasicSqlHandler {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance for internal debug. (QLog should be used instead for query log) */
    private static final Logger _log = LoggerFactory.getLogger(TnAbstractBasicSqlHandler.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DataSource _dataSource;
    protected final StatementFactory _statementFactory;
    protected final String _sql;
    protected Object[] _exceptionMessageSqlArgs; // not required

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * Constructor.
     * @param dataSource The data source for a database connection. (NotNull)
     * @param statementFactory The factory of statement. (NotNull)
     * @param sql The executed SQL. (NotNull)
     */
    public TnAbstractBasicSqlHandler(DataSource dataSource, StatementFactory statementFactory, String sql) {
        assertObjectNotNull("dataSource", dataSource);
        assertObjectNotNull("statementFactory", statementFactory);
        assertObjectNotNull("sql", sql);
        _dataSource = dataSource;
        _statementFactory = statementFactory;
        _sql = sql;
    }

    // ===================================================================================
    //                                                                        Common Logic
    //                                                                        ============
    // -----------------------------------------------------
    //                                    Arguments Handling
    //                                    ------------------
    /**
     * @param conn The connection for the database. (NotNull)
     * @param ps The prepared statement for the SQL. (NotNull)
     * @param args The arguments for binding. (NullAllowed)
     * @param valueTypes The types of binding value. (NotNull)
     */
    protected void bindArgs(Connection conn, PreparedStatement ps, Object[] args, ValueType[] valueTypes) {
        if (args == null) {
            return;
        }
        Object current = null;
        try {
            for (int i = 0; i < args.length; ++i) {
                final ValueType valueType = valueTypes[i];
                current = args[i];
                valueType.bindValue(conn, ps, i + 1, current);
            }
        } catch (SQLException e) {
            final SQLExceptionResource resource = createSQLExceptionResource();
            resource.setNotice("Failed to bind the value.");
            if (current != null) {
                resource.addResource("Bound Value", current);
            }
            handleSQLException(e, resource);
        }
    }

    /**
     * @param conn The connection for the database. (NotNull)
     * @param ps The prepared statement for the SQL. (NotNull)
     * @param args The arguments for binding. (NullAllowed)
     * @param argTypes The types of arguments. (NullAllowed: if args is null, this is also null)
     */
    protected void bindArgs(Connection conn, PreparedStatement ps, Object[] args, Class<?>[] argTypes) {
        bindArgs(conn, ps, args, argTypes, 0);
    }

    /**
     * @param conn The connection for the database. (NotNull)
     * @param ps The prepared statement for the SQL. (NotNull)
     * @param args The arguments for binding. (NullAllowed)
     * @param argTypes The types of arguments. (NullAllowed: if args is null, this is also null)
     * @param beginIndex The index for beginning of binding.
     */
    protected void bindArgs(Connection conn, PreparedStatement ps, Object[] args, Class<?>[] argTypes, int beginIndex) {
        if (args == null) {
            return;
        }
        Object current = null;
        try {
            for (int i = beginIndex; i < args.length; ++i) {
                current = args[i];
                final ValueType valueType = findValueType(argTypes[i], current);
                valueType.bindValue(conn, ps, i + 1, current);
            }
        } catch (SQLException e) {
            final SQLExceptionResource resource = createSQLExceptionResource();
            resource.setNotice("Failed to bind the value.");
            if (current != null) {
                resource.addResource("Bound Value", current);
            }
            handleSQLException(e, resource);
        }
    }

    protected ValueType findValueType(Class<?> type, Object instance) {
        return TnValueTypes.findByTypeOrValue(type, instance);
    }

    protected Class<?>[] getArgTypes(Object[] args) {
        if (args == null) {
            return null;
        }
        final Class<?>[] argTypes = new Class[args.length];
        for (int i = 0; i < args.length; ++i) {
            Object arg = args[i];
            if (arg != null) {
                argTypes[i] = arg.getClass();
            }
        }
        return argTypes;
    }

    // -----------------------------------------------------
    //                                           SQL Logging
    //                                           -----------
    protected void logSql(Object[] args, Class<?>[] argTypes) {
        final boolean logEnabled = isLogEnabled();
        final boolean hasSqlFireHook = hasSqlFireHook();
        final boolean hasSqlLog = hasSqlLogHandler();
        final boolean hasSqlResult = hasSqlResultHandler();

        if (logEnabled || hasSqlFireHook || hasSqlLog || hasSqlResult) {
            if (isInternalDebugEnabled()) {
                final String determination = logEnabled + ", " + hasSqlFireHook + ", " + hasSqlLog + ", " + hasSqlResult;
                _log.debug("...Logging SQL by " + determination);
            }
            if (processBeforeLogging(args, argTypes, logEnabled, hasSqlFireHook, hasSqlLog, hasSqlResult)) {
                return; // processed by anyone
            }
            doLogSql(args, argTypes, logEnabled, hasSqlFireHook, hasSqlLog, hasSqlResult);
        }
    }

    protected boolean processBeforeLogging(Object[] args, Class<?>[] argTypes, boolean logEnabled, boolean hasSqlFireHook,
            boolean hasSqlLog, boolean hasSqlResult) {
        return false;
    }

    protected void doLogSql(Object[] args, Class<?>[] argTypes, boolean logEnabled, boolean hasSqlFireHook, boolean hasSqlLog,
            boolean hasSqlResult) {
        final String firstDisplaySql;
        if (logEnabled) { // build at once
            if (isInternalDebugEnabled()) {
                _log.debug("...Building DisplaySql by " + logEnabled);
            }
            firstDisplaySql = buildDisplaySql(_sql, args);
            if (logEnabled) {
                logDisplaySql(firstDisplaySql);
            }
        } else {
            firstDisplaySql = null;
        }
        if (hasSqlFireHook || hasSqlLog || hasSqlResult) { // build lazily
            if (isInternalDebugEnabled()) {
                _log.debug("...Handling SqlFireHook or SqlLog or SqlResult by " + hasSqlFireHook + ", " + hasSqlLog + ", " + hasSqlResult);
            }
            final SqlLogInfo sqlLogInfo = prepareSqlLogInfo(args, argTypes, firstDisplaySql);
            if (sqlLogInfo != null) { // basically true (except override)
                if (hasSqlLog) {
                    getSqlLogHander().handle(sqlLogInfo);
                }
                if (hasSqlFireHook) {
                    saveHookSqlLogInfo(sqlLogInfo);
                }
                if (hasSqlResult) {
                    saveResultSqlLogInfo(sqlLogInfo);
                }
            }
        }
    }

    // -----------------------------------------------------
    //                                            DisplaySql
    //                                            ----------
    protected void logDisplaySql(String displaySql) {
        log((isContainsLineSeparatorInSql(displaySql) ? ln() : "") + displaySql);
    }

    protected boolean isContainsLineSeparatorInSql(String displaySql) {
        return displaySql != null ? displaySql.contains(ln()) : false;
    }

    protected String buildDisplaySql(String sql, Object[] args) {
        return createDisplaySqlBuilder().buildDisplaySql(sql, args);
    }

    protected String getBindVariableText(Object bindVariable) { // basically for sub-class
        return createDisplaySqlBuilder().getBindVariableText(bindVariable);
    }

    protected DisplaySqlBuilder createDisplaySqlBuilder() {
        final BoundDateDisplayStyle realStyle;
        final BoundDateDisplayStyle specifiedStyle = getSpecifiedLogDateDisplayStyle();
        if (specifiedStyle != null) {
            realStyle = specifiedStyle;
        } else {
            realStyle = createResourcedLogDateDisplayStyle();
        }
        return newDisplaySqlBuilder(realStyle);
    }

    protected BoundDateDisplayStyle getSpecifiedLogDateDisplayStyle() {
        if (ConditionBeanContext.isExistConditionBeanOnThread()) {
            final ConditionBean cb = ConditionBeanContext.getConditionBeanOnThread();
            final BoundDateDisplayStyle specifiedStyle = cb.getLogDateDisplayStyle();
            if (specifiedStyle != null) {
                return specifiedStyle;
            }
        }
        return null;
    }

    protected BoundDateDisplayStyle createResourcedLogDateDisplayStyle() {
        final String datePattern = ResourceContext.getLogDatePattern();
        final String iimestampPattern = ResourceContext.getLogTimestampPattern();
        final String timePattern = ResourceContext.getLogTimePattern();
        final BoundDateDisplayTimeZoneProvider timeZoneProvider = ResourceContext.getLogTimeZoneProvider();
        return new BoundDateDisplayStyle(datePattern, iimestampPattern, timePattern, timeZoneProvider);
    }

    protected DisplaySqlBuilder newDisplaySqlBuilder(BoundDateDisplayStyle dateDisplayStyle) {
        return new DisplaySqlBuilder(dateDisplayStyle);
    }

    // -----------------------------------------------------
    //                                           SqlFireHook
    //                                           -----------
    protected SqlFireHook getSqlFireHook() {
        if (!CallbackContext.isExistCallbackContextOnThread()) {
            return null;
        }
        return CallbackContext.getCallbackContextOnThread().getSqlFireHook();
    }

    protected boolean hasSqlFireHook() {
        return getSqlFireHook() != null;
    }

    protected void saveHookSqlLogInfo(SqlLogInfo sqlLogInfo) {
        InternalMapContext.setHookSqlLogInfo(sqlLogInfo);
    }

    // -----------------------------------------------------
    //                                         SqlLogHandler
    //                                         -------------
    protected SqlLogHandler getSqlLogHander() {
        if (!CallbackContext.isExistCallbackContextOnThread()) {
            return null;
        }
        return CallbackContext.getCallbackContextOnThread().getSqlLogHandler();
    }

    protected boolean hasSqlLogHandler() {
        return getSqlLogHander() != null;
    }

    protected SqlLogInfo prepareSqlLogInfo(Object[] args, Class<?>[] argTypes, String alreadyBuiltDisplaySql) {
        final SqlLogDisplaySqlBuilder sqlLogDisplaySqlBuilder = createSqlLogDisplaySqlBuilder(alreadyBuiltDisplaySql);
        return new SqlLogInfo(ResourceContext.behaviorCommand(), _sql, args, argTypes, sqlLogDisplaySqlBuilder);
    }

    protected SqlLogDisplaySqlBuilder createSqlLogDisplaySqlBuilder(final String alreadyBuiltDisplaySql) {
        if (alreadyBuiltDisplaySql != null) {
            return (executedSql, bindArgs, bindArgTypes) -> {
                if (isInternalDebugEnabled()) {
                    _log.debug("...Returning DisplaySql, already built");
                }
                return alreadyBuiltDisplaySql;
            };
        } else {
            return (executedSql, bindArgs, bindArgTypes) -> {
                if (isInternalDebugEnabled()) {
                    _log.debug("...Building DisplaySql lazily");
                }
                return buildDisplaySql(executedSql, bindArgs);
            };
        }
    }

    // -----------------------------------------------------
    //                                      SqlResultHandler
    //                                      ----------------
    protected SqlResultHandler getSqlResultHander() {
        if (!CallbackContext.isExistCallbackContextOnThread()) {
            return null;
        }
        return CallbackContext.getCallbackContextOnThread().getSqlResultHandler();
    }

    protected boolean hasSqlResultHandler() {
        return getSqlResultHander() != null;
    }

    protected void saveResultSqlLogInfo(SqlLogInfo sqlLogInfo) {
        InternalMapContext.setResultSqlLogInfo(sqlLogInfo);
    }

    // ===================================================================================
    //                                                                   Exception Handler
    //                                                                   =================
    protected void handleSQLException(SQLException e, SQLExceptionResource resource) {
        resource.setExecutedSql(_sql);
        resource.setDisplaySql(buildExceptionMessageSql());
        createSQLExceptionHandler().handleSQLException(e, resource);
    }

    protected SQLExceptionHandler createSQLExceptionHandler() {
        return ResourceContext.createSQLExceptionHandler();
    }

    protected SQLExceptionResource createSQLExceptionResource() {
        return new SQLExceptionResource();
    }

    protected String buildExceptionMessageSql() {
        String displaySql = null;
        if (_sql != null && _exceptionMessageSqlArgs != null) {
            try {
                displaySql = buildDisplaySql(_sql, _exceptionMessageSqlArgs);
            } catch (RuntimeException continued) { // because of when exception occurs
                if (_log.isDebugEnabled()) {
                    _log.debug("*Failed to build SQL for an exception message: " + continued.getMessage());
                }
            }
        }
        return displaySql;
    }

    // ===================================================================================
    //                                                                       JDBC Handling
    //                                                                       =============
    // -----------------------------------------------------
    //                                            Connection
    //                                            ----------
    /**
     * Get the database connection from data source. <br>
     * getting connection for SQL executions is only here. <br>
     * (for meta data is at TnBeanMetaDataFactoryImpl)
     * @return The new-created or inherited instance of connection. (NotNull)
     */
    protected Connection getConnection() {
        try {
            final ManualThreadDataSourceHandler handler = getManualThreadDataSourceHandler();
            if (handler != null) {
                return handler.getConnection(_dataSource);
            }
            final Connection conn = _dataSource.getConnection();
            return conn;
        } catch (SQLException e) {
            final SQLExceptionResource resource = createSQLExceptionResource();
            resource.setNotice("Failed to get database connection.");
            handleSQLException(e, resource);
            return null; // unreachable
        }
    }

    /**
     * Get the data source handler of manual thread.
     * @return The instance of the data source handler. (NullAllowed: if null, no manual thread handling)
     */
    protected ManualThreadDataSourceHandler getManualThreadDataSourceHandler() {
        return ManualThreadDataSourceHandler.getDataSourceHandler();
    }

    /**
     * @param conn The instance of connection for the statement. (NotNull)
     * @return The new-created prepared statement. (NotNull)
     */
    protected PreparedStatement prepareStatement(Connection conn) {
        if (_sql == null) {
            throw new IllegalStateException("The SQL should not be null.");
        }
        return _statementFactory.createPreparedStatement(conn, _sql);
    }

    /**
     * @param conn The instance of connection for the statement. (NotNull)
     * @return The new-created call-able statement. (NotNull)
     */
    protected CallableStatement prepareCall(final Connection conn) {
        if (_sql == null) {
            throw new IllegalStateException("The SQL should not be null.");
        }
        return _statementFactory.createCallableStatement(conn, _sql);
    }

    /**
     * Create the data source (wrapper) to inherit connection.
     * @param conn The instance of connection to be wrapped. (NotNull)
     * @return The new-created wrapper for data source handling. (NotNull)
     */
    protected HandlingDataSourceWrapper createInheritedConnectionDataSource(final Connection conn) {
        return new HandlingDataSourceWrapper(_dataSource, new DataSourceHandler() {
            public Connection getConnection(DataSource dataSource) throws SQLException {
                return new NotClosingConnectionWrapper(conn); // not keep actual if closed (is default)
            }
        });
    }

    // -----------------------------------------------------
    //                                             Execution
    //                                             ---------
    // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
    // four super stars:
    // o executeQuery()
    // o executeUpdate()
    // o executeBatch()
    // o executeProcedure()
    // _/_/_/_/_/_/_/_/_/_/
    protected ResultSet executeQuery(PreparedStatement ps) throws SQLException {
        final boolean saveMillis = isSaveMillis();
        if (saveMillis) {
            saveBeforeSqlTimeMillis();
        }
        hookSqlFireBefore();
        ResultSet rs = null;
        SQLException nativeCause = null;
        try {
            rs = ps.executeQuery();
            if (saveMillis) {
                saveAfterSqlTimeMillis();
            }
            return rs;
        } catch (SQLException e) {
            nativeCause = e;
            throw e;
        } finally {
            hookSqlFireFinally(rs, nativeCause);
        }
    }

    protected int executeUpdate(PreparedStatement ps) { // with SQLException handling
        final boolean saveMillis = isSaveMillis();
        if (saveMillis) {
            saveBeforeSqlTimeMillis();
        }
        hookSqlFireBefore();
        Integer updated = null;
        SQLException nativeCause = null;
        try {
            updated = ps.executeUpdate();
            if (saveMillis) {
                saveAfterSqlTimeMillis();
            }
            return updated;
        } catch (SQLException e) {
            nativeCause = e;
            final SQLExceptionResource resource = createSQLExceptionResource();
            final String processTitle = getUpdateSQLFailureProcessTitle();
            resource.setNotice("Failed to execute the SQL for " + processTitle + ".");
            resource.enableUniqueConstraintHandling();
            handleSQLException(e, resource);
            return -1; // unreachable
        } finally {
            hookSqlFireFinally(updated, nativeCause);
        }
    }

    protected String getUpdateSQLFailureProcessTitle() {
        return "update (non-select)"; // as default
    }

    protected int[] executeBatch(PreparedStatement ps, List<?> list) {
        final boolean saveMillis = isSaveMillis();
        if (saveMillis) {
            saveBeforeSqlTimeMillis();
        }
        hookSqlFireBefore();
        int[] batchResult = null;
        SQLException nativeCause = null;
        try {
            batchResult = ps.executeBatch();
            if (saveMillis) {
                saveAfterSqlTimeMillis();
            }
            return batchResult;
        } catch (SQLException e) {
            nativeCause = e;
            final SQLExceptionResource resource = createSQLExceptionResource();
            final String processTitle = getBatchUpdateSQLFailureProcessTitle();
            resource.setNotice("Failed to execute the SQL for " + processTitle + ".");
            resource.enableUniqueConstraintHandling();
            resource.enableDisplaySqlPartHandling();
            handleSQLException(e, resource);
            return null; // unreachable
        } finally {
            hookSqlFireFinally(batchResult, nativeCause);
        }
    }

    protected String getBatchUpdateSQLFailureProcessTitle() {
        return "batch update (non-select)";
    }

    protected void addBatch(PreparedStatement ps) {
        try {
            ps.addBatch();
        } catch (SQLException e) {
            final SQLExceptionResource resource = createSQLExceptionResource();
            resource.setNotice("Failed to add the batch statement.");
            resource.enableUniqueConstraintHandling();
            handleSQLException(e, resource);
        }
    }

    protected boolean executeProcedure(CallableStatement cs) throws SQLException {
        final boolean saveMillis = isSaveMillis();
        if (saveMillis) {
            saveBeforeSqlTimeMillis();
        }
        hookSqlFireBefore();
        Boolean executed = null;
        SQLException nativeCause = null;
        try {
            executed = cs.execute();
            if (saveMillis) {
                saveAfterSqlTimeMillis();
            }
            return executed;
        } catch (SQLException e) {
            nativeCause = e;
            throw e;
        } finally {
            hookSqlFireFinally(executed, nativeCause);
        }
    }

    // -----------------------------------------------------
    //                                            SaveMillis
    //                                            ----------
    protected boolean isSaveMillis() {
        return hasSqlFireHook() || hasSqlResultHandler();
    }

    protected void saveBeforeSqlTimeMillis() {
        InternalMapContext.setSqlBeforeTimeMillis(systemTime());
    }

    protected void saveAfterSqlTimeMillis() {
        InternalMapContext.setSqlAfterTimeMillis(systemTime());
    }

    // -----------------------------------------------------
    //                                           SqlFireHook
    //                                           -----------
    protected void hookSqlFireBefore() {
        if (!hasSqlFireHook()) {
            return;
        }
        final SqlLogInfo sqlLogInfo = InternalMapContext.getHookSqlLogInfo();
        final SqlFireReadyInfo fireReadyInfo = new SqlFireReadyInfo(sqlLogInfo);
        getSqlFireHook().hookBefore(ResourceContext.behaviorCommand(), fireReadyInfo);
    }

    protected void hookSqlFireFinally(Object nativeResult, SQLException nativeCause) {
        if (!hasSqlFireHook()) {
            return;
        }
        final SqlLogInfo sqlLogInfo = InternalMapContext.getHookSqlLogInfo();
        final Long sqlBefore = InternalMapContext.getSqlBeforeTimeMillis();
        final Long sqlAfter = InternalMapContext.getSqlAfterTimeMillis();
        final ExecutionTimeInfo timeInfo = new ExecutionTimeInfo(null, null, sqlBefore, sqlAfter);
        final SqlFireResultInfo fireResultInfo = new SqlFireResultInfo(nativeResult, sqlLogInfo, timeInfo, nativeCause);
        getSqlFireHook().hookFinally(ResourceContext.behaviorCommand(), fireResultInfo);
    }

    // -----------------------------------------------------
    //                                           JDBC Option
    //                                           -----------
    protected void setFetchSize(Statement st, int fetchSize) {
        if (st == null) {
            return;
        }
        try {
            st.setFetchSize(fetchSize);
        } catch (SQLException e) {
            final SQLExceptionResource resource = createSQLExceptionResource();
            resource.setNotice("Failed to set fetch size.");
            resource.addResource("Fetch Size", fetchSize);
            handleSQLException(e, resource);
        }
    }

    protected void setMaxRows(Statement st, int maxRows) {
        if (st == null) {
            return;
        }
        try {
            st.setMaxRows(maxRows);
        } catch (SQLException e) {
            final SQLExceptionResource resource = createSQLExceptionResource();
            resource.setNotice("Failed to set max rows.");
            resource.addResource("Max Rows", maxRows);
            handleSQLException(e, resource);
        }
    }

    // -----------------------------------------------------
    //                                                 Close
    //                                                 -----
    protected void close(Statement st) {
        if (st == null) {
            return;
        }
        try {
            st.close();
        } catch (SQLException e) {
            final SQLExceptionResource resource = createSQLExceptionResource();
            resource.setNotice("Failed to close the statement.");
            handleSQLException(e, resource);
        }
    }

    protected void close(ResultSet resultSet) {
        if (resultSet == null) {
            return;
        }
        try {
            resultSet.close();
        } catch (SQLException e) {
            final SQLExceptionResource resource = createSQLExceptionResource();
            resource.setNotice("Failed to close the result set.");
            handleSQLException(e, resource);
        }
    }

    protected void close(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            conn.close();
        } catch (SQLException e) {
            final SQLExceptionResource resource = createSQLExceptionResource();
            resource.setNotice("Failed to close the database connection.");
            handleSQLException(e, resource);
        }
    }

    // ===================================================================================
    //                                                                           Query Log
    //                                                                           =========
    protected boolean isLogEnabled() {
        return QLog.isLogEnabled();
    }

    protected void log(String msg) {
        QLog.log(msg);
    }

    // ===================================================================================
    //                                                                      Internal Debug
    //                                                                      ==============
    protected boolean isInternalDebugEnabled() { // because log instance is private
        return ResourceContext.isInternalDebug() && _log.isDebugEnabled();
    }

    // ===================================================================================
    //                                                                       Assert Helper
    //                                                                       =============
    protected void assertObjectNotNull(String variableName, Object value) {
        if (variableName == null) {
            String msg = "The value should not be null: variableName=null value=" + value;
            throw new IllegalArgumentException(msg);
        }
        if (value == null) {
            String msg = "The value should not be null: variableName=" + variableName;
            throw new IllegalArgumentException(msg);
        }
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String ln() {
        return DBFluteSystem.ln();
    }

    protected long systemTime() {
        return DBFluteSystem.currentTimeMillis(); // for calculating performance
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setExceptionMessageSqlArgs(Object[] exceptionMessageSqlArgs) {
        this._exceptionMessageSqlArgs = exceptionMessageSqlArgs;
    }
}
