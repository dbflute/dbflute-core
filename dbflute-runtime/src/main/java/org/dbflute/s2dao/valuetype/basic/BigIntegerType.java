/*
 * Copyright 2014-2025 the original author or authors.
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
package org.dbflute.s2dao.valuetype.basic;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.dbflute.s2dao.valuetype.TnAbstractValueType;
import org.dbflute.util.DfTypeUtil;

/**
 * @author modified by jflute (originated in Seasar2)
 */
public class BigIntegerType extends TnAbstractValueType {

    public BigIntegerType() {
        super(Types.BIGINT);
    }

    public Object getValue(ResultSet rs, final int index) throws SQLException {
        return DfTypeUtil.toBigInteger(rs.getBigDecimal(index));
    }

    public Object getValue(ResultSet rs, final String columnName) throws SQLException {
        return DfTypeUtil.toBigInteger(rs.getBigDecimal(columnName));
    }

    public Object getValue(CallableStatement cs, final int index) throws SQLException {
        return DfTypeUtil.toBigInteger(cs.getBigDecimal(index));
    }

    public Object getValue(CallableStatement cs, final String parameterName) throws SQLException {
        return DfTypeUtil.toBigInteger(cs.getBigDecimal(parameterName));
    }

    public void bindValue(Connection conn, PreparedStatement ps, final int index, final Object value) throws SQLException {
        if (value == null) {
            setNull(ps, index);
        } else {
            ps.setBigDecimal(index, DfTypeUtil.toBigDecimal(value));
        }
    }

    public void bindValue(Connection conn, CallableStatement cs, final String parameterName, final Object value) throws SQLException {
        if (value == null) {
            setNull(cs, parameterName);
        } else {
            cs.setBigDecimal(parameterName, DfTypeUtil.toBigDecimal(value));
        }
    }
}
