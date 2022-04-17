/*
 * Copyright 2014-2022 the original author or authors.
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

import org.dbflute.cbean.sqlclause.query.QueryClause;
import org.dbflute.cbean.sqlclause.query.StringQueryClause;
import org.dbflute.dbmeta.name.ColumnRealName;

/**
 * The condition-key of isNullOrEmpty.
 * @author jflute
 */
public class ConditionKeyIsNullOrEmpty extends ConditionKeyIsNull {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The serial version UID for object serialization. (Default) */
    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    protected ConditionKeyIsNullOrEmpty() {
    }

    @Override
    protected void initializeConditionKey() {
        _conditionKey = "isNullOrEmpty";
    }

    @Override
    protected void initializeOperand() {
        _operand = "is null"; // added or-part later
    }

    // ===================================================================================
    //                                                                      Implementation
    //                                                                      ==============
    @Override
    protected QueryClause buildClauseWithoutValue(ColumnRealName columnRealName) {
        final String clause = "(" + columnRealName + " " + getOperand() + " or " + columnRealName + " = '')";
        return new StringQueryClause(clause);
    }
}
