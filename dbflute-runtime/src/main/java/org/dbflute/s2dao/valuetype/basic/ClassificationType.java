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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.dbflute.jdbc.Classification;
import org.dbflute.jdbc.ClassificationCodeType;
import org.dbflute.s2dao.valuetype.TnAbstractValueType;
import org.dbflute.util.DfTypeUtil;

/**
 * The value type of classification. (DBFlute original)
 * @author jflute
 */
public class ClassificationType extends TnAbstractValueType {

    public ClassificationType() {
        super(Types.VARCHAR);
    }

    public Object getValue(ResultSet rs, int index) throws SQLException {
        String msg = "Getting as classification is unsupported: index=" + index;
        throw new UnsupportedOperationException(msg);
    }

    public Object getValue(ResultSet rs, String columnName) throws SQLException {
        String msg = "Getting as classification is unsupported: columnName=" + columnName;
        throw new UnsupportedOperationException(msg);
    }

    public Object getValue(CallableStatement cs, int index) throws SQLException {
        String msg = "Getting as classification for Procedure is unsupported: index=" + index;
        throw new UnsupportedOperationException(msg);
    }

    public Object getValue(CallableStatement cs, String parameterName) throws SQLException {
        String msg = "Getting as classification for Procedure is unsupported: parameterName=" + parameterName;
        throw new UnsupportedOperationException(msg);
    }

    public void bindValue(Connection conn, PreparedStatement ps, int index, Object value) throws SQLException {
        if (value == null) {
            setNull(ps, index);
        } else {
            if (!(value instanceof Classification)) {
                String msg = "The value should be classification:";
                msg = msg + " value=" + value + " type=" + value.getClass();
                throw new IllegalStateException(msg);
            }
            final Classification cls = (Classification) value;
            if (ClassificationCodeType.String.equals(cls.meta().codeType())) {
                ps.setString(index, cls.code());
            } else if (ClassificationCodeType.Number.equals(cls.meta().codeType())) {
                ps.setInt(index, DfTypeUtil.toInteger(cls.code()));
            } else if (ClassificationCodeType.Boolean.equals(cls.meta().codeType())) {
                ps.setBoolean(index, DfTypeUtil.toBoolean(cls.code()));
            } else {
                ps.setObject(index, cls.code());
            }
        }
    }

    public void bindValue(Connection conn, CallableStatement cs, String parameterName, Object value) throws SQLException {
        String msg = "Binding as classification for Procedure is unsupported: value=" + value;
        throw new UnsupportedOperationException(msg);
    }
}