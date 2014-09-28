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

import org.seasar.dbflute.cbean.ConditionBean;
import org.seasar.dbflute.cbean.SpecifyQuery;
import org.seasar.dbflute.cbean.ckey.ConditionKey;

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
     * cb.<span style="color: #DD4747">columnQuery</span>(new SpecifyQuery&lt;MemberCB&gt;() {
     *     public void query(MemberCB cb) {
     *         cb.specify().<span style="color: #DD4747">columnFoo()</span>; <span style="color: #3F7E5E">// left column</span>
     *     }
     * }).<span style="color: #DD4747">equal</span>(new SpecifyQuery&lt;MemberCB&gt;() {
     *     public void query(MemberCB cb) {
     *         cb.specify().<span style="color: #DD4747">columnBar()</span>; <span style="color: #3F7E5E">// right column</span>
     *     }
     * }); <span style="color: #3F7E5E">// you can calculate for right column like '}).plus(3);'</span>
     * </pre>
     * @param rightSpecifyQuery The specify-query for right column. (NotNull)
     * @return The calculator for right column. (NotNull)
     */
    public HpCalculator equal(SpecifyQuery<CB> rightSpecifyQuery) {
        return _handler.handle(rightSpecifyQuery, ConditionKey.CK_EQUAL.getOperand());
    }

    /**
     * NotEqual(&lt;&gt;).
     * <pre>
     * <span style="color: #3F7E5E">// where FOO &lt;&gt; BAR</span>
     * cb.<span style="color: #DD4747">columnQuery</span>(new SpecifyQuery&lt;MemberCB&gt;() {
     *     public void query(MemberCB cb) {
     *         cb.specify().<span style="color: #DD4747">columnFoo()</span>; <span style="color: #3F7E5E">// left column</span>
     *     }
     * }).<span style="color: #DD4747">notEqual</span>(new SpecifyQuery&lt;MemberCB&gt;() {
     *     public void query(MemberCB cb) {
     *         cb.specify().<span style="color: #DD4747">columnBar()</span>; <span style="color: #3F7E5E">// right column</span>
     *     }
     * }); <span style="color: #3F7E5E">// you can calculate for right column like '}).plus(3);'</span>
     * </pre>
     * @param rightSpecifyQuery The specify-query for right column. (NotNull)
     * @return The calculator for right column. (NotNull)
     */
    public HpCalculator notEqual(SpecifyQuery<CB> rightSpecifyQuery) {
        return _handler.handle(rightSpecifyQuery, ConditionKey.CK_NOT_EQUAL_STANDARD.getOperand());
    }

    /**
     * GreaterThan(&gt;).
     * <pre>
     * <span style="color: #3F7E5E">// where FOO &gt; BAR</span>
     * cb.<span style="color: #DD4747">columnQuery</span>(new SpecifyQuery&lt;MemberCB&gt;() {
     *     public void query(MemberCB cb) {
     *         cb.specify().<span style="color: #DD4747">columnFoo()</span>; <span style="color: #3F7E5E">// left column</span>
     *     }
     * }).<span style="color: #DD4747">greaterThan</span>(new SpecifyQuery&lt;MemberCB&gt;() {
     *     public void query(MemberCB cb) {
     *         cb.specify().<span style="color: #DD4747">columnBar()</span>; <span style="color: #3F7E5E">// right column</span>
     *     }
     * }); <span style="color: #3F7E5E">// you can calculate for right column like '}).plus(3);'</span>
     * </pre>
     * @param rightSpecifyQuery The specify-query for right column. (NotNull)
     * @return The calculator for right column. (NotNull)
     */
    public HpCalculator greaterThan(SpecifyQuery<CB> rightSpecifyQuery) {
        return _handler.handle(rightSpecifyQuery, ConditionKey.CK_GREATER_THAN.getOperand());
    }

    /**
     * LessThan(&lt;).
     * <pre>
     * <span style="color: #3F7E5E">// where FOO &lt; BAR</span>
     * cb.<span style="color: #DD4747">columnQuery</span>(new SpecifyQuery&lt;MemberCB&gt;() {
     *     public void query(MemberCB cb) {
     *         cb.specify().<span style="color: #DD4747">columnFoo()</span>; <span style="color: #3F7E5E">// left column</span>
     *     }
     * }).<span style="color: #DD4747">lessThan</span>(new SpecifyQuery&lt;MemberCB&gt;() {
     *     public void query(MemberCB cb) {
     *         cb.specify().<span style="color: #DD4747">columnBar()</span>; <span style="color: #3F7E5E">// right column</span>
     *     }
     * }); <span style="color: #3F7E5E">// you can calculate for right column like '}).plus(3);'</span>
     * </pre>
     * @param rightSpecifyQuery The specify-query for right column. (NotNull)
     * @return The calculator for right column. (NotNull)
     */
    public HpCalculator lessThan(SpecifyQuery<CB> rightSpecifyQuery) {
        return _handler.handle(rightSpecifyQuery, ConditionKey.CK_LESS_THAN.getOperand());
    }

    /**
     * GreaterEqual(&gt;=).
     * <pre>
     * <span style="color: #3F7E5E">// where FOO &gt;= BAR</span>
     * cb.<span style="color: #DD4747">columnQuery</span>(new SpecifyQuery&lt;MemberCB&gt;() {
     *     public void query(MemberCB cb) {
     *         cb.specify().<span style="color: #DD4747">columnFoo()</span>; <span style="color: #3F7E5E">// left column</span>
     *     }
     * }).<span style="color: #DD4747">greaterEqual</span>(new SpecifyQuery&lt;MemberCB&gt;() {
     *     public void query(MemberCB cb) {
     *         cb.specify().<span style="color: #DD4747">columnBar()</span>; <span style="color: #3F7E5E">// right column</span>
     *     }
     * }); <span style="color: #3F7E5E">// you can calculate for right column like '}).plus(3);'</span>
     * </pre>
     * @param rightSpecifyQuery The specify-query for right column. (NotNull)
     * @return The calculator for right column. (NotNull)
     */
    public HpCalculator greaterEqual(SpecifyQuery<CB> rightSpecifyQuery) {
        return _handler.handle(rightSpecifyQuery, ConditionKey.CK_GREATER_EQUAL.getOperand());
    }

    /**
     * LessThan(&lt;=).
     * <pre>
     * <span style="color: #3F7E5E">// where FOO &lt;= BAR</span>
     * cb.<span style="color: #DD4747">columnQuery</span>(new SpecifyQuery&lt;MemberCB&gt;() {
     *     public void query(MemberCB cb) {
     *         cb.specify().<span style="color: #DD4747">columnFoo()</span>; <span style="color: #3F7E5E">// left column</span>
     *     }
     * }).<span style="color: #DD4747">lessEqual</span>(new SpecifyQuery&lt;MemberCB&gt;() {
     *     public void query(MemberCB cb) {
     *         cb.specify().<span style="color: #DD4747">columnBar()</span>; <span style="color: #3F7E5E">// right column</span>
     *     }
     * }); <span style="color: #3F7E5E">// you can calculate for right column like '}).plus(3);'</span>
     * </pre>
     * @param rightSpecifyQuery The specify-query for right column. (NotNull)
     * @return The calculator for right column. (NotNull)
     */
    public HpCalculator lessEqual(SpecifyQuery<CB> rightSpecifyQuery) {
        return _handler.handle(rightSpecifyQuery, ConditionKey.CK_LESS_EQUAL.getOperand());
    }

    // ===================================================================================
    //                                                                     DBMS Dependency
    //                                                                     ===============
    public static class HpExtendedColQyOperandMySql<CB extends ConditionBean> extends HpColQyOperand<CB> {

        public HpExtendedColQyOperandMySql(HpColQyHandler<CB> handler) {
            super(handler);
        }

        /**
         * BitAnd(&).
         * @param rightSpecifyQuery The specify-query for right column. (NotNull)
         * @return The calculator for right column. (NotNull)
         */
        public HpCalculator bitAnd(SpecifyQuery<CB> rightSpecifyQuery) {
            return _handler.handle(rightSpecifyQuery, "&");
        }

        /**
         * BitOr(|).
         * @param rightSpecifyQuery The specify-query for right column. (NotNull)
         * @return The calculator for right column. (NotNull)
         */
        public HpCalculator bitOr(SpecifyQuery<CB> rightSpecifyQuery) {
            return _handler.handle(rightSpecifyQuery, "|");
        }
    }
}
