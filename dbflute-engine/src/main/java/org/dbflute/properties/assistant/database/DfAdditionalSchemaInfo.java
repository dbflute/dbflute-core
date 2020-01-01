/*
 * Copyright 2014-2020 the original author or authors.
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
package org.dbflute.properties.assistant.database;

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
    protected List<String> _tableTargetList;
    protected List<String> _tableTargetGenOnlyList;
    protected Map<String, List<String>> _columnExceptMap;
    protected Map<String, List<String>> _columnExceptGenOnlyMap;
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
        _unifiedSchema = unifiedSchema;
    }

    public List<String> getObjectTypeTargetList() {
        return _objectTypeTargetList;
    }

    public void setObjectTypeTargetList(List<String> objectTypeTargetList) {
        _objectTypeTargetList = objectTypeTargetList;
    }

    public List<String> getTableExceptList() {
        return _tableExceptList;
    }

    public void setTableExceptList(List<String> tableExceptList) {
        _tableExceptList = tableExceptList;
    }

    public List<String> getTableExceptGenOnlyList() {
        return _tableExceptGenOnlyList;
    }

    public void setTableExceptGenOnlyList(List<String> tableExceptGenOnlyList) {
        _tableExceptGenOnlyList = tableExceptGenOnlyList;
    }

    public List<String> getTableTargetList() {
        return _tableTargetList;
    }

    public void setTableTargetList(List<String> tableTargetList) {
        _tableTargetList = tableTargetList;
    }

    public List<String> getTableTargetGenOnlyList() {
        return _tableTargetGenOnlyList;
    }

    public void setTableTargetGenOnlyList(List<String> tableTargetGenOnlyList) {
        _tableTargetGenOnlyList = tableTargetGenOnlyList;
    }

    public Map<String, List<String>> getColumnExceptMap() {
        return _columnExceptMap;
    }

    public void setColumnExceptMap(Map<String, List<String>> columnExceptMap) {
        _columnExceptMap = columnExceptMap;
    }

    public Map<String, List<String>> getColumnExceptGenOnlyMap() {
        return _columnExceptGenOnlyMap;
    }

    public void setColumnExceptGenOnlyMap(Map<String, List<String>> columnExceptGenOnlyMap) {
        _columnExceptGenOnlyMap = columnExceptGenOnlyMap;
    }

    public boolean isSuppressCommonColumn() {
        return _suppressCommonColumn;
    }

    public void setSuppressCommonColumn(boolean suppressCommonColumn) {
        _suppressCommonColumn = suppressCommonColumn;
    }

    public boolean isSuppressProcedure() {
        return _suppressProcedure;
    }

    public void setSuppressProcedure(boolean suppressProcedure) {
        _suppressProcedure = suppressProcedure;
    }

    public Map<String, String> getSupplementaryConnectionMap() {
        return _supplementaryConnectionMap;
    }

    public void setSupplementaryConnectionMap(Map<String, String> supplementaryConnectionMap) {
        _supplementaryConnectionMap = supplementaryConnectionMap;
    }
}
