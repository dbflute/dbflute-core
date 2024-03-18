/*
 * Copyright 2014-2024 the original author or authors.
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
package org.dbflute.logic.jdbc.metadata.sequence;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.torque.engine.database.model.UnifiedSchema;
import org.dbflute.helper.jdbc.facade.DfJdbcFacade;
import org.dbflute.logic.jdbc.metadata.DfAbstractMetaDataExtractor;
import org.dbflute.logic.jdbc.metadata.info.DfSequenceMeta;
import org.dbflute.util.DfCollectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 0.9.6.4 (2010/01/16 Saturday)
 */
public abstract class DfSequenceExtractorBase extends DfAbstractMetaDataExtractor implements DfSequenceExtractor {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger _log = LoggerFactory.getLogger(DfSequenceExtractorBase.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected DataSource _dataSource;
    protected List<UnifiedSchema> _unifiedSchemaList;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfSequenceExtractorBase(DataSource dataSource, List<UnifiedSchema> unifiedSchemaList) {
        _dataSource = dataSource;
        _unifiedSchemaList = unifiedSchemaList;
    }

    // ===================================================================================
    //                                                                        Sequence Map
    //                                                                        ============
    public Map<String, DfSequenceMeta> extractSequenceMap() {
        return doGetSequenceMap();
    }

    protected abstract Map<String, DfSequenceMeta> doGetSequenceMap();

    protected String buildSequenceMapKey(String catalog, String schema, String name) {
        return (catalog != null ? catalog + "." : "") + (schema != null ? schema + "." : "") + name;
    }

    // ===================================================================================
    //                                                                       Select Facade
    //                                                                       =============
    protected List<Map<String, String>> selectStringList(String sql, List<String> columnList) {
        final DfJdbcFacade facade = new DfJdbcFacade(_dataSource);
        try {
            _log.info(sql);
            return facade.selectStringList(sql, columnList);
        } catch (RuntimeException continued) { // because of supplement
            _log.info("*Failed to select sequence meta: " + continued.getMessage());
            return DfCollectionUtil.newArrayList();
        }
    }
}