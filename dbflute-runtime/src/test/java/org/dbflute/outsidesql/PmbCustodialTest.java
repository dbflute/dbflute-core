/*
 * Copyright 2014-2018 the original author or authors.
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
package org.dbflute.outsidesql;

import org.dbflute.exception.CharParameterShortSizeException;
import org.dbflute.outsidesql.PmbCustodial.PmbShortCharHandlingMode;
import org.dbflute.unit.RuntimeTestCase;

/**
 * @author jflute
 * @since 0.9.6.1 (2009/11/17 Tuesday)
 */
public class PmbCustodialTest extends RuntimeTestCase {

    // ===================================================================================
    //                                                                       Empty to Null
    //                                                                       =============
    public void test_convertEmptyToNull() {
        assertNull(PmbCustodial.convertEmptyToNull(""));
        assertNull(PmbCustodial.convertEmptyToNull(null));
        assertEquals(" ", PmbCustodial.convertEmptyToNull(" "));
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
        PmbShortCharHandlingMode mode = PmbShortCharHandlingMode.EXCEPTION;

        // ## Act ##
        try {
            PmbCustodial.handleShortChar(parameterName, value, size, mode);

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
        PmbShortCharHandlingMode mode = PmbShortCharHandlingMode.EXCEPTION;

        // ## Act ##
        String actual = PmbCustodial.handleShortChar(parameterName, value, size, mode);

        // ## Assert ##
        assertNull(actual);
    }

    public void test_handleShortChar_basic_nullSize() {
        // ## Arrange ##
        String parameterName = "testParameter";
        String value = "AB";
        Integer size = null;
        PmbShortCharHandlingMode mode = PmbShortCharHandlingMode.EXCEPTION;

        // ## Act ##
        String actual = PmbCustodial.handleShortChar(parameterName, value, size, mode);

        // ## Assert ##
        assertEquals(value, actual);
    }

    public void test_handleShortChar_basic_nullMode() {
        // ## Arrange ##
        String parameterName = "testParameter";
        String value = "AB";
        Integer size = 3;
        PmbShortCharHandlingMode mode = null;

        // ## Act ##
        try {
            PmbCustodial.handleShortChar(parameterName, value, size, mode);

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
        PmbShortCharHandlingMode mode = PmbShortCharHandlingMode.EXCEPTION;

        // ## Act ##
        try {
            PmbCustodial.handleShortChar(parameterName, value, size, mode);

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
        PmbShortCharHandlingMode mode = PmbShortCharHandlingMode.EXCEPTION;

        // ## Act ##
        String actual = PmbCustodial.handleShortChar(parameterName, value, size, mode);

        // ## Assert ##
        assertEquals(value, actual);
    }

    public void test_handleShortChar_EXCEPTION_overSize() {
        // ## Arrange ##
        String parameterName = "testParameter";
        String value = "ABCD";
        Integer size = 3;
        PmbShortCharHandlingMode mode = PmbShortCharHandlingMode.EXCEPTION;

        // ## Act ##
        String actual = PmbCustodial.handleShortChar(parameterName, value, size, mode);

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
        PmbShortCharHandlingMode mode = PmbShortCharHandlingMode.RFILL;

        // ## Act ##
        String actual = PmbCustodial.handleShortChar(parameterName, value, size, mode);

        // ## Assert ##
        assertEquals(value + " ", actual);
    }

    public void test_handleShortChar_RFILL_justSize() {
        // ## Arrange ##
        String parameterName = "testParameter";
        String value = "AB";
        Integer size = 3;
        PmbShortCharHandlingMode mode = PmbShortCharHandlingMode.RFILL;

        // ## Act ##
        String actual = PmbCustodial.handleShortChar(parameterName, value, size, mode);

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
        PmbShortCharHandlingMode mode = PmbShortCharHandlingMode.LFILL;

        // ## Act ##
        String actual = PmbCustodial.handleShortChar(parameterName, value, size, mode);

        // ## Assert ##
        assertEquals(" " + value, actual);
    }

    public void test_handleShortChar_LFILL_justSize() {
        // ## Arrange ##
        String parameterName = "testParameter";
        String value = "AB";
        Integer size = 3;
        PmbShortCharHandlingMode mode = PmbShortCharHandlingMode.LFILL;

        // ## Act ##
        String actual = PmbCustodial.handleShortChar(parameterName, value, size, mode);

        // ## Assert ##
        assertEquals(" " + value, actual);
    }
}
