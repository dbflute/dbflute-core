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
package org.seasar.dbflute.s2dao.sqlcommand;

import java.util.Set;

import javax.sql.DataSource;

import org.seasar.dbflute.bhv.DeleteOption;
import org.seasar.dbflute.cbean.ConditionBean;
import org.seasar.dbflute.jdbc.StatementConfig;
import org.seasar.dbflute.jdbc.StatementFactory;
import org.seasar.dbflute.resource.InternalMapContext;
import org.seasar.dbflute.s2dao.sqlhandler.TnDeleteEntityHandler;

/**
 * @author modified by jflute (originated in S2Dao)
 */
public class TnDeleteEntityDynamicCommand extends TnAbstractEntityDynamicCommand {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected boolean _optimisticLockHandling;
    protected boolean _versionNoAutoIncrementOnMemory;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public TnDeleteEntityDynamicCommand(DataSource dataSource, StatementFactory statementFactory) {
        super(dataSource, statementFactory);
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    public Object execute(Object[] args) {
        final Object bean = extractBeanFromArgsChecked(args);
        final DeleteOption<ConditionBean> option = extractDeleteOption(args);
        prepareStatementConfigOnThreadIfExists(option);

        final String sql = filterExecutedSql(createDeleteSql(bean, option));
        return doExecute(bean, sql, option);
    }

    protected DeleteOption<ConditionBean> extractDeleteOption(Object[] args) {
        if (args.length < 2 || args[1] == null) {
            return null;
        }
        // should be same as fixed option about static options,
        // for example, PrimaryKeyIdentityDisabled
        @SuppressWarnings("unchecked")
        final DeleteOption<ConditionBean> option = (DeleteOption<ConditionBean>) args[1];
        return option;
    }

    protected void prepareStatementConfigOnThreadIfExists(DeleteOption<ConditionBean> option) {
        final StatementConfig config = option != null ? option.getDeleteStatementConfig() : null;
        if (config != null) {
            InternalMapContext.setUpdateStatementConfig(config);
        }
    }

    protected Object doExecute(Object bean, String sql, DeleteOption<ConditionBean> option) {
        final TnDeleteEntityHandler handler = createDeleteEntityHandler(sql, option);
        final Object[] realArgs = new Object[] { bean };
        handler.setExceptionMessageSqlArgs(realArgs);
        final int result = handler.execute(realArgs);
        return Integer.valueOf(result);
    }

    // ===================================================================================
    //                                                                          Delete SQL
    //                                                                          ==========
    /**
     * Create update SQL. The delete is by the primary keys or unique keys.
     * @param bean The bean of the entity to delete. (NotNull)
     * @param option An option of delete. (NullAllowed)
     * @return The delete SQL. (NotNull)
     */
    protected String createDeleteSql(Object bean, DeleteOption<ConditionBean> option) {
        checkPrimaryKey();
        final StringBuilder sb = new StringBuilder(64);
        sb.append("delete from ").append(_targetDBMeta.getTableSqlName());
        final Set<String> uniqueDrivenPropSet = extractUniqueDrivenPropSet(bean);
        setupUpdateWhere(sb, uniqueDrivenPropSet, _optimisticLockHandling);
        return sb.toString();
    }

    // ===================================================================================
    //                                                                             Handler
    //                                                                             =======
    protected TnDeleteEntityHandler createDeleteEntityHandler(String sql, DeleteOption<ConditionBean> option) {
        final TnDeleteEntityHandler handler = newDeleteEntityHandler(sql);
        handler.setOptimisticLockHandling(_optimisticLockHandling); // [DBFlute-0.8.0]
        handler.setVersionNoAutoIncrementOnMemory(_versionNoAutoIncrementOnMemory);
        handler.setDeleteOption(option);
        return handler;
    }

    protected TnDeleteEntityHandler newDeleteEntityHandler(String sql) {
        return new TnDeleteEntityHandler(_dataSource, _statementFactory, sql, _beanMetaData);
    }

    // ===================================================================================
    //                                                                  Execute Status Log
    //                                                                  ==================
    public void setOptimisticLockHandling(boolean optimisticLockHandling) {
        _optimisticLockHandling = optimisticLockHandling;
    }
}
