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
package org.seasar.dbflute.outsidesql.factory;

import org.seasar.dbflute.DBDef;
import org.seasar.dbflute.bhv.core.BehaviorCommandInvoker;
import org.seasar.dbflute.jdbc.StatementConfig;
import org.seasar.dbflute.outsidesql.OutsideSqlFilter;
import org.seasar.dbflute.outsidesql.OutsideSqlOption;
import org.seasar.dbflute.outsidesql.executor.OutsideSqlAutoPagingExecutor;
import org.seasar.dbflute.outsidesql.executor.OutsideSqlBasicExecutor;
import org.seasar.dbflute.outsidesql.executor.OutsideSqlCursorExecutor;
import org.seasar.dbflute.outsidesql.executor.OutsideSqlEntityExecutor;
import org.seasar.dbflute.outsidesql.executor.OutsideSqlManualPagingExecutor;

/**
 * @author jflute
 */
public class DefaultOutsideSqlExecutorFactory implements OutsideSqlExecutorFactory {

    /**
     * {@inheritDoc}
     */
    public <BEHAVIOR> OutsideSqlBasicExecutor<BEHAVIOR> createBasic(BehaviorCommandInvoker behaviorCommandInvoker,
            String tableDbName, DBDef currentDBDef, StatementConfig defaultStatementConfig,
            OutsideSqlOption outsideSqlOption) {
        final OutsideSqlContextFactory outsideSqlContextFactory = createOutsideSqlContextFactory();
        final OutsideSqlFilter outsideSqlFilter = createOutsideSqlExecutionFilter();
        return new OutsideSqlBasicExecutor<BEHAVIOR>(behaviorCommandInvoker, tableDbName, currentDBDef,
                defaultStatementConfig, outsideSqlOption, outsideSqlContextFactory, outsideSqlFilter, this);
    }

    /**
     * {@inheritDoc}
     */
    public <BEHAVIOR> OutsideSqlCursorExecutor<BEHAVIOR> createCursor(BehaviorCommandInvoker behaviorCommandInvoker,
            String tableDbName, DBDef currentDBDef, OutsideSqlOption outsideSqlOption) {
        final OutsideSqlContextFactory outsideSqlContextFactory = createOutsideSqlContextFactory();
        final OutsideSqlFilter outsideSqlFilter = createOutsideSqlExecutionFilter();
        return new OutsideSqlCursorExecutor<BEHAVIOR>(behaviorCommandInvoker, tableDbName, currentDBDef,
                outsideSqlOption, outsideSqlContextFactory, outsideSqlFilter, this);
    }

    /**
     * Create the factory of outside-SQL context. <br />
     * This is the very point for an extension of the outside-SQL context. 
     * @return The instance of the factory. (NotNull)
     */
    protected OutsideSqlContextFactory createOutsideSqlContextFactory() { // extension point
        return new DefaultOutsideSqlContextFactory();
    }

    /**
     * Create the filter of outside-SQL. <br />
     * This is the very point for an extension of the outside-SQL filtering. 
     * @return The instance of the filter. (NullAllowed)
     */
    protected OutsideSqlFilter createOutsideSqlExecutionFilter() { // extension point
        return null; // as default (no filter)
    }

    /**
     * {@inheritDoc}
     */
    public <BEHAVIOR> OutsideSqlEntityExecutor<BEHAVIOR> createEntity(BehaviorCommandInvoker behaviorCommandInvoker,
            String tableDbName, DBDef currentDBDef, StatementConfig defaultStatementConfig,
            OutsideSqlOption outsideSqlOption) {
        return new OutsideSqlEntityExecutor<BEHAVIOR>(behaviorCommandInvoker, tableDbName, currentDBDef,
                defaultStatementConfig, outsideSqlOption, this);
    }

    /**
     * {@inheritDoc}
     */
    public <BEHAVIOR> OutsideSqlManualPagingExecutor<BEHAVIOR> createManualPaging(
            BehaviorCommandInvoker behaviorCommandInvoker, String tableDbName, DBDef currentDBDef,
            StatementConfig defaultStatementConfig, OutsideSqlOption outsideSqlOption) {
        return new OutsideSqlManualPagingExecutor<BEHAVIOR>(behaviorCommandInvoker, tableDbName, currentDBDef,
                defaultStatementConfig, outsideSqlOption, this);
    }

    /**
     * {@inheritDoc}
     */
    public <BEHAVIOR> OutsideSqlAutoPagingExecutor<BEHAVIOR> createAutoPaging(
            BehaviorCommandInvoker behaviorCommandInvoker, String tableDbName, DBDef currentDBDef,
            StatementConfig defaultStatementConfig, OutsideSqlOption outsideSqlOption) {
        return new OutsideSqlAutoPagingExecutor<BEHAVIOR>(behaviorCommandInvoker, tableDbName, currentDBDef,
                defaultStatementConfig, outsideSqlOption, this);
    }
}
