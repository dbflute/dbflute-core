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

import java.io.Serializable;

import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public enum HpCBPurpose {

    NORMAL_USE(new HpSpec()) // basic (all functions can be used)

    , UNION_QUERY(new HpSpec().noSetupSelect().noSpecify().noOrderBy()) // Union

    , EXISTS_REFERRER(new HpSpec().noSetupSelect().noSpecify().noOrderBy().subQuery()) // ExistsReferrer

    , IN_SCOPE_RELATION(new HpSpec().noSetupSelect().noSpecify().noOrderBy().subQuery()) // InScopeRelation

    , DERIVED_REFERRER(new HpSpec().noSetupSelect().noSpecifyColumnTwoOrMore().noSpecifyColumnWithDerivedReferrer()
            .noSpecifyDerivedReferrerTwoOrMore().noOrderBy().subQuery()) // DerivedReferrer

    , SCALAR_SELECT(new HpSpec().noSetupSelect().noSpecifyColumnTwoOrMore().noSpecifyColumnWithDerivedReferrer()
            .noSpecifyDerivedReferrerTwoOrMore().noSpecifyRelation().noOrderBy()) // ScalarSelect

    , SCALAR_CONDITION(new HpSpec().noSetupSelect().noSpecifyColumnTwoOrMore().noSpecifyRelation()
            .noSpecifyDerivedReferrer().noOrderBy().subQuery()) // ScalarCondition

    , SCALAR_CONDITION_PARTITION_BY(new HpSpec().noSetupSelect().noSpecifyColumnTwoOrMore().noSpecifyRelation()
            .noSpecifyDerivedReferrer().noQuery().noOrderBy().subQuery()) // ScalarConditionPartitionBy

    , MYSELF_EXISTS(new HpSpec().noSetupSelect().noSpecifyRelation().noSpecifyColumnTwoOrMore()
            .noSpecifyColumnWithDerivedReferrer().noOrderBy().subQuery()) // MyselfExists
    , MYSELF_IN_SCOPE(new HpSpec().noSetupSelect().noSpecifyRelation().noSpecifyColumnTwoOrMore()
            .noSpecifyColumnWithDerivedReferrer().noOrderBy().subQuery()) // MyselfInScope

    // A purpose that can specify but not allowed to query
    // needs to switch condition-bean used in specification
    // to non-checked condition-bean.
    // Because specification uses query internally.
    , COLUMN_QUERY(new HpSpec().noSetupSelect().noSpecifyColumnTwoOrMore().noSpecifyColumnWithDerivedReferrer()
            .noSpecifyDerivedReferrerTwoOrMore().noQuery()) // ColumnQuery

    , OR_SCOPE_QUERY(new HpSpec().noSetupSelect().noSpecify().noOrderBy()) // OrScopeQuery

    , DREAM_CRUISE(new HpSpec().noSetupSelect().noSpecifyColumnWithDerivedReferrer()
            .noSpecifyDerivedReferrerTwoOrMore().noQuery()) // DreamCruise

    , VARYING_UPDATE(new HpSpec().noSetupSelect().noSpecifyColumnTwoOrMore().noSpecifyRelation()
            .noSpecifyDerivedReferrer().noQuery()) // VaryingUpdate

    , SPECIFIED_UPDATE(new HpSpec().noSetupSelect().noSpecifyRelation().noSpecifyDerivedReferrer().noQuery()) // SpecifiedUpdate

    // for intoCB (not for resourceCB)
    , QUERY_INSERT(new HpSpec().noSetupSelect().noSpecifyDerivedReferrer().noSpecifyRelation().noQuery().noOrderBy()) // QueryInsert

    // QueryUpdate and QueryDelete are not defined here
    // because their condition-beans are created by an application
    // (not call-back style)
    //, QUERY_UPDATE(new HpSpec().noWaySetupSelect().noWaySpecify().noWayOrderBy()) // QueryUpdate
    //, QUERY_DELETE(new HpSpec().noWaySetupSelect().noWaySpecify().noWayOrderBy()) // QueryDelete
    ;

    private final HpSpec _spec;

    private HpCBPurpose(HpSpec spec) {
        _spec = spec;
    }

    public boolean isAny(HpCBPurpose... purposes) {
        for (HpCBPurpose purpose : purposes) {
            if (equals(purpose)) {
                return true;
            }
        }
        return false;
    }

    // any checks are not implemented
    // because it's so touch

    public boolean isNoSetupSelect() {
        return _spec.isNoSetupSelect();
    }

    public boolean isNoSpecify() {
        return _spec.isNoSpecify();
    }

    public boolean isNoSpecifyColumnTwoOrMore() {
        return _spec.isNoSpecifyColumnTwoOrMore();
    }

    public boolean isNoSpecifyColumnWithDerivedReferrer() {
        return _spec.isNoSpecifyColumnWithDerivedReferrer();
    }

    public boolean isNoSpecifyRelation() {
        return _spec.isNoSpecifyRelation();
    }

    public boolean isNoSpecifyDerivedReferrer() {
        return _spec.isNoSpecifyDerivedReferrer();
    }

    public boolean isNoSpecifyDerivedReferrerTwoOrMore() {
        return _spec.isNoSpecifyDerivedReferrerTwoOrMore();
    }

    public boolean isNoQuery() {
        return _spec.isNoQuery();
    }

    public boolean isNoOrderBy() {
        return _spec.isNoOrderBy();
    }

    public boolean isSubQuery() {
        return _spec.isSubQuery();
    }

    @Override
    public String toString() {
        return Srl.camelize(name());
    }

    public static class HpSpec implements Serializable {
        private static final long serialVersionUID = 1L;
        protected boolean _noSetupSelect;
        protected boolean _noSpecify;
        protected boolean _noSpecifyColumnTwoOrMore;
        protected boolean _noSpecifyColumnWithDerivedReferrer;
        protected boolean _noSpecifyRelation;
        protected boolean _noSpecifyDerivedReferrer;
        protected boolean _noSpecifyDerivedReferrerTwoOrMore;
        protected boolean _noQuery;
        protected boolean _noOrderBy;
        protected boolean _subQuery;

        public HpSpec noSetupSelect() {
            _noSetupSelect = true;
            return this;
        }

        public HpSpec noSpecify() {
            _noSpecify = true;
            return this;
        }

        public HpSpec noSpecifyColumnTwoOrMore() {
            _noSpecifyColumnTwoOrMore = true;
            return this;
        }

        public HpSpec noSpecifyColumnWithDerivedReferrer() {
            _noSpecifyColumnWithDerivedReferrer = true;
            return this;
        }

        public HpSpec noSpecifyRelation() {
            _noSpecifyRelation = true;
            return this;
        }

        public HpSpec noSpecifyDerivedReferrer() {
            _noSpecifyDerivedReferrer = true;
            return this;
        }

        public HpSpec noSpecifyDerivedReferrerTwoOrMore() {
            _noSpecifyDerivedReferrerTwoOrMore = true;
            return this;
        }

        public HpSpec noQuery() {
            _noQuery = true;
            return this;
        }

        public HpSpec noOrderBy() {
            _noOrderBy = true;
            return this;
        }

        public HpSpec subQuery() {
            _subQuery = true;
            return this;
        }

        public boolean isNoSetupSelect() {
            return _noSetupSelect;
        }

        public boolean isNoSpecify() {
            return _noSpecify;
        }

        public boolean isNoSpecifyColumnTwoOrMore() {
            return _noSpecifyColumnTwoOrMore;
        }

        public boolean isNoSpecifyColumnWithDerivedReferrer() {
            return _noSpecifyColumnWithDerivedReferrer;
        }

        public boolean isNoSpecifyRelation() {
            return _noSpecifyRelation;
        }

        public boolean isNoSpecifyDerivedReferrer() {
            return _noSpecifyDerivedReferrer;
        }

        public boolean isNoSpecifyDerivedReferrerTwoOrMore() {
            return _noSpecifyDerivedReferrerTwoOrMore;
        }

        public boolean isNoQuery() {
            return _noQuery;
        }

        public boolean isNoOrderBy() {
            return _noOrderBy;
        }

        public boolean isSubQuery() {
            return _subQuery;
        }
    }
}
