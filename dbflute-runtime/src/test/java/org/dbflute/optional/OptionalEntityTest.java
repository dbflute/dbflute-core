/*
 * Copyright 2014-2022 the original author or authors.
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

import org.dbflute.Entity;
import org.dbflute.exception.EntityAlreadyDeletedException;
import org.dbflute.mock.MockEntity;
import org.dbflute.unit.RuntimeTestCase;

/**
 * @author jflute
 * @since 1.0.5F (2014/05/05 Monday)
 */
public class OptionalEntityTest extends RuntimeTestCase {

    // ===================================================================================
    //                                                                      Value Handling
    //                                                                      ==============
    // -----------------------------------------------------
    //                                             ifPresent
    //                                             ---------
    public void test_ifPresent_orElse_basic() throws Exception {
        // ## Arrange ##

        // ## Act ##
        prepareOpt(new MockEntity()).ifPresent(table -> {
            markHere("present");
        }).orElse(() -> {
            fail();
        });

        // ## Assert ##
        assertMarked("present");

        // ## Act ##
        prepareOpt(null).ifPresent(table -> {
            fail();
        }).orElse(() -> {
            markHere("none");
        });

        // ## Assert ##
        assertMarked("none");
    }

    public void test_ifPresentOrElse_basic() throws Exception {
        // ## Arrange ##

        // ## Act ##
        prepareOpt(new MockEntity()).ifPresentOrElse(table -> {
            markHere("present");
        }, () -> {
            fail();
        });

        // ## Assert ##
        assertMarked("present");

        // ## Act ##
        prepareOpt(null).ifPresentOrElse(table -> {
            fail();
        }, () -> {
            markHere("none");
        });

        // ## Assert ##
        assertMarked("none");
    }

    // -----------------------------------------------------
    //                                         isPresent/get
    //                                         -------------
    public void test_isPresent() throws Exception {
        assertTrue(prepareOpt(new MockEntity()).isPresent());
        assertFalse(prepareOpt(null).isPresent());
    }

    public void test_isEmpty() throws Exception {
        assertFalse(prepareOpt(new MockEntity()).isEmpty());
        assertTrue(prepareOpt(null).isEmpty());
    }

    public void test_get_basic() throws Exception {
        // ## Arrange ##
        MockEntity entity = new MockEntity();
        OptionalEntity<MockEntity> opt = prepareOpt(entity);

        // ## Act ##
        MockEntity actual = opt.get();

        // ## Assert ##
        assertEquals(entity, actual);
    }

    public void test_get_notFound() throws Exception {
        // ## Arrange ##
        OptionalEntity<MockEntity> opt = prepareOpt(null);

        // ## Act ##
        try {
            opt.get();
            // ## Assert ##
            fail();
        } catch (IllegalStateException e) {
            String msg = e.getMessage();
            log(msg);
            assertEquals("foo", msg);
        }
    }

    public void test_get_with_empty() throws Exception {
        // ## Arrange ##
        OptionalEntity<Entity> empty = OptionalEntity.empty();
        assertFalse(empty.isPresent());

        // ## Act ##
        try {
            empty.get();
            // ## Assert ##
            fail();
        } catch (EntityAlreadyDeletedException e) {
            log(e.getMessage());
        }
        assertEquals(OptionalEntity.empty(), empty);
    }

    // -----------------------------------------------------
    //                                             or/orElse
    //                                             ---------
    public void test_or_basic() throws Exception {
        MockEntity entity = new MockEntity();
        OptionalThing<MockEntity> opt = prepareOpt(entity).or(() -> {
            fail();
            return OptionalEntity.of(new MockEntity());
        });
        assertSame(entity, opt.get());
    }

    public void test_or_empty() throws Exception {
        MockEntity entity = new MockEntity();
        OptionalThing<MockEntity> opt = prepareOpt(null).or(() -> {
            markHere("called");
            return OptionalEntity.of(entity);
        });
        assertMarked("called");
        assertSame(entity, opt.get());
    }

    public void test_orElse_basic() throws Exception {
        {
            MockEntity entity = new MockEntity();
            assertEquals(entity, prepareOpt(entity).orElse(null));
        }
        {
            MockEntity entity = new MockEntity();
            assertEquals(entity, prepareOpt(null).orElse(entity));
        }
    }

    public void test_orElseGet_basic() throws Exception {
        {
            MockEntity entity = new MockEntity();
            assertEquals(entity, prepareOpt(entity).orElseGet(() -> null));
        }
        {
            MockEntity entity = new MockEntity();
            assertEquals(entity, prepareOpt(null).orElseGet(() -> entity));
        }
    }

    public void test_orElseThrow_default() throws Exception {
        try {
            prepareOpt(null).orElseThrow();
        } catch (IllegalStateException e) {
            assertEquals("foo", e.getMessage());
        }
        MockEntity entity = new MockEntity();
        assertEquals(entity, prepareOpt(entity).orElseThrow());
    }

    public void test_orElseThrow_lambda() throws Exception {
        try {
            prepareOpt(null).orElseThrow(() -> new IllegalStateException("sea"));
        } catch (IllegalStateException e) {
            assertEquals("sea", e.getMessage());
        }
        MockEntity entity = new MockEntity();
        assertEquals(entity, prepareOpt(entity).orElseThrow(() -> new IllegalStateException("sea")));
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

    // -----------------------------------------------------
    //                                            filter/map
    //                                            ----------
    public void test_map() throws Exception {
        assertNull(prepareOpt(null).map(obj -> "a").orElse(null));
        assertNull(prepareOpt(null).map(obj -> null).orElse(null));
        MockEntity entity = new MockEntity();
        assertNull(prepareOpt(entity).map(obj -> null).orElse(null));
        assertEquals("a", prepareOpt(entity).map(obj -> "a").get());
        assertEquals(entity.hashCode(), prepareOpt(entity).map(obj -> obj).get().hashCode());
    }

    public void test_flatmap() throws Exception {
        assertNull(prepareOpt(null).flatMap(OptionalEntity::of).orElse(null));
        assertNull(prepareOpt(null).flatMap(obj -> null).orElse(null));
        MockEntity entity = new MockEntity();
        entity.setMemberId(3);
        try {
            prepareOpt(entity).flatMap(obj -> null);
            fail();
        } catch (IllegalStateException e) {
            log(e.getMessage());
        }
        assertEquals(entity.hashCode(), prepareOpt(entity).flatMap(OptionalEntity::of).get().hashCode());
    }

    // -----------------------------------------------------
    //                                                stream
    //                                                ------
    public void test_stream_basic() throws Exception {
        {
            MockEntity entity = new MockEntity();
            assertEquals(entity, prepareOpt(entity).stream().findAny().get());
        }
        {
            assertFalse(prepareOpt(null).stream().findAny().isPresent());
        }
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    public void test_equals_basic() throws Exception {
        // ## Arrange ##
        MockEntity firstEntity = new MockEntity();
        firstEntity.setMemberId(3);
        OptionalEntity<MockEntity> firstOpt = prepareOpt(firstEntity);
        MockEntity secondEntity = new MockEntity();
        secondEntity.setMemberId(3);
        OptionalEntity<MockEntity> secondOpt = prepareOpt(secondEntity);

        // ## Act ##
        // ## Assert ##
        assertTrue(firstOpt.equals(secondOpt));
        secondOpt.get().setMemberId(4);
        assertFalse(firstOpt.equals(secondOpt));
    }

    public void test_equals_null() throws Exception {
        // ## Arrange ##
        OptionalEntity<MockEntity> firstOpt = prepareOpt(null);
        OptionalEntity<MockEntity> secondOpt = prepareOpt(null);

        // ## Act ##
        // ## Assert ##
        assertTrue(firstOpt.equals(secondOpt));
    }

    // ===================================================================================
    //                                                                         Test Helper
    //                                                                         ===========
    protected OptionalEntity<MockEntity> prepareOpt(MockEntity entity) {
        return new OptionalEntity<MockEntity>(entity, new OptionalThingExceptionThrower() {
            public void throwNotFoundException() {
                throw new IllegalStateException("foo");
            }
        });
    }
}
