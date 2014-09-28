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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.seasar.dbflute.cbean.ckey.ConditionKey;

/**
 * @author jflute
 */
public class HpMobCaseWhenElement {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final ConditionKey _conditionKey;
    protected final Object _orderValue;
    protected List<HpMobCaseWhenElement> _connectedElementList; // top element only
    protected HpMobConnectionMode _connectionMode; // connected elements only
    protected Object _thenValue; // basically for switch order

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public HpMobCaseWhenElement(ConditionKey conditionKey, Object orderValue) {
        _conditionKey = conditionKey;
        _orderValue = orderValue;
    }

    // ===================================================================================
    //                                                                     Connection Mode
    //                                                                     ===============
    public void toBeConnectionModeAsAnd() {
        _connectionMode = HpMobConnectionMode.AND;
    }

    public void toBeConnectionModeAsOr() {
        _connectionMode = HpMobConnectionMode.OR;
    }

    public String toConnector() {
        return _connectionMode != null ? _connectionMode.toString() : null;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return "{" + _conditionKey + ", " + _orderValue + ", " + _connectedElementList + ", " + _connectionMode + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public ConditionKey getConditionKey() {
        return _conditionKey;
    }

    public Object getOrderValue() {
        return _orderValue;
    }

    @SuppressWarnings("unchecked")
    public List<HpMobCaseWhenElement> getConnectedElementList() {
        return _connectedElementList != null ? _connectedElementList : Collections.EMPTY_LIST;
    }

    public void addConnectedElement(HpMobCaseWhenElement connectedElement) {
        if (_connectedElementList == null) {
            _connectedElementList = new ArrayList<HpMobCaseWhenElement>();
        }
        _connectedElementList.add(connectedElement);
    }

    public HpMobConnectionMode getConnectionMode() {
        return _connectionMode;
    }

    public void setConnectionMode(HpMobConnectionMode connectionMode) {
        _connectionMode = connectionMode;
    }

    public Object getThenValue() {
        return _thenValue;
    }

    public void setThenValue(Object thenValue) {
        this._thenValue = thenValue;
    }
}
