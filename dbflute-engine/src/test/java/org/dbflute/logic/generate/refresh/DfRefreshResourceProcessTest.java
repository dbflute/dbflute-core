/*
 * Copyright 2014-2023 the original author or authors.
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
package org.dbflute.logic.generate.refresh;

import java.io.IOException;
import java.util.Arrays;

import org.dbflute.unit.EngineTestCase;

/**
 * @author jflute
 */
public class DfRefreshResourceProcessTest extends EngineTestCase {

    public void test_buildContinuedIOExceptionMessage_basic() {
        // ## Arrange ##
        DfRefreshResourceProcess process = new DfRefreshResourceProcess(Arrays.asList("sea"), "http://dbflute.org");

        // ## Act ##
        // ## Assert ##
        // expects no exception and visual check
        process.handleRefreshIOException(new IOException());
        process.handleRefreshIOException(new IOException("mystic"));
    }
}
