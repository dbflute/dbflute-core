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
package org.dbflute.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * The provider of connection for database meta data.
 * @author jflute
 */
public interface MetaDataConnectionProvider {

    /**
     * @return The instance of connection. (NotNull)
     * @throws SQLException When it fails to handle the SQL.
     */
    Connection getConnection() throws SQLException;
}
