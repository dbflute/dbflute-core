/*
 * Copyright 2014-2025 the original author or authors.
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
package org.dbflute.cbean.coption;

import java.util.List;

import org.dbflute.cbean.ConditionBean;
import org.dbflute.cbean.chelper.HpCalcSpecification;
import org.dbflute.cbean.cipher.GearedCipherManager;
import org.dbflute.cbean.ckey.ConditionKey;
import org.dbflute.cbean.dream.ColumnCalculator;
import org.dbflute.cbean.dream.SpecifiedColumn;
import org.dbflute.cbean.scoping.SpecifyQuery;
import org.dbflute.cbean.sqlclause.query.QueryClauseArranger;
import org.dbflute.dbmeta.info.ColumnInfo;
import org.dbflute.dbway.topic.ExtensionOperand;
import org.dbflute.dbway.topic.OnQueryStringConnector;
import org.dbflute.exception.IllegalConditionBeanOperationException;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.DfTypeUtil;

/**
 * The option of range-of scope for Number type.
 * @author jflute
 */
public class RangeOfOption implements ConditionOption, ColumnCalculator {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected boolean _greaterThan;
    protected boolean _lessThan;
    protected boolean _orIsNull;
    protected HpCalcSpecification<ConditionBean> _calcSpecification;

    /** Does it allow one-side only from-to? */
    protected boolean _oneSideAllowed;

    // ===================================================================================
    //                                                                   Manual Adjustment
    //                                                                   =================
    // -----------------------------------------------------
    //                                               Operand
    //                                               -------
    /**
     * Set up operand for min number as greater-than. <br>
     * This is for manual adjustment.
     * @return this. (NotNull)
     */
    public RangeOfOption greaterThan() {
        _greaterThan = true;
        return this;
    }

    /**
     * Set up operand for max number as less-than. <br>
     * This is for manual adjustment.
     * @return this. (NotNull)
     */
    public RangeOfOption lessThan() {
        _lessThan = true;
        return this;
    }

    public boolean isGreaterThan() { // basically for framework
        return _greaterThan;
    }

    public boolean isLessThan() { // basically for framework
        return _lessThan;
    }

    protected void clearOperand() {
        _greaterThan = false;
        _lessThan = false;
    }

    // ===================================================================================
    //                                                                      Plug-in Option
    //                                                                      ==============
    /**
     * Add 'or is null' to range-of conditions.
     * @return this. (NotNull)
     */
    public RangeOfOption orIsNull() {
        _orIsNull = true;
        return this;
    }

    /**
     * Does it add or-is-null to the condition?
     * @return The determination, true or false.
     */
    public boolean isOrIsNull() { // basically for framework
        return _orIsNull;
    }

    /**
     * Allow you to set one-side only condition. (null allowed) <br>
     * If you ignore null-or-empty query, you don't need to call this.
     * @return this. (NotNull)
     */
    public RangeOfOption allowOneSide() {
        _oneSideAllowed = true;
        return this;
    }

    /**
     * Does it allow you to set one-side only condition.
     * @return The determination, true or false.
     */
    public boolean isOneSideAllowed() { // basically for framework
        return _oneSideAllowed;
    }

    // ===================================================================================
    //                                                                       Internal Main
    //                                                                       =============
    /**
     * Get the condition-key of the min number.
     * @return The condition-key of the min number. (NotNull)
     */
    public ConditionKey getMinNumberConditionKey() {
        if (_greaterThan) {
            return _orIsNull ? ConditionKey.CK_GREATER_THAN_OR_IS_NULL : ConditionKey.CK_GREATER_THAN;
        } else { // as default
            return _orIsNull ? ConditionKey.CK_GREATER_EQUAL_OR_IS_NULL : ConditionKey.CK_GREATER_EQUAL;
        }
    }

    /**
     * Get the condition-key of the max number.
     * @return The condition-key of the max number. (NotNull)
     */
    public ConditionKey getMaxNumberConditionKey() {
        if (_lessThan) {
            return _orIsNull ? ConditionKey.CK_LESS_THAN_OR_IS_NULL : ConditionKey.CK_LESS_THAN;
        } else { // as default
            return _orIsNull ? ConditionKey.CK_LESS_EQUAL_OR_IS_NULL : ConditionKey.CK_LESS_EQUAL;
        }
    }

    // ===================================================================================
    //                                                                         Calculation
    //                                                                         ===========
    /**
     * {@inheritDoc}
     */
    public ColumnCalculator plus(Number plusValue) {
        assertObjectNotNull("plusValue", plusValue);
        initializeCalcSpecificationIfNeeds();
        return _calcSpecification.plus(plusValue);
    }

    /**
     * {@inheritDoc}
     */
    public ColumnCalculator plus(SpecifiedColumn plusColumn) {
        assertObjectNotNull("plusColumn", plusColumn);
        assertCalculationColumnNumber(plusColumn);
        assertSpecifiedDreamCruiseTicket(plusColumn);
        initializeCalcSpecificationIfNeeds();
        setupSelectDreamCruiseJourneyLogBookIfUnionExists(plusColumn);
        return _calcSpecification.plus(plusColumn);
    }

    /**
     * {@inheritDoc}
     */
    public ColumnCalculator minus(Number minusValue) {
        assertObjectNotNull("minusValue", minusValue);
        initializeCalcSpecificationIfNeeds();
        return _calcSpecification.minus(minusValue);
    }

    /**
     * {@inheritDoc}
     */
    public ColumnCalculator minus(SpecifiedColumn minusColumn) {
        assertObjectNotNull("minusColumn", minusColumn);
        assertCalculationColumnNumber(minusColumn);
        assertSpecifiedDreamCruiseTicket(minusColumn);
        initializeCalcSpecificationIfNeeds();
        setupSelectDreamCruiseJourneyLogBookIfUnionExists(minusColumn);
        return _calcSpecification.minus(minusColumn);
    }

    /**
     * {@inheritDoc}
     */
    public ColumnCalculator multiply(Number multiplyValue) {
        assertObjectNotNull("multiplyValue", multiplyValue);
        initializeCalcSpecificationIfNeeds();
        return _calcSpecification.multiply(multiplyValue);
    }

    /**
     * {@inheritDoc}
     */
    public ColumnCalculator multiply(SpecifiedColumn multiplyColumn) {
        assertObjectNotNull("multiplyColumn", multiplyColumn);
        assertCalculationColumnNumber(multiplyColumn);
        assertSpecifiedDreamCruiseTicket(multiplyColumn);
        initializeCalcSpecificationIfNeeds();
        setupSelectDreamCruiseJourneyLogBookIfUnionExists(multiplyColumn);
        return _calcSpecification.multiply(multiplyColumn);
    }

    /**
     * {@inheritDoc}
     */
    public ColumnCalculator divide(Number divideValue) {
        assertObjectNotNull("divideValue", divideValue);
        initializeCalcSpecificationIfNeeds();
        return _calcSpecification.divide(divideValue);
    }

    /**
     * {@inheritDoc}
     */
    public ColumnCalculator divide(SpecifiedColumn divideColumn) {
        assertObjectNotNull("divideColumn", divideColumn);
        assertCalculationColumnNumber(divideColumn);
        assertSpecifiedDreamCruiseTicket(divideColumn);
        initializeCalcSpecificationIfNeeds();
        setupSelectDreamCruiseJourneyLogBookIfUnionExists(divideColumn);
        return _calcSpecification.divide(divideColumn);
    }

    /**
     * {@inheritDoc}
     */
    public ColumnCalculator convert(FunctionFilterOptionCall<ColumnConversionOption> opLambda) {
        assertObjectNotNull("opLambda", opLambda);
        initializeCalcSpecificationIfNeeds();
        return _calcSpecification.convert(opLambda);
    }

    /**
     * {@inheritDoc}
     */
    public ColumnCalculator left() {
        initializeCalcSpecificationIfNeeds();
        return _calcSpecification.left();
    }

    /**
     * {@inheritDoc}
     */
    public ColumnCalculator right() {
        initializeCalcSpecificationIfNeeds();
        return _calcSpecification.right();
    }

    // -----------------------------------------------------
    //                            CalcSpecification Handling
    //                            --------------------------
    protected void initializeCalcSpecificationIfNeeds() {
        if (_calcSpecification == null) {
            _calcSpecification = createEmptyCalcSpecification();
        }
    }

    protected void setupSelectDreamCruiseJourneyLogBookIfUnionExists(SpecifiedColumn column) {
        column.setupSelectDreamCruiseJourneyLogBookIfUnionExists();
    }

    protected HpCalcSpecification<ConditionBean> createEmptyCalcSpecification() {
        return newCalcSpecification(createEmptySpecifyQuery());
    }

    protected SpecifyQuery<ConditionBean> createEmptySpecifyQuery() {
        return new SpecifyQuery<ConditionBean>() {
            public void specify(ConditionBean cb) {
            }
        };
    }

    protected HpCalcSpecification<ConditionBean> newCalcSpecification(SpecifyQuery<ConditionBean> specifyQuery) {
        return new HpCalcSpecification<ConditionBean>(specifyQuery);
    }

    public boolean hasCalculationRange() {
        return _calcSpecification != null;
    }

    public HpCalcSpecification<ConditionBean> getCalculationRange() {
        return _calcSpecification;
    }

    public void xinitCalculationRange(ConditionBean baseCB, ConditionBean dreamCruiseCB) {
        if (!dreamCruiseCB.xisDreamCruiseShip()) {
            String msg = "The CB was not dream cruise: " + dreamCruiseCB.getClass();
            throw new IllegalConditionBeanOperationException(msg);
        }
        _calcSpecification.setBaseCB(baseCB);
        _calcSpecification.specify(dreamCruiseCB);
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

    public List<SpecifiedColumn> getCompoundColumnList() {
        return DfCollectionUtil.emptyList();
    }

    public boolean hasStringConnector() {
        return false;
    }

    public OnQueryStringConnector getStringConnector() {
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

    protected void assertCalculationColumnNumber(SpecifiedColumn specifiedColumn) {
        final ColumnInfo columnInfo = specifiedColumn.getColumnInfo();
        if (columnInfo == null) { // basically not null but just in case
            return;
        }
        if (!columnInfo.isObjectNativeTypeNumber()) {
            String msg = "The type of the calculation column should be Number: " + specifiedColumn;
            throw new IllegalArgumentException(msg);
        }
    }

    protected void assertSpecifiedDreamCruiseTicket(SpecifiedColumn column) {
        if (!column.isDreamCruiseTicket()) {
            final String msg = "The specified column was not dream cruise ticket: " + column;
            throw new IllegalConditionBeanOperationException(msg);
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
        sb.append(":{greaterThan=").append(_greaterThan);
        sb.append(", lessThan=").append(_lessThan);
        sb.append(", orIsNull=").append(_orIsNull);
        sb.append(", allowOneSide=").append(_oneSideAllowed);
        sb.append(", calcSpec=").append(_calcSpecification).append("}");
        return sb.toString();
    }
}
