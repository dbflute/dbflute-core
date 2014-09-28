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
package org.seasar.dbflute.bhv.core;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.seasar.dbflute.CallbackContext;
import org.seasar.dbflute.DBDef;
import org.seasar.dbflute.Entity;
import org.seasar.dbflute.XLog;
import org.seasar.dbflute.bhv.core.InvokerAssistant.DisposableProcess;
import org.seasar.dbflute.bhv.core.supplement.SequenceCacheHandler;
import org.seasar.dbflute.bhv.logging.invoke.BehaviorInvokeNameExtractor;
import org.seasar.dbflute.bhv.logging.invoke.BehaviorInvokeNameResult;
import org.seasar.dbflute.bhv.logging.invoke.BehaviorInvokePathBuilder;
import org.seasar.dbflute.bhv.logging.invoke.BehaviorInvokePathResult;
import org.seasar.dbflute.bhv.logging.result.BehaviorResultBuilder;
import org.seasar.dbflute.cbean.FetchAssistContext;
import org.seasar.dbflute.cbean.FetchNarrowingBean;
import org.seasar.dbflute.dbmeta.DBMeta;
import org.seasar.dbflute.exception.SQLFailureException;
import org.seasar.dbflute.exception.handler.SQLExceptionResource;
import org.seasar.dbflute.exception.thrower.BehaviorExceptionThrower;
import org.seasar.dbflute.jdbc.ExecutionTimeInfo;
import org.seasar.dbflute.jdbc.SQLExceptionDigger;
import org.seasar.dbflute.jdbc.SqlLogInfo;
import org.seasar.dbflute.jdbc.SqlResultHandler;
import org.seasar.dbflute.jdbc.SqlResultInfo;
import org.seasar.dbflute.jdbc.StatementConfig;
import org.seasar.dbflute.optional.RelationOptionalFactory;
import org.seasar.dbflute.outsidesql.OutsideSqlContext;
import org.seasar.dbflute.outsidesql.executor.OutsideSqlBasicExecutor;
import org.seasar.dbflute.outsidesql.factory.OutsideSqlExecutorFactory;
import org.seasar.dbflute.resource.DBFluteSystem;
import org.seasar.dbflute.resource.InternalMapContext;
import org.seasar.dbflute.resource.InternalMapContext.InvokePathProvider;
import org.seasar.dbflute.resource.ResourceContext;
import org.seasar.dbflute.util.DfTraceViewUtil;
import org.seasar.dbflute.util.DfTypeUtil;
import org.seasar.dbflute.util.Srl;

/**
 * The invoker of behavior command.
 * <pre>
 * public interface methods are as follows:
 *   o clearExecutionCache();
 *   o isExecutionCacheEmpty();
 *   o getExecutionCacheSize();
 *   o injectComponentProperty(BehaviorCommandComponentSetup behaviorCommand);
 *   o invoke(BehaviorCommand behaviorCommand);
 *   o createOutsideSqlBasicExecutor(String tableDbName);
 *   o createBehaviorExceptionThrower();
 *   o getSequenceCacheHandler();
 * </pre>
 * @author jflute
 */
public class BehaviorCommandInvoker {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                      Injection Target
    //                                      ----------------
    protected InvokerAssistant _invokerAssistant;

    // -----------------------------------------------------
    //                                       Execution Cache
    //                                       ---------------
    /** The map of SQL execution. (dispose target, synchronized manually as transaction) */
    protected final Map<String, SqlExecution> _executionMap = newConcurrentHashMap();

    /** The lock object to synchronize the execution map for transaction. (NotNull) */
    protected final Object _executionCacheLock = new Object();

    // -----------------------------------------------------
    //                                    Disposable Process
    //                                    ------------------
    protected final DisposableProcess _disposableProcess = new DisposableProcess() {
        public void dispose() {
            clearExecutionCache();
        }
    };

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public BehaviorCommandInvoker() {
    }

    // ===================================================================================
    //                                                                     Execution Cache
    //                                                                     ===============
    public void clearExecutionCache() {
        // basically should be called only for special case (e.g. HotDeploy)
        synchronized (_executionCacheLock) {
            _executionMap.clear();
        }
    }

    public boolean isExecutionCacheEmpty() {
        return _executionMap.isEmpty();
    }

    public int getExecutionCacheSize() {
        return _executionMap.size();
    }

    // ===================================================================================
    //                                                                      Command Set up
    //                                                                      ==============
    /**
     * Inject the properties of component to the command of behavior. {Public Interface}
     * @param behaviorCommand The command of behavior. (NotNull)
     */
    public void injectComponentProperty(BehaviorCommandComponentSetup behaviorCommand) {
        assertInvokerAssistant();
        behaviorCommand.setDataSource(_invokerAssistant.assistDataSource());
        behaviorCommand.setStatementFactory(_invokerAssistant.assistStatementFactory());
        behaviorCommand.setBeanMetaDataFactory(_invokerAssistant.assistBeanMetaDataFactory());
        behaviorCommand.setSqlFileEncoding(getSqlFileEncoding());
    }

    protected String getSqlFileEncoding() {
        assertInvokerAssistant();
        return _invokerAssistant.assistSqlFileEncoding();
    }

    // ===================================================================================
    //                                                                      Command Invoke
    //                                                                      ==============
    /**
     * Invoke the command of behavior. {Public Interface}
     * This method is an entry point!
     * @param <RESULT> The type of result.
     * @param behaviorCommand The command of behavior. (NotNull)
     * @return The result object. (NullAllowed)
     */
    public <RESULT> RESULT invoke(BehaviorCommand<RESULT> behaviorCommand) {
        RuntimeException cause = null;
        RESULT result = null;
        try {
            final ResourceContext parentContext = getParentContext();
            initializeContext();
            setupResourceContext(behaviorCommand, parentContext);
            processBeforeHook(behaviorCommand);
            result = dispatchInvoking(behaviorCommand);
        } catch (RuntimeException e) {
            cause = e;
        } finally {
            processFinallyHook(behaviorCommand, cause);
            closeContext();
        }
        if (cause != null) {
            throw cause;
        } else {
            return result;
        }
    }

    protected <RESULT> void setupResourceContext(BehaviorCommand<RESULT> behaviorCommand, ResourceContext parentContext) {
        assertInvokerAssistant();
        final ResourceContext resourceContext = new ResourceContext();
        resourceContext.setParentContext(parentContext); // not null only when recursive call
        resourceContext.setBehaviorCommand(behaviorCommand);
        resourceContext.setCurrentDBDef(_invokerAssistant.assistCurrentDBDef());
        resourceContext.setDBMetaProvider(_invokerAssistant.assistDBMetaProvider());
        resourceContext.setSqlClauseCreator(_invokerAssistant.assistSqlClauseCreator());
        resourceContext.setSqlAnalyzerFactory(_invokerAssistant.assistSqlAnalyzerFactory());
        resourceContext.setSQLExceptionHandlerFactory(_invokerAssistant.assistSQLExceptionHandlerFactory());
        resourceContext.setGearedCipherManager(_invokerAssistant.assistGearedCipherManager());
        resourceContext.setResourceParameter(_invokerAssistant.assistResourceParameter());
        ResourceContext.setResourceContextOnThread(resourceContext);
    }

    protected <RESULT> void processBeforeHook(BehaviorCommand<RESULT> behaviorCommand) {
        if (!CallbackContext.isExistBehaviorCommandHookOnThread()) {
            return;
        }
        final BehaviorCommandHook hook = CallbackContext.getCallbackContextOnThread().getBehaviorCommandHook();
        hook.hookBefore(behaviorCommand);
    }

    protected <RESULT> void processFinallyHook(BehaviorCommand<RESULT> behaviorCommand, RuntimeException cause) {
        if (!CallbackContext.isExistBehaviorCommandHookOnThread()) {
            return;
        }
        final BehaviorCommandHook hook = CallbackContext.getCallbackContextOnThread().getBehaviorCommandHook();
        hook.hookFinally(behaviorCommand, cause);
    }

    /**
     * @param <RESULT> The type of result.
     * @param behaviorCommand The command of behavior. (NotNull)
     * @return The result object. (NullAllowed)
     */
    protected <RESULT> RESULT dispatchInvoking(BehaviorCommand<RESULT> behaviorCommand) {
        final boolean logEnabled = isLogEnabled();

        // - - - - - - - - - - - - -
        // Initialize SQL Execution
        // - - - - - - - - - - - - -
        if (behaviorCommand.isInitializeOnly()) {
            initializeSqlExecution(behaviorCommand);
            return null; // The end! (Initialize Only)
        }
        behaviorCommand.beforeGettingSqlExecution();
        SqlExecution execution = findSqlExecution(behaviorCommand);

        // - - - - - - - - - - -
        // Execute SQL Execution
        // - - - - - - - - - - -
        final SqlResultHandler sqlResultHander = getSqlResultHander();
        final boolean hasSqlResultHandler = sqlResultHander != null;
        final long before = deriveCommandBeforeAfterTimeIfNeeds(logEnabled, hasSqlResultHandler);
        Long after = null;
        Object ret = null;
        RuntimeException cause = null;
        try {
            final Object[] args = behaviorCommand.getSqlExecutionArgument();
            ret = executeSql(execution, args);

            final Class<?> retType = behaviorCommand.getCommandReturnType();
            assertRetType(retType, ret);

            after = deriveCommandBeforeAfterTimeIfNeeds(logEnabled, hasSqlResultHandler);
            if (logEnabled) {
                logResult(behaviorCommand, retType, ret, before, after);
            }

            ret = convertReturnValueIfNeeds(ret, retType);
        } catch (RuntimeException e) {
            try {
                handleExecutionException(e); // always throw
            } catch (RuntimeException handled) {
                cause = handled;
                throw handled;
            }
        } finally {
            behaviorCommand.afterExecuting();

            // - - - - - - - - - - - -
            // Call the handler back!
            // - - - - - - - - - - - -
            if (hasSqlResultHandler) {
                callbackSqlResultHanler(behaviorCommand, sqlResultHander, ret, before, after, cause);
            }
        }

        // - - - - - - - - -
        // Cast and Return!
        // - - - - - - - - -
        @SuppressWarnings("unchecked")
        final RESULT result = (RESULT) ret;
        return result;
    }

    protected long deriveCommandBeforeAfterTimeIfNeeds(boolean logEnabled, boolean hasSqlResultHandler) {
        long time = 0;
        if (logEnabled || hasSqlResultHandler) {
            time = systemTime();
        }
        return time;
    }

    protected long systemTime() {
        return DBFluteSystem.currentTimeMillis(); // for calculating performance
    }

    protected Object convertReturnValueIfNeeds(Object ret, Class<?> retType) {
        if (retType.isPrimitive()) {
            return convertPrimitiveWrapper(ret, retType);
        } else if (Number.class.isAssignableFrom(retType)) {
            return convertNumber(ret, retType);
        }
        return ret;
    }

    protected void handleExecutionException(RuntimeException cause) {
        if (cause instanceof SQLFailureException) {
            throw cause;
        }
        final SQLExceptionDigger digger = getSQLExceptionDigger();
        final SQLException sqlEx = digger.digUp(cause);
        if (sqlEx != null) {
            handleSQLException(sqlEx);
        } else {
            throw cause;
        }
    }

    protected void handleSQLException(SQLException e) {
        final SQLExceptionResource resource = new SQLExceptionResource();
        ResourceContext.createSQLExceptionHandler().handleSQLException(e, resource);
    }

    protected <RESULT> void callbackSqlResultHanler(BehaviorCommand<RESULT> behaviorCommand,
            SqlResultHandler sqlResultHander, Object ret, Long commandBefore, Long commandAfter, RuntimeException cause) {
        final SqlLogInfo sqlLogInfo = getResultSqlLogInfo(behaviorCommand);
        final Long sqlBefore = InternalMapContext.getSqlBeforeTimeMillis();
        final Long sqlAfter = InternalMapContext.getSqlAfterTimeMillis();
        final ExecutionTimeInfo timeInfo = new ExecutionTimeInfo(commandBefore, commandAfter, sqlBefore, sqlAfter);
        final SqlResultInfo info = new SqlResultInfo(behaviorCommand, ret, sqlLogInfo, timeInfo, cause);
        sqlResultHander.handle(info);
    }

    protected <RESULT> SqlLogInfo getResultSqlLogInfo(BehaviorCommand<RESULT> behaviorCommand) {
        final SqlLogInfo sqlLogInfo = InternalMapContext.getResultSqlLogInfo();
        if (sqlLogInfo != null) {
            return sqlLogInfo;
        }
        return new SqlLogInfo(behaviorCommand, null, new Object[] {}, new Class<?>[] {},
                new SqlLogInfo.SqlLogDisplaySqlBuilder() {
                    public String build(String executedSql, Object[] bindArgs, Class<?>[] bindArgTypes) {
                        return null;
                    }
                }); // as dummy
    }

    // ===================================================================================
    //                                                                       SQL Execution
    //                                                                       =============
    protected <RESULT> SqlExecution findSqlExecution(final BehaviorCommand<RESULT> behaviorCommand) {
        final boolean logEnabled = isLogEnabled();
        SqlExecution execution = null;
        try {
            final String key = behaviorCommand.buildSqlExecutionKey();
            execution = getSqlExecution(key);
            if (execution == null) {
                long beforeCmd = 0;
                if (logEnabled) {
                    beforeCmd = systemTime();
                }
                SqlExecutionCreator creator = behaviorCommand.createSqlExecutionCreator();
                execution = getOrCreateSqlExecution(key, creator);
                if (logEnabled) {
                    final long afterCmd = systemTime();
                    if (beforeCmd != afterCmd) {
                        logSqlExecution(behaviorCommand, execution, beforeCmd, afterCmd);
                    }
                }
            }
            return execution;
        } finally {
            if (logEnabled) {
                logInvocation(behaviorCommand, false);
            }
            readyInvokePath(behaviorCommand);
        }
    }

    protected <RESULT> void initializeSqlExecution(BehaviorCommand<RESULT> behaviorCommand) {
        final String key = behaviorCommand.buildSqlExecutionKey();
        final SqlExecutionCreator creator = behaviorCommand.createSqlExecutionCreator();
        final SqlExecution execution = getSqlExecution(key);
        if (execution != null) {
            return; // already initialized
        }
        getOrCreateSqlExecution(key, creator); // initialize
    }

    /**
     * Get SQL-execution if it exists.
     * @param key The key of SQL execution. (NotNull)
     * @return The SQL execution that may be created then. (NullAllowed)
     */
    protected SqlExecution getSqlExecution(String key) {
        return _executionMap.get(key);
    }

    /**
     * Get SQL-execution that may be created if it does not exist.
     * @param key The key of SQL-execution. (NotNull)
     * @param executionCreator The creator of SQL-execution. (NotNull)
     * @return The SQL-execution that may be created then. (NotNull)
     */
    protected SqlExecution getOrCreateSqlExecution(String key, SqlExecutionCreator executionCreator) {
        SqlExecution execution = null;
        synchronized (_executionCacheLock) {
            execution = getSqlExecution(key);
            if (execution != null) {
                // previous thread might have initialized
                // or reading might failed by same-time writing
                return execution;
            }
            if (isLogEnabled()) {
                log("...Initializing sqlExecution for the key '" + key + "'");
            }
            execution = executionCreator.createSqlExecution();
            _executionMap.put(key, execution);
        }
        if (execution == null) {
            String msg = "sqlExecutionCreator.createSqlCommand() should not return null:";
            msg = msg + " sqlExecutionCreator=" + executionCreator + " key=" + key;
            throw new IllegalStateException(msg);
        }
        toBeDisposable(); // for HotDeploy
        return execution;
    }

    protected Object executeSql(SqlExecution execution, Object[] args) {
        return execution.execute(args);
    }

    // ===================================================================================
    //                                                                      Log SqlCommand
    //                                                                      ==============
    protected <RESULT> void logSqlExecution(BehaviorCommand<RESULT> behaviorCommand, SqlExecution execution,
            long beforeCmd, long afterCmd) {
        final String view = DfTraceViewUtil.convertToPerformanceView(afterCmd - beforeCmd);
        log("SqlExecution Initialization Cost: [" + view + "]");
    }

    // ===================================================================================
    //                                                                      Log Invocation
    //                                                                      ==============
    protected <RESULT> void logInvocation(BehaviorCommand<RESULT> behaviorCommand, boolean saveOnly) {
        final StackTraceElement[] stackTrace = new Exception().getStackTrace();
        final BehaviorInvokeNameResult behaviorInvokeNameResult = extractBehaviorInvoke(behaviorCommand, stackTrace);
        saveBehaviorInvokeName(behaviorInvokeNameResult);
        final BehaviorInvokePathResult invokePathResult = buildInvokePath(behaviorCommand, stackTrace,
                behaviorInvokeNameResult);
        if (invokePathResult != null) {
            saveClientInvokeName(invokePathResult);
            saveByPassInvokeName(invokePathResult);
            saveInvokePath(invokePathResult);
        }

        if (saveOnly) { // e.g. log level is INFO and invocation path ready
            return;
        }

        final String expNoMethodSuffix = behaviorInvokeNameResult.getInvocationExpNoMethodSuffix();
        final String equalBorder = buildFitBorder("", "=", expNoMethodSuffix, false);
        final String frameBase = "/=====================================================";
        final String spaceBase = "                                                      ";
        log(frameBase + equalBorder + "==");
        log(spaceBase + behaviorInvokeNameResult.getInvocationExp());
        log(spaceBase + equalBorder + "=/");
        if (invokePathResult != null) {
            final String invokePath = invokePathResult.getInvokePath();
            if (Srl.is_NotNull_and_NotTrimmedEmpty(invokePath)) { // just in case
                log(invokePath);
            }
        }

        if (behaviorCommand.isOutsideSql() && !behaviorCommand.isProcedure()) {
            final OutsideSqlContext outsideSqlContext = getOutsideSqlContext();
            if (outsideSqlContext != null) {
                log("path: " + behaviorCommand.getOutsideSqlPath());
                log("option: " + behaviorCommand.getOutsideSqlOption());
            }
        }
    }

    // -----------------------------------------------------
    //                                Extract BehaviorInvoke
    //                                ----------------------
    protected <RESULT> BehaviorInvokeNameResult extractBehaviorInvoke(BehaviorCommand<RESULT> behaviorCommand,
            StackTraceElement[] stackTrace) {
        final DBMeta dbmeta = ResourceContext.provideDBMeta(behaviorCommand.getTableDbName());
        if (dbmeta == null) { // basically no way, only direct invoking
            return createUnknownInvokeNameResult();
        }
        Class<?> outsideSqlResultType = null;
        boolean outsideSqlAutoPaging = false;
        if (behaviorCommand.isOutsideSql()) {
            final OutsideSqlContext outsideSqlContext = getOutsideSqlContext();
            outsideSqlResultType = outsideSqlContext.getResultType();
            outsideSqlAutoPaging = outsideSqlContext.isAutoPagingLogging();
        }
        final BehaviorInvokeNameExtractor extractor = createBehaviorInvokeNameExtractor(dbmeta, outsideSqlResultType,
                outsideSqlAutoPaging);
        return extractor.extractBehaviorInvoke(stackTrace);
    }

    protected BehaviorInvokeNameResult createUnknownInvokeNameResult() { // basically no way
        final String unknownKeyword;
        if (OutsideSqlContext.isExistOutsideSqlContextOnThread()) { // e.g. OutsideSql engine use
            final OutsideSqlContext context = OutsideSqlContext.getOutsideSqlContextOnThread();
            unknownKeyword = context.getTableDbName();
        } else {
            unknownKeyword = "Unknown";
        }
        final String expNoMethodSuffix = unknownKeyword + ".invoke";
        return new BehaviorInvokeNameResult(expNoMethodSuffix + "()", expNoMethodSuffix, null, null);
    }

    protected BehaviorInvokeNameExtractor createBehaviorInvokeNameExtractor(final DBMeta dbmeta,
            Class<?> outsideSqlResultType, boolean outsideSqlAutoPaging) {
        return new BehaviorInvokeNameExtractor(dbmeta, outsideSqlResultType, outsideSqlAutoPaging);
    }

    // -----------------------------------------------------
    //                                 Invocation Adjustment
    //                                 ---------------------
    protected String buildFitBorder(String prefix, String element, String lengthTargetString, boolean space) {
        final int length = space ? lengthTargetString.length() / 2 : lengthTargetString.length();
        final StringBuffer sb = new StringBuffer();
        sb.append(prefix);
        for (int i = 0; i < length; i++) {
            sb.append(element);
            if (space) {
                sb.append(" ");
            }
        }
        if (space) {
            sb.append(element);
        }
        return sb.toString();
    }

    // -----------------------------------------------------
    //                                      Build InvokePath
    //                                      ----------------
    protected <RESULT> BehaviorInvokePathResult buildInvokePath(BehaviorCommand<RESULT> behaviorCommand,
            StackTraceElement[] stackTrace, BehaviorInvokeNameResult behaviorInvokeNameResult) {
        final String[] clientNames = _invokerAssistant.assistClientInvokeNames();
        final String[] byPassNames = _invokerAssistant.assistByPassInvokeNames();
        final BehaviorInvokePathBuilder invokePathBuilder = new BehaviorInvokePathBuilder(clientNames, byPassNames);
        return invokePathBuilder.buildInvokePath(stackTrace, behaviorInvokeNameResult);
    }

    // -----------------------------------------------------
    //                                       Save Invocation
    //                                       ---------------
    // basically for error message
    protected void saveBehaviorInvokeName(BehaviorInvokeNameResult behaviorInvokeNameResult) {
        final String behaviorInvokeName = behaviorInvokeNameResult.getInvocationExp();
        InternalMapContext.setBehaviorInvokeName(behaviorInvokeName);
    }

    protected void saveClientInvokeName(BehaviorInvokePathResult invokePathResult) {
        final String clientInvokeName = invokePathResult != null ? invokePathResult.getClientInvokeName() : null;
        if (clientInvokeName != null && clientInvokeName.trim().length() > 0) {
            InternalMapContext.setClientInvokeName(clientInvokeName);
        }
    }

    protected void saveByPassInvokeName(BehaviorInvokePathResult invokePathResult) {
        final String byPassInvokeName = invokePathResult != null ? invokePathResult.getByPassInvokeName() : null;
        if (byPassInvokeName != null && byPassInvokeName.trim().length() > 0) {
            InternalMapContext.setByPassInvokeName(byPassInvokeName);
        }
    }

    protected void saveInvokePath(BehaviorInvokePathResult invokePathResult) {
        final BehaviorInvokeNameResult behaviorInvokeNameResult = invokePathResult.getBehaviorInvokeNameResult();
        final String invokePath = invokePathResult.getInvokePath();
        final String callerExp = behaviorInvokeNameResult.getInvocationExp();
        final String omitMark = BehaviorInvokePathBuilder.OMIT_MARK;
        InternalMapContext.setSavedInvokePath(Srl.substringLastFront(invokePath, omitMark) + callerExp);
    }

    // ===================================================================================
    //                                                                          Log Result
    //                                                                          ==========
    protected <RESULT> void logResult(BehaviorCommand<RESULT> behaviorCommand, Class<?> retType, Object ret,
            long before, long after) {
        final BehaviorResultBuilder behaviorResultBuilder = createBehaviorResultBuilder();
        final String resultExp = behaviorResultBuilder.buildResultExp(retType, ret, before, after);
        log(resultExp);
        log(" ");
    }

    protected BehaviorResultBuilder createBehaviorResultBuilder() {
        return new BehaviorResultBuilder();
    }

    // ===================================================================================
    //                                                                    InvokePath Ready
    //                                                                    ================
    protected <RESULT> void readyInvokePath(final BehaviorCommand<RESULT> behaviorCommand) {
        // basically for exception message and SQL string filter
        InternalMapContext.setInvokePathProvider(new InvokePathProvider() {
            public String provide() { // lazily
                final String invokePath = InternalMapContext.getSavedInvokePath();
                if (invokePath != null) {
                    return invokePath;
                }
                logInvocation(behaviorCommand, true); // save only
                return InternalMapContext.getSavedInvokePath();
            }
        });
    }

    // ===================================================================================
    //                                                                      Context Helper
    //                                                                      ==============
    protected ResourceContext getParentContext() {
        if (isRecursiveInvoking()) {
            return ResourceContext.getResourceContextOnThread();
        }
        return null;
    }

    protected void initializeContext() {
        if (isRecursiveInvoking()) {
            saveAllContextOnThread();
        }
        clearAllCurrentContext();
    }

    protected boolean isRecursiveInvoking() { // should be called before initialization
        return ResourceContext.isExistResourceContextOnThread();
    }

    protected void closeContext() {
        if (FetchAssistContext.isExistFetchNarrowingBeanOnThread()) {
            // /- - - - - - - - - - - - - - - - - - - - - - - - - - - -
            // Because there is possible that fetch narrowing has been
            // ignored for manualPaging of outsideSql.
            // - - - - - - - - - -/
            final FetchNarrowingBean fnbean = FetchAssistContext.getFetchNarrowingBeanOnThread();
            fnbean.xenableIgnoredFetchNarrowing();
        }
        clearAllCurrentContext();
        restoreAllContextOnThreadIfExists();
    }

    protected void saveAllContextOnThread() {
        ContextStack.saveAllContextOnThread();
    }

    protected void restoreAllContextOnThreadIfExists() {
        ContextStack.restoreAllContextOnThreadIfExists();
    }

    protected void clearAllCurrentContext() {
        ContextStack.clearAllCurrentContext();
    }

    protected OutsideSqlContext getOutsideSqlContext() {
        if (!OutsideSqlContext.isExistOutsideSqlContextOnThread()) {
            return null;
        }
        return OutsideSqlContext.getOutsideSqlContextOnThread();
    }

    protected SqlResultHandler getSqlResultHander() {
        if (!CallbackContext.isExistCallbackContextOnThread()) {
            return null;
        }
        return CallbackContext.getCallbackContextOnThread().getSqlResultHandler();
    }

    // ===================================================================================
    //                                                                  Execute Status Log
    //                                                                  ==================
    protected void log(String msg) {
        XLog.log(msg);
    }

    protected boolean isLogEnabled() {
        return XLog.isLogEnabled();
    }

    // ===================================================================================
    //                                                                             Dispose
    //                                                                             =======
    protected void toBeDisposable() {
        assertInvokerAssistant();
        _invokerAssistant.toBeDisposable(_disposableProcess);
    }

    // ===================================================================================
    //                                                                   Relation Optional
    //                                                                   =================
    /**
     * Get the factory of relation optional.
     * @return The factory assisted by invoker assistant. (NotNull)
     */
    public RelationOptionalFactory getRelationOptionalFactory() {
        return _invokerAssistant.assistRelationOptionalFactory();
    }

    // ===================================================================================
    //                                                                          OutsideSql
    //                                                                          ==========
    /**
     * @param <BEHAVIOR> The type of behavior.
     * @param tableDbName The DB name of table. (NotNull)
     * @return The basic executor of outside SQL. (NotNull) 
     */
    public <BEHAVIOR> OutsideSqlBasicExecutor<BEHAVIOR> createOutsideSqlBasicExecutor(String tableDbName) {
        final OutsideSqlExecutorFactory factory = _invokerAssistant.assistOutsideSqlExecutorFactory();
        final DBDef dbdef = _invokerAssistant.assistCurrentDBDef();
        final StatementConfig config = _invokerAssistant.assistDefaultStatementConfig();
        return factory.createBasic(this, tableDbName, dbdef, config, null); // for an entry instance
    }

    // ===================================================================================
    //                                                                 SQLException Digger
    //                                                                 ===================
    /**
     * Get the digger of SQLException.
     * @return The digger assisted by invoker assistant. (NotNull)
     */
    public SQLExceptionDigger getSQLExceptionDigger() {
        return _invokerAssistant.assistSQLExceptionDigger();
    }

    // ===================================================================================
    //                                                                      Sequence Cache
    //                                                                      ==============
    /**
     * Get the handler of sequence cache.
     * @return The handler assisted by invoker assistant. (NotNull)
     */
    public SequenceCacheHandler getSequenceCacheHandler() {
        return _invokerAssistant.assistSequenceCacheHandler();
    }

    // ===================================================================================
    //                                                                   Exception Thrower
    //                                                                   =================
    /**
     * Get the thrower of behavior exception.
     * @return The thrower of assisted by invoker assistant. (NotNull)
     */
    public BehaviorExceptionThrower createBehaviorExceptionThrower() {
        return _invokerAssistant.assistBehaviorExceptionThrower();
    }

    // ===================================================================================
    //                                                                      Convert Helper
    //                                                                      ==============
    protected Object convertPrimitiveWrapper(Object ret, Class<?> retType) {
        return DfTypeUtil.toWrapper(ret, retType);
    }

    protected Object convertNumber(Object ret, Class<?> retType) {
        return DfTypeUtil.toNumber(ret, retType);
    }

    // ===================================================================================
    //                                                                       Assert Helper
    //                                                                       =============
    protected void assertRetType(Class<?> retType, Object ret) {
        if (List.class.isAssignableFrom(retType)) {
            if (ret != null && !(ret instanceof List<?>)) {
                String msg = "The retType is difference from actual return: ";
                msg = msg + "retType=" + retType + " ret.getClass()=" + ret.getClass() + " ref=" + ret;
                throw new IllegalStateException(msg);
            }
        } else if (Entity.class.isAssignableFrom(retType)) {
            if (ret != null && !(ret instanceof Entity)) {
                String msg = "The retType is difference from actual return: ";
                msg = msg + "retType=" + retType + " ret.getClass()=" + ret.getClass() + " ref=" + ret;
                throw new IllegalStateException(msg);
            }
        }
    }

    protected void assertInvokerAssistant() {
        if (_invokerAssistant == null) {
            String msg = "The attribute 'invokerAssistant' should not be null!";
            throw new IllegalStateException(msg);
        }
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected <KEY, VALUE> ConcurrentHashMap<KEY, VALUE> newConcurrentHashMap() {
        return new ConcurrentHashMap<KEY, VALUE>();
    }

    protected String ln() {
        return DBFluteSystem.getBasicLn();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setInvokerAssistant(InvokerAssistant invokerAssistant) {
        _invokerAssistant = invokerAssistant;
    }
}
