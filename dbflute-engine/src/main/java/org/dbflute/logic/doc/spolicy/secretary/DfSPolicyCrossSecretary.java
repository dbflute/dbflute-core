/*
 * Copyright 2014-2018 the original author or authors.
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
package org.dbflute.logic.doc.spolicy.secretary;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.torque.engine.database.model.Column;
import org.apache.torque.engine.database.model.Database;
import org.apache.torque.engine.database.model.Table;
import org.dbflute.logic.doc.spolicy.DfSPolicyChecker;

/**
 * @author jflute
 * @since 1.1.8 (2018/5/2 Wednesday)
 */
public class DfSPolicyCrossSecretary {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfSPolicyChecker _spolicyChecker;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfSPolicyCrossSecretary(DfSPolicyChecker spolicyChecker) {
        _spolicyChecker = spolicyChecker;
    }

    // ===================================================================================
    //                                                                        Unique Table
    //                                                                        ============
    public String analyzeUniqueTableAlias(Database database) {
        return determineUniqueTableWhat(database, table -> table.getAlias());
    }

    protected String determineUniqueTableWhat(Database database, Function<Table, Object> valueProvider) {
        final List<Table> myTableList = database.getTableList();
        for (Table myTable : myTableList) {
            if (!isTargetTable(myTable)) {
                continue;
            }
            for (Table yourTable : myTableList) {
                if (!isTargetTable(yourTable)) {
                    continue;
                }
                if (myTable.equals(yourTable)) { // myself
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

    // ===================================================================================
    //                                                                 if same Column Name
    //                                                                 ===================
    // -----------------------------------------------------
    //                                              Analyzer
    //                                              --------
    // whole style (no your targeting)
    public String analyzeSameColumnAliasIfSameColumnName(Database database) {
        return doAnalyzeSameWhatIfSameColumnName(database, col -> determineSameColumnAliasIfSameColumnName(col, your -> true));
    }

    public String analyzeSameColumnDbTypeIfSameColumnName(Database database) {
        return doAnalyzeSameWhatIfSameColumnName(database, col -> determineSameColumnDbTypeIfSameColumnName(col, your -> true));
    }

    public String analyzeSameColumnSizeIfSameColumnName(Database database) {
        return doAnalyzeSameWhatIfSameColumnName(database, col -> determineSameColumnSizeIfSameColumnName(col, your -> true));
    }

    protected String doAnalyzeSameWhatIfSameColumnName(Database database, Function<Column, String> determiner) {
        final List<Table> tableList = database.getTableList();
        for (Table myTable : tableList) {
            if (!isTargetTable(myTable)) { // non-target
                continue;
            }
            final List<Column> myColumnList = myTable.getColumnList();
            for (Column myColumn : myColumnList) {
                if (!isTargetColumn(myColumn)) { // non-target
                    continue;
                }
                final String violation = determiner.apply(myColumn);
                if (violation != null) {
                    return violation;
                }
            }
        }
        return null;
    }

    // -----------------------------------------------------
    //                                            Determiner
    //                                            ----------
    // called by column statement
    public String determineSameColumnAliasIfSameColumnName(Column myColumn, Predicate<Column> yourTargeting) {
        final boolean ignoreEmpty = true; // alias existence should be checked by other process
        return determineSameWhatIfSameColumnName(myColumn, yourTargeting, col -> col.getAlias(), ignoreEmpty);
    }

    public String determineSameColumnDbTypeIfSameColumnName(Column myColumn, Predicate<Column> yourTargeting) {
        final boolean ignoreEmpty = false; // basically cannot be empty
        return determineSameWhatIfSameColumnName(myColumn, yourTargeting, col -> col.getDbType(), ignoreEmpty);
    }

    public String determineSameColumnSizeIfSameColumnName(Column myColumn, Predicate<Column> yourTargeting) {
        final boolean ignoreEmpty = false; // basically cannot be empty
        return determineSameWhatIfSameColumnName(myColumn, yourTargeting, col -> col.getColumnSize(), ignoreEmpty);
    }

    protected String determineSameWhatIfSameColumnName(Column myColumn, Predicate<Column> yourTargeting,
            Function<Column, Object> valueProvider, boolean ignoreEmpty) {
        final Table myTable = myColumn.getTable();
        for (Table yourTable : myTable.getDatabase().getTableList()) {
            if (!isTargetTable(yourTable)) { // non-target
                continue;
            }
            if (myTable.equals(yourTable)) { // myself
                continue;
            }
            final String myColumnName = myColumn.getName();
            final Column yourColumn = yourTable.getColumn(myColumnName);
            if (yourColumn != null) {
                if (!isTargetColumn(yourColumn)) { // non-target
                    continue;
                }
                if (!yourTargeting.test(yourColumn)) { // non-target (e.g. by statement)
                    continue;
                }
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
        return null;
    }

    // ===================================================================================
    //                                                                if same Column Alias
    //                                                                ====================
    // -----------------------------------------------------
    //                                              Analyzer
    //                                              --------
    // whole style (no your targeting)
    public String analyzeSameColumnNameIfSameColumnAlias(Database database) {
        return doAnalyzeSameWhatIfSameColumnAlias(database, col -> determineSameColumnNameIfSameColumnAlias(col, your -> true));
    }

    protected String doAnalyzeSameWhatIfSameColumnAlias(Database database, Function<Column, String> determiner) {
        final List<Table> tableList = database.getTableList();
        for (Table myTable : tableList) {
            if (!isTargetTable(myTable)) { // non-target
                continue;
            }
            final List<Column> myColumnList = myTable.getColumnList();
            for (Column myColumn : myColumnList) {
                if (!isTargetColumn(myColumn)) { // non-target
                    continue;
                }
                final String violation = determiner.apply(myColumn);
                if (violation != null) {
                    return violation;
                }
            }
        }
        return null;
    }

    // -----------------------------------------------------
    //                                            Determiner
    //                                            ----------
    // may be called by column statement
    public String determineSameColumnNameIfSameColumnAlias(Column myColumn, Predicate<Column> yourTargeting) {
        final boolean ignoreEmpty = false; // basically cannot be empty
        return determineSameWhatIfSameColumnAlias(myColumn, yourTargeting, col -> col.getName(), ignoreEmpty);
    }

    protected String determineSameWhatIfSameColumnAlias(Column myColumn, Predicate<Column> yourTargeting,
            Function<Column, Object> valueProvider, boolean ignoreEmpty) {
        if (!myColumn.hasAlias()) { // cannot determine
            return null;
        }
        final String myColumnAlias = myColumn.getAlias();
        final Table myTable = myColumn.getTable();
        for (Table yourTable : myTable.getDatabase().getTableList()) {
            if (!isTargetTable(yourTable)) { // non-target
                continue;
            }
            if (myTable.equals(yourTable)) { // myself
                continue;
            }
            final List<Column> yourColumnList = yourTable.getColumnList();
            for (Column yourColumn : yourColumnList) {
                if (!isTargetColumn(yourColumn)) { // non-target
                    continue;
                }
                if (!yourTargeting.test(yourColumn)) { // non-target (e.g. by statement)
                    continue;
                }
                if (!yourColumn.hasAlias()) { // cannot determine
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
        return null;
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    protected boolean isTargetTable(Table table) {
        return _spolicyChecker.getExceptTargetSecretary().isTargetTable(table);
    }

    protected boolean isTargetColumn(Column column) {
        return _spolicyChecker.getExceptTargetSecretary().isTargetColumn(column);
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
        return _spolicyChecker.getLogicalSecretary().toTableDisp(table);
    }

    protected String toColumnExp(Column column) { // simple for comparing
        return column.getTable().getTableDispName() + "." + column.getName();
    }
}
