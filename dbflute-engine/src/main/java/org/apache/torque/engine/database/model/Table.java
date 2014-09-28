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

/* ====================================================================
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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
 */

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.engine.EngineException;
import org.apache.torque.engine.database.transform.XmlToAppData.XmlReadingFilter;
import org.seasar.dbflute.DBDef;
import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.bhv.ConditionBeanSetupper;
import org.seasar.dbflute.bhv.ReferrerConditionSetupper;
import org.seasar.dbflute.dbmeta.DerivedMappable;
import org.seasar.dbflute.helper.StringKeyMap;
import org.seasar.dbflute.helper.StringSet;
import org.seasar.dbflute.helper.jdbc.context.DfSchemaSource;
import org.seasar.dbflute.logic.doc.schemahtml.DfSchemaHtmlBuilder;
import org.seasar.dbflute.logic.generate.column.DfColumnListToStringBuilder;
import org.seasar.dbflute.logic.generate.language.DfLanguageDependency;
import org.seasar.dbflute.logic.generate.language.grammar.DfLanguageGrammar;
import org.seasar.dbflute.logic.generate.language.implstyle.DfLanguageImplStyle;
import org.seasar.dbflute.logic.sql2entity.analyzer.DfOutsideSqlFile;
import org.seasar.dbflute.logic.sql2entity.bqp.DfBehaviorQueryPathSetupper;
import org.seasar.dbflute.properties.DfBasicProperties;
import org.seasar.dbflute.properties.DfBehaviorFilterProperties;
import org.seasar.dbflute.properties.DfClassificationProperties;
import org.seasar.dbflute.properties.DfCommonColumnProperties;
import org.seasar.dbflute.properties.DfDatabaseProperties;
import org.seasar.dbflute.properties.DfDocumentProperties;
import org.seasar.dbflute.properties.DfIncludeQueryProperties;
import org.seasar.dbflute.properties.DfLittleAdjustmentProperties;
import org.seasar.dbflute.properties.DfLittleAdjustmentProperties.NonCompilableChecker;
import org.seasar.dbflute.properties.DfOutsideSqlProperties;
import org.seasar.dbflute.properties.DfSequenceIdentityProperties;
import org.seasar.dbflute.properties.DfSimpleDtoProperties;
import org.seasar.dbflute.properties.assistant.DfAdditionalSchemaInfo;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.Srl;
import org.xml.sax.Attributes;

/**
 * @author modified by jflute (originated in Apache Torque)
 */
public class Table {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log _log = LogFactory.getLog(Table.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                              Database
    //                                              --------
    protected Database _database;

    // -----------------------------------------------------
    //                                      Table Definition
    //                                      ----------------
    protected String _name;
    protected String _type;
    protected UnifiedSchema _unifiedSchema;
    protected String _plainComment;
    protected boolean _existsSameNameTable;
    protected boolean _existsSameSchemaSameNameTable;

    // -----------------------------------------------------
    //                                                Column
    //                                                ------
    protected final List<Column> _columnList = new ArrayList<Column>();
    protected final List<String> _columnNameList = new ArrayList<String>();
    protected final StringKeyMap<Column> _columnMap = StringKeyMap.createAsFlexible(); // only used as key-value

    // -----------------------------------------------------
    //                                           Foreign Key
    //                                           -----------
    // map style because of removing in the final initialization
    // and names of foreign key should be unique in a table
    protected final Map<String, ForeignKey> _foreignKeyMap = StringKeyMap.createAsFlexibleOrdered();

    // on the other hand, names of referrer are not
    // always unique because a referrer may be synonym
    // (fortunately, removing is not required about referrer)
    protected final List<ForeignKey> _referrerList = new ArrayList<ForeignKey>(5);

    protected final List<ForeignKey> _cannotBeReferrerList = new ArrayList<ForeignKey>(3);

    // -----------------------------------------------------
    //                                                Unique
    //                                                ------
    protected final List<Unique> _unices = new ArrayList<Unique>(5);

    // -----------------------------------------------------
    //                                                 Index
    //                                                 -----
    protected final List<Index> _indices = new ArrayList<Index>(5);

    // -----------------------------------------------------
    //                                       Java Definition
    //                                       ---------------
    protected String _javaName;

    // -----------------------------------------------------
    //                                 Sql2Entity Definition
    //                                 ---------------------
    protected boolean _sql2EntityCustomize;
    protected boolean _sql2EntityCustomizeHasNested;
    protected boolean _sql2EntityTypeSafeCursor;

    // basically not null if Sql2Entity
    // but you should check it just in case
    protected DfOutsideSqlFile _sql2EntitySqlFile;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * Default Constructor
     */
    public Table() {
    }

    // -----------------------------------------------------
    //                                         Load from XML
    //                                         -------------
    /**
     * Load the table object from an XML tag.
     * @param attrib XML attributes. (NotNull)
     * @param readingFilter The filter of object by name when reading XML. (NullAllowed)
     * @return Should be the table excepted?
     */
    public boolean loadFromXML(Attributes attrib, XmlReadingFilter readingFilter) {
        _name = attrib.getValue("name"); // table name
        _type = attrib.getValue("type"); // TABLE, VIEW, SYNONYM...
        _unifiedSchema = UnifiedSchema.createAsDynamicSchema(attrib.getValue("schema"));
        if (readingFilter != null && readingFilter.isTableExcept(_unifiedSchema, _name)) {
            return false;
        }
        _plainComment = attrib.getValue("comment");
        _javaName = attrib.getValue("javaName");
        return true;
    }

    // ===================================================================================
    //                                                                            Database
    //                                                                            ========
    /**
     * Get the parent of the table.
     * @return the parent database.
     */
    public Database getDatabase() {
        return _database;
    }

    /**
     * Set the parent of the table.
     * @param parent the parent database.
     */
    public void setDatabase(Database parent) {
        _database = parent;
    }

    // ===================================================================================
    //                                                                               Table
    //                                                                               =====
    // -----------------------------------------------------
    //                                            Table Name
    //                                            ----------
    /**
     * Get the pure name of the table, no prefix even if schema-driven. <br />
     * You cannot use for identity, instead you should use {@link #getTableDbName()}. <br />
     * Basically no referred from velocity templates because of no necessary.
     * @return The table name as String. (NotNull)
     */
    public String getName() { // Torque-traditional method
        return _name;
    }

    /**
     * Set the pure name of the table, no prefix even if schema-driven.
     * @param name The table name as String. (NotNull)
     */
    public void setName(String name) {
        this._name = name;
    }

    /**
     * Get the DB name of the table, resolved schema-driven or table-driven. <br />
     * You can use this for e.g. matching with specified table in properties, and use for simple display. <br />
     * And you can use as identity for e.g. map key.
     * @return The table name as String, might contains dot '.'. (NotNull)
     */
    public String getTableDbName() { // new face for DBFlute
        final String pureName = getName();
        if (_unifiedSchema == null) {
            return pureName;
        }
        final String schemaPrefix = buildSchemaPrefixForIdentity();
        if (schemaPrefix == null || schemaPrefix.trim().length() == 0) {
            return pureName;
        }
        final StringBuilder sb = new StringBuilder();
        sb.append(schemaPrefix).append(".").append(pureName);
        return sb.toString();
    }

    protected String buildSchemaPrefixForIdentity() {
        if (_unifiedSchema == null) {
            return "";
        }
        if (existsSameNameTable()) { // might be true after adding table in database
            final String fixedPrefix;
            if (existsSameSchemaSameNameTable()) {
                // e.g. EXAMPLEDB.PUBLIC.MEMBER and NEXTEXAMPLEDB.PUBLIC.MEMBER
                // schema-driven but exists same-schema same-name table
                fixedPrefix = _unifiedSchema.getCatalogSchema(); // forcedly catalog schema
            } else {
                // e.g. PUBLIC.MEMBER and NEXTSCHEMA.MEMBER
                fixedPrefix = _unifiedSchema.getExecutableSchema();
            }
            return fixedPrefix;
        }
        final String drivenSchema = _unifiedSchema.getDrivenSchema();
        if (drivenSchema != null) { // e.g. PUBLIC.MEMBER, EXAMPLEDB.MEMBER
            return drivenSchema;
        }
        return "";
    }

    public boolean existsSameNameTable() {
        return _existsSameNameTable;
    }

    public void markSameNameTableExists() {
        _existsSameNameTable = true;
    }

    public boolean existsSameSchemaSameNameTable() {
        return _existsSameSchemaSameNameTable;
    }

    public void markSameSchemaSameNameTableExists() {
        markSameNameTableExists();
        _existsSameSchemaSameNameTable = true;
    }

    // -----------------------------------------------------
    //                                              SQL Name
    //                                              --------
    /**
     * Get the SQL name of the table, which is used in your SQL after generated world. (for templates) <br />
     * This might be quoted with fitting to template or has its schema prefix.
     * @return The table name as String. (NotNull)
     */
    public String getTableSqlName() { // new face for DBFlute
        final String tableName = quoteTableNameIfNeeds(getResourceNameForSqlName());
        return filterSchemaSqlPrefix(tableName);
    }

    /**
     * Get the SQL name of the table, which is used in your SQL on the Java process. (for direct use) <br />
     * This might be quoted for direct use or has its schema prefix.
     * @return The table name as String. (NotNull)
     */
    public String getTableSqlNameDirectUse() { // new face for DBFlute
        final String tableName = quoteTableNameIfNeedsDirectUse(getResourceNameForSqlName());
        return filterSchemaSqlPrefix(tableName);
    }

    protected String getResourceNameForSqlName() {
        final String pureName = getName();
        return isSqlNameUpperCase() ? pureName.toUpperCase() : pureName;
    }

    protected boolean isSqlNameUpperCase() {
        if (isSql2EntityCustomize()) { // Sql2Entity is on the camel case basis
            return false;
        }
        return getProperties().getLittleAdjustmentProperties().isTableSqlNameUpperCase();
    }

    protected String filterSchemaSqlPrefix(String tableName) {
        if (hasSchema()) {
            return _unifiedSchema.buildSqlName(tableName);
        }
        return tableName;
    }

    protected String quoteTableNameIfNeeds(String tableName) {
        final DfLittleAdjustmentProperties prop = getProperties().getLittleAdjustmentProperties();
        return prop.quoteTableNameIfNeeds(tableName);
    }

    protected String quoteTableNameIfNeedsDirectUse(String tableName) {
        final DfLittleAdjustmentProperties prop = getProperties().getLittleAdjustmentProperties();
        return prop.quoteTableNameIfNeedsDirectUse(tableName);
    }

    // -----------------------------------------------------
    //                                               HTML ID
    //                                               -------
    /**
     * Get the value for HTML (SchemaHTML) ID attribute of the table.
     * @return The table ID for SchemaHTML. (NotNull)
     */
    public String getTableIdForSchemaHtml() {
        return Srl.replace(getTableDbName().toLowerCase(), ".", "_");
    }

    // -----------------------------------------------------
    //                                           Custom Name
    //                                           -----------
    /**
     * Get the table name for annotation. (for S2Dao, DBFlute.NET only)
     * @return The table name for annotation. (NotNull)
     */
    public String getAnnotationTableName() {
        return getTableSqlName();
    }

    // -----------------------------------------------------
    //                                            Alias Name
    //                                            ----------
    public boolean hasAlias() {
        final String alias = getAlias();
        return alias != null && alias.trim().length() > 0;
    }

    /**
     * Get the alias of the table.
     * @return The table alias as String. (NotNull, EmptyAllowed: when no alias)
     */
    public String getAlias() {
        final DfDocumentProperties prop = getProperties().getDocumentProperties();
        final String comment = _plainComment;
        if (comment != null) {
            final String alias = prop.extractAliasFromDbComment(comment);
            if (alias != null) {
                return alias;
            }
        }
        return "";
    }

    public String getAliasExpression() { // for expression '(alias)name'
        final String alias = getAlias();
        if (alias == null || alias.trim().length() == 0) {
            return "";
        }
        return "(" + alias + ")";
    }

    // -----------------------------------------------------
    //                                            Table Type
    //                                            ----------
    /**
     * Get the type of the Table
     */
    public String getType() {
        return _type;
    }

    /**
     * Set the type of the Table
     */
    public void setType(String type) {
        this._type = type;
    }

    public boolean isTypeTable() {
        return _type != null && _type.equalsIgnoreCase("table");
    }

    public boolean isTypeView() {
        return _type != null && _type.equalsIgnoreCase("view");
    }

    // -----------------------------------------------------
    //                                          Table Schema
    //                                          ------------
    public UnifiedSchema getUnifiedSchema() {
        return _unifiedSchema;
    }

    public void setUnifiedSchema(UnifiedSchema unifiedSchema) { // basically for Sql2Entity
        _unifiedSchema = unifiedSchema;
    }

    public String getDocumentSchema() {
        if (_unifiedSchema == null) {
            return "";
        }
        if (getDatabase().hasCatalogAdditionalSchema()) {
            return _unifiedSchema.getCatalogSchema();
        } else {
            return _unifiedSchema.getPureSchema();
        }
    }

    protected String getPureCatalog() { // NOT contain catalog name
        return _unifiedSchema != null ? _unifiedSchema.getPureSchema() : null;
    }

    protected String getPureSchema() { // NOT contain catalog name
        return _unifiedSchema != null ? _unifiedSchema.getPureSchema() : null;
    }

    public boolean hasSchema() {
        return _unifiedSchema != null ? _unifiedSchema.hasSchema() : false;
    }

    public boolean isMainSchema() {
        return hasSchema() && getUnifiedSchema().isMainSchema();
    }

    public boolean isAdditionalSchema() {
        return hasSchema() && getUnifiedSchema().isAdditionalSchema();
    }

    public boolean isCatalogAdditionalSchema() {
        return hasSchema() && getUnifiedSchema().isCatalogAdditionalSchema();
    }

    // -----------------------------------------------------
    //                                         Table Comment
    //                                         -------------
    public String getPlainComment() { // may contain its alias name
        return _plainComment;
    }

    public boolean hasComment() { // means resolved comment (not plain)
        final String comment = getComment();
        return comment != null && comment.trim().length() > 0;
    }

    public String getComment() {
        final DfDocumentProperties prop = getProperties().getDocumentProperties();
        final String comment = prop.extractCommentFromDbComment(_plainComment);
        return comment != null ? comment : "";
    }

    public String getCommentForSchemaHtml() {
        final DfDocumentProperties prop = getProperties().getDocumentProperties();
        String comment = prop.resolveTextForSchemaHtml(getComment());
        return comment != null ? comment : "";
    }

    public boolean isCommentForJavaDocValid() {
        final DfDocumentProperties prop = getProperties().getDocumentProperties();
        return hasComment() && prop.isEntityJavaDocDbCommentValid();
    }

    public String getCommentForJavaDoc() {
        final DfDocumentProperties prop = getProperties().getDocumentProperties();
        final String comment = prop.resolveTextForJavaDoc(getComment(), "");
        return comment != null ? comment : "";
    }

    public boolean isCommentForDBMetaValid() {
        final DfDocumentProperties prop = getProperties().getDocumentProperties();
        return hasComment() && prop.isEntityDBMetaDbCommentValid();
    }

    public String getCommentForDBMeta() {
        final DfDocumentProperties prop = getProperties().getDocumentProperties();
        final String comment = prop.resolveTextForDBMeta(getComment());
        return comment != null ? comment : "";
    }

    // -----------------------------------------------------
    //                                               Display
    //                                               -------
    public String getTableDispName() {
        if (isSql2EntityCustomize()) { // Sql2Entity is on the camel case basis
            return getTableDbName();
        }
        return filterTableDispNameIfNeeds(getTableDbName());
    }

    protected String filterTableDispNameIfNeeds(String tableName) {
        final DfLittleAdjustmentProperties prop = getProperties().getLittleAdjustmentProperties();
        return prop.filterTableDispNameIfNeeds(tableName);
    }

    public String getBasicInfoDispString() {
        final String type = getType();
        return getAliasExpression() + getTableDispName() + (type != null ? " as " + type : "");
    }

    public String getTitleForSchemaHtml() {
        final StringBuilder sb = new StringBuilder();
        sb.append("type=").append(_type);
        if (isAdditionalSchema()) {
            sb.append(", schema=").append(getDocumentSchema());
        }
        sb.append(", primaryKey={").append(getPrimaryKeyNameCommaString()).append("}");
        sb.append(", nameLength=").append(getTableDbName().length());
        sb.append(", columnCount=").append(getColumns().length);
        final DfDocumentProperties prop = getProperties().getDocumentProperties();
        return " title=\"" + prop.resolveAttributeForSchemaHtml(sb.toString()) + "\"";
    }

    // ===================================================================================
    //                                                                              Column
    //                                                                              ======
    // -----------------------------------------------------
    //                                                 Basic
    //                                                 -----
    /**
     * A utility function to create a new column from attrib and add it to this table.
     * @param attrib xml attributes for the column to add
     * @param readingFilter The filter of column. (NullAllowed)
     * @return the added column (NullAllowed: if null, means the column was filtered)
     */
    public Column addColumn(Attributes attrib, XmlReadingFilter readingFilter) {
        Column col = new Column();
        col.setTable(this);
        if (!col.loadFromXML(attrib, readingFilter)) {
            return null;
        }
        addColumn(col);
        return col;
    }

    /**
     * Adds a new column to the column list and set the
     * parent table of the column to the current table
     * @param col the column to add
     */
    public void addColumn(Column col) {
        col.setTable(this);
        _columnList.add(col);
        _columnNameList.add(col.getName());
        _columnMap.put(col.getName(), col);
        final String synonym = col.getSynonym();
        if (synonym != null) {
            _columnMap.put(synonym, col); // to find by synonym name
        }
    }

    /**
     * Returns a List containing all the columns in the table
     * @return the list of column. (NotNull)
     */
    public List<Column> getColumnList() {
        return _columnList;
    }

    /**
     * Returns a List containing all the names of column in the table
     * @return the list of column name. (NotNull)
     */
    public List<String> getColumnNameList() {
        return _columnNameList;
    }

    /**
     * Returns an Array containing all the columns in the table
     * @return the array of column. (NotNull)
     */
    public Column[] getColumns() {
        return _columnList.toArray(new Column[] {});
    }

    // -----------------------------------------------------
    //                                               Arrange
    //                                               -------
    /**
     * Returns an Array containing all the columns in the table
     */
    public String getColumnNameCommaString() {
        final StringBuilder sb = new StringBuilder();
        for (String columnName : _columnNameList) {
            sb.append(", ").append(columnName);
        }
        sb.delete(0, ", ".length());
        return sb.toString();
    }

    public String getPropertyNameCommaString() {
        final StringBuilder sb = new StringBuilder();
        for (Column column : _columnList) {
            sb.append(", ").append(column.getJavaBeansRulePropertyName());
        }
        sb.delete(0, ", ".length());
        return sb.toString();
    }

    /**
     * Returns a specified column.
     * @param name name of the column
     * @return Return a Column object or null if it does not exist.
     */
    public Column getColumn(String name) {
        return _columnMap.get(name);
    }

    public Integer getColumnIndex(String name) {
        final Column column = getColumn(name);
        if (column == null) {
            return null;
        }
        return getColumnNameList().indexOf(column.getName());
    }

    // -----------------------------------------------------
    //                                         Determination
    //                                         -------------
    public boolean containsColumn(List<String> columnNameList) {
        if (columnNameList.isEmpty()) {
            return false;
        }
        for (String columnName : columnNameList) {
            if (getColumn(columnName) == null) {
                return false;
            }
        }
        return true;
    }

    public boolean hasUtilDateColumn() {
        for (Column column : _columnList) {
            if (column.isJavaNativeUtilDate()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasByteArrayColumn() {
        for (Column column : _columnList) {
            if (column.isJavaNativeByteArray()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasByteArrayColumnInEqualsHashcode() {
        for (Column column : getEqualsHashcodeColumnList()) {
            if (column.isJavaNativeByteArray()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasDefaultValueExceptAutoIncrement() {
        for (Column column : getColumnList()) {
            if (column.hasDefaultValueExceptAutoIncrement()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasColumnComment() { // means resolved comment (not plain)
        for (Column column : getColumnList()) {
            if (column.hasComment()) {
                return true;
            }
        }
        return false;
    }

    // ===================================================================================
    //                                                                         Primary Key
    //                                                                         ===========
    public String getPrimaryKeyConstraintName() {
        final List<Column> columnList = getColumnList();
        for (Column column : columnList) {
            if (column.isPrimaryKey()) {
                return column.getPrimaryKeyName();
            }
        }
        return null;
    }

    /**
     * Returns the collection of Columns which make up the single primary
     * key for this table.
     * @return A list of the primary key parts.
     */
    public List<Column> getPrimaryKey() {
        final List<Column> pk = new ArrayList<Column>(_columnList.size());
        for (Column column : _columnList) {
            if (column.isPrimaryKey()) {
                pk.add(column);
            }
        }
        return pk;
    }

    public Column getPrimaryKeyAsOne() {
        if (getPrimaryKey().size() != 1) {
            String msg = "This method is for only-one primary-key:";
            msg = msg + " getPrimaryKey().size()=" + getPrimaryKey().size() + " table=" + getTableDbName();
            throw new IllegalStateException(msg);
        }
        return getPrimaryKey().get(0);
    }

    public String getPrimaryKeyNameAsOne() {
        return getPrimaryKeyAsOne().getName();
    }

    public String getPrimaryKeyJavaNameAsOne() {
        return getPrimaryKeyAsOne().getJavaName();
    }

    public String getPrimaryKeyJavaNativeAsOne() {
        return getPrimaryKeyAsOne().getJavaNative();
    }

    public String getPrimaryKeyColumnDbNameOnlyFirstOne() {
        if (hasPrimaryKey()) {
            return getPrimaryKey().get(0).getName();
        } else {
            return "";
        }
    }

    public List<Column> getEqualsHashcodeColumnList() {
        if (hasPrimaryKey()) {
            return getPrimaryKey();
        } else {
            return getColumnList();
        }
    }

    // -----------------------------------------------------
    //                                      Arguments String
    //                                      ----------------
    /**
     * Returns primaryKeyArgsString. [BigDecimal rcvlcqNo, String sprlptTp]
     * @return The value of primaryKeyArgsString. (NotNull)
     */
    public String getPrimaryKeyArgsString() {
        final DfLanguageDependency lang = getBasicProperties().getLanguageDependency();
        return DfColumnListToStringBuilder.getColumnArgsString(getPrimaryKey(), lang.getLanguageGrammar());
    }

    /**
     * Returns primaryKeyArgsJavaDocString. [AtMarkparam rcvlcqNo The one of primary key. (NotNull)...]
     * @return The value of primaryKeyArgsJavaDocString. (NotNull)
     */
    public String getPrimaryKeyArgsJavaDocString() {
        final String ln = getBasicProperties().getSourceCodeLineSeparator();
        return DfColumnListToStringBuilder.getColumnArgsJavaDocString(getPrimaryKey(), "primary key", ln);
    }

    /**
     * Returns primaryKeyArgsAssertString. [assertObjectNotNull("rcvlcqNo", rcvlcqNo); assert...;]
     * @return The value of primaryKeyArgsAssertString. (NotNull)
     */
    public String getPrimaryKeyArgsAssertString() {
        return DfColumnListToStringBuilder.getColumnArgsAssertString(getPrimaryKey());
    }

    /**
     * Returns primaryKeyArgsAssertStringCSharp. [AssertObjectNotNull("rcvlcqNo", rcvlcqNo); assert...;]
     * @return The value of primaryKeyArgsAssertStringCSharp. (NotNull)
     */
    public String getPrimaryKeyArgsAssertStringCSharp() {
        return DfColumnListToStringBuilder.getColumnArgsAssertStringCSharp(getPrimaryKey());
    }

    /**
     * Returns primaryKeyArgsSetupString. [setRcvlcqNo(rcvlcqNo);setSprlptTp(sprlptTp);]
     * @return The value of primaryKeyArgsSetupString. (NotNull)
     */
    public String getPrimaryKeyArgsSetupString() {
        return DfColumnListToStringBuilder.getColumnArgsSetupString(null, getPrimaryKey());
    }

    /**
     * Returns primaryKeyArgsSetupString. [beanName.setRcvlcqNo(rcvlcqNo);beanName.setSprlptTp(sprlptTp);]
     * @param beanName The name of bean. (NullAllowed)
     * @return The value of primaryKeyArgsSetupString. (NotNull)
     */
    public String getPrimaryKeyArgsSetupString(String beanName) {
        return DfColumnListToStringBuilder.getColumnArgsSetupString(beanName, getPrimaryKey());
    }

    /**
     * Returns primaryKeyArgsSetupStringCSharp. [beanName.RcvlcqNo = rcvlcqNo;beanName.SprlptTp = sprlptTp;]
     * @return The value of primaryKeyArgsSetupStringCSharp. (NotNull)
     */
    public String getPrimaryKeyArgsSetupStringCSharp() {
        return DfColumnListToStringBuilder.getColumnArgsSetupStringCSharp(null, getPrimaryKey());
    }

    /**
     * Returns primaryKeyArgsSetupStringCSharp. [beanName.RcvlcqNo = rcvlcqNo;beanName.SprlptTp = sprlptTp;]
     * @param beanName The name of bean. (NullAllowed)
     * @return The value of primaryKeyArgsSetupStringCSharp. (NotNull)
     */
    public String getPrimaryKeyArgsSetupStringCSharp(String beanName) {
        return DfColumnListToStringBuilder.getColumnArgsSetupStringCSharp(beanName, getPrimaryKey());
    }

    /**
     * Returns primaryKeyArgsConditionSetupString. [cb.query().setRcvlcqNo_Equal(rcvlcqNo);cb.query()...;]
     * @return The value of primaryKeyArgsConditionSetupString. (NotNull)
     */
    public String getPrimaryKeyArgsConditionSetupString() {
        return DfColumnListToStringBuilder.getColumnArgsConditionSetupString(getPrimaryKey());
    }

    /**
     * Returns primaryKeyArgsConditionSetupStringCSharp. [cb.Query().SetRcvlcqNo_Equal(rcvlcqNo);cb.Query()...;]
     * @return The value of primaryKeyArgsConditionSetupStringCSharp. (NotNull)
     */
    public String getPrimaryKeyArgsConditionSetupStringCSharp() { // for compatible
        return DfColumnListToStringBuilder.getColumnArgsConditionSetupString(getPrimaryKey());
    }

    /**
     * Returns primaryKeyArgsCallingString. [rcvlcqNo, sprlptTp]
     * @return The value of primaryKeyArgsCallingString. (NotNull)
     */
    public String getPrimaryKeyArgsCallingString() {
        return getPrimaryKeyUncapitalisedJavaNameCommaString();
    }

    // -----------------------------------------------------
    //                                       Order-By String
    //                                       ---------------
    /**
     * Returns primaryKeyOrderByAscString. [RCVLCQ_NO asc, SPRLPT_TP asc]
     * @return Generated string.
     */
    public String getPrimaryKeyOrderByAscString() {
        return DfColumnListToStringBuilder.getColumnOrderByString(getPrimaryKey(), "asc");
    }

    /**
     * Returns primaryKeyOrderByDescString. [RCVLCQ_NO asc, SPRLPT_TP asc]
     * @return Generated string.
     */
    public String getPrimaryKeyOrderByDescString() {
        return DfColumnListToStringBuilder.getColumnOrderByString(getPrimaryKey(), "desc");
    }

    // -----------------------------------------------------
    //                                        Display String
    //                                        --------------
    /**
     * Returns primaryKeyDispValueString. [value-value-value...]
     * @return Generated string.
     */
    public String getPrimaryKeyDispValueString() {
        return DfColumnListToStringBuilder.getColumnDispValueString(getPrimaryKey(), "get");
    }

    /**
     * Returns primaryKeyDispValueString. [value-value-value...]
     * @return Generated string.
     */
    public String getPrimaryKeyDispValueStringByGetterInitCap() {
        return DfColumnListToStringBuilder.getColumnDispValueString(getPrimaryKey(), "Get");
    }

    // -----------------------------------------------------
    //                                    Basic Comma String
    //                                    ------------------
    /**
     * Returns primaryKeyNameCommaString. [RCVLCQ_NO, SPRLPT_TP]
     * @return Generated string.
     */
    public String getPrimaryKeyNameCommaString() {
        return DfColumnListToStringBuilder.getColumnNameCommaString(getPrimaryKey());
    }

    /**
     * Returns primaryKeyUncapitalisedJavaNameCommaString. [rcvlcqNo, sprlptTp]
     * @return Generated string.
     */
    public String getPrimaryKeyUncapitalisedJavaNameCommaString() {
        return DfColumnListToStringBuilder.getColumnUncapitalisedJavaNameCommaString(getPrimaryKey());
    }

    /**
     * Returns primaryKeyJavaNameCommaString. [RcvlcqNo, SprlptTp]
     * @return Generated string.
     */
    public String getPrimaryKeyJavaNameCommaString() {
        return DfColumnListToStringBuilder.getColumnJavaNameCommaString(getPrimaryKey());
    }

    /**
     * Returns primaryKeyGetterCommaString. [getRcvlcqNo(), getSprlptTp()]
     * @return Generated string.
     */
    public String getPrimaryKeyGetterCommaString() {
        return DfColumnListToStringBuilder.getColumnGetterCommaString(getPrimaryKey());
    }

    // -----------------------------------------------------
    //                                         Determination
    //                                         -------------
    /**
     * Determine whether this table has a primary key.
     * @return The determination, true or false.
     */
    public boolean hasPrimaryKey() {
        return (getPrimaryKey().size() > 0);
    }

    /**
     * Determine whether this table has a single primary key.
     * @return The determination, true or false.
     */
    public boolean hasSinglePrimaryKey() {
        return (getPrimaryKey().size() == 1);
    }

    /**
     * Determine whether this table has a compound primary key.
     * @return The determination, true or false.
     */
    public boolean hasCompoundPrimaryKey() {
        return (getPrimaryKey().size() > 1);
    }

    /**
     * Returns all parts of the primary key, separated by commas.
     * @return A CSV list of primary key parts.
     */
    public String printPrimaryKey() {
        return printList(_columnList);
    }

    /**
     * Is this table writable?
     * @return The determination, true or false.
     */
    public boolean isWritable() {
        return hasPrimaryKey();
    }

    /**
     * Returns AttachedPKArgsSetupString. [setRcvlcqNo(pk.rcvlcqNo);setSprlptTp(pk.sprlptTp);]
     * @param attachedPKVariableName
     * @return Generated string.
     */
    public String getAttachedPKArgsSetupString(String attachedPKVariableName) {
        final List<Column> pkList = this.getPrimaryKey();
        String result = "";
        for (Iterator<Column> ite = pkList.iterator(); ite.hasNext();) {
            Column pk = (Column) ite.next();
            String javaName = pk.getJavaName();
            String pkGetString = attachedPKVariableName + ".get" + javaName + "()";
            String setterString = "set" + javaName + "(" + pkGetString + ");";
            if ("".equals(result)) {
                result = setterString;
            } else {
                result = result + setterString;
            }
        }
        return result;
    }

    // ===================================================================================
    //                                                                         Foreign Key
    //                                                                         ===========
    // -----------------------------------------------------
    //                                                 Basic
    //                                                 -----
    /**
     * Returns a List containing all the FKs in the table
     * @return Foreign-key list.
     */
    public List<ForeignKey> getForeignKeyList() {
        return new ArrayList<ForeignKey>(_foreignKeyMap.values());
    }

    /**
     * Returns an Array containing all the FKs in the table
     * @return Foreign-key array.
     */
    public ForeignKey[] getForeignKeys() {
        return _foreignKeyMap.values().toArray(new ForeignKey[_foreignKeyMap.size()]);
    }

    public List<ForeignKey> getJoinableForeignKeyList() {
        final List<ForeignKey> foreignKeyList = getForeignKeyList();
        final List<ForeignKey> filteredList = new ArrayList<ForeignKey>();
        for (ForeignKey fk : foreignKeyList) {
            if (fk.isSuppressJoin()) {
                continue;
            }
            filteredList.add(fk);
        }
        return filteredList;
    }

    /**
     * Return the first foreign key that includes column in it's list
     * of local columns.  Eg. Foreign key (a,b,c) references table(x,y,z)
     * will be returned of column is either a,b or c.
     * @param columnName column name included in the key
     * @return Return a Column object or null if it does not exist.
     */
    public ForeignKey getForeignKey(String columnName) {
        ForeignKey firstFK = null;
        for (ForeignKey fk : _foreignKeyMap.values()) {
            final List<String> localColumns = fk.getLocalColumnNameList();
            if (Srl.containsElementIgnoreCase(localColumns, columnName)) {
                if (firstFK == null) {
                    firstFK = fk;
                }
            }
        }
        return firstFK;
    }

    public List<ForeignKey> getForeignKeyList(String columnName) {
        final List<ForeignKey> fkList = new ArrayList<ForeignKey>();
        for (ForeignKey fk : _foreignKeyMap.values()) {
            final List<String> localColumns = fk.getLocalColumnNameList();
            if (Srl.containsElementIgnoreCase(localColumns, columnName)) {
                fkList.add(fk);
            }
        }
        return fkList;
    }

    public ForeignKey getSelfReferenceForeignKey() { // returns first found
        final List<ForeignKey> foreignKeyList = getForeignKeyList();
        for (ForeignKey fk : foreignKeyList) {
            if (fk.isSelfReference()) {
                return fk;
            }
        }
        return null;
    }

    /**
     * A utility function to create a new foreign key
     * from attrib and add it to this table.
     * @param attrib the xml attributes
     * @return the created ForeignKey. (NotNull)
     */
    public ForeignKey addForeignKey(Attributes attrib) {
        final ForeignKey fk = new ForeignKey();
        fk.loadFromXML(this, attrib);
        addForeignKey(fk);
        return fk;
    }

    /**
     * Adds a new FK to the FK list and set the
     * parent table of the column to the current table
     * @param fk A foreign key
     */
    public void addForeignKey(ForeignKey fk) {
        fk.setTable(this);
        _foreignKeyMap.put(fk.getName(), fk);
    }

    /**
     * Remove the foreign key, for example, foreign table is excepted.
     * @param fk The removed foreign key. (NotNull)
     */
    public void removeForeignKey(ForeignKey fk) {
        _foreignKeyMap.remove(fk.getName());
    }

    // -----------------------------------------------------
    //                                               Arrange
    //                                               -------
    /**
     * Returns an comma string containing all the foreign table name. <br />
     * And contains one-to-one table.
     * @return Foreign table as comma string.
     */
    public String getForeignTableNameCommaString() {
        final StringBuilder sb = new StringBuilder();
        final Set<String> tableSet = new HashSet<String>();
        final List<ForeignKey> foreignKeyList = getForeignKeyList();
        for (int i = 0; i < foreignKeyList.size(); i++) {
            final ForeignKey fk = foreignKeyList.get(i);
            final String name = fk.getForeignTablePureName();
            if (tableSet.contains(name)) {
                continue;
            }
            tableSet.add(name);
            sb.append(", ").append(name);
            if (fk.hasFixedSuffix()) {
                sb.append("(").append(fk.getFixedSuffix()).append(")");
            }
        }
        for (ForeignKey referrer : _referrerList) {
            if (!referrer.isOneToOne()) {
                continue;
            }
            final String name = referrer.getTable().getTableDbName();
            if (tableSet.contains(name)) {
                continue;
            }
            tableSet.add(name);
            sb.append(", ").append(name).append("(AsOne)");
        }
        sb.delete(0, ", ".length());
        return sb.toString();
    }

    public String getForeignTableNameCommaStringWithHtmlHref() { // for SchemaHTML
        final StringBuilder sb = new StringBuilder();
        final DfDocumentProperties prop = getProperties().getDocumentProperties();
        final DfSchemaHtmlBuilder schemaHtmlBuilder = new DfSchemaHtmlBuilder(prop);
        final String delimiter = ", ";
        final List<ForeignKey> foreignKeyList = getForeignKeyList();
        final int size = foreignKeyList.size();
        if (size == 0) {
            return "&nbsp;";
        }
        for (int i = 0; i < size; i++) {
            final ForeignKey fk = foreignKeyList.get(i);
            final Table foreignTable = fk.getForeignTable();
            sb.append(schemaHtmlBuilder.buildRelatedTableLink(fk, foreignTable, delimiter));
        }
        sb.delete(0, delimiter.length());
        return sb.toString();
    }

    /**
     * Returns an comma string containing all the foreign property name.
     * @return Foreign property-name as comma string.
     */
    public String getForeignPropertyNameCommaString() {
        final StringBuilder sb = new StringBuilder();

        final List<ForeignKey> ls = getForeignKeyList();
        final int size = ls.size();
        for (int i = 0; i < size; i++) {
            final ForeignKey fk = ls.get(i);
            sb.append(", ").append(fk.getForeignPropertyName());
        }
        for (ForeignKey referrer : _referrerList) {
            if (referrer.isOneToOne()) {
                sb.append(", ").append(referrer.getReferrerPropertyNameAsOne());
            }
        }
        sb.delete(0, ", ".length());
        return sb.toString();
    }

    // -----------------------------------------------------
    //                                  Existing Foreign Key
    //                                  --------------------
    public boolean existsForeignKey(String foreignTableName, List<String> localColumnNameList,
            List<String> foreignColumnNameList) { // no suffix
        return doExistsForeignKey(foreignTableName, localColumnNameList, foreignColumnNameList, null, false);
    }

    public boolean existsForeignKey(String foreignTableName, List<String> localColumnNameList,
            List<String> foreignColumnNameList, String fixedSuffix) { // all
        return doExistsForeignKey(foreignTableName, localColumnNameList, foreignColumnNameList, fixedSuffix, true);
    }

    protected boolean doExistsForeignKey(String foreignTableName, List<String> localColumnNameList,
            List<String> foreignColumnNameList, String fixedSuffix, boolean compareSuffix) {
        final ForeignKey fk = doFindExistingForeignKey(foreignTableName, localColumnNameList, foreignColumnNameList,
                fixedSuffix, compareSuffix, true);
        return fk != null;
    }

    public ForeignKey findExistingForeignKey(String foreignTableName, List<String> localColumnNameList,
            List<String> foreignColumnNameList) { // no suffix
        return doFindExistingForeignKey(foreignTableName, localColumnNameList, foreignColumnNameList, null, false, true);
    }

    public ForeignKey findExistingForeignKey(String foreignTableName, List<String> foreignColumnNameList,
            String fixedSuffix) { // no local columns
        final List<String> emptyList = DfCollectionUtil.emptyList();
        return doFindExistingForeignKey(foreignTableName, emptyList, foreignColumnNameList, fixedSuffix, true, false);
    }

    public ForeignKey findExistingForeignKey(String foreignTableName, List<String> localColumnNameList,
            List<String> foreignColumnNameList, String fixedSuffix) { // all
        return doFindExistingForeignKey(foreignTableName, localColumnNameList, foreignColumnNameList, fixedSuffix,
                true, true);
    }

    protected ForeignKey doFindExistingForeignKey(String foreignTableName, List<String> localColumnNameList,
            List<String> foreignColumnNameList, String fixedSuffix, boolean compareSuffix, boolean compareLocalColumn) {
        final StringSet localColumnNameSet = StringSet.createAsFlexibleOrdered();
        localColumnNameSet.addAll(localColumnNameList);
        final StringSet foreignColumnNameSet = StringSet.createAsFlexibleOrdered();
        foreignColumnNameSet.addAll(foreignColumnNameList);

        for (ForeignKey fk : getForeignKeys()) {
            if (!Srl.equalsFlexible(foreignTableName, fk.getForeignTablePureName())) {
                continue;
            }
            if (compareSuffix && !Srl.equalsFlexible(fixedSuffix, fk.getFixedSuffix())) {
                continue;
            }
            final StringSet currentLocalColumnNameSet = StringSet.createAsFlexibleOrdered();
            currentLocalColumnNameSet.addAll(fk.getLocalColumnNameList());
            if (compareLocalColumn && !localColumnNameSet.equalsUnderCharOption(currentLocalColumnNameSet)) {
                continue;
            }
            final StringSet currentForeignColumnNameSet = StringSet.createAsFlexibleOrdered();
            currentForeignColumnNameSet.addAll(fk.getForeignColumnNameList());
            if (!foreignColumnNameSet.equalsUnderCharOption(currentForeignColumnNameSet)) {
                continue;
            }
            return fk; // first-found one
        }
        return null;
    }

    // -----------------------------------------------------
    //                                         Determination
    //                                         -------------
    public boolean hasForeignKey() {
        return (getForeignKeys().length != 0);
    }

    public boolean hasForeignKeyAsOne() {
        final List<ForeignKey> foreignKeyList = getForeignKeyList();
        for (ForeignKey fk : foreignKeyList) {
            if (fk.isOneToOne()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Has relation? (hasForeignKey() or hasReferrer())
     * @return The determination, true or false.
     */
    public boolean hasRelation() {
        return (hasForeignKey() || hasReferrer());
    }

    public boolean hasSelfReference() {
        final List<ForeignKey> foreignKeyList = getForeignKeyList();
        for (ForeignKey fk : foreignKeyList) {
            if (fk.isSelfReference()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasForeignTableContainsOne(Table foreignTable) {
        final List<ForeignKey> foreignKeyList = getForeignKeyList();
        for (ForeignKey foreignKey : foreignKeyList) {
            if (foreignKey.getForeignTableDbName().equals(foreignTable.getTableDbName())) {
                return true;
            }
        }
        final List<ForeignKey> referrerAsOneList = getReferrerAsOneList();
        for (ForeignKey referrer : referrerAsOneList) {
            if (referrer.getTable().getTableDbName().equals(foreignTable.getTableDbName())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasDynamicFixedConditionForeignKey() {
        List<ForeignKey> foreignKeyList = getForeignKeyList();
        for (ForeignKey foreignKey : foreignKeyList) {
            if (foreignKey.hasDynamicFixedCondition()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasOptionalRelation() {
        final List<ForeignKey> fkList = getForeignKeyList();
        for (ForeignKey fk : fkList) {
            if (fk.isForeignPropertyOptionalEntity()) {
                return true;
            }
        }
        final List<ForeignKey> referrerList = getReferrerAsOneList();
        for (ForeignKey referrer : referrerList) {
            if (referrer.isReferrerPropertyOptionalEntityAsOne()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasReverseOptionalRelation() {
        final List<ForeignKey> referrerList = getReferrerList();
        for (ForeignKey referrer : referrerList) {
            if (referrer.isForeignPropertyOptionalEntity()) {
                return true;
            }
        }
        return false;
    }

    // ===================================================================================
    //                                                                      Relation Index
    //                                                                      ==============
    protected Map<String, Integer> _relationIndexMap = new LinkedHashMap<String, Integer>();

    public int resolveForeignIndex(ForeignKey foreignKey) {
        return doResolveRelationIndex(foreignKey, false, false);// Ignore oneToOne
    }

    public int resolveReferrerIndexAsOne(ForeignKey foreignKey) {// oneToOne!
        return doResolveRelationIndex(foreignKey, true, true);
    }

    public int resolveRefererIndexAsOne(ForeignKey foreignKey) {// oneToOne!
        return resolveReferrerIndexAsOne(foreignKey);
    }

    public int resolveReferrerIndex(ForeignKey foreignKey) {
        return doResolveRelationIndex(foreignKey, true, false);
    }

    public int resolveRefererIndex(ForeignKey foreignKey) {
        return resolveReferrerIndex(foreignKey);
    }

    protected int doResolveRelationIndex(ForeignKey foreignKey, boolean referer, boolean oneToOne) {
        try {
            final String relationIndexKey = buildRefererIndexKey(foreignKey, referer, oneToOne);
            final Integer realIndex = _relationIndexMap.get(relationIndexKey);
            if (realIndex != null) {
                return realIndex;
            }
            final int minimumRelationIndex = extractMinimumRelationIndex(_relationIndexMap);
            _relationIndexMap.put(relationIndexKey, minimumRelationIndex);
            return minimumRelationIndex;
        } catch (RuntimeException e) {
            _log.warn("doResolveRelationIndex() threw the exception: " + foreignKey, e);
            throw e;
        }
    }

    protected String buildRefererIndexKey(ForeignKey foreignKey, boolean referer, boolean oneToOne) {
        if (!referer) {
            return foreignKey.getForeignJavaBeansRulePropertyName();
        } else {
            if (oneToOne) {
                return foreignKey.getReferrerJavaBeansRulePropertyNameAsOne();
            } else {
                return foreignKey.getReferrerJavaBeansRulePropertyName();
            }
        }
    }

    protected int extractMinimumRelationIndex(java.util.Map<String, Integer> relationIndexMap) {
        final Set<String> keySet = relationIndexMap.keySet();
        final List<Integer> indexList = new ArrayList<Integer>();
        for (String key : keySet) {
            final Integer index = relationIndexMap.get(key);
            indexList.add(index);
        }
        if (indexList.isEmpty()) {
            return 0;
        }
        Integer minimumIndex = -1;
        for (Integer currentIndex : indexList) {
            if (minimumIndex + 1 < currentIndex) {
                return minimumIndex + 1;
            }
            minimumIndex = currentIndex;
        }
        return indexList.size();
    }

    public boolean hasForeignKeyOrReferrer() {
        return hasForeignKey() || hasReferrer();
    }

    public boolean hasForeignKeyOrReferrerAsOne() {
        return hasForeignKey() || hasReferrerAsOne();
    }

    public boolean hasJoinableForeignKeyOrReferrerAsOne() {
        return !getJoinableForeignKeyList().isEmpty() || !getJoinableReferrerAsOneList().isEmpty();
    }

    public boolean hasJoinableRelationNestSelectSetupper() {
        final List<ForeignKey> foreignKeyList = getJoinableForeignKeyList();
        for (ForeignKey fk : foreignKeyList) {
            if (fk.hasForeignNestSelectSetupper()) {
                return true;
            }
        }
        final List<ForeignKey> referrerAsOneList = getJoinableReferrerAsOneList();
        for (ForeignKey referrer : referrerAsOneList) {
            if (referrer.hasReferrerNestSelectSetupper()) {
                return true;
            }
        }
        return false;
    }

    // ===================================================================================
    //                                                                            Referrer
    //                                                                            ========
    // -----------------------------------------------------
    //                                                 Basic
    //                                                 -----
    /**
     * Adds the foreign key from another table that refers to this table.
     * @param fk A foreign key referring to this table
     * @return Can the foreign key be referrer?
     */
    public boolean addReferrer(ForeignKey fk) {
        if (!fk.canBeReferrer()) {
            _cannotBeReferrerList.add(fk);
            return false;
        }
        _referrerList.add(fk);
        return true;
    }

    public List<ForeignKey> getReferrerList() {
        return _referrerList;
    }

    public List<ForeignKey> getReferrerAsManyList() {
        return getReferrerAsWhatList(false);
    }

    public List<ForeignKey> getReferrerAsOneList() {
        return getReferrerAsWhatList(true);
    }

    public List<ForeignKey> getJoinableReferrerAsOneList() {
        final List<ForeignKey> referrerList = getReferrerAsOneList();
        final List<ForeignKey> filteredList = new ArrayList<ForeignKey>();
        for (ForeignKey fk : referrerList) {
            if (fk.isSuppressJoin()) {
                continue;
            }
            filteredList.add(fk);
        }
        return filteredList;
    }

    protected List<ForeignKey> getReferrerAsWhatList(boolean oneToOne) {
        final List<ForeignKey> referrerList = getReferrerList();
        if (referrerList == null || referrerList.isEmpty()) {
            return referrerList;
        }
        List<ForeignKey> referrerListAsWhat = DfCollectionUtil.newArrayList();
        for (ForeignKey key : referrerList) {
            if (oneToOne) {
                if (key.isOneToOne()) {
                    referrerListAsWhat.add(key);
                }
            } else {
                if (!key.isOneToOne()) {
                    referrerListAsWhat.add(key);
                }
            }
        }
        return referrerListAsWhat;
    }

    public List<ForeignKey> getReferrerBothNonPKList() { // e.g. for SpecifyColumn's implicit
        final List<ForeignKey> targetReferrerList = new ArrayList<ForeignKey>(getRefererList());
        targetReferrerList.addAll(_cannotBeReferrerList); // contains cannotBeReferrer
        final List<ForeignKey> nonPkReferredReferrerList = new ArrayList<ForeignKey>();
        for (ForeignKey fk : targetReferrerList) {
            if (!fk.isLocalColumnPrimaryKey() && !fk.isForeignColumnPrimaryKey()) { // both non PK
                nonPkReferredReferrerList.add(fk);
            }
        }
        return nonPkReferredReferrerList;
    }

    public List<ForeignKey> getRefererList() { // for compatibility (spell miss)
        return getReferrerList();
    }

    public List<ForeignKey> getReferrers() { // for compatibility (old style)
        return getReferrerList();
    }

    public boolean hasReferrer() {
        return (getReferrerList() != null && !getReferrerList().isEmpty());
    }

    public boolean hasReferrerAsMany() {
        final List<ForeignKey> manyList = getReferrerAsManyList();
        return manyList != null && !manyList.isEmpty();
    }

    public boolean hasReferrerAsOne() {
        final List<ForeignKey> oneList = getReferrerAsOneList();
        return oneList != null && !oneList.isEmpty();
    }

    // -----------------------------------------------------
    //                                               Arrange
    //                                               -------
    protected List<ForeignKey> _singleKeyReferrers;

    public boolean hasSingleKeyReferrer() {
        return !getSingleKeyReferrers().isEmpty();
    }

    public List<ForeignKey> getSingleKeyReferrers() {
        if (_singleKeyReferrers != null) {
            return _singleKeyReferrers;
        }
        _singleKeyReferrers = new ArrayList<ForeignKey>(5);
        if (!hasReferrer()) {
            return _singleKeyReferrers;
        }
        final List<ForeignKey> referrerList = getReferrers();
        for (ForeignKey referrer : referrerList) {
            if (!referrer.isSimpleKeyFK()) {
                continue;
            }
            _singleKeyReferrers.add(referrer);
        }
        return _singleKeyReferrers;
    }

    protected List<ForeignKey> _compoundKeyReferrers;

    public boolean hasCompoundKeyReferrer() {
        return !getCompoundKeyReferrers().isEmpty();
    }

    public List<ForeignKey> getCompoundKeyReferrers() {
        if (_compoundKeyReferrers != null) {
            return _compoundKeyReferrers;
        }
        _compoundKeyReferrers = new ArrayList<ForeignKey>(5);
        if (!hasReferrer()) {
            return _compoundKeyReferrers;
        }
        final List<ForeignKey> referrerList = getReferrers();
        for (ForeignKey referrer : referrerList) {
            if (!referrer.isCompoundFK()) {
                continue;
            }
            _compoundKeyReferrers.add(referrer);
        }
        return _compoundKeyReferrers;
    }

    protected List<ForeignKey> _derivedReferrerReferrers;

    public List<ForeignKey> getDerivedReferrerReferrers() { // contains compound key
        if (_derivedReferrerReferrers != null) {
            return _derivedReferrerReferrers;
        }
        _derivedReferrerReferrers = new ArrayList<ForeignKey>(5);
        if (!hasReferrer()) {
            return _derivedReferrerReferrers;
        }
        for (ForeignKey referrer : getReferrers()) {
            if (!referrer.isDerivedReferrerSupported()) {
                continue;
            }
            _derivedReferrerReferrers.add(referrer);
        }
        return _derivedReferrerReferrers;
    }

    // unused, after all
    //protected List<ForeignKey> _stringOrIntegerReferrers;
    //
    //public boolean hasStringOrIntegerReferrer() {
    //    return !getStringOrIntegerReferrers().isEmpty();
    //}
    //
    //public List<ForeignKey> getStringOrIntegerReferrers() {
    //    if (_stringOrIntegerReferrers != null) {
    //        return _stringOrIntegerReferrers;
    //    }
    //    _stringOrIntegerReferrers = new ArrayList<ForeignKey>(5);
    //    if (!hasReferrer()) {
    //        return _stringOrIntegerReferrers;
    //    }
    //    prepareStringOrIntegerForeignKeyList(_stringOrIntegerReferrers, false);
    //    return _stringOrIntegerReferrers;
    //}

    protected List<ForeignKey> _singleKeyStringOrIntegerReferrers;

    public boolean hasSingleKeyStringOrIntegerReferrer() {
        return !getSingleKeyStringOrIntegerReferrers().isEmpty();
    }

    public List<ForeignKey> getSingleKeyStringOrIntegerReferrers() { // still used in CSharp's DerivedReferrer
        if (_singleKeyStringOrIntegerReferrers != null) {
            return _singleKeyStringOrIntegerReferrers;
        }
        _singleKeyStringOrIntegerReferrers = new ArrayList<ForeignKey>(5);
        if (!hasReferrer()) {
            return _singleKeyStringOrIntegerReferrers;
        }
        prepareStringOrIntegerForeignKeyList(_singleKeyStringOrIntegerReferrers, true);
        return _singleKeyStringOrIntegerReferrers;
    }

    protected void prepareStringOrIntegerForeignKeyList(List<ForeignKey> fkList, boolean simpleKey) {
        final List<ForeignKey> referrerList = getReferrers();
        fkloop: //
        for (ForeignKey referrer : referrerList) {
            if (simpleKey && !referrer.isSimpleKeyFK()) {
                continue;
            }
            final List<Column> localColumnList = referrer.getLocalColumnList();
            for (Column column : localColumnList) {
                if (!(column.isJavaNativeStringObject() || column.isJavaNativeNumberObject())) {
                    continue fkloop;
                }
            }
            fkList.add(referrer);
        }
    }

    public List<ForeignKey> getCompoundKeyExistsReferrerReferrers() {
        final List<ForeignKey> filteredList = new ArrayList<ForeignKey>();
        for (ForeignKey referrer : getCompoundKeyReferrers()) {
            if (!referrer.isExistsReferrerSupported()) {
                continue;
            }
            filteredList.add(referrer);
        }
        return filteredList;
    }

    public List<ForeignKey> getCompoundKeyDerivedReferrerReferrers() {
        final List<ForeignKey> filteredList = new ArrayList<ForeignKey>();
        for (ForeignKey referrer : getCompoundKeyReferrers()) {
            if (!referrer.isDerivedReferrerSupported()) {
                continue;
            }
            filteredList.add(referrer);
        }
        return filteredList;
    }

    // -----------------------------------------------------
    //                                          Comma String
    //                                          ------------
    public String getReferrerTableNameCommaString() {
        final StringBuilder sb = new StringBuilder();
        final Set<String> tableSet = new HashSet<String>();
        final List<ForeignKey> ls = getReferrerList();
        int size = ls.size();
        for (int i = 0; i < size; i++) {
            final ForeignKey fk = ls.get(i);
            if (fk.isOneToOne()) {
                continue;
            }
            final String name = fk.getTable().getTableDbName();
            if (tableSet.contains(name)) {
                continue;
            }
            tableSet.add(name);
            sb.append(", ").append(name);
        }
        for (int i = 0; i < size; i++) {
            final ForeignKey fk = ls.get(i);
            if (!fk.isOneToOne()) {
                continue;
            }
            final String name = fk.getTable().getTableDbName();
            if (tableSet.contains(name)) {
                continue;
            }
            tableSet.add(name);
            sb.append(", ").append(name);
        }
        sb.delete(0, ", ".length());
        return sb.toString();
    }

    public String getReferrerTableNameCommaStringWithHtmlHref() { // for SchemaHTML
        final StringBuilder sb = new StringBuilder();
        final DfDocumentProperties prop = getProperties().getDocumentProperties();
        final DfSchemaHtmlBuilder schemaHtmlBuilder = new DfSchemaHtmlBuilder(prop);
        final String delimiter = ", ";
        final List<ForeignKey> referrerList = getReferrerList();
        final int size = referrerList.size();
        if (size == 0) {
            return "&nbsp;";
        }
        for (int i = 0; i < size; i++) {
            final ForeignKey fk = referrerList.get(i);
            final Table referrerTable = fk.getTable();
            sb.append(schemaHtmlBuilder.buildRelatedTableLink(fk, referrerTable, delimiter));
        }
        sb.delete(0, delimiter.length());
        return sb.toString();
    }

    public String getReferrerPropertyNameCommaString() {
        final StringBuilder sb = new StringBuilder();
        final List<ForeignKey> ls = getReferrerList();
        int size = ls.size();
        for (int i = 0; i < size; i++) {
            final ForeignKey fk = ls.get(i);
            if (!fk.isOneToOne()) {
                sb.append(", ").append(fk.getReferrerPropertyName());
            }
        }
        sb.delete(0, ", ".length());
        return sb.toString();
    }

    // ===================================================================================
    //                                                                          Unique Key
    //                                                                          ==========
    /**
     * Returns an Array containing all the UKs in the table
     * @return An array containing all the UKs
     */
    public Unique[] getUnices() {
        final int size = _unices.size();
        final Unique[] tbls = new Unique[size];
        for (int i = 0; i < size; i++) {
            tbls[i] = (Unique) _unices.get(i);
        }
        return tbls;
    }

    public List<Unique> getUniqueList() {
        return _unices;
    }

    public boolean hasUnique() {
        return !_unices.isEmpty();
    }

    public List<Unique> getKeyableUniqueList() {
        final Integer limit = getLittleAdjustmentProperties().getKeyableUniqueColumnLimit();
        final List<Unique> uniqueList = new ArrayList<Unique>();
        final Set<String> uniqueNameSet = new HashSet<String>();
        for (Unique unique : _unices) {
            final List<Column> columnList = unique.getColumnList();
            if (columnList.isEmpty()) {
                continue;
            }
            if (limit >= 0 && columnList.size() > limit) {
                continue;
            }
            for (Column column : columnList) {
                if (column.isOptimisticLock()) {
                    continue; // just in case
                }
            }
            final String javaNameIdentity = unique.getConnectedJavaName();
            if (uniqueNameSet.contains(javaNameIdentity)) {
                continue;
            }
            uniqueNameSet.add(javaNameIdentity);
            uniqueList.add(unique);
        }
        return uniqueList;
    }

    public List<Unique> getOnlyOneColumnUniqueList() {
        final List<Unique> uniqueList = getUniqueList();
        final List<Unique> resultList = DfCollectionUtil.newArrayList();
        for (Unique unique : uniqueList) {
            if (unique.isOnlyOneColumn()) {
                resultList.add(unique);
            }
        }
        return resultList;
    }

    public List<Unique> getTwoOrMoreColumnUniqueList() {
        final List<Unique> uniqueList = getUniqueList();
        final List<Unique> resultList = DfCollectionUtil.newArrayList();
        for (Unique unique : uniqueList) {
            if (unique.isTwoOrMoreColumn()) {
                resultList.add(unique);
            }
        }
        return resultList;
    }

    /**
     * Adds a new Unique to the Unique list and set the
     * parent table of the column to the current table
     */
    public void addUnique(Unique unique) {
        unique.setTable(this);
        _unices.add(unique);
    }

    /**
     * A utility function to create a new Unique
     * from attrib and add it to this table.
     *
     * @param attrib the xml attributes
     */
    public Unique addUnique(Attributes attrib) {
        final Unique unique = new Unique();
        unique.loadFromXML(attrib);
        addUnique(unique);
        return unique;
    }

    public List<Column> getUniqueColumnList() {
        final List<Column> uniqueColumnList = new ArrayList<Column>();
        for (Column column : _columnList) {
            if (column.isUnique()) {
                uniqueColumnList.add(column);
            }
        }
        return uniqueColumnList;
    }

    protected List<Column> _singlePureUQColumnList;

    public boolean hasSingleUniqueUQColumn() { // e.g. for extract UQ column
        return !getSingleUniqueUQColumnList().isEmpty();
    }

    public List<Column> getSingleUniqueUQColumnList() { // e.g. for extract UQ column
        if (_singlePureUQColumnList != null) {
            return _singlePureUQColumnList;
        }
        final Map<String, Column> uqColMap = StringKeyMap.createAsFlexibleOrdered();
        final Unique[] unices = getUnices();
        for (Unique unique : unices) {
            if (!unique.isOnlyOneColumn()) {
                continue;
            }
            final Map<Integer, String> indexColumnMap = unique.getIndexColumnMap();
            final String columnName = indexColumnMap.values().iterator().next();
            final Column column = getColumn(columnName);
            if (column == null) { // basically no way but...
                // Oracle's materialized view has internal unique index
                // so this column variable can be null
                continue;
            }
            if (hasSinglePrimaryKey() && column.isPrimaryKey()) {
                continue;
            }
            uqColMap.put(column.getName(), column);
        }
        _singlePureUQColumnList = new ArrayList<Column>(uqColMap.values());
        return _singlePureUQColumnList;
    }

    // ===================================================================================
    //                                                                               Index
    //                                                                               =====
    /**
     * Returns an Array containing all the indices in the table
     * @return An array containing all the indices
     */
    public Index[] getIndices() {
        int size = _indices.size();
        Index[] tbls = new Index[size];
        for (int i = 0; i < size; i++) {
            tbls[i] = (Index) _indices.get(i);
        }
        return tbls;
    }

    public List<Index> getIndexList() {
        return _indices;
    }

    public List<Index> getOnlyOneColumnIndexList() {
        final List<Index> indexList = getIndexList();
        final List<Index> resultList = DfCollectionUtil.newArrayList();
        for (Index index : indexList) {
            if (index.isOnlyOneColumn()) {
                resultList.add(index);
            }
        }
        return resultList;

    }

    public List<Index> getTwoOrMoreColumnIndexList() {
        final List<Index> indexList = getIndexList();
        final List<Index> resultList = DfCollectionUtil.newArrayList();
        for (Index index : indexList) {
            if (index.isTwoOrMoreColumn()) {
                resultList.add(index);
            }
        }
        return resultList;
    }

    /**
     * Adds a new index to the index list and set the
     * parent table of the column to the current table
     */
    public void addIndex(Index index) {
        index.setTable(this);
        _indices.add(index);
    }

    /**
     * A utility function to create a new index
     * from attrib and add it to this table.
     */
    public Index addIndex(Attributes attrib) {
        Index index = new Index();
        index.loadFromXML(attrib);
        addIndex(index);
        return index;
    }

    // ===================================================================================
    //                                                                           Java Name
    //                                                                           =========
    protected boolean _needsJavaNameConvert = true;

    public void suppressJavaNameConvert() {
        _needsJavaNameConvert = false;
    }

    public boolean needsJavaNameConvert() {
        return _needsJavaNameConvert;
    }

    /**
     * Get name to use in Java sources
     */
    public String getJavaName() {
        if (_javaName != null) {
            return _javaName;
        }
        final String pureName = getName();
        if (needsJavaNameConvert()) {
            _javaName = getDatabase().convertJavaNameByJdbcNameAsTable(pureName);
        } else {
            _javaName = pureName; // for sql2entity mainly
        }
        _javaName = filterJavaNameNonCompilableConnector(_javaName);
        return _javaName;
    }

    protected String filterJavaNameNonCompilableConnector(String javaName) {
        final DfLittleAdjustmentProperties prop = getProperties().getLittleAdjustmentProperties();
        return prop.filterJavaNameNonCompilableConnector(javaName, new NonCompilableChecker() {
            public String name() {
                return getName(); // pure name here to convert it to Java name
            }

            public String disp() {
                return getBasicInfoDispString();
            }
        });
    }

    /**
     * Set name to use in Java sources
     */
    public void setJavaName(String javaName) {
        this._javaName = javaName;
    }

    // -----------------------------------------------------
    //                               Uncapitalized Java Name
    //                               -----------------------
    /**
     * Get variable name to use in Java sources (= uncapitalized java name)
     */
    public String getUncapitalisedJavaName() { // allowed spell miss
        return Srl.initUncap(getJavaName());
    }

    // -----------------------------------------------------
    //                         Java Beans Rule Property Name
    //                         -----------------------------
    /**
     * Get property name to use in Java sources (according to java beans rule)
     */
    public String getJavaBeansRulePropertyName() {
        return Srl.initBeansProp(getJavaName());
    }

    // ===================================================================================
    //                                                                     Base Class Name
    //                                                                     ===============
    // -----------------------------------------------------
    //                                                Entity
    //                                                ------
    public String getBaseEntityClassName() { // mutable entity
        // basically pure name sometimes it has mutable mark
        //  e.g. MEMBER_SERVICE -> BsMemberService (normally)
        //  e.g. MEMBER_SERVICE -> BsDbleMemberService (e.g. in Scala)
        final String dbablePrefix = getLittleAdjustmentProperties().getEntityDBablePrefix();
        return buildBaseEntityClassName(dbablePrefix);
    }

    public boolean isMakeImmutableEntity() {
        return getLittleAdjustmentProperties().isMakeImmutableEntity();
    }

    public String getImmutableBaseEntityClassName() { // immutable entity, basically for Scala
        return getPureBaseEntityClassName(); // same as pure name
    }

    public String getMutableBaseEntityClassName() { // mutable entity, basically for Scala
        final String mutablePrefix = getLittleAdjustmentProperties().getEntityMutablePrefix();
        return buildBaseEntityClassName(mutablePrefix);
    }

    protected String getPureBaseEntityClassName() { // e.g. MEMBER_SERVICE -> BsMemberService
        return buildBaseEntityClassName("");
    }

    protected String buildBaseEntityClassName(String symbolPrefix) {
        // there is same logic in old class handler too
        final String projectPrefix = getProjectPrefix();
        final String basePrefix = getBasePrefix();
        final String schemaClassPrefix = getSchemaClassPrefix();
        final String javaName = getJavaName();
        final String baseSuffixForEntity = getDatabase().getBaseSuffixForEntity();
        return projectPrefix + basePrefix + symbolPrefix + schemaClassPrefix + javaName + baseSuffixForEntity;
    }

    protected String getProjectPrefix() {
        return getBasicProperties().getProjectPrefix();
    }

    protected String getBasePrefix() {
        return getBasicProperties().getBasePrefix();
    }

    // -----------------------------------------------------
    //                                              Behavior
    //                                              --------
    public String getBaseDaoClassName() {
        return getPureBaseEntityClassName() + "Dao";
    }

    public String getBaseBehaviorClassName() {
        return getPureBaseEntityClassName() + "Bhv";
    }

    public String getBaseBehaviorApClassName() {
        final String suffix = getBasicProperties().getApplicationBehaviorAdditionalSuffix();
        return getBaseBehaviorClassName() + suffix;
    }

    // referrer load has no generation gap
    //public String getBaseReferrerLoaderClassName() {
    //    return buildBaseEntityClassName("LoaderOf");
    //}

    public String getBaseBehaviorExtendsClassName() {
        return isWritable() ? "AbstractBehaviorWritable" : "AbstractBehaviorReadable";
    }

    // -----------------------------------------------------
    //                                         ConditionBean
    //                                         -------------
    public String getBaseConditionBeanClassName() {
        return getPureBaseEntityClassName() + "CB";
    }

    public String getAbstractBaseConditionQueryClassName() {
        final String projectPrefix = getProjectPrefix();
        final String basePrefix = getBasePrefix();
        return projectPrefix + "Abstract" + basePrefix + getSchemaClassPrefix() + getJavaName() + "CQ";
    }

    public String getBaseConditionQueryClassName() {
        return getPureBaseEntityClassName() + "CQ";
    }

    // -----------------------------------------------------
    //                                            Sql2Entity
    //                                            ----------
    public String getBaseTypeSafeCursorClassName() {
        return getProjectPrefix() + getBasePrefix() + getJavaName() + "Cursor";
    }

    public String getBaseTypeSafeCursorHandlerClassName() {
        return getProjectPrefix() + getBasePrefix() + getJavaName() + "CursorHandler";
    }

    public String getCompanionBaseTypeSafeCursorHandlerClassName() {
        return "Cpon" + getBaseTypeSafeCursorHandlerClassName();
    }

    // ===================================================================================
    //                                                                 Extended Class Name
    //                                                                 ===================
    // -----------------------------------------------------
    //                                                Entity
    //                                                ------
    public String getExtendedEntityClassName() { // mutable entity
        // basically pure name sometimes it has mutable mark
        //  e.g. MEMBER_SERVICE -> MemberService (normally)
        //  e.g. MEMBER_SERVICE -> DbleMemberService (e.g. in Scala)
        final String projectPrefix = getProjectPrefix();
        final String dbablePrefix = getLittleAdjustmentProperties().getEntityDBablePrefix();
        return buildExtendedEntityClassName(projectPrefix, dbablePrefix);
    }

    public String getImmutableExtendedEntityClassName() { // immutable entity, basically for Scala
        return getPureExtendedEntityClassName(); // same as pure name
    }

    public String getMutableExtendedEntityClassName() { // mutable entity, basically for Scala
        final String projectPrefix = getProjectPrefix();
        final String mutablePrefix = getLittleAdjustmentProperties().getEntityMutablePrefix();
        return buildExtendedEntityClassName(projectPrefix, mutablePrefix);
    }

    protected String getPureExtendedEntityClassName() { // e.g. MEMBER_SERVICE -> MemberService
        final String projectPrefix = getProjectPrefix();
        return buildExtendedEntityClassName(projectPrefix, "");
    }

    protected String buildExtendedEntityClassName(String projectPrefix, String symbolPrefix) {
        // there is same logic in old class handler too
        return projectPrefix + symbolPrefix + getSchemaClassPrefix() + getJavaName();
    }

    public String getRelationTraceClassName() {
        return getSchemaClassPrefix() + getJavaName();
    }

    // -----------------------------------------------------
    //                                                DBMeta
    //                                                ------
    public String getDBMetaClassName() {
        return getPureExtendedEntityClassName() + "Dbm";
    }

    public String getDBMetaFullClassName() {
        return getDatabase().getBaseEntityPackage() + ".dbmeta." + getDBMetaClassName();
    }

    // -----------------------------------------------------
    //                                              Behavior
    //                                              --------
    public String getExtendedDaoClassName() {
        return getPureExtendedEntityClassName() + "Dao";
    }

    public String getExtendedDaoFullClassName() {
        final String extendedDaoPackage = getBasicProperties().getExtendedDaoPackage();
        return extendedDaoPackage + "." + getExtendedDaoClassName();
    }

    public String getExtendedBehaviorClassName() {
        return getPureExtendedEntityClassName() + "Bhv";
    }

    public String getExtendedBehaviorApClassName() {
        final String suffix = getBasicProperties().getApplicationBehaviorAdditionalSuffix();
        return getExtendedBehaviorClassName() + suffix;
    }

    public String getExtendedBehaviorLibClassName() {
        final String projectPrefix = getBasicProperties().getLibraryProjectPrefix();
        return buildExtendedEntityClassName(projectPrefix, "") + "Bhv";
    }

    public String getExtendedBehaviorFullClassName() {
        final String extendedBehaviorPackage = getBasicProperties().getExtendedBehaviorPackage();
        return extendedBehaviorPackage + "." + getExtendedBehaviorClassName();
    }

    public String getExtendedBehaviorApFullClassName() {
        final String extendedBehaviorPackage = getBasicProperties().getExtendedBehaviorPackage();
        return extendedBehaviorPackage + "." + getExtendedBehaviorApClassName();
    }

    public String getReferrerLoaderClassName() { // referrer load has no generation gap
        return getExtendedReferrerLoaderClassName();
    }

    protected String getExtendedReferrerLoaderClassName() {
        final String projectPrefix = getProjectPrefix();
        return buildExtendedEntityClassName(projectPrefix, "LoaderOf");
    }

    // -----------------------------------------------------
    //                                         ConditionBean
    //                                         -------------
    public String getExtendedConditionBeanClassName() {
        return getPureExtendedEntityClassName() + "CB";
    }

    public String getExtendedConditionQueryClassName() {
        return getPureExtendedEntityClassName() + "CQ";
    }

    public String getExtendedConditionInlineQueryClassName() {
        return getPureExtendedEntityClassName() + "CIQ";
    }

    public String getNestSelectSetupperClassName() {
        return getPureExtendedEntityClassName() + "Nss";
    }

    public boolean hasConditionInlineQuery() {
        if (getLittleAdjustmentProperties().isCompatibleConditionInlineQueryAlwaysGenerate()) {
            return true;
        }
        return isAvailableMyselfInlineView();
    }

    public boolean hasNestSelectSetupper() {
        if (getLittleAdjustmentProperties().isCompatibleNestSelectSetupperAlwaysGenerate()) {
            return true;
        }
        // might be referred from BizOneToOne so cannot determine it any more
        //final boolean canBeReferred = hasForeignKeyAsOne() || hasReferrer();
        return hasForeignKeyOrReferrerAsOne();
    }

    // -----------------------------------------------------
    //                                            Sql2Entity
    //                                            ----------
    public String getExtendedTypeSafeCursorClassName() {
        return getProjectPrefix() + getJavaName() + "Cursor";
    }

    public String getExtendedTypeSafeCursorHandlerClassName() {
        return getProjectPrefix() + getJavaName() + "CursorHandler";
    }

    public String getCompanionExtendedTypeSafeCursorHandlerClassName() {
        return getExtendedTypeSafeCursorHandlerClassName();
    }

    // ===================================================================================
    //                                                                 Schema Class Prefix
    //                                                                 ===================
    protected String _schemaClassPrefix;

    protected String getSchemaClassPrefix() {
        if (_schemaClassPrefix != null) {
            return _schemaClassPrefix;
        }
        // *however same-name tables between different schemas are unsupported at 0.9.6.8
        // *and the limiter can be removed by DBFlute property at 1.0.3
        if (hasSchema()) {
            final String drivenSchema = _unifiedSchema.getDrivenSchema();
            if (drivenSchema != null) { // forcedly prefix
                _schemaClassPrefix = filterSchemaForClassPrefix(drivenSchema);
            } else {
                _schemaClassPrefix = buildSameNameTableClassPrefix();
            }
        }
        if (_schemaClassPrefix == null) {
            _schemaClassPrefix = "";
        }
        return _schemaClassPrefix;
    }

    protected String buildSameNameTableClassPrefix() {
        final String prefix;
        if (existsSameNameTable()) {
            if (isMainSchema()) {
                // no prefix if main schema
                // same-name table of other schemas should have prefix
                prefix = "";
            } else if (isAdditionalSchema()) {
                // fixed prefix according to its definition if additional schema
                if (isCatalogAdditionalSchema()) {
                    prefix = buildCatalogSchemaTableClassPrefix();
                } else { // pure schema
                    prefix = buildPureSchemaTableClassPrefix();
                }
            } else { // unknown schema 
                // dynamic prefix according to table's determination if unknown schema
                if (existsSameSchemaSameNameTable()) {
                    prefix = buildCatalogSchemaTableClassPrefix();
                } else {
                    prefix = buildPureSchemaTableClassPrefix();
                }
            }
        } else { // unique name (mainly here)
            prefix = "";
        }
        return prefix;
    }

    protected String buildCatalogSchemaTableClassPrefix() {
        // schema of DB2 may have space either size so needs to trim it
        final String pureCatalog = filterSchemaForClassPrefix(getPureCatalog());
        final String pureSchema = filterSchemaForClassPrefix(getPureSchema());
        return pureCatalog + pureSchema;
    }

    protected String buildPureSchemaTableClassPrefix() {
        return filterSchemaForClassPrefix(getPureSchema());
    }

    protected String filterSchemaForClassPrefix(String name) {
        if (name == null) {
            return "";
        }
        if (name.contains("_")) { // EXAMPLE_DB
            return Srl.camelize(name);
        } else { // e.g. 'EXAMPLEDB' (no delimiter, one word)
            return Srl.initCapTrimmed(name.trim().toLowerCase());
        }
    }

    // ===================================================================================
    //                                                              Behavior Handling Name
    //                                                              ======================
    public String getBehaviorComponentName() {
        final String componentName = Srl.initUncap(getExtendedBehaviorClassName());

        // remove "$" because a component name that has a dollar mark may be unsupported
        // for example, in Spring Framework case:
        //   -> SAXParseException: Attribute value FOO$BAR of type ID must be a name.
        return Srl.replace(componentName, "$", "");
    }

    public String getBehaviorInstanceMethodName() { // basically for Scala
        final String methodName = Srl.initUncap(getExtendedBehaviorClassName());
        final DfLanguageDependency lang = getLanguageDependency();
        return lang.getLanguageGrammar().adjustMethodInitialChar(methodName);
    }

    public String getBehaviorApComponentName() {
        final String suffix = getBasicProperties().getApplicationBehaviorAdditionalSuffix();
        return getBehaviorComponentName() + suffix;
    }

    public String getDaoComponentName() { // basically for CSharp
        return getDatabase().filterComponentNameWithProjectPrefix(getUncapitalisedJavaName()) + "Dao";
    }

    // ===================================================================================
    //                                                               Sql2Entity Definition
    //                                                               =====================
    // -----------------------------------------------------
    //                                        Basic Property
    //                                        --------------
    public boolean isSql2EntityCustomize() {
        return _sql2EntityCustomize;
    }

    public void setSql2EntityCustomize(boolean sql2EntityCustomize) {
        _sql2EntityCustomize = sql2EntityCustomize;
    }

    public boolean isSql2EntityCustomizeHasNested() {
        return _sql2EntityCustomizeHasNested;
    }

    public void setSql2EntityCustomizeHasNested(boolean sql2EntityCustomizeHasNested) {
        _sql2EntityCustomizeHasNested = sql2EntityCustomizeHasNested;
    }

    public boolean isSql2EntityTypeSafeCursor() {
        return _sql2EntityTypeSafeCursor;
    }

    public void setSql2EntityTypeSafeCursor(boolean sql2EntityTypeSafeCursor) {
        this._sql2EntityTypeSafeCursor = sql2EntityTypeSafeCursor;
    }

    // -----------------------------------------------------
    //                                     Physical Property
    //                                     -----------------
    /**
     * @return The file of outside-SQL. (basically NotNull if Sql2Entity)
     */
    public DfOutsideSqlFile getSql2EntitySqlFile() {
        return _sql2EntitySqlFile;
    }

    /**
     * @param sql2EntitySqlFile The file of outside-SQL. (basically NotNull if Sql2Entity)
     */
    public void setSql2EntitySqlFile(DfOutsideSqlFile sql2EntitySqlFile) {
        this._sql2EntitySqlFile = sql2EntitySqlFile;
    }

    /**
     * @return The output directory for Sql2Entity. (NotNull)
     */
    public String getSql2EntityOutputDirectory() {
        if (_sql2EntitySqlFile != null) {
            return _sql2EntitySqlFile.getSql2EntityOutputDirectory();
        }
        // basically no way if Sql2Entity
        return getOutsideSqlProperties().getSql2EntityOutputDirectory();
    }

    // -----------------------------------------------------
    //                                     Thematic Property
    //                                     -----------------
    public boolean isGenerateTypeSafeCursor() {
        return isSql2EntityTypeSafeCursor(); // only when cursor (might be with cursor)
    }

    public boolean isGenerateCustomizeEntity() {
        // always generate in spite of pure cursor
        // because it might be used for paging or free-style select
        // and to avoid remaining old customize entity of pure cursor
        return true;
    }

    public boolean isLoadableCustomizeEntity() {
        final Table domain = getLoadableCustomizeDomain();
        return domain != null && domain.hasReferrerAsMany();
    }

    public Table getLoadableCustomizeDomain() {
        if (!isSql2EntityCustomize() || !hasPrimaryKey()) {
            return null;
        }
        final List<Column> primaryKeyList = getPrimaryKey();
        for (Column pk : primaryKeyList) {
            // check whether the related column is also primary key if it exists
            final Column relatedColumn = pk.getSql2EntityRelatedColumn();
            if (relatedColumn != null && !relatedColumn.isPrimaryKey()) {
                return null;
            }
        }
        return primaryKeyList.get(0).getSql2EntityRelatedTable();
    }

    public List<String> getLoadableCustomizePrimaryKeySettingExpressionList() {
        final Table domain = getLoadableCustomizeDomain();
        if (domain == null) {
            return DfCollectionUtil.emptyList();
        }
        final List<Column> primaryKeyList = getPrimaryKey();
        final List<String> settingList = DfCollectionUtil.newArrayList();
        final boolean hasRelatedColumn; // true if all PKs have related columns
        {
            boolean notFoundExists = false;
            for (Column pk : primaryKeyList) {
                if (!pk.hasSql2EntityRelatedColumn()) {
                    notFoundExists = true; // for example, PostgreSQL
                    break;
                }
            }
            hasRelatedColumn = !notFoundExists;
        }
        int index = 0;
        for (Column pk : primaryKeyList) {
            final Column relatedColumn;
            if (hasRelatedColumn) {
                relatedColumn = pk.getSql2EntityRelatedColumn();
            } else {
                // if there are not related columns, it uses a key order
                relatedColumn = domain.getPrimaryKey().get(index);
            }
            final DfLanguageGrammar grammar = getBasicProperties().getLanguageDependency().getLanguageGrammar();
            settingList.add(grammar.buildEntityPropertyGetSet(pk, relatedColumn));
            ++index;
        }
        return settingList;
    }

    // -----------------------------------------------------
    //                               Switch Output Directory
    //                               -----------------------
    public void switchSql2EntityOutputDirectory() {
        final String outputDirectory = getSql2EntityOutputDirectory();
        getProperties().getOutsideSqlProperties().switchSql2EntityOutputDirectory(outputDirectory);
    }

    public void switchSql2EntitySimpleDtoOutputDirectory() {
        final DfOutsideSqlProperties prop = getProperties().getOutsideSqlProperties();
        if (_sql2EntitySqlFile != null && _sql2EntitySqlFile.isSqlAp()) {
            prop.switchSql2EntityOutputDirectory(_sql2EntitySqlFile.getSql2EntityOutputDirectory());
        } else {
            final String outputDirectory = getProperties().getSimpleDtoProperties().getSimpleDtoOutputDirectory();
            prop.switchSql2EntityOutputDirectory(outputDirectory);
        }
    }

    public void switchSql2EntityDtoMapperOutputDirectory() {
        final DfOutsideSqlProperties prop = getProperties().getOutsideSqlProperties();
        if (_sql2EntitySqlFile != null && _sql2EntitySqlFile.isSqlAp()) {
            prop.switchSql2EntityOutputDirectory(_sql2EntitySqlFile.getSql2EntityOutputDirectory());
        } else {
            final String outputDirectory = getProperties().getSimpleDtoProperties().getDtoMapperOutputDirectory();
            prop.switchSql2EntityOutputDirectory(outputDirectory);
        }
    }

    // ===================================================================================
    //                                                                             Utility
    //                                                                             =======
    /**
     * Returns the elements of the list, separated by commas.
     * @param list a list of Columns
     * @return A CSV list.
     */
    private String printList(List<Column> list) {
        StringBuilder result = new StringBuilder();
        boolean comma = false;
        for (Iterator<Column> iter = list.iterator(); iter.hasNext();) {
            Column col = (Column) iter.next();
            if (col.isPrimaryKey()) {
                if (comma) {
                    result.append(',');
                } else {
                    comma = true;
                }
                result.append(col.getName());
            }
        }
        return result.toString();
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

    protected DfLanguageDependency getLanguageDependency() {
        return getBasicProperties().getLanguageDependency();
    }

    protected DfLanguageGrammar getLanguageGrammar() {
        return getLanguageDependency().getLanguageGrammar();
    }

    protected DfDatabaseProperties getDatabaseProperties() {
        return getProperties().getDatabaseProperties();
    }

    protected DfClassificationProperties getClassificationProperties() {
        return getProperties().getClassificationProperties();
    }

    protected DfCommonColumnProperties getCommonColumnProperties() {
        return getProperties().getCommonColumnProperties();
    }

    protected DfIncludeQueryProperties getIncludeQueryProperties() {
        return DfBuildProperties.getInstance().getIncludeQueryProperties();
    }

    protected DfLittleAdjustmentProperties getLittleAdjustmentProperties() {
        return getProperties().getLittleAdjustmentProperties();
    }

    protected DfSequenceIdentityProperties getSequenceIdentityProperties() {
        return getProperties().getSequenceIdentityProperties();
    }

    protected DfOutsideSqlProperties getOutsideSqlProperties() {
        return getProperties().getOutsideSqlProperties();
    }

    // ===================================================================================
    //                                                                       Include Query
    //                                                                       =============
    public boolean isAvailableRelationSpecifiedDerivedOrderBy() {
        final Column dummyPk = getTableOnlyIncludeQueryDummyColumn(); // it is available in any time
        return dummyPk == null || getIncludeQueryProperties().isAvailableRelationSpecifiedDerivedOrderBy(dummyPk);
    }

    public boolean isAvailableMyselfInlineView() {
        final Column dummyPk = getTableOnlyIncludeQueryDummyColumn(); // it is available in any time
        return dummyPk == null || getIncludeQueryProperties().isAvailableMyselfInlineView(dummyPk);
    }

    public boolean isAvailableMyselfScalarCondition() {
        final Column dummyPk = getTableOnlyIncludeQueryDummyColumn(); // it is available with compound PK
        return dummyPk == null || getIncludeQueryProperties().isAvailableMyselfScalarCondition(dummyPk);
    }

    public boolean isAvailableMyselfMyselfDerived() {
        return getIncludeQueryProperties().isAvailableMyselfMyselfDerived(getPrimaryKeyAsOne());
    }

    public boolean isAvailableMyselfMyselfExists() {
        return getIncludeQueryProperties().isAvailableMyselfMyselfExists(getPrimaryKeyAsOne());
    }

    public boolean isAvailableMyselfMyselfInScope() {
        return getIncludeQueryProperties().isAvailableMyselfMyselfInScope(getPrimaryKeyAsOne());
    }

    protected Column getTableOnlyIncludeQueryDummyColumn() {
        final Column dummyPk;
        if (hasPrimaryKey()) {
            if (hasSinglePrimaryKey()) {
                dummyPk = getPrimaryKeyAsOne();
            } else {
                dummyPk = getPrimaryKey().get(0);
            }
        } else {
            final List<Column> columnList = getColumnList();
            if (!columnList.isEmpty()) { // just in case
                dummyPk = columnList.get(0);
            } else { // basically no way
                dummyPk = null; // means available fixedly
            }
        }
        return dummyPk;
    }

    // ===================================================================================
    //                                                                      Classification
    //                                                                      ==============
    public boolean hasClassification() {
        final Column[] columns = getColumns();
        for (Column column : columns) {
            if (column.hasClassification()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasTableClassification() {
        final Column[] columns = getColumns();
        for (Column column : columns) {
            if (column.isTableClassification()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasImplicitClassification() {
        final Column[] columns = getColumns();
        for (Column column : columns) {
            if (column.hasClassification() && !column.isTableClassification()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasPrimaryKeyForcedClassificationSetting() {
        final List<Column> columns = getPrimaryKey();
        for (Column column : columns) {
            if (column.isForceClassificationSetting()) {
                return true;
            }
        }
        final List<Unique> uniqueList = getKeyableUniqueList();
        for (Unique unique : uniqueList) {
            final List<Column> columnList = unique.getColumnList();
            for (Column column : columnList) {
                if (column.isForceClassificationSetting()) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean isSuppressDBAccessClass() {
        return getClassificationProperties().isSuppressDBAccessClassTable(getTableDbName());
    }

    public boolean hasCheckClassificationCodeOnEntity() {
        if (!hasClassification()) {
            return false;
        }
        final List<Column> columnList = getColumnList();
        for (Column column : columnList) {
            if (column.hasCheckClassificationCodeOnEntity()) {
                return true;
            }
        }
        return false;
    }

    // ===================================================================================
    //                                                                            Sequence
    //                                                                            ========
    /**
     * Determine whether this table uses a sequence.
     * @return The determination, true or false.
     */
    public boolean isUseSequence() {
        final String sequenceName = getSequenceIdentityProperties().getSequenceName(getTableDbName());
        if (sequenceName == null || sequenceName.trim().length() == 0) {
            if (hasPostgreSQLSerialSequenceName()) {
                return true;
            }
            return false;
        } else {
            return true;
        }
    }

    /**
     * Get the value of sequence name defined at definition map.
     * @return The string as name. (NotNull: If a sequence is not found, return empty string.)
     */
    public String getDefinedSequenceName() {
        if (!isUseSequence()) {
            return "";
        }
        final String sequenceName = getSequenceIdentityProperties().getSequenceName(getTableDbName());
        if (Srl.is_Null_or_TrimmedEmpty(sequenceName)) {
            final String serialSequenceName = extractPostgreSQLSerialSequenceName();
            if (Srl.is_NotNull_and_NotTrimmedEmpty(serialSequenceName)) {
                return serialSequenceName;
            }
            return ""; // if it uses sequence, unreachable
        }
        return sequenceName;
    }

    /**
     * Get the value of sequence name for SQL.
     * @return The string as name. (NotNull: If a sequence is not found, return empty string.)
     */
    public String getSequenceSqlName() {
        if (!isUseSequence()) {
            return "";
        }
        final String sequenceName = getSequenceIdentityProperties().getSequenceName(getTableDbName());
        if (Srl.is_Null_or_TrimmedEmpty(sequenceName)) {
            final String serialSequenceName = extractPostgreSQLSerialSequenceName();
            if (Srl.is_Null_or_TrimmedEmpty(serialSequenceName)) {
                String msg = "The sequence for serial type should exist when isUseSequence() is true!";
                throw new IllegalStateException(msg);
            }
            // the schema prefix of sequence for serial type has already been resolved here
            // (the name in default value has schema prefix)
            return serialSequenceName;
        }
        return sequenceName;
    }

    /**
     * Get the SQL for next value of sequence.
     * @return The SQL for next value of sequence. (NotNull: If a sequence is not found, return empty string.)
     */
    public String getSequenceNextValSql() { // basically for C#
        if (!isUseSequence()) {
            return "";
        }
        final DBDef dbdef = getBasicProperties().getCurrentDBDef();
        final String sequenceName = getSequenceSqlName();
        final String sql = dbdef.dbway().buildSequenceNextValSql(sequenceName);
        return sql != null ? sql : "";
    }

    public BigDecimal getSequenceMinimumValue() {
        if (!isUseSequence()) {
            return null;
        }
        final DfSequenceIdentityProperties prop = getSequenceIdentityProperties();
        final DfSchemaSource ds = getDatabase().getDataSource();
        BigDecimal value = prop.getSequenceMinimumValueByTableName(ds, getUnifiedSchema(), getTableDbName());
        if (value == null) {
            final String sequenceName = extractPostgreSQLSerialSequenceName();
            if (sequenceName != null && sequenceName.trim().length() > 0) {
                value = prop.getSequenceMinimumValueBySequenceName(ds, getUnifiedSchema(), sequenceName);
            }
        }
        return value;
    }

    public String getSequenceMinimumValueExpression() {
        final BigDecimal value = getSequenceMinimumValue();
        return value != null ? value.toString() : "null";
    }

    public BigDecimal getSequenceMaximumValue() {
        if (!isUseSequence()) {
            return null;
        }
        final DfSequenceIdentityProperties prop = getSequenceIdentityProperties();
        final DfSchemaSource ds = getDatabase().getDataSource();
        BigDecimal value = prop.getSequenceMaximumValueByTableName(ds, getUnifiedSchema(), getTableDbName());
        if (value == null) {
            final String sequenceName = extractPostgreSQLSerialSequenceName();
            if (sequenceName != null && sequenceName.trim().length() > 0) {
                value = prop.getSequenceMaximumValueBySequenceName(ds, getUnifiedSchema(), sequenceName);
            }
        }
        return value;
    }

    public String getSequenceMaximumValueExpression() {
        final BigDecimal value = getSequenceMaximumValue();
        return value != null ? value.toString() : "null";
    }

    public Integer getSequenceIncrementSize() {
        if (!isUseSequence()) {
            return null;
        }
        final DfSequenceIdentityProperties prop = getSequenceIdentityProperties();
        final DfSchemaSource ds = getDatabase().getDataSource();
        Integer size = prop.getSequenceIncrementSizeByTableName(ds, getUnifiedSchema(), getTableDbName());
        if (size == null) {
            final String sequenceName = extractPostgreSQLSerialSequenceName();
            if (sequenceName != null && sequenceName.trim().length() > 0) {
                size = prop.getSequenceIncrementSizeBySequenceName(ds, getUnifiedSchema(), sequenceName);
            }
        }
        return size;
    }

    public String getSequenceIncrementSizeExpression() {
        final Integer value = getSequenceIncrementSize();
        return value != null ? value.toString() : "null";
    }

    public Integer getSequenceCacheSize() {
        if (!isUseSequence()) {
            return null;
        }
        final DfSequenceIdentityProperties prop = getSequenceIdentityProperties();
        final DfSchemaSource ds = getDatabase().getDataSource();
        return prop.getSequenceCacheSize(ds, getUnifiedSchema(), getTableDbName());
    }

    public String getSequenceCacheSizeExpression() {
        final Integer value = getSequenceCacheSize();
        return value != null ? value.toString() : "null";
    }

    public String getSequenceReturnType() {
        final DfSequenceIdentityProperties sequenceIdentityProperties = getProperties().getSequenceIdentityProperties();
        final String sequenceReturnType = sequenceIdentityProperties.getSequenceReturnType();
        if (hasCompoundPrimaryKey()) {
            return sequenceReturnType;
        }
        final Column primaryKeyAsOne = getPrimaryKeyAsOne();
        if (primaryKeyAsOne.isJavaNativeNumberObject()) {
            return primaryKeyAsOne.getJavaNative();
        }
        return sequenceReturnType;
    }

    /**
     * Has sequence name of postgreSQL serial type column.
     * @return The determination, true or false.
     */
    protected boolean hasPostgreSQLSerialSequenceName() {
        final String postgreSQLSerialSequenceName = extractPostgreSQLSerialSequenceName();
        return postgreSQLSerialSequenceName != null;
    }

    /**
     * Extract sequence name of postgreSQL serial type column.
     * @return Sequence name of postgreSQL serial type column. (NullAllowed: If null, not found)
     */
    protected String extractPostgreSQLSerialSequenceName() {
        final DfBasicProperties basicProperties = getBasicProperties();
        if (!basicProperties.isDatabasePostgreSQL() || !hasAutoIncrementColumn()) {
            return null;
        }
        final Column autoIncrementColumn = getAutoIncrementColumn();
        if (autoIncrementColumn == null) {
            return null;
        }
        final String defaultValue = autoIncrementColumn.getDefaultValue();
        if (defaultValue == null) {
            return null;
        }
        final String prefix = "nextval('";
        if (!defaultValue.startsWith(prefix)) {
            return null;
        }
        final String excludedPrefixString = defaultValue.substring(prefix.length());
        final int endIndex = excludedPrefixString.indexOf("'");
        if (endIndex < 0) {
            return null;
        }
        return excludedPrefixString.substring(0, endIndex);
    }

    /**
     * Get the value of assigned property name.
     * @return Assigned property name. (NotNull)
     */
    public String getAssignedPropertyName() {
        final Column primaryKeyAsOne = getPrimaryKeyAsOne();
        return getPropertyNameResolvedLanguage(primaryKeyAsOne);
    }

    protected String getPropertyNameResolvedLanguage(Column col) {
        return getBasicProperties().getLanguageDependency().getLanguageGrammar().buildEntityPropertyName(col);
    }

    protected List<Column> _subColumnSequenceColumnList;

    public boolean isUseSubColumnSequence() {
        return !getSubColumnSequenceColumnList().isEmpty();
    }

    public List<Column> getSubColumnSequenceColumnList() {
        if (_subColumnSequenceColumnList != null) {
            return _subColumnSequenceColumnList;
        }
        _subColumnSequenceColumnList = DfCollectionUtil.newArrayList();
        final DfSequenceIdentityProperties prop = getSequenceIdentityProperties();
        if (!prop.hasSubColumnSequence()) {
            return _subColumnSequenceColumnList;
        }
        final List<Column> columnList = getColumnList();
        for (Column column : columnList) {
            final String sequenceName = prop.getSubColumnSequenceName(getTableDbName(), column.getName());
            if (sequenceName != null) {
                _subColumnSequenceColumnList.add(column);
            }
        }
        return _subColumnSequenceColumnList;
    }

    // ===================================================================================
    //                                                                            Identity
    //                                                                            ========
    /**
     * Determine whether this table uses an identity.
     * @return The determination, true or false.
     */
    public boolean isUseIdentity() {
        final DfBasicProperties basicProperties = getBasicProperties();

        // because serial type is treated as sequence
        if (basicProperties.isDatabasePostgreSQL()) {
            return false;
        }

        // It gives priority to auto-increment information of JDBC.
        if (hasAutoIncrementColumn()) {
            return true;
        }
        final DfSequenceIdentityProperties prop = getSequenceIdentityProperties();
        return prop.getIdentityColumnName(getTableDbName()) != null;
    }

    public String getIdentityColumnName() {
        final Column column = getIdentityColumn();
        return column != null ? column.getName() : "";
    }

    public String getIdentityPropertyName() {
        final Column column = getIdentityColumn();
        return column != null ? getPropertyNameResolvedLanguage(column) : "";
    }

    protected Column getIdentityColumn() {
        if (!isUseIdentity()) {
            return null;
        }

        // It gives priority to auto-increment information of JDBC.
        final Column autoIncrementColumn = getAutoIncrementColumn();
        if (autoIncrementColumn != null) {
            return autoIncrementColumn;
        }
        final DfSequenceIdentityProperties prop = getSequenceIdentityProperties();
        final String columnName = prop.getIdentityColumnName(getTableDbName());
        final Column column = getColumn(columnName);
        if (column == null) {
            String msg = "The columnName does not exist in the table: ";
            msg = msg + " tableName=" + getTableDbName() + " columnName=" + columnName;
            msg = msg + " columnList=" + getColumnNameCommaString();
            throw new IllegalStateException(msg);
        }
        return column;
    }

    protected boolean hasAutoIncrementColumn() {
        final Column[] columnArray = getColumns();
        for (Column column : columnArray) {
            if (column.isAutoIncrement()) {
                return true;
            }
        }
        return false;
    }

    protected Column getAutoIncrementColumn() {
        final Column[] columnArray = getColumns();
        for (Column column : columnArray) {
            if (column.isAutoIncrement()) {
                return column;
            }
        }
        return null;
    }

    // ===================================================================================
    //                                                                     Optimistic Lock
    //                                                                     ===============
    public boolean hasOptimisticLock() {
        return isUseUpdateDate() || isUseVersionNo();
    }

    // ===================================================================================
    //                                                                          UpdateDate
    //                                                                          ==========
    /**
     * Determine whether this table uses a update date column.
     * @return The determination, true or false.
     */
    public boolean isUseUpdateDate() {
        final String updateDateColumnName = getProperties().getOptimisticLockProperties().getUpdateDateFieldName();
        if ("".equals(updateDateColumnName)) {
            return false;
        }
        final Column column = getColumn(updateDateColumnName);
        if (column == null) {
            return false;
        }
        return true;
    }

    protected Column getUpdateDateColumn() {
        if (!isUseUpdateDate()) {
            return null;
        }
        final String fieldName = getProperties().getOptimisticLockProperties().getUpdateDateFieldName();
        if (fieldName != null && fieldName.trim().length() != 0) {
            final Column column = getColumn(fieldName);
            return column;
        } else {
            return null;
        }
    }

    public String getUpdateDateColumnName() {
        final Column column = getUpdateDateColumn();
        if (column == null) {
            return "";
        }
        return column.getName();
    }

    public String getUpdateDateJavaName() {
        final Column column = getUpdateDateColumn();
        if (column == null) {
            return "";
        }
        return column.getJavaName();
    }

    public String getUpdateDateUncapitalisedJavaName() {
        return Srl.initUncap(getUpdateDateJavaName());
    }

    public String getUpdateDatePropertyName() {
        final Column column = getUpdateDateColumn();
        if (column == null) {
            return "";
        }
        return getPropertyNameResolvedLanguage(column);
    }

    /**
     * Get the value of update-date as uncapitalised java name.
     * @return String. (NotNull)
     */
    public String getUpdateDateJavaNative() {
        if (!isUseUpdateDate()) {
            return "";
        }
        final Column column = getColumn(getProperties().getOptimisticLockProperties().getUpdateDateFieldName());
        return column.getJavaNative();
    }

    // ===================================================================================
    //                                                                           VersionNo
    //                                                                           =========
    /**
     * Determine whether this table uses a version-no column.
     * @return The determination, true or false.
     */
    public boolean isUseVersionNo() {
        final String versionNoColumnName = getProperties().getOptimisticLockProperties().getVersionNoFieldName();
        final Column column = getColumn(versionNoColumnName);
        if (column == null) {
            return false;
        }
        return true;
    }

    public Column getVersionNoColumn() {
        if (!isUseVersionNo()) {
            return null;
        }
        final String versionNoColumnName = getProperties().getOptimisticLockProperties().getVersionNoFieldName();
        return getColumn(versionNoColumnName);
    }

    public String getVersionNoColumnName() {
        final Column column = getVersionNoColumn();
        if (column == null) {
            return "";
        }
        return column.getName();
    }

    public String getVersionNoJavaName() {
        final Column column = getVersionNoColumn();
        if (column == null) {
            return "";
        }
        return column.getJavaName();
    }

    public String getVersionNoPropertyName() {
        final Column column = getVersionNoColumn();
        if (column == null) {
            return "";
        }
        return getPropertyNameResolvedLanguage(column);
    }

    public String getVersionNoUncapitalisedJavaName() {
        return buildVersionNoUncapitalisedJavaName(getVersionNoJavaName());
    }

    protected String buildVersionNoUncapitalisedJavaName(String versionNoJavaName) {
        return Srl.initUncap(versionNoJavaName);
    }

    public boolean isVersionNoHasValueMethodValid() {
        if (!isUseVersionNo()) {
            return false;
        }
        // basically other types are not used as version no
        final Column col = getVersionNoColumn();
        return !col.isJavaNativePrimitiveInt() && !col.isJavaNativePrimitiveLong();
    }

    // ===================================================================================
    //                                                                       Common Column
    //                                                                       =============
    /**
     * Is this table defined all common columns?
     * @return The determination, true or false.
     */
    public boolean hasAllCommonColumn() {
        try {
            return doHasAllCommonColumn();
        } catch (RuntimeException e) {
            _log.debug("Failed to execute 'Table.hasAllCommonColumn()'!", e);
            throw e;
        }
    }

    protected boolean doHasAllCommonColumn() {
        if (!isWritable()) {
            return false;
        }
        if (isAdditionalSchema()) {
            final DfDatabaseProperties prop = getDatabaseProperties();
            final DfAdditionalSchemaInfo schemaInfo = prop.getAdditionalSchemaInfo(_unifiedSchema);
            if (schemaInfo.isSuppressCommonColumn()) {
                return false;
            }
        }
        final List<String> commonColumnNameList = getCommonColumnProperties().getCommonColumnNameList();
        if (commonColumnNameList.isEmpty()) {
            return false;
        }
        for (String commonColumnName : commonColumnNameList) {
            if (getCommonColumnProperties().isCommonColumnConversion(commonColumnName)) {
                final Column col = findMyCommonColumn(commonColumnName);
                if (col == null) {
                    return false;
                }
            } else {
                if (!_columnMap.containsKey(commonColumnName)) {
                    return false;
                }
            }
        }
        return true;
    }

    public List<Column> getCommonColumnList() {
        final List<Column> ls = new ArrayList<Column>();
        if (!hasAllCommonColumn()) {
            return ls;
        }
        final List<String> commonColumnNameList = getCommonColumnProperties().getCommonColumnNameList();
        for (String commonColumnName : commonColumnNameList) {
            ls.add(findMyCommonColumn(commonColumnName));
        }
        return ls;
    }

    public String getCommonColumnListSetupExpression() {
        return buildCommonColumnListSetupExpression(getCommonColumnList());
    }

    public List<Column> getCommonColumnBeforeInsertList() {
        final List<Column> ls = new ArrayList<Column>();
        if (!hasAllCommonColumn()) {
            return ls;
        }
        final List<String> commonColumnNameList = getCommonColumnProperties().getCommonColumnNameList();
        for (String commonColumnName : commonColumnNameList) {
            if (getCommonColumnProperties().hasCommonColumnBeforeInsertLogic(commonColumnName)) {
                ls.add(findMyCommonColumn(commonColumnName));
            }
        }
        return ls;
    }

    public String getCommonColumnBeforeInsertListSetupExpression() {
        return buildCommonColumnListSetupExpression(getCommonColumnBeforeInsertList());
    }

    public List<Column> getCommonColumnBeforeUpdateList() {
        final List<Column> ls = new ArrayList<Column>();
        if (!hasAllCommonColumn()) {
            return ls;
        }
        final List<String> commonColumnNameList = getCommonColumnProperties().getCommonColumnNameList();
        for (String commonColumnName : commonColumnNameList) {
            if (getCommonColumnProperties().hasCommonColumnBeforeUpdateLogic(commonColumnName)) {
                ls.add(findMyCommonColumn(commonColumnName));
            }
        }
        return ls;
    }

    public String getCommonColumnBeforeUpdateListSetupExpression() {
        return buildCommonColumnListSetupExpression(getCommonColumnBeforeUpdateList());
    }

    protected String buildCommonColumnListSetupExpression(List<Column> commonColumnList) {
        final DfLanguageImplStyle implStyle = getLanguageDependency().getLanguageImplStyle();
        final DfLanguageGrammar grammar = getLanguageGrammar();
        final StringBuilder sb = new StringBuilder();
        int index = 0;
        for (Column column : commonColumnList) {
            if (index > 0) {
                sb.append(", ");
            }
            final String resourceName = "column" + column.getJavaName();
            final String callExp;
            if (implStyle.isDBMetaColumnGetterProperty()) {
                callExp = grammar.buildPropertyGetterCall(resourceName);
            } else { // method style
                callExp = resourceName + "()";
            }
            sb.append(callExp);
            ++index;
        }
        return sb.toString();
    }

    public String findTargetColumnUncapitalisedJavaNameByCommonColumnName(String commonColumnName) { // called by templates
        final Column column = findMyCommonColumn(commonColumnName);
        return column != null ? column.getUncapitalisedJavaName() : null;
    }

    public String findTargetColumnJavaNameByCommonColumnName(String commonColumnName) { // called by templates
        final Column column = findMyCommonColumn(commonColumnName);
        return column != null ? column.getJavaName() : null;
    }

    public String findTargetColumnNameByCommonColumnName(String commonColumnName) { // called by templates
        final Column column = findMyCommonColumn(commonColumnName);
        return column != null ? column.getName() : null;
    }

    protected Column findMyCommonColumn(String commonColumnName) {
        final Column column;
        if (getCommonColumnProperties().isCommonColumnConversion(commonColumnName)) {
            column = getColumn(convertCommonColumnName(commonColumnName));
        } else {
            column = getColumn(commonColumnName);
        }
        return column;
    }

    protected Column getCommonColumnNormal(String commonColumnName) {
        return getColumn(commonColumnName);
    }

    protected String convertCommonColumnName(String commonColumnName) {
        String filteredCommonColumn = getCommonColumnProperties().filterCommonColumn(commonColumnName);
        final String pureName = getName();
        filteredCommonColumn = Srl.replace(filteredCommonColumn, "TABLE_NAME", pureName);
        filteredCommonColumn = Srl.replace(filteredCommonColumn, "table_name", pureName);
        final String javaName = getJavaName();
        filteredCommonColumn = Srl.replace(filteredCommonColumn, "TableName", javaName);
        filteredCommonColumn = Srl.replace(filteredCommonColumn, "tablename", javaName);
        return filteredCommonColumn;
    }

    // ===================================================================================
    //                                                                     Behavior Status
    //                                                                     ===============
    public boolean hasBehavior() {
        return !isSuppressDBAccessClass();
    }

    public boolean hasReferrerLoader() { // the loader class of referrer
        // no relation is rare case so simplify
        //return hasBehavior() && hasRelation();
        return hasBehavior();
    }

    public boolean hasLoadReferrer() { // the function for one-to-many
        return hasPrimaryKey() && hasReferrerAsMany();
    }

    public boolean isAvailableLoadReferrerByOldOption() {
        return getLittleAdjustmentProperties().isCompatibleLoadReferrerOldOption();
    }

    public String getLoadReferrerConditionSetupperName() {
        final Class<?> type;
        if (getLittleAdjustmentProperties().isCompatibleLoadReferrerConditionBeanSetupper()) {
            type = ConditionBeanSetupper.class;
        } else {
            type = ReferrerConditionSetupper.class;
        }
        return type.getSimpleName();
    }

    // ===================================================================================
    //                                                                 Behavior Adjustment
    //                                                                 ===================
    public boolean isAvailableNonPrimaryKeyWritable() {
        if (hasPrimaryKey()) {
            return false;
        }
        return getLittleAdjustmentProperties().isAvailableNonPrimaryKeyWritable();
    }

    // -----------------------------------------------------
    //                                         Select Entity
    //                                         -------------
    public boolean isAvailableSelectEntityPlainReturn() {
        return getLittleAdjustmentProperties().isAvailableSelectEntityPlainReturn();
    }

    public String filterSelectEntityOptionalReturn(String entityType) {
        final String optionalEntity = getLittleAdjustmentProperties().getBasicOptionalEntitySimpleName();
        return optionalEntity + getLanguageGrammar().buildGenericOneClassHint(entityType);
    }

    public String filterSelectEntityOptionalReturnIfNeeds(String entityType) {
        if (isAvailableSelectEntityPlainReturn()) {
            return entityType;
        } else {
            final String optionalEntity = getLittleAdjustmentProperties().getBasicOptionalEntitySimpleName();
            return optionalEntity + getLanguageGrammar().buildGenericOneClassHint(entityType);
        }
    }

    protected boolean isAvailableSelectEntityWithDeletedCheck() {
        return getLittleAdjustmentProperties().isAvailableSelectEntityWithDeletedCheck();
    }

    public String getSelectEntityWithDeletedCheckModifier() {
        final DfLanguageGrammar grammar = getLanguageGrammar();
        return isAvailableSelectEntityWithDeletedCheck() ? grammar.getPublicModifier() : grammar.getProtectedModifier();
    }

    public String getSelectEntityWithDeletedCheckModifierAsPrefix() {
        final String modifier = getSelectEntityWithDeletedCheckModifier();
        return !modifier.isEmpty() ? modifier + " " : "";
    }

    public boolean isCompatibleSelectByPKPlainReturn() {
        return getLittleAdjustmentProperties().isCompatibleSelectByPKPlainReturn();
    }

    public String filterSelectByPKOptionalReturnIfNeeds(String entityType) {
        if (isCompatibleSelectByPKPlainReturn()) {
            return entityType;
        } else {
            final String optionalEntity = getLittleAdjustmentProperties().getBasicOptionalEntitySimpleName();
            return optionalEntity + getLanguageGrammar().buildGenericOneClassHint(entityType);
        }
    }

    public String getSelectByPKSuffix() {
        final DfLittleAdjustmentProperties prop = getLittleAdjustmentProperties();
        return prop.isCompatibleSelectByPKOldStyle() ? "Value" : "";
    }

    public boolean isCompatibleSelectByPKWithDeletedCheck() {
        final DfLittleAdjustmentProperties prop = getLittleAdjustmentProperties();
        return prop.isCompatibleSelectByPKWithDeletedCheck();
    }

    // -----------------------------------------------------
    //                                   Optional Properties
    //                                   -------------------
    public boolean needsBasicOptionalEntityImport() {
        return getLittleAdjustmentProperties().needsBasicOptionalEntityImport();
    }

    public boolean needsRelationOptionalEntityImport() {
        return hasOptionalRelation() && getLittleAdjustmentProperties().needsRelationOptionalEntityImport();
    }

    public boolean needsRelationOptionalEntityNextImport() {
        return hasOptionalRelation() && getLittleAdjustmentProperties().needsRelationOptionalEntityNextImport();
    }

    // -----------------------------------------------------
    //                                      Small Adjustment
    //                                      ----------------
    public boolean isCompatibleNewMyEntityConditionBean() {
        final DfLittleAdjustmentProperties prop = getLittleAdjustmentProperties();
        return prop.isCompatibleNewMyEntityConditionBean();
    }

    public boolean isCompatibleDeleteNonstrictIgnoreDeleted() {
        final DfLittleAdjustmentProperties prop = getLittleAdjustmentProperties();
        return prop.isCompatibleDeleteNonstrictIgnoreDeleted();
    }

    // ===================================================================================
    //                                                                   Entity Adjustment
    //                                                                   =================
    public boolean isMakeEntityChaseRelation() {
        return getLittleAdjustmentProperties().isMakeEntityChaseRelation();
    }

    public String getDerivedMappableDefinition() {
        if (isEntityDerivedMappable()) {
            final String delimiter = getLanguageGrammar().getImplementsDelimiter();
            return delimiter + DerivedMappable.class.getSimpleName();
        }
        return "";
    }

    public boolean isEntityDerivedMappable() {
        return getLittleAdjustmentProperties().isEntityDerivedMappable();
    }

    // ===================================================================================
    //                                                            ConditionBean Adjustment
    //                                                            ========================
    public boolean isCompatibleOrScopeQueryPurposeNoCheck() {
        final DfLittleAdjustmentProperties prop = getLittleAdjustmentProperties();
        return prop.isCompatibleOrScopeQueryPurposeNoCheck();
    }

    public boolean isCompatibleConditionBeanAcceptPKOldStyle() {
        final DfLittleAdjustmentProperties prop = getLittleAdjustmentProperties();
        return prop.isCompatibleConditionBeanAcceptPKOldStyle();
    }

    public boolean isCompatibleConditionBeanOldNamingCheckInvalid() {
        final DfLittleAdjustmentProperties prop = getLittleAdjustmentProperties();
        return prop.isCompatibleConditionBeanOldNamingCheckInvalid();
    }

    public boolean isCompatibleConditionBeanOldNamingOption() {
        final DfLittleAdjustmentProperties prop = getLittleAdjustmentProperties();
        return prop.isCompatibleConditionBeanOldNamingOption();
    }

    public boolean isCompatibleConditionBeanFromToOneSideAllowed() {
        final DfLittleAdjustmentProperties prop = getLittleAdjustmentProperties();
        return prop.isCompatibleConditionBeanFromToOneSideAllowed();
    }

    public boolean isMakeConditionQueryPlainListManualOrder() {
        return getLittleAdjustmentProperties().isMakeConditionQueryPlainListManualOrder();
    }

    // ===================================================================================
    //                                                               Adding Schema/Catalog
    //                                                               =====================
    protected boolean isAvailableAddingSchemaToTableSqlName() {
        return getLittleAdjustmentProperties().isAvailableAddingSchemaToTableSqlName();
    }

    protected boolean isAvailableAddingCatalogToTableSqlName() {
        return getLittleAdjustmentProperties().isAvailableAddingCatalogToTableSqlName();
    }

    // ===================================================================================
    //                                                                Convert Empty String
    //                                                                ====================
    public boolean hasEntityConvertEmptyStringToNull() {
        final List<Column> columnList = getColumnList();
        for (Column column : columnList) {
            if (column.isEntityConvertEmptyStringToNull()) {
                return true;
            }
        }
        return false;
    }

    // ===================================================================================
    //                                                              Relational Null Object
    //                                                              ======================
    public boolean canBeRelationalNullObjectForeign() {
        return getLittleAdjustmentProperties().hasRelationalNullObjectForeign(getTableDbName());
    }

    public String getRelationalNullObjectProviderForeignExp() {
        final DfLittleAdjustmentProperties prop = getLittleAdjustmentProperties();
        return prop.getRelationalNullObjectProviderForeignExp(getTableDbName());
    }

    public boolean hasRelationalNullObjectProviderImport() {
        final DfLittleAdjustmentProperties prop = getLittleAdjustmentProperties();
        final String providerPackage = prop.getNullObjectProviderPackage();
        return providerPackage != null && hasRelationalNullObjectForeignKey();
    }

    public String getRelationalNullObjectProviderPackage() {
        return getLittleAdjustmentProperties().getNullObjectProviderPackage();
    }

    protected boolean hasRelationalNullObjectForeignKey() {
        for (ForeignKey fk : getForeignKeyList()) {
            if (fk.getForeignTable().canBeRelationalNullObjectForeign()) {
                return true;
            }
        }
        for (ForeignKey referrer : getReferrerAsOneList()) {
            if (referrer.getTable().canBeRelationalNullObjectForeign()) {
                return true;
            }
        }
        return false;
    }

    // ===================================================================================
    //                                                                 CursorSelect Option
    //                                                                 ===================
    public boolean isCursorSelectOptionAllowed() {
        return getLittleAdjustmentProperties().isCursorSelectOptionAllowed();
    }

    // ===================================================================================
    //                                                                     Optional Object
    //                                                                     ===============
    // #thinking optional property
    //protected String getEntityOptionalPropertyClassName() {
    //    return getLittleAdjustmentProperties().getEntityOptionalPropertyClassName();
    //}
    //
    //protected String getEntityOptionalPropertyClassSimpleName() {
    //    return getLittleAdjustmentProperties().getEntityOptionalPropertyClassSimpleName();
    //}
    //
    //public boolean hasOptionalColumn() {
    //    final List<Column> columnList = getColumnList();
    //    for (Column column : columnList) {
    //        if (column.isOptionalProperty()) {
    //            return true;
    //        }
    //    }
    //    return false;
    //}
    //
    //public String toPropertyType(String columnName, String javaNative) {
    //    final Column column = getColumn(columnName);
    //    if (column == null) {
    //        String msg = "Not found the column in the table: column=" + columnName + ", table=" + getTableDbName();
    //        throw new IllegalArgumentException(msg);
    //    }
    //    if (column.isOptionalProperty()) {
    //        return column.buildOptionalExpression(javaNative);
    //    }
    //    return javaNative;
    //}

    // ===================================================================================
    //                                                                          Simple DTO
    //                                                                          ==========
    public String getSimpleDtoBaseDtoClassName() {
        final DfSimpleDtoProperties prop = getProperties().getSimpleDtoProperties();
        final String prefix = prop.getBaseDtoPrefix();
        final String suffix = prop.getBaseDtoSuffix();
        return prefix + getJavaName() + suffix;
    }

    public String getSimpleDtoExtendedDtoClassName() {
        final DfSimpleDtoProperties prop = getProperties().getSimpleDtoProperties();
        final String prefix = prop.getExtendedDtoPrefix();
        final String suffix = prop.getExtendedDtoSuffix();
        return prefix + getJavaName() + suffix;
    }

    public String getSimpleDtoBaseMapperClassName() {
        final DfSimpleDtoProperties prop = getProperties().getSimpleDtoProperties();
        return getSimpleDtoBaseDtoClassName() + prop.getMapperSuffix();
    }

    public String getSimpleDtoExtendedMapperClassName() {
        final DfSimpleDtoProperties prop = getProperties().getSimpleDtoProperties();
        return getSimpleDtoExtendedDtoClassName() + prop.getMapperSuffix();
    }

    // -----------------------------------------------------
    //                                     JSONIC Decoration
    //                                     -----------------
    public boolean hasSimpleDtoJsonicDecoration() {
        final List<Column> columnList = getColumnList();
        for (Column column : columnList) {
            if (column.hasSimpleDtoJsonicDecoration()) {
                return true;
            }
        }
        return false;
    }

    // -----------------------------------------------------
    //                             JsonPullParser Decoration
    //                             -------------------------
    public boolean hasSimpleDtoJsonPullParserDecoration() {
        final List<Column> columnList = getColumnList();
        for (Column column : columnList) {
            if (column.hasSimpleDtoJsonPullParserDecoration()) {
                return true;
            }
        }
        return false;
    }

    // -----------------------------------------------------
    //                                    Jackson Decoration
    //                                    ------------------
    public boolean hasSimpleDtoJacksonDecoration() {
        final List<Column> columnList = getColumnList();
        for (Column column : columnList) {
            if (column.hasSimpleDtoJacksonDecoration()) {
                return true;
            }
        }
        return false;
    }

    // ===================================================================================
    //                                                                            FLEX DTO
    //                                                                            ========
    public boolean isFlexDtoBindable() {
        return getProperties().getFlexDtoProperties().isBindable(getTableDbName());
    }

    // ===================================================================================
    //                                                                          Value Type
    //                                                                          ==========
    public boolean needsMappingValueType() {
        final List<Column> columnList = getColumnList();
        for (Column column : columnList) {
            if (column.needsMappingValueType()) {
                return true;
            }
        }
        return false;
    }

    // ===================================================================================
    //                                                                     Behavior Filter
    //                                                                     ===============
    public boolean hasBehaviorFilterBeforeColumn() {
        try {
            return hasBehaviorFilterBeforeInsertColumn() || hasBehaviorFilterBeforeUpdateColumn();
        } catch (RuntimeException e) {
            _log.debug("Failed to execute 'Table.hasBehaviorFilterBeforeColumn()'!", e);
            throw e;
        }
    }

    protected List<Column> _behaviorFilterBeforeInsertColumnList;

    public boolean hasBehaviorFilterBeforeInsertColumn() {
        return !getBehaviorFilterBeforeInsertColumnList().isEmpty();
    }

    public List<Column> getBehaviorFilterBeforeInsertColumnList() {
        if (_behaviorFilterBeforeInsertColumnList != null) {
            return _behaviorFilterBeforeInsertColumnList;
        }
        final DfBehaviorFilterProperties prop = getProperties().getBehaviorFilterProperties();
        final Map<String, Object> map = prop.getBeforeInsertMap();
        final Set<String> columnNameSet = map.keySet();
        _behaviorFilterBeforeInsertColumnList = new ArrayList<Column>();
        final Set<String> commonColumnNameSet = new HashSet<String>();
        if (hasAllCommonColumn()) {
            final List<Column> commonColumnList = getCommonColumnList();
            for (Column commonColumn : commonColumnList) {
                commonColumnNameSet.add(commonColumn.getName());
            }
        }
        for (String columnName : columnNameSet) {
            Column column = getColumn(columnName);
            if (column != null && !commonColumnNameSet.contains(columnName)) {
                _behaviorFilterBeforeInsertColumnList.add(column);
                String expression = (String) map.get(columnName);
                if (expression == null || expression.trim().length() == 0) {
                    String msg = "The value expression was not found in beforeInsertMap: column=" + column;
                    throw new IllegalStateException(msg);
                }
                column.setBehaviorFilterBeforeInsertColumnExpression(expression);
            }
        }
        return _behaviorFilterBeforeInsertColumnList;
    }

    public String getBehaviorFilterBeforeInsertColumnExpression(String columName) {
        DfBehaviorFilterProperties prop = getProperties().getBehaviorFilterProperties();
        Map<String, Object> map = prop.getBeforeInsertMap();
        return (String) map.get(columName);
    }

    protected List<Column> _behaviorFilterBeforeUpdateColumnList;

    public boolean hasBehaviorFilterBeforeUpdateColumn() {
        return !getBehaviorFilterBeforeUpdateColumnList().isEmpty();
    }

    public List<Column> getBehaviorFilterBeforeUpdateColumnList() {
        if (_behaviorFilterBeforeUpdateColumnList != null) {
            return _behaviorFilterBeforeUpdateColumnList;
        }
        DfBehaviorFilterProperties prop = getProperties().getBehaviorFilterProperties();
        Map<String, Object> map = prop.getBeforeUpdateMap();
        Set<String> columnNameSet = map.keySet();
        _behaviorFilterBeforeUpdateColumnList = new ArrayList<Column>();
        Set<String> commonColumnNameSet = new HashSet<String>();
        if (hasAllCommonColumn()) {
            List<Column> commonColumnList = getCommonColumnList();
            for (Column commonColumn : commonColumnList) {
                commonColumnNameSet.add(commonColumn.getName());
            }
        }
        for (String columnName : columnNameSet) {
            Column column = getColumn(columnName);
            if (column != null && !commonColumnNameSet.contains(columnName)) {
                _behaviorFilterBeforeUpdateColumnList.add(column);
                String expression = (String) map.get(columnName);
                if (expression == null || expression.trim().length() == 0) {
                    String msg = "The value expression was not found in beforeUpdateMap: column=" + column;
                    throw new IllegalStateException(msg);
                }
                column.setBehaviorFilterBeforeUpdateColumnExpression(expression);
            }
        }
        return _behaviorFilterBeforeUpdateColumnList;
    }

    // ===================================================================================
    //                                                                 Behavior Query Path
    //                                                                 ===================
    protected Map<String, Map<String, String>> getBehaviorQueryPathMap() {
        final Map<String, Map<String, Map<String, String>>> tableBqpMap = getDatabase().getTableBqpMap();
        final Map<String, Map<String, String>> elementMap = tableBqpMap.get(getName());
        return elementMap != null ? elementMap : new HashMap<String, Map<String, String>>();
    }

    public boolean hasBehaviorQueryPath() {
        return !getBehaviorQueryPathList().isEmpty();
    }

    public List<String> getBehaviorQueryPathList() {
        final Map<String, Map<String, String>> bqpMap = getBehaviorQueryPathMap();
        return new ArrayList<String>(bqpMap.keySet());
    }

    protected Map<String, String> getBehaviorQueryPathElementMap(String behaviorQueryPath) {
        final Map<String, Map<String, String>> bqpMap = getBehaviorQueryPathMap();
        return bqpMap.get(behaviorQueryPath);
    }

    public String getBehaviorQueryPathDisplayName(String behaviorQueryPath) {
        final String subDirectoryPath = getBehaviorQueryPathSubDirectoryPath(behaviorQueryPath);
        if (Srl.is_NotNull_and_NotTrimmedEmpty(subDirectoryPath)) {
            final String connector = "_";
            return Srl.replace(subDirectoryPath, "/", connector) + connector + behaviorQueryPath;
        } else {
            return behaviorQueryPath;
        }
    }

    public String getBehaviorQueryPathFileName(String behaviorQueryPath) {
        final String path = getBehaviorQueryPathPath(behaviorQueryPath);
        if (Srl.is_NotNull_and_NotTrimmedEmpty(path)) {
            final int fileNameIndex = path.lastIndexOf("/");
            if (fileNameIndex >= 0) {
                return path.substring(fileNameIndex + "/".length());
            } else {
                return path;
            }
        } else {
            return "";
        }
    }

    public String getBehaviorQueryPathSubDirectoryPath(String behaviorQueryPath) {
        final Map<String, String> elementMap = getBehaviorQueryPathElementMap(behaviorQueryPath);
        final String subDirectoryPath = elementMap.get("subDirectoryPath");
        return Srl.is_NotNull_and_NotTrimmedEmpty(subDirectoryPath) ? subDirectoryPath : "";
    }

    public String getBehaviorQueryPathPath(String behaviorQueryPath) {
        final Map<String, String> elementMap = getBehaviorQueryPathElementMap(behaviorQueryPath);
        final String path = elementMap.get(DfBehaviorQueryPathSetupper.KEY_PATH);
        return Srl.is_NotNull_and_NotTrimmedEmpty(path) ? path : "";
    }

    public boolean hasBehaviorQueryPathCustomizeEntity(String behaviorQueryPath) {
        return Srl.is_NotNull_and_NotTrimmedEmpty(getBehaviorQueryPathCustomizeEntity(behaviorQueryPath));
    }

    public String getBehaviorQueryPathCustomizeEntity(String behaviorQueryPath) {
        final Map<String, String> elementMap = getBehaviorQueryPathElementMap(behaviorQueryPath);
        final String customizeEntity = elementMap.get("customizeEntity");
        return Srl.is_NotNull_and_NotTrimmedEmpty(customizeEntity) ? customizeEntity : "";
    }

    public boolean hasBehaviorQueryPathParameterBean(String behaviorQueryPath) {
        return Srl.is_NotNull_and_NotTrimmedEmpty(getBehaviorQueryPathParameterBean(behaviorQueryPath));
    }

    public String getBehaviorQueryPathParameterBean(String behaviorQueryPath) {
        final Map<String, String> elementMap = getBehaviorQueryPathElementMap(behaviorQueryPath);
        final String parameterBean = elementMap.get("parameterBean");
        return Srl.is_NotNull_and_NotTrimmedEmpty(parameterBean) ? parameterBean : "";
    }

    public boolean hasBehaviorQueryPathCursor(String behaviorQueryPath) {
        return Srl.is_NotNull_and_NotTrimmedEmpty(getBehaviorQueryPathCursor(behaviorQueryPath));
    }

    public String getBehaviorQueryPathCursor(String behaviorQueryPath) {
        final Map<String, String> elementMap = getBehaviorQueryPathElementMap(behaviorQueryPath);
        final String cursor = elementMap.get("cursor");
        return Srl.is_NotNull_and_NotTrimmedEmpty(cursor) ? cursor : "";
    }

    public String getBehaviorQueryPathCursorForSchemaHtml(String behaviorQueryPath) {
        final String cursor = getBehaviorQueryPathCursor(behaviorQueryPath);
        return Srl.is_NotNull_and_NotTrimmedEmpty(cursor) ? " *" + cursor : "";
    }

    public String getBehaviorQueryPathTitle(String behaviorQueryPath) {
        final Map<String, String> elementMap = getBehaviorQueryPathElementMap(behaviorQueryPath);
        final String title = elementMap.get("title");
        return Srl.is_NotNull_and_NotTrimmedEmpty(title) ? title : "";
    }

    public String getBehaviorQueryPathTitleForSchemaHtml(String behaviorQueryPath) {
        String title = getBehaviorQueryPathTitle(behaviorQueryPath);
        if (Srl.is_NotNull_and_NotTrimmedEmpty(title)) {
            final DfDocumentProperties prop = getProperties().getDocumentProperties();
            title = prop.resolveTextForSchemaHtml(title);
            return "(" + title + ")";
        } else {
            return "&nbsp;";
        }
    }

    public boolean hasBehaviorQueryPathDescription(String behaviorQueryPath) {
        return Srl.is_NotNull_and_NotTrimmedEmpty(getBehaviorQueryPathDescription(behaviorQueryPath));
    }

    public String getBehaviorQueryPathDescription(String behaviorQueryPath) {
        final Map<String, String> elementMap = getBehaviorQueryPathElementMap(behaviorQueryPath);
        final String description = elementMap.get("description");
        return Srl.is_NotNull_and_NotTrimmedEmpty(description) ? description : "";
    }

    public String getBehaviorQueryPathDescriptionForSchemaHtml(String behaviorQueryPath) {
        String description = getBehaviorQueryPathDescription(behaviorQueryPath);
        if (Srl.is_NotNull_and_NotTrimmedEmpty(description)) {
            final DfDocumentProperties prop = getProperties().getDocumentProperties();
            description = prop.resolvePreTextForSchemaHtml(description);
            return description;
        } else {
            return "&nbsp;";
        }
    }

    public boolean isBehaviorQueryPathSqlAp(String behaviorQueryPath) {
        final Map<String, String> elementMap = getBehaviorQueryPathElementMap(behaviorQueryPath);
        final String sqlAp = elementMap.get(DfBehaviorQueryPathSetupper.KEY_SQLAP);
        return Srl.is_NotNull_and_NotTrimmedEmpty(sqlAp) ? "true".equals(sqlAp) : false;
    }

    public String getBehaviorQueryPathSqlApProjectName(String behaviorQueryPath) {
        final Map<String, String> elementMap = getBehaviorQueryPathElementMap(behaviorQueryPath);
        final String sqlApProjectName = elementMap.get(DfBehaviorQueryPathSetupper.KEY_SQLAP_PROJECT_NAME);
        return Srl.is_NotNull_and_NotTrimmedEmpty(sqlApProjectName) ? sqlApProjectName : "";
    }

    // This method is not necessary because sql2entity cannot use this.
    //public List<String> getBehaviorQueryPathDefinitionList() {
    //}

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    // *not override equals() because table comparing process is so complex
    /**
     * Returns a XML representation of this table.
     * @return XML representation of this table
     */
    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append("<table name=\"").append(getTableDbName()).append('\"');
        if (_javaName != null) {
            result.append(" javaName=\"").append(_javaName).append('\"');
        }
        result.append(">\n");
        if (_columnList != null) {
            for (Iterator<Column> iter = _columnList.iterator(); iter.hasNext();) {
                result.append(iter.next());
            }
        }
        final List<ForeignKey> foreignKeyList = getForeignKeyList();
        if (!foreignKeyList.isEmpty()) {
            for (ForeignKey fk : foreignKeyList) {
                result.append(fk);
            }
        }
        result.append("</table>\n");
        return result.toString();
    }

    // ===================================================================================
    //                                                                             Unknown
    //                                                                             =======
    /**
     * <p>A hook for the SAX XML parser to call when this table has
     * been fully loaded from the XML, and all nested elements have
     * been processed.</p>
     * <p>Performs heavy indexing and naming of elements which weren't
     * provided with a name.</p>
     */
    public void doFinalInitialization() {
        // Name any indices which are missing a name using the
        // appropriate algorithm.
        doNaming();
    }

    /**
     * Names composing objects which haven't yet been named.  This
     * currently consists of foreign-key and index entities.
     */
    private void doNaming() {
        int i;
        int size;
        String name;

        // Assure names are unique across all databases.
        try {
            final List<ForeignKey> foreignKeyList = getForeignKeyList();
            for (i = 0, size = foreignKeyList.size(); i < size; i++) {
                final ForeignKey fk = (ForeignKey) foreignKeyList.get(i);
                name = fk.getName();
                if (Srl.is_Null_or_Empty(name)) {
                    name = acquireConstraintName("FK", i + 1);
                    fk.setName(name);
                }
            }

            for (i = 0, size = _indices.size(); i < size; i++) {
                Index index = (Index) _indices.get(i);
                name = index.getName();
                if (Srl.is_Null_or_Empty(name)) {
                    name = acquireConstraintName("I", i + 1);
                    index.setName(name);
                }
            }

            // NOTE: Most RDBMSes can apparently name unique column
            // constraints/indices themselves (using MySQL and Oracle
            // as test cases), so we'll assume that we needn't add an
            // entry to the system name list for these.
        } catch (EngineException nameAlreadyInUse) {
            _log.error(nameAlreadyInUse, nameAlreadyInUse);
        }
    }

    /**
     * Macro to a constraint name.
     * @param nameType constraint type
     * @param nbr unique number for this constraint type
     * @return unique name for constraint
     * @throws EngineException
     */
    private final String acquireConstraintName(String nameType, int nbr) throws EngineException {
        final List<Object> inputs = new ArrayList<Object>(4);
        inputs.add(getDatabase());
        inputs.add(getName());
        inputs.add(nameType);
        inputs.add(new Integer(nbr));
        return NameFactory.generateName(NameFactory.CONSTRAINT_GENERATOR, inputs);
    }
}