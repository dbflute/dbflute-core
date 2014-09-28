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
package org.seasar.dbflute.s2dao.sqlhandler;

import java.sql.Connection;

import javax.sql.DataSource;

import org.seasar.dbflute.jdbc.StatementFactory;
import org.seasar.dbflute.s2dao.metadata.TnBeanMetaData;
import org.seasar.dbflute.s2dao.metadata.TnPropertyType;

/**
 * @author modified by jflute (originated in S2Dao)
 */
public class TnBatchInsertHandler extends TnAbstractBatchHandler {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public TnBatchInsertHandler(DataSource dataSource, StatementFactory statementFactory, String sql,
            TnBeanMetaData beanMetaData, TnPropertyType[] boundPropTypes) {
        super(dataSource, statementFactory, sql, beanMetaData, boundPropTypes);
        setOptimisticLockHandling(false);
    }

    // ===================================================================================
    //                                                                            Override
    //                                                                            ========
    @Override
    protected void setupBindVariables(Object bean) {
        setupInsertBindVariables(bean);
    }

    @Override
    protected Integer getBatchLoggingLimit() {
        return _insertOption != null ? _insertOption.getBatchInsertLoggingLimit() : null;
    }

    @Override
    protected void processBefore(Connection conn, Object beanList) {
        super.processBefore(conn, beanList);
        if (isPrimaryKeyIdentityDisabled()) {
            disableIdentityGeneration(createInheritedConnectionDataSource(conn));
        }
    }

    @Override
    protected void processFinally(Connection conn, Object beanList, RuntimeException sqlEx) {
        super.processFinally(conn, beanList, sqlEx);
        if (isPrimaryKeyIdentityDisabled()) {
            try {
                enableIdentityGeneration(createInheritedConnectionDataSource(conn));
            } catch (RuntimeException e) {
                if (sqlEx == null) {
                    throw e;
                }
                // ignore the exception when main SQL fails
                // not to close the main exception
            }
        }
    }

    @Override
    protected String getBatchUpdateSQLFailureProcessTitle() {
        return "batch insert";
    }
}