/*
 * Copyright 2014-2023 the original author or authors.
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
package org.dbflute.logic.doc.lreverse.schema;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.apache.torque.engine.database.model.Database;
import org.apache.torque.engine.database.model.Table;
import org.dbflute.DfBuildProperties;
import org.dbflute.helper.StringSet;
import org.dbflute.helper.jdbc.context.DfSchemaSource;
import org.dbflute.logic.doc.lreverse.existing.DfLReverseExistingFileProvider;
import org.dbflute.logic.doc.lreverse.existing.DfLReverseExistingTsvInfo;
import org.dbflute.logic.doc.lreverse.existing.DfLReverseExistingXlsInfo;
import org.dbflute.logic.replaceschema.loaddata.xls.dataprop.DfTableNameProp;
import org.dbflute.properties.DfDocumentProperties;
import org.dbflute.properties.DfReplaceSchemaProperties;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 1.2.5 as split (2021/01/04 Monday at roppongi japanese)
 */
public class DfLReverseSchemaTableFilter {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The logger instance for this class. (NotNull) */
    private static final Logger _log = LoggerFactory.getLogger(DfLReverseSchemaTableFilter.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfSchemaSource _dataSource;
    protected final DfTableNameProp _tableNameProp;
    protected final List<Table> _skippedTableList;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfLReverseSchemaTableFilter(DfSchemaSource dataSource, DfTableNameProp tableNameProp, List<Table> skippedTableList) {
        _dataSource = dataSource;
        _tableNameProp = tableNameProp;
        _skippedTableList = skippedTableList;
    }

    // ===================================================================================
    //                                                                        Filter Table
    //                                                                        ============
    public List<Table> filterTableList(Database database) {
        final List<Table> tableList = database.getTableList();
        final Set<String> commonSkippedTableSet = prepareCommonSkippedTableSet(); // if needed
        final List<Table> filteredList = DfCollectionUtil.newArrayListSized(tableList.size());
        _skippedTableList.clear();
        final List<Table> commonSkippedList = DfCollectionUtil.newArrayList();
        final List<Table> exceptSkippedList = DfCollectionUtil.newArrayList();
        _log.info("...Filtering reversed table: " + tableList.size());
        for (Table table : tableList) {
            if (table.isTypeView() || table.isAdditionalSchema()) {
                // fixedly out of target
                //   view object - view is not an object which has own data
                //   additional schema - tables on main schema only are target
                continue;
            }
            if (commonSkippedTableSet.contains(table.getTableDbName())) {
                commonSkippedList.add(table);
                continue;
            }
            if (!isTargetTable(table)) {
                exceptSkippedList.add(table);
                continue;
            }
            filteredList.add(table);
        }
        if (!commonSkippedList.isEmpty()) {
            _log.info("[Common Table] *skipped");
            for (Table table : commonSkippedList) {
                _log.info("  " + table.getTableDbName());
                _skippedTableList.add(table);
            }
        }
        if (!exceptSkippedList.isEmpty()) {
            _log.info("[Except Table] *skipped");
            for (Table table : exceptSkippedList) {
                _log.info("  " + table.getTableDbName());
                _skippedTableList.add(table);
            }
        }
        return filteredList;
    }

    protected Set<String> prepareCommonSkippedTableSet() {
        if (!isReplaceSchemaDirectUse()) {
            return DfCollectionUtil.emptySet();
        }
        // if ReplaceSchema direct use (cyclic data), it does not need to reverse common data
        // so prepare name set of table skipped as common here
        final Set<String> tableSet = StringSet.createAsFlexible();

        // TSV
        tableSet.addAll(extractCommonExistingTsvTableSet(getMainCommonReverseTsvDataDir()));
        tableSet.addAll(extractCommonExistingTsvTableSet(getMainCommonTsvDataDir()));

        // Xls
        tableSet.addAll(extractCommonExistingXlsTableSet(getMainCommonFirstXlsDataDir()));
        tableSet.addAll(extractCommonExistingXlsTableSet(getMainCommonReverseXlsDataDir()));
        tableSet.addAll(extractCommonExistingXlsTableSet(getMainCommonXlsDataDir()));

        return tableSet;
    }

    protected boolean isTargetTable(Table table) {
        return isReverseTableTarget(table.getTableDbName());
    }

    // ===================================================================================
    //                                                                        Existing TSV
    //                                                                        ============
    protected Set<String> extractCommonExistingTsvTableSet(String commonTsvDataDir) { // e.g. .../common/reversetsv
        return extractExistingTsvInfo(new File(commonTsvDataDir)).getTableExistingTsvListMap().keySet();
    }

    protected DfLReverseExistingTsvInfo extractExistingTsvInfo(File commonTsvDataDir) { // e.g. .../common/reversetsv
        return new DfLReverseExistingFileProvider(_tableNameProp).extractExistingTsvInfo(commonTsvDataDir);
    }

    // ===================================================================================
    //                                                                        Existing Xls
    //                                                                        ============
    protected Set<String> extractCommonExistingXlsTableSet(String commonXlsDataDir) {
        return extractExistingXlsInfo(new File(commonXlsDataDir)).getTableExistingXlsMap().keySet();
    }

    protected DfLReverseExistingXlsInfo extractExistingXlsInfo(File commonXlsDataDir) {
        return new DfLReverseExistingFileProvider(_tableNameProp).extractExistingXlsInfo(commonXlsDataDir);
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

    protected DfReplaceSchemaProperties getReplaceSchemaProperties() {
        return getProperties().getReplaceSchemaProperties();
    }

    // -----------------------------------------------------
    //                                          Basic Option
    //                                          ------------
    protected boolean isReplaceSchemaDirectUse() {
        return getDocumentProperties().isLoadDataReverseReplaceSchemaDirectUse();
    }

    // -----------------------------------------------------
    //                                          Table Except
    //                                          ------------
    protected boolean isReverseTableTarget(String name) {
        return getDocumentProperties().isLoadDataReverseTableTarget(name);
    }

    // -----------------------------------------------------
    //                                         ReplaceSchema
    //                                         -------------
    protected String getMainCommonReverseTsvDataDir() {
        return getReplaceSchemaProperties().getMainCommonReverseTsvDataDir();
    }

    protected String getMainCommonTsvDataDir() {
        return getReplaceSchemaProperties().getMainCommonTsvDataDir();
    }

    protected String getMainCommonFirstXlsDataDir() {
        return getReplaceSchemaProperties().getMainCommonFirstXlsDataDir();
    }

    protected String getMainCommonReverseXlsDataDir() {
        return getReplaceSchemaProperties().getMainCommonReverseXlsDataDir();
    }

    protected String getMainCommonXlsDataDir() {
        return getReplaceSchemaProperties().getMainCommonXlsDataDir();
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String resolvePath(File file) {
        return Srl.replace(file.getPath(), "\\", "/");
    }
}
