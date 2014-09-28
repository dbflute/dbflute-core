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
package org.seasar.dbflute.cbean.ckey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.dbflute.cbean.ConditionBean;
import org.seasar.dbflute.cbean.chelper.HpCalcSpecification;
import org.seasar.dbflute.cbean.chelper.HpSpecifiedColumn;
import org.seasar.dbflute.cbean.cipher.ColumnFunctionCipher;
import org.seasar.dbflute.cbean.cipher.GearedCipherManager;
import org.seasar.dbflute.cbean.coption.ConditionOption;
import org.seasar.dbflute.cbean.coption.RangeOfOption;
import org.seasar.dbflute.cbean.cvalue.ConditionValue;
import org.seasar.dbflute.cbean.cvalue.ConditionValue.CallbackProcessor;
import org.seasar.dbflute.cbean.cvalue.ConditionValue.QueryModeProvider;
import org.seasar.dbflute.cbean.sqlclause.query.QueryClause;
import org.seasar.dbflute.cbean.sqlclause.query.QueryClauseArranger;
import org.seasar.dbflute.cbean.sqlclause.query.StringQueryClause;
import org.seasar.dbflute.dbmeta.info.ColumnInfo;
import org.seasar.dbflute.dbmeta.name.ColumnRealName;
import org.seasar.dbflute.dbmeta.name.ColumnSqlName;
import org.seasar.dbflute.dbway.ExtensionOperand;
import org.seasar.dbflute.dbway.StringConnector;
import org.seasar.dbflute.exception.IllegalConditionBeanOperationException;

/**
 * The abstract class of condition-key.
 * @author jflute
 */
public abstract class ConditionKey implements Serializable {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Serial version UID. (Default) */
    private static final long serialVersionUID = 1L;

    /** Log-instance. */
    private static final Log _log = LogFactory.getLog(ConditionKey.class);

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
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

    /**
     * Is the condition key null-able?
     * @param key The condition key. (NotNull)
     * @return The determination, true or false.
     */
    public static boolean isNullaleConditionKey(ConditionKey key) {
        return CK_GREATER_EQUAL_OR_IS_NULL.equals(key) || CK_GREATER_THAN_OR_IS_NULL.equals(key)
                || CK_LESS_EQUAL_OR_IS_NULL.equals(key) || CK_LESS_THAN_OR_IS_NULL.equals(key)
                || CK_IS_NULL.equals(key) || CK_IS_NULL_OR_EMPTY.equals(key);
    }

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** Condition-key. */
    protected String _conditionKey;

    /** Operand. */
    protected String _operand;

    // ===================================================================================
    //                                                                       Prepare Query
    //                                                                       =============
    /**
     * Prepare the query of the condition value to register the condition.
     * @param provider The provider of query mode. (NotNull)
     * @param cvalue The object of condition value. (NotNull)
     * @param value The value of the condition. (NotNull)
     * @param callerName Caller's real name. (NotNull)
     * @return Is the query valid?
     */
    public boolean prepareQuery(final QueryModeProvider provider, final ConditionValue cvalue, final Object value,
            final ColumnRealName callerName) {
        return cvalue.process(new CallbackProcessor<Boolean>() {
            public Boolean process() {
                return doPrepareQuery(cvalue, value, callerName);
            }

            public QueryModeProvider getProvider() {
                return provider;
            }
        });
    }

    protected abstract boolean doPrepareQuery(ConditionValue cvalue, Object value, ColumnRealName callerName);

    // ===================================================================================
    //                                                                      Override Check
    //                                                                      ==============
    /**
     * Does it need to override the existing value to register the value?
     * @param cvalue The object of condition value. (NotNull)
     * @return The determination, true or false.
     */
    public abstract boolean needsOverrideValue(ConditionValue cvalue);

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
    public void addWhereClause(final QueryModeProvider provider, final List<QueryClause> conditionList,
            final ColumnRealName columnRealName, final ConditionValue cvalue, // basic resources
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
    protected abstract void doAddWhereClause(List<QueryClause> conditionList, ColumnRealName columnRealName,
            ConditionValue cvalue, ColumnFunctionCipher cipher, ConditionOption option);

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
    protected abstract void doSetupConditionValue(ConditionValue cvalue, Object value, String location,
            ConditionOption option);

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
    protected QueryClause buildBindClauseOrIsNull(ColumnRealName columnRealName, String location,
            ColumnFunctionCipher cipher, ConditionOption option) {
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
    protected BindClauseResult resolveBindClause(ColumnRealName columnRealName, String location,
            ColumnFunctionCipher cipher, ConditionOption option) {
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
        final List<HpSpecifiedColumn> compoundColumnList = option.getCompoundColumnList();
        final List<ColumnRealName> realNameList = new ArrayList<ColumnRealName>();
        realNameList.add(baseRealName); // already cipher
        for (HpSpecifiedColumn specifiedColumn : compoundColumnList) {
            realNameList.add(doResolveCompoundColumn(option, specifiedColumn));
        }
        final StringConnector stringConnector = option.getStringConnector();
        final String connected = stringConnector.connect(realNameList.toArray());
        return ColumnRealName.create(null, new ColumnSqlName(connected));
    }

    protected ColumnRealName doResolveCompoundColumn(ConditionOption option, HpSpecifiedColumn specifiedColumn) {
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
    //                                                                       Assist Helper
    //                                                                       =============
    protected void noticeRegistered(ColumnRealName callerName, Object value) {
        if (_log.isDebugEnabled()) {
            final String target = callerName + "." + _conditionKey;
            _log.debug("*Found the duplicate query: target=" + target + " value=" + value);
        }
    }

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
