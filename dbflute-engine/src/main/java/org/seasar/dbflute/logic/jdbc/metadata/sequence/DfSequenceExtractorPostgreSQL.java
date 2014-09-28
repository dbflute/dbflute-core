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

/**
 * @author jflute
 * @since 0.9.6.4 (2010/01/16 Saturday)
 */
public class DfSequenceExtractorPostgreSQL extends DfSequenceExtractorBase {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log _log = LogFactory.getLog(DfSequenceExtractorPostgreSQL.class);

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfSequenceExtractorPostgreSQL(DataSource dataSource, List<UnifiedSchema> unifiedSchemaList) {
        super(dataSource, unifiedSchemaList);
    }

    // ===================================================================================
    //                                                                        Sequence Map
    //                                                                        ============
    protected Map<String, DfSequenceMeta> doGetSequenceMap() {
        _log.info("...Loading sequence informations");
        final Map<String, DfSequenceMeta> resultMap = StringKeyMap.createAsFlexibleOrdered();
        final String sql = buildMetaSelectSql();
        final List<String> columnList = new ArrayList<String>();
        columnList.add("sequence_catalog");
        columnList.add("sequence_schema");
        columnList.add("sequence_name");
        columnList.add("minimum_value");
        columnList.add("maximum_value");
        columnList.add("increment");
        final List<Map<String, String>> resultList = selectStringList(sql, columnList);
        final StringBuilder logSb = new StringBuilder();
        logSb.append(ln()).append("[SEQUENCE]");
        for (Map<String, String> recordMap : resultList) {
            final DfSequenceMeta info = new DfSequenceMeta();
            final String sequenceCatalog = recordMap.get("sequence_catalog");
            info.setSequenceCatalog(sequenceCatalog);
            final String sequenceSchema = recordMap.get("sequence_schema");
            info.setSequenceSchema(sequenceSchema);
            final String sequenceName = recordMap.get("sequence_name");
            info.setSequenceName(sequenceName);

            final UnifiedSchema unifiedSchema = createAsDynamicSchema(sequenceCatalog, sequenceSchema);

            String minValue = recordMap.get("minimum_value");
            if (minValue == null || minValue.trim().length() == 0) {
                minValue = selectMinimumValue(unifiedSchema, sequenceName);
            }
            info.setMinimumValue(minValue != null ? new BigDecimal(minValue) : null);

            String maxValue = recordMap.get("maximum_value");
            if (maxValue == null || maxValue.trim().length() == 0) {
                maxValue = selectMaximumValue(unifiedSchema, sequenceName);
            }
            info.setMaximumValue(maxValue != null ? new BigDecimal(maxValue) : null);

            String incrementSize = recordMap.get("increment");
            if (incrementSize == null || incrementSize.trim().length() == 0) {
                incrementSize = selectIncrementSize(unifiedSchema, sequenceName);
            }
            info.setIncrementSize(incrementSize != null ? Integer.valueOf(incrementSize) : null);

            final String key = buildSequenceMapKey(sequenceCatalog, sequenceSchema, sequenceName);
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
            schemaCondition = "'public'";
        }
        // it allowed to exist unused sequences so it does not use catalog condition
        // and sequences cannot show only connected catalog, maybe...
        return "select * from information_schema.sequences where sequence_schema in (" + schemaCondition + ")";
    }

    protected String selectMinimumValue(UnifiedSchema unifiedSchema, String sequenceName) {
        return selectElementValue(unifiedSchema, sequenceName, "min_value");
    }

    protected String selectMaximumValue(UnifiedSchema unifiedSchema, String sequenceName) {
        return selectElementValue(unifiedSchema, sequenceName, "max_value");
    }

    protected String selectIncrementSize(UnifiedSchema unifiedSchema, String sequenceName) {
        return selectElementValue(unifiedSchema, sequenceName, "increment_by");
    }

    protected String selectElementValue(UnifiedSchema unifiedSchema, String sequenceName, String elementName) {
        String sql = buildElementValueSql(unifiedSchema.buildSqlName(sequenceName), elementName);
        final List<String> columnList = new ArrayList<String>();
        columnList.add(elementName);
        final List<Map<String, String>> resultList = selectStringList(sql, columnList);
        if (!resultList.isEmpty()) {
            return resultList.get(0).get(elementName); // only one record exists
        }
        return null;
    }

    protected String buildElementValueSql(String sequenceName, String elementName) {
        return "select " + elementName + " from " + sequenceName;
    }
}