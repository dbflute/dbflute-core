/*
 * Copyright 2014-2025 the original author or authors.
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
public class ClassificationUndefinedHandlingTypeTest extends RuntimeTestCase {

    public void test_of() {
        assertEquals(ClassificationUndefinedHandlingType.EXCEPTION, ClassificationUndefinedHandlingType.of("EXCEPTION").get());
        assertEquals(ClassificationUndefinedHandlingType.EXCEPTION, ClassificationUndefinedHandlingType.of("ExCEpTiON").get());
        assertEquals(ClassificationUndefinedHandlingType.LOGGING, ClassificationUndefinedHandlingType.of("LOGGING").get());
        assertEquals(ClassificationUndefinedHandlingType.ALLOWED,
                ClassificationUndefinedHandlingType.of(ClassificationUndefinedHandlingType.ALLOWED).get());
        assertEquals(ClassificationUndefinedHandlingType.LOGGING,
                ClassificationUndefinedHandlingType.of(ClassificationUndefinedHandlingType.of("LOGGING")).get());
        assertEquals(ClassificationUndefinedHandlingType.LOGGING,
                ClassificationUndefinedHandlingType.of(OptionalThing.of(ClassificationUndefinedHandlingType.LOGGING)).get());
        assertEquals(ClassificationUndefinedHandlingType.LOGGING,
                ClassificationUndefinedHandlingType.of(OptionalThing.of("LOGGING")).get());
        assertException(IllegalArgumentException.class, () -> ClassificationUndefinedHandlingType.of(null).get());
        assertException(IllegalStateException.class, () -> ClassificationUndefinedHandlingType.of("none").get());
        assertException(IllegalStateException.class, () -> ClassificationUndefinedHandlingType.of(OptionalThing.of("none")).get());
    }

    @SuppressWarnings("deprecation")
    public void test_codeOf() {
        assertEquals(ClassificationUndefinedHandlingType.EXCEPTION, ClassificationUndefinedHandlingType.codeOf("EXCEPTION"));
        assertEquals(ClassificationUndefinedHandlingType.EXCEPTION, ClassificationUndefinedHandlingType.codeOf("ExCEpTiON"));
        assertEquals(ClassificationUndefinedHandlingType.LOGGING, ClassificationUndefinedHandlingType.codeOf("LOGGING"));
        assertEquals(ClassificationUndefinedHandlingType.LOGGING,
                ClassificationUndefinedHandlingType.codeOf(ClassificationUndefinedHandlingType.LOGGING));
        assertNull(ClassificationUndefinedHandlingType.codeOf(null));
        assertNull(ClassificationUndefinedHandlingType.codeOf("none"));
    }
}
