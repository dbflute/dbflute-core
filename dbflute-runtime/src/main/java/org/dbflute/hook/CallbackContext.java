/*
 * Copyright 2014-2018 the original author or authors.
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
     * Set the hook interface of behavior commands. <br>
     * This hook interface is called back before executing behavior commands and finally. <br> 
     * The hook methods may be called by nested process
     * so you should pay attention to it when you implements this.
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
     * @param behaviorCommandHook The hook interface of behavior commands. (NullAllowed)
     */
    public static void setBehaviorCommandHookOnThread(BehaviorCommandHook behaviorCommandHook) {
        final CallbackContext context = getOrCreateContext();
        context.setBehaviorCommandHook(behaviorCommandHook);
    }

    /**
     * Is existing the hook interface of behavior commands on thread?
     * @return The determination, true or false.
     */
    public static boolean isExistBehaviorCommandHookOnThread() {
        return isExistCallbackContextOnThread() && getCallbackContextOnThread().getBehaviorCommandHook() != null;
    }

    /**
     * Clear the hook interface of behavior commands from callback context on thread. <br>
     * If the callback context does not have other interfaces, the context is removed from thread.
     */
    public static void clearBehaviorCommandHookOnThread() {
        if (isExistCallbackContextOnThread()) {
            final CallbackContext context = getCallbackContextOnThread();
            context.setBehaviorCommandHook(null);
            clearIfNoInterface(context);
        }
    }

    // -----------------------------------------------------
    //                                           SqlFireHook
    //                                           -----------
    /**
     * Set the hook interface of SQL fires. <br>
     * This hook interface is called back before firing SQL and finally. <br> 
     * The hook methods may be called by nested process
     * so you should pay attention to it when you implements this.
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
     * @param sqlFireHook The hook interface of behavior commands. (NullAllowed)
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
     * Clear the hook interface of behavior commands from callback context on thread. <br>
     * If the callback context does not have other interfaces, the context is removed from thread.
     */
    public static void clearSqlFireHookOnThread() {
        if (isExistCallbackContextOnThread()) {
            final CallbackContext context = getCallbackContextOnThread();
            context.setSqlFireHook(null);
            clearIfNoInterface(context);
        }
    }

    // -----------------------------------------------------
    //                                         SqlLogHandler
    //                                         -------------
    /**
     * Set the handler of SQL log. <br>
     * This handler is called back before executing the SQL.
     * <pre>
     * context.setSqlLogHandler(new SqlLogHandler() {
     *     public void handle(SqlLogInfo info) {
     *         // You can get your SQL string here.
     *     }
     * });
     * </pre>
     * @param sqlLogHandler The handler of SQL log. (NullAllowed)
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
     * Clear the handler of SQL log from callback context on thread. <br>
     * If the callback context does not have other interfaces, the context is removed from thread.
     */
    public static void clearSqlLogHandlerOnThread() {
        if (isExistCallbackContextOnThread()) {
            final CallbackContext context = getCallbackContextOnThread();
            context.setSqlLogHandler(null);
            clearIfNoInterface(context);
        }
    }

    // -----------------------------------------------------
    //                                      SqlResultHandler
    //                                      ----------------
    /**
     * Set the handler of SQL result. <br>
     * This handler is called back before executing the SQL. 
     * <pre>
     * context.setSqlResultHandler(new SqlResultHandler() {
     *     public void handle(SqlResultInfo info) {
     *         // You can get your SQL result information here.
     *     }
     * });
     * </pre>
     * @param sqlResultHandler The handler of SQL result. (NullAllowed)
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
     * Clear the handler of SQL result from callback context on thread. <br>
     * If the callback context does not have other interfaces, the context is removed from thread.
     */
    public static void clearSqlResultHandlerOnThread() {
        if (isExistCallbackContextOnThread()) {
            final CallbackContext context = getCallbackContextOnThread();
            context.setSqlResultHandler(null);
            clearIfNoInterface(context);
        }
    }

    // -----------------------------------------------------
    //                                       SqlStringFilter
    //                                       ---------------
    /**
     * Set the filter of SQL string. <br>
     * This handler is called back before executing the SQL. 
     * <pre>
     * context.setSqlStringFilter(new SqlStringFilter() {
     *     public String filter(String executedSql) {
     *         // You can filter your executed SQL string here.
     *     }
     * });
     * </pre>
     * @param sqlStringFilter The filter of SQL string. (NullAllowed)
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
     * Clear the filter of SQL string from callback context on thread. <br>
     * If the callback context does not have other interfaces, the context is removed from thread.
     */
    public static void clearSqlStringFilterOnThread() {
        if (isExistCallbackContextOnThread()) {
            final CallbackContext context = getCallbackContextOnThread();
            context.setSqlStringFilter(null);
            clearIfNoInterface(context);
        }
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

    protected static void clearIfNoInterface(final CallbackContext context) {
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

    /**
     * Set the hook interface of behavior commands. (overriding existing hook as default) <br>
     * This hook interface is called back before executing behavior commands and finally. <br> 
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
     * <p>The hook methods may be called by nested process so you should pay attention to it when you implements this.</p>
     * <p>Also you can inherit the existing hook with your hook by overriding inheritsExistingHook().</p>
     * <pre>
     * context.setBehaviorCommandHook(new BehaviorCommandHook() {
     *     public void hookBefore(BehaviorCommandMeta meta) {
     *         ...
     *     }
     *     public void hookFinally(BehaviorCommandMeta meta, RuntimeException cause) {
     *         ...
     *     }
     *     public boolean inheritsExistingHook() {
     *         return true; // also existing hooks will be executed, 
     *     }
     * });
     * </pre>
     * @param behaviorCommandHook The hook interface of behavior commands. (NullAllowed, Inheritable)
     */
    public void setBehaviorCommandHook(BehaviorCommandHook behaviorCommandHook) {
        if (_behaviorCommandHook != null && behaviorCommandHook != null && behaviorCommandHook.inheritsExistingHook()) {
            _behaviorCommandHook = createInheritableBehaviorCommandHook(behaviorCommandHook);
        } else {
            _behaviorCommandHook = behaviorCommandHook;
        }
    }

    protected InheritableBehaviorCommandHook createInheritableBehaviorCommandHook(BehaviorCommandHook behaviorCommandHook) {
        return new InheritableBehaviorCommandHook(_behaviorCommandHook, behaviorCommandHook);
    }

    protected static class InheritableBehaviorCommandHook implements BehaviorCommandHook {

        protected final BehaviorCommandHook _originally; // might be null e.g. when first one
        protected final BehaviorCommandHook _yourHook;

        public InheritableBehaviorCommandHook(BehaviorCommandHook originally, BehaviorCommandHook yourHook) {
            _originally = originally;
            _yourHook = yourHook;
        }

        public void hookBefore(BehaviorCommandMeta meta) {
            if (_originally != null) {
                _originally.hookBefore(meta);
            }
            _yourHook.hookBefore(meta);
        }

        public void hookFinally(BehaviorCommandMeta meta, RuntimeException cause) {
            _yourHook.hookFinally(meta, cause);
            if (_originally != null) {
                _originally.hookFinally(meta, cause);
            }
        }
    }

    // -----------------------------------------------------
    //                                           SqlFireHook
    //                                           -----------
    public SqlFireHook getSqlFireHook() {
        return _sqlFireHook;
    }

    /**
     * Set the hook interface of SQL fires. (overriding existing hook as default) <br>
     * This hook interface is called back before firing SQL and finally. 
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
     * <p>The hook methods may be called by nested process so you should pay attention to it when you implements this.</p>
     * <p>Also you can inherit the existing hook with your hook by overriding inheritsExistingHook().</p>
     * <pre>
     * context.setSqlFireHook(new SqlFireHook() {
     *     public void hookBefore(BehaviorCommandMeta meta, SqlFireReadyInfo fireReadyInfo) {
     *         ...
     *     }
     *     public void hookFinally(BehaviorCommandMeta meta, SqlFireResultInfo fireResultInfo) {
     *         ...
     *     }
     *     public boolean inheritsExistingHook() {
     *         return true; // also existing hooks will be executed, 
     *     }
     * });
     * </pre>
     * @param sqlFireHook The hook interface of SQL fires. (NullAllowed, Inheritable)
     */
    public void setSqlFireHook(SqlFireHook sqlFireHook) {
        if (_sqlFireHook != null && sqlFireHook != null && sqlFireHook.inheritsExistingHook()) {
            _sqlFireHook = createInheritableSqlFireHook(sqlFireHook);
        } else {
            _sqlFireHook = sqlFireHook;
        }
    }

    protected InheritableSqlFireHook createInheritableSqlFireHook(SqlFireHook sqlFireHook) {
        return new InheritableSqlFireHook(_sqlFireHook, sqlFireHook);
    }

    protected static class InheritableSqlFireHook implements SqlFireHook {

        protected final SqlFireHook _originally; // might be null e.g. when first one
        protected final SqlFireHook _yourHook;

        public InheritableSqlFireHook(SqlFireHook originally, SqlFireHook yourHook) {
            _originally = originally;
            _yourHook = yourHook;
        }

        public void hookBefore(BehaviorCommandMeta meta, SqlFireReadyInfo fireReadyInfo) {
            if (_originally != null) {
                _originally.hookBefore(meta, fireReadyInfo);
            }
            _yourHook.hookBefore(meta, fireReadyInfo);
        }

        public void hookFinally(BehaviorCommandMeta meta, SqlFireResultInfo fireResultInfo) {
            _yourHook.hookFinally(meta, fireResultInfo);
            if (_originally != null) {
                _originally.hookFinally(meta, fireResultInfo);
            }
        }
    }

    // -----------------------------------------------------
    //                                         SqlLogHandler
    //                                         -------------
    public SqlLogHandler getSqlLogHandler() {
        return _sqlLogHandler;
    }

    /**
     * Set the handler of SQL log. <br>
     * This handler is called back before executing the SQL.
     * <pre>
     * context.setSqlLogHandler(new SqlLogHandler() {
     *     public void handle(String executedSql, String displaySql
     *                      , Object[] args, Class&lt;?&gt;[] argTypes) {
     *         // You can get your SQL string here.
     *     }
     * });
     * </pre>
     * @param sqlLogHandler The handler of SQL log. (NullAllowed, Inheritable)
     */
    public void setSqlLogHandler(SqlLogHandler sqlLogHandler) {
        if (_sqlLogHandler != null && sqlLogHandler != null && sqlLogHandler.inheritsExistingHandler()) {
            _sqlLogHandler = createInheritableSqlLogHandler(sqlLogHandler);
        } else {
            _sqlLogHandler = sqlLogHandler;
        }
    }

    protected InheritableSqlLogHandler createInheritableSqlLogHandler(SqlLogHandler sqlLogHandler) {
        return new InheritableSqlLogHandler(_sqlLogHandler, sqlLogHandler);
    }

    protected static class InheritableSqlLogHandler implements SqlLogHandler {

        protected final SqlLogHandler _originally; // might be null e.g. when first one
        protected final SqlLogHandler _yourHandler;

        public InheritableSqlLogHandler(SqlLogHandler originally, SqlLogHandler yourHandler) {
            _originally = originally;
            _yourHandler = yourHandler;
        }

        public void handle(SqlLogInfo info) {
            if (_originally != null) {
                _originally.handle(info);
            }
            _yourHandler.handle(info);
        }
    }

    // -----------------------------------------------------
    //                                      SqlResultHandler
    //                                      ----------------
    public SqlResultHandler getSqlResultHandler() {
        return _sqlResultHandler;
    }

    /**
     * Set the handler of SQL result. <br>
     * This handler is called back before executing the SQL. 
     * <pre>
     * context.setSqlResultHandler(new SqlResultHandler() {
     *     public void handle(SqlResultInfo info) {
     *         // You can get your SQL result information here.
     *     }
     * });
     * </pre>
     * @param sqlResultHandler The handler of SQL result. (NullAllowed, Inheritable)
     */
    public void setSqlResultHandler(SqlResultHandler sqlResultHandler) {
        if (_sqlResultHandler != null && sqlResultHandler != null && sqlResultHandler.inheritsExistingHandler()) {
            _sqlResultHandler = createInheritableSqlResultHandler(sqlResultHandler);
        } else {
            _sqlResultHandler = sqlResultHandler;
        }
    }

    protected InheritableSqlResultHandler createInheritableSqlResultHandler(SqlResultHandler sqlResultHandler) {
        return new InheritableSqlResultHandler(_sqlResultHandler, sqlResultHandler);
    }

    protected static class InheritableSqlResultHandler implements SqlResultHandler {

        protected final SqlResultHandler _originally; // might be null e.g. when first one
        protected final SqlResultHandler _yourHandler;

        public InheritableSqlResultHandler(SqlResultHandler originally, SqlResultHandler yourHandler) {
            _originally = originally;
            _yourHandler = yourHandler;
        }

        public void handle(SqlResultInfo info) {
            if (_originally != null) {
                _originally.handle(info);
            }
            _yourHandler.handle(info);
        }
    }

    // -----------------------------------------------------
    //                                       SqlStringFilter
    //                                       ---------------
    public SqlStringFilter getSqlStringFilter() {
        return _sqlStringFilter;
    }

    /**
     * Set the filter of SQL string. <br>
     * This filter is called back before executing the SQL. 
     * <pre>
     * context.setSqlStringFilter(new SqlStringFilter() {
     *     public String filterSelectCB(BehaviorCommandMeta meta, String executedSql) {
     *         // You can filter your SQL string here.
     *     }
     *     ...
     * });
     * </pre>
     * @param sqlStringFilter The filter of SQL string. (NullAllowed, Inheritable)
     */
    public void setSqlStringFilter(SqlStringFilter sqlStringFilter) {
        if (_sqlStringFilter != null && sqlStringFilter != null && sqlStringFilter.inheritsExistingFilter()) {
            _sqlStringFilter = createInheritableSqlStringFilter(sqlStringFilter);
        } else {
            _sqlStringFilter = sqlStringFilter;
        }
    }

    protected InheritableSqlStringFilter createInheritableSqlStringFilter(SqlStringFilter sqlStringFilter) {
        return new InheritableSqlStringFilter(_sqlStringFilter, sqlStringFilter);
    }

    protected static class InheritableSqlStringFilter implements SqlStringFilter {

        protected final SqlStringFilter _originally; // might be null e.g. when first one
        protected final SqlStringFilter _yourHandler;

        public InheritableSqlStringFilter(SqlStringFilter originally, SqlStringFilter yourHandler) {
            _originally = originally;
            _yourHandler = yourHandler;
        }

        @Override
        public String filterSelectCB(BehaviorCommandMeta meta, String executedSql) {
            final String first;
            if (_originally != null) {
                final String filtered = _originally.filterSelectCB(meta, executedSql);
                first = filtered != null ? filtered : executedSql;
            } else {
                first = executedSql;
            }
            final String second = _yourHandler.filterSelectCB(meta, first);
            return second != null ? second : first;
        }

        @Override
        public String filterEntityUpdate(BehaviorCommandMeta meta, String executedSql) {
            final String first;
            if (_originally != null) {
                final String filtered = _originally.filterEntityUpdate(meta, executedSql);
                first = filtered != null ? filtered : executedSql;
            } else {
                first = executedSql;
            }
            final String second = _yourHandler.filterEntityUpdate(meta, first);
            return second != null ? second : first;
        }

        @Override
        public String filterQueryUpdate(BehaviorCommandMeta meta, String executedSql) {
            final String first;
            if (_originally != null) {
                final String filtered = _originally.filterQueryUpdate(meta, executedSql);
                first = filtered != null ? filtered : executedSql;
            } else {
                first = executedSql;
            }
            final String second = _yourHandler.filterQueryUpdate(meta, first);
            return second != null ? second : first;
        }

        @Override
        public String filterOutsideSql(BehaviorCommandMeta meta, String executedSql) {
            final String first;
            if (_originally != null) {
                final String filtered = _originally.filterOutsideSql(meta, executedSql);
                first = filtered != null ? filtered : executedSql;
            } else {
                first = executedSql;
            }
            final String second = _yourHandler.filterOutsideSql(meta, first);
            return second != null ? second : first;
        }

        @Override
        public String filterProcedure(BehaviorCommandMeta meta, String executedSql) {
            final String first;
            if (_originally != null) {
                final String filtered = _originally.filterProcedure(meta, executedSql);
                first = filtered != null ? filtered : executedSql;
            } else {
                first = executedSql;
            }
            final String second = _yourHandler.filterProcedure(meta, first);
            return second != null ? second : first;
        }
    }
}
