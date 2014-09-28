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

import java.sql.Timestamp;
import java.util.Date;

import org.seasar.dbflute.AccessContext.AccessContextHolder;
import org.seasar.dbflute.AccessContext.AccessDateProvider;
import org.seasar.dbflute.AccessContext.AccessModuleProvider;
import org.seasar.dbflute.AccessContext.AccessProcessProvider;
import org.seasar.dbflute.AccessContext.AccessTimestampProvider;
import org.seasar.dbflute.AccessContext.AccessUserProvider;
import org.seasar.dbflute.exception.AccessContextNoValueException;
import org.seasar.dbflute.exception.AccessContextNotFoundException;
import org.seasar.dbflute.unit.core.PlainTestCase;

/**
 * @author jflute
 */
public class AccessContextTest extends PlainTestCase {

    // ===================================================================================
    //                                                                             Setting
    //                                                                             =======
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        AccessContext.clearAccessContextOnThread();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        AccessContext.clearAccessContextOnThread();
    }

    // ===================================================================================
    //                                                                      Basic Handling
    //                                                                      ==============
    public void test_getValue_whenAccessContextExists_Tx() throws Exception {
        // ## Arrange ##
        AccessContext accessContext = new AccessContext();
        Date currentDate = currentDate();
        accessContext.setAccessDate(currentDate);
        Timestamp currentTimestamp = currentTimestamp();
        accessContext.setAccessTimestamp(currentTimestamp);
        accessContext.setAccessUser("accessUser");
        accessContext.setAccessProcess("accessProcess");
        accessContext.setAccessModule("accessModule");
        accessContext.registerAccessValue("foo", "bar");
        AccessContext.setAccessContextOnThread(accessContext);

        // ## Act & Assert ##
        assertEquals(currentDate, AccessContext.getAccessDateOnThread());
        assertEquals(currentTimestamp, AccessContext.getAccessTimestampOnThread());
        assertEquals("accessUser", AccessContext.getAccessUserOnThread());
        assertEquals("accessProcess", AccessContext.getAccessProcessOnThread());
        assertEquals("accessModule", AccessContext.getAccessModuleOnThread());
        assertEquals("bar", AccessContext.getAccessValueOnThread("foo"));
    }

    public void test_getValue_whenAccessContextProvider_Tx() throws Exception {
        // ## Arrange ##
        AccessContext accessContext = new AccessContext();
        final String dateExp = "2013-02-02 12:34:56";
        accessContext.setAccessDateProvider(new AccessDateProvider() {
            public Date getAccessDate() {
                return toDate(dateExp);
            }
        });
        final String timestampExp = "2013-02-02 12:34:56";
        accessContext.setAccessTimestampProvider(new AccessTimestampProvider() {
            public Timestamp getAccessTimestamp() {
                return toTimestamp(timestampExp);
            }
        });
        accessContext.setAccessUserProvider(new AccessUserProvider() {
            public String getAccessUser() {
                return "foo";
            }
        });
        accessContext.setAccessProcessProvider(new AccessProcessProvider() {
            public String getAccessProcess() {
                return "bar";
            }
        });
        accessContext.setAccessModuleProvider(new AccessModuleProvider() {
            public String getAccessModule() {
                return "qux";
            }
        });
        AccessContext.setAccessContextOnThread(accessContext);

        // ## Act & Assert ##
        assertNotNull(dateExp, AccessContext.getAccessDateOnThread());
        assertNotNull(timestampExp, AccessContext.getAccessTimestampOnThread());
        assertEquals("foo", AccessContext.getAccessUserOnThread());
        assertEquals("bar", AccessContext.getAccessProcessOnThread());
        assertEquals("qux", AccessContext.getAccessModuleOnThread());
    }

    public void test_getValue_whenAccessContextNotFound_Tx() throws Exception {
        try {
            AccessContext.getAccessUserOnThread();
            fail();
        } catch (AccessContextNotFoundException e) {
            // OK
            log(e.getMessage());
        }
        try {
            AccessContext.getAccessProcessOnThread();
            fail();
        } catch (AccessContextNotFoundException e) {
            // OK
            log(e.getMessage());
        }
        try {
            AccessContext.getAccessModuleOnThread();
            fail();
        } catch (AccessContextNotFoundException e) {
            // OK
            log(e.getMessage());
        }
        assertNotNull(AccessContext.getAccessDateOnThread());
        assertNotNull(AccessContext.getAccessTimestampOnThread());
        try {
            AccessContext.getAccessValueOnThread("foo");
            fail();
        } catch (AccessContextNotFoundException e) {
            // OK
            log(e.getMessage());
        }
    }

    public void test_getValue_whenAccessContextEmpty_Tx() throws Exception {
        AccessContext.setAccessContextOnThread(new AccessContext());
        try {
            AccessContext.getAccessUserOnThread();
            fail();
        } catch (AccessContextNoValueException e) {
            // OK
            log(e.getMessage());
        }
        try {
            AccessContext.getAccessProcessOnThread();
            fail();
        } catch (AccessContextNoValueException e) {
            // OK
            log(e.getMessage());
        }
        try {
            AccessContext.getAccessModuleOnThread();
            fail();
        } catch (AccessContextNoValueException e) {
            // OK
            log(e.getMessage());
        }
        assertNotNull(AccessContext.getAccessDateOnThread());
        assertNotNull(AccessContext.getAccessTimestampOnThread());
        try {
            AccessContext.getAccessValueOnThread("foo");
            fail();
        } catch (AccessContextNoValueException e) {
            // OK
            log(e.getMessage());
        }
    }

    // ===================================================================================
    //                                                                          Management
    //                                                                          ==========
    public void test_useThreadLocalProvider_basic() throws Exception {
        // ## Arrange ##
        // ## Act ##
        assertTrue(AccessContext.isLocked());
        AccessContext.unlock();
        assertFalse(AccessContext.isLocked());
        AccessContextHolder holder = new AccessContextHolder() {

            private AccessContext accessContext;

            public AccessContext provide() {
                markHere("get()");
                return accessContext;
            }

            public void save(AccessContext accessContext) {
                markHere("set()");
                this.accessContext = accessContext;
            }
        };
        AccessContext.useSurrogateHolder(holder);

        // ## Assert ##
        assertTrue(AccessContext.isLocked());
        AccessContext context = new AccessContext();
        context.setAccessUser("foo");
        AccessContext.setAccessContextOnThread(context);
        AccessContext actual = AccessContext.getAccessContextOnThread();
        assertEquals(context, actual);
        assertMarked("get()");
        assertMarked("set()");

        assertNotNull(holder.provide());
        assertMarked("get()");
        holder.save(null);
        assertMarked("set()");
        assertNull(holder.provide());
        assertMarked("get()");

        AccessContext.unlock();
        AccessContext.useSurrogateHolder(null); // to suppress mark when tearDown()
    }

    public void test_useThreadLocalProvider_locked() throws Exception {
        try {
            assertTrue(AccessContext.isLocked());
            AccessContext.useSurrogateHolder(new AccessContextHolder() {

                private AccessContext accessContext;

                public AccessContext provide() {
                    markHere("set()");
                    return accessContext;
                }

                public void save(AccessContext accessContext) {
                    markHere("get()");
                    this.accessContext = accessContext;
                }
            });
            fail();
        } catch (IllegalStateException e) {
            log(e.getMessage());
        } finally {
            assertTrue(AccessContext.isLocked());
        }
    }
}
