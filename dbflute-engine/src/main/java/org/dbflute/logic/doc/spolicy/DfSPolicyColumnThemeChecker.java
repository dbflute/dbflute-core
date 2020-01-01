/*
 * Copyright 2014-2020 the original author or authors.
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
import org.dbflute.helper.StringKeyMap;
import org.dbflute.logic.doc.spolicy.result.DfSPolicyResult;
import org.dbflute.logic.doc.spolicy.secretary.DfSPolicyLogicalSecretary;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.1.2 (2016/12/29 Thursday at higashi-ginza)
 */
public class DfSPolicyColumnThemeChecker {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static Map<String, BiConsumer<Column, DfSPolicyResult>> _cachedThemeMap;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfSPolicyLogicalSecretary _logicalSecretary;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfSPolicyColumnThemeChecker(DfSPolicyLogicalSecretary logicalSecretary) {
        _logicalSecretary = logicalSecretary;
    }

    // ===================================================================================
    //                                                                        Column Theme
    //                                                                        ============
    public void checkColumnTheme(List<String> themeList, DfSPolicyResult result, Column column) {
        for (String theme : themeList) {
            evaluateColumnTheme(theme, result, column);
        }
    }

    protected void evaluateColumnTheme(String theme, DfSPolicyResult result, Column column) {
        final Map<String, BiConsumer<Column, DfSPolicyResult>> themeMap = getThemeMap();
        final BiConsumer<Column, DfSPolicyResult> themeProcessor = themeMap.get(theme);
        if (themeProcessor != null) {
            themeProcessor.accept(column, result);
        } else {
            throwSchemaPolicyCheckUnknownThemeException(theme, "Column");
        }
    }

    protected Map<String, BiConsumer<Column, DfSPolicyResult>> getThemeMap() {
        if (_cachedThemeMap != null) {
            return _cachedThemeMap;
        }
        final Map<String, BiConsumer<Column, DfSPolicyResult>> themeMap = StringKeyMap.createAsCaseInsensitiveOrdered();
        prepareColumnTheme(themeMap);
        _cachedThemeMap = themeMap;
        return _cachedThemeMap;
    }

    // ===================================================================================
    //                                                                         Theme Logic
    //                                                                         ===========
    protected void prepareColumnTheme(Map<String, BiConsumer<Column, DfSPolicyResult>> themeMap) {
        // e.g.
        // ; columnMap = map:{
        //     ; themeList = list:{ upperCaseBasis }
        // }
        define(themeMap, "upperCaseBasis", column -> Srl.isLowerCaseAny(toComparingColumnName(column)), column -> {
            return "The column name should be on upper case basis: " + toColumnDisp(column);
        });
        define(themeMap, "lowerCaseBasis", column -> Srl.isUpperCaseAny(toComparingColumnName(column)), column -> {
            return "The column name should be on lower case basis: " + toColumnDisp(column);
        });
        define(themeMap, "hasAlias", column -> !column.hasAlias(), column -> {
            return "The column should have column alias: " + toColumnDisp(column);
        });
        define(themeMap, "hasComment", column -> !column.hasComment(), column -> {
            return "The column should have column comment: " + toColumnDisp(column);
        });
    }

    protected void define(Map<String, BiConsumer<Column, DfSPolicyResult>> themeMap, String theme, Predicate<Column> determiner,
            Function<Column, String> messenger) {
        themeMap.put(theme, (column, result) -> {
            if (determiner.test(column)) {
                result.violate("column.theme: " + theme, messenger.apply(column));
            }
        });
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    protected String toComparingColumnName(Column column) {
        return _logicalSecretary.toComparingColumnName(column);
    }

    protected String toColumnDisp(Column column) {
        return _logicalSecretary.toColumnDisp(column);
    }

    // ===================================================================================
    //                                                                           Exception
    //                                                                           =========
    protected void throwSchemaPolicyCheckUnknownThemeException(String theme, String targetType) {
        _logicalSecretary.throwSchemaPolicyCheckUnknownThemeException(theme, targetType);
    }
}
