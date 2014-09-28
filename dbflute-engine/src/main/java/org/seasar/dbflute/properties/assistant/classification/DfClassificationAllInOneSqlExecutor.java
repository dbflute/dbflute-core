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
package org.seasar.dbflute.properties.assistant.classification;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.dbflute.exception.SQLFailureException;
import org.seasar.dbflute.resource.DBFluteSystem;

/**
 * @author jflute
 * @since 0.9.5.1 (2009/07/03 Friday)
 */
public class DfClassificationAllInOneSqlExecutor {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log _log = LogFactory.getLog(DfClassificationAllInOneSqlExecutor.class);

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    public List<Map<String, String>> executeAllInOneSql(Connection conn, String sql) {
        Statement st = null;
        ResultSet rs = null;
        final List<Map<String, String>> elementList = new ArrayList<Map<String, String>>();
        try {
            st = conn.createStatement();
            _log.debug("/ - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
            _log.debug("The classification sql: " + sql);
            rs = st.executeQuery(sql);
            while (rs.next()) {
                final String tmpClassificationNameValue = rs.getString("classificationName");
                final String tmpCodeValue = rs.getString(DfClassificationElement.KEY_CODE);
                if (tmpCodeValue == null) {
                    String msg = "The sql should have 'code' column. But null: sql=" + sql;
                    throw new IllegalStateException(msg);
                }
                String tmpNameValue = rs.getString(DfClassificationElement.KEY_NAME);
                if (tmpNameValue == null) {
                    tmpNameValue = tmpCodeValue;
                }
                String tmpAliasValue = rs.getString(DfClassificationElement.KEY_ALIAS);
                if (tmpAliasValue == null) {
                    tmpAliasValue = tmpNameValue;
                }
                final String tmpCommentValue = rs.getString(DfClassificationElement.KEY_COMMENT);
                final String tmpTopCommentValue = rs.getString(DfClassificationTop.KEY_TOP_COMMENT);

                final Map<String, String> selectedTmpMap = new LinkedHashMap<String, String>();
                selectedTmpMap.put("classificationName", tmpClassificationNameValue);
                selectedTmpMap.put(DfClassificationElement.KEY_CODE, tmpCodeValue);
                selectedTmpMap.put(DfClassificationElement.KEY_NAME, tmpNameValue);
                selectedTmpMap.put(DfClassificationElement.KEY_ALIAS, tmpAliasValue);
                if (tmpCommentValue != null) {
                    selectedTmpMap.put(DfClassificationElement.KEY_COMMENT, tmpCommentValue);
                }
                if (tmpTopCommentValue != null) {
                    selectedTmpMap.put(DfClassificationTop.KEY_TOP_COMMENT, tmpTopCommentValue);
                }

                elementList.add(selectedTmpMap);
            }
            _log.debug("- - - - - - - - /");
        } catch (SQLException e) {
            throw new SQLFailureException("Failed to execute the SQL:" + ln() + sql, e);
        } finally {
            new DfClassificationSqlResourceCloser().closeStatement(st, rs);
        }
        return elementList;
    }

    protected String ln() {
        return DBFluteSystem.getBasicLn();
    }
}
