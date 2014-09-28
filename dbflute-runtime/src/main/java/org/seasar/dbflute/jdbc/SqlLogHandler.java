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
 * The handler of SQL log. <br />
 * This handler is called back before executing the SQL.
 * <pre>
 * context.setSqlLogHandler(new SqlLogHandler() {
 *     public void handle(SqlLogInfo info) {
 *         // You can get your SQL string here.
 *     }
 * });
 * </pre>
 * @author jflute
 */
public interface SqlLogHandler {

    /**
     * Handle the SQL log. <br />
     * This is called back per SQL logging.
     * But if the SQL would be not executed, this is not called back,
     * for example, update() that the entity has no modification.
     * And if the command is for batch, this is called back per batch elements in a command.
     * @param info The information of SQL log. (NotNull)
     */
    void handle(SqlLogInfo info);
}
