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
package org.seasar.dbflute.logic.replaceschema.process;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.engine.database.model.UnifiedSchema;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.helper.StringKeyMap;
import org.seasar.dbflute.helper.StringSet;
import org.seasar.dbflute.helper.jdbc.DfRunnerInformation;
import org.seasar.dbflute.helper.jdbc.sqlfile.DfSqlFileFireMan;
import org.seasar.dbflute.helper.jdbc.sqlfile.DfSqlFileFireResult;
import org.seasar.dbflute.helper.jdbc.sqlfile.DfSqlFileRunner;
import org.seasar.dbflute.helper.jdbc.sqlfile.DfSqlFileRunnerDispatcher;
import org.seasar.dbflute.helper.jdbc.sqlfile.DfSqlFileRunnerExecute;
import org.seasar.dbflute.helper.jdbc.sqlfile.DfSqlFileRunnerExecute.DfRunnerDispatchResult;
import org.seasar.dbflute.infra.reps.DfRepsExecuteLimitter;
import org.seasar.dbflute.logic.replaceschema.finalinfo.DfCreateSchemaFinalInfo;
import org.seasar.dbflute.logic.replaceschema.schemainitializer.DfSchemaInitializer;
import org.seasar.dbflute.logic.replaceschema.schemainitializer.factory.DfSchemaInitializerFactory;
import org.seasar.dbflute.logic.replaceschema.schemainitializer.factory.DfSchemaInitializerFactory.InitializeType;
import org.seasar.dbflute.properties.DfReplaceSchemaProperties;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.DfStringUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.9.8.3 (2011/04/29 Friday)
 */
public class DfCreateSchemaProcess extends DfAbstractReplaceSchemaProcess {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance. */
    private static final Log _log = LogFactory.getLog(DfCreateSchemaProcess.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                        Basic Resource
    //                                        --------------
    protected final String _sqlRootDir;
    protected final CreatingDataSourcePlayer _dataSourcePlayer;

    // -----------------------------------------------------
    //                                           Change User
    //                                           -----------
    protected boolean _lazyConnection;
    protected String _currentUser;
    protected StringSet _goodByeUserSet = StringSet.createAsCaseInsensitive();
    protected StringSet _revivedUserSet = StringSet.createAsCaseInsensitive();
    protected StringKeyMap<Connection> _changeUserConnectionMap = StringKeyMap.createAsCaseInsensitive();
    protected boolean _skippedInitializeSchema;
    protected boolean _alreadyExistsMainSchema;
    protected boolean _retryInitializeSchemaFinished;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    protected DfCreateSchemaProcess(String sqlRootDir, CreatingDataSourcePlayer dataSourcePlayer, boolean lazyConnection) {
        _sqlRootDir = sqlRootDir;
        _dataSourcePlayer = dataSourcePlayer;
        _lazyConnection = lazyConnection;
    }

    public static DfCreateSchemaProcess createAsCore(String sqlRootDir, CreatingDataSourcePlayer dataSourcePlayer,
            boolean lazyConnection) {
        return new DfCreateSchemaProcess(sqlRootDir, dataSourcePlayer, lazyConnection);
    }

    public static interface CreatingDataSourcePlayer {

        /**
         * Callback getting data source.
         * It returns valid data source after setupDataSource() success. <br />
         * Basically not null but when data source does not exist on thread, it returns null.
         * @return The data source with schema. (NullAllowed: when data source does not exist on thread, e.g. lazy connection)
         */
        DataSource callbackGetDataSource();

        /**
         * Callback setting up data source.
         * @throws SQLException
         */
        void callbackSetupDataSource() throws SQLException;
    }

    /**
     * Get data source. <br />
     * It returns valid data source after setupDataSource() success. <br />
     * Basically not null but when data source does not exist on thread, it returns null.
     * @return The data source. (NullAllowed: when data source does not exist on thread, e.g. lazy connection)
     */
    protected DataSource getDataSource() {
        return _dataSourcePlayer.callbackGetDataSource();
    }

    /**
     * Set up data source.
     * @throws SQLException
     */
    protected void setupDataSource() throws SQLException {
        _dataSourcePlayer.callbackSetupDataSource();
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    public DfCreateSchemaFinalInfo execute() {
        checkBeforeInitialize();
        initializeSchema();
        final DfRunnerInformation runInfo = createRunnerInformation();
        final DfSqlFileFireResult fireResult = createSchema(runInfo);
        return createFinalInfo(fireResult);
    }

    // ===================================================================================
    //                                                             Check before Initialize 
    //                                                             =======================
    protected void checkBeforeInitialize() {
        final DfRepsExecuteLimitter limitter = createRepsExecuteLimitter();
        limitter.checkExecutableOrNot();
    }

    protected DfRepsExecuteLimitter createRepsExecuteLimitter() {
        final String sqlFileEncoding = getSqlFileEncoding();
        return new DfRepsExecuteLimitter(_sqlRootDir, sqlFileEncoding);
    }

    // ===================================================================================
    //                                                                   Initialize Schema
    //                                                                   =================
    protected void initializeSchema() {
        // additional first for dropping references to main schema
        initializeSchemaAdditionalDrop();
        initializeSchemaMainDrop();
    }

    protected void initializeSchemaAdditionalDrop() {
        final List<Map<String, Object>> additionalDropMapList = getReplaceSchemaProperties().getAdditionalDropMapList();
        if (additionalDropMapList.isEmpty()) {
            return;
        }
        _log.info("");
        _log.info("* * * * * * * * * * * * * * * * * * * *");
        _log.info("*                                     *");
        _log.info("* Initialize Schema (Additional Drop) *");
        _log.info("*                                     *");
        _log.info("* * * * * * * * * * * * * * * * * * * *");
        if (_lazyConnection) {
            _log.info("*Passed because it's a lazy connection");
            return;
        }
        for (Map<String, Object> additionalDropMap : additionalDropMapList) {
            final UnifiedSchema dropSchema = getReplaceSchemaProperties().getAdditionalDropSchema(additionalDropMap);
            final String dropUrl = getReplaceSchemaProperties().getAdditionalDropUrl(additionalDropMap);
            final StringBuilder logSb = new StringBuilder();
            if (dropSchema.hasSchema()) {
                logSb.append("[").append(dropSchema.getLoggingSchema()).append("]");
                if (dropUrl != null && dropUrl.trim().length() > 0) {
                    logSb.append(": ").append(dropUrl);
                }
            } else {
                if (dropUrl != null && dropUrl.trim().length() > 0) {
                    logSb.append(dropUrl);
                }
            }
            _log.info(logSb.toString());
            final DfSchemaInitializer initializer = createSchemaInitializerAdditional(additionalDropMap);
            if (initializer != null) {
                initializer.initializeSchema();
            }
        }
    }

    protected void initializeSchemaMainDrop() {
        _log.info("");
        _log.info("* * * * * * * * * * *");
        _log.info("*                   *");
        _log.info("* Initialize Schema *");
        _log.info("*                   *");
        _log.info("* * * * * * * * * * *");
        if (_lazyConnection) {
            _log.info("*Passed because it's a lazy connection");
            _skippedInitializeSchema = true;
            return;
        }
        final DfSchemaInitializer initializer = createSchemaInitializer(InitializeType.MAIN);
        if (initializer != null) {
            initializer.initializeSchema();
        }
    }

    protected DfSchemaInitializer createSchemaInitializer(InitializeType initializeType) {
        final DfSchemaInitializerFactory factory = createSchemaInitializerFactory(initializeType);
        return factory.createSchemaInitializer();
    }

    protected DfSchemaInitializer createSchemaInitializerAdditional(Map<String, Object> additionalDropMap) {
        final DfSchemaInitializerFactory factory = createSchemaInitializerFactory(InitializeType.ADDTIONAL);
        factory.setAdditionalDropMap(additionalDropMap);
        return factory.createSchemaInitializer();
    }

    protected DfSchemaInitializerFactory createSchemaInitializerFactory(InitializeType initializeType) {
        return new DfSchemaInitializerFactory(getDataSource(), getDatabaseTypeFacadeProp(), getDatabaseProperties(),
                getReplaceSchemaProperties(), initializeType);
    }

    // ===================================================================================
    //                                                                       Create Schema
    //                                                                       =============
    protected DfSqlFileFireResult createSchema(DfRunnerInformation runInfo) {
        _log.info("");
        _log.info("* * * * * * * * *");
        _log.info("*               *");
        _log.info("* Create Schema *");
        _log.info("*               *");
        _log.info("* * * * * * * * *");
        final DfSqlFileFireMan fireMan = new DfSqlFileFireMan();
        fireMan.setExecutorName("Create Schema");
        final DfSqlFileFireResult result = fireMan.fire(getSqlFileRunner(runInfo), getReplaceSchemaSqlFileList());
        destroyChangeUserConnection();
        return result;
    }

    protected DfSqlFileRunner getSqlFileRunner(final DfRunnerInformation runInfo) {
        final DfReplaceSchemaProperties prop = getReplaceSchemaProperties();
        final DfSqlFileRunnerExecute execute = new DfSqlFileRunnerExecuteCreateSchema(runInfo, getDataSource());
        execute.setDispatcher(new DfSqlFileRunnerDispatcher() { // for additional user dispatch
            protected final Set<String> _skippedFileSet = new HashSet<String>();

            public DfRunnerDispatchResult dispatch(File sqlFile, Statement st, String sql) throws SQLException {
                if (_currentUser == null || _currentUser.trim().length() == 0) {
                    return DfRunnerDispatchResult.NONE;
                }
                checkSkippedUser();
                if (isSkippedUser()) {
                    return DfRunnerDispatchResult.SKIPPED;
                }
                Connection conn = _changeUserConnectionMap.get(_currentUser);
                if (conn == null) {
                    _log.info("...Creating a connection to " + _currentUser);
                    conn = prop.createAdditionalUserConnection(_currentUser);
                    if (conn != null) {
                        _changeUserConnectionMap.put(_currentUser, conn);
                    } else {
                        final StringBuilder sb = new StringBuilder();
                        sb.append("...Saying good-bye to the user '").append(_currentUser).append("'");
                        sb.append(" because of no definition");
                        _log.info(sb.toString());
                        _goodByeUserSet.add(_currentUser);
                        return DfRunnerDispatchResult.SKIPPED;
                    }
                }
                final Statement dispatchStmt = conn.createStatement();
                try {
                    dispatchStmt.execute(sql);
                    return DfRunnerDispatchResult.DISPATCHED;
                } catch (SQLException e) {
                    final List<String> argList = analyzeCheckUser(sql);
                    if (argList != null) { // means the command was found
                        if (argList.contains("mainSchema")) {
                            _alreadyExistsMainSchema = true;
                        }
                        final StringBuilder sb = new StringBuilder();
                        sb.append("...Saying good-bye to the user '").append(_currentUser).append("'");
                        sb.append(" because of checked: ").append(argList);
                        _log.info(sb.toString());
                        final String exmsg = e.getMessage();
                        _log.info(" -> " + (exmsg != null ? exmsg.trim() : null));
                        _goodByeUserSet.add(_currentUser);
                        return DfRunnerDispatchResult.SKIPPED;
                    }
                    throw e;
                } finally {
                    if (dispatchStmt != null) {
                        dispatchStmt.close();
                    }
                }
            }

            protected void checkSkippedUser() {
                if (_skippedFileSet.contains(_currentUser)) {
                    return;
                }
                if (prop.isAdditionalUserSkipIfNotFoundPasswordFileAndDefault(_currentUser)) {
                    _log.info("...Skipping the user since no password file: " + _currentUser);
                    _skippedFileSet.add(_currentUser);
                }
            }

            protected boolean isSkippedUser() {
                return _skippedFileSet.contains(_currentUser);
            }
        });
        return execute;
    }

    protected class DfSqlFileRunnerExecuteCreateSchema extends DfSqlFileRunnerExecute {

        public DfSqlFileRunnerExecuteCreateSchema(DfRunnerInformation runInfo, DataSource dataSource) {
            super(runInfo, dataSource);
        }

        @Override
        public void prepare(File sqlFile) {
            super.prepare(sqlFile);
            restoreRevivedUser();
            restoreCurrentUser();
        }

        protected void restoreRevivedUser() {
            for (String revivedUser : _revivedUserSet) {
                if (_goodByeUserSet.contains(revivedUser)) {
                    continue; // already good-bye again
                }
                _log.info("...Saying good-bye to the user '" + revivedUser + "' again");
                _goodByeUserSet.add(revivedUser);
            }
            if (!_revivedUserSet.isEmpty()) {
                _log.info("...Clearing revived users");
                _revivedUserSet.clear();
            }
        }

        protected void restoreCurrentUser() {
            if (_currentUser != null) {
                _log.info("...Coming back to the main user from the user '" + _currentUser + "'");
                _currentUser = null; // because the max scope of change user is one SQL file
            }
        }

        @Override
        protected String filterSql(String sql) {
            sql = super.filterSql(sql);
            sql = getReplaceSchemaProperties().resolveFilterVariablesIfNeeds(sql);
            return sql;
        }

        @Override
        protected boolean isHandlingCommentOnLineSeparator() {
            return true;
        }

        @Override
        protected boolean isDbCommentLine(String line) {
            final boolean commentLine = super.isDbCommentLine(line);
            if (commentLine) {
                return commentLine;
            }
            // for irregular pattern
            return isDbCommentLineForIrregularPattern(line);
        }

        @Override
        protected String getTerminator4Tool() {
            return resolveTerminator4Tool();
        }

        @Override
        protected boolean isTargetFile(String sql) {
            return getReplaceSchemaProperties().isTargetRepsFile(sql);
        }

        @Override
        protected boolean isTargetSql(String sql) {
            final String changeUesr = analyzeChangeUser(sql);
            if (changeUesr != null) {
                _currentUser = changeUesr;
            }
            final boolean backToMainUser = analyzeBackToMainUser(sql);
            if (backToMainUser) {
                _log.info("...Coming back to the main user from the user '" + _currentUser + "'");
                _currentUser = null;
            }
            final boolean reviveUser = analyzeReviveUser(sql);
            if (_currentUser != null && _currentUser.trim().length() > 0) {
                if (_goodByeUserSet.contains(_currentUser)) {
                    if (reviveUser) {
                        _log.info("...Reviving the user '" + _currentUser + "' until the end of this SQL file");
                        _revivedUserSet.add(_currentUser);
                        _goodByeUserSet.remove(_currentUser);
                    } else {
                        String logSql = sql;
                        if (logSql.length() > 30) {
                            logSql = logSql.substring(0, 27) + "...";
                        }
                        _log.info("passed: " + logSql);
                        return false;
                    }
                }
            } else {
                if (reviveUser) {
                    _log.warn("*The mark 'reviveUser()' is unsupported at the timing!");
                }
            }
            return true;
        }

        @Override
        protected void processNonDispatch(String sql) throws SQLException {
            if (!_retryInitializeSchemaFinished && _skippedInitializeSchema && _alreadyExistsMainSchema) {
                _log.info("...Intercepting by retry initializing schema because of skipped before");
                initializeSchema();
                _retryInitializeSchemaFinished = true; // only one called
            }
            super.processNonDispatch(sql);
        }

        @Override
        protected void lazyConnectIfNeeds() throws SQLException {
            if (_lazyConnection) {
                _log.info("...Connecting by main user lazily");
                setupDataSource(); // setting up data source and set it to this thread 
                _dataSource = getDataSource(); // not null because of after setting up data source
                setupConnection();
                setupStatement();
                _lazyConnection = false;
            }
        }
    }

    /**
     * @param sql The target SQL. (NotNull)
     * @return The user changed to. (NotNull) 
     */
    protected String analyzeChangeUser(String sql) {
        final List<String> argList = doAnalyzeCommand(sql, "changeUser");
        if (argList == null) { // means not found
            return null;
        }
        if (argList.isEmpty()) { // because of required
            throwCreateSchemaChangeUserNotFoundArgException(sql);
        }
        return argList.get(0); // only one argument
    }

    protected void throwCreateSchemaChangeUserNotFoundArgException(String sql) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found argument in the 'changeUser' command.");
        br.addItem("SQL");
        br.addElement(sql);
        String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg);
    }

    /**
     * @param sql The target SQL. (NotNull)
     * @return The list of arguments. (NullAllowed: if null, not found the command)
     */
    protected List<String> analyzeCheckUser(String sql) {
        return doAnalyzeCommand(sql, "checkUser");
    }

    protected boolean analyzeBackToMainUser(String sql) {
        final String mark = "#df:backToMainUser()#";
        return sql.contains(mark);
    }

    protected boolean analyzeReviveUser(String sql) {
        final String mark = "#df:reviveUser()#";
        return sql.contains(mark);
    }

    /**
     * @param sql The target SQL. (NotNull)
     * @param commandName The name of command. (NotNull)
     * @return The list of arguments. (NullAllowed: if null, not found the command)
     */
    protected List<String> doAnalyzeCommand(String sql, String commandName) {
        final String beginMark = "#df:" + commandName + "(";
        final int markIndex = sql.indexOf(beginMark);
        if (markIndex < 0) {
            return null;
        }
        final String rear = sql.substring(markIndex + beginMark.length());
        final int endIndex = rear.indexOf(")");
        if (endIndex < 0) {
            throwCreateSchemaCommandCommentInvalidException(sql, commandName);
        }
        final String args = rear.substring(0, endIndex).trim();
        if (Srl.is_NotNull_and_NotTrimmedEmpty(args)) {
            return Srl.splitListTrimmed(args, ",");
        } else {
            return DfCollectionUtil.emptyList();
        }
    }

    protected void throwCreateSchemaCommandCommentInvalidException(String sql, String commandName) {
        ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found end mark ')' in the command.");
        br.addItem("SQL");
        br.addElement(sql);
        br.addItem("Command");
        br.addElement(commandName);
        String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg);
    }

    protected List<File> getReplaceSchemaSqlFileList() {
        final List<File> fileList = new ArrayList<File>();
        fileList.addAll(getReplaceSchemaProperties().getReplaceSchemaSqlFileList(_sqlRootDir));
        fileList.addAll(getReplaceSchemaProperties().getApplicationReplaceSchemaSqlFileList());
        return fileList;
    }

    protected void destroyChangeUserConnection() {
        if (_changeUserConnectionMap.isEmpty()) {
            return;
        }
        _log.info("...Closing connections to change-users: " + _changeUserConnectionMap.keySet());
        for (Entry<String, Connection> entry : _changeUserConnectionMap.entrySet()) {
            final String changeUser = entry.getKey();
            final Connection conn = entry.getValue();
            try {
                conn.close();
            } catch (SQLException continued) {
                String msg = "Failed to close the connection for " + changeUser + ":";
                msg = msg + " message=" + continued.getMessage();
                _log.info(msg);
            }
        }
    }

    // ===================================================================================
    //                                                                          Final Info
    //                                                                          ==========
    protected DfCreateSchemaFinalInfo createFinalInfo(DfSqlFileFireResult fireResult) {
        final DfCreateSchemaFinalInfo finalInfo = new DfCreateSchemaFinalInfo();
        finalInfo.setResultMessage(fireResult.getResultMessage());
        final List<String> detailMessageList = extractDetailMessageList(fireResult);
        for (String detailMessage : detailMessageList) {
            finalInfo.addDetailMessage(detailMessage);
        }
        finalInfo.setBreakCause(fireResult.getBreakCause());
        finalInfo.setFailure(fireResult.isExistsError());
        return finalInfo;
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String replaceString(String text, String fromText, String toText) {
        return DfStringUtil.replace(text, fromText, toText);
    }
}
