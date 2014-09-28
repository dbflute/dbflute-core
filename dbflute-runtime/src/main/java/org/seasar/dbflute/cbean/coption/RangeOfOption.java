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

import java.util.List;

import org.seasar.dbflute.cbean.ConditionBean;
import org.seasar.dbflute.cbean.SpecifyQuery;
import org.seasar.dbflute.cbean.chelper.HpCalcSpecification;
import org.seasar.dbflute.cbean.chelper.HpCalculator;
import org.seasar.dbflute.cbean.chelper.HpSpecifiedColumn;
import org.seasar.dbflute.cbean.cipher.GearedCipherManager;
import org.seasar.dbflute.cbean.ckey.ConditionKey;
import org.seasar.dbflute.cbean.sqlclause.query.QueryClauseArranger;
import org.seasar.dbflute.dbmeta.info.ColumnInfo;
import org.seasar.dbflute.dbway.ExtensionOperand;
import org.seasar.dbflute.dbway.StringConnector;
import org.seasar.dbflute.exception.IllegalConditionBeanOperationException;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.DfTypeUtil;

/**
 * The option of range-of scope for Number type.
 * @author jflute
 */
public class RangeOfOption implements ConditionOption, HpCalculator {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected boolean _greaterThan;
    protected boolean _lessThan;
    protected boolean _orIsNull;
    protected HpCalcSpecification<ConditionBean> _calcSpecification;

    // ===================================================================================
    //                                                                   Manual Adjustment
    //                                                                   =================
    // -----------------------------------------------------
    //                                               Operand
    //                                               -------
    /**
     * Set up operand for min number as greater-than. <br />
     * This is for manual adjustment.
     * @return this. (NotNull)
     */
    public RangeOfOption greaterThan() {
        _greaterThan = true;
        return this;
    }

    /**
     * Set up operand for max number as less-than. <br />
     * This is for manual adjustment.
     * @return this. (NotNull)
     */
    public RangeOfOption lessThan() {
        _lessThan = true;
        return this;
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
    public HpCalculator plus(Number plusValue) {
        assertObjectNotNull("plusValue", plusValue);
        initializeCalcSpecificationIfNeeds();
        return _calcSpecification.plus(plusValue);
    }

    /**
     * {@inheritDoc}
     */
    public HpCalculator plus(HpSpecifiedColumn plusColumn) {
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
    public HpCalculator minus(Number minusValue) {
        assertObjectNotNull("minusValue", minusValue);
        initializeCalcSpecificationIfNeeds();
        return _calcSpecification.minus(minusValue);
    }

    /**
     * {@inheritDoc}
     */
    public HpCalculator minus(HpSpecifiedColumn minusColumn) {
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
    public HpCalculator multiply(Number multiplyValue) {
        assertObjectNotNull("multiplyValue", multiplyValue);
        initializeCalcSpecificationIfNeeds();
        return _calcSpecification.multiply(multiplyValue);
    }

    /**
     * {@inheritDoc}
     */
    public HpCalculator multiply(HpSpecifiedColumn multiplyColumn) {
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
    public HpCalculator divide(Number divideValue) {
        assertObjectNotNull("divideValue", divideValue);
        initializeCalcSpecificationIfNeeds();
        return _calcSpecification.divide(divideValue);
    }

    /**
     * {@inheritDoc}
     */
    public HpCalculator divide(HpSpecifiedColumn divideColumn) {
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
    public HpCalculator convert(ColumnConversionOption option) {
        assertObjectNotNull("option", option);
        initializeCalcSpecificationIfNeeds();
        return _calcSpecification.convert(option);
    }

    /**
     * {@inheritDoc}
     */
    public HpCalculator left() {
        initializeCalcSpecificationIfNeeds();
        return _calcSpecification.left();
    }

    /**
     * {@inheritDoc}
     */
    public HpCalculator right() {
        initializeCalcSpecificationIfNeeds();
        return _calcSpecification.right();
    }

    protected void initializeCalcSpecificationIfNeeds() {
        if (_calcSpecification == null) {
            _calcSpecification = createCalcSpecification();
        }
    }

    protected void setupSelectDreamCruiseJourneyLogBookIfUnionExists(HpSpecifiedColumn column) {
        column.setupSelectDreamCruiseJourneyLogBookIfUnionExists();
    }

    protected HpCalcSpecification<ConditionBean> createCalcSpecification() {
        return new HpCalcSpecification<ConditionBean>(createEmptySpecifyQuery());
    }

    protected SpecifyQuery<ConditionBean> createEmptySpecifyQuery() {
        return new SpecifyQuery<ConditionBean>() {
            public void specify(ConditionBean cb) {
            }
        };
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

    protected void assertCalculationColumnNumber(HpSpecifiedColumn specifiedColumn) {
        final ColumnInfo columnInfo = specifiedColumn.getColumnInfo();
        if (columnInfo == null) { // basically not null but just in case
            return;
        }
        if (!columnInfo.isObjectNativeTypeNumber()) {
            String msg = "The type of the calculation column should be Number: " + specifiedColumn;
            throw new IllegalArgumentException(msg);
        }
    }

    protected void assertSpecifiedDreamCruiseTicket(HpSpecifiedColumn column) {
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
        sb.append(":{greaterThan=").append(_greaterThan).append(", lessThan=").append(_lessThan);
        sb.append(", orIsNull=").append(_orIsNull).append("}");
        return sb.toString();
    }
}
