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
package org.seasar.dbflute.logic.jdbc.mapping;

import java.sql.Types;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.torque.engine.database.model.TypeMap;
import org.seasar.dbflute.logic.generate.language.DfLanguageDependency;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfColumnMeta;
import org.seasar.dbflute.util.DfNameHintUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfJdbcTypeMapper {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Map<String, String> _nameToJdbcTypeMap;
    protected final Map<String, Map<String, String>> _pointToJdbcTypeMap;
    protected final DfMapperResource _resource;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    public DfJdbcTypeMapper(Map<String, String> nameToJdbcTypeMap, Map<String, Map<String, String>> pointToJdbcTypeMap,
            DfMapperResource resource) {
        _nameToJdbcTypeMap = nameToJdbcTypeMap;
        _pointToJdbcTypeMap = pointToJdbcTypeMap;
        _resource = resource;
    }

    public static interface DfMapperResource {
        DfLanguageDependency getLang();

        boolean isDbmsPostgreSQL();

        boolean isDbmsOracle();

        boolean isDbmsDB2();

        boolean isDbmsSQLServer();

        boolean isDbmsDerby();
    }

    // ===================================================================================
    //                                                                 Torque Type Getting
    //                                                                 ===================
    /**
     * Get the JDBC type of the column. (contains point type-mapping) <br />
     * Look at the java-doc of overload method if you want to know the priority of mapping.
     * @param columnMeta The meta information of column. (NotNull)
     * @return The JDBC type of the column. (NotNull)
     */
    public String getColumnJdbcType(DfColumnMeta columnMeta) {
        final String pointMappingType = findPointMappingType(columnMeta);
        if (pointMappingType != null) {
            return pointMappingType;
        }
        return getColumnJdbcType(columnMeta.getJdbcDefValue(), columnMeta.getDbTypeName());
    }

    protected String findPointMappingType(DfColumnMeta columnMeta) {
        final String tableName = columnMeta.getTableName();
        if (tableName == null) {
            return null;
        }
        Map<String, String> columnTypeMap = _pointToJdbcTypeMap.get(tableName);
        final String foundType = doFindPointMappingType(columnMeta, columnTypeMap);
        if (foundType != null) {
            return foundType;
        }
        columnTypeMap = _pointToJdbcTypeMap.get("$$ALL$$");
        return doFindPointMappingType(columnMeta, columnTypeMap);
    }

    protected String doFindPointMappingType(DfColumnMeta columnMeta, Map<String, String> columnTypeMap) {
        if (columnTypeMap != null) {
            final String columnName = columnMeta.getColumnName();
            for (Entry<String, String> entry : columnTypeMap.entrySet()) {
                final String columnHint = entry.getKey();
                if (DfNameHintUtil.isHitByTheHint(columnName, columnHint)) {
                    final String value = entry.getValue();
                    if (value != null) {
                        return value;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Get the JDBC type of the column. <br /> 
     * The priority of mapping is as follows:
     * <pre>
     * 1. The specified type mapping by DB type name (typeMappingMap.dfprop)
     * 2. The fixed type mapping (PostgreSQL's OID and Oracle's Date and so on...)
     * 3. The standard type mapping by JDBC type if the type is not 'OTHER' (typeMappingMap.dfprop)
     * 4. The auto type mapping by DB type name
     * 5. String finally
     * </pre>
     * @param jdbcDefType The definition type of JDBC.
     * @param dbTypeName The name of DB data type. (NullAllowed: If null, the mapping using this is invalid)
     * @return The JDBC type of the column. (NotNull)
     */
    public String getColumnJdbcType(int jdbcDefType, String dbTypeName) {
        final String jdbcType = doGetColumnJdbcType(jdbcDefType, dbTypeName);
        if (jdbcType == null) {
            // * * * * * *
            // Priority 5
            // * * * * * *
            return getVarcharJdbcType();
        }
        return jdbcType;
    }

    /**
     * Does it have a mapping about the type?
     * @param jdbcDefType The definition type of JDBC.
     * @param dbTypeName The name of DB data type. (NullAllowed: If null, the mapping using this is invalid)
     * @return The JDBC type of the column. (NotNull)
     */
    public boolean hasMappingJdbcType(int jdbcDefType, String dbTypeName) {
        return doGetColumnJdbcType(jdbcDefType, dbTypeName) != null;
    }

    public String doGetColumnJdbcType(int jdbcDefType, String dbTypeName) {
        // * * * * * *
        // Priority 1
        // * * * * * *
        if (dbTypeName != null) {
            if (_nameToJdbcTypeMap != null && !_nameToJdbcTypeMap.isEmpty()) {
                final String torqueType = _nameToJdbcTypeMap.get(dbTypeName);
                if (torqueType != null) {
                    return (String) torqueType;
                }
            }
        }

        // * * * * * *
        // Priority 2
        // * * * * * *
        final String adjustment = processForcedAdjustment(jdbcDefType, dbTypeName);
        if (adjustment != null) {
            return adjustment;
        }

        // * * * * * *
        // Priority 3
        // * * * * * *
        if (!isOtherType(jdbcDefType)) {
            final String jdbcType = getJdbcType(jdbcDefType);
            if (Srl.is_NotNull_and_NotEmpty(jdbcType)) {
                return jdbcType;
            }
        }
        // here means that it cannot determine by jdbcDefValue

        // * * * * * *
        // Priority 4
        // * * * * * *
        // /- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        // Here is coming if the JDBC type is OTHER or is not found in TypeMap.
        // - - - - - - - - - -/
        if (containsIgnoreCase(dbTypeName, "varchar")) {
            return getVarcharJdbcType();
        } else if (containsIgnoreCase(dbTypeName, "char")) {
            return getCharJdbcType();
        } else if (containsIgnoreCase(dbTypeName, "numeric", "number", "decimal")) {
            return getNumericJdbcType();
        } else if (containsIgnoreCase(dbTypeName, "timestamp", "datetime")) {
            return getTimestampJdbcType();
        } else if (containsIgnoreCase(dbTypeName, "date")) {
            return getDateJdbcType();
        } else if (containsIgnoreCase(dbTypeName, "time")) {
            return getTimeJdbcType();
        } else if (containsIgnoreCase(dbTypeName, "clob")) {
            return getClobJdbcType();
        } else if (containsIgnoreCase(dbTypeName, "blob")) {
            return getBlobJdbcType();
        } else {
            return null;
        }
    }

    protected String processForcedAdjustment(int jdbcDefValue, String dbTypeName) {
        if (isConceptTypeUUID(dbTypeName)) {
            final String uuid = _resource.getLang().getLanguageTypeMapping().getJdbcTypeOfUUID();
            if (uuid != null) { // might be unsupported in any language
                return uuid;
            }
        }
        if (isConceptTypeBytesOid(dbTypeName)) {
            return getBlobJdbcType();
        }
        // interval type needs ... string? at PostgreSQL-9.x!?
        // (it had been worked but error now)
        //if (isPostgreSQLInterval(dbTypeName)) {
        //    return getTimeJdbcType();
        //}
        if (isOracleCompatibleDate(jdbcDefValue, dbTypeName)) {
            // for compatibility to Oracle's JDBC driver
            return getDateJdbcType();
        }
        return null;
    }

    // -----------------------------------------------------
    //                                          Concept Type
    //                                          ------------
    public boolean isConceptTypeUUID(final String dbTypeName) { // mapped by UUID
        if (isPostgreSQLUuid(dbTypeName)) {
            return true;
        }
        if (isSQLServerUniqueIdentifier(dbTypeName)) {
            return true;
        }
        return false;
    }

    /**
     * Is the type 'PlainClob' as concept type? </br >
     * This type is not related to a way of JDBC handling,
     * whether the type can be purely called 'CLOB type' or not. <br />
     * But 'text' type is not contained to it.
     * @param dbTypeName The name of DB type. (NotNull)
     * @return The determination, true or false.
     */
    public boolean isConceptTypePlainClob(final String dbTypeName) { // all CLOB type
        return isOracleClob(dbTypeName) || isDB2Clob(dbTypeName) || isDerbyClob(dbTypeName);
    }

    /**
     * Is the type 'StringClob' as concept type? </br >
     * It means the type needs to be handled as stream on JDBC.
     * @param dbTypeName The name of DB type. (NotNull)
     * @return The determination, true or false.
     */
    public boolean isConceptTypeStringClob(final String dbTypeName) { // needs stream
        // only Oracle's CLOB (it can get all text by getString() on DB2)
        return isOracleClob(dbTypeName);
    }

    public boolean isConceptTypeBytesOid(final String dbTypeName) {
        // now only PostgreSQL's oid
        return isPostgreSQLOid(dbTypeName);
    }

    public boolean isConceptTypeFixedLengthString(final String dbTypeName) {
        return isPostgreSQLBpChar(dbTypeName); // procedure only
    }

    public boolean isConceptTypeObjectBindingBigDecimal(final String dbTypeName) {
        return isPostgreSQLNumeric(dbTypeName); // procedure only
    }

    // -----------------------------------------------------
    //                                         Pinpoint Type
    //                                         -------------
    public boolean isPostgreSQLBpChar(final String dbTypeName) {
        return _resource.isDbmsPostgreSQL() && matchIgnoreCase(dbTypeName, "bpchar");
    }

    public boolean isPostgreSQLNumeric(final String dbTypeName) {
        return _resource.isDbmsPostgreSQL() && matchIgnoreCase(dbTypeName, "numeric");
    }

    public boolean isPostgreSQLUuid(final String dbTypeName) {
        return _resource.isDbmsPostgreSQL() && matchIgnoreCase(dbTypeName, "uuid");
    }

    public boolean isPostgreSQLOid(final String dbTypeName) {
        return _resource.isDbmsPostgreSQL() && matchIgnoreCase(dbTypeName, "oid");
    }

    public boolean isPostgreSQLInterval(final String dbTypeName) {
        return _resource.isDbmsPostgreSQL() && matchIgnoreCase(dbTypeName, "interval");
    }

    public boolean isPostgreSQLCursor(final String dbTypeName) {
        return _resource.isDbmsPostgreSQL() && containsIgnoreCase(dbTypeName, "cursor");
    }

    public boolean isOracleClob(final String dbTypeName) {
        return _resource.isDbmsOracle() && containsIgnoreCase(dbTypeName, "clob");
    }

    public boolean isOracleNCharOrNVarchar(final String dbTypeName) {
        return _resource.isDbmsOracle() && containsIgnoreCase(dbTypeName, "nchar", "nvarchar");
    }

    public boolean isOracleNCharOrNVarcharOrNClob(final String dbTypeName) {
        return _resource.isDbmsOracle() && containsIgnoreCase(dbTypeName, "nchar", "nvarchar", "nclob");
    }

    public boolean isOracleNumber(final String dbTypeName) {
        return _resource.isDbmsOracle() && matchIgnoreCase(dbTypeName, "number");
    }

    public boolean isOracleDate(final String dbTypeName) {
        return _resource.isDbmsOracle() && matchIgnoreCase(dbTypeName, "date");
    }

    public boolean isOracleCompatibleDate(final int jdbcType, final String dbTypeName) {
        return _resource.isDbmsOracle() && java.sql.Types.TIMESTAMP == jdbcType && matchIgnoreCase(dbTypeName, "date");
    }

    public boolean isOracleBinaryFloatDouble(final String dbTypeName) {
        return _resource.isDbmsOracle() && matchIgnoreCase(dbTypeName, "binary_float", "binary_double");
    }

    public boolean isOracleCursor(final String dbTypeName) {
        return _resource.isDbmsOracle() && containsIgnoreCase(dbTypeName, "cursor");
    }

    public boolean isOracleTable(final String dbTypeName) {
        return _resource.isDbmsOracle() && containsIgnoreCase(dbTypeName, "table");
    }

    public boolean isOracleVArray(final String dbTypeName) {
        return _resource.isDbmsOracle() && containsIgnoreCase(dbTypeName, "varray");
    }

    public boolean isDB2Clob(final String dbTypeName) {
        return _resource.isDbmsDB2() && containsIgnoreCase(dbTypeName, "clob");
    }

    public boolean isSQLServerUniqueIdentifier(final String dbTypeName) {
        return _resource.isDbmsSQLServer() && matchIgnoreCase(dbTypeName, "uniqueidentifier");
    }

    public boolean isDerbyClob(final String dbTypeName) {
        return _resource.isDbmsDerby() && containsIgnoreCase(dbTypeName, "clob");
    }

    // -----------------------------------------------------
    //                                             JDBC Type
    //                                             ---------
    protected boolean isOtherType(final int jdbcDefValue) {
        return Types.OTHER == jdbcDefValue;
    }

    protected String getJdbcType(int jdbcDefValue) {
        return TypeMap.findJdbcTypeByJdbcDefValue(jdbcDefValue);
    }

    protected String getVarcharJdbcType() {
        return TypeMap.findJdbcTypeByJdbcDefValue(java.sql.Types.VARCHAR);
    }

    protected String getCharJdbcType() {
        return TypeMap.findJdbcTypeByJdbcDefValue(java.sql.Types.CHAR);
    }

    protected String getNumericJdbcType() {
        return TypeMap.findJdbcTypeByJdbcDefValue(java.sql.Types.NUMERIC);
    }

    protected String getTimestampJdbcType() {
        return TypeMap.findJdbcTypeByJdbcDefValue(java.sql.Types.TIMESTAMP);
    }

    protected String getTimeJdbcType() {
        return TypeMap.findJdbcTypeByJdbcDefValue(java.sql.Types.TIME);
    }

    protected String getDateJdbcType() {
        return TypeMap.findJdbcTypeByJdbcDefValue(java.sql.Types.DATE);
    }

    protected String getClobJdbcType() {
        return TypeMap.findJdbcTypeByJdbcDefValue(java.sql.Types.CLOB);
    }

    protected String getBlobJdbcType() {
        return TypeMap.findJdbcTypeByJdbcDefValue(java.sql.Types.BLOB);
    }

    protected String getBinaryJdbcType() {
        return TypeMap.findJdbcTypeByJdbcDefValue(java.sql.Types.BINARY);
    }

    // ===================================================================================
    //                                                                     Matching Helper
    //                                                                     ===============
    protected boolean matchIgnoreCase(String dbTypeName, String... types) {
        if (dbTypeName == null) {
            return false;
        }
        for (String type : types) {
            if (dbTypeName.trim().equalsIgnoreCase(type.trim())) {
                return true;
            }
        }
        return false;
    }

    protected boolean containsIgnoreCase(String dbTypeName, String... types) {
        if (dbTypeName == null) {
            return false;
        }
        final String trimmedLowerName = dbTypeName.toLowerCase().trim();
        for (String type : types) {
            if (trimmedLowerName.contains(type.toLowerCase().trim())) {
                return true;
            }
        }
        return false;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return _nameToJdbcTypeMap + ":" + _resource;
    }
}