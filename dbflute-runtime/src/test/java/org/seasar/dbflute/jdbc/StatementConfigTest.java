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
package org.seasar.dbflute.jdbc;

import org.seasar.dbflute.unit.core.PlainTestCase;

/**
 * @author jflute
 */
public class StatementConfigTest extends PlainTestCase {

    public void test_buildResultSetTypeDisp_basic() throws Exception {
        StatementConfig config = new StatementConfig();
        assertEquals("default", config.buildResultSetTypeDisp());
        config.typeForwardOnly();
        assertEquals("forward", config.buildResultSetTypeDisp());
        config.typeScrollInsensitive();
        assertEquals("scroll(ins)", config.buildResultSetTypeDisp());
        config.typeScrollSensitive();
        assertEquals("scroll(sen)", config.buildResultSetTypeDisp());
    }
}
