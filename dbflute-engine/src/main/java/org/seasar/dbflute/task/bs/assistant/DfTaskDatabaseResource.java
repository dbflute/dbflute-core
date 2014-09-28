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
package org.seasar.dbflute.task.bs.assistant;

import java.util.Properties;

import org.apache.torque.engine.database.model.UnifiedSchema;
import org.seasar.dbflute.helper.jdbc.connection.DfDataSourceHandler;

/**
 * @author jflute
 */
public class DfTaskDatabaseResource {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** DB driver. */
    protected String _driver;

    /** DB URL. */
    protected String _url;

    /** Main schema. */
    protected UnifiedSchema _mainSchema;

    /** User name. */
    protected String _user;

    /** Password */
    protected String _password;

    /** Connection properties. */
    protected Properties _connectionProperties;

    /** The handler of data source. (NotNull) */
    protected final DfDataSourceHandler _dataSourceHandler = new DfDataSourceHandler();

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getDriver() {
        return _driver;
    }

    public void setDriver(String driver) {
        this._driver = driver;
    }

    public String getUrl() {
        return _url;
    }

    public void setUrl(String url) {
        this._url = url;
    }

    public UnifiedSchema getMainSchema() {
        return _mainSchema;
    }

    public void setMainSchema(UnifiedSchema mainSchema) {
        this._mainSchema = mainSchema;
    }

    public String getUser() {
        return _user;
    }

    public void setUser(String user) {
        this._user = user;
    }

    public String getPassword() {
        return _password;
    }

    public void setPassword(String password) {
        this._password = password;
    }

    public Properties getConnectionProperties() {
        return _connectionProperties;
    }

    public void setConnectionProperties(Properties connectionProperties) {
        this._connectionProperties = connectionProperties;
    }

    public DfDataSourceHandler getDataSourceHandler() {
        return _dataSourceHandler;
    }
}
