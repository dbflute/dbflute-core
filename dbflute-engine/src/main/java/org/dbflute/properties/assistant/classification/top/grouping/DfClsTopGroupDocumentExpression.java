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
package org.dbflute.properties.assistant.classification.top.grouping;

import java.util.List;

import org.dbflute.DfBuildProperties;
import org.dbflute.properties.DfDocumentProperties;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.2.5 split from DfClassificationGroup (2021/07/03 Saturday at roppongi japanese)
 */
public class DfClsTopGroupDocumentExpression {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _groupComment; // null allowed (not required)
    protected final List<String> _elementNameList; // null allowed, empty allowed

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfClsTopGroupDocumentExpression(String groupComment, List<String> elementNameList) {
        _groupComment = groupComment;
        _elementNameList = elementNameList;
    }

    // ===================================================================================
    //                                                                HTML Title Attribute
    //                                                                ====================
    public String buildGroupTitleForSchemaHtml() {
        final StringBuilder sb = new StringBuilder();
        if (Srl.is_NotNull_and_NotTrimmedEmpty(_groupComment)) {
            sb.append(_groupComment);
        } else {
            sb.append("(no comment)");
        }
        sb.append(" :: ");
        sb.append(_elementNameList);
        final String title = resolveSchemaHtmlTagAttr(sb.toString());
        return title != null ? " title=\"" + title + "\"" : "";
    }

    public String buildElementDisp() {
        return "The group elements:" + _elementNameList;
    }

    // ===================================================================================
    //                                                                         Escape Text
    //                                                                         ===========
    protected String resolveSchemaHtmlTagAttr(String comment) {
        return getDocumentProperties().resolveSchemaHtmlTagAttr(comment);
    }

    protected DfDocumentProperties getDocumentProperties() {
        return DfBuildProperties.getInstance().getDocumentProperties();
    }
}
