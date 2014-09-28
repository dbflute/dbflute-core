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
package org.seasar.dbflute.cbean.chelper;

import org.seasar.dbflute.cbean.coption.ColumnConversionOption;

/**
 * @author jflute
 */
public interface HpCalculator {

    // ===================================================================================
    //                                                                         Calculation
    //                                                                         ===========
    /**
     * Plus the specified column with the value. (+)
     * @param plusValue The number value for plus. (NotNull)
     * @return this. (NotNull)
     */
    HpCalculator plus(Number plusValue);

    /**
     * Plus the specified column with the plus column. (+) {Dream Cruise}
     * <pre>
     * e.g. ManualOrder: order by PURCHASE_PRICE + PURCHASE_COUNT
     *  PurchaseCB cb = new PurchaseCB();
     *  ManualOrderBean mob = new ManualOrderBean
     *  mob.<span style="color: #DD4747">plus</span>(cb.<span style="color: #DD4747">dreamCruiseCB()</span>.specify().columnPurchaseCount());
     *  cb.query().addOrderBy_PurchasePrice_Asc().withManualOrder(mob);
     * 
     * e.g. ColumnQuery: ... > PURCHASE_PRICE + PURCHASE_COUNT
     *  PurchaseCB cb = new PurchaseCB();
     *  cb.columnQuery(new SpecifyQuery() {
     *      public void specify(Purchase cb) {
     *          cb.column...();
     *      }
     *  }).greaterThan(new SpecifyQuery() {
     *      public void specify(Purchase cb) {
     *          cb.columnPurchasePrice();
     *      }
     *  }).<span style="color: #DD4747">plus</span>(cb.<span style="color: #DD4747">dreamCruiseCB()</span>.specify().columnPurchaseCount());
     * </pre>
     * @param plusColumn The plus column specified by your Dream Cruise. (NotNull)
     * @return this. (NotNull)
     */
    HpCalculator plus(HpSpecifiedColumn plusColumn);

    /**
     * Minus the specified column with the value. (-)
     * @param minusValue The number value for minus. (NotNull)
     * @return this. (NotNull)
     */
    HpCalculator minus(Number minusValue);

    /**
     * Minus the specified column with the minus column. (-) {Dream Cruise}
     * <pre>
     * e.g. ManualOrder: order by PURCHASE_PRICE - PURCHASE_COUNT
     *  PurchaseCB cb = new PurchaseCB();
     *  ManualOrderBean mob = new ManualOrderBean
     *  mob.<span style="color: #DD4747">minus</span>(cb.<span style="color: #DD4747">dreamCruiseCB()</span>.specify().columnPurchaseCount());
     *  cb.query().addOrderBy_PurchasePrice_Asc().withManualOrder(mob);
     * 
     * e.g. ColumnQuery: ... > PURCHASE_PRICE - PURCHASE_COUNT
     *  PurchaseCB cb = new PurchaseCB();
     *  cb.columnQuery(new SpecifyQuery() {
     *      public void specify(Purchase cb) {
     *          cb.column...();
     *      }
     *  }).greaterThan(new SpecifyQuery() {
     *      public void specify(Purchase cb) {
     *          cb.columnPurchasePrice();
     *      }
     *  }).<span style="color: #DD4747">minus</span>(cb.<span style="color: #DD4747">dreamCruiseCB()</span>.specify().columnPurchaseCount());
     * </pre>
     * @param minusColumn The minus column specified by your Dream Cruise. (NotNull)
     * @return this. (NotNull)
     */
    HpCalculator minus(HpSpecifiedColumn minusColumn);

    /**
     * Multiply the value to the specified column. (*)
     * @param multiplyValue The number value for multiply. (NotNull)
     * @return this. (NotNull)
     */
    HpCalculator multiply(Number multiplyValue);

    /**
     * Multiply the specified column with the multiply column. (*) {Dream Cruise}
     * <pre>
     * e.g. ManualOrder: order by PURCHASE_PRICE * PURCHASE_COUNT
     *  PurchaseCB cb = new PurchaseCB();
     *  ManualOrderBean mob = new ManualOrderBean
     *  mob.<span style="color: #DD4747">multiply</span>(cb.<span style="color: #DD4747">dreamCruiseCB()</span>.specify().columnPurchaseCount());
     *  cb.query().addOrderBy_PurchasePrice_Asc().withManualOrder(mob);
     * 
     * e.g. ColumnQuery: ... > PURCHASE_PRICE * PURCHASE_COUNT
     *  PurchaseCB cb = new PurchaseCB();
     *  cb.columnQuery(new SpecifyQuery() {
     *      public void specify(Purchase cb) {
     *          cb.column...();
     *      }
     *  }).greaterThan(new SpecifyQuery() {
     *      public void specify(Purchase cb) {
     *          cb.columnPurchasePrice();
     *      }
     *  }).<span style="color: #DD4747">multiply</span>(cb.<span style="color: #DD4747">dreamCruiseCB()</span>.specify().columnPurchaseCount());
     * </pre>
     * @param multiplyColumn The multiply column specified by your Dream Cruise. (NotNull)
     * @return this. (NotNull)
     */
    HpCalculator multiply(HpSpecifiedColumn multiplyColumn);

    /**
     * Divide the specified column by the value. (/)
     * @param divideValue The number value for divide. (NotNull)
     * @return this. (NotNull)
     */
    HpCalculator divide(Number divideValue);

    /**
     * Divide the specified column with the divide column. (/) {Dream Cruise}
     * <pre>
     * e.g. ManualOrder: order by PURCHASE_PRICE / PURCHASE_COUNT
     *  PurchaseCB cb = new PurchaseCB();
     *  ManualOrderBean mob = new ManualOrderBean
     *  mob.<span style="color: #DD4747">divide</span>(cb.<span style="color: #DD4747">dreamCruiseCB()</span>.specify().columnPurchaseCount());
     *  cb.query().addOrderBy_PurchasePrice_Asc().withManualOrder(mob);
     * 
     * e.g. ColumnQuery: ... > PURCHASE_PRICE / PURCHASE_COUNT
     *  PurchaseCB cb = new PurchaseCB();
     *  cb.columnQuery(new SpecifyQuery() {
     *      public void specify(Purchase cb) {
     *          cb.column...();
     *      }
     *  }).greaterThan(new SpecifyQuery() {
     *      public void specify(Purchase cb) {
     *          cb.columnPurchasePrice();
     *      }
     *  }).<span style="color: #DD4747">divide</span>(cb.<span style="color: #DD4747">dreamCruiseCB()</span>.specify().columnPurchaseCount());
     * </pre>
     * @param divideColumn The divide column specified by your Dream Cruise. (NotNull)
     * @return this. (NotNull)
     */
    HpCalculator divide(HpSpecifiedColumn divideColumn);

    /**
     * Convert the value of right column by function.
     * @param option The conversion option of column. (NotNull)
     * @return this. (NotNull)
     */
    HpCalculator convert(ColumnConversionOption option);

    // ===================================================================================
    //                                                                     Left/Right Mode
    //                                                                     ===============
    /**
     * To be for left column.
     * @return this. (NotNull)
     */
    HpCalculator left();

    /**
     * To be for right column. (default)<br />
     * It also means main process internally.
     * @return this. (NotNull)
     */
    HpCalculator right();
}
