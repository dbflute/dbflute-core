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
package org.dbflute.properties.assistant.classification.top.elementoption.subitem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dbflute.properties.assistant.classification.DfClassificationElement;
import org.dbflute.properties.assistant.classification.DfClassificationTop;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.2.5 split from DfClassificationTop (2021/07/03 Saturday at roppongi japanese)
 */
public class DfClsTopSubItemHandler {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfClassificationTop _classificationTop; // not null, mutable

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfClsTopSubItemHandler(DfClassificationTop classificationTop) {
        _classificationTop = classificationTop;
    }

    // ===================================================================================
    //                                                                       Determination
    //                                                                       =============
    public boolean hasSubItem() {
        final List<DfClassificationElement> elementList = _classificationTop.getClassificationElementList();
        for (DfClassificationElement element : elementList) {
            Map<String, Object> subItemMap = element.getSubItemMap();
            if (subItemMap != null && !subItemMap.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    // ===================================================================================
    //                                                                     Regular SubItem
    //                                                                     ===============
    // Regular SubItem means existing in all elements
    public List<DfClsTopRegularSubItem> arrangeRegularSubItemList() {
        final List<DfClassificationElement> elementList = _classificationTop.getClassificationElementList();
        final Map<String, List<Object>> subItemListMap = new LinkedHashMap<String, List<Object>>();
        for (DfClassificationElement element : elementList) {
            final Map<String, Object> subItemMap = element.getSubItemMap();
            if (subItemMap == null || subItemMap.isEmpty()) {
                continue;
            }
            for (Entry<String, Object> entry : subItemMap.entrySet()) {
                final String subItemKey = entry.getKey();
                final Object subItemValue = entry.getValue();
                List<Object> subItemList = subItemListMap.get(subItemKey);
                if (subItemList == null) {
                    subItemList = new ArrayList<Object>();
                    subItemListMap.put(subItemKey, subItemList);
                }
                subItemList.add(subItemValue);
            }
        }
        final String typeObject = DfClsTopRegularSubItem.TYPE_OBJECT;
        final String typeString = DfClsTopRegularSubItem.TYPE_STRING;
        final List<DfClsTopRegularSubItem> regularSubItemList = new ArrayList<DfClsTopRegularSubItem>();
        final int elementSize = elementList.size();
        for (Entry<String, List<Object>> entry : subItemListMap.entrySet()) {
            final String subItemKey = entry.getKey();
            final List<Object> subItemList = entry.getValue();
            if (subItemList != null && subItemList.size() == elementSize) {
                String subItemType = null;
                for (Object value : subItemList) {
                    if (value == null) {
                        continue;
                    }
                    if (!(value instanceof String)) {
                        subItemType = typeObject;
                        break;
                    } else if (Srl.startsWith((String) value, "map:", "list:")) {
                        subItemType = typeObject;
                        break;
                    }
                }
                if (subItemType == null) {
                    subItemType = typeString;
                }
                regularSubItemList.add(new DfClsTopRegularSubItem(subItemKey, subItemType));
            }
        }
        return regularSubItemList;
    }
}
