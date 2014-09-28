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
package org.seasar.dbflute.helper.io.xls;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.seasar.dbflute.helper.StringKeyMap;
import org.seasar.dbflute.helper.dataset.DfDataColumn;
import org.seasar.dbflute.helper.dataset.DfDataRow;
import org.seasar.dbflute.helper.dataset.DfDataSet;
import org.seasar.dbflute.helper.dataset.DfDataTable;
import org.seasar.dbflute.unit.core.PlainTestCase;
import org.seasar.dbflute.util.DfResourceUtil;

/**
 * @author jflute
 * @since 0.7.9 (2008/08/24 Monday)
 */
public class DfTableXlsReaderTest extends PlainTestCase {

    // ===================================================================================
    //                                                                          Basic Read
    //                                                                          ==========
    public void test_read_basic() throws IOException {
        // ## Arrange ##
        final File xlsFile = prepareTestBasicXlsFile();
        final DfTableXlsReader reader = createTableXlsReader(xlsFile, null, false);

        // ## Act ##
        final DfDataSet dataSet = reader.read();

        // ## Assert ##
        log("[DataSet]:" + ln() + dataSet);
        final int tableSize = dataSet.getTableSize();
        assertTrue(tableSize > 0);
        for (int tableIndex = 0; tableIndex < tableSize; tableIndex++) {
            final DfDataTable table = dataSet.getTable(tableIndex);
            final int columnSize = table.getColumnSize();
            assertTrue(columnSize > 0);
            final int rowSize = table.getRowSize();
            assertTrue(rowSize > 0);
            for (int rowIndex = 0; rowIndex < rowSize; rowIndex++) {
                final DfDataRow row = table.getRow(rowIndex);
                for (int columnIndex = 0; columnIndex < columnSize; columnIndex++) {
                    final DfDataColumn column = table.getColumn(columnIndex);
                    final String columnDbName = column.getColumnDbName();
                    final Object value = row.getValue(columnDbName);
                    if (columnDbName.equals("AAA")) {
                        assertNotNull(value);
                    } else if (columnDbName.equals("BBB")) {
                        markHere("nullBBB");
                    } else if (columnDbName.equals("CCC")) {
                        assertNotNull(value);
                    } else if (columnDbName.equals("DDD")) {
                        assertNotNull(value);
                        String str = (String) value;
                        if (str.length() > str.trim().length()) {
                            markHere("trimmed_DDD");
                        }
                    } else if (columnDbName.equals("EEE")) {
                        assertNotNull(value);
                        String str = (String) value;
                        if (str.length() > str.trim().length()) {
                            markHere("trimmed_EEE");
                        }
                    }
                }
            }
        }
        assertMarked("nullBBB");
        assertMarked("trimmed_DDD");
        assertMarked("trimmed_EEE");
    }

    public void test_read_rtrim() throws IOException {
        // ## Arrange ##
        final File xlsFile = prepareTestBasicXlsFile();
        final DfTableXlsReader reader = createTableXlsReader(xlsFile, null, true);

        // ## Act ##
        final DfDataSet dataSet = reader.read();

        // ## Assert ##
        log("[DataSet]:" + ln() + dataSet);
        final int tableSize = dataSet.getTableSize();
        assertTrue(tableSize > 0);
        for (int tableIndex = 0; tableIndex < tableSize; tableIndex++) {
            final DfDataTable table = dataSet.getTable(tableIndex);
            final int columnSize = table.getColumnSize();
            assertTrue(columnSize > 0);
            final int rowSize = table.getRowSize();
            assertTrue(rowSize > 0);
            for (int rowIndex = 0; rowIndex < rowSize; rowIndex++) {
                final DfDataRow row = table.getRow(rowIndex);
                for (int columnIndex = 0; columnIndex < columnSize; columnIndex++) {
                    final DfDataColumn column = table.getColumn(columnIndex);
                    final String columnDbName = column.getColumnDbName();
                    final Object value = row.getValue(columnDbName);
                    if (columnDbName.equals("AAA")) {
                        assertNotNull(value);
                    } else if (columnDbName.equals("BBB")) {
                        markHere("nullBBB");
                    } else if (columnDbName.equals("CCC")) {
                        assertNotNull(value);
                    } else if (columnDbName.equals("DDD")) {
                        assertNotNull(value);
                        String str = (String) value;
                        if (str.length() > str.trim().length()) {
                            fail();
                        }
                    } else if (columnDbName.equals("EEE")) {
                        assertNotNull(value);
                        String str = (String) value;
                        if (str.length() > str.trim().length()) {
                            markHere("trimmed_EEE"); // because of not trimmed column
                        }
                    }
                }
            }
        }
        assertMarked("nullBBB");
        assertMarked("trimmed_EEE");
    }

    // ===================================================================================
    //                                                                          Large Data
    //                                                                          ==========
    public void test_read_largeData() throws IOException {
        // ## Arrange ##
        final File xlsFile = prepareTestLargeDataXlsFile();
        final DfTableXlsReader reader = createTableXlsReader(xlsFile, null, false);

        // ## Act ##
        final DfDataSet dataSet = reader.read();

        // ## Assert ##
        log("[DataSet]:" + ln() + dataSet);
        final String expected1stFoo = "large 1st foo second row";
        final String expected2ndFoo = "large 2nd foo";
        final String expected2ndBar = "large 2nd bar&second row more row starts with space ends with space {brace}{brace} terminal row ";
        int tableSize = dataSet.getTableSize();
        assertTrue(tableSize > 0);
        for (int tableIndex = 0; tableIndex < tableSize; tableIndex++) {
            final DfDataTable table = dataSet.getTable(tableIndex);
            final String tableDbName = table.getTableDbName();
            log("[" + tableDbName + "]");
            final int columnSize = table.getColumnSize();
            final int rowSize = table.getRowSize();
            for (int rowIndex = 0; rowIndex < rowSize; rowIndex++) {
                final DfDataRow row = table.getRow(rowIndex);
                log("(" + row.getRowNumber() + ")");
                for (int columnIndex = 0; columnIndex < columnSize; columnIndex++) {
                    final DfDataColumn column = table.getColumn(columnIndex);
                    final String columnDbName = column.getColumnDbName();
                    final Object value = row.getValue(columnDbName);
                    log(columnDbName + " = " + value);
                    if (tableDbName.equals("FIRST_TABLE")) {
                        if (columnDbName.equals("CCC") && value != null && value.equals(expected1stFoo)) {
                            markHere("expected1stFoo");
                        }
                    }
                    if (tableDbName.equals("SECOND_TABLE")) {
                        if (columnDbName.equals("BBB") && value != null && value.equals(expected2ndFoo)) {
                            markHere("expected2ndFoo");
                        }
                        if (columnDbName.equals("CCC") && value != null && value.equals(expected2ndBar)) {
                            markHere("expected2ndBar");
                        }
                    }
                }
            }
        }
        assertMarked("expected1stFoo");
        assertMarked("expected2ndFoo");
        assertMarked("expected2ndBar");
        String actualExp = dataSet.toString();
        assertFalse(actualExp.contains(DfTableXlsReader.LDATA_SHEET_NAME));
        assertFalse(actualExp.contains(DfTableXlsReader.LDATA_KEY_DELIMITER));
        assertFalse(actualExp.contains(DfTableXlsReader.LDATA_REF_PREFIX));
    }

    // ===================================================================================
    //                                                                          Sheet Name
    //                                                                          ==========
    public void test_isCommentOutSheet_basic() {
        // ## Arrange ##
        final DfTableXlsReader reader = createEmptyXlsReader(null);

        // ## Act & Assert ##
        assertTrue(reader.isCommentOutSheet("#MST_STATUS"));
        assertFalse(reader.isSkipSheet("MST_STATUS"));
    }

    public void test_isSkipSheet_basic() {
        // ## Arrange ##
        final DfTableXlsReader reader = createEmptyXlsReader(Pattern.compile("MST.+"));

        // ## Act & Assert ##
        assertTrue(reader.isSkipSheet("MST_STATUS"));
        assertTrue(reader.isSkipSheet("MST_"));
        assertFalse(reader.isSkipSheet("MST"));
        assertFalse(reader.isSkipSheet("MS_STATUS"));
        assertFalse(reader.isSkipSheet("AMST_STATUS"));
        assertFalse(reader.isSkipSheet("9MST_STATUS"));
        assertFalse(reader.isSkipSheet("#MST_STATUS"));
    }

    // ===================================================================================
    //                                                                         Test Helper
    //                                                                         ===========
    protected File prepareTestBasicXlsFile() throws IOException {
        final File buildDir = DfResourceUtil.getBuildDir(getClass());
        return new File(buildDir.getCanonicalPath() + "/xls/table-xls-basic.xls");
    }

    protected File prepareTestLargeDataXlsFile() throws IOException {
        final File buildDir = DfResourceUtil.getBuildDir(getClass());
        return new File(buildDir.getCanonicalPath() + "/xls/table-xls-large-data.xls");
    }

    protected DfTableXlsReader createTableXlsReader(File xlsFile, Pattern skipSheetPattern, boolean rtrimCellValue) {
        final Map<String, String> tableNameMap = StringKeyMap.createAsCaseInsensitive();
        final Map<String, List<String>> notTrimTableColumnMap = prepareNotTrimTableColumnMap();
        final Map<String, List<String>> stringEmptyTableColumnMap = prepareStringEmptyTableColumnMap();
        return new DfTableXlsReader(xlsFile, tableNameMap, notTrimTableColumnMap, stringEmptyTableColumnMap,
                skipSheetPattern, rtrimCellValue);
    }

    protected Map<String, List<String>> prepareNotTrimTableColumnMap() {
        final Map<String, List<String>> notTrimTableColumnMap = StringKeyMap.createAsCaseInsensitive();
        notTrimTableColumnMap.put("TEST_TABLE", Arrays.asList("EEE"));
        return notTrimTableColumnMap;
    }

    protected Map<String, List<String>> prepareStringEmptyTableColumnMap() {
        final Map<String, List<String>> stringEmptyTableColumnMap = StringKeyMap.createAsCaseInsensitive();
        stringEmptyTableColumnMap.put("TEST_TABLE", Arrays.asList("CCC"));
        return stringEmptyTableColumnMap;
    }

    protected DfTableXlsReader createEmptyXlsReader(Pattern skipSheetPattern) {
        final Map<String, String> tableNameMap = StringKeyMap.createAsCaseInsensitive();
        final Map<String, List<String>> notTrimTableColumnMap = StringKeyMap.createAsCaseInsensitive();
        final Map<String, List<String>> stringEmptyTableColumnMap = StringKeyMap.createAsCaseInsensitive();
        return new DfTableXlsReaderEmpty(tableNameMap, notTrimTableColumnMap, stringEmptyTableColumnMap,
                skipSheetPattern);
    }

    protected static class DfTableXlsReaderEmpty extends DfTableXlsReader {

        public DfTableXlsReaderEmpty(Map<String, String> tableNameMap, Map<String, List<String>> notTrimTableColumnMap,
                Map<String, List<String>> stringEmptyTableColumnMap, Pattern skipSheetPattern) {
            super(null, tableNameMap, notTrimTableColumnMap, stringEmptyTableColumnMap, skipSheetPattern, false);
        }

        @Override
        protected InputStream toStream(File xlsFile) {
            return new ByteArrayInputStream(new byte[] {});
        }

        @Override
        protected void setupWorkbook(InputStream ins) {
            // do nothing
        }
    }
}
