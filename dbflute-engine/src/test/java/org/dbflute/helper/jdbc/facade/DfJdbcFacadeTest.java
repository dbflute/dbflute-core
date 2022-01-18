/*
 * Copyright 2014-2021 the original author or authors.
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
package org.dbflute.helper.jdbc.facade;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.dbflute.exception.SQLFailureException;
import org.dbflute.unit.EngineTestCase;

/**
 * @author jflute
 */
public class DfJdbcFacadeTest extends EngineTestCase {

    public void test_handleSQLException() throws Exception {
        // ## Arrange ##
        DfJdbcFacade facade = new DfJdbcFacade((DataSource) null);
        String sql = "select * from dual";
        SQLException e = new SQLException("foo message");

        try {
            // ## Act ##
            facade.handleSQLException(sql, e);

            // ## Assert ##
            fail();
        } catch (SQLFailureException actual) {
            // OK
            log(actual.getMessage());
            assertEquals(e, actual.getCause());
        }
    }
}
