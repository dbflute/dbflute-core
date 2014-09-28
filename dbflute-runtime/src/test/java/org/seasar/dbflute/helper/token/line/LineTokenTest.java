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
package org.seasar.dbflute.helper.token.line;

import java.util.ArrayList;
import java.util.List;

import org.seasar.dbflute.unit.core.PlainTestCase;

/**
 * @author jflute
 * @since 0.9.4 (2009/03/14 Saturday)
 */
public class LineTokenTest extends PlainTestCase {

    public void test_make_quoteAll_basic() {
        // ## Arrange ##
        LineToken impl = new LineToken();
        LineMakingOption option = new LineMakingOption();
        option.delimitateByComma();
        option.quoteAll();
        List<String> valueList = new ArrayList<String>();
        valueList.add("a");
        valueList.add("b");
        valueList.add("cc");
        valueList.add("d");
        valueList.add("e");

        // ## Act ##
        String line = impl.make(valueList, option);

        // ## Assert ##
        log(line);
        assertEquals("\"a\",\"b\",\"cc\",\"d\",\"e\"", line);
    }

    public void test_make_quoteAll_escape() {
        // ## Arrange ##
        LineToken impl = new LineToken();
        LineMakingOption option = new LineMakingOption();
        option.delimitateByComma();
        option.quoteAll();
        List<String> valueList = new ArrayList<String>();
        valueList.add("a");
        valueList.add("b");
        valueList.add("c\"c");
        valueList.add("d");
        valueList.add("e");

        // ## Act ##
        String line = impl.make(valueList, option);

        // ## Assert ##
        log(line);
        assertEquals("\"a\",\"b\",\"c\"\"c\",\"d\",\"e\"", line);
    }

    public void test_make_quoteMinimally_escape() {
        // ## Arrange ##
        LineToken impl = new LineToken();
        LineMakingOption option = new LineMakingOption();
        option.delimitateByComma();
        option.quoteMinimally();
        List<String> valueList = new ArrayList<String>();
        valueList.add("a");
        valueList.add("b");
        valueList.add("c\"c");
        valueList.add("d,d");
        valueList.add("e");

        // ## Act ##
        String line = impl.make(valueList, option);

        // ## Assert ##
        log(line);
        assertEquals("a,b,\"c\"\"c\",\"d,d\",e", line);
    }
}
