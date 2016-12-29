/*
 * Copyright 2014-2017 the original author or authors.
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
import java.util.Arrays;
import java.util.List;

import org.dbflute.unit.RuntimeTestCase;

/**
 * @author jflute
 */
public class JavaPropertiesReaderTest extends RuntimeTestCase {

    public void test_loadConvert() {
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
        JavaPropertiesReader reader = new JavaPropertiesReader("abc", () -> {
            return new ByteArrayInputStream("land.sea = land{0}sea{1}ikspiary".getBytes("UTF-8"));
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
        JavaPropertiesReader reader = new JavaPropertiesReader("abc", () -> {
            return new ByteArrayInputStream("land.sea = land{0}sea{foo}ikspiary".getBytes("UTF-8"));
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
        JavaPropertiesReader reader = new JavaPropertiesReader("abc", () -> {
            return new ByteArrayInputStream("land.sea = land{0}sea{foo}ikspiary".getBytes("UTF-8"));
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
        assertEquals(Arrays.asList("0", "foo"), property.getVariableStringList());
        assertEquals("String arg0, String foo", property.getVariableArgDef());
        assertEquals("arg0, foo", property.getVariableArgSet());
    }
}
