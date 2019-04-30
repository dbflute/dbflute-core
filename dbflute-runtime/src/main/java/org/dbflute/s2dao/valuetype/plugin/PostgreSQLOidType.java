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
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * The value type of bytes OID. (basically for PostgreSQL)
 * @author modified by jflute (originated in Seasar2)
 */
public class PostgreSQLOidType extends BytesType {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public PostgreSQLOidType() {
        super(new TnBytesOidTrait());
    }

    // ===================================================================================
    //                                                                          Blob Trait
    //                                                                          ==========
    protected static class TnBytesOidTrait implements Trait {

        public int getSqlType() {
            return Types.BLOB;
        }

        public void set(PreparedStatement ps, int parameterIndex, byte[] bytes) throws SQLException {
            ps.setBlob(parameterIndex, createBytesOidImpl(bytes));
        }

        protected Blob createBytesOidImpl(byte[] bytes) {
            return new TnBytesOidImpl(bytes);
        }

        public void set(CallableStatement cs, String parameterName, byte[] bytes) throws SQLException {
            cs.setBlob(parameterName, createBytesOidImpl(bytes));
        }

        public byte[] get(ResultSet rs, int columnIndex) throws SQLException {
            return BytesType.toBytes(rs.getBlob(columnIndex));
        }

        public byte[] get(ResultSet rs, String columnName) throws SQLException {
            return BytesType.toBytes(rs.getBlob(columnName));
        }

        public byte[] get(CallableStatement cs, int columnIndex) throws SQLException {
            return BytesType.toBytes(cs.getBlob(columnIndex));
        }

        public byte[] get(CallableStatement cs, String columnName) throws SQLException {
            return BytesType.toBytes(cs.getBlob(columnName));
        }
    }

    // ===================================================================================
    //                                                                 Blob Implementation
    //                                                                 ===================
    protected static class TnBytesOidImpl implements Blob {

        protected byte[] _bytes;

        public TnBytesOidImpl(byte[] bytes) {
            this._bytes = bytes;
        }

        public void free() throws SQLException { // for JDK-6.0
            throw new UnsupportedOperationException("free()");
        }

        public InputStream getBinaryStream() throws SQLException {
            return new ByteArrayInputStream(_bytes);
        }

        public InputStream getBinaryStream(long pos, long length) throws SQLException { // for JDK-6.0
            throw new UnsupportedOperationException("getBinaryStream(pos, length)");
        }

        public byte[] getBytes(long pos, int length) throws SQLException {
            if (length == _bytes.length) {
                return _bytes;
            }
            final byte[] result = new byte[length];
            System.arraycopy(_bytes, 0, result, 0, length);
            return result;
        }

        public long length() throws SQLException {
            return _bytes.length;
        }

        public long position(Blob pattern, long start) throws SQLException {
            throw new UnsupportedOperationException("position");
        }

        public long position(byte[] pattern, long start) throws SQLException {
            throw new UnsupportedOperationException("position");
        }

        public OutputStream setBinaryStream(long pos) throws SQLException {
            throw new UnsupportedOperationException("setBinaryStream");
        }

        public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException {
            throw new UnsupportedOperationException("setBytes");
        }

        public int setBytes(long pos, byte[] bytes) throws SQLException {
            throw new UnsupportedOperationException("setBytes");
        }

        public void truncate(long len) throws SQLException {
            throw new UnsupportedOperationException("truncate");
        }
    }
}