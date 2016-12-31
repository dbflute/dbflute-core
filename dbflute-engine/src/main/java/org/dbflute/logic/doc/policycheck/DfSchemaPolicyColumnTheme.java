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
package org.dbflute.logic.doc.policycheck;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

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
    protected static Map<String, BiConsumer<Column, DfSchemaPolicyResult>> _cachedThemeMap;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfSchemaPolicyMiscSecretary _secretary = new DfSchemaPolicyMiscSecretary();

    // ===================================================================================
    //                                                                        Column Theme
    //                                                                        ============
    public void checkColumnTheme(Table table, Map<String, Object> columnMap, DfSchemaPolicyResult result) {
        processColumnTheme(table, columnMap, result);
    }

    protected void processColumnTheme(Table table, Map<String, Object> columnMap, DfSchemaPolicyResult result) {
        @SuppressWarnings("unchecked")
        final List<String> themeList = (List<String>) columnMap.get("themeList");
        if (themeList == null) {
            return;
        }
        for (String theme : themeList) {
            evaluateColumnTheme(table, result, theme);
        }
    }

    protected void evaluateColumnTheme(Table table, DfSchemaPolicyResult result, String theme) {
        final Map<String, BiConsumer<Column, DfSchemaPolicyResult>> themeMap = getThemeMap();
        final List<Column> columnList = table.getColumnList();
        for (Column column : columnList) {
            final BiConsumer<Column, DfSchemaPolicyResult> themeProcessor = themeMap.get(theme);
            if (themeProcessor != null) {
                themeProcessor.accept(column, result);
            } else {
                throwSchemaPolicyCheckUnknownThemeException(theme, "Column");
            }
        }
    }

    protected Map<String, BiConsumer<Column, DfSchemaPolicyResult>> getThemeMap() {
        if (_cachedThemeMap != null) {
            return _cachedThemeMap;
        }
        final Map<String, BiConsumer<Column, DfSchemaPolicyResult>> themeMap = StringKeyMap.createAsCaseInsensitiveOrdered();
        prepareColumnTheme(themeMap);
        _cachedThemeMap = themeMap;
        return _cachedThemeMap;
    }

    // ===================================================================================
    //                                                                         Theme Logic
    //                                                                         ===========
    protected void prepareColumnTheme(Map<String, BiConsumer<Column, DfSchemaPolicyResult>> themeMap) {
        // e.g.
        // ; columnMap = map:{
        //     ; themeList = list:{ upperCaseBasis }
        // }
        define(themeMap, "upperCaseBasis", column -> Srl.isLowerCaseAny(buildCaseComparingColumnName(column)), column -> {
            return "The column name should be on upper case basis: " + toColumnDisp(column);
        });
        define(themeMap, "lowerCaseBasis", column -> Srl.isUpperCaseAny(buildCaseComparingColumnName(column)), column -> {
            return "The column name should be on lower case basis: " + toColumnDisp(column);
        });
    }

    protected void define(Map<String, BiConsumer<Column, DfSchemaPolicyResult>> themeMap, String theme, Predicate<Column> determiner,
            Function<Column, String> messenger) {
        themeMap.put(theme, (column, result) -> {
            if (determiner.test(column)) {
                result.addViolation("column.theme: " + theme, messenger.apply(column));
            }
        });
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    protected String buildCaseComparingColumnName(Column column) {
        return _secretary.buildCaseComparingColumnName(column);
    }

    protected String toColumnDisp(Column column) {
        return _secretary.toColumnDisp(column);
    }

    // ===================================================================================
    //                                                                           Exception
    //                                                                           =========
    protected void throwSchemaPolicyCheckUnknownThemeException(String theme, String targetType) {
        _secretary.throwSchemaPolicyCheckUnknownThemeException(theme, targetType);
    }
}
