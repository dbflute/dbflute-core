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
package org.dbflute.logic.doc.spolicy;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.torque.engine.database.model.Column;
import org.apache.torque.engine.database.model.Table;
import org.dbflute.helper.StringKeyMap;
import org.dbflute.logic.doc.spolicy.result.DfSPolicyResult;
import org.dbflute.logic.doc.spolicy.secretary.DfSPolicyMiscSecretary;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.1.2 (2016/12/29 Thursday at higashi-ginza)
 */
public class DfSPolicyTableThemeChecker {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static Map<String, BiConsumer<Table, DfSPolicyResult>> _cachedThemeMap;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfSPolicyChecker _spolicyChecker;
    protected final DfSPolicyMiscSecretary _secretary = new DfSPolicyMiscSecretary();

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfSPolicyTableThemeChecker(DfSPolicyChecker spolicyChecker) {
        _spolicyChecker = spolicyChecker;
    }

    // ===================================================================================
    //                                                                         Table Theme
    //                                                                         ===========
    public void checkTableTheme(List<String> themeList, DfSPolicyResult result, Table table) {
        for (String theme : themeList) {
            evaluateTableTheme(theme, result, table);
        }
    }

    protected void evaluateTableTheme(String theme, DfSPolicyResult result, Table table) {
        final Map<String, BiConsumer<Table, DfSPolicyResult>> themeMap = getThemeMap();
        final BiConsumer<Table, DfSPolicyResult> themeProcessor = themeMap.get(theme);
        if (themeProcessor != null) {
            themeProcessor.accept(table, result);
        } else {
            throwSchemaPolicyCheckUnknownThemeException(theme, "Table");
        }
    }

    protected Map<String, BiConsumer<Table, DfSPolicyResult>> getThemeMap() {
        if (_cachedThemeMap != null) {
            return _cachedThemeMap;
        }
        final Map<String, BiConsumer<Table, DfSPolicyResult>> themeMap = StringKeyMap.createAsCaseInsensitiveOrdered();
        prepareTableTheme(themeMap);
        _cachedThemeMap = themeMap;
        return _cachedThemeMap;
    }

    // ===================================================================================
    //                                                                         Theme Logic
    //                                                                         ===========
    protected void prepareTableTheme(Map<String, BiConsumer<Table, DfSPolicyResult>> themeMap) {
        // e.g.
        // ; tableMap = map:{
        //     ; themeList = list:{ hasPK ; upperCaseBasis ; identityIfPureIDPK }
        // }
        define(themeMap, "hasPK", table -> !table.hasPrimaryKey(), table -> {
            return "The table should have primary key: " + toTableDisp(table);
        });
        define(themeMap, "upperCaseBasis", table -> Srl.isLowerCaseAny(buildCaseComparingTableName(table)), table -> {
            return "The table name should be on upper case basis: " + toTableDisp(table);
        });
        define(themeMap, "lowerCaseBasis", table -> Srl.isUpperCaseAny(buildCaseComparingTableName(table)), table -> {
            return "The table name should be on lower case basis: " + toTableDisp(table);
        });
        define(themeMap, "identityIfPureIDPK", table -> {
            if (table.hasPrimaryKey() && table.hasSinglePrimaryKey()) {
                final Column pk = table.getPrimaryKeyAsOne();
                return !pk.isForeignKey() && Srl.endsWith(pk.getName(), "ID") && !pk.isIdentity();
            } else {
                return false;
            }
        }, table -> {
            return "The primary key should be identity: " + toTableDisp(table) + "." + table.getPrimaryKeyAsOne().getName();
        });
        define(themeMap, "hasCommonColumn", table -> !table.hasAllCommonColumn(), table -> {
            return "The table should have common columns: " + toTableDisp(table);
        });
    }

    protected void define(Map<String, BiConsumer<Table, DfSPolicyResult>> themeMap, String theme, Predicate<Table> determiner,
            Function<Table, String> messenger) {
        themeMap.put(theme, (table, result) -> {
            if (determiner.test(table)) {
                result.violate("table.theme: " + theme, messenger.apply(table));
            }
        });
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    protected String buildCaseComparingTableName(Table table) {
        return _secretary.buildCaseComparingTableName(table);
    }

    protected String toTableDisp(Table table) {
        return _secretary.toTableDisp(table);
    }

    // ===================================================================================
    //                                                                           Exception
    //                                                                           =========
    protected void throwSchemaPolicyCheckUnknownThemeException(String theme, String targetType) {
        _secretary.throwSchemaPolicyCheckUnknownThemeException(theme, targetType);
    }
}
