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
import org.dbflute.bhv.core.SqlExecutionCreator;
import org.dbflute.bhv.writable.DeleteOption;
import org.dbflute.cbean.ConditionBean;
import org.dbflute.s2dao.metadata.TnBeanMetaData;
import org.dbflute.s2dao.sqlcommand.TnDeleteEntityDynamicCommand;

/**
 * @author jflute
 */
public class DeleteEntityCommand extends AbstractEntityUpdateCommand {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The option of delete. (NotRequired) */
    protected DeleteOption<? extends ConditionBean> _deleteOption;

    // ===================================================================================
    //                                                                   Basic Information
    //                                                                   =================
    public String getCommandName() {
        return "delete";
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
    public String buildSqlExecutionKey() {
        // no special unique key for options
        return super.buildSqlExecutionKey();
    }

    public SqlExecutionCreator createSqlExecutionCreator() {
        assertStatus("createSqlExecutionCreator");
        return () -> {
            final TnBeanMetaData bmd = createBeanMetaData();
            return createDeleteEntitySqlExecution(bmd);
        };
    }

    protected SqlExecution createDeleteEntitySqlExecution(TnBeanMetaData bmd) {
        final String[] propertyNames = getPersistentPropertyNames(bmd);
        return createDeleteEntityDynamicCommand(bmd, propertyNames);
    }

    protected TnDeleteEntityDynamicCommand createDeleteEntityDynamicCommand(TnBeanMetaData bmd, String[] propertyNames) {
        final TnDeleteEntityDynamicCommand cmd = newDeleteEntityDynamicCommand();
        cmd.setBeanMetaData(bmd);
        cmd.setTargetDBMeta(findDBMeta());
        cmd.setPropertyNames(propertyNames);
        cmd.setOptimisticLockHandling(isOptimisticLockHandling());
        return cmd;
    }

    protected TnDeleteEntityDynamicCommand newDeleteEntityDynamicCommand() {
        return new TnDeleteEntityDynamicCommand(_dataSource, _statementFactory);
    }

    protected boolean isOptimisticLockHandling() {
        return true;
    }

    @Override
    protected Object[] doGetSqlExecutionArgument() {
        return new Object[] { _entity, _deleteOption };
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setDeleteOption(DeleteOption<? extends ConditionBean> deleteOption) {
        _deleteOption = deleteOption;
    }
}
