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
package org.seasar.dbflute.resource;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.sql.DataSource;

import org.seasar.dbflute.unit.core.PlainTestCase;

/**
 * @author jflute
 */
public class ManualThreadDataSourceTest extends PlainTestCase {

    public void test_thread() throws Exception {
        final Set<ManualThreadDataSourceHandler> handlerSet = Collections
                .synchronizedSet(new HashSet<ManualThreadDataSourceHandler>());
        ExecutionCreator<ManualThreadDataSourceHandler> creator = new ExecutionCreator<ManualThreadDataSourceHandler>() {
            public Execution<ManualThreadDataSourceHandler> create() {
                return new Execution<ManualThreadDataSourceHandler>() {
                    public ManualThreadDataSourceHandler execute() {
                        ManualThreadDataSourceHandler.prepareDataSourceHandler();
                        ManualThreadDataSourceHandler handler = ManualThreadDataSourceHandler.getDataSourceHandler();
                        log(handler);
                        if (handlerSet.contains(handler)) {
                            fail();
                        }
                        handlerSet.add(handler);
                        return handler;
                    }
                };
            }
        };
        for (int i = 0; i < 5; i++) {
            fireSameExecution(creator, false);
        }
    }

    protected static class MockDataSource implements DataSource {

        public Connection getConnection() throws SQLException {
            return null;
        }

        public Connection getConnection(String username, String password) throws SQLException {
            return null;
        }

        public PrintWriter getLogWriter() throws SQLException {
            return null;
        }

        public void setLogWriter(PrintWriter out) throws SQLException {
        }

        public void setLoginTimeout(int seconds) throws SQLException {
        }

        public int getLoginTimeout() throws SQLException {
            return 0;
        }

        public <T> T unwrap(Class<T> class1) throws SQLException {
            return null;
        }

        public boolean isWrapperFor(Class<?> class1) throws SQLException {
            return false;
        }
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    private <RESULT> void fireSameExecution(ExecutionCreator<RESULT> creator, boolean sameCheck) {
        // ## Arrange ##
        ExecutorService service = Executors.newCachedThreadPool();
        int threadCount = 10;
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch goal = new CountDownLatch(threadCount);
        Execution<RESULT> execution = creator.create();
        List<Future<RESULT>> futureList = new ArrayList<Future<RESULT>>();
        for (int i = 0; i < threadCount; i++) {
            Future<RESULT> future = service.submit(createCallable(execution, ready, start, goal));
            futureList.add(future);
        }

        // ## Act ##
        // Start!
        start.countDown();
        try {
            // Wait until all threads are finished!
            goal.await();
        } catch (InterruptedException e) {
            String msg = "goal.await() was interrupted!";
            throw new IllegalStateException(msg, e);
        }
        log("All threads are finished!");

        // ## Assert ##
        List<RESULT> resultList = new ArrayList<RESULT>();
        for (Future<RESULT> future : futureList) {
            try {
                RESULT result = future.get();
                assertNotNull(result);
                resultList.add(result);
            } catch (InterruptedException e) {
                String msg = "future.get() was interrupted!";
                throw new IllegalStateException(msg, e);
            } catch (ExecutionException e) {
                String msg = "Failed to execute!";
                throw new ThreadFireException(msg, e.getCause());
            }
        }
        if (sameCheck) {
            RESULT preResult = null;
            for (RESULT result : resultList) {
                log(result);
                if (preResult == null) {
                    preResult = result;
                    continue;
                }
                assertEquals(preResult, result);
            }
        }
    }

    protected static class ThreadFireException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public ThreadFireException(String msg, Throwable e) {
            super(msg, e);
        }
    }

    private static interface ExecutionCreator<RESULT> {
        Execution<RESULT> create();
    }

    private static interface Execution<RESULT> {
        RESULT execute();
    }

    private <RESULT> Callable<RESULT> createCallable(final Execution<RESULT> execution, final CountDownLatch ready,
            final CountDownLatch start, final CountDownLatch goal) {
        return new Callable<RESULT>() {
            public RESULT call() {
                try {
                    ready.countDown();
                    try {
                        start.await();
                    } catch (InterruptedException e) {
                        String msg = "start.await() was interrupted!";
                        throw new IllegalStateException(msg, e);
                    }
                    RESULT result = execution.execute();
                    return result;
                } finally {
                    goal.countDown();
                }
            }
        };
    }
}
