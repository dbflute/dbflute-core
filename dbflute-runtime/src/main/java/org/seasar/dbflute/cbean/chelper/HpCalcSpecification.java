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

import java.util.List;
import java.util.Map;

import org.seasar.dbflute.cbean.ConditionBean;
import org.seasar.dbflute.cbean.SpecifyQuery;
import org.seasar.dbflute.cbean.chelper.HpCalcElement.CalculationType;
import org.seasar.dbflute.cbean.cipher.ColumnFunctionCipher;
import org.seasar.dbflute.cbean.coption.ColumnConversionOption;
import org.seasar.dbflute.dbmeta.info.ColumnInfo;
import org.seasar.dbflute.dbmeta.name.ColumnRealName;
import org.seasar.dbflute.dbmeta.name.ColumnSqlName;
import org.seasar.dbflute.exception.IllegalConditionBeanOperationException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.DfTypeUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @param <CB> The type of condition-bean for column specification. 
 * @author jflute
 */
public class HpCalcSpecification<CB extends ConditionBean> implements HpCalculator, HpCalcStatement {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The specify query call-back to specify column. (NotNull) */
    protected final SpecifyQuery<CB> _specifyQuery;

    /** The condition bean of target column to judge database type and save parameters of conversion. (NotNull after specify) */
    protected ConditionBean _baseCB;

    /** The specified condition bean to handle the specified column. (NotNull after specify) */
    protected CB _specifedCB;

    protected final List<HpCalcElement> _calculationList = DfCollectionUtil.newArrayList();
    protected boolean _leftMode;
    protected HpCalcSpecification<CB> _leftCalcSp;
    protected boolean _convert;
    protected boolean _synchronizeSetupSelectByJourneyLogBook;
    protected Object _mysticBindingSnapshot; // e.g. to determine binding type

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public HpCalcSpecification(SpecifyQuery<CB> specifyQuery) { // e.g. called by Update Calculation, ManualOrder, DerivedReferrer
        _specifyQuery = specifyQuery;
    }

    public HpCalcSpecification(SpecifyQuery<CB> specifyQuery, ConditionBean baseCB) { // e.g. called by ColumnQuery Calculation
        _specifyQuery = specifyQuery;
        _baseCB = baseCB;
    }

    // ===================================================================================
    //                                                                             Specify
    //                                                                             =======
    public void specify(CB cb) {
        _specifyQuery.specify(cb);
        _specifedCB = cb; // saves for handling the specified column
        if (_baseCB == null) { // means base CB is same as specified one
            _baseCB = cb;
        }
    }

    // -----------------------------------------------------
    //                                           Column Info
    //                                           -----------
    // if column info is null, cipher adjustment is unsupported
    /**
     * @return The column info of specified column. (NullAllowed)
     */
    public ColumnInfo getSpecifiedColumnInfo() { // only when plain (or dream cruise)
        checkSpecifiedCB();
        if (_specifedCB.xhasDreamCruiseTicket()) {
            final HpSpecifiedColumn dreamCruiseTicket = _specifedCB.xshowDreamCruiseTicket();
            return !dreamCruiseTicket.isDerived() ? dreamCruiseTicket.getColumnInfo() : null;
        }
        return _specifedCB.getSqlClause().getSpecifiedColumnInfoAsOne();
    }

    /**
     * @return The column info of specified deriving column. (NullAllowed)
     */
    public ColumnInfo getSpecifiedDerivingColumnInfo() { // only when deriving sub-query
        checkSpecifiedCB();
        if (_specifedCB.xhasDreamCruiseTicket()) {
            final HpSpecifiedColumn dreamCruiseTicket = _specifedCB.xshowDreamCruiseTicket();
            return dreamCruiseTicket.isDerived() ? dreamCruiseTicket.getColumnInfo() : null;
        }
        return _specifedCB.getSqlClause().getSpecifiedDerivingColumnInfoAsOne();
    }

    /**
     * @return The specified column of resolved column. (NullAllowed)
     */
    public HpSpecifiedColumn getResolvedSpecifiedColumn() { // resolved plain or deriving sub-query
        checkSpecifiedCB();
        if (_specifedCB.xhasDreamCruiseTicket()) {
            final HpSpecifiedColumn dreamCruiseTicket = _specifedCB.xshowDreamCruiseTicket();
            return !dreamCruiseTicket.isDerived() ? dreamCruiseTicket : null;
        }
        return _specifedCB.getSqlClause().getSpecifiedColumnAsOne();
    }

    /**
     * @return The column info of specified resolved column. (NullAllowed)
     */
    public ColumnInfo getResolvedSpecifiedColumnInfo() { // resolved plain or deriving sub-query
        checkSpecifiedCB();
        final ColumnInfo columnInfo = getSpecifiedColumnInfo();
        return columnInfo != null ? columnInfo : getSpecifiedDerivingColumnInfo();
    }

    // -----------------------------------------------------
    //                                           Column Name
    //                                           -----------
    /**
     * @return The column DB name of specified resolved column. (NullAllowed)
     */
    public String getResolvedSpecifiedColumnDbName() { // resolved plain or deriving sub-query
        checkSpecifiedCB();
        if (_specifedCB.xhasDreamCruiseTicket()) {
            final HpSpecifiedColumn ticket = _specifedCB.xshowDreamCruiseTicket();
            return ticket.getColumnDbName();
        }
        final ColumnInfo columnInfo = getResolvedSpecifiedColumnInfo();
        return columnInfo != null ? columnInfo.getColumnDbName() : null;
    }

    /**
     * @return The column SQL name of specified resolved column. (NullAllowed)
     */
    protected ColumnSqlName getResolvedSpecifiedColumnSqlName() { // resolved plain or deriving sub-query
        // Basically for UpdateOption, No SpecifyCalculation.
        checkSpecifiedCB();
        if (_specifedCB.xhasDreamCruiseTicket()) {
            final HpSpecifiedColumn ticket = _specifedCB.xshowDreamCruiseTicket();
            return ticket.toColumnSqlName();
        }
        return _specifedCB.getSqlClause().getSpecifiedResolvedColumnSqlNameAsOne();
    }

    /**
     * @return The column real name of specified resolved column. (NullAllowed)
     */
    public ColumnRealName getResolvedSpecifiedColumnRealName() { // resolved plain or deriving sub-query and calculation
        checkSpecifiedCB();
        if (_specifedCB.xhasDreamCruiseTicket()) {
            final HpSpecifiedColumn ticket = _specifedCB.xshowDreamCruiseTicket();
            return ticket.toColumnRealName();
        }
        return _specifedCB.getSqlClause().getSpecifiedResolvedColumnRealNameAsOne();
    }

    // -----------------------------------------------------
    //                                           Table Alias
    //                                           -----------
    public String getResolvedSpecifiedTableAliasName() { // resolved plain or deriving sub-query
        checkSpecifiedCB();
        if (_specifedCB.xhasDreamCruiseTicket()) {
            final HpSpecifiedColumn ticket = _specifedCB.xshowDreamCruiseTicket();
            return ticket.getTableAliasName();
        }
        final ColumnRealName columnRealName = _specifedCB.getSqlClause().getSpecifiedColumnRealNameAsOne();
        if (columnRealName != null) {
            return columnRealName.getTableAliasName();
        }
        return _specifedCB.getSqlClause().getSpecifiedDerivingAliasNameAsOne();
    }

    // -----------------------------------------------------
    //                                          Check Status
    //                                          ------------
    protected void checkSpecifiedCB() {
        if (_specifedCB == null) {
            throwSpecifiedConditionBeanNotFoundException();
        }
    }

    protected void throwSpecifiedConditionBeanNotFoundException() {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the specified condition-bean.");
        br.addItem("Advice");
        br.addElement("You should call specify(cb) before building statements.");
        br.addItem("Specify Query");
        br.addElement(_specifyQuery);
        br.addItem("Calculation List");
        if (!_calculationList.isEmpty()) {
            for (HpCalcElement element : _calculationList) {
                br.addElement(element);
            }
        } else {
            br.addElement("*No calculation");
        }
        final String msg = br.buildExceptionMessage();
        throw new IllegalConditionBeanOperationException(msg);
    }

    // ===================================================================================
    //                                                                         Calculation
    //                                                                         ===========
    /**
     * {@inheritDoc}
     */
    public HpCalculator plus(Number plusValue) {
        assertObjectNotNull("plusValue", plusValue);
        if (_leftMode) {
            assertLeftCalcSp();
            _leftCalcSp.plus(plusValue); // dispatch to nested one
            return this;
        } else {
            return register(CalculationType.PLUS, plusValue); // main process
        }
    }

    /**
     * {@inheritDoc}
     */
    public HpCalculator plus(HpSpecifiedColumn plusColumn) {
        assertObjectNotNull("plusColumn", plusColumn);
        assertCalculationColumnNumber(plusColumn);
        assertSpecifiedDreamCruiseTicket(plusColumn);
        if (_leftMode) {
            assertLeftCalcSp();
            _leftCalcSp.plus(plusColumn); // dispatch to nested one
            return this;
        } else {
            return register(CalculationType.PLUS, plusColumn); // main process
        }
    }

    /**
     * {@inheritDoc}
     */
    public HpCalculator minus(Number minusValue) {
        assertObjectNotNull("minusValue", minusValue);
        if (_leftMode) {
            assertLeftCalcSp();
            _leftCalcSp.minus(minusValue); // dispatch to nested one
            return this;
        } else {
            return register(CalculationType.MINUS, minusValue); // main process
        }
    }

    /**
     * {@inheritDoc}
     */
    public HpCalculator minus(HpSpecifiedColumn minusColumn) {
        assertObjectNotNull("minusColumn", minusColumn);
        assertCalculationColumnNumber(minusColumn);
        assertSpecifiedDreamCruiseTicket(minusColumn);
        if (_leftMode) {
            assertLeftCalcSp();
            _leftCalcSp.plus(minusColumn); // dispatch to nested one
            return this;
        } else {
            return register(CalculationType.MINUS, minusColumn); // main process
        }
    }

    /**
     * {@inheritDoc}
     */
    public HpCalculator multiply(Number multiplyValue) {
        assertObjectNotNull("multiplyValue", multiplyValue);
        if (_leftMode) {
            assertLeftCalcSp();
            _leftCalcSp.multiply(multiplyValue); // dispatch to nested one
            return this;
        } else {
            return register(CalculationType.MULTIPLY, multiplyValue); // main process
        }
    }

    /**
     * {@inheritDoc}
     */
    public HpCalculator multiply(HpSpecifiedColumn multiplyColumn) {
        assertObjectNotNull("multiplyColumn", multiplyColumn);
        assertCalculationColumnNumber(multiplyColumn);
        assertSpecifiedDreamCruiseTicket(multiplyColumn);
        if (_leftMode) {
            assertLeftCalcSp();
            _leftCalcSp.multiply(multiplyColumn); // dispatch to nested one
            return this;
        } else {
            return register(CalculationType.MULTIPLY, multiplyColumn); // main process
        }
    }

    /**
     * {@inheritDoc}
     */
    public HpCalculator divide(Number divideValue) {
        assertObjectNotNull("divideValue", divideValue);
        if (_leftMode) {
            assertLeftCalcSp();
            _leftCalcSp.divide(divideValue); // dispatch to nested one
            return this;
        } else {
            return register(CalculationType.DIVIDE, divideValue); // main process
        }
    }

    /**
     * {@inheritDoc}
     */
    public HpCalculator divide(HpSpecifiedColumn divideColumn) {
        assertObjectNotNull("divideColumn", divideColumn);
        assertCalculationColumnNumber(divideColumn);
        assertSpecifiedDreamCruiseTicket(divideColumn);
        if (_leftMode) {
            assertLeftCalcSp();
            _leftCalcSp.divide(divideColumn); // dispatch to nested one
            return this;
        } else {
            return register(CalculationType.DIVIDE, divideColumn); // main process
        }
    }

    protected HpCalculator register(CalculationType type, Number value) {
        assertObjectNotNull("type", type);
        if (value == null) {
            String msg = "The null value was specified as " + type + ".";
            throw new IllegalArgumentException(msg);
        }
        final HpCalcElement calculation = new HpCalcElement();
        calculation.setCalculationType(type);
        calculation.setCalculationValue(value);
        _calculationList.add(calculation);
        return this;
    }

    protected HpCalculator register(CalculationType type, HpSpecifiedColumn column) {
        assertObjectNotNull("type", type);
        if (column == null) {
            String msg = "The null column was specified as " + type + ".";
            throw new IllegalArgumentException(msg);
        }
        final HpCalcElement calculation = new HpCalcElement();
        calculation.setCalculationType(type);
        calculation.setCalculationColumn(column);
        _calculationList.add(calculation);
        setupSelectDreamCruiseJourneyLogBookIfUnionExists(column);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public HpCalculator convert(ColumnConversionOption option) {
        assertObjectNotNull("option", option);
        if (_leftMode) {
            assertLeftCalcSp();
            _leftCalcSp.convert(option); // dispatch to nested one
            return this;
        } else {
            return registerConv(option); // main process
        }
    }

    protected HpCalculator registerConv(ColumnConversionOption option) {
        if (option == null) {
            String msg = "The null value was specified as conversion option.";
            throw new IllegalArgumentException(msg);
        }
        final HpCalcElement calculation = new HpCalcElement();
        calculation.setCalculationType(CalculationType.CONV);
        calculation.setColumnConversionOption(option);
        _calculationList.add(calculation);
        // called later for VaryingUpdate
        //prepareConvOption(option);
        _convert = true;
        return this;
    }

    protected void prepareConvOption(ColumnConversionOption option) {
        option.xjudgeDatabase(_baseCB.getSqlClause());
        if (option.xgetTargetColumnInfo() == null) {
            // might be already set (e.g. HpSpecifiedColumn's convert(), see the comment)
            // DreamCruise specifies several columns so cannot get here so checked
            final ColumnInfo columnInfo = getResolvedSpecifiedColumnInfo();
            option.xsetTargetColumnInfo(columnInfo); // can be set if specified once
        }
        if (_mysticBindingSnapshot != null && option.xgetMysticBindingSnapshot() == null) {
            option.xsetMysticBindingSnapshot(_mysticBindingSnapshot); // can be set if specified once
        }
        _baseCB.localCQ().xregisterParameterOption(option);
    }

    protected void assertLeftCalcSp() {
        if (_leftCalcSp == null) {
            throwCalculationLeftColumnUnsupportedException();
        }
    }

    protected void throwCalculationLeftColumnUnsupportedException() {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The calculation for left column is unsupported at the function.");
        br.addItem("Advice");
        br.addElement("For example, ColumnQuery supports it);");
        br.addElement("but UpdateOption does not.");
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg);
    }

    // ===================================================================================
    //                                                                     Left/Right Mode
    //                                                                     ===============
    /**
     * {@inheritDoc}
     */
    public HpCalculator left() {
        _leftMode = true;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public HpCalculator right() {
        _leftMode = false;
        return this;
    }

    // ===================================================================================
    //                                                                           Statement
    //                                                                           =========
    /**
     * {@inheritDoc}
     */
    public String buildStatementAsSqlName(String aliasName) { // e.g. VaryingUpdate, VaryingQueryUdpate
        final ColumnSqlName columnSqlName = getResolvedSpecifiedColumnSqlName();
        final String columnExp = (aliasName != null ? aliasName : "") + columnSqlName.toString();
        final boolean removeCalcAlias = aliasName == null;
        return doBuildStatement(columnExp, null, removeCalcAlias);
    }

    /**
     * {@inheritDoc}
     */
    public String buildStatementToSpecifidName(String columnExp) { // e.g. ColumnQuery, DerivedReferrer
        return doBuildStatement(columnExp, null, false);
    }

    /**
     * {@inheritDoc}
     */
    public String buildStatementToSpecifidName(String columnExp, Map<String, String> columnAliasMap) { // e.g. ManualOrder
        return doBuildStatement(columnExp, columnAliasMap, false);
    }

    protected String doBuildStatement(String columnExp, Map<String, String> columnAliasMap, boolean removeCalcAlias) {
        if (_calculationList.isEmpty()) {
            return null;
        }
        // columnAliasMap means, e.g. union, already handled cipher
        String targetExp = columnAliasMap != null ? columnExp : decryptIfNeeds(columnExp);
        int index = 0;
        final boolean firstEnclosing = needsFirstEnclosing(targetExp);
        for (HpCalcElement calculation : _calculationList) {
            if (index > 0 || (index == 0 && firstEnclosing)) {
                targetExp = "(" + targetExp + ")";
            }
            if (!calculation.isPreparedConvOption()) {
                final ColumnConversionOption option = calculation.getColumnConversionOption();
                if (option != null) {
                    prepareConvOption(option);
                    calculation.setPreparedConvOption(true);
                }
            }
            targetExp = buildCalculationExp(targetExp, columnAliasMap, calculation, removeCalcAlias);
            ++index;
        }
        return targetExp;
    }

    protected boolean needsFirstEnclosing(String targetExp) {
        if (targetExp == null) { // just in case
            return false;
        }
        final String checkedStr;
        if (targetExp.contains(")")) {
            // e.g. (select ...) here, coalesce(...) here 
            // not accurate but small problem
            // cannot use Srl.isQuotedAnything() because targetExp might be '(select ...)--#df:...'
            // (checking identity is no point but identity does not contain operand)
            checkedStr = Srl.substringLastRear(targetExp, ")"); // after scope end e.g. (select ...) here
        } else {
            checkedStr = targetExp; // e.g. normal column
        }
        return Srl.containsAny(checkedStr, " + ", " - ", " * ", " / ");
    }

    /**
     * @param targetExp The expression of target column already handled cipher. (NotNull)
     * @param columnAliasMap The map of column alias. (NullAllowed)
     * @param calculation The element of calculation. (NotNull)
     * @param removeCalcAlias Does it remove alias of calculation column.
     * @return The expression of calculation statement. (NotNull)
     */
    protected String buildCalculationExp(String targetExp, Map<String, String> columnAliasMap,
            HpCalcElement calculation, boolean removeCalcAlias) {
        final CalculationType calculationType = calculation.getCalculationType();
        if (calculationType.equals(CalculationType.CONV)) { // convert
            final ColumnConversionOption columnConversionOption = calculation.getColumnConversionOption();
            return columnConversionOption.filterFunction(targetExp);
        }
        // number value or number column here
        final Object calcValueExp;
        if (calculation.hasCalculationValue()) { // number value
            calcValueExp = calculation.getCalculationValue();
        } else if (calculation.hasCalculationColumn()) { // number column
            final HpSpecifiedColumn calculationColumn = calculation.getCalculationColumn();
            final String columnExp;
            if (removeCalcAlias) { // means e.g. plain update
                final String basePointAliasName = _baseCB.getSqlClause().getBasePointAliasName();
                if (!basePointAliasName.equals(calculationColumn.getTableAliasName())) { // may be relation column
                    throwCalculationColumnRelationUnresolvedException(targetExp, calculationColumn);
                }
                columnExp = calculationColumn.toColumnSqlName().toString();
            } else {
                columnExp = calculationColumn.toColumnRealName().toString();
            }
            final Object baseExp;
            if (columnAliasMap != null) { // e.g. ManualOrder on union
                final String mappedAlias = columnAliasMap.get(columnExp);
                baseExp = mappedAlias != null ? mappedAlias : columnExp;
            } else { // e.g. ColumnQuery, UpdateOption, non-union ManualOrder, DerivedReferrer
                final ColumnInfo columnInfo = calculationColumn.getColumnInfo();
                baseExp = !calculationColumn.isDerived() ? decryptIfNeeds(columnInfo, columnExp) : columnExp;
            }
            calcValueExp = filterNestedCalculation(baseExp, calculationColumn);
        } else {
            throwCalculationElementIllegalStateException(targetExp);
            return null; // unreachable
        }
        return targetExp + " " + calculationType.operand() + " " + calcValueExp;
    }

    protected String decryptIfNeeds(String valueExp) {
        return decryptIfNeeds(getSpecifiedColumnInfo(), valueExp);
    }

    protected String decryptIfNeeds(ColumnInfo columnInfo, String valueExp) {
        if (columnInfo == null) { // means sub-query
            return valueExp;
        }
        final ColumnFunctionCipher cipher = _baseCB.getSqlClause().findColumnFunctionCipher(columnInfo);
        return cipher != null ? cipher.decrypt(valueExp) : valueExp;
    }

    protected Object filterNestedCalculation(Object specifiedRealName, HpSpecifiedColumn hpCol) {
        if (hpCol != null && hpCol.hasSpecifyCalculation()) {
            hpCol.xinitSpecifyCalculation();
            final HpCalcSpecification<ConditionBean> calcSpecification = hpCol.getSpecifyCalculation();
            return "(" + calcSpecification.buildStatementToSpecifidName(specifiedRealName.toString()) + ")";
        }
        return specifiedRealName;
    }

    protected void throwCalculationColumnRelationUnresolvedException(String targetExp,
            HpSpecifiedColumn calculationColumn) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The relation for the calculation column was unresolved.");
        br.addItem("Advice");
        br.addElement("For example, you cannot use relation columns for calculation");
        br.addElement("on set clause of update. (because of relation unresolved)");
        br.addItem("Base ConditionBean");
        br.addElement(_baseCB.getClass().getName());
        br.addItem("Specified Column");
        br.addElement(targetExp);
        br.addItem("Calculation Column");
        br.addElement(calculationColumn);
        final String msg = br.buildExceptionMessage();
        throw new IllegalConditionBeanOperationException(msg);
    }

    protected void throwCalculationElementIllegalStateException(String targetExp) {
        String msg = "The either calculationValue or calculationColumn should exist: targetExp=" + targetExp;
        throw new IllegalStateException(msg);
    }

    // ===================================================================================
    //                                                                       Determination
    //                                                                       =============
    public boolean isSpecifyColumn() {
        return getSpecifiedColumnInfo() != null;
    }

    public boolean isDerivedReferrer() {
        return getSpecifiedDerivingColumnInfo() != null;
    }

    public boolean mayNullRevived() { // basically for auto-detect of inner-join
        if ((_specifedCB != null && _specifedCB.xhasDreamCruiseTicket()) || isDerivedReferrer()) {
            return true; // because it is so difficult to judge it accurately
        }
        for (HpCalcElement calcElement : _calculationList) {
            if (calcElement.getCalculationColumn() != null) {
                return true; // because it is so difficult to judge it accurately
            }
            final ColumnConversionOption option = calcElement.getColumnConversionOption();
            if (option != null && option.mayNullRevived()) {
                return true; // e.g. coalesce()
            }
        }
        return false;
    }

    // ===================================================================================
    //                                                                     Journey LogBook
    //                                                                     ===============
    public void synchronizeSetupSelectByJourneyLogBook() {
        _synchronizeSetupSelectByJourneyLogBook = true;
        if (_leftCalcSp != null) {
            _leftCalcSp.synchronizeSetupSelectByJourneyLogBook();
        }
    }

    protected void setupSelectDreamCruiseJourneyLogBookIfUnionExists(HpSpecifiedColumn column) {
        if (!_synchronizeSetupSelectByJourneyLogBook) {
            return;
        }
        // to synchronize setupSelect if union already exists
        // basically for ManualOrderCalculation with Union
        // union needs alias name defined in select-clause on order-by clause
        // e.g.
        // cb.union(new UnionQuery<MemberCB>() {
        //     public void query(MemberCB unionCB) {
        //     }
        // });
        // MemberCB dreamCruiseCB = cb.dreamCruiseCB();
        // ManualOrderBean mob = new ManualOrderBean();
        // mob.multiply(dreamCruiseCB.specify().specifyMemberServiceAsOne().columnServicePointCount());
        column.setupSelectDreamCruiseJourneyLogBookIfUnionExists();
    }

    // ===================================================================================
    //                                                                       Assert Helper
    //                                                                       =============
    // -----------------------------------------------------
    //                                         Assert Object
    //                                         -------------
    /**
     * Assert that the object is not null.
     * @param variableName Variable name. (NotNull)
     * @param value Value. (NotNull)
     * @exception IllegalArgumentException
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
        return title + ":{left=" + _leftMode + ", convert=" + _convert + ", logBook="
                + _synchronizeSetupSelectByJourneyLogBook + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public SpecifyQuery<CB> getSpecifyQuery() {
        return _specifyQuery;
    }

    public void setBaseCB(ConditionBean baseCB) {
        _baseCB = baseCB;
    }

    public boolean hasCalculation() {
        return !_calculationList.isEmpty();
    }

    public List<HpCalcElement> getCalculationList() {
        return _calculationList;
    }

    public boolean hasConvert() {
        return _convert;
    }

    public HpCalcSpecification<CB> getLeftCalcSp() {
        return _leftCalcSp;
    }

    public void setLeftCalcSp(HpCalcSpecification<CB> leftCalcSp) {
        _leftCalcSp = leftCalcSp;
    }

    public Object getMysticBindingSnapshot() {
        return _mysticBindingSnapshot;
    }

    public void setMysticBindingSnapshot(Object mysticBindingSnapshot) {
        _mysticBindingSnapshot = mysticBindingSnapshot;
    }
}
