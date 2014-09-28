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
package org.seasar.dbflute.logic.generate.language;

import org.seasar.dbflute.unit.core.PlainTestCase;

/**
 * @author jflute
 * @since 1.0.6A (2014/06/16 Monday)
 */
public class DfLanguageSmallHelperTest extends PlainTestCase {

    public void test_extractGenericClassElement_basic() throws Exception {
        // ## Arrange ##
        DfLanguageSmallHelper helper = new DfLanguageSmallHelper();

        // ## Act ##
        String element = helper.extractGenericClassElement("List", "List<String>", "<", ">");

        // ## Assert ##
        assertEquals("String", element);
    }

    public void test_extractGenericClassElement_nested() throws Exception {
        // ## Arrange ##
        DfLanguageSmallHelper helper = new DfLanguageSmallHelper();

        // ## Act ##
        String element = helper.extractGenericClassElement("List", "List<Map<String, Object>>", "<", ">");

        // ## Assert ##
        assertEquals("Map<String, Object>", element);
    }
}
