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
package org.seasar.dbflute.cbean.sqlclause.query;

import org.seasar.dbflute.dbmeta.name.ColumnRealName;

/**
 * The arranger of query clause.
 * @author jflute
 */
public interface QueryClauseArranger {

    /**
     * Arrange the query clause.
     * @param columnRealName The real name of column. (NotNull)
     * @param operand The operand for the query. (NotNull)
     * @param bindExpression The expression for binding. (NotNull)
     * @param rearOption The option of rear. (NotNull)
     * @return The arranged query clause. (NotNull)
     */
    String arrange(ColumnRealName columnRealName, String operand, String bindExpression, String rearOption);
}
