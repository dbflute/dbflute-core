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

import org.seasar.dbflute.cbean.cipher.ColumnFunctionCipher;
import org.seasar.dbflute.cbean.coption.ConditionOption;
import org.seasar.dbflute.cbean.sqlclause.query.QueryClause;
import org.seasar.dbflute.dbmeta.name.ColumnRealName;

/**
 * The condition-key of greaterEqual or isNull. <br />
 * This key is greaterEqual's alternate so you cannot independent use.
 * @author jflute
 */
public class ConditionKeyGreaterEqualOrIsNull extends ConditionKeyGreaterEqual {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Serial version UID. (Default) */
    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    @Override
    protected void initializeConditionKey() {
        _conditionKey = "greaterEqualOrIsNull";
    }

    // ===================================================================================
    //                                                                      Implementation
    //                                                                      ==============
    @Override
    protected QueryClause buildBindClause(ColumnRealName columnRealName, String location, ColumnFunctionCipher cipher,
            ConditionOption option) {
        return buildBindClauseOrIsNull(columnRealName, location, cipher, option);
    }
}
