/*
 * Copyright 2004-2014 the Seasar Foundation and the Others.
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
package org.seasar.dbflute.logic.doc.schemahtml;

import org.apache.torque.engine.database.model.ForeignKey;
import org.apache.torque.engine.database.model.Table;
import org.seasar.dbflute.properties.DfDocumentProperties;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.9.2 (2009/02/12 Thursday)
 */
public class DfSchemaHtmlBuilder {

    protected DfDocumentProperties _documentProperties;

    public DfSchemaHtmlBuilder(DfDocumentProperties documentProperties) {
        _documentProperties = documentProperties;
    }

    public String buildRelatedTableLink(ForeignKey fk, Table table, String delimiter) {
        final String tableDispName = table.getTableDispName();
        final String tableId = table.getTableIdForSchemaHtml();
        final StringBuilder sb = new StringBuilder();
        sb.append(delimiter);
        final String baseTitle = fk.getName();
        final String comment = fk.getComment();
        final String contentName;
        if (fk.isAdditionalForeignKey()) {
            final String addtionalBaseTitle = baseTitle;
            final String fixedCondition = fk.getFixedCondition();
            final StringBuilder titleSb = new StringBuilder();
            titleSb.append(addtionalBaseTitle);
            boolean comma = false;
            if (fk.hasFixedCondition()) {
                titleSb.append(comma ? ", " : ": ");
                titleSb.append("fixedCondition=\"").append(fixedCondition).append("\"");
                comma = true;
            }
            if (Srl.is_NotNull_and_NotTrimmedEmpty(comment)) {
                titleSb.append(comma ? ", " : ": ");
                titleSb.append("comment=").append(comment);
                comma = true;
            }
            final String title = resolveTitle(titleSb.toString());
            sb.append("<a href=\"#").append(tableId);
            sb.append("\" class=\"additionalfk\" title=\"").append(title).append("\">");
            contentName = tableDispName + (fk.hasFixedSuffix() ? "(" + fk.getFixedSuffix() + ")" : "");
        } else {
            final StringBuilder titleSb = new StringBuilder();
            titleSb.append(baseTitle);
            if (Srl.is_NotNull_and_NotTrimmedEmpty(comment)) {
                titleSb.append(": comment=").append(comment);
            }
            final String title = resolveTitle(titleSb.toString());
            sb.append("<a href=\"#").append(tableId).append("\" title=\"").append(title).append("\">");
            contentName = tableDispName;
        }
        sb.append(contentName).append("</a>");
        return sb.toString();
    }

    protected String resolveTitle(String title) {
        return _documentProperties.resolveAttributeForSchemaHtml(title);
    }
}
