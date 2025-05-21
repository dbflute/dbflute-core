/*
 * Copyright 2014-2025 the original author or authors.
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

/**
 * @author jflute
 * @since 1.1.0 (2014/10/17 Friday)
 */
public enum ConditionKeyPrepareResult {

    NEW_QUERY(true, false, false, false) // newClause
    , INVALID_QUERY(false, true, false, false) // invalid
    , OVERRIDING_QUERY(false, false, true, false) // overridden
    , DUPLICATE_QUERY(false, false, true, true) // overridden, duplicate
    ;

    private final boolean _newClause;
    private final boolean _invalid;
    private final boolean _overridden;
    private final boolean _duplicate;

    private ConditionKeyPrepareResult(boolean newClause, boolean invalid, boolean overridden, boolean duplicate) {
        _newClause = newClause;
        _invalid = invalid;
        _overridden = overridden;
        _duplicate = duplicate;
    }

    public boolean newClause() {
        return _newClause;
    }

    public boolean invalid() {
        return _invalid;
    }

    public boolean overridden() {
        return _overridden;
    }

    public boolean duplicate() {
        return _duplicate;
    }
}
