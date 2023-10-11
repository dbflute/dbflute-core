/*
 * Copyright 2014-2023 the original author or authors.
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
package org.dbflute.dbway;

import org.dbflute.dbway.WayOfMySQL.FullTextSearchModifier;
import org.dbflute.optional.OptionalThing;
import org.dbflute.unit.RuntimeTestCase;

/**
 * @author jflute
 * @since 1.2.6 (2022/04/17 Sunday)
 */
public class WayOfMySQLTest extends RuntimeTestCase {

    public void test_FullTextSearchModifier_of() {
        assertEquals(FullTextSearchModifier.InBooleanMode, FullTextSearchModifier.of("in boolean mode").get());
        assertEquals(FullTextSearchModifier.InNaturalLanguageMode, FullTextSearchModifier.of("in natural language mode").get());
        assertEquals(FullTextSearchModifier.InBooleanMode, FullTextSearchModifier.of(FullTextSearchModifier.InBooleanMode).get());
        assertEquals(FullTextSearchModifier.InBooleanMode, FullTextSearchModifier.of(FullTextSearchModifier.of("in boolean mode")).get());
        assertEquals(FullTextSearchModifier.InBooleanMode, FullTextSearchModifier.of(OptionalThing.of("in boolean mode")).get());
        assertException(IllegalArgumentException.class, () -> FullTextSearchModifier.of(null).get());
        assertException(IllegalStateException.class, () -> FullTextSearchModifier.of("none").get());
    }

    @SuppressWarnings("deprecation")
    public void test_FullTextSearchModifier_codeOf() {
        assertEquals(FullTextSearchModifier.InBooleanMode, FullTextSearchModifier.codeOf("in boolean mode"));
        assertEquals(FullTextSearchModifier.InBooleanMode, FullTextSearchModifier.codeOf(FullTextSearchModifier.InBooleanMode));
        assertNull(FullTextSearchModifier.codeOf(null));
        assertNull(FullTextSearchModifier.codeOf("none"));
    }
}
