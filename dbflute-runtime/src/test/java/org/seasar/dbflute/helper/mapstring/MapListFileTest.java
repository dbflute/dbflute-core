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
package org.seasar.dbflute.helper.mapstring;

import java.io.ByteArrayInputStream;

import org.seasar.dbflute.unit.core.PlainTestCase;

/**
 * @author jflute
 */
public class MapListFileTest extends PlainTestCase {

    public void test_readString_lineComment_removed() throws Exception {
        // ## Arrange ##
        MapListFile file = new MapListFile();
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
        MapListFile file = new MapListFile();
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
        MapListFile file = new MapListFile();
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
        MapListFile file = new MapListFile();
        String text = "abc" + '\uFEFF' + "foo, bar, baz, qux";
        ByteArrayInputStream ins = new ByteArrayInputStream(text.getBytes("UTF-8"));

        // ## Act ##
        String actual = file.readString(ins);

        // ## Assert ##
        log(actual);
        assertEquals("abc\uFEFFfoo, bar, baz, qux", actual);
    }
}
