/*
 * Copyright 2014-2014 the original author or authors.
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

import org.dbflute.cbean.paging.PagingBean;
import org.dbflute.cbean.result.ListResultBean;
import org.dbflute.cbean.result.PagingResultBean;
import org.dbflute.exception.EntityAlreadyDeletedException;
import org.dbflute.jdbc.CursorHandler;
import org.dbflute.optional.OptionalEntity;
import org.dbflute.outsidesql.typed.AutoPagingHandlingPmb;
import org.dbflute.outsidesql.typed.ManualPagingHandlingPmb;

/**
 * The traditional executor of outside-SQL.
 * @param <BEHAVIOR> The type of behavior.
 * @author jflute
 * @since 1.1.0 (2014/10/13 Monday)
 */
public class OutsideSqlTraditionalExecutor<BEHAVIOR> {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The basic executor. (NotNull) */
    protected final OutsideSqlBasicExecutor<BEHAVIOR> _basicExecutor;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public OutsideSqlTraditionalExecutor(OutsideSqlBasicExecutor<BEHAVIOR> basicExecutor) {
        _basicExecutor = basicExecutor;
    }

    // ===================================================================================
    //                                                                       Entity Select
    //                                                                       =============
    /**
     * Select entity by the outside-SQL. {FreeStyle Interface}<br />
     * This method can accept each element: path, parameter-bean(Object type), entity-type.
     * <pre>
     * String path = MemberBhv.PATH_selectSimpleMember;
     * SimpleMemberPmb pmb = new SimpleMemberPmb();
     * pmb.setMemberId(3);
     * Class&lt;SimpleMember&gt; entityType = SimpleMember.class;
     * SimpleMember member
     *     = memberBhv.outsideSql().entityHandling().<span style="color: #DD4747">selectEntity</span>(path, pmb, entityType);
     * if (member != null) {
     *     ... = member.get...();
     * } else {
     *     ...
     * }
     * </pre>
     * @param <ENTITY> The type of entity.
     * @param path The path of SQL file. (NotNull)
     * @param pmb The object as parameter-bean. Allowed types are Bean object and Map object. (NullAllowed)
     * @param entityType The type of entity. (NotNull)
     * @return The selected entity. (NullAllowed)
     * @exception org.dbflute.exception.OutsideSqlNotFoundException When the outside-SQL is not found.
     * @exception org.dbflute.exception.EntityDuplicatedException When the entity is duplicated.
     */
    public <ENTITY> OptionalEntity<ENTITY> selectEntity(String path, Object pmb, Class<ENTITY> entityType) {
        return OptionalEntity.ofNullable(_basicExecutor.entityHandling().selectEntity(path, pmb, entityType), () -> {
            // TODO jflute OutsideSql optional exception
                throw new EntityAlreadyDeletedException("TODO jflute");
            });
    }

    // ===================================================================================
    //                                                                         List Select
    //                                                                         ===========
    /**
     * Select the list of the entity by the outsideSql. {FreeStyle Interface}<br />
     * This method can accept each element: path, parameter-bean(Object type), entity-type.
     * <pre>
     * String path = MemberBhv.PATH_selectSimpleMember;
     * SimpleMemberPmb pmb = new SimpleMemberPmb();
     * pmb.setMemberName_PrefixSearch("S");
     * Class&lt;SimpleMember&gt; entityType = SimpleMember.class;
     * ListResultBean&lt;SimpleMember&gt; memberList
     *     = memberBhv.outsideSql().<span style="color: #DD4747">selectList</span>(path, pmb, entityType);
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
     * @param path The path of SQL file. (NotNull)
     * @param pmb The object as parameter-bean. Allowed types are Bean object and Map object. (NullAllowed)
     * @param entityType The element type of entity. (NotNull)
     * @return The result bean of selected list. (NotNull)
     * @exception org.dbflute.exception.OutsideSqlNotFoundException When the outsideSql is not found.
     * @exception org.dbflute.exception.DangerousResultSizeException When the result size is over the specified safety size.
     */
    public <ENTITY> ListResultBean<ENTITY> selectList(String path, Object pmb, Class<ENTITY> entityType) {
        return _basicExecutor.selectList(path, pmb, entityType);
    }

    // ===================================================================================
    //                                                                       Paging Select
    //                                                                       =============
    /**
     * Select page by the outside-SQL. {FreeStyle Interface}<br />
     * (both count-select and paging-select are executed)<br />
     * This method can accept each element: path, parameter-bean(Object type), entity-type.
     * <pre>
     * String path = MemberBhv.PATH_selectSimpleMember;
     * SimpleMemberPmb pmb = new SimpleMemberPmb();
     * pmb.setMemberName_PrefixSearch("S");
     * pmb.paging(20, 3); <span style="color: #3F7E5E">// 20 records per a page and current page number is 3</span>
     * Class&lt;SimpleMember&gt; entityType = SimpleMember.class;
     * PagingResultBean&lt;SimpleMember&gt; page
     *     = memberBhv.outsideSql().manualPaging().<span style="color: #DD4747">selectPage</span>(path, pmb, entityType);
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
     *  limit <span style="color: #3F7E5E">/*$pmb.pageStartIndex&#42;/</span>80, <span style="color: #3F7E5E">/*$pmb.fetchSize&#42;/</span>20
     *  <span style="color: #3F7E5E">/*END&#42;/</span>
     * </pre>
     * @param <ENTITY> The type of entity.
     * @param path The path of SQL that executes count and paging. (NotNull)
     * @param pmb The bean of paging parameter. (NotNull)
     * @param entityType The type of result entity. (NotNull)
     * @return The result bean of paging. (NotNull)
     * @exception org.dbflute.exception.OutsideSqlNotFoundException When the outside-SQL is not found.
     * @exception org.dbflute.exception.DangerousResultSizeException When the result size is over the specified safety size.
     */
    public <ENTITY> PagingResultBean<ENTITY> selectPage(String path, PagingBean pmb, Class<ENTITY> entityType) {
        if (pmb instanceof ManualPagingHandlingPmb) {
            return _basicExecutor.manualPaging().selectPage(path, pmb, entityType);
        } else if (pmb instanceof AutoPagingHandlingPmb) {
            return _basicExecutor.autoPaging().selectPage(path, pmb, entityType);
        } else {
            String msg = "Unknown paging handling parameter-bean: " + pmb;
            throw new IllegalStateException(msg);
        }
    }

    /**
     * Select list with paging by the outside-SQL. {FreeStyle Interface}<br />
     * (count-select is not executed, only paging-select)<br />
     * This method can accept each element: path, parameter-bean(Object type), entity-type.
     * <pre>
     * String path = MemberBhv.PATH_selectSimpleMember;
     * SimpleMemberPmb pmb = new SimpleMemberPmb();
     * pmb.setMemberName_PrefixSearch("S");
     * pmb.paging(20, 3); <span style="color: #3F7E5E">// 20 records per a page and current page number is 3</span>
     * Class&lt;SimpleMember&gt; entityType = SimpleMember.class;
     * ListResultBean&lt;SimpleMember&gt; memberList
     *     = memberBhv.outsideSql().manualPaging().<span style="color: #DD4747">selectList</span>(path, pmb, entityType);
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
     * @param path The path of SQL that executes count and paging. (NotNull)
     * @param pmb The bean of paging parameter. (NotNull)
     * @param entityType The type of result entity. (NotNull)
     * @return The result bean of paged list. (NotNull)
     * @exception org.dbflute.exception.OutsideSqlNotFoundException When the outside-SQL is not found.
     * @exception org.dbflute.exception.DangerousResultSizeException When the result size is over the specified safety size.
     */
    public <ENTITY> ListResultBean<ENTITY> selectPagedListOnly(String path, PagingBean pmb, Class<ENTITY> entityType) {
        if (pmb instanceof ManualPagingHandlingPmb) {
            return _basicExecutor.manualPaging().selectPage(path, pmb, entityType);
        } else if (pmb instanceof AutoPagingHandlingPmb) {
            return _basicExecutor.autoPaging().selectPage(path, pmb, entityType);
        } else {
            String msg = "Unknown paging handling parameter-bean: " + pmb;
            throw new IllegalStateException(msg);
        }
    }

    // ===================================================================================
    //                                                                       Cursor Select
    //                                                                       =============
    /**
     * Select the cursor of the entity by outside-SQL. {FreeStyle Interface}<br />
     * This method can accept each element: path, parameter-bean(Object type), cursor-handler.
     * <pre>
     * String path = MemberBhv.PATH_selectSimpleMember;
     * SimpleMemberPmb pmb = new SimpleMemberPmb();
     * pmb.setMemberName_PrefixSearch("S");
     * memberBhv.outsideSql().cursorHandling()
     *         .<span style="color: #DD4747">selectCursor</span>(path, pmb, new PurchaseSummaryMemberCursorHandler() {
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
     * @param path The path of SQL file. (NotNull)
     * @param pmb The object as parameter-bean. Allowed types are Bean object and Map object. (NullAllowed)
     * @param handler The handler of cursor called back with result set. (NotNull)
     * @return The result object that the cursor handler returns. (NullAllowed)
     * @exception org.dbflute.exception.OutsideSqlNotFoundException When the outside-SQL is not found.
     */
    public Object selectCursor(String path, Object pmb, CursorHandler handler) {
        return _basicExecutor.cursorHandling().selectCursor(path, pmb, handler);
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    /**
     * Execute the outsideSql. (insert, update, delete, etc...) {FreeStyle Interface}<br />
     * This method can accept each element: path, parameter-bean(Object type).
     * <pre>
     * String path = MemberBhv.PATH_selectSimpleMember;
     * SimpleMemberPmb pmb = new SimpleMemberPmb();
     * pmb.setMemberId(3);
     * int count = memberBhv.outsideSql().<span style="color: #DD4747">execute</span>(path, pmb);
     * </pre>
     * @param path The path of SQL file. (NotNull)
     * @param pmb The parameter-bean. Allowed types are Bean object and Map object. (NullAllowed)
     * @return The count of execution.
     * @exception org.dbflute.exception.OutsideSqlNotFoundException When the outsideSql is not found.
     */
    public int execute(String path, Object pmb) {
        return _basicExecutor.execute(path, pmb);
    }
}
