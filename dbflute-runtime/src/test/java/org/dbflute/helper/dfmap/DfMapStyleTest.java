/*
 * Copyright 2014-2020 the original author or authors.
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.dbflute.helper.dfmap.exception.DfMapDuplicateEntryException;
import org.dbflute.helper.dfmap.exception.DfMapParseFailureException;
import org.dbflute.unit.RuntimeTestCase;
import org.dbflute.util.DfTraceViewUtil;

/**
 * @author jflute
 * @since 1.1.8 (2018/05/05 Saturday)
 */
public class DfMapStyleTest extends RuntimeTestCase {

    // ===================================================================================
    //                                                                        to MapString
    //                                                                        ============
    // -----------------------------------------------------
    //                                                 Basic
    //                                                 -----
    public void test_toMapString_basic() {
        // ## Arrange ##
        final DfMapStyle mapStyle = new DfMapStyle();
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
        String mapString = mapStyle.toMapString(map);

        // ## Assert ##
        log(ln() + mapString);
        assertTrue(mapString.contains("; key1 = value1" + ln()));
        assertTrue(mapString.contains("; key2 = value2" + ln()));
        assertTrue(mapString.contains("; key3 = map:{" + ln()));
        assertTrue(mapString.contains("    ; key3-1 = value3-1" + ln()));
        Map<String, Object> generateMap = mapStyle.fromMapString(mapString);
        log(ln() + generateMap);
        assertEquals(map, generateMap);
    }

    // -----------------------------------------------------
    //                                                Escape
    //                                                ------
    public void test_toMapString_escape_basic() {
        // ## Arrange ##
        final DfMapStyle mapStyle = new DfMapStyle();
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
        String mapString = mapStyle.toMapString(map);

        // ## Assert ##
        log(ln() + mapString);
        assertTrue(mapString.contains("; k\\=ey1 = val\\{ue1" + ln()));
        assertTrue(mapString.contains("; ke\\;y2 = va\\=lu\\}e2" + ln()));
        assertTrue(mapString.contains("; key3 = map:{" + ln()));
        assertTrue(mapString.contains("; k\\}ey3-1 = va\\;lue3-1" + ln()));
        assertTrue(mapString.contains("; key@4-3-2 = val\\{ue4\\=-3-2" + ln()));
        Map<String, Object> generateMap = mapStyle.fromMapString(mapString);
        log(ln() + generateMap);
        assertEquals(map, generateMap);
    }

    public void test_toMapString_escape_withoutDisplaySideSpace() {
        // ## Arrange ##
        final DfMapStyle mapStyle = new DfMapStyle().withoutDisplaySideSpace();
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
        String mapString = mapStyle.toMapString(map);

        // ## Assert ##
        log(ln() + mapString);
        assertTrue(mapString.contains(";k\\=ey1=val\\{ue1" + ln()));
        assertTrue(mapString.contains(";ke\\;y2=va\\=lu\\}e2" + ln()));
        assertTrue(mapString.contains(";key3=map:{" + ln()));
        assertTrue(mapString.contains(";k\\}ey3-1=va\\;lue3-1" + ln()));
        assertTrue(mapString.contains(";key@4-3-2=val\\{ue4\\=-3-2" + ln()));
        Map<String, Object> generateMap = mapStyle.fromMapString(mapString);
        log(ln() + generateMap);
        assertEquals(map, generateMap);
    }

    // -----------------------------------------------------
    //                                        Print OneLiner
    //                                        --------------
    public void test_toMapString_printOneLiner_basic() {
        // ## Arrange ##
        final DfMapStyle mapStyle = new DfMapStyle().printOneLiner();
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
        String mapString = mapStyle.toMapString(map);

        // ## Assert ##
        log(ln() + mapString);
        assertTrue(mapString.contains(" key1 = value1"));
        assertTrue(mapString.contains("; key2 = value2"));
        assertTrue(mapString.contains("; key3 = map:{"));
        assertTrue(mapString.contains(" key3-1 = value3-1"));
        assertFalse(mapString.contains(ln()));
        Map<String, Object> generateMap = mapStyle.fromMapString(mapString);
        log(ln() + generateMap);
        assertEquals(map, generateMap);
    }

    public void test_toMapString_printOneLiner_withoutSideSpace() {
        // ## Arrange ##
        final DfMapStyle mapStyle = new DfMapStyle().printOneLiner().withoutDisplaySideSpace();
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
        String mapString = mapStyle.toMapString(map);

        // ## Assert ##
        log(ln() + mapString);
        assertTrue(mapString.contains("key1=value1"));
        assertTrue(mapString.contains(";key2=value2"));
        assertTrue(mapString.contains(";key3=map:{"));
        assertTrue(mapString.contains("key3-1=value3-1"));
        assertNotContains(mapString, ln());
        assertNotContains(mapString, " ");
        Map<String, Object> generateMap = mapStyle.fromMapString(mapString);
        log(ln() + generateMap);
        assertEquals(map, generateMap);
    }

    // -----------------------------------------------------
    //                                           Nested Bean
    //                                           -----------
    public void test_toMapString_nestedBean() {
        // ## Arrange ##
        final DfMapStyle mapStyle = new DfMapStyle() {
            @Override
            protected Map<String, Object> resolvePotentialMapOfBuildingMapString(Object value) {
                if (value instanceof MockNestedBean) {
                    return ((MockNestedBean) value).toMap();
                } else {
                    return super.resolvePotentialMapOfBuildingMapString(value);
                }
            }
        };
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("bean", new MockNestedBean(1, "Pixy", LocalDate.now(), true));

        // ## Act ##
        String mapString = mapStyle.toMapString(map);

        // ## Assert ##
        log(ln() + mapString);
        assertContains(mapString, "; memberName = Pixy");
        Map<String, Object> restoredMap = mapStyle.fromMapString(mapString);
        log(ln() + restoredMap);
        @SuppressWarnings("unchecked")
        Map<String, Object> beanMap = (Map<String, Object>) restoredMap.get("bean");
        assertNotNull(beanMap);
        assertEquals("Pixy", beanMap.get("memberName"));
    }

    public static class MockNestedBean {

        public final Integer memberId;
        public final String memberName;
        public final LocalDate birthdate;
        public final Boolean formalized;

        public MockNestedBean(Integer memberId, String memberName, LocalDate birthdate, Boolean formalized) {
            this.memberId = memberId;
            this.memberName = memberName;
            this.birthdate = birthdate;
            this.formalized = formalized;
        }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("memberId", memberId);
            map.put("memberName", memberName);
            map.put("birthdate", birthdate);
            map.put("formalized", formalized);
            return map;
        }
    }

    // -----------------------------------------------------
    //                                                 Empty
    //                                                 -----
    public void test_toMapString_empty_basic() {
        // ## Arrange ##
        final DfMapStyle mapStyle = new DfMapStyle();

        // ## Act ##
        String mapString = mapStyle.toMapString(new HashMap<>());

        // ## Assert ##
        log(mapString);
        assertEquals("map:{}", mapString);
    }

    // ===================================================================================
    //                                                                       to ListString
    //                                                                       =============
    public void test_toListString_basic() {
        // ## Arrange ##
        final DfMapStyle mapStyle = new DfMapStyle().printOneLiner();
        final List<String> list = newArrayList("sea", "land", "piari");

        // ## Act ##
        String listString = mapStyle.toListString(list);

        // ## Assert ##
        log(ln() + listString);
        assertEquals("list:{ sea ; land ; piari }", listString);
    }

    // ===================================================================================
    //                                                                      from MapString
    //                                                                      ==============
    public void test_fromMapString_contains_List() throws Exception {
        // ## Arrange ##
        final DfMapStyle mapStyle = new DfMapStyle();
        final String mapString = "map:{key1=value1;key2=list:{value2-1;value2-2;value2-3};key3=value3}";

        // ## Act ##
        final Map<String, Object> resultMap = mapStyle.fromMapString(mapString);

        // ## Assert ##
        showMap(resultMap);
        assertEquals("value1", resultMap.get("key1"));
        assertEquals(Arrays.asList(new String[] { "value2-1", "value2-2", "value2-3" }), resultMap.get("key2"));
        assertEquals("value3", resultMap.get("key3"));
    }

    public void test_fromMapString_contains_EmptyString_and_Null() throws Exception {
        // ## Arrange ##
        final DfMapStyle mapStyle = new DfMapStyle();
        final String mapString = "map:{key1=value1;key2=;key3=list:{null;value3-2;null;null};key4=null}";

        // ## Act ##
        final Map<String, Object> resultMap = mapStyle.fromMapString(mapString);

        // ## Assert ##
        showMap(resultMap);
        assertEquals(resultMap.get("key1"), "value1");
        assertEquals(resultMap.get("key2"), null);
        assertEquals(resultMap.get("key3"), Arrays.asList(new String[] { null, "value3-2", null, null }));
        assertEquals(resultMap.get("key4"), null);
    }

    public void test_fromMapString_contains_LineSeparator() throws Exception {
        // ## Arrange ##
        final DfMapStyle mapStyle = new DfMapStyle();
        final String mapString = "map:{key1=value1;key2=value2;key3=val\nue3;key4=value4}";

        // ## Act ##
        final Map<String, Object> generatedMap = mapStyle.fromMapString(mapString);

        // ## Assert ##
        showMap(generatedMap);
        assertEquals("value1", generatedMap.get("key1"));
        assertEquals("value2", generatedMap.get("key2"));
        assertEquals("val\nue3", generatedMap.get("key3"));
        assertEquals("value4", generatedMap.get("key4"));
    }

    public void test_fromMapString_contains_DoubleByte() throws Exception {
        // ## Arrange ##
        final DfMapStyle mapStyle = new DfMapStyle();
        final String mapString = "map:{key1=value1;key2=　値２;キー３=このあと改行\nした;key4=あと全角セミコロン；とかね　}";

        // ## Act ##
        final Map<String, Object> generatedMap = mapStyle.fromMapString(mapString);

        // ## Assert ##
        showMap(generatedMap);
    }

    // -----------------------------------------------------
    //                                                 Empty
    //                                                 -----
    public void test_fromMapString_empty_basic() {
        // ## Arrange ##
        final DfMapStyle mapStyle = new DfMapStyle();

        // ## Act ##
        // ## Assert ##
        assertTrue(mapStyle.fromMapString("map:{}").isEmpty());
        assertTrue(mapStyle.fromMapString("map:{} ").isEmpty());
        assertTrue(mapStyle.fromMapString("map:{}\n").isEmpty());
        assertTrue(mapStyle.fromMapString("map:{ }").isEmpty());
        assertTrue(mapStyle.fromMapString("map:{\n}").isEmpty());
        assertTrue(mapStyle.fromMapString(" map:{}").isEmpty());
        assertTrue(mapStyle.fromMapString("\nmap:{}").isEmpty());
        assertTrue(mapStyle.fromMapString("\nmap:{\n}\n").isEmpty());
    }

    // ===================================================================================
    //                                                                     from ListString
    //                                                                     ===============
    public void test_fromListString_basic() throws Exception {
        // ## Arrange ##
        final DfMapStyle mapStyle = new DfMapStyle();
        final String listString = "list:{sea;land;map:{piari=bonvo}}";

        // ## Act ##
        List<Object> resultList = mapStyle.fromListString(listString);

        // ## Assert ##
        log(ln() + resultList);
        assertEquals(3, resultList.size());
        assertEquals("sea", resultList.get(0));
        assertEquals("land", resultList.get(1));
        assertEquals(newHashMap("piari", "bonvo"), resultList.get(2));
    }

    // ===================================================================================
    //                                                                     Duplicate Entry
    //                                                                     ===============
    public void test_duplicate_fromMapString_basic() throws Exception {
        // ## Arrange ##
        final DfMapStyle mapStyle = new DfMapStyle();
        final String mapString = "map:{key1=value1;key2=value2;key1=value3}";

        // ## Act ##
        final Map<String, Object> resultMap = mapStyle.fromMapString(mapString);

        // ## Assert ##
        showMap(resultMap);
        assertEquals("value3", resultMap.get("key1"));
        assertEquals("value2", resultMap.get("key2"));
    }

    public void test_duplicate_fromMapString_nested() throws Exception {
        // ## Arrange ##
        final DfMapStyle mapStyle = new DfMapStyle();
        final String mapString = "map:{key1=value1;key1=map:{key1=value2;key2=value3;key2=value4};key3=value3}";

        // ## Act ##
        final Map<String, Object> resultMap = mapStyle.fromMapString(mapString);

        // ## Assert ##
        showMap(resultMap);
        assertEquals(newHashMap("key1", "value2", "key2", "value4"), resultMap.get("key1"));
        assertEquals("value3", resultMap.get("key3"));
    }

    public void test_duplicate_fromMapString_checked_basic() throws Exception {
        // ## Arrange ##
        final DfMapStyle mapStyle = new DfMapStyle().checkDuplicateEntry();
        final String mapString = "map:{key1=value1;key2=map:{key1=value2;key2=value3;key2=value4};key1=value3}";

        // ## Act ##
        try {
            mapStyle.fromMapString(mapString);
            // ## Assert ##
            fail();
        } catch (DfMapDuplicateEntryException e) {
            log(e.getMessage());
        }
    }

    public void test_duplicate_fromMapString_checked_list() throws Exception {
        // ## Arrange ##
        final DfMapStyle mapStyle = new DfMapStyle().checkDuplicateEntry();
        final String mapString = "map:{key1=value1;key1=list:{a;b;c};key3=value3}";

        // ## Act ##
        try {
            mapStyle.fromMapString(mapString);
            // ## Assert ##
            fail();
        } catch (DfMapDuplicateEntryException e) {
            log(e.getMessage());
        }
    }

    public void test_duplicate_fromMapString_checked_map() throws Exception {
        // ## Arrange ##
        final DfMapStyle mapStyle = new DfMapStyle().checkDuplicateEntry();
        final String mapString = "map:{key1=value1;key1=map:{key1=value2;key2=value3;key2=value4};key3=value3}";

        // ## Act ##
        try {
            mapStyle.fromMapString(mapString);
            // ## Assert ##
            fail();
        } catch (DfMapDuplicateEntryException e) {
            log(e.getMessage());
        }
    }

    public void test_duplicate_fromMapString_checked_nested() throws Exception {
        // ## Arrange ##
        final DfMapStyle mapStyle = new DfMapStyle().checkDuplicateEntry();
        final String mapString = "map:{key1=value1;key2=map:{key1=value2;key2=value3;key2=value4};key3=value3}";

        // ## Act ##
        try {
            mapStyle.fromMapString(mapString);
            // ## Assert ##
            fail();
        } catch (DfMapDuplicateEntryException e) {
            log(e.getMessage());
        }
    }

    // ===================================================================================
    //                                                                         Escape Mark
    //                                                                         ===========
    // -----------------------------------------------------
    //                                           Map String
    //                                           -----------
    public void test_escape_mapString_basic() throws Exception {
        // ## Arrange ##
        final DfMapStyle mapStyle = new DfMapStyle();
        final String mapString = "map:{ke\\{y1\"=v\\;\\}al\\}u\\=e\\\\}1\\ }"; // needs space

        // ## Act ##
        final Map<String, Object> resultMap = mapStyle.fromMapString(mapString);

        // ## Assert ##
        showMap(resultMap);
        assertEquals("v;}al}u=e\\}1\\", resultMap.get("ke{y1\"")); // escaping escape char is not supported
        String reversed = mapStyle.toMapString(resultMap);
        log(reversed);
        assertContains(reversed, "; ke\\{y1\" = v\\;\\}al\\}u\\=e\\\\}1\\");
    }

    public void test_escape_mapString_border() throws Exception {
        // ## Arrange ##
        final DfMapStyle mapStyle = new DfMapStyle();
        final String mapString = "map:{\\{key1\\;=\\\\{v\\;al\\}u\\=e\\}1\\;\\}}";

        // ## Act ##
        final Map<String, Object> resultMap = mapStyle.fromMapString(mapString);

        // ## Assert ##
        showMap(resultMap);
        assertEquals("\\{v;al}u=e}1;}", resultMap.get("{key1;"));
        String reversed = mapStyle.toMapString(resultMap);
        log(reversed);
        assertContains(reversed, "; \\{key1\\; = \\\\{v\\;al\\}u\\=e\\}1\\;\\}");
    }

    public void test_escape_mapString_two_equal() throws Exception {
        // ## Arrange ##
        final DfMapStyle mapStyle = new DfMapStyle();
        final String mapString = "map:{key1=value1=value2}";

        // ## Act ##
        final Map<String, Object> resultMap = mapStyle.fromMapString(mapString);

        // ## Assert ##
        showMap(resultMap);
        assertEquals("value1=value2", resultMap.get("key1")); // no need to escape here
        String reversed = mapStyle.toMapString(resultMap);
        log(reversed);
        assertContains(reversed, "; key1 = value1\\=value2");
    }

    // -----------------------------------------------------
    //                                           List String
    //                                           -----------
    public void test_escape_listString_basic() throws Exception {
        // ## Arrange ##
        final DfMapStyle mapStyle = new DfMapStyle();
        final String mapString = "list:{ke\\{y1\"=>v\\;\\}al\\}u\\=e\\\\}1\\ }"; // needs space

        // ## Act ##
        final List<Object> resultList = mapStyle.fromListString(mapString);

        // ## Assert ##
        showList(resultList);
        assertEquals("ke{y1\"=>v;}al}u=e\\}1\\", resultList.get(0)); // escaping escape char is not supported
        String reversed = mapStyle.toListString(resultList);
        log(reversed);
        assertContains(reversed, "ke\\{y1\"\\=>v\\;\\}al\\}u\\=e\\\\}1\\");
    }

    public void test_escape_listString_ignoreEqualAsEscape() throws Exception {
        // ## Arrange ##
        final DfMapStyle mapStyle = new DfMapStyle() {
            @Override
            protected boolean isIgnoreEqualAsEscapeControlMarkInList() {
                return true;
            }
        };
        final String mapString = "list:{ke\\{y1\"=>v\\;\\}al\\}u\\=e\\\\}1\\ }"; // needs space

        // ## Act ##
        final List<Object> resultList = mapStyle.fromListString(mapString);

        // ## Assert ##
        showList(resultList);
        assertEquals("ke{y1\"=>v;}al}u=e\\}1\\", resultList.get(0)); // escaping escape char is not supported
        String reversed = mapStyle.toListString(resultList);
        log(reversed);
        assertContains(reversed, "ke\\{y1\"=>v\\;\\}al\\}u=e\\\\}1\\");
    }

    public void test_escape_listString_ignoreEqualAsUnescape() throws Exception {
        // ## Arrange ##
        final DfMapStyle mapStyle = new DfMapStyle() {
            @Override
            protected boolean isIgnoreEqualAsUnescapeControlMarkInList() {
                return true;
            }
        };
        final String mapString = "list:{ke\\{y1\"=>v\\;\\}al\\}u\\=e\\\\}1\\ }"; // needs space

        // ## Act ##
        final List<Object> resultList = mapStyle.fromListString(mapString);

        // ## Assert ##
        showList(resultList);
        assertEquals("ke{y1\"=>v;}al}u\\=e\\}1\\", resultList.get(0)); // escaping escape char is not supported
        String reversed = mapStyle.toListString(resultList);
        log(reversed);
        assertContains(reversed, "ke\\{y1\"\\=>v\\;\\}al\\}u\\\\=e\\\\}1\\");
    }

    public void test_escape_listString_inMap_basic() throws Exception {
        // ## Arrange ##
        final DfMapStyle mapStyle = new DfMapStyle();
        final String mapString = "map:{ s\\=ea = list:{ke\\{y1\"=>v\\;\\}al\\}u\\=e\\\\}1\\ } }";

        // ## Act ##
        final Map<String, Object> resultMap = mapStyle.fromMapString(mapString);

        // ## Assert ##
        showMap(resultMap);
        @SuppressWarnings("unchecked")
        List<Object> elementList = (List<Object>) resultMap.get("s=ea");
        assertEquals("ke{y1\"=>v;}al}u=e\\}1\\", elementList.get(0)); // escaping escape char is not supported
        String reversed = mapStyle.toMapString(resultMap);
        log(reversed);
        assertContains(reversed, "ke\\{y1\"\\=>v\\;\\}al\\}u\\=e\\\\}1\\");
    }

    public void test_escape_listString_inMap_ignoreEqualAsUnescape() throws Exception {
        // ## Arrange ##
        final DfMapStyle mapStyle = new DfMapStyle() {
            @Override
            protected boolean isIgnoreEqualAsUnescapeControlMarkInList() {
                return true;
            }
        };
        final String mapString = "map:{ s\\=ea = list:{ke\\{y1\"=>v\\;\\}al\\}u\\=e\\\\}1\\ } ; land = one=man }";

        // ## Act ##
        final Map<String, Object> resultMap = mapStyle.fromMapString(mapString);

        // ## Assert ##
        showMap(resultMap);
        @SuppressWarnings("unchecked")
        List<Object> elementList = (List<Object>) resultMap.get("s=ea");
        assertEquals("ke{y1\"=>v;}al}u\\=e\\}1\\", elementList.get(0)); // escaping escape char is not supported
        assertEquals("one=man", resultMap.get("land"));
        String reversed = mapStyle.toMapString(resultMap);
        log(reversed);
        assertContains(reversed, "ke\\{y1\"\\=>v\\;\\}al\\}u\\\\=e\\\\}1\\");
        assertContains(reversed, "one\\=man");
    }

    // ===================================================================================
    //                                                               Various Specification
    //                                                               =====================
    // -----------------------------------------------------
    //                                                Quoted
    //                                                ------
    public void test_quoted_fromMapString_basic() throws Exception {
        // ## Arrange ##
        final DfMapStyle mapStyle = new DfMapStyle();
        final String mapString = "map:{key1=\"value1\"}";

        // ## Act ##
        final Map<String, Object> resultMap = mapStyle.fromMapString(mapString);

        // ## Assert ##
        showMap(resultMap);
        assertEquals("\"value1\"", resultMap.get("key1")); // keep quoted
    }

    // -----------------------------------------------------
    //                                               Trimmed
    //                                               -------
    public void test_trimmed_fromMapString_basic() throws Exception {
        // ## Arrange ##
        final DfMapStyle mapStyle = new DfMapStyle();
        final String mapString = "  map:{ key1    = value1  }      ";

        // ## Act ##
        final Map<String, Object> resultMap = mapStyle.fromMapString(mapString);

        // ## Assert ##
        showMap(resultMap);
        assertEquals("value1", resultMap.get("key1"));
    }

    // -----------------------------------------------------
    //                                              Surprise
    //                                              --------
    public void test_surprise_fromMapString_beginPrefixInValue() throws Exception {
        // ## Arrange ##
        final DfMapStyle mapStyle = new DfMapStyle();
        final String mapString = "map:{key1=value1 map:value2 list:{ }";

        // ## Act ##
        final Map<String, Object> resultMap = mapStyle.fromMapString(mapString);

        // ## Assert ##
        showMap(resultMap);
        assertEquals("value1 map:value2 list:{", resultMap.get("key1"));
    }

    // -----------------------------------------------------
    //                                               Illegal
    //                                               -------
    public void test_parse_fromMapString_failure() throws Exception {
        // ## Arrange ##
        final DfMapStyle mapStyle = new DfMapStyle();

        // ## Act ##
        // ## Assert ##
        try {
            mapStyle.fromMapString("map:{ foo = bar");
            fail();
        } catch (DfMapParseFailureException e) {
            log(e.getMessage());
        }
        // because quit brace-count check
        //try {
        //    maplist.fromMapString("map:{ foo = map:{ }");
        //    fail();
        //} catch (DfMapParseFailureException e) {
        //    log(e.getMessage());
        //}
        try {
            mapStyle.fromMapString("map:{ foo }");
            fail();
        } catch (DfMapParseFailureException e) {
            log(e.getMessage());
        }
    }

    // -----------------------------------------------------
    //                                           Performance
    //                                           -----------
    public void test_performance_fromMapString_check() throws Exception {
        // ## Arrange ##
        final DfMapStyle mapStyle = new DfMapStyle();
        String mapString;
        {
            // if i < 1000
            //  mapString.length(): 182127
            //  performance cost: 00m01s573ms, 00m01s487ms, 00m01s560ms, 00m01s705ms, 00m01s528ms
            //  after tuning: 00m00s046ms, 00m00s036ms, 00m00s038ms
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            for (int i = 0; i < 1000; i++) {
                String baseKey = "key" + i;
                String baseValue = "value" + i;
                Map<String, Object> valueMap = new LinkedHashMap<String, Object>();
                valueMap.put(baseKey + "-1", baseValue + "-1");
                valueMap.put(baseKey + "-2", baseValue + "-2");
                {
                    List<Object> valueList = new ArrayList<Object>();
                    valueList.add(baseValue + "-3-1");
                    valueList.add(baseValue + "-3-2");
                    valueMap.put(baseKey + "-3", valueList);
                }
                map.put(baseKey, valueMap);
            }
            mapString = mapStyle.toMapString(map);
        }
        log("mapString.length(): {}", mapString.length());

        // ## Act ##
        long before = System.currentTimeMillis();
        final Map<String, Object> resultMap = mapStyle.fromMapString(mapString);
        long after = System.currentTimeMillis();

        // ## Assert ##
        assertHasAnyElement(resultMap.keySet());
        log("performance cost: {}", DfTraceViewUtil.convertToPerformanceView(after - before));
    }

    // ===================================================================================
    //                                                                        Small Helper
    //                                                                        ============
    protected void showMap(Map<String, Object> generatedMap) {
        final String targetString = generatedMap.toString();
        final StringBuilder sb = new StringBuilder();
        sb.append(ln()).append(targetString);
        log(sb);
    }
}
