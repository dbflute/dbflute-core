/*
 * Copyright 2014-2023 the original author or authors.
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
package org.dbflute.helper.jprop;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import org.dbflute.unit.RuntimeTestCase;

import junit.framework.AssertionFailedError;

/**
 * @author jflute
 */
public class JavaPropertiesReaderTest extends RuntimeTestCase {

    // ===================================================================================
    //                                                                             Unicode
    //                                                                             =======
    public void test_unicode_loadConvert_basic() {
        // ## Arrange ##
        JavaPropertiesReader reader = new JavaPropertiesReader(null, null);
        String text = "\u304a\u308b\u3050\u3069\u3063\u304f\u3055\u3044\u3069\u3059\u3066\u30fc\u3058";

        // ## Act ##
        String actual = reader.loadConvert(text);

        // ## Assert ##
        log(actual);
        assertEquals("おるぐどっくさいどすてーじ", actual);
    }

    public void test_unicode_loadConvert_nonTrimmed() {
        // ## Arrange ##
        JavaPropertiesReader reader = new JavaPropertiesReader(null, null);
        String text = " \u304a\u308b\u3050\u3069\u3063\u304f\u3055\u3044\u3069\u3059\u3066\u30fc\u3058  ";

        // ## Act ##
        String actual = reader.loadConvert(text);

        // ## Assert ##
        log(actual);
        assertEquals(" おるぐどっくさいどすてーじ  ", actual);
    }

    // ===================================================================================
    //                                                                            Variable
    //                                                                            ========
    public void test_variable_basic() {
        // ## Arrange ##
        JavaPropertiesReader reader = new JavaPropertiesReader("maihama", () -> {
            return stream("land.sea = land{0}sea{1}ikspiary");
        });

        // ## Act ##
        JavaPropertiesResult result = reader.read();

        // ## Assert ##
        log(result);
        List<JavaPropertiesProperty> propertyList = result.getPropertyList();
        assertHasOnlyOneElement(propertyList);
        JavaPropertiesProperty property = propertyList.get(0);
        List<Integer> variableNumberList = property.getVariableNumberList();
        log(property, variableNumberList);
        assertEquals(Arrays.asList(0, 1), variableNumberList);
        assertEquals(Arrays.asList("0", "1"), property.getVariableStringList());
    }

    public void test_variable_nonNumber_default() {
        // ## Arrange ##
        JavaPropertiesReader reader = new JavaPropertiesReader("maihama", () -> {
            return stream("land.sea = land{0}sea{mystic}piari");
        });

        // ## Act ##
        JavaPropertiesResult result = reader.read();

        // ## Assert ##
        log(result);
        List<JavaPropertiesProperty> propertyList = result.getPropertyList();
        assertHasOnlyOneElement(propertyList);
        JavaPropertiesProperty property = propertyList.get(0);
        List<Integer> variableNumberList = property.getVariableNumberList();
        log(property, variableNumberList);
        assertEquals(Arrays.asList(0), variableNumberList);
        assertEquals(Arrays.asList("0"), property.getVariableStringList());
    }

    public void test_variable_nonNumber_use() {
        // ## Arrange ##
        JavaPropertiesReader reader = new JavaPropertiesReader("maihama", () -> {
            return stream("land.sea = land{0}sea{mystic}piari");
        }).useNonNumberVariable();

        // ## Act ##
        JavaPropertiesResult result = reader.read();

        // ## Assert ##
        log(result);
        List<JavaPropertiesProperty> propertyList = result.getPropertyList();
        assertHasOnlyOneElement(propertyList);
        JavaPropertiesProperty property = propertyList.get(0);
        List<Integer> variableNumberList = property.getVariableNumberList();
        log(property, variableNumberList);
        assertEquals(Arrays.asList(0), variableNumberList);
        assertEquals(Arrays.asList("0", "mystic"), property.getVariableStringList());
        assertEquals("String arg0, String mystic", property.getVariableArgDef());
        assertEquals("arg0, mystic", property.getVariableArgSet());
    }

    // -----------------------------------------------------
    //                                               Ordered
    //                                               -------
    public void test_variable_ordered_basic() {
        // ## Arrange ##
        JavaPropertiesReader reader = new JavaPropertiesReader("maihama", () -> {
            return stream("land.sea = land{2}sea{mystic}piari{0}bonvo{mic}dstore{1}");
        }).useNonNumberVariable();

        // ## Act ##
        JavaPropertiesResult result = reader.read();

        // ## Assert ##
        log(result);
        List<JavaPropertiesProperty> propertyList = result.getPropertyList();
        assertHasOnlyOneElement(propertyList);
        JavaPropertiesProperty property = propertyList.get(0);
        List<Integer> variableNumberList = property.getVariableNumberList();
        log(property, variableNumberList);
        assertEquals(Arrays.asList(0, 1, 2), variableNumberList);
        assertEquals(Arrays.asList("0", "1", "2", "mic", "mystic"), property.getVariableStringList());
        assertEquals("String arg0, String arg1, String arg2, String mic, String mystic", property.getVariableArgDef());
        assertEquals("arg0, arg1, arg2, mic, mystic", property.getVariableArgSet());
    }

    public void test_variable_ordered_compatible() {
        // ## Arrange ##
        JavaPropertiesReader reader = new JavaPropertiesReader("abc", () -> {
            return new ByteArrayInputStream("land.sea = land{2}sea{mystic}piari{0}bonvo{goof}dstore{1}".getBytes("UTF-8"));
        }).useNonNumberVariable().suppressVariableOrder();

        // ## Act ##
        JavaPropertiesResult result = reader.read();

        // ## Assert ##
        log(result);
        List<JavaPropertiesProperty> propertyList = result.getPropertyList();
        assertHasOnlyOneElement(propertyList);
        JavaPropertiesProperty property = propertyList.get(0);
        List<Integer> variableNumberList = property.getVariableNumberList();
        log(property, variableNumberList);
        assertEquals(Arrays.asList(2, 0, 1), variableNumberList);
        assertEquals(Arrays.asList("2", "mystic", "0", "goof", "1"), property.getVariableStringList());
        assertEquals(Arrays.asList("arg2", "mystic", "arg0", "goof", "arg1"), property.getVariableArgNameList());
        assertEquals("String arg2, String mystic, String arg0, String goof, String arg1", property.getVariableArgDef());
        assertEquals("arg2, mystic, arg0, goof, arg1", property.getVariableArgSet());
    }

    public void test_variable_ordered_various() {
        // ## Arrange ##
        JavaPropertiesReader reader = new JavaPropertiesReader("maihama", () -> {
            return stream("land.sea = land{oneman}sea{mystic}piari{4}bonvo{mou}{1}");
        }).useNonNumberVariable();

        // ## Act ##
        JavaPropertiesResult result = reader.read();

        // ## Assert ##
        log(result);
        List<JavaPropertiesProperty> propertyList = result.getPropertyList();
        assertHasOnlyOneElement(propertyList);
        JavaPropertiesProperty property = propertyList.get(0);
        List<Integer> variableNumberList = property.getVariableNumberList();
        log(property, variableNumberList);
        assertEquals(Arrays.asList(1, 4), variableNumberList);
        assertEquals(Arrays.asList("1", "4", "mou", "mystic", "oneman"), property.getVariableStringList());
        assertEquals(Arrays.asList("arg1", "arg4", "mou", "mystic", "oneman"), property.getVariableArgNameList());
        assertEquals("String arg1, String arg4, String mou, String mystic, String oneman", property.getVariableArgDef());
        assertEquals("arg1, arg4, mou, mystic, oneman", property.getVariableArgSet());
    }

    protected ByteArrayInputStream stream(String propDef) throws UnsupportedEncodingException {
        return new ByteArrayInputStream(propDef.getBytes("UTF-8"));
    }

    // ===================================================================================
    //                                                                             Comment
    //                                                                             =======
    public void test_comment_basic() {
        // ## Arrange ##
        JavaPropertiesReader reader = new JavaPropertiesReader("maihama", () -> {
            StringBuilder sb = new StringBuilder();
            sb.append("# one liner comment").append(ln());
            sb.append("oneline.sea = 1").append(ln());

            sb.append("# multiple comment1").append(ln());
            sb.append("# multiple comment2").append(ln());
            sb.append("# multiple comment3").append(ln());
            sb.append("multiple.comment.sea = 2").append(ln());

            sb.append("#commentout.prop = dummy").append(ln());
            sb.append("commentout.only.sea = 3").append(ln());

            sb.append("# comment out property value").append(ln());
            sb.append("#commentout.prop = dummy").append(ln());
            sb.append("commentout.before.sea = 4").append(ln());

            sb.append("# before commentout").append(ln());
            sb.append("#commentout.prop = dummy").append(ln());
            sb.append("# after commentout").append(ln());
            sb.append("commentout.beforeafter.sea = 5").append(ln());

            sb.append("# before tagcomment").append(ln());
            sb.append("# =========================").append(ln());
            sb.append("#                      Tag").append(ln());
            sb.append("#                     =====").append(ln());
            sb.append("# after tagcomment1").append(ln());
            sb.append("# after tagcomment2").append(ln());
            sb.append("tagomment.beforeafter.sea = 6").append(ln());

            sb.append("# before nesttagcomment").append(ln());
            sb.append("# -------------------------").append(ln());
            sb.append("#                      Tag").append(ln());
            sb.append("#                     -----").append(ln());
            sb.append("# after nesttagcomment1").append(ln());
            sb.append("# after nesttagcomment2").append(ln());
            sb.append("nesttagomment.beforeafter.sea = 7").append(ln());

            sb.append("# @Override after override @Secure after secure").append(ln());
            sb.append("annotation.only.sea = 8").append(ln());

            sb.append("# before annotation").append(ln());
            sb.append("# @Override after override @Secure after secure").append(ln());
            sb.append("# after annotation").append(ln());
            sb.append("annotation.beforeafter.sea = 9").append(ln());
            return stream(sb.toString());
        });

        // ## Act ##
        JavaPropertiesResult result = reader.read();

        // ## Assert ##
        assertPropertyComment(result, "oneline.sea", "1", "one liner comment");

        assertPropertyComment(result, "multiple.comment.sea", "2", "multiple", "comment3");
        assertPropertyComment(result, "multiple.comment.sea", "2", "multiple", "comment1", "comment2");

        assertPropertyComment(result, "commentout.only.sea", "3");

        assertPropertyComment(result, "commentout.before.sea", "4");
        assertException(AssertionFailedError.class, () -> {
            assertPropertyComment(result, "commentout.before.sea", "4", "comment out property value");
        });

        assertPropertyComment(result, "commentout.beforeafter.sea", "5", "after commentout");
        assertException(AssertionFailedError.class, () -> {
            assertPropertyComment(result, "commentout.beforeafter.sea", "5", "after commentout", "before commentout");
        });

        assertPropertyComment(result, "tagomment.beforeafter.sea", "6", "after tagcomment");
        assertException(AssertionFailedError.class, () -> {
            assertPropertyComment(result, "tagomment.beforeafter.sea", "6", "after tagcomment", "before tagcomment");
        });

        assertPropertyComment(result, "nesttagomment.beforeafter.sea", "7", "after nesttagcomment");
        assertException(AssertionFailedError.class, () -> {
            assertPropertyComment(result, "nesttagomment.beforeafter.sea", "7", "after nesttagcomment", "before nesttagcomment");
        });

        assertPropertyComment(result, "annotation.only.sea", "8", "@Override", "@Secure");

        assertPropertyComment(result, "annotation.beforeafter.sea", "9", "after anno", "before anno", "@Override", "@Secure");
    }

    private void assertPropertyComment(JavaPropertiesResult result, String propertyKey, String propertyValue, String... comments) {
        JavaPropertiesProperty property = result.getProperty(propertyKey);
        log("...Asserting property: {}, {}, {}", property.getPropertyKey(), property.getPropertyValue(), property.getComment());
        assertEquals(propertyValue, property.getPropertyValue());
        if (comments.length > 0) {
            assertNotNull(property.getComment());
            for (String comment : comments) {
                assertContains(property.getComment(), comment);
            }
        } else {
            assertNull(property.getComment());
        }
    }
}
