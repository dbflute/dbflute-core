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
package org.seasar.dbflute.s2dao.sqlhandler;

import java.util.ArrayList;
import java.util.List;

import org.seasar.dbflute.CallbackContext;
import org.seasar.dbflute.jdbc.SqlLogHandler;
import org.seasar.dbflute.jdbc.SqlLogInfo;
import org.seasar.dbflute.jdbc.SqlResultHandler;
import org.seasar.dbflute.jdbc.SqlResultInfo;
import org.seasar.dbflute.mock.MockBehaviorCommand;
import org.seasar.dbflute.resource.InternalMapContext;
import org.seasar.dbflute.resource.ResourceContext;
import org.seasar.dbflute.unit.core.PlainTestCase;

/**
 * @author jflute
 * @since 0.9.5.1 (2009/06/19 Friday)
 */
public class TnAbstractBasicSqlHandlerTest extends PlainTestCase {

    public void test_logSql_whitebox_nothing() {
        // ## Arrange ##
        prepareMockBehaviorCommand();
        TnAbstractBasicSqlHandler handler = new TnAbstractBasicSqlHandler(null, null, null) {
            @Override
            protected String buildDisplaySql(String sql, Object[] args) {
                throw new IllegalStateException("log should not be called!");
            }

            @Override
            protected void log(String msg) {
                throw new IllegalStateException("log should not be called!");
            }

            @Override
            protected boolean isLogEnabled() {
                return false;
            }

            @Override
            protected void assertObjectNotNull(String variableName, Object value) {
                // for no check of constructor
            }
        };

        // ## Act & Assert ##
        handler.logSql(null, null); // Expect no exception
    }

    public void test_logSql_whitebox_logEnabledOnly() {
        // ## Arrange ##
        prepareMockBehaviorCommand();
        final List<String> markList = new ArrayList<String>();
        TnAbstractBasicSqlHandler handler = new TnAbstractBasicSqlHandler(null, null, null) {
            @Override
            protected String buildDisplaySql(String sql, Object[] args) {
                markList.add("getDisplaySql");
                return "select ...";
            }

            @Override
            protected void logDisplaySql(String displaySql) {
                assertEquals("select ...", displaySql);
                markList.add("log");
            }

            @Override
            protected void saveResultSqlLogInfo(SqlLogInfo sqlLogInfo) {
                markList.add("saveResultSqlLogInfo");
                super.saveResultSqlLogInfo(sqlLogInfo);
            }

            @Override
            protected boolean isLogEnabled() {
                return true;
            }

            @Override
            protected void assertObjectNotNull(String variableName, Object value) {
                // for no check of constructor
            }
        };

        // ## Act ##
        try {
            handler.logSql(null, null);

            assertNull(InternalMapContext.getResultSqlLogInfo());
        } finally {
            CallbackContext.clearCallbackContextOnThread();
            InternalMapContext.clearInternalMapContextOnThread();
        }

        // ## Assert ##
        assertEquals(2, markList.size());
        assertEquals("getDisplaySql", markList.get(0));
        assertEquals("log", markList.get(1));
    }

    public void test_logSql_whitebox_sqlLogHandlerOnly() {
        // ## Arrange ##
        prepareMockBehaviorCommand();
        final List<String> markList = new ArrayList<String>();
        final Object[] args = new Object[] {};
        final Class<?>[] argsTypes = new Class<?>[] {};
        TnAbstractBasicSqlHandler handler = new TnAbstractBasicSqlHandler(null, null, "select ...") {
            @Override
            protected String buildDisplaySql(String sql, Object[] args) {
                markList.add("buildDisplaySql");
                return "select ...";
            }

            @Override
            protected void logDisplaySql(String displaySql) {
                throw new IllegalStateException("log should not be called!");
            }

            @Override
            protected void log(String msg) {
                throw new IllegalStateException("log should not be called!");
            }

            @Override
            protected void saveResultSqlLogInfo(SqlLogInfo sqlLogInfo) {
                throw new IllegalStateException("log should not be called!");
            }

            @Override
            protected boolean isLogEnabled() {
                return false;
            }

            @Override
            protected void assertObjectNotNull(String variableName, Object value) {
                // for no check of constructor
            }
        };

        // ## Act ##
        try {
            CallbackContext callbackContext = new CallbackContext();
            callbackContext.setSqlLogHandler(new SqlLogHandler() {
                public void handle(SqlLogInfo info) {
                    markList.add("handle");
                    assertEquals("select ...", info.getDisplaySql());
                    assertEquals(newArrayList(args), newArrayList(info.getBindArgs()));
                    assertEquals(newArrayList(argsTypes), newArrayList(info.getBindArgTypes()));
                }
            });
            CallbackContext.setCallbackContextOnThread(callbackContext);
            handler.logSql(args, argsTypes);

            assertNull(InternalMapContext.getResultSqlLogInfo());
        } finally {
            CallbackContext.clearCallbackContextOnThread();
            InternalMapContext.clearInternalMapContextOnThread();
        }

        // ## Assert ##
        assertEquals(2, markList.size());
        assertEquals("handle", markList.get(0));
        assertEquals("buildDisplaySql", markList.get(1));
    }

    public void test_logSql_whitebox_sqlResultHandlerOnly() {
        // ## Arrange ##
        prepareMockBehaviorCommand();
        final List<String> markList = new ArrayList<String>();
        TnAbstractBasicSqlHandler handler = new TnAbstractBasicSqlHandler(null, null, "select ...") {
            @Override
            protected String buildDisplaySql(String sql, Object[] args) {
                markList.add("buildDisplaySql");
                return "select ...";
            }

            @Override
            protected void logDisplaySql(String displaySql) {
                throw new IllegalStateException("log should not be called!");
            }

            @Override
            protected void log(String msg) {
                throw new IllegalStateException("log should not be called!");
            }

            @Override
            protected void saveResultSqlLogInfo(SqlLogInfo sqlLogInfo) {
                markList.add("saveResultSqlLogInfo");
                super.saveResultSqlLogInfo(sqlLogInfo);
            }

            @Override
            protected boolean isLogEnabled() {
                return false;
            }

            @Override
            protected void assertObjectNotNull(String variableName, Object value) {
                // for no check of constructor
            }
        };

        // ## Act ##
        try {
            CallbackContext callbackContext = new CallbackContext();
            callbackContext.setSqlResultHandler(new SqlResultHandler() {
                public void handle(SqlResultInfo sqlResultInfo) {
                    throw new IllegalStateException("handle should not be called!");
                }
            });
            CallbackContext.setCallbackContextOnThread(callbackContext);
            handler.logSql(null, null);

            assertEquals("select ...", InternalMapContext.getResultSqlLogInfo().getDisplaySql());
        } finally {
            CallbackContext.clearCallbackContextOnThread();
            InternalMapContext.clearInternalMapContextOnThread();
        }

        // ## Assert ##
        assertEquals(2, markList.size());
        assertEquals("saveResultSqlLogInfo", markList.get(0));
        assertEquals("buildDisplaySql", markList.get(1));
    }

    public void test_logSql_whitebox_bigThree() {
        // ## Arrange ##
        prepareMockBehaviorCommand();
        final List<String> markList = new ArrayList<String>();
        final Object[] args = new Object[] {};
        final Class<?>[] argsTypes = new Class<?>[] {};
        TnAbstractBasicSqlHandler handler = new TnAbstractBasicSqlHandler(null, null, null) {
            @Override
            protected String buildDisplaySql(String sql, Object[] args) {
                markList.add("getDisplaySql");
                return "select ..." + ln() + "  from ...";
            }

            @Override
            protected void logDisplaySql(String displaySql) {
                markList.add("logDisplaySql");
                assertEquals("select ..." + ln() + "  from ...", displaySql);
                super.logDisplaySql(displaySql);
            }

            @Override
            protected void log(String msg) {
                markList.add("log");
                assertEquals(ln() + "select ..." + ln() + "  from ...", msg);
            }

            @Override
            protected void saveResultSqlLogInfo(SqlLogInfo sqlLogInfo) {
                markList.add("saveResultSqlLogInfo");
                super.saveResultSqlLogInfo(sqlLogInfo);
            }

            @Override
            protected boolean isLogEnabled() {
                return true;
            }

            @Override
            protected void assertObjectNotNull(String variableName, Object value) {
                // for no check of constructor
            }
        };

        // ## Act ##
        try {
            CallbackContext callbackContext = new CallbackContext();
            callbackContext.setSqlLogHandler(new SqlLogHandler() {

                public void handle(SqlLogInfo info) {
                    markList.add("handle");
                    assertEquals("select ..." + ln() + "  from ...", info.getDisplaySql());
                    assertEquals(newArrayList(args), newArrayList(info.getBindArgs()));
                    assertEquals(newArrayList(argsTypes), newArrayList(info.getBindArgTypes()));
                }
            });
            callbackContext.setSqlResultHandler(new SqlResultHandler() {
                public void handle(SqlResultInfo sqlResultInfo) {
                    throw new IllegalStateException("handle should not be called!");
                }
            });
            CallbackContext.setCallbackContextOnThread(callbackContext);
            handler.logSql(args, argsTypes);

            assertEquals("select ..." + ln() + "  from ...", InternalMapContext.getResultSqlLogInfo().getDisplaySql());
        } finally {
            CallbackContext.clearCallbackContextOnThread();
            InternalMapContext.clearInternalMapContextOnThread();
        }

        // ## Assert ##
        assertEquals(5, markList.size());
        assertEquals("getDisplaySql", markList.get(0));
        assertEquals("logDisplaySql", markList.get(1));
        assertEquals("log", markList.get(2));
        assertEquals("handle", markList.get(3));
        assertEquals("saveResultSqlLogInfo", markList.get(4));
    }

    protected void prepareMockBehaviorCommand() {
        MockBehaviorCommand behaviorCommand = new MockBehaviorCommand();
        ResourceContext resourceContext = new ResourceContext();
        resourceContext.setBehaviorCommand(behaviorCommand);
        ResourceContext.setResourceContextOnThread(resourceContext);
    }
}
