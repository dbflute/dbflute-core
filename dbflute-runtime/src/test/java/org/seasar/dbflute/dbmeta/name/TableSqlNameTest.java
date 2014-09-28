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
package org.seasar.dbflute.dbmeta.name;

import org.seasar.dbflute.unit.core.PlainTestCase;

/**
 * @author jflute
 */
public class TableSqlNameTest extends PlainTestCase {

    public void test_basic() throws Exception {
        TableSqlName tableSqlName = new TableSqlName("FOO_SQL", "BAR_DB");
        assertEquals("FOO_SQL", tableSqlName.toString());
        assertEquals("BAR_DB", tableSqlName.getCorrespondingDbName());
        assertFalse(tableSqlName._locked);
        tableSqlName.xacceptFilter(new SqlNameFilter() {
            public String filter(String sqlName, String correspondingDbName) {
                assertEquals("FOO_SQL", sqlName);
                assertEquals("BAR_DB", correspondingDbName);
                return "filtered." + sqlName;
            }
        });
        assertEquals("filtered.FOO_SQL", tableSqlName.toString());
        assertTrue(tableSqlName._locked);
        try {
            tableSqlName.xacceptFilter(null);

            fail();
        } catch (IllegalStateException e) {
            // OK
            log(e.getMessage());
        }
    }
}
