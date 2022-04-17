/*
 * Copyright 2014-2022 the original author or authors.
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
import org.dbflute.unit.RuntimeTestCase;

/**
 * @author jflute
 * @since 1.2.0 (2019/04/06 Saturday)
 */
public class CallbackContextWholeTest extends RuntimeTestCase {

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        CallbackContext.clearCallbackContextOnThread(); // just in case
    }

    // ===================================================================================
    //                                                                 BehaviorCommandHook
    //                                                                 ===================
    public void test_BehaviorCommandHook_whole_basic() throws Exception {
        // ## Arrange ##

        // ## Act ##
        setupTwoBehaviorCommandHook();

        // ## Assert ##
        assertTwoBehaviorCommandHook(/*finalCallback*/true);
    }

    private void setupTwoBehaviorCommandHook() {
        CallbackContext.setBehaviorCommandHookOnThread(new BehaviorCommandHook() {
            public void hookBefore(BehaviorCommandMeta meta) {
                log("firstBefore");
                markHere("firstBefore");
            }

            public void hookFinally(BehaviorCommandMeta meta, RuntimeException cause) {
                log("firstFinally");
                markHere("firstFinally");
            }
        });
        CallbackContext.setBehaviorCommandHookOnThread(new BehaviorCommandHook() {
            public void hookBefore(BehaviorCommandMeta meta) {
                log("secondBefore");
                markHere("secondBefore");
            }

            public void hookFinally(BehaviorCommandMeta meta, RuntimeException cause) {
                log("secondFinally");
                markHere("secondFinally");
            }
        });
    }

    private void assertTwoBehaviorCommandHook(boolean finalCallback) {
        BehaviorCommandHook hook = CallbackContext.getCallbackContextOnThread().getBehaviorCommandHook();
        assertTrue(hook.inheritsExistingHook());
        hook.hookBefore(null);
        hook.hookFinally(null, null);
        assertMarked("firstBefore");
        assertMarked("secondBefore");
        assertMarked("secondFinally");
        assertMarked("firstFinally");

        assertEquals(hook, CallbackContext.getCallbackContextOnThread().getBehaviorCommandHook());
        CallbackContext.terminateLastBehaviorCommandHookOnThread();
        assertNotNull(CallbackContext.getCallbackContextOnThread().getBehaviorCommandHook());
        assertNotSame(hook, CallbackContext.getCallbackContextOnThread().getBehaviorCommandHook());

        CallbackContext.terminateLastBehaviorCommandHookOnThread();
        if (finalCallback) {
            assertNull(CallbackContext.getCallbackContextOnThread());
        } else {
            assertNull(CallbackContext.getCallbackContextOnThread().getBehaviorCommandHook());
        }
    }

    // -----------------------------------------------------
    //                                                 Clear
    //                                                 -----
    public void test_BehaviorCommandHook_whole_clear() throws Exception {
        // ## Arrange ##

        // ## Act ##
        setupTwoBehaviorCommandHook();

        // ## Assert ##
        BehaviorCommandHook hook = CallbackContext.getCallbackContextOnThread().getBehaviorCommandHook();
        assertTrue(hook.inheritsExistingHook());
        hook.hookBefore(null);
        hook.hookFinally(null, null);
        assertMarked("firstBefore");
        assertMarked("secondBefore");
        assertMarked("secondFinally");
        assertMarked("firstFinally");

        CallbackContext.clearBehaviorCommandHookOnThread();
        assertNull(CallbackContext.getCallbackContextOnThread());
    }

    // ===================================================================================
    //                                                                         SqlFireHook
    //                                                                         ===========
    public void test_SqlFireHook_whole_basic() {
        // ## Arrange ##

        // ## Act ##
        setupTwoSqlFireHook();

        // ## Assert ##
        assertTwoSqlFireHook(/*finalCallback*/true);
    }

    private void setupTwoSqlFireHook() {
        CallbackContext.setSqlFireHookOnThread(new SqlFireHook() {
            public void hookBefore(BehaviorCommandMeta meta, SqlFireReadyInfo fireReadyInfo) {
                markHere("firstBefore");
            }

            public void hookFinally(BehaviorCommandMeta meta, SqlFireResultInfo fireResultInfo) {
                markHere("firstFinally");
            }
        });
        CallbackContext.setSqlFireHookOnThread(new SqlFireHook() {
            public void hookBefore(BehaviorCommandMeta meta, SqlFireReadyInfo fireReadyInfo) {
                markHere("secondBefore");
            }

            public void hookFinally(BehaviorCommandMeta meta, SqlFireResultInfo fireResultInfo) {
                markHere("secondFinally");
            }
        });
    }

    private void assertTwoSqlFireHook(boolean finalCallback) {
        SqlFireHook hook = CallbackContext.getCallbackContextOnThread().getSqlFireHook();
        assertTrue(hook.inheritsExistingHook());
        hook.hookBefore(null, null);
        hook.hookFinally(null, null);
        assertMarked("firstBefore");
        assertMarked("secondBefore");
        assertMarked("secondFinally");
        assertMarked("firstFinally");

        assertEquals(hook, CallbackContext.getCallbackContextOnThread().getSqlFireHook());
        CallbackContext.terminateLastSqlFireHookOnThread();
        assertNotNull(CallbackContext.getCallbackContextOnThread().getSqlFireHook());
        assertNotSame(hook, CallbackContext.getCallbackContextOnThread().getSqlFireHook());

        CallbackContext.terminateLastSqlFireHookOnThread();
        if (finalCallback) {
            assertNull(CallbackContext.getCallbackContextOnThread());
        } else {
            assertNull(CallbackContext.getCallbackContextOnThread().getSqlFireHook());
        }
    }

    // ===================================================================================
    //                                                                       SqlLogHandler
    //                                                                       =============
    public void test_SqlLogHandler_whole_basic() {
        // ## Arrange ##

        // ## Act ##
        setupTwoSqlLogHandler();

        // ## Assert ##
        assertTwoSqlLogHandler(/*finalCallback*/true);
    }

    private void setupTwoSqlLogHandler() {
        CallbackContext.setSqlLogHandlerOnThread(new SqlLogHandler() {
            public void handle(SqlLogInfo info) {
                markHere("first");
            }
        });
        CallbackContext.setSqlLogHandlerOnThread(new SqlLogHandler() {
            public void handle(SqlLogInfo info) {
                markHere("second");
            }
        });
    }

    private void assertTwoSqlLogHandler(boolean finalCallback) {
        SqlLogHandler handler = CallbackContext.getCallbackContextOnThread().getSqlLogHandler();
        assertTrue(handler.inheritsExistingHandler());
        handler.handle(null);
        assertMarked("first");
        assertMarked("second");

        assertEquals(handler, CallbackContext.getCallbackContextOnThread().getSqlLogHandler());
        CallbackContext.terminateLastSqlLogHandlerOnThread();
        assertNotNull(CallbackContext.getCallbackContextOnThread().getSqlLogHandler());
        assertNotSame(handler, CallbackContext.getCallbackContextOnThread().getSqlLogHandler());

        CallbackContext.terminateLastSqlLogHandlerOnThread();
        if (finalCallback) {
            assertNull(CallbackContext.getCallbackContextOnThread());
        } else {
            assertNull(CallbackContext.getCallbackContextOnThread().getSqlLogHandler());
        }
    }

    // ===================================================================================
    //                                                                    SqlResultHandler
    //                                                                    ================
    public void test_SqlResultHandler_whole_basic() {
        // ## Arrange ##

        // ## Act ##
        setupTwoSqlResultHandler();

        // ## Assert ##
        assertTwoSqlResultHandler(/*finalCallback*/true);
    }

    private void setupTwoSqlResultHandler() {
        CallbackContext.setSqlResultHandlerOnThread(new SqlResultHandler() {
            public void handle(SqlResultInfo info) {
                markHere("first");
            }
        });
        CallbackContext.setSqlResultHandlerOnThread(new SqlResultHandler() {
            public void handle(SqlResultInfo info) {
                markHere("second");
            }
        });
    }

    private void assertTwoSqlResultHandler(boolean finalCallback) {
        SqlResultHandler handler = CallbackContext.getCallbackContextOnThread().getSqlResultHandler();
        assertTrue(handler.inheritsExistingHandler());
        handler.handle(null);
        assertMarked("first");
        assertMarked("second");

        assertEquals(handler, CallbackContext.getCallbackContextOnThread().getSqlResultHandler());
        CallbackContext.terminateLastSqlResultHandlerOnThread();
        assertNotNull(CallbackContext.getCallbackContextOnThread().getSqlResultHandler());
        assertNotSame(handler, CallbackContext.getCallbackContextOnThread().getSqlResultHandler());

        CallbackContext.terminateLastSqlResultHandlerOnThread();
        if (finalCallback) {
            assertNull(CallbackContext.getCallbackContextOnThread());
        } else {
            assertNull(CallbackContext.getCallbackContextOnThread().getSqlResultHandler());
        }
    }

    // ===================================================================================
    //                                                                     SqlStringFilter
    //                                                                     ===============
    public void test_SqlStringFilter_whole_basic() {
        // ## Arrange ##

        // ## Act ##
        setupTwoSqlStringFilter();

        // ## Assert ##
        assertTwoSqlStringFilter(/*finalCallback*/true);
    }

    private void assertTwoSqlStringFilter(boolean finalCallback) {
        SqlStringFilter filter = CallbackContext.getCallbackContextOnThread().getSqlStringFilter();
        assertTrue(filter.inheritsExistingFilter());
        filter.filterSelectCB(null, null);
        assertMarked("first");
        assertMarked("second");

        assertEquals(filter, CallbackContext.getCallbackContextOnThread().getSqlStringFilter());
        CallbackContext.terminateLastSqlStringFilterOnThread();
        assertNotNull(CallbackContext.getCallbackContextOnThread().getSqlStringFilter());
        assertNotSame(filter, CallbackContext.getCallbackContextOnThread().getSqlStringFilter());

        CallbackContext.terminateLastSqlStringFilterOnThread();
        if (finalCallback) {
            assertNull(CallbackContext.getCallbackContextOnThread());
        } else {
            assertNull(CallbackContext.getCallbackContextOnThread().getSqlStringFilter());
        }
    }

    private void setupTwoSqlStringFilter() {
        CallbackContext.setSqlStringFilterOnThread(new SqlStringFilter() {
            public String filterSelectCB(BehaviorCommandMeta meta, String executedSql) {
                markHere("first");
                return SqlStringFilter.super.filterSelectCB(meta, executedSql);
            }
        });
        CallbackContext.setSqlStringFilterOnThread(new SqlStringFilter() {
            public String filterSelectCB(BehaviorCommandMeta meta, String executedSql) {
                markHere("second");
                return SqlStringFilter.super.filterSelectCB(meta, executedSql);
            }
        });
    }

    // ===================================================================================
    //                                                                               Mixed
    //                                                                               =====
    public void test_mixed_basic() {
        // ## Arrange ##

        // ## Act ##
        setupTwoBehaviorCommandHook();
        setupTwoSqlFireHook();
        setupTwoSqlLogHandler();
        setupTwoSqlResultHandler();
        setupTwoSqlStringFilter();

        // ## Assert ##
        assertTwoBehaviorCommandHook(/*finalCallback*/false);
        assertTwoSqlFireHook(/*finalCallback*/false);
        assertTwoSqlLogHandler(/*finalCallback*/false);
        assertTwoSqlResultHandler(/*finalCallback*/false);
        assertTwoSqlStringFilter(/*finalCallback*/true); // is final
    }
}
