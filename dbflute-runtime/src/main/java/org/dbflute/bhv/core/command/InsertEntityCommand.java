/*
 * Copyright 2014-2022 the original author or authors.
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

import java.util.List;

import org.dbflute.bhv.core.SqlExecution;
import org.dbflute.bhv.core.SqlExecutionCreator;
import org.dbflute.bhv.writable.InsertOption;
import org.dbflute.cbean.ConditionBean;
import org.dbflute.dbmeta.DBMeta;
import org.dbflute.dbmeta.info.ColumnInfo;
import org.dbflute.s2dao.metadata.TnBeanMetaData;
import org.dbflute.s2dao.sqlcommand.TnInsertEntityDynamicCommand;

/**
 * @author jflute
 */
public class InsertEntityCommand extends AbstractEntityUpdateCommand {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The option of insert. (NotRequired) */
    protected InsertOption<? extends ConditionBean> _insertOption;

    // ===================================================================================
    //                                                                   Basic Information
    //                                                                   =================
    public String getCommandName() {
        return "insert";
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
        return () -> {
            final TnBeanMetaData bmd = createBeanMetaData();
            return createInsertEntitySqlExecution(bmd);
        };
    }

    protected SqlExecution createInsertEntitySqlExecution(TnBeanMetaData bmd) {
        final SqlExecution nonPrimaryKeySqlExecution = createNonPrimaryInsertSqlExecution(bmd);
        if (nonPrimaryKeySqlExecution != null) {
            return nonPrimaryKeySqlExecution;
        }
        final String[] propertyNames = getPersistentPropertyNames(bmd);
        return createInsertEntityDynamicCommand(bmd, propertyNames);
    }

    protected TnInsertEntityDynamicCommand createInsertEntityDynamicCommand(TnBeanMetaData bmd, String[] propertyNames) {
        final TnInsertEntityDynamicCommand cmd = newInsertEntityDynamicCommand();
        cmd.setBeanMetaData(bmd);
        cmd.setTargetDBMeta(findDBMeta());
        cmd.setPropertyNames(propertyNames);
        return cmd;
    }

    protected TnInsertEntityDynamicCommand newInsertEntityDynamicCommand() {
        return new TnInsertEntityDynamicCommand(_dataSource, _statementFactory);
    }

    /**
     * @param bmd The meta data of bean. (NotNull)
     * @return Whether the method is target. (For example if it has primary key, returns false.)
     */
    protected SqlExecution createNonPrimaryInsertSqlExecution(TnBeanMetaData bmd) {
        final DBMeta dbmeta = findDBMeta();
        if (dbmeta.hasPrimaryKey()) {
            return null;
        }
        final List<ColumnInfo> columnInfoList = dbmeta.getColumnInfoList();
        final StringBuilder columnDefSb = new StringBuilder();
        for (org.dbflute.dbmeta.info.ColumnInfo columnInfo : columnInfoList) {
            columnDefSb.append(", ").append(columnInfo.getColumnSqlName());
        }
        columnDefSb.delete(0, ", ".length()).insert(0, "(").append(")");
        final StringBuilder columnValuesSb = new StringBuilder();
        for (org.dbflute.dbmeta.info.ColumnInfo columnInfo : columnInfoList) {
            columnValuesSb.append(", /*pmb.").append(columnInfo.getPropertyName()).append("*/null");
        }
        columnValuesSb.delete(0, ", ".length()).insert(0, "(").append(")");
        final String sql = "insert into " + dbmeta.getTableSqlName() + columnDefSb + " values" + columnValuesSb;
        return createOutsideSqlExecuteExecution(_entity.getClass(), sql);
    }

    @Override
    protected Object[] doGetSqlExecutionArgument() {
        return new Object[] { _entity, _insertOption };
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setInsertOption(InsertOption<? extends ConditionBean> insertOption) {
        _insertOption = insertOption;
    }
}
