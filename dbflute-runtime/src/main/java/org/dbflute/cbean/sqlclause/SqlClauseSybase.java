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

import org.dbflute.cbean.sqlclause.orderby.OrderByClause;
import org.dbflute.dbway.DBDef;
import org.dbflute.dbway.DBWay;

/**
 * SqlClause for Sybase.
 * @author jflute
 */
public class SqlClauseSybase extends AbstractSqlClause {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The serial version UID for object serialization. (Default) */
    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** String of fetch-first as select-hint. */
    protected String _fetchFirstSelectHint = "";

    /** String of lock as sql-suffix. */
    protected String _lockSqlSuffix = "";

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * Constructor.
     * @param tableDbName The DB name of table. (NotNull)
     **/
    public SqlClauseSybase(String tableDbName) {
        super(tableDbName);
    }

    // ===================================================================================
    //                                                                Main Clause Override
    //                                                                ====================
    @Override
    protected boolean isUnionNormalSelectEnclosingRequired() {
        return true;
    }

    // ===================================================================================
    //                                                               Clause Parts Override
    //                                                               =====================
    @Override
    protected void appendSelectHint(StringBuilder sb) {
        if (needsUnionNormalSelectEnclosing()) {
            return; // because clause should be enclosed when union normal select
        }
        super.appendSelectHint(sb);
    }

    // ===================================================================================
    //                                                                    OrderBy Override
    //                                                                    ================
    @Override
    protected OrderByClause.OrderByNullsSetupper createOrderByNullsSetupper() {
        return createOrderByNullsSetupperByCaseWhen();
    }

    // ===================================================================================
    //                                                                 FetchScope Override
    //                                                                 ===================
    /**
     * {@inheritDoc}
     */
    protected void doFetchFirst() {
        if (isFetchSizeSupported()) {
            _fetchFirstSelectHint = " top " + getFetchSize();
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void doFetchPage() {
        if (isFetchSizeSupported()) {
            if (isFetchStartIndexSupported()) {
                _fetchFirstSelectHint = " top " + getFetchSize();
            } else {
                _fetchFirstSelectHint = " top " + getPageEndIndex();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void doClearFetchPageClause() {
        _fetchFirstSelectHint = "";
    }

    /**
     * @return The determination, true or false.
     */
    public boolean isFetchStartIndexSupported() {
        return false;
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
        return _fetchFirstSelectHint;
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
        return _lockSqlSuffix;
    }

    // [DBFlute-0.9.8.4]
    // ===================================================================================
    //                                                                               DBWay
    //                                                                               =====
    public DBWay dbway() {
        return DBDef.Sybase.dbway();
    }
}
