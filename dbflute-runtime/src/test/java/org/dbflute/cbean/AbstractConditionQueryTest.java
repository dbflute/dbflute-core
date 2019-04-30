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
package org.dbflute.cbean;

import org.dbflute.cbean.ckey.ConditionKey;
import org.dbflute.unit.RuntimeTestCase;

/**
 * @author jflute
 * @since 0.9.5 (2009/04/08 Wednesday)
 */
public class AbstractConditionQueryTest extends RuntimeTestCase {

    public void test_isConditionKeyInScope() throws Exception {
        // ## Arrange & Act & Assert ##
        assertTrue(AbstractConditionQuery.isConditionKeyInScope(ConditionKey.CK_IN_SCOPE));
        assertFalse(AbstractConditionQuery.isConditionKeyInScope(ConditionKey.CK_NOT_IN_SCOPE));
    }
}
