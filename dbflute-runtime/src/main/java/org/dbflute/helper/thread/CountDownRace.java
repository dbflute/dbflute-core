/*
 * Copyright 2014-2017 the original author or authors.
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
package org.dbflute.helper.thread;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.dbflute.helper.thread.exception.ThreadFireFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 1.0.5A (2013/10/17 Thursday)
 */
public class CountDownRace {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger _log = LoggerFactory.getLogger(CountDownRace.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Map<Integer, Object> _runnerRequestMap; // map:{entryNumber, parameterObject} 
    protected final ExecutorService _service;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public CountDownRace(int runnerCount) { // assigned by only runner count (without parameter)
        if (runnerCount < 1) {
            String msg = "The argument 'runnerCount' should not be minus or zero: " + runnerCount;
            throw new IllegalArgumentException(msg);
        }
        _runnerRequestMap = newRunnerRequestMap(runnerCount);
        for (int i = 0; i < runnerCount; i++) { // basically synchronized with parameter size
            final int entryNumber = i + 1;
            _runnerRequestMap.put(entryNumber, null);
        }
        _service = prepareExecutorService();
    }

    public CountDownRace(List<Object> parameterList) { // assigned by parameters (the size is runner count)
        if (parameterList == null || parameterList.isEmpty()) {
            String msg = "The argument 'parameterList' should not be null or empty: " + parameterList;
            throw new IllegalArgumentException(msg);
        }
        _runnerRequestMap = newRunnerRequestMap(parameterList.size());
        int index = 0;
        for (Object parameter : parameterList) {
            final int entryNumber = index + 1;
            _runnerRequestMap.put(entryNumber, parameter);
            ++index;
        }
        _service = prepareExecutorService();
    }

    protected Map<Integer, Object> newRunnerRequestMap(int size) {
        return new LinkedHashMap<Integer, Object>(size);
    }

    protected ExecutorService prepareExecutorService() {
        return Executors.newCachedThreadPool();
    }

    // ===================================================================================
    //                                                                         Thread Fire
    //                                                                         ===========
    public void readyGo(CountDownRaceExecution runnerLambda) {
        if (runnerLambda == null) {
            throw new IllegalArgumentException("The argument 'runnerLambda' should be not null.");
        }
        doReadyGo(runnerLambda);
    }

    protected void doReadyGo(CountDownRaceExecution execution) {
        final int runnerCount = _runnerRequestMap.size();
        final CountDownLatch ready = new CountDownLatch(runnerCount);
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch goal = new CountDownLatch(runnerCount);
        final CountDownRaceLatch ourLatch = new CountDownRaceLatch(runnerCount);
        final Object lockObj = new Object();
        final List<Future<Void>> futureList = new ArrayList<Future<Void>>();
        for (Entry<Integer, Object> entry : _runnerRequestMap.entrySet()) {
            final Integer entryNumber = entry.getKey();
            final Object parameter = entry.getValue(); // null allowed
            final Callable<Void> callable = createCallable(execution, ready, start, goal, ourLatch, entryNumber, parameter, lockObj);
            final Future<Void> future = _service.submit(callable);
            futureList.add(future);
        }

        if (_log.isDebugEnabled()) {
            _log.debug("...Ready Go! CountDownRace just begun! (runner=" + runnerCount + ")");
        }
        start.countDown(); // fire!
        try {
            goal.await(); // wait until all threads are finished
            if (_log.isDebugEnabled()) {
                _log.debug("All runners finished line! (runner=" + runnerCount + ")");
            }
        } catch (InterruptedException e) {
            String msg = "goal.await() was interrupted!";
            throw new IllegalStateException(msg, e);
        }

        handleFuture(futureList);
    }

    protected void handleFuture(List<Future<Void>> futureList) {
        for (Future<Void> future : futureList) {
            try {
                future.get();
            } catch (InterruptedException e) {
                String msg = "future.get() was interrupted!";
                throw new IllegalStateException(msg, e);
            } catch (ExecutionException e) {
                String msg = "Failed to fire the thread: " + future;
                throw new ThreadFireFailureException(msg, e.getCause());
            }
        }
    }

    // ===================================================================================
    //                                                                            Callable
    //                                                                            ========
    protected Callable<Void> createCallable(final CountDownRaceExecution execution, final CountDownLatch ready, final CountDownLatch start,
            final CountDownLatch goal, final CountDownRaceLatch ourLatch, final int entryNumber, final Object parameter,
            final Object lockObj) {
        return new Callable<Void>() {
            public Void call() { // each thread here
                final long threadId = Thread.currentThread().getId();
                try {
                    ready.countDown();
                    try {
                        start.await();
                    } catch (InterruptedException e) {
                        String msg = "start.await() was interrupted: start=" + start;
                        throw new IllegalStateException(msg, e);
                    }
                    RuntimeException cause = null;
                    try {
                        execution.execute(createRunner(threadId, ourLatch, entryNumber, parameter, lockObj));
                    } catch (RuntimeException e) {
                        cause = e;
                    }
                    if (cause != null) {
                        throw cause;
                    }
                } finally {
                    goal.countDown();
                    ourLatch.reset(); // to release waiting threads
                }
                return null;
            }
        };
    }

    protected CountDownRaceRunner createRunner(long threadId, CountDownRaceLatch ourLatch, int entryNumber, Object parameter,
            Object lockObj) {
        return new CountDownRaceRunner(threadId, ourLatch, entryNumber, parameter, lockObj, _runnerRequestMap.size());
    }
}
