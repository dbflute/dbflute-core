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
package org.dbflute.hook;

import org.dbflute.bhv.core.BehaviorCommandHook;
import org.dbflute.bhv.core.BehaviorCommandMeta;
import org.dbflute.hook.CallbackContext.CallbackContextHolder;
import org.dbflute.unit.RuntimeTestCase;

/**
 * @author jflute
 * @since 1.0.5F (2014/05/05 Monday)
 */
public class CallbackContextTest extends RuntimeTestCase {

    // ===================================================================================
    //                                                                 BehaviorCommandHook
    //                                                                 ===================
    public void test_BehaviorCommandHook_twiceSet_default() throws Exception {
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
                log("2");
                markHere("secondBefore");
            }

            public void hookFinally(BehaviorCommandMeta meta, RuntimeException cause) {
                log("3");
                markHere("secondFinally");
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

    public void test_BehaviorCommandHook_twiceSet_inherits() throws Exception {
        // ## Arrange ##
        CallbackContext context = new CallbackContext();
        assertNull(context.getBehaviorCommandHook());

        // ## Act ##
        context.setBehaviorCommandHook(new BehaviorCommandHook() {
            public void hookBefore(BehaviorCommandMeta meta) {
                log("1");
                markHere("firstBefore");
            }

            public void hookFinally(BehaviorCommandMeta meta, RuntimeException cause) {
                log("4");
                markHere("firstFinally");
            }
        });
        context.setBehaviorCommandHook(new BehaviorCommandHook() {
            public void hookBefore(BehaviorCommandMeta meta) {
                log("2");
                markHere("secondBefore");
            }

            public void hookFinally(BehaviorCommandMeta meta, RuntimeException cause) {
                log("3");
                markHere("secondFinally");
            }

            @Override
            public boolean inheritsExistingHook() {
                return true;
            }
        });

        // ## Assert ##
        BehaviorCommandHook hook = context.getBehaviorCommandHook();
        assertFalse(hook.inheritsExistingHook());
        hook.hookBefore(null);
        hook.hookFinally(null, null);
        assertMarked("firstBefore");
        assertMarked("secondBefore");
        assertMarked("secondFinally");
        assertMarked("firstFinally");
    }

    // ===================================================================================
    //                                                                         SqlFireHook
    //                                                                         ===========
    public void test_SqlFireHook_twiceSet_default() throws Exception {
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
                log("2");
                markHere("secondBefore");
            }

            public void hookFinally(BehaviorCommandMeta meta, SqlFireResultInfo fireResultInfo) {
                log("3");
                markHere("secondFinally");
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

    public void test_SqlFireHook_twiceSet_inherits() throws Exception {
        // ## Arrange ##
        CallbackContext context = new CallbackContext();
        assertNull(context.getSqlFireHook());

        // ## Act ##
        context.setSqlFireHook(new SqlFireHook() {
            public void hookBefore(BehaviorCommandMeta meta, SqlFireReadyInfo fireReadyInfo) {
                log("1");
                markHere("firstBefore");
            }

            public void hookFinally(BehaviorCommandMeta meta, SqlFireResultInfo fireResultInfo) {
                log("4");
                markHere("firstFinally");
            }
        });
        context.setSqlFireHook(new SqlFireHook() {
            public void hookBefore(BehaviorCommandMeta meta, SqlFireReadyInfo fireReadyInfo) {
                log("2");
                markHere("secondBefore");
            }

            public void hookFinally(BehaviorCommandMeta meta, SqlFireResultInfo fireResultInfo) {
                log("3");
                markHere("secondFinally");
            }

            @Override
            public boolean inheritsExistingHook() {
                return true;
            }
        });

        // ## Assert ##
        SqlFireHook hook = context.getSqlFireHook();
        assertFalse(hook.inheritsExistingHook());
        hook.hookBefore(null, null);
        hook.hookFinally(null, null);
        assertMarked("firstBefore");
        assertMarked("secondBefore");
        assertMarked("secondFinally");
        assertMarked("firstFinally");
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
