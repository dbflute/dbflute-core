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
package org.dbflute.logic.doc.schemahtml.alias;

import java.util.List;

import org.apache.torque.engine.database.model.Table;
import org.dbflute.DfBuildProperties;
import org.dbflute.properties.DfDocumentProperties;

/**
 * @author jflute
 * @since 1.3.1 (2012/12/23 Tuesday at ichihara)
 */
public class DfSchemaHtmlAliasEverywhereDeterminer {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static Boolean cachedEverywhere; // for many call

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final List<Table> _tableList; // not null, not empty

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfSchemaHtmlAliasEverywhereDeterminer(List<Table> tableList) {
        _tableList = tableList;
    }

    // ===================================================================================
    //                                                                           Determine
    //                                                                           =========
    public boolean determineEverywhere() {
        if (cachedEverywhere != null) {
            return cachedEverywhere;
        }
        final boolean everywhere = doDetermineEverywhere();
        cachedEverywhere = everywhere;
        return cachedEverywhere;
    }

    protected boolean doDetermineEverywhere() {
        if (isAliasDelimiterInDbCommentValid()) {
            return true;
        }
        if (hasAliasOnDecommentAtLeastOneInAll()) {
            return true;
        }
        return false;
    }

    protected boolean isAliasDelimiterInDbCommentValid() {
        return getProperties().getDocumentProperties().isAliasDelimiterInDbCommentValid();
    }

    protected boolean hasAliasOnDecommentAtLeastOneInAll() {
        return _tableList.stream().anyMatch(table -> {
            if (table.hasAliasOnDecomment()) {
                return true;
            }
            return table.getColumnList().stream().anyMatch(col -> {
                return col.hasAliasOnDecomment();
            });
        });
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfDocumentProperties getDocumentProperties() {
        return getProperties().getDocumentProperties();
    }

    protected DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }
}
