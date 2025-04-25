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
package org.dbflute.logic.replaceschema.loaddata.base;

import org.apache.torque.engine.database.model.UnifiedSchema;
import org.dbflute.DfBuildProperties;
import org.dbflute.properties.DfLittleAdjustmentProperties;

/**
 * @author jflute
 * @since 1.2.9 (2025/04/24 Thursday at ichihara)
 */
public class DfLoadedSchemaTable {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final UnifiedSchema _unifiedSchema; // not null (basically main schema)
    protected final String _tableDbName; // not null (basically same as on-file name)
    protected final String _onfileTableName; // not null (basically same as table DB name)

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfLoadedSchemaTable(UnifiedSchema unifiedSchema, String tableDbName, String onfileTableName) {
        _unifiedSchema = unifiedSchema;
        _tableDbName = tableDbName;
        _onfileTableName = onfileTableName;
    }

    // ===================================================================================
    //                                                                      SQL Expression
    //                                                                      ==============
    public String buildTableSqlName() { // may have schema prefix, and quoted
        return buildSqlSchemaPrefix() + quoteTableNameIfNeeds(_tableDbName);
    }

    protected String buildSqlSchemaPrefix() {
        if (_unifiedSchema.isMainSchema()) {
            return ""; // executable without schema
        } else {
            return _unifiedSchema.getCatalogSchema() + ".";
        }
    }

    protected String quoteTableNameIfNeeds(String tableDbName) {
        final DfLittleAdjustmentProperties prop = DfBuildProperties.getInstance().getLittleAdjustmentProperties();
        return prop.quoteTableNameIfNeedsDirectUse(tableDbName);
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        if (_tableDbName.equals(_onfileTableName)) { // no schema specified, basically here
            return "{" + _unifiedSchema + "." + _tableDbName + "}";
        } else { // schema specified on file
            return "{" + _unifiedSchema + "." + _tableDbName + " :: " + _onfileTableName + "}";
        }
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public UnifiedSchema getUnifiedSchema() {
        return _unifiedSchema;
    }

    public String getTableDbName() {
        return _tableDbName;
    }

    public String getOnfileTableName() {
        return _onfileTableName;
    }
}
