/*
 * Copyright 2014-2015 the original author or authors.
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

    public void test_isPresent() throws Exception {
        assertTrue(prepareOpt(new MockEntity()).isPresent());
        assertFalse(prepareOpt(null).isPresent());
    }

    public void test_empty() throws Exception {
        OptionalEntity<Entity> empty = OptionalEntity.empty();
        assertFalse(empty.isPresent());
        try {
            empty.get();
            fail();
        } catch (EntityAlreadyDeletedException e) {
            log(e.getMessage());
        }
        assertEquals(OptionalEntity.empty(), empty);
    }

    public void test_orElseThrow() throws Exception {
        try {
            prepareOpt(null).orElseThrow(() -> new IllegalStateException("sea"));
        } catch (IllegalStateException e) {
            assertEquals("sea", e.getMessage());
        }
        MockEntity entity = new MockEntity();
        assertEquals(entity, prepareOpt(entity).orElseThrow(() -> new IllegalStateException("sea")));
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

    public void test_empty_flatmap() throws Exception {
        // ## Arrange ##
        OptionalEntity<MockEntity> opt = prepareOpt(null);
        // ## Act ##
        MockEntity mock = opt.flatMap(OptionalEntity::of).orElseNull();
        // ## Assert ##
        assertNull(mock);
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
