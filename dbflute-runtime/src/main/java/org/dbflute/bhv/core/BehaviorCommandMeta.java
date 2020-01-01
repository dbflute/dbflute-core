/*
 * Copyright 2014-2020 the original author or authors.
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
package org.dbflute.bhv.core;

import java.util.List;

import org.dbflute.Entity;
import org.dbflute.cbean.ConditionBean;
import org.dbflute.dbmeta.DBMeta;
import org.dbflute.outsidesql.OutsideSqlOption;

/**
 * The meta information interface of behavior commands. <br>
 * You can get what the behavior command is.
 * @author jflute
 */
public interface BehaviorCommandMeta {

    // ===================================================================================
    //                                                                   Basic Information
    //                                                                   =================
    /**
     * Get project name of the database (DBFlute client) for the table.
     * @return The project name e.g. maihamadb (NotNull)
     */
    String getProjectName();

    /**
     * Get the DB meta instance for the table.
     * @return The (singleton) instance of DB meta for the table. (NotNull)
     */
    DBMeta getDBMeta();

    /**
     * Get the DB name of table corresponding to the executed behavior.
     * @return The DB name of table. (NotNull)
     */
    String getTableDbName();

    /**
     * Get the name of the command, e.g. selectList, update.
     * @return The name of the command. (NotNull)
     */
    String getCommandName();

    /**
     * Get the return type of command.
     * This type is not related to generic type because this is for conversion and check only.
     * @return The return type of command. (NotNull)
     */
    Class<?> getCommandReturnType();

    /**
     * Is the command only for initialization?
     * @return The determination, true or false.
     */
    boolean isInitializeOnly();

    // ===================================================================================
    //                                                                  Detail Information
    //                                                                  ==================
    /**
     * Does the command use condition-bean? <br>
     * e.g. selectList(cb), queryUpdate(entity, cb)
     * @return The determination, true or false.
     */
    boolean isConditionBean();

    /**
     * Does the command use outside-SQL? <br>
     * It contains procedure calls. <br>
     * e.g. outsideSql().selectList(pmb), outsideSql().execute(pmb)
     * @return The determination, true or false.
     */
    boolean isOutsideSql();

    /**
     * Does the command call procedure? <br>
     * But if an outside-SQL containing procedure call in it, it returns false. <br>
     * e.g. outsideSql().call(pmb)
     * @return The determination, true or false.
     */
    boolean isProcedure();

    /**
     * Does the command return selected records? <br>
     * But if it's a procedure, it returns false. <br>
     * e.g. selectList(cb), outsideSql().select(pmb)
     * @return The determination, true or false.
     */
    boolean isSelect();

    /**
     * Does the command return selected record count?
     * All outside-SQL return false, even if it has 'select count(*)' in the outside-SQL.
     * e.g. selectCount(cb)
     * @return The determination, true or false.
     */
    boolean isSelectCount();

    /**
     * Does the command handle its cursor?
     * e.g. selectCursor(cb), outsideSql().cursorHandling().selectCursor(pmb)
     * @return The determination, true or false.
     */
    boolean isSelectCursor();

    /**
     * Does the command execute insert? <br>
     * But if it's a procedure or an outside-SQL, it returns false. <br>
     * e.g. insert(entity), batchInsert(entityList), queryInsert(setupper)
     * @return The determination, true or false.
     */
    boolean isInsert();

    /**
     * Does the command execute update? <br>
     * But if it's a procedure or an outside-SQL, it returns false. <br>
     * e.g. update(entity), batchUpdate(entityList), queryUpdate(entity, cb)
     * @return The determination, true or false.
     */
    boolean isUpdate();

    /**
     * Does the command execute delete? <br>
     * But if it's a procedure or an outside-SQL, it returns false. <br>
     * e.g. delete(entity), batchDelete(entityList), queryDelete(cb)
     * @return The determination, true or false.
     */
    boolean isDelete();

    /**
     * Does the command execute entity update family? (means insert, update, delete) <br>
     * e.g. insert(entity), update(entity), delete(entity)
     * @return The determination, true or false.
     */
    boolean isEntityUpdateFamily();

    /**
     * Does the command execute batch update family? (means insert, update, delete) <br>
     * e.g. batchInsert(entityList), batchUpdate(entityList), batchDelete(entityList)
     * @return The determination, true or false.
     */
    boolean isBatchUpdateFamily();

    /**
     * Does the command execute query update family? (means insert, update, delete) <br>
     * e.g. queryInsert(setupper), queryUpdate(entity, cb), queryDelete(cb)
     * @return The determination, true or false.
     */
    boolean isQueryUpdateFamily();

    // ===================================================================================
    //                                                                Argument Information
    //                                                                ====================
    /**
     * Get the instance of condition-bean specified as argument if it exists.
     * @return The instance of condition-bean. (NullAllowed: when CB unused)
     */
    ConditionBean getConditionBean();

    /**
     * Get the instance of entity specified as one argument if it exists.
     * @return The instance of entity. (NullAllowed: when entity unused and batch update, query delete...)
     */
    Entity getEntity();

    /**
     * Get the list of entity specified as list argument if it exists.
     * @return The read-only list of entity instance. (NotNull, EmptyAllowed: when entity unused and entity update...)
     */
    List<Entity> getEntityList(); // since 1.2.0

    /**
     * Get the path of outside-SQL if it's outside-SQL.
     * @return The path of outside-SQL. (NullAllowed)
     */
    String getOutsideSqlPath();

    /**
     * Get the parameter-bean for outside-SQL if it's outside-SQL.
     * @return The parameter-bean for outside-SQL. (NullAllowed)
     */
    Object getParameterBean();

    /**
     * Get the option of outside-SQL if it's outside-SQL.
     * @return The option of outside-SQL. (NullAllowed)
     */
    OutsideSqlOption getOutsideSqlOption();

    // ===================================================================================
    //                                                                 Runtime Information
    //                                                                 ===================
    /**
     * Get the invoke path of behavior command lazily. <br>
     * Invoke path is e.g. FooAction.index():38 -&gt; BarLogic.selectQux():127 -&gt; ... <br>
     * To create this path needs stack trace (from exception instance) so lazily.
     * @return The display string of invoke path. (NullAllowed: e.g. before provider setup)
     */
    String getInvokePath();
}
