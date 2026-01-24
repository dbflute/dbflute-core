/*
 * Copyright 2014-2026 the original author or authors.
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
package org.dbflute.logic.replaceschema.takefinally.conventional;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.dbflute.exception.DfTakeFinallyAssertionFailureEmptyTableException;
import org.dbflute.exception.SQLFailureException;
import org.dbflute.helper.HandyDate;
import org.dbflute.helper.jdbc.context.DfSchemaSource;
import org.dbflute.helper.jdbc.facade.DfJdbcFacade;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.logic.doc.supplement.firstdate.DfFirstDateAgent;
import org.dbflute.logic.jdbc.metadata.basic.DfColumnExtractor;
import org.dbflute.logic.jdbc.metadata.basic.DfTableExtractor;
import org.dbflute.logic.jdbc.metadata.info.DfColumnMeta;
import org.dbflute.logic.jdbc.metadata.info.DfTableMeta;
import org.dbflute.logic.jdbc.schemadiff.DfSchemaDiff;
import org.dbflute.logic.replaceschema.process.DfAbstractRepsProcess;
import org.dbflute.properties.DfReplaceSchemaProperties;
import org.dbflute.properties.assistant.reps.DfConventionalTakeAssertMap;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 1.1.7 (2018/03/17 Saturday)
 */
public class DfConventionalTakeAsserter extends DfAbstractRepsProcess {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger _log = LoggerFactory.getLogger(DfConventionalTakeAsserter.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfSchemaSource _dataSource;
    protected final Supplier<String> _dispPropertiesProvider;
    protected final DfFirstDateAgent _firstDateAgent;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfConventionalTakeAsserter(DfSchemaSource dataSource, Supplier<String> dispPropertiesProvider,
            Supplier<List<DfSchemaDiff>> schemaDiffListSupplier) {
        _dataSource = dataSource;
        _dispPropertiesProvider = dispPropertiesProvider;
        _firstDateAgent = new DfFirstDateAgent(schemaDiffListSupplier);
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    public void assertConventionally() {
        final DfReplaceSchemaProperties repsProp = getReplaceSchemaProperties();
        final DfConventionalTakeAssertMap propMap = repsProp.getConventionalTakeAssertMap();
        if (propMap.isEmptyTableFailure()) {
            _log.info("...Checking conventional empty tables");
            if (propMap.isEmptyTableWorkableEnv()) {
                detectEmptyTable(propMap);
            } else {
                _log.info(" => out of target environment so do nothing: currentEnv=" + repsProp.getRepsEnvType());
            }
        }
        if (propMap.isNullOnlyColumnFailure()) {
            if (propMap.isNullOnlyColumnWorkableEnv()) {
                detectNullOnlyColumn(propMap); // @since 1.3.2
            } else {
                _log.info(" => out of target environment so do nothing: currentEnv=" + repsProp.getRepsEnvType());
            }
        }
    }

    // ===================================================================================
    //                                                                         Empty Table
    //                                                                         ===========
    protected void detectEmptyTable(DfConventionalTakeAssertMap propMap) {
        final List<DfTableMeta> allTableList = extractTableList();
        final List<DfTableMeta> emptyTableList = DfCollectionUtil.newArrayList();
        final Date tableFirstDate = propMap.getEmptyTableErrorIfFirstDateAfter(); // null allowed
        final boolean frameworkDebug = propMap.isEmptyTableFrameworkDebug();
        if (tableFirstDate != null) {
            _log.info("...Using first-date for empty table: tableFirstDate=" + new HandyDate(tableFirstDate));
        }
        for (DfTableMeta tableMeta : allTableList) {
            if (!propMap.isEmptyTableTarget(tableMeta.getTableDbName())) {
                continue;
            }
            if (tableFirstDate != null && !isTableFirstDateAfter(tableMeta, tableFirstDate)) { // old table
                if (frameworkDebug) {
                    _log.debug("...Skipping the table for empty table by first-date: old-table=" + tableMeta);
                }
                continue;
            }
            if (determineEmptyTable(tableMeta)) {
                emptyTableList.add(tableMeta); // bad
            }
        }
        if (!emptyTableList.isEmpty()) {
            throwTakeFinallyAssertionFailureEmptyTableException(propMap, emptyTableList, tableFirstDate);
        }
    }

    protected boolean determineEmptyTable(DfTableMeta tableMeta) {
        final DfJdbcFacade facade = new DfJdbcFacade(_dataSource);
        final int countAll = facade.selectCountAll(tableMeta.getTableSqlName());
        return countAll == 0;
    }

    protected void throwTakeFinallyAssertionFailureEmptyTableException(DfConventionalTakeAssertMap propMap,
            List<DfTableMeta> emptyTableList, Date tableFirstDate) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Found the empty table (no-data) after ReplaceSchema.");
        br.addItem("Advice");
        br.addElement("The tables should have at least one record");
        br.addElement("by conventionalTakeAssertMap settings in replaceSchemaMap.dfprop:");
        br.addElement("");
        br.addElement(Srl.indent(2, _dispPropertiesProvider.get()));
        br.addElement("");
        br.addElement("So prepare the data (e.g. tsv or xls) for ReplaceSchema.");
        br.addElement("");
        br.addElement("  playsql");
        br.addElement("    |-data");
        br.addElement("    |   |-common");
        br.addElement("    |   |   |-tsv");
        br.addElement("    |   |   |-xls");
        br.addElement("    |   |-ut");
        br.addElement("    |   |   |-tsv");
        br.addElement("    |   |   |-xls");
        br.addElement("    ...");
        br.addElement("");
        br.addElement("Or adjust dfprop settings, for example,");
        br.addElement("you can except the table if it cannot be help.");
        br.addElement("(Of course, do after you ask your friends developing together)");
        br.addItem("Empty Table");
        for (DfTableMeta tableMeta : emptyTableList) {
            br.addElement(tableMeta.getTableDispName());
        }
        br.addItem("errorIfFirstDateAfter");
        br.addElement(tableFirstDate != null ? new HandyDate(tableFirstDate) : null);
        final String errorMessage = propMap.getEmptyTableErrorMessage();
        if (Srl.is_NotNull_and_NotTrimmedEmpty(errorMessage)) {
            br.addItem("Project Message");
            br.addElement(errorMessage);
        }
        final String msg = br.buildExceptionMessage();
        throw new DfTakeFinallyAssertionFailureEmptyTableException(msg);
    }

    // ===================================================================================
    //                                                                     nullOnly Column
    //                                                                     ===============
    protected void detectNullOnlyColumn(DfConventionalTakeAssertMap propMap) {
        final List<DfTableMeta> allTableList = extractTableList();
        final Date tableFirstDate = propMap.getNullOnlyColumnErrorIfTableFirstDateAfter(); // null allowed
        if (tableFirstDate != null) {
            _log.info("...Using table first-date for nullOnly columns: tableFirstDate=" + new HandyDate(tableFirstDate));
        }
        final Date columnFirstDate = propMap.getNullOnlyColumnErrorIfColumnFirstDateAfter(); // null allowed
        if (columnFirstDate != null) {
            _log.info("...Using column first-date for nullOnly columns: columnFirstDate=" + new HandyDate(columnFirstDate));
        }
        final boolean frameworkDebug = propMap.isNullOnlyColumnFrameworkDebug();
        final boolean skipIfEmptyTable = propMap.isSkipIfEmptyTable();
        final List<DfColumnMeta> emptyTableColumnList = DfCollectionUtil.newArrayList();
        final List<DfColumnMeta> nullOnlyColumnList = DfCollectionUtil.newArrayList();
        for (DfTableMeta tableMeta : allTableList) {
            if (!propMap.isNullOnlyColumnTarget(tableMeta.getTableDbName())) {
                continue;
            }
            if (tableFirstDate != null && !isTableFirstDateAfter(tableMeta, tableFirstDate)) { // old table
                if (frameworkDebug) {
                    _log.debug("...Skipping the table for nullOnly column by first-date: old-table=" + tableMeta);
                }
                continue;
            }
            final boolean emptyTable = determineEmptyTable(tableMeta);
            final List<DfColumnMeta> columnList = extractColumnList(tableMeta);
            for (DfColumnMeta columnMeta : columnList) {
                if (columnMeta.isRequired()) {
                    continue; // no related
                }
                // null-allowed column here
                if (columnFirstDate != null && !isColumnFirstDateAfter(columnMeta, columnFirstDate)) { // old column
                    if (frameworkDebug) {
                        _log.debug("...Skipping the column for nullOnly column by first-date: old-column=" + columnMeta);
                    }
                    continue;
                }
                if (emptyTable) {
                    if (skipIfEmptyTable) {
                        if (frameworkDebug) {
                            _log.debug("...Skipping the column for nullOnly column by empty table: old-column=" + columnMeta);
                        }
                    } else {
                        emptyTableColumnList.add(columnMeta); // bad
                    }
                } else {
                    if (determineNullOnlyColumn(tableMeta, columnMeta)) {
                        nullOnlyColumnList.add(columnMeta); // bad
                    }
                }
            }
        }
        if (!emptyTableColumnList.isEmpty() || !nullOnlyColumnList.isEmpty()) {
            throwTakeFinallyAssertionFailureNullOnlyColumnException(propMap, emptyTableColumnList, nullOnlyColumnList, tableFirstDate,
                    columnFirstDate);
        }
    }

    protected boolean determineNullOnlyColumn(DfTableMeta tableMeta, DfColumnMeta columnMeta) {
        // #for_now jflute if too many null-allowed columns exist, performance cost may be occurred (2026/01/24)
        final DfJdbcFacade facade = new DfJdbcFacade(_dataSource);
        final String table = tableMeta.getTableSqlName();
        final String column = columnMeta.getColumnSqlName();
        if (getBasicProperties().isDatabaseMySQL()) {
            // for performance, effective? but just in case (2026/01/23)
            final String alias = "existing";
            final String sql = "select 1 as " + alias + " from " + table + " where " + column + " is not null limit 1";
            final List<String> columnList = DfCollectionUtil.newArrayList(alias);
            final List<Map<String, String>> resultList = facade.selectStringList(sql, columnList);
            return !resultList.isEmpty();
        } else {
            final String sql = "select count(*) as cnt from " + table + " where " + column + " is not null";
            final int notNullCount = facade.selectCount(sql);
            return notNullCount == 0;
        }
    }

    protected void throwTakeFinallyAssertionFailureNullOnlyColumnException(DfConventionalTakeAssertMap propMap,
            List<DfColumnMeta> emptyTableColumnList, List<DfColumnMeta> nullOnlyColumnList, Date tableFirstDate, Date columnFirstDate) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Found the null-only column (no-data) after ReplaceSchema.");
        br.addItem("Advice");
        br.addElement("The column should have at least one not-null value in all records");
        br.addElement("by conventionalTakeAssertMap settings in replaceSchemaMap.dfprop:");
        br.addElement("");
        br.addElement(Srl.indent(2, _dispPropertiesProvider.get()));
        br.addElement("");
        br.addElement("So prepare the data (e.g. tsv or xls) for ReplaceSchema.");
        br.addElement("");
        br.addElement("  playsql");
        br.addElement("    |-data");
        br.addElement("    |   |-common");
        br.addElement("    |   |   |-tsv");
        br.addElement("    |   |   |-xls");
        br.addElement("    |   |-ut");
        br.addElement("    |   |   |-tsv");
        br.addElement("    |   |   |-xls");
        br.addElement("    ...");
        br.addElement("");
        br.addElement("Or adjust dfprop settings, for example,");
        br.addElement("you can except the table if it cannot be help.");
        br.addElement("(Of course, do after you ask your friends developing together)");
        br.addItem("emptyTable Column");
        for (DfColumnMeta columnMeta : emptyTableColumnList) {
            br.addElement(columnMeta);
        }
        br.addItem("nullOnly Column");
        for (DfColumnMeta columnMeta : nullOnlyColumnList) {
            br.addElement(columnMeta);
        }
        br.addItem("errorIfTableFirstDateAfter");
        br.addElement(tableFirstDate != null ? new HandyDate(tableFirstDate) : null);
        br.addItem("errorIfColumnFirstDateAfter");
        br.addElement(columnFirstDate != null ? new HandyDate(columnFirstDate) : null);
        final String errorMessage = propMap.getNullOnlyColumnErrorMessage();
        if (Srl.is_NotNull_and_NotTrimmedEmpty(errorMessage)) {
            br.addItem("Project Message");
            br.addElement(errorMessage);
        }
        final String msg = br.buildExceptionMessage();
        throw new DfTakeFinallyAssertionFailureEmptyTableException(msg);
    }

    // ===================================================================================
    //                                                                         DB MetaData
    //                                                                         ===========
    protected List<DfTableMeta> extractTableList() {
        Connection conn = null;
        try {
            conn = _dataSource.getConnection();
            final DatabaseMetaData metaData = conn.getMetaData();
            return new DfTableExtractor().getTableList(metaData, _dataSource.getSchema());
        } catch (SQLException e) {
            throw new SQLFailureException("Failed to extract table meta list: " + _dataSource, e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    protected List<DfColumnMeta> extractColumnList(DfTableMeta tableMeta) {
        Connection conn = null;
        try {
            conn = _dataSource.getConnection();
            final DatabaseMetaData metaData = conn.getMetaData();
            return new DfColumnExtractor().getColumnList(metaData, tableMeta);
        } catch (SQLException e) {
            String msg = "Failed to extract column meta list: " + tableMeta + ", " + _dataSource;
            throw new SQLFailureException(msg, e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    // ===================================================================================
    //                                                                          First Date
    //                                                                          ==========
    protected boolean isTableFirstDateAfter(DfTableMeta tableMeta, Date targetDate) {
        return _firstDateAgent.isTableFirstDateAfter(tableMeta.getTableDbName(), targetDate);
    }

    protected boolean isColumnFirstDateAfter(DfColumnMeta columnMeta, Date targetDate) {
        final String tableName = columnMeta.getTableName();
        final String columnName = columnMeta.getColumnName();
        return _firstDateAgent.isColumnFirstDateAfter(tableName, columnName, targetDate);
    }
}
