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
package org.dbflute.infra.doc.hacomment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author hakiba
 */
public class DfHacoMapPickup {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String DEFAULT_FORMAT_VERSION = "1.0";
    private static final String HACO_MAP_KEY_HACOMAP = "hacoMap";
    private static final String HACO_MAP_KEY_DIFF_LIST = "diffList";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String formatVersion;
    protected LocalDateTime pickupDatetime;
    protected final Map<String, List<DfHacoMapDiffPart>> hacoMap = new LinkedHashMap<>();

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfHacoMapPickup() {
        this(DEFAULT_FORMAT_VERSION);
    }

    public DfHacoMapPickup(String formatVersion) {
        this.hacoMap.put(HACO_MAP_KEY_DIFF_LIST, new ArrayList<>());
        this.formatVersion = formatVersion;
    }

    // ===================================================================================
    //                                                                           Converter
    //                                                                           =========
    // map:{
    //     ; formatVersion = 1.0
    //     ; pickupDatetime = 2018-01-20T04:55:55.009
    //     ; hacoMap = map:{
    //         ; diffList = list:{
    //             map:{
    //                 ; diffCode = 20180110160922
    //                 ; diffDate = 2018/01/10 16:09:22
    //                 ; propertyList = list:{
    //                     ; map:{
    //                         ; hacomment = example comment! hey!
    //                         ; diffComment = null
    //                         ; authorList = list:{hakiba}
    //                         ; pieceCode = HAJDJDD
    //                         ; pieceOwner = hakiba
    //                         ; pieceDatetime = 2018-01-16T05:05:29.009
    //                         ; previousPieceList = list:{}
    //                     }
    //                 }
    //             } ... // more other diffs
    //         }
    //     }
    // }
    public Map<String, Object> convertToMap() {
        final Map<Object, List<Map<String, Object>>> hacoMap = new LinkedHashMap<>();
        final List<Map<String, Object>> convertedDiffList =
                this.getDiffList().stream().map(diffPart -> diffPart.convertPickupMap()).collect(Collectors.toList());
        hacoMap.put(HACO_MAP_KEY_DIFF_LIST, convertedDiffList);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("formatVersion", formatVersion);
        map.put("pickupDatetime", pickupDatetime);
        map.put(HACO_MAP_KEY_HACOMAP, hacoMap);
        return map;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getFormatVersion() {
        return formatVersion;
    }

    public void setPickupDatetime(LocalDateTime pickupDatetime) {
        this.pickupDatetime = pickupDatetime;
    }

    public LocalDateTime getPickupDatetime() {
        return pickupDatetime;
    }

    public void addAllDiffList(List<DfHacoMapDiffPart> diffPartList) {
        getHacoMapDiffPartList().addAll(diffPartList);
    }

    public List<DfHacoMapDiffPart> getDiffList() {
        return Collections.unmodifiableList(getHacoMapDiffPartList());
    }

    private List<DfHacoMapDiffPart> getHacoMapDiffPartList() {
        List<DfHacoMapDiffPart> hacoMapDiffPartList = hacoMap.get(HACO_MAP_KEY_DIFF_LIST);
        if (hacoMapDiffPartList == null) {
            throw new IllegalStateException("hacoMap history list is not exists");
        }
        return hacoMapDiffPartList;
    }
}
