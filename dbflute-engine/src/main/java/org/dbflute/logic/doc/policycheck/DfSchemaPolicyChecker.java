/*
 * Copyright 2014-2015 the original author or authors.
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
package org.dbflute.logic.doc.policycheck;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import org.apache.torque.engine.database.model.Column;
import org.apache.torque.engine.database.model.Table;
import org.dbflute.exception.DfSchemaPolicyCheckIllegalIfThenStatementException;
import org.dbflute.exception.DfSchemaPolicyCheckUnknownPropertyException;
import org.dbflute.exception.DfSchemaPolicyCheckUnknownThemeException;
import org.dbflute.exception.DfSchemaPolicyCheckViolationException;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.util.DfNameHintUtil;
import org.dbflute.util.Srl;
import org.dbflute.util.Srl.ScopeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 1.1.1 (2015/12/31 Thursday)
 */
public class DfSchemaPolicyChecker {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The logger instance for this class. (NotNull) */
    private static final Logger _log = LoggerFactory.getLogger(DfSchemaPolicyChecker.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Supplier<List<Table>> _tableListSupplier;
    protected final Map<String, Object> _policyMap;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfSchemaPolicyChecker(Supplier<List<Table>> tableListSupplier, Map<String, Object> policyMap) {
        _tableListSupplier = tableListSupplier;
        _policyMap = policyMap;
    }

    // ===================================================================================
    //                                                                        Check Policy
    //                                                                        ============
    public void checkPolicyIfNeeds() {
        if (_policyMap.isEmpty()) {
            return;
        }
        _log.info("");
        _log.info("* * * * * * * * * * * * * * * * *");
        _log.info("*                               *");
        _log.info("*      Check Schema Policy      *");
        _log.info("*                               *");
        _log.info("* * * * * * * * * * * * * * * * *");
        // map:{
        //     ; tableExceptList = list:{}
        //     ; tableTargetList = list:{}
        //     ; isMainSchemaOnly = false
        //     ; tableMap = map:{
        //         ; themeList = list:{ hasPK ; upperCaseBasis ; identityIfPureIDPK }
        //     }
        //     ; columnMap = map:{
        //         ; themeList = list:{ upperCaseBasis }
        //         ; statementList = list:{
        //             ; if columnName is suffix:_FLAG then bad
        //             ; if columnName is suffix:_FLG then notNull
        //             ; if columnName is suffix:_FLG then dbType is INTEGER 
        //         }
        //     }
        //     ; additionalSchemaPolicyMap = list:{
        //         ; tableExceptList = list:{}
        //         ; tableTargetList = list:{}
        //         ; tableMap = map:{
        //             ; inheritsMainSchema = false
        //         }
        //         ; columnMap = map:{
        //         }
        //     }
        // }
        final boolean mainSchemaOnly = isMainSchemaOnly();
        final List<String> vioList = new ArrayList<String>();
        final List<Table> tableList = _tableListSupplier.get();
        for (Table table : tableList) {
            if (table.isTypeView()) { // out of target
                continue;
            }
            if (!isTargetTable(table.getTableDbName())) {
                continue;
            }
            if (mainSchemaOnly && table.isAdditionalSchema()) {
                continue;
            }
            doCheck(table, vioList);
        }
        if (!vioList.isEmpty()) {
            throwSchemaPolicyCheckViolationException(vioList);
        } else {
            _log.info("No violation of schema policy, good!\n[Schema Policy]\n" + buildPolicyExp());
        }
    }

    protected boolean isMainSchemaOnly() {
        return ((String) _policyMap.getOrDefault("isMainSchemaOnly", "false")).equalsIgnoreCase("true");
    }

    protected void doCheck(Table table, List<String> vioList) {
        for (Entry<String, Object> entry : _policyMap.entrySet()) {
            final String key = entry.getKey();
            final Object value = entry.getValue();
            if (key.equals("tableMap")) {
                @SuppressWarnings("unchecked")
                final Map<String, Object> tableMap = (Map<String, Object>) value;
                doCheckTableMap(table, tableMap, vioList);
            } else if (key.equals("columnMap")) {
                @SuppressWarnings("unchecked")
                final Map<String, Object> columnMap = (Map<String, Object>) value;
                doCheckColumnMap(table, columnMap, vioList);
            } else {
                if (!Srl.equalsPlain(key, "isMainSchemaOnly", "tableExceptList", "tableTargetList")) {
                    throwSchemaPolicyCheckUnknownPropertyException(key);
                }
            }
        }
    }

    protected void throwSchemaPolicyCheckViolationException(List<String> vioList) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The schema policy has been violated.");
        br.addItem("Advice");
        br.addElement("Make sure your violating schema (ERD and DDL).");
        br.addElement("And after that, execute renewal (or regenerate) again.");
        br.addElement("(tips: The schema policy is on schemaPolicyMap.dfprop)");
        br.addItem("Schema Policy");
        br.addElement(buildPolicyExp());
        br.addItem("Violation");
        for (String vio : vioList) {
            br.addElement(vio);
        }
        final String msg = br.buildExceptionMessage();
        throw new DfSchemaPolicyCheckViolationException(msg, vioList);
    }

    protected String buildPolicyExp() {
        final StringBuilder policySb = new StringBuilder();
        _policyMap.forEach((key, value) -> {
            if (key.equals("tableMap")) {
                setupTableColumnMapDisp(policySb, key, value);
            } else if (key.equals("columnMap")) {
                setupTableColumnMapDisp(policySb, key, value);
            } else {
                policySb.append("\n").append(key).append(": ").append(value);
            }
        });
        return Srl.ltrim(policySb.toString());
    }

    protected void setupTableColumnMapDisp(StringBuilder policySb, String mapTitle, Object mapObj) {
        policySb.append("\n").append(mapTitle).append(":");
        @SuppressWarnings("unchecked")
        final Map<String, Object> map = (Map<String, Object>) mapObj;
        map.forEach((key, value) -> {
            if (key.equals("statementList")) {
                policySb.append("\n  " + key + ":");
                @SuppressWarnings("unchecked")
                final List<String> statementList = (List<String>) value;
                for (Object statement : statementList) {
                    policySb.append("\n    " + statement);
                }
            } else {
                policySb.append("\n  " + key + ": " + value);
            }
        });
    }

    // ===================================================================================
    //                                                                         Check Table
    //                                                                         ===========
    protected void doCheckTableMap(Table table, Map<String, Object> tableMap, List<String> unmatchedList) {
        processTableTheme(table, tableMap, unmatchedList);
    }

    // -----------------------------------------------------
    //                                                 Theme
    //                                                 -----
    protected void processTableTheme(Table table, Map<String, Object> tableMap, List<String> unmatchedList) {
        @SuppressWarnings("unchecked")
        final List<String> themeList = (List<String>) tableMap.get("themeList");
        if (themeList == null) {
            return;
        }
        for (String theme : themeList) {
            evaluateTableTheme(table, unmatchedList, theme);
        }
    }

    protected void evaluateTableTheme(Table table, List<String> unmatchedList, String theme) {
        final String tableDbName = table.getTableDbName();
        if (theme.equals("hasPK")) {
            if (!table.hasPrimaryKey()) {
                unmatchedList.add("The table should have primary key: " + tableDbName);
            }
        } else if (theme.equals("identityIfPureIDPK")) {
            if (table.hasPrimaryKey() && table.hasSinglePrimaryKey()) {
                final Column pk = table.getPrimaryKeyAsOne();
                if (!pk.isForeignKey() && Srl.endsWith(pk.getName(), "ID") && !pk.isIdentity()) {
                    unmatchedList.add("The primary key should be identity: " + tableDbName + "." + pk.getName());
                }
            }
        } else if (theme.equals("upperCaseBasis")) {
            if (Srl.isLowerCaseAny(table.getTableSqlName())) { // use SQL name because DB name may be control name
                unmatchedList.add("The table name should be on upper case basis: " + tableDbName);
            }
        } else if (theme.equals("lowerCaseBasis")) {
            if (Srl.isUpperCaseAny(table.getTableSqlName())) { // same reason
                unmatchedList.add("The table name should be on lower case basis: " + tableDbName);
            }
        } else {
            throwSchemaPolicyCheckUnknownThemeException(theme, "Table");
        }
    }

    // ===================================================================================
    //                                                                        Check Column
    //                                                                        ============
    protected void doCheckColumnMap(Table table, Map<String, Object> columnMap, List<String> vioList) {
        processColumnTheme(table, columnMap, vioList);
        final List<Column> columnList = table.getColumnList();
        for (Column column : columnList) {
            processColumnStatement(table, column, columnMap, vioList);
        }
    }

    // -----------------------------------------------------
    //                                                 Theme
    //                                                 -----
    protected void processColumnTheme(Table table, Map<String, Object> columnMap, List<String> vioList) {
        @SuppressWarnings("unchecked")
        final List<String> themeList = (List<String>) columnMap.get("themeList");
        if (themeList == null) {
            return;
        }
        for (String theme : themeList) {
            evaluateColumnTheme(table, vioList, theme);
        }
    }

    protected void evaluateColumnTheme(Table table, List<String> vioList, String theme) {
        final List<Column> columnList = table.getColumnList();
        for (Column column : columnList) {
            if (theme.equals("upperCaseBasis")) {
                if (Srl.isLowerCaseAny(column.getColumnSqlName())) { // use SQL name because DB name may be control name
                    vioList.add("The column name should be on upper case basis: " + toColumnDisp(table, column));
                }
            } else if (theme.equals("lowerCaseBasis")) {
                if (Srl.isUpperCaseAny(column.getColumnSqlName())) { // same reason
                    vioList.add("The column name should be on lower case basis: " + toColumnDisp(table, column));
                }
            } else {
                throwSchemaPolicyCheckUnknownThemeException(theme, "Column");
            }
        }
    }

    // -----------------------------------------------------
    //                                             Statement
    //                                             ---------
    protected void processColumnStatement(Table table, Column column, Map<String, Object> columnMap, List<String> vioList) {
        @SuppressWarnings("unchecked")
        final List<String> statementList = (List<String>) columnMap.get("statementList");
        if (statementList != null) {
            for (String statement : statementList) {
                doProcessColumnStatement(table, column, statement, vioList);
            }
        }
    }

    protected void doProcessColumnStatement(Table table, Column column, String statement, List<String> vioList) {
        // e.g.
        //  if columnName is suffix:_FLG then notNull
        //  if columnName is suffix:_FLG then dbType is integer
        if (!statement.startsWith("if ")) {
            String msg = "The element of statementList should start with 'if' for SchemaPolicyCheck: " + statement;
            throw new IllegalStateException(msg);
        }
        final ScopeInfo ifClauseScope = Srl.extractScopeFirst(statement, "if ", " then ");
        if (ifClauseScope == null) {
            final String additional = "The statement should start with 'if' and contain 'then'.";
            throwSchemaPolicyCheckIllegalIfThenStatementException(statement, additional);
        }
        final String ifClause = ifClauseScope.getContent().trim();
        if (!ifClause.contains(" is ")) {
            final String additional = "The if-clause should contain 'is': " + ifClause;
            throwSchemaPolicyCheckIllegalIfThenStatementException(statement, additional);
        }
        final String ifItem = Srl.substringFirstFront(ifClause, " is ").trim();
        final String ifValue = Srl.substringFirstRear(ifClause, " is ").trim();
        final String thenClause = ifClauseScope.substringInterspaceToNext();
        if (ifItem.equalsIgnoreCase("columnName")) {
            if (isHitColumn(column.getName(), ifValue)) {
                evaluateColumnThenClause(table, column, statement, vioList, thenClause);
            }
        } else if (ifItem.equalsIgnoreCase("dbType")) {
            if (column.hasDbType() && isHitExp(column.getDbType(), ifValue)) {
                evaluateColumnThenClause(table, column, statement, vioList, thenClause);
            }
        } else {
            throwSchemaPolicyCheckIllegalIfThenStatementException(statement, "Unknown if-item: " + ifItem);
        }
    }

    protected void evaluateColumnThenClause(Table table, Column column, String statement, List<String> vioList, String thenClause) {
        if (thenClause.equalsIgnoreCase("notNull")) {
            if (!column.isNotNull()) {
                vioList.add("The column should be not-null: " + toColumnDisp(table, column));
            }
        } else if (thenClause.equalsIgnoreCase("bad")) {
            vioList.add("The column is no good: " + toColumnDisp(table, column));
        } else if (thenClause.contains(" is ")) { // e.g. dbType is integer
            evaluateColumnThenItemValue(table, column, statement, vioList, thenClause);
        } else {
            throwSchemaPolicyCheckIllegalIfThenStatementException(statement, "Unknown then-clause: " + thenClause);
        }
    }

    protected void evaluateColumnThenItemValue(Table table, Column column, String statement, List<String> vioList, String thenClause) {
        final String thenItem = Srl.substringFirstFront(thenClause, " is ").trim();
        final String thenValue = Srl.substringFirstRear(thenClause, " is ").trim();
        if (thenItem.equalsIgnoreCase("dbType")) { // e.g. dbType is integer
            if (column.hasDbType()) {
                final String dbType = column.getDbType();
                if (!isHitExp(dbType, thenValue)) {
                    vioList.add("The column db-type should be " + thenValue + " but " + dbType + ": " + toColumnDisp(table, column));
                }
            }
        } else if (thenItem.equalsIgnoreCase("alias")) { // e.g. alias is suffix:ID
            if (column.hasAlias()) {
                final String alias = column.getAlias();
                if (!isHitExp(alias, thenValue)) {
                    vioList.add("The column alias should be " + thenValue + " but " + alias + ": " + toColumnDisp(table, column));
                }
            }
        } else if (thenItem.equalsIgnoreCase("comment")) { // e.g. comment is contain:SEA
            if (column.hasAlias()) {
                final String comment = column.getComment();
                if (!isHitExp(comment, thenValue)) {
                    vioList.add("The column comment should be " + thenValue + " but " + comment + ": " + toColumnDisp(table, column));
                }
            }
        } else {
            throwSchemaPolicyCheckIllegalIfThenStatementException(statement, "Unknown then-item: " + thenItem);
        }
    }

    protected String toColumnDisp(Table table, Column column) {
        final String notNull = column.isNotNull() ? "*" : "";
        final String dbType = column.hasDbType() ? column.getDbType() : "(unknownType)";
        final String size = column.hasColumnSize() ? "(" + column.getColumnSize() + ")" : "";
        return notNull + table.getTableDbName() + "." + column.getName() + " " + dbType + size;
    }

    // ===================================================================================
    //                                                                          Hit Helper
    //                                                                          ==========
    public boolean isHitColumn(String columnName, String hint) {
        return determineHitBy(columnName, hint);
    }

    public boolean isHitExp(String exp, String hint) {
        return determineHitBy(exp, hint);
    }

    protected boolean determineHitBy(String name, String hint) {
        if (hint.contains(" and ")) {
            final List<String> elementHintList = Srl.splitListTrimmed(hint, " and ");
            for (String elementHint : elementHintList) {
                if (!DfNameHintUtil.isHitByTheHint(name, elementHint)) {
                    return false;
                }
            }
            return true;
        } else if (hint.contains(" or ")) {
            final List<String> elementHintList = Srl.splitListTrimmed(hint, " or ");
            for (String elementHint : elementHintList) {
                if (DfNameHintUtil.isHitByTheHint(name, elementHint)) {
                    return true;
                }
            }
            return false;
        } else {
            return DfNameHintUtil.isHitByTheHint(name, hint);
        }
    }

    // ===================================================================================
    //                                                                           Exception
    //                                                                           =========
    protected void throwSchemaPolicyCheckUnknownPropertyException(String property) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Unknown property for SchemaPolicyCheck.");
        br.addItem("Advice");
        br.addElement("Make sure your schemaPolicyMap.dfprop.");
        br.addItem("Unknown Property");
        br.addElement(property);
        final String msg = br.buildExceptionMessage();
        throw new DfSchemaPolicyCheckUnknownPropertyException(msg);
    }

    protected void throwSchemaPolicyCheckUnknownThemeException(String theme, String targetType) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Unknown theme for SchemaPolicyCheck.");
        br.addItem("Advice");
        br.addElement("Make sure your schemaPolicyMap.dfprop.");
        br.addElement("You can use following themes:");
        br.addElement(" Table  : hasPK, upperCaseBasis, lowerCaseBasis, identityIfPureIDPK");
        br.addElement(" Column : upperCaseBasis, lowerCaseBasis");
        br.addItem("Target");
        br.addElement(targetType);
        br.addItem("Unknown Theme");
        br.addElement(theme);
        final String msg = br.buildExceptionMessage();
        throw new DfSchemaPolicyCheckUnknownThemeException(msg);
    }

    protected void throwSchemaPolicyCheckIllegalIfThenStatementException(String statement, String additional) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Illegal if-then statement for SchemaPolicyCheck.");
        br.addItem("Advice");
        br.addElement("Make sure your schemaPolicyMap.dfprop.");
        br.addElement("If-then statement should be like this:");
        br.addElement(" if [if-clause] then [then-clause]");
        br.addElement("");
        br.addElement("To be precise:");
        br.addElement(" if [if-item] is [if-value] then [then-item]");
        br.addElement("  or ");
        br.addElement(" if [if-item] is [if-value] then [then-item] is [then-value]");
        br.addElement("");
        br.addElement("For example:");
        br.addElement("  (o): if columnName is suffix:_FLAG then bad");
        br.addElement("  (o): if columnName is suffix:_FLG then notNull");
        br.addElement("  (o): if columnName is suffix:_FLG then dbType is INTEGER");
        br.addElement("");
        br.addElement(additional);
        br.addItem("Statement");
        br.addElement(statement);
        final String msg = br.buildExceptionMessage();
        throw new DfSchemaPolicyCheckIllegalIfThenStatementException(msg);
    }

    // ===================================================================================
    //                                                                       Except/Target
    //                                                                       =============
    public boolean isTargetTable(String name) {
        return DfNameHintUtil.isTargetByHint(name, getTableTargetList(), getTableExceptList());
    }

    protected List<String> _tableExceptList;

    protected List<String> getTableExceptList() {
        if (_tableExceptList != null) {
            return _tableExceptList;
        }
        @SuppressWarnings("unchecked")
        final List<String> plainList = (List<String>) _policyMap.get("tableExceptList");
        _tableExceptList = plainList != null ? plainList : new ArrayList<String>();
        return _tableExceptList;
    }

    protected List<String> _tableTargetList;

    protected List<String> getTableTargetList() {
        if (_tableTargetList != null) {
            return _tableTargetList;
        }
        @SuppressWarnings("unchecked")
        final List<String> plainList = (List<String>) _policyMap.get("tableTargetList");
        _tableTargetList = plainList != null ? plainList : new ArrayList<String>();
        return _tableTargetList;
    }
}
