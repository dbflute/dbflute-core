/*
 * Copyright 2014-2022 the original author or authors.
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
package org.dbflute.logic.replaceschema.loaddata.xls;

import java.util.regex.PatternSyntaxException;

import org.dbflute.unit.EngineTestCase;

/**
 * @author jflute
 */
public class DfXlsDataHandlerImplTest extends EngineTestCase {

    // ===================================================================================
    //                                                                          Skip Sheet
    //                                                                          ==========
    public void test_DfXlsDataHandlerImpl_acceptSkipSheet_SyntaxError() {
        // ## Arrange ##
        final DfXlsDataHandlingWriter impl = createHandler();

        // ## Act & Assert ##
        try {
            impl.acceptSkipSheet("MST.*+`*`+*P*`+*}+");
            fail();
        } catch (IllegalStateException e) {
            // OK
            log(e.getMessage());
            assertNotNull(e.getCause());
            log(e.getCause().getMessage());
            assertTrue(e.getCause() instanceof PatternSyntaxException);
        }
    }

    protected DfXlsDataHandlingWriter createHandler() {
        return new DfXlsDataHandlingWriter(null, null);
    }
}
