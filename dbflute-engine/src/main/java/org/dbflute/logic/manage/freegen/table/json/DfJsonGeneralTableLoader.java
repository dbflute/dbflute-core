/*
 * Copyright 2014-2022 the original author or authors.
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
package org.dbflute.logic.manage.freegen.table.json;

import java.util.Map;

import org.dbflute.logic.manage.freegen.DfFreeGenMapProp;
import org.dbflute.logic.manage.freegen.DfFreeGenMetaData;
import org.dbflute.logic.manage.freegen.DfFreeGenResource;
import org.dbflute.logic.manage.freegen.DfFreeGenTableLoader;

/**
 * @author jflute
 * @since 1.1.5 (2017/10/06 Friday)
 */
public class DfJsonGeneralTableLoader implements DfFreeGenTableLoader {

    // ===================================================================================
    //                                                                          Load Table
    //                                                                          ==========
    // ; resourceMap = map:{
    //     ; resourceType = JSON_GENERAL
    //     ; resourceFile = ../../.../swagger.json
    // }
    // ; outputMap = map:{
    //     ; templateFile = unused
    //     ; outputDirectory = ../src/main/java
    //     ; package = org.dbflute...
    //     ; className = unused
    // }
    // ; optionMap = map:{
    //     ; ...
    // }
    @Override
    public DfFreeGenMetaData loadTable(String requestName, DfFreeGenResource resource, DfFreeGenMapProp mapProp) {
        final Map<String, Object> optionMap = mapProp.getOptionMap();
        final String resourceFile = resource.getResourceFile();
        final Map<String, Object> jsonMap = decodeJsonMap(requestName, resourceFile);
        optionMap.put("jsonMap", jsonMap); // contains all meta data
        return DfFreeGenMetaData.asFlexible(optionMap); // uses only option map
    }

    protected Map<String, Object> decodeJsonMap(String requestName, final String resourceFile) {
        return new DfJsonFreeAgent().decodeJsonMap(requestName, resourceFile);
    }
}
