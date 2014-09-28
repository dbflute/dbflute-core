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
package org.seasar.dbflute.exception.thrower;

import org.seasar.dbflute.exception.EntityAlreadyDeletedException;
import org.seasar.dbflute.exception.EntityDuplicatedException;
import org.seasar.dbflute.unit.core.PlainTestCase;

/**
 * @author jflute
 */
public class BehaviorExceptionThrowerTest extends PlainTestCase {

    public void test_throwEntityAlreadyDeletedException() {
        try {
            createTarget().throwSelectEntityAlreadyDeletedException("foo");

            fail();
        } catch (EntityAlreadyDeletedException e) {
            // OK
            log(e.getMessage());
            assertTrue(e.getMessage().contains("foo"));
        }
    }

    public void test_throwEntityDuplicatedException() {
        try {
            createTarget().throwSelectEntityDuplicatedException("123", "foo", new Exception());

            fail();
        } catch (EntityDuplicatedException e) {
            // OK
            log(e.getMessage());
            assertTrue(e.getMessage().contains("123"));
            assertTrue(e.getMessage().contains("foo"));
        }
    }

    protected BehaviorExceptionThrower createTarget() {
        return new BehaviorExceptionThrower();
    }
}
