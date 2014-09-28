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
package org.seasar.dbflute.logic.jdbc.metadata.procedure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.torque.engine.database.model.UnifiedSchema;
import org.seasar.dbflute.helper.StringKeyMap;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfProcedureSourceInfo;

/**
 * @author jflute
 * @since 0.9.9.7F (2012/08/22 Wednesday)
 */
public class DfProcedureSupplementExtractorH2 extends DfProcedureSupplementExtractorBase {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfProcedureSupplementExtractorH2(DataSource dataSource) {
        super(dataSource);
    }

    // ===================================================================================
    //                                                                         Source Info
    //                                                                         ===========
    @Override
    protected Map<String, DfProcedureSourceInfo> doExtractProcedureSourceInfo(UnifiedSchema unifiedSchema) {
        final List<Map<String, String>> sourceList = selectProcedureSourceList(unifiedSchema);
        final Map<String, DfProcedureSourceInfo> resultMap = StringKeyMap.createAsFlexibleOrdered();
        for (Map<String, String> sourceMap : sourceList) {
            final String name = sourceMap.get("ALIAS_NAME");
            if (name == null) { // just in case
                continue;
            }
            final DfProcedureSourceInfo sourceInfo = new DfProcedureSourceInfo();

            final String body = sourceMap.get("SOURCE"); // full text
            if (body == null) {
                continue;
            }
            sourceInfo.setSourceCode(body);
            sourceInfo.setSourceLine(calculateSourceLine(body));
            sourceInfo.setSourceSize(calculateSourceSize(body));

            resultMap.put(name, sourceInfo);
        }
        return resultMap;
    }

    protected List<Map<String, String>> selectProcedureSourceList(UnifiedSchema unifiedSchema) {
        StringBuilder sb = new StringBuilder();
        sb.append("select * from INFORMATION_SCHEMA.FUNCTION_ALIASES");
        sb.append(" where ALIAS_CATALOG = '").append(unifiedSchema.getPureCatalog()).append("'");
        sb.append(" and ALIAS_SCHEMA = '").append(unifiedSchema.getPureSchema()).append("'");
        sb.append(" order by ALIAS_NAME");
        String sql = sb.toString();
        final List<String> columnList = new ArrayList<String>();
        columnList.add("ALIAS_CATALOG");
        columnList.add("ALIAS_SCHEMA");
        columnList.add("ALIAS_NAME");
        columnList.add("SOURCE"); // full text (contains parameter definition)
        return selectStringList(sql, columnList);
    }
}
