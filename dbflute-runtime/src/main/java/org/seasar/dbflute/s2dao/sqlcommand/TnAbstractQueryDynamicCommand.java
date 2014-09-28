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
package org.seasar.dbflute.s2dao.sqlcommand;

import javax.sql.DataSource;

import org.seasar.dbflute.CallbackContext;
import org.seasar.dbflute.bhv.SqlStringFilter;
import org.seasar.dbflute.bhv.core.BehaviorCommandMeta;
import org.seasar.dbflute.jdbc.StatementFactory;
import org.seasar.dbflute.resource.ResourceContext;
import org.seasar.dbflute.s2dao.sqlhandler.TnCommandContextHandler;
import org.seasar.dbflute.twowaysql.SqlAnalyzer;
import org.seasar.dbflute.twowaysql.context.CommandContext;
import org.seasar.dbflute.twowaysql.context.CommandContextCreator;
import org.seasar.dbflute.twowaysql.node.Node;

/**
 * @author modified by jflute (originated in S2Dao)
 */
public abstract class TnAbstractQueryDynamicCommand extends TnAbstractBasicSqlCommand {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public TnAbstractQueryDynamicCommand(DataSource dataSource, StatementFactory statementFactory) {
        super(dataSource, statementFactory);
    }

    // ===================================================================================
    //                                                              CommandContext Handler
    //                                                              ======================
    protected TnCommandContextHandler createCommandContextHandler(CommandContext context) {
        final String executedSql = filterSqlStringByCallbackFilter(context.getSql());
        final TnCommandContextHandler handler = newCommandContextHandler(executedSql, context);
        handler.setUpdateSQLFailureProcessTitle(getUpdateSQLFailureProcessTitle());
        return handler;
    }

    protected String filterSqlStringByCallbackFilter(String executedSql) {
        final SqlStringFilter sqlStringFilter = getSqlStringFilter();
        if (sqlStringFilter != null) {
            final BehaviorCommandMeta meta = ResourceContext.behaviorCommand();
            final String filteredSql = sqlStringFilter.filterQueryUpdate(meta, executedSql);
            return filteredSql != null ? filteredSql : executedSql;
        }
        return executedSql;
    }

    protected SqlStringFilter getSqlStringFilter() {
        if (!CallbackContext.isExistSqlStringFilterOnThread()) {
            return null;
        }
        return CallbackContext.getCallbackContextOnThread().getSqlStringFilter();
    }

    protected TnCommandContextHandler newCommandContextHandler(String executedSql, CommandContext context) {
        return new TnCommandContextHandler(_dataSource, _statementFactory, executedSql, context);
    }

    protected abstract String getUpdateSQLFailureProcessTitle();

    // ===================================================================================
    //                                                                      CommandContext
    //                                                                      ==============
    protected CommandContext createCommandContext(String twoWaySql, String[] argNames, Class<?>[] argTypes,
            Object[] args) {
        final CommandContext ctx;
        {
            final SqlAnalyzer analyzer = createSqlAnalyzer(twoWaySql);
            final Node node = analyzer.analyze();
            final CommandContextCreator creator = new CommandContextCreator(argNames, argTypes);
            ctx = creator.createCommandContext(args);
            node.accept(ctx);
        }
        return ctx;
    }

    protected SqlAnalyzer createSqlAnalyzer(String twoWaySql) {
        return ResourceContext.createSqlAnalyzer(twoWaySql, true);
    }
}
