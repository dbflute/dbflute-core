/*
 * Copyright 2014-2023 the original author or authors.
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

import org.dbflute.util.DfNameHintUtil;

/**
 * @author jflute
 * @since 1.2.5 split from DfClassificationProperties (2021/07/11 Sunday at roppongi japanese)
 */
public class DfClsColumnDolloarAllClsNameArranger {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Map<String, String> _allColumnClassificationMap;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfClsColumnDolloarAllClsNameArranger(Map<String, String> allColumnClassificationMap) {
        _allColumnClassificationMap = allColumnClassificationMap;
    }

    // ===================================================================================
    //                                                                          Initialize
    //                                                                          ==========
    public String findAllClassificationName(String columnName, Map<String, String> fkeyAllColumnClassificationMap) { // null allowed
        final Map<String, String> plainMap = _allColumnClassificationMap;
        if (plainMap == null) {
            return null;
        }
        final String classificationName;
        {
            if (fkeyAllColumnClassificationMap.isEmpty()) {
                fkeyAllColumnClassificationMap.putAll(plainMap);
            }
            classificationName = fkeyAllColumnClassificationMap.get(columnName);
        }
        if (classificationName != null) {
            return classificationName;
        }
        for (String columnNameHint : plainMap.keySet()) {
            if (isHitByTheHint(columnName, columnNameHint)) {
                return plainMap.get(columnNameHint);
            }
        }
        return null;
    }

    protected boolean isHitByTheHint(final String name, final String hint) {
        return DfNameHintUtil.isHitByTheHint(name, hint);
    }
}
