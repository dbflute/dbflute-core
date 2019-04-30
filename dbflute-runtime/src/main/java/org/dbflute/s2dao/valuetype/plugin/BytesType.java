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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
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
public class BytesType extends TnAbstractValueType {

    public static final byte[] EMPTY_BYTES = new byte[0];
    public static final Trait BYTES_TRAIT = new BytesTrait();
    public static final Trait STREAM_TRAIT = new StreamTrait();
    public static final Trait BLOB_TRAIT = new BlobTrait();

    protected final Trait trait;

    public BytesType(Trait trait) {
        super(trait.getSqlType());
        this.trait = trait;
    }

    public void bindValue(Connection conn, PreparedStatement ps, int index, Object value) throws SQLException {
        if (value == null) {
            setNull(ps, index);
        } else if (value instanceof byte[]) {
            trait.set(ps, index, (byte[]) value);
        } else {
            ps.setObject(index, value);
        }
    }

    public void bindValue(Connection conn, CallableStatement cs, String parameterName, Object value) throws SQLException {
        if (value == null) {
            setNull(cs, parameterName);
        } else if (value instanceof byte[]) {
            trait.set(cs, parameterName, (byte[]) value);
        } else {
            cs.setObject(parameterName, value);
        }
    }

    public Object getValue(ResultSet rs, int index) throws SQLException {
        return trait.get(rs, index);
    }

    public Object getValue(ResultSet rs, String columnName) throws SQLException {
        return trait.get(rs, columnName);
    }

    public Object getValue(CallableStatement cs, int index) throws SQLException {
        return trait.get(cs, index);
    }

    public Object getValue(CallableStatement cs, String parameterName) throws SQLException {
        return trait.get(cs, parameterName);
    }

    public static byte[] toBytes(final InputStream is) throws SQLException {
        try {
            final byte[] bytes = new byte[is.available()];
            is.read(bytes);
            return bytes;
        } catch (final IOException e) {
            String msg = "The IOException occurred: " + is;
            throw new IllegalStateException(msg, e);
        }
    }

    public static byte[] toBytes(Blob blob) throws SQLException {
        if (blob == null) {
            return null;
        }
        final long length = blob.length();
        if (length == 0) {
            return EMPTY_BYTES;
        }
        if (length > Integer.MAX_VALUE) {
            throw new ArrayIndexOutOfBoundsException();
        }
        return blob.getBytes(1L, (int) length);
    }

    /**
     * @author jflute
     */
    public interface Trait {

        int getSqlType();

        void set(PreparedStatement ps, int parameterIndex, byte[] bytes) throws SQLException;

        void set(CallableStatement cs, String parameterName, byte[] bytes) throws SQLException;

        byte[] get(ResultSet rs, int columnIndex) throws SQLException;

        byte[] get(ResultSet rs, String columnName) throws SQLException;

        byte[] get(CallableStatement cs, int columnIndex) throws SQLException;

        byte[] get(CallableStatement cs, String columnName) throws SQLException;

    }

    /**
     * @author jflute
     */
    public static class BytesTrait implements Trait {

        public int getSqlType() {
            return Types.BINARY;
        }

        public void set(final PreparedStatement ps, final int parameterIndex, final byte[] bytes) throws SQLException {
            ps.setBytes(parameterIndex, bytes);
        }

        public void set(final CallableStatement cs, final String parameterName, final byte[] bytes) throws SQLException {
            cs.setBytes(parameterName, bytes);
        }

        public byte[] get(final ResultSet rs, final int columnIndex) throws SQLException {
            return rs.getBytes(columnIndex);
        }

        public byte[] get(final ResultSet rs, final String columnName) throws SQLException {
            return rs.getBytes(columnName);
        }

        public byte[] get(final CallableStatement cs, final int columnIndex) throws SQLException {
            return cs.getBytes(columnIndex);
        }

        public byte[] get(final CallableStatement cs, final String columnName) throws SQLException {
            return cs.getBytes(columnName);
        }
    }

    /**
     * @author jflute
     */
    public static class StreamTrait implements Trait {

        public int getSqlType() {
            return Types.BINARY;
        }

        public void set(final PreparedStatement ps, final int parameterIndex, final byte[] bytes) throws SQLException {
            ps.setBinaryStream(parameterIndex, new ByteArrayInputStream(bytes), bytes.length);
        }

        public void set(final CallableStatement cs, final String parameterName, final byte[] bytes) throws SQLException {
            cs.setBinaryStream(parameterName, new ByteArrayInputStream(bytes), bytes.length);
        }

        public byte[] get(final ResultSet rs, final int columnIndex) throws SQLException {
            final InputStream is = rs.getBinaryStream(columnIndex);
            try {
                return toBytes(is);
            } finally {
                DfResourceUtil.close(is);
            }
        }

        public byte[] get(final ResultSet rs, final String columnName) throws SQLException {
            final InputStream is = rs.getBinaryStream(columnName);
            try {
                return toBytes(is);
            } finally {
                DfResourceUtil.close(is);
            }
        }

        public byte[] get(final CallableStatement cs, final int columnIndex) throws SQLException {
            return cs.getBytes(columnIndex);
        }

        public byte[] get(final CallableStatement cs, final String columnName) throws SQLException {
            return cs.getBytes(columnName);
        }
    }

    /**
     * @author jflute
     */
    public static class BlobTrait implements Trait {

        public int getSqlType() {
            return Types.BLOB;
        }

        public void set(final PreparedStatement ps, final int parameterIndex, final byte[] bytes) throws SQLException {
            ps.setBinaryStream(parameterIndex, new ByteArrayInputStream(bytes), bytes.length);
        }

        public void set(final CallableStatement cs, final String parameterName, final byte[] bytes) throws SQLException {
            cs.setBinaryStream(parameterName, new ByteArrayInputStream(bytes), bytes.length);
        }

        public byte[] get(final ResultSet rs, final int columnIndex) throws SQLException {
            return toBytes(rs.getBlob(columnIndex));
        }

        public byte[] get(final ResultSet rs, final String columnName) throws SQLException {
            return toBytes(rs.getBlob(columnName));
        }

        public byte[] get(final CallableStatement cs, final int columnIndex) throws SQLException {
            return toBytes(cs.getBlob(columnIndex));
        }

        public byte[] get(final CallableStatement cs, final String columnName) throws SQLException {
            return toBytes(cs.getBlob(columnName));
        }
    }
}
