/*
 * Copyright 2014-2023 the original author or authors.
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
package org.dbflute.properties.assistant.classification.element.topoption;

import java.util.List;

import org.dbflute.properties.assistant.classification.DfClassificationGroup;
import org.dbflute.properties.assistant.classification.DfClassificationTop;

/**
 * @author jflute
 * @since 1.2.5 split from DfClassificationElement (2021/07/03 Saturday at roppongi japanese)
 */
public class DfClsElementGroupHandling {

    protected final DfClassificationTop _classificationTop; // null allowed
    protected final String _name; // null allowed, empty allowed

    public DfClsElementGroupHandling(DfClassificationTop classificationTop, String name) {
        _classificationTop = classificationTop;
        _name = name;
    }

    public boolean isGroup(String groupName) {
        if (_classificationTop == null) {
            return false;
        }
        final List<DfClassificationGroup> groupList = _classificationTop.getGroupList();
        for (DfClassificationGroup group : groupList) {
            if (groupName.equals(group.getGroupName())) {
                final List<String> elementNameList = group.getElementNameList();
                if (_name != null && elementNameList.contains(_name)) {
                    return true;
                }
                break;
            }
        }
        return false;
    }
}
