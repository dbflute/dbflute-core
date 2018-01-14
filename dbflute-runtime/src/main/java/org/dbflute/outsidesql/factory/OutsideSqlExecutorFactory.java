/*
 * Copyright 2014-2018 the original author or authors.
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
package org.dbflute.outsidesql.factory;

import org.dbflute.bhv.core.BehaviorCommandInvoker;
import org.dbflute.dbway.DBDef;
import org.dbflute.outsidesql.OutsideSqlOption;
import org.dbflute.outsidesql.executor.OutsideSqlAllFacadeExecutor;
import org.dbflute.outsidesql.executor.OutsideSqlAutoPagingExecutor;
import org.dbflute.outsidesql.executor.OutsideSqlBasicExecutor;
import org.dbflute.outsidesql.executor.OutsideSqlCursorExecutor;
import org.dbflute.outsidesql.executor.OutsideSqlEntityExecutor;
import org.dbflute.outsidesql.executor.OutsideSqlManualPagingExecutor;

/**
 * @author jflute
 */
public interface OutsideSqlExecutorFactory {

    /**
     * Create the all facade of outside-SQL.
     * @param <BEHAVIOR> The type of behavior.
     * @param basicExecutor The basic executor of outside-SQL. (NotNull)
     * @return The new-created instance of facade. (NotNull)
     */
    <BEHAVIOR> OutsideSqlAllFacadeExecutor<BEHAVIOR> createAllFacade(OutsideSqlBasicExecutor<BEHAVIOR> basicExecutor);

    /**
     * Create the basic executor of outside-SQL.
     * @param <BEHAVIOR> The type of behavior.
     * @param behaviorCommandInvoker The invoker of behavior command. (NotNull)
     * @param tableDbName The DB name of table. (NotNull)
     * @param currentDBDef The definition of current DBMS. (NotNull)
     * @param outsideSqlOption The option of outsideSql. (NullAllowed: if null, means for an entry instance)
     * @return The new-created instance of executor. (NotNull)
     */
    <BEHAVIOR> OutsideSqlBasicExecutor<BEHAVIOR> createBasic(BehaviorCommandInvoker behaviorCommandInvoker, String tableDbName,
            DBDef currentDBDef, OutsideSqlOption outsideSqlOption);

    /**
     * Create the cursor executor of outside-SQL.
     * @param <BEHAVIOR> The type of behavior.
     * @param behaviorCommandInvoker The invoker of behavior command. (NotNull)
     * @param tableDbName The DB name of table. (NotNull)
     * @param currentDBDef The definition of current DBMS. (NotNull)
     * @param outsideSqlOption The option of outsideSql. (NotNull)
     * @return The new-created instance of executor. (NotNull)
     */
    <BEHAVIOR> OutsideSqlCursorExecutor<BEHAVIOR> createCursor(BehaviorCommandInvoker behaviorCommandInvoker, String tableDbName,
            DBDef currentDBDef, OutsideSqlOption outsideSqlOption);

    /**
     * Create the entity executor of outside-SQL.
     * @param <BEHAVIOR> The type of behavior.
     * @param behaviorCommandInvoker The invoker of behavior command. (NotNull)
     * @param tableDbName The DB name of table. (NotNull)
     * @param currentDBDef The definition of DBMS. (NotNull)
     * @param outsideSqlOption The option of outsideSql. (NotNull)
     * @return The new-created instance of executor. (NotNull)
     */
    <BEHAVIOR> OutsideSqlEntityExecutor<BEHAVIOR> createEntity(BehaviorCommandInvoker behaviorCommandInvoker, String tableDbName,
            DBDef currentDBDef, OutsideSqlOption outsideSqlOption);

    /**
     * Create the manual-paging executor of outside-SQL.
     * @param <BEHAVIOR> The type of behavior.
     * @param behaviorCommandInvoker The invoker of behavior command. (NotNull)
     * @param tableDbName The DB name of table. (NotNull)
     * @param currentDBDef The definition of current DBMS. (NotNull)
     * @param outsideSqlOption The option of outsideSql. (NotNull)
     * @return The new-created instance of executor. (NotNull)
     */
    <BEHAVIOR> OutsideSqlManualPagingExecutor<BEHAVIOR> createManualPaging(BehaviorCommandInvoker behaviorCommandInvoker,
            String tableDbName, DBDef currentDBDef, OutsideSqlOption outsideSqlOption);

    /**
     * Create the auto-paging executor of outside-SQL.
     * @param <BEHAVIOR> The type of behavior.
     * @param behaviorCommandInvoker The invoker of behavior command. (NotNull)
     * @param tableDbName The DB name of table. (NotNull)
     * @param currentDBDef The definition of current DBMS. (NotNull)
     * @param outsideSqlOption The option of outsideSql. (NotNull)
     * @return The new-created instance of executor. (NotNull)
     */
    <BEHAVIOR> OutsideSqlAutoPagingExecutor<BEHAVIOR> createAutoPaging(BehaviorCommandInvoker behaviorCommandInvoker, String tableDbName,
            DBDef currentDBDef, OutsideSqlOption outsideSqlOption);
}
