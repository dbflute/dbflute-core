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
package org.seasar.dbflute.logic.jdbc.metadata.comment;

import java.sql.Connection;
import java.util.List;
import java.util.Set;

/**
 * @author jflute
 * @since 0.9.6 (2009/10/31 Saturday)
 */
public class DfDbCommentExtractorMySQL extends DfDbCommentExtractorBase {

    // ===================================================================================
    //                                                                    Select Meta Data
    //                                                                    ================
    protected List<UserTabComments> selectUserTabComments(Connection conn, Set<String> tableSet) {
        // MySQL treats catalog as schema in informations schema
        if (!_unifiedSchema.existsPureCatalog()) {
            String msg = "Extracting comments from MySQL requires pure catalog in unified schema:";
            msg = msg + " unifiedSchema=" + _unifiedSchema;
            throw new IllegalStateException(msg);
        }
        final StringBuilder sb = new StringBuilder();
        sb.append("select table_type as OBJECT_TYPE");
        sb.append(", table_name as TABLE_NAME");
        sb.append(", table_comment as COMMENTS");
        sb.append(" from information_schema.tables");
        sb.append(" where table_schema = '").append(_unifiedSchema.getPureCatalog()).append("'");
        sb.append(" order by table_name asc");
        final String sql = sb.toString();
        return doSelectUserTabComments(sql, conn, tableSet);
    }

    @Override
    protected String filterTableComments(String comments) { // extension point
        if (comments.startsWith("InnoDB free:")) {
            return null;
        }
        final int semicolunIndex = comments.indexOf("; InnoDB free:");
        if (semicolunIndex < 0) {
            return comments;
        }
        return comments.substring(0, semicolunIndex);
    }

    protected List<UserColComments> selectUserColComments(Connection conn, Set<String> tableSet) {
        // MySQL treats catalog as schema in informations schema
        if (!_unifiedSchema.existsPureCatalog()) {
            String msg = "Extracting comments from MySQL requires pure catalog in unified schema:";
            msg = msg + " unifiedSchema=" + _unifiedSchema;
            throw new IllegalStateException(msg);
        }
        final StringBuilder sb = new StringBuilder();
        sb.append("select table_name as TABLE_NAME");
        sb.append(", column_name as COLUMN_NAME");
        sb.append(", column_comment as COMMENTS");
        sb.append(" from information_schema.columns");
        sb.append(" where table_schema = '").append(_unifiedSchema.getPureCatalog()).append("'");
        sb.append(" order by table_name asc, column_name asc");
        final String sql = sb.toString();
        return doSelectUserColComments(sql, conn, tableSet);
    }
}
