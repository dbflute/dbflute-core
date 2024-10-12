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
package org.dbflute.properties.assistant.document.tableorder;

import java.util.Comparator;

import org.apache.torque.engine.database.model.Table;

/**
 * @author jflute
 * @since 1.2.9 split from DfDocumentProperties (2024/10/12 Saturday at nakamguro)
 */
public class DfDocTableOrder {

    public Comparator<Table> createTableDisplayOrderBy() {
        return new Comparator<Table>() {
            public int compare(Table table1, Table table2) {
                // = = = =
                // Schema
                // = = = =
                // The main schema has priority
                {
                    final boolean mainSchema1 = table1.isMainSchema();
                    final boolean mainSchema2 = table2.isMainSchema();
                    if (mainSchema1 != mainSchema2) {
                        if (mainSchema1) {
                            return -1;
                        }
                        if (mainSchema2) {
                            return 1;
                        }
                        // unreachable
                    }
                    final String schema1 = table1.getDocumentSchema();
                    final String schema2 = table2.getDocumentSchema();
                    if (schema1 != null && schema2 != null && !schema1.equals(schema2)) {
                        return schema1.compareTo(schema2);
                    } else if (schema1 == null && schema2 != null) {
                        return 1; // nulls last
                    } else if (schema1 != null && schema2 == null) {
                        return -1; // nulls last
                    }
                    // passed: when both are NOT main and are same schema
                }

                // = = =
                // Type
                // = = =
                {
                    final String type1 = table1.getType();
                    final String type2 = table2.getType();
                    if (!type1.equals(type2)) {
                        // The table type has priority
                        if (table1.isTypeTable()) {
                            return -1;
                        }
                        if (table2.isTypeTable()) {
                            return 1;
                        }
                        return type1.compareTo(type2);
                    }
                }

                // = = =
                // Table
                // = = =
                final String name1 = table1.getName();
                final String name2 = table2.getName();
                return name1.compareTo(name2);
            }
        };
    }
}
