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

import javax.sql.DataSource;

import org.seasar.dbflute.jdbc.StatementFactory;
import org.seasar.dbflute.s2dao.metadata.TnBeanMetaData;
import org.seasar.dbflute.s2dao.metadata.TnPropertyType;

/**
 * @author modified by jflute (originated in S2Dao)
 */
public class TnDeleteEntityHandler extends TnAbstractEntityHandler {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final TnPropertyType[] EMPTY_PROPS = new TnPropertyType[0];

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public TnDeleteEntityHandler(DataSource dataSource, StatementFactory statementFactory, String sql,
            TnBeanMetaData beanMetaData) {
        super(dataSource, statementFactory, sql, beanMetaData, EMPTY_PROPS);
    }

    // ===================================================================================
    //                                                                            Override
    //                                                                            ========
    @Override
    protected void setupBindVariables(Object bean) {
        setupDeleteBindVariables(bean);
        setExceptionMessageSqlArgs(_bindVariables);
    }

    @Override
    protected String getUpdateSQLFailureProcessTitle() {
        return "delete";
    }
}