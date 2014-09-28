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
package org.seasar.dbflute.helper.jdbc.context;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.torque.engine.database.model.UnifiedSchema;
import org.seasar.dbflute.util.DfTypeUtil;

/**
 * @author jflute
 */
public class DfSchemaSource implements DataSource {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DataSource _dataSource;
    protected final UnifiedSchema _schema;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfSchemaSource(DataSource dataSource, UnifiedSchema schema) {
        if (dataSource == null) {
            throw new IllegalArgumentException("The argument 'dataSource' should not be null.");
        }
        if (schema == null) {
            throw new IllegalArgumentException("The argument 'schema' should not be null.");
        }
        _dataSource = dataSource;
        _schema = schema;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return "{" + DfTypeUtil.toClassTitle(_dataSource) + ":" + _schema + "}";
    }

    // ===================================================================================
    //                                                                      Implementation
    //                                                                      ==============
    public PrintWriter getLogWriter() throws SQLException {
        return _dataSource.getLogWriter();
    }

    public int getLoginTimeout() throws SQLException {
        return _dataSource.getLoginTimeout();
    }

    public void setLogWriter(PrintWriter printwriter) throws SQLException {
        _dataSource.setLogWriter(printwriter);
    }

    public void setLoginTimeout(int i) throws SQLException {
        _dataSource.setLoginTimeout(i);
    }

    public boolean isWrapperFor(Class<?> class1) throws SQLException {
        return _dataSource.isWrapperFor(class1);
    }

    public <T> T unwrap(Class<T> class1) throws SQLException {
        return _dataSource.unwrap(class1);
    }

    public Connection getConnection() throws SQLException {
        return _dataSource.getConnection();
    }

    public Connection getConnection(String s, String s1) throws SQLException {
        return _dataSource.getConnection(s, s1);
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public DataSource getDataSource() {
        return _dataSource;
    }

    public UnifiedSchema getSchema() {
        return _schema;
    }
}
