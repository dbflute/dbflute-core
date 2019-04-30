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

import org.dbflute.bhv.core.SqlExecution;
import org.dbflute.bhv.writable.InsertOption;
import org.dbflute.cbean.ConditionBean;
import org.dbflute.s2dao.sqlcommand.TnQueryInsertDynamicCommand;

/**
 * @author jflute
 */
public class QueryInsertCBCommand extends AbstractQueryUpdateCommand {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The instance of condition-bean for insert into. (NotNull) */
    protected ConditionBean _intoConditionBean;

    /** The option of insert. (NullAllowed) */
    protected InsertOption<? extends ConditionBean> _insertOption;

    // ===================================================================================
    //                                                                   Basic Information
    //                                                                   =================
    public String getCommandName() {
        return "queryInsert";
    }

    // ===================================================================================
    //                                                                  Detail Information
    //                                                                  ==================
    @Override
    public boolean isInsert() {
        return true;
    }

    // ===================================================================================
    //                                                               SqlExecution Handling
    //                                                               =====================
    @Override
    protected SqlExecution createQueryEntityCBExecution() {
        return newQueryInsertDynamicCommand();
    }

    protected TnQueryInsertDynamicCommand newQueryInsertDynamicCommand() {
        return new TnQueryInsertDynamicCommand(_dataSource, _statementFactory);
    }

    @Override
    protected Object[] doGetSqlExecutionArgument() {
        return new Object[] { _entity, _intoConditionBean, _conditionBean, _insertOption };
    }

    // ===================================================================================
    //                                                                       Assert Helper
    //                                                                       =============
    @Override
    protected void assertStatus(String methodName) {
        super.assertStatus(methodName);
        if (_intoConditionBean == null) {
            throw new IllegalStateException(buildAssertMessage("_intoConditionBean", methodName));
        }
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setIntoConditionBean(ConditionBean intoConditionBean) {
        _intoConditionBean = intoConditionBean;
    }

    public void setInsertOption(InsertOption<? extends ConditionBean> insertOption) {
        _insertOption = insertOption;
    }
}
