/*
 * Copyright 2014-2019 the original author or authors.
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
import java.util.NoSuchElementException;
import java.util.Set;

import org.dbflute.Entity;
import org.dbflute.cbean.result.grouping.GroupingListDeterminer;
import org.dbflute.cbean.result.grouping.GroupingListRowResource;
import org.dbflute.cbean.result.grouping.GroupingMapDeterminer;
import org.dbflute.cbean.result.mapping.EntityColumnExtractor;
import org.dbflute.cbean.result.mapping.EntityDtoMapper;
import org.dbflute.cbean.sqlclause.orderby.OrderByClause;
import org.dbflute.util.DfTypeUtil;

/**
 * The result bean for list. <br>
 * You can treat it as normal List, because this implements java.util.List.
 * <pre>
 * [Extension Method]
 * mappingList()  : mapping to other class, and returns as list
 * groupingList() : grouping the list per category, and returns as list
 * groupingMap()  : grouping the list per category, and returns as map
 * extractColumnList() : extract one column as value list
 * extractColumnSet()  : extract one column as value list
 * </pre>
 * @param <ENTITY> The type of entity for the element of selected list.
 * @author jflute
 */
public class ListResultBean<ENTITY> implements List<ENTITY>, Serializable {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The serial version UID for object serialization. (Default) */
    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The DB name of table. (NullAllowed: if null, means not-selected-yet) */
    protected String _tableDbName;

    /** The count of all record. */
    protected int _allRecordCount;

    /** The clause of order-by. (NullAllowed: lazy-loaded) */
    protected transient OrderByClause _orderByClause; // #later: removed

    /** The list of selected entity. (NullAllowed: lazy-loaded) */
    protected List<ENTITY> _selectedList;

    // ===================================================================================
    //                                                                             Mapping
    //                                                                             =======
    /**
     * Map the entity list to the list of other object.
     * <pre>
     * ListResultBean&lt;MemberWebBean&gt; beanList = memberList.<span style="color: #CC4747">mappingList</span>(<span style="color: #553000">member</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     MemberWebBean <span style="color: #553000">bean</span> = new MemberWebBean();
     *     <span style="color: #553000">bean</span>.setMemberId(member.getMemberId());
     *     <span style="color: #553000">bean</span>.setMemberName(<span style="color: #553000">member</span>.getMemberName());
     *     ...
     *     return <span style="color: #553000">bean</span>;
     * });
     * </pre>
     * <p>This method needs the property 'selectedList' only.</p>
     * @param <DTO> The type of DTO.
     * @param entityLambda The callback for mapping of entity and DTO. (NotNull)
     * @return The new mapped list as result bean. (NotNull)
     */
    public <DTO> ListResultBean<DTO> mappingList(EntityDtoMapper<ENTITY, DTO> entityLambda) {
        final List<DTO> mappingList;
        if (hasWrappedListInstance()) {
            final List<ENTITY> selectedList = getSelectedList();
            mappingList = new ArrayList<DTO>(selectedList.size());
            for (ENTITY entity : selectedList) {
                mappingList.add(entityLambda.map(entity));
            }
        } else {
            mappingList = null;
        }
        return createInheritedResultBean(mappingList);
    }

    // ===================================================================================
    //                                                                            Grouping
    //                                                                            ========
    /**
     * Return grouping list (split the list per group). <br>
     * This method needs the property 'selectedList' only.
     * <pre>
     * <span style="color: #3F7E5E">// e.g. grouping per three records</span>
     * List&lt;ListResultBean&lt;Member&gt;&gt; groupingList = memberList.<span style="color: #CC4747">groupingList</span>(new GroupingListDeterminer&lt;Member&gt;() {
     *     public boolean isBreakRow(GroupingListRowResource&lt;Member&gt; rowResource, Member nextEntity) {
     *         return rowResource.getNextIndex() &gt;= 3;
     *     }
     * }, groupingOption);
     * 
     * <span style="color: #3F7E5E">// e.g. grouping per initial character of MEMBER_NAME</span>
     * List&lt;ListResultBean&lt;Member&gt;&gt; groupingList = memberList.<span style="color: #CC4747">groupingList</span>(new GroupingListDeterminer&lt;Member&gt;() {
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
        if (!hasWrappedListInstance()) {
            return groupingRowList; // not read-only
        }
        GroupingListRowResource<ENTITY> rowResource = new GroupingListRowResource<ENTITY>();
        int rowElementIndex = 0;
        int wholeElementIndex = 0;
        final List<ENTITY> selectedList = getSelectedList();
        for (ENTITY entity : selectedList) {
            // set up row resource
            rowResource.addGroupingEntity(entity);
            rowResource.setCurrentIndex(rowElementIndex);

            if (selectedList.size() == (wholeElementIndex + 1)) { // last loop
                groupingRowList.add(createInheritedResultBean(rowResource.getGroupingEntityList()));
                break;
            }

            // not last loop so the nextElement must exist
            final ENTITY nextElement = selectedList.get(wholeElementIndex + 1);

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
     * Return grouping map (split the list per group key). <br>
     * This method needs the property 'selectedList' only.
     * <pre>
     * <span style="color: #3F7E5E">// e.g. grouping per initial character of MEMBER_NAME</span>
     * Map&lt;String, ListResultBean&lt;Member&gt;&gt; groupingMap = memberList.<span style="color: #CC4747">groupingMap</span>(member <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     return member.getMemberName().substring(0, 1);
     * });
     * </pre>
     * @param entityLambda The callback for determiner of grouping map. (NotNull)
     * @return The grouping map that has grouped rows, the key is provided. (NotNull)
     */
    public Map<String, ListResultBean<ENTITY>> groupingMap(GroupingMapDeterminer<ENTITY> entityLambda) {
        final Map<String, ListResultBean<ENTITY>> groupingListMap = new LinkedHashMap<String, ListResultBean<ENTITY>>();
        if (!hasWrappedListInstance()) {
            return groupingListMap; // not read-only
        }
        for (ENTITY entity : getSelectedList()) {
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
    //                                                                      Extract Column
    //                                                                      ==============
    /**
     * Extract the value list of the column specified in extractor.
     * <pre>
     * List&lt;Integer&gt; memberIdList = memberList.<span style="color: #CC4747">extractColumnList</span>(member <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     return entity.getMemberId();
     * });
     * </pre>
     * <p>This method needs the property 'selectedList' only.</p>
     * @param <COLUMN> The type of COLUMN.
     * @param entityLambda The callback for value extractor of entity column. (NotNull)
     * @return The value list of the entity column. (NotNull, NotNullElement)
     */
    public <COLUMN> List<COLUMN> extractColumnList(EntityColumnExtractor<ENTITY, COLUMN> entityLambda) {
        if (!hasWrappedListInstance()) {
            return new ArrayList<COLUMN>(2); // not read-only
        }
        final List<ENTITY> selectedList = getSelectedList();
        final List<COLUMN> columnList = new ArrayList<COLUMN>(selectedList.size());
        for (ENTITY entity : selectedList) {
            final COLUMN column = entityLambda.extract(entity);
            if (column != null) {
                columnList.add(column);
            }
        }
        return columnList;
    }

    /**
     * Extract the value set of the column specified in extractor.
     * <pre>
     * Set&lt;Integer&gt; memberIdList = memberList.<span style="color: #CC4747">extractColumnSet</span>(member <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     return entity.getMemberId();
     * });
     * </pre>
     * <p>This method needs the property 'selectedList' only.</p>
     * @param <COLUMN> The type of COLUMN.
     * @param entityLambda The callback for value extractor of entity column. (NotNull)
     * @return The value set of the entity column. (NotNull, NotNullElement)
     */
    public <COLUMN> Set<COLUMN> extractColumnSet(EntityColumnExtractor<ENTITY, COLUMN> entityLambda) {
        if (!hasWrappedListInstance()) {
            return new LinkedHashSet<COLUMN>(2); // not read-only
        }
        final List<ENTITY> selectedList = getSelectedList();
        final Set<COLUMN> columnSet = new LinkedHashSet<COLUMN>(selectedList.size());
        for (ENTITY entity : selectedList) {
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
     * Is this result by selected?
     * @return The determination, true or false. {whether table DB name is not null}
     */
    public boolean isSelectedResult() {
        return _tableDbName != null;
    }

    // ===================================================================================
    //                                                                Inherited ResultBean
    //                                                                ====================
    protected <ELEMENT> ListResultBean<ELEMENT> createInheritedResultBean(List<ELEMENT> selectedList) {
        final ResultBeanBuilder<ELEMENT> builder = newResultBeanBuilder(getTableDbName());
        return builder.buildListInherited(this, selectedList);
    }

    protected <ELEMENT> ResultBeanBuilder<ELEMENT> newResultBeanBuilder(String tableDbName) {
        return new ResultBeanBuilder<ELEMENT>(tableDbName);
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    /**
     * @return Hash-code from primary-keys.
     */
    public int hashCode() {
        int result = 17;
        if (hasWrappedListInstance()) {
            result = (31 * result) + getSelectedList().hashCode();
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
        if (!hasWrappedListInstance()) {
            return false; // basically unreachable
        }
        if (other instanceof ListResultBean<?>) {
            return getSelectedList().equals(((ListResultBean<?>) other).getSelectedList());
        } else {
            return getSelectedList().equals(other);
        }
    }

    /**
     * @return The view string of all attribute values. (NotNull)
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{").append(_tableDbName).append(", ").append(_allRecordCount).append(", ").append(_selectedList).append("}");
        return sb.toString();
    }

    /**
     * @return The rich string of all record values. (NotNull)
     */
    public String toRichString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(DfTypeUtil.toClassTitle(this)).append(" Show:");
        final StringBuilder headerSb = new StringBuilder();
        buildRichStringHeader(headerSb);
        final String header = headerSb.toString();
        sb.append(!header.isEmpty() ? (header.contains("\n") ? "\n" : " ") : "").append(headerSb);
        if (!isEmpty()) {
            forEach(entity -> {
                sb.append("\n");
                if (entity instanceof Entity) {
                    sb.append(((Entity) entity).toStringWithRelation());
                } else {
                    sb.append(entity.toString());
                }
            });
        } else {
            sb.append(" ").append("*empty");
        }
        return sb.toString();
    }

    protected void buildRichStringHeader(StringBuilder sb) {
    }

    // ===================================================================================
    //                                                                       List Elements
    //                                                                       =============
    public boolean add(ENTITY o) {
        return getSelectedList().add(o);
    }

    public boolean addAll(Collection<? extends ENTITY> c) {
        return getSelectedList().addAll(c);
    }

    public void clear() {
        if (hasWrappedListInstance()) {
            getSelectedList().clear();
        }
    }

    public boolean contains(Object o) {
        return hasWrappedListInstance() ? getSelectedList().contains(o) : false;
    }

    public boolean containsAll(Collection<?> c) {
        return hasWrappedListInstance() ? getSelectedList().containsAll(c) : false;
    }

    public boolean isEmpty() {
        return hasWrappedListInstance() ? getSelectedList().isEmpty() : true; // empty if no instance
    }

    public Iterator<ENTITY> iterator() {
        return hasWrappedListInstance() ? getSelectedList().iterator() : createEmptyIterator();
    }

    protected Iterator<ENTITY> createEmptyIterator() {
        return new Iterator<ENTITY>() {
            public boolean hasNext() {
                return false;
            }

            public ENTITY next() {
                throw new NoSuchElementException();
            }
        };
    }

    public boolean remove(Object o) {
        return getSelectedList().remove(o);
    }

    public boolean removeAll(Collection<?> c) {
        return getSelectedList().removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return getSelectedList().retainAll(c);
    }

    public int size() {
        return getSelectedList().size();
    }

    public Object[] toArray() {
        return getSelectedList().toArray();
    }

    public <TYPE> TYPE[] toArray(TYPE[] a) {
        return getSelectedList().toArray(a);
    }

    public void add(int index, ENTITY element) {
        getSelectedList().add(index, element);
    }

    public boolean addAll(int index, Collection<? extends ENTITY> c) {
        return getSelectedList().addAll(index, c);
    }

    public ENTITY get(int index) {
        return getSelectedList().get(index);
    }

    public int indexOf(Object o) {
        return getSelectedList().indexOf(o);
    }

    public int lastIndexOf(Object o) {
        return getSelectedList().lastIndexOf(o);
    }

    public ListIterator<ENTITY> listIterator() {
        return getSelectedList().listIterator();
    }

    public ListIterator<ENTITY> listIterator(int index) {
        return getSelectedList().listIterator(index);
    }

    public ENTITY remove(int index) {
        return getSelectedList().remove(index);
    }

    public ENTITY set(int index, ENTITY element) {
        return getSelectedList().set(index, element);
    }

    public List<ENTITY> subList(int fromIndex, int toIndex) {
        return getSelectedList().subList(fromIndex, toIndex);
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    /**
     * Get the value of tableDbName.
     * @return The DB name of table. (NullAllowed: if null, it means not-selected-yet)
     */
    public String getTableDbName() {
        return _tableDbName;
    }

    /**
     * Set the value of tableDbName.
     * @param tableDbName The DB name of table. (NotNull)
     */
    public void setTableDbName(String tableDbName) {
        _tableDbName = tableDbName;
    }

    /**
     * Get the value of allRecordCount.
     * @return The count of all records.
     */
    public int getAllRecordCount() {
        return _allRecordCount;
    }

    /**
     * Set the value of allRecordCount.
     * @param allRecordCount The count of all records.
     */
    public void setAllRecordCount(int allRecordCount) {
        _allRecordCount = allRecordCount;
    }

    /**
     * Get the value of orderByClause.
     * @return The value of orderByClause. (NotNull)
     * @deprecated don't use this
     */
    public OrderByClause getOrderByClause() {
        if (_orderByClause == null) {
            _orderByClause = new OrderByClause();
        }
        return _orderByClause;
    }

    /**
     * Set the value of orderByClause.
     * @param orderByClause The value of orderByClause. (NullAllowed)
     */
    public void setOrderByClause(OrderByClause orderByClause) {
        _orderByClause = orderByClause;
    }

    /**
     * Get the value of selectedList.
     * @return Selected list. (NotNull)
     */
    public List<ENTITY> getSelectedList() {
        if (_selectedList == null) {
            _selectedList = new ArrayList<ENTITY>();
        }
        return _selectedList;
    }

    /**
     * Set the value of selectedList.
     * @param selectedList Selected list. (NullAllowed: if null, clear the list)
     */
    public void setSelectedList(List<ENTITY> selectedList) {
        _selectedList = selectedList;
    }

    protected boolean hasWrappedListInstance() {
        return _selectedList != null;
    }
}
