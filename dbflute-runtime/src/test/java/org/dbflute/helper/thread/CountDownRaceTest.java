/*
 * Copyright 2014-2015 the original author or authors.
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
}
