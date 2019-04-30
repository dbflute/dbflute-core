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
package org.dbflute.cbean.sqlclause;

import org.dbflute.dbway.DBDef;
import org.dbflute.dbway.DBWay;

/**
 * SqlClause for H2.
 * @author jflute
 */
public class SqlClauseH2 extends AbstractSqlClause {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The serial version UID for object serialization. (Default) */
    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** String of fetch-scope as sql-suffix. */
    protected String _fetchScopeSqlSuffix = "";

    /** String of lock as sql-suffix. */
    protected String _lockSqlSuffix = "";

    /** The binding value for paging as 'limit'. */
    protected Integer _pagingBindingLimit;

    /** The binding value for paging as 'offset'. */
    protected Integer _pagingBindingOffset;

    /** Does it suppress bind variable for paging? */
    protected boolean _suppressPagingBinding;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * Constructor.
     * @param tableDbName The DB name of table. (NotNull)
     **/
    public SqlClauseH2(String tableDbName) {
        super(tableDbName);
    }

    // ===================================================================================
    //                                                                 FetchScope Override
    //                                                                 ===================
    /**
     * {@inheritDoc}
     */
    protected void doFetchFirst() {
        doFetchPage();
    }

    /**
     * {@inheritDoc}
     */
    protected void doFetchPage() {
        if (_suppressPagingBinding) {
            _fetchScopeSqlSuffix = " limit " + getFetchSize() + " offset " + getPageStartIndex();
        } else { // mainly here
            _pagingBindingLimit = getFetchSize();
            _pagingBindingOffset = getPageStartIndex();
            _fetchScopeSqlSuffix = " limit /*pmb.sqlClause.pagingBindingLimit*/0 offset /*pmb.sqlClause.pagingBindingOffset*/0";
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void doClearFetchPageClause() {
        _fetchScopeSqlSuffix = "";
    }

    // ===================================================================================
    //                                                                       Lock Override
    //                                                                       =============
    /**
     * {@inheritDoc}
     */
    public void lockForUpdate() {
        _lockSqlSuffix = " for update";
    }

    // ===================================================================================
    //                                                                       Hint Override
    //                                                                       =============
    /**
     * {@inheritDoc}
     */
    protected String createSelectHint() {
        return "";
    }

    /**
     * {@inheritDoc}
     */
    protected String createFromBaseTableHint() {
        return "";
    }

    /**
     * {@inheritDoc}
     */
    protected String createFromHint() {
        return "";
    }

    /**
     * {@inheritDoc}
     */
    protected String createSqlSuffix() {
        return _fetchScopeSqlSuffix + _lockSqlSuffix;
    }

    // [DBFlute-0.9.8.4]
    // ===================================================================================
    //                                                                               DBWay
    //                                                                               =====
    public DBWay dbway() {
        return DBDef.H2.dbway();
    }

    // [DBFlute-1.0.4D]
    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public Integer getPagingBindingLimit() { // for parameter comment
        return _pagingBindingLimit;
    }

    public Integer getPagingBindingOffset() { // for parameter comment
        return _pagingBindingOffset;
    }

    public SqlClauseH2 suppressPagingBinding() { // for compatible? anyway, just in case
        _suppressPagingBinding = true;
        return this;
    }
}
