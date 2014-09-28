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
package org.seasar.dbflute.logic.jdbc.metadata.synonym;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.engine.database.model.UnifiedSchema;
import org.seasar.dbflute.helper.StringKeyMap;
import org.seasar.dbflute.helper.jdbc.facade.DfJdbcFacade;
import org.seasar.dbflute.logic.jdbc.metadata.DfAbstractMetaDataExtractor;
import org.seasar.dbflute.logic.jdbc.metadata.basic.DfAutoIncrementExtractor;
import org.seasar.dbflute.logic.jdbc.metadata.basic.DfForeignKeyExtractor;
import org.seasar.dbflute.logic.jdbc.metadata.basic.DfIndexExtractor;
import org.seasar.dbflute.logic.jdbc.metadata.basic.DfTableExtractor;
import org.seasar.dbflute.logic.jdbc.metadata.basic.DfUniqueKeyExtractor;
import org.seasar.dbflute.logic.jdbc.metadata.comment.DfDbCommentExtractor.UserColComments;
import org.seasar.dbflute.logic.jdbc.metadata.comment.DfDbCommentExtractor.UserTabComments;
import org.seasar.dbflute.logic.jdbc.metadata.comment.DfDbCommentExtractorOracle;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfColumnMeta;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfForeignKeyMeta;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfPrimaryKeyMeta;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfSynonymMeta;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfTableMeta;
import org.seasar.dbflute.logic.jdbc.metadata.synonym.DfDBLinkNativeExtractorOracle.DBLinkNativeInfo;
import org.seasar.dbflute.logic.jdbc.metadata.synonym.DfSynonymNativeExtractorOracle.SynonymNativeInfo;
import org.seasar.dbflute.util.DfCollectionUtil;

/**
 * @author jflute
 * @since 0.9.3 (2009/02/24 Tuesday)
 */
public class DfSynonymExtractorOracle extends DfAbstractMetaDataExtractor implements DfSynonymExtractor {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log _log = LogFactory.getLog(DfSynonymExtractorOracle.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected DataSource _dataSource;
    protected List<UnifiedSchema> _unifiedSchemaList;
    protected Map<String, DfTableMeta> _generatedTableMap;

    // -----------------------------------------------------
    //                                     Meta Data Handler
    //                                     -----------------
    protected final DfTableExtractor _tableExtractor = new DfTableExtractor();
    protected final DfUniqueKeyExtractor _uniqueKeyExtractor = new DfUniqueKeyExtractor();
    protected final DfAutoIncrementExtractor _autoIncrementExtractor = new DfAutoIncrementExtractor();
    protected final DfForeignKeyExtractor _foreignKeyExtractor = new DfForeignKeyExtractor();
    {
        // All foreign tables are target if the foreign table is except.
        // Because the filtering is executed when translating foreign keys.
        _foreignKeyExtractor.suppressExceptTarget();
    }
    protected final DfIndexExtractor _indexExtractor = new DfIndexExtractor();

    // ===================================================================================
    //                                                                             Extract
    //                                                                             =======
    public Map<String, DfSynonymMeta> extractSynonymMap() {
        final Map<String, DfSynonymMeta> synonymMap = StringKeyMap.createAsFlexibleOrdered();
        Map<String, Map<String, SynonymNativeInfo>> dbLinkSynonymNativeMap = null;
        final String sql = buildSynonymSelect();
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;
        try {
            conn = _dataSource.getConnection();
            statement = conn.createStatement();
            _log.info(sql);
            rs = statement.executeQuery(sql);
            while (rs.next()) {
                final UnifiedSchema synonymOwner = createAsDynamicSchema(null, rs.getString("OWNER"));
                final String synonymName = rs.getString("SYNONYM_NAME");
                final UnifiedSchema tableOwner = createAsDynamicSchema(null, rs.getString("TABLE_OWNER"));
                final String tableName = rs.getString("TABLE_NAME");
                final String dbLinkName = rs.getString("DB_LINK");

                if (_tableExtractor.isTableExcept(synonymOwner, synonymName)) {
                    // because it is not necessary to handle excepted tables 
                    continue;
                }

                final DfSynonymMeta info = new DfSynonymMeta();

                // Basic
                info.setSynonymOwner(synonymOwner);
                info.setSynonymName(synonymName);
                info.setTableOwner(tableOwner);
                info.setTableName(tableName);
                info.setDBLinkName(dbLinkName);

                // Select-able?
                judgeSynonymSelectable(info);

                if (info.isSelectable()) { // e.g. procedure synonym
                    // set up column definition for supplement info
                    final List<DfColumnMeta> columnMetaInfoList = getSynonymColumns(conn, synonymOwner, synonymName);
                    info.setColumnMetaInfoList(columnMetaInfoList);
                }

                if (dbLinkName != null && dbLinkName.trim().length() > 0) {
                    if (dbLinkSynonymNativeMap == null) { // lazy load
                        dbLinkSynonymNativeMap = extractDBLinkSynonymNativeMap();
                    }
                    // = = = = = = = = = = = = 
                    // It's a DB Link Synonym!
                    // = = = = = = = = = = = = 
                    try {
                        final String synonymKey = buildSynonymMapKey(synonymOwner, synonymName);
                        synonymMap.put(synonymKey, setupDBLinkSynonym(conn, info, dbLinkSynonymNativeMap));
                    } catch (Exception continued) {
                        _log.info("Failed to get meta data of " + synonymName + ": " + continued.getMessage());
                    }
                    continue;
                }
                if (!tableOwner.hasSchema()) {
                    continue; // basically no way because it may be for DB Link Synonym
                }

                // = = = = = = = = = = = = 
                // It's a normal Synonym!
                // = = = = = = = = = = = = 
                // PK, ID, UQ, FK, Index
                try {
                    setupBasicConstraintInfo(info, tableOwner, tableName, conn);
                } catch (Exception continued) {
                    _log.info("Failed to get meta data of " + synonymName + ": " + continued.getMessage());
                    continue;
                }
                final String synonymKey = buildSynonymMapKey(synonymOwner, synonymName);
                synonymMap.put(synonymKey, info);
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignored) {
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignored) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
        translateFKTable(synonymMap); // It translates foreign key meta informations. 
        setupTableColumnComment(synonymMap);
        return synonymMap;
    }

    protected String buildSynonymSelect() {
        final StringBuilder sb = new StringBuilder();
        int count = 0;
        for (UnifiedSchema unifiedSchema : _unifiedSchemaList) {
            if (count > 0) {
                sb.append(", ");
            }
            sb.append("'").append(unifiedSchema.getPureSchema()).append("'");
            ++count;
        }
        final String sql = "select * from ALL_SYNONYMS where OWNER in (" + sb.toString() + ")";
        return sql;
    }

    protected String buildSynonymMapKey(UnifiedSchema synonymOwner, String synonymName) {
        return synonymOwner.buildSchemaQualifiedName(synonymName);
    }

    protected void judgeSynonymSelectable(DfSynonymMeta info) {
        final DfJdbcFacade facade = new DfJdbcFacade(_dataSource);
        final String synonymSqlName = info.buildSynonymSqlName();
        final String sql = "select * from " + synonymSqlName + " where 0 = 1";
        try {
            final List<String> columnList = new ArrayList<String>();
            columnList.add("dummy");
            facade.selectStringList(sql, columnList);
            info.setSelectable(true);
        } catch (RuntimeException ignored) {
            info.setSelectable(false);
        }
    }

    protected void setupBasicConstraintInfo(DfSynonymMeta info, UnifiedSchema tableOwner, String tableName,
            Connection conn) throws SQLException {
        final DatabaseMetaData md = conn.getMetaData();
        final DfPrimaryKeyMeta pkInfo = getPKList(md, tableOwner, tableName);
        info.setPrimaryKey(pkInfo);
        final List<String> pkList = pkInfo.getPrimaryKeyList();
        if (info.isSelectable()) { // because it needs a select statement
            for (String primaryKeyName : pkList) {
                final boolean autoIncrement = isAutoIncrement(conn, tableOwner, tableName, primaryKeyName);
                if (autoIncrement) {
                    info.setAutoIncrement(autoIncrement);
                    break;
                }
            }
        }
        {
            final Map<String, Map<Integer, String>> uqMap = getUQMap(md, tableOwner, tableName, pkList);
            info.setUniqueKeyMap(uqMap);
        }
        {
            final Map<String, DfForeignKeyMeta> fkMap = getFKMap(md, tableOwner, tableName);
            info.setForeignKeyMap(fkMap); // It's tentative information at this timing!
        }
        {
            final Map<String, Map<Integer, String>> uqMap = info.getUniqueKeyMap();
            final Map<String, Map<Integer, String>> indexMap = getIndexMap(md, tableOwner, tableName, uqMap);
            info.setIndexMap(indexMap);
        }
    }

    protected DfSynonymMeta setupDBLinkSynonym(Connection conn, DfSynonymMeta info,
            Map<String, Map<String, SynonymNativeInfo>> dbLinkSynonymNativeMap) throws SQLException {
        if (!info.isSelectable()) { // e.g. procedure synonym
            return info;
        }
        final String tableName = info.getTableName();
        final String dbLinkName = info.getDBLinkName();

        final String realTableName = translateTableName(tableName, dbLinkName, dbLinkSynonymNativeMap);
        final DfPrimaryKeyMeta pkInfo = getDBLinkSynonymPKInfo(conn, realTableName, dbLinkName);
        info.setPrimaryKey(pkInfo);
        final Map<String, Map<Integer, String>> uniqueKeyMap = getDBLinkSynonymUQMap(conn, realTableName, dbLinkName);
        info.setUniqueKeyMap(uniqueKeyMap);

        // Foreign Key and Index of DBLink are unsupported.
        info.setForeignKeyMap(new LinkedHashMap<String, DfForeignKeyMeta>());
        info.setIndexMap(new LinkedHashMap<String, Map<Integer, String>>());

        return info;
    }

    protected String translateTableName(String basicName, String dbLinkName,
            Map<String, Map<String, SynonymNativeInfo>> dbLinkSynonymNativeMap) {
        final Map<String, SynonymNativeInfo> synonymNativeMap = dbLinkSynonymNativeMap.get(dbLinkName);
        if (synonymNativeMap == null) {
            return basicName;
        }
        final SynonymNativeInfo synonymNativeInfo = synonymNativeMap.get(basicName);
        if (synonymNativeInfo == null) { // not synonym at least
            return basicName;
        }
        // it's a synonym (but nested synonyms are unsupported)
        return synonymNativeInfo.getTableName();
    }

    // ===================================================================================
    //                                                                           Meta Data
    //                                                                           =========
    // -----------------------------------------------------
    //                                    For Normal Synonym
    //                                    ------------------
    protected DfPrimaryKeyMeta getPKList(DatabaseMetaData metaData, UnifiedSchema unifiedSchema, String tableName) {
        try {
            return _uniqueKeyExtractor.getPrimaryKey(metaData, unifiedSchema, tableName);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    protected Map<String, Map<Integer, String>> getUQMap(DatabaseMetaData metaData, UnifiedSchema unifiedSchema,
            String tableName, List<String> primaryKeyNameList) {
        try {
            return _uniqueKeyExtractor.getUniqueKeyMap(metaData, unifiedSchema, tableName, primaryKeyNameList);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    protected Map<String, DfForeignKeyMeta> getFKMap(DatabaseMetaData metaData, UnifiedSchema unifiedSchema,
            String tableName) {
        try {
            return _foreignKeyExtractor.getForeignKeyMap(metaData, unifiedSchema, tableName);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    protected Map<String, Map<Integer, String>> getIndexMap(DatabaseMetaData metaData, UnifiedSchema unifiedSchema,
            String tableName, Map<String, Map<Integer, String>> uniqueKeyMap) {
        try {
            return _indexExtractor.getIndexMap(metaData, unifiedSchema, tableName, uniqueKeyMap);
        } catch (SQLException e) {
            String msg = "Failed to extract indexes: " + tableName;
            throw new IllegalStateException(msg, e);
        }
    }

    protected boolean isAutoIncrement(Connection conn, UnifiedSchema tableOwner, String tableName,
            String primaryKeyColumnName) {
        return false; // because Oracle does not support identity
        //try {
        //    final DfTableMetaInfo tableMetaInfo = new DfTableMetaInfo();
        //    tableMetaInfo.setTableName(tableName);
        //    tableMetaInfo.setTableSchema(tableOwner);
        //    return _autoIncrementHandler.isAutoIncrementColumn(conn, tableMetaInfo, primaryKeyColumnName);
        //} catch (RuntimeException continued) { // because the priority is low and it needs select
        //    _log.info(continued.getMessage());
        //    return false;
        //}
    }

    protected void translateFKTable(Map<String, DfSynonymMeta> synonymMap) {
        final Collection<DfSynonymMeta> synonymList = synonymMap.values();
        final Map<String, List<DfSynonymMeta>> referredTableSynonymListMap = DfCollectionUtil.newLinkedHashMap();
        for (DfSynonymMeta synonym : synonymList) {
            final String tableName = synonym.getTableName();
            final List<DfSynonymMeta> foreignSynonymList = referredTableSynonymListMap.get(tableName);
            if (foreignSynonymList != null) {
                foreignSynonymList.add(synonym);
            } else {
                final List<DfSynonymMeta> foreignNewSynonymList = DfCollectionUtil.newArrayList();
                foreignNewSynonymList.add(synonym);
                referredTableSynonymListMap.put(tableName, foreignNewSynonymList);
            }
        }
        for (DfSynonymMeta synonym : synonymList) {
            final Map<String, DfForeignKeyMeta> fkMap = synonym.getForeignKeyMap();
            if (fkMap == null || fkMap.isEmpty()) {
                continue;
            }
            final Map<String, DfForeignKeyMeta> additionalFKMap = DfCollectionUtil.newLinkedHashMap();
            final Map<String, String> removedFKMap = DfCollectionUtil.newLinkedHashMap();
            for (Entry<String, DfForeignKeyMeta> entry : fkMap.entrySet()) {
                final String fkName = entry.getKey();
                final DfForeignKeyMeta fk = entry.getValue();

                // at first translate a local table name
                fk.setLocalSchema(synonym.getSynonymOwner());
                fk.setLocalTablePureName(synonym.getSynonymName());

                final String orignalForeignTableName = fk.getForeignTablePureName();
                final List<DfSynonymMeta> foreignSynonymList = referredTableSynonymListMap.get(orignalForeignTableName);
                if (foreignSynonymList == null || foreignSynonymList.isEmpty()) {
                    final UnifiedSchema tableOwner = synonym.getTableOwner();
                    if (_tableExtractor.isTableExcept(tableOwner, orignalForeignTableName)) {
                        removedFKMap.put(fkName, orignalForeignTableName);
                    } else if (!isForeignTableGenerated(tableOwner, orignalForeignTableName)) {
                        removedFKMap.put(fkName, orignalForeignTableName);
                    }
                    continue;
                }
                final String originalForeignKeyName = fk.getForeignKeyName();
                boolean firstDone = false;
                for (int i = 0; i < foreignSynonymList.size(); i++) {
                    final String newForeignKeyName = originalForeignKeyName + "_SYNONYM" + (i + 1);
                    final DfSynonymMeta newForeignSynonym = foreignSynonymList.get(i);
                    final String newForeignTableName = newForeignSynonym.getSynonymName();
                    final UnifiedSchema newForeignSchema = newForeignSynonym.getSynonymOwner();
                    if (!firstDone) {
                        // first (switching FK informations)
                        fk.setForeignKeyName(newForeignKeyName);
                        fk.setForeignTablePureName(newForeignTableName);
                        fk.setForeignSchema(newForeignSchema);
                        firstDone = true;
                        continue;
                    }

                    // second or more (creating new FK instance)
                    final DfForeignKeyMeta additionalFK = new DfForeignKeyMeta();
                    additionalFK.setForeignKeyName(newForeignKeyName);
                    additionalFK.setLocalSchema(fk.getLocalSchema());
                    additionalFK.setLocalTablePureName(fk.getLocalTablePureName());
                    additionalFK.setForeignSchema(newForeignSchema);
                    additionalFK.setForeignTablePureName(newForeignTableName);
                    additionalFK.putColumnNameAll(fk.getColumnNameMap());
                    additionalFKMap.put(additionalFK.getForeignKeyName(), additionalFK);
                }
            }
            fkMap.putAll(additionalFKMap);
            if (!removedFKMap.isEmpty()) {
                final StringBuilder sb = new StringBuilder();
                sb.append("...Excepting foreign keys from the synonym:").append(ln()).append("[Excepted Foreign Key]");
                final Set<String> removedFKKeySet = removedFKMap.keySet();
                for (String removedKey : removedFKKeySet) {
                    sb.append(ln()).append(" ").append(removedKey);
                    sb.append(" (").append(synonym.getSynonymName()).append(" to ");
                    sb.append(removedFKMap.get(removedKey)).append(")");
                    fkMap.remove(removedKey);
                }
                _log.info(sb.toString());
            }
        }
    }

    protected boolean isForeignTableGenerated(UnifiedSchema tableOwner, String foreignTableName) {
        if (_generatedTableMap == null || _generatedTableMap.isEmpty()) {
            // means no check of generation
            return true;
        }
        final DfTableMeta info = _generatedTableMap.get(foreignTableName);
        if (info == null) {
            return false;
        }
        final UnifiedSchema unifiedSchema = info.getUnifiedSchema();
        if (!unifiedSchema.getPureSchema().equals(tableOwner.getPureSchema())) {
            // treated as NOT FOUND
            // (tables in same schema are target here)
            return false;
        }
        if (info.isOutOfGenerateTarget()) {
            return false;
        }
        return true;
    }

    /**
     * Set up table and column comment. <br />
     * This does not support DB link synonym.
     * @param synonymMap The map of synonym. (NotNull)
     */
    protected void setupTableColumnComment(Map<String, DfSynonymMeta> synonymMap) {
        final Map<UnifiedSchema, Set<String>> ownerTabSetMap = createOwnerTableSetMap(synonymMap);
        final Map<UnifiedSchema, Map<String, UserTabComments>> ownerTabCommentMap = newLinkedHashMap();
        final Map<UnifiedSchema, Map<String, Map<String, UserColComments>>> ownerTabColCommentMap = newLinkedHashMap();
        for (UnifiedSchema owner : ownerTabSetMap.keySet()) {
            final Set<String> tableSet = ownerTabSetMap.get(owner);
            final DfDbCommentExtractorOracle extractor = createDbCommentExtractor(owner);
            final Map<String, UserTabComments> tabCommentMap = extractor.extractTableComment(tableSet);
            final Map<String, Map<String, UserColComments>> tabColCommentMap = extractor.extractColumnComment(tableSet);
            ownerTabCommentMap.put(owner, tabCommentMap);
            ownerTabColCommentMap.put(owner, tabColCommentMap);
        }
        for (DfSynonymMeta synonym : synonymMap.values()) {
            final UnifiedSchema owner = synonym.getTableOwner();
            final String tableName = synonym.getTableName();
            final Map<String, UserTabComments> tableCommentMap = ownerTabCommentMap.get(owner);
            if (tableCommentMap != null) {
                final UserTabComments userTabComments = tableCommentMap.get(tableName);
                if (userTabComments != null && userTabComments.hasComments()) {
                    synonym.setTableComment(userTabComments.getComments());
                }
            }
            final Map<String, Map<String, UserColComments>> tabColCommentMap = ownerTabColCommentMap.get(owner);
            if (tabColCommentMap != null) {
                final Map<String, UserColComments> colCommentMap = tabColCommentMap.get(tableName);
                if (colCommentMap != null && !colCommentMap.isEmpty()) {
                    synonym.setColumnCommentMap(colCommentMap);
                }
            }
        }
    }

    protected DfDbCommentExtractorOracle createDbCommentExtractor(UnifiedSchema schema) {
        final DfDbCommentExtractorOracle extractor = new DfDbCommentExtractorOracle();
        extractor.setDataSource(_dataSource);
        extractor.setUnifiedSchema(schema);
        return extractor;
    }

    protected Map<UnifiedSchema, Set<String>> createOwnerTableSetMap(Map<String, DfSynonymMeta> synonymMap) {
        final Map<UnifiedSchema, Set<String>> ownerTabSetMap = newLinkedHashMap();
        for (DfSynonymMeta synonym : synonymMap.values()) {
            final UnifiedSchema owner = synonym.getTableOwner();
            if (synonym.isDBLink()) { // Synonym of DB Link is out of target!
                continue;
            }
            Set<String> tableSet = ownerTabSetMap.get(owner);
            if (tableSet == null) {
                tableSet = new LinkedHashSet<String>();
                ownerTabSetMap.put(owner, tableSet);
            }
            tableSet.add(synonym.getTableName());
        }
        return ownerTabSetMap;
    }

    // -----------------------------------------------------
    //                             Supplementary Column Info
    //                             -------------------------
    protected List<DfColumnMeta> getSynonymColumns(Connection conn, UnifiedSchema synonymOwner, String synonymName)
            throws SQLException {
        final List<DfColumnMeta> columnList = new ArrayList<DfColumnMeta>();
        Statement st = null;
        ResultSet rs = null;
        try {
            st = conn.createStatement();
            final String synonymSqlName = synonymOwner.buildSchemaQualifiedName(synonymName);
            final String sql = "select * from " + synonymSqlName + " where 0=1";
            rs = st.executeQuery(sql);
            final ResultSetMetaData metaData = rs.getMetaData();
            int count = metaData.getColumnCount();
            for (int i = 0; i < count; i++) {
                int index = i + 1;
                String columnName = metaData.getColumnName(index);
                int columnType = metaData.getColumnType(index);
                String columnTypeName = metaData.getColumnTypeName(index);
                int precision = metaData.getPrecision(index);
                int scale = metaData.getScale(index);
                int nullableType = metaData.isNullable(index);
                DfColumnMeta column = new DfColumnMeta();
                column.setColumnName(columnName);
                column.setJdbcDefValue(columnType);
                column.setDbTypeName(columnTypeName);
                column.setColumnSize(precision);
                column.setDecimalDigits(scale);
                column.setRequired(nullableType == ResultSetMetaData.columnNoNulls);
                columnList.add(column);
            }
            return columnList;
        } finally {
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException ignored) {
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    // -----------------------------------------------------
    //                                   For DB Link Synonym
    //                                   -------------------
    protected DfPrimaryKeyMeta getDBLinkSynonymPKInfo(Connection conn, String tableName, String dbLinkName)
            throws SQLException {
        final DfPrimaryKeyMeta pkInfo = new DfPrimaryKeyMeta();
        StringBuilder sb = new StringBuilder();
        sb.append("select cols.OWNER, cols.CONSTRAINT_NAME, cols.TABLE_NAME, cols.COLUMN_NAME");
        sb.append("  from USER_CONS_COLUMNS@" + dbLinkName + " cols");
        sb.append("    left outer join USER_CONSTRAINTS@" + dbLinkName + " cons");
        sb.append("      on cols.CONSTRAINT_NAME = cons.CONSTRAINT_NAME");
        sb.append(" where cols.TABLE_NAME = '").append(tableName).append("'");
        sb.append("   and cons.CONSTRAINT_TYPE = 'P'");
        sb.append(" order by cols.POSITION");
        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = conn.createStatement();
            rs = statement.executeQuery(sb.toString());
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                String pkName = rs.getString("CONSTRAINT_NAME");
                pkInfo.addPrimaryKey(columnName, pkName);
            }
            return pkInfo;
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignored) {
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    protected Map<String, Map<Integer, String>> getDBLinkSynonymUQMap(Connection conn, String tableName,
            String dbLinkName) throws SQLException {
        final Map<String, Map<Integer, String>> uniqueMap = new LinkedHashMap<String, Map<Integer, String>>();
        final StringBuilder sb = new StringBuilder();
        sb.append("select cols.OWNER, cols.CONSTRAINT_NAME, cols.TABLE_NAME, cols.COLUMN_NAME, cols.POSITION");
        sb.append("  from USER_CONS_COLUMNS@" + dbLinkName + " cols");
        sb.append("    left outer join USER_CONSTRAINTS@" + dbLinkName + " cons");
        sb.append("      on cols.CONSTRAINT_NAME = cons.CONSTRAINT_NAME");
        sb.append(" where cols.TABLE_NAME = '").append(tableName).append("'");
        sb.append("   and cons.CONSTRAINT_TYPE = 'U'");
        sb.append(" order by cols.CONSTRAINT_NAME, cols.POSITION");
        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = conn.createStatement();
            rs = statement.executeQuery(sb.toString());
            while (rs.next()) {
                final String constraintName = rs.getString("CONSTRAINT_NAME");
                final String columnName = rs.getString("COLUMN_NAME");
                final Integer position = rs.getInt("POSITION");
                Map<Integer, String> uniqueElementMap = uniqueMap.get(uniqueMap);
                if (uniqueElementMap == null) {
                    uniqueElementMap = new LinkedHashMap<Integer, String>();
                    uniqueMap.put(constraintName, uniqueElementMap);
                }
                uniqueElementMap.put(position, columnName);
            }
            return uniqueMap;
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignored) {
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    protected Map<String, Map<String, SynonymNativeInfo>> extractDBLinkSynonymNativeMap() { // main schema's DB link only
        final DfDBLinkNativeExtractorOracle dbLinkExtractor = createDBLinkNativeExtractor();
        final Map<String, DBLinkNativeInfo> dbLinkInfoMap = dbLinkExtractor.selectDBLinkInfoMap();
        final DfSynonymNativeExtractorOracle nativeExtractor = createSynonymNativeExtractor();
        final Map<String, Map<String, SynonymNativeInfo>> map = DfCollectionUtil.newLinkedHashMap();
        for (String dbLinkName : dbLinkInfoMap.keySet()) {
            map.put(dbLinkName, nativeExtractor.selectDBLinkSynonymInfoMap(dbLinkName));
        }
        return map;
    }

    protected DfDBLinkNativeExtractorOracle createDBLinkNativeExtractor() {
        return new DfDBLinkNativeExtractorOracle(_dataSource, false);
    }

    protected DfSynonymNativeExtractorOracle createSynonymNativeExtractor() {
        return new DfSynonymNativeExtractorOracle(_dataSource, false);
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected <KEY, VALUE> LinkedHashMap<KEY, VALUE> newLinkedHashMap() {
        return DfCollectionUtil.newLinkedHashMap();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setDataSource(DataSource dataSource) {
        _dataSource = dataSource;
    }

    public void setTargetSchemaList(List<UnifiedSchema> unifiedSchemaList) {
        this._unifiedSchemaList = unifiedSchemaList;
    }

    public void setGeneratedTableMap(Map<String, DfTableMeta> generatedTableMap) {
        this._generatedTableMap = generatedTableMap;
    }
}
