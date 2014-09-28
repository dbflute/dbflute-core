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

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.seasar.dbflute.bhv.UpdateOption;
import org.seasar.dbflute.cbean.ConditionBean;
import org.seasar.dbflute.jdbc.StatementFactory;
import org.seasar.dbflute.s2dao.metadata.TnPropertyType;
import org.seasar.dbflute.s2dao.sqlhandler.TnBatchUpdateHandler;

/**
 * @author modified by jflute (originated in S2Dao)
 */
public class TnBatchUpdateDynamicCommand extends TnUpdateEntityDynamicCommand {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The result for no batch-update as normal execution. */
    private static final int[] NON_BATCH_UPDATE = new int[] {};

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public TnBatchUpdateDynamicCommand(DataSource dataSource, StatementFactory statementFactory) {
        super(dataSource, statementFactory);
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    @Override
    protected Object doExecute(Object bean, TnPropertyType[] propertyTypes, String sql,
            UpdateOption<ConditionBean> option) {
        final List<?> beanList = extractBeanListFromBeanChecked(bean);
        final TnBatchUpdateHandler handler = createBatchUpdateHandler(propertyTypes, sql, option);
        // because the variable is set when exception occurs if batch
        //handler.setExceptionMessageSqlArgs(new Object[] { beanList });
        return handler.executeBatch(beanList);
    }

    // ===================================================================================
    //                                                                       Update Column
    //                                                                       =============
    // Batch Update does not use modified properties here
    // (modified properties are converted to specified columns before here)
    @Override
    protected Set<String> getModifiedPropertyNames(Object bean) {
        return Collections.emptySet();
    }

    @Override
    protected boolean isModifiedProperty(Set<?> modifiedSet, TnPropertyType pt) {
        return true; // as default (all columns are updated)
    }

    // ===================================================================================
    //                                                                             Handler
    //                                                                             =======
    protected TnBatchUpdateHandler createBatchUpdateHandler(TnPropertyType[] boundPropTypes, String sql,
            UpdateOption<ConditionBean> option) {
        final TnBatchUpdateHandler handler = newBatchUpdateHandler(boundPropTypes, sql);
        handler.setOptimisticLockHandling(_optimisticLockHandling);
        handler.setVersionNoAutoIncrementOnMemory(_versionNoAutoIncrementOnMemory);
        handler.setUpdateOption(option);
        return handler;
    }

    protected TnBatchUpdateHandler newBatchUpdateHandler(TnPropertyType[] boundPropTypes, String sql) {
        return new TnBatchUpdateHandler(_dataSource, _statementFactory, sql, _beanMetaData, boundPropTypes);
    }

    // ===================================================================================
    //                                                                          Create SQL
    //                                                                          ==========
    @Override
    protected Set<String> extractUniqueDrivenPropSet(Object bean) {
        return null; // cannot use unique-driven for batch
    }

    // ===================================================================================
    //                                                                  Non Update Message
    //                                                                  ==================
    @Override
    protected String createNonUpdateLogMessage(final Object bean) {
        final StringBuilder sb = new StringBuilder();
        final String tableDbName = _targetDBMeta.getTableDbName();
        sb.append("...Skipping batch update because of non-specified: table=").append(tableDbName);
        if (bean instanceof List<?>) {
            final List<?> entityList = (List<?>) bean;
            sb.append(", batchSize=").append(entityList.size());
        }
        return sb.toString();
    }

    @Override
    protected Object getNonUpdateReturn() {
        return NON_BATCH_UPDATE;
    }
}
