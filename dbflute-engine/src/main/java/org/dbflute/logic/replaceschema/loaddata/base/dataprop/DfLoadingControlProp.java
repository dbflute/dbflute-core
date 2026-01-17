/*
 * Copyright 2014-2026 the original author or authors.
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
package org.dbflute.logic.replaceschema.loaddata.base.dataprop;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.dbflute.exception.DfIllegalPropertySettingException;
import org.dbflute.exception.DfLoadDataRegistrationFailureException;
import org.dbflute.helper.StringKeyMap;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.logic.jdbc.metadata.info.DfColumnMeta;
import org.dbflute.logic.replaceschema.loaddata.base.DfLoadedSchemaTable;
import org.dbflute.logic.replaceschema.loaddata.base.dataprop.dateadj.DfDateAdjustmentPreparer;
import org.dbflute.logic.replaceschema.loaddata.base.dataprop.dateadj.DfDateAdjustmentRowExecutor;
import org.dbflute.logic.replaceschema.loaddata.base.secretary.DfColumnBindTypeProvider;
import org.dbflute.logic.replaceschema.loaddata.base.secretary.DfRelativeDateResolver;
import org.dbflute.logic.replaceschema.loaddata.delimiter.DfDelimiterDataResultInfo;
import org.dbflute.logic.replaceschema.loaddata.delimiter.line.DfDelimiterDataFirstLineInfo;
import org.dbflute.properties.propreader.DfOutsideMapPropReader;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.DfNameHintUtil;
import org.dbflute.util.DfTypeUtil;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 1.0.4A (2013/03/09 Saturday)
 */
public class DfLoadingControlProp {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger _log = LoggerFactory.getLogger(DfLoadingControlProp.class);

    public static final String LOADING_CONTROL_MAP_NAME = "loadingControlMap.dataprop";
    public static final String PROP_DATE_ADJUSTMENT_MAP = "dateAdjustmentMap";
    public static final String PROP_LARGE_TEXT_FILE_MAP = "largeTextFileMap";

    // -----------------------------------------------------
    //                                       Date Adjustment
    //                                       ---------------
    protected static final String KEY_ORIGIN_DATE = DfDateAdjustmentPreparer.KEY_ORIGIN_DATE;
    protected static final String KEY_MILLIS_COLUMN_LIST = DfDateAdjustmentPreparer.KEY_MILLIS_COLUMN_LIST;
    protected static final String KEY_DATE_ADJ_ALL_MARK = DfDateAdjustmentPreparer.KEY_ALL_MARK;
    protected static final String KEY_DISTANCE_YEARS = DfDateAdjustmentPreparer.KEY_DISTANCE_YEARS;
    protected static final String KEY_DISTANCE_MONTHS = DfDateAdjustmentPreparer.KEY_DISTANCE_MONTHS;
    protected static final String KEY_DISTANCE_DAYS = DfDateAdjustmentPreparer.KEY_DISTANCE_DAYS;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                       .dataprop Cache
    //                                       ---------------
    protected final Map<String, Map<String, Object>> _loadingControlMapMap = DfCollectionUtil.newLinkedHashMap();

    // -----------------------------------------------------
    //                                         Assist Object
    //                                         -------------
    protected final DfDateAdjustmentPreparer _dateAdjustmentPreparer = new DfDateAdjustmentPreparer();

    // instance recycle between rows
    protected final DfRelativeDateResolver _relativeDateResolver = new DfRelativeDateResolver();

    // ===================================================================================
    //                                                                 Logging Insert Type
    //                                                                 ===================
    public LoggingInsertType getLoggingInsertType(String dataDirectory, boolean loggingInsertSql) {
        final Map<String, Object> loadingControlMap = findLoadingControlMap(dataDirectory);
        final String prop = (String) loadingControlMap.get("loggingInsertType");
        if (isSpecifiedValidProperty(prop)) {
            final String trimmed = prop.trim();
            if (trimmed.equalsIgnoreCase("all")) {
                return LoggingInsertType.ALL;
            } else if (trimmed.equalsIgnoreCase("none")) {
                return LoggingInsertType.NONE;
            } else if (trimmed.equalsIgnoreCase("part")) {
                return LoggingInsertType.PART;
            } else {
                String msg = "Unknown property value for loggingInsertType:";
                msg = msg + " value=" + trimmed + " dataDirectory=" + dataDirectory;
                throw new DfIllegalPropertySettingException(msg);
            }
        }
        return loggingInsertSql ? LoggingInsertType.ALL : LoggingInsertType.NONE;
    }

    public static enum LoggingInsertType {
        ALL, NONE, PART
    }

    // ===================================================================================
    //                                                               Suppress Batch Update
    //                                                               =====================
    public boolean isMergedSuppressBatchUpdate(String dataDirectory, boolean suppressBatchUpdate) {
        final Map<String, Object> loadingControlMap = findLoadingControlMap(dataDirectory);
        final String prop = (String) loadingControlMap.get("isSuppressBatchUpdate");
        if (isSpecifiedValidProperty(prop)) {
            return prop.trim().equalsIgnoreCase("true");
        }
        return suppressBatchUpdate;
    }

    // ===================================================================================
    //                                                         Different ColumnValue Count
    //                                                         ===========================
    protected boolean isContinueDifferentColumnValueCount(String dataDirectory) { // basically delimiter file only
        final Map<String, Object> loadingControlMap = findLoadingControlMap(dataDirectory);
        final String prop = (String) loadingControlMap.get("isContinueDifferentColumnValueCount");
        if (isSpecifiedValidProperty(prop) && prop.trim().equalsIgnoreCase("true")) {
            return true; // continue (warning only)
        }
        return false; // default is error
    }

    public void handleDifferentColumnValueCount(DfDelimiterDataResultInfo resultInfo, String dataDirectory, String fileName,
            DfLoadedSchemaTable schemaTable, DfDelimiterDataFirstLineInfo firstLineInfo, List<String> valueList) {
        if (isContinueDifferentColumnValueCount(dataDirectory)) {
            String msg = "The count of values wasn't correct:";
            msg = msg + " column=" + firstLineInfo.getColumnNameList().size() + " value=" + valueList.size();
            msg = msg + " -> " + valueList;
            resultInfo.registerColumnCountDiff(fileName, msg);
        } else {
            throwLoadingControlDifferentColumnValueCountException(fileName, schemaTable, firstLineInfo, valueList);
        }
    }

    protected void throwLoadingControlDifferentColumnValueCountException(String fileName, DfLoadedSchemaTable schemaTable,
            DfDelimiterDataFirstLineInfo firstLineInfo, List<String> valueList) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Different column and value count in your data file.");
        br.addItem("Advice");
        br.addElement("Confirm the count of header columns and row values.");
        br.addElement("For example:");
        br.addElement("  (o):");
        br.addElement("    SEA_ID (delimiter) SEA_NAME (delimiter) SEA_DATE");
        br.addElement("    1 (delimiter) mystic (delimiter) 2017/03/26 16:34:00");
        br.addElement("    2 (delimiter) bigband (delimiter) 2017/03/26 16:34:00");
        br.addElement("  (x):");
        br.addElement("    SEA_ID (delimiter) SEA_NAME (delimiter) SEA_DATE");
        br.addElement("    1 (delimiter) mystic // *Bad");
        br.addElement("    2 (delimiter) bigband (delimiter) 2017/03/26 16:34:00");
        br.addItem("Data File");
        br.addElement(fileName); // contains path
        br.addItem("Table Name");
        br.addElement(schemaTable);
        br.addItem("Header Columns");
        final String displayDelimiter = " | ";
        final List<String> columnNameList = firstLineInfo.getColumnNameList();
        br.addElement(Srl.connectByDelimiter(columnNameList, displayDelimiter));
        br.addElement("count: " + columnNameList.size());
        br.addItem("Row Values");
        br.addElement(Srl.connectByDelimiter(valueList, displayDelimiter));
        br.addElement("count: " + valueList.size());
        final String msg = br.buildExceptionMessage();
        throw new DfLoadDataRegistrationFailureException(msg);
    }

    // ===================================================================================
    //                                                             Column Definition Check
    //                                                             =======================
    public boolean isCheckColumnDef(String dataDirectory) {
        final Map<String, Object> loadingControlMap = findLoadingControlMap(dataDirectory);
        final String prop = (String) loadingControlMap.get("isSuppressColumnDefCheck");
        if (isSpecifiedValidProperty(prop) && prop.trim().equalsIgnoreCase("true")) {
            return false; // suppress
        }
        return true; // default is checked
    }

    public void checkColumnDef(File dataFile, DfLoadedSchemaTable schemaTable, List<String> columnDefNameList,
            Map<String, DfColumnMeta> columnMetaMap) {
        final List<String> unneededList = new ArrayList<String>();
        for (String columnName : columnDefNameList) {
            if (!columnMetaMap.containsKey(columnName)) {
                unneededList.add(columnName);
            }
        }
        if (!unneededList.isEmpty()) {
            throwLoadingControlNonExistingColumnException(dataFile, schemaTable, columnMetaMap, unneededList);
        }
    }

    protected void throwLoadingControlNonExistingColumnException(File dataFile, DfLoadedSchemaTable schemaTable,
            Map<String, DfColumnMeta> columnMetaMap, List<String> unneededList) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Non-existing column in database, defined your data file.");
        br.addItem("Advice");
        br.addElement("Confirm the column names in your data file");
        br.addElement("and existing columns in database.");
        br.addElement("For example:");
        br.addElement("  (o):");
        br.addElement("    SEA_ID | SEA_NAME | SEA_COUNT | SEA_DATE // Good");
        br.addElement("    1      | mystic   | 904       | 2001/09/04");
        br.addElement("    ...");
        br.addElement("  (x):");
        br.addElement("    SEA_ID | SEA_NAME | SEA_COUNT | C_DATE   // *Bad");
        br.addElement("    1      | mystic   | 904       | 2001/09/04");
        br.addElement("    ...");
        br.addItem("Data File");
        br.addElement(dataFile);
        br.addItem("Table Name");
        br.addElement(schemaTable);
        br.addItem("Existing Column");
        buildNonExistingColumnNumberingColumnElement(br, columnMetaMap.keySet());
        br.addItem("Non-existing Column");
        buildNonExistingColumnNumberingColumnElement(br, unneededList);
        final String msg = br.buildExceptionMessage();
        throw new DfLoadDataRegistrationFailureException(msg);
    }

    protected void buildNonExistingColumnNumberingColumnElement(ExceptionMessageBuilder br, Collection<String> columnList) {
        int number = 1; // numbering for mistake of CSV/TSV
        for (String column : columnList) {
            br.addElement(number + ": " + column);
            ++number;
        }
    }

    // ===================================================================================
    //                                                                     Date Adjustment
    //                                                                     ===============
    public void resolveRelativeDate(String dataDirectory, DfLoadedSchemaTable schemaTable, DfColumnBindTypeProvider bindTypeProvider,
            Map<String, Object> columnValueMap, Map<String, DfColumnMeta> columnMetaMap, Set<String> sysdateColumnSet, int rowNumber) { // was born at LUXA
        final DfDateAdjustmentRowExecutor executor = new DfDateAdjustmentRowExecutor(dataDirectory, schemaTable, dir -> {
            return findLoadingControlMap(dir);
        }, bindTypeProvider, _relativeDateResolver);
        executor.executeDateAdjustment(columnValueMap, columnMetaMap, sysdateColumnSet, rowNumber);
    }

    // ===================================================================================
    //                                                                    RTrim Cell Value
    //                                                                    ================
    public boolean isRTrimCellValue(String dataDirectory) { // basically for compatible
        final Map<String, Object> loadingControlMap = findLoadingControlMap(dataDirectory);
        final String prop = (String) loadingControlMap.get("isRTrimCellValue");
        if (isSpecifiedValidProperty(prop)) {
            return prop.trim().equalsIgnoreCase("true");
        }
        return false; // default is NO-trimming since 1.0.5F
    }

    // ===================================================================================
    //                                                                          Large Text
    //                                                                          ==========
    public boolean isLargeTextFile(String dataDirectory, DfLoadedSchemaTable schemaTable, String columnName) {
        final Map<String, Object> largeTextFileMap = getLargeTextFileMap(dataDirectory);
        if (largeTextFileMap == null || largeTextFileMap.isEmpty()) {
            return false;
        }
        // also same reason as date adjustment, see the comment for the detail by jflute (2025/04/28)
        final String onfileTableName = schemaTable.getOnfileTableName();
        @SuppressWarnings("unchecked")
        final List<String> columnList = (List<String>) largeTextFileMap.get(onfileTableName);
        if (columnList == null || columnList.isEmpty()) {
            return false;
        }
        return DfNameHintUtil.isTargetByHint(columnName, columnList, DfCollectionUtil.emptyList());
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> getLargeTextFileMap(String dataDirectory) {
        final Map<String, Object> loadingControlMap = findLoadingControlMap(dataDirectory);
        return (Map<String, Object>) loadingControlMap.get(PROP_LARGE_TEXT_FILE_MAP);
    }

    // ===================================================================================
    //                                                                         Batch Limit
    //                                                                         ===========
    public Integer getDelimiterDataBatchLimit(String dataDirectory) { // closet, basically for framework test
        final Map<String, Object> loadingControlMap = findLoadingControlMap(dataDirectory);
        final String prop = (String) loadingControlMap.get("delimiterDataBatchLimit"); // since 1.2.5
        try {
            return DfTypeUtil.toInteger(prop); // null allowed, default is defined at caller
        } catch (RuntimeException e) {
            throw new IllegalStateException("Not number value for delimiterDataBatchLimit: " + prop, e);
        }
    }

    // ===================================================================================
    //                                                                 Loading Control Map
    //                                                                 ===================
    protected Map<String, Object> findLoadingControlMap(String dataDirectory) { // not null, empty allowed
        final Map<String, Object> cachedMap = _loadingControlMapMap.get(dataDirectory);
        if (cachedMap != null) {
            return cachedMap;
        }
        final DfOutsideMapPropReader reader = new DfOutsideMapPropReader();
        final String path = dataDirectory + "/" + LOADING_CONTROL_MAP_NAME;
        final Map<String, Object> datapropPlainMap = reader.readMap(path); // empty allowed if not found
        final Map<String, Object> analyzedMap = new LinkedHashMap<String, Object>();
        if (datapropPlainMap != null && !datapropPlainMap.isEmpty()) {
            analyzeLoadingControlMap(dataDirectory, datapropPlainMap, analyzedMap);
        }
        _loadingControlMapMap.put(dataDirectory, analyzedMap);
        return _loadingControlMapMap.get(dataDirectory);
    }

    protected void analyzeLoadingControlMap(String dataDirectory, Map<String, Object> datapropPlainMap, Map<String, Object> analyzedMap) {
        if (_log.isInfoEnabled()) {
            _log.info("...Analyzing loadingControlMap:");
        }
        for (Entry<String, Object> datapropPlainEntry : datapropPlainMap.entrySet()) {
            final String datapropPlainKey = datapropPlainEntry.getKey();
            final Object datapropPlainValue = datapropPlainEntry.getValue();
            if (PROP_DATE_ADJUSTMENT_MAP.equals(datapropPlainKey)) {
                analyzeDateAdjustmentMap(dataDirectory, analyzedMap, datapropPlainKey, datapropPlainValue);
            } else if (PROP_LARGE_TEXT_FILE_MAP.equals(datapropPlainKey)) {
                analyzeLargeTextFileMap(dataDirectory, analyzedMap, datapropPlainKey, datapropPlainValue);
            } else {
                analyzedMap.put(datapropPlainKey, datapropPlainValue);
            }
        }
        showLoadingControlMap(analyzedMap);
    }

    // -----------------------------------------------------
    //                                       Date Adjustment
    //                                       ---------------
    protected void analyzeDateAdjustmentMap(String dataDirectory, Map<String, Object> analyzedMap, String datapropPlainKey,
            Object datapropPlainValue) {
        final Map<String, Object> dateAdjustmentMap = _dateAdjustmentPreparer.prepareDateAdjustmentMap(dataDirectory, datapropPlainValue);
        analyzedMap.put(datapropPlainKey, dateAdjustmentMap);
    }

    // -----------------------------------------------------
    //                                       Large Text File
    //                                       ---------------
    protected void analyzeLargeTextFileMap(String dataDirectory, Map<String, Object> analyzedMap, String key, Object value) {
        // ; $$ALL$$ = list:{suffix:_TEXT}
        // ; MEMBER = list:{MEMBER_NAME}
        final Map<String, Object> flTableMap = StringKeyMap.createAsFlexibleOrdered();
        @SuppressWarnings("unchecked")
        final Map<String, Object> elementTableMap = (Map<String, Object>) value;
        for (Entry<String, Object> elementTableEntry : elementTableMap.entrySet()) {
            final String tableName = elementTableEntry.getKey();
            final Object columnList = elementTableEntry.getValue();
            flTableMap.put(tableName, columnList);
        }
        analyzedMap.put(key, flTableMap);
    }

    // -----------------------------------------------------
    //                                      Logging Analyzed
    //                                      ----------------
    protected void showLoadingControlMap(Map<String, Object> analyzedMap) {
        if (!_log.isInfoEnabled()) {
            return;
        }
        _log.info("map:{");
        for (Entry<String, Object> entry : analyzedMap.entrySet()) {
            if (PROP_DATE_ADJUSTMENT_MAP.equals(entry.getKey())) {
                _log.info("    " + entry.getKey() + " = map:{");
                @SuppressWarnings("unchecked")
                final Map<String, Object> adjustmentMap = (Map<String, Object>) entry.getValue();
                for (Entry<String, Object> adjustmentEntry : adjustmentMap.entrySet()) {
                    final String filteredValue = filterLoggingValue(adjustmentEntry.getValue());
                    _log.info("        " + adjustmentEntry.getKey() + " = " + filteredValue);
                }
                _log.info("    }");
            } else {
                final String filteredValue = filterLoggingValue(entry.getValue());
                _log.info("    " + entry.getKey() + " = " + filteredValue);
            }
        }
        _log.info("}");
    }

    protected String filterLoggingValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof java.util.Date) {
            return DfTypeUtil.toString(value, "yyyy/MM/dd");
        }
        return value.toString();
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected boolean isSpecifiedValidProperty(String prop) {
        return prop != null && prop.trim().length() > 0 && !prop.trim().equalsIgnoreCase("null");
    }
}
