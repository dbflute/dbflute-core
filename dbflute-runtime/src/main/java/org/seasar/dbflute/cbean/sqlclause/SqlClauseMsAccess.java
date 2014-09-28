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
package org.seasar.dbflute.cbean.sqlclause;

import java.util.Map;

import org.seasar.dbflute.DBDef;
import org.seasar.dbflute.cbean.sqlclause.join.FixedConditionResolver;
import org.seasar.dbflute.cbean.sqlclause.orderby.OrderByClause;
import org.seasar.dbflute.dbmeta.info.ForeignInfo;
import org.seasar.dbflute.dbmeta.name.ColumnRealName;
import org.seasar.dbflute.dbway.DBWay;
import org.seasar.dbflute.exception.IllegalConditionBeanOperationException;

/**
 * SqlClause for MS Access.
 * @author jflute
 */
public class SqlClauseMsAccess extends AbstractSqlClause {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Serial version UID. (Default) */
    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * Constructor.
     * @param tableDbName The DB name of table. (NotNull)
     **/
    public SqlClauseMsAccess(String tableDbName) {
        super(tableDbName);
    }

    // ===================================================================================
    //                                                                       From Override
    //                                                                       =============
    @Override
    protected boolean isJoinInParentheses() {
        return true; // needs to join in parentheses at MS Access
    }

    // ===================================================================================
    //                                                                  OuterJoin Override
    //                                                                  ==================
    @Override
    public void registerOuterJoin(String foreignAliasName, String foreignTableDbName, String localAliasName,
            String localTableDbName, Map<ColumnRealName, ColumnRealName> joinOnMap, String relationPath,
            ForeignInfo foreignInfo, String fixedCondition, FixedConditionResolver fixedConditionResolver) {
        // MS-Access does not support additional conditions on OnClause
        // so switch it to in-line where clause
        registerOuterJoinFixedInline(foreignAliasName, foreignTableDbName, localAliasName, localTableDbName, joinOnMap,
                relationPath, foreignInfo, fixedCondition, fixedConditionResolver);
    }

    // ===================================================================================
    //                                                                    OrderBy Override
    //                                                                    ================
    @Override
    protected OrderByClause.OrderByNullsSetupper createOrderByNullsSetupper() {
        return createOrderByNullsSetupperByCaseWhen();
    }

    /**
     * {@inheritDoc}
     */
    protected void doFetchFirst() {
    }

    /**
     * {@inheritDoc}
     */
    protected void doFetchPage() {
    }

    /**
     * {@inheritDoc}
     */
    protected void doClearFetchPageClause() {
    }

    /**
     * {@inheritDoc}
     */
    public boolean isFetchStartIndexSupported() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isFetchSizeSupported() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void lockForUpdate() {
        String msg = "LockForUpdate-SQL is unavailable in the database. Sorry...: " + toString();
        throw new IllegalConditionBeanOperationException(msg);
    }

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
        return "";
    }

    // [DBFlute-0.9.8.4]
    // ===================================================================================
    //                                                                               DBWay
    //                                                                               =====
    public DBWay dbway() {
        return DBDef.MSAccess.dbway();
    }
}
