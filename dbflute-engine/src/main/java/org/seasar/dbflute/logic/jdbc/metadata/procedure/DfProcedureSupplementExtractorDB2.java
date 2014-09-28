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
public class DfProcedureSupplementExtractorDB2 extends DfProcedureSupplementExtractorBase {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfProcedureSupplementExtractorDB2(DataSource dataSource) {
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
            final String name = sourceMap.get("ROUTINENAME");
            if (name == null) { // just in case
                continue;
            }
            final DfProcedureSourceInfo sourceInfo = new DfProcedureSourceInfo();

            final String body = sourceMap.get("TEXT"); // full text (null allowed)
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
        // the table 'ROUTINES' on DB2 does not have catalog of procedures
        sb.append("select * from SYSCAT.ROUTINES");
        sb.append(" where ROUTINESCHEMA = '").append(unifiedSchema.getPureSchema()).append("'");
        sb.append(" order by ROUTINENAME");
        String sql = sb.toString();
        final List<String> columnList = new ArrayList<String>();
        columnList.add("ROUTINESCHEMA");
        columnList.add("ROUTINENAME");
        columnList.add("TEXT"); // full text (contains parameter definition)
        return selectStringList(sql, columnList);
    }
}
