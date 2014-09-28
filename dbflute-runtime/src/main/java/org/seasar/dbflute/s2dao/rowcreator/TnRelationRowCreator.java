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
import java.util.Map;

import org.seasar.dbflute.s2dao.metadata.TnBeanMetaData;
import org.seasar.dbflute.s2dao.metadata.TnPropertyMapping;
import org.seasar.dbflute.s2dao.metadata.TnRelationPropertyType;

/**
 * @author modified by jflute (originated in S2Dao)
 */
public interface TnRelationRowCreator {

    /**
     * Create relation row from first level relation.
     * @param rs Result set. (NotNull)
     * @param rpt The type of relation property. (NotNull)
     * @param selectColumnMap The name map of select column. map:{flexibleName = columnDbName} (NotNull)
     * @param selectIndexMap The map of select index. map:{entityNo(e.g. loc00 or _0_3) = map:{selectColumnKeyName = selectIndex}} (NullAllowed: null means select index is disabled)
     * @param relKey The relation key, which has key values, of the relation. (NotNull)
     * @param relPropCache The map of relation property cache. map:{relationNoSuffix = map:{columnName = PropertyMapping}} (NotNull)
     * @param relRowCache The cache of relation row. (NotNull)
     * @param relSelector The selector of relation, which can determines e.g. is it not-selected relation?. (NotNull)
     * @return The created row of the relation. (NullAllowed: if null, no data about the relation)
     * @throws SQLException
     */
    Object createRelationRow(ResultSet rs, TnRelationPropertyType rpt, Map<String, String> selectColumnMap,
            Map<String, Map<String, Integer>> selectIndexMap, TnRelationKey relKey,
            Map<String, Map<String, TnPropertyMapping>> relPropCache, TnRelationRowCache relRowCache,
            TnRelationSelector relSelector) throws SQLException;

    /**
     * Create relation property cache.
     * @param selectColumnMap The name map of select column. map:{flexibleName = columnDbName} (NotNull)
     * @param selectIndexMap The map of select index. map:{entityNo(e.g. loc00 or _0_3) = map:{selectColumnKeyName = selectIndex}} (NullAllowed: null means select index is disabled)
     * @param relSelector The selector of relation, which can determines e.g. is it not-selected relation?. (NotNull)
     * @param baseBmd Bean meta data of base object. (NotNull)
     * @return The map of relation property cache. map:{relationNoSuffix = map:{columnName = PropertyMapping}} (NotNull)
     * @throws SQLException
     */
    Map<String, Map<String, TnPropertyMapping>> createPropertyCache(Map<String, String> selectColumnMap,
            Map<String, Map<String, Integer>> selectIndexMap, TnRelationSelector relSelector, TnBeanMetaData baseBmd)
            throws SQLException;

    /**
     * Filter the relation row as optional object if it needs.
     * @param row The base point row, which is previous relation row. (NotNull)
     * @param rpt The property type for the relation. (NotNull)
     * @param relationRow The row instance of relation entity. (NullAllowed)
     * @return The filtered instance of relation entity. (NullAllowed)
     */
    Object filterOptionalRelationRowIfNeeds(Object row, TnRelationPropertyType rpt, Object relationRow);
}
