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
package org.seasar.dbflute.s2dao.rshandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.seasar.dbflute.resource.ResourceContext;
import org.seasar.dbflute.s2dao.extension.TnRowCreatorExtension;
import org.seasar.dbflute.s2dao.jdbc.TnResultSetHandler;
import org.seasar.dbflute.s2dao.metadata.TnBeanMetaData;
import org.seasar.dbflute.s2dao.metadata.TnPropertyMapping;
import org.seasar.dbflute.s2dao.metadata.TnRelationPropertyType;
import org.seasar.dbflute.s2dao.rowcreator.TnRelationKey;
import org.seasar.dbflute.s2dao.rowcreator.TnRelationRowCache;
import org.seasar.dbflute.s2dao.rowcreator.TnRelationRowCreator;
import org.seasar.dbflute.s2dao.rowcreator.TnRelationSelector;
import org.seasar.dbflute.s2dao.rowcreator.TnRowCreator;

/**
 * @author modified by jflute (originated in S2Dao)
 */
public abstract class TnAbstractBeanResultSetHandler implements TnResultSetHandler {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    private final TnBeanMetaData _beanMetaData;
    protected final TnRowCreator _rowCreator;
    protected final TnRelationRowCreator _relationRowCreator;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * @param beanMetaData Bean meta data. (NotNull)
     * @param rowCreator Row creator. (NotNull)
     * @param relationRowCreator Relation row creator. (NotNul)
     */
    public TnAbstractBeanResultSetHandler(TnBeanMetaData beanMetaData, TnRowCreator rowCreator,
            TnRelationRowCreator relationRowCreator) {
        _beanMetaData = beanMetaData;
        _rowCreator = rowCreator;
        _relationRowCreator = relationRowCreator;
    }

    // ===================================================================================
    //                                                                      Property Cache
    //                                                                      ==============
    /**
     * Create property cache for base point row.
     * @param selectColumnMap The map of select column name. map:{flexibleName = columnAliasName} (NotNull)
     * @param selectIndexMap The map of select index. map:{entityNo(e.g. loc00 or _0_3) = map:{selectColumnKeyName = selectIndex}} (NullAllowed)
     * @return The map of row property cache. map:{columnName, PropertyMapping} (NotNull)
     * @throws SQLException
     */
    protected Map<String, TnPropertyMapping> createPropertyCache(Map<String, String> selectColumnMap,
            Map<String, Map<String, Integer>> selectIndexMap) throws SQLException {
        // - - - - - - - - -
        // Override for Bean
        // - - - - - - - - -
        return _rowCreator.createPropertyCache(selectColumnMap, selectIndexMap, _beanMetaData);
    }

    /**
     * Create relation property cache.
     * @param selectColumnMap The map of select column name. map:{flexibleName = columnAliasName} (NotNull)
     * @param selectIndexMap The map of select index. map:{entityNo(e.g. loc00 or _0_3) = map:{selectColumnKeyName = selectIndex}} (NullAllowed)
     * @param relSelector The selector of relation, which can determines e.g. is it not-selected relation?. (NotNull)
     * @return The map of relation property cache. map:{relationNoSuffix = map:{columnName = PropertyMapping}} (NotNull)
     * @throws SQLException
     */
    protected Map<String, Map<String, TnPropertyMapping>> createRelationPropertyCache(
            Map<String, String> selectColumnMap, Map<String, Map<String, Integer>> selectIndexMap,
            TnRelationSelector relSelector) throws SQLException {
        return _relationRowCreator.createPropertyCache(selectColumnMap, selectIndexMap, relSelector, _beanMetaData);
    }

    // ===================================================================================
    //                                                                          Create Row
    //                                                                          ==========
    /**
     * Create base point row.
     * @param rs Result set. (NotNull)
     * @param selectIndexMap The map of select index. map:{entityNo(e.g. loc00 or _0_3) = map:{selectColumnKeyName = selectIndex}} (NullAllowed)
     * @param propertyCache The map of property cache. map:{columnName, PropertyMapping} (NotNull)
     * @return The created row. (NotNull)
     * @throws SQLException
     */
    protected Object createRow(ResultSet rs, Map<String, Map<String, Integer>> selectIndexMap,
            Map<String, TnPropertyMapping> propertyCache) throws SQLException {
        // - - - - - - - - -
        // Override for Bean
        // - - - - - - - - -
        final Class<?> beanClass = _beanMetaData.getBeanClass();
        return _rowCreator.createRow(rs, selectIndexMap, propertyCache, beanClass);
    }

    /**
     * Create relation row.
     * @param rs Result set. (NotNull)
     * @param rpt The type of relation property. (NotNull)
     * @param selectColumnMap The name map of select column. map:{flexibleName = columnDbName} (NotNull)
     * @param selectIndexMap The map of select index. map:{entityNo(e.g. loc00 or _0_3) = map:{selectColumnKeyName = selectIndex}} (NullAllowed)
     * @param relKey The relation key, which has key values, of the relation. (NotNull)
     * @param relPropCache The map of relation property cache. map:{relationNoSuffix = map:{columnName = PropertyMapping}} (NotNull)
     * @param relRowCache The cache of relation row. (NotNull)
     * @param relSelector The selector of relation, which can determines e.g. is it not-selected relation?. (NotNull)
     * @return Created relation row. (NullAllowed)
     * @throws SQLException
     */
    protected Object createRelationRow(ResultSet rs, TnRelationPropertyType rpt, Map<String, String> selectColumnMap,
            Map<String, Map<String, Integer>> selectIndexMap, TnRelationKey relKey,
            Map<String, Map<String, TnPropertyMapping>> relPropCache, TnRelationRowCache relRowCache,
            TnRelationSelector relSelector) throws SQLException {
        return _relationRowCreator.createRelationRow(rs, rpt // basic resource
                , selectColumnMap, selectIndexMap // select resource
                , relKey, relPropCache, relRowCache, relSelector); // relation resource
    }

    /**
     * Adjust create row.
     * @param row The row of result list. (NotNull)
     * @param bmd The bean meta data of the row. (NotNull)
     */
    protected void adjustCreatedRow(final Object row, TnBeanMetaData bmd) {
        TnRowCreatorExtension.adjustCreatedRow(row, bmd);
    }

    // ===================================================================================
    //                                                                       Select Column
    //                                                                       =============
    protected Map<String, String> createSelectColumnMap(ResultSet rs) throws SQLException {
        return ResourceContext.createSelectColumnMap(rs);
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public TnBeanMetaData getBeanMetaData() {
        return _beanMetaData;
    }
}
