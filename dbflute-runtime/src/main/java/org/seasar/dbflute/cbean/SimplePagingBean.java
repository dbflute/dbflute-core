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
package org.seasar.dbflute.cbean;

import java.util.LinkedHashMap;
import java.util.Map;

import org.seasar.dbflute.cbean.sqlclause.SqlClause;
import org.seasar.dbflute.cbean.sqlclause.SqlClauseDefault;
import org.seasar.dbflute.cbean.sqlclause.orderby.OrderByClause;
import org.seasar.dbflute.exception.PagingPageSizeNotPlusException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.resource.DBFluteSystem;
import org.seasar.dbflute.twowaysql.pmbean.MapParameterBean;

/**
 * The simple implementation of paging-bean.
 * @author jflute
 */
public class SimplePagingBean implements PagingBean, MapParameterBean<Object> {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** SQL clause instance. */
    protected final SqlClause _sqlClause;
    {
        // use only paging methods and order-by methods
        _sqlClause = new SqlClauseDefault("dummy");
    }

    /** The map of parameter. (NullAllowed) */
    protected Map<String, Object> _parameterMap;

    /** The max size of safety result. */
    protected int _safetyMaxResultSize;

    /** Is the execution for paging(NOT count)? */
    protected boolean _paging = true;

    /** Is the count executed later? */
    protected boolean _pagingCountLater;

    /** Can the paging re-select? */
    protected boolean _pagingReSelect = true;

    /** Is fetch narrowing valid? */
    protected boolean _fetchNarrowing = true;

    // ===================================================================================
    //                                                        Implementation of PagingBean
    //                                                        ============================
    // -----------------------------------------------------
    //                                  Paging Determination
    //                                  --------------------
    /**
     * {@inheritDoc}
     */
    public boolean isPaging() { // for parameter comment
        return _paging;
    }

    /**
     * {@inheritDoc}
     */
    public boolean canPagingCountLater() { // for framework
        return _pagingCountLater;
    }

    /**
     * {@inheritDoc}
     */
    public boolean canPagingReSelect() { // for framework
        return _pagingReSelect;
    }

    // -----------------------------------------------------
    //                                        Paging Setting
    //                                        --------------
    /**
     * {@inheritDoc}
     */
    public void paging(int pageSize, int pageNumber) {
        if (pageSize <= 0) {
            throwPagingPageSizeNotPlusException(pageSize, pageNumber);
        }
        fetchFirst(pageSize);
        fetchPage(pageNumber);
    }

    protected void throwPagingPageSizeNotPlusException(int pageSize, int pageNumber) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Page size for paging should not be minus or zero!");
        br.addItem("Advice");
        br.addElement("Confirm the value of your parameter 'pageSize'.");
        br.addElement("The first parameter of paging() should be a plus value!");
        br.addElement("For example:");
        br.addElement("  (x): pmb.paging(0, 1);");
        br.addElement("  (x): pmb.paging(-3, 2);");
        br.addElement("  (o): pmb.paging(4, 3);");
        br.addItem("Page Size");
        br.addElement(pageSize);
        br.addItem("Page Number");
        br.addElement(pageNumber);
        final String msg = br.buildExceptionMessage();
        throw new PagingPageSizeNotPlusException(msg);
    }

    /**
     * {@inheritDoc}
     */
    public void xsetPaging(boolean paging) {
        if (paging) {
            getSqlClause().reviveFetchScope();
        } else {
            getSqlClause().suppressFetchScope();
        }
        _paging = paging;
    }

    /**
     * {@inheritDoc}
     */
    public void enablePagingCountLater() {
        _pagingCountLater = true;
    }

    /**
     * {@inheritDoc}
     */
    public void disablePagingCountLater() {
        _pagingCountLater = false;
    }

    /**
     * {@inheritDoc}
     */
    public void enablePagingReSelect() {
        _pagingReSelect = true;
    }

    /**
     * {@inheritDoc}
     */
    public void disablePagingReSelect() {
        _pagingReSelect = false;
    }

    // -----------------------------------------------------
    //                                         Fetch Setting
    //                                         -------------
    /**
     * {@inheritDoc}
     */
    public PagingBean fetchFirst(int fetchSize) {
        getSqlClause().fetchFirst(fetchSize);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public PagingBean fetchScope(int fetchStartIndex, int fetchSize) {
        getSqlClause().fetchScope(fetchStartIndex, fetchSize);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public PagingBean fetchPage(int fetchPageNumber) {
        getSqlClause().fetchPage(fetchPageNumber);
        return this;
    }

    // -----------------------------------------------------
    //                                       Paging Resource
    //                                       ---------------
    /**
     * {@inheritDoc}
     */
    public <ENTITY> PagingInvoker<ENTITY> createPagingInvoker(String tableDbName) {
        return new PagingInvoker<ENTITY>(tableDbName);
    }

    // -----------------------------------------------------
    //                                        Fetch Property
    //                                        --------------
    /**
     * {@inheritDoc}
     */
    public int getFetchStartIndex() {
        return getSqlClause().getFetchStartIndex();
    }

    /**
     * {@inheritDoc}
     */
    public int getFetchSize() {
        return getSqlClause().getFetchSize();
    }

    /**
     * {@inheritDoc}
     */
    public int getFetchPageNumber() {
        return getSqlClause().getFetchPageNumber();
    }

    /**
     * {@inheritDoc}
     */
    public int getPageStartIndex() {
        return getSqlClause().getPageStartIndex();
    }

    /**
     * {@inheritDoc}
     */
    public int getPageEndIndex() {
        return getSqlClause().getPageEndIndex();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isFetchScopeEffective() {
        return getSqlClause().isFetchScopeEffective();
    }

    // ===================================================================================
    //                                                        Implementation of SelectBean
    //                                                        ============================
    /**
     * {@inheritDoc}
     */
    public int getSafetyMaxResultSize() {
        return _safetyMaxResultSize;
    }

    // ===================================================================================
    //                                                Implementation of FetchNarrowingBean
    //                                                ====================================
    /**
     * {@inheritDoc}
     */
    public int getFetchNarrowingSkipStartIndex() {
        return getSqlClause().getFetchNarrowingSkipStartIndex();
    }

    /**
     * {@inheritDoc}
     */
    public int getFetchNarrowingLoopCount() {
        return getSqlClause().getFetchNarrowingLoopCount();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isFetchNarrowingSkipStartIndexEffective() {
        return !getSqlClause().isFetchStartIndexSupported();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isFetchNarrowingLoopCountEffective() {
        return !getSqlClause().isFetchSizeSupported();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isFetchNarrowingEffective() {
        return _fetchNarrowing && getSqlClause().isFetchNarrowingEffective();
    }

    /**
     * {@inheritDoc}
     */
    public void xdisableFetchNarrowing() {
        _fetchNarrowing = false;
    }

    /**
     * {@inheritDoc}
     */
    public void xenableIgnoredFetchNarrowing() {
        _fetchNarrowing = true;
    }

    // ===================================================================================
    //                                                       Implementation of OrderByBean
    //                                                       =============================
    // basically unused because this class does not have order-by registration I/F
    // (you can use these methods if you implements original methods at your sub-class of this class)
    /**
     * {@inheritDoc}
     */
    public String getOrderByClause() {
        return getSqlClause().getOrderByClause();
    }

    /**
     * {@inheritDoc}
     */
    public OrderByClause getOrderByComponent() {
        return getSqlClause().getOrderByComponent();
    }

    /**
     * {@inheritDoc}
     */
    public OrderByBean clearOrderBy() {
        getSqlClause().clearOrderBy();
        return this;
    }

    // ===================================================================================
    //                                                    Implementation of SelectResource
    //                                                    ================================
    /**
     * {@inheritDoc}
     */
    public void checkSafetyResult(int safetyMaxResultSize) {
        _safetyMaxResultSize = safetyMaxResultSize;
    }

    // ===================================================================================
    //                                                  Implementation of MapParameterBean
    //                                                  ==================================
    /**
     * {@inheritDoc}
     */
    public Map<String, Object> getParameterMap() {
        initializeParameterMapIfNeeds();
        return _parameterMap;
    }

    /**
     * Add the parameter to the map.
     * @param key The key of parameter. (NotNull)
     * @param value The value of parameter. (NullAllowed)
     */
    public void addParameter(String key, Object value) {
        initializeParameterMapIfNeeds();
        _parameterMap.put(key, value);
    }

    protected void initializeParameterMapIfNeeds() {
        if (_parameterMap == null) {
            _parameterMap = new LinkedHashMap<String, Object>();
        }
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String ln() {
        return DBFluteSystem.getBasicLn();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    // -----------------------------------------------------
    //                                             SqlClause
    //                                             ---------
    /**
     * Get SQL clause instance. {Internal}<br />
     * @return SQL clause. (NotNull)
     */
    protected SqlClause getSqlClause() {
        return _sqlClause;
    }
}
