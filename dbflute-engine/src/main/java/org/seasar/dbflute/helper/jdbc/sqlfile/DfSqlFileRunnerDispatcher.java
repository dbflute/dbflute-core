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
package org.seasar.dbflute.helper.jdbc.sqlfile;

import java.io.File;
import java.sql.SQLException;
import java.sql.Statement;

import org.seasar.dbflute.helper.jdbc.sqlfile.DfSqlFileRunnerExecute.DfRunnerDispatchResult;

/**
 * @author jflute
 * @since 0.9.5.4 (2009/08/07 Friday)
 */
public interface DfSqlFileRunnerDispatcher {

    /**
     * Dispatch executing a SQL.
     * @param sqlFile The SQL file that contains the SQL. (NotNull)
     * @param st Statement. (NotNull)
     * @param sql SQL string. (NotNull)
     * @return The type of dispatch result. (NotNull)
     * @throws SQLException
     */
    DfRunnerDispatchResult dispatch(File sqlFile, Statement st, String sql) throws SQLException;
}
