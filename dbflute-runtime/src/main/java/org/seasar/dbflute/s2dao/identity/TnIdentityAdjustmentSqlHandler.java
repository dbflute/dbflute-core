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
package org.seasar.dbflute.s2dao.identity;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.seasar.dbflute.exception.handler.SQLExceptionResource;
import org.seasar.dbflute.jdbc.StatementFactory;
import org.seasar.dbflute.s2dao.sqlhandler.TnBasicUpdateHandler;

/**
 * @author modified by jflute (originated in S2Dao)
 */
public class TnIdentityAdjustmentSqlHandler extends TnBasicUpdateHandler {

    public TnIdentityAdjustmentSqlHandler(DataSource dataSource, StatementFactory statementFactory, String sql) {
        super(dataSource, statementFactory, sql);
    }

    @Override
    protected Object doExecute(Connection conn, Object[] args, Class<?>[] argTypes) {
        logSql(args, argTypes);
        Statement st = null;
        try {
            // PreparedStatement is not used here
            // because SQLServer do not work by PreparedStatement
            // but it do work well by Statement
            st = conn.createStatement();
            return st.executeUpdate(_sql);
        } catch (SQLException e) {
            final SQLExceptionResource resource = createSQLExceptionResource();
            resource.setNotice("Failed to execute the SQL to adjust identity.");
            handleSQLException(e, resource);
            return 0; // unreachable
        } finally {
            close(st);
        }
    };
}
