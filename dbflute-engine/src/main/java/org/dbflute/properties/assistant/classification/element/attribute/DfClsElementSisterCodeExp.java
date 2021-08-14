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
package org.dbflute.properties.assistant.classification.element.attribute;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.dbflute.helper.dfmap.DfMapStyle;

/**
 * @author jflute
 * @since 1.2.5 split from DfClassificationElement (2021/07/03 Saturday at roppongi japanese)
 */
public class DfClsElementSisterCodeExp {

    protected final String[] _sisters; // null allowed, empty allowed

    public DfClsElementSisterCodeExp(String[] sisters) {
        _sisters = sisters;
    }

    public String buildSisterCodeExpForSchemaHtml() { // e.g. "sea, land"
        if (_sisters == null || _sisters.length == 0) {
            return "&nbsp;";
        }
        final StringBuilder sb = new StringBuilder();
        int index = 0;
        for (String sister : _sisters) {
            if (index > 0) {
                sb.append(", ");
            }
            sb.append(sister);
            ++index;
        }
        return sb.toString();
    }

    public String buildSisterCodeExpForDfpropMap() { // e.g. "sea, land"
        final String listPrefix = DfMapStyle.LIST_PREFIX;
        final String beginBrace = DfMapStyle.BEGIN_BRACE;
        final String endBrace = DfMapStyle.END_BRACE;
        final String delimiter = DfMapStyle.ELEMENT_DELIMITER;
        if (_sisters == null || _sisters.length == 0) {
            return listPrefix + beginBrace + endBrace;
        }
        final String contentExp = Stream.of(_sisters).collect(Collectors.joining(" " + delimiter + " "));
        return listPrefix + beginBrace + contentExp + endBrace;
    }
}
