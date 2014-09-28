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

import org.apache.torque.engine.database.model.UnifiedSchema;
import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.properties.DfBasicProperties;
import org.seasar.dbflute.properties.DfDocumentProperties;
import org.seasar.dbflute.properties.DfLittleAdjustmentProperties;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfProcedureMeta {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String _procedureCatalog; // not required
    protected UnifiedSchema _procedureSchema; // required (if DBLink, DBLink name)
    protected String _procedureName; // contains package prefix and DBLink mark (e.g. MAIN_PKG.FOO, FOO@BAR_LINK)
    protected DfProcedureType _procedureType;
    protected String _procedureFullQualifiedName;
    protected String _procedureSchemaQualifiedName;
    protected String _procedureSqlName; // basically for procedure synonym
    protected String _procedureComment;
    protected String _procedurePackage; // basically for dropping procedure
    protected boolean _procedureSynonym;
    protected boolean _includedProcedureToDBLink;

    protected final List<DfProcedureColumnMeta> _procedureColumnList = DfCollectionUtil.newArrayList(); // procedure parameters
    protected final List<DfProcedureNotParamResultMeta> _notParamResultList = DfCollectionUtil.newArrayList(); // by execution meta
    protected DfProcedureSourceInfo _procedureSourceInfo; // assist info (basically for schema diff)

    // ===================================================================================
    //                                                                          Expression
    //                                                                          ==========
    public String getProcedureDisplayName() {
        return buildProcedureSqlName();
    }

    public String getProcedureDisplayNameForSchemaHtml() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getProcedureDisplayName());

        final String typeDisp = _procedureType.alias() + (_procedureSynonym ? ", Synonym" : "");
        sb.append(" <span class=\"type\">(").append(typeDisp).append(")</span>");
        return sb.toString();
    }

    public boolean hasProcedureComment() {
        return Srl.is_NotNull_and_NotTrimmedEmpty(_procedureComment);
    }

    public String getProcedureCommentForSchemaHtml() {
        final DfDocumentProperties prop = getDocumentProperties();
        String comment = _procedureComment;
        comment = prop.resolvePreTextForSchemaHtml(comment);
        return comment;
    }

    public boolean isPackageProcdure() {
        return Srl.is_NotNull_and_NotTrimmedEmpty(_procedurePackage);
    }

    // -----------------------------------------------------
    //                                            Build Name
    //                                            ----------
    public String buildProcedureKeyName() {
        final String drivenSchema = _procedureSchema.getDrivenSchema();
        return (drivenSchema != null ? drivenSchema + "." : "") + _procedureName;
    }

    public String buildProcedureLoggingName() {
        return _procedureFullQualifiedName;
    }

    public String buildProcedureSqlName() {
        if (Srl.is_NotNull_and_NotTrimmedEmpty(_procedureSqlName)) {
            return _procedureSqlName;
        }
        final DfBasicProperties prop = getBasicProperties();
        final String sqlName = getProcedureSchema().buildSqlName(getProcedureName());
        if (prop.isDatabaseDB2() && !sqlName.contains(".")) { // patch
            // DB2 needs schema prefix for calling procedures
            // (actually executed and confirmed result)
            _procedureSqlName = getProcedureSchema().buildSchemaQualifiedName(sqlName);
        } else {
            _procedureSqlName = sqlName;
        }
        if (prop.isDatabaseSQLServer()) {
            // SQLServer returns 'sp_foo;1'
            _procedureSqlName = Srl.substringLastFront(_procedureSqlName, ";");
        }
        return _procedureSqlName;
    }

    public String buildProcedurePureName() {
        return Srl.substringLastRear(_procedureName, ".");
    }

    // ===================================================================================
    //                                                                    Bind Information
    //                                                                    ================
    public int getBindParameterCount() {
        int count = 0;
        for (DfProcedureColumnMeta columnInfo : _procedureColumnList) {
            if (columnInfo.isBindParameter()) {
                ++count;
            }
        }
        return count;
    }

    public int getInputParameterCount() {
        int count = 0;
        for (DfProcedureColumnMeta columnInfo : _procedureColumnList) {
            if (columnInfo.isInputParameter()) {
                ++count;
            }
        }
        return count;
    }

    public String getColumnDefinitionIndentity() {
        final StringBuilder sb = new StringBuilder();
        int index = 0;
        for (DfProcedureColumnMeta columnMeta : _procedureColumnList) {
            if (index > 0) {
                sb.append(", ");
            }
            sb.append(columnMeta.getColumnDefinitionIndentity());
            ++index;
        }
        return sb.toString();
    }

    // ===================================================================================
    //                                                             Execution Determination
    //                                                             =======================
    // -----------------------------------------------------
    //                                               Calling
    //                                               -------
    public boolean isCalledBySelect() {
        // SQLServer's table valued function cannot be called normally
        // (whether that others like this exist or not is unknown for now)
        return isSQLServerTableValuedFunction();
    }

    // -----------------------------------------------------
    //                                              Overload
    //                                              --------
    public boolean hasOverloadParameter() {
        for (DfProcedureColumnMeta columnInfo : _procedureColumnList) {
            if (columnInfo.getOverloadNo() != null) {
                return true;
            }
        }
        return false;
    }

    // ===================================================================================
    //                                                              Pinpoint Determination
    //                                                              ======================
    public boolean isSQLServerTableValuedFunction() {
        if (!getBasicProperties().isDatabaseSQLServer()) {
            return false;
        }
        for (DfProcedureColumnMeta columnInfo : _procedureColumnList) {
            if (columnInfo.isSQLServerTableReturnValue()) {
                return true;
            }
        }
        return false;
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    protected DfBasicProperties getBasicProperties() {
        return getProperties().getBasicProperties();
    }

    protected DfDocumentProperties getDocumentProperties() {
        return getProperties().getDocumentProperties();
    }

    protected DfLittleAdjustmentProperties getLittleAdjustmentProperties() {
        return getProperties().getLittleAdjustmentProperties();
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return "{" + _procedureFullQualifiedName + ", " + _procedureType + ", " + _procedureComment + ", "
                + _procedureColumnList + ", notParamResult=" + _notParamResultList.size() + "}";
    }

    // ===================================================================================
    //                                                                      Procedure Type
    //                                                                      ==============
    public enum DfProcedureType {
        procedureResultUnknown("ResultUnknown"), procedureNoResult("NoResult"), procedureReturnsResult("ReturnsResult");
        private String _alias;

        private DfProcedureType(String alias) {
            _alias = alias;
        }

        public String alias() {
            return _alias;
        }
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getProcedureCatalog() {
        return _procedureCatalog;
    }

    public void setProcedureCatalog(String procedureCatalog) {
        this._procedureCatalog = procedureCatalog;
    }

    public UnifiedSchema getProcedureSchema() {
        return _procedureSchema;
    }

    public void setProcedureSchema(UnifiedSchema procedureSchema) {
        this._procedureSchema = procedureSchema;
    }

    public String getProcedureName() {
        return _procedureName;
    }

    public void setProcedureName(String procedureName) {
        this._procedureName = procedureName;
    }

    public DfProcedureType getProcedureType() {
        return _procedureType;
    }

    public void setProcedureType(DfProcedureType procedureType) {
        this._procedureType = procedureType;
    }

    public String getProcedureFullQualifiedName() {
        return _procedureFullQualifiedName;
    }

    public void setProcedureFullQualifiedName(String procedureFullQualifiedName) {
        this._procedureFullQualifiedName = procedureFullQualifiedName;
    }

    public String getProcedureSchemaQualifiedName() {
        return _procedureSchemaQualifiedName;
    }

    public void setProcedureSchemaQualifiedName(String procedureSchemaQualifiedName) {
        this._procedureSchemaQualifiedName = procedureSchemaQualifiedName;
    }

    public void setProcedureSqlName(String procedureSqlName) { // basically for procedure synonym
        this._procedureSqlName = procedureSqlName;
    }

    public String getProcedureComment() {
        return _procedureComment;
    }

    public void setProcedureComment(String procedureComment) {
        this._procedureComment = procedureComment;
    }

    public String getProcedurePackage() {
        return _procedurePackage;
    }

    public void setProcedurePackage(String procedurePackage) {
        this._procedurePackage = procedurePackage;
    }

    public boolean isProcedureSynonym() {
        return _procedureSynonym;
    }

    public void setProcedureSynonym(boolean procedureSynonym) {
        this._procedureSynonym = procedureSynonym;
    }

    public boolean isIncludedProcedureToDBLink() {
        return _includedProcedureToDBLink;
    }

    public void setIncludedProcedureToDBLink(boolean includedProcedureToDBLink) {
        this._includedProcedureToDBLink = includedProcedureToDBLink;
    }

    public List<DfProcedureColumnMeta> getProcedureColumnList() {
        return _procedureColumnList;
    }

    public void addProcedureColumn(DfProcedureColumnMeta procedureColumn) {
        _procedureColumnList.add(procedureColumn);
    }

    public List<DfProcedureNotParamResultMeta> getNotParamResultList() {
        return _notParamResultList;
    }

    public void addNotParamResult(DfProcedureNotParamResultMeta notParamResult) {
        this._notParamResultList.add(notParamResult);
    }

    public DfProcedureSourceInfo getProcedureSourceInfo() {
        return _procedureSourceInfo;
    }

    public void setProcedureSourceInfo(DfProcedureSourceInfo procedureSourceInfo) {
        this._procedureSourceInfo = procedureSourceInfo;
    }
}
