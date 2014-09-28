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
package org.dbflute.helper.jdbc.connection;

import java.sql.Connection;
import java.sql.SQLException;

import org.dbflute.util.DfTypeUtil;

/**
 * @author jflute
 */
public class DfFittingDataSource extends DfCushionDataSource {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfConnectionProvider _dataSourceProvider;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfFittingDataSource(DfDataSourceHandler dataSourceProvider) {
        _dataSourceProvider = dataSourceProvider;
    }

    // ===================================================================================
    //                                                                 Connection Handling
    //                                                                 ===================
    public Connection getConnection() throws SQLException {
        return _dataSourceProvider.getConnection();
    }

    public Connection newConnection() throws SQLException {
        return _dataSourceProvider.newConnection();
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return DfTypeUtil.toClassTitle(this) + ":" + _dataSourceProvider;
    }
}
