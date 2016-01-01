/*
 * Copyright 2014-2015 the original author or authors.
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

import org.dbflute.Entity;
import org.dbflute.bhv.core.SqlExecutionCreator;
import org.dbflute.bhv.core.context.ConditionBeanContext;
import org.dbflute.bhv.core.context.FetchAssistContext;
import org.dbflute.bhv.readable.EntityRowHandler;
import org.dbflute.cbean.ConditionBean;
import org.dbflute.s2dao.jdbc.TnResultSetHandler;
import org.dbflute.s2dao.metadata.TnBeanMetaData;
import org.dbflute.util.DfTypeUtil;

/**
 * @author jflute
 * @param <ENTITY> The type of entity.
 */
public class SelectCursorCBCommand<ENTITY extends Entity> extends AbstractSelectCBCommand<ENTITY> {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The type of entity. (NotNull) */
    protected Class<? extends ENTITY> _entityType; // generic 'extends' to specify extended type

    /** The handler of entity row. (NotNull) */
    protected EntityRowHandler<ENTITY> _entityRowHandler;

    // ===================================================================================
    //                                                                   Basic Information
    //                                                                   =================
    public String getCommandName() {
        return "selectCursor";
    }

    public Class<?> getCommandReturnType() {
        return Object.class;
    }

    // ===================================================================================
    //                                                                  Detail Information
    //                                                                  ==================
    public boolean isSelectCount() {
        return false;
    }

    public boolean isSelectCursor() {
        return true;
    }

    // ===================================================================================
    //                                                                    Process Callback
    //                                                                    ================
    public void beforeGettingSqlExecution() {
        assertStatus("beforeGettingSqlExecution");
        final ConditionBean cb = _conditionBean;
        FetchAssistContext.setFetchBeanOnThread(cb);
        ConditionBeanContext.setConditionBeanOnThread(cb);
        ConditionBeanContext.setEntityRowHandlerOnThread(_entityRowHandler);
    }

    public void afterExecuting() {
        assertStatus("afterExecuting");
    }

    // ===================================================================================
    //                                                               SqlExecution Handling
    //                                                               =====================
    @Override
    public String buildSqlExecutionKey() {
        // entity row handler uses name (not simple) because of no-name inner class
        final String handlerName = _entityRowHandler.getClass().getName();
        final String entityName = DfTypeUtil.toClassTitle(_entityType);
        return super.buildSqlExecutionKey() + ":" + handlerName + ":" + entityName;
    }

    public SqlExecutionCreator createSqlExecutionCreator() {
        assertStatus("createSqlExecutionCreator");
        return () -> {
            final TnBeanMetaData bmd = createBeanMetaData();
            final TnResultSetHandler handler = createBeanCursorResultSetHandler(bmd);
            return createSelectCBExecution(_conditionBean.getClass(), handler);
        };
    }

    protected TnBeanMetaData createBeanMetaData() {
        return _beanMetaDataFactory.createBeanMetaData(_entityType);
    }

    // ===================================================================================
    //                                                                       Assert Helper
    //                                                                       =============
    @Override
    protected void assertStatus(String methodName) {
        super.assertStatus(methodName);
        if (_entityType == null) {
            throw new IllegalStateException(buildAssertMessage("_entityType", methodName));
        }
        if (_entityRowHandler == null) {
            throw new IllegalStateException(buildAssertMessage("_cursorHandler", methodName));
        }
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setEntityType(Class<? extends ENTITY> entityType) {
        _entityType = entityType;
    }

    public void setEntityRowHandler(EntityRowHandler<ENTITY> entityRowHandler) {
        _entityRowHandler = entityRowHandler;
    }
}
