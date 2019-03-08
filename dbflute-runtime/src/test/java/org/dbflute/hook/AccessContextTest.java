/*
 * Copyright 2014-2019 the original author or authors.
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

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import org.dbflute.exception.AccessContextNoValueException;
import org.dbflute.exception.AccessContextNotFoundException;
import org.dbflute.hook.AccessContext.AccessContextHolder;
import org.dbflute.hook.AccessContext.AccessDateProvider;
import org.dbflute.hook.AccessContext.AccessModuleProvider;
import org.dbflute.hook.AccessContext.AccessProcessProvider;
import org.dbflute.hook.AccessContext.AccessTimestampProvider;
import org.dbflute.hook.AccessContext.AccessUserProvider;
import org.dbflute.unit.RuntimeTestCase;

/**
 * @author jflute
 */
public class AccessContextTest extends RuntimeTestCase {

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
            public Date provideDate() {
                return toDate(dateExp);
            }
        });
        final String timestampExp = "2013-02-02 12:34:56";
        accessContext.setAccessTimestampProvider(new AccessTimestampProvider() {
            public Timestamp provideTimestamp() {
                return toTimestamp(timestampExp);
            }
        });
        accessContext.setAccessUserProvider(new AccessUserProvider() {
            public String provideUser() {
                return "foo";
            }
        });
        accessContext.setAccessProcessProvider(new AccessProcessProvider() {
            public String provideProcess() {
                return "bar";
            }
        });
        accessContext.setAccessModuleProvider(new AccessModuleProvider() {
            public String provideModule() {
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
    //                                                                      Basic Override
    //                                                                      ==============
    public void test_toString_basic() throws Exception {
        // ## Arrange ##
        AccessContext accessContext = new AccessContext();
        Date currentDate = toDate("2015/01/18 23:47:22.123");
        accessContext.setAccessDate(currentDate);
        Timestamp currentTimestamp = toTimestamp("2015/01/18 23:48:52.456");
        accessContext.setAccessTimestamp(currentTimestamp);
        accessContext.setAccessUser("accessUser");
        accessContext.setAccessProcess("accessProcess");
        accessContext.setAccessModule("accessModule");
        accessContext.registerAccessValue("foo", "bar");

        // ## Act ##
        String str = accessContext.toString();

        // ## Assert ##
        log(str);
        StringBuilder sb = new StringBuilder();
        sb.append("AccessContext:{");
        sb.append("date=2015/01/18, timestamp=2015/01/18 23:48:52.456");
        sb.append(", user=accessUser, process=accessProcess, module=accessModule, valueMap={foo=bar}}");
        assertEquals(sb.toString(), str);
    }

    public void test_toString_localDate() throws Exception {
        // ## Arrange ##
        AccessContext accessContext = new AccessContext();
        LocalDate currentDate = toLocalDate("2015/01/18 23:47:22.123");
        accessContext.setAccessLocalDate(currentDate);
        LocalDateTime currentTimestamp = toLocalDateTime("2015/01/18 23:48:52.456");
        accessContext.setAccessLocalDateTime(currentTimestamp);

        // ## Act ##
        String str = accessContext.toString();

        // ## Assert ##
        log(str);
        assertEquals("AccessContext:{localDate=2015-01-18, localDateTime=2015-01-18T23:48:52.456}", str);
    }

    public void test_toString_provider() throws Exception {
        // ## Arrange ##
        AccessContext accessContext = new AccessContext();
        accessContext.setAccessLocalDateProvider(() -> null);
        accessContext.setAccessLocalDateTimeProvider(() -> null);
        accessContext.setAccessDateProvider(() -> null);
        accessContext.setAccessTimestampProvider(() -> null);
        accessContext.setAccessUserProvider(() -> null);
        accessContext.setAccessProcessProvider(() -> null);
        accessContext.setAccessModuleProvider(() -> null);

        // ## Act ##
        String str = accessContext.toString();

        // ## Assert ##
        log(str);
        assertContainsAll(str, "localDateProvider", "localDateTimeProvider", "dateProvider", "timestampProvider");
        assertContainsAll(str, "userProvider", "processProvider", "moduleProvider");
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
