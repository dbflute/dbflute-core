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

import org.seasar.dbflute.bhv.UpdateOption;
import org.seasar.dbflute.bhv.core.SqlExecution;
import org.seasar.dbflute.bhv.core.SqlExecutionCreator;
import org.seasar.dbflute.cbean.ConditionBean;
import org.seasar.dbflute.s2dao.metadata.TnBeanMetaData;
import org.seasar.dbflute.s2dao.sqlcommand.TnUpdateEntityDynamicCommand;

/**
 * @author jflute
 */
public class UpdateEntityCommand extends AbstractEntityCommand {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The option of update. (NotRequired) */
    protected UpdateOption<? extends ConditionBean> _updateOption;

    // ===================================================================================
    //                                                                   Basic Information
    //                                                                   =================
    public String getCommandName() {
        return "update";
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
        return new SqlExecutionCreator() {
            public SqlExecution createSqlExecution() {
                final TnBeanMetaData bmd = createBeanMetaData();
                return createUpdateEntitySqlExecution(bmd);
            }
        };
    }

    protected SqlExecution createUpdateEntitySqlExecution(TnBeanMetaData bmd) {
        final String[] propertyNames = getPersistentPropertyNames(bmd);
        return createUpdateEntityDynamicCommand(bmd, propertyNames);
    }

    protected TnUpdateEntityDynamicCommand createUpdateEntityDynamicCommand(TnBeanMetaData bmd, String[] propertyNames) {
        final TnUpdateEntityDynamicCommand cmd = newUpdateEntityDynamicCommand();
        cmd.setBeanMetaData(bmd);
        cmd.setTargetDBMeta(findDBMeta());
        cmd.setPropertyNames(propertyNames);
        cmd.setOptimisticLockHandling(isOptimisticLockHandling());
        cmd.setVersionNoAutoIncrementOnMemory(isVersionNoAutoIncrementOnMemory());
        return cmd;
    }

    protected TnUpdateEntityDynamicCommand newUpdateEntityDynamicCommand() {
        return new TnUpdateEntityDynamicCommand(_dataSource, _statementFactory);
    }

    protected boolean isOptimisticLockHandling() {
        return true;
    }

    protected boolean isVersionNoAutoIncrementOnMemory() {
        return isOptimisticLockHandling();
    }

    @Override
    protected Object[] doGetSqlExecutionArgument() {
        return new Object[] { _entity, _updateOption };
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setUpdateOption(UpdateOption<? extends ConditionBean> updateOption) {
        _updateOption = updateOption;
    }
}
