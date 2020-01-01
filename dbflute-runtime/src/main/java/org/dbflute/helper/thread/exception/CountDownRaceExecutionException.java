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
 */package org.dbflute.helper.thread.exception;

import java.util.List;

import org.dbflute.optional.OptionalThing;

/**
 * @author jflute
 * @since 1.0.5A (2013/10/17 Thursday)
 */
public class CountDownRaceExecutionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    protected List<Throwable> runnerCauseList; // null allowed, contains first cause

    public CountDownRaceExecutionException(String msg, Throwable firstCause) {
        super(msg, firstCause);
    }

    public CountDownRaceExecutionException(String msg, List<Throwable> runnerCauseList) {
        super(msg);
        this.runnerCauseList = runnerCauseList;
    }

    public OptionalThing<List<Throwable>> getRunnerCauseList() {
        return OptionalThing.ofNullable(runnerCauseList, () -> {
            throw new IllegalStateException("Not found the runner cause list.");
        });
    }
}
