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

import org.seasar.dbflute.cbean.chelper.HpSpecifiedColumn;

/**
 * The conversion option for column. e.g. ColumnQuery <br />
 * You can filter an aggregate function by scalar function filters.
 * @author jflute
 */
public class ColumnConversionOption extends FunctionFilterOption {

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
    public ColumnConversionOption coalesce(Object coalesce) {
        doCoalesce(coalesce);
        return this;
    }

    /**
     * Round the specified part of the number.
     * @param round Decimal digits or date format for round. (NullAllowed: if null, no round)
     * @return this. (NotNull)
     */
    public ColumnConversionOption round(Object round) {
        doRound(round);
        return this;
    }

    /**
     * Truncate the specified part of the number or date-time value.
     * @param trunc Decimal digits or date option for trunc. (NullAllowed: if null, no trunc)
     * @return this. (NotNull)
     */
    public ColumnConversionOption trunc(Object trunc) {
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
    public ColumnConversionOption truncMonth() {
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
    public ColumnConversionOption truncDay() {
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
    public ColumnConversionOption truncTime() {
        doTruncTime();
        return this;
    }

    // -----------------------------------------------------
    //                                        Add Date, Year
    //                                        --------------
    /**
     * Add years to the date or date-time value.
     * @param addedYear The count of added years. (NullAllowed: if null, no dateAdd)
     * @return this. (NotNull)
     */
    public ColumnConversionOption addYear(Integer addedYear) {
        doAddYear(addedYear);
        return this;
    }

    /**
     * Add to the date or date-time value by the specified years column. <br />
     * Plus only, if you want minus, use substractYear() method.
     * <pre>
     * e.g. ColumnQuery: ... > PURCHASE_DATETIME + (PURCHASE_COUNT years)
     *  PurchaseCB cb = new PurchaseCB();
     *  cb.columnQuery(new SpecifyQuery() {
     *      public void specify(Purchase cb) {
     *          cb.column...();
     *      }
     *  }).greaterThan(new SpecifyQuery() {
     *      public void specify(Purchase cb) {
     *          cb.columnPurchaseDatetime();
     *      }
     *  }).convert(new ColumnConversionOption()
     *          .<span style="color: #DD4747">addYear</span>(cb.<span style="color: #DD4747">dreamCruiseCB()</span>.specify().columnPurchaseCount());
     * </pre>
     * @param addedColumn The added column specified by your Dream. (NullAllowed: if null, no dateAdd)
     * @return this. (NotNull)
     */
    public ColumnConversionOption addYear(HpSpecifiedColumn addedColumn) {
        doAddYear(addedColumn);
        return this;
    }

    /**
     * Subtract to the date or date-time value by the specified years column.
     * <pre>
     * e.g. ColumnQuery: ... > PURCHASE_DATETIME - (PURCHASE_COUNT years)
     *  PurchaseCB cb = new PurchaseCB();
     *  cb.columnQuery(new SpecifyQuery() {
     *      public void specify(Purchase cb) {
     *          cb.column...();
     *      }
     *  }).greaterThan(new SpecifyQuery() {
     *      public void specify(Purchase cb) {
     *          cb.columnPurchaseDatetime();
     *      }
     *  }).convert(new ColumnConversionOption()
     *          .<span style="color: #DD4747">subtractYear</span>(cb.<span style="color: #DD4747">dreamCruiseCB()</span>.specify().columnPurchaseCount());
     * </pre>
     * @param subtractedColumn The subtracted column specified by your Dream. (NullAllowed: if null, no dateAdd)
     * @return this. (NotNull)
     */
    public ColumnConversionOption subtractYear(HpSpecifiedColumn subtractedColumn) {
        doAddYear(subtractedColumn, true);
        return this;
    }

    // -----------------------------------------------------
    //                                       Add Date, Month
    //                                       ---------------
    /**
     * Add months to the date or date-time value.
     * @param addedMonth The count of added months. (NullAllowed: if null, no dateAdd)
     * @return this. (NotNull)
     */
    public ColumnConversionOption addMonth(Integer addedMonth) {
        doAddMonth(addedMonth);
        return this;
    }

    /**
     * Add to the date or date-time value by the specified months column. <br />
     * Plus only, if you want minus, use substractMonth() method.
     * <pre>
     * e.g. ColumnQuery: ... > PURCHASE_DATETIME + (PURCHASE_COUNT months)
     *  PurchaseCB cb = new PurchaseCB();
     *  cb.columnQuery(new SpecifyQuery() {
     *      public void specify(Purchase cb) {
     *          cb.column...();
     *      }
     *  }).greaterThan(new SpecifyQuery() {
     *      public void specify(Purchase cb) {
     *          cb.columnPurchaseDatetime();
     *      }
     *  }).convert(new ColumnConversionOption()
     *          .<span style="color: #DD4747">addMonth</span>(cb.<span style="color: #DD4747">dreamCruiseCB()</span>.specify().columnPurchaseCount());
     * </pre>
     * @param addedColumn The added column specified by your Dream. (NullAllowed: if null, no dateAdd)
     * @return this. (NotNull)
     */
    public ColumnConversionOption addMonth(HpSpecifiedColumn addedColumn) {
        doAddMonth(addedColumn);
        return this;
    }

    /**
     * Subtract to the date or date-time value by the specified months column.
     * <pre>
     * e.g. ColumnQuery: ... > PURCHASE_DATETIME - (PURCHASE_COUNT months)
     *  PurchaseCB cb = new PurchaseCB();
     *  cb.columnQuery(new SpecifyQuery() {
     *      public void specify(Purchase cb) {
     *          cb.column...();
     *      }
     *  }).greaterThan(new SpecifyQuery() {
     *      public void specify(Purchase cb) {
     *          cb.columnPurchaseDatetime();
     *      }
     *  }).convert(new ColumnConversionOption()
     *          .<span style="color: #DD4747">subtractMonth</span>(cb.<span style="color: #DD4747">dreamCruiseCB()</span>.specify().columnPurchaseCount());
     * </pre>
     * @param subtractedColumn The subtracted column specified by your Dream. (NullAllowed: if null, no dateAdd)
     * @return this. (NotNull)
     */
    public ColumnConversionOption subtractMonth(HpSpecifiedColumn subtractedColumn) {
        doAddMonth(subtractedColumn, true);
        return this;
    }

    // -----------------------------------------------------
    //                                         Add Date, Day
    //                                         -------------
    /**
     * Add days to the date or date-time value.
     * @param addedDay The count of added days. (NullAllowed: if null, no dateAdd)
     * @return this. (NotNull)
     */
    public ColumnConversionOption addDay(Integer addedDay) {
        doAddDay(addedDay);
        return this;
    }

    /**
     * Add to the date or date-time value by the specified days column. <br />
     * Plus only, if you want minus, use substractDay() method.
     * <pre>
     * e.g. ColumnQuery: ... > PURCHASE_DATETIME + (PURCHASE_COUNT days)
     *  PurchaseCB cb = new PurchaseCB();
     *  cb.columnQuery(new SpecifyQuery() {
     *      public void specify(Purchase cb) {
     *          cb.column...();
     *      }
     *  }).greaterThan(new SpecifyQuery() {
     *      public void specify(Purchase cb) {
     *          cb.columnPurchaseDatetime();
     *      }
     *  }).convert(new ColumnConversionOption()
     *          .<span style="color: #DD4747">addDay</span>(cb.<span style="color: #DD4747">dreamCruiseCB()</span>.specify().columnPurchaseCount());
     * </pre>
     * @param addedColumn The added column specified by your Dream. (NullAllowed: if null, no dateAdd)
     * @return this. (NotNull)
     */
    public ColumnConversionOption addDay(HpSpecifiedColumn addedColumn) {
        doAddDay(addedColumn);
        return this;
    }

    /**
     * Subtract to the date or date-time value by the specified days column.
     * <pre>
     * e.g. ColumnQuery: ... > PURCHASE_DATETIME - (PURCHASE_COUNT days)
     *  PurchaseCB cb = new PurchaseCB();
     *  cb.columnQuery(new SpecifyQuery() {
     *      public void specify(Purchase cb) {
     *          cb.column...();
     *      }
     *  }).greaterThan(new SpecifyQuery() {
     *      public void specify(Purchase cb) {
     *          cb.columnPurchaseDatetime();
     *      }
     *  }).convert(new ColumnConversionOption()
     *          .<span style="color: #DD4747">subtractDay</span>(cb.<span style="color: #DD4747">dreamCruiseCB()</span>.specify().columnPurchaseCount());
     * </pre>
     * @param subtractedColumn The subtracted column specified by your Dream. (NullAllowed: if null, no dateAdd)
     * @return this. (NotNull)
     */
    public ColumnConversionOption subtractDay(HpSpecifiedColumn subtractedColumn) {
        doAddDay(subtractedColumn, true);
        return this;
    }

    // -----------------------------------------------------
    //                                        Add Date, Hour
    //                                        --------------
    /**
     * Add hours to date-time value.
     * @param addedHour The count of added hours. (NullAllowed: if null, no dateAdd)
     * @return this. (NotNull)
     */
    public ColumnConversionOption addHour(Integer addedHour) {
        doAddHour(addedHour);
        return this;
    }

    /**
     * Add to the date or date-time value by the specified hours column. <br />
     * Plus only, if you want minus, use substractHour() method.
     * <pre>
     * e.g. ColumnQuery: ... > PURCHASE_DATETIME + (PURCHASE_COUNT hours)
     *  PurchaseCB cb = new PurchaseCB();
     *  cb.columnQuery(new SpecifyQuery() {
     *      public void specify(Purchase cb) {
     *          cb.column...();
     *      }
     *  }).greaterThan(new SpecifyQuery() {
     *      public void specify(Purchase cb) {
     *          cb.columnPurchaseDatetime();
     *      }
     *  }).convert(new ColumnConversionOption()
     *          .<span style="color: #DD4747">addHour</span>(cb.<span style="color: #DD4747">dreamCruiseCB()</span>.specify().columnPurchaseCount());
     * </pre>
     * @param addedColumn The added column specified by your Dream. (NullAllowed: if null, no dateAdd)
     * @return this. (NotNull)
     */
    public ColumnConversionOption addHour(HpSpecifiedColumn addedColumn) {
        doAddHour(addedColumn);
        return this;
    }

    /**
     * Subtract to the date or date-time value by the specified hours column.
     * <pre>
     * e.g. ColumnQuery: ... > PURCHASE_DATETIME - (PURCHASE_COUNT hours)
     *  PurchaseCB cb = new PurchaseCB();
     *  cb.columnQuery(new SpecifyQuery() {
     *      public void specify(Purchase cb) {
     *          cb.column...();
     *      }
     *  }).greaterThan(new SpecifyQuery() {
     *      public void specify(Purchase cb) {
     *          cb.columnPurchaseDatetime();
     *      }
     *  }).convert(new ColumnConversionOption()
     *          .<span style="color: #DD4747">subtractHour</span>(cb.<span style="color: #DD4747">dreamCruiseCB()</span>.specify().columnPurchaseCount());
     * </pre>
     * @param subtractedColumn The subtracted column specified by your Dream. (NullAllowed: if null, no dateAdd)
     * @return this. (NotNull)
     */
    public ColumnConversionOption subtractHour(HpSpecifiedColumn subtractedColumn) {
        doAddHour(subtractedColumn, true);
        return this;
    }

    // -----------------------------------------------------
    //                                      Add Date, Minute
    //                                      ----------------
    /**
     * Add minutes to date-time value.
     * @param addedMinute The count of added minutes. (NullAllowed: if null, no dateAdd)
     * @return this. (NotNull)
     */
    public ColumnConversionOption addMinute(Integer addedMinute) {
        doAddMinute(addedMinute);
        return this;
    }

    /**
     * Add to the date or date-time value by the specified minutes column. <br />
     * Plus only, if you want minus, use substractMinute() method.
     * <pre>
     * e.g. ColumnQuery: ... > PURCHASE_DATETIME + (PURCHASE_COUNT minutes)
     *  PurchaseCB cb = new PurchaseCB();
     *  cb.columnQuery(new SpecifyQuery() {
     *      public void specify(Purchase cb) {
     *          cb.column...();
     *      }
     *  }).greaterThan(new SpecifyQuery() {
     *      public void specify(Purchase cb) {
     *          cb.columnPurchaseDatetime();
     *      }
     *  }).convert(new ColumnConversionOption()
     *          .<span style="color: #DD4747">addMinute</span>(cb.<span style="color: #DD4747">dreamCruiseCB()</span>.specify().columnPurchaseCount());
     * </pre>
     * @param addedColumn The added column specified by your Dream. (NullAllowed: if null, no dateAdd)
     * @return this. (NotNull)
     */
    public ColumnConversionOption addMinute(HpSpecifiedColumn addedColumn) {
        doAddMinute(addedColumn);
        return this;
    }

    /**
     * Subtract to the date or date-time value by the specified minutes column.
     * <pre>
     * e.g. ColumnQuery: ... > PURCHASE_DATETIME - (PURCHASE_COUNT minutes)
     *  PurchaseCB cb = new PurchaseCB();
     *  cb.columnQuery(new SpecifyQuery() {
     *      public void specify(Purchase cb) {
     *          cb.column...();
     *      }
     *  }).greaterThan(new SpecifyQuery() {
     *      public void specify(Purchase cb) {
     *          cb.columnPurchaseDatetime();
     *      }
     *  }).convert(new ColumnConversionOption()
     *          .<span style="color: #DD4747">subtractMinute</span>(cb.<span style="color: #DD4747">dreamCruiseCB()</span>.specify().columnPurchaseCount());
     * </pre>
     * @param subtractedColumn The subtracted column specified by your Dream. (NullAllowed: if null, no dateAdd)
     * @return this. (NotNull)
     */
    public ColumnConversionOption subtractMinute(HpSpecifiedColumn subtractedColumn) {
        doAddMinute(subtractedColumn, true);
        return this;
    }

    // -----------------------------------------------------
    //                                      Add Date, Second
    //                                      ----------------
    /**
     * Add seconds to date-time value.
     * @param addedSecond The count of added seconds. (NullAllowed: if null, no dateAdd)
     * @return this. (NotNull)
     */
    public ColumnConversionOption addSecond(Integer addedSecond) {
        doAddSecond(addedSecond);
        return this;
    }

    /**
     * Add to the date or date-time value by the specified seconds column. <br />
     * Plus only, if you want minus, use substractSecond() method.
     * <pre>
     * e.g. ColumnQuery: ... > PURCHASE_DATETIME + (PURCHASE_COUNT seconds)
     *  PurchaseCB cb = new PurchaseCB();
     *  cb.columnQuery(new SpecifyQuery() {
     *      public void specify(Purchase cb) {
     *          cb.column...();
     *      }
     *  }).greaterThan(new SpecifyQuery() {
     *      public void specify(Purchase cb) {
     *          cb.columnPurchaseDatetime();
     *      }
     *  }).convert(new ColumnConversionOption()
     *          .<span style="color: #DD4747">addSecond</span>(cb.<span style="color: #DD4747">dreamCruiseCB()</span>.specify().columnPurchaseCount());
     * </pre>
     * @param addedColumn The added column specified by your Dream. (NullAllowed: if null, no dateAdd)
     * @return this. (NotNull)
     */
    public ColumnConversionOption addSecond(HpSpecifiedColumn addedColumn) {
        doAddSecond(addedColumn);
        return this;
    }

    /**
     * Subtract to the date or date-time value by the specified seconds column.
     * <pre>
     * e.g. ColumnQuery: ... > PURCHASE_DATETIME - (PURCHASE_COUNT seconds)
     *  PurchaseCB cb = new PurchaseCB();
     *  cb.columnQuery(new SpecifyQuery() {
     *      public void specify(Purchase cb) {
     *          cb.column...();
     *      }
     *  }).greaterThan(new SpecifyQuery() {
     *      public void specify(Purchase cb) {
     *          cb.columnPurchaseDatetime();
     *      }
     *  }).convert(new ColumnConversionOption()
     *          .<span style="color: #DD4747">subtractSecond</span>(cb.<span style="color: #DD4747">dreamCruiseCB()</span>.specify().columnPurchaseCount());
     * </pre>
     * @param subtractedColumn The subtracted column specified by your Dream. (NullAllowed: if null, no dateAdd)
     * @return this. (NotNull)
     */
    public ColumnConversionOption subtractSecond(HpSpecifiedColumn subtractedColumn) {
        doAddSecond(subtractedColumn, true);
        return this;
    }
}
