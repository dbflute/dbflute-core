/*
 * Copyright 2014-2022 the original author or authors.
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

import org.dbflute.cbean.paging.numberlink.PageNumberLinkOptionCall;
import org.dbflute.cbean.paging.numberlink.group.PageGroupBean;
import org.dbflute.cbean.paging.numberlink.group.PageGroupOption;
import org.dbflute.cbean.paging.numberlink.range.PageRangeBean;
import org.dbflute.cbean.paging.numberlink.range.PageRangeOption;
import org.dbflute.cbean.result.mapping.EntityDtoMapper;

/**
 * The result bean of paging.
 * @param <ENTITY> The type of entity for the element of selected list.
 * @author jflute
 */
public class PagingResultBean<ENTITY> extends ListResultBean<ENTITY> {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The serial version UID for object serialization. (Default) */
    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                       Page Basic Info
    //                                       ---------------
    /** The value of page size that means record count in one page. */
    protected int _pageSize;

    /** The value of current page number. */
    protected int _currentPageNumber;

    // ===================================================================================
    //                                                                       Determination
    //                                                                       =============
    /**
     * Does the previous page exist? <br>
     * Using values are currentPageNumber.
     * <pre>
     * e.g. range-size=5, current-page=8 
     *  8 / 23 pages (453 records)
     * <span style="color: #CC4747">previous</span> 3 4 5 6 <span style="color: #CC4747">7</span> 8 9 10 11 12 13 next
     * </pre>
     * @return The determination, true or false.
     */
    public boolean existsPreviousPage() {
        return (_allRecordCount > 0 && _currentPageNumber > 1);
    }

    /**
     * Does the next page exist? <br>
     * Using values are currentPageNumber and allPageCount.
     * <pre>
     * e.g. range-size=5, current-page=8 
     *  8 / 23 pages (453 records)
     * previous 3 4 5 6 7 8 <span style="color: #CC4747">9</span> 10 11 12 13 <span style="color: #CC4747">next</span>
     * </pre>
     * @return The determination, true or false.
     */
    public boolean existsNextPage() {
        return (_allRecordCount > 0 && _currentPageNumber < getAllPageCount());
    }

    /**
     * Is existing previous page?
     * Using values are currentPageNumber.
     * <pre>
     * e.g. range-size=5, current-page=8 
     *  8 / 23 pages (453 records)
     * <span style="color: #CC4747">previous</span> 3 4 5 6 <span style="color: #CC4747">7</span> 8 9 10 11 12 13 next
     * </pre>
     * @return The determination, true or false.
     * @deprecated use existsPreviousPage()
     */
    public boolean isExistPrePage() {
        return existsPreviousPage();
    }

    /**
     * Is existing next page?
     * Using values are currentPageNumber and allPageCount.
     * <pre>
     * e.g. range-size=5, current-page=8 
     *  8 / 23 pages (453 records)
     * previous 3 4 5 6 7 8 <span style="color: #CC4747">9</span> 10 11 12 13 <span style="color: #CC4747">next</span>
     * </pre>
     * @return The determination, true or false.
     * @deprecated use existsNextPage()
     */
    public boolean isExistNextPage() {
        return existsNextPage();
    }

    // ===================================================================================
    //                                                                    Page Group/Range
    //                                                                    ================
    // -----------------------------------------------------
    //                                            Page Group
    //                                            ----------
    /**
     * Get the value of pageGroupBean.
     * <pre>
     * e.g. group-size=10, current-page=8
     * PageGroupBean pageGroup = page.<span style="color: #CC4747">pageGroup</span>(op -&gt; op.groupSize(3));
     * List&lt;Integer&gt; numberList = pageGroup.createPageNumberList();
     *
     * <span style="color: #3F7E5E">//  8 / 23 pages (453 records)</span>
     * <span style="color: #3F7E5E">// previous</span> <span style="color: #CC4747">1 2 3 4 5 6 7 8 9 10</span> <span style="color: #3F7E5E">next</span>
     * </pre>
     * @param opLambda The callback for setting of page-group option. (NotNull)
     * @return The bean of page group. (NotNull)
     */
    public PageGroupBean pageGroup(PageNumberLinkOptionCall<PageGroupOption> opLambda) {
        assertPageGroupOptionCall(opLambda);
        return createPageGroupBean(createPageGroupOption(opLambda));
    }

    protected void assertPageGroupOptionCall(PageNumberLinkOptionCall<PageGroupOption> opLambda) {
        if (opLambda == null) {
            throw new IllegalArgumentException("The argument 'opLambda' should not be null.");
        }
    }

    protected PageGroupOption createPageGroupOption(PageNumberLinkOptionCall<PageGroupOption> opLambda) {
        final PageGroupOption op = newPageGroupOption();
        opLambda.callback(op);
        return op;
    }

    protected PageGroupOption newPageGroupOption() {
        return new PageGroupOption();
    }

    protected PageGroupBean createPageGroupBean(PageGroupOption op) {
        final PageGroupBean bean = newPageGroupBean();
        bean.setPageGroupOption(op);
        bean.setCurrentPageNumber(getCurrentPageNumber());
        bean.setAllPageCount(getAllPageCount());
        return bean;
    }

    protected PageGroupBean newPageGroupBean() {
        return new PageGroupBean();
    }

    // -----------------------------------------------------
    //                                            Page Range
    //                                            ----------
    /**
     * Get the value of pageRangeBean.
     * <pre>
     * e.g. group-size=10, current-page=8
     * PageRangeBean pageRange = page.<span style="color: #CC4747">pageRange</span>(op -&gt; op.rangeSize(3));
     * List&lt;Integer&gt; numberList = pageRange.createPageNumberList();
     *
     * <span style="color: #3F7E5E">//  8 / 23 pages (453 records)</span>
     * <span style="color: #3F7E5E">// previous</span> <span style="color: #CC4747">1 2 3 4 5 6 7 8 9 10</span> <span style="color: #3F7E5E">next</span>
     * </pre>
     * @param opLambda The callback for setting of page-range option. (NotNull)
     * @return The bean of page range. (NotNull)
     */
    public PageRangeBean pageRange(PageNumberLinkOptionCall<PageRangeOption> opLambda) {
        assertPageRangeOptionCall(opLambda);
        return createPageRangeBean(createPageRangeOption(opLambda));
    }

    protected void assertPageRangeOptionCall(PageNumberLinkOptionCall<PageRangeOption> opLambda) {
        if (opLambda == null) {
            throw new IllegalArgumentException("The argument 'opLambda' should not be null.");
        }
    }

    protected PageRangeOption createPageRangeOption(PageNumberLinkOptionCall<PageRangeOption> opLambda) {
        final PageRangeOption op = newPageRangeOption();
        opLambda.callback(op);
        return op;
    }

    protected PageRangeOption newPageRangeOption() {
        return new PageRangeOption();
    }

    protected PageRangeBean createPageRangeBean(PageRangeOption op) {
        final PageRangeBean bean = newPageRangeBean();
        bean.setPageRangeOption(op);
        bean.setCurrentPageNumber(getCurrentPageNumber());
        bean.setAllPageCount(getAllPageCount());
        return bean;
    }

    protected PageRangeBean newPageRangeBean() {
        return new PageRangeBean();
    }

    // ===================================================================================
    //                                                                 Calculate(Internal)
    //                                                                 ===================
    /**
     * Calculate all page count.
     * @param allRecordCount The record count of all records (without paging).
     * @param pageSize The record count of one page.
     * @return The count of all pages.
     */
    protected int calculateAllPageCount(int allRecordCount, int pageSize) {
        if (allRecordCount == 0) {
            return 1;
        }
        int pageCountBase = (allRecordCount / pageSize);
        if (allRecordCount % pageSize > 0) {
            pageCountBase++;
        }
        return pageCountBase;
    }

    protected int calculateCurrentStartRecordNumber(int currentPageNumber, int pageSize) {
        return ((currentPageNumber - 1) * pageSize) + 1; // 1 origin even if no records
    }

    protected int calculateCurrentEndRecordNumber(int currentPageNumber, int pageSize) {
        final int listSize = _selectedList != null ? _selectedList.size() : 1; // 1 origin even if no records
        return calculateCurrentStartRecordNumber(currentPageNumber, pageSize) + listSize - 1;
    }

    // ===================================================================================
    //                                                                             Mapping
    //                                                                             =======
    @SuppressWarnings("deprecation")
    @Override
    public <DTO> PagingResultBean<DTO> mappingList(EntityDtoMapper<ENTITY, DTO> entityLambda) {
        final ListResultBean<DTO> ls = super.mappingList(entityLambda);
        final PagingResultBean<DTO> mappingList = new PagingResultBean<DTO>();
        mappingList.setSelectedList(ls.getSelectedList());
        mappingList.setTableDbName(getTableDbName());
        mappingList.setAllRecordCount(getAllRecordCount());
        mappingList.setOrderByClause(getOrderByClause());
        mappingList.setPageSize(getPageSize());
        mappingList.setCurrentPageNumber(getCurrentPageNumber());
        return mappingList;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    /**
     * @return Hash-code from primary-keys.
     */
    @Override
    public int hashCode() {
        int result = 17;
        result = (31 * result) + _pageSize;
        result = (31 * result) + _currentPageNumber;
        if (_selectedList != null) {
            result = (31 * result) + _selectedList.hashCode();
        }
        return result;
    }

    /**
     * @param other Other entity. (NullAllowed)
     * @return Comparing result. If other is null, returns false.
     */
    @Override
    public boolean equals(Object other) {
        boolean equals = super.equals(other);
        if (!equals) {
            return false;
        }
        if (!(other instanceof PagingResultBean<?>)) {
            return false;
        }
        PagingResultBean<?> otherBean = (PagingResultBean<?>) other;
        if (_pageSize != otherBean.getPageSize()) {
            return false;
        }
        if (_currentPageNumber != otherBean.getCurrentPageNumber()) {
            return false;
        }
        return true;
    }

    /**
     * @return The view string of all attribute values. (NotNull)
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        buildPagingDisp(sb);
        sb.append(":selectedList=").append(getSelectedList());
        return sb.toString();
    }

    @Override
    protected void buildRichStringHeader(StringBuilder sb) {
        buildPagingDisp(sb);
    }

    protected void buildPagingDisp(StringBuilder sb) {
        sb.append("{").append(getCurrentPageNumber()).append("/").append(getAllPageCount());
        sb.append(" of ").append(getAllRecordCount());
        sb.append(" ").append(existsPreviousPage()).append("/").append(existsNextPage());
        sb.append(" list=").append(size()).append(" page=").append(getPageSize());
        sb.append("}");
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    /**
     * Get the value of allRecordCount when no paging. <br>
     * This is not same as size() basically.
     * <pre>
     * e.g. range-size=5, current-page=8 
     *  8 / 23 pages (<span style="color: #CC4747">453</span> records)
     * previous 3 4 5 6 7 8 9 10 11 12 13 next
     * </pre>
     * @return The count of all records without paging.
     */
    @Override
    public int getAllRecordCount() { // override for java-doc
        return super.getAllRecordCount();
    }

    /**
     * Set the value of allRecordCount when no paging with initializing cached beans.
     * @param allRecordCount The count of all records without paging.
     */
    @Override
    public void setAllRecordCount(int allRecordCount) {
        super.setAllRecordCount(allRecordCount);
    }

    /**
     * Get the value of pageSize that means record size in a page.
     * @return The size of one page.
     */
    public int getPageSize() {
        return _pageSize;
    }

    /**
     * Set the value of pageSize with initializing cached beans.
     * @param pageSize The size of one page.
     */
    public void setPageSize(int pageSize) {
        _pageSize = pageSize;
    }

    /**
     * Get the value of currentPageNumber.
     * <pre>
     * e.g. range-size=5, current-page=8 
     *  <span style="color: #CC4747">8</span> / 23 pages (453 records)
     * previous 3 4 5 6 7 8 9 10 11 12 13 next
     * </pre>
     * @return The number of current page.
     */
    public int getCurrentPageNumber() {
        return _currentPageNumber;
    }

    /**
     * Set the value of currentPageNumber with initializing cached beans.
     * @param currentPageNumber The number of current page.
     */
    public void setCurrentPageNumber(int currentPageNumber) {
        _currentPageNumber = currentPageNumber;
    }

    // -----------------------------------------------------
    //                                   Calculated Property
    //                                   -------------------
    /**
     * Get the value of allPageCount that is calculated.
     * <pre>
     * e.g. range-size=5, current-page=8 
     *  8 / <span style="color: #CC4747">23</span> pages (453 records)
     * previous 3 4 5 6 7 8 9 10 11 12 13 next
     * </pre>
     * @return The count of all pages.
     */
    public int getAllPageCount() {
        return calculateAllPageCount(_allRecordCount, _pageSize);
    }

    /**
     * Get the value of previousPageNumber that is calculated. <br>
     * You should use this.existsPreviousPage() before calling this. (call only when true)
     * <pre>
     * e.g. range-size=5, current-page=8 
     *  8 / 23 pages (453 records)
     * <span style="color: #CC4747">previous</span> 3 4 5 6 <span style="color: #CC4747">7</span> 8 9 10 11 12 13 next
     * </pre>
     * @return The number of previous page.
     */
    public int getPreviousPageNumber() {
        if (!existsPreviousPage()) {
            String msg = "The previous page should exist when you use previousPageNumber:";
            msg = msg + " currentPageNumber=" + _currentPageNumber;
            throw new IllegalStateException(msg);
        }
        return _currentPageNumber - 1;
    }

    /**
     * Get the value of nextPageNumber that is calculated. <br>
     * You should use this.isExistNextPage() before calling this. (call only when true)
     * <pre>
     * e.g. range-size=5, current-page=8 
     *  8 / 23 pages (453 records)
     * previous 3 4 5 6 7 8 <span style="color: #CC4747">9</span> 10 11 12 13 <span style="color: #CC4747">next</span>
     * </pre>
     * @return The number of next page.
     */
    public int getNextPageNumber() {
        if (!existsNextPage()) {
            String msg = "The next page should exist when you use nextPageNumber:";
            msg = msg + " currentPageNumber=" + _currentPageNumber;
            throw new IllegalStateException(msg);
        }
        return _currentPageNumber + 1;
    }

    /**
     * Get the value of prePageNumber that is calculated. <br>
     * You should use this.isExistPrePage() before calling this. (call only when true)
     * <pre>
     * e.g. range-size=5, current-page=8 
     *  8 / 23 pages (453 records)
     * <span style="color: #CC4747">previous</span> 3 4 5 6 <span style="color: #CC4747">7</span> 8 9 10 11 12 13 next
     * </pre>
     * @return The value of prePageNumber.
     * @deprecated use getPreviousPageNumber()
     */
    public int getPrePageNumber() {
        return getPreviousPageNumber();
    }

    /**
     * Get the value of currentStartRecordNumber that is calculated.
     * @return The value of currentStartRecordNumber.
     */
    public int getCurrentStartRecordNumber() {
        return calculateCurrentStartRecordNumber(_currentPageNumber, _pageSize);
    }

    /**
     * Get the value of currentEndRecordNumber that is calculated.
     * @return The value of currentEndRecordNumber.
     */
    public int getCurrentEndRecordNumber() {
        return calculateCurrentEndRecordNumber(_currentPageNumber, _pageSize);
    }
}
