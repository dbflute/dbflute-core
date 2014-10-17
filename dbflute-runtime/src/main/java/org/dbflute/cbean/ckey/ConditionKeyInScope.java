/*
 * Copyright 2014-2014 the original author or authors.
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
 * The condition-key of inScope.
 * @author jflute
 */
public class ConditionKeyInScope extends ConditionKey {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The serial version UID for object serialization. (Default) */
    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    protected ConditionKeyInScope() {
        _conditionKey = "inScope";
        _operand = "in";
    }

    // ===================================================================================
    //                                                                       Prepare Query
    //                                                                       =============
    @Override
    protected ConditionKeyPrepareResult doPrepareQuery(ConditionValue cvalue, Object value) {
        return chooseResultListQuery(value);
    }

    // ===================================================================================
    //                                                                      Override Check
    //                                                                      ==============
    @Override
    public boolean needsOverrideValue(ConditionValue cvalue) {
        return false; // for not fixed query
    }

    // ===================================================================================
    //                                                                        Where Clause
    //                                                                        ============
    @Override
    protected void doAddWhereClause(List<QueryClause> conditionList, ColumnRealName columnRealName, ConditionValue value,
            ColumnFunctionCipher cipher, ConditionOption option) {
        conditionList.add(buildBindClause(columnRealName, value.getInScopeLatestLocation(), cipher, option));
    }

    // ===================================================================================
    //                                                                         Bind Clause
    //                                                                         ===========
    @Override
    protected String getBindVariableDummyValue() {
        return "('a1', 'a2')"; // to indicate inScope
    }

    // ===================================================================================
    //                                                                     Condition Value
    //                                                                     ===============
    @Override
    protected void doSetupConditionValue(ConditionValue cvalue, Object value, String location, ConditionOption option) {
        cvalue.setupInScope(value, location);
    }
}
