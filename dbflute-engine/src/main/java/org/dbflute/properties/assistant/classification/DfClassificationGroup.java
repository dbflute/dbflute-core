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
package org.dbflute.properties.assistant.classification;

import java.util.List;
import java.util.stream.Collectors;

import org.dbflute.DfBuildProperties;
import org.dbflute.properties.DfDocumentProperties;
import org.dbflute.properties.assistant.classification.top.grouping.DfClsTopGroupCodingExpression;
import org.dbflute.properties.assistant.classification.top.grouping.DfClsTopGroupCommentDisp;
import org.dbflute.properties.assistant.classification.top.grouping.DfClsTopGroupDocumentExpression;
import org.dbflute.properties.assistant.classification.top.grouping.DfClsTopGroupElementHandling;
import org.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfClassificationGroup { // directly used in template

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfClassificationTop _classificationTop; // not null
    protected final String _groupName; // not null
    protected String _groupComment; // null allowed (not required)
    protected List<String> _elementNameList; // basically not null (is set immediately after initialization)
    protected boolean _useDocumentOnly;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfClassificationGroup(DfClassificationTop classificationTop, String groupName) {
        _classificationTop = classificationTop;
        _groupName = groupName;
    }

    // ===================================================================================
    //                                                                          Basic Name
    //                                                                          ==========
    public String getClassificationName() {
        return _classificationTop.getClassificationName();
    }

    public String getGroupNameInitCap() {
        return Srl.initCap(_groupName);
    }

    // ===================================================================================
    //                                                                       Group Comment
    //                                                                       =============
    public boolean hasGroupComment() {
        return _groupComment != null;
    }

    public String getGroupCommentDisp() { // unused in template (e.g. forJavaDoc used instead) (2021/07/03)
        return createClsGroupGroupCommentDisp().buildGroupCommentDisp();
    }

    public String getGroupCommentForJavaDoc() {
        return createClsGroupGroupCommentDisp().buildGroupCommentForJavaDoc();
    }

    public String getGroupCommentForJavaDocNest() {
        return createClsGroupGroupCommentDisp().buildGroupCommentForJavaDocNest();
    }

    // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
    // forSchemaHtml is treated as groupTitle
    // _/_/_/_/_/_/_/_/_/_/

    public String getGroupCommentForDfpropMap() {
        return createClsGroupGroupCommentDisp().buildGroupCommentForDfpropMap();
    }

    protected DfClsTopGroupCommentDisp createClsGroupGroupCommentDisp() {
        return new DfClsTopGroupCommentDisp(_groupComment);
    }

    // ===================================================================================
    //                                                                   Coding Expression
    //                                                                   =================
    public String buildReturnExpThis() {
        return createClsGroupCodingExpression().buildReturnExpThis();
    }

    public String buildCDefArgExp() {
        return createClsGroupCodingExpression().buildCDefArgExp();
    }

    public String buildCDefArgExp(String cdefClassName) {
        return createClsGroupCodingExpression().buildCDefArgExp(cdefClassName);
    }

    protected DfClsTopGroupCodingExpression createClsGroupCodingExpression() {
        return new DfClsTopGroupCodingExpression(_classificationTop, _elementNameList);
    }

    // ===================================================================================
    //                                                                 Document Expression
    //                                                                 ===================
    public String getGroupTitleForSchemaHtml() {
        return createClsGroupDocumentExpression().buildGroupTitleForSchemaHtml();
    }

    public String buildElementDisp() {
        return createClsGroupDocumentExpression().buildElementDisp();
    }

    protected DfClsTopGroupDocumentExpression createClsGroupDocumentExpression() {
        return new DfClsTopGroupDocumentExpression(_groupComment, _elementNameList);
    }

    // ===================================================================================
    //                                                             Element Object Handling
    //                                                             =======================
    public List<DfClassificationElement> getElementList() {
        return createClsGroupElementHandling().toElementList();
    }

    public String getElementNameExpForDfpropMap() { // e.g. "Formalized ; Provisional"
        return getElementList().stream().map(el -> el.getName()).collect(Collectors.joining(" ; "));
    }

    protected DfClsTopGroupElementHandling createClsGroupElementHandling() {
        return new DfClsTopGroupElementHandling(_classificationTop, _groupName, _groupComment, _elementNameList);
    }

    // ===================================================================================
    //                                                                         Escape Text
    //                                                                         ===========
    protected String resolveTextForJavaDoc(String comment, String indent) {
        final DfDocumentProperties prop = getProperties().getDocumentProperties();
        return prop.resolveJavaDocContent(comment, indent);
    }

    protected String resolveTextForSchemaHtml(String comment) {
        final DfDocumentProperties prop = getProperties().getDocumentProperties();
        return prop.resolveSchemaHtmlContent(comment);
    }

    protected DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return "{" + _groupName + ": " + _elementNameList + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public DfClassificationTop getClassificationTop() {
        return _classificationTop; // not null
    }

    public String getGroupName() {
        return _groupName; // not null
    }

    public String getGroupComment() {
        return _groupComment; // null allowed (not required)
    }

    public void setGroupComment(String groupComment) {
        _groupComment = groupComment;
    }

    public List<String> getElementNameList() {
        return _elementNameList;
    }

    public void setElementNameList(List<String> elementNameList) {
        _elementNameList = elementNameList;
    }

    public boolean isUseDocumentOnly() {
        return _useDocumentOnly;
    }

    public void setUseDocumentOnly(boolean useDocumentOnly) {
        _useDocumentOnly = useDocumentOnly;
    }
}
