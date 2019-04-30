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
package org.dbflute.s2dao.sqlhandler;

import javax.sql.DataSource;

import org.dbflute.jdbc.StatementFactory;
import org.dbflute.s2dao.metadata.TnBeanMetaData;
import org.dbflute.s2dao.metadata.TnPropertyType;

/**
 * @author modified by jflute (originated in S2Dao)
 */
public class TnBatchUpdateHandler extends TnAbstractBatchHandler {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public TnBatchUpdateHandler(DataSource dataSource, StatementFactory statementFactory, String sql, TnBeanMetaData beanMetaData,
            TnPropertyType[] boundPropTypes) {
        super(dataSource, statementFactory, sql, beanMetaData, boundPropTypes);
    }

    // ===================================================================================
    //                                                                            Override
    //                                                                            ========
    @Override
    protected void setupBindVariables(Object bean) {
        setupUpdateBindVariables(bean);
    }

    @Override
    protected Integer getBatchLoggingLimit() {
        return _updateOption != null ? _updateOption.getBatchUpdateLoggingLimit() : null;
    }

    @Override
    protected String getBatchUpdateSQLFailureProcessTitle() {
        return "batch update";
    }
}