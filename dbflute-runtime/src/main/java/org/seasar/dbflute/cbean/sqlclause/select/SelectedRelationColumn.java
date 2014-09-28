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
package org.seasar.dbflute.cbean.sqlclause.select;

import org.seasar.dbflute.dbmeta.info.ColumnInfo;
import org.seasar.dbflute.dbmeta.name.ColumnSqlName;

/**
 * @author jflute
 */
public class SelectedRelationColumn {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String _tableAliasName;
    protected ColumnInfo _columnInfo;
    protected String _relationNoSuffix; // e.g. _0_3

    // ===================================================================================
    //                                                                              Naming
    //                                                                              ======
    public String buildRealColumnSqlName() {
        final ColumnSqlName columnSqlName = _columnInfo.getColumnSqlName();
        if (_tableAliasName != null) {
            return _tableAliasName + "." + columnSqlName;
        } else {
            return columnSqlName.toString();
        }
    }

    public String buildColumnAliasName() {
        return _columnInfo.getColumnDbName() + _relationNoSuffix; // e.g. FOO_0_3
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getTableAliasName() {
        return _tableAliasName;
    }

    public void setTableAliasName(String tableAliasName) {
        _tableAliasName = tableAliasName;
    }

    public ColumnInfo getColumnInfo() {
        return _columnInfo;
    }

    public void setColumnInfo(ColumnInfo columnInfo) {
        _columnInfo = columnInfo;
    }

    public String getRelationNoSuffix() {
        return _relationNoSuffix;
    }

    public void setRelationNoSuffix(String relationNoSuffix) {
        _relationNoSuffix = relationNoSuffix;
    }
}
