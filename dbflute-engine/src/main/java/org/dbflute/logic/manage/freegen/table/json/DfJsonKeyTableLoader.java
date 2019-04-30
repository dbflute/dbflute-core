/*
 * Copyright 2014-2019 the original author or authors.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dbflute.logic.manage.freegen.DfFreeGenMapProp;
import org.dbflute.logic.manage.freegen.DfFreeGenMetaData;
import org.dbflute.logic.manage.freegen.DfFreeGenResource;
import org.dbflute.logic.manage.freegen.DfFreeGenTableLoader;
import org.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfJsonKeyTableLoader implements DfFreeGenTableLoader {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final String JSON_DECODER_NAME = "net.arnx.jsonic.JSON";

    // ===================================================================================
    //                                                                          Load Table
    //                                                                          ==========
    // ; resourceMap = map:{
    //     ; resourceType = JSON_KEY
    //     ; resourceFile = ../../../sea.json
    // }
    // ; outputMap = map:{
    //     ; templateFile = MessageDef.vm
    //     ; outputDirectory = ../src/main/java
    //     ; package = org.dbflute...
    //     ; className = MessageDef
    // }
    // ; optionMap = map:{
    //     ; keyPath = categories -> map.key
    // }
    @Override
    public DfFreeGenMetaData loadTable(String requestName, DfFreeGenResource resource, DfFreeGenMapProp mapProp) {
        final Map<String, Object> tableMap = mapProp.getOptionMap();
        final String resourceFile = resource.getResourceFile();

        final Map<String, Object> rootMap = decodeJsonMap(requestName, resourceFile);
        final String keyPath = (String) tableMap.get("keyPath");
        final List<String> pathList = Srl.splitListTrimmed(keyPath, "->");
        final List<String> keyList = traceKeyList(requestName, resource, rootMap, keyPath, pathList);

        final String tableName = buildTableName(resourceFile);
        final List<Map<String, Object>> columnList = setupColumnList(requestName, resource, keyList);
        return DfFreeGenMetaData.asOnlyOne(tableMap, tableName, columnList);
    }

    protected String buildTableName(final String resourceFile) {
        return Srl.substringLastFront((Srl.substringLastRear(resourceFile, "/")), "."); // sea
    }

    // ===================================================================================
    //                                                                         Decode JSON
    //                                                                         ===========
    protected Map<String, Object> decodeJsonMap(String requestName, String resourceFile) {
        return new DfJsonFreeAgent().decodeJsonMap(requestName, resourceFile);
    }

    // ===================================================================================
    //                                                                       Trace KeyList
    //                                                                       =============
    protected List<String> traceKeyList(String requestName, DfFreeGenResource resource, Map<String, Object> rootMap, String keyPath,
            List<String> pathList) {
        return new DfJsonFreeAgent().traceKeyList(requestName, resource, rootMap, keyPath, pathList);
    }

    // ===================================================================================
    //                                                                   Set up ColumnList
    //                                                                   =================
    protected List<Map<String, Object>> setupColumnList(String requestName, DfFreeGenResource resource, List<String> keyList) {
        final List<Map<String, Object>> columnList = new ArrayList<Map<String, Object>>();
        for (String key : keyList) {
            final Map<String, Object> columnMap = new HashMap<String, Object>();
            columnMap.put("key", key);
            final String defName;
            if (key.contains(".")) { // e.g. foo.bar.qux
                defName = Srl.replace(key, ".", "_").toUpperCase();
            } else { // e.g. fooBarQux
                defName = Srl.decamelize(key);
            }
            columnMap.put("defName", defName);

            final String camelizedName = Srl.camelize(defName);
            columnMap.put("camelizedName", camelizedName);
            columnMap.put("capCamelName", Srl.initCap(camelizedName));
            columnMap.put("uncapCamelName", Srl.initUncap(camelizedName));

            columnList.add(columnMap);
        }
        return columnList;
    }
}
