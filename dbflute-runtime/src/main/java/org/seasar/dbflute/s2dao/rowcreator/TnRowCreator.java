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

/**
 * @author modified by jflute (originated in S2Dao)
 */
public interface TnRowCreator {

    /**
     * Create row instance of base point table.
     * @param rs Result set. (NotNull)
     * @param selectIndexMap The map of select index. map:{entityNo(e.g. loc00 or _0_3) = map:{selectColumnKeyName = selectIndex}} (NullAllowed)
     * @param columnPropertyTypeMap The map of row property cache. The key is String(columnName) and the value is a PropertyMapping. (NotNull)
     * @param beanClass Bean class. (NotNull)
     * @return The created row. (NotNull)
     * @throws SQLException
     */
    Object createRow(ResultSet rs, Map<String, Map<String, Integer>> selectIndexMap,
            Map<String, TnPropertyMapping> columnPropertyTypeMap, Class<?> beanClass) throws SQLException;

    /**
     * Create property cache as map. <br />
     * The map key is column DB-name or alias name when derived-referrer.
     * @param selectColumnMap The map of select column name. {flexible-name = columnAliasName} (NotNull)
     * @param selectIndexMap The map of select index. map:{entityNo(e.g. loc00 or _0_3) = map:{selectColumnKeyName = selectIndex}} (NullAllowed)
     * @param beanMetaData Bean meta data. (NotNull)
     * @return The map of row property cache. The key is String(columnName) and the value is a PropertyMapping. (NotNull)
     * @throws SQLException
     */
    Map<String, TnPropertyMapping> createPropertyCache(Map<String, String> selectColumnMap,
            Map<String, Map<String, Integer>> selectIndexMap, TnBeanMetaData beanMetaData) throws SQLException;
}
