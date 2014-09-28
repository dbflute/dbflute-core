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
package org.seasar.dbflute.cbean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.seasar.dbflute.unit.core.PlainTestCase;

/**
 * @author jflute
 */
public class SimpleMapPmbTest extends PlainTestCase {

    public void test_size() {
        // ## Arrange ##
        SimpleMapPmb<Object> pmb = createTarget();
        pmb.addParameter("foo", "value");

        // ## Act ##
        int size = pmb.size();

        // ## Assert ##
        assertEquals(1, size);
    }

    public void test_isEmpty() {
        // ## Arrange ##
        SimpleMapPmb<Object> pmb = createTarget();
        assertTrue(pmb.isEmpty());
        pmb.addParameter("foo", "value");

        // ## Act ##
        boolean empty = pmb.isEmpty();

        // ## Assert ##
        assertFalse(empty);
    }

    public void test_values() {
        // ## Arrange ##
        SimpleMapPmb<Object> pmb = createTarget();
        assertTrue(pmb.isEmpty());
        pmb.addParameter("foo", "value1");
        pmb.addParameter("bar", "value2");

        // ## Act ##
        Collection<Object> values = pmb.values();

        // ## Assert ##
        assertEquals(2, values.size());
        List<Object> ls = new ArrayList<Object>(values);
        assertEquals("value1", ls.get(0));
        assertEquals("value2", ls.get(1));
    }

    protected SimpleMapPmb<Object> createTarget() {
        return new SimpleMapPmb<Object>();
    }
}
