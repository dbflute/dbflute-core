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

import org.seasar.dbflute.jdbc.SqlLogInfo;

/**
 * The information of SQL fire ready.
 * @author jflute
 */
public class SqlFireReadyInfo {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final SqlLogInfo _sqlLogInfo;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public SqlFireReadyInfo(SqlLogInfo sqlLogInfo) {
        _sqlLogInfo = sqlLogInfo;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append(", sqlLogInfo=").append(_sqlLogInfo);
        sb.append("}");
        return sb.toString();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    /**
     * Get the information of SQL log info.
     * <pre>
     * [SqlLogInfo]
     * o executedSql : The actually-executed SQL, which JDBC can analyze. (basically NotNull: if no SQL execution, null)
     * o bindArgs : The argument values of bind variables. (NotNull, EmptyAllowed)
     * o bindArgTypes : The argument types of bind variables. (NotNull, EmptyAllowed)
     * o displaySql : The SQL string for display, bind variables are embedded. (basically NotNull: if no SQL execution, null)
     * </pre>
     * @return The information of SQL info. (NotNull) 
     */
    public SqlLogInfo getSqlLogInfo() {
        return _sqlLogInfo;
    }
}
