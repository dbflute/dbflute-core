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
package org.seasar.dbflute.cbean.coption;

import org.seasar.dbflute.cbean.sqlclause.subquery.SubQueryIndentProcessor;
import org.seasar.dbflute.unit.core.PlainTestCase;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.9.7.7 (2010/12/04 Saturday)
 */
public class DerivedReferrerOptionTest extends PlainTestCase {

    public void test_processSimpleFunction_basic() throws Exception {
        // ## Arrange ##
        DerivedReferrerOption option = new DerivedReferrerOption();
        option.acceptParameterKey("key", "path");

        // ## Act ##
        String actual = option.processSimpleFunction("max(foo.COL)", "func", null, false, "param0");

        // ## Assert ##
        log(actual);
        assertTrue(Srl.startsWith(actual, "func(max(foo.COL), "));
        assertTrue(Srl.endsWith(actual, "/*pmb.path.key.bindMap.param0*/null)"));
    }

    public void test_processSimpleFunction_thirdArg() throws Exception {
        // ## Arrange ##
        DerivedReferrerOption option = new DerivedReferrerOption();
        option.acceptParameterKey("key", "path");

        // ## Act ##
        String actual = option.processSimpleFunction("max(foo.COL)", "func", "third", false, "param0");

        // ## Assert ##
        log(actual);
        assertTrue(Srl.startsWith(actual, "func(max(foo.COL), "));
        assertTrue(Srl.endsWith(actual, "/*pmb.path.key.bindMap.param0*/null, third)"));
    }

    public void test_processSimpleFunction_leftArg() throws Exception {
        // ## Arrange ##
        DerivedReferrerOption option = new DerivedReferrerOption();
        option.acceptParameterKey("key", "path");

        // ## Act ##
        String actual = option.processSimpleFunction("max(foo.COL)", "func", "third", true, "param0");

        // ## Assert ##
        log(actual);
        assertTrue(Srl.contains(actual, "func(/*pmb.path.key.bindMap.param0*/null, max(foo.COL), third)"));
    }

    public void test_processSimpleFunction_nested_basic() throws Exception {
        // ## Arrange ##
        DerivedReferrerOption option = new DerivedReferrerOption();
        option.acceptParameterKey("key", "path");
        String sqbegin = SubQueryIndentProcessor.BEGIN_MARK_PREFIX;
        String sqend = SubQueryIndentProcessor.END_MARK_PREFIX;
        String identity = "identity";
        StringBuilder sb = new StringBuilder();
        sb.append("max(").append(sqbegin).append(identity);
        sb.append(ln()).append("select max(foo.COL)");
        sb.append(ln()).append("  from FOO foo");
        sb.append(ln()).append(")").append(sqend).append(identity);

        // ## Act ##
        String actual = option.processSimpleFunction(sb.toString(), "func", null, false, "param0");

        // ## Assert ##
        log(ln() + actual);
        assertTrue(Srl.startsWith(actual, "func(max(" + sqbegin + identity));
        assertTrue(Srl.contains(actual, "select max(foo.COL)"));
        assertTrue(Srl.contains(actual, "  from FOO foo"));
        assertTrue(Srl.endsWith(actual, ", /*pmb.path.key.bindMap.param0*/null)" + sqend + identity));
    }

    public void test_processSimpleFunction_nested_third() throws Exception {
        // ## Arrange ##
        DerivedReferrerOption option = new DerivedReferrerOption();
        option.acceptParameterKey("key", "path");
        String sqbegin = SubQueryIndentProcessor.BEGIN_MARK_PREFIX;
        String sqend = SubQueryIndentProcessor.END_MARK_PREFIX;
        String identity = "identity";
        StringBuilder sb = new StringBuilder();
        sb.append("max(").append(sqbegin).append(identity);
        sb.append(ln()).append("select max(foo.COL)");
        sb.append(ln()).append("  from FOO foo");
        sb.append(ln()).append(")").append(sqend).append(identity);

        // ## Act ##
        String actual = option.processSimpleFunction(sb.toString(), "func", "third", false, "param0");

        // ## Assert ##
        log(ln() + actual);
        assertTrue(Srl.startsWith(actual, "func(max(" + sqbegin + identity));
        assertTrue(Srl.contains(actual, "select max(foo.COL)"));
        assertTrue(Srl.contains(actual, "  from FOO foo"));
        assertTrue(Srl.endsWith(actual, ", /*pmb.path.key.bindMap.param0*/null, third)" + sqend + identity));
    }

    public void test_processSimpleFunction_nested_leftArg() throws Exception {
        // ## Arrange ##
        DerivedReferrerOption option = new DerivedReferrerOption();
        option.acceptParameterKey("key", "path");
        String sqbegin = SubQueryIndentProcessor.BEGIN_MARK_PREFIX;
        String sqend = SubQueryIndentProcessor.END_MARK_PREFIX;
        String identity = "identity";
        StringBuilder sb = new StringBuilder();
        sb.append("max(").append(sqbegin).append(identity);
        sb.append(ln()).append("select max(foo.COL)");
        sb.append(ln()).append("  from FOO foo");
        sb.append(ln()).append(")").append(sqend).append(identity);

        // ## Act ##
        String actual = option.processSimpleFunction(sb.toString(), "func", "third", true, "param0");

        // ## Assert ##
        log(ln() + actual);
        assertTrue(actual.contains("func(/*pmb.path.key.bindMap.param0*/null" + ln()));
        assertTrue(actual.contains(" , max(" + sqbegin + identity));
        assertTrue(Srl.contains(actual, "select max(foo.COL)"));
        assertTrue(Srl.contains(actual, "  from FOO foo"));
        assertTrue(Srl.endsWith(actual, "), third)" + sqend + identity));
    }

    public void test_processSimpleFunction_nested_nested() throws Exception {
        // ## Arrange ##
        DerivedReferrerOption option = new DerivedReferrerOption();
        option.acceptParameterKey("key", "path");
        String sqbegin = SubQueryIndentProcessor.BEGIN_MARK_PREFIX;
        String sqend = SubQueryIndentProcessor.END_MARK_PREFIX;
        String identity = "identity";
        StringBuilder sb = new StringBuilder();
        sb.append("max(").append(sqbegin).append(identity);
        sb.append(ln()).append("select max(").append(sqbegin).append(identity);
        sb.append(ln()).append("select max(foo.COL)");
        sb.append(ln()).append("  from FOO foo");
        sb.append(ln()).append(")").append(sqend).append(identity);
        sb.append(ln()).append("  from FOO foo");
        sb.append(ln()).append(")").append(sqend).append(identity);

        // ## Act ##
        String actual = option.processSimpleFunction(sb.toString(), "func", null, false, "param0");

        // ## Assert ##
        log(ln() + actual);
        assertTrue(Srl.startsWith(actual, "func(max(" + sqbegin + identity));
        assertTrue(Srl.contains(actual, "select max(foo.COL)"));
        assertTrue(Srl.contains(actual, "  from FOO foo"));

        assertEquals(2, Srl.count(actual, "max(" + sqbegin + identity));
        assertEquals(1, Srl.count(actual, "func(max(" + sqbegin + identity));
        assertEquals(2, Srl.count(actual, ")" + sqend + identity));
        assertEquals(1, Srl.count(actual, "/*pmb.path.key.bindMap.param0*/null)" + sqend + identity));
        assertTrue(Srl.endsWith(actual, ", /*pmb.path.key.bindMap.param0*/null)" + sqend + identity));
    }

    public void test_needsHandleSubQueryEnd_basic() throws Exception {
        // ## Arrange ##
        DerivedReferrerOption option = new DerivedReferrerOption();
        option.acceptParameterKey("key", "path");
        String sqend = SubQueryIndentProcessor.END_MARK_PREFIX;

        // ## Act & Assert ##
        assertFalse(option.hasSubQueryEndOnLastLine("FOO"));
        assertFalse(option.hasSubQueryEndOnLastLine("FOO" + ln() + "BAR"));
        assertTrue(option.hasSubQueryEndOnLastLine("FOO" + ln() + "BAR" + sqend));
        assertFalse(option.hasSubQueryEndOnLastLine("FOO" + sqend + ln() + "BAR"));
        assertFalse(option.hasSubQueryEndOnLastLine("FOO" + sqend));
    }
}
