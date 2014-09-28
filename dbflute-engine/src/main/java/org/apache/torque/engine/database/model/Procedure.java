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
package org.apache.torque.engine.database.model;

import java.math.BigDecimal;

import org.apache.torque.engine.database.transform.XmlToAppData.XmlReadingFilter;
import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.properties.DfBasicProperties;
import org.seasar.dbflute.properties.DfDocumentProperties;
import org.seasar.dbflute.properties.DfLittleAdjustmentProperties;
import org.seasar.dbflute.util.Srl;
import org.xml.sax.Attributes;

/**
 * @author awaawa (this class is new-created by DBFlute but located in Torque package to keep structure)
 * @author jflute
 * @since 0.9.9.7F (2012/08/20 Monday)
 */
public class Procedure {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected Database _database;
    protected String _procedureName;
    protected UnifiedSchema _unifiedSchema;
    protected BigDecimal _sourceLine;
    protected BigDecimal _sourceSize;
    protected String _sourceHash;
    protected String _procedureComment;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    // -----------------------------------------------------
    //                                         Load from XML
    //                                         -------------
    public boolean loadFromXML(Attributes attrib, XmlReadingFilter readingFilter) {
        _procedureName = attrib.getValue("name"); // procedure name
        _unifiedSchema = UnifiedSchema.createAsDynamicSchema(attrib.getValue("schema"));
        if (readingFilter != null && readingFilter.isProcedureExcept(_unifiedSchema, _procedureName)) {
            return false;
        }
        final String sourceLine = attrib.getValue("sourceLine");
        if (sourceLine != null) {
            try {
                _sourceLine = new BigDecimal(sourceLine);
            } catch (NumberFormatException ignored) { // just in case
            }
        }
        final String sourceSize = attrib.getValue("sourceSize");
        if (sourceSize != null) {
            try {
                _sourceSize = new BigDecimal(sourceSize);
            } catch (NumberFormatException ignored) { // just in case
            }
        }
        _sourceHash = attrib.getValue("sourceHash");
        _procedureComment = attrib.getValue("comment");
        return true;
    }

    // ===================================================================================
    //                                                                    Derived Property
    //                                                                    ================
    public String getProcedureUniqueName() {
        final DfBasicProperties prop = getBasicProperties();
        final String filteredName;
        if (prop.isDatabaseSQLServer()) {
            // SQLServer returns 'sp_foo;1'
            filteredName = Srl.substringLastFront(_procedureName, ";");
        } else {
            filteredName = _procedureName;
        }
        return _unifiedSchema.getCatalogSchema() + "." + filteredName;
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
        return _unifiedSchema + "." + _procedureName;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public Database getDatabase() {
        return _database;
    }

    public void setDatabase(Database database) {
        this._database = database;
    }

    public String getProcedureName() {
        return _procedureName;
    }

    public void setProcedureName(String procedureName) {
        this._procedureName = procedureName;
    }

    public UnifiedSchema getUnifiedSchema() {
        return _unifiedSchema;
    }

    public void setUnifiedSchema(UnifiedSchema _unifiedSchema) {
        this._unifiedSchema = _unifiedSchema;
    }

    public BigDecimal getSourceLine() {
        return _sourceLine;
    }

    public void setSourceLine(BigDecimal sourceLine) {
        this._sourceLine = sourceLine;
    }

    public BigDecimal getSourceSize() {
        return _sourceSize;
    }

    public void setSourceSize(BigDecimal sourceSize) {
        this._sourceSize = sourceSize;
    }

    public String getSourceHash() {
        return _sourceHash;
    }

    public void setSourceHash(String sourceHash) {
        this._sourceHash = sourceHash;
    }

    public String getProcedureComment() {
        return _procedureComment;
    }

    public void setProcedureComment(String procedureComment) {
        this._procedureComment = procedureComment;
    }
}
