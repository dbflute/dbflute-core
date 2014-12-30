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
package org.dbflute.bhv.readable.coins;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.dbflute.cbean.result.PagingResultBean;
import org.dbflute.unit.RuntimeTestCase;

/**
 * @author jflute
 */
public class VirtualUnionPagingBeanTest extends RuntimeTestCase {

    // ===================================================================================
    //                                                                         Paging Just
    //                                                                         ===========
    public void test_selectPage_paging_just_first() throws Exception {
        // ## Arrange ##
        // ## Act ##
        PagingResultBean<MappedBean> beanPage = prepareBean().paging(3, 1).selectPage();

        // ## Assert ##
        assertHasAnyElement(beanPage);
        for (MappedBean bean : beanPage) {
            log(bean);
        }
        assertEquals(12, beanPage.getAllRecordCount());
        assertEquals(4, beanPage.getAllPageCount());
        assertEquals(3, beanPage.getPageSize());
        assertEquals(1, beanPage.getCurrentPageNumber());
        assertEquals(3, beanPage.size());
        assertEquals("m1", beanPage.get(0).getName());
        assertEquals("m2", beanPage.get(1).getName());
        assertEquals("m3", beanPage.get(2).getName());
    }

    public void test_selectPage_paging_just_second() throws Exception {
        // ## Arrange ##
        // ## Act ##
        PagingResultBean<MappedBean> beanPage = prepareBean().paging(3, 2).selectPage();

        // ## Assert ##
        assertHasAnyElement(beanPage);
        for (MappedBean bean : beanPage) {
            log(bean);
        }
        assertEquals(12, beanPage.getAllRecordCount());
        assertEquals(4, beanPage.getAllPageCount());
        assertEquals(3, beanPage.getPageSize());
        assertEquals(2, beanPage.getCurrentPageNumber());
        assertEquals(3, beanPage.size());
        assertEquals("m4", beanPage.get(0).getName());
        assertEquals("m5", beanPage.get(1).getName());
        assertEquals("m6", beanPage.get(2).getName());
    }

    public void test_selectPage_paging_just_last() throws Exception {
        // ## Arrange ##
        // ## Act ##
        PagingResultBean<MappedBean> beanPage = prepareBean().paging(3, 4).selectPage();

        // ## Assert ##
        assertHasAnyElement(beanPage);
        for (MappedBean bean : beanPage) {
            log(bean);
        }
        assertEquals(12, beanPage.getAllRecordCount());
        assertEquals(4, beanPage.getAllPageCount());
        assertEquals(3, beanPage.getPageSize());
        assertEquals(4, beanPage.getCurrentPageNumber());
        assertEquals(3, beanPage.size());
        assertEquals("p10", beanPage.get(0).getName());
        assertEquals("p11", beanPage.get(1).getName());
        assertEquals("p12", beanPage.get(2).getName());
    }

    // ===================================================================================
    //                                                                     Paging Non-Just
    //                                                                     ===============
    public void test_selectPage_paging_nonJust_first() throws Exception {
        // ## Arrange ##
        // ## Act ##
        PagingResultBean<MappedBean> beanPage = prepareBean().paging(5, 1).selectPage();

        // ## Assert ##
        assertHasAnyElement(beanPage);
        for (MappedBean bean : beanPage) {
            log(bean);
        }
        assertEquals(12, beanPage.getAllRecordCount());
        assertEquals(3, beanPage.getAllPageCount());
        assertEquals(5, beanPage.getPageSize());
        assertEquals(1, beanPage.getCurrentPageNumber());
        assertEquals(5, beanPage.size());
        assertEquals("m1", beanPage.get(0).getName());
        assertEquals("m2", beanPage.get(1).getName());
        assertEquals("m3", beanPage.get(2).getName());
        assertEquals("m4", beanPage.get(3).getName());
        assertEquals("m5", beanPage.get(4).getName());
    }

    public void test_selectPage_paging_nonJust_second() throws Exception {
        // ## Arrange ##
        // ## Act ##
        PagingResultBean<MappedBean> beanPage = prepareBean().paging(5, 2).selectPage();

        // ## Assert ##
        assertHasAnyElement(beanPage);
        for (MappedBean bean : beanPage) {
            log(bean);
        }
        assertEquals(12, beanPage.getAllRecordCount());
        assertEquals(3, beanPage.getAllPageCount());
        assertEquals(5, beanPage.getPageSize());
        assertEquals(2, beanPage.getCurrentPageNumber());
        assertEquals(5, beanPage.size());
        assertEquals("m6", beanPage.get(0).getName());
        assertEquals("p7", beanPage.get(1).getName());
        assertEquals("p8", beanPage.get(2).getName());
        assertEquals("p9", beanPage.get(3).getName());
        assertEquals("p10", beanPage.get(4).getName());
    }

    public void test_selectPage_paging_nonJust_third() throws Exception {
        // ## Arrange ##
        // ## Act ##
        PagingResultBean<MappedBean> beanPage = prepareBean().paging(5, 3).selectPage();

        // ## Assert ##
        assertHasAnyElement(beanPage);
        for (MappedBean bean : beanPage) {
            log(bean);
        }
        assertEquals(12, beanPage.getAllRecordCount());
        assertEquals(3, beanPage.getAllPageCount());
        assertEquals(5, beanPage.getPageSize());
        assertEquals(3, beanPage.getCurrentPageNumber());
        assertEquals(2, beanPage.size());
        assertEquals("p11", beanPage.get(0).getName());
        assertEquals("p12", beanPage.get(1).getName());
    }

    // ===================================================================================
    //                                                                            Big Page
    //                                                                            ========
    public void test_selectPage_paging_bigPage_containsFirst() throws Exception {
        // ## Arrange ##
        // ## Act ##
        PagingResultBean<MappedBean> beanPage = prepareBean().paging(10, 1).selectPage();

        // ## Assert ##
        assertHasAnyElement(beanPage);
        for (MappedBean bean : beanPage) {
            log(bean);
        }
        assertEquals(12, beanPage.getAllRecordCount());
        assertEquals(2, beanPage.getAllPageCount());
        assertEquals(10, beanPage.getPageSize());
        assertEquals(1, beanPage.getCurrentPageNumber());
        assertEquals(10, beanPage.size());
        assertEquals("m1", beanPage.get(0).getName());
        assertEquals("m2", beanPage.get(1).getName());
        assertEquals("m3", beanPage.get(2).getName());
        assertEquals("m4", beanPage.get(3).getName());
        assertEquals("m5", beanPage.get(4).getName());
        assertEquals("m6", beanPage.get(5).getName());
        assertEquals("p7", beanPage.get(6).getName());
        assertEquals("p8", beanPage.get(7).getName());
        assertEquals("p9", beanPage.get(8).getName());
        assertEquals("p10", beanPage.get(9).getName());
    }

    public void test_selectPage_paging_bigPage_containsAll() throws Exception {
        // ## Arrange ##
        // ## Act ##
        PagingResultBean<MappedBean> beanPage = prepareBean().paging(20, 1).selectPage();

        // ## Assert ##
        assertHasAnyElement(beanPage);
        for (MappedBean bean : beanPage) {
            log(bean);
        }
        assertEquals(12, beanPage.getAllRecordCount());
        assertEquals(1, beanPage.getAllPageCount());
        assertEquals(20, beanPage.getPageSize());
        assertEquals(1, beanPage.getCurrentPageNumber());
        assertEquals(12, beanPage.size());
        assertEquals("m1", beanPage.get(0).getName());
        assertEquals("m2", beanPage.get(1).getName());
        assertEquals("m3", beanPage.get(2).getName());
        assertEquals("m4", beanPage.get(3).getName());
        assertEquals("m5", beanPage.get(4).getName());
        assertEquals("m6", beanPage.get(5).getName());
        assertEquals("p7", beanPage.get(6).getName());
        assertEquals("p8", beanPage.get(7).getName());
        assertEquals("p9", beanPage.get(8).getName());
        assertEquals("p10", beanPage.get(9).getName());
        assertEquals("p11", beanPage.get(10).getName());
        assertEquals("p12", beanPage.get(11).getName());
    }

    public void test_selectPage_paging_bigPage_overNumber_retry() throws Exception {
        // ## Arrange ##
        // ## Act ##
        PagingResultBean<MappedBean> beanPage = prepareBean().paging(3, 99).selectPage();

        // ## Assert ##
        assertHasAnyElement(beanPage);
        assertEquals(12, beanPage.getAllRecordCount());
        assertEquals(4, beanPage.getAllPageCount());
        assertEquals(3, beanPage.getPageSize());
        assertEquals(4, beanPage.getCurrentPageNumber());
        assertEquals(3, beanPage.size());
        assertEquals("p10", beanPage.get(0).getName());
        assertEquals("p11", beanPage.get(1).getName());
        assertEquals("p12", beanPage.get(2).getName());
    }

    // ===================================================================================
    //                                                                             No Data
    //                                                                             =======
    public void test_selectPage_paging_noData() throws Exception {
        // ## Arrange ##
        // ## Act ##
        PagingResultBean<MappedBean> beanPage = prepareNoDataBean().paging(3, 1).selectPage();

        // ## Assert ##
        assertHasZeroElement(beanPage);
        assertEquals(0, beanPage.getAllRecordCount());
        assertEquals(1, beanPage.getAllPageCount());
        assertEquals(3, beanPage.getPageSize());
        assertEquals(1, beanPage.getCurrentPageNumber());
        assertEquals(0, beanPage.size());
    }

    public void test_selectPage_paging_noDataMiddle() throws Exception {
        // ## Arrange ##
        // ## Act ##
        PagingResultBean<MappedBean> beanPage = prepareNoDataMiddleBean().paging(5, 2).selectPage();

        // ## Assert ##
        assertHasAnyElement(beanPage);
        for (MappedBean bean : beanPage) {
            log(bean);
        }
        assertEquals(11, beanPage.getAllRecordCount());
        assertEquals(3, beanPage.getAllPageCount());
        assertEquals(5, beanPage.getPageSize());
        assertEquals(2, beanPage.getCurrentPageNumber());
        assertEquals(5, beanPage.size());
        assertEquals("m6", beanPage.get(0).getName());
        assertEquals("pr13", beanPage.get(1).getName());
        assertEquals("pr14", beanPage.get(2).getName());
        assertEquals("pr15", beanPage.get(3).getName());
        assertEquals("pr16", beanPage.get(4).getName());
    }

    public void test_selectPage_paging_shortData() throws Exception {
        // ## Arrange ##
        // ## Act ##
        PagingResultBean<MappedBean> beanPage = prepareShortBean().paging(3, 2).selectPage();

        // ## Assert ##
        assertHasAnyElement(beanPage);
        assertEquals(12, beanPage.getAllRecordCount());
        assertEquals(4, beanPage.getAllPageCount());
        assertEquals(3, beanPage.getPageSize());
        assertEquals(2, beanPage.getCurrentPageNumber());
        assertEquals(3, beanPage.size());
        assertEquals("m4", beanPage.get(0).getName());
        assertEquals("p7", beanPage.get(1).getName());
        assertEquals("p8", beanPage.get(2).getName());
    }

    // ===================================================================================
    //                                                                         Test Helper
    //                                                                         ===========
    protected VirtualUnionPagingBean<Integer, MappedBean> prepareBean() {
        return new VirtualUnionPagingBean<Integer, MappedBean>("MEMBER", () -> {
            return Arrays.asList(1, 2, 3, 4, 5, 6);
        }, idList -> {
            return idList.stream().map(id -> new MappedBean(id, "m" + id)).collect(Collectors.toList());
        }).unionAll("PURCHASE", () -> {
            return Arrays.asList(7, 8, 9, 10, 11, 12);
        }, idList -> {
            return idList.stream().map(id -> new MappedBean(id, "p" + id)).collect(Collectors.toList());
        });
    }

    protected VirtualUnionPagingBean<Integer, MappedBean> prepareNoDataBean() {
        return new VirtualUnionPagingBean<Integer, MappedBean>("MEMBER", () -> {
            return Arrays.asList();
        }, idList -> {
            return idList.stream().map(id -> new MappedBean(id, "m" + id)).collect(Collectors.toList());
        }).unionAll("PURCHASE", () -> {
            return Arrays.asList();
        }, idList -> {
            return idList.stream().map(id -> new MappedBean(id, "p" + id)).collect(Collectors.toList());
        });
    }

    protected VirtualUnionPagingBean<Integer, MappedBean> prepareNoDataMiddleBean() {
        return new VirtualUnionPagingBean<Integer, MappedBean>("MEMBER", () -> {
            return Arrays.asList(1, 2, 3, 4, 5, 6);
        }, idList -> {
            return idList.stream().map(id -> new MappedBean(id, "m" + id)).collect(Collectors.toList());
        }).unionAll("PURCHASE", () -> {
            return Arrays.asList();
        }, idList -> {
            return idList.stream().map(id -> new MappedBean(id, "pc" + id)).collect(Collectors.toList());
        }).unionAll("PRODUCT", () -> {
            return Arrays.asList(13, 14, 15, 16, 17);
        }, idList -> {
            return idList.stream().map(id -> new MappedBean(id, "pr" + id)).collect(Collectors.toList());
        });
    }

    protected VirtualUnionPagingBean<Integer, MappedBean> prepareShortBean() {
        return new VirtualUnionPagingBean<Integer, MappedBean>("MEMBER", () -> {
            return Arrays.asList(1, 2, 3, 4, 5, 6);
        }, idList -> {
            return idList.stream().filter(id -> id <= 4).map(id -> new MappedBean(id, "m" + id)).collect(Collectors.toList());
        }).unionAll("PURCHASE", () -> {
            return Arrays.asList(7, 8, 9, 10, 11, 12);
        }, idList -> {
            return idList.stream().map(id -> new MappedBean(id, "p" + id)).collect(Collectors.toList());
        });
    }

    protected static class MappedBean {
        protected Integer _id;
        protected String _name;

        @Override
        public String toString() {
            return "{" + _id + ", " + _name + "}";
        }

        public MappedBean(Integer id, String name) {
            _id = id;
            _name = name;
        }

        public Integer getId() {
            return _id;
        }

        public String getName() {
            return _name;
        }
    }
}
