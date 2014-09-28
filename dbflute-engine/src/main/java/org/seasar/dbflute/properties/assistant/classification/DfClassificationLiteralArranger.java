/*
 * Copyright 2004-2014 the Seasar Foundation and the Others.
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
package org.seasar.dbflute.properties.assistant.classification;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.seasar.dbflute.exception.DfClassificationRequiredAttributeNotFoundException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.helper.mapstring.MapListString;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.9.5.1 (2009/07/03 Friday)
 */
public class DfClassificationLiteralArranger {

    public void arrange(String classificationName, Map<String, Object> elementMap, List<Map<String, Object>> elementList) {
        final String codeKey = DfClassificationElement.KEY_CODE;
        final String code = (String) elementMap.get(codeKey);
        if (code == null) { // required check
            throwClassificationLiteralCodeNotFoundException(classificationName, elementMap);
        }

        final String nameKey = DfClassificationElement.KEY_NAME;
        final String name = (String) elementMap.get(nameKey);
        if (name == null) { // use code
            elementMap.put(nameKey, code);
        }

        final String aliasKey = DfClassificationElement.KEY_ALIAS;
        final String alias = (String) elementMap.get(aliasKey);
        if (alias == null) { // use name or code
            elementMap.put(aliasKey, name != null ? name : code);
        }

        final String subItemMapKey = DfClassificationElement.KEY_SUB_ITEM_MAP;
        @SuppressWarnings("unchecked")
        final Map<String, Object> subItemMap = (Map<String, Object>) elementMap.get(subItemMapKey);
        if (subItemMap != null) {
            final MapListString mapListString = new MapListString();
            for (Entry<String, Object> entry : subItemMap.entrySet()) {
                String key = entry.getKey();
                final Object value = entry.getValue();
                if (value != null && value instanceof List<?>) { // nested List is treated as string
                    final List<?> listValue = (List<?>) value;
                    final String listString = mapListString.buildListString(listValue);
                    subItemMap.put(key, filterLineStringOnMapListString(listString));
                } else if (value != null && value instanceof Map<?, ?>) { // nested Map is treated as string
                    @SuppressWarnings("unchecked")
                    final Map<String, ?> mapValue = (Map<String, ?>) value;
                    final String mapString = mapListString.buildMapString(mapValue);
                    subItemMap.put(key, filterLineStringOnMapListString(mapString));
                } else if (value != null && value instanceof String) {
                    subItemMap.put(key, filterLineStringOnMapListString((String) value));
                }
            }
        }

        elementList.add(elementMap);
    }

    protected String filterLineStringOnMapListString(String mapListString) {
        return Srl.replace(mapListString, "\n", "\\n");
    }

    protected void throwClassificationLiteralCodeNotFoundException(String classificationName, Map<?, ?> elementMap) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The code attribute of the classification was not found.");
        br.addItem("Advice");
        br.addElement("The classification should have the code attribute.");
        br.addElement("See the document for the DBFlute property.");
        br.addItem("Classification");
        br.addElement(classificationName);
        br.addItem("Element Map");
        br.addElement(elementMap);
        final String msg = br.buildExceptionMessage();
        throw new DfClassificationRequiredAttributeNotFoundException(msg);
    }
}
