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
package org.dbflute.cbean.chelper;

import org.dbflute.bhv.exception.BehaviorExceptionThrower;
import org.dbflute.cbean.ConditionBean;
import org.dbflute.cbean.coption.FunctionFilterOptionCall;
import org.dbflute.cbean.coption.ScalarSelectOption;
import org.dbflute.cbean.exception.ConditionBeanExceptionThrower;
import org.dbflute.cbean.scoping.ScalarQuery;
import org.dbflute.cbean.sqlclause.SqlClause;
import org.dbflute.cbean.sqlclause.clause.SelectClauseType;
import org.dbflute.dbmeta.info.ColumnInfo;
import org.dbflute.optional.OptionalScalar;

/**
 * The function for scalar select. 
 * @param <CB> The type of condition-bean.
 * @param <RESULT> The type of result for scalar select
 * @author jflute
 */
public class HpSLSFunction<CB extends ConditionBean, RESULT> {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The condition-bean for scalar select. (NotNull) */
    protected final CB _conditionBean;

    /** The condition-bean for scalar select. (NotNull) */
    protected final Class<RESULT> _resultType;

    /** The executor of scalar select. (NotNull) */
    protected final HpSLSExecutor<CB, RESULT> _executor;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * @param conditionBean The condition-bean initialized only for scalar select. (NotNull)
     * @param resultType The type as result. (NotNull)
     * @param executor The executor of scalar select with select clause type. (NotNull)
     */
    public HpSLSFunction(CB conditionBean, Class<RESULT> resultType, HpSLSExecutor<CB, RESULT> executor) {
        _conditionBean = conditionBean;
        _resultType = resultType;
        _executor = executor;
    }

    // ===================================================================================
    //                                                                            Function
    //                                                                            ========
    /**
     * Select the count value. <br>
     * You can also get same result by selectCount(cb) method.
     * <pre>
     * memberBhv.scalarSelect(Integer.class).<span style="color: #CC4747">count</span>(<span style="color: #553000">cb</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">cb</span>.specify().<span style="color: #CC4747">columnMemberId</span>(); <span style="color: #3F7E5E">// the required specification of (basically) primary key column</span>
     *     <span style="color: #553000">cb</span>.query().setMemberStatusCode_Equal_Formalized(); <span style="color: #3F7E5E">// query as you like it</span>
     * });
     * </pre>
     * @param cbLambda The callback to select scalar value. (NotNull)
     * @return The count value calculated by function. (NotNull)
     */
    public RESULT count(ScalarQuery<CB> cbLambda) {
        return doCount(cbLambda, null);
    }

    /**
     * Select the count value with function conversion option.
     * <pre>
     * memberBhv.scalarSelect(Integer.class).<span style="color: #CC4747">count</span>(<span style="color: #553000">cb</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">cb</span>.specify().columnMemberId(); <span style="color: #3F7E5E">// the required specification of (basically) primary key column</span>
     *     <span style="color: #553000">cb</span>.query().setMemberStatusCode_Equal_Formalized(); <span style="color: #3F7E5E">// query as you like it</span>
     * }, <span style="color: #553000">op</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> <span style="color: #553000">op</span>.<span style="color: #CC4747">coalesce</span>(0));
     * </pre>
     * @param cbLambda The callback to select scalar value. (NotNull)
     * @param opLambda The callback for option of scalar. (NotNull)
     * @return The count value calculated by function. (NotNull)
     */
    public RESULT count(ScalarQuery<CB> cbLambda, FunctionFilterOptionCall<ScalarSelectOption> opLambda) {
        final ScalarSelectOption option = createScalarSelectOption();
        opLambda.callback(option);
        assertScalarSelectOption(option);
        return doCount(cbLambda, option);
    }

    protected RESULT doCount(ScalarQuery<CB> scalarQuery, ScalarSelectOption option) {
        assertScalarQuery(scalarQuery);
        return exec(scalarQuery, SelectClauseType.UNIQUE_COUNT, option);
    }

    /**
     * Select the count-distinct value. <br>
     * You can also get same result by selectCount(cb) method.
     * <pre>
     * memberBhv.scalarSelect(Integer.class).<span style="color: #CC4747">countDistinct</span>(<span style="color: #553000">cb</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">cb</span>.specify().<span style="color: #CC4747">columnMemberId</span>(); <span style="color: #3F7E5E">// the required specification of (basically) primary key column</span>
     *     <span style="color: #553000">cb</span>.query().setMemberStatusCode_Equal_Formalized(); <span style="color: #3F7E5E">// query as you like it</span>
     * });
     * </pre>
     * @param cbLambda The callback to select scalar value. (NotNull)
     * @return The count-distinct value calculated by function. (NotNull)
     */
    public RESULT countDistinct(ScalarQuery<CB> cbLambda) {
        return doCountDistinct(cbLambda, null);
    }

    /**
     * Select the count-distinct value with function conversion option.
     * <pre>
     * memberBhv.scalarSelect(Integer.class).<span style="color: #CC4747">countDistinct</span>(<span style="color: #553000">cb</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">cb</span>.specify().columnMemberId(); <span style="color: #3F7E5E">// the required specification of (basically) primary key column</span>
     *     <span style="color: #553000">cb</span>.query().setMemberStatusCode_Equal_Formalized(); <span style="color: #3F7E5E">// query as you like it</span>
     * }, <span style="color: #553000">op</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> <span style="color: #553000">op</span>.<span style="color: #CC4747">coalesce</span>(0));
     * </pre>
     * @param cbLambda The callback to select scalar value. (NotNull)
     * @param opLambda The callback for option of scalar. (NotNull)
     * @return The count-distinct value calculated by function. (NotNull)
     */
    public RESULT countDistinct(ScalarQuery<CB> cbLambda, FunctionFilterOptionCall<ScalarSelectOption> opLambda) {
        final ScalarSelectOption option = createScalarSelectOption();
        opLambda.callback(option);
        assertScalarSelectOption(option);
        return doCountDistinct(cbLambda, option);
    }

    protected RESULT doCountDistinct(ScalarQuery<CB> scalarQuery, ScalarSelectOption option) {
        assertScalarQuery(scalarQuery);
        return exec(scalarQuery, SelectClauseType.COUNT_DISTINCT, option);
    }

    /**
     * Select the maximum value.
     * <pre>
     * memberBhv.scalarSelect(Date.class).<span style="color: #CC4747">max</span>(<span style="color: #553000">cb</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">cb</span>.specify().<span style="color: #CC4747">columnBirthdate</span>(); <span style="color: #3F7E5E">// the required specification of target column</span>
     *     <span style="color: #553000">cb</span>.query().setMemberStatusCode_Equal_Formalized(); <span style="color: #3F7E5E">// query as you like it</span>
     * });
     * </pre>
     * @param cbLambda The callback to select scalar value. (NotNull)
     * @return The optional scalar for maximum value calculated by function. (NullAllowed)
     */
    public OptionalScalar<RESULT> max(ScalarQuery<CB> cbLambda) {
        return doMax(cbLambda, null);
    }

    /**
     * Select the maximum value with function conversion option.
     * <pre>
     * memberBhv.scalarSelect(Date.class).<span style="color: #CC4747">max</span>(<span style="color: #553000">cb</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">cb</span>.specify().<span style="color: #CC4747">columnBirthdate</span>(); <span style="color: #3F7E5E">// the required specification of target column</span>
     *     <span style="color: #553000">cb</span>.query().setMemberStatusCode_Equal_Formalized(); <span style="color: #3F7E5E">// query as you like it</span>
     * }, <span style="color: #553000">op</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> <span style="color: #553000">op</span>.<span style="color: #CC4747">coalesce</span>(0));
     * </pre>
     * @param cbLambda The callback to select scalar value. (NotNull)
     * @param opLambda The callback for option of scalar. (NotNull)
     * @return The optional scalar for maximum value calculated by function. (NullAllowed: or NotNull if you use coalesce by option)
     */
    public OptionalScalar<RESULT> max(ScalarQuery<CB> cbLambda, FunctionFilterOptionCall<ScalarSelectOption> opLambda) {
        final ScalarSelectOption option = createScalarSelectOption();
        opLambda.callback(option);
        assertScalarSelectOption(option);
        return doMax(cbLambda, option);
    }

    protected OptionalScalar<RESULT> doMax(ScalarQuery<CB> scalarQuery, ScalarSelectOption option) {
        assertScalarQuery(scalarQuery);
        return optionalOf("max", exec(scalarQuery, SelectClauseType.MAX, option));
    }

    /**
     * Select the minimum value.
     * <pre>
     * memberBhv.scalarSelect(Date.class).<span style="color: #CC4747">min</span>(<span style="color: #553000">cb</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">cb</span>.specify().<span style="color: #CC4747">columnBirthdate</span>(); <span style="color: #3F7E5E">// the required specification of target column</span>
     *     <span style="color: #553000">cb</span>.query().setMemberStatusCode_Equal_Formalized(); <span style="color: #3F7E5E">// query as you like it</span>
     * });
     * </pre>
     * @param cbLambda The callback to select scalar value. (NotNull)
     * @return The optional scalar for minimum value calculated by function. (NullAllowed)
     */
    public OptionalScalar<RESULT> min(ScalarQuery<CB> cbLambda) {
        return doMin(cbLambda, null);
    }

    /**
     * Select the minimum value with function conversion option.
     * <pre>
     * memberBhv.scalarSelect(Date.class).<span style="color: #CC4747">min</span>(<span style="color: #553000">cb</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">cb</span>.specify().<span style="color: #CC4747">columnBirthdate</span>(); <span style="color: #3F7E5E">// the required specification of target column</span>
     *     <span style="color: #553000">cb</span>.query().setMemberStatusCode_Equal_Formalized(); <span style="color: #3F7E5E">// query as you like it</span>
     * }, <span style="color: #553000">op</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> <span style="color: #553000">op</span>.<span style="color: #CC4747">coalesce</span>(0));
     * </pre>
     * @param cbLambda The callback to select scalar value. (NotNull)
     * @param opLambda The callback for option of scalar. (NotNull)
     * @return The optional scalar for minimum value calculated by function. (NullAllowed: or NotNull if you use coalesce by option)
     */
    public OptionalScalar<RESULT> min(ScalarQuery<CB> cbLambda, FunctionFilterOptionCall<ScalarSelectOption> opLambda) {
        final ScalarSelectOption option = createScalarSelectOption();
        opLambda.callback(option);
        assertScalarSelectOption(option);
        return doMin(cbLambda, option);
    }

    protected OptionalScalar<RESULT> doMin(ScalarQuery<CB> scalarQuery, ScalarSelectOption option) {
        assertScalarQuery(scalarQuery);
        return optionalOf("min", exec(scalarQuery, SelectClauseType.MIN, option));
    }

    /**
     * Select the summary value.
     * <pre>
     * purchaseBhv.scalarSelect(Integer.class).<span style="color: #CC4747">sum</span>(<span style="color: #553000">cb</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">cb</span>.specify().<span style="color: #CC4747">columnPurchaseCount</span>(); <span style="color: #3F7E5E">// the required specification of target column</span>
     *     <span style="color: #553000">cb</span>.query().setPurchaseDatetime_GreaterEqual(date); <span style="color: #3F7E5E">// query as you like it</span>
     * });
     * </pre>
     * @param cbLambda The callback to select scalar value. (NotNull)
     * @return The optional scalar for summary value calculated by function. (NullAllowed)
     */
    public OptionalScalar<RESULT> sum(ScalarQuery<CB> cbLambda) {
        return doSum(cbLambda, null);
    }

    /**
     * Select the summary value with function conversion option.
     * <pre>
     * purchaseBhv.scalarSelect(Integer.class).<span style="color: #CC4747">sum</span>(<span style="color: #553000">cb</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">cb</span>.specify().<span style="color: #CC4747">columnPurchaseCount</span>(); <span style="color: #3F7E5E">// the required specification of target column</span>
     *     <span style="color: #553000">cb</span>.query().setPurchaseDatetime_GreaterEqual(date); <span style="color: #3F7E5E">// query as you like it</span>
     * }, <span style="color: #553000">op</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> <span style="color: #553000">op</span>.<span style="color: #CC4747">coalesce</span>(0));
     * </pre>
     * @param cbLambda The callback to select scalar value. (NotNull)
     * @param opLambda The callback for option of scalar. (NotNull)
     * @return The optional scalar for summary value calculated by function. (NullAllowed: or NotNull if you use coalesce by option)
     */
    public OptionalScalar<RESULT> sum(ScalarQuery<CB> cbLambda, FunctionFilterOptionCall<ScalarSelectOption> opLambda) {
        final ScalarSelectOption option = createScalarSelectOption();
        opLambda.callback(option);
        assertScalarSelectOption(option);
        return doSum(cbLambda, option);
    }

    protected OptionalScalar<RESULT> doSum(ScalarQuery<CB> scalarQuery, ScalarSelectOption option) {
        assertScalarQuery(scalarQuery);
        return optionalOf("sum", exec(scalarQuery, SelectClauseType.SUM, option));
    }

    /**
     * Select the average value.
     * <pre>
     * purchaseBhv.scalarSelect(Integer.class).<span style="color: #CC4747">avg</span>(<span style="color: #553000">cb</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">cb</span>.specify().<span style="color: #CC4747">columnPurchaseCount</span>(); <span style="color: #3F7E5E">// the required specification of target column</span>
     *     <span style="color: #553000">cb</span>.query().setPurchaseDatetime_GreaterEqual(date); <span style="color: #3F7E5E">// query as you like it</span>
     * });
     * </pre>
     * @param cbLambda The callback to select scalar value. (NotNull)
     * @return The optional scalar for average value calculated by function. (NullAllowed)
     */
    public OptionalScalar<RESULT> avg(ScalarQuery<CB> cbLambda) {
        return doAvg(cbLambda, null);
    }

    /**
     * Select the average value.
     * <pre>
     * purchaseBhv.scalarSelect(Integer.class).<span style="color: #CC4747">avg</span>(<span style="color: #553000">cb</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">cb</span>.specify().<span style="color: #CC4747">columnPurchaseCount</span>(); <span style="color: #3F7E5E">// the required specification of target column</span>
     *     <span style="color: #553000">cb</span>.query().setPurchaseDatetime_GreaterEqual(date); <span style="color: #3F7E5E">// query as you like it</span>
     * }, <span style="color: #553000">op</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> <span style="color: #553000">op</span>.<span style="color: #CC4747">coalesce</span>(0));
     * </pre>
     * @param cbLambda The callback to select scalar value. (NotNull)
     * @param opLambda The callback for option of scalar. (NotNull)
     * @return The optional scalar for average value calculated by function. (NullAllowed: or NotNull if you use coalesce by option)
     */
    public OptionalScalar<RESULT> avg(ScalarQuery<CB> cbLambda, FunctionFilterOptionCall<ScalarSelectOption> opLambda) {
        final ScalarSelectOption option = createScalarSelectOption();
        opLambda.callback(option);
        assertScalarSelectOption(option);
        return doAvg(cbLambda, option);
    }

    protected OptionalScalar<RESULT> doAvg(ScalarQuery<CB> scalarQuery, ScalarSelectOption option) {
        assertScalarQuery(scalarQuery);
        return optionalOf("avg", exec(scalarQuery, SelectClauseType.AVG, option));
    }

    protected OptionalScalar<RESULT> optionalOf(String title, RESULT result) {
        return OptionalScalar.ofNullable(result, () -> {
            throwScalarSelectValueNotFoundException(title);
        });
    }

    protected void throwScalarSelectValueNotFoundException(String title) {
        createBhvExThrower().throwScalarSelectValueNotFoundException(title, _conditionBean, _resultType);
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    protected RESULT exec(ScalarQuery<CB> scalarQuery, SelectClauseType selectClauseType, ScalarSelectOption option) {
        assertObjectNotNull("scalarQuery", scalarQuery);
        assertObjectNotNull("selectClauseType", selectClauseType);
        assertObjectNotNull("conditionBean", _conditionBean);
        assertObjectNotNull("resultType", _resultType);
        scalarQuery.query(_conditionBean);
        setupTargetColumnInfo(option);
        setupScalarSelectOption(option);
        assertScalarSelectRequiredSpecifyColumn();
        return _executor.execute(_conditionBean, _resultType, selectClauseType);
    }

    protected void setupTargetColumnInfo(ScalarSelectOption option) {
        if (option == null) {
            return;
        }
        final SqlClause sqlClause = _conditionBean.getSqlClause();
        ColumnInfo columnInfo = sqlClause.getSpecifiedColumnInfoAsOne();
        if (columnInfo != null) {
            columnInfo = sqlClause.getSpecifiedDerivingColumnInfoAsOne();
        }
        option.xsetTargetColumnInfo(columnInfo);
    }

    protected void setupScalarSelectOption(ScalarSelectOption option) {
        if (option != null) {
            _conditionBean.xacceptScalarSelectOption(option);
            _conditionBean.localCQ().xregisterParameterOption(option);
        }
    }

    protected void assertScalarSelectRequiredSpecifyColumn() {
        final SqlClause sqlClause = _conditionBean.getSqlClause();
        final String columnName = sqlClause.getSpecifiedColumnDbNameAsOne();
        final String subQuery = sqlClause.getSpecifiedDerivingSubQueryAsOne();
        // should be specified is an only one object (column or sub-query)
        if ((columnName != null && subQuery != null) || (columnName == null && subQuery == null)) {
            throwScalarSelectInvalidColumnSpecificationException();
        }
    }

    protected void throwScalarSelectInvalidColumnSpecificationException() {
        createCBExThrower().throwScalarSelectInvalidColumnSpecificationException(_conditionBean, _resultType);
    }

    // ===================================================================================
    //                                                                       Assert Helper
    //                                                                       =============
    protected ScalarSelectOption createScalarSelectOption() {
        return newScalarSelectOption();
    }

    protected ScalarSelectOption newScalarSelectOption() {
        return new ScalarSelectOption();
    }

    protected BehaviorExceptionThrower createBhvExThrower() {
        return new BehaviorExceptionThrower();
    }

    protected ConditionBeanExceptionThrower createCBExThrower() {
        return new ConditionBeanExceptionThrower();
    }

    protected void assertScalarQuery(ScalarQuery<?> scalarQuery) {
        if (scalarQuery == null) {
            String msg = "The argument 'scalarQuery' for ScalarSelect should not be null.";
            throw new IllegalArgumentException(msg);
        }
    }

    protected void assertScalarSelectOption(ScalarSelectOption option) {
        if (option == null) {
            String msg = "The argument 'option' for ScalarSelect should not be null.";
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Assert that the object is not null.
     * @param variableName The variable name for message. (NotNull)
     * @param value The value the checked variable. (NotNull)
     * @throws IllegalArgumentException When the variable name or the variable is null.
     */
    protected void assertObjectNotNull(String variableName, Object value) {
        if (variableName == null) {
            String msg = "The value should not be null: variableName=null value=" + value;
            throw new IllegalArgumentException(msg);
        }
        if (value == null) {
            String msg = "The value should not be null: variableName=" + variableName;
            throw new IllegalArgumentException(msg);
        }
    }
}
