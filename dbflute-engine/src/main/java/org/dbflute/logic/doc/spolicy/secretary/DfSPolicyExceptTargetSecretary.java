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
package org.dbflute.logic.doc.spolicy.secretary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.torque.engine.database.model.Column;
import org.apache.torque.engine.database.model.Table;
import org.dbflute.util.DfNameHintUtil;

/**
 * @author jflute
 * @since 1.1.8 (2018/5/3 Thursday)
 */
public class DfSPolicyExceptTargetSecretary {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Map<String, Object> _policyMap;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfSPolicyExceptTargetSecretary(Map<String, Object> policyMap) {
        _policyMap = policyMap;
    }

    // ===================================================================================
    //                                                                           Targeting
    //                                                                           =========
    public boolean isTargetTable(Table table) { // may be called by nested checker
        if (table.isTypeView()) {
            return false; // fixedly
        }
        if (isMainSchemaOnly() && table.isAdditionalSchema()) {
            return false;
        }
        return DfNameHintUtil.isTargetByHint(table.getTableDbName(), getTableTargetList(), getTableExceptList());
    }

    public boolean isTargetColumn(Column column) { // may be called by nested checker
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

    // ===================================================================================
    //                                                                     Cached Resource
    //                                                                     ===============
    protected List<String> _tableExceptList;

    public List<String> getTableExceptList() {
        if (_tableExceptList != null) {
            return _tableExceptList;
        }
        @SuppressWarnings("unchecked")
        final List<String> plainList = (List<String>) _policyMap.get("tableExceptList");
        _tableExceptList = plainList != null ? plainList : new ArrayList<String>();
        return _tableExceptList;
    }

    protected List<String> _tableTargetList;

    public List<String> getTableTargetList() {
        if (_tableTargetList != null) {
            return _tableTargetList;
        }
        @SuppressWarnings("unchecked")
        final List<String> plainList = (List<String>) _policyMap.get("tableTargetList");
        _tableTargetList = plainList != null ? plainList : new ArrayList<String>();
        return _tableTargetList;
    }

    protected Map<String, List<String>> _columnExceptMap;

    public Map<String, List<String>> getColumnExceptMap() {
        if (_columnExceptMap != null) {
            return _columnExceptMap;
        }
        @SuppressWarnings("unchecked")
        final Map<String, List<String>> plainList = (Map<String, List<String>>) _policyMap.get("columnExceptMap");
        _columnExceptMap = plainList != null ? plainList : new HashMap<String, List<String>>();
        return _columnExceptMap;
    }

    public boolean isMainSchemaOnly() {
        return ((String) _policyMap.getOrDefault("isMainSchemaOnly", "false")).equalsIgnoreCase("true");
    }
}
