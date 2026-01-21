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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dbflute.exception.DfLoadDataRegistrationFailureException;
import org.dbflute.exception.ParseDateExpressionFailureException;
import org.dbflute.helper.HandyDate;
import org.dbflute.helper.StringKeyMap;
import org.dbflute.helper.dfmap.DfMapStyle;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.system.DBFluteSystem;
import org.dbflute.util.Srl;
import org.dbflute.util.Srl.ScopeInfo;

/**
 * @author jflute
 * @since 1.3.2 split from DfLoadingControlProp (2026/01/16 Friday at ichihara)
 */
public class DfDateAdjustmentPreparer {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String KEY_ORIGIN_DATE = "df:originDate";
    public static final String KEY_MILLIS_COLUMN_LIST = "df:millisColumnList";
    public static final String KEY_ALL_MARK = "$$ALL$$";
    public static final String KEY_DISTANCE_YEARS = "df:distanceYears";
    public static final String KEY_DISTANCE_MONTHS = "df:distanceMonths";
    public static final String KEY_DISTANCE_DAYS = "df:distanceDays";

    public static final String MY_ORIGIN_DATE_BEGIN = "df:myOriginDate(";
    public static final String MY_ORIGIN_DATE_END = ")";

    // ===================================================================================
    //                                                                             Prepare
    //                                                                             =======
    public Map<String, Object> prepareDateAdjustmentMap(String dataDirectory, Object datapropPlainValue) {
        // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
        // ; df:originDate = 2013/03/09
        // ; df:millisColumnList = list:{ ... }
        // ; $$ALL$$ = map:{
        //      ; REGISTER_DATETIME  = addDay($distance)
        //  }
        // ; MEMBER = map:{
        //     ; BIRTHDATE = addDay(6)
        // }
        //  ↓↓↓
        // ; df:originDate = 2013/03/09 // as Date object
        // ; df:distanceYears = 12      // pre-calculation 
        // ; df:distanceMonths = 123    // me too
        // ; df:distanceDays = 12345    // me too
        // ; df:millisColumnList = list:{ ... } // no filter
        // ; $$ALL$$ = map:{
        //      ; REGISTER_DATETIME  = addDay($distance)
        //  }
        // ; MEMBER = map:{            // flexible
        //     ; BIRTHDATE = addDay(6) // flexible
        // }
        // _/_/_/_/_/_/_/_/
        final Map<String, Object> flexibleDateAdjMap = StringKeyMap.createAsFlexibleOrdered();
        @SuppressWarnings("unchecked")
        final Map<String, Object> plainDateAdjMap = (Map<String, Object>) datapropPlainValue; // fixedly
        for (Entry<String, Object> plainDateAdjEntry : plainDateAdjMap.entrySet()) {
            final String rootLayerKey = plainDateAdjEntry.getKey(); // e.g. tableName, originDate
            final Object rootLayerValue = plainDateAdjEntry.getValue(); // variety as key
            final Object filteredValue;
            if (rootLayerValue != null) {
                filteredValue = resolveRootLayerValue(dataDirectory, flexibleDateAdjMap, rootLayerKey, rootLayerValue);
            } else {
                filteredValue = null;
            }
            flexibleDateAdjMap.put(rootLayerKey, filteredValue);
        }
        return flexibleDateAdjMap;
    }

    protected Object resolveRootLayerValue(String dataDirectory, Map<String, Object> flexibleDateAdjMap //
            , String rootLayerKey, Object rootLayerValue) { // value is not null here
        final Object filteredValue;
        if (KEY_ORIGIN_DATE.equalsIgnoreCase(rootLayerKey)) { // e.g. df:originDate = 2026/01/16
            filteredValue = handleOriginDate(dataDirectory, rootLayerValue, flexibleDateAdjMap);
        } else if (KEY_MILLIS_COLUMN_LIST.equalsIgnoreCase(rootLayerKey)) { // e.g. df:millisColumnList = list:{ LOGIN_MILLIS }
            filteredValue = handleMillisColumnList(dataDirectory, rootLayerValue);
        } else { // e.g. ; MEMBER = map:{ BIRTHDATE = addDay(6) }
            filteredValue = handleColumnAdjustmentExp(dataDirectory, rootLayerKey, rootLayerValue);
        }
        return filteredValue;
    }

    // ===================================================================================
    //                                                                         Origin Date
    //                                                                         ===========
    protected java.util.Date handleOriginDate(String dataDirectory, Object rootLayerValue, Map<String, Object> flexibleDateAdjMap) {
        final String originExp = rootLayerValue.toString();
        final HandyDate originDate;
        try {
            originDate = new HandyDate(originExp);
        } catch (ParseDateExpressionFailureException e) {
            throwLoadingControlOriginDateParseFailureException(dataDirectory, originExp, e);
            return null; // unreachable
        }
        final java.util.Date filteredValue = originDate.getDate();

        // derive distances in advance (pre-calculation)
        final java.util.Date currentDate = DBFluteSystem.currentDate();
        flexibleDateAdjMap.put(KEY_DISTANCE_YEARS, originDate.calculateCalendarDistanceYears(currentDate));
        flexibleDateAdjMap.put(KEY_DISTANCE_MONTHS, originDate.calculateCalendarDistanceMonths(currentDate));
        flexibleDateAdjMap.put(KEY_DISTANCE_DAYS, originDate.calculateCalendarDistanceDays(currentDate));

        return filteredValue;
    }

    protected void throwLoadingControlOriginDateParseFailureException(String dataDirectory, String originExp,
            ParseDateExpressionFailureException e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to parse the value of the origin date.");
        br.addItem("Advcei");
        br.addElement("Make sure your origin date in the loadingControlMap.dataprop.");
        br.addElement("The date expression should be e.g. 'yyyy/MM/dd HH:mm:ss.SSS'.");
        br.addItem("Data Directory");
        br.addElement(dataDirectory);
        br.addItem("OriginDate Expression");
        br.addElement(originExp);
        final String msg = br.buildExceptionMessage();
        throw new DfLoadDataRegistrationFailureException(msg, e);
    }

    // ===================================================================================
    //                                                                       Millis Column
    //                                                                       =============
    protected Object handleMillisColumnList(String dataDirectory, Object rootLayerValue) {
        return rootLayerValue; // not need filter
    }

    // ===================================================================================
    //                                                               Adjustment Expression
    //                                                               =====================
    protected Object handleColumnAdjustmentExp(String dataDirectory, String rootLayerKey, Object rootLayerValue) {
        // e.g. map:{ BIRTHDATE = addDay(6) }
        @SuppressWarnings("unchecked")
        final Map<String, Object> elementColumnMap = (Map<String, Object>) rootLayerValue;

        // to be flexible as column name key
        final Map<String, Object> flColumnMap = StringKeyMap.createAsFlexibleOrdered();
        flColumnMap.putAll(elementColumnMap);

        // e.g.
        // ; SEA    = addDay($distanceDays) df:myOriginDate(2026/01/10)
        // ; HANGAR = addDay($distanceDays) df:myOriginDate(2026/01/13, where pk from 1 to 10)
        // ; MYSTIC = addDay($distanceDays) df:myOriginDate(2026/01/10, where pk from 1 to 2)
        //                                  df:myOriginDate(2026/01/08, where pk from 4 to 5)
        //                                  df:myOriginDate(2026/01/06)
        //                                                 vvv
        // ; SEA    = addDay($distanceDays) df:myOriginDate(map:{years=0;months=0;days=8})
        // ; HANGAR = addDay($distanceDays) df:myOriginDate(map:{years=0;months=0;days=5}, where pk from 1 to 10)
        // ; MYSTIC = addDay($distanceDays) df:myOriginDate(map:{years=0;months=0;days=8}, where pk from 1 to 2)
        //                                  df:myOriginDate(map:{years=0;months=0;days=10}, where pk from 4 to 5)
        //                                  df:myOriginDate(map:{years=0;months=0;days=12})
        for (Entry<String, Object> entry : flColumnMap.entrySet()) {
            final String columnName = (String) entry.getKey();
            final String adjustmentExp = (String) entry.getValue();
            final String myOriginBegin = MY_ORIGIN_DATE_BEGIN;
            final String myOriginEnd = MY_ORIGIN_DATE_END;

            String filteredExp = adjustmentExp;
            if (adjustmentExp.contains(myOriginBegin)) { // e.g. addDay($distanceDays) df:myOriginDate(2026/01/10)
                final List<ScopeInfo> scopeList = Srl.extractScopeList(adjustmentExp, myOriginBegin, myOriginEnd);
                for (ScopeInfo scopeInfo : scopeList) {
                    String content = scopeInfo.getContent(); // e.g. 2026/01/13 || 2026/01/13, where pk from 1 to 10
                    String myOriginExp = Srl.substringFirstFront(content, ","); // e.g. 2026/01/13
                    final HandyDate originDate;
                    try {
                        originDate = new HandyDate(myOriginExp.trim()); // trim here to keep spaces to be fromStr
                    } catch (ParseDateExpressionFailureException e) {
                        throwLoadingControlMyOriginDateParseFailureException(dataDirectory, rootLayerKey, columnName, adjustmentExp,
                                myOriginExp, e);
                        return null; // unreachable
                    }
                    final String distanceExp = deriveMyOriginDistanceExp(originDate); // e.g. map:{years=0;months=0;days=5}
                    filteredExp = switchOriginDateToDistance(myOriginBegin, filteredExp, myOriginExp, distanceExp);
                }
            }
            flColumnMap.put(columnName, filteredExp); // replace
        }
        return flColumnMap;
    }

    protected String deriveMyOriginDistanceExp(HandyDate originDate) {
        final Map<String, Object> distanceMap = new LinkedHashMap<>();
        final java.util.Date currentDate = DBFluteSystem.currentDate();
        distanceMap.put("years", originDate.calculateCalendarDistanceYears(currentDate));
        distanceMap.put("months", originDate.calculateCalendarDistanceMonths(currentDate));
        distanceMap.put("days", originDate.calculateCalendarDistanceDays(currentDate));
        return new DfMapStyle().toMapString(distanceMap); // e.g. map:{years=0;months=0;days=5}
    }

    protected String switchOriginDateToDistance(String myOriginBegin, String filteredExp, String myOriginExp, String distanceExp) {
        final String fromStr = myOriginBegin + myOriginExp; // e.g. df:myOriginDate(2026/01/13
        final String toStr = myOriginBegin + distanceExp; // e.g. df:myOriginDate(map:{years=0;months=0;days=5}
        return Srl.replace(filteredExp, fromStr, toStr); // e.g. addDay($distanceDays) df:myOriginDate(map:{years=0;months=0;days=5})
    }

    // -----------------------------------------------------
    //                                             Exception
    //                                             ---------
    protected void throwLoadingControlMyOriginDateParseFailureException(String dataDirectory, String rootLayerKey, String columnName,
            String adjustmentExp, String myOriginExp, ParseDateExpressionFailureException e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to parse the value of the origin date.");
        br.addItem("Advcei");
        br.addElement("Make sure your origin date in the loadingControlMap.dataprop.");
        br.addElement("The date expression should be e.g. 'yyyy/MM/dd HH:mm:ss.SSS'.");
        br.addElement("For example:");
        br.addElement("  (x): df:myOriginDate(2026@01+13, where pk from 1 to 10)");
        br.addElement("  (o): df:myOriginDate(2026/01/13, where pk from 1 to 10)");
        br.addItem("Data Directory");
        br.addElement(dataDirectory);
        br.addItem("Table Name");
        br.addElement(rootLayerKey);
        br.addItem("Columm Name");
        br.addElement(columnName);
        br.addItem("DateAdjustment Expression");
        br.addElement(adjustmentExp);
        br.addItem("MyOriginDate Expression");
        br.addElement(myOriginExp);
        final String msg = br.buildExceptionMessage();
        throw new DfLoadDataRegistrationFailureException(msg, e);
    }
}
