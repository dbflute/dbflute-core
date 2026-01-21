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
package org.dbflute.logic.replaceschema.loaddata.base.dataprop.dateadj.origindate;

import java.util.List;
import java.util.Map;

import org.apache.torque.engine.database.model.TypeMap;
import org.dbflute.exception.DfIllegalPropertySettingException;
import org.dbflute.helper.HandyDate;
import org.dbflute.helper.dfmap.DfMapStyle;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.logic.jdbc.metadata.info.DfColumnMeta;
import org.dbflute.logic.replaceschema.loaddata.base.DfLoadedSchemaTable;
import org.dbflute.logic.replaceschema.loaddata.base.dataprop.dateadj.DfDateAdjustmentPreparer;
import org.dbflute.util.DfTypeUtil;
import org.dbflute.util.DfTypeUtil.ParseDateException;
import org.dbflute.util.Srl;
import org.dbflute.util.Srl.ScopeInfo;

/**
 * @author jflute
 * @since 1.3.2 split from DfDateAdjustmentRowExecutor (2026/01/20 Tuesday at ichihara)
 */
public class DfDateAdjustmentOriginDateDistanceFilter {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    // for root originDate
    protected static final String KEY_DISTANCE_YEARS = DfDateAdjustmentPreparer.KEY_DISTANCE_YEARS;
    protected static final String KEY_DISTANCE_MONTHS = DfDateAdjustmentPreparer.KEY_DISTANCE_MONTHS;
    protected static final String KEY_DISTANCE_DAYS = DfDateAdjustmentPreparer.KEY_DISTANCE_DAYS;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _dataDirectory; // not null
    protected final DfLoadedSchemaTable _schemaTable; // not null
    protected final Map<String, Object> _columnValueMap; // not null
    protected final Map<String, DfColumnMeta> _columnMetaMap; // not null

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfDateAdjustmentOriginDateDistanceFilter(String dataDirectory, DfLoadedSchemaTable schemaTable,
            Map<String, Object> columnValueMap, Map<String, DfColumnMeta> columnMetaMap) {
        _dataDirectory = dataDirectory;
        _schemaTable = schemaTable;
        _columnValueMap = columnValueMap;
        _columnMetaMap = columnMetaMap;
    }

    // ===================================================================================
    //                                                                     Filter Distance
    //                                                                     ===============
    public String filterDistanceOnAdjustmentExp(String columnName, String adjustmentExp, Map<String, Object> dateAdjustmentMap) {
        if (!hasDistanceVariable(adjustmentExp)) {
            return adjustmentExp; // no related
        }
        Integer years = null;
        Integer months = null;
        Integer days = null;
        final boolean useRootOrigin;
        if (adjustmentExp.contains("df:myOriginDate(")) { // @since 1.3.2
            // e.g.
            // ; SEA    = addDay($distanceDays) df:myOriginDate(map:{years=0;months=0;days=8})
            // ; HANGAR = addDay($distanceDays) df:myOriginDate(map:{years=0;months=0;days=5}, where MEMBER_ID from 1 to 10)
            // ; MYSTIC = addDay($distanceDays) df:myOriginDate(map:{years=0;months=0;days=8}, where MEMBER_ID from 1 to 2)
            //                                  df:myOriginDate(map:{years=0;months=0;days=10}, where MEMBER_ID from 4 to 5)
            //                                  df:myOriginDate(map:{years=0;months=0;days=12})
            //                                  df:useRootOriginIfNoHit()
            final Map<String, String> distanceMap = deriveMyOriginDateDistanceMap(adjustmentExp);
            if (distanceMap != null) { // hit by where
                years = Integer.valueOf(distanceMap.get("years")); // always integer, not null
                months = Integer.valueOf(distanceMap.get("months")); // me too
                days = Integer.valueOf(distanceMap.get("days")); // me too
                useRootOrigin = false;
            } else { // no hit
                if (adjustmentExp.contains("df:useRootOriginIfNoHit()")) {
                    useRootOrigin = true;
                } else { // no distance
                    years = 0;
                    months = 0;
                    days = 0;
                    useRootOrigin = false;
                }
            }
        } else { // as root origin
            useRootOrigin = true;
        }
        if (useRootOrigin) {
            years = (Integer) dateAdjustmentMap.get(KEY_DISTANCE_YEARS); // null allowed
            months = (Integer) dateAdjustmentMap.get(KEY_DISTANCE_MONTHS); // me too
            days = (Integer) dateAdjustmentMap.get(KEY_DISTANCE_DAYS); // me too
        }
        final String havingDistanceExp = extractHavingDistanceExp(adjustmentExp); // not null
        return evaluateDistance(years, months, days, havingDistanceExp);
    }

    protected boolean hasDistanceVariable(String adjustmentExp) {
        return adjustmentExp.contains("$distance"); // e.g. $distance, $distanceDays, ...
    }

    protected String extractHavingDistanceExp(String adjustmentExp) { // without "df:" option
        // e.g. addDay($distanceDays) df:myOriginDate(map:{years=0;months=0;days=8})
        return Srl.substringFirstFront(adjustmentExp, "df:").trim();
    }

    protected String evaluateDistance(Integer years, Integer months, Integer days, String filtered) {
        if (years != null) {
            filtered = Srl.replace(filtered, "addYear($distance)", "addYear(" + years + ")");
            filtered = Srl.replace(filtered, "$distanceYears", years.toString());
        }
        if (months != null) {
            filtered = Srl.replace(filtered, "addMonth($distance)", "addMonth(" + months + ")");
            filtered = Srl.replace(filtered, "$distanceMonths", months.toString());
        }
        if (days != null) {
            filtered = Srl.replace(filtered, "addDay($distance)", "addDay(" + days + ")");
            filtered = Srl.replace(filtered, "$distanceDays", days.toString());
        }
        return filtered;
    }

    // ===================================================================================
    //                                                                       My OriginDate
    //                                                                       =============
    protected Map<String, String> deriveMyOriginDateDistanceMap(String adjustmentExp) {
        Map<String, String> distanceMap = null;
        final List<ScopeInfo> scopeList = Srl.extractScopeList(adjustmentExp, "df:myOriginDate(", ")");
        for (ScopeInfo scopeInfo : scopeList) {
            final String content = scopeInfo.getContent(); // e.g. map:{years=0;months=0;days=8}, ...

            boolean whereHit = false;
            if (content.contains(",")) { // e.g. df:myOriginDate(2026/01/10, where pk from 1 to 2)
                final String whereCondition = Srl.substringFirstRear(content, ",").trim(); // e.g. if pk from 1 to 2
                if (whereCondition.startsWith("where ") && whereCondition.contains(" from ") && whereCondition.contains(" to ")) {
                    final String whereColumn = Srl.extractScopeFirst(whereCondition, "where ", " from ").getContent();
                    final String numToNumExp = Srl.substringFirstRear(whereCondition, " from ");
                    final String fromExp = Srl.substringFirstFront(numToNumExp, "to").trim();
                    final String toExp = Srl.substringFirstRear(numToNumExp, "to").trim();
                    if (Srl.is_Null_or_TrimmedEmpty(fromExp) || Srl.is_Null_or_TrimmedEmpty(toExp)) {
                        throwMyOriginDateWhereFromToNotFoundException(adjustmentExp, whereCondition, whereColumn, fromExp, toExp);
                    }
                    final DfColumnMeta columnMeta = _columnMetaMap.get(whereColumn);
                    if (columnMeta == null) { // spell miss?
                        throwMyOriginDateWhereColumnNotFoundException(adjustmentExp, whereCondition, whereColumn);
                    }
                    final Object columnValue = _columnValueMap.get(whereColumn);
                    if (columnValue == null) { // or no defined in tsv? but already checked by columnMetaMap
                        continue; // null value means no hit
                    }
                    final String jdbcType = TypeMap.findJdbcTypeByJdbcDefValue(columnMeta.getJdbcDefValue());
                    if (TypeMap.isJdbcTypeIntegerFamily(jdbcType)) { // e.g. Integer, Long
                        final Long numberValue = DfTypeUtil.toLong(columnValue); // may be string expression integer
                        final long whereFromNum;
                        final long whereToNum;
                        try {
                            whereFromNum = DfTypeUtil.toLong(fromExp); // not empty here
                            whereToNum = DfTypeUtil.toLong(toExp); // me too
                        } catch (NumberFormatException e) {
                            throwMyOriginDateWhereFromToNumberFormatException(adjustmentExp, columnMeta, jdbcType, fromExp, toExp, e);
                            return null; // unreachable
                        }
                        if (whereFromNum <= numberValue && numberValue <= whereToNum) {
                            whereHit = true;
                        } else { // no hit so next
                            continue;
                        }
                    } else if (TypeMap.isJdbcTypeDatePartFamily(jdbcType)) { // e.g. Date
                        final HandyDate columnDate = new HandyDate(DfTypeUtil.toDate(columnValue));
                        final HandyDate whereFromDate;
                        final HandyDate whereToDate;
                        try {
                            whereFromDate = new HandyDate(DfTypeUtil.toDate(fromExp)); // not empty here
                            whereToDate = new HandyDate(DfTypeUtil.toDate(toExp)); // me too
                        } catch (ParseDateException e) {
                            throwMyOriginDateWhereFromToDateFormatException(adjustmentExp, columnMeta, jdbcType, fromExp, toExp, e);
                            return null; // unreachable
                        }
                        if (whereFromDate.isLessEqual(columnDate.getDate()) && columnDate.isLessEqual(whereToDate.getDate())) {
                            whereHit = true;
                        } else { // no hit so next
                            continue;
                        }
                    } else { // e.g. Date
                        throwMyOriginDateIfStatementUnsupportedTypeException(adjustmentExp, columnMeta, jdbcType);
                    }
                } else { // other options
                    throwMyOriginDateUnsupportedStatementException(adjustmentExp);
                }
            } else { // no where
                whereHit = true;
            }
            if (whereHit) {
                final String preCalculation = Srl.substringFirstFront(content, ",");
                final Map<String, Object> workingMap = fromMapString(preCalculation);
                @SuppressWarnings("unchecked")
                final Map<String, String> goinStringMap = (Map<String, String>) (Object) workingMap;
                distanceMap = goinStringMap; // may be overridden
                break; // first hit is prior
            }
        }
        return distanceMap; // null allowed if no hit

    }

    protected Map<String, Object> fromMapString(String preCalculation) {
        return new DfMapStyle().printOneLiner().fromMapString(preCalculation);
    }

    // ===================================================================================
    //                                                                           Exception
    //                                                                           =========
    protected void throwMyOriginDateWhereFromToNotFoundException(String adjustmentExp, String whereCondition, String whereColumn,
            String fromExp, String toExp) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the where from/to value on dateAdjustmentMap.");
        br.addItem("Advice");
        br.addElement("Confirm your loadingControlMap.dataprop in the data directory.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    df:myOriginDate(2026/01/20, where MEMBER_ID from  to ) // *Bad");
        br.addElement("  (o):");
        br.addElement("    df:myOriginDate(2026/01/20, where MEMBER_ID from 1 to 234) // Good");
        br.addItem("Data Directory");
        br.addElement(_dataDirectory);
        br.addItem("Table Name");
        br.addElement(_schemaTable);
        br.addItem("columnValueMap");
        br.addElement(_columnValueMap);
        br.addItem("columnMetaMap");
        br.addElement(_columnMetaMap);
        br.addItem("adjustmentExp");
        br.addElement(adjustmentExp);
        br.addItem("whereCondition");
        br.addElement(whereCondition);
        br.addItem("whereColumn");
        br.addElement(whereColumn);
        br.addItem("fromExp");
        br.addElement(fromExp);
        br.addItem("toExp");
        br.addElement(toExp);
        final String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg);
    }

    protected void throwMyOriginDateWhereColumnNotFoundException(String adjustmentExp, String whereCondition, String whereColumn) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the whereColumn on dateAdjustmentMap.");
        br.addItem("Advice");
        br.addElement("Confirm your loadingControlMap.dataprop in the data directory.");
        br.addElement("Is the column name on the df:myOriginDate() misspelling?");
        br.addItem("Data Directory");
        br.addElement(_dataDirectory);
        br.addItem("Table Name");
        br.addElement(_schemaTable);
        br.addItem("columnValueMap");
        br.addElement(_columnValueMap);
        br.addItem("columnMetaMap");
        br.addElement(_columnMetaMap);
        br.addItem("adjustmentExp");
        br.addElement(adjustmentExp);
        br.addItem("whereCondition");
        br.addElement(whereCondition);
        br.addItem("whereColumn");
        br.addElement(whereColumn);
        final String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg);
    }

    protected void throwMyOriginDateWhereFromToNumberFormatException(String adjustmentExp, DfColumnMeta columnMeta, String jdbcType,
            String fromExp, String toExp, NumberFormatException cause) {
        ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Cannot convert from/to value to Integer type on dateAdjustmentMap.");
        br.addItem("Advice");
        br.addElement("Confirm your loadingControlMap.dataprop in the data directory.");
        br.addElement("If the where column is e.g. INTEGER/BIGINT, from/to should be integer number.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    df:myOriginDate(2026/01/20, where MEMBER_ID from SEA to LAND) // *Bad");
        br.addElement("  (o):");
        br.addElement("    df:myOriginDate(2026/01/20, where MEMBER_ID from 1 to 234) // Good");
        br.addItem("Data Directory");
        br.addElement(_dataDirectory);
        br.addItem("Table Name");
        br.addElement(_schemaTable);
        br.addItem("adjustmentExp");
        br.addElement(adjustmentExp);
        br.addItem("columnMeta");
        br.addElement(columnMeta);
        br.addItem("jdbcType");
        br.addElement(jdbcType);
        br.addItem("fromExp");
        br.addElement(fromExp);
        br.addItem("toExp");
        br.addElement(toExp);
        String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg, cause);
    }

    protected void throwMyOriginDateWhereFromToDateFormatException(String adjustmentExp, DfColumnMeta columnMeta, String jdbcType,
            String fromExp, String toExp, ParseDateException cause) {
        ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Cannot convert from/to value to Date type on dateAdjustmentMap.");
        br.addItem("Advice");
        br.addElement("Confirm your loadingControlMap.dataprop in the data directory.");
        br.addElement("If the where column is e.g. date/timestamp, from/to should be date expression.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    df:myOriginDate(2026/01/20, where BIRTHDATE from 1 to 234) // *Bad");
        br.addElement("  (o):");
        br.addElement("    df:myOriginDate(2026/01/20, where BIRTHDATE from 2025/06/14 to 2025/06/30) // Good");
        br.addItem("Data Directory");
        br.addElement(_dataDirectory);
        br.addItem("Table Name");
        br.addElement(_schemaTable);
        br.addItem("adjustmentExp");
        br.addElement(adjustmentExp);
        br.addItem("columnMeta");
        br.addElement(columnMeta);
        br.addItem("jdbcType");
        br.addElement(jdbcType);
        br.addItem("fromExp");
        br.addElement(fromExp);
        br.addItem("toExp");
        br.addElement(toExp);
        String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg, cause);
    }

    protected void throwMyOriginDateIfStatementUnsupportedTypeException(String adjustmentExp, DfColumnMeta columnMeta, String jdbcType) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Unsupported column type as myOriginDate() if statement on dateAdjustmentMap.");
        br.addItem("Advice");
        br.addElement("Confirm your loadingControlMap.dataprop in the data directory.");
        br.addElement("Supported types are e.g. Integer, Long, Date.");
        br.addItem("Data Directory");
        br.addElement(_dataDirectory);
        br.addItem("Table Name");
        br.addElement(_schemaTable);
        br.addItem("adjustmentExp");
        br.addElement(adjustmentExp);
        br.addItem("columnMeta");
        br.addElement(columnMeta);
        br.addItem("jdbcType");
        br.addElement(jdbcType);
        final String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg);
    }

    protected void throwMyOriginDateUnsupportedStatementException(String adjustmentExp) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Unsupported statement of df:myOriginDate() on dateAdjustmentMap.");
        br.addItem("Advice");
        br.addElement("Confirm your loadingControlMap.dataprop in the data directory.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    df:myOriginDate(2026/01/20, detarame MEMBER_ID from ... to ...) // *Bad");
        br.addElement("  (o):");
        br.addElement("    df:myOriginDate(2026/01/20, where MEMBER_ID from ... to ...) // Good");
        br.addItem("Data Directory");
        br.addElement(_dataDirectory);
        br.addItem("Table Name");
        br.addElement(_schemaTable);
        br.addItem("adjustmentExp");
        br.addElement(adjustmentExp);
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg);
    }
}
