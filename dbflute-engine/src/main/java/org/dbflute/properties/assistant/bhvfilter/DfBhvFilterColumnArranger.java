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
package org.dbflute.properties.assistant.bhvfilter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.torque.engine.database.model.Column;
import org.apache.torque.engine.database.model.Table;
import org.dbflute.DfBuildProperties;
import org.dbflute.properties.DfBehaviorFilterProperties;

/**
 * @author jflute
 * @since 1.3.1 split from Table (2025/12/30 Tuesday at ichihara)
 */
public class DfBhvFilterColumnArranger {

    // ===================================================================================
    //                                                                       Insert Column
    //                                                                       =============
    public List<Column> arrangeBeforeInsertColumnList(Table table) {
        final DfBehaviorFilterProperties prop = getBehaviorFilterProperties();
        final Map<String, Object> map = prop.getBeforeInsertMap();
        final Set<String> columnNameSet = map.keySet();
        final List<Column> insertColumnList = new ArrayList<Column>();
        final Set<String> commonColumnNameSet = new HashSet<String>();
        if (table.hasAllCommonColumn()) {
            final List<Column> commonColumnList = table.getCommonColumnList();
            for (Column commonColumn : commonColumnList) {
                commonColumnNameSet.add(commonColumn.getName());
            }
        }
        for (String columnName : columnNameSet) {
            final Column column = table.getColumn(columnName);
            if (column != null && !commonColumnNameSet.contains(columnName)) {
                insertColumnList.add(column);
                final String expression = (String) map.get(columnName);
                if (expression == null || expression.trim().length() == 0) {
                    String msg = "The value expression was not found in beforeInsertMap: column=" + column;
                    throw new IllegalStateException(msg);
                }
                column.setBehaviorFilterBeforeInsertColumnExpression(expression);
            }
        }
        return insertColumnList;
    }

    // ===================================================================================
    //                                                                       Update Column
    //                                                                       =============
    public List<Column> arrangeBeforeUpdateColumnList(Table table) {
        final DfBehaviorFilterProperties prop = getProperties().getBehaviorFilterProperties();
        final Map<String, Object> map = prop.getBeforeUpdateMap();
        final Set<String> columnNameSet = map.keySet();
        final List<Column> updateColumnList = new ArrayList<Column>();
        final Set<String> commonColumnNameSet = new HashSet<String>();
        if (table.hasAllCommonColumn()) {
            final List<Column> commonColumnList = table.getCommonColumnList();
            for (Column commonColumn : commonColumnList) {
                commonColumnNameSet.add(commonColumn.getName());
            }
        }
        for (String columnName : columnNameSet) {
            final Column column = table.getColumn(columnName);
            if (column != null && !commonColumnNameSet.contains(columnName)) {
                updateColumnList.add(column);
                String expression = (String) map.get(columnName);
                if (expression == null || expression.trim().length() == 0) {
                    String msg = "The value expression was not found in beforeUpdateMap: column=" + column;
                    throw new IllegalStateException(msg);
                }
                column.setBehaviorFilterBeforeUpdateColumnExpression(expression);
            }
        }
        return updateColumnList;
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfBehaviorFilterProperties getBehaviorFilterProperties() {
        return getProperties().getBehaviorFilterProperties();
    }

    protected DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }
}
