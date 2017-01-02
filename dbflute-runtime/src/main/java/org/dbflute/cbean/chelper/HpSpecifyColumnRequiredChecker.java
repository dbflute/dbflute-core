/*
 * Copyright 2014-2017 the original author or authors.
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
package org.dbflute.cbean.chelper;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

import org.dbflute.cbean.ConditionBean;
import org.dbflute.cbean.sqlclause.SqlClause;
import org.dbflute.cbean.sqlclause.select.SelectedRelationColumn;

/**
 * @author jflute
 * @since 1.1.2 (2016/12/31 Saturday)
 */
public class HpSpecifyColumnRequiredChecker {

    public void checkSpecifyColumnRequiredIfNeeds(ConditionBean cb, Consumer<Set<String>> thrower) {
        // cannot embed this to SQL clause because of too complex
        // so simple implementation like this:
        final SqlClause sqlClause = cb.getSqlClause();
        final String basePointAliasName = sqlClause.getBasePointAliasName();
        final Set<String> nonSpecifiedAliasSet = new LinkedHashSet<>();
        if (!sqlClause.hasSpecifiedSelectColumn(basePointAliasName)) { // local table without SpecifyColumn
            nonSpecifiedAliasSet.add(cb.asDBMeta().getTableDispName() + " (" + basePointAliasName + ")");
        }
        for (Entry<String, Map<String, SelectedRelationColumn>> entry : sqlClause.getSelectedRelationColumnMap().entrySet()) {
            final String tableAliasName = entry.getKey();
            if (!sqlClause.hasSpecifiedSelectColumn(tableAliasName)) { // relation table without SpecifyColumn
                final Collection<SelectedRelationColumn> values = entry.getValue().values();
                final String dispName;
                if (!values.isEmpty()) {
                    final SelectedRelationColumn firstColumn = values.iterator().next();
                    dispName = sqlClause.translateSelectedRelationPathToPropName(firstColumn.getRelationNoSuffix());
                } else { // no way, just in case
                    dispName = "*no name";
                }
                nonSpecifiedAliasSet.add(dispName + " (" + tableAliasName + ")");
            }
        }
        if (!nonSpecifiedAliasSet.isEmpty()) {
            thrower.accept(nonSpecifiedAliasSet);
        }
    }
}
