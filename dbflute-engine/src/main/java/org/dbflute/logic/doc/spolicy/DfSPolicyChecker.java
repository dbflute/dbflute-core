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
package org.dbflute.logic.doc.spolicy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import org.apache.torque.engine.database.model.Column;
import org.apache.torque.engine.database.model.Table;
import org.dbflute.logic.doc.spolicy.parsed.DfSPolicyParsedPolicy;
import org.dbflute.logic.doc.spolicy.parsed.DfSPolicyParsedPolicy.DfSPolicyParsedPolicyPart;
import org.dbflute.logic.doc.spolicy.parsed.DfSPolicyStatement;
import org.dbflute.logic.doc.spolicy.result.DfSPolicyResult;
import org.dbflute.logic.doc.spolicy.secretary.DfSPolicyMiscSecretary;
import org.dbflute.system.DBFluteSystem;
import org.dbflute.util.DfNameHintUtil;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 1.1.1 (2015/12/31 Thursday)
 */
public class DfSPolicyChecker {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The logger instance for this class. (NotNull) */
    private static final Logger _log = LoggerFactory.getLogger(DfSPolicyChecker.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Supplier<List<Table>> _tableListSupplier;
    protected final Map<String, Object> _policyMap;
    protected final DfSPolicyWholeThemeChecker _wholeThemeChecker;
    protected final DfSPolicyTableThemeChecker _tableThemeChecker;
    protected final DfSPolicyTableStatementChecker _tableStatementChecker;
    protected final DfSPolicyColumnThemeChecker _columnThemeChecker;
    protected final DfSPolicyColumnStatementChecker _columnStatementChecker;
    protected final DfSPolicyMiscSecretary _secretary = new DfSPolicyMiscSecretary();

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfSPolicyChecker(Supplier<List<Table>> tableListSupplier, Map<String, Object> policyMap) {
        _tableListSupplier = tableListSupplier;
        _policyMap = policyMap;
        _wholeThemeChecker = new DfSPolicyWholeThemeChecker(this, column -> isTargetColumn(column));
        _tableThemeChecker = new DfSPolicyTableThemeChecker(this);
        _tableStatementChecker = new DfSPolicyTableStatementChecker(this);
        _columnThemeChecker = new DfSPolicyColumnThemeChecker(this);
        _columnStatementChecker = new DfSPolicyColumnStatementChecker(this);
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
        //     ; columnExceptMap = map:{}
        //     ; isMainSchemaOnly = false
        //     ; wholeMap = map:{
        //         ; themeList = list:{ uniqueTableAlias ; sameColumnAliasIfSameColumnName }
        //     }
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
        // }
        final DfSPolicyParsedPolicy policy = parsePolicy();
        showParsedPolicy(policy);
        final DfSPolicyResult result = new DfSPolicyResult();
        final List<Table> tableList = _tableListSupplier.get();
        doCheckWhole(policy, result, tableList);
        for (Table table : tableList) {
            if (!isTargetTable(table)) {
                continue;
            }
            doCheckTableColumn(policy, result, table);
        }
        if (!result.isEmpty()) {
            _secretary.throwSchemaPolicyCheckViolationException(_policyMap, result);
        } else {
            _log.info(" -> No violation of schema policy. Good DB design!");
            _log.info("");
        }
    }

    protected void doCheckWhole(DfSPolicyParsedPolicy policy, DfSPolicyResult result, List<Table> tableList) {
        if (!tableList.isEmpty()) { // just in case
            final List<String> themeList = policy.getWholePolicyPart().getThemeList();
            _wholeThemeChecker.checkWholeTheme(themeList, result, tableList.get(0).getDatabase());
        }
    }

    protected void doCheckTableColumn(DfSPolicyParsedPolicy policy, DfSPolicyResult result, Table table) {
        final DfSPolicyParsedPolicyPart tablePolicyPart = policy.getTablePolicyPart();
        _tableThemeChecker.checkTableTheme(tablePolicyPart.getThemeList(), result, table);
        _tableStatementChecker.checkTableStatement(tablePolicyPart.getStatementList(), result, table);

        final List<Column> columnList = table.getColumnList();
        final DfSPolicyParsedPolicyPart columnPolicyPart = policy.getColumnPolicyPart();
        for (Column column : columnList) {
            if (!isTargetColumn(column)) {
                continue;
            }
            _columnThemeChecker.checkColumnTheme(columnPolicyPart.getThemeList(), result, column);
            _columnStatementChecker.checkColumnStatement(columnPolicyPart.getStatementList(), result, column);
        }
    }

    // ===================================================================================
    //                                                                        Parse Policy
    //                                                                        ============
    protected DfSPolicyParsedPolicy parsePolicy() {
        _log.info("...Parcing schema policy map: " + _policyMap.keySet());
        DfSPolicyParsedPolicyPart wholePolicyPart = null;
        DfSPolicyParsedPolicyPart tablePolicyPart = null;
        DfSPolicyParsedPolicyPart columnPolicyPart = null;
        for (Entry<String, Object> entry : _policyMap.entrySet()) {
            final String key = entry.getKey();
            final Object value = entry.getValue();
            if (key.equals("wholeMap")) {
                @SuppressWarnings("unchecked")
                final Map<String, Object> wholeMap = (Map<String, Object>) value;
                final List<String> themeList = extractThemeList(wholeMap);
                wholePolicyPart = new DfSPolicyParsedPolicyPart(themeList, Collections.emptyList());
            } else if (key.equals("tableMap")) {
                @SuppressWarnings("unchecked")
                final Map<String, Object> tableMap = (Map<String, Object>) value;
                final List<String> themeList = extractThemeList(tableMap);
                tablePolicyPart = new DfSPolicyParsedPolicyPart(themeList, extractStatementList(tableMap));
            } else if (key.equals("columnMap")) {
                @SuppressWarnings("unchecked")
                final Map<String, Object> columnMap = (Map<String, Object>) value;
                final List<String> themeList = extractThemeList(columnMap);
                columnPolicyPart = new DfSPolicyParsedPolicyPart(themeList, extractStatementList(columnMap));
            } else {
                if (!Srl.equalsPlain(key, "tableExceptList", "tableTargetList", "columnExceptMap", "isMainSchemaOnly")) {
                    _secretary.throwSchemaPolicyCheckUnknownPropertyException(key);
                }
            }
        }
        if (wholePolicyPart == null) {
            wholePolicyPart = new DfSPolicyParsedPolicyPart(Collections.emptyList(), Collections.emptyList());
        }
        if (tablePolicyPart == null) {
            tablePolicyPart = new DfSPolicyParsedPolicyPart(Collections.emptyList(), Collections.emptyList());
        }
        if (columnPolicyPart == null) {
            columnPolicyPart = new DfSPolicyParsedPolicyPart(Collections.emptyList(), Collections.emptyList());
        }
        return new DfSPolicyParsedPolicy(wholePolicyPart, tablePolicyPart, columnPolicyPart);
    }

    @SuppressWarnings("unchecked")
    protected List<String> extractThemeList(Map<String, Object> map) {
        return (List<String>) map.getOrDefault("themeList", Collections.emptyList());
    }

    protected List<DfSPolicyStatement> extractStatementList(Map<String, Object> map) {
        @SuppressWarnings("unchecked")
        final List<String> nativeStatementList = (List<String>) map.get("statementList");
        final List<DfSPolicyStatement> statementList = new ArrayList<DfSPolicyStatement>();
        if (nativeStatementList != null) {
            for (String nativeStatement : nativeStatementList) {
                statementList.add(_secretary.parseStatement(nativeStatement));
            }
        }
        return Collections.unmodifiableList(statementList);
    }

    protected void showParsedPolicy(DfSPolicyParsedPolicy policy) {
        final StringBuilder sb = new StringBuilder();
        sb.append(ln()).append("[Schema Policy]");
        sb.append(ln()).append(" tableExceptList: ").append(getTableExceptList());
        sb.append(ln()).append(" tableTargetList: ").append(getTableTargetList());
        sb.append(ln()).append(" columnExceptMap: ").append(getColumnExceptMap());
        sb.append(ln()).append(" isMainSchemaOnly: ").append(isMainSchemaOnly());
        buildElementMap(sb, "wholeMap", policy.getWholePolicyPart());
        buildElementMap(sb, "tableMap", policy.getTablePolicyPart());
        buildElementMap(sb, "columnMap", policy.getColumnPolicyPart());
        _log.info(sb.toString());
    }

    protected void buildElementMap(StringBuilder sb, String title, DfSPolicyParsedPolicyPart policyPart) {
        sb.append(ln()).append(" ").append(title).append(":");
        sb.append(ln()).append("   themeList: ").append(policyPart.getThemeList());
        sb.append(ln()).append("   statementList:");
        final List<DfSPolicyStatement> tableStatementList = policyPart.getStatementList();
        for (DfSPolicyStatement statement : tableStatementList) {
            sb.append(ln()).append("     statement:");
            sb.append(ln()).append("       native: ").append(statement.getNativeExp());
            sb.append(ln()).append("       parsed: ");
            sb.append(statement.getIfClause()).append(", ").append(statement.getThenClause());
        }
    }

    // ===================================================================================
    //                                                                       Except/Target
    //                                                                       =============
    public boolean isTargetTable(Table table) { // may be called by nested checker
        if (table.isTypeView()) {
            return false; // fixedly
        }
        if (isMainSchemaOnly() && table.isAdditionalSchema()) {
            return false;
        }
        return DfNameHintUtil.isTargetByHint(table.getTableDbName(), getTableTargetList(), getTableExceptList());
    }

    protected boolean isTargetColumn(Column column) {
        final Map<String, List<String>> columnExceptMap = getColumnExceptMap();
        if (columnExceptMap.isEmpty()) {
            return true;
        }
        final String tableDbName = column.getTable().getTableDbName();
        for (Entry<String, List<String>> entry : columnExceptMap.entrySet()) {
            final String tableHint = entry.getKey();
            if (DfNameHintUtil.isHitByTheHint(tableDbName, tableHint)) {
                final List<String> columnExceptList = entry.getValue();
                if (!DfNameHintUtil.isTargetByHint(column.getName(), Collections.emptyList(), columnExceptList)) {
                    return false;
                }
            }
        }
        return true;
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

    protected Map<String, List<String>> _columnExceptMap;

    protected Map<String, List<String>> getColumnExceptMap() {
        if (_columnExceptMap != null) {
            return _columnExceptMap;
        }
        @SuppressWarnings("unchecked")
        final Map<String, List<String>> plainList = (Map<String, List<String>>) _policyMap.get("columnExceptMap");
        _columnExceptMap = plainList != null ? plainList : new HashMap<String, List<String>>();
        return _columnExceptMap;
    }

    protected boolean isMainSchemaOnly() {
        return ((String) _policyMap.getOrDefault("isMainSchemaOnly", "false")).equalsIgnoreCase("true");
    }

    // ===================================================================================
    //                                                                        Small Helper
    //                                                                        ============
    protected String ln() {
        return DBFluteSystem.ln();
    }
}
