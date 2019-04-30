/*
 * Copyright 2014-2019 the original author or authors.
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.dbflute.DfBuildProperties;
import org.dbflute.exception.DfTableNotFoundException;
import org.dbflute.helper.StringKeyMap;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.logic.generate.column.DfColumnListToStringBuilder;
import org.dbflute.logic.generate.foreignkey.DfFixedConditionDynamicAnalyzer;
import org.dbflute.logic.generate.foreignkey.DfFixedConditionOptionChecker;
import org.dbflute.logic.generate.language.grammar.DfLanguageGrammar;
import org.dbflute.properties.DfBasicProperties;
import org.dbflute.properties.DfClassificationProperties;
import org.dbflute.properties.DfDocumentProperties;
import org.dbflute.properties.DfIncludeQueryProperties;
import org.dbflute.properties.DfLittleAdjustmentProperties;
import org.dbflute.properties.DfMultipleFKPropertyProperties;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.Srl;
import org.xml.sax.Attributes;

/**
 * A class for information about foreign keys of a table.
 * @author modified by jflute (originated in Apache Torque)
 */
public class ForeignKey implements Constraint {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                                 Basic
    //                                                 -----
    protected String _name; // constraint name (no change because it's used by templates)
    protected Table _localTable;
    protected UnifiedSchema _foreignSchema;
    protected String _foreignTablePureName; // as table DB name, may be user input (if additional FK)

    // -----------------------------------------------------
    //                                            Additional
    //                                            ----------
    protected boolean _additionalForeignKey;
    protected String _fixedCondition;
    protected String _fixedSuffix;
    protected boolean _fixedInline;
    protected boolean _fixedReferrer;
    protected boolean _fixedOnlyJoin;
    protected String _comment;
    protected boolean _suppressJoin;
    protected boolean _suppressSubQuery;
    protected String _deprecated;
    protected String _foreignPropertyNamePrefix;
    protected boolean _implicitReverseForeignKey;

    // -----------------------------------------------------
    //                                                Column
    //                                                ------
    protected List<Column> _localColumnList; // lazy-loaded
    protected List<Column> _foreignColumnList; // lazy-loaded
    protected final List<String> _localColumnNameList = new ArrayList<String>(3);
    protected final List<String> _foreignColumnNameList = new ArrayList<String>(3);

    // -----------------------------------------------------
    //                                               Mapping
    //                                               -------
    protected final Map<String, String> _localForeignMap = StringKeyMap.createAsFlexibleOrdered();
    protected final Map<String, String> _foreignLocalMap = StringKeyMap.createAsFlexibleOrdered();
    protected final Map<String, String> _dynamicFixedConditionMap = DfCollectionUtil.newLinkedHashMap();

    // -----------------------------------------------------
    //                                    Lazy Determination
    //                                    ------------------
    protected Boolean _oneToOne;
    protected Boolean _bizOneToOne;
    protected Boolean _bizManyToOne;

    // ===================================================================================
    //                                                                       Load from XML
    //                                                                       =============
    /**
     * Imports foreign key from an XML specification
     * @param localTable The local table to provide its schema. (NotNull)
     * @param attrib the XML attributes
     */
    public void loadFromXML(Table localTable, Attributes attrib) {
        _foreignTablePureName = attrib.getValue("foreignTable");
        final String schemaExp = attrib.getValue("foreignSchema");
        if (schemaExp != null) {
            _foreignSchema = UnifiedSchema.createAsDynamicSchema(schemaExp);
        } else { // for compatible
            _foreignSchema = localTable.getUnifiedSchema();
        }
        _name = attrib.getValue("name"); // constraint name for this FK
    }

    /**
     * Adds a new reference entry to the foreign key
     * @param attrib the xml attributes
     */
    public void addReference(Attributes attrib) {
        final String localColumn = attrib.getValue("local");
        final String foreignColumn = attrib.getValue("foreign");
        addReference(localColumn, foreignColumn);
    }

    /**
     * Adds a new reference entry to the foreign key
     * @param local name of the local column
     * @param foreign name of the foreign column
     */
    public void addReference(String local, String foreign) {
        _localColumnNameList.add(local);
        _foreignColumnNameList.add(foreign);
        _localForeignMap.put(local, foreign);
        _foreignLocalMap.put(foreign, local);
    }

    /**
     * Adds a new reference entry to the foreign key
     * @param localColumnNameList Name list  of the local column
     * @param foreignColumnNameList Name list of the foreign column
     */
    public void addReference(List<String> localColumnNameList, List<String> foreignColumnNameList) {
        _localColumnNameList.addAll(localColumnNameList);
        _foreignColumnNameList.addAll(foreignColumnNameList);
        for (int i = 0; i < localColumnNameList.size(); i++) {
            _localForeignMap.put(localColumnNameList.get(i), foreignColumnNameList.get(i));
            _foreignLocalMap.put(foreignColumnNameList.get(i), localColumnNameList.get(i));
        }
    }

    // ===================================================================================
    //                                                                               Table
    //                                                                               =====
    /**
     * Get foreign table.
     * @return Foreign table.
     * @throws DfTableNotFoundException When the foreign table is not found.
     */
    public Table getForeignTable() {
        final String foreignTableSearchName = getForeignTableSearchName();
        final Table foreignTable = findTable(foreignTableSearchName);
        if (foreignTable == null) {
            throwForeignTableNotFoundException(foreignTableSearchName);
        }
        return foreignTable;
    }

    protected String getForeignTableSearchName() {
        final String foreignTableName = getForeignTablePureName();
        if (_foreignSchema == null) {
            return foreignTableName;
        }
        return _foreignSchema.getCatalogSchema() + "." + foreignTableName;
    }

    protected Table findTable(String tableDbName) {
        return getTable().getDatabase().getTable(tableDbName);
    }

    public String getForeignTableDbName() {
        return getForeignTable().getTableDbName();
    }

    protected void throwForeignTableNotFoundException(String foreignTableName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the foreign table in the database.");
        br.addItem("Foreign Key");
        br.addElement(_name);
        br.addItem("Local Table");
        br.addElement(getTable().getBasicInfoDispString());
        br.addItem("ForeignTable Name");
        br.addElement(foreignTableName);
        final String msg = br.buildExceptionMessage();
        throw new DfTableNotFoundException(msg);
    }

    // ===================================================================================
    //                                                                    Foreign Property
    //                                                                    ================
    // -----------------------------------------------------
    //                                                 Basic
    //                                                 -----
    /**
     * Get the value of foreign property name.
     * @return Generated string.
     */
    public String getForeignPropertyName() {
        return getForeignPropertyName(false);
    }

    /**
     * Get the value of foreign property name.
     * @return Generated string.
     */
    public String getForeignJavaBeansRulePropertyName() {
        return getForeignPropertyName(true);
    }

    /**
     * Get the value of foreign property name.
     * @return Generated string.
     */
    public String getForeignJavaBeansRulePropertyNameInitCap() {
        return initCap(getForeignPropertyName(true));
    }

    /**
     * Get the value of foreign property name.
     * @param isJavaBeansRule Is it java-beans rule?
     * @return Generated string.
     */
    protected String getForeignPropertyName(boolean isJavaBeansRule) {
        String name = "";
        name = buildForeignPropertySubIdentityName(name);
        name = buildForeignPropertySelfRelationName(name);
        name = buildForeignPropertyTableIdentityName(isJavaBeansRule, name);
        name = buildForeignPropertySpecifiedPrefixName(name);
        return name;
    }

    protected String buildForeignPropertySubIdentityName(String name) {
        final List<Column> localColumnList = getLocalColumnList();
        if (hasFixedSuffix()) {
            return getFixedSuffix();
        }
        final List<String> multipleFKColumnNameList = new ArrayList<String>();
        for (final Iterator<Column> ite = localColumnList.iterator(); ite.hasNext();) {
            final Column col = (Column) ite.next();
            if (col.isMultipleFK()) { // the bug exists (refer to the method's source code)
                multipleFKColumnNameList.add(col.getName());
                name = name + col.getJavaName();
            }
        }
        if (name.trim().length() > 0) { // means multiple FK columns exist
            final String localTableDbName = getTable().getTableDbName();
            final String aliasName = getMultipleFKPropertyColumnAliasName(localTableDbName, multipleFKColumnNameList);
            if (aliasName != null && aliasName.trim().length() > 0) { // my young code
                final String firstUpper = aliasName.substring(0, 1).toUpperCase();
                if (aliasName.trim().length() == 1) {
                    name = "By" + firstUpper;
                } else {
                    name = "By" + firstUpper + aliasName.substring(1, aliasName.length());
                }
            } else {
                name = "By" + name;
            }
        }
        return name;
    }

    protected String buildForeignPropertySelfRelationName(String name) {
        if (getForeignTable().getTableDbName().equals(getTable().getTableDbName())) {
            name = name + "Self";
        }
        return name;
    }

    protected String buildForeignPropertyTableIdentityName(boolean isJavaBeansRule, String name) {
        if (isJavaBeansRule) {
            name = getForeignTable().getJavaBeansRulePropertyName() + name;
        } else {
            name = getForeignTable().getUncapitalisedJavaName() + name;
        }
        return name;
    }

    protected String buildForeignPropertySpecifiedPrefixName(String name) {
        if (_foreignPropertyNamePrefix != null) {
            name = _foreignPropertyNamePrefix + name;
        }
        return name;
    }

    protected String getMultipleFKPropertyColumnAliasName(String tableName, List<String> multipleFKColumnNameList) {
        final DfMultipleFKPropertyProperties prop = DfBuildProperties.getInstance().getMultipleFKPropertyProperties();
        final String columnAliasName = prop.getMultipleFKPropertyColumnAliasName(tableName, multipleFKColumnNameList);
        return columnAliasName;
    }

    public String getForeignPropertyNameInitCap() {
        final String foreignPropertyName = getForeignPropertyName();
        return foreignPropertyName.substring(0, 1).toUpperCase() + foreignPropertyName.substring(1);
    }

    // -----------------------------------------------------
    //                              Relation Optional Entity
    //                              ------------------------
    public boolean isForeignPropertyOptionalEntity() {
        return getRelationOptionalEntityClassName() != null;
    }

    public String getForeignPropertyEntityDefinitionType() {
        final String extendedEntityClassName = getForeignTableExtendedEntityClassName();
        final String propertyAccessType = getRelationOptionalEntityClassName();
        return doGetForeignPropertyEntityDefinitionType(extendedEntityClassName, propertyAccessType);
    }

    public String getForeignPropertyImmutableEntityDefinitionType() {
        final String extendedEntityClassName = getForeignTableImmutableExtendedEntityClassName();
        final String propertyAccessType = getRelationOptionalEntityClassName();
        return doGetForeignPropertyEntityDefinitionType(extendedEntityClassName, propertyAccessType);
    }

    public boolean isReferrerPropertyOptionalEntityAsOne() {
        return getRelationOptionalEntityClassName() != null;
    }

    public String getReferrerPropertyEntityDefinitionTypeAsOne() {
        final String extendedEntityClassName = getReferrerTableExtendedEntityClassName();
        final String propertyAccessType = getRelationOptionalEntityClassName();
        return doGetForeignPropertyEntityDefinitionType(extendedEntityClassName, propertyAccessType);
    }

    public String getReferrerPropertyImmutableEntityDefinitionTypeAsOne() {
        final String extendedEntityClassName = getReferrerTableImmutableExtendedEntityClassName();
        final String propertyAccessType = getRelationOptionalEntityClassName();
        return doGetForeignPropertyEntityDefinitionType(extendedEntityClassName, propertyAccessType);
    }

    protected String doGetForeignPropertyEntityDefinitionType(String extendedEntityClassName, String propertyAccessType) {
        if (propertyAccessType != null) {
            final String simpleName = Srl.substringLastRear(propertyAccessType, "."); // needs import definition
            final DfLanguageGrammar grammar = getBasicProperties().getLanguageDependency().getLanguageGrammar();
            return simpleName + grammar.buildGenericOneClassHint(extendedEntityClassName);
        }
        return extendedEntityClassName;
    }

    protected String getRelationOptionalEntityClassName() {
        final DfLittleAdjustmentProperties prop = getLittleAdjustmentProperties();
        return !prop.isAvailableRelationPlainEntity() ? prop.getRelationOptionalEntityClass() : null;
    }

    // -----------------------------------------------------
    //                                  Property Access Type
    //                                  --------------------
    public String getForeignPropertyAccessTypeMetaLiteral() {
        return doGetRelationPropertyAccessTypeMetaLiteral();
    }

    public String getReferrerPropertyAccessTypeMetaLiteralAsOne() {
        return doGetRelationPropertyAccessTypeMetaLiteral();
    }

    protected String doGetRelationPropertyAccessTypeMetaLiteral() {
        if (getRelationOptionalEntityClassName() != null) {
            final String className = getLittleAdjustmentProperties().getRelationOptionalEntityClass();
            final DfLanguageGrammar grammar = getBasicProperties().getLanguageDependency().getLanguageGrammar();
            return grammar.buildClassTypeLiteral(className);
        } else {
            return "null"; // means that it uses default type (relation entity type)
        }
    }

    // -----------------------------------------------------
    //                                       Foreign Reverse
    //                                       ---------------
    public String getForeignReverseRelationPropertyName() {
        if (canBeReferrer()) {
            if (isOneToOne()) {
                return getReferrerJavaBeansRulePropertyNameAsOne();
            } else {
                return getReferrerJavaBeansRulePropertyName();
            }
        } else {
            return null;
        }
    }

    public String getForeignReverseRelationPropertyNameArg() {
        final String propertyName = getForeignReverseRelationPropertyName();
        return propertyName != null ? "\"" + propertyName + "\"" : "null";
    }

    public String getForeignReverseRelationPropertyNameInitCap() {
        if (canBeReferrer()) {
            if (isOneToOne()) {
                return getReferrerJavaBeansRulePropertyNameAsOneInitCap();
            } else {
                return getReferrerJavaBeansRulePropertyNameInitCap();
            }
        } else {
            return null;
        }
    }

    public String getForeignReverseRelationPropertyNameInitCapArg() {
        final String propertyName = getForeignReverseRelationPropertyNameInitCap();
        return propertyName != null ? "\"" + propertyName + "\"" : "null";
    }

    // -----------------------------------------------------
    //                                     Lambda Short Name
    //                                     -----------------
    public String getForeignLambdaShortName() {
        return getTable().getLambdaShortName();
    }

    public String getForeignLambdaSubQueryCBName() {
        return "subCBLambda"; // not use short name for fixed trick
    }

    // ===================================================================================
    //                                                                   Referrer Property
    //                                                                   =================
    // -----------------------------------------------------
    //                                                 Basic
    //                                                 -----
    public String getReferrerPropertyName() {
        return getReferrerPropertyName(false, getReferrerPropertyListSuffix());
    }

    public String getReferrerJavaBeansRulePropertyName() {
        return getReferrerPropertyName(true, getReferrerPropertyListSuffix());
    }

    public String getReferrerJavaBeansRulePropertyNameInitCap() {
        return initCap(getReferrerPropertyName(true, getReferrerPropertyListSuffix()));
    }

    public String getReferrerConditionMethodIdentityName() {
        final DfLittleAdjustmentProperties prop = getLittleAdjustmentProperties();
        final boolean hasListSufix = prop.isCompatibleReferrerCBMethodIdentityNameListSuffix();
        final String suffix = hasListSufix ? "List" : "";
        return initCap(getReferrerPropertyName(true, suffix));
    }

    protected String getReferrerPropertyListSuffix() {
        return "List";
    }

    protected String getReferrerPropertyName(boolean isJavaBeansRule, String listSuffix) {
        final String firstName = buildReferrerPropertyTableIdentityName(isJavaBeansRule);
        final String subIdentityName = buildReferrerPropertySubIdentityName();
        return firstName + subIdentityName + listSuffix;
    }

    protected String buildReferrerPropertyTableIdentityName(boolean isJavaBeansRule) {
        final String tableIdentityName;
        if (isJavaBeansRule) {
            tableIdentityName = getTable().getJavaBeansRulePropertyName();
        } else {
            tableIdentityName = getTable().getUncapitalisedJavaName();
        }
        return tableIdentityName;
    }

    protected String buildReferrerPropertySubIdentityName() {
        final List<Column> localColumnList = getLocalColumnList();
        final List<String> columnNameList = new ArrayList<String>();
        String secondName = "";
        if (hasFixedSuffix()) {
            secondName = getFixedSuffix();
        } else {
            for (final Iterator<Column> ite = localColumnList.iterator(); ite.hasNext();) {
                final Column col = (Column) ite.next();
                if (col.isMultipleFK()) {
                    columnNameList.add(col.getName());
                    secondName = secondName + col.getJavaName();
                }
            }
            if (secondName.trim().length() != 0) { // isMultipleFK()==true
                final String foreignTableDbName = getForeignTable().getTableDbName();
                final String aliasName = getMultipleFKPropertyColumnAliasName(foreignTableDbName, columnNameList);
                if (aliasName != null && aliasName.trim().length() != 0) {
                    final String firstUpper = aliasName.substring(0, 1).toUpperCase();
                    if (aliasName.trim().length() == 1) {
                        secondName = "By" + firstUpper;
                    } else {
                        secondName = "By" + firstUpper + aliasName.substring(1, aliasName.length());
                    }
                } else {
                    secondName = "By" + secondName;
                }
            }
        }
        if (getTable().getTableDbName().equals(getForeignTable().getTableDbName())) {
            secondName = secondName + "Self";
        }
        return secondName;
    }

    // -----------------------------------------------------
    //                                                as One
    //                                                ------
    public String getReferrerPropertyNameAsOne() {
        return getReferrerPropertyNameAsOne(false);
    }

    public String getReferrerPropertyNameInitCapAsOne() {
        return initCap(getReferrerPropertyNameAsOne());
    }

    public String getReferrerPropertyNameAsOneInitCap() { // for compatible
        return getReferrerPropertyNameInitCapAsOne();
    }

    public String getReferrerJavaBeansRulePropertyNameAsOne() {
        return getReferrerPropertyNameAsOne(true);
    }

    public String getReferrerJavaBeansRulePropertyNameInitCapAsOne() {
        return initCap(getReferrerPropertyNameAsOne(true));
    }

    public String getReferrerJavaBeansRulePropertyNameAsOneInitCap() { // for compatible
        return getReferrerJavaBeansRulePropertyNameInitCapAsOne();
    }

    protected String getReferrerPropertyNameAsOne(boolean isJavaBeansRule) {
        final String firstName = buildReferrerPropertyTableIdentityName(isJavaBeansRule);
        final String secondName = buildReferrerPropertySubIdentityNameAsOne();
        final String asOneSuffix = "AsOne";
        return firstName + secondName + asOneSuffix;
    }

    protected String buildReferrerPropertySubIdentityNameAsOne() {
        final List<Column> localColumnList = getLocalColumnList();
        String subIdentityName = "";
        for (final Iterator<Column> ite = localColumnList.iterator(); ite.hasNext();) {
            final Column col = (Column) ite.next();
            if (col.isMultipleFK()) {
                subIdentityName = subIdentityName + col.getJavaName();
            }
        }
        if (subIdentityName.trim().length() != 0) {
            subIdentityName = "By" + subIdentityName;
        }
        if (getTable().getTableDbName().equals(getForeignTable().getTableDbName())) {
            subIdentityName = subIdentityName + "Self";
        }
        return subIdentityName;
    }

    public String getReferrerPropertyNameInitCap() {
        final String referrerPropertyName = getReferrerPropertyName();
        return referrerPropertyName.substring(0, 1).toUpperCase() + referrerPropertyName.substring(1);
    }

    // -----------------------------------------------------
    //                                      Referrer Reverse
    //                                      ----------------
    public String getReferrerReverseRelationPropertyName() {
        return getForeignJavaBeansRulePropertyName();
    }

    public String getReferrerReverseRelationPropertyNameArg() {
        final String propertyName = getForeignJavaBeansRulePropertyName();
        return "\"" + propertyName + "\"";
    }

    // -----------------------------------------------------
    //                                     Lambda Short Name
    //                                     -----------------
    public String getReferrerLambdaShortName() {
        return getTable().getLambdaShortName();
    }

    public String getReferrerLambdaExampleCBName() {
        return getReferrerLambdaShortName() + "CB";
    }

    public String getReferrerLambdaSubQueryCBName() {
        return "subCBLambda"; // not use short name for fixed trick
    }

    // ===================================================================================
    //                                                                              Column
    //                                                                              ======
    // -----------------------------------------------------
    //                                                 Local
    //                                                 -----
    public List<Column> getLocalColumnList() {
        if (_localColumnList != null) {
            return _localColumnList;
        }
        final List<String> columnList = getLocalColumnNameList();
        if (isFixedOnlyJoin()) {
            if (columnList != null && !columnList.isEmpty()) {
                String msg = "The list of local column should be null or empty if fixedOnlyJoin: " + columnList;
                throw new IllegalStateException(msg);
            }
        } else { // normally here
            if (columnList == null || columnList.isEmpty()) {
                String msg = "The list of local column is null or empty: " + columnList;
                throw new IllegalStateException(msg);
            }
        }
        final List<Column> resultList = new ArrayList<Column>();
        for (final Iterator<String> ite = columnList.iterator(); ite.hasNext();) {
            final String name = (String) ite.next();
            final Column col = getTable().getColumn(name);
            if (col == null) {
                String msg = "The columnName is not existing at the table: ";
                msg = msg + "columnName=" + name + " tableName=" + getTable().getTableDbName();
                throw new IllegalStateException(msg);
            }
            resultList.add(col);
        }
        _localColumnList = resultList;
        return _localColumnList;
    }

    public List<String> getLocalColumnNameList() {
        return _localColumnNameList;
    }

    public List<String> getLocalColumnJavaNameList() {
        final List<String> resultList = new ArrayList<String>();
        final List<Column> localColumnList = getLocalColumnList();
        for (Column column : localColumnList) {
            resultList.add(column.getJavaName());
        }
        return resultList;
    }

    public Column getLocalColumnAsOne() {
        return getTable().getColumn(getLocalColumnNameAsOne());
    }

    public String getLocalColumnNameAsOne() {
        final List<String> columnNameList = getLocalColumnNameList();
        if (columnNameList.size() != 1) {
            String msg = "This method is for only-one foreign-key:";
            msg = msg + " getLocalColumnNameList().size()=" + columnNameList.size();
            msg = msg + " baseTable=" + getTable().getTableDbName();
            msg = msg + " foreignTable=" + getForeignTable().getTableDbName();
            throw new IllegalStateException(msg);
        }
        return columnNameList.get(0);
    }

    public String getLocalColumnJavaNameAsOne() {
        return getLocalColumnAsOne().getJavaName();
    }

    public Column getLocalColumnByForeignColumn(Column foreignColumn) {
        final String localColumnName = getForeignLocalMapping().get(foreignColumn.getName());
        return getTable().getColumn(localColumnName);
    }

    public boolean hasLocalColumnExceptPrimaryKey() {
        final List<Column> localColumnList = getLocalColumnList();
        for (Column column : localColumnList) {
            if (!column.isPrimaryKey()) {
                return true;
            }
        }
        return false;
    }

    // -----------------------------------------------------
    //                                       Foreign Element
    //                                       ---------------
    public List<Column> getForeignColumnList() {
        if (_foreignColumnList != null) {
            return _foreignColumnList;
        }
        final Table foreignTable = getForeignTable();
        final List<String> columnList = getForeignColumnNameList();
        if (columnList == null || columnList.isEmpty()) {
            throwForeignColumnListNullOrEmptyException(columnList);
        }
        final List<Column> resultList = new ArrayList<Column>();
        for (Iterator<String> ite = columnList.iterator(); ite.hasNext();) {
            final String name = (String) ite.next();
            final Column foreignCol = foreignTable.getColumn(name);
            if (foreignCol == null) {
                throwForeignColumnNotFoundException(foreignTable, name);
            }
            resultList.add(foreignCol);
        }
        _foreignColumnList = resultList;
        return _foreignColumnList;
    }

    protected void throwForeignColumnListNullOrEmptyException(List<String> columnList) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The list of foreign column was null or empty.");
        prepareBasicExceptionItem(br);
        br.addItem("ForeignColumn List");
        br.addElement(columnList);
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg);
    }

    protected void throwForeignColumnNotFoundException(Table targetTable, String columnName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the foreign column in the table.");
        prepareBasicExceptionItem(br);
        br.addItem("Target Table");
        br.addElement(targetTable.getBasicInfoDispString());
        br.addItem("NotFound Column");
        br.addElement(columnName);
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg);
    }

    public List<String> getForeignColumnNameList() {
        return _foreignColumnNameList;
    }

    public Column getForeignColumnAsOne() {
        return getForeignTable().getColumn(getForeignColumnNameAsOne());
    }

    public String getForeignColumnNameAsOne() {
        final List<String> columnNameList = getForeignColumnNameList();
        if (columnNameList.size() != 1) {
            throwForeignColumnCannotAsOneException(columnNameList);
        }
        return columnNameList.get(0);
    }

    protected void throwForeignColumnCannotAsOneException(List<String> columnNameList) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Cannot get the foreign column as one.");
        prepareBasicExceptionItem(br);
        br.addItem("Foreign Column");
        br.addElement(columnNameList);
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg);
    }

    public String getForeignColumnJavaNameAsOne() {
        final String columnName = getForeignColumnNameAsOne();
        final Table foreignTable = getForeignTable();
        return foreignTable.getColumn(columnName).getJavaName();
    }

    public Column getForeignColumnByLocalColumn(Column localColumn) {
        final String foreignColumnName = getLocalForeignMapping().get(localColumn.getName());
        return getForeignTable().getColumn(foreignColumnName);
    }

    // ===================================================================================
    //                                                                      Column Mapping
    //                                                                      ==============
    public Map<String, String> getLocalForeignMapping() {
        return _localForeignMap;
    }

    public Map<String, String> getForeignLocalMapping() {
        return _foreignLocalMap;
    }

    // ===================================================================================
    //                                                                   Column Expression
    //                                                                   =================
    // -----------------------------------------------------
    //                                                 Local
    //                                                 -----
    /**
     * Returns a comma delimited string of local column names.
     * @return Generated string.
     */
    public String getLocalColumnNameCommaString() { // same as 
        return DfColumnListToStringBuilder.getColumnNameCommaString(getLocalColumnList());
    }

    /**
     * Returns a comma delimited string of local column getters. [getRcvlcqNo(), getSprlptTp()]
     * @return Generated string.
     */
    public String getLocalColumnGetterCommaString() {
        return DfColumnListToStringBuilder.getColumnGetterCommaString(getLocalColumnList());
    }

    /**
     * Returns a local column name of first element.
     * @return Selected string.
     */
    public String getFirstLocalColumnName() {
        return getLocalColumnNameList().get(0);
    }

    // -----------------------------------------------------
    //                                               Foreign
    //                                               -------
    /**
     * Returns a comma delimited string of foreign column names.
     * @return Generated string.
     */
    public String getForeignColumnNameCommaString() {
        return DfColumnListToStringBuilder.getColumnNameCommaString(getForeignColumnList());
    }

    /**
     * Returns a comma delimited string of foreign column getters. [getRcvlcqNo(), getSprlptTp()]
     * @return Generated string.
     */
    public String getForeignColumnGetterCommaString() {
        return DfColumnListToStringBuilder.getColumnGetterCommaString(getForeignColumnList());
    }

    /**
     * Returns a foreign column name of first element.
     * @return Selected string.
     */
    public String getFirstForeignColumnName() {
        return getForeignColumnNameList().get(0);
    }

    // -----------------------------------------------------
    //                                          Setup String
    //                                          ------------
    /**
     * Returns ForeignTable-BeanSetupString. [setRcvlcqNo_Suffix(getRcvlcqNo()).setSprlptTp_Suffix(getSprlptTp())]
     * @param setterSuffix Setter suffix(_Equal and _IsNotNull and so on...).
     * @return Generated string.
     */
    public String getForeignTableBeanSetupString(String setterSuffix) {
        return getForeignTableBeanSetupString(setterSuffix, "set");
    }

    public String getForeignTableBeanSetupString(String setterSuffix, String setterPrefix) {
        final List<Column> localColumnList = getLocalColumnList();
        String result = "";
        for (final Iterator<Column> ite = localColumnList.iterator(); ite.hasNext();) {
            final Column localCol = (Column) ite.next();
            final Column foreignCol = getForeignColumnByLocalColumn(localCol);
            final String setterName = setterPrefix + foreignCol.getJavaName() + setterSuffix;
            final String getterName = "(_" + localCol.getUncapitalisedJavaName() + ")";
            if ("".equals(result)) {
                result = setterName + getterName;
            } else {
                result = result + "." + setterName + getterName;
            }
        }
        return result;
    }

    /**
     * Returns ChildrenTable-BeanSetupString. [setRcvlcqNo_Suffix(getRcvlcqNo()).setSprlptTp_Suffix(getSprlptTp());]
     * About ForeginKey that Table#getReferrer() returns, Local means children.
     * @param setterSuffix Setter suffix(_Equal and _IsNotNull and so on...).
     * @return Generated string.
     */
    public String getChildrenTableBeanSetupString(String setterSuffix) {
        return getChildrenTableBeanSetupString(setterSuffix, "set");
    }

    public String getChildrenTableBeanSetupString(String setterSuffix, String setterPrefix) {
        List<Column> localColumnList = getLocalColumnList();
        String result = "";

        for (final Iterator<Column> ite = localColumnList.iterator(); ite.hasNext();) {
            final Column localCol = (Column) ite.next();
            final Column foreignCol = getForeignColumnByLocalColumn(localCol);
            final String setterName = setterPrefix + localCol.getJavaName() + setterSuffix;
            final String getterName = "(_" + foreignCol.getUncapitalisedJavaName() + ")";
            if ("".equals(result)) {
                result = setterName + getterName;
            } else {
                result = result + "." + setterName + getterName;
            }
        }
        return result;
    }

    // -----------------------------------------------------
    //                                         for S2Dao.NET
    //                                         -------------
    /**
     * Returns RelationKeysCommaString. [RECLCQ_NO:RECLCQ_NO, SPRLPT_TP:...] (LOCAL:FOREIGN) <br>
     * (for s2dao)
     * @return Generated string.
     */
    public String getRelationKeysCommaString() { // for S2Dao.NET (only used for C#)
        final List<Column> localColumnList = getLocalColumnList();
        String result = "";
        for (final Iterator<Column> ite = localColumnList.iterator(); ite.hasNext();) {
            final Column localCol = (Column) ite.next();
            final Column foreignCol = getForeignColumnByLocalColumn(localCol);
            final String localName = localCol.getColumnSqlName();
            final String foreignName = foreignCol.getColumnSqlName();
            if ("".equals(result)) {
                result = localName + ":" + foreignName;
            } else {
                result = result + ", " + localName + ":" + foreignName;
            }
        }
        return result;
    }

    /**
     * Returns RelationKeysCommaString for OneToOneReferrer. [RECLCQ_NO:RECLCQ_NO, SPRLPT_TP:...] (FOREIGN:LOCAL) <br>
     * (for s2dao)
     * @return Generated string.
     */
    public String getRelationKeysCommaStringForOneToOneReferrer() { // for S2Dao.NET (only used for C#)
        final List<Column> foreignColumnList = getForeignColumnList();
        String result = "";
        for (final Iterator<Column> ite = foreignColumnList.iterator(); ite.hasNext();) {
            final Column foreignCol = (Column) ite.next();
            final Column localCol = getLocalColumnByForeignColumn(foreignCol);
            final String foreignName = foreignCol.getColumnSqlName();
            final String localName = localCol.getColumnSqlName();

            if ("".equals(result)) {
                result = foreignName + ":" + localName;
            } else {
                result = result + ", " + foreignName + ":" + localName;
            }
        }
        return result;
    }

    // -----------------------------------------------------
    //                                            for S2JDBC
    //                                            ----------
    public String getReferrerPropertyNameAsOneS2Jdbc() { // for S2JDBC
        String name = buildReferrerPropertySubIdentityNameAsOne();
        return getTable().getUncapitalisedJavaName() + name;
    }

    // ===================================================================================
    //                                                                     Fixed Condition
    //                                                                     ===============
    public String getFixedConditionArg() {
        analyzeDynamicFixedConditionIfNeeds();
        checkOptionConstraints();
        return _fixedCondition != null ? "\"" + _fixedCondition + "\"" : "null";
    }

    protected void checkOptionConstraints() {
        final DfFixedConditionOptionChecker checker =
                new DfFixedConditionOptionChecker(this, _fixedCondition, _dynamicFixedConditionMap, _fixedInline, _fixedReferrer);
        checker.checkOptionConstraints();
    }

    public boolean hasFixedCondition() {
        analyzeDynamicFixedConditionIfNeeds();
        return _fixedCondition != null && _fixedCondition.trim().length() > 0;
    }

    public boolean hasFixedSuffix() {
        analyzeDynamicFixedConditionIfNeeds();
        return _fixedSuffix != null && _fixedSuffix.trim().length() > 0;
    }

    public boolean containsIFCommentInFixedCondition() {
        return hasFixedCondition() && _fixedCondition.contains("/*IF ");
    }

    // ===================================================================================
    //                                                             Dynamic Fixed Condition
    //                                                             =======================
    public String getDynamicFixedConditionArgs() {
        analyzeDynamicFixedConditionIfNeeds();
        return buildDynamicFixedConditionArgs(false);
    }

    public String getDynamicFixedConditionFinalArgs() {
        analyzeDynamicFixedConditionIfNeeds();
        return buildDynamicFixedConditionArgs(true);
    }

    protected String buildDynamicFixedConditionArgs(boolean finalArg) {
        final Set<String> parameterNameSet = _dynamicFixedConditionMap.keySet();
        final StringBuilder sb = new StringBuilder();
        for (String parameterName : parameterNameSet) {
            final String paramterType = _dynamicFixedConditionMap.get(parameterName);
            if (sb.length() > 0) {
                sb.append(", ");
            }
            if (finalArg) {
                sb.append("final ");
            }
            sb.append(paramterType).append(" ").append(parameterName);
        }
        return sb.toString();
    }

    public boolean hasDynamicFixedCondition() {
        analyzeDynamicFixedConditionIfNeeds();
        return hasFixedCondition() && !_dynamicFixedConditionMap.isEmpty();
    }

    public String getDynamicFixedConditionVariables() {
        analyzeDynamicFixedConditionIfNeeds();
        return buildDynamicFixedConditionVariables();
    }

    protected String buildDynamicFixedConditionVariables() {
        final StringBuilder sb = new StringBuilder();
        for (String parameterName : _dynamicFixedConditionMap.keySet()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(parameterName);
        }
        return sb.toString();
    }

    public String getDynamicFixedConditionParameterMapSetup() {
        analyzeDynamicFixedConditionIfNeeds();
        final StringBuilder sb = new StringBuilder();
        for (Entry<String, String> entry : _dynamicFixedConditionMap.entrySet()) {
            final String parameterName = entry.getKey();
            final String parameterType = entry.getValue();
            sb.append("parameterMap.put(\"").append(parameterName).append("\", ");
            if (java.util.Date.class.getName().equals(parameterType)) {
                sb.append("fCTPD(").append(parameterName).append(")");
            } else {
                sb.append(parameterName);
            }
            sb.append(");");
        }
        return sb.toString();
    }

    public String getDynamicFixedConditionDBMetaSetupList() {
        analyzeDynamicFixedConditionIfNeeds();
        final String variables = buildDynamicFixedConditionVariables();
        if (variables != null && variables.trim().length() > 0) {
            return "newArrayList(\"" + variables + "\")";
        } else {
            return "null";
        }
    }

    public String getDynamicFixedConditionArgsJavaDocString() {
        return buildDynamicFixedConditionArgsJavaDocString(false);
    }

    public String getDynamicFixedConditionArgsJavaDocStringNest() {
        return buildDynamicFixedConditionArgsJavaDocString(true);
    }

    protected String buildDynamicFixedConditionArgsJavaDocString(boolean nest) {
        analyzeDynamicFixedConditionIfNeeds();
        final StringBuilder sb = new StringBuilder();
        int count = 0;
        for (Entry<String, String> entry : _dynamicFixedConditionMap.entrySet()) {
            final String parameterName = entry.getKey();
            if (count > 0) {
                if (nest) {
                    sb.append("    ");
                }
                sb.append("     * ");
            }
            sb.append("@param ").append(parameterName).append(" ");
            sb.append("The bind parameter of fixed condition for ").append(parameterName);
            if (containsIFCommentInFixedCondition()) {
                sb.append(". (might be NullAllowed: IF comment exists in the fixed condition)");
            } else { // mainly here
                sb.append(". (NotNull)");
            }
            sb.append(getBasicProperties().getSourceCodeLineSeparator());
            ++count;
        }
        final String result = Srl.rtrim(sb.toString()); // trim last line separator
        return result;
    }

    protected void analyzeDynamicFixedConditionIfNeeds() { // lazy called
        final DfFixedConditionDynamicAnalyzer analyzer =
                new DfFixedConditionDynamicAnalyzer(this, _fixedCondition, _dynamicFixedConditionMap);
        final Map<String, String> replacementMap = analyzer.analyzeToReplacementMap();
        for (Entry<String, String> entry : replacementMap.entrySet()) {
            _fixedCondition = replace(_fixedCondition, entry.getKey(), entry.getValue());
        }
    }

    // ===================================================================================
    //                                                                          Class Name
    //                                                                          ==========
    // -----------------------------------------------------
    //                                    Foreign Class Name
    //                                    ------------------
    public String getForeignTableExtendedEntityClassName() {
        return getForeignTable().getExtendedEntityClassName();
    }

    public String getForeignTableImmutableExtendedEntityClassName() {
        return getForeignTable().getImmutableExtendedEntityClassName();
    }

    public String getForeignTableExtendedBehaviorClassName() {
        return getForeignTable().getExtendedBehaviorClassName();
    }

    public String getForeignTableExtendedReferrerLoaderClassName() {
        return getForeignTable().getExtendedReferrerLoaderClassName();
    }

    public String getForeignTableDBMetaClassName() {
        return getForeignTable().getDBMetaClassName();
    }

    public String getForeignTableExtendedConditionBeanClassName() {
        return getForeignTable().getExtendedConditionBeanClassName();
    }

    public String getForeignTableExtendedConditionQueryClassName() {
        return getForeignTable().getExtendedConditionQueryClassName();
    }

    public String getForeignTableNestSelectSetupperClassName() {
        return getForeignTable().getNestSelectSetupperClassName();
    }

    public String getForeignTableExtendedSimpleDtoClassName() {
        return getForeignTable().getSimpleDtoExtendedDtoClassName();
    }

    // -----------------------------------------------------
    //                                   Referrer Class Name
    //                                   -------------------
    public String getReferrerTableExtendedEntityClassName() {
        return getTable().getExtendedEntityClassName();
    }

    public String getReferrerTableImmutableExtendedEntityClassName() {
        return getTable().getImmutableExtendedEntityClassName();
    }

    public String getReferrerTableExtendedBehaviorClassName() {
        return getTable().getExtendedBehaviorClassName();
    }

    public String getReferrerTableExtendedReferrerLoaderClassName() {
        return getTable().getExtendedReferrerLoaderClassName();
    }

    public String getReferrerTableDBMetaClassName() {
        return getTable().getDBMetaClassName();
    }

    public String getReferrerTableExtendedConditionBeanClassName() {
        return getTable().getExtendedConditionBeanClassName();
    }

    public String getReferrerTableExtendedConditionQueryClassName() {
        return getTable().getExtendedConditionQueryClassName();
    }

    public String getReferrerTableNestSelectSetupperClassName() {
        return getTable().getNestSelectSetupperClassName();
    }

    public String getReferrerTableExtendedSimpleDtoClassName() {
        return getTable().getSimpleDtoExtendedDtoClassName();
    }

    // ===================================================================================
    //                                                                       Determination
    //                                                                       =============
    /**
     * Is this relation 'one-to-one'?
     * @return The determination, true or false.
     */
    public boolean isOneToOne() {
        if (_oneToOne == null) {
            _oneToOne = false;
            final List<Column> localColumnList = getLocalColumnList();
            final List<Column> localPrimaryColumnList = getTable().getPrimaryKey();
            if (localColumnList.equals(localPrimaryColumnList)) {
                _oneToOne = true;
            } else {
                final List<Unique> uniqueList = getTable().getUniqueList();
                for (final Unique unique : uniqueList) {
                    if (unique.hasSameColumnSet(localColumnList)) {
                        _oneToOne = true;
                    }
                }
            }
        }
        return _oneToOne;
    }

    public boolean isBizOneToOne() {
        if (_bizOneToOne == null) {
            _bizOneToOne = isOneToOne() && hasFixedCondition();
        }
        return _bizOneToOne;
    }

    public boolean isBizManyToOne() {
        if (_bizManyToOne == null) {
            _bizManyToOne = !isOneToOne() && hasFixedCondition();
        }
        return _bizManyToOne;
    }

    public boolean isSimpleKeyFK() {
        return _localColumnNameList.size() == 1;
    }

    public boolean isCompoundFK() {
        return _localColumnNameList.size() > 1;
    }

    public boolean isSelfReference() {
        return _localTable.getTableDbName().equals(getForeignTable().getTableDbName());
    }

    public boolean canBeReferrer() {
        if (isSuppressReferrerRelation()) {
            return false;
        }
        if (hasFixedCondition() && !isFixedReferrer()) {
            return false;
        }
        return isForeignColumnPrimaryKey() || isForeignColumnUnique();

        // *reference to unique key is unsupported basically
        //  (a-little-supported)
    }

    protected boolean isSuppressReferrerRelation() {
        final DfLittleAdjustmentProperties prop = getLittleAdjustmentProperties();
        final Map<String, Set<String>> referrerRelationMap = prop.getSuppressReferrerRelationMap();
        final Set<String> relationNameSet = referrerRelationMap.get(getForeignTable().getTableDbName());
        if (relationNameSet == null) {
            return false;
        }
        // the table exists in the map here
        final String relationName;
        if (isOneToOne()) {
            relationName = getReferrerPropertyNameAsOne();
        } else {
            relationName = getReferrerPropertyName();
        }
        if (relationNameSet.contains("$$ALL$$")) {
            if (relationNameSet.contains("!" + relationName)) { // pinpoint include
                return false;
            }
            return true;
        }
        return relationNameSet.contains(relationName);
    }

    /**
     * Are all local columns primary-key?
     * @return The determination, true or false.
     */
    public boolean isLocalColumnPrimaryKey() {
        return isColumnPrimaryKey(getLocalColumnList());
    }

    /**
     * Are all foreign columns primary-key? <br>
     * Basically true. If biz-one-to-one and unique key, false.
     * @return The determination, true or false.
     */
    public boolean isForeignColumnPrimaryKey() {
        return isColumnPrimaryKey(getForeignColumnList());
    }

    /**
     * Are all foreign columns unique-key? <br>
     * @return The determination, true or false.
     */
    public boolean isForeignColumnUnique() {
        return isColumnUnique(getForeignColumnList());
    }

    protected boolean isColumnPrimaryKey(List<Column> columnList) {
        for (Column column : columnList) {
            if (!column.isPrimaryKey()) {
                return false;
            }
        }
        return true;
    }

    protected boolean isColumnUnique(List<Column> columnList) {
        for (Column column : columnList) {
            if (!column.isUnique()) {
                return false;
            }
        }
        return true;
    }

    // -----------------------------------------------------
    //                                           Nest Select
    //                                           -----------
    public boolean hasForeignNestSelectSetupper() {
        return getForeignTable().hasNestSelectSetupper();
    }

    public boolean hasReferrerNestSelectSetupper() {
        return getTable().hasNestSelectSetupper();
    }

    // -----------------------------------------------------
    //                                              SubQuery
    //                                              --------
    public boolean isExistsReferrerSupported() {
        if (!isMakeConditionQueryExistsReferrerToOne() && isOneToOne()) {
            return false;
        }
        if (isCompoundFKImplicitReverseForeignKey()) {
            return false; // too complex so unsupported
        }
        if (isSuppressSubQuery()) {
            return false;
        }
        final List<Column> columnList = getForeignColumnList();
        for (Column column : columnList) {
            if (!getIncludeQueryProperties().isAvailableRelationExistsReferrer(column)) {
                return false;
            }
        }
        return true;
    }

    public boolean isInScopeRelationAllowedForeignKey() {
        if (!isMakeConditionQueryInScopeRelationToOne()) {
            return false; // suppress InScopeRelation for many-to-one
        }
        final List<Column> columnList = getLocalColumnList();
        for (Column column : columnList) {
            if (!getIncludeQueryProperties().isAvailableRelationInScopeRelation(column)) {
                return false;
            }
        }
        return isSimpleKeyFK() && !hasFixedCondition();
    }

    public boolean isInScopeRelationAsReferrerSupported() {
        if (!isMakeConditionQueryInScopeRelationToOne() && isOneToOne()) {
            return false;
        }
        if (isCompoundFKImplicitReverseForeignKey()) {
            return false; // too complex so unsupported
        }
        if (isSuppressSubQuery()) {
            return false;
        }
        final List<Column> columnList = getForeignColumnList();
        for (Column column : columnList) {
            if (!getIncludeQueryProperties().isAvailableRelationInScopeRelation(column)) {
                return false;
            }
        }
        return true;
    }

    public boolean isDerivedReferrerSupported() {
        if (isOneToOne()) {
            return false;
        }
        if (isCompoundFKImplicitReverseForeignKey()) {
            return false; // too complex so unsupported
        }
        if (isSuppressSubQuery()) {
            return false;
        }
        final List<Column> columnList = getForeignColumnList();
        for (Column column : columnList) {
            if (!(column.isJavaNativeStringObject() || column.isJavaNativeNumberObject())) {
                return false; // only string or number is supported
            }
            if (!getIncludeQueryProperties().isAvailableRelationDerivedReferrer(column)) {
                return false;
            }
        }
        return true;
    }

    protected boolean isMakeConditionQueryExistsReferrerToOne() {
        return getLittleAdjustmentProperties().isMakeConditionQueryExistsReferrerToOne();
    }

    protected boolean isMakeConditionQueryInScopeRelationToOne() {
        return getLittleAdjustmentProperties().isMakeConditionQueryInScopeRelationToOne();
    }

    protected boolean isCompoundFKImplicitReverseForeignKey() {
        return isCompoundFK() && isImplicitReverseForeignKey();
    }

    // ===================================================================================
    //                                                                             Display
    //                                                                             =======
    // -----------------------------------------------------
    //                                               Foreign
    //                                               -------
    public String getForeignDispForJavaDoc() {
        return buildForeignDispForJavaDoc("    ");
    }

    public String getForeignDispForJavaDocNest() {
        return buildForeignDispForJavaDoc("        ");
    }

    protected String buildForeignDispForJavaDoc(String indent) {
        final StringBuilder sb = new StringBuilder();
        sb.append(getForeignSimpleDisp()).append(".");
        if (Srl.is_NotNull_and_NotTrimmedEmpty(_comment)) {
            final String comment = resolveCommentForJavaDoc(_comment, indent);
            final String sourceCodeLn = getBasicProperties().getSourceCodeLineSeparator();
            sb.append(" <br>").append(sourceCodeLn);
            sb.append(indent).append(" * ").append(comment);
        }
        return sb.toString();
    }

    public String getForeignSimpleDisp() {
        final StringBuilder sb = new StringBuilder();
        final Table foreignTable = getForeignTable();
        sb.append(foreignTable.getAliasExpression());
        sb.append(foreignTable.getTableDispName());
        sb.append(" by my ").append(getLocalColumnNameCommaString());
        sb.append(", named '").append(getForeignJavaBeansRulePropertyName()).append("'");
        return sb.toString();
    }

    // -----------------------------------------------------
    //                                         ReferrerAsOne
    //                                         -------------
    public String getReferrerDispAsOneForJavaDoc() {
        return doGetReferrerDispAsOneForJavaDoc("    ");
    }

    public String getReferrerDispAsOneForJavaDocNest() {
        return doGetReferrerDispAsOneForJavaDoc("        ");
    }

    public String doGetReferrerDispAsOneForJavaDoc(String indent) {
        final StringBuilder sb = new StringBuilder();
        sb.append(getReferrerSimpleDispAsOne()).append(".");
        if (Srl.is_NotNull_and_NotTrimmedEmpty(_comment)) {
            final String comment = resolveCommentForJavaDoc(_comment, indent);
            final String sourceCodeLn = getBasicProperties().getSourceCodeLineSeparator();
            sb.append(" <br>").append(sourceCodeLn);
            sb.append(indent).append(" * ").append(comment);
        }
        return sb.toString();
    }

    public String getReferrerSimpleDispAsOne() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getTable().getAliasExpression());
        sb.append(getTable().getTableDbName());
        sb.append(" by ").append(getLocalColumnNameCommaString());
        sb.append(", named '").append(getReferrerJavaBeansRulePropertyNameAsOne()).append("'");
        return sb.toString();
    }

    // -----------------------------------------------------
    //                                              Referrer
    //                                              --------
    public String getReferrerDispForJavaDoc() {
        return doGetReferrerDispForJavaDoc("    ");
    }

    public String getReferrerDispForJavaDocNest() {
        return doGetReferrerDispForJavaDoc("        ");
    }

    public String doGetReferrerDispForJavaDoc(String indent) {
        final StringBuilder sb = new StringBuilder();
        sb.append(getReferrerSimpleDisp()).append(".");
        if (Srl.is_NotNull_and_NotTrimmedEmpty(_comment)) {
            final String comment = resolveCommentForJavaDoc(_comment, indent);
            final String sourceCodeLn = getBasicProperties().getSourceCodeLineSeparator();
            sb.append(" <br>").append(sourceCodeLn);
            sb.append(indent).append(" * ").append(comment);
        }
        return sb.toString();
    }

    public String getReferrerSimpleDisp() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getTable().getAliasExpression());
        sb.append(getTable().getTableDispName());
        sb.append(" by ").append(getLocalColumnNameCommaString());
        sb.append(", named '").append(getReferrerJavaBeansRulePropertyName()).append("'");
        return sb.toString();
    }

    // -----------------------------------------------------
    //                                         Common Helper
    //                                         -------------
    protected String resolveCommentForJavaDoc(String comment, String indent) {
        return getDocumentProperties().resolveJavaDocContent(comment, indent);
    }

    // ===================================================================================
    //                                                                 Implicit Conversion
    //                                                                 ===================
    public boolean canImplicitConversion() {
        if (!isSimpleKeyFK()) {
            return false;
        }
        final Column localColumn = getLocalColumnAsOne();
        final Column foreignColumn = getForeignColumnAsOne();
        // the scope is in String and Number types
        if ((localColumn.isJavaNativeStringObject() || localColumn.isJavaNativeNumberObject())
                && (foreignColumn.isJavaNativeStringObject() || foreignColumn.isJavaNativeNumberObject())) {
            return !localColumn.getJdbcType().equals(foreignColumn.getJdbcType());
        }
        return false;
    }

    // /- - - - - - - - - - - - - - - - - - -
    // attention: local is referrer
    // *but resolved in runtime after 1.0.6A
    // - - - - - - - - - -/

    public boolean isConvertToReferrerByToString() {
        return getLocalColumnAsOne().isJavaNativeStringObject();
    }

    public boolean isConvertToReferrerByConstructor() {
        return getLocalColumnAsOne().isJavaNativeBigDecimal();
    }

    public boolean isConvertToReferrerByValueOf() {
        return getLocalColumnAsOne().isJavaNativeValueOfAbleObject();
    }

    // ===================================================================================
    //                                                              Relational Null Object
    //                                                              ======================
    // -----------------------------------------------------
    //                                               Foreign
    //                                               -------
    public boolean canBeRelationalNullObjectForeign() {
        return getForeignTable().canBeRelationalNullObjectForeign();
    }

    public String getRelationalNullObjectForeignProviderExp() {
        return doGetRelationalNullObjectForeignProviderExp(getForeignTable(), getLocalColumnGetterCommaString());
    }

    protected String doGetRelationalNullObjectForeignProviderExp(Table table, String getterCommaString) {
        final String foreignProperty = getForeignPropertyName();
        final String beansRuleProperty = getForeignJavaBeansRulePropertyName();
        return table.buildRelationalNullObjectProviderForeignExp(foreignProperty, beansRuleProperty, getterCommaString);
    }

    public String getRelationalNullObjectForeignEmptyExp() {
        return getLittleAdjustmentProperties().getRelationalNullObjectOptionalEmptyExp();
    }

    // -----------------------------------------------------
    //                                       Referrer as One
    //                                       ---------------
    public boolean canBeRelationalNullObjectReferrerAsOne() {
        return getTable().canBeRelationalNullObjectForeign();
    }

    public String getRelationalNullObjectReferrerAsOneProviderExp() {
        return doGetRelationalNullObjectReferrerAsOneProviderExp(getTable(), getForeignColumnGetterCommaString());
    }

    protected String doGetRelationalNullObjectReferrerAsOneProviderExp(Table table, String getterCommaString) {
        final String foreignProperty = getReferrerPropertyNameAsOne();
        final String beansRuleProperty = getReferrerJavaBeansRulePropertyNameAsOne();
        return table.buildRelationalNullObjectProviderForeignExp(foreignProperty, beansRuleProperty, getterCommaString);
    }

    public String getRelationalNullObjectReferrerAsOneEmptyExp() {
        return getLittleAdjustmentProperties().getRelationalNullObjectOptionalEmptyExp();
    }

    // -----------------------------------------------------
    //                                      Referrer as Many
    //                                      ----------------
    // unsupported for now
    //public String getRelationalNullObjectReferrerProviderExp() {
    //    return doGetRelationalNullObjectProviderExp(getTable(), getForeignColumnGetterCommaString());
    //}
    //
    //public String getRelationalNullObjectReferrerEmptyExp() {
    //    return getLittleAdjustmentProperties().getRelationalNullObjectOptionalEmptyExp();
    //}

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    protected DfBasicProperties getBasicProperties() {
        return getProperties().getBasicProperties();
    }

    protected DfClassificationProperties getClassificationProperties() {
        return getProperties().getClassificationProperties();
    }

    protected DfDocumentProperties getDocumentProperties() {
        return getProperties().getDocumentProperties();
    }

    protected DfIncludeQueryProperties getIncludeQueryProperties() {
        return getProperties().getIncludeQueryProperties();
    }

    protected DfLittleAdjustmentProperties getLittleAdjustmentProperties() {
        return getProperties().getLittleAdjustmentProperties();
    }

    // ===================================================================================
    //                                                                          Simple DTO
    //                                                                          ==========
    public String getSimpleDtoForeignVariableName() {
        return getProperties().getSimpleDtoProperties().buildFieldName(getForeignPropertyNameInitCap());
    }

    public String getSimpleDtoReferrerAsOneVariableName() {
        return getProperties().getSimpleDtoProperties().buildFieldName(getReferrerPropertyNameAsOneInitCap());
    }

    public String getSimpleDtoReferrerVariableName() {
        return getProperties().getSimpleDtoProperties().buildFieldName(getReferrerPropertyNameInitCap());
    }

    // ===================================================================================
    //                                                                  Exception Handling
    //                                                                  ==================
    protected void prepareBasicExceptionItem(ExceptionMessageBuilder br) {
        br.addItem("Foreign Key");
        br.addElement(_name);
        br.addItem("Local Table");
        br.addElement(getTable().getBasicInfoDispString());
        br.addItem("Foreign Table");
        br.addElement(getForeignTable().getBasicInfoDispString());
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String replace(String text, String fromText, String toText) {
        return Srl.replace(text, fromText, toText);
    }

    protected String initCap(String str) {
        return Srl.initCap(str);
    }

    protected String initUncap(String str) {
        return Srl.initUncap(str);
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    /**
     * String representation of the foreign key. This is an xml representation.
     * @return string representation in xml
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("    <foreign-key");
        sb.append(" foreignTable=\"").append(getForeignTablePureName()).append("\"");
        sb.append(" name=\"").append(getName()).append("\"");
        sb.append(">\n");

        for (int i = 0; i < _localColumnNameList.size(); i++) {
            sb.append("        <reference local=\"").append(_localColumnNameList.get(i));
            sb.append("\" foreign=\"").append(_foreignColumnNameList.get(i)).append("\"/>\n");
        }
        sb.append("    </foreign-key>");
        return sb.toString();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    // -----------------------------------------------------
    //                                                 Basic
    //                                                 -----
    /**
     * {@inheritDoc}
     */
    public String getName() {
        return _name;
    }

    /**
     * Set the constraint name of the FK.
     * @param name the name string. (NullAllowed)
     */
    public void setName(String name) {
        this._name = name;
    }

    /**
     * Get the pure name of the table for the FK.
     * @return the pure name of the foreign table. (NotNull)
     */
    public String getForeignTablePureName() {
        return _foreignTablePureName;
    }

    /**
     * Set the pure name of the table for the FK.
     * @param tablePureName the pure name of the foreign table. (NotNull)
     */
    public void setForeignTablePureName(String tablePureName) {
        _foreignTablePureName = tablePureName;
    }

    /**
     * Get the foreignSchema of the foreign table
     * @return the unified schema of the foreign table
     */
    public UnifiedSchema getForeignSchema() {
        return _foreignSchema;
    }

    /**
     * Set the foreignSchema of the foreign table
     * @param foreignSchema the unified schema of the foreign table
     */
    public void setForeignSchema(UnifiedSchema foreignSchema) {
        _foreignSchema = foreignSchema;
    }

    /**
     * Set the base Table of the foreign key
     * @param baseTable the table
     */
    public void setTable(Table baseTable) {
        _localTable = baseTable;
    }

    /**
     * Get the base Table of the foreign key
     * @return the base table
     */
    public Table getTable() {
        return _localTable;
    }

    // -----------------------------------------------------
    //                                            Additional
    //                                            ----------
    public boolean isAdditionalForeignKey() {
        return _additionalForeignKey;
    }

    public void setAdditionalForeignKey(boolean additionalForeignKey) {
        _additionalForeignKey = additionalForeignKey;
    }

    public String getFixedCondition() {
        return _fixedCondition;
    }

    public void setFixedCondition(String fixedCondition) {
        _fixedCondition = fixedCondition;
    }

    public String getFixedSuffix() {
        return _fixedSuffix;
    }

    public String getFixedSuffixExp() {
        return hasFixedSuffix() ? _fixedSuffix : "";
    }

    public void setFixedSuffix(String fixedSuffix) {
        _fixedSuffix = fixedSuffix;
    }

    public boolean isFixedInline() {
        return _fixedInline;
    }

    public void setFixedInline(boolean fixedInline) {
        _fixedInline = fixedInline;
    }

    public boolean isFixedReferrer() {
        return _fixedReferrer;
    }

    public void setFixedReferrer(boolean fixedReferrer) {
        _fixedReferrer = fixedReferrer;
    }

    public boolean isFixedOnlyJoin() {
        return _fixedOnlyJoin;
    }

    public void setFixedOnlyJoin(boolean fixedOnlyJoin) {
        _fixedOnlyJoin = fixedOnlyJoin;
    }

    public String getComment() {
        return _comment;
    }

    public void setComment(String comment) {
        _comment = comment;
    }

    public boolean isSuppressJoin() {
        return _suppressJoin;
    }

    public void setSuppressJoin(boolean suppressJoin) {
        _suppressJoin = suppressJoin;
    }

    public boolean isSuppressSubQuery() {
        return _suppressSubQuery;
    }

    public void setSuppressSubQuery(boolean suppressSubQuery) {
        _suppressSubQuery = suppressSubQuery;
    }

    public boolean isDeprecatedRelation() {
        return Srl.is_NotNull_and_NotTrimmedEmpty(_deprecated);
    }

    public String getDeprecated() {
        return _deprecated;
    }

    public void setDeprecated(String deprecated) {
        _deprecated = deprecated;
    }

    public void setForeignPropertyNamePrefix(String propertyNamePrefix) {
        _foreignPropertyNamePrefix = propertyNamePrefix;
    }

    public boolean isImplicitReverseForeignKey() {
        return _implicitReverseForeignKey;
    }

    public void setImplicitReverseForeignKey(boolean implicitReverseForeignKey) {
        _implicitReverseForeignKey = implicitReverseForeignKey;
    }
}