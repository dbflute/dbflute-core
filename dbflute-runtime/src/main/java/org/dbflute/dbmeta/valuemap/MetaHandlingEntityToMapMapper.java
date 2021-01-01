/*
 * Copyright 2014-2021 the original author or authors.
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
package org.dbflute.dbmeta.valuemap;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dbflute.Entity;
import org.dbflute.dbmeta.info.ColumnInfo;
import org.dbflute.util.DfCollectionUtil;

/**
 * The mapping object of entity to column value map by DB meta.
 * @author jflute
 * @since 1.1.0-sp1 (2015/01/19 Monday)
 */
public class MetaHandlingEntityToMapMapper {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Entity _entity;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public MetaHandlingEntityToMapMapper(Entity entity) {
        _entity = entity;
    }

    // ===================================================================================
    //                                                                             Mapping
    //                                                                             =======
    public Map<String, Object> mappingToColumnValueMap(List<ColumnInfo> columnInfoList) {
        final Map<String, Object> valueMap = newLinkedHashMapSized(columnInfoList.size());
        final Set<String> specifiedProperties = _entity.myspecifiedProperties();
        final boolean nonSpChecked = !specifiedProperties.isEmpty();
        for (ColumnInfo columnInfo : columnInfoList) {
            final String columnName = columnInfo.getColumnDbName();
            final Object value;
            if (nonSpChecked && !specifiedProperties.contains(columnInfo.getPropertyName())) { // non-specified column
                value = null; // to avoid non-specified check
            } else {
                value = columnInfo.read(_entity);
            }
            valueMap.put(columnName, value);
        }
        return valueMap;
    }

    protected <KEY, VALUE> LinkedHashMap<KEY, VALUE> newLinkedHashMapSized(int size) {
        return DfCollectionUtil.newLinkedHashMapSized(size);
    }
}
