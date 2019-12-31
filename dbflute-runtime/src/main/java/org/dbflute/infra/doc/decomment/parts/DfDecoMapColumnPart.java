/*
 * Copyright 2014-2020 the original author or authors.
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
 */
public class DfDecoMapColumnPart {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String columnName;
    protected final List<DfDecoMapMappingPart> mappingList;
    protected final List<DfDecoMapPropertyPart> propertyList;

    // ===================================================================================
    //                                                                           Converter
    //                                                                           =========
    public DfDecoMapColumnPart(String columnName, List<DfDecoMapMappingPart> mappingList, List<DfDecoMapPropertyPart> propertyList) {
        this.columnName = columnName;
        this.mappingList = mappingList;
        this.propertyList = propertyList;
    }

    @SuppressWarnings("unchecked")
    public DfDecoMapColumnPart(Map<String, Object> columnPartMap) {
        this.columnName = (String) columnPartMap.get("columnName");
        // not exists mapping list if format version is less equal 1.0
        final List<Map<String, Object>> mappingMapList =
                (List<Map<String, Object>>) columnPartMap.getOrDefault("mappingList", Collections.emptyList());
        this.mappingList = mappingMapList.stream().map(DfDecoMapMappingPart::new).collect(Collectors.toList());
        final List<Map<String, Object>> propertyMapList = (List<Map<String, Object>>) columnPartMap.get("propertyList");
        this.propertyList = propertyMapList.stream().map(DfDecoMapPropertyPart::new).collect(Collectors.toList());
    }

    // done cabos convertToMap()? by jflute (2017/11/11)
    public Map<String, Object> convertToMap() {
        final List<Map<String, Object>> mappingMapList =
                this.mappingList.stream().map(DfDecoMapMappingPart::convertToMap).collect(Collectors.toList());
        final List<Map<String, Object>> propertyMapList =
                this.propertyList.stream().map(DfDecoMapPropertyPart::convertToMap).collect(Collectors.toList());
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("columnName", columnName);
        map.put("mappingList", mappingMapList);
        map.put("propertyList", propertyMapList);
        return map;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getColumnName() {
        return this.columnName;
    }

    public List<DfDecoMapMappingPart> getMappingList() {
        return mappingList;
    }

    public List<DfDecoMapPropertyPart> getPropertyList() {
        return Collections.unmodifiableList(this.propertyList);
    }
}
