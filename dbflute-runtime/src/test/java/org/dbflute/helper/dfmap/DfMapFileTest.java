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
package org.dbflute.helper.dfmap;

import java.io.ByteArrayInputStream;
import java.util.Map;

import org.dbflute.unit.RuntimeTestCase;

/**
 * @author jflute
 */
public class DfMapFileTest extends RuntimeTestCase {

    // ===================================================================================
    //                                                                            Read Map
    //                                                                            ========
    public void test_readMap_plain_basic() throws Exception {
        // ## Arrange ##
        DfMapFile file = new DfMapFile();
        String text = "map:{sea=mystic ; land=oneman;piari=plaza}";
        ByteArrayInputStream ins = new ByteArrayInputStream(text.getBytes("UTF-8"));

        // ## Act ##
        Map<String, Object> map = file.readMap(ins);

        // ## Assert ##
        log(map);
        assertEquals("mystic", map.get("sea"));
        assertEquals("oneman", map.get("land"));
        assertEquals("plaza", map.get("piari"));
    }

    public void test_readMap_plain_empty() throws Exception {
        // ## Arrange ##
        DfMapFile file = new DfMapFile();
        String text = "";
        ByteArrayInputStream ins = new ByteArrayInputStream(text.getBytes("UTF-8"));

        // ## Act ##
        Map<String, Object> map = file.readMap(ins);

        // ## Assert ##
        log(map);
        assertTrue(map.isEmpty());
    }

    public void test_readMap_typed_basic() throws Exception {
        // ## Arrange ##
        DfMapFile file = new DfMapFile();
        String text = "map:{sea=mystic ; land=oneman;piari=plaza ; dstore=}";
        ByteArrayInputStream ins = new ByteArrayInputStream(text.getBytes("UTF-8"));

        // ## Act ##
        Map<String, String> map = file.readMap(ins, String.class);

        // ## Assert ##
        log(map);
        assertEquals("mystic", map.get("sea"));
        assertEquals("oneman", map.get("land"));
        assertEquals("plaza", map.get("piari"));
        assertEquals(null, map.get("dstore"));
    }

    public void test_readMap_typed_empty() throws Exception {
        // ## Arrange ##
        DfMapFile file = new DfMapFile();
        String text = "";
        ByteArrayInputStream ins = new ByteArrayInputStream(text.getBytes("UTF-8"));

        // ## Act ##
        Map<String, String> map = file.readMap(ins, String.class);

        // ## Assert ##
        log(map);
        assertTrue(map.isEmpty());
    }

    // ===================================================================================
    //                                                                         Read String
    //                                                                         ===========
    public void test_readString_lineComment_removed() throws Exception {
        // ## Arrange ##
        DfMapFile file = new DfMapFile();
        String text = "foo, \n#bar, \nbaz, qux";
        ByteArrayInputStream ins = new ByteArrayInputStream(text.getBytes("UTF-8"));

        // ## Act ##
        String actual = file.readString(ins);

        // ## Assert ##
        log(actual);
        assertEquals("foo, \nbaz, qux", actual);
    }

    public void test_readString_lineComment_last_removed_add_ln() throws Exception {
        // ## Arrange ##
        DfMapFile file = new DfMapFile();
        String text = "foo, \n#bar, \nbaz, qux\n#abc";
        ByteArrayInputStream ins = new ByteArrayInputStream(text.getBytes("UTF-8"));

        // ## Act ##
        String actual = file.readString(ins);

        // ## Assert ##
        log(actual);
        assertEquals("foo, \nbaz, qux\n", actual);
    }

    public void test_readString_BOM_removed() throws Exception {
        // ## Arrange ##
        DfMapFile file = new DfMapFile();
        String text = '\uFEFF' + "foo, bar, baz, qux";
        ByteArrayInputStream ins = new ByteArrayInputStream(text.getBytes("UTF-8"));

        // ## Act ##
        String actual = file.readString(ins);

        // ## Assert ##
        log(actual);
        assertEquals("foo, bar, baz, qux", actual);
    }

    public void test_readString_BOM_notRemoved_ifNotInitial() throws Exception {
        // ## Arrange ##
        DfMapFile file = new DfMapFile();
        String text = "abc" + '\uFEFF' + "foo, bar, baz, qux";
        ByteArrayInputStream ins = new ByteArrayInputStream(text.getBytes("UTF-8"));

        // ## Act ##
        String actual = file.readString(ins);

        // ## Assert ##
        log(actual);
        assertEquals("abc\uFEFFfoo, bar, baz, qux", actual);
    }
}
