/*
 * Copyright 2014-2017 the original author or authors.
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
package org.dbflute.logic.jdbc.metadata.synonym;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.torque.engine.database.model.UnifiedSchema;
import org.dbflute.helper.jdbc.facade.DfJdbcFacade;
import org.dbflute.logic.jdbc.metadata.procedure.DfProcedureParameterNativeExtractorOracle;
import org.dbflute.util.DfCollectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 0.9.9.1A (2011/09/30 Friday)
 */
public class DfDBLinkNativeExtractorOracle {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger _log = LoggerFactory.getLogger(DfProcedureParameterNativeExtractorOracle.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DataSource _dataSource;
    protected final boolean _suppressLogging;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfDBLinkNativeExtractorOracle(DataSource dataSource, boolean suppressLogging) {
        _dataSource = dataSource;
        _suppressLogging = suppressLogging;
    }

    // ===================================================================================
    //                                                                         DBLink Info
    //                                                                         ===========
    public Map<String, DBLinkNativeInfo> selectDBLinkInfoMap() { // main schema
        final String sql = buildDBLinkSql();
        return doSelectDBLinkInfoMap(sql);
    }

    public Map<String, DBLinkNativeInfo> selectDBLinkInfoMap(UnifiedSchema unifiedSchema) {
        final String sql = buildDBLinkSql(unifiedSchema);
        return doSelectDBLinkInfoMap(sql);
    }

    protected String buildDBLinkSql() {
        final StringBuilder sb = new StringBuilder();
        sb.append("select *");
        sb.append(" from USER_DB_LINKS");
        sb.append(" order by DB_LINK");
        return sb.toString();
    }

    protected String buildDBLinkSql(UnifiedSchema unifiedSchema) {
        final StringBuilder sb = new StringBuilder();
        sb.append("select *");
        sb.append(" from ALL_DB_LINKS");
        sb.append(" where OWNER = '").append(unifiedSchema.getPureSchema()).append("'");
        sb.append(" order by OWNER, DB_LINK");
        return sb.toString();
    }

    protected Map<String, DBLinkNativeInfo> doSelectDBLinkInfoMap(String sql) {
        final DfJdbcFacade facade = new DfJdbcFacade(_dataSource);
        final List<String> columnList = new ArrayList<String>();
        columnList.add("DB_LINK");
        columnList.add("USERNAME");
        columnList.add("HOST");
        final List<Map<String, String>> resultList;
        try {
            log(sql);
            resultList = facade.selectStringList(sql, columnList);
        } catch (Exception continued) {
            // because it's basically assist info
            log("Failed to select DB link info: " + continued.getMessage());
            return DfCollectionUtil.emptyMap();
        }
        final Map<String, DBLinkNativeInfo> infoMap = DfCollectionUtil.newLinkedHashMap();
        for (Map<String, String> map : resultList) {
            final DBLinkNativeInfo info = new DBLinkNativeInfo();
            info.setDbLink(map.get("DB_LINK"));
            info.setUserName(map.get("USERNAME"));
            info.setHost(map.get("HOST"));
            infoMap.put(info.getDbLink(), info);
        }
        return infoMap;
    }

    public static class DBLinkNativeInfo {
        protected String _dbLink;
        protected String _userName;
        protected String _host;

        public String getDbLink() {
            return _dbLink;
        }

        public void setDbLink(String dbLink) {
            this._dbLink = dbLink;
        }

        public String getUserName() {
            return _userName;
        }

        public void setUserName(String userName) {
            this._userName = userName;
        }

        public String getHost() {
            return _host;
        }

        public void setHost(String host) {
            this._host = host;
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
