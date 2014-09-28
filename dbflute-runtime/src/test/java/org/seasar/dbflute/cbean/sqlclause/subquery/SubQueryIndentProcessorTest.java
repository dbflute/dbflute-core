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
package org.seasar.dbflute.cbean.sqlclause.subquery;

import org.seasar.dbflute.unit.core.PlainTestCase;

/**
 * @author jflute
 * @since 0.9.8.6 (2011/06/24 Friday)
 */
public class SubQueryIndentProcessorTest extends PlainTestCase {

    public void test_moveSubQueryEndToRearOnLastLine_basic() throws Exception {
        // ## Arrange ##
        String sqend = "--#df:sqend#memberId_SpecifyDerivedReferrer_PurchaseList.subQueryMapKey1[1]#df:idterm#";
        String exp = "(select ... from ... where ..." + sqend + ") + 123";

        // ## Act ##
        String moved = SubQueryIndentProcessor.moveSubQueryEndToRear(exp);

        // ## Assert ##
        log(moved);
        assertEquals("(select ... from ... where ...) + 123" + sqend, moved);
    }
}
