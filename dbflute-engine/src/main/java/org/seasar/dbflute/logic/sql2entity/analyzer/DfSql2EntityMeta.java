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
package org.seasar.dbflute.logic.sql2entity.analyzer;

import java.util.Map;

import org.seasar.dbflute.logic.sql2entity.cmentity.DfCustomizeEntityInfo;
import org.seasar.dbflute.logic.sql2entity.pmbean.DfPmbMetaData;
import org.seasar.dbflute.util.DfCollectionUtil;

/**
 * @author jflute
 */
public class DfSql2EntityMeta {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // key=entityName
    // an entity name for a map key is a name not resolved about project prefix
    // (the prefix is resolved in Table class)
    protected final Map<String, DfCustomizeEntityInfo> _entityInfoMap = DfCollectionUtil.newLinkedHashMap();

    // key=pmbName
    protected final Map<String, DfPmbMetaData> _pmbMetaDataMap = DfCollectionUtil.newLinkedHashMap();

    // key=fileName
    protected final Map<String, String> _exceptionInfoMap = DfCollectionUtil.newLinkedHashMap();

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public Map<String, DfCustomizeEntityInfo> getEntityInfoMap() {
        return _entityInfoMap;
    }

    public void addEntityInfo(String entityName, DfCustomizeEntityInfo entityInfo) {
        _entityInfoMap.put(entityName, entityInfo);
    }

    public Map<String, DfPmbMetaData> getPmbMetaDataMap() {
        return _pmbMetaDataMap;
    }

    public void addPmbMetaData(String pmbName, DfPmbMetaData pmbMetaData) {
        _pmbMetaDataMap.put(pmbName, pmbMetaData);
    }

    public Map<String, String> getExceptionInfoMap() {
        return _exceptionInfoMap;
    }

    public void addExceptionInfo(String fileName, String exceptionInfo) {
        _exceptionInfoMap.put(fileName, exceptionInfo);
    }
}
