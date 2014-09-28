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
package org.seasar.dbflute.logic.jdbc.metadata.sequence;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.engine.database.model.UnifiedSchema;
import org.seasar.dbflute.helper.StringKeyMap;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfSequenceMeta;
import org.seasar.dbflute.util.DfCollectionUtil;

/**
 * @author jflute
 * @since 0.9.6.4 (2010/01/16 Saturday)
 */
public class DfSequenceExtractorDB2 extends DfSequenceExtractorBase {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log _log = LogFactory.getLog(DfSequenceExtractorDB2.class);

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfSequenceExtractorDB2(DataSource dataSource, List<UnifiedSchema> unifiedSchemaList) {
        super(dataSource, unifiedSchemaList);
    }

    // ===================================================================================
    //                                                                        Sequence Map
    //                                                                        ============
    protected Map<String, DfSequenceMeta> doGetSequenceMap() {
        _log.info("...Loading sequence informations");
        final Map<String, DfSequenceMeta> resultMap = StringKeyMap.createAsFlexibleOrdered();
        final String sql = buildMetaSelectSql();
        if (sql == null) {
            return DfCollectionUtil.emptyMap();
        }
        final List<String> columnList = new ArrayList<String>();
        columnList.add("SEQSCHEMA");
        columnList.add("SEQNAME");
        columnList.add("MINVALUE");
        columnList.add("MAXVALUE");
        columnList.add("INCREMENT");
        final List<Map<String, String>> resultList = selectStringList(sql, columnList);
        final StringBuilder logSb = new StringBuilder();
        logSb.append(ln()).append("[SEQUENCE]");
        for (Map<String, String> recordMap : resultList) {
            final DfSequenceMeta info = new DfSequenceMeta();
            String sequenceSchema = recordMap.get("SEQSCHEMA");

            // trim because DB2 returns "char(8)-owner"
            sequenceSchema = sequenceSchema != null ? sequenceSchema.trim() : null;

            info.setSequenceSchema(sequenceSchema);
            final String sequenceName = recordMap.get("SEQNAME");
            info.setSequenceName(sequenceName);
            final String minValue = recordMap.get("MINVALUE");
            info.setMinimumValue(minValue != null ? new BigDecimal(minValue) : null);
            final String maxValue = recordMap.get("MAXVALUE");
            info.setMaximumValue(maxValue != null ? new BigDecimal(maxValue) : null);
            final String incrementSize = recordMap.get("INCREMENT");
            info.setIncrementSize(incrementSize != null ? Integer.valueOf(incrementSize) : null);

            // DB2 does not return sequence catalog so catalog argument is null
            final String key = buildSequenceMapKey(null, sequenceSchema, sequenceName);

            resultMap.put(key, info);
            logSb.append(ln()).append(" ").append(key).append(" = ").append(info.toString());
        }
        _log.info(logSb.toString());
        return resultMap;
    }

    protected String buildMetaSelectSql() {
        final String schemaCondition;
        if (!_unifiedSchemaList.isEmpty()) {
            final StringBuilder sb = new StringBuilder();
            for (UnifiedSchema unifiedSchema : _unifiedSchemaList) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append("'").append(unifiedSchema.getPureSchema()).append("'");
            }
            schemaCondition = sb.toString();
        } else {
            return null;
        }
        // the table 'SEQUENCES' on DB2 does not have catalog of sequence
        return "select * from SYSCAT.SEQUENCES where SEQSCHEMA in (" + schemaCondition + ")";
    }
}