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
package org.seasar.dbflute.logic.jdbc.metadata.info;

import java.util.Map;

import org.apache.torque.engine.database.model.UnifiedSchema;
import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.properties.DfLittleAdjustmentProperties;
import org.seasar.dbflute.util.DfCollectionUtil;

/**
 * @author jflute
 */
public class DfForeignKeyMeta {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String _foreignKeyName;
    protected UnifiedSchema _localSchema;
    protected String _localTablePureName;
    protected UnifiedSchema _foreignSchema;
    protected String _foreignTablePureName;
    protected final Map<String, String> _columnNameMap = DfCollectionUtil.newLinkedHashMap();

    // ===================================================================================
    //                                                                       Name Building
    //                                                                       =============
    public String getForeignTableIdentityName() {
        if (_foreignSchema == null) {
            return _foreignTablePureName;
        }
        return _foreignSchema.getIdentifiedSchema() + "." + _foreignTablePureName;
    }

    public String getLocalTableSqlName() {
        if (_localSchema == null) {
            return _localTablePureName;
        }
        final DfLittleAdjustmentProperties prop = DfBuildProperties.getInstance().getLittleAdjustmentProperties();
        final String quotedName = prop.quoteTableNameIfNeedsDirectUse(_localTablePureName);
        return _localSchema.buildSqlName(quotedName); // driven is resolved here so it uses pure name here
    }

    public String getForeignTableSqlName() {
        if (_foreignSchema == null) {
            return _foreignTablePureName;
        }
        final DfLittleAdjustmentProperties prop = DfBuildProperties.getInstance().getLittleAdjustmentProperties();
        final String quotedName = prop.quoteTableNameIfNeedsDirectUse(_foreignTablePureName);
        return _foreignSchema.buildSqlName(quotedName); // driven is resolved here so it uses pure name here
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return _foreignKeyName + ":{" + _localTablePureName + ":" + _foreignTablePureName + ":" + _columnNameMap + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getForeignKeyName() {
        return _foreignKeyName;
    }

    public void setForeignKeyName(String foreignKeyName) {
        _foreignKeyName = foreignKeyName;
    }

    public UnifiedSchema getLocalSchema() {
        return _localSchema;
    }

    public void setLocalSchema(UnifiedSchema localSchema) {
        _localSchema = localSchema;
    }

    public String getLocalTablePureName() {
        return _localTablePureName;
    }

    public void setLocalTablePureName(String localTablePureName) {
        _localTablePureName = localTablePureName;
    }

    public UnifiedSchema getForeignSchema() {
        return _foreignSchema;
    }

    public void setForeignSchema(UnifiedSchema foreignSchema) {
        _foreignSchema = foreignSchema;
    }

    public String getForeignTablePureName() {
        return _foreignTablePureName;
    }

    public void setForeignTablePureName(String foreignTablePureName) {
        _foreignTablePureName = foreignTablePureName;
    }

    public Map<String, String> getColumnNameMap() {
        return _columnNameMap;
    }

    public void putColumnName(String localColumnName, String foreignColumnName) {
        _columnNameMap.put(localColumnName, foreignColumnName);
    }

    public void putColumnNameAll(Map<String, String> columnNameMap) {
        _columnNameMap.putAll(columnNameMap);
    }
}
