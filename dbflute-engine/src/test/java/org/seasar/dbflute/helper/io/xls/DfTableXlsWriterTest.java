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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.seasar.dbflute.helper.StringKeyMap;
import org.seasar.dbflute.helper.dataset.DfDataSet;
import org.seasar.dbflute.infra.manage.refresh.DfRefreshResourceRequest;
import org.seasar.dbflute.unit.core.PlainTestCase;
import org.seasar.dbflute.util.DfResourceUtil;

/**
 * @author jflute
 */
public class DfTableXlsWriterTest extends PlainTestCase {

    // ===================================================================================
    //                                                                          Large Data
    //                                                                          ==========
    public void test_write_largeData_handling() throws Exception {
        // ## Arrange ##
        DfTableXlsReader existingReader = createTableXlsReader(prepareTestLargeDataXlsFile());
        DfDataSet baseSet = existingReader.read();
        log(ln() + baseSet);
        String fileName = "output-table-xls-large-data-handling.xls";
        String path = getTestCaseBuildDir().getCanonicalPath() + "/../" + fileName;
        File outputFile = new File(path);
        DfTableXlsWriter writer = new DfTableXlsWriter(new FileOutputStream(outputFile));
        writer.largeDataHandling().cellLengthLimit(5);

        // ## Act ##
        writer.write(baseSet);

        // ## Assert ##
        refresh();
        DfTableXlsReader outputReader = createTableXlsReader(outputFile);
        DfDataSet actualSet = outputReader.read();
        log(ln() + actualSet);
        String actualExp = actualSet.toString();
        assertEquals(baseSet.toString(), actualExp);
        assertFalse(actualExp.contains(DfTableXlsReader.LDATA_SHEET_NAME));
        assertFalse(actualExp.contains(DfTableXlsReader.LDATA_KEY_DELIMITER));
        assertFalse(actualExp.contains(DfTableXlsReader.LDATA_REF_PREFIX));
    }

    public void test_write_largeData_truncated() throws Exception {
        // ## Arrange ##
        DfTableXlsReader reader = createTableXlsReader(prepareTestLargeDataXlsFile());
        DfDataSet baseSet = reader.read();
        String fileName = "output-table-xls-large-data-truncated.xls";
        String path = getTestCaseBuildDir().getCanonicalPath() + "/../" + fileName;
        File outputFile = new File(path);
        DfTableXlsWriter writer = new DfTableXlsWriter(new FileOutputStream(outputFile)).cellLengthLimit(5);

        // ## Act ##
        writer.write(baseSet);

        // ## Assert ##
        refresh();
        DfTableXlsReader outputReader = createTableXlsReader(outputFile);
        DfDataSet actualSet = outputReader.read();
        log(ln() + actualSet);
        String actualExp = actualSet.toString();
        assertNotSame(baseSet.toString(), actualExp);
        assertContains(actualExp, "...");
        assertFalse(actualExp.contains(DfTableXlsReader.LDATA_SHEET_NAME));
        assertFalse(actualExp.contains(DfTableXlsReader.LDATA_KEY_DELIMITER));
        assertFalse(actualExp.contains(DfTableXlsReader.LDATA_REF_PREFIX));
    }

    // ===================================================================================
    //                                                                         EmptyString
    //                                                                         ===========
    public void test_write_emptyString_quoted() throws Exception {
        // ## Arrange ##
        DfTableXlsReader existingReader = createTableXlsReader(prepareTestLargeDataXlsFile());
        DfDataSet baseSet = existingReader.read();
        baseSet.getTable(0).getRow(1).setValue(1, "");
        String baseExp = baseSet.toString();
        log(ln() + baseExp);
        assertContains(baseExp, ", ,");
        assertContains(baseExp, ", null,");
        String fileName = "output-table-xls-large-data-handling.xls";
        String path = getTestCaseBuildDir().getCanonicalPath() + "/../" + fileName;
        File outputFile = new File(path);
        DfTableXlsWriter writer = new DfTableXlsWriter(new FileOutputStream(outputFile));
        writer.largeDataHandling().quoteEmptyString().cellLengthLimit(5);

        // ## Act ##
        writer.write(baseSet);

        // ## Assert ##
        refresh();
        DfTableXlsReader outputReader = createTableXlsReader(outputFile);
        DfDataSet actualSet = outputReader.read();
        log(ln() + actualSet);
        String actualExp = actualSet.toString();
        assertNotSame(baseExp, actualExp);
        assertContains(actualExp, ", \"\",");
        assertContains(actualExp, ", null,");
        assertFalse(actualExp.contains(DfTableXlsReader.LDATA_SHEET_NAME));
        assertFalse(actualExp.contains(DfTableXlsReader.LDATA_KEY_DELIMITER));
        assertFalse(actualExp.contains(DfTableXlsReader.LDATA_REF_PREFIX));
    }

    // ===================================================================================
    //                                                                               Point
    //                                                                               =====
    public void test_toLargeDataSplitList_basic() throws Exception {
        // ## Arrange ##
        DfTableXlsWriter writer = new DfTableXlsWriter(new ByteArrayOutputStream()).cellLengthLimit(3);

        // ## Act ##
        List<String> splitList = writer.toLargeDataSplitList("abcdefghijklmnop");

        // ## Assert ##
        log(splitList);
        assertEquals("abc", splitList.get(0));
        assertEquals("def", splitList.get(1));
        assertEquals("ghi", splitList.get(2));
        assertEquals("jkl", splitList.get(3));
        assertEquals("mno", splitList.get(4));
        assertEquals("p", splitList.get(5));
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

    protected DfTableXlsReader createTableXlsReader(File xlsFile) {
        final Map<String, String> tableNameMap = StringKeyMap.createAsCaseInsensitive();
        final Map<String, List<String>> notTrimTableColumnMap = StringKeyMap.createAsCaseInsensitive();
        final Map<String, List<String>> stringEmptyTableColumnMap = StringKeyMap.createAsCaseInsensitive();
        return new DfTableXlsReader(xlsFile, tableNameMap, notTrimTableColumnMap, stringEmptyTableColumnMap, null,
                false);
    }

    protected void refresh() throws IOException {
        new DfRefreshResourceRequest(Arrays.asList("dbflute"), "http://localhost:8386/").refreshResources();
    }
}
