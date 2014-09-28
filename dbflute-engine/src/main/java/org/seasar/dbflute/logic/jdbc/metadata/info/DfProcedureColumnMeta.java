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
import org.seasar.dbflute.logic.jdbc.metadata.basic.DfColumnExtractor;
import org.seasar.dbflute.properties.DfBasicProperties;
import org.seasar.dbflute.properties.DfDocumentProperties;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfProcedureColumnMeta {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String _columnName;
    protected int _jdbcDefType;
    protected String _dbTypeName;
    protected Integer _columnSize;
    protected Integer _decimalDigits;
    protected Integer _overloadNo; // for example, Oracle's package procedure (by supplement meta)
    protected String _columnComment;
    protected DfProcedureColumnType _procedureColumnType;

    // if the informations can be extracted
    // (if these attributes are null, it's not always true that these are other types)
    protected Map<String, DfColumnMeta> _resultSetColumnInfoMap; // when ResultSet type (by execution meta)
    protected DfTypeArrayInfo _typeArrayInfo; // when ARRAY type (by supplement meta)
    protected DfTypeStructInfo _typeStructInfo; // when STRUCT type (by supplement meta)

    protected final DfColumnExtractor _columnHandler = new DfColumnExtractor(); // for type determination

    // ===================================================================================
    //                                                                Status Determination
    //                                                                ====================
    public boolean hasColumnComment() {
        return Srl.is_NotNull_and_NotTrimmedEmpty(_columnComment);
    }

    public boolean hasResultSetColumnInfo() {
        return _resultSetColumnInfoMap != null && !_resultSetColumnInfoMap.isEmpty();
    }

    public boolean hasTypeArrayInfo() {
        return _typeArrayInfo != null;
    }

    public boolean hasTypeArrayElementJavaNative() {
        return hasTypeArrayInfo() && _typeArrayInfo.hasElementJavaNative();
    }

    public boolean hasTypeStructInfo() {
        return _typeStructInfo != null;
    }

    public boolean hasTypeStructEntityType() {
        return hasTypeStructInfo() && _typeStructInfo.hasEntityType();
    }

    // ===================================================================================
    //                                                                             Display
    //                                                                             =======
    public String getColumnDisplayName() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getColumnNameDisp());
        sb.append(": ");
        sb.append(_dbTypeName);
        if (hasTypeArrayInfo()) {
            sb.append("(").append(getTypeArrayInfo().toString()).append(")");
        }
        sb.append(getColumnSizeDisp());
        sb.append(" as ").append(_procedureColumnType.alias());
        return sb.toString();
    }

    public String getColumnDisplayNameForSchemaHtml() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getColumnNameDisp());
        sb.append(" - ");
        sb.append(_dbTypeName);
        sb.append(getColumnSizeDisp());
        if (hasTypeArrayInfo()) {
            sb.append(" <span class=\"attrs\">{");
            sb.append(getTypeArrayInfo().toStringForHtml());
            sb.append("}</span>");
        } else if (hasTypeStructInfo()) {
            sb.append(" <span class=\"attrs\">{");
            sb.append(getTypeStructInfo().toStringAttributeOnlyForHtml());
            sb.append("}</span>");
        }
        sb.append(" <span class=\"type\">(").append(_procedureColumnType.alias()).append(")</span>");
        return sb.toString();
    }

    public String getColumnNameDisp() {
        if (Srl.is_NotNull_and_NotTrimmedEmpty(_columnName)) {
            return _columnName;
        } else {
            if (DfProcedureColumnType.procedureColumnReturn.equals(_procedureColumnType)) {
                return "(return)";
            } else {
                return "(arg)";
            }
        }
    }

    public String getColumnSizeDisp() {
        final StringBuilder sb = new StringBuilder();
        if (DfColumnExtractor.isColumnSizeValid(_columnSize)) {
            sb.append("(").append(_columnSize);
            if (DfColumnExtractor.isDecimalDigitsValid(_decimalDigits)) {
                sb.append(", ").append(_decimalDigits);
            }
            sb.append(")");
        }
        return sb.toString();
    }

    public String getColumnDefinitionLineDisp() {
        final StringBuilder sb = new StringBuilder();
        sb.append(_dbTypeName);
        if (DfColumnExtractor.isColumnSizeValid(_columnSize)) {
            sb.append("(").append(_columnSize);
            if (DfColumnExtractor.isDecimalDigitsValid(_decimalDigits)) {
                sb.append(", ").append(_decimalDigits);
            }
            sb.append(")");
        }
        sb.append(" as ").append(_procedureColumnType.alias());
        return sb.toString();
    }

    public String getColumnCommentForSchemaHtml() {
        final DfDocumentProperties prop = DfBuildProperties.getInstance().getDocumentProperties();
        String comment = _columnComment;
        comment = prop.resolvePreTextForSchemaHtml(comment);
        return comment;
    }

    // ===================================================================================
    //                                                                 Definition Identity
    //                                                                 ===================
    public String getColumnDefinitionIndentity() {
        final StringBuilder sb = new StringBuilder();
        sb.append(_columnName);
        sb.append(" ");
        sb.append(_dbTypeName);
        if (DfColumnExtractor.isColumnSizeValid(_columnSize)) {
            sb.append("(").append(_columnSize);
            if (DfColumnExtractor.isDecimalDigitsValid(_decimalDigits)) {
                sb.append(", ").append(_decimalDigits);
            }
            sb.append(")");
        }
        sb.append(" as ").append(_procedureColumnType.alias());
        return sb.toString();
    }

    // ===================================================================================
    //                                                             Parameter Determination
    //                                                             =======================
    public boolean isBindParameter() {
        final DfProcedureColumnType columnType = _procedureColumnType;
        if (DfProcedureColumnType.procedureColumnResult.equals(columnType) // just in case
                || isSQLServerTableReturnValue()) { // pinpoint patch
            return false;
        }
        return true; // almost true

        // RESULT type does not have a column info basically
        // but sometimes it can be, so checks it just in case
    }

    public boolean isInputParameter() {
        final DfProcedureColumnType columnType = _procedureColumnType;
        if (DfProcedureColumnType.procedureColumnReturn.equals(columnType)
                || DfProcedureColumnType.procedureColumnResult.equals(columnType) // just in case
                || isSQLServerTableReturnValue()) { // pinpoint patch
            return false;
        }
        if (DfProcedureColumnType.procedureColumnIn.equals(columnType)
                || DfProcedureColumnType.procedureColumnInOut.equals(columnType)) {
            return true;
        }
        return false;
    }

    // ===================================================================================
    //                                                                  Type Determination
    //                                                                  ==================
    public boolean isConceptTypeStringClob() {
        final String dbTypeName = getDbTypeName();
        return _columnHandler.isConceptTypeStringClob(dbTypeName);
    }

    public boolean isConceptTypeBytesOid() {
        final String dbTypeName = getDbTypeName();
        return _columnHandler.isConceptTypeBytesOid(dbTypeName);
    }

    public boolean isConceptTypeFixedLengthString() {
        final String dbTypeName = getDbTypeName();
        return _columnHandler.isConceptTypeFixedLengthString(dbTypeName);
    }

    public boolean isConceptTypeObjectBindingBigDecimal() {
        final String dbTypeName = getDbTypeName();
        return _columnHandler.isConceptTypeObjectBindingBigDecimal(dbTypeName);
    }

    public boolean isPostgreSQLUuid() {
        final String dbTypeName = getDbTypeName();
        return _columnHandler.isPostgreSQLUuid(dbTypeName);
    }

    public boolean isPostgreSQLOid() {
        final String dbTypeName = getDbTypeName();
        return _columnHandler.isPostgreSQLOid(dbTypeName);
    }

    public boolean isPostgreSQLCursor() {
        final String dbTypeName = getDbTypeName();
        return _columnHandler.isPostgreSQLCursor(dbTypeName);
    }

    public boolean isOracleNCharOrNVarchar() {
        final String dbTypeName = getDbTypeName();
        return _columnHandler.isOracleNCharOrNVarchar(dbTypeName);
    }

    public boolean isOracleNumber() {
        final String dbTypeName = getDbTypeName();
        return _columnHandler.isOracleNumber(dbTypeName);
    }

    public boolean isOracleCursor() {
        final String dbTypeName = getDbTypeName();
        return _columnHandler.isOracleCursor(dbTypeName);
    }

    public boolean isOracleStruct() {
        // STRUCT is unknown by dbTypeName
        // so determines from Data Dictionary
        return _typeStructInfo != null;
    }

    public boolean isOracleTreatedAsArray() {
        final String dbTypeName = getDbTypeName();
        return _columnHandler.isOracleTreatedAsArray(dbTypeName);
    }

    public boolean isSQLServerUniqueIdentifier() {
        final String dbTypeName = getDbTypeName();
        return _columnHandler.isSQLServerUniqueIdentifier(dbTypeName);
    }

    public boolean isSQLServerTableReturnValue() {
        if (!getBasicProperties().isDatabaseSQLServer()) {
            return false;
        }
        // it ignores column type of procedure here
        // (basically this has RESULT type but only a name is enough to determine)
        return "@TABLE_RETURN_VALUE".equalsIgnoreCase(getColumnName());
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    protected DfBasicProperties getBasicProperties() {
        return DfBuildProperties.getInstance().getBasicProperties();
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return "{" + _columnName + ", " + _procedureColumnType + ", " + _jdbcDefType + ", " + _dbTypeName + "("
                + _columnSize + ", " + _decimalDigits + ")" + _columnComment + "}";
    }

    // ===================================================================================
    //                                                                      Procedure Type
    //                                                                      ==============
    public enum DfProcedureColumnType {
        procedureColumnUnknown("Unknown"), procedureColumnIn("In"), procedureColumnInOut("InOut"), procedureColumnOut(
                "Out"), procedureColumnReturn("Return"), procedureColumnResult("Result");
        private final String _alias;

        private DfProcedureColumnType(String alias) {
            _alias = alias;
        }

        public String alias() {
            return _alias;
        }
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getColumnName() {
        return _columnName;
    }

    public void setColumnName(String columnName) {
        this._columnName = columnName;
    }

    public DfProcedureColumnType getProcedureColumnType() {
        return _procedureColumnType;
    }

    public void setProcedureColumnType(DfProcedureColumnType procedureColumnType) {
        this._procedureColumnType = procedureColumnType;
    }

    public int getJdbcDefType() {
        return _jdbcDefType;
    }

    public void setJdbcDefType(int jdbcDefType) {
        this._jdbcDefType = jdbcDefType;
    }

    public String getDbTypeName() {
        return _dbTypeName;
    }

    public void setDbTypeName(String dbTypeName) {
        this._dbTypeName = dbTypeName;
    }

    public Integer getColumnSize() {
        return _columnSize;
    }

    public void setColumnSize(Integer columnSize) {
        this._columnSize = columnSize;
    }

    public Integer getDecimalDigits() {
        return _decimalDigits;
    }

    public void setDecimalDigits(Integer decimalDigits) {
        this._decimalDigits = decimalDigits;
    }

    public Integer getOverloadNo() {
        return _overloadNo;
    }

    public void setOverloadNo(Integer overloadNo) {
        this._overloadNo = overloadNo;
    }

    public String getColumnComment() {
        return _columnComment;
    }

    public void setColumnComment(String columnComment) {
        this._columnComment = columnComment;
    }

    public Map<String, DfColumnMeta> getResultSetColumnInfoMap() {
        return _resultSetColumnInfoMap;
    }

    public void setResultSetColumnInfoMap(Map<String, DfColumnMeta> resultSetColumnInfoMap) {
        this._resultSetColumnInfoMap = resultSetColumnInfoMap;
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
