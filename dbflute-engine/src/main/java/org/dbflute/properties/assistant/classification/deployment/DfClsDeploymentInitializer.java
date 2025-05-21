/*
 * Copyright 2014-2025 the original author or authors.
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
package org.dbflute.properties.assistant.classification.deployment;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.torque.engine.database.model.Column;
import org.apache.torque.engine.database.model.Database;
import org.apache.torque.engine.database.model.ForeignKey;
import org.apache.torque.engine.database.model.Table;
import org.dbflute.helper.function.IndependentProcessor;
import org.dbflute.properties.assistant.classification.DfClassificationElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 1.2.5 split from DfClassificationProperties (2021/07/10 Saturday at ikspiari)
 */
public class DfClsDeploymentInitializer {

    private static final Logger _log = LoggerFactory.getLogger(DfClsDeploymentInitializer.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Map<String, Map<String, String>> _existingDeploymentMap;
    protected final Map<String, DfClassificationElement> _tableClassificationMap;
    protected final Map<String, String> _allColumnClassificationMap;
    protected final IndependentProcessor _classificationDefinitionInitializer;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfClsDeploymentInitializer(Map<String, Map<String, String>> existingDeploymentMap,
            Map<String, DfClassificationElement> tableClassificationMap, Map<String, String> allColumnClassificationMap,
            IndependentProcessor classificationDefinitionInitializer) {
        _existingDeploymentMap = existingDeploymentMap;
        _tableClassificationMap = tableClassificationMap;
        _allColumnClassificationMap = allColumnClassificationMap;
        _classificationDefinitionInitializer = classificationDefinitionInitializer;
    }

    // ===================================================================================
    //                                                                          Initialize
    //                                                                          ==========
    public Map<String, Map<String, String>> initializeClassificationDeployment(Database database) {
        // this should be called immediately after creating schema data
        // it should be called after additional-foreign-key initialization
        // so no modification for now
        _log.info("...Initializing ClassificationDeployment: project={}", database.getProjectName());

        // only overridden if called with same database
        if (_allColumnClassificationMap != null) {
            final List<Table> tableList = database.getTableList();
            for (Table table : tableList) {
                final Map<String, String> columnClsMap = getColumnClsMap(_existingDeploymentMap, table.getTableDbName());
                for (Entry<String, String> entry : _allColumnClassificationMap.entrySet()) {
                    columnClsMap.put(entry.getKey(), entry.getValue());
                }
            }
        }
        _classificationDefinitionInitializer.process();
        for (Entry<String, DfClassificationElement> entry : _tableClassificationMap.entrySet()) {
            final DfClassificationElement element = entry.getValue();
            if (element.getClassificationTop().isSuppressAutoDeploy()) {
                continue;
            }
            final Map<String, String> columnClsMap = getColumnClsMap(_existingDeploymentMap, element.getTable());
            final String classificationName = element.getClassificationName();
            registerColumnClsIfNeeds(columnClsMap, element.getCode(), classificationName);
            final Table table = database.getTable(element.getTable());
            if (table == null || table.hasCompoundPrimaryKey()) {
                continue;
            }
            final Column column = table.getColumn(element.getCode());
            if (column == null || !column.isPrimaryKey()) {
                continue;
            }
            final List<ForeignKey> referrers = column.getReferrers();
            for (ForeignKey referrer : referrers) {
                if (!referrer.isSimpleKeyFK()) {
                    continue;
                }
                final Table referrerTable = referrer.getTable();
                final String referrerTableDbName = referrerTable.getTableDbName();
                final Map<String, String> referrerClsMap = getColumnClsMap(_existingDeploymentMap, referrerTableDbName);
                final Column localColumnAsOne = referrer.getLocalColumnAsOne();
                registerColumnClsIfNeeds(referrerClsMap, localColumnAsOne.getName(), classificationName);
            }
        }
        return _existingDeploymentMap;
    }

    protected Map<String, String> getColumnClsMap(Map<String, Map<String, String>> deploymentMap, String tableName) {
        Map<String, String> columnClassificationMap = deploymentMap.get(tableName);
        if (columnClassificationMap == null) {
            // It's normal map because this column name key contains hint.
            columnClassificationMap = new LinkedHashMap<String, String>();
            deploymentMap.put(tableName, columnClassificationMap);
        }
        return columnClassificationMap;
    }

    protected void registerColumnClsIfNeeds(Map<String, String> columnClsMap, String columnName, String classificationName) {
        final String value = columnClsMap.get(columnName);
        if (value != null) {
            return;
        }
        columnClsMap.put(columnName, classificationName);
    }
}
