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
package org.dbflute.logic.replaceschema.loaddata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author jflute
 */
public class DfDelimiterDataResultInfo {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /**
     * as INFO, map:{ tableName = set:{ not-found-column } } <br>
     * may be empty because already checked by loading control
     */
    protected final Map<String, Set<String>> _notFoundColumnMap = new LinkedHashMap<String, Set<String>>();

    /** as WARN, map:{ tsv-file-path = list:{ message-of-diff } } */
    protected final Map<String, List<String>> _columnCountDiffMap = new LinkedHashMap<String, List<String>>();

    // ===================================================================================
    //                                                                         Easy-to-Use
    //                                                                         ===========
    public void registerColumnCountDiff(String fileName, String message) {
        List<String> messageList = _columnCountDiffMap.get(fileName);
        if (messageList == null) {
            messageList = new ArrayList<String>();
            _columnCountDiffMap.put(fileName, messageList);
        }
        messageList.add(message);
    }

    public boolean containsColumnCountDiff(String delimiterFilePath) {
        return _columnCountDiffMap.containsKey(delimiterFilePath);
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public Map<String, Set<String>> getNotFoundColumnMap() {
        return _notFoundColumnMap; // cannot be read-only, put by outer processes
    }

    public Map<String, List<String>> getColumnCountDiffMap() {
        return Collections.unmodifiableMap(_columnCountDiffMap);
    }
}
