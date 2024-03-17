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
package org.dbflute.properties.assistant.classification.element.attribute;

import org.dbflute.DfBuildProperties;
import org.dbflute.properties.DfDocumentProperties;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.2.5 split from DfClassificationElement (2021/07/03 Saturday at roppongi japanese)
 */
public class DfClsElementApplicationComment {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _alias; // null allowed, empty allowed
    protected final String _commentDisp; // null allowed, empty allowed

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfClsElementApplicationComment(String alias, String commentDisp) {
        _alias = alias;
        _commentDisp = commentDisp;
    }

    // ===================================================================================
    //                                                                       Build Comment
    //                                                                       =============
    public String buildApplicationCommentForJavaDoc() {
        return resolveTextForJavaDoc(buildApplicationComment(), "    ");
    }

    public String buildApplicationCommentForSchemaHtml() {
        return resolveTextForSchemaHtml(buildApplicationComment());
    }

    protected String buildApplicationComment() {
        final StringBuilder sb = new StringBuilder();
        if (Srl.is_NotNull_and_NotTrimmedEmpty(_alias)) {
            sb.append(_alias);
        }
        if (Srl.is_NotNull_and_NotTrimmedEmpty(_commentDisp)) {
            if (sb.length() > 0) {
                sb.append(": ");
            }
            sb.append(toOneLiner(_commentDisp));
        }
        return sb.toString();
    }

    protected String toOneLiner(final String comment) {
        return Srl.replace(comment, "\n", " "); // just in case (basically one line)
    }

    // ===================================================================================
    //                                                                         Escape Text
    //                                                                         ===========
    protected String resolveTextForJavaDoc(String comment, String indent) {
        return getDocumentProperties().resolveJavaDocContent(comment, indent);
    }

    protected String resolveTextForSchemaHtml(String comment) {
        return getDocumentProperties().resolveSchemaHtmlContent(comment);
    }

    protected DfDocumentProperties getDocumentProperties() {
        return DfBuildProperties.getInstance().getDocumentProperties();
    }
}
