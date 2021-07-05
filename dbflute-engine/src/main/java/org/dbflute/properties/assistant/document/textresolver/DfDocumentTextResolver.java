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
package org.dbflute.properties.assistant.document.textresolver;

import org.dbflute.DfBuildProperties;
import org.dbflute.helper.dfmap.DfMapStyle;
import org.dbflute.logic.generate.language.grammar.DfLanguageGrammar;
import org.dbflute.properties.DfBasicProperties;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.1.8 (2018/5/13 Sunday at bay maihama)
 */
public class DfDocumentTextResolver {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    // here fixed line separator (simplified)
    protected static final String BASIC_LINE_SEPARATOR = "\n";
    protected static final String SPECIAL_LINE_SEPARATOR = "&#xa;";

    // ===================================================================================
    //                                                                             JavaDoc
    //                                                                             =======
    public String resolveJavaDocContent(String comment, String indent) {
        return doResolveJavaDocContent(comment, indent, false);
    }

    public String resolveJavaDocContentIndentDirectly(String comment, String indent) {
        return doResolveJavaDocContent(comment, indent, true);
    }

    protected String doResolveJavaDocContent(String comment, String indent, boolean directIndent) {
        if (comment == null || comment.trim().length() == 0) {
            return null;
        }
        String work = comment;
        final DfBasicProperties basicProp = getBasicProperties();
        final DfLanguageGrammar grammar = basicProp.getLanguageDependency().getLanguageGrammar();
        work = grammar.escapeJavaDocString(work);
        work = removeCR(work);
        final String sourceCodeLineSeparator = basicProp.getSourceCodeLineSeparator();
        final String javaDocLineSeparator;
        if (directIndent) {
            javaDocLineSeparator = grammar.buildJavaDocLineAndIndentDirectly(sourceCodeLineSeparator, indent);
        } else {
            javaDocLineSeparator = grammar.buildJavaDocLineAndIndent(sourceCodeLineSeparator, indent);
        }
        if (work.contains(BASIC_LINE_SEPARATOR)) {
            work = work.replaceAll(BASIC_LINE_SEPARATOR, javaDocLineSeparator);
        }
        if (work.contains(SPECIAL_LINE_SEPARATOR)) {
            work = work.replaceAll(SPECIAL_LINE_SEPARATOR, javaDocLineSeparator);
        }
        return work;
    }

    // ===================================================================================
    //                                                                          SchemaHTML
    //                                                                          ==========
    // contains HistoryHTML, PropertiesHTML and so on... (means generated HTML)
    public String resolveSchemaHtmlContent(String text) {
        if (text == null || text.trim().length() == 0) {
            return null;
        }
        // escape
        text = Srl.replace(text, "&", "&amp;"); // should be first, to keep already-escaped word
        text = Srl.replace(text, "<", "&lt;");
        text = Srl.replace(text, ">", "&gt;");
        text = Srl.replace(text, " ", "&nbsp;");

        // line separator (should be after escaping)
        text = removeCR(text);
        final String htmlLineSeparator = "<br>";
        if (text.contains(BASIC_LINE_SEPARATOR)) {
            text = text.replaceAll(BASIC_LINE_SEPARATOR, htmlLineSeparator);
        }
        if (text.contains(SPECIAL_LINE_SEPARATOR)) {
            text = text.replaceAll(SPECIAL_LINE_SEPARATOR, htmlLineSeparator);
        }
        return text;
    }

    public String resolveSchemaHtmlPreText(String text) {
        if (text == null || text.trim().length() == 0) {
            return null;
        }
        // escape
        text = Srl.replace(text, "&", "&amp;"); // should be first, to keep already-escaped word
        text = Srl.replace(text, "<", "&lt;");
        text = Srl.replace(text, ">", "&gt;");
        // unneeded because of "pre" tag
        //text = Srl.replace(text, " ", "&nbsp;");

        // line separator
        text = removeCR(text);
        return text;
    }

    public String resolveSchemaHtmlTagAttr(String text) {
        if (text == null || text.trim().length() == 0) {
            return null;
        }
        // escape
        text = Srl.replace(text, "<", "&lt;");
        text = Srl.replace(text, ">", "&gt;");
        text = Srl.replace(text, "\"", "&quot;");

        // line separator
        text = removeCR(text);
        return text;
    }

    // ===================================================================================
    //                                                                     SimpleLine HTML
    //                                                                     ===============
    public String resolveSimpleLineHtmlContent(String text) {
        if (text == null || text.trim().length() == 0) {
            return null;
        }
        // escape
        text = Srl.replace(text, "<", "&lt;");
        text = Srl.replace(text, ">", "&gt;");
        return text;
    }

    // ===================================================================================
    //                                                                         DBMeta code
    //                                                                         ===========
    // is it document? (for compatible)
    public String resolveDBMetaCodeSettingText(String text) { // C# same as Java
        if (text == null || text.trim().length() == 0) {
            return null;
        }
        text = removeCR(text);
        text = Srl.replace(text, "\\", "\\\\"); // escape escape character
        text = Srl.replace(text, "\"", "\\\""); // escape double quotation

        final String literalLineSeparator = "\\\\n";
        if (text.contains(BASIC_LINE_SEPARATOR)) {
            text = text.replaceAll(BASIC_LINE_SEPARATOR, literalLineSeparator);
        }
        if (text.contains(SPECIAL_LINE_SEPARATOR)) {
            text = text.replaceAll(SPECIAL_LINE_SEPARATOR, literalLineSeparator);
        }
        return text;
    }

    // ===================================================================================
    //                                                                     dfprop Settings
    //                                                                     ===============
    public String resolveDfpropMapContent(String text) {
        if (text == null || text.trim().length() == 0) {
            return null;
        }
        return new DfMapStyle().escapeControlMarkAsMap(text);
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    public DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    protected DfBasicProperties getBasicProperties() {
        return getProperties().getBasicProperties();
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String removeCR(String str) {
        return str.replaceAll("\r", "");
    }
}
