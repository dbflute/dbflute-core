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
import org.dbflute.bhv.writable.DeleteOption;
import org.dbflute.cbean.ConditionBean;
import org.dbflute.s2dao.sqlcommand.TnQueryDeleteDynamicCommand;

/**
 * @author jflute
 */
public class QueryDeleteCBCommand extends AbstractQueryUpdateCommand {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The option of delete. (NullAllowed) */
    protected DeleteOption<? extends ConditionBean> _deleteOption;

    // ===================================================================================
    //                                                                   Basic Information
    //                                                                   =================
    public String getCommandName() {
        return "queryDelete";
    }

    // ===================================================================================
    //                                                                  Detail Information
    //                                                                  ==================
    @Override
    public boolean isDelete() {
        return true;
    }

    // ===================================================================================
    //                                                               SqlExecution Handling
    //                                                               =====================

    @Override
    protected SqlExecution createQueryEntityCBExecution() {
        return newQueryDeleteDynamicCommand();
    }

    protected TnQueryDeleteDynamicCommand newQueryDeleteDynamicCommand() {
        return new TnQueryDeleteDynamicCommand(_dataSource, _statementFactory);
    }

    @Override
    protected Object[] doGetSqlExecutionArgument() {
        return new Object[] { _conditionBean, _deleteOption };
    }

    // ===================================================================================
    //                                                                       Assert Helper
    //                                                                       =============
    @Override
    protected void assertEntityProperty(String methodName) {
        // entity is not used here
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setDeleteOption(DeleteOption<? extends ConditionBean> deleteOption) {
        _deleteOption = deleteOption;
    }
}
