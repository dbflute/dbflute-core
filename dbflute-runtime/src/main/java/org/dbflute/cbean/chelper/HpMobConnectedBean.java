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
package org.dbflute.cbean.chelper;

import org.dbflute.cbean.ckey.ConditionKey;
import org.dbflute.cbean.ordering.ManualOrderOption;

/**
 * @author jflute
 */
public class HpMobConnectedBean {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final ManualOrderOption _parentOption;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public HpMobConnectedBean(ManualOrderOption parentOption) {
        _parentOption = parentOption;
    }

    // ===================================================================================
    //                                                                      And Connection
    //                                                                      ==============
    public HpMobConnectedBean and_Equal(Object orderValue) {
        return doAnd(ConditionKey.CK_EQUAL, orderValue);
    }

    public HpMobConnectedBean and_NotEqual(Object orderValue) {
        return doAnd(ConditionKey.CK_NOT_EQUAL_STANDARD, orderValue);
    }

    public HpMobConnectedBean and_GreaterThan(Object orderValue) {
        return doAnd(ConditionKey.CK_GREATER_THAN, orderValue);
    }

    public HpMobConnectedBean and_LessThan(Object orderValue) {
        return doAnd(ConditionKey.CK_LESS_THAN, orderValue);
    }

    public HpMobConnectedBean and_GreaterEqual(Object orderValue) {
        return doAnd(ConditionKey.CK_GREATER_EQUAL, orderValue);
    }

    public HpMobConnectedBean and_LessEqual(Object orderValue) {
        return doAnd(ConditionKey.CK_LESS_EQUAL, orderValue);
    }

    public HpMobConnectedBean and_IsNull() {
        return doAnd(ConditionKey.CK_IS_NULL, null);
    }

    public HpMobConnectedBean and_IsNotNull() {
        return doAnd(ConditionKey.CK_IS_NOT_NULL, null);
    }

    public HpMobConnectedBean doAnd(ConditionKey conditionKey, Object orderValue) {
        toBeConnectionModeAsAnd();
        try {
            return delegate(conditionKey, orderValue);
        } finally {
            clearConnectionMode();
        }
    }

    // ===================================================================================
    //                                                                       Or Connection
    //                                                                       =============
    public HpMobConnectedBean or_Equal(Object orderValue) {
        return doOr(ConditionKey.CK_EQUAL, orderValue);
    }

    public HpMobConnectedBean or_NotEqual(Object orderValue) {
        return doOr(ConditionKey.CK_NOT_EQUAL_STANDARD, orderValue);
    }

    public HpMobConnectedBean or_GreaterThan(Object orderValue) {
        return doOr(ConditionKey.CK_GREATER_THAN, orderValue);
    }

    public HpMobConnectedBean or_LessThan(Object orderValue) {
        return doOr(ConditionKey.CK_LESS_THAN, orderValue);
    }

    public HpMobConnectedBean or_GreaterEqual(Object orderValue) {
        return doOr(ConditionKey.CK_GREATER_EQUAL, orderValue);
    }

    public HpMobConnectedBean or_LessEqual(Object orderValue) {
        return doOr(ConditionKey.CK_LESS_EQUAL, orderValue);
    }

    public HpMobConnectedBean or_IsNull() {
        return doOr(ConditionKey.CK_IS_NULL, null);
    }

    public HpMobConnectedBean or_IsNotNull() {
        return doOr(ConditionKey.CK_IS_NOT_NULL, null);
    }

    public HpMobConnectedBean doOr(ConditionKey conditionKey, Object orderValue) {
        toBeConnectionModeAsOr();
        try {
            return delegate(conditionKey, orderValue);
        } finally {
            clearConnectionMode();
        }
    }

    protected HpMobConnectedBean delegate(ConditionKey conditionKey, Object orderValue) {
        if (ConditionKey.CK_EQUAL.equals(conditionKey)) {
            _parentOption.when_Equal(orderValue);
        } else if (ConditionKey.CK_NOT_EQUAL_STANDARD.equals(conditionKey)) {
            _parentOption.when_NotEqual(orderValue);
        } else if (ConditionKey.CK_GREATER_THAN.equals(conditionKey)) {
            _parentOption.when_GreaterThan(orderValue);
        } else if (ConditionKey.CK_LESS_THAN.equals(conditionKey)) {
            _parentOption.when_LessThan(orderValue);
        } else if (ConditionKey.CK_GREATER_EQUAL.equals(conditionKey)) {
            _parentOption.when_GreaterEqual(orderValue);
        } else if (ConditionKey.CK_LESS_EQUAL.equals(conditionKey)) {
            _parentOption.when_LessEqual(orderValue);
        } else if (ConditionKey.CK_IS_NULL.equals(conditionKey)) {
            _parentOption.when_IsNull();
        } else if (ConditionKey.CK_IS_NOT_NULL.equals(conditionKey)) {
            _parentOption.when_IsNotNull();
        } else {
            String msg = "Unknown conditionKey: " + conditionKey;
            throw new IllegalStateException(msg);
        }
        return this;
    }

    // ===================================================================================
    //                                                                     Connection Mode
    //                                                                     ===============
    protected void toBeConnectionModeAsAnd() {
        _parentOption.toBeConnectionModeAsAnd();
    }

    protected void toBeConnectionModeAsOr() {
        _parentOption.toBeConnectionModeAsOr();
    }

    protected void clearConnectionMode() {
        _parentOption.clearConnectionMode();
    }

    // ===================================================================================
    //                                                                          Then Value
    //                                                                          ==========
    /**
     * Add 'then' value to the last case-when element. (Basically for SwitchOrder) <br>
     * You should also set 'then' values to other elements and set 'else' value.
     * @param thenValue The value for 'then', String, Integer, Date, DreamCruiseTicket... (NotNull)
     */
    public void then(Object thenValue) {
        if (thenValue == null) {
            String msg = "The argument 'thenValue' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        _parentOption.xregisterThenValueToLastElement(thenValue);
    }
}
