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

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.seasar.dbflute.cbean.chelper.HpSpecifiedColumn;
import org.seasar.dbflute.cbean.cipher.GearedCipherManager;
import org.seasar.dbflute.cbean.ckey.ConditionKey;
import org.seasar.dbflute.cbean.sqlclause.query.QueryClauseArranger;
import org.seasar.dbflute.dbway.ExtensionOperand;
import org.seasar.dbflute.dbway.StringConnector;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.DfTypeUtil;

/**
 * The option of from-to scope for Date type.
 * <pre>
 * e.g. from:{2007/04/10 08:24:53} to:{2007/04/16 14:36:29}
 * 
 * [Comparison Pattern]
 *   new FromToOption().compareAsYear();
 *     --&gt; column &gt;= '2007/01/01 00:00:00'
 *     and column &lt; '2008/01/01 00:00:00'
 * 
 *   new FromToOption().compareAsMonth();
 *     --&gt; column &gt;= '2007/04/01 00:00:00'
 *     and column &lt; '2007/05/01 00:00:00'
 * 
 *   new FromToOption().compareAsDate();
 *     --&gt; column &gt;= '2007/04/10 00:00:00'
 *     and column &lt; '2007/04/17 00:00:00'
 * 
 *   new FromToOption().compareAsHour();
 *     --&gt; column &gt;= '2007/04/10 08:00:00'
 *     and column &lt; '2007/04/16 15:00:00'
 * 
 *   new FromToOption().compareAsWeek().beginWeek_DayOfWeek1st_Sunday(); 
 *     --&gt; column &gt;= '2007/04/08 00:00:00'
 *     and column &lt; '2008/04/22 00:00:00'
 * 
 *   new FromToOption().compareAsQuarterOfYear(); 
 *     --&gt; column &gt;= '2007/04/01 00:00:00'
 *     and column &lt; '2007/07/01 00:00:00'
 * 
 * [Manual Adjustment]
 *   new FromToOption().greaterThan(); 
 *     --&gt; column &gt; '2007/04/10 08:24:53'
 *     and column &lt;= '2007/04/16 14:36:29'
 * 
 *   new FromToOption().lessThan(); 
 *     --&gt; column &gt;= '2007/04/10 08:24:53'
 *     and column &lt; '2007/04/16 14:36:29'
 * 
 *   new FromToOption().greaterThan().lessThan(); 
 *     --&gt; column &gt; '2007/04/10 08:24:53'
 *     and column &lt; '2007/04/16 14:36:29'
 * 
 *   and so on...
 * 
 * [Default]
 *   new FromToOption(); 
 *     --&gt; column &gt;= '2007/04/10 08:24:53'
 *     and column &lt;= '2007/04/16 14:36:29'
 * </pre>
 * @author jflute
 */
public class FromToOption implements ConditionOption {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected boolean _greaterThan;
    protected boolean _lessThan;

    protected boolean _fromPatternYearJust;
    protected boolean _fromPatternMonthJust;
    protected boolean _fromPatternDayJust;
    protected boolean _fromPatternHourJust;
    protected boolean _fromPatternWeekJust;
    protected boolean _fromPatternQuarterOfYearJust;
    protected boolean _fromDateWithNoon;
    protected Integer _fromDateWithHour;

    protected boolean _toPatternNextYearJust;
    protected boolean _toPatternNextMonthJust;
    protected boolean _toPatternNextDayJust;
    protected boolean _toPatternNextHourJust;
    protected boolean _toPatternNextWeekJust;
    protected boolean _toPatternNextQuarterOfYearJust;
    protected boolean _toDateWithNoon;
    protected Integer _toDateWithHour;

    protected Integer _yearBeginMonth = 1; // as default
    protected Integer _monthBeginDay = 1; // as default
    protected Integer _dayBeginHour = 0; // as default
    protected Integer _weekBeginDay = Calendar.SUNDAY; // as default
    protected Integer _moveToScope;
    protected boolean _usePattern;
    protected boolean _orIsNull;
    protected boolean _oneSideAllowed;

    // ===================================================================================
    //                                                                  Comparison Pattern
    //                                                                  ==================
    /**
     * Compare as year. <br />
     * The year part of the date is only used.
     * This method ignores operand adjustments and other patterns.
     * <pre>
     * e.g. from:{<span style="color: #DD4747">2007</span>/04/10 08:24:53} to:{<span style="color: #DD4747">2008</span>/08/16 14:36:29}
     * 
     *   new FromToOption().compareAsYear();
     *     --&gt; column &gt;= '2007/01/01 00:00:00'
     *     and column &lt; '2009/01/01 00:00:00'
     * 
     *   new FromToOption().compareAsYear().beginYear_Month(4);
     *     --&gt; column &gt;= '2007/04/01 00:00:00'
     *     and column &lt; '2009/04/01 00:00:00'
     * </pre>
     * @return this. (NotNull)
     */
    public FromToOption compareAsYear() {
        fromPatternYearJust();
        toPatternNextYearJust();
        clearOperand();
        lessThan();
        _usePattern = true;
        return this;
    }

    /**
     * Compare as month. <br />
     * The year and month parts of the date are only used. <br />
     * This method ignores operand adjustments and other patterns.
     * <pre>
     * e.g. from:{<span style="color: #DD4747">2007/04</span>/10 08:24:53} to:{<span style="color: #DD4747">2008/08</span>/16 14:36:29}
     * 
     *   new FromToOption().compareAsMonth();
     *     --&gt; column &gt;= '2007/04/01 00:00:00'
     *     and column &lt; '2008/09/01 00:00:00'
     * 
     *   new FromToOption().compareAsMonth().beginMonth_Day(3);
     *     --&gt; column &gt;= '2007/04/03 00:00:00'
     *     and column &lt; '2008/09/03 00:00:00'
     * </pre>
     * @return this. (NotNull)
     */
    public FromToOption compareAsMonth() {
        fromPatternMonthJust();
        toPatternNextMonthJust();
        clearOperand();
        lessThan();
        _usePattern = true;
        return this;
    }

    /**
     * Compare as date. <br />
     * The year, month, day parts of the date are only used. <br />
     * This method ignores operand adjustments and other patterns.
     * <pre>
     * e.g. from:{<span style="color: #DD4747">2007/04/10</span> 08:24:53} to:{<span style="color: #DD4747">2007/04/16</span> 14:36:29}
     * 
     *   new FromToOption().compareAsDate();
     *     --&gt; column &gt;= '2007/04/10 00:00:00'
     *     and column &lt; '2007/04/17 00:00:00'
     * 
     *   new FromToOption().compareAsDate().beginDay_Hour(6);
     *     --&gt; column &gt;= '2007/04/10 06:00:00'
     *     and column &lt; '2007/04/17 06:00:00'
     * </pre>
     * @return this. (NotNull)
     */
    public FromToOption compareAsDate() {
        fromPatternDayJust();
        toPatternNextDayJust();
        clearOperand();
        lessThan();
        _usePattern = true;
        return this;
    }

    /**
     * Compare as hour. <br />
     * The year, month, day, hour parts of the date are only used. <br />
     * This method ignores operand adjustments and other patterns.
     * <pre>
     * e.g. from:{<span style="color: #DD4747">2007/04/10 08</span>:24:53} to:{<span style="color: #DD4747">2007/04/16 14</span>:36:29}
     * 
     *   new FromToOption().compareAsHour();
     *     --&gt; column &gt;= '2007/04/10 08:00:00'
     *     and column &lt; '2007/04/16 15:00:00'
     * </pre>
     * @return this. (NotNull)
     */
    public FromToOption compareAsHour() {
        fromPatternHourJust();
        toPatternNextHourJust();
        clearOperand();
        lessThan();
        _usePattern = true;
        return this;
    }

    /**
     * Compare as week. <br />
     * The year, month, day parts of the date are only used. <br />
     * This method ignores operand adjustments and other patterns. <br />
     * The default beginning day of week is Sunday, but you can change it by beginWeek_DayOfWeek...() methods.
     * <pre>
     * e.g. from:{<span style="color: #DD4747">2007/04/10</span> 08:24:53} to:{<span style="color: #DD4747">2007/04/16</span> 14:36:29}
     * 
     *   new FromToOption().compareAsWeek().beginWeek_DayOfWeek1st_Sunday();
     *     --&gt; column &gt;= '2007/04/08 00:00:00'
     *     and column &lt; '2007/04/22 00:00:00'
     * </pre>
     * @return this. (NotNull)
     */
    public FromToOption compareAsWeek() {
        fromPatternWeekJust();
        toPatternNextWeekJust();
        clearOperand();
        lessThan();
        _usePattern = true;
        return this;
    }

    /**
     * Compare as quarter of year. <br />
     * The year and month parts of the date are only used. <br />
     * This method ignores operand adjustments and other patterns. <br />
     * The default beginning of quarter of year is 1st month, but you can change it by beginYear_Month...() methods.
     * <pre>
     * e.g. from:{<span style="color: #DD4747">2007/04</span>/10 08:24:53} to:{<span style="color: #DD4747">2008/08</span>/16 14:36:29}
     * 
     *   new FromToOption().compareAsQuarterOfYear();
     *     --&gt; column &gt;= '2007/04/01 00:00:00'
     *     and column &lt; '2008/10/01 00:00:00'
     * </pre>
     * @return this. (NotNull)
     */
    public FromToOption compareAsQuarterOfYear() {
        fromPatternQuarterOfYearJust();
        toPatternNextQuarterOfYearJust();
        clearOperand();
        lessThan();
        _usePattern = true;
        return this;
    }

    // -----------------------------------------------------
    //                                            Begin Year
    //                                            ----------
    /**
     * Begin year from the specified month. <br />
     * The date of argument is used as only the month part.
     * <pre>
     * e.g. beginYear_Month(toDate("2001/04/01"))
     *  year is from 4th month to 3rd month of next year
     *  (the 2011 year means 2011/04/01 to 2012/03/31)
     * 
     * e.g. option.compareAsYear().beginYear_Month(toDate("2001/04/01"))
     *  if from-date is 2011/01/01 and to-date is 2012/01/01 (means 2011, 2012 year are target),
     *  the condition is: greater-equal 2011/04/01 and less-than 2013/04/04
     * </pre>
     * @param yearBeginMonth The date that has the month of year-begin. (NotNull)
     * @return this. (NotNull)
     */
    public FromToOption beginYear_Month(Date yearBeginMonth) {
        assertPatternOptionValid("beginYear_Month");
        assertArgumentNotNull("yearBeginMonth", yearBeginMonth);
        final Calendar cal = Calendar.getInstance();
        cal.setTime(yearBeginMonth);
        _yearBeginMonth = cal.get(Calendar.MONTH) + 1; // zero origin headache
        return this;
    }

    /**
     * Begin year from the specified month.
     * <pre>
     * e.g. beginYear_Month(4)
     *  year is from 4th month to 3rd month of next year
     *  (the 2011 year means 2011/04/01 to 2012/03/31)
     * 
     * e.g. option.compareAsYear().beginYear_Month(4)
     *  if from-date is 2011/01/01 and to-date is 2012/01/01 (means 2011, 2012 year are target),
     *  the condition is: greater-equal 2011/04/01 and less-than 2013/04/04
     * </pre>
     * @param yearBeginMonth The month for year-begin. (NotMinus, 1-12)
     * @return this. (NotNull)
     */
    public FromToOption beginYear_Month(int yearBeginMonth) {
        assertPatternOptionValid("beginYear_Month");
        assertNotMinusNotOver("yearBeginMonth", yearBeginMonth, 12);
        _yearBeginMonth = yearBeginMonth;
        return this;
    }

    /**
     * Begin year from January (1st month).
     * @return this. (NotNull)
     */
    public FromToOption beginYear_Month01_January() {
        assertPatternOptionValid("beginYear_Month01_January");
        _yearBeginMonth = 1;
        return this;
    }

    /**
     * Begin year from February (2nd month).
     * @return this. (NotNull)
     */
    public FromToOption beginYear_Month02_February() {
        assertPatternOptionValid("beginYear_Month02_February");
        _yearBeginMonth = 2;
        return this;
    }

    /**
     * Begin year from March (3rd month).
     * @return this. (NotNull)
     */
    public FromToOption beginYear_Month03_March() {
        assertPatternOptionValid("beginYear_Month03_March");
        _yearBeginMonth = 3;
        return this;
    }

    /**
     * Begin year from April (4th month).
     * @return this. (NotNull)
     */
    public FromToOption beginYear_Month04_April() {
        assertPatternOptionValid("beginYear_Month04_April");
        _yearBeginMonth = 4;
        return this;
    }

    /**
     * Begin year from May (5th month).
     * @return this. (NotNull)
     */
    public FromToOption beginYear_Month05_May() {
        assertPatternOptionValid("beginYear_Month05_May");
        _yearBeginMonth = 5;
        return this;
    }

    /**
     * Begin year from June (6th month).
     * @return this. (NotNull)
     */
    public FromToOption beginYear_Month06_June() {
        assertPatternOptionValid("beginYear_Month06_June");
        _yearBeginMonth = 6;
        return this;
    }

    /**
     * Begin year from July (7th month).
     * @return this. (NotNull)
     */
    public FromToOption beginYear_Month07_July() {
        assertPatternOptionValid("beginYear_Month07_July");
        _yearBeginMonth = 7;
        return this;
    }

    /**
     * Begin year from August (8th month).
     * @return this. (NotNull)
     */
    public FromToOption beginYear_Month08_August() {
        assertPatternOptionValid("beginYear_Month08_August");
        _yearBeginMonth = 8;
        return this;
    }

    /**
     * Begin year from September (9th month).
     * @return this. (NotNull)
     */
    public FromToOption beginYear_Month09_September() {
        assertPatternOptionValid("beginYear_Month09_September");
        _yearBeginMonth = 9;
        return this;
    }

    /**
     * Begin year from October (10th month).
     * @return this. (NotNull)
     */
    public FromToOption beginYear_Month10_October() {
        assertPatternOptionValid("beginYear_Month10_October");
        _yearBeginMonth = 10;
        return this;
    }

    /**
     * Begin year from November (11th month).
     * @return this. (NotNull)
     */
    public FromToOption beginYear_Month11_November() {
        assertPatternOptionValid("beginYear_Month11_November");
        _yearBeginMonth = 11;
        return this;
    }

    /**
     * Begin year from December (12th month).
     * @return this. (NotNull)
     */
    public FromToOption beginYear_Month12_December() {
        assertPatternOptionValid("beginYear_Month12_December");
        _yearBeginMonth = 12;
        return this;
    }

    /**
     * Begin year from the specified month of previous year.
     * <pre>
     * e.g. beginYear_PreviousMonth(11)
     *  year is from 11th month of previous year to 10th month of this year
     *  (the 2011 year means 2010/11/01 to 2011/10/31)
     * 
     * e.g. option.compareAsYear().beginYear_PreviousMonth(11)
     *  if from-date is 2011/01/01 and to-date is 2012/01/01 (means 2011, 2012 year are target),
     *  the condition is: greater-equal 2010/11/01 and less-than 2012/11/01
     * </pre>
     * @param yearBeginMonth The month of previous year for year-begin. (NotMinus, 1-12)
     * @return this. (NotNull)
     */
    public FromToOption beginYear_PreviousMonth(int yearBeginMonth) {
        assertPatternOptionValid("beginYear_PreviousMonth");
        assertNotMinusNotOver("yearBeginMonth", yearBeginMonth, 12);
        _yearBeginMonth = -yearBeginMonth; // to be minus
        return this;
    }

    // -----------------------------------------------------
    //                                           Begin Month
    //                                           -----------
    /**
     * Begin month from the specified day. <br />
     * The date of argument is used as only the day part.
     * <pre>
     * e.g. beginMonth_Day(toDate("2001/01/03"))
     *  month is from 3 day to 2 day of next month
     *  (the 2011/11 means 2011/11/03 to 2011/12/02)
     * 
     * e.g. option.compareAsMonth().beginMonth_Day(toDate("2001/01/03"))
     *  if from-date is 2011/11/01 and to-date is 2011/12/01 (means 11th, 12th months are target),
     *  the condition is: greater-equal 2011/11/03 and less-than 2012/01/03
     * </pre>
     * @param monthBeginDay The date that has the day of month-begin. (NotNull)
     * @return this. (NotNull)
     */
    public FromToOption beginMonth_Day(Date monthBeginDay) {
        assertPatternOptionValid("beginMonth_Day");
        assertArgumentNotNull("monthBeginDay", monthBeginDay);
        final Calendar cal = Calendar.getInstance();
        cal.setTime(monthBeginDay);
        _monthBeginDay = cal.get(Calendar.DAY_OF_MONTH);
        return this;
    }

    /**
     * Begin month from the specified day.
     * <pre>
     * e.g. beginMonth_Day(3)
     *  month is from 3 day to 2 day of next month
     *  (the 2011/11 means 2011/11/03 to 2011/12/02)
     * 
     * e.g. option.compareAsMonth().beginMonth_Day(3)
     *  if from-date is 2011/11/01 and to-date is 2011/12/01 (means 11th, 12th months are target),
     *  the condition is: greater-equal 2011/11/03 and less-than 2012/01/03
     * </pre>
     * @param monthBeginDay The day for month-begin. (NotMinus, 1-31)
     * @return this. (NotNull)
     */
    public FromToOption beginMonth_Day(int monthBeginDay) {
        assertPatternOptionValid("beginMonth_Day");
        assertNotMinusNotOver("monthBeginDay", monthBeginDay, 31);
        _monthBeginDay = monthBeginDay;
        return this;
    }

    /**
     * Begin year from the specified day of previous month.
     * <pre>
     * e.g. beginMonth_PreviousDay(25)
     *  month is from 25 day of previous year to 24 day of this month
     *  (the 2011/11 means 2011/10/25 to 2011/11/24)
     * 
     * e.g. option.compareAsMonth().beginMonth_PreviousDay(25)
     *  if from-date is 2011/11/01 and to-date is 2011/12/01 (means 11th, 12th months are target),
     *  the condition is: greater-equal 2011/10/25 and less-than 2011/12/25
     * </pre>
     * @param monthBeginDay The day of previous month for month-begin. (NotMinus, 1-31)
     * @return this. (NotNull)
     */
    public FromToOption beginMonth_PreviousDay(int monthBeginDay) {
        assertPatternOptionValid("beginMonth_PreviousDay");
        assertNotMinusNotOver("monthBeginDay", monthBeginDay, 31);
        _monthBeginDay = -monthBeginDay; // to be minus
        return this;
    }

    // -----------------------------------------------------
    //                                             Begin Day
    //                                             ---------
    /**
     * Begin day from the specified hour.
     * <pre>
     * e.g. beginDay_Hour(toDate("2001/01/01 06:00:00"))
     *  day is from 06h to 05h of next day
     *  (the 2011/11/27 means 2011/11/27 06h to 2011/11/28 05h)
     * 
     * e.g. option.compareAsDate().beginDay_Hour(toDate("2001/01/01 06:00:00"))
     *  if from-date is 2011/11/27 and to-date is 2011/11/28 (means 27, 28 days are target),
     *  the condition is: greater-equal 2011/11/27 06:00:00 and less-than 2011/11/28 06:00:00
     * </pre>
     * @param dayBeginHour The date that has the hour of day-begin. (NotNull)
     * @return this. (NotNull)
     */
    public FromToOption beginDay_Hour(Date dayBeginHour) {
        assertPatternOptionValid("beginDay_Hour");
        assertArgumentNotNull("dayBeginHour", dayBeginHour);
        final Calendar cal = Calendar.getInstance();
        cal.setTime(dayBeginHour);
        _dayBeginHour = cal.get(Calendar.HOUR_OF_DAY);
        return this;
    }

    /**
     * Begin day from the specified hour.
     * <pre>
     * e.g. beginDay_Hour(6)
     *  day is from 06h to 05h of next day
     *  (the 2011/11/27 means 2011/11/27 06h to 2011/11/28 05h)
     * 
     * e.g. option.compareAsDate().beginDay_Hour(6)
     *  if from-date is 2011/11/27 and to-date is 2011/11/28 (means 27, 28 days are target),
     *  the condition is: greater-equal 2011/11/27 06:00:00 and less-than 2011/11/28 06:00:00
     * </pre>
     * @param dayBeginHour The day of day-begin. (NotMinus, 1-23)
     * @return this. (NotNull)
     */
    public FromToOption beginDay_Hour(int dayBeginHour) {
        assertPatternOptionValid("beginDay_Hour");
        assertNotMinusNotOver("dayBeginHour", dayBeginHour, 23);
        _dayBeginHour = dayBeginHour;
        return this;
    }

    /**
     * Begin day from the specified hour of previous day.
     * <pre>
     * e.g. beginDay_PreviousHour(22)
     *  day is from 22h of previous day to 21h of this day
     *  (the 2011/11/27 means 2011/11/26 22h to 2011/11/27 21h)
     * 
     * e.g. option.compareAsDate().beginDay_PreviousHour(22)
     *  if from-date is 2011/11/27 and to-date is 2011/11/28 (means 27, 28 days are target),
     *  the condition is: greater-equal 2011/11/26 22:00:00 and less-than 2011/11/27 22:00:00
     * </pre>
     * @param dayBeginHour The day of day-begin. (NotMinus, 1-23)
     * @return this. (NotNull)
     */
    public FromToOption beginDay_PreviousHour(int dayBeginHour) {
        assertPatternOptionValid("beginDay_PreviousHour");
        assertNotMinusNotOver("dayBeginHour", dayBeginHour, 23);
        _dayBeginHour = -dayBeginHour; // to be minus
        return this;
    }

    // -----------------------------------------------------
    //                                            Begin Week
    //                                            ----------
    /**
     * Begin week from the specified day of week.
     * <pre>
     * e.g. beginWeek_DayOfWeek(toDate("2011/11/28")) *means Monday
     *  week starts Monday (the 2011/11/27 belongs the week, 2011/11/21 to 2011/11/27)
     * 
     * e.g. option.compareAsWeek().beginWeek_DayOfWeek(toDate("2011/11/28")) *means Monday
     *  if from-date is 2011/11/24 and to-date is 2011/12/01 (means two weeks are target),
     *  the condition is: greater-equal 2011/11/21 and less-than 2011/12/05
     * </pre>
     * @param weekBeginDayOfWeek The date that has the day of day-of-week-begin. (NotNull)
     * @return this. (NotNull)
     */
    public FromToOption beginWeek_DayOfWeek(Date weekBeginDayOfWeek) {
        assertPatternOptionValid("beginWeek_DayOfWeek");
        assertArgumentNotNull("weekBeginDayOfWeek", weekBeginDayOfWeek);
        final Calendar cal = Calendar.getInstance();
        cal.setTime(weekBeginDayOfWeek);
        final int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        return doBeginWeek(dayOfWeek);
    }

    /**
     * Begin week from Sunday.
     * <pre>
     * e.g. option.compareAsWeek().beginWeek_DayOfWeek1st_Sunday()
     *  if from-date is 2011/11/24 and to-date is 2011/12/01 (means two weeks are target),
     *  the condition is: greater-equal 2011/11/20 and less-than 2011/12/04
     * </pre>
     * @return this. (NotNull)
     */
    public FromToOption beginWeek_DayOfWeek1st_Sunday() {
        assertPatternOptionValid("beginWeek_DayOfWeek1st_Sunday");
        return doBeginWeek(Calendar.SUNDAY);
    }

    /**
     * Begin week from Monday.
     * <pre>
     * e.g. option.compareAsWeek().beginWeek_DayOfWeek2nd_Monday()
     *  if from-date is 2011/11/24 and to-date is 2011/12/01 (means two weeks are target),
     *  the condition is: greater-equal 2011/11/21 and less-than 2011/12/05
     * </pre>
     * @return this. (NotNull)
     */
    public FromToOption beginWeek_DayOfWeek2nd_Monday() {
        assertPatternOptionValid("beginWeek_DayOfWeek2nd_Monday");
        return doBeginWeek(Calendar.MONDAY);
    }

    /**
     * Begin week from Tuesday.
     * <pre>
     * e.g. option.compareAsWeek().beginWeek_DayOfWeek3rd_Tuesday()
     *  if from-date is 2011/11/24 and to-date is 2011/12/01 (means two weeks are target),
     *  the condition is: greater-equal 2011/11/22 and less-than 2011/12/06
     * </pre>
     * @return this. (NotNull)
     */
    public FromToOption beginWeek_DayOfWeek3rd_Tuesday() {
        assertPatternOptionValid("beginWeek_DayOfWeek3rd_Tuesday");
        return doBeginWeek(Calendar.TUESDAY);
    }

    /**
     * Begin week from Wednesday.
     * <pre>
     * e.g. option.compareAsWeek().beginWeek_DayOfWeek4th_Wednesday()
     *  if from-date is 2011/11/24 and to-date is 2011/12/01 (means two weeks are target),
     *  the condition is: greater-equal 2011/11/23 and less-than 2011/12/07
     * </pre>
     * @return this. (NotNull)
     */
    public FromToOption beginWeek_DayOfWeek4th_Wednesday() {
        assertPatternOptionValid("beginWeek_DayOfWeek4th_Wednesday");
        return doBeginWeek(Calendar.WEDNESDAY);
    }

    /**
     * Begin week from Thursday.
     * <pre>
     * e.g. option.compareAsWeek().beginWeek_DayOfWeek5th_Thursday()
     *  if from-date is 2011/11/24 and to-date is 2011/12/01 (means two weeks are target),
     *  the condition is: greater-equal 2011/11/24 and less-than 2011/12/08
     * </pre>
     * @return this. (NotNull)
     */
    public FromToOption beginWeek_DayOfWeek5th_Thursday() {
        assertPatternOptionValid("beginWeek_DayOfWeek5th_Thursday");
        return doBeginWeek(Calendar.THURSDAY);
    }

    /**
     * Begin week from Friday.
     * <pre>
     * e.g. option.compareAsWeek().beginWeek_DayOfWeek6th_Friday()
     *  if from-date is 2011/11/24 and to-date is 2011/12/01 (means two weeks are target),
     *  the condition is: greater-equal 2011/11/18 and less-than 2011/12/02
     * </pre>
     * @return this. (NotNull)
     */
    public FromToOption beginWeek_DayOfWeek6th_Friday() {
        assertPatternOptionValid("beginWeek_DayOfWeek6th_Friday");
        return doBeginWeek(Calendar.FRIDAY);
    }

    /**
     * Begin week from Saturday.
     * <pre>
     * e.g. option.compareAsWeek().beginWeek_DayOfWeek7th_Saturday()
     *  if from-date is 2011/11/24 and to-date is 2011/12/01 (means two weeks are target),
     *  the condition is: greater-equal 2011/11/19 and less-than 2011/12/03
     * </pre>
     * @return this. (NotNull)
     */
    public FromToOption beginWeek_DayOfWeek7th_Saturday() {
        assertPatternOptionValid("beginWeek_DayOfWeek7th_Saturday");
        return doBeginWeek(Calendar.SATURDAY);
    }

    protected FromToOption doBeginWeek(int weekBeginDayOfWeek) {
        _weekBeginDay = weekBeginDayOfWeek;
        return this;
    }

    // -----------------------------------------------------
    //                                         Move-to Scope
    //                                         -------------
    /**
     * Move to the specified count of scope.
     * <pre>
     * e.g.
     *  compareAsYear().moveToScope(-1): 2011 to 2010 year
     *  compareAsMonth().moveToScope(-1): 11th to 10th month
     *  compareAsDate().moveToScope(2): 27 to 29 day
     *  compareAsHour().moveToScope(7): 12 to 19 hour
     * 
     * e.g. compareAsDate().moveToScope(-1)
     *  if both from-date and to-date are 2011/11/27,
     *  the condition is: greater-equal 2011/11/26 and less-than 2011/11/27
     * 
     *  if both from-date is 2011/11/26 and to-date is 2011/11/28,
     *  the condition is: greater-equal 2011/11/25 and less-than 2011/11/28 
     * 
     * e.g. compareAsDate() *normal (no move-to scope)
     *  if both from-date and to-date are 2011/11/27,
     *  the condition is: greater-equal 2011/11/27 and less-than 2011/11/28
     * 
     *  if both from-date is 2011/11/26 and to-date is 2011/11/28,
     *  the condition is: greater-equal 2011/11/26 and less-than 2011/11/29 
     * </pre>
     * @param moveToCount The count to move-to. (MinusAllowed)
     * @return this. (NotNull)
     */
    public FromToOption moveToScope(int moveToCount) {
        assertPatternOptionValid("moveToScope");
        _moveToScope = moveToCount;
        return this;
    }

    // ===================================================================================
    //                                                                   Manual Adjustment
    //                                                                   =================
    // -----------------------------------------------------
    //                                                   All
    //                                                   ---
    protected void clearAll() {
        clearOperand();
        clearFromPattern();
        clearToPattern();
        clearFromDateWith();
        clearToDateWith();
        _usePattern = false;
    }

    // -----------------------------------------------------
    //                                               Operand
    //                                               -------
    /**
     * Set up operand for from-date as greater-than. <br />
     * This is for manual adjustment.
     * @return this. (NotNull)
     */
    public FromToOption greaterThan() {
        assertNotAdjustmentAfterPattern("greaterThan");
        _greaterThan = true;
        return this;
    }

    /**
     * Set up operand for to-date as less-than. <br />
     * This is for manual adjustment.
     * @return this. (NotNull)
     */
    public FromToOption lessThan() {
        assertNotAdjustmentAfterPattern("lessThan");
        _lessThan = true;
        return this;
    }

    protected void clearOperand() {
        _greaterThan = false;
        _lessThan = false;
    }

    // -----------------------------------------------------
    //                                             From Date
    //                                             ---------
    public FromToOption fromPatternHourJust() {
        assertNotAdjustmentAfterPattern("fromPatternHourJust");
        clearFromPattern();
        _fromPatternHourJust = true;
        return this;
    }

    public FromToOption fromPatternDayJust() {
        assertNotAdjustmentAfterPattern("fromPatternDayJust");
        clearFromPattern();
        _fromPatternDayJust = true;
        return this;
    }

    public FromToOption fromPatternMonthJust() {
        assertNotAdjustmentAfterPattern("fromPatternMonthJust");
        clearFromPattern();
        _fromPatternMonthJust = true;
        return this;
    }

    public FromToOption fromPatternYearJust() {
        assertNotAdjustmentAfterPattern("fromPatternYearJust");
        clearFromPattern();
        _fromPatternYearJust = true;
        return this;
    }

    public FromToOption fromPatternWeekJust() {
        assertNotAdjustmentAfterPattern("fromPatternWeekJust");
        clearFromPattern();
        _fromPatternWeekJust = true;
        return this;
    }

    public FromToOption fromPatternQuarterOfYearJust() {
        assertNotAdjustmentAfterPattern("fromPatternQuarterOfYearJust");
        clearFromPattern();
        _fromPatternQuarterOfYearJust = true;
        return this;
    }

    protected void clearFromPattern() {
        _fromPatternHourJust = false;
        _fromPatternDayJust = false;
        _fromPatternMonthJust = false;
        _fromPatternYearJust = false;
        _fromPatternWeekJust = false;
        _fromPatternQuarterOfYearJust = false;
    }

    public FromToOption fromDateWithNoon() {
        clearFromDateWith();
        _fromDateWithNoon = true;
        return this;
    }

    public FromToOption fromDateWithHour(int hourOfDay) {
        clearFromDateWith();
        _fromDateWithHour = hourOfDay;
        return this;
    }

    protected void clearFromDateWith() {
        _fromDateWithNoon = false;
        _fromDateWithHour = null;
    }

    // -----------------------------------------------------
    //                                               To Date
    //                                               -------
    public FromToOption toPatternNextHourJust() {
        assertNotAdjustmentAfterPattern("toPatternNextHourJust");
        clearToPattern();
        _toPatternNextHourJust = true;
        return this;
    }

    public FromToOption toPatternNextDayJust() {
        assertNotAdjustmentAfterPattern("toPatternNextDayJust");
        clearToPattern();
        _toPatternNextDayJust = true;
        return this;
    }

    public FromToOption toPatternNextMonthJust() {
        assertNotAdjustmentAfterPattern("toPatternNextMonthBegin");
        clearToPattern();
        _toPatternNextMonthJust = true;
        return this;
    }

    public FromToOption toPatternNextYearJust() {
        assertNotAdjustmentAfterPattern("toPatternNextYearJust");
        clearToPattern();
        _toPatternNextYearJust = true;
        return this;
    }

    public FromToOption toPatternNextWeekJust() {
        assertNotAdjustmentAfterPattern("toPatternNextWeekJust");
        clearToPattern();
        _toPatternNextWeekJust = true;
        return this;
    }

    public FromToOption toPatternNextQuarterOfYearJust() {
        assertNotAdjustmentAfterPattern("toPatternNextQuarterOfYearJust");
        clearToPattern();
        _toPatternNextQuarterOfYearJust = true;
        return this;
    }

    protected void clearToPattern() {
        _toPatternNextHourJust = false;
        _toPatternNextDayJust = false;
        _toPatternNextMonthJust = false;
        _toPatternNextYearJust = false;
        _toPatternNextWeekJust = false;
        _toPatternNextQuarterOfYearJust = false;
    }

    public FromToOption toDateWithNoon() {
        clearToDateWith();
        _toDateWithNoon = true;
        return this;
    }

    public FromToOption toDateWithHour(int hourOfDay) {
        clearToDateWith();
        _toDateWithHour = hourOfDay;
        return this;
    }

    protected void clearToDateWith() {
        _toDateWithNoon = false;
        _toDateWithHour = null;
    }

    // ===================================================================================
    //                                                                      Plug-in Option
    //                                                                      ==============
    /**
     * Add 'or is null' to from-to conditions.
     * @return this. (NotNull)
     */
    public FromToOption orIsNull() {
        _orIsNull = true;
        return this;
    }

    /**
     * Allow you to set one-side only condition. (null allowed) <br />
     * If you ignore null-or-empty query, you don't need to call this.
     * @return this. (NotNull)
     */
    public FromToOption allowOneSide() {
        _oneSideAllowed = true;
        return this;
    }

    /**
     * Does it allow you to set one-side only condition. <br />
     * @return The determination, true or false.
     */
    public boolean isOneSideAllowed() {
        return _oneSideAllowed;
    }

    // ===================================================================================
    //                                                                       Internal Main
    //                                                                       =============
    /**
     * Filter the date as From. It requires this method is called before getFromDateConditionKey().
     * @param fromDate The date as From. (NullAllowed: If the value is null, it returns null)
     * @return The filtered date as From. (NullAllowed)
     */
    public <DATE extends Date> DATE filterFromDate(DATE fromDate) {
        if (fromDate == null) {
            return null;
        }
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(fromDate.getTime());

        if (_fromPatternYearJust) {
            moveToCalendarYearJust(cal);
            moveToScopeYear(cal);
        } else if (_fromPatternMonthJust) {
            moveToCalendarMonthJust(cal);
            moveToScopeMonth(cal);
        } else if (_fromPatternDayJust) {
            moveToCalendarDayJust(cal);
            moveToScopeDay(cal);
        } else if (_fromPatternHourJust) {
            moveToCalendarHourJust(cal);
            moveToScopeHour(cal);
        } else if (_fromPatternWeekJust) {
            moveToCalendarWeekJust(cal);
            moveToScopeWeek(cal);
        } else if (_fromPatternQuarterOfYearJust) {
            moveToCalendarQuarterOfYearJust(cal);
            moveToScopeQuarterOfYear(cal);
        }
        if (_fromDateWithNoon) {
            moveToCalendarHourJustNoon(cal);
        }
        if (_fromDateWithHour != null) {
            moveToCalendarHourJustFor(cal, _fromDateWithHour);
        }

        @SuppressWarnings("unchecked")
        final DATE cloneDate = (DATE) fromDate.clone();
        cloneDate.setTime(cal.getTimeInMillis());
        fromDate = cloneDate;
        return fromDate;
    }

    /**
     * Filter the date as To. It requires this method is called before getToDateConditionKey().
     * @param toDate The date as To. (NullAllowed: If the value is null, it returns null)
     * @return The filtered date as To. (NullAllowed)
     */
    public <DATE extends Date> DATE filterToDate(DATE toDate) {
        if (toDate == null) {
            return null;
        }
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(toDate.getTime());

        if (_toPatternNextYearJust) {
            moveToCalendarNextYearJust(cal);
            moveToScopeYear(cal);
        } else if (_toPatternNextMonthJust) {
            moveToCalendarNextMonthJust(cal);
            moveToScopeMonth(cal);
        } else if (_toPatternNextDayJust) {
            moveToCalendarNextDayJust(cal);
            moveToScopeDay(cal);
        } else if (_toPatternNextHourJust) {
            moveToCalendarNextHourJust(cal);
            moveToScopeHour(cal);
        } else if (_toPatternNextWeekJust) {
            moveToCalendarNextWeekJust(cal);
            moveToScopeWeek(cal);
        } else if (_toPatternNextQuarterOfYearJust) {
            moveToCalendarNextQuarterOfYearJust(cal);
            moveToScopeQuarterOfYear(cal);
        }
        if (_toDateWithNoon) {
            moveToCalendarHourJustNoon(cal);
        }
        if (_toDateWithHour != null) {
            moveToCalendarHourJustFor(cal, _toDateWithHour);
        }

        @SuppressWarnings("unchecked")
        final DATE cloneDate = (DATE) toDate.clone();
        cloneDate.setTime(cal.getTimeInMillis());
        toDate = cloneDate;
        return toDate;
    }

    public Date xfilterToDateBetweenWay(Date toDate) {
        if (toDate == null) {
            return null;
        }
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(toDate.getTime());

        // moveToScope first because of using terminal
        if (_toPatternNextYearJust) {
            moveToScopeYear(cal);
            moveToCalendarYearTerminal(cal);
        } else if (_toPatternNextMonthJust) {
            moveToScopeMonth(cal);
            moveToCalendarMonthTerminal(cal);
        } else if (_toPatternNextDayJust) {
            moveToScopeDay(cal);
            moveToCalendarDayTerminal(cal);
        } else if (_toPatternNextHourJust) {
            moveToScopeHour(cal);
            moveToCalendarHourTerminal(cal);
        } else if (_toPatternNextWeekJust) {
            moveToScopeWeek(cal);
            moveToCalendarWeekTerminal(cal);
        } else if (_toPatternNextQuarterOfYearJust) {
            moveToScopeQuarterOfYear(cal);
            moveToCalendarQuarterOfYearTerminal(cal);
        }
        if (_toDateWithNoon) {
            moveToCalendarHourJustNoon(cal);
        }
        if (_toDateWithHour != null) {
            moveToCalendarHourJustFor(cal, _toDateWithHour);
        }

        final Date cloneDate = (Date) toDate.clone();
        cloneDate.setTime(cal.getTimeInMillis());
        toDate = cloneDate;
        return toDate;
    }

    protected Date filterNoon(Date date) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date.getTime());
        moveToCalendarHourJustNoon(cal);
        final Date cloneDate = (Date) date.clone();
        cloneDate.setTime(cal.getTimeInMillis());
        return cloneDate;
    }

    /**
     * Get the condition-key of the from-date. It requires this method is called after filterFromDate().
     * @return The condition-key of the from-date. (NotNull)
     */
    public ConditionKey getFromDateConditionKey() {
        if (_greaterThan) {
            return _orIsNull ? ConditionKey.CK_GREATER_THAN_OR_IS_NULL : ConditionKey.CK_GREATER_THAN;
        } else { // as default
            return _orIsNull ? ConditionKey.CK_GREATER_EQUAL_OR_IS_NULL : ConditionKey.CK_GREATER_EQUAL;
        }
    }

    /**
     * Get the condition-key of the to-date. It requires this method is called after filterToDate().
     * @return The condition-key of the to-date. (NotNull)
     */
    public ConditionKey getToDateConditionKey() {
        if (_lessThan) {
            return _orIsNull ? ConditionKey.CK_LESS_THAN_OR_IS_NULL : ConditionKey.CK_LESS_THAN;
        } else { // as default
            return _orIsNull ? ConditionKey.CK_LESS_EQUAL_OR_IS_NULL : ConditionKey.CK_LESS_EQUAL;
        }
    }

    // ===================================================================================
    //                                                                     Calendar Helper
    //                                                                     ===============
    // -----------------------------------------------------
    //                                          Move-to Just
    //                                          ------------
    protected void moveToCalendarYearJust(Calendar cal) {
        DfTypeUtil.moveToCalendarYearJust(cal, _yearBeginMonth);
    }

    protected void moveToCalendarMonthJust(Calendar cal) {
        DfTypeUtil.moveToCalendarMonthJust(cal, _monthBeginDay);
    }

    protected void moveToCalendarDayJust(Calendar cal) {
        DfTypeUtil.moveToCalendarDayJust(cal, _dayBeginHour);
    }

    protected void moveToCalendarHourJust(Calendar cal) {
        DfTypeUtil.moveToCalendarHourJust(cal);
    }

    protected void moveToCalendarHourJustFor(Calendar cal, int hourOfDay) {
        DfTypeUtil.moveToCalendarHourJustFor(cal, hourOfDay);
    }

    protected void moveToCalendarHourJustNoon(Calendar cal) {
        DfTypeUtil.moveToCalendarHourJustNoon(cal);
    }

    protected void moveToCalendarWeekJust(Calendar cal) {
        DfTypeUtil.moveToCalendarWeekJust(cal, _weekBeginDay);
    }

    protected void moveToCalendarQuarterOfYearJust(Calendar cal) {
        DfTypeUtil.moveToCalendarQuarterOfYearJust(cal, _yearBeginMonth);
    }

    // -----------------------------------------------------
    //                                          Move-to Next
    //                                          ------------
    protected void moveToCalendarNextYearJust(Calendar cal) {
        moveToCalendarYearTerminal(cal);
        addCalendarMillisecondOne(cal);
    }

    protected void moveToCalendarNextMonthJust(Calendar cal) {
        moveToCalendarMonthTerminal(cal);
        addCalendarMillisecondOne(cal);
    }

    protected void moveToCalendarNextDayJust(Calendar cal) {
        moveToCalendarDayTerminal(cal);
        addCalendarMillisecondOne(cal);
    }

    protected void moveToCalendarNextHourJust(Calendar cal) {
        moveToCalendarHourTerminal(cal);
        addCalendarMillisecondOne(cal);
    }

    protected void moveToCalendarNextWeekJust(Calendar cal) {
        moveToCalendarWeekTerminal(cal);
        addCalendarMillisecondOne(cal);
    }

    protected void moveToCalendarNextQuarterOfYearJust(Calendar cal) {
        moveToCalendarQuarterOfYearTerminal(cal);
        addCalendarMillisecondOne(cal);
    }

    protected void addCalendarMillisecondOne(Calendar cal) {
        DfTypeUtil.addCalendarMillisecond(cal, 1);
    }

    // -----------------------------------------------------
    //                                      Move-to Terminal
    //                                      ----------------
    protected void moveToCalendarYearTerminal(Calendar cal) {
        DfTypeUtil.moveToCalendarYearTerminal(cal, _yearBeginMonth);
    }

    protected void moveToCalendarMonthTerminal(Calendar cal) {
        DfTypeUtil.moveToCalendarMonthTerminal(cal, _monthBeginDay);
    }

    protected void moveToCalendarDayTerminal(Calendar cal) {
        DfTypeUtil.moveToCalendarDayTerminal(cal, _dayBeginHour);
    }

    protected void moveToCalendarHourTerminal(Calendar cal) {
        DfTypeUtil.moveToCalendarHourTerminal(cal);
    }

    protected void moveToCalendarWeekTerminal(Calendar cal) {
        DfTypeUtil.moveToCalendarWeekTerminal(cal, _weekBeginDay);
    }

    protected void moveToCalendarQuarterOfYearTerminal(Calendar cal) {
        DfTypeUtil.moveToCalendarQuarterOfYearTerminal(cal, _yearBeginMonth);
    }

    // -----------------------------------------------------
    //                                         Move-to Scope
    //                                         -------------
    protected void moveToScopeYear(Calendar cal) {
        if (_moveToScope != null) {
            DfTypeUtil.addCalendarYear(cal, _moveToScope);
        }
    }

    protected void moveToScopeMonth(Calendar cal) {
        if (_moveToScope != null) {
            DfTypeUtil.addCalendarMonth(cal, _moveToScope);
        }
    }

    protected void moveToScopeDay(Calendar cal) {
        if (_moveToScope != null) {
            DfTypeUtil.addCalendarDay(cal, _moveToScope);
        }
    }

    protected void moveToScopeHour(Calendar cal) {
        if (_moveToScope != null) {
            DfTypeUtil.addCalendarHour(cal, _moveToScope);
        }
    }

    protected void moveToScopeWeek(Calendar cal) {
        if (_moveToScope != null) {
            DfTypeUtil.addCalendarWeek(cal, _moveToScope);
        }
    }

    protected void moveToScopeQuarterOfYear(Calendar cal) {
        if (_moveToScope != null) {
            DfTypeUtil.addCalendarQuarterOfYear(cal, _moveToScope);
        }
    }

    // ===================================================================================
    //                                                            Interface Implementation
    //                                                            ========================
    public String getRearOption() {
        return "";
    }

    public boolean hasCompoundColumn() {
        return false;
    }

    public List<HpSpecifiedColumn> getCompoundColumnList() {
        return DfCollectionUtil.emptyList();
    }

    public boolean hasStringConnector() {
        return false;
    }

    public StringConnector getStringConnector() {
        return null;
    }

    public ExtensionOperand getExtensionOperand() {
        return null;
    }

    public QueryClauseArranger getWhereClauseArranger() {
        return null;
    }

    public GearedCipherManager getGearedCipherManager() {
        return null;
    }

    // ===================================================================================
    //                                                                       Assert Helper
    //                                                                       =============
    protected void assertArgumentNotNull(String name, Object value) {
        if (value == null) {
            String msg = "The argument '" + name + "' should not be null.";
            throw new IllegalArgumentException(msg);
        }
    }

    protected void assertNotMinusNotOver(String name, int value, int max) {
        if (value < 0) {
            String msg = "The argument '" + name + "' should not be minus: value=" + value;
            throw new IllegalArgumentException(msg);
        }
        if (value > max) {
            String msg = "The argument '" + name + "' should not be over: value=" + value + " max=" + max;
            throw new IllegalArgumentException(msg);
        }
    }

    protected void assertPatternOptionValid(String option) {
        if (!_usePattern) {
            String msg = "The option '" + option + "()' should be called after pattern setting.";
            throw new IllegalStateException(msg);
        }
    }

    protected void assertNotAdjustmentAfterPattern(String option) {
        if (_usePattern) {
            String msg = "The option '" + option + "()' should not be call after pattern setting.";
            throw new IllegalStateException(msg);
        }
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        final String title = DfTypeUtil.toClassTitle(this);
        final StringBuilder sb = new StringBuilder();
        sb.append(title);
        sb.append(":{usePattern=").append(_usePattern);
        sb.append(", greaterThan=").append(_greaterThan).append(", lessThan=").append(_lessThan);
        sb.append(", orIsNull=").append(_orIsNull).append("}");
        return sb.toString();
    }
}
