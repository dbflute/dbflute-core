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
package org.seasar.dbflute.bhv.proposal.callback;

import java.lang.reflect.Method;

import org.seasar.dbflute.bhv.SqlStringFilter;
import org.seasar.dbflute.bhv.core.BehaviorCommandMeta;

/**
 * @author jflute
 * @since 1.0.4D (2013/06/16 Sunday)
 */
public class QueryTraceableSqlStringFilter implements SqlStringFilter {

    protected final SimpleTraceableSqlStringFilter _filter;

    public QueryTraceableSqlStringFilter(Method actionMethod, TraceableSqlAdditionalInfoProvider additionalInfoProvider) {
        _filter = new SimpleTraceableSqlStringFilter(actionMethod, additionalInfoProvider);
    }

    public String filterSelectCB(BehaviorCommandMeta meta, String executedSql) {
        return _filter.filterSelectCB(meta, executedSql);
    }

    public String filterEntityUpdate(BehaviorCommandMeta meta, String executedSql) {
        return null;
    }

    public String filterQueryUpdate(BehaviorCommandMeta meta, String executedSql) {
        return _filter.filterQueryUpdate(meta, executedSql);
    }

    public String filterOutsideSql(BehaviorCommandMeta meta, String executedSql) {
        return null;
    }

    public String filterProcedure(BehaviorCommandMeta meta, String executedSql) {
        return null;
    }

    public QueryTraceableSqlStringFilter markingAtFront() {
        _filter.markingAtFront();
        return this;
    }
}
