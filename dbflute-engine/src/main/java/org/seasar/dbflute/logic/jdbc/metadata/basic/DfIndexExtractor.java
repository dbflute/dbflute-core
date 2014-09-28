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
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.engine.database.model.UnifiedSchema;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfTableMeta;
import org.seasar.dbflute.properties.facade.DfDatabaseTypeFacadeProp;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.8.2 (2008/10/18 Saturday)
 */
public class DfIndexExtractor extends DfAbstractMetaDataBasicExtractor {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log _log = LogFactory.getLog(DfIndexExtractor.class);

    // ===================================================================================
    //                                                                        Meta Getting
    //                                                                        ============
    public Map<String, Map<Integer, String>> getIndexMap(DatabaseMetaData metaData, DfTableMeta tableInfo,
            Map<String, Map<Integer, String>> uniqueKeyMap) throws SQLException { // non unique only
        final UnifiedSchema unifiedSchema = tableInfo.getUnifiedSchema();
        final String tableName = tableInfo.getTableName();
        if (tableInfo.isTableTypeView()) {
            return newLinkedHashMap();
        }
        return getIndexMap(metaData, unifiedSchema, tableName, uniqueKeyMap);
    }

    public Map<String, Map<Integer, String>> getIndexMap(DatabaseMetaData metaData, UnifiedSchema unifiedSchema,
            String tableName, Map<String, Map<Integer, String>> uniqueKeyMap) throws SQLException { // non unique only
        final String translatedName = translateTableCaseName(tableName);
        Map<String, Map<Integer, String>> map = doGetIndexMap(metaData, unifiedSchema, translatedName, uniqueKeyMap,
                false);
        if (isRetryCaseInsensitiveIndex()) {
            if (map.isEmpty() && !translatedName.equals(translatedName.toLowerCase())) { // retry by lower case
                map = doGetIndexMap(metaData, unifiedSchema, translatedName.toLowerCase(), uniqueKeyMap, true);
            }
            if (map.isEmpty() && !translatedName.equals(translatedName.toUpperCase())) { // retry by upper case
                map = doGetIndexMap(metaData, unifiedSchema, translatedName.toUpperCase(), uniqueKeyMap, true);
            }
        }
        return map;
    }

    protected Map<String, Map<Integer, String>> doGetIndexMap(DatabaseMetaData metaData, UnifiedSchema unifiedSchema,
            String tableName, Map<String, Map<Integer, String>> uniqueKeyMap, boolean retry) throws SQLException { // non unique only
        final Map<String, Map<Integer, String>> indexMap = newTableConstraintMap();
        ResultSet rs = null;
        try {
            rs = extractIndexMetaData(metaData, unifiedSchema, tableName, retry);
            if (rs == null) {
                return DfCollectionUtil.newHashMap();
            }
            while (rs.next()) {
                // /- - - - - - - - - - - - - - - - - - - - - - - -
                // same policy as table process about JDBC handling
                // (see DfTableHandler.java)
                // - - - - - - - - - -/

                final String metaTableName = rs.getString(3);
                if (checkMetaTableDiffIfNeeds(tableName, metaTableName)) {
                    continue;
                }

                final String indexName = rs.getString(6);
                final boolean isNonUnique;
                {
                    final Boolean nonUnique = rs.getBoolean(4);
                    isNonUnique = (nonUnique != null && nonUnique);
                }
                if (!isNonUnique) {
                    continue;
                }
                if (uniqueKeyMap != null && uniqueKeyMap.containsKey(indexName)) {
                    continue;
                }

                // Non Unique Only

                final String indexType;
                {
                    indexType = rs.getString(7);
                }

                final String columnName = rs.getString(9);
                if (columnName == null || columnName.trim().length() == 0) {
                    continue;
                }
                if (isColumnExcept(unifiedSchema, tableName, columnName)) {
                    continue;
                }
                final Integer ordinalPosition;
                {
                    final String ordinalPositionString = rs.getString(8);
                    if (ordinalPositionString == null) {
                        String msg = "The unique columnName should have ordinal-position but null: ";
                        msg = msg + " columnName=" + columnName + " indexType=" + indexType;
                        _log.warn(msg);
                        continue;
                    }
                    try {
                        ordinalPosition = Integer.parseInt(ordinalPositionString);
                    } catch (NumberFormatException e) {
                        String msg = "The unique column should have ordinal-position as number but: ";
                        msg = msg + ordinalPositionString + " columnName=" + columnName + " indexType=" + indexType;
                        _log.warn(msg);
                        continue;
                    }
                }

                if (indexMap.containsKey(indexName)) {
                    final Map<Integer, String> indexElementMap = indexMap.get(indexName);
                    indexElementMap.put(ordinalPosition, columnName);
                } else {
                    final Map<Integer, String> indexElementMap = newLinkedHashMap();
                    indexElementMap.put(ordinalPosition, columnName);
                    indexMap.put(indexName, indexElementMap);
                }
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
        return indexMap;
    }

    protected ResultSet extractIndexMetaData(DatabaseMetaData metaData, UnifiedSchema unifiedSchema, String tableName,
            boolean retry) throws SQLException {
        final boolean uniqueKeyOnly = false;
        final DfDatabaseTypeFacadeProp prop = getDatabaseTypeFacadeProp();
        return delegateExtractIndexInfoMetaData(metaData, unifiedSchema, tableName, uniqueKeyOnly, retry, prop);
    }

    // public static for recycle
    public static ResultSet delegateExtractIndexInfoMetaData(DatabaseMetaData metaData, UnifiedSchema unifiedSchema,
            String tableName, boolean uniqueKeyOnly, boolean retry, DfDatabaseTypeFacadeProp prop) throws SQLException {
        final String catalogName = unifiedSchema.getPureCatalog();
        final String schemaName = unifiedSchema.getPureSchema();
        try {
            return metaData.getIndexInfo(catalogName, schemaName, tableName, uniqueKeyOnly, true);
        } catch (SQLException e) {
            if (prop.isDatabaseOracle() && !Srl.isQuotedDouble(tableName)) {
                // Oracle JDBC Driver does not allow Japanese table names
                // about index info so retry it with quoted here
                // (however PK, FK are allowed about it...)
                final String quoted = Srl.quoteDouble(tableName);
                try {
                    return metaData.getIndexInfo(catalogName, schemaName, quoted, uniqueKeyOnly, true);
                } catch (SQLException ignored) {
                }
            }
            if (retry) {
                // because the exception may be thrown when the table is not found
                // (for example, Sybase)
                return null;
            } else {
                throw e;
            }
        }
    }
}