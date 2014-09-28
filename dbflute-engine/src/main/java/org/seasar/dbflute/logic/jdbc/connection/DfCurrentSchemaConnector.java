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
package org.seasar.dbflute.logic.jdbc.connection;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.engine.database.model.UnifiedSchema;
import org.seasar.dbflute.properties.facade.DfDatabaseTypeFacadeProp;
import org.seasar.dbflute.resource.DBFluteSystem;

/**
 * @author jflute
 * @since 0.9.4 (2009/03/03 Tuesday)
 */
public class DfCurrentSchemaConnector {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance. */
    private static final Log _log = LogFactory.getLog(DfCurrentSchemaConnector.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final UnifiedSchema _unifiedSchema;
    protected final DfDatabaseTypeFacadeProp _databaseTypeFacadeProp;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfCurrentSchemaConnector(UnifiedSchema unifiedSchema, DfDatabaseTypeFacadeProp databaseTypeFacadeProp) {
        _unifiedSchema = unifiedSchema;
        _databaseTypeFacadeProp = databaseTypeFacadeProp;
    }

    // ===================================================================================
    //                                                                                Main
    //                                                                                ====
    public void connectSchema(Connection conn) throws SQLException {
        if (!_unifiedSchema.existsPureSchema()) {
            return;
        }
        final String pureSchema = _unifiedSchema.getPureSchema();
        if (_databaseTypeFacadeProp.isDatabaseDB2()) {
            final String sql = "SET CURRENT SCHEMA = " + pureSchema;
            executeCurrentSchemaSql(conn, sql);
        } else if (_databaseTypeFacadeProp.isDatabaseOracle()) {
            final String sql = "ALTER SESSION SET CURRENT_SCHEMA = " + pureSchema;
            executeCurrentSchemaSql(conn, sql);
        } else if (_databaseTypeFacadeProp.isDatabasePostgreSQL()) {
            final String sql = "set search_path to " + pureSchema;
            executeCurrentSchemaSql(conn, sql);
        }
    }

    protected void executeCurrentSchemaSql(Connection conn, String sql) throws SQLException {
        final Statement st = conn.createStatement();
        try {
            _log.info("...Connecting to the schema");
            _log.info(sql);
            st.execute(sql);
        } catch (SQLException continued) { // continue because it's supplementary SQL
            String msg = "Failed to execute the SQL:" + ln() + sql + ln() + continued.getMessage();
            _log.warn(msg);
            return;
        } finally {
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String ln() {
        return DBFluteSystem.getBasicLn();
    }
}
