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
package org.seasar.dbflute.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.seasar.dbflute.unit.core.PlainTestCase;
import org.seasar.dbflute.util.DfCollectionUtil.AccordingToOrderIdExtractor;
import org.seasar.dbflute.util.DfCollectionUtil.AccordingToOrderResource;
import org.seasar.dbflute.util.DfCollectionUtil.OrderDiff;
import org.seasar.dbflute.util.DfCollectionUtil.OrderDiffDetail;

/**
 * @author jflute
 * @since 0.9.4 (2009/03/20 Friday)
 */
public class DfCollectionUtilTest extends PlainTestCase {

    // ===================================================================================
    //                                                                                List
    //                                                                                ====
    public void test_newArrayList_dynamicArg() {
        // ## Arrange & Act ##
        List<String> list = DfCollectionUtil.newArrayList("foo", "bar");

        // ## Assert ##
        assertEquals(2, list.size());
        assertEquals("foo", list.get(0));
        assertEquals("bar", list.get(1));
    }

    public void test_newArrayList_collection() {
        // ## Arrange ##
        String[] array = new String[] { "foo", "bar" };

        // ## Act ##
        List<String> list = DfCollectionUtil.newArrayList(array);

        // ## Assert ##
        assertEquals(2, list.size());
        assertEquals("foo", list.get(0));
        assertEquals("bar", list.get(1));
    }

    public void test_newArrayList_stringArray() {
        // ## Arrange ##
        List<String> res = DfCollectionUtil.newArrayList("foo", "bar");

        // ## Act ##
        List<String> list = DfCollectionUtil.newArrayList(res);

        // ## Assert ##
        assertEquals(2, list.size());
        assertEquals("foo", list.get(0));
        assertEquals("bar", list.get(1));
    }

    public void test_splitByLimit_basic() {
        // ## Arrange ##
        List<String> value = new ArrayList<String>();
        value.add("1");
        value.add("2");
        value.add("3");
        value.add("4");
        value.add("5");
        value.add("6");
        value.add("7");

        // ## Act ##
        List<List<String>> actual = DfCollectionUtil.splitByLimit(value, 3);

        // ## Assert ##
        log(actual);
        assertEquals(3, actual.size());
        assertEquals(3, actual.get(0).size());
        assertEquals("1", actual.get(0).get(0));
        assertEquals("2", actual.get(0).get(1));
        assertEquals("3", actual.get(0).get(2));
        assertEquals(3, actual.get(1).size());
        assertEquals("4", actual.get(1).get(0));
        assertEquals("5", actual.get(1).get(1));
        assertEquals("6", actual.get(1).get(2));
        assertEquals(1, actual.get(2).size());
        assertEquals("7", actual.get(2).get(0));
    }

    public void test_splitByLimit_just() {
        // ## Arrange ##
        List<String> value = new ArrayList<String>();
        value.add("1");
        value.add("2");
        value.add("3");
        value.add("4");

        // ## Act ##
        List<List<String>> actual = DfCollectionUtil.splitByLimit(value, 4);

        // ## Assert ##
        log(actual);
        assertEquals(1, actual.size());
        assertEquals("1", actual.get(0).get(0));
        assertEquals("2", actual.get(0).get(1));
        assertEquals("3", actual.get(0).get(2));
        assertEquals("4", actual.get(0).get(3));
    }

    public void test_splitByLimit_justPlus() {
        // ## Arrange ##
        List<String> value = new ArrayList<String>();
        value.add("1");
        value.add("2");
        value.add("3");
        value.add("4");
        value.add("5");

        // ## Act ##
        List<List<String>> actual = DfCollectionUtil.splitByLimit(value, 4);

        // ## Assert ##
        log(actual);
        assertEquals(2, actual.size());
        assertEquals(4, actual.get(0).size());
        assertEquals("1", actual.get(0).get(0));
        assertEquals("2", actual.get(0).get(1));
        assertEquals("3", actual.get(0).get(2));
        assertEquals("4", actual.get(0).get(3));
        assertEquals(1, actual.get(1).size());
        assertEquals("5", actual.get(1).get(0));
    }

    public void test_splitByLimit_secondJust() {
        // ## Arrange ##
        List<String> value = new ArrayList<String>();
        value.add("1");
        value.add("2");
        value.add("3");
        value.add("4");
        value.add("5");
        value.add("6");
        value.add("7");
        value.add("8");

        // ## Act ##
        List<List<String>> actual = DfCollectionUtil.splitByLimit(value, 4);

        // ## Assert ##
        log(actual);
        assertEquals(2, actual.size());
        assertEquals(4, actual.get(0).size());
        assertEquals("1", actual.get(0).get(0));
        assertEquals("2", actual.get(0).get(1));
        assertEquals("3", actual.get(0).get(2));
        assertEquals("4", actual.get(0).get(3));
        assertEquals(4, actual.get(1).size());
        assertEquals("5", actual.get(1).get(0));
        assertEquals("6", actual.get(1).get(1));
        assertEquals("7", actual.get(1).get(2));
        assertEquals("8", actual.get(1).get(3));
    }

    public void test_splitByLimit_thirdJust() {
        // ## Arrange ##
        List<String> value = new ArrayList<String>();
        value.add("1");
        value.add("2");
        value.add("3");
        value.add("4");
        value.add("5");
        value.add("6");
        value.add("7");
        value.add("8");
        value.add("9");
        value.add("10");
        value.add("11");
        value.add("12");

        // ## Act ##
        List<List<String>> actual = DfCollectionUtil.splitByLimit(value, 4);

        // ## Assert ##
        assertEquals(3, actual.size());
        assertEquals(4, actual.get(0).size());
        assertEquals("1", actual.get(0).get(0));
        assertEquals("2", actual.get(0).get(1));
        assertEquals("3", actual.get(0).get(2));
        assertEquals("4", actual.get(0).get(3));
        assertEquals(4, actual.get(1).size());
        assertEquals("5", actual.get(1).get(0));
        assertEquals("6", actual.get(1).get(1));
        assertEquals("7", actual.get(1).get(2));
        assertEquals("8", actual.get(1).get(3));
        assertEquals(4, actual.get(2).size());
        assertEquals("9", actual.get(2).get(0));
        assertEquals("10", actual.get(2).get(1));
        assertEquals("11", actual.get(2).get(2));
        assertEquals("12", actual.get(2).get(3));
    }

    // -----------------------------------------------------
    //                                               Advance
    //                                               -------
    public void test_analyzeOrderChange_basic() throws Exception {
        // ## Arrange ##
        List<String> beforeUniqueList = DfCollectionUtil.newArrayList("foo", "bar", "baz", "qux", "quux");
        List<String> afterUniqueList = DfCollectionUtil.newArrayList("foo", "qux", "baz", "bar", "quux");

        // ## Act ##
        OrderDiff<String> orderDiff = DfCollectionUtil.analyzeOrderDiff(beforeUniqueList, afterUniqueList);

        // ## Assert ##
        final Map<String, OrderDiffDetail<String>> movedMap = orderDiff.getMovedMap();
        for (Entry<String, OrderDiffDetail<String>> entry : movedMap.entrySet()) {
            String element = entry.getKey();
            String previous = entry.getValue().getPreviousElement();
            log(element + " after " + previous);
        }
        assertEquals("baz", movedMap.get("bar").getPreviousElement());
        assertEquals("qux", movedMap.get("baz").getPreviousElement());
        assertEquals(2, movedMap.size());
    }

    public void test_analyzeOrderChange_reverse() throws Exception {
        // ## Arrange ##
        List<String> beforeUniqueList = DfCollectionUtil.newArrayList("foo", "qux", "baz", "bar", "quux");
        List<String> afterUniqueList = DfCollectionUtil.newArrayList("foo", "bar", "baz", "qux", "quux");

        // ## Act ##
        OrderDiff<String> orderDiff = DfCollectionUtil.analyzeOrderDiff(beforeUniqueList, afterUniqueList);

        // ## Assert ##
        final Map<String, OrderDiffDetail<String>> movedMap = orderDiff.getMovedMap();
        for (Entry<String, OrderDiffDetail<String>> entry : movedMap.entrySet()) {
            String element = entry.getKey();
            String previous = entry.getValue().getPreviousElement();
            log(element + " after " + previous);
        }
        assertEquals("baz", movedMap.get("qux").getPreviousElement());
        assertEquals("bar", movedMap.get("baz").getPreviousElement());
        assertEquals(2, movedMap.size());
    }

    public void test_analyzeOrderChange_guchagucha() throws Exception {
        // ## Arrange ##
        List<String> beforeUniqueList = DfCollectionUtil.newArrayList("foo", "bar", "baz", "qux", "quux");
        List<String> afterUniqueList = DfCollectionUtil.newArrayList("bar", "qux", "quux", "foo", "baz");

        // ## Act ##
        OrderDiff<String> orderDiff = DfCollectionUtil.analyzeOrderDiff(beforeUniqueList, afterUniqueList);

        // ## Assert ##
        final Map<String, OrderDiffDetail<String>> movedMap = orderDiff.getMovedMap();
        for (Entry<String, OrderDiffDetail<String>> entry : movedMap.entrySet()) {
            String element = entry.getKey();
            String previous = entry.getValue().getPreviousElement();
            log(element + " after " + previous);
        }
        assertEquals("quux", movedMap.get("foo").getPreviousElement());
        assertEquals("foo", movedMap.get("baz").getPreviousElement());
        assertEquals(2, movedMap.size());
    }

    public void test_analyzeOrderChange_various() throws Exception {
        // ## Arrange ##
        List<String> beforeUniqueList = DfCollectionUtil.newArrayList("foo", "bar", "baz", "qux", "deleted", "quux");
        List<String> afterUniqueList = DfCollectionUtil.newArrayList("qux", "corge", "bar", "grault", "quux", "foo",
                "baz", "garply", "waldo", "fred");

        // ## Act ##
        OrderDiff<String> orderDiff = DfCollectionUtil.analyzeOrderDiff(beforeUniqueList, afterUniqueList);

        // ## Assert ##
        final Map<String, OrderDiffDetail<String>> movedMap = orderDiff.getMovedMap();
        for (Entry<String, OrderDiffDetail<String>> entry : movedMap.entrySet()) {
            String element = entry.getKey();
            String previous = entry.getValue().getPreviousElement();
            log(element + " after " + previous);
        }
        assertEquals("quux", movedMap.get("foo").getPreviousElement());
        assertEquals("corge", movedMap.get("bar").getPreviousElement());
        assertEquals("foo", movedMap.get("baz").getPreviousElement());
        assertEquals(3, movedMap.size());
    }

    public void test_moveElementToIndex_basic() throws Exception {
        // ## Arrange ##
        List<String> list = DfCollectionUtil.newArrayList("foo", "bar", "baz", "qux", "quux");

        // ## Act ##
        List<String> movedList = DfCollectionUtil.moveElementToIndex(list, 1, 3);

        // ## Assert ##
        log("movedList: " + movedList);
        assertEquals("foo", movedList.get(0));
        assertEquals("baz", movedList.get(1));
        assertEquals("qux", movedList.get(2));
        assertEquals("bar", movedList.get(3));
        assertEquals("quux", movedList.get(4));
        assertEquals(list.size(), movedList.size());
    }

    public void test_moveElementToIndex_first() throws Exception {
        // ## Arrange ##
        List<String> list = DfCollectionUtil.newArrayList("foo", "bar", "baz", "qux", "quux");

        // ## Act ##
        List<String> movedList = DfCollectionUtil.moveElementToIndex(list, 0, 3);

        // ## Assert ##
        log("movedList: " + movedList);
        assertEquals("bar", movedList.get(0));
        assertEquals("baz", movedList.get(1));
        assertEquals("qux", movedList.get(2));
        assertEquals("foo", movedList.get(3));
        assertEquals("quux", movedList.get(4));
        assertEquals(list.size(), movedList.size());
    }

    public void test_moveElementToIndex_last() throws Exception {
        // ## Arrange ##
        List<String> list = DfCollectionUtil.newArrayList("foo", "bar", "baz", "qux", "quux");

        // ## Act ##
        List<String> movedList = DfCollectionUtil.moveElementToIndex(list, 1, 4);

        // ## Assert ##
        log("movedList: " + movedList);
        assertEquals("foo", movedList.get(0));
        assertEquals("baz", movedList.get(1));
        assertEquals("qux", movedList.get(2));
        assertEquals("quux", movedList.get(3));
        assertEquals("bar", movedList.get(4));
        assertEquals(list.size(), movedList.size());
    }

    public void test_moveElementToIndex_reverse() throws Exception {
        // ## Arrange ##
        List<String> list = DfCollectionUtil.newArrayList("foo", "bar", "baz", "qux", "quux");

        // ## Act ##
        List<String> movedList = DfCollectionUtil.moveElementToIndex(list, 3, 1);

        // ## Assert ##
        log("movedList: " + movedList);
        assertEquals("foo", movedList.get(0));
        assertEquals("qux", movedList.get(1));
        assertEquals("bar", movedList.get(2));
        assertEquals("baz", movedList.get(3));
        assertEquals("quux", movedList.get(4));
        assertEquals(list.size(), movedList.size());
    }

    // ===================================================================================
    //                                                                                 Map
    //                                                                                 ===
    public void test_newLinkedHashMap_dynamicArgOne() {
        // ## Arrange & Act ##
        LinkedHashMap<String, String> map = DfCollectionUtil.newLinkedHashMap("foo", "bar");

        // ## Assert ##
        assertEquals(1, map.size());
        assertEquals("bar", map.get("foo"));
    }

    public void test_newLinkedHashMap_dynamicArgTwo() {
        // ## Arrange & Act ##
        LinkedHashMap<String, String> map = DfCollectionUtil.newLinkedHashMap("foo", "bar", "baz", "qux");

        // ## Assert ##
        assertEquals(2, map.size());
        assertEquals("bar", map.get("foo"));
        assertEquals("qux", map.get("baz"));
    }

    // ===================================================================================
    //                                                                               Order
    //                                                                               =====
    public void test_orderAccordingTo_basic() throws Exception {
        // ## Arrange ##
        List<Integer> unorderedList = newArrayList(4, 2, 8, 5, 1111, 9);
        AccordingToOrderResource<Integer, Integer> resource = new AccordingToOrderResource<Integer, Integer>();
        resource.setIdExtractor(new AccordingToOrderIdExtractor<Integer, Integer>() {
            public Integer extractId(Integer element) {
                return element;
            }
        });
        resource.setOrderedUniqueIdList(newArrayList(2, 1111, 4, 5, 9, 8));

        // ## Act ##
        DfCollectionUtil.orderAccordingTo(unorderedList, resource);

        // ## Assert ##
        log(unorderedList);
        assertEquals(newArrayList(2, 1111, 4, 5, 9, 8), unorderedList);
    }

    public void test_orderAccordingTo_overElement() throws Exception {
        // ## Arrange ##
        List<Integer> unorderedList = newArrayList(4, 2, 8, 5, 1111, 9);
        AccordingToOrderResource<Integer, Integer> resource = new AccordingToOrderResource<Integer, Integer>();
        resource.setIdExtractor(new AccordingToOrderIdExtractor<Integer, Integer>() {
            public Integer extractId(Integer element) {
                return element;
            }
        });
        resource.setOrderedUniqueIdList(newArrayList(5, 2, 8));

        // ## Act ##
        DfCollectionUtil.orderAccordingTo(unorderedList, resource);

        // ## Assert ##
        log(unorderedList);
        assertEquals(newArrayList(5, 2, 8, 4, 1111, 9), unorderedList);
    }

    public void test_orderAccordingTo_shortElement() throws Exception {
        // ## Arrange ##
        List<Integer> unorderedList = newArrayList(2, 8, 5);
        AccordingToOrderResource<Integer, Integer> resource = new AccordingToOrderResource<Integer, Integer>();
        resource.setIdExtractor(new AccordingToOrderIdExtractor<Integer, Integer>() {
            public Integer extractId(Integer element) {
                return element;
            }
        });
        resource.setOrderedUniqueIdList(newArrayList(5, 9, 2, 1111, 8));

        // ## Act ##
        DfCollectionUtil.orderAccordingTo(unorderedList, resource);

        // ## Assert ##
        log(unorderedList);
        assertEquals(newArrayList(5, 2, 8), unorderedList);
    }
}
