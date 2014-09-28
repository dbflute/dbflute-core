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
package org.seasar.dbflute.logic.sql2entity.pmbean;

import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.engine.database.model.Database;
import org.apache.torque.engine.database.model.TypeMap;
import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.helper.StringKeyMap;
import org.seasar.dbflute.helper.jdbc.context.DfSchemaSource;
import org.seasar.dbflute.logic.generate.language.grammar.DfLanguageGrammar;
import org.seasar.dbflute.logic.jdbc.metadata.basic.DfColumnExtractor;
import org.seasar.dbflute.logic.jdbc.metadata.basic.DfProcedureExtractor;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfColumnMeta;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfProcedureColumnMeta;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfProcedureColumnMeta.DfProcedureColumnType;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfProcedureMeta;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfProcedureMeta.DfProcedureType;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfProcedureNotParamResultMeta;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfTypeArrayInfo;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfTypeStructInfo;
import org.seasar.dbflute.logic.sql2entity.cmentity.DfCustomizeEntityInfo;
import org.seasar.dbflute.logic.sql2entity.cmentity.DfProcedureExecutionMetaExtractor;
import org.seasar.dbflute.properties.DfBasicProperties;
import org.seasar.dbflute.properties.DfDatabaseProperties;
import org.seasar.dbflute.properties.DfLittleAdjustmentProperties;
import org.seasar.dbflute.properties.DfOutsideSqlProperties;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfProcedurePmbSetupper {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance. */
    private static final Log _log = LogFactory.getLog(DfProcedurePmbSetupper.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfSchemaSource _dataSource;
    protected final Map<String, DfCustomizeEntityInfo> _entityInfoMap;
    protected final Map<String, DfPmbMetaData> _pmbMetaDataMap;
    protected final Database _database;
    protected final DfColumnExtractor _columnExtractor = new DfColumnExtractor();
    protected final DfProcedureExtractor _procedureExtractor = new DfProcedureExtractor();
    protected final Map<String, String> _continuedFailureMessageMap = DfCollectionUtil.newLinkedHashMap();

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfProcedurePmbSetupper(DfSchemaSource dataSource, Map<String, DfCustomizeEntityInfo> entityInfoMap,
            Map<String, DfPmbMetaData> pmbMetaDataMap, Database database) {
        _dataSource = dataSource;
        _entityInfoMap = entityInfoMap;
        _pmbMetaDataMap = pmbMetaDataMap;
        _database = database;
    }

    // ===================================================================================
    //                                                                              Set up
    //                                                                              ======
    public void setupProcedure() throws SQLException {
        if (!getOutsideSqlProperties().isGenerateProcedureParameterBean()) {
            return;
        }
        _log.info(" ");
        _log.info("...Setting up procedures for generating parameter-beans");
        final List<DfProcedureMeta> procedureList = getAvailableProcedureList();
        _log.info("");
        _log.info("/= = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =");
        for (DfProcedureMeta procedure : procedureList) {
            final Map<String, String> propertyNameTypeMap = DfCollectionUtil.newLinkedHashMap();
            final Map<String, String> propertyNameOptionMap = DfCollectionUtil.newLinkedHashMap();
            final Map<String, String> propertyNameColumnNameMap = DfCollectionUtil.newLinkedHashMap();
            final Map<String, DfProcedureColumnMeta> propertyNameColumnInfoMap = DfCollectionUtil.newLinkedHashMap();
            final List<DfProcedureColumnMeta> procedureColumnList = procedure.getProcedureColumnList();
            final List<DfProcedureNotParamResultMeta> notParamResultList = procedure.getNotParamResultList();

            final String pmbName = convertProcedureNameToPmbName(procedure.getProcedureName());
            {
                final String procDisp = procedure.buildProcedureLoggingName();
                final DfProcedureType procType = procedure.getProcedureType();
                _log.info("[" + pmbName + "]: " + procDisp + " // " + procType);
                if (procedureColumnList.isEmpty() && notParamResultList.isEmpty()) {
                    _log.info("    *No Parameter");
                }
            }

            boolean refCustomizeEntity = false;

            // Procedure Parameter handling
            int index = 0;
            for (DfProcedureColumnMeta column : procedureColumnList) {
                if (!column.isBindParameter()) {
                    continue;
                }
                final String columnName;
                {
                    final String plainColumnName = column.getColumnName();
                    if (Srl.is_NotNull_and_NotTrimmedEmpty(plainColumnName)) {
                        columnName = resolveVendorColumnNameHeadable(plainColumnName);
                    } else {
                        columnName = "arg" + (index + 1);
                    }
                }
                final String propertyName;
                {
                    propertyName = convertColumnNameToPropertyName(columnName);
                }

                // procedure's overload is unsupported because of this (override property) 
                propertyNameColumnInfoMap.put(propertyName, column);

                final ProcedurePropertyInfo propertyInfo = processProcedureProperty(pmbName, column, propertyName);
                final String propertyType = propertyInfo.getPropertyType();
                if (propertyInfo.isRefCustomizeEntity()) {
                    refCustomizeEntity = true;
                }
                propertyNameTypeMap.put(propertyName, propertyType);
                final DfProcedureColumnType procedureColumnType = column.getProcedureColumnType();
                propertyNameOptionMap.put(propertyName, procedureColumnType.toString());
                propertyNameColumnNameMap.put(propertyName, columnName);
                String msg = "    " + propertyType + " " + propertyName + ";";
                msg = msg + " // " + column.getProcedureColumnType();
                msg = msg + "(" + column.getJdbcDefType() + ", " + column.getDbTypeName() + ")";
                _log.info(msg);
                ++index;
            }

            // NotParamResult handling
            for (DfProcedureNotParamResultMeta result : notParamResultList) {
                final String propertyName = result.getPropertyName();
                final String propertyType;
                if (result.hasResultSetColumnInfo()) {
                    final String entityName = convertProcedurePmbNameToEntityName(pmbName, propertyName);
                    _entityInfoMap.put(entityName, createEntityInfo(entityName, result.getResultSetColumnInfoMap()));
                    propertyType = convertProcedureListPropertyType(entityName);
                    refCustomizeEntity = true;
                } else {
                    propertyType = getProcedureDefaultResultSetPropertyType();
                }
                propertyNameTypeMap.put(propertyName, propertyType);
                propertyNameOptionMap.put(propertyName, DfProcedureColumnType.procedureColumnResult.toString());
                propertyNameColumnNameMap.put(propertyName, propertyName);
                String msg = "    " + propertyType + " " + propertyName + ";";
                msg = msg + " // " + DfProcedureColumnType.procedureColumnResult;
                _log.info(msg);
            }

            final DfPmbMetaData parameterBeanMetaData = new DfPmbMetaData();
            parameterBeanMetaData.setClassName(pmbName);
            parameterBeanMetaData.setPropertyNameTypeMap(propertyNameTypeMap);
            parameterBeanMetaData.setPropertyNameOptionMap(propertyNameOptionMap);
            parameterBeanMetaData.setProcedureName(procedure.buildProcedureSqlName());
            parameterBeanMetaData.setPropertyNameColumnNameMap(propertyNameColumnNameMap);
            parameterBeanMetaData.setPropertyNameColumnInfoMap(propertyNameColumnInfoMap);
            parameterBeanMetaData.setProcedureCalledBySelect(procedure.isCalledBySelect());
            parameterBeanMetaData.setProcedureRefCustomizeEntity(refCustomizeEntity);
            _pmbMetaDataMap.put(pmbName, parameterBeanMetaData);
        }
        _log.info("= = = = = = = = = =/");
        _log.info(" ");
    }

    // ===================================================================================
    //                                                                      Procedure List
    //                                                                      ==============
    protected List<DfProcedureMeta> getAvailableProcedureList() throws SQLException {
        _procedureExtractor.includeProcedureSynonym(_dataSource);
        _procedureExtractor.includeProcedureToDBLink(_dataSource);
        final List<DfProcedureMeta> procedureList = _procedureExtractor.getAvailableProcedureList(_dataSource);
        if (getOutsideSqlProperties().isGenerateProcedureCustomizeEntity()) {
            final DfProcedureExecutionMetaExtractor executionMetaHandler = new DfProcedureExecutionMetaExtractor();
            executionMetaHandler.extractExecutionMetaData(_dataSource, procedureList);
            _continuedFailureMessageMap.putAll(executionMetaHandler.getContinuedFailureMessageMap());
        }
        return procedureList;
    }

    // ===================================================================================
    //                                                                       Property Type
    //                                                                       =============
    protected ProcedurePropertyInfo processProcedureProperty(String pmbName, DfProcedureColumnMeta column,
            String propertyName) {
        final ProcedurePropertyInfo propertyInfo = new ProcedurePropertyInfo();
        propertyInfo.setColumnInfo(column);
        if (isResultSetProperty(column)) {
            if (column.hasResultSetColumnInfo()) {
                final String entityName = convertProcedurePmbNameToEntityName(pmbName, propertyName);
                _entityInfoMap.put(entityName, createEntityInfo(entityName, column.getResultSetColumnInfoMap()));
                propertyInfo.setPropertyType(convertProcedureListPropertyType(entityName));
                propertyInfo.setRefCustomizeEntity(true);
            } else {
                propertyInfo.setPropertyType(getProcedureDefaultResultSetPropertyType());
            }
            return propertyInfo;
        }
        final int jdbcDefType = column.getJdbcDefType();
        final Integer columnSize = column.getColumnSize();
        final Integer decimalDigits = column.getDecimalDigits();

        final String specialType = doProcessSpecialType(pmbName, column, propertyInfo);
        final String propertyType;
        if (Srl.is_NotNull_and_NotTrimmedEmpty(specialType)) {
            propertyType = specialType;
        } else {
            final String dbTypeName = column.getDbTypeName();
            propertyType = findPlainPropertyType(jdbcDefType, dbTypeName, columnSize, decimalDigits);
        }
        propertyInfo.setPropertyType(propertyType);
        return propertyInfo;
    }

    protected String doProcessSpecialType(String pmbName, DfProcedureColumnMeta column,
            ProcedurePropertyInfo propertyInfo) {
        if (getLittleAdjustmentProperties().isAvailableDatabaseNativeJDBC()) {
            final String wallOfOracleType = doProcessGreatWallOfOracleType(pmbName, column, propertyInfo);
            if (Srl.is_NotNull_and_NotTrimmedEmpty(wallOfOracleType)) {
                return wallOfOracleType;
            }
        }
        final String propertyType;
        if (column.isOracleNumber()) {
            // because the length setting of procedure parameter is unsupported on Oracle
            propertyType = TypeMap.getDefaultDecimalJavaNativeType();
        } else {
            propertyType = null;
        }
        return propertyType;
    }

    protected String doProcessGreatWallOfOracleType(String pmbName, DfProcedureColumnMeta column,
            ProcedurePropertyInfo propertyInfo) {
        final String propertyType;
        if (column.isOracleTreatedAsArray() && column.hasTypeArrayInfo()) {
            // here dbTypeName is "PL/SQL TABLE" or "TABLE" or "VARRAY"
            // (it's not useful for type mapping, so search like this)
            final DfTypeArrayInfo arrayInfo = column.getTypeArrayInfo();
            propertyType = getGenericListClassName(doProcessArrayProperty(arrayInfo, propertyInfo));
        } else if (column.isOracleStruct() && column.hasTypeStructInfo()) {
            final DfTypeStructInfo structInfo = column.getTypeStructInfo();
            propertyType = doProcessStructProperty(structInfo, propertyInfo);
        } else {
            propertyType = null;
        }
        return propertyType;
    }

    // -----------------------------------------------------
    //                                    ResultSet Property
    //                                    ------------------
    protected boolean isResultSetProperty(DfProcedureColumnMeta column) {
        if (column.hasResultSetColumnInfo()) {
            return true;
        }
        return column.isPostgreSQLCursor() || column.isOracleCursor();
    }

    protected String getProcedureDefaultResultSetPropertyType() {
        final DfLanguageGrammar grammarInfo = getBasicProperties().getLanguageDependency().getLanguageGrammar();
        return grammarInfo.buildGenericMapListClassName("String", "Object"); // Map<String, Object>
    }

    // -----------------------------------------------------
    //                                        Array Property
    //                                        --------------
    protected String doProcessArrayProperty(DfTypeArrayInfo arrayInfo, ProcedurePropertyInfo propertyInfo) {
        final String propertyType;
        if (arrayInfo.hasNestedArray()) { // array in array
            final DfTypeArrayInfo nestedArrayInfo = arrayInfo.getNestedArrayInfo();
            final String nestedType = doProcessArrayProperty(nestedArrayInfo, propertyInfo); // recursive call
            propertyType = getGenericListClassName(nestedType);
        } else if (arrayInfo.hasElementStructInfo()) { // struct in array
            final DfTypeStructInfo structInfo = arrayInfo.getElementStructInfo();
            propertyType = doProcessStructProperty(structInfo, propertyInfo);
        } else { // scalar in array
            final String dbTypeName = arrayInfo.getElementType();
            propertyType = findPlainPropertyType(Types.OTHER, dbTypeName, null, null);
        }
        arrayInfo.setElementJavaNative(propertyType);
        return propertyType;
    }

    protected String findArrayScalarElementPropertyType(DfTypeArrayInfo arrayInfo) {
        // by only name
        final String dbTypeName = arrayInfo.getElementType();
        return findPlainPropertyType(Types.OTHER, dbTypeName, null, null);
    }

    // -----------------------------------------------------
    //                                       Struct Property
    //                                       ---------------
    protected String doProcessStructProperty(DfTypeStructInfo structInfo, ProcedurePropertyInfo propertyInfo) {
        // register entity generation for struct that contains nested array & struct 
        registerEntityInfoIfNeeds(structInfo, propertyInfo);

        // entityType is class name can used on program
        // so it adjusts project prefix here
        final String entityType = buildStructEntityType(structInfo);
        propertyInfo.setRefCustomizeEntity(true); // why propertyInfo is here, is only for this
        return entityType;
    }

    protected void registerEntityInfoIfNeeds(DfTypeStructInfo structInfo, ProcedurePropertyInfo propertyInfo) {
        final String typeName = getStructEntityNameResouce(structInfo);
        if (!_entityInfoMap.containsKey(typeName)) { // because of independent objects and so called several times
            final StringKeyMap<DfColumnMeta> attrMap = structInfo.getAttributeInfoMap();
            _entityInfoMap.put(typeName, createEntityInfo(typeName, attrMap, structInfo));
            setupStructAttribute(structInfo, propertyInfo);
        }
    }

    protected void setupStructAttribute(DfTypeStructInfo structInfo, ProcedurePropertyInfo propertyInfo) {
        final StringKeyMap<DfColumnMeta> attrMap = structInfo.getAttributeInfoMap();
        for (DfColumnMeta attrInfo : attrMap.values()) { // nested array or struct handling
            if (attrInfo.hasTypeArrayInfo()) { // array in struct
                final DfTypeArrayInfo typeArrayInfo = attrInfo.getTypeArrayInfo();
                if (typeArrayInfo.hasElementStructInfo()) { // struct in array in struct
                    registerEntityInfoIfNeeds(typeArrayInfo.getElementStructInfo(), propertyInfo);
                }
                if (typeArrayInfo.hasElementJavaNative()) {
                    final String elementJavaNative = typeArrayInfo.getElementJavaNative();
                    attrInfo.setSql2EntityForcedJavaNative(getGenericListClassName(elementJavaNative));
                } else {
                    final String elementType;
                    if (typeArrayInfo.hasNestedArray()) { // array in array in struct
                        final DfTypeArrayInfo nestedArrayInfo = typeArrayInfo.getNestedArrayInfo();
                        elementType = getGenericListClassName(doProcessArrayProperty(nestedArrayInfo, propertyInfo));
                    } else if (typeArrayInfo.hasElementStructInfo()) { // struct in array in struct
                        final DfTypeStructInfo elementStructInfo = typeArrayInfo.getElementStructInfo();
                        elementType = buildStructEntityType(elementStructInfo);
                    } else { // scalar in array in struct
                        elementType = findArrayScalarElementPropertyType(attrInfo.getTypeArrayInfo());
                    }
                    typeArrayInfo.setElementJavaNative(elementType);
                    attrInfo.setSql2EntityForcedJavaNative(getGenericListClassName(elementType));
                }
            } else if (attrInfo.hasTypeStructInfo()) {
                final DfTypeStructInfo nestedStructInfo = attrInfo.getTypeStructInfo();
                registerEntityInfoIfNeeds(nestedStructInfo, propertyInfo);
                if (nestedStructInfo.hasEntityType()) {
                    attrInfo.setSql2EntityForcedJavaNative(nestedStructInfo.getEntityType());
                } else {
                    attrInfo.setSql2EntityForcedJavaNative(buildStructEntityType(nestedStructInfo));
                }
            }
        }
    }

    protected String buildStructEntityType(DfTypeStructInfo structInfo) {
        // resource name becomes entity name plainly but it will be converted as java name
        // so it uses database's convert that is same conversion way as generating
        // because this process needs entityType on program
        final String entityName = _database.convertJavaNameByJdbcNameAsTable(getStructEntityNameResouce(structInfo));
        final String projectPrefix = getBasicProperties().getProjectPrefix();
        final String entityType = projectPrefix + entityName;
        structInfo.setEntityType(entityType);
        return entityType;
    }

    protected String getStructEntityNameResouce(DfTypeStructInfo structInfo) {
        return structInfo.getTypePureName(); // *same names between schema is unsupported
    }

    // -----------------------------------------------------
    //                                           Entity Info
    //                                           -----------
    protected DfCustomizeEntityInfo createEntityInfo(String entityName, Map<String, DfColumnMeta> columnMap) {
        return doCreateEntityInfo(entityName, columnMap, null);
    }

    protected DfCustomizeEntityInfo createEntityInfo(String entityName, Map<String, DfColumnMeta> columnMap,
            DfTypeStructInfo structInfo) {
        return doCreateEntityInfo(entityName, columnMap, structInfo);
    }

    protected DfCustomizeEntityInfo doCreateEntityInfo(String entityName, Map<String, DfColumnMeta> columnMap,
            DfTypeStructInfo structInfo) {
        final DfCustomizeEntityInfo entityInfo;
        if (structInfo != null) {
            entityInfo = new DfCustomizeEntityInfo(entityName, columnMap, structInfo);
        } else {
            entityInfo = new DfCustomizeEntityInfo(entityName, columnMap);
        }
        entityInfo.setProcedureHandling(true);
        return entityInfo;
    }

    // -----------------------------------------------------
    //                                         Assist Helper
    //                                         -------------
    protected String findPlainPropertyType(int jdbcDefType, String dbTypeName, Integer columnSize, Integer decimalDigits) {
        if (_columnExtractor.hasMappingJdbcType(jdbcDefType, dbTypeName)) {
            final String torqueType = _columnExtractor.getColumnJdbcType(jdbcDefType, dbTypeName);
            return TypeMap.findJavaNativeByJdbcType(torqueType, columnSize, decimalDigits);
        } else {
            return "Object"; // procedure has many-many types so it uses Object type (not String)

            // it's not complete because nested properties are not target
            // for example, attributes in STRUCT type
            // but it's OK, that's the specification of DBFlute
        }
    }

    protected String getGenericListClassName(String element) {
        final DfLanguageGrammar grammarInfo = getBasicProperties().getLanguageDependency().getLanguageGrammar();
        return grammarInfo.buildGenericListClassName(element); // List<ELEMENT>
    }

    protected static class ProcedurePropertyInfo {
        protected DfProcedureColumnMeta _columnInfo;
        protected String _propertyType;
        protected boolean _refCustomizeEntity;

        public DfProcedureColumnMeta getColumnInfo() {
            return _columnInfo;
        }

        public void setColumnInfo(DfProcedureColumnMeta columnInfo) {
            _columnInfo = columnInfo;
        }

        public String getPropertyType() {
            return _propertyType;
        }

        public void setPropertyType(String propertyType) {
            _propertyType = propertyType;
        }

        public boolean isRefCustomizeEntity() {
            return _refCustomizeEntity;
        }

        public void setRefCustomizeEntity(boolean refCustomizeEntity) {
            _refCustomizeEntity = refCustomizeEntity;
        }
    }

    // ===================================================================================
    //                                                                        Convert Name
    //                                                                        ============
    protected String convertProcedureNameToPmbName(String procedureName) {
        // here you do not need to handle project prefix
        // because the prefix is resolved at table object
        procedureName = Srl.replace(procedureName, ".", "_");
        procedureName = Srl.replace(procedureName, "@", "_"); // e.g. Oracle's DBLink
        procedureName = resolveVendorProcedureNameHeadache(procedureName);
        return Srl.camelize(procedureName) + "Pmb";
    }

    protected String resolveVendorProcedureNameHeadache(String procedureName) {
        if (getBasicProperties().isDatabaseSQLServer()) { // SQLServer returns 'sp_foo;1'
            procedureName = Srl.substringLastFront(procedureName, ";");
        }
        return procedureName;
    }

    protected String convertProcedurePmbNameToEntityName(String pmbName, String propertyName) {
        final String baseName = Srl.substringLastFront(pmbName, "Pmb");
        return baseName + Srl.initCap(propertyName);
    }

    protected String convertProcedureListPropertyType(String entityName) {
        // propertyType is class name can used on program
        // so it adjusts project prefix here
        final String projectPrefix = getBasicProperties().getProjectPrefix();
        final String propertyType = projectPrefix + entityName;
        final DfLanguageGrammar grammarInfo = getBasicProperties().getLanguageDependency().getLanguageGrammar();
        return grammarInfo.buildGenericListClassName(propertyType);
    }

    protected String convertColumnNameToPropertyName(String columnName) {
        columnName = resolveVendorColumnNameHeadable(columnName);
        return Srl.initBeansProp(Srl.camelize(columnName));
    }

    protected String resolveVendorColumnNameHeadable(String columnName) {
        if (getBasicProperties().isDatabaseSQLServer()) {
            // SQLServer returns '@returnValue'
            columnName = Srl.substringFirstRear(columnName, "@");
        }
        return columnName;
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

    protected DfOutsideSqlProperties getOutsideSqlProperties() {
        return getProperties().getOutsideSqlProperties();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public Map<String, String> getContinuedFailureMessageMap() {
        return _continuedFailureMessageMap;
    }
}
