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
package org.dbflute.cbean.chelper;

import org.dbflute.cbean.ConditionBean;
import org.dbflute.cbean.ConditionQuery;
import org.dbflute.cbean.coption.DerivedReferrerOption;
import org.dbflute.cbean.coption.DerivedReferrerOptionFactory;
import org.dbflute.cbean.coption.FunctionFilterOptionCall;
import org.dbflute.cbean.exception.ConditionBeanExceptionThrower;
import org.dbflute.cbean.scoping.SubQuery;
import org.dbflute.dbmeta.DBMeta;
import org.dbflute.dbmeta.DBMetaProvider;
import org.dbflute.dbmeta.accessory.DerivedMappable;
import org.dbflute.dbmeta.info.ColumnInfo;
import org.dbflute.exception.IllegalConditionBeanOperationException;
import org.dbflute.exception.SpecifyDerivedReferrerInvalidAliasNameException;
import org.dbflute.util.Srl;

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
    protected final DerivedReferrerOptionFactory _sdrOpFactory;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public HpSDRFunction(ConditionBean baseCB, LOCAL_CQ localCQ, HpSDRSetupper<REFERRER_CB, LOCAL_CQ> querySetupper,
            DBMetaProvider dbmetaProvider, DerivedReferrerOptionFactory sdrOpFactory) {
        _baseCB = baseCB;
        _localCQ = localCQ;
        _querySetupper = querySetupper;
        _dbmetaProvider = dbmetaProvider;
        _sdrOpFactory = sdrOpFactory;
    }

    // ===================================================================================
    //                                                                            Function
    //                                                                            ========
    // -----------------------------------------------------
    //                                                 Count
    //                                                 -----
    /**
     * Set up the sub query of referrer for the scalar 'count'.
     * <pre>
     * cb.specify().derivePurchaseList().<span style="color: #CC4747">count</span>(<span style="color: #553000">purchaseCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">purchaseCB</span>.specify().<span style="color: #CC4747">columnPurchaseId</span>(); <span style="color: #3F7E5E">// basically PK to count records</span>
     *     <span style="color: #553000">purchaseCB</span>.query().setPaymentCompleteFlg_Equal_True(); <span style="color: #3F7E5E">// referrer condition</span>
     * }, Member.<span style="color: #CC4747">ALIAS_purchaseCount</span>);
     * </pre> 
     * @param derivedCBLambda The callback for sub-query of referrer. (NotNull)
     * @param aliasName The alias of the name. The property should exists on the entity. (NotNull)
     */
    public void count(SubQuery<REFERRER_CB> derivedCBLambda, String aliasName) {
        doCount(derivedCBLambda, aliasName, null);
    }

    /**
     * An overload method for count() with an option. So refer to the method's java-doc about basic info.
     * <pre>
     * cb.specify().derivePurchaseList().<span style="color: #CC4747">count</span>(<span style="color: #553000">purchaseCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">purchaseCB</span>.specify().<span style="color: #994747">columnPurchaseId</span>(); <span style="color: #3F7E5E">// basically PK to count records</span>
     *     <span style="color: #553000">purchaseCB</span>.query().setPaymentCompleteFlg_Equal_True(); <span style="color: #3F7E5E">// referrer condition</span>
     * }, Member.<span style="color: #994747">ALIAS_purchaseCount</span>, <span style="color: #553000">op</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> <span style="color: #553000">op</span>.<span style="color: #CC4747">coalesce</span>(0));
     * </pre> 
     * @param derivedCBLambda The callback for sub-query of referrer. (NotNull)
     * @param aliasName The alias of the name. The property should exists on the entity. (NotNull)
     * @param opLambda The callback for option of DerivedReferrer. e.g. you can use a coalesce function. (NotNull)
     */
    public void count(SubQuery<REFERRER_CB> derivedCBLambda, String aliasName, FunctionFilterOptionCall<DerivedReferrerOption> opLambda) {
        doCount(derivedCBLambda, aliasName, createDerivedReferrerOption(opLambda));
    }

    protected void doCount(SubQuery<REFERRER_CB> subQuery, String aliasName, DerivedReferrerOption option) {
        assertSubQuery(subQuery);
        assertAliasName(aliasName);
        _querySetupper.setup("count", subQuery, _localCQ, filterAliasName(aliasName), option);
    }

    // -----------------------------------------------------
    //                                        Count Distinct
    //                                        --------------
    /**
     * Set up the sub query of referrer for the scalar 'count-distinct'.
     * <pre>
     * cb.specify().derivePurchaseList().<span style="color: #CC4747">countDistinct</span>(<span style="color: #553000">purchaseCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">purchaseCB</span>.specify().<span style="color: #CC4747">columnProductId</span>(); <span style="color: #3F7E5E">// derived column by function</span>
     *     <span style="color: #553000">purchaseCB</span>.query().setPaymentCompleteFlg_Equal_True(); <span style="color: #3F7E5E">// referrer condition</span>
     * }, Member.<span style="color: #CC4747">ALIAS_productKindCount</span>);
     * </pre> 
     * @param derivedCBLambda The callback for sub-query of referrer. (NotNull)
     * @param aliasName The alias of the name. The property should exists on the entity. (NotNull)
     */
    public void countDistinct(SubQuery<REFERRER_CB> derivedCBLambda, String aliasName) {
        doCountDistinct(derivedCBLambda, aliasName, null);
    }

    /**
     * An overload method for count() with an option. So refer to the method's java-doc about basic info.
     * <pre>
     * cb.specify().derivePurchaseList().<span style="color: #CC4747">countDistinct</span>(<span style="color: #553000">purchaseCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">purchaseCB</span>.specify().<span style="color: #994747">columnProductId</span>(); <span style="color: #3F7E5E">// derived column by function</span>
     *     <span style="color: #553000">purchaseCB</span>.query().setPaymentCompleteFlg_Equal_True(); <span style="color: #3F7E5E">// referrer condition</span>
     * }, Member.<span style="color: #994747">ALIAS_productKindCount</span>, <span style="color: #553000">op</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> <span style="color: #553000">op</span>.<span style="color: #CC4747">coalesce</span>(0));
     * </pre> 
     * @param derivedCBLambda The callback for sub-query of referrer. (NotNull)
     * @param aliasName The alias of the name. The property should exists on the entity. (NotNull)
     * @param opLambda The callback for option of DerivedReferrer. e.g. you can use a coalesce function. (NotNull)
     */
    public void countDistinct(SubQuery<REFERRER_CB> derivedCBLambda, String aliasName,
            FunctionFilterOptionCall<DerivedReferrerOption> opLambda) {
        doCountDistinct(derivedCBLambda, aliasName, createDerivedReferrerOption(opLambda));
    }

    protected void doCountDistinct(SubQuery<REFERRER_CB> subQuery, String aliasName, DerivedReferrerOption option) {
        assertSubQuery(subQuery);
        assertAliasName(aliasName);
        _querySetupper.setup("count(distinct", subQuery, _localCQ, filterAliasName(aliasName), option);
    }

    // -----------------------------------------------------
    //                                                  Max
    //                                                 -----
    /**
     * Set up the sub query of referrer for the scalar 'max'.
     * <pre>
     * cb.specify().derivePurchaseList().<span style="color: #CC4747">max</span>(<span style="color: #553000">purchaseCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">purchaseCB</span>.specify().<span style="color: #CC4747">columnPurchaseDatetime</span>(); <span style="color: #3F7E5E">// derived column by function</span>
     *     <span style="color: #553000">purchaseCB</span>.query().setPaymentCompleteFlg_Equal_True(); <span style="color: #3F7E5E">// referrer condition</span>
     * }, Member.<span style="color: #CC4747">ALIAS_latestPurchaseDatetime</span>);
     * </pre> 
     * @param derivedCBLambda The callback for sub-query of referrer. (NotNull)
     * @param aliasName The alias of the name. The property should exists on the entity. (NotNull)
     */
    public void max(SubQuery<REFERRER_CB> derivedCBLambda, String aliasName) {
        doMax(derivedCBLambda, aliasName, null);
    }

    /**
     * An overload method for max() with an option. So refer to the method's java-doc.
     * <pre>
     * cb.specify().derivePurchaseList().<span style="color: #CC4747">max</span>(<span style="color: #553000">purchaseCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">purchaseCB</span>.specify().<span style="color: #994747">columnPurchaseDatetime</span>(); <span style="color: #3F7E5E">// derived column by function</span>
     *     <span style="color: #553000">purchaseCB</span>.query().setPaymentCompleteFlg_Equal_True(); <span style="color: #3F7E5E">// referrer condition</span>
     * }, Member.<span style="color: #994747">ALIAS_latestPurchaseDatetime</span>, <span style="color: #553000">op</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> <span style="color: #553000">op</span>.<span style="color: #CC4747">coalesce</span>("2011-06-07"));
     * </pre>
     * @param derivedCBLambda The callback for sub-query of referrer. (NotNull)
     * @param aliasName The alias of the name. The property should exists on the entity. (NotNull)
     * @param opLambda The callback for option of DerivedReferrer. e.g. you can use a coalesce function. (NotNull)
     */
    public void max(SubQuery<REFERRER_CB> derivedCBLambda, String aliasName, FunctionFilterOptionCall<DerivedReferrerOption> opLambda) {
        doMax(derivedCBLambda, aliasName, createDerivedReferrerOption(opLambda));
    }

    protected void doMax(SubQuery<REFERRER_CB> subQuery, String aliasName, DerivedReferrerOption option) {
        assertSubQuery(subQuery);
        assertAliasName(aliasName);
        _querySetupper.setup("max", subQuery, _localCQ, filterAliasName(aliasName), option);
    }

    // -----------------------------------------------------
    //                                                  Min
    //                                                 -----
    /**
     * Set up the sub query of referrer for the scalar 'min'.
     * <pre>
     * cb.specify().derivePurchaseList().<span style="color: #CC4747">min</span>(<span style="color: #553000">purchaseCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">purchaseCB</span>.specify().<span style="color: #CC4747">columnPurchaseDatetime</span>(); <span style="color: #3F7E5E">// derived column by function</span>
     *     <span style="color: #553000">purchaseCB</span>.query().setPaymentCompleteFlg_Equal_True(); <span style="color: #3F7E5E">// referrer condition</span>
     * }, Member.<span style="color: #CC4747">ALIAS_firstPurchaseDatetime</span>);
     * </pre> 
     * @param derivedCBLambda The callback for sub-query of referrer. (NotNull)
     * @param aliasName The alias of the name. The property should exists on the entity. (NotNull)
     */
    public void min(SubQuery<REFERRER_CB> derivedCBLambda, String aliasName) {
        doMin(derivedCBLambda, aliasName, null);
    }

    /**
     * An overload method for min() with an option. So refer to the method's java-doc.
     * <pre>
     * cb.specify().derivePurchaseList().<span style="color: #CC4747">min</span>(<span style="color: #553000">purchaseCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">purchaseCB</span>.specify().<span style="color: #994747">columnPurchaseDatetime</span>(); <span style="color: #3F7E5E">// derived column by function</span>
     *     <span style="color: #553000">purchaseCB</span>.query().setPaymentCompleteFlg_Equal_True(); <span style="color: #3F7E5E">// referrer condition</span>
     * }, Member.<span style="color: #994747">ALIAS_firstPurchaseDatetime</span>, <span style="color: #553000">op</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> <span style="color: #553000">op</span>.<span style="color: #CC4747">coalesce</span>("2011-06-07"));
     * </pre>
     * @param derivedCBLambda The callback for sub-query of referrer. (NotNull)
     * @param aliasName The alias of the name. The property should exists on the entity. (NotNull)
     * @param opLambda The callback for option of DerivedReferrer. e.g. you can use a coalesce function. (NotNull)
     */
    public void min(SubQuery<REFERRER_CB> derivedCBLambda, String aliasName, FunctionFilterOptionCall<DerivedReferrerOption> opLambda) {
        doMin(derivedCBLambda, aliasName, createDerivedReferrerOption(opLambda));
    }

    protected void doMin(SubQuery<REFERRER_CB> subQuery, String aliasName, DerivedReferrerOption option) {
        assertSubQuery(subQuery);
        assertAliasName(aliasName);
        _querySetupper.setup("min", subQuery, _localCQ, filterAliasName(aliasName), option);
    }

    // -----------------------------------------------------
    //                                                  Sum
    //                                                 -----
    /**
     * Set up the sub query of referrer for the scalar 'sum'.
     * <pre>
     * cb.specify().derivePurchaseList().<span style="color: #CC4747">sum</span>(<span style="color: #553000">purchaseCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">purchaseCB</span>.specify().<span style="color: #CC4747">columnPurchasePrice</span>(); <span style="color: #3F7E5E">// derived column by function</span>
     *     <span style="color: #553000">purchaseCB</span>.query().setPaymentCompleteFlg_Equal_True(); <span style="color: #3F7E5E">// referrer condition</span>
     * }, Member.<span style="color: #CC4747">ALIAS_purchasePriceSummary</span>);
     * </pre> 
     * @param derivedCBLambda The callback for sub-query of referrer. (NotNull)
     * @param aliasName The alias of the name. The property should exists on the entity. (NotNull)
     */
    public void sum(SubQuery<REFERRER_CB> derivedCBLambda, String aliasName) {
        doSum(derivedCBLambda, aliasName, null);
    }

    /**
     * An overload method for sum() with an option. So refer to the method's java-doc.
     * <pre>
     * cb.specify().derivePurchaseList().<span style="color: #CC4747">sum</span>(<span style="color: #553000">purchaseCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">purchaseCB</span>.specify().<span style="color: #CC4747">columnPurchasePrice</span>(); <span style="color: #3F7E5E">// derived column by function</span>
     *     <span style="color: #553000">purchaseCB</span>.query().setPaymentCompleteFlg_Equal_True(); <span style="color: #3F7E5E">// referrer condition</span>
     * }, Member.<span style="color: #994747">ALIAS_purchasePriceSummary</span>, <span style="color: #553000">op</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> <span style="color: #553000">op</span>.<span style="color: #CC4747">coalesce</span>(0));
     * </pre>
     * @param derivedCBLambda The callback for sub-query of referrer. (NotNull)
     * @param aliasName The alias of the name. The property should exists on the entity. (NotNull)
     * @param opLambda The callback for option of DerivedReferrer. e.g. you can use a coalesce function. (NotNull)
     */
    public void sum(SubQuery<REFERRER_CB> derivedCBLambda, String aliasName, FunctionFilterOptionCall<DerivedReferrerOption> opLambda) {
        doSum(derivedCBLambda, aliasName, createDerivedReferrerOption(opLambda));
    }

    protected void doSum(SubQuery<REFERRER_CB> subQuery, String aliasName, DerivedReferrerOption option) {
        assertSubQuery(subQuery);
        assertAliasName(aliasName);
        _querySetupper.setup("sum", subQuery, _localCQ, filterAliasName(aliasName), option);
    }

    // -----------------------------------------------------
    //                                               Average
    //                                               -------
    /**
     * Set up the sub query of referrer for the scalar 'avg'.
     * <pre>
     * cb.specify().derivePurchaseList().<span style="color: #CC4747">avg</span>(<span style="color: #553000">purchaseCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">purchaseCB</span>.specify().<span style="color: #CC4747">columnPurchasePrice</span>(); <span style="color: #3F7E5E">// derived column by function</span>
     *     <span style="color: #553000">purchaseCB</span>.query().setPaymentCompleteFlg_Equal_True(); <span style="color: #3F7E5E">// referrer condition</span>
     * }, Member.<span style="color: #CC4747">ALIAS_purchasePriceAverage</span>);
     * </pre> 
     * @param derivedCBLambda The callback for sub-query of referrer. (NotNull)
     * @param aliasName The alias of the name. The property should exists on the entity. (NotNull)
     */
    public void avg(SubQuery<REFERRER_CB> derivedCBLambda, String aliasName) {
        doAvg(derivedCBLambda, aliasName, null);
    }

    /**
     * An overload method for avg() with an option. So refer to the method's java-doc.
     * <pre>
     * cb.specify().derivePurchaseList().<span style="color: #CC4747">avg</span>(<span style="color: #553000">purchaseCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">purchaseCB</span>.specify().<span style="color: #CC4747">columnPurchasePrice</span>(); <span style="color: #3F7E5E">// derived column by function</span>
     *     <span style="color: #553000">purchaseCB</span>.query().setPaymentCompleteFlg_Equal_True(); <span style="color: #3F7E5E">// referrer condition</span>
     * }, Member.<span style="color: #CC4747">ALIAS_purchasePriceAverage</span>, <span style="color: #553000">op</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> <span style="color: #553000">op</span>.<span style="color: #CC4747">coalesce</span>(0));
     * </pre>
     * @param derivedCBLambda The callback for sub-query of referrer. (NotNull)
     * @param aliasName The alias of the name. The property should exists on the entity. (NotNull)
     * @param opLambda The callback for option of DerivedReferrer. e.g. you can use a coalesce function. (NotNull)
     */
    public void avg(SubQuery<REFERRER_CB> derivedCBLambda, String aliasName, FunctionFilterOptionCall<DerivedReferrerOption> opLambda) {
        doAvg(derivedCBLambda, aliasName, createDerivedReferrerOption(opLambda));
    }

    protected void doAvg(SubQuery<REFERRER_CB> subQuery, String aliasName, DerivedReferrerOption option) {
        doSetupQuery("avg", subQuery, aliasName, option);
    }

    protected void doSetupQuery(String function, SubQuery<REFERRER_CB> subQuery, String aliasName, DerivedReferrerOption option) {
        assertSubQuery(subQuery);
        assertAliasName(aliasName);
        _querySetupper.setup(function, subQuery, _localCQ, filterAliasName(aliasName), option);
    }

    // -----------------------------------------------------
    //                                       User Definition
    //                                       ---------------
    /**
     * Basically for database dependency (DBMS sub-class). {Internal} <br>
     * Not public because of condition-bean policy: cannot input SQL string.
     * @param derivedCBLambda The callback for sub-query of referrer. (NotNull)
     * @param aliasName The alias of the name. The property should exists on the entity. (NotNull)
     * @param function The function expression e.g. sum, max (NotNull)
     * @param opLambda The callback for option of DerivedReferrer. e.g. you can use a coalesce function. (NotNull)
     */
    protected void userDef(SubQuery<REFERRER_CB> derivedCBLambda, String aliasName, String function,
            FunctionFilterOptionCall<DerivedReferrerOption> opLambda) { // closet
        doUserDef(derivedCBLambda, aliasName, function, createDerivedReferrerOption(opLambda));
    }

    protected void doUserDef(SubQuery<REFERRER_CB> subQuery, String aliasName, String function, DerivedReferrerOption option) {
        assertSubQuery(subQuery);
        assertAliasName(aliasName);
        assertUserDefFunction(aliasName, function);
        _querySetupper.setup(function, subQuery, _localCQ, filterAliasName(aliasName), option);
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected DerivedReferrerOption createDerivedReferrerOption(FunctionFilterOptionCall<DerivedReferrerOption> opLambda) {
        assertDerivedReferrerOption(opLambda);
        final DerivedReferrerOption option = newDerivedReferrerOption();
        opLambda.callback(option);
        return option;
    }

    protected DerivedReferrerOption newDerivedReferrerOption() {
        return _sdrOpFactory.create();
    }

    protected void assertSubQuery(SubQuery<?> subQuery) {
        if (subQuery == null) {
            String msg = "The argument 'subQuery' for DerivedReferrer should not be null.";
            throw new IllegalArgumentException(msg);
        }
    }

    protected void assertAliasName(String aliasName) {
        doAssertInvalidAliasName(aliasName);
        doAssertConflictAliasName(aliasName);
        // *this check was moved to runtime (when creating a behavior command)
        //String tableDbName = _baseCB.asTableDbName();
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

    protected void doAssertInvalidAliasName(String aliasName) {
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
    }

    protected boolean isPurposeNullAlias() {
        final HpCBPurpose purpose = _baseCB.getPurpose();
        return purpose.equals(HpCBPurpose.COLUMN_QUERY) || purpose.equals(HpCBPurpose.SCALAR_SELECT)
                || purpose.equals(HpCBPurpose.DERIVED_REFERRER);
    }

    protected void throwSpecifyDerivedReferrerInvalidAliasNameException() {
        createCBExThrower().throwSpecifyDerivedReferrerInvalidAliasNameException(_localCQ);
    }

    protected void doAssertConflictAliasName(String aliasName) {
        if (isPurposeNullAlias()) {
            return;
        }
        final String mappingAliasPrefix = DerivedMappable.MAPPING_ALIAS_PREFIX;
        final String realName;
        if (aliasName.startsWith(mappingAliasPrefix)) {
            realName = Srl.substringFirstRear(aliasName, mappingAliasPrefix);
        } else {
            realName = aliasName;
        }
        final String tableDbName = _baseCB.asTableDbName();
        final DBMeta dbmeta = _dbmetaProvider.provideDBMetaChecked(tableDbName);
        if (dbmeta.hasColumn(realName)) {
            throwSpecifyDerivedReferrerConflictAliasNameException(aliasName, dbmeta.findColumnInfo(realName));
        }
    }

    protected void throwSpecifyDerivedReferrerConflictAliasNameException(String aliasName, ColumnInfo existingColumn) {
        createCBExThrower().throwSpecifyDerivedReferrerConflictAliasNameException(_localCQ, aliasName, existingColumn);
    }

    // *this check was moved to runtime (when creating a behavior command)
    //protected void throwSpecifyDerivedReferrerEntityPropertyNotFoundException(String aliasName, Class<?> entityType) {
    //    createCBExThrower().throwSpecifyDerivedReferrerEntityPropertyNotFoundException(aliasName, entityType);
    //}
    //protected String replaceString(String text, String fromText, String toText) {
    //    return Srl.replace(text, fromText, toText);
    //}

    protected void assertDerivedReferrerOption(FunctionFilterOptionCall<DerivedReferrerOption> opLambda) {
        if (opLambda == null) {
            String msg = "The argument 'opLambda' for DerivedReferrer should not be null.";
            throw new IllegalArgumentException(msg);
        }
    }

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

    protected void assertUserDefFunction(String aliasName, String function) {
        if (!Srl.isAlphabetNumberHarfAllOr(function, '_')) { // e.g. ')', ';' are NG
            String msg = "Illegal function, only alphabet or number can be allowed:";
            msg = msg + " aliasName=" + aliasName + ", function=" + function;
            throw new IllegalConditionBeanOperationException(msg);
        }
    }

    // ===================================================================================
    //                                                                    Exception Helper
    //                                                                    ================
    protected ConditionBeanExceptionThrower createCBExThrower() {
        return new ConditionBeanExceptionThrower();
    }
}
