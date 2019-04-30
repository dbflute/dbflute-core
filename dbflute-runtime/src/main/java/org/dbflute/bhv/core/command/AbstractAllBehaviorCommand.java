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
package org.dbflute.bhv.core.command;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.dbflute.bhv.core.BehaviorCommand;
import org.dbflute.bhv.core.BehaviorCommandComponentSetup;
import org.dbflute.bhv.core.context.InternalMapContext;
import org.dbflute.bhv.core.context.InternalMapContext.InvokePathProvider;
import org.dbflute.bhv.core.execution.OutsideSqlExecuteExecution;
import org.dbflute.dbmeta.DBMeta;
import org.dbflute.dbmeta.DBMetaProvider;
import org.dbflute.jdbc.StatementFactory;
import org.dbflute.jdbc.ValueType;
import org.dbflute.s2dao.jdbc.TnResultSetHandler;
import org.dbflute.s2dao.jdbc.TnResultSetHandlerFactory;
import org.dbflute.s2dao.metadata.TnBeanMetaData;
import org.dbflute.s2dao.metadata.TnBeanMetaDataFactory;
import org.dbflute.util.DfTypeUtil;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @param <RESULT> The type of result.
 */
public abstract class AbstractAllBehaviorCommand<RESULT> implements BehaviorCommand<RESULT>, BehaviorCommandComponentSetup {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                     Basic Information
    //                                     -----------------
    /** The table DB name. (NotNull) */
    protected String _tableDbName;

    /** Is it initialize only? */
    protected boolean _initializeOnly;

    // -----------------------------------------------------
    //                                   Injection Component
    //                                   -------------------
    // these are not null
    protected DBMetaProvider _dbmetaProvider;
    protected DataSource _dataSource;
    protected StatementFactory _statementFactory;
    protected TnBeanMetaDataFactory _beanMetaDataFactory;
    protected TnResultSetHandlerFactory _resultSetHandlerFactory;
    protected String _sqlFileEncoding;

    // ===================================================================================
    //                                                                              DBMeta
    //                                                                              ======
    public String getProjectName() {
        return getDBMeta().getProjectName();
    }

    public DBMeta getDBMeta() {
        return _dbmetaProvider.provideDBMeta(_tableDbName);
    }

    // ===================================================================================
    //                                                                             Factory
    //                                                                             =======
    // -----------------------------------------------------
    //                            OutsideSqlExecuteExecution
    //                            --------------------------
    // the non-primary insert also uses this so defined here
    protected OutsideSqlExecuteExecution createOutsideSqlExecuteExecution(Object pmbTypeObj, String sql) {
        final Map<String, Class<?>> argNameTypeMap = createBeanArgNameTypeMap(pmbTypeObj);
        return newOutsideSqlExecuteExecution(argNameTypeMap, sql);
    }

    protected OutsideSqlExecuteExecution newOutsideSqlExecuteExecution(Map<String, Class<?>> argNameTypeMap, String sql) {
        return new OutsideSqlExecuteExecution(_dataSource, _statementFactory, argNameTypeMap, sql);
    }

    protected Map<String, Class<?>> createBeanArgNameTypeMap(Object pmbTypeObj) {
        final Map<String, Class<?>> argNameTypeMap = newArgNameTypeMap();
        if (pmbTypeObj == null) {
            return argNameTypeMap;
        }
        final Class<?> pmbType;
        if (pmbTypeObj instanceof Class<?>) {
            pmbType = (Class<?>) pmbTypeObj;
        } else {
            pmbType = pmbTypeObj.getClass();
        }
        argNameTypeMap.put("pmb", pmbType);
        return argNameTypeMap;
    }

    protected Map<String, Class<?>> newArgNameTypeMap() {
        return new LinkedHashMap<String, Class<?>>();
    }

    // -----------------------------------------------------
    //                                      ResultSetHandler
    //                                      ----------------
    protected TnResultSetHandler createBeanListResultSetHandler(TnBeanMetaData bmd) {
        return _resultSetHandlerFactory.createBeanListResultSetHandler(bmd, _beanMetaDataFactory.getRelationRowOptionalHandler());
    }

    protected TnResultSetHandler createBeanOneResultSetHandler(TnBeanMetaData bmd, Object searchKey) {
        return _resultSetHandlerFactory.createBeanOneResultSetHandler(bmd, _beanMetaDataFactory.getRelationRowOptionalHandler(), searchKey);
    }

    protected TnResultSetHandler createBeanCursorResultSetHandler(TnBeanMetaData bmd) {
        return _resultSetHandlerFactory.createBeanCursorResultSetHandler(bmd, _beanMetaDataFactory.getRelationRowOptionalHandler());
    }

    protected TnResultSetHandler createScalarResultSetHandler(Class<?> objectType) {
        return _resultSetHandlerFactory.createScalarResultSetHandler(objectType);
    }

    protected TnResultSetHandler createScalarListResultSetHandler(Class<?> objectType) {
        return _resultSetHandlerFactory.createScalarListResultSetHandler(objectType);
    }

    protected TnResultSetHandler createScalarListResultSetHandler(ValueType valueType) {
        return _resultSetHandlerFactory.createScalarListResultSetHandler(valueType);
    }

    protected TnResultSetHandler createDynamicScalarResultSetHandler(Class<?> objectType) {
        return _resultSetHandlerFactory.createDynamicScalarResultSetHandler(objectType);
    }

    // ===================================================================================
    //                                                                 Runtime Information
    //                                                                 ===================
    public String getInvokePath() {
        final InvokePathProvider provider = InternalMapContext.getInvokePathProvider();
        return provider != null ? provider.provide() : null;
    }

    // ===================================================================================
    //                                                                       Assert Helper
    //                                                                       =============
    protected void assertBasicProperty(String methodName) {
        if (_tableDbName == null) {
            throw new IllegalStateException(buildAssertMessage("_tableDbName", methodName));
        }
    }

    protected void assertComponentProperty(String methodName) {
        if (_dataSource == null) {
            throw new IllegalStateException(buildAssertMessage("_dataSource", methodName));
        }
        if (_statementFactory == null) {
            throw new IllegalStateException(buildAssertMessage("_statementFactory", methodName));
        }
        if (_beanMetaDataFactory == null) {
            throw new IllegalStateException(buildAssertMessage("_beanMetaDataFactory", methodName));
        }
        if (_resultSetHandlerFactory == null) {
            throw new IllegalStateException(buildAssertMessage("_resultSetHandlerFactory", methodName));
        }
        if (_sqlFileEncoding == null) {
            throw new IllegalStateException(buildAssertMessage("_sqlFileEncoding", methodName));
        }
    }

    protected String buildAssertMessage(String propertyName, String methodName) {
        String msg = "The property '" + Srl.ltrim(propertyName, "_") + "' should not be null";
        msg = msg + " when you call " + methodName + "().";
        throw new IllegalStateException(msg);
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return DfTypeUtil.toClassTitle(this) + ":{" + buildSqlExecutionKey() + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    // -----------------------------------------------------
    //                                     Basic Information
    //                                     -----------------
    public String getTableDbName() {
        return _tableDbName;
    }

    public void setTableDbName(String tableDbName) {
        _tableDbName = tableDbName;
    }

    public void setInitializeOnly(boolean initializeOnly) {
        _initializeOnly = initializeOnly;
    }

    public boolean isInitializeOnly() {
        return _initializeOnly;
    }

    // -----------------------------------------------------
    //                                   Injection Component
    //                                   -------------------
    @Override
    public void setDBMetaProvider(DBMetaProvider dbmetaProvider) {
        _dbmetaProvider = dbmetaProvider;
    }

    public void setDataSource(DataSource dataSource) {
        _dataSource = dataSource;
    }

    public void setStatementFactory(StatementFactory statementFactory) {
        _statementFactory = statementFactory;
    }

    public void setBeanMetaDataFactory(TnBeanMetaDataFactory beanMetaDataFactory) {
        _beanMetaDataFactory = beanMetaDataFactory;
    }

    public void setResultSetHandlerFactory(TnResultSetHandlerFactory resultSetHandlerFactory) {
        _resultSetHandlerFactory = resultSetHandlerFactory;
    }

    public void setSqlFileEncoding(String sqlFileEncoding) {
        _sqlFileEncoding = sqlFileEncoding;
    }
}
