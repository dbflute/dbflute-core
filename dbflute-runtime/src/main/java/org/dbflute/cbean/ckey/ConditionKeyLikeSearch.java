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
import org.dbflute.cbean.coption.LikeSearchOption;
import org.dbflute.cbean.cvalue.ConditionValue;
import org.dbflute.cbean.sqlclause.query.QueryClause;
import org.dbflute.dbmeta.name.ColumnRealName;

/**
 * The condition-key of likeSearch.
 * @author jflute
 */
public class ConditionKeyLikeSearch extends ConditionKey {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The serial version UID for object serialization. (Default) */
    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    protected ConditionKeyLikeSearch() {
        _conditionKey = defineConditionKey();
        _operand = defineOperand();
    }

    protected String defineConditionKey() {
        return "likeSearch";
    }

    protected String defineOperand() {
        return "like";
    }

    // ===================================================================================
    //                                                                       Prepare Query
    //                                                                       =============
    @Override
    protected ConditionKeyPrepareResult doPrepareQuery(ConditionValue cvalue, Object value) {
        return chooseResultNonFixedQuery(value);
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
        assertLikeSearchOption(columnRealName, value, option);
        conditionList.add(buildBindClause(columnRealName, getLocation(value), cipher, option));
    }

    protected void assertLikeSearchOption(ColumnRealName columnRealName, ConditionValue value, ConditionOption option) {
        if (option == null) {
            String msg = "The argument 'option' should not be null:";
            msg = msg + " columnName=" + columnRealName + " value=" + value;
            throw new IllegalArgumentException(msg);
        }
        if (!(option instanceof LikeSearchOption)) {
            String msg = "The argument 'option' should be LikeSearchOption:";
            msg = msg + " columnName=" + columnRealName + " value=" + value;
            msg = msg + " option=" + option;
            throw new IllegalArgumentException(msg);
        }
    }

    protected String getLocation(ConditionValue value) { // to override
        return value.getLikeSearchLatestLocation();
    }

    // ===================================================================================
    //                                                                         Bind Clause
    //                                                                         ===========
    @Override
    protected boolean isOutOfBindEncryptConditionKey() { // to override
        return true; // because wild cards are embedded in condition value for likeSearch
    }

    // ===================================================================================
    //                                                                     Condition Value
    //                                                                     ===============
    @Override
    protected void doSetupConditionValue(ConditionValue cvalue, Object value, String location, ConditionOption option) {
        cvalue.setupLikeSearch((String) value, (LikeSearchOption) option, location);
    }

    // ===================================================================================
    //                                                                       Null-able Key
    //                                                                       =============
    @Override
    public boolean isNullaleKey() {
        return false;
    }
}
