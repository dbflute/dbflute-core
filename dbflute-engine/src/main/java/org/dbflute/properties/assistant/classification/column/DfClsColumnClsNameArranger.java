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
package org.dbflute.properties.assistant.classification.column;

import java.util.Map;

import org.dbflute.helper.StringKeyMap;
import org.dbflute.properties.DfClassificationProperties;
import org.dbflute.util.DfNameHintUtil;

/**
 * @author jflute
 * @since 1.2.5 split from DfClassificationProperties (2021/07/11 Sunday at roppongi japanese)
 */
public class DfClsColumnClsNameArranger {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Map<String, Map<String, String>> _deploymentMap;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfClsColumnClsNameArranger(Map<String, Map<String, String>> deploymentMap) {
        _deploymentMap = deploymentMap;
    }

    // ===================================================================================
    //                                                                          Initialize
    //                                                                          ==========
    public String findClassificationName(String tableName, String columnName,
            Map<String, StringKeyMap<String>> fkeyColumnClassificationMap) { // null allowed
        Map<String, String> plainMap = _deploymentMap.get(tableName);
        if (plainMap == null) {
            // should it be merged with all's map? no modification for now...
            // (so needs to call initializeClassificationDeployment())
            final String allMark = DfClassificationProperties.MARK_allColumnClassification;
            if (_deploymentMap.containsKey(allMark)) {
                // because the mark is unresolved when ReplaceSchema task
                plainMap = _deploymentMap.get(allMark);
            } else {
                return null;
            }
        }
        final String foundClassificationName;
        {
            // because columnClassificationMap is not flexible map
            StringKeyMap<String> columnClassificationMap = fkeyColumnClassificationMap.get(tableName);
            if (columnClassificationMap == null) {
                columnClassificationMap = StringKeyMap.createAsFlexible();
                columnClassificationMap.putAll(plainMap);
                fkeyColumnClassificationMap.put(tableName, columnClassificationMap);
            }
            foundClassificationName = columnClassificationMap.get(columnName);
        }
        String classificationName = null;
        if (foundClassificationName != null) {
            classificationName = foundClassificationName;
        } else {
            for (String columnNameHint : plainMap.keySet()) {
                if (isHitByTheHint(columnName, columnNameHint)) {
                    classificationName = plainMap.get(columnNameHint);
                }
            }
        }
        return classificationName;
    }

    protected boolean isHitByTheHint(final String name, final String hint) {
        return DfNameHintUtil.isHitByTheHint(name, hint);
    }
}
