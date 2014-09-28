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

import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.seasar.dbflute.bhv.DeleteOption;
import org.seasar.dbflute.cbean.ConditionBean;
import org.seasar.dbflute.jdbc.StatementFactory;
import org.seasar.dbflute.s2dao.sqlhandler.TnBatchDeleteHandler;

/**
 * @author modified by jflute (originated in S2Dao)
 */
public class TnBatchDeleteDynamicCommand extends TnDeleteEntityDynamicCommand {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public TnBatchDeleteDynamicCommand(DataSource dataSource, StatementFactory statementFactory) {
        super(dataSource, statementFactory);
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    @Override
    protected Object doExecute(Object bean, String sql, DeleteOption<ConditionBean> option) {
        final List<?> beanList = extractBeanListFromBeanChecked(bean);
        final TnBatchDeleteHandler handler = createBatchDeleteHandler(sql, option);
        // because the variable is set when exception occurs if batch
        //handler.setExceptionMessageSqlArgs(new Object[] { beanList });
        return handler.executeBatch(beanList);
    }

    // ===================================================================================
    //                                                                             Handler
    //                                                                             =======
    protected TnBatchDeleteHandler createBatchDeleteHandler(String sql, DeleteOption<ConditionBean> option) {
        final TnBatchDeleteHandler handler = newBatchDeleteHandler(sql);
        handler.setOptimisticLockHandling(_optimisticLockHandling); // [DBFlute-0.8.0]
        handler.setVersionNoAutoIncrementOnMemory(_versionNoAutoIncrementOnMemory);
        handler.setDeleteOption(option);
        return handler;
    }

    protected TnBatchDeleteHandler newBatchDeleteHandler(String sql) {
        return new TnBatchDeleteHandler(_dataSource, _statementFactory, sql, _beanMetaData);
    }

    // ===================================================================================
    //                                                                          Create SQL
    //                                                                          ==========
    @Override
    protected Set<String> extractUniqueDrivenPropSet(Object bean) {
        return null; // cannot use unique-driven for batch
    }
}
