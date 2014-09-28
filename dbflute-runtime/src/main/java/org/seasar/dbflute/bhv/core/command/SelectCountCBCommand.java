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

import org.seasar.dbflute.bhv.core.SqlExecution;
import org.seasar.dbflute.bhv.core.SqlExecutionCreator;
import org.seasar.dbflute.cbean.ConditionBean;
import org.seasar.dbflute.cbean.ConditionBeanContext;
import org.seasar.dbflute.s2dao.jdbc.TnResultSetHandler;

/**
 * @author jflute
 */
public class SelectCountCBCommand extends AbstractSelectCBCommand<Integer> {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** Is it unique-count select? (NotNull) */
    protected Boolean _uniqueCount;

    // ===================================================================================
    //                                                                   Basic Information
    //                                                                   =================
    public String getCommandName() {
        return "selectCount";
    }

    public Class<?> getCommandReturnType() {
        return Integer.class;
    }

    // ===================================================================================
    //                                                                    Process Callback
    //                                                                    ================
    public void beforeGettingSqlExecution() {
        assertStatus("beforeGettingSqlExecution");
        final ConditionBean cb = _conditionBean;
        cb.xsetupSelectCountIgnoreFetchScope(_uniqueCount); // *Point!
        ConditionBeanContext.setConditionBeanOnThread(cb);
    }

    public void afterExecuting() {
        assertStatus("afterExecuting");
        final ConditionBean cb = _conditionBean;
        cb.xafterCareSelectCountIgnoreFetchScope();
    }

    // ===================================================================================
    //                                                                  Detail Information
    //                                                                  ==================
    public boolean isSelectCount() {
        return true;
    }

    public boolean isSelectCursor() {
        return false;
    }

    // ===================================================================================
    //                                                               SqlExecution Handling
    //                                                               =====================
    @Override
    public String buildSqlExecutionKey() {
        return super.buildSqlExecutionKey() + ":" + (_uniqueCount ? "unique" : "plain");
    }

    public SqlExecutionCreator createSqlExecutionCreator() {
        assertStatus("createSqlExecutionCreator");
        return new SqlExecutionCreator() {
            public SqlExecution createSqlExecution() {
                TnResultSetHandler handler = createScalarResultSetHandler(getCommandReturnType());
                return createSelectCBExecution(_conditionBean.getClass(), handler);
            }
        };
    }

    // ===================================================================================
    //                                                                       Assert Helper
    //                                                                       =============
    protected void assertStatus(String methodName) {
        super.assertStatus(methodName);
        if (_uniqueCount == null) {
            throw new IllegalStateException(buildAssertMessage("_uniqueCount", methodName));
        }
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setUniqueCount(boolean uniqueCount) {
        _uniqueCount = uniqueCount;
    }
}
