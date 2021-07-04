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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.dbflute.exception.DfClassificationRequiredAttributeNotFoundException;
import org.dbflute.exception.DfIllegalPropertySettingException;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.properties.assistant.classification.DfClassificationGroup;
import org.dbflute.properties.assistant.classification.DfClassificationTop;
import org.dbflute.task.DfDBFluteTaskStatus;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.2.5 split from DfClassificationTop (2021/07/03 Saturday at roppongi japanese)
 */
public class DfClsGroupArranger {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfClassificationTop _classificationTop; // not null, mutable
    protected final Map<String, Map<String, Object>> _groupingMap; // not null, mutable

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfClsGroupArranger(DfClassificationTop classificationTop, Map<String, Map<String, Object>> groupingMap) {
        _classificationTop = classificationTop;
        _groupingMap = groupingMap;
    }

    // ===================================================================================
    //                                                                          Group List
    //                                                                          ==========
    public List<DfClassificationGroup> arrangeGroupList() {
        final List<DfClassificationGroup> groupList = new ArrayList<DfClassificationGroup>();
        for (Entry<String, Map<String, Object>> entry : _groupingMap.entrySet()) {
            final String groupName = entry.getKey();
            final Map<String, Object> attrMap = entry.getValue();
            final String groupComment = (String) attrMap.get("groupComment");
            @SuppressWarnings("unchecked")
            final List<String> elementList = (List<String>) attrMap.get("elementList");
            if (elementList == null) {
                String msg = "The elementList in grouping map is required: " + _classificationTop.getClassificationName();
                throw new DfClassificationRequiredAttributeNotFoundException(msg);
            }
            final String docOnly = (String) attrMap.get("isUseDocumentOnly");
            final DfClassificationGroup group = new DfClassificationGroup(_classificationTop, groupName);
            group.setGroupComment(groupComment);
            group.setElementNameList(elementList);
            group.setUseDocumentOnly(docOnly != null && docOnly.trim().equalsIgnoreCase("true"));
            groupList.add(group);
        }
        resolveGroupVariable(groupList);
        return filterTaskMatchingList(groupList);
    }

    // -----------------------------------------------------
    //                                        Group Variable
    //                                        --------------
    protected void resolveGroupVariable(List<DfClassificationGroup> groupList) {
        final Map<String, DfClassificationGroup> groupMap = new LinkedHashMap<String, DfClassificationGroup>();
        for (DfClassificationGroup group : groupList) {
            groupMap.put(group.getGroupName(), group);
        }
        // e.g.
        // ; servicePlus = map:{
        //     ; elementList = list:{ $$ref$$.serviceAvailable ; Withdrawal }
        // }
        final String refPrefix = "$$ref$$.";
        for (DfClassificationGroup group : groupList) {
            final List<String> elementNameList = group.getElementNameList();
            final Set<String> resolvedNameSet = new LinkedHashSet<String>();
            for (String elementName : elementNameList) {
                if (Srl.startsWith(elementName, refPrefix)) {
                    final String refName = Srl.substringFirstRear(elementName, refPrefix).trim();
                    final DfClassificationGroup refGroup = groupMap.get(refName);
                    if (refGroup == null) {
                        throwClassificationGroupingMapReferenceNotFoundException(groupList, group, refName);
                    }
                    resolvedNameSet.addAll(refGroup.getElementNameList());
                } else {
                    resolvedNameSet.add(elementName);
                }
            }
            if (elementNameList.size() < resolvedNameSet.size()) {
                group.setElementNameList(new ArrayList<String>(resolvedNameSet));
            }
        }
    }

    protected void throwClassificationGroupingMapReferenceNotFoundException(List<DfClassificationGroup> groupList,
            DfClassificationGroup group, String refName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the refenrece in the grouping map.");
        br.addItem("Classification Name");
        br.addElement(group.getClassificationName());
        br.addItem("Group Name");
        br.addElement(group.getGroupName());
        br.addItem("NotFound Name");
        br.addElement(refName);
        br.addItem("Defined Group");
        for (DfClassificationGroup defined : groupList) {
            br.addElement(defined);
        }
        final String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg);
    }

    // -----------------------------------------------------
    //                                    Task-Matching List
    //                                    ------------------
    protected List<DfClassificationGroup> filterTaskMatchingList(List<DfClassificationGroup> groupList) {
        final List<DfClassificationGroup> realList = new ArrayList<DfClassificationGroup>();
        final boolean docOnly = isDocOnlyTaskNow();
        for (DfClassificationGroup group : groupList) {
            if (!docOnly && group.isUseDocumentOnly()) {
                continue;
            }
            realList.add(group);
        }
        return realList;
    }

    public boolean isDocOnlyTaskNow() {
        final DfDBFluteTaskStatus instance = DfDBFluteTaskStatus.getInstance();
        return instance.isDocTask() || instance.isReplaceSchema();
    }
}
