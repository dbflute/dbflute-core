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
package org.seasar.dbflute.helper.token.file;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.seasar.dbflute.helper.token.file.exception.FileTokenizingInvalidValueCountException;

/**
 * @author jflute
 */
public class FileTokenizingRowResource {

    // =====================================================================================
    //                                                                             Attribute
    //                                                                             =========
    protected FileTokenizingHeaderInfo _headerInfo;
    protected List<String> _valueList;
    protected String _rowString;
    protected int _rowNumber;
    protected int _lineNumber;

    // ===================================================================================
    //                                                                        Map Handling
    //                                                                        ============
    /**
     * Convert the value list to column value map.
     * @return The map of column-key value. (NullAllowed: when no header or no value list)
     */
    public Map<String, String> toColumnValueMap() {
        if (_headerInfo == null || _headerInfo.isEmpty()) {
            return null;
        }
        if (_valueList == null || _valueList.isEmpty()) {
            return null;
        }
        final List<String> columnNameList = _headerInfo.getColumnNameList();
        if (columnNameList.size() != _valueList.size()) {
            String msg = "Different count between header columns and values:";
            msg = msg + " " + columnNameList.size() + ", " + _valueList.size();
            throw new FileTokenizingInvalidValueCountException(msg);
        }
        final Map<String, String> map = new LinkedHashMap<String, String>(columnNameList.size());
        for (int i = 0; i < columnNameList.size(); i++) {
            final String columnName = columnNameList.get(i);
            final String value = _valueList.get(i);
            map.put(columnName, value);
        }
        return map;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return "{" + _lineNumber + ", row=" + _rowNumber + ": " + _rowString + "}";
    }

    // =====================================================================================
    //                                                                              Accessor
    //                                                                              ========
    /**
     * Get the header info of the token file.
     * @return The header info of the token file. (NotNull in callback)
     */
    public FileTokenizingHeaderInfo getHeaderInfo() {
        return _headerInfo;
    }

    public void setHeaderInfo(FileTokenizingHeaderInfo headerInfo) {
        _headerInfo = headerInfo;
    }

    /**
     * Get the list of value. <br />
     * The list instance is recycled for next line,
     * so you cannot save it (or convert it to your object).
     * @return The list of value. (NotNull, NotEmpty in callback)
     */
    public List<String> getValueList() {
        return _valueList;
    }

    public void setValueList(List<String> valueList) {
        _valueList = valueList;
    }

    /**
     * Get the row string with delimiters. e.g. foo,bar,qux
     * @return The string of row. (NotNull in callback)
     */
    public String getRowString() {
        return _rowString;
    }

    public void setRowString(String rowString) {
        _rowString = rowString;
    }

    /**
     * Get the row number. e.g. first data is always 1
     * @return The integer as row number. (NotZero, NotMinus in callback)
     */
    public int getRowNumber() {
        return _rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        _rowNumber = rowNumber;
    }

    /**
     * Get the line number. e.g. first data is 1 if no header, 2 if header exists
     * @return The integer as line number. (NotZero, NotMinus in callback)
     */
    public int getLineNumber() {
        return _lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        _lineNumber = lineNumber;
    }
}
