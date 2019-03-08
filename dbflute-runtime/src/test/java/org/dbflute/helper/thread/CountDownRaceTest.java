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

import java.util.Arrays;

import org.dbflute.exception.EntityAlreadyDeletedException;
import org.dbflute.helper.thread.exception.CountDownRaceExecutionException;
import org.dbflute.unit.RuntimeTestCase;

/**
 * @author jflute
 */
public class CountDownRaceTest extends RuntimeTestCase {

    public void test_assignedBy_runnerCount() {
        // ## Arrange ##
        CountDownRace race = new CountDownRace(3);

        // ## Act ##
        race.readyGo(new CountDownRaceExecution() {
            public void execute(CountDownRaceRunner runner) {
                synchronized (this) {
                    log(runner.getEntryNumber() + " is here!");
                    markHere("entry" + runner.getEntryNumber());
                }
                assertFalse(runner.getParameter().isPresent());
            }
        });

        // ## Assert ##
        assertMarked("entry1");
        assertMarked("entry2");
        assertMarked("entry3");
    }

    public void test_assignedBy_parameterList() {
        // ## Arrange ##
        CountDownRace race = new CountDownRace(Arrays.asList("sea", "land", "piari"));

        // ## Act ##
        race.readyGo(new CountDownRaceExecution() {
            public void execute(CountDownRaceRunner runner) {
                synchronized (this) {
                    log(runner.getEntryNumber() + " is here!");
                    markHere("entry:" + runner.getParameter().get());
                }
            }
        });

        // ## Assert ##
        assertMarked("entry:sea");
        assertMarked("entry:land");
        assertMarked("entry:piari");
    }

    // -----------------------------------------------------
    //                                             Exception
    //                                             ---------
    public void test_exception_causeList() {
        // ## Arrange ##
        CountDownRace race = new CountDownRace(Arrays.asList("sea", "land", "piari", "bonvo"));

        // ## Act ##
        // ## Assert ##
        assertException(CountDownRaceExecutionException.class, () -> {
            race.readyGo(runner -> {
                String parameter = (String) runner.getParameter().get();
                log(parameter);
                int entryNumber = runner.getEntryNumber();
                if (entryNumber == 1) {
                    throw new IllegalStateException("parameter=" + parameter);
                } else if (entryNumber == 2) {
                    try {
                        throw new MockNestedException("nested");
                    } catch (RuntimeException e) {
                        throw new IllegalStateException("parameter=" + parameter, e);
                    }
                } else if (entryNumber == 4) {
                    throw new Error("parameter=" + parameter);
                }
            });
        }).handle(cause -> {
            String msg = cause.getMessage();
            log(msg);
            assertContains(msg, MockNestedException.class.getSimpleName());
            assertContainsAll(msg, "sea", "land", "bonvo");
            assertNull(cause.getCause());
        });
    }

    public void test_exception_firstCause() {
        // ## Arrange ##
        CountDownRace race = new CountDownRace(Arrays.asList("sea", "land", "piari", "bonvo"));

        // ## Act ##
        // ## Assert ##
        assertException(CountDownRaceExecutionException.class, () -> {
            race.readyGo(new CountDownRaceExecution() {
                @Override
                public void execute(CountDownRaceRunner runner) {
                    String parameter = (String) runner.getParameter().get();
                    log(parameter);
                    int entryNumber = runner.getEntryNumber();
                    if (entryNumber == 1) {
                        throw new IllegalStateException("parameter=" + parameter);
                    } else if (entryNumber == 2) {
                        sleep(1000);
                        try {
                            throw new MockNestedException("nested");
                        } catch (RuntimeException e) {
                            throw new IllegalStateException("parameter=" + parameter, e);
                        }
                    } else if (entryNumber == 4) {
                        sleep(1000);
                        throw new Error("parameter=" + parameter);
                    }
                }

                @Override
                public boolean isThrowImmediatelyByFirstCause() {
                    return true;
                }
            });
        }).handle(cause -> {
            StringBuilder sb = new StringBuilder();
            sb.append(cause.getMessage());
            String msg = sb.toString();
            log(msg);
            assertNotContains(msg, EntityAlreadyDeletedException.class.getSimpleName());
            assertNotContains(msg, "sea");
            assertNotContains(msg, "land");
            assertNotContains(msg, "piari");
            assertNotContains(msg, "bonvo");
            assertNotNull(cause.getCause());
            assertContains(cause.getCause().getMessage(), "sea");
        });
    }

    protected static class MockNestedException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public MockNestedException(String msg) {
            super(msg);
        }
    }
}
