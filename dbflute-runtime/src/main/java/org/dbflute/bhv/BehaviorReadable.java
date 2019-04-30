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
package org.dbflute.bhv;

import org.dbflute.Entity;
import org.dbflute.bhv.readable.EntityRowHandler;
import org.dbflute.cbean.ConditionBean;
import org.dbflute.cbean.chelper.HpSLSFunction;
import org.dbflute.cbean.result.ListResultBean;
import org.dbflute.cbean.result.PagingResultBean;
import org.dbflute.dbmeta.DBMeta;
import org.dbflute.outsidesql.executor.OutsideSqlAllFacadeExecutor;

/**
 * The interface of behavior-readable.
 * @author jflute
 */
public interface BehaviorReadable {

    // ===================================================================================
    //                                                                             DB Meta
    //                                                                             =======
    /**
     * Handle the meta as DBMeta, that has all info of the table.
     * @return The (singleton) instance of DB meta for the table. (NotNull)
     */
    DBMeta asDBMeta();

    /**
     * Handle the meta as table DB name, that can be identity of table.
     * @return The (fixed) DB name of the table. (NotNull)
     */
    String asTableDbName();

    // ===================================================================================
    //                                                                        New Instance
    //                                                                        ============
    /**
     * New entity instance.
     * @return Entity. (NotNull)
     */
    Entity newEntity();

    /**
     * New condition-bean instance.
     * @return Condition-bean. (NotNull)
     */
    ConditionBean newConditionBean();

    // ===================================================================================
    //                                                                    Basic Read Count
    //                                                                    ================
    /**
     * Read count by condition-bean. <br>
     * An interface dispatch for selectCount().
     * @param cb The instance of corresponding condition-bean. (NotNull)
     * @return Read count. (NotNull)
     */
    int readCount(ConditionBean cb);

    // ===================================================================================
    //                                                                   Basic Read Entity
    //                                                                   =================
    /**
     * Read entity by condition-bean. <br>
     * An interface dispatch for selectEntity().
     * @param cb The instance of corresponding condition-bean. (NotNull)
     * @return Read entity. (NullAllowed: when not found)
     */
    Entity readEntity(ConditionBean cb);

    /**
     * Read simple entity by condition-bean with deleted check. <br>
     * An interface dispatch for selectEntityWithDeletedCheck().
     * @param cb The instance of corresponding condition-bean. (NotNull)
     * @return Read entity. (NotNull)
     */
    Entity readEntityWithDeletedCheck(ConditionBean cb);

    // ===================================================================================
    //                                                                     Basic Read List
    //                                                                     ===============
    /**
     * Read list as result-bean. <br>
     * An interface dispatch for selectList().
     * @param <RESULT> The result type of entity.
     * @param cb The instance of corresponding condition-bean. (NotNull)
     * @return The list of entity as result-bean. If the select result is zero, it returns empty list. (NotNull)
     */
    <RESULT extends Entity> ListResultBean<RESULT> readList(ConditionBean cb);

    /**
     * Read page as result-bean. <br>
     * An interface dispatch for selectPage().
     * @param <RESULT> The result type of entity.
     * @param cb The instance of corresponding condition-bean. (NotNull)
     * @return The page of entity as result-bean. (NotNull)
     */
    <RESULT extends Entity> PagingResultBean<RESULT> readPage(ConditionBean cb);

    // ===================================================================================
    //                                                                         Cursor Read
    //                                                                         ===========
    /**
     * Read the cursor by the row handler. <br>
     * An interface dispatch for selecrCursor().
     * @param <RESULT> The result type of entity.
     * @param cb The instance of corresponding condition-bean. (NotNull)
     * @param entityLambda The handler of entity row. (NotNull)
     */
    <RESULT extends Entity> void readCursor(ConditionBean cb, EntityRowHandler<RESULT> entityLambda);

    // ===================================================================================
    //                                                                         Scalar Read
    //                                                                         ===========
    /**
     * Read the scalar value derived by a function from uniquely-selected records. <br>
     * An interface dispatch for selectScalar().
     * @param <RESULT> The type of scalar result.
     * @param resultType The type of result. (NotNull)
     * @return The scalar function object to specify function for scalar value. (NotNull)
     */
    <RESULT> HpSLSFunction<ConditionBean, RESULT> readScalar(Class<RESULT> resultType);

    // ===================================================================================
    //                                                                            Sequence
    //                                                                            ========
    /**
     * Read next value of sequence. <br>
     * An interface dispatch for selectNextVal().
     * @return The next value of sequence. (NotNull)
     */
    Number readNextVal();

    // ===================================================================================
    //                                                                          OutsideSql
    //                                                                          ==========
    /**
     * Prepare an outside-SQL execution by returning an instance of the executor for outside-SQL.
     * @param <BEHAVIOR> The type of behavior.
     * @return The facade for outside-SQL. (NotNull) 
     */
    <BEHAVIOR extends BehaviorReadable> OutsideSqlAllFacadeExecutor<BEHAVIOR> readyOutsideSql();

    // ===================================================================================
    //                                                                             Warm Up
    //                                                                             =======
    /**
     * Warm up the command of behavior. {Internal}
     */
    void warmUpCommand();
}
