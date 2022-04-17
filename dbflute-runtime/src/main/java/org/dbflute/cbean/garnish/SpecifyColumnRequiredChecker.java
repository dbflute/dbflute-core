/*
 * Copyright 2014-2022 the original author or authors.
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
package org.dbflute.cbean.garnish;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

import org.dbflute.cbean.ConditionBean;
import org.dbflute.cbean.sqlclause.SqlClause;
import org.dbflute.cbean.sqlclause.select.SelectedRelationColumn;
import org.dbflute.exception.RequiredSpecifyColumnNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 1.1.2 (2016/12/31 Saturday)
 */
public class SpecifyColumnRequiredChecker {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger _log = LoggerFactory.getLogger(SpecifyColumnRequiredChecker.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected boolean _warningOnly;

    // ===================================================================================
    //                                                                              Option
    //                                                                              ======
    public SpecifyColumnRequiredChecker warningOnly() { // since 1.2.0
        _warningOnly = true;
        return this;
    }

    // ===================================================================================
    //                                                                               Check
    //                                                                               =====
    public void checkSpecifyColumnRequiredIfNeeds(ConditionBean cb, Consumer<Set<String>> thrower) {
        // cannot embed this to SQL clause because of too complex
        // so simple implementation like this:
        final SqlClause sqlClause = cb.getSqlClause();
        final Set<String> nonSpecifiedAliasSet = new LinkedHashSet<>();
        doCheckBasePointTable(cb, sqlClause, nonSpecifiedAliasSet);
        doCheckRelationTable(sqlClause, nonSpecifiedAliasSet);
        if (!nonSpecifiedAliasSet.isEmpty()) {
            handleNonSpecified(thrower, nonSpecifiedAliasSet);
        }
    }

    protected void doCheckBasePointTable(ConditionBean cb, SqlClause sqlClause, Set<String> nonSpecifiedAliasSet) {
        final String basePointAliasName = sqlClause.getBasePointAliasName();
        if (!sqlClause.hasSpecifiedSelectColumn(basePointAliasName)) { // local table without SpecifyColumn
            nonSpecifiedAliasSet.add(cb.asDBMeta().getTableDispName() + " (" + basePointAliasName + ")");
        }
    }

    protected void doCheckRelationTable(SqlClause sqlClause, Set<String> nonSpecifiedAliasSet) {
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
    }

    protected void handleNonSpecified(Consumer<Set<String>> thrower, Set<String> nonSpecifiedAliasSet) {
        try {
            evaluateThrower(thrower, nonSpecifiedAliasSet);
        } catch (RequiredSpecifyColumnNotFoundException e) { // see ConditionBeanExceptionThrower
            if (_warningOnly) { // optional for e.g. production
                warnNonSpecified(e);
            } else { // basically here
                throw e;
            }
        }
    }

    protected void evaluateThrower(Consumer<Set<String>> thrower, Set<String> nonSpecifiedAliasSet) {
        thrower.accept(nonSpecifiedAliasSet);
    }

    protected void warnNonSpecified(RequiredSpecifyColumnNotFoundException e) {
        // needs stack-trace to know application caller
        _log.warn("*Found the violation of SpecifyColumnRequired", e);
    }
}
