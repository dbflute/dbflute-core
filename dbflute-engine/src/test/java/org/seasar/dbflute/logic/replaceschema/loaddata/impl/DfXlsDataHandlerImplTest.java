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
package org.seasar.dbflute.logic.replaceschema.loaddata.impl;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

import org.seasar.dbflute.helper.StringKeyMap;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfColumnMeta;
import org.seasar.dbflute.unit.core.PlainTestCase;

/**
 * @author jflute
 */
public class DfXlsDataHandlerImplTest extends PlainTestCase {

    // ===================================================================================
    //                                                                    Process per Type
    //                                                                    ================
    // -----------------------------------------------------
    //                                     NotNull NotString
    //                                     -----------------
    public void test_DfXlsDataHandlerImpl_isNotNullNotString() {
        // ## Arrange ##
        final DfXlsDataHandlerImpl impl = createHandler();

        // ## Act & Assert ##
        assertFalse(impl.isNotNullNotString(null));
        assertFalse(impl.isNotNullNotString("abc"));
        assertTrue(impl.isNotNullNotString(new Date()));
        assertTrue(impl.isNotNullNotString(new Timestamp(System.currentTimeMillis())));
    }

    // -----------------------------------------------------
    //                                               Boolean
    //                                               -------
    public void test_processBoolean() throws Exception {
        // ## Arrange ##
        final DfXlsDataHandlerImpl impl = new DfXlsDataHandlerImpl(null, null) {
            @Override
            protected Class<?> getBindType(String tableName, DfColumnMeta columnMetaInfo) {
                return BigDecimal.class;
            }
        };
        Map<String, DfColumnMeta> columnMetaInfoMap = StringKeyMap.createAsCaseInsensitive();
        DfColumnMeta info = new DfColumnMeta();
        info.setColumnName("foo");
        info.setColumnSize(3);
        info.setJdbcDefValue(Types.NUMERIC);
        columnMetaInfoMap.put("foo", info);

        // ## Act ##
        boolean actual = impl.processBoolean("tbl", "foo", "0", null, null, 0, columnMetaInfoMap, 3);

        // ## Assert ##
        log("actual=" + actual);
        assertFalse(actual);
    }

    // ===================================================================================
    //                                                                          Skip Sheet
    //                                                                          ==========
    public void test_DfXlsDataHandlerImpl_setSkipSheet_SyntaxError() {
        // ## Arrange ##
        final DfXlsDataHandlerImpl impl = createHandler();

        // ## Act & Assert ##
        try {
            impl.setSkipSheet("MST.*+`*`+*P*`+*}+");
            fail();
        } catch (IllegalStateException e) {
            // OK
            log(e.getMessage());
            assertNotNull(e.getCause());
            log(e.getCause().getMessage());
            assertTrue(e.getCause() instanceof PatternSyntaxException);
        }
    }

    protected DfXlsDataHandlerImpl createHandler() {
        return new DfXlsDataHandlerImpl(null, null);
    }
}
