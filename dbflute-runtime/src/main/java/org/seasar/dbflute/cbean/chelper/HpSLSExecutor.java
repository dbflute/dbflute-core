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
package org.seasar.dbflute.cbean.chelper;

import org.seasar.dbflute.cbean.ConditionBean;
import org.seasar.dbflute.cbean.sqlclause.clause.SelectClauseType;

/**
 * @param <CB> The type of condition-bean.
 * @param <RESULT> The type of result of function.
 * @author jflute
 * @since 1.0.6A (2014/06/12 Thursday)
 */
public interface HpSLSExecutor<CB extends ConditionBean, RESULT> {

    /**
     * Execute the scalar select for the select clause type.
     * @param cb The condition-bean to execute. (NotNull)
     * @param resultType The type of the scalar result. (NotNull)
     * @param selectClauseType The type of select clause. (NullAllowed)
     * @return The result of the function. (NullAllowed)
     */
    RESULT execute(CB cb, Class<RESULT> resultType, SelectClauseType selectClauseType);
}
