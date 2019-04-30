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

import org.dbflute.bhv.core.BehaviorCommand;
import org.dbflute.bhv.core.BehaviorCommandInvoker;
import org.dbflute.bhv.core.command.AbstractOutsideSqlCommand;
import org.dbflute.bhv.core.command.OutsideSqlCallCommand;
import org.dbflute.bhv.core.command.OutsideSqlExecuteCommand;
import org.dbflute.bhv.core.command.OutsideSqlSelectListCommand;
import org.dbflute.bhv.exception.BehaviorExceptionThrower;
import org.dbflute.cbean.result.ListResultBean;
import org.dbflute.cbean.result.ResultBeanBuilder;
import org.dbflute.dbway.DBDef;
import org.dbflute.exception.FetchingOverSafetySizeException;
import org.dbflute.jdbc.FetchBean;
import org.dbflute.jdbc.StatementConfig;
import org.dbflute.outsidesql.OutsideSqlFilter;
import org.dbflute.outsidesql.OutsideSqlOption;
import org.dbflute.outsidesql.ProcedurePmb;
import org.dbflute.outsidesql.factory.OutsideSqlContextFactory;
import org.dbflute.outsidesql.factory.OutsideSqlExecutorFactory;
import org.dbflute.outsidesql.typed.ExecuteHandlingPmb;
import org.dbflute.outsidesql.typed.ListHandlingPmb;
import org.dbflute.outsidesql.typed.TypedParameterBean;

/**
 * The basic executor of outside-SQL.
 * @param <BEHAVIOR> The type of behavior.
 * @author jflute
 */
public class OutsideSqlBasicExecutor<BEHAVIOR> {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The invoker of behavior command. (NotNull) */
    protected final BehaviorCommandInvoker _behaviorCommandInvoker;

    /** Table DB name. (NotNull) */
    protected final String _tableDbName;

    /** The current database definition. (NotNull) */
    protected final DBDef _currentDBDef;

    /** The option of outside-SQL. (NotNull) */
    protected final OutsideSqlOption _outsideSqlOption;

    /** The factory of outside-SQL context. (NotNull) */
    protected final OutsideSqlContextFactory _outsideSqlContextFactory;

    /** The filter of outside-SQL. (NullAllowed) */
    protected final OutsideSqlFilter _outsideSqlFilter;

    /** The factory of outside-SQL executor. (NotNull) */
    protected final OutsideSqlExecutorFactory _outsideSqlExecutorFactory;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public OutsideSqlBasicExecutor(BehaviorCommandInvoker behaviorCommandInvoker, String tableDbName, DBDef currentDBDef,
            OutsideSqlOption outsideSqlOption, OutsideSqlContextFactory outsideSqlContextFactory, OutsideSqlFilter outsideSqlFilter,
            OutsideSqlExecutorFactory outsideSqlExecutorFactory) {
        _behaviorCommandInvoker = behaviorCommandInvoker;
        _tableDbName = tableDbName;
        _currentDBDef = currentDBDef;
        if (outsideSqlOption != null) { // for nested call (inherits options)
            _outsideSqlOption = outsideSqlOption;
        } else { // for entry call (initializes an option instance)
            _outsideSqlOption = new OutsideSqlOption();
            _outsideSqlOption.setTableDbName(tableDbName); // as information
        }
        _outsideSqlContextFactory = outsideSqlContextFactory;
        _outsideSqlFilter = outsideSqlFilter;
        _outsideSqlExecutorFactory = outsideSqlExecutorFactory;
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
        assertTypedPmbNotNull(pmb);
        return doSelectList(pmb.getOutsideSqlPath(), pmb, pmb.getEntityType());
    }

    /**
     * Select the list of the entity by the outsideSql. {FreeStyle Interface}<br>
     * This method can accept each element: path, parameter-bean(Object type), entity-type.
     * <pre>
     * String path = MemberBhv.PATH_selectSimpleMember;
     * SimpleMemberPmb pmb = new SimpleMemberPmb();
     * pmb.setMemberName_PrefixSearch("S");
     * Class&lt;SimpleMember&gt; entityType = SimpleMember.class;
     * ListResultBean&lt;SimpleMember&gt; memberList
     *     = memberBhv.outsideSql().<span style="color: #CC4747">selectList</span>(path, pmb, entityType);
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
     * @throws org.dbflute.exception.OutsideSqlNotFoundException When the outsideSql is not found.
     * @throws org.dbflute.exception.DangerousResultSizeException When the result size is over the specified safety size.
     */
    public <ENTITY> ListResultBean<ENTITY> selectList(String path, Object pmb, Class<ENTITY> entityType) {
        return doSelectList(path, pmb, entityType);
    }

    protected <ENTITY> ListResultBean<ENTITY> doSelectList(String path, Object pmb, Class<ENTITY> entityType) {
        if (path == null) {
            String msg = "The argument 'path' of outside-SQL should not be null.";
            throw new IllegalArgumentException(msg);
        }
        if (entityType == null) {
            String msg = "The argument 'entityType' for result should not be null: path=" + path;
            throw new IllegalArgumentException(msg);
        }
        try {
            List<ENTITY> resultList = invoke(createSelectListCommand(path, pmb, entityType));
            return createListResultBean(resultList);
        } catch (FetchingOverSafetySizeException e) { // occurs only when fetch-bean
            throwDangerousResultSizeException(pmb, e);
            return null; // unreachable
        }
    }

    protected <ENTITY> ListResultBean<ENTITY> createListResultBean(List<ENTITY> selectedList) {
        return new ResultBeanBuilder<ENTITY>(_tableDbName).buildListSimply(selectedList);
    }

    protected void throwDangerousResultSizeException(Object pmb, FetchingOverSafetySizeException e) {
        if (!(pmb instanceof FetchBean)) { // no way
            String msg = "The exception should be thrown only when the parameter-bean is instance of fetch-bean:";
            msg = msg + " pmb=" + (pmb != null ? pmb.getClass().getName() : null);
            throw new IllegalStateException(msg, e);
        }
        createBhvExThrower().throwDangerousResultSizeException((FetchBean) pmb, e);
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
        assertTypedPmbNotNull(pmb);
        return doExecute(pmb.getOutsideSqlPath(), pmb);
    }

    /**
     * Execute the outsideSql. (insert, update, delete, etc...) {FreeStyle Interface}<br>
     * This method can accept each element: path, parameter-bean(Object type).
     * <pre>
     * String path = MemberBhv.PATH_selectSimpleMember;
     * SimpleMemberPmb pmb = new SimpleMemberPmb();
     * pmb.setMemberId(3);
     * int count = memberBhv.outsideSql().<span style="color: #CC4747">execute</span>(path, pmb);
     * </pre>
     * @param path The path of SQL file. (NotNull)
     * @param pmb The parameter-bean. Allowed types are Bean object and Map object. (NullAllowed)
     * @return The count of execution.
     * @throws org.dbflute.exception.OutsideSqlNotFoundException When the outsideSql is not found.
     */
    public int execute(String path, Object pmb) {
        return doExecute(path, pmb);
    }

    protected int doExecute(String path, Object pmb) {
        if (path == null) {
            String msg = "The argument 'path' of outside-SQL should not be null.";
            throw new IllegalArgumentException(msg);
        }
        return invoke(createExecuteCommand(path, pmb));
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
        if (pmb == null) {
            throw new IllegalArgumentException("The argument 'pmb' of procedure should not be null.");
        }
        try {
            invoke(createCallCommand(pmb.getProcedureName(), pmb));
        } catch (FetchingOverSafetySizeException e) { // occurs only when fetch-bean
            throwDangerousResultSizeException(pmb, e);
        }
    }

    // ===================================================================================
    //                                                                    Behavior Command
    //                                                                    ================
    protected <ENTITY> BehaviorCommand<List<ENTITY>> createSelectListCommand(String path, Object pmb, Class<ENTITY> entityType) {
        final OutsideSqlSelectListCommand<ENTITY> cmd;
        {
            final OutsideSqlSelectListCommand<ENTITY> newed = newOutsideSqlSelectListCommand();
            cmd = xsetupCommand(newed, path, pmb); // has a little generic headache...
        }
        cmd.setEntityType(entityType);
        return cmd;
    }

    protected <ENTITY> OutsideSqlSelectListCommand<ENTITY> newOutsideSqlSelectListCommand() {
        return new OutsideSqlSelectListCommand<ENTITY>();
    }

    protected BehaviorCommand<Integer> createExecuteCommand(String path, Object pmb) {
        return xsetupCommand(newOutsideSqlExecuteCommand(), path, pmb);
    }

    protected OutsideSqlExecuteCommand newOutsideSqlExecuteCommand() {
        return new OutsideSqlExecuteCommand();
    }

    protected BehaviorCommand<Void> createCallCommand(String path, Object pmb) {
        return xsetupCommand(newOutsideSqlCallCommand(), path, pmb);
    }

    protected OutsideSqlCallCommand newOutsideSqlCallCommand() {
        return new OutsideSqlCallCommand();
    }

    protected <COMMAND extends AbstractOutsideSqlCommand<?>> COMMAND xsetupCommand(COMMAND cmd, String path, Object pmb) {
        cmd.setTableDbName(_tableDbName);
        _behaviorCommandInvoker.injectComponentProperty(cmd);
        cmd.setOutsideSqlPath(path);
        cmd.setParameterBean(pmb);
        cmd.setOutsideSqlOption(_outsideSqlOption);
        cmd.setCurrentDBDef(_currentDBDef);
        cmd.setOutsideSqlContextFactory(_outsideSqlContextFactory);
        cmd.setOutsideSqlFilter(_outsideSqlFilter);
        return cmd;
    }

    /**
     * Invoke the command of behavior.
     * @param <RESULT> The type of result.
     * @param behaviorCommand The command of behavior. (NotNull)
     * @return The instance of result. (NullAllowed)
     */
    protected <RESULT> RESULT invoke(BehaviorCommand<RESULT> behaviorCommand) {
        return _behaviorCommandInvoker.invoke(behaviorCommand);
    }

    // ===================================================================================
    //                                                                      Executor Chain
    //                                                                      ==============
    // -----------------------------------------------------
    //                                       Entity Handling
    //                                       ---------------
    /**
     * Prepare entity handling.
     * <pre>
     * memberBhv.outsideSql().<span style="color: #CC4747">entityHandling()</span>.selectEntityWithDeletedCheck(pmb);
     * </pre>
     * @return The cursor executor of outsideSql. (NotNull)
     */
    public OutsideSqlEntityExecutor<BEHAVIOR> entityHandling() {
        return createOutsideSqlEntityExecutor();
    }

    protected OutsideSqlEntityExecutor<BEHAVIOR> createOutsideSqlEntityExecutor() {
        return _outsideSqlExecutorFactory.createEntity(_behaviorCommandInvoker, _tableDbName, _currentDBDef, _outsideSqlOption);
    }

    // -----------------------------------------------------
    //                                       Paging Handling
    //                                       ---------------
    /**
     * Prepare the paging as manual-paging.
     * <pre>
     * memberBhv.outsideSql().<span style="color: #CC4747">manualPaging()</span>.selectPage(pmb);
     * </pre>
     * If you call this, you need to write paging condition on your SQL.
     * <pre>
     * e.g. MySQL
     * select member.MEMBER_ID, member...
     *   from Member member
     *  where ...
     *  order by ...
     *  limit 40, 20 <span style="color: #3F7E5E">-- is necessary!</span>
     * </pre>
     * @return The executor of paging that the paging mode is manual. (NotNull)
     */
    public OutsideSqlManualPagingExecutor<BEHAVIOR> manualPaging() {
        _outsideSqlOption.manualPaging();
        return createOutsideSqlManualPagingExecutor();
    }

    protected OutsideSqlManualPagingExecutor<BEHAVIOR> createOutsideSqlManualPagingExecutor() {
        return _outsideSqlExecutorFactory.createManualPaging(_behaviorCommandInvoker, _tableDbName, _currentDBDef, _outsideSqlOption);
    }

    /**
     * Prepare the paging as auto-paging.
     * <pre>
     * memberBhv.outsideSql().<span style="color: #CC4747">autoPaging()</span>.selectPage(pmb);
     * </pre>
     * If you call this, you don't need to write paging condition on your SQL.
     * <pre>
     * e.g. MySQL
     * select member.MEMBER_ID, member...
     *   from Member member
     *  where ...
     *  order by ...
     * <span style="color: #3F7E5E">-- limit 40, 20 -- is unnecessary!</span>
     * </pre>
     * @return The executor of paging that the paging mode is auto. (NotNull)
     */
    public OutsideSqlAutoPagingExecutor<BEHAVIOR> autoPaging() {
        _outsideSqlOption.autoPaging();
        return createOutsideSqlAutoPagingExecutor();
    }

    protected OutsideSqlAutoPagingExecutor<BEHAVIOR> createOutsideSqlAutoPagingExecutor() {
        return _outsideSqlExecutorFactory.createAutoPaging(_behaviorCommandInvoker, _tableDbName, _currentDBDef, _outsideSqlOption);
    }

    // -----------------------------------------------------
    //                                       Cursor Handling
    //                                       ---------------
    /**
     * Prepare cursor handling.
     * <pre>
     * memberBhv.outsideSql().<span style="color: #CC4747">cursorHandling()</span>.selectCursor(pmb);
     * </pre>
     * @return The cursor executor of outsideSql. (NotNull)
     */
    public OutsideSqlCursorExecutor<BEHAVIOR> cursorHandling() {
        return createOutsideSqlCursorExecutor();
    }

    protected OutsideSqlCursorExecutor<BEHAVIOR> createOutsideSqlCursorExecutor() {
        return _outsideSqlExecutorFactory.createCursor(_behaviorCommandInvoker, _tableDbName, _currentDBDef, _outsideSqlOption);
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
    public OutsideSqlBasicExecutor<BEHAVIOR> removeBlockComment() {
        _outsideSqlOption.removeBlockComment();
        return this;
    }

    /**
     * Set up remove-line-comment for this outsideSql.
     * @return this. (NotNull)
     */
    public OutsideSqlBasicExecutor<BEHAVIOR> removeLineComment() {
        _outsideSqlOption.removeLineComment();
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
    public OutsideSqlBasicExecutor<BEHAVIOR> formatSql() {
        _outsideSqlOption.formatSql();
        return this;
    }

    // -----------------------------------------------------
    //                                       StatementConfig
    //                                       ---------------
    /**
     * Configure statement JDBC options. (For example, queryTimeout, fetchSize, ...)
     * @param statementConfig The configuration of statement. (NotNull)
     * @return this. (NotNull)
     */
    public OutsideSqlBasicExecutor<BEHAVIOR> configure(StatementConfig statementConfig) {
        if (statementConfig == null) {
            throw new IllegalArgumentException("The argument 'statementConfig' should not be null.");
        }
        _outsideSqlOption.setStatementConfig(statementConfig);
        return this;
    }

    // ===================================================================================
    //                                                                    Exception Helper
    //                                                                    ================
    public BehaviorExceptionThrower createBhvExThrower() { // public for facade
        return _behaviorCommandInvoker.createBehaviorExceptionThrower();
    }

    // ===================================================================================
    //                                                                       Assert Helper
    //                                                                       =============
    protected <ENTITY> void assertTypedPmbNotNull(TypedParameterBean<BEHAVIOR> pmb) {
        if (pmb == null) {
            String msg = "The argument 'pmb' (typed parameter-bean) should not be null.";
            throw new IllegalArgumentException(msg);
        }
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public OutsideSqlOption getOutsideSqlOption() {
        return _outsideSqlOption;
    }
}
