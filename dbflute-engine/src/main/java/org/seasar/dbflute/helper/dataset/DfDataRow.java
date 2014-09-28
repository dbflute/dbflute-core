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

import org.seasar.dbflute.helper.dataset.states.DfDtsRowState;
import org.seasar.dbflute.helper.dataset.states.DfDtsRowStates;
import org.seasar.dbflute.helper.dataset.types.DfDtsColumnType;
import org.seasar.dbflute.helper.dataset.types.DfDtsColumnTypes;

/**
 * @author modified by jflute (originated in Seasar2)
 * @since 0.8.3 (2008/10/28 Tuesday)
 */
public class DfDataRow {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfDataTable _table;
    protected final int _rowNumber;
    protected final List<Object> _values = new ArrayList<Object>();
    protected DfDtsRowState _state = DfDtsRowStates.UNCHANGED;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfDataRow(DfDataTable table, int rowNumber) {
        _table = table;
        _rowNumber = rowNumber;
    }

    // ===================================================================================
    //                                                                      Value Handling
    //                                                                      ==============
    public Object getValue(int index) {
        return _values.get(index);
    }

    public Object getValue(String columnName) {
        final DfDataColumn column = _table.getColumn(columnName);
        return _values.get(column.getColumnIndex());
    }

    public void addValue(String columnName, Object value) {
        final DfDataColumn column = _table.getColumn(columnName);
        _values.add(column.convert(value));
        modify();
    }

    public void setValue(int columnIndex, Object value) {
        final DfDataColumn column = _table.getColumn(columnIndex);
        _values.set(columnIndex, column.convert(value));
        modify();
    }

    private void modify() {
        if (_state.equals(DfDtsRowStates.UNCHANGED)) {
            _state = DfDtsRowStates.MODIFIED;
        }
    }

    public void remove() {
        _state = DfDtsRowStates.REMOVED;
    }

    public DfDataTable getTable() {
        return _table;
    }

    public int getRowNumber() {
        return _rowNumber;
    }

    public DfDtsRowState getState() {
        return _state;
    }

    public void setState(DfDtsRowState state) {
        _state = state;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof DfDataRow)) {
            return false;
        }
        final DfDataRow other = (DfDataRow) o;
        for (int i = 0; i < _table.getColumnSize(); ++i) {
            final String columnName = _table.getColumnName(i);
            final Object value = _values.get(i);
            final Object otherValue = other.getValue(columnName);
            final DfDtsColumnType ct = DfDtsColumnTypes.getColumnType(value);
            if (ct.equals(value, otherValue)) {
                continue;
            }
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(100);
        sb.append("{");
        for (int i = 0; i < _values.size(); ++i) {
            sb.append(getValue(i));
            sb.append(", ");
        }
        sb.setLength(sb.length() - 2);
        sb.append('}');
        return sb.toString();
    }
}
