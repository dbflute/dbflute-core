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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.engine.database.model.UnifiedSchema;
import org.seasar.dbflute.exception.DfCommentExtractingFailureException;
import org.seasar.dbflute.helper.StringKeyMap;
import org.seasar.dbflute.logic.jdbc.metadata.DfAbstractMetaDataExtractor;

/**
 * @author jflute
 * @since 0.9.5.3 (2009/08/06 Thursday)
 */
public abstract class DfDbCommentExtractorBase extends DfAbstractMetaDataExtractor implements DfDbCommentExtractor {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log _log = LogFactory.getLog(DfDbCommentExtractorBase.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected DataSource _dataSource;
    protected UnifiedSchema _unifiedSchema;

    // ===================================================================================
    //                                                                                Main
    //                                                                                ====
    public Map<String, UserTabComments> extractTableComment(Set<String> tableSet) {
        final Map<String, UserTabComments> resultMap = StringKeyMap.createAsFlexible();
        Connection conn = null;
        try {
            conn = _dataSource.getConnection();
            final List<UserTabComments> userTabCommentsList = selectUserTabComments(conn, tableSet);
            for (UserTabComments userTabComments : userTabCommentsList) {
                resultMap.put(userTabComments.getTableName(), userTabComments);
            }
            return resultMap;
        } catch (SQLException e) {
            String msg = "Failed to extract comment data: unifiedSchema=" + _unifiedSchema;
            throw new DfCommentExtractingFailureException(msg, e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignored) {
                    _log.info("connection.close() threw the exception!", ignored);
                }
            }
        }
    }

    public Map<String, Map<String, UserColComments>> extractColumnComment(Set<String> tableSet) {
        final Map<String, Map<String, UserColComments>> resultMap = StringKeyMap.createAsFlexible();
        Connection conn = null;
        try {
            conn = _dataSource.getConnection();
            final List<UserColComments> userColCommentsList = selectUserColComments(conn, tableSet);
            String previousTableName = null;
            Map<String, UserColComments> elementMap = null;
            for (UserColComments userColComments : userColCommentsList) {
                final String tableName = userColComments.getTableName();
                if (previousTableName == null || !previousTableName.equals(tableName)) {
                    previousTableName = tableName;
                    elementMap = new LinkedHashMap<String, UserColComments>();
                    resultMap.put(tableName, elementMap);
                }
                final String columnName = userColComments.getColumnName();
                elementMap.put(columnName, userColComments);
            }
            return resultMap;
        } catch (SQLException e) {
            String msg = "Failed to extract comment data: schema=" + _unifiedSchema;
            throw new DfCommentExtractingFailureException(msg, e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignored) {
                    _log.info("connection.close() threw the exception!", ignored);
                }
            }
        }
    }

    protected abstract List<UserTabComments> selectUserTabComments(Connection conn, Set<String> tableSet);

    protected List<UserTabComments> doSelectUserTabComments(String sql, Connection conn, Set<String> tableSet) {
        Statement st = null;
        ResultSet rs = null;
        try {
            st = conn.createStatement();
            _log.info(sql);
            rs = st.executeQuery(sql);
            final List<UserTabComments> resultList = new ArrayList<UserTabComments>();
            while (rs.next()) {
                final String tableName = rs.getString("TABLE_NAME");
                if (!tableSet.contains(tableName)) {
                    continue;
                }
                final String comments = rs.getString("COMMENTS");
                final UserTabComments userTabComments = new UserTabComments();
                userTabComments.setTableName(tableName);
                userTabComments.setComments(filterTableComments(comments));
                resultList.add(userTabComments);
            }
            return resultList;
        } catch (SQLException e) {
            String msg = "Failed to extract table comment: sql=" + sql;
            throw new DfCommentExtractingFailureException(msg, e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignored) {
                    _log.info("rs.close() threw the exception!", ignored);
                }
            }
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException ignored) {
                    _log.info("statement.close() threw the exception!", ignored);
                }
            }
        }
    }

    protected String filterTableComments(String comments) { // extension point
        return comments; // as default
    }

    protected abstract List<UserColComments> selectUserColComments(Connection conn, Set<String> tableSet);

    protected List<UserColComments> doSelectUserColComments(String sql, Connection conn, Set<String> tableSet) {
        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = conn.createStatement();
            _log.info(sql);
            rs = statement.executeQuery(sql);
            final List<UserColComments> resultList = new ArrayList<UserColComments>();
            while (rs.next()) {
                final String tableName = rs.getString("TABLE_NAME");
                if (!tableSet.contains(tableName)) {
                    continue;
                }
                final String columnName = rs.getString("COLUMN_NAME");
                final String comments = rs.getString("COMMENTS");
                final UserColComments userColComments = new UserColComments();
                userColComments.setTableName(tableName);
                userColComments.setColumnName(columnName);
                userColComments.setComments(filterColumnComments(comments));
                resultList.add(userColComments);
            }
            return resultList;
        } catch (SQLException e) {
            String msg = "Failed to extract column comment: sql=" + sql;
            throw new DfCommentExtractingFailureException(msg, e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignored) {
                    _log.info("rs.close() threw the exception!", ignored);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignored) {
                    _log.info("statement.close() threw the exception!", ignored);
                }
            }
        }
    }

    protected String filterColumnComments(String comments) { // extension point
        return comments; // as default
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setDataSource(DataSource dataSource) {
        _dataSource = dataSource;
    }

    public UnifiedSchema getUnifiedSchema() {
        return _unifiedSchema;
    }

    public void setUnifiedSchema(UnifiedSchema unifiedSchema) {
        this._unifiedSchema = unifiedSchema;
    }
}
