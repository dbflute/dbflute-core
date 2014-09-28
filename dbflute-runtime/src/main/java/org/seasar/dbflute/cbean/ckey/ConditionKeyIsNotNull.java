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
package org.seasar.dbflute.cbean.ckey;

import java.util.List;

import org.seasar.dbflute.cbean.cipher.ColumnFunctionCipher;
import org.seasar.dbflute.cbean.coption.ConditionOption;
import org.seasar.dbflute.cbean.cvalue.ConditionValue;
import org.seasar.dbflute.cbean.sqlclause.query.QueryClause;
import org.seasar.dbflute.dbmeta.name.ColumnRealName;

/**
 * The condition-key of isNotNull.
 * @author jflute
 */
public class ConditionKeyIsNotNull extends ConditionKey {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Serial version UID. (Default) */
    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    protected ConditionKeyIsNotNull() {
        _conditionKey = "isNotNull";
        _operand = "is not null";
    }

    // ===================================================================================
    //                                                                       Prepare Query
    //                                                                       =============
    @Override
    protected boolean doPrepareQuery(ConditionValue cvalue, Object value, ColumnRealName callerName) {
        if (needsOverrideValue(cvalue)) {
            noticeRegistered(callerName, value);
            return false;
        }
        return true;
    }

    // ===================================================================================
    //                                                                      Override Check
    //                                                                      ==============
    @Override
    public boolean needsOverrideValue(ConditionValue cvalue) {
        return cvalue.isFixedQuery() && cvalue.hasIsNotNull();
    }

    // ===================================================================================
    //                                                                        Where Clause
    //                                                                        ============
    @Override
    protected void doAddWhereClause(List<QueryClause> conditionList, ColumnRealName columnRealName,
            ConditionValue value, ColumnFunctionCipher cipher, ConditionOption option) {
        conditionList.add(buildClauseWithoutValue(columnRealName));
    }

    // ===================================================================================
    //                                                                     Condition Value
    //                                                                     ===============
    @Override
    protected void doSetupConditionValue(ConditionValue cvalue, Object value, String location, ConditionOption option) {
        cvalue.setIsNotNull(DUMMY_OBJECT);
    }
}
