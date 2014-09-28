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
package org.seasar.dbflute.cbean.coption;

import java.util.Date;

import org.seasar.dbflute.cbean.ckey.ConditionKey;
import org.seasar.dbflute.unit.core.PlainTestCase;
import org.seasar.dbflute.util.DfTypeUtil;

/**
 * @author jflute
 */
public class FromToOptionTest extends PlainTestCase {

    // ===================================================================================
    //                                                                  Comparison Pattern
    //                                                                  ==================
    // -----------------------------------------------------
    //                                                  Year
    //                                                  ----
    public void test_compareAsYear_basic() {
        // ## Arrange ##
        String fromRes = "2008-09-14 12:34:56";
        String toRes = "2008-11-18 18:34:56";
        FromToOption option = createOption();

        // ## Act ##
        option.compareAsYear();
        Date fromDate = option.filterFromDate(DfTypeUtil.toDate(fromRes));
        Date toDate = option.filterToDate(DfTypeUtil.toDate(toRes));

        // ## Assert ##
        assertEquals(ConditionKey.CK_GREATER_EQUAL, option.getFromDateConditionKey());
        assertEquals(ConditionKey.CK_LESS_THAN, option.getToDateConditionKey());
        assertEquals("2008-01-01 00:00:00", DfTypeUtil.toString(fromDate, "yyyy-MM-dd HH:mm:ss"));
        assertEquals("2009-01-01 00:00:00", DfTypeUtil.toString(toDate, "yyyy-MM-dd HH:mm:ss"));
    }

    public void test_compareAsYear_YearBegin_fromFour() {
        // ## Arrange ##
        String fromRes = "2008-09-14 12:34:56";
        String toRes = "2008-11-18 18:34:56";
        FromToOption option = createOption();

        // ## Act ##
        option.compareAsYear().beginYear_Month(4);
        Date fromDate = option.filterFromDate(DfTypeUtil.toDate(fromRes));
        Date toDate = option.filterToDate(DfTypeUtil.toDate(toRes));

        // ## Assert ##
        assertEquals(ConditionKey.CK_GREATER_EQUAL, option.getFromDateConditionKey());
        assertEquals(ConditionKey.CK_LESS_THAN, option.getToDateConditionKey());
        assertEquals("2008-04-01 00:00:00", DfTypeUtil.toString(fromDate, "yyyy-MM-dd HH:mm:ss"));
        assertEquals("2009-04-01 00:00:00", DfTypeUtil.toString(toDate, "yyyy-MM-dd HH:mm:ss"));
    }

    public void test_compareAsYear_YearBegin_fromOver() {
        // ## Arrange ##
        String fromRes = "2008-02-14 12:34:56";
        String toRes = "2009-02-02 18:34:56";
        FromToOption option = createOption();

        // ## Act ##
        option.compareAsYear().beginYear_Month(4);
        Date fromDate = option.filterFromDate(DfTypeUtil.toDate(fromRes));
        Date toDate = option.filterToDate(DfTypeUtil.toDate(toRes));

        // ## Assert ##
        assertEquals(ConditionKey.CK_GREATER_EQUAL, option.getFromDateConditionKey());
        assertEquals(ConditionKey.CK_LESS_THAN, option.getToDateConditionKey());
        assertEquals("2008-04-01 00:00:00", DfTypeUtil.toString(fromDate, "yyyy-MM-dd HH:mm:ss"));
        assertEquals("2010-04-01 00:00:00", DfTypeUtil.toString(toDate, "yyyy-MM-dd HH:mm:ss"));
    }

    public void test_compareAsYear_YearBegin_fromPrevious() {
        // ## Arrange ##
        String fromRes = "2008-02-14 12:34:56";
        String toRes = "2009-02-02 18:34:56";
        FromToOption option = createOption();

        // ## Act ##
        option.compareAsYear().beginYear_PreviousMonth(11);
        Date fromDate = option.filterFromDate(DfTypeUtil.toDate(fromRes));
        Date toDate = option.filterToDate(DfTypeUtil.toDate(toRes));

        // ## Assert ##
        assertEquals(ConditionKey.CK_GREATER_EQUAL, option.getFromDateConditionKey());
        assertEquals(ConditionKey.CK_LESS_THAN, option.getToDateConditionKey());
        assertEquals("2007-11-01 00:00:00", DfTypeUtil.toString(fromDate, "yyyy-MM-dd HH:mm:ss"));
        assertEquals("2009-11-01 00:00:00", DfTypeUtil.toString(toDate, "yyyy-MM-dd HH:mm:ss"));
    }

    // -----------------------------------------------------
    //                                                 Month
    //                                                 -----
    public void test_compareAsMonth_basic() {
        // ## Arrange ##
        String fromRes = "2008-09-14 12:34:56.789";
        String toRes = "2008-11-18 18:34:56.789";
        FromToOption option = createOption();

        // ## Act ##
        option.compareAsMonth();
        Date fromDate = option.filterFromDate(DfTypeUtil.toDate(fromRes));
        Date toDate = option.filterToDate(DfTypeUtil.toDate(toRes));

        // ## Assert ##
        assertEquals(ConditionKey.CK_GREATER_EQUAL, option.getFromDateConditionKey());
        assertEquals(ConditionKey.CK_LESS_THAN, option.getToDateConditionKey());
        assertEquals("2008-09-01 00:00:00.000", DfTypeUtil.toString(fromDate, "yyyy-MM-dd HH:mm:ss.SSS"));
        assertEquals("2008-12-01 00:00:00.000", DfTypeUtil.toString(toDate, "yyyy-MM-dd HH:mm:ss.SSS"));
    }

    public void test_compareAsMonth_MonthBegin_fromThree() {
        // ## Arrange ##
        String fromRes = "2008-09-14 12:34:56";
        String toRes = "2008-11-18 18:34:56";
        FromToOption option = createOption();

        // ## Act ##
        option.compareAsMonth().beginMonth_Day(3);
        Date fromDate = option.filterFromDate(DfTypeUtil.toDate(fromRes));
        Date toDate = option.filterToDate(DfTypeUtil.toDate(toRes));

        // ## Assert ##
        assertEquals(ConditionKey.CK_GREATER_EQUAL, option.getFromDateConditionKey());
        assertEquals(ConditionKey.CK_LESS_THAN, option.getToDateConditionKey());
        assertEquals("2008-09-03 00:00:00", DfTypeUtil.toString(fromDate, "yyyy-MM-dd HH:mm:ss"));
        assertEquals("2008-12-03 00:00:00", DfTypeUtil.toString(toDate, "yyyy-MM-dd HH:mm:ss"));
    }

    public void test_compareAsMonth_MonthBegin_fromOver() {
        // ## Arrange ##
        String fromRes = "2008-09-14 12:34:56";
        String toRes = "2008-11-18 18:34:56";
        FromToOption option = createOption();

        // ## Act ##
        option.compareAsMonth().beginMonth_Day(15);
        Date fromDate = option.filterFromDate(DfTypeUtil.toDate(fromRes));
        Date toDate = option.filterToDate(DfTypeUtil.toDate(toRes));

        // ## Assert ##
        assertEquals(ConditionKey.CK_GREATER_EQUAL, option.getFromDateConditionKey());
        assertEquals(ConditionKey.CK_LESS_THAN, option.getToDateConditionKey());
        assertEquals("2008-09-15 00:00:00", DfTypeUtil.toString(fromDate, "yyyy-MM-dd HH:mm:ss"));
        assertEquals("2008-12-15 00:00:00", DfTypeUtil.toString(toDate, "yyyy-MM-dd HH:mm:ss"));
    }

    public void test_compareAsMonth_MonthBegin_fromPrevious() {
        // ## Arrange ##
        String fromRes = "2008-09-14 12:34:56";
        String toRes = "2008-11-18 18:34:56";
        FromToOption option = createOption();

        // ## Act ##
        option.compareAsMonth().beginMonth_PreviousDay(25);
        Date fromDate = option.filterFromDate(DfTypeUtil.toDate(fromRes));
        Date toDate = option.filterToDate(DfTypeUtil.toDate(toRes));

        // ## Assert ##
        assertEquals(ConditionKey.CK_GREATER_EQUAL, option.getFromDateConditionKey());
        assertEquals(ConditionKey.CK_LESS_THAN, option.getToDateConditionKey());
        assertEquals("2008-08-25 00:00:00", DfTypeUtil.toString(fromDate, "yyyy-MM-dd HH:mm:ss"));
        assertEquals("2008-11-25 00:00:00", DfTypeUtil.toString(toDate, "yyyy-MM-dd HH:mm:ss"));
    }

    public void test_compareAsMonth_moveToScope_previous() {
        // ## Arrange ##
        FromToOption option = createOption();
        String fmt = "yyyy-MM-dd HH:mm:ss";

        // ## Act ##
        option.compareAsMonth().beginMonth_Day(3).moveToScope(-1);

        // ## Assert ##
        // from date
        assertEquals("2011-10-03 00:00:00", toString(option.filterFromDate(toDate("2011-11-01 00:34:56")), fmt));
        assertEquals("2011-10-03 00:00:00", toString(option.filterFromDate(toDate("2011-11-01 12:34:56")), fmt));
        assertEquals("2011-10-03 00:00:00", toString(option.filterFromDate(toDate("2011-11-01 23:34:56")), fmt));
        assertEquals("2011-10-03 00:00:00", toString(option.filterFromDate(toDate("2011-11-04 23:34:56")), fmt));
        assertEquals("2011-10-03 00:00:00", toString(option.filterFromDate(toDate("2011-11-29 23:34:56")), fmt));

        // to date
        assertEquals("2011-11-03 00:00:00", toString(option.filterToDate(toDate("2011-11-01 00:34:56")), fmt));
        assertEquals("2011-11-03 00:00:00", toString(option.filterToDate(toDate("2011-11-01 12:34:56")), fmt));
        assertEquals("2011-11-03 00:00:00", toString(option.filterToDate(toDate("2011-11-01 23:34:56")), fmt));
        assertEquals("2011-11-03 00:00:00", toString(option.filterToDate(toDate("2011-11-04 23:34:56")), fmt));
        assertEquals("2011-11-03 00:00:00", toString(option.filterToDate(toDate("2011-11-29 23:34:56")), fmt));
    }

    // -----------------------------------------------------
    //                                                  Date
    //                                                  ----
    public void test_compareAsDate_basic() {
        // ## Arrange ##
        String fromRes = "2008-12-14 12:34:56.789";
        String toRes = "2008-12-18 18:34:56.789";
        FromToOption option = createOption();

        // ## Act ##
        option.compareAsDate();
        Date fromDate = option.filterFromDate(DfTypeUtil.toDate(fromRes));
        Date toDate = option.filterToDate(DfTypeUtil.toDate(toRes));

        // ## Assert ##
        assertEquals(ConditionKey.CK_GREATER_EQUAL, option.getFromDateConditionKey());
        assertEquals(ConditionKey.CK_LESS_THAN, option.getToDateConditionKey());
        assertEquals("2008-12-14 00:00:00.000", DfTypeUtil.toString(fromDate, "yyyy-MM-dd HH:mm:ss.SSS"));
        assertEquals("2008-12-19 00:00:00.000", DfTypeUtil.toString(toDate, "yyyy-MM-dd HH:mm:ss.SSS"));
    }

    public void test_compareAsDate_beginDay_fromMorning() {
        // ## Arrange ##
        String fromRes = "2008-12-14 12:34:56.789";
        String toRes = "2008-12-18 18:34:56.789";
        FromToOption option = createOption();

        // ## Act ##
        option.compareAsDate().beginDay_Hour(6);
        Date fromDate = option.filterFromDate(DfTypeUtil.toDate(fromRes));
        Date toDate = option.filterToDate(DfTypeUtil.toDate(toRes));

        // ## Assert ##
        assertEquals(ConditionKey.CK_GREATER_EQUAL, option.getFromDateConditionKey());
        assertEquals(ConditionKey.CK_LESS_THAN, option.getToDateConditionKey());
        assertEquals("2008-12-14 06:00:00.000", DfTypeUtil.toString(fromDate, "yyyy-MM-dd HH:mm:ss.SSS"));
        assertEquals("2008-12-19 06:00:00.000", DfTypeUtil.toString(toDate, "yyyy-MM-dd HH:mm:ss.SSS"));
    }

    public void test_compareAsDate_beginDay_fromOver() {
        // ## Arrange ##
        String fromRes = "2008-12-14 2:34:56.789";
        String toRes = "2008-12-18 18:34:56.789";
        FromToOption option = createOption();

        // ## Act ##
        option.compareAsDate().beginDay_Hour(6);
        Date fromDate = option.filterFromDate(DfTypeUtil.toDate(fromRes));
        Date toDate = option.filterToDate(DfTypeUtil.toDate(toRes));

        // ## Assert ##
        assertEquals(ConditionKey.CK_GREATER_EQUAL, option.getFromDateConditionKey());
        assertEquals(ConditionKey.CK_LESS_THAN, option.getToDateConditionKey());
        assertEquals("2008-12-14 06:00:00.000", DfTypeUtil.toString(fromDate, "yyyy-MM-dd HH:mm:ss.SSS"));
        assertEquals("2008-12-19 06:00:00.000", DfTypeUtil.toString(toDate, "yyyy-MM-dd HH:mm:ss.SSS"));
    }

    public void test_compareAsDate_beginDay_fromPrevious() {
        // ## Arrange ##
        String fromRes = "2008-12-14 12:34:56.789";
        String toRes = "2008-12-18 18:34:56.789";
        FromToOption option = createOption();

        // ## Act ##
        option.compareAsDate().beginDay_PreviousHour(22);
        Date fromDate = option.filterFromDate(DfTypeUtil.toDate(fromRes));
        Date toDate = option.filterToDate(DfTypeUtil.toDate(toRes));

        // ## Assert ##
        assertEquals(ConditionKey.CK_GREATER_EQUAL, option.getFromDateConditionKey());
        assertEquals(ConditionKey.CK_LESS_THAN, option.getToDateConditionKey());
        assertEquals("2008-12-13 22:00:00.000", DfTypeUtil.toString(fromDate, "yyyy-MM-dd HH:mm:ss.SSS"));
        assertEquals("2008-12-18 22:00:00.000", DfTypeUtil.toString(toDate, "yyyy-MM-dd HH:mm:ss.SSS"));
    }

    public void test_compareAsDate_with_greaterThan() {
        // ## Arrange ##
        FromToOption option = createOption();

        // ## Act ##
        option.compareAsDate();
        try {
            option.greaterThan();

            // ## Assert ##
            fail();
        } catch (IllegalStateException e) {
            // OK
            log(e.getMessage());
        }
    }

    public void test_compareAsDate_with_noon() {
        // ## Arrange ##
        String fromRes = "2008-12-14 12:34:56";
        String toRes = "2008-12-18 18:34:56";
        FromToOption option = createOption();

        // ## Act ##
        option.compareAsDate();
        option.fromDateWithNoon();
        option.toDateWithNoon();
        Date fromDate = option.filterFromDate(DfTypeUtil.toDate(fromRes));
        Date toDate = option.filterToDate(DfTypeUtil.toDate(toRes));

        // ## Assert ##
        assertEquals(ConditionKey.CK_GREATER_EQUAL, option.getFromDateConditionKey());
        assertEquals(ConditionKey.CK_LESS_THAN, option.getToDateConditionKey());
        assertEquals("2008-12-14 12:00:00", DfTypeUtil.toString(fromDate, "yyyy-MM-dd HH:mm:ss"));
        assertEquals("2008-12-19 12:00:00", DfTypeUtil.toString(toDate, "yyyy-MM-dd HH:mm:ss"));
    }

    public void test_compareAsDate_moveToScope__previous() {
        // ## Arrange ##
        FromToOption option = createOption();
        String fmt = "yyyy-MM-dd HH:mm:ss";

        // ## Act ##
        option.compareAsDate().beginDay_PreviousHour(22).moveToScope(-2);

        // ## Assert ##
        // from date
        assertEquals("2011-10-29 22:00:00", toString(option.filterFromDate(toDate("2011-11-01 00:34:56")), fmt));
        assertEquals("2011-10-29 22:00:00", toString(option.filterFromDate(toDate("2011-11-01 12:34:56")), fmt));
        assertEquals("2011-10-29 22:00:00", toString(option.filterFromDate(toDate("2011-11-01 23:34:56")), fmt));
        assertEquals("2011-10-30 22:00:00", toString(option.filterFromDate(toDate("2011-11-02 23:34:56")), fmt));

        // to date
        assertEquals("2011-10-30 22:00:00", toString(option.filterToDate(toDate("2011-11-01 00:34:56")), fmt));
        assertEquals("2011-10-30 22:00:00", toString(option.filterToDate(toDate("2011-11-01 12:34:56")), fmt));
        assertEquals("2011-10-30 22:00:00", toString(option.filterToDate(toDate("2011-11-01 23:34:56")), fmt));
        assertEquals("2011-10-31 22:00:00", toString(option.filterToDate(toDate("2011-11-02 23:34:56")), fmt));
    }

    // -----------------------------------------------------
    //                                                  Hour
    //                                                  ----
    public void test_compareAsHour_basic() {
        // ## Arrange ##
        String fromRes = "2008-12-14 12:34:56.789";
        String toRes = "2008-12-18 18:34:56.789";
        FromToOption option = createOption();

        // ## Act ##
        option.compareAsHour();
        Date fromDate = option.filterFromDate(DfTypeUtil.toDate(fromRes));
        Date toDate = option.filterToDate(DfTypeUtil.toDate(toRes));

        // ## Assert ##
        assertEquals(ConditionKey.CK_GREATER_EQUAL, option.getFromDateConditionKey());
        assertEquals(ConditionKey.CK_LESS_THAN, option.getToDateConditionKey());
        assertEquals("2008-12-14 12:00:00.000", DfTypeUtil.toString(fromDate, "yyyy-MM-dd HH:mm:ss.SSS"));
        assertEquals("2008-12-18 19:00:00.000", DfTypeUtil.toString(toDate, "yyyy-MM-dd HH:mm:ss.SSS"));
    }

    public void test_compareAsHour_moveToScope_previous() {
        // ## Arrange ##
        FromToOption option = createOption();
        String fmt = "yyyy-MM-dd HH:mm:ss";

        // ## Act ##
        option.compareAsHour().moveToScope(-2);

        // ## Assert ##
        // from date
        assertEquals("2011-11-08 22:00:00", toString(option.filterFromDate(toDate("2011-11-09 00:34:56")), fmt));
        assertEquals("2011-11-09 10:00:00", toString(option.filterFromDate(toDate("2011-11-09 12:34:56")), fmt));
        assertEquals("2011-11-09 11:00:00", toString(option.filterFromDate(toDate("2011-11-09 13:34:56")), fmt));
        assertEquals("2011-11-09 21:00:00", toString(option.filterFromDate(toDate("2011-11-09 23:34:56")), fmt));

        // to date
        assertEquals("2011-11-08 23:00:00", toString(option.filterToDate(toDate("2011-11-09 00:34:56")), fmt));
        assertEquals("2011-11-09 11:00:00", toString(option.filterToDate(toDate("2011-11-09 12:34:56")), fmt));
        assertEquals("2011-11-09 12:00:00", toString(option.filterToDate(toDate("2011-11-09 13:34:56")), fmt));
        assertEquals("2011-11-09 22:00:00", toString(option.filterToDate(toDate("2011-11-09 23:34:56")), fmt));
    }

    // -----------------------------------------------------
    //                                                  Week
    //                                                  ----
    public void test_compareAsWeek_basic() {
        // ## Arrange ##
        String fromRes = "2011-11-14 12:34:56"; // Monday
        String toRes = "2011-11-23 18:34:56"; // Wednesday
        FromToOption option = createOption();

        // ## Act ##
        option.compareAsWeek();
        Date fromDate = option.filterFromDate(DfTypeUtil.toDate(fromRes));
        Date toDate = option.filterToDate(DfTypeUtil.toDate(toRes));

        // ## Assert ##
        assertEquals(ConditionKey.CK_GREATER_EQUAL, option.getFromDateConditionKey());
        assertEquals(ConditionKey.CK_LESS_THAN, option.getToDateConditionKey());
        assertEquals("2011-11-13 00:00:00", DfTypeUtil.toString(fromDate, "yyyy-MM-dd HH:mm:ss"));
        assertEquals("2011-11-27 00:00:00", DfTypeUtil.toString(toDate, "yyyy-MM-dd HH:mm:ss"));
    }

    public void test_compareAsWeek_beginWeekMonday() {
        // ## Arrange ##
        String fromRes = "2011-11-14 12:34:56"; // Monday
        String toRes = "2011-11-23 18:34:56"; // Wednesday
        FromToOption option = createOption();

        // ## Act ##
        option.compareAsWeek().beginWeek_DayOfWeek2nd_Monday();
        Date fromDate = option.filterFromDate(DfTypeUtil.toDate(fromRes));
        Date toDate = option.filterToDate(DfTypeUtil.toDate(toRes));

        // ## Assert ##
        assertEquals(ConditionKey.CK_GREATER_EQUAL, option.getFromDateConditionKey());
        assertEquals(ConditionKey.CK_LESS_THAN, option.getToDateConditionKey());
        assertEquals("2011-11-14 00:00:00", DfTypeUtil.toString(fromDate, "yyyy-MM-dd HH:mm:ss"));
        assertEquals("2011-11-28 00:00:00", DfTypeUtil.toString(toDate, "yyyy-MM-dd HH:mm:ss"));
    }

    public void test_compareAsWeek_beginWeekFriday() {
        // ## Arrange ##
        String fromRes = "2011-11-14 12:34:56"; // Monday
        String toRes = "2011-11-23 18:34:56"; // Wednesday
        FromToOption option = createOption();

        // ## Act ##
        option.compareAsWeek().beginWeek_DayOfWeek6th_Friday();
        Date fromDate = option.filterFromDate(DfTypeUtil.toDate(fromRes));
        Date toDate = option.filterToDate(DfTypeUtil.toDate(toRes));

        // ## Assert ##
        assertEquals(ConditionKey.CK_GREATER_EQUAL, option.getFromDateConditionKey());
        assertEquals(ConditionKey.CK_LESS_THAN, option.getToDateConditionKey());
        assertEquals("2011-11-11 00:00:00", DfTypeUtil.toString(fromDate, "yyyy-MM-dd HH:mm:ss"));
        assertEquals("2011-11-25 00:00:00", DfTypeUtil.toString(toDate, "yyyy-MM-dd HH:mm:ss"));
    }

    public void test_compareAsWeek_beginWeekSpecified() {
        // ## Arrange ##
        String fromRes = "2011-11-14 12:34:56"; // Monday
        Date targetDate = DfTypeUtil.toDate(fromRes);
        FromToOption option = createOption();

        // ## Act ##
        option.compareAsWeek().beginWeek_DayOfWeek(targetDate);
        Date fromDate = option.filterFromDate(targetDate);
        Date toDate = option.filterToDate(targetDate);

        // ## Assert ##
        assertEquals(ConditionKey.CK_GREATER_EQUAL, option.getFromDateConditionKey());
        assertEquals(ConditionKey.CK_LESS_THAN, option.getToDateConditionKey());
        assertEquals("2011-11-14 00:00:00", DfTypeUtil.toString(fromDate, "yyyy-MM-dd HH:mm:ss"));
        assertEquals("2011-11-21 00:00:00", DfTypeUtil.toString(toDate, "yyyy-MM-dd HH:mm:ss"));
    }

    public void test_compareAsWeek_moveToScope_previous() {
        // ## Arrange ##
        FromToOption option = createOption();
        String fmt = "yyyy-MM-dd HH:mm:ss";

        // ## Act ##
        option.compareAsWeek().beginWeek_DayOfWeek2nd_Monday().moveToScope(-1);

        // ## Assert ##
        // from date (11/09 is Wednesday)
        assertEquals("2011-10-31 00:00:00", toString(option.filterFromDate(toDate("2011-11-09 12:34:56")), fmt));
        assertEquals("2011-10-31 00:00:00", toString(option.filterFromDate(toDate("2011-11-10 12:34:56")), fmt));
        assertEquals("2011-10-31 00:00:00", toString(option.filterFromDate(toDate("2011-11-11 12:34:56")), fmt));
        assertEquals("2011-10-31 00:00:00", toString(option.filterFromDate(toDate("2011-11-12 12:34:56")), fmt));
        assertEquals("2011-10-31 00:00:00", toString(option.filterFromDate(toDate("2011-11-13 12:34:56")), fmt));
        assertEquals("2011-11-07 00:00:00", toString(option.filterFromDate(toDate("2011-11-14 12:34:56")), fmt));
        assertEquals("2011-11-07 00:00:00", toString(option.filterFromDate(toDate("2011-11-15 12:34:56")), fmt));
        assertEquals("2011-11-07 00:00:00", toString(option.filterFromDate(toDate("2011-11-16 12:34:56")), fmt));
        assertEquals("2011-11-07 00:00:00", toString(option.filterFromDate(toDate("2011-11-17 12:34:56")), fmt));
        assertEquals("2011-11-07 00:00:00", toString(option.filterFromDate(toDate("2011-11-18 12:34:56")), fmt));
        assertEquals("2011-11-07 00:00:00", toString(option.filterFromDate(toDate("2011-11-19 12:34:56")), fmt));
        assertEquals("2011-11-07 00:00:00", toString(option.filterFromDate(toDate("2011-11-20 12:34:56")), fmt));
        assertEquals("2011-11-14 00:00:00", toString(option.filterFromDate(toDate("2011-11-21 12:34:56")), fmt));

        // to date
        assertEquals("2011-11-07 00:00:00", toString(option.filterToDate(toDate("2011-11-09 12:34:56")), fmt));
        assertEquals("2011-11-07 00:00:00", toString(option.filterToDate(toDate("2011-11-10 12:34:56")), fmt));
        assertEquals("2011-11-07 00:00:00", toString(option.filterToDate(toDate("2011-11-11 12:34:56")), fmt));
        assertEquals("2011-11-07 00:00:00", toString(option.filterToDate(toDate("2011-11-12 12:34:56")), fmt));
        assertEquals("2011-11-07 00:00:00", toString(option.filterToDate(toDate("2011-11-13 12:34:56")), fmt));
        assertEquals("2011-11-14 00:00:00", toString(option.filterToDate(toDate("2011-11-14 12:34:56")), fmt));
        assertEquals("2011-11-14 00:00:00", toString(option.filterToDate(toDate("2011-11-15 12:34:56")), fmt));
        assertEquals("2011-11-14 00:00:00", toString(option.filterToDate(toDate("2011-11-16 12:34:56")), fmt));
        assertEquals("2011-11-14 00:00:00", toString(option.filterToDate(toDate("2011-11-17 12:34:56")), fmt));
        assertEquals("2011-11-14 00:00:00", toString(option.filterToDate(toDate("2011-11-18 12:34:56")), fmt));
        assertEquals("2011-11-14 00:00:00", toString(option.filterToDate(toDate("2011-11-19 12:34:56")), fmt));
        assertEquals("2011-11-14 00:00:00", toString(option.filterToDate(toDate("2011-11-20 12:34:56")), fmt));
        assertEquals("2011-11-21 00:00:00", toString(option.filterToDate(toDate("2011-11-21 12:34:56")), fmt));
    }

    // -----------------------------------------------------
    //                                       Quarter of Year
    //                                       ---------------
    public void test_compareAsQuarterOfYear_basic() {
        // ## Arrange ##
        String fromRes = "2008-05-14 12:34:56.789";
        String toRes = "2008-11-18 18:34:56.789";
        FromToOption option = createOption();

        // ## Act ##
        option.compareAsQuarterOfYear();
        Date fromDate = option.filterFromDate(DfTypeUtil.toDate(fromRes));
        Date toDate = option.filterToDate(DfTypeUtil.toDate(toRes));

        // ## Assert ##
        assertEquals(ConditionKey.CK_GREATER_EQUAL, option.getFromDateConditionKey());
        assertEquals(ConditionKey.CK_LESS_THAN, option.getToDateConditionKey());
        assertEquals("2008-04-01 00:00:00.000", DfTypeUtil.toString(fromDate, "yyyy-MM-dd HH:mm:ss.SSS"));
        assertEquals("2009-01-01 00:00:00.000", DfTypeUtil.toString(toDate, "yyyy-MM-dd HH:mm:ss.SSS"));
    }

    public void test_compareAsQuarterOfYear_begin_basic() {
        // ## Arrange ##
        String fromRes = "2008-05-14 12:34:56.789";
        String toRes = "2008-11-18 18:34:56.789";
        FromToOption option = createOption();

        // ## Act ##
        option.compareAsQuarterOfYear().beginYear_Month02_February();
        Date fromDate = option.filterFromDate(DfTypeUtil.toDate(fromRes));
        Date toDate = option.filterToDate(DfTypeUtil.toDate(toRes));

        // ## Assert ##
        assertEquals(ConditionKey.CK_GREATER_EQUAL, option.getFromDateConditionKey());
        assertEquals(ConditionKey.CK_LESS_THAN, option.getToDateConditionKey());
        assertEquals("2008-05-01 00:00:00.000", DfTypeUtil.toString(fromDate, "yyyy-MM-dd HH:mm:ss.SSS"));
        assertEquals("2009-02-01 00:00:00.000", DfTypeUtil.toString(toDate, "yyyy-MM-dd HH:mm:ss.SSS"));
    }

    public void test_compareAsQuarterOfYear_begin_over() {
        // ## Arrange ##
        String fromRes = "2008-01-14 12:34:56.789";
        String toRes = "2008-12-18 18:34:56.789";
        FromToOption option = createOption();

        // ## Act ##
        option.compareAsQuarterOfYear().beginYear_Month02_February();
        Date fromDate = option.filterFromDate(DfTypeUtil.toDate(fromRes));
        Date toDate = option.filterToDate(DfTypeUtil.toDate(toRes));

        // ## Assert ##
        assertEquals(ConditionKey.CK_GREATER_EQUAL, option.getFromDateConditionKey());
        assertEquals(ConditionKey.CK_LESS_THAN, option.getToDateConditionKey());
        assertEquals("2007-11-01 00:00:00.000", DfTypeUtil.toString(fromDate, "yyyy-MM-dd HH:mm:ss.SSS"));
        assertEquals("2009-02-01 00:00:00.000", DfTypeUtil.toString(toDate, "yyyy-MM-dd HH:mm:ss.SSS"));
    }

    public void test_compareAsQuarterOfYear_moveToScope() {
        // ## Arrange ##
        String fromRes = "2008-01-14 12:34:56.789";
        String toRes = "2008-12-18 18:34:56.789";
        FromToOption option = createOption();

        // ## Act ##
        option.compareAsQuarterOfYear().beginYear_Month02_February().moveToScope(-1);
        Date fromDate = option.filterFromDate(DfTypeUtil.toDate(fromRes));
        Date toDate = option.filterToDate(DfTypeUtil.toDate(toRes));

        // ## Assert ##
        assertEquals(ConditionKey.CK_GREATER_EQUAL, option.getFromDateConditionKey());
        assertEquals(ConditionKey.CK_LESS_THAN, option.getToDateConditionKey());
        assertEquals("2007-08-01 00:00:00.000", DfTypeUtil.toString(fromDate, "yyyy-MM-dd HH:mm:ss.SSS"));
        assertEquals("2008-11-01 00:00:00.000", DfTypeUtil.toString(toDate, "yyyy-MM-dd HH:mm:ss.SSS"));
    }

    // ===================================================================================
    //                                                                   Manual Adjustment
    //                                                                   =================
    public void test_no_adjustment_basic() {
        // ## Arrange ##
        String expected = "2008-12-14 12:34:56";
        FromToOption option = createOption();

        // ## Act ##
        Date fromDate = option.filterFromDate(DfTypeUtil.toDate(expected));
        Date toDate = option.filterToDate(DfTypeUtil.toDate(expected));

        // ## Assert ##
        assertEquals(ConditionKey.CK_GREATER_EQUAL, option.getFromDateConditionKey());
        assertEquals(ConditionKey.CK_LESS_EQUAL, option.getToDateConditionKey());
        assertEquals(expected, DfTypeUtil.toString(fromDate, "yyyy-MM-dd HH:mm:ss"));
        assertEquals(expected, DfTypeUtil.toString(toDate, "yyyy-MM-dd HH:mm:ss"));
    }

    public void test_greaterThan_basic() {
        // ## Arrange ##
        FromToOption option = createOption();

        // ## Act ##
        option.greaterThan();

        // ## Assert ##
        assertEquals(ConditionKey.CK_GREATER_THAN, option.getFromDateConditionKey());
    }

    public void test_lessThan_basic() {
        // ## Arrange ##
        FromToOption option = createOption();

        // ## Act ##
        option.lessThan();

        // ## Assert ##
        assertEquals(ConditionKey.CK_LESS_THAN, option.getToDateConditionKey());
    }

    public void test_fromDateWithNoon_basic() {
        // ## Arrange ##
        String fromRes = "2008-12-14 18:34:56";
        String toRes = "2008-12-17 09:34:56";
        FromToOption option = createOption();

        // ## Act ##
        option.fromDateWithNoon();
        Date fromDate = option.filterFromDate(DfTypeUtil.toDate(fromRes));
        Date toDate = option.filterToDate(DfTypeUtil.toDate(toRes));

        // ## Assert ##
        assertEquals(ConditionKey.CK_GREATER_EQUAL, option.getFromDateConditionKey());
        assertEquals(ConditionKey.CK_LESS_EQUAL, option.getToDateConditionKey());
        assertEquals("2008-12-14 12:00:00", DfTypeUtil.toString(fromDate, "yyyy-MM-dd HH:mm:ss"));
        assertEquals(toRes, DfTypeUtil.toString(toDate, "yyyy-MM-dd HH:mm:ss"));
    }

    public void test_toDateWithNoon_basic() {
        // ## Arrange ##
        String fromRes = "2008-12-14 18:34:56";
        String toRes = "2008-12-17 09:34:56";
        FromToOption option = createOption();

        // ## Act ##
        option.toDateWithNoon();
        Date fromDate = option.filterFromDate(DfTypeUtil.toDate(fromRes));
        Date toDate = option.filterToDate(DfTypeUtil.toDate(toRes));

        // ## Assert ##
        assertEquals(ConditionKey.CK_GREATER_EQUAL, option.getFromDateConditionKey());
        assertEquals(ConditionKey.CK_LESS_EQUAL, option.getToDateConditionKey());
        assertEquals(fromRes, DfTypeUtil.toString(fromDate, "yyyy-MM-dd HH:mm:ss"));
        assertEquals("2008-12-17 12:00:00", DfTypeUtil.toString(toDate, "yyyy-MM-dd HH:mm:ss"));
    }

    public void test_fromDateWithHour_basic() {
        // ## Arrange ##
        String fromRes = "2008-12-14 18:34:56.789";
        String toRes = "2008-12-17 09:34:56.789";
        FromToOption option = createOption();

        // ## Act ##
        option.fromDateWithHour(16);
        Date fromDate = option.filterFromDate(DfTypeUtil.toDate(fromRes));
        Date toDate = option.filterToDate(DfTypeUtil.toDate(toRes));

        // ## Assert ##
        assertEquals(ConditionKey.CK_GREATER_EQUAL, option.getFromDateConditionKey());
        assertEquals(ConditionKey.CK_LESS_EQUAL, option.getToDateConditionKey());
        assertEquals("2008-12-14 16:00:00.000", DfTypeUtil.toString(fromDate, "yyyy-MM-dd HH:mm:ss.SSS"));
        assertEquals(toRes, DfTypeUtil.toString(toDate, "yyyy-MM-dd HH:mm:ss.SSS"));
    }

    public void test_toDateWithHour_basic() {
        // ## Arrange ##
        String fromRes = "2008-12-14 18:34:56.789";
        String toRes = "2008-12-17 09:34:56.789";
        FromToOption option = createOption();

        // ## Act ##
        option.toDateWithHour(3);
        Date fromDate = option.filterFromDate(DfTypeUtil.toDate(fromRes));
        Date toDate = option.filterToDate(DfTypeUtil.toDate(toRes));

        // ## Assert ##
        assertEquals(ConditionKey.CK_GREATER_EQUAL, option.getFromDateConditionKey());
        assertEquals(ConditionKey.CK_LESS_EQUAL, option.getToDateConditionKey());
        assertEquals("2008-12-17 03:00:00.000", DfTypeUtil.toString(toDate, "yyyy-MM-dd HH:mm:ss.SSS"));
        assertEquals(fromRes, DfTypeUtil.toString(fromDate, "yyyy-MM-dd HH:mm:ss.SSS"));
    }

    // ===================================================================================
    //                                                                    protected Method
    //                                                                    ================
    public void test_clearAll_pattern() {
        // ## Arrange ##
        String fromRes = "2008-12-14 18:34:56.789";
        String toRes = "2008-12-17 09:34:56.789";
        FromToOption option = createOption();

        // ## Act ##
        option.compareAsDate().clearAll();
        Date fromDate = option.filterFromDate(DfTypeUtil.toDate(fromRes));
        Date toDate = option.filterToDate(DfTypeUtil.toDate(toRes));

        // ## Assert ##
        assertEquals(ConditionKey.CK_GREATER_EQUAL, option.getFromDateConditionKey());
        assertEquals(ConditionKey.CK_LESS_EQUAL, option.getToDateConditionKey());
        assertEquals(fromRes, DfTypeUtil.toString(fromDate, "yyyy-MM-dd HH:mm:ss.SSS"));
        assertEquals(toRes, DfTypeUtil.toString(toDate, "yyyy-MM-dd HH:mm:ss.SSS"));
    }

    public void test_clearAll_manual() {
        // ## Arrange ##
        String fromRes = "2008-12-14 18:34:56.789";
        String toRes = "2008-12-17 09:34:56.789";
        FromToOption option = createOption();

        // ## Act ##
        option.toDateWithNoon().fromPatternDayJust().clearAll();
        Date fromDate = option.filterFromDate(DfTypeUtil.toDate(fromRes));
        Date toDate = option.filterToDate(DfTypeUtil.toDate(toRes));

        // ## Assert ##
        assertEquals(ConditionKey.CK_GREATER_EQUAL, option.getFromDateConditionKey());
        assertEquals(ConditionKey.CK_LESS_EQUAL, option.getToDateConditionKey());
        assertEquals(fromRes, DfTypeUtil.toString(fromDate, "yyyy-MM-dd HH:mm:ss.SSS"));
        assertEquals(toRes, DfTypeUtil.toString(toDate, "yyyy-MM-dd HH:mm:ss.SSS"));
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    public void test_toString() {
        // ## Arrange ##
        FromToOption option = createOption();
        option.compareAsDate();

        // ## Act ##
        String actual = option.toString();

        // ## Assert ##
        log(actual);
        assertTrue(actual.contains("lessThan=true"));
    }

    // ===================================================================================
    //                                                                         Test Helper
    //                                                                         ===========
    protected FromToOption createOption() {
        return new FromToOption();
    }
}
