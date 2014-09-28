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
package org.seasar.dbflute.bhv.core;

import java.sql.SQLException;

import org.seasar.dbflute.jdbc.ExecutionTimeInfo;
import org.seasar.dbflute.jdbc.SqlLogInfo;

/**
 * The information of SQL fire result.
 * @author jflute
 */
public class SqlFireResultInfo {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Object _nativeResult;
    protected final SqlLogInfo _sqlLogInfo;
    protected final ExecutionTimeInfo _executionTimeInfo;
    protected final SQLException _nativeCause;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public SqlFireResultInfo(Object nativeResult, SqlLogInfo sqlLogInfo, ExecutionTimeInfo millisInfo,
            SQLException nativeCause) {
        _nativeResult = nativeResult;
        _sqlLogInfo = sqlLogInfo;
        _executionTimeInfo = millisInfo;
        _nativeCause = nativeCause;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("nativeResult=").append(_nativeResult != null ? _nativeResult.getClass().getName() : null);
        sb.append(", sqlLogInfo=").append(_sqlLogInfo);
        sb.append(", executionTimeInfo=").append(_executionTimeInfo);
        sb.append(", nativeCause=").append(_nativeCause != null ? _nativeCause.getClass().getName() : null);
        sb.append("}");
        return sb.toString();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    /**
     * Get the JDBC native result of SQL fire. <br />
     * @return The instance of result. (NullAllowed)
     */
    public Object getNativeResult() {
        return _nativeResult;
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
     * o sqlBeforeTimeMillis : The time as millisecond before SQL execution (after building SQL clause).
     * o sqlAfterTimeMillis : The time as millisecond after SQL execution (before mapping to entity).
     * </pre>
     * @return The information of execution time. (NotNull)
     */
    public ExecutionTimeInfo getExecutionTimeInfo() {
        return _executionTimeInfo;
    }

    /**
     * Get the native cause of SQL fire failure.
     * @return The native cause of SQL fire failure. (NullAllowed: if fire success, returns null)
     */
    public SQLException getNativeCause() {
        return _nativeCause;
    }
}
