/*
 * Copyright 2014-2025 the original author or authors.
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

import org.apache.torque.engine.database.model.Column;
import org.apache.torque.engine.database.model.Table;

/**
 * @author jflute
 * @since 1.3.1 (2012/12/26 Friday at ichihara)
 */
public class DfSchemaHtmlAliasBasicFacade {

    public boolean needsTableListTableAlias(List<Table> tableList) {
        final DfSchemaHtmlAliasEverywhereDeterminer determiner = createEverywhereDeterminer(tableList);
        if (determiner.determineEverywhere()) {
            return true;
        }
        return tableList.stream().anyMatch(table -> table.hasAlias());
    }

    public boolean needsTableDetailColumnAlias(List<Table> tableList, List<Column> columnList) {
        final DfSchemaHtmlAliasEverywhereDeterminer determiner = createEverywhereDeterminer(tableList);
        if (determiner.determineEverywhere()) {
            return true;
        }
        return columnList.stream().anyMatch(column -> column.hasAlias());
    }

    protected DfSchemaHtmlAliasEverywhereDeterminer createEverywhereDeterminer(List<Table> tableList) {
        return new DfSchemaHtmlAliasEverywhereDeterminer(tableList);
    }
}
