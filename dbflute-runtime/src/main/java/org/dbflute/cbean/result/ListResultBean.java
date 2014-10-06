/*
 * Copyright 2014-2014 the original author or authors.
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
package org.dbflute.cbean.result;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.dbflute.cbean.result.grouping.GroupingListDeterminer;
import org.dbflute.cbean.result.grouping.GroupingListRowResource;
import org.dbflute.cbean.result.grouping.GroupingMapDeterminer;
import org.dbflute.cbean.result.mapping.EntityColumnExtractor;
import org.dbflute.cbean.result.mapping.EntityDtoMapper;
import org.dbflute.cbean.sqlclause.orderby.OrderByClause;

/**
 * The result bean for list.
 * @param <ENTITY> The type of entity for the element of selected list.
 * @author jflute
 */
public class ListResultBean<ENTITY> implements List<ENTITY>, Serializable {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Serial version UID. (Default) */
    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The value of table db-name. (NullAllowed: If it's null, it means 'Not Selected Yet'.) */
    protected String _tableDbName;

    /** The value of all record count. */
    protected int _allRecordCount;

    /** The list of selected entity. (NotNull) */
    protected List<ENTITY> _selectedList = new ArrayList<ENTITY>();

    /** The clause of order-by. (NotNull) */
    protected OrderByClause _orderByClause = new OrderByClause();

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * Constructor.
     */
    public ListResultBean() {
    }

    // ===================================================================================
    //                                                                            Grouping
    //                                                                            ========
    /**
     * Return grouping list (split the list per group). <br />
     * This method needs the property 'selectedList' only.
     * <pre>
     * <span style="color: #3F7E5E">// e.g. grouping per three records</span>
     * List&lt;ListResultBean&lt;Member&gt;&gt; groupingList = memberList.<span style="color: #DD4747">groupingList</span>(new GroupingListDeterminer&lt;Member&gt;() {
     *     public boolean isBreakRow(GroupingListRowResource&lt;Member&gt; rowResource, Member nextEntity) {
     *         return rowResource.getNextIndex() >= 3;
     *     }
     * }, groupingOption);
     * 
     * <span style="color: #3F7E5E">// e.g. grouping per initial character of MEMBER_NAME</span>
     * List&lt;ListResultBean&lt;Member&gt;&gt; groupingList = memberList.<span style="color: #DD4747">groupingList</span>(new GroupingListDeterminer&lt;Member&gt;() {
     *     public boolean isBreakRow(GroupingListRowResource&lt;Member&gt; rowResource, Member nextEntity) {
     *         Member currentEntity = rowResource.getCurrentEntity();
     *         String currentInitChar = currentEntity.getMemberName().substring(0, 1);
     *         String nextInitChar = nextEntity.getMemberName().substring(0, 1);
     *         return !currentInitChar.equalsIgnoreCase(nextInitChar);
     *     }
     * });
     * </pre>
     * @param determiner The determiner of grouping list. (NotNull)
     * @return The grouping list that has grouped rows. (NotNull)
     */
    public List<ListResultBean<ENTITY>> groupingList(GroupingListDeterminer<ENTITY> determiner) {
        final List<ListResultBean<ENTITY>> groupingRowList = new ArrayList<ListResultBean<ENTITY>>();
        GroupingListRowResource<ENTITY> rowResource = new GroupingListRowResource<ENTITY>();
        int rowElementIndex = 0;
        int wholeElementIndex = 0;
        for (ENTITY entity : _selectedList) {
            // set up row resource
            rowResource.addGroupingEntity(entity);
            rowResource.setCurrentIndex(rowElementIndex);

            if (_selectedList.size() == (wholeElementIndex + 1)) { // last loop
                groupingRowList.add(createInheritedResultBean(rowResource.getGroupingEntityList()));
                break;
            }

            // not last loop so the nextElement must exist
            final ENTITY nextElement = _selectedList.get(wholeElementIndex + 1);

            // do at row end
            if (determiner.isBreakRow(rowResource, nextElement)) { // determine the row end
                groupingRowList.add(createInheritedResultBean(rowResource.getGroupingEntityList()));
                rowResource = new GroupingListRowResource<ENTITY>(); // for the next row
                rowElementIndex = 0;
                ++wholeElementIndex;
                continue;
            }
            ++rowElementIndex;
            ++wholeElementIndex;
        }
        return groupingRowList;
    }

    /**
     * Return grouping map (split the list per group key). <br />
     * This method needs the property 'selectedList' only.
     * <pre>
     * <span style="color: #3F7E5E">// e.g. grouping per initial character of MEMBER_NAME</span>
     * Map&lt;String, ListResultBean&lt;Member&gt;&gt; groupingMap = memberList.<span style="color: #DD4747">groupingMap</span>(new GroupingMapDeterminer&lt;Member&gt;() {
     *     public String provideKey(Member entity) {
     *         return entity.getMemberName().substring(0, 1);
     *     }
     * });
     * </pre>
     * @param entityLambda The callback for determiner of grouping map. (NotNull)
     * @return The grouping map that has grouped rows, the key is provided. (NotNull)
     */
    public Map<String, ListResultBean<ENTITY>> groupingMap(GroupingMapDeterminer<ENTITY> entityLambda) {
        final Map<String, ListResultBean<ENTITY>> groupingListMap = new LinkedHashMap<String, ListResultBean<ENTITY>>();
        for (ENTITY entity : _selectedList) {
            final String groupingKey = entityLambda.provideKey(entity);
            ListResultBean<ENTITY> rowList = groupingListMap.get(groupingKey);
            if (rowList == null) {
                rowList = createInheritedResultBean(null); // no set selectedList
                groupingListMap.put(groupingKey, rowList);
            }
            rowList.add(entity);
        }
        return groupingListMap;
    }

    // ===================================================================================
    //                                                                             Mapping
    //                                                                             =======
    /**
     * Map the entity list to the list of other object. <br />
     * This method needs the property 'selectedList' only.
     * <pre>
     * ListResultBean&lt;MemberDto&gt; dtoList
     *         = entityList.<span style="color: #DD4747">mappingList</span>(new EntityDtoMapper&lt;Member, MemberDto&gt;() {
     *     public MemberDto map(Member entity) {
     *         MemberDto dto = new MemberDto();
     *         dto.setMemberId(entity.getMemberId());
     *         dto.setMemberName(entity.getMemberName());
     *         ...
     *         return dto;
     *     }
     * });
     * </pre>
     * @param <DTO> The type of DTO.
     * @param entityLambda The callback for mapping of entity and DTO. (NotNull)
     * @return The new mapped list as result bean. (NotNull)
     */
    public <DTO> ListResultBean<DTO> mappingList(EntityDtoMapper<ENTITY, DTO> entityLambda) {
        final ListResultBean<DTO> mappingList = new ListResultBean<DTO>();
        for (ENTITY entity : _selectedList) {
            mappingList.add(entityLambda.map(entity));
        }
        mappingList.setTableDbName(getTableDbName());
        mappingList.setAllRecordCount(getAllRecordCount());
        mappingList.setOrderByClause(getOrderByClause());
        return mappingList;
    }

    // ===================================================================================
    //                                                                      Extract Column
    //                                                                      ==============
    /**
     * Extract the value list of the column specified in extractor. <br />
     * This method needs the property 'selectedList' only.
     * <pre>
     * List&lt;Integer&gt; memberIdList
     *         = entityList.<span style="color: #DD4747">extractColumnList</span>(new EntityColumnExtractor&lt;Member, Integer&gt;() {
     *     public Integer extract(Member entity) {
     *         return entity.getMemberId();
     *     }
     * });
     * </pre>
     * @param <COLUMN> The type of COLUMN.
     * @param entityLambda The callback for value extractor of entity column. (NotNull)
     * @return The value list of the entity column. (NotNull, NotNullElement)
     */
    public <COLUMN> List<COLUMN> extractColumnList(EntityColumnExtractor<ENTITY, COLUMN> entityLambda) {
        final List<COLUMN> columnList = new ArrayList<COLUMN>();
        for (ENTITY entity : _selectedList) {
            final COLUMN column = entityLambda.extract(entity);
            if (column != null) {
                columnList.add(column);
            }
        }
        return columnList;
    }

    /**
     * Extract the value set of the column specified in extractor. <br />
     * This method needs the property 'selectedList' only.
     * <pre>
     * Set&lt;Integer&gt; memberIdList
     *         = entityList.<span style="color: #DD4747">extractColumnSet</span>(new EntityColumnExtractor&lt;Member, Integer&gt;() {
     *     public Integer extract(Member entity) {
     *         return entity.getMemberId();
     *     }
     * });
     * </pre>
     * @param <COLUMN> The type of COLUMN.
     * @param entityLambda The callback for value extractor of entity column. (NotNull)
     * @return The value set of the entity column. (NotNull, NotNullElement)
     */
    public <COLUMN> Set<COLUMN> extractColumnSet(EntityColumnExtractor<ENTITY, COLUMN> entityLambda) {
        final Set<COLUMN> columnSet = new LinkedHashSet<COLUMN>();
        for (ENTITY entity : _selectedList) {
            COLUMN column = entityLambda.extract(entity);
            if (column != null) {
                columnSet.add(column);
            }
        }
        return columnSet;
    }

    // ===================================================================================
    //                                                                       Determination
    //                                                                       =============
    /**
     * Has this result selected?
     * @return The determination, true or false. {Whether table DB name is not null}
     */
    public boolean isSelectedResult() {
        return _tableDbName != null;
    }

    // ===================================================================================
    //                                                                Inherited ResultBean
    //                                                                ====================
    protected ListResultBean<ENTITY> createInheritedResultBean(List<ENTITY> selectedList) {
        final ListResultBean<ENTITY> rb = newInheritedResultBean();
        rb.setTableDbName(getTableDbName());
        rb.setAllRecordCount(getAllRecordCount());
        rb.setOrderByClause(getOrderByClause());
        rb.setSelectedList(selectedList); // if null, nothing is set
        return rb;
    }

    protected ListResultBean<ENTITY> newInheritedResultBean() {
        return new ListResultBean<ENTITY>();
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    /**
     * @return Hash-code from primary-keys.
     */
    public int hashCode() {
        int result = 17;
        if (_selectedList != null) {
            result = (31 * result) + _selectedList.hashCode();
        }
        return result;
    }

    /**
     * @param other Other entity. (NullAllowed)
     * @return Comparing result. If other is null, returns false.
     */
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (!(other instanceof List<?>)) {
            return false;
        }
        if (_selectedList == null) {
            return false; // basically unreachable
        }
        if (other instanceof ListResultBean<?>) {
            return _selectedList.equals(((ListResultBean<?>) other).getSelectedList());
        } else {
            return _selectedList.equals(other);
        }
    }

    /**
     * @return The view string of all attribute values. (NotNull)
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{").append(_tableDbName);
        sb.append(",").append(_allRecordCount);
        sb.append(",").append(_orderByClause != null ? _orderByClause.getOrderByClause() : null);
        sb.append(",").append(_selectedList);
        sb.append("}");
        return sb.toString();
    }

    // ===================================================================================
    //                                                                       List Elements
    //                                                                       =============
    public boolean add(ENTITY o) {
        return _selectedList.add(o);
    }

    public boolean addAll(Collection<? extends ENTITY> c) {
        return _selectedList.addAll(c);
    }

    public void clear() {
        _selectedList.clear();
    }

    public boolean contains(Object o) {
        return _selectedList.contains(o);
    }

    public boolean containsAll(Collection<?> c) {
        return _selectedList.containsAll(c);
    }

    public boolean isEmpty() {
        return _selectedList.isEmpty();
    }

    public Iterator<ENTITY> iterator() {
        return _selectedList.iterator();
    }

    public boolean remove(Object o) {
        return _selectedList.remove(o);
    }

    public boolean removeAll(Collection<?> c) {
        return _selectedList.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return _selectedList.retainAll(c);
    }

    public int size() {
        return _selectedList.size();
    }

    public Object[] toArray() {
        return _selectedList.toArray();
    }

    public <TYPE> TYPE[] toArray(TYPE[] a) {
        return _selectedList.toArray(a);
    }

    public void add(int index, ENTITY element) {
        _selectedList.add(index, element);
    }

    public boolean addAll(int index, Collection<? extends ENTITY> c) {
        return _selectedList.addAll(index, c);
    }

    public ENTITY get(int index) {
        return _selectedList.get(index);
    }

    public int indexOf(Object o) {
        return _selectedList.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        return _selectedList.lastIndexOf(o);
    }

    public ListIterator<ENTITY> listIterator() {
        return _selectedList.listIterator();
    }

    public ListIterator<ENTITY> listIterator(int index) {
        return _selectedList.listIterator(index);
    }

    public ENTITY remove(int index) {
        return _selectedList.remove(index);
    }

    public ENTITY set(int index, ENTITY element) {
        return _selectedList.set(index, element);
    }

    public List<ENTITY> subList(int fromIndex, int toIndex) {
        return _selectedList.subList(fromIndex, toIndex);
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    /**
     * Get the value of tableDbName.
     * @return The value of tableDbName. (NullAllowed: if null, it means not-selected-yet)
     */
    public String getTableDbName() {
        return _tableDbName;
    }

    /**
     * Set the value of tableDbName.
     * @param tableDbName The value of tableDbName. (NotNull)
     */
    public void setTableDbName(String tableDbName) {
        _tableDbName = tableDbName;
    }

    /**
     * Get the value of allRecordCount.
     * @return The value of allRecordCount.
     */
    public int getAllRecordCount() {
        return _allRecordCount;
    }

    /**
     * Set the value of allRecordCount.
     * @param allRecordCount The value of allRecordCount.
     */
    public void setAllRecordCount(int allRecordCount) {
        _allRecordCount = allRecordCount;
    }

    /**
     * Get the value of selectedList.
     * @return Selected list. (NotNull)
     */
    public List<ENTITY> getSelectedList() {
        return _selectedList;
    }

    /**
     * Set the value of selectedList.
     * @param selectedList Selected list. (NotNull: if you set null, it ignores it)
     */
    public void setSelectedList(List<ENTITY> selectedList) {
        if (selectedList == null) {
            return; // not allowed to set null value to the selected list
        }
        _selectedList = selectedList;
    }

    /**
     * Get the value of orderByClause.
     * @return The value of orderByClause. (NotNull)
     */
    public OrderByClause getOrderByClause() {
        return _orderByClause;
    }

    /**
     * Set the value of orderByClause.
     * @param orderByClause The value of orderByClause. (NotNull: if you set null, it ignores it)
     */
    public void setOrderByClause(OrderByClause orderByClause) {
        if (orderByClause == null) {
            return; // not allowed to set null value to the selected list
        }
        _orderByClause = orderByClause;
    }
}
