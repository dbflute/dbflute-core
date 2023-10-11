/*
 * Copyright 2014-2023 the original author or authors.
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

import java.util.List;

import org.dbflute.cbean.ConditionBean;
import org.dbflute.cbean.paging.PagingBean;

/**
 * The builder of result bean.
 * @param <ENTITY> The type of entity.
 * @author jflute
 */
public class ResultBeanBuilder<ENTITY> {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _tableDbName; // null allowed

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public ResultBeanBuilder(String tableDbName) {
        _tableDbName = tableDbName;
    }

    // ===================================================================================
    //                                                                                List
    //                                                                                ====
    /**
     * Build the result bean of list. {for CB}
     * @param cb The condition-bean. (NotNull)
     * @param selectedList The actually selected list of the entity. (NotNull)
     * @return The result bean of list. (NullAllowed: if null, treated as empty)
     */
    public ListResultBean<ENTITY> buildListOfCB(ConditionBean cb, List<ENTITY> selectedList) {
        ListResultBean<ENTITY> rb = newListResultBean();
        rb.setTableDbName(_tableDbName);
        rb.setAllRecordCount(selectedList.size());
        rb.setOrderByClause(cb.getOrderByComponent());
        rb.setSelectedList(selectedList);
        return rb;
    }

    /**
     * Build the result bean of list with all record count, order-by clause. {for Nested}
     * @param inherited The result bean to inherit option info, e.g. all record count. (NotNull) 
     * @param selectedList The actually selected list of the entity. (NotNull)
     * @return The result bean of list. (NullAllowed: if null, treated as empty)
     */
    @SuppressWarnings("deprecation")
    public ListResultBean<ENTITY> buildListInherited(ListResultBean<?> inherited, List<ENTITY> selectedList) {
        ListResultBean<ENTITY> rb = newListResultBean();
        rb.setTableDbName(_tableDbName);
        rb.setAllRecordCount(inherited.getAllRecordCount());
        rb.setOrderByClause(inherited.getOrderByClause());
        rb.setSelectedList(selectedList);
        return rb;
    }

    /**
     * Build the result bean of list without order-by clause. {for Simple}
     * @param selectedList The actually selected list of the entity. (NotNull)
     * @return The result bean of list. (NullAllowed: if null, treated as empty)
     */
    public ListResultBean<ENTITY> buildListSimply(List<ENTITY> selectedList) {
        ListResultBean<ENTITY> rb = newListResultBean();
        rb.setTableDbName(_tableDbName);
        rb.setAllRecordCount(selectedList.size());
        rb.setSelectedList(selectedList);
        return rb;
    }

    /**
     * Build the result bean of list as empty. {for Paging}
     * @param pb The bean of paging. (NotNull)
     * @return The result bean of list as empty. (NotNull)
     */
    public ListResultBean<ENTITY> buildEmptyListOfPaging(PagingBean pb) {
        ListResultBean<ENTITY> rb = newListResultBean();
        rb.setTableDbName(_tableDbName);
        rb.setOrderByClause(pb.getOrderByComponent());
        return rb;
    }

    /**
     * Build the result bean of list as empty. {for General}
     * @return The result bean of list as empty. (NotNull)
     */
    public ListResultBean<ENTITY> buildEmptyListSimply() {
        ListResultBean<ENTITY> rb = newListResultBean();
        rb.setTableDbName(_tableDbName);
        return rb;
    }

    protected ListResultBean<ENTITY> newListResultBean() {
        return new ListResultBean<ENTITY>();
    }

    // ===================================================================================
    //                                                                              Paging
    //                                                                              ======
    /**
     * Build the result bean of paging. {for Paging}
     * @param pb The bean of paging. (NotNull)
     * @param allRecordCount All record count.
     * @param selectedList The list of selected entity. (NotNull)
     * @return The result bean of paging. (NotNull)
     */
    public PagingResultBean<ENTITY> buildPagingOfPaging(PagingBean pb, int allRecordCount, List<ENTITY> selectedList) {
        PagingResultBean<ENTITY> rb = newPagingResultBean();
        rb.setTableDbName(_tableDbName);
        rb.setAllRecordCount(allRecordCount);
        rb.setOrderByClause(pb.getOrderByComponent());
        rb.setSelectedList(selectedList);
        rb.setPageSize(pb.getFetchSize());
        rb.setCurrentPageNumber(pb.getFetchPageNumber());
        return rb;
    }

    /**
     * Build the result bean of paging. {for Simple}
     * @param pageSize The record count of one page.
     * @param pageNumber The number of current page.
     * @param allRecordCount All record count.
     * @param selectedList The list of selected entity. (NotNull)
     * @return The result bean of paging. (NotNull)
     */
    public PagingResultBean<ENTITY> buildPagingSimply(int pageSize, int pageNumber, int allRecordCount, List<ENTITY> selectedList) {
        PagingResultBean<ENTITY> rb = newPagingResultBean();
        rb.setTableDbName(_tableDbName);
        rb.setAllRecordCount(allRecordCount);
        rb.setSelectedList(selectedList);
        rb.setPageSize(pageSize);
        rb.setCurrentPageNumber(pageNumber);
        return rb;
    }

    /**
     * Build the result bean of paging as empty. {for Empty}
     * @param pageSize The record count of one page.
     * @return The result bean of list as empty. (NotNull)
     */
    public ListResultBean<ENTITY> buildEmptyPagingSimply(int pageSize) {
        PagingResultBean<ENTITY> rb = newPagingResultBean();
        rb.setTableDbName(_tableDbName);
        rb.setPageSize(pageSize);
        rb.setCurrentPageNumber(1);
        return rb;
    }

    protected PagingResultBean<ENTITY> newPagingResultBean() {
        return new PagingResultBean<ENTITY>();
    }
}
