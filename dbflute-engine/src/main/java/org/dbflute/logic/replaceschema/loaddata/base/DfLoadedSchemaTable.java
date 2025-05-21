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
    protected final UnifiedSchema _unifiedSchema; // not null (basically main schema but may be other schema)
    protected final String _tablePureName; // not null (basically same as on-file name)
    protected final String _onfileTableName; // not null (basically same as table DB name)

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfLoadedSchemaTable(UnifiedSchema unifiedSchema, String tablePureName, String onfileTableName) {
        _unifiedSchema = unifiedSchema;
        _tablePureName = tablePureName;
        _onfileTableName = onfileTableName;
    }

    // ===================================================================================
    //                                                                      SQL Expression
    //                                                                      ==============
    public String buildTableSqlName() { // may have schema prefix, and quoted
        final String resolvedPureName = quoteTablePureNameIfNeeds(_tablePureName);
        return _unifiedSchema.buildSqlName(resolvedPureName);
    }

    protected String quoteTablePureNameIfNeeds(String tablePureName) {
        final DfLittleAdjustmentProperties prop = DfBuildProperties.getInstance().getLittleAdjustmentProperties();
        return prop.quoteTableNameIfNeedsDirectUse(tablePureName);
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        if (_tablePureName.equals(_onfileTableName)) { // no schema specified, basically here
            return "{" + _unifiedSchema + "." + _tablePureName + "}";
        } else { // schema specified on file
            return "{" + _unifiedSchema + "." + _tablePureName + " :: " + _onfileTableName + "}";
        }
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public UnifiedSchema getUnifiedSchema() {
        return _unifiedSchema;
    }

    public String getTablePureName() {
        return _tablePureName;
    }

    public String getOnfileTableName() {
        return _onfileTableName;
    }
}
