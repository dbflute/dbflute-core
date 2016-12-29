/*
 * Copyright 2014-2016 the original author or authors.
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
package org.dbflute.logic.doc.policycheck;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.apache.torque.engine.database.model.Column;
import org.apache.torque.engine.database.model.Table;
import org.dbflute.helper.StringKeyMap;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.1.2 (2016/12/29 Thursday at higashi-ginza)
 */
public class DfSchemaPolicyTableTheme {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static Map<String, BiConsumer<Table, List<String>>> _cachedThemeMap;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfSchemaPolicyMiscSecretary _secretary = new DfSchemaPolicyMiscSecretary();

    // ===================================================================================
    //                                                                         Table Theme
    //                                                                         ===========
    public void checkTableTheme(Table table, Map<String, Object> tableMap, List<String> vioList) {
        processTableTheme(table, tableMap, vioList);
    }

    protected void processTableTheme(Table table, Map<String, Object> tableMap, List<String> vioList) {
        @SuppressWarnings("unchecked")
        final List<String> themeList = (List<String>) tableMap.get("themeList");
        if (themeList == null) {
            return;
        }
        for (String theme : themeList) {
            evaluateTableTheme(table, vioList, theme);
        }
    }

    protected void evaluateTableTheme(Table table, List<String> vioList, String theme) {
        final Map<String, BiConsumer<Table, List<String>>> themeMap = getThemeMap();
        final BiConsumer<Table, List<String>> themeProcessor = themeMap.get(theme);
        if (themeProcessor != null) {
            themeProcessor.accept(table, vioList);
        } else {
            throwSchemaPolicyCheckUnknownThemeException(theme, "Table");
        }
    }

    protected Map<String, BiConsumer<Table, List<String>>> getThemeMap() {
        if (_cachedThemeMap != null) {
            return _cachedThemeMap;
        }
        final Map<String, BiConsumer<Table, List<String>>> themeMap = StringKeyMap.createAsCaseInsensitiveOrdered();
        defineTableTheme(themeMap);
        _cachedThemeMap = themeMap;
        return _cachedThemeMap;
    }

    // ===================================================================================
    //                                                                         Theme Logic
    //                                                                         ===========
    protected void defineTableTheme(Map<String, BiConsumer<Table, List<String>>> themeMap) {
        // e.g.
        // ; tableMap = map:{
        //     ; themeList = list:{ hasPK ; upperCaseBasis ; identityIfPureIDPK }
        // }
        themeMap.put("hasPK", (table, vioList) -> {
            if (!table.hasPrimaryKey()) {
                vioList.add("The table should have primary key: " + table.getTableDbName());
            }
        });
        themeMap.put("upperCaseBasis", (table, vioList) -> {
            if (Srl.isLowerCaseAny(table.getTableSqlName())) { // use SQL name because DB name may be control name
                vioList.add("The table name should be on upper case basis: " + table.getTableDbName());
            }
        });
        themeMap.put("lowerCaseBasis", (table, vioList) -> {
            if (Srl.isUpperCaseAny(table.getTableSqlName())) { // same reason
                vioList.add("The table name should be on lower case basis: " + table.getTableDbName());
            }
        });
        themeMap.put("identityIfPureIDPK", (table, vioList) -> {
            if (table.hasPrimaryKey() && table.hasSinglePrimaryKey()) {
                final Column pk = table.getPrimaryKeyAsOne();
                if (!pk.isForeignKey() && Srl.endsWith(pk.getName(), "ID") && !pk.isIdentity()) {
                    vioList.add("The primary key should be identity: " + table.getTableDbName() + "." + pk.getName());
                }
            }
        });
        themeMap.put("hasCommonColumn", (table, vioList) -> {
            if (!table.hasAllCommonColumn()) {
                vioList.add("The table should have common columns: " + table.getTableDbName());
            }
        });
    }

    // ===================================================================================
    //                                                                           Exception
    //                                                                           =========
    protected void throwSchemaPolicyCheckUnknownThemeException(String theme, String targetType) {
        _secretary.throwSchemaPolicyCheckUnknownThemeException(theme, targetType);
    }
}
