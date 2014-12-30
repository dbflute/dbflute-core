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
package org.dbflute.unit;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import junit.framework.TestCase;

import org.dbflute.cbean.result.PagingResultBean;
import org.dbflute.hook.AccessContext;
import org.dbflute.system.DBFluteSystem;
import org.dbflute.unit.markhere.MarkHereManager;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.DfStringUtil;
import org.dbflute.util.DfTypeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 1.1.0 (2014/10/12 Sunday)
 */
public abstract class RuntimeTestCase extends TestCase {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The logger instance for sub class. (NotNull) */
    protected final Logger _xlogger = LoggerFactory.getLogger(getClass());

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The manager of mark here. (NullAllowed: lazy-loaded) */
    protected MarkHereManager _xmarkHereManager;

    /** The reserved title for logging test case beginning. (NullAllowed: before preparation or already showed) */
    protected String _xreservedTitle;

    // ===================================================================================
    //                                                                            Settings
    //                                                                            ========
    @Override
    protected void setUp() throws Exception {
        xreserveShowTitle();
        xprepareAccessContext();
        super.setUp();
    }

    protected void xreserveShowTitle() {
        // lazy-logging (no logging test case, no title)
        _xreservedTitle = "<<< " + xgetCaseDisp() + " >>>";
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        xclearAccessContext();
        xclearMark();
    }

    protected void xprepareAccessContext() {
        final AccessContext context = new AccessContext();
        context.setAccessTimestamp(currentTimestamp());
        context.setAccessDate(currentDate());
        context.setAccessUser(Thread.currentThread().getName());
        context.setAccessProcess(getClass().getSimpleName());
        AccessContext.setAccessContextOnThread(context);
    }

    /**
     * Get the access context for common column auto setup of DBFlute.
     * @return The instance of access context on the thread. (basically NotNull)
     */
    protected AccessContext getAccessContext() { // user method
        return AccessContext.getAccessContextOnThread();
    }

    protected void xclearAccessContext() {
        AccessContext.clearAccessContextOnThread();
    }

    protected void xclearMark() {
        if (xhasMarkHereManager()) {
            xgetMarkHereManager().checkNonAssertedMark();
            xgetMarkHereManager().clearMarkMap();
            xdestroyMarkHereManager();
        }
    }

    // ===================================================================================
    //                                                                       Assert Helper
    //                                                                       =============
    // -----------------------------------------------------
    //                                                Equals
    //                                                ------
    // to avoid setting like this:
    //  assertEquals(Integer.valueOf(3), member.getMemberId())
    protected void assertEquals(String message, int expected, Integer actual) {
        assertEquals(message, Integer.valueOf(expected), actual);
    }

    protected void assertEquals(int expected, Integer actual) {
        assertEquals(null, Integer.valueOf(expected), actual);
    }

    // -----------------------------------------------------
    //                                            True/False
    //                                            ----------
    protected void assertTrueAll(boolean... conditions) {
        int index = 0;
        for (boolean condition : conditions) {
            assertTrue("conditions[" + index + "]" + " expected: <true> but was: " + condition, condition);
            ++index;
        }
    }

    protected void assertTrueAny(boolean... conditions) {
        boolean hasTrue = false;
        for (boolean condition : conditions) {
            if (condition) {
                hasTrue = true;
                break;
            }
        }
        assertTrue("all conditions were false", hasTrue);
    }

    protected void assertFalseAll(boolean... conditions) {
        int index = 0;
        for (boolean condition : conditions) {
            assertFalse("conditions[" + index + "]" + " expected: <false> but was: " + condition, condition);
            ++index;
        }
    }

    protected void assertFalseAny(boolean... conditions) {
        boolean hasFalse = false;
        for (boolean condition : conditions) {
            if (!condition) {
                hasFalse = true;
                break;
            }
        }
        assertTrue("all conditions were true", hasFalse);
    }

    // -----------------------------------------------------
    //                                                String
    //                                                ------
    /**
     * Assert that the string contains the keyword.
     * <pre>
     * String str = "foo";
     * assertContains(str, "fo"); <span style="color: #3F7E5E">// true</span>
     * assertContains(str, "oo"); <span style="color: #3F7E5E">// true</span>
     * assertContains(str, "foo"); <span style="color: #3F7E5E">// true</span>
     * assertContains(str, "Foo"); <span style="color: #3F7E5E">// false</span>
     * </pre>
     * @param str The string to assert. (NotNull)
     * @param keyword The keyword string. (NotNull) 
     */
    protected void assertContains(String str, String keyword) {
        if (!DfStringUtil.contains(str, keyword)) {
            log("Asserted string: " + str); // might be large so show at log
            fail("the string should have the keyword but not found: " + keyword);
        }
    }

    /**
     * Assert that the string contains the keyword. (ignore case)
     * <pre>
     * String str = "foo";
     * assertContains(str, "fo"); <span style="color: #3F7E5E">// true</span>
     * assertContains(str, "oo"); <span style="color: #3F7E5E">// true</span>
     * assertContains(str, "foo"); <span style="color: #3F7E5E">// true</span>
     * assertContains(str, "Foo"); <span style="color: #3F7E5E">// true</span>
     * assertContains(str, "ux"); <span style="color: #3F7E5E">// false</span>
     * </pre>
     * @param str The string to assert. (NotNull)
     * @param keyword The keyword string. (NotNull) 
     */
    protected void assertContainsIgnoreCase(String str, String keyword) {
        if (!DfStringUtil.containsIgnoreCase(str, keyword)) {
            log("Asserted string: " + str); // might be large so show at log
            fail("the string should have the keyword but not found: " + keyword);
        }
    }

    /**
     * Assert that the string contains all keywords.
     * <pre>
     * String str = "foo";
     * assertContains(str, "fo", "oo"); <span style="color: #3F7E5E">// true</span>
     * assertContains(str, "f", "foo"); <span style="color: #3F7E5E">// true</span>
     * assertContains(str, "f", "Foo"); <span style="color: #3F7E5E">// false</span>
     * assertContains(str, "fx", "oo"); <span style="color: #3F7E5E">// false</span>
     * </pre>
     * @param str The string to assert. (NotNull)
     * @param keywords The array of keyword string. (NotNull) 
     */
    protected void assertContainsAll(String str, String... keywords) {
        if (!DfStringUtil.containsAll(str, keywords)) {
            log("Asserted string: " + str); // might be large so show at log
            fail("the string should have all keywords but not found: " + newArrayList(keywords));
        }
    }

    /**
     * Assert that the string contains all keywords. (ignore case)
     * <pre>
     * String str = "foo";
     * assertContains(str, "fo", "oo"); <span style="color: #3F7E5E">// true</span>
     * assertContains(str, "f", "foo"); <span style="color: #3F7E5E">// true</span>
     * assertContains(str, "f", "Foo"); <span style="color: #3F7E5E">// true</span>
     * assertContains(str, "fx", "oo"); <span style="color: #3F7E5E">// false</span>
     * </pre>
     * @param str The string to assert. (NotNull)
     * @param keywords The array of keyword string. (NotNull) 
     */
    protected void assertContainsAllIgnoreCase(String str, String... keywords) {
        if (!DfStringUtil.containsAllIgnoreCase(str, keywords)) {
            log("Asserted string: " + str); // might be large so show at log
            fail("the string should have all keywords but not found: " + newArrayList(keywords));
        }
    }

    /**
     * Assert that the string contains any keyword.
     * <pre>
     * String str = "foo";
     * assertContains(str, "fo", "oo"); <span style="color: #3F7E5E">// true</span>
     * assertContains(str, "f", "foo"); <span style="color: #3F7E5E">// true</span>
     * assertContains(str, "f", "qux"); <span style="color: #3F7E5E">// true</span>
     * assertContains(str, "F", "qux"); <span style="color: #3F7E5E">// false</span>
     * assertContains(str, "fx", "ux"); <span style="color: #3F7E5E">// false</span>
     * </pre>
     * @param str The string to assert. (NotNull)
     * @param keywords The array of keyword string. (NotNull) 
     */
    protected void assertContainsAny(String str, String... keywords) {
        if (!DfStringUtil.containsAny(str, keywords)) {
            log("Asserted string: " + str); // might be large so show at log
            fail("the string should have any keyword but not found: " + newArrayList(keywords));
        }
    }

    /**
     * Assert that the string contains any keyword. (ignore case)
     * <pre>
     * String str = "foo";
     * assertContains(str, "fo", "oo"); <span style="color: #3F7E5E">// true</span>
     * assertContains(str, "f", "foo"); <span style="color: #3F7E5E">// true</span>
     * assertContains(str, "f", "qux"); <span style="color: #3F7E5E">// true</span>
     * assertContains(str, "F", "qux"); <span style="color: #3F7E5E">// true</span>
     * assertContains(str, "fx", "ux"); <span style="color: #3F7E5E">// false</span>
     * </pre>
     * @param str The string to assert. (NotNull)
     * @param keywords The array of keyword string. (NotNull) 
     */
    protected void assertContainsAnyIgnoreCase(String str, String... keywords) {
        if (!DfStringUtil.containsAnyIgnoreCase(str, keywords)) {
            log("Asserted string: " + str); // might be large so show at log
            fail("the string should have any keyword but not found: " + newArrayList(keywords));
        }
    }

    /**
     * Assert that the string does not contains the keyword.
     * <pre>
     * String str = "foo";
     * assertNotContains(str, "ux"); <span style="color: #3F7E5E">// true</span>
     * assertNotContains(str, "Foo"); <span style="color: #3F7E5E">// true</span>
     * assertNotContains(str, "fo"); <span style="color: #3F7E5E">// false</span>
     * assertNotContains(str, "oo"); <span style="color: #3F7E5E">// false</span>
     * assertNotContains(str, "foo"); <span style="color: #3F7E5E">// false</span>
     * </pre>
     * @param str The string to assert. (NotNull)
     * @param keyword The keyword string. (NotNull) 
     */
    protected void assertNotContains(String str, String keyword) {
        if (DfStringUtil.contains(str, keyword)) {
            log("Asserted string: " + str); // might be large so show at log
            fail("the string should not have the keyword but found: " + keyword);
        }
    }

    /**
     * Assert that the string does not contains the keyword. (ignore case)
     * <pre>
     * String str = "foo";
     * assertContains(str, "ux"); <span style="color: #3F7E5E">// true</span>
     * assertContains(str, "Foo"); <span style="color: #3F7E5E">// false</span>
     * assertContains(str, "fo"); <span style="color: #3F7E5E">// false</span>
     * assertContains(str, "oo"); <span style="color: #3F7E5E">// false</span>
     * assertContains(str, "foo"); <span style="color: #3F7E5E">// false</span>
     * </pre>
     * @param str The string to assert. (NotNull)
     * @param keyword The keyword string. (NotNull) 
     */
    protected void assertNotContainsIgnoreCase(String str, String keyword) {
        if (DfStringUtil.containsIgnoreCase(str, keyword)) {
            log("Asserted string: " + str); // might be large so show at log
            fail("the string should not have the keyword but found: " + keyword);
        }
    }

    // -----------------------------------------------------
    //                                                  List
    //                                                  ----
    /**
     * Assert that the list has an element containing the keyword.
     * <pre>
     * List&lt;String&gt; strList = ...; <span style="color: #3F7E5E">// [foo, bar]</span>
     * assertContainsKeyword(strList, "fo"); <span style="color: #3F7E5E">// true</span>
     * assertContainsKeyword(strList, "ar"); <span style="color: #3F7E5E">// true</span>
     * assertContainsKeyword(strList, "foo"); <span style="color: #3F7E5E">// true</span>
     * assertContainsKeyword(strList, "Foo"); <span style="color: #3F7E5E">// false</span>
     * assertContainsKeyword(strList, "ux"); <span style="color: #3F7E5E">// false</span>
     * </pre>
     * @param strList The list of string. (NotNull)
     * @param keyword The keyword string. (NotNull) 
     */
    protected void assertContainsKeyword(Collection<String> strList, String keyword) {
        if (!DfStringUtil.containsKeyword(newArrayList(strList), keyword)) {
            fail("the list should have the keyword but not found: " + keyword);
        }
    }

    /**
     * Assert that the list has an element containing all keywords.
     * <pre>
     * List&lt;String&gt; strList = ...; <span style="color: #3F7E5E">// [foo, bar]</span>
     * assertContainsKeyword(strList, "fo", "ar", "foo"); <span style="color: #3F7E5E">// true</span>
     * assertContainsKeyword(strList, "fo", "ar", "Foo"); <span style="color: #3F7E5E">// false</span>
     * assertContainsKeyword(strList, "fo", "ux", "foo"); <span style="color: #3F7E5E">// false</span>
     * </pre>
     * @param strList The list of string. (NotNull)
     * @param keywords The array of keyword string. (NotNull) 
     */
    protected void assertContainsKeywordAll(Collection<String> strList, String... keywords) {
        if (!DfStringUtil.containsKeywordAll(newArrayList(strList), keywords)) {
            fail("the list should have all keywords but not found: " + newArrayList(keywords));
        }
    }

    /**
     * Assert that the list has an element containing all keywords. (ignore case)
     * <pre>
     * List&lt;String&gt; strList = ...; <span style="color: #3F7E5E">// [foo, bar]</span>
     * assertContainsKeyword(strList, "fo", "ar", "foo"); <span style="color: #3F7E5E">// true</span>
     * assertContainsKeyword(strList, "fO", "ar", "Foo"); <span style="color: #3F7E5E">// true</span>
     * assertContainsKeyword(strList, "fo", "ux", "foo"); <span style="color: #3F7E5E">// false</span>
     * </pre>
     * @param strList The list of string. (NotNull)
     * @param keywords The array of keyword string. (NotNull) 
     */
    protected void assertContainsKeywordAllIgnoreCase(Collection<String> strList, String... keywords) {
        if (!DfStringUtil.containsKeywordAllIgnoreCase(newArrayList(strList), keywords)) {
            fail("the list should have all keywords (case ignored) but not found: " + newArrayList(keywords));
        }
    }

    /**
     * Assert that the list has an element containing any keyword.
     * <pre>
     * List&lt;String&gt; strList = ...; <span style="color: #3F7E5E">// [foo, bar]</span>
     * assertContainsKeyword(strList, "fo", "ar", "foo"); <span style="color: #3F7E5E">// true</span>
     * assertContainsKeyword(strList, "fo", "ux", "qux"); <span style="color: #3F7E5E">// true</span>
     * assertContainsKeyword(strList, "Fo", "ux", "qux"); <span style="color: #3F7E5E">// false</span>
     * </pre>
     * @param strList The list of string. (NotNull)
     * @param keywords The array of keyword string. (NotNull) 
     */
    protected void assertContainsKeywordAny(Collection<String> strList, String... keywords) {
        if (!DfStringUtil.containsKeywordAny(newArrayList(strList), keywords)) {
            fail("the list should have any keyword but not found: " + newArrayList(keywords));
        }
    }

    /**
     * Assert that the list has an element containing any keyword. (ignore case)
     * <pre>
     * List&lt;String&gt; strList = ...; <span style="color: #3F7E5E">// [foo, bar]</span>
     * assertContainsKeyword(strList, "fo", "ar", "foo"); <span style="color: #3F7E5E">// true</span>
     * assertContainsKeyword(strList, "fo", "ux", "qux"); <span style="color: #3F7E5E">// true</span>
     * assertContainsKeyword(strList, "Fo", "ux", "qux"); <span style="color: #3F7E5E">// true</span>
     * assertContainsKeyword(strList, "po", "ux", "qux"); <span style="color: #3F7E5E">// false</span>
     * </pre>
     * @param strList The list of string. (NotNull)
     * @param keywords The array of keyword string. (NotNull) 
     */
    protected void assertContainsKeywordAnyIgnoreCase(Collection<String> strList, String... keywords) {
        if (!DfStringUtil.containsKeywordAnyIgnoreCase(newArrayList(strList), keywords)) {
            fail("the list should have any keyword (case ignored) but not found: " + newArrayList(keywords));
        }
    }

    /**
     * Assert that the list has any element (not empty). <br />
     * You can use this to guarantee assertion in loop like this:
     * <pre>
     * List&lt;Member&gt; memberList = memberBhv.selectList(cb);
     * <span style="color: #FD4747">assertHasAnyElement(memberList);</span>
     * for (Member member : memberList) {
     *     assertTrue(member.getMemberName().startsWith("S"));
     * }
     * </pre>
     * @param notEmptyList The list expected not empty. (NotNull)
     */
    protected void assertHasAnyElement(Collection<?> notEmptyList) {
        if (notEmptyList.isEmpty()) {
            fail("the list should have any element (not empty) but empty.");
        }
    }

    protected void assertHasOnlyOneElement(Collection<?> lonelyList) {
        if (lonelyList.size() != 1) {
            fail("the list should have the only one element but: " + lonelyList);
        }
    }

    protected void assertHasPluralElement(Collection<?> crowdedList) {
        if (crowdedList.size() < 2) {
            fail("the list should have plural elements but: " + crowdedList);
        }
    }

    protected void assertHasZeroElement(Collection<?> emptyList) {
        if (!emptyList.isEmpty()) {
            fail("the list should have zero element (empty) but: " + emptyList);
        }
    }

    // -----------------------------------------------------
    //                                             Mark Here
    //                                             ---------
    /**
     * Mark here to assert that it goes through the road.
     * <pre>
     * final String mark = "cursor";
     * MemberCB cb = new MemberCB();
     * memberBhv.selectCursor(cb, entity -&gt; {
     *     <span style="color: #FD4747">markHere</span>(mark);
     * });
     * assertMarked(mark); <span style="color: #3F7E5E">// the callback called</span>
     * </pre>
     * @param mark The your original mark expression as string. (NotNull)
     */
    protected void markHere(String mark) {
        assertNotNull(mark);
        xgetMarkHereManager().mark(mark);
    }

    /**
     * Assert the mark is marked. (found in existing marks)
     * <pre>
     * final String mark = "cursor";
     * MemberCB cb = new MemberCB();
     * memberBhv.selectCursor(cb, entity -&gt; {
     *     markHere(mark);
     * });
     * <span style="color: #FD4747">assertMarked</span>(mark); <span style="color: #3F7E5E">// the callback called</span>
     * </pre>
     * @param mark The your original mark expression as string. (NotNull)
     */
    protected void assertMarked(String mark) {
        assertNotNull(mark);
        xgetMarkHereManager().assertMarked(mark);
    }

    /**
     * Is the mark marked? (found the mark in existing marks?)
     * @param mark The your original mark expression as string. (NotNull)
     * @return The determination, true or false.
     */
    protected boolean isMarked(String mark) {
        assertNotNull(mark);
        return xgetMarkHereManager().isMarked(mark);
    }

    protected MarkHereManager xgetMarkHereManager() {
        if (_xmarkHereManager == null) {
            _xmarkHereManager = new MarkHereManager();
        }
        return _xmarkHereManager;
    }

    protected boolean xhasMarkHereManager() {
        return _xmarkHereManager != null;
    }

    protected void xdestroyMarkHereManager() {
        _xmarkHereManager = null;
    }

    // ===================================================================================
    //                                                                      Logging Helper
    //                                                                      ==============
    /**
     * Log the messages. <br />
     * If you set an exception object to the last element, it shows stack traces.
     * <pre>
     * Member member = ...;
     * <span style="color: #FD4747">log</span>(member.getMemberName(), member.getBirthdate());
     * <span style="color: #3F7E5E">// -&gt; Stojkovic, 1965/03/03</span>
     * 
     * Exception e = ...;
     * <span style="color: #FD4747">log</span>(member.getMemberName(), member.getBirthdate(), e);
     * <span style="color: #3F7E5E">// -&gt; Stojkovic, 1965/03/03</span>
     * <span style="color: #3F7E5E">//  (and stack traces)</span>
     * </pre>
     * @param msgs The array of messages. (NotNull)
     */
    protected void log(Object... msgs) {
        if (msgs == null) {
            throw new IllegalArgumentException("The argument 'msgs' should not be null.");
        }
        Throwable cause = null;
        final int arrayLength = msgs.length;
        if (arrayLength > 0) {
            final Object lastElement = msgs[arrayLength - 1];
            if (lastElement instanceof Throwable) {
                cause = (Throwable) lastElement;
            }
        }
        final StringBuilder sb = new StringBuilder();
        int index = 0;
        for (Object msg : msgs) {
            if (index == arrayLength - 1 && cause != null) { // last loop and it is cause
                break;
            }
            if (sb.length() > 0) {
                sb.append(", ");
            }
            final String appended;
            if (msg instanceof Timestamp) {
                appended = toString(msg, "yyyy/MM/dd HH:mm:ss.SSS");
            } else if (msg instanceof Date) {
                appended = toString(msg, "yyyy/MM/dd");
            } else {
                appended = msg != null ? msg.toString() : null;
            }
            sb.append(appended);
            ++index;
        }
        final String msg = sb.toString();
        if (_xreservedTitle != null) {
            _xlogger.debug("");
            _xlogger.debug(_xreservedTitle);
            _xreservedTitle = null;
        }
        if (cause != null) {
            _xlogger.debug(msg, cause);
        } else {
            _xlogger.debug(msg);
        }
        // see comment for logger definition for the detail
        //_xlogger.log(PlainTestCase.class.getName(), Level.DEBUG, msg, cause);
    }

    // ===================================================================================
    //                                                                         Show Helper
    //                                                                         ===========
    protected void showPage(PagingResultBean<?>... pages) {
        int count = 1;
        for (PagingResultBean<? extends Object> page : pages) {
            log("[page" + count + "]");
            for (Object entity : page) {
                log("  " + entity);
            }
            ++count;
        }
    }

    protected void showList(List<?>... list) {
        int count = 1;
        for (List<? extends Object> ls : list) {
            log("[list" + count + "]");
            for (Object entity : ls) {
                log("  " + entity);
            }
            ++count;
        }
    }

    // ===================================================================================
    //                                                                       String Helper
    //                                                                       =============
    protected String replace(String str, String fromStr, String toStr) {
        return DfStringUtil.replace(str, fromStr, toStr);
    }

    protected List<String> splitList(String str, String delimiter) {
        return DfStringUtil.splitList(str, delimiter);
    }

    protected List<String> splitListTrimmed(String str, String delimiter) {
        return DfStringUtil.splitListTrimmed(str, delimiter);
    }

    protected String toString(Object obj) {
        return DfTypeUtil.toString(obj);
    }

    protected String toString(Object obj, String pattern) {
        return DfTypeUtil.toString(obj, pattern);
    }

    // ===================================================================================
    //                                                                       Number Helper
    //                                                                       =============
    protected Integer toInteger(Object obj) {
        return DfTypeUtil.toInteger(obj);
    }

    protected Long toLong(Object obj) {
        return DfTypeUtil.toLong(obj);
    }

    protected BigDecimal toBigDecimal(Object obj) {
        return DfTypeUtil.toBigDecimal(obj);
    }

    // ===================================================================================
    //                                                                         Date Helper
    //                                                                         ===========
    protected Date currentDate() {
        return new Date(System.currentTimeMillis());
    }

    protected Timestamp currentTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }

    protected LocalDate toLocalDate(Object obj) {
        return DfTypeUtil.toLocalDate(obj, DBFluteSystem.getFinalTimeZone());
    }

    protected LocalDateTime toLocalDateTime(Object obj) {
        return DfTypeUtil.toLocalDateTime(obj, DBFluteSystem.getFinalTimeZone());
    }

    protected Date toDate(Object obj) {
        return DfTypeUtil.toDate(obj);
    }

    protected Timestamp toTimestamp(Object obj) {
        return DfTypeUtil.toTimestamp(obj);
    }

    // ===================================================================================
    //                                                                   Collection Helper
    //                                                                   =================
    protected <ELEMENT> ArrayList<ELEMENT> newArrayList() {
        return DfCollectionUtil.newArrayList();
    }

    public <ELEMENT> ArrayList<ELEMENT> newArrayList(Collection<ELEMENT> elements) {
        return DfCollectionUtil.newArrayList(elements);
    }

    // avoid heap warning
    protected <ELEMENT> List<ELEMENT> newArrayList(ELEMENT el) {
        return DfCollectionUtil.newArrayList(el);
    }

    protected <ELEMENT> List<ELEMENT> newArrayList(ELEMENT el1, ELEMENT el3) {
        return DfCollectionUtil.newArrayList(el1, el3);
    }

    protected <ELEMENT> List<ELEMENT> newArrayList(ELEMENT el1, ELEMENT el2, ELEMENT el3) {
        return DfCollectionUtil.newArrayList(el1, el2, el3);
    }

    protected <ELEMENT> List<ELEMENT> newArrayList(ELEMENT el1, ELEMENT el2, ELEMENT el3, ELEMENT el4) {
        return DfCollectionUtil.newArrayList(el1, el2, el3, el4);
    }

    protected <ELEMENT> List<ELEMENT> newArrayList(ELEMENT el1, ELEMENT el2, ELEMENT el3, ELEMENT el4, ELEMENT el5) {
        return DfCollectionUtil.newArrayList(el1, el2, el3, el4, el5);
    }

    protected <ELEMENT> List<ELEMENT> newArrayList(ELEMENT el1, ELEMENT el2, ELEMENT el3, ELEMENT el4, ELEMENT el5, ELEMENT el6) {
        return DfCollectionUtil.newArrayList(el1, el2, el3, el4, el5, el6);
    }

    protected <ELEMENT> List<ELEMENT> newArrayList(ELEMENT el1, ELEMENT el2, ELEMENT el3, ELEMENT el4, ELEMENT el5, ELEMENT el6, ELEMENT el7) {
        return DfCollectionUtil.newArrayList(el1, el2, el3, el4, el5, el6, el7);
    }

    protected <ELEMENT> HashSet<ELEMENT> newHashSet() {
        return DfCollectionUtil.newHashSet();
    }

    protected <ELEMENT> HashSet<ELEMENT> newHashSet(Collection<ELEMENT> elements) {
        return DfCollectionUtil.newHashSet(elements);
    }

    protected <ELEMENT> LinkedHashSet<ELEMENT> newLinkedHashSet() {
        return DfCollectionUtil.newLinkedHashSet();
    }

    protected <ELEMENT> LinkedHashSet<ELEMENT> newLinkedHashSet(Collection<ELEMENT> elements) {
        return DfCollectionUtil.newLinkedHashSet(elements);
    }

    protected <KEY, VALUE> HashMap<KEY, VALUE> newHashMap() {
        return DfCollectionUtil.newHashMap();
    }

    protected <KEY, VALUE> HashMap<KEY, VALUE> newHashMap(KEY key, VALUE value) {
        return DfCollectionUtil.newHashMap(key, value);
    }

    protected <KEY, VALUE> HashMap<KEY, VALUE> newHashMap(KEY key1, VALUE value1, KEY key2, VALUE value2) {
        return DfCollectionUtil.newHashMap(key1, value1, key2, value2);
    }

    protected <KEY, VALUE> LinkedHashMap<KEY, VALUE> newLinkedHashMap() {
        return DfCollectionUtil.newLinkedHashMap();
    }

    protected <KEY, VALUE> LinkedHashMap<KEY, VALUE> newLinkedHashMap(KEY key, VALUE value) {
        return DfCollectionUtil.newLinkedHashMap(key, value);
    }

    protected <KEY, VALUE> LinkedHashMap<KEY, VALUE> newLinkedHashMap(KEY key1, VALUE value1, KEY key2, VALUE value2) {
        return DfCollectionUtil.newLinkedHashMap(key1, value1, key2, value2);
    }

    // ===================================================================================
    //                                                                       System Helper
    //                                                                       =============
    /**
     * Get the line separator. (LF fixedly)
     * @return The string of the line separator. (NotNull)
     */
    protected String ln() {
        return "\n";
    }

    protected String xgetCaseDisp() {
        return getClass().getSimpleName() + "." + getName() + "()";
    }
}
