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
package org.seasar.dbflute.logic.jdbc.metadata.synonym;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.engine.database.model.UnifiedSchema;
import org.seasar.dbflute.helper.jdbc.facade.DfJdbcFacade;
import org.seasar.dbflute.logic.jdbc.metadata.procedure.DfProcedureParameterNativeExtractorOracle;
import org.seasar.dbflute.util.DfCollectionUtil;

/**
 * @author jflute
 * @since 0.9.9.1A (2011/09/30 Friday)
 */
public class DfSynonymNativeExtractorOracle {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log _log = LogFactory.getLog(DfProcedureParameterNativeExtractorOracle.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DataSource _dataSource;
    protected final boolean _suppressLogging;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfSynonymNativeExtractorOracle(DataSource dataSource, boolean suppressLogging) {
        _dataSource = dataSource;
        _suppressLogging = suppressLogging;
    }

    // ===================================================================================
    //                                                                        Synonym Info
    //                                                                        ============
    public Map<String, SynonymNativeInfo> selectSynonymInfoMap(UnifiedSchema unifiedSchema) {
        final String sql = buildSynonymSql(unifiedSchema);
        return doSelectSynonymInfoMap(sql, false);
    }

    protected String buildSynonymSql(UnifiedSchema unifiedSchema) {
        final StringBuilder sb = new StringBuilder();
        sb.append("select *");
        sb.append(" from ALL_SYNONYMS");
        sb.append(" where OWNER = '").append(unifiedSchema.getPureSchema()).append("'");
        sb.append(" order by SYNONYM_NAME");
        return sb.toString();
    }

    public Map<String, SynonymNativeInfo> selectDBLinkSynonymInfoMap(String dbLinkName) {
        final String sql = buildDBLinkSynonymSql(dbLinkName);
        return doSelectSynonymInfoMap(sql, true);
    }

    protected String buildDBLinkSynonymSql(String dbLinkName) {
        final StringBuilder sb = new StringBuilder();
        sb.append("select *");
        sb.append(" from USER_SYNONYMS@").append(dbLinkName);
        sb.append(" order by SYNONYM_NAME");
        return sb.toString();
    }

    protected Map<String, SynonymNativeInfo> doSelectSynonymInfoMap(String sql, boolean transaction) {
        final DfJdbcFacade facade = new DfJdbcFacade(_dataSource);
        if (transaction) {
            // reported that five or more DB links are not allowed to connect
            // you can avoid it by committing so it uses transaction
            facade.useTransaction();
        }
        final List<String> columnList = new ArrayList<String>();
        columnList.add("SYNONYM_NAME");
        columnList.add("TABLE_OWNER");
        columnList.add("TABLE_NAME");
        columnList.add("DB_LINK");
        final List<Map<String, String>> resultList;
        try {
            log(sql);
            resultList = facade.selectStringList(sql, columnList);
        } catch (Exception continued) {
            // because it's basically assist info
            log("Failed to select synonym info: " + continued.getMessage());
            return DfCollectionUtil.emptyMap();
        }
        final Map<String, SynonymNativeInfo> infoMap = DfCollectionUtil.newLinkedHashMap();
        for (Map<String, String> map : resultList) {
            final SynonymNativeInfo info = new SynonymNativeInfo();
            info.setSynonymName(map.get("SYNONYM_NAME"));
            info.setTableOwner(map.get("TABLE_OWNER"));
            info.setTableName(map.get("TABLE_NAME"));
            infoMap.put(info.getSynonymName(), info);
        }
        return infoMap;
    }

    public static class SynonymNativeInfo {
        protected String _synonymName;
        protected String _tableOwner;
        protected String _tableName;
        protected String _dbLink;

        public String getSynonymName() {
            return _synonymName;
        }

        public void setSynonymName(String synonymName) {
            this._synonymName = synonymName;
        }

        public String getTableOwner() {
            return _tableOwner;
        }

        public void setTableOwner(String tableOwner) {
            this._tableOwner = tableOwner;
        }

        public String getTableName() {
            return _tableName;
        }

        public void setTableName(String tableName) {
            this._tableName = tableName;
        }

        public String getDbLink() {
            return _dbLink;
        }

        public void setDbLink(String dbLink) {
            this._dbLink = dbLink;
        }
    }

    // ===================================================================================
    //                                                                             Logging
    //                                                                             =======
    protected void log(String msg) {
        if (_suppressLogging) {
            return;
        }
        _log.info(msg);
    }
}
