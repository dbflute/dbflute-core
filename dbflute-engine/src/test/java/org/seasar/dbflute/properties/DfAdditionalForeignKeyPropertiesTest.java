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
package org.seasar.dbflute.properties;

import java.util.Properties;

import org.seasar.dbflute.unit.core.PlainTestCase;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfAdditionalForeignKeyPropertiesTest extends PlainTestCase {

    // ===================================================================================
    //                                                                     Fixed Condition
    //                                                                     ===============
    public void test_adjustFixedConditionFormat_noIndent() {
        // ## Arrange ##
        DfAdditionalForeignKeyProperties prop = createProp();
        StringBuilder sb = new StringBuilder();
        sb.append("$$foreignAlias$$.VALID_BEGIN_DATE <= /*targetDate(Date)*/null");
        sb.append("\\nand $$foreignAlias$$.VALID_END_DATE >= /*targetDate(Date)*/null");

        // ## Act ##
        final String actual = prop.adjustFixedConditionFormat(sb.toString());

        // ## Assert ##
        log(ln() + actual);
        assertTrue(actual.contains("\\n     and "));
        assertEquals(1, Srl.count(actual, "\\n"));
    }

    public void test_adjustFixedConditionFormat_shortIndent() {
        // ## Arrange ##
        DfAdditionalForeignKeyProperties prop = createProp();
        StringBuilder sb = new StringBuilder();
        sb.append("$$foreignAlias$$.VALID_BEGIN_DATE <= /*targetDate(Date)*/null");
        sb.append("\\n    and $$foreignAlias$$.VALID_END_DATE >= /*targetDate(Date)*/null");

        // ## Act ##
        final String actual = prop.adjustFixedConditionFormat(sb.toString());

        // ## Assert ##
        log(ln() + actual);
        assertTrue(actual.contains("\\n     and "));
        assertEquals(1, Srl.count(actual, "\\n"));
    }

    public void test_adjustFixedConditionFormat_overIndent() {
        // ## Arrange ##
        DfAdditionalForeignKeyProperties prop = createProp();
        StringBuilder sb = new StringBuilder();
        sb.append("$$foreignAlias$$.VALID_BEGIN_DATE <= /*targetDate(Date)*/null");
        sb.append("\\n       and $$foreignAlias$$.VALID_END_DATE >= /*targetDate(Date)*/null");

        // ## Act ##
        final String actual = prop.adjustFixedConditionFormat(sb.toString());

        // ## Assert ##
        log(ln() + actual);
        assertTrue(actual.contains("\\n     and "));
        assertEquals(1, Srl.count(actual, "\\n"));
    }

    public void test_adjustFixedConditionFormat_fitIndent() {
        // ## Arrange ##
        DfAdditionalForeignKeyProperties prop = createProp();
        StringBuilder sb = new StringBuilder();
        sb.append("$$foreignAlias$$.VALID_BEGIN_DATE <= /*targetDate(Date)*/null");
        sb.append("\\n     and $$foreignAlias$$.VALID_END_DATE >= /*targetDate(Date)*/null");

        // ## Act ##
        final String actual = prop.adjustFixedConditionFormat(sb.toString());

        // ## Assert ##
        log(ln() + actual);
        assertTrue(actual.contains("\\n     and "));
        assertEquals(1, Srl.count(actual, "\\n"));
    }

    public void test_adjustFixedConditionFormat_line() {
        // ## Arrange ##
        DfAdditionalForeignKeyProperties prop = createProp();
        StringBuilder sb = new StringBuilder();
        sb.append("\\n$$foreignAlias$$.VALID_BEGIN_DATE <= /*targetDate(Date)*/null");
        sb.append("\\n     and $$foreignAlias$$.VALID_END_DATE >= /*targetDate(Date)*/null\\n");

        // ## Act ##
        final String actual = prop.adjustFixedConditionFormat(sb.toString());

        // ## Assert ##
        log(ln() + actual);
        assertTrue(actual.contains("\\n     and "));
        assertTrue(actual.endsWith("\\n"));
        assertEquals(3, Srl.count(actual, "\\n"));
    }

    public void test_adjustFixedConditionFormat_sqbegin() {
        // ## Arrange ##
        DfAdditionalForeignKeyProperties prop = createProp();
        StringBuilder sb = new StringBuilder();
        sb.append("$$foreignAlias$$.VALID_BEGIN_DATE <= /*targetDate(Date)*/null$$sqbegin$$");
        sb.append("\\nand $$foreignAlias$$.VALID_END_DATE >= /*targetDate(Date)*/null");

        // ## Act ##
        final String actual = prop.adjustFixedConditionFormat(sb.toString());

        // ## Assert ##
        log(ln() + actual);
        assertTrue(actual.contains("\\nand "));
        assertEquals(1, Srl.count(actual, "\\n"));
    }

    public void test_adjustFixedConditionFormat_oneLine() {
        // ## Arrange ##
        DfAdditionalForeignKeyProperties prop = createProp();
        StringBuilder sb = new StringBuilder();
        sb.append("$$foreignAlias$$.VALID_BEGIN_DATE <= /*targetDate(Date)*/null");

        // ## Act ##
        final String actual = prop.adjustFixedConditionFormat(sb.toString());

        // ## Assert ##
        log(ln() + actual);
        assertTrue(actual.contains("VALID_BEGIN_DATE"));
        assertEquals(0, Srl.count(actual, "\\n"));
    }

    public void test_adjustFixedConditionFormat_null() {
        // ## Arrange ##
        DfAdditionalForeignKeyProperties prop = createProp();

        // ## Act ##
        final String actual = prop.adjustFixedConditionFormat(null);

        // ## Assert ##
        assertNull(actual);
    }

    public void test_adjustFixedConditionFormat_empty() {
        // ## Arrange ##
        DfAdditionalForeignKeyProperties prop = createProp();

        // ## Act ##
        final String actual = prop.adjustFixedConditionFormat("");

        // ## Assert ##
        assertEquals("", actual);
    }

    protected DfAdditionalForeignKeyProperties createProp() {
        return new DfAdditionalForeignKeyProperties(new Properties());
    }
}
