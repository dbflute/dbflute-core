/*
 * Copyright 2014-2019 the original author or authors.
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
package org.dbflute.s2dao.sqlcommand;

import javax.sql.DataSource;

import org.dbflute.bhv.core.context.ResourceContext;
import org.dbflute.jdbc.StatementFactory;
import org.dbflute.s2dao.sqlhandler.TnBasicParameterHandler;
import org.dbflute.twowaysql.SqlAnalyzer;
import org.dbflute.twowaysql.context.CommandContext;
import org.dbflute.twowaysql.context.CommandContextCreator;
import org.dbflute.twowaysql.node.Node;

/**
 * The SQL execution of 2Way-SQL.
 * @author modified by jflute (originated in S2Dao)
 * @since 0.9.7.9 (2010/12/26 Sunday)
 */
public abstract class TnAbstractTwoWaySqlCommand extends TnAbstractBasicSqlCommand {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * Constructor.
     * @param dataSource The data source for a database connection. (NotNull)
     * @param statementFactory The factory of statement. (NotNull)
     */
    public TnAbstractTwoWaySqlCommand(DataSource dataSource, StatementFactory statementFactory) {
        super(dataSource, statementFactory);
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    public Object execute(Object[] args) {
        final Node rootNode = getRootNode(args);
        final CommandContext ctx = apply(rootNode, args, getArgNames(args), getArgTypes(args));
        final String executedSql = filterExecutedSql(ctx);
        final TnBasicParameterHandler handler = createBasicParameterHandler(ctx, executedSql);
        final Object[] bindVariables = ctx.getBindVariables();
        final Class<?>[] bindVariableTypes = ctx.getBindVariableTypes();
        return filterReturnValue(handler.execute(bindVariables, bindVariableTypes));
    }

    // ===================================================================================
    //                                                                            Resource
    //                                                                            ========
    protected abstract Node getRootNode(Object[] args);

    protected abstract String[] getArgNames(Object[] args);

    protected abstract Class<?>[] getArgTypes(Object[] args);

    // ===================================================================================
    //                                                                             Handler
    //                                                                             =======
    protected TnBasicParameterHandler createBasicParameterHandler(CommandContext context, String executedSql) {
        final TnBasicParameterHandler handler = newBasicParameterHandler(executedSql);
        final Object[] bindVariables = context.getBindVariables();
        handler.setExceptionMessageSqlArgs(bindVariables);
        return handler;
    }

    protected abstract TnBasicParameterHandler newBasicParameterHandler(String executedSql);

    // ===================================================================================
    //                                                                              Filter
    //                                                                              ======
    protected String filterExecutedSql(CommandContext ctx) {
        return ctx.getSql();
    }

    protected Object filterReturnValue(Object returnValue) {
        return returnValue;
    }

    // ===================================================================================
    //                                                                        SQL Handling
    //                                                                        ============
    protected Node analyzeTwoWaySql(String twoWaySql) {
        return createSqlAnalyzer(twoWaySql).analyze();
    }

    protected SqlAnalyzer createSqlAnalyzer(String twoWaySql) {
        return ResourceContext.createSqlAnalyzer(twoWaySql, isBlockNullParameter());
    }

    protected boolean isBlockNullParameter() { // extension point
        return false; // as default
    }

    // ===================================================================================
    //                                                                   Argument Handling
    //                                                                   =================
    protected CommandContext apply(Node rootNode, Object[] args, String[] argNames, Class<?>[] argTypes) {
        final CommandContext ctx = createCommandContext(args, argNames, argTypes);
        rootNode.accept(ctx);
        return ctx;
    }

    protected CommandContext createCommandContext(Object[] args, String[] argNames, Class<?>[] argTypes) {
        return createCommandContextCreator(argNames, argTypes).createCommandContext(args);
    }

    protected CommandContextCreator createCommandContextCreator(String[] argNames, Class<?>[] argTypes) {
        return new CommandContextCreator(argNames, argTypes);
    }
}
