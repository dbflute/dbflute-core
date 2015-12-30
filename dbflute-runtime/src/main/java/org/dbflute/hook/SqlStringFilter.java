/*
 * Copyright 2014-2015 the original author or authors.
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
package org.dbflute.hook;

import org.dbflute.bhv.core.BehaviorCommandMeta;

/**
 * The filter of SQL string. <br>
 * This filter is called back before executing the SQL.
 * <pre>
 * context.setSqlStringFilter(new SqlStringFilter() {
 *     public String filterSelectCB(BehaviorCommandMeta meta, String executedSql) {
 *         // You can filter your SQL string here.
 *     }
 *     ...
 * });
 * </pre>
 * <p>This filter does not have all SQL call-back point.
 * For example, you cannot filter SQL of sequence next value.</p>
 * @author jflute
 */
public interface SqlStringFilter {

    /**
     * Filter the executed SQL of select by condition-bean.
     * @param meta The meta information of the behavior command. (NotNull)
     * @param executedSql The string of actually-executed SQL. (NotNull)
     * @return The filtered SQL string. (NullAllowed: if null, means no filter)
     */
    default String filterSelectCB(BehaviorCommandMeta meta, String executedSql) {
        return null;
    }

    /**
     * Filter the executed SQL of entity update, insert and delete. (contains batch)
     * @param meta The meta information of the behavior command. (NotNull)
     * @param executedSql The string of actually-executed SQL. (NotNull)
     * @return The filtered SQL string. (NullAllowed: if null, means no filter)
     */
    default String filterEntityUpdate(BehaviorCommandMeta meta, String executedSql) {
        return null;
    }

    /**
     * Filter the executed SQL of query update, insert and delete (by condition-bean).
     * @param meta The meta information of the behavior command. (NotNull)
     * @param executedSql The string of actually-executed SQL. (NotNull)
     * @return The filtered SQL string. (NullAllowed: if null, means no filter)
     */
    default String filterQueryUpdate(BehaviorCommandMeta meta, String executedSql) {
        return null;
    }

    /**
     * Filter the executed SQL of outside-SQL. <br>
     * Called after filtering by outside-SQL filter.
     * @param meta The meta information of the behavior command. (NotNull)
     * @param executedSql The string of actually-executed SQL. (NotNull)
     * @return The filtered SQL string. (NullAllowed: if null, means no filter)
     */
    default String filterOutsideSql(BehaviorCommandMeta meta, String executedSql) {
        return null;
    }

    /**
     * Filter the executed SQL of procedure call.
     * @param meta The meta information of the behavior command. (NotNull)
     * @param executedSql The string of actually-executed SQL. (NotNull)
     * @return The filtered SQL string. (NullAllowed: if null, means no filter)
     */
    default String filterProcedure(BehaviorCommandMeta meta, String executedSql) {
        return null;
    }

    /**
     * Does it inherit the existing filter? <br>
     * Completely overriding as default but you can inherit it by this determination.
     * @return The determination, true or false.
     */
    default boolean inheritsExistingFilter() {
        return true; // inherits as default since 1.1.1
    }
}
