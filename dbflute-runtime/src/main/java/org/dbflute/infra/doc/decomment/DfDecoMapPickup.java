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
package org.dbflute.infra.doc.decomment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.dbflute.helper.mapstring.MapListString;
import org.dbflute.infra.doc.decomment.parts.DfDecoMapTablePart;

/**
 * @author hakiba
 * @author cabos
 * @author jflute
 */
public class DfDecoMapPickup {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String DEFAULT_FORMAT_VERSION = "1.0";
    private static final String DECO_MAP_KEY = "tableList";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // done cabos add pickupDatetime by jflute (2017/11/11)
    protected final String formatVersion;
    protected LocalDateTime pickupDatetime;
    protected final Map<String, List<DfDecoMapTablePart>> decoMap;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfDecoMapPickup() {
        this(DEFAULT_FORMAT_VERSION);
    }

    public DfDecoMapPickup(String formatVersion) {
        this.decoMap = new LinkedHashMap<>();
        this.decoMap.put(DECO_MAP_KEY, new ArrayList<>()); // always exists decoMap table list
        this.formatVersion = formatVersion;
    }

    // ===================================================================================
    //                                                                           Converter
    //                                                                           =========
    // map:{
    //     ; formatVersion = 1.0
    //     ; pickupDatetime = 2017-11-09T09:09:09.009
    //     ; decoMap = map:{
    //         ; tableList = list:{
    //             ; map:{
    //                 ; tableName = MEMBER
    //                 ; propertyList = list:{
    //                     ; map:{
    //                         ; decomment = first decomment
    //                         ; databaseComment = ...
    //                         ; commentVersion = ...
    //                         ; authorList = list:{ deco }
    //                         ; pieceCode = DECO0000
    //                         ; pieceDatetime = 2017-11-05T00:38:13.645
    //                         ; pieceOwner = cabos
    //                         ; previousPieceList = list:{}
    //                     }
    //                     ; map:{ // propertyList size is more than 2 if decomment conflicts exists
    //                         ; ...
    //                     }
    //                 }
    //                 ; columnList = list:{
    //                     ; map:{
    //                         ; columnName = MEMBER_NAME
    //                         ; propertyList = list:{
    //                             ; map:{
    //                                 ; decomment = sea mystic land oneman
    //                                 ; databaseComment = sea mystic
    //                                 ; commentVersion = 1
    //                                 ; authorList = list:{ cabos, hakiba, deco, jflute }
    //                                 ; pieceCode = HAKIBA00
    //                                 ; pieceDatetime = 2017-11-05T00:38:13.645
    //                                 ; pieceOwner = cabos
    //                                 ; previousPieceList = list:{ JFLUTE00, CABOS000 }
    //                             }
    //                         }
    //                     }
    //                     ; ... // more other columns
    //                 }
    //             }
    //             ; map:{ // Of course, other table decomment info is exists that
    //                 ; tableName = MEMBER_LOGIN
    //                 ; ...
    //             }
    //         }
    //     }
    // }
    public Map<String, Object> convertToMap() {
        final List<Map<String, Object>> convertedTableList =
                this.getTableList().stream().map(DfDecoMapTablePart::convertPickupMap).collect(Collectors.toList());

        final Map<String, List<Map<String, Object>>> convertedDecoMap = new LinkedHashMap<>();
        convertedDecoMap.put("tableList", convertedTableList);

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("formatVersion", formatVersion);
        map.put("pickupDatetime", pickupDatetime);
        map.put("decoMap", convertedDecoMap);
        return map;
    }

    // done hakiba move to before Accessor by jflute (2017/08/17)
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

    public LocalDateTime getPickupDatetime() {
        return pickupDatetime;
    }

    public void setPickupDatetime(LocalDateTime pickupDatetime) {
        this.pickupDatetime = pickupDatetime;
    }

    public List<DfDecoMapTablePart> getTableList() {
        return Collections.unmodifiableList(getDecoMapTablePartList());
    }

    public void addTable(DfDecoMapTablePart table) {
        getDecoMapTablePartList().add(table);
    }

    public void addAllTables(Collection<DfDecoMapTablePart> tables) {
        getDecoMapTablePartList().addAll(tables);
    }

    private List<DfDecoMapTablePart> getDecoMapTablePartList() {
        List<DfDecoMapTablePart> decoMapTablePartList = decoMap.get(DECO_MAP_KEY);
        if (decoMapTablePartList == null) {
            throw new IllegalStateException("decoMap table list is not exists");
        }
        return decoMapTablePartList;
    }
}
