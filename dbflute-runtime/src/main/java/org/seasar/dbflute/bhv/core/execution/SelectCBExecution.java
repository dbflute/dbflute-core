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
package org.seasar.dbflute.bhv.core.execution;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.seasar.dbflute.CallbackContext;
import org.seasar.dbflute.Entity;
import org.seasar.dbflute.bhv.SqlStringFilter;
import org.seasar.dbflute.bhv.core.BehaviorCommandMeta;
import org.seasar.dbflute.cbean.ConditionBean;
import org.seasar.dbflute.cbean.ckey.ConditionKey;
import org.seasar.dbflute.cbean.sqlclause.SqlClause;
import org.seasar.dbflute.dbmeta.DBMeta;
import org.seasar.dbflute.dbmeta.info.ColumnInfo;
import org.seasar.dbflute.dbmeta.info.UniqueInfo;
import org.seasar.dbflute.jdbc.StatementFactory;
import org.seasar.dbflute.resource.ResourceContext;
import org.seasar.dbflute.s2dao.jdbc.TnResultSetHandler;
import org.seasar.dbflute.s2dao.sqlhandler.TnBasicParameterHandler;
import org.seasar.dbflute.s2dao.sqlhandler.TnBasicSelectHandler;
import org.seasar.dbflute.twowaysql.node.Node;

/**
 * The SQL execution of select by condition-bean. <br />
 * The first element of arguments should be condition-bean (and not null).
 * @author jflute
 */
public class SelectCBExecution extends AbstractFixedArgExecution {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final TnResultSetHandler _resultSetHandler;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * Constructor.
     * @param dataSource The data source for a database connection. (NotNull)
     * @param statementFactory The factory of statement. (NotNull)
     * @param argNameTypeMap The map of names and types for arguments. (NotNull)
     * @param resultSetHandler The handler of result set. (NotNull)
     */
    public SelectCBExecution(DataSource dataSource, StatementFactory statementFactory,
            Map<String, Class<?>> argNameTypeMap, TnResultSetHandler resultSetHandler) {
        super(dataSource, statementFactory, argNameTypeMap);
        assertObjectNotNull("resultSetHandler", resultSetHandler);
        _resultSetHandler = resultSetHandler;
    }

    // ===================================================================================
    //                                                                            Resource
    //                                                                            ========
    @Override
    public Object execute(Object[] args) {
        final ConditionBean cb = extractConditionBean(args);
        final Object splitResult = processPagingSelectAndQuerySplit(args, cb);
        if (splitResult != null) { // rarely
            return splitResult;
        }
        return superExecute(args); // basically here
    }

    protected Object superExecute(Object[] args) {
        return super.execute(args);
    }

    @Override
    protected Node getRootNode(Object[] args) {
        return analyzeTwoWaySql(extractTwoWaySql(args)); // dynamic analysis
    }

    protected String extractTwoWaySql(Object[] args) {
        final ConditionBean cb = extractConditionBean(args);
        return cb.getSqlClause().getClause();
    }

    // -----------------------------------------------------
    //                                     Argument Handling
    //                                     -----------------
    protected ConditionBean extractConditionBean(Object[] args) {
        assertArgsValid(args);
        final Object firstElement = args[0];
        assertObjectNotNull("args[0]", firstElement);
        assertFirstElementConditionBean(firstElement);
        final ConditionBean cb = (ConditionBean) firstElement;
        return cb;
    }

    protected void assertArgsValid(Object[] args) {
        if (args == null) {
            String msg = "The argument 'args' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        if (args.length == 0) {
            String msg = "The argument 'args' should not be empty.";
            throw new IllegalArgumentException(msg);
        }
    }

    protected void assertFirstElementConditionBean(Object firstElement) {
        if (!(firstElement instanceof ConditionBean)) {
            String msg = "The first element of 'args' should be condition-bean: " + firstElement.getClass();
            throw new IllegalArgumentException(msg);
        }
    }

    // ===================================================================================
    //                                                       Paging Select and Query Split
    //                                                       =============================
    protected Object processPagingSelectAndQuerySplit(Object[] args, ConditionBean cb) {
        if (!cb.canPagingSelectAndQuerySplit()) {
            return null;
        }
        if (!cb.isFetchScopeEffective()) {
            return null;
        }
        final DBMeta dbmeta = cb.getDBMeta();
        final UniqueInfo primaryUniqueInfo = dbmeta.getPrimaryUniqueInfo();
        if (primaryUniqueInfo.isTwoOrMore()) { // basically no way, already checked
            return null;
        }
        final ColumnInfo pkColumn = primaryUniqueInfo.getFirstColumn();
        final SqlClause sqlClause = cb.getSqlClause();
        final List<Object> pkList = doSplitSelectFirst(args, cb, dbmeta, sqlClause);
        if (pkList == null) { // no way just in case
            return null;
        }
        if (pkList.isEmpty()) {
            return pkList;
        }
        return doSplitSelectSecond(args, cb, pkColumn, sqlClause, pkList);
    }

    protected List<Object> doSplitSelectFirst(Object[] args, ConditionBean cb, DBMeta dbmeta, SqlClause sqlClause) {
        final List<Object> pkList = new ArrayList<Object>();
        try {
            sqlClause.enablePKOnlySelectForcedly();
            final Object firstResult = superExecute(args);
            if (firstResult == null || !(firstResult instanceof List)) { // no way just in case
                return null;
            }
            @SuppressWarnings("unchecked")
            final List<Entity> entityList = (List<Entity>) firstResult;
            for (Entity entity : entityList) {
                final Map<String, Object> primaryKeyMap = dbmeta.extractPrimaryKeyMap(entity);
                pkList.add(primaryKeyMap.values().iterator().next()); // only-one here
            }
            return pkList;
        } finally {
            sqlClause.disablePKOnlySelectForcedly();
        }
    }

    protected Object doSplitSelectSecond(Object[] args, ConditionBean cb, ColumnInfo pkColumn, SqlClause sqlClause,
            List<Object> pkList) {
        final int fetchSize = sqlClause.getFetchSize();
        final int fetchPageNumber = sqlClause.getFetchPageNumber();
        try {
            sqlClause.backupWhereClauseOnBaseQuery();
            sqlClause.clearWhereClauseOnBaseQuery();
            sqlClause.suppressFetchScope();

            // order by is inherited
            // basically small list here so one more order-by is not problem
            final String ckey = ConditionKey.CK_IN_SCOPE.getConditionKey();
            cb.localCQ().invokeQuery(pkColumn.getColumnDbName(), ckey, pkList);
            return superExecute(args);
        } finally {
            sqlClause.restoreWhereClauseOnBaseQuery();
            sqlClause.fetchFirst(fetchSize);
            sqlClause.fetchPage(fetchPageNumber);
        }
    }

    // ===================================================================================
    //                                                                             Handler
    //                                                                             =======
    @Override
    protected TnBasicParameterHandler newBasicParameterHandler(String executedSql) {
        return new TnBasicSelectHandler(_dataSource, executedSql, _resultSetHandler, _statementFactory);
    }

    // ===================================================================================
    //                                                                              Filter
    //                                                                              ======
    @Override
    protected String filterExecutedSql(String executedSql) {
        return doFilterExecutedSqlByCallbackFilter(super.filterExecutedSql(executedSql));
    }

    protected String doFilterExecutedSqlByCallbackFilter(String executedSql) {
        final SqlStringFilter sqlStringFilter = getSqlStringFilter();
        if (sqlStringFilter != null) {
            final BehaviorCommandMeta meta = ResourceContext.behaviorCommand();
            final String filteredSql = sqlStringFilter.filterSelectCB(meta, executedSql);
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

    // ===================================================================================
    //                                                                        SQL Handling
    //                                                                        ============
    @Override
    protected boolean isBlockNullParameter() {
        return true; // because the SQL is select
    }
}
