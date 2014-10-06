/*
 * Copyright 2014-2014 the original author or authors.
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
package org.dbflute.cbean.ordering;

import java.util.Date;
import java.util.List;

import org.dbflute.cbean.chelper.HpManualOrderThemeListHandler;
import org.dbflute.cbean.chelper.HpMobCaseWhenElement;
import org.dbflute.cbean.chelper.HpMobConnectionMode;
import org.dbflute.cbean.ckey.ConditionKey;
import org.dbflute.unit.PlainTestCase;
import org.dbflute.util.DfTypeUtil;

/**
 * @author jflute
 * @since 0.9.8.2 (2011/04/08 Friday)
 */
public class ManualOrderOptionTest extends PlainTestCase {

    public void test_DateFromTo() throws Exception {
        // ## Arrange ##
        Date fromDate = DfTypeUtil.toDate("1969/01/01");
        Date toDate = DfTypeUtil.toDate("1970/12/31");
        ManualOrderOption mob = new ManualOrderOption();

        // ## Act ##
        mob.when_FromTo(fromDate, toDate, op -> op.compareAsDate());

        // ## Assert ##
        List<HpMobCaseWhenElement> caseWhenAcceptedList = mob.getCaseWhenAcceptedList();
        {
            assertEquals(1, caseWhenAcceptedList.size());
            final HpMobCaseWhenElement fromElement = caseWhenAcceptedList.get(0);
            assertEquals(ConditionKey.CK_GREATER_EQUAL, fromElement.getConditionKey());
            assertNull(fromElement.getConnectionMode());
            HpMobCaseWhenElement toElement = fromElement.getConnectedElementList().get(0);
            assertEquals(ConditionKey.CK_LESS_THAN, toElement.getConditionKey());
            assertEquals(HpMobConnectionMode.AND, toElement.getConnectionMode());
            assertEquals(0, mob.getCaseWhenBoundList().size());

            String fromExp = DfTypeUtil.toString(fromElement.getOrderValue(), "yyyy/MM/dd");
            String toExp = DfTypeUtil.toString(toElement.getOrderValue(), "yyyy/MM/dd");
            assertEquals("1969/01/01", fromExp);
            assertEquals("1971/01/01", toExp);
        }

        // ## Act ##
        mob.bind(new HpManualOrderThemeListHandler() {
            int index = 0;

            public String register(String themeKey, Object orderValue) {
                ++index;
                return "foo" + index;
            }
        });

        // ## Assert ##
        List<HpMobCaseWhenElement> caseWhenBoundList = mob.getCaseWhenBoundList();
        {
            assertNotSame(0, caseWhenBoundList.size());
            assertEquals(1, caseWhenBoundList.size());
            final HpMobCaseWhenElement fromElement = caseWhenBoundList.get(0);
            assertEquals(ConditionKey.CK_GREATER_EQUAL, fromElement.getConditionKey());
            assertNull(fromElement.getConnectionMode());
            HpMobCaseWhenElement toElement = fromElement.getConnectedElementList().get(0);
            assertEquals(ConditionKey.CK_LESS_THAN, toElement.getConditionKey());
            assertEquals(HpMobConnectionMode.AND, toElement.getConnectionMode());
        }
    }
}
