/*
 * Copyright 2014-2017 the original author or authors.
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
import java.sql.Timestamp;
import java.sql.Types;
import java.util.TimeZone;

import org.dbflute.bhv.core.context.ResourceContext;
import org.dbflute.s2dao.valuetype.TnAbstractValueType;
import org.dbflute.system.DBFluteSystem;
import org.dbflute.util.DfTypeUtil;

/**
 * @author jflute
 * @since 1.1.0 (2014/11/03 Monday)
 */
public class LocalDateAsTimestampType extends TnAbstractValueType {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final TimestampType _timestampType = new TimestampType();

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public LocalDateAsTimestampType() {
        super(Types.TIMESTAMP);
    }

    // ===================================================================================
    //                                                                           Get Value
    //                                                                           =========
    public Object getValue(ResultSet rs, int index) throws SQLException {
        return toLocalDate(_timestampType.getValue(rs, index));
    }

    public Object getValue(ResultSet rs, String columnName) throws SQLException {
        return toLocalDate(_timestampType.getValue(rs, columnName));
    }

    public Object getValue(CallableStatement cs, int index) throws SQLException {
        return toLocalDate(_timestampType.getValue(cs, index));
    }

    public Object getValue(CallableStatement cs, String parameterName) throws SQLException {
        return toLocalDate(_timestampType.getValue(cs, parameterName));
    }

    // ===================================================================================
    //                                                                          Bind Value
    //                                                                          ==========
    public void bindValue(Connection conn, PreparedStatement ps, int index, Object value) throws SQLException {
        _timestampType.bindValue(conn, ps, index, toTimestamp(value));
    }

    public void bindValue(Connection conn, CallableStatement cs, String parameterName, Object value) throws SQLException {
        _timestampType.bindValue(conn, cs, parameterName, toTimestamp(value));
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected Object toLocalDate(Object date) { // object for override
        return DfTypeUtil.toLocalDate(date, getTimeZone());
    }

    protected Timestamp toTimestamp(Object date) {
        return DfTypeUtil.toTimestamp(date, getTimeZone());
    }

    protected TimeZone getTimeZone() {
        if (ResourceContext.isExistResourceContextOnThread()) {
            final TimeZone provided = ResourceContext.provideMappingDateTimeZone();
            if (provided != null) {
                return provided;
            }
        }
        return DBFluteSystem.getFinalTimeZone();
    }
}