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
package org.seasar.dbflute.helper.jdbc.facade;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.seasar.dbflute.exception.SQLFailureException;
import org.seasar.dbflute.unit.core.PlainTestCase;

/**
 * @author jflute
 */
public class DfJdbcFacadeTest extends PlainTestCase {

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
