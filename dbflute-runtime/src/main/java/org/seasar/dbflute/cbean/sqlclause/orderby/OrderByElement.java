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
package org.seasar.dbflute.cbean.sqlclause.orderby;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.seasar.dbflute.cbean.ConditionBean;
import org.seasar.dbflute.cbean.ManualOrderBean;
import org.seasar.dbflute.cbean.chelper.HpCalcSpecification;
import org.seasar.dbflute.cbean.chelper.HpMobCaseWhenElement;
import org.seasar.dbflute.cbean.cipher.ColumnFunctionCipher;
import org.seasar.dbflute.cbean.cipher.GearedCipherManager;
import org.seasar.dbflute.cbean.ckey.ConditionKey;
import org.seasar.dbflute.dbmeta.info.ColumnInfo;
import org.seasar.dbflute.exception.IllegalConditionBeanOperationException;
import org.seasar.dbflute.resource.DBFluteSystem;
import org.seasar.dbflute.util.DfTypeUtil;

/**
 * @author jflute
 */
public class OrderByElement implements Serializable {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Serial version UID. (Default) */
    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The value of alias name. (NullAllowed: when e.g. derived order-by) */
    protected final String _aliasName;

    /** The value of column name. (NotNull) */
    protected final String _columnName;

    /** The column info of the order column. (NotNull) */
    protected transient final ColumnInfo _columnInfo;
    // transient but serializing is already bankrupt

    /** Is this derived order-by? */
    protected final boolean _derivedOrderBy;

    /** The value of ascDesc. (NotNull, Changeable) */
    protected String _ascDesc = "asc";

    /** The manager of geared cipher. (NullAllowed) */
    protected transient GearedCipherManager _gearedCipherManager;

    /** The set-upper of order-by nulls. (NullAllowed, SetupLater) */
    protected transient OrderByClause.OrderByNullsSetupper _orderByNullsSetupper;

    /** Is nulls ordered first? (SetupLater) */
    protected boolean _nullsFirst;

    /** The bean of manual order. (NullAllowed, SetupLater) */
    protected transient ManualOrderBean _mob;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public OrderByElement(String aliasName, String columnName, ColumnInfo columnInfo, boolean derivedOrderBy) {
        assertColumnName(aliasName, columnName);
        assertColumnInfo(aliasName, columnInfo);
        _aliasName = aliasName;
        _columnName = columnName;
        _columnInfo = columnInfo;
        _derivedOrderBy = derivedOrderBy;
    }

    protected void assertColumnName(String aliasName, String columnName) {
        if (columnName == null) {
            String msg = "The argument 'columnName' should not be null: aliasName=" + aliasName;
            throw new IllegalArgumentException(msg);
        }
    }

    protected void assertColumnInfo(String aliasName, ColumnInfo columnInfo) {
        if (columnInfo == null) {
            String msg = "The argument 'columnInfo' should not be null: aliasName=" + aliasName;
            throw new IllegalArgumentException(msg);
        }
    }

    // ===================================================================================
    //                                                                        Manipulation
    //                                                                        ============
    public void setupAsc() {
        _ascDesc = "asc";
    }

    public void setupDesc() {
        _ascDesc = "desc";
    }

    public void reverse() {
        if (_ascDesc == null) {
            String msg = "The attribute[ascDesc] should not be null.";
            throw new IllegalStateException(msg);
        }
        if (_ascDesc.equals("asc")) {
            _ascDesc = "desc";
        } else if (_ascDesc.equals("desc")) {
            _ascDesc = "asc";
        } else {
            String msg = "The attribute[ascDesc] should be asc or desc: but ascDesc=" + _ascDesc;
            throw new IllegalStateException(msg);
        }
    }

    // ===================================================================================
    //                                                                     Order-By Clause 
    //                                                                     ===============
    public String getElementClause() { // needs cipher
        if (_ascDesc == null) {
            String msg = "The attribute[ascDesc] should not be null.";
            throw new IllegalStateException(msg);
        }
        final StringBuilder sb = new StringBuilder();
        final String columnFullName = getColumnFullName();
        if (_mob != null && _mob.hasManualOrder()) {
            setupManualOrderClause(sb, columnFullName, null);
            return sb.toString();
        } else {
            sb.append(decryptIfNeeds(_columnInfo, columnFullName)).append(" ").append(_ascDesc);
            final String clause = sb.toString();
            if (_orderByNullsSetupper != null) {
                return _orderByNullsSetupper.setup(columnFullName, clause, _nullsFirst);
            } else {
                return clause;
            }
        }
    }

    public String getElementClause(Map<String, String> selectClauseRealColumnAliasMap) { // basically for union
        if (selectClauseRealColumnAliasMap == null) {
            String msg = "The argument 'selectClauseRealColumnAliasMap' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        if (_ascDesc == null) {
            String msg = "The attribute 'ascDesc' should not be null.";
            throw new IllegalStateException(msg);
        }
        final String columnAlias = mappingToRealColumnAlias(selectClauseRealColumnAliasMap, getColumnFullName());
        final StringBuilder sb = new StringBuilder();
        if (_mob != null && _mob.hasManualOrder()) {
            setupManualOrderClause(sb, columnAlias, selectClauseRealColumnAliasMap);
            return sb.toString();
        } else {
            sb.append(columnAlias).append(" ").append(_ascDesc); // no need to cipher because of union
            if (_orderByNullsSetupper != null) {
                return _orderByNullsSetupper.setup(columnAlias, sb.toString(), _nullsFirst);
            } else {
                return sb.toString();
            }
        }
    }

    protected String mappingToRealColumnAlias(Map<String, String> selectClauseRealColumnAliasMap, String columnFullName) {
        final String columnAlias = selectClauseRealColumnAliasMap.get(columnFullName);
        if (columnAlias == null || columnAlias.trim().length() == 0) {
            throwOrderByColumnNotFoundException(getColumnFullName(), selectClauseRealColumnAliasMap);
        }
        return columnAlias;
    }

    protected void setupManualOrderClause(StringBuilder sb, String columnAlias,
            Map<String, String> selectClauseRealColumnAliasMap) {
        final String realAlias;
        if (_mob.hasOrderByCalculation()) {
            final HpCalcSpecification<ConditionBean> calculationOrder = _mob.getOrderByCalculation();
            realAlias = calculationOrder.buildStatementToSpecifidName(columnAlias, selectClauseRealColumnAliasMap);
        } else {
            if (selectClauseRealColumnAliasMap != null) { // means union
                realAlias = columnAlias;
            } else {
                realAlias = decryptIfNeeds(_columnInfo, columnAlias);
            }
        }
        final List<HpMobCaseWhenElement> caseWhenList = _mob.getCaseWhenBoundList();
        if (!caseWhenList.isEmpty()) {
            sb.append(ln()).append("   case").append(ln());
            int index = 0;
            for (HpMobCaseWhenElement element : caseWhenList) {
                sb.append("     when ");
                doSetupManualOrderClause(sb, realAlias, element);
                final List<HpMobCaseWhenElement> connectedElementList = element.getConnectedElementList();
                for (HpMobCaseWhenElement connectedElement : connectedElementList) {
                    doSetupManualOrderClause(sb, realAlias, connectedElement);
                }
                final Object thenValue = element.getThenValue();
                final String thenExp;
                if (thenValue != null) {
                    thenExp = thenValue.toString();
                } else {
                    thenExp = String.valueOf(index);
                }
                sb.append(" then ").append(thenExp).append(ln());
                ++index;
            }
            final Object elseValue = _mob.getElseValue();
            final String elseExp;
            if (elseValue != null) {
                elseExp = elseValue.toString();
            } else {
                elseExp = String.valueOf(index);
            }
            sb.append("     else ").append(elseExp).append(ln());
            sb.append("   end");
        } else {
            sb.append(realAlias);
        }
        sb.append(" ").append(_ascDesc);
    }

    protected void doSetupManualOrderClause(StringBuilder sb, String columnAlias, HpMobCaseWhenElement element) {
        final ConditionKey conditionKey = element.getConditionKey();
        final String keyExp = conditionKey.getOperand();
        final String connector = element.toConnector();
        if (connector != null) { // only connected elements
            sb.append(" ").append(connector).append(" ");
        }
        if (isManualOrderConditionKeyNullHandling(conditionKey)) {
            sb.append(columnAlias).append(" ").append(keyExp);
        } else {
            final Object bindExp = element.getOrderValue();
            sb.append(columnAlias).append(" ").append(keyExp).append(" ").append(bindExp);
        }
    }

    protected boolean isManualOrderConditionKeyNullHandling(ConditionKey conditionKey) {
        return conditionKey.equals(ConditionKey.CK_IS_NULL) || conditionKey.equals(ConditionKey.CK_IS_NOT_NULL);
    }

    protected void throwOrderByColumnNotFoundException(String columnName,
            Map<String, String> selectClauseRealColumnAliasMap) {
        String msg = "Look! Read the message below." + ln();
        msg = msg + "/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *" + ln();
        msg = msg + "The column for order-by was not found in select-clause!" + ln();
        msg = msg + ln();
        msg = msg + "[Advice]" + ln();
        msg = msg + "If you use 'union()' or 'unionAll()', check your condition-bean!" + ln();
        msg = msg + "You can use only order-by columns on select-clause if union." + ln();
        msg = msg + "For example:" + ln();
        msg = msg + "  (x):" + ln();
        msg = msg + "    AaaCB cb = new AaaCB();" + ln();
        msg = msg + "    cb.query().setXxx...();" + ln();
        msg = msg + "    cb.union(new UnionQuery<AaaCB>() {" + ln();
        msg = msg + "        public void query(AaaCB unionCB) {" + ln();
        msg = msg + "            unionCB.query().setXxx...();" + ln();
        msg = msg + "        }" + ln();
        msg = msg + "    }" + ln();
        msg = msg + "    cb.query().queryBbb().addOrderBy_BbbName_Asc();// *NG!" + ln();
        msg = msg + "    " + ln();
        msg = msg + "  (o):" + ln();
        msg = msg + "    AaaCB cb = new AaaCB();" + ln();
        msg = msg + "    cb.setupSelect_Bbb();// *Point!" + ln();
        msg = msg + "    cb.query().setXxx...();" + ln();
        msg = msg + "    cb.union(new UnionQuery<AaaCB>() {" + ln();
        msg = msg + "        public void query(AaaCB unionCB) {" + ln();
        msg = msg + "            unionCB.query().setXxx...();" + ln();
        msg = msg + "        }" + ln();
        msg = msg + "    }" + ln();
        msg = msg + "    cb.query().queryBbb().addOrderBy_BbbName_Asc();// *OK!" + ln();
        msg = msg + "    " + ln();
        msg = msg + "Or else if you DON'T use 'union()' or 'unionAll()', This is the Framework Exception!" + ln();
        msg = msg + ln();
        msg = msg + "[Target Column]" + ln();
        msg = msg + columnName + ln();
        msg = msg + ln();
        msg = msg + "[Internal Object]" + ln();
        msg = msg + "selectClauseRealColumnAliasMap=" + selectClauseRealColumnAliasMap + ln();
        msg = msg + "* * * * * * * * * */";
        throw new IllegalConditionBeanOperationException(msg);
    }

    protected String ln() {
        return DBFluteSystem.getBasicLn();
    }

    // ===================================================================================
    //                                                                       Order-By Info
    //                                                                       =============
    public boolean isAsc() {
        if (_ascDesc == null) {
            String msg = "The attribute[ascDesc] should not be null.";
            throw new IllegalStateException(msg);
        }
        if (_ascDesc.equals("asc")) {
            return true;
        } else if (_ascDesc.equals("desc")) {
            return false;
        } else {
            String msg = "The attribute[ascDesc] should be asc or desc: but ascDesc=" + _ascDesc;
            throw new IllegalStateException(msg);
        }
    }

    public String getColumnFullName() {
        final StringBuilder sb = new StringBuilder();
        if (_aliasName != null) {
            sb.append(_aliasName).append(".");
        }
        if (_columnName == null) {
            String msg = "The attribute[columnName] should not be null.";
            throw new IllegalStateException(msg);
        }
        sb.append(_columnName);
        return sb.toString();
    }

    // ===================================================================================
    //                                                                       Geared Cipher
    //                                                                       =============
    protected String decryptIfNeeds(ColumnInfo columnInfo, String valueExp) {
        if (_gearedCipherManager == null) {
            return valueExp;
        }
        final ColumnFunctionCipher cipher = _gearedCipherManager.findColumnFunctionCipher(columnInfo);
        return cipher != null ? cipher.decrypt(valueExp) : valueExp;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    /**
     * This method overrides the method that is declared at super.
     * @return The view-string of all-columns value. (NotNull)
     */
    public String toString() {
        final String title = DfTypeUtil.toClassTitle(this);
        final StringBuilder sb = new StringBuilder();
        sb.append(title).append(":");
        sb.append("{aliasName=").append(_aliasName);
        sb.append(" columnName=").append(_columnName);
        sb.append(" ascDesc=").append(_ascDesc);
        sb.append("}");
        return sb.toString();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getAliasName() {
        return _aliasName;
    }

    public String getColumnName() {
        return _columnName;
    }

    public ColumnInfo getColumnInfo() {
        return _columnInfo;
    }

    public boolean isDerivedOrderBy() {
        return _derivedOrderBy;
    }

    public String getAscDesc() {
        return _ascDesc;
    }

    public void setGearedCipherManager(GearedCipherManager gearedCipherManager) {
        this._gearedCipherManager = gearedCipherManager;
    }

    public void setOrderByNullsSetupper(OrderByClause.OrderByNullsSetupper value, boolean nullsFirst) {
        _orderByNullsSetupper = value;
        _nullsFirst = nullsFirst;
    }

    public void setManualOrderBean(ManualOrderBean mob) {
        _mob = mob;
    }

    public ManualOrderBean getManualOrderBean() {
        return _mob;
    }
}
