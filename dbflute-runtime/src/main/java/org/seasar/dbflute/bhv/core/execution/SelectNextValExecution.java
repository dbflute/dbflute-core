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
package org.seasar.dbflute.bhv.core.execution;

import java.util.Map;

import javax.sql.DataSource;

import org.seasar.dbflute.bhv.core.supplement.SequenceCache;
import org.seasar.dbflute.bhv.core.supplement.SequenceCache.SequenceRealExecutor;
import org.seasar.dbflute.jdbc.StatementFactory;
import org.seasar.dbflute.s2dao.jdbc.TnResultSetHandler;

/**
 * @author jflute
 */
public class SelectNextValExecution extends SelectSimpleExecution {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final SequenceCache _sequenceCache;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public SelectNextValExecution(DataSource dataSource, StatementFactory statementFactory,
            Map<String, Class<?>> argNameTypeMap, String twoWaySql, TnResultSetHandler resultSetHandler,
            SequenceCache sequenceCache) {
        super(dataSource, statementFactory, argNameTypeMap, twoWaySql, resultSetHandler);
        _sequenceCache = sequenceCache;
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    @Override
    public Object execute(final Object[] args) {
        final Object nextVal;
        if (_sequenceCache != null) {
            nextVal = _sequenceCache.nextval(new SequenceRealExecutor() {
                public Object execute() {
                    return executeSuperExecute(args);
                }
            });
        } else {
            nextVal = executeSuperExecute(args);
        }
        return nextVal;
    }

    protected Object executeSuperExecute(Object[] args) {
        return super.execute(args);
    }
}
