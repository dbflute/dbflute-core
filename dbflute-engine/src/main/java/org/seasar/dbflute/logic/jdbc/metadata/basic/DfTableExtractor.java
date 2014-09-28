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
package org.seasar.dbflute.logic.jdbc.metadata.basic;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.engine.database.model.UnifiedSchema;
import org.seasar.dbflute.helper.StringSet;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfTableMeta;
import org.seasar.dbflute.properties.assistant.DfAdditionalSchemaInfo;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.Srl;

/**
 * This class generates an XML schema of an existing database from JDBC meta data.
 * @author jflute
 */
public class DfTableExtractor extends DfAbstractMetaDataBasicExtractor {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log _log = LogFactory.getLog(DfTableExtractor.class);

    // ===================================================================================
    //                                                                        Meta Getting
    //                                                                        ============
    /**
     * Get all the table names in the current database that are not system tables. <br />
     * This does not contain additional schema. only specified schema is considered.
     * @param metaData JDBC database meta data. (NotNull)
     * @param unifiedSchema The unified schema that can contain catalog name and no-name mark. (NullAllowed)
     * @return The list of all the table meta info in a database.
     * @throws SQLException
     */
    public List<DfTableMeta> getTableList(DatabaseMetaData metaData, UnifiedSchema unifiedSchema) throws SQLException {
        return doGetTableList(metaData, unifiedSchema);
    }

    protected List<DfTableMeta> doGetTableList(DatabaseMetaData metaData, UnifiedSchema unifiedSchema)
            throws SQLException {
        final String[] objectTypes = getRealObjectTypeTargetArray(unifiedSchema);
        final List<DfTableMeta> tableList = new ArrayList<DfTableMeta>();
        ResultSet rs = null;
        try {
            _log.info("...Getting tables:");
            _log.info("  schema = " + unifiedSchema);
            _log.info("  types  = " + DfCollectionUtil.newArrayList(objectTypes));
            final String catalogName = unifiedSchema.getPureCatalog();
            final String schemaName = unifiedSchema.getPureSchema();
            rs = metaData.getTables(catalogName, schemaName, "%", objectTypes);
            while (rs.next()) {
                // /- - - - - - - - - - - - - - - - - - - - - - - - - - - -
                // basically uses getString() because
                // a JDBC driver might return an unexpected accident
                // (other methods are used only when an item can be trust)
                // - - - - - - - - - -/

                final String tableName = rs.getString("TABLE_NAME");
                final String tableType = rs.getString("TABLE_TYPE");
                final String tableCatalog;
                {
                    final String plainCatalog = rs.getString("TABLE_CAT");
                    if (Srl.is_NotNull_and_NotTrimmedEmpty(plainCatalog)) { // because PostgreSQL returns null
                        tableCatalog = plainCatalog;
                    } else {
                        tableCatalog = catalogName;
                    }
                }
                final String tableSchema = rs.getString("TABLE_SCHEM");
                final String tableComment = rs.getString("REMARKS");

                // create new original unified schema for this table
                final UnifiedSchema tableUnifiedSchema = createAsDynamicSchema(tableCatalog, tableSchema);

                if (isTableExcept(tableUnifiedSchema, tableName)) {
                    _log.info(tableName + " is excepted!");
                    continue;
                }
                if (isSystemTableForDBMS(tableName)) {
                    _log.info(tableName + " is excepted! {system table}");
                    continue;
                }

                final DfTableMeta tableMetaInfo = new DfTableMeta();
                tableMetaInfo.setTableName(tableName);
                tableMetaInfo.setTableType(tableType);
                tableMetaInfo.setUnifiedSchema(tableUnifiedSchema);
                tableMetaInfo.setTableComment(tableComment);
                tableList.add(tableMetaInfo);
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
        return tableList;
    }

    public boolean isSystemTableForDBMS(String tableName) {
        if (isDatabaseOracle() && tableName.startsWith("BIN$")) {
            return true;
        }
        if (isDatabaseSQLServer()) {
            final Set<String> systemSet = StringSet.createAsFlexible();
            systemSet.add("sysobjects");
            systemSet.add("sysconstraints");
            systemSet.add("syssegments");
            if (systemSet.contains(tableName)) {
                return true;
            }
        }
        if (isDatabaseSQLite() && tableName.startsWith("sqlite_")) {
            return true;
        }
        return false;
    }

    protected String[] getRealObjectTypeTargetArray(UnifiedSchema unifiedSchema) {
        if (unifiedSchema != null) {
            final DfAdditionalSchemaInfo schemaInfo = getAdditionalSchemaInfo(unifiedSchema);
            if (schemaInfo != null) {
                final List<String> objectTypeTargetList = schemaInfo.getObjectTypeTargetList();
                assertObjectTypeTargetListNotEmpty(unifiedSchema, objectTypeTargetList);
                return objectTypeTargetList.toArray(new String[objectTypeTargetList.size()]);
            }
        }
        final List<String> objectTypeTargetList = getProperties().getDatabaseProperties().getObjectTypeTargetList();
        assertObjectTypeTargetListNotEmpty(unifiedSchema, objectTypeTargetList);
        return objectTypeTargetList.toArray(new String[objectTypeTargetList.size()]);
    }

    protected void assertObjectTypeTargetListNotEmpty(UnifiedSchema unifiedSchema, List<String> objectTypeTargetList) {
        if (objectTypeTargetList == null || objectTypeTargetList.isEmpty()) {
            String msg = "The property 'objectTypeTargetList' should be required:";
            msg = msg + " unifiedSchema=" + unifiedSchema;
            throw new IllegalStateException(msg);
        }
    }

    public Map<String, DfTableMeta> getTableMap(DatabaseMetaData metaData, UnifiedSchema unifiedSchema)
            throws SQLException {
        final List<DfTableMeta> tableList = getTableList(metaData, unifiedSchema);
        final Map<String, DfTableMeta> map = DfCollectionUtil.newLinkedHashMap();
        for (DfTableMeta tableInfo : tableList) {
            map.put(tableInfo.getTableName(), tableInfo);
        }
        return map;
    }

    // ===================================================================================
    //                                                                        Table Except
    //                                                                        ============
    /**
     * Is the table name out of sight?
     * @param unifiedSchema The unified schema that can contain catalog name and no-name schema. (NullAllowed)
     * @param tableName The name of table. (NotNull)
     * @return The determination, true or false.
     */
    public boolean isTableExcept(UnifiedSchema unifiedSchema, final String tableName) {
        if (tableName == null) {
            throw new IllegalArgumentException("The argument 'tableName' should not be null.");
        }
        if (_suppressExceptTarget) {
            return false;
        }
        final List<String> tableTargetList = getRealTableTargetList(unifiedSchema);
        final List<String> tableExceptList = getRealTableExceptList(unifiedSchema);
        return !isTargetByHint(tableName, tableTargetList, tableExceptList);
    }

    protected List<String> getRealTableExceptList(UnifiedSchema unifiedSchema) { // extension point
        if (unifiedSchema != null) {
            final DfAdditionalSchemaInfo schemaInfo = getAdditionalSchemaInfo(unifiedSchema);
            if (schemaInfo != null) {
                return schemaInfo.getTableExceptList();
            }
        }
        return getProperties().getDatabaseProperties().getTableExceptList();
    }

    protected List<String> getRealTableTargetList(UnifiedSchema unifiedSchema) { // extension point
        if (unifiedSchema != null) {
            final DfAdditionalSchemaInfo schemaInfo = getAdditionalSchemaInfo(unifiedSchema);
            if (schemaInfo != null) {
                return schemaInfo.getTableTargetList();
            }
        }
        return getProperties().getDatabaseProperties().getTableTargetList();
    }
}