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
package org.dbflute.logic.replaceschema.loaddata.delimiter.line;

import java.util.Map;
import java.util.Map.Entry;

import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.2.5 extracted from DfDelimiterDataWriterImpl (2021/01/20 Wednesday at roppongi japanese)
 */
public class DfDelimiterDataLineDirectFilter {

    protected Map<String, String> _convertLineCacheMap;

    public void filterLineString(StringBuilder lineStringSb, Map<String, Map<String, String>> convertValueMap) {
        final Map<String, String> convertLineMap = findConvertLineMap(convertValueMap);
        if (convertLineMap.isEmpty()) {
            return;
        }
        String filtered = lineStringSb.toString();
        for (Entry<String, String> entry : convertLineMap.entrySet()) {
            // #hope should use StringBuilder replace for performance? by jflute (2021/01/22)
            filtered = Srl.replace(filtered, entry.getKey(), entry.getValue());
        }
        lineStringSb.setLength(0);
        lineStringSb.append(filtered);
    }

    protected Map<String, String> findConvertLineMap(Map<String, Map<String, String>> convertValueMap) {
        if (_convertLineCacheMap != null) {
            return _convertLineCacheMap;
        }
        if (convertValueMap != null) {
            _convertLineCacheMap = convertValueMap.get("$$LINE$$");
        }
        if (_convertLineCacheMap == null) {
            _convertLineCacheMap = DfCollectionUtil.emptyMap();
        }
        return _convertLineCacheMap;
    }
}
