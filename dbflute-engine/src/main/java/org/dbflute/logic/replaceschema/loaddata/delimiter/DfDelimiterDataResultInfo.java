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
package org.dbflute.logic.replaceschema.loaddata.delimiter;

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
    /** map:{ data-directory = map:{ file-name = loaded-meta } } */
    protected final Map<String, Map<String, DfDelimiterDataLoadedMeta>> _loadedMetaMap =
            new LinkedHashMap<String, Map<String, DfDelimiterDataLoadedMeta>>();

    public static class DfDelimiterDataLoadedMeta {

        protected final String _fileName; // contains path
        protected final int _successRowCount;

        public DfDelimiterDataLoadedMeta(String fileName, int successRowCount) {
            _fileName = fileName;
            _successRowCount = successRowCount;
        }

        public String getFileName() {
            return _fileName;
        }

        public Integer getSuccessRowCount() {
            return _successRowCount;
        }
    }

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
    public void registerLoadedMeta(String dataDirectory, String fileName, int successRowCount) {
        Map<String, DfDelimiterDataLoadedMeta> firstMap = _loadedMetaMap.get(dataDirectory);
        if (firstMap == null) {
            firstMap = new LinkedHashMap<String, DfDelimiterDataLoadedMeta>();
            _loadedMetaMap.put(dataDirectory, firstMap);
        }
        firstMap.put(fileName, new DfDelimiterDataLoadedMeta(fileName, successRowCount));
    }

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
    public Map<String, Map<String, DfDelimiterDataLoadedMeta>> getLoadedMetaMap() {
        return Collections.unmodifiableMap(_loadedMetaMap);
    }

    public Map<String, Set<String>> getNotFoundColumnMap() {
        return _notFoundColumnMap; // cannot be read-only, put by outer processes
    }

    public Map<String, List<String>> getColumnCountDiffMap() {
        return Collections.unmodifiableMap(_columnCountDiffMap);
    }
}
