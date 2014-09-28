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

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.seasar.dbflute.CallbackContext;
import org.seasar.dbflute.bhv.SqlStringFilter;
import org.seasar.dbflute.bhv.core.BehaviorCommandMeta;
import org.seasar.dbflute.jdbc.StatementFactory;
import org.seasar.dbflute.outsidesql.OutsideSqlContext;
import org.seasar.dbflute.outsidesql.OutsideSqlFilter;
import org.seasar.dbflute.outsidesql.ProcedurePmb;
import org.seasar.dbflute.resource.ResourceContext;
import org.seasar.dbflute.s2dao.jdbc.TnResultSetHandler;
import org.seasar.dbflute.s2dao.metadata.TnProcedureMetaData;
import org.seasar.dbflute.s2dao.metadata.TnProcedureParameterType;
import org.seasar.dbflute.s2dao.sqlhandler.TnProcedureHandler;
import org.seasar.dbflute.s2dao.sqlhandler.TnProcedureHandler.TnProcedureResultSetHandlerProvider;

/**
 * @author modified by jflute (originated in S2Dao)
 */
public class TnProcedureCommand extends TnAbstractBasicSqlCommand {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final TnProcedureMetaData _procedureMetaData;
    protected final TnProcedureResultSetHandlerFactory _procedureResultSetHandlerFactory;

    /** The filter of outside-SQL. (NullAllowed) */
    protected OutsideSqlFilter _outsideSqlFilter;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public TnProcedureCommand(DataSource dataSource, StatementFactory statementFactory,
            TnProcedureMetaData procedureMetaData, TnProcedureResultSetHandlerFactory procedureResultSetHandlerFactory) {
        super(dataSource, statementFactory);
        assertObjectNotNull("procedureMetaData", procedureMetaData);
        assertObjectNotNull("procedureResultSetHandlerFactory", procedureResultSetHandlerFactory);
        _procedureMetaData = procedureMetaData;
        _procedureResultSetHandlerFactory = procedureResultSetHandlerFactory;
    }

    public static interface TnProcedureResultSetHandlerFactory { // is needed to construct an instance
        TnResultSetHandler createBeanHandler(Class<?> beanClass);

        TnResultSetHandler createMapHandler();
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    public Object execute(final Object[] args) {
        // the args is unused because of getting from context
        // (actually the args has same parameter as context)

        final OutsideSqlContext outsideSqlContext = OutsideSqlContext.getOutsideSqlContextOnThread();
        final Object pmb = outsideSqlContext.getParameterBean(); // basically implements ProcedurePmb
        final TnProcedureHandler handler = createProcedureHandler(pmb);
        final Object[] onlyPmbArgs = new Object[] { pmb };

        // The method that builds display SQL is overridden for procedure
        // so it can set arguments which have only parameter bean
        handler.setExceptionMessageSqlArgs(onlyPmbArgs);

        return handler.execute(onlyPmbArgs);
    }

    protected TnProcedureHandler createProcedureHandler(Object pmb) {
        final String sql = filterExecutedSql(buildSql(pmb));
        TnProcedureResultSetHandlerProvider provider = createProcedureResultSetHandlerProvider();
        return new TnProcedureHandler(_dataSource, _statementFactory, sql, _procedureMetaData, provider);
    }

    protected String filterExecutedSql(String executedSql) {
        executedSql = doFilterExecutedSqlByOutsideSqlFilter(executedSql);
        executedSql = doFilterExecutedSqlByCallbackFilter(executedSql);
        return executedSql;
    }

    protected String doFilterExecutedSqlByOutsideSqlFilter(String executedSql) {
        if (_outsideSqlFilter != null) {
            return _outsideSqlFilter.filterExecution(executedSql, OutsideSqlFilter.ExecutionFilterType.PROCEDURE);
        }
        return executedSql;
    }

    protected String doFilterExecutedSqlByCallbackFilter(String executedSql) {
        final SqlStringFilter sqlStringFilter = getSqlStringFilter();
        if (sqlStringFilter != null) {
            final BehaviorCommandMeta meta = ResourceContext.behaviorCommand();
            final String filteredSql = sqlStringFilter.filterProcedure(meta, executedSql);
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

    protected String buildSql(Object pmb) {
        final String procedureName = _procedureMetaData.getProcedureName();
        final int bindSize = _procedureMetaData.getBindParameterTypeList().size();
        final boolean existsReturn = _procedureMetaData.hasReturnParameterType();

        return doBuildSql(pmb, procedureName, bindSize, existsReturn);
    }

    protected String doBuildSql(Object pmb, String procedureName, int bindSize, boolean existsReturn) {
        // normally escape and call
        boolean kakou = true;
        boolean calledBySelect = false;
        if (pmb instanceof ProcedurePmb) { // so you can specify through ProcedurePmb
            kakou = ((ProcedurePmb) pmb).isEscapeStatement();
            calledBySelect = ((ProcedurePmb) pmb).isCalledBySelect();
        }
        if (calledBySelect) { // for example, table valued function
            return doBuildSqlAsCalledBySelect(procedureName, bindSize);
        } else { // basically here
            return doBuildSqlAsProcedureCall(procedureName, bindSize, existsReturn, kakou);
        }
    }

    protected String doBuildSqlAsCalledBySelect(String procedureName, int bindSize) {
        final StringBuilder sb = new StringBuilder();
        sb.append("select * from ").append(procedureName).append("(");
        for (int i = 0; i < bindSize; i++) {
            sb.append("?, ");
        }
        if (bindSize > 0) {
            sb.setLength(sb.length() - 2);
        }
        sb.append(")");
        return sb.toString();
    }

    protected String doBuildSqlAsProcedureCall(String procedureName, int bindSize, boolean existsReturn, boolean kakou) {
        final StringBuilder sb = new StringBuilder();
        final int argSize;
        {
            if (existsReturn) {
                sb.append("? = ");
                argSize = bindSize - 1;
            } else {
                argSize = bindSize;
            }
        }
        sb.append("call ").append(procedureName).append("(");
        for (int i = 0; i < argSize; i++) {
            sb.append("?, ");
        }
        if (argSize > 0) {
            sb.setLength(sb.length() - 2);
        }
        sb.append(")");
        if (kakou) {
            sb.insert(0, "{").append("}");
        }
        return sb.toString();
    }

    protected TnProcedureResultSetHandlerProvider createProcedureResultSetHandlerProvider() {
        return new TnProcedureResultSetHandlerProvider() {
            public TnResultSetHandler provideResultSetHandler(TnProcedureParameterType ppt) {
                final Class<?> parameterType = ppt.getParameterType();
                if (!List.class.isAssignableFrom(parameterType)) {
                    String msg = "The parameter type for result set should be List:";
                    msg = msg + " parameter=" + ppt.getParameterName() + " type=" + parameterType;
                    throw new IllegalStateException(msg);
                }
                final Class<?> elementType = ppt.getElementType();
                if (elementType == null) {
                    String msg = "The parameter type for result set should have generic type of List:";
                    msg = msg + " parameter=" + ppt.getParameterName() + " type=" + parameterType;
                    throw new IllegalStateException(msg);
                }
                if (Map.class.isAssignableFrom(elementType)) {
                    return _procedureResultSetHandlerFactory.createMapHandler();
                } else {
                    return _procedureResultSetHandlerFactory.createBeanHandler(elementType);
                }
            }
        };
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setOutsideSqlFilter(OutsideSqlFilter outsideSqlFilter) {
        _outsideSqlFilter = outsideSqlFilter;
    }
}
