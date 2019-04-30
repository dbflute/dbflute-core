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
 * The digger of physical connection.
 * @author jflute
 */
public interface PhysicalConnectionDigger {

    /**
     * Dig up the physical connection from logical one.
     * @param conn The connection supposed to be logical connection. (NotNull)
     * @return The instance of connection as physical one. (NotNull)
     * @throws SQLException When it fails to handle the connection.
     */
    Connection digUp(Connection conn) throws SQLException;
}
