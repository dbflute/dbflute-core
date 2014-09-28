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
package org.seasar.dbflute.s2dao.rowcreator;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.seasar.dbflute.jdbc.ValueType;
import org.seasar.dbflute.resource.ResourceContext;
import org.seasar.dbflute.s2dao.metadata.TnPropertyType;
import org.seasar.dbflute.s2dao.metadata.TnRelationPropertyType;
import org.seasar.dbflute.s2dao.rowcreator.impl.TnRelationKeyCompound;
import org.seasar.dbflute.s2dao.rowcreator.impl.TnRelationKeyEmpty;
import org.seasar.dbflute.s2dao.rowcreator.impl.TnRelationKeySimple;

/**
 * The cache of relation row. <br />
 * This is not thread safe so you should create per one select.
 * @author modified by jflute (originated in S2Dao)
 */
public class TnRelationRowCache {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final TnRelationKey EMPTY_KEY = new TnRelationKeyEmpty();

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The list of row map. map:{relationPath = map:{relationKey = row}} (NotNull: if canCache is true) */
    protected final Map<String, Map<TnRelationKey, Object>> _rowMap;

    /** Can the relation row cache? */
    protected final boolean _canCache;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * @param relSize The size of relation.
     * @param canCache Can the relation row cache?
     */
    public TnRelationRowCache(int relSize, boolean canCache) {
        _rowMap = canCache ? new HashMap<String, Map<TnRelationKey, Object>>(relSize) : null;
        _canCache = canCache;
    }

    // ===================================================================================
    //                                                                      Cache Handling
    //                                                                      ==============
    /**
     * Get relation row from cache by relation key.
     * @param relationNoSuffix The relation No suffix that indicates the location of the relation.
     * @param relKey The key of relation. (NotNull)
     * @return The relation row. (NullAllowed)
     */
    public Object getRelationRow(String relationNoSuffix, TnRelationKey relKey) {
        if (!_canCache) {
            return null;
        }
        final Map<TnRelationKey, Object> elementMap = _rowMap.get(relationNoSuffix);
        if (elementMap == null) {
            return null;
        }
        return elementMap.get(relKey);
    }

    /**
     * Add relation row to cache.
     * @param relationNoSuffix The relation No suffix that indicates the location of the relation.
     * @param relKey The key of relation. (NotNull)
     * @param relationRow The relation row. (NullAllowed)
     */
    public void addRelationRow(String relationNoSuffix, TnRelationKey relKey, Object relationRow) {
        if (!_canCache) {
            return;
        }
        Map<TnRelationKey, Object> elementMap = _rowMap.get(relationNoSuffix);
        if (elementMap == null) {
            elementMap = new HashMap<TnRelationKey, Object>();
            _rowMap.put(relationNoSuffix, elementMap);
        }
        elementMap.put(relKey, relationRow);
    }

    // ===================================================================================
    //                                                                        Key Creation
    //                                                                        ============
    /**
     * Create the key of relation.
     * @param rs The result set. (NotNull)
     * @param rpt The property type of relation. (NotNull)
     * @param selectColumnMap The name map of select column. {flexible-name = column-DB-name} (NotNull)
     * @param selectIndexMap The map of select index. map:{entityNo(e.g. loc00 or _0_3) = map:{selectColumnKeyName = selectIndex}} (NullAllowed: If it's null, it doesn't use select index.)
     * @param relationNoSuffix The suffix of relation No. (NotNull)
     * @return The key of relation. (NullAllowed: null means no data of the relation)
     * @throws SQLException
     */
    public TnRelationKey createRelationKey(ResultSet rs, TnRelationPropertyType rpt // basic resource
            , Map<String, String> selectColumnMap, Map<String, Map<String, Integer>> selectIndexMap // select resource
            , String relationNoSuffix) throws SQLException { // relation resource
        if (!_canCache) {
            return EMPTY_KEY;
        }
        final TnRelationKey relKey;
        if (rpt.hasSimpleUniqueKey()) {
            relKey = doCreateRelationKeySimple(rs, rpt, selectColumnMap, selectIndexMap, relationNoSuffix);
        } else if (rpt.hasCompoundUniqueKey()) {
            relKey = doCreateRelationKeyCompound(rs, rpt, selectColumnMap, selectIndexMap, relationNoSuffix);
        } else { // empty
            relKey = null; // treated as no data of the relation
        }
        return relKey;
    }

    protected TnRelationKey doCreateRelationKeySimple(ResultSet rs, TnRelationPropertyType rpt,
            Map<String, String> selectColumnMap, Map<String, Map<String, Integer>> selectIndexMap,
            String relationNoSuffix) throws SQLException {
        final TnPropertyType pt = rpt.getSimpleUniquePropertyType();
        final String columnKeyName = buildColumnKeyName(pt, relationNoSuffix);
        final Object keyValue = setupKeyElement(rs, rpt, selectColumnMap, selectIndexMap, columnKeyName, pt,
                relationNoSuffix);
        return keyValue != null ? new TnRelationKeySimple(columnKeyName, keyValue) : null;
    }

    protected TnRelationKey doCreateRelationKeyCompound(ResultSet rs, TnRelationPropertyType rpt,
            Map<String, String> selectColumnMap, Map<String, Map<String, Integer>> selectIndexMap,
            String relationNoSuffix) throws SQLException {
        final List<TnPropertyType> uniquePropertyTypeList = rpt.getUniquePropertyTypeList();
        Map<String, Object> relKeyValues = null;
        for (TnPropertyType pt : uniquePropertyTypeList) {
            final String columnKeyName = buildColumnKeyName(pt, relationNoSuffix);
            final Object keyValue = setupKeyElement(rs, rpt, selectColumnMap, selectIndexMap, columnKeyName, pt,
                    relationNoSuffix);
            if (keyValue == null) {
                if (relKeyValues != null) {
                    relKeyValues.clear();
                }
                break; // if either one is null, treated as no data
            }
            if (relKeyValues == null) { // lazy-load for performance
                relKeyValues = new HashMap<String, Object>(uniquePropertyTypeList.size());
            }
            relKeyValues.put(columnKeyName, keyValue);
        }
        return (relKeyValues != null && !relKeyValues.isEmpty()) ? new TnRelationKeyCompound(relKeyValues) : null;
    }

    protected String buildColumnKeyName(TnPropertyType pt, String relationNoSuffix) {
        return pt.getColumnDbName() + relationNoSuffix;
    }

    protected Object setupKeyElement(ResultSet rs, TnRelationPropertyType rpt, Map<String, String> selectColumnMap,
            Map<String, Map<String, Integer>> selectIndexMap, String columnKeyName, TnPropertyType pt,
            String relationNoSuffix) throws SQLException {
        if (isOutOfRelationSelectIndex(relationNoSuffix, columnKeyName, selectIndexMap)) {
            // basically unreachable, same reason with next if statement, check just in case
            return null;
        }
        if (!selectColumnMap.containsKey(columnKeyName)) {
            // basically unreachable
            // because the referred column (basically PK or FK) must exist
            // if the relation's select clause is specified
            return null;
        }
        final ValueType valueType = pt.getValueType();
        final Object value;
        if (selectIndexMap != null) {
            value = ResourceContext.getRelationValue(rs, relationNoSuffix, columnKeyName, valueType, selectIndexMap);
        } else {
            value = valueType.getValue(rs, columnKeyName);
        }
        // null-able when the referred column data is null
        // (treated as no relation data)
        return value;
    }

    protected boolean isOutOfRelationSelectIndex(String relationNoSuffix, String columnDbName,
            Map<String, Map<String, Integer>> selectIndexMap) throws SQLException {
        return ResourceContext.isOutOfRelationSelectIndex(relationNoSuffix, columnDbName, selectIndexMap);
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public Map<String, Map<TnRelationKey, Object>> getRowMap() {
        return _rowMap;
    }
}
