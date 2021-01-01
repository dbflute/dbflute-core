/*
 * Copyright 2014-2021 the original author or authors.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.torque.engine.database.model.Column;
import org.apache.torque.engine.database.model.Database;
import org.apache.torque.engine.database.model.Table;
import org.dbflute.DfBuildProperties;
import org.dbflute.helper.StringKeyMap;
import org.dbflute.properties.DfBasicProperties;
import org.dbflute.util.DfCollectionUtil;

/**
 * @author jflute
 * @since 1.1.8 (2018/5/2 Wednesday)
 */
public class DfSPolicyCrossSecretary {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfSPolicyExceptTargetSecretary _exceptTargetSecretary;
    protected final DfSPolicyLogicalSecretary _logicalSecretary;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfSPolicyCrossSecretary(DfSPolicyExceptTargetSecretary exceptTargetSecretary, DfSPolicyLogicalSecretary logicalSecretary) {
        _exceptTargetSecretary = exceptTargetSecretary;
        _logicalSecretary = logicalSecretary;
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
        final Map<String, List<Column>> columnListMap = getSameNameColumnListMap(database);
        for (List<Column> columnList : columnListMap.values()) {
            for (Column myColumn : columnList) {
                final String violation = determiner.apply(myColumn);
                if (violation != null) {
                    return violation;
                }
            }
        }
        return null;

        // memorable code before performance tuning
        //final List<Table> tableList = database.getTableList();
        //for (Table myTable : tableList) {
        //    if (!isTargetTable(myTable)) { // non-target
        //        continue;
        //    }
        //    final List<Column> myColumnList = myTable.getColumnList();
        //    for (Column myColumn : myColumnList) {
        //        if (!isTargetColumn(myColumn)) { // non-target
        //            continue;
        //        }
        //        final String violation = determiner.apply(myColumn);
        //        if (violation != null) {
        //            return violation;
        //        }
        //    }
        //}
        //return null;
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
        final Predicate<? super Column> forcedTargeting = col -> { // for both my and your
            // serial type can be referred by e.g. integer column so normally it has difference
            // and quit too complex comparing logic with other many number types so simply ignore
            return !col.isDbTypePostgreSQLSerialFamily();
        };
        if (!forcedTargeting.test(myColumn)) {
            return null; // out of target
        }
        return determineSameWhatIfSameColumnName(myColumn, yourTargeting.and(forcedTargeting), col -> col.getDbType(), ignoreEmpty);
    }

    public String determineSameColumnSizeIfSameColumnName(Column myColumn, Predicate<Column> yourTargeting) {
        final boolean ignoreEmpty = false; // basically cannot be empty
        return determineSameWhatIfSameColumnName(myColumn, yourTargeting, col -> col.getColumnSize(), ignoreEmpty);
    }

    protected String determineSameWhatIfSameColumnName(Column myColumn, Predicate<Column> yourTargeting,
            Function<Column, Object> valueProvider, boolean ignoreEmpty) {
        final Map<String, List<Column>> columnListMap = getSameNameColumnListMap(myColumn.getTable().getDatabase());
        final List<Column> columnList = columnListMap.getOrDefault(myColumn.getName(), DfCollectionUtil.emptyList());
        for (Column yourColumn : columnList) {
            if (myColumn.equals(yourColumn)) { // myself
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
        return null; // no problem

        // memorable code before performance tuning
        //for (Table yourTable : myTable.getDatabase().getTableList()) {
        //    if (!isTargetTable(yourTable)) { // non-target
        //        continue;
        //    }
        //    if (myTable.equals(yourTable)) { // myself
        //        continue;
        //    }
        //    final String myColumnName = myColumn.getName();
        //    final Column yourColumn = yourTable.getColumn(myColumnName);
        //    if (yourColumn != null) {
        //        if (!isTargetColumn(yourColumn)) { // non-target
        //            continue;
        //        }
        //        if (!yourTargeting.test(yourColumn)) { // non-target (e.g. by statement)
        //            continue;
        //        }
        //        final Object myValue = valueProvider.apply(myColumn);
        //        final Object yourValue = valueProvider.apply(yourColumn);
        //        if (ignoreEmpty && eitherEmpty(myValue, yourValue)) {
        //            continue;
        //        }
        //        if (!isEqual(myValue, yourValue)) { // different in spite of same column name
        //            return toColumnExp(myColumn) + "=" + myValue + ", " + toColumnExp(yourColumn) + "=" + yourValue;
        //        }
        //    }
        //}
        //return null;
    }

    // -----------------------------------------------------
    //                                    Same-Name Resource
    //                                    ------------------
    protected Map<String, List<Column>> _sameNameColumnListMap; // map:{ column-name = list:{ column, ... } }

    protected Map<String, List<Column>> getSameNameColumnListMap(Database database) {
        if (_sameNameColumnListMap != null) {
            return _sameNameColumnListMap;
        }
        // flexible map because column name is treated as flexible in DBFlute (so MEMBER_NAME and MemberName are same-name)
        final Map<String, List<Column>> sameNameColumnListMap = StringKeyMap.createAsFlexible();
        final List<Table> targetTableList = database.getTableList().stream().filter(tbl -> isTargetTable(tbl)).collect(Collectors.toList());
        for (Table myTable : targetTableList) {
            final List<Column> columnList = myTable.getColumnList();
            for (Column myColumn : columnList) {
                if (!isTargetColumn(myColumn)) { // non-target
                    continue;
                }
                final String myName = myColumn.getName();
                if (sameNameColumnListMap.containsKey(myName)) { // registered by the other same-name column
                    continue;
                }
                for (Table yourTable : targetTableList) {
                    final Column yourColumn = yourTable.getColumn(myName);
                    if (yourColumn != null) {
                        if (!isTargetColumn(yourColumn)) { // non-target
                            continue;
                        }
                        List<Column> sameColumnList = sameNameColumnListMap.get(myName);
                        if (sameColumnList == null) {
                            sameColumnList = new ArrayList<Column>();
                            sameColumnList.add(myColumn);
                            sameNameColumnListMap.put(myName, sameColumnList);
                        }
                        sameColumnList.add(yourColumn);
                    }
                }
            }
        }
        _sameNameColumnListMap = sameNameColumnListMap;
        return _sameNameColumnListMap;
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
        final Map<String, List<Column>> columnListMap = getSameAliasColumnListMap(database);
        for (List<Column> columnList : columnListMap.values()) {
            for (Column myColumn : columnList) {
                final String violation = determiner.apply(myColumn);
                if (violation != null) {
                    return violation;
                }
            }
        }
        return null;

        // memorable code before performance tuning
        //final List<Table> tableList = database.getTableList();
        //for (Table myTable : tableList) {
        //    if (!isTargetTable(myTable)) { // non-target
        //        continue;
        //    }
        //    final List<Column> myColumnList = myTable.getColumnList();
        //    for (Column myColumn : myColumnList) {
        //        if (!isTargetColumn(myColumn)) { // non-target
        //            continue;
        //        }
        //        final String violation = determiner.apply(myColumn);
        //        if (violation != null) {
        //            return violation;
        //        }
        //    }
        //}
        //return null;
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
        if (!myColumn.hasAlias()) { // cannot determine (possible when from statement)
            return null;
        }
        final Map<String, List<Column>> columnListMap = getSameAliasColumnListMap(myColumn.getTable().getDatabase());
        final List<Column> columnList = columnListMap.getOrDefault(myColumn.getName(), DfCollectionUtil.emptyList());
        for (Column yourColumn : columnList) { // alias always exists here
            if (myColumn.equals(yourColumn)) { // myself
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
            if (!isEqual(myValue, yourValue)) { // different in spite of same column alias
                return toColumnExp(myColumn) + "=" + myValue + ", " + toColumnExp(yourColumn) + "=" + yourValue;
            }
        }
        return null;

        // memorable code before performance tuning
        //final String myColumnAlias = myColumn.getAlias();
        //final Table myTable = myColumn.getTable();
        //for (Table yourTable : myTable.getDatabase().getTableList()) {
        //    if (!isTargetTable(yourTable)) { // non-target
        //        continue;
        //    }
        //    if (myTable.equals(yourTable)) { // myself
        //        continue;
        //    }
        //    final List<Column> yourColumnList = yourTable.getColumnList();
        //    for (Column yourColumn : yourColumnList) {
        //        if (!isTargetColumn(yourColumn)) { // non-target
        //            continue;
        //        }
        //        if (!yourTargeting.test(yourColumn)) { // non-target (e.g. by statement)
        //            continue;
        //        }
        //        if (!yourColumn.hasAlias()) { // cannot determine
        //            continue;
        //        }
        //        if (myColumnAlias.equals(yourColumn.getAlias())) { // same column alias
        //            final Object myValue = valueProvider.apply(myColumn);
        //            final Object yourValue = valueProvider.apply(yourColumn);
        //            if (ignoreEmpty && eitherEmpty(myValue, yourValue)) {
        //                continue;
        //            }
        //            if (!isEqual(myValue, yourValue)) { // different in spite of same column alias
        //                return toColumnExp(myColumn) + "=" + myValue + ", " + toColumnExp(yourColumn) + "=" + yourValue;
        //            }
        //        }
        //    }
        //}
        //return null;
    }

    // -----------------------------------------------------
    //                                    Same-Name Resource
    //                                    ------------------
    protected Map<String, List<Column>> _sameAliasColumnListMap; // map:{ column-alias = list:{ column, ... } }

    protected Map<String, List<Column>> getSameAliasColumnListMap(Database database) {
        if (_sameAliasColumnListMap != null) {
            return _sameAliasColumnListMap;
        }
        // case insensitive (not flexible), alias handling rule does not exist in DBFlute
        // but flexible is hard to implement and small merit
        final Map<String, List<Column>> sameAliasColumnListMap = StringKeyMap.createAsCaseInsensitive();
        final List<Table> targetTableList = database.getTableList().stream().filter(tbl -> isTargetTable(tbl)).collect(Collectors.toList());
        for (Table myTable : targetTableList) {
            final List<Column> columnList = myTable.getColumnList();
            for (Column myColumn : columnList) {
                if (!isTargetColumn(myColumn)) { // non-target
                    continue;
                }
                if (!myColumn.hasAlias()) { // cannot determine
                    continue;
                }
                final String myAlias = myColumn.getAlias();
                if (sameAliasColumnListMap.containsKey(myAlias)) { // registered by the other same-name column
                    continue;
                }
                for (Table yourTable : targetTableList) {
                    List<Column> yourColumnList = yourTable.getColumnList();
                    Column yourColumn = null;
                    for (Column currentColumn : yourColumnList) {
                        if (currentColumn.hasAlias() && myAlias.equalsIgnoreCase(currentColumn.getAlias())) {
                            yourColumn = currentColumn;
                            break;
                        }
                    }
                    if (yourColumn != null) {
                        if (!isTargetColumn(yourColumn)) { // non-target
                            continue;
                        }
                        if (!yourColumn.hasAlias()) { // cannot determine
                            continue;
                        }
                        List<Column> sameColumnList = sameAliasColumnListMap.get(myAlias);
                        if (sameColumnList == null) {
                            sameColumnList = new ArrayList<Column>();
                            sameColumnList.add(myColumn);
                            sameAliasColumnListMap.put(myAlias, sameColumnList);
                        }
                        sameColumnList.add(yourColumn);
                    }
                }
            }
        }
        _sameAliasColumnListMap = sameAliasColumnListMap;
        return _sameAliasColumnListMap;
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    protected boolean isTargetTable(Table table) {
        return _exceptTargetSecretary.isTargetTable(table);
    }

    protected boolean isTargetColumn(Column column) {
        return _exceptTargetSecretary.isTargetColumn(column);
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
        return _logicalSecretary.toTableDisp(table);
    }

    protected String toColumnExp(Column column) { // simple for comparing
        return column.getTable().getTableDispName() + "." + column.getName();
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfBasicProperties getBasicProperties() {
        return DfBuildProperties.getInstance().getBasicProperties();
    }
}
