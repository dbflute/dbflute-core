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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.torque.engine.database.model.Column;
import org.apache.torque.engine.database.model.Database;
import org.apache.torque.engine.database.model.Table;
import org.dbflute.helper.StringKeyMap;
import org.dbflute.logic.doc.spolicy.result.DfSPolicyResult;
import org.dbflute.logic.doc.spolicy.secretary.DfSPolicyMiscSecretary;

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
    protected final DfSPolicyChecker _spolicyChecker;
    protected final DfSPolicyMiscSecretary _secretary = new DfSPolicyMiscSecretary();

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfSPolicyWholeThemeChecker(DfSPolicyChecker spolicyChecker) {
        _spolicyChecker = spolicyChecker;
    }

    // ===================================================================================
    //                                                                         Whole Theme
    //                                                                         ===========
    public void checkTableTheme(List<String> themeList, DfSPolicyResult result, Database database) {
        for (String theme : themeList) {
            evaluateTableTheme(theme, result, database);
        }
    }

    protected void evaluateTableTheme(String theme, DfSPolicyResult result, Database database) {
        final Map<String, BiConsumer<Database, DfSPolicyResult>> themeMap = getThemeMap();
        final BiConsumer<Database, DfSPolicyResult> themeProcessor = themeMap.get(theme);
        if (themeProcessor != null) {
            themeProcessor.accept(database, result);
        } else {
            throwSchemaPolicyCheckUnknownThemeException(theme, "Schema");
        }
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
    //                                                                         Theme Logic
    //                                                                         ===========
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
            return analyzeSameSizeColumnIfSameColumnName(database);
        }, violation -> {
            return "The column size should be same if column name is same: " + violation;
        });
        define(themeMap, "sameColumnNameIfSameColumnAlias", database -> {
            return analyzeSameColumnNameIfSameColumnAlias(database);
        }, violation -> {
            return "The column alias should be same if column name is same: " + violation;
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
    //                                                                         Whole Theme
    //                                                                         ===========
    // -----------------------------------------------------
    //                                          Unique Table
    //                                          ------------
    protected String analyzeUniqueTableAlias(Database database) {
        return determineUniqueTableWhat(database, table -> table.getAlias());
    }

    protected String determineUniqueTableWhat(Database database, Function<Table, Object> valueProvider) {
        final List<Table> myTableList = toTableList(database);
        for (Table myTable : myTableList) {
            for (Table yourTable : myTableList) {
                if (myTable.equals(yourTable)) {
                    continue;
                }
                final Object myValue = valueProvider.apply(myTable);
                final Object yourValue = valueProvider.apply(yourTable);
                if (eitherEmpty(myValue, yourValue)) { // except non-defined value
                    continue;
                }
                if (isEqual(myValue, yourValue)) {
                    return toTableDisp(myTable) + "=" + myValue + ", " + toTableDisp(yourTable) + "=" + yourValue;
                }
            }
        }
        return null;
    }

    // -----------------------------------------------------
    //                                   if same Column Name
    //                                   -------------------
    protected String analyzeSameColumnAliasIfSameColumnName(Database database) {
        final boolean ignoreEmpty = true; // alias existence should be checked by other process
        return determineSameWhatIfSameColumnName(database, column -> column.getAlias(), ignoreEmpty);
    }

    protected String analyzeSameColumnDbTypeIfSameColumnName(Database database) {
        return determineSameWhatIfSameColumnName(database, column -> column.getDbType(), false);
    }

    protected String analyzeSameSizeColumnIfSameColumnName(Database database) {
        return determineSameWhatIfSameColumnName(database, column -> column.getColumnSize(), false);
    }

    protected String determineSameWhatIfSameColumnName(Database database, Function<Column, Object> valueProvider, boolean ignoreEmpty) {
        final List<Table> myTableList = toTableList(database);
        for (Table myTable : myTableList) {
            final List<Column> myColumnList = myTable.getColumnList();
            for (Column myColumn : myColumnList) {
                for (Table yourTable : myTableList) {
                    if (myTable.equals(yourTable)) {
                        continue;
                    }
                    final String myColumnName = myColumn.getName();
                    final Column yourColumn = yourTable.getColumn(myColumnName);
                    if (yourColumn != null) {
                        final Object myValue = valueProvider.apply(myColumn);
                        final Object yourValue = valueProvider.apply(yourColumn);
                        if (ignoreEmpty && eitherEmpty(myValue, yourValue)) {
                            continue;
                        }
                        if (!isEqual(myValue, yourValue)) { // different in spite of same column name
                            return toColumnExp(myColumn) + "=" + myValue + ", " + toColumnExp(yourColumn) + "=" + yourValue;
                        }
                    }
                }
            }
        }
        return null;
    }

    // -----------------------------------------------------
    //                                  if same Column Alias
    //                                  --------------------
    protected String analyzeSameColumnNameIfSameColumnAlias(Database database) {
        final boolean ignoreEmpty = true; // alias existence should be checked by other process
        return determineSameWhatIfSameColumnAlias(database, column -> column.getAlias(), ignoreEmpty);
    }

    protected String determineSameWhatIfSameColumnAlias(Database database, Function<Column, Object> valueProvider, boolean ignoreEmpty) {
        final List<Table> myTableList = toTableList(database);
        for (Table myTable : myTableList) {
            final List<Column> myColumnList = myTable.getColumnList();
            for (Column myColumn : myColumnList) {
                if (!myColumn.hasAlias()) {
                    continue;
                }
                for (Table yourTable : myTableList) {
                    if (myTable.equals(yourTable)) {
                        continue;
                    }
                    final String myColumnAlias = myColumn.getAlias();
                    final List<Column> yourColumnList = yourTable.getColumnList();
                    for (Column yourColumn : yourColumnList) {
                        if (!yourColumn.hasAlias()) {
                            continue;
                        }
                        if (myColumnAlias.equals(yourColumn.getAlias())) { // same column alias
                            final Object myValue = valueProvider.apply(myColumn);
                            final Object yourValue = valueProvider.apply(yourColumn);
                            if (ignoreEmpty && eitherEmpty(myValue, yourValue)) {
                                continue;
                            }
                            if (!isEqual(myValue, yourValue)) { // different in spite of same column alias
                                return toColumnExp(myColumn) + "=" + myValue + ", " + toColumnExp(yourColumn) + "=" + yourValue;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    protected List<Table> toTableList(Database database) {
        final List<Table> filteredTableList = new ArrayList<Table>();
        for (Table table : database.getTableList()) {
            if (_spolicyChecker.isTargetTable(table)) {
                filteredTableList.add(table);
            }
        }
        return filteredTableList;
    }

    protected boolean eitherEmpty(Object myValue, Object yourValue) {
        if (myValue instanceof String && ((String) myValue).isEmpty()) {
            return true;
        }
        if (yourValue instanceof String && ((String) yourValue).isEmpty()) {
            return true;
        }
        return false;
    }

    protected boolean isEqual(Object myValue, Object yourValue) { // considering null
        return (myValue == null && yourValue == null) || (myValue != null && myValue.equals(yourValue));
    }

    protected String toTableDisp(Table table) {
        return _secretary.toTableDisp(table);
    }

    protected String toColumnExp(Column column) { // simple for comparing
        return column.getTable().getTableDispName() + "." + column.getName();
    }

    // ===================================================================================
    //                                                                           Exception
    //                                                                           =========
    protected void throwSchemaPolicyCheckUnknownThemeException(String theme, String targetType) {
        _secretary.throwSchemaPolicyCheckUnknownThemeException(theme, targetType);
    }
}
