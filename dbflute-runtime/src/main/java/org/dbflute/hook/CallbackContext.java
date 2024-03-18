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
package org.dbflute.hook;

import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.dbflute.bhv.core.BehaviorCommandHook;
import org.dbflute.bhv.core.BehaviorCommandMeta;
import org.dbflute.util.DfTypeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The context of callback in DBFlute deep logic.
 * @author jflute
 */
public class CallbackContext {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The logger instance for this class. (NotNull) */
    private static final Logger _log = LoggerFactory.getLogger(CallbackContext.class);

    // ===================================================================================
    //                                                                        Thread Local
    //                                                                        ============
    // -----------------------------------------------------
    //                                         Thread Object
    //                                         -------------
    /** The default thread-local for this. */
    protected static final ThreadLocal<CallbackContext> _defaultThreadLocal = new ThreadLocal<CallbackContext>();

    /** The default holder for callback context, using thread local. (NotNull) */
    protected static final CallbackContextHolder _defaultHolder = new CallbackContextHolder() {

        public CallbackContext provide() {
            return _defaultThreadLocal.get();
        }

        public void save(CallbackContext context) {
            _defaultThreadLocal.set(context);
        }
    };

    /** The holder for callback context, might be changed. (NotNull: null setting is not allowed) */
    protected static CallbackContextHolder _holder = _defaultHolder; // as default

    /** Is this static world locked? e.g. you should unlock it to set your own provider. */
    protected static boolean _locked = true; // at first locked

    /**
     * The holder of for callback context. <br>
     * Basically for asynchronous of web framework e.g. Play2.
     */
    public static interface CallbackContextHolder {

        /**
         * Provide callback context. <br>
         * You should return same instance in same request.
         * @return The instance of callback context. (NullAllowed: when no context, but should exist in real handling)
         */
        CallbackContext provide();

        /**
         * Hold callback context and save it in holder.
         * @param callbackContext The callback context set by static setter. (NullAllowed: if null, context is removed)
         */
        void save(CallbackContext callbackContext);
    }

    // -----------------------------------------------------
    //                                        Basic Handling
    //                                        --------------
    /**
     * Get callback context on thread.
     * @return The context of callback. (NullAllowed)
     */
    public static CallbackContext getCallbackContextOnThread() {
        return getActiveHolder().provide();
    }

    /**
     * Set callback context on thread. <br>
     * You can use setting methods per interface instead of this method.
     * @param callbackContext The context of callback. (NotNull)
     */
    public static void setCallbackContextOnThread(CallbackContext callbackContext) {
        if (callbackContext == null) {
            String msg = "The argument 'callbackContext' must not be null.";
            throw new IllegalArgumentException(msg);
        }
        getActiveHolder().save(callbackContext);
    }

    /**
     * Is existing callback context on thread? <br>
     * You can use determination methods per interface instead of this method.
     * @return The determination, true or false.
     */
    public static boolean isExistCallbackContextOnThread() {
        return getActiveHolder().provide() != null;
    }

    /**
     * Clear callback context on thread. <br>
     * Basically you should call other clear methods per interfaces,
     * because this clear method clears all interfaces. 
     */
    public static void clearCallbackContextOnThread() {
        getActiveHolder().save(null);
    }

    /**
     * Get the active holder for callback context.
     * @return The holder instance to handle callback context. (NotNull)
     */
    protected static CallbackContextHolder getActiveHolder() {
        return _holder;
    }

    // -----------------------------------------------------
    //                                            Management
    //                                            ----------
    /**
     * Use the surrogate holder for callback context. (automatically locked after setting) <br>
     * You should call this in application initialization if it needs.
     * @param holder The holder instance. (NullAllowed: if null, use default holder)
     */
    public static void useSurrogateHolder(CallbackContextHolder holder) {
        assertNotLocked();
        if (_log.isInfoEnabled()) {
            _log.info("...Setting surrogate holder for callback context: " + holder);
        }
        if (holder != null) {
            _holder = holder;
        } else {
            _holder = _defaultHolder;
        }
        _locked = true;
    }

    /**
     * Is this static world locked?
     * @return The determination, true or false.
     */
    public static boolean isLocked() {
        return _locked;
    }

    /**
     * Lock this static world, e.g. not to set the holder of thread-local.
     */
    public static void lock() {
        if (_log.isInfoEnabled()) {
            _log.info("...Locking the static world of the callback context!");
        }
        _locked = true;
    }

    /**
     * Unlock this static world, e.g. to set the holder of thread-local.
     */
    public static void unlock() {
        if (_log.isInfoEnabled()) {
            _log.info("...Unlocking the static world of the callback context!");
        }
        _locked = false;
    }

    /**
     * Assert this is not locked.
     */
    protected static void assertNotLocked() {
        if (!isLocked()) {
            return;
        }
        String msg = "The callback context is locked! Don't access at this timing!";
        throw new IllegalStateException(msg);
    }

    // -----------------------------------------------------
    //                                   BehaviorCommandHook
    //                                   -------------------
    /**
     * Set the hook interface of behavior commands. (inheriting existing hooks as default) <br>
     * This hook interface is called-back before executing behavior commands and finally. <br> 
     * The hook methods may be called by nested process so pay attention to it. <br>
     * And don't forget to terminate the last hook in finally scope.
     * <pre>
     * CallbackContext.setBehaviorCommandHook(new BehaviorCommandHook() {
     *     public void hookBefore(BehaviorCommandMeta meta) {
     *         // You can implement your favorite callback here.
     *     }
     *     public void hookFinally(BehaviorCommandMeta meta, RuntimeException cause) {
     *         // You can implement your favorite callback here.
     *     }
     * });
     * try {
     *     ...(DB access)
     * } finally {
     *     CallbackContext.terminateLastBehaviorCommandHookOnThread();
     * }
     * </pre>
     * @param behaviorCommandHook The hook interface of behavior commands. (NullAllowed: completely clear, Inheritable)
     */
    public static void setBehaviorCommandHookOnThread(BehaviorCommandHook behaviorCommandHook) {
        final CallbackContext context = getOrCreateContext();
        context.setBehaviorCommandHook(behaviorCommandHook);
    }

    /**
     * Is existing the hook interface of behavior commands on thread?
     * @return The determination, true or false.
     */
    public static boolean isExistBehaviorCommandHookOnThread() { // memorable code: wants to rename to exists...()
        return isExistCallbackContextOnThread() && getCallbackContextOnThread().getBehaviorCommandHook() != null;
    }

    /**
     * Terminate the last hook interface of behavior commands from callback context on thread. <br>
     * If the callback context does not have other interfaces, the context is removed from thread.
     */
    public static void terminateLastBehaviorCommandHookOnThread() {
        endCallback(context -> context.terminateLastBehaviorCommandHook());
    }

    /**
     * <span style="color: #AD4747; font-size: 120%">
     * (removes all existing hooks completely so use terminateLast...() basically) <br>
     * </span>
     * Clear the hook interface of behavior commands from callback context on thread. <br>
     * If the callback context does not have other interfaces, the context is removed from thread.
     */
    public static void clearBehaviorCommandHookOnThread() {
        endCallback(context -> context.setBehaviorCommandHook(null));
    }

    // -----------------------------------------------------
    //                                           SqlFireHook
    //                                           -----------
    /**
     * Set the hook interface of SQL fires. (inheriting existing hooks as default) <br>
     * This hook interface is called back before firing SQL and finally. <br> 
     * The hook methods may be called by nested process so pay attention to it. <br>
     * And don't forget to terminate the last hook in finally scope.
     * <pre>
     * context.setSqlFireHook(new SqlFireHook() {
     *     public void hookBefore(BehaviorCommandMeta meta, SqlFireReadyInfo fireReadyInfo) {
     *         // You can implement your favorite callback here.
     *     }
     *     public void hookFinally(BehaviorCommandMeta meta, SqlFireResultInfo fireResultInfo) {
     *         // You can implement your favorite callback here.
     *     }
     * });
     * try {
     *     ...(DB access)
     * } finally {
     *     CallbackContext.terminateLastSqlFireHookOnThread();
     * }
     * </pre>
     * @param sqlFireHook The hook interface of SQL fires. (NullAllowed: completely clear, Inheritable)
     */
    public static void setSqlFireHookOnThread(SqlFireHook sqlFireHook) {
        final CallbackContext context = getOrCreateContext();
        context.setSqlFireHook(sqlFireHook);
    }

    /**
     * Is existing the hook interface of behavior commands on thread?
     * @return The determination, true or false.
     */
    public static boolean isExistSqlFireHookOnThread() {
        return isExistCallbackContextOnThread() && getCallbackContextOnThread().getSqlFireHook() != null;
    }

    /**
     * Terminate the last hook interface of SQL fires from callback context on thread. <br>
     * If the callback context does not have other interfaces, the context is removed from thread.
     */
    public static void terminateLastSqlFireHookOnThread() {
        endCallback(context -> context.terminateLastSqlFireHook());
    }

    /**
     * <span style="color: #AD4747; font-size: 120%">
     * (removes all existing hooks completely so use terminateLast...() basically) <br>
     * </span>
     * Clear the hook interface of behavior commands from callback context on thread. <br>
     * If the callback context does not have other interfaces, the context is removed from thread.
     */
    public static void clearSqlFireHookOnThread() {
        endCallback(context -> context.setSqlFireHook(null));
    }

    // -----------------------------------------------------
    //                                         SqlLogHandler
    //                                         -------------
    /**
     * Set the handler of SQL log. (inheriting existing handlers as default) <br>
     * This handler is called back before executing the SQL. <br>
     * The handler methods may be called by nested process so pay attention to it. <br>
     * And don't forget to terminate the last handler in finally scope.
     * <pre>
     * context.setSqlLogHandler(new SqlLogHandler() {
     *     public void handle(SqlLogInfo info) {
     *         // You can get your SQL string here.
     *     }
     * });
     * try {
     *     ...(DB access)
     * } finally {
     *     CallbackContext.terminateLastSqlLogHandlerOnThread();
     * }
     * </pre>
     * @param sqlLogHandler The handler of SQL log. (NullAllowed: completely clear, Inheritable)
     */
    public static void setSqlLogHandlerOnThread(SqlLogHandler sqlLogHandler) {
        final CallbackContext context = getOrCreateContext();
        context.setSqlLogHandler(sqlLogHandler);
    }

    /**
     * Is existing the handler of SQL log on thread?
     * @return The determination, true or false.
     */
    public static boolean isExistSqlLogHandlerOnThread() {
        return isExistCallbackContextOnThread() && getCallbackContextOnThread().getSqlLogHandler() != null;
    }

    /**
     * Terminate the last handler interface of SQL logs from callback context on thread. <br>
     * If the callback context does not have other interfaces, the context is removed from thread.
     */
    public static void terminateLastSqlLogHandlerOnThread() {
        endCallback(context -> context.terminateLastSqlLogHandler());
    }

    /**
     * <span style="color: #AD4747; font-size: 120%">
     * (removes all existing handlers completely so use terminateLast...() basically) <br>
     * </span>
     * Clear the handler of SQL log from callback context on thread. <br>
     * If the callback context does not have other interfaces, the context is removed from thread.
     */
    public static void clearSqlLogHandlerOnThread() {
        endCallback(context -> context.setSqlLogHandler(null));
    }

    // -----------------------------------------------------
    //                                      SqlResultHandler
    //                                      ----------------
    /**
     * Set the handler of SQL result. (inheriting existing handlers as default) <br>
     * This handler is called back before executing the SQL. <br>
     * The handler methods may be called by nested process so pay attention to it. <br>
     * And don't forget to terminate the last handler in finally scope.
     * <pre>
     * context.setSqlResultHandler(new SqlResultHandler() {
     *     public void handle(SqlResultInfo info) {
     *         // You can get your SQL result information here.
     *     }
     * });
     * try {
     *     ...(DB access)
     * } finally {
     *     CallbackContext.terminateLastSqlResultHandlerOnThread();
     * }
     * </pre>
     * @param sqlResultHandler The handler of SQL result. (NullAllowed: completely clear, Inheritable)
     */
    public static void setSqlResultHandlerOnThread(SqlResultHandler sqlResultHandler) {
        final CallbackContext context = getOrCreateContext();
        context.setSqlResultHandler(sqlResultHandler);
    }

    /**
     * Is existing the handler of SQL result on thread?
     * @return The determination, true or false.
     */
    public static boolean isExistSqlResultHandlerOnThread() {
        return isExistCallbackContextOnThread() && getCallbackContextOnThread().getSqlResultHandler() != null;
    }

    /**
     * Terminate the last handler interface of SQL results from callback context on thread. <br>
     * If the callback context does not have other interfaces, the context is removed from thread.
     */
    public static void terminateLastSqlResultHandlerOnThread() {
        endCallback(context -> context.terminateLastSqlResultHandler());
    }

    /**
     * <span style="color: #AD4747; font-size: 120%">
     * (removes all existing handlers completely so use terminateLast...() basically) <br>
     * </span>
     * Clear the handler of SQL result from callback context on thread. <br>
     * If the callback context does not have other interfaces, the context is removed from thread.
     */
    public static void clearSqlResultHandlerOnThread() {
        endCallback(context -> context.setSqlResultHandler(null));
    }

    // -----------------------------------------------------
    //                                       SqlStringFilter
    //                                       ---------------
    /**
     * Set the filter of SQL string. (inheriting existing filters as default) <br>
     * This handler is called back before executing the SQL. <br>
     * The filter methods may be called by nested process so pay attention to it. <br>
     * And don't forget to terminate the last filter in finally scope.
     * <pre>
     * context.setSqlStringFilter(new SqlStringFilter() {
     *     public String filter(String executedSql) {
     *         // You can filter your executed SQL string here.
     *     }
     * });
     * try {
     *     ...(DB access)
     * } finally {
     *     CallbackContext.terminateLastSqlStringFilterOnThread();
     * }
     * </pre>
     * @param sqlStringFilter The filter of SQL string. (NullAllowed: completely clear, Inheritable)
     */
    public static void setSqlStringFilterOnThread(SqlStringFilter sqlStringFilter) {
        final CallbackContext context = getOrCreateContext();
        context.setSqlStringFilter(sqlStringFilter);
    }

    /**
     * Is existing the handler of SQL result on thread?
     * @return The determination, true or false.
     */
    public static boolean isExistSqlStringFilterOnThread() {
        return isExistCallbackContextOnThread() && getCallbackContextOnThread().getSqlStringFilter() != null;
    }

    /**
     * Terminate the last filter interface of SQL strings from callback context on thread. <br>
     * If the callback context does not have other interfaces, the context is removed from thread.
     */
    public static void terminateLastSqlStringFilterOnThread() {
        endCallback(context -> context.terminateLastSqlStringFilter());
    }

    /**
     * <span style="color: #AD4747; font-size: 120%">
     * (removes all existing filters completely so use terminateLast...() basically) <br>
     * </span>
     * Clear the filter of SQL string from callback context on thread. <br>
     * If the callback context does not have other interfaces, the context is removed from thread.
     */
    public static void clearSqlStringFilterOnThread() {
        endCallback(context -> context.setSqlStringFilter(null));
    }

    // -----------------------------------------------------
    //                                         Assist Helper
    //                                         -------------
    protected static CallbackContext getOrCreateContext() {
        if (isExistCallbackContextOnThread()) {
            return getCallbackContextOnThread();
        } else {
            final CallbackContext context = new CallbackContext();
            setCallbackContextOnThread(context);
            return context;
        }
    }

    protected static void endCallback(Consumer<CallbackContext> oneArgLambda) {
        if (isExistCallbackContextOnThread()) {
            final CallbackContext context = getCallbackContextOnThread();
            oneArgLambda.accept(context);
            clearContextIfNoInterface(context);
        }
    }

    protected static void clearContextIfNoInterface(final CallbackContext context) {
        if (!context.hasAnyInterface()) {
            clearCallbackContextOnThread();
        }
    }

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected BehaviorCommandHook _behaviorCommandHook;
    protected SqlFireHook _sqlFireHook;
    protected SqlLogHandler _sqlLogHandler;
    protected SqlResultHandler _sqlResultHandler;
    protected SqlStringFilter _sqlStringFilter;

    // ===================================================================================
    //                                                                       Determination
    //                                                                       =============
    public boolean hasAnyInterface() {
        return _behaviorCommandHook != null || _sqlFireHook != null // hook
                || _sqlLogHandler != null || _sqlResultHandler != null // handler
                || _sqlStringFilter != null; // filter
    }

    // ===================================================================================
    //                                                                        Registration
    //                                                                        ============
    // -----------------------------------------------------
    //                                   BehaviorCommandHook
    //                                   -------------------
    /**
     * Set the hook interface of behavior commands. (inheriting existing hooks as default) <br>
     * This hook interface is called back before executing behavior commands and finally. <br> 
     * The hook methods may be called by nested process so pay attention to it.
     * <pre>
     * context.setBehaviorCommandHook(new BehaviorCommandHook() {
     *     public void hookBefore(BehaviorCommandMeta meta) {
     *         // You can implement your favorite callback here.
     *     }
     *     public void hookFinally(BehaviorCommandMeta meta, RuntimeException cause) {
     *         // You can implement your favorite callback here.
     *     }
     * });
     * </pre>
     * @param behaviorCommandHook The hook interface of behavior commands. (NullAllowed: completely clear, Inheritable)
     */
    public void setBehaviorCommandHook(BehaviorCommandHook behaviorCommandHook) {
        if (_behaviorCommandHook != null && behaviorCommandHook != null && behaviorCommandHook.inheritsExistingHook()) {
            _behaviorCommandHook = newInheritableBehaviorCommandHook(_behaviorCommandHook, behaviorCommandHook);
        } else {
            _behaviorCommandHook = behaviorCommandHook;
        }
    }

    /**
     * Terminate the last behavior command hook. <br>
     * If the only one hook is registered, completely remove the instance from variable.
     */
    public void terminateLastBehaviorCommandHook() {
        if (_behaviorCommandHook instanceof InheritableBehaviorCommandHook) {
            _behaviorCommandHook = ((InheritableBehaviorCommandHook) _behaviorCommandHook).getOriginally();
        } else {
            _behaviorCommandHook = null;
        }
    }

    // -----------------------------------------------------
    //                                           SqlFireHook
    //                                           -----------
    /**
     * Set the hook interface of SQL fires. (inheriting existing hooks as default) <br>
     * This hook interface is called back before firing SQL and finally. <br>
     * The hook methods may be called by nested process so pay attention to it.
     * <pre>
     * context.setSqlFireHook(new SqlFireHook() {
     *     public void hookBefore(BehaviorCommandMeta meta, SqlFireReadyInfo fireReadyInfo) {
     *         // You can implement your favorite callback here.
     *     }
     *     public void hookFinally(BehaviorCommandMeta meta, SqlFireResultInfo fireResultInfo) {
     *         // You can implement your favorite callback here.
     *     }
     * });
     * </pre>
     * @param sqlFireHook The hook interface of SQL fires. (NullAllowed: completely clear, Inheritable)
     */
    public void setSqlFireHook(SqlFireHook sqlFireHook) {
        if (_sqlFireHook != null && sqlFireHook != null && sqlFireHook.inheritsExistingHook()) {
            _sqlFireHook = newInheritableSqlFireHook(_sqlFireHook, sqlFireHook);
        } else {
            _sqlFireHook = sqlFireHook;
        }
    }

    /**
     * Terminate the last SQL fire hook. <br>
     * If the only one hook is registered, completely remove the instance from variable.
     */
    public void terminateLastSqlFireHook() {
        if (_sqlFireHook instanceof InheritableSqlFireHook) {
            _sqlFireHook = ((InheritableSqlFireHook) _sqlFireHook).getOriginally();
        } else {
            _sqlFireHook = null;
        }
    }

    // -----------------------------------------------------
    //                                         SqlLogHandler
    //                                         -------------
    /**
     * Set the handler of SQL log. (inheriting existing hooks as default) <br>
     * This handler is called back before executing the SQL.
     * <pre>
     * context.setSqlLogHandler(new SqlLogHandler() {
     *     public void handle(String executedSql, String displaySql
     *                      , Object[] args, Class&lt;?&gt;[] argTypes) {
     *         // You can get your SQL string here.
     *     }
     * });
     * </pre>
     * @param sqlLogHandler The handler of SQL log. (NullAllowed: completely clear, Inheritable)
     */
    public void setSqlLogHandler(SqlLogHandler sqlLogHandler) {
        if (_sqlLogHandler != null && sqlLogHandler != null && sqlLogHandler.inheritsExistingHandler()) {
            _sqlLogHandler = newInheritableSqlLogHandler(_sqlLogHandler, sqlLogHandler);
        } else {
            _sqlLogHandler = sqlLogHandler;
        }
    }

    /**
     * Terminate the last SQL log handler. <br>
     * If the only one handler is registered, completely remove the instance from variable.
     */
    public void terminateLastSqlLogHandler() {
        if (_sqlLogHandler instanceof InheritableSqlLogHandler) {
            _sqlLogHandler = ((InheritableSqlLogHandler) _sqlLogHandler).getOriginally();
        } else {
            _sqlLogHandler = null;
        }
    }

    // -----------------------------------------------------
    //                                      SqlResultHandler
    //                                      ----------------
    /**
     * Set the handler of SQL result. (inheriting existing hooks as default) <br>
     * This handler is called back before executing the SQL. 
     * <pre>
     * context.setSqlResultHandler(new SqlResultHandler() {
     *     public void handle(SqlResultInfo info) {
     *         // You can get your SQL result information here.
     *     }
     * });
     * </pre>
     * @param sqlResultHandler The handler of SQL result. (NullAllowed: completely clear, Inheritable)
     */
    public void setSqlResultHandler(SqlResultHandler sqlResultHandler) {
        if (_sqlResultHandler != null && sqlResultHandler != null && sqlResultHandler.inheritsExistingHandler()) {
            _sqlResultHandler = newInheritableSqlResultHandler(_sqlResultHandler, sqlResultHandler);
        } else {
            _sqlResultHandler = sqlResultHandler;
        }
    }

    /**
     * Terminate the last SQL log handler. <br>
     * If the only one handler is registered, completely remove the instance from variable.
     */
    public void terminateLastSqlResultHandler() {
        if (_sqlResultHandler instanceof InheritableSqlResultHandler) {
            _sqlResultHandler = ((InheritableSqlResultHandler) _sqlResultHandler).getOriginally();
        } else {
            _sqlResultHandler = null;
        }
    }

    // -----------------------------------------------------
    //                                       SqlStringFilter
    //                                       ---------------
    /**
     * Set the filter of SQL string. (inheriting existing hooks as default) <br>
     * This filter is called back before executing the SQL. 
     * <pre>
     * context.setSqlStringFilter(new SqlStringFilter() {
     *     public String filterSelectCB(BehaviorCommandMeta meta, String executedSql) {
     *         // You can filter your SQL string here.
     *     }
     *     ...
     * });
     * </pre>
     * @param sqlStringFilter The filter of SQL string. (NullAllowed: completely clear, Inheritable)
     */
    public void setSqlStringFilter(SqlStringFilter sqlStringFilter) {
        if (_sqlStringFilter != null && sqlStringFilter != null && sqlStringFilter.inheritsExistingFilter()) {
            _sqlStringFilter = newInheritableSqlStringFilter(_sqlStringFilter, sqlStringFilter);
        } else {
            _sqlStringFilter = sqlStringFilter;
        }
    }

    /**
     * Terminate the last SQL string filter. <br>
     * If the only one filter is registered, completely remove the instance from variable.
     */
    public void terminateLastSqlStringFilter() {
        if (_sqlStringFilter instanceof InheritableSqlStringFilter) {
            _sqlStringFilter = ((InheritableSqlStringFilter) _sqlStringFilter).getOriginally();
        } else {
            _sqlStringFilter = null;
        }
    }

    // ===================================================================================
    //                                                                         Inheritable
    //                                                                         ===========
    // -----------------------------------------------------
    //                                   BehaviorCommandHook
    //                                   -------------------
    protected InheritableBehaviorCommandHook newInheritableBehaviorCommandHook(BehaviorCommandHook originally,
            BehaviorCommandHook yourHook) {
        return new InheritableBehaviorCommandHook(originally, yourHook);
    }

    protected static class InheritableBehaviorCommandHook implements BehaviorCommandHook, InheritableCallback<BehaviorCommandHook> {

        protected final BehaviorCommandHook _originally; // may be inheritable, not null
        protected final BehaviorCommandHook _yourHook; // is not inheritable, always real hook, not null

        public InheritableBehaviorCommandHook(BehaviorCommandHook originally, BehaviorCommandHook yourHook) {
            _originally = originally;
            _yourHook = yourHook;
        }

        public void hookBefore(BehaviorCommandMeta meta) {
            _originally.hookBefore(meta);
            _yourHook.hookBefore(meta); // your is inside
        }

        public void hookFinally(BehaviorCommandMeta meta, RuntimeException cause) {
            _yourHook.hookFinally(meta, cause); // your is inside
            _originally.hookFinally(meta, cause);
        }

        public BehaviorCommandHook getOriginally() {
            return _originally;
        }

        public BehaviorCommandHook getYourHook() {
            return _yourHook;
        }
    }

    // -----------------------------------------------------
    //                                           SqlFireHook
    //                                           -----------
    protected InheritableSqlFireHook newInheritableSqlFireHook(SqlFireHook originally, SqlFireHook yourHook) {
        return new InheritableSqlFireHook(originally, yourHook);
    }

    protected static class InheritableSqlFireHook implements SqlFireHook, InheritableCallback<SqlFireHook> {

        protected final SqlFireHook _originally; // may be inheritable, not null
        protected final SqlFireHook _yourHook; // is not inheritable, always real hook, not null

        public InheritableSqlFireHook(SqlFireHook originally, SqlFireHook yourHook) {
            _originally = originally;
            _yourHook = yourHook;
        }

        public void hookBefore(BehaviorCommandMeta meta, SqlFireReadyInfo fireReadyInfo) {
            _originally.hookBefore(meta, fireReadyInfo);
            _yourHook.hookBefore(meta, fireReadyInfo); // your is inside
        }

        public void hookFinally(BehaviorCommandMeta meta, SqlFireResultInfo fireResultInfo) {
            _yourHook.hookFinally(meta, fireResultInfo); // your is inside
            _originally.hookFinally(meta, fireResultInfo);
        }

        public SqlFireHook getOriginally() {
            return _originally;
        }

        public SqlFireHook getYourHook() {
            return _yourHook;
        }
    }

    // -----------------------------------------------------
    //                                         SqlLogHandler
    //                                         -------------
    protected InheritableSqlLogHandler newInheritableSqlLogHandler(SqlLogHandler originally, SqlLogHandler yourHandler) {
        return new InheritableSqlLogHandler(originally, yourHandler);
    }

    protected static class InheritableSqlLogHandler implements SqlLogHandler, InheritableCallback<SqlLogHandler> {

        protected final SqlLogHandler _originally; // may be inheritable, not null
        protected final SqlLogHandler _yourHandler; // is not inheritable, always real handler, not null

        public InheritableSqlLogHandler(SqlLogHandler originally, SqlLogHandler yourHandler) {
            _originally = originally;
            _yourHandler = yourHandler;
        }

        public void handle(SqlLogInfo info) {
            _originally.handle(info);
            _yourHandler.handle(info);
        }

        public SqlLogHandler getOriginally() {
            return _originally;
        }

        public SqlLogHandler getYourHandler() {
            return _yourHandler;
        }
    }

    // -----------------------------------------------------
    //                                      SqlResultHandler
    //                                      ----------------
    protected InheritableSqlResultHandler newInheritableSqlResultHandler(SqlResultHandler originally, SqlResultHandler yourHandler) {
        return new InheritableSqlResultHandler(originally, yourHandler);
    }

    protected static class InheritableSqlResultHandler implements SqlResultHandler, InheritableCallback<SqlResultHandler> {

        protected final SqlResultHandler _originally; // may be inheritable, not null
        protected final SqlResultHandler _yourHandler; // is not inheritable, always real handler, not null

        public InheritableSqlResultHandler(SqlResultHandler originally, SqlResultHandler yourHandler) {
            _originally = originally;
            _yourHandler = yourHandler;
        }

        public void handle(SqlResultInfo info) {
            _originally.handle(info);
            _yourHandler.handle(info);
        }

        @Override
        public SqlResultHandler getOriginally() {
            return _originally;
        }

        public SqlResultHandler getYourHandler() {
            return _yourHandler;
        }
    }

    // -----------------------------------------------------
    //                                       SqlStringFilter
    //                                       ---------------
    protected InheritableSqlStringFilter newInheritableSqlStringFilter(SqlStringFilter originally, SqlStringFilter yourFilter) {
        return new InheritableSqlStringFilter(originally, yourFilter);
    }

    protected static class InheritableSqlStringFilter implements SqlStringFilter, InheritableCallback<SqlStringFilter> {

        protected final SqlStringFilter _originally; // might be null e.g. when first one
        protected final SqlStringFilter _yourFilter; // is not inheritable, always real filter, not null

        public InheritableSqlStringFilter(SqlStringFilter originally, SqlStringFilter yourFilter) {
            _originally = originally;
            _yourFilter = yourFilter;
        }

        @Override
        public String filterSelectCB(BehaviorCommandMeta meta, String executedSql) {
            return doFilter(executedSql, (filter, sql) -> filter.filterSelectCB(meta, sql));
        }

        @Override
        public String filterEntityUpdate(BehaviorCommandMeta meta, String executedSql) {
            return doFilter(executedSql, (filter, sql) -> filter.filterEntityUpdate(meta, sql));
        }

        @Override
        public String filterQueryUpdate(BehaviorCommandMeta meta, String executedSql) {
            return doFilter(executedSql, (filter, sql) -> filter.filterQueryUpdate(meta, sql));
        }

        @Override
        public String filterOutsideSql(BehaviorCommandMeta meta, String executedSql) {
            return doFilter(executedSql, (filter, sql) -> filter.filterOutsideSql(meta, sql));
        }

        @Override
        public String filterProcedure(BehaviorCommandMeta meta, String executedSql) {
            return doFilter(executedSql, (filter, sql) -> filter.filterProcedure(meta, sql));
        }

        protected String doFilter(String executedSql, BiFunction<SqlStringFilter, String, String> call) {
            final String originallyFiltered = actuallyFilter(_originally, executedSql, call);
            return actuallyFilter(_yourFilter, originallyFiltered, call);

        }

        protected String actuallyFilter(SqlStringFilter filter, String executedSql, BiFunction<SqlStringFilter, String, String> call) {
            final String filtered = call.apply(filter, executedSql);
            return filtered != null ? filtered : executedSql;
        }

        @Override
        public SqlStringFilter getOriginally() {
            return _originally;
        }

        public SqlStringFilter getYourFilter() {
            return _yourFilter;
        }
    }

    // -----------------------------------------------------
    //                                          Small Helper
    //                                          ------------
    protected static interface InheritableCallback<CALL> {

        default int countManagedHook() { // contains myself
            final CALL originally = getOriginally();
            final int nestHookCount;
            if (originally instanceof InheritableBehaviorCommandHook) { // e.g. three or more hooks registered
                nestHookCount = ((InheritableBehaviorCommandHook) originally).countManagedHook();
            } else { // termination
                nestHookCount = 1; // add nested one (the 1 is for originally that is real hook)
            }
            return nestHookCount + 1; // add myself (the 1 is for yourHook)
        }

        CALL getOriginally();
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        final String title = DfTypeUtil.toClassTitle(this);
        final StringBuilder sb = new StringBuilder();
        sb.append(title);
        sb.append(":{behaviorCommandHook=").append(_behaviorCommandHook);
        sb.append(", sqlFireHook=").append(_sqlFireHook);
        sb.append(", sqlLogHandler=").append(_sqlLogHandler);
        sb.append(", sqlResultHandler=").append(_sqlResultHandler);
        sb.append(", sqlStringFilter=").append(_sqlStringFilter);
        sb.append("}");
        return sb.toString();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    // -----------------------------------------------------
    //                                   BehaviorCommandHook
    //                                   -------------------
    public BehaviorCommandHook getBehaviorCommandHook() {
        return _behaviorCommandHook;
    }

    // -----------------------------------------------------
    //                                           SqlFireHook
    //                                           -----------
    public SqlFireHook getSqlFireHook() {
        return _sqlFireHook;
    }

    // -----------------------------------------------------
    //                                         SqlLogHandler
    //                                         -------------
    public SqlLogHandler getSqlLogHandler() {
        return _sqlLogHandler;
    }

    // -----------------------------------------------------
    //                                      SqlResultHandler
    //                                      ----------------
    public SqlResultHandler getSqlResultHandler() {
        return _sqlResultHandler;
    }

    // -----------------------------------------------------
    //                                       SqlStringFilter
    //                                       ---------------
    public SqlStringFilter getSqlStringFilter() {
        return _sqlStringFilter;
    }
}
