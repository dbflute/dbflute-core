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

import java.util.List;

/**
 * The option of file-making.
 * <pre>
 * e.g. TSV, UTF-8, with header
 *  new FileMakingOption().delimitateByTab().encodeAsUTF8().headerInfo(columnNameList)
 * </pre>
 * @author jflute
 */
public class FileMakingOption {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The delimiter of data. (Required) */
    protected String _delimiter;

    /** The encoding for the file. (Required) */
    protected String _encoding;

    /** The line separator for the file. (NotRequired) */
    protected String _lineSeparator;

    /** Does it quote values minimally? (NotRequired) */
    protected boolean _quoteMinimally;

    /** Does it suppress value count check? (NotRequired) */
    protected boolean _suppressValueCountCheck;

    /** The header info of file-making. (NotRequired) */
    protected FileMakingHeaderInfo _headerInfo;

    // ===================================================================================
    //                                                                         Easy-to-Use
    //                                                                         ===========
    /**
     * Delimitate by Comma.
     * @return this. (NotNull)
     */
    public FileMakingOption delimitateByComma() {
        _delimiter = ",";
        return this;
    }

    /**
     * Delimitate by Tab.
     * @return this. (NotNull)
     */
    public FileMakingOption delimitateByTab() {
        _delimiter = "\t";
        return this;
    }

    /**
     * Encode file as UTF-8.
     * @return this. (NotNull)
     */
    public FileMakingOption encodeAsUTF8() {
        _encoding = "UTF-8";
        return this;
    }

    /**
     * Encode file as Windows-31J.
     * @return this. (NotNull)
     */
    public FileMakingOption encodeAsWindows31J() {
        _encoding = "Windows-31J";
        return this;
    }

    /**
     * Separate line by CR + LF.
     * @return this. (NotNull)
     */
    public FileMakingOption separateByCrLf() {
        _lineSeparator = "\r\n";
        return this;
    }

    /**
     * Separate line by LF.
     * @return this. (NotNull)
     */
    public FileMakingOption separateByLf() {
        _lineSeparator = "\n";
        return this;
    }

    /**
     * Quote values minimally (if it needs).
     * @return this. (NotNull)
     */
    public FileMakingOption quoteMinimally() {
        _quoteMinimally = true;
        return this;
    }

    /**
     * Suppress the value count check. (compare with header's column count)
     * @return this. (NotNull)
     */
    public FileMakingOption suppressValueCountCheck() {
        _suppressValueCountCheck = true;
        return this;
    }

    /**
     * Set the header info with the list of column name.
     * @param columnNameList The list of column name. (NullAllowed: means no header)
     * @return this. (NotNull)
     */
    public FileMakingOption headerInfo(List<String> columnNameList) {
        if (columnNameList != null) {
            final FileMakingHeaderInfo headerInfo = new FileMakingHeaderInfo();
            headerInfo.acceptColumnNameList(columnNameList);
            _headerInfo = headerInfo;
        } else {
            _headerInfo = null;
        }
        return this;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return "{" + _delimiter + ", " + _encoding + ", " + _quoteMinimally + ", " + _headerInfo + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getDelimiter() {
        return _delimiter;
    }

    public void setDelimiter(String delimiter) {
        _delimiter = delimiter;
    }

    public String getEncoding() {
        return _encoding;
    }

    public void setEncoding(String encoding) {
        _encoding = encoding;
    }

    public String getLineSeparator() {
        return _lineSeparator;
    }

    public void setLineSeparator(String lineSeparator) {
        _lineSeparator = lineSeparator;
    }

    public boolean isQuoteMinimally() {
        return _quoteMinimally;
    }

    public boolean isSuppressValueCountCheck() {
        return _suppressValueCountCheck;
    }

    public FileMakingHeaderInfo getFileMakingHeaderInfo() {
        return _headerInfo;
    }

    public void setFileMakingHeaderInfo(FileMakingHeaderInfo headerInfo) {
        _headerInfo = headerInfo;
    }
}