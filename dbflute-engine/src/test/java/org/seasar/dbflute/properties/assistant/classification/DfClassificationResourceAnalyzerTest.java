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
package org.seasar.dbflute.properties.assistant.classification;

import static org.seasar.dbflute.properties.assistant.classification.DfClassificationResourceAnalyzer.LN_MARK_PLAIN;

import java.util.ArrayList;
import java.util.List;

import org.seasar.dbflute.unit.core.PlainTestCase;

/**
 * @author jflute
 * @since 0.8.2 (2008/10/22 Wednesday)
 */
public class DfClassificationResourceAnalyzerTest extends PlainTestCase {

    public void test_analuze_by_lineList() {
        // ## Arrange ##
        final DfClassificationResourceAnalyzer analyzer = new DfClassificationResourceAnalyzer();
        final List<String> lineList = new ArrayList<String>();
        lineList.add("<encoding=\"UTF-8\"/>");
        lineList.add("AAAAAAAAAAAA");
        lineList.add("BBBB");
        lineList.add("[会員ステータス]: MEMBER_STATUS_CODE");
        lineList.add("$ MemberStatus, 会員の状態を示す");
        lineList.add("- FML, Formalized, 正式会員, 正式な会員を示す");
        lineList.add("- PVS, Provisinal, 仮会員, 仮の会員を示す");
        lineList.add("- WDL, Withdrawal, 退会会員, 退会した会員を示す");
        lineList.add("CCCCCCCCCCCC");
        lineList.add("[会員ステータス2]: *_FLG");
        lineList.add("$ MemberStatus2, 会員の状態を示す");
        lineList.add("- FML, Formalized, 正式会員, 正式な会員を示す");
        lineList.add("- PVS, Provisinal, 仮会員, 仮の会員を示す");
        lineList.add("DDDDDDDDDD");
        lineList.add("- WDL, Withdrawal, 退会会員, 退会した会員を示す");
        lineList.add("EEEEEEEEEEEEE");
        lineList.add("[Dummy]");
        lineList.add("FFFFFFFFFFFFFFFFFF");
        lineList.add("[Dummy2]");
        lineList.add("$ ABC, DEF");
        lineList.add("HHHHHHHHHHHHHHHHHHHHHH");
        lineList.add("[会員ステータス3]");
        lineList.add("$ MemberStatus3");
        lineList.add("- FML, Formalized");
        lineList.add("- PVS, Provisinal");
        lineList.add("IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII");

        // ## Act ##
        final List<DfClassificationTop> classificationTopList = analyzer.analyze(lineList);

        // ## Assert ##
        assertNotNull(classificationTopList);
        assertFalse(classificationTopList.isEmpty());
        assertEquals(3, classificationTopList.size());
        {
            final DfClassificationTop top = classificationTopList.get(0);
            assertEquals("MemberStatus", top.getClassificationName());
            assertEquals("会員ステータス: 会員の状態を示す", top.getTopComment());
            assertEquals("MEMBER_STATUS_CODE", top.getRelatedColumnName());
            assertEquals(3, top.getClassificationElementList().size());
            final List<DfClassificationElement> classificationElementList = top.getClassificationElementList();
            {
                final DfClassificationElement element = classificationElementList.get(0);
                assertEquals("FML", element.getCode());
                assertEquals("Formalized", element.getName());
                assertEquals("正式会員", element.getAlias());
                assertEquals("正式な会員を示す", element.getComment());
            }
            {
                final DfClassificationElement element = classificationElementList.get(1);
                assertEquals("PVS", element.getCode());
                assertEquals("Provisinal", element.getName());
                assertEquals("仮会員", element.getAlias());
                assertEquals("仮の会員を示す", element.getComment());
            }
            {
                final DfClassificationElement element = classificationElementList.get(2);
                assertEquals("WDL", element.getCode());
                assertEquals("Withdrawal", element.getName());
                assertEquals("退会会員", element.getAlias());
                assertEquals("退会した会員を示す", element.getComment());
            }
        }
        {
            final DfClassificationTop top = classificationTopList.get(1);
            assertEquals("MemberStatus2", top.getClassificationName());
            assertEquals("会員ステータス2: 会員の状態を示す", top.getTopComment());
            assertEquals("suffix:_FLG", top.getRelatedColumnName());
            assertEquals(2, top.getClassificationElementList().size());
            final List<DfClassificationElement> classificationElementList = top.getClassificationElementList();
            {
                final DfClassificationElement element = classificationElementList.get(0);
                assertEquals("FML", element.getCode());
                assertEquals("Formalized", element.getName());
                assertEquals("正式会員", element.getAlias());
                assertEquals("正式な会員を示す", element.getComment());
            }
            {
                final DfClassificationElement element = classificationElementList.get(1);
                assertEquals("PVS", element.getCode());
                assertEquals("Provisinal", element.getName());
                assertEquals("仮会員", element.getAlias());
                assertEquals("仮の会員を示す", element.getComment());
            }
        }
        {
            final DfClassificationTop top = classificationTopList.get(2);
            assertEquals("MemberStatus3", top.getClassificationName());
            assertEquals(null, top.getTopComment());
            assertNull(top.getRelatedColumnName());
            assertEquals(2, top.getClassificationElementList().size());
            final List<DfClassificationElement> classificationElementList = top.getClassificationElementList();
            {
                final DfClassificationElement element = classificationElementList.get(0);
                assertEquals("FML", element.getCode());
                assertEquals("Formalized", element.getName());
                assertNull(element.getAlias());
                assertNull(element.getComment());
            }
            {
                final DfClassificationElement element = classificationElementList.get(1);
                assertEquals("PVS", element.getCode());
                assertEquals("Provisinal", element.getName());
                assertNull(element.getAlias());
                assertNull(element.getComment());
            }
        }
    }

    public void test_containsLineSeparatorMark() {
        // ## Arrange ##
        final DfClassificationResourceAnalyzer analyzer = new DfClassificationResourceAnalyzer();

        // ## Act & Assert ##
        assertFalse(analyzer.containsLineSeparatorMark("foobar"));
        assertFalse(analyzer.containsLineSeparatorMark("foo\nbar"));
        assertTrue(analyzer.containsLineSeparatorMark("foo" + LN_MARK_PLAIN + "bar"));
        assertTrue(analyzer.containsLineSeparatorMark("foo&#xA;bar"));
        assertTrue(analyzer.containsLineSeparatorMark("foo" + LN_MARK_PLAIN + "&#x0A;bar"));
    }

    public void test_tokenizedLineSeparatorMark_base16() {
        // ## Arrange ##
        final DfClassificationResourceAnalyzer analyzer = new DfClassificationResourceAnalyzer();
        final String plain = LN_MARK_PLAIN;
        final String xml = "&#x0D;&#x0A;";

        // ## Act & Assert ##
        assertEquals("foobar", analyzer.tokenizedLineSeparatorMark("foobar").get(0));
        assertEquals("foo", analyzer.tokenizedLineSeparatorMark("foo" + plain + "bar").get(0));
        assertEquals("bar", analyzer.tokenizedLineSeparatorMark("foo" + plain + "bar").get(1));
        assertEquals("foo", analyzer.tokenizedLineSeparatorMark("foo" + xml + "bar").get(0));
        assertEquals("bar", analyzer.tokenizedLineSeparatorMark("foo" + xml + "bar").get(1));
        assertEquals("foo", analyzer.tokenizedLineSeparatorMark("foo" + xml + "&" + plain + "bar").get(0));
        assertEquals("&", analyzer.tokenizedLineSeparatorMark("foo" + xml + "&" + plain + "bar").get(1));
        assertEquals("bar", analyzer.tokenizedLineSeparatorMark("foo" + plain + "&" + xml + "bar").get(2));
    }

    public void test_tokenizedLineSeparatorMark_base10() {
        // ## Arrange ##
        final DfClassificationResourceAnalyzer analyzer = new DfClassificationResourceAnalyzer();
        final String plain = LN_MARK_PLAIN;
        final String xml = "&#13;";

        // ## Act & Assert ##
        assertEquals("foobar", analyzer.tokenizedLineSeparatorMark("foobar").get(0));
        assertEquals("foo", analyzer.tokenizedLineSeparatorMark("foo" + plain + "bar").get(0));
        assertEquals("bar", analyzer.tokenizedLineSeparatorMark("foo" + plain + "bar").get(1));
        assertEquals("foo", analyzer.tokenizedLineSeparatorMark("foo" + xml + "bar").get(0));
        assertEquals("bar", analyzer.tokenizedLineSeparatorMark("foo" + xml + "bar").get(1));
        assertEquals("foo", analyzer.tokenizedLineSeparatorMark("foo" + xml + "&" + plain + "bar").get(0));
        assertEquals("&", analyzer.tokenizedLineSeparatorMark("foo" + xml + "&" + plain + "bar").get(1));
        assertEquals("bar", analyzer.tokenizedLineSeparatorMark("foo" + plain + "&" + xml + "bar").get(2));
    }
}
