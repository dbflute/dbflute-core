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
package org.dbflute.logic.doc.decomment.glance;

import java.util.List;
import java.util.Map;

import org.dbflute.helper.StringKeyMap;
import org.dbflute.infra.doc.decomment.DfDecoMapPickup;
import org.dbflute.infra.doc.decomment.parts.DfDecoMapColumnPart;
import org.dbflute.infra.doc.decomment.parts.DfDecoMapPropertyPart;
import org.dbflute.infra.doc.decomment.parts.DfDecoMapTablePart;
import org.dbflute.logic.doc.decomment.DfDecommentPickupProcess;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.3.0 (2025/07/21 Monday at ichihara)
 */
public class DfDecommentGlancePickup { // basically used by Java code for e.g. alias on decomment

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final DfDecommentGlancePickup _instance = new DfDecommentGlancePickup();

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected DfDecoMapPickup _glancePickup; // not null after lazy-loaded
    protected Map<String, DfGlanceDecommentTable> _tableMap; // not null after lazy-loaded

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    protected DfDecommentGlancePickup() {
    }

    public static DfDecommentGlancePickup getInstance() {
        return _instance;
    }

    // ===================================================================================
    //                                                                      Find Decomment
    //                                                                      ==============
    public String findUnifiedTableDecomment(String tableDbName) { // null allowed
        final Map<String, DfGlanceDecommentTable> tableMap = prepareTableMap();
        final DfGlanceDecommentTable table = tableMap.get(tableDbName);
        if (table == null) {
            return null;
        }
        return table.findUnifiedTableDecomment();
    }

    public String findUnifiedColumnDecomment(String tableDbName, String columnDbName) { // null allowed
        final Map<String, DfGlanceDecommentTable> tableMap = prepareTableMap();
        final DfGlanceDecommentTable table = tableMap.get(tableDbName);
        if (table == null) {
            return null;
        }
        final Map<String, DfGlanceDecommentColumn> columnMap = table.getColumnMap();
        final DfGlanceDecommentColumn column = columnMap.get(columnDbName);
        if (column == null) {
            return null;
        }
        return column.findUnifiedColumnDecomment();
    }

    // ===================================================================================
    //                                                                              Glance
    //                                                                              ======
    /**
     * @return The pickup object for display e.g. SchemaHTML (NotNull, EmptyAllowed: no table)
     */
    protected DfDecoMapPickup prepareGlancePickup() { // without write operation
        if (_glancePickup != null) {
            return _glancePickup;
        }
        final String clientPath = "."; // fixedly current client
        _glancePickup = new DfDecommentPickupProcess().glanceDecomment(clientPath);
        if (_glancePickup == null) { // no way
            throw new IllegalStateException("Not found the glanced decomment.");
        }
        return _glancePickup;
    }

    protected Map<String, DfGlanceDecommentTable> prepareTableMap() {
        if (_tableMap != null) {
            return _tableMap;
        }
        final Map<String, DfGlanceDecommentTable> workingMap = StringKeyMap.createAsFlexibleOrdered();
        final List<DfDecoMapTablePart> tableList = prepareGlancePickup().getTableList();
        for (DfDecoMapTablePart tablePart : tableList) {
            final String tableName = tablePart.getTableName();
            if (workingMap.containsKey(tableName)) { // no way, just in case
                continue; // first appeared prior
            }
            final Map<String, DfGlanceDecommentColumn> columnPartMap = StringKeyMap.createAsFlexibleOrdered();
            final List<DfDecoMapColumnPart> columnList = tablePart.getColumnList();
            for (DfDecoMapColumnPart columnPart : columnList) {
                final String columnName = columnPart.getColumnName();
                if (columnPartMap.containsKey(columnName)) { // no way, just in case
                    continue; // first appeared prior
                }
                final DfGlanceDecommentColumn column = new DfGlanceDecommentColumn(columnPart);
                columnPartMap.put(columnName, column);
            }
            final DfGlanceDecommentTable decommentTable = new DfGlanceDecommentTable(tablePart, columnPartMap);
            workingMap.put(tableName, decommentTable);
        }
        _tableMap = workingMap;
        return _tableMap;
    }

    public static class DfGlanceDecommentTable {

        protected final DfDecoMapTablePart _tablePart; // not null
        protected final Map<String, DfGlanceDecommentColumn> _columnMap; // not null

        public DfGlanceDecommentTable(DfDecoMapTablePart tablePart, Map<String, DfGlanceDecommentColumn> columnPartMap) {
            _tablePart = tablePart;
            _columnMap = columnPartMap;
        }

        public String findUnifiedTableDecomment() { // null allowed
            return buildUnifiedDecomment(_tablePart.getPropertyList());
        }

        public DfDecoMapTablePart getTablePart() {
            return _tablePart;
        }

        public Map<String, DfGlanceDecommentColumn> getColumnMap() {
            return _columnMap;
        }
    }

    public static class DfGlanceDecommentColumn {

        protected final DfDecoMapColumnPart _columnPart; // not null

        public DfGlanceDecommentColumn(DfDecoMapColumnPart columnPart) {
            _columnPart = columnPart;
        }

        public String findUnifiedColumnDecomment() {
            return buildUnifiedDecomment(_columnPart.getPropertyList());
        }

        public DfDecoMapColumnPart getColumnPart() {
            return _columnPart;
        }
    }

    protected static String buildUnifiedDecomment(List<DfDecoMapPropertyPart> propertyList) {
        if (propertyList.isEmpty()) {
            return null;
        }
        // if two or more, the table or column have conflicted decomments
        final StringBuilder sb = new StringBuilder();
        for (DfDecoMapPropertyPart propertyPart : propertyList) {
            if (sb.length() >= 1) {
                sb.append("\n---\n"); // conflict delimiter
            }
            final String decomment = propertyPart.getDecomment();
            if (Srl.is_NotNull_and_NotTrimmedEmpty(decomment)) { // basically exists, just in case
                sb.append(decomment);
            }
        }
        return sb.length() >= 1 ? sb.toString() : null; // just in case
    }
}
