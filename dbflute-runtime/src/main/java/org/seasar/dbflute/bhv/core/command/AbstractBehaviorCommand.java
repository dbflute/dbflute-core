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
package org.seasar.dbflute.bhv.core.command;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.seasar.dbflute.bhv.core.BehaviorCommand;
import org.seasar.dbflute.bhv.core.BehaviorCommandComponentSetup;
import org.seasar.dbflute.bhv.core.execution.OutsideSqlExecuteExecution;
import org.seasar.dbflute.jdbc.StatementFactory;
import org.seasar.dbflute.jdbc.ValueType;
import org.seasar.dbflute.resource.InternalMapContext;
import org.seasar.dbflute.resource.InternalMapContext.InvokePathProvider;
import org.seasar.dbflute.s2dao.extension.TnRelationRowCreatorExtension;
import org.seasar.dbflute.s2dao.extension.TnRelationRowOptionalHandler;
import org.seasar.dbflute.s2dao.extension.TnRowCreatorExtension;
import org.seasar.dbflute.s2dao.jdbc.TnResultSetHandler;
import org.seasar.dbflute.s2dao.metadata.TnBeanMetaData;
import org.seasar.dbflute.s2dao.metadata.TnBeanMetaDataFactory;
import org.seasar.dbflute.s2dao.rshandler.TnBeanCursorResultSetHandler;
import org.seasar.dbflute.s2dao.rshandler.TnBeanListResultSetHandler;
import org.seasar.dbflute.s2dao.rshandler.TnScalarDynamicResultSetHandler;
import org.seasar.dbflute.s2dao.rshandler.TnScalarListResultSetHandler;
import org.seasar.dbflute.s2dao.rshandler.TnScalarResultSetHandler;
import org.seasar.dbflute.s2dao.valuetype.TnValueTypes;
import org.seasar.dbflute.util.DfTypeUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 * @param <RESULT> The type of result.
 */
public abstract class AbstractBehaviorCommand<RESULT> implements BehaviorCommand<RESULT>, BehaviorCommandComponentSetup {

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
    protected DataSource _dataSource;
    protected StatementFactory _statementFactory;
    protected TnBeanMetaDataFactory _beanMetaDataFactory;
    protected String _sqlFileEncoding;

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
        final TnRowCreatorExtension rowCreator = createRowCreator(bmd);
        final TnRelationRowCreatorExtension relationRowCreator = createRelationRowCreator(bmd);
        return new TnBeanListResultSetHandler(bmd, rowCreator, relationRowCreator);
    }

    protected TnResultSetHandler createBeanCursorResultSetHandler(TnBeanMetaData bmd) {
        final TnRowCreatorExtension rowCreator = createRowCreator(bmd);
        final TnRelationRowCreatorExtension relationRowCreator = createRelationRowCreator(bmd);
        return new TnBeanCursorResultSetHandler(bmd, rowCreator, relationRowCreator);
    }

    protected TnResultSetHandler createScalarResultSetHandler(Class<?> objectType) {
        final ValueType valueType = TnValueTypes.getValueType(objectType);
        return new TnScalarResultSetHandler(valueType);
    }

    protected TnResultSetHandler createScalarListResultSetHandler(Class<?> objectType) {
        final ValueType valueType = TnValueTypes.getValueType(objectType);
        return createScalarListResultSetHandler(valueType);
    }

    protected TnResultSetHandler createDynamicScalarResultSetHandler(Class<?> objectType) {
        final ValueType valueType = TnValueTypes.getValueType(objectType);
        return new TnScalarDynamicResultSetHandler(valueType);
    }

    protected TnResultSetHandler createScalarListResultSetHandler(ValueType valueType) {
        return new TnScalarListResultSetHandler(valueType);
    }

    protected TnRowCreatorExtension createRowCreator(TnBeanMetaData bmd) {
        final Class<?> clazz = bmd != null ? bmd.getBeanClass() : null;
        return TnRowCreatorExtension.createRowCreator(clazz);
    }

    protected TnRelationRowCreatorExtension createRelationRowCreator(TnBeanMetaData bmd) {
        final TnRelationRowOptionalHandler optionalFactory = createRelationOptionalFactory();
        return TnRelationRowCreatorExtension.createRelationRowCreator(optionalFactory);
    }

    protected TnRelationRowOptionalHandler createRelationOptionalFactory() {
        return _beanMetaDataFactory.getRelationRowOptionalHandler();
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
    public void setDataSource(DataSource dataSource) {
        _dataSource = dataSource;
    }

    public void setStatementFactory(StatementFactory statementFactory) {
        _statementFactory = statementFactory;
    }

    public void setBeanMetaDataFactory(TnBeanMetaDataFactory beanMetaDataFactory) {
        _beanMetaDataFactory = beanMetaDataFactory;
    }

    public void setSqlFileEncoding(String sqlFileEncoding) {
        _sqlFileEncoding = sqlFileEncoding;
    }
}
