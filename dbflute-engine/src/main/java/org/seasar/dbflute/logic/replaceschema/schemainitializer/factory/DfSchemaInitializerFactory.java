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
package org.seasar.dbflute.logic.replaceschema.schemainitializer.factory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.torque.engine.database.model.UnifiedSchema;
import org.seasar.dbflute.helper.jdbc.connection.DfCushionDataSource;
import org.seasar.dbflute.logic.replaceschema.schemainitializer.DfSchemaInitializer;
import org.seasar.dbflute.logic.replaceschema.schemainitializer.DfSchemaInitializerDB2;
import org.seasar.dbflute.logic.replaceschema.schemainitializer.DfSchemaInitializerFirebird;
import org.seasar.dbflute.logic.replaceschema.schemainitializer.DfSchemaInitializerH2;
import org.seasar.dbflute.logic.replaceschema.schemainitializer.DfSchemaInitializerJdbc;
import org.seasar.dbflute.logic.replaceschema.schemainitializer.DfSchemaInitializerMySQL;
import org.seasar.dbflute.logic.replaceschema.schemainitializer.DfSchemaInitializerOracle;
import org.seasar.dbflute.logic.replaceschema.schemainitializer.DfSchemaInitializerPostgreSQL;
import org.seasar.dbflute.logic.replaceschema.schemainitializer.DfSchemaInitializerSQLServer;
import org.seasar.dbflute.properties.DfDatabaseProperties;
import org.seasar.dbflute.properties.DfReplaceSchemaProperties;
import org.seasar.dbflute.properties.facade.DfDatabaseTypeFacadeProp;

/**
 * @author jflute
 */
public class DfSchemaInitializerFactory {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected DataSource _dataSource;
    protected DfDatabaseTypeFacadeProp _databaseTypeFacadeProp;
    protected DfDatabaseProperties _databaseProperties;
    protected DfReplaceSchemaProperties _replaceSchemaProperties;
    protected InitializeType _initializeType;
    protected Map<String, Object> _additionalDropMap;

    public enum InitializeType {
        MAIN, ADDTIONAL
    }

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfSchemaInitializerFactory(DataSource dataSource, DfDatabaseTypeFacadeProp databaseTypeFacadeProp,
            DfDatabaseProperties databaseProperties, DfReplaceSchemaProperties replaceSchemaProperties,
            InitializeType initializeType) {
        _dataSource = dataSource;
        _databaseTypeFacadeProp = databaseTypeFacadeProp;
        _databaseProperties = databaseProperties;
        _replaceSchemaProperties = replaceSchemaProperties;
        _initializeType = initializeType;
    }

    // ===================================================================================
    //                                                                              Create
    //                                                                              ======
    public DfSchemaInitializer createSchemaInitializer() {
        final DfSchemaInitializer initializer;
        if (_databaseTypeFacadeProp.isDatabaseMySQL()) {
            initializer = createSchemaInitializerMySQL();
        } else if (_databaseTypeFacadeProp.isDatabasePostgreSQL()) {
            initializer = createSchemaInitializerPostgreSQL();
        } else if (_databaseTypeFacadeProp.isDatabaseOracle()) {
            initializer = createSchemaInitializerOracle();
        } else if (_databaseTypeFacadeProp.isDatabaseDB2()) {
            initializer = createSchemaInitializerDB2();
        } else if (_databaseTypeFacadeProp.isDatabaseSQLServer()) {
            initializer = createSchemaInitializerSqlServer();
        } else if (_databaseTypeFacadeProp.isDatabaseH2()) {
            initializer = createSchemaInitializerH2();
        } else if (_databaseTypeFacadeProp.isDatabaseFirebird()) {
            initializer = createSchemaInitializerFirebird();
        } else {
            initializer = createSchemaInitializerJdbc();
        }
        return initializer;
    }

    protected DfSchemaInitializer createSchemaInitializerJdbc() {
        final DfSchemaInitializerJdbc initializer = new DfSchemaInitializerJdbc();
        setupSchemaInitializerJdbcProperties(initializer);
        return initializer;
    }

    protected DfSchemaInitializer createSchemaInitializerPostgreSQL() {
        final DfSchemaInitializerPostgreSQL initializer = new DfSchemaInitializerPostgreSQL();
        setupSchemaInitializerJdbcProperties(initializer);
        return initializer;
    }

    protected DfSchemaInitializer createSchemaInitializerOracle() {
        final DfSchemaInitializerOracle initializer = new DfSchemaInitializerOracle();
        setupSchemaInitializerJdbcProperties(initializer);
        return initializer;
    }

    protected DfSchemaInitializer createSchemaInitializerDB2() {
        final DfSchemaInitializerDB2 initializer = new DfSchemaInitializerDB2();
        setupSchemaInitializerJdbcProperties(initializer);
        return initializer;
    }

    protected DfSchemaInitializer createSchemaInitializerMySQL() {
        final DfSchemaInitializerMySQL initializer = new DfSchemaInitializerMySQL();
        setupSchemaInitializerJdbcProperties(initializer);
        return initializer;
    }

    protected DfSchemaInitializer createSchemaInitializerSqlServer() {
        final DfSchemaInitializerSQLServer initializer = new DfSchemaInitializerSQLServer();
        setupSchemaInitializerJdbcProperties(initializer);
        return initializer;
    }

    protected DfSchemaInitializer createSchemaInitializerH2() {
        final DfSchemaInitializerH2 initializer = new DfSchemaInitializerH2();
        setupSchemaInitializerJdbcProperties(initializer);
        return initializer;
    }

    protected DfSchemaInitializer createSchemaInitializerFirebird() {
        final DfSchemaInitializerFirebird initializer = new DfSchemaInitializerFirebird();
        setupSchemaInitializerJdbcProperties(initializer);
        return initializer;
    }

    protected void setupSchemaInitializerJdbcProperties(DfSchemaInitializerJdbc initializer) {
        setupDetailExecutionHandling(initializer);

        if (_initializeType.equals(InitializeType.MAIN)) {
            initializer.setDataSource(_dataSource);
            initializer.setUnifiedSchema(_databaseProperties.getDatabaseSchema());
            initializer.setDropObjectTypeList(_replaceSchemaProperties.getObjectTypeTargetList());
            initializer.setInitializeFirstSqlList(_replaceSchemaProperties.getInitializeFirstSqlList());
            initializer.setDropTableExceptList(_replaceSchemaProperties.getDropTableExceptList());
            initializer.setDropSequenceExceptList(_replaceSchemaProperties.getDropSequenceExceptList());
            initializer.setDropProcedureExceptList(_replaceSchemaProperties.getDropProcedureExceptList());
            return;
        }

        if (_initializeType.equals(InitializeType.ADDTIONAL)) {
            // Here 'Additional'!
            if (_additionalDropMap == null) {
                String msg = "The additional drop map should exist if the initialize type is additional!";
                throw new IllegalStateException(msg);
            }
            initializer.setDataSource(getAdditionalDataSource());
            final UnifiedSchema unifiedSchema = getAdditionalDropSchema(_additionalDropMap);
            initializer.setUnifiedSchema(unifiedSchema);
            initializer.setDropObjectTypeList(getAdditionalDropObjectTypeList(_additionalDropMap));
            // unsupported for additional
            //initializer.setInitializeFirstSqlList(null);
            //initializer.setDropTableExceptList(null);
            //initializer.setDropSequenceExceptList(null);
            //initializer.setDropProcedureExceptList(null);
            return;
        }

        String msg = "Unknown initialize type: " + _initializeType;
        throw new IllegalStateException(msg);
    }

    protected DataSource getAdditionalDataSource() {
        return new DfCushionDataSource() {
            public Connection getConnection() throws SQLException {
                return _replaceSchemaProperties.createAdditionalDropConnection(_additionalDropMap);
            }
        };
    }

    protected void setupDetailExecutionHandling(DfSchemaInitializerJdbc initializer) {
        initializer.setSuppressTruncateTable(_replaceSchemaProperties.isSuppressTruncateTable());
        initializer.setSuppressDropForeignKey(_replaceSchemaProperties.isSuppressDropForeignKey());
        initializer.setSuppressDropTable(_replaceSchemaProperties.isSuppressDropTable());
        initializer.setSuppressDropSequence(_replaceSchemaProperties.isSuppressDropSequence());
        initializer.setSuppressDropProcedure(_replaceSchemaProperties.isSuppressDropProcedure());
        initializer.setSuppressDropDBLink(_replaceSchemaProperties.isSuppressDropDBLink());
        initializer.setSuppressLoggingSql(_replaceSchemaProperties.isSuppressLoggingReplaceSql());
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    protected UnifiedSchema getAdditionalDropSchema(Map<String, Object> map) {
        return _replaceSchemaProperties.getAdditionalDropSchema(map);
    }

    protected List<String> getAdditionalDropObjectTypeList(Map<String, Object> map) {
        return _replaceSchemaProperties.getAdditionalDropObjectTypeList(map);
    }

    public Map<String, Object> getAdditionalDropMap() {
        return _additionalDropMap;
    }

    public void setAdditionalDropMap(Map<String, Object> additionalDropMap) {
        this._additionalDropMap = additionalDropMap;
    }
}
