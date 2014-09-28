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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.seasar.dbflute.exception.MapListStringDuplicateEntryException;
import org.seasar.dbflute.exception.MapListStringParseFailureException;
import org.seasar.dbflute.unit.core.PlainTestCase;

/**
 * @author jflute
 * @since 0.6.0 (2008/01/17 Thursday)
 */
public class MapListStringTest extends PlainTestCase {

    // ===================================================================================
    //                                                                               Build
    //                                                                               =====
    public void test_buildMapString_basic() {
        // ## Arrange ##
        final MapListString maplist = new MapListString();
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        {
            Map<String, Object> valueMap = new LinkedHashMap<String, Object>();
            valueMap.put("key3-1", "value3-1");
            valueMap.put("key3-2", "value3-2");
            List<Object> valueList = new ArrayList<Object>();
            valueList.add("value3-3-1");
            valueList.add("value3-3-2");
            valueMap.put("key3-3", valueList);
            map.put("key3", valueMap);
        }
        {
            List<Object> valueList = new ArrayList<Object>();
            valueList.add("value4-1");
            valueList.add("value4-2");
            Map<String, Object> valueMap = new LinkedHashMap<String, Object>();
            valueMap.put("key4-3-1", "value4-3-1");
            valueMap.put("key4-3-2", "value4-3-2");
            valueList.add(valueMap);
            map.put("key4", valueList);
        }

        // ## Act ##
        String actual = maplist.buildMapString(map);

        // ## Assert ##
        log(ln() + actual);
        assertTrue(actual.contains("; key1 = value1" + ln()));
        assertTrue(actual.contains("; key2 = value2" + ln()));
        assertTrue(actual.contains("; key3 = map:{" + ln()));
        assertTrue(actual.contains("    ; key3-1 = value3-1" + ln()));
        Map<String, Object> generateMap = maplist.generateMap(actual);
        log(ln() + generateMap);
        assertEquals(map, generateMap);
    }

    public void test_buildMapString_escape() {
        // ## Arrange ##
        final MapListString maplist = new MapListString();
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("k=ey1", "val{ue1");
        map.put("ke;y2", "va=lu}e2");
        {
            Map<String, Object> valueMap = new LinkedHashMap<String, Object>();
            valueMap.put("k}ey3-1", "va;lue3-1");
            valueMap.put("key3-2", "value3-2");
            List<Object> valueList = new ArrayList<Object>();
            valueList.add("value3-3-1");
            valueList.add("value3-3-2");
            valueMap.put("key3-3", valueList);
            map.put("key3", valueMap);
        }
        {
            List<Object> valueList = new ArrayList<Object>();
            valueList.add("value4-1");
            valueList.add("value4-2");
            Map<String, Object> valueMap = new LinkedHashMap<String, Object>();
            valueMap.put("key=4-3-1", "value4-3-1");
            valueMap.put("key@4-3-2", "val{ue4=-3-2");
            valueList.add(valueMap);
            map.put("key4", valueList);
        }

        // ## Act ##
        String actual = maplist.buildMapString(map);

        // ## Assert ##
        log(ln() + actual);
        assertTrue(actual.contains("; k\\=ey1 = val\\{ue1" + ln()));
        assertTrue(actual.contains("; ke\\;y2 = va\\=lu\\}e2" + ln()));
        assertTrue(actual.contains("; key3 = map:{" + ln()));
        assertTrue(actual.contains("; k\\}ey3-1 = va\\;lue3-1" + ln()));
        assertTrue(actual.contains("; key@4-3-2 = val\\{ue4\\=-3-2" + ln()));
        Map<String, Object> generateMap = maplist.generateMap(actual);
        log(ln() + generateMap);
        assertEquals(map, generateMap);
    }

    // ===================================================================================
    //                                                                            Generate
    //                                                                            ========
    public void test_generateMap_contains_List() throws Exception {
        // ## Arrange ##
        final MapListString maplist = new MapListString();
        final String mapString = "map:{key1=value1;key2=list:{value2-1;value2-2;value2-3};key3=value3}";

        // ## Act ##
        final Map<String, Object> resultMap = maplist.generateMap(mapString);

        // ## Assert ##
        showGeneratedMap(resultMap);
        assertEquals("value1", resultMap.get("key1"));
        assertEquals(Arrays.asList(new String[] { "value2-1", "value2-2", "value2-3" }), resultMap.get("key2"));
        assertEquals("value3", resultMap.get("key3"));
    }

    public void test_generateMap_contains_EmptyString_and_Null() throws Exception {
        // ## Arrange ##
        final MapListString maplist = new MapListString();
        final String mapString = "map:{key1=value1;key2=;key3=list:{null;value3-2;null;null};key4=null}";

        // ## Act ##
        final Map<String, Object> resultMap = maplist.generateMap(mapString);

        // ## Assert ##
        showGeneratedMap(resultMap);
        assertEquals(resultMap.get("key1"), "value1");
        assertEquals(resultMap.get("key2"), null);
        assertEquals(resultMap.get("key3"), Arrays.asList(new String[] { null, "value3-2", null, null }));
        assertEquals(resultMap.get("key4"), null);
    }

    public void test_generateMap_contains_LineSeparator() throws Exception {
        // ## Arrange ##
        final MapListString maplist = new MapListString();
        final String mapString = "map:{key1=value1;key2=value2;key3=val\nue3;key4=value4}";

        // ## Act ##
        final Map<String, Object> generatedMap = maplist.generateMap(mapString);

        // ## Assert ##
        showGeneratedMap(generatedMap);
        assertEquals("value1", generatedMap.get("key1"));
        assertEquals("value2", generatedMap.get("key2"));
        assertEquals("val\nue3", generatedMap.get("key3"));
        assertEquals("value4", generatedMap.get("key4"));
    }

    public void test_generateMap_contains_DoubleByte() throws Exception {
        // ## Arrange ##
        final MapListString maplist = new MapListString();
        final String mapString = "map:{key1=value1;key2=値２;キー３=このあと改行\nした;key4=あと全角セミコロン；とかね}";

        // ## Act ##
        final Map<String, Object> generatedMap = maplist.generateMap(mapString);

        // ## Assert ##
        showGeneratedMap(generatedMap);
    }

    // -----------------------------------------------------
    //                                             Duplicate
    //                                             ---------
    public void test_generateMap_duplicate_basic() throws Exception {
        // ## Arrange ##
        final MapListString maplist = new MapListString();
        final String mapString = "map:{key1=value1;key2=value2;key1=value3}";

        // ## Act ##
        final Map<String, Object> resultMap = maplist.generateMap(mapString);

        // ## Assert ##
        showGeneratedMap(resultMap);
        assertEquals("value3", resultMap.get("key1"));
        assertEquals("value2", resultMap.get("key2"));
    }

    public void test_generateMap_duplicate_nested() throws Exception {
        // ## Arrange ##
        final MapListString maplist = new MapListString();
        final String mapString = "map:{key1=value1;key1=map:{key1=value2;key2=value3;key2=value4};key3=value3}";

        // ## Act ##
        final Map<String, Object> resultMap = maplist.generateMap(mapString);

        // ## Assert ##
        showGeneratedMap(resultMap);
        assertEquals(newHashMap("key1", "value2", "key2", "value4"), resultMap.get("key1"));
        assertEquals("value3", resultMap.get("key3"));
    }

    public void test_generateMap_duplicate_checked_basic() throws Exception {
        // ## Arrange ##
        final MapListString maplist = new MapListString().checkDuplicateEntry();
        final String mapString = "map:{key1=value1;key2=map:{key1=value2;key2=value3;key2=value4};key1=value3}";

        // ## Act ##
        try {
            maplist.generateMap(mapString);
            // ## Assert ##
            fail();
        } catch (MapListStringDuplicateEntryException e) {
            log(e.getMessage());
        }
    }

    public void test_generateMap_duplicate_checked_list() throws Exception {
        // ## Arrange ##
        final MapListString maplist = new MapListString().checkDuplicateEntry();
        final String mapString = "map:{key1=value1;key1=list:{a;b;c};key3=value3}";

        // ## Act ##
        try {
            maplist.generateMap(mapString);
            // ## Assert ##
            fail();
        } catch (MapListStringDuplicateEntryException e) {
            log(e.getMessage());
        }
    }

    public void test_generateMap_duplicate_checked_map() throws Exception {
        // ## Arrange ##
        final MapListString maplist = new MapListString().checkDuplicateEntry();
        final String mapString = "map:{key1=value1;key1=map:{key1=value2;key2=value3;key2=value4};key3=value3}";

        // ## Act ##
        try {
            maplist.generateMap(mapString);
            // ## Assert ##
            fail();
        } catch (MapListStringDuplicateEntryException e) {
            log(e.getMessage());
        }
    }

    public void test_generateMap_duplicate_checked_nested() throws Exception {
        // ## Arrange ##
        final MapListString maplist = new MapListString().checkDuplicateEntry();
        final String mapString = "map:{key1=value1;key2=map:{key1=value2;key2=value3;key2=value4};key3=value3}";

        // ## Act ##
        try {
            maplist.generateMap(mapString);
            // ## Assert ##
            fail();
        } catch (MapListStringDuplicateEntryException e) {
            log(e.getMessage());
        }
    }

    // -----------------------------------------------------
    //                                                Escape
    //                                                ------
    public void test_generateMap_escape_basic() throws Exception {
        // ## Arrange ##
        final MapListString maplist = new MapListString();
        final String mapString = "map:{ke\\{y1\"=v\\;\\}al\\}u\\=e\\\\}1\\ }"; // needs space

        // ## Act ##
        final Map<String, Object> resultMap = maplist.generateMap(mapString);

        // ## Assert ##
        showGeneratedMap(resultMap);
        assertEquals("v;}al}u=e\\}1\\", resultMap.get("ke{y1\""));
    }

    public void test_generateMap_escape_border() throws Exception {
        // ## Arrange ##
        final MapListString maplist = new MapListString();
        final String mapString = "map:{\\{key1\\;=\\\\{v\\;al\\}u\\=e\\}1\\;\\}}";

        // ## Act ##
        final Map<String, Object> resultMap = maplist.generateMap(mapString);

        // ## Assert ##
        showGeneratedMap(resultMap);
        assertEquals("\\{v;al}u=e}1;}", resultMap.get("{key1;"));
    }

    // -----------------------------------------------------
    //                                                Quoted
    //                                                ------
    public void test_generateMap_quoted_basic() throws Exception {
        // ## Arrange ##
        final MapListString maplist = new MapListString();
        final String mapString = "map:{key1=\"value1\"}";

        // ## Act ##
        final Map<String, Object> resultMap = maplist.generateMap(mapString);

        // ## Assert ##
        showGeneratedMap(resultMap);
        assertEquals("\"value1\"", resultMap.get("key1")); // keep quoted
    }

    // -----------------------------------------------------
    //                                              Surprise
    //                                              --------
    public void test_generateMap_surprise_two_equal() throws Exception {
        // ## Arrange ##
        final MapListString maplist = new MapListString();
        final String mapString = "map:{key1=value1=value2}";

        // ## Act ##
        final Map<String, Object> resultMap = maplist.generateMap(mapString);

        // ## Assert ##
        showGeneratedMap(resultMap);
        assertEquals("value1=value2", resultMap.get("key1"));
    }

    // -----------------------------------------------------
    //                                         Assist Helper
    //                                         -------------
    protected void showGeneratedMap(Map<String, Object> generatedMap) {
        final String targetString = generatedMap.toString();
        final StringBuilder sb = new StringBuilder();
        sb.append(ln()).append(targetString);
        log(sb);
    }

    // -----------------------------------------------------
    //                                               Illegal
    //                                               -------
    public void test_generateMap_parse_failure() throws Exception {
        // ## Arrange ##
        final MapListString maplist = new MapListString();

        // ## Act ##
        // ## Assert ##
        try {
            maplist.generateMap("map:{ foo = bar");
            fail();
        } catch (MapListStringParseFailureException e) {
            log(e.getMessage());
        }
        try {
            maplist.generateMap("map:{ foo = map:{ }");
            fail();
        } catch (MapListStringParseFailureException e) {
            log(e.getMessage());
        }
        try {
            maplist.generateMap("map:{ foo }");
            fail();
        } catch (MapListStringParseFailureException e) {
            log(e.getMessage());
        }
    }
}
