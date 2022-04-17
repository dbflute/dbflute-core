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
package org.dbflute.logic.replaceschema.loaddata.xls.dataprop;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.dbflute.helper.StringKeyMap;
import org.dbflute.properties.propreader.DfOutsideMapPropReader;
import org.dbflute.util.DfCollectionUtil;

/**
 * @author jflute
 * @since 1.2.5 extracted from DfXlsDataHandlerImpl (2021/01/18 Monday at roppongi japanese)
 */
public class DfNotTrimTableColumnProp {

    protected Map<String, Map<String, List<String>>> _notTrimTableColumnMapMap = DfCollectionUtil.newHashMap();

    public Map<String, List<String>> findNotTrimTableColumnMap(String dataDirectory) {
        final Map<String, List<String>> cachedMap = _notTrimTableColumnMapMap.get(dataDirectory);
        if (cachedMap != null) {
            return cachedMap;
        }
        final DfOutsideMapPropReader reader = new DfOutsideMapPropReader();
        String path = dataDirectory + "/notTrimColumnMap.dataprop";
        Map<String, List<String>> resultMap = reader.readMapAsStringListValue(path);
        if (resultMap == null || resultMap.isEmpty()) {
            path = dataDirectory + "/not-trim-column.txt"; // old style
            resultMap = reader.readMapAsStringListValue(path);
        }
        final Set<Entry<String, List<String>>> entrySet = resultMap.entrySet();
        final StringKeyMap<List<String>> flmap = StringKeyMap.createAsFlexible();
        for (Entry<String, List<String>> entry : entrySet) {
            flmap.put(entry.getKey(), entry.getValue());
        }
        _notTrimTableColumnMapMap.put(dataDirectory, flmap);
        return _notTrimTableColumnMapMap.get(dataDirectory);
    }
}
