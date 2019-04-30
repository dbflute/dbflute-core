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
package org.dbflute.util;

import static org.dbflute.util.Srl.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dbflute.unit.RuntimeTestCase;
import org.dbflute.util.Srl.DelimiterInfo;
import org.dbflute.util.Srl.IndexOfInfo;
import org.dbflute.util.Srl.ScopeInfo;

/**
 * @author jflute
 * @since 0.9.5 (2009/04/10 Friday)
 */
public class DfStringUtilTest extends RuntimeTestCase {

    // ===================================================================================
    //                                                                        Null & Empty
    //                                                                        ============
    public void test_is_Null_or_Empty() {
        assertTrue(is_Null_or_Empty(null));
        assertTrue(is_Null_or_Empty(""));
        assertFalse(is_Null_or_Empty(" "));
        assertFalse(is_Null_or_Empty("foo"));
    }

    public void test_is_Null_or_TrimmedEmpty() {
        assertTrue(is_Null_or_TrimmedEmpty(null));
        assertTrue(is_Null_or_TrimmedEmpty(""));
        assertTrue(is_Null_or_TrimmedEmpty(" "));
        assertFalse(is_Null_or_TrimmedEmpty("foo"));
    }

    public void test_is_NotNull_and_NotEmpty() {
        assertFalse(is_NotNull_and_NotEmpty(null));
        assertFalse(is_NotNull_and_NotEmpty(""));
        assertTrue(is_NotNull_and_NotEmpty(" "));
        assertTrue(is_NotNull_and_NotEmpty("foo"));
    }

    public void test_is_NotNull_and_NotTrimmedEmpty() {
        assertFalse(is_NotNull_and_NotTrimmedEmpty(null));
        assertFalse(is_NotNull_and_NotTrimmedEmpty(""));
        assertFalse(is_NotNull_and_NotTrimmedEmpty(" "));
        assertTrue(is_NotNull_and_NotTrimmedEmpty("foo"));
    }

    // ===================================================================================
    //                                                                              Length
    //                                                                              ======
    public void test_length() {
        assertEquals(0, length(""));
        assertEquals(1, length(" "));
        assertEquals(3, length("foo"));
    }

    public void test_cut() {
        assertEquals("", cut("", 3));
        assertEquals(" ", cut(" ", 2));
        assertEquals("f", cut("foo", 1));
        assertEquals("fo", cut("foo", 2));
        assertEquals("foo", cut("foo", 3));
        assertEquals("foo", cut("foo", 4));
    }

    public void test_cut_suffix() {
        assertEquals("", cut("", 3, "..."));
        assertEquals(" ", cut(" ", 2, "..."));
        assertEquals("f...", cut("foo", 1, "..."));
        assertEquals("fo++", cut("foo", 2, "++"));
        assertEquals("foo", cut("foo", 3, "@"));
        assertEquals("foo", cut("foo", 4, ">>>"));
    }

    // ===================================================================================
    //                                                                                Case
    //                                                                                ====
    public void test_toLowerCase() {
        assertEquals("", toLowerCase(""));
        assertEquals("f", toLowerCase("F"));
        assertEquals("foo", toLowerCase("FOO"));
    }

    public void test_toUpperCase() {
        assertEquals("", toUpperCase(""));
        assertEquals("F", toUpperCase("f"));
        assertEquals("FOO", toUpperCase("foo"));
    }

    // ===================================================================================
    //                                                                                Trim
    //                                                                                ====
    public void test_trim_default() {
        assertEquals("foo", trim(" foo "));
        assertEquals("foo", trim("\n foo "));
        assertEquals("foo", trim("\n \n foo "));
        assertEquals("foo", trim(" \r\n foo "));
        assertEquals("foo", trim(" \r\n \r\n foo "));
    }

    public void test_trim_originalTrimTarget() {
        assertEquals(" foo ", trim("\n foo ", "\n"));
        assertEquals(" \n foo ", trim(" \n foo ", "\n"));
        assertEquals("\r foo ", trim("\n\r foo ", "\n"));
        assertEquals(" foo ", trim("\r\n foo ", "\r\n"));
        assertEquals("foo", trim("'foo'", "'"));
        assertEquals("f'o'o", trim("'f'o'o'", "'"));
        assertEquals("fo''o", trim("'fo''o'", "'"));
        assertEquals("foo", trim("\"foo\"", "\""));
        assertEquals("f\"o\"o", trim("\"f\"o\"o\"", "\""));
        assertEquals("fo\"\"o", trim("\"fo\"\"o\"", "\""));
        assertEquals("Long", trim(">>Long>>", ">"));
    }

    public void test_ltrim_default() {
        assertEquals("foo ", ltrim(" foo "));
        assertEquals("foo ", ltrim("\n foo "));
        assertEquals("foo ", ltrim("\n \n foo "));
        assertEquals("foo ", ltrim(" \r\n foo "));
        assertEquals("foo ", ltrim(" \r\n \r\n foo "));
    }

    public void test_ltrim_originalTrimTarget() {
        assertEquals(" foo ", ltrim("\n foo ", "\n"));
        assertEquals(" foo ", ltrim("\n\n foo ", "\n"));
        assertEquals(" \n foo ", ltrim(" \n foo ", "\n"));
        assertEquals("\r foo ", ltrim("\n\r foo ", "\n"));
        assertEquals(" foo ", ltrim("\r\n foo ", "\r\n"));
        assertEquals("Long>", ltrim("List<Long>", "List<"));
        assertEquals("Long>>", ltrim("List<List<Long>>", "List<"));
    }

    public void test_rtrim_default() {
        assertEquals(" foo", rtrim(" foo "));
        assertEquals(" foo", rtrim(" foo \n "));
        assertEquals(" foo", rtrim(" foo \n \n"));
        assertEquals(" foo", rtrim(" foo \r\n "));
        assertEquals(" foo", rtrim(" foo \r\n \r\n"));
    }

    public void test_rtrim_originalTrimTarget() {
        assertEquals(" foo ", rtrim(" foo \n", "\n"));
        assertEquals(" foo ", rtrim(" foo \n\n", "\n"));
        assertEquals(" foo \n ", rtrim(" foo \n ", "\n"));
        assertEquals(" foo \r", rtrim(" foo \r\n", "\n"));
        assertEquals(" foo ", rtrim(" foo \r\n", "\r\n"));
        assertEquals("List<Long", rtrim("List<Long>", ">"));
        assertEquals("List<List<Long", rtrim("List<List<Long>>", ">"));
    }

    // ===================================================================================
    //                                                                             IndexOf
    //                                                                             =======
    public void test_indexOfFirst_basic() {
        assertEquals(3, indexOfFirst("foo.bar/baz.qux", ".", "/").getIndex());
        assertEquals(3, indexOfFirst("foo/bar.baz/qux", ".", "/").getIndex());
        IndexOfInfo info = indexOfFirst("foo.bar/baz.qux", ".", "/");
        assertEquals(4, info.getRearIndex());
        assertEquals("foo", info.substringFront());
        assertEquals("bar/baz.qux", info.substringRear());
        assertNull(indexOfFirst("foo.bar/baz.qux", "O", "A"));
    }

    public void test_indexOfFirstIgnoreCase_basic() {
        assertEquals(5, indexOfFirstIgnoreCase("foo.bar/baz.qux", "A", "U").getIndex());
        assertEquals(4, indexOfFirstIgnoreCase("foo/bar.baz/qux", "z", "B").getIndex());
        assertEquals("B", indexOfFirstIgnoreCase("foo/bar.baz/qux", "z", "B").getDelimiter());
        assertEquals("foo/bar.baz/qux", indexOfFirstIgnoreCase("foo/bar.baz/qux", "z", "B").getBaseString());
        assertNull(indexOfFirstIgnoreCase("foo.bar/baz.qux", "*", "@"));
    }

    public void test_indexOfLast_basic() {
        assertEquals(11, indexOfLast("foo.bar/baz.qux", ".", "/").getIndex());
        assertEquals(11, indexOfLast("foo/bar.baz/qux", ".", "/").getIndex());
        assertNull(indexOfLast("foo.bar/baz.qux", "O", "A"));
    }

    public void test_indexOfLastIgnoreCase_basic() {
        assertEquals(13, indexOfLastIgnoreCase("foo.bar/baz.qux", "A", "U").getIndex());
        assertEquals(12, indexOfLastIgnoreCase("foo/bar.baz/qux", "z", "Q").getIndex());
        assertEquals("Q", indexOfLastIgnoreCase("foo/bar.baz/qux", "z", "Q").getDelimiter());
        assertEquals("foo/bar.baz/qux", indexOfLastIgnoreCase("foo/bar.baz/qux", "z", "Q").getBaseString());
        assertNull(indexOfLastIgnoreCase("foo.bar/baz.qux", "@", "-"));
    }

    // ===================================================================================
    //                                                                           SubString
    //                                                                           =========
    public void test_substring_basic() {
        assertEquals("456", substring("123456", 3));
        assertEquals("123", substring("123456", 0, 3));
        assertEquals("4", substring("123456", 3, 4));
        assertEquals("456", substring("123456", 3, 6));
        assertEquals("123456", substring("123456", 0));
        assertEquals("123456", substring("123456", 0, 6));
        assertEquals("", substring("123456", 0, 0));
        assertEquals("", substring("123456", 3, 3));
    }

    public void test_substring_illegal() {
        try {
            substring("123456", 7);
            fail();
        } catch (StringIndexOutOfBoundsException e) {
            // OK
            log(e.getMessage());
        }
        try {
            substring("123456", 7, 8);
            fail();
        } catch (StringIndexOutOfBoundsException e) {
            // OK
            log(e.getMessage());
        }
        try {
            substring("123456", 1, 8);
            fail();
        } catch (StringIndexOutOfBoundsException e) {
            // OK
            log(e.getMessage());
        }
        try {
            substring("123456", 3, 2);
            fail();
        } catch (StringIndexOutOfBoundsException e) {
            // OK
            log(e.getMessage());
        }
    }

    public void test_frontstring_basic() {
        assertEquals("123", frontstring("123456", 3));
        assertEquals("1", frontstring("123456", 1));
        assertEquals("123456", frontstring("123456", 6));
        assertEquals("", frontstring("123456", 0));
    }

    public void test_frontstring_illegal() {
        try {
            frontstring("123456", 7);
            fail();
        } catch (StringIndexOutOfBoundsException e) {
            // OK
            log(e.getMessage());
        }
    }

    public void test_rearstring_basic() {
        assertEquals("456", rearstring("123456", 3));
        assertEquals("6", rearstring("123456", 1));
        assertEquals("123456", rearstring("123456", 6));
        assertEquals("", rearstring("123456", 0));
    }

    public void test_rearstring_illegal() {
        try {
            rearstring("123456", 7);
            fail();
        } catch (StringIndexOutOfBoundsException e) {
            // OK
            log(e.getMessage());
        }
    }

    public void test_substringFirstFront_basic() {
        assertEquals("foo", substringFirstFront("foo.bar", "."));
        assertEquals("foo", substringFirstFront("foo.bar.don", "."));
        assertEquals("foobar", substringFirstFront("foobar", "."));
        assertEquals("foo", substringFirstFront("foo.bar/baz.qux", "/", "."));
        assertEquals("foo/bar", substringFirstFront("foo/bar.don.moo", "."));
        assertEquals("foo", substringFirstFront("foo/bar.don.moo", ".", "/"));
        assertEquals("foo", substringFirstFront("foo.bar.don.moo", ".", "/"));
        assertEquals("foo", substringFirstFront("foo.bar.don.moo", "/", "."));
    }

    public void test_substringFirstFrontIgnoreCase_basic() {
        assertEquals("foo.b", substringFirstFrontIgnoreCase("foo.bar", "A"));
        assertEquals("foo.bar/ba", substringFirstFrontIgnoreCase("foo.bar/baz.qux", "Z", "U"));
        assertEquals("foo.baR/bAz.qux", substringFirstFrontIgnoreCase("foo.baR/bAz.qux", "C"));
        assertEquals("fOo.", substringFirstFrontIgnoreCase("fOo.baR/bAz.qux", "B"));
    }

    public void test_substringFirstRear_basic() {
        assertEquals("bar", substringFirstRear("foo.bar", "."));
        assertEquals("bar.don", substringFirstRear("foo.bar.don", "."));
        assertEquals("foobar", substringFirstRear("foobar", "."));
        assertEquals("bar/baz.qux", substringFirstRear("foo.bar/baz.qux", "/", "."));
        assertEquals("don.moo", substringFirstRear("foo/bar.don.moo", "."));
        assertEquals("bar.don.moo", substringFirstRear("foo/bar.don.moo", ".", "/"));
        assertEquals("bar.don.moo", substringFirstRear("foo.bar.don.moo", ".", "/"));
        assertEquals("bar.don.moo", substringFirstRear("foo.bar.don.moo", "/", "."));
    }

    public void test_substringFirstRearIgnoreCase_basic() {
        assertEquals("r", substringFirstRearIgnoreCase("foo.bar", "A"));
        assertEquals(".qux", substringFirstRearIgnoreCase("foo.bar/baz.qux", "Z", "U"));
    }

    public void test_substringLastFront_basic() {
        assertEquals("foo", substringLastFront("foo.bar", "."));
        assertEquals("foo.bar", substringLastFront("foo.bar.don", "."));
        assertEquals("foobar", substringLastFront("foobar", "."));
        assertEquals("foo.bar/baz", substringLastFront("foo.bar/baz.qux", "/", "."));
        assertEquals("foo.bar", substringLastFront("foo.bar.don/moo", "."));
        assertEquals("foo.bar.don", substringLastFront("foo.bar.don/moo", ".", "/"));
        assertEquals("foo.bar.don", substringLastFront("foo.bar.don.moo", ".", "/"));
        assertEquals("foo.bar.don", substringLastFront("foo.bar.don.moo", "/", "."));

    }

    public void test_substringLastFrontIgnoreCase_basic() {
        assertEquals("fo", substringLastFrontIgnoreCase("foo.bar", "O"));
        assertEquals("foo.bar/baz.q", substringLastFrontIgnoreCase("foo.bar/baz.qux", "Z", "U"));
    }

    public void test_substringLastRear_basic() {
        assertEquals("bar", substringLastRear("foo.bar", "."));
        assertEquals("don", substringLastRear("foo.bar.don", "."));
        assertEquals("foobar", substringLastRear("foobar", "."));
        assertEquals("qux", substringLastRear("foo.bar/baz.qux", "/", "."));
        assertEquals("don/moo", substringLastRear("foo.bar.don/moo", "."));
        assertEquals("moo", substringLastRear("foo.bar.don/moo", ".", "/"));
        assertEquals("moo", substringLastRear("foo.bar.don.moo", ".", "/"));
        assertEquals("moo", substringLastRear("foo.bar.don.moo", "/", "."));
    }

    public void test_substringLastRearIgnoreCase_basic() {
        assertEquals("r", substringLastRearIgnoreCase("foo.bar", "A"));
        assertEquals("x", substringLastRearIgnoreCase("foo.bar/baz.qux", "Z", "U"));
    }

    // ===================================================================================
    //                                                                               Split
    //                                                                               =====
    public void test_splitList_basic() {
        String ln = "\n";
        List<String> splitList = splitList("aaa" + ln + "bbb" + ln + "ccc", ln);
        assertEquals("aaa", splitList.get(0));
        assertEquals("bbb", splitList.get(1));
        assertEquals("ccc", splitList.get(2));
    }

    public void test_splitList_empty() {
        String ln = "\n";
        List<String> splitList = splitList("", ln);
        assertHasOnlyOneElement(splitList);
        assertEquals("", splitList.get(0));
    }

    public void test_splitList_notTrim() {
        String ln = "\n";
        List<String> splitList = DfStringUtil.splitList("aaa " + ln + "bbb" + ln + " ccc", ln);
        assertEquals("aaa ", splitList.get(0));
        assertEquals("bbb", splitList.get(1));
        assertEquals(" ccc", splitList.get(2));
    }

    public void test_splitListTrimmed_trim() {
        String ln = "\n";
        List<String> splitList = splitListTrimmed("aaa " + ln + "bbb" + ln + " ccc", ln);
        assertEquals("aaa", splitList.get(0));
        assertEquals("bbb", splitList.get(1));
        assertEquals("ccc", splitList.get(2));
    }

    // ===================================================================================
    //                                                                             Replace
    //                                                                             =======
    public void test_replaceScopeContent_basic() {
        // ## Arrange ##
        String str = "/*foo*/foo/*bar*/bar/*foobarbaz*/";

        // ## Act ##
        String actual = replaceScopeContent(str, "foo", "jflute", "/*", "*/");

        // ## Assert ##
        assertEquals("/*jflute*/foo/*bar*/bar/*jflutebarbaz*/", actual);
    }

    public void test_replaceInterspaceContent_basic() {
        // ## Arrange ##
        String str = "/*foo*/foo/*bar*/bar/*foobarbaz*/";

        // ## Act ##
        String actual = replaceScopeInterspace(str, "foo", "jflute", "/*", "*/");

        // ## Assert ##
        assertEquals("/*foo*/jflute/*bar*/bar/*foobarbaz*/", actual);
    }

    // ===================================================================================
    //                                                                            Contains
    //                                                                            ========
    public void test_contains_basic() {
        assertTrue(contains("foobar", "foo"));
        assertTrue(contains("foobar", "bar"));
        assertTrue(contains("foobar", "ob"));
        assertFalse(contains("foobar", "Foo"));
        assertTrue(contains("foobar", ""));
        assertFalse(contains("foobar", null));
    }

    public void test_containsIgnoreCase_basic() {
        assertTrue(containsIgnoreCase("foobar", "foo"));
        assertTrue(containsIgnoreCase("foobar", "bar"));
        assertTrue(containsIgnoreCase("foobar", "ob"));
        assertTrue(containsIgnoreCase("foobar", "Foo"));
        assertFalse(containsIgnoreCase("foobar", "fo b"));
        assertTrue(containsIgnoreCase("foobar", ""));
        assertFalse(containsIgnoreCase("foobar", null));
    }

    public void test_containsAll_basic() {
        assertTrue(containsAll("foobar", "foo"));
        assertTrue(containsAll("foobar", "foo", "bar"));
        assertFalse(containsAll("foobar", "foo", "baz"));
        assertFalse(containsAll("foobar", "Foo", "bar"));
        assertFalse(containsAll("foobar", new String[] {}));
        assertFalse(containsAll("foobar", "foo", null));
    }

    public void test_containsAllIgnoreCase_basic() {
        assertTrue(containsAllIgnoreCase("foobar", "foo"));
        assertTrue(containsAllIgnoreCase("foobar", "foo", "bar"));
        assertFalse(containsAllIgnoreCase("foobar", "foo", "baz"));
        assertTrue(containsAllIgnoreCase("foobar", "Foo", "bar"));
        assertFalse(containsAllIgnoreCase("foobar", new String[] {}));
        assertFalse(containsAllIgnoreCase("foobar", "foo", null));
    }

    public void test_containsAny_basic() {
        assertTrue(containsAny("foobar", "foo"));
        assertTrue(containsAny("foobar", "foo", "bar"));
        assertTrue(containsAny("foobar", "foo", "baz"));
        assertFalse(containsAny("foobar", "Foo", "qux"));
        assertFalse(containsAny("foobar", new String[] {}));
        assertTrue(containsAny("foobar", "foo", null));
        assertTrue(containsAny("foobar", null, "foo", null));
    }

    public void test_containsAnyIgnoreCase_basic() {
        assertTrue(containsAnyIgnoreCase("foobar", "foo"));
        assertTrue(containsAnyIgnoreCase("foobar", "foo", "bar"));
        assertTrue(containsAnyIgnoreCase("foobar", "foo", "baz"));
        assertTrue(containsAnyIgnoreCase("foobar", "Foo", "qux"));
        assertFalse(containsAnyIgnoreCase("foobar", new String[] {}));
        assertTrue(containsAnyIgnoreCase("foobar", null, "foo", null));
    }

    public void test_containsOrderedAll_basic() {
        assertTrue(containsOrderedAll("foobar", "foo"));
        assertTrue(containsOrderedAll("foobar", "foo", "bar"));
        assertTrue(containsOrderedAll("foobar", "foo", "ba"));
        assertFalse(containsOrderedAll("foobar", "foo", "Bar"));
        assertFalse(containsOrderedAll("foobar", "bar", "foo"));
        assertFalse(containsOrderedAll("foobar", "foo", "ob"));
        assertFalse(containsOrderedAll("foobar", "foo", "baz"));
        assertFalse(containsOrderedAll("foobar", "Foo", "qux"));
        assertFalse(containsOrderedAll("foobar", new String[] {}));
        assertFalse(containsOrderedAll("foobar", null, "foo", null));
    }

    public void test_containsOrderedAllIgnoreCase_basic() {
        assertTrue(containsOrderedAllIgnoreCase("foobar", "foo"));
        assertTrue(containsOrderedAllIgnoreCase("foobar", "foo", "bar"));
        assertTrue(containsOrderedAllIgnoreCase("foobar", "foo", "ba"));
        assertTrue(containsOrderedAllIgnoreCase("foobar", "foo", "Bar"));
        assertFalse(containsOrderedAllIgnoreCase("foobar", "bar", "foo"));
        assertFalse(containsOrderedAllIgnoreCase("foobar", "foo", "ob"));
        assertFalse(containsOrderedAllIgnoreCase("foobar", "foo", "baz"));
        assertFalse(containsOrderedAllIgnoreCase("foobar", "Foo", "qux"));
        assertFalse(containsOrderedAllIgnoreCase("foobar", new String[] {}));
        assertFalse(containsOrderedAllIgnoreCase("foobar", null, "foo", null));
    }

    // -----------------------------------------------------
    //                                          List Element
    //                                          ------------
    public void test_containsElement_basic() {
        assertTrue(containsElement(Arrays.asList("foo", "bar"), "foo"));
        assertTrue(containsElement(Arrays.asList("foo", "bar"), "bar"));
        assertFalse(containsElement(Arrays.asList("foo", "bar"), "baz"));
        assertFalse(containsElement(Arrays.asList("foo", "bar"), "o"));
        assertFalse(containsElement(Arrays.asList("foo", "bar"), "fOo"));
        assertFalse(containsElement(new ArrayList<String>(), "o"));
        assertFalse(containsElement(Arrays.asList("foo", "bar"), null));
    }

    public void test_containsElementIgnoreCase_basic() {
        assertTrue(containsElementIgnoreCase(Arrays.asList("foo", "bar"), "foo"));
        assertTrue(containsElementIgnoreCase(Arrays.asList("foo", "bar"), "bar"));
        assertFalse(containsElementIgnoreCase(Arrays.asList("foo", "bar"), "baz"));
        assertFalse(containsElementIgnoreCase(Arrays.asList("foo", "bar"), "o"));
        assertTrue(containsElementIgnoreCase(Arrays.asList("foo", "bar"), "fOo"));
        assertFalse(containsElementIgnoreCase(new ArrayList<String>(), "o"));
        assertFalse(containsElementIgnoreCase(Arrays.asList("foo", "bar"), null));
    }

    public void test_containsElementAll_basic() {
        assertTrue(containsElementAll(Arrays.asList("foo", "bar"), "foo"));
        assertTrue(containsElementAll(Arrays.asList("foo", "bar"), "bar"));
        assertTrue(containsElementAll(Arrays.asList("foo", "bar"), "foo", "bar"));
        assertFalse(containsElementAll(Arrays.asList("foo", "bar"), "quux", "foo"));
        assertFalse(containsElementAll(Arrays.asList("foo", "bar"), "baz"));
        assertFalse(containsElementAll(Arrays.asList("foo", "bar"), "o"));
        assertFalse(containsElementAll(Arrays.asList("foo", "bar"), "fOo"));
        assertFalse(containsElementAll(new ArrayList<String>(), "o"));
        assertFalse(containsElementAll(Arrays.asList("foo", "bar"), new String[] {}));
        assertTrue(containsElementAll(Arrays.asList("foo", "bar", null), "foo", null, "bar"));
    }

    public void test_containsElementAllIgnoreCase_basic() {
        assertTrue(containsElementAllIgnoreCase(Arrays.asList("foo", "bar"), "foo"));
        assertTrue(containsElementAllIgnoreCase(Arrays.asList("foo", "bar"), "bar"));
        assertTrue(containsElementAllIgnoreCase(Arrays.asList("foo", "bar"), "foo", "bar"));
        assertFalse(containsElementAllIgnoreCase(Arrays.asList("foo", "bar"), "quux", "foo"));
        assertFalse(containsElementAllIgnoreCase(Arrays.asList("foo", "bar"), "baz"));
        assertFalse(containsElementAllIgnoreCase(Arrays.asList("foo", "bar"), "o"));
        assertTrue(containsElementAllIgnoreCase(Arrays.asList("foo", "bar"), "fOo"));
        assertTrue(containsElementAllIgnoreCase(Arrays.asList("foo", "bar"), "fOo", "foO", "baR"));
        assertFalse(containsElementAllIgnoreCase(new ArrayList<String>(), "o"));
        assertFalse(containsElementAllIgnoreCase(Arrays.asList("foo", "bar"), new String[] {}));
        assertTrue(containsElementAllIgnoreCase(Arrays.asList("foo", "bar", null), "Foo", null, "bAr"));
    }

    public void test_containsElementAny_basic() {
        assertTrue(containsElementAny(Arrays.asList("foo", "bar"), "foo"));
        assertTrue(containsElementAny(Arrays.asList("foo", "bar"), "bar"));
        assertTrue(containsElementAny(Arrays.asList("foo", "bar"), "foo", "bar"));
        assertTrue(containsElementAny(Arrays.asList("foo", "bar"), "quux", "foo"));
        assertFalse(containsElementAny(Arrays.asList("foo", "bar"), "baz"));
        assertFalse(containsElementAny(Arrays.asList("foo", "bar"), "o"));
        assertFalse(containsElementAny(Arrays.asList("foo", "bar"), "fOo"));
        assertFalse(containsElementAny(new ArrayList<String>(), "o"));
        assertFalse(containsElementAny(Arrays.asList("foo", "bar"), new String[] {}));
        assertTrue(containsElementAny(Arrays.asList("foo", "bar", null), "qux", null));
    }

    public void test_containsElementAnyIgnoreCase_basic() {
        assertTrue(containsElementAnyIgnoreCase(Arrays.asList("foo", "bar"), "foo"));
        assertTrue(containsElementAnyIgnoreCase(Arrays.asList("foo", "bar"), "bar"));
        assertTrue(containsElementAnyIgnoreCase(Arrays.asList("foo", "bar"), "foo", "bar"));
        assertTrue(containsElementAnyIgnoreCase(Arrays.asList("foo", "bar"), "quux", "foo"));
        assertFalse(containsElementAnyIgnoreCase(Arrays.asList("foo", "bar"), "baz"));
        assertFalse(containsElementAnyIgnoreCase(Arrays.asList("foo", "bar"), "o"));
        assertTrue(containsElementAnyIgnoreCase(Arrays.asList("foo", "bar"), "fOo"));
        assertTrue(containsElementAnyIgnoreCase(Arrays.asList("foo", "bar"), "fOo", "foO", "qux"));
        assertFalse(containsElementAnyIgnoreCase(new ArrayList<String>(), "o"));
        assertFalse(containsElementAnyIgnoreCase(Arrays.asList("foo", "bar"), new String[] {}));
        assertTrue(containsElementAnyIgnoreCase(Arrays.asList("foo", "bar", null), "qux", null));
    }

    // -----------------------------------------------------
    //                                          List Keyword
    //                                          ------------
    public void test_containsKeyword_basic() {
        assertTrue(containsKeyword(Arrays.asList("foo", "bar"), "foo"));
        assertTrue(containsKeyword(Arrays.asList("foo", "bar"), "bar"));
        assertFalse(containsKeyword(Arrays.asList("foo", "bar"), "baz"));
        assertTrue(containsKeyword(Arrays.asList("foo", "bar"), "o"));
        assertFalse(containsKeyword(Arrays.asList("foo", "bar"), "fOo"));
        assertFalse(containsKeyword(new ArrayList<String>(), "o"));
        assertFalse(containsKeyword(Arrays.asList("foo", "bar"), null));
    }

    public void test_containsKeywordIgnoreCase_basic() {
        assertTrue(containsKeywordIgnoreCase(Arrays.asList("foo", "bar"), "foo"));
        assertTrue(containsKeywordIgnoreCase(Arrays.asList("foo", "bar"), "bar"));
        assertFalse(containsKeywordIgnoreCase(Arrays.asList("foo", "bar"), "baz"));
        assertTrue(containsKeywordIgnoreCase(Arrays.asList("foo", "bar"), "o"));
        assertTrue(containsKeywordIgnoreCase(Arrays.asList("foo", "bar"), "fOo"));
        assertFalse(containsKeywordIgnoreCase(new ArrayList<String>(), "o"));
        assertFalse(containsKeywordIgnoreCase(Arrays.asList("foo", "bar"), null));
    }

    public void test_containsKeywordAll_basic() {
        assertTrue(containsKeywordAll(Arrays.asList("foo", "bar"), "foo"));
        assertTrue(containsKeywordAll(Arrays.asList("foo", "bar"), "bar"));
        assertTrue(containsKeywordAll(Arrays.asList("foo", "bar"), "foo", "bar"));
        assertFalse(containsKeywordAll(Arrays.asList("foo", "bar"), "quux", "foo"));
        assertFalse(containsKeywordAll(Arrays.asList("foo", "bar"), "baz"));
        assertTrue(containsKeywordAll(Arrays.asList("foo", "bar"), "o"));
        assertFalse(containsKeywordAll(Arrays.asList("foo", "bar"), "fOo"));
        assertFalse(containsKeywordAll(new ArrayList<String>(), "o"));
        assertFalse(containsKeywordAll(Arrays.asList("foo", "bar"), new String[] {}));
        assertFalse(containsKeywordAll(Arrays.asList("foo", "bar", null), "foo", null, "bar"));
    }

    public void test_containsKeywordAllIgnoreCase_basic() {
        assertTrue(containsKeywordAllIgnoreCase(Arrays.asList("foo", "bar"), "foo"));
        assertTrue(containsKeywordAllIgnoreCase(Arrays.asList("foo", "bar"), "bar"));
        assertTrue(containsKeywordAllIgnoreCase(Arrays.asList("foo", "bar"), "foo", "bar"));
        assertFalse(containsKeywordAllIgnoreCase(Arrays.asList("foo", "bar"), "quux", "foo"));
        assertFalse(containsKeywordAllIgnoreCase(Arrays.asList("foo", "bar"), "baz"));
        assertTrue(containsKeywordAllIgnoreCase(Arrays.asList("foo", "bar"), "o"));
        assertTrue(containsKeywordAllIgnoreCase(Arrays.asList("foo", "bar"), "fOo"));
        assertTrue(containsKeywordAllIgnoreCase(Arrays.asList("foo", "bar"), "fOo", "foO", "baR"));
        assertFalse(containsKeywordAllIgnoreCase(new ArrayList<String>(), "o"));
        assertFalse(containsKeywordAllIgnoreCase(Arrays.asList("foo", "bar"), new String[] {}));
        assertFalse(containsKeywordAllIgnoreCase(Arrays.asList("foo", "bar", null), "Foo", null, "bAr"));
    }

    public void test_containsKeywordAny_basic() {
        assertTrue(containsKeywordAny(Arrays.asList("foo", "bar"), "foo"));
        assertTrue(containsKeywordAny(Arrays.asList("foo", "bar"), "bar"));
        assertTrue(containsKeywordAny(Arrays.asList("foo", "bar"), "foo", "bar"));
        assertTrue(containsKeywordAny(Arrays.asList("foo", "bar"), "quux", "foo"));
        assertFalse(containsKeywordAny(Arrays.asList("foo", "bar"), "baz"));
        assertTrue(containsKeywordAny(Arrays.asList("foo", "bar"), "o"));
        assertFalse(containsKeywordAny(Arrays.asList("foo", "bar"), "fOo"));
        assertFalse(containsKeywordAny(new ArrayList<String>(), "o"));
        assertFalse(containsKeywordAny(Arrays.asList("foo", "bar"), new String[] {}));
        assertFalse(containsKeywordAny(Arrays.asList("foo", "bar", null), "qux", null));
    }

    public void test_containsKeywordAnyIgnoreCase_basic() {
        assertTrue(containsKeywordAnyIgnoreCase(Arrays.asList("foo", "bar"), "foo"));
        assertTrue(containsKeywordAnyIgnoreCase(Arrays.asList("foo", "bar"), "bar"));
        assertTrue(containsKeywordAnyIgnoreCase(Arrays.asList("foo", "bar"), "foo", "bar"));
        assertTrue(containsKeywordAnyIgnoreCase(Arrays.asList("foo", "bar"), "quux", "foo"));
        assertFalse(containsKeywordAnyIgnoreCase(Arrays.asList("foo", "bar"), "baz"));
        assertTrue(containsKeywordAnyIgnoreCase(Arrays.asList("foo", "bar"), "o"));
        assertTrue(containsKeywordAnyIgnoreCase(Arrays.asList("foo", "bar"), "fOo"));
        assertTrue(containsKeywordAnyIgnoreCase(Arrays.asList("foo", "bar"), "fOo", "foO", "qux"));
        assertFalse(containsKeywordAnyIgnoreCase(new ArrayList<String>(), "o"));
        assertFalse(containsKeywordAnyIgnoreCase(Arrays.asList("foo", "bar"), new String[] {}));
        assertFalse(containsKeywordAnyIgnoreCase(Arrays.asList("foo", "bar", null), "qux", null));
    }

    // -----------------------------------------------------
    //                                           List Prefix
    //                                           -----------
    public void test_containsPrefixAll_basic() {
        assertTrue(containsPrefixAll(Arrays.asList("foo", "bar"), "fo"));
        assertTrue(containsPrefixAll(Arrays.asList("foo", "bar"), "ba"));
        assertTrue(containsPrefixAll(Arrays.asList("foo", "bar"), "fo", "ba"));
        assertFalse(containsPrefixAll(Arrays.asList("foo", "bar"), "quux", "foo"));
        assertFalse(containsPrefixAll(Arrays.asList("foo", "bar"), "baz"));
        assertFalse(containsPrefixAll(Arrays.asList("foo", "bar"), "o"));
        assertFalse(containsPrefixAll(Arrays.asList("foo", "bar"), "fO"));
        assertFalse(containsPrefixAll(new ArrayList<String>(), "o"));
        assertFalse(containsPrefixAll(Arrays.asList("foo", "bar"), new String[] {}));
        assertFalse(containsPrefixAll(Arrays.asList("foo", "bar", null), "foo", null));
    }

    public void test_containsPrefixAllIgnoreCase_basic() {
        assertTrue(containsPrefixAllIgnoreCase(Arrays.asList("foo", "bar"), "fo"));
        assertTrue(containsPrefixAllIgnoreCase(Arrays.asList("foo", "bar"), "ba"));
        assertTrue(containsPrefixAllIgnoreCase(Arrays.asList("foo", "bar"), "fo", "ba"));
        assertFalse(containsPrefixAllIgnoreCase(Arrays.asList("foo", "bar"), "quux", "foo"));
        assertFalse(containsPrefixAllIgnoreCase(Arrays.asList("foo", "bar"), "baz"));
        assertFalse(containsPrefixAllIgnoreCase(Arrays.asList("foo", "bar"), "o"));
        assertTrue(containsPrefixAllIgnoreCase(Arrays.asList("foo", "bar"), "fO"));
        assertFalse(containsPrefixAllIgnoreCase(new ArrayList<String>(), "o"));
        assertFalse(containsPrefixAllIgnoreCase(Arrays.asList("foo", "bar"), new String[] {}));
        assertFalse(containsPrefixAllIgnoreCase(Arrays.asList("foo", "bar", null), "foo", null));
    }

    public void test_containsPrefixAny_basic() {
        assertTrue(containsPrefixAny(Arrays.asList("foo", "bar"), "fo"));
        assertTrue(containsPrefixAny(Arrays.asList("foo", "bar"), "ba"));
        assertTrue(containsPrefixAny(Arrays.asList("foo", "bar"), "fo", "ba"));
        assertTrue(containsPrefixAny(Arrays.asList("foo", "bar"), "quux", "foo"));
        assertFalse(containsPrefixAny(Arrays.asList("foo", "bar"), "baz"));
        assertFalse(containsPrefixAny(Arrays.asList("foo", "bar"), "o"));
        assertFalse(containsPrefixAny(Arrays.asList("foo", "bar"), "fO"));
        assertFalse(containsPrefixAny(new ArrayList<String>(), "o"));
        assertFalse(containsPrefixAny(Arrays.asList("foo", "bar"), new String[] {}));
        assertFalse(containsPrefixAny(Arrays.asList("foo", "bar", null), "qux", null));
    }

    public void test_containsPrefixAnyIgnoreCase_basic() {
        assertTrue(containsPrefixAnyIgnoreCase(Arrays.asList("foo", "bar"), "fo"));
        assertTrue(containsPrefixAnyIgnoreCase(Arrays.asList("foo", "bar"), "ba"));
        assertTrue(containsPrefixAnyIgnoreCase(Arrays.asList("foo", "bar"), "fo", "ba"));
        assertTrue(containsPrefixAnyIgnoreCase(Arrays.asList("foo", "bar"), "quux", "foo"));
        assertFalse(containsPrefixAnyIgnoreCase(Arrays.asList("foo", "bar"), "baz"));
        assertFalse(containsPrefixAnyIgnoreCase(Arrays.asList("foo", "bar"), "o"));
        assertTrue(containsPrefixAnyIgnoreCase(Arrays.asList("foo", "bar"), "fO"));
        assertFalse(containsPrefixAnyIgnoreCase(new ArrayList<String>(), "o"));
        assertFalse(containsPrefixAnyIgnoreCase(Arrays.asList("foo", "bar"), new String[] {}));
        assertFalse(containsPrefixAnyIgnoreCase(Arrays.asList("foo", "bar", null), "qux", null));
    }

    // -----------------------------------------------------
    //                                           List Suffix
    //                                           -----------
    public void test_containsSuffixAll_basic() {
        assertTrue(containsSuffixAll(Arrays.asList("foo", "bar"), "oo"));
        assertTrue(containsSuffixAll(Arrays.asList("foo", "bar"), "ar"));
        assertTrue(containsSuffixAll(Arrays.asList("foo", "bar"), "oo", "ar"));
        assertFalse(containsSuffixAll(Arrays.asList("foo", "bar"), "quux", "foo"));
        assertFalse(containsSuffixAll(Arrays.asList("foo", "bar"), "baz"));
        assertFalse(containsSuffixAll(Arrays.asList("foo", "bar"), "f"));
        assertFalse(containsSuffixAll(Arrays.asList("foo", "bar"), "Oo"));
        assertFalse(containsSuffixAll(new ArrayList<String>(), "o"));
        assertFalse(containsSuffixAll(Arrays.asList("foo", "bar"), new String[] {}));
        assertFalse(containsSuffixAll(Arrays.asList("foo", "bar", null), "qux", null));
    }

    public void test_containsSuffixAllIgnoreCase_basic() {
        assertTrue(containsSuffixAllIgnoreCase(Arrays.asList("foo", "bar"), "oo"));
        assertTrue(containsSuffixAllIgnoreCase(Arrays.asList("foo", "bar"), "ar"));
        assertTrue(containsSuffixAllIgnoreCase(Arrays.asList("foo", "bar"), "oo", "ar"));
        assertFalse(containsSuffixAllIgnoreCase(Arrays.asList("foo", "bar"), "quux", "foo"));
        assertFalse(containsSuffixAllIgnoreCase(Arrays.asList("foo", "bar"), "baz"));
        assertFalse(containsSuffixAllIgnoreCase(Arrays.asList("foo", "bar"), "f"));
        assertTrue(containsSuffixAllIgnoreCase(Arrays.asList("foo", "bar"), "Oo"));
        assertFalse(containsSuffixAllIgnoreCase(new ArrayList<String>(), "o"));
        assertFalse(containsSuffixAllIgnoreCase(Arrays.asList("foo", "bar"), new String[] {}));
        assertFalse(containsSuffixAllIgnoreCase(Arrays.asList("foo", "bar", null), "qux", null));
    }

    public void test_containsSuffixAny_basic() {
        assertTrue(containsSuffixAny(Arrays.asList("foo", "bar"), "oo"));
        assertTrue(containsSuffixAny(Arrays.asList("foo", "bar"), "ar"));
        assertTrue(containsSuffixAny(Arrays.asList("foo", "bar"), "oo", "ar"));
        assertTrue(containsSuffixAny(Arrays.asList("foo", "bar"), "quux", "foo"));
        assertFalse(containsSuffixAny(Arrays.asList("foo", "bar"), "baz"));
        assertFalse(containsSuffixAny(Arrays.asList("foo", "bar"), "f"));
        assertFalse(containsSuffixAny(Arrays.asList("foo", "bar"), "Oo"));
        assertFalse(containsSuffixAny(new ArrayList<String>(), "o"));
        assertFalse(containsSuffixAny(Arrays.asList("foo", "bar"), new String[] {}));
        assertFalse(containsSuffixAny(Arrays.asList("foo", "bar", null), "qux", null));
    }

    public void test_containsSuffixAnyIgnoreCase_basic() {
        assertTrue(containsSuffixAnyIgnoreCase(Arrays.asList("foo", "bar"), "oo"));
        assertTrue(containsSuffixAnyIgnoreCase(Arrays.asList("foo", "bar"), "ar"));
        assertTrue(containsSuffixAnyIgnoreCase(Arrays.asList("foo", "bar"), "oo", "ar"));
        assertTrue(containsSuffixAnyIgnoreCase(Arrays.asList("foo", "bar"), "quux", "foo"));
        assertFalse(containsSuffixAnyIgnoreCase(Arrays.asList("foo", "bar"), "baz"));
        assertFalse(containsSuffixAnyIgnoreCase(Arrays.asList("foo", "bar"), "f"));
        assertTrue(containsSuffixAnyIgnoreCase(Arrays.asList("foo", "bar"), "Oo"));
        assertFalse(containsSuffixAnyIgnoreCase(new ArrayList<String>(), "o"));
        assertFalse(containsSuffixAnyIgnoreCase(Arrays.asList("foo", "bar"), new String[] {}));
        assertFalse(containsSuffixAnyIgnoreCase(Arrays.asList("foo", "bar", null), "qux", null));
    }

    // ===================================================================================
    //                                                                          StartsWith
    //                                                                          ==========
    public void test_startsWith_basic() {
        assertTrue(startsWith("foobar", "foo"));
        assertTrue(startsWith("foobar", "fo"));
        assertTrue(startsWith("foobar", "f"));
        assertTrue(startsWith("foobar", "foo", "baz"));
        assertTrue(startsWith("foobar", "baz", "foo"));
        assertFalse(startsWith("foobar", "baz", "qux"));
        assertFalse(startsWith("foobar", "o"));
        assertFalse(startsWith("foobar", new String[] {}));
        assertTrue(startsWith("foobar", ""));
        assertFalse(startsWith("foobar", "fOo"));
        assertFalse(startsWith("fOobar", "foo"));
        assertTrue(startsWith("fOobar", "fOo"));
    }

    public void test_startsWithIgnoreCase_basic() {
        assertTrue(startsWithIgnoreCase("foobar", "foo"));
        assertTrue(startsWithIgnoreCase("foobar", "fo"));
        assertTrue(startsWithIgnoreCase("foobar", "f"));
        assertFalse(startsWithIgnoreCase("foobar", "o"));
        assertFalse(startsWithIgnoreCase("foobar", new String[] {}));
        assertTrue(startsWithIgnoreCase("foobar", ""));
        assertTrue(startsWithIgnoreCase("foobar", "fOo"));
        assertTrue(startsWithIgnoreCase("fOobar", "foo"));
        assertTrue(startsWithIgnoreCase("fOobar", "fOo"));
    }

    // ===================================================================================
    //                                                                            EndsWith
    //                                                                            ========
    public void test_endsWith_basic() {
        assertTrue(endsWith("foobar", "bar"));
        assertTrue(endsWith("foobar", "ar"));
        assertTrue(endsWith("foobar", "r"));
        assertTrue(endsWith("foobar", "bar", "baz"));
        assertTrue(endsWith("foobar", "baz", "bar"));
        assertFalse(endsWith("foobar", "baz", "qux"));
        assertFalse(endsWith("foobar", "a"));
        assertFalse(endsWith("foobar", new String[] {}));
        assertTrue(endsWith("foobar", ""));
        assertFalse(endsWith("foobar", "bAr"));
        assertFalse(endsWith("foobAr", "bar"));
        assertTrue(endsWith("foobAr", "bAr"));
    }

    public void test_endsWithIgnoreCase_basic() {
        assertTrue(endsWithIgnoreCase("foobar", "bar"));
        assertTrue(endsWithIgnoreCase("foobar", "ar"));
        assertTrue(endsWithIgnoreCase("foobar", "r"));
        assertFalse(endsWithIgnoreCase("foobar", "a"));
        assertFalse(endsWithIgnoreCase("foobar", new String[] {}));
        assertTrue(endsWithIgnoreCase("foobar", ""));
        assertTrue(endsWithIgnoreCase("foobar", "bAr"));
        assertTrue(endsWithIgnoreCase("foobAr", "bar"));
        assertTrue(endsWithIgnoreCase("foobAr", "bAr"));
    }

    // ===================================================================================
    //                                                                          HasKeyword
    //                                                                          ==========
    public void test_hasKeywordAll_basic() {
        assertTrue(hasKeywordAll("foo", "foobar"));
        assertTrue(hasKeywordAll("foo", "foobar", "barfoo"));
        assertTrue(hasKeywordAll("oob", "foobar", "foobaz"));
        assertFalse(hasKeywordAll("foo", "quxbar", "bazqux"));
        assertFalse(hasKeywordAll("oob", "foObar"));
        assertFalse(hasKeywordAll("foo", new String[] {}));
    }

    public void test_hasKeywordAllIgnoreCase_basic() {
        assertTrue(hasKeywordAllIgnoreCase("foo", "foobar"));
        assertTrue(hasKeywordAllIgnoreCase("foo", "foobar", "barfoo"));
        assertTrue(hasKeywordAllIgnoreCase("oob", "foobar", "foobaz"));
        assertFalse(hasKeywordAllIgnoreCase("foo", "quxbar", "bazqux"));
        assertTrue(hasKeywordAllIgnoreCase("oob", "foObar"));
        assertFalse(hasKeywordAllIgnoreCase("foo", new String[] {}));
    }

    public void test_hasKeywordAny_basic() {
        assertTrue(hasKeywordAny("foo", "foobar"));
        assertTrue(hasKeywordAny("foo", "foobar", "quux"));
        assertTrue(hasKeywordAny("foo", "foobar", "fooqux"));
        assertFalse(hasKeywordAny("foo", "quxbar", "bazqux"));
        assertFalse(hasKeywordAny("oob", "foObar"));
        assertFalse(hasKeywordAny("foo", new String[] {}));
    }

    public void test_hasKeywordAnyIgnoreCase_basic() {
        assertTrue(hasKeywordAnyIgnoreCase("foo", "foobar"));
        assertTrue(hasKeywordAnyIgnoreCase("foo", "foobar", "quux"));
        assertTrue(hasKeywordAnyIgnoreCase("foo", "foobar", "fooqux"));
        assertFalse(hasKeywordAnyIgnoreCase("foo", "quxbar", "bazqux"));
        assertTrue(hasKeywordAnyIgnoreCase("oob", "foObar"));
        assertFalse(hasKeywordAnyIgnoreCase("foo", new String[] {}));
    }

    public void test_hasPrefixAll_basic() {
        assertTrue(hasPrefixAll("foo", "foobar"));
        assertFalse(hasPrefixAll("foo", "foobar", "barfoo"));
        assertTrue(hasPrefixAll("foo", "foobar", "fooqux"));
        assertFalse(hasPrefixAll("foo", "quxbar", "bazqux"));
        assertFalse(hasPrefixAll("foo", "foObar"));
        assertFalse(hasPrefixAll("foo", new String[] {}));
    }

    public void test_hasPrefixAllIgnoreCase_basic() {
        assertTrue(hasPrefixAllIgnoreCase("foo", "foobar"));
        assertFalse(hasPrefixAllIgnoreCase("foo", "foobar", "barfoo"));
        assertTrue(hasPrefixAllIgnoreCase("foo", "foobar", "fooqux"));
        assertFalse(hasPrefixAllIgnoreCase("foo", "quxbar", "bazqux"));
        assertTrue(hasPrefixAllIgnoreCase("foo", "foObar"));
        assertFalse(hasPrefixAllIgnoreCase("foo", new String[] {}));
    }

    public void test_hasPrefixAny_basic() {
        assertTrue(hasPrefixAny("foo", "foobar"));
        assertTrue(hasPrefixAny("foo", "foobar", "barfoo"));
        assertTrue(hasPrefixAny("foo", "foobar", "fooqux"));
        assertFalse(hasPrefixAny("foo", "quxbar", "bazqux"));
        assertFalse(hasPrefixAny("foo", "foObar"));
        assertFalse(hasPrefixAny("foo", new String[] {}));
    }

    public void test_hasPrefixAnyIgnoreCase_basic() {
        assertTrue(hasPrefixAnyIgnoreCase("foo", "foobar"));
        assertTrue(hasPrefixAnyIgnoreCase("foo", "foobar", "barfoo"));
        assertTrue(hasPrefixAnyIgnoreCase("foo", "foobar", "fooqux"));
        assertFalse(hasPrefixAnyIgnoreCase("foo", "quxbar", "bazqux"));
        assertTrue(hasPrefixAnyIgnoreCase("foo", "foObar"));
        assertFalse(hasPrefixAnyIgnoreCase("foo", new String[] {}));
    }

    public void test_hasSuffixAll_basic() {
        assertTrue(hasSuffixAll("bar", "foobar"));
        assertFalse(hasSuffixAll("bar", "barfoo", "foobar"));
        assertTrue(hasSuffixAll("bar", "foobar", "quxbar"));
        assertFalse(hasSuffixAll("bar", "quxfoo", "bazqux"));
        assertFalse(hasSuffixAll("bar", "foobaR"));
        assertFalse(hasSuffixAll("bar", new String[] {}));
    }

    public void test_hasSuffixAllIgnoreCase_basic() {
        assertTrue(hasSuffixAllIgnoreCase("bar", "foobar"));
        assertFalse(hasSuffixAllIgnoreCase("bar", "barfoo", "foobar"));
        assertTrue(hasSuffixAllIgnoreCase("bar", "foobar", "quxbar"));
        assertFalse(hasSuffixAllIgnoreCase("bar", "quxfoo", "bazqux"));
        assertTrue(hasSuffixAllIgnoreCase("bar", "foobaR"));
        assertFalse(hasSuffixAllIgnoreCase("bar", new String[] {}));
    }

    public void test_hasSuffixAny_basic() {
        assertTrue(hasSuffixAny("bar", "foobar"));
        assertTrue(hasSuffixAny("bar", "barfoo", "foobar"));
        assertTrue(hasSuffixAny("bar", "foobar", "quxbar"));
        assertFalse(hasSuffixAny("bar", "quxfoo", "bazqux"));
        assertFalse(hasSuffixAny("bar", "foobaR"));
        assertFalse(hasSuffixAny("bar", new String[] {}));
    }

    public void test_hasSuffixAnyIgnoreCase_basic() {
        assertTrue(hasSuffixAnyIgnoreCase("bar", "foobar"));
        assertTrue(hasSuffixAnyIgnoreCase("bar", "barfoo", "foobar"));
        assertTrue(hasSuffixAnyIgnoreCase("bar", "foobar", "quxbar"));
        assertFalse(hasSuffixAnyIgnoreCase("bar", "quxfoo", "bazqux"));
        assertTrue(hasSuffixAnyIgnoreCase("bar", "foobaR"));
        assertFalse(hasSuffixAnyIgnoreCase("bar", new String[] {}));
    }

    // ===================================================================================
    //                                                                               Count
    //                                                                               =====
    public void test_count_basic() {
        assertEquals(0, count("foobar", "."));
        assertEquals(1, count("foo.bar", "."));
        assertEquals(0, count("foo.bar", "Foo"));
        assertEquals(2, count("foo.bar.baz", "."));
        assertEquals(4, count(".foo.bar.baz.", "."));
        assertEquals(1, count("fooo.bar", "oo"));
    }

    public void test_countIgnoreCase_basic() {
        assertEquals(0, countIgnoreCase("foobar", "."));
        assertEquals(1, countIgnoreCase("foo.bar", "."));
        assertEquals(1, countIgnoreCase("foo.bar", "Foo"));
        assertEquals(1, countIgnoreCase("foo.bar", "foo"));
        assertEquals(2, countIgnoreCase("foo.bar.baz", "."));
        assertEquals(4, countIgnoreCase(".foo.bar.baz.", "."));
        assertEquals(1, countIgnoreCase("fooo.bar", "Oo"));
    }

    // ===================================================================================
    //                                                                              Equals
    //                                                                              ======
    public void test_equalsIgnoreCase_basic() {
        assertTrue(equalsIgnoreCase(null, (String[]) null));
        assertFalse(equalsIgnoreCase(null, ""));
        assertFalse(equalsIgnoreCase("", (String[]) null));
        assertTrue(equalsIgnoreCase("foobar", "foobar"));
        assertTrue(equalsIgnoreCase("foobar", "fooBar"));
        assertFalse(equalsIgnoreCase("foobar", "foo_bar"));
        assertFalse(equalsIgnoreCase("foobar", "foobar "));
        assertTrue(equalsIgnoreCase("foobar", "fooqux", "fooBar"));
        assertTrue(equalsIgnoreCase(null, "foo", null));
        assertTrue(equalsIgnoreCase(null, "foo", null, "bar"));
        assertTrue(equalsIgnoreCase("bar", "foo", null, "bAr"));
    }

    public void test_equalsFlexible_basic() {
        assertTrue(equalsFlexible(null, (String[]) null));
        assertFalse(equalsFlexible(null, ""));
        assertFalse(equalsFlexible("", (String[]) null));
        assertTrue(equalsFlexible("foobar", "foobar"));
        assertTrue(equalsFlexible("foobar", "fooBar"));
        assertTrue(equalsFlexible("foobar", "foo_bar"));
        assertFalse(equalsFlexible("foobar", "foobar "));
        assertTrue(equalsFlexible("foobar", "barbar", "foobar"));
    }

    public void test_equalsFlexibleTrimmed_basic() {
        assertTrue(equalsFlexibleTrimmed(null, (String[]) null));
        assertFalse(equalsFlexibleTrimmed(null, ""));
        assertFalse(equalsFlexibleTrimmed("", (String[]) null));
        assertTrue(equalsFlexibleTrimmed("foobar", "foobar"));
        assertTrue(equalsFlexibleTrimmed("foobar", "fooBar"));
        assertTrue(equalsFlexibleTrimmed("foobar", "foo_bar"));
        assertTrue(equalsFlexibleTrimmed("foobar", "foobar "));
        assertTrue(equalsFlexibleTrimmed("foobar", "barbar ", "foobar "));
        assertFalse(equalsFlexibleTrimmed("foobar", "barbar ", "quxqux "));
    }

    public void test_equalsPlain_basic() {
        assertTrue(equalsPlain(null, (String[]) null));
        assertFalse(equalsPlain(null, ""));
        assertFalse(equalsPlain("", (String[]) null));
        assertTrue(equalsPlain("foobar", "foobar"));
        assertFalse(equalsPlain("foobar", "fooBar"));
        assertFalse(equalsPlain("foobar", "foo_bar"));
        assertFalse(equalsPlain("foobar", "foobar "));
        assertTrue(equalsPlain("foobar", "barbar", "foobar"));
        assertTrue(equalsPlain(null, "foo", null));
        assertTrue(equalsPlain(null, "foo", null, "bar"));
        assertTrue(equalsPlain("bar", "foo", null, "bar"));
    }

    // ===================================================================================
    //                                                                    Connect & Remove
    //                                                                    ================
    public void test_connectByDelimiter_basic() {
        assertEquals("foo,bar", connectByDelimiter(newArrayList("foo", "bar"), ","));
        assertEquals("foo, bar", connectByDelimiter(newArrayList("foo", "bar"), ", "));
        assertEquals("foo bar", connectByDelimiter(newArrayList("foo", "bar"), " "));
        assertEquals("foo;:bar;:qux", connectByDelimiter(newArrayList("foo", "bar", "qux"), ";:"));
        assertEquals("foo,,qux", connectByDelimiter(newArrayList("foo", "", "qux"), ","));
        assertEquals("foo,null,qux", connectByDelimiter(newArrayList("foo", null, "qux"), ","));
        assertEquals("", connectByDelimiter(new ArrayList<String>(), ","));
    }

    public void test_connectByDelimiterQuoted_basic() {
        assertEquals("'foo','bar'", connectByDelimiterQuoted(newArrayList("foo", "bar"), ",", "'"));
        assertEquals("'foo', 'bar'", connectByDelimiterQuoted(newArrayList("foo", "bar"), ", ", "'"));
        assertEquals("\"foo\" \"bar\"", connectByDelimiterQuoted(newArrayList("foo", "bar"), " ", "\""));
        assertEquals("foo;:bar;:qux", connectByDelimiterQuoted(newArrayList("foo", "bar", "qux"), ";:", ""));
        assertEquals("'foo','','qux'", connectByDelimiterQuoted(newArrayList("foo", "", "qux"), ",", "'"));
        assertEquals("'foo','null','qux'", connectByDelimiterQuoted(newArrayList("foo", null, "qux"), ",", "'"));
        assertEquals("", connectByDelimiterQuoted(new ArrayList<String>(), ",", "'"));
    }

    public void test_connectPrefix_basic() {
        assertEquals("foo", connectPrefix("foo", null, "."));
        assertEquals("foo", connectPrefix("foo", "", "."));
        assertEquals("foo", connectPrefix("foo", " ", "."));
        assertEquals("bar.foo", connectPrefix("foo", "bar", "."));
        assertEquals("bar/foo", connectPrefix("foo", "bar", "/"));
    }

    public void test_connectSuffix_basic() {
        assertEquals("foo", connectSuffix("foo", null, "."));
        assertEquals("foo", connectSuffix("foo", "", "."));
        assertEquals("foo", connectSuffix("foo", " ", "."));
        assertEquals("foo.bar", connectSuffix("foo", "bar", "."));
        assertEquals("foo/bar", connectSuffix("foo", "bar", "/"));
    }

    public void test_removePrefix_basic() {
        assertEquals("oo", removePrefix("foo", "f"));
        assertEquals("o", removePrefix("foo", "fo"));
        assertEquals("", removePrefix("foo", "foo"));
        assertEquals("foo", removePrefix("foo", "bo"));
        assertEquals("foo", removePrefix("foo", "Fo"));
        assertEquals("foo", removePrefix("foo", "o"));
    }

    public void test_removePrefixIgnoreCase_basic() {
        assertEquals("oo", removePrefixIgnoreCase("foo", "f"));
        assertEquals("o", removePrefixIgnoreCase("foo", "fo"));
        assertEquals("", removePrefixIgnoreCase("foo", "foo"));
        assertEquals("foo", removePrefixIgnoreCase("foo", "bo"));
        assertEquals("o", removePrefixIgnoreCase("foo", "Fo"));
        assertEquals("foo", removePrefixIgnoreCase("foo", "o"));
    }

    public void test_removeSuffix_basic() {
        assertEquals("fo", removeSuffix("foo", "o"));
        assertEquals("f", removeSuffix("foo", "oo"));
        assertEquals("", removeSuffix("foo", "foo"));
        assertEquals("foo", removeSuffix("foo", "O"));
        assertEquals("foo", removeSuffix("foo", "bo"));
    }

    public void test_removeSuffixIgnoreCase_basic() {
        assertEquals("fo", removeSuffixIgnoreCase("foo", "o"));
        assertEquals("f", removeSuffixIgnoreCase("foo", "oo"));
        assertEquals("", removeSuffixIgnoreCase("foo", "foo"));
        assertEquals("fo", removeSuffixIgnoreCase("foo", "O"));
        assertEquals("foo", removeSuffixIgnoreCase("foo", "bo"));
    }

    // ===================================================================================
    //                                                                  Quotation Handling
    //                                                                  ==================
    public void test_quoteAnything_basic() {
        assertEquals("foo%", unquoteAnything("foo%", "%"));
        assertEquals("%foo", unquoteAnything("%foo", "%"));
        assertEquals("foo", unquoteAnything("%foo%", "%"));
        assertEquals("foo", unquoteAnything("<foo<", "<"));
        assertEquals("%foo", unquoteAnything("%%foo%", "%"));
        assertEquals("foo", unquoteAnything("List<foo>", "List<", ">"));
        assertEquals("List<foo>", unquoteAnything("List<List<foo>>", "List<", ">"));
    }

    public void test_quoteDouble_basic() {
        assertEquals("\"\"", quoteDouble(""));
        assertEquals("\"\"\"\"", quoteDouble("\"\""));
        assertEquals("\" \"", quoteDouble(" "));
        assertEquals("\"foo\"", quoteDouble("foo"));
        assertEquals("\"\"foo\"\"", quoteDouble("\"foo\""));
        assertEquals("\"'foo'\"", quoteDouble("'foo'"));
        assertEquals("\"\"foo\"", quoteDouble("\"foo"));
    }

    public void test_quoteSingle_basic() {
        assertEquals("''", quoteSingle(""));
        assertEquals("''''", quoteSingle("''"));
        assertEquals("' '", quoteSingle(" "));
        assertEquals("'foo'", quoteSingle("foo"));
        assertEquals("''foo''", quoteSingle("'foo'"));
        assertEquals("'\"foo\"'", quoteSingle("\"foo\""));
        assertEquals("''foo'", quoteSingle("'foo"));
    }

    public void test_unquoteDouble_basic() {
        assertEquals("", unquoteDouble(""));
        assertEquals("", unquoteDouble("\"\""));
        assertEquals("f", unquoteDouble("\"f\""));
        assertEquals("foo", unquoteDouble("\"foo\""));
        assertEquals("foo", unquoteDouble("foo"));
        assertEquals("'foo'", unquoteDouble("'foo'"));
        assertEquals("\"foo", unquoteDouble("\"foo"));
        assertEquals("'foo'", unquoteDouble("\"'foo'\""));
        assertEquals("\"foo\"", unquoteDouble("\"\"foo\"\""));
    }

    public void test_unquoteSingle_basic() {
        assertEquals("", unquoteSingle(""));
        assertEquals("", unquoteSingle("''"));
        assertEquals("f", unquoteSingle("'f'"));
        assertEquals("foo", unquoteSingle("'foo'"));
        assertEquals("foo", unquoteSingle("foo"));
        assertEquals("\"foo\"", unquoteSingle("\"foo\""));
        assertEquals("'foo", unquoteSingle("'foo"));
        assertEquals("\"foo\"", unquoteSingle("'\"foo\"'"));
        assertEquals("'foo'", unquoteSingle("''foo''"));
    }

    // ===================================================================================
    //                                                                  Delimiter Handling
    //                                                                  ==================
    public void test_extractDelimiterList_basic() {
        // ## Arrange ##
        String str = "foo--bar--baz--and";

        // ## Act ##
        List<DelimiterInfo> list = extractDelimiterList(str, "--");

        // ## Assert ##
        assertEquals(3, list.size());
        assertEquals(3, list.get(0).getBeginIndex());
        assertEquals(8, list.get(1).getBeginIndex());
        assertEquals(13, list.get(2).getBeginIndex());
        assertEquals(5, list.get(0).getEndIndex());
        assertEquals(10, list.get(1).getEndIndex());
        assertEquals(15, list.get(2).getEndIndex());
        assertEquals("foo", list.get(0).substringInterspaceToPrevious());
        assertEquals("bar", list.get(1).substringInterspaceToPrevious());
        assertEquals("baz", list.get(2).substringInterspaceToPrevious());
        assertEquals("bar", list.get(0).substringInterspaceToNext());
        assertEquals("baz", list.get(1).substringInterspaceToNext());
        assertEquals("and", list.get(2).substringInterspaceToNext());
    }

    public void test_extractDelimiterList_bothSide() {
        // ## Arrange ##
        String str = "--foo--bar--baz--and--";

        // ## Act ##
        List<DelimiterInfo> list = extractDelimiterList(str, "--");

        // ## Assert ##
        assertEquals(5, list.size());
        assertEquals(0, list.get(0).getBeginIndex());
        assertEquals(5, list.get(1).getBeginIndex());
        assertEquals(10, list.get(2).getBeginIndex());
        assertEquals(15, list.get(3).getBeginIndex());
        assertEquals(20, list.get(4).getBeginIndex());
    }

    public void test_extractDelimiterList_noDelimiter() {
        // ## Arrange ##
        String str = "foo-bar-baz-and";

        // ## Act ##
        List<DelimiterInfo> list = extractDelimiterList(str, "--");

        // ## Assert ##
        assertEquals(0, list.size());
    }

    public void test_delimit_basic() {
        // ## Arrange ##
        List<String> list = Arrays.asList("sea", "land", "iks");

        // ## Act ##
        // ## Assert ##
        assertEquals("sea, land, iks", delimit(list, el -> el, ", "));
        assertEquals("s|l|i", delimit(list, el -> String.valueOf(el.charAt(0)), "|"));
        assertEquals("3, 4, 3", delimit(list, el -> String.valueOf(el.length()), ", "));
        assertEquals("sea", delimit(Arrays.asList("sea"), el -> el, ", "));
        assertEquals("", delimit(new ArrayList<String>(), el -> el, ", "));
        assertEquals("sea, null", delimit(Arrays.asList("sea", null), el -> el, ", "));
    }

    // ===================================================================================
    //                                                                      Scope Handling
    //                                                                      ==============
    public void test_extractScopeFirst_content() {
        assertEquals("BAR", extractScopeFirst("FOObeginBARendDODO", "begin", "end").getContent());
        assertEquals("BAR", extractScopeFirst("FOObeginBARend", "begin", "end").getContent());
        assertEquals("BAR", extractScopeFirst("beginBARendDODO", "begin", "end").getContent());
        assertEquals(null, extractScopeFirst("beginBARedDODO", "begin", "end"));
        assertEquals(null, extractScopeFirst("begnBARendDODO", "begin", "end"));
        assertEquals(null, extractScopeFirst("begnBARedDODO", "begin", "end"));
        assertEquals("9", extractScopeFirst("get(9)", "get(", ")").getContent());
        assertEquals("99", extractScopeFirst("get(99)", "get(", ")").getContent());
        assertEquals(" 99 ", extractScopeFirst("get( 99 )", "get(", ")").getContent()); // not trimmed
        assertEquals("foo", extractScopeFirst("get(foo)-get(bar)", "get(", ")").getContent());
        assertEquals("foo", extractScopeFirst("@foo@-get@bar@", "@", "@").getContent());
    }

    public void test_extractScopeFirst_scope() {
        assertEquals("beginBARend", extractScopeFirst("FOObeginBARendDODO", "begin", "end").getScope());
        assertEquals("beginBARend", extractScopeFirst("FOObeginBARend", "begin", "end").getScope());
        assertEquals("beginBARend", extractScopeFirst("beginBARendDODO", "begin", "end").getScope());
        assertEquals(null, extractScopeFirst("beginBARedDODO", "begin", "end"));
        assertEquals(null, extractScopeFirst("begnBARendDODO", "begin", "end"));
        assertEquals(null, extractScopeFirst("begnBARedDODO", "begin", "end"));
        assertEquals("get(9)", extractScopeFirst("xget(9)x", "get(", ")").getScope());
        assertEquals("get(99)", extractScopeFirst("xget(99)x", "get(", ")").getScope());
        assertEquals("get( 99 )", extractScopeFirst("xget( 99 )x", "get(", ")").getScope()); // not trimmed
        assertEquals("get(foo)", extractScopeFirst("get(foo)-get(bar)", "get(", ")").getScope());
        assertEquals("@foo@", extractScopeFirst("@foo@-get@bar@", "@", "@").getScope());
    }

    public void test_extractScopeFirst_various() {
        ScopeInfo scope = extractScopeFirst("FOObeginBARendDODO", "begin", "end");
        log("baseString: " + scope.getBaseString());
        log("scope: " + scope.getScope());
        log("content: " + scope.getContent());
        log("previous: " + scope.getPrevious());
        log("next: " + scope.getNext());
        log("substringInterspaceToPrevious(): " + scope.substringInterspaceToPrevious());
        log("substringInterspaceToNext(): " + scope.substringInterspaceToNext());
        log("substringScopeToPrevious(): " + scope.substringScopeToPrevious());
        log("substringScopeToNext(): " + scope.substringScopeToNext());
        log("replaceContentOnBaseString(): " + scope.replaceContentOnBaseString("SEA"));
        log("replaceContentOnBaseString(): " + scope.replaceContentOnBaseString("A", "B"));
        log("replaceInterspaceOnBaseString(): " + scope.replaceInterspaceOnBaseString("O", "R"));
        assertEquals("FOObeginBARendDODO", scope.getBaseString());
        assertEquals("beginBARend", scope.getScope());
        assertEquals("BAR", scope.getContent());
        assertNull(scope.getPrevious());
        assertNull(scope.getNext());
        assertEquals("FOO", scope.substringInterspaceToPrevious());
        assertEquals("DODO", scope.substringInterspaceToNext());
        assertEquals("FOObeginBARend", scope.substringScopeToPrevious());
        assertEquals("beginBARendDODO", scope.substringScopeToNext());
        assertEquals("FOObeginSEAendDODO", scope.replaceContentOnBaseString("SEA"));
        assertEquals("FOObeginBBRendDODO", scope.replaceContentOnBaseString("A", "B"));
        assertEquals("FRRbeginBARendDRDR", scope.replaceInterspaceOnBaseString("O", "R"));
    }

    public void test_extractScopeList_basic() {
        // ## Arrange ##
        String str = "baz/*BEGIN*/where /*FOR pmb*/ /*FIRST 'foo'*/member.../*END FOR*//* END */bar";

        // ## Act ##
        List<ScopeInfo> list = extractScopeList(str, "/*", "*/");

        // ## Assert ##
        assertEquals(5, list.size());
        assertEquals(str.indexOf("/*BEGIN*/"), list.get(0).getBeginIndex());
        assertEquals(str.indexOf("/*FOR pmb*/"), list.get(1).getBeginIndex());
        assertEquals(str.indexOf("/*FIRST 'foo'*/"), list.get(2).getBeginIndex());
        assertEquals(str.indexOf("/*END FOR*/"), list.get(3).getBeginIndex());
        assertEquals(str.indexOf("/* END */"), list.get(4).getBeginIndex());
        assertEquals(str.indexOf("/*BEGIN*/") + "/*BEGIN*/".length(), list.get(0).getEndIndex());
        assertEquals(str.indexOf("/*FOR pmb*/") + "/*FOR pmb*/".length(), list.get(1).getEndIndex());
        assertEquals(str.indexOf("/*FIRST 'foo'*/") + "/*FIRST 'foo'*/".length(), list.get(2).getEndIndex());
        assertEquals(str.indexOf("/*END FOR*/") + "/*END FOR*/".length(), list.get(3).getEndIndex());
        assertEquals(str.indexOf("/* END */") + "/* END */".length(), list.get(4).getEndIndex());
        assertEquals("BEGIN", list.get(0).getContent());
        assertEquals("FOR pmb", list.get(1).getContent());
        assertEquals("FIRST 'foo'", list.get(2).getContent());
        assertEquals("END FOR", list.get(3).getContent());
        assertEquals(" END ", list.get(4).getContent()); // not trimmed
        assertEquals("/*BEGIN*/", list.get(0).getScope());
        assertEquals("/*FOR pmb*/", list.get(1).getScope());
        assertEquals("/*FIRST 'foo'*/", list.get(2).getScope());
        assertEquals("/*END FOR*/", list.get(3).getScope());
        assertEquals("/* END */", list.get(4).getScope()); // not trimmed
        assertEquals("baz", list.get(0).substringInterspaceToPrevious());
        assertEquals("where ", list.get(1).substringInterspaceToPrevious());
        assertEquals(" ", list.get(2).substringInterspaceToPrevious());
        assertEquals("member...", list.get(3).substringInterspaceToPrevious());
        assertEquals("", list.get(4).substringInterspaceToPrevious());
        assertEquals("where ", list.get(0).substringInterspaceToNext());
        assertEquals(" ", list.get(1).substringInterspaceToNext());
        assertEquals("member...", list.get(2).substringInterspaceToNext());
        assertEquals("", list.get(3).substringInterspaceToNext());
        assertEquals("bar", list.get(4).substringInterspaceToNext());
        assertEquals("baz/*BEGIN*/", list.get(0).substringScopeToPrevious());
        assertEquals("/*BEGIN*/where /*FOR pmb*/", list.get(1).substringScopeToPrevious());
        assertEquals("/*FOR pmb*/ /*FIRST 'foo'*/", list.get(2).substringScopeToPrevious());
        assertEquals("/*FIRST 'foo'*/member.../*END FOR*/", list.get(3).substringScopeToPrevious());
        assertEquals("/*END FOR*//* END */", list.get(4).substringScopeToPrevious());
        assertEquals("/*BEGIN*/where /*FOR pmb*/", list.get(0).substringScopeToNext());
        assertEquals("/*FOR pmb*/ /*FIRST 'foo'*/", list.get(1).substringScopeToNext());
        assertEquals("/*FIRST 'foo'*/member.../*END FOR*/", list.get(2).substringScopeToNext());
        assertEquals("/*END FOR*//* END */", list.get(3).substringScopeToNext());
        assertEquals("/* END */bar", list.get(4).substringScopeToNext());
    }

    public void test_extractScopeList_replaceContentOnBaseString() {
        // ## Arrange ##
        String str = "/*foo*/foo/*bar*/bar/*foobarbaz*/";

        // ## Act ##
        List<ScopeInfo> list = extractScopeList(str, "/*", "*/");

        // ## Assert ##
        ScopeInfo scope = list.get(1);
        String baseString1 = scope.replaceContentOnBaseString("foo", "jflute");
        assertEquals("/*jflute*/foo/*bar*/bar/*jflutebarbaz*/", baseString1);
        String baseString2 = scope.replaceContentOnBaseString("*", "jflute");
        assertEquals(str, baseString2); // marks no change
        assertEquals(str, scope.getBaseString()); // no change
    }

    public void test_extractScopeList_replaceInterspaceOnBaseString() {
        // ## Arrange ##
        String str = "/*foo*/foo/*bar*/bar/*foobarbaz*/";

        // ## Act ##
        List<ScopeInfo> list = extractScopeList(str, "/*", "*/");

        // ## Assert ##
        ScopeInfo scope = list.get(1);
        String baseString1 = scope.replaceInterspaceOnBaseString("foo", "jflute");
        assertEquals("/*foo*/jflute/*bar*/bar/*foobarbaz*/", baseString1);
        String baseString2 = scope.replaceInterspaceOnBaseString("*", "jflute");
        assertEquals(str, baseString2); // marks no change
        assertEquals(str, scope.getBaseString()); // no change
    }

    public void test_extractScopeList_sameMark() {
        // ## Arrange ##
        String str = "baz@@BEGIN@@where @@FOR pmb@@ @@FIRST 'foo'@@member...@@END FOR@@@@ END @@bar";

        // ## Act ##
        List<ScopeInfo> list = extractScopeList(str, "@@", "@@");

        // ## Assert ##
        assertEquals(5, list.size());
        assertEquals(str.indexOf("@@BEGIN@@"), list.get(0).getBeginIndex());
        assertEquals(str.indexOf("@@FOR pmb@@"), list.get(1).getBeginIndex());
        assertEquals(str.indexOf("@@FIRST 'foo'@@"), list.get(2).getBeginIndex());
        assertEquals(str.indexOf("@@END FOR@@"), list.get(3).getBeginIndex());
        assertEquals(str.indexOf("@@ END @@"), list.get(4).getBeginIndex());
        assertEquals(str.indexOf("@@BEGIN@@") + "@@BEGIN@@".length(), list.get(0).getEndIndex());
        assertEquals(str.indexOf("@@FOR pmb@@") + "@@FOR pmb@@".length(), list.get(1).getEndIndex());
        assertEquals(str.indexOf("@@FIRST 'foo'@@") + "@@FIRST 'foo'@@".length(), list.get(2).getEndIndex());
        assertEquals(str.indexOf("@@END FOR@@") + "@@END FOR@@".length(), list.get(3).getEndIndex());
        assertEquals(str.indexOf("@@ END @@") + "@@ END @@".length(), list.get(4).getEndIndex());
        assertEquals("BEGIN", list.get(0).getContent());
        assertEquals("FOR pmb", list.get(1).getContent());
        assertEquals("FIRST 'foo'", list.get(2).getContent());
        assertEquals("END FOR", list.get(3).getContent());
        assertEquals(" END ", list.get(4).getContent()); // not trimmed
        assertEquals("@@BEGIN@@", list.get(0).getScope());
        assertEquals("@@FOR pmb@@", list.get(1).getScope());
        assertEquals("@@FIRST 'foo'@@", list.get(2).getScope());
        assertEquals("@@END FOR@@", list.get(3).getScope());
        assertEquals("@@ END @@", list.get(4).getScope()); // not trimmed
        assertEquals("FOR pmb", list.get(0).getNext().getContent());
        assertEquals("FIRST 'foo'", list.get(1).getNext().getContent());
        assertEquals("END FOR", list.get(2).getNext().getContent());
        assertEquals(" END ", list.get(3).getNext().getContent());
        assertEquals(null, list.get(4).getNext());
        assertEquals("where ", list.get(0).substringInterspaceToNext());
        assertEquals(" ", list.get(1).substringInterspaceToNext());
        assertEquals("member...", list.get(2).substringInterspaceToNext());
        assertEquals("", list.get(3).substringInterspaceToNext());
        assertEquals("bar", list.get(4).substringInterspaceToNext());
    }

    public void test_extractScopeWide_content() {
        assertEquals("BAR", extractScopeWide("FOObeginBARendDODO", "begin", "end").getContent());
        assertEquals("BAR", extractScopeWide("FOObeginBARend", "begin", "end").getContent());
        assertEquals("BAR", extractScopeWide("beginBARendDODO", "begin", "end").getContent());
        assertEquals(null, extractScopeWide("beginBARedDODO", "begin", "end"));
        assertEquals(null, extractScopeWide("begnBARendDODO", "begin", "end"));
        assertEquals(null, extractScopeWide("begnBARedDODO", "begin", "end"));
        assertEquals("9", extractScopeWide("get(9)", "get(", ")").getContent());
        assertEquals("99", extractScopeWide("get(99)", "get(", ")").getContent());
        assertEquals(" 99 ", extractScopeWide("get( 99 )", "get(", ")").getContent()); // not trimmed
        assertEquals("foo)-get(bar", extractScopeWide("get(foo)-get(bar)", "get(", ")").getContent());
        assertEquals("foo@-get@bar", extractScopeWide("@foo@-get@bar@", "@", "@").getContent());
        assertEquals("foo", extractScopeWide("FIRST 'foo'", "'", "'").getContent());
        assertEquals("f'o'o", extractScopeWide("FIRST 'f'o'o'", "'", "'").getContent());
    }

    // ===================================================================================
    //                                                                    Initial Handling
    //                                                                    ================
    public void test_initBeansProp_basic() {
        assertEquals("fooName", initBeansProp("FooName"));
        assertEquals("fooName", initBeansProp("fooName"));
        assertEquals("BFooName", initBeansProp("BFooName"));
        assertEquals("bFooName", initBeansProp("bFooName"));
        assertEquals("bbFooName", initBeansProp("bbFooName"));
        assertEquals("f", initBeansProp("f"));
        assertEquals("f", initBeansProp("F"));
        assertEquals("FOO_NAME", initBeansProp("FOO_NAME"));
        assertEquals("foo_name", initBeansProp("foo_name"));
    }

    // ===================================================================================
    //                                                                       Name Handling
    //                                                                       =============
    public void test_camelize_basic() {
        assertEquals("FooName", DfStringUtil.camelize("FOO_NAME"));
        assertEquals("FooName", DfStringUtil.camelize("foo_name"));
        assertEquals("FooNameBar", DfStringUtil.camelize("foo_nameBar"));
        assertEquals("FooBar", DfStringUtil.camelize("foo_Bar"));
        assertEquals("FooNBar", camelize("foo_nBar"));
        assertEquals("FooNBar", DfStringUtil.camelize("FOO_nBar"));
        assertEquals("Foo", DfStringUtil.camelize("FOO"));
        assertEquals("FooName", DfStringUtil.camelize("FooName"));
        assertEquals("FName", DfStringUtil.camelize("FName"));
        assertEquals("FooName", DfStringUtil.camelize("foo__name"));
        assertEquals("FooName", DfStringUtil.camelize("FOO _ NAME"));
        assertEquals("FooNa me", DfStringUtil.camelize("FOO _ NA ME"));
    }

    public void test_camelize_delimiters() {
        assertEquals("FooName", DfStringUtil.camelize("FOO_NAME", "_"));
        assertEquals("FooNaMe", DfStringUtil.camelize("foo_na-me", "_", "-"));
        assertEquals("FooNaMeBarId", DfStringUtil.camelize("foo_na-me_bar@id", "_", "-", "@"));
        assertEquals("FooNaMeBId", DfStringUtil.camelize("foo_na-me_b@id", "_", "-", "@"));
        assertEquals("FooNaMeBarId", DfStringUtil.camelize("FOO_NA-ME_BAR@ID", "_", "-", "@"));
        assertEquals("FooName", DfStringUtil.camelize("foo--name", "-"));
        assertEquals("FooName", DfStringUtil.camelize("foo-_@name", "-", "_", "@"));
        assertEquals("FooNaMe", DfStringUtil.camelize("FOO _ NA - ME", "_", "-"));
        assertEquals("FooNa - me", DfStringUtil.camelize("FOO _ NA - ME", "_"));
        assertEquals("FooNameBarId", DfStringUtil.camelize("FOO NAME BAR ID", " "));
    }

    public void test_decamelize_basic() {
        assertEquals("FOO_NAME", DfStringUtil.doDecamelize("FooName", "_"));
        assertEquals("FOO_NAME", DfStringUtil.doDecamelize("fooName", "_"));
        assertEquals("FOO_BAR_NAME", DfStringUtil.doDecamelize("FooBarName", "_"));
        assertEquals("FOO_BAR_NAME", DfStringUtil.doDecamelize("fooBarName", "_"));
        assertEquals("F", DfStringUtil.decamelize("f"));
        assertEquals("FOO_NAME_BAR", DfStringUtil.decamelize("FOO_NameBar"));
        assertEquals("FOO_NAME_BAR", DfStringUtil.decamelize("foo_NameBar"));
        assertEquals("FOO_NAME", DfStringUtil.decamelize("FOO_NAME"));
        assertEquals("FOO_NAME", DfStringUtil.decamelize("foo_name"));
    }

    public void test_decamelize_delimiter() {
        assertEquals("FOO-NAME", DfStringUtil.decamelize("FooName", "-"));
        assertEquals("FOO@NAME", DfStringUtil.decamelize("fooName", "@"));
        assertEquals("FOO@BAR@NAME", DfStringUtil.decamelize("fooBarName", "@"));
        assertEquals("F", DfStringUtil.decamelize("f", "_"));
        assertEquals("FOO_*NAME*BAR", DfStringUtil.decamelize("FOO_NameBar", "*"));
    }

    // ===================================================================================
    //                                                                        SQL Handling
    //                                                                        ============
    public void test_removeEmptyLine_basic() {
        // ## Arrange ##
        String sql = "aaaa\r\n";
        sql = sql + "bbbb\r\n";
        sql = sql + "--\r\n";
        sql = sql + "\r\n";
        sql = sql + "\n";
        sql = sql + "cccc\r\n";

        // ## Act ##
        String actual = removeEmptyLine(sql);

        // ## Assert ##
        assertEquals("aaaa\nbbbb\n--\ncccc", actual);
    }

    public void test_removeBlockComment_basic() {
        // ## Arrange ##
        String sql = "baz/*BEGIN*/where /*FOR pmb*/ /*FIRST 'foo'*/member.../*END FOR*//* END */bar";

        // ## Act ##
        String actual = removeBlockComment(sql);

        // ## Assert ##
        assertEquals("bazwhere  member...bar", actual);
    }

    public void test_removeBlockComment_noComment() {
        // ## Arrange ##
        String sql = "barbaz";

        // ## Act ##
        String actual = removeBlockComment(sql);

        // ## Assert ##
        assertEquals("barbaz", actual);
    }

    public void test_removeLineComment_basic() throws Exception {
        // ## Arrange ##
        String sql = "aaa\n";
        sql = sql + "bbb\n";
        sql = sql + "--\n";
        sql = sql + "ccc -- foo\n";
        sql = sql + "ddd /* -- foo */\n";
        sql = sql + "eee\n";

        // ## Act ##
        String actual = removeLineComment(sql);

        // ## Assert ##
        log(actual);
        assertEquals("aaa\nbbb\nccc \nddd /* -- foo */\neee\n", actual);
    }

    public void test_removeLineComment_Lf() throws Exception {
        // ## Arrange ##
        String sql = "aaaa\n";
        sql = sql + "bbbb\n";
        sql = sql + "--\n";
        sql = sql + "cccc\n";

        // ## Act ##
        String actual = removeLineComment(sql);

        // ## Assert ##
        log(actual);
        assertEquals("aaaa\nbbbb\ncccc\n", actual);
    }

    public void test_removeLineComment_CrLf() throws Exception {
        String sql = "aaaa\r\n";
        sql = sql + "bbbb\r\n";
        sql = sql + "--\r\n";
        sql = sql + "cccc\r\n";
        String actual = DfStringUtil.removeLineComment(sql);
        log(actual);
        assertFalse(actual.contains("--"));
        assertFalse(actual.contains("\r"));
        assertEquals("aaaa\nbbbb\ncccc\n", actual);
    }

    // ===================================================================================
    //                                                                     Indent Handling
    //                                                                     ===============
    public void test_indent_basic() {
        assertEquals("   ", indent(3));
        assertEquals("    ", indent(4));
        assertEquals("  foo", indent(2, "foo"));
        assertEquals("  foo\n  bar\n  qux", indent(2, "foo\nbar\nqux"));
        assertEquals("  foo\n  bar\n  qux", indent(2, "foo\r\nbar\r\nqux"));
        assertEquals("  foo\n  bar\n  qux\n  ", indent(2, "foo\nbar\nqux\n"));
        assertEquals("  \n  foo\n  bar\n  qux\n  ", indent(2, "\nfoo\nbar\nqux\n"));
    }

    // ===================================================================================
    //                                                                  Character Handling
    //                                                                  ==================
    public void test_isAlphabetHarfAll_basic() throws Exception {
        assertTrue(isAlphabetHarfAll("ABCDEFGHIJKLMONPQRSTUVWXYZ"));
        assertTrue(isAlphabetHarfAll("A"));
        assertTrue(isAlphabetHarfAll("BL"));
        assertTrue(isAlphabetHarfAll("ABCDEFGHIJKLMONPQRSTUVWXYZ".toLowerCase()));
        assertTrue(isAlphabetHarfAll("a"));
        assertTrue(isAlphabetHarfAll("bbbOoo"));
        assertFalse(isAlphabetHarfAll("ab2"));
        assertFalse(isAlphabetHarfAll("-ab"));
        assertFalse(isAlphabetHarfAll(""));
        assertFalse(isAlphabetHarfAll(" "));
        assertFalse(isAlphabetHarfAll(" Ab"));
    }

    public void test_isAlphabetHarfLowerAll_basic() throws Exception {
        assertFalse(isAlphabetHarfLowerAll("ABCDEFGHIJKLMONPQRSTUVWXYZ"));
        assertFalse(isAlphabetHarfLowerAll("A"));
        assertFalse(isAlphabetHarfLowerAll("BL"));
        assertTrue(isAlphabetHarfLowerAll("ABCDEFGHIJKLMONPQRSTUVWXYZ".toLowerCase()));
        assertTrue(isAlphabetHarfLowerAll("a"));
        assertFalse(isAlphabetHarfLowerAll("bbbOoo"));
        assertFalse(isAlphabetHarfLowerAll("ab2"));
        assertFalse(isAlphabetHarfLowerAll("-ab"));
        assertFalse(isAlphabetHarfLowerAll(""));
        assertFalse(isAlphabetHarfLowerAll(" "));
        assertFalse(isAlphabetHarfLowerAll(" a"));
    }

    public void test_isAlphabetHarfUpperAll_basic() throws Exception {
        assertTrue(isAlphabetHarfUpperAll("ABCDEFGHIJKLMONPQRSTUVWXYZ"));
        assertTrue(isAlphabetHarfUpperAll("A"));
        assertTrue(isAlphabetHarfUpperAll("BL"));
        assertFalse(isAlphabetHarfUpperAll("ABCDEFGHIJKLMONPQRSTUVWXYZ".toLowerCase()));
        assertFalse(isAlphabetHarfUpperAll("a"));
        assertFalse(isAlphabetHarfUpperAll("bbbOoo"));
        assertFalse(isAlphabetHarfUpperAll("ab2"));
        assertFalse(isAlphabetHarfUpperAll("-ab"));
        assertFalse(isAlphabetHarfUpperAll(""));
        assertFalse(isAlphabetHarfUpperAll(" "));
        assertFalse(isAlphabetHarfUpperAll(" A"));
    }

    public void test_isNumberHarfAll_basic() throws Exception {
        assertTrue(isNumberHarfAll("0123456789"));
        assertTrue(isNumberHarfAll("0"));
        assertTrue(isNumberHarfAll("99"));
        assertFalse(isNumberHarfAll("9a9"));
        assertFalse(isNumberHarfAll("-1"));
        assertFalse(isNumberHarfAll("1.1"));
        assertFalse(isNumberHarfAll(""));
        assertFalse(isNumberHarfAll(" "));
        assertFalse(isNumberHarfAll(" 1"));
    }

    public void test_isAlphabetNumberHarfAll_basic() throws Exception {
        assertTrue(isAlphabetNumberHarfAll("ABCDEFGHIJKLMONPQRSTUVWXYZ"));
        assertTrue(isAlphabetNumberHarfAll("A"));
        assertTrue(isAlphabetNumberHarfAll("BL"));
        assertTrue(isAlphabetNumberHarfAll("ABCDEFGHIJKLMONPQRSTUVWXYZ".toLowerCase()));
        assertTrue(isAlphabetNumberHarfAll("a"));
        assertTrue(isAlphabetNumberHarfAll("bbbOoo"));
        assertTrue(isAlphabetNumberHarfAll("ab2"));
        assertTrue(isAlphabetNumberHarfAll("234789"));
        assertTrue(isAlphabetNumberHarfAll("23A4789"));
        assertTrue(isAlphabetNumberHarfAll("23A47a89"));
        assertFalse(isAlphabetNumberHarfAll("-ab"));
        assertFalse(isAlphabetNumberHarfAll(""));
        assertFalse(isAlphabetNumberHarfAll(" "));
        assertFalse(isAlphabetNumberHarfAll(" a"));
    }

    public void test_isAlphabetNumberHarfAllOr_basic() throws Exception {
        assertTrue(isAlphabetNumberHarfAllOr("ABCDEFGHIJKLMONPQRSTUVWXYZ"));
        assertTrue(isAlphabetNumberHarfAllOr("A"));
        assertTrue(isAlphabetNumberHarfAllOr("BL"));
        assertTrue(isAlphabetNumberHarfAllOr("ABCDEFGHIJKLMONPQRSTUVWXYZ".toLowerCase()));
        assertTrue(isAlphabetNumberHarfAllOr("a"));
        assertTrue(isAlphabetNumberHarfAllOr("bbbOoo"));
        assertTrue(isAlphabetNumberHarfAllOr("ab2"));
        assertTrue(isAlphabetNumberHarfAllOr("234789"));
        assertTrue(isAlphabetNumberHarfAllOr("23A4789"));
        assertTrue(isAlphabetNumberHarfAllOr("23A47a89"));
        assertFalse(isAlphabetNumberHarfAllOr("-ab"));
        assertFalse(isAlphabetNumberHarfAllOr(""));
        assertFalse(isAlphabetNumberHarfAllOr(" "));
        assertFalse(isAlphabetNumberHarfAllOr(" a"));
        assertTrue(isAlphabetNumberHarfAllOr("-ab", '-'));
        assertTrue(isAlphabetNumberHarfAllOr(" b", ' '));
        assertFalse(isAlphabetNumberHarfAllOr("=ab", '-'));
    }

    public void test_isAlphabetNumberHarfLowerAll_basic() throws Exception {
        assertFalse(isAlphabetNumberHarfLowerAll("ABCDEFGHIJKLMONPQRSTUVWXYZ"));
        assertFalse(isAlphabetNumberHarfLowerAll("A"));
        assertFalse(isAlphabetNumberHarfLowerAll("BL"));
        assertTrue(isAlphabetNumberHarfLowerAll("ABCDEFGHIJKLMONPQRSTUVWXYZ".toLowerCase()));
        assertTrue(isAlphabetNumberHarfLowerAll("a"));
        assertFalse(isAlphabetNumberHarfLowerAll("bbbOoo"));
        assertTrue(isAlphabetNumberHarfLowerAll("ab2"));
        assertTrue(isAlphabetNumberHarfLowerAll("234789"));
        assertFalse(isAlphabetNumberHarfLowerAll("23A4789"));
        assertFalse(isAlphabetNumberHarfLowerAll("-ab"));
        assertFalse(isAlphabetNumberHarfLowerAll(""));
        assertFalse(isAlphabetNumberHarfLowerAll(" "));
        assertFalse(isAlphabetNumberHarfLowerAll(" a"));
    }

    public void test_isAlphabetNumberHarfUpperAll_basic() throws Exception {
        assertTrue(isAlphabetNumberHarfUpperAll("ABCDEFGHIJKLMONPQRSTUVWXYZ"));
        assertTrue(isAlphabetNumberHarfUpperAll("A"));
        assertTrue(isAlphabetNumberHarfUpperAll("BL"));
        assertFalse(isAlphabetNumberHarfUpperAll("ABCDEFGHIJKLMONPQRSTUVWXYZ".toLowerCase()));
        assertFalse(isAlphabetNumberHarfUpperAll("a"));
        assertFalse(isAlphabetNumberHarfUpperAll("bbbOoo"));
        assertTrue(isAlphabetNumberHarfUpperAll("AB2"));
        assertTrue(isAlphabetNumberHarfUpperAll("234789"));
        assertFalse(isAlphabetNumberHarfUpperAll("234a789"));
        assertFalse(isAlphabetNumberHarfUpperAll("-ab"));
        assertFalse(isAlphabetNumberHarfUpperAll(""));
        assertFalse(isAlphabetNumberHarfUpperAll(" "));
        assertFalse(isAlphabetNumberHarfUpperAll(" A"));
    }

    // -----------------------------------------------------
    //                                            Lower Case
    //                                            ----------
    public void test_isLowerCaseAll_basic() throws Exception {
        assertTrue(isLowerCaseAll("member"));
        assertTrue(isLowerCaseAll("purchase"));
        assertFalse(isLowerCaseAll("Member"));
        assertFalse(isLowerCaseAll("MeMBER"));
        assertFalse(isLowerCaseAll("mEMBER"));
        assertFalse(isLowerCaseAll("membeR"));
        assertFalse(isLowerCaseAll("MEMBER"));
        assertFalse(isLowerCaseAll("member1"));
        assertFalse(isLowerCaseAll("member_status"));
        assertFalse(isLowerCaseAll("member$status"));
        assertFalse(isLowerCaseAll(""));
        assertFalse(isLowerCaseAll("   "));
    }

    public void test_isLowerCaseAny_basic() throws Exception {
        assertTrue(isLowerCaseAny("member"));
        assertTrue(isLowerCaseAny("purchase"));
        assertTrue(isLowerCaseAny("Member"));
        assertTrue(isLowerCaseAny("MeMBER"));
        assertTrue(isLowerCaseAny("mEMBER"));
        assertTrue(isLowerCaseAny("membeR"));
        assertFalse(isLowerCaseAny("MEMBER"));
        assertTrue(isLowerCaseAny("member1"));
        assertTrue(isLowerCaseAny("member_status"));
        assertTrue(isLowerCaseAny("member$status"));
        assertFalse(isLowerCaseAny(""));
        assertFalse(isLowerCaseAny("   "));
    }

    // -----------------------------------------------------
    //                                            Upper Case
    //                                            ----------
    public void test_isUpperCaseAll_basic() throws Exception {
        assertTrue(isUpperCaseAll("MEMBER"));
        assertTrue(isUpperCaseAll("PURCHASE"));
        assertFalse(isUpperCaseAll("Member"));
        assertFalse(isUpperCaseAll("MeMBER"));
        assertFalse(isUpperCaseAll("mEMBER"));
        assertFalse(isUpperCaseAll("MEMBEr"));
        assertFalse(isUpperCaseAll("member"));
        assertFalse(isUpperCaseAll("MEMBER1"));
        assertFalse(isUpperCaseAll("MEMBER_STATUS"));
        assertFalse(isUpperCaseAll("MEMBER$STATUS"));
        assertFalse(isUpperCaseAll(""));
        assertFalse(isUpperCaseAll("   "));
    }

    public void test_isUpperCaseAny_basic() throws Exception {
        assertTrue(isUpperCaseAny("MEMBER"));
        assertTrue(isUpperCaseAny("PURCHASE"));
        assertTrue(isUpperCaseAny("Member"));
        assertTrue(isUpperCaseAny("MeMBER"));
        assertTrue(isUpperCaseAny("mEMBER"));
        assertTrue(isUpperCaseAny("MEMBEr"));
        assertTrue(isUpperCaseAny("membeR"));
        assertFalse(isUpperCaseAny("member"));
        assertTrue(isUpperCaseAny("MEMBER1"));
        assertTrue(isUpperCaseAny("MEMBER_STATUS"));
        assertTrue(isUpperCaseAny("MEMBER$STATUS"));
        assertFalse(isUpperCaseAny(""));
        assertFalse(isUpperCaseAny("   "));
    }
}
