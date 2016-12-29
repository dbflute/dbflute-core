/*
 * Copyright 2014-2017 the original author or authors.
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

import org.apache.torque.engine.database.model.Table;
import org.dbflute.util.DfNameHintUtil;
import org.dbflute.util.Srl;
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
    protected final DfSchemaPolicyTableTheme _tableTheme = new DfSchemaPolicyTableTheme();
    protected final DfSchemaPolicyTableStatement _tableStatement = new DfSchemaPolicyTableStatement();
    protected final DfSchemaPolicyColumnTheme _columnTheme = new DfSchemaPolicyColumnTheme();
    protected final DfSchemaPolicyColumnStatement _columnStatement = new DfSchemaPolicyColumnStatement();
    protected final DfSchemaPolicyMiscSecretary _secretary = new DfSchemaPolicyMiscSecretary();

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
        final List<String> vioList = new ArrayList<String>(); // #hope needs to be structured, rule and message
        final boolean mainSchemaOnly = isMainSchemaOnly();
        for (Table table : _tableListSupplier.get()) {
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
            _log.info("No violation of schema policy, good!\n[Schema Policy]\n" + _secretary.buildPolicyExp(_policyMap));
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
        _secretary.throwSchemaPolicyCheckViolationException(_policyMap, vioList);
    }

    protected void throwSchemaPolicyCheckUnknownPropertyException(String property) {
        _secretary.throwSchemaPolicyCheckUnknownPropertyException(property);
    }

    // ===================================================================================
    //                                                                         Check Table
    //                                                                         ===========
    protected void doCheckTableMap(Table table, Map<String, Object> tableMap, List<String> vioList) {
        _tableTheme.checkTableTheme(table, tableMap, vioList);
        _tableStatement.checkTableStatement(table, tableMap, vioList);
    }

    // ===================================================================================
    //                                                                        Check Column
    //                                                                        ============
    protected void doCheckColumnMap(Table table, Map<String, Object> columnMap, List<String> vioList) {
        _columnTheme.checkColumnTheme(table, columnMap, vioList);
        _columnStatement.checkColumnStatement(table, columnMap, vioList);
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
