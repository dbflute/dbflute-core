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
package org.dbflute.properties.assistant.classification.top.grouping;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.dbflute.exception.DfIllegalPropertySettingException;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.properties.assistant.classification.DfClassificationGroup;
import org.dbflute.properties.assistant.classification.DfClassificationTop;

/**
 * @author jflute
 * @since 1.2.5 (2021/07/06 Tuesday at roppongi japanese)
 */
public class DfClsTopGroupInheritanceArranger {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final String KEY_GROUP_COMMENT = DfClsTopGroupListArranger.KEY_GROUP_COMMENT;
    protected static final String KEY_ELEMENT_LIST = DfClsTopGroupListArranger.KEY_ELEMENT_LIST;
    protected static final String KEY_USE_DOCUMENT_ONLY = DfClsTopGroupListArranger.KEY_USE_DOCUMENT_ONLY;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfClassificationTop _classificationTop; // my classification, not null
    protected final DfClassificationTop _referredClsTop; // referred classification, not null

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfClsTopGroupInheritanceArranger(DfClassificationTop classificationTop, DfClassificationTop referredClsTop) {
        _classificationTop = classificationTop;
        _referredClsTop = referredClsTop;
    }

    // ===================================================================================
    //                                                                       Inherit Group
    //                                                                       =============
    public void inheritRefClsGroup(Map<String, Map<String, Object>> groupingMap) { // for e.g. appcls
        if (_classificationTop.isAlreadyGroupArranged()) { // basically no way
            throw new IllegalStateException("Group objects are already arranged so too late: " + _classificationTop);
        }
        final List<DfClassificationGroup> referredGroupList = _referredClsTop.getGroupList();
        for (DfClassificationGroup referredGroup : referredGroupList) {
            if (groupingMap.containsKey(referredGroup.getGroupName())) {
                throwClassificationReferredGroupingMapConflictException(referredGroup);
            }
            final List<String> translatedElementNameList = translateElementNameList(referredGroup);
            if (translatedElementNameList.isEmpty()) { // when completely not related to the group elements
                continue;
            }
            final Map<String, Object> contentMap = new LinkedHashMap<>();
            contentMap.put(DfClsTopGroupListArranger.KEY_GROUP_COMMENT, referredGroup.getGroupComment());
            contentMap.put(DfClsTopGroupListArranger.KEY_ELEMENT_LIST, translatedElementNameList);
            contentMap.put(DfClsTopGroupListArranger.KEY_USE_DOCUMENT_ONLY, String.valueOf(referredGroup.isUseDocumentOnly()));
            groupingMap.put(referredGroup.getGroupName(), contentMap);
        }
    }

    protected List<String> translateElementNameList(DfClassificationGroup referredGroup) {
        // translate by code for e.g. exists, matches (names may be changed in the case)
        final List<String> referredGroupedCodeList = extractReferredGroupedCodeList(referredGroup);
        return _classificationTop.getClassificationElementList().stream() // /all elements of my classfication
                .filter(el -> referredGroupedCodeList.contains(el.getCode())) // for e.g. group reference, exists
                .map(el -> el.getName()).collect(Collectors.toList()); // to name
    }

    protected List<String> extractReferredGroupedCodeList(DfClassificationGroup referredGroup) {
        return referredGroup.getElementList().stream().map(el -> el.getCode()).collect(Collectors.toList());
    }

    protected void throwClassificationReferredGroupingMapConflictException(DfClassificationGroup referredGroup) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The group name is conflicted with grouping map of the referred classification.");
        br.addItem("Advice");
        br.addElement("You cannot define same-name group as one of referred classification.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    refCls has serviceAvailable");
        br.addElement("    myCls has serviceAvailable // *Bad");
        br.addElement("  (o):");
        br.addElement("    refCls has serviceAvailable");
        br.addElement("    myCls has otherNameAvailable // Good");
        br.addItem("Classification Name");
        br.addElement(_classificationTop.getClassificationName());
        br.addItem("refCls Name");
        br.addElement(_referredClsTop.getClassificationName());
        br.addItem("Conflicted Group");
        br.addElement(referredGroup.getGroupName());
        final String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg);
    }
}
