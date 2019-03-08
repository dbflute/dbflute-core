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
package org.dbflute.helper.jprop;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import org.dbflute.unit.RuntimeTestCase;

/**
 * @author jflute
 */
public class JavaPropertiesReaderTest extends RuntimeTestCase {

    // ===================================================================================
    //                                                                             Unicode
    //                                                                             =======
    public void test_unicode_loadConvert() {
        // ## Arrange ##
        JavaPropertiesReader reader = new JavaPropertiesReader(null, null);
        String text = "\u938c\u5009\u306e\u3044\u306c";

        // ## Act ##
        String actual = reader.loadConvert(text);

        // ## Assert ##
        log(actual);
        assertEquals("鎌倉のいぬ", actual);
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
}
