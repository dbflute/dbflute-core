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
package org.dbflute.cbean.paging.numberlink.group;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.dbflute.cbean.paging.numberlink.PageNumberLink;
import org.dbflute.cbean.paging.numberlink.PageNumberLinkSetupper;

/**
 * The bean of page group.
 * @author jflute
 */
public class PageGroupBean implements Serializable {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The serial version UID for object serialization. (Default) */
    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected int _currentPageNumber;
    protected int _allPageCount;
    protected PageGroupOption _pageGroupOption;
    protected List<Integer> _cachedPageNumberList;

    // ===================================================================================
    //                                                                    Page Number List
    //                                                                    ================
    /**
     * Build the list of page number link.
     * <pre>
     * page.setPageGroupSize(10);
     * List&lt;PageNumberLink&gt; linkList = page.pageGroup().<span style="color: #CC4747">buildPageNumberLinkList</span>(new PageNumberLinkSetupper&lt;PageNumberLink&gt;() {
     *     public PageNumberLink setup(int pageNumberElement, boolean current) {
     *         String href = buildPagingHref(pageNumberElement); <span style="color: #3F7E5E">// for paging navigation links</span>
     *         return new PageNumberLink().initialize(pageNumberElement, current, href);
     *     }
     * });
     * </pre>
     * @param <LINK> The type of link.
     * @param manyArgLambda Page number link set-upper. (NotNull and Required LINK)
     * @return The list of Page number link. (NotNull)
     */
    public <LINK extends PageNumberLink> List<LINK> buildPageNumberLinkList(PageNumberLinkSetupper<LINK> manyArgLambda) {
        final List<Integer> pageNumberList = createPageNumberList();
        final List<LINK> pageNumberLinkList = new ArrayList<LINK>();
        for (Integer pageNumber : pageNumberList) {
            pageNumberLinkList.add(manyArgLambda.setup(pageNumber, pageNumber.equals(_currentPageNumber)));
        }
        return pageNumberLinkList;
    }

    /**
     * Create the list of page number.
     * <pre>
     * e.g. group-size=10, current-page=8 
     * page.<span style="color: #CC4747">setPageGroupSize</span>(10);
     * List&lt;Integer&gt; numberList = page.pageGroup().<span style="color: #CC4747">createPageNumberList()</span>;
     * 
     * <span style="color: #3F7E5E">//  8 / 23 pages (453 records)</span>
     * <span style="color: #3F7E5E">//</span> <span style="color: #CC4747">1 2 3 4 5 6 7 8 9 10</span> <span style="color: #3F7E5E">next</span>
     * </pre>
     * @return The list of page number. (NotNull)
     */
    public List<Integer> createPageNumberList() {
        assertPageGroupValid();
        if (_cachedPageNumberList != null) {
            return _cachedPageNumberList;
        }
        final int pageGroupSize = _pageGroupOption.getPageGroupSize();
        final int allPageCount = _allPageCount;
        final int currentPageGroupStartPageNumber = calculateStartPageNumber();
        if (!(currentPageGroupStartPageNumber > 0)) {
            String msg = "currentPageGroupStartPageNumber should be greater than 0. {> 0} But:";
            msg = msg + " currentPageGroupStartPageNumber=" + currentPageGroupStartPageNumber;
            throw new IllegalStateException(msg);
        }
        final int nextPageGroupStartPageNumber = currentPageGroupStartPageNumber + pageGroupSize;

        final List<Integer> resultList = new ArrayList<Integer>();
        for (int i = currentPageGroupStartPageNumber; i < nextPageGroupStartPageNumber && i <= allPageCount; i++) {
            resultList.add(Integer.valueOf(i));
        }
        _cachedPageNumberList = resultList;
        return _cachedPageNumberList;
    }

    /**
     * Calculate start page number.
     * @return Start page number.
     */
    protected int calculateStartPageNumber() {
        assertPageGroupValid();
        final int pageGroupSize = _pageGroupOption.getPageGroupSize();
        final int currentPageNumber = _currentPageNumber;

        int currentPageGroupNumber = (currentPageNumber / pageGroupSize);
        if ((currentPageNumber % pageGroupSize) == 0) {
            currentPageGroupNumber--;
        }
        final int currentPageGroupStartPageNumber = (pageGroupSize * currentPageGroupNumber) + 1;
        if (!(currentPageNumber >= currentPageGroupStartPageNumber)) {
            String msg = "currentPageNumber should be greater equal currentPageGroupStartPageNumber. But:";
            msg = msg + " currentPageNumber=" + currentPageNumber;
            msg = msg + " currentPageGroupStartPageNumber=" + currentPageGroupStartPageNumber;
            throw new IllegalStateException(msg);
        }
        return currentPageGroupStartPageNumber;
    }

    // ===================================================================================
    //                                                                       Determination
    //                                                                       =============
    /**
     * Does the previous group exist? <br>
     * Using values are currentPageNumber and pageGroupSize.
     * <pre>
     * e.g. group-size=10, current-page=12
     *  12 / 23 pages (453 records)
     * previous 11 12 13 14 15 16 17 18 19 20 next
     * 
     * <span style="color: #3F7E5E">// this method returns existence of</span> <span style="color: #CC4747">10</span>
     * </pre>
     * @return The determination, true or false.
     */
    public boolean existsPreviousGroup() {
        assertPageGroupValid();
        return (_currentPageNumber > _pageGroupOption.getPageGroupSize());
    }

    /**
     * Does the next group exist? <br>
     * Using values are currentPageNumber and pageGroupSize and allPageCount.
     * <pre>
     * e.g. group-size=10, current-page=12
     *  12 / 23 pages (453 records)
     * previous 11 12 13 14 15 16 17 18 19 20 next
     * 
     * <span style="color: #3F7E5E">// this method returns existence of</span> <span style="color: #CC4747">21</span>
     * </pre>
     * @return The determination, true or false.
     */
    public boolean existsNextGroup() {
        assertPageGroupValid();
        final int currentStartPageNumber = calculateStartPageNumber();
        if (!(currentStartPageNumber > 0)) {
            String msg = "currentStartPageNumber should be greater than 0. {> 0} But:";
            msg = msg + " currentStartPageNumber=" + currentStartPageNumber;
            throw new IllegalStateException(msg);
        }
        final int nextStartPageNumber = currentStartPageNumber + _pageGroupOption.getPageGroupSize();
        return (nextStartPageNumber <= _allPageCount);
    }

    /**
     * Is existing previous page-group?
     * Using values are currentPageNumber and pageGroupSize.
     * <pre>
     * e.g. group-size=10, current-page=12
     *  12 / 23 pages (453 records)
     * previous 11 12 13 14 15 16 17 18 19 20 next
     * 
     * <span style="color: #3F7E5E">// this method returns existence of</span> <span style="color: #CC4747">10</span>
     * </pre>
     * @return The determination, true or false.
     * @deprecated use existsPreviousGroup()
     */
    public boolean isExistPrePageGroup() {
        return existsPreviousGroup();
    }

    /**
     * Is existing next page-group?
     * Using values are currentPageNumber and pageGroupSize and allPageCount.
     * <pre>
     * e.g. group-size=10, current-page=12
     *  12 / 23 pages (453 records)
     * previous 11 12 13 14 15 16 17 18 19 20 next
     * 
     * <span style="color: #3F7E5E">// this method returns existence of</span> <span style="color: #CC4747">21</span>
     * </pre>
     * @return The determination, true or false.
     * @deprecated use existsNextGroup()
     */
    public boolean isExistNextPageGroup() {
        return existsNextGroup();
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected int[] convertListToIntArray(List<Integer> ls) {
        final int[] resultArray = new int[ls.size()];
        int arrayIndex = 0;
        for (int pageNumber : resultArray) {
            resultArray[arrayIndex] = pageNumber;
            arrayIndex++;
        }
        return resultArray;
    }

    protected void assertPageGroupValid() {
        if (_pageGroupOption == null) {
            String msg = "The pageGroupOption should not be null. Please call setPageGroupOption().";
            throw new IllegalStateException(msg);
        }
        if (_pageGroupOption.getPageGroupSize() == 0) {
            String msg = "The pageGroupSize should be greater than 1. But the value is zero.";
            msg = msg + " pageGroupSize=" + _pageGroupOption.getPageGroupSize();
            throw new IllegalStateException(msg);
        }
        if (_pageGroupOption.getPageGroupSize() == 1) {
            String msg = "The pageGroupSize should be greater than 1. But the value is one.";
            msg = msg + " pageGroupSize=" + _pageGroupOption.getPageGroupSize();
            throw new IllegalStateException(msg);
        }
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    /**
     * @return The view string of all attribute values. (NotNull)
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("currentPageNumber=").append(_currentPageNumber);
        sb.append(", allPageCount=").append(_allPageCount);
        sb.append(", pageGroupOption=").append(_pageGroupOption);
        sb.append("}");
        return sb.toString();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setCurrentPageNumber(int currentPageNumber) {
        _currentPageNumber = currentPageNumber;
    }

    public void setAllPageCount(int allPageCount) {
        _allPageCount = allPageCount;
    }

    public void setPageGroupOption(PageGroupOption pageGroupOption) {
        _pageGroupOption = pageGroupOption;
    }

    // -----------------------------------------------------
    //                                   Calculated Property
    //                                   -------------------
    /**
     * Get the value of preGroupNearestPageNumber that is calculated. <br>
     * You should use this.isExistPrePageGroup() before calling this. (call only when true)
     * <pre>
     * e.g. group-size=10, current-page=12
     *  12 / 23 pages (453 records)
     * previous 11 12 13 14 15 16 17 18 19 20 next
     * 
     * <span style="color: #3F7E5E">// this method returns</span> <span style="color: #CC4747">10</span>
     * </pre>
     * @return The value of preGroupNearestPageNumber.
     */
    public int getPreGroupNearestPageNumber() {
        if (!existsPreviousGroup()) {
            String msg = "The previous page range should exist when you use preGroupNearestPageNumber:";
            msg = msg + " currentPageNumber=" + _currentPageNumber + " allPageCount=" + _allPageCount;
            msg = msg + " pageGroupOption=" + _pageGroupOption;
            throw new IllegalStateException(msg);
        }
        return createPageNumberList().get(0) - 1;
    }

    /**
     * Get the value of nextGroupNearestPageNumber that is calculated. <br>
     * You should use this.isExistNextPageGroup() before calling this. (call only when true)
     * <pre>
     * e.g. group-size=10, current-page=12
     *  12 / 23 pages (453 records)
     * previous 11 12 13 14 15 16 17 18 19 20 next
     * 
     * <span style="color: #3F7E5E">// this method returns</span> <span style="color: #CC4747">21</span>
     * </pre>
     * @return The value of nextGroupNearestPageNumber.
     */
    public int getNextGroupNearestPageNumber() {
        if (!existsNextGroup()) {
            String msg = "The next page range should exist when you use nextGroupNearestPageNumber:";
            msg = msg + " currentPageNumber=" + _currentPageNumber + " allPageCount=" + _allPageCount;
            msg = msg + " pageGroupOption=" + _pageGroupOption;
            throw new IllegalStateException(msg);
        }
        final List<Integer> ls = createPageNumberList();
        return ls.get(ls.size() - 1) + 1;
    }
}
