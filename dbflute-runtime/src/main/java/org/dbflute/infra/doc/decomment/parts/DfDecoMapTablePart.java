/*
 * Copyright 2014-2017 the original author or authors.
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author hakiba
 * @author cabos
 */
public class DfDecoMapTablePart {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String tableName;
    protected List<DfDecoMapPropertyPart> propertyList = new ArrayList<>();
    protected List<DfDecoMapColumnPart> columnList = new ArrayList<>();

    // ===================================================================================
    //                                                           Constructor and Converter
    //                                                           =========================
    public DfDecoMapTablePart() {
    }

    @SuppressWarnings("unchecked")
    public DfDecoMapTablePart(Map<String, Object> tablePartMap) {
        this.tableName = (String) tablePartMap.get("tableName");
        List<Map<String, Object>> propertyMapList = (List<Map<String, Object>>) tablePartMap.get("propertyList");
        List<Map<String, Object>> columnMapList = (List<Map<String, Object>>) tablePartMap.get("columnList");
        List<DfDecoMapPropertyPart> propertyList = propertyMapList.stream().map(DfDecoMapPropertyPart::new).collect(Collectors.toList());
        this.propertyList.addAll(propertyList);
        List<DfDecoMapColumnPart> columnList = columnMapList.stream().map(map -> new DfDecoMapColumnPart(map)).collect(Collectors.toList());
        this.columnList.addAll(columnList);
    }

    public Map<String, Object> convertPickupMap() {
        Map<String, List<Map<String, Object>>> columnMap = columnList.stream()
            .collect(Collectors.toMap(column -> column.getColumnName(), column -> column.convertToMap(), (c1, c2) -> c1));

        Map<String, Object> map = new LinkedHashMap<>();
        map.put(tableName, columnMap);
        return map;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<DfDecoMapPropertyPart> getPropertyList() {
        return propertyList;
    }

    public void addProperty(DfDecoMapPropertyPart property) {
        this.propertyList.add(property);
    }

    public List<DfDecoMapColumnPart> getColumnList() {
        return columnList;
    }

    public void addColumn(DfDecoMapColumnPart column) {
        this.columnList.add(column);
    }
}
