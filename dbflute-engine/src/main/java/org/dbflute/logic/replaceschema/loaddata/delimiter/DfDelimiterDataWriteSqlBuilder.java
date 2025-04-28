/*
 * Copyright 2014-2024 the original author or authors.
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
package org.dbflute.logic.replaceschema.loaddata.delimiter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.dbflute.DfBuildProperties;
import org.dbflute.exception.DfDelimiterDataRegistrationFailureException;
import org.dbflute.logic.jdbc.metadata.info.DfColumnMeta;
import org.dbflute.logic.replaceschema.loaddata.base.DfLoadedSchemaTable;
import org.dbflute.logic.replaceschema.loaddata.base.dataprop.DfDefaultValueProp;
import org.dbflute.logic.replaceschema.loaddata.base.secretary.DfColumnBindTypeProvider;
import org.dbflute.logic.replaceschema.loaddata.base.secretary.DfColumnValueConverter;
import org.dbflute.properties.DfLittleAdjustmentProperties;

/**
 * @author jflute
 */
public class DfDelimiterDataWriteSqlBuilder {

    //====================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                        Basic Resource
    //                                        -- ------------
    protected DfLoadedSchemaTable _schemaTable; // not null (after setup)
    protected Map<String, DfColumnMeta> _columnMetaMap; // not null (after setup)
    protected List<String> _columnNameList; // not null (after setup)
    protected List<String> _valueList; // not null (after setup)
    protected Map<String, Set<String>> _notFoundColumnMap; // not null (after setup)
    protected Map<String, Map<String, String>> _convertValueMap; // not null (after setup)
    protected Map<String, String> _defaultValueMap; // not null (after setup)
    protected DfColumnBindTypeProvider _bindTypeProvider; // not null (after setup)
    protected DfDefaultValueProp _defaultValueProp; // not null (after setup)

    // -----------------------------------------------------
    //                                          Internal Use
    //                                          ------------
    protected Map<String, String> _basicColumnValueCacheMap; // null allowed (lazy-loaded)
    protected Set<String> _sysdateColumnSet; // null allowed (lazy-loaded)

    // ===================================================================================
    //                                                                        Prepared SQL
    //                                                                        ============
    public String buildPreparedSql() {
        final Map<String, String> columnValueMap = createBasicColumnValueMap();
        final StringBuilder sb = new StringBuilder();
        final StringBuilder sbValues = new StringBuilder();
        for (String columnDbName : columnValueMap.keySet()) {
            final String columnSqlName = quoteColumnNameIfNeeds(columnDbName);
            sb.append(", ").append(columnSqlName);
            sbValues.append(", ?");
        }
        final String tableSqlName = _schemaTable.buildTableSqlName();
        sb.delete(0, ", ".length()).insert(0, "insert into " + tableSqlName + " (").append(")");
        sbValues.delete(0, ", ".length()).insert(0, " values(").append(")");
        sb.append(sbValues);
        return sb.toString();
    }

    public Map<String, Object> setupParameter() {
        @SuppressWarnings("unchecked")
        final Map<String, Object> columnValueMap = (Map<String, Object>) ((Object) createBasicColumnValueMap());
        saveSysdateColumnSet(columnValueMap); // for relative date
        convertColumnValueIfNeeds(columnValueMap);
        return columnValueMap;
    }

    protected void saveSysdateColumnSet(Map<String, Object> columnValueMap) { // should be called before convert
        _sysdateColumnSet = _defaultValueProp.extractSysdateColumnSet(columnValueMap, _defaultValueMap);
    }

    protected void convertColumnValueIfNeeds(Map<String, Object> columnValueMap) {
        // because it needs to convert empty string to null
        //if ((_convertValueMap == null || _convertValueMap.isEmpty()) // no convert
        //        && (_defaultValueMap == null || _defaultValueMap.isEmpty())) { // and no default
        //    return;
        //}
        adjustColumnValueBeforeConvert(columnValueMap);
        final DfColumnValueConverter converter = createColumnValueConverter();
        converter.treatEmptyBeforeAsNull(); // for compatible with e.g. $$empty$$ = $$empty$$
        converter.convert(_schemaTable, columnValueMap, _columnMetaMap);
    }

    protected void adjustColumnValueBeforeConvert(Map<String, Object> columnValueMap) {
        final Map<String, Object> overridingMap = new HashMap<String, Object>();
        for (Entry<String, Object> entry : columnValueMap.entrySet()) {
            Object value = entry.getValue();
            if ("".equals(value)) {
                // e.g. TSV might have empty string (treated as null as default)
                // you can change null to empty by convertValueMap.dataprop
                overridingMap.put(entry.getKey(), null);
            }
        }
        columnValueMap.putAll(overridingMap);
    }

    protected DfColumnValueConverter createColumnValueConverter() {
        return new DfColumnValueConverter(_convertValueMap, _defaultValueMap, _bindTypeProvider);
    }

    protected String quoteTableNameIfNeeds(String tableDbName) {
        final DfLittleAdjustmentProperties prop = DfBuildProperties.getInstance().getLittleAdjustmentProperties();
        return prop.quoteTableNameIfNeedsDirectUse(tableDbName);
    }

    protected String quoteColumnNameIfNeeds(String columnDbName) {
        final DfLittleAdjustmentProperties prop = DfBuildProperties.getInstance().getLittleAdjustmentProperties();
        return prop.quoteColumnNameIfNeedsDirectUse(columnDbName);
    }

    // ===================================================================================
    //                                                                           SQL Parts
    //                                                                           =========
    protected Map<String, String> createBasicColumnValueMap() {
        if (_basicColumnValueCacheMap != null) {
            return _basicColumnValueCacheMap;
        }
        _basicColumnValueCacheMap = new LinkedHashMap<String, String>();
        int columnCount = -1;
        for (String columnName : _columnNameList) {
            columnCount++;
            if (!_columnMetaMap.isEmpty() && !_columnMetaMap.containsKey(columnName)) {
                // changed logic at setupColumnNameList() in writer like this:
                // "added columns for default value are existing in DB"
                //  by jflute (2017/03/26)
                //if (hasDefaultValue(columnName)) {
                //    continue;
                //}
                handleNotFoundColumn(columnName);
                continue;
            }
            final String value;
            try {
                value = columnCount < _valueList.size() ? _valueList.get(columnCount) : null;
            } catch (RuntimeException e) {
                String msg = buildDelimiterDataRegistrationFailureMessage(columnCount);
                throw new DfDelimiterDataRegistrationFailureException(msg, e);
            }
            if (!_columnMetaMap.isEmpty() && _columnMetaMap.containsKey(columnName)) {
                String realDbName = _columnMetaMap.get(columnName).getColumnName();
                _basicColumnValueCacheMap.put(realDbName, value);
            } else {
                _basicColumnValueCacheMap.put(columnName, value);
            }
        }
        return _basicColumnValueCacheMap;
    }

    private void handleNotFoundColumn(String columnName) {
        final String onfileTableName = _schemaTable.getOnfileTableName();
        Set<String> notFoundColumnSet = _notFoundColumnMap.get(onfileTableName);
        if (notFoundColumnSet == null) {
            notFoundColumnSet = new LinkedHashSet<String>();
            _notFoundColumnMap.put(onfileTableName, notFoundColumnSet);
        }
        notFoundColumnSet.add(columnName);
    }

    protected String buildDelimiterDataRegistrationFailureMessage(int columnCount) {
        String msg = "valueList.get(columnCount) threw the exception:";
        msg = msg + " tableName=" + _schemaTable + " columnNameList=" + _columnNameList;
        msg = msg + " valueList=" + _valueList + " columnCount=" + columnCount;
        return msg;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public DfLoadedSchemaTable getSchemaTable() {
        return _schemaTable;
    }

    public void setSchemaTable(DfLoadedSchemaTable schemaTable) {
        _schemaTable = schemaTable;
    }

    public Map<String, DfColumnMeta> getColumnMetaMap() {
        return _columnMetaMap;
    }

    public void setColumnMetaMap(Map<String, DfColumnMeta> columnMetaMap) {
        _columnMetaMap = columnMetaMap;
    }

    public List<String> getColumnNameList() {
        return _columnNameList;
    }

    public void setColumnNameList(List<String> columnNameList) {
        _columnNameList = columnNameList;
    }

    public Map<String, Set<String>> getNotFoundColumnMap() {
        return _notFoundColumnMap;
    }

    public void setNotFoundColumnMap(Map<String, Set<String>> notFoundColumnMap) {
        _notFoundColumnMap = notFoundColumnMap;
    }

    public List<String> getValueList() {
        return _valueList;
    }

    public void setValueList(List<String> valueList) {
        _valueList = valueList;
    }

    public Map<String, Map<String, String>> getConvertValueMap() {
        return _convertValueMap;
    }

    public void setConvertValueMap(Map<String, Map<String, String>> convertValueMap) {
        _convertValueMap = convertValueMap;
    }

    public Map<String, String> getDefaultValueMap() {
        return _defaultValueMap;
    }

    public void setDefaultValueMap(Map<String, String> defaultValueMap) {
        _defaultValueMap = defaultValueMap;
    }

    public DfColumnBindTypeProvider getBindTypeProvider() {
        return _bindTypeProvider;
    }

    public void setBindTypeProvider(DfColumnBindTypeProvider bindTypeProvider) {
        _bindTypeProvider = bindTypeProvider;
    }

    public DfDefaultValueProp getDefaultValueProp() {
        return _defaultValueProp;
    }

    public void setDefaultValueProp(DfDefaultValueProp defaultValueProp) {
        _defaultValueProp = defaultValueProp;
    }

    public Set<String> getSysdateColumnSet() {
        return _sysdateColumnSet;
    }
}
