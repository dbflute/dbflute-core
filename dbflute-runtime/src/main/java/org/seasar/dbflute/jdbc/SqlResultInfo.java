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
package org.seasar.dbflute.jdbc;

import org.seasar.dbflute.bhv.core.BehaviorCommandMeta;

/**
 * The information of SQL result.
 * @author jflute
 */
public class SqlResultInfo {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final BehaviorCommandMeta _meta;
    protected final Object _result;
    protected final SqlLogInfo _sqlLogInfo;
    protected final ExecutionTimeInfo _executionTimeInfo;
    protected final RuntimeException _cause;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public SqlResultInfo(BehaviorCommandMeta meta, Object result, SqlLogInfo sqlLogInfo, ExecutionTimeInfo millisInfo,
            RuntimeException cause) {
        _meta = meta;
        _result = result;
        _sqlLogInfo = sqlLogInfo;
        _executionTimeInfo = millisInfo;
        _cause = cause;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append(", meta=").append(_meta.getTableDbName()).append(".").append(_meta.getCommandName());
        sb.append(", result=").append(_result != null ? _result.getClass().getName() : null);
        sb.append(", sqlLogInfo=").append(_sqlLogInfo);
        sb.append(", executionTimeInfo=").append(_executionTimeInfo);
        sb.append(", cause=").append(_cause != null ? _cause.getClass().getName() : null);
        sb.append("}");
        return sb.toString();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    /**
     * Get the meta information of the behavior command.
     * @return The meta information of the behavior command. (NotNull)
     */
    public BehaviorCommandMeta getMeta() {
        return _meta;
    }

    /**
     * Get the result of SQL execution (mapped to entity if select). <br />
     * @return The instance of result. (NullAllowed)
     */
    public Object getResult() {
        return _result;
    }

    /**
     * Get the information of SQL log info.
     * <pre>
     * [SqlLogInfo]
     * o executedSql : The actually-executed SQL, which JDBC can analyze.
     * o bindArgs : The argument values of bind variables.
     * o bindArgTypes : The argument types of bind variables.
     * o displaySql : The SQL string for display, bind variables are embedded.
     * </pre>
     * @return The information of SQL info. (NotNull) 
     */
    public SqlLogInfo getSqlLogInfo() {
        return _sqlLogInfo;
    }

    /**
     * Get the information of execution time info.
     * <pre>
     * [ExecutionTimeInfo]
     * o commandBeforeTimeMillis : The time as millisecond before command invoking (before building SQL clause).
     * o commandAfterTimeMillis : The time as millisecond after command invoking (after mapping to entity).
     * o sqlBeforeTimeMillis : The time as millisecond before SQL execution (after building SQL clause).
     * o sqlAfterTimeMillis : The time as millisecond after SQL execution (before mapping to entity).
     * </pre>
     * @return The information of execution time. (NotNull)
     */
    public ExecutionTimeInfo getExecutionTimeInfo() {
        return _executionTimeInfo;
    }

    /**
     * Get the cause of command failure.
     * @return The exception for runtime. (NullAllowed: if no failure, returns null)
     */
    public RuntimeException getCause() {
        return _cause;
    }
}
