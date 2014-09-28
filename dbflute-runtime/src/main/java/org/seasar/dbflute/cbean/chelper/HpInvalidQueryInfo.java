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
package org.seasar.dbflute.cbean.chelper;

import org.seasar.dbflute.cbean.ckey.ConditionKey;
import org.seasar.dbflute.dbmeta.info.ColumnInfo;
import org.seasar.dbflute.util.DfTypeUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public class HpInvalidQueryInfo {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _locationBase;
    protected final ColumnInfo _targetColumn;
    protected final ConditionKey _conditionKey;
    protected final Object _invalidValue;
    protected boolean _inlineView;
    protected boolean _onClause;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public HpInvalidQueryInfo(String locationBase, ColumnInfo targetColumn, ConditionKey conditionKey,
            Object invalidValue) {
        assertObjectNotNull("locationBase", locationBase);
        assertObjectNotNull("targetColumn", targetColumn);
        assertObjectNotNull("conditionKey", conditionKey);
        _locationBase = locationBase;
        _targetColumn = targetColumn;
        _conditionKey = conditionKey;
        _invalidValue = invalidValue;
    }

    public HpInvalidQueryInfo inlineView() {
        _inlineView = true;
        return this;
    }

    public HpInvalidQueryInfo onClause() {
        _onClause = true;
        return this;
    }

    protected void assertObjectNotNull(String variableName, Object value) {
        if (variableName == null) {
            String msg = "The value should not be null: variableName=null value=" + value;
            throw new IllegalArgumentException(msg);
        }
        if (value == null) {
            String msg = "The value should not be null: variableName=" + variableName;
            throw new IllegalArgumentException(msg);
        }
    }

    // ===================================================================================
    //                                                                             Display
    //                                                                             =======
    public String buildDisplay() {
        final StringBuilder sb = new StringBuilder();
        final String tableDbName = _targetColumn.getDBMeta().getTableDbName();
        final String columnDbName = _targetColumn.getColumnDbName();
        sb.append(tableDbName).append(".").append(columnDbName);
        sb.append(" ").append(_conditionKey.getConditionKey());
        sb.append(" {value=").append(_invalidValue).append("}");
        sb.append(" : ").append(buildLocationDisp());
        if (_inlineView) {
            sb.append("(").append("inlineView").append(")");
        } else if (_onClause) {
            sb.append("(").append("onClause").append(")");
        }
        return sb.toString();
    }

    protected String buildLocationDisp() {
        // you should throw an exception if specification of locationBase changes
        String locationExp = Srl.replace(_locationBase, ".", "().");
        locationExp = Srl.replace(locationExp, "conditionQuery()", "query()");
        locationExp = Srl.replace(locationExp, ".conditionQuery", ".query");
        locationExp = Srl.rtrim(locationExp, ".");
        return locationExp;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return DfTypeUtil.toClassTitle(this) + ":{" + buildDisplay() + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getLocationBase() {
        return _locationBase;
    }

    public ColumnInfo getTargetColumn() {
        return _targetColumn;
    }

    public ConditionKey getConditionKey() {
        return _conditionKey;
    }

    public Object getInvalidValue() {
        return _invalidValue;
    }

    public boolean isInlineView() {
        return _inlineView;
    }

    public boolean isOnClause() {
        return _onClause;
    }
}
