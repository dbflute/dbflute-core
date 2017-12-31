/*
 * Copyright 2014-2018 the original author or authors.
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
import org.dbflute.bhv.core.SqlExecutionCreator;
import org.dbflute.bhv.writable.UpdateOption;
import org.dbflute.cbean.ConditionBean;
import org.dbflute.s2dao.metadata.TnBeanMetaData;
import org.dbflute.s2dao.sqlcommand.TnBatchUpdateDynamicCommand;

/**
 * @author jflute
 */
public class BatchUpdateCommand extends AbstractBatchUpdateCommand {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The option of update. (NotRequired) */
    protected UpdateOption<? extends ConditionBean> _updateOption;

    // ===================================================================================
    //                                                                   Basic Information
    //                                                                   =================
    public String getCommandName() {
        return "batchUpdate";
    }

    // ===================================================================================
    //                                                                  Detail Information
    //                                                                  ==================
    @Override
    public boolean isUpdate() {
        return true;
    }

    // ===================================================================================
    //                                                               SqlExecution Handling
    //                                                               =====================
    public SqlExecutionCreator createSqlExecutionCreator() {
        assertStatus("createSqlExecutionCreator");
        return () -> {
            final TnBeanMetaData bmd = createBeanMetaData();
            return createBatchUpdateEntitySqlExecution(bmd);
        };
    }

    protected SqlExecution createBatchUpdateEntitySqlExecution(TnBeanMetaData bmd) {
        final String[] propertyNames = getPersistentPropertyNames(bmd);
        return createBatchUpdateDynamicCommand(bmd, propertyNames);
    }

    protected TnBatchUpdateDynamicCommand createBatchUpdateDynamicCommand(TnBeanMetaData bmd, String[] propertyNames) {
        final TnBatchUpdateDynamicCommand cmd = newBatchUpdateDynamicCommand();
        cmd.setBeanMetaData(bmd);
        cmd.setTargetDBMeta(findDBMeta());
        cmd.setPropertyNames(propertyNames);
        cmd.setOptimisticLockHandling(isOptimisticLockHandling());
        cmd.setVersionNoAutoIncrementOnMemory(isVersionNoAutoIncrementOnMemory());
        return cmd;
    }

    protected TnBatchUpdateDynamicCommand newBatchUpdateDynamicCommand() {
        return new TnBatchUpdateDynamicCommand(_dataSource, _statementFactory);
    }

    protected boolean isOptimisticLockHandling() {
        return true;
    }

    protected boolean isVersionNoAutoIncrementOnMemory() {
        return isOptimisticLockHandling();
    }

    @Override
    protected Object[] doGetSqlExecutionArgument() {
        return new Object[] { _entityList, _updateOption };
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setUpdateOption(UpdateOption<? extends ConditionBean> updateOption) {
        _updateOption = updateOption;
    }
}
