/*
 * Copyright 2014-2019 the original author or authors.
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
package org.dbflute.properties.initializer;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.torque.engine.database.model.Column;
import org.apache.torque.engine.database.model.Table;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.properties.DfIncludeQueryProperties;
import org.dbflute.properties.assistant.base.DfTableFinder;
import org.dbflute.util.DfNameHintUtil;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 0.7.3 (2008/05/30 Friday)
 */
public class DfIncludeQueryInitializer {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The logger instance for this class. (NotNull) */
    private static final Logger _log = LoggerFactory.getLogger(DfIncludeQueryInitializer.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected DfIncludeQueryProperties _includeQueryProperties;

    protected DfTableFinder _tableFinder;

    // ===================================================================================
    //                                                                          Initialize
    //                                                                          ==========
    public void initializeIncludeQuery() {
        final Map<String, Map<String, Map<String, List<String>>>> includeQueryMap = _includeQueryProperties.getIncludeQueryMap();
        if (!includeQueryMap.isEmpty()) {
            _log.info("/=============================");
            _log.info("...Initializing include query.");
            checkQueryMap(includeQueryMap);
            _log.info("========/");
        }
        final Map<String, Map<String, Map<String, List<String>>>> excludeQueryMap = _includeQueryProperties.getExcludeQueryMap();
        if (!excludeQueryMap.isEmpty()) {
            _log.info("/=============================");
            _log.info("...Initializing exclude query.");
            checkQueryMap(excludeQueryMap);
            _log.info("========/");
        }
    }

    protected void checkQueryMap(Map<String, Map<String, Map<String, List<String>>>> map) {
        final String allMark = DfIncludeQueryProperties.ALL_MARK;
        final String commonColumnMark = DfIncludeQueryProperties.COMMON_COLUMN_MARK;
        final String versionNoMark = DfIncludeQueryProperties.VERSION_NO_MARK;
        final String typeMark = DfIncludeQueryProperties.TYPE_MARK;
        final String[] hintMarks = DfNameHintUtil.getMarkList().toArray(new String[] {});
        for (Entry<String, Map<String, Map<String, List<String>>>> entry : map.entrySet()) {
            final String propType = entry.getKey();
            final Map<String, Map<String, List<String>>> ckeyMap = entry.getValue();
            _log.info(propType);
            for (Entry<String, Map<String, List<String>>> ckeyEntry : ckeyMap.entrySet()) {
                final String ckey = ckeyEntry.getKey();
                final Map<String, List<String>> tableColumnMap = ckeyEntry.getValue();
                final Set<String> tableElementKeySet = tableColumnMap.keySet();
                _log.info("  " + ckey + " -> " + tableElementKeySet);
                for (String tableName : tableElementKeySet) {
                    final boolean allTable = tableName.equalsIgnoreCase(allMark);
                    final boolean markTable = Srl.containsAnyIgnoreCase(tableName, hintMarks);
                    final boolean pureTable = !allTable && !markTable;
                    Table targetTable = null;
                    if (pureTable) {
                        // check existence
                        targetTable = _tableFinder.findTable(tableName);
                        if (targetTable == null) {
                            throwIncludeQueryTableNotFoundException(ckey, tableName, map);
                        }
                    }
                    List<String> columnNameList = null;
                    try {
                        columnNameList = tableColumnMap.get(tableName);
                    } catch (ClassCastException e) { // also type check
                        throwIncludeQueryNotListColumnSpecificationException(ckey, tableName, map, e);
                    }
                    if (pureTable) {
                        for (String columnName : columnNameList) {
                            final boolean commonColumn = columnName.equalsIgnoreCase(commonColumnMark);
                            final boolean versionNo = columnName.equalsIgnoreCase(versionNoMark);
                            final boolean columnType = Srl.containsAnyIgnoreCase(columnName, typeMark);
                            final boolean columnHint = Srl.containsAnyIgnoreCase(columnName, hintMarks);
                            if (!commonColumn && !versionNo && !columnType && !columnHint) {
                                // check existence
                                final Column targetColumn = targetTable.getColumn(columnName);
                                if (targetColumn == null) {
                                    throwIncludeQueryColumnNotFoundException(ckey, tableName, columnName, map);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    protected void throwIncludeQueryTableNotFoundException(String ckey, String tableName,
            Map<String, Map<String, Map<String, List<String>>>> map) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The table in includeQueryMap was not found in the meta data.");
        br.addItem("Condition Key");
        br.addElement(ckey);
        br.addItem("Table Name");
        br.addElement(tableName);
        br.addItem("Query Map");
        br.addElement(map);
        final String msg = br.buildExceptionMessage();
        throw new DfIncludeQueryTableNotFoundException(msg);
    }

    protected void throwIncludeQueryNotListColumnSpecificationException(String ckey, String tableName,
            Map<String, Map<String, Map<String, List<String>>>> map, RuntimeException e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The column specification of the table was not List type in includeQueryMap.");
        br.addItem("Advice");
        br.addElement("You shuold specify them this way:");
        br.addElement("  " + tableName + " = list:{ FOO_ID ; FOO_NAME }");
        br.addItem("Condition Key");
        br.addElement(ckey);
        br.addItem("Table Name");
        br.addElement(tableName);
        br.addItem("Query Map");
        br.addElement(map);
        final String msg = br.buildExceptionMessage();
        throw new DfIncludeQueryNotListColumnSpecificationException(msg, e);
    }

    protected void throwIncludeQueryColumnNotFoundException(String ckey, String tableName, String columnName,
            Map<String, Map<String, Map<String, List<String>>>> map) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The column in includeQueryMap was not found in the meta data.");
        br.addItem("Condition Key");
        br.addElement(ckey);
        br.addItem("Table Name");
        br.addElement(tableName);
        br.addItem("Column Name");
        br.addElement(columnName);
        br.addItem("Query Map");
        br.addElement(map);
        final String msg = br.buildExceptionMessage();
        throw new DfIncludeQueryColumnNotFoundException(msg);
    }

    protected static class DfIncludeQueryTableNotFoundException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public DfIncludeQueryTableNotFoundException(String msg) {
            super(msg);
        }
    }

    protected static class DfIncludeQueryNotListColumnSpecificationException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public DfIncludeQueryNotListColumnSpecificationException(String msg, RuntimeException e) {
            super(msg, e);
        }
    }

    protected static class DfIncludeQueryColumnNotFoundException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public DfIncludeQueryColumnNotFoundException(String msg) {
            super(msg);
        }
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    public String ln() {
        return "\n";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public DfIncludeQueryProperties getIncludeQueryProperties() {
        return _includeQueryProperties;
    }

    public void setIncludeQueryProperties(DfIncludeQueryProperties includeQueryProperties) {
        this._includeQueryProperties = includeQueryProperties;
    }

    public DfTableFinder getTableFinder() {
        return _tableFinder;
    }

    public void setTableFinder(DfTableFinder tableFinder) {
        this._tableFinder = tableFinder;
    }
}
