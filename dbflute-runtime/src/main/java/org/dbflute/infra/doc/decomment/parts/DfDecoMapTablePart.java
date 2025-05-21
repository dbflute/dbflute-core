/*
 * Copyright 2014-2025 the original author or authors.
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
package org.dbflute.infra.doc.decomment.parts;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author hakiba
 * @author cabos
 * @author jflute
 */
public class DfDecoMapTablePart {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String tableName; // not null
    protected final List<DfDecoMapMappingPart> mappingList; // not null
    protected final List<DfDecoMapPropertyPart> propertyList; // not null
    protected final List<DfDecoMapColumnPart> columnList; // not null

    // ===================================================================================
    //                                                           Constructor and Converter
    //                                                           =========================
    public DfDecoMapTablePart(String tableName, List<DfDecoMapMappingPart> mappingList, List<DfDecoMapPropertyPart> propertyList,
            List<DfDecoMapColumnPart> columnList) {
        this.tableName = tableName;
        this.mappingList = mappingList;
        this.propertyList = propertyList;
        this.columnList = columnList;
    }

    @SuppressWarnings("unchecked")
    public DfDecoMapTablePart(Map<String, Object> tablePartMap) {
        this.tableName = (String) tablePartMap.get("tableName");
        // not exists mapping list if format version is less equal 1.0
        final List<Map<String, Object>> mappingMapList =
                (List<Map<String, Object>>) tablePartMap.getOrDefault("mappingList", Collections.emptyList());
        this.mappingList = mappingMapList.stream().map(DfDecoMapMappingPart::new).collect(Collectors.toList());
        final List<Map<String, Object>> propertyMapList = (List<Map<String, Object>>) tablePartMap.get("propertyList");
        this.propertyList = propertyMapList.stream().map(DfDecoMapPropertyPart::new).collect(Collectors.toList());
        final List<Map<String, Object>> columnMapList = (List<Map<String, Object>>) tablePartMap.get("columnList");
        this.columnList = columnMapList.stream().map(DfDecoMapColumnPart::new).collect(Collectors.toList());
    }

    public Map<String, Object> convertPickupMap() {
        final List<Map<String, Object>> mappingMapList =
                this.mappingList.stream().map(DfDecoMapMappingPart::convertToMap).collect(Collectors.toList());
        final List<Map<String, Object>> columnMapList =
                this.columnList.stream().map(DfDecoMapColumnPart::convertToMap).collect(Collectors.toList());
        final List<Map<String, Object>> propertyMapList =
                this.propertyList.stream().map(DfDecoMapPropertyPart::convertToMap).collect(Collectors.toList());
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("tableName", tableName);
        map.put("mappingList", mappingMapList);
        map.put("propertyList", propertyMapList);
        map.put("columnList", columnMapList);
        return map;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return "tablePart:{" + tableName //
                + ", mappings=" + toSizeExp(mappingList) //
                + ", properties=" + toSizeExp(propertyList) //
                + ", columns=" + toSizeExp(columnList) //
                + "}";
    }

    protected String toSizeExp(List<?> list) {
        return list != null ? String.valueOf(list.size()) : "null"; // just in case
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getTableName() {
        return tableName;
    }

    public List<DfDecoMapMappingPart> getMappingList() {
        return mappingList;
    }

    public List<DfDecoMapPropertyPart> getPropertyList() {
        return Collections.unmodifiableList(propertyList);
    }

    public List<DfDecoMapColumnPart> getColumnList() {
        return columnList;
    }
}
