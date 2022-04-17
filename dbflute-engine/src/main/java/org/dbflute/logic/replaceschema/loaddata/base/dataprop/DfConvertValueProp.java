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
package org.dbflute.logic.replaceschema.loaddata.base.dataprop;

import java.util.Map;
import java.util.Map.Entry;

import org.dbflute.helper.StringKeyMap;
import org.dbflute.properties.propreader.DfOutsideMapPropReader;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.2.5 extracted from DfXlsDataHandlerImpl (2021/01/18 Monday at roppongi japanese)
 */
public class DfConvertValueProp {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected Map<String, Map<String, Map<String, String>>> _convertValueMapMap = DfCollectionUtil.newHashMap();

    // ===================================================================================
    //                                                                            Find Map
    //                                                                            ========
    public Map<String, Map<String, String>> findConvertValueMap(String dataDirectory) {
        final Map<String, Map<String, String>> cachedMap = _convertValueMapMap.get(dataDirectory);
        if (cachedMap != null) {
            return cachedMap;
        }
        _convertValueMapMap.put(dataDirectory, doGetConvertValueMap(dataDirectory));
        return _convertValueMapMap.get(dataDirectory);
    }

    protected Map<String, Map<String, String>> doGetConvertValueMap(String dataDirectory) {
        final DfOutsideMapPropReader reader = new DfOutsideMapPropReader();
        String path = dataDirectory + "/convertValueMap.dataprop";
        final Map<String, Map<String, String>> resultMap = StringKeyMap.createAsFlexibleOrdered();
        Map<String, Map<String, String>> readMap = reader.readMapAsStringMapValue(path);
        if (readMap != null && !readMap.isEmpty()) {
            resultMap.putAll(readMap);
        } else {
            path = dataDirectory + "/convert-value.txt";
            readMap = reader.readMapAsStringMapValue(path);
            resultMap.putAll(readMap);
        }
        return resolveControlCharacter(resultMap);
    }

    protected static Map<String, Map<String, String>> resolveControlCharacter(Map<String, Map<String, String>> convertValueMap) {
        final Map<String, Map<String, String>> resultMap = StringKeyMap.createAsFlexibleOrdered();
        for (Entry<String, Map<String, String>> entry : convertValueMap.entrySet()) {
            final Map<String, String> elementMap = DfCollectionUtil.newLinkedHashMap();
            for (Entry<String, String> nextEntry : entry.getValue().entrySet()) {
                final String key = resolveControlCharacter(nextEntry.getKey());
                final String value = resolveControlCharacter(nextEntry.getValue());
                elementMap.put(key, value);
            }
            resultMap.put(entry.getKey(), elementMap);
        }
        return resultMap;
    }

    protected static String resolveControlCharacter(String value) {
        if (value == null) {
            return null;
        }
        final String tmp = "${df:temporaryVariable}";
        value = Srl.replace(value, "\\\\", tmp); // "\\" to "\" later

        // e.g. pure string "\n" to (real) line separator
        value = Srl.replace(value, "\\r", "\r");
        value = Srl.replace(value, "\\n", "\n");
        value = Srl.replace(value, "\\t", "\t");

        value = Srl.replace(value, tmp, "\\");
        return value;
    }
}
