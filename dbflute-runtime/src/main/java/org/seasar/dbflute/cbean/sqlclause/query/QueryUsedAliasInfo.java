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
package org.seasar.dbflute.cbean.sqlclause.query;

import org.seasar.dbflute.cbean.sqlclause.join.InnerJoinNoWaySpeaker;

/**
 * @author jflute
 * @since 0.9.9.0A (2011/07/27 Wednesday)
 */
public class QueryUsedAliasInfo {

    protected final String _usedAliasName; // not null
    protected final InnerJoinNoWaySpeaker _innerJoinNoWaySpeaker; // null allowed

    /**
     * @param usedAliasName The alias name of joined table (or local) where it is used in query. (NotNull)
     * @param innerJoinNoWaySpeaker The no-way speaker for auto-detect of inner-join. (NullAllowed: null means inner-allowed)
     */
    public QueryUsedAliasInfo(String usedAliasName, InnerJoinNoWaySpeaker innerJoinNoWaySpeaker) {
        _usedAliasName = usedAliasName;
        _innerJoinNoWaySpeaker = innerJoinNoWaySpeaker;
    }

    public String getUsedAliasName() {
        return _usedAliasName;
    }

    public InnerJoinNoWaySpeaker getInnerJoinAutoDetectNoWaySpeaker() {
        return _innerJoinNoWaySpeaker;
    }
}
