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

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.torque.engine.database.model.UnifiedSchema;
import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.helper.StringKeyMap;
import org.seasar.dbflute.helper.dataset.states.DfDtsRowStates;
import org.seasar.dbflute.helper.dataset.types.DfDtsColumnType;
import org.seasar.dbflute.helper.dataset.types.DfDtsColumnTypes;
import org.seasar.dbflute.logic.jdbc.metadata.basic.DfColumnExtractor;
import org.seasar.dbflute.logic.jdbc.metadata.basic.DfUniqueKeyExtractor;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfColumnMeta;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfPrimaryKeyMeta;
import org.seasar.dbflute.properties.DfLittleAdjustmentProperties;

/**
 * @author modified by jflute (originated in Seasar2)
 * @since 0.8.3 (2008/10/28 Tuesday)
 */
public class DfDataTable {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _tableDbName;
    protected final Map<String, DfDataColumn> _columnMap = StringKeyMap.createAsFlexibleOrdered();
    protected final List<DfDataColumn> _columnList = new ArrayList<DfDataColumn>();
    protected final List<DfDataRow> _rows = new ArrayList<DfDataRow>();
    protected final List<DfDataRow> _removedRows = new ArrayList<DfDataRow>();
    protected boolean _hasMetaData;

    // failed to be helper dependencies...
    protected final DfColumnExtractor _columnExtractor = new DfColumnExtractor();
    protected final DfUniqueKeyExtractor _uniqueKeyExtractor = new DfUniqueKeyExtractor();

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfDataTable(String tableName) {
        _tableDbName = tableName;
    }

    // ===================================================================================
    //                                                                                Main
    //                                                                                ====
    public int getRowSize() {
        return _rows.size();
    }

    public DfDataRow getRow(int rowIndex) {
        return (DfDataRow) _rows.get(rowIndex);
    }

    public DfDataRow addRow() {
        final int rowNumber = _rows.size() + 1;
        final DfDataRow row = new DfDataRow(this, rowNumber);
        _rows.add(row);
        row.setState(DfDtsRowStates.CREATED);
        return row;
    }

    public int getRemovedRowSize() {
        return _removedRows.size();
    }

    public DfDataRow getRemovedRow(int index) {
        return (DfDataRow) _removedRows.get(index);
    }

    public DfDataRow[] removeRows() {
        for (int i = 0; i < _rows.size();) {
            final DfDataRow row = getRow(i);
            if (row.getState().equals(DfDtsRowStates.REMOVED)) {
                _removedRows.add(row);
                _rows.remove(i);
            } else {
                ++i;
            }
        }
        return (DfDataRow[]) _removedRows.toArray(new DfDataRow[_removedRows.size()]);
    }

    public int getColumnSize() {
        return _columnMap.size();
    }

    public DfDataColumn getColumn(int columnIndex) {
        return _columnList.get(columnIndex);
    }

    public DfDataColumn getColumn(String columnName) {
        final DfDataColumn column = getColumn0(columnName);
        if (column == null) {
            String msg = "The column was not found in the table: ";
            msg = msg + " tableName=" + _tableDbName + " columnName=" + columnName;
            throw new IllegalStateException(msg);
        }
        return column;
    }

    private DfDataColumn getColumn0(String columnName) {
        return _columnMap.get(columnName);
    }

    public boolean hasColumn(String columnName) {
        return getColumn0(columnName) != null;
    }

    public String getColumnName(int columnIndex) {
        return getColumn(columnIndex).getColumnDbName();
    }

    public DfDtsColumnType getColumnType(int columnIndex) {
        return getColumn(columnIndex).getColumnType();
    }

    public DfDtsColumnType getColumnType(String columnName) {
        return getColumn(columnName).getColumnType();
    }

    public DfDataColumn addColumn(String columnName) {
        return addColumn(columnName, DfDtsColumnTypes.OBJECT);
    }

    public DfDataColumn addColumn(String columnName, DfDtsColumnType columnType) {
        final DfDataColumn column = new DfDataColumn(columnName, columnType, _columnMap.size());
        _columnMap.put(columnName, column);
        _columnList.add(column);
        return column;
    }

    public boolean hasMetaData() {
        return _hasMetaData;
    }

    public void setupMetaData(DatabaseMetaData metaData, UnifiedSchema unifiedSchema) throws SQLException {
        final Map<String, DfColumnMeta> metaMap = extractColumnMetaMap(metaData, unifiedSchema);
        final Set<String> primaryKeySet = getPrimaryKeySet(metaData, unifiedSchema);
        for (int i = 0; i < getColumnSize(); ++i) {
            final DfDataColumn column = getColumn(i);
            if (primaryKeySet.contains(column.getColumnDbName())) {
                column.setPrimaryKey(true);
            } else {
                column.setPrimaryKey(false);
            }
            final DfColumnMeta metaInfo = metaMap.get(column.getColumnDbName());
            if (metaInfo != null) {
                column.setWritable(true);
                final int jdbcDefValue = metaInfo.getJdbcDefValue();
                column.setColumnType(DfDtsColumnTypes.getColumnType(jdbcDefValue));
            } else {
                column.setWritable(false);
            }
        }
        _hasMetaData = true;
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected Map<String, DfColumnMeta> extractColumnMetaMap(DatabaseMetaData metaData, UnifiedSchema unifiedSchema)
            throws SQLException {
        final List<DfColumnMeta> metaList = _columnExtractor.getColumnList(metaData, unifiedSchema, _tableDbName);
        final Map<String, DfColumnMeta> metaMap = new HashMap<String, DfColumnMeta>();
        for (DfColumnMeta metaInfo : metaList) {
            metaMap.put(metaInfo.getColumnName(), metaInfo);
        }
        return metaMap;
    }

    protected Set<String> getPrimaryKeySet(DatabaseMetaData metaData, UnifiedSchema unifiedSchema) {
        try {
            final DfPrimaryKeyMeta pkInfo = _uniqueKeyExtractor.getPrimaryKey(metaData, unifiedSchema, _tableDbName);
            return new HashSet<String>(pkInfo.getPrimaryKeyList());
        } catch (SQLException e) {
            String msg = "SQLException occured: unifiedSchema=" + unifiedSchema + " tableName=" + _tableDbName;
            throw new IllegalStateException(msg);
        }
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(100);
        sb.append(_tableDbName);
        sb.append(": ");
        for (int columnIndex = 0; columnIndex < _columnMap.size(); ++columnIndex) {
            sb.append(getColumnName(columnIndex));
            sb.append(", ");
        }
        sb.setLength(sb.length() - 2);
        sb.append("\n");
        for (int rowIndex = 0; rowIndex < _rows.size(); ++rowIndex) {
            sb.append(getRow(rowIndex) + "\n");
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof DfDataTable)) {
            return false;
        }
        final DfDataTable other = (DfDataTable) o;
        if (getRowSize() != other.getRowSize()) {
            return false;
        }
        for (int i = 0; i < getRowSize(); ++i) {
            if (!getRow(i).equals(other.getRow(i))) {
                return false;
            }
        }
        if (getRemovedRowSize() != other.getRemovedRowSize()) {
            return false;
        }
        for (int i = 0; i < getRemovedRowSize(); ++i) {
            if (!getRemovedRow(i).equals(other.getRemovedRow(i))) {
                return false;
            }
        }
        return true;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getTableDbName() {
        return _tableDbName;
    }

    public String getTableSqlName() {
        return quoteTableNameIfNeeds(_tableDbName);
    }

    protected String quoteTableNameIfNeeds(String tableDbName) {
        final DfLittleAdjustmentProperties prop = DfBuildProperties.getInstance().getLittleAdjustmentProperties();
        return prop.quoteTableNameIfNeedsDirectUse(tableDbName);
    }
}
