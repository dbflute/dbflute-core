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
package org.dbflute.logic.doc.spolicy.determiner;

import java.util.List;
import java.util.function.Function;

import org.apache.torque.engine.database.model.Column;
import org.apache.torque.engine.database.model.Database;
import org.apache.torque.engine.database.model.Table;
import org.dbflute.logic.doc.spolicy.DfSPolicyChecker;
import org.dbflute.logic.doc.spolicy.secretary.DfSPolicyMiscSecretary;

/**
 * @author jflute
 * @since 1.1.8 (2018/5/2 Wednesday)
 */
public class DfSPolicyCrossDeterminer {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfSPolicyChecker _spolicyChecker;
    protected final DfSPolicyMiscSecretary _secretary = new DfSPolicyMiscSecretary();

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfSPolicyCrossDeterminer(DfSPolicyChecker spolicyChecker) {
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
    // whole style
    public String analyzeSameColumnAliasIfSameColumnName(Database database) {
        return doAnalyzeSameWhatIfSameColumnName(database, col -> determineSameColumnAliasIfSameColumnName(col));
    }

    public String analyzeSameColumnDbTypeIfSameColumnName(Database database) {
        return doAnalyzeSameWhatIfSameColumnName(database, col -> determineSameColumnDbTypeIfSameColumnName(col));
    }

    public String analyzeSameColumnSizeIfSameColumnName(Database database) {
        return doAnalyzeSameWhatIfSameColumnName(database, col -> determineSameColumnSizeIfSameColumnName(col));
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
    public String determineSameColumnAliasIfSameColumnName(Column myColumn) {
        final boolean ignoreEmpty = true; // alias existence should be checked by other process
        return determineSameWhatIfSameColumnName(myColumn, col -> col.getAlias(), ignoreEmpty);
    }

    public String determineSameColumnDbTypeIfSameColumnName(Column myColumn) {
        final boolean ignoreEmpty = false; // basically cannot be empty
        return determineSameWhatIfSameColumnName(myColumn, col -> col.getDbType(), ignoreEmpty);
    }

    public String determineSameColumnSizeIfSameColumnName(Column myColumn) {
        final boolean ignoreEmpty = false; // basically cannot be empty
        return determineSameWhatIfSameColumnName(myColumn, col -> col.getColumnSize(), ignoreEmpty);
    }

    protected String determineSameWhatIfSameColumnName(Column myColumn, Function<Column, Object> valueProvider, boolean ignoreEmpty) {
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
    // whole style
    public String analyzeSameColumnNameIfSameColumnAlias(Database database) {
        return doAnalyzeSameWhatIfSameColumnAlias(database, col -> determineSameColumnNameIfSameColumnAlias(col));
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
    public String determineSameColumnNameIfSameColumnAlias(Column myColumn) {
        final boolean ignoreEmpty = false; // basically cannot be empty
        return determineSameWhatIfSameColumnAlias(myColumn, col -> col.getName(), ignoreEmpty);
    }

    protected String determineSameWhatIfSameColumnAlias(Column myColumn, Function<Column, Object> valueProvider, boolean ignoreEmpty) {
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
        return _spolicyChecker.isTargetTable(table);
    }

    protected boolean isTargetColumn(Column column) {
        return _spolicyChecker.isTargetColumn(column);
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
}
