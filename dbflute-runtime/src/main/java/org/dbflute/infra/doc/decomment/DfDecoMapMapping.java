/*
 * Copyright 2014-2018 the original author or authors.
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
package org.dbflute.infra.doc.decomment;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.dbflute.helper.mapstring.MapListString;
import org.dbflute.infra.doc.decomment.parts.DfDecoMapMappingPart;

/**
 * @author cabos
 */
public class DfDecoMapMapping {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String DEFAULT_FORMAT_VERSION = "1.1";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String formatVersion;
    protected final String oldTableName;
    protected final String oldColumnName;
    protected final String newTableName;
    protected final String newColumnName;
    protected final DfDecoMapPieceTargetType targetType;
    protected final List<String> authorList;
    protected final String mappingCode;
    protected final String mappingOwner;
    protected final LocalDateTime mappingDatetime;
    protected final List<String> previousMappingList;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfDecoMapMapping(String formatVersion, String oldTableName, String oldColumnName, String newTableName, String newColumnName,
            DfDecoMapPieceTargetType targetType, List<String> authorList, String mappingCode, String mappingOwner,
            LocalDateTime mappingDatetime, List<String> previousMappingList) {
        this.formatVersion = formatVersion;
        this.oldTableName = oldTableName;
        this.oldColumnName = oldColumnName;
        this.newTableName = newTableName;
        this.newColumnName = newColumnName;
        this.targetType = targetType;
        this.authorList = authorList.stream().distinct().collect(Collectors.toList());
        if (!authorList.contains(mappingOwner)) {
            this.authorList.add(mappingOwner);
        }
        this.mappingCode = mappingCode;
        this.mappingOwner = mappingOwner;
        this.mappingDatetime = mappingDatetime;
        this.previousMappingList = previousMappingList.stream().distinct().collect(Collectors.toList());
    }

    public DfDecoMapMapping(String oldTableName, String oldColumnName, DfDecoMapPieceTargetType type, DfDecoMapMappingPart mappingPart) {
        this.formatVersion = DEFAULT_FORMAT_VERSION;
        this.oldTableName = oldTableName;
        this.oldColumnName = oldColumnName;
        this.newTableName = mappingPart.getNewTableName();
        this.newColumnName = mappingPart.getNewColumnName();
        this.targetType = type;
        this.mappingOwner = mappingPart.getMappingOwner();
        this.authorList = mappingPart.getAuthorList().stream().distinct().collect(Collectors.toList());
        if (!authorList.contains(mappingOwner)) {
            this.authorList.add(mappingOwner);
        }
        this.mappingCode = mappingPart.getMappingCode();
        this.mappingDatetime = mappingPart.getMappingDatetime();
        this.previousMappingList = mappingPart.getPreviousMappingList().stream().distinct().collect(Collectors.toList());
    }

    // ===================================================================================
    //                                                                           Converter
    //                                                                           =========
    public Map<String, Object> convertToMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("formatVersion", this.formatVersion);
        map.put("oldTableName", this.oldTableName);
        map.put("oldColumnName", this.oldColumnName);
        map.put("newTableName", this.newTableName);
        map.put("newColumnName", this.newColumnName);
        map.put("targetType", this.targetType.code());
        map.put("authorList", this.authorList);
        map.put("mappingCode", this.mappingCode);
        map.put("mappingOwner", this.mappingOwner);
        map.put("mappingDatetime", this.mappingDatetime);
        map.put("previousMappingList", this.previousMappingList);
        return map;
    }

    // ===================================================================================
    //                                                                            Override
    //                                                                            ========
    @Override
    public String toString() {
        return new MapListString().buildMapString(this.convertToMap());
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getFormatVersion() {
        return formatVersion;
    }

    public String getOldTableName() {
        return oldTableName;
    }

    public String getOldColumnName() {
        return oldColumnName;
    }

    public String getNewTableName() {
        return newTableName;
    }

    public String getNewColumnName() {
        return newColumnName;
    }

    public DfDecoMapPieceTargetType getTargetType() {
        return targetType;
    }

    public boolean isTargetTypeTable() {
        return targetType == DfDecoMapPieceTargetType.Table;
    }

    public boolean isTargetTypeColumn() {
        return targetType == DfDecoMapPieceTargetType.Column;
    }

    public List<String> getAuthorList() {
        return Collections.unmodifiableList(authorList);
    }

    public String getMappingCode() {
        return mappingCode;
    }

    public String getMappingOwner() {
        return mappingOwner;
    }

    public LocalDateTime getMappingDatetime() {
        return mappingDatetime;
    }

    public List<String> getPreviousMappingList() {
        return Collections.unmodifiableList(previousMappingList);
    }
}
