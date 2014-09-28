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
package org.seasar.dbflute.logic.replaceschema.loaddata.interceptor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.dbway.WayOfSQLServer;
import org.seasar.dbflute.helper.StringSet;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfColumnMeta;
import org.seasar.dbflute.properties.DfLittleAdjustmentProperties;

/**
 * @author jflute
 */
public class DfDataWritingInterceptorSQLServer implements DfDataWritingInterceptor {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance. */
    private static final Log _log = LogFactory.getLog(DfDataWritingInterceptorSQLServer.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DataSource _dataSource;
    protected boolean _loggingSql;
    protected final Set<String> _identityTableSet = StringSet.createAsFlexible();

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfDataWritingInterceptorSQLServer(DataSource dataSource, boolean loggingSql) {
        _dataSource = dataSource;
        _loggingSql = loggingSql;
    }

    // ===================================================================================
    //                                                                      Implementation
    //                                                                      ==============
    public void processBeforeHandlingTable(String tableDbName, Map<String, DfColumnMeta> columnInfoMap) {
        final String tableSqlName = quoteTableNameIfNeeds(tableDbName);
        if (hasIdentityColumn(_dataSource, tableSqlName, columnInfoMap)) {
            turnOnIdentityInsert(_dataSource, tableSqlName);
            _identityTableSet.add(tableSqlName);
        }
    }

    public void processFinallyHandlingTable(String tableDbName, Map<String, DfColumnMeta> columnInfoMap) {
        final String tableSqlName = quoteTableNameIfNeeds(tableDbName);
        if (_identityTableSet.contains(tableSqlName)) {
            turnOffIdentityInsert(_dataSource, tableSqlName);
        }
    }

    protected String quoteTableNameIfNeeds(String tableDbName) {
        final DfLittleAdjustmentProperties prop = DfBuildProperties.getInstance().getLittleAdjustmentProperties();
        return prop.quoteTableNameIfNeedsDirectUse(tableDbName);
    }

    // ===================================================================================
    //                                                                            Identity
    //                                                                            ========
    protected boolean hasIdentityColumn(DataSource dataSource, String tableSqlName,
            Map<String, DfColumnMeta> columnInfoMap) {
        final String sql = "select ident_current ('" + tableSqlName + "') as IDENT_CURRENT";
        final Connection conn = getConnection(dataSource);
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                final Object value = rs.getObject(1);
                return value != null;
            }
            return true;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignored) {
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ignored) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    protected void turnOnIdentityInsert(DataSource dataSource, String tableSqlName) {
        setIdentityInsert(dataSource, tableSqlName, true);
    }

    protected void turnOffIdentityInsert(DataSource dataSource, String tableSqlName) {
        setIdentityInsert(dataSource, tableSqlName, false);
    }

    protected void setIdentityInsert(DataSource dataSource, String tableSqlName, boolean insertOn) {
        final String sql = buildIdentityInsertSettingSql(tableSqlName, insertOn);
        if (_loggingSql) {
            _log.info(sql);
        }
        Connection conn = null;
        try {
            conn = getConnection(dataSource);
            final Statement stmt = createStatement(conn);
            try {
                stmt.execute(sql);
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            } finally {
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (SQLException ignored) {
                    }
                }
            }
        } finally {
            close(conn);
        }
    }

    protected String buildIdentityInsertSettingSql(String tableSqlName, boolean insertOn) {
        final WayOfSQLServer wayOfSQLServer = new WayOfSQLServer();
        if (insertOn) {
            return wayOfSQLServer.buildIdentityDisableSql(tableSqlName);
        } else {
            return wayOfSQLServer.buildIdentityEnableSql(tableSqlName);
        }
    }

    protected static Connection getConnection(DataSource dataSource) {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    protected static Statement createStatement(Connection conn) {
        try {
            return conn.createStatement();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    protected static void close(Connection conn) {
        if (conn == null)
            return;
        try {
            conn.close();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public boolean isLoggingInsertSql() {
        return _loggingSql;
    }

    public void setLoggingInsertSql(boolean loggingSql) {
        this._loggingSql = loggingSql;
    }
}
