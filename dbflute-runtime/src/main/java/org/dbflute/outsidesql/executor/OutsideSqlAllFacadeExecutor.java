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
package org.dbflute.outsidesql.executor;

import org.dbflute.bhv.exception.BehaviorExceptionThrower;
import org.dbflute.cbean.coption.StatementConfigCall;
import org.dbflute.cbean.result.ListResultBean;
import org.dbflute.cbean.result.PagingResultBean;
import org.dbflute.exception.IllegalConditionBeanOperationException;
import org.dbflute.jdbc.CursorHandler;
import org.dbflute.jdbc.StatementConfig;
import org.dbflute.optional.OptionalEntity;
import org.dbflute.outsidesql.OutsideSqlOption;
import org.dbflute.outsidesql.ProcedurePmb;
import org.dbflute.outsidesql.typed.AutoPagingHandlingPmb;
import org.dbflute.outsidesql.typed.CursorHandlingPmb;
import org.dbflute.outsidesql.typed.EntityHandlingPmb;
import org.dbflute.outsidesql.typed.ExecuteHandlingPmb;
import org.dbflute.outsidesql.typed.ListHandlingPmb;
import org.dbflute.outsidesql.typed.ManualPagingHandlingPmb;
import org.dbflute.outsidesql.typed.PagingHandlingPmb;

/**
 * The all facade executor of outside-SQL.
 * <pre>
 * <span style="color: #3F7E5E">// main style</span> 
 * memberBhv.outideSql().selectEntity(pmb); <span style="color: #3F7E5E">// OptionalEntity</span>
 * memberBhv.outideSql().selectList(pmb); <span style="color: #3F7E5E">// ListResultBean</span>
 * memberBhv.outideSql().selectPage(pmb); <span style="color: #3F7E5E">// PagingResultBean</span>
 * memberBhv.outideSql().selectPagedListOnly(pmb); <span style="color: #3F7E5E">// ListResultBean</span>
 * memberBhv.outideSql().selectCursor(pmb, handler); <span style="color: #3F7E5E">// (by handler)</span>
 * memberBhv.outideSql().execute(pmb); <span style="color: #3F7E5E">// int (updated count)</span>
 * memberBhv.outideSql().call(pmb); <span style="color: #3F7E5E">// void (pmb has OUT parameters)</span>
 *
 * <span style="color: #3F7E5E">// traditional style</span> 
 * memberBhv.outideSql().traditionalStyle().selectEntity(path, pmb, entityType);
 * memberBhv.outideSql().traditionalStyle().selectList(path, pmb, entityType);
 * memberBhv.outideSql().traditionalStyle().selectPage(path, pmb, entityType);
 * memberBhv.outideSql().traditionalStyle().selectPagedListOnly(path, pmb, entityType);
 * memberBhv.outideSql().traditionalStyle().selectCursor(path, pmb, handler);
 * memberBhv.outideSql().traditionalStyle().execute(path, pmb);
 *
 * <span style="color: #3F7E5E">// options</span> 
 * memberBhv.outideSql().removeBlockComment().selectList()
 * memberBhv.outideSql().removeLineComment().selectList()
 * memberBhv.outideSql().formatSql().selectList()
 * </pre>
 * @param <BEHAVIOR> The type of behavior.
 * @author jflute
 * @since 1.1.0 (2014/10/13)
 */
public class OutsideSqlAllFacadeExecutor<BEHAVIOR> {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The basic executor of outside-SQL. (NotNull) */
    protected final OutsideSqlBasicExecutor<BEHAVIOR> _basicExecutor;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public OutsideSqlAllFacadeExecutor(OutsideSqlBasicExecutor<BEHAVIOR> basicExecutor) {
        _basicExecutor = basicExecutor;
    }

    // ===================================================================================
    //                                                                       Entity Select
    //                                                                       =============
    /**
     * Select entity by the outside-SQL. <span style="color: #AD4747">{Typed Interface}</span><br>
     * You can call this method by only a typed parameter-bean
     * which is related to its own (outside-SQL) path and entity-type.
     * <pre>
     * SimpleMemberPmb pmb = new SimpleMemberPmb();
     * pmb.setMemberId(3);
     * SimpleMember member
     *     = memberBhv.outsideSql().<span style="color: #CC4747">selectEntity</span>(pmb);
     * if (member != null) {
     *     ... = member.get...();
     * } else {
     *     ...
     * }
     * </pre>
     * @param <ENTITY> The type of entity.
     * @param pmb The typed parameter-bean for entity handling. (NotNull)
     * @return The optional entity selected by the outside-SQL. (NotNull: if no data, returns empty entity)
     * @throws org.dbflute.exception.OutsideSqlNotFoundException When the outside-SQL is not found.
     * @throws org.dbflute.exception.EntityDuplicatedException When the entity is duplicated.
     */
    public <ENTITY> OptionalEntity<ENTITY> selectEntity(EntityHandlingPmb<BEHAVIOR, ENTITY> pmb) {
        return OptionalEntity.ofNullable(_basicExecutor.entityHandling().selectEntity(pmb), () -> {
            createBhvExThrower().throwSelectEntityAlreadyDeletedException(pmb);
        });
    }

    // ===================================================================================
    //                                                                         List Select
    //                                                                         ===========
    /**
     * Select the list of the entity by the outsideSql. <span style="color: #AD4747">{Typed Interface}</span><br>
     * You can call this method by only a typed parameter-bean
     * which is related to its own (outside-SQL) path and entity-type.
     * <pre>
     * SimpleMemberPmb pmb = new SimpleMemberPmb();
     * pmb.setMemberName_PrefixSearch("S");
     * ListResultBean&lt;SimpleMember&gt; memberList
     *     = memberBhv.outsideSql().<span style="color: #CC4747">selectList</span>(pmb);
     * for (SimpleMember member : memberList) {
     *     ... = member.get...();
     * }
     * </pre>
     * It needs to use customize-entity and parameter-bean.
     * The way to generate them is following:
     * <pre>
     * -- #df:entity#
     * -- !df:pmb!
     * -- !!Integer memberId!!
     * -- !!String memberName!!
     * -- !!...!!
     * </pre>
     * @param <ENTITY> The type of entity for element.
     * @param pmb The typed parameter-bean for list handling. (NotNull)
     * @return The result bean of selected list. (NotNull)
     * @throws org.dbflute.exception.OutsideSqlNotFoundException When the outsideSql is not found.
     * @throws org.dbflute.exception.DangerousResultSizeException When the result size is over the specified safety size.
     */
    public <ENTITY> ListResultBean<ENTITY> selectList(ListHandlingPmb<BEHAVIOR, ENTITY> pmb) {
        return _basicExecutor.selectList(pmb);
    }

    // ===================================================================================
    //                                                                       Paging Select
    //                                                                       =============
    /**
     * Select page by the outside-SQL. <span style="color: #AD4747">{Typed Interface}</span><br>
     * (both count-select and paging-select are executed)<br>
     * You can call this method by only a typed parameter-bean
     * which is related to its own (outside-SQL) path and entity-type.
     * <pre>
     * SimpleMemberPmb pmb = new SimpleMemberPmb();
     * pmb.setMemberName_PrefixSearch("S");
     * pmb.paging(20, 3); <span style="color: #3F7E5E">// 20 records per a page and current page number is 3</span>
     * PagingResultBean&lt;SimpleMember&gt; page
     *     = memberBhv.outsideSql().<span style="color: #CC4747">selectPage</span>(pmb);
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
     * <span style="color: #3F7E5E">-- !df:pmb extends Paging!</span>
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
     *  limit <span style="color: #3F7E5E">/*pmb.pageStartIndex&#42;/</span>80, <span style="color: #3F7E5E">/*pmb.fetchSize&#42;/</span>20
     *  <span style="color: #3F7E5E">/*END&#42;/</span>
     * </pre>
     * @param <ENTITY> The type of entity.
     * @param pmb The typed parameter-bean for paging handling. (NotNull)
     * @return The result bean of paging. (NotNull)
     * @throws org.dbflute.exception.OutsideSqlNotFoundException When the outside-SQL is not found.
     * @throws org.dbflute.exception.DangerousResultSizeException When the result size is over the specified safety size.
     */
    public <ENTITY> PagingResultBean<ENTITY> selectPage(PagingHandlingPmb<BEHAVIOR, ENTITY> pmb) {
        if (pmb instanceof ManualPagingHandlingPmb) {
            return _basicExecutor.manualPaging().selectPage((ManualPagingHandlingPmb<BEHAVIOR, ENTITY>) pmb);
        } else if (pmb instanceof AutoPagingHandlingPmb) {
            return _basicExecutor.autoPaging().selectPage((AutoPagingHandlingPmb<BEHAVIOR, ENTITY>) pmb);
        } else {
            String msg = "Unknown paging handling parameter-bean: " + pmb;
            throw new IllegalStateException(msg);
        }
    }

    /**
     * Select list with paging by the outside-SQL. <span style="color: #AD4747">{Typed Interface}</span><br>
     * (count-select is not executed, only paging-select)<br>
     * You can call this method by only a typed parameter-bean
     * which is related to its own (outside-SQL) path and entity-type.
     * <pre>
     * SimpleMemberPmb pmb = new SimpleMemberPmb();
     * pmb.setMemberName_PrefixSearch("S");
     * pmb.paging(20, 3); <span style="color: #3F7E5E">// 20 records per a page and current page number is 3</span>
     * ListResultBean&lt;SimpleMember&gt; memberList
     *     = memberBhv.outsideSql().<span style="color: #CC4747">selectPagedListOnly</span>(pmb);
     * for (SimpleMember member : memberList) {
     *     ... = member.get...();
     * }
     * </pre>
     * The parameter-bean needs to extend SimplePagingBean.
     * The way to generate it is following:
     * <pre>
     * <span style="color: #3F7E5E">-- !df:pmb extends Paging!</span>
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
     *  limit <span style="color: #3F7E5E">/*pmb.pageStartIndex&#42;/</span>80, <span style="color: #3F7E5E">/*pmb.fetchSize&#42;/</span>20
     * </pre>
     * @param <ENTITY> The type of entity.
     * @param pmb The typed parameter-bean for paging handling. (NotNull)
     * @return The result bean of paged list. (NotNull)
     * @throws org.dbflute.exception.OutsideSqlNotFoundException When the outside-SQL is not found.
     * @throws org.dbflute.exception.DangerousResultSizeException When the result size is over the specified safety size.
     */
    public <ENTITY> ListResultBean<ENTITY> selectPagedListOnly(PagingHandlingPmb<BEHAVIOR, ENTITY> pmb) {
        if (pmb instanceof ManualPagingHandlingPmb) {
            return _basicExecutor.manualPaging().selectList((ManualPagingHandlingPmb<BEHAVIOR, ENTITY>) pmb);
        } else if (pmb instanceof AutoPagingHandlingPmb) {
            return _basicExecutor.autoPaging().selectList((AutoPagingHandlingPmb<BEHAVIOR, ENTITY>) pmb);
        } else {
            String msg = "Unknown paging handling parameter-bean: " + pmb;
            throw new IllegalStateException(msg);
        }
    }

    // ===================================================================================
    //                                                                       Cursor Select
    //                                                                       =============
    /**
     * Select the cursor of the entity by outside-SQL. <span style="color: #AD4747">{Typed Interface}</span><br>
     * You can call this method by only a typed parameter-bean
     * which is related to its own (outside-SQL) path.
     * <pre>
     * SimpleMemberPmb pmb = new SimpleMemberPmb();
     * pmb.setMemberName_PrefixSearch("S");
     * memberBhv.outsideSql().<span style="color: #CC4747">selectCursor</span>(pmb, new PurchaseSummaryMemberCursorHandler() {
     *     protected Object fetchCursor(PurchaseSummaryMemberCursor cursor) throws SQLException {
     *         while (cursor.next()) {
     *             Integer memberId = cursor.getMemberId();
     *             String memberName = cursor.getMemberName();
     *             ...
     *         }
     *         return null;
     *     }
     * });
     * </pre>
     * It needs to use type-safe-cursor instead of customize-entity.
     * The way to generate it is following:
     * <pre>
     * <span style="color: #3F7E5E">-- #df:entity#</span>
     * <span style="color: #3F7E5E">-- +cursor+</span>
     * </pre>
     * @param <ENTITY> The type of entity, might be void.
     * @param pmb The typed parameter-bean for cursor handling. (NotNull)
     * @param handler The handler of cursor called back with result set. (NotNull)
     * @return The result object that the cursor handler returns. (NullAllowed)
     * @throws org.dbflute.exception.OutsideSqlNotFoundException When the outside-SQL is not found.
     */
    public <ENTITY> Object selectCursor(CursorHandlingPmb<BEHAVIOR, ENTITY> pmb, CursorHandler handler) {
        return _basicExecutor.cursorHandling().selectCursor(pmb, handler);
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    /**
     * Execute the outsideSql. (insert, update, delete, etc...) <span style="color: #AD4747">{Typed Interface}</span><br>
     * You can call this method by only a typed parameter-bean
     * which is related to its own (outside-SQL) path.
     * <pre>
     * SimpleMemberPmb pmb = new SimpleMemberPmb();
     * pmb.setMemberId(3);
     * int count = memberBhv.outsideSql().<span style="color: #CC4747">execute</span>(pmb);
     * </pre>
     * @param pmb The parameter-bean. Allowed types are Bean object and Map object. (NullAllowed)
     * @return The count of execution.
     * @throws org.dbflute.exception.OutsideSqlNotFoundException When the outsideSql is not found.
     */
    public int execute(ExecuteHandlingPmb<BEHAVIOR> pmb) {
        return _basicExecutor.execute(pmb);
    }

    // [DBFlute-0.7.5]
    // ===================================================================================
    //                                                                      Procedure Call
    //                                                                      ==============
    /**
     * Call the procedure.
     * <pre>
     * SpInOutParameterPmb pmb = new SpInOutParameterPmb();
     * pmb.setVInVarchar("foo");
     * pmb.setVInOutVarchar("bar");
     * memberBhv.outsideSql().<span style="color: #CC4747">call</span>(pmb);
     * String outVar = pmb.getVOutVarchar();
     * </pre>
     * It needs to use parameter-bean for procedure (ProcedurePmb).
     * The way to generate is to set the option of DBFlute property and execute Sql2Entity.
     * @param pmb The parameter-bean for procedure. (NotNull)
     */
    public void call(ProcedurePmb pmb) {
        _basicExecutor.call(pmb);
    }

    // [DBFlute-1.1.0]
    // ===================================================================================
    //                                                                   Traditional Style
    //                                                                   =================
    public OutsideSqlTraditionalExecutor<BEHAVIOR> traditionalStyle() {
        return new OutsideSqlTraditionalExecutor<BEHAVIOR>(_basicExecutor);
    }

    // ===================================================================================
    //                                                                              Option
    //                                                                              ======
    // -----------------------------------------------------
    //                                       Remove from SQL
    //                                       ---------------
    /**
     * Set up remove-block-comment for this outsideSql.
     * @return this. (NotNull)
     */
    public OutsideSqlAllFacadeExecutor<BEHAVIOR> removeBlockComment() {
        _basicExecutor.removeBlockComment();
        return this;
    }

    /**
     * Set up remove-line-comment for this outsideSql.
     * @return this. (NotNull)
     */
    public OutsideSqlAllFacadeExecutor<BEHAVIOR> removeLineComment() {
        _basicExecutor.removeLineComment();
        return this;
    }

    // -----------------------------------------------------
    //                                            Format SQL
    //                                            ----------
    /**
     * Set up format-SQL for this outsideSql. <br>
     * (For example, empty lines removed)
     * @return this. (NotNull)
     */
    public OutsideSqlAllFacadeExecutor<BEHAVIOR> formatSql() {
        _basicExecutor.formatSql();
        return this;
    }

    // -----------------------------------------------------
    //                                       StatementConfig
    //                                       ---------------
    /**
     * Configure statement JDBC options. e.g. queryTimeout, fetchSize, ... (only one-time call)
     * <pre>
     * <span style="color: #0000C0">memberBhv</span>.outsideSql().<span style="color: #CC4747">configure</span>(<span style="color: #553000">conf</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> <span style="color: #553000">conf</span>.<span style="color: #994747">queryTimeout</span>(<span style="color: #2A00FF">3</span>)).selectList(...);
     * </pre>
     * @param confLambda The callback for configuration of statement. (NotNull)
     * @return this. (NotNull)
     */
    public OutsideSqlAllFacadeExecutor<BEHAVIOR> configure(StatementConfigCall<StatementConfig> confLambda) {
        if (confLambda == null) {
            throw new IllegalArgumentException("The argument 'confLambda' should not be null.");
        }
        assertStatementConfigNotDuplicated(confLambda);
        _basicExecutor.configure(createStatementConfig(confLambda));
        return this;
    }

    protected void assertStatementConfigNotDuplicated(StatementConfigCall<StatementConfig> configCall) {
        final OutsideSqlOption option = _basicExecutor.getOutsideSqlOption();
        if (option != null) {
            final StatementConfig existingConfig = option.getStatementConfig();
            if (existingConfig != null) {
                String msg = "Already registered the configuration: existing=" + existingConfig + ", new=" + configCall;
                throw new IllegalConditionBeanOperationException(msg);
            }
        }
    }

    protected StatementConfig createStatementConfig(StatementConfigCall<StatementConfig> configCall) {
        if (configCall == null) {
            throw new IllegalArgumentException("The argument 'confLambda' should not be null.");
        }
        final StatementConfig config = newStatementConfig();
        configCall.callback(config);
        return config;
    }

    protected StatementConfig newStatementConfig() {
        return new StatementConfig();
    }

    // ===================================================================================
    //                                                                    Exception Helper
    //                                                                    ================
    protected BehaviorExceptionThrower createBhvExThrower() {
        return _basicExecutor.createBhvExThrower();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public OutsideSqlBasicExecutor<BEHAVIOR> xbasicExecutor() { // for compatible use
        return _basicExecutor;
    }
}
