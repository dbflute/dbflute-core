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
import org.seasar.dbflute.logic.jdbc.metadata.info.DfProcedureMeta;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfTableMeta;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfSchemaInitializerH2 extends DfSchemaInitializerJdbc {

    // ===================================================================================
    //                                                                       Drop Sequence
    //                                                                       =============
    @Override
    protected void dropSequence(Connection conn, List<DfTableMeta> tableMetaInfoList) {
        final String catalog = _unifiedSchema.existsPureCatalog() ? _unifiedSchema.getPureCatalog() : null;
        final String schema = _unifiedSchema.getPureSchema();
        final List<String> sequenceNameList = new ArrayList<String>();
        final DfJdbcFacade jdbcFacade = new DfJdbcFacade(conn);
        final String sequenceColumnName = "sequence_name";
        final StringBuilder sb = new StringBuilder();
        sb.append("select ").append(sequenceColumnName).append(" from information_schema.sequences");
        sb.append(" where ");
        if (Srl.is_NotNull_and_NotTrimmedEmpty(catalog)) {
            sb.append("sequence_catalog = '").append(catalog).append("'").append(" and ");
        }
        sb.append("sequence_schema = '").append(schema).append("'");
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
            final String sequenceSqlName = _unifiedSchema.buildSqlName(sequenceName);
            final String dropSequenceSql = "drop sequence " + sequenceSqlName;
            logReplaceSql(dropSequenceSql);
            jdbcFacade.execute(dropSequenceSql);
        }
    }

    // ===================================================================================
    //                                                                      Drop Procedure
    //                                                                      ==============
    @Override
    protected DfDropProcedureByJdbcCallback createDropProcedureByJdbcCallback() {
        return new DfDropProcedureByJdbcDefaultCallback() {
            @Override
            public String buildDropFunctionSql(DfProcedureMeta procedureMeta) {
                return "drop alias " + buildProcedureSqlName(procedureMeta);
            }
        };
    }

    @Override
    protected boolean isDropFunctionFirst() {
        return true;
    }
}