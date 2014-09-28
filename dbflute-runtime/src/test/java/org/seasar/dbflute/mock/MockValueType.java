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
package org.seasar.dbflute.mock;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.seasar.dbflute.jdbc.ValueType;

/**
 * @author jflute
 * @since 0.9.6.4 (2010/01/22 Friday)
 */
public class MockValueType implements ValueType {

    public int getSqlType() {
        return 0;
    }

    public Object getValue(ResultSet resultSet, int index) throws SQLException {
        return null;
    }

    public Object getValue(ResultSet resultSet, String columnName) throws SQLException {
        return null;
    }

    public Object getValue(CallableStatement cs, int index) throws SQLException {
        return null;
    }

    public Object getValue(CallableStatement cs, String parameterName) throws SQLException {
        return null;
    }

    public void bindValue(Connection conn, PreparedStatement ps, int index, Object value) throws SQLException {
    }

    public void bindValue(Connection conn, CallableStatement cs, String parameterName, Object value)
            throws SQLException {
    }

    public void registerOutParameter(Connection conn, CallableStatement cs, int index) throws SQLException {
    }

    public void registerOutParameter(Connection conn, CallableStatement cs, String parameterName) throws SQLException {
    }
}
