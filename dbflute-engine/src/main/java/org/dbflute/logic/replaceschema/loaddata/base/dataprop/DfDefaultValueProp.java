/*
 * Copyright 2014-2021 the original author or authors.
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
package org.dbflute.logic.replaceschema.loaddata.base.dataprop;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.dbflute.helper.StringKeyMap;
import org.dbflute.properties.propreader.DfOutsideMapPropReader;
import org.dbflute.util.DfCollectionUtil;

/**
 * @author jflute
 * @since 1.0.4A (2013/03/09 Saturday)
 */
public class DfDefaultValueProp {

    protected Map<String, Map<String, String>> _defaultValueMapMap = DfCollectionUtil.newHashMap();

    public Map<String, String> findDefaultValueMap(String dataDirectory) {
        final Map<String, String> cachedMap = _defaultValueMapMap.get(dataDirectory);
        if (cachedMap != null) {
            return cachedMap;
        }
        final StringKeyMap<String> flmap = doGetDefaultValueMap(dataDirectory);
        _defaultValueMapMap.put(dataDirectory, flmap);
        return _defaultValueMapMap.get(dataDirectory);
    }

    protected StringKeyMap<String> doGetDefaultValueMap(String dataDirectory) { // recycle
        final DfOutsideMapPropReader reader = new DfOutsideMapPropReader();
        String path = dataDirectory + "/defaultValueMap.dataprop";
        Map<String, String> resultMap = reader.readMapAsStringValue(path);
        if (resultMap == null || resultMap.isEmpty()) {
            path = dataDirectory + "/default-value.txt"; // old style
            resultMap = reader.readMapAsStringValue(path);
        }
        // ordered here because of columns added
        final StringKeyMap<String> flmap = StringKeyMap.createAsFlexibleOrdered();
        flmap.putAll(resultMap);
        return flmap;
    }

    public Set<String> extractSysdateColumnSet(Map<String, Object> columnValueMap, Map<String, String> defaultValueMap) {
        // should be called before convert
        if (defaultValueMap == null || defaultValueMap.isEmpty()) {
            return null;
        }
        if (!defaultValueMap.containsValue("sysdate")) {
            return null;
        }
        Set<String> sysdateColumnSet = null;
        for (Entry<String, Object> entry : columnValueMap.entrySet()) {
            final String columnName = entry.getKey();
            final Object value = entry.getValue();
            if (value == null || (value instanceof String && ((String) value).length() == 0)) {
                final String defaultValue = defaultValueMap.get(columnName);
                if (defaultValue != null && "sysdate".equalsIgnoreCase(defaultValue)) {
                    if (sysdateColumnSet == null) {
                        sysdateColumnSet = new HashSet<String>(2);
                    }
                    sysdateColumnSet.add(columnName);
                }
            }
        }
        return sysdateColumnSet;
    }
}
