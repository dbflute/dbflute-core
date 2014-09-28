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
package org.seasar.dbflute.cbean.sqlclause.join;

/**
 * @author jflute
 * @since 0.9.7.5 (2010/10/11 Monday)
 */
public interface FixedConditionResolver {

    /**
     * Resolve variables on fixed-condition.
     * @param fixedCondition The string of fixed-condition. (NotNull)
     * @param fixedInline Are the fixed conditions located on in-line view?
     * @return The resolved string of fixed-condition. (NotNull)
     */
    String resolveVariable(String fixedCondition, boolean fixedInline);

    /**
     * Resolve fixed InlineView for fixed-condition.
     * @param foreignTable The SQL name of foreign table that has fixed-condition. (NotNull) 
     * @param treatedAsInnerJoin Does the join treated as inner-join?
     * @return The resolved string of foreign table expression. (NotNull)
     */
    String resolveFixedInlineView(String foreignTable, boolean treatedAsInnerJoin);

    /**
     * Does the fixed-condition have over-relation?
     * @param fixedCondition The string of fixed-condition. (NotNull)
     * @return The determination, true or false.
     */
    boolean hasOverRelation(String fixedCondition);
}
