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
import java.util.Collections;
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
    protected final String tableName;
    protected final Map<String, DfDecoMapPropertyPart> propertyMap = new LinkedHashMap<>();
    protected final List<DfDecoMapColumnPart> columnList = new ArrayList<>();

    // ===================================================================================
    //                                                           Constructor and Converter
    //                                                           =========================
    public DfDecoMapTablePart(String tableName) {
        this.tableName = tableName;
    }

    @SuppressWarnings("unchecked")
    public DfDecoMapTablePart(Map<String, Object> tablePartMap) {
        this.tableName = (String) tablePartMap.get("tableName");
        final List<Map<String, Object>> propertyMapList = (List<Map<String, Object>>) tablePartMap.get("propertyList");
        final List<Map<String, Object>> columnMapList = (List<Map<String, Object>>) tablePartMap.get("columnList");
        propertyMapList.stream().map(DfDecoMapPropertyPart::new).forEach(property -> {
            propertyMap.put(property.getPieceCode(), property);
        });
        final List<DfDecoMapColumnPart> columnList = columnMapList.stream().map(DfDecoMapColumnPart::new).collect(Collectors.toList());
        this.columnList.addAll(columnList);
    }

    public Map<String, Object> convertPickupMap() {
        final List<Map<String, Object>> columnMapList = columnList.stream().map(DfDecoMapColumnPart::convertToMap).collect(Collectors.toList());
        final List<Map<String, Object>> propertyMapList = propertyMap.values().stream()
                .map(DfDecoMapPropertyPart::convertToMap).collect(Collectors.toList());
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("tableName", tableName);
        map.put("propertyList", propertyMapList);
        map.put("columnList", columnMapList);
        return map;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getTableName() {
        return tableName;
    }

    public List<DfDecoMapPropertyPart> getPropertyList() {
        return Collections.unmodifiableList(new ArrayList<>(propertyMap.values()));
    }

    public void addProperty(DfDecoMapPropertyPart property) {
        this.propertyMap.put(property.getPieceCode(), property);
    }

    public void removeProperty(String pieceCode) {
        if (pieceCode == null) {
            throw new IllegalArgumentException("piece code is Null , piece code : " + pieceCode);
        }
        propertyMap.remove(pieceCode);
    }

    public List<DfDecoMapColumnPart> getColumnList() {
        return columnList;
    }

    public void addColumn(DfDecoMapColumnPart column) {
        this.columnList.add(column);
    }
}
