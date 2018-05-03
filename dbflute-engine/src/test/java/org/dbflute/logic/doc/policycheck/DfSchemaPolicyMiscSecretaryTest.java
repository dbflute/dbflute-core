/*
 * Copyright 2014-2018 the original author or authors.
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
package org.dbflute.logic.doc.policycheck;

import org.dbflute.logic.doc.spolicy.secretary.DfSPolicyLogicalSecretary;
import org.dbflute.unit.EngineTestCase;

/**
 * @author jflute
 * @since 1.1.2 (2016/12/31 Saturday)
 */
public class DfSchemaPolicyMiscSecretaryTest extends EngineTestCase {

    public void test_isHitExp_basic() {
        // ## Arrange ##
        DfSPolicyLogicalSecretary secretary = new DfSPolicyLogicalSecretary();

        // ## Act ##
        // ## Assert ##
        assertTrue(secretary.isHitExp("SEA_MEMBER", "prefix:SEA_"));
        assertFalse(secretary.isHitExp("SEA_MEMBER", "prefix:LAND_"));
        assertTrue(secretary.isHitExp("SEA_MEMBER", "prefix:SEA_ and suffix:_MEMBER"));
        assertTrue(secretary.isHitExp("SEA_MEMBER", "prefix:SEA_ or prefix:LAND_"));
        assertTrue(secretary.isHitExp("LAND_MEMBER", "prefix:SEA_ or prefix:LAND_"));
        assertFalse(secretary.isHitExp("PIARI_MEMBER", "prefix:SEA_ or prefix:LAND_"));
    }
}
