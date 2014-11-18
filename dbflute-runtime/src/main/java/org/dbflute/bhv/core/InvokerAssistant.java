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
package org.dbflute.bhv.core;

import javax.sql.DataSource;

import org.dbflute.bhv.core.context.ResourceParameter;
import org.dbflute.bhv.core.supplement.SequenceCacheHandler;
import org.dbflute.bhv.exception.BehaviorExceptionThrower;
import org.dbflute.bhv.exception.SQLExceptionHandlerFactory;
import org.dbflute.cbean.cipher.GearedCipherManager;
import org.dbflute.cbean.sqlclause.SqlClauseCreator;
import org.dbflute.dbmeta.DBMetaProvider;
import org.dbflute.dbway.DBDef;
import org.dbflute.jdbc.SQLExceptionDigger;
import org.dbflute.jdbc.StatementConfig;
import org.dbflute.jdbc.StatementFactory;
import org.dbflute.optional.RelationOptionalFactory;
import org.dbflute.outsidesql.OutsideSqlOption;
import org.dbflute.outsidesql.factory.OutsideSqlExecutorFactory;
import org.dbflute.s2dao.jdbc.TnResultSetHandlerFactory;
import org.dbflute.s2dao.metadata.TnBeanMetaDataFactory;
import org.dbflute.twowaysql.factory.SqlAnalyzerFactory;

/**
 * @author jflute
 */
public interface InvokerAssistant {

    /**
     * @return The current database definition. (NotNull)
     */
    DBDef assistCurrentDBDef();

    /**
     * @return The data source. (NotNull)
     */
    DataSource assistDataSource();

    /**
     * @return The provider of DB meta. (NotNull)
     */
    DBMetaProvider assistDBMetaProvider();

    /**
     * Assist the creator of SQL clause. <br>
     * This is only used in internal world of DBFlute (to judge unique-constraint).
     * So condition-bean does not use this.
     * @return The instance of creator. (NotNull)
     */
    SqlClauseCreator assistSqlClauseCreator();

    /**
     * @return The factory of statement. (NotNull)
     */
    StatementFactory assistStatementFactory();

    /**
     * @return The factory of bean meta data. (NotNull)
     */
    TnBeanMetaDataFactory assistBeanMetaDataFactory();

    /**
     * @return The factory of result set handler. (NotNull)
     */
    TnResultSetHandlerFactory assistResultSetHandlerFactory();

    /**
     * @return The factory of relation optional. (NotNull)
     */
    RelationOptionalFactory assistRelationOptionalFactory();

    /**
     * Assist the factory of SQL analyzer. <br>
     * This factory is also used on ConditionBean.toDisplaySql().
     * So this method should be state-less.
     * @return The instance of factory. (NotNull)
     */
    SqlAnalyzerFactory assistSqlAnalyzerFactory();

    /**
     * @param tableDbName The DB name of table to be related to. (NotNull)
     * @return The first option of outside-SQL. (NullAllowed: if null, lazy-loaded)
     */
    OutsideSqlOption assistFirstOutsideSqlOption(String tableDbName);

    /**
     * Assist the factory of outside-SQL executor.
     * @return The instance of factory. (NotNull)
     */
    OutsideSqlExecutorFactory assistOutsideSqlExecutorFactory();

    /**
     * @return The digger of SQLException. (NotNull)
     */
    SQLExceptionDigger assistSQLExceptionDigger();

    /**
     * Assist the factory of SQLException handler.
     * @return The instance of factory. (NotNull)
     */
    SQLExceptionHandlerFactory assistSQLExceptionHandlerFactory();

    /**
     * Assist the handler of sequence cache.
     * @return The instance of handler. (NotNull)
     */
    SequenceCacheHandler assistSequenceCacheHandler();

    /**
     * @return The encoding of SQL file. (NotNull)
     */
    String assistSqlFileEncoding();

    /**
     * @return The default configuration of statement. (NotNull)
     */
    StatementConfig assistDefaultStatementConfig();

    /**
     * @return The thrower of behavior exception. (NotNull)
     */
    BehaviorExceptionThrower assistBehaviorExceptionThrower();

    /**
     * @return The manager of geared cipher. (NullAllowed)
     */
    GearedCipherManager assistGearedCipherManager();

    /**
     * @return The parameter of resource. (NotNull)
     */
    ResourceParameter assistResourceParameter();

    /**
     * @return The array of client invoke names. (NotNull)
     */
    String[] assistClientInvokeNames();

    /**
     * @return The array of by-pass invoke names. (NotNull)
     */
    String[] assistByPassInvokeNames();

    /**
     * To be disposable.
     * @param callerProcess The disposable process for the caller. (NotNull)
     */
    void toBeDisposable(DisposableProcess callerProcess);

    /**
     * The call-back interface for disposable process.
     */
    public interface DisposableProcess {

        /**
         * Dispose resources.
         */
        void dispose();
    }
}
