/*
 * Copyright 2014-2025 the original author or authors.
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
import org.dbflute.hook.CallbackContext.CallbackContextHolder;
import org.dbflute.hook.CallbackContext.InheritableBehaviorCommandHook;
import org.dbflute.unit.RuntimeTestCase;

/**
 * @author jflute
 * @since 1.0.5F (2014/05/05 Monday)
 */
public class CallbackContextTest extends RuntimeTestCase {

    @Override
    protected void markHere(String mark) {
        super.markHere(mark);
        log("...Marking {}", mark); // for visual check
    }

    // ===================================================================================
    //                                                                 BehaviorCommandHook
    //                                                                 ===================
    // -----------------------------------------------------
    //                                               Two Set
    //                                               -------
    public void test_BehaviorCommandHook_twoSet_inherits() throws Exception {
        // ## Arrange ##
        CallbackContext context = new CallbackContext();
        assertNull(context.getBehaviorCommandHook());

        // ## Act ##
        setupTwoBehaviorCommandHook(context);

        // ## Assert ##
        BehaviorCommandHook hook = context.getBehaviorCommandHook();
        assertTrue(hook.inheritsExistingHook());
        hook.hookBefore(null);
        hook.hookFinally(null, null);
        assertMarked("firstBefore");
        assertMarked("secondBefore");
        assertMarked("secondFinally");
        assertMarked("firstFinally");
        context.terminateLastBehaviorCommandHook();
        assertNotNull(context.getBehaviorCommandHook());
        context.terminateLastBehaviorCommandHook();
        assertNull(context.getBehaviorCommandHook());
    }

    public void test_BehaviorCommandHook_twoSet_terminateLast() throws Exception {
        // ## Arrange ##
        CallbackContext context = new CallbackContext();
        assertNull(context.getBehaviorCommandHook());
        setupTwoBehaviorCommandHook(context);
        assertNotNull(context.getBehaviorCommandHook());
        assertTrue(context.getBehaviorCommandHook() instanceof InheritableBehaviorCommandHook);
        assertEquals(2, ((InheritableBehaviorCommandHook) context.getBehaviorCommandHook()).countManagedHook());

        // ## Act ##
        context.terminateLastBehaviorCommandHook();

        // ## Assert ##
        BehaviorCommandHook hook = context.getBehaviorCommandHook();
        assertNotNull(hook);
        hook.hookBefore(null);
        hook.hookFinally(null, null);
        assertMarked("firstBefore");
        assertMarked("firstFinally");
        assertFalse(context.getBehaviorCommandHook() instanceof InheritableBehaviorCommandHook);
    }

    // -----------------------------------------------------
    //                                             Three Set
    //                                             ---------
    public void test_BehaviorCommandHook_threeSet_inherits() throws Exception {
        // ## Arrange ##
        CallbackContext context = new CallbackContext();
        assertNull(context.getBehaviorCommandHook());

        // ## Act ##
        setupThreeBehaviorCommandHook(context);

        // ## Assert ##
        BehaviorCommandHook hook = context.getBehaviorCommandHook();
        assertTrue(hook.inheritsExistingHook());
        hook.hookBefore(null);
        hook.hookFinally(null, null);
        assertMarked("firstBefore");
        assertMarked("secondBefore");
        assertMarked("thirdBefore");
        assertMarked("thirdFinally");
        assertMarked("secondFinally");
        assertMarked("firstFinally");
    }

    public void test_BehaviorCommandHook_threeSet_terminateLast() throws Exception {
        // ## Arrange ##
        CallbackContext context = new CallbackContext();
        assertNull(context.getBehaviorCommandHook());
        setupThreeBehaviorCommandHook(context);
        assertNotNull(context.getBehaviorCommandHook());
        assertTrue(context.getBehaviorCommandHook() instanceof InheritableBehaviorCommandHook);
        assertEquals(3, ((InheritableBehaviorCommandHook) context.getBehaviorCommandHook()).countManagedHook());

        // ## Act ##
        context.terminateLastBehaviorCommandHook();

        // ## Assert ##
        BehaviorCommandHook hook = context.getBehaviorCommandHook();
        assertNotNull(hook);
        hook.hookBefore(null);
        hook.hookFinally(null, null);
        assertMarked("firstBefore");
        assertMarked("secondBefore");
        assertMarked("secondFinally");
        assertMarked("firstFinally");
        assertTrue(context.getBehaviorCommandHook() instanceof InheritableBehaviorCommandHook);
    }

    private void setupTwoBehaviorCommandHook(CallbackContext context) {
        context.setBehaviorCommandHook(new BehaviorCommandHook() {
            public void hookBefore(BehaviorCommandMeta meta) {
                markHere("firstBefore");
            }

            public void hookFinally(BehaviorCommandMeta meta, RuntimeException cause) {
                markHere("firstFinally");
            }
        });
        context.setBehaviorCommandHook(new BehaviorCommandHook() {
            public void hookBefore(BehaviorCommandMeta meta) {
                markHere("secondBefore");
            }

            public void hookFinally(BehaviorCommandMeta meta, RuntimeException cause) {
                markHere("secondFinally");
            }
        });
    }

    private void setupThreeBehaviorCommandHook(CallbackContext context) {
        setupTwoBehaviorCommandHook(context);
        context.setBehaviorCommandHook(new BehaviorCommandHook() {
            public void hookBefore(BehaviorCommandMeta meta) {
                markHere("thirdBefore");
            }

            public void hookFinally(BehaviorCommandMeta meta, RuntimeException cause) {
                markHere("thirdFinally");
            }
        });
    }

    // -----------------------------------------------------
    //                                           No Inherits
    //                                           -----------
    public void test_BehaviorCommandHook_twoSet_noInherits() throws Exception {
        // ## Arrange ##
        CallbackContext context = new CallbackContext();
        assertNull(context.getBehaviorCommandHook());

        // ## Act ##
        context.setBehaviorCommandHook(new BehaviorCommandHook() {
            public void hookBefore(BehaviorCommandMeta meta) {
                fail();
            }

            public void hookFinally(BehaviorCommandMeta meta, RuntimeException cause) {
                fail();
            }
        });
        context.setBehaviorCommandHook(new BehaviorCommandHook() {
            public void hookBefore(BehaviorCommandMeta meta) {
                markHere("secondBefore");
            }

            public void hookFinally(BehaviorCommandMeta meta, RuntimeException cause) {
                markHere("secondFinally");
            }

            @Override
            public boolean inheritsExistingHook() {
                return false;
            }
        });

        // ## Assert ##
        BehaviorCommandHook hook = context.getBehaviorCommandHook();
        assertFalse(hook.inheritsExistingHook());
        hook.hookBefore(null);
        hook.hookFinally(null, null);
        assertMarked("secondBefore");
        assertMarked("secondFinally");
    }

    // ===================================================================================
    //                                                                         SqlFireHook
    //                                                                         ===========
    public void test_SqlFireHook_twoSet_inherits() throws Exception {
        // ## Arrange ##
        CallbackContext context = new CallbackContext();
        assertNull(context.getSqlFireHook());

        // ## Act ##
        context.setSqlFireHook(new SqlFireHook() {
            public void hookBefore(BehaviorCommandMeta meta, SqlFireReadyInfo fireReadyInfo) {
                markHere("firstBefore");
            }

            public void hookFinally(BehaviorCommandMeta meta, SqlFireResultInfo fireResultInfo) {
                markHere("firstFinally");
            }
        });
        context.setSqlFireHook(new SqlFireHook() {
            public void hookBefore(BehaviorCommandMeta meta, SqlFireReadyInfo fireReadyInfo) {
                markHere("secondBefore");
            }

            public void hookFinally(BehaviorCommandMeta meta, SqlFireResultInfo fireResultInfo) {
                markHere("secondFinally");
            }
        });

        // ## Assert ##
        SqlFireHook hook = context.getSqlFireHook();
        assertTrue(hook.inheritsExistingHook());
        hook.hookBefore(null, null);
        hook.hookFinally(null, null);
        assertMarked("firstBefore");
        assertMarked("secondBefore");
        assertMarked("secondFinally");
        assertMarked("firstFinally");
        context.terminateLastSqlFireHook();
        assertNotNull(context.getSqlFireHook());
        context.terminateLastSqlFireHook();
        assertNull(context.getSqlFireHook());
    }

    public void test_SqlFireHook_twoSet_noInherits() throws Exception {
        // ## Arrange ##
        CallbackContext context = new CallbackContext();
        assertNull(context.getSqlFireHook());

        // ## Act ##
        context.setSqlFireHook(new SqlFireHook() {
            public void hookBefore(BehaviorCommandMeta meta, SqlFireReadyInfo fireReadyInfo) {
                fail();
            }

            public void hookFinally(BehaviorCommandMeta meta, SqlFireResultInfo fireResultInfo) {
                fail();
            }
        });
        context.setSqlFireHook(new SqlFireHook() {
            public void hookBefore(BehaviorCommandMeta meta, SqlFireReadyInfo fireReadyInfo) {
                markHere("secondBefore");
            }

            public void hookFinally(BehaviorCommandMeta meta, SqlFireResultInfo fireResultInfo) {
                markHere("secondFinally");
            }

            @Override
            public boolean inheritsExistingHook() {
                return false;
            }
        });

        // ## Assert ##
        SqlFireHook hook = context.getSqlFireHook();
        assertFalse(hook.inheritsExistingHook());
        hook.hookBefore(null, null);
        hook.hookFinally(null, null);
        assertMarked("secondBefore");
        assertMarked("secondFinally");
    }

    // ===================================================================================
    //                                                                       SqlLogHandler
    //                                                                       =============
    public void test_SqlLogHandler_twoSet_noInherits() throws Exception {
        // ## Arrange ##
        CallbackContext context = new CallbackContext();
        assertNull(context.getSqlLogHandler());

        // ## Act ##
        context.setSqlLogHandler(new SqlLogHandler() {
            public void handle(SqlLogInfo info) {
                markHere("first");
            }
        });
        context.setSqlLogHandler(new SqlLogHandler() {
            public void handle(SqlLogInfo info) {
                markHere("second");
            }
        });

        // ## Assert ##
        SqlLogHandler handler = context.getSqlLogHandler();
        assertTrue(handler.inheritsExistingHandler());
        handler.handle(null);
        assertMarked("first");
        assertMarked("second");
        context.terminateLastSqlLogHandler();
        assertNotNull(context.getSqlLogHandler());
        context.terminateLastSqlLogHandler();
        assertNull(context.getSqlLogHandler());
    }

    // ===================================================================================
    //                                                                    SqlResultHandler
    //                                                                    ================
    public void test_SqlResultHandler_twoSet_noInherits() throws Exception {
        // ## Arrange ##
        CallbackContext context = new CallbackContext();
        assertNull(context.getSqlResultHandler());

        // ## Act ##
        context.setSqlResultHandler(new SqlResultHandler() {
            public void handle(SqlResultInfo info) {
                markHere("first");
            }
        });
        context.setSqlResultHandler(new SqlResultHandler() {
            public void handle(SqlResultInfo info) {
                markHere("second");
            }
        });

        // ## Assert ##
        SqlResultHandler handler = context.getSqlResultHandler();
        assertTrue(handler.inheritsExistingHandler());
        handler.handle(null);
        assertMarked("first");
        assertMarked("second");
        context.terminateLastSqlResultHandler();
        assertNotNull(context.getSqlResultHandler());
        context.terminateLastSqlResultHandler();
        assertNull(context.getSqlResultHandler());
    }

    // ===================================================================================
    //                                                                     SqlStringFilter
    //                                                                     ===============
    public void test_SqlStringFilter_twoSet_inherits() throws Exception {
        // ## Arrange ##
        CallbackContext context = new CallbackContext();
        assertNull(context.getSqlFireHook());
        context.setSqlStringFilter(new SqlStringFilter() {
            @Override
            public String filterSelectCB(BehaviorCommandMeta meta, String executedSql) {
                return executedSql + ":first";
            }

            @Override
            public String filterEntityUpdate(BehaviorCommandMeta meta, String executedSql) {
                return executedSql + ":first";
            }
        });
        context.setSqlStringFilter(new SqlStringFilter() {
            @Override
            public String filterSelectCB(BehaviorCommandMeta meta, String executedSql) {
                return executedSql + ":second";
            }

            @Override
            public String filterQueryUpdate(BehaviorCommandMeta meta, String executedSql) {
                return executedSql + ":second";
            }
        });

        // ## Act ##
        // ## Assert ##
        assertEquals("base:first:second", context.getSqlStringFilter().filterSelectCB(null, "base"));
        assertEquals("base:first", context.getSqlStringFilter().filterEntityUpdate(null, "base"));
        assertEquals("base:second", context.getSqlStringFilter().filterQueryUpdate(null, "base"));
        assertEquals("base", context.getSqlStringFilter().filterOutsideSql(null, "base"));
        assertNull(context.getSqlStringFilter().filterOutsideSql(null, null));
        context.terminateLastSqlStringFilter();
        assertNotNull(context.getSqlStringFilter());
        context.terminateLastSqlStringFilter();
        assertNull(context.getSqlStringFilter());
    }

    // ===================================================================================
    //                                                                          Management
    //                                                                          ==========
    public void test_useThreadLocalProvider_basic() throws Exception {
        // ## Arrange ##
        // ## Act ##
        assertTrue(CallbackContext.isLocked());
        CallbackContext.unlock();
        assertFalse(CallbackContext.isLocked());
        CallbackContextHolder holder = new CallbackContextHolder() {

            private CallbackContext context;

            public void save(CallbackContext context) {
                markHere("set()");
                this.context = context;
            }

            public CallbackContext provide() {
                markHere("get()");
                return context;
            }
        };
        CallbackContext.useSurrogateHolder(holder);

        // ## Assert ##
        assertTrue(CallbackContext.isLocked());
        CallbackContext context = new CallbackContext();
        context.setSqlLogHandler(new SqlLogHandler() {
            public void handle(SqlLogInfo info) {
            }
        });
        CallbackContext.setCallbackContextOnThread(context);
        CallbackContext actual = CallbackContext.getCallbackContextOnThread();
        assertEquals(context, actual);
        assertMarked("get()");
        assertMarked("set()");

        assertNotNull(holder.provide());
        assertMarked("get()");
        holder.save(null);
        assertMarked("set()");
        assertNull(holder.provide());
        assertMarked("get()");

        CallbackContext.unlock();
        CallbackContext.useSurrogateHolder(null); // to suppress mark when tearDown()
    }

    public void test_useThreadLocalProvider_locked() throws Exception {
        try {
            assertTrue(CallbackContext.isLocked());
            CallbackContext.useSurrogateHolder(new CallbackContextHolder() {

                private CallbackContext context;

                public void save(CallbackContext context) {
                    markHere("get()");
                    this.context = context;
                }

                public CallbackContext provide() {
                    markHere("set()");
                    return context;
                }
            });
            fail();
        } catch (IllegalStateException e) {
            log(e.getMessage());
        } finally {
            assertTrue(CallbackContext.isLocked());
        }
    }
}
