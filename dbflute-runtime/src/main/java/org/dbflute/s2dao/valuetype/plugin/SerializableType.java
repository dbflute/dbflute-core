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
package org.dbflute.s2dao.valuetype.plugin;

import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.dbflute.util.DfTypeUtil;

/**
 * @author modified by jflute (originated in Seasar2)
 */
public class SerializableType extends BytesType {

    public SerializableType(Trait trait) {
        super(trait);
    }

    @Override
    public Object getValue(ResultSet rs, final int index) throws SQLException {
        return deserialize(super.getValue(rs, index));
    }

    @Override
    public Object getValue(ResultSet rs, final String columnName) throws SQLException {
        return deserialize(super.getValue(rs, columnName));
    }

    @Override
    public Object getValue(CallableStatement cs, final int index) throws SQLException {
        return deserialize(super.getValue(cs, index));
    }

    @Override
    public Object getValue(CallableStatement cs, final String parameterName) throws SQLException {
        return deserialize(super.getValue(cs, parameterName));
    }

    @Override
    public void bindValue(Connection conn, PreparedStatement ps, final int index, final Object value) throws SQLException {
        super.bindValue(conn, ps, index, serialize(value));
    }

    @Override
    public void bindValue(Connection conn, CallableStatement cs, final String parameterName, final Object value) throws SQLException {
        super.bindValue(conn, cs, parameterName, serialize(value));
    }

    protected byte[] serialize(final Object obj) throws SQLException {
        return DfTypeUtil.toBinary((Serializable) obj);
    }

    protected Serializable deserialize(final Object bytes) throws SQLException {
        return DfTypeUtil.toSerializable((byte[]) bytes);
    }
}
