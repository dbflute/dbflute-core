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
package org.seasar.dbflute.logic.jdbc.schemadiff;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.torque.engine.database.model.Column;
import org.apache.torque.engine.database.model.Database;
import org.apache.torque.engine.database.model.Procedure;
import org.apache.torque.engine.database.model.Sequence;
import org.apache.torque.engine.database.model.Table;
import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.logic.jdbc.schemadiff.differ.DfConstraintKeyDiffer;
import org.seasar.dbflute.logic.jdbc.schemadiff.differ.DfForeignKeyDiffer;
import org.seasar.dbflute.logic.jdbc.schemadiff.differ.DfIndexDiffer;
import org.seasar.dbflute.logic.jdbc.schemadiff.differ.DfUniqueKeyDiffer;
import org.seasar.dbflute.logic.jdbc.schemaxml.DfSchemaXmlReader;
import org.seasar.dbflute.properties.DfDocumentProperties;
import org.seasar.dbflute.resource.DBFluteSystem;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.DfTypeUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.9.7.1 (2010/06/06 Sunday)
 */
public class DfSchemaDiff extends DfAbstractDiff {

    //[diff-date] = map:{
    //    ; diffDate = 2010/12/12 12:34:56
    //    ; tableCount = map:{ next = 123 ; previous = 145}
    //    ; tableDiff = map:{
    //        ; [table-name] = map:{
    //            ; diffType = [ADD or CHANGE or DELETE]
    //            ; unifiedSchemaDiff = map:{ next = [schema] ; previous = [schema] }
    //            ; objectTypeDiff = map:{ next = [type] ; previous = [type] }
    //            ; columnDefOrderDiff = map:{ next = [column-index-exp] ; previous = [column-index-exp] }
    //            ; columnDiff = map:{
    //                [column-name] = map:{
    //                    ; diffType = [ADD or CHANGE or DELETE]
    //                    ; dbTypeDiff = map:{next = [db-type-name]; previous = [db-type-name]}
    //                    ; columnSizeDiff = map:{next = [column-size&digit]; previous = [column-size&digit]}
    //                    ; defaultValueDiff = map:{next = [default-value]; previous = [default-value]}
    //                    ; notNullDiff = map:{next = [true or false] ; previous = [true or false]}
    //                    ; autoIncrementDiff = map:{next = [true or false] ; previous = [true or false]}
    //                }
    //            }
    //            ; primaryKeyDiff = map:{
    //                ; [pk-name] = map:{
    //                    ; diffType = [ADD or CHANGE or DELETE]
    //                    ; nameDiff = map:{
    //                        ; next = [constraint-name]
    //                        ; previous = [constraint-name]
    //                    }
    //                    ; columnDiff = map:{
    //                        ; next = [column-name, ...]
    //                        ; previous = [column-name, ...]
    //                    }
    //                }
    //            }
    //            ; foreingkKeyDiff = map:{
    //                ; [fk-name] = map:{
    //                    ; diffType = [ADD or CHANGE or DELETE]
    //                    ; nameDiff = map:{
    //                        ; next = [constraint-name]
    //                        ; previous = [constraint-name]
    //                    }
    //                    ; columnDiff = map:{
    //                        ; next = [column-name, ...]
    //                        ; previous = [column-name, ...]
    //                    }
    //                    ; foreignTableDiff = map:{
    //                        ; next = [table-name]
    //                        ; previous = [table-name]
    //                    }
    //                }
    //            }
    //            ; uniqueKeyDiff = map:{
    //                ; [uq-name] = map:{
    //                    ; diffType = [ADD or CHANGE or DELETE]
    //                    ; nameDiff = map:{
    //                        ; next = [constraint-name]
    //                        ; previous = [constraint-name]
    //                    }
    //                    ; columnDiff = map:{
    //                        ; next = [column-name, ...]
    //                        ; previous = [column-name, ...]
    //                    }
    //                }
    //            }
    //            ; indexDiff = map:{
    //                ; [index-name] = map:{
    //                    ; diffType = [ADD or CHANGE or DELETE]
    //                    ; nameDiff = map:{
    //                        ; next = [constraint-name]
    //                        ; previous = [constraint-name]
    //                    }
    //                    ; columnDiff = map:{
    //                        ; next = [column-name, ...]
    //                        ; previous = [column-name, ...]
    //                    }
    //                }
    //            }
    //        }
    //    }
    //    ; sequenceDiff = map:{
    //        ; [sequence-name] = map:{
    //            ; diffType = [ADD or CHANGE or DELETE]
    //            ; unifiedSchemaDiff = map:{ next = [schema] ; previous = [schema] }
    //            ; minimumValueDiff = map:{ next = [value] ; previous = [value] }
    //            ; maximumValueDiff = map:{ next = [value] ; previous = [value] }
    //            ; incrementSizeDiff = map:{ next = [value] ; previous = [value] }
    //            ; sequenceCommentDiff = map:{ next = [comment] ; previous = [comment] }
    //        }
    //    }
    //    ; procedureDiff = map:{
    //        ; [procedure-name] = map:{
    //            ; diffType = [ADD or CHANGE or DELETE]
    //            ; unifiedSchemaDiff = map:{ next = [schema] ; previous = [schema] }
    //            ; sourceLineDiff = map:{ next = [value] ; previous = [value] }
    //            ; sourceSizeDiff = map:{ next = [value] ; previous = [value] }
    //            ; sourceHashDiff = map:{ next = [value] ; previous = [value] }
    //            ; procedureCommentDiff = map:{ next = [comment] ; previous = [comment] }
    //        }
    //    }
    //    ; craftDiff = map:{
    //        ; [craft-title] = map:{
    //            ; craftRowDiff = map:{
    //                ; [craft-key] = map:{
    //                    ; diffType = [ADD or CHANGE or DELETE]
    //                    ; craftValueDiff = map:{ next = [value] ; previous = [value] } 
    //                }
    //            }
    //        }
    //    }
    //}
    //
    // sequenceDiff and procedureDiff have no-diff-suffix items, sourceLine, sourceSize and so on,
    // because I forgot to add the suffix, however no longer fix...

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String DIFF_DATE_KEY = "diffDate";
    public static final String DIFF_DATE_PATTERN = "yyyy/MM/dd HH:mm:ss";
    public static final String COMMENT_KEY = "comment";
    public static final String TABLE_COUNT_KEY = "tableCount";
    public static final String TABLE_DIFF_KEY = "tableDiff";
    public static final String SEQUENCE_DIFF_KEY = "sequenceDiff";
    public static final String KEYWORD_DB2_SYSTEM_SEQUENCE = "SQL";
    public static final String KEYWORD_H2_SYSTEM_SEQUENCE = "SYSTEM_SEQUENCE";
    public static final String PROCEDURE_DIFF_KEY = "procedureDiff";
    public static final String CRAFT_DIFF_KEY = "craftDiff";
    public static final String PROCEDURE_SOURCE_NO_META_MARK = "-1";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                           Load Schema
    //                                           -----------
    protected final DfSchemaXmlReader _nextReader;
    protected final DfSchemaXmlReader _previousReader;
    protected Database _nextDb; // not null after next loading
    protected Database _previousDb; // not null after previous loading
    protected Integer _previousTableCount; // not null after previous loading
    protected boolean _firstTime; // judged when loading previous schema
    protected boolean _loadingFailure; // judged when loading previous schema

    // -----------------------------------------------------
    //                                                 Basic
    //                                                 -----
    protected Date _diffDate; // not null after loading next schema
    protected String _comment; // after restoring
    protected DfNextPreviousDiff _tableCountDiff; // not null after next loading

    // -----------------------------------------------------
    //                                                Option
    //                                                ------
    protected boolean _checkColumnDefOrder; // depends on DBFlute property
    protected boolean _checkDbComment; // depends on DBFlute property
    protected boolean _suppressSchema; // basically for SchemaSyncCheck

    // -----------------------------------------------------
    //                                            Table Diff
    //                                            ----------
    protected final List<DfTableDiff> _tableDiffAllList = DfCollectionUtil.newArrayList();
    protected final List<DfTableDiff> _addedTableDiffList = DfCollectionUtil.newArrayList();
    protected final List<DfTableDiff> _changedTableDiffList = DfCollectionUtil.newArrayList();
    protected final List<DfTableDiff> _deletedTableDiffList = DfCollectionUtil.newArrayList();

    // -----------------------------------------------------
    //                                         Sequence Diff
    //                                         -------------
    protected final List<DfSequenceDiff> _sequenceDiffAllList = DfCollectionUtil.newArrayList();
    protected final List<DfSequenceDiff> _addedSequenceDiffList = DfCollectionUtil.newArrayList();
    protected final List<DfSequenceDiff> _changedSequenceDiffList = DfCollectionUtil.newArrayList();
    protected final List<DfSequenceDiff> _deletedSequenceDiffList = DfCollectionUtil.newArrayList();

    // -----------------------------------------------------
    //                                        Procedure Diff
    //                                        --------------
    protected final List<DfProcedureDiff> _procedureDiffAllList = DfCollectionUtil.newArrayList();
    protected final List<DfProcedureDiff> _addedProcedureDiffList = DfCollectionUtil.newArrayList();
    protected final List<DfProcedureDiff> _changedProcedureDiffList = DfCollectionUtil.newArrayList();
    protected final List<DfProcedureDiff> _deletedProcedureDiffList = DfCollectionUtil.newArrayList();

    // -----------------------------------------------------
    //                                            Craft Diff
    //                                            ----------
    // these are not null if craft enabled
    protected final DfCraftDiff _craftDiff = new DfCraftDiff();
    protected String _craftMetaDir;

    // -----------------------------------------------------
    //                                             Nest Diff
    //                                             ---------
    protected final List<NestDiffSetupper> _nestDiffList = DfCollectionUtil.newArrayList();
    {
        _nestDiffList.add(new NestDiffSetupper() {
            public String propertyName() {
                return TABLE_DIFF_KEY;
            }

            public List<? extends DfNestDiff> provide() {
                return _tableDiffAllList;
            }

            public void setup(Map<String, Object> diff) {
                addTableDiff(createTableDiff(diff));
            }
        });
        _nestDiffList.add(new NestDiffSetupper() {
            public String propertyName() {
                return SEQUENCE_DIFF_KEY;
            }

            public List<? extends DfNestDiff> provide() {
                return _sequenceDiffAllList;
            }

            public void setup(Map<String, Object> diff) {
                addSequenceDiff(createSequenceDiff(diff));
            }
        });
        _nestDiffList.add(new NestDiffSetupper() {
            public String propertyName() {
                return PROCEDURE_DIFF_KEY;
            }

            public List<? extends DfNestDiff> provide() {
                return _procedureDiffAllList;
            }

            public void setup(Map<String, Object> diff) {
                addProcedureDiff(createProcedureDiff(diff));
            }
        });
        _nestDiffList.add(new NestDiffSetupper() {
            public String propertyName() {
                return CRAFT_DIFF_KEY;
            }

            public List<? extends DfNestDiff> provide() {
                return _craftDiff.getCraftTitleDiffList();
            }

            public void setup(Map<String, Object> diff) {
                _craftDiff.addCraftTitleDiff(createCraftDiffTitle(diff));
            }
        });
    }

    // -----------------------------------------------------
    //                                             Meta Info
    //                                             ---------
    protected boolean _latest;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    protected DfSchemaDiff(DfSchemaXmlReader previousReader, DfSchemaXmlReader nextReader) {
        _previousReader = previousReader;
        _nextReader = nextReader;
    }

    public static DfSchemaDiff createAsHistory() {
        final DfSchemaXmlReader reader = DfSchemaXmlReader.createAsCoreToManage();
        return newReader(reader);
    }

    public static DfSchemaDiff createAsSerializer(String schemaXml) {
        final DfSchemaXmlReader reader = DfSchemaXmlReader.createAsFlexibleToManage(schemaXml);
        return prepareDiffOption(newReader(reader));
    }

    public static DfSchemaDiff createAsAlterCheck(String previousXml, String nextXml) {
        final DfSchemaXmlReader previousReader = DfSchemaXmlReader.createAsFlexibleToManage(previousXml);
        final DfSchemaXmlReader nextReader = DfSchemaXmlReader.createAsFlexibleToManage(nextXml);
        return prepareDiffOption(newReader(previousReader, nextReader));
    }

    protected static DfSchemaDiff newReader(DfSchemaXmlReader reader) {
        return new DfSchemaDiff(reader, reader);
    }

    protected static DfSchemaDiff newReader(DfSchemaXmlReader previousReader, DfSchemaXmlReader nextReader) {
        return new DfSchemaDiff(previousReader, nextReader);
    }

    protected static DfSchemaDiff prepareDiffOption(DfSchemaDiff schemaDiff) {
        // all diff processes are depends on the DBFlute property
        // (CraftDiff settings are set later)
        final DfDocumentProperties prop = DfBuildProperties.getInstance().getDocumentProperties();
        if (prop.isCheckColumnDefOrderDiff()) {
            schemaDiff.checkColumnDefOrder();
        }
        if (prop.isCheckDbCommentDiff()) {
            schemaDiff.checkDbComment();
        }
        return schemaDiff;
    }

    // ===================================================================================
    //                                                                         Load Schema
    //                                                                         ===========
    public void loadPreviousSchema() { // before loading next schema
        final DfSchemaXmlReader reader = _previousReader;
        if (!reader.exists()) {
            _firstTime = true;
            return;
        }
        try {
            _previousDb = reader.read().getDatabase();
        } catch (RuntimeException e) {
            _loadingFailure = true;
            handleReadingException(e, reader);
        }
        _previousTableCount = _previousDb.getTableList().size();
    }

    public void loadNextSchema() { // after loading previous schema
        if (isFirstTime()) {
            String msg = "You should not call this because of first time.";
            throw new IllegalStateException(msg);
        }
        if (_previousDb == null) {
            String msg = "You should not call this because of previous not loaded.";
            throw new IllegalStateException(msg);
        }
        final DfSchemaXmlReader reader = _nextReader;
        try {
            _nextDb = reader.read().getDatabase();
        } catch (RuntimeException e) {
            handleReadingException(e, reader);
        }
        _diffDate = new Date(DBFluteSystem.currentTimeMillis());
        final int nextTableCount = _nextDb.getTableList().size();
        _tableCountDiff = createNextPreviousDiff(nextTableCount, _previousTableCount);
    }

    protected void handleReadingException(Exception e, DfSchemaXmlReader reader) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to load schema XML.");
        br.addItem("SchemaXML");
        br.addElement(reader.getSchemaXml());
        br.addItem("Exception");
        br.addElement(e.getClass().getName());
        br.addElement(e.getMessage());
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg, e);
    }

    // ===================================================================================
    //                                                                        Analyze Diff
    //                                                                        ============
    /**
     * Analyze schema difference between previous and next.
     * <pre>
     * schemaDiff.loadPreviousSchema();
     * schemaDiff.loadNextSchema();
     * schemaDiff.analyzeDiff();
     * </pre>
     */
    public void analyzeDiff() {
        processTable();
        processSequence();
        processProcedure();
        processCraftDiff();
    }

    // ===================================================================================
    //                                                                       Table Process
    //                                                                       =============
    protected void processTable() {
        processAddedTable();
        processChangedTable();
        processDeletedTable();
    }

    // -----------------------------------------------------
    //                                                 Added
    //                                                 -----
    protected void processAddedTable() {
        final List<Table> tableList = _nextDb.getTableList();
        for (Table table : tableList) {
            final Table found = findPreviousTable(table);
            if (found == null || !isSameTableName(table, found)) { // added
                addTableDiff(DfTableDiff.createAdded(table.getTableDbName()));
            }
        }
    }

    // -----------------------------------------------------
    //                                               Changed
    //                                               -------
    protected void processChangedTable() {
        final List<Table> tableList = _nextDb.getTableList();
        for (Table next : tableList) {
            final Table previous = findPreviousTable(next);
            if (previous == null || !isSameTableName(next, previous)) {
                continue;
            }
            // found
            final DfTableDiff tableDiff = DfTableDiff.createChanged(next.getTableDbName());

            // direct attributes
            processUnifiedSchema(next, previous, tableDiff);
            processObjectType(next, previous, tableDiff);
            processColumnDefOrder(next, previous, tableDiff);
            processTableComment(next, previous, tableDiff);

            // nested attributes
            processColumn(tableDiff, next, previous);
            processPrimaryKey(tableDiff, next, previous);
            processForeignKey(tableDiff, next, previous);
            processUniqueKey(tableDiff, next, previous);
            processIndex(tableDiff, next, previous);

            if (tableDiff.hasDiff()) { // changed
                addTableDiff(tableDiff);
            }
        }
    }

    protected void processUnifiedSchema(Table next, Table previous, DfTableDiff tableDiff) {
        if (_suppressSchema) {
            return;
        }
        diffNextPrevious(next, previous, tableDiff, new StringNextPreviousDiffer<Table, DfTableDiff>() {
            public String provide(Table obj) {
                return obj.getUnifiedSchema().getCatalogSchema();
            }

            public void diff(DfTableDiff diff, DfNextPreviousDiff nextPreviousDiff) {
                diff.setUnifiedSchemaDiff(nextPreviousDiff);
            }
        });
    }

    protected void processObjectType(Table next, Table previous, DfTableDiff tableDiff) {
        diffNextPrevious(next, previous, tableDiff, new StringNextPreviousDiffer<Table, DfTableDiff>() {
            public String provide(Table obj) {
                return obj.getType();
            }

            public void diff(DfTableDiff diff, DfNextPreviousDiff nextPreviousDiff) {
                diff.setObjectTypeDiff(nextPreviousDiff);
            }
        });
    }

    protected void processColumnDefOrder(Table next, Table previous, DfTableDiff tableDiff) {
        if (!_checkColumnDefOrder) {
            return;
        }
        diffNextPrevious(next, previous, tableDiff, new ColumnDefOrderDiffer());
    }

    protected static class ColumnDefOrderDiffer implements NextPreviousDiffer<Table, DfTableDiff, Table> {

        private static final String KEY_NEXT_NAME = "nextName";
        private static final String KEY_NEXT_NUMBER = "nextNumber";
        private static final String KEY_PREVIOUS_NAME = "previousName";
        private static final String KEY_PREVIOUS_NUMBER = "previousNumber";

        protected final List<Map<String, Object>> _diffList = DfCollectionUtil.newArrayList();

        public Table provide(Table obj) {
            return obj;
        }

        public boolean isMatch(Table next, Table previous) {
            // compare without added or deleted columns
            // (and renamed columns cannot be cached by DBFlute)
            final List<String> nextList = createCompareColumnList(next, previous);
            final List<String> previousList = createCompareColumnList(previous, next);
            filterCompareColumnList(next, previous, nextList, previousList);
            while (true) {
                final Map<String, Object> foundDiffMap = findColumnDefOrder(next, previous, nextList, previousList);
                if (foundDiffMap == null) {
                    break;
                }
                _diffList.add(foundDiffMap);

                // remove the found column finished being compared
                nextList.remove(foundDiffMap.get(KEY_NEXT_NAME));
                previousList.remove(foundDiffMap.get(KEY_PREVIOUS_NAME));
            }
            return _diffList.isEmpty();
        }

        public void diff(DfTableDiff diff, DfNextPreviousDiff nextPreviousDiff) {
            diff.setColumnDefOrderDiff(nextPreviousDiff);
        }

        public String disp(Table obj, boolean next) {
            if (next) {
                return buildDisp(KEY_NEXT_NAME, KEY_NEXT_NUMBER);
            } else {
                return buildDisp(KEY_PREVIOUS_NAME, KEY_PREVIOUS_NUMBER);
            }
        }

        protected String buildDisp(String nameKey, String numberKey) {
            final StringBuilder sb = new StringBuilder();
            for (Map<String, Object> diffMap : _diffList) {
                final Object name = diffMap.get(nameKey);
                final Object number = diffMap.get(numberKey);
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(name).append("(").append(number).append(")");
            }
            return sb.toString();
        }

        protected List<String> createCompareColumnList(Table main, Table target) {
            // *uses LinkedList because the lists are removed so many times
            final List<String> mainList = new LinkedList<String>();
            for (Column column : main.getColumnList()) {
                final Column corresponding = target.getColumn(column.getName());
                if (corresponding == null) {
                    continue;
                }
                mainList.add(column.getName());
            }
            return mainList;
        }

        protected void filterCompareColumnList(Table next, Table previous, List<String> nextList,
                List<String> previousList) {
            final List<String> removedNextList = DfCollectionUtil.newArrayList();
            final List<String> removedPreviousList = DfCollectionUtil.newArrayList();
            for (int i = 0; i < nextList.size(); i++) {
                final String nextName = nextList.get(i);
                final String previousSameOrderName = previousList.get(i);
                if (nextName.equalsIgnoreCase(previousSameOrderName)) {
                    removedNextList.add(nextName);
                    removedPreviousList.add(previousSameOrderName);
                }
            }
            for (String removedName : removedNextList) {
                nextList.remove(next.getColumn(removedName).getName());
            }
            for (String removedName : removedPreviousList) {
                previousList.remove(previous.getColumn(removedName).getName());
            }
        }

        protected Map<String, Object> findColumnDefOrder(Table next, Table previous, List<String> nextList,
                List<String> previousList) {
            Map<String, Object> resultMap = null;
            for (int i = 0; i < nextList.size(); i++) {
                final String nextName = nextList.get(i);
                final String previousSameOrderName = previousList.get(i);
                if (nextName.equalsIgnoreCase(previousSameOrderName)) {
                    continue; // basically no way because of filtered already
                }
                resultMap = new HashMap<String, Object>();
                resultMap.put(KEY_NEXT_NAME, next.getColumn(nextName).getName());
                resultMap.put(KEY_NEXT_NUMBER, next.getColumnIndex(nextName) + 1);
                final String previousCorrespondingName = previous.getColumn(nextName).getName();
                resultMap.put(KEY_PREVIOUS_NAME, previousCorrespondingName);
                resultMap.put(KEY_PREVIOUS_NUMBER, previous.getColumnIndex(previousCorrespondingName) + 1);
                break;
            }
            return resultMap;
        }
    }

    protected void processTableComment(Table next, Table previous, DfTableDiff tableDiff) {
        if (!_checkDbComment) {
            return;
        }
        diffNextPrevious(next, previous, tableDiff, new StringNextPreviousDiffer<Table, DfTableDiff>() {
            public String provide(Table obj) {
                return obj.getPlainComment();
            }

            public void diff(DfTableDiff diff, DfNextPreviousDiff nextPreviousDiff) {
                diff.setTableCommentDiff(nextPreviousDiff);
            }
        });
    }

    protected <TYPE> void diffNextPrevious(Table next, Table previous, DfTableDiff diff,
            NextPreviousDiffer<Table, DfTableDiff, TYPE> differ) {
        final TYPE nextValue = differ.provide(next);
        final TYPE previousValue = differ.provide(previous);
        if (!differ.isMatch(nextValue, previousValue)) {
            final String nextDisp = differ.disp(nextValue, true);
            final String previousDisp = differ.disp(previousValue, false);
            differ.diff(diff, createNextPreviousDiff(nextDisp, previousDisp));
        }
    }

    // -----------------------------------------------------
    //                                               Deleted
    //                                               -------
    protected void processDeletedTable() {
        final List<Table> tableList = _previousDb.getTableList();
        for (Table table : tableList) {
            final Table found = findNextTable(table);
            if (found == null || !isSameTableName(table, found)) { // deleted
                addTableDiff(DfTableDiff.createDeleted(table.getTableDbName()));
            }
        }
    }

    // -----------------------------------------------------
    //                                         Assist Helper
    //                                         -------------
    /**
     * Find the table from next schema by table object. <br />
     * This method can be used after {@link #loadNextSchema()}.
     * @param table The object of target table that has table name. (NotNull)
     * @return The object of found table. (NullAllowed: if null, not found)
     */
    protected Table findNextTable(Table table) {
        return _nextDb.getTable(table.getTableDbName());
    }

    /**
     * Find the table from previous schema by table object. <br />
     * This method can be used after {@link #loadPreviousSchema()}.
     * @param table The object of target table that has table name. (NotNull)
     * @return The object of found table. (NullAllowed: if null, not found)
     */
    protected Table findPreviousTable(Table table) {
        return findPreviousTable(table.getTableDbName());
    }

    /**
     * Find the table from previous schema by table name. <br />
     * This method can be used after {@link #loadPreviousSchema()}.
     * @param tableDbName The DB name of target table. (NotNull)
     * @return The object of found table. (NullAllowed: if null, not found)
     */
    public Table findPreviousTable(String tableDbName) { // public to glance previous world
        return _previousDb.getTable(tableDbName);
    }

    protected boolean isSameTableName(Table next, Table previous) {
        return isSame(next.getTableDbName(), previous.getTableDbName());
    }

    // ===================================================================================
    //                                                                      Column Process
    //                                                                      ==============
    // -----------------------------------------------------
    //                                                  Main
    //                                                  ----
    protected void processColumn(DfTableDiff tableDiff, Table nextTable, Table previousTable) {
        processAddedColumn(tableDiff, nextTable, previousTable);
        processChangedColumn(tableDiff, nextTable, previousTable);
        processDeletedColumn(tableDiff, nextTable, previousTable);
    }

    // -----------------------------------------------------
    //                                                 Added
    //                                                 -----
    protected void processAddedColumn(DfTableDiff tableDiff, Table nextTable, Table previousTable) {
        final List<Column> columnList = nextTable.getColumnList();
        for (Column column : columnList) {
            final Column found = previousTable.getColumn(column.getName());
            if (found == null || !isSameColumnName(column, found)) { // added
                tableDiff.addColumnDiff(DfColumnDiff.createAdded(column.getName()));
            }
        }
    }

    // -----------------------------------------------------
    //                                               Changed
    //                                               -------
    protected void processChangedColumn(DfTableDiff tableDiff, Table nextTable, Table previousTable) {
        final List<Column> columnList = nextTable.getColumnList();
        for (Column next : columnList) {
            final Column previous = previousTable.getColumn(next.getName());
            if (previous == null || !isSameColumnName(next, previous)) {
                continue;
            }
            // found
            final DfColumnDiff columnDiff = DfColumnDiff.createChanged(next.getName());
            processDbType(next, previous, columnDiff);
            processColumnSize(next, previous, columnDiff);
            processDefaultValue(next, previous, columnDiff);
            processNotNull(next, previous, columnDiff);
            processAutoIncrement(next, previous, columnDiff);
            processColumnComment(next, previous, columnDiff);
            if (columnDiff.hasDiff()) { // changed
                tableDiff.addColumnDiff(columnDiff);
            }
        }
    }

    protected void processDbType(Column next, Column previous, DfColumnDiff columnDiff) {
        diffNextPrevious(next, previous, columnDiff, new StringNextPreviousDiffer<Column, DfColumnDiff>() {
            public String provide(Column obj) {
                return obj.getDbType();
            }

            public void diff(DfColumnDiff diff, DfNextPreviousDiff nextPreviousDiff) {
                diff.setDbTypeDiff(nextPreviousDiff);
            }
        });
    }

    protected void processColumnSize(Column next, Column previous, DfColumnDiff columnDiff) {
        diffNextPrevious(next, previous, columnDiff, new StringNextPreviousDiffer<Column, DfColumnDiff>() {
            public String provide(Column obj) {
                return obj.getColumnSize();
            }

            public void diff(DfColumnDiff diff, DfNextPreviousDiff nextPreviousDiff) {
                diff.setColumnSizeDiff(nextPreviousDiff);
            }
        });
    }

    protected void processDefaultValue(Column next, Column previous, DfColumnDiff columnDiff) {
        diffNextPrevious(next, previous, columnDiff, new StringNextPreviousDiffer<Column, DfColumnDiff>() {
            public String provide(Column obj) {
                return obj.getDefaultValue();
            }

            @Override
            public boolean isMatch(String next, String previous) {
                if (super.isMatch(next, previous)) {
                    return true;
                }
                final boolean bothValid = next != null && previous != null;
                if (bothValid && isSystemSequence(next) && isSystemSequence(previous)) {
                    return true;
                }
                return false;
            }

            public void diff(DfColumnDiff diff, DfNextPreviousDiff nextPreviousDiff) {
                diff.setDefaultValueDiff(nextPreviousDiff);
            }
        });
    }

    protected void processNotNull(Column next, Column previous, DfColumnDiff columnDiff) {
        diffNextPrevious(next, previous, columnDiff, new BooleanNextPreviousDiffer<Column, DfColumnDiff>() {
            public Boolean provide(Column obj) {
                return obj.isNotNull();
            }

            public void diff(DfColumnDiff diff, DfNextPreviousDiff nextPreviousDiff) {
                diff.setNotNullDiff(nextPreviousDiff);
            }
        });
    }

    protected void processAutoIncrement(Column next, Column previous, DfColumnDiff columnDiff) {
        diffNextPrevious(next, previous, columnDiff, new BooleanNextPreviousDiffer<Column, DfColumnDiff>() {
            public Boolean provide(Column obj) {
                return obj.isAutoIncrement();
            }

            public void diff(DfColumnDiff diff, DfNextPreviousDiff nextPreviousDiff) {
                diff.setAutoIncrementDiff(nextPreviousDiff);
            }
        });
    }

    protected void processColumnComment(Column next, Column previous, DfColumnDiff columnDiff) {
        if (!_checkDbComment) {
            return;
        }
        diffNextPrevious(next, previous, columnDiff, new StringNextPreviousDiffer<Column, DfColumnDiff>() {
            public String provide(Column obj) {
                return obj.getPlainComment();
            }

            public void diff(DfColumnDiff diff, DfNextPreviousDiff nextPreviousDiff) {
                diff.setColumnCommentDiff(nextPreviousDiff);
            }
        });
    }

    protected <ITEM, TYPE> void diffNextPrevious(Column next, Column previous, DfColumnDiff diff,
            NextPreviousDiffer<Column, DfColumnDiff, TYPE> differ) {
        final TYPE nextValue = differ.provide(next);
        final TYPE previousValue = differ.provide(previous);
        if (!differ.isMatch(nextValue, previousValue)) {
            final String nextStr = nextValue != null ? nextValue.toString() : null;
            final String previousStr = previousValue != null ? previousValue.toString() : null;
            differ.diff(diff, createNextPreviousDiff(nextStr, previousStr));
        }
    }

    // -----------------------------------------------------
    //                                               Deleted
    //                                               -------
    protected void processDeletedColumn(DfTableDiff tableDiff, Table nextTable, Table previousTable) {
        final List<Column> columnList = previousTable.getColumnList();
        for (Column column : columnList) {
            final Column found = nextTable.getColumn(column.getName());
            if (found == null || !isSameColumnName(column, found)) { // deleted
                tableDiff.addColumnDiff(DfColumnDiff.createDeleted(column.getName()));
            }
        }
    }

    // -----------------------------------------------------
    //                                           Same Helper
    //                                           -----------
    protected boolean isSameColumnName(Column next, Column previous) {
        return isSame(next.getName(), previous.getName());
    }

    // ===================================================================================
    //                                                                  PrimaryKey Process
    //                                                                  ==================
    protected void processPrimaryKey(DfTableDiff tableDiff, Table nextTable, Table previousTable) {
        if (!nextTable.hasPrimaryKey() && !previousTable.hasPrimaryKey()) {
            return; // both no PK
        }
        final String noNamePKName = "(PK)";
        final String nextName = nextTable.getPrimaryKeyConstraintName();
        final String previousName = previousTable.getPrimaryKeyConstraintName();
        if (nextName == null && previousName == null) { // has PK but both no name
            if (hasSameStructurePrimaryKey(nextTable, previousTable)) {
                return; // no changed
            } else {
                final String constraintName = noNamePKName;
                final DfPrimaryKeyDiff primaryKeyDiff = DfPrimaryKeyDiff.createChanged(constraintName);
                processPrimaryKeyColumnDiff(tableDiff, nextTable, previousTable, primaryKeyDiff, constraintName);
            }
        }
        final String constraintName = nextName != null ? nextName : noNamePKName;
        if (isSame(nextName, previousName)) {
            final DfPrimaryKeyDiff primaryKeyDiff = DfPrimaryKeyDiff.createChanged(constraintName);
            processPrimaryKeyColumnDiff(tableDiff, nextTable, previousTable, primaryKeyDiff, constraintName);
        } else if (hasSameStructurePrimaryKey(nextTable, previousTable)) {
            return; // treated as no changed because only a name-change means nothing for developers
            //final DfPrimaryKeyDiff primaryKeyDiff = DfPrimaryKeyDiff.createChanged(constraintName);
            //final DfNextPreviousDiff nameDiff = createNextPreviousDiff(nextName, previousName);
            //primaryKeyDiff.setNameDiff(nameDiff);
            //tableDiff.addPrimaryKeyDiff(primaryKeyDiff);
        } else {
            if (nextName == null) { // deleted
                tableDiff.addPrimaryKeyDiff(DfPrimaryKeyDiff.createDeleted(previousName));
                return;
            } else if (previousName == null) { // added
                tableDiff.addPrimaryKeyDiff(DfPrimaryKeyDiff.createAdded(nextName));
                return;
            } else { // both are not null and different structure
                final DfPrimaryKeyDiff primaryKeyDiff = DfPrimaryKeyDiff.createChanged(constraintName);
                final DfNextPreviousDiff nameDiff = createNextPreviousDiff(nextName, previousName);
                primaryKeyDiff.setNameDiff(nameDiff);
                processPrimaryKeyColumnDiff(tableDiff, nextTable, previousTable, primaryKeyDiff, constraintName);
            }
        }
    }

    protected boolean hasSameStructurePrimaryKey(Table nextTable, Table previousTable) {
        final String nextCommaString = nextTable.getPrimaryKeyNameCommaString();
        final String previousCommaString = previousTable.getPrimaryKeyNameCommaString();
        return Srl.equalsPlain(nextCommaString, previousCommaString);
    }

    protected void processPrimaryKeyColumnDiff(DfTableDiff tableDiff, Table nextTable, Table previousTable,
            DfPrimaryKeyDiff primaryKeyDiff, String constraintName) {
        final String nextColumn = nextTable.getPrimaryKeyNameCommaString();
        final String previousColumn = previousTable.getPrimaryKeyNameCommaString();
        if (!isSame(nextColumn, previousColumn)) {
            final DfNextPreviousDiff columnDiff = createNextPreviousDiff(nextColumn, previousColumn);
            primaryKeyDiff.setColumnDiff(columnDiff);
        }
        if (primaryKeyDiff.hasDiff()) { // changed
            tableDiff.addPrimaryKeyDiff(primaryKeyDiff);
        }
    }

    // ===================================================================================
    //                                                                  ForeignKey Process
    //                                                                  ==================
    protected void processForeignKey(DfTableDiff tableDiff, Table nextTable, Table previousTable) {
        processConstraintKey(nextTable, previousTable, new DfForeignKeyDiffer(tableDiff));
    }

    // ===================================================================================
    //                                                                   UniqueKey Process
    //                                                                   =================
    protected void processUniqueKey(DfTableDiff tableDiff, Table nextTable, Table previousTable) {
        processConstraintKey(nextTable, previousTable, new DfUniqueKeyDiffer(tableDiff));
    }

    // ===================================================================================
    //                                                                       Index Process
    //                                                                       =============
    protected void processIndex(final DfTableDiff tableDiff, Table nextTable, Table previousTable) {
        processConstraintKey(nextTable, previousTable, new DfIndexDiffer(tableDiff));
    }

    // ===================================================================================
    //                                                                  Constraint Process
    //                                                                  ==================
    protected <KEY, DIFF extends DfConstraintDiff> void processConstraintKey(Table nextTable, Table previousTable,
            DfConstraintKeyDiffer<KEY, DIFF> differ) { // for except PK
        final List<KEY> keyList = differ.keyList(nextTable);
        final Set<String> sameStructureNextSet = DfCollectionUtil.newHashSet();
        final Map<String, KEY> nextPreviousMap = DfCollectionUtil.newLinkedHashMap();
        final Map<String, KEY> previousNextMap = DfCollectionUtil.newLinkedHashMap();
        nextLoop: for (KEY nextKey : keyList) {
            final String nextName = differ.constraintName(nextKey);
            if (nextName == null) {
                continue;
            }
            for (KEY previousKey : differ.keyList(previousTable)) {
                final String previousName = differ.constraintName(previousKey);
                if (differ.isSameConstraintName(nextName, previousName)) { // found
                    // auto-generated names are not here
                    nextPreviousMap.put(nextName, previousKey);
                    previousNextMap.put(previousName, nextKey);
                    continue nextLoop;
                }
            }
        }
        nextLoop: for (KEY nextKey : keyList) {
            final String nextName = differ.constraintName(nextKey);
            if (nextName == null || nextPreviousMap.containsKey(nextName)) {
                continue;
            }
            for (KEY previousKey : differ.keyList(previousTable)) {
                final String previousName = differ.constraintName(previousKey);
                if (previousNextMap.containsKey(previousName)) {
                    continue;
                }
                if (differ.isSameStructure(nextKey, previousKey)) { // found
                    nextPreviousMap.put(nextName, previousKey);
                    previousNextMap.put(previousName, nextKey);
                    sameStructureNextSet.add(nextName);
                    continue nextLoop;
                }
            }
        }
        for (Entry<String, KEY> entry : nextPreviousMap.entrySet()) {
            final String nextName = entry.getKey();
            if (sameStructureNextSet.contains(nextName)) {
                // treated as no changed because only a name-change means nothing for developers
                continue;
            }
            final KEY previousKey = entry.getValue();
            final String previousName = differ.constraintName(previousKey);
            final KEY nextKey = previousNextMap.get(previousName);
            processChangedConstraintKeyDiff(nextKey, previousKey, nextName, previousName, differ);
        }

        processAddedConstraintKey(nextTable, differ, nextPreviousMap);
        processDeletedConstraintKey(previousTable, differ, previousNextMap);
    }

    protected <KEY, DIFF extends DfConstraintDiff> void processChangedConstraintKeyDiff(KEY nextKey, KEY previousKey,
            String nextName, String previousName, DfConstraintKeyDiffer<KEY, DIFF> differ) {
        if (differ.isSameConstraintName(nextName, previousName)) { // same name, different structure
            final String nextColumn = differ.column(nextKey);
            final String previousColumn = differ.column(previousKey);
            DfNextPreviousDiff columnDiff = null;
            if (!isSame(nextColumn, previousColumn)) {
                columnDiff = createNextPreviousDiff(nextColumn, previousColumn);
            }
            final DIFF diff = differ.createChangedDiff(nextName);
            diff.setColumnDiff(columnDiff);
            differ.diff(diff, nextKey, previousKey);
        } else { // different name, same structure (*no way because of skipped)
            final DfNextPreviousDiff nameDiff = createNextPreviousDiff(nextName, previousName);
            final DIFF diff = differ.createChangedDiff(nextName);
            diff.setNameDiff(nameDiff);
            differ.diff(diff, nextKey, previousKey);
        }
    }

    protected <KEY, DIFF extends DfConstraintDiff> void processAddedConstraintKey(Table nextTable,
            DfConstraintKeyDiffer<KEY, DIFF> differ, Map<String, KEY> nextPreviousMap) {
        final List<KEY> keyList = differ.keyList(nextTable);
        for (KEY nextKey : keyList) {
            final String nextName = differ.constraintName(nextKey);
            if (nextPreviousMap.containsKey(nextName)) {
                continue;
            }
            // added
            final String registeredName;
            if (differ.isAutoGeneratedName(nextName)) {
                // to identity with deleted one
                registeredName = nextName + "(new)";
            } else {
                registeredName = nextName;
            }
            final DIFF diff = differ.createAddedDiff(registeredName);
            differ.diff(diff, nextKey, null);
        }
    }

    protected <KEY, DIFF extends DfConstraintDiff> void processDeletedConstraintKey(Table previousTable,
            DfConstraintKeyDiffer<KEY, DIFF> differ, Map<String, KEY> previousNextMap) { // for except PK
        final List<KEY> keyList = differ.keyList(previousTable);
        for (KEY previousKey : keyList) {
            final String previousName = differ.constraintName(previousKey);
            if (previousNextMap.containsKey(previousName)) {
                continue;
            }
            // deleted
            final String registeredName;
            if (differ.isAutoGeneratedName(previousName)) {
                // to identity with deleted one
                registeredName = previousName + "(old)";
            } else {
                registeredName = previousName;
            }
            final DIFF diff = differ.createDeletedDiff(registeredName);
            differ.diff(diff, null, previousKey);
        }
    }

    // ===================================================================================
    //                                                                    Sequence Process
    //                                                                    ================
    protected void processSequence() {
        if (_previousDb.hasSequenceGroup() && _nextDb.hasSequenceGroup()) {
            processAddedSequence();
            processChangedSequence();
            processDeletedSequence();
        }
    }

    // -----------------------------------------------------
    //                                                 Added
    //                                                 -----
    protected void processAddedSequence() {
        final List<Sequence> sequenceList = _nextDb.getSequenceList();
        for (Sequence sequence : sequenceList) {
            if (isSystemSequence(sequence.getSequenceName())) {
                continue;
            }
            final Sequence found = findPreviousSequence(sequence);
            if (found == null || !isSameSequenceName(sequence, found)) { // added
                addSequenceDiff(DfSequenceDiff.createAdded(sequence.getFormalUniqueName()));
            }
        }
    }

    // -----------------------------------------------------
    //                                               Changed
    //                                               -------
    protected void processChangedSequence() {
        final List<Sequence> sequenceList = _nextDb.getSequenceList();
        for (Sequence next : sequenceList) {
            final Sequence previous = findPreviousSequence(next);
            if (previous == null || !isSameSequenceName(next, previous)) {
                continue;
            }
            // found
            final DfSequenceDiff sequenceDiff = DfSequenceDiff.createChanged(next.getFormalUniqueName());

            // sequence needs schema to be unique so comparing schema is non-sense here
            //processUnifiedSchema(next, previous, sequenceDiff);

            // direct attributes
            processMinimumValue(next, previous, sequenceDiff);
            processMaximumValue(next, previous, sequenceDiff);
            processIncrementSize(next, previous, sequenceDiff);
            processSequenceComment(next, previous, sequenceDiff);

            if (sequenceDiff.hasDiff()) { // changed
                addSequenceDiff(sequenceDiff);
            }
        }
    }

    protected void processMinimumValue(Sequence next, Sequence previous, DfSequenceDiff sequenceDiff) {
        diffNextPrevious(next, previous, sequenceDiff, new StringNextPreviousDiffer<Sequence, DfSequenceDiff>() {
            public String provide(Sequence obj) {
                return DfTypeUtil.toString(obj.getMinimumValue());
            }

            public void diff(DfSequenceDiff diff, DfNextPreviousDiff nextPreviousDiff) {
                diff.setMinimumValueDiff(nextPreviousDiff);
            }
        });
    }

    protected void processMaximumValue(Sequence next, Sequence previous, DfSequenceDiff sequenceDiff) {
        diffNextPrevious(next, previous, sequenceDiff, new StringNextPreviousDiffer<Sequence, DfSequenceDiff>() {
            public String provide(Sequence obj) {
                return DfTypeUtil.toString(obj.getMaximumValue());
            }

            public void diff(DfSequenceDiff diff, DfNextPreviousDiff nextPreviousDiff) {
                diff.setMaximumValueDiff(nextPreviousDiff);
            }
        });
    }

    protected void processIncrementSize(Sequence next, Sequence previous, DfSequenceDiff sequenceDiff) {
        diffNextPrevious(next, previous, sequenceDiff, new StringNextPreviousDiffer<Sequence, DfSequenceDiff>() {
            public String provide(Sequence obj) {
                return DfTypeUtil.toString(obj.getIncrementSize());
            }

            public void diff(DfSequenceDiff diff, DfNextPreviousDiff nextPreviousDiff) {
                diff.setIncrementSizeDiff(nextPreviousDiff);
            }
        });
    }

    protected void processSequenceComment(Sequence next, Sequence previous, DfSequenceDiff sequenceDiff) {
        if (!_checkDbComment) {
            return;
        }
        diffNextPrevious(next, previous, sequenceDiff, new StringNextPreviousDiffer<Sequence, DfSequenceDiff>() {
            public String provide(Sequence obj) {
                return DfTypeUtil.toString(obj.getSequenceComment());
            }

            public void diff(DfSequenceDiff diff, DfNextPreviousDiff nextPreviousDiff) {
                diff.setSequenceCommentDiff(nextPreviousDiff);
            }
        });
    }

    protected <TYPE> void diffNextPrevious(Sequence next, Sequence previous, DfSequenceDiff diff,
            NextPreviousDiffer<Sequence, DfSequenceDiff, TYPE> differ) {
        final TYPE nextValue = differ.provide(next);
        final TYPE previousValue = differ.provide(previous);
        if (!differ.isMatch(nextValue, previousValue)) {
            final String nextDisp = differ.disp(nextValue, true);
            final String previousDisp = differ.disp(previousValue, false);
            differ.diff(diff, createNextPreviousDiff(nextDisp, previousDisp));
        }
    }

    // -----------------------------------------------------
    //                                               Deleted
    //                                               -------
    protected void processDeletedSequence() {
        final List<Sequence> sequenceList = _previousDb.getSequenceList();
        for (Sequence sequence : sequenceList) {
            if (isSystemSequence(sequence.getSequenceName())) {
                continue;
            }
            final Sequence found = findNextSequence(sequence);
            if (found == null || !isSameSequenceName(sequence, found)) { // deleted
                addSequenceDiff(DfSequenceDiff.createDeleted(sequence.getFormalUniqueName()));
            }
        }
    }

    // -----------------------------------------------------
    //                                         Assist Helper
    //                                         -------------
    protected Sequence findNextSequence(Sequence sequence) {
        return doFindSequence(_nextDb, sequence);
    }

    protected Sequence findPreviousSequence(Sequence sequence) {
        return doFindSequence(_previousDb, sequence);
    }

    protected Sequence doFindSequence(Database db, Sequence sequence) {
        return _suppressSchema ? db.getSequenceByPureName(sequence) : db.getSequenceByUniqueName(sequence);
    }

    protected boolean isSystemSequence(String sequenceName) {
        final String pureName = Srl.substringLastRear(sequenceName, ".");
        if (isDatabaseDB2()) {
            return Srl.hasPrefixAllIgnoreCase(KEYWORD_DB2_SYSTEM_SEQUENCE, pureName);
        } else if (isDatabaseH2()) {
            return Srl.hasPrefixAllIgnoreCase(KEYWORD_H2_SYSTEM_SEQUENCE, pureName);
        }
        return false;
    }

    protected boolean isSameSequenceName(Sequence next, Sequence previous) {
        return isSame(getSequenceKeyName(next), getSequenceKeyName(previous));
    }

    protected String getSequenceKeyName(Sequence sequence) {
        return _suppressSchema ? sequence.getSequenceName() : sequence.getFormalUniqueName();
    }

    // ===================================================================================
    //                                                                   Procedure Process
    //                                                                   =================
    protected void processProcedure() {
        if (_previousDb.hasProcedureGroup() && _nextDb.hasProcedureGroup()) {
            processAddedProcedure();
            processChangedProcedure();
            processDeletedProcedure();
        }
    }

    // -----------------------------------------------------
    //                                                 Added
    //                                                 -----
    protected void processAddedProcedure() {
        final List<Procedure> procedureList = _nextDb.getProcedureList();
        for (Procedure procedure : procedureList) {
            final Procedure found = findPreviousProcedure(procedure);
            if (found == null || !isSameProcedureName(procedure, found)) { // added
                addProcedureDiff(DfProcedureDiff.createAdded(procedure.getProcedureUniqueName()));
            }
        }
    }

    // -----------------------------------------------------
    //                                               Changed
    //                                               -------
    protected void processChangedProcedure() {
        final List<Procedure> procedureList = _nextDb.getProcedureList();
        for (Procedure next : procedureList) {
            final Procedure previous = findPreviousProcedure(next);
            if (previous == null || !isSameProcedureName(next, previous)) {
                continue;
            }
            // found
            final DfProcedureDiff procedureDiff = DfProcedureDiff.createChanged(next.getProcedureUniqueName());

            // procedure needs schema to be unique so comparing schema is non-sense here
            //processUnifiedSchema(next, previous, procedureDiff);

            // direct attributes
            processSourceLine(next, previous, procedureDiff);
            processSourceSize(next, previous, procedureDiff);
            processSourceHash(next, previous, procedureDiff);
            processProcedureComment(next, previous, procedureDiff);

            if (procedureDiff.hasDiff()) { // changed
                addProcedureDiff(procedureDiff);
            }
        }
    }

    protected void processSourceLine(Procedure next, Procedure previous, DfProcedureDiff procedureDiff) {
        diffNextPrevious(next, previous, procedureDiff, new ProcedureSourceNextPreviousDiffer() {
            public String provide(Procedure obj) {
                return DfTypeUtil.toString(obj.getSourceLine());
            }

            public void diff(DfProcedureDiff diff, DfNextPreviousDiff nextPreviousDiff) {
                diff.setSourceLineDiff(nextPreviousDiff);
            }
        });
    }

    protected void processSourceSize(Procedure next, Procedure previous, DfProcedureDiff procedureDiff) {
        diffNextPrevious(next, previous, procedureDiff, new ProcedureSourceNextPreviousDiffer() {
            public String provide(Procedure obj) {
                return DfTypeUtil.toString(obj.getSourceSize());
            }

            public void diff(DfProcedureDiff diff, DfNextPreviousDiff nextPreviousDiff) {
                diff.setSourceSizeDiff(nextPreviousDiff);
            }
        });
    }

    protected void processSourceHash(Procedure next, Procedure previous, DfProcedureDiff procedureDiff) {
        diffNextPrevious(next, previous, procedureDiff, new ProcedureSourceNextPreviousDiffer() {
            public String provide(Procedure obj) {
                return obj.getSourceHash();
            }

            public void diff(DfProcedureDiff diff, DfNextPreviousDiff nextPreviousDiff) {
                diff.setSourceHashDiff(nextPreviousDiff);
            }
        });
    }

    protected abstract class ProcedureSourceNextPreviousDiffer extends
            StringNextPreviousDiffer<Procedure, DfProcedureDiff> {
        @Override
        public boolean isMatch(String next, String previous) {
            if (isNullMeta(next, previous)) {
                return true;
            }
            return super.isMatch(next, previous);
        }

        protected boolean isNullMeta(String next, String previous) {
            if (next != null && next.equals(PROCEDURE_SOURCE_NO_META_MARK)) {
                return true;
            }
            if (previous != null && previous.equals(PROCEDURE_SOURCE_NO_META_MARK)) {
                return true;
            }
            return false;
        }
    }

    protected void processProcedureComment(Procedure next, Procedure previous, DfProcedureDiff procedureDiff) {
        if (!_checkDbComment) {
            return;
        }
        diffNextPrevious(next, previous, procedureDiff, new StringNextPreviousDiffer<Procedure, DfProcedureDiff>() {
            public String provide(Procedure obj) {
                return DfTypeUtil.toString(obj.getProcedureComment());
            }

            public void diff(DfProcedureDiff diff, DfNextPreviousDiff nextPreviousDiff) {
                diff.setProcedureCommentDiff(nextPreviousDiff);
            }
        });
    }

    protected <TYPE> void diffNextPrevious(Procedure next, Procedure previous, DfProcedureDiff diff,
            NextPreviousDiffer<Procedure, DfProcedureDiff, TYPE> differ) {
        final TYPE nextValue = differ.provide(next);
        final TYPE previousValue = differ.provide(previous);
        if (!differ.isMatch(nextValue, previousValue)) {
            final String nextDisp = differ.disp(nextValue, true);
            final String previousDisp = differ.disp(previousValue, false);
            differ.diff(diff, createNextPreviousDiff(nextDisp, previousDisp));
        }
    }

    // -----------------------------------------------------
    //                                               Deleted
    //                                               -------
    protected void processDeletedProcedure() {
        final List<Procedure> procedureList = _previousDb.getProcedureList();
        for (Procedure procedure : procedureList) {
            final Procedure found = findNextProcedure(procedure);
            if (found == null || !isSameProcedureName(procedure, found)) { // deleted
                addProcedureDiff(DfProcedureDiff.createDeleted(procedure.getProcedureUniqueName()));
            }
        }
    }

    // -----------------------------------------------------
    //                                         Assist Helper
    //                                         -------------
    protected Procedure findNextProcedure(Procedure procedure) {
        return doFindProcedure(_nextDb, procedure);
    }

    protected Procedure findPreviousProcedure(Procedure procedure) {
        return doFindProcedure(_previousDb, procedure);
    }

    protected Procedure doFindProcedure(Database db, Procedure procedure) {
        return _suppressSchema ? db.getProcedureByPureName(procedure) : db.getProcedureByUniqueName(procedure);
    }

    protected boolean isSameProcedureName(Procedure next, Procedure previous) {
        return isSame(getProcedureKeyName(next), getProcedureKeyName(previous));
    }

    protected String getProcedureKeyName(Procedure procedure) {
        // the simple procedureName contains package name of Oracle
        return _suppressSchema ? procedure.getProcedureName() : procedure.getProcedureUniqueName();
    }

    // ===================================================================================
    //                                                                       Craft Process
    //                                                                       =============
    protected void processCraftDiff() {
        if (_craftMetaDir != null) {
            _craftDiff.analyzeDiff(_craftMetaDir);
        }
    }

    // ===================================================================================
    //                                                                            Diff Map
    //                                                                            ========
    public Map<String, Object> createSchemaDiffMap() {
        final Map<String, Object> schemaDiffMap = DfCollectionUtil.newLinkedHashMap();
        schemaDiffMap.put(DIFF_DATE_KEY, DfTypeUtil.toString(_diffDate, DIFF_DATE_PATTERN));
        if (_tableCountDiff.hasDiff()) {
            schemaDiffMap.put(TABLE_COUNT_KEY, _tableCountDiff.createNextPreviousDiffMap());
        }

        final List<NestDiffSetupper> nestDiffList = _nestDiffList;
        for (NestDiffSetupper setupper : nestDiffList) {
            final List<? extends DfNestDiff> diffAllList = setupper.provide();
            if (!diffAllList.isEmpty()) {
                final Map<String, Map<String, Object>> diffMap = DfCollectionUtil.newLinkedHashMap();
                for (DfNestDiff nestDiff : diffAllList) {
                    if (nestDiff.hasDiff()) {
                        diffMap.put(nestDiff.getKeyName(), nestDiff.createDiffMap());
                    }
                }
                if (!diffMap.isEmpty()) {
                    schemaDiffMap.put(setupper.propertyName(), diffMap);
                }
            }
        }
        return schemaDiffMap;
    }

    public void acceptSchemaDiffMap(Map<String, Object> schemaDiffMap) {
        final Set<Entry<String, Object>> entrySet = schemaDiffMap.entrySet();
        for (Entry<String, Object> entry : entrySet) {
            final String key = entry.getKey();
            final Object value = entry.getValue();
            if (DIFF_DATE_KEY.equals(key)) {
                _diffDate = DfTypeUtil.toDate(value, DIFF_DATE_PATTERN);
                assertDiffDateExists(key, _diffDate, schemaDiffMap);
            } else if (COMMENT_KEY.equals(key)) {
                _comment = (String) value; // nullable
            } else if (TABLE_COUNT_KEY.equals(key)) {
                _tableCountDiff = restoreNextPreviousDiff(schemaDiffMap, key);
                assertTableCountExists(key, _tableCountDiff, schemaDiffMap);
            } else {
                final List<NestDiffSetupper> nestDiffList = _nestDiffList;
                for (NestDiffSetupper setupper : nestDiffList) {
                    if (setupper.propertyName().equals(key)) {
                        restoreNestDiff(schemaDiffMap, setupper);
                    }
                }
            }
        }
    }

    protected void assertDiffDateExists(String key, Date diffDate, Map<String, Object> schemaDiffMap) {
        if (diffDate == null) { // basically no way
            String msg = "The diff-date of diff-map is required:";
            msg = msg + " key=" + key + " schemaDiffMap=" + schemaDiffMap;
            throw new IllegalStateException(msg);
        }
    }

    protected void assertTableCountExists(String key, DfNextPreviousDiff nextPreviousDiff,
            Map<String, Object> schemaDiffMap) {
        if (nextPreviousDiff == null) { // basically no way
            String msg = "The table count of diff-map is required:";
            msg = msg + " key=" + key + " schemaDiffMap=" + schemaDiffMap;
            throw new IllegalStateException(msg);
        }
    }

    protected void assertNextTableCountExists(String key, String nextTableCount, Map<String, Object> schemaDiffMap) {
        if (nextTableCount == null) { // basically no way
            String msg = "The next table count of diff-map is required:";
            msg = msg + " key=" + key + " schemaDiffMap=" + schemaDiffMap;
            throw new IllegalStateException(msg);
        }
    }

    protected void assertPreviousTableCountExists(String key, String previousTableCount,
            Map<String, Object> schemaDiffMap) {
        if (previousTableCount == null) { // basically no way
            String msg = "The previous table count of diff-map is required:";
            msg = msg + " key=" + key + " schemaDiffMap=" + schemaDiffMap;
            throw new IllegalStateException(msg);
        }
    }

    // ===================================================================================
    //                                                                              Status
    //                                                                              ======
    public boolean hasDiff() {
        final List<NestDiffSetupper> nestDiffList = _nestDiffList;
        for (NestDiffSetupper setupper : nestDiffList) {
            final List<? extends DfNestDiff> diffAllList = setupper.provide();
            for (DfNestDiff nestDiff : diffAllList) {
                if (nestDiff.hasDiff()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean canReadNext() {
        return !isFirstTime() && !isLoadingFailure();
    }

    /**
     * Is the first time to read the schema? <br />
     * It also means previous schema info was not found. <br />
     * This determination is set after {@link #loadPreviousSchema()}.
     * @return The determination, true or false.
     */
    public boolean isFirstTime() {
        return _firstTime;
    }

    public boolean isLoadingFailure() {
        return _loadingFailure;
    }

    // ===================================================================================
    //                                                                              Option
    //                                                                              ======
    public void checkColumnDefOrder() {
        _checkColumnDefOrder = true;
    }

    public void checkDbComment() {
        _checkDbComment = true;
    }

    public void suppressSchema() {
        _suppressSchema = true;
    }

    public void enableCraftDiff(String craftMetaDir) {
        if (craftMetaDir == null) {
            return;
        }
        _craftMetaDir = craftMetaDir;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    // -----------------------------------------------------
    //                                                 Basic
    //                                                 -----
    public String getDiffDate() {
        return DfTypeUtil.toString(_diffDate, DIFF_DATE_PATTERN);
    }

    public boolean hasComment() {
        return Srl.is_NotNull_and_NotTrimmedEmpty(_comment);
    }

    public String getComment() {
        return _comment;
    }

    public DfNextPreviousDiff getTableCount() {
        return _tableCountDiff;
    }

    // -----------------------------------------------------
    //                                            Table Diff
    //                                            ----------
    public List<DfTableDiff> getTableDiffAllList() {
        return _tableDiffAllList;
    }

    public List<DfTableDiff> getAddedTableDiffList() {
        return _addedTableDiffList;
    }

    public List<DfTableDiff> getChangedTableDiffList() {
        return _changedTableDiffList;
    }

    public List<DfTableDiff> getDeletedTableDiffList() {
        return _deletedTableDiffList;
    }

    protected void addTableDiff(DfTableDiff tableDiff) {
        _tableDiffAllList.add(tableDiff);
        if (tableDiff.isAdded()) {
            _addedTableDiffList.add(tableDiff);
        } else if (tableDiff.isChanged()) {
            _changedTableDiffList.add(tableDiff);
        } else if (tableDiff.isDeleted()) {
            _deletedTableDiffList.add(tableDiff);
        } else { // no way
            String msg = "Unknown diff-type of table: ";
            msg = msg + " diffType=" + tableDiff.getDiffType() + " tableDiff=" + tableDiff;
            throw new IllegalStateException(msg);
        }
    }

    // -----------------------------------------------------
    //                                         Sequence Diff
    //                                         -------------
    public List<DfSequenceDiff> getSequenceDiffAllList() {
        return _sequenceDiffAllList;
    }

    public List<DfSequenceDiff> getAddedSequenceDiffList() {
        return _addedSequenceDiffList;
    }

    public List<DfSequenceDiff> getChangedSequenceDiffList() {
        return _changedSequenceDiffList;
    }

    public List<DfSequenceDiff> getDeletedSequenceDiffList() {
        return _deletedSequenceDiffList;
    }

    protected void addSequenceDiff(DfSequenceDiff sequenceDiff) {
        _sequenceDiffAllList.add(sequenceDiff);
        if (sequenceDiff.isAdded()) {
            _addedSequenceDiffList.add(sequenceDiff);
        } else if (sequenceDiff.isChanged()) {
            _changedSequenceDiffList.add(sequenceDiff);
        } else if (sequenceDiff.isDeleted()) {
            _deletedSequenceDiffList.add(sequenceDiff);
        } else { // no way
            String msg = "Unknown diff-type of sequence: ";
            msg = msg + " diffType=" + sequenceDiff.getDiffType() + " sequenceDiff=" + sequenceDiff;
            throw new IllegalStateException(msg);
        }
    }

    // -----------------------------------------------------
    //                                        Procedure Diff
    //                                        --------------
    public List<DfProcedureDiff> getProcedureDiffAllList() {
        return _procedureDiffAllList;
    }

    public List<DfProcedureDiff> getAddedProcedureDiffList() {
        return _addedProcedureDiffList;
    }

    public List<DfProcedureDiff> getChangedProcedureDiffList() {
        return _changedProcedureDiffList;
    }

    public List<DfProcedureDiff> getDeletedProcedureDiffList() {
        return _deletedProcedureDiffList;
    }

    protected void addProcedureDiff(DfProcedureDiff procedureDiff) {
        _procedureDiffAllList.add(procedureDiff);
        if (procedureDiff.isAdded()) {
            _addedProcedureDiffList.add(procedureDiff);
        } else if (procedureDiff.isChanged()) {
            _changedProcedureDiffList.add(procedureDiff);
        } else if (procedureDiff.isDeleted()) {
            _deletedProcedureDiffList.add(procedureDiff);
        } else { // no way
            String msg = "Unknown diff-type of procedure: ";
            msg = msg + " diffType=" + procedureDiff.getDiffType() + " procedureDiff=" + procedureDiff;
            throw new IllegalStateException(msg);
        }
    }

    // -----------------------------------------------------
    //                                            Craft Diff
    //                                            ----------
    public List<DfCraftTitleDiff> getCraftTitleDiffList() {
        return _craftDiff.getCraftTitleDiffList();
    }

    // -----------------------------------------------------
    //                                             Meta Info
    //                                             ---------
    public void setLatest(boolean latest) {
        _latest = latest;
    }

    public boolean isLatest() { // called by the template 'diffmodel.vm'
        return _latest;
    }
}
