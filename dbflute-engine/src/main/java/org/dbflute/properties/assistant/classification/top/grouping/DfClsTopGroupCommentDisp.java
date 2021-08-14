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

import org.dbflute.properties.assistant.document.textresolver.DfDocumentTextResolver;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.2.5 split from DfClassificationGroup (2021/07/03 Saturday at roppongi japanese)
 */
public class DfClsTopGroupCommentDisp {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _groupComment; // null allowed (not required)

    protected final DfDocumentTextResolver _documentTextResolver = new DfDocumentTextResolver();

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfClsTopGroupCommentDisp(String groupComment) {
        _groupComment = groupComment;
    }

    // ===================================================================================
    //                                                                     Comment Display
    //                                                                     ===============
    public String buildGroupCommentDisp() {
        return doBuildGroupCommentDisp();
    }

    protected String doBuildGroupCommentDisp() {
        if (_groupComment == null) {
            return "";
        }
        return Srl.replace(_groupComment, "\n", ""); // basically one line
    }

    // ===================================================================================
    //                                                                         for JavaDoc
    //                                                                         ===========
    public String buildGroupCommentForJavaDoc() {
        return doBuildGroupCommentForJavaDoc("    ");
    }

    public String buildGroupCommentForJavaDocNest() {
        return doBuildGroupCommentForJavaDoc("        ");
    }

    protected String doBuildGroupCommentForJavaDoc(String indent) {
        return _documentTextResolver.resolveJavaDocContent(buildGroupCommentDisp(), indent);
    }

    // ===================================================================================
    //                                                                          for dfprop
    //                                                                          ==========
    public String buildGroupCommentForDfpropMap() {
        return _documentTextResolver.resolveDfpropMapContent(buildGroupCommentDisp());
    }
}
