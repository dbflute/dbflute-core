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
package org.dbflute.logic.doc.schemahtml;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.torque.engine.database.model.UnifiedSchema;
import org.dbflute.DfBuildProperties;
import org.dbflute.helper.jdbc.context.DfSchemaSource;
import org.dbflute.logic.jdbc.metadata.basic.DfProcedureExtractor;
import org.dbflute.logic.jdbc.metadata.info.DfProcedureMeta;
import org.dbflute.properties.DfDocumentProperties;
import org.dbflute.util.DfCollectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 1.1.3 (2017/03/19 Sunday)
 */
public class DfSchemaHtmlDataProcedure {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger _log = LoggerFactory.getLogger(DfSchemaHtmlDataProcedure.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected List<DfProcedureMeta> _procedureMetaInfoList;
    protected Map<String, List<DfProcedureMeta>> _schemaProcedureMap;

    // ===================================================================================
    //                                                                 Available Procedure
    //                                                                 ===================
    public List<DfProcedureMeta> getAvailableProcedureList(Supplier<DfSchemaSource> dataSourceProvider) throws SQLException {
        if (_procedureMetaInfoList != null) {
            return _procedureMetaInfoList;
        }
        _log.info(" ");
        _log.info("...Setting up procedures for documents");
        final DfProcedureExtractor handler = new DfProcedureExtractor();
        final DfSchemaSource dataSource = dataSourceProvider.get();
        handler.includeProcedureSynonym(dataSource);
        handler.includeProcedureToDBLink(dataSource);
        if (getDocumentProperties().isShowSchemaHtmlProcedureRegardlessOfGeneration()) {
            handler.suppressGenerationRestriction();
        }
        _procedureMetaInfoList = handler.getAvailableProcedureList(dataSource); // ordered by schema
        return _procedureMetaInfoList;
    }

    public Map<String, List<DfProcedureMeta>> getAvailableSchemaProcedureMap(Supplier<DfSchemaSource> dataSourceProvider)
            throws SQLException {
        if (_schemaProcedureMap != null) {
            return _schemaProcedureMap;
        }
        final List<DfProcedureMeta> procedureList = getAvailableProcedureList(dataSourceProvider);
        final Map<String, List<DfProcedureMeta>> schemaProcedureListMap = DfCollectionUtil.newLinkedHashMap();
        final String mainName = "(main schema)";
        for (DfProcedureMeta meta : procedureList) {
            final UnifiedSchema procedureSchema = meta.getProcedureSchema();
            final String schemaName;
            if (procedureSchema != null) {
                final String drivenSchema = procedureSchema.getDrivenSchema();
                if (drivenSchema != null) {
                    schemaName = drivenSchema;
                } else {
                    schemaName = procedureSchema.isMainSchema() ? mainName : procedureSchema.getSqlPrefixSchema();
                }
            } else {
                schemaName = "(no schema)";
            }
            List<DfProcedureMeta> metaList = schemaProcedureListMap.get(schemaName);
            if (metaList == null) {
                metaList = DfCollectionUtil.newArrayList();
                schemaProcedureListMap.put(schemaName, metaList);
            }
            metaList.add(meta);
        }
        _schemaProcedureMap = schemaProcedureListMap;
        return _schemaProcedureMap;
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    protected DfDocumentProperties getDocumentProperties() {
        return getProperties().getDocumentProperties();
    }
}
