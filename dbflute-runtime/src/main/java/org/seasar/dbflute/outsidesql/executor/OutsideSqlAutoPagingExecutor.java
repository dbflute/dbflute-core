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
package org.seasar.dbflute.outsidesql.executor;

import org.seasar.dbflute.DBDef;
import org.seasar.dbflute.bhv.core.BehaviorCommandInvoker;
import org.seasar.dbflute.cbean.ListResultBean;
import org.seasar.dbflute.cbean.PagingResultBean;
import org.seasar.dbflute.jdbc.StatementConfig;
import org.seasar.dbflute.outsidesql.OutsideSqlOption;
import org.seasar.dbflute.outsidesql.factory.OutsideSqlExecutorFactory;
import org.seasar.dbflute.outsidesql.typed.AutoPagingHandlingPmb;

/**
 * The auto-paging executor of outside-SQL.
 * @param <BEHAVIOR> The type of behavior.
 * @author jflute
 */
public class OutsideSqlAutoPagingExecutor<BEHAVIOR> extends AbstractOutsideSqlPagingExecutor<BEHAVIOR> {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public OutsideSqlAutoPagingExecutor(BehaviorCommandInvoker behaviorCommandInvoker, String tableDbName,
            DBDef currentDBDef, StatementConfig defaultStatementConfig, OutsideSqlOption outsideSqlOption,
            OutsideSqlExecutorFactory outsideSqlExecutorFactory) {
        super(behaviorCommandInvoker, tableDbName, currentDBDef, defaultStatementConfig, outsideSqlOption,
                outsideSqlExecutorFactory);
    }

    // ===================================================================================
    //                                                                              Select
    //                                                                              ======
    /**
     * Select page by the outside-SQL. <span style="color: #AD4747">{Typed Interface}</span><br />
     * (both count-select and paging-select are executed)<br />
     * You can call this method by only a typed parameter-bean
     * which is related to its own (outside-SQL) path and entity-type.
     * <pre>
     * SimpleMemberPmb pmb = new SimpleMemberPmb();
     * pmb.setMemberName_PrefixSearch("S");
     * pmb.paging(20, 3); <span style="color: #3F7E5E">// 20 records per a page and current page number is 3</span>
     * PagingResultBean&lt;SimpleMember&gt; page
     *     = memberBhv.outsideSql().manualPaging().<span style="color: #DD4747">selectPage</span>(pmb);
     * int allRecordCount = page.getAllRecordCount();
     * int allPageCount = page.getAllPageCount();
     * boolean isExistPrePage = page.isExistPrePage();
     * boolean isExistNextPage = page.isExistNextPage();
     * ...
     * for (SimpleMember member : page) {
     *     ... = member.get...();
     * }
     * </pre>
     * The parameter-bean needs to extend SimplePagingBean.
     * The way to generate it is following:
     * <pre>
     * <span style="color: #3F7E5E">-- !df:pmb extends SPB!</span>
     * <span style="color: #3F7E5E">-- !!Integer memberId!!</span>
     * <span style="color: #3F7E5E">-- !!...!!</span>
     * </pre>
     * You can realize by pagingBean's isPaging() method on your 'Parameter Comment'.
     * It returns false when it executes Count. And it returns true when it executes Paging.
     * <pre>
     * e.g. ManualPaging and MySQL
     * <span style="color: #3F7E5E">/*IF pmb.isPaging()&#42;/</span>
     * select member.MEMBER_ID
     *      , member.MEMBER_NAME
     *      , memberStatus.MEMBER_STATUS_NAME
     * <span style="color: #3F7E5E">-- ELSE select count(*)</span>
     * <span style="color: #3F7E5E">/*END&#42;/</span>
     *   from MEMBER member
     *     <span style="color: #3F7E5E">/*IF pmb.isPaging()&#42;/</span>
     *     left outer join MEMBER_STATUS memberStatus
     *       on member.MEMBER_STATUS_CODE = memberStatus.MEMBER_STATUS_CODE
     *     <span style="color: #3F7E5E">/*END&#42;/</span>
     *  <span style="color: #3F7E5E">/*BEGIN&#42;/</span>
     *  where
     *    <span style="color: #3F7E5E">/*IF pmb.memberId != null&#42;/</span>
     *    member.MEMBER_ID = <span style="color: #3F7E5E">/*pmb.memberId&#42;/</span>'123'
     *    <span style="color: #3F7E5E">/*END&#42;/</span>
     *    <span style="color: #3F7E5E">/*IF pmb.memberName != null&#42;/</span>
     *    and member.MEMBER_NAME like <span style="color: #3F7E5E">/*pmb.memberName&#42;/</span>'Billy%'
     *    <span style="color: #3F7E5E">/*END&#42;/</span>
     *  <span style="color: #3F7E5E">/*END&#42;/</span>
     *  <span style="color: #3F7E5E">/*IF pmb.isPaging()&#42;/</span>
     *  order by member.UPDATE_DATETIME desc
     *  <span style="color: #3F7E5E">/*END&#42;/</span>
     *  <span style="color: #3F7E5E">/*IF pmb.isPaging()&#42;/</span>
     *  limit <span style="color: #3F7E5E">/*$pmb.pageStartIndex&#42;/</span>80, <span style="color: #3F7E5E">/*$pmb.fetchSize&#42;/</span>20
     *  <span style="color: #3F7E5E">/*END&#42;/</span>
     * </pre>
     * @param <ENTITY> The type of entity.
     * @param pmb The typed parameter-bean for auto-paging handling. (NotNull)
     * @return The result bean of paging. (NotNull)
     * @exception org.seasar.dbflute.exception.OutsideSqlNotFoundException When the outside-SQL is not found.
     * @exception org.seasar.dbflute.exception.DangerousResultSizeException When the result size is over the specified safety size.
     */
    public <ENTITY> PagingResultBean<ENTITY> selectPage(AutoPagingHandlingPmb<BEHAVIOR, ENTITY> pmb) {
        if (pmb == null) {
            String msg = "The argument 'pmb' (typed parameter-bean) should not be null.";
            throw new IllegalArgumentException(msg);
        }
        return doSelectPage(pmb.getOutsideSqlPath(), pmb, pmb.getEntityType());
    }

    /**
     * Select list with paging by the outside-SQL. <span style="color: #AD4747">{Typed Interface}</span><br />
     * (count-select is not executed, only paging-select)<br />
     * You can call this method by only a typed parameter-bean
     * which is related to its own (outside-SQL) path and entity-type.
     * <pre>
     * SimpleMemberPmb pmb = new SimpleMemberPmb();
     * pmb.setMemberName_PrefixSearch("S");
     * pmb.paging(20, 3); <span style="color: #3F7E5E">// 20 records per a page and current page number is 3</span>
     * ListResultBean&lt;SimpleMember&gt; memberList
     *     = memberBhv.outsideSql().manualPaging().<span style="color: #DD4747">selectList</span>(pmb);
     * for (SimpleMember member : memberList) {
     *     ... = member.get...();
     * }
     * </pre>
     * The parameter-bean needs to extend SimplePagingBean.
     * The way to generate it is following:
     * <pre>
     * <span style="color: #3F7E5E">-- !df:pmb extends SPB!</span>
     * <span style="color: #3F7E5E">-- !!Integer memberId!!</span>
     * <span style="color: #3F7E5E">-- !!...!!</span>
     * </pre>
     * You don't need to use pagingBean's isPaging() method on your 'Parameter Comment'.
     * <pre>
     * e.g. ManualPaging and MySQL 
     * select member.MEMBER_ID
     *      , member.MEMBER_NAME
     *      , memberStatus.MEMBER_STATUS_NAME
     *   from MEMBER member
     *     left outer join MEMBER_STATUS memberStatus
     *       on member.MEMBER_STATUS_CODE = memberStatus.MEMBER_STATUS_CODE
     *  <span style="color: #3F7E5E">/*BEGIN&#42;/</span>
     *  where
     *    <span style="color: #3F7E5E">/*IF pmb.memberId != null&#42;/</span>
     *    member.MEMBER_ID = <span style="color: #3F7E5E">/*pmb.memberId&#42;/</span>'123'
     *    <span style="color: #3F7E5E">/*END&#42;/</span>
     *    <span style="color: #3F7E5E">/*IF pmb.memberName != null&#42;/</span>
     *    and member.MEMBER_NAME like <span style="color: #3F7E5E">/*pmb.memberName&#42;/</span>'Billy%'
     *    <span style="color: #3F7E5E">/*END&#42;/</span>
     *  <span style="color: #3F7E5E">/*END&#42;/</span>
     *  order by member.UPDATE_DATETIME desc
     *  limit <span style="color: #3F7E5E">/*$pmb.pageStartIndex&#42;/</span>80, <span style="color: #3F7E5E">/*$pmb.fetchSize&#42;/</span>20
     * </pre>
     * @param <ENTITY> The type of entity.
     * @param pmb The typed parameter-bean for auto-paging handling. (NotNull)
     * @return The result bean of paged list. (NotNull)
     * @exception org.seasar.dbflute.exception.OutsideSqlNotFoundException When the outside-SQL is not found.
     * @exception org.seasar.dbflute.exception.DangerousResultSizeException When the result size is over the specified safety size.
     */
    public <ENTITY> ListResultBean<ENTITY> selectList(AutoPagingHandlingPmb<BEHAVIOR, ENTITY> pmb) {
        return doSelectList(pmb.getOutsideSqlPath(), pmb, pmb.getEntityType());
    }

    // ===================================================================================
    //                                                                              Option
    //                                                                              ======
    /**
     * {@inheritDoc}
     */
    @Override
    public OutsideSqlAutoPagingExecutor<BEHAVIOR> removeBlockComment() {
        return (OutsideSqlAutoPagingExecutor<BEHAVIOR>) super.removeBlockComment();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OutsideSqlAutoPagingExecutor<BEHAVIOR> removeLineComment() {
        return (OutsideSqlAutoPagingExecutor<BEHAVIOR>) super.removeLineComment();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OutsideSqlAutoPagingExecutor<BEHAVIOR> formatSql() {
        return (OutsideSqlAutoPagingExecutor<BEHAVIOR>) super.formatSql();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OutsideSqlAutoPagingExecutor<BEHAVIOR> configure(StatementConfig statementConfig) {
        return (OutsideSqlAutoPagingExecutor<BEHAVIOR>) super.configure(statementConfig);
    }
}
