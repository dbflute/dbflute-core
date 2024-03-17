/*
 * Copyright 2014-2024 the original author or authors.
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
package org.dbflute.properties.assistant.classification.element.attribute;

import java.util.Map;
import java.util.Map.Entry;

import org.dbflute.DfBuildProperties;
import org.dbflute.helper.dfmap.DfMapStyle;
import org.dbflute.properties.DfDocumentProperties;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.2.5 split from DfClassificationElement (2021/07/03 Saturday at roppongi japanese)
 */
public class DfClsElementSubItemExp {

    protected final Map<String, Object> _subItemMap; // null allowed, empty allowed

    public DfClsElementSubItemExp(Map<String, Object> subItemMap) {
        _subItemMap = subItemMap;
    }

    public String buildSubItemExpForSchemaHtml() {
        if (_subItemMap == null || _subItemMap.isEmpty()) {
            return "&nbsp;";
        }
        final StringBuilder sb = new StringBuilder();
        int index = 0;
        for (Entry<String, Object> entry : _subItemMap.entrySet()) {
            final String key = entry.getKey();
            final Object value = entry.getValue();
            if (index > 0) {
                sb.append("\n, ");
            }
            sb.append(key).append("=").append(value);
            ++index;
        }
        return filterSubItemForSchemaHtml(sb.toString());
    }

    protected String filterSubItemForSchemaHtml(String str) {
        final DfDocumentProperties prop = DfBuildProperties.getInstance().getDocumentProperties();
        return prop.resolveSchemaHtmlContent(Srl.replace(str, "\\n", "\n"));
    }

    public String buildSubItemExpForDfpropMap() {
        final String mapPrefix = DfMapStyle.MAP_PREFIX;
        final String beginBrace = DfMapStyle.BEGIN_BRACE;
        final String endBrace = DfMapStyle.END_BRACE;
        final String delimiter = DfMapStyle.ELEMENT_DELIMITER;
        final String valueEqual = DfMapStyle.VALUE_EQUAL;
        if (_subItemMap == null || _subItemMap.isEmpty()) {
            return mapPrefix + beginBrace + endBrace;
        }
        final StringBuilder sb = new StringBuilder();
        sb.append(mapPrefix).append(beginBrace);
        int index = 0;
        for (Entry<String, Object> entry : _subItemMap.entrySet()) {
            final String key = entry.getKey();
            final Object value = entry.getValue();
            if (index > 0) {
                sb.append(delimiter);
            }
            sb.append(key).append(valueEqual).append(value);
            ++index;
        }
        sb.append(endBrace);
        return sb.toString();
    }
}
