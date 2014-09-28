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
package org.seasar.dbflute.s2dao.sqlcommand;

import org.seasar.dbflute.unit.core.PlainTestCase;

/**
 * @author jflute
 */
public class TnProcedureCommandTest extends PlainTestCase {

    public void test_doBuildSqlAsCalledBySelect_Basic() throws Exception {
        // ## Arrange ##
        TnProcedureCommand target = createTarget();

        // ## Act ##
        String sql = target.doBuildSqlAsCalledBySelect("SP_FOO", 3);

        // ## Assert ##
        log(sql);
        assertEquals("select * from SP_FOO(?, ?, ?)", sql);
    }

    public void test_doBuildSqlAsProcedureCall_kakou() throws Exception {
        // ## Arrange ##
        TnProcedureCommand target = createTarget();

        // ## Act ##
        String sql = target.doBuildSqlAsProcedureCall("SP_FOO", 3, true, true);

        // ## Assert ##
        log(sql);
        assertEquals("{? = call SP_FOO(?, ?)}", sql);
    }

    public void test_doBuildSqlAsProcedureCall_kakowanai() throws Exception {
        // ## Arrange ##
        TnProcedureCommand target = createTarget();

        // ## Act ##
        String sql = target.doBuildSqlAsProcedureCall("SP_FOO", 3, true, false);

        // ## Assert ##
        log(sql);
        assertEquals("? = call SP_FOO(?, ?)", sql);
    }

    protected TnProcedureCommand createTarget() {
        return new TnProcedureCommand(null, null, null, null) {
            @Override
            protected void assertObjectNotNull(String variableName, Object value) {
                // for no check of constructor
            }
        };
    }
}
