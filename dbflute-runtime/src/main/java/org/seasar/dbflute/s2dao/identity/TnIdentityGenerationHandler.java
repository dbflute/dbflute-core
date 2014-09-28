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
package org.seasar.dbflute.s2dao.identity;

import javax.sql.DataSource;

import org.seasar.dbflute.DBDef;
import org.seasar.dbflute.dbmeta.DBMeta;
import org.seasar.dbflute.dbway.WayOfSQLServer;
import org.seasar.dbflute.dbway.WayOfSybase;
import org.seasar.dbflute.jdbc.StatementFactory;
import org.seasar.dbflute.resource.ResourceContext;

/**
 * @author modified by jflute (originated in S2Dao)
 */
public class TnIdentityGenerationHandler {

    public void disableIdentityGeneration(String tableDbName, DataSource dataSource, StatementFactory statementFactory) {
        if (isDatabaseSQLServer()) {
            final String tableSqlName = findDBMeta(tableDbName).getTableSqlName().toString();
            final String disableSql = getWayOfSQLServer().buildIdentityDisableSql(tableSqlName);
            doExecuteIdentityAdjustment(disableSql, dataSource, statementFactory);
        } else if (isDatabaseSybase()) {
            final String tableSqlName = findDBMeta(tableDbName).getTableSqlName().toString();
            final String disableSql = getWayOfSybase().buildIdentityDisableSql(tableSqlName);
            doExecuteIdentityAdjustment(disableSql, dataSource, statementFactory);
        }
    }

    public void enableIdentityGeneration(String tableDbName, DataSource dataSource, StatementFactory statementFactory) {
        if (isDatabaseSQLServer()) {
            final String tableSqlName = findDBMeta(tableDbName).getTableSqlName().toString();
            final String enableSql = getWayOfSQLServer().buildIdentityEnableSql(tableSqlName);
            doExecuteIdentityAdjustment(enableSql, dataSource, statementFactory);
        } else if (isDatabaseSybase()) {
            final String tableSqlName = findDBMeta(tableDbName).getTableSqlName().toString();
            final String enableSql = getWayOfSybase().buildIdentityEnableSql(tableSqlName);
            doExecuteIdentityAdjustment(enableSql, dataSource, statementFactory);
        }
    }

    protected DBMeta findDBMeta(String tableDbName) {
        return ResourceContext.dbmetaProvider().provideDBMeta(tableDbName);
    }

    protected boolean isDatabaseSQLServer() {
        return ResourceContext.isCurrentDBDef(DBDef.SQLServer);
    }

    protected boolean isDatabaseSybase() {
        return ResourceContext.isCurrentDBDef(DBDef.Sybase);
    }

    protected WayOfSQLServer getWayOfSQLServer() {
        return (WayOfSQLServer) ResourceContext.currentDBDef().dbway();
    }

    protected WayOfSybase getWayOfSybase() {
        return (WayOfSybase) ResourceContext.currentDBDef().dbway();
    }

    protected void doExecuteIdentityAdjustment(String sql, DataSource dataSource, StatementFactory statementFactory) {
        final TnIdentityAdjustmentSqlHandler handler = createIdentityAdjustmentSqlHandler(sql, dataSource,
                statementFactory);
        handler.execute(new Object[] {}); // SQL for identity adjustment does not have a bind-variable
    }

    protected TnIdentityAdjustmentSqlHandler createIdentityAdjustmentSqlHandler(String sql, DataSource dataSource,
            StatementFactory statementFactory) {
        return new TnIdentityAdjustmentSqlHandler(dataSource, statementFactory, sql);
    }
}
