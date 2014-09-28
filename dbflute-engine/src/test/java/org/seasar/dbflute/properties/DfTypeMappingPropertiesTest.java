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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author jflute
 * @since 0.9.5 (2009/04/21 Tuesday)
 */
public class DfTypeMappingPropertiesTest {

    public void test_isNameTypeMappingKey() {
        assertTrue(DfTypeMappingProperties.isNameTypeMappingKey("$$foo$$"));
        assertTrue(DfTypeMappingProperties.isNameTypeMappingKey("$$$foo$$$"));
        assertFalse(DfTypeMappingProperties.isNameTypeMappingKey("$foo$$"));
        assertFalse(DfTypeMappingProperties.isNameTypeMappingKey("$$foo$"));
        assertFalse(DfTypeMappingProperties.isNameTypeMappingKey("FOO"));
    }

    public void test_extractDbTypeName() {
        assertEquals("foo", DfTypeMappingProperties.extractDbTypeName("$$foo$$"));
        assertEquals("$foo$", DfTypeMappingProperties.extractDbTypeName("$$$foo$$$"));
    }
}
