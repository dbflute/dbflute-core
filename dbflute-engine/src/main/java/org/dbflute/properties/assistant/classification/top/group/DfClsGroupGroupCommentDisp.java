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
package org.dbflute.properties.assistant.classification.top.group;

import org.dbflute.DfBuildProperties;
import org.dbflute.properties.DfDocumentProperties;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.2.5 split from DfClassificationGroup (2021/07/03 Saturday at roppongi japanese)
 */
public class DfClsGroupGroupCommentDisp {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _groupComment; // null allowed (not required)

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfClsGroupGroupCommentDisp(String groupComment) {
        _groupComment = groupComment;
    }

    // ===================================================================================
    //                                                                       Group Comment
    //                                                                       =============
    public String getGroupCommentForJavaDoc() {
        return buildGroupCommentForJavaDoc("    ");
    }

    public String getGroupCommentForJavaDocNest() {
        return buildGroupCommentForJavaDoc("        ");
    }

    protected String buildGroupCommentForJavaDoc(String indent) {
        return resolveTextForJavaDoc(getGroupCommentDisp(), indent);
    }

    public String getGroupCommentDisp() {
        return buildGroupCommentDisp();
    }

    protected String buildGroupCommentDisp() {
        if (_groupComment == null) {
            return "";
        }
        return Srl.replace(_groupComment, "\n", ""); // basically one line
    }

    // ===================================================================================
    //                                                                         Escape Text
    //                                                                         ===========
    protected String resolveTextForJavaDoc(String comment, String indent) {
        return getDocumentProperties().resolveJavaDocContent(comment, indent);
    }

    protected DfDocumentProperties getDocumentProperties() {
        return DfBuildProperties.getInstance().getDocumentProperties();
    }
}
