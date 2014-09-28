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
package org.seasar.dbflute;

import org.seasar.dbflute.CallbackContext.CallbackContextHolder;
import org.seasar.dbflute.jdbc.SqlLogHandler;
import org.seasar.dbflute.jdbc.SqlLogInfo;
import org.seasar.dbflute.unit.core.PlainTestCase;

/**
 * @author jflute
 * @since 1.0.5F (2014/05/05 Monday)
 */
public class CallbackContextTest extends PlainTestCase {

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
