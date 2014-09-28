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

import org.seasar.dbflute.dbmeta.info.ColumnInfo;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public class ColumnRealName implements Serializable {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _tableAliasName;
    protected final ColumnSqlName _columnSqlName;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    protected ColumnRealName(String tableAliasName, ColumnSqlName columnSqlName) {
        _tableAliasName = tableAliasName;
        _columnSqlName = columnSqlName;
    }

    public static ColumnRealName create(String tableAliasName, ColumnSqlName columnSqlName) {
        return new ColumnRealName(tableAliasName, columnSqlName);
    }

    public static ColumnRealName create(String tableAliasName, ColumnInfo columnInfo) {
        return new ColumnRealName(tableAliasName, columnInfo.getColumnSqlName());
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ColumnRealName)) {
            return false;
        }
        final ColumnRealName target = (ColumnRealName) obj;
        return toString().equals(target.toString());
    }

    @Override
    public String toString() {
        if (Srl.is_NotNull_and_NotTrimmedEmpty(_tableAliasName)) {
            return _tableAliasName + "." + _columnSqlName;
        } else {
            return _columnSqlName.toString();
        }
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getTableAliasName() {
        return _tableAliasName;
    }

    public ColumnSqlName getColumnSqlName() {
        return _columnSqlName;
    }
}
