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
package org.dbflute.cbean.paging.numberlink.range;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.dbflute.cbean.paging.numberlink.PageNumberLink;
import org.dbflute.cbean.paging.numberlink.PageNumberLinkSetupper;

/**
 * The bean of page range.
 * @author jflute
 */
public class PageRangeBean implements Serializable {

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
    protected PageRangeOption _pageRangeOption;
    protected List<Integer> _cachedPageNumberList;

    // ===================================================================================
    //                                                                    Page Number List
    //                                                                    ================
    /**
     * Build the list of page number link.
     * <pre>
     * List&lt;PageNumberLink&gt; linkList = page.pageRange(op -&gt; {
     *     op.rangeSize(5);
     * }).<span style="color: #CC4747">buildPageNumberLinkList</span>((pageNumberElement, current) -&gt; {
     *     String href = buildPagingHref(pageNumberElement); <span style="color: #3F7E5E">// for paging navigation links</span>
     *     return new PageNumberLink().initialize(pageNumberElement, current, href);
     * });
     * </pre>
     * @param <LINK> The type of link.
     * @param manyArgLambda The setup interface of Page number link. (NotNull, Required LINK)
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
     * e.g. range-size=5, current-page=8 
     * List&lt;Integer&gt; numberList = page.pageRange(op -&gt; {
     *     op.rangeSize(5);
     * }).<span style="color: #CC4747">createPageNumberList()</span>;
     * 
     * <span style="color: #3F7E5E">//  8 / 23 pages (453 records)</span>
     * <span style="color: #3F7E5E">// previous</span> <span style="color: #CC4747">3 4 5 6 7 8 9 10 11 12 13</span> <span style="color: #3F7E5E">next</span>
     * </pre>
     * @return The list of page number. (NotNull)
     */
    public List<Integer> createPageNumberList() {
        assertPageRangeValid();
        if (_cachedPageNumberList != null) {
            return _cachedPageNumberList;
        }
        final int pageRangeSize = _pageRangeOption.getPageRangeSize();
        final int allPageCount = _allPageCount;
        final int currentPageNumber = _currentPageNumber;

        final List<Integer> resultList = new ArrayList<Integer>();
        for (int i = currentPageNumber - pageRangeSize; i < currentPageNumber; i++) {
            if (i < 1) {
                continue;
            }
            resultList.add(Integer.valueOf(i));
        }

        resultList.add(Integer.valueOf(currentPageNumber));

        final int endPageNumber = (currentPageNumber + pageRangeSize);
        for (int i = currentPageNumber + 1; i <= endPageNumber && i <= allPageCount; i++) {
            resultList.add(Integer.valueOf(i));
        }

        final boolean fillLimit = _pageRangeOption.isFillLimit();
        final int limitSize = (pageRangeSize * 2) + 1;
        if (fillLimit && !resultList.isEmpty() && resultList.size() < limitSize) {
            final Integer firstElements = (Integer) resultList.get(0);
            final Integer lastElements = (Integer) resultList.get(resultList.size() - 1);
            if (firstElements.intValue() > 1) {
                for (int i = firstElements.intValue() - 1; resultList.size() < limitSize && i > 0; i--) {
                    resultList.add(0, Integer.valueOf(i));
                }
            }
            for (int i = lastElements.intValue() + 1; resultList.size() < limitSize && i <= allPageCount; i++) {
                resultList.add(Integer.valueOf(i));
            }
        }
        _cachedPageNumberList = resultList;
        return _cachedPageNumberList;
    }

    // ===================================================================================
    //                                                                       Determination
    //                                                                       =============
    /**
     * Does the previous range exist? <br>
     * Using values are currentPageNumber and pageRangeSize and allPageCount.
     * <pre>
     * e.g. range-size=5, current-page=8 
     *  8 / 23 pages (453 records)
     * previous 3 4 5 6 7 8 9 10 11 12 13 next
     * 
     * <span style="color: #3F7E5E">// this method returns existence of</span> <span style="color: #CC4747">2</span>
     * </pre>
     * @return The determination, true or false.
     */
    public boolean existsPreviousRange() {
        assertPageRangeValid();
        final List<Integer> ls = createPageNumberList();
        if (ls.isEmpty()) {
            return false;
        }
        return ls.get(0) > 1;
    }

    /**
     * Does the next range exist? <br>
     * Using values are currentPageNumber and pageRangeSize and allPageCount.
     * <pre>
     * e.g. range-size=5, current-page=8 
     *  8 / 23 pages (453 records)
     * previous 3 4 5 6 7 8 9 10 11 12 13 next
     * 
     * <span style="color: #3F7E5E">// this method returns existence of</span> <span style="color: #CC4747">14</span>
     * </pre>
     * @return The determination, true or false.
     */
    public boolean existsNextRange() {
        assertPageRangeValid();
        final List<Integer> ls = createPageNumberList();
        if (ls.isEmpty()) {
            return false;
        }
        return ls.get(ls.size() - 1) < _allPageCount;
    }

    /**
     * Is existing previous page range?
     * <pre>
     * e.g. range-size=5, current-page=8 
     *  8 / 23 pages (453 records)
     * previous 3 4 5 6 7 8 9 10 11 12 13 next
     * 
     * <span style="color: #3F7E5E">// this method returns existence of</span> <span style="color: #CC4747">2</span>
     * </pre>
     * @return The determination, true or false.
     * @deprecated use existsPreviousRange()
     */
    public boolean isExistPrePageRange() {
        return existsPreviousRange();
    }

    /**
     * Is existing next page range?
     * <pre>
     * e.g. range-size=5, current-page=8 
     *  8 / 23 pages (453 records)
     * previous 3 4 5 6 7 8 9 10 11 12 13 next
     * 
     * <span style="color: #3F7E5E">// this method returns existence of</span> <span style="color: #CC4747">14</span>
     * </pre>
     * @return The determination, true or false.
     * @deprecated use existsNextRange()
     */
    public boolean isExistNextPageRange() {
        return existsNextRange();
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

    protected void assertPageRangeValid() {
        if (_pageRangeOption == null) {
            String msg = "The pageRangeOption should not be null. Please call setPageRangeOption().";
            throw new IllegalStateException(msg);
        }
        final int pageRangeSize = _pageRangeOption.getPageRangeSize();
        if (pageRangeSize == 0) {
            String msg = "The pageRangeSize should be greater than 1. But the value is zero.";
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
        sb.append(", pageRangeOption=").append(_pageRangeOption);
        sb.append("}");
        return sb.toString();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setCurrentPageNumber(int currentPageNumber) {
        this._currentPageNumber = currentPageNumber;
    }

    public void setAllPageCount(int allPageCount) {
        this._allPageCount = allPageCount;
    }

    public PageRangeOption getPageRangeOption() {
        return _pageRangeOption;
    }

    public void setPageRangeOption(PageRangeOption pageRangeOption) {
        this._pageRangeOption = pageRangeOption;
    }

    // -----------------------------------------------------
    //                                   Calculated Property
    //                                   -------------------
    /**
     * Get the value of previousRangeNearestPageNumber that is calculated. <br>
     * You should use this.existsPreviousRange() before calling this. (call only when true)
     * <pre>
     * e.g. range-size=5, current-page=8 
     *  8 / 23 pages (453 records)
     * previous 3 4 5 6 7 8 9 10 11 12 13 next
     * 
     * <span style="color: #3F7E5E">// this method returns</span> <span style="color: #CC4747">2</span>
     * </pre>
     * @return The number of previous range-nearest page.
     */
    public int getPreviousRangeNearestPageNumber() {
        if (!existsPreviousRange()) {
            String msg = "The previous page range should exist when you use previousRangeNearestPageNumber:";
            msg = msg + " currentPageNumber=" + _currentPageNumber + " allPageCount=" + _allPageCount;
            msg = msg + " pageRangeOption=" + _pageRangeOption;
            throw new IllegalStateException(msg);
        }
        return createPageNumberList().get(0) - 1;
    }

    /**
     * Get the value of nextRangeNearestPageNumber that is calculated. <br>
     * You should use this.existsNextRange() before calling this. (call only when true)
     * <pre>
     * e.g. range-size=5, current-page=8 
     *  8 / 23 pages (453 records)
     * previous 3 4 5 6 7 8 9 10 11 12 13 next
     * 
     * <span style="color: #3F7E5E">// this method returns</span> <span style="color: #CC4747">14</span>
     * </pre>
     * @return The number of next range-nearest page.
     */
    public int getNextRangeNearestPageNumber() {
        if (!existsNextRange()) {
            String msg = "The next page range should exist when you use nextRangeNearestPageNumber:";
            msg = msg + " currentPageNumber=" + _currentPageNumber + " allPageCount=" + _allPageCount;
            msg = msg + " pageRangeOption=" + _pageRangeOption;
            throw new IllegalStateException(msg);
        }
        final List<Integer> ls = createPageNumberList();
        return ls.get(ls.size() - 1) + 1;
    }

    /**
     * Get the value of preRangeNearestPageNumber that is calculated. <br>
     * You should use this.existsPreviousRange() before calling this. (call only when true)
     * <pre>
     * e.g. range-size=5, current-page=8 
     *  8 / 23 pages (453 records)
     * previous 3 4 5 6 7 8 9 10 11 12 13 next
     * 
     * <span style="color: #3F7E5E">// this method returns</span> <span style="color: #CC4747">2</span>
     * </pre>
     * @return The value of preRangeNearestPageNumber.
     * @deprecated use getPreviousRangeNearestPageNumber()
     */
    public int getPreRangeNearestPageNumber() {
        return getPreviousRangeNearestPageNumber();
    }
}
