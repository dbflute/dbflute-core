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
package org.dbflute.cbean.paging;

import java.util.ArrayList;
import java.util.List;

import org.dbflute.cbean.result.PagingResultBean;
import org.dbflute.mock.MockColumnInfo;
import org.dbflute.outsidesql.paging.SimplePagingBean;
import org.dbflute.unit.RuntimeTestCase;

/**
 * @author jflute
 * @since 0.9.5 (2009/05/27 Wednesday)
 */
@SuppressWarnings("deprecation")
public class PagingInvokerTest extends RuntimeTestCase {

    // ===================================================================================
    //                                                                      invokePaging()
    //                                                                      ==============
    public void test_invokePaging_emtpy() {
        // ## Arrange ##
        final List<String> selectedList = new ArrayList<String>();
        final SimplePagingBean pagingBean = new SimplePagingBean();
        pagingBean.getSqlClause().registerOrderBy("aaa", true, new MockColumnInfo());
        pagingBean.fetchFirst(20);
        PagingInvoker<String> tgt = createTarget();

        // ## Act ##
        final List<String> markList = new ArrayList<String>();
        PagingResultBean<String> rb = tgt.invokePaging(new PagingHandler<String>() {
            public PagingBean getPagingBean() {
                return pagingBean;
            }

            public int count() {
                markList.add("count");
                return 0;
            }

            public List<String> paging() {
                markList.add("paging");
                return selectedList;
            }
        });

        // ## Assert ##
        assertEquals(0, rb.size());
        assertEquals(0, rb.getAllRecordCount());
        assertEquals(1, rb.getAllPageCount());
        assertEquals(1, rb.getOrderByClause().getOrderByList().size());
        assertEquals("count", markList.get(0));
        assertEquals(1, markList.size());
    }

    public void test_invokePaging_emtpy_countLater() {
        // ## Arrange ##
        final List<String> selectedList = new ArrayList<String>();
        final SimplePagingBean pagingBean = new SimplePagingBean() {
            @Override
            public boolean canPagingCountLater() {
                return true;
            }
        };
        pagingBean.getSqlClause().registerOrderBy("aaa", true, new MockColumnInfo());
        pagingBean.fetchFirst(20);
        PagingInvoker<String> tgt = createTarget();

        // ## Act ##
        final List<String> markList = new ArrayList<String>();
        PagingResultBean<String> rb = tgt.invokePaging(new PagingHandler<String>() {
            public PagingBean getPagingBean() {
                return pagingBean;
            }

            public int count() {
                markList.add("count");
                return 0;
            }

            public List<String> paging() {
                markList.add("paging");
                return selectedList;
            }
        });

        // ## Assert ##
        assertEquals(0, rb.size());
        assertEquals(0, rb.getAllRecordCount());
        assertEquals(1, rb.getAllPageCount());
        assertEquals(1, rb.getOrderByClause().getOrderByList().size());
        assertEquals("paging", markList.get(0));
        assertEquals(1, markList.size());
    }

    public void test_invokePaging_onePage() {
        // ## Arrange ##
        final List<String> selectedList = new ArrayList<String>();
        fillList(selectedList, 19);
        final SimplePagingBean pagingBean = new SimplePagingBean();
        pagingBean.getSqlClause().registerOrderBy("aaa", true, new MockColumnInfo());
        pagingBean.fetchFirst(20);
        PagingInvoker<String> tgt = createTarget();

        // ## Act ##
        final List<String> markList = new ArrayList<String>();
        PagingResultBean<String> rb = tgt.invokePaging(new PagingHandler<String>() {
            public PagingBean getPagingBean() {
                return pagingBean;
            }

            public int count() {
                markList.add("count");
                return 19;
            }

            public List<String> paging() {
                markList.add("paging");
                return selectedList;
            }
        });

        // ## Assert ##
        assertEquals(19, rb.size());
        assertEquals(19, rb.getAllRecordCount());
        assertEquals(1, rb.getAllPageCount());
        assertEquals(1, rb.getOrderByClause().getOrderByList().size());
        assertEquals("count", markList.get(0));
        assertEquals("paging", markList.get(1));
    }

    public void test_invokePaging_onePage_countLater() {
        // ## Arrange ##
        final List<String> selectedList = new ArrayList<String>();
        fillList(selectedList, 19);
        final SimplePagingBean pagingBean = new SimplePagingBean() {
            @Override
            public boolean canPagingCountLater() {
                return true;
            }
        };
        pagingBean.getSqlClause().registerOrderBy("aaa", true, new MockColumnInfo());
        pagingBean.fetchFirst(20);
        PagingInvoker<String> tgt = createTarget();

        // ## Act ##
        final List<String> markList = new ArrayList<String>();
        PagingResultBean<String> rb = tgt.invokePaging(new PagingHandler<String>() {
            public PagingBean getPagingBean() {
                return pagingBean;
            }

            public int count() {
                markList.add("count");
                return 19;
            }

            public List<String> paging() {
                markList.add("paging");
                return selectedList;
            }
        });

        // ## Assert ##
        assertEquals(19, rb.size());
        assertEquals(19, rb.getAllRecordCount());
        assertEquals(1, rb.getAllPageCount());
        assertEquals(1, rb.getOrderByClause().getOrderByList().size());
        assertEquals("paging", markList.get(0));
        assertEquals(1, markList.size());
    }

    public void test_invokePaging_onePage_countLater_just() {
        // ## Arrange ##
        final List<String> selectedList = new ArrayList<String>();
        fillList(selectedList, 20);
        final SimplePagingBean pagingBean = new SimplePagingBean() {
            @Override
            public boolean canPagingCountLater() {
                return true;
            }
        };
        pagingBean.getSqlClause().registerOrderBy("aaa", true, new MockColumnInfo());
        pagingBean.fetchFirst(20);
        PagingInvoker<String> tgt = createTarget();

        // ## Act ##
        final List<String> markList = new ArrayList<String>();
        PagingResultBean<String> rb = tgt.invokePaging(new PagingHandler<String>() {
            public PagingBean getPagingBean() {
                return pagingBean;
            }

            public int count() {
                markList.add("count");
                return 20;
            }

            public List<String> paging() {
                markList.add("paging");
                return selectedList;
            }
        });

        // ## Assert ##
        assertEquals(20, rb.size());
        assertEquals(20, rb.getAllRecordCount());
        assertEquals(1, rb.getAllPageCount());
        assertEquals(1, rb.getOrderByClause().getOrderByList().size());
        assertEquals("paging", markList.get(0));
        assertEquals("count", markList.get(1));
    }

    public void test_invokePaging_twoPageAll() {
        // ## Arrange ##
        final List<String> selectedList = new ArrayList<String>();
        fillList(selectedList, 20);
        final SimplePagingBean pagingBean = new SimplePagingBean();
        pagingBean.getSqlClause().registerOrderBy("aaa", true, new MockColumnInfo());
        pagingBean.fetchFirst(20);
        PagingInvoker<String> tgt = createTarget();

        // ## Act ##
        final List<String> markList = new ArrayList<String>();
        PagingResultBean<String> rb = tgt.invokePaging(new PagingHandler<String>() {
            public PagingBean getPagingBean() {
                return pagingBean;
            }

            public int count() {
                markList.add("count");
                return 38;
            }

            public List<String> paging() {
                markList.add("paging");
                return selectedList;
            }
        });

        // ## Assert ##
        assertEquals(20, rb.size());
        assertEquals(38, rb.getAllRecordCount());
        assertEquals(2, rb.getAllPageCount());
        assertEquals(1, rb.getOrderByClause().getOrderByList().size());
        assertEquals("count", markList.get(0));
        assertEquals("paging", markList.get(1));
    }

    public void test_invokePaging_twoPageCurrent_countLater() {
        // ## Arrange ##
        final List<String> selectedList = new ArrayList<String>();
        fillList(selectedList, 19);
        final SimplePagingBean pagingBean = new SimplePagingBean() {
            @Override
            public boolean canPagingCountLater() {
                return true;
            }
        };
        pagingBean.getSqlClause().registerOrderBy("aaa", true, new MockColumnInfo());
        pagingBean.fetchFirst(20);
        pagingBean.xfetchPage(2);
        PagingInvoker<String> tgt = createTarget();

        // ## Act ##
        final List<String> markList = new ArrayList<String>();
        PagingResultBean<String> rb = tgt.invokePaging(new PagingHandler<String>() {
            public PagingBean getPagingBean() {
                return pagingBean;
            }

            public int count() {
                markList.add("count");
                return 68; // should be unused
            }

            public List<String> paging() {
                markList.add("paging");
                return selectedList;
            }
        });

        // ## Assert ##
        assertEquals(19, rb.size());
        assertEquals(39, rb.getAllRecordCount());
        assertEquals(2, rb.getAllPageCount());
        assertEquals(1, rb.getOrderByClause().getOrderByList().size());
        assertEquals("paging", markList.get(0));
        assertEquals(1, markList.size());
    }

    public void test_invokePaging_threePageAll_just() {
        // ## Arrange ##
        final List<String> selectedList = new ArrayList<String>();
        fillList(selectedList, 20);
        final SimplePagingBean pagingBean = new SimplePagingBean();
        pagingBean.getSqlClause().registerOrderBy("aaa", true, new MockColumnInfo());
        pagingBean.fetchFirst(20);
        PagingInvoker<String> tgt = createTarget();

        // ## Act ##
        final List<String> markList = new ArrayList<String>();
        PagingResultBean<String> rb = tgt.invokePaging(new PagingHandler<String>() {
            public PagingBean getPagingBean() {
                return pagingBean;
            }

            public int count() {
                markList.add("count");
                return 60;
            }

            public List<String> paging() {
                markList.add("paging");
                return selectedList;
            }
        });

        // ## Assert ##
        assertEquals(20, rb.size());
        assertEquals(60, rb.getAllRecordCount());
        assertEquals(3, rb.getAllPageCount());
        assertEquals(1, rb.getOrderByClause().getOrderByList().size());
        assertEquals("count", markList.get(0));
        assertEquals("paging", markList.get(1));
    }

    public void test_invokePaging_threePageCurrent_countLater() {
        // ## Arrange ##
        final List<String> selectedList = new ArrayList<String>();
        fillList(selectedList, 19);
        final SimplePagingBean pagingBean = new SimplePagingBean() {
            @Override
            public boolean canPagingCountLater() {
                return true;
            }
        };
        pagingBean.getSqlClause().registerOrderBy("aaa", true, new MockColumnInfo());
        pagingBean.fetchFirst(20);
        pagingBean.xfetchPage(3);
        PagingInvoker<String> tgt = createTarget();

        // ## Act ##
        final List<String> markList = new ArrayList<String>();
        PagingResultBean<String> rb = tgt.invokePaging(new PagingHandler<String>() {
            public PagingBean getPagingBean() {
                return pagingBean;
            }

            public int count() {
                markList.add("count");
                return 59;
            }

            public List<String> paging() {
                markList.add("paging");
                return selectedList;
            }
        });

        // ## Assert ##
        assertEquals(19, rb.size());
        assertEquals(59, rb.getAllRecordCount());
        assertEquals(3, rb.getAllPageCount());
        assertEquals(1, rb.getOrderByClause().getOrderByList().size());
        assertEquals("paging", markList.get(0));
        assertEquals(1, markList.size());
    }

    public void test_invokePaging_threePageCurrent_countLater_just() {
        // ## Arrange ##
        final List<String> selectedList = new ArrayList<String>();
        fillList(selectedList, 20);
        final SimplePagingBean pagingBean = new SimplePagingBean() {
            @Override
            public boolean canPagingCountLater() {
                return true;
            }
        };
        pagingBean.getSqlClause().registerOrderBy("aaa", true, new MockColumnInfo());
        pagingBean.fetchFirst(20);
        pagingBean.xfetchPage(3);
        PagingInvoker<String> tgt = createTarget();

        // ## Act ##
        final List<String> markList = new ArrayList<String>();
        PagingResultBean<String> rb = tgt.invokePaging(new PagingHandler<String>() {
            public PagingBean getPagingBean() {
                return pagingBean;
            }

            public int count() {
                markList.add("count");
                return 60;
            }

            public List<String> paging() {
                markList.add("paging");
                return selectedList;
            }
        });

        // ## Assert ##
        assertEquals(20, rb.size());
        assertEquals(60, rb.getAllRecordCount());
        assertEquals(3, rb.getAllPageCount());
        assertEquals(1, rb.getOrderByClause().getOrderByList().size());
        assertEquals("paging", markList.get(0));
        assertEquals("count", markList.get(1));
    }

    // ===================================================================================
    //                                                       isNecessaryToReadCountLater()
    //                                                       =============================
    public void test_isCurrentLastPage() {
        // ## Arrange ##
        List<String> selectedList = new ArrayList<String>();
        PagingBean pagingBean = new SimplePagingBean();
        pagingBean.fetchFirst(30);
        PagingInvoker<String> tgt = createTarget();

        // ## Act & Assert ##
        fillList(selectedList, 28);
        assertTrue(tgt.isCurrentLastPage(selectedList, pagingBean));
        fillList(selectedList, 29);
        assertTrue(tgt.isCurrentLastPage(selectedList, pagingBean));
        fillList(selectedList, 30);
        assertFalse(tgt.isCurrentLastPage(selectedList, pagingBean));
        fillList(selectedList, 31);
        assertFalse(tgt.isCurrentLastPage(selectedList, pagingBean));
        fillList(selectedList, 60);
        assertFalse(tgt.isCurrentLastPage(selectedList, pagingBean));
        fillList(selectedList, 61);
        assertFalse(tgt.isCurrentLastPage(selectedList, pagingBean));
    }

    // ===================================================================================
    //                                            deriveAllRecordCountFromLastPageValues()
    //                                            ========================================
    public void test_deriveAllRecordCountFromLastPageValues() {
        // ## Arrange ##
        List<String> selectedList = new ArrayList<String>();
        PagingBean pagingBean = new SimplePagingBean();
        pagingBean.fetchFirst(30);
        PagingInvoker<String> tgt = createTarget();

        // ## Act & Assert ##
        pagingBean.xfetchPage(1);
        fillList(selectedList, 28);
        assertEquals(28, tgt.deriveAllRecordCountByLastPage(selectedList, pagingBean));
        fillList(selectedList, 30);
        assertEquals(30, tgt.deriveAllRecordCountByLastPage(selectedList, pagingBean));
        pagingBean.xfetchPage(2);
        fillList(selectedList, 28);
        assertEquals(58, tgt.deriveAllRecordCountByLastPage(selectedList, pagingBean));
        fillList(selectedList, 30);
        assertEquals(60, tgt.deriveAllRecordCountByLastPage(selectedList, pagingBean));
    }

    // ===================================================================================
    //                                                        isNecessaryToReadPageAgain()
    //                                                        ============================
    public void test_isNecessaryToReadPageAgain() {
        // ## Arrange ##
        List<String> selectedList = new ArrayList<String>();
        PagingResultBean<String> rb = new PagingResultBean<String>();
        rb.setSelectedList(selectedList);
        PagingInvoker<String> tgt = createTarget();

        // ## Act & Assert ##
        rb.setAllRecordCount(0);
        assertFalse(tgt.isNecessaryToReadPageAgain(rb));
        rb.setAllRecordCount(1);
        assertTrue(tgt.isNecessaryToReadPageAgain(rb));
        selectedList.add("one");
        assertFalse(tgt.isNecessaryToReadPageAgain(rb));
        rb.setAllRecordCount(0);
        assertFalse(tgt.isNecessaryToReadPageAgain(rb));
    }

    // ===================================================================================
    //                                                                         Test Helper
    //                                                                         ===========
    protected void fillList(List<String> selectedList, int size) {
        selectedList.clear();
        for (int i = 0; i < size; i++) {
            selectedList.add("element" + i);
        }
    }

    protected PagingInvoker<String> createTarget() {
        return new PagingInvoker<String>("dummy");
    }
}
