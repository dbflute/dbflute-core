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
package org.dbflute.logic.replaceschema.loaddata.base;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.Map;

import org.dbflute.helper.StringKeyMap;
import org.dbflute.logic.jdbc.metadata.info.DfColumnMeta;
import org.dbflute.logic.replaceschema.loaddata.xls.DfXlsDataHandlingWriter;
import org.dbflute.unit.EngineTestCase;

/**
 * @author jflute
 */
public class DfAbstractDataWriterTest extends EngineTestCase {

    // ===================================================================================
    //                                                                    Process per Type
    //                                                                    ================
    // -----------------------------------------------------
    //                                     NotNull NotString
    //                                     -----------------
    public void test_isNotNullNotString() { // via XlsData
        // ## Arrange ##
        final DfXlsDataHandlingWriter impl = new DfXlsDataHandlingWriter(null, null);

        // ## Act & Assert ##
        assertFalse(impl.isNotNullNotString(null));
        assertFalse(impl.isNotNullNotString("abc"));
        assertTrue(impl.isNotNullNotString(new Date()));
        assertTrue(impl.isNotNullNotString(new Timestamp(System.currentTimeMillis())));
    }

    // -----------------------------------------------------
    //                                               Boolean
    //                                               -------
    public void test_processBoolean() throws Exception { // via XlsData
        // ## Arrange ##
        final DfXlsDataHandlingWriter impl = new DfXlsDataHandlingWriter(null, null) {
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
}
