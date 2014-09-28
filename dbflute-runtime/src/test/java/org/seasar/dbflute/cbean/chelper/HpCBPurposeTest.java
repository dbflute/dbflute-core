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

import junit.framework.TestCase;

/**
 * @author jflute
 */
public class HpCBPurposeTest extends TestCase {

    public void test_spec_NormalUse() {
        // ## Arrange ##
        HpCBPurpose purpose = HpCBPurpose.NORMAL_USE; // all can be used

        // ## Act & Assert ##
        assertFalse(purpose.isNoSetupSelect());
        assertFalse(purpose.isNoSpecify());
        assertFalse(purpose.isNoSpecifyColumnTwoOrMore());
        assertFalse(purpose.isNoSpecifyRelation());
        assertFalse(purpose.isNoSpecifyDerivedReferrer());
        assertFalse(purpose.isNoQuery());
        assertFalse(purpose.isNoOrderBy());
    }

    public void test_toString() {
        assertEquals("NormalUse", HpCBPurpose.NORMAL_USE.toString());
        assertEquals("UnionQuery", HpCBPurpose.UNION_QUERY.toString());
        assertEquals("ExistsReferrer", HpCBPurpose.EXISTS_REFERRER.toString());
        assertEquals("InScopeRelation", HpCBPurpose.IN_SCOPE_RELATION.toString());
        assertEquals("DerivedReferrer", HpCBPurpose.DERIVED_REFERRER.toString());
        assertEquals("ScalarSelect", HpCBPurpose.SCALAR_SELECT.toString());
        assertEquals("ScalarCondition", HpCBPurpose.SCALAR_CONDITION.toString());
        assertEquals("ColumnQuery", HpCBPurpose.COLUMN_QUERY.toString());
        assertEquals("VaryingUpdate", HpCBPurpose.VARYING_UPDATE.toString());
    }
}
