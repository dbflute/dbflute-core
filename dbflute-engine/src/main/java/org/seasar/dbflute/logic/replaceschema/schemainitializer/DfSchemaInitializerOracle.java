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
package org.seasar.dbflute.logic.replaceschema.schemainitializer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.dbflute.exception.SQLFailureException;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfTableMeta;
import org.seasar.dbflute.util.Srl;

/**
 * The schema initializer for Oracle.
 * @author jflute
 * @since 0.8.0 (2008/09/05 Friday)
 */
public class DfSchemaInitializerOracle extends DfSchemaInitializerJdbc {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log _log = LogFactory.getLog(DfSchemaInitializerOracle.class);

    // ===================================================================================
    //                                                                    Drop Foreign Key
    //                                                                    ================
    @Override
    protected boolean isSkipDropForeignKey(DfTableMeta tableMetaInfo) {
        return tableMetaInfo.isTableTypeSynonym();
    }

    // ===================================================================================
    //                                                                          Drop Table
    //                                                                          ==========
    @Override
    protected void setupDropTable(StringBuilder sb, DfTableMeta metaInfo) {
        if (metaInfo.isTableTypeSynonym()) {
            final String tableName = metaInfo.getTableSqlName();
            sb.append("drop synonym ").append(tableName);
        } else {
            super.setupDropTable(sb, metaInfo);
        }
    }

    // ===================================================================================
    //                                                                       Drop Sequence
    //                                                                       =============
    @Override
    protected void dropSequence(Connection conn, List<DfTableMeta> tableMetaInfoList) {
        doDropSequence(conn);
    }

    protected void doDropSequence(Connection conn) {
        final String tableName = "ALL_SEQUENCES";
        dropDicObject(conn, "sequences", "sequence", tableName, "SEQUENCE_OWNER", "SEQUENCE_NAME", null, true, false,
                new ObjectExceptCallback() {
                    public boolean isExcept(String objectName) {
                        return isSequenceExcept(objectName);
                    }
                });
    }

    // ===================================================================================
    //                                                                        Drop DB Link
    //                                                                        ============
    @Override
    protected void dropDBLink(Connection conn, List<DfTableMeta> tableMetaInfoList) {
        doDropDBLink(conn);
    }

    /**
     * Drop DB links that are private DB links. <br />
     * @param conn The connection to main schema. (NotNull)
     */
    protected void doDropDBLink(Connection conn) {
        final String tableName = "ALL_DB_LINKS";
        dropDicObject(conn, "DB links", "database link", tableName, "OWNER", "DB_LINK", null, false, false, null);
    }

    // ===================================================================================
    //                                                                    Drop Type Object
    //                                                                    ================
    @Override
    protected void dropTypeObject(Connection conn, List<DfTableMeta> tableMetaInfoList) {
        // TYPE objects have dependences themselves
        final int retryLimit = 10;
        int retryCount = 0;
        while (true) {
            final String orderBy = "TYPECODE " + (retryCount % 2 == 0 ? "asc" : "desc");
            boolean complete = doDropTypeObject(conn, orderBy, true);
            if (complete || retryLimit <= retryCount) {
                break;
            }
            ++retryCount;
        }
    }

    protected boolean doDropTypeObject(Connection conn, String orderBy, boolean errorContinue) {
        return dropDicObject(conn, "type objects", "type", "ALL_TYPES", "OWNER", "TYPE_NAME", orderBy, false,
                errorContinue, null);
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected boolean dropDicObject(Connection conn, String titleName, String sqlName, String tableName,
            String ownerColumnName, String targetColumnName, String orderBy, boolean schemaPrefix,
            boolean errorContinue, ObjectExceptCallback callback) {
        if (!_unifiedSchema.hasSchema()) {
            return true;
        }
        final String schema = _unifiedSchema.getPureSchema();
        final List<String> objectNameList = new ArrayList<String>();
        final StringBuilder sb = new StringBuilder();
        sb.append("select * from ").append(tableName);
        sb.append(" where ").append(ownerColumnName).append(" = '").append(schema).append("'");
        if (Srl.is_NotNull_and_NotTrimmedEmpty(orderBy)) {
            sb.append(" order by ").append(orderBy);
        }
        final String metaSql = sb.toString();
        Statement st = null;
        ResultSet rs = null;
        try {
            st = conn.createStatement();
            _log.info("...Executing helper SQL:" + ln() + metaSql);
            rs = st.executeQuery(metaSql);
            while (rs.next()) {
                final String objectName = rs.getString(targetColumnName);
                objectNameList.add(objectName);
            }
        } catch (SQLException continued) {
            // if the data dictionary table is not found,
            // it continues because it might be a version difference 
            String msg = "*Failed to the SQL:" + ln();
            msg = msg + (continued.getMessage() != null ? continued.getMessage() : null) + ln();
            msg = msg + metaSql;
            _log.info(metaSql);
            return true;
        } finally {
            closeResource(rs, st);
        }
        try {
            boolean complete = true;
            st = conn.createStatement();
            for (String objectName : objectNameList) {
                if (callback != null && callback.isExcept(objectName)) {
                    continue;
                }
                final String prefix = schemaPrefix ? schema + "." : "";
                final String dropSql = "drop " + sqlName + " " + prefix + objectName;
                logReplaceSql(dropSql);
                try {
                    st.execute(dropSql);
                } catch (SQLException e) {
                    if (errorContinue) {
                        complete = false;
                        continue;
                    }
                    throw e;
                }
            }
            return complete;
        } catch (SQLException e) {
            String msg = "Failed to drop " + titleName + ": " + objectNameList;
            throw new SQLFailureException(msg, e);
        } finally {
            closeStatement(st);
        }
    }

    protected static interface ObjectExceptCallback {
        boolean isExcept(String objectName);
    }
}