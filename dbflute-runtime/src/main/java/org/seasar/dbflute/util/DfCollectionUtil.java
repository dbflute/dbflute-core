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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jflute
 * @since 0.9.4 (2009/03/20 Friday)
 */
public class DfCollectionUtil {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final List<?> EMPTY_LIST = Collections.unmodifiableList(new ArrayList<Object>());
    private static final Map<?, ?> EMPTY_MAP = Collections.unmodifiableMap(new HashMap<Object, Object>());
    private static final Set<?> EMPTY_SET = Collections.unmodifiableSet(new HashSet<Object>());

    // ===================================================================================
    //                                                                          Collection
    //                                                                          ==========
    public static Class<?> findFirstElementType(Collection<?> collection) {
        for (Object object : collection) {
            if (object != null) {
                return object.getClass();
            }
        }
        return null;
    }

    public static boolean hasValidElement(Collection<?> collection) {
        for (Object object : collection) {
            if (object != null) {
                return true;
            }
        }
        return false;
    }

    // ===================================================================================
    //                                                                                List
    //                                                                                ====
    public static <ELEMENT> ArrayList<ELEMENT> newArrayList() {
        return new ArrayList<ELEMENT>();
    }

    public static <ELEMENT> ArrayList<ELEMENT> newArrayList(Collection<ELEMENT> elements) {
        final ArrayList<ELEMENT> list = newArrayListSized(elements.size());
        list.addAll(elements);
        return list;
    }

    public static <ELEMENT> ArrayList<ELEMENT> newArrayList(ELEMENT... elements) {
        final ArrayList<ELEMENT> list = newArrayListSized(elements.length);
        for (ELEMENT element : elements) {
            list.add(element);
        }
        return list;
    }

    public static <ELEMENT> ArrayList<ELEMENT> newArrayListSized(int size) {
        return new ArrayList<ELEMENT>(size);
    }

    @SuppressWarnings("unchecked")
    public static <ELEMENT> List<ELEMENT> emptyList() {
        return (List<ELEMENT>) EMPTY_LIST;
    }

    // -----------------------------------------------------
    //                                               Utility
    //                                               -------
    public static <ELEMENT extends Object> List<List<ELEMENT>> splitByLimit(List<ELEMENT> elementList, int limit) {
        final List<List<ELEMENT>> valueList = newArrayList();
        final int valueSize = elementList.size();
        int index = 0;
        int remainderSize = valueSize;
        do {
            final int beginIndex = limit * index;
            final int endPoint = beginIndex + limit;
            final int endIndex = limit <= remainderSize ? endPoint : valueSize;
            final List<ELEMENT> splitList = newArrayList();
            splitList.addAll(elementList.subList(beginIndex, endIndex));
            valueList.add(splitList);
            remainderSize = valueSize - endIndex;
            ++index;
        } while (remainderSize > 0);
        return valueList;
    }

    // -----------------------------------------------------
    //                                               Advance
    //                                               -------
    public static <ELEMENT> OrderDiff<ELEMENT> analyzeOrderDiff(List<ELEMENT> beforeUniqueList,
            List<ELEMENT> afterUniqueList) {
        final LinkedHashSet<ELEMENT> linkedBeforeSet = newLinkedHashSet(beforeUniqueList);
        if (beforeUniqueList.size() != linkedBeforeSet.size()) {
            String msg = "The argument 'beforeList' should be unique: " + beforeUniqueList;
            throw new IllegalArgumentException(msg);
        }
        final LinkedHashSet<ELEMENT> linkedAfterSet = newLinkedHashSet(afterUniqueList);
        if (afterUniqueList.size() != linkedAfterSet.size()) {
            String msg = "The argument 'afterList' should be unique: " + afterUniqueList;
            throw new IllegalArgumentException(msg);
        }
        final ElementDiff<ELEMENT> elementDiff = analyzeElementDiff(linkedBeforeSet, linkedAfterSet);
        final Set<ELEMENT> addedSet = elementDiff.getAddedSet();
        final Set<ELEMENT> deletedSet = elementDiff.getDeletedSet();
        final List<ELEMENT> beforeRemainingList = newArrayList(beforeUniqueList);
        beforeRemainingList.removeAll(deletedSet);
        final List<ELEMENT> afterRemainingList = newArrayList(afterUniqueList);
        afterRemainingList.removeAll(addedSet);
        if (beforeRemainingList.size() != afterRemainingList.size()) {
            String msg = "The beforeRemainingList.size() should be the same as the afterRemainingList's:";
            msg = msg + " beforeRemainingList.size()=" + beforeRemainingList.size();
            msg = msg + " afterRemainingList.size()=" + afterRemainingList.size();
            throw new IllegalStateException(msg);
        }
        final Map<ELEMENT, OrderDiffDetail<ELEMENT>> movedMap = newLinkedHashMap();
        doAnalyzeOrderChange(beforeRemainingList, afterRemainingList, movedMap);
        for (Entry<ELEMENT, OrderDiffDetail<ELEMENT>> entry : movedMap.entrySet()) {
            final ELEMENT movedElement = entry.getKey();
            final ELEMENT previousElement = entry.getValue().getPreviousElement();
            final int movedIndex = afterUniqueList.indexOf(movedElement);
            final ELEMENT realPrevious = afterUniqueList.get(movedIndex - 1);
            if (!previousElement.equals(realPrevious)) {
                entry.getValue().setPreviousElement(realPrevious);
            }
        }
        final OrderDiff<ELEMENT> orderDiff = new OrderDiff<ELEMENT>();
        orderDiff.setMovedMap(movedMap);
        return orderDiff;
    }

    protected static <ELEMENT> void doAnalyzeOrderChange(List<ELEMENT> beforeRemainingList,
            List<ELEMENT> afterRemainingList, Map<ELEMENT, OrderDiffDetail<ELEMENT>> movedMap) {
        ELEMENT movedElement = null;
        ELEMENT previousElement = null;
        for (int i = 0; i < beforeRemainingList.size(); i++) {
            final ELEMENT beforeElement = beforeRemainingList.get(i);
            final int afterIndex = afterRemainingList.indexOf(beforeElement);
            if (i == afterIndex) { // no change
                continue;
            }
            // changed
            movedElement = beforeElement;
            previousElement = afterRemainingList.get(afterIndex - 1);
            break;
        }
        if (movedElement != null) {
            final OrderDiffDetail<ELEMENT> diffResult = new OrderDiffDetail<ELEMENT>();
            diffResult.setMovedElement(movedElement);
            diffResult.setPreviousElement(previousElement);
            movedMap.put(movedElement, diffResult);
            final List<ELEMENT> movedList = moveElementToIndex(beforeRemainingList, movedElement, previousElement);
            doAnalyzeOrderChange(movedList, afterRemainingList, movedMap); // recursive call
        }
    }

    public static class OrderDiff<ELEMENT> {
        protected Map<ELEMENT, OrderDiffDetail<ELEMENT>> _movedMap;

        public Map<ELEMENT, OrderDiffDetail<ELEMENT>> getMovedMap() {
            return _movedMap;
        }

        public void setMovedMap(Map<ELEMENT, OrderDiffDetail<ELEMENT>> movedMap) {
            this._movedMap = movedMap;
        }
    }

    public static class OrderDiffDetail<ELEMENT> {
        protected ELEMENT _movedElement;
        protected ELEMENT _previousElement;

        public ELEMENT getMovedElement() {
            return _movedElement;
        }

        public void setMovedElement(ELEMENT movedElement) {
            this._movedElement = movedElement;
        }

        public ELEMENT getPreviousElement() {
            return _previousElement;
        }

        public void setPreviousElement(ELEMENT previousElement) {
            this._previousElement = previousElement;
        }
    }

    public static <ELEMENT> List<ELEMENT> moveElementToIndex(List<ELEMENT> list, ELEMENT fromElement, ELEMENT toElement) {
        assertObjectNotNull("list", list);
        final int fromIndex = list.indexOf(fromElement);
        final int toIndex = list.indexOf(toElement);
        return moveElementToIndex(list, fromIndex, toIndex);
    }

    public static <ELEMENT> List<ELEMENT> moveElementToIndex(List<ELEMENT> list, int fromIndex, int toIndex) {
        assertObjectNotNull("list", list);
        if (fromIndex == toIndex) {
            String msg = "The argument 'fromIndex' and 'toIndex' should not be same:";
            msg = msg + " fromIndex=" + fromIndex + " toIndex" + toIndex;
            throw new IllegalArgumentException(msg);
        }
        if (fromIndex < 0 || toIndex < 0) {
            String msg = "The argument 'fromIndex' and 'toIndex' should not be minus:";
            msg = msg + " fromIndex=" + fromIndex + " toIndex" + toIndex;
            throw new IllegalArgumentException(msg);
        }
        final boolean fromLess = fromIndex < toIndex;
        final List<ELEMENT> movedList = new ArrayList<ELEMENT>();
        final int firstIndex = fromLess ? fromIndex : toIndex;
        final int secondIndex = !fromLess ? fromIndex : toIndex;
        final List<ELEMENT> first = list.subList(0, firstIndex);
        final ELEMENT element = list.get(fromIndex);
        final int adjustmentIndex = fromLess ? 1 : 0;
        final List<ELEMENT> middle = list.subList(firstIndex + adjustmentIndex, secondIndex + adjustmentIndex);
        final List<ELEMENT> last = list.subList(secondIndex + 1, list.size());
        movedList.addAll(first);
        if (!fromLess) {
            movedList.add(element);
        }
        movedList.addAll(middle);
        if (fromLess) {
            movedList.add(element);
        }
        movedList.addAll(last);
        return movedList;
    }

    // ===================================================================================
    //                                                                                 Map
    //                                                                                 ===
    public static <KEY, VALUE> HashMap<KEY, VALUE> newHashMap() {
        return new HashMap<KEY, VALUE>();
    }

    public static <KEY, VALUE> HashMap<KEY, VALUE> newHashMap(Map<KEY, VALUE> map) {
        return new HashMap<KEY, VALUE>(map);
    }

    public static <KEY, VALUE> HashMap<KEY, VALUE> newHashMap(KEY key, VALUE value) {
        final HashMap<KEY, VALUE> map = newHashMapSized(1);
        map.put(key, value);
        return map;
    }

    public static <KEY, VALUE> HashMap<KEY, VALUE> newHashMap(KEY key1, VALUE value1, KEY key2, VALUE value2) {
        final HashMap<KEY, VALUE> map = newHashMapSized(2);
        map.put(key1, value1);
        map.put(key2, value2);
        return map;
    }

    public static <KEY, VALUE> HashMap<KEY, VALUE> newHashMapSized(int size) {
        return new HashMap<KEY, VALUE>(size);
    }

    public static <KEY, VALUE> LinkedHashMap<KEY, VALUE> newLinkedHashMap() {
        return new LinkedHashMap<KEY, VALUE>();
    }

    public static <KEY, VALUE> LinkedHashMap<KEY, VALUE> newLinkedHashMap(Map<KEY, VALUE> map) {
        return new LinkedHashMap<KEY, VALUE>(map);
    }

    public static <KEY, VALUE> LinkedHashMap<KEY, VALUE> newLinkedHashMap(KEY key, VALUE value) {
        final LinkedHashMap<KEY, VALUE> map = newLinkedHashMapSized(1);
        map.put(key, value);
        return map;
    }

    public static <KEY, VALUE> LinkedHashMap<KEY, VALUE> newLinkedHashMap(KEY key1, VALUE value1, KEY key2, VALUE value2) {
        final LinkedHashMap<KEY, VALUE> map = newLinkedHashMapSized(2);
        map.put(key1, value1);
        map.put(key2, value2);
        return map;
    }

    public static <KEY, VALUE> LinkedHashMap<KEY, VALUE> newLinkedHashMapSized(int size) {
        return new LinkedHashMap<KEY, VALUE>(size);
    }

    public static <KEY, VALUE> ConcurrentHashMap<KEY, VALUE> newConcurrentHashMap() {
        return new ConcurrentHashMap<KEY, VALUE>();
    }

    public static <KEY, VALUE> ConcurrentHashMap<KEY, VALUE> newConcurrentHashMap(Map<KEY, VALUE> map) {
        return new ConcurrentHashMap<KEY, VALUE>(map);
    }

    public static <KEY, VALUE> ConcurrentHashMap<KEY, VALUE> newConcurrentHashMap(KEY key, VALUE value) {
        final ConcurrentHashMap<KEY, VALUE> map = newConcurrentHashMapSized(1);
        map.put(key, value);
        return map;
    }

    public static <KEY, VALUE> ConcurrentHashMap<KEY, VALUE> newConcurrentHashMap(KEY key1, VALUE value1, KEY key2,
            VALUE value2) {
        final ConcurrentHashMap<KEY, VALUE> map = newConcurrentHashMapSized(2);
        map.put(key1, value1);
        map.put(key2, value2);
        return map;
    }

    public static <KEY, VALUE> ConcurrentHashMap<KEY, VALUE> newConcurrentHashMapSized(int size) {
        return new ConcurrentHashMap<KEY, VALUE>(size);
    }

    @SuppressWarnings("unchecked")
    public static <KEY, VALUE> Map<KEY, VALUE> emptyMap() {
        return (Map<KEY, VALUE>) EMPTY_MAP;
    }

    // ===================================================================================
    //                                                                                 Set
    //                                                                                 ===
    public static <ELEMENT> HashSet<ELEMENT> newHashSet() {
        return new HashSet<ELEMENT>();
    }

    public static <ELEMENT> HashSet<ELEMENT> newHashSet(Collection<ELEMENT> elements) {
        final HashSet<ELEMENT> set = newHashSetSized(elements.size());
        set.addAll(elements);
        return set;
    }

    public static <ELEMENT> HashSet<ELEMENT> newHashSet(ELEMENT... elements) {
        final HashSet<ELEMENT> set = newHashSetSized(elements.length);
        for (ELEMENT element : elements) {
            set.add(element);
        }
        return set;
    }

    public static <ELEMENT> HashSet<ELEMENT> newHashSetSized(int size) {
        return new HashSet<ELEMENT>(size);
    }

    public static <ELEMENT> LinkedHashSet<ELEMENT> newLinkedHashSet() {
        return new LinkedHashSet<ELEMENT>();
    }

    public static <ELEMENT> LinkedHashSet<ELEMENT> newLinkedHashSet(Collection<ELEMENT> elements) {
        final LinkedHashSet<ELEMENT> set = newLinkedHashSetSized(elements.size());
        set.addAll(elements);
        return set;
    }

    public static <ELEMENT> LinkedHashSet<ELEMENT> newLinkedHashSet(ELEMENT... elements) {
        final LinkedHashSet<ELEMENT> set = newLinkedHashSetSized(elements.length);
        for (ELEMENT element : elements) {
            set.add(element);
        }
        return set;
    }

    public static <ELEMENT> LinkedHashSet<ELEMENT> newLinkedHashSetSized(int size) {
        return new LinkedHashSet<ELEMENT>(size);
    }

    @SuppressWarnings("unchecked")
    public static <ELEMENT> Set<ELEMENT> emptySet() {
        return (Set<ELEMENT>) EMPTY_SET;
    }

    // -----------------------------------------------------
    //                                               Advance
    //                                               -------
    public static <ELEMENT> ElementDiff<ELEMENT> analyzeElementDiff(Set<ELEMENT> beforeCol, Set<ELEMENT> afterCol) {
        final Set<ELEMENT> addedSet = newLinkedHashSet();
        final Set<ELEMENT> deletedSet = newLinkedHashSet();
        final Set<ELEMENT> remainingSet = newLinkedHashSet();
        for (ELEMENT beforeElement : beforeCol) {
            if (afterCol.contains(beforeElement)) {
                remainingSet.add(beforeElement);
            } else {
                deletedSet.add(beforeElement);
            }
        }
        for (ELEMENT afterElement : afterCol) {
            if (!beforeCol.contains(afterElement)) {
                addedSet.add(afterElement);
            }
        }
        final ElementDiff<ELEMENT> elementDiff = new ElementDiff<ELEMENT>();
        elementDiff.setAddedSet(addedSet);
        elementDiff.setDeletedSet(deletedSet);
        elementDiff.setRemainingSet(remainingSet);
        return elementDiff;
    }

    public static class ElementDiff<ELEMENT> {
        protected Set<ELEMENT> _addedSet;
        protected Set<ELEMENT> _deletedSet;
        protected Set<ELEMENT> _remainingSet;

        public Set<ELEMENT> getAddedSet() {
            return _addedSet;
        }

        public void setAddedSet(Set<ELEMENT> addedSet) {
            this._addedSet = addedSet;
        }

        public Set<ELEMENT> getDeletedSet() {
            return _deletedSet;
        }

        public void setDeletedSet(Set<ELEMENT> deletedSet) {
            this._deletedSet = deletedSet;
        }

        public Set<ELEMENT> getRemainingSet() {
            return _remainingSet;
        }

        public void setRemainingSet(Set<ELEMENT> remainingSet) {
            this._remainingSet = remainingSet;
        }
    }

    // ===================================================================================
    //                                                                               Order
    //                                                                               =====
    /**
     * Order the unordered list according to specified resources.
     * @param <ELEMENT_TYPE> The type of element.
     * @param <ID_TYPE> The type of ID.
     * @param unorderedList The unordered list. (NotNull)
     * @param resource The resource of according-to-order. (NotNull)
     */
    public static <ELEMENT_TYPE, ID_TYPE> void orderAccordingTo(List<ELEMENT_TYPE> unorderedList,
            AccordingToOrderResource<ELEMENT_TYPE, ID_TYPE> resource) {
        assertObjectNotNull("unorderedList", unorderedList);
        if (unorderedList.isEmpty()) {
            return;
        }
        assertObjectNotNull("resource", resource);
        final List<ID_TYPE> orderedUniqueIdList = resource.getOrderedUniqueIdList();
        assertObjectNotNull("resource.getOrderedUniqueIdList()", orderedUniqueIdList);
        if (orderedUniqueIdList.isEmpty()) {
            return;
        }
        final AccordingToOrderIdExtractor<ELEMENT_TYPE, ID_TYPE> idExtractor = resource.getIdExtractor();
        assertObjectNotNull("resource.getIdExtractor()", idExtractor);

        final Map<ID_TYPE, Integer> idIndexMap = new LinkedHashMap<ID_TYPE, Integer>();
        int index = 0;
        for (ID_TYPE id : orderedUniqueIdList) {
            if (idIndexMap.containsKey(id)) {
                String msg = "The id was duplicated: id=" + id + " orderedUniqueIdList=" + orderedUniqueIdList;
                throw new IllegalStateException(msg);
            }
            idIndexMap.put(id, index);
            ++index;
        }
        final Comparator<ELEMENT_TYPE> comp = new Comparator<ELEMENT_TYPE>() {
            public int compare(ELEMENT_TYPE o1, ELEMENT_TYPE o2) {
                final ID_TYPE id1 = idExtractor.extractId(o1);
                final ID_TYPE id2 = idExtractor.extractId(o2);
                assertObjectNotNull("id1 of " + o1, id1);
                assertObjectNotNull("id2 of " + o2, id2);
                final Integer index1 = idIndexMap.get(id1);
                final Integer index2 = idIndexMap.get(id2);
                if (index1 != null && index2 != null) {
                    return index1.compareTo(index2);
                }
                if (index1 == null && index2 == null) {
                    return 0;
                }
                return index1 == null ? 1 : -1;
            }
        };
        Collections.sort(unorderedList, comp);
    }

    public static interface AccordingToOrderIdExtractor<ELEMENT_TYPE, ID_TYPE> {

        /**
         * Extract ID from the element instance.
         * @param element Element instance. (NotNull)
         * @return Extracted ID. (NotNull)
         */
        ID_TYPE extractId(ELEMENT_TYPE element);
    }

    public static class AccordingToOrderResource<ELEMENT_TYPE, ID_TYPE> {
        protected List<ID_TYPE> _orderedUniqueIdList;
        protected AccordingToOrderIdExtractor<ELEMENT_TYPE, ID_TYPE> _idExtractor;

        public AccordingToOrderResource<ELEMENT_TYPE, ID_TYPE> setupResource(List<ID_TYPE> orderedUniqueIdList,
                AccordingToOrderIdExtractor<ELEMENT_TYPE, ID_TYPE> idExtractor) {
            setOrderedUniqueIdList(orderedUniqueIdList);
            setIdExtractor(idExtractor);
            return this;
        }

        public List<ID_TYPE> getOrderedUniqueIdList() {
            return _orderedUniqueIdList;
        }

        public void setOrderedUniqueIdList(List<ID_TYPE> orderedUniqueIdList) {
            _orderedUniqueIdList = orderedUniqueIdList;
        }

        public AccordingToOrderIdExtractor<ELEMENT_TYPE, ID_TYPE> getIdExtractor() {
            return _idExtractor;
        }

        public void setIdExtractor(AccordingToOrderIdExtractor<ELEMENT_TYPE, ID_TYPE> idExtractor) {
            _idExtractor = idExtractor;
        }
    }

    // ===================================================================================
    //                                                                       Assert Helper
    //                                                                       =============
    protected static void assertObjectNotNull(String variableName, Object value) {
        if (variableName == null) {
            String msg = "The value should not be null: variableName=null value=" + value;
            throw new IllegalArgumentException(msg);
        }
        if (value == null) {
            String msg = "The value should not be null: variableName=" + variableName;
            throw new IllegalArgumentException(msg);
        }
    }
}
