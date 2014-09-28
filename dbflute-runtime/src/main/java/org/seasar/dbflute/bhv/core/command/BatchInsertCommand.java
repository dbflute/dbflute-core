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

import org.seasar.dbflute.bhv.InsertOption;
import org.seasar.dbflute.bhv.core.SqlExecution;
import org.seasar.dbflute.bhv.core.SqlExecutionCreator;
import org.seasar.dbflute.cbean.ConditionBean;
import org.seasar.dbflute.s2dao.metadata.TnBeanMetaData;
import org.seasar.dbflute.s2dao.sqlcommand.TnBatchInsertDynamicCommand;
import org.seasar.dbflute.s2dao.sqlcommand.TnInsertEntityDynamicCommand;

/**
 * @author jflute
 */
public class BatchInsertCommand extends AbstractListEntityCommand {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The option of insert. (NotRequired) */
    protected InsertOption<? extends ConditionBean> _insertOption;

    // ===================================================================================
    //                                                                   Basic Information
    //                                                                   =================
    public String getCommandName() {
        return "batchInsert";
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
    public SqlExecutionCreator createSqlExecutionCreator() {
        assertStatus("createSqlExecutionCreator");
        return new SqlExecutionCreator() {
            public SqlExecution createSqlExecution() {
                final TnBeanMetaData bmd = createBeanMetaData();
                return createBatchInsertSqlExecution(bmd);
            }
        };
    }

    protected SqlExecution createBatchInsertSqlExecution(TnBeanMetaData bmd) {
        final String[] propertyNames = getPersistentPropertyNames(bmd);
        return createBatchInsertDynamicCommand(bmd, propertyNames);
    }

    protected TnInsertEntityDynamicCommand createBatchInsertDynamicCommand(TnBeanMetaData bmd, String[] propertyNames) {
        final TnBatchInsertDynamicCommand cmd = newBatchInsertDynamicCommand();
        cmd.setBeanMetaData(bmd);
        cmd.setTargetDBMeta(findDBMeta());
        cmd.setPropertyNames(propertyNames);
        return cmd;
    }

    protected TnBatchInsertDynamicCommand newBatchInsertDynamicCommand() {
        return new TnBatchInsertDynamicCommand(_dataSource, _statementFactory);
    }

    @Override
    protected Object[] doGetSqlExecutionArgument() {
        return new Object[] { _entityList, _insertOption };
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setInsertOption(InsertOption<? extends ConditionBean> insertOption) {
        _insertOption = insertOption;
    }
}
