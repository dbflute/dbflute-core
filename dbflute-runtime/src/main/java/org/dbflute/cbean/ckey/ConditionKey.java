/*
 * Copyright 2014-2019 the original author or authors.
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
package org.dbflute.cbean.ckey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.dbflute.cbean.ConditionBean;
import org.dbflute.cbean.chelper.HpCalcSpecification;
import org.dbflute.cbean.cipher.ColumnFunctionCipher;
import org.dbflute.cbean.cipher.GearedCipherManager;
import org.dbflute.cbean.coption.ConditionOption;
import org.dbflute.cbean.coption.RangeOfOption;
import org.dbflute.cbean.cvalue.ConditionValue;
import org.dbflute.cbean.cvalue.ConditionValue.CallbackProcessor;
import org.dbflute.cbean.cvalue.ConditionValue.QueryModeProvider;
import org.dbflute.cbean.dream.SpecifiedColumn;
import org.dbflute.cbean.sqlclause.query.QueryClause;
import org.dbflute.cbean.sqlclause.query.QueryClauseArranger;
import org.dbflute.cbean.sqlclause.query.StringQueryClause;
import org.dbflute.dbmeta.info.ColumnInfo;
import org.dbflute.dbmeta.name.ColumnRealName;
import org.dbflute.dbmeta.name.ColumnSqlName;
import org.dbflute.dbway.ExtensionOperand;
import org.dbflute.dbway.OnQueryStringConnector;
import org.dbflute.exception.IllegalConditionBeanOperationException;

/**
 * The abstract class of condition-key.
 * @author jflute
 * @author h-funaki modified resolveCompoundColumn() to be able to treat coalesce option.
 */
public abstract class ConditionKey implements Serializable {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The serial version UID for object serialization. (Default) */
    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    // -----------------------------------------------------
    //                                         Condition Key
    //                                         -------------
    /** The condition key of equal. */
    public static final ConditionKey CK_EQUAL = new ConditionKeyEqual();

    /** The condition key of notEqual as standard. */
    public static final ConditionKey CK_NOT_EQUAL_STANDARD = new ConditionKeyNotEqualStandard();

    /** The condition key of notEqual as tradition. */
    public static final ConditionKey CK_NOT_EQUAL_TRADITION = new ConditionKeyNotEqualTradition();

    /** The condition key of greaterThan. */
    public static final ConditionKey CK_GREATER_THAN = new ConditionKeyGreaterThan();

    /** The condition key of greaterThan with orIsNull. */
    public static final ConditionKey CK_GREATER_THAN_OR_IS_NULL = new ConditionKeyGreaterThanOrIsNull();

    /** The condition key of lessThan. */
    public static final ConditionKey CK_LESS_THAN = new ConditionKeyLessThan();

    /** The condition key of lessThan with orIsNull. */
    public static final ConditionKey CK_LESS_THAN_OR_IS_NULL = new ConditionKeyLessThanOrIsNull();

    /** The condition key of greaterEqual. */
    public static final ConditionKey CK_GREATER_EQUAL = new ConditionKeyGreaterEqual();

    /** The condition key of greaterEqual with orIsNull. */
    public static final ConditionKey CK_GREATER_EQUAL_OR_IS_NULL = new ConditionKeyGreaterEqualOrIsNull();

    /** The condition key of lessEqual. */
    public static final ConditionKey CK_LESS_EQUAL = new ConditionKeyLessEqual();

    /** The condition key of lessEqual with orIsNull. */
    public static final ConditionKey CK_LESS_EQUAL_OR_IS_NULL = new ConditionKeyLessEqualOrIsNull();

    /** The condition key of inScope. */
    public static final ConditionKey CK_IN_SCOPE = new ConditionKeyInScope();

    /** The condition key of notInScope. */
    public static final ConditionKey CK_NOT_IN_SCOPE = new ConditionKeyNotInScope();

    /** The condition key of likeSearch. */
    public static final ConditionKey CK_LIKE_SEARCH = new ConditionKeyLikeSearch();

    /** The condition key of notLikeSearch. */
    public static final ConditionKey CK_NOT_LIKE_SEARCH = new ConditionKeyNotLikeSearch();

    /** The condition key of isNull. */
    public static final ConditionKey CK_IS_NULL = new ConditionKeyIsNull();

    /** The condition key of isNullOrEmpty. */
    public static final ConditionKey CK_IS_NULL_OR_EMPTY = new ConditionKeyIsNullOrEmpty();

    /** The condition key of isNotNull. */
    public static final ConditionKey CK_IS_NOT_NULL = new ConditionKeyIsNotNull();

    /** Dummy-object for IsNull and IsNotNull and so on... */
    protected static final Object DUMMY_OBJECT = new Object();

    // -----------------------------------------------------
    //                                        Prepare Result
    //                                        --------------
    protected static final ConditionKeyPrepareResult RESULT_NEW_QUERY = ConditionKeyPrepareResult.NEW_QUERY;
    protected static final ConditionKeyPrepareResult RESULT_INVALID_QUERY = ConditionKeyPrepareResult.INVALID_QUERY;
    protected static final ConditionKeyPrepareResult RESULT_OVERRIDING_QUERY = ConditionKeyPrepareResult.OVERRIDING_QUERY;
    protected static final ConditionKeyPrepareResult RESULT_DUPLICATE_QUERY = ConditionKeyPrepareResult.DUPLICATE_QUERY;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // not final because of no time of refactoring...
    /** The key name of the condition. (NotNull: initialized in constructor of sub-class) */
    protected String _conditionKey;

    /** The string of operand, used in SQL. (NotNull: initialized in constructor of sub-class) */
    protected String _operand;

    // ===================================================================================
    //                                                                       Prepare Query
    //                                                                       =============
    /**
     * Prepare the query of the condition value to register the condition.
     * @param provider The provider of query mode. (NotNull)
     * @param cvalue The object of condition value. (NotNull)
     * @param value The value of the condition. (NotNull)
     * @return The result of the preparation for the condition key. (NotNull)
     */
    public ConditionKeyPrepareResult prepareQuery(final QueryModeProvider provider, final ConditionValue cvalue, final Object value) {
        return cvalue.process(new CallbackProcessor<ConditionKeyPrepareResult>() {
            public ConditionKeyPrepareResult process() {
                return doPrepareQuery(cvalue, value);
            }

            public QueryModeProvider getProvider() {
                return provider;
            }
        });
    }

    protected abstract ConditionKeyPrepareResult doPrepareQuery(ConditionValue cvalue, Object value);

    // -----------------------------------------------------
    //                                         Choose Result
    //                                         -------------
    protected ConditionKeyPrepareResult chooseResultAlreadyExists(boolean equalValue) {
        return equalValue ? RESULT_DUPLICATE_QUERY : RESULT_OVERRIDING_QUERY;
    }

    protected ConditionKeyPrepareResult chooseResultNonValue(ConditionValue cvalue) {
        return needsOverrideValue(cvalue) ? RESULT_DUPLICATE_QUERY : RESULT_NEW_QUERY;
    }

    protected ConditionKeyPrepareResult chooseResultNonFixedQuery(Object value) {
        return isInvalidNonFixedQuery(value) ? RESULT_INVALID_QUERY : RESULT_NEW_QUERY;
    }

    protected boolean isInvalidNonFixedQuery(Object value) {
        return value == null;
    }

    protected ConditionKeyPrepareResult chooseResultListQuery(Object value) {
        return isInvalidListQuery(value) ? RESULT_INVALID_QUERY : RESULT_NEW_QUERY;
    }

    protected boolean isInvalidListQuery(Object value) {
        return value == null || !(value instanceof List<?>) || ((List<?>) value).isEmpty();
    }

    // ===================================================================================
    //                                                                      Override Check
    //                                                                      ==============
    /**
     * Does it need to override the existing value to register the value? <br>
     * This should be called in CallbackProcessor for e.g. in-line query
     * @param cvalue The object of condition value. (NotNull)
     * @return The determination, true or false.
     */
    protected abstract boolean needsOverrideValue(ConditionValue cvalue);

    // ===================================================================================
    //                                                                        Where Clause
    //                                                                        ============
    /**
     * Add where clause.
     * @param provider The provider of query mode. (NotNull)
     * @param conditionList The list of condition. (NotNull)
     * @param columnRealName The real name of column. (NotNull)
     * @param cvalue The object of condition value. (NotNull)
     * @param cipher The cipher of column by function. (NullAllowed)
     * @param option The option of condition. (NullAllowed)
     */
    public void addWhereClause(final QueryModeProvider provider, final List<QueryClause> conditionList, final ColumnRealName columnRealName,
            final ConditionValue cvalue, // basic resources
            final ColumnFunctionCipher cipher, final ConditionOption option) { // optional resources
        cvalue.process(new CallbackProcessor<Void>() {
            public Void process() {
                doAddWhereClause(conditionList, columnRealName, cvalue, cipher, option);
                return null;
            }

            public QueryModeProvider getProvider() {
                return provider;
            }
        });
    }

    /**
     * Do adding where clause.
     * @param conditionList The list of condition. (NotNull)
     * @param columnRealName The real name of column. (NotNull)
     * @param cvalue The object of condition value. (NotNull)
     * @param cipher The cipher of column by function. (NullAllowed)
     * @param option The option of condition. (NullAllowed)
     */
    protected abstract void doAddWhereClause(List<QueryClause> conditionList, ColumnRealName columnRealName, ConditionValue cvalue,
            ColumnFunctionCipher cipher, ConditionOption option);

    // ===================================================================================
    //                                                                     Condition Value
    //                                                                     ===============
    /**
     * Setup condition value.
     * @param provider The provider of query mode. (NotNull)
     * @param cvalue The object of condition value. (NotNull)
     * @param value The native value of condition. (NullAllowed)
     * @param location The location on parameter comment. (NotNull)
     * @param option Condition option. (NullAllowed)
     */
    public void setupConditionValue(final QueryModeProvider provider, final ConditionValue cvalue, final Object value,
            final String location, final ConditionOption option) {
        cvalue.process(new CallbackProcessor<Void>() {
            public Void process() {
                doSetupConditionValue(cvalue, value, location, option);
                return null;
            }

            public QueryModeProvider getProvider() {
                return provider;
            }
        });
    }

    /**
     * Do setting up condition value.
     * @param cvalue The object of condition value. (NotNull)
     * @param value The native value of condition. (NullAllowed)
     * @param location The location on parameter comment. (NotNull)
     * @param option The option of condition. (NullAllowed)
     */
    protected abstract void doSetupConditionValue(ConditionValue cvalue, Object value, String location, ConditionOption option);

    // ===================================================================================
    //                                                                         Bind Clause
    //                                                                         ===========
    // -----------------------------------------------------
    //                                           Entry Point
    //                                           -----------
    /**
     * Build bind clause.
     * @param columnRealName The real name of column. (NotNull)
     * @param location The location on parameter comment. (NotNull)
     * @param cipher The cipher of column by function. (NullAllowed)
     * @param option The option of condition. (NullAllowed)
     * @return The query clause as bind clause. (NotNull)
     */
    protected QueryClause buildBindClause(ColumnRealName columnRealName, String location, ColumnFunctionCipher cipher,
            ConditionOption option) {
        return new StringQueryClause(doBuildBindClause(columnRealName, location, cipher, option));
    }

    /**
     * Build bind clause with orIsNull condition.
     * @param columnRealName The real name of column. (NotNull)
     * @param location The location on parameter comment. (NotNull)
     * @param cipher The cipher of column by function. (NullAllowed)
     * @param option The option of condition. (NullAllowed)
     * @return The query clause as bind clause. (NotNull)
     */
    protected QueryClause buildBindClauseOrIsNull(ColumnRealName columnRealName, String location, ColumnFunctionCipher cipher,
            ConditionOption option) {
        final String mainQuery = doBuildBindClause(columnRealName, location, cipher, option);
        final String clause = "(" + mainQuery + " or " + columnRealName + " is null)";
        return new StringQueryClause(clause);
    }

    protected String doBuildBindClause(ColumnRealName columnRealName, String location, ColumnFunctionCipher cipher,
            ConditionOption option) {
        final BindClauseResult result = resolveBindClause(columnRealName, location, cipher, option);
        return result.toBindClause();
    }

    /**
     * Build clause without value. (basically for isNull, isNotNull)
     * @param columnRealName The real name of column. (NotNull)
     * @return The query clause as bind clause. (NotNull)
     */
    protected QueryClause buildClauseWithoutValue(ColumnRealName columnRealName) {
        final String clause = columnRealName + " " + getOperand(); // no need to resolve cipher
        return new StringQueryClause(clause);
    }

    // -----------------------------------------------------
    //                                       Clause Resolver
    //                                       ---------------
    protected BindClauseResult resolveBindClause(ColumnRealName columnRealName, String location, ColumnFunctionCipher cipher,
            ConditionOption option) {
        final String basicBindExp = buildBindVariableExp(location, option);
        final String bindExp;
        final ColumnRealName resolvedColumn;
        {
            final ColumnRealName columnExp;
            if (cipher != null) {
                final String plainColumnExp = columnRealName.toString();
                final String decryptExp = cipher.decrypt(plainColumnExp);
                final boolean nonInvertible = plainColumnExp.equals(decryptExp);
                if (isBindEncryptAllowed(columnRealName, option, nonInvertible)) {
                    bindExp = cipher.encrypt(basicBindExp);
                    columnExp = columnRealName;
                } else { // needs to decrypt (invertible)
                    bindExp = basicBindExp;
                    columnExp = toColumnRealName(decryptExp);
                }
            } else { // mainly here
                bindExp = basicBindExp;
                columnExp = columnRealName;
            }
            resolvedColumn = resolveOptionalColumn(columnExp, option);
        }
        return createBindClauseResult(resolvedColumn, bindExp, option);
    }

    protected boolean isBindEncryptAllowed(ColumnRealName columnRealName, ConditionOption option, boolean nonInvertible) {
        if (isOutOfBindEncryptConditionKey()) { // e.g. LikeSearch
            return false; // regardless of invertible
        }
        if (nonInvertible) { // means it cannot decrypt
            return true;
        }
        // invertible here
        // basically decrypt but encrypt if it can be, for better performance
        final boolean possible = isPossibleBindEncryptConditionKey(); // e.g. Equal, NotEqual
        return possible && !hasColumnCollaboration(columnRealName, option); // means simple condition
    }

    protected String buildBindVariableExp(String location, ConditionOption option) {
        return "/*pmb." + location + "*/" + getBindVariableDummyValue();
    }

    protected String getBindVariableDummyValue() { // to override
        return null; // as default
    }

    protected boolean isOutOfBindEncryptConditionKey() { // to override
        return false; // as default
    }

    protected boolean isPossibleBindEncryptConditionKey() { // to override
        return false; // as default
    }

    protected ColumnRealName toColumnRealName(String columnSqlName) {
        return ColumnRealName.create(null, new ColumnSqlName(columnSqlName));
    }

    protected BindClauseResult createBindClauseResult(ColumnRealName columnExp, String bindExp, ConditionOption option) {
        final String operand = resolveOperand(option);
        final String rearOption = resolveRearOption(option);
        final BindClauseResult result = new BindClauseResult(columnExp, operand, bindExp, rearOption);
        result.setArranger(resolveWhereClauseArranger(option));
        return result;
    }

    protected String resolveOperand(ConditionOption option) {
        final String operand = extractExtOperand(option);
        return operand != null ? operand : getOperand();
    }

    protected String extractExtOperand(ConditionOption option) {
        final ExtensionOperand extOperand = option != null ? option.getExtensionOperand() : null;
        return extOperand != null ? extOperand.operand() : null;
    }

    protected String resolveRearOption(ConditionOption option) {
        return option != null ? option.getRearOption() : "";
    }

    protected QueryClauseArranger resolveWhereClauseArranger(ConditionOption option) {
        return option != null ? option.getWhereClauseArranger() : null;
    }

    // -----------------------------------------------------
    //                                       Optional Column
    //                                       ---------------
    protected ColumnRealName resolveOptionalColumn(ColumnRealName columnExp, ConditionOption option) {
        return resolveCalculationColumn(resolveCompoundColumn(columnExp, option), option);
    }

    protected boolean hasColumnCollaboration(ColumnRealName columnRealName, ConditionOption option) {
        return hasCalculationColumn(columnRealName, option) || hasCompoundColumn(columnRealName, option);
    }

    protected boolean hasCalculationColumn(ColumnRealName columnRealName, ConditionOption option) {
        return option != null && option instanceof RangeOfOption && ((RangeOfOption) option).hasCalculationRange();
    }

    protected ColumnRealName resolveCalculationColumn(ColumnRealName columnRealName, ConditionOption option) {
        if (option == null) {
            return columnRealName;
        }
        if (option instanceof RangeOfOption) {
            final RangeOfOption rangeOfOption = (RangeOfOption) option;
            if (rangeOfOption.hasCalculationRange()) {
                final HpCalcSpecification<ConditionBean> calculationRange = rangeOfOption.getCalculationRange();
                final String calculated = calculationRange.buildStatementToSpecifidName(columnRealName.toString());
                return toColumnRealName(calculated);
            }
        }
        return columnRealName;
    }

    protected boolean hasCompoundColumn(ColumnRealName columnRealName, ConditionOption option) {
        return option != null && !option.hasCompoundColumn();
    }

    protected ColumnRealName resolveCompoundColumn(ColumnRealName baseRealName, ConditionOption option) {
        if (option == null || !option.hasCompoundColumn()) {
            return baseRealName;
        }
        if (!option.hasStringConnector()) { // basically no way
            String msg = "The option should have string connector when compound column is specified: " + option;
            throw new IllegalConditionBeanOperationException(msg);
        }
        final List<SpecifiedColumn> compoundColumnList = option.getCompoundColumnList();
        final List<ColumnRealName> realNameList = new ArrayList<ColumnRealName>();
        realNameList.add(doResolveCompoundColumnOption(option, baseRealName)); // already cipher    
        for (SpecifiedColumn specifiedColumn : compoundColumnList) {
            realNameList.add(doResolveCompoundColumnOption(option, doResolveCompoundColumnCipher(option, specifiedColumn)));
        }
        final OnQueryStringConnector stringConnector = option.getStringConnector();
        final String connected = stringConnector.connect(realNameList.toArray());
        return ColumnRealName.create(null, new ColumnSqlName(connected));
    }

    protected ColumnRealName doResolveCompoundColumnOption(ConditionOption option, ColumnRealName columnRealName) {
        if (option.isNullCompoundedAsEmpty()) {
            return toColumnRealName("coalesce(" + columnRealName + ",\'\')");
        }
        return columnRealName;
    }

    protected ColumnRealName doResolveCompoundColumnCipher(ConditionOption option, SpecifiedColumn specifiedColumn) {
        final GearedCipherManager cipherManager = option.getGearedCipherManager();
        final ColumnRealName specifiedName = specifiedColumn.toColumnRealName();
        if (cipherManager != null && !specifiedColumn.isDerived()) {
            final ColumnInfo columnInfo = specifiedColumn.getColumnInfo();
            final ColumnFunctionCipher cipher = cipherManager.findColumnFunctionCipher(columnInfo);
            if (cipher != null) {
                return toColumnRealName(cipher.decrypt(specifiedName.toString()));
            } else {
                return specifiedName;
            }
        } else {
            return specifiedName;
        }
    }

    // -----------------------------------------------------
    //                                         Result Object
    //                                         -------------
    public static class BindClauseResult {
        protected final ColumnRealName _columnExp;
        protected final String _operand;
        protected final String _bindExp;
        protected final String _rearOption;
        protected QueryClauseArranger _arranger;

        public BindClauseResult(ColumnRealName columnExp, String operand, String bindExp, String rearOption) {
            _columnExp = columnExp;
            _operand = operand;
            _bindExp = bindExp;
            _rearOption = rearOption;
        }

        public String toBindClause() {
            final String clause;
            if (_arranger != null) {
                clause = _arranger.arrange(_columnExp, _operand, _bindExp, _rearOption);
            } else {
                clause = _columnExp + " " + _operand + " " + _bindExp + _rearOption;
            }
            return clause;
        }

        public ColumnRealName getColumnExp() {
            return _columnExp;
        }

        public String getOperand() {
            return _operand;
        }

        public String getBindExp() {
            return _bindExp;
        }

        public String getRearOption() {
            return _rearOption;
        }

        public QueryClauseArranger getArranger() {
            return _arranger;
        }

        public void setArranger(QueryClauseArranger arranger) {
            this._arranger = arranger;
        }
    }

    // ===================================================================================
    //                                                                       Null-able Key
    //                                                                       =============
    /**
     * Is the condition key null-able? (basically for join determination)
     * @return The determination, true or false.
     */
    public abstract boolean isNullaleKey();

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    /**
     * @return View-string of condition key information.
     */
    @Override
    public String toString() {
        return "ConditionKey:{" + getConditionKey() + " " + getOperand() + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    /**
     * Get condition-key.
     * @return Condition-key.
     */
    public String getConditionKey() {
        return _conditionKey;
    }

    /**
     * Get operand.
     * @return Operand.
     */
    public String getOperand() {
        return _operand;
    }
}
