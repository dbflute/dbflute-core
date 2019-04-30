/*
 * Copyright 2014-2019 the original author or authors.
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
package org.dbflute.logic.jdbc.metadata.comment;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 */
public class DfDbCommentExtractorOracle extends DfDbCommentExtractorBase {

    private static final Logger _log = LoggerFactory.getLogger(DfDbCommentExtractorBase.class);

    // ===================================================================================
    //                                                                    Select Meta Data
    //                                                                    ================
    protected List<UserTabComments> selectUserTabComments(Connection conn, Set<String> tableSet) {
        if (!_unifiedSchema.existsPureSchema()) {
            String msg = "Extracting comments from Oracle requires pure schema in unified schema:";
            msg = msg + " unifiedSchema=" + _unifiedSchema;
            throw new IllegalStateException(msg);
        }
        final StringBuilder sb = new StringBuilder();
        sb.append("select * from ALL_TAB_COMMENTS");
        sb.append(" where OWNER = '").append(_unifiedSchema.getPureSchema()).append("'");
        buildTableNameCondition(sb, "TABLE_NAME", tableSet);
        sb.append(" order by TABLE_NAME asc");
        final String basicSql = sb.toString();
        final List<UserTabComments> basicCommentsList = doSelectUserTabComments(basicSql, conn, tableSet);
        final List<UserTabComments> materializedCommentsList = selectMaterializedViewComments(conn, tableSet);
        basicCommentsList.addAll(materializedCommentsList);
        return basicCommentsList;
    }

    protected List<UserTabComments> selectMaterializedViewComments(Connection conn, Set<String> tableSet) {
        try {
            final StringBuilder sb = new StringBuilder();
            sb.append("select MVIEW_NAME as TABLE_NAME, COMMENTS from ALL_MVIEW_COMMENTS");
            sb.append(" where OWNER = '").append(_unifiedSchema.getPureSchema()).append("'");
            buildTableNameCondition(sb, "MVIEW_NAME", tableSet);
            sb.append(" order by TABLE_NAME asc");
            final String materializedSql = sb.toString();
            return doSelectUserTabComments(materializedSql, conn, tableSet);
        } catch (RuntimeException continued) { // just in case
            final String main = continued.getMessage();
            final Throwable cause = continued.getCause();
            final String nested = cause != null ? (", " + cause.getMessage()) : "";
            _log.info("*Failed to select materialized view comments: " + main + nested);
            return new ArrayList<UserTabComments>(2);
        }
    }

    protected List<UserColComments> selectUserColComments(Connection conn, Set<String> tableSet) {
        if (!_unifiedSchema.existsPureSchema()) {
            String msg = "Extracting comments from Oracle requires pure schema in unified schema:";
            msg = msg + " unifiedSchema=" + _unifiedSchema;
            throw new IllegalStateException(msg);
        }
        final StringBuilder sb = new StringBuilder();
        sb.append("select * from ALL_COL_COMMENTS");
        sb.append(" where OWNER = '").append(_unifiedSchema.getPureSchema()).append("'");
        buildTableNameCondition(sb, "TABLE_NAME", tableSet);
        sb.append(" order by TABLE_NAME asc, COLUMN_NAME asc");
        final String sql = sb.toString();
        return doSelectUserColComments(sql, conn, tableSet);
    }

    protected void buildTableNameCondition(StringBuilder sb, String columnName, Set<String> tableSet) { // for performance
        if (!tableSet.isEmpty() && tableSet.size() < 1000) { // Oracle's inScope limit
            sb.append(" and ").append(columnName).append(" in (");
            int index = 0;
            for (String table : tableSet) {
                sb.append(index > 0 ? ", " : "").append("'").append(table).append("'");
                ++index;
            }
            sb.append(")");
        }
        // even if over limit, removed after select
    }
}
