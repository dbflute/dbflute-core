/*
 * Copyright 2014-2019 the original author or authors.
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.dbflute.s2dao.valuetype.TnAbstractValueType;
import org.dbflute.util.DfResourceUtil;

/**
 * @author modified by jflute (originated in Seasar2)
 */
public class BinaryStreamType extends TnAbstractValueType {

    public BinaryStreamType() {
        super(Types.BINARY);
    }

    public Object getValue(ResultSet rs, int index) throws SQLException {
        return rs.getBinaryStream(index);
    }

    public Object getValue(ResultSet rs, String columnName) throws SQLException {
        return rs.getBinaryStream(columnName);
    }

    public Object getValue(CallableStatement cs, int index) throws SQLException {
        return toBinaryStream(cs.getBytes(index));
    }

    public Object getValue(CallableStatement cs, String parameterName) throws SQLException {
        return toBinaryStream(cs.getBytes(parameterName));
    }

    private InputStream toBinaryStream(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        return new ByteArrayInputStream(bytes);
    }

    public void bindValue(Connection conn, PreparedStatement ps, int index, Object value) throws SQLException {
        if (value == null) {
            setNull(ps, index);
        } else if (value instanceof InputStream) {
            InputStream is = (InputStream) value;
            ps.setBinaryStream(index, is, DfResourceUtil.available(is));
        } else {
            ps.setObject(index, value);
        }
    }

    public void bindValue(Connection conn, CallableStatement cs, String parameterName, Object value) throws SQLException {
        if (value == null) {
            setNull(cs, parameterName);
        } else if (value instanceof InputStream) {
            InputStream is = (InputStream) value;
            cs.setBinaryStream(parameterName, is, DfResourceUtil.available(is));
        } else {
            cs.setObject(parameterName, value);
        }
    }
}