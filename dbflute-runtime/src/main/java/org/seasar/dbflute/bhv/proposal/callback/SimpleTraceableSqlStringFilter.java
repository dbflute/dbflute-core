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
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.0.4D (2013/06/16 Sunday)
 */
public class SimpleTraceableSqlStringFilter implements SqlStringFilter, ExecutedSqlCounter {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Method _actionMethod;
    protected final TraceableSqlAdditionalInfoProvider _additionalInfoProvider;
    protected boolean _markingAtFront;
    protected boolean _suppressMarking;
    protected int _countOfSelectCB;
    protected int _countOfEntityUpdate;
    protected int _countOfQueryUpdate;
    protected int _countOfOutsideSql;
    protected int _countOfProcedure;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public SimpleTraceableSqlStringFilter(Method actionMethod, TraceableSqlAdditionalInfoProvider additionalInfoProvider) {
        _actionMethod = actionMethod;
        _additionalInfoProvider = additionalInfoProvider;
    }

    // ===================================================================================
    //                                                                              Filter
    //                                                                              ======
    public String filterSelectCB(BehaviorCommandMeta meta, String executedSql) {
        ++_countOfSelectCB;
        return markingSql(executedSql);
    }

    public String filterEntityUpdate(BehaviorCommandMeta meta, String executedSql) {
        ++_countOfEntityUpdate;
        return markingSql(executedSql);
    }

    public String filterQueryUpdate(BehaviorCommandMeta meta, String executedSql) {
        ++_countOfQueryUpdate;
        return markingSql(executedSql);
    }

    public String filterOutsideSql(BehaviorCommandMeta meta, String executedSql) {
        ++_countOfOutsideSql;
        // outside-SQL is easy to find caller by SQL
        // and it might have unexpected SQL so no marking
        //return markingSql(executedSql);
        return null;
    }

    public String filterProcedure(BehaviorCommandMeta meta, String executedSql) {
        ++_countOfProcedure;
        // procedure call uses JDBC's escape "{" and "}"
        // so it might fail to execute the SQL (actually PostgreSQL)
        //return markingSql(executedSql);
        return null;
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected String markingSql(String executedSql) {
        if (_suppressMarking) {
            return null;
        }
        final String filtered;
        if (_markingAtFront) {
            filtered = "-- " + buildInvokeMark() + "\n" + executedSql;
        } else { // default here
            filtered = executedSql + "\n-- " + buildInvokeMark();
        }
        return filtered;
    }

    protected String buildInvokeMark() {
        final StringBuilder sb = new StringBuilder();
        sb.append(_actionMethod.getDeclaringClass().getName());
        sb.append("#").append(_actionMethod.getName()).append("()");
        if (_additionalInfoProvider != null) {
            final String addiitonalInfo = _additionalInfoProvider.provide();
            if (addiitonalInfo != null) {
                sb.append(": ").append(resolveUnsupportedMark(addiitonalInfo));
            }
        }
        return sb.toString();
    }

    protected String resolveUnsupportedMark(String info) {
        String resolved = Srl.replace(info, "?", "Q"); // ? is binding mark
        resolved = Srl.replace(resolved, "{", "("); // {} is NG mark on Oracle
        resolved = Srl.replace(resolved, "}", ")");
        resolved = Srl.replace(resolved, "'", "\""); // ' is NG mark when update on Oracle
        return resolved;
    }

    // ===================================================================================
    //                                                                              Option
    //                                                                              ======
    public SimpleTraceableSqlStringFilter markingAtFront() {
        _markingAtFront = true;
        return this;
    }

    public SimpleTraceableSqlStringFilter suppressMarking() {
        _suppressMarking = true;
        return this;
    }

    // ===================================================================================
    //                                                                         SQL Counter
    //                                                                         ===========
    public int getTotalCountOfSql() {
        return _countOfSelectCB + _countOfEntityUpdate + _countOfQueryUpdate + _countOfOutsideSql + _countOfProcedure;
    }

    public int getCountOfSelectCB() {
        return _countOfSelectCB;
    }

    public int getCountOfEntityUpdate() {
        return _countOfEntityUpdate;
    }

    public int getCountOfQueryUpdate() {
        return _countOfQueryUpdate;
    }

    public int getCountOfOutsideSql() {
        return _countOfOutsideSql;
    }

    public int getCountOfProcedure() {
        return _countOfProcedure;
    }

    public String toLineDisp() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{total=").append(getTotalCountOfSql());
        sb.append(", selectCB=").append(getCountOfSelectCB());
        sb.append(", entityUpdate=").append(getCountOfEntityUpdate());
        sb.append(", queryUpdate=").append(getCountOfQueryUpdate());
        sb.append(", outsideSql=").append(getCountOfOutsideSql());
        sb.append(", procedure=").append(getCountOfProcedure());
        sb.append("}");
        return sb.toString();
    }
}
