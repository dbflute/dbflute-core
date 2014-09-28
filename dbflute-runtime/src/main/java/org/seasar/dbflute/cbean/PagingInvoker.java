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

import java.util.List;

import org.seasar.dbflute.exception.DangerousResultSizeException;
import org.seasar.dbflute.exception.PagingOverSafetySizeException;
import org.seasar.dbflute.exception.PagingStatusInvalidException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.resource.DBFluteSystem;
import org.seasar.dbflute.resource.ManualThreadDataSourceHandler;

/**
 * The invoker of paging.
 * @param <ENTITY> The type of entity.
 * @author jflute
 */
public class PagingInvoker<ENTITY> {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _tableDbName;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public PagingInvoker(String tableDbName) {
        _tableDbName = tableDbName;
    }

    // ===================================================================================
    //                                                                              Invoke
    //                                                                              ======
    /**
     * Invoke select-page by handler.
     * @param handler The handler of paging. (NotNull)
     * @return The result bean of paging. (NotNull)
     * @exception org.seasar.dbflute.exception.PagingStatusInvalidException When the paging status is invalid.
     * @exception org.seasar.dbflute.exception.PagingOverSafetySizeException When the paging is over safety size.
     */
    public PagingResultBean<ENTITY> invokePaging(PagingHandler<ENTITY> handler) {
        assertObjectNotNull("handler", handler);
        final PagingBean pagingBean = handler.getPagingBean();
        assertObjectNotNull("handler.getPagingBean()", pagingBean);
        if (!pagingBean.isFetchScopeEffective()) {
            throwPagingStatusInvalidException(pagingBean);
        }
        final ResultBeanBuilder<ENTITY> builder = createResultBeanBuilder();
        final boolean useManualThreadDataSource = isUseManualThreadDataSource();
        if (useManualThreadDataSource) {
            ManualThreadDataSourceHandler.prepareDataSourceHandler();
        }
        try {
            final InvocationResultResource<ENTITY> resource = doPaging(handler, pagingBean, builder);
            final int allRecordCount = resource.getAllRecordCount();
            final List<ENTITY> selectedList = resource.getSelectedList();
            final PagingResultBean<ENTITY> rb = builder.buildPagingResultBean(pagingBean, allRecordCount, selectedList);
            if (pagingBean.canPagingReSelect() && isNecessaryToReadPageAgain(rb)) {
                return reselect(handler, pagingBean, builder, rb);
            } else {
                return rb;
            }
        } finally {
            pagingBean.xsetPaging(true); // restore its paging state finally
            if (useManualThreadDataSource) {
                ManualThreadDataSourceHandler.closeDataSourceHandler();
            }
        }
    }

    protected InvocationResultResource<ENTITY> doPaging(PagingHandler<ENTITY> handler, PagingBean pagingBean,
            ResultBeanBuilder<ENTITY> builder) {
        final int safetyMaxResultSize = pagingBean.getSafetyMaxResultSize();
        final int allRecordCount;
        final List<ENTITY> selectedList;
        if (pagingBean.canPagingCountLater()) { // faster when last page selected (contains zero record)
            selectedList = executePaging(handler);
            if (isCurrentLastPage(selectedList, pagingBean)) {
                allRecordCount = deriveAllRecordCountByLastPage(selectedList, pagingBean);
            } else {
                allRecordCount = executeCount(handler); // count later
            }
            checkSafetyResultIfNeeds(safetyMaxResultSize, allRecordCount);
        } else { // faster when zero record selected
            // basically main here because it has been used for a long time
            allRecordCount = executeCount(handler);
            checkSafetyResultIfNeeds(safetyMaxResultSize, allRecordCount);
            if (allRecordCount == 0) {
                selectedList = builder.buildEmptyListResultBean(pagingBean);
            } else {
                selectedList = executePaging(handler);
            }
        }
        final InvocationResultResource<ENTITY> resource = new InvocationResultResource<ENTITY>();
        resource.setAllRecordCount(allRecordCount);
        resource.setSelectedList(selectedList);
        return resource;
    }

    protected static class InvocationResultResource<ENTITY> {
        protected int _allRecordCount;
        protected List<ENTITY> _selectedList;

        public int getAllRecordCount() {
            return _allRecordCount;
        }

        public void setAllRecordCount(int allRecordCount) {
            _allRecordCount = allRecordCount;
        }

        public List<ENTITY> getSelectedList() {
            return _selectedList;
        }

        public void setSelectedList(List<ENTITY> selectedList) {
            _selectedList = selectedList;
        }
    }

    /**
     * Create the builder of result bean.
     * @return The builder of result bean. (NotNull)
     */
    protected ResultBeanBuilder<ENTITY> createResultBeanBuilder() {
        return new ResultBeanBuilder<ENTITY>(_tableDbName);
    }

    protected int executeCount(PagingHandler<ENTITY> handler) {
        return handler.count();
    }

    protected List<ENTITY> executePaging(PagingHandler<ENTITY> handler) {
        return handler.paging();
    }

    protected PagingResultBean<ENTITY> reselect(PagingHandler<ENTITY> handler, PagingBean pagingBean,
            ResultBeanBuilder<ENTITY> builder, PagingResultBean<ENTITY> rb) {
        pagingBean.fetchPage(rb.getAllPageCount());
        final InvocationResultResource<ENTITY> resource = doPaging(handler, pagingBean, builder);
        final int allRecordCount = resource.getAllRecordCount();
        final List<ENTITY> selectedList = resource.getSelectedList();
        return builder.buildPagingResultBean(pagingBean, allRecordCount, selectedList);
    }

    /**
     * Is the current page is last page?
     * @param selectedList The selected list. (NotNull)
     * @param pagingBean The bean of paging. (NotNull)
     * @return The determination, true or false.
     */
    protected boolean isCurrentLastPage(List<ENTITY> selectedList, PagingBean pagingBean) {
        if (selectedList.size() == 0 && pagingBean.getFetchPageNumber() > 1) {
            return false; // because of unknown all record count (re-selected later)
        }
        // /- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  
        // It returns true if the size of list is under fetch size(page size).
        // (contains the size is zero and first page is target)
        // 
        // {For example}
        // If the fetch size is 20 and the size of selected list is 19 or less,
        // the current page must be last page(contains when only one page exists). 
        // it is NOT necessary to read count because the 19 is the hint to derive all record count.
        // 
        // If the fetch size is 20 and the size of selected list is 20,
        // it is necessary to read count because we cannot know whether the next pages exist or not.
        // - - - - - - - - - -/
        return selectedList.size() <= (pagingBean.getFetchSize() - 1);
    }

    /**
     * Derive all record count by last page.
     * @param selectedList The selected list. (NotNull)
     * @param pagingBean The bean of paging. (NotNull)
     * @return Derived all record count.
     */
    protected int deriveAllRecordCountByLastPage(List<ENTITY> selectedList, PagingBean pagingBean) {
        int baseSize = (pagingBean.getFetchPageNumber() - 1) * pagingBean.getFetchSize();
        return baseSize + selectedList.size();
    }

    /**
     * Is it necessary to read page again?
     * @param rb The result bean of paging. (NotNull)
     * @return The determination, true or false.
     */
    protected boolean isNecessaryToReadPageAgain(PagingResultBean<ENTITY> rb) {
        return rb.getAllRecordCount() > 0 && rb.getSelectedList().isEmpty();
    }

    /**
     * Check whether the count of all records is safety or not if it needs.
     * @param safetyMaxResultSize The max size of safety result.
     * @param allRecordCount The count of all records.
     * @throws DangerousResultSizeException When the count of all records is dangerous.
     */
    protected void checkSafetyResultIfNeeds(int safetyMaxResultSize, int allRecordCount) {
        if (safetyMaxResultSize > 0 && allRecordCount > safetyMaxResultSize) {
            throwPagingOverSafetySizeException(safetyMaxResultSize, allRecordCount);
        }
    }

    protected boolean isUseManualThreadDataSource() {
        return true; // basically for MySQL's found_rows() when no transaction
    }

    protected void throwPagingStatusInvalidException(PagingBean pagingBean) {
        final boolean cbean = pagingBean instanceof ConditionBean;
        final String name = cbean ? "condition-bean" : "parameter-bean";
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The status of paging was INVALID. (paging parameters was not found)");
        br.addItem("Advice");
        br.addElement("Confirm your logic for paging of " + name + ".");
        br.addElement("Paging execution needs paging parameters 'pageSize' and 'pageNumber'.");
        br.addElement("For example:");
        br.addElement("  (x):");
        if (cbean) {
            br.addElement("    MemberCB cb = new MemberCB();");
            br.addElement("    cb.query().set...;");
            br.addElement("    ... = memberBhv.selectPage(cb);");
        } else {
            br.addElement("    SimpleMemberPmb pmb = new SimpleMemberPmb();");
            br.addElement("    pmb.set...;");
            br.addElement("    ... = memberBhv.outsideSql().manualPaging().selectPage(...);");
        }
        br.addElement("  (o):");
        if (cbean) {
            br.addElement("    MemberCB cb = new MemberCB();");
            br.addElement("    cb.query().set...;");
            br.addElement("    cb.paging(20, 2); // *Point!");
            br.addElement("    ... = memberBhv.selectPage(cb);");
        } else {
            br.addElement("    SimpleMemberPmb pmb = new SimpleMemberPmb();");
            br.addElement("    pmb.set...;");
            br.addElement("    pmb.paging(20, 2); // *Point!");
            br.addElement("    ... = memberBhv.outsideSql().manualPaging().selectPage(...);");
        }
        br.addItem("PagingBean");
        br.addElement(pagingBean);
        final String msg = br.buildExceptionMessage();
        throw new PagingStatusInvalidException(msg);
    }

    protected void throwPagingOverSafetySizeException(int safetyMaxResultSize, int allRecordCount) {
        // here simple message because an entry method catches this
        String msg = "The paging was over the specified safety size:";
        msg = msg + " " + allRecordCount + " > " + safetyMaxResultSize;
        throw new PagingOverSafetySizeException(msg, safetyMaxResultSize, allRecordCount);
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String ln() {
        return DBFluteSystem.getBasicLn();
    }

    // ===================================================================================
    //                                                                       Assert Helper
    //                                                                       =============
    /**
     * Assert that the object is not null.
     * @param variableName Variable name. (NotNull)
     * @param value Value. (NotNull)
     * @exception IllegalArgumentException
     */
    protected void assertObjectNotNull(String variableName, Object value) {
        if (variableName == null) {
            String msg = "The value should not be null: variableName=null value=" + value;
            throw new IllegalArgumentException(msg);
        }
        if (value == null) {
            String msg = "The value should not be null: variableName=" + variableName;
            throw new IllegalArgumentException(msg);
        }
    }
}
