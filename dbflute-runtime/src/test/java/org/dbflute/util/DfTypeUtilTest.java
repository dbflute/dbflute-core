/*
 * Copyright 2014-2014 the original author or authors.
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
package org.dbflute.util;

import static org.dbflute.util.DfTypeUtil.GMT_AD_ORIGIN_MILLISECOND;
import static org.dbflute.util.DfTypeUtil.addCalendarDay;
import static org.dbflute.util.DfTypeUtil.addDateDay;
import static org.dbflute.util.DfTypeUtil.addDateHour;
import static org.dbflute.util.DfTypeUtil.addDateMillisecond;
import static org.dbflute.util.DfTypeUtil.addDateMinute;
import static org.dbflute.util.DfTypeUtil.addDateMonth;
import static org.dbflute.util.DfTypeUtil.addDateSecond;
import static org.dbflute.util.DfTypeUtil.addDateWeekOfMonth;
import static org.dbflute.util.DfTypeUtil.addDateYear;
import static org.dbflute.util.DfTypeUtil.clearCalendarTimeParts;
import static org.dbflute.util.DfTypeUtil.clearDateTimeParts;
import static org.dbflute.util.DfTypeUtil.createDateFormat;
import static org.dbflute.util.DfTypeUtil.getTimeZonedADOriginMillis;
import static org.dbflute.util.DfTypeUtil.isDateAD;
import static org.dbflute.util.DfTypeUtil.isDateBC;
import static org.dbflute.util.DfTypeUtil.isLocalDateAD;
import static org.dbflute.util.DfTypeUtil.isLocalDateBC;
import static org.dbflute.util.DfTypeUtil.toBigInteger;
import static org.dbflute.util.DfTypeUtil.toBinary;
import static org.dbflute.util.DfTypeUtil.toCalendar;
import static org.dbflute.util.DfTypeUtil.toClassTitle;
import static org.dbflute.util.DfTypeUtil.toDate;
import static org.dbflute.util.DfTypeUtil.toInteger;
import static org.dbflute.util.DfTypeUtil.toLocalDate;
import static org.dbflute.util.DfTypeUtil.toLocalDateTime;
import static org.dbflute.util.DfTypeUtil.toLocalTime;
import static org.dbflute.util.DfTypeUtil.toSqlDate;
import static org.dbflute.util.DfTypeUtil.toStringDate;
import static org.dbflute.util.DfTypeUtil.toTime;
import static org.dbflute.util.DfTypeUtil.toTimestamp;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dbflute.util.DfTypeUtil.ParseDateException;
import org.dbflute.util.DfTypeUtil.ParseDateNumberFormatException;
import org.dbflute.util.DfTypeUtil.ParseDateOutOfCalendarException;
import org.dbflute.util.DfTypeUtil.ParseTimestampException;
import org.dbflute.util.DfTypeUtil.ParseTimestampNumberFormatException;
import org.dbflute.util.DfTypeUtil.ParseTimestampOutOfCalendarException;

/**
 * @author jflute
 * @since 0.9.0 (2009/01/19 Monday)
 */
public class DfTypeUtilTest extends TestCase { // because PlainTestCase uses this

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance for sub class. */
    private final static Logger _logger = Logger.getLogger(DfTypeUtilTest.class);

    // ===================================================================================
    //                                                                              String
    //                                                                              ======
    public void test_toString_basic() {
        assertNull(DfTypeUtil.toString(null));
        assertEquals("", DfTypeUtil.toString(""));
        assertEquals("foo", DfTypeUtil.toString("foo"));
        assertEquals("3", DfTypeUtil.toString(3));
        assertEquals("3", DfTypeUtil.toString(3L));
        assertEquals("3.7", DfTypeUtil.toString(new BigDecimal("3.7")));
    }

    public void test_toString_Exception() {
        String stackTrace = DfTypeUtil.toString(new Exception("foo"));
        assertTrue(stackTrace.contains("foo"));
    }

    public void test_toClassTitle_basic() {
        assertNull(toClassTitle(null));
        assertEquals("", toClassTitle(""));
        assertEquals("  ", toClassTitle("  "));
        assertEquals("Foo", toClassTitle("com.example.Foo"));
        assertEquals("Foo", toClassTitle("Foo"));
        assertEquals("Foo$Bar", toClassTitle("com.example.Foo$Bar"));
        assertEquals("Foo$1", toClassTitle("com.example.Foo$1"));
        assertEquals("Foo$1", toClassTitle("Foo$1"));
        assertEquals("String", toClassTitle(String.class));
        assertEquals("Object", toClassTitle(Object.class.getName()));
        assertEquals("Object", toClassTitle(Object.class));
        Object inner = new Object() {
        };
        assertEquals(getClass().getSimpleName() + "$1", toClassTitle(inner.getClass().getName()));
        assertEquals(getClass().getSimpleName() + "$1", toClassTitle(inner.getClass()));
        assertEquals(getClass().getSimpleName() + "$1", toClassTitle(inner));
        assertEquals(getClass().getSimpleName() + "$TestTitle", toClassTitle(TestTitle.class.getName()));
        assertEquals(getClass().getSimpleName() + "$TestTitle", toClassTitle(TestTitle.class));
        assertEquals(getClass().getSimpleName() + "$TestTitle", toClassTitle(new TestTitle()));
    }

    private static class TestTitle {
    }

    // -----------------------------------------------------
    //                                                  Date
    //                                                  ----
    public void test_toString_Date_basic() {
        assertEquals("2014-10-28 12:34:56.789", DfTypeUtil.toString(toDate("2014/10/28 12:34:56.789")));
        assertEquals("2014-10-28 12:34:56.789", DfTypeUtil.toString(toTimestamp("2014/10/28 12:34:56.789")));
        assertEquals("12:34:56", DfTypeUtil.toString(toTime("2014/10/28 12:34:56.789")));
        assertEquals("2014-10-28", DfTypeUtil.toString(toLocalDate("2014/10/28 12:34:56.789")));
        assertEquals("2014-10-28 12:34:56.789", DfTypeUtil.toString(toLocalDateTime("2014/10/28 12:34:56.789")));
        assertEquals("2014-10-28 12:34:56.789", DfTypeUtil.toString(toLocalDateTime("2014/10/28 12:34:56.789999")));
        assertEquals("12:34:56", DfTypeUtil.toString(toLocalTime("2014/10/28 12:34:56.789")));

        assertEquals("789999", DfTypeUtil.toString(toLocalDateTime("2014/10/28 12:34:56.789999"), "SSSSSS"));
        assertEquals("12:34:56.789999999", DfTypeUtil.toString(toLocalTime("12:34:56.789999999"), "HH:mm:ss.SSSSSSSSS"));
    }

    // ===================================================================================
    //                                                                              Number
    //                                                                              ======
    // -----------------------------------------------------
    //                                               Integer
    //                                               -------
    public void test_toInteger_basic() {
        assertNull(DfTypeUtil.toInteger(null));
        assertNull(DfTypeUtil.toInteger(""));
        assertNull(DfTypeUtil.toInteger(" "));
        assertEquals(Integer.valueOf(3), toInteger("3"));
        assertEquals(Integer.valueOf(-3), toInteger("-3"));
        assertEquals(Integer.valueOf(3), toInteger("+3"));
        assertEquals(Integer.valueOf(33333), DfTypeUtil.toInteger("33333"));
        assertEquals(Integer.valueOf(-33333), toInteger("-33333"));
        assertEquals(Integer.valueOf(33333), DfTypeUtil.toInteger("33,333"));
    }

    public void test_toInteger_notNumber() {
        try {
            DfTypeUtil.toInteger("foo");
            fail();
        } catch (NumberFormatException e) {
            // OK
            log(e.getMessage());
        }
        try {
            DfTypeUtil.toInteger("3.3");
            fail();
        } catch (NumberFormatException e) {
            // OK
            log(e.getMessage());
        }
    }

    public void test_toBigInteger_basic() {
        assertNull(DfTypeUtil.toBigInteger(null));
        assertNull(DfTypeUtil.toBigInteger(""));
        assertNull(DfTypeUtil.toBigInteger(" "));
        assertEquals(BigInteger.valueOf(3), toBigInteger("3"));
        assertEquals(BigInteger.valueOf(33333), DfTypeUtil.toBigInteger("33333"));
        assertEquals(BigInteger.valueOf(-33333), DfTypeUtil.toBigInteger("-33333"));
        assertEquals(BigInteger.valueOf(33333), DfTypeUtil.toBigInteger("33,333"));
        assertEquals(new BigInteger("18446744073709551615"), DfTypeUtil.toBigInteger("18446744073709551615"));
        assertEquals(new BigInteger("18446744073709551615"), DfTypeUtil.toBigInteger(new BigDecimal("18446744073709551615")));
        assertEquals(new BigInteger("123456789123456"), DfTypeUtil.toBigInteger("123456789123456"));
        assertEquals(new BigInteger("18446744073709551615"), new BigInteger("18446744073709551615") {
            private static final long serialVersionUID = 1L;
        });
        assertEquals(new BigInteger(String.valueOf(Long.MAX_VALUE)), DfTypeUtil.toBigInteger(Long.MAX_VALUE));
    }

    // ===================================================================================
    //                                                                            Time API
    //                                                                            ========
    // -----------------------------------------------------
    //                                            Local Date
    //                                            ----------
    public void test_toLocalDate_fromUtilDate_timeZone() {
        TimeZone gmt2hour = TimeZone.getTimeZone("GMT+2");
        Date pureDate = toDate("2009-12-13 01:23:45.123", gmt2hour);
        LocalDate localDate = toLocalDate(pureDate, gmt2hour);
        assertEquals("2009-12-13", localDate.format(DateTimeFormatter.ISO_DATE));
    }

    public void test_toLocalDate_fromUtilDate_gmt9hour() {
        TimeZone gmt9hour = TimeZone.getTimeZone("GMT+9");
        Date pureDate = toDate("2009-12-13 01:23:45.123", gmt9hour);
        LocalDate localDate = toLocalDate(pureDate, gmt9hour);
        assertEquals("2009-12-13", localDate.format(DateTimeFormatter.ISO_DATE));
    }

    public void test_toLocalDate_fromStringDate_basic() {
        assertEquals(LocalDate.of(2009, 12, 13), toLocalDate("2009-12-13 01:23:45.123"));
        assertEquals(LocalDate.of(1582, 10, 04), toLocalDate("1582-10-04 01:23:45.123"));
        assertEquals(LocalDate.of(0001, 10, 04), toLocalDate("0001-10-04 01:23:45.123"));
        assertEquals(LocalDate.of(2014, 10, 28), toLocalDate("2014/10/28 12:34:56.789"));
    }

    public void test_toLocalDate_fromStringDate_pattern() {
        assertEquals(LocalDate.of(2009, 12, 13), toLocalDate("01:23:45.123$13-12-2009", "HH:mm:ss.SSS$dd-MM-yyyy"));
    }

    public void test_toLocalDate_illegal() {
        TimeZone gmt2hour = TimeZone.getTimeZone("GMT+2");
        assertNotNull(toLocalDate("2009-12-13 01:23:45.123", gmt2hour));
        try {
            assertNull(toLocalDate("2009-12-13 01:23:45.123", (String) null));
            fail();
        } catch (IllegalArgumentException e) {
            log(e.getMessage());
        }
        try {
            assertNull(toLocalDate("2009-12-13 01:23:45.123", (TimeZone) null));
            fail();
        } catch (IllegalArgumentException e) {
            log(e.getMessage());
        }
        try {
            assertNull(toLocalDate("2009-12-13 01:23:45.123", (String) null, (TimeZone) null));
            fail();
        } catch (IllegalArgumentException e) {
            log(e.getMessage());
        }
        assertNull(toLocalDate(null, gmt2hour));
        assertNull(toLocalDate("", gmt2hour));
        assertNull(toLocalDate(" ", gmt2hour));
        try {
            assertNull(toLocalDate("a", gmt2hour));
            fail();
        } catch (ParseDateException e) {
            log(e.getMessage());
        }
        try {
            log(toLocalDate("2014/10/2a 12:34:56.789"));
            fail();
        } catch (ParseDateNumberFormatException e) {
            log(e.getMessage());
        }
        try {
            toLocalDate("2014/10/28$12:34:56.789");
            fail();
        } catch (ParseDateNumberFormatException e) {
            log(e.getMessage());
        }
        try {
            toLocalDate("2014/19/28 12:34:56.789");
            fail();
        } catch (ParseDateException e) {
            log(e.getMessage());
        }
    }

    public void test_toLocalDate_isLocalDateAD() {
        // ## Arrange & Act & Assert ##
        assertTrue(isLocalDateAD(toLocalDate("2008-12-30 12:34:56.789")));
        LocalDate before = toLocalDate("BC0001-12-31 23:59:59.999");
        LocalDate after = toLocalDate("0001-01-01 00:00:00.000");
        log("before time = " + before);
        log("after  time = " + after);
        assertFalse(isLocalDateAD(before));
        assertTrue(isLocalDateAD(after));
        // cannot convert local to calendar if old date
        //assertEquals(GregorianCalendar.BC, toCalendar(before).get(Calendar.ERA));
        //assertEquals(GregorianCalendar.AD, toCalendar(after).get(Calendar.ERA));

        // extra
        assertEquals(after.atStartOfDay(), before.plusDays(1).atStartOfDay());
    }

    public void test_toLocalDate_isLocalDateBC() {
        // ## Arrange & Act & Assert ##
        assertFalse(isLocalDateBC(toLocalDate("2008-12-30 12:34:56.789")));
        LocalDate before = toLocalDate("BC0001-12-31 23:59:59.999");
        LocalDate after = toLocalDate("0001-01-01 00:00:00.000");
        log("before time = " + before);
        log("after  time = " + after);
        assertTrue(isLocalDateBC(before));
        assertFalse(isLocalDateBC(after));
        // cannot convert local to calendar if old date
        //assertEquals(GregorianCalendar.BC, toCalendar(before).get(Calendar.ERA));
        //assertEquals(GregorianCalendar.AD, toCalendar(after).get(Calendar.ERA));
    }

    // -----------------------------------------------------
    //                                        Local DateTime
    //                                        --------------
    public void test_toLocalDateTime_fromUtilDate_timeZone() {
        TimeZone gmt3Hour = TimeZone.getTimeZone("GMT+3");
        Date pureDate = toDate("2009-12-13 12:34:56.123", gmt3Hour);
        LocalDateTime localDate = toLocalDateTime(pureDate, gmt3Hour);
        assertEquals("2009-12-13T12:34:56.123", localDate.format(DateTimeFormatter.ISO_DATE_TIME));
    }

    public void test_toLocalDateTime_fromStringDate_basic() {
        assertEquals(LocalDateTime.of(2009, 12, 13, 01, 23, 45, 123000000), toLocalDateTime("2009-12-13 01:23:45.123"));
        assertEquals(LocalDateTime.of(1582, 10, 04, 23, 23, 45, 456000000), toLocalDateTime("1582-10-04 23:23:45.456"));
        assertEquals(LocalDateTime.of(0001, 10, 04, 14, 23, 45, 789000000), toLocalDateTime("0001-10-04 14:23:45.789"));
        assertEquals(LocalDateTime.of(0001, 10, 04, 14, 23, 45, 9000000), toLocalDateTime("0001-10-04 14:23:45.9"));
        assertEquals(LocalDateTime.of(0001, 10, 04, 14, 23, 45, 89000000), toLocalDateTime("0001-10-04 14:23:45.89"));
        assertEquals(LocalDateTime.of(0001, 10, 04, 14, 23, 45, 678900000), toLocalDateTime("0001-10-04 14:23:45.6789"));
        assertEquals(LocalDateTime.of(0001, 10, 04, 14, 23, 45, 123456789), toLocalDateTime("0001-10-04 14:23:45.123456789"));
        try {
            toLocalDateTime("0001-10-04 14:23:45.123456789123");
            fail();
        } catch (ParseDateException e) {
            log(e.getMessage());
        }
    }

    public void test_toLocalDateTime_fromStringDate_pattern() {
        assertEquals(LocalDateTime.of(2009, 12, 13, 01, 23, 45, 123000000),
                toLocalDateTime("01:23:45.123$13-12-2009", "HH:mm:ss.SSS$dd-MM-yyyy"));
    }

    public void test_toLocalDateTime_fromStringDate_timeZone() {
        // ## Arrange ##
        String strDate = "1970-01-01 09:00:06.789";

        // ## Act ##
        TimeZone gmt2hour = TimeZone.getTimeZone("GMT+2");
        LocalDateTime ldt = toLocalDateTime(strDate, gmt2hour);

        // ## Assert ##
        String converted = ldt.format(DateTimeFormatter.ISO_DATE_TIME);
        log("converted : " + converted);
        assertEquals("1970-01-01T09:00:06.789", converted);

        // e.g. 1 hour is 3600000L, 7 hours is 25200000L, 9 hours is 32400000L
        Date reversedDate = DfTypeUtil.toDate(ldt, gmt2hour);
        String reversedStrDate = DfTypeUtil.toStringDate(reversedDate, "yyyy/MM/dd HH:mm:ss.SSS", gmt2hour);
        log("reversed  : " + reversedStrDate + ", " + reversedDate.getTime());
        assertEquals("1970/01/01 09:00:06.789", reversedStrDate);

        TimeZone gmtZone = TimeZone.getTimeZone("GMT");
        Date gmt7hour = toDate("1970/01/01 07:00:06.789", gmtZone);
        log("emg7hour  : " + toStringDate(gmt7hour, "yyyy/MM/dd HH:mm:ss.SSS", gmtZone));
        assertEquals(gmt7hour.getTime(), reversedDate.getTime());
    }

    public void test_toLocalDateTime_illegal() {
        TimeZone gmt2hour = TimeZone.getTimeZone("GMT+2");
        assertNotNull(DfTypeUtil.toLocalDateTime("2009-12-13 01:23:45.123", gmt2hour));
        try {
            assertNull(toLocalDateTime("2009-12-13 01:23:45.123", (String) null));
            fail();
        } catch (IllegalArgumentException e) {
            log(e.getMessage());
        }
        try {
            assertNull(toLocalDateTime("2009-12-13 01:23:45.123", (TimeZone) null));
            fail();
        } catch (IllegalArgumentException e) {
            log(e.getMessage());
        }
        try {
            assertNull(toLocalDateTime("2009-12-13 01:23:45.123", (String) null, (TimeZone) null));
            fail();
        } catch (IllegalArgumentException e) {
            log(e.getMessage());
        }
        assertNull(DfTypeUtil.toLocalDateTime(null, gmt2hour));
        assertNull(DfTypeUtil.toLocalDateTime("", gmt2hour));
        assertNull(DfTypeUtil.toLocalDateTime(" ", gmt2hour));
        try {
            assertNull(DfTypeUtil.toLocalDateTime("a", gmt2hour));
            fail();
        } catch (ParseDateException e) {
            log(e.getMessage());
        }
    }

    // -----------------------------------------------------
    //                                            Local Time
    //                                            ----------
    public void test_toLocalTime_fromUtilDate_timeZone() {
        TimeZone gmt3Hour = TimeZone.getTimeZone("GMT+3");
        Date pureDate = toDate("2009-12-13 12:34:56.123", gmt3Hour);
        LocalTime localDate = toLocalTime(pureDate, gmt3Hour);
        assertEquals("12:34:56.123", localDate.format(DateTimeFormatter.ISO_TIME));
    }

    public void test_toLocalTime_fromStringDate_basic() {
        assertEquals(LocalTime.of(23, 59, 59), toLocalTime("23:59:59"));
    }

    public void test_toLocalTime_fromStringDate_pattern() {
        assertEquals(LocalTime.of(23, 59, 59), toLocalTime("59:59:23", "ss:mm:HH"));
    }

    // ===================================================================================
    //                                                                          (util)Date
    //                                                                          ==========
    public void test_toDate_sameClass() {
        // ## Arrange ##
        DateFormat df = DfTypeUtil.createDateFormat("yyyy/MM/dd HH:mm:ss");
        Date pureDate = new Date(toDate("2009-12-13 12:34:56.123").getTime());

        // ## Act ##
        Date date = DfTypeUtil.toDate(pureDate);

        // ## Assert ##
        assertEquals(java.util.Date.class, date.getClass());
        assertFalse(date instanceof Timestamp); // because it returns pure date
        assertEquals("2009/12/13 12:34:56", df.format(date));
    }

    public void test_toDate_subClass() {
        // ## Arrange ##
        DateFormat df = createDateFormat("yyyy/MM/dd HH:mm:ss");
        Timestamp timestamp = toTimestamp("2009-12-13 12:34:56.123");

        // ## Act ##
        Date date = toDate(timestamp);

        // ## Assert ##
        assertEquals(java.util.Date.class, date.getClass());
        assertFalse(date instanceof Timestamp); // because it returns pure date
        assertEquals("2009/12/13 12:34:56", df.format(date));
    }

    public void test_toDate_string_basic() {
        // ## Arrange ##
        DateFormat df = DfTypeUtil.createDateFormat("yyyy/MM/dd HH:mm:ss");

        // ## Act & Assert ##
        assertNull(toDate(null));
        assertNull(toDate(""));
        assertEquals("2008/12/30 12:34:56", df.format(toDate(" 2008-12-30 12:34:56 "))); // trimmed
        assertEquals("2008/12/30 12:34:56", df.format(toDate("2008-12-30 12:34:56"))); // normal
        assertEquals("2008/12/30 00:00:00", df.format(toDate("20081230", "yyyyMMdd")));
        assertEquals("2009/11/30 00:00:00", df.format(toDate("20091130", "yyyyMMdd")));
        assertEquals("2009/11/01 00:00:00", df.format(toDate("200911", "yyyyMM")));
        assertEquals("2009/01/01 00:00:00", df.format(toDate("2009", "yyyy")));
        assertEquals("2009/01/01 00:00:00", df.format(toDate("2009", "yyyy")));
        // illegal patterns were implemented at test_toDate_illegal()
        //assertEquals("2009/11/30 00:00:00", df.format(toDate("2009113012", "yyyyMMdd")));
    }

    public void test_toDate_string_instanceType() {
        // ## Arrange & Act & Assert ##
        assertEquals(java.util.Date.class, toDate("2008-12-30 12:34:56.789").getClass());
        assertNotSame(java.sql.Date.class, toDate("2008-12-30 12:34:56.789").getClass());
        assertNotSame(java.sql.Timestamp.class, toDate("2008-12-30 12:34:56.789").getClass());
    }

    public void test_toDate_string_AD() {
        // ## Arrange ##
        DateFormat df = DfTypeUtil.createDateFormat("yyyy/MM/dd HH:mm:ss");
        DateFormat dfmil = DfTypeUtil.createDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        DateFormat gdf = DfTypeUtil.createDateFormat("Gyyyy/MM/dd HH:mm:ss.SSS");
        long timeZonedADJustTimeMillis = getTimeZonedADOriginMillis(null);

        // ## Act & Assert ##
        log(gdf.format(DfTypeUtil.toDate("A.D.2008-9-1")));
        assertTrue(DfTypeUtil.toDate("A.D.1-1-1 00:00:00.000").getTime() >= timeZonedADJustTimeMillis);
        assertEquals(timeZonedADJustTimeMillis, DfTypeUtil.toDate("A.D.1-1-1 00:00:00.000").getTime());
        assertTrue(DfTypeUtil.toDate("2008-10-21 12:34:56").getTime() >= timeZonedADJustTimeMillis);
        assertTrue(DfTypeUtil.toDate("AD8-9-1 12:34:56").getTime() >= timeZonedADJustTimeMillis);
        assertTrue(DfTypeUtil.toDate("A.D.8-9-1 12:34:56").getTime() >= timeZonedADJustTimeMillis);
        assertEquals("0008/09/01 12:34:56", df.format(DfTypeUtil.toDate("AD8-9-1 12:34:56")));
        assertEquals("2008/09/01 00:00:00", df.format(DfTypeUtil.toDate("A.D.2008-9-1")));
        assertEquals("0001/01/01 00:00:00.000", dfmil.format(DfTypeUtil.toDate("AD1-1-1 00:00:00.000")));
        assertEquals("0001/01/01 00:00:00.000", dfmil.format(DfTypeUtil.toDate("date 1-1-1 00:00:00.000")));
        assertEquals("0001/01/01 00:00:00.000", dfmil.format(DfTypeUtil.toDate("date AD1-1-1 00:00:00.000")));
    }

    public void test_toDate_string_BC() {
        // ## Arrange ##
        DateFormat df = DfTypeUtil.createDateFormat("yyyy/MM/dd HH:mm:ss");
        SimpleDateFormat fullDf = new SimpleDateFormat("Gyyyy/MM/dd HH:mm:ss.SSS");
        long timeZonedADJustTimeMillis = getTimeZonedADOriginMillis(null);

        // ## Act & Assert ##
        log(fullDf.format(DfTypeUtil.toDate("-2008-09-01 02:04:06")));
        assertTrue(DfTypeUtil.toDate("B.C.0001-12-31 23:59:59.999").getTime() < timeZonedADJustTimeMillis);
        assertEquals(timeZonedADJustTimeMillis - 1L, DfTypeUtil.toDate("B.C.0001-12-31 23:59:59.999").getTime());
        assertTrue(DfTypeUtil.toDate("-8-9-1").getTime() < timeZonedADJustTimeMillis);
        assertTrue(DfTypeUtil.toDate("-1-9-1").getTime() < timeZonedADJustTimeMillis);
        assertTrue(DfTypeUtil.toDate("-8-9-1 2:4:6").getTime() < timeZonedADJustTimeMillis);
        assertTrue(DfTypeUtil.toDate("date -80901").getTime() < timeZonedADJustTimeMillis);
        assertTrue(DfTypeUtil.toDate("-2008-09-01 02:04:06").getTime() < timeZonedADJustTimeMillis);
        assertTrue(DfTypeUtil.toDate("-2008-13-01 02:04:06").getTime() < timeZonedADJustTimeMillis);
        assertTrue(DfTypeUtil.toDate("BC8-9-1 2:4:6").getTime() < timeZonedADJustTimeMillis);
        assertTrue(DfTypeUtil.toDate("BC2008-09-01 02:04:06").getTime() < timeZonedADJustTimeMillis);
        assertTrue(DfTypeUtil.toDate("BC2008-13-01 02:04:06").getTime() < timeZonedADJustTimeMillis);
        assertTrue(DfTypeUtil.toDate("B.C.8-9-1 2:4:6").getTime() < timeZonedADJustTimeMillis);
        assertTrue(DfTypeUtil.toDate("B.C.2008-09-01 02:04:06").getTime() < timeZonedADJustTimeMillis);
        assertTrue(DfTypeUtil.toDate("B.C.2008-13-01 02:04:06").getTime() < timeZonedADJustTimeMillis);
        assertEquals("2008/11/01 02:04:06", df.format(toDate("BC2008-11-01 02:04:06")));
        // until 1.0.x (changed to be minus 0 origin)
        //assertEquals("2008/11/01 02:04:06", df.format(toDate("-2008-11-01 02:04:06")));
        //assertEquals("0008/09/01 00:00:00", df.format(toDate("date -80901")));
        //assertEquals("2007/01/01 02:04:06", df.format(toDate("-2008-13-01 02:04:06"))); // no calendar check when BC
        assertEquals("2009/11/01 02:04:06", df.format(toDate("-2008-11-01 02:04:06")));
        assertEquals("0009/09/01 00:00:00", df.format(toDate("date -80901")));
        assertEquals("2008/01/01 02:04:06", df.format(toDate("-2008-13-01 02:04:06"))); // no calendar check when BC
    }

    public void test_toDate_string_various() {
        // ## Arrange ##
        DateFormat df = DfTypeUtil.createDateFormat("yyyy/MM/dd HH:mm:ss");
        DateFormat dfmil = DfTypeUtil.createDateFormat("yyyy/MM/dd HH:mm:ss.SSS");

        // ## Act & Assert ##
        assertEquals("0002/01/12 00:00:00", df.format(DfTypeUtil.toDate("date 20112")));
        assertEquals("0012/01/22 00:00:00", df.format(DfTypeUtil.toDate("date 120122")));
        assertEquals("0923/01/27 00:00:00", df.format(DfTypeUtil.toDate("date 9230127")));
        assertEquals("2008/12/30 00:00:00", df.format(DfTypeUtil.toDate("date 20081230")));
        assertEquals("2008/12/30 00:00:00", df.format(DfTypeUtil.toDate("2008/12/30")));
        assertEquals("2008/12/30 00:00:00", df.format(DfTypeUtil.toDate("2008-12-30")));
        assertEquals("2008/12/30 12:34:56", df.format(DfTypeUtil.toDate("2008-12-30 12:34:56")));
        assertEquals("2008/12/30 12:34:56", df.format(DfTypeUtil.toDate("2008-12-30 12:34:56.789")));
        assertEquals("2008/09/30 12:34:56", df.format(DfTypeUtil.toDate("2008-09-30 12:34:56")));
        assertEquals("2008/09/30 12:34:56", df.format(DfTypeUtil.toDate("2008-9-30 12:34:56")));
        assertEquals("2008/09/01 12:34:56", df.format(DfTypeUtil.toDate("2008-9-1 12:34:56")));
        assertEquals("0008/09/01 12:34:56", df.format(DfTypeUtil.toDate("8-9-1 12:34:56")));
        assertEquals("2008/09/01 00:00:00", df.format(DfTypeUtil.toDate("2008-9-1")));
        assertEquals("0008/09/01 02:04:06", df.format(DfTypeUtil.toDate("8-9-1 02:04:06")));
        assertEquals("0008/09/01 02:04:06", df.format(DfTypeUtil.toDate("8-9-1 2:4:6")));
        assertEquals("2008/12/30 12:34:56.012", dfmil.format(DfTypeUtil.toDate("2008-12-30 12:34:56.12")));
        assertEquals("2008/12/30 12:34:56.789", dfmil.format(DfTypeUtil.toDate("2008-12-30 12:34:56.789")));
        assertEquals("2008/12/30 12:34:56.123", dfmil.format(DfTypeUtil.toDate("2008-12-30 12:34:56.123456")));
    }

    public void test_toDate_long_basic() {
        // ## Arrange ##
        DateFormat dfmil = DfTypeUtil.createDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        Date date = DfTypeUtil.toDate("2008-12-30 12:34:56.789");

        // ## Act ##
        Date actual = DfTypeUtil.toDate(date.getTime());

        // ## Assert ##
        assertEquals(dfmil.format(date), dfmil.format(actual));
    }

    public void test_toDate_LocalDateTime_basic() {
        // ## Arrange ##
        TimeZone gmt9hour = TimeZone.getTimeZone("GMT+9");
        DateFormat dfmil = DfTypeUtil.createDateFormat("yyyy/MM/dd HH:mm:ss.SSS", gmt9hour);

        // ## Act ##
        TimeZone gmtZone = TimeZone.getTimeZone("GMT");
        Date actual = DfTypeUtil.toDate(LocalDateTime.of(1970, 1, 1, 0, 0, 0), gmtZone);

        // ## Assert ##
        String formatted = dfmil.format(actual);
        log(formatted + ", " + actual.getTime());
        assertEquals("1970/01/01 09:00:00.000", formatted);
        assertEquals(0L, actual.getTime());
    }

    public void test_toDate_illegal() {
        // short expression
        try {
            DfTypeUtil.toDate("2009-12");

            fail();
        } catch (ParseDateException e) {
            // OK
            log(e.getMessage());
        }
        try {
            DfTypeUtil.toDate("date 2009");

            fail();
        } catch (ParseDateException e) {
            // OK
            log(e.getMessage());
        }
        try {
            DfTypeUtil.toDate("2009-12");

            fail();
        } catch (ParseDateException e) {
            // OK
            log(e.getMessage());
        }
        try {
            DfTypeUtil.toDate("date 20091");

            fail();
        } catch (ParseDateOutOfCalendarException e) {
            // OK
            log(e.getMessage());
        }

        // out of calendar
        try {
            DfTypeUtil.toDate("2009-12-09 12:34:60");

            fail();
        } catch (ParseDateOutOfCalendarException e) {
            // OK
            log(e.getMessage());
        }
        try {
            DfTypeUtil.toDate("AD2009-12-09 12:34:60");

            fail();
        } catch (ParseDateOutOfCalendarException e) {
            // OK
            log(e.getMessage());
        }
        try {
            DfTypeUtil.toDate("2009-1209");

            fail();
        } catch (ParseDateException e) {
            // OK
            log(e.getMessage());
        }

        // number format
        try {
            DfTypeUtil.toDate("2009-12-0-9 12:34:56");

            fail();
        } catch (ParseDateNumberFormatException e) {
            // OK
            log(e.getMessage());
        }
        try {
            DfTypeUtil.toDate("2009-12-a9 12:34:56");

            fail();
        } catch (ParseDateNumberFormatException e) {
            // OK
            log(e.getMessage());
        }
        try {
            DfTypeUtil.toDate("2009-12-09 12:34:a6");

            fail();
        } catch (ParseDateNumberFormatException e) {
            // OK
            log(e.getMessage());
        }
        try {
            DfTypeUtil.toDate("0000-12-09 12:34:26");

            fail();
        } catch (ParseDateOutOfCalendarException e) {
            // OK
            log(e.getMessage());
        }

        // with pattern
        try {
            DfTypeUtil.toDate("2009120101", "yyyyMMdd");

            fail();
        } catch (ParseDateException e) {
            // OK
            log(e.getMessage());
        }
    }

    public void test_toDate_isDateAD() {
        // ## Arrange & Act & Assert ##
        assertTrue(isDateAD(toDate("2008-12-30 12:34:56.789")));
        Date before = toDate("BC0001-12-31 23:59:59.999");
        Date after = toDate("0001-01-01 00:00:00.000");
        log("before time = " + before.getTime());
        log("after  time = " + after.getTime());
        assertFalse(isDateAD(before));
        assertTrue(isDateAD(after));
        assertEquals(GregorianCalendar.BC, toCalendar(before).get(Calendar.ERA));
        assertEquals(GregorianCalendar.AD, toCalendar(after).get(Calendar.ERA));

        // extra
        addDateDay(before, 1);
        clearDateTimeParts(before);
        assertEquals(after, before);
    }

    public void test_toDate_isDateAD_timeZone() {
        // ## Arrange & Act & Assert ##
        TimeZone gmt2hour = TimeZone.getTimeZone("GMT+2");
        assertTrue(isDateAD(toDate("2008-12-30 12:34:56.789", gmt2hour)));
        Date before = toDate("BC0001-12-31 23:59:59.999", gmt2hour);
        Date after = toDate("0001-01-01 00:00:00.000", gmt2hour);
        log("before time = " + before.getTime());
        log("after  time = " + after.getTime());
        assertFalse(isDateAD(before, gmt2hour));
        assertTrue(isDateAD(after, gmt2hour));
        Calendar beforeCal = toCalendar(before, gmt2hour);
        assertEquals(GregorianCalendar.BC, beforeCal.get(Calendar.ERA));
        assertEquals(GregorianCalendar.AD, toCalendar(after, gmt2hour).get(Calendar.ERA));

        // extra
        addCalendarDay(beforeCal, 1);
        clearCalendarTimeParts(beforeCal);
        assertEquals(after, beforeCal.getTime());
    }

    public void test_toDate_isDateBC() {
        // ## Arrange & Act & Assert ##
        assertFalse(isDateBC(toDate("2008-12-30 12:34:56.789")));
        Date before = toDate("BC0001-12-31 23:59:59.999");
        Date after = toDate("0001-01-01 00:00:00.000");
        log("before time = " + before.getTime());
        log("after  time = " + after.getTime());
        assertTrue(isDateBC(before));
        assertFalse(isDateBC(after));
        assertEquals(GregorianCalendar.BC, toCalendar(before).get(Calendar.ERA));
        assertEquals(GregorianCalendar.AD, toCalendar(after).get(Calendar.ERA));
    }

    public void test_toDate_fromString_timeZone() {
        // ## Arrange ##
        String strDate = "1970-01-01 09:00:06.789";
        TimeZone timeZone = TimeZone.getTimeZone("GMT+2");

        // ## Act ##
        Date actual = toDate(strDate, timeZone); // expects 7 hour for GMT

        // ## Assert ##
        String reversed = toStringDate(actual, "yyyy/MM/dd HH:mm:ss.SSS", timeZone);
        Date gmt7hour = toDate("1970-01-01 07:00:06.789", TimeZone.getTimeZone("GMT"));
        log(reversed + ", " + actual.getTime() + ", " + gmt7hour.getTime());
        assertEquals(gmt7hour.getTime(), actual.getTime());
    }

    // -----------------------------------------------------
    //                                              Add Date
    //                                              --------
    public void test_Date_addDateYear() {
        // ## Arrange ##
        Date date = toDate("2008-12-30 12:34:56.789");

        // ## Act & Assert ##
        addDateYear(date, 1);
        assertEquals(toDate("2009-12-30 12:34:56.789"), date);
        addDateMonth(date, 1);
        assertEquals(toDate("2010-01-30 12:34:56.789"), date);
        addDateDay(date, 1);
        assertEquals(toDate("2010-01-31 12:34:56.789"), date);
        addDateHour(date, 1);
        assertEquals(toDate("2010-01-31 13:34:56.789"), date);
        addDateMinute(date, 1);
        assertEquals(toDate("2010-01-31 13:35:56.789"), date);
        addDateSecond(date, 1);
        assertEquals(toDate("2010-01-31 13:35:57.789"), date);
        addDateMillisecond(date, 1);
        assertEquals(toDate("2010-01-31 13:35:57.790"), date);
        addDateWeekOfMonth(date, 1);
        assertEquals(toDate("2010-02-07 13:35:57.790"), date);
    }

    // -----------------------------------------------------
    //                                          Move-to Date
    //                                          ------------
    // here is only for basic tests, detail tests are implemented HandyDateTest
    public void test_Date_moveToDateYearJust() {
        // ## Arrange ##
        DateFormat df = createDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        Date date = toDate("2008-12-30 12:34:56.789");

        // ## Act ##
        DfTypeUtil.moveToDateYearJust(date);

        // ## Assert ##
        assertEquals("2008/01/01 00:00:00.000", df.format(date));
    }

    public void test_Date_moveToDateYearTerminal() {
        // ## Arrange ##
        DateFormat df = createDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        Date date = toDate("2008-12-30 12:34:56.789");

        // ## Act ##
        DfTypeUtil.moveToDateYearTerminal(date);

        // ## Assert ##
        assertEquals("2008/12/31 23:59:59.999", df.format(date));
    }

    public void test_Date_moveToDateMonthJust() {
        // ## Arrange ##
        DateFormat df = createDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        Date date = toDate("2008-12-30 12:34:56.789");

        // ## Act ##
        DfTypeUtil.moveToDateMonthJust(date);

        // ## Assert ##
        assertEquals("2008/12/01 00:00:00.000", df.format(date));
    }

    public void test_Date_moveToDateMonthTerminal() {
        // ## Arrange ##
        DateFormat df = createDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        Date date = toDate("2008-02-06 12:34:56.789");

        // ## Act ##
        DfTypeUtil.moveToDateMonthTerminal(date);

        // ## Assert ##
        assertEquals("2008/02/29 23:59:59.999", df.format(date));
    }

    public void test_Date_moveToDateDayJust() {
        // ## Arrange ##
        DateFormat df = DfTypeUtil.createDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        Date date = DfTypeUtil.toDate("2008-12-30 12:34:56.789");

        // ## Act ##
        DfTypeUtil.moveToDateDayJust(date);

        // ## Assert ##
        assertEquals("2008/12/30 00:00:00.000", df.format(date));
    }

    public void test_Date_moveToDateDayTerminal() {
        // ## Arrange ##
        DateFormat df = DfTypeUtil.createDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        Date date = DfTypeUtil.toDate("2008-02-06 12:34:56.789");

        // ## Act ##
        DfTypeUtil.moveToDateDayTerminal(date);

        // ## Assert ##
        assertEquals("2008/02/06 23:59:59.999", df.format(date));
    }

    public void test_Date_moveToDateHourJust() {
        // ## Arrange ##
        DateFormat df = DfTypeUtil.createDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        Date date = DfTypeUtil.toDate("2008-12-30 12:34:56.789");

        // ## Act ##
        DfTypeUtil.moveToDateHourJust(date);

        // ## Assert ##
        assertEquals("2008/12/30 12:00:00.000", df.format(date));
    }

    public void test_Date_moveToDateHourTerminal() {
        // ## Arrange ##
        DateFormat df = DfTypeUtil.createDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        Date date = DfTypeUtil.toDate("2008-02-06 12:34:56.789");

        // ## Act ##
        DfTypeUtil.moveToDateHourTerminal(date);

        // ## Assert ##
        assertEquals("2008/02/06 12:59:59.999", df.format(date));
    }

    public void test_Date_moveToDateWeekJust_basic() {
        // ## Arrange ##
        DateFormat df = DfTypeUtil.createDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        Date date = DfTypeUtil.toDate("2011-11-17 12:34:56.789"); // Thursday

        // ## Act ##
        DfTypeUtil.moveToDateWeekJust(date, Calendar.SUNDAY);

        // ## Assert ##
        assertEquals("2011/11/13 00:00:00.000", df.format(date));
    }

    public void test_Date_moveToDateWeekJust_just_day() {
        // ## Arrange ##
        DateFormat df = DfTypeUtil.createDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        Date date = DfTypeUtil.toDate("2011-11-17 12:34:56.789"); // Thursday

        // ## Act ##
        DfTypeUtil.moveToDateWeekJust(date, Calendar.THURSDAY);

        // ## Assert ##
        assertEquals("2011/11/17 00:00:00.000", df.format(date));
    }

    public void test_Date_moveToDateWeekJust_Friday() {
        // ## Arrange ##
        DateFormat df = DfTypeUtil.createDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        Date date = DfTypeUtil.toDate("2011-11-20 12:34:56.789"); // Sunday

        // ## Act ##
        DfTypeUtil.moveToDateWeekJust(date, Calendar.FRIDAY);

        // ## Assert ##
        assertEquals("2011/11/18 00:00:00.000", df.format(date));
    }

    public void test_Date_moveToDateWeekTerminal_basic() {
        // ## Arrange ##
        DateFormat df = DfTypeUtil.createDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        Date date = DfTypeUtil.toDate("2011-11-17 12:34:56.789"); // Thursday

        // ## Act ##
        DfTypeUtil.moveToDateWeekTerminal(date, Calendar.SUNDAY);

        // ## Assert ##
        assertEquals("2011/11/19 23:59:59.999", df.format(date));
    }

    public void test_Date_moveToDateWeekTerminal_just_day() {
        // ## Arrange ##
        DateFormat df = DfTypeUtil.createDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        Date date = DfTypeUtil.toDate("2011-11-17 12:34:56.789"); // Thursday

        // ## Act ##
        DfTypeUtil.moveToDateWeekTerminal(date, Calendar.THURSDAY);

        // ## Assert ##
        assertEquals("2011/11/23 23:59:59.999", df.format(date));
    }

    public void test_Date_moveToDateWeekTerminal_Friday() {
        // ## Arrange ##
        DateFormat df = DfTypeUtil.createDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        Date date = DfTypeUtil.toDate("2011-11-20 12:34:56.789"); // Sunday

        // ## Act ##
        DfTypeUtil.moveToDateWeekTerminal(date, Calendar.FRIDAY);

        // ## Assert ##
        assertEquals("2011/11/24 23:59:59.999", df.format(date));
    }

    public void test_Date_moveToDateQuarterOfYearJust_basic() {
        // ## Arrange ##
        DateFormat df = DfTypeUtil.createDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        Date date = DfTypeUtil.toDate("2011-11-20 12:34:56.789");

        // ## Act ##
        DfTypeUtil.moveToDateQuarterOfYearJust(date);

        // ## Assert ##
        assertEquals("2011/10/01 00:00:00.000", df.format(date));
    }

    public void test_Date_moveToDateQuarterOfYearJust_over() {
        // ## Arrange ##
        DateFormat df = DfTypeUtil.createDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        Date date = DfTypeUtil.toDate("2012-01-20 12:34:56.789");

        // ## Act ##
        DfTypeUtil.moveToDateQuarterOfYearJust(date, 2);

        // ## Assert ##
        assertEquals("2011/11/01 00:00:00.000", df.format(date));
    }

    public void test_Date_moveToDateQuarterOfYearTerminal_basic() {
        // ## Arrange ##
        DateFormat df = DfTypeUtil.createDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        Date date = DfTypeUtil.toDate("2011-11-20 12:34:56.789");

        // ## Act ##
        DfTypeUtil.moveToDateQuarterOfYearTerminal(date);

        // ## Assert ##
        assertEquals("2011/12/31 23:59:59.999", df.format(date));
    }

    public void test_Date_moveToDateQuarterOfYearTerminal_over() {
        // ## Arrange ##
        DateFormat df = DfTypeUtil.createDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        Date date = DfTypeUtil.toDate("2012-01-20 12:34:56.789");

        // ## Act ##
        DfTypeUtil.moveToDateQuarterOfYearTerminal(date, 2);

        // ## Assert ##
        assertEquals("2012/01/31 23:59:59.999", df.format(date));
    }

    // -----------------------------------------------------
    //                                            Clear Date
    //                                            ----------
    public void test_clearDateTimeParts() {
        // ## Arrange ##
        DateFormat df = DfTypeUtil.createDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        Date date = DfTypeUtil.toDate("2008-12-30 12:34:56.789");

        // ## Act ##
        DfTypeUtil.clearDateTimeParts(date);

        // ## Assert ##
        assertEquals("2008/12/30 00:00:00.000", df.format(date));
    }

    public void test_clearDateMillisecond() {
        // ## Arrange ##
        DateFormat df = DfTypeUtil.createDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        Date date = DfTypeUtil.toDate("2008-12-30 12:34:56.789");

        // ## Act ##
        DfTypeUtil.clearDateMillisecond(date);

        // ## Assert ##
        assertEquals("2008/12/30 12:34:56.000", df.format(date));
    }

    // -----------------------------------------------------
    //                                             Timestamp
    //                                             ---------
    public void test_1mp_various() {
        // ## Arrange ##
        DateFormat df = DfTypeUtil.createDateFormat("yyyy/MM/dd HH:mm:ss.SSS");

        // ## Act & Assert ##
        assertNull(DfTypeUtil.toTimestamp(null));
        assertNull(DfTypeUtil.toTimestamp(""));
        assertEquals("0002/01/12 00:00:00.000", df.format(toTimestamp("date 20112")));
        assertEquals("0012/01/22 00:00:00.000", df.format(toTimestamp("date 120122")));
        assertEquals("0923/01/27 00:00:00.000", df.format(toTimestamp("date 9230127")));
        assertEquals("2008/12/30 00:00:00.000", df.format(toTimestamp("date 20081230")));
        assertEquals("2008/12/30 00:00:00.000", df.format(toTimestamp("2008/12/30")));
        assertEquals("2008/12/30 12:34:56.000", df.format(toTimestamp("2008/12/30 12:34:56")));
        assertEquals("2008/12/30 12:34:56.789", df.format(toTimestamp("2008/12/30 12:34:56.789")));
        assertEquals("2008/12/30 00:00:00.000", df.format(toTimestamp("2008-12-30")));
        assertEquals("2008/12/30 12:34:56.000", df.format(toTimestamp("2008-12-30 12:34:56")));
        assertEquals("2008/12/30 12:34:56.789", df.format(toTimestamp("2008-12-30 12:34:56.789")));
        assertEquals("2008/09/30 12:34:56.000", df.format(toTimestamp("2008-09-30 12:34:56")));
        assertEquals("2008/09/30 12:34:56.000", df.format(toTimestamp("2008-9-30 12:34:56")));
        assertEquals("2008/09/01 12:34:56.000", df.format(toTimestamp("2008-9-1 12:34:56")));
        assertEquals("0008/09/01 12:34:56.000", df.format(toTimestamp("8-9-1 12:34:56")));
        assertEquals("2008/09/01 00:00:00.000", df.format(toTimestamp("2008-9-1")));
        assertEquals("0008/09/01 02:04:06.000", df.format(toTimestamp("8-9-1 02:04:06")));
        assertEquals("0008/09/01 02:04:06.000", df.format(toTimestamp("8-9-1 2:4:6")));
        assertEquals("2008/12/30 12:34:56.009", df.format(toTimestamp("2008-12-30 12:34:56.9")));
        assertEquals("0008/09/01 02:04:06.000", df.format(toTimestamp("AD8-9-1 02:04:06")));
        assertEquals("0008/09/01 02:04:06.000", df.format(toTimestamp("A.D.8-9-1 2:4:6")));
        assertEquals("2008/12/30 12:34:56.009", df.format(toTimestamp(" 2008-12-30 12:34:56.9 ")));
        assertEquals("2008/12/30 12:34:56.009", df.format(toTimestamp(" date 2008-12-30 12:34:56.9 ")));
        assertEquals("2008/12/30 12:34:56.009", df.format(toTimestamp(" date A.D.2008-12-30 12:34:56.9 ")));
        assertEquals("2008/12/30 12:34:56.123", df.format(toTimestamp(" date A.D.2008-12-30 12:34:56.1234")));
        assertEquals("2008/12/30 12:34:56.987", df.format(toTimestamp(" date A.D.2008-12-30 12:34:56.98765432")));
        assertNotSame(java.util.Date.class, DfTypeUtil.toTimestamp("2008-12-30 12:34:56.789").getClass());
        assertNotSame(java.sql.Date.class, DfTypeUtil.toTimestamp("2008-12-30 12:34:56.789").getClass());
        assertEquals(java.sql.Timestamp.class, DfTypeUtil.toTimestamp("2008-12-30 12:34:56.789").getClass());
    }

    public void test_toTimestamp_various_BC() {
        // ## Arrange ##
        DateFormat df = DfTypeUtil.createDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        SimpleDateFormat fullDf = new SimpleDateFormat("Gyyyy/MM/dd HH:mm:ss.SSS");

        // ## Act & Assert ##
        log(fullDf.format(toTimestamp("-2008-09-01 02:04:06.123")));
        assertTrue(DfTypeUtil.toTimestamp("-8-9-1").getTime() < GMT_AD_ORIGIN_MILLISECOND);
        assertTrue(DfTypeUtil.toTimestamp("-1-9-1").getTime() < GMT_AD_ORIGIN_MILLISECOND);
        assertTrue(DfTypeUtil.toTimestamp("-8-9-1 2:4:6").getTime() < GMT_AD_ORIGIN_MILLISECOND);
        assertTrue(DfTypeUtil.toTimestamp("date -80901").getTime() < GMT_AD_ORIGIN_MILLISECOND);
        assertTrue(DfTypeUtil.toTimestamp("-2008-09-01 02:04:06.123").getTime() < GMT_AD_ORIGIN_MILLISECOND);
        assertTrue(DfTypeUtil.toTimestamp("-2008-13-01 02:04:06").getTime() < GMT_AD_ORIGIN_MILLISECOND);
        assertTrue(DfTypeUtil.toTimestamp("BC8-9-1 2:4:6").getTime() < GMT_AD_ORIGIN_MILLISECOND);
        assertTrue(DfTypeUtil.toTimestamp("BC2008-09-01 02:04:06").getTime() < GMT_AD_ORIGIN_MILLISECOND);
        assertTrue(DfTypeUtil.toTimestamp("BC2008-13-01 02:04:06.123").getTime() < GMT_AD_ORIGIN_MILLISECOND);
        assertTrue(DfTypeUtil.toTimestamp("B.C.8-9-1 2:4:6").getTime() < GMT_AD_ORIGIN_MILLISECOND);
        assertTrue(DfTypeUtil.toTimestamp("B.C.2008-09-01 02:04:06.123").getTime() < GMT_AD_ORIGIN_MILLISECOND);
        assertTrue(DfTypeUtil.toTimestamp("B.C.2008-13-01 02:04:06").getTime() < GMT_AD_ORIGIN_MILLISECOND);
        assertEquals("2008/11/01 02:04:06.123", df.format(toTimestamp("BC2008-11-01 02:04:06.123")));
        assertEquals("2009/11/01 02:04:06.123", df.format(toTimestamp("-2008-11-01 02:04:06.123")));
        assertEquals("0009/09/01 00:00:00.000", df.format(toTimestamp("date -80901")));
        assertEquals("2008/01/01 02:04:06.123", df.format(toTimestamp("-2008-13-01 02:04:06.123"))); // no calendar check when BC
    }

    public void test_toTimestamp_long_basic() {
        // ## Arrange ##
        DateFormat dfmil = DfTypeUtil.createDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        Date date = DfTypeUtil.toTimestamp("2008-12-30 12:34:56.789");

        // ## Act ##
        Date actual = DfTypeUtil.toTimestamp(date.getTime());

        // ## Assert ##
        assertEquals(dfmil.format(date), dfmil.format(actual));
    }

    public void test_toTimestamp_illegal() {
        try {
            toTimestamp("2009-12");

            fail();
        } catch (ParseTimestampException e) {
            // OK
            log(e.getMessage());
        }
        try {
            DfTypeUtil.toTimestamp("2009");

            fail();
        } catch (ParseTimestampException e) {
            // OK
            log(e.getMessage());
        }
        try {
            DfTypeUtil.toTimestamp("date 20091");

            fail();
        } catch (ParseTimestampOutOfCalendarException e) {
            // OK
            log(e.getMessage());
        }
        try {
            DfTypeUtil.toTimestamp("2009-12-09 12:34:60");

            fail();
        } catch (ParseTimestampOutOfCalendarException e) {
            // OK
            log(e.getMessage());
        }
        try {
            DfTypeUtil.toTimestamp("AD2009-12-09 12:34:60");

            fail();
        } catch (ParseTimestampOutOfCalendarException e) {
            // OK
            log(e.getMessage());
        }
        try {
            DfTypeUtil.toTimestamp("2009-1209");

            fail();
        } catch (ParseTimestampException e) {
            // OK
            log(e.getMessage());
        }
        try {
            DfTypeUtil.toTimestamp("2009-12-0-9 12:34:56");

            fail();
        } catch (ParseTimestampNumberFormatException e) {
            // OK
            log(e.getMessage());
        }
        try {
            DfTypeUtil.toTimestamp("2009-12-a9 12:34:56");

            fail();
        } catch (ParseTimestampNumberFormatException e) {
            // OK
            log(e.getMessage());
        }
        try {
            DfTypeUtil.toTimestamp("2009-12-09 12:a4:36");

            fail();
        } catch (ParseTimestampNumberFormatException e) {
            // OK
            log(e.getMessage());
        }
        try {
            DfTypeUtil.toTimestamp("2009-12-09 12:34:36.12a");

            fail();
        } catch (ParseTimestampNumberFormatException e) {
            // OK
            log(e.getMessage());
        }
        try {
            DfTypeUtil.toTimestamp("0000-12-09 12:34:26.541");

            fail();
        } catch (ParseTimestampOutOfCalendarException e) {
            // OK
            log(e.getMessage());
        }

    }

    // -----------------------------------------------------
    //                                                  Time
    //                                                  ----
    public void test_toTime_timestamp() {
        // ## Arrange ##
        DateFormat df = DfTypeUtil.createDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        Date date = DfTypeUtil.toTimestamp("2008-12-30 12:34:56.789");

        // ## Act & Assert ##
        assertNull(toTime(null));
        assertNull(toTime(""));
        assertEquals("1970/01/01 12:34:56.789", df.format(DfTypeUtil.toTime(date)));
    }

    public void test_toTime_long_basic() {
        // ## Arrange ##
        DateFormat dfmil = DfTypeUtil.createDateFormat("HH:mm:ss.SSS");
        Date date = DfTypeUtil.toDate("2008-12-30 12:34:56.789");

        // ## Act ##
        Date actual = DfTypeUtil.toTime(date.getTime());

        // ## Assert ##
        assertEquals(dfmil.format(date), dfmil.format(actual));
    }

    public void test_toTime_various() {
        // ## Arrange ##
        DateFormat df = createDateFormat("HH:mm:ss");
        DateFormat fullDf = createDateFormat("HH:mm:ss.SSS");

        // ## Act & Assert ##
        assertNull(DfTypeUtil.toTime(null));
        assertNull(DfTypeUtil.toTime(""));
        assertEquals("12:34:56", df.format(toTime("2009/12/12 12:34:56")));
        assertEquals("12:34:56", df.format(DfTypeUtil.toTime("12:34:56")));
        assertEquals("02:04:06", df.format(DfTypeUtil.toTime("02:04:06")));
        assertEquals("02:04:06", df.format(DfTypeUtil.toTime("2:4:6")));
        assertEquals("12:34:56", df.format(DfTypeUtil.toTime("12:34:56.789")));
        assertEquals("12:34:56.000", fullDf.format(DfTypeUtil.toTime("12:34:56.789")));
    }

    // -----------------------------------------------------
    //                                              SQL Date
    //                                              --------
    public void test_toSqlDate_basic() {
        assertNull(DfTypeUtil.toSqlDate(null));
        assertNull(DfTypeUtil.toSqlDate(""));
    }

    public void test_toSqlDate_same() {
        // ## Arrange ##
        DateFormat df = createDateFormat("yyyy/MM/dd HH:mm:ss.SSS");

        // ## Act ##
        java.sql.Date date = DfTypeUtil.toSqlDate(DfTypeUtil.toDate("2008-12-30 12:34:56.789"));

        // ## Assert ##
        assertEquals("2008/12/30 00:00:00.000", df.format(date));
    }

    public void test_toSqlDate_timestamp() {
        // ## Arrange ##
        DateFormat df = createDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        Timestamp date = DfTypeUtil.toTimestamp("2008-12-30 12:34:56.789");

        // ## Act & Assert ##
        assertEquals("2008/12/30 00:00:00.000", df.format(DfTypeUtil.toSqlDate(date)));
    }

    public void test_toSqlDate_various() {
        // ## Arrange ##
        DateFormat df = createDateFormat("yyyy/MM/dd");
        DateFormat dft = createDateFormat("yyyy/MM/dd HH:mm:ss.SSS");

        // ## Act & Assert ##
        assertNull(DfTypeUtil.toSqlDate(null));
        assertNull(DfTypeUtil.toSqlDate(""));
        assertEquals("0002/01/12", df.format(DfTypeUtil.toSqlDate("date 20112")));
        assertEquals("0012/01/22", df.format(DfTypeUtil.toSqlDate("date 120122")));
        assertEquals("0923/01/27", df.format(DfTypeUtil.toSqlDate("date 9230127")));
        assertEquals("2008/12/30", df.format(DfTypeUtil.toSqlDate("date 20081230")));
        assertEquals("2008/12/30", df.format(DfTypeUtil.toSqlDate("2008/12/30")));
        assertEquals("2008/12/30", df.format(DfTypeUtil.toSqlDate("2008-12-30")));
        assertEquals("2008/12/30", df.format(DfTypeUtil.toSqlDate("2008-12-30 12:34:56")));
        assertEquals("2008/12/30", df.format(DfTypeUtil.toSqlDate("2008-12-30 12:34:56.789")));
        assertEquals("2008/09/30", df.format(toSqlDate("2008-09-30 12:34:56")));
        assertEquals("2008/09/30", df.format(DfTypeUtil.toSqlDate("2008-9-30 12:34:56")));
        assertEquals("2008/09/01", df.format(DfTypeUtil.toSqlDate("2008-9-1 12:34:56")));
        assertEquals("0008/09/01", df.format(DfTypeUtil.toSqlDate("8-9-1 12:34:56")));
        assertEquals("2008/09/01", df.format(DfTypeUtil.toSqlDate("2008-9-1")));
        assertEquals("0008/09/01 00:00:00.000", dft.format(DfTypeUtil.toSqlDate("8-9-1 12:34:56")));
        assertEquals("2008/12/30 00:00:00.000", dft.format(DfTypeUtil.toSqlDate("2008-12-30 12:34:56.789")));
        assertEquals(java.sql.Date.class, DfTypeUtil.toSqlDate("2008-12-30 12:34:56.789").getClass());
        assertNotSame(java.util.Date.class, DfTypeUtil.toSqlDate("2008-12-30 12:34:56.789").getClass());
        assertNotSame(java.sql.Timestamp.class, DfTypeUtil.toSqlDate("2008-12-30 12:34:56.789").getClass());
    }

    // -----------------------------------------------------
    //                                               Boolean
    //                                               -------
    public void test_toBoolean_basic() {
        // ## Arrange & Act & Assert ##
        assertNull(DfTypeUtil.toBoolean(null));
        assertTrue(DfTypeUtil.toBoolean("true"));
        assertFalse(DfTypeUtil.toBoolean("false"));
    }

    // -----------------------------------------------------
    //                                                Binary
    //                                                ------
    public void test_toBinary_basic() {
        // ## Arrange & Act & Assert ##
        assertNull(DfTypeUtil.toBinary(null));
        assertNotNull(toBinary(""));
    }

    public void test_toBinary_byteArray() throws UnsupportedEncodingException {
        // ## Arrange ##
        final byte[] bytes = "foo".getBytes("UTF-8");

        // ## Act & Assert ##
        assertEquals(bytes.length, toBinary(bytes).length);
        assertTrue(bytes instanceof Serializable); // confirmation
    }

    public void test_toBinary_serializable() {
        // ## Arrange ##
        Date expected = DfTypeUtil.toDate("2010-03-11");
        String pt = "yyyy-MM-dd";

        // ## Act ##
        byte[] binary = DfTypeUtil.toBinary(expected);

        // ## Assert ##
        Date actual = DfTypeUtil.toDate(binary);
        log(DfTypeUtil.toString(actual, "yyyy-MM-dd"));
        assertEquals(DfTypeUtil.toString(expected, pt), DfTypeUtil.toString(actual, pt));
    }

    // ===================================================================================
    //                                                                              Format
    //                                                                              ======
    public void test_format_Date() {
        // ## Arrange ##
        Date date = DfTypeUtil.toDate("2008/12/30 12:34:56");
        Timestamp timestamp = DfTypeUtil.toTimestamp("2008/12/30 12:34:56");

        // ## Act & Assert ##
        assertNull(DfTypeUtil.toString((Date) null, "yyyy/MM/dd HH:mm:ss"));
        assertEquals("2008/12/30 12:34:56", DfTypeUtil.toString(date, "yyyy/MM/dd HH:mm:ss"));
        assertEquals("2008/12/30", DfTypeUtil.toString(date, "yyyy/MM/dd"));
        assertEquals("2008-12-30", DfTypeUtil.toString(date, "yyyy-MM-dd"));
        assertEquals("2008-12-30 12:34:56.000", DfTypeUtil.toString(date, "yyyy-MM-dd HH:mm:ss.SSS"));
        assertEquals("2008/12/30 12:34:56", DfTypeUtil.toString(timestamp, "yyyy/MM/dd HH:mm:ss"));
        assertEquals("2008/12/30", DfTypeUtil.toString(timestamp, "yyyy/MM/dd"));
        assertEquals("2008-12-30", DfTypeUtil.toString(timestamp, "yyyy-MM-dd"));
        assertEquals("2008-12-30 12:34:56.000", DfTypeUtil.toString(timestamp, "yyyy-MM-dd HH:mm:ss.SSS"));
    }

    // ===================================================================================
    //                                                                              Format
    //                                                                              ======
    protected void log(Object msg) {
        _logger.log(getClass().getName(), Level.DEBUG, msg, null);
    }
}
