/*
 * Copyright 2014-2016 the original author or authors.
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
package org.dbflute.logic.manage.freegen;

import java.util.List;
import java.util.Map;

import org.dbflute.util.DfCollectionUtil;

/**
 * @author jflute
 */
public class DfFreeGenTable {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Map<String, Object> _tableMap; // not null
    protected final String _tableName; // only-one table, but may be unused
    protected final List<Map<String, Object>> _columnList; // only-one table
    protected final Map<String, Map<String, Object>> _schemaMap; // multiple table

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
    // only-one table : tableMap, tableName, columnList 
    // multiple table : tableMap, schemaMap
    // _/_/_/_/_/_/_/_/_/_/
    public DfFreeGenTable(Map<String, Object> tableMap, String tableName, List<Map<String, Object>> columnList) {
        _tableMap = tableMap;
        _tableName = tableName;
        _columnList = columnList;
        _schemaMap = DfCollectionUtil.emptyMap();
    }

    public DfFreeGenTable(Map<String, Object> tableMap, Map<String, Map<String, Object>> schemaMap) {
        _tableMap = tableMap;
        _tableName = null;
        _columnList = DfCollectionUtil.emptyList();
        _schemaMap = schemaMap;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        if (_tableName != null) {
            return "{tableName=" + _tableName + ", rowList.size()=" + _columnList.size() + "}";
        } else {
            return "{schemaMap.size=" + _schemaMap.size() + ", keys=" + _schemaMap.keySet() + "}";
        }
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public Map<String, Object> getTableMap() {
        return _tableMap;
    }

    // -----------------------------------------------------
    //                                        Only-One Table
    //                                        --------------
    public boolean isOnlyOneTable() {
        return _tableName != null;
    }

    public String getTableName() { // can be used when only-one table
        return _tableName;
    }

    public List<Map<String, Object>> getColumnList() { // can be used when only-one table
        return _columnList;
    }

    // -----------------------------------------------------
    //                                        Multiple Table
    //                                        --------------
    public Map<String, Map<String, Object>> getSchemaMap() { // can be used when multiple table
        return _schemaMap;
    }

    public List<Map<String, Object>> getTableList() { // can be used when multiple table
        return DfCollectionUtil.newArrayList(_schemaMap.values());
    }
}
