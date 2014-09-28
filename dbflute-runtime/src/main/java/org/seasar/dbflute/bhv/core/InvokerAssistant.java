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
package org.seasar.dbflute.bhv.core;

import javax.sql.DataSource;

import org.seasar.dbflute.DBDef;
import org.seasar.dbflute.bhv.core.supplement.SequenceCacheHandler;
import org.seasar.dbflute.cbean.cipher.GearedCipherManager;
import org.seasar.dbflute.cbean.sqlclause.SqlClauseCreator;
import org.seasar.dbflute.dbmeta.DBMetaProvider;
import org.seasar.dbflute.exception.factory.SQLExceptionHandlerFactory;
import org.seasar.dbflute.exception.thrower.BehaviorExceptionThrower;
import org.seasar.dbflute.jdbc.SQLExceptionDigger;
import org.seasar.dbflute.jdbc.StatementConfig;
import org.seasar.dbflute.jdbc.StatementFactory;
import org.seasar.dbflute.optional.RelationOptionalFactory;
import org.seasar.dbflute.outsidesql.factory.OutsideSqlExecutorFactory;
import org.seasar.dbflute.resource.ResourceParameter;
import org.seasar.dbflute.s2dao.metadata.TnBeanMetaDataFactory;
import org.seasar.dbflute.twowaysql.factory.SqlAnalyzerFactory;

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
     * Assist the creator of SQL clause. <br />
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
     * @return The factory of relation optional. (NotNull)
     */
    RelationOptionalFactory assistRelationOptionalFactory();

    /**
     * Assist the factory of SQL analyzer. <br />
     * This factory is also used on ConditionBean.toDisplaySql().
     * So this method should be state-less.
     * @return The instance of factory. (NotNull)
     */
    SqlAnalyzerFactory assistSqlAnalyzerFactory();

    /**
     * Assist the factory of outside SQL executor.
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
