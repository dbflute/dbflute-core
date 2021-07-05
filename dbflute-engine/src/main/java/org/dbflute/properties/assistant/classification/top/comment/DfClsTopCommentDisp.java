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
package org.dbflute.properties.assistant.classification.top.comment;

import org.dbflute.properties.assistant.document.textresolver.DfDocumentTextResolver;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.2.5 split from DfClassificationTop (2021/07/04 Sunday at roppongi japanese)
 */
public class DfClsTopCommentDisp {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _topComment; // not null (required)
    protected final boolean _useDocumentOnly;

    protected final DfDocumentTextResolver _documentTextResolver = new DfDocumentTextResolver();

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfClsTopCommentDisp(String topComment, boolean useDocumentOnly) {
        _topComment = topComment;
        _useDocumentOnly = useDocumentOnly;
    }

    // ===================================================================================
    //                                                                     Comment Display
    //                                                                     ===============
    public String buildTopCommentDisp() {
        return doBuildTopCommentDisp();
    }

    protected String doBuildTopCommentDisp() {
        if (_topComment == null) {
            return "";
        }
        final String comment;
        if (_useDocumentOnly) {
            comment = _topComment + " (document only)";
        } else {
            comment = _topComment;
        }
        return Srl.replace(comment, "\n", ""); // basically one line
    }

    // ===================================================================================
    //                                                                         for JavaDoc
    //                                                                         ===========
    public String buildTopCommentForJavaDoc() {
        return doBuildTopCommentForJavaDoc("    "); // basically indent unused
    }

    public String buildTopCommentForJavaDocNest() {
        return doBuildTopCommentForJavaDoc("        "); // basically indent unused
    }

    protected String doBuildTopCommentForJavaDoc(String indent) {
        return _documentTextResolver.resolveJavaDocContent(buildTopCommentDisp(), indent);
    }

    // ===================================================================================
    //                                                                      for SchemaHtml
    //                                                                      ==============
    public String buildTopCommentForSchemaHtml() {
        return _documentTextResolver.resolveSchemaHtmlContent(buildTopCommentDisp());
    }

    // ===================================================================================
    //                                                                          for dfprop
    //                                                                          ==========
    public String buildTopCommentPlainlyForDfpropMap() {
        return _documentTextResolver.resolveDfpropMapContent(_topComment != null ? _topComment : "");
    }
}
