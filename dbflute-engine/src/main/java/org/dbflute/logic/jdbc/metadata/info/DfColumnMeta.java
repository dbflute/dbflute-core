/*
 * Copyright 2014-2025 the original author or authors.
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
package org.dbflute.logic.jdbc.metadata.info;

import java.util.Map;

import org.dbflute.DfBuildProperties;
import org.dbflute.logic.jdbc.metadata.comment.DfDbCommentExtractor.UserColComments;
import org.dbflute.properties.DfLittleAdjustmentProperties;
import org.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfColumnMeta {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String _tableName; // null allowed if e.g. Sql2Entity, Synonym
    protected String _columnName; // basically not null
    protected int _jdbcDefValue;
    protected String _dbTypeName; // basically not null
    protected int _columnSize;
    protected int _decimalDigits;
    protected Integer _datetimePrecision; // null allowed
    protected boolean _required;
    protected String _columnComment; // null allowed
    protected String _defaultValue; // null allowed

    // -----------------------------------------------------
    //                                            Sql2Entity
    //                                            ----------
    // only when Sql2Entity task, contains procedure result set (null allowed)
    protected String _sql2entityRelatedTableName;
    protected String _sql2entityRelatedColumnName;
    protected String _sql2entityForcedJavaNative;

    // -----------------------------------------------------
    //                                             Procedure
    //                                             ---------
    // only when procedure on Sql2Entity (null allowed)
    protected String _procedureName; // from procedure meta
    protected boolean _procedureParameter;

    // basically only when procedure parameter (null allowed)
    protected DfTypeArrayInfo _typeArrayInfo;
    protected DfTypeStructInfo _typeStructInfo;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfColumnMeta() {
    }

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

    public void acceptDatetimePrecision(Map<String, Integer> columnDatetimePrecisionMap) {
        if (columnDatetimePrecisionMap == null) {
            return;
        }
        Integer datetimePrecision = columnDatetimePrecisionMap.get(_columnName);
        if (datetimePrecision == null) {
            return;
        }
        _datetimePrecision = datetimePrecision;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append(_tableName).append(".").append(_columnName);
        sb.append(", ").append(_dbTypeName);
        sb.append("(");
        if (_datetimePrecision != null) {
            sb.append(_datetimePrecision);
        } else { // mainly here
            sb.append(_columnSize).append(", ").append(_decimalDigits);
        }
        sb.append("), ");
        sb.append(_jdbcDefValue).append(", ").append(_required);
        sb.append(", ").append(_columnComment).append(", ").append(_defaultValue);
        sb.append("}");
        return sb.toString();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getTableName() {
        return _tableName;
    }

    public void setTableName(String tableName) {
        _tableName = tableName;
    }

    public String getColumnName() {
        return _columnName;
    }

    public void setColumnName(String columnName) {
        _columnName = columnName;
    }

    public int getColumnSize() {
        return _columnSize;
    }

    public void setColumnSize(int columnSize) {
        _columnSize = columnSize;
    }

    public int getDecimalDigits() {
        return _decimalDigits;
    }

    public void setDecimalDigits(int decimalDigits) {
        _decimalDigits = decimalDigits;
    }

    public Integer getDatetimePrecision() {
        return _datetimePrecision;
    }

    public void setDatetimePrecision(Integer datetimePrecision) {
        _datetimePrecision = datetimePrecision;
    }

    public String getDefaultValue() {
        return _defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        _defaultValue = defaultValue;
    }

    public boolean isRequired() {
        return _required;
    }

    public void setRequired(boolean required) {
        _required = required;
    }

    public int getJdbcDefValue() {
        return _jdbcDefValue;
    }

    public void setJdbcDefValue(int jdbcDefValue) {
        _jdbcDefValue = jdbcDefValue;
    }

    public String getDbTypeName() {
        return _dbTypeName;
    }

    public void setDbTypeName(String dbTypeName) {
        _dbTypeName = dbTypeName;
    }

    public String getColumnComment() {
        return _columnComment;
    }

    public void setColumnComment(String columnComment) {
        _columnComment = columnComment;
    }

    // -----------------------------------------------------
    //                                            Sql2Entity
    //                                            ----------
    public String getSql2EntityRelatedTableName() {
        return _sql2entityRelatedTableName;
    }

    public void setSql2EntityRelatedTableName(String sql2entityRelatedTableName) {
        _sql2entityRelatedTableName = sql2entityRelatedTableName;
    }

    public String getSql2EntityRelatedColumnName() {
        return _sql2entityRelatedColumnName;
    }

    public void setSql2EntityRelatedColumnName(String sql2entityRelatedColumnName) {
        _sql2entityRelatedColumnName = sql2entityRelatedColumnName;
    }

    public String getSql2EntityForcedJavaNative() {
        return _sql2entityForcedJavaNative;
    }

    public void setSql2EntityForcedJavaNative(String sql2entityForcedJavaNative) {
        _sql2entityForcedJavaNative = sql2entityForcedJavaNative;
    }

    // -----------------------------------------------------
    //                                             Procedure
    //                                             ---------
    public String getProcedureName() {
        return _procedureName;
    }

    public void setProcedureName(String procedureName) {
        _procedureName = procedureName;
    }

    public boolean isProcedureParameter() {
        return _procedureParameter;
    }

    public void setProcedureParameter(boolean procedureParameter) {
        _procedureParameter = procedureParameter;
    }

    public DfTypeArrayInfo getTypeArrayInfo() {
        return _typeArrayInfo;
    }

    public void setTypeArrayInfo(DfTypeArrayInfo typeArrayInfo) {
        _typeArrayInfo = typeArrayInfo;
    }

    public DfTypeStructInfo getTypeStructInfo() {
        return _typeStructInfo;
    }

    public void setTypeStructInfo(DfTypeStructInfo typeStructInfo) {
        _typeStructInfo = typeStructInfo;
    }
}
