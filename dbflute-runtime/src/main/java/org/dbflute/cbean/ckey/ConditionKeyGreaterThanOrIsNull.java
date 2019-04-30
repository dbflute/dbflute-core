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

import org.dbflute.cbean.cipher.ColumnFunctionCipher;
import org.dbflute.cbean.coption.ConditionOption;
import org.dbflute.cbean.sqlclause.query.QueryClause;
import org.dbflute.dbmeta.name.ColumnRealName;

/**
 * The condition-key of greaterThan or isNull. <br>
 * This key is greaterThan's alternate so you cannot independent use.
 * @author jflute
 */
public class ConditionKeyGreaterThanOrIsNull extends ConditionKeyGreaterThan {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The serial version UID for object serialization. (Default) */
    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    @Override
    protected void initializeConditionKey() {
        _conditionKey = "greaterThanOrIsNull";
    }

    // ===================================================================================
    //                                                                      Implementation
    //                                                                      ==============
    @Override
    protected QueryClause buildBindClause(ColumnRealName columnRealName, String location, ColumnFunctionCipher cipher,
            ConditionOption option) {
        return buildBindClauseOrIsNull(columnRealName, location, cipher, option);
    }

    // ===================================================================================
    //                                                                       Null-able Key
    //                                                                       =============
    @Override
    public boolean isNullaleKey() {
        return true;
    }
}
