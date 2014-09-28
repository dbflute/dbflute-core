/*
 * Copyright 2004-2014 the Seasar Foundation and the Others.
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
package org.seasar.dbflute.helper.jdbc.facade;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.seasar.dbflute.jdbc.ValueType;

/**
 * @author jflute
 * @since 0.9.8.3 (2011/04/25 Monday)
 */
public class DfJFadResultSetWrapper {

    protected final ResultSet _rs;
    protected final Map<String, ValueType> _columnValueTypeMap;
    protected final DfJFadStringConverter _stringConverter;

    public DfJFadResultSetWrapper(ResultSet rs, Map<String, ValueType> columnValueTypeMap,
            DfJFadStringConverter stringConverter) {
        _rs = rs;
        _columnValueTypeMap = columnValueTypeMap;
        _stringConverter = stringConverter;
    }

    public boolean next() throws SQLException {
        return _rs.next();
    }

    public Object getObject(String columnName) throws SQLException {
        final ValueType valueType = _columnValueTypeMap.get(columnName);
        if (valueType != null) {
            return valueType.getValue(_rs, columnName);
        } else {
            return _rs.getObject(columnName);
        }
    }

    public String getString(String columnName) throws SQLException {
        final Object value;
        final ValueType valueType = _columnValueTypeMap.get(columnName);
        if (valueType != null) {
            value = valueType.getValue(_rs, columnName);
        } else {
            value = _rs.getString(columnName);
        }
        if (value == null) {
            return null;
        }
        if (_stringConverter != null) {
            return _stringConverter.convert(value);
        } else {
            return value.toString();
        }
    }
}
