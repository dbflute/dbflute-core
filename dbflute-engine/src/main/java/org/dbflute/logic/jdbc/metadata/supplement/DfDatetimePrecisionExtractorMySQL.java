/*
 * Copyright 2014-2021 the original author or authors.
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
package org.dbflute.logic.jdbc.metadata.supplement;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.torque.engine.database.model.UnifiedSchema;
import org.dbflute.helper.StringKeyMap;
import org.dbflute.helper.jdbc.facade.DfJdbcFacade;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 1.1.4 (2017/08/12 Saturday at ikspiari)
 */
public class DfDatetimePrecisionExtractorMySQL implements DfDatetimePrecisionExtractor {

    private static final Logger _log = LoggerFactory.getLogger(DfDatetimePrecisionExtractorMySQL.class);

    protected DataSource _dataSource;
    protected UnifiedSchema _unifiedSchema;

    public Map<String, Map<String, Integer>> extractDatetimePrecisionMap(Set<String> tableSet) {
        try {
            final Map<String, Map<String, Integer>> precisionMap = StringKeyMap.createAsFlexibleOrdered();
            final DfJdbcFacade facade = new DfJdbcFacade(_dataSource);
            final List<String> columnList = DfCollectionUtil.newArrayList("TABLE_NAME", "COLUMN_NAME", "DATETIME_PRECISION");
            final StringBuilder sb = new StringBuilder();
            sb.append("select ").append(Srl.connectByDelimiter(columnList, ", "));
            sb.append(" from INFORMATION_SCHEMA.COLUMNS");
            sb.append(" where TABLE_SCHEMA = '").append(_unifiedSchema.getPureCatalog()).append("'");
            // unneeded (use if performance problem at future)
            //if (!tableSet.isEmpty()) { // just in case
            //    sb.append(" and TABLE_NAME in ('").append(Srl.connectByDelimiter(tableSet, "', '")).append("')");
            //}
            sb.append(" and DATA_TYPE = 'datetime'");
            final String sql = sb.toString();
            _log.info(sql);
            final List<Map<String, String>> resultList = facade.selectStringList(sql, columnList);
            for (Map<String, String> recordMap : resultList) {
                final String tableName = recordMap.get("TABLE_NAME");
                final String columnName = recordMap.get("COLUMN_NAME");
                final String datetimePrecision = recordMap.get("DATETIME_PRECISION");
                Map<String, Integer> columnMap = precisionMap.get(tableName);
                if (columnMap == null) {
                    columnMap = StringKeyMap.createAsFlexibleOrdered();
                    precisionMap.put(tableName, columnMap);
                }
                columnMap.put(columnName, Integer.valueOf(datetimePrecision));
            }
            return precisionMap;
        } catch (RuntimeException continued) {
            _log.info("Failed to select date-time precision, so you cannot use date precision option.", continued);
            return Collections.emptyMap();
        }
    }

    public void setDataSource(DataSource dataSource) {
        _dataSource = dataSource;
    }

    public void setUnifiedSchema(UnifiedSchema unifiedSchema) {
        _unifiedSchema = unifiedSchema;
    }
}
