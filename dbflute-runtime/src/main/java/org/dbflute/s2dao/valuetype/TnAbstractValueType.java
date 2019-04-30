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
package org.dbflute.s2dao.valuetype;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.dbflute.jdbc.ValueType;

/**
 * @author modified by jflute (originated in Seasar2)
 */
public abstract class TnAbstractValueType implements ValueType {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected int _sqlType;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public TnAbstractValueType(int sqlType) {
        this._sqlType = sqlType;
    }

    // ===================================================================================
    //                                                                        Null Setting
    //                                                                        ============
    protected void setNull(PreparedStatement ps, int index) throws SQLException {
        ps.setNull(index, _sqlType);
    }

    protected void setNull(CallableStatement cs, String parameterName) throws SQLException {
        cs.setNull(parameterName, _sqlType);
    }

    // ===================================================================================
    //                                                                       Out Parameter
    //                                                                       =============
    public void registerOutParameter(Connection conn, CallableStatement cs, int index) throws SQLException {
        cs.registerOutParameter(index, _sqlType);
    }

    public void registerOutParameter(Connection conn, CallableStatement cs, String parameterName) throws SQLException {
        cs.registerOutParameter(parameterName, _sqlType);
    }

    // ===================================================================================
    //                                                                            SQL Type
    //                                                                            ========
    public int getSqlType() {
        return _sqlType;
    }
}
