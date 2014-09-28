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
package org.seasar.dbflute.helper.token.file;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.seasar.dbflute.helper.token.file.exception.FileMakingInvalidValueCountException;
import org.seasar.dbflute.unit.core.PlainTestCase;

/**
 * @author jflute
 * @since 0.9.4 (2009/03/14 Saturday)
 */
public class FileTokenTest extends PlainTestCase {

    // ===================================================================================
    //                                                                            Tokenize
    //                                                                            ========
    public void test_tokenize_basic() throws Exception {
        // ## Arrange ##
        FileToken impl = new FileToken();
        final String header = "A, B, C, D, E";
        final String first = "\"a\",\"b,\",\"cc\",\"\"\"\",\"e\n,\n,\n\"\",,\"";
        final String second = "\"a\",\"\",\"c\"\"c\",\"d\"\"\",\"e\"";
        final String third = "\"a\",\"b,b\",\"c\"\",c\",\"d\n\",\"e\"";
        String all = header + ln() + first + ln() + second + ln() + third;
        ByteArrayInputStream ins = new ByteArrayInputStream(all.getBytes("UTF-8"));

        // ## Act ##
        final Set<String> markSet = new HashSet<String>();
        impl.tokenize(ins, new FileTokenizingCallback() {
            int index = 0;

            public void handleRow(FileTokenizingRowResource resource) {
                // ## Assert ##
                List<String> valueList = resource.getValueList();
                log(valueList);
                if (index == 0) {
                    assertEquals("a", valueList.get(0));
                    assertEquals("b,", valueList.get(1));
                    assertEquals("cc", valueList.get(2));
                    assertEquals("\"", valueList.get(3));
                    assertEquals("e\n,\n,\n\",,", valueList.get(4));
                    assertEquals(first, resource.getRowString());
                    Map<String, String> columnValueMap = resource.toColumnValueMap();
                    assertEquals(columnValueMap.get("A"), valueList.get(0));
                    assertEquals(columnValueMap.get("B"), valueList.get(1));
                    assertEquals(columnValueMap.get("C"), valueList.get(2));
                    assertEquals(columnValueMap.get("D"), valueList.get(3));
                    assertEquals(columnValueMap.get("E"), valueList.get(4));
                } else if (index == 1) {
                    assertEquals("a", valueList.get(0));
                    assertEquals("", valueList.get(1));
                    assertEquals("c\"c", valueList.get(2));
                    assertEquals("d\"", valueList.get(3));
                    assertEquals("e", valueList.get(4));
                    assertEquals(second, resource.getRowString());
                    Map<String, String> columnValueMap = resource.toColumnValueMap();
                    assertEquals(columnValueMap.get("A"), valueList.get(0));
                    assertEquals(columnValueMap.get("B"), valueList.get(1));
                    assertEquals(columnValueMap.get("C"), valueList.get(2));
                    assertEquals(columnValueMap.get("D"), valueList.get(3));
                    assertEquals(columnValueMap.get("E"), valueList.get(4));
                } else if (index == 2) {
                    assertEquals("a", valueList.get(0));
                    assertEquals("b,b", valueList.get(1));
                    assertEquals("c\",c", valueList.get(2));
                    assertEquals("d\n", valueList.get(3));
                    assertEquals("e", valueList.get(4));
                    assertEquals(third, resource.getRowString());
                    Map<String, String> columnValueMap = resource.toColumnValueMap();
                    assertEquals(columnValueMap.get("A"), valueList.get(0));
                    assertEquals(columnValueMap.get("B"), valueList.get(1));
                    assertEquals(columnValueMap.get("C"), valueList.get(2));
                    assertEquals(columnValueMap.get("D"), valueList.get(3));
                    assertEquals(columnValueMap.get("E"), valueList.get(4));
                    markSet.add("done");
                }

                ++index;
            }
        }, new FileTokenizingOption().delimitateByComma().encodeAsUTF8());
        assertTrue(markSet.contains("done"));
    }

    public void test_tokenize_noHeader() throws Exception {
        // ## Arrange ##
        FileToken impl = new FileToken();
        final String first = "\"a\",\"b,\",\"cc\",\"\"\"\",\"e\n,\n,\n\"\",,\"";
        final String second = "\"a\",\"\",\"c\"\"c\",\"d\"\"\",\"e\"";
        final String third = "\"a\",\"b,b\",\"c\"\",c\",\"d\n\",\"e\"";
        String all = first + ln() + second + ln() + third;
        ByteArrayInputStream ins = new ByteArrayInputStream(all.getBytes("UTF-8"));

        // ## Act ##
        final Set<String> markSet = new HashSet<String>();
        impl.tokenize(ins, new FileTokenizingCallback() {
            int index = 0;

            public void handleRow(FileTokenizingRowResource resource) {
                // ## Assert ##
                List<String> valueList = resource.getValueList();
                log(valueList);
                if (index == 0) {
                    assertEquals("a", valueList.get(0));
                    assertEquals("b,", valueList.get(1));
                    assertEquals("cc", valueList.get(2));
                    assertEquals("\"", valueList.get(3));
                    assertEquals("e\n,\n,\n\",,", valueList.get(4));
                    assertEquals(first, resource.getRowString());
                    assertNull(resource.toColumnValueMap());
                } else if (index == 1) {
                    assertEquals("a", valueList.get(0));
                    assertEquals("", valueList.get(1));
                    assertEquals("c\"c", valueList.get(2));
                    assertEquals("d\"", valueList.get(3));
                    assertEquals("e", valueList.get(4));
                    assertEquals(second, resource.getRowString());
                    assertNull(resource.toColumnValueMap());
                } else if (index == 2) {
                    assertEquals("a", valueList.get(0));
                    assertEquals("b,b", valueList.get(1));
                    assertEquals("c\",c", valueList.get(2));
                    assertEquals("d\n", valueList.get(3));
                    assertEquals("e", valueList.get(4));
                    assertEquals(third, resource.getRowString());
                    assertNull(resource.toColumnValueMap());
                    markSet.add("done");
                }

                ++index;
            }
        }, new FileTokenizingOption().beginFirstLine().delimitateByComma().encodeAsUTF8());
        assertTrue(markSet.contains("done"));
    }

    public void test_tokenize_plus() throws Exception {
        // ## Arrange ##
        FileToken impl = new FileToken();
        String first = "1001\t1\tabc";
        String second = "1002\t\t\"abc\"";
        String third = "1003\t3\t\"a\"\"bc\"";
        String all = first + ln() + second + ln() + third;
        ByteArrayInputStream ins = new ByteArrayInputStream(all.getBytes("UTF-8"));

        // ## Act ##
        final Set<String> markSet = new HashSet<String>();
        impl.tokenize(ins, new FileTokenizingCallback() {
            int index = 0;

            public void handleRow(FileTokenizingRowResource resource) {
                // ## Assert ##
                List<String> valueList = resource.getValueList();
                log(valueList);
                if (index == 0) {
                    assertEquals("1001", valueList.get(0));
                    assertEquals("1", valueList.get(1));
                    assertEquals("abc", valueList.get(2));
                } else if (index == 1) {
                    assertEquals("1002", valueList.get(0));
                    assertEquals("", valueList.get(1));
                    assertEquals("abc", valueList.get(2));
                } else if (index == 2) {
                    assertEquals("1003", valueList.get(0));
                    assertEquals("3", valueList.get(1));
                    assertEquals("a\"bc", valueList.get(2));
                    markSet.add("done");
                }
                ++index;
            }
        }, new FileTokenizingOption().beginFirstLine().delimitateByTab().encodeAsUTF8());
        assertTrue(markSet.contains("done"));
    }

    // ===================================================================================
    //                                                                                Make
    //                                                                                ====
    public void test_make_basic() throws Exception {
        // ## Arrange ##
        FileToken impl = new FileToken();
        ByteArrayOutputStream ous = new ByteArrayOutputStream();

        // ## Act ##
        List<String> columnNameList = Arrays.asList("A", "B", "C", "D", "E");
        impl.make(ous, new FileMakingCallback() {
            public void write(FileMakingRowWriter writer) throws IOException {
                List<String> valueList = new ArrayList<String>();
                {
                    valueList.add("a");
                    valueList.add("b");
                    valueList.add("cc");
                    valueList.add("d");
                    valueList.add("e");
                    writer.writeRow(valueList);
                    valueList.clear();
                }
                {
                    valueList.add("a");
                    valueList.add("\"");
                    valueList.add("c\"c");
                    valueList.add("d\"");
                    valueList.add("e");
                    writer.writeRow(valueList);
                    valueList.clear();
                }
                {
                    valueList.add("a");
                    valueList.add("b,b");
                    valueList.add("c\",c");
                    valueList.add("d\n");
                    valueList.add("e");
                    writer.writeRow(valueList);
                    valueList.clear();
                }
            }
        }, new FileMakingOption().delimitateByComma().encodeAsUTF8().separateByLf().headerInfo(columnNameList));

        // ## Assert ##
        String actual = ous.toString();
        log(actual);
        assertFalse(actual.endsWith("\n"));
        String[] split = actual.split("\n");
        assertEquals("\"A\",\"B\",\"C\",\"D\",\"E\"", split[0]);
        assertEquals("\"a\",\"b\",\"cc\",\"d\",\"e\"", split[1]);
        assertEquals("\"a\",\"\"\"\",\"c\"\"c\",\"d\"\"\",\"e\"", split[2]);
        assertEquals("\"a\",\"b,b\",\"c\"\",c\",\"d", split[3]);
        assertEquals("\",\"e\"", split[4]);
    }

    public void test_make_noHeader() throws Exception {
        // ## Arrange ##
        FileToken impl = new FileToken();
        ByteArrayOutputStream ous = new ByteArrayOutputStream();

        // ## Act ##
        impl.make(ous, new FileMakingCallback() {
            public void write(FileMakingRowWriter writer) throws IOException, SQLException {
                List<String> valueList = new ArrayList<String>();
                {
                    valueList.add("a");
                    valueList.add("b");
                    valueList.add("cc");
                    valueList.add("d");
                    valueList.add("e");
                    writer.writeRow(valueList);
                    valueList.clear();
                }
                {
                    valueList.add("a");
                    valueList.add("\"");
                    valueList.add("c\"c");
                    valueList.add("d\"");
                    valueList.add("e");
                    writer.writeRow(valueList);
                    valueList.clear();
                }
                {
                    valueList.add("a");
                    valueList.add("b,b");
                    valueList.add("c\",c");
                    valueList.add("d\n");
                    valueList.add("e");
                    writer.writeRow(valueList);
                    valueList.clear();
                }
            }
        }, new FileMakingOption().delimitateByComma().encodeAsUTF8().separateByLf());

        // ## Assert ##
        String actual = ous.toString();
        log(actual);
        assertFalse(actual.endsWith("\n"));
        String[] split = actual.split("\n");
        assertEquals("\"a\",\"b\",\"cc\",\"d\",\"e\"", split[0]);
        assertEquals("\"a\",\"\"\"\",\"c\"\"c\",\"d\"\"\",\"e\"", split[1]);
        assertEquals("\"a\",\"b,b\",\"c\"\",c\",\"d", split[2]);
        assertEquals("\",\"e\"", split[3]);
    }

    public void test_make_quoteMinimally() throws Exception {
        // ## Arrange ##
        FileToken impl = new FileToken();
        ByteArrayOutputStream ous = new ByteArrayOutputStream();

        // ## Act ##
        impl.make(ous, new FileMakingCallback() {
            public void write(FileMakingRowWriter writer) throws IOException, SQLException {
                List<String> valueList = new ArrayList<String>();
                {
                    valueList.add("a");
                    valueList.add("b");
                    valueList.add("cc");
                    valueList.add("d");
                    valueList.add("e");
                    writer.writeRow(valueList);
                    valueList.clear();
                }
                {
                    valueList.add("a");
                    valueList.add("b");
                    valueList.add("c\"c");
                    valueList.add("d");
                    valueList.add("e");
                    writer.writeRow(valueList);
                    valueList.clear();
                }
                {
                    valueList.add("a");
                    valueList.add("b,b");
                    valueList.add("c\",c");
                    valueList.add("d\n");
                    valueList.add("e");
                    writer.writeRow(valueList);
                    valueList.clear();
                }
            }
        }, new FileMakingOption().delimitateByComma().encodeAsUTF8().separateByLf().quoteMinimally());

        // ## Assert ##
        String actual = ous.toString();
        log(actual);
        assertFalse(actual.endsWith("\n"));
        String[] split = actual.split("\n");
        assertEquals("a,b,cc,d,e", split[0]);
        assertEquals("a,b,\"c\"\"c\",d,e", split[1]);
        assertEquals("a,\"b,b\",\"c\"\",c\",\"d", split[2]);
        assertEquals("\",e", split[3]);
    }

    public void test_make_invalidValueCount_basic() throws Exception {
        // ## Arrange ##
        FileToken impl = new FileToken();
        ByteArrayOutputStream ous = new ByteArrayOutputStream();

        // ## Act ##
        try {
            List<String> columnNameList = Arrays.asList("A", "B", "C", "D", "E");
            impl.make(ous, new FileMakingCallback() {
                public void write(FileMakingRowWriter writer) throws IOException, SQLException {
                    {
                        List<String> valueList = new ArrayList<String>();
                        valueList.add("a");
                        valueList.add("b");
                        valueList.add("cc");
                        valueList.add("d");
                        valueList.add("e");
                        writer.writeRow(valueList);
                    }
                    {
                        List<String> valueList = new ArrayList<String>();
                        valueList.add("a");
                        valueList.add("b");
                        valueList.add("c\"c");
                        valueList.add("d");
                        writer.writeRow(valueList);
                    }
                    {
                        List<String> valueList = new ArrayList<String>();
                        valueList.add("a");
                        valueList.add("b,b");
                        valueList.add("c\",c");
                        valueList.add("d\n");
                        valueList.add("e");
                        writer.writeRow(valueList);
                    }
                }
            },
                    new FileMakingOption().delimitateByComma().encodeAsUTF8().separateByLf().quoteMinimally()
                            .headerInfo(columnNameList));

            // ## Assert ##
            fail();
        } catch (FileMakingInvalidValueCountException e) {
            // OK
            log(e.getMessage());
        }
    }

    public void test_make_invalidValueCount_suppress() throws Exception {
        // ## Arrange ##
        FileToken impl = new FileToken();
        ByteArrayOutputStream ous = new ByteArrayOutputStream();

        // ## Act ##
        List<String> columnNameList = Arrays.asList("A", "B", "C", "D", "E");
        impl.make(ous, new FileMakingCallback() {
            public void write(FileMakingRowWriter writer) throws IOException, SQLException {
                {
                    List<String> valueList = new ArrayList<String>();
                    valueList.add("a");
                    valueList.add("b");
                    valueList.add("cc");
                    valueList.add("d");
                    valueList.add("e");
                    writer.writeRow(valueList);
                }
                {
                    List<String> valueList = new ArrayList<String>();
                    valueList.add("a");
                    valueList.add("b");
                    valueList.add("c\"c");
                    valueList.add("d");
                    writer.writeRow(valueList);
                }
                {
                    List<String> valueList = new ArrayList<String>();
                    valueList.add("a");
                    valueList.add("b,b");
                    valueList.add("c\",c");
                    valueList.add("d\n");
                    valueList.add("e");
                    writer.writeRow(valueList);
                }
            }
        }, new FileMakingOption().delimitateByComma().encodeAsUTF8().separateByLf().quoteMinimally()
                .suppressValueCountCheck().headerInfo(columnNameList));

        // ## Assert ##
        String actual = ous.toString();
        log(actual);
        assertFalse(actual.endsWith("\n"));
        String[] split = actual.split("\n");
        assertEquals("A,B,C,D,E", split[0]);
        assertEquals("a,b,cc,d,e", split[1]);
        assertEquals("a,b,\"c\"\"c\",d", split[2]);
        assertEquals("a,\"b,b\",\"c\"\",c\",\"d", split[3]);
        assertEquals("\",e", split[4]);
    }

    public void test_make_valueMap_basic() throws Exception {
        // ## Arrange ##
        FileToken impl = new FileToken();
        ByteArrayOutputStream ous = new ByteArrayOutputStream();

        // ## Act ##
        impl.make(ous, new FileMakingCallback() {
            public void write(FileMakingRowWriter writer) throws IOException, SQLException {
                {
                    Map<String, String> valueMap = new LinkedHashMap<String, String>();
                    valueMap.put("A", "a");
                    valueMap.put("B", "b");
                    valueMap.put("C", "cc");
                    valueMap.put("D", "d");
                    valueMap.put("E", "e");
                    writer.writeRow(valueMap);
                }
                {
                    Map<String, String> valueMap = new LinkedHashMap<String, String>();
                    valueMap.put("D", "d\"");
                    valueMap.put("B", "\"");
                    valueMap.put("A", "a");
                    valueMap.put("E", "e");
                    valueMap.put("C", "c\"c");
                    writer.writeRow(valueMap);
                }
                {
                    Map<String, String> valueMap = new HashMap<String, String>();
                    valueMap.put("D", "d\n");
                    valueMap.put("E", "e");
                    valueMap.put("A", "a");
                    valueMap.put("B", "b,b");
                    valueMap.put("C", "c\",c");
                    writer.writeRow(valueMap);
                }
            }
        }, new FileMakingOption().delimitateByComma().encodeAsUTF8().separateByLf());

        // ## Assert ##
        String actual = ous.toString();
        log(actual);
        assertFalse(actual.endsWith("\n"));
        String[] split = actual.split("\n");
        assertEquals("\"A\",\"B\",\"C\",\"D\",\"E\"", split[0]);
        assertEquals("\"a\",\"b\",\"cc\",\"d\",\"e\"", split[1]);
        assertEquals("\"a\",\"\"\"\",\"c\"\"c\",\"d\"\"\",\"e\"", split[2]);
        assertEquals("\"a\",\"b,b\",\"c\"\",c\",\"d", split[3]);
        assertEquals("\",\"e\"", split[4]);
    }

    // ===================================================================================
    //                                                                            Pinpoint
    //                                                                            ========
    public void test_isOddNumber() {
        // ## Arrange ##
        FileToken impl = new FileToken();

        // ## Act & Assert ##
        assertFalse(impl.isOddNumber(0));
        assertTrue(impl.isOddNumber(1));
        assertFalse(impl.isOddNumber(2));
        assertTrue(impl.isOddNumber(3));
        assertFalse(impl.isOddNumber(4));
        assertTrue(impl.isOddNumber(123));
        assertFalse(impl.isOddNumber(1234));
        assertTrue(impl.isOddNumber(-1));
        assertFalse(impl.isOddNumber(-2));
    }
}
