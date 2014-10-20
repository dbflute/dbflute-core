/*
 * Copyright 2014-2014 the original author or authors.
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
package org.dbflute.logic.jdbc.metadata.procedure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.torque.engine.database.model.UnifiedSchema;
import org.dbflute.helper.jdbc.facade.DfJdbcFacade;
import org.dbflute.logic.jdbc.metadata.info.DfProcedureSourceInfo;
import org.dbflute.logic.jdbc.metadata.info.DfTypeArrayInfo;
import org.dbflute.logic.jdbc.metadata.info.DfTypeStructInfo;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 0.9.9.7F (2012/08/22 Wednesday)
 */
public abstract class DfProcedureSupplementExtractorBase implements DfProcedureSupplementExtractor {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger _log = LoggerFactory.getLogger(DfProcedureSupplementExtractorBase.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                                 Basic
    //                                                 -----
    protected final DataSource _dataSource;

    // -----------------------------------------------------
    //                                       ResultMap Cache
    //                                       ---------------
    protected final Map<UnifiedSchema, Map<String, DfProcedureSourceInfo>> _procedureSourceMapMap = newHashMap();

    // -----------------------------------------------------
    //                                                Option
    //                                                ------
    protected boolean _suppressLogging;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfProcedureSupplementExtractorBase(DataSource dataSource) {
        _dataSource = dataSource;
    }

    // ===================================================================================
    //                                                              Default Implementation
    //                                                              ======================
    public Map<String, Integer> extractParameterOverloadInfoMap(UnifiedSchema unifiedSchema) {
        return DfCollectionUtil.emptyMap();
    }

    public Map<String, DfTypeArrayInfo> extractParameterArrayInfoMap(UnifiedSchema unifiedSchema) {
        return DfCollectionUtil.emptyMap();
    }

    public Map<String, DfTypeStructInfo> extractStructInfoMap(UnifiedSchema unifiedSchema) {
        return DfCollectionUtil.emptyMap();
    }

    public String generateParameterInfoMapKey(String catalog, String procedureName, String parameterName) {
        return null;
    }

    // ===================================================================================
    //                                                                         Source Info
    //                                                                         ===========
    /**
     * {@inheritDoc}
     */
    public Map<String, DfProcedureSourceInfo> extractProcedureSourceInfo(UnifiedSchema unifiedSchema) {
        final Map<String, DfProcedureSourceInfo> cachedMap = _procedureSourceMapMap.get(unifiedSchema);
        if (cachedMap != null) {
            return cachedMap;
        }
        final Map<String, DfProcedureSourceInfo> resultMap = doExtractProcedureSourceInfo(unifiedSchema);
        _procedureSourceMapMap.put(unifiedSchema, resultMap);
        return _procedureSourceMapMap.get(unifiedSchema);
    }

    protected abstract Map<String, DfProcedureSourceInfo> doExtractProcedureSourceInfo(UnifiedSchema unifiedSchema);

    protected Integer calculateSourceLine(String sourceCode) {
        return sourceCode != null ? (Srl.count(sourceCode, "\n") + 1) : null;
    }

    protected Integer calculateSourceSize(String sourceCode) {
        return sourceCode != null ? sourceCode.length() : 0;
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

    public void suppressLogging() {
        _suppressLogging = true;
    }

    // ===================================================================================
    //                                                                       Select Facade
    //                                                                       =============
    protected List<Map<String, String>> selectStringList(String sql, List<String> columnList) {
        final DfJdbcFacade facade = new DfJdbcFacade(_dataSource);
        try {
            log(sql);
            return facade.selectStringList(sql, columnList);
        } catch (RuntimeException continued) { // because of supplement
            log("*Failed to select supplement meta: " + continued.getMessage());
            return DfCollectionUtil.newArrayList();
        }
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected <KEY, VALUE> HashMap<KEY, VALUE> newHashMap() {
        return DfCollectionUtil.newHashMap();
    }
}
