/*
 * Copyright 2014-2020 the original author or authors.
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
package org.dbflute.optional;

import java.util.Optional;

import org.dbflute.unit.RuntimeTestCase;

/**
 * @author jflute
 * @since 1.1.0-sp8 (2015/09/02 Wednesday)
 */
public class OptionalThingTest extends RuntimeTestCase {

    public void test_migratedFrom() throws Exception {
        assertTrue(OptionalThing.migratedFrom(Optional.of("sea"), () -> {
            throw new IllegalStateException();
        }).isPresent());
        assertFalse(OptionalThing.migratedFrom(Optional.empty(), () -> {
            throw new IllegalStateException();
        }).isPresent());
        try {
            OptionalThing.migratedFrom(Optional.empty(), () -> {
                throw new IllegalStateException("land");
            }).get();
            fail();
        } catch (IllegalStateException e) {
            String message = e.getMessage();
            log(message);
            assertContains(message, "land");
        }
    }

    public void test_translatedFrom() throws Exception {
        assertTrue(OptionalThing.translatedFrom(OptionalThing.of("sea"), () -> {
            throw new IllegalStateException();
        }).isPresent());
        assertFalse(OptionalThing.translatedFrom(OptionalThing.empty(), () -> {
            throw new IllegalStateException();
        }).isPresent());
        try {
            OptionalThing.translatedFrom(OptionalThing.ofNullable(null, () -> {
                throw new IllegalStateException("sea");
            }), () -> {
                throw new IllegalStateException("land");
            }).get();
            fail();
        } catch (IllegalStateException e) {
            String message = e.getMessage();
            log(message);
            assertNotContains(message, "sea");
            assertContains(message, "land");
        }
    }

    public void test_orElseTranslatingThrow() throws Exception {
        assertNotNull(OptionalThing.of("sea").orElseTranslatingThrow(cause -> {
            return new IllegalStateException("traslated", cause);
        }));
        assertException(IllegalStateException.class, () -> {
            OptionalThing.ofNullable(null, () -> {
                throw new IllegalArgumentException("cause");
            }).orElseTranslatingThrow(cause -> {
                assertEquals("cause", cause.getMessage());
                markHere("cause");
                return new IllegalStateException("traslated", cause);
            });
        });
        assertMarked("cause");
    }
}
