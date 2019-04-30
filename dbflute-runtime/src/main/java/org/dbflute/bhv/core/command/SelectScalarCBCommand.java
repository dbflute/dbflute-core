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

import org.dbflute.bhv.core.SqlExecutionCreator;
import org.dbflute.bhv.core.context.ConditionBeanContext;
import org.dbflute.cbean.ConditionBean;
import org.dbflute.cbean.sqlclause.clause.SelectClauseType;
import org.dbflute.s2dao.jdbc.TnResultSetHandler;
import org.dbflute.util.DfTypeUtil;

/**
 * @author jflute
 * @param <RESULT> The type of result.
 */
public class SelectScalarCBCommand<RESULT> extends AbstractSelectCBCommand<RESULT> {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The type of result. (NotNull) */
    protected Class<RESULT> _resultType;

    /** The type of select clause. (NotNull) */
    protected SelectClauseType _selectClauseType;

    // ===================================================================================
    //                                                                   Basic Information
    //                                                                   =================
    public String getCommandName() {
        assertStatus("getCommandName");
        final String resultTypeName = DfTypeUtil.toClassTitle(_resultType);
        final String scalarMethodName = _selectClauseType.toString().toLowerCase();
        return "scalarSelect(" + resultTypeName + ")." + scalarMethodName;
    }

    public Class<?> getCommandReturnType() {
        assertStatus("getCommandReturnType");
        return _resultType;
    }

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
        ConditionBeanContext.setConditionBeanOnThread(cb);
        cb.getSqlClause().classifySelectClauseType(_selectClauseType); // *Point!
    }

    public void afterExecuting() {
        assertStatus("afterExecuting");
        final ConditionBean cb = _conditionBean;
        cb.getSqlClause().rollbackSelectClauseType();
    }

    // ===================================================================================
    //                                                               SqlExecution Handling
    //                                                               =====================
    public SqlExecutionCreator createSqlExecutionCreator() {
        assertStatus("createSqlExecutionCreator");
        return () -> {
            final TnResultSetHandler handler = createScalarResultSetHandler(getCommandReturnType());
            return createSelectCBExecution(_conditionBean.getClass(), handler);
        };
    }

    // ===================================================================================
    //                                                                       Assert Helper
    //                                                                       =============
    @Override
    protected void assertStatus(String methodName) {
        super.assertStatus(methodName);
        if (_resultType == null) {
            throw new IllegalStateException(buildAssertMessage("_resultType", methodName));
        }
        if (_selectClauseType == null) {
            throw new IllegalStateException(buildAssertMessage("_selectClauseType", methodName));
        }
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setResultType(Class<RESULT> resultType) {
        _resultType = resultType;
    }

    public void setSelectClauseType(SelectClauseType selectClauseType) {
        _selectClauseType = selectClauseType;
    }
}
