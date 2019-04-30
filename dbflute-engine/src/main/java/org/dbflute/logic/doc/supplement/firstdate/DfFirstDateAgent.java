/*
 * Copyright 2014-2019 the original author or authors.
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
package org.dbflute.logic.doc.supplement.firstdate;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.dbflute.helper.StringKeyMap;
import org.dbflute.logic.jdbc.schemadiff.DfColumnDiff;
import org.dbflute.logic.jdbc.schemadiff.DfSchemaDiff;
import org.dbflute.logic.jdbc.schemadiff.DfTableDiff;
import org.dbflute.optional.OptionalThing;

/**
 * @author jflute
 * @since 1.1.8 (2018/5/6 Sunday at bay maihama)
 */
public class DfFirstDateAgent {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Supplier<List<DfSchemaDiff>> _schemaDiffListSupplier;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfFirstDateAgent(Supplier<List<DfSchemaDiff>> schemaDiffListSupplier) {
        _schemaDiffListSupplier = schemaDiffListSupplier;
    }

    // ===================================================================================
    //                                                                             Finding
    //                                                                             =======
    public OptionalThing<Date> findTableFirstDate(String tableDbName) {
        return OptionalThing.ofNullable(getTableFirstDateMap().get(tableDbName), () -> {
            throw new IllegalStateException("Not found the first date of the table: " + tableDbName);
        });
    }

    public OptionalThing<Date> findColumnFirstDate(String tableDbName, String columnDbName) {
        final String columnKey = generateColumnKey(tableDbName, columnDbName);
        return OptionalThing.ofNullable(getColumnFirstDateMap().get(columnKey), () -> {
            throw new IllegalStateException("Not found the first date of the column: " + tableDbName + "." + columnDbName);
        });
    }

    // ===================================================================================
    //                                                                           Targeting
    //                                                                           =========
    // e.g. firstDate is after:2018/05/03
    public boolean isTableFirstDateAfter(String tableDbName, Date targetDate) {
        final Map<String, Date> tableFirstDateMap = getTableFirstDateMap();
        final Date firstDate = tableFirstDateMap.get(tableDbName);
        if (firstDate != null) {
            return firstDate.after(targetDate);
        } else { // no new difference
            return true; // treated as new table
        }
    }

    // e.g. columnFirstDate is after:2018/05/03
    public boolean isColumnFirstDateAfter(String tableDbName, String columnDbName, Date targetDate) {
        final Map<String, Date> columnFirstDateMap = getColumnFirstDateMap();
        final String columnKey = generateColumnKey(tableDbName, columnDbName);
        final Date firstDate = columnFirstDateMap.get(columnKey);
        if (firstDate != null) {
            return firstDate.after(targetDate);
        } else { // no new difference, means that it may be in new table difference
            final Map<String, Date> tableFirstDateMap = getTableFirstDateMap();
            final Date tableFirstDate = tableFirstDateMap.get(tableDbName);
            if (tableFirstDate != null) { // so use table first date
                return tableFirstDate.after(targetDate);
            } else {
                return true; // treated as new column
            }
        }
    }

    // ===================================================================================
    //                                                                     Cached Resource
    //                                                                     ===============
    // -----------------------------------------------------
    //                                      Table First Date
    //                                      ----------------
    protected Map<String, Date> _tableFirstDateMap;

    protected Map<String, Date> getTableFirstDateMap() { // case insensitive (not flexible because of historical changes)
        if (_tableFirstDateMap != null) {
            return _tableFirstDateMap;
        }
        final Map<String, Date> tableFirstDateMap = StringKeyMap.createAsCaseInsensitiveOrdered();
        final List<DfSchemaDiff> schemaDiffList = prepareSchemaDiffList();
        for (DfSchemaDiff schemaDiff : schemaDiffList) {
            List<DfTableDiff> tableDiffList = schemaDiff.getAddedTableDiffList();
            for (DfTableDiff tableDiff : tableDiffList) {
                final String tableName = tableDiff.getTableName();
                tableFirstDateMap.put(tableName, schemaDiff.getNativeDiffDate());
            }
        }
        _tableFirstDateMap = tableFirstDateMap;
        return _tableFirstDateMap;
    }

    // -----------------------------------------------------
    //                                     Column First Date
    //                                     -----------------
    protected Map<String, Date> _columnFirstDateMap; // key is table.column

    protected Map<String, Date> getColumnFirstDateMap() { // case insensitive (not flexible because of historical changes)
        if (_columnFirstDateMap != null) {
            return _columnFirstDateMap;
        }
        final Map<String, Date> columnFirstDateMap = StringKeyMap.createAsCaseInsensitiveOrdered();
        final List<DfSchemaDiff> schemaDiffList = prepareSchemaDiffList();
        for (DfSchemaDiff schemaDiff : schemaDiffList) {
            final List<DfTableDiff> tableDiffList = schemaDiff.getChangedTableDiffList();
            for (DfTableDiff tableDiff : tableDiffList) {
                final String tableName = tableDiff.getTableName();
                final List<DfColumnDiff> columnDiffList = tableDiff.getAddedColumnDiffList();
                for (DfColumnDiff columnDiff : columnDiffList) {
                    final String columnName = columnDiff.getColumnName();
                    final String keyName = generateColumnKey(tableName, columnName);
                    columnFirstDateMap.put(keyName, schemaDiff.getNativeDiffDate());
                }
            }
        }
        _columnFirstDateMap = columnFirstDateMap;
        return _columnFirstDateMap;
    }

    protected String generateColumnKey(String tableName, String columnName) {
        return tableName + "." + columnName;
    }

    // -----------------------------------------------------
    //                                            SchemaDiff
    //                                            ----------
    protected List<DfSchemaDiff> _schemaDiffList;

    protected List<DfSchemaDiff> prepareSchemaDiffList() {
        if (_schemaDiffList != null) {
            return _schemaDiffList;
        }
        _schemaDiffList = _schemaDiffListSupplier.get();
        return _schemaDiffList;
    }
}
