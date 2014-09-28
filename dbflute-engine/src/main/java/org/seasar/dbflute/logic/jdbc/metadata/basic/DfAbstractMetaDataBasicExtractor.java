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
package org.seasar.dbflute.logic.jdbc.metadata.basic;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.torque.engine.database.model.UnifiedSchema;
import org.seasar.dbflute.helper.StringKeyMap;
import org.seasar.dbflute.logic.jdbc.metadata.DfAbstractMetaDataExtractor;
import org.seasar.dbflute.properties.assistant.DfAdditionalSchemaInfo;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.DfNameHintUtil;

/**
 * @author jflute
 */
public class DfAbstractMetaDataBasicExtractor extends DfAbstractMetaDataExtractor {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final List<String> EMPTY_STRING_LIST = DfCollectionUtil.emptyList();

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected boolean _suppressExceptTarget;

    /** The dictionary set of table name for real case name. (NullAllowed) */
    protected Map<String, String> _tableNameDictionaryMap;

    // ===================================================================================
    //                                                                        Table Except
    //                                                                        ============
    /**
     * Is the column of the table out of sight?
     * @param unifiedSchema The unified schema that can contain catalog name and no-name mark. (NullAllowed)
     * @param tableName The name of table. (NotNull)
     * @param columnName The name of column. (NotNull)
     * @return The determination, true or false.
     */
    public boolean isColumnExcept(UnifiedSchema unifiedSchema, String tableName, String columnName) {
        if (tableName == null) {
            throw new IllegalArgumentException("The argument 'tableName' should not be null.");
        }
        if (columnName == null) {
            throw new IllegalArgumentException("The argument 'columnName' should not be null.");
        }
        if (_suppressExceptTarget) {
            return false;
        }
        final Map<String, List<String>> columnExceptMap = getRealColumnExceptMap(unifiedSchema);
        final List<String> columnExceptList = columnExceptMap.get(tableName);
        if (columnExceptList == null) { // no definition about the table
            return false;
        }
        return !isTargetByHint(columnName, EMPTY_STRING_LIST, columnExceptList);
    }

    protected Map<String, List<String>> getRealColumnExceptMap(UnifiedSchema unifiedSchema) { // extension point
        final DfAdditionalSchemaInfo schemaInfo = getAdditionalSchemaInfo(unifiedSchema);
        if (schemaInfo != null) {
            return schemaInfo.getColumnExceptMap();
        }
        return getProperties().getDatabaseProperties().getColumnExceptMap();
    }

    protected boolean isTargetByHint(String name, List<String> targetList, List<String> exceptList) {
        return DfNameHintUtil.isTargetByHint(name, targetList, exceptList);
    }

    protected final DfAdditionalSchemaInfo getAdditionalSchemaInfo(UnifiedSchema unifiedSchema) {
        return getProperties().getDatabaseProperties().getAdditionalSchemaInfo(unifiedSchema);
    }

    // ===================================================================================
    //                                                                    Constraint Order
    //                                                                    ================
    protected <KEY, VALUE> Map<KEY, VALUE> newTableConstraintMap() {
        return new TreeMap<KEY, VALUE>(new Comparator<KEY>() {
            public int compare(KEY o1, KEY o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
    }

    // ===================================================================================
    //                                                                              Option
    //                                                                              ======
    public void suppressExceptTarget() {
        _suppressExceptTarget = true;
    }

    public void enableTableCaseTranslation(List<String> tableNameList) {
        _tableNameDictionaryMap = StringKeyMap.createAsFlexible();
        for (String tableName : tableNameList) {
            _tableNameDictionaryMap.put(tableName, tableName);
        }
    }

    public boolean isEnableTableCaseTranslation() {
        return _tableNameDictionaryMap != null;
    }

    protected String translateTableCaseName(String tableName) {
        if (_tableNameDictionaryMap == null) {
            return tableName;
        }
        final String foundName = _tableNameDictionaryMap.get(tableName);
        return foundName != null ? foundName : tableName;
    }
}