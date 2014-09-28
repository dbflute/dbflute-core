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
package org.seasar.dbflute.helper.filesystem;

import org.seasar.dbflute.unit.core.PlainTestCase;

/**
 * @author jflute
 * @since 1.0.5K (2014/08/16 Saturday)
 */
public class FileTextIOTest extends PlainTestCase {

    // ===================================================================================
    //                                                                      filterAsLine()
    //                                                                      ==============
    public void test_filterAsLine_noFilter() throws Exception {
        // ## Arrange ##
        FileTextIO textIO = new FileTextIO().encodeAsUTF8();
        StringBuilder sb = new StringBuilder();
        sb.append("foo\n");
        sb.append("bar\r\n");
        sb.append(" qux \n");
        String text = sb.toString();

        // ## Act ##
        String filteredText = textIO.filterAsLine(text, new FileTextLineFilter() {
            public String filter(String line) {
                return line;
            }
        });

        // ## Assert ##
        log(filteredText);
        assertEquals(text, filteredText);
    }

    public void test_filterAsLine_somethingFiltered() throws Exception {
        // ## Arrange ##
        FileTextIO textIO = new FileTextIO().encodeAsUTF8();
        StringBuilder sb = new StringBuilder();
        sb.append("foo\n");
        sb.append("bar\r\n");
        sb.append(" qux \n");
        String text = sb.toString();

        // ## Act ##
        String filteredText = textIO.filterAsLine(text, new FileTextLineFilter() {
            public String filter(String line) {
                return replace(line, "bar", "");
            }
        });

        // ## Assert ##
        log(filteredText);
        assertEquals(replace(text, "bar", ""), filteredText);
    }

    public void test_filterAsLine_removeLine() throws Exception {
        // ## Arrange ##
        FileTextIO textIO = new FileTextIO().encodeAsUTF8();
        StringBuilder sb = new StringBuilder();
        sb.append("foo\n");
        sb.append("bar\r\n");
        sb.append(" qux \n");
        String text = sb.toString();

        // ## Act ##
        String filteredText = textIO.filterAsLine(text, new FileTextLineFilter() {
            public String filter(String line) {
                if (line.contains("ar")) {
                    return null;
                }
                return line;
            }
        });

        // ## Assert ##
        log(filteredText);
        StringBuilder expectedSb = new StringBuilder();
        expectedSb.append("foo\n");
        expectedSb.append(" qux \n");
        String expectedText = expectedSb.toString();
        assertEquals(expectedText, filteredText);
    }

    // ===================================================================================
    //                                                                             Illegal
    //                                                                             =======
    public void test_illegal_noEncoding() throws Exception {
        // ## Arrange ##
        FileTextIO textIO = new FileTextIO();

        // ## Act ##
        try {
            textIO.read("foo");
            // ## Assert ##
            fail();
        } catch (IllegalStateException e) {
            log(e.getMessage());
        }
    }
}
