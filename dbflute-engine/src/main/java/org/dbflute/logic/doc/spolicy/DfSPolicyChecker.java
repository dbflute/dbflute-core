/*
 * Copyright 2014-2022 the original author or authors.
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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import org.apache.torque.engine.database.model.Column;
import org.apache.torque.engine.database.model.Database;
import org.apache.torque.engine.database.model.Table;
import org.dbflute.logic.doc.spolicy.parsed.DfSPolicyParsedPolicy;
import org.dbflute.logic.doc.spolicy.parsed.DfSPolicyParsedPolicy.DfSPolicyParsedPolicyPart;
import org.dbflute.logic.doc.spolicy.parsed.DfSPolicyStatement;
import org.dbflute.logic.doc.spolicy.result.DfSPolicyResult;
import org.dbflute.logic.doc.spolicy.secretary.DfSPolicyCrossSecretary;
import org.dbflute.logic.doc.spolicy.secretary.DfSPolicyExceptTargetSecretary;
import org.dbflute.logic.doc.spolicy.secretary.DfSPolicyFirstDateSecretary;
import org.dbflute.logic.doc.spolicy.secretary.DfSPolicyLogicalSecretary;
import org.dbflute.logic.jdbc.schemadiff.DfSchemaDiff;
import org.dbflute.system.DBFluteSystem;
import org.dbflute.util.DfTraceViewUtil;
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
    protected final Database _database; // for tables
    protected final Supplier<List<DfSchemaDiff>> _schemaDiffListSupplier; // callback for lazy-loading
    protected final Map<String, Object> _policyMap; // from DBFlute properties

    // -----------------------------------------------------
    //                                             Secretary
    //                                             ---------
    protected final DfSPolicyCrossSecretary _crossSecretary;
    protected final DfSPolicyExceptTargetSecretary _exceptTargetSecretary;
    protected final DfSPolicyFirstDateSecretary _firstDateSecretary;
    protected final DfSPolicyLogicalSecretary _logicalSecretary = new DfSPolicyLogicalSecretary();

    // -----------------------------------------------------
    //                                        Nested Checker
    //                                        --------------
    protected final DfSPolicyWholeThemeChecker _wholeThemeChecker;
    protected final DfSPolicyTableThemeChecker _tableThemeChecker;
    protected final DfSPolicyTableStatementChecker _tableStatementChecker;
    protected final DfSPolicyColumnThemeChecker _columnThemeChecker;
    protected final DfSPolicyColumnStatementChecker _columnStatementChecker;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfSPolicyChecker(Database database // for tables
            , Supplier<List<DfSchemaDiff>> schemaDiffListSupplier // for firstDate
            , Map<String, Object> policyMap // from DBFlute properties
    ) {
        _database = database;
        _schemaDiffListSupplier = schemaDiffListSupplier;
        _policyMap = policyMap;

        // should be before creating nested checkers
        _exceptTargetSecretary = new DfSPolicyExceptTargetSecretary(policyMap);
        _firstDateSecretary = new DfSPolicyFirstDateSecretary(_schemaDiffListSupplier);
        _crossSecretary = new DfSPolicyCrossSecretary(_exceptTargetSecretary, _logicalSecretary);

        // checkers using secretaries
        _wholeThemeChecker = new DfSPolicyWholeThemeChecker(_crossSecretary, _logicalSecretary);
        _tableThemeChecker = new DfSPolicyTableThemeChecker(_logicalSecretary);
        _tableStatementChecker = new DfSPolicyTableStatementChecker(_firstDateSecretary, _logicalSecretary);
        _columnThemeChecker = new DfSPolicyColumnThemeChecker(_logicalSecretary);
        _columnStatementChecker = new DfSPolicyColumnStatementChecker(_crossSecretary, _firstDateSecretary, _logicalSecretary);
    }

    // ===================================================================================
    //                                                                        Check Policy
    //                                                                        ============
    public DfSPolicyResult checkPolicyIfNeeds() { // null allowed if no policy
        if (_policyMap.isEmpty()) {
            return null;
        }
        _log.info("");
        _log.info("...Beginning schema policy check");
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
        final long before = System.currentTimeMillis();
        final DfSPolicyParsedPolicy policy = parsePolicy();
        final String dispPolicy = showParsedPolicy(policy);
        final DfSPolicyResult result = createPolicyResult(policy);
        result.acceptPolicyMessage(dispPolicy);
        final List<Table> tableList = _database.getTableList();
        doCheckWhole(policy, result, tableList);
        for (Table table : tableList) {
            if (!isTargetTable(table)) {
                continue;
            }
            doCheckTableColumn(policy, result, table);
        }
        final String violationMessage = _logicalSecretary.buildSchemaPolicyCheckViolationMessage(result);
        result.acceptViolationMessage(violationMessage);
        result.acceptEndingHandler(() -> { // lazy handling for display of SchemaHTML
            _log.info("...Ending schema policy check: " + result);
            if (result.hasViolation()) {
                _logicalSecretary.throwSchemaPolicyCheckViolationException(violationMessage);
            } else {
                final long after = System.currentTimeMillis();
                final String performanceView = DfTraceViewUtil.convertToPerformanceView(after - before); // for tuning
                _log.info(" -> No violation of schema policy. Good DB design! [" + performanceView + "]");
                _log.info("");
            }
        });
        return result; // not ending yet
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
        _log.info("...Parsing schema policy map: " + _policyMap.keySet());
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
                    _logicalSecretary.throwSchemaPolicyCheckUnknownPropertyException(key);
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
                statementList.add(_logicalSecretary.parseStatement(nativeStatement));
            }
        }
        return Collections.unmodifiableList(statementList);
    }

    protected String showParsedPolicy(DfSPolicyParsedPolicy policy) {
        final StringBuilder sb = new StringBuilder();
        sb.append(ln()).append("[Schema Policy]");
        sb.append(ln()).append(" tableExceptList: ").append(_exceptTargetSecretary.getTableExceptList());
        sb.append(ln()).append(" tableTargetList: ").append(_exceptTargetSecretary.getTableTargetList());
        sb.append(ln()).append(" columnExceptMap: ").append(_exceptTargetSecretary.getColumnExceptMap());
        sb.append(ln()).append(" isMainSchemaOnly: ").append(_exceptTargetSecretary.isMainSchemaOnly());
        buildElementMap(sb, "wholeMap", policy.getWholePolicyPart());
        buildElementMap(sb, "tableMap", policy.getTablePolicyPart());
        buildElementMap(sb, "columnMap", policy.getColumnPolicyPart());
        final String display = sb.toString();
        _log.info(display);
        return display;
    }

    protected void buildElementMap(StringBuilder sb, String title, DfSPolicyParsedPolicyPart policyPart) {
        sb.append(ln()).append(" ").append(title).append(":");
        sb.append(ln()).append("   themeList: ").append(policyPart.getThemeList());
        sb.append(ln()).append("   statementList:");
        final List<DfSPolicyStatement> tableStatementList = policyPart.getStatementList();
        for (DfSPolicyStatement statement : tableStatementList) {
            sb.append(ln()).append("    ").append(statement.getIfClause()).append(" ").append(statement.getThenClause());
        }
    }

    // ===================================================================================
    //                                                                       Policy Result
    //                                                                       =============
    protected DfSPolicyResult createPolicyResult(DfSPolicyParsedPolicy policy) {
        return new DfSPolicyResult(policy);
    }

    // ===================================================================================
    //                                                                           Secretary
    //                                                                           =========
    // -----------------------------------------------------
    //                                                 Cross
    //                                                 -----
    public DfSPolicyCrossSecretary getCrossSecretary() {
        return _crossSecretary;
    }

    // -----------------------------------------------------
    //                                         Except/Target
    //                                         -------------
    public DfSPolicyExceptTargetSecretary getExceptTargetSecretary() { // called by nested checker
        return _exceptTargetSecretary;
    }

    protected boolean isTargetTable(Table table) {
        return _exceptTargetSecretary.isTargetTable(table);
    }

    protected boolean isTargetColumn(Column column) {
        return _exceptTargetSecretary.isTargetColumn(column);
    }

    // -----------------------------------------------------
    //                                            First Date
    //                                            ----------
    public DfSPolicyFirstDateSecretary getFirstDateSecretary() { // called by nested checker
        return _firstDateSecretary;
    }

    // -----------------------------------------------------
    //                                               Logical
    //                                               -------
    public DfSPolicyLogicalSecretary getLogicalSecretary() { // called by nested checker
        return _logicalSecretary;
    }

    // ===================================================================================
    //                                                                        Small Helper
    //                                                                        ============
    protected String ln() {
        return DBFluteSystem.ln();
    }
}
