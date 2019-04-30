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
package org.dbflute.outsidesql.executor;

import java.util.List;

import org.dbflute.bhv.core.BehaviorCommandInvoker;
import org.dbflute.bhv.exception.BehaviorExceptionThrower;
import org.dbflute.cbean.paging.PagingBean;
import org.dbflute.cbean.paging.PagingHandler;
import org.dbflute.cbean.paging.PagingInvoker;
import org.dbflute.cbean.result.ListResultBean;
import org.dbflute.cbean.result.PagingResultBean;
import org.dbflute.dbway.DBDef;
import org.dbflute.exception.EntityDuplicatedException;
import org.dbflute.exception.FetchingOverSafetySizeException;
import org.dbflute.exception.PagingOverSafetySizeException;
import org.dbflute.jdbc.StatementConfig;
import org.dbflute.outsidesql.OutsideSqlOption;
import org.dbflute.outsidesql.factory.OutsideSqlExecutorFactory;

/**
 * The paging executor of outside-SQL.
 * @param <BEHAVIOR> The type of behavior.
 * @author jflute
 */
public abstract class AbstractOutsideSqlPagingExecutor<BEHAVIOR> {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The invoker of behavior command. (NotNull) */
    protected final BehaviorCommandInvoker _behaviorCommandInvoker;

    /** The DB name of table. (NotNull) */
    protected final String _tableDbName;

    /** The current database definition. (NotNull) */
    protected final DBDef _currentDBDef;

    /** The option of outside-SQL. (NotNull) */
    protected final OutsideSqlOption _outsideSqlOption;

    /** The factory of outside-SQL executor. (NotNull) */
    protected final OutsideSqlExecutorFactory _outsideSqlExecutorFactory;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public AbstractOutsideSqlPagingExecutor(BehaviorCommandInvoker behaviorCommandInvoker, String tableDbName, DBDef currentDBDef,
            OutsideSqlOption outsideSqlOption, OutsideSqlExecutorFactory outsideSqlExecutorFactory) {
        _behaviorCommandInvoker = behaviorCommandInvoker;
        _tableDbName = tableDbName;
        _currentDBDef = currentDBDef;
        _outsideSqlOption = outsideSqlOption;
        _outsideSqlExecutorFactory = outsideSqlExecutorFactory;
    }

    // ===================================================================================
    //                                                                                Page
    //                                                                                ====
    /**
     * Select page by the outside-SQL. {FreeStyle Interface}<br>
     * (both count-select and paging-select are executed)<br>
     * This method can accept each element: path, parameter-bean(Object type), entity-type.
     * <pre>
     * String path = MemberBhv.PATH_selectSimpleMember;
     * SimpleMemberPmb pmb = new SimpleMemberPmb();
     * pmb.setMemberName_PrefixSearch("S");
     * pmb.paging(20, 3); <span style="color: #3F7E5E">// 20 records per a page and current page number is 3</span>
     * Class&lt;SimpleMember&gt; entityType = SimpleMember.class;
     * PagingResultBean&lt;SimpleMember&gt; page
     *     = memberBhv.outsideSql().manualPaging().<span style="color: #CC4747">selectPage</span>(path, pmb, entityType);
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
     * @param path The path of SQL that executes count and paging. (NotNull)
     * @param pmb The bean of paging parameter. (NotNull)
     * @param entityType The type of result entity. (NotNull)
     * @return The result bean of paging. (NotNull)
     * @throws org.dbflute.exception.OutsideSqlNotFoundException When the outside-SQL is not found.
     * @throws org.dbflute.exception.DangerousResultSizeException When the result size is over the specified safety size.
     */
    public <ENTITY> PagingResultBean<ENTITY> selectPage(String path, PagingBean pmb, Class<ENTITY> entityType) {
        return doSelectPage(path, pmb, entityType);
    }

    protected <ENTITY> PagingResultBean<ENTITY> doSelectPage(String path, PagingBean pmb, Class<ENTITY> entityType) {
        if (path == null) {
            String msg = "The argument 'path' of outside-SQL should not be null.";
            throw new IllegalArgumentException(msg);
        }
        if (entityType == null) {
            String msg = "The argument 'entityType' for result should not be null: path=" + path;
            throw new IllegalArgumentException(msg);
        }
        try {
            final PagingHandler<ENTITY> handler = createPagingHandler(path, pmb, entityType);
            final PagingInvoker<ENTITY> invoker = createPagingInvoker(pmb);
            return invoker.invokePaging(handler);
        } catch (PagingOverSafetySizeException e) {
            createBhvExThrower().throwDangerousResultSizeException(pmb, e);
            return null; // unreachable
        }
    }

    protected <ENTITY> PagingHandler<ENTITY> createPagingHandler(final String path, final PagingBean pmb, final Class<ENTITY> entityType) {
        final OutsideSqlEntityExecutor<BEHAVIOR> countExecutor = createCountExecutor();
        return new PagingHandler<ENTITY>() {
            public PagingBean getPagingBean() {
                return pmb;
            }

            public int count() {
                pmb.xsetPaging(false);
                try {
                    return countExecutor.selectEntityWithDeletedCheck(path, pmb, Integer.class);
                } catch (EntityDuplicatedException e) { // means switching the select clause failed
                    throwPagingCountSelectNotCountException(path, pmb, entityType, e);
                    return -1; // unreachable
                }
            }

            public List<ENTITY> paging() {
                pmb.xsetPaging(true);
                return doSelectList(path, pmb, entityType);
            }
        };
    }

    protected OutsideSqlEntityExecutor<BEHAVIOR> createCountExecutor() {
        final OutsideSqlOption countOption = _outsideSqlOption.copyOptionForPagingCount();
        return _outsideSqlExecutorFactory.createEntity(_behaviorCommandInvoker, _tableDbName, _currentDBDef, countOption);
    }

    protected <ENTITY> void throwPagingCountSelectNotCountException(String path, PagingBean pmb, Class<ENTITY> entityType,
            EntityDuplicatedException e) {
        createBhvExThrower().throwPagingCountSelectNotCountException(_tableDbName, path, pmb, entityType, e);
    }

    protected <ENTITY> PagingInvoker<ENTITY> createPagingInvoker(PagingBean pmb) {
        return pmb.createPagingInvoker(_tableDbName);
    }

    protected void setupScrollableCursorIfNeeds() {
        if (!_outsideSqlOption.isAutoPaging()) {
            return;
        }
        StatementConfig statementConfig = _outsideSqlOption.getStatementConfig();
        if (statementConfig != null && statementConfig.hasResultSetType()) {
            return;
        }
        if (statementConfig == null) {
            statementConfig = new StatementConfig();
            configure(statementConfig);
        }
        if (_currentDBDef.dbway().isScrollableCursorSupported()) {
            statementConfig.typeScrollInsensitive();
        } else {
            statementConfig.typeForwardOnly();
        }
    }

    // ===================================================================================
    //                                                                                List
    //                                                                                ====
    /**
     * Select list with paging by the outside-SQL. {FreeStyle Interface}<br>
     * (count-select is not executed, only paging-select)<br>
     * This method can accept each element: path, parameter-bean(Object type), entity-type.
     * <pre>
     * String path = MemberBhv.PATH_selectSimpleMember;
     * SimpleMemberPmb pmb = new SimpleMemberPmb();
     * pmb.setMemberName_PrefixSearch("S");
     * pmb.paging(20, 3); <span style="color: #3F7E5E">// 20 records per a page and current page number is 3</span>
     * Class&lt;SimpleMember&gt; entityType = SimpleMember.class;
     * ListResultBean&lt;SimpleMember&gt; memberList
     *     = memberBhv.outsideSql().manualPaging().<span style="color: #CC4747">selectList</span>(path, pmb, entityType);
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
     * @throws org.dbflute.exception.OutsideSqlNotFoundException When the outside-SQL is not found.
     * @throws org.dbflute.exception.DangerousResultSizeException When the result size is over the specified safety size.
     */
    public <ENTITY> ListResultBean<ENTITY> selectList(String path, PagingBean pmb, Class<ENTITY> entityType) {
        return doSelectList(path, pmb, entityType);
    }

    protected <ENTITY> ListResultBean<ENTITY> doSelectList(String path, PagingBean pmb, Class<ENTITY> entityType) {
        if (path == null) {
            String msg = "The argument 'path' of outside-SQL should not be null.";
            throw new IllegalArgumentException(msg);
        }
        if (entityType == null) {
            String msg = "The argument 'entityType' for result should not be null: path=" + path;
            throw new IllegalArgumentException(msg);
        }
        setupScrollableCursorIfNeeds();
        try {
            return createBasicExecutor().selectList(path, pmb, entityType);
        } catch (FetchingOverSafetySizeException e) {
            createBhvExThrower().throwDangerousResultSizeException(pmb, e);
            return null; // unreachable
        }
    }

    protected OutsideSqlBasicExecutor<BEHAVIOR> createBasicExecutor() {
        return _outsideSqlExecutorFactory.createBasic(_behaviorCommandInvoker, _tableDbName, _currentDBDef, _outsideSqlOption);
    }

    // ===================================================================================
    //                                                                              Option
    //                                                                              ======
    /**
     * Set up remove-block-comment for this outside-SQL.
     * @return this. (NotNull)
     */
    public AbstractOutsideSqlPagingExecutor<BEHAVIOR> removeBlockComment() {
        _outsideSqlOption.removeBlockComment();
        return this;
    }

    /**
     * Set up remove-line-comment for this outside-SQL.
     * @return this. (NotNull)
     */
    public AbstractOutsideSqlPagingExecutor<BEHAVIOR> removeLineComment() {
        _outsideSqlOption.removeLineComment();
        return this;
    }

    /**
     * Set up format-SQL for this outside-SQL. <br>
     * (For example, empty lines removed)
     * @return this. (NotNull)
     */
    public AbstractOutsideSqlPagingExecutor<BEHAVIOR> formatSql() {
        _outsideSqlOption.formatSql();
        return this;
    }

    /**
     * Configure statement JDBC options. (For example, queryTimeout, fetchSize, ...)
     * @param statementConfig The configuration of statement. (NullAllowed)
     * @return this. (NotNull)
     */
    public AbstractOutsideSqlPagingExecutor<BEHAVIOR> configure(StatementConfig statementConfig) {
        _outsideSqlOption.setStatementConfig(statementConfig);
        return this;
    }

    // ===================================================================================
    //                                                                    Exception Helper
    //                                                                    ================
    protected BehaviorExceptionThrower createBhvExThrower() {
        return _behaviorCommandInvoker.createBehaviorExceptionThrower();
    }
}
