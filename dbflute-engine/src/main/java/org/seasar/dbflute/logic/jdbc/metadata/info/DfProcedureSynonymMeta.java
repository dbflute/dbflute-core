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
package org.seasar.dbflute.logic.jdbc.metadata.info;

import java.util.List;

import org.apache.torque.engine.database.model.UnifiedSchema;

/**
 * @author jflute
 * @since 0.9.6.2 (2009/12/08 Tuesday)
 */
public class DfProcedureSynonymMeta {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected DfSynonymMeta _synonymMetaInfo;
    protected DfProcedureMeta _procedureMetaInfo;

    // ===================================================================================
    //                                                                              Switch
    //                                                                              ======
    public DfProcedureMeta createMergedProcedure() {
        if (_procedureMetaInfo == null) {
            String msg = "The procedureMetaInfo should not be null!";
            throw new IllegalStateException(msg);
        }
        if (_synonymMetaInfo == null) {
            String msg = "The synonymMetaInfo should not be null!";
            throw new IllegalStateException(msg);
        }
        final DfProcedureMeta metaInfo = new DfProcedureMeta();
        final UnifiedSchema synonymOwner = _synonymMetaInfo.getSynonymOwner();
        final String synonymName = _synonymMetaInfo.getSynonymName();
        final String synonymFullQualifiedName = _synonymMetaInfo.buildSynonymFullQualifiedName();
        final String synonymSchemaQualifiedName = _synonymMetaInfo.buildSynonymSchemaQualifiedName();
        final String synonymSqlName = _synonymMetaInfo.buildSynonymSqlName();
        metaInfo.setProcedureSchema(synonymOwner);
        metaInfo.setProcedureName(synonymName);
        metaInfo.setProcedureFullQualifiedName(synonymFullQualifiedName);
        metaInfo.setProcedureSchemaQualifiedName(synonymSchemaQualifiedName);
        metaInfo.setProcedureSqlName(synonymSqlName);
        metaInfo.setProcedureSynonym(_procedureMetaInfo.isProcedureSynonym());
        metaInfo.setProcedureType(_procedureMetaInfo.getProcedureType());
        metaInfo.setProcedureComment(_procedureMetaInfo.getProcedureComment());
        final List<DfProcedureColumnMeta> columnMetaInfoList = _procedureMetaInfo.getProcedureColumnList();
        for (DfProcedureColumnMeta columnMetaInfo : columnMetaInfoList) {
            metaInfo.addProcedureColumn(columnMetaInfo);
        }
        return metaInfo;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public DfSynonymMeta getSynonymMetaInfo() {
        return _synonymMetaInfo;
    }

    public void setSynonymMetaInfo(DfSynonymMeta synonymMetaInfo) {
        this._synonymMetaInfo = synonymMetaInfo;
    }

    public DfProcedureMeta getProcedureMetaInfo() {
        return _procedureMetaInfo;
    }

    public void setProcedureMetaInfo(DfProcedureMeta procedureMetaInfo) {
        this._procedureMetaInfo = procedureMetaInfo;
    }
}
