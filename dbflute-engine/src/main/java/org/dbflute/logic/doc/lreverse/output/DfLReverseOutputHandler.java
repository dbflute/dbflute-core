/*
 * Copyright 2014-2024 the original author or authors.
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
package org.dbflute.logic.doc.lreverse.output;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.torque.engine.database.model.Column;
import org.apache.torque.engine.database.model.Table;
import org.dbflute.DfBuildProperties;
import org.dbflute.helper.dataset.DfDataRow;
import org.dbflute.helper.dataset.DfDataSet;
import org.dbflute.helper.dataset.DfDataTable;
import org.dbflute.helper.dataset.types.DfDtsColumnTypes;
import org.dbflute.helper.io.xls.DfTableXlsWriter;
import org.dbflute.helper.jdbc.facade.DfJFadCursorCallback;
import org.dbflute.helper.jdbc.facade.DfJFadCursorHandler;
import org.dbflute.helper.jdbc.facade.DfJFadResultSetWrapper;
import org.dbflute.helper.token.file.FileToken;
import org.dbflute.logic.doc.lreverse.DfLReverseOutputResource;
import org.dbflute.properties.DfAdditionalTableProperties;
import org.dbflute.properties.DfLittleAdjustmentProperties;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 0.8.3 (2008/10/28 Tuesday)
 */
public class DfLReverseOutputHandler {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The logger instance for this class. (NotNull) */
    private static final Logger _log = LoggerFactory.getLogger(DfLReverseOutputHandler.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DataSource _dataSource;
    protected boolean _containsCommonColumn;

    // -----------------------------------------------------
    //                                        Delimiter Data
    //                                        --------------
    protected String _largeDataDir; // option for large data (delimiter data when large xls)
    protected boolean _delimiterDataBasis; // option for delimiter data @since 1.2.9
    protected boolean _delimiterDataMinimallyQuoted; // option for large data

    // -----------------------------------------------------
    //                                              Xls Data
    //                                              --------
    protected int _xlsLimit = 65000; // as default
    protected boolean _suppressLargeDataHandling; // default is in writer
    protected boolean _suppressQuoteEmptyString; // default is in writer
    protected Integer _cellLengthLimit; // default is in writer

    // -----------------------------------------------------
    //                                            Saved Data
    //                                            ----------
    protected final Map<String, Table> _tableNameMap = new LinkedHashMap<String, Table>();

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfLReverseOutputHandler(DataSource dataSource) {
        _dataSource = dataSource;
    }

    // ===================================================================================
    //                                                                         Output Data
    //                                                                         ===========
    /**
     * Output load data to data file. (using dataSource)
     * @param tableMap The map of table to extract. (NotNull)
     * @param recordLimit The limit of extracted record. (MinusAllowed: if minus, no limit)
     * @param outputDataFile The data file to output. (NotNull)
     * @param resource The resource information of output data. (NotNull)
     * @param sectionInfoList The list of section info for display. (NotNull)
     */
    public void outputData(Map<String, Table> tableMap, int recordLimit, File outputDataFile, DfLReverseOutputResource resource,
            List<String> sectionInfoList) {
        filterUnsupportedTable(tableMap);
        final DfLReverseDataExtractor extractor = new DfLReverseDataExtractor(_dataSource);
        extractor.setExtractingLimit(recordLimit);
        extractor.setLargeBorder(calculateLargeBorder());
        final Map<String, DfLReverseDataResult> loadDataMap = extractor.extractData(tableMap);
        transferDataToFile(tableMap, loadDataMap, recordLimit, outputDataFile, resource, sectionInfoList);
    }

    protected int calculateLargeBorder() {
        if (_delimiterDataBasis) {
            return 0; // treated as large
        } else { // tranditional
            return _xlsLimit;
        }
    }

    protected void filterUnsupportedTable(Map<String, Table> tableInfoMap) {
        // additional tables are unsupported here
        // because it's not an important function
        final Map<String, Object> additionalTableMap = getAdditionalTableProperties().getAdditionalTableMap();
        for (String tableDbName : additionalTableMap.keySet()) {
            if (tableInfoMap.containsKey(tableDbName)) {
                _log.info("...Skipping additional table: " + tableDbName);
                tableInfoMap.remove(tableDbName);
            }
        }
    }

    /**
     * Transfer load data to data file. (state-less)
     * @param tableMap The map of table. (NotNull)
     * @param loadDataMap The map of load data. (NotNull)
     * @param recordLimit The limit of extracted record. (MinusAllowed: if minus, no limit)
     * @param outputDataFile The data file to output. (NotNull)
     * @param resource The resource information of output data. (NotNull)
     * @param sectionInfoList The list of section info for display. (NotNull)
     */
    protected void transferDataToFile(Map<String, Table> tableMap, Map<String, DfLReverseDataResult> loadDataMap, int recordLimit,
            File outputDataFile, DfLReverseOutputResource resource, List<String> sectionInfoList) {
        final DfDataSet dataSet = new DfDataSet();
        int sheetNumber = 1;
        for (Entry<String, Table> entry : tableMap.entrySet()) {
            final String tableDbName = entry.getKey();
            final Table table = entry.getValue();
            final DfLReverseDataResult dataResult = loadDataMap.get(tableDbName);
            if (dataResult.isLargeData()) { // delimiter basis or large table for xls
                outputLargeData(outputDataFile, table, dataResult, recordLimit, resource, sheetNumber, sectionInfoList);
            } else { // normal size xls
                final List<Map<String, String>> extractedList = dataResult.getResultList();
                setupXlsDataTable(dataSet, table, extractedList, sheetNumber, sectionInfoList);
            }
            ++sheetNumber;
        }
        if (dataSet.getTableSize() > 0) {
            writeXlsData(dataSet, outputDataFile);
        }
    }

    // ===================================================================================
    //                                                                            Xls Data
    //                                                                            ========
    protected void setupXlsDataTable(DfDataSet dataSet, Table table, List<Map<String, String>> extractedList, int sheetNumber,
            List<String> sectionInfoList) {
        final List<Map<String, String>> recordList;
        {
            final String tableInfo = "  " + table.getTableDispName() + " (" + extractedList.size() + ")";
            _log.info(tableInfo);
            sectionInfoList.add(tableInfo);
            if (extractedList.size() > calculateLargeBorder()) {
                recordList = extractedList.subList(0, calculateLargeBorder()); // just in case
            } else {
                recordList = extractedList;
            }
        }
        final DfDataTable dataTable = new DfDataTable(resolveSheetName(table, sheetNumber));
        final List<Column> columnList = table.getColumnList();
        for (Column column : columnList) {
            if (isExceptCommonColumn(column)) {
                continue;
            }
            dataTable.addColumn(column.getName(), DfDtsColumnTypes.STRING);
        }
        for (Map<String, String> recordMap : recordList) {
            final Set<String> columnNameSet = recordMap.keySet();
            final DfDataRow dataRow = dataTable.addRow();
            for (String columnName : columnNameSet) {
                if (!dataTable.hasColumn(columnName)) {
                    continue; // basically excepted common columns
                }
                final String value = recordMap.get(columnName);
                dataRow.addValue(columnName, value);
            }
        }
        dataSet.addTable(dataTable);
    }

    protected String resolveSheetName(Table table, int sheetNumber) {
        String sheetName = deriveSheetName(table);
        if (sheetName.length() > 30) { // restriction of excel
            final String middleParts = sheetName.substring(0, 25);
            boolean resolved = false;
            int basePoint = 0;
            while (true) {
                final String suffixParts = middleParts.substring(basePoint, basePoint + 3);
                sheetName = "$" + middleParts + "_" + suffixParts;
                if (!_tableNameMap.containsKey(sheetName)) {
                    resolved = true;
                    break;
                }
                if (basePoint > 20) {
                    break;
                }
                ++basePoint;
                continue;
            }
            if (!resolved) {
                final String indexExp = (sheetNumber < 10 ? "0" + sheetNumber : String.valueOf(sheetNumber));
                sheetName = "$" + middleParts + "_" + indexExp;
            }
            _tableNameMap.put(sheetName, table);
        }
        return sheetName;
    }

    protected String deriveSheetName(Table table) {
        // if schema-driven, output with table name with schema as sheet name
        // it depends on ReplaceSchema specification whether the data file can be loaded or not
        final String tableDbName = table.getTableDbName();

        // sheet name's case is depends on table display name's case
        // because sheet name (as table name) is also display name
        final DfLittleAdjustmentProperties prop = getLittleAdjustmentProperties();
        return prop.isTableDispNameUpperCase() ? tableDbName.toUpperCase() : tableDbName;
    }

    protected boolean isExceptCommonColumn(Column column) {
        return !_containsCommonColumn && column.isCommonColumn();
    }

    protected void writeXlsData(DfDataSet dataSet, File xlsFile) {
        final DfTableXlsWriter writer = createTableXlsWriter(xlsFile);
        try {
            writer.write(dataSet); // flush
        } catch (RuntimeException e) {
            String msg = "Failed to write the xls file: " + xlsFile + " tables=" + dataSet.getTableSize();
            throw new IllegalStateException(msg, e);
        }
    }

    protected DfTableXlsWriter createTableXlsWriter(File xlsFile) {
        // the XLS file should have all string cell type basically for replace-schema
        final DfTableXlsWriter writer = new DfTableXlsWriter(xlsFile);
        writer.stringCellType();
        if (!_suppressLargeDataHandling) {
            writer.largeDataHandling();
        }
        if (!_suppressQuoteEmptyString) {
            writer.quoteEmptyString();
        }
        if (_cellLengthLimit != null) {
            writer.cellLengthLimit(_cellLengthLimit);
        }
        return writer;
    }

    // ===================================================================================
    //                                                                          Large Data
    //                                                                          ==========
    protected void outputLargeData(File outputDataFile, Table table, DfLReverseDataResult dataResult, int recordLimit,
            DfLReverseOutputResource resource, int sheetNumber, List<String> sectionInfoList) {
        if (_largeDataDir == null) {
            return;
        }
        final File largeDataDir = new File(_largeDataDir);
        if (!largeDataDir.exists()) {
            largeDataDir.mkdirs();
        }
        final FileToken fileToken = new FileToken();
        final File delimiterFile = prepareLargeDelimiterFile(outputDataFile, table, resource, sheetNumber, largeDataDir);
        final List<String> columnNameList = new ArrayList<String>();
        for (Column column : table.getColumnList()) {
            if (!_containsCommonColumn && column.isCommonColumn()) {
                continue;
            }
            columnNameList.add(column.getName());
        }
        final DfJFadCursorCallback cursorCallback = dataResult.getCursorCallback(); // not null here
        cursorCallback.select(new DfJFadCursorHandler() {
            int writtenRowCount = 0;

            public void handle(final DfJFadResultSetWrapper wrapper) {
                FileOutputStream stream = null;
                try {
                    stream = new FileOutputStream(delimiterFile);
                    makeFile(fileToken, stream, wrapper, columnNameList, recordLimit);
                } catch (IOException e) {
                    handleDelimiterDataFailureException(table, delimiterFile, e);
                } finally {
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException ignored) {}
                    }
                }
                final String delimiterInfo = "  " + delimiterFile.getPath() + " (" + writtenRowCount + ")";
                _log.info(delimiterInfo);
                sectionInfoList.add(delimiterInfo);
            }

            protected void makeFile(FileToken fileToken, FileOutputStream stream, DfJFadResultSetWrapper wrapper,
                    List<String> columnNameList, int recordLimit) throws FileNotFoundException, IOException {
                fileToken.make(stream, writer -> {
                    while (wrapper.next()) {
                        if (recordLimit >= 0 && recordLimit < writtenRowCount) {
                            break;
                        }
                        final List<String> valueList = new ArrayList<String>();
                        for (String columnName : columnNameList) {
                            valueList.add(wrapper.getString(columnName));
                        }
                        writer.writeRow(valueList);
                        ++writtenRowCount;
                    }
                }, op -> {
                    op.encodeAsUTF8().separateByLf().delimitateByTab().headerInfo(columnNameList);
                    if (_delimiterDataMinimallyQuoted) { // since 1.2.9
                        op.quoteMinimally(); // to fit with application policy
                    }
                });
            }
        });
    }

    protected File prepareLargeDelimiterFile(File outputDataFile, Table table, DfLReverseOutputResource resource, int sheetNumber,
            File largeDataDir) {
        final File delimiterFile;
        if (_delimiterDataBasis) { // @since 1.2.9
            // delimiter basis always is treated as large data using cursor for performance
            delimiterFile = outputDataFile;
        } else {
            delimiterFile = createLargeXlsDelimiterFile(table, resource, sheetNumber, largeDataDir);
        }
        return delimiterFile;
    }

    protected File createLargeXlsDelimiterFile(Table table, DfLReverseOutputResource resource, int sheetNumber, File largeDataDir) {
        final String delimiterFilePath = buildLargeXlsDelimiterFilePath(table, resource, sheetNumber, largeDataDir);
        return new File(delimiterFilePath);
    }

    protected String buildLargeXlsDelimiterFilePath(Table table, DfLReverseOutputResource resource, int sheetNumber, File largeDataDir) {
        final String dirPath = resolvePath(largeDataDir);
        final String fileName = buildLargeXlsDelimiterFilePrefix(resource, sheetNumber) + table.getTableDispName() + ".tsv";
        return dirPath + "/" + fileName;
    }

    protected String buildLargeXlsDelimiterFilePrefix(DfLReverseOutputResource resource, int sheetNumber) {
        final int sectionNo = resource.getSectionNo();
        final String sectionPrefix = sectionNo < 10 ? "0" + sectionNo : String.valueOf(sectionNo);
        final String sheetPrefix = sheetNumber < 10 ? "0" + sheetNumber : String.valueOf(sheetNumber);
        return "cyclic_" + sectionPrefix + "_" + sheetPrefix + "-";
    }

    protected void handleDelimiterDataFailureException(Table table, File delimiterFile, Exception cause) {
        String msg = "Failed to output delimiter data: table=" + table.getTableDispName() + " file=" + delimiterFile;
        throw new IllegalStateException(msg, cause);
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    protected DfLittleAdjustmentProperties getLittleAdjustmentProperties() {
        return getProperties().getLittleAdjustmentProperties();
    }

    protected DfAdditionalTableProperties getAdditionalTableProperties() {
        return getProperties().getAdditionalTableProperties();
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String resolvePath(File file) {
        return Srl.replace(file.getPath(), "\\", "/");
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setContainsCommonColumn(boolean containsCommonColumn) {
        _containsCommonColumn = containsCommonColumn;
    }

    // -----------------------------------------------------
    //                                        Delimiter Data
    //                                        --------------
    public String getLargeDataDir() { // used for e.g. delete
        return _largeDataDir;
    }

    public void setLargeDataDir(String largeDataDir) {
        _largeDataDir = largeDataDir;
    }

    public void setDelimiterDataBasis(boolean delimiterDataBasis) {
        _delimiterDataBasis = delimiterDataBasis;
    }

    public void setDelimiterMinimallyQuoted(boolean delimiterDataMinimallyQuoted) {
        _delimiterDataMinimallyQuoted = delimiterDataMinimallyQuoted;
    }

    // -----------------------------------------------------
    //                                              Xls Data
    //                                              --------
    public void setXlsLimit(int xlsLimit) {
        _xlsLimit = xlsLimit;
    }

    public void setSuppressLargeDataHandling(boolean suppressLargeDataHandling) {
        _suppressLargeDataHandling = suppressLargeDataHandling;
    }

    public void setSuppressQuoteEmptyString(boolean suppressQuoteEmptyString) {
        _suppressQuoteEmptyString = suppressQuoteEmptyString;
    }

    public void setCellLengthLimit(int cellLengthLimit) {
        _cellLengthLimit = cellLengthLimit;
    }

    // -----------------------------------------------------
    //                                            Saved Data
    //                                            ----------
    public Map<String, Table> getTableNameMap() {
        return _tableNameMap;
    }
}
