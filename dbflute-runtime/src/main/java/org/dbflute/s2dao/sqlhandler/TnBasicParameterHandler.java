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

import java.sql.Connection;

import javax.sql.DataSource;

import org.dbflute.jdbc.StatementFactory;

/**
 * @author modified by jflute (originated in S2Dao)
 */
public abstract class TnBasicParameterHandler extends TnAbstractBasicSqlHandler {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public TnBasicParameterHandler(DataSource dataSource, StatementFactory statementFactory, String sql) {
        super(dataSource, statementFactory, sql);
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    public Object execute(Object[] args) {
        return execute(args, getArgTypes(args));
    }

    public Object execute(Object[] args, Class<?>[] argTypes) {
        final Connection conn = getConnection();
        try {
            return doExecute(conn, args, argTypes);
        } finally {
            close(conn);
        }
    }

    protected abstract Object doExecute(Connection conn, Object[] args, Class<?>[] argTypes);
}
