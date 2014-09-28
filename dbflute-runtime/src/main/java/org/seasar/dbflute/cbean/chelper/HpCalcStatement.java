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

import java.util.Map;

/**
 * @author jflute
 */
public interface HpCalcStatement {

    /**
     * Build the calculation statement of the column as SQL name. <br />
     * e.g. called by Update Calculation
     * @param aliasName The alias name of the target column, containing dot mark. (NullAllowed)
     * @return The statement that has calculation. (NullAllowed: if null, means the column is not specified)
     */
    String buildStatementAsSqlName(String aliasName);

    /**
     * Build the calculation statement to the specified column. <br />
     * e.g. called by ColumnQuery Calculation
     * @param columnExp The expression of the column. (NotNull)
     * @return The statement that has calculation. (NullAllowed: if null, means the column is not specified)
     */
    String buildStatementToSpecifidName(String columnExp);

    /**
     * Build the calculation statement to the specified column. <br />
     * No cipher here because the column has already been handled cipher. <br />
     * e.g. called by ManualOrder Calculation
     * @param columnExp The expression of the column. (NotNull)
     * @param columnAliasMap The map of column alias. (NotNull)
     * @return The statement that has calculation. (NullAllowed: if null, means the column is not specified)
     */
    String buildStatementToSpecifidName(String columnExp, Map<String, String> columnAliasMap);
}
