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
import org.seasar.dbflute.s2dao.identity.TnIdentifierGenerator;
import org.seasar.dbflute.s2dao.metadata.TnBeanMetaData;
import org.seasar.dbflute.s2dao.metadata.TnPropertyType;

/**
 * @author modified by jflute (originated in S2Dao)
 */
public class TnInsertEntityHandler extends TnAbstractEntityHandler {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public TnInsertEntityHandler(DataSource dataSource, StatementFactory statementFactory, String sql,
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
        setExceptionMessageSqlArgs(_bindVariables);
    }

    @Override
    protected void processBefore(final Connection conn, Object bean) {
        super.processBefore(conn, bean);
        doProcessIdentity(new IdentityProcessCallback() {
            public void callback(TnIdentifierGenerator generator) {
                if (generator.isPrimaryKey() && isPrimaryKeyIdentityDisabled()) {
                    disableIdentityGeneration(createInheritedConnectionDataSource(conn));
                }
            }
        });
    }

    @Override
    protected void processFinally(final Connection conn, Object bean, RuntimeException sqlEx) {
        super.processFinally(conn, bean, sqlEx);
        try {
            doProcessIdentity(new IdentityProcessCallback() {
                public void callback(TnIdentifierGenerator generator) {
                    if (generator.isPrimaryKey() && isPrimaryKeyIdentityDisabled()) {
                        enableIdentityGeneration(createInheritedConnectionDataSource(conn));
                    }
                }
            });
        } catch (RuntimeException e) {
            if (sqlEx == null) {
                throw e;
            }
            // ignore the exception when main SQL fails
            // not to close the main exception
        }

    }

    @Override
    protected void processSuccess(final Connection conn, final Object bean, int ret) {
        super.processSuccess(conn, bean, ret);
        doProcessIdentity(new IdentityProcessCallback() {
            public void callback(TnIdentifierGenerator generator) {
                if (generator.isPrimaryKey() && isPrimaryKeyIdentityDisabled()) {
                    return;
                }
                generator.setIdentifier(bean, createInheritedConnectionDataSource(conn));
            }
        });
        updateVersionNoIfNeed(bean);
        updateTimestampIfNeed(bean);
    }

    protected void doProcessIdentity(IdentityProcessCallback callback) {
        final TnBeanMetaData bmd = getBeanMetaData();
        for (int i = 0; i < bmd.getIdentifierGeneratorSize(); i++) {
            final TnIdentifierGenerator generator = bmd.getIdentifierGenerator(i);
            if (!generator.isSelfGenerate()) { // identity
                callback.callback(generator);
            }
        }
    }

    protected static interface IdentityProcessCallback {
        void callback(TnIdentifierGenerator generator);
    }

    @Override
    protected String getUpdateSQLFailureProcessTitle() {
        return "insert";
    }
}
