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

import java.util.Map;

import javax.sql.DataSource;

import org.seasar.dbflute.CallbackContext;
import org.seasar.dbflute.bhv.SqlStringFilter;
import org.seasar.dbflute.bhv.core.BehaviorCommandMeta;
import org.seasar.dbflute.jdbc.StatementFactory;
import org.seasar.dbflute.outsidesql.OutsideSqlFilter;
import org.seasar.dbflute.outsidesql.OutsideSqlFilter.ExecutionFilterType;
import org.seasar.dbflute.resource.ResourceContext;
import org.seasar.dbflute.util.Srl;

/**
 * The SQL execution by outside-SQL. <br />
 * This has filter options.
 * @author jflute
 */
public abstract class AbstractOutsideSqlExecution extends AbstractFixedSqlExecution {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected boolean _removeBlockComment;
    protected boolean _removeLineComment;
    protected boolean _formatSql;
    protected OutsideSqlFilter _outsideSqlFilter;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * Constructor.
     * @param dataSource The data source for a database connection. (NotNull)
     * @param statementFactory The factory of statement. (NotNull)
     * @param argNameTypeMap The map of names and types for arguments. (NotNull)
     * @param twoWaySql The SQL string as 2Way-SQL. (NotNull)
     */
    public AbstractOutsideSqlExecution(DataSource dataSource, StatementFactory statementFactory,
            Map<String, Class<?>> argNameTypeMap, String twoWaySql) {
        super(dataSource, statementFactory, argNameTypeMap, twoWaySql);
    }

    // ===================================================================================
    //                                                                              Filter
    //                                                                              ======
    @Override
    protected String filterExecutedSql(String executedSql) {
        executedSql = super.filterExecutedSql(executedSql);
        executedSql = doFilterExecutedSqlByOutsideSqlFilter(executedSql);
        if (_removeBlockComment) {
            executedSql = Srl.removeBlockComment(executedSql);
        }
        if (_removeLineComment) {
            executedSql = Srl.removeLineComment(executedSql);
        }
        if (_formatSql) {
            executedSql = Srl.removeEmptyLine(executedSql);
        }
        executedSql = doFilterExecutedSqlByCallbackFilter(executedSql);
        return executedSql;
    }

    protected String doFilterExecutedSqlByOutsideSqlFilter(String executedSql) {
        if (_outsideSqlFilter != null) {
            final ExecutionFilterType filterType = getOutsideSqlExecutionFilterType();
            return _outsideSqlFilter.filterExecution(executedSql, filterType);
        }
        return executedSql;
    }

    protected abstract OutsideSqlFilter.ExecutionFilterType getOutsideSqlExecutionFilterType();

    protected String doFilterExecutedSqlByCallbackFilter(String executedSql) {
        final SqlStringFilter sqlStringFilter = getSqlStringFilter();
        if (sqlStringFilter != null) {
            final BehaviorCommandMeta meta = ResourceContext.behaviorCommand();
            final String filteredSql = sqlStringFilter.filterOutsideSql(meta, executedSql);
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
    //                                                                            Accessor
    //                                                                            ========
    public boolean isRemoveBlockComment() {
        return _removeBlockComment;
    }

    public void setRemoveBlockComment(boolean removeBlockComment) {
        this._removeBlockComment = removeBlockComment;
    }

    public boolean isRemoveLineComment() {
        return _removeLineComment;
    }

    public void setRemoveLineComment(boolean removeLineComment) {
        this._removeLineComment = removeLineComment;
    }

    public boolean isRemoveEmptyLine() {
        return _formatSql;
    }

    public void setFormatSql(boolean formatSql) {
        this._formatSql = formatSql;
    }

    public OutsideSqlFilter getOutsideSqlFilter() {
        return _outsideSqlFilter;
    }

    public void setOutsideSqlFilter(OutsideSqlFilter outsideSqlFilter) {
        this._outsideSqlFilter = outsideSqlFilter;
    }
}
