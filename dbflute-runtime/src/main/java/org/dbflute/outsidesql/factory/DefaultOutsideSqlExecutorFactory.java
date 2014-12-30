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
package org.dbflute.outsidesql.factory;

import org.dbflute.bhv.core.BehaviorCommandInvoker;
import org.dbflute.dbway.DBDef;
import org.dbflute.outsidesql.OutsideSqlFilter;
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
public class DefaultOutsideSqlExecutorFactory implements OutsideSqlExecutorFactory {

    /**
     * {@inheritDoc}
     */
    public <BEHAVIOR> OutsideSqlAllFacadeExecutor<BEHAVIOR> createAllFacade(OutsideSqlBasicExecutor<BEHAVIOR> basicExecutor) {
        return new OutsideSqlAllFacadeExecutor<BEHAVIOR>(basicExecutor);
    }

    /**
     * {@inheritDoc}
     */
    public <BEHAVIOR> OutsideSqlBasicExecutor<BEHAVIOR> createBasic(BehaviorCommandInvoker behaviorCommandInvoker, String tableDbName,
            DBDef currentDBDef, OutsideSqlOption outsideSqlOption) {
        final OutsideSqlContextFactory factory = createOutsideSqlContextFactory();
        final OutsideSqlFilter filter = createOutsideSqlExecutionFilter();
        return new OutsideSqlBasicExecutor<BEHAVIOR>(behaviorCommandInvoker, tableDbName, currentDBDef, outsideSqlOption, factory, filter,
                this);
    }

    /**
     * {@inheritDoc}
     */
    public <BEHAVIOR> OutsideSqlCursorExecutor<BEHAVIOR> createCursor(BehaviorCommandInvoker behaviorCommandInvoker, String tableDbName,
            DBDef currentDBDef, OutsideSqlOption outsideSqlOption) {
        final OutsideSqlContextFactory factory = createOutsideSqlContextFactory();
        final OutsideSqlFilter filter = createOutsideSqlExecutionFilter();
        return new OutsideSqlCursorExecutor<BEHAVIOR>(behaviorCommandInvoker, tableDbName, currentDBDef, outsideSqlOption, factory, filter,
                this);
    }

    /**
     * Create the factory of outside-SQL context. <br>
     * This is the very point for an extension of the outside-SQL context. 
     * @return The instance of the factory. (NotNull)
     */
    protected OutsideSqlContextFactory createOutsideSqlContextFactory() { // extension point
        return new DefaultOutsideSqlContextFactory();
    }

    /**
     * Create the filter of outside-SQL. <br>
     * This is the very point for an extension of the outside-SQL filtering. 
     * @return The instance of the filter. (NullAllowed)
     */
    protected OutsideSqlFilter createOutsideSqlExecutionFilter() { // extension point
        return null; // as default (no filter)
    }

    /**
     * {@inheritDoc}
     */
    public <BEHAVIOR> OutsideSqlEntityExecutor<BEHAVIOR> createEntity(BehaviorCommandInvoker behaviorCommandInvoker, String tableDbName,
            DBDef currentDBDef, OutsideSqlOption outsideSqlOption) {
        return new OutsideSqlEntityExecutor<BEHAVIOR>(behaviorCommandInvoker, tableDbName, currentDBDef, outsideSqlOption, this);
    }

    /**
     * {@inheritDoc}
     */
    public <BEHAVIOR> OutsideSqlManualPagingExecutor<BEHAVIOR> createManualPaging(BehaviorCommandInvoker behaviorCommandInvoker,
            String tableDbName, DBDef currentDBDef, OutsideSqlOption outsideSqlOption) {
        return new OutsideSqlManualPagingExecutor<BEHAVIOR>(behaviorCommandInvoker, tableDbName, currentDBDef, outsideSqlOption, this);
    }

    /**
     * {@inheritDoc}
     */
    public <BEHAVIOR> OutsideSqlAutoPagingExecutor<BEHAVIOR> createAutoPaging(BehaviorCommandInvoker behaviorCommandInvoker,
            String tableDbName, DBDef currentDBDef, OutsideSqlOption outsideSqlOption) {
        return new OutsideSqlAutoPagingExecutor<BEHAVIOR>(behaviorCommandInvoker, tableDbName, currentDBDef, outsideSqlOption, this);
    }
}
