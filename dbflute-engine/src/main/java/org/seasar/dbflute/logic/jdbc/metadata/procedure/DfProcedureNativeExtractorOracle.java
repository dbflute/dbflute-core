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
package org.seasar.dbflute.logic.jdbc.metadata.procedure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.engine.database.model.UnifiedSchema;
import org.seasar.dbflute.helper.jdbc.facade.DfJdbcFacade;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfProcedureArgumentInfo;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.9.9.1A (2011/09/30 Friday)
 */
public class DfProcedureNativeExtractorOracle {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log _log = LogFactory.getLog(DfProcedureNativeExtractorOracle.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DataSource _dataSource;

    protected final boolean _suppressLogging;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfProcedureNativeExtractorOracle(DataSource dataSource, boolean suppressLogging) {
        _dataSource = dataSource;
        _suppressLogging = suppressLogging;
    }

    // ===================================================================================
    //                                                                             Extract
    //                                                                             =======
    public Map<String, ProcedureNativeInfo> extractProcedureNativeInfoMap(UnifiedSchema unifiedSchema) { // Oracle dependency
        return selectProcedureNativeInfoMap(unifiedSchema);
    }

    public Map<String, ProcedureNativeInfo> extractDBLinkProcedureNativeInfoList(String dbLinkName) { // Oracle dependency
        return selectDBLinkProcedureNativeInfoMap(dbLinkName);
    }

    // ===================================================================================
    //                                                                         Native Info
    //                                                                         ===========
    protected Map<String, ProcedureNativeInfo> selectProcedureNativeInfoMap(UnifiedSchema unifiedSchema) {
        final String sql = buildProcedureNativeSql(unifiedSchema);
        final Map<String, ProcedureNativeInfo> nativeInfoMap = doSelectProcedureNativeInfoMap(sql);
        final Map<String, List<DfProcedureArgumentInfo>> argInfoMap = selectProcedureArgumentInfoMap(unifiedSchema);
        for (Entry<String, ProcedureNativeInfo> entry : nativeInfoMap.entrySet()) {
            final List<DfProcedureArgumentInfo> argInfoList = argInfoMap.get(entry.getKey());
            if (argInfoList != null) { // found (means the procedure has parameters)
                entry.getValue().acceptArgInfoList(argInfoList);
            }
        }
        return nativeInfoMap;
    }

    protected String buildProcedureNativeSql(UnifiedSchema unifiedSchema) {
        final StringBuilder sb = new StringBuilder();
        sb.append("select OBJECT_NAME, PROCEDURE_NAME");
        sb.append(" from ALL_PROCEDURES");
        sb.append(" where OWNER = '").append(unifiedSchema.getPureSchema()).append("'");
        sb.append(" order by OBJECT_NAME, PROCEDURE_NAME");
        return sb.toString();
    }

    protected Map<String, ProcedureNativeInfo> selectDBLinkProcedureNativeInfoMap(String dbLinkName) {
        final String sql = buildDBLinkProcedureNativeSql(dbLinkName);
        final Map<String, ProcedureNativeInfo> nativeInfoMap = doSelectProcedureNativeInfoMap(sql);
        final Map<String, List<DfProcedureArgumentInfo>> argInfoMap = selectDBLinkProcedureArgumentInfoMap(dbLinkName);
        for (Entry<String, ProcedureNativeInfo> entry : nativeInfoMap.entrySet()) {
            final List<DfProcedureArgumentInfo> argInfoList = argInfoMap.get(entry.getKey());
            if (argInfoList != null) { // found (means the procedure has parameters)
                entry.getValue().acceptArgInfoList(argInfoList);
            }
        }
        return nativeInfoMap;
    }

    protected String buildDBLinkProcedureNativeSql(String dbLinkName) {
        final StringBuilder sb = new StringBuilder();
        sb.append("select OBJECT_NAME, PROCEDURE_NAME");
        sb.append(" from USER_PROCEDURES@").append(dbLinkName);
        sb.append(" order by OBJECT_NAME, PROCEDURE_NAME");
        return sb.toString();
    }

    protected Map<String, ProcedureNativeInfo> doSelectProcedureNativeInfoMap(String sql) {
        final DfJdbcFacade facade = new DfJdbcFacade(_dataSource);
        final List<String> columnList = new ArrayList<String>();
        columnList.add("OBJECT_NAME");
        columnList.add("PROCEDURE_NAME");
        final List<Map<String, String>> resultList;
        try {
            log(sql);
            resultList = facade.selectStringList(sql, columnList);
        } catch (Exception continued) {
            // because it's basically assist info
            log("Failed to select procedure native info: " + continued.getMessage());
            return DfCollectionUtil.emptyMap();
        }
        final Map<String, ProcedureNativeInfo> infoMap = DfCollectionUtil.newLinkedHashMap();
        for (Map<String, String> map : resultList) {
            final ProcedureNativeInfo info = new ProcedureNativeInfo();
            final String objectName = map.get("OBJECT_NAME");
            final String procedureName = map.get("PROCEDURE_NAME");
            // translate Oracle's strange data structure
            if (Srl.is_NotNull_and_NotTrimmedEmpty(procedureName)) {
                info.setPackageName(objectName); // objectName is packageName here
                info.setProcedureName(procedureName);
            } else {
                info.setProcedureName(objectName); // objectName is procedureName here
            }
            infoMap.put(generateNativeInfoMapKey(objectName, procedureName), info);
        }
        return infoMap;
    }

    public static String generateNativeInfoMapKey(String packageName, String procedureName) {
        final StringBuilder keySb = new StringBuilder();
        if (Srl.is_NotNull_and_NotTrimmedEmpty(packageName)) {
            keySb.append(packageName).append(".");
        }
        if (Srl.is_NotNull_and_NotTrimmedEmpty(procedureName)) {
            keySb.append(procedureName).append(".");
        }
        // DBFlute treats overload methods as one method
        //if (Srl.is_NotNull_and_NotTrimmedEmpty(overload)) {
        //    keySb.append(overload).append(".");
        //}
        return keySb.toString();
    }

    public static class ProcedureNativeInfo {
        protected String _packageName;
        protected String _procedureName;
        protected final List<DfProcedureArgumentInfo> _argInfoList = DfCollectionUtil.newArrayList();

        public void acceptArgInfoList(List<DfProcedureArgumentInfo> argInfoList) {
            for (DfProcedureArgumentInfo argInfo : argInfoList) {
                addArgInfo(argInfo);
            }
        }

        public String getPackageName() {
            return _packageName;
        }

        public void setPackageName(String packageName) {
            this._packageName = packageName;
        }

        public String getProcedureName() {
            return _procedureName;
        }

        public void setProcedureName(String procedureName) {
            this._procedureName = procedureName;
        }

        public List<DfProcedureArgumentInfo> getArgInfoList() {
            return _argInfoList;
        }

        public void addArgInfo(DfProcedureArgumentInfo argInfo) {
            this._argInfoList.add(argInfo);
        }
    }

    // ===================================================================================
    //                                                                       Argument Info
    //                                                                       =============
    protected Map<String, List<DfProcedureArgumentInfo>> selectProcedureArgumentInfoMap(UnifiedSchema unifiedSchema) {
        final DfProcedureParameterNativeExtractorOracle extractor = new DfProcedureParameterNativeExtractorOracle(
                _dataSource, _suppressLogging);
        final List<DfProcedureArgumentInfo> allArgList = extractor.extractProcedureArgumentInfoList(unifiedSchema);
        return arrangeProcedureArgumentInfoMap(allArgList);
    }

    protected Map<String, List<DfProcedureArgumentInfo>> selectDBLinkProcedureArgumentInfoMap(String dbLinkName) {
        final DfProcedureParameterNativeExtractorOracle extractor = new DfProcedureParameterNativeExtractorOracle(
                _dataSource, _suppressLogging);
        final List<DfProcedureArgumentInfo> allArgList = extractor.extractProcedureArgumentInfoToDBLinkList(dbLinkName);
        return arrangeProcedureArgumentInfoMap(allArgList);
    }

    protected Map<String, List<DfProcedureArgumentInfo>> arrangeProcedureArgumentInfoMap(
            List<DfProcedureArgumentInfo> allArgList) {
        final Map<String, List<DfProcedureArgumentInfo>> map = DfCollectionUtil.newLinkedHashMap();
        for (DfProcedureArgumentInfo currentArgInfo : allArgList) {
            final String packageName = currentArgInfo.getPackageName();
            final String procedureName = currentArgInfo.getObjectName();
            // DBFlute treats overload methods as one method
            //final String overload = currentArgInfo.getOverload();
            final String key = generateNativeInfoMapKey(packageName, procedureName);
            List<DfProcedureArgumentInfo> argList = map.get(key);
            if (argList == null) {
                argList = DfCollectionUtil.newArrayList();
                map.put(key, argList);
            }
            argList.add(currentArgInfo);
        }
        return map;
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
