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
import org.seasar.dbflute.cbean.coption.ColumnConversionOption;
import org.seasar.dbflute.dbmeta.info.ColumnInfo;
import org.seasar.dbflute.dbmeta.name.ColumnRealName;
import org.seasar.dbflute.dbmeta.name.ColumnSqlName;
import org.seasar.dbflute.exception.IllegalConditionBeanOperationException;

/**
 * @author jflute
 */
public class HpSpecifiedColumn implements HpCalculator {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _tableAliasName;
    protected final ColumnInfo _columnInfo; // required
    protected final ConditionBean _baseCB; // required
    protected final String _columnDirectName;
    protected final boolean _derived;
    protected HpSpecifiedColumn _mappedSpecifiedColumn;
    protected String _mappedDerivedAlias;
    protected String _onQueryName;
    protected HpCalcSpecification<ConditionBean> _calcSpecification;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public HpSpecifiedColumn(String tableAliasName, ColumnInfo columnInfo, ConditionBean baseCB) {
        assertColumnInfo(tableAliasName, columnInfo);
        assertBaseCB(tableAliasName, baseCB);
        _tableAliasName = tableAliasName;
        _columnInfo = columnInfo;
        _baseCB = baseCB;
        _columnDirectName = null;
        _derived = false;
    }

    public HpSpecifiedColumn(String tableAliasName, ColumnInfo columnInfo, ConditionBean baseCB,
            String columnDirectName, boolean derived) {
        assertColumnInfo(tableAliasName, columnInfo);
        assertBaseCB(tableAliasName, baseCB);
        _tableAliasName = tableAliasName;
        _columnInfo = columnInfo;
        _baseCB = baseCB;
        _columnDirectName = columnDirectName;
        _derived = derived;
    }

    protected void assertColumnInfo(String tableAliasName, ColumnInfo columnInfo) {
        if (columnInfo == null) {
            String msg = "The argument 'columnInfo' should not be null: tableAliasName=" + tableAliasName;
            throw new IllegalArgumentException(msg);
        }
    }

    protected void assertBaseCB(String tableAliasName, ConditionBean baseCB) {
        if (baseCB == null) {
            String msg = "The argument 'baseCB' should not be null: tableAliasName=" + tableAliasName;
            throw new IllegalArgumentException(msg);
        }
    }

    // ===================================================================================
    //                                                                        Dream Cruise
    //                                                                        ============
    public boolean isDreamCruiseTicket() {
        return _baseCB.xisDreamCruiseShip();
    }

    public void setupSelectDreamCruiseJourneyLogBookIfUnionExists() {
        if (!isDreamCruiseTicket()) {
            String msg = "This method is only allowed at Dream Cruise.";
            throw new IllegalConditionBeanOperationException(msg);
        }
        _baseCB.xsetupSelectDreamCruiseJourneyLogBookIfUnionExists();
    }

    // ===================================================================================
    //                                                                         Column Name
    //                                                                         ===========
    public String getColumnDbName() {
        return _columnInfo.getColumnDbName();
    }

    public ColumnSqlName toColumnSqlName() {
        return _columnDirectName != null ? new ColumnSqlName(_columnDirectName) : _columnInfo.getColumnSqlName();
    }

    public ColumnRealName toColumnRealName() {
        return ColumnRealName.create(_tableAliasName, toColumnSqlName());
    }

    // ===================================================================================
    //                                                                             Mapping
    //                                                                             =======
    public void mappedFrom(HpSpecifiedColumn mappedSpecifiedInfo) {
        _mappedSpecifiedColumn = mappedSpecifiedInfo;
    }

    public void mappedFromDerived(String mappedDerivedAlias) {
        _mappedDerivedAlias = mappedDerivedAlias;
    }

    public String getValidMappedOnQueryName() {
        if (_mappedSpecifiedColumn != null) {
            return _mappedSpecifiedColumn.getOnQueryName();
        } else if (_mappedDerivedAlias != null) {
            return _mappedDerivedAlias;
        } else {
            return null;
        }
    }

    // ===================================================================================
    //                                                                         Calculation
    //                                                                         ===========
    // SpecifyCalculation: basically for nested Calculation, DerivedReferrer
    /**
     * {@inheritDoc}
     */
    public HpSpecifiedColumn plus(Number plusValue) {
        assertObjectNotNull("plusValue", plusValue);
        initializeCalcSpecificationIfNeeds();
        _calcSpecification.plus(plusValue);
        // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
        // not return calcSpecification but this
        // for smart specification of DreamCruise
        // e.g.
        // subCB.specify().columnPurchasePrice()
        //    .multiply(dreamCruiseCB.specify().specifyMember().columnMemberId().plus(3));
        // _/_/_/_/_/_/_/_/_/_/
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public HpSpecifiedColumn plus(HpSpecifiedColumn plusColumn) {
        assertObjectNotNull("plusColumn", plusColumn);
        assertCalculationColumnNumber(plusColumn);
        assertSpecifiedDreamCruiseTicket(plusColumn);
        initializeCalcSpecificationIfNeeds();
        _calcSpecification.plus(plusColumn);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public HpSpecifiedColumn minus(Number minusValue) {
        assertObjectNotNull("minusValue", minusValue);
        initializeCalcSpecificationIfNeeds();
        _calcSpecification.minus(minusValue);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public HpSpecifiedColumn minus(HpSpecifiedColumn minusColumn) {
        assertObjectNotNull("minusColumn", minusColumn);
        assertCalculationColumnNumber(minusColumn);
        assertSpecifiedDreamCruiseTicket(minusColumn);
        initializeCalcSpecificationIfNeeds();
        _calcSpecification.minus(minusColumn);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public HpSpecifiedColumn multiply(Number multiplyValue) {
        assertObjectNotNull("multiplyValue", multiplyValue);
        initializeCalcSpecificationIfNeeds();
        _calcSpecification.multiply(multiplyValue);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public HpSpecifiedColumn multiply(HpSpecifiedColumn multiplyColumn) {
        assertObjectNotNull("multiplyColumn", multiplyColumn);
        assertCalculationColumnNumber(multiplyColumn);
        assertSpecifiedDreamCruiseTicket(multiplyColumn);
        initializeCalcSpecificationIfNeeds();
        _calcSpecification.multiply(multiplyColumn);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public HpSpecifiedColumn divide(Number divideValue) {
        assertObjectNotNull("divideValue", divideValue);
        initializeCalcSpecificationIfNeeds();
        _calcSpecification.divide(divideValue);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public HpSpecifiedColumn divide(HpSpecifiedColumn divideColumn) {
        assertObjectNotNull("divideColumn", divideColumn);
        assertCalculationColumnNumber(divideColumn);
        assertSpecifiedDreamCruiseTicket(divideColumn);
        initializeCalcSpecificationIfNeeds();
        _calcSpecification.divide(divideColumn);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public HpSpecifiedColumn convert(ColumnConversionOption option) {
        assertObjectNotNull("option", option);
        initializeCalcSpecificationIfNeeds();
        initializeConvOptionColumn(option);
        _calcSpecification.convert(option);
        return this;
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
            final ConditionBean handlingCB;
            if (_baseCB.xisDreamCruiseShip()) {
                // to tell option parameters by DreamCruise to base condition-bean
                handlingCB = _baseCB.xgetDreamCruiseDeparturePort();
            } else {
                handlingCB = _baseCB;
            }
            _calcSpecification.setBaseCB(handlingCB);
        }
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

    public boolean hasSpecifyCalculation() {
        return _calcSpecification != null;
    }

    public HpCalcSpecification<ConditionBean> getSpecifyCalculation() {
        return _calcSpecification;
    }

    public void xinitSpecifyCalculation() { // called by e.g. HpHpCalcSpecification, DerivedReferrer
        // specify the condition-bean that has this specified column
        _calcSpecification.specify(_baseCB);
    }

    protected void initializeConvOptionColumn(ColumnConversionOption option) {
        // e.g. DreamCruise specifies several columns
        // then cannot set the target column later so needs to set here
        // e.g. DerivedReferrer has only-one specified column so either way is OK
        option.xsetTargetColumnInfo(_columnInfo); // to judge correctly e.g. Date type
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
        final StringBuilder sb = new StringBuilder();
        sb.append("{").append(_tableAliasName).append(", ");
        if (_columnDirectName != null) {
            sb.append(_columnDirectName + ", ");
        }
        sb.append(_columnInfo).append("}");
        return sb.toString();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getTableAliasName() {
        return _tableAliasName;
    }

    public ColumnInfo getColumnInfo() {
        return _columnInfo;
    }

    public String getColumnDirectName() {
        return _columnDirectName;
    }

    public boolean isDerived() {
        return _derived;
    }

    public HpSpecifiedColumn getMappedSpecifiedInfo() {
        return _mappedSpecifiedColumn;
    }

    public String getMappedAliasName() {
        return _mappedDerivedAlias;
    }

    public String getOnQueryName() {
        return _onQueryName;
    }

    public void setOnQueryName(String onQueryName) {
        this._onQueryName = onQueryName;
    }
}
