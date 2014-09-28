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
package org.seasar.dbflute;

import org.seasar.dbflute.dbway.DBWay;
import org.seasar.dbflute.dbway.WayOfMySQL;
import org.seasar.dbflute.unit.core.PlainTestCase;

/**
 * @author jflute
 * @since 1.0.5F (2014/05/05 Monday)
 */
public class DBDefTest extends PlainTestCase {

    public void test_switchDBWay_basic() throws Exception {
        // ## Arrange ##
        DBWay original = DBDef.MySQL.dbway();
        assertEquals(WayOfMySQL.class, original.getClass());

        // ## Act ##
        try {
            assertTrue(DBDef.MySQL.isLocked());
            DBDef.MySQL.unlock();
            assertTrue(DBDef.Oracle.isLocked());
            assertFalse(DBDef.MySQL.isLocked());
            DBDef.MySQL.switchDBWay(new WayOfMySQL() {

                private static final long serialVersionUID = 1L;

                @Override
                public String getIdentitySelectSql() {
                    return "foo";
                }
            });

            // ## Assert ##
            assertTrue(DBDef.MySQL.isLocked());
            assertEquals("foo", DBDef.MySQL.dbway().getIdentitySelectSql());
        } finally {
            DBDef.MySQL.unlock();
            DBDef.MySQL.switchDBWay(original);
            assertTrue(DBDef.MySQL.isLocked());
            assertTrue(DBDef.Oracle.isLocked());
        }
    }

    public void test_switchDBWay_locked() throws Exception {
        try {
            assertTrue(DBDef.MySQL.isLocked());
            DBDef.MySQL.switchDBWay(new WayOfMySQL() {

                private static final long serialVersionUID = 1L;

                @Override
                public String getIdentitySelectSql() {
                    return "foo";
                }
            });
            fail();
        } catch (IllegalStateException e) {
            log(e.getMessage());
        } finally {
            assertTrue(DBDef.MySQL.isLocked());
            assertTrue(DBDef.Oracle.isLocked());
        }
    }

    public void test_switchDBWay_null() throws Exception {
        try {
            assertTrue(DBDef.MySQL.isLocked());
            DBDef.MySQL.unlock();
            DBDef.MySQL.switchDBWay(null);
            assertFalse(DBDef.MySQL.isLocked());
            fail();
        } catch (IllegalArgumentException e) {
            log(e.getMessage());
        } finally {
            assertFalse(DBDef.MySQL.isLocked());
            assertTrue(DBDef.Oracle.isLocked());
            DBDef.MySQL.lock();
            assertTrue(DBDef.MySQL.isLocked());
        }
    }
}
