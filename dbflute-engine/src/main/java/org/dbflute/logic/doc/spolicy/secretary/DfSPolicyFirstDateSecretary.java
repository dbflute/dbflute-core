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

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.torque.engine.database.model.Column;
import org.apache.torque.engine.database.model.Table;
import org.dbflute.exception.DfSchemaPolicyCheckIllegalFirstDateException;
import org.dbflute.helper.HandyDate;
import org.dbflute.helper.StringKeyMap;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.logic.doc.spolicy.parsed.DfSPolicyStatement;
import org.dbflute.logic.jdbc.schemadiff.DfColumnDiff;
import org.dbflute.logic.jdbc.schemadiff.DfSchemaDiff;
import org.dbflute.logic.jdbc.schemadiff.DfTableDiff;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.1.8 (2018/5/3 Thursday)
 */
public class DfSPolicyFirstDateSecretary {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Supplier<List<DfSchemaDiff>> _schemaDiffListSupplier;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfSPolicyFirstDateSecretary(Supplier<List<DfSchemaDiff>> schemaDiffListSupplier) {
        _schemaDiffListSupplier = schemaDiffListSupplier;
    }

    // ===================================================================================
    //                                                                           Determine
    //                                                                           =========
    public boolean determineTableFirstDate(DfSPolicyStatement statement, String ifValue, boolean notIfValue, Table table) {
        return doDetermineColumnFirstDate(statement, ifValue, notIfValue, targetDate -> {
            return isTableFirstDateAfter(table, targetDate);
        });
    }

    public boolean determineColumnFirstDate(DfSPolicyStatement statement, String ifValue, boolean notIfValue, Column column) {
        return doDetermineColumnFirstDate(statement, ifValue, notIfValue, targetDate -> {
            return isColumnFirstDateAfter(column, targetDate);
        });
    }

    protected boolean doDetermineColumnFirstDate(DfSPolicyStatement statement, String ifValue, boolean notIfValue,
            Predicate<Date> determiner) {
        if (notIfValue) { // "is not" is unsupported for firstDate
            throwSchemaPolicyCheckIllegalFirstDateException(statement, ifValue);
            return false; // unreachable
        }
        if (ifValue.startsWith("after:")) { // e.g. if firstDate is after:2018/05/03
            final String dateExp = Srl.substringFirstRear(ifValue, "after:").trim();
            final Date targetDate = new HandyDate(dateExp).getDate();
            return determiner.test(targetDate);
        } else {
            // only "after:" supported because other patterns might not be needed
            // (and difficult logic needs because of no-existing firstDate tables)
            throwSchemaPolicyCheckIllegalFirstDateException(statement, ifValue);
            return false; // unreachable
        }
    }

    // ===================================================================================
    //                                                                           Targeting
    //                                                                           =========
    // e.g. firstDate is after:2018/05/03
    protected boolean isTableFirstDateAfter(Table table, Date targetDate) {
        final Map<String, Date> tableFirstDateMap = getTableFirstDateMap();
        final Date firstDate = tableFirstDateMap.get(table.getTableDbName());
        if (firstDate != null) {
            return firstDate.after(targetDate);
        } else { // no new difference
            return true; // treated as new table
        }
    }

    // e.g. columnFirstDate is after:2018/05/03
    protected boolean isColumnFirstDateAfter(Column column, Date targetDate) {
        final String tableDbName = column.getTable().getTableDbName();
        final Map<String, Date> columnFirstDateMap = getColumnFirstDateMap();
        final String columnKey = generateColumnKey(tableDbName, column.getName());
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
            final List<DfTableDiff> tableDiffList = schemaDiff.getAddedTableDiffList();
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

    // ===================================================================================
    //                                                                           Exception
    //                                                                           =========
    protected void throwSchemaPolicyCheckIllegalFirstDateException(DfSPolicyStatement statement, String ifValue) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the valid prefix of firstDate for SchemaPolicyCheck.");
        br.addItem("Advice");
        br.addElement("Make sure your schemaPolicyMap.dfprop.");
        br.addElement("Only limited patterns of firstDate can be used like this:");
        br.addElement("  (o):");
        br.addElement("    if firstDate is after:2018/05/03");
        br.addElement("  (x):");
        br.addElement("    if firstDate is not after:2018/05/03");
        br.addElement("    if firstDate is before:2018/05/03");
        br.addElement("    if firstDate is 2018/05/03");
        br.addItem("Statement");
        br.addElement(statement);
        br.addItem("If-Value");
        br.addElement(ifValue);
        final String msg = br.buildExceptionMessage();
        throw new DfSchemaPolicyCheckIllegalFirstDateException(msg);
    }
}
