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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.engine.database.model.UnifiedSchema;
import org.seasar.dbflute.exception.DfIllegalPropertySettingException;
import org.seasar.dbflute.helper.StringKeyMap;
import org.seasar.dbflute.helper.jdbc.context.DfDataSourceContext;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfForeignKeyMeta;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfTableMeta;
import org.seasar.dbflute.logic.jdbc.metadata.supplement.DfUniqueKeyFkExtractor;
import org.seasar.dbflute.logic.jdbc.metadata.supplement.DfUniqueKeyFkExtractor.UserUniqueFkColumn;
import org.seasar.dbflute.logic.jdbc.metadata.supplement.factory.DfUniqueKeyFkExtractorFactory;
import org.seasar.dbflute.properties.facade.DfDatabaseTypeFacadeProp;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfForeignKeyExtractor extends DfAbstractMetaDataBasicExtractor {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log _log = LogFactory.getLog(DfForeignKeyExtractor.class);
    protected static final Map<UnifiedSchema, Map<String, List<DfForeignKeyMeta>>> _uniqueKeyFkMap = new ConcurrentHashMap<UnifiedSchema, Map<String, List<DfForeignKeyMeta>>>(); // singleton cache

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected Map<String, DfTableMeta> _generatedTableMap;

    // ===================================================================================
    //                                                                         Foreign Key
    //                                                                         ===========
    /**
     * Retrieves a map of foreign key columns for a given table. (the key is FK name)
     * @param metaData JDBC meta data. (NotNull)
     * @param tableInfo The meta information of table. (NotNull)
     * @return A list of foreign keys in <code>tableName</code>.
     * @throws SQLException
     */
    public Map<String, DfForeignKeyMeta> getForeignKeyMap(DatabaseMetaData metaData, DfTableMeta tableInfo)
            throws SQLException {
        final UnifiedSchema unifiedSchema = tableInfo.getUnifiedSchema();
        final String tableName = tableInfo.getTableName();
        return getForeignKeyMap(metaData, unifiedSchema, tableName);
    }

    /**
     * Retrieves a map of foreign key columns for a given table. (the key is FK name)
     * @param metaData JDBC meta data. (NotNull)
     * @param unifiedSchema The unified schema that can contain catalog name and no-name mark. (NullAllowed)
     * @param tableName The name of table. (NotNull, CaseInsensitiveByOption)
     * @return A list of foreign keys in <code>tableName</code>.
     * @throws SQLException
     */
    public Map<String, DfForeignKeyMeta> getForeignKeyMap(DatabaseMetaData metaData, UnifiedSchema unifiedSchema,
            String tableName) throws SQLException {
        final String translatedName = translateTableCaseName(tableName);
        Map<String, DfForeignKeyMeta> map = doGetForeignKeyMap(metaData, unifiedSchema, translatedName, false);
        if (isRetryCaseInsensitiveForeignKey()) {
            if (map.isEmpty() && !translatedName.equals(translatedName.toLowerCase())) { // retry by lower case
                map = doGetForeignKeyMap(metaData, unifiedSchema, translatedName.toLowerCase(), true);
            }
            if (map.isEmpty() && !translatedName.equals(translatedName.toUpperCase())) { // retry by upper case
                map = doGetForeignKeyMap(metaData, unifiedSchema, translatedName.toUpperCase(), true);
            }
        }
        return map;
    }

    protected Map<String, DfForeignKeyMeta> doGetForeignKeyMap(DatabaseMetaData metaData, UnifiedSchema unifiedSchema,
            String tableName, boolean retry) throws SQLException {
        final Map<String, DfForeignKeyMeta> fkMap = newTableConstraintMap();
        if (isForeignKeyExtractingUnsupported()) {
            return fkMap;
        }
        final Map<String, String> exceptedFKMap = newLinkedHashMap();
        ResultSet rs = null;
        try {
            rs = extractForeignKeyMetaData(metaData, unifiedSchema, tableName, retry);
            if (rs == null) {
                return DfCollectionUtil.newHashMap();
            }
            while (rs.next()) {
                // /- - - - - - - - - - - - - - - - - - - - - - - -
                // same policy as table process about JDBC handling
                // (see DfTableHandler.java)
                // - - - - - - - - - -/

                final String localTableName = rs.getString(7);
                if (checkMetaTableDiffIfNeeds(tableName, localTableName)) {
                    continue;
                }

                final String foreignCatalogName = rs.getString(1);
                final String foreignSchemaName = rs.getString(2);
                final String foreignTableName = rs.getString(3);
                final String foreignColumnName = rs.getString(4);
                final String localCatalogName = rs.getString(5);
                final String localSchemaName = rs.getString(6);
                final String localColumnName = rs.getString(8);
                final String fkName;
                {
                    final String fkPlainName = rs.getString(12);
                    if (Srl.is_NotNull_and_NotTrimmedEmpty(fkPlainName)) {
                        fkName = fkPlainName;
                    } else {
                        // basically no way but SQLite comes here
                        // make it up automatically just in case
                        // (use local column name and foreign table name)
                        fkName = "FK_" + tableName + "_" + localColumnName + "_" + foreignTableName;
                        _log.info("...Making FK name (because of no name): " + fkName);
                    }
                }

                // handling except tables if the set for check is set
                // (basically if the foreign table is non-generate target, it is excepted)
                if (!isForeignTableGenerated(foreignTableName)) {
                    exceptedFKMap.put(fkName, foreignTableName);
                    continue;
                }

                // check except columns
                assertFKColumnNotExcepted(unifiedSchema, tableName, localColumnName);
                final UnifiedSchema foreignSchema = createAsDynamicSchema(foreignCatalogName, foreignSchemaName);
                assertPKColumnNotExcepted(foreignSchema, foreignTableName, foreignColumnName);

                DfForeignKeyMeta meta = fkMap.get(fkName);
                if (meta == null) { // basically here
                    meta = new DfForeignKeyMeta();
                    fkMap.put(fkName, meta);
                } else { // same-name FK was found!
                    final String firstName = meta.getForeignTablePureName(); // pure name is enough for check
                    final String secondName = foreignTableName;
                    if (firstName.equalsIgnoreCase(secondName)) { // means compound FK
                        meta.putColumnName(localColumnName, foreignColumnName);
                        continue; // putting columns only
                    } else { // here: same-name FK and same different foreign table.
                        // Basically no way!
                        // But DB2 returns to-ALIAS foreign key as same-name FK.
                        // Same type as local's type is prior
                        // and if types are different, use first.
                        final String msgBase = "...Handling same-name FK ";
                        if (judgeSameNameForeignKey(tableName, firstName, secondName)) {
                            // use first (skip current)
                            _log.info(msgBase + "(use first one): " + fkName + " to " + firstName);
                            continue;
                        } else {
                            // use second (override)
                            _log.info(msgBase + "(use second one): " + fkName + " to " + secondName);
                        }
                    }
                }
                // first or override
                meta.setForeignKeyName(fkName);
                meta.setLocalSchema(createAsDynamicSchema(localCatalogName, localSchemaName));
                meta.setLocalTablePureName(localTableName);
                meta.setForeignSchema(foreignSchema);
                meta.setForeignTablePureName(foreignTableName);
                meta.putColumnName(localColumnName, foreignColumnName);
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
        reflectUniqueKeyFk(unifiedSchema, tableName, fkMap);
        handleExceptedForeignKey(exceptedFKMap, tableName);
        return immobilizeOrder(filterSameStructureForeignKey(fkMap));
    }

    protected ResultSet extractForeignKeyMetaData(DatabaseMetaData metaData, UnifiedSchema unifiedSchema,
            String tableName, boolean retry) throws SQLException {
        try {
            final String catalogName = unifiedSchema.getPureCatalog();
            final String schemaName = unifiedSchema.getPureSchema();
            return metaData.getImportedKeys(catalogName, schemaName, tableName);
        } catch (SQLException e) {
            if (retry) {
                // because the exception may be thrown when the table is not found
                return null;
            } else {
                throw e;
            }
        }
    }

    protected void assertFKColumnNotExcepted(UnifiedSchema unifiedSchema, String tableName, String columnName) {
        if (isColumnExcept(unifiedSchema, tableName, columnName)) {
            String msg = "FK columns are unsupported on 'columnExcept' property:";
            msg = msg + " unifiedSchema=" + unifiedSchema;
            msg = msg + " tableName=" + tableName;
            msg = msg + " columnName=" + columnName;
            throw new DfIllegalPropertySettingException(msg);
        }
    }

    protected void assertPKColumnNotExcepted(UnifiedSchema unifiedSchema, String tableName, String columnName) {
        if (isColumnExcept(unifiedSchema, tableName, columnName)) {
            String msg = "PK columns are unsupported on 'columnExcept' property:";
            msg = msg + " unifiedSchema=" + unifiedSchema;
            msg = msg + " tableName=" + tableName;
            msg = msg + " columnName=" + columnName;
            throw new DfIllegalPropertySettingException(msg);
        }
    }

    protected boolean judgeSameNameForeignKey(String localName, String firstName, String secondName) {
        final DfTableMeta localInfo = getTableMeta(localName);
        final DfTableMeta firstInfo = getTableMeta(firstName);
        final DfTableMeta secondInfo = getTableMeta(secondName);
        if (localInfo != null && firstInfo != null && secondInfo != null) {
            final String localType = localInfo.getTableType();
            if (localType.equals(firstInfo.getTableType())) {
                // use first
                return true;
            } else if (localType.equals(secondInfo.getTableType())) {
                // use second
                return false;
            }
        }
        return true; // use first
    }

    protected void handleExceptedForeignKey(Map<String, String> exceptedFKMap, String localTableName) {
        if (exceptedFKMap.isEmpty()) {
            return;
        }
        final StringBuilder sb = new StringBuilder();
        sb.append("...Excepting foreign keys (refers to non-generated table):");
        sb.append(ln()).append("[Excepted Foreign Key]");
        final Set<Entry<String, String>> entrySet = exceptedFKMap.entrySet();
        for (Entry<String, String> entry : entrySet) {
            final String fkName = entry.getKey();
            final String foreignTableName = entry.getValue();
            sb.append(ln()).append(" ").append(fkName);
            sb.append(" (").append(localTableName).append(" to ").append(foreignTableName).append(")");
        }
        _log.info(sb.toString());
    }

    protected Map<String, DfForeignKeyMeta> filterSameStructureForeignKey(Map<String, DfForeignKeyMeta> fkMap) {
        final Map<String, DfForeignKeyMeta> filteredFKMap = newLinkedHashMap();
        final Map<Map<String, Object>, Object> checkMap = newLinkedHashMap();
        final Object dummyObj = new Object();
        for (Entry<String, DfForeignKeyMeta> entry : fkMap.entrySet()) {
            final String foreinKeyName = entry.getKey();
            final DfForeignKeyMeta metaInfo = entry.getValue();
            final Map<String, Object> checkKey = newLinkedHashMap();
            checkKey.put(metaInfo.getForeignTableIdentityName(), dummyObj);
            checkKey.put("columnNameMap:" + metaInfo.getColumnNameMap(), dummyObj);
            if (checkMap.containsKey(checkKey)) { // basically no way
                String msg = "*The same-structural foreign key was found: skipped=" + foreinKeyName + ":" + checkKey;
                _log.warn(msg);
            } else {
                checkMap.put(checkKey, dummyObj);
                filteredFKMap.put(foreinKeyName, metaInfo);
            }
        }
        return filteredFKMap;
    }

    protected Map<String, DfForeignKeyMeta> immobilizeOrder(final Map<String, DfForeignKeyMeta> fkMap) {
        final Comparator<String> comparator = createImmobilizedComparator(fkMap);
        final TreeMap<String, DfForeignKeyMeta> sortedMap = new TreeMap<String, DfForeignKeyMeta>(comparator);
        sortedMap.putAll(fkMap);
        final Map<String, DfForeignKeyMeta> resultMap = newLinkedHashMap();
        resultMap.putAll(sortedMap);
        return resultMap; // renewal map just in case
    }

    protected Comparator<String> createImmobilizedComparator(final Map<String, DfForeignKeyMeta> fkMap) {
        return new Comparator<String>() {
            public int compare(String o1, String o2) {
                // sorted by "column names + FK name" (overridden default sort is by FK name)
                // because FK name might be auto-generated name by DBMS,
                // no change FK but generated classes might be changed after ReplaceSchema
                // (basically FK name should be named fixedly...)
                // so uses local column names as first key here
                final DfForeignKeyMeta meta1 = fkMap.get(o1);
                final DfForeignKeyMeta meta2 = fkMap.get(o2);
                final Map<String, String> columnNameMap1 = meta1.getColumnNameMap(); // the map is sorted
                final Map<String, String> columnNameMap2 = meta2.getColumnNameMap();
                final String exp1 = Srl.connectByDelimiter(columnNameMap1.keySet(), "/") + "," + o1;
                final String exp2 = Srl.connectByDelimiter(columnNameMap2.keySet(), "/") + "," + o2;
                return exp1.compareTo(exp2);
            }
        };
    }

    // ===================================================================================
    //                                                                        UniqueKey FK
    //                                                                        ============
    protected void reflectUniqueKeyFk(UnifiedSchema unifiedSchema, String tableName, Map<String, DfForeignKeyMeta> fkMap) {
        final List<DfForeignKeyMeta> uniqueKeyFkMetaList = findUniqueKeyFkMetaList(unifiedSchema, tableName);
        if (uniqueKeyFkMetaList == null) {
            return;
        }
        for (DfForeignKeyMeta uniqueKeyFkMeta : uniqueKeyFkMetaList) {
            final String foreignKeyName = uniqueKeyFkMeta.getForeignKeyName();
            if (fkMap.containsKey(foreignKeyName)) {
                _log.info("*The foreign key already exists: table=" + tableName + ", fk=" + foreignKeyName);
                continue;
            }
            fkMap.put(foreignKeyName, uniqueKeyFkMeta);
        }
    }

    protected List<DfForeignKeyMeta> findUniqueKeyFkMetaList(UnifiedSchema unifiedSchema, String tableName) {
        final Map<String, List<DfForeignKeyMeta>> tableMap = _uniqueKeyFkMap.get(unifiedSchema);
        if (tableMap != null) {
            return tableMap.get(tableName);
        }
        // not found info of the schema
        synchronized (_uniqueKeyFkMap) {
            final Map<String, List<DfForeignKeyMeta>> retryMap = _uniqueKeyFkMap.get(unifiedSchema); // retry
            if (retryMap != null) {
                return retryMap.get(tableName);
            }
            prepareUniqueKeyFkCache(unifiedSchema);
            return _uniqueKeyFkMap.get(unifiedSchema).get(tableName);
        }
    }

    protected void prepareUniqueKeyFkCache(UnifiedSchema unifiedSchema) {
        // preparing unique-key FK info of the schema
        final Map<String, List<DfForeignKeyMeta>> tableMap = StringKeyMap.createAsFlexibleConcurrent();
        final DfUniqueKeyFkExtractor extractor = createUniqueKeyFkExtractor(unifiedSchema);
        if (extractor == null) { // no need to extract in this DBMS
            _uniqueKeyFkMap.put(unifiedSchema, new ConcurrentHashMap<String, List<DfForeignKeyMeta>>());
            return;
        }
        _log.info("...Extracting unique-key FK: " + unifiedSchema);
        final Map<String, Map<String, List<UserUniqueFkColumn>>> uniqueKeyFkMap = extractor.extractUniqueKeyFkMap();
        for (Entry<String, Map<String, List<UserUniqueFkColumn>>> tableEntry : uniqueKeyFkMap.entrySet()) {
            final String tableKey = tableEntry.getKey();
            final List<DfForeignKeyMeta> metaList = new ArrayList<DfForeignKeyMeta>();
            final Map<String, List<UserUniqueFkColumn>> fkColumnListMap = tableEntry.getValue();
            for (Entry<String, List<UserUniqueFkColumn>> fkEntry : fkColumnListMap.entrySet()) {
                final List<UserUniqueFkColumn> columnList = fkEntry.getValue();
                DfForeignKeyMeta meta = null;
                for (UserUniqueFkColumn uniqueFkColumn : columnList) {
                    if (meta == null) {
                        meta = new DfForeignKeyMeta();
                        meta.setForeignKeyName(uniqueFkColumn.getForeignKeyName());
                        meta.setLocalSchema(unifiedSchema);
                        meta.setLocalTablePureName(uniqueFkColumn.getLocalTableName());
                        meta.setForeignSchema(unifiedSchema); // same schema only supported
                        final String foreignTableName = uniqueFkColumn.getForeignTableName();
                        meta.setForeignTablePureName(foreignTableName);
                        if (!isForeignTableGenerated(foreignTableName)) {
                            break;
                        }
                    }
                    meta.putColumnName(uniqueFkColumn.getLocalColumnName(), uniqueFkColumn.getForeignColumnName());
                }
                if (meta == null) { // basically no way
                    throw new IllegalStateException("The key should have any elements: " + tableKey);
                }
                metaList.add(meta);
            }
            tableMap.put(tableKey, metaList);
        }
        _uniqueKeyFkMap.put(unifiedSchema, tableMap);
        if (!tableMap.isEmpty()) {
            _log.info(" -> Unique-key FK: " + tableMap.keySet());
        } else {
            _log.info(" -> Not found unique-key FK");
        }
    }

    protected DfUniqueKeyFkExtractor createUniqueKeyFkExtractor(UnifiedSchema unifiedSchema) {
        final DataSource dataSource = DfDataSourceContext.getDataSource(); // uses thread data source
        if (dataSource == null) {
            String msg = "Not found thread data source for unique-key FK extracting: " + unifiedSchema;
            throw new IllegalStateException(msg);
        }
        final DfDatabaseTypeFacadeProp facadeProp = new DfDatabaseTypeFacadeProp(getBasicProperties());
        final DfUniqueKeyFkExtractorFactory factory = new DfUniqueKeyFkExtractorFactory(dataSource, unifiedSchema,
                facadeProp);
        return factory.createUniqueKeyFkExtractor();
    }

    // ===================================================================================
    //                                                                              Option
    //                                                                              ======
    public void exceptForeignTableNotGenerated(Map<String, DfTableMeta> generatedTableMap) {
        _generatedTableMap = generatedTableMap;
    }

    protected boolean isForeignTableGenerated(String foreignTableName) {
        if (_generatedTableMap == null || _generatedTableMap.isEmpty()) {
            // means no check of generation
            return true;
        }
        final DfTableMeta meta = _generatedTableMap.get(foreignTableName);
        if (meta == null) {
            return false;
        }
        if (meta.isOutOfGenerateTarget()) {
            return false;
        }
        return true;
    }

    protected DfTableMeta getTableMeta(String tableName) {
        if (_generatedTableMap == null) {
            return null;
        }
        return _generatedTableMap.get(tableName);
    }
}