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
package org.dbflute.jdbc;

import org.dbflute.optional.OptionalThing;
import org.dbflute.unit.RuntimeTestCase;

/**
 * @author jflute
 * @since 1.2.6 (2022/04/17 Sunday)
 */
public class ShortCharHandlingModeTest extends RuntimeTestCase {

    public void test_of() {
        assertEquals(ShortCharHandlingMode.EXCEPTION, ShortCharHandlingMode.of("E").get());
        assertEquals(ShortCharHandlingMode.EXCEPTION, ShortCharHandlingMode.of("e").get());
        assertEquals(ShortCharHandlingMode.LFILL, ShortCharHandlingMode.of("L").get());
        assertEquals(ShortCharHandlingMode.RFILL, ShortCharHandlingMode.of(ShortCharHandlingMode.RFILL).get());
        assertEquals(ShortCharHandlingMode.RFILL, ShortCharHandlingMode.of(ShortCharHandlingMode.of("R")).get());
        assertEquals(ShortCharHandlingMode.RFILL, ShortCharHandlingMode.of(OptionalThing.of(ShortCharHandlingMode.RFILL)).get());
        assertEquals(ShortCharHandlingMode.RFILL, ShortCharHandlingMode.of(OptionalThing.of("R")).get());
        assertException(IllegalArgumentException.class, () -> ShortCharHandlingMode.of(null).get());
        assertException(IllegalStateException.class, () -> ShortCharHandlingMode.of("none").get());
        assertException(IllegalStateException.class, () -> ShortCharHandlingMode.of(OptionalThing.of("none")).get());
    }

    @SuppressWarnings("deprecation")
    public void test_codeOf() {
        assertEquals(ShortCharHandlingMode.EXCEPTION, ShortCharHandlingMode.codeOf("E"));
        assertEquals(ShortCharHandlingMode.EXCEPTION, ShortCharHandlingMode.codeOf("e"));
        assertEquals(ShortCharHandlingMode.LFILL, ShortCharHandlingMode.codeOf("L"));
        assertEquals(ShortCharHandlingMode.LFILL, ShortCharHandlingMode.codeOf(ShortCharHandlingMode.LFILL));
        assertNull(ShortCharHandlingMode.codeOf(null));
        assertNull(ShortCharHandlingMode.codeOf("none"));
    }
}
