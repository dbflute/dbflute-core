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
package org.seasar.dbflute.s2dao.metadata.impl;

import org.seasar.dbflute.dbmeta.info.ColumnInfo;
import org.seasar.dbflute.dbmeta.name.ColumnSqlName;
import org.seasar.dbflute.helper.beans.DfPropertyAccessor;
import org.seasar.dbflute.helper.beans.DfPropertyDesc;
import org.seasar.dbflute.jdbc.ValueType;
import org.seasar.dbflute.s2dao.metadata.TnPropertyType;
import org.seasar.dbflute.s2dao.valuetype.TnValueTypes;
import org.seasar.dbflute.util.DfTypeUtil;

/**
 * @author modified by jflute (originated in S2Dao)
 */
public class TnPropertyTypeImpl implements TnPropertyType {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfPropertyDesc _propertyDesc;
    protected final ValueType _valueType;
    protected final String _propertyName;
    protected final String _columnDbName;
    protected final ColumnSqlName _columnSqlName;
    protected final ColumnInfo _entityColumnInfo; // not required
    protected boolean _primaryKey = false;
    protected boolean _persistent = true;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public TnPropertyTypeImpl(DfPropertyDesc propertyDesc) {
        // for non persistent property (for example, relation)
        this(propertyDesc, TnValueTypes.DEFAULT_OBJECT, propertyDesc.getPropertyName(), new ColumnSqlName(
                propertyDesc.getPropertyName()), null);
    }

    public TnPropertyTypeImpl(DfPropertyDesc propertyDesc, ValueType valueType, String columnDbName,
            ColumnSqlName columnSqlName, ColumnInfo entityColumnInfo) {
        _propertyDesc = propertyDesc;
        _propertyName = propertyDesc.getPropertyName();
        _valueType = valueType;
        _columnDbName = columnDbName;
        _columnSqlName = columnSqlName;
        _entityColumnInfo = entityColumnInfo;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return DfTypeUtil.toClassTitle(this) + ":{" + _propertyName + "(" + _columnDbName + "), "
                + DfTypeUtil.toClassTitle(_valueType) + ", " + _primaryKey + ", " + _persistent + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public DfPropertyAccessor getPropertyAccessor() {
        return _propertyDesc;
    }

    public DfPropertyDesc getPropertyDesc() {
        return _propertyDesc;
    }

    public ValueType getValueType() {
        return _valueType;
    }

    public String getPropertyName() {
        return _propertyName;
    }

    public String getColumnDbName() {
        return _columnDbName;
    }

    public ColumnSqlName getColumnSqlName() {
        return _columnSqlName;
    }

    public ColumnInfo getEntityColumnInfo() {
        return _entityColumnInfo;
    }

    public boolean isPrimaryKey() {
        return _primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this._primaryKey = primaryKey;
    }

    public boolean isPersistent() {
        return _persistent;
    }

    public void setPersistent(boolean persistent) {
        this._persistent = persistent;
    }
}