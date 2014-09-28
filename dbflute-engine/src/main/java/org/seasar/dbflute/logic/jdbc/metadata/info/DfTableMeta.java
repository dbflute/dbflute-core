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

import java.util.List;
import java.util.Map;

import org.apache.torque.engine.database.model.UnifiedSchema;
import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.logic.jdbc.metadata.comment.DfDbCommentExtractor.UserTabComments;
import org.seasar.dbflute.properties.DfLittleAdjustmentProperties;
import org.seasar.dbflute.resource.DBFluteSystem;

/**
 * @author jflute
 * @since 0.7.0 (2008/04/18 Friday)
 */
public class DfTableMeta {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String _tableName; // NotNull
    protected String _tableType; // NotNull
    protected String _tableComment;
    protected UnifiedSchema _unifiedSchema; // NotNull
    protected boolean _existSameNameTable;
    protected boolean _outOfGenerateTarget;
    protected List<DfColumnMeta> _lazyColumnMetaList; // NullAllowed (additional info for pinpoint)

    // ===================================================================================
    //                                                                       Determination
    //                                                                       =============
    public boolean isTableTypeTable() {
        return _tableType != null ? _tableType.equalsIgnoreCase("TABLE") : false;
    }

    public boolean isTableTypeView() {
        return _tableType != null ? _tableType.equalsIgnoreCase("VIEW") : false;
    }

    public boolean isTableTypeAlias() {
        return _tableType != null ? _tableType.equalsIgnoreCase("ALIAS") : false;
    }

    public boolean isTableTypeSynonym() {
        return _tableType != null ? _tableType.equalsIgnoreCase("SYNONYM") : false;
    }

    public boolean canHandleSynonym() {
        return isTableTypeSynonym() || isTableTypeAlias();
    }

    public boolean hasTableComment() {
        return _tableComment != null && _tableComment.trim().length() > 0;
    }

    // ===================================================================================
    //                                                                       Name Building
    //                                                                       =============
    public String getTableDbName() {
        if (_unifiedSchema == null) {
            return _tableName;
        }
        final String drivenSchema = _unifiedSchema.getDrivenSchema();
        if (drivenSchema == null) {
            return _tableName;
        }
        return drivenSchema + "." + _tableName;
    }

    public String getTableFullQualifiedName() {
        return _unifiedSchema.buildFullQualifiedName(_tableName);
    }

    public String getSchemaQualifiedName() {
        return _unifiedSchema.buildSchemaQualifiedName(_tableName);
    }

    public String getTableSqlName() {
        final DfLittleAdjustmentProperties prop = DfBuildProperties.getInstance().getLittleAdjustmentProperties();
        final String quotedName = prop.quoteTableNameIfNeedsDirectUse(_tableName);
        return _unifiedSchema.buildSqlName(quotedName); // driven is resolved here so it uses pure name here
    }

    // ===================================================================================
    //                                                                              Accept
    //                                                                              ======
    public void acceptTableComment(Map<String, UserTabComments> tableCommentMap) {
        if (tableCommentMap == null) {
            return;
        }
        final UserTabComments userTabComments = tableCommentMap.get(_tableName);
        if (userTabComments != null && userTabComments.hasComments()) {
            _tableComment = userTabComments.getComments();
        }
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof DfTableMeta) {
            return getTableFullQualifiedName().equals(((DfTableMeta) obj).getTableFullQualifiedName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getTableFullQualifiedName().hashCode();
    }

    @Override
    public String toString() {
        String comment = "";
        if (_tableComment != null) {
            final int indexOf = _tableComment.indexOf(ln());
            if (indexOf > 0) { // not contain 0 because ignore first line separator
                comment = _tableComment.substring(0, indexOf) + "..."; // until line separator
            } else {
                comment = _tableComment;
            }
        }
        return getTableFullQualifiedName() + "(" + _tableType + ")"
                + (comment.trim().length() > 0 ? " // " + comment : "");
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String ln() {
        return DBFluteSystem.getBasicLn();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getTableName() {
        return _tableName;
    }

    public void setTableName(String tableName) {
        this._tableName = tableName;
    }

    public String getTableType() {
        return _tableType;
    }

    public void setTableType(String tableType) {
        this._tableType = tableType;
    }

    public String getTableComment() {
        return _tableComment;
    }

    public void setTableComment(String tableComment) {
        this._tableComment = tableComment;
    }

    public UnifiedSchema getUnifiedSchema() {
        return _unifiedSchema;
    }

    public void setUnifiedSchema(UnifiedSchema unifiedSchema) {
        this._unifiedSchema = unifiedSchema;
    }

    public boolean isOutOfGenerateTarget() {
        return _outOfGenerateTarget;
    }

    public void setOutOfGenerateTarget(boolean outOfGenerateTarget) {
        this._outOfGenerateTarget = outOfGenerateTarget;
    }

    public List<DfColumnMeta> getLazyColumnMetaList() {
        return _lazyColumnMetaList;
    }

    public void setLazyColumnMetaList(List<DfColumnMeta> lazyColumnMetaList) {
        this._lazyColumnMetaList = lazyColumnMetaList;
    }
}
