/*
 * Copyright 2014-2020 the original author or authors.
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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.torque.engine.database.model.UnifiedSchema;
import org.dbflute.helper.StringKeyMap;
import org.dbflute.logic.jdbc.metadata.DfAbstractMetaDataExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 1.0.5A (2013/11/04 Monday)
 */
public abstract class DfUniqueKeyFkExtractorBase extends DfAbstractMetaDataExtractor implements DfUniqueKeyFkExtractor {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger _log = LoggerFactory.getLogger(DfUniqueKeyFkExtractorBase.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected Connection _conn;
    protected UnifiedSchema _unifiedSchema;

    // ===================================================================================
    //                                                                                Main
    //                                                                                ====
    public Map<String, Map<String, List<UserUniqueFkColumn>>> extractUniqueKeyFkMap() {
        final Map<String, Map<String, List<UserUniqueFkColumn>>> resultMap = StringKeyMap.createAsFlexible();
        try {
            final List<UserUniqueFkColumn> userTabCommentsList = selectUserUniqueFkList(_conn);
            for (UserUniqueFkColumn userUniqueFk : userTabCommentsList) {
                final String foreignKeyName = userUniqueFk.getForeignKeyName();
                final String localTableName = userUniqueFk.getLocalTableName();

                Map<String, List<UserUniqueFkColumn>> fkColumnListMap = resultMap.get(localTableName);
                if (fkColumnListMap == null) {
                    fkColumnListMap = newLinkedHashMap();
                    resultMap.put(localTableName, fkColumnListMap);
                }
                List<UserUniqueFkColumn> columnList = fkColumnListMap.get(foreignKeyName);
                if (columnList == null) {
                    columnList = new ArrayList<UserUniqueFkColumn>();
                    fkColumnListMap.put(foreignKeyName, columnList);
                }
                columnList.add(userUniqueFk);
            }
            return resultMap;
        } catch (RuntimeException e) {
            String msg = "Failed to extract unique-key FK: unifiedSchema=" + _unifiedSchema;
            throw new IllegalStateException(msg, e);
        }
    }

    protected abstract List<UserUniqueFkColumn> selectUserUniqueFkList(Connection conn);

    protected List<UserUniqueFkColumn> doSelectUserUniqueFkList(String sql, Connection conn) {
        Statement st = null;
        ResultSet rs = null;
        try {
            st = conn.createStatement();
            _log.info(sql);
            rs = st.executeQuery(sql);
            final List<UserUniqueFkColumn> resultList = new ArrayList<UserUniqueFkColumn>();
            while (rs.next()) {
                final String foreignKeyName = rs.getString("FOREIGN_KEY_NAME");
                final String localTableName = rs.getString("LOCAL_TABLE_NAME");
                final String localColumnName = rs.getString("LOCAL_COLUMN_NAME");
                final String foreignTableName = rs.getString("FOREIGN_TABLE_NAME");
                final String foreignColumnName = rs.getString("FOREIGN_COLUMN_NAME");
                final UserUniqueFkColumn userUniqueFk = new UserUniqueFkColumn();
                userUniqueFk.setForeignKeyName(foreignKeyName);
                userUniqueFk.setLocalTableName(localTableName);
                userUniqueFk.setLocalColumnName(localColumnName);
                userUniqueFk.setForeignTableName(foreignTableName);
                userUniqueFk.setForeignColumnName(foreignColumnName);
                resultList.add(userUniqueFk);
            }
            return resultList;
        } catch (SQLException e) {
            String msg = "Failed to extract unique-key FK: sql=" + sql;
            throw new IllegalStateException(msg, e);
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
                    _log.info("st.close() threw the exception!", ignored);
                }
            }
        }
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setConnection(Connection conn) {
        _conn = conn;
    }

    public UnifiedSchema getUnifiedSchema() {
        return _unifiedSchema;
    }

    public void setUnifiedSchema(UnifiedSchema unifiedSchema) {
        _unifiedSchema = unifiedSchema;
    }
}
