/*
 * Copyright 2014-2017 the original author or authors.
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
package org.dbflute.logic.doc.policycheck;

/**
 * @author jflute
 * @since 1.1.2 (2016/12/31 Saturday)
 */
public class DfSchemaPolicyIfClause {

    protected final String _statement;
    protected final String _ifItem;
    protected final String _ifValue;
    protected final boolean _notIfValue;
    protected final String _thenClause;
    protected final boolean _notThenClause;
    protected final String _thenItem; // null allowed
    protected final String _thenValue; // null allowed
    protected final boolean _notThenValue;
    protected final String _supplement; // null allowed

    public DfSchemaPolicyIfClause(String statement, String ifItem, String ifValue, boolean ifNotValue, String thenClause,
            boolean notThenClause, String thenItem, String thenValue, boolean thenNotValue, String supplement) {
        _statement = statement;
        _ifItem = ifItem;
        _ifValue = ifValue;
        _notIfValue = ifNotValue;
        _thenClause = thenClause;
        _notThenClause = notThenClause;
        _thenItem = thenItem;
        _thenValue = thenValue;
        _notThenValue = thenNotValue;
        _supplement = supplement;
    }

    public String getStatement() {
        return _statement;
    }

    public String getIfItem() {
        return _ifItem;
    }

    public String getIfValue() {
        return _ifValue;
    }

    public boolean isNotIfValue() {
        return _notIfValue;
    }

    public String getThenClause() {
        return _thenClause;
    }

    public boolean isNotThenClause() {
        return _notThenClause;
    }

    public String getThenItem() {
        return _thenItem;
    }

    public String getThenValue() {
        return _thenValue;
    }

    public boolean isNotThenValue() {
        return _notThenValue;
    }

    public String getSupplement() {
        return _supplement;
    }
}
