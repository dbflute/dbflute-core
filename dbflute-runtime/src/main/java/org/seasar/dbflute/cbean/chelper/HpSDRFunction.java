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
import org.seasar.dbflute.cbean.ConditionQuery;
import org.seasar.dbflute.cbean.SubQuery;
import org.seasar.dbflute.cbean.coption.DerivedReferrerOption;
import org.seasar.dbflute.dbmeta.DBMetaProvider;
import org.seasar.dbflute.exception.SpecifyDerivedReferrerInvalidAliasNameException;
import org.seasar.dbflute.exception.thrower.ConditionBeanExceptionThrower;
import org.seasar.dbflute.util.Srl;

/**
 * The function for (Specify)DerivedReferrer.
 * @param <REFERRER_CB> The type of referrer condition-bean.
 * @param <LOCAL_CQ> The type of local condition-query.
 * @author jflute
 */
public class HpSDRFunction<REFERRER_CB extends ConditionBean, LOCAL_CQ extends ConditionQuery> {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final ConditionBean _baseCB;
    protected final LOCAL_CQ _localCQ;
    protected final HpSDRSetupper<REFERRER_CB, LOCAL_CQ> _querySetupper;
    protected final DBMetaProvider _dbmetaProvider;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public HpSDRFunction(ConditionBean baseCB, LOCAL_CQ localCQ, HpSDRSetupper<REFERRER_CB, LOCAL_CQ> querySetupper,
            DBMetaProvider dbmetaProvider) {
        _baseCB = baseCB;
        _localCQ = localCQ;
        _querySetupper = querySetupper;
        _dbmetaProvider = dbmetaProvider;
    }

    // ===================================================================================
    //                                                                            Function
    //                                                                            ========
    /**
     * Set up the sub query of referrer for the scalar 'count'.
     * <pre>
     * cb.specify().derivePurchaseList().<span style="color: #DD4747">count</span>(new SubQuery&lt;PurchaseCB&gt;() {
     *     protected void query(PurchaseCB subCB) {
     *         subCB.specify().<span style="color: #DD4747">columnPurchaseId</span>(); <span style="color: #3F7E5E">// basically PK to count records</span>
     *         subCB.query().setPaymentCompleteFlg_Equal_True(); <span style="color: #3F7E5E">// referrer condition</span>
     *     }
     * }, Member.<span style="color: #DD4747">ALIAS_purchaseCount</span>);
     * </pre> 
     * @param subQuery The sub query of referrer. (NotNull)
     * @param aliasName The alias of the name. The property should exists on the entity. (NotNull)
     */
    public void count(SubQuery<REFERRER_CB> subQuery, String aliasName) {
        doCount(subQuery, aliasName, null);
    }

    /**
     * An overload method for count() with an option. So refer to the method's java-doc about basic info.
     * <pre>
     * cb.specify().derivePurchaseList().<span style="color: #DD4747">count</span>(new SubQuery&lt;PurchaseCB&gt;() {
     *     protected void query(PurchaseCB subCB) {
     *         subCB.specify().<span style="color: #DD4747">columnPurchaseId</span>(); <span style="color: #3F7E5E">// basically PK to count records</span>
     *         subCB.query().setPaymentCompleteFlg_Equal_True(); <span style="color: #3F7E5E">// referrer condition</span>
     *     }
     * }, Member.<span style="color: #DD4747">ALIAS_purchaseCount</span>, new DerivedReferrerOption().<span style="color: #DD4747">coalesce</span>(0));
     * </pre> 
     * @param subQuery The sub query of referrer. (NotNull)
     * @param aliasName The alias of the name. The property should exists on the entity. (NotNull)
     * @param option The option for DerivedReferrer. e.g. you can use a coalesce function. (NotNull)
     */
    public void count(SubQuery<REFERRER_CB> subQuery, String aliasName, DerivedReferrerOption option) {
        assertDerivedReferrerOption(option);
        doCount(subQuery, aliasName, option);
    }

    protected void doCount(SubQuery<REFERRER_CB> subQuery, String aliasName, DerivedReferrerOption option) {
        assertSubQuery(subQuery);
        assertAliasName(aliasName);
        _querySetupper.setup("count", subQuery, _localCQ, filterAliasName(aliasName), option);
    }

    /**
     * Set up the sub query of referrer for the scalar 'count-distinct'.
     * <pre>
     * cb.specify().derivePurchaseList().<span style="color: #DD4747">countDistinct</span>(new SubQuery&lt;PurchaseCB&gt;() {
     *     protected void query(PurchaseCB subCB) {
     *         subCB.specify().<span style="color: #DD4747">columnProductId</span>(); <span style="color: #3F7E5E">// derived column by function</span>
     *         subCB.query().setPaymentCompleteFlg_Equal_True(); <span style="color: #3F7E5E">// referrer condition</span>
     *     }
     * }, Member.<span style="color: #DD4747">ALIAS_productKindCount</span>);
     * </pre> 
     * @param subQuery The sub query of referrer. (NotNull)
     * @param aliasName The alias of the name. The property should exists on the entity. (NotNull)
     */
    public void countDistinct(SubQuery<REFERRER_CB> subQuery, String aliasName) {
        doCountDistinct(subQuery, aliasName, null);
    }

    /**
     * An overload method for count() with an option. So refer to the method's java-doc about basic info.
     * <pre>
     * cb.specify().derivePurchaseList().<span style="color: #DD4747">countDistinct</span>(new SubQuery&lt;PurchaseCB&gt;() {
     *     protected void query(PurchaseCB subCB) {
     *         subCB.specify().<span style="color: #DD4747">columnProductId</span>(); <span style="color: #3F7E5E">// derived column by function</span>
     *         subCB.query().setPaymentCompleteFlg_Equal_True(); <span style="color: #3F7E5E">// referrer condition</span>
     *     }
     * }, Member.<span style="color: #DD4747">ALIAS_productKindCount</span>, new DerivedReferrerOption().<span style="color: #DD4747">coalesce</span>(0));
     * </pre> 
     * @param subQuery The sub query of referrer. (NotNull)
     * @param aliasName The alias of the name. The property should exists on the entity. (NotNull)
     * @param option The option for DerivedReferrer. e.g. you can use a coalesce function. (NotNull)
     */
    public void countDistinct(SubQuery<REFERRER_CB> subQuery, String aliasName, DerivedReferrerOption option) {
        assertDerivedReferrerOption(option);
        doCountDistinct(subQuery, aliasName, option);
    }

    protected void doCountDistinct(SubQuery<REFERRER_CB> subQuery, String aliasName, DerivedReferrerOption option) {
        assertSubQuery(subQuery);
        assertAliasName(aliasName);
        _querySetupper.setup("count(distinct", subQuery, _localCQ, filterAliasName(aliasName), option);
    }

    /**
     * Set up the sub query of referrer for the scalar 'max'.
     * <pre>
     * cb.specify().derivePurchaseList().<span style="color: #DD4747">max</span>(new SubQuery&lt;PurchaseCB&gt;() {
     *     protected void query(PurchaseCB subCB) {
     *         subCB.specify().<span style="color: #DD4747">columnPurchaseDatetime</span>(); <span style="color: #3F7E5E">// derived column by function</span>
     *         subCB.query().setPaymentCompleteFlg_Equal_True(); <span style="color: #3F7E5E">// referrer condition</span>
     *     }
     * }, Member.<span style="color: #DD4747">ALIAS_latestPurchaseDatetime</span>);
     * </pre> 
     * @param subQuery The sub query of referrer. (NotNull)
     * @param aliasName The alias of the name. The property should exists on the entity. (NotNull)
     */
    public void max(SubQuery<REFERRER_CB> subQuery, String aliasName) {
        doMax(subQuery, aliasName, null);
    }

    /**
     * An overload method for max() with an option. So refer to the method's java-doc.
     * <pre>
     * cb.specify().derivePurchaseList().<span style="color: #DD4747">max</span>(new SubQuery&lt;PurchaseCB&gt;() {
     *     protected void query(PurchaseCB subCB) {
     *         subCB.specify().<span style="color: #DD4747">columnPurchaseDatetime</span>(); <span style="color: #3F7E5E">// derived column by function</span>
     *         subCB.query().setPaymentCompleteFlg_Equal_True(); <span style="color: #3F7E5E">// referrer condition</span>
     *     }
     * }, Member.<span style="color: #DD4747">ALIAS_latestPurchaseDatetime</span>, new DerivedReferrerOption().<span style="color: #DD4747">coalesce</span>("2011-06-07"));
     * </pre>
     * @param subQuery The sub query of referrer. (NotNull)
     * @param aliasName The alias of the name. The property should exists on the entity. (NotNull)
     * @param option The option for DerivedReferrer. e.g. you can use a coalesce function. (NotNull)
     */
    public void max(SubQuery<REFERRER_CB> subQuery, String aliasName, DerivedReferrerOption option) {
        assertDerivedReferrerOption(option);
        doMax(subQuery, aliasName, option);
    }

    protected void doMax(SubQuery<REFERRER_CB> subQuery, String aliasName, DerivedReferrerOption option) {
        assertSubQuery(subQuery);
        assertAliasName(aliasName);
        _querySetupper.setup("max", subQuery, _localCQ, filterAliasName(aliasName), option);
    }

    /**
     * Set up the sub query of referrer for the scalar 'min'.
     * <pre>
     * cb.specify().derivePurchaseList().<span style="color: #DD4747">min</span>(new SubQuery&lt;PurchaseCB&gt;() {
     *     protected void query(PurchaseCB subCB) {
     *         subCB.specify().<span style="color: #DD4747">columnPurchaseDatetime</span>(); <span style="color: #3F7E5E">// derived column by function</span>
     *         subCB.query().setPaymentCompleteFlg_Equal_True(); <span style="color: #3F7E5E">// referrer condition</span>
     *     }
     * }, Member.<span style="color: #DD4747">ALIAS_firstPurchaseDatetime</span>);
     * </pre> 
     * @param subQuery The sub query of referrer. (NotNull)
     * @param aliasName The alias of the name. The property should exists on the entity. (NotNull)
     */
    public void min(SubQuery<REFERRER_CB> subQuery, String aliasName) {
        doMin(subQuery, aliasName, null);
    }

    /**
     * An overload method for min() with an option. So refer to the method's java-doc.
     * <pre>
     * cb.specify().derivePurchaseList().<span style="color: #DD4747">min</span>(new SubQuery&lt;PurchaseCB&gt;() {
     *     protected void query(PurchaseCB subCB) {
     *         subCB.specify().<span style="color: #DD4747">columnPurchaseDatetime</span>(); <span style="color: #3F7E5E">// derived column by function</span>
     *         subCB.query().setPaymentCompleteFlg_Equal_True(); <span style="color: #3F7E5E">// referrer condition</span>
     *     }
     * }, Member.<span style="color: #DD4747">ALIAS_firstPurchaseDatetime</span>, new DerivedReferrerOption().<span style="color: #DD4747">coalesce</span>("2011-06-07"));
     * </pre>
     * @param subQuery The sub query of referrer. (NotNull)
     * @param aliasName The alias of the name. The property should exists on the entity. (NotNull)
     * @param option The option for DerivedReferrer. e.g. you can use a coalesce function. (NotNull)
     */
    public void min(SubQuery<REFERRER_CB> subQuery, String aliasName, DerivedReferrerOption option) {
        assertDerivedReferrerOption(option);
        doMin(subQuery, aliasName, option);
    }

    protected void doMin(SubQuery<REFERRER_CB> subQuery, String aliasName, DerivedReferrerOption option) {
        assertSubQuery(subQuery);
        assertAliasName(aliasName);
        _querySetupper.setup("min", subQuery, _localCQ, filterAliasName(aliasName), option);
    }

    /**
     * Set up the sub query of referrer for the scalar 'sum'.
     * <pre>
     * cb.specify().derivePurchaseList().<span style="color: #DD4747">sum</span>(new SubQuery&lt;PurchaseCB&gt;() {
     *     protected void query(PurchaseCB subCB) {
     *         subCB.specify().<span style="color: #DD4747">columnPurchasePrice</span>(); <span style="color: #3F7E5E">// derived column by function</span>
     *         subCB.query().setPaymentCompleteFlg_Equal_True(); <span style="color: #3F7E5E">// referrer condition</span>
     *     }
     * }, Member.<span style="color: #DD4747">ALIAS_purchasePriceSummary</span>);
     * </pre> 
     * @param subQuery The sub query of referrer. (NotNull)
     * @param aliasName The alias of the name. The property should exists on the entity. (NotNull)
     */
    public void sum(SubQuery<REFERRER_CB> subQuery, String aliasName) {
        doSum(subQuery, aliasName, null);
    }

    /**
     * An overload method for sum() with an option. So refer to the method's java-doc.
     * <pre>
     * cb.specify().derivePurchaseList().<span style="color: #DD4747">sum</span>(new SubQuery&lt;PurchaseCB&gt;() {
     *     protected void query(PurchaseCB subCB) {
     *         subCB.specify().<span style="color: #DD4747">columnPurchasePrice</span>(); <span style="color: #3F7E5E">// derived column by function</span>
     *         subCB.query().setPaymentCompleteFlg_Equal_True(); <span style="color: #3F7E5E">// referrer condition</span>
     *     }
     * }, Member.<span style="color: #DD4747">ALIAS_purchasePriceSummary</span>, new DerivedReferrerOption().<span style="color: #DD4747">coalesce</span>(0));
     * </pre>
     * @param subQuery The sub query of referrer. (NotNull)
     * @param aliasName The alias of the name. The property should exists on the entity. (NotNull)
     * @param option The option for DerivedReferrer. e.g. you can use a coalesce function. (NotNull)
     */
    public void sum(SubQuery<REFERRER_CB> subQuery, String aliasName, DerivedReferrerOption option) {
        assertDerivedReferrerOption(option);
        doSum(subQuery, aliasName, option);
    }

    protected void doSum(SubQuery<REFERRER_CB> subQuery, String aliasName, DerivedReferrerOption option) {
        assertSubQuery(subQuery);
        assertAliasName(aliasName);
        _querySetupper.setup("sum", subQuery, _localCQ, filterAliasName(aliasName), option);
    }

    /**
     * Set up the sub query of referrer for the scalar 'avg'.
     * <pre>
     * cb.specify().derivePurchaseList().<span style="color: #DD4747">avg</span>(new SubQuery&lt;PurchaseCB&gt;() {
     *     protected void query(PurchaseCB subCB) {
     *         subCB.specify().<span style="color: #DD4747">columnPurchasePrice</span>(); <span style="color: #3F7E5E">// derived column by function</span>
     *         subCB.query().setPaymentCompleteFlg_Equal_True(); <span style="color: #3F7E5E">// referrer condition</span>
     *     }
     * }, Member.<span style="color: #DD4747">ALIAS_purchasePriceAverage</span>);
     * </pre> 
     * @param subQuery The sub query of referrer. (NotNull)
     * @param aliasName The alias of the name. The property should exists on the entity. (NotNull)
     */
    public void avg(SubQuery<REFERRER_CB> subQuery, String aliasName) {
        doAvg(subQuery, aliasName, null);
    }

    /**
     * An overload method for avg() with an option. So refer to the method's java-doc.
     * <pre>
     * cb.specify().derivePurchaseList().<span style="color: #DD4747">avg</span>(new SubQuery&lt;PurchaseCB&gt;() {
     *     protected void query(PurchaseCB subCB) {
     *         subCB.specify().<span style="color: #DD4747">columnPurchasePrice</span>(); <span style="color: #3F7E5E">// derived column by function</span>
     *         subCB.query().setPaymentCompleteFlg_Equal_True(); <span style="color: #3F7E5E">// referrer condition</span>
     *     }
     * }, Member.<span style="color: #DD4747">ALIAS_purchasePriceAverage</span>, new DerivedReferrerOption().<span style="color: #DD4747">coalesce</span>(0));
     * </pre>
     * @param subQuery The sub query of referrer. (NotNull)
     * @param aliasName The alias of the name. The property should exists on the entity. (NotNull)
     * @param option The option for DerivedReferrer. e.g. you can use a coalesce function. (NotNull)
     */
    public void avg(SubQuery<REFERRER_CB> subQuery, String aliasName, DerivedReferrerOption option) {
        assertDerivedReferrerOption(option);
        doAvg(subQuery, aliasName, option);
    }

    protected void doAvg(SubQuery<REFERRER_CB> subQuery, String aliasName, DerivedReferrerOption option) {
        assertSubQuery(subQuery);
        assertAliasName(aliasName);
        _querySetupper.setup("avg", subQuery, _localCQ, filterAliasName(aliasName), option);
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected void assertSubQuery(SubQuery<?> subQuery) {
        if (subQuery == null) {
            String msg = "The argument 'subQuery' for DerivedReferrer should not be null.";
            throw new IllegalArgumentException(msg);
        }
    }

    protected void assertAliasName(String aliasName) {
        if (isPurposeNullAlias()) {
            if (aliasName != null) {
                String msg = "The aliasName should be null in the purpose: " + _baseCB.getPurpose();
                throw new SpecifyDerivedReferrerInvalidAliasNameException(msg);
            }
        } else { // normal
            if (Srl.is_Null_or_TrimmedEmpty(aliasName)) {
                throwSpecifyDerivedReferrerInvalidAliasNameException();
            }
        }
        // *this check was moved to runtime (when creating a behavior command)
        //String tableDbName = _baseCB.getTableDbName();
        //DBMeta dbmeta = _dbmetaProvider.provideDBMetaChecked(tableDbName);
        //Method[] methods = dbmeta.getEntityType().getMethods();
        //String targetMethodName = "set" + replaceString(aliasName, "_", "").toLowerCase();
        //boolean existsSetterMethod = false;
        //for (Method method : methods) {
        //    if (!method.getName().startsWith("set")) {
        //        continue;
        //    }
        //    if (targetMethodName.equals(method.getName().toLowerCase())) {
        //        existsSetterMethod = true;
        //        break;
        //    }
        //}
        //if (!existsSetterMethod) {
        //    throwSpecifyDerivedReferrerEntityPropertyNotFoundException(aliasName, dbmeta.getEntityType());
        //}
    }

    protected boolean isPurposeNullAlias() {
        final HpCBPurpose purpose = _baseCB.getPurpose();
        return purpose.equals(HpCBPurpose.COLUMN_QUERY) || purpose.equals(HpCBPurpose.SCALAR_SELECT)
                || purpose.equals(HpCBPurpose.DERIVED_REFERRER);
    }

    protected void throwSpecifyDerivedReferrerInvalidAliasNameException() {
        createCBExThrower().throwSpecifyDerivedReferrerInvalidAliasNameException(_localCQ);
    }

    protected void assertDerivedReferrerOption(DerivedReferrerOption option) {
        if (option == null) {
            String msg = "The argument 'option' for DerivedReferrer should not be null.";
            throw new IllegalArgumentException(msg);
        }
    }

    // *this check was moved to runtime (when creating a behavior command)
    //protected void throwSpecifyDerivedReferrerEntityPropertyNotFoundException(String aliasName, Class<?> entityType) {
    //    createCBExThrower().throwSpecifyDerivedReferrerEntityPropertyNotFoundException(aliasName, entityType);
    //}
    //protected String replaceString(String text, String fromText, String toText) {
    //    return Srl.replace(text, fromText, toText);
    //}

    protected String filterAliasName(String aliasName) {
        if (aliasName != null) {
            return aliasName.trim();
        } else {
            final HpCBPurpose purpose = _baseCB.getPurpose();
            if (isPurposeNullAlias()) {
                if (purpose.equals(HpCBPurpose.SCALAR_SELECT)) {
                    return _baseCB.getSqlClause().getScalarSelectColumnAlias();
                } else if (purpose.equals(HpCBPurpose.DERIVED_REFERRER)) {
                    return _baseCB.getSqlClause().getDerivedReferrerNestedAlias();
                } else { // for example, ColumnQuery
                    return null;
                }
            } else { // basically no way because of checked before
                return null;
            }
        }
    }

    // ===================================================================================
    //                                                                    Exception Helper
    //                                                                    ================
    protected ConditionBeanExceptionThrower createCBExThrower() {
        return new ConditionBeanExceptionThrower();
    }
}
