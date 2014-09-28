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
package org.seasar.dbflute.logic.sql2entity.cmentity;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.torque.engine.database.model.UnifiedSchema;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfColumnMeta;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfTypeStructInfo;
import org.seasar.dbflute.logic.sql2entity.analyzer.DfOutsideSqlFile;
import org.seasar.dbflute.logic.sql2entity.pmbean.DfPmbMetaData;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfCustomizeEntityInfo {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _tableDbName;
    protected final Map<String, DfColumnMeta> _columnMap;
    protected final DfTypeStructInfo _typeStructInfo;

    // additional information (outsideSql only)
    protected File _sqlFile;
    protected List<String> _primaryKeyList;
    protected boolean _cursorHandling;
    protected boolean _scalarHandling;
    protected boolean _domainHandling;
    protected DfPmbMetaData _pmbMetaData; // is a related parameter-bean
    protected String _entityClassName; // is set immediately before generation
    protected String _immutableEntityClassName; // is set immediately before generation

    // only when scalar handling
    protected String _scalarJavaNative;
    protected String _scalarColumnDisp;

    // additional information (procedure only)
    protected boolean _procedureHandling;

    // SQL file defined at
    protected DfOutsideSqlFile _outsideSqlFile;

    // supplementary comment
    protected Map<String, String> _selectColumnCommentMap;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfCustomizeEntityInfo(String tableDbName, Map<String, DfColumnMeta> columnMap) {
        this(tableDbName, columnMap, null);
    }

    public DfCustomizeEntityInfo(String tableDbName, Map<String, DfColumnMeta> columnMap,
            DfTypeStructInfo typeStructInfo) {
        _tableDbName = tableDbName;
        _columnMap = columnMap;
        _typeStructInfo = typeStructInfo;
    }

    // ===================================================================================
    //                                                                       Determination
    //                                                                       =============
    public boolean isResultHandling() {
        return !_cursorHandling;
    }

    public boolean hasTypeStructInfo() {
        return _typeStructInfo != null;
    }

    public boolean needsJavaNameConvert() {
        return hasTypeStructInfo();
    }

    public boolean hasNestedCustomizeEntity() {
        return hasTypeStructInfo() && _typeStructInfo.hasNestedStructEntityRef();
    }

    public boolean isAdditionalSchema() {
        return hasTypeStructInfo() && _typeStructInfo.isAdditinalSchema();
    }

    // ===================================================================================
    //                                                                   Additional Schema
    //                                                                   =================
    public UnifiedSchema getAdditionalSchema() {
        return hasTypeStructInfo() ? _typeStructInfo.getOwner() : null;
    }

    // ===================================================================================
    //                                                               Select Column Comment
    //                                                               =====================
    public void acceptSelectColumnComment(Map<String, String> commentMap) {
        if (commentMap == null || commentMap.isEmpty()) {
            return;
        }
        for (Entry<String, DfColumnMeta> entry : _columnMap.entrySet()) {
            final String columnName = entry.getKey();
            final String selectColumnComment = commentMap.get(columnName); // commentMap should be flexible
            if (Srl.is_NotNull_and_NotTrimmedEmpty(selectColumnComment)) {
                final DfColumnMeta meta = entry.getValue();
                meta.setColumnComment(selectColumnComment); // basically new-set (get no meta comment)
                if (selectColumnComment.startsWith("*")) { // means not-null e.g. -- // *Member Name
                    meta.setRequired(true);
                }
            }
        }
    }

    // ===================================================================================
    //                                                                             Display
    //                                                                             =======
    public String buildHandlingDisp() {
        if (isCursorHandling()) {
            return "(cursor)";
        }
        if (isScalarHandling()) {
            return "(scalar)";
        }
        if (isDomainHandling()) {
            return "(domain)";
        }
        if (isProcedureHandling()) {
            if (hasTypeStructInfo()) {
                return "(procedure, struct)";
            } else {
                return "(procedure)";
            }
        } else if (hasTypeStructInfo()) {
            return "(struct)";
        }
        return "";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getTableDbName() {
        return _tableDbName;
    }

    public Map<String, DfColumnMeta> getColumnMap() {
        return _columnMap;
    }

    public DfTypeStructInfo getTypeStructInfo() {
        return _typeStructInfo;
    }

    public File getSqlFile() {
        return _sqlFile;
    }

    public void setSqlFile(File sqlFile) {
        _sqlFile = sqlFile;
    }

    public List<String> getPrimaryKeyList() {
        return _primaryKeyList;
    }

    public void setPrimaryKeyList(List<String> primaryKeyList) {
        _primaryKeyList = primaryKeyList;
    }

    public boolean isCursorHandling() {
        return _cursorHandling;
    }

    public void setCursorHandling(boolean cursorHandling) {
        _cursorHandling = cursorHandling;
    }

    public boolean isScalarHandling() {
        return _scalarHandling;
    }

    public void setScalarHandling(boolean scalarHandling) {
        _scalarHandling = scalarHandling;
    }

    public boolean isDomainHandling() {
        return _domainHandling;
    }

    public void setDomainHandling(boolean domainHandling) {
        _domainHandling = domainHandling;
    }

    public DfPmbMetaData getPmbMetaData() {
        return _pmbMetaData;
    }

    public void setPmbMetaData(DfPmbMetaData pmbMetaData) {
        _pmbMetaData = pmbMetaData;
    }

    public String getEntityClassName() {
        return _entityClassName;
    }

    public void setEntityClassName(String entityClassName) {
        _entityClassName = entityClassName;
    }

    public String getImmutableEntityClassName() {
        return _immutableEntityClassName;
    }

    public void setImmutableClassName(String immutableEntityClassName) {
        _immutableEntityClassName = immutableEntityClassName;
    }

    public String getScalarJavaNative() {
        return _scalarJavaNative;
    }

    public void setScalarJavaNative(String scalarJavaNative) {
        _scalarJavaNative = scalarJavaNative;
    }

    public String getScalarColumnDisp() {
        return _scalarColumnDisp;
    }

    public void setScalarColumnDisp(String scalarColumnDisp) {
        _scalarColumnDisp = scalarColumnDisp;
    }

    public boolean isProcedureHandling() {
        return _procedureHandling;
    }

    public void setProcedureHandling(boolean procedureHandling) {
        _procedureHandling = procedureHandling;
    }

    public DfOutsideSqlFile getOutsideSqlFile() {
        return _outsideSqlFile;
    }

    public void setOutsideSqlFile(DfOutsideSqlFile outsideSqlFile) {
        _outsideSqlFile = outsideSqlFile;
    }

    public Map<String, String> getSelectColumnCommentMap() {
        return _selectColumnCommentMap;
    }

    public void setSelectColumnCommentMap(Map<String, String> selectColumnCommentMap) {
        _selectColumnCommentMap = selectColumnCommentMap;
    }
}
