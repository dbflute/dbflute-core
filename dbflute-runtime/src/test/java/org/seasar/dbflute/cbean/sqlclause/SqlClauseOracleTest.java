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
package org.seasar.dbflute.cbean.sqlclause;

import java.util.HashSet;
import java.util.Set;

import org.seasar.dbflute.unit.core.PlainTestCase;

/**
 * @author jflute
 * @since 0.9.1 (2009/02/08 Sunday)
 */
public class SqlClauseOracleTest extends PlainTestCase {

    public void test_lockForUpdateNoWait() {
        // ## Arrange ##
        final Set<String> markSet = new HashSet<String>();
        SqlClauseOracle target = new SqlClauseOracle("test") {
            private static final long serialVersionUID = 1L;

            @Override
            public void lockForUpdate() {
                _lockSqlSuffix = " for update of dummy";
                markSet.add("lockForUpdate");
            }
        };

        // ## Act ##
        target.lockForUpdateNoWait();

        // ## Assert ##
        log(target._lockSqlSuffix);
        assertTrue(target._lockSqlSuffix.endsWith(" nowait"));
        assertTrue(markSet.contains("lockForUpdate"));

        // Should be overridden lockSqlSuffix.
        target.lockForUpdateWait(123);
        log(target._lockSqlSuffix);
        assertTrue(target._lockSqlSuffix.endsWith(" wait 123"));
    }

    public void test_lockForUpdateWait() {
        // ## Arrange ##
        final Set<String> markSet = new HashSet<String>();
        SqlClauseOracle target = new SqlClauseOracle("test") {
            private static final long serialVersionUID = 1L;

            @Override
            public void lockForUpdate() {
                _lockSqlSuffix = " for update of dummy";
                markSet.add("lockForUpdate");
            }
        };

        // ## Act ##
        target.lockForUpdateWait(123);

        // ## Assert ##
        log(target._lockSqlSuffix);
        assertTrue(target._lockSqlSuffix.endsWith(" wait 123"));
        assertTrue(markSet.contains("lockForUpdate"));

        // Should be overridden lockSqlSuffix.
        target.lockForUpdateNoWait();
        log(target._lockSqlSuffix);
        assertTrue(target._lockSqlSuffix.endsWith(" nowait"));
    }

    public void test_escapeFullTextSearchValue() {
        // ## Arrange ##
        SqlClauseOracle target = new SqlClauseOracle("test");

        // ## Act & Assert ##
        assertEquals("{foo}", target.escapeFullTextSearchValue("foo"));
        assertEquals("{f{oo}", target.escapeFullTextSearchValue("f{oo"));
        assertEquals("{fo}}o}", target.escapeFullTextSearchValue("fo}o"));
        assertEquals("{f{o}}o}", target.escapeFullTextSearchValue("f{o}o"));
    }
}
