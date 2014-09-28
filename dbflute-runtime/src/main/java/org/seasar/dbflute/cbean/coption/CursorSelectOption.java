/*
 * Copyright 2004-2014 the Seasar Foundation and the Others.
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
package org.seasar.dbflute.cbean.coption;

/**
 * The option for CursorSelect.
 * @author jflute
 * @since 1.0.4A (2013/03/07 Wednesday)
 */
public class CursorSelectOption {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected boolean _byPaging;
    protected boolean _orderByPK;
    protected int _pageSize;

    // ===================================================================================
    //                                                                           by Paging
    //                                                                           =========
    /**
     * Do cursor select by paging ordering by PK. <br />
     * Consistent-read is not perfect. <br />
     * You might select records at second or more select that are not target when the first select.
     * @param pageSize The size of one page. (NotMinus, NotZero)
     * @return this. (NotNull)
     */
    public CursorSelectOption byPagingOrderByPK(int pageSize) {
        _byPaging = true;
        _orderByPK = true;
        _pageSize = pageSize;
        return this;
    }

    /**
     * Do cursor select by paging simply. <br />
     * Consistent-read is not guaranteed. <br />
     * You might select duplicate records or cannot select required records,
     * so you should save consistent-read by DBMS settings, e.g. MySQL's RepeatableRead.
     * @param pageSize The size of one page. (NotMinus, NotZero)
     * @return this. (NotNull)
     * @deprecated Consistent-read is not guaranteed so you should use this carefully.
     */
    public CursorSelectOption byPagingSimply(int pageSize) {
        _byPaging = true;
        _orderByPK = false;
        _pageSize = pageSize;
        return this;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return "{" + _byPaging + ", " + _orderByPK + ", " + _pageSize + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public boolean isByPaging() {
        return _byPaging;
    }

    public boolean isOrderByPK() {
        return _orderByPK;
    }

    public int getPageSize() {
        return _pageSize;
    }
}
