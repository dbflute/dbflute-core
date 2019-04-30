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
package org.dbflute.cbean.ckey;

import java.util.List;

import org.dbflute.cbean.cipher.ColumnFunctionCipher;
import org.dbflute.cbean.coption.ConditionOption;
import org.dbflute.cbean.cvalue.ConditionValue;
import org.dbflute.cbean.sqlclause.query.QueryClause;
import org.dbflute.dbmeta.name.ColumnRealName;

/**
 * The condition-key of equal.
 * @author jflute
 */
public class ConditionKeyEqual extends ConditionKey {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The serial version UID for object serialization. (Default) */
    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    protected ConditionKeyEqual() {
        _conditionKey = "equal";
        _operand = "=";
    }

    // ===================================================================================
    //                                                                       Prepare Query
    //                                                                       =============
    @Override
    protected ConditionKeyPrepareResult doPrepareQuery(ConditionValue cvalue, Object value) {
        if (value == null) {
            return RESULT_INVALID_QUERY;
        }
        if (needsOverrideValue(cvalue)) {
            cvalue.overrideEqual(value);
            return chooseResultAlreadyExists(cvalue.equalEqual(value));
        }
        return RESULT_NEW_QUERY;
    }

    // ===================================================================================
    //                                                                      Override Check
    //                                                                      ==============
    @Override
    public boolean needsOverrideValue(ConditionValue cvalue) {
        return cvalue.isFixedQuery() && cvalue.hasEqual();
    }

    // ===================================================================================
    //                                                                        Where Clause
    //                                                                        ============
    @Override
    protected void doAddWhereClause(List<QueryClause> conditionList, ColumnRealName columnRealName, ConditionValue value,
            ColumnFunctionCipher cipher, ConditionOption option) {
        conditionList.add(buildBindClause(columnRealName, value.getEqualLatestLocation(), cipher, option));
    }

    @Override
    protected boolean isPossibleBindEncryptConditionKey() {
        return true;
    }

    // ===================================================================================
    //                                                                     Condition Value
    //                                                                     ===============
    @Override
    protected void doSetupConditionValue(ConditionValue cvalue, Object value, String location, ConditionOption option) {
        cvalue.setupEqual(value, location);
    }

    // ===================================================================================
    //                                                                       Null-able Key
    //                                                                       =============
    @Override
    public boolean isNullaleKey() {
        return false;
    }
}
