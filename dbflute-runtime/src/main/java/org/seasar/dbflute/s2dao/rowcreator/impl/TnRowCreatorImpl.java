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
package org.seasar.dbflute.s2dao.rowcreator.impl;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.seasar.dbflute.helper.StringKeyMap;
import org.seasar.dbflute.resource.ResourceContext;
import org.seasar.dbflute.s2dao.metadata.TnBeanMetaData;
import org.seasar.dbflute.s2dao.metadata.TnPropertyMapping;
import org.seasar.dbflute.s2dao.metadata.TnPropertyType;
import org.seasar.dbflute.s2dao.rowcreator.TnRowCreator;
import org.seasar.dbflute.util.DfReflectionUtil;

/**
 * @author modified by jflute (originated in S2Dao)
 */
public abstract class TnRowCreatorImpl implements TnRowCreator {

    // ===================================================================================
    //                                                                        Row Creation
    //                                                                        ============
    protected Object newBean(Class<?> beanClass) {
        return DfReflectionUtil.newInstance(beanClass);
    }

    // ===================================================================================
    //                                                             Property Cache Creation
    //                                                             =======================
    // -----------------------------------------------------
    //                                                  Bean
    //                                                  ----
    /**
     * {@inheritDoc}
     */
    public Map<String, TnPropertyMapping> createPropertyCache(Map<String, String> selectColumnMap,
            Map<String, Map<String, Integer>> selectIndexMap, TnBeanMetaData beanMetaData) throws SQLException {
        // - - - - - - - 
        // Entry Point!
        // - - - - - - -
        final Map<String, TnPropertyMapping> proprertyCache = newPropertyCache();
        setupPropertyCache(proprertyCache, selectColumnMap, selectIndexMap, beanMetaData);
        return proprertyCache;
    }

    protected void setupPropertyCache(Map<String, TnPropertyMapping> proprertyCache,
            Map<String, String> selectColumnMap, Map<String, Map<String, Integer>> selectIndexMap,
            TnBeanMetaData beanMetaData) throws SQLException {
        final List<TnPropertyType> ptList = beanMetaData.getPropertyTypeList();
        for (TnPropertyType pt : ptList) { // already been filtered as data properties only
            setupPropertyCacheElement(proprertyCache, selectColumnMap, selectIndexMap, pt);
        }
    }

    protected void setupPropertyCacheElement(Map<String, TnPropertyMapping> proprertyCache,
            Map<String, String> selectColumnMap, Map<String, Map<String, Integer>> selectIndexMap, TnPropertyType pt)
            throws SQLException {
        final String columnDbName = pt.getColumnDbName();
        if (isOutOfLocalSelectIndex(columnDbName, selectIndexMap)) {
            return;
        }
        if (pt.isPersistent()) {
            if (selectColumnMap.containsKey(columnDbName)) { // basically true if persistent
                // the column DB name is same as selected name
                proprertyCache.put(columnDbName, pt);
            }
        } else {
            if (selectColumnMap.containsKey(columnDbName)) {
                // for example, the column DB name is property name when derived-referrer
                // so you should switch the name to selected name
                proprertyCache.put(selectColumnMap.get(columnDbName), pt);
            }
        }
        // only a column that is not persistent and non-selected property
    }

    protected boolean isOutOfLocalSelectIndex(String columnDbName, Map<String, Map<String, Integer>> selectIndexMap)
            throws SQLException {
        return ResourceContext.isOutOfLocalSelectIndex(columnDbName, selectIndexMap);
    }

    // -----------------------------------------------------
    //                                                Common
    //                                                ------
    protected Map<String, TnPropertyMapping> newPropertyCache() {
        return StringKeyMap.createAsCaseInsensitive();
    }
}
