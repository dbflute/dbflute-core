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
 *
 * And the following license definition is for Apache Torque.
 * DBFlute modified this source code and redistribute as same license 'Apache'.
 * /- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 *
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Turbine" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 * 
 * - - - - - - - - - -/
 */
package org.apache.torque.engine.database.model;

import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.properties.DfBasicProperties;
import org.seasar.dbflute.properties.DfDatabaseProperties;
import org.seasar.dbflute.properties.DfLittleAdjustmentProperties;
import org.seasar.dbflute.properties.assistant.DfAdditionalSchemaInfo;
import org.seasar.dbflute.resource.DBFluteSystem;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public class UnifiedSchema {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String NO_NAME_SCHEMA = "$$NoNameSchema$$"; // basically for MySQL

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _catalog;
    protected final String _schema;
    protected boolean _mainSchema;
    protected boolean _additionalSchema;
    protected boolean _unknownSchema;
    protected boolean _catalogAdditionalSchema;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    protected UnifiedSchema(String catalog, String schema) {
        if (isCompletelyUnsupportedDBMS()) {
            _catalog = null;
            _schema = null;
            return;
        }
        _catalog = filterAttribute(catalog);
        _schema = filterAttribute(schema);
    }

    protected UnifiedSchema(String schemaExpression) {
        if (isCompletelyUnsupportedDBMS()) {
            _catalog = null;
            _schema = null;
            return;
        }
        if (schemaExpression != null) {
            if (schemaExpression.contains(".")) {
                _catalog = filterAttribute(Srl.substringFirstFront(schemaExpression, "."));
                _schema = filterAttribute(Srl.substringFirstRear(schemaExpression, "."));
            } else {
                _catalog = null;
                _schema = filterAttribute(schemaExpression);
            }
        } else {
            _catalog = null;
            _schema = null;
        }
    }

    protected String filterAttribute(String element) {
        return Srl.is_NotNull_and_NotTrimmedEmpty(element) ? element.trim() : null;
    }

    protected boolean isCompletelyUnsupportedDBMS() {
        return getBasicProperties().isDatabaseAsUnifiedSchemaUnsupported();
    }

    // -----------------------------------------------------
    //                                               Creator
    //                                               -------
    public static UnifiedSchema createAsMainSchema(String catalog, String schema) {
        return new UnifiedSchema(catalog, schema).asMainSchema();
    }

    public static UnifiedSchema createAsAdditionalSchema(String catalog, String schema, boolean explicitCatalog) {
        final UnifiedSchema unifiedSchema = new UnifiedSchema(catalog, schema).asAdditionalSchema();
        if (explicitCatalog) {
            unifiedSchema.asCatalogAdditionalSchema();
        }
        return unifiedSchema;
    }

    public static UnifiedSchema createAsDynamicSchema(String catalog, String schema) {
        return new UnifiedSchema(catalog, schema).judgeSchema();
    }

    public static UnifiedSchema createAsDynamicSchema(String schemaExpression) {
        return new UnifiedSchema(schemaExpression).judgeSchema();
    }

    // -----------------------------------------------------
    //                                                Status
    //                                                ------
    protected UnifiedSchema asMainSchema() {
        _mainSchema = true;
        return this;
    }

    protected UnifiedSchema asAdditionalSchema() {
        _additionalSchema = true;
        return this;
    }

    protected UnifiedSchema asCatalogAdditionalSchema() {
        _catalogAdditionalSchema = true;
        return this;
    }

    protected UnifiedSchema asUnknownSchema() {
        _unknownSchema = true;
        return this;
    }

    protected UnifiedSchema judgeSchema() {
        final DfDatabaseProperties databaseProp = getDatabaseProperties();
        final UnifiedSchema mainSchema = databaseProp.getDatabaseSchema();
        if (equals(mainSchema)) {
            asMainSchema();
        } else {
            final DfAdditionalSchemaInfo info = databaseProp.getAdditionalSchemaInfo(this);
            if (info != null) {
                asAdditionalSchema();
                if (info.getUnifiedSchema().isCatalogAdditionalSchema()) {
                    asCatalogAdditionalSchema();
                }
            } else {
                asUnknownSchema();
            }
        }
        return this;
    }

    // ===================================================================================
    //                                                                   Schema Expression
    //                                                                   =================
    public String getCatalogSchema() {
        final StringBuilder sb = new StringBuilder();
        if (Srl.is_NotNull_and_NotTrimmedEmpty(_catalog)) {
            sb.append(_catalog);
        }
        if (Srl.is_NotNull_and_NotTrimmedEmpty(_schema)) {
            if (!isNoNameSchema()) {
                if (sb.length() > 0) {
                    sb.append(".");
                }
                sb.append(_schema);
            }
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    public String getIdentifiedSchema() {
        final StringBuilder sb = new StringBuilder();
        if (Srl.is_NotNull_and_NotTrimmedEmpty(_catalog)) {
            sb.append(_catalog);
        }
        if (sb.length() > 0) {
            sb.append(".");
        }
        if (Srl.is_NotNull_and_NotTrimmedEmpty(_schema)) {
            sb.append(_schema);
        } else {
            sb.append(NO_NAME_SCHEMA);
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    public String getLoggingSchema() {
        return getCatalogSchema();
    }

    public String getPureCatalog() {
        return _catalog;
    }

    public String getPureSchema() {
        if (isNoNameSchema()) {
            return null;
        }
        return _schema;
    }

    protected String getSqlPrefixSchema() {
        final DfLittleAdjustmentProperties prop = getLittleAdjustmentProperties();
        if (prop.isAvailableAddingSchemaToTableSqlName()) {
            if (prop.isAvailableAddingCatalogToTableSqlName()) {
                return getCatalogSchema();
            } else {
                return getPureSchema();
            }
        }
        return getExecutableSchema();
    }

    public String getDrivenSchema() {
        final DfLittleAdjustmentProperties prop = getLittleAdjustmentProperties();
        if (prop.isAvailableSchemaDrivenTable()) {
            final String drivenSchema;
            if (isNoNameSchema()) { // e.g. MySQL
                drivenSchema = getCatalogSchema();
            } else { // e.g. PostgreSQL, Oracle
                drivenSchema = getPureSchema();
            }
            return drivenSchema;
        }
        // unsupported CatalogDrivenTable because it might be no-possible case
        return null;
    }

    public String getExecutableSchema() {
        if (_mainSchema) {
            return "";
        }
        if (_additionalSchema) {
            if (_catalogAdditionalSchema) {
                return getCatalogSchema();
            } else { // schema-only additional schema
                return getPureSchema();
            }
        }
        // basically no way but, for example,
        // SchemaSyncCheck's target schema comes here to extract auto-increment info
        return getCatalogSchema(); // as default
    }

    // ===================================================================================
    //                                                                 Unique Element Name
    //                                                                 ===================
    public String buildFullQualifiedName(String elementName) {
        return Srl.connectPrefix(elementName, getCatalogSchema(), ".");
    }

    public String buildSchemaQualifiedName(String elementName) {
        return Srl.connectPrefix(elementName, getPureSchema(), ".");
    }

    public String buildIdentifiedName(String elementName) {
        return Srl.connectPrefix(elementName, getIdentifiedSchema(), ".");
    }

    public String buildSqlName(String elementName) {
        final String sqlPrefixSchema = getSqlPrefixSchema();
        return Srl.connectPrefix(elementName, sqlPrefixSchema, ".");
    }

    // ===================================================================================
    //                                                                       Determination
    //                                                                       =============
    public boolean isMainSchema() {
        return _mainSchema;
    }

    public boolean isAdditionalSchema() {
        return _additionalSchema;
    }

    public boolean isUnknownSchema() {
        return _unknownSchema;
    }

    public boolean isCatalogAdditionalSchema() {
        return isAdditionalSchema() && _catalogAdditionalSchema;
    }

    public boolean hasSchema() {
        return Srl.is_NotNull_and_NotTrimmedEmpty(getCatalogSchema());
    }

    public boolean existsPureCatalog() {
        return Srl.is_NotNull_and_NotTrimmedEmpty(getPureCatalog());
    }

    public boolean existsPureSchema() {
        return Srl.is_NotNull_and_NotTrimmedEmpty(getPureSchema());
    }

    protected boolean isNoNameSchema() {
        return Srl.is_NotNull_and_NotTrimmedEmpty(_schema) && NO_NAME_SCHEMA.equalsIgnoreCase(_schema);
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof UnifiedSchema)) {
            return false;
        }
        final String mySchema = getIdentifiedSchema();
        final String yourSchema = ((UnifiedSchema) obj).getIdentifiedSchema();
        if (mySchema == null && yourSchema == null) {
            return true;
        }
        return mySchema != null && mySchema.equalsIgnoreCase(yourSchema);
    }

    @Override
    public int hashCode() {
        final String identifiedSchema = getIdentifiedSchema();
        return identifiedSchema != null ? identifiedSchema.hashCode() : 17;
    }

    @Override
    public String toString() {
        return "{" + getIdentifiedSchema() + " as " + (isMainSchema() ? "main" : "")
                + (isAdditionalSchema() ? "additional" : "") + (isCatalogAdditionalSchema() ? "(catalog)" : "")
                + (isUnknownSchema() ? "unknown" : "") + "}";
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

    protected DfDatabaseProperties getDatabaseProperties() {
        return getProperties().getDatabaseProperties();
    }

    protected DfLittleAdjustmentProperties getLittleAdjustmentProperties() {
        return getProperties().getLittleAdjustmentProperties();
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String ln() {
        return DBFluteSystem.getBasicLn();
    }
}
