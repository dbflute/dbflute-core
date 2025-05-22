/*
 * Copyright 2014-2025 the original author or authors.
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
package org.dbflute.properties.assistant.base;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.dbflute.exception.DfIllegalPropertySettingException;
import org.dbflute.helper.message.ExceptionMessageBuilder;

/**
 * @author jflute
 * @since 1.1.7 (2018/03/17 Saturday)
 */
public class DfSplittingDfpropHandler {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfPropertyValueHandler _propertyValueHandler;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfSplittingDfpropHandler(DfPropertyValueHandler propertyValueHandler) {
        _propertyValueHandler = propertyValueHandler;
    }

    // ===================================================================================
    //                                                                        Split DfProp
    //                                                                        ============
    public Map<String, Object> resolveSplit(String mapName, Map<String, Object> plainDefinitionMap) {
        Map<String, Object> splitDefinitionMap = null;
        for (Entry<String, Object> entry : plainDefinitionMap.entrySet()) {
            final String rootKey = entry.getKey();
            if (isSplitMark(rootKey)) {
                @SuppressWarnings("unchecked")
                final Map<String, Object> splitMap = (Map<String, Object>) entry.getValue();
                splitDefinitionMap = handleSplitDefinition(mapName, rootKey, splitMap);
            }
        }
        if (splitDefinitionMap == null) {
            return plainDefinitionMap;
        }
        final Map<String, Object> resolvedMap = new LinkedHashMap<String, Object>();
        for (Entry<String, Object> entry : plainDefinitionMap.entrySet()) {
            final String rootKey = entry.getKey();
            if (isSplitMark(rootKey)) {
                continue;
            }
            resolvedMap.put(rootKey, entry.getValue());
        }
        if (splitDefinitionMap != null) {
            for (Entry<String, Object> entry : splitDefinitionMap.entrySet()) {
                final String elementName = entry.getKey();
                if (resolvedMap.containsKey(elementName)) {
                    throwDfPropDefinitionDuplicateDefinitionException(mapName, elementName, null);
                }
                resolvedMap.put(elementName, entry.getValue());
            }
        }
        return resolvedMap;
    }

    protected boolean isSplitMark(String rootKey) {
        return rootKey.equalsIgnoreCase("$$split$$");
    }

    protected Map<String, Object> handleSplitDefinition(String mapName, String rootKey, Map<?, ?> splitMap) {
        final Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
        @SuppressWarnings("unchecked")
        final Set<String> keywordSet = (Set<String>) splitMap.keySet();
        for (String keyword : keywordSet) {
            final Map<String, Object> splitProp = _propertyValueHandler.getOutsideMapProp(mapName + "_" + keyword);
            if (splitProp.isEmpty()) {
                throwClassificationSplitDefinitionNotFoundException(mapName, keyword);
            }
            for (Entry<String, Object> entry : splitProp.entrySet()) {
                final String elementName = entry.getKey();
                if (resultMap.containsKey(elementName)) {
                    throwDfPropDefinitionDuplicateDefinitionException(mapName, elementName, keyword);
                }
                resultMap.put(elementName, entry.getValue());
            }
        }
        return resultMap;
    }

    protected void throwClassificationSplitDefinitionNotFoundException(String mapName, String keyword) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the split definition of DBFlute property.");
        br.addItem("Advice");
        br.addElement("Make sure the file name of your split dfprop");
        br.addElement("or define at least one definition in the split file.");
        br.addElement("");
        br.addElement("For example:");
        br.addElement("    $$split$$ = map:{");
        br.addElement("        ; land = dummy");
        br.addElement("        ; sea  = dummy");
        br.addElement("    }");
        br.addElement("");
        br.addElement("The following files should exist:");
        br.addElement("    dfprop/" + mapName + "_land.dfprop");
        br.addElement("    dfprop/" + mapName + "_sea.dfprop");
        br.addItem("DBFlute Property");
        br.addElement(mapName);
        br.addItem("Split Keyword");
        br.addElement(keyword);
        final String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg);
    }

    protected void throwDfPropDefinitionDuplicateDefinitionException(String mapName, String elementName, String keyword) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Found the duplicate definition.");
        br.addItem("Advice");
        br.addElement("The element names should be unique.");
        br.addElement("(in all split files if split)");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    Sea = map:{");
        br.addElement("        ; ...");
        br.addElement("    }");
        br.addElement("    Sea = map:{");
        br.addElement("        ; ...");
        br.addElement("    }");
        br.addElement("  (o):");
        br.addElement("    Land = map:{");
        br.addElement("        ; ...");
        br.addElement("    }");
        br.addElement("    Sea = map:{");
        br.addElement("        ; ...");
        br.addElement("    }");
        br.addItem("DBFlute Property");
        br.addElement(mapName);
        if (keyword != null) {
            br.addItem("Duplicate Found Location");
            br.addElement(keyword);
        }
        br.addItem("Duplicate Name");
        br.addElement(elementName);
        final String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg);
    }
}
