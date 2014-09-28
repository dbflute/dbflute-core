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
package org.seasar.dbflute.logic.replaceschema.loaddata.impl.dataprop;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.dbflute.exception.DfIllegalPropertySettingException;
import org.seasar.dbflute.exception.DfLoadDataRegistrationFailureException;
import org.seasar.dbflute.exception.ParseDateExpressionFailureException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.helper.HandyDate;
import org.seasar.dbflute.helper.StringKeyMap;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfColumnMeta;
import org.seasar.dbflute.logic.replaceschema.loaddata.DfColumnBindTypeProvider;
import org.seasar.dbflute.logic.replaceschema.loaddata.impl.DfRelativeDateResolver;
import org.seasar.dbflute.properties.propreader.DfOutsideMapPropReader;
import org.seasar.dbflute.resource.DBFluteSystem;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.DfNameHintUtil;
import org.seasar.dbflute.util.DfTypeUtil;
import org.seasar.dbflute.util.DfTypeUtil.ParseDateException;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.0.4A (2013/03/09 Saturday)
 */
public class DfLoadingControlProp {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log _log = LogFactory.getLog(DfLoadingControlProp.class);
    public static final String LOADING_CONTROL_MAP_NAME = "loadingControlMap.dataprop";
    public static final String PROP_DATE_ADJUSTMENT_MAP = "dateAdjustmentMap";
    public static final String KEY_ORIGIN_DATE = "df:originDate";
    public static final String KEY_MILLIS_COLUMN_LIST = "df:millisColumnList";
    protected static final String KEY_ALL_MARK = "$$ALL$$";
    protected static final String KEY_DISTANCE_YEARS = "df:distanceYears";
    protected static final String KEY_DISTANCE_MONTHS = "df:distanceMonths";
    protected static final String KEY_DISTANCE_DAYS = "df:distanceDays";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Map<String, Map<String, Object>> _loadingControlMapMap = DfCollectionUtil.newLinkedHashMap();
    protected final DfRelativeDateResolver _relativeDateResolver = new DfRelativeDateResolver();

    // ===================================================================================
    //                                                                 Logging Insert Type
    //                                                                 ===================
    public LoggingInsertType getLoggingInsertType(String dataDirectory, boolean loggingInsertSql) {
        final Map<String, Object> loadingControlMap = getLoadingControlMap(dataDirectory);
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
        final Map<String, Object> loadingControlMap = getLoadingControlMap(dataDirectory);
        final String prop = (String) loadingControlMap.get("isSuppressBatchUpdate");
        if (isSpecifiedValidProperty(prop)) {
            return prop.trim().equalsIgnoreCase("true");
        }
        return suppressBatchUpdate;
    }

    // ===================================================================================
    //                                                                 ColumnDef Existence
    //                                                                 ===================
    public boolean isCheckColumnDefExistence(String dataDirectory) {
        final Map<String, Object> loadingControlMap = getLoadingControlMap(dataDirectory);
        final String prop = (String) loadingControlMap.get("isSuppressColumnDefCheck");
        if (isSpecifiedValidProperty(prop) && prop.trim().equalsIgnoreCase("true")) {
            return false; // suppress
        }
        return true; // default is checked
    }

    public void checkColumnDefExistence(String dataDirectory, File dataFile, String tableName,
            List<String> columnDefNameList, Map<String, DfColumnMeta> columnMetaMap) {
        final List<String> unneededList = new ArrayList<String>();
        for (String columnName : columnDefNameList) {
            if (!columnMetaMap.containsKey(columnName)) {
                unneededList.add(columnName);
            }
        }
        if (!unneededList.isEmpty()) {
            throwLoadingControlNoExistenceColumnFoundException(dataDirectory, dataFile, tableName, unneededList);
        }
    }

    protected void throwLoadingControlNoExistenceColumnFoundException(String dataDirectory, File dataFile,
            String tableName, List<String> unneededList) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Found the no-exist column in your data file.");
        br.addItem("Data Directory");
        br.addElement(dataDirectory);
        br.addItem("Data File");
        br.addElement(dataFile);
        br.addItem("Table Name");
        br.addElement(tableName);
        br.addItem("Found Column");
        for (String columnName : unneededList) {
            br.addElement(columnName);
        }
        final String msg = br.buildExceptionMessage();
        throw new DfLoadDataRegistrationFailureException(msg);
    }

    // ===================================================================================
    //                                                                     Date Adjustment
    //                                                                     ===============
    public void resolveRelativeDate(String dataDirectory, String tableName, Map<String, Object> columnValueMap,
            Map<String, DfColumnMeta> columnMetaMap, Set<String> sysdateColumnSet,
            DfColumnBindTypeProvider bindTypeProvider, int rowNumber) { // was born at LUXA
        if (!hasDateAdjustment(dataDirectory, tableName)) {
            return;
        }
        final Map<String, Object> resolvedMap = new HashMap<String, Object>();
        for (Entry<String, Object> entry : columnValueMap.entrySet()) {
            final String columnName = entry.getKey();
            if (isSysdateColumn(sysdateColumnSet, columnName)) { // keep sysdate as default value
                continue;
            }
            final Object value = entry.getValue();
            if (value == null) {
                continue;
            }
            if (!isDateAdjustmentAllowedValueType(value)) { // out of target type
                continue;
            }
            if (!hasDateAdjustmentExp(dataDirectory, tableName, columnName)) { // no-adjustment column
                continue;
            }
            final DfColumnMeta columnMeta = columnMetaMap.get(columnName);
            final Class<?> bindType = bindTypeProvider.provide(tableName, columnMeta);
            if (bindType == null) { // unknown column type
                continue;
            }
            if (!isDateAdjustmentAllowedBindType(dataDirectory, tableName, columnName, bindType)) { // cannot be date
                continue;
            }
            final String dateExp = toAdjustedResourceDateExp(tableName, columnName, bindType, value);
            if (dateExp == null) { // e.g. wrong value
                continue;
            }
            final String adjusted = adjustDateIfNeeds(dataDirectory, tableName, columnName, dateExp, rowNumber);
            resolvedMap.put(columnName, convertAdjustedValueToDateType(tableName, columnName, bindType, adjusted));
        }
        for (Entry<String, Object> entry : resolvedMap.entrySet()) { // to keep original map instance
            columnValueMap.put(entry.getKey(), entry.getValue());
        }
    }

    protected boolean hasDateAdjustment(String dataDirectory, String tableName) { // first check (for performance)
        final Map<String, Object> adjustmentMap = getDateAdjustmentMap(dataDirectory);
        if (adjustmentMap == null) {
            return false;
        }
        return adjustmentMap.containsKey(tableName) || adjustmentMap.containsKey(KEY_ALL_MARK);
    }

    protected boolean isSysdateColumn(Set<String> sysdateColumnSet, String columnName) {
        return sysdateColumnSet != null && sysdateColumnSet.contains(columnName);
    }

    protected boolean isDateAdjustmentAllowedValueType(Object value) {
        return (value instanceof java.util.Date && !(value instanceof Time)) // util.Date and sql.Timestamp
                || isDateAdjustmentMillisColumnAllowedNumberValueType(value) // for millisecond column
                || value instanceof String; // date or millisecond column
    }

    protected boolean isDateAdjustmentMillisColumnAllowedNumberValueType(Object value) {
        // Long is just fit, Integer is just in case, but POI returns actually BigDecimal
        return value instanceof Long || value instanceof Integer || value instanceof BigDecimal;
    }

    protected boolean hasDateAdjustmentExp(String dataDirectory, String tableName, String columnName) { // second check
        return getDateAdjustmentExp(dataDirectory, tableName, columnName) != null;
    }

    protected boolean isDateAdjustmentAllowedBindType(String dataDirectory, String tableName, String columnName,
            Class<?> bindType) {
        if (isDateStampType(bindType)) {
            return true; // util.Date and sql.Timestamp
        }
        if (Long.class.isAssignableFrom(bindType)) {
            final Map<String, Object> dateAdjustmentMap = getDateAdjustmentMap(dataDirectory);
            if (dateAdjustmentMap != null) { // not null but just in case
                @SuppressWarnings("unchecked")
                final List<String> millisColumn = (List<String>) dateAdjustmentMap.get(KEY_MILLIS_COLUMN_LIST);
                if (millisColumn != null) {
                    final List<String> emptyList = DfCollectionUtil.emptyList();
                    if (DfNameHintUtil.isTargetByHint(columnName, millisColumn, emptyList)) {
                        return true; // millisecond column
                    }
                }
            }
        }
        if (isDateAdjustmentPinpointColumn(dataDirectory, tableName, columnName)) {
            // cannot be date adjustment column but specified as pinpoint
            throwLoadingControlDateAdjustmentColumnCannotDateException(dataDirectory, tableName, columnName, bindType);
        }
        return false;
    }

    protected void throwLoadingControlDateAdjustmentColumnCannotDateException(String dataDirectory, String tableName,
            String columnName, Class<?> bindType) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Found the column that cannot be date adjustment column.");
        br.addItem("Advice");
        br.addElement("The column cannot be date adjustment column");
        br.addElement("but specified as pinpoint in your loadingControlMap.dataprop.");
        br.addItem("Data Directory");
        br.addElement(dataDirectory);
        br.addItem("Table Name");
        br.addElement(tableName);
        br.addItem("Column Name");
        br.addElement(columnName);
        br.addItem("Bind Type");
        br.addElement(bindType);
        final String msg = br.buildExceptionMessage();
        throw new DfLoadDataRegistrationFailureException(msg);
    }

    protected String toAdjustedResourceDateExp(String tableName, String columnName, Class<?> bindType, Object value) {
        final String resolvedPattern = DfRelativeDateResolver.RESOLVED_PATTERN;
        if (isDateStampType(bindType)) {
            if (value instanceof java.util.Date) { // not contains time (already checked)
                return DfTypeUtil.toString(value, resolvedPattern);
            } else if (isDateAdjustmentMillisColumnAllowedNumberValueType(value)) {
                return null; // will be exception when insert anyhow so do nothing here
            } else if (value instanceof String) {
                final String strValue = ((String) value).trim();
                if (strValue.startsWith(DfRelativeDateResolver.CURRENT_MARK)) { // resolved later
                    return null;
                }
                if (strValue.equals("sysdate")) { // basically no way (might be default value!?)
                    return null;
                }
                final java.util.Date parsedDate;
                try {
                    parsedDate = DfTypeUtil.toDate(value);
                } catch (ParseDateException ignored) { // wrong value for date type
                    return null; // will be exception when insert anyhow so do nothing here
                }
                return DfTypeUtil.toString(parsedDate, resolvedPattern);
            }
        }
        if (Long.class.isAssignableFrom(bindType)) {
            if (value instanceof java.util.Date) { // not contains time (already checked)
                return DfTypeUtil.toString(value, resolvedPattern);
            } else if (value instanceof Long) {
                return DfTypeUtil.toString(new java.util.Date((Long) value), resolvedPattern);
            } else { // basically e.g. Integer, BigDecimal, String (not others, already checked)
                try {
                    final Long parsedLong = DfTypeUtil.toLong(value);
                    return DfTypeUtil.toString(new java.util.Date(parsedLong), resolvedPattern);
                } catch (NumberFormatException ignored) { // wrong value for millisecond type
                    try {
                        final java.util.Date parsedDate = DfTypeUtil.toDate(value);
                        return DfTypeUtil.toString(parsedDate, resolvedPattern);
                    } catch (ParseDateException andIgnored) { // wrong value for date type
                        return null; // will be exception when insert anyhow so do nothing here
                    }
                }
            }
        }
        // no way (already checked)
        throw new IllegalStateException("Unknown bind type: " + bindType + " for " + tableName + "." + columnName);
    }

    protected Object convertAdjustedValueToDateType(String tableName, String columnName, Class<?> bindType,
            String adjusted) {
        if (isDateStampType(bindType)) {
            return adjusted; // converted later (when registration)
        } else if (Long.class.isAssignableFrom(bindType)) {
            return new HandyDate(adjusted).getDate().getTime();
        }
        // no way (already checked)
        throw new IllegalStateException("Unknown bind type: " + bindType + " for " + tableName + "." + columnName);
    }

    protected boolean isDateStampType(Class<?> bindType) {
        return java.util.Date.class.isAssignableFrom(bindType) && !Time.class.isAssignableFrom(bindType);
    }

    // -----------------------------------------------------
    //                                           Adjust Date
    //                                           -----------
    protected String adjustDateIfNeeds(String dataDirectory, String tableName, String columnName, String dateExp,
            int rowNumber) {
        if (dateExp == null || dateExp.trim().length() == 0) { // basically no way (already checked)
            return dateExp;
        }
        final Map<String, Object> dateAdjustmentMap = getDateAdjustmentMap(dataDirectory);
        if (dateAdjustmentMap == null) { // basically no way (already checked)
            return dateExp;
        }
        final String adjustmentExp = getDateAdjustmentExp(dataDirectory, tableName, columnName);
        if (adjustmentExp == null || adjustmentExp.trim().length() == 0) { // basically no way (already checked)
            return dateExp;
        }
        final java.util.Date date;
        try {
            date = new HandyDate(dateExp).getDate();
        } catch (ParseDateExpressionFailureException e) { // basically no way (already checked)
            throwLoadingControlColumnValueParseFailureException(adjustmentExp, dataDirectory, tableName, columnName,
                    dateExp, rowNumber, e);
            return null; // unreachable
        }
        final String filteredExp = filterAdjustmentExp(dateAdjustmentMap, adjustmentExp);
        return _relativeDateResolver.resolveRelativeDate(tableName, columnName, filteredExp, date);
    }

    protected void throwLoadingControlColumnValueParseFailureException(String adjustmentExp, String dataDirectory,
            String tableName, String columnName, String value, int rowNumber, ParseDateExpressionFailureException e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to parse the value of the column for date adjustment.");
        br.addItem("Adjustment Expression");
        br.addElement(adjustmentExp);
        br.addItem("Data Directory");
        br.addElement(dataDirectory);
        br.addItem("Table Name");
        br.addElement(tableName);
        br.addItem("Column Name");
        br.addElement(columnName);
        br.addItem("Column Value");
        br.addElement(value);
        br.addItem("Row Number");
        br.addElement(rowNumber);
        final String msg = br.buildExceptionMessage();
        throw new DfLoadDataRegistrationFailureException(msg, e);
    }

    protected String filterAdjustmentExp(Map<String, Object> dateAdjustmentMap, String adjustmentExp) {
        String filtered = adjustmentExp;
        final Integer years = (Integer) dateAdjustmentMap.get(KEY_DISTANCE_YEARS);
        if (years != null) {
            filtered = Srl.replace(filtered, "addYear($distance)", "addYear(" + years + ")");
            filtered = Srl.replace(filtered, "$distanceYears", years.toString());
        }
        final Integer months = (Integer) dateAdjustmentMap.get(KEY_DISTANCE_MONTHS);
        if (months != null) {
            filtered = Srl.replace(filtered, "addMonth($distance)", "addMonth(" + months + ")");
            filtered = Srl.replace(filtered, "$distanceMonths", months.toString());
        }
        final Integer days = (Integer) dateAdjustmentMap.get(KEY_DISTANCE_DAYS);
        if (days != null) {
            filtered = Srl.replace(filtered, "addDay($distance)", "addDay(" + days + ")");
            filtered = Srl.replace(filtered, "$distanceDays", days.toString());
        }
        return filtered;
    }

    @SuppressWarnings("unchecked")
    protected String getDateAdjustmentExp(String dataDirectory, String tableName, String columnName) {
        final Map<String, Object> dateAdjustmentMap = getDateAdjustmentMap(dataDirectory);
        if (dateAdjustmentMap == null) {
            return null;
        }
        Map<String, String> columnMap = (Map<String, String>) dateAdjustmentMap.get(tableName);
        final String foundExp = findAdjustmentExp(tableName, columnName, columnMap);
        if (foundExp != null) {
            return foundExp;
        }
        columnMap = (Map<String, String>) dateAdjustmentMap.get(KEY_ALL_MARK);
        return findAdjustmentExp(tableName, columnName, columnMap);
    }

    protected String findAdjustmentExp(String tableName, String columnName, Map<String, String> columnMap) {
        if (columnMap != null) {
            final String exp = columnMap.get(columnName);
            if (exp != null) {
                return exp;
            }
            return columnMap.get(KEY_ALL_MARK);
        }
        return null;
    }

    protected boolean isDateAdjustmentPinpointColumn(String dataDirectory, String tableName, String columnName) {
        final Map<String, Object> dateAdjustmentMap = getDateAdjustmentMap(dataDirectory);
        if (dateAdjustmentMap == null) {
            return false;
        }
        @SuppressWarnings("unchecked")
        final Map<String, String> columnMap = (Map<String, String>) dateAdjustmentMap.get(tableName);
        if (columnMap != null && columnMap.get(columnName) != null) {
            return true;
        }
        @SuppressWarnings("unchecked")
        final Map<String, String> allTableColumnMap = (Map<String, String>) dateAdjustmentMap.get(KEY_ALL_MARK);
        return allTableColumnMap != null && allTableColumnMap.get(columnName) != null;
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> getDateAdjustmentMap(String dataDirectory) {
        final Map<String, Object> loadingControlMap = getLoadingControlMap(dataDirectory);
        return (Map<String, Object>) loadingControlMap.get(PROP_DATE_ADJUSTMENT_MAP);
    }

    // ===================================================================================
    //                                                                    RTrim Cell Value
    //                                                                    ================
    public boolean isRTrimCellValue(String dataDirectory) { // basically for compatible
        final Map<String, Object> loadingControlMap = getLoadingControlMap(dataDirectory);
        final String prop = (String) loadingControlMap.get("isRTrimCellValue");
        if (isSpecifiedValidProperty(prop)) {
            return prop.trim().equalsIgnoreCase("true");
        }
        return false; // default is NO-trimming since 1.0.5F
    }

    // ===================================================================================
    //                                                                 Loading Control Map
    //                                                                 ===================
    protected Map<String, Object> getLoadingControlMap(String dataDirectory) {
        final Map<String, Object> cachedMap = _loadingControlMapMap.get(dataDirectory);
        if (cachedMap != null) {
            return cachedMap;
        }
        final DfOutsideMapPropReader reader = new DfOutsideMapPropReader();
        final String path = dataDirectory + "/" + LOADING_CONTROL_MAP_NAME;
        final Map<String, Object> resultMap = reader.readMap(path);
        final Map<String, Object> analyzedMap = new LinkedHashMap<String, Object>();
        if (resultMap != null && !resultMap.isEmpty()) {
            analyzeLoadingControlMap(dataDirectory, resultMap, analyzedMap);
        }
        _loadingControlMapMap.put(dataDirectory, analyzedMap);
        return _loadingControlMapMap.get(dataDirectory);
    }

    protected void analyzeLoadingControlMap(String dataDirectory, Map<String, Object> resultMap,
            Map<String, Object> analyzedMap) {
        if (_log.isInfoEnabled()) {
            _log.info("...Analyzing loadingControlMap:");
        }
        for (Entry<String, Object> entry : resultMap.entrySet()) {
            final String key = entry.getKey();
            final Object value = entry.getValue();
            if (PROP_DATE_ADJUSTMENT_MAP.equals(key)) {
                analyzeDateAdjustmentMap(dataDirectory, analyzedMap, key, value);
            } else {
                analyzedMap.put(key, value);
            }
        }
        showLoadingControlMap(analyzedMap);
    }

    protected void analyzeDateAdjustmentMap(String dataDirectory, Map<String, Object> analyzedMap, String key,
            Object value) {
        // ; df:originDate = 2013/03/09
        // ; $$ALL$$ = addDay($distance)
        // ; MEMBER = map:{
        //     ; BIRTHDATE = addDay(6)
        // }
        final Map<String, Object> flTableMap = StringKeyMap.createAsFlexibleOrdered();
        @SuppressWarnings("unchecked")
        final Map<String, Object> elementTableMap = (Map<String, Object>) value;
        for (Entry<String, Object> elementTableEntry : elementTableMap.entrySet()) {
            final String tableName = elementTableEntry.getKey();
            final Object elementTableValue = elementTableEntry.getValue();
            final Object registeredTableValue;
            if (elementTableValue != null) {
                if (KEY_ORIGIN_DATE.equalsIgnoreCase(tableName)) {
                    final String originExp = elementTableValue.toString();
                    final HandyDate originDate;
                    try {
                        originDate = new HandyDate(originExp);
                    } catch (ParseDateExpressionFailureException e) {
                        throwLoadingControlOriginDateParseFailureException(dataDirectory, originExp, e);
                        return; // unreachable
                    }
                    final java.util.Date currentDate = DBFluteSystem.currentDate();
                    registeredTableValue = originDate.getDate();
                    flTableMap.put(KEY_DISTANCE_YEARS, originDate.calculateCalendarDistanceYears(currentDate));
                    flTableMap.put(KEY_DISTANCE_MONTHS, originDate.calculateCalendarDistanceMonths(currentDate));
                    flTableMap.put(KEY_DISTANCE_DAYS, originDate.calculateCalendarDistanceDays(currentDate));
                } else if (KEY_MILLIS_COLUMN_LIST.equalsIgnoreCase(tableName)) {
                    registeredTableValue = elementTableValue; // not need filter
                } else {
                    @SuppressWarnings("unchecked")
                    final Map<String, Object> elementColumnMap = (Map<String, Object>) elementTableValue;
                    final Map<String, Object> flColumnMap = StringKeyMap.createAsFlexibleOrdered();
                    flColumnMap.putAll(elementColumnMap);
                    registeredTableValue = flColumnMap;
                }
            } else {
                registeredTableValue = null;
            }
            flTableMap.put(tableName, registeredTableValue);
        }
        analyzedMap.put(key, flTableMap);
    }

    protected void throwLoadingControlOriginDateParseFailureException(String dataDirectory, String value,
            ParseDateExpressionFailureException e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to parse the value of the origin date.");
        br.addItem("Advcei");
        br.addElement("Make sure your origin date in the loadingControlMap.dataprop.");
        br.addElement("The date expression should be e.g. 'yyyy/MM/dd HH:mm:ss.SSS'.");
        br.addItem("Data Directory");
        br.addElement(dataDirectory);
        br.addItem("Column Value");
        br.addElement(value);
        final String msg = br.buildExceptionMessage();
        throw new DfLoadDataRegistrationFailureException(msg, e);
    }

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
