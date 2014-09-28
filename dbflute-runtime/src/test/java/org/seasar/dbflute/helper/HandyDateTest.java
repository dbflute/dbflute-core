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
package org.seasar.dbflute.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.seasar.dbflute.helper.secretary.BusinessDayDeterminer;
import org.seasar.dbflute.unit.core.PlainTestCase;

/**
 * @author jflute
 */
public class HandyDateTest extends PlainTestCase {

    // ===================================================================================
    //                                                                            Add Date
    //                                                                            ========
    public void test_add_basic() throws Exception {
        // ## Arrange ##
        HandyDate date = new HandyDate("2011/11/17 12:34:56.789");

        // ## Act ##
        date.addYear(1).addMonth(1).addDay(1).addHour(1).addMinute(1).addSecond(1).addMillisecond(1);

        // ## Assert ##
        assertEquals("2012/12/18 13:35:57.790", toString(date.getDate(), "yyyy/MM/dd HH:mm:ss.SSS"));
    }

    // ===================================================================================
    //                                                                        Move-to Date
    //                                                                        ============
    public void test_moveTo_basic() throws Exception {
        // ## Arrange ##
        String targetExp = "2011/11/17 12:34:56.789";

        // ## Act & Assert ##
        assertEquals(handy("2007/11/17 12:34:56.789"), handy(targetExp).moveToYear(2007));
        assertEquals(handy("2011/01/01 00:00:00.000"), handy(targetExp).moveToYearJust());
        assertEquals(handy("2011/12/31 23:59:59.999"), handy(targetExp).moveToYearTerminal());
        assertEquals(handy("2011/09/17 12:34:56.789"), handy(targetExp).moveToMonth(9));
        assertEquals(handy("2011/11/01 00:00:00.000"), handy(targetExp).moveToMonthJust());
        assertEquals(handy("2011/11/30 23:59:59.999"), handy(targetExp).moveToMonthTerminal());
        assertEquals(handy("2011/11/28 12:34:56.789"), handy(targetExp).moveToDay(28));
        assertEquals(handy("2011/11/30 12:34:56.789"), handy(targetExp).moveToDay(30));
        assertEquals(handy("2011/12/31 12:34:56.789"), handy(targetExp).addMonth(1).moveToDay(31));
        assertEquals(handy("2012/02/28 12:34:56.789"), handy(targetExp).addMonth(3).moveToDay(28));
        assertEquals(handy("2011/11/17 00:00:00.000"), handy(targetExp).moveToDayJust());
        assertEquals(handy("2011/11/17 23:59:59.999"), handy(targetExp).moveToDayTerminal());
        assertEquals(handy("2011/11/17 00:34:56.789"), handy(targetExp).moveToHour(0));
        assertEquals(handy("2011/11/17 23:34:56.789"), handy(targetExp).moveToHour(23));
        assertEquals(handy("2011/11/17 12:00:00.000"), handy(targetExp).moveToHourJust());
        assertEquals(handy("2011/11/17 12:59:59.999"), handy(targetExp).moveToHourTerminal());
        assertEquals(handy("2011/11/17 12:00:56.789"), handy(targetExp).moveToMinute(0));
        assertEquals(handy("2011/11/17 12:59:56.789"), handy(targetExp).moveToMinute(59));
        assertEquals(handy("2011/11/17 12:34:00.000"), handy(targetExp).moveToMinuteJust());
        assertEquals(handy("2011/11/17 12:34:59.999"), handy(targetExp).moveToMinuteTerminal());
        assertEquals(handy("2011/11/17 12:34:00.789"), handy(targetExp).moveToSecond(0));
        assertEquals(handy("2011/11/17 12:34:59.789"), handy(targetExp).moveToSecond(59));
        assertEquals(handy("2011/11/17 12:34:56.000"), handy(targetExp).moveToSecondJust());
        assertEquals(handy("2011/11/17 12:34:56.999"), handy(targetExp).moveToSecondTerminal());
        assertEquals(handy("2011/11/17 12:34:56.000"), handy(targetExp).moveToMillisecond(0));
        assertEquals(handy("2011/11/17 12:34:56.999"), handy(targetExp).moveToMillisecond(999));

        assertEquals(handy("2013/06/03 00:00:00.000"), handy("2013/06/15 23:59:59.999").moveToMonthFirstWeekdayJust());
        assertEquals(handy("2013/03/29 23:59:59.999"), handy("2013/03/15 12:13:59.123")
                .moveToMonthLastWeekdayTerminal());
        assertEquals(handy("2013/03/02 00:00:00.000"), handy("2013/03/13 23:59:59.999").moveToMonthFirstWeekendJust());
        assertEquals(handy("2013/04/28 23:59:59.999"), handy("2013/04/13 12:13:59.123")
                .moveToMonthLastWeekendTerminal());
    }

    public void test_moveTo_begin() throws Exception {
        // ## Arrange ##
        String small02 = "2011/02/02 02:02:02.222";
        String large11 = "2011/11/17 11:34:56.789";

        // ## Act & Assert ##
        assertEquals(handy("2011/04/01 00:00:00.000"), year4(small02).moveToYearJust());
        assertEquals(handy("2012/03/31 23:59:59.999"), year4(small02).moveToYearTerminal());
        assertEquals(handy("2011/04/01 00:00:00.000"), year4(large11).moveToYearJust());
        assertEquals(handy("2012/03/31 23:59:59.999"), year4(large11).moveToYearTerminal());
        assertEquals(handy("2010/11/01 00:00:00.000"), yearPre11(small02).moveToYearJust());
        assertEquals(handy("2011/10/31 23:59:59.999"), yearPre11(small02).moveToYearTerminal());
        assertEquals(handy("2010/11/01 00:00:00.000"), yearPre11(large11).moveToYearJust());
        assertEquals(handy("2011/10/31 23:59:59.999"), yearPre11(large11).moveToYearTerminal());
        assertEquals(handy("2011/02/03 00:00:00.000"), month3(small02).moveToMonthJust());
        assertEquals(handy("2011/03/02 23:59:59.999"), month3(small02).moveToMonthTerminal());
        assertEquals(handy("2011/11/03 00:00:00.000"), month3(large11).moveToMonthJust());
        assertEquals(handy("2011/12/02 23:59:59.999"), month3(large11).moveToMonthTerminal());
        assertEquals(handy("2011/01/26 00:00:00.000"), monthPre26(small02).moveToMonthJust());
        assertEquals(handy("2011/02/25 23:59:59.999"), monthPre26(small02).moveToMonthTerminal());
        assertEquals(handy("2011/10/26 00:00:00.000"), monthPre26(large11).moveToMonthJust());
        assertEquals(handy("2011/11/25 23:59:59.999"), monthPre26(large11).moveToMonthTerminal());
    }

    public void test_moveTo_minus_allowed() throws Exception {
        // ## Arrange ##
        String targetExp = "2011/11/17 12:34:56.789";

        // ## Act & Assert ##
        assertEquals(handy("BC2007/11/17 12:34:56.789"), handy(targetExp).moveToYear(-2007));
        assertEquals(handy("BC0001/11/17 12:34:56.789"), handy(targetExp).moveToYear(-1));
    }

    public void test_moveTo_illegal() throws Exception {
        // ## Arrange ##
        String targetExp = "2011/11/17 12:34:56.789";

        // ## Act & Assert ##
        // year
        try {
            handy(targetExp).moveToYear(0);

            fail();
        } catch (IllegalArgumentException e) {
            // OK
            log(e.getMessage());
        }

        // month
        try {
            handy(targetExp).moveToMonth(0);

            fail();
        } catch (IllegalArgumentException e) {
            // OK
            log(e.getMessage());
        }
        try {
            handy(targetExp).moveToMonth(-1);

            fail();
        } catch (IllegalArgumentException e) {
            // OK
            log(e.getMessage());
        }

        // day
        try {
            handy(targetExp).moveToDay(0);

            fail();
        } catch (IllegalArgumentException e) {
            // OK
            log(e.getMessage());
        }
        try {
            handy(targetExp).moveToDay(-1);

            fail();
        } catch (IllegalArgumentException e) {
            // OK
            log(e.getMessage());
        }

        // hour
        try {
            handy(targetExp).moveToHour(-1);

            fail();
        } catch (IllegalArgumentException e) {
            // OK
            log(e.getMessage());
        }
    }

    // -----------------------------------------------------
    //                                          Move-to Next
    //                                          ------------
    public void test_moveTo_next_businessDay() throws Exception {
        assertEquals(handy("2013/03/04"), handy("2013/03/02").moveToNextBusinessDay(new BusinessDayDeterminer() {
            public boolean isBusinessDay(HandyDate movedDate) {
                return !movedDate.isWeek_DayOfWeek1st_Sunday() && !movedDate.isWeek_DayOfWeek7th_Saturday();
            }
        }));
        assertEquals(handy("2013/03/05"), handy("2013/03/02").moveToNextBusinessDay(new BusinessDayDeterminer() {
            public boolean isBusinessDay(HandyDate movedDate) {
                return !movedDate.isWeek_DayOfWeekWeekend() && movedDate.getDay() != 4;
            }
        }));
        try {
            handy("2013/03/02").moveToNextBusinessDay(new BusinessDayDeterminer() {
                public boolean isBusinessDay(HandyDate movedDate) {
                    return false;
                }
            });
        } catch (IllegalStateException e) {
            // OK
            log(e.getMessage());
        }
        assertEquals(handy("2013/03/08"), handy("2013/03/02").moveToNextBusinessDay(5, new BusinessDayDeterminer() {
            public boolean isBusinessDay(HandyDate movedDate) {
                return !movedDate.isWeek_DayOfWeek1st_Sunday() && !movedDate.isWeek_DayOfWeek7th_Saturday();
            }
        }));
        assertEquals(handy("2013/03/11"), handy("2013/03/01").moveToNextBusinessDay(6, new BusinessDayDeterminer() {
            public boolean isBusinessDay(HandyDate movedDate) {
                return movedDate.isWeek_DayOfWeekWeekday();
            }
        }));
    }

    // -----------------------------------------------------
    //                                          Move-to Week
    //                                          ------------
    public void test_moveTo_week() throws Exception {
        // ## Arrange ##
        String large11 = "2011/11/17 11:34:56.789";

        // ## Act & Assert ##
        assertEquals(handy("2011/11/03 11:34:56.789"), handy(large11).moveToWeekOfMonth(1));
        assertEquals(handy("2011/11/10 11:34:56.789"), handy(large11).moveToWeekOfMonth(2));
        assertEquals(handy(large11), handy(large11).moveToWeekOfMonth(3));
        assertEquals(handy("2011/11/24 11:34:56.789"), handy(large11).moveToWeekOfMonth(4));
        assertEquals(handy("2011/12/01 11:34:56.789"), handy(large11).moveToWeekOfMonth(5));
        assertEquals(handy("2011/12/08 11:34:56.789"), handy(large11).moveToWeekOfMonth(6));
        assertEquals(handy("2011/10/30 00:00:00.000"), handy(large11).moveToWeekOfMonth(1).moveToWeekJust());
        assertEquals(handy("2011/11/05 23:59:59.999"), handy(large11).moveToWeekOfMonth(1).moveToWeekTerminal());
        {
            HandyDate date = handy(large11).moveToWeekOfMonth(2).beginWeek_DayOfWeek2nd_Monday().moveToWeekJust();
            assertEquals(handy("2011/11/07 00:00:00.000"), date);
        }
        assertEquals(handy("2010/12/30 11:34:56.789"), handy(large11).moveToWeekOfYear(1));
        assertEquals(handy("2011/01/06 11:34:56.789"), handy(large11).moveToWeekOfYear(2));

        assertEquals(2, handy(large11).moveToWeekOfMonth(2).getWeekOfMonth());
        assertEquals(3, handy(large11).moveToWeekOfYear(3).getWeekOfYear());
    }

    // -----------------------------------------------------
    //                                         Related Begin
    //                                         -------------
    public void test_moveTo_related_begin() throws Exception {
        // ## Arrange ##
        String targetExp = "2011/11/17 12:34:56.789";
        HandyDate date = handy(targetExp);
        date.beginYear_Month02_February().beginMonth_Day(3).beginDay_Hour(4);

        // ## Act & Assert ##
        assertEquals(handy("2011/02/03 04:00:00.000"), date.moveToYearJust());
        date.beginDay_PreviousHour(22);
        assertEquals(handy("2011/02/02 22:00:00.000"), date.moveToYearJust());
        date.beginMonth_PreviousDay(25);
        assertEquals(handy("2011/01/24 22:00:00.000"), date.moveToYearJust());
        date.beginYear_PreviousMonth(11);
        assertEquals(handy("2010/10/24 22:00:00.000"), date.moveToYearJust());
    }

    public void test_moveTo_related_quarterOfYear() throws Exception {
        // ## Arrange ##
        String exp = "2011/11/17 12:34:56.789";

        // ## Act & Assert ##
        assertEquals(handy("2011/11/03 04:00:00.000"), handyRelated(exp).moveToQuarterOfYearJust());
        assertEquals(handy("2012/02/03 04:00:00.000"), handyRelated(exp).moveToQuarterOfYearJustAdded(1));
        assertEquals(handy("2011/05/03 04:00:00.000"), handyRelated(exp).moveToQuarterOfYearJustFor(2));
        assertEquals(handy("2012/05/03 03:59:59.999"), handyRelated(exp).moveToQuarterOfYearTerminalAdded(1));
    }

    // ===================================================================================
    //                                                                        Compare Date
    //                                                                        ============
    // -----------------------------------------------------
    //                                            Match Date
    //                                            ----------
    public void test_isMatch() throws Exception {
        assertTrue(handy("2011/11/27").isMatch(toDate("2011/11/27")));
        assertFalse(handy("2011/11/27").isMatch(toDate("2011/11/28")));
        assertTrue(handy("2011/11/27").isMatch(toDate("2011/11/27 00:00:00.000")));
        assertFalse(handy("2011/11/27").isMatch(toDate("2011/11/27 00:00:00.001")));
    }

    // -----------------------------------------------------
    //                                          Greater Date
    //                                          ------------
    public void test_isGreaterThan() throws Exception {
        assertTrue(handy("2011/11/27").isGreaterThan(toDate("2011/11/24")));
        assertFalse(handy("2011/11/27").isGreaterThan(toDate("2011/11/27")));
        assertFalse(handy("2011/11/27").isGreaterThan(toDate("2011/11/28")));
        assertTrue(handy("2011/11/27 12:34:56.789").isGreaterThan(toDate("2011/11/27 12:34:56.788")));
        assertFalse(handy("2011/11/27 12:34:56.789").isGreaterThan(toDate("2011/11/27 12:34:56.789")));
        assertFalse(handy("2011/11/27 12:34:56.789").isGreaterThan(toDate("2011/11/27 12:34:56.790")));
    }

    public void test_isGreaterThanAll() throws Exception {
        assertTrue(handy("2011/11/27").isGreaterThanAll(toDate("2011/11/24"), toDate("2011/11/26")));
        assertFalse(handy("2011/11/27").isGreaterThanAll(toDate("2011/11/24"), toDate("2011/11/27")));
        assertFalse(handy("2011/11/27").isGreaterThanAll(toDate("2011/11/24"), toDate("2011/11/28")));
        assertFalse(handy("2011/11/27").isGreaterThanAll(toDate("2011/11/27"), toDate("2011/11/29")));
        assertFalse(handy("2011/11/27").isGreaterThanAll(toDate("2011/11/28"), toDate("2011/11/29")));
    }

    public void test_isGreaterThanAny() throws Exception {
        assertTrue(handy("2011/11/27").isGreaterThanAny(toDate("2011/11/24"), toDate("2011/11/26")));
        assertTrue(handy("2011/11/27").isGreaterThanAny(toDate("2011/11/24"), toDate("2011/11/27")));
        assertTrue(handy("2011/11/27").isGreaterThanAny(toDate("2011/11/24"), toDate("2011/11/28")));
        assertFalse(handy("2011/11/27").isGreaterThanAny(toDate("2011/11/27"), toDate("2011/11/29")));
        assertFalse(handy("2011/11/27").isGreaterThanAny(toDate("2011/11/28"), toDate("2011/11/29")));
    }

    public void test_isGreaterEqual() throws Exception {
        assertTrue(handy("2011/11/27").isGreaterEqual(toDate("2011/11/24")));
        assertTrue(handy("2011/11/27").isGreaterEqual(toDate("2011/11/27")));
        assertFalse(handy("2011/11/27").isGreaterEqual(toDate("2011/11/28")));
        assertTrue(handy("2011/11/27 12:34:56.789").isGreaterEqual(toDate("2011/11/27 12:34:56.788")));
        assertTrue(handy("2011/11/27 12:34:56.789").isGreaterEqual(toDate("2011/11/27 12:34:56.789")));
        assertFalse(handy("2011/11/27 12:34:56.789").isGreaterEqual(toDate("2011/11/27 12:34:56.790")));
    }

    public void test_isGreaterEqualAll() throws Exception {
        assertTrue(handy("2011/11/27").isGreaterEqualAll(toDate("2011/11/24"), toDate("2011/11/26")));
        assertTrue(handy("2011/11/27").isGreaterEqualAll(toDate("2011/11/24"), toDate("2011/11/27")));
        assertFalse(handy("2011/11/27").isGreaterEqualAll(toDate("2011/11/24"), toDate("2011/11/28")));
        assertFalse(handy("2011/11/27").isGreaterEqualAll(toDate("2011/11/27"), toDate("2011/11/29")));
        assertFalse(handy("2011/11/27").isGreaterEqualAll(toDate("2011/11/28"), toDate("2011/11/29")));
    }

    public void test_isGreaterEqualAny() throws Exception {
        assertTrue(handy("2011/11/27").isGreaterEqualAny(toDate("2011/11/24"), toDate("2011/11/26")));
        assertTrue(handy("2011/11/27").isGreaterEqualAny(toDate("2011/11/24"), toDate("2011/11/27")));
        assertTrue(handy("2011/11/27").isGreaterEqualAny(toDate("2011/11/24"), toDate("2011/11/28")));
        assertTrue(handy("2011/11/27").isGreaterEqualAny(toDate("2011/11/27"), toDate("2011/11/29")));
        assertFalse(handy("2011/11/27").isGreaterEqualAny(toDate("2011/11/28"), toDate("2011/11/29")));
    }

    // -----------------------------------------------------
    //                                             Less Date
    //                                             ---------
    public void test_isLessThan() throws Exception {
        assertFalse(handy("2011/11/27").isLessThan(toDate("2011/11/24")));
        assertFalse(handy("2011/11/27").isLessThan(toDate("2011/11/27")));
        assertTrue(handy("2011/11/27").isLessThan(toDate("2011/11/28")));
        assertFalse(handy("2011/11/27 12:34:56.789").isLessThan(toDate("2011/11/27 12:34:56.788")));
        assertFalse(handy("2011/11/27 12:34:56.789").isLessThan(toDate("2011/11/27 12:34:56.789")));
        assertTrue(handy("2011/11/27 12:34:56.789").isLessThan(toDate("2011/11/27 12:34:56.790")));
    }

    public void test_isLessThanAll() throws Exception {
        assertFalse(handy("2011/11/27").isLessThanAll(toDate("2011/11/24"), toDate("2011/11/26")));
        assertFalse(handy("2011/11/27").isLessThanAll(toDate("2011/11/24"), toDate("2011/11/27")));
        assertFalse(handy("2011/11/27").isLessThanAll(toDate("2011/11/24"), toDate("2011/11/28")));
        assertFalse(handy("2011/11/27").isLessThanAll(toDate("2011/11/27"), toDate("2011/11/29")));
        assertTrue(handy("2011/11/27").isLessThanAll(toDate("2011/11/28"), toDate("2011/11/29")));
    }

    public void test_isLessThanAny() throws Exception {
        assertFalse(handy("2011/11/27").isLessThanAny(toDate("2011/11/24"), toDate("2011/11/26")));
        assertFalse(handy("2011/11/27").isLessThanAny(toDate("2011/11/24"), toDate("2011/11/27")));
        assertTrue(handy("2011/11/27").isLessThanAny(toDate("2011/11/24"), toDate("2011/11/28")));
        assertTrue(handy("2011/11/27").isLessThanAny(toDate("2011/11/27"), toDate("2011/11/29")));
        assertTrue(handy("2011/11/27").isLessThanAny(toDate("2011/11/28"), toDate("2011/11/29")));
    }

    public void test_isLessEqual() throws Exception {
        assertFalse(handy("2011/11/27").isLessEqual(toDate("2011/11/24")));
        assertTrue(handy("2011/11/27").isLessEqual(toDate("2011/11/27")));
        assertTrue(handy("2011/11/27").isLessEqual(toDate("2011/11/28")));
        assertFalse(handy("2011/11/27 12:34:56.789").isLessEqual(toDate("2011/11/27 12:34:56.788")));
        assertTrue(handy("2011/11/27 12:34:56.789").isLessEqual(toDate("2011/11/27 12:34:56.789")));
        assertTrue(handy("2011/11/27 12:34:56.789").isLessEqual(toDate("2011/11/27 12:34:56.790")));
    }

    public void test_isLessEqualAll() throws Exception {
        assertFalse(handy("2011/11/27").isLessEqualAll(toDate("2011/11/24"), toDate("2011/11/26")));
        assertFalse(handy("2011/11/27").isLessEqualAll(toDate("2011/11/24"), toDate("2011/11/27")));
        assertFalse(handy("2011/11/27").isLessEqualAll(toDate("2011/11/24"), toDate("2011/11/28")));
        assertTrue(handy("2011/11/27").isLessEqualAll(toDate("2011/11/27"), toDate("2011/11/29")));
        assertTrue(handy("2011/11/27").isLessEqualAll(toDate("2011/11/28"), toDate("2011/11/29")));
    }

    public void test_isLessEqualAny() throws Exception {
        assertFalse(handy("2011/11/27").isLessEqualAny(toDate("2011/11/24"), toDate("2011/11/26")));
        assertTrue(handy("2011/11/27").isLessEqualAny(toDate("2011/11/24"), toDate("2011/11/27")));
        assertTrue(handy("2011/11/27").isLessEqualAny(toDate("2011/11/24"), toDate("2011/11/28")));
        assertTrue(handy("2011/11/27").isLessEqualAny(toDate("2011/11/27"), toDate("2011/11/29")));
        assertTrue(handy("2011/11/27").isLessEqualAny(toDate("2011/11/28"), toDate("2011/11/29")));
    }

    // ===================================================================================
    //                                                                       Confirm Parts
    //                                                                       =============
    // -----------------------------------------------------
    //                                          Confirm Year
    //                                          ------------
    public void test_isYear_basic() throws Exception {
        assertTrue(handy("2011/01/01 00:00:00").isYear(2011));
        assertFalse(handy("2011/01/01 00:00:00").isYear(2012));
        assertTrue(handy("BC2011/01/01 00:00:00").isYear(-2011));
        assertTrue(handy("AD2011/01/01 00:00:00").isYear_AnnoDomini());
        assertTrue(handy("BC2011/01/01 00:00:00").isYear_BeforeChrist());
        assertTrue(handy("2011/01/01 00:00:00").isYearSameAs(toDate("2011/03/03 12:34:45")));
        assertFalse(handy("2011/01/01 00:00:00").isYearSameAs(toDate("2012/03/03 12:34:45")));
    }

    // -----------------------------------------------------
    //                                         Confirm Month
    //                                         -------------
    public void test_isMonth_basic() throws Exception {
        assertTrue(handy("2011/01/01 00:00:00").isMonth(1));
        assertTrue(handy("2011/02/01 00:00:00").isMonth(2));
        assertTrue(handy("2011/03/01 00:00:00").isMonth(3));
        assertFalse(handy("2011/03/01 00:00:00").isMonth(1));
        assertTrue(handy("2011/01/01 00:00:00").isMonthSameAs(toDate("2011/01/03 12:34:45")));
        assertTrue(handy("2011/01/01 00:00:00").isMonthSameAs(toDate("2013/01/03 12:34:45")));
        assertFalse(handy("2011/01/01 00:00:00").isMonthSameAs(toDate("2012/03/03 12:34:45")));
    }

    // -----------------------------------------------------
    //                                           Confirm Day
    //                                           -----------
    public void test_isDay_basic() throws Exception {
        assertTrue(handy("2011/01/01 00:00:00").isDay(1));
        assertTrue(handy("2011/02/20 00:00:00").isDay(20));
        assertTrue(handy("2011/03/30 00:00:00").isDay(30));
        assertTrue(handy("2011/03/01 00:00:00").isDay_MonthFirstDay());
        assertFalse(handy("2011/03/14 00:00:00").isDay_MonthFirstDay());
        assertFalse(handy("2011/03/30 00:00:00").isDay_MonthFirstDay());
        assertFalse(handy("2011/03/31 00:00:00").isDay_MonthFirstDay());
        assertFalse(handy("2011/03/01 00:00:00").isDay_MonthLastDay());
        assertFalse(handy("2011/03/14 00:00:00").isDay_MonthLastDay());
        assertFalse(handy("2011/03/30 00:00:00").isDay_MonthLastDay());
        assertTrue(handy("2011/03/31 00:00:00").isDay_MonthLastDay());
        assertTrue(handy("2011/01/01 00:00:00").isDaySameAs(toDate("2011/01/01 12:34:45")));
        assertTrue(handy("2011/01/01 00:00:00").isDaySameAs(toDate("2012/04/01 12:34:45")));
        assertFalse(handy("2011/01/01 00:00:00").isDaySameAs(toDate("2011/01/03 12:34:45")));
    }

    // -----------------------------------------------------
    //                                          Confirm Week
    //                                          ------------
    public void test_isWeek_basic() throws Exception {
        assertTrue(handy("2013/03/01 00:00:00").isWeek_DayOfWeek6th_Friday());
        assertTrue(handy("2013/03/02 00:00:00").isWeek_DayOfWeek7th_Saturday());
        assertTrue(handy("2013/03/03 00:00:00").isWeek_DayOfWeek1st_Sunday());
        assertTrue(handy("2013/03/04 00:00:00").isWeek_DayOfWeek2nd_Monday());
        assertTrue(handy("2013/03/05 00:00:00").isWeek_DayOfWeek3rd_Tuesday());
        assertTrue(handy("2013/03/06 00:00:00").isWeek_DayOfWeek4th_Wednesday());
        assertTrue(handy("2013/03/07 00:00:00").isWeek_DayOfWeek5th_Thursday());
        assertTrue(handy("2013/03/08 00:00:00").isWeek_DayOfWeek6th_Friday());
        assertTrue(handy("2013/03/09 00:00:00").isWeek_DayOfWeek7th_Saturday());
        assertTrue(handy("2013/03/09 00:00:00").isWeek_DayOfWeekWeekend());
        assertTrue(handy("2013/03/02 00:00:00").isWeek_DayOfWeekWeekend());
        assertFalse(handy("2013/03/01 00:00:00").isWeek_DayOfWeekWeekend());
        assertTrue(handy("2013/03/04 00:00:00").isWeek_DayOfWeekWeekday());
        assertTrue(handy("2013/03/08 00:00:00").isWeek_DayOfWeekWeekday());
        assertFalse(handy("2013/03/02 00:00:00").isWeek_DayOfWeekWeekday());
        assertFalse(handy("2013/03/03 00:00:00").isWeek_DayOfWeekWeekday());
    }

    // ===================================================================================
    //                                                                     Calculate Parts
    //                                                                     ===============
    // -----------------------------------------------------
    //                                     Calendar Distance
    //                                     -----------------
    public void test_calculateCalendarDistanceYears() throws Exception {
        assertEquals(0, handy("2013/03/03").calculateCalendarDistanceYears(toDate("2013/03/03")));
        assertEquals(0, handy("2013/03/07").calculateCalendarDistanceYears(toDate("2013/03/03")));
        assertEquals(0, handy("2013/03/07").calculateCalendarDistanceYears(toDate("2013/04/03")));
        assertEquals(1, handy("2013/12/31").calculateCalendarDistanceYears(toDate("2014/01/01")));
        assertEquals(-1, handy("2013/03/07").calculateCalendarDistanceYears(toDate("2012/01/03")));
        assertEquals(1, handy("2013/03/07").calculateCalendarDistanceYears(toDate("2014/01/03")));
        assertEquals(7, handy("2013/03/07").calculateCalendarDistanceYears(toDate("2020/01/03")));
        assertEquals(1000, handy("2013/03/07").calculateCalendarDistanceYears(toDate("3013/03/07")));
    }

    public void test_calculateCalendarDistanceMonths() throws Exception {
        assertEquals(0, handy("2013/03/03").calculateCalendarDistanceMonths(toDate("2013/03/03")));
        assertEquals(0, handy("2013/03/07").calculateCalendarDistanceMonths(toDate("2013/03/03")));
        assertEquals(1, handy("2013/03/07").calculateCalendarDistanceMonths(toDate("2013/04/03")));
        assertEquals(-2, handy("2013/03/07").calculateCalendarDistanceMonths(toDate("2013/01/03")));
        assertEquals(10, handy("2013/03/07").calculateCalendarDistanceMonths(toDate("2014/01/03")));
        assertEquals(1, handy("2013/03/31").calculateCalendarDistanceMonths(toDate("2013/04/01")));
        assertEquals(12000, handy("2013/01/01").calculateCalendarDistanceMonths(toDate("3013/01/01")));
    }

    public void test_calculateCalendarDistanceDays() throws Exception {
        assertEquals(0, handy("2013/03/03").calculateCalendarDistanceDays(toDate("2013/03/03")));
        assertEquals(0, handy("2013/03/03").calculateCalendarDistanceDays(toDate("2013/03/03 12:34:56")));
        assertEquals(4, handy("2013/03/03").calculateCalendarDistanceDays(toDate("2013/03/07")));
        assertEquals(35, handy("2013/03/03").calculateCalendarDistanceDays(toDate("2013/04/07")));
        assertEquals(-4, handy("2013/03/07").calculateCalendarDistanceDays(toDate("2013/03/03")));
        assertEquals(-35, handy("2013/04/07").calculateCalendarDistanceDays(toDate("2013/03/03")));
        assertEquals(365, handy("2013/03/03").calculateCalendarDistanceDays(toDate("2014/03/03")));
        assertEquals(400, handy("2013/03/03").calculateCalendarDistanceDays(toDate("2014/04/07")));
        assertEquals(4, handy("2013/03/03 23:59:59").calculateCalendarDistanceDays(toDate("2013/03/07")));
    }

    public void test_calculateCalendarDistanceHours() throws Exception {
        assertEquals(0, handy("2013/03/03").calculateCalendarDistanceHours(toDate("2013/03/03")));
        assertEquals(12, handy("2013/03/03").calculateCalendarDistanceHours(toDate("2013/03/03 12:34:56")));
        assertEquals(96, handy("2013/03/03").calculateCalendarDistanceHours(toDate("2013/03/07")));
        assertEquals(-5, handy("2013/03/03 12:00:00").calculateCalendarDistanceHours(toDate("2013/03/03 07:34:56")));
        assertEquals(-6, handy("2013/03/03 12:34:56").calculateCalendarDistanceHours(toDate("2013/03/03 06:00:00")));
        assertEquals(-30, handy("2013/03/03 12:34:56").calculateCalendarDistanceHours(toDate("2013/03/02 06:59:59")));
        assertEquals(31, handy("2013/03/03 07:00:00").calculateCalendarDistanceHours(toDate("2013/03/04 14:34:56")));
        assertEquals(23, handy("2013/03/03 01:59:59").calculateCalendarDistanceHours(toDate("2013/03/04")));
        assertEquals(2, handy("2013/03/03 07:59:59").calculateCalendarDistanceHours(toDate("2013/03/03 09:00:00")));
    }

    public void test_calculateCalendarDistanceMinutes() throws Exception {
        assertEquals(0, handy("2013/03/03").calculateCalendarDistanceMinutes(toDate("2013/03/03")));
        assertEquals(3, handy("2013/03/03 12:34:56").calculateCalendarDistanceMinutes(toDate("2013/03/03 12:37:56")));
        assertEquals(63, handy("2013/03/03 12:34:56").calculateCalendarDistanceMinutes(toDate("2013/03/03 13:37:56")));
        assertEquals(-26, handy("2013/03/03 12:00:00").calculateCalendarDistanceMinutes(toDate("2013/03/03 11:34:56")));
        assertEquals(3, handy("2013/03/03 07:13:59").calculateCalendarDistanceMinutes(toDate("2013/03/03 07:16:00")));
    }

    public void test_calculateCalendarDistanceSeconds() throws Exception {
        assertEquals(0, handy("2013/03/03").calculateCalendarDistanceSeconds(toDate("2013/03/03")));
        assertEquals(3, handy("2013/03/03 12:34:56").calculateCalendarDistanceSeconds(toDate("2013/03/03 12:34:59")));
        assertEquals(65, handy("2013/03/03 12:58:56").calculateCalendarDistanceSeconds(toDate("2013/03/03 13:00:01")));
        assertEquals(-6, handy("2013/03/03 12:34:56").calculateCalendarDistanceSeconds(toDate("2013/03/03 12:34:50")));
        assertEquals(2,
                handy("2013/03/03 07:13:25.999").calculateCalendarDistanceSeconds(toDate("2013/03/03 07:13:27.000")));
    }

    public void test_calculateCalendarDistanceMilliseconds() throws Exception {
        assertEquals(
                1000,
                handy("2013/03/03 12:34:56.000").calculateCalendarDistanceMilliseconds(
                        toDate("2013/03/03 12:34:57.000")));
        assertEquals(
                877,
                handy("2013/03/03 12:34:56.123").calculateCalendarDistanceMilliseconds(
                        toDate("2013/03/03 12:34:57.000")));
        assertEquals(
                3040,
                handy("2013/03/03 07:34:12.123").calculateCalendarDistanceMilliseconds(
                        toDate("2013/03/03 07:34:15.163")));
    }

    // -----------------------------------------------------
    //                                     Measured Distance
    //                                     -----------------
    public void test_calculateMeasuredDistanceYears() throws Exception {
        assertEquals(0, handy("2013/03/03").calculateMeasuredDistanceYears(toDate("2013/03/03")));
        assertEquals(0, handy("2013/12/31").calculateMeasuredDistanceYears(toDate("2014/01/01")));
        assertEquals(0, handy("2013/12/31").calculateMeasuredDistanceYears(toDate("2014/06/15")));
        assertEquals(0, handy("2013/12/31").calculateMeasuredDistanceYears(toDate("2014/06/30")));
        assertEquals(0, handy("2013/12/31").calculateMeasuredDistanceYears(toDate("2014/07/01")));
        assertEquals(0, handy("2013/12/31").calculateMeasuredDistanceYears(toDate("2014/07/15")));
        assertEquals(1, handy("2013/12/31").calculateMeasuredDistanceYears(toDate("2014/07/16")));
        assertEquals(1, handy("2013/12/31").calculateMeasuredDistanceYears(toDate("2014/08/31")));
        assertEquals(1, handy("2013/12/31").calculateMeasuredDistanceYears(toDate("2015/06/30")));
        assertEquals(1, handy("2013/12/31").calculateMeasuredDistanceYears(toDate("2015/07/01")));
        assertEquals(1, handy("2013/12/31").calculateMeasuredDistanceYears(toDate("2015/07/09")));
        assertEquals(2, handy("2013/12/31").calculateMeasuredDistanceYears(toDate("2015/07/16")));
        assertEquals(0, handy("2014/01/01").calculateMeasuredDistanceYears(toDate("2014/06/15")));
        assertEquals(0, handy("2014/01/01").calculateMeasuredDistanceYears(toDate("2014/06/30")));
        assertEquals(0, handy("2014/01/01").calculateMeasuredDistanceYears(toDate("2014/07/01")));
        assertEquals(1, handy("2014/01/01").calculateMeasuredDistanceYears(toDate("2014/07/17")));
        assertEquals(1, handy("2014/01/01").calculateMeasuredDistanceYears(toDate("2014/08/31")));
        assertEquals(1, handy("2014/01/01").calculateMeasuredDistanceYears(toDate("2015/06/30")));
        assertEquals(1, handy("2014/01/01").calculateMeasuredDistanceYears(toDate("2015/07/01")));
        assertEquals(2, handy("2013/01/01").calculateMeasuredDistanceYears(toDate("2014/12/31")));
        assertEquals(1, handy("2013/01/01").calculateMeasuredDistanceYears(toDate("2014/06/06")));
        assertEquals(1, handy("2013/01/01").calculateMeasuredDistanceYears(toDate("2014/07/01")));
        assertEquals(1, handy("2013/01/01").calculateMeasuredDistanceYears(toDate("2014/07/09")));
        assertEquals(2, handy("2013/01/01").calculateMeasuredDistanceYears(toDate("2014/07/17")));
        assertEquals(3, handy("2013/01/01").calculateMeasuredDistanceYears(toDate("2015/10/01")));
        assertEquals(1000, handy("2013/03/07").calculateMeasuredDistanceYears(toDate("3013/03/07")));
        assertEquals(5000, handy("2013/03/07").calculateMeasuredDistanceYears(toDate("7013/03/07")));
        assertEquals(5087, handy("2013/03/07").calculateMeasuredDistanceYears(toDate("7099/10/07")));
    }

    public void test_calculateMeasuredDistanceMonths() throws Exception {
        assertEquals(0, handy("2013/03/03").calculateMeasuredDistanceMonths(toDate("2013/03/03")));
        assertEquals(0, handy("2013/03/07").calculateMeasuredDistanceMonths(toDate("2013/03/03")));
        assertEquals(0, handy("2013/03/20").calculateMeasuredDistanceMonths(toDate("2013/04/03")));
        assertEquals(1, handy("2013/03/07").calculateMeasuredDistanceMonths(toDate("2013/04/03")));
        assertEquals(-1, handy("2013/03/01").calculateMeasuredDistanceMonths(toDate("2013/01/28")));
        assertEquals(5, handy("2013/03/01").calculateMeasuredDistanceMonths(toDate("2013/08/01")));
        assertEquals(6, handy("2013/03/01").calculateMeasuredDistanceMonths(toDate("2013/08/31")));
        assertEquals(30, handy("2013/03/01").calculateMeasuredDistanceMonths(toDate("2015/08/31")));
        assertEquals(246, handy("2013/03/01").calculateMeasuredDistanceMonths(toDate("2033/08/31")));
        assertEquals(12000, handy("2013/01/01").calculateMeasuredDistanceMonths(toDate("3013/01/01")));
        assertEquals(24000, handy("2013/01/01").calculateMeasuredDistanceMonths(toDate("4013/01/01")));
        assertEquals(36000, handy("2013/01/01").calculateMeasuredDistanceMonths(toDate("5013/01/01")));
        assertEquals(60000, handy("2013/01/01").calculateMeasuredDistanceMonths(toDate("7013/01/01")));
        assertEquals(12001, handy("2013/01/01").calculateMeasuredDistanceMonths(toDate("3013/01/31")));
    }

    public void test_calculateMeasuredDistanceDays() throws Exception {
        assertEquals(0, handy("2013/04/01 23:59:59").calculateMeasuredDistanceDays(toDate("2013/04/02 00:00:00")));
        assertEquals(2, handy("2013/04/01 10:00:00").calculateMeasuredDistanceDays(toDate("2013/04/02 23:59:59")));
        assertEquals(1, handy("2013/04/01 10:00:00").calculateMeasuredDistanceDays(toDate("2013/04/01 23:59:59")));
    }

    public void test_calculateMeasuredDistanceHours() throws Exception {
        assertEquals(0, handy("2013/12/31 12:34:56").calculateMeasuredDistanceHours(toDate("2013/12/31 13:00:00")));
        assertEquals(2, handy("2013/12/31 12:34:56").calculateMeasuredDistanceHours(toDate("2013/12/31 14:10:00")));
    }

    public void test_calculateMeasuredDistanceMinutes() throws Exception {
        assertEquals(0, handy("2013/12/31 12:34:56").calculateMeasuredDistanceMinutes(toDate("2013/12/31 12:35:00")));
        assertEquals(2, handy("2013/12/31 12:34:56").calculateMeasuredDistanceMinutes(toDate("2013/12/31 12:37:00")));
    }

    public void test_calculateDistanceSecondsRounded() throws Exception {
        assertEquals(0,
                handy("2013/12/31 12:34:56.789").calculateMeasuredDistanceSeconds(toDate("2013/12/31 12:34:57.000")));
        assertEquals(2,
                handy("2013/12/31 12:34:56.789").calculateMeasuredDistanceSeconds(toDate("2013/12/31 12:34:58.333")));
    }

    // -----------------------------------------------------
    //                                          Size of Days
    //                                          ------------
    public void test_calculateSizeBusinessDays() throws Exception {
        assertEquals(5,
                handy("2013/03/14").calculateSizeBusinessDays(toDate("2013/03/21"), new BusinessDayDeterminer() {
                    public boolean isBusinessDay(HandyDate handyDate) {
                        return handyDate.isWeek_DayOfWeekWeekday() && !handyDate.isDaySameAs(toDate("2013/03/20"));
                    }
                }));
        assertEquals(4,
                handy("2013/03/07").calculateSizeBusinessDays(toDate("2013/03/03"), new BusinessDayDeterminer() {
                    public boolean isBusinessDay(HandyDate handyDate) {
                        return handyDate.isWeek_DayOfWeekWeekday();
                    }
                }));
        assertEquals(7,
                handy("2013/03/07").calculateSizeBusinessDays(toDate("2013/03/16"), new BusinessDayDeterminer() {
                    public boolean isBusinessDay(HandyDate handyDate) {
                        return handyDate.isWeek_DayOfWeekWeekday();
                    }
                }));
    }

    public void test_calculateSizeWeekdays() throws Exception {
        assertEquals(0, handy("2013/03/03").calculateSizeWeekdays(toDate("2013/03/03")));
        assertEquals(0, handy("2013/03/03").calculateSizeWeekdays(toDate("2013/03/03 12:34:56")));
        assertEquals(4, handy("2013/03/03").calculateSizeWeekdays(toDate("2013/03/07")));
        assertEquals(4, handy("2013/03/07").calculateSizeWeekdays(toDate("2013/03/03")));
        assertEquals(5, handy("2013/03/07").calculateSizeWeekdays(toDate("2013/03/13")));
        assertEquals(5, handy("2013/03/13").calculateSizeWeekdays(toDate("2013/03/07")));
        assertEquals(7, handy("2013/03/07").calculateSizeWeekdays(toDate("2013/03/16")));
        assertEquals(7, handy("2013/03/16").calculateSizeWeekdays(toDate("2013/03/07")));
        assertEquals(0, handy("2013/03/16").calculateSizeWeekdays(toDate("2013/03/17")));
        assertEquals(1, handy("2013/03/16").calculateSizeWeekdays(toDate("2013/03/18")));
    }

    public void test_calculateSizeWeekendDays() throws Exception {
        assertEquals(0, handy("2013/03/03").calculateSizeWeekendDays(toDate("2013/03/03")));
        assertEquals(0, handy("2013/03/03").calculateSizeWeekendDays(toDate("2013/03/03 12:34:56")));
        assertEquals(1, handy("2013/03/03").calculateSizeWeekendDays(toDate("2013/03/07")));
        assertEquals(1, handy("2013/03/07").calculateSizeWeekendDays(toDate("2013/03/03")));
        assertEquals(2, handy("2013/03/07").calculateSizeWeekendDays(toDate("2013/03/13")));
        assertEquals(2, handy("2013/03/13").calculateSizeWeekendDays(toDate("2013/03/07")));
        assertEquals(6, handy("2013/03/07").calculateSizeWeekendDays(toDate("2013/03/28")));
    }

    // ===================================================================================
    //                                                                        Choose Parts
    //                                                                        ============
    public void test_chooseNearestFDate() throws Exception {
        HandyDate date = handy("2013/03/03");
        assertEquals(toDate("2013/03/03"), date.chooseNearestDate(toDate("2013/03/03"), toDate("2013/03/04")));
        assertEquals(toDate("2013/03/04"), date.chooseNearestDate(toDate("2013/03/02"), toDate("2013/03/04")));
        assertEquals(toDate("2013/03/02"), date.chooseNearestDate(toDate("2013/03/02"), toDate("2013/03/06")));
        assertEquals(toDate("2013/03/05"), date.chooseNearestDate(toDate("2013/03/06"), toDate("2013/03/05")));
        assertEquals(toDate("2013/03/04 12:34:55"),
                date.chooseNearestDate(toDate("2013/03/04 12:34:56"), toDate("2013/03/04 12:34:55")));
        assertEquals(toDate("2013/03/05"),
                date.chooseNearestDate(toDate("2013/03/05"), toDate("2013/03/12"), toDate("2013/03/01")));
    }

    public void test_chooseNearestFutureDate() throws Exception {
        HandyDate date = handy("2013/03/03");
        assertEquals(toDate("2013/03/03"), date.chooseNearestFutureDate(toDate("2013/03/03"), toDate("2013/03/04")));
        assertEquals(toDate("2013/03/04"), date.chooseNearestFutureDate(toDate("2013/03/02"), toDate("2013/03/04")));
        assertEquals(toDate("2013/03/06"), date.chooseNearestFutureDate(toDate("2013/03/02"), toDate("2013/03/06")));
        assertEquals(toDate("2013/03/05"), date.chooseNearestFutureDate(toDate("2013/03/06"), toDate("2013/03/05")));
        assertEquals(toDate("2013/03/04 12:34:55"),
                date.chooseNearestFutureDate(toDate("2013/03/04 12:34:56"), toDate("2013/03/04 12:34:55")));
        assertEquals(toDate("2013/03/05"),
                date.chooseNearestFutureDate(toDate("2013/03/05"), toDate("2013/03/12"), toDate("2013/03/02")));

    }

    public void test_chooseNearestPastDate() throws Exception {
        HandyDate date = handy("2013/03/03");
        assertEquals(toDate("2013/03/03"), date.chooseNearestPastDate(toDate("2013/03/03"), toDate("2013/03/04")));
        assertEquals(toDate("2013/03/02"), date.chooseNearestPastDate(toDate("2013/03/02"), toDate("2013/03/04")));
        assertEquals(toDate("2013/03/01"), date.chooseNearestPastDate(toDate("2013/03/01"), toDate("2013/03/04")));
        assertEquals(toDate("2013/03/02"), date.chooseNearestPastDate(toDate("2013/03/02"), toDate("2013/03/01")));
        assertEquals(toDate("2013/03/02 12:34:56"),
                date.chooseNearestPastDate(toDate("2013/03/02 12:34:56"), toDate("2013/03/02 12:34:55")));
        assertEquals(toDate("2013/03/01"),
                date.chooseNearestPastDate(toDate("2013/03/03 00:00:01"), toDate("2013/02/28"), toDate("2013/03/01")));
    }

    // ===================================================================================
    //                                                                          To Display
    //                                                                          ==========
    public void test_toDisp_TimeZone_GMT_constructor() throws Exception {
        TimeZone gmtZone = TimeZone.getTimeZone("GMT");
        HandyDate handyDate = handy("2011/01/01", gmtZone);
        assertEquals("2011-01-01", handyDate.toDisp("yyyy-MM-dd"));
        assertEquals("2011-01-01 00:00:00", handyDate.toDisp("yyyy-MM-dd HH:mm:ss"));
        assertEquals("2011-01-01", handyDate.toDisp("yyyy-MM-dd", gmtZone));
        assertEquals("2011-01-01 00:00:00", handyDate.toDisp("yyyy-MM-dd HH:mm:ss", gmtZone));
        TimeZone japanZone = TimeZone.getTimeZone("Asia/Tokyo");
        assertEquals("2011-01-01", handyDate.toDisp("yyyy-MM-dd", japanZone));
        assertEquals("2011-01-01 09:00:00", handyDate.toDisp("yyyy-MM-dd HH:mm:ss", japanZone));
    }

    public void test_toDisp_TimeZone_GMT_later() throws Exception {
        TimeZone gmtZone = TimeZone.getTimeZone("GMT");
        HandyDate handyDate = handy(prepareJapanDate("2011/01/01")).timeZone(gmtZone);
        assertEquals("2010-12-31", handyDate.toDisp("yyyy-MM-dd"));
        assertEquals("2010-12-31 15:00:00", handyDate.toDisp("yyyy-MM-dd HH:mm:ss"));
        assertEquals("2010-12-31", handyDate.toDisp("yyyy-MM-dd", gmtZone));
        assertEquals("2010-12-31 15:00:00", handyDate.toDisp("yyyy-MM-dd HH:mm:ss", gmtZone));
        TimeZone japanZone = TimeZone.getTimeZone("Asia/Tokyo");
        assertEquals("2011-01-01", handyDate.toDisp("yyyy-MM-dd", japanZone));
        assertEquals("2011-01-01 00:00:00", handyDate.toDisp("yyyy-MM-dd HH:mm:ss", japanZone));
    }

    public void test_toDisp_TimeZone_Japan_constructor() throws Exception {
        TimeZone japanZone = TimeZone.getTimeZone("Asia/Tokyo");
        HandyDate handyDate = handy("2011/01/01", japanZone);
        assertEquals("2011-01-01", handyDate.toDisp("yyyy-MM-dd"));
        assertEquals("2011-01-01 00:00:00", handyDate.toDisp("yyyy-MM-dd HH:mm:ss"));
        assertEquals("2011-01-01", handyDate.toDisp("yyyy-MM-dd", japanZone));
        assertEquals("2011-01-01 00:00:00", handyDate.toDisp("yyyy-MM-dd HH:mm:ss", japanZone));
    }

    public void test_toDisp_TimeZone_Japan_later() throws Exception {
        TimeZone japanZone = TimeZone.getTimeZone("Asia/Tokyo");
        HandyDate handyDate = handy(prepareJapanDate("2011/01/01")).timeZone(japanZone);
        assertEquals("2011-01-01", handyDate.toDisp("yyyy-MM-dd"));
        assertEquals("2011-01-01 00:00:00", handyDate.toDisp("yyyy-MM-dd HH:mm:ss"));
    }

    public void test_toDisp_TimeZone_Canada_constructor() throws Exception {
        TimeZone canadaZone = TimeZone.getTimeZone("Canada/Mountain"); // GMT-07:00, Etc/GMT+7
        HandyDate handyDate = handy("2011/01/01", canadaZone);
        assertEquals("2011-01-01", handyDate.toDisp("yyyy-MM-dd"));
        assertEquals("2011-01-01 00:00:00", handyDate.toDisp("yyyy-MM-dd HH:mm:ss"));
        assertEquals("2011-01-01", handyDate.toDisp("yyyy-MM-dd", canadaZone));
        assertEquals("2011-01-01 00:00:00", handyDate.toDisp("yyyy-MM-dd HH:mm:ss", canadaZone));
        TimeZone gmtZone = TimeZone.getTimeZone("GMT");
        assertEquals("2011-01-01", handyDate.toDisp("yyyy-MM-dd", gmtZone));
        assertEquals("2011-01-01 07:00:00", handyDate.toDisp("yyyy-MM-dd HH:mm:ss", gmtZone));
    }

    public void test_toDisp_TimeZone_Canada_later() throws Exception {
        TimeZone canadaZone = TimeZone.getTimeZone("Canada/Mountain"); // GMT-07:00, Etc/GMT+7
        HandyDate handyDate = handy(prepareJapanDate("2011/01/01")).timeZone(canadaZone);
        assertEquals("2010-12-31", handyDate.toDisp("yyyy-MM-dd"));
        assertEquals("2010-12-31 08:00:00", handyDate.toDisp("yyyy-MM-dd HH:mm:ss"));
        assertEquals("2010-12-31", handyDate.toDisp("yyyy-MM-dd", canadaZone));
        assertEquals("2010-12-31 08:00:00", handyDate.toDisp("yyyy-MM-dd HH:mm:ss", canadaZone));
        TimeZone gmtZone = TimeZone.getTimeZone("GMT");
        assertEquals("2010-12-31", handyDate.toDisp("yyyy-MM-dd", gmtZone));
        assertEquals("2010-12-31 15:00:00", handyDate.toDisp("yyyy-MM-dd HH:mm:ss", gmtZone));
    }

    public void test_toDisp_TimeZone_Canada_daylight_constructor() throws Exception {
        TimeZone canadaZone = TimeZone.getTimeZone("Canada/Mountain"); // GMT-07:00, Etc/GMT+7
        HandyDate handyDate = handy("2011/07/01", canadaZone).timeZone(canadaZone);
        assertEquals("2011-07-01", handyDate.toDisp("yyyy-MM-dd"));
        assertEquals("2011-07-01 00:00:00", handyDate.toDisp("yyyy-MM-dd HH:mm:ss"));
        assertEquals("2011-07-01", handyDate.toDisp("yyyy-MM-dd", canadaZone));
        assertEquals("2011-07-01 00:00:00", handyDate.toDisp("yyyy-MM-dd HH:mm:ss", canadaZone));
        TimeZone gmtZone = TimeZone.getTimeZone("GMT");
        assertEquals("2011-07-01", handyDate.toDisp("yyyy-MM-dd", gmtZone));
        assertEquals("2011-07-01 06:00:00", handyDate.toDisp("yyyy-MM-dd HH:mm:ss", gmtZone));
    }

    public void test_toDisp_TimeZone_Canada_daylight_later() throws Exception {
        TimeZone canadaZone = TimeZone.getTimeZone("Canada/Mountain"); // GMT-07:00, Etc/GMT+7
        HandyDate handyDate = handy(prepareJapanDate("2011/07/01")).timeZone(canadaZone);
        assertEquals("2011-06-30", handyDate.toDisp("yyyy-MM-dd"));
        assertEquals("2011-06-30 09:00:00", handyDate.toDisp("yyyy-MM-dd HH:mm:ss"));
        assertEquals("2011-06-30", handyDate.toDisp("yyyy-MM-dd", canadaZone));
        assertEquals("2011-06-30 09:00:00", handyDate.toDisp("yyyy-MM-dd HH:mm:ss", canadaZone));
        TimeZone gmtZone = TimeZone.getTimeZone("GMT");
        assertEquals("2011-06-30", handyDate.toDisp("yyyy-MM-dd", gmtZone));
        assertEquals("2011-06-30 15:00:00", handyDate.toDisp("yyyy-MM-dd HH:mm:ss", gmtZone));
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    public void test_deepCopy_basic() throws Exception {
        // ## Arrange ##
        String pattern = "yyyy/MM/dd";
        HandyDate date = handy("2011/01/01");
        date.beginMonth_Day(10);

        // ## Act ##
        HandyDate copy = date.deepCopy();

        // ## Assert ##
        copy.addDay(1);
        assertEquals("2011/01/02", copy.toDisp(pattern));
        assertEquals("2011/01/01", date.toDisp(pattern));

        copy.moveToMonthJust();
        assertEquals("2011/01/10", copy.toDisp(pattern));
        assertEquals("2011/01/01", date.toDisp(pattern));
        date.moveToMonthJust();
        assertEquals("2011/01/10", date.toDisp(pattern));
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected HandyDate handy(Date date) {
        return new HandyDate(date);
    }

    protected HandyDate handy(String exp) {
        return new HandyDate(exp);
    }

    protected HandyDate handy(String exp, TimeZone timeZone) {
        return new HandyDate(exp, timeZone);
    }

    protected HandyDate year4(String exp) {
        return new HandyDate(exp).beginYear_Month(4);
    }

    protected HandyDate yearPre11(String exp) {
        return new HandyDate(exp).beginYear_PreviousMonth(11);
    }

    protected HandyDate month3(String exp) {
        return new HandyDate(exp).beginMonth_Day(3);
    }

    protected HandyDate monthPre26(String exp) {
        return new HandyDate(exp).beginMonth_PreviousDay(26);
    }

    protected HandyDate handyRelated(String exp) {
        return new HandyDate(exp).beginYear_Month02_February().beginMonth_Day(3).beginDay_Hour(4);
    }

    protected Date prepareDate(String exp, String timeZoneId) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        TimeZone japanZone = TimeZone.getTimeZone(timeZoneId);
        dateFormat.setTimeZone(japanZone);
        return dateFormat.parse(exp);
    }

    protected Date prepareGMTDate(String exp) throws ParseException {
        return prepareDate(exp, "GMT");
    }

    protected Date prepareJapanDate(String exp) throws ParseException {
        return prepareDate(exp, "Asia/Tokyo");
    }

    protected Date prepareCanadaDate(String exp) throws ParseException {
        return prepareDate(exp, "Canada/Mountain");
    }
}
