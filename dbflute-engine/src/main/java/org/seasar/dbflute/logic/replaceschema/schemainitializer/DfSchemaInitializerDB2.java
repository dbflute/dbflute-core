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
package org.seasar.dbflute.logic.replaceschema.schemainitializer;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.seasar.dbflute.helper.jdbc.facade.DfJdbcFacade;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfTableMeta;

/**
 * The schema initializer for DB2.
 * @author jflute
 * @since 0.7.9 (2008/08/24 Monday)
 */
public class DfSchemaInitializerDB2 extends DfSchemaInitializerJdbc {

    // ===================================================================================
    //                                                                    Drop Foreign Key
    //                                                                    ================
    @Override
    protected boolean isSkipDropForeignKey(DfTableMeta tableMetaInfo) {
        return tableMetaInfo.isTableTypeAlias();
    }

    // ===================================================================================
    //                                                                          Drop Table
    //                                                                          ==========
    @Override
    protected void setupDropTable(StringBuilder sb, DfTableMeta metaInfo) {
        if (metaInfo.isTableTypeAlias()) {
            final String tableName = metaInfo.getTableSqlName();
            sb.append("drop alias ").append(tableName);
        } else {
            super.setupDropTable(sb, metaInfo);
        }
    }

    // ===================================================================================
    //                                                                       Drop Sequence
    //                                                                       =============
    @Override
    protected void dropSequence(Connection conn, List<DfTableMeta> tableMetaInfoList) {
        if (!_unifiedSchema.hasSchema()) {
            return;
        }
        final String schema = _unifiedSchema.getPureSchema();
        final List<String> sequenceNameList = new ArrayList<String>();
        final DfJdbcFacade jdbcFacade = new DfJdbcFacade(conn);
        final String sequenceColumnName = "sequence_name";
        final StringBuilder sb = new StringBuilder();
        sb.append("select SEQNAME as ").append(sequenceColumnName).append(" from SYSCAT.SEQUENCES");
        sb.append(" where SEQSCHEMA = '").append(schema).append("'");
        final String sql = sb.toString();
        final List<String> sequenceColumnList = Arrays.asList(sequenceColumnName);
        final List<Map<String, String>> resultList = jdbcFacade.selectStringList(sql, sequenceColumnList);
        for (Map<String, String> recordMap : resultList) {
            sequenceNameList.add(recordMap.get(sequenceColumnName));
        }
        for (String sequenceName : sequenceNameList) {
            if (isSequenceExcept(sequenceName)) {
                continue;
            }
            final String dropSequenceSql = "drop sequence " + schema + "." + sequenceName;
            logReplaceSql(dropSequenceSql);
            jdbcFacade.execute(dropSequenceSql);
        }
    }
}