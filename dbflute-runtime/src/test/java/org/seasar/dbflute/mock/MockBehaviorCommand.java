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
package org.seasar.dbflute.mock;

import org.seasar.dbflute.bhv.core.BehaviorCommand;
import org.seasar.dbflute.bhv.core.SqlExecutionCreator;
import org.seasar.dbflute.cbean.ConditionBean;
import org.seasar.dbflute.outsidesql.OutsideSqlOption;

/**
 * @author jflute
 */
public class MockBehaviorCommand implements BehaviorCommand<Object> {

    public void afterExecuting() {
    }

    public void beforeGettingSqlExecution() {
    }

    public String buildSqlExecutionKey() {
        throw new UnsupportedOperationException();
    }

    public SqlExecutionCreator createSqlExecutionCreator() {
        throw new UnsupportedOperationException();
    }

    public String getCommandName() {
        return "FooCommand";
    }

    public Class<?> getCommandReturnType() {
        return Object.class;
    }

    public ConditionBean getConditionBean() {
        throw new UnsupportedOperationException();
    }

    public OutsideSqlOption getOutsideSqlOption() {
        throw new UnsupportedOperationException();
    }

    public String getOutsideSqlPath() {
        throw new UnsupportedOperationException();
    }

    public Object getParameterBean() {
        return null;
    }

    public Object[] getSqlExecutionArgument() {
        return new Object[] {};
    }

    public String getTableDbName() {
        return "FooTable";
    }

    public boolean isConditionBean() {
        return false;
    }

    public boolean isInitializeOnly() {
        return false;
    }

    public boolean isOutsideSql() {
        return false;
    }

    public boolean isProcedure() {
        return false;
    }

    public boolean isSelect() {
        return false;
    }

    public boolean isSelectCount() {
        return false;
    }

    public boolean isSelectCursor() {
        return false;
    }

    public boolean isInsert() {
        return false;
    }

    public boolean isUpdate() {
        return false;
    }

    public boolean isDelete() {
        return false;
    }

    public String getInvokePath() {
        return null;
    }
}
