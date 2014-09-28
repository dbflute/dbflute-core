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
package org.seasar.dbflute.helper.dataset;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.seasar.dbflute.helper.StringKeyMap;

/**
 * @author modified by jflute (originated in Seasar2)
 * @since 0.8.3 (2008/10/28 Tuesday)
 */
public class DfDataSet {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Map<String, DfDataTable> _tableMap = StringKeyMap.createAsFlexibleOrdered();
    protected final List<DfDataTable> _tableList = new ArrayList<DfDataTable>();

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfDataSet() {
    }

    // ===================================================================================
    //                                                                      Table Handling
    //                                                                      ==============
    public int getTableSize() {
        return _tableMap.size();
    }

    public String getTableName(int index) {
        return getTable(index).getTableDbName();
    }

    public DfDataTable getTable(int index) {
        return (DfDataTable) _tableList.get(index);
    }

    public boolean hasTable(String tableName) {
        return _tableMap.containsKey(tableName);
    }

    public DfDataTable getTable(String tableName) {
        DfDataTable table = (DfDataTable) _tableMap.get(tableName);
        if (table == null) {
            String msg = "The table was Not Found: " + tableName;
            throw new IllegalStateException(msg);
        }
        return table;
    }

    public DfDataTable addTable(String tableName) {
        return addTable(new DfDataTable(tableName));
    }

    public DfDataTable addTable(DfDataTable table) {
        _tableMap.put(table.getTableDbName(), table);
        _tableList.add(table);
        return table;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (int tableIndex = 0; tableIndex < getTableSize(); ++tableIndex) {
            if (tableIndex > 0) {
                sb.append("\n");
            }
            sb.append(getTable(tableIndex));
        }
        return sb.toString();
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof DfDataSet)) {
            return false;
        }
        DfDataSet other = (DfDataSet) o;
        if (getTableSize() != other.getTableSize()) {
            return false;
        }
        for (int i = 0; i < getTableSize(); ++i) {
            if (!getTable(i).equals(other.getTable(i))) {
                return false;
            }
        }
        return true;
    }
}
