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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.seasar.dbflute.exception.DfXlsReaderReadFailureException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.helper.StringKeyMap;
import org.seasar.dbflute.helper.dataset.DfDataColumn;
import org.seasar.dbflute.helper.dataset.DfDataRow;
import org.seasar.dbflute.helper.dataset.DfDataSet;
import org.seasar.dbflute.helper.dataset.DfDataSetConstants;
import org.seasar.dbflute.helper.dataset.DfDataTable;
import org.seasar.dbflute.helper.dataset.types.DfDtsColumnType;
import org.seasar.dbflute.helper.dataset.types.DfDtsColumnTypes;
import org.seasar.dbflute.resource.DBFluteSystem;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.DfTypeUtil;
import org.seasar.dbflute.util.Srl;
import org.seasar.dbflute.util.Srl.ScopeInfo;

/**
 * @author modified by jflute (originated in Seasar2)
 */
public class DfTableXlsReader {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log _log = LogFactory.getLog(DfTableXlsReader.class);
    public static final String LDATA_SHEET_NAME = "df$LARGE_DATA";
    public static final String LDATA_KEY_DELIMITER = "(df:delimiter)";
    public static final String LDATA_QUOTE_BEGIN = "{";
    public static final String LDATA_QUOTE_END = "}";
    public static final String LDATA_REF_PREFIX = "df:refLargeData(";
    public static final String LDATA_REF_SUFFIX = ")";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                          XLS Resource
    //                                          ------------
    protected final File _xlsFile;
    protected DfDataSet _dataSet;
    protected HSSFWorkbook _workbook;
    protected HSSFDataFormat _dataFormat;

    // -----------------------------------------------------
    //                                           Read Option
    //                                           -----------
    protected final Map<String, String> _tableNameMap;
    protected final Map<String, List<String>> _notTrimTableColumnMap;
    protected final Map<String, List<String>> _emptyStringTableColumnMap;
    protected final Pattern _skipSheetPattern; // not required
    protected boolean _rtrimCellValue;

    // -----------------------------------------------------
    //                                            Large Data
    //                                            ----------
    /** The map for large data table. map:{ table.column = map:{ dataKey = joined-large-string } } (NullAllowed) */
    protected Map<String, Map<String, String>> _largeDataMap;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfTableXlsReader(File xlsFile // XLS file to read
            , Map<String, String> tableNameMap // map for long table name
            , Map<String, List<String>> notTrimTableColumnMap // map for not-trim column
            , Map<String, List<String>> emptyStringTableColumnMap // map for empty-string-allowed column
            , Pattern skipSheetPattern // pattern of skipped sheet
            , boolean rtrimCellValue) { // Does it right-trim cell value?
        // /- - - - - - - - - - - - - - - - - - - - - - - - - - - -
        // actually read in constructor so accept all options here
        // - - - - - - - - - -/
        _xlsFile = xlsFile;
        if (tableNameMap != null) {
            _tableNameMap = tableNameMap;
        } else {
            _tableNameMap = StringKeyMap.createAsFlexible();
        }
        if (notTrimTableColumnMap != null) {
            _notTrimTableColumnMap = notTrimTableColumnMap;
        } else {
            _notTrimTableColumnMap = StringKeyMap.createAsFlexible();
        }
        if (emptyStringTableColumnMap != null) {
            _emptyStringTableColumnMap = emptyStringTableColumnMap;
        } else {
            _emptyStringTableColumnMap = StringKeyMap.createAsFlexible();
        }
        _skipSheetPattern = skipSheetPattern;
        _rtrimCellValue = rtrimCellValue;

        // actually read
        setupWorkbook(toStream(xlsFile));
    }

    protected InputStream toStream(File file) {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    // -----------------------------------------------------
    //                                       Set up Workbook
    //                                       ---------------
    protected void setupWorkbook(InputStream ins) {
        try {
            _workbook = new HSSFWorkbook(ins);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create workbook: " + _xlsFile, e);
        }
        _dataFormat = _workbook.createDataFormat();
        _dataSet = new DfDataSet();
        prepareLargeDataTable();
        for (int i = 0; i < _workbook.getNumberOfSheets(); ++i) {
            final String sheetName = _workbook.getSheetName(i);
            if (isCommentOutSheet(sheetName)) { // since 0.7.9
                _log.info("*The sheet has comment-out mark so skip it: " + sheetName);
                continue;
            }
            if (isSkipSheet(sheetName)) { // since 0.7.9 for [DBFLUTE-251]
                _log.info("*The sheet name matched skip-sheet specification so skip it: " + sheetName);
                continue;
            }
            if (isLargeDataSheet(sheetName)) { // already analyzed here
                continue;
            }
            prepareTable(sheetName, _workbook.getSheetAt(i));
        }
    }

    // -----------------------------------------------------
    //                                            Large Data
    //                                            ----------
    protected void prepareLargeDataTable() {
        for (int i = 0; i < _workbook.getNumberOfSheets(); ++i) {
            final String sheetName = _workbook.getSheetName(i);
            if (!isLargeDataSheet(sheetName)) {
                continue;
            }
            final HSSFSheet sheet = _workbook.getSheetAt(i);
            final String largeTableName = "LARGE_DATA"; // unused
            final DfDataTable table = setupTable(sheet, largeTableName, new DfDataTable(largeTableName));
            _largeDataMap = DfCollectionUtil.newLinkedHashMap();
            final Map<Integer, String> indexColumnTitleMap = DfCollectionUtil.newLinkedHashMap();
            for (int columnIndex = 0; columnIndex < table.getColumnSize(); columnIndex++) {
                final DfDataColumn column = table.getColumn(columnIndex);
                final String columnTitle = column.getColumnDbName();
                if (!columnTitle.contains(".")) { // should be e.g. MEMBER.MEMBER_NAME
                    throwLargeDataInvalidColumnTitleException(sheetName, columnTitle);
                }
                Map<String, String> dataMap = _largeDataMap.get(columnTitle);
                if (dataMap == null) {
                    dataMap = DfCollectionUtil.newLinkedHashMap();
                }
                _largeDataMap.put(columnTitle, dataMap);
                indexColumnTitleMap.put(columnIndex, columnTitle);
            }
            for (int rowIndex = 0; rowIndex < table.getRowSize(); rowIndex++) {
                final DfDataRow row = table.getRow(rowIndex);
                for (int columnIndex = 0; columnIndex < table.getColumnSize(); ++columnIndex) {
                    final Object obj = row.getValue(columnIndex);
                    if (obj == null) {
                        continue;
                    }
                    final String value = obj.toString(); // basically String, but just in case
                    final String columnTitle = indexColumnTitleMap.get(columnIndex);
                    final Map<String, String> dataMap = _largeDataMap.get(columnTitle);
                    if (!value.contains(LDATA_KEY_DELIMITER)) { // should be e.g. key(df:delimiter){value}
                        throwLargeDataInvalidManagedDataException(sheetName, columnTitle, row, value);
                    }
                    final String dataKey = Srl.substringFirstFront(value, LDATA_KEY_DELIMITER);
                    final String largeValue = Srl.substringFirstRear(value, LDATA_KEY_DELIMITER);
                    final String unquotedValue = Srl.unquoteAnything(largeValue, LDATA_QUOTE_BEGIN, LDATA_QUOTE_END);
                    final String existingValue = dataMap.get(dataKey);
                    final String realValue = existingValue != null ? existingValue + unquotedValue : unquotedValue;
                    dataMap.put(dataKey, realValue);
                }
            }
            break; // only one
        }
    }

    protected boolean isLargeDataSheet(String sheetName) {
        return sheetName.equals(LDATA_SHEET_NAME);
    }

    protected void throwLargeDataInvalidColumnTitleException(String sheetName, String columnTitle) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Invalid column title for large data.");
        br.addItem("Advice");
        br.addElement("It should be [table].[column] e.g. MEMBER.MEMBER_NAME");
        br.addItem("Xls File");
        br.addElement(_xlsFile);
        br.addItem("Sheet Name");
        br.addElement(sheetName);
        br.addItem("Column Title");
        br.addElement(columnTitle);
        final String msg = br.buildExceptionMessage();
        throw new DfXlsReaderReadFailureException(msg);
    }

    protected void throwLargeDataInvalidManagedDataException(String sheetName, String columnTitle, DfDataRow row,
            String value) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Invalid managed large data.");
        br.addItem("Advice");
        br.addElement("It should be key" + LDATA_KEY_DELIMITER + "{value}");
        br.addElement(" e.g. foo" + LDATA_KEY_DELIMITER + "{bar}");
        br.addItem("Xls File");
        br.addElement(_xlsFile);
        br.addItem("Sheet Name");
        br.addElement(sheetName);
        br.addItem("Column Title");
        br.addElement(columnTitle);
        br.addItem("Row Number");
        br.addElement(row.getRowNumber());
        br.addItem("Large Data");
        br.addElement(value);
        final String msg = br.buildExceptionMessage();
        throw new DfXlsReaderReadFailureException(msg);
    }

    // -----------------------------------------------------
    //                                            Data Table
    //                                            ----------
    protected DfDataTable prepareTable(String sheetName, HSSFSheet sheet) {
        String tableName = sheetName;
        if (_tableNameMap != null && !_tableNameMap.isEmpty() && sheetName.startsWith("$")) {
            String realTableName = _tableNameMap.get(sheetName);
            if (realTableName == null) {
                realTableName = _tableNameMap.get(sheetName.substring("$".length()));
                if (realTableName == null) {
                    throwXlsReaderMappingTableNotFoundException(sheetName);
                }
            }
            tableName = realTableName;
        }
        final DfDataTable table = _dataSet.addTable(tableName);
        return setupTable(sheet, tableName, table);
    }

    protected DfDataTable setupTable(HSSFSheet sheet, String tableName, final DfDataTable table) {
        final int rowCount = sheet.getLastRowNum();
        final HSSFRow nameRow = sheet.getRow(0);
        if (nameRow == null) {
            throwXlsReaderFirstRowNotColumnDefinitionException(tableName);
        }
        if (rowCount > 0) {
            setupColumns(table, nameRow, sheet.getRow(1));
            setupRows(table, sheet);
        } else if (rowCount == 0) {
            setupColumns(table, nameRow, null);
        }
        return table;
    }

    protected void throwXlsReaderMappingTableNotFoundException(String sheetName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The sheetName was not found in the tableNameMap.");
        br.addItem("Xls File");
        br.addElement(_xlsFile);
        br.addItem("Sheet Name");
        br.addElement(sheetName);
        br.addItem("TableName Map");
        if (!_tableNameMap.isEmpty()) {
            for (Entry<String, String> entry : _tableNameMap.entrySet()) {
                br.addElement(entry.getKey() + " = " + entry.getValue());
            }
        } else {
            br.addElement("*empty");
        }
        final String msg = br.buildExceptionMessage();
        throw new DfXlsReaderReadFailureException(msg);
    }

    protected void throwXlsReaderFirstRowNotColumnDefinitionException(String tableName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The first row of the sheet was not column definition.");
        br.addItem("Xls File");
        br.addElement(_xlsFile);
        br.addItem("Table");
        br.addElement(tableName);
        final String msg = br.buildExceptionMessage();
        throw new DfXlsReaderReadFailureException(msg);
    }

    // -----------------------------------------------------
    //                                           Data Column
    //                                           -----------
    protected void setupColumns(DfDataTable table, HSSFRow nameRow, HSSFRow valueRow) {
        for (int i = 0;; ++i) {
            final HSSFCell nameCell = nameRow.getCell(i);
            if (nameCell == null) {
                break;
            }
            final HSSFRichTextString richStringCellValue = nameCell.getRichStringCellValue();
            if (richStringCellValue == null) {
                break;
            }
            final String columnName = richStringCellValue.getString().trim();
            if (columnName.length() == 0) {
                break;
            }
            HSSFCell valueCell = null;
            if (valueRow != null) {
                valueCell = valueRow.getCell(i);
            }
            if (valueCell != null) {
                table.addColumn(columnName, getColumnType(valueCell));
            } else {
                table.addColumn(columnName);
            }
        }
    }

    // -----------------------------------------------------
    //                                              Data Row
    //                                              --------
    protected void setupRows(DfDataTable table, HSSFSheet sheet) {
        for (int i = 1;; ++i) {
            HSSFRow row = sheet.getRow(i);
            if (row == null) {
                break;
            }
            setupRow(table, row);
        }
    }

    protected void setupRow(DfDataTable table, HSSFRow row) {
        final DfDataRow dataRow = table.addRow();
        HSSFCell cell = null;
        Object value = null;
        DfDataColumn column = null;
        try {
            for (int columnIndex = 0; columnIndex < table.getColumnSize(); ++columnIndex) {
                cell = row.getCell(columnIndex);
                value = extractCellValue(table, columnIndex, row, cell);
                column = table.getColumn(columnIndex);
                final String columnName = column.getColumnDbName();
                try {
                    dataRow.addValue(columnName, value);
                } catch (NumberFormatException e) {
                    if (cell.getCellType() != HSSFCell.CELL_TYPE_STRING) {
                        throw e;
                    }
                    _log.info("...Changing the column type to STRING type: name=" + columnName + " value=" + value);
                    column.setColumnType(DfDtsColumnTypes.STRING);
                    dataRow.addValue(columnName, value);
                }
            }
        } catch (RuntimeException e) {
            throwCellValueHandlingException(table, column, row, cell, value, e);
        }
    }

    protected void throwCellValueHandlingException(DfDataTable table, DfDataColumn column, HSSFRow row, HSSFCell cell,
            Object value, RuntimeException cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to handle the cell value on the xls file.");
        br.addItem("Advice");
        br.addElement("Confirm the exception message.");
        br.addElement("The cell value may be wrong type for the column.");
        br.addElement("So confirm the value on the xls file.");
        br.addItem("RuntimeException");
        br.addElement(cause.getMessage());
        br.addItem("Xls File");
        br.addElement(_xlsFile);
        br.addItem("Table");
        br.addElement(table.getTableDbName());
        br.addItem("Column");
        br.addElement(column != null ? column.getColumnDbName() : null);
        br.addItem("Mapping Type");
        final DfDtsColumnType columnType = column.getColumnType();
        br.addElement(columnType != null ? columnType.getType() : null);
        br.addItem("Cell Type");
        if (cell != null) {
            switch (cell.getCellType()) {
            case HSSFCell.CELL_TYPE_NUMERIC:
                br.addElement("CELL_TYPE_NUMERIC");
                break;
            case HSSFCell.CELL_TYPE_STRING:
                br.addElement("CELL_TYPE_STRING");
                break;
            case HSSFCell.CELL_TYPE_FORMULA:
                br.addElement("CELL_TYPE_FORMULA");
                break;
            case HSSFCell.CELL_TYPE_BLANK:
                br.addElement("CELL_TYPE_BLANK");
                break;
            case HSSFCell.CELL_TYPE_BOOLEAN:
                br.addElement("CELL_TYPE_BOOLEAN");
                break;
            case HSSFCell.CELL_TYPE_ERROR:
                br.addElement("CELL_TYPE_ERROR");
                break;
            default:
                br.addElement(cell.getCellType());
                break;
            }
        }
        br.addItem("Cell Value");
        br.addElement(value);
        br.addItem("Row Number");
        br.addElement(column != null ? row.getRowNum() : null);
        final String msg = br.buildExceptionMessage();
        throw new DfXlsReaderReadFailureException(msg, cause);
    }

    // ===================================================================================
    //                                                                      Value Handling
    //                                                                      ==============
    protected Object extractCellValue(DfDataTable table, int columnIndex, HSSFRow row, HSSFCell cell) {
        if (cell == null) {
            return isEmptyStringTarget(table, columnIndex) ? "" : null;
        }
        switch (cell.getCellType()) {
        case HSSFCell.CELL_TYPE_NUMERIC:
            if (isCellDateFormatted(cell)) {
                return DfTypeUtil.toTimestamp(cell.getDateCellValue());
            }
            final double numericCellValue = cell.getNumericCellValue();
            if (isInt(numericCellValue)) {
                return new BigDecimal((int) numericCellValue);
            }
            return new BigDecimal(Double.toString(numericCellValue));
        case HSSFCell.CELL_TYPE_STRING:
            return processRichStringCellValue(table, columnIndex, row, cell);
        case HSSFCell.CELL_TYPE_BOOLEAN:
            boolean b = cell.getBooleanCellValue();
            return Boolean.valueOf(b);
        default:
            return isEmptyStringTarget(table, columnIndex) ? "" : null;
        }
    }

    protected Object processRichStringCellValue(DfDataTable table, int columnIndex, HSSFRow row, HSSFCell cell) {
        String str = cell.getRichStringCellValue().getString();
        str = rtrimCellValueIfNeeds(table, cell, str); // basically for compatible
        str = treatEmptyAsNullBasically(str); // empty means null basically
        str = treatNullAsEmptyIfTarget(table, columnIndex, str); // but empty if target
        str = treatCrLfAsLf(str); // remove CR
        if (isCellBase64Formatted(cell)) {
            return decodeAsBase64(str);
        }
        // normal cell here
        return resolveLargeDataIfNeeds(table, columnIndex, row, str);
    }

    protected String rtrimCellValueIfNeeds(DfDataTable table, HSSFCell cell, String str) {
        if (str != null && _rtrimCellValue && !isNotTrimTarget(table, cell)) {
            return Srl.rtrim(str);
        }
        return str;
    }

    protected String treatEmptyAsNullBasically(String str) {
        return "".equals(str) ? null : str;
    }

    protected String treatNullAsEmptyIfTarget(DfDataTable table, int columnIndex, String str) {
        return str == null && isEmptyStringTarget(table, columnIndex) ? "" : str;
    }

    protected String treatCrLfAsLf(String str) {
        // basically excel treats line separators as LF
        // so this process cannot be required but just in case
        return str != null ? Srl.replace(str, "\r\n", "\n") : null;
    }

    protected Object decodeAsBase64(String str) {
        return str != null ? DfTypeUtil.decodeAsBase64(str) : null;
    }

    protected String resolveLargeDataIfNeeds(DfDataTable table, int columnIndex, HSSFRow row, String str) {
        if (str == null) {
            return null;
        }
        final String refPrefix = LDATA_REF_PREFIX;
        final String refSuffix = LDATA_REF_SUFFIX;
        if (_largeDataMap != null && str.startsWith(refPrefix) && str.endsWith(refSuffix)) {
            final ScopeInfo scopeInfo = Srl.extractScopeFirst(str, refPrefix, refSuffix);
            final String dataKey = scopeInfo.getContent();
            final DfDataColumn column = table.getColumn(columnIndex);
            final String columnTitle = table.getTableDbName() + "." + column.getColumnDbName();
            final Map<String, String> dataMap = _largeDataMap.get(columnTitle);
            if (dataMap != null) {
                final String largeData = dataMap.get(dataKey);
                if (largeData != null) {
                    return largeData;
                } else {
                    throwLargeDataReferenceDataNotFoundException(table, columnIndex, row, str, dataKey);
                }
            } else {
                throwLargeDataReferenceDataNotFoundException(table, columnIndex, row, str, dataKey);
            }
        }
        return str;
    }

    protected void throwLargeDataReferenceDataNotFoundException(DfDataTable table, int columnIndex, HSSFRow row,
            String str, String dataKey) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the reference data of large data for the column.");
        br.addItem("Xls File");
        br.addElement(_xlsFile);
        br.addItem("Table Name");
        br.addElement(table.getTableDbName());
        br.addItem("Column");
        br.addElement(table.getColumnName(columnIndex));
        br.addItem("Row Number");
        br.addElement(row.getRowNum());
        br.addItem("Cell Value");
        br.addElement(str);
        br.addItem("Data Key");
        br.addElement(dataKey);
        final String msg = br.buildExceptionMessage();
        throw new DfXlsReaderReadFailureException(msg);
    }

    public boolean isNotTrimTarget(DfDataTable table, HSSFCell cell) {
        final String tableName = table.getTableDbName();
        if (!_notTrimTableColumnMap.containsKey(tableName)) {
            return false;
        }
        final List<String> notTrimTargetColumnList = _notTrimTableColumnMap.get(tableName);
        final DfDataColumn column = table.getColumn(cell.getColumnIndex());
        final String target = column.getColumnDbName();
        for (String specified : notTrimTargetColumnList) {
            if (target.equalsIgnoreCase(specified)) {
                return true;
            }
        }
        return false;
    }

    public boolean isEmptyStringTarget(DfDataTable table, int columnIndex) {
        final String tableName = table.getTableDbName();
        if (!_emptyStringTableColumnMap.containsKey(tableName)) {
            return false;
        }
        final List<String> emptyStringTargetColumnList = _emptyStringTableColumnMap.get(tableName);
        final DfDataColumn column = table.getColumn(columnIndex);
        final String target = column.getColumnDbName();
        for (String specified : emptyStringTargetColumnList) {
            if (target.equalsIgnoreCase(specified)) {
                return true;
            }
        }
        return false;
    }

    protected DfDtsColumnType getColumnType(HSSFCell cell) {
        switch (cell.getCellType()) {
        case HSSFCell.CELL_TYPE_NUMERIC:
            if (isCellDateFormatted(cell)) {
                return DfDtsColumnTypes.TIMESTAMP;
            }
            return DfDtsColumnTypes.BIGDECIMAL;
        case HSSFCell.CELL_TYPE_BOOLEAN:
            return DfDtsColumnTypes.BOOLEAN;
        case HSSFCell.CELL_TYPE_STRING:
            if (isCellBase64Formatted(cell)) {
                return DfDtsColumnTypes.BINARY;
            }
            return DfDtsColumnTypes.STRING;
        default:
            return DfDtsColumnTypes.STRING;
        }
    }

    // ===================================================================================
    //                                                                       Determination
    //                                                                       =============
    protected boolean isCellBase64Formatted(HSSFCell cell) {
        final HSSFCellStyle cs = cell.getCellStyle();
        final short dfNum = cs.getDataFormat();
        return DfDataSetConstants.BASE64_FORMAT.equals(_dataFormat.getFormat(dfNum));
    }

    protected boolean isCellDateFormatted(HSSFCell cell) {
        final HSSFCellStyle cs = cell.getCellStyle();
        final short dfNum = cs.getDataFormat();
        final String format = _dataFormat.getFormat(dfNum);
        if (format == null || format.length() == 0) {
            return false;
        }
        if (format.indexOf('/') > 0 || format.indexOf('y') > 0 || format.indexOf('m') > 0 || format.indexOf('d') > 0) {
            return true;
        }
        return false;
    }

    protected boolean isInt(final double numericCellValue) {
        return ((int) numericCellValue) == numericCellValue;
    }

    protected boolean isCommentOutSheet(String sheetName) {
        return sheetName.startsWith("#");
    }

    protected boolean isSkipSheet(String sheetName) {
        return _skipSheetPattern != null && _skipSheetPattern.matcher(sheetName).matches();
    }

    // ===================================================================================
    //                                                                                Read
    //                                                                                ====
    public DfDataSet read() { // already read, only returns result
        return _dataSet;
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String ln() {
        return DBFluteSystem.getBasicLn();
    }
}
