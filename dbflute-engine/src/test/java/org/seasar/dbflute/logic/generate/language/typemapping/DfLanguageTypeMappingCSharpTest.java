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
package org.seasar.dbflute.logic.generate.language.typemapping;

import org.seasar.dbflute.unit.core.PlainTestCase;

/**
 * @author jflute
 */
public class DfLanguageTypeMappingCSharpTest extends PlainTestCase {

    public void test_switchParameterBeanTestValueType() throws Exception {
        // ## Arrange ##
        DfLanguageTypeMappingCSharp target = createTarget();

        // ## Act && Assert ##
        assertEquals("String", target.switchParameterBeanTestValueType("String"));
        assertEquals("int?", target.switchParameterBeanTestValueType("Integer"));
        assertEquals("long?", target.switchParameterBeanTestValueType("Long"));
        assertEquals("decimal?", target.switchParameterBeanTestValueType("BigDecimal"));
        assertEquals("DateTime?", target.switchParameterBeanTestValueType("Date"));
        assertEquals("DateTime?", target.switchParameterBeanTestValueType("Timestamp"));
        assertEquals("DateTime?", target.switchParameterBeanTestValueType("Time"));
        assertEquals("IList<String>", target.switchParameterBeanTestValueType("List<String>"));
        assertEquals("IList<int?>", target.switchParameterBeanTestValueType("List<Integer>"));
        assertEquals("IList<long?>", target.switchParameterBeanTestValueType("List<Long>"));
        assertEquals("IList<decimal?>", target.switchParameterBeanTestValueType("List<BigDecimal>"));
    }

    protected DfLanguageTypeMappingCSharp createTarget() {
        return new DfLanguageTypeMappingCSharp();
    }
}
