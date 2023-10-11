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
package org.dbflute.logic.doc.arrqy;

import org.dbflute.properties.assistant.document.textresolver.DfDocumentTextResolver;

/**
 * @author jflute
 * @since 1.1.9 (2018/12/31 Monday)
 */
public class DfArrangeQueryMethod {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _tableDbName; // not null
    protected final String _methodName; // not null
    // no argument for now
    //protected List<String> _argumentList;
    protected String _title; // one liner, null allowed
    protected String _description; // null allowed

    protected final DfDocumentTextResolver _documentTextResolver = new DfDocumentTextResolver();

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfArrangeQueryMethod(String tableDbName, String methodName) {
        _tableDbName = tableDbName;
        _methodName = methodName;
    }

    // ===================================================================================
    //                                                                     HTML Expression
    //                                                                     ===============
    public String buildTitleExpForSchemaHtml() {
        final StringBuilder sb = new StringBuilder();
        sb.append(_methodName).append("()");
        if (_title != null) {
            // #thinking jflute should HTML-escape? (JavaDoc is HTML, however ...) (2018/12/31)
            //final String resolvedTitle = _documentTextResolver.resolveSchemaHtmlContent(_title);
            sb.append(" <span class=\"commentdesc\">// ").append(_title).append("</span>");
        }
        return sb.toString();
    }

    public String buildDescriptionExpForSchemaHtml() {
        return "&nbsp;"; // #hope jflute from JavaDoc (2018/12/31)
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getTableDbName() {
        return _tableDbName;
    }

    public String getMethodName() {
        return _methodName;
    }

    public String getTitle() {
        return _title;
    }

    public void setTitle(String title) {
        _title = title;
    }

    public String getDescription() {
        return _description;
    }

    public void setDescription(String description) {
        _description = description;
    }
}