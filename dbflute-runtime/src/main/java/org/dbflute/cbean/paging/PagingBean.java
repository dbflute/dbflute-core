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

import org.dbflute.cbean.ordering.OrderByBean;

/**
 * The bean for paging.
 * @author jflute
 */
public interface PagingBean extends FetchNarrowingBean, OrderByBean {

    // ===================================================================================
    //                                                                Paging Determination
    //                                                                ====================
    /**
     * Is the execution for paging(NOT count)? {for parameter comment}
     * @return The determination, true or false.
     */
    boolean isPaging();

    /**
     * Can the paging execute count later? {for framework}
     * @return The determination, true or false.
     */
    boolean canPagingCountLater();

    /**
     * Can the paging re-select? {for framework}
     * @return The determination, true or false.
     */
    boolean canPagingReSelect();

    // ===================================================================================
    //                                                                      Paging Setting
    //                                                                      ==============
    /**
     * Set up paging resources.
     * <pre>
     * e.g. ConditionBean
     * MemberCB cb = new MemberCB();
     * cb.query().setMemberName_PrefixSearch("S");
     * cb.query().addOrderBy_Birthdate_Desc();
     * cb.<span style="color: #CC4747">paging</span>(20, 3); <span style="color: #3F7E5E">// 20 records per a page and current page number is 3</span>
     * PagingResultBean&lt;Member&gt; page = memberBhv.<span style="color: #CC4747">selectPage</span>(cb);
     * </pre>
     * @param pageSize The page size per one page. (NotMinus, NotZero)
     * @param pageNumber The number of page. It's ONE origin. (NotMinus, NotZero: If it's minus or zero, it treats as one.)
     * @throws org.dbflute.exception.PagingPageSizeNotPlusException When the page size for paging is minus or zero. 
     */
    void paging(int pageSize, int pageNumber);

    /**
     * Set whether the execution for paging(NOT count). {INTERNAL METHOD}
     * @param paging Determination.
     */
    void xsetPaging(boolean paging);

    /**
     * Enable paging count-later that means counting after selecting. (back to default) <br>
     * You can use it by default on DBFlute so you don't need to call this basically.
     * If you've suppressed it by settings of DBFlute property, you can use it by calling. <br>
     * You should call this before execution of selectPage().
     */
    void enablePagingCountLater();

    /**
     * Disable paging count-later that means counting after selecting. (default is enabled) <br>
     * You should call this before execution of selectPage().
     */
    void disablePagingCountLater();

    /**
     * Enable paging re-select that is executed when the page number is over page count. (back to default) <br>
     * You can use it by default on DBFlute so you don't need to call this basically.
     * If you've suppressed it by settings of DBFlute property, you can use it by calling. <br>
     * You should call this before execution of selectPage().
     */
    void enablePagingReSelect();

    /**
     * Disable paging re-select that is executed when the page number is over page count. (default is enabled) <br>
     * You should call this before execution of selectPage().
     */
    void disablePagingReSelect();

    // ===================================================================================
    //                                                                       Fetch Setting
    //                                                                       =============
    /**
     * Fetch first records only.
     * e.g. ConditionBean
     * MemberCB cb = new MemberCB();
     * cb.query().setMemberName_PrefixSearch("S");
     * cb.query().addOrderBy_Birthdate_Desc();
     * cb.<span style="color: #CC4747">fetchFirst</span>(5); <span style="color: #3F7E5E">// top 5</span>
     * ListResultBean&lt;Member&gt; memberList = memberBhv.<span style="color: #CC4747">selectList</span>(cb);
     * @param fetchSize The size of fetch. (NotMinus, NotZero)
     * @return this. (NotNull)
     */
    PagingBean fetchFirst(int fetchSize);

    /**
     * Fetch records in the scope only. {Internal}<br>
     * This method is an old style, so you should use paging() instead of this. <br>
     * @param fetchStartIndex The start index of fetch. 0 origin. (NotMinus)
     * @param fetchSize The size of fetch. (NotMinus, NotZero)
     * @return this. (NotNull)
     */
    PagingBean xfetchScope(int fetchStartIndex, int fetchSize);

    /**
     * Fetch page. {Internal}<br>
     * This method is an old style, so you should use paging() instead of this. <br>
     * When you call this, it is normally necessary to invoke 'fetchFirst()' or 'fetchScope()' ahead of that. <br>
     * But you also can use default-fetch-size without invoking 'fetchFirst()' or 'fetchScope()'. <br>
     * If you invoke this, your SQL returns [fetch-size] records from [fetch-start-index] calculated by [fetch-page-number].
     * @param fetchPageNumber The page number of fetch. 1 origin. (NotMinus, NotZero: If minus or zero, set one.)
     * @return this. (NotNull)
     */
    PagingBean xfetchPage(int fetchPageNumber);

    // ===================================================================================
    //                                                                     Paging Resource
    //                                                                     ===============
    /**
     * Create the invoker of paging.
     * @param <ENTITY> The type of entity.
     * @param tableDbName The DB name of table. (NotNull)
     * @return The instance of PagingInvoker for the table. (NotNull)
     */
    <ENTITY> PagingInvoker<ENTITY> createPagingInvoker(String tableDbName);

    // ===================================================================================
    //                                                                      Fetch Property
    //                                                                      ==============
    /**
     * Get fetch start index.
     * @return The index of fetch-start.
     */
    int getFetchStartIndex();

    /**
     * Get fetch size of paging, also called page size.
     * @return The size of fetching rows.
     */
    int getFetchSize();

    /**
     * Get fetch page number.
     * @return The page number of fetch.
     */
    int getFetchPageNumber();

    /**
     * Get page start index.
     * @return The index of page start, 0 origin. (NotMinus)
     */
    int getPageStartIndex();

    /**
     * Get page end index.
     * @return The index of page end, 0 origin. (NotMinus)
     */
    int getPageEndIndex();

    /**
     * Is fetch scope effective?
     * @return The determination, true or false.
     */
    boolean isFetchScopeEffective();
}
