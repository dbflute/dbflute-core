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

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * @author jflute
 */
public class PostgreSQLByteaType extends BytesType {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public PostgreSQLByteaType() {
        super(new TnByteaTrait());
    }

    // ===================================================================================
    //                                                                          Blob Trait
    //                                                                          ==========
    protected static class TnByteaTrait implements Trait {

        public int getSqlType() {
            return Types.BINARY;
        }

        public void set(PreparedStatement ps, int parameterIndex, byte[] bytes) throws SQLException {
            ps.setBytes(parameterIndex, bytes);
        }

        public void set(CallableStatement cs, String parameterName, byte[] bytes) throws SQLException {
            cs.setBytes(parameterName, bytes);
        }

        public byte[] get(ResultSet rs, int columnIndex) throws SQLException {
            return rs.getBytes(columnIndex);
        }

        public byte[] get(ResultSet rs, String columnName) throws SQLException {
            return rs.getBytes(columnName);
        }

        public byte[] get(CallableStatement cs, int columnIndex) throws SQLException {
            return cs.getBytes(columnIndex);
        }

        public byte[] get(CallableStatement cs, String columnName) throws SQLException {
            return cs.getBytes(columnName);
        }
    }
}