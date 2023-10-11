/*
 * Copyright 2014-2023 the original author or authors.
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
import org.dbflute.cbean.scoping.SpecifyQuery;
import org.dbflute.cbean.sqlclause.SqlClause;

/**
 * The customized info for ScalarCondition (the old name: ScalarSubQuery).
 * @param <CB> The type of condition-bean.
 * @author jflute
 */
public class HpSLCCustomized<CB extends ConditionBean> {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected SpecifyQuery<CB> _partitionBySpecify;
    protected CB _partitionByCBean;

    // ===================================================================================
    //                                                                            Behavior
    //                                                                            ========
    public boolean hasPartitionBy() {
        return _partitionBySpecify != null;
    }

    public SqlClause preparePartitionBySqlClause() {
        if (_partitionBySpecify == null) {
            return null;
        }
        _partitionBySpecify.specify(_partitionByCBean);
        return _partitionByCBean.getSqlClause();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public SpecifyQuery<CB> getPartitionBySpecify() {
        return _partitionBySpecify;
    }

    public void setPartitionBySpecify(SpecifyQuery<CB> partitionBySpecify) {
        this._partitionBySpecify = partitionBySpecify;
    }

    public CB getPartitionByCBean() {
        return _partitionByCBean;
    }

    public void setPartitionByCBean(CB partitionByCBean) {
        this._partitionByCBean = partitionByCBean;
    }
}
