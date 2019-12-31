/*
 * Copyright 2014-2020 the original author or authors.
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
package org.dbflute.cbean.ckey;

import org.dbflute.cbean.coption.ConditionOption;
import org.dbflute.cbean.coption.LikeSearchOption;
import org.dbflute.cbean.cvalue.ConditionValue;

/**
 * The condition-key of notLikeSearch.
 * @author jflute
 */
public class ConditionKeyNotLikeSearch extends ConditionKeyLikeSearch {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The serial version UID for object serialization. (Default) */
    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    @Override
    protected String defineConditionKey() {
        return "notLikeSearch";
    }

    @Override
    protected String defineOperand() {
        return "not like";
    }

    // ===================================================================================
    //                                                                        Where Clause
    //                                                                        ============
    @Override
    protected String getLocation(ConditionValue value) {
        return value.getNotLikeSearchLatestLocation();
    }

    // ===================================================================================
    //                                                                         Bind Clause
    //                                                                         ===========
    @Override
    protected String extractExtOperand(ConditionOption option) {
        final String extOperand = super.extractExtOperand(option);
        return extOperand != null ? "not " + extOperand : null;
    }

    // ===================================================================================
    //                                                                     Condition Value
    //                                                                     ===============
    @Override
    protected void doSetupConditionValue(ConditionValue conditionValue, Object value, String location, ConditionOption option) {
        conditionValue.setupNotLikeSearch((String) value, (LikeSearchOption) option, location);
    }

    // ===================================================================================
    //                                                                       Null-able Key
    //                                                                       =============
    @Override
    public boolean isNullaleKey() {
        return false;
    }
}
