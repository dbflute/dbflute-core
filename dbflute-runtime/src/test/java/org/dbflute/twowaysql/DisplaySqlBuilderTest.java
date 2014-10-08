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
package org.dbflute.twowaysql;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.TimeZone;

import org.dbflute.twowaysql.DisplaySqlBuilder.DateFormatResource;
import org.dbflute.twowaysql.style.BoundDateDisplayStyle;
import org.dbflute.twowaysql.style.BoundDateDisplayTimeZoneProvider;
import org.dbflute.unit.RuntimeTestCase;
import org.dbflute.util.DfTypeUtil;
import org.dbflute.util.Srl;

/**
 * 
 * @author jflute
 * @since 0.9.6 (2009/10/27 Tuesday)
 */
public class DisplaySqlBuilderTest extends RuntimeTestCase {

    // ===================================================================================
    //                                                                               Basic
    //                                                                               =====
    public void test_buildDisplaySql_basic() {
        // ## Arrange ##
        String sql = "select * from where FOO_CODE = ?";
        String fooCode = "qux";

        // ## Act ##
        String actual = createTarget().buildDisplaySql(sql, new Object[] { fooCode });

        // ## Assert ##
        log(actual);
        assertTrue(actual.contains("FOO_CODE = 'qux'"));
    }

    // ===================================================================================
    //                                                                          Date Style
    //                                                                          ==========
    public void test_buildDisplaySql_date_default() {
        // ## Arrange ##
        String sql = "select * from where FOO_DATE = ?";
        Date fooDate = DfTypeUtil.toDate("2010/12/12 12:34:56");

        // ## Act ##
        String actual = createTarget().buildDisplaySql(sql, new Object[] { fooDate });

        // ## Assert ##
        log(actual);
        assertTrue(actual.contains("FOO_DATE = '2010-12-12'"));
    }

    public void test_buildDisplaySql_date_default_bc_basic() {
        // ## Arrange ##
        String sql = "select * from where FOO_DATE = ?";
        Date fooDate = DfTypeUtil.toDate("BC1010/12/12 12:34:56");

        // ## Act ##
        String actual = createTarget().buildDisplaySql(sql, new Object[] { fooDate });

        // ## Assert ##
        log(actual);
        assertTrue(actual.contains("FOO_DATE = 'BC1010-12-12'"));
    }

    public void test_buildDisplaySql_date_default_bc_limit() {
        // ## Arrange ##
        String sql = "select * from where FOO_DATE = ?";
        Date fooDate = DfTypeUtil.toDate("BC0001/12/12 12:34:56");
        log(DfTypeUtil.toString(fooDate, "Gyyyy/MM/dd"));

        // ## Act ##
        String actual = createTarget().buildDisplaySql(sql, new Object[] { fooDate });

        // ## Assert ##
        log(actual);
        assertTrue(actual.contains("FOO_DATE = 'BC0001-12-12'"));
    }

    public void test_buildDisplaySql_date_format() {
        // ## Arrange ##
        String sql = "select * from where FOO_DATE = ?";
        Date fooDate = DfTypeUtil.toDate("2010/12/12 12:34:56");

        // ## Act ##
        String actual = createTargetWithDatePattern("yyyy@MM@dd").buildDisplaySql(sql, new Object[] { fooDate });

        // ## Assert ##
        log(actual);
        assertTrue(actual.contains("FOO_DATE = '2010@12@12'"));
    }

    public void test_buildDisplaySql_timestamp_basic() {
        // ## Arrange ##
        String sql = "select * from where FOO_DATE = ?";
        Timestamp fooDate = DfTypeUtil.toTimestamp("2010/12/12 12:34:56");
        DisplaySqlBuilder builder = createTarget();

        // ## Act ##
        String actual = builder.buildDisplaySql(sql, new Object[] { fooDate });

        // ## Assert ##
        log(actual);
        assertTrue(actual.contains("FOO_DATE = '2010-12-12 12:34:56.000'"));
    }

    public void test_buildDisplaySql_timestamp_specified() {
        // ## Arrange ##
        String sql = "select * from where FOO_DATE = ?";
        Timestamp fooDate = DfTypeUtil.toTimestamp("2010/12/12 12:34:56");
        DisplaySqlBuilder builder = createTargetWithTimestampPattern("yyyy@MM+dd HH_mm_ss.SSS");

        // ## Act ##
        String actual = builder.buildDisplaySql(sql, new Object[] { fooDate });

        // ## Assert ##
        log(actual);
        assertTrue(actual.contains("FOO_DATE = '2010@12+12 12_34_56.000'"));
    }

    public void test_buildDisplaySql_timestamp_timeZone() {
        // ## Arrange ##
        String sql = "select * from where FOO_DATE = ?";
        Timestamp fooDate = DfTypeUtil.toTimestamp("2010/12/12 12:34:56");
        DisplaySqlBuilder builder = createTargetWithTimeZone(() -> TimeZone.getTimeZone("GMT+3"));

        // ## Act ##
        String actual = builder.buildDisplaySql(sql, new Object[] { fooDate });

        // ## Assert ##
        log(actual);
        assertTrue(actual.contains("FOO_DATE = '2010-12-12 06:34:56.000'"));
    }

    public void test_buildDisplaySql_time_basic() {
        // ## Arrange ##
        String sql = "select * from where FOO_DATE = ?";
        Time fooDate = DfTypeUtil.toTime("2010/12/12 12:34:56");
        DisplaySqlBuilder builder = createTarget();

        // ## Act ##
        String actual = builder.buildDisplaySql(sql, new Object[] { fooDate });

        // ## Assert ##
        log(actual);
        assertTrue(actual.contains("FOO_DATE = '12:34:56'"));
    }

    public void test_buildDisplaySql_time_specified() {
        // ## Arrange ##
        String sql = "select * from where FOO_DATE = ?";
        Time fooDate = DfTypeUtil.toTime("2010/12/12 12:34:56");
        DisplaySqlBuilder builder = createTargetWithTimePattern("HH@mm@ss");

        // ## Act ##
        String actual = builder.buildDisplaySql(sql, new Object[] { fooDate });

        // ## Assert ##
        log(actual);
        assertTrue(actual.contains("FOO_DATE = '12@34@56'"));
    }

    // ===================================================================================
    //                                                                             Symbols
    //                                                                             =======
    public void test_buildDisplaySql_questionInComment_quotationInComment() {
        // ## Arrange ##
        String sql = "/*foo's bar*/select * from where FOO_NAME/*qux?*/ like ? escape '|' and ? /*quux'?*/and ?";
        String fooName = "fooName%";
        String barCode = "barCode";
        Integer bazId = 3;

        // ## Act ##
        String actual = createTarget().buildDisplaySql(sql, new Object[] { fooName, barCode, bazId });

        // ## Assert ##
        log(actual);
        assertTrue(Srl.containsAll(actual, fooName, barCode, String.valueOf(bazId), "/*foo's bar*/", "escape '|'",
                "/*qux?*/", "/*quux'?*/and"));
    }

    public void test_buildDisplaySql_beforeComment_quotationOverComment() {
        // ## Arrange ##
        String sql = "' ?/*foo's bar*/select * from where FOO_NAME like ? escape '|'";
        String fooName = "fooName%";
        String barCode = "barCode";

        // ## Act ##
        String actual = createTarget().buildDisplaySql(sql, new Object[] { fooName, barCode });

        // ## Assert ##
        log(actual);
        assertTrue(Srl.containsAll(actual, fooName, barCode, "/*foo's bar*/", "escape '|'"));
    }

    // ===================================================================================
    //                                                                        BindVariable
    //                                                                        ============
    public void test_getBindVariableText_dateFormat_basic() {
        // ## Arrange ##
        Date date = DfTypeUtil.toDate("2009-10-27");

        // ## Act ##
        String actual = createTarget().getBindVariableText(date);

        // ## Assert ##
        assertEquals("'2009-10-27'", actual);
    }

    public void test_getBindVariableText_dateFormat_custom() {
        // ## Arrange ##
        String format = "date $df:{yyyy-MM-dd}";
        Date date = DfTypeUtil.toDate("2009-10-27");

        // ## Act ##
        String actual = createTargetWithDatePattern(format).getBindVariableText(date);

        // ## Assert ##
        assertEquals("date '2009-10-27'", actual);
    }

    public void test_getBindVariableText_timestampFormat_basic() {
        // ## Arrange ##
        String format = "yyyy-MM-dd HH:mm:ss.SSS";
        Timestamp timestamp = DfTypeUtil.toTimestamp("2009-10-27 16:22:23.123");

        // ## Act ##
        String actual = createTargetWithTimestampPattern(format).getBindVariableText(timestamp);

        // ## Assert ##
        assertEquals("'2009-10-27 16:22:23.123'", actual);
    }

    public void test_getBindVariableText_timestampFormat_custom() {
        // ## Arrange ##
        String format = "timestamp $df:{yyyy-MM-dd HH:mm:ss.SSS}";
        Timestamp timestamp = DfTypeUtil.toTimestamp("2009-10-27 16:22:23.123");

        // ## Act ##
        String actual = createTargetWithTimestampPattern(format).getBindVariableText(timestamp);

        // ## Assert ##
        assertEquals("timestamp '2009-10-27 16:22:23.123'", actual);
    }

    // ===================================================================================
    //                                                                  Analyze DateFormat
    //                                                                  ==================
    public void test_analyzeDateFormat_basic() {
        // ## Arrange ##
        String format = "yyyy-MM-dd";

        // ## Act ##
        DateFormatResource resource = createTarget().analyzeDateFormat(format);

        // ## Assert ##
        assertEquals(format, resource.getFormat());
        assertNull(resource.getPrefix());
        assertNull(resource.getSuffix());
    }

    public void test_analyzeDateFormat_markOnly() {
        // ## Arrange ##
        String format = "$df:{yyyy-MM-dd}";

        // ## Act ##
        DateFormatResource resource = createTarget().analyzeDateFormat(format);

        // ## Assert ##
        assertEquals("yyyy-MM-dd", resource.getFormat());
        assertEquals("", resource.getPrefix());
        assertEquals("", resource.getSuffix());
    }

    public void test_analyzeDateFormat_prefixOnly() {
        // ## Arrange ##
        String format = "date $df:{yyyy-MM-dd}";

        // ## Act ##
        DateFormatResource resource = createTarget().analyzeDateFormat(format);

        // ## Assert ##
        assertEquals("yyyy-MM-dd", resource.getFormat());
        assertEquals("date ", resource.getPrefix());
        assertEquals("", resource.getSuffix());
    }

    public void test_analyzeDateFormat_suffixOnly() {
        // ## Arrange ##
        String format = "$df:{yyyy-MM-dd}sufsuf";

        // ## Act ##
        DateFormatResource resource = createTarget().analyzeDateFormat(format);

        // ## Assert ##
        assertEquals("yyyy-MM-dd", resource.getFormat());
        assertEquals("", resource.getPrefix());
        assertEquals("sufsuf", resource.getSuffix());
    }

    public void test_analyzeDateFormat_prefixSuffix() {
        // ## Arrange ##
        String format = "FOO($df:{yyyy-MM-dd}, 'BAR')";

        // ## Act ##
        DateFormatResource resource = createTarget().analyzeDateFormat(format);

        // ## Assert ##
        assertEquals("yyyy-MM-dd", resource.getFormat());
        assertEquals("FOO(", resource.getPrefix());
        assertEquals(", 'BAR')", resource.getSuffix());
    }

    // ===================================================================================
    //                                                                        Quote Helper
    //                                                                        ============
    public void test_quote_basic() {
        assertEquals("'foo'", createTarget().quote("foo"));
    }

    public void test_quote_with_DateFormatResource() {
        // ## Arrange ##
        DateFormatResource resource = new DateFormatResource();
        resource.setPrefix("prepre");
        resource.setSuffix("sufsuf");

        // ## Act & Assert ##
        assertEquals("prepre'foo'sufsuf", createTarget().quote("foo", resource));
    }

    // ===================================================================================
    //                                                                         Test Helper
    //                                                                         ===========
    protected DisplaySqlBuilder createTarget() {
        return doCreateTarget(null, null, null, null);
    }

    protected DisplaySqlBuilder createTargetWithDatePattern(String datePattern) {
        return doCreateTarget(datePattern, null, null, null);
    }

    protected DisplaySqlBuilder createTargetWithTimestampPattern(String timestampPattern) {
        return doCreateTarget(null, timestampPattern, null, null);
    }

    protected DisplaySqlBuilder createTargetWithTimePattern(String timePattern) {
        return doCreateTarget(null, null, timePattern, null);
    }

    protected DisplaySqlBuilder createTargetWithTimeZone(BoundDateDisplayTimeZoneProvider timeZoneProvider) {
        return doCreateTarget(null, null, null, timeZoneProvider);
    }

    protected DisplaySqlBuilder doCreateTarget(String datePattern, String timestampPattern, String timePattern,
            BoundDateDisplayTimeZoneProvider timeZoneProvider) {
        BoundDateDisplayStyle dateDisplayStyle = new BoundDateDisplayStyle(datePattern, timestampPattern, timePattern,
                timeZoneProvider);
        return new DisplaySqlBuilder(dateDisplayStyle);
    }
}
