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
package org.dbflute.helper.thread;

/**
 * @author jflute
 * @since 1.0.5A (2013/10/17 Thursday)
 */
public interface CountDownRaceExecution {

    default void readyCaller() {
    }

    default void hookBeforeCountdown() {
    }

    /**
     * Execute the application code as thread-fire.
     * @param runner The resource for the thread-fire. (NotNull)
     */
    void execute(CountDownRaceRunner runner);

    default void hookBeforeGoalFinally() {
    }

    default boolean isThrowImmediatelyByFirstCause() {
        return false;
    }
}
