/*
 * Copyright 2014-2026 the original author or authors.
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
package org.dbflute.logic.doc.schemahtml;

import org.apache.torque.engine.database.model.Database;
import org.apache.torque.engine.database.model.ForeignKey;
import org.apache.torque.engine.database.model.Table;
import org.dbflute.DfBuildProperties;
import org.dbflute.properties.DfDatabaseProperties;
import org.dbflute.properties.DfDocumentProperties;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.9.2 (2009/02/12 Thursday)
 */
public class DfSchemaHtmlBuilder {

    // ===================================================================================
    //                                                                    Table List Title
    //                                                                    ================
    public String buildTableListTitle(Database database) {
        final StringBuilder sb = new StringBuilder();
        sb.append("mainSchema=").append(getDatabaseProperties().getDatabaseSchema().getCatalogSchema());
        sb.append(", tableCount=").append(database.getTableList().size());
        final long columnCountAll = database.getTableList().stream().flatMap(table -> {
            return table.getColumnList().stream();
        }).count();
        sb.append(", columnCountAll=").append(columnCountAll);
        return " title=\"" + resolveTitle(sb.toString()) + "\"";
    }

    // ===================================================================================
    //                                                                         Table Title
    //                                                                         ===========
    public String buildTableTitle(Table table) {
        final StringBuilder sb = new StringBuilder();
        sb.append("type=").append(table.getType());
        if (table.isAdditionalSchema()) {
            sb.append(", schema=").append(table.getDocumentSchema());
        }
        sb.append(", primaryKey={").append(table.getPrimaryKeyNameCommaString()).append("}");
        sb.append(", nameLength=").append(table.getTableDbName().length());
        sb.append(", columnCount=").append(table.getColumns().length);
        if (table.isDeprecatedTable()) {
            sb.append(", @deprecated reason=").append(table.getDeprecatedTableReasonComment());
        }
        return " title=\"" + resolveTitle(sb.toString()) + "\"";
    }

    // ===================================================================================
    //                                                                  Related Table Link
    //                                                                  ==================
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
        if (table.isDeprecatedTable()) {
            sb.append(table.getDeprecatedTableRelationTagPrefixForSchemaHtml());
        }
        sb.append(contentName);
        if (table.isDeprecatedTable()) {
            sb.append(table.getDeprecatedTableRelationTagSuffixForSchemaHtml());
        }
        sb.append("</a>");
        return sb.toString();
    }

    // ===================================================================================
    //                                                                        Escape Logic
    //                                                                        ============
    protected String resolveTitle(String title) {
        return getDocumentProperties().resolveSchemaHtmlTagAttr(title);
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    protected DfDatabaseProperties getDatabaseProperties() {
        return getProperties().getDatabaseProperties();
    }

    protected DfDocumentProperties getDocumentProperties() {
        return getProperties().getDocumentProperties();
    }
}
