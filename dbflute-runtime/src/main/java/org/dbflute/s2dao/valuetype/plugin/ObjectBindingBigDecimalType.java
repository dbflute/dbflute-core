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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.dbflute.s2dao.valuetype.basic.BigDecimalType;
import org.dbflute.util.DfTypeUtil;

/**
 * The value type for BigDecimal which uses setObject() when binding. <br>
 * Basically for NUMERIC of PostgreSQL's procedure parameter which needs setObject().
 * @author modified by jflute (originated in Seasar2)
 */
public class ObjectBindingBigDecimalType extends BigDecimalType {

    public ObjectBindingBigDecimalType() {
    }

    @Override
    public void bindValue(Connection conn, PreparedStatement ps, int index, Object value) throws SQLException {
        if (value == null) {
            setNull(ps, index);
        } else {
            ps.setObject(index, DfTypeUtil.toBigDecimal(value), getSqlType());
        }
    }

    @Override
    public void bindValue(Connection conn, CallableStatement cs, String parameterName, Object value) throws SQLException {
        if (value == null) {
            setNull(cs, parameterName);
        } else {
            cs.setObject(parameterName, DfTypeUtil.toBigDecimal(value), getSqlType());
        }
    }
}