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
package org.seasar.dbflute.s2dao.jdbc;

import java.sql.ResultSet;

import org.seasar.dbflute.jdbc.StatementConfig;
import org.seasar.dbflute.unit.core.PlainTestCase;

/**
 * @author jflute
 */
public class TnStatementFactoryImplTest extends PlainTestCase {

    // ===================================================================================
    //                                                                               Basic
    //                                                                               =====
    public void test_getResultSetType_plain() throws Exception {
        // ## Arrange ##
        TnStatementFactoryImpl impl = new TnStatementFactoryImpl();

        // ## Act ##
        int resultSetType = impl.getResultSetType(null);

        // ## Assert ##
        assertEquals(ResultSet.TYPE_FORWARD_ONLY, resultSetType);
    }

    public void test_getResultSetType_request() throws Exception {
        // ## Arrange ##
        TnStatementFactoryImpl impl = new TnStatementFactoryImpl();
        StatementConfig config = new StatementConfig();
        config.typeScrollInsensitive();

        // ## Act ##
        int resultSetType = impl.getResultSetType(config);

        // ## Assert ##
        assertEquals(ResultSet.TYPE_SCROLL_INSENSITIVE, resultSetType);
    }

    public void test_getResultSetConcurrency_plain() throws Exception {
        // ## Arrange ##
        TnStatementFactoryImpl impl = new TnStatementFactoryImpl();

        // ## Act ##
        int resultSetConcurrency = impl.getResultSetConcurrency(null);

        // ## Assert ##
        assertEquals(ResultSet.CONCUR_READ_ONLY, resultSetConcurrency);
    }

    public void test_getActualStatementConfig_plain() throws Exception {
        // ## Arrange ##
        TnStatementFactoryImpl impl = new TnStatementFactoryImpl();

        // ## Act ##
        StatementConfig actual = impl.getActualStatementConfig(null);

        // ## Assert ##
        assertNull(actual);
    }

    public void test_getActualStatementConfig_request() throws Exception {
        // ## Arrange ##
        TnStatementFactoryImpl impl = new TnStatementFactoryImpl();
        StatementConfig config = new StatementConfig();
        config.queryTimeout(1).fetchSize(2).maxRows(3);

        // ## Act ##
        StatementConfig actual = impl.getActualStatementConfig(config);

        // ## Assert ##
        assertNotNull(actual);
        assertEquals(1, actual.getQueryTimeout());
        assertEquals(2, actual.getFetchSize());
        assertEquals(3, actual.getMaxRows());
    }

    // ===================================================================================
    //                                                                     OverrideDefault
    //                                                                     ===============
    public void test_getActualStatementConfig_overrideDefault() throws Exception {
        // ## Arrange ##
        TnStatementFactoryImpl impl = new TnStatementFactoryImpl();
        {
            StatementConfig defaultConfig = new StatementConfig();
            defaultConfig.queryTimeout(10).fetchSize(20).maxRows(30);
            impl.setDefaultStatementConfig(defaultConfig);
        }
        StatementConfig config = new StatementConfig();
        config.queryTimeout(1).fetchSize(2).maxRows(3);

        // ## Act ##
        StatementConfig actual = impl.getActualStatementConfig(config);

        // ## Assert ##
        assertNotNull(actual);
        assertEquals(1, actual.getQueryTimeout());
        assertEquals(2, actual.getFetchSize());
        assertEquals(3, actual.getMaxRows());
    }

    public void test_getResultSetType_overrideDefault() throws Exception {
        // ## Arrange ##
        TnStatementFactoryImpl impl = new TnStatementFactoryImpl();
        StatementConfig defaultConfig = new StatementConfig();
        defaultConfig.typeScrollSensitive();
        impl.setDefaultStatementConfig(defaultConfig);
        StatementConfig config = new StatementConfig();
        config.typeScrollInsensitive();

        // ## Act ##
        int resultSetType = impl.getResultSetType(config);

        // ## Assert ##
        assertEquals(ResultSet.TYPE_SCROLL_INSENSITIVE, resultSetType);
    }

    // ===================================================================================
    //                                                                         DefaultOnly
    //                                                                         ===========
    public void test_getActualStatementConfig_defaultOnly_null() throws Exception {
        // ## Arrange ##
        TnStatementFactoryImpl impl = new TnStatementFactoryImpl();
        {
            StatementConfig defaultConfig = new StatementConfig();
            defaultConfig.queryTimeout(10).fetchSize(20).maxRows(30);
            impl.setDefaultStatementConfig(defaultConfig);
        }

        // ## Act ##
        StatementConfig actual = impl.getActualStatementConfig(null);

        // ## Assert ##
        assertNotNull(actual);
        assertEquals(10, actual.getQueryTimeout());
        assertEquals(20, actual.getFetchSize());
        assertEquals(30, actual.getMaxRows());
    }

    public void test_getActualStatementConfig_defaultOnly_empty() throws Exception {
        // ## Arrange ##
        TnStatementFactoryImpl impl = new TnStatementFactoryImpl();
        {
            StatementConfig defaultConfig = new StatementConfig();
            defaultConfig.queryTimeout(10).fetchSize(20).maxRows(30);
            impl.setDefaultStatementConfig(defaultConfig);
        }
        StatementConfig config = new StatementConfig();

        // ## Act ##
        StatementConfig actual = impl.getActualStatementConfig(config);

        // ## Assert ##
        assertNotNull(actual);
        assertEquals(10, actual.getQueryTimeout());
        assertEquals(20, actual.getFetchSize());
        assertEquals(30, actual.getMaxRows());
    }

    public void test_getResultSetType_defaultOnly() throws Exception {
        // ## Arrange ##
        TnStatementFactoryImpl impl = new TnStatementFactoryImpl();
        StatementConfig defaultConfig = new StatementConfig();
        defaultConfig.typeScrollSensitive();
        impl.setDefaultStatementConfig(defaultConfig);

        // ## Act ##
        int resultSetType = impl.getResultSetType(null);

        // ## Assert ##
        assertEquals(ResultSet.TYPE_SCROLL_SENSITIVE, resultSetType);
    }

    // ===================================================================================
    //                                                                        DefaultMerge
    //                                                                        ============
    public void test_getActualStatementConfig_defaultMerge_defaultAll() throws Exception {
        // ## Arrange ##
        TnStatementFactoryImpl impl = new TnStatementFactoryImpl();
        {
            StatementConfig defaultConfig = new StatementConfig();
            defaultConfig.queryTimeout(10).fetchSize(20).maxRows(30);
            impl.setDefaultStatementConfig(defaultConfig);
        }
        StatementConfig config = new StatementConfig();
        config.fetchSize(2);

        // ## Act ##
        StatementConfig actual = impl.getActualStatementConfig(config);

        // ## Assert ##
        assertNotNull(actual);
        assertEquals(10, actual.getQueryTimeout());
        assertEquals(2, actual.getFetchSize());
        assertEquals(30, actual.getMaxRows());
    }

    public void test_getActualStatementConfig_defaultMerge_defaultPart() throws Exception {
        // ## Arrange ##
        TnStatementFactoryImpl impl = new TnStatementFactoryImpl();
        {
            StatementConfig defaultConfig = new StatementConfig();
            defaultConfig.queryTimeout(10).maxRows(30);
            impl.setDefaultStatementConfig(defaultConfig);
        }
        StatementConfig config = new StatementConfig();
        config.fetchSize(2).maxRows(3);

        // ## Act ##
        StatementConfig actual = impl.getActualStatementConfig(config);

        // ## Assert ##
        assertNotNull(actual);
        assertEquals(10, actual.getQueryTimeout());
        assertEquals(2, actual.getFetchSize());
        assertEquals(3, actual.getMaxRows());
    }

    // ===================================================================================
    //                                                                     SuppressDefault
    //                                                                     ===============
    public void test_getActualStatementConfig_suppressDefault() throws Exception {
        // ## Arrange ##
        TnStatementFactoryImpl impl = new TnStatementFactoryImpl();
        {
            StatementConfig defaultConfig = new StatementConfig();
            defaultConfig.queryTimeout(10).fetchSize(20).maxRows(30);
            impl.setDefaultStatementConfig(defaultConfig);
        }
        StatementConfig config = new StatementConfig();
        config.suppressDefault();
        config.fetchSize(2);

        // ## Act ##
        StatementConfig actual = impl.getActualStatementConfig(config);

        // ## Assert ##
        assertNotNull(actual);
        assertEquals(null, actual.getQueryTimeout());
        assertEquals(2, actual.getFetchSize());
        assertEquals(null, actual.getMaxRows());
    }

    public void test_getResultSetType_suppressDefault() throws Exception {
        // ## Arrange ##
        TnStatementFactoryImpl impl = new TnStatementFactoryImpl();
        StatementConfig defaultConfig = new StatementConfig();
        defaultConfig.typeScrollSensitive();
        impl.setDefaultStatementConfig(defaultConfig);
        StatementConfig config = new StatementConfig();
        config.suppressDefault();

        // ## Act ##
        int resultSetType = impl.getResultSetType(config);

        // ## Assert ##
        assertEquals(ResultSet.TYPE_FORWARD_ONLY, resultSetType);
    }

    // ===================================================================================
    //                                                               CursorSelectFetchSize
    //                                                               =====================
    public void test_getActualStatementConfig_cursorSelectFetchSize_cursor_plain() throws Exception {
        // ## Arrange ##
        TnStatementFactoryImpl impl = new TnStatementFactoryImpl() {
            @Override
            protected boolean isSelectCursorCommand() {
                return true;
            }
        };
        impl.setCursorSelectFetchSize(200);

        // ## Act ##
        StatementConfig actual = impl.getActualStatementConfig(null);

        // ## Assert ##
        assertNotNull(actual);
        assertEquals(null, actual.getQueryTimeout());
        assertEquals(200, actual.getFetchSize());
        assertEquals(null, actual.getMaxRows());
    }

    public void test_getActualStatementConfig_cursorSelectFetchSize_cursor_merged() throws Exception {
        // ## Arrange ##
        TnStatementFactoryImpl impl = new TnStatementFactoryImpl() {
            @Override
            protected boolean isSelectCursorCommand() {
                return true;
            }
        };
        {
            StatementConfig defaultConfig = new StatementConfig();
            defaultConfig.queryTimeout(10).fetchSize(20).maxRows(30);
            impl.setDefaultStatementConfig(defaultConfig);
        }
        impl.setCursorSelectFetchSize(200);
        StatementConfig config = new StatementConfig();
        config.queryTimeout(1);

        // ## Act ##
        StatementConfig actual = impl.getActualStatementConfig(config);

        // ## Assert ##
        assertNotNull(actual);
        assertEquals(1, actual.getQueryTimeout());
        assertEquals(200, actual.getFetchSize());
        assertEquals(30, actual.getMaxRows());
    }

    public void test_getActualStatementConfig_cursorSelectFetchSize_cursor_suppressDefault() throws Exception {
        // ## Arrange ##
        TnStatementFactoryImpl impl = new TnStatementFactoryImpl() {
            @Override
            protected boolean isSelectCursorCommand() {
                return true;
            }
        };
        {
            StatementConfig defaultConfig = new StatementConfig();
            defaultConfig.queryTimeout(10).fetchSize(20).maxRows(30);
            impl.setDefaultStatementConfig(defaultConfig);
        }
        impl.setCursorSelectFetchSize(200);
        StatementConfig config = new StatementConfig();
        config.suppressDefault();
        config.queryTimeout(1);

        // ## Act ##
        StatementConfig actual = impl.getActualStatementConfig(config);

        // ## Assert ##
        assertNotNull(actual);
        assertEquals(1, actual.getQueryTimeout());
        assertEquals(null, actual.getFetchSize());
        assertEquals(null, actual.getMaxRows());
    }

    public void test_getActualStatementConfig_cursorSelectFetchSize_notCursor_plain() throws Exception {
        // ## Arrange ##
        TnStatementFactoryImpl impl = new TnStatementFactoryImpl() {
            @Override
            protected boolean isSelectCursorCommand() {
                return false;
            }
        };
        impl.setCursorSelectFetchSize(200);

        // ## Act ##
        StatementConfig actual = impl.getActualStatementConfig(null);

        // ## Assert ##
        assertNull(actual);
    }

    public void test_getActualStatementConfig_cursorSelectFetchSize_notCursor_merged() throws Exception {
        // ## Arrange ##
        TnStatementFactoryImpl impl = new TnStatementFactoryImpl() {
            @Override
            protected boolean isSelectCursorCommand() {
                return false;
            }
        };
        {
            StatementConfig defaultConfig = new StatementConfig();
            defaultConfig.queryTimeout(10).fetchSize(20).maxRows(30);
            impl.setDefaultStatementConfig(defaultConfig);
        }
        impl.setCursorSelectFetchSize(200);
        StatementConfig config = new StatementConfig();
        config.queryTimeout(1);

        // ## Act ##
        StatementConfig actual = impl.getActualStatementConfig(config);

        // ## Assert ##
        assertNotNull(actual);
        assertEquals(1, actual.getQueryTimeout());
        assertEquals(20, actual.getFetchSize());
        assertEquals(30, actual.getMaxRows());
    }

    public void test_getActualStatementConfig_cursorSelectFetchSize_notCursor_suppressDefault() throws Exception {
        // ## Arrange ##
        TnStatementFactoryImpl impl = new TnStatementFactoryImpl() {
            @Override
            protected boolean isSelectCursorCommand() {
                return false;
            }
        };
        {
            StatementConfig defaultConfig = new StatementConfig();
            defaultConfig.queryTimeout(10).fetchSize(20).maxRows(30);
            impl.setDefaultStatementConfig(defaultConfig);
        }
        impl.setCursorSelectFetchSize(200);
        StatementConfig config = new StatementConfig();
        config.suppressDefault();
        config.queryTimeout(1);

        // ## Act ##
        StatementConfig actual = impl.getActualStatementConfig(config);

        // ## Assert ##
        assertNotNull(actual);
        assertEquals(1, actual.getQueryTimeout());
        assertEquals(null, actual.getFetchSize());
        assertEquals(null, actual.getMaxRows());
    }
}
