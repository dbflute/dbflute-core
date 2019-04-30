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

import java.util.Map;

import org.dbflute.cbean.sqlclause.join.FixedConditionResolver;
import org.dbflute.cbean.sqlclause.orderby.OrderByClause;
import org.dbflute.dbmeta.info.ForeignInfo;
import org.dbflute.dbmeta.name.ColumnRealName;
import org.dbflute.dbway.DBDef;
import org.dbflute.dbway.DBWay;
import org.dbflute.exception.IllegalConditionBeanOperationException;

/**
 * SqlClause for MS Access.
 * @author jflute
 */
public class SqlClauseMsAccess extends AbstractSqlClause {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The serial version UID for object serialization. (Default) */
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
    public void registerOuterJoin(String foreignAliasName, String foreignTableDbName, String localAliasName, String localTableDbName,
            Map<ColumnRealName, ColumnRealName> joinOnMap, String relationPath, ForeignInfo foreignInfo, String fixedCondition,
            FixedConditionResolver fixedConditionResolver) {
        // MS-Access does not support additional conditions on OnClause
        // so switch it to in-line where clause
        registerOuterJoinFixedInline(foreignAliasName, foreignTableDbName, localAliasName, localTableDbName, joinOnMap, relationPath,
                foreignInfo, fixedCondition, fixedConditionResolver);
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
