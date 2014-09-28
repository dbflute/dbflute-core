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
package org.seasar.dbflute.logic.replaceschema.dataassert;

import java.io.File;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.dbflute.exception.DfTakeFinallyAssertionFailureCountNotExistsException;
import org.seasar.dbflute.exception.DfTakeFinallyAssertionFailureCountNotZeroException;
import org.seasar.dbflute.exception.DfTakeFinallyAssertionFailureListNotExistsException;
import org.seasar.dbflute.exception.DfTakeFinallyAssertionFailureListNotZeroException;
import org.seasar.dbflute.exception.DfTakeFinallyAssertionInvalidMarkException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.util.DfStringUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.9.5.4 (2009/08/07 Friday)
 */
public class DfDataAssertProvider {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance. */
    private static Log _log = LogFactory.getLog(DfDataAssertProvider.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Map<String, DfDataAssertHandler> _assertHandlerMap = new LinkedHashMap<String, DfDataAssertHandler>();
    {
        _assertHandlerMap.put("assertCountZero", new DfDataAssertHandler() {
            public void handle(File sqlFile, Statement st, String sql) throws SQLException {
                assertCountZero(sqlFile, st, sql);
            }
        });
        _assertHandlerMap.put("assertCountExists", new DfDataAssertHandler() {
            public void handle(File sqlFile, Statement st, String sql) throws SQLException {
                assertCountExists(sqlFile, st, sql);
            }
        });
        _assertHandlerMap.put("assertListZero", new DfDataAssertHandler() {
            public void handle(File sqlFile, Statement st, String sql) throws SQLException {
                assertListZero(sqlFile, st, sql);
            }
        });
        _assertHandlerMap.put("assertListExists", new DfDataAssertHandler() {
            public void handle(File sqlFile, Statement st, String sql) throws SQLException {
                assertListExists(sqlFile, st, sql);
            }
        });
    }
    protected final String _envType;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfDataAssertProvider(String envType) {
        _envType = envType;
    }

    // ===================================================================================
    //                                                                             Provide
    //                                                                             =======
    /**
     * @param sql The SQL string to assert. (NotNull)
     * @return The handle of data assert. (NullAllowed: if null, means not found)
     */
    public DfDataAssertHandler provideDataAssertHandler(String sql) {
        if (!sql.contains("--")) {
            return null;
        }
        final String starter = "#df:";
        final String terminator = "#";
        final String typeAtMark = "@";

        // resolve comment spaces
        sql = DfStringUtil.replace(sql, "-- #", "--#");

        final Set<Entry<String, DfDataAssertHandler>> entrySet = _assertHandlerMap.entrySet();
        DfDataAssertHandler defaultHandler = null;
        for (Entry<String, DfDataAssertHandler> entry : entrySet) {
            final String key = entry.getKey();

            // find plain mark
            final String firstMark = "--" + starter + key + terminator;
            if (sql.contains(firstMark)) {
                return entry.getValue();
            }

            // find with envType
            final String secondMark = "--" + starter + key + typeAtMark + _envType + terminator;
            if (sql.contains(secondMark)) {
                return entry.getValue();
            }

            // not found but set up default handler with check
            final String thirdMark = "--" + starter + key;
            final int keyIndex = sql.indexOf(thirdMark);
            if (keyIndex < 0) {
                continue;
            }
            String rearString = sql.substring(keyIndex + thirdMark.length());
            if (rearString.contains(ln())) {
                rearString = rearString.substring(0, rearString.indexOf(ln()));
            }
            if (!rearString.contains(terminator)) {
                String msg = "The data assert mark should ends '" + terminator + "':" + ln() + sql;
                throw new DfTakeFinallyAssertionInvalidMarkException(msg);
            }
            final String option = rearString.substring(0, rearString.indexOf(terminator));
            if (option.startsWith(typeAtMark)) {
                final String envType = Srl.substringFirstRear(option, typeAtMark);
                defaultHandler = createDefaultHandler(key, envType);
            } else {
                String msg = "Unknown option '" + option + "':" + ln() + sql;
                throw new DfTakeFinallyAssertionInvalidMarkException(msg);
            }
        }
        return defaultHandler; // when not found
    }

    protected DfDataAssertHandler createDefaultHandler(String key, String envType) {
        final String msg = "...Skipping for the different envType: " + key + "@" + envType + "/" + _envType;
        return new DfDataAssertHandler() {
            public void handle(File sqlFile, Statement st, String sql) throws SQLException {
                _log.info(msg);
            }
        };
    }

    // ===================================================================================
    //                                                                              Assert
    //                                                                              ======
    protected void assertCountZero(File sqlFile, Statement st, String sql) throws SQLException {
        assertCount(sqlFile, st, sql, false);
    }

    protected void assertCountExists(File sqlFile, Statement st, String sql) throws SQLException {
        assertCount(sqlFile, st, sql, true);
    }

    protected void assertCount(File sqlFile, Statement st, String sql, boolean exists) throws SQLException {
        if (st == null) {
            String msg = "The argument 'st' should not be null: sqlFile=" + sqlFile;
            throw new IllegalStateException(msg);
        }
        ResultSet rs = null;
        try {
            rs = st.executeQuery(sql);
            int count = 0;
            while (rs.next()) { // one loop only!
                count = rs.getInt(1);
                break;
            }
            if (exists) {
                if (count == 0) {
                    throwAssertionFailureCountNotExistsException(sqlFile, sql, count);
                } else {
                    String result = "[RESULT]: count=" + count;
                    _log.info(result);
                }
            } else {
                if (count > 0) {
                    throwAssertionFailureCountNotZeroException(sqlFile, sql, count);
                }
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
    }

    protected void assertListZero(File sqlFile, Statement st, String sql) throws SQLException {
        assertList(sqlFile, st, sql, false);
    }

    protected void assertListExists(File sqlFile, Statement st, String sql) throws SQLException {
        assertList(sqlFile, st, sql, true);
    }

    protected void assertList(File sqlFile, Statement st, String sql, boolean exists) throws SQLException {
        if (st == null) {
            String msg = "The argument 'st' should not be null: sqlFile=" + sqlFile;
            throw new IllegalStateException(msg);
        }
        ResultSet rs = null;
        try {
            rs = st.executeQuery(sql);
            final ResultSetMetaData metaData = rs.getMetaData();
            final int columnCount = metaData.getColumnCount();
            final List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
            int count = 0;
            while (rs.next()) {
                Map<String, String> recordMap = new LinkedHashMap<String, String>();
                for (int i = 1; i <= columnCount; i++) {
                    recordMap.put(metaData.getColumnName(i), rs.getString(i));
                }
                resultList.add(recordMap);
                ++count;
            }
            if (exists) {
                if (count == 0) {
                    throwAssertionFailureListNotExistsException(sqlFile, sql, count, resultList);
                } else {
                    String result = "[RESULT]: count=" + count + ln();
                    for (Map<String, String> recordMap : resultList) {
                        result = result + recordMap + ln();
                    }
                    _log.info(result.trim());
                }
            } else {
                if (count > 0) {
                    throwAssertionFailureListNotZeroException(sqlFile, sql, count, resultList);
                }
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
    }

    protected void throwAssertionFailureCountNotZeroException(File sqlFile, String sql, int resultCount) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The SQL expects ZERO but the result was NOT ZERO.");
        br.addItem("Advice");
        br.addElement("Make sure your business data constraints.");
        br.addItem("SQL File");
        br.addElement(sqlFile);
        br.addItem("Executed SQL");
        br.addElement(sql);
        br.addItem("Result Count");
        br.addElement(resultCount);
        final String msg = br.buildExceptionMessage();
        throw new DfTakeFinallyAssertionFailureCountNotZeroException(msg);
    }

    protected void throwAssertionFailureCountNotExistsException(File sqlFile, String sql, int resultCount) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The SQL expects EXISTS but the result was NOT EXISTS.");
        br.addItem("Advice");
        br.addElement("Make sure your business data constraints.");
        br.addItem("SQL File");
        br.addElement(sqlFile);
        br.addItem("Executed SQL");
        br.addElement(sql);
        br.addItem("Result Count");
        br.addElement(resultCount);
        final String msg = br.buildExceptionMessage();
        throw new DfTakeFinallyAssertionFailureCountNotExistsException(msg);
    }

    protected void throwAssertionFailureListNotZeroException(File sqlFile, String sql, int resultCount,
            List<Map<String, String>> resultList) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The SQL expects ZERO but the result was NOT ZERO.");
        br.addItem("Advice");
        br.addElement("Make sure your business data constraints.");
        br.addItem("SQL File");
        br.addElement(sqlFile);
        br.addItem("Executed SQL");
        br.addElement(sql);
        br.addItem("Result Count");
        br.addElement(resultCount);
        br.addItem("Result List");
        for (Map<String, String> recordMap : resultList) {
            br.addElement(recordMap);
        }
        final String msg = br.buildExceptionMessage();
        throw new DfTakeFinallyAssertionFailureListNotZeroException(msg);
    }

    protected void throwAssertionFailureListNotExistsException(File sqlFile, String sql, int resultCount,
            List<Map<String, String>> resultList) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The SQL expects EXISTS but the result was NOT EXISTS.");
        br.addItem("Advice");
        br.addElement("Make sure your business data constraints.");
        br.addItem("SQL File");
        br.addElement(sqlFile);
        br.addItem("Executed SQL");
        br.addElement(sql);
        br.addItem("Result Count");
        br.addElement(resultCount);
        br.addItem("Result List");
        for (Map<String, String> recordMap : resultList) {
            br.addElement(recordMap);
        }
        final String msg = br.buildExceptionMessage();
        throw new DfTakeFinallyAssertionFailureListNotExistsException(msg);
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String ln() {
        return "\n";
    }
}
