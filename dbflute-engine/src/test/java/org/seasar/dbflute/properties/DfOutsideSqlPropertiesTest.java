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

/**
 * @author jflute
 * @since 0.8.3 (2008/11/17 Monday)
 */
public class DfOutsideSqlPropertiesTest extends PlainTestCase {

    public void test_resolveSqlPackageFileSeparatorWithFlatDirectory_contains() {
        // ## Arrange ##
        final DfOutsideSqlProperties prop = new DfOutsideSqlProperties(new Properties());

        // ## Act ##
        final String actual = prop.resolveSqlPackageFileSeparatorWithFlatDirectory("abc.def.ghi.dbflute", "def.ghi");

        // ## Assert ##
        log(actual);
        assertEquals("abc/def.ghi/dbflute", actual);
    }

    public void test_resolveFileSeparatorWithFlatDirectory_startsWith() {
        // ## Arrange ##
        final DfOutsideSqlProperties prop = new DfOutsideSqlProperties(new Properties());

        // ## Act ##
        final String actual = prop.resolveSqlPackageFileSeparatorWithFlatDirectory("abc.def.ghi.dbflute", "abc.def");

        // ## Assert ##
        log(actual);
        assertEquals("abc.def/ghi/dbflute", actual);
    }

    public void test_resolveFileSeparatorWithFlatDirectory_endsWith() {
        // ## Arrange ##
        final DfOutsideSqlProperties prop = new DfOutsideSqlProperties(new Properties());

        // ## Act ##
        final String actual = prop
                .resolveSqlPackageFileSeparatorWithFlatDirectory("abc.def.ghi.dbflute", "ghi.dbflute");

        // ## Assert ##
        log(actual);
        assertEquals("abc/def/ghi.dbflute", actual);
    }
}
