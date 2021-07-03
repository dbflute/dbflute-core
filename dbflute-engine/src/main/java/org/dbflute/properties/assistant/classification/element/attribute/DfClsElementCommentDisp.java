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
package org.dbflute.properties.assistant.classification.element.attribute;

import org.dbflute.DfBuildProperties;
import org.dbflute.properties.DfDocumentProperties;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.2.5 split from DfClassificationElement (2021/07/03 Saturday at roppongi japanese)
 */
public class DfClsElementCommentDisp {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _comment; // null allowed
    protected final String _deprecatedComment; // not null, empty allowed

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfClsElementCommentDisp(String comment, String deprecatedComment) {
        _comment = comment;
        _deprecatedComment = deprecatedComment;
    }

    // ===================================================================================
    //                                                                       Basic Display
    //                                                                       =============
    public String buildCommentDisp() { // empty allowed if no comment
        final StringBuilder sb = new StringBuilder();
        sb.append(_comment != null ? _comment : "");
        if (!_deprecatedComment.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append("(deprecated: ").append(_deprecatedComment).append(")");
        }
        final String disp = sb.toString();
        return Srl.replace(disp, "\n", ""); // basically one line
    }

    // ===================================================================================
    //                                                                         for JavaDoc
    //                                                                         ===========
    public String buildCommentForJavaDoc() {
        return doBuildCommentForJavaDoc("    "); // 4 spaces indent (for Java, C# root methods)
    }

    public String buildCommentForJavaDocNest() {
        return doBuildCommentForJavaDoc("        "); // 8 spaces indent (for Java, C# nested methods)
    }

    protected String doBuildCommentForJavaDoc(String indent) {
        return resolveTextForJavaDoc(buildCommentDisp(), indent);
    }

    // ===================================================================================
    //                                                                      for SchemaHtml
    //                                                                      ==============
    public String buildCommentForSchemaHtml() {
        return resolveTextForSchemaHtml(buildCommentDisp());
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
