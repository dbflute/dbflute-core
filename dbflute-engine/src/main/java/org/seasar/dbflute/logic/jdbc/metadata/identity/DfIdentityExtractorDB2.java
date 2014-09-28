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
package org.seasar.dbflute.logic.jdbc.metadata.identity;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

/**
 * @author jflute
 */
public class DfIdentityExtractorDB2 implements DfIdentityExtractor {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected DataSource _dataSource;

    // ===================================================================================
    //                                                                                Main
    //                                                                                ====
    public Map<String, String> extractIdentityMap() {
        final Connection conn;
        try {
            conn = _dataSource.getConnection();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        try {
            HashMap<String, String> resultMap = new HashMap<String, String>();
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("select * from SYSCAT.COLUMNS where GENERATED != ' '");
            while (rs.next()) {
                String tableName = rs.getString("TABNAME");
                String columnName = rs.getString("COLNAME");
                String generated = rs.getString("GENERATED");
                if (generated == null || generated.trim().length() == 0) {
                    continue;
                }
                resultMap.put(tableName, columnName);
            }
            return resultMap;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setDataSource(DataSource dataSource) {
        _dataSource = dataSource;
    }
}
