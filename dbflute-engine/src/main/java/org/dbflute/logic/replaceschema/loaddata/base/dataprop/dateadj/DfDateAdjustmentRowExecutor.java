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
package org.dbflute.logic.replaceschema.loaddata.base.dataprop.dateadj;

import java.math.BigDecimal;
import java.sql.Time;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import org.dbflute.exception.DfLoadDataRegistrationFailureException;
import org.dbflute.exception.ParseDateExpressionFailureException;
import org.dbflute.helper.HandyDate;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.logic.jdbc.metadata.info.DfColumnMeta;
import org.dbflute.logic.replaceschema.loaddata.base.DfLoadedSchemaTable;
import org.dbflute.logic.replaceschema.loaddata.base.dataprop.DfLoadingControlProp;
import org.dbflute.logic.replaceschema.loaddata.base.secretary.DfColumnBindTypeProvider;
import org.dbflute.logic.replaceschema.loaddata.base.secretary.DfRelativeDateResolver;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.DfNameHintUtil;
import org.dbflute.util.DfTypeUtil;
import org.dbflute.util.DfTypeUtil.ParseDateException;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.3.2 split from DfLoadingControlProp (2026/01/17 Saturday at ichihara)
 */
public class DfDateAdjustmentRowExecutor {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final String PROP_DATE_ADJUSTMENT_MAP = DfLoadingControlProp.PROP_DATE_ADJUSTMENT_MAP;

    protected static final String KEY_ORIGIN_DATE = DfDateAdjustmentPreparer.KEY_ORIGIN_DATE;
    protected static final String KEY_MILLIS_COLUMN_LIST = DfDateAdjustmentPreparer.KEY_MILLIS_COLUMN_LIST;
    protected static final String KEY_DATE_ADJ_ALL_MARK = DfDateAdjustmentPreparer.KEY_ALL_MARK;
    protected static final String KEY_DISTANCE_YEARS = DfDateAdjustmentPreparer.KEY_DISTANCE_YEARS;
    protected static final String KEY_DISTANCE_MONTHS = DfDateAdjustmentPreparer.KEY_DISTANCE_MONTHS;
    protected static final String KEY_DISTANCE_DAYS = DfDateAdjustmentPreparer.KEY_DISTANCE_DAYS;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _dataDirectory; // not null
    protected final DfLoadedSchemaTable _schemaTable; // not null
    protected final Function<String, Map<String, Object>> _loadingControlMapFinder; // not null
    protected final DfColumnBindTypeProvider _bindTypeProvider; // not null
    protected final DfRelativeDateResolver _relativeDateResolver; // not null

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfDateAdjustmentRowExecutor(String dataDirectory, DfLoadedSchemaTable schemaTable,
            Function<String, Map<String, Object>> loadingControlMapFinder, DfColumnBindTypeProvider bindTypeProvider,
            DfRelativeDateResolver relativeDateResolver) {
        _dataDirectory = dataDirectory;
        _schemaTable = schemaTable;
        _loadingControlMapFinder = loadingControlMapFinder;
        _bindTypeProvider = bindTypeProvider;
        _relativeDateResolver = relativeDateResolver;
    }

    // ===================================================================================
    //                                                                     Date Adjustment
    //                                                                     ===============
    public void executeDateAdjustment(Map<String, Object> columnValueMap, Map<String, DfColumnMeta> columnMetaMap,
            Set<String> sysdateColumnSet, int rowNumber) { // was born at LUXA
        if (!hasDateAdjustment()) {
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
            if (!hasDateAdjustmentExp(columnName)) { // no-adjustment column
                continue;
            }
            final DfColumnMeta columnMeta = columnMetaMap.get(columnName);
            final Class<?> bindType = _bindTypeProvider.provide(_schemaTable, columnMeta);
            if (bindType == null) { // unknown column type
                continue;
            }
            if (!isDateAdjustmentAllowedBindType(columnName, bindType)) { // cannot be date
                continue;
            }
            final String dateExp = toAdjustedResourceDateExp(columnName, bindType, value);
            if (dateExp == null) { // e.g. wrong value
                continue;
            }
            final String adjusted = adjustDateIfNeeds(columnName, dateExp, rowNumber);
            resolvedMap.put(columnName, convertAdjustedValueToDateType(columnName, bindType, adjusted));
        }
        for (Entry<String, Object> entry : resolvedMap.entrySet()) { // to keep original map instance
            columnValueMap.put(entry.getKey(), entry.getValue());
        }
    }

    protected boolean hasDateAdjustment() { // first check (for performance)
        final Map<String, Object> adjustmentMap = getDateAdjustmentMap();
        if (adjustmentMap == null) {
            return false;
        }
        // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/ by jflute (2025/04/28)
        // matching with on-file table name in dataprop
        // if schema prefix on file name, also table name in dataprop should have it
        // _/_/_/_/_/_/_/_/_/_/
        final String onfileTableName = _schemaTable.getOnfileTableName();
        return adjustmentMap.containsKey(onfileTableName) || adjustmentMap.containsKey(KEY_DATE_ADJ_ALL_MARK);
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

    protected boolean hasDateAdjustmentExp(String columnName) { // second check
        return getDateAdjustmentExp(columnName) != null;
    }

    protected boolean isDateAdjustmentAllowedBindType(String columnName, Class<?> bindType) {
        if (isDateStampType(bindType)) {
            return true; // util.Date and sql.Timestamp
        }
        if (Long.class.isAssignableFrom(bindType)) {
            final Map<String, Object> dateAdjustmentMap = getDateAdjustmentMap();
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
        if (isDateAdjustmentPinpointColumn(columnName)) {
            // cannot be date adjustment column but specified as pinpoint
            throwLoadingControlDateAdjustmentColumnCannotDateException(columnName, bindType);
        }
        return false;
    }

    protected void throwLoadingControlDateAdjustmentColumnCannotDateException(String columnName, Class<?> bindType) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Found the column that cannot be date adjustment column.");
        br.addItem("Advice");
        br.addElement("The column cannot be date adjustment column");
        br.addElement("but specified as pinpoint in your loadingControlMap.dataprop.");
        br.addItem("Data Directory");
        br.addElement(_dataDirectory);
        br.addItem("Table Name");
        br.addElement(_schemaTable);
        br.addItem("Column Name");
        br.addElement(columnName);
        br.addItem("Bind Type");
        br.addElement(bindType);
        final String msg = br.buildExceptionMessage();
        throw new DfLoadDataRegistrationFailureException(msg);
    }

    protected String toAdjustedResourceDateExp(String columnName, Class<?> bindType, Object value) {
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
        throw new IllegalStateException("Unknown bind type: " + bindType + " for " + _schemaTable + "." + columnName);
    }

    protected Object convertAdjustedValueToDateType(String columnName, Class<?> bindType, String adjusted) {
        if (isDateStampType(bindType)) {
            return adjusted; // converted later (when registration)
        } else if (Long.class.isAssignableFrom(bindType)) {
            return new HandyDate(adjusted).getDate().getTime();
        }
        // no way (already checked)
        throw new IllegalStateException("Unknown bind type: " + bindType + " for " + _schemaTable + "." + columnName);
    }

    protected boolean isDateStampType(Class<?> bindType) {
        return java.util.Date.class.isAssignableFrom(bindType) && !Time.class.isAssignableFrom(bindType);
    }

    // -----------------------------------------------------
    //                                           Adjust Date
    //                                           -----------
    protected String adjustDateIfNeeds(String columnName, String dateExp, int rowNumber) {
        if (dateExp == null || dateExp.trim().length() == 0) { // basically no way (already checked)
            return dateExp;
        }
        final Map<String, Object> dateAdjustmentMap = getDateAdjustmentMap();
        if (dateAdjustmentMap == null) { // basically no way (already checked)
            return dateExp;
        }
        final String adjustmentExp = getDateAdjustmentExp(columnName);
        if (adjustmentExp == null || adjustmentExp.trim().length() == 0) { // basically no way (already checked)
            return dateExp;
        }
        final java.util.Date date;
        try {
            date = new HandyDate(dateExp).getDate();
        } catch (ParseDateExpressionFailureException e) { // basically no way (already checked)
            throwLoadingControlColumnValueParseFailureException(adjustmentExp, columnName, dateExp, rowNumber, e);
            return null; // unreachable
        }
        final String filteredExp = filterAdjustmentExp(dateAdjustmentMap, adjustmentExp);
        return _relativeDateResolver.resolveRelativeDate(_schemaTable, columnName, filteredExp, date);
    }

    protected void throwLoadingControlColumnValueParseFailureException(String adjustmentExp, String columnName, String value, int rowNumber,
            ParseDateExpressionFailureException e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to parse the value of the column for date adjustment.");
        br.addItem("Adjustment Expression");
        br.addElement(adjustmentExp);
        br.addItem("Data Directory");
        br.addElement(_dataDirectory);
        br.addItem("Table Name");
        br.addElement(_schemaTable);
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
    protected String getDateAdjustmentExp(String columnName) {
        final Map<String, Object> dateAdjustmentMap = getDateAdjustmentMap();
        if (dateAdjustmentMap == null) {
            return null;
        }
        final String onfileTableName = _schemaTable.getOnfileTableName();
        Map<String, String> columnMap = (Map<String, String>) dateAdjustmentMap.get(onfileTableName);
        final String foundExp = findAdjustmentExp(columnName, columnMap);
        if (foundExp != null) {
            return foundExp;
        }
        columnMap = (Map<String, String>) dateAdjustmentMap.get(KEY_DATE_ADJ_ALL_MARK);
        return findAdjustmentExp(columnName, columnMap);
    }

    protected String findAdjustmentExp(String columnName, Map<String, String> columnMap) {
        if (columnMap != null) {
            final String exp = columnMap.get(columnName);
            if (exp != null) {
                return exp;
            }
            return columnMap.get(KEY_DATE_ADJ_ALL_MARK);
        }
        return null;
    }

    protected boolean isDateAdjustmentPinpointColumn(String columnName) {
        final Map<String, Object> dateAdjustmentMap = getDateAdjustmentMap(); // null allowed
        if (dateAdjustmentMap == null) {
            return false;
        }
        final String onfileTableName = _schemaTable.getOnfileTableName();
        @SuppressWarnings("unchecked")
        final Map<String, String> columnMap = (Map<String, String>) dateAdjustmentMap.get(onfileTableName);
        if (columnMap != null && columnMap.get(columnName) != null) {
            return true;
        }
        @SuppressWarnings("unchecked")
        final Map<String, String> allTableColumnMap = (Map<String, String>) dateAdjustmentMap.get(KEY_DATE_ADJ_ALL_MARK);
        return allTableColumnMap != null && allTableColumnMap.get(columnName) != null;
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> getDateAdjustmentMap() { // null allowed
        final Map<String, Object> loadingControlMap = _loadingControlMapFinder.apply(_dataDirectory); // not null
        return (Map<String, Object>) loadingControlMap.get(PROP_DATE_ADJUSTMENT_MAP);
    }
}
