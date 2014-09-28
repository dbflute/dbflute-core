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
import java.io.File;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.engine.EngineException;
import org.apache.torque.engine.database.transform.XmlToAppData.XmlReadingFilter;
import org.apache.velocity.texen.util.FileUtil;
import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.exception.DfColumnNotFoundException;
import org.seasar.dbflute.exception.DfTableNotFoundException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.friends.velocity.DfGenerator;
import org.seasar.dbflute.helper.StringKeyMap;
import org.seasar.dbflute.helper.StringSet;
import org.seasar.dbflute.helper.jdbc.context.DfDataSourceContext;
import org.seasar.dbflute.helper.jdbc.context.DfSchemaSource;
import org.seasar.dbflute.infra.core.DfDatabaseNameMapping;
import org.seasar.dbflute.logic.generate.deletefile.DfOldClassHandler;
import org.seasar.dbflute.logic.generate.exmange.DfCopyrightResolver;
import org.seasar.dbflute.logic.generate.exmange.DfSerialVersionUIDResolver;
import org.seasar.dbflute.logic.generate.language.DfLanguageDependency;
import org.seasar.dbflute.logic.generate.language.pkgstyle.DfLanguageClassPackage;
import org.seasar.dbflute.logic.generate.packagepath.DfPackagePathHandler;
import org.seasar.dbflute.logic.jdbc.metadata.basic.DfProcedureExtractor;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfProcedureMeta;
import org.seasar.dbflute.logic.sql2entity.analyzer.DfOutsideSqlCollector;
import org.seasar.dbflute.logic.sql2entity.analyzer.DfOutsideSqlPack;
import org.seasar.dbflute.logic.sql2entity.bqp.DfBehaviorQueryPathSetupper;
import org.seasar.dbflute.logic.sql2entity.pmbean.DfPmbGenerationHandler;
import org.seasar.dbflute.logic.sql2entity.pmbean.DfPmbMetaData;
import org.seasar.dbflute.properties.DfBasicProperties;
import org.seasar.dbflute.properties.DfClassificationProperties;
import org.seasar.dbflute.properties.DfDatabaseProperties;
import org.seasar.dbflute.properties.DfLittleAdjustmentProperties;
import org.seasar.dbflute.properties.assistant.DfTableDeterminer;
import org.seasar.dbflute.properties.assistant.DfTableFinder;
import org.seasar.dbflute.properties.assistant.DfTableListProvider;
import org.seasar.dbflute.properties.assistant.classification.DfClassificationElement;
import org.seasar.dbflute.properties.assistant.classification.DfClassificationTop;
import org.seasar.dbflute.properties.assistant.commoncolumn.CommonColumnSetupResource;
import org.seasar.dbflute.properties.initializer.DfAdditionalForeignKeyInitializer;
import org.seasar.dbflute.properties.initializer.DfAdditionalPrimaryKeyInitializer;
import org.seasar.dbflute.properties.initializer.DfAdditionalUniqueKeyInitializer;
import org.seasar.dbflute.properties.initializer.DfIncludeQueryInitializer;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.Srl;
import org.xml.sax.Attributes;

/**
 * A class for holding application data structures. <br />
 * DBFlute treats all tables containing other schema's as one database object.
 * @author modified by jflute (originated in Apache Torque)
 */
public class Database {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance. */
    private static final Log _log = LogFactory.getLog(Database.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                                 Basic
    //                                                 -----
    protected Integer _version;
    protected String _name;

    // -----------------------------------------------------
    //                                               AppData
    //                                               -------
    protected AppData _appData;
    protected AppData _sql2entitySchemaData; // when sql2entity only

    // -----------------------------------------------------
    //                                                 Table
    //                                                 -----
    // use duplicate collection to suppress a little performance cost
    // because tables are frequently referred
    protected final List<Table> _tableList = new ArrayList<Table>(100); // for ordering
    protected final StringKeyMap<Table> _tableMap = StringKeyMap.createAsFlexible(); // for key-map
    protected final StringKeyMap<Table> _distinctPureNameTableMap = StringKeyMap.createAsFlexible(); // for schema driven
    protected final StringSet _sameNameTableNameSet = StringSet.createAsFlexible(); // for schema driven or additional schema

    // -----------------------------------------------------
    //                                              Sequence
    //                                              --------
    // same reason as table's reason
    protected final List<Sequence> _sequenceList = new ArrayList<Sequence>(100);
    protected final StringKeyMap<Sequence> _sequencePureNameMap = StringKeyMap.createAsFlexible(); // for key-map
    protected final StringKeyMap<Sequence> _sequenceUniqueNameMap = StringKeyMap.createAsFlexible(); // for key-map
    protected boolean _sequenceGroupMarked;

    // -----------------------------------------------------
    //                                             Procedure
    //                                             ---------
    // same reason as table's reason
    protected final List<Procedure> _procedureList = new ArrayList<Procedure>(100);
    protected final StringKeyMap<Procedure> _procedurePureNameMap = StringKeyMap.createAsFlexible(); // for key-map
    protected final StringKeyMap<Procedure> _procedureUniqueNameMap = StringKeyMap.createAsFlexible(); // for key-map
    protected boolean _procedureGroupMarked;

    // -----------------------------------------------------
    //                                         ParameterBean
    //                                         -------------
    /** The meta data of parameter bean. */
    protected Map<String, DfPmbMetaData> _pmbMetaDataMap; // when sql2entity only

    // -----------------------------------------------------
    //                                                 Other
    //                                                 -----
    protected String _databaseType;
    protected String _defaultJavaNamingMethod;
    protected boolean _skipDeleteOldClass;

    // *unused on DBFlute
    //protected String _pkg;
    //protected String _defaultIdMethod;
    //protected String _defaultJavaType;
    //protected boolean _isHeavyIndexing;

    // ===================================================================================
    //                                                                             Version
    //                                                                             =======
    public void initializeVersion(Integer version) {
        DfBuildProperties.getInstance().setVersion(version);
    }

    // ===================================================================================
    //                                                                             Loading
    //                                                                             =======
    /**
     * Load the database object from an XML tag.
     * @param attrib the XML attributes
     */
    public void loadFromXML(Attributes attrib) {
        setName(attrib.getValue("name"));
        _defaultJavaNamingMethod = attrib.getValue("defaultJavaNamingMethod"); // Basically Null
        if (_defaultJavaNamingMethod == null) {
            _defaultJavaNamingMethod = NameGenerator.CONV_METHOD_UNDERSCORE; // Basically Here!
        }
    }

    // ===================================================================================
    //                                                                               Table
    //                                                                               =====
    /**
     * Get the list of all tables.
     * @return The list of table object. (NotNull, NotEmpty)
     */
    public List<Table> getTableList() {
        return _tableList;
    }

    public Table[] getTables() { // old style, for compatibility
        final List<Table> tableList = getTableList();
        return tableList.toArray(new Table[tableList.size()]);
    }

    public List<Table> getTableDisplaySortedList() {
        final Comparator<Table> tableDisplayOrderBy = getProperties().getDocumentProperties().getTableDisplayOrderBy();
        final TreeSet<Table> tableSet = new TreeSet<Table>(tableDisplayOrderBy);
        tableSet.addAll(getTableList());
        return new ArrayList<Table>(tableSet);
    }

    public List<Table> getBehaviorTableList() {
        final List<Table> tableList = getTableList();
        final List<Table> filteredList = new ArrayList<Table>();
        for (Table table : tableList) {
            if (!table.isSuppressDBAccessClass()) {
                filteredList.add(table);
            }
        }
        return filteredList;
    }

    /**
     * Get the table by the table DB name.
     * @param tableDbName The DB name of the table to find. (NullAllowed: when e.g. Sql2Entity's related table)
     * @return The found table object. (NullAllowed: when not found)
     */
    public Table getTable(String tableDbName) {
        if (tableDbName == null) {
            return null;
        }
        final Table byName = _tableMap.get(tableDbName);
        if (byName != null) {
            return byName;
        }
        if (tableDbName.contains(".")) { // is e.g. EXAMPLEDB.PUBLIC.MEMBER or PUBLIC.MEMBER
            final String firstPureName = Srl.substringFirstRear(tableDbName, "."); // first remove
            final Table firstPureFound = _tableMap.get(firstPureName); // by e.g. PUBLIC.MEMBER or MEMBER
            if (firstPureFound != null) {
                return firstPureFound;
            }
            if (firstPureName.contains(".")) { // is e.g. PUBLIC.MEMBER
                final String secondPureName = Srl.substringFirstRear(firstPureName, "."); // second remove
                final Table secondPureFound = _tableMap.get(secondPureName); // by e.g. MEMBER
                if (secondPureFound != null) {
                    return secondPureFound;
                }
            }
        } else { // is pure name e.g. MEMBER
            final Table distinctFound = _distinctPureNameTableMap.get(tableDbName);
            if (distinctFound != null && !distinctFound.existsSameNameTable()) { // unique even if pure name
                return distinctFound; // basically for schema-driven table
            }
        }
        return null;
    }

    /**
     * Add table from attributes of SchemaXML.
     * @param attrib The attributes of SchemaXML. (NotNull)
     * @param readingFilter The filter of object. (NullAllowed)
     * @return The instance of added table. (NullAllowed: if null, means the table was filtered)
     */
    public Table addTable(Attributes attrib, XmlReadingFilter readingFilter) {
        final Table table = new Table();
        table.setDatabase(this);
        if (!table.loadFromXML(attrib, readingFilter)) {
            return null;
        }
        addTable(table);
        return table;
    }

    public void addTable(Table table) {
        table.setDatabase(this);
        _tableList.add(table);

        // basically returns pure name (with schema prefix if schema-driven)
        // consideration for same-name does not work yet here 
        final String tableDbName = table.getTableDbName();

        if (handleSameNameTable(table, tableDbName)) {
            return;
        }

        // mainly here
        _tableMap.put(tableDbName, table);

        if (getLittleAdjustmentProperties().isAvailableSchemaDrivenTable()) { // use only when schema-driven table
            final String pureName = Srl.substringLastRear(tableDbName, ".");
            _distinctPureNameTableMap.put(pureName, table);
        }
    }

    protected boolean handleSameNameTable(Table table, String tableDbName) {
        if (_sameNameTableNameSet.contains(tableDbName)) { // found next same-name table
            arrangeSameNameTable(table, tableDbName);
            return true;
        }
        final Table sameNameTable = _tableMap.get(tableDbName);
        if (sameNameTable != null) { // found first same-name table
            _sameNameTableNameSet.add(tableDbName); // for next same-name table
            rearrangeSameNameTable(sameNameTable, tableDbName);
            arrangeSameNameTable(table, tableDbName);
            return true;
        }
        return false;
    }

    protected void rearrangeSameNameTable(Table sameNameTable, String tableDbName) {
        _tableMap.remove(tableDbName);
        arrangeSameNameTable(sameNameTable, tableDbName);
    }

    protected void arrangeSameNameTable(Table table, String tableDbName) {
        if (tableDbName.contains(".")) { // e.g. PUBLIC.MEMBER
            // basically no way e.g. when driven-schema but found same-name
            table.markSameSchemaSameNameTableExists();
        } else { // pure name e.g. MEMBER
            table.markSameNameTableExists();
        }
        _tableMap.put(table.getTableDbName(), table);
    }

    /**
     * Initialize detail points after loading as final process.
     */
    public void doFinalInitialization() {
        final List<Table> tableList = getTableList();
        for (Table table : tableList) {
            table.doFinalInitialization();

            // setup reverse relations and check existences
            final List<ForeignKey> fkList = table.getForeignKeyList();
            for (ForeignKey fk : fkList) {
                final Table foreignTable;
                try {
                    // check an existence of foreign table
                    foreignTable = fk.getForeignTable();
                } catch (DfTableNotFoundException e) { // may be except table generate-only
                    table.removeForeignKey(fk);
                    continue;
                }

                // adjust reverse relation
                final List<ForeignKey> refererList = foreignTable.getRefererList();
                final boolean canBeReferrer;
                if ((refererList == null || !refererList.contains(fk))) {
                    canBeReferrer = foreignTable.addReferrer(fk);
                } else {
                    canBeReferrer = false;
                }

                // local column references
                final List<String> localColumnNameList = fk.getLocalColumnNameList();
                for (String localColumnName : localColumnNameList) {
                    final Column localColumn = table.getColumn(localColumnName);
                    // give notice of a schema inconsistency.
                    // note we do not prevent the npe as there is nothing
                    // that we can do, if it is to occur.
                    if (localColumn == null) {
                        throwForeignKeyLocalColumnNotFoundException(table, fk, localColumn);
                    }
                    // column has no information of its foreign keys
                }

                // foreign column references
                final List<String> foreignColumnNameList = fk.getForeignColumnNameList();
                for (String foreignColumnName : foreignColumnNameList) {
                    final Column foreignColumn = foreignTable.getColumn(foreignColumnName);
                    // if the foreign column does not exist, we may have an
                    // external reference or a misspelling
                    if (foreignColumn == null) {
                        throwForeignKeyForeignColumnNotFoundException(table, fk, foreignColumn);
                    } else {
                        if (canBeReferrer) {
                            foreignColumn.addReferrer(fk);
                        }
                    }
                }
            }
        }
    }

    protected void throwForeignKeyLocalColumnNotFoundException(Table table, ForeignKey fk, Column localColumn) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the local column of the foreign key in the table.");
        br.addItem("Foreign Key");
        br.addElement(fk.getName());
        br.addItem("Local Table");
        br.addElement(table.getTableDbName());
        br.addItem("Foreign Table");
        br.addElement(fk.getForeignTableDbName());
        br.addItem("Local Column");
        br.addElement(localColumn.getName());
        final String msg = br.buildExceptionMessage();
        throw new DfColumnNotFoundException(msg);
    }

    protected void throwForeignKeyForeignColumnNotFoundException(Table table, ForeignKey fk, Column foreignColumn) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the foreign column of the foreign key in the table.");
        br.addItem("Foreign Key");
        br.addElement(fk.getName());
        br.addItem("Local Table");
        br.addElement(table.getTableDbName());
        br.addItem("Foreign Table");
        br.addElement(fk.getForeignTableDbName());
        br.addItem("Foreign Column");
        br.addElement(foreignColumn.getName());
        final String msg = br.buildExceptionMessage();
        throw new DfColumnNotFoundException(msg);
    }

    // -----------------------------------------------------
    //                                         Determination
    //                                         -------------
    public boolean hasTableComment() { // means resolved comment (not plain)
        for (Table table : getTableList()) {
            if (table.hasComment()) {
                return true;
            }
        }
        return false;
    }

    // ===================================================================================
    //                                                                            Sequence
    //                                                                            ========
    public void markSequenceGroup() {
        _sequenceGroupMarked = true;
    }

    public boolean hasSequenceGroup() {
        return _sequenceGroupMarked;
    }

    public List<Sequence> getSequenceList() {
        return _sequenceList;
    }

    public Sequence getSequenceByPureName(Sequence sequence) {
        return _sequencePureNameMap.get(sequence.getSequenceName());
    }

    public Sequence getSequenceByUniqueName(Sequence sequence) {
        return _sequenceUniqueNameMap.get(sequence.getFormalUniqueName());
    }

    public Sequence addSequence(Attributes attrib, XmlReadingFilter readingFilter) {
        final Sequence seq = new Sequence();
        seq.setDatabase(this);
        if (!seq.loadFromXML(attrib, readingFilter)) {
            return null;
        }
        addSequence(seq);
        return seq;
    }

    public void addSequence(Sequence seq) {
        seq.setDatabase(this);
        _sequenceList.add(seq);
        _sequencePureNameMap.put(seq.getSequenceName(), seq);
        _sequenceUniqueNameMap.put(seq.getFormalUniqueName(), seq);
    }

    // ===================================================================================
    //                                                                      Parameter Bean
    //                                                                      ==============
    protected DfPmbGenerationHandler _pmbBasicHandler;

    protected DfPmbGenerationHandler getPmbBasicHandler() {
        if (_pmbBasicHandler != null) {
            return _pmbBasicHandler;
        }
        _pmbBasicHandler = new DfPmbGenerationHandler(_pmbMetaDataMap);
        return _pmbBasicHandler;
    }

    // -----------------------------------------------------
    //                                              MetaData
    //                                              --------
    public Collection<DfPmbMetaData> getPmbMetaDataList() {
        return getPmbBasicHandler().getPmbMetaDataList();
    }

    public boolean isExistPmbMetaData() {
        return getPmbBasicHandler().isExistPmbMetaData();
    }

    public String getPmbMetaDataBusinessName(String className) {
        return getPmbBasicHandler().getBusinessName(className);
    }

    public String getPmbMetaDataAbstractDefinition(String className) {
        return getPmbBasicHandler().getAbstractDefinition(className);
    }

    public String getPmbMetaDataSuperClassDefinition(String className) {
        return getPmbBasicHandler().getSuperClassDefinition(className);
    }

    public String getPmbMetaDataInterfaceDefinition(String className) {
        return getPmbBasicHandler().getInterfaceDefinition(className);
    }

    public boolean hasPmbMetaDataPagingExtension(String className) {
        return getPmbBasicHandler().hasPagingExtension(className);
    }

    public boolean hasPmbMetaDataCheckSafetyResult(String className) {
        return getPmbBasicHandler().hasPmbMetaDataCheckSafetyResult(className);
    }

    public Set<String> getPmbMetaDataPropertySet(String className) {
        return getPmbBasicHandler().getPropertySet(className);
    }

    public String getPmbMetaDataPropertyType(String className, String propertyName) {
        return getPmbBasicHandler().getPropertyType(className, propertyName);
    }

    public String getPmbMetaDataPropertyColumnName(String className, String propertyName) {
        return getPmbBasicHandler().getPropertyColumnName(className, propertyName);
    }

    public String getPmbMetaDataPropertyTypeRemovedCSharpNullable(String className, String propertyName) {
        return getPmbBasicHandler().getPropertyTypeRemovedCSharpNullable(className, propertyName);
    }

    public boolean isPmbMetaDataPropertyJavaNativeStringObject(String className, String propertyName) {
        return getPmbBasicHandler().isPmbMetaDataPropertyJavaNativeStringObject(className, propertyName);
    }

    public boolean isPmbMetaDataPropertyJavaNativeNumberObject(String className, String propertyName) {
        return getPmbBasicHandler().isPmbMetaDataPropertyJavaNativeNumberObject(className, propertyName);
    }

    public boolean isPmbMetaDataPropertyJavaNativeBooleanObject(String className, String propertyName) {
        return getPmbBasicHandler().isPmbMetaDataPropertyJavaNativeBooleanObject(className, propertyName);
    }

    // -----------------------------------------------------
    //                                            Typed Info
    //                                            ----------
    public boolean isPmbMetaDataTypedParameterBean(String className) {
        return getPmbBasicHandler().isTypedParameterBean(className);
    }

    public boolean isPmbMetaDataTypedSelectPmb(String className) {
        return getPmbBasicHandler().isTypedSelectPmb(className);
    }

    public boolean isPmbMetaDataTypedUpdatePmb(String className) {
        return getPmbBasicHandler().isTypedUpdatePmb(className);
    }

    public boolean isPmbMetaDataTypedReturnEntityPmb(String className) {
        return getPmbBasicHandler().isTypedReturnEntityPmb(className);
    }

    public boolean isPmbMetaDataTypedReturnCustomizeEntityPmb(String className) {
        return getPmbBasicHandler().isTypedReturnCustomizeEntityPmb(className);
    }

    public boolean isPmbMetaDataTypedReturnDomainEntityPmb(String className) {
        return getPmbBasicHandler().isTypedReturnDomainEntityPmb(className);
    }

    public String getPmbMetaDataBehaviorClassName(String className) {
        return getPmbBasicHandler().getBehaviorClassName(className);
    }

    public String getPmbMetaDataBehaviorQueryPath(String className) {
        return getPmbBasicHandler().getBehaviorQueryPath(className);
    }

    public String getPmbMetaDataCustomizeEntityType(String className) {
        return getPmbBasicHandler().getCustomizeEntityType(className);
    }

    public String getPmbMetaDataCustomizeEntityLineDisp(String className) {
        return getPmbBasicHandler().getCustomizeEntityLineDisp(className);
    }

    // -----------------------------------------------------
    //                                             Procedure
    //                                             ---------
    public void markProcedureGroup() {
        _procedureGroupMarked = true;
    }

    public boolean hasProcedureGroup() {
        return _procedureGroupMarked;
    }

    public List<Procedure> getProcedureList() {
        return _procedureList;
    }

    public Procedure getProcedureByPureName(Procedure procedure) {
        return _procedurePureNameMap.get(procedure.getProcedureName());
    }

    public Procedure getProcedureByUniqueName(Procedure procedure) {
        return _procedureUniqueNameMap.get(procedure.getProcedureUniqueName());
    }

    public Procedure addProcedure(Attributes attrib, XmlReadingFilter readingFilter) {
        final Procedure procedure = new Procedure();
        procedure.setDatabase(this);
        if (!procedure.loadFromXML(attrib, readingFilter)) {
            return null;
        }
        addProcedure(procedure);
        return procedure;
    }

    public void addProcedure(Procedure procedure) {
        procedure.setDatabase(this);
        _procedureList.add(procedure);
        _procedurePureNameMap.put(procedure.getProcedureName(), procedure);
        _procedureUniqueNameMap.put(procedure.getProcedureUniqueName(), procedure);
    }

    public boolean isPmbMetaDataForProcedure(String className) {
        return getPmbBasicHandler().isForProcedure(className);
    }

    public String getPmbMetaDataProcedureName(String className) {
        return getPmbBasicHandler().getProcedureName(className);
    }

    public boolean isPmbMetaDataProcedureCalledBySelect(String className) {
        return getPmbBasicHandler().isProcedureCalledBySelect(className);
    }

    public boolean isPmbMetaDataProcedureRefCustomizeEntity(String className) {
        return getPmbBasicHandler().isProcedureRefCustomizeEntity(className);
    }

    public boolean hasPmbMetaDataProcedureOverload(String className) {
        return getPmbBasicHandler().hasProcedureOverload(className);
    }

    public boolean isPmbMetaDataPropertyOptionProcedureParameterIn(String className, String propertyName) {
        return getPmbBasicHandler().isPropertyOptionProcedureParameterIn(className, propertyName);
    }

    public boolean isPmbMetaDataPropertyOptionProcedureParameterOut(String className, String propertyName) {
        return getPmbBasicHandler().isPropertyOptionProcedureParameterOut(className, propertyName);
    }

    public boolean isPmbMetaDataPropertyOptionProcedureParameterInOut(String className, String propertyName) {
        return getPmbBasicHandler().isPropertyOptionProcedureParameterInOut(className, propertyName);
    }

    public boolean isPmbMetaDataPropertyOptionProcedureParameterReturn(String className, String propertyName) {
        return getPmbBasicHandler().isPropertyOptionProcedureParameterReturn(className, propertyName);
    }

    public boolean isPmbMetaDataPropertyOptionProcedureParameterResult(String className, String propertyName) {
        return getPmbBasicHandler().isPropertyOptionProcedureParameterResult(className, propertyName);
    }

    public boolean needsPmbMetaDataProcedureParameterStringClobHandling(String className, String propertyName) {
        return getPmbBasicHandler().needsStringClobHandling(className, propertyName);
    }

    public boolean needsPmbMetaDataProcedureParameterBytesOidHandling(String className, String propertyName) {
        return getPmbBasicHandler().needsBytesOidHandling(className, propertyName);
    }

    public boolean needsPmbMetaDataProcedureParameterFixedLengthStringHandling(String className, String propertyName) {
        return getPmbBasicHandler().needsFixedLengthStringHandling(className, propertyName);
    }

    public boolean needsPmbMetaDataProcedureParameterObjectBindingBigDecimalHandling(String className,
            String propertyName) {
        return getPmbBasicHandler().needsObjectBindingBigDecimalHandling(className, propertyName);
    }

    public boolean needsPmbMetaDataProcedureParameterOracleArrayHandling(String className, String propertyName) {
        return getPmbBasicHandler().needsOracleArrayHandling(className, propertyName);
    }

    public boolean needsPmbMetaDataProcedureParameterOracleStructHandling(String className, String propertyName) {
        return getPmbBasicHandler().needsOracleStructHandling(className, propertyName);
    }

    public String getPmbMetaDataProcedureParameterOracleArrayTypeName(String className, String propertyName) {
        return getPmbBasicHandler().getProcedureParameterOracleArrayTypeName(className, propertyName);
    }

    public String getPmbMetaDataProcedureParameterOracleArrayElementJavaNative(String className, String propertyName) {
        return getPmbBasicHandler().getProcedureParameterOracleArrayElementJavaNative(className, propertyName);
    }

    public String getPmbMetaDataProcedureParameterOracleArrayElementJavaNativeTypeLiteral(String className,
            String propertyName) {
        return getPmbBasicHandler().getProcedureParameterOracleArrayElementJavaNativeTypeLiteral(className,
                propertyName);
    }

    public String getPmbMetaDataProcedureParameterOracleStructTypeName(String className, String propertyName) {
        return getPmbBasicHandler().getProcedureParameterOracleStructTypeName(className, propertyName);
    }

    public String getPmbMetaDataProcedureParameterOracleStructEntityType(String className, String propertyName) {
        return getPmbBasicHandler().getProcedureParameterOracleStructEntityType(className, propertyName);
    }

    public String getPmbMetaDataProcedureParameterOracleStructEntityTypeTypeLiteral(String className,
            String propertyName) {
        return getPmbBasicHandler().getProcedureParameterOracleStructEntityTypeTypeLiteral(className, propertyName);
    }

    // -----------------------------------------------------
    //                                                Option
    //                                                ------
    public boolean hasPmbMetaDataPropertyOptionOriginalOnlyOneSetter(String className, String propertyName) {
        return getPmbBasicHandler().hasPropertyOptionOriginalOnlyOneSetter(className, propertyName,
                _sql2entitySchemaData);
    }

    public boolean hasPmbMetaDataPropertyUseOriginalException(String className) {
        return hasPmbMetaDataPropertyOptionAnyLikeSearch(className) || hasPmbMetaDataPropertyOptionAnyFromTo(className);
    }

    // -----------------------------------------------------
    //                                    Option LikeSeasrch
    //                                    ------------------
    public boolean hasPmbMetaDataPropertyOptionAnyLikeSearch(String className) {
        return getPmbBasicHandler().hasPropertyOptionAnyLikeSearch(className);
    }

    public boolean hasPmbMetaDataPropertyOptionAnyLikeSearch(String className, String propertyName) {
        return getPmbBasicHandler().hasPropertyOptionAnyLikeSearch(className, propertyName);
    }

    public boolean isPmbMetaDataPropertyOptionLikeSearch(String className, String propertyName) {
        return getPmbBasicHandler().isPropertyOptionLikeSearch(className, propertyName);
    }

    public boolean isPmbMetaDataPropertyOptionPrefixSearch(String className, String propertyName) {
        return getPmbBasicHandler().isPropertyOptionPrefixSearch(className, propertyName);
    }

    public boolean isPmbMetaDataPropertyOptionContainSearch(String className, String propertyName) {
        return getPmbBasicHandler().isPropertyOptionContainSearch(className, propertyName);
    }

    public boolean isPmbMetaDataPropertyOptionSuffixSearch(String className, String propertyName) {
        return getPmbBasicHandler().isPropertyOptionSuffixSearch(className, propertyName);
    }

    // -----------------------------------------------------
    //                                         Option FromTo
    //                                         -------------
    public boolean hasPmbMetaDataPropertyOptionAnyFromTo(String className) {
        return getPmbBasicHandler().hasPropertyOptionAnyFromTo(className);
    }

    public boolean hasPmbMetaDataPropertyOptionAnyFromTo(String className, String propertyName) {
        return getPmbBasicHandler().hasPropertyOptionAnyFromTo(className, propertyName);
    }

    public boolean isPmbMetaDataPropertyOptionFromDate(String className, String propertyName) {
        return getPmbBasicHandler().isPropertyOptionFromDate(className, propertyName);
    }

    public boolean isPmbMetaDataPropertyOptionFromDateOption(String className, String propertyName) {
        return getPmbBasicHandler().isPropertyOptionFromDateOption(className, propertyName);
    }

    public boolean isPmbMetaDataPropertyOptionToDate(String className, String propertyName) {
        return getPmbBasicHandler().isPropertyOptionToDate(className, propertyName);
    }

    public boolean isPmbMetaDataPropertyOptionToDateOption(String className, String propertyName) {
        return getPmbBasicHandler().isPropertyOptionToDateOption(className, propertyName);
    }

    // -----------------------------------------------------
    //                                 Option Classification
    //                                 ---------------------
    public boolean isPmbMetaDataPropertyOptionClassification(String className, String propertyName) {
        return getPmbBasicHandler().isPropertyOptionClassification(className, propertyName, _sql2entitySchemaData);
    }

    public boolean isPmbMetaDataPropertyOptionClassificationSetter(String className, String propertyName) {
        return getPmbBasicHandler()
                .isPropertyOptionClassificationSetter(className, propertyName, _sql2entitySchemaData);
    }

    public boolean isPmbMetaDataPropertyOptionClassificationFixedElement(String className, String propertyName) {
        return getPmbBasicHandler().isPropertyOptionClassificationFixedElement(className, propertyName);
    }

    public boolean isPmbMetaDataPropertyOptionClassificationFixedElementList(String className, String propertyName) {
        return getPmbBasicHandler().isPropertyOptionClassificationFixedElementList(className, propertyName);
    }

    public String getPmbMetaDataPropertyOptionClassificationName(String className, String propertyName) {
        return getPmbBasicHandler().getPropertyOptionClassificationName(className, propertyName, _sql2entitySchemaData);
    }

    public String getPmbMetaDataPropertyOptionClassificationFixedElementValueExp(String className, String propertyName) {
        return getPmbBasicHandler().getPropertyOptionClassificationFixedElementValueExp(className, propertyName);
    }

    public DfClassificationTop getPmbMetaDataPropertyOptionClassificationTop(String className, String propertyName) {
        return getPmbBasicHandler().getPropertyOptionClassificationTop(className, propertyName, _sql2entitySchemaData);
    }

    public String getPmbMetaDataPropertyOptionClassificationSettingElementValueExp(String className,
            String propertyName, String element) {
        return getPmbBasicHandler().getPropertyOptionClassificationSettingElementValueExp(className, propertyName,
                element, _sql2entitySchemaData);
    }

    // -----------------------------------------------------
    //                                        Option Comment
    //                                        --------------
    public boolean hasPropertyOptionComment(String className, String propertyName) {
        return getPmbBasicHandler().hasPropertyOptionComment(className, propertyName);
    }

    public String getPropertyOptionComment(String className, String propertyName) {
        return getPmbBasicHandler().getPropertyOptionComment(className, propertyName);
    }

    // -----------------------------------------------------
    //                              Alternate Boolean Method
    //                              ------------------------
    public boolean existsPmbMetaDataAlternateBooleanMethodNameSet(String className) {
        return getPmbBasicHandler().existsAlternateBooleanMethodNameSet(className);
    }

    public Set<String> getPmbMetaDataAlternateBooleanMethodNameSet(String className) {
        return getPmbBasicHandler().getAlternateBooleanMethodNameSet(className);
    }

    // -----------------------------------------------------
    //                                               Display
    //                                               -------
    public String getPmbMetaDataPropertyRefColumnInfo(String className, String propertyName) {
        try {
            final DfPmbGenerationHandler handler = getPmbBasicHandler();
            return handler.getPropertyRefColumnInfo(className, propertyName, _sql2entitySchemaData);
        } catch (RuntimeException e) { // just in case
            String msg = "Failed to get ref-column info:";
            msg = msg + " " + className + "." + propertyName;
            _log.debug(msg, e);
            throw e;
        }
    }

    public boolean isPmbMetaDataPropertyRefColumnChar(String className, String propertyName) {
        return getPmbBasicHandler().isPropertyRefColumnChar(className, propertyName, _sql2entitySchemaData);
    }

    public String getPmbMetaDataPropertyRefSize(String className, String propertyName) {
        return getPmbBasicHandler().getPropertyRefSize(className, propertyName, _sql2entitySchemaData);
    }

    // ===================================================================================
    //                                                                         Initializer
    //                                                                         ===========
    // -----------------------------------------------------
    //                                  AdditionalPrimaryKey
    //                                  --------------------
    public void initializeAdditionalPrimaryKey() {
        final DfAdditionalPrimaryKeyInitializer initializer = new DfAdditionalPrimaryKeyInitializer(this);
        initializer.initializeAdditionalPrimaryKey();
    }

    // -----------------------------------------------------
    //                                   AdditionalUniqueKey
    //                                   -------------------
    public void initializeAdditionalUniqueKey() {
        final DfAdditionalUniqueKeyInitializer initializer = new DfAdditionalUniqueKeyInitializer(this);
        initializer.initializeAdditionalUniqueKey();
    }

    // -----------------------------------------------------
    //                                  AdditionalForeignKey
    //                                  --------------------
    /**
     * Initialize additional foreign key. <br />
     * This is basically for Generate task. (Not Sql2Entity)
     */
    public void initializeAdditionalForeignKey() {
        // /- - - - - - - - - - - - - - - - -
        // Initialize additional foreign key
        // - - - - - - - - - -/
        final DfAdditionalForeignKeyInitializer initializer = new DfAdditionalForeignKeyInitializer(this);
        initializer.initializeAdditionalForeignKey();
    }

    // -----------------------------------------------------
    //                              ClassificationDeployment
    //                              ------------------------
    public void initializeClassificationDeployment() {
        final DfClassificationProperties clsProp = getClassificationProperties();

        // Initialize classification definition before initializing deployment.
        clsProp.initializeClassificationDefinition(); // Together!

        // Initialize current target database.
        clsProp.initializeClassificationDeployment(this);

        // If this is in sql2entity task, initialize schema database.
        if (_sql2entitySchemaData != null) {
            clsProp.initializeClassificationDeployment(_sql2entitySchemaData.getDatabase());
        }
    }

    // -----------------------------------------------------
    //                                          IncludeQuery
    //                                          ------------
    public void initializeIncludeQuery() {
        DfIncludeQueryInitializer initializer = new DfIncludeQueryInitializer();
        initializer.setIncludeQueryProperties(getProperties().getIncludeQueryProperties());
        initializer.setTableFinder(new DfTableFinder() {
            public Table findTable(String name) {
                return getTable(name);
            }
        });
        initializer.initializeIncludeQuery();
    }

    // ===================================================================================
    //                                                                    Check Properties
    //                                                                    ================
    /**
     * Check properties as mutually related validation. <br />
     * This is basically for Generate task. (Not Sql2Entity)
     */
    public void checkProperties() {
        final DfBuildProperties prop = getProperties();
        final DfTableListProvider tableListProvider = new DfTableListProvider() {
            public List<Table> provideTableList() {
                return getTableList();
            }
        };
        prop.getCommonColumnProperties().checkDefinition(tableListProvider);
        prop.getOptimisticLockProperties().checkDefinition(tableListProvider);

        final DfTableDeterminer tableDeterminer = new DfTableDeterminer() {
            public boolean hasTable(String tableName) {
                return getTable(tableName) != null;
            }

            public boolean hasTableColumn(String tableName, String columnName) {
                if (!hasTable(tableName)) {
                    return false;
                }
                return getTable(tableName).getColumn(columnName) != null;
            }
        };
        // give up because all mark provides complex structure and also for Sql2Entity case
        // so cost-benefit performance is low (to suppress degrading is prior)
        //prop.getClassificationProperties().checkProperties(tableDeterminer);
        prop.getSequenceIdentityProperties().checkDefinition(tableDeterminer);

        getBasicProperties().checkDirectoryPackage();
    }

    // ===================================================================================
    //                                                              Delete Old Table Class
    //                                                              ======================
    public void deleteOldTableClass() {
        if (isSuppressDeleteOldClass()) {
            return;
        }
        final DfOldClassHandler handler = createOldClassHandler();
        handler.deleteOldTableClass();
    }

    public void deleteOldCustomizeClass() {
        if (isSuppressDeleteOldClass()) {
            return;
        }
        final DfOldClassHandler handler = createOldClassHandler();
        handler.setCustomizeTableList(getTableList());
        handler.setPmbMetaDataMap(_pmbMetaDataMap);
        handler.deleteOldCustomizeClass();
    }

    public void deleteOldSimpleDtoTableClass() {
        if (isSuppressDeleteOldClass()) {
            return;
        }
        final DfOldClassHandler handler = createOldClassHandler();
        handler.deleteOldSimpleDtoTableClass();
    }

    public void deleteOldSimpleDtoMapperTableClass() {
        if (isSuppressDeleteOldClass()) {
            return;
        }
        final DfOldClassHandler handler = createOldClassHandler();
        handler.deleteOldSimpleDtoMapperTableClass();
    }

    public void deleteOldSimpleDtoCustomizeClass() {
        if (isSuppressDeleteOldClass()) {
            return;
        }
        final DfOldClassHandler handler = createOldClassHandler();
        handler.setCustomizeTableList(getTableList());
        handler.setPmbMetaDataMap(_pmbMetaDataMap);
        handler.deleteOldSimpleDtoCustomizeClass();
    }

    public void deleteOldSimpleDtoMapperCustomizeClass() {
        if (isSuppressDeleteOldClass()) {
            return;
        }
        final DfOldClassHandler handler = createOldClassHandler();
        handler.setCustomizeTableList(getTableList());
        handler.setPmbMetaDataMap(_pmbMetaDataMap);
        handler.deleteOldSimpleDtoMapperCustomizeClass();
    }

    protected boolean isSuppressDeleteOldClass() {
        return _skipDeleteOldClass || !isDeleteOldTableClass();
    }

    protected DfOldClassHandler createOldClassHandler() {
        final DfGenerator generator = getGeneratorInstance();
        final DfLanguageDependency lang = getBasicProperties().getLanguageDependency();
        final DfLanguageClassPackage pkg = lang.getLanguageClassPackage();
        return new DfOldClassHandler(generator, pkg, getTableList());
    }

    // ===================================================================================
    //                                                                    Output Directory
    //                                                                    ================
    public void enableGenerateOutputDirectory() {
        doEnableGenerateOutputDirectory(true);
    }

    public void backToGenerateOutputDirectory() {
        doEnableGenerateOutputDirectory(false);
    }

    protected void doEnableGenerateOutputDirectory(boolean logging) {
        final String outputDirectory = getProperties().getBasicProperties().getGenerateOutputDirectory();
        if (logging) {
            _log.info("...Setting up generateOutputDirectory: " + outputDirectory);
        }
        getGeneratorInstance().setOutputPath(outputDirectory);
    }

    public void enableSql2EntityOutputDirectory() {
        doEnableSql2EntityOutputDirectory(true);
    }

    public void backToSql2EntityOutputDirectory() {
        doEnableSql2EntityOutputDirectory(false);
    }

    protected void doEnableSql2EntityOutputDirectory(boolean logging) {
        final String outputDirectory = getProperties().getOutsideSqlProperties().getSql2EntityOutputDirectory();
        if (logging) {
            _log.info("...Setting up sql2EntityOutputDirectory: " + outputDirectory);
        }
        getGeneratorInstance().setOutputPath(outputDirectory);
    }

    public void enableDocumentOutputDirectory() {
        doEnableDocumentOutputDirectory(true);
    }

    public void backToDocumentOutputDirectory() {
        doEnableDocumentOutputDirectory(false);
    }

    protected void doEnableDocumentOutputDirectory(boolean logging) {
        final String outputDirectory = getProperties().getDocumentProperties().getDocumentOutputDirectory();
        if (logging) {
            _log.info("...Setting up documentOutputDirectory: " + outputDirectory);
        }
        final File dir = new File(outputDirectory);
        if (!dir.exists()) {
            if (logging) {
                _log.info("...Making directories for documentOutputDirectory: " + dir);
            }
            dir.mkdirs(); // because this directory is NOT user setting basically
        }
        getGeneratorInstance().setOutputPath(outputDirectory);
    }

    public void enableMigrationOutputDirectory() {
        final String outputDirectory = getProperties().getReplaceSchemaProperties().getMigrationSchemaDirectory();
        _log.info("...Setting up migrationOutputDirectory: " + outputDirectory);
        final File dir = new File(outputDirectory);
        if (!dir.exists()) {
            _log.info("...Making directories for migrationOutputDirectory: " + dir);
            dir.mkdirs(); // because this directory is NOT user setting basically
        }
        getGeneratorInstance().setOutputPath(outputDirectory);
    }

    public void enableSimpleDtoOutputDirectory() {
        final String outputDirectory = getProperties().getSimpleDtoProperties().getSimpleDtoOutputDirectory();
        _log.info("...Setting up simpleDtoOutputDirectory: " + outputDirectory);
        getGeneratorInstance().setOutputPath(outputDirectory);
    }

    public void enableDtoMapperOutputDirectory() {
        final String outputDirectory = getProperties().getSimpleDtoProperties().getDtoMapperOutputDirectory();
        _log.info("...Setting up dtoMapperOutputDirectory: " + outputDirectory);
        getGeneratorInstance().setOutputPath(outputDirectory);
    }

    public void enableSimpleCDefOutputDirectory() {
        final String outputDirectory = getProperties().getSimpleDtoProperties().getSimpleCDefOutputDirectory();
        _log.info("...Setting up simpleCDefOutputDirectory: " + outputDirectory);
        getGeneratorInstance().setOutputPath(outputDirectory);
    }

    public void enableFlexDtoOutputDirectory() {
        final String outputDirectory = getProperties().getFlexDtoProperties().getOutputDirectory();
        _log.info("...Setting up flexDtoOutputDirectory: " + outputDirectory);
        getGeneratorInstance().setOutputPath(outputDirectory);
    }

    // ===================================================================================
    //                                                                           Generator
    //                                                                           =========
    public DfGenerator getGeneratorInstance() {
        return DfGenerator.getInstance();
    }

    //====================================================================================
    //                                                               Database Name Mapping
    //                                                               =====================
    public String getDefaultDBDef() { // for DBCurrent
        return getBasicProperties().getCurrentDBDef().code();
    }

    public String getGenerateDbName() { // for Class Name
        return DfDatabaseNameMapping.getInstance().findGenerateName(getDatabaseType());
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    // /- - - - - - - - - - - - - - - - - - - - - - - -
    // basically return types of property methods are
    // String or boolean or List (not Number and Date)
    // because Velocity templates use them.
    // - - - - - - - - - -/

    // ===================================================================================
    //                                                                    Basic Properties
    //                                                                    ================
    protected DfBasicProperties getBasicProperties() {
        return getProperties().getBasicProperties();
    }

    // -----------------------------------------------------
    //                                               Project
    //                                               -------
    public String getProjectName() {
        return getBasicProperties().getProjectName();
    }

    public boolean isApplicationBehaviorProject() {
        return getBasicProperties().isApplicationBehaviorProject();
    }

    // -----------------------------------------------------
    //                                              Database
    //                                              --------
    public String getDatabaseName() {
        return getBasicProperties().getTargetDatabase();
    }

    public boolean isDatabaseMySQL() {
        return getBasicProperties().isDatabaseMySQL();
    }

    public boolean isDatabasePostgreSQL() {
        return getBasicProperties().isDatabasePostgreSQL();
    }

    public boolean isDatabaseOracle() {
        return getBasicProperties().isDatabaseOracle();
    }

    public boolean isDatabaseDB2() {
        return getBasicProperties().isDatabaseDB2();
    }

    public boolean isDatabaseSQLServer() {
        return getBasicProperties().isDatabaseSQLServer();
    }

    public boolean isDatabaseDerby() {
        return getBasicProperties().isDatabaseDerby();
    }

    public boolean isDatabaseH2() {
        return getBasicProperties().isDatabaseH2();
    }

    public boolean isDatabaseMSAccess() {
        return getBasicProperties().isDatabaseMSAccess();
    }

    public boolean isDatabaseSybase() {
        return getBasicProperties().isDatabaseSybase();
    }

    // -----------------------------------------------------
    //                                              Language
    //                                              --------
    public String getTargetLanguage() {
        return getBasicProperties().getTargetLanguage();
    }

    public String getResourceDirectory() {
        return getBasicProperties().getResourceDirectory();
    }

    public String getTargetLanguageInitCap() {
        final String targetLanguage = getBasicProperties().getTargetLanguage();
        return targetLanguage.substring(0, 1).toUpperCase() + targetLanguage.substring(1);
    }

    // -----------------------------------------------------
    //                                             Container
    //                                             ---------
    public String getTargetContainerName() {
        return getBasicProperties().getTargetContainerName();
    }

    public boolean isTargetContainerSeasar() {
        return getBasicProperties().isTargetContainerSeasar();
    }

    public boolean isTargetContainerSpring() {
        return getBasicProperties().isTargetContainerSpring();
    }

    public boolean isTargetContainerLucy() {
        return getBasicProperties().isTargetContainerLucy();
    }

    public boolean isTargetContainerGuice() {
        return getBasicProperties().isTargetContainerGuice();
    }

    public boolean isTargetContainerSlim3() {
        return getBasicProperties().isTargetContainerSlim3();
    }

    public boolean isTargetContainerCDI() {
        return getBasicProperties().isTargetContainerCDI();
    }

    // -----------------------------------------------------
    //                                             Extension
    //                                             ---------
    public String getTemplateFileExtension() {
        return getBasicProperties().getTemplateFileExtension();
    }

    public String getClassFileExtension() {
        return getBasicProperties().getClassFileExtension();
    }

    // -----------------------------------------------------
    //                                              Encoding
    //                                              --------
    public String getTemplateFileEncoding() {
        return getBasicProperties().getTemplateFileEncoding();
    }

    // -----------------------------------------------------
    //                                               JavaDir
    //                                               -------
    public String getJavaDir() {
        return getBasicProperties().getGenerateOutputDirectory();
    }

    // -----------------------------------------------------
    //                                                Author
    //                                                ------
    public String getClassAuthor() {
        return getBasicProperties().getClassAuthor();
    }

    // -----------------------------------------------------
    //                                             Copyright
    //                                             ---------
    public String getAllClassCopyright() {
        return getProperties().getAllClassCopyrightProperties().getAllClassCopyright();
    }

    public void reflectAllExCopyright(String path) {
        final String outputPath = DfGenerator.getInstance().getOutputPath();
        final String absolutePath = outputPath + "/" + path;
        final String sourceCodeEncoding = getTemplateFileEncoding();
        final String sourceCodeLn = getBasicProperties().getSourceCodeLineSeparator();
        final DfCopyrightResolver resolver = new DfCopyrightResolver(sourceCodeEncoding, sourceCodeLn);
        final String copyright = getProperties().getAllClassCopyrightProperties().getAllClassCopyright();
        resolver.reflectAllExCopyright(absolutePath, copyright);
    }

    // -----------------------------------------------------
    //                                         Prefix/Suffix
    //                                         -------------
    public String getProjectPrefix() {
        return getBasicProperties().getProjectPrefix();
    }

    public String getBasePrefix() {
        return getBasicProperties().getBasePrefix();
    }

    public String getBaseSuffixForEntity() {
        return "";
    }

    // -----------------------------------------------------
    //                                      Generate Package
    //                                      ----------------
    public String getPackageBase() {
        return getProperties().getBasicProperties().getPackageBase();
    }

    public String getBaseCommonPackage() {
        return getProperties().getBasicProperties().getBaseCommonPackage();
    }

    public String getBaseBehaviorPackage() {
        return getProperties().getBasicProperties().getBaseBehaviorPackage();
    }

    public String getReferrerLoaderPackage() {
        return getProperties().getBasicProperties().getReferrerLoaderPackage();
    }

    public String getBaseDaoPackage() {
        return getProperties().getBasicProperties().getBaseDaoPackage();
    }

    public String getBaseEntityPackage() {
        return getProperties().getBasicProperties().getBaseEntityPackage();
    }

    public String getDBMetaPackage() {
        return getProperties().getBasicProperties().getDBMetaPackage();
    }

    public String getConditionBeanPackage() {
        return getProperties().getBasicProperties().getConditionBeanPackage();
    }

    public String getExtendedConditionBeanPackage() {
        return getProperties().getBasicProperties().getExtendedConditionBeanPackage();
    }

    public String getExtendedBehaviorPackage() {
        return getProperties().getBasicProperties().getExtendedBehaviorPackage();
    }

    public String getExtendedDaoPackage() {
        return getProperties().getBasicProperties().getExtendedDaoPackage();
    }

    public String getExtendedEntityPackage() {
        return getProperties().getBasicProperties().getExtendedEntityPackage();
    }

    public String getLibraryAllCommonPackage() { // for Application Behavior
        return getBasicProperties().getLibraryAllCommonPackage();
    }

    public String getLibraryBehaviorPackage() { // for Application Behavior
        return getBasicProperties().getLibraryBehaviorPackage();
    }

    public String getLibraryEntityPackage() { // for Application Behavior
        return getBasicProperties().getLibraryEntityPackage();
    }

    public String getLibraryProjectPrefix() { // for Application Behavior
        return getBasicProperties().getLibraryProjectPrefix();
    }

    public String getApplicationAllCommonPackage() { // for Application Behavior
        return getBasicProperties().getApplicationAllCommonPackage();
    }

    // -----------------------------------------------------
    //                                             Flat/Omit
    //                                             ---------
    // CSharp Only
    public boolean isFlatDirectoryPackageValid() {
        return getProperties().getBasicProperties().isFlatDirectoryPackageValid();
    }

    public String getFlatDirectoryPackage() {
        return getProperties().getBasicProperties().getFlatDirectoryPackage();
    }

    public boolean isOmitDirectoryPackageValid() {
        return getProperties().getBasicProperties().isOmitDirectoryPackageValid();
    }

    public String getOmitDirectoryPackage() {
        return getProperties().getBasicProperties().getOmitDirectoryPackage();
    }

    // -----------------------------------------------------
    //                                        Begin/End Mark
    //                                        --------------
    public String getBehaviorQueryPathBeginMark() {
        return getBasicProperties().getBehaviorQueryPathBeginMark();
    }

    public String getBehaviorQueryPathEndMark() {
        return getBasicProperties().getBehaviorQueryPathEndMark();
    }

    public String getExtendedClassDescriptionBeginMark() {
        return getBasicProperties().getExtendedClassDescriptionBeginMark();
    }

    public String getExtendedClassDescriptionEndMark() {
        return getBasicProperties().getExtendedClassDescriptionEndMark();
    }

    // -----------------------------------------------------
    //                                    Serial Version UID
    //                                    ------------------
    public void reflectAllExSerialVersionUID(String path) {
        // basically for parameter-bean
        // because it has become to need it since 0.9.7.0
        // (supported classes since older versions don't need this)
        //  -> not called because it has become NOT to need it since 0.9.9.6
        final String outputPath = DfGenerator.getInstance().getOutputPath();
        final String absolutePath = outputPath + "/" + path;
        final String sourceCodeEncoding = getTemplateFileEncoding();
        final String sourceCodeLn = getBasicProperties().getSourceCodeLineSeparator();
        final DfSerialVersionUIDResolver resolver = new DfSerialVersionUIDResolver(sourceCodeEncoding, sourceCodeLn);
        resolver.reflectAllExSerialUID(absolutePath);
    }

    // ===================================================================================
    //                                                                 Database Properties
    //                                                                 ===================
    protected DfDatabaseProperties getDatabaseProperties() {
        return getProperties().getDatabaseProperties();
    }

    public UnifiedSchema getDatabaseSchema() {
        return getDatabaseProperties().getDatabaseSchema();
    }

    public boolean hasDatabaseSchema() {
        return getDatabaseSchema().hasSchema();
    }

    public boolean hasAdditionalSchema() {
        return getDatabaseProperties().hasAdditionalSchema();
    }

    public boolean hasCatalogAdditionalSchema() {
        return getDatabaseProperties().hasCatalogAdditionalSchema();
    }

    // ===================================================================================
    //                                                                Dependency Injection
    //                                                                ====================
    // -----------------------------------------------------
    //                                                Seasar
    //                                                ------
    public String getDBFluteDiconNamespace() {
        return getProperties().getDependencyInjectionProperties().getDBFluteDiconNamespace();
    }

    public List<String> getDBFluteDiconPackageNameList() {
        final String resourceOutputDirectory = getBasicProperties().getResourceOutputDirectory();
        if (resourceOutputDirectory != null) {
            final List<String> resulList = new ArrayList<String>();
            resulList.add(resourceOutputDirectory);
            return resulList;
        }

        // for compatibility and default value
        final List<String> diconPackageNameList = getProperties().getDependencyInjectionProperties()
                .getDBFluteDiconPackageNameList();
        if (diconPackageNameList != null && !diconPackageNameList.isEmpty()) {
            return diconPackageNameList;
        } else {
            final List<String> resulList = new ArrayList<String>();
            resulList.add(getBasicProperties().getDefaultResourceOutputDirectory());
            return resulList;
        }
    }

    public String getDBFluteCreatorDiconFileName() {
        return getProperties().getDependencyInjectionProperties().getDBFluteCreatorDiconFileName();
    }

    public String getDBFluteCustomizerDiconFileName() {
        return getProperties().getDependencyInjectionProperties().getDBFluteCustomizerDiconFileName();
    }

    public String getDBFluteDiconFileName() {
        return getProperties().getDependencyInjectionProperties().getDBFluteDiconFileName();
    }

    public String getJ2eeDiconResourceName() {
        return getProperties().getDependencyInjectionProperties().getJ2eeDiconResourceName();
    }

    public List<String> getDBFluteDiconBeforeJ2eeIncludePathList() {
        return getProperties().getDependencyInjectionProperties().getDBFluteDiconBeforeJ2eeIncludePathList();
    }

    public List<String> getDBFluteDiconOtherIncludePathList() {
        return getProperties().getDependencyInjectionProperties().getDBFluteDiconOtherIncludePathList();
    }

    public boolean isSuppressDiconBehaviorDefinition() {
        return getProperties().getDependencyInjectionProperties().isSuppressDiconBehaviorDefinition();
    }

    public String filterDBFluteDiconBhvAp(String filePath) { // as utility for application behavior
        if (filePath.endsWith(".dicon")) {
            filePath = Srl.replace(filePath, ".dicon", "++.dicon");
        }
        return filePath;
    }

    // -----------------------------------------------------
    //                                         Spring & Lucy
    //                                         -------------
    public List<String> getDBFluteBeansPackageNameList() {
        final String resourceOutputDirectory = getBasicProperties().getResourceOutputDirectory();
        if (resourceOutputDirectory != null) {
            final List<String> resulList = new ArrayList<String>();
            resulList.add(resourceOutputDirectory);
            return resulList;
        }

        // for compatibility and default value
        final List<String> diconPackageNameList = getProperties().getDependencyInjectionProperties()
                .getDBFluteBeansPackageNameList();
        if (diconPackageNameList != null && !diconPackageNameList.isEmpty()) {
            return diconPackageNameList;
        } else {
            final List<String> resulList = new ArrayList<String>();
            resulList.add(getBasicProperties().getDefaultResourceOutputDirectory());
            return resulList;
        }
    }

    public String getDBFluteBeansFileName() {
        return getProperties().getDependencyInjectionProperties().getDBFluteBeansFileName();
    }

    public String getDBFluteBeansDataSourceName() {
        return getProperties().getDependencyInjectionProperties().getDBFluteBeansDataSourceName();
    }

    public String getDBFluteBeansDefaultAttribute() {
        return getProperties().getDependencyInjectionProperties().getDBFluteBeansDefaultAttribute();
    }

    public String filterDBFluteBeansBhvAp(String filePath) { // as utility for application behavior
        if (filePath.endsWith(".xml")) {
            filePath = Srl.replace(filePath, ".xml", "BhvAp.xml");
        }
        return filePath;
    }

    public boolean isDBFluteBeansGeneratedAsJavaConfig() {
        return getProperties().getDependencyInjectionProperties().isDBFluteBeansGeneratedAsJavaConfig();
    }

    // -----------------------------------------------------
    //                                                 Guice
    //                                                 -----
    public String getDBFluteModuleBhvApClassName() {
        return getProjectPrefix() + "DBFluteModuleBhvAp";
    }

    public String filterDBFluteModuleBhvAp(String filePath) { // as utility for application behavior
        if (filePath.endsWith(".java")) {
            filePath = Srl.replace(filePath, ".java", "BhvAp.java");
        }
        return filePath;
    }

    // -----------------------------------------------------
    //                                                 Quill
    //                                                 -----
    public boolean isQuillDataSourceNameValid() {
        return getProperties().getDependencyInjectionProperties().isQuillDataSourceNameValid();
    }

    public String getQuillDataSourceName() {
        return getProperties().getDependencyInjectionProperties().getQuillDataSourceName();
    }

    // -----------------------------------------------------
    //                                                   CDI
    //                                                   ---
    public String getMetaInfOutputDirectory() {
        String resourceOutputDirectory = getBasicProperties().getResourceOutputDirectory();
        if (resourceOutputDirectory == null) {
            resourceOutputDirectory = getBasicProperties().getDefaultResourceOutputDirectory();
        }
        return resourceOutputDirectory + "/META-INF";
    }

    // ===================================================================================
    //                                                        Sequence/Identity Properties
    //                                                        ============================
    public String getSequenceReturnType() {
        return getProperties().getSequenceIdentityProperties().getSequenceReturnType();
    }

    // ===================================================================================
    //                                                            Common Column Properties
    //                                                            ========================
    public Map<String, String> getCommonColumnMap() {
        return getProperties().getCommonColumnProperties().getCommonColumnMap();
    }

    public List<String> getCommonColumnNameList() {
        return getProperties().getCommonColumnProperties().getCommonColumnNameList();
    }

    public List<String> getCommonColumnNameConversionList() {
        return getProperties().getCommonColumnProperties().getCommonColumnNameConversionList();
    }

    public String filterCommonColumn(String commonColumnName) {
        return getProperties().getCommonColumnProperties().filterCommonColumn(commonColumnName);
    }

    public boolean hasCommonColumn() {
        return !getProperties().getCommonColumnProperties().getCommonColumnNameList().isEmpty();
    }

    public boolean isExistCommonColumnSetupElement() {
        return getProperties().getCommonColumnProperties().isExistCommonColumnSetupElement();
    }

    public boolean hasCommonColumnConvertion(String commonColumnName) {
        return getProperties().getCommonColumnProperties().isCommonColumnConversion(commonColumnName);
    }

    // -----------------------------------------------------
    //                                                insert
    //                                                ------
    public boolean hasCommonColumnBeforeInsertLogic(String columnName) {
        return getProperties().getCommonColumnProperties().hasCommonColumnBeforeInsertLogic(columnName);
    }

    public String getCommonColumnBeforeInsertLogicByColumnName(String columnName) {
        return getProperties().getCommonColumnProperties().getCommonColumnBeforeInsertLogicByColumnName(columnName);
    }

    // -----------------------------------------------------
    //                                                update
    //                                                ------
    public boolean hasCommonColumnBeforeUpdateLogic(String columnName) {
        return getProperties().getCommonColumnProperties().hasCommonColumnBeforeUpdateLogic(columnName);
    }

    public String getCommonColumnBeforeUpdateLogicByColumnName(String columnName) {
        return getProperties().getCommonColumnProperties().getCommonColumnBeforeUpdateLogicByColumnName(columnName);
    }

    // -----------------------------------------------------
    //                                              resource
    //                                              --------
    public boolean hasCommonColumnSetupResource() {
        return getProperties().getCommonColumnProperties().hasCommonColumnSetupResource();
    }

    public List<CommonColumnSetupResource> getCommonColumnSetupResourceList() {
        return getProperties().getCommonColumnProperties().getCommonColumnSetupResourceList();
    }

    // -----------------------------------------------------
    //                                        logic handling
    //                                        --------------
    public boolean isCommonColumnSetupInvokingLogic(String logic) {
        return getProperties().getCommonColumnProperties().isCommonColumnSetupInvokingLogic(logic);
    }

    public String removeCommonColumnSetupInvokingMark(String logic) {
        return getProperties().getCommonColumnProperties().removeCommonColumnSetupInvokingMark(logic);
    }

    // ===================================================================================
    //                                                           Classification Properties
    //                                                           =========================
    public DfClassificationProperties getClassificationProperties() {
        return getProperties().getClassificationProperties();
    }

    // -----------------------------------------------------
    //                                            Definition
    //                                            ----------
    public boolean hasClassificationDefinition() {
        return getClassificationProperties().hasClassificationDefinition();
    }

    public List<String> getClassificationNameList() {
        return getClassificationProperties().getClassificationNameList();
    }

    public boolean hasClassificationTop(String classificationName) {
        return getClassificationProperties().hasClassificationTop(classificationName);
    }

    public DfClassificationTop getClassificationTop(String classificationName) {
        return getClassificationProperties().getClassificationTop(classificationName);
    }

    public String buildClassificationApplicationComment(DfClassificationElement classificationElement) {
        return getClassificationProperties().buildClassificationApplicationComment(classificationElement);
    }

    public String buildClassificationApplicationCommentForJavaDoc(DfClassificationElement classificationElement) {
        return getClassificationProperties().buildClassificationApplicationCommentForJavaDoc(classificationElement);
    }

    public String buildClassificationApplicationCommentForSchemaHtml(DfClassificationElement classificationElement) {
        return getClassificationProperties().buildClassificationApplicationCommentForSchemaHtml(classificationElement);
    }

    public String buildClassificationCodeAliasVariables(DfClassificationElement classificationElement) {
        return getClassificationProperties().buildClassificationCodeAliasVariables(classificationElement);
    }

    public String buildClassificationCodeAliasSisterCodeVariables(DfClassificationElement classificationElement) {
        return getClassificationProperties().buildClassificationCodeAliasSisterCodeVariables(classificationElement);
    }

    public String buildClassificationCodeNameAliasVariables(DfClassificationElement classificationElement) {
        return getClassificationProperties().buildClassificationCodeNameAliasVariables(classificationElement);
    }

    public boolean isTableClassification(String classificationName) {
        return getClassificationProperties().isTableClassification(classificationName);
    }

    public boolean hasClassificationSubItemMap(String classificationName) {
        return getClassificationProperties().hasClassificationSubItemMap(classificationName);
    }

    // -----------------------------------------------------
    //                                            Deployment
    //                                            ----------
    public boolean hasClassification(String tableName, String columnName) {
        return getClassificationProperties().hasClassification(tableName, columnName);
    }

    public String getClassificationName(String tableName, String columnName) {
        return getClassificationProperties().getClassificationName(tableName, columnName);
    }

    public boolean hasClassificationName(String tableName, String columnName) {
        return getClassificationProperties().hasClassificationName(tableName, columnName);
    }

    public boolean hasClassificationAlias(String tableName, String columnName) {
        return getClassificationProperties().hasClassificationAlias(tableName, columnName);
    }

    public boolean hasClassificationAlias(String classificationName) {
        return getClassificationProperties().hasClassificationAlias(classificationName);
    }

    public Map<String, String> getAllColumnClassificationMap() { // for EntityDefinedCommonColumn
        return getClassificationProperties().getAllColumnClassificationMap();
    }

    public boolean isAllClassificationColumn(String columnName) { // for EntityDefinedCommonColumn
        return getClassificationProperties().isAllClassificationColumn(columnName);
    }

    public String getAllClassificationName(String columnName) { // for EntityDefinedCommonColumn
        return getClassificationProperties().getAllClassificationName(columnName);
    }

    // ===================================================================================
    //                                                        Little Adjustment Properties
    //                                                        ============================
    protected DfLittleAdjustmentProperties getLittleAdjustmentProperties() {
        return getProperties().getLittleAdjustmentProperties();
    }

    public boolean isAvailableDatabaseDependency() {
        return getLittleAdjustmentProperties().isAvailableDatabaseDependency();
    }

    public boolean isCDefToStringReturnsName() {
        return getLittleAdjustmentProperties().isCDefToStringReturnsName();
    }

    public boolean isMakeEntityOldStyleClassify() {
        return getLittleAdjustmentProperties().isMakeEntityOldStyleClassify();
    }

    public String getConditionQueryNotEqualDefinitionName() {
        return getLittleAdjustmentProperties().getConditionQueryNotEqualDefinitionName();
    }

    public boolean isPagingCountLater() {
        return getLittleAdjustmentProperties().isPagingCountLater();
    }

    public boolean isPagingCountLeastJoin() {
        return getLittleAdjustmentProperties().isPagingCountLeastJoin();
    }

    public boolean isInnerJoinAutoDetect() {
        return getLittleAdjustmentProperties().isInnerJoinAutoDetect();
    }

    public boolean isThatsBadTimingDetect() {
        return getLittleAdjustmentProperties().isThatsBadTimingDetect();
    }

    public boolean isNullOrEmptyQueryAllowed() {
        return getLittleAdjustmentProperties().isNullOrEmptyQueryAllowed();
    }

    public boolean isOverridingQueryAllowed() {
        return getLittleAdjustmentProperties().isOverridingQueryAllowed();
    }

    public boolean isAvailableDatabaseNativeJDBC() {
        return getLittleAdjustmentProperties().isAvailableDatabaseNativeJDBC();
    }

    public boolean isAvailableOracleNativeJDBC() { // Oracle facade
        return isDatabaseOracle() && isAvailableDatabaseNativeJDBC();
    }

    public boolean isAvailableJava8TimeEntity() {
        return getLittleAdjustmentProperties().isAvailableJava8TimeEntity();
    }

    public boolean isAvailableJava8TimeLocalDateEntity() {
        return getLittleAdjustmentProperties().isAvailableJava8TimeLocalDateEntity();
    }

    public boolean isAvailableJodaTimeEntity() {
        return getLittleAdjustmentProperties().isAvailableJodaTimeEntity();
    }

    public boolean isAvailableJodaTimeLocalDateEntity() {
        return getLittleAdjustmentProperties().isAvailableJodaTimeLocalDateEntity();
    }

    // unsupported for now
    //public boolean isAvailableJodaTimeDateTimeEntity() {
    //    return getLittleAdjustmentProperties().isAvailableJodaTimeDateTimeEntity();
    //}

    public boolean isMakeDeprecated() {
        return getLittleAdjustmentProperties().isMakeDeprecated();
    }

    public boolean isMakeRecentlyDeprecated() {
        return getLittleAdjustmentProperties().isMakeRecentlyDeprecated();
    }

    public String getDBFluteInitializerClass() {
        return getLittleAdjustmentProperties().getDBFluteInitializerClass();
    }

    public String getImplementedInvokerAssistantClass() {
        return getLittleAdjustmentProperties().getImplementedInvokerAssistantClass();
    }

    public String getImplementedCommonColumnAutoSetupperClass() {
        return getLittleAdjustmentProperties().getImplementedCommonColumnAutoSetupperClass();
    }

    public String getS2DaoSettingClass() {
        return getLittleAdjustmentProperties().getS2DaoSettingClass();
    }

    public boolean isShortCharHandlingValid() {
        return getLittleAdjustmentProperties().isShortCharHandlingValid();
    }

    public String getShortCharHandlingMode() {
        return getLittleAdjustmentProperties().getShortCharHandlingMode();
    }

    public String getShortCharHandlingModeCode() {
        return getLittleAdjustmentProperties().getShortCharHandlingModeCode();
    }

    public boolean isCursorSelectFetchSizeValid() {
        return getLittleAdjustmentProperties().isCursorSelectFetchSizeValid();
    }

    public String getCursorSelectFetchSize() {
        return getLittleAdjustmentProperties().getCursorSelectFetchSize();
    }

    public boolean isBatchInsertColumnModifiedPropertiesFragmentedDisallowed() {
        return getLittleAdjustmentProperties().isBatchInsertColumnModifiedPropertiesFragmentedDisallowed();
    }

    public boolean isBatchUpdateColumnModifiedPropertiesFragmentedAllowed() {
        return getLittleAdjustmentProperties().isBatchUpdateColumnModifiedPropertiesFragmentedAllowed();
    }

    public boolean isQueryUpdateCountPreCheck() {
        return getLittleAdjustmentProperties().isQueryUpdateCountPreCheck();
    }

    public boolean isStopGenerateExtendedBhv() {
        return getLittleAdjustmentProperties().isStopGenerateExtendedBhv();
    }

    public boolean isStopGenerateExtendedDao() {
        return getLittleAdjustmentProperties().isStopGenerateExtendedDao();
    }

    public boolean isStopGenerateExtendedEntity() {
        return getLittleAdjustmentProperties().isStopGenerateExtendedEntity();
    }

    public boolean isDeleteOldTableClass() {
        return getLittleAdjustmentProperties().isDeleteOldTableClass();
    }

    public boolean isAvailableToLowerInGeneratorUnderscoreMethod() {
        return getLittleAdjustmentProperties().isAvailableToLowerInGeneratorUnderscoreMethod();
    }

    public boolean isMakeDaoInterface() {
        return getLittleAdjustmentProperties().isMakeDaoInterface();
    }

    public boolean isCompatibleInsertColumnNotNullOnly() {
        return getLittleAdjustmentProperties().isCompatibleInsertColumnNotNullOnly();
    }

    public boolean isCompatibleBatchInsertDefaultEveryColumn() {
        return getLittleAdjustmentProperties().isCompatibleBatchInsertDefaultEveryColumn();
    }

    public boolean isCompatibleBatchUpdateDefaultEveryColumn() {
        return getLittleAdjustmentProperties().isCompatibleBatchUpdateDefaultEveryColumn();
    }

    public boolean isCompatibleConditionBeanOldNamingCheckInvalid() {
        return getLittleAdjustmentProperties().isCompatibleConditionBeanOldNamingCheckInvalid();
    }

    public boolean isCompatibleConditionBeanOldNamingOption() {
        return getLittleAdjustmentProperties().isCompatibleConditionBeanOldNamingOption();
    }

    // -----------------------------------------------------
    //                                       Optional Entity
    //                                       ---------------
    public String getBasicOptionalEntityClassName() {
        return getLittleAdjustmentProperties().getBasicOptionalEntityClass();
    }

    public String getBasicOptionalEntitySimpleName() {
        return getLittleAdjustmentProperties().getBasicOptionalEntitySimpleName();
    }

    public boolean isBasicOptionalEntityDBFluteEmbeddedClass() {
        return getLittleAdjustmentProperties().isBasicOptionalEntityDBFluteEmbeddedClass();
    }

    public String getRelationOptionalEntityClassName() {
        return getLittleAdjustmentProperties().getRelationOptionalEntityClass();
    }

    public String getRelationOptionalEntitySimpleName() {
        return getLittleAdjustmentProperties().getRelationOptionalEntitySimpleName();
    }

    public boolean isRelationOptionalEntityDBFluteEmbeddedClass() {
        return getLittleAdjustmentProperties().isRelationOptionalEntityDBFluteEmbeddedClass();
    }

    // ===================================================================================
    //                                                         SQL Log Registry Properties
    //                                                         ===========================
    public boolean isSqlLogRegistryValid() {
        return getProperties().getSqlLogRegistryProperties().isValid();
    }

    public int getSqlLogRegistryLimitSize() {
        return getProperties().getSqlLogRegistryProperties().getLimitSize();
    }

    // ===================================================================================
    //                                                               OutsideSql Properties
    //                                                               =====================
    public boolean isGenerateProcedureParameterBean() {
        return getProperties().getOutsideSqlProperties().isGenerateProcedureParameterBean();
    }

    public boolean hasSqlFileEncoding() {
        return getProperties().getOutsideSqlProperties().hasSqlFileEncoding();
    }

    public String getSqlFileEncoding() {
        return getProperties().getOutsideSqlProperties().getSqlFileEncoding();
    }

    public boolean isOutsideSqlPackageValid() {
        return getProperties().getOutsideSqlProperties().isSqlPackageValid();
    }

    public String getOutsideSqlPackage() {
        return getProperties().getOutsideSqlProperties().getSqlPackage();
    }

    public boolean isDefaultPackageValid() {
        return getProperties().getOutsideSqlProperties().isDefaultPackageValid();
    }

    public String getDefaultPackage() {
        return getProperties().getOutsideSqlProperties().getDefaultPackage();
    }

    public boolean isOmitResourcePathPackageValid() {
        return getProperties().getOutsideSqlProperties().isOmitResourcePathPackageValid();
    }

    public String getOmitResourcePathPackage() {
        return getProperties().getOutsideSqlProperties().getOmitResourcePathPackage();
    }

    public boolean isOmitFileSystemPathPackageValid() {
        return getProperties().getOutsideSqlProperties().isOmitFileSystemPathPackageValid();
    }

    public String getOmitFileSystemPathPackage() {
        return getProperties().getOutsideSqlProperties().getOmitFileSystemPathPackage();
    }

    public String getSql2EntityBaseEntityPackage() {
        return getProperties().getOutsideSqlProperties().getBaseEntityPackage();
    }

    public String getSql2EntityDBMetaPackage() {
        return getProperties().getOutsideSqlProperties().getDBMetaPackage();
    }

    public String getSql2EntityExtendedEntityPackage() {
        return getProperties().getOutsideSqlProperties().getExtendedEntityPackage();
    }

    public String getSql2EntityBaseCursorPackage() {
        return getProperties().getOutsideSqlProperties().getBaseCursorPackage();
    }

    public String getSql2EntityExtendedCursorPackage() {
        return getProperties().getOutsideSqlProperties().getExtendedCursorPackage();
    }

    public String getSql2EntityBaseParameterBeanPackage() {
        return getProperties().getOutsideSqlProperties().getBaseParameterBeanPackage();
    }

    public String getSql2EntityExtendedParameterBeanPackage() {
        return getProperties().getOutsideSqlProperties().getExtendedParameterBeanPackage();
    }

    // ===================================================================================
    //                                                                 Document Properties
    //                                                                 ===================
    // -----------------------------------------------------
    //                                            DB Comment
    //                                            ----------
    public boolean isAliasDelimiterInDbCommentValid() {
        return getProperties().getDocumentProperties().isAliasDelimiterInDbCommentValid();
    }

    public boolean isEntityJavaDocDbCommentValid() {
        return getProperties().getDocumentProperties().isEntityJavaDocDbCommentValid();
    }

    // -----------------------------------------------------
    //                                            SchemaHtml
    //                                            ----------
    public boolean isSchemaHtmlOutsideSqlValid() {
        if (getProperties().getDocumentProperties().isSuppressSchemaHtmlOutsideSql()) {
            return false;
        }
        return hasTableBqpMap();
    }

    public boolean isSchemaHtmlProcedureValid() {
        if (getProperties().getDocumentProperties().isSuppressSchemaHtmlProcedure()) {
            return false;
        }
        return isGenerateProcedureParameterBean();
    }

    public boolean isSchemaHtmlStyleSheetEmbedded() {
        return getProperties().getDocumentProperties().isSchemaHtmlStyleSheetEmbedded();
    }

    public boolean isSchemaHtmlStyleSheetLink() {
        return getProperties().getDocumentProperties().isSchemaHtmlStyleSheetLink();
    }

    public String getSchemaHtmlStyleSheetEmbedded() {
        return getProperties().getDocumentProperties().getSchemaHtmlStyleSheetEmbedded();
    }

    public String getSchemaHtmlStyleSheetLink() {
        return getProperties().getDocumentProperties().getSchemaHtmlStyleSheetLink();
    }

    public boolean isSchemaHtmlJavaScriptEmbedded() {
        return getProperties().getDocumentProperties().isSchemaHtmlJavaScriptEmbedded();
    }

    public boolean isSchemaHtmlJavaScriptLink() {
        return getProperties().getDocumentProperties().isSchemaHtmlJavaScriptLink();
    }

    public String getSchemaHtmlJavaScriptEmbedded() {
        return getProperties().getDocumentProperties().getSchemaHtmlJavaScriptEmbedded();
    }

    public String getSchemaHtmlJavaScriptLink() {
        return getProperties().getDocumentProperties().getSchemaHtmlJavaScriptLink();
    }

    // -----------------------------------------------------
    //                                           HistoryHtml
    //                                           -----------
    public boolean isHistoryHtmlStyleSheetEmbedded() {
        return getProperties().getDocumentProperties().isHistoryHtmlStyleSheetEmbedded();
    }

    public boolean isHistoryHtmlStyleSheetLink() {
        return getProperties().getDocumentProperties().isHistoryHtmlStyleSheetLink();
    }

    public String getHistoryHtmlStyleSheetEmbedded() {
        return getProperties().getDocumentProperties().getHistoryHtmlStyleSheetEmbedded();
    }

    public String getHistoryHtmlStyleSheetLink() {
        return getProperties().getDocumentProperties().getHistoryHtmlStyleSheetLink();
    }

    public boolean isHistoryHtmlJavaScriptEmbedded() {
        return getProperties().getDocumentProperties().isHistoryHtmlJavaScriptEmbedded();
    }

    public boolean isHistoryHtmlJavaScriptLink() {
        return getProperties().getDocumentProperties().isHistoryHtmlJavaScriptLink();
    }

    public String getHistoryHtmlJavaScriptEmbedded() {
        return getProperties().getDocumentProperties().getHistoryHtmlJavaScriptEmbedded();
    }

    public String getHistoryHtmlJavaScriptLink() {
        return getProperties().getDocumentProperties().getHistoryHtmlJavaScriptLink();
    }

    // ===================================================================================
    //                                                               Simple DTO Properties
    //                                                               =====================
    public boolean hasSimpleDtoDefinition() {
        return getProperties().getSimpleDtoProperties().hasSimpleDtoDefinition();
    }

    public String getSimpleDtoBaseDtoPackage() {
        return getProperties().getSimpleDtoProperties().getBaseDtoPackage();
    }

    public String getSimpleDtoExtendedDtoPackage() {
        return getProperties().getSimpleDtoProperties().getExtendedDtoPackage();
    }

    public String getSimpleDtoBaseDtoPrefix() {
        return getProperties().getSimpleDtoProperties().getBaseDtoPrefix();
    }

    public String getSimpleDtoBaseDtoSuffix() {
        return getProperties().getSimpleDtoProperties().getBaseDtoSuffix();
    }

    public String getSimpleDtoExtendedDtoPrefix() {
        return getProperties().getSimpleDtoProperties().getExtendedDtoPrefix();
    }

    public String getSimpleDtoExtendedDtoSuffix() {
        return getProperties().getSimpleDtoProperties().getExtendedDtoSuffix();
    }

    public String getSimpleDtoBaseMapperPackage() {
        return getProperties().getSimpleDtoProperties().getBaseMapperPackage();
    }

    public String getSimpleDtoExtendedMapperPackage() {
        return getProperties().getSimpleDtoProperties().getExtendedMapperPackage();
    }

    public boolean isSimpleDtoUseDtoMapper() {
        return getProperties().getSimpleDtoProperties().isUseDtoMapper();
    }

    public boolean isSimpleDtoMappingExceptCommonColumn() {
        return getProperties().getSimpleDtoProperties().isMappingExceptCommonColumn();
    }

    public boolean isSimpleDtoMappingReverseReference() {
        return getProperties().getSimpleDtoProperties().isMappingReverseReference();
    }

    public boolean isSimpleDtoClassificationDeployment() {
        return getProperties().getSimpleDtoProperties().isClassificationDeployment();
    }

    public boolean hasSimpleCDefDefinition() {
        return getProperties().getSimpleDtoProperties().hasSimpleCDefDefinition();
    }

    public String getSimpleCDefClass() {
        return getProperties().getSimpleDtoProperties().getSimpleCDefClass();
    }

    public String getSimpleCDefPackage() {
        return getProperties().getSimpleDtoProperties().getSimpleCDefPackage();
    }

    public boolean isSimpleCDefTarget(String classificationName) {
        return getProperties().getSimpleDtoProperties().isSimpleCDefTarget(classificationName);
    }

    public List<String> getSimpleCDefTargetClassificationNameList() {
        return getProperties().getSimpleDtoProperties().getSimpleCDefTargetClassificationNameList();
    }

    public boolean isSimpleDtoGwtDecorationSuppressJavaDependency() {
        return getProperties().getSimpleDtoProperties().isGwtDecorationSuppressJavaDependency();
    }

    // ===================================================================================
    //                                                                 Flex DTO Properties
    //                                                                 ===================
    public boolean hasFlexDtoDefinition() {
        return getProperties().getFlexDtoProperties().hasFlexDtoDefinition();
    }

    public boolean isFlexDtoOverrideExtended() {
        return getProperties().getFlexDtoProperties().isOverrideExtended();
    }

    public String getFlexDtoBaseDtoPackage() {
        return getProperties().getFlexDtoProperties().getBaseDtoPackage();
    }

    public String getFlexDtoExtendedDtoPackage() {
        return getProperties().getFlexDtoProperties().getExtendedDtoPackage();
    }

    public String getFlexDtoBaseDtoPrefix() {
        return getProperties().getFlexDtoProperties().getBaseDtoPrefix();
    }

    public String getFlexDtoBaseDtoSuffix() {
        return getProperties().getFlexDtoProperties().getBaseDtoSuffix();
    }

    public String getFlexDtoExtendedDtoPrefix() {
        return getProperties().getFlexDtoProperties().getExtendedDtoPrefix();
    }

    public String getFlexDtoExtendedDtoSuffix() {
        return getProperties().getFlexDtoProperties().getExtendedDtoSuffix();
    }

    // ===================================================================================
    //                                                                Hibernate Properties
    //                                                                ====================
    public boolean hasHibernateDefinition() {
        return getProperties().getHibernateProperties().hasHibernateDefinition();
    }

    public String getHibernateBaseEntityPackage() {
        return getBaseEntityPackage();
    }

    public String getHibernateExtendedEntityPackage() {
        return getExtendedEntityPackage();
    }

    public String getHibernateBaseEntityPrefix() {
        return getBasePrefix();
    }

    public String getHibernateManyToOneFetch() {
        return getProperties().getHibernateProperties().getManyToOneFetch();
    }

    public String getHibernateOneToOneFetch() {
        return getProperties().getHibernateProperties().getOneToOneFetch();
    }

    public String getHibernateOneToManyFetch() {
        return getProperties().getHibernateProperties().getOneToManyFetch();
    }

    // ===================================================================================
    //                                                                   S2JDBC Properties
    //                                                                   =================
    public boolean hasS2jdbcDefinition() {
        return getProperties().getS2jdbcProperties().hasS2jdbcDefinition();
    }

    public String getS2jdbcBaseEntityPackage() {
        return getProperties().getS2jdbcProperties().getBaseEntityPackage();
    }

    public String getS2jdbcExtendedEntityPackage() {
        return getProperties().getS2jdbcProperties().getExtendedEntityPackage();
    }

    public String getS2jdbcBaseEntityPrefix() {
        return getProperties().getS2jdbcProperties().getBaseEntityPrefix();
    }

    public boolean isSuppressPublicField() {
        return getProperties().getS2jdbcProperties().isSuppressPublicField();
    }

    // ===================================================================================
    //                                                  Component Name Helper for Template
    //                                                  ==================================
    // -----------------------------------------------------
    //                                   AllCommon Component
    //                                   -------------------
    // /= = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
    // These methods for all-common components are used when it needs to identity their components.
    // For example when the DI container is Seasar, These methods are not used
    // because S2Container has name-space in the DI architecture.
    // = = = = = = = = = =/

    public String getDBFluteInitializerComponentName() {
        return filterComponentNameWithProjectPrefix("introduction");
    }

    public String getInvokerAssistantComponentName() {
        return filterComponentNameWithProjectPrefix("invokerAssistant");
    }

    public String getCommonColumnAutoSetupperComponentName() {
        return filterComponentNameWithProjectPrefix("commonColumnAutoSetupper");
    }

    public String getBehaviorSelectorComponentName() {
        return filterComponentNameWithProjectPrefix("behaviorSelector");
    }

    public String getBehaviorCommandInvokerComponentName() {
        return filterComponentNameWithProjectPrefix("behaviorCommandInvoker");
    }

    // -----------------------------------------------------
    //                                     Filtering Utility
    //                                     -----------------
    /**
     * Filter a component name with a project prefix.
     * @param componentName The name of component. (NotNull)
     * @return A filtered component name with project prefix. (NotNull)
     */
    public String filterComponentNameWithProjectPrefix(String componentName) {
        final String prefix = getBasicProperties().getProjectPrefix();
        if (prefix == null || prefix.trim().length() == 0) {
            return componentName;
        }
        final String filteredPrefix = prefix.substring(0, 1).toLowerCase() + prefix.substring(1);
        return filteredPrefix + componentName.substring(0, 1).toUpperCase() + componentName.substring(1);
    }

    // ===================================================================================
    //                                                                 Type Mapping Helper
    //                                                                 ===================
    public String convertJavaNativeByJdbcType(String jdbcType) {
        try {
            return TypeMap.findJavaNativeByJdbcType(jdbcType, null, null);
        } catch (RuntimeException e) {
            _log.warn("TypeMap.findJavaNativeTypeString(jdbcType, null, null) threw the exception: jdbcType="
                    + jdbcType, e);
            throw e;
        }
    }

    // ===================================================================================
    //                                                                 Name Convert Helper
    //                                                                 ===================
    public String convertJavaNameByJdbcNameAsTable(String jdbcName) {
        // Don't use Srl.camelize() because
        // it saves compatible and here simple is best.
        // (Srl.camelize() is used at parameter bean and other sub objects)
        if (getBasicProperties().isTableNameCamelCase()) {
            // initial-capitalize only
            return initCap(jdbcName);
        }
        final List<String> inputs = new ArrayList<String>(2);
        inputs.add(jdbcName);
        inputs.add(getDefaultJavaNamingMethod());
        return initCap(generateName(inputs)); // use title case
    }

    public String convertJavaNameByJdbcNameAsColumn(String jdbcName) {
        // same policy as table naming
        if (getBasicProperties().isColumnNameCamelCase()) {
            // initial-capitalize only
            return initCap(jdbcName);
        }
        final List<String> inputs = new ArrayList<String>(2);
        inputs.add(jdbcName);
        inputs.add(getDefaultJavaNamingMethod());
        return initCap(generateName(inputs)); // use title case
    }

    public String convertUncapitalisedJavaNameByJdbcNameAsColumn(String jdbcName) {
        return Srl.initUncap(convertJavaNameByJdbcNameAsColumn(jdbcName));
    }

    /**
     * Generate name.
     * @param inputs Inputs.
     * @return Generated name.
     */
    protected String generateName(List<?> inputs) {
        String javaName = null;
        try {
            javaName = NameFactory.generateName(NameFactory.JAVA_GENERATOR, inputs);
        } catch (EngineException e) {
            String msg = "NameFactory.generateName() threw the exception: inputs=" + inputs;
            _log.warn(msg, e);
            throw new RuntimeException(msg, e);
        } catch (RuntimeException e) {
            String msg = "NameFactory.generateName() threw the exception: inputs=" + inputs;
            _log.warn(msg, e);
            throw new RuntimeException(msg, e);
        }
        if (javaName == null) {
            String msg = "NameFactory.generateName() returned null: inputs=" + inputs;
            _log.warn(msg);
            throw new IllegalStateException(msg);
        }
        return javaName;
    }

    // ===================================================================================
    //                                                         General Helper for Template
    //                                                         ===========================
    // -----------------------------------------------------
    //                                             Character
    //                                             ---------
    public String getWildCard() {
        return "%";
    }

    public String getSharp() {
        return "#";
    }

    public String getDollar() {
        return "$";
    }

    // -----------------------------------------------------
    //                                               Comment
    //                                               -------
    public String getOverrideComment() {
        return "{@inheritDoc}";
    }

    public String getImplementComment() {
        return "{@inheritDoc}";
    }

    // -----------------------------------------------------
    //                                               Logging
    //                                               -------
    public void info(String msg) {
        _log.info(msg);
    }

    public void debug(String msg) {
        _log.debug(msg);
    }

    // -----------------------------------------------------
    //                                             Timestamp
    //                                             ---------
    public String getTimestampExpression() {
        final Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(timestamp);
    }

    // -----------------------------------------------------
    //                                                String
    //                                                ------
    public String initCap(String str) {
        return Srl.initCap(str);
    }

    public String initUncap(String str) {
        return Srl.initUncap(str);
    }

    public boolean isInitNumber(String str) {
        if (str == null) {
            String msg = "Argument[str] must not be null.";
            throw new IllegalArgumentException(msg);
        }
        if (str.length() == 0) {
            return false;
        }
        try {
            Integer.valueOf(str.substring(0, 1));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // -----------------------------------------------------
    //                                                   Map
    //                                                   ---
    public String getMapValue(Map<?, ?> map, String key) {
        final Object value = map.get(key);
        return value != null ? (String) value : "";
    }

    // -----------------------------------------------------
    //                                                   I/O
    //                                                   ---
    public void makeDirectory(String packagePath) {
        FileUtil.mkdir(getGeneratorInstance().getOutputPath() + "/" + packagePath);
    }

    public String getPackageAsPath(String pckge) {
        final DfPackagePathHandler handler = new DfPackagePathHandler(getBasicProperties());
        return handler.getPackageAsPath(pckge);
    }

    // ===================================================================================
    //                                                                 Behavior Query Path
    //                                                                 ===================
    protected Map<String, Map<String, Map<String, String>>> _tableBqpMap;

    public boolean hasTableBqpMap() { // basically for SchemaHTML
        return !getTableBqpMap().isEmpty();
    }

    protected Map<String, Map<String, Map<String, String>>> getTableBqpMap() {
        if (_tableBqpMap != null) {
            return _tableBqpMap;
        }
        final DfBehaviorQueryPathSetupper setupper = new DfBehaviorQueryPathSetupper();
        try {
            _tableBqpMap = setupper.extractTableBqpMap(collectOutsideSql());
        } catch (RuntimeException e) {
            _log.warn("Failed to extract the map of table behavior query path!", e);
            _tableBqpMap = new HashMap<String, Map<String, Map<String, String>>>();
        }
        return _tableBqpMap;
    }

    protected DfOutsideSqlPack collectOutsideSql() {
        final DfOutsideSqlCollector outsideSqlCollector = new DfOutsideSqlCollector();
        outsideSqlCollector.suppressDirectoryCheck();
        return outsideSqlCollector.collectOutsideSql();
    }

    // ===================================================================================
    //                                                                  Procedure Document
    //                                                                  ==================
    protected List<DfProcedureMeta> _procedureMetaInfoList;
    protected Map<String, List<DfProcedureMeta>> _schemaProcedureMap;

    public boolean hasSeveralProcedureSchema() throws SQLException {
        return getAvailableSchemaProcedureMap().size() >= 2;
    }

    public List<DfProcedureMeta> getAvailableProcedureList() throws SQLException {
        if (_procedureMetaInfoList != null) {
            return _procedureMetaInfoList;
        }
        _log.info(" ");
        _log.info("...Setting up procedures for documents");
        final DfProcedureExtractor handler = new DfProcedureExtractor();
        final DfSchemaSource dataSource = getDataSource();
        handler.includeProcedureSynonym(dataSource);
        handler.includeProcedureToDBLink(dataSource);
        _procedureMetaInfoList = handler.getAvailableProcedureList(dataSource); // ordered by schema
        return _procedureMetaInfoList;
    }

    public Map<String, List<DfProcedureMeta>> getAvailableSchemaProcedureMap() throws SQLException {
        if (_schemaProcedureMap != null) {
            return _schemaProcedureMap;
        }
        final List<DfProcedureMeta> procedureList = getAvailableProcedureList();
        final Map<String, List<DfProcedureMeta>> schemaProcedureListMap = DfCollectionUtil.newLinkedHashMap();
        final String mainName = "(main schema)";
        for (DfProcedureMeta meta : procedureList) {
            final UnifiedSchema procedureSchema = meta.getProcedureSchema();
            final String schemaName;
            if (procedureSchema != null) {
                final String drivenSchema = procedureSchema.getDrivenSchema();
                if (drivenSchema != null) {
                    schemaName = drivenSchema;
                } else {
                    schemaName = procedureSchema.isMainSchema() ? mainName : procedureSchema.getSqlPrefixSchema();
                }
            } else {
                schemaName = "(no schema)";
            }
            List<DfProcedureMeta> metaList = schemaProcedureListMap.get(schemaName);
            if (metaList == null) {
                metaList = DfCollectionUtil.newArrayList();
                schemaProcedureListMap.put(schemaName, metaList);
            }
            metaList.add(meta);
        }
        _schemaProcedureMap = schemaProcedureListMap;
        return _schemaProcedureMap;
    }

    // ===================================================================================
    //                                                                          DataSource
    //                                                                          ==========
    protected DfSchemaSource getDataSource() {
        final UnifiedSchema mainSchema = getDatabaseProperties().getDatabaseSchema();
        return new DfSchemaSource(DfDataSourceContext.getDataSource(), mainSchema);
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    /**
     * Creates a string representation of this Database.
     * The representation is given in xml format.
     * @return String representation in XML. (NotNull)
     */
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("<database name=\"").append(getName()).append('"').append(">\n");
        final List<Table> tableList = getTableList();
        for (Table table : tableList) {
            sb.append(table);
        }
        sb.append("</database>");
        return sb.toString();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getName() {
        return _name;
    }

    public void setName(String name) {
        this._name = (name == null ? "default" : name);
    }

    /**
     * Get the value of defaultJavaNamingMethod which specifies the
     * method for converting schema names for table and column to Java names.
     * @return The default naming conversion used by this database.
     */
    public String getDefaultJavaNamingMethod() {
        return _defaultJavaNamingMethod;
    }

    /**
     * Set the value of defaultJavaNamingMethod.
     * @param v The default naming conversion for this database to use.
     */
    public void setDefaultJavaNamingMethod(String v) {
        this._defaultJavaNamingMethod = v;
    }

    public void setAppData(AppData appData) {
        _appData = appData;
    }

    public void setSql2EntitySchemaData(AppData sql2entitySchemaData) {
        _sql2entitySchemaData = sql2entitySchemaData;
    }

    public AppData getAppData() {
        return _appData;
    }

    public String getDatabaseType() {
        return _databaseType;
    }

    public void setDatabaseType(String databaseType) {
        this._databaseType = databaseType;
    }

    public Map<String, DfPmbMetaData> getPmbMetaDataMap() {
        return _pmbMetaDataMap;
    }

    public void setPmbMetaDataMap(Map<String, DfPmbMetaData> pmbMetaDataMap) {
        _pmbMetaDataMap = pmbMetaDataMap;
    }

    public void setSkipDeleteOldClass(boolean skipDeleteOldClass) {
        _skipDeleteOldClass = skipDeleteOldClass;
    }
}