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

import org.dbflute.dbway.WayOfPostgreSQL.OperandOfLikeSearch;
import org.dbflute.optional.OptionalThing;
import org.dbflute.unit.RuntimeTestCase;

/**
 * @author jflute
 * @since 1.2.6 (2022/04/17 Sunday)
 */
public class WayOfPostgreSQLTest extends RuntimeTestCase {

    public void test_OperandOfLikeSearch_of() {
        assertEquals(OperandOfLikeSearch.BASIC, OperandOfLikeSearch.of("like").get());
        assertEquals(OperandOfLikeSearch.BASIC, OperandOfLikeSearch.of("LIKE").get());
        assertEquals(OperandOfLikeSearch.CASE_INSENSITIVE, OperandOfLikeSearch.of("ilike").get());
        assertEquals(OperandOfLikeSearch.FULL_TEXT_SEARCH, OperandOfLikeSearch.of("%%").get());
        assertEquals(OperandOfLikeSearch.BASIC, OperandOfLikeSearch.of(OperandOfLikeSearch.BASIC).get());
        assertEquals(OperandOfLikeSearch.BASIC, OperandOfLikeSearch.of(OperandOfLikeSearch.of("LIKE")).get());
        assertEquals(OperandOfLikeSearch.BASIC, OperandOfLikeSearch.of(OptionalThing.of("like")).get());
        assertException(IllegalArgumentException.class, () -> OperandOfLikeSearch.of(null).get());
        assertException(IllegalStateException.class, () -> OperandOfLikeSearch.of("none").get());
    }

    @SuppressWarnings("deprecation")
    public void test_OperandOfLikeSearch_codeOf() {
        assertEquals(OperandOfLikeSearch.BASIC, OperandOfLikeSearch.codeOf("like"));
        assertEquals(OperandOfLikeSearch.BASIC, OperandOfLikeSearch.codeOf("LIKE"));
        assertNull(OperandOfLikeSearch.codeOf(null));
        assertNull(OperandOfLikeSearch.codeOf("none"));
    }
}
