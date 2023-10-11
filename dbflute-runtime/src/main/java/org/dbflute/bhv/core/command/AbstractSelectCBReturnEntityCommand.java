/*
 * Copyright 2014-2023 the original author or authors.
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

import org.dbflute.bhv.core.SqlExecutionCreator;
import org.dbflute.bhv.core.context.ConditionBeanContext;
import org.dbflute.bhv.core.context.FetchAssistContext;
import org.dbflute.cbean.ConditionBean;
import org.dbflute.s2dao.jdbc.TnResultSetHandler;
import org.dbflute.s2dao.metadata.TnBeanMetaData;
import org.dbflute.util.DfTypeUtil;

/**
 * @author jflute
 * @param <RESULT> The type of result, e.g. entity, list.
 */
public abstract class AbstractSelectCBReturnEntityCommand<RESULT> extends AbstractSelectCBCommand<RESULT> {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The type of entity. (Required) */
    protected Class<?> _entityType; // generic 'extends' to specify extended type

    // ===================================================================================
    //                                                                  Detail Information
    //                                                                  ==================
    public boolean isSelectCount() {
        return false;
    }

    public boolean isSelectCursor() {
        return false;
    }

    // ===================================================================================
    //                                                                    Process Callback
    //                                                                    ================
    public void beforeGettingSqlExecution() {
        assertStatus("beforeGettingSqlExecution");
        final ConditionBean cb = _conditionBean;
        FetchAssistContext.setFetchBeanOnThread(cb);
        ConditionBeanContext.setConditionBeanOnThread(cb);
    }

    public void afterExecuting() {
        assertStatus("afterExecuting");
    }

    // ===================================================================================
    //                                                               SqlExecution Handling
    //                                                               =====================
    @Override
    public String buildSqlExecutionKey() {
        final String entityName = DfTypeUtil.toClassTitle(_entityType);
        return super.buildSqlExecutionKey() + ":" + entityName;
    }

    public SqlExecutionCreator createSqlExecutionCreator() {
        assertStatus("createSqlExecutionCreator");
        return () -> {
            final TnBeanMetaData bmd = createBeanMetaData();
            final TnResultSetHandler handler = createReturnEntityResultSetHandler(bmd);
            return createSelectCBExecution(_conditionBean.getClass(), handler);
        };
    }

    protected abstract TnResultSetHandler createReturnEntityResultSetHandler(TnBeanMetaData bmd);

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
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setEntityType(Class<?> entityType) {
        _entityType = entityType;
    }
}
