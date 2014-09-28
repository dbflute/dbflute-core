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

import java.util.List;

import org.seasar.dbflute.DBDef;
import org.seasar.dbflute.bhv.core.BehaviorCommandInvoker;
import org.seasar.dbflute.exception.DangerousResultSizeException;
import org.seasar.dbflute.exception.thrower.BehaviorExceptionThrower;
import org.seasar.dbflute.jdbc.FetchBean;
import org.seasar.dbflute.jdbc.StatementConfig;
import org.seasar.dbflute.outsidesql.OutsideSqlOption;
import org.seasar.dbflute.outsidesql.factory.OutsideSqlExecutorFactory;
import org.seasar.dbflute.outsidesql.typed.EntityHandlingPmb;
import org.seasar.dbflute.resource.DBFluteSystem;
import org.seasar.dbflute.util.DfTypeUtil;

/**
 * The cursor executor of outside-SQL.
 * @param <BEHAVIOR> The type of behavior.
 * @author jflute
 */
public class OutsideSqlEntityExecutor<BEHAVIOR> {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The invoker of behavior command. (NotNull) */
    protected final BehaviorCommandInvoker _behaviorCommandInvoker;

    /** The DB name of table. (NotNull) */
    protected final String _tableDbName;

    /** The current database definition. (NotNull) */
    protected final DBDef _currentDBDef;

    /** The default configuration of statement. (NullAllowed) */
    protected final StatementConfig _defaultStatementConfig;

    /** The option of outside-SQL. (NotNull) */
    protected final OutsideSqlOption _outsideSqlOption;

    /** The factory of outside-SQL executor. (NotNull) */
    protected final OutsideSqlExecutorFactory _outsideSqlExecutorFactory;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public OutsideSqlEntityExecutor(BehaviorCommandInvoker behaviorCommandInvoker, String tableDbName,
            DBDef currentDBDef, StatementConfig defaultStatementConfig, OutsideSqlOption outsideSqlOption,
            OutsideSqlExecutorFactory outsideSqlExecutorFactory) {
        _behaviorCommandInvoker = behaviorCommandInvoker;
        _tableDbName = tableDbName;
        _currentDBDef = currentDBDef;
        _defaultStatementConfig = defaultStatementConfig;
        _outsideSqlOption = outsideSqlOption;
        _outsideSqlExecutorFactory = outsideSqlExecutorFactory;
    }

    // ===================================================================================
    //                                                                  Entity NullAllowed
    //                                                                  ==================
    /**
     * Select entity by the outside-SQL. <span style="color: #AD4747">{Typed Interface}</span><br />
     * You can call this method by only a typed parameter-bean
     * which is related to its own (outside-SQL) path and entity-type.
     * <pre>
     * SimpleMemberPmb pmb = new SimpleMemberPmb();
     * pmb.setMemberId(3);
     * SimpleMember member
     *     = memberBhv.outsideSql().entityHandling().<span style="color: #DD4747">selectEntity</span>(pmb);
     * if (member != null) {
     *     ... = member.get...();
     * } else {
     *     ...
     * }
     * </pre>
     * @param <ENTITY> The type of entity.
     * @param pmb The typed parameter-bean for entity handling. (NotNull)
     * @return The selected entity. (NullAllowed)
     * @exception org.seasar.dbflute.exception.OutsideSqlNotFoundException When the outside-SQL is not found.
     * @exception org.seasar.dbflute.exception.EntityDuplicatedException When the entity is duplicated.
     */
    public <ENTITY> ENTITY selectEntity(EntityHandlingPmb<BEHAVIOR, ENTITY> pmb) {
        if (pmb == null) {
            String msg = "The argument 'pmb' (typed parameter-bean) should not be null.";
            throw new IllegalArgumentException(msg);
        }
        return doSelectEntity(pmb.getOutsideSqlPath(), pmb, pmb.getEntityType());
    }

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
     * @exception org.seasar.dbflute.exception.OutsideSqlNotFoundException When the outside-SQL is not found.
     * @exception org.seasar.dbflute.exception.EntityDuplicatedException When the entity is duplicated.
     */
    public <ENTITY> ENTITY selectEntity(String path, Object pmb, Class<ENTITY> entityType) {
        return doSelectEntity(path, pmb, entityType);
    }

    protected <ENTITY> ENTITY doSelectEntity(String path, Object pmb, Class<ENTITY> entityType) {
        if (path == null) {
            String msg = "The argument 'path' of outside-SQL should not be null.";
            throw new IllegalArgumentException(msg);
        }
        if (entityType == null) {
            String msg = "The argument 'entityType' for result should not be null: path=" + path;
            throw new IllegalArgumentException(msg);
        }
        final int preSafetyMaxResultSize = xcheckSafetyResultAsOneIfNeed(pmb);
        final List<ENTITY> ls;
        try {
            ls = doSelectList(path, pmb, entityType);
        } catch (DangerousResultSizeException e) {
            final String searchKey4Log = buildSearchKey4Exception(path, pmb, entityType);
            throwSelectEntityDuplicatedException("{over safetyMaxResultSize '1'}", searchKey4Log, e);
            return null; // unreachable
        } finally {
            xrestoreSafetyResultIfNeed(pmb, preSafetyMaxResultSize);
        }
        if (ls == null || ls.isEmpty()) {
            return null;
        }
        if (ls.size() > 1) {
            final String searchKey4Log = buildSearchKey4Exception(path, pmb, entityType);
            throwSelectEntityDuplicatedException(String.valueOf(ls.size()), searchKey4Log, null);
        }
        return ls.get(0);
    }

    protected int xcheckSafetyResultAsOneIfNeed(Object pmb) {
        if (pmb instanceof FetchBean) {
            final int safetyMaxResultSize = ((FetchBean) pmb).getSafetyMaxResultSize();
            ((FetchBean) pmb).checkSafetyResult(1);
            return safetyMaxResultSize;
        }
        return 0;
    }

    protected void xrestoreSafetyResultIfNeed(Object pmb, int preSafetyMaxResultSize) {
        if (pmb instanceof FetchBean) {
            ((FetchBean) pmb).checkSafetyResult(preSafetyMaxResultSize);
        }
    }

    protected <ENTITY> String buildSearchKey4Exception(String path, Object pmb, Class<ENTITY> entityType) {
        final StringBuilder sb = new StringBuilder();
        sb.append("table  = ").append(_outsideSqlOption.getTableDbName()).append(ln());
        sb.append("path   = ").append(path).append(ln());
        sb.append("pmbean = ").append(DfTypeUtil.toClassTitle(pmb)).append(":").append(pmb).append(ln());
        sb.append("entity = ").append(DfTypeUtil.toClassTitle(entityType)).append(ln());
        sb.append("option = ").append(_outsideSqlOption);
        return sb.toString();
    }

    protected void throwSelectEntityDuplicatedException(String resultCountExp, Object searchKey, Throwable cause) {
        createBhvExThrower().throwSelectEntityDuplicatedException(resultCountExp, searchKey, cause);
    }

    protected <ENTITY> List<ENTITY> doSelectList(String path, Object pmb, Class<ENTITY> entityType) {
        return createBasicExecutor().selectList(path, pmb, entityType);
    }

    protected OutsideSqlBasicExecutor<BEHAVIOR> createBasicExecutor() {
        return _outsideSqlExecutorFactory.createBasic(_behaviorCommandInvoker, _tableDbName, _currentDBDef,
                _defaultStatementConfig, _outsideSqlOption);
    }

    // ===================================================================================
    //                                                                      Entity NotNull
    //                                                                      ==============
    /**
     * Select entity with deleted check by the outside-SQL. <span style="color: #AD4747">{Typed Interface}</span><br />
     * You can call this method by only a typed parameter-bean
     * which is related to its own (outside-SQL) path and entity-type.
     * <pre>
     * SimpleMemberPmb pmb = new SimpleMemberPmb();
     * pmb.setMemberId(3);
     * SimpleMember member
     *     = memberBhv.outsideSql().entityHandling().<span style="color: #DD4747">selectEntityWithDeletedCheck</span>(pmb);
     * ... = member.get...(); <span style="color: #3F7E5E">// the entity always be not null</span>
     * </pre>
     * @param <ENTITY> The type of entity.
     * @param pmb The typed parameter-bean for entity handling. (NotNull)
     * @return The selected entity. (NullAllowed)
     * @exception org.seasar.dbflute.exception.OutsideSqlNotFoundException When the outside-SQL is not found.
     * @exception org.seasar.dbflute.exception.EntityAlreadyDeletedException When the entity has already been deleted(not found).
     * @exception org.seasar.dbflute.exception.EntityDuplicatedException When the entity is duplicated.
     */
    public <ENTITY> ENTITY selectEntityWithDeletedCheck(EntityHandlingPmb<BEHAVIOR, ENTITY> pmb) {
        if (pmb == null) {
            String msg = "The argument 'pmb' (typed parameter-bean) should not be null.";
            throw new IllegalArgumentException(msg);
        }
        return doSelectEntityWithDeletedCheck(pmb.getOutsideSqlPath(), pmb, pmb.getEntityType());
    }

    /**
     * Select entity with deleted check by the outside-SQL. {FreeStyle Interface}<br />
     * This method can accept each element: path, parameter-bean(Object type), entity-type.
     * <pre>
     * String path = MemberBhv.PATH_selectSimpleMember;
     * SimpleMemberPmb pmb = new SimpleMemberPmb();
     * pmb.setMemberId(3);
     * Class&lt;SimpleMember&gt; entityType = SimpleMember.class;
     * SimpleMember member
     *     = memberBhv.outsideSql().entityHandling().<span style="color: #DD4747">selectEntityWithDeletedCheck</span>(path, pmb, entityType);
     * ... = member.get...(); <span style="color: #3F7E5E">// the entity always be not null</span>
     * </pre>
     * @param <ENTITY> The type of entity.
     * @param path The path of SQL file. (NotNull)
     * @param pmb The parameter-bean. Allowed types are Bean object and Map object. (NullAllowed)
     * @param entityType The type of entity. (NotNull)
     * @return The selected entity. (NullAllowed)
     * @exception org.seasar.dbflute.exception.OutsideSqlNotFoundException When the outside-SQL is not found.
     * @exception org.seasar.dbflute.exception.EntityAlreadyDeletedException When the entity has already been deleted(not found).
     * @exception org.seasar.dbflute.exception.EntityDuplicatedException When the entity is duplicated.
     */
    public <ENTITY> ENTITY selectEntityWithDeletedCheck(String path, Object pmb, Class<ENTITY> entityType) {
        return doSelectEntityWithDeletedCheck(path, pmb, entityType);
    }

    protected <ENTITY> ENTITY doSelectEntityWithDeletedCheck(String path, Object pmb, Class<ENTITY> entityType) {
        final ENTITY entity = selectEntity(path, pmb, entityType);
        if (entity == null) {
            throwSelectEntityAlreadyDeletedException(buildSearchKey4Exception(path, pmb, entityType));
        }
        return entity;
    }

    protected void throwSelectEntityAlreadyDeletedException(Object searchKey) {
        createBhvExThrower().throwSelectEntityAlreadyDeletedException(searchKey);
    }

    // ===================================================================================
    //                                                                              Option
    //                                                                              ======
    /**
     * Set up remove-block-comment for this outside-SQL.
     * @return this. (NotNull)
     */
    public OutsideSqlEntityExecutor<BEHAVIOR> removeBlockComment() {
        _outsideSqlOption.removeBlockComment();
        return this;
    }

    /**
     * Set up remove-line-comment for this outside-SQL.
     * @return this. (NotNull)
     */
    public OutsideSqlEntityExecutor<BEHAVIOR> removeLineComment() {
        _outsideSqlOption.removeLineComment();
        return this;
    }

    /**
     * Set up format-SQL for this outside-SQL. <br />
     * (For example, empty lines removed)
     * @return this. (NotNull)
     */
    public OutsideSqlEntityExecutor<BEHAVIOR> formatSql() {
        _outsideSqlOption.formatSql();
        return this;
    }

    /**
     * Configure statement JDBC options. (For example, queryTimeout, fetchSize, ...)
     * @param statementConfig The configuration of statement. (NullAllowed)
     * @return this. (NotNull)
     */
    public OutsideSqlEntityExecutor<BEHAVIOR> configure(StatementConfig statementConfig) {
        _outsideSqlOption.setStatementConfig(statementConfig);
        return this;
    }

    // ===================================================================================
    //                                                                    Exception Helper
    //                                                                    ================
    protected BehaviorExceptionThrower createBhvExThrower() {
        return _behaviorCommandInvoker.createBehaviorExceptionThrower();
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    /**
     * Get the value of line separator.
     * @return The value of line separator. (NotNull)
     */
    protected static String ln() {
        return DBFluteSystem.getBasicLn();
    }
}
