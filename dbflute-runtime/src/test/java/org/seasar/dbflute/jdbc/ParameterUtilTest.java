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
package org.seasar.dbflute.jdbc;

import org.seasar.dbflute.exception.CharParameterShortSizeException;
import org.seasar.dbflute.jdbc.ParameterUtil.ShortCharHandlingMode;
import org.seasar.dbflute.unit.core.PlainTestCase;

/**
 * @author jflute
 * @since 0.9.6.1 (2009/11/17 Tuesday)
 */
public class ParameterUtilTest extends PlainTestCase {

    // ===================================================================================
    //                                                                       Empty to Null
    //                                                                       =============
    public void test_convertEmptyToNull() {
        assertNull(ParameterUtil.convertEmptyToNull(""));
        assertNull(ParameterUtil.convertEmptyToNull(null));
        assertEquals(" ", ParameterUtil.convertEmptyToNull(" "));
    }

    // ===================================================================================
    //                                                                          Short Char
    //                                                                          ==========
    // -----------------------------------------------------
    //                                                 Basic
    //                                                 -----
    public void test_handleShortChar_basic_nullParameterName() {
        // ## Arrange ##
        String parameterName = null;
        String value = "AB";
        Integer size = 3;
        ShortCharHandlingMode mode = ShortCharHandlingMode.EXCEPTION;

        // ## Act ##
        try {
            ParameterUtil.handleShortChar(parameterName, value, size, mode);

            // ## Assert ##
            fail();
        } catch (IllegalArgumentException e) {
            // OK
            log(e.getMessage());
        }
    }

    public void test_handleShortChar_basic_nullValue() {
        // ## Arrange ##
        String parameterName = "testParameter";
        String value = null;
        Integer size = 3;
        ShortCharHandlingMode mode = ShortCharHandlingMode.EXCEPTION;

        // ## Act ##
        String actual = ParameterUtil.handleShortChar(parameterName, value, size, mode);

        // ## Assert ##
        assertNull(actual);
    }

    public void test_handleShortChar_basic_nullSize() {
        // ## Arrange ##
        String parameterName = "testParameter";
        String value = "AB";
        Integer size = null;
        ShortCharHandlingMode mode = ShortCharHandlingMode.EXCEPTION;

        // ## Act ##
        String actual = ParameterUtil.handleShortChar(parameterName, value, size, mode);

        // ## Assert ##
        assertEquals(value, actual);
    }

    public void test_handleShortChar_basic_nullMode() {
        // ## Arrange ##
        String parameterName = "testParameter";
        String value = "AB";
        Integer size = 3;
        ShortCharHandlingMode mode = null;

        // ## Act ##
        try {
            ParameterUtil.handleShortChar(parameterName, value, size, mode);

            // ## Assert ##
            fail();
        } catch (IllegalArgumentException e) {
            // OK
            log(e.getMessage());
        }
    }

    // -----------------------------------------------------
    //                                             EXCEPTION
    //                                             ---------
    public void test_handleShortChar_EXCEPTION_shortChar() {
        // ## Arrange ##
        String parameterName = "testParameter";
        String value = "AB";
        Integer size = 3;
        ShortCharHandlingMode mode = ShortCharHandlingMode.EXCEPTION;

        // ## Act ##
        try {
            ParameterUtil.handleShortChar(parameterName, value, size, mode);

            // ## Assert ##
            fail();
        } catch (CharParameterShortSizeException e) {
            // OK
            log(e.getMessage());
        }
    }

    public void test_handleShortChar_EXCEPTION_justSize() {
        // ## Arrange ##
        String parameterName = "testParameter";
        String value = "ABC";
        Integer size = 3;
        ShortCharHandlingMode mode = ShortCharHandlingMode.EXCEPTION;

        // ## Act ##
        String actual = ParameterUtil.handleShortChar(parameterName, value, size, mode);

        // ## Assert ##
        assertEquals(value, actual);
    }

    public void test_handleShortChar_EXCEPTION_overSize() {
        // ## Arrange ##
        String parameterName = "testParameter";
        String value = "ABCD";
        Integer size = 3;
        ShortCharHandlingMode mode = ShortCharHandlingMode.EXCEPTION;

        // ## Act ##
        String actual = ParameterUtil.handleShortChar(parameterName, value, size, mode);

        // ## Assert ##
        assertEquals(value, actual);

        // *The overSize is out of scope in spite of CHAR type.
    }

    // -----------------------------------------------------
    //                                                 RFILL
    //                                                 -----
    public void test_handleShortChar_RFILL_shortChar() {
        // ## Arrange ##
        String parameterName = "testParameter";
        String value = "AB";
        Integer size = 3;
        ShortCharHandlingMode mode = ShortCharHandlingMode.RFILL;

        // ## Act ##
        String actual = ParameterUtil.handleShortChar(parameterName, value, size, mode);

        // ## Assert ##
        assertEquals(value + " ", actual);
    }

    public void test_handleShortChar_RFILL_justSize() {
        // ## Arrange ##
        String parameterName = "testParameter";
        String value = "AB";
        Integer size = 3;
        ShortCharHandlingMode mode = ShortCharHandlingMode.RFILL;

        // ## Act ##
        String actual = ParameterUtil.handleShortChar(parameterName, value, size, mode);

        // ## Assert ##
        assertEquals(value + " ", actual);
    }

    // -----------------------------------------------------
    //                                                 LFILL
    //                                                 -----
    public void test_handleShortChar_LFILL_shortChar() {
        // ## Arrange ##
        String parameterName = "testParameter";
        String value = "AB";
        Integer size = 3;
        ShortCharHandlingMode mode = ShortCharHandlingMode.LFILL;

        // ## Act ##
        String actual = ParameterUtil.handleShortChar(parameterName, value, size, mode);

        // ## Assert ##
        assertEquals(" " + value, actual);
    }

    public void test_handleShortChar_LFILL_justSize() {
        // ## Arrange ##
        String parameterName = "testParameter";
        String value = "AB";
        Integer size = 3;
        ShortCharHandlingMode mode = ShortCharHandlingMode.LFILL;

        // ## Act ##
        String actual = ParameterUtil.handleShortChar(parameterName, value, size, mode);

        // ## Assert ##
        assertEquals(" " + value, actual);
    }
}
