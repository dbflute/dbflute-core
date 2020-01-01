/*
 * Copyright 2014-2020 the original author or authors.
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
package org.dbflute.cbean.chelper;

import org.dbflute.cbean.ConditionBean;
import org.dbflute.cbean.ckey.ConditionKey;
import org.dbflute.cbean.dream.ColumnCalculator;
import org.dbflute.cbean.scoping.SpecifyQuery;

/**
 * @param <CB> The type of condition-bean.
 * @author jflute
 */
public class HpColQyOperand<CB extends ConditionBean> {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final HpColQyHandler<CB> _handler;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public HpColQyOperand(HpColQyHandler<CB> handler) {
        _handler = handler;
    }

    // ===================================================================================
    //                                                                          Comparison
    //                                                                          ==========
    /**
     * Equal(=).
     * <pre>
     * <span style="color: #3F7E5E">// where FOO = BAR</span>
     * cb.<span style="color: #994747">columnQuery</span>(<span style="color: #553000">colCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">colCB</span>.specify().<span style="color: #994747">columnFoo()</span>; <span style="color: #3F7E5E">// left column</span>
     * }).<span style="color: #CC4747">equal</span>(<span style="color: #553000">colCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">colCB</span>.specify().<span style="color: #CC4747">columnBar()</span>; <span style="color: #3F7E5E">// right column</span>
     * }); <span style="color: #3F7E5E">// you can calculate for right column like '}).plus(3);'</span>
     * </pre>
     * @param colCBLambda The callback for specify-query of right column. (NotNull)
     * @return The calculator for right column. (NotNull)
     */
    public ColumnCalculator equal(SpecifyQuery<CB> colCBLambda) {
        return _handler.handle(colCBLambda, ConditionKey.CK_EQUAL.getOperand());
    }

    /**
     * NotEqual(&lt;&gt;).
     * <pre>
     * <span style="color: #3F7E5E">// where FOO &lt;&gt; BAR</span>
     * cb.<span style="color: #994747">columnQuery</span>(<span style="color: #553000">colCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">colCB</span>.specify().<span style="color: #994747">columnFoo()</span>; <span style="color: #3F7E5E">// left column</span>
     * }).<span style="color: #CC4747">notEqual</span>(<span style="color: #553000">colCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">colCB</span>.specify().<span style="color: #CC4747">columnBar()</span>; <span style="color: #3F7E5E">// right column</span>
     * }); <span style="color: #3F7E5E">// you can calculate for right column like '}).plus(3);'</span>
     * </pre>
     * @param colCBLambda The callback for specify-query of right column. (NotNull)
     * @return The calculator for right column. (NotNull)
     */
    public ColumnCalculator notEqual(SpecifyQuery<CB> colCBLambda) {
        return _handler.handle(colCBLambda, ConditionKey.CK_NOT_EQUAL_STANDARD.getOperand());
    }

    /**
     * GreaterThan(&gt;).
     * <pre>
     * <span style="color: #3F7E5E">// where FOO &gt; BAR</span>
     * cb.<span style="color: #994747">columnQuery</span>(<span style="color: #553000">colCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">colCB</span>.specify().<span style="color: #994747">columnFoo()</span>; <span style="color: #3F7E5E">// left column</span>
     * }).<span style="color: #CC4747">greaterThan</span>(<span style="color: #553000">colCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">colCB</span>.specify().<span style="color: #CC4747">columnBar()</span>; <span style="color: #3F7E5E">// right column</span>
     * }); <span style="color: #3F7E5E">// you can calculate for right column like '}).plus(3);'</span>
     * </pre>
     * @param colCBLambda The callback for specify-query of right column. (NotNull)
     * @return The calculator for right column. (NotNull)
     */
    public ColumnCalculator greaterThan(SpecifyQuery<CB> colCBLambda) {
        return _handler.handle(colCBLambda, ConditionKey.CK_GREATER_THAN.getOperand());
    }

    /**
     * LessThan(&lt;).
     * <pre>
     * <span style="color: #3F7E5E">// where FOO &lt; BAR</span>
     * cb.<span style="color: #994747">columnQuery</span>(<span style="color: #553000">colCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">colCB</span>.specify().<span style="color: #994747">columnFoo()</span>; <span style="color: #3F7E5E">// left column</span>
     * }).<span style="color: #CC4747">lessThan</span>(<span style="color: #553000">colCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">colCB</span>.specify().<span style="color: #CC4747">columnBar()</span>; <span style="color: #3F7E5E">// right column</span>
     * }); <span style="color: #3F7E5E">// you can calculate for right column like '}).plus(3);'</span>
     * </pre>
     * @param colCBLambda The callback for specify-query of right column. (NotNull)
     * @return The calculator for right column. (NotNull)
     */
    public ColumnCalculator lessThan(SpecifyQuery<CB> colCBLambda) {
        return _handler.handle(colCBLambda, ConditionKey.CK_LESS_THAN.getOperand());
    }

    /**
     * GreaterEqual(&gt;=).
     * <pre>
     * <span style="color: #3F7E5E">// where FOO &gt;= BAR</span>
     * cb.<span style="color: #994747">columnQuery</span>(<span style="color: #553000">colCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">colCB</span>.specify().<span style="color: #994747">columnFoo()</span>; <span style="color: #3F7E5E">// left column</span>
     * }).<span style="color: #CC4747">greaterEqual</span>(<span style="color: #553000">colCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">colCB</span>.specify().<span style="color: #CC4747">columnBar()</span>; <span style="color: #3F7E5E">// right column</span>
     * }); <span style="color: #3F7E5E">// you can calculate for right column like '}).plus(3);'</span>
     * </pre>
     * @param colCBLambda The callback for specify-query of right column. (NotNull)
     * @return The calculator for right column. (NotNull)
     */
    public ColumnCalculator greaterEqual(SpecifyQuery<CB> colCBLambda) {
        return _handler.handle(colCBLambda, ConditionKey.CK_GREATER_EQUAL.getOperand());
    }

    /**
     * LessThan(&lt;=).
     * <pre>
     * <span style="color: #3F7E5E">// where FOO &lt;= BAR</span>
     * cb.<span style="color: #994747">columnQuery</span>(<span style="color: #553000">colCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">colCB</span>.specify().<span style="color: #994747">columnFoo()</span>; <span style="color: #3F7E5E">// left column</span>
     * }).<span style="color: #CC4747">lessEqual</span>(<span style="color: #553000">colCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">colCB</span>.specify().<span style="color: #CC4747">columnBar()</span>; <span style="color: #3F7E5E">// right column</span>
     * }); <span style="color: #3F7E5E">// you can calculate for right column like '}).plus(3);'</span>
     * </pre>
     * @param colCBLambda The callback for specify-query of right column. (NotNull)
     * @return The calculator for right column. (NotNull)
     */
    public ColumnCalculator lessEqual(SpecifyQuery<CB> colCBLambda) {
        return _handler.handle(colCBLambda, ConditionKey.CK_LESS_EQUAL.getOperand());
    }

    // ===================================================================================
    //                                                                     DBMS Dependency
    //                                                                     ===============
    public static class HpExtendedColQyOperandMySql<CB extends ConditionBean> extends HpColQyOperand<CB> {

        public HpExtendedColQyOperandMySql(HpColQyHandler<CB> handler) {
            super(handler);
        }

        /**
         * BitAnd(&amp;).
         * @param rightSpecifyQuery The specify-query for right column. (NotNull)
         * @return The calculator for right column. (NotNull)
         */
        public ColumnCalculator bitAnd(SpecifyQuery<CB> rightSpecifyQuery) {
            return _handler.handle(rightSpecifyQuery, "&");
        }

        /**
         * BitOr(|).
         * @param rightSpecifyQuery The specify-query for right column. (NotNull)
         * @return The calculator for right column. (NotNull)
         */
        public ColumnCalculator bitOr(SpecifyQuery<CB> rightSpecifyQuery) {
            return _handler.handle(rightSpecifyQuery, "|");
        }
    }
}
