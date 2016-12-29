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
package org.apache.torque.engine.database.model;

import org.dbflute.unit.EngineTestCase;

/**
 * @author jflute
 */
public class UnifiedSchemaTest extends EngineTestCase {

    public void test_equals_basic() {
        // ## Arrange ##
        UnifiedSchema first = createTarget("foo", "bar");
        UnifiedSchema second = createTarget("foo", "bar");

        // ## Act ##
        boolean actual = first.equals(second);

        // ## Assert ##
        assertTrue(actual);
    }

    public void test_equals_catalog_diff() {
        // ## Arrange ##
        UnifiedSchema first = createTarget("foo", "bar");
        UnifiedSchema second = createTarget("baz", "bar");

        // ## Act ##
        boolean actual = first.equals(second);

        // ## Assert ##
        assertFalse(actual);
    }

    public void test_equals_schema_diff() {
        // ## Arrange ##
        UnifiedSchema first = createTarget("foo", "bar");
        UnifiedSchema second = createTarget("foo", "baz");

        // ## Act ##
        boolean actual = first.equals(second);

        // ## Assert ##
        assertFalse(actual);
    }

    public void test_equals_null() {
        // ## Arrange ##
        UnifiedSchema first = createTarget("foo", "bar");
        UnifiedSchema second = null;

        // ## Act ##
        boolean actual = first.equals(second);

        // ## Assert ##
        assertFalse(actual);
    }

    protected UnifiedSchema createTarget(String catalog, String schema) {
        return new UnifiedSchema(catalog, schema) {
            @Override
            protected boolean isCompletelyUnsupportedDBMS() {
                return false;
            }
        };
    }
}
