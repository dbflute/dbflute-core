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
package org.dbflute.properties.assistant.classification.top.grouping;

import java.util.ArrayList;
import java.util.List;

import org.dbflute.exception.DfIllegalPropertySettingException;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.properties.assistant.classification.DfClassificationElement;
import org.dbflute.properties.assistant.classification.DfClassificationTop;

/**
 * @author jflute
 * @since 1.2.5 split from DfClassificationGroup (2021/07/03 Saturday at roppongi japanese)
 */
public class DfClsTopGroupElementHandling {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfClassificationTop _classificationTop; // not null
    protected final String _groupName; // not null
    protected final String _groupComment; // null allowed (not required)
    protected final List<String> _elementNameList; // null allowed, empty allowed

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfClsTopGroupElementHandling(DfClassificationTop classificationTop, String groupName, String groupComment,
            List<String> elementNameList) {
        _classificationTop = classificationTop;
        _groupName = groupName;
        _groupComment = groupComment;
        _elementNameList = elementNameList;
    }

    // ===================================================================================
    //                                                             Element Object Handling
    //                                                             =======================
    public List<DfClassificationElement> toElementList() {
        if (_elementNameList == null) {
            return new ArrayList<DfClassificationElement>();
        }
        final int size = _elementNameList.size();
        final List<DfClassificationElement> elementList = new ArrayList<DfClassificationElement>(size);
        for (String elementName : _elementNameList) {
            final DfClassificationElement element = _classificationTop.findClassificationElementByName(elementName);
            if (element == null) {
                throwClassificationGroupingMapElementNotFoundException(elementName);
            }
            elementList.add(element);
        }
        return elementList;
    }

    protected void throwClassificationGroupingMapElementNotFoundException(String elementName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the classification element in the grouping map.");
        br.addItem("Classification Name");
        br.addElement(_classificationTop.getClassificationName());
        br.addItem("Group Name");
        br.addElement(_groupName);
        br.addItem("NotFound Name");
        br.addElement(elementName);
        br.addItem("Defined Element");
        final List<DfClassificationElement> elementList = _classificationTop.getClassificationElementList();
        for (DfClassificationElement element : elementList) {
            br.addElement(element);
        }
        final String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg);
    }
}
