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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.dbflute.Entity;
import org.dbflute.bhv.core.execution.SelectCBExecution;
import org.dbflute.cbean.ConditionBean;
import org.dbflute.outsidesql.OutsideSqlOption;
import org.dbflute.s2dao.jdbc.TnResultSetHandler;
import org.dbflute.util.DfTypeUtil;

/**
 * @author jflute
 * @param <RESULT> The type of result.
 */
public abstract class AbstractSelectCBCommand<RESULT> extends AbstractAllBehaviorCommand<RESULT> {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The instance of condition-bean. (NotNull) */
    protected ConditionBean _conditionBean;

    // ===================================================================================
    //                                                                  Detail Information
    //                                                                  ==================
    public boolean isConditionBean() {
        return true;
    }

    public boolean isOutsideSql() {
        return false;
    }

    public boolean isProcedure() {
        return false;
    }

    public boolean isSelect() {
        return true;
    }

    public boolean isInsert() {
        return false;
    }

    public boolean isUpdate() {
        return false;
    }

    public boolean isDelete() {
        return false;
    }

    public boolean isEntityUpdateFamily() {
        return false;
    }

    public boolean isBatchUpdateFamily() {
        return false;
    }

    public boolean isQueryUpdateFamily() {
        return false;
    }

    // ===================================================================================
    //                                                               SqlExecution Handling
    //                                                               =====================
    public String buildSqlExecutionKey() {
        assertStatus("buildSqlExecutionKey");
        final String cbName = DfTypeUtil.toClassTitle(_conditionBean);
        return _tableDbName + ":" + getCommandName() + "(" + cbName + ")";
    }

    protected SelectCBExecution createSelectCBExecution(Class<? extends ConditionBean> cbType, TnResultSetHandler handler) {
        return newSelectCBExecution(createBeanArgNameTypeMap(cbType), handler);
    }

    protected SelectCBExecution newSelectCBExecution(Map<String, Class<?>> argNameTypeMap, TnResultSetHandler handler) {
        return new SelectCBExecution(_dataSource, _statementFactory, argNameTypeMap, handler);
    }

    public Object[] getSqlExecutionArgument() {
        assertStatus("getSqlExecutionArgument");
        return new Object[] { _conditionBean };
    }

    // ===================================================================================
    //                                                                Argument Information
    //                                                                ====================
    public ConditionBean getConditionBean() {
        return _conditionBean;
    }

    public Entity getEntity() {
        return null;
    }

    public List<Entity> getEntityList() {
        return Collections.emptyList();
    }

    public String getOutsideSqlPath() {
        return null;
    }

    public String getParameterBean() {
        return null;
    }

    public OutsideSqlOption getOutsideSqlOption() {
        return null;
    }

    // ===================================================================================
    //                                                                       Assert Helper
    //                                                                       =============
    protected void assertStatus(String methodName) {
        assertBasicProperty(methodName);
        assertComponentProperty(methodName);
        assertConditionBeanProperty(methodName);
    }

    protected void assertConditionBeanProperty(String methodName) {
        if (_conditionBean == null) {
            throw new IllegalStateException(buildAssertMessage("_conditionBean", methodName));
        }
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setConditionBean(ConditionBean conditionBean) {
        _conditionBean = conditionBean;
    }
}
