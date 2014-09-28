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
package org.seasar.dbflute.properties.assistant;

import java.util.List;
import java.util.Map;

import org.apache.torque.engine.database.model.UnifiedSchema;

/**
 * @author jflute
 * @since 0.9.5.5 (2009/10/04 Sunday)
 */
public class DfAdditionalSchemaInfo {

    protected UnifiedSchema _unifiedSchema;
    protected List<String> _objectTypeTargetList;
    protected List<String> _tableExceptList;
    protected List<String> _tableExceptGenOnlyList;
    protected Map<String, List<String>> _columnExceptGenOnlyMap;
    protected List<String> _tableTargetList;
    protected Map<String, List<String>> _columnExceptMap;
    protected boolean _suppressCommonColumn;
    protected boolean _suppressProcedure;
    protected Map<String, String> _supplementaryConnectionMap;

    public boolean hasObjectTypeSynonym() {
        return DfConnectionProperties.hasObjectTypeSynonym(getObjectTypeTargetList());
    }

    public String getSupplementaryConnectionUser() {
        if (_supplementaryConnectionMap == null) {
            return null;
        }
        return _supplementaryConnectionMap.get("user");
    }

    public String getSupplementaryConnectionPassword() {
        if (_supplementaryConnectionMap == null) {
            return null;
        }
        return _supplementaryConnectionMap.get("password");
    }

    public UnifiedSchema getUnifiedSchema() {
        return _unifiedSchema;
    }

    public void setUnifiedSchema(UnifiedSchema unifiedSchema) {
        this._unifiedSchema = unifiedSchema;
    }

    public List<String> getObjectTypeTargetList() {
        return _objectTypeTargetList;
    }

    public void setObjectTypeTargetList(List<String> objectTypeTargetList) {
        this._objectTypeTargetList = objectTypeTargetList;
    }

    public List<String> getTableExceptList() {
        return _tableExceptList;
    }

    public void setTableExceptList(List<String> tableExceptList) {
        this._tableExceptList = tableExceptList;
    }

    public List<String> getTableExceptGenOnlyList() {
        return _tableExceptGenOnlyList;
    }

    public void setTableExceptGenOnlyList(List<String> tableExceptGenOnlyList) {
        this._tableExceptGenOnlyList = tableExceptGenOnlyList;
    }

    public List<String> getTableTargetList() {
        return _tableTargetList;
    }

    public void setTableTargetList(List<String> tableTargetList) {
        this._tableTargetList = tableTargetList;
    }

    public Map<String, List<String>> getColumnExceptMap() {
        return _columnExceptMap;
    }

    public void setColumnExceptMap(Map<String, List<String>> columnExceptMap) {
        this._columnExceptMap = columnExceptMap;
    }

    public Map<String, List<String>> getColumnExceptGenOnlyMap() {
        return _columnExceptGenOnlyMap;
    }

    public void setColumnExceptGenOnlyMap(Map<String, List<String>> _columnExceptGenOnlyMap) {
        this._columnExceptGenOnlyMap = _columnExceptGenOnlyMap;
    }

    public boolean isSuppressCommonColumn() {
        return _suppressCommonColumn;
    }

    public void setSuppressCommonColumn(boolean suppressCommonColumn) {
        this._suppressCommonColumn = suppressCommonColumn;
    }

    public boolean isSuppressProcedure() {
        return _suppressProcedure;
    }

    public void setSuppressProcedure(boolean suppressProcedure) {
        this._suppressProcedure = suppressProcedure;
    }

    public Map<String, String> getSupplementaryConnectionMap() {
        return _supplementaryConnectionMap;
    }

    public void setSupplementaryConnectionMap(Map<String, String> supplementaryConnectionMap) {
        this._supplementaryConnectionMap = supplementaryConnectionMap;
    }
}
