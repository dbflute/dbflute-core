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

import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.logic.jdbc.metadata.comment.DfDbCommentExtractor.UserColComments;
import org.seasar.dbflute.properties.DfLittleAdjustmentProperties;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfColumnMeta {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String _tableName;
    protected String _columnName;
    protected int _jdbcDefValue;
    protected String _dbTypeName;
    protected int _columnSize;
    protected int _decimalDigits;
    protected boolean _required;
    protected String _columnComment;
    protected String _defaultValue;

    // only when Sql2Entity task
    protected String _sql2entityRelatedTableName;
    protected String _sql2entityRelatedColumnName;
    protected String _sql2entityForcedJavaNative;
    protected boolean _procedureParameter;

    // basically only when procedure parameter
    protected DfTypeArrayInfo _typeArrayInfo;
    protected DfTypeStructInfo _typeStructInfo;

    // ===================================================================================
    //                                                                       Determination
    //                                                                       =============
    public boolean hasColumnComment() {
        return _columnComment != null && _columnComment.trim().length() > 0;
    }

    public boolean hasTypeArrayInfo() {
        return _typeArrayInfo != null;
    }

    public boolean hasTypeStructInfo() {
        return _typeStructInfo != null;
    }

    // ===================================================================================
    //                                                                     DBMS Dependency
    //                                                                     ===============
    public boolean isSybaseAutoIncrement() {
        return Srl.equalsIgnoreCase("autoincrement", _defaultValue);
    }

    // ===================================================================================
    //                                                                       Name Building
    //                                                                       =============
    public String buildColumnSqlName() {
        final DfLittleAdjustmentProperties prop = DfBuildProperties.getInstance().getLittleAdjustmentProperties();
        return prop.quoteColumnNameIfNeedsDirectUse(_columnName);
    }

    // ===================================================================================
    //                                                                              Accept
    //                                                                              ======
    public void acceptColumnComment(Map<String, UserColComments> columnCommentMap) {
        if (columnCommentMap == null) {
            return;
        }
        final UserColComments userColComments = columnCommentMap.get(_columnName);
        if (userColComments == null) {
            return;
        }
        final String comment = userColComments.getComments();
        if (comment != null && comment.trim().length() > 0) {
            _columnComment = comment;
        }
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return "{" + _tableName + "." + _columnName + ", " + _dbTypeName + "(" + _columnSize + "," + _decimalDigits
                + "), " + _jdbcDefValue + ", " + _required + ", " + _columnComment + ", " + _defaultValue + "}";
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

    public String getColumnName() {
        return _columnName;
    }

    public void setColumnName(String columnName) {
        this._columnName = columnName;
    }

    public int getColumnSize() {
        return _columnSize;
    }

    public void setColumnSize(int columnSize) {
        this._columnSize = columnSize;
    }

    public int getDecimalDigits() {
        return _decimalDigits;
    }

    public void setDecimalDigits(int decimalDigits) {
        this._decimalDigits = decimalDigits;
    }

    public String getDefaultValue() {
        return _defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this._defaultValue = defaultValue;
    }

    public boolean isRequired() {
        return _required;
    }

    public void setRequired(boolean required) {
        this._required = required;
    }

    public int getJdbcDefValue() {
        return _jdbcDefValue;
    }

    public void setJdbcDefValue(int jdbcDefValue) {
        this._jdbcDefValue = jdbcDefValue;
    }

    public String getDbTypeName() {
        return _dbTypeName;
    }

    public void setDbTypeName(String dbTypeName) {
        this._dbTypeName = dbTypeName;
    }

    public String getColumnComment() {
        return _columnComment;
    }

    public void setColumnComment(String columnComment) {
        this._columnComment = columnComment;
    }

    public String getSql2EntityRelatedTableName() {
        return _sql2entityRelatedTableName;
    }

    public void setSql2EntityRelatedTableName(String sql2entityRelatedTableName) {
        this._sql2entityRelatedTableName = sql2entityRelatedTableName;
    }

    public String getSql2EntityRelatedColumnName() {
        return _sql2entityRelatedColumnName;
    }

    public void setSql2EntityRelatedColumnName(String sql2entityRelatedColumnName) {
        this._sql2entityRelatedColumnName = sql2entityRelatedColumnName;
    }

    public String getSql2EntityForcedJavaNative() {
        return _sql2entityForcedJavaNative;
    }

    public void setSql2EntityForcedJavaNative(String sql2entityForcedJavaNative) {
        this._sql2entityForcedJavaNative = sql2entityForcedJavaNative;
    }

    public boolean isProcedureParameter() {
        return _procedureParameter;
    }

    public void setProcedureParameter(boolean procedureParameter) {
        this._procedureParameter = procedureParameter;
    }

    public DfTypeArrayInfo getTypeArrayInfo() {
        return _typeArrayInfo;
    }

    public void setTypeArrayInfo(DfTypeArrayInfo typeArrayInfo) {
        this._typeArrayInfo = typeArrayInfo;
    }

    public DfTypeStructInfo getTypeStructInfo() {
        return _typeStructInfo;
    }

    public void setTypeStructInfo(DfTypeStructInfo typeStructInfo) {
        this._typeStructInfo = typeStructInfo;
    }
}
