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
package org.seasar.dbflute.dbmeta.name;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The value class for the SQL name of column.
 * @author jflute
 */
public class ColumnSqlName implements Serializable {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // using concurrent one just in case 
    private static final Map<Character, Object> _basicCharMap = new ConcurrentHashMap<Character, Object>();
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("abcdefghijklmnopqrstuvwxyz");
        sb.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        sb.append("0123456789");
        sb.append("_");
        final String basicCharStr = sb.toString();
        final Object dummyObj = new Object();
        for (int i = 0; i < basicCharStr.length(); i++) {
            final char ch = basicCharStr.charAt(i);
            _basicCharMap.put(ch, dummyObj);
        }
    }

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _columnSqlName;
    protected final boolean _irregularChar;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public ColumnSqlName(String columnSqlName) {
        _columnSqlName = columnSqlName;
        _irregularChar = analyzeIrregularChar(columnSqlName);
    }

    protected boolean analyzeIrregularChar(String columnSqlName) {
        for (int i = 0; i < columnSqlName.length(); i++) {
            final char ch = columnSqlName.charAt(i);
            if (!_basicCharMap.containsKey(ch)) {
                return true;
            }
        }
        return false;
    }

    // ===================================================================================
    //                                                                      Irregular Char
    //                                                                      ==============
    public boolean hasIrregularChar() {
        return _irregularChar;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public int hashCode() {
        return _columnSqlName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ColumnSqlName)) {
            return false;
        }
        final ColumnSqlName target = (ColumnSqlName) obj;
        return _columnSqlName.equals(target._columnSqlName);
    }

    @Override
    public String toString() {
        return _columnSqlName;
    }
}
