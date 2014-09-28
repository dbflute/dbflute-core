/*
 * Copyright 2004-2014 the Seasar Foundation and the Others.
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
package org.seasar.dbflute.logic.doc.craftdiff;

import java.io.File;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.exception.DfCraftDiffCraftTitleNotFoundException;
import org.seasar.dbflute.exception.DfCraftDiffTableEqualsParameterNotFound;
import org.seasar.dbflute.exception.DfJDBCException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.helper.StringKeyMap;
import org.seasar.dbflute.helper.StringSet;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfColumnMeta;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfTableMeta;
import org.seasar.dbflute.properties.DfBasicProperties;
import org.seasar.dbflute.properties.DfCommonColumnProperties;
import org.seasar.dbflute.properties.DfOptimisticLockProperties;
import org.seasar.dbflute.resource.DBFluteSystem;
import org.seasar.dbflute.util.DfNameHintUtil;
import org.seasar.dbflute.util.DfStringUtil;
import org.seasar.dbflute.util.Srl;
import org.seasar.dbflute.util.Srl.ScopeInfo;

/**
 * @author jflute
 * @since 0.9.9.8 (2012/09/04 Tuesday)
 */
public class DfCraftDiffAssertProvider {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log _log = LogFactory.getLog(DfCraftDiffAssertProvider.class);
    public static final String TABLE_EQUALS_UNIQUE_NAME = "UNIQUE_CODE";
    public static final String TABLE_EQUALS_DATA_NAME = "TARGET_DATA";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _craftMetaDir;
    protected final DfCraftDiffAssertDirection _nextDirection;
    protected final List<DfTableMeta> _tableList;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfCraftDiffAssertProvider(String craftMetaDir, DfCraftDiffAssertDirection nextDirection,
            List<DfTableMeta> tableList) {
        _craftMetaDir = craftMetaDir;
        _nextDirection = nextDirection;
        _tableList = tableList;
    }

    // ===================================================================================
    //                                                                             Provide
    //                                                                             =======
    /**
     * @param sqlFile The text file that has the specified SQL. (NotNull) 
     * @param sql The SQL string to assert. (NotNull)
     * @return The handle of CraftDiff assert. (NullAllowed: if null, means not found)
     */
    public DfCraftDiffAssertHandler provideCraftDiffAssertHandler(File sqlFile, String sql) {
        if (!sql.contains("--")) {
            return null;
        }
        // resolve comment spaces
        final String resolvedSql = DfStringUtil.replace(sql, "-- #", "--#");

        // CraftDiff supports only equals
        DfCraftDiffAssertHandler handler = processBasicEquals(sqlFile, sql, resolvedSql);
        if (handler == null) {
            handler = processTableEquals(sqlFile, sql, resolvedSql);
        }
        return handler;
    }

    protected DfCraftDiffAssertHandler processBasicEquals(File sqlFile, String plainSql, final String resolvedSql) {
        final String keyPrefix = "--#df:assertEquals("; // space after '--' has been resolved here
        final String keySuffix = ")#";
        final ScopeInfo scopeFirst = Srl.extractScopeFirst(resolvedSql, keyPrefix, keySuffix);
        if (scopeFirst == null) {
            return null; // not found
        }
        final String craftTitle = scopeFirst.getContent().trim();
        if (Srl.is_Null_or_TrimmedEmpty(craftTitle)) {
            throwCraftDiffCraftTitleNotFoundException(sqlFile, plainSql);
        }
        // *unsupported envType on assert definition
        return new DfCraftDiffAssertHandler(_craftMetaDir, _nextDirection, craftTitle);
    }

    protected void throwCraftDiffCraftTitleNotFoundException(File sqlFile, String sql) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the craft title in the SQL.");
        br.addItem("Advice");
        br.addElement("The assertion should have its title like this:");
        br.addElement("  -- #df:assertEquals([craft-title])#");
        br.addElement("");
        br.addElement("For example:");
        br.addElement("  (o): -- #df:assertEquals(Trigger)#");
        br.addElement("  (x): -- #df:assertEquals()#");
        br.addItem("SQL File");
        br.addElement(sqlFile.getPath());
        br.addItem("Assertion SQL");
        br.addElement(sql);
        final String msg = br.buildExceptionMessage();
        throw new DfCraftDiffCraftTitleNotFoundException(msg);
    }

    protected DfCraftDiffAssertHandler processTableEquals(File sqlFile, String plainSql, final String resolvedSql) {
        final String keyPrefix = "--#df:assertTableEquals("; // space after '--' has been resolved here
        final String keySuffix = ")#";
        final ScopeInfo scopeFirst = Srl.extractScopeFirst(resolvedSql, keyPrefix, keySuffix);
        if (scopeFirst == null) {
            return null; // not found
        }
        final String args = scopeFirst.getContent().trim();
        if (Srl.is_Null_or_TrimmedEmpty(args)) {
            throwCraftDiffCraftTitleNotFoundException(sqlFile, plainSql);
        }
        final List<String> argList = Srl.splitListTrimmed(args, ",");
        if (argList.size() < 2) {
            throwCraftDiffTableEqualsParameterNotFound(sqlFile, resolvedSql, argList);
        }
        final String craftTitle = argList.get(0);
        final String tableHint = argList.get(1);
        final String exceptExp = argList.size() > 2 ? argList.get(2) : null;

        // *unsupported envType on assert definition
        return createTableEqualsAssertHandler(craftTitle, tableHint, exceptExp);
    }

    protected void throwCraftDiffTableEqualsParameterNotFound(File sqlFile, String sql, List<String> argList) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the parameter for table-equals of CraftDiff.");
        br.addItem("Advice");
        br.addElement("You should specify two or more parameters at least like this:");
        br.addElement("  -- #df:assertTableEquals([title-name], [table-hint])");
        br.addElement("  -- #df:assertTableEquals([title-name], [table-hint], [column-adjustment])");
        br.addElement("");
        br.addElement("For example:");
        br.addElement("  -- #df:assertTableEquals(TableCls, prefix:CLS_)");
        br.addElement("  -- #df:assertTableEquals(TableCls, prefix:CLS_, except:DESCRIPTION)");
        br.addItem("SQL File");
        br.addElement(sqlFile.getPath());
        br.addItem("Target SQL");
        br.addElement(sql);
        br.addItem("Found Parameter");
        br.addElement(argList);
        final String msg = br.buildExceptionMessage();
        throw new DfCraftDiffTableEqualsParameterNotFound(msg);
    }

    protected DfCraftDiffAssertHandler createTableEqualsAssertHandler(final String craftTitle, final String tableHint,
            final String exceptExp) {
        return new DfCraftDiffAssertHandler(_craftMetaDir, _nextDirection, craftTitle) {
            @Override
            protected List<Map<String, String>> selectDiffDataList(File sqlFile, Statement st, String sql)
                    throws SQLException {
                final List<Map<String, String>> unifiedList = new ArrayList<Map<String, String>>();
                final Map<String, String> tableSqlMap = toTableSqlMap(tableHint, exceptExp);
                for (DfTableMeta tableMeta : _tableList) {
                    final String tableSql = tableSqlMap.get(tableMeta.getTableName());
                    if (tableSql == null) {
                        continue;
                    }
                    final List<Map<String, String>> selectedList = super.selectDiffDataList(sqlFile, st, tableSql);
                    final List<DfColumnMeta> columnMetaList = tableMeta.getLazyColumnMetaList();
                    if (columnMetaList == null) {
                        String msg = "Not found the column meta for the table: " + tableMeta;
                        throw new IllegalStateException(msg);
                    }
                    if (columnMetaList.isEmpty()) {
                        String msg = "Empty column meta for the table: " + tableMeta;
                        throw new IllegalStateException(msg);
                    }
                    final DfColumnMeta pkCol = columnMetaList.get(0); // first column should be PK
                    final String pkName = pkCol.getColumnName();
                    for (Map<String, String> recordMap : selectedList) {
                        final String pkValue = recordMap.remove(pkName);
                        final Map<String, String> adjustedMap = StringKeyMap.createAsFlexibleOrdered();
                        final String uniqueCode = tableMeta.getTableName() + "::" + pkValue;
                        adjustedMap.put(TABLE_EQUALS_UNIQUE_NAME, uniqueCode);
                        final StringBuilder valueSb = new StringBuilder();
                        int columnIndex = 0;
                        for (Entry<String, String> entry : recordMap.entrySet()) { // no PK loop
                            if (columnIndex > 0) {
                                valueSb.append("|");
                            }
                            final String columnValue = entry.getValue();
                            valueSb.append(columnValue);
                            ++columnIndex;
                        }
                        adjustedMap.put(TABLE_EQUALS_DATA_NAME, valueSb.toString());
                        unifiedList.add(adjustedMap);
                    }
                }
                return unifiedList;
            }

            @Override
            protected void handleSQLException(SQLException e, String sql) throws SQLException {
                String msg = "Failed to execute the SQL for CraftDiff.";
                msg = msg + ln() + "The SQL has been switched so see the SQL bellow:";
                msg = msg + ln() + sql;
                throw new DfJDBCException(msg, e);
            }
        };
    }

    protected Map<String, String> toTableSqlMap(String tableHint, String exceptExp) {
        final Set<String> exceptColumnSet = StringSet.createAsFlexible();
        final String exceptMark = "except:";
        if (exceptExp != null && exceptExp.startsWith(exceptMark)) {
            final String columnExp = Srl.substringFirstRear(exceptExp, exceptMark);
            final List<String> exceptColumnList = Srl.splitListTrimmed(columnExp, "/");
            exceptColumnSet.addAll(exceptColumnList);
        }
        final Map<String, String> tableSqlMap = new HashMap<String, String>();
        final StringBuilder logSb = new StringBuilder();
        logSb.append("...Switching table-equals SQL to:");
        for (DfTableMeta tableMeta : _tableList) {
            final String tableSql = buildTableEqualsSql(tableMeta, tableHint, exceptColumnSet);
            if (tableSql == null) {
                continue;
            }
            logSb.append(ln()).append(tableSql).append(";");
            tableSqlMap.put(tableMeta.getTableName(), tableSql);
        }
        _log.info(logSb.toString());
        return tableSqlMap;
    }

    protected String buildTableEqualsSql(DfTableMeta tableMeta, String tableHint, Set<String> exceptColumnSet) {
        final String tableName = tableMeta.getTableName();
        if (!DfNameHintUtil.isHitByTheHint(tableName, tableHint)) {
            return null;
        }
        final DfCommonColumnProperties commonColumnProp = getCommonColumnProperties();
        final DfOptimisticLockProperties optimisticLockProp = getOptimisticLockProperties();
        final StringBuilder sb = new StringBuilder();
        sb.append("select ");
        final String tableSqlName = tableMeta.getTableSqlName();
        final List<DfColumnMeta> columnMetaList = tableMeta.getLazyColumnMetaList();
        int columnIndex = 0;
        for (DfColumnMeta columnMeta : columnMetaList) {
            final String columnName = columnMeta.getColumnName();
            if (commonColumnProp.isCommonColumn(columnName)) {
                continue;
            }
            if (optimisticLockProp.isOptimisticLockColumn(columnName)) {
                continue;
            }
            if (exceptColumnSet.contains(columnName)) {
                continue;
            }
            if (columnIndex > 0) {
                sb.append(", ");
            }
            sb.append(columnMeta.buildColumnSqlName());
            ++columnIndex;
        }
        sb.append(ln()).append("  from ").append(tableSqlName);
        return sb.toString();
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    protected DfBasicProperties getBasicProperties() {
        return getProperties().getBasicProperties();
    }

    protected DfCommonColumnProperties getCommonColumnProperties() {
        return getProperties().getCommonColumnProperties();
    }

    protected DfOptimisticLockProperties getOptimisticLockProperties() {
        return getProperties().getOptimisticLockProperties();
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String ln() {
        return DBFluteSystem.getBasicLn();
    }
}
