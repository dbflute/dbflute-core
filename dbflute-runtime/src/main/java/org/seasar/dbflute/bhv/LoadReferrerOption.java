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
package org.seasar.dbflute.bhv;

import java.util.List;

import org.seasar.dbflute.Entity;
import org.seasar.dbflute.cbean.ConditionBean;

/**
 * The class of load referrer option. #beforejava8 <br />
 * This option is basically for loading second or more level referrer.
 * @param <REFERRER_CB> The type of referrer condition-bean.
 * @param <REFERRER_ENTITY> The type of referrer entity.
 * @author jflute
 */
public class LoadReferrerOption<REFERRER_CB extends ConditionBean, REFERRER_ENTITY extends Entity> {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected ReferrerConditionSetupper<REFERRER_CB> _referrerConditionSetupper;
    protected ConditionBeanSetupper<REFERRER_CB> _conditionBeanSetupper;
    protected EntityListSetupper<REFERRER_ENTITY> _entityListSetupper;
    protected REFERRER_CB _referrerConditionBean;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * Constructor. <br />
     * This option is basically for loading second or more level referrer like this:
     * <pre>
     * <span style="color: #3F7E5E">// base point table is MEMBER</span>
     * MemberCB cb = new MemberCB();
     * ListResultBean&lt;Member&gt; memberList = memberBhv.selectList(cb);
     * 
     * LoadReferrerOption option = new LoadReferrerOption();
     * 
     * <span style="color: #3F7E5E">// PURCHASE (first level referrer from MEMBER)</span>
     * option.setReferrerConditionSetupper(new ReferrerConditionSetupper&lt;PurchaseCB&gt;() {
     *     public void setup(PurchaseCB cb) {
     *         cb.query().addOrderBy_PurchaseDatetime_Desc();
     *     }
     * });
     * 
     * <span style="color: #3F7E5E">// PURCHASE_DETAIL (second level referrer from PURCHASE)</span>
     * option.<span style="color: #DD4747">setEntityListSetupper</span>(new EntityListSetupper&lt;Purchase&gt;() {
     *     public void setup(List&lt;Purchase&gt; entityList) {
     *         purchaseBhv.loadPurchaseDetailList(entityList, new ConditionBeanSetupper&lt;PurchaseDetailCB&gt;() {
     *             public void setup(PurchaseDetailCB cb) {
     *                 ...
     *             }
     *         });
     *     }
     * });
     * 
     * memberStatusBhv.loadMemberList(memberList, option);
     * </pre>
     */
    public LoadReferrerOption() {
    }

    public LoadReferrerOption<REFERRER_CB, REFERRER_ENTITY> xinit(
            ReferrerConditionSetupper<REFERRER_CB> referrerConditionSetupper) { // internal
        setReferrerConditionSetupper(referrerConditionSetupper);
        return this;
    }

    public LoadReferrerOption<REFERRER_CB, REFERRER_ENTITY> xinit(
            ConditionBeanSetupper<REFERRER_CB> conditionBeanSetupper) { // internal
        setConditionBeanSetupper(conditionBeanSetupper);
        return this;
    }

    // ===================================================================================
    //                                                                         Easy-to-Use
    //                                                                         ===========
    public void delegateConditionBeanSettingUp(REFERRER_CB cb) { // internal
        if (_referrerConditionSetupper != null) {
            _referrerConditionSetupper.setup(cb);
        } else if (_conditionBeanSetupper != null) {
            _conditionBeanSetupper.setup(cb);
        }
    }

    public void delegateEntitySettingUp(List<REFERRER_ENTITY> entityList) { // internal
        if (_entityListSetupper != null) {
            _entityListSetupper.setup(entityList);
        }
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public ReferrerConditionSetupper<REFERRER_CB> getReferrerConditionSetupper() {
        return _conditionBeanSetupper;
    }

    /**
     * Set the set-upper of condition-bean for first level referrer. <br />
     * <pre>
     * LoadReferrerOption option = new LoadReferrerOption();
     * 
     * <span style="color: #3F7E5E">// PURCHASE (first level referrer from MEMBER)</span>
     * option.<span style="color: #DD4747">setReferrerConditionSetupper</span>(new ReferrerConditionSetupper&lt;PurchaseCB&gt;() {
     *     public void setup(PurchaseCB cb) {
     *         cb.query().addOrderBy_PurchaseDatetime_Desc();
     *     }
     * });
     * ...
     * </pre>
     * @param referrerConditionSetupper The set-upper of condition-bean for referrer. (NullAllowed: if null, means no condition for a first level referrer)
     */
    public void setReferrerConditionSetupper(ReferrerConditionSetupper<REFERRER_CB> referrerConditionSetupper) {
        _referrerConditionSetupper = referrerConditionSetupper;
    }

    public ConditionBeanSetupper<REFERRER_CB> getConditionBeanSetupper() {
        return _conditionBeanSetupper;
    }

    /**
     * Set the set-upper of condition-bean for first level referrer. <br />
     * <pre>
     * LoadReferrerOption option = new LoadReferrerOption();
     * 
     * <span style="color: #3F7E5E">// PURCHASE (first level referrer from MEMBER)</span>
     * option.<span style="color: #DD4747">setConditionBeanSetupper</span>(new ConditionBeanSetupper&lt;PurchaseCB&gt;() {
     *     public void setup(PurchaseCB cb) {
     *         cb.query().addOrderBy_PurchaseDatetime_Desc();
     *     }
     * });
     * ...
     * </pre>
     * @param conditionBeanSetupper The set-upper of condition-bean. (NullAllowed: if null, means no condition for a first level referrer)
     */
    public void setConditionBeanSetupper(ConditionBeanSetupper<REFERRER_CB> conditionBeanSetupper) {
        _conditionBeanSetupper = conditionBeanSetupper;
    }

    public EntityListSetupper<REFERRER_ENTITY> getEntityListSetupper() {
        return _entityListSetupper;
    }

    /**
     * Set the set-upper of entity list for second or more level referrer. <br />
     * <pre>
     * LoadReferrerOption loadReferrerOption = new LoadReferrerOption();
     * ...
     * <span style="color: #3F7E5E">// PURCHASE (second level referrer)</span>
     * loadReferrerOption.<span style="color: #DD4747">setEntityListSetupper</span>(new EntityListSetupper&lt;Member&gt;() {
     *     public void setup(List&lt;Member&gt; entityList) {
     *         memberBhv.loadPurchaseList(entityList, new ConditionBeanSetupper&lt;PurchaseCB&gt;() {
     *             public void setup(PurchaseCB cb) {
     *                 cb.query().addOrderBy_PurchaseCount_Desc();
     *                 cb.query().addOrderBy_ProductId_Desc();
     *             }
     *         });
     *     }
     * });
     * </pre>
     * @param entityListSetupper The set-upper of entity list. (NullAllowed: if null, means no loading for second level referrer)
     */
    public void setEntityListSetupper(EntityListSetupper<REFERRER_ENTITY> entityListSetupper) {
        this._entityListSetupper = entityListSetupper;
    }

    public REFERRER_CB getReferrerConditionBean() {
        return _referrerConditionBean;
    }

    /**
     * Set the original instance of condition-bean for first level referrer. <br />
     * use this, if you want to set the original instance.
     * @param referrerConditionBean The original instance of condition-bean. (NullAllowed: if null, means normal)
     */
    public void setReferrerConditionBean(REFERRER_CB referrerConditionBean) {
        this._referrerConditionBean = referrerConditionBean;
    }
}
