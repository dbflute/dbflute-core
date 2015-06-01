/*
 * Copyright 2014-2015 the original author or authors.
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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.dbflute.cbean.paging.numberlink.group.PageGroupBean;
import org.dbflute.cbean.paging.numberlink.range.PageRangeBean;
import org.dbflute.unit.RuntimeTestCase;
import org.dbflute.util.DfReflectionUtil;

/**
 * @author jflute
 */
public class PagingResultBeanTest extends RuntimeTestCase {

    // ===================================================================================
    //                                                                      All Page Count
    //                                                                      ==============
    public void test_getAllPageCount_basic() {
        assertEquals(5, createTarget(4, 3, 20).getAllPageCount());
        assertEquals(5, createTarget(4, 3, 19).getAllPageCount());
        assertEquals(6, createTarget(4, 3, 21).getAllPageCount());
    }

    // ===================================================================================
    //                                                                         Page Number
    //                                                                         ===========
    public void test_getCurrentPageNumber_basic() {
        // ## Arrange ##
        PagingResultBean<String> page = createTarget(4, 3, 20);

        // ## Act ##
        int currentPageNumber = page.getCurrentPageNumber();

        // ## Assert ##
        assertEquals(3, currentPageNumber);
    }

    public void test_getPrePageNumber_basic() {
        // ## Arrange ##
        PagingResultBean<String> page = createTarget(4, 3, 20);

        // ## Act ##
        int prePageNumber = page.getPreviousPageNumber();

        // ## Assert ##
        assertEquals(2, prePageNumber);
    }

    public void test_getPrePageNumber_noExist() {
        // ## Arrange ##
        PagingResultBean<String> page = createTarget(4, 1, 20);

        // ## Act ##
        try {
            int prePageNumber = page.getPreviousPageNumber();

            // ## Assert ##
            fail("prePageNumber=" + prePageNumber);
        } catch (IllegalStateException e) {
            // OK
            log(e.getMessage());
        }
    }

    public void test_getNextPageNumber_basic() {
        // ## Arrange ##
        PagingResultBean<String> page = createTarget(4, 3, 20);

        // ## Act ##
        int nextPageNumber = page.getNextPageNumber();

        // ## Assert ##
        assertEquals(4, nextPageNumber);
    }

    public void test_getNextPageNumber_noExist() {
        // ## Arrange ##
        PagingResultBean<String> page = createTarget(4, 5, 20);

        // ## Act ##
        try {
            int nextPageNumber = page.getNextPageNumber();

            // ## Assert ##
            fail("nextPageNumber=" + nextPageNumber);
        } catch (IllegalStateException e) {
            // OK
            log(e.getMessage());
        }
    }

    // ===================================================================================
    //                                                                       Determination
    //                                                                       =============
    public void test_existsPreviousPage_basic() {
        assertTrue(createTarget(4, 5, 20).existsPreviousPage());
        assertTrue(createTarget(4, 4, 20).existsPreviousPage());
        assertTrue(createTarget(4, 3, 20).existsPreviousPage());
        assertTrue(createTarget(4, 2, 20).existsPreviousPage());
        assertFalse(createTarget(4, 1, 20).existsPreviousPage());
    }

    public void test_existsNextPage_basic() {
        assertTrue(createTarget(4, 1, 20).existsNextPage());
        assertTrue(createTarget(4, 2, 20).existsNextPage());
        assertTrue(createTarget(4, 3, 20).existsNextPage());
        assertTrue(createTarget(4, 4, 20).existsNextPage());
        assertFalse(createTarget(4, 5, 20).existsNextPage());
    }

    @SuppressWarnings("deprecation")
    public void test_isExistPrePage_basic() {
        assertTrue(createTarget(4, 5, 20).isExistPrePage());
        assertTrue(createTarget(4, 4, 20).isExistPrePage());
        assertTrue(createTarget(4, 3, 20).isExistPrePage());
        assertTrue(createTarget(4, 2, 20).isExistPrePage());
        assertFalse(createTarget(4, 1, 20).isExistPrePage());
    }

    @SuppressWarnings("deprecation")
    public void test_isExistNextPage_basic() {
        assertTrue(createTarget(4, 1, 20).isExistNextPage());
        assertTrue(createTarget(4, 2, 20).isExistNextPage());
        assertTrue(createTarget(4, 3, 20).isExistNextPage());
        assertTrue(createTarget(4, 4, 20).isExistNextPage());
        assertFalse(createTarget(4, 5, 20).isExistNextPage());
    }

    // ===================================================================================
    //                                                                          Page Group
    //                                                                          ==========
    public void test_pageGroup_createPageNumberList_firstGroup() {
        // ## Arrange ##
        PagingResultBean<String> page = createTarget(4, 3, 20);

        // ## Act ##
        List<Integer> ls = page.pageGroup(op -> op.groupSize(3)).createPageNumberList();

        // ## Assert ##
        assertEquals(3, ls.size());
        assertEquals(1, ls.get(0).intValue());
        assertEquals(2, ls.get(1).intValue());
        assertEquals(3, ls.get(2).intValue());
        assertEquals(3, page.pageGroup(op -> op.groupSize(3)).createPageNumberList().size()); // once more call
    }

    public void test_pageGroup_createPageNumberList_lastGroup() {
        // ## Arrange ##
        PagingResultBean<String> page = createTarget(4, 4, 20);

        // ## Act ##
        List<Integer> ls = page.pageGroup(op -> op.groupSize(3)).createPageNumberList();

        // ## Assert ##
        assertEquals(2, ls.size());
        assertEquals(4, ls.get(0).intValue());
        assertEquals(5, ls.get(1).intValue());
        assertEquals(2, page.pageGroup(op -> op.groupSize(3)).createPageNumberList().size()); // once more call
    }

    public void test_pageGroup_createPageNumberList_dynamic() {
        // ## Arrange ##
        String fieldName = "_cachedPageNumberList";
        Field cachedField = DfReflectionUtil.getAccessibleField(PageGroupBean.class, fieldName);
        cachedField.setAccessible(true);
        PagingResultBean<String> page = createTarget(4, 3, 20);
        assertNull(DfReflectionUtil.getValue(cachedField, page.pageGroup(op -> op.groupSize(3))));

        // ## Act ##
        List<Integer> ls = page.pageGroup(op -> op.groupSize(3)).createPageNumberList();

        // ## Assert ##
        assertEquals(3, ls.size());
        assertEquals(1, ls.get(0).intValue());
        assertEquals(2, ls.get(1).intValue());
        assertEquals(3, ls.get(2).intValue());
        PageGroupBean oneMoreCall = page.pageGroup(op -> op.groupSize(3));
        assertEquals(3, oneMoreCall.createPageNumberList().size());
        assertNotNull(DfReflectionUtil.getValue(cachedField, oneMoreCall));

        // ## Act ##
        ls = page.pageGroup(op -> op.groupSize(2)).createPageNumberList();

        // ## Assert ##
        assertEquals(2, ls.size());
        assertEquals(3, ls.get(0).intValue());
        assertEquals(4, ls.get(1).intValue());

        // ## Act ##
        page.setCurrentPageNumber(5);
        ls = page.pageGroup(op -> op.groupSize(2)).createPageNumberList();

        // ## Assert ##
        assertEquals(1, ls.size());
        assertEquals(5, ls.get(0).intValue());
    }

    public void test_pageGroup_getPreGroupNearestPageNumber_lastGroup() {
        // ## Arrange ##
        PagingResultBean<String> page = createTarget(4, 4, 20);

        // ## Act ##
        int pageNumber = page.pageGroup(op -> op.groupSize(3)).getPreviousGroupNearestPageNumber();

        // ## Assert ##
        assertEquals(3, pageNumber);
    }

    public void test_pageGroup_getPreGroupNearestPageNumber_noExist() {
        // ## Arrange ##
        PagingResultBean<String> page = createTarget(4, 3, 20);

        // ## Act ##
        try {
            int pageNumber = page.pageGroup(op -> op.groupSize(3)).getPreviousGroupNearestPageNumber();

            // ## Assert ##
            fail("pageNumber=" + pageNumber);
        } catch (IllegalStateException e) {
            // OK
            log(e.getMessage());
        }
    }

    public void test_pageGroup_getNextGroupNearestPageNumber_firstGroup() {
        // ## Arrange ##
        PagingResultBean<String> page = createTarget(4, 3, 20);

        // ## Act ##
        int pageNumber = page.pageGroup(op -> op.groupSize(3)).getNextGroupNearestPageNumber();

        // ## Assert ##
        assertEquals(4, pageNumber);
    }

    public void test_pageGroup_getNextGroupNearestPageNumber_noExist() {
        // ## Arrange ##
        PagingResultBean<String> page = createTarget(4, 4, 20);

        // ## Act ##
        try {
            int pageNumber = page.pageGroup(op -> op.groupSize(3)).getNextGroupNearestPageNumber();

            // ## Assert ##
            fail("pageNumber=" + pageNumber);
        } catch (IllegalStateException e) {
            // OK
            log(e.getMessage());
        }
    }

    // ===================================================================================
    //                                                                          Page Range
    //                                                                          ==========
    public void test_pageRange_createPageNumberList_nearFirstRange() {
        // ## Arrange ##
        PagingResultBean<String> page = createTarget(4, 3, 40);

        // ## Act ##
        List<Integer> ls = page.pageRange(op -> op.rangeSize(3)).createPageNumberList();

        // ## Assert ##
        assertEquals(6, ls.size());
        assertEquals(1, ls.get(0).intValue());
        assertEquals(2, ls.get(1).intValue());
        assertEquals(3, ls.get(2).intValue());
        assertEquals(4, ls.get(3).intValue());
        assertEquals(5, ls.get(4).intValue());
        assertEquals(6, ls.get(5).intValue());
        assertEquals(6, page.pageRange(op -> op.rangeSize(3)).createPageNumberList().size()); // once more call
    }

    public void test_pageRange_createPageNumberList_nearLastRange() {
        // ## Arrange ##
        PagingResultBean<String> page = createTarget(4, 8, 40);

        // ## Act ##
        List<Integer> ls = page.pageRange(op -> op.rangeSize(3)).createPageNumberList();

        // ## Assert ##
        assertEquals(6, ls.size());
        assertEquals(5, ls.get(0).intValue());
        assertEquals(6, ls.get(1).intValue());
        assertEquals(7, ls.get(2).intValue());
        assertEquals(8, ls.get(3).intValue());
        assertEquals(9, ls.get(4).intValue());
        assertEquals(10, ls.get(5).intValue());
        assertEquals(6, page.pageRange(op -> op.rangeSize(3)).createPageNumberList().size()); // once more call
    }

    public void test_pageRange_createPageNumberList_dynamic() {
        // ## Arrange ##
        String fieldName = "_cachedPageNumberList";
        Field cachedField = DfReflectionUtil.getAccessibleField(PageRangeBean.class, fieldName);
        cachedField.setAccessible(true);
        PagingResultBean<String> page = createTarget(4, 3, 40);
        assertNull(DfReflectionUtil.getValue(cachedField, page.pageRange(op -> op.rangeSize(3))));

        // ## Act ##
        List<Integer> ls = page.pageRange(op -> op.rangeSize(3)).createPageNumberList();

        // ## Assert ##
        assertEquals(6, ls.size());
        assertEquals(1, ls.get(0).intValue());
        assertEquals(2, ls.get(1).intValue());
        assertEquals(3, ls.get(2).intValue());
        assertEquals(4, ls.get(3).intValue());
        assertEquals(5, ls.get(4).intValue());
        assertEquals(6, ls.get(5).intValue());
        PageRangeBean oneMoreCall = page.pageRange(op -> op.rangeSize(3));
        assertEquals(6, oneMoreCall.createPageNumberList().size());
        assertNotNull(DfReflectionUtil.getValue(cachedField, oneMoreCall));

        // ## Act ##
        ls = page.pageRange(op -> op.rangeSize(2)).createPageNumberList();

        // ## Assert ##
        assertEquals(5, ls.size());
        assertEquals(1, ls.get(0).intValue());
        assertEquals(2, ls.get(1).intValue());
        assertEquals(3, ls.get(2).intValue());
        assertEquals(4, ls.get(3).intValue());
        assertEquals(5, ls.get(4).intValue());

        // ## Act ##
        page.setCurrentPageNumber(8);
        ls = page.pageRange(op -> op.rangeSize(2)).createPageNumberList();

        // ## Assert ##
        assertEquals(5, ls.size());
        assertEquals(6, ls.get(0).intValue());
        assertEquals(7, ls.get(1).intValue());
        assertEquals(8, ls.get(2).intValue());
        assertEquals(9, ls.get(3).intValue());
        assertEquals(10, ls.get(4).intValue());
    }

    public void test_pageRange_getPreRangeNearestPageNumber_nearLastRange() {
        // ## Arrange ##
        PagingResultBean<String> page = createTarget(4, 8, 40);

        // ## Act ##
        int pageNumber = page.pageRange(op -> op.rangeSize(3)).getPreviousRangeNearestPageNumber();

        // ## Assert ##
        assertEquals(4, pageNumber);
    }

    public void test_pageRange_getPreRangeNearestPageNumber_noExist() {
        // ## Arrange ##
        PagingResultBean<String> page = createTarget(4, 3, 40);

        // ## Act ##
        try {
            int pageNumber = page.pageRange(op -> op.rangeSize(3)).getPreviousRangeNearestPageNumber();

            // ## Assert ##
            fail("pageNumber=" + pageNumber);
        } catch (IllegalStateException e) {
            // OK
            log(e.getMessage());
        }
    }

    public void test_pageRange_getNextRangeNearestPageNumber_nearFirstRange() {
        // ## Arrange ##
        PagingResultBean<String> page = createTarget(4, 3, 40);

        // ## Act ##
        int pageNumber = page.pageRange(op -> op.rangeSize(3)).getNextRangeNearestPageNumber();

        // ## Assert ##
        assertEquals(7, pageNumber);
    }

    public void test_pageRange_getNextRangeNearestPageNumber_noExist() {
        // ## Arrange ##
        PagingResultBean<String> page = createTarget(4, 8, 40);

        // ## Act ##
        try {
            int pageNumber = page.pageRange(op -> op.rangeSize(3)).getNextRangeNearestPageNumber();

            // ## Assert ##
            fail("pageNumber=" + pageNumber);
        } catch (IllegalStateException e) {
            // OK
            log(e.getMessage());
        }
    }

    // ===================================================================================
    //                                                                       New Only Call
    //                                                                       =============
    public void test_getCurrentEndRecordNumber_basic() throws Exception {
        // ## Arrange ##
        PagingResultBean<String> bean = new PagingResultBean<String>();

        // ## Act ##
        // ## Assert ##
        assertEquals(1, bean.getCurrentStartRecordNumber());
        assertEquals(0, bean.getCurrentEndRecordNumber());

        bean.setPageSize(3);
        bean.setCurrentPageNumber(1);
        bean.setSelectedList(newArrayList("sea", "land"));

        assertEquals(1, bean.getCurrentStartRecordNumber());
        assertEquals(2, bean.getCurrentEndRecordNumber());
    }

    // ===================================================================================
    //                                                                         Test Helper
    //                                                                         ===========
    protected PagingResultBean<String> createTarget(int pageSize, int currentPageNumber, int allRecordCount) {
        PagingResultBean<String> page = new PagingResultBean<String>();
        page.setPageSize(pageSize);
        page.setCurrentPageNumber(currentPageNumber);
        page.setTableDbName("MEMBER");
        page.setAllRecordCount(allRecordCount);
        List<String> selectedList = new ArrayList<String>();
        for (int i = 0; i < allRecordCount; i++) {
            selectedList.add("No." + i);
        }
        page.setSelectedList(selectedList);
        return page;
    }
}
