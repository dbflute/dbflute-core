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
package org.dbflute.logic.sql2entity.pmbean;

import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.torque.engine.database.model.Database;
import org.apache.torque.engine.database.model.TypeMap;
import org.dbflute.DfBuildProperties;
import org.dbflute.helper.StringKeyMap;
import org.dbflute.helper.jdbc.context.DfSchemaSource;
import org.dbflute.logic.generate.language.grammar.DfLanguageGrammar;
import org.dbflute.logic.jdbc.metadata.basic.DfColumnExtractor;
import org.dbflute.logic.jdbc.metadata.basic.DfProcedureExtractor;
import org.dbflute.logic.jdbc.metadata.info.DfColumnMeta;
import org.dbflute.logic.jdbc.metadata.info.DfProcedureColumnMeta;
import org.dbflute.logic.jdbc.metadata.info.DfProcedureColumnMeta.DfProcedureColumnType;
import org.dbflute.logic.jdbc.metadata.info.DfProcedureMeta;
import org.dbflute.logic.jdbc.metadata.info.DfProcedureMeta.DfProcedureType;
import org.dbflute.logic.jdbc.metadata.info.DfProcedureNotParamResultMeta;
import org.dbflute.logic.jdbc.metadata.info.DfTypeArrayInfo;
import org.dbflute.logic.jdbc.metadata.info.DfTypeStructInfo;
import org.dbflute.logic.sql2entity.cmentity.DfCustomizeEntityInfo;
import org.dbflute.logic.sql2entity.cmentity.DfProcedureExecutionMetaExtractor;
import org.dbflute.properties.DfBasicProperties;
import org.dbflute.properties.DfDatabaseProperties;
import org.dbflute.properties.DfLittleAdjustmentProperties;
import org.dbflute.properties.DfOutsideSqlProperties;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 */
public class DfProcedurePmbSetupper {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The logger instance for this class. (NotNull) */
    private static final Logger _log = LoggerFactory.getLogger(DfProcedurePmbSetupper.class);

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
            boolean alreadyReturnNameResolved = false; // just in case
            for (DfProcedureColumnMeta column : procedureColumnList) {
                if (!column.isBindParameter()) {
                    continue;
                }
                final String resolvedColumnName = resolveColumnName(procedure, index, alreadyReturnNameResolved, column);
                if (column.isProcedureColumnType_Return()) {
                    alreadyReturnNameResolved = true;
                }
                final String propertyName = convertColumnNameToPropertyName(resolvedColumnName);

                // procedure's overload is unsupported because of this (override property) 
                propertyNameColumnInfoMap.put(propertyName, column);

                final ProcedurePropertyInfo propertyInfo =
                        processProcedureProperty(procedure, resolvedColumnName, pmbName, column, propertyName);
                final String propertyType = propertyInfo.getPropertyType();
                if (propertyInfo.isRefCustomizeEntity()) {
                    refCustomizeEntity = true;
                }
                propertyNameTypeMap.put(propertyName, propertyType);
                final DfProcedureColumnType procedureColumnType = column.getProcedureColumnType();
                propertyNameOptionMap.put(propertyName, procedureColumnType.toString());
                propertyNameColumnNameMap.put(propertyName, resolvedColumnName);
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
    //                                                                         Column Name
    //                                                                         ===========
    // -----------------------------------------------------
    //                                   Resolve Column Name
    //                                   -------------------
    protected String resolveColumnName(DfProcedureMeta procedure, int index, boolean alreadyReturnNameResolved,
            DfProcedureColumnMeta column) {
        final String resolvedColumnName;
        final String plainColumnName = column.getColumnName();
        if (Srl.is_NotNull_and_NotTrimmedEmpty(plainColumnName)) {
            resolvedColumnName = resolveVendorColumnNameHeadable(plainColumnName);
        } else {
            resolvedColumnName = deriveNoNameColumnResolvedName(procedure, column, index, alreadyReturnNameResolved);
        }
        return resolvedColumnName;
    }

    // -----------------------------------------------------
    //                           NoName Column Resolved Name
    //                           ---------------------------
    protected String deriveNoNameColumnResolvedName(DfProcedureMeta procedure, DfProcedureColumnMeta column, int index,
            boolean alreadyReturnExists) {
        final int argNumber = index + 1;
        if (getLittleAdjustmentProperties().isCompatibleProcedureReturnValueNameAsArg()) {
            return buildNoNameColumnDefaultSimpleArgName(argNumber);
        }
        // use standard name to avoid "arg1" return name (e.g. Oracle)
        // https://github.com/dbflute/dbflute-core/issues/230
        final String returnKeyword = "returnValue"; // according to PostgreSQL
        if (column.isProcedureColumnType_Return()) {
            if (alreadyReturnExists) { // second or more return value, just in case
                return returnKeyword + argNumber; // e.g. returnValue2
            } else { // return standard here
                return returnKeyword;
            }
        } else {
            return buildNoNameColumnDefaultSimpleArgName(argNumber);
        }
    }

    protected String buildNoNameColumnDefaultSimpleArgName(final int argNumber) {
        return "arg" + argNumber;
    }

    // ===================================================================================
    //                                                                       Property Type
    //                                                                       =============
    protected ProcedurePropertyInfo processProcedureProperty(DfProcedureMeta procedure, String resolvedColumnName, String pmbName,
            DfProcedureColumnMeta column, String propertyName) {
        final ProcedurePropertyInfo propertyInfo = new ProcedurePropertyInfo();
        propertyInfo.setResolvedColumnName(resolvedColumnName);
        propertyInfo.setColumnMeta(column);
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
        final String dbTypeName = column.getDbTypeName();

        final String propertyType =
                findPropertyType(procedure, resolvedColumnName, jdbcDefType, dbTypeName, columnSize, decimalDigits, () -> {
                    // special type should be after pointTypeMapping
                    return doProcessSpecialType(procedure, pmbName, column, propertyInfo); // as afterPointMapping
                });
        propertyInfo.setPropertyType(propertyType);
        return propertyInfo;
    }

    // -----------------------------------------------------
    //                                          Special Type
    //                                          ------------
    protected String doProcessSpecialType(DfProcedureMeta procedure, String pmbName, DfProcedureColumnMeta column,
            ProcedurePropertyInfo propertyInfo) {
        if (getLittleAdjustmentProperties().isAvailableDatabaseNativeJDBC()) {
            final String wallOfOracleType = doProcessGreatWallOfOracleType(procedure, pmbName, column, propertyInfo);
            if (Srl.is_NotNull_and_NotTrimmedEmpty(wallOfOracleType)) {
                return wallOfOracleType;
            }
        }
        if (column.isOracleNumber() && isOracleUnlabeledNumber(column)) {
            // because the length setting of procedure parameter is unsupported on Oracle
            //  e.g. o "sea_id number", x "sea_id number(14,2)"
            // however table-related type can be set e.g. sea_id MAIHAMA.SEA_ID%TYPE
            // so fixed the if-statement as pinpoint determination (2025/04/19)
            return TypeMap.getDefaultDecimalJavaNativeType(); // not null
        }
        return null;
    }

    protected String doProcessGreatWallOfOracleType(DfProcedureMeta procedure, String pmbName, DfProcedureColumnMeta column,
            ProcedurePropertyInfo propertyInfo) {
        final String propertyType;
        if (column.isOracleTreatedAsArray() && column.hasTypeArrayInfo()) {
            // here dbTypeName is "PL/SQL TABLE" or "TABLE" or "VARRAY"
            // (it's not useful for type mapping, so search like this)
            final DfTypeArrayInfo arrayInfo = column.getTypeArrayInfo();
            final String processArrayProperty = doProcessArrayProperty(procedure, arrayInfo, propertyInfo);
            propertyType = getGenericListClassName(processArrayProperty);
        } else if (column.isOracleStruct() && column.hasTypeStructInfo()) {
            final DfTypeStructInfo structInfo = column.getTypeStructInfo();
            propertyType = doProcessStructProperty(procedure, structInfo, propertyInfo);
        } else {
            propertyType = null;
        }
        return propertyType;
    }

    protected boolean isOracleUnlabeledNumber(DfProcedureColumnMeta column) {
        final Integer columnSize = column.getColumnSize();
        final Integer decimalDigits = column.getDecimalDigits();

        // #for_now jflute NUMBER(22) means unlabeled number type at least current Oracle JDBC (2025/04/20)
        final boolean fixedNumber = columnSize != null && columnSize.equals(22);
        final boolean nonDigits = decimalDigits == null || decimalDigits.equals(0);
        return fixedNumber && nonDigits;
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
    protected String doProcessArrayProperty(DfProcedureMeta procedure, DfTypeArrayInfo arrayInfo, ProcedurePropertyInfo propertyInfo) {
        final String propertyType;
        if (arrayInfo.hasNestedArray()) { // array in array
            final DfTypeArrayInfo nestedArrayInfo = arrayInfo.getNestedArrayInfo();
            final String nestedType = doProcessArrayProperty(procedure, nestedArrayInfo, propertyInfo); // recursive call
            propertyType = getGenericListClassName(nestedType);
        } else if (arrayInfo.hasElementStructInfo()) { // struct in array
            final DfTypeStructInfo structInfo = arrayInfo.getElementStructInfo();
            propertyType = doProcessStructProperty(procedure, structInfo, propertyInfo);
        } else { // scalar in array
            final String dbTypeName = arrayInfo.getElementType();
            final String resolvedColumnName = propertyInfo.getResolvedColumnName();
            propertyType = findPropertyType(procedure, resolvedColumnName, Types.OTHER, dbTypeName, null, null, null);
        }
        arrayInfo.setElementJavaNative(propertyType);
        return propertyType;
    }

    protected String findArrayScalarElementPropertyType(DfProcedureMeta procedure, String resolvedColumnName, DfTypeArrayInfo arrayInfo) {
        // by only name
        final String dbTypeName = arrayInfo.getElementType();
        return findPropertyType(procedure, resolvedColumnName, Types.OTHER, dbTypeName, null, null, null);
    }

    // -----------------------------------------------------
    //                                       Struct Property
    //                                       ---------------
    protected String doProcessStructProperty(DfProcedureMeta procedure, DfTypeStructInfo structInfo, ProcedurePropertyInfo propertyInfo) {
        // register entity generation for struct that contains nested array & struct 
        registerEntityInfoIfNeeds(procedure, structInfo, propertyInfo);

        // entityType is class name can used on program
        // so it adjusts project prefix here
        final String entityType = buildStructEntityType(structInfo);
        propertyInfo.setRefCustomizeEntity(true); // why propertyInfo is here, is only for this
        return entityType;
    }

    protected void registerEntityInfoIfNeeds(DfProcedureMeta procedure, DfTypeStructInfo structInfo, ProcedurePropertyInfo propertyInfo) {
        final String typeName = getStructEntityNameResouce(structInfo);
        if (!_entityInfoMap.containsKey(typeName)) { // because of independent objects and so called several times
            final StringKeyMap<DfColumnMeta> attrMap = structInfo.getAttributeInfoMap();
            _entityInfoMap.put(typeName, createEntityInfo(typeName, attrMap, structInfo));
            setupStructAttribute(procedure, structInfo, propertyInfo);
        }
    }

    protected void setupStructAttribute(DfProcedureMeta procedure, DfTypeStructInfo structInfo, ProcedurePropertyInfo propertyInfo) {
        final StringKeyMap<DfColumnMeta> attrMap = structInfo.getAttributeInfoMap();
        for (DfColumnMeta attrInfo : attrMap.values()) { // nested array or struct handling
            if (attrInfo.hasTypeArrayInfo()) { // array in struct
                final DfTypeArrayInfo typeArrayInfo = attrInfo.getTypeArrayInfo();
                if (typeArrayInfo.hasElementStructInfo()) { // struct in array in struct
                    registerEntityInfoIfNeeds(procedure, typeArrayInfo.getElementStructInfo(), propertyInfo);
                }
                if (typeArrayInfo.hasElementJavaNative()) {
                    final String elementJavaNative = typeArrayInfo.getElementJavaNative();
                    attrInfo.setSql2EntityForcedJavaNative(getGenericListClassName(elementJavaNative));
                } else {
                    final String elementType;
                    if (typeArrayInfo.hasNestedArray()) { // array in array in struct
                        final DfTypeArrayInfo nestedArrayInfo = typeArrayInfo.getNestedArrayInfo();
                        final String processArrayProperty = doProcessArrayProperty(procedure, nestedArrayInfo, propertyInfo);
                        elementType = getGenericListClassName(processArrayProperty);
                    } else if (typeArrayInfo.hasElementStructInfo()) { // struct in array in struct
                        final DfTypeStructInfo elementStructInfo = typeArrayInfo.getElementStructInfo();
                        elementType = buildStructEntityType(elementStructInfo);
                    } else { // scalar in array in struct
                        final String columnName = attrInfo.getColumnName(); // as resolved
                        elementType = findArrayScalarElementPropertyType(procedure, columnName, attrInfo.getTypeArrayInfo());
                    }
                    typeArrayInfo.setElementJavaNative(elementType);
                    attrInfo.setSql2EntityForcedJavaNative(getGenericListClassName(elementType));
                }
            } else if (attrInfo.hasTypeStructInfo()) {
                final DfTypeStructInfo nestedStructInfo = attrInfo.getTypeStructInfo();
                registerEntityInfoIfNeeds(procedure, nestedStructInfo, propertyInfo);
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
    protected DfCustomizeEntityInfo createEntityInfo(String entityName, Map<String, DfColumnMeta> columnMetaMap) {
        return doCreateEntityInfo(entityName, columnMetaMap, null);
    }

    protected DfCustomizeEntityInfo createEntityInfo(String entityName, Map<String, DfColumnMeta> columnMetaMap,
            DfTypeStructInfo structInfo) {
        return doCreateEntityInfo(entityName, columnMetaMap, structInfo);
    }

    protected DfCustomizeEntityInfo doCreateEntityInfo(String entityName, Map<String, DfColumnMeta> columnMetaMap,
            DfTypeStructInfo structInfo) {
        final DfCustomizeEntityInfo entityInfo;
        if (structInfo != null) {
            entityInfo = new DfCustomizeEntityInfo(entityName, columnMetaMap, structInfo);
        } else {
            entityInfo = new DfCustomizeEntityInfo(entityName, columnMetaMap);
        }
        entityInfo.setProcedureHandling(true);
        return entityInfo;
    }

    // -----------------------------------------------------
    //                                          Assist Logic
    //                                          ------------
    protected String getGenericListClassName(String element) {
        final DfLanguageGrammar grammarInfo = getBasicProperties().getLanguageDependency().getLanguageGrammar();
        return grammarInfo.buildGenericListClassName(element); // List<ELEMENT>
    }

    protected static class ProcedurePropertyInfo {
        protected String _resolvedColumnName; // not null after setup
        protected DfProcedureColumnMeta _columnMeta; // not null after setup
        protected String _propertyType; // not null after setup
        protected boolean _refCustomizeEntity;

        public String getResolvedColumnName() {
            return _resolvedColumnName;
        }

        public void setResolvedColumnName(String resolvedColumnName) {
            _resolvedColumnName = resolvedColumnName;
        }

        public DfProcedureColumnMeta getColumnMeta() {
            return _columnMeta;
        }

        public void setColumnMeta(DfProcedureColumnMeta columnMeta) {
            _columnMeta = columnMeta;
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
    //                                                                Property TypeMapping
    //                                                                ====================
    /**
     * @param procedure The meta of the current procedure. (NotNull)
     * @param resolvedColumnName The resolved name of the column for point typeMapping. (NullAllowed: almost not null but just in case)
     * @param jdbcDefType The type number of official JDBC definition.
     * @param dbTypeName The name of DB type on database. (NotNull)
     * @param columnSize The size of the column. (NullAllowed: if no meta)
     * @param decimalDigits The digits of the column. (NullAllowed: if no meta)
     * @param afterPointMapping The callback to mapping type after pointTypeMapping. (NullAllowed: not required)
     * @return The found Java native type of the column. (NotNull: Object as default)
     */
    protected String findPropertyType(DfProcedureMeta procedure, String resolvedColumnName, int jdbcDefType, String dbTypeName,
            Integer columnSize, Integer decimalDigits, Supplier<String> afterPointMapping) {
        if (resolvedColumnName != null) { // try point typeMapping
            // point typeMapping for proceudre parameter since 1.2.9
            final DfOutsideSqlProperties prop = getOutsideSqlProperties();
            final String procedureName = procedure.getProcedureName();
            final String translatedType = prop.translateProcedureParameterJdbcType(procedureName, resolvedColumnName);
            if (Srl.is_NotNull_and_NotTrimmedEmpty(translatedType)) { // translated
                return TypeMap.findJavaNativeByJdbcType(translatedType, columnSize, decimalDigits); // not null
            }
        }
        if (afterPointMapping != null) { // after-point mapping
            final String afterPointType = afterPointMapping.get();
            if (Srl.is_NotNull_and_NotTrimmedEmpty(afterPointType)) { // use after-point type
                return afterPointType;
            }
        }
        if (_columnExtractor.hasMappingJdbcType(jdbcDefType, dbTypeName)) { // normal mapping 
            final String torqueType = _columnExtractor.getColumnJdbcType(jdbcDefType, dbTypeName);
            return TypeMap.findJavaNativeByJdbcType(torqueType, columnSize, decimalDigits); // not null
        } else { // finally
            return "Object"; // procedure has many-many types so it uses Object type (not String)

            // it's not complete because nested properties are not target
            // for example, attributes in STRUCT type
            // but it's OK, that's the specification of DBFlute
        }
    }

    // ===================================================================================
    //                                                                        Convert Name
    //                                                                        ============
    // -----------------------------------------------------
    //                                    ParameterBean Name
    //                                    ------------------
    protected String convertProcedureNameToPmbName(String procedureName) {
        // here you do not need to handle project prefix
        // because the prefix is resolved at table object
        procedureName = Srl.replace(procedureName, ".", "_");
        procedureName = Srl.replace(procedureName, "@", "_"); // e.g. Oracle's DBLink
        procedureName = resolveVendorProcedureNameHeadache(procedureName);
        return Srl.camelize(procedureName) + "Pmb";
    }

    protected String convertProcedurePmbNameToEntityName(String pmbName, String propertyName) {
        final String baseName = Srl.substringLastFront(pmbName, "Pmb");
        return baseName + Srl.initCap(propertyName);
    }

    // -----------------------------------------------------
    //                                         Property Name
    //                                         -------------
    protected String convertColumnNameToPropertyName(String columnName) {
        columnName = resolveVendorColumnNameHeadable(columnName);
        return Srl.initBeansProp(Srl.camelize(columnName));
    }

    // -----------------------------------------------------
    //                                         Property Type
    //                                         -------------
    protected String convertProcedureListPropertyType(String entityName) {
        // propertyType is class name can used on program
        // so it adjusts project prefix here
        final String projectPrefix = getBasicProperties().getProjectPrefix();
        final String propertyType = projectPrefix + entityName;
        final DfLanguageGrammar grammarInfo = getBasicProperties().getLanguageDependency().getLanguageGrammar();
        return grammarInfo.buildGenericListClassName(propertyType);
    }

    // ===================================================================================
    //                                                                 Resolve Vendor Name
    //                                                                 ===================
    protected String resolveVendorProcedureNameHeadache(String procedureName) {
        if (getBasicProperties().isDatabaseSQLServer()) { // SQLServer returns 'sp_foo;1'
            procedureName = Srl.substringLastFront(procedureName, ";");
        }
        return procedureName;
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
