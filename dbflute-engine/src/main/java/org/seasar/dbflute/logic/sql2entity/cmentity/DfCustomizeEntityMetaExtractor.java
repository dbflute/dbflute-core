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
package org.seasar.dbflute.logic.sql2entity.cmentity;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.dbflute.helper.StringKeyMap;
import org.seasar.dbflute.logic.jdbc.metadata.basic.DfColumnExtractor;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfColumnMeta;

public class DfCustomizeEntityMetaExtractor {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log _log = LogFactory.getLog(DfCustomizeEntityMetaExtractor.class);

    public static interface DfForcedJavaNativeProvider {
        String provide(String columnName);
    }

    // ===================================================================================
    //                                                                                Main
    //                                                                                ====
    public Map<String, DfColumnMeta> extractColumnMetaInfoMap(ResultSet rs, String sql,
            DfForcedJavaNativeProvider forcedJavaNativeProvider) throws SQLException {
        final Map<String, DfColumnMeta> columnMetaInfoMap = StringKeyMap.createAsFlexibleOrdered();
        final ResultSetMetaData md = rs.getMetaData();
        for (int i = 1; i <= md.getColumnCount(); i++) {
            final DfColumnMeta columnMeta = new DfColumnMeta();

            String sql2EntityRelatedTableName = null;
            try {
                sql2EntityRelatedTableName = md.getTableName(i);
            } catch (SQLException continued) {
                // because this table name is not required, basically only for classification
                String msg = "ResultSetMetaData.getTableName(" + i + ") threw the exception: " + continued.getMessage();
                _log.info(msg);
            }
            columnMeta.setSql2EntityRelatedTableName(sql2EntityRelatedTableName);

            String columnName = md.getColumnLabel(i);
            final String relatedColumnName = md.getColumnName(i);
            columnMeta.setSql2EntityRelatedColumnName(relatedColumnName);
            if (columnName == null || columnName.trim().length() == 0) {
                columnName = relatedColumnName;
            }
            if (columnName == null || columnName.trim().length() == 0) {
                final String ln = ln();
                String msg = "The columnName is invalid: columnName=" + columnName + ln;
                msg = msg + "ResultSetMetaData returned invalid value." + ln;
                msg = msg + "sql=" + sql;
                throw new IllegalStateException(msg);
            }
            columnMeta.setColumnName(columnName);

            final int columnType = md.getColumnType(i);
            columnMeta.setJdbcDefValue(columnType);

            final String columnTypeName = md.getColumnTypeName(i);
            columnMeta.setDbTypeName(columnTypeName);

            int columnSize = md.getPrecision(i);
            if (!DfColumnExtractor.isColumnSizeValid(columnSize)) {
                columnSize = md.getColumnDisplaySize(i); // e.g. sum(COLUMN)
            }
            columnMeta.setColumnSize(columnSize);

            final int scale = md.getScale(i);
            columnMeta.setDecimalDigits(scale);

            if (forcedJavaNativeProvider != null) {
                final String sql2entityForcedJavaNative = forcedJavaNativeProvider.provide(columnName);
                columnMeta.setSql2EntityForcedJavaNative(sql2entityForcedJavaNative);
            }

            // not use meta data because it might be not accuracy
            // and it is unneeded in outside-SQL first
            // but only used as optional determination for Scala
            // so you can specify not-null mark at select column comment e.g. -- // *Member Name
            // (see DfCustomizeEntityInfo#acceptSelectColumnComment())
            //try {
            //    // basically it is unneeded in outside-SQL and might be not accuracy
            //    // but get it here just in case (use-or-not depends on Sql2Entity handling)
            //    final int nullable = md.isNullable(i);
            //    if (ResultSetMetaData.columnNoNulls == nullable) {
            //        columnMeta.setRequired(true);
            //    }
            //} catch (SQLException continued) {
            //    // because this is added after production so for compatible just in case
            //    String msg = "ResultSetMetaData.isNullable(" + i + ") threw the exception: " + continued.getMessage();
            //    _log.info(msg);
            //}

            // column comment is not set here (no comment on meta data)
            // if select column comment is specified, comment will be set later

            columnMetaInfoMap.put(columnName, columnMeta);
        }
        return columnMetaInfoMap;
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String ln() {
        return "\n";
    }
}
