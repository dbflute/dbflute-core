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

/**
 * The handler of SQL result. <br />
 * This handler is called back after executing the SQL and mapping entities. <br />
 * (before you get the result)
 * <pre>
 * context.setSqlResultHandler(new SqlResultHandler() {
 *     public void handle(SqlResultInfo info) {
 *         // You can get your SQL result information here.
 *     }
 * });
 * </pre>
 * @author jflute
 */
public interface SqlResultHandler {

    /**
     * Handle the SQL result. <br />
     * This is called back per command execution.
     * <pre>
     * [SqlResultInfo]
     * o result : The result (mapped object) of executed SQL. (NullAllowed)
     * o sqlLogInfo : The information of SQL log, which has executedSql, arguments, displaySql... (NotNull)
     * o executionTimeInfo : The information of execution time. (NotNull)
     * o cause : The failure cause when the command failed. (NullAllowed)
     * </pre>
     * @param info The information of executed SQL result. (NotNull)
     */
    void handle(SqlResultInfo info);
}
