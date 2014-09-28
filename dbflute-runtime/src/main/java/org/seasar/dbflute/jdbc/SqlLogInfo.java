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
 * The information of SQL log.
 * @author jflute
 */
public class SqlLogInfo {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final BehaviorCommandMeta _meta;
    protected final String _executedSql;
    protected final Object[] _bindArgs;
    protected final Class<?>[] _bindArgTypes;
    protected final SqlLogDisplaySqlBuilder _displaySqlBuilder;
    protected String _cachedDisplaySql;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public SqlLogInfo(BehaviorCommandMeta meta, String executedSql, Object[] bindArgs, Class<?>[] bindArgTypes,
            SqlLogDisplaySqlBuilder displaySqlBuilder) {
        _meta = meta;
        _executedSql = executedSql;
        _bindArgs = bindArgs;
        _bindArgTypes = bindArgTypes;
        _displaySqlBuilder = displaySqlBuilder;
    }

    public static interface SqlLogDisplaySqlBuilder {
        String build(String executedSql, Object[] bindArgs, Class<?>[] bindArgTypes);
    }

    // ===================================================================================
    //                                                                          DisplaySql
    //                                                                          ==========
    /**
     * Get the SQL string for display, bind variables are embedded. <br />
     * Basically the string is built lazily, but no guarantee. <br />
     * If the command is for batch, it returns SQLs for a part of entities.
     * @return The string of SQL. (basically NotNull: if no SQL execution, returns null)
     */
    public String getDisplaySql() {
        if (_cachedDisplaySql != null) {
            return _cachedDisplaySql;
        }
        _cachedDisplaySql = _displaySqlBuilder.build(_executedSql, _bindArgs, _bindArgTypes);
        return _cachedDisplaySql;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("executedSql.length()=").append(_executedSql != null ? _executedSql.length() : null);
        sb.append(", bindArgs.length=").append(_bindArgs != null ? _bindArgs.length : null);
        sb.append(", bindArgTypes.length=").append(_bindArgTypes != null ? _bindArgTypes.length : null);
        sb.append(", cachedDisplaySql.length()=").append(_cachedDisplaySql != null ? _cachedDisplaySql.length() : null);
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
     * Get the actually-executed SQL, which JDBC can analyze.
     * @return The string of SQL. (basically NotNull: if no SQL execution, returns null)
     */
    public String getExecutedSql() {
        return _executedSql;
    }

    /**
     * Get the argument values of bind variables.
     * @return The array of value. (NotNull)
     */
    public Object[] getBindArgs() {
        return _bindArgs;
    }

    /**
     * Get the argument types of bind variables.
     * @return The array of value. (NotNull)
     */
    public Class<?>[] getBindArgTypes() {
        return _bindArgTypes;
    }
}
