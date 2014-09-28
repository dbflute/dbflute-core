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

import org.seasar.dbflute.bhv.InsertOption;
import org.seasar.dbflute.cbean.ConditionBean;
import org.seasar.dbflute.jdbc.StatementFactory;
import org.seasar.dbflute.s2dao.metadata.TnPropertyType;
import org.seasar.dbflute.s2dao.sqlhandler.TnBatchInsertHandler;

/**
 * @author modified by jflute (originated in S2Dao)
 */
public class TnBatchInsertDynamicCommand extends TnInsertEntityDynamicCommand {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public TnBatchInsertDynamicCommand(DataSource dataSource, StatementFactory statementFactory) {
        super(dataSource, statementFactory);
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    @Override
    protected Object doExecute(Object bean, TnPropertyType[] propertyTypes, String sql,
            InsertOption<ConditionBean> option) {
        final List<?> beanList = extractBeanListFromBeanChecked(bean);
        final TnBatchInsertHandler handler = createBatchInsertHandler(propertyTypes, sql, option);
        // because the variable is set when exception occurs if batch 
        //handler.setExceptionMessageSqlArgs(new Object[] { ... });
        return handler.executeBatch(beanList);
    }

    // ===================================================================================
    //                                                                       Insert Column
    //                                                                       =============
    // Batch Update does not use modified properties here
    // (modified properties are converted to specified columns before here)
    @Override
    protected Set<?> getModifiedPropertyNames(Object bean) {
        return Collections.EMPTY_SET;
    }

    @Override
    protected boolean isModifiedProperty(Set<?> modifiedSet, TnPropertyType pt) {
        return true; // as default (all columns are updated)
    }

    // ===================================================================================
    //                                                                             Handler
    //                                                                             =======
    protected TnBatchInsertHandler createBatchInsertHandler(TnPropertyType[] boundPropTypes, String sql,
            InsertOption<ConditionBean> option) {
        final TnBatchInsertHandler handler = newBatchInsertHandler(boundPropTypes, sql);
        handler.setInsertOption(option);
        return handler;
    }

    protected TnBatchInsertHandler newBatchInsertHandler(TnPropertyType[] boundPropTypes, String sql) {
        return new TnBatchInsertHandler(_dataSource, _statementFactory, sql, _beanMetaData, boundPropTypes);
    }

    // ===================================================================================
    //                                                                          Create SQL
    //                                                                          ==========
    @Override
    protected Set<String> extractUniqueDrivenPropSet(Object bean) {
        return null; // cannot use unique-driven for batch
    }
}
