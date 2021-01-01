/*
 * Copyright 2014-2021 the original author or authors.
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
package org.dbflute.helper.io.xls;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.dbflute.helper.StringKeyMap;
import org.dbflute.helper.dataset.DfDataColumn;
import org.dbflute.helper.dataset.DfDataRow;
import org.dbflute.helper.dataset.DfDataSet;
import org.dbflute.helper.dataset.DfDataSetConstants;
import org.dbflute.helper.dataset.DfDataTable;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.DfTypeUtil;
import org.dbflute.util.Srl;

/**
 * @author modified by jflute (originated in Seasar2)
 * @author p1us2er0 (pull request)
 * @since 0.8.3 (2008/10/28 Tuesday)
 */
public class DfTableXlsWriter implements DfDataSetConstants {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                          XLS Resource
    //                                          ------------
    protected final OutputStream _out;
    protected final Workbook _workbook;
    protected final CellStyle _dateStyle;
    protected final CellStyle _base64Style;

    // -----------------------------------------------------
    //                                           Read Option
    //                                           -----------
    protected boolean _stringCellType;
    protected boolean _largeDataHandling;
    protected boolean _quoteEmptyString;
    protected int _cellLengthLimit = 30000; // as default, actually 32767 allowed by excel;

    // -----------------------------------------------------
    //                                            Large Data
    //                                            ----------
    /** The map for large data table. map:{ table.column = map:{ dataKey = list:{split-large-data ; ...} } } (NullAllowed) */
    protected Map<String, Map<String, List<String>>> _largeDataMap;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfTableXlsWriter(File file) {
        _out = create(file);
        _workbook = DfXlsFactory.instance().createWorkbook(file);
        final DataFormat dataFormat = _workbook.createDataFormat();
        _dateStyle = _workbook.createCellStyle();
        _dateStyle.setDataFormat(dataFormat.getFormat(DATE_FORMAT));
        _base64Style = _workbook.createCellStyle();
        _base64Style.setDataFormat(dataFormat.getFormat(BASE64_FORMAT));
    }

    protected static OutputStream create(File file) {
        try {
            return new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    // ===================================================================================
    //                                                                              Option
    //                                                                              ======
    /**
     * Enable string types of all cells.
     * @return this.
     */
    public DfTableXlsWriter stringCellType() {
        _stringCellType = true;
        return this;
    }

    /**
     * Enable large data handling. (using large data table)
     * @return this.
     */
    public DfTableXlsWriter largeDataHandling() {
        _largeDataHandling = true;
        return this;
    }

    /**
     * Quote empty string by double quotation. (to keep empty string basically for ReplaceSchema)
     * @return this.
     */
    public DfTableXlsWriter quoteEmptyString() {
        _quoteEmptyString = true;
        return this;
    }

    /**
     * Change cell length limit. (override default value)
     * @param limit The limit size of cell length.
     * @return this.
     */
    public DfTableXlsWriter cellLengthLimit(int limit) {
        _cellLengthLimit = limit;
        return this;
    }

    // ===================================================================================
    //                                                                               Write
    //                                                                               =====
    public void write(DfDataSet dataSet) {
        setupTableSheet(dataSet);
        setupLargeDataSheet();
        doWriteWorkbook();
    }

    protected void doWriteWorkbook() {
        try {
            _workbook.write(_out);
            _out.flush();
            _out.close();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write the workbook.", e);
        }
    }

    // ===================================================================================
    //                                                                         Table Sheet
    //                                                                         ===========
    protected void setupTableSheet(DfDataSet dataSet) {
        for (int tableIndex = 0; tableIndex < dataSet.getTableSize(); ++tableIndex) {
            final DfDataTable table = dataSet.getTable(tableIndex);
            final Sheet sheet = _workbook.createSheet();
            final String tableName = table.getTableDbName();
            try {
                _workbook.setSheetName(tableIndex, tableName);
            } catch (RuntimeException e) {
                String msg = "Failed to set the sheet name: " + tableName;
                throw new IllegalStateException(msg, e);
            }
            final Row headerRow = sheet.createRow(0);
            for (int columnIndex = 0; columnIndex < table.getColumnSize(); ++columnIndex) {
                final Cell cell = headerRow.createCell(columnIndex);
                cell.setCellValue(createRichTextString(_workbook, table.getColumnName(columnIndex)));
            }
            for (int rowIndex = 0; rowIndex < table.getRowSize(); ++rowIndex) {
                final Row row = sheet.createRow(rowIndex + 1);
                for (int columnIndex = 0; columnIndex < table.getColumnSize(); ++columnIndex) {
                    final DfDataRow dataRow = table.getRow(rowIndex);
                    final Object value = dataRow.getValue(columnIndex);
                    if (value != null) {
                        final Cell cell = row.createCell(columnIndex);
                        setupCellValueOfTableSheet(table, columnIndex, row, cell, value);
                    }
                }
            }
        }
    }

    // ===================================================================================
    //                                                                    Large Data Sheet
    //                                                                    ================
    protected void setupLargeDataSheet() {
        if (_largeDataHandling && _largeDataMap != null) {
            final int nextSheetIndex = _workbook.getNumberOfSheets();
            final Sheet sheet = _workbook.createSheet();
            final String sheetName = DfTableXlsReader.LDATA_SHEET_NAME;
            try {
                _workbook.setSheetName(nextSheetIndex, sheetName);
            } catch (RuntimeException e) {
                String msg = "Failed to set the sheet name for large data: " + sheetName;
                throw new IllegalStateException(msg, e);
            }
            doSetupLargeDataHeader(sheet);
            doSetupLargeDataRowCell(sheet);
        }
    }

    protected void doSetupLargeDataHeader(Sheet sheet) {
        final Row headerRow = sheet.createRow(0);
        int columnIndex = 0;
        for (Entry<String, Map<String, List<String>>> entry : _largeDataMap.entrySet()) {
            final String columnTitle = entry.getKey();
            final Cell cell = headerRow.createCell(columnIndex);
            cell.setCellValue(createRichTextString(_workbook, columnTitle));
            ++columnIndex;
        }
    }

    protected void doSetupLargeDataRowCell(Sheet sheet) {
        final Map<Integer, Row> rowMap = allocateLargeDataRow(sheet);
        int columnIndex = 0;
        for (Entry<String, Map<String, List<String>>> entry : _largeDataMap.entrySet()) {
            final Map<String, List<String>> dataMap = entry.getValue();
            int rowIndex = 0;
            for (Entry<String, List<String>> dataEntry : dataMap.entrySet()) {
                final String dataKey = dataEntry.getKey();
                final List<String> splitDataList = dataEntry.getValue();
                for (String splitData : splitDataList) {
                    final Row row = rowMap.get(rowIndex);
                    final Cell cell = row.createCell(columnIndex);
                    final String managedValue = convertToLargeDataManagedValue(dataKey, splitData);
                    setupCellValueOfLargeDataSheet(row, cell, managedValue);
                    ++rowIndex;
                }
            }
            ++columnIndex;
        }
    }

    protected Map<Integer, Row> allocateLargeDataRow(Sheet sheet) {
        int maxRowSize = 0;
        for (Entry<String, Map<String, List<String>>> entry : _largeDataMap.entrySet()) {
            final Map<String, List<String>> dataMap = entry.getValue();
            int dataRowSize = 0;
            for (Entry<String, List<String>> dataEntry : dataMap.entrySet()) {
                final List<String> splitDataList = dataEntry.getValue();
                dataRowSize = dataRowSize + splitDataList.size();
            }
            if (maxRowSize < dataRowSize) {
                maxRowSize = dataRowSize;
            }
        }
        final Map<Integer, Row> rowMap = DfCollectionUtil.newLinkedHashMap();
        for (int rowIndex = 0; rowIndex < maxRowSize; rowIndex++) {
            final Row row = sheet.createRow(rowIndex + 1);
            rowMap.put(rowIndex, row);
        }
        return rowMap;
    }

    protected String convertToLargeDataManagedValue(final String dataKey, String splitData) {
        return dataKey + DfTableXlsReader.LDATA_KEY_DELIMITER + "{" + splitData + "}";
    }

    // ===================================================================================
    //                                                                 Cell Value Handling
    //                                                                 ===================
    protected void setupCellValueOfTableSheet(DfDataTable table, int columnIndex, Row row, Cell cell, Object value) {
        doSetupCellValue(table, columnIndex, row, cell, value);
    }

    protected void setupCellValueOfLargeDataSheet(Row row, Cell cell, Object value) {
        doSetupCellValue(null, -1, row, cell, value);
    }

    protected void doSetupCellValue(DfDataTable table, int columnIndex, Row row, Cell cell, Object value) {
        // value is not null here
        if (_stringCellType) {
            cell.setCellType(Cell.CELL_TYPE_STRING);
        }
        if (value instanceof Number) {
            cell.setCellValue(createRichTextString(_workbook, value.toString()));
        } else if (value instanceof Date) {
            cell.setCellValue((Date) value);
            cell.setCellStyle(_dateStyle);
        } else if (value instanceof byte[]) {
            cell.setCellValue(createRichTextString(_workbook, DfTypeUtil.encodeAsBase64((byte[]) value)));
            cell.setCellStyle(_base64Style);
        } else if (value instanceof Boolean) {
            cell.setCellValue(((Boolean) value).booleanValue());
        } else { // e.g. String
            final String adjustedStr = adjustStringCellValue(table, columnIndex, row, cell, value);
            cell.setCellValue(createRichTextString(_workbook, adjustedStr));
        }
    }

    protected String adjustStringCellValue(DfDataTable table, int columnIndex, Row row, Cell cell, Object value) {
        final boolean tableSheet = table != null;
        final String plainStr = DfTypeUtil.toString(value);
        final String strValue;
        if (tableSheet) {
            strValue = resolveLargeDataReferenceIfNeeds(table, columnIndex, row, plainStr);
        } else {
            strValue = plainStr;
        }
        return resolveEmptyStringIfNeeds(strValue);
    }

    protected String resolveLargeDataReferenceIfNeeds(DfDataTable table, int columnIndex, Row row, String strValue) {
        final String mark = "...";
        final int splitLength = getCellLengthLimit() - mark.length();
        if (strValue != null && strValue.length() > splitLength) {
            final String filteredValue;
            if (_largeDataHandling) {
                if (_largeDataMap == null) {
                    _largeDataMap = StringKeyMap.createAsFlexibleOrdered();
                }
                final String tableDbName = table.getTableDbName();
                final DfDataColumn column = table.getColumn(columnIndex);
                final String columnDbName = column.getColumnDbName();
                final String columnTitle = tableDbName + "." + columnDbName;
                Map<String, List<String>> dataMap = _largeDataMap.get(columnTitle);
                if (dataMap == null) {
                    dataMap = DfCollectionUtil.newLinkedHashMap();
                    _largeDataMap.put(columnTitle, dataMap);
                }
                final String plainKey = columnTitle + ":" + row.getRowNum();
                final String dataKey = Integer.toHexString(plainKey.hashCode());
                dataMap.put(dataKey, toLargeDataSplitList(strValue));
                filteredValue = toLargeDataReferenceExp(dataKey);
            } else {
                filteredValue = strValue.substring(0, splitLength) + mark;
            }
            return filteredValue;
        }
        return strValue;
    }

    protected List<String> toLargeDataSplitList(String strValue) {
        final List<String> splitList = DfCollectionUtil.newArrayList();
        final int limit = getCellLengthLimit();
        String workingStr = strValue;
        while (workingStr.length() > limit) {
            final String element = Srl.substring(workingStr, 0, limit);
            splitList.add(element);
            workingStr = Srl.substring(workingStr, limit);
        }
        splitList.add(workingStr);
        return splitList;
    }

    protected String toLargeDataReferenceExp(final String dataKey) {
        return DfTableXlsReader.LDATA_REF_PREFIX + dataKey + DfTableXlsReader.LDATA_REF_SUFFIX;
    }

    protected int getCellLengthLimit() {
        return _cellLengthLimit;
    }

    protected String resolveEmptyStringIfNeeds(String strValue) {
        // empty string from database can be treated as empty string by quoting
        return _quoteEmptyString && strValue.equals("") ? "\"\"" : strValue;
    }

    protected RichTextString createRichTextString(Workbook workbook, String str) {
        return DfXlsFactory.instance().createRichTextString(workbook, str);
    }
}
