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
package org.dbflute.logic.doc.spolicy;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.torque.engine.database.model.Database;
import org.dbflute.helper.StringKeyMap;
import org.dbflute.logic.doc.spolicy.result.DfSPolicyResult;
import org.dbflute.logic.doc.spolicy.secretary.DfSPolicyCrossSecretary;
import org.dbflute.logic.doc.spolicy.secretary.DfSPolicyLogicalSecretary;

/**
 * @author jflute
 * @since 1.1.2 (2017/1/3 Tuesday)
 */
public class DfSPolicyWholeThemeChecker {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static Map<String, BiConsumer<Database, DfSPolicyResult>> _cachedThemeMap;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfSPolicyCrossSecretary _crossDeterminer;
    protected final DfSPolicyLogicalSecretary _logicalSecretary;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfSPolicyWholeThemeChecker(DfSPolicyCrossSecretary crossDeterminer, DfSPolicyLogicalSecretary logicalSecretary) {
        _crossDeterminer = crossDeterminer;
        _logicalSecretary = logicalSecretary;
    }

    // ===================================================================================
    //                                                                         Whole Theme
    //                                                                         ===========
    public void checkWholeTheme(List<String> themeList, DfSPolicyResult result, Database database) {
        for (String theme : themeList) {
            evaluateWholeTheme(theme, result, database);
        }
    }

    protected void evaluateWholeTheme(String theme, DfSPolicyResult result, Database database) {
        final Map<String, BiConsumer<Database, DfSPolicyResult>> themeMap = getThemeMap();
        final BiConsumer<Database, DfSPolicyResult> themeProcessor = themeMap.get(theme);
        if (themeProcessor != null) {
            themeProcessor.accept(database, result);
        } else {
            throwSchemaPolicyCheckUnknownThemeException(theme, "Schema");
        }
    }

    protected void throwSchemaPolicyCheckUnknownThemeException(String theme, String targetType) {
        _logicalSecretary.throwSchemaPolicyCheckUnknownThemeException(theme, targetType);
    }

    protected Map<String, BiConsumer<Database, DfSPolicyResult>> getThemeMap() {
        if (_cachedThemeMap != null) {
            return _cachedThemeMap;
        }
        final Map<String, BiConsumer<Database, DfSPolicyResult>> themeMap = StringKeyMap.createAsCaseInsensitiveOrdered();
        prepareTableTheme(themeMap);
        _cachedThemeMap = themeMap;
        return _cachedThemeMap;
    }

    // ===================================================================================
    //                                                                       Prepare Theme
    //                                                                       =============
    protected void prepareTableTheme(Map<String, BiConsumer<Database, DfSPolicyResult>> themeMap) {
        // e.g.
        // ; wholeMap = map:{
        //     ; themeList = list:{ uniqueTableAlias ; sameColumnAliasIfSameColumnName }
        // }
        define(themeMap, "uniqueTableAlias", database -> {
            return analyzeUniqueTableAlias(database);
        }, violation -> {
            return "The table alias should be unique in all tables: " + violation;
        });
        define(themeMap, "sameColumnAliasIfSameColumnName", database -> {
            return analyzeSameColumnAliasIfSameColumnName(database);
        }, violation -> {
            return "The column alias should be same if column name is same: " + violation;
        });
        define(themeMap, "sameColumnDbTypeIfSameColumnName", database -> {
            return analyzeSameColumnDbTypeIfSameColumnName(database);
        }, violation -> {
            return "The column db-type should be same if column name is same: " + violation;
        });
        define(themeMap, "sameColumnSizeIfSameColumnName", database -> {
            return analyzeSameColumnSizeIfSameColumnName(database);
        }, violation -> {
            return "The column size should be same if column name is same: " + violation;
        });
        define(themeMap, "sameColumnNameIfSameColumnAlias", database -> {
            return analyzeSameColumnNameIfSameColumnAlias(database);
        }, violation -> {
            return "The column name should be same if column alias is same: " + violation;
        });
    }

    protected void define(Map<String, BiConsumer<Database, DfSPolicyResult>> themeMap, String theme, Function<Database, String> determiner,
            Function<String, String> messenger) {
        themeMap.put(theme, (database, result) -> {
            final String violation = determiner.apply(database);
            if (violation != null) {
                result.violate("whole.theme: " + theme, messenger.apply(violation));
            }
        });
    }

    // ===================================================================================
    //                                                                       Analyze Theme
    //                                                                       =============
    // -----------------------------------------------------
    //                                          Unique Table
    //                                          ------------
    protected String analyzeUniqueTableAlias(Database database) {
        return _crossDeterminer.analyzeUniqueTableAlias(database);
    }

    // -----------------------------------------------------
    //                                   if same Column Name
    //                                   -------------------
    protected String analyzeSameColumnAliasIfSameColumnName(Database database) {
        return _crossDeterminer.analyzeSameColumnAliasIfSameColumnName(database);
    }

    protected String analyzeSameColumnDbTypeIfSameColumnName(Database database) {
        return _crossDeterminer.analyzeSameColumnDbTypeIfSameColumnName(database);
    }

    protected String analyzeSameColumnSizeIfSameColumnName(Database database) {
        return _crossDeterminer.analyzeSameColumnSizeIfSameColumnName(database);
    }

    // -----------------------------------------------------
    //                                  if same Column Alias
    //                                  --------------------
    protected String analyzeSameColumnNameIfSameColumnAlias(Database database) {
        return _crossDeterminer.analyzeSameColumnNameIfSameColumnAlias(database);
    }
}
