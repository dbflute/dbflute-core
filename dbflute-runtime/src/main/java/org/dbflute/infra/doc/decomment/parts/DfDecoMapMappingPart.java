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
package org.dbflute.infra.doc.decomment.parts;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.dbflute.helper.HandyDate;
import org.dbflute.infra.doc.decomment.DfDecoMapMapping;

/**
 * @author cabos
 */
public class DfDecoMapMappingPart {

    protected final String newTableName;
    protected final String newColumnName;
    protected final List<String> authorList;
    protected final String mappingCode;
    protected final String mappingOwner;
    protected final LocalDateTime mappingDatetime;
    protected final List<String> previousMappingList;

    public DfDecoMapMappingPart(DfDecoMapMapping mapping) {
        this.newTableName = mapping.getNewTableName();
        this.newColumnName = mapping.getNewColumnName();
        this.authorList = mapping.getAuthorList();
        this.mappingCode = mapping.getMappingCode();
        this.mappingOwner = mapping.getMappingOwner();
        this.mappingDatetime = mapping.getMappingDatetime();
        this.previousMappingList = mapping.getPreviousMappingList();
    }

    public DfDecoMapMappingPart(Map<String, Object> mappingMap) {
        this.newTableName = (String) mappingMap.get("newTableName");
        this.newColumnName = (String) mappingMap.get("newColumnName");
        this.authorList = ((List<?>) mappingMap.get("authorList")).stream().map(obj -> (String) obj).collect(Collectors.toList());
        this.mappingCode = (String) mappingMap.get("mappingCode");
        this.mappingOwner = (String) mappingMap.get("mappingOwner");
        this.mappingDatetime = new HandyDate((String) mappingMap.get("mappingDatetime")).getLocalDateTime();
        this.previousMappingList =
                ((List<?>) mappingMap.get("previousMappingList")).stream().map(obj -> (String) obj).collect(Collectors.toList());
    }

    public Map<String, Object> convertToMap() {
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("newTableName", this.newTableName);
        map.put("newColumnName", this.newColumnName);
        map.put("authorList", this.authorList);
        map.put("mappingCode", this.mappingCode);
        map.put("mappingOwner", this.mappingOwner);
        map.put("mappingDatetime", this.mappingDatetime);
        map.put("previousMappingList", this.previousMappingList);
        return map;
    }

    public String getNewTableName() {
        return newTableName;
    }

    public String getNewColumnName() {
        return newColumnName;
    }

    public List<String> getAuthorList() {
        return authorList;
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
        return previousMappingList;
    }
}
