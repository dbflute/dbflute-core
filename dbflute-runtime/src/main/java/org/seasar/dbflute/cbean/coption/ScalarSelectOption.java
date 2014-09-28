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

/**
 * The option for ScalarSelect. <br />
 * You can filter an aggregate function by scalar function filters.
 * @author jflute
 */
public class ScalarSelectOption extends FunctionFilterOption {

    // ===================================================================================
    //                                                                     Function Option
    //                                                                     ===============
    /**
     * Set the value for coalesce function. <br />
     * If you set string value and the derived column is date type, it converts it to a date object internally.
     * For example, "2010-10-30 12:34:56.789", "2010/10/30" and so on ... are acceptable.
     * @param coalesce An alternate value when group function returns null. (NullAllowed: if null, no coalesce)
     * @return this. (NotNull)
     */
    public ScalarSelectOption coalesce(Object coalesce) {
        doCoalesce(coalesce);
        return this;
    }

    /**
     * Round the specified part of the number.
     * @param round Decimal digits or date format for round. (NullAllowed: if null, no round)
     * @return this. (NotNull)
     */
    public ScalarSelectOption round(Object round) {
        doRound(round);
        return this;
    }

    /**
     * Truncate the specified part of the number or date-time value.
     * @param trunc Decimal digits or date option for trunc. (NullAllowed: if null, no trunc)
     * @return this. (NotNull)
     */
    public ScalarSelectOption trunc(Object trunc) {
        doTrunc(trunc);
        return this;
    }

    // ===================================================================================
    //                                                                      Purpose Option
    //                                                                      ==============
    // -----------------------------------------------------
    //                                         Truncate Date
    //                                         -------------
    /**
     * Truncate the month and day and time part of the date or the date-time value.
     * <pre>
     *  e.g. 2012/12/31 01:50:46 -&gt; 2012/<span style="color: #DD4747">01/01 00:00:00</span>
     * </pre>
     * @return this. (NotNull)
     */
    public ScalarSelectOption truncMonth() {
        doTruncMonth();
        return this;
    }

    /**
     * Truncate the day and time part of the date or the date-time value.
     * <pre>
     *  e.g. 2012/12/31 01:50:46 -&gt; 2012/12/<span style="color: #DD4747">01 00:00:00</span>
     * </pre>
     * @return this. (NotNull)
     */
    public ScalarSelectOption truncDay() {
        doTruncDay();
        return this;
    }

    /**
     * Truncate the time part of the date-time value.
     * <pre>
     *  e.g. 2012/12/31 01:50:46 -&gt; 2012/12/31 <span style="color: #DD4747">00:00:00</span>
     * </pre>
     * @return this. (NotNull)
     */
    public ScalarSelectOption truncTime() {
        doTruncTime();
        return this;
    }

    // -----------------------------------------------------
    //                                              Add Date
    //                                              --------
    // no DreamCruise ticket when ScalarSelect
    // cannot use not grouping column
    /**
     * Add years to the date or date-time value.
     * @param addedYear The count of added years. (NullAllowed: if null, no dateAdd)
     * @return this. (NotNull)
     */
    public ScalarSelectOption addYear(Integer addedYear) {
        doAddYear(addedYear);
        return this;
    }

    /**
     * Add months to the date or date-time value.
     * @param addedMonth The count of added months. (NullAllowed: if null, no dateAdd)
     * @return this. (NotNull)
     */
    public ScalarSelectOption addMonth(Integer addedMonth) {
        doAddMonth(addedMonth);
        return this;
    }

    /**
     * Add days to the date or date-time value.
     * @param addedDay The count of added days. (NullAllowed: if null, no dateAdd)
     * @return this. (NotNull)
     */
    public ScalarSelectOption addDay(Integer addedDay) {
        doAddDay(addedDay);
        return this;
    }

    /**
     * Add hours to date-time value.
     * @param addedHour The count of added hours. (NullAllowed: if null, no dateAdd)
     * @return this. (NotNull)
     */
    public ScalarSelectOption addHour(Integer addedHour) {
        doAddHour(addedHour);
        return this;
    }

    /**
     * Add minutes to date-time value.
     * @param addedMinute The count of added minutes. (NullAllowed: if null, no dateAdd)
     * @return this. (NotNull)
     */
    public ScalarSelectOption addMinute(Integer addedMinute) {
        doAddMinute(addedMinute);
        return this;
    }

    /**
     * Add seconds to date-time value.
     * @param addedSecond The count of added seconds. (NullAllowed: if null, no dateAdd)
     * @return this. (NotNull)
     */
    public ScalarSelectOption addSecond(Integer addedSecond) {
        doAddSecond(addedSecond);
        return this;
    }
}
