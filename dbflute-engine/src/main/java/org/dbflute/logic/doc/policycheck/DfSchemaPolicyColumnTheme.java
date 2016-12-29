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
public class DfSchemaPolicyColumnTheme {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static Map<String, BiConsumer<Column, List<String>>> _cachedThemeMap;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfSchemaPolicyMiscSecretary _assist = new DfSchemaPolicyMiscSecretary();

    // ===================================================================================
    //                                                                        Column Theme
    //                                                                        ============
    public void checkColumnTheme(Table table, Map<String, Object> columnMap, List<String> vioList) {
        processColumnTheme(table, columnMap, vioList);
    }

    protected void processColumnTheme(Table table, Map<String, Object> columnMap, List<String> vioList) {
        @SuppressWarnings("unchecked")
        final List<String> themeList = (List<String>) columnMap.get("themeList");
        if (themeList == null) {
            return;
        }
        for (String theme : themeList) {
            evaluateColumnTheme(table, vioList, theme);
        }
    }

    protected void evaluateColumnTheme(Table table, List<String> vioList, String theme) {
        final Map<String, BiConsumer<Column, List<String>>> themeMap = getThemeMap();
        final List<Column> columnList = table.getColumnList();
        for (Column column : columnList) {
            final BiConsumer<Column, List<String>> themeProcessor = themeMap.get(theme);
            if (themeProcessor != null) {
                themeProcessor.accept(column, vioList);
            } else {
                throwSchemaPolicyCheckUnknownThemeException(theme, "Column");
            }
        }
    }

    protected Map<String, BiConsumer<Column, List<String>>> getThemeMap() {
        if (_cachedThemeMap != null) {
            return _cachedThemeMap;
        }
        final Map<String, BiConsumer<Column, List<String>>> themeMap = StringKeyMap.createAsCaseInsensitiveOrdered();
        defineColumnTheme(themeMap);
        _cachedThemeMap = themeMap;
        return _cachedThemeMap;
    }

    // ===================================================================================
    //                                                                         Theme Logic
    //                                                                         ===========
    protected void defineColumnTheme(Map<String, BiConsumer<Column, List<String>>> themeMap) {
        // e.g.
        // ; columnMap = map:{
        //     ; themeList = list:{ upperCaseBasis }
        // }
        themeMap.put("upperCaseBasis", (column, vioList) -> {
            if (Srl.isLowerCaseAny(column.getColumnSqlName())) { // use SQL name because DB name may be control name
                vioList.add("The column name should be on upper case basis: " + toColumnDisp(column));
            }
        });
        themeMap.put("lowerCaseBasis", (column, vioList) -> {
            if (Srl.isUpperCaseAny(column.getColumnSqlName())) { // same reason
                vioList.add("The column name should be on lower case basis: " + toColumnDisp(column));
            }
        });
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    protected String toColumnDisp(Column column) {
        return _assist.toColumnDisp(column);
    }

    // ===================================================================================
    //                                                                           Exception
    //                                                                           =========
    protected void throwSchemaPolicyCheckUnknownThemeException(String theme, String targetType) {
        _assist.throwSchemaPolicyCheckUnknownThemeException(theme, targetType);
    }
}
