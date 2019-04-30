/*
 * Copyright 2014-2019 the original author or authors.
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
package org.dbflute.helper.token.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.helper.token.file.exception.FileMakingInvalidValueCountException;
import org.dbflute.helper.token.file.exception.FileMakingRequiredOptionNotFoundException;
import org.dbflute.helper.token.file.exception.FileMakingSQLHandlingFailureException;
import org.dbflute.helper.token.file.exception.FileTokenizingSQLHandlingFailureException;
import org.dbflute.helper.token.line.LineMakingOption;
import org.dbflute.helper.token.line.LineToken;
import org.dbflute.util.Srl;

/**
 * The handler of token file. <br>
 * You can read/write the token file.
 * <pre>
 * e.g. Tokenize (read)
 *  File tsvFile = ... <span style="color: #3F7E5E">// input file</span>
 *  FileToken fileToken = new FileToken();
 *  fileToken.tokenize(new FileInputStream(tsvFile), <span style="color: #553000">resource</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
 *      ... = resource.getHeaderInfo();
 *      ... = resource.<span style="color: #AD4747">getValueList()</span>;
 *      ... = resource.<span style="color: #AD4747">toColumnValueMap()</span>;
 *  }, <span style="color: #553000">op</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> op.delimitateByTab().encodeAsUTF8());
 * 
 * e.g. Make (write)
 * File tsvFile = ... <span style="color: #3F7E5E">// output file</span>
 * List&lt;String&gt; columnNameList = ... <span style="color: #3F7E5E">// columns for header</span>
 * FileToken fileToken = new FileToken();
 * fileToken.make(new FileOutputStream(tsvFile), <span style="color: #553000">writer</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
 *     for (Member member : ...) { <span style="color: #3F7E5E">// output data loop</span>
 *         List&lt;String&gt; valueList = ...; <span style="color: #3F7E5E">// convert the member to the row resource</span>
 *         writer.<span style="color: #AD4747">writeRow</span>(valueList); <span style="color: #3F7E5E">// Yes, you write!</span>
 *     }
 * }, <span style="color: #553000">op</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> op.delimitateByTab().encodeAsUTF8().headerInfo(columnNameList));
 * </pre>
 * @author jflute
 * @author iwamat-saw
 */
public class FileToken {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The mark that means header done for writing process. */
    protected static final String HEADER_DONE_MARK = "headerDone";

    /** The mark that means first line done for writing process. */
    protected static final String FIRST_LINE_DONE_MARK = "firstLineDone";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The handler of line token for help. */
    protected final LineToken _lineToken = new LineToken();

    // ===================================================================================
    //                                                                     Tokenize (read)
    //                                                                     ===============
    /**
     * Tokenize (read) the token data from the specified file. (file-tokenizing) <br>
     * CR + LF is treated as LF.
     * <pre>
     * File tsvFile = ... <span style="color: #3F7E5E">// input file</span>
     * FileToken fileToken = new FileToken();
     * fileToken.tokenize(new FileInputStream(tsvFile), <span style="color: #553000">resource</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     ... = resource.getHeaderInfo();
     *     ... = resource.<span style="color: #AD4747">getValueList()</span>;
     *     ... = resource.<span style="color: #AD4747">toColumnValueMap()</span>;
     * }, <span style="color: #553000">op</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> op.delimitateByTab().encodeAsUTF8().handleEmptyAsNull());
     * </pre>
     * @param filePath The path of file name to read. (NotNull)
     * @param oneArgLambda The callback for file-tokenizing. (NotNull)
     * @param opLambda The callback for option for file-tokenizing. (NotNull, Required{delimiter, encoding})
     * @throws FileNotFoundException When the file was not found.
     * @throws IOException When the file reading failed.
     * @throws FileTokenizingSQLHandlingFailureException When the SQL handling fails in the row handling process.
     */
    public void tokenize(String filePath, FileTokenizingCallback oneArgLambda, FileTokenOptionCall<FileTokenizingOption> opLambda)
            throws FileNotFoundException, IOException {
        assertStringNotNullAndNotTrimmedEmpty("filePath", filePath);
        assertObjectNotNull("oneArgLambda(FileTokenizingCallback)", oneArgLambda);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(filePath);
            doTokenize(fis, oneArgLambda, opLambda);
        } finally {
            if (fis != null) {
                try {
                    fis.close(); // basically no needed but just in case
                } catch (IOException ignored) {}
            }
        }
    }

    protected FileTokenizingOption createFileTokenizingOption(FileTokenOptionCall<FileTokenizingOption> opLambda) {
        assertObjectNotNull("opLambda", opLambda);
        final FileTokenizingOption op = newFileTokenizingOption();
        opLambda.callback(op);
        return op;
    }

    protected FileTokenizingOption newFileTokenizingOption() {
        return new FileTokenizingOption();
    }

    /**
     * Tokenize (read) token data to specified file. (named file-tokenizing) <br>
     * CR + LF is treated as LF. <br>
     * This method uses {@link InputStreamReader} and {@link BufferedReader} that wrap the stream. <br>
     * And these objects are closed. (close() called finally)
     * <pre>
     * File tsvFile = ... <span style="color: #3F7E5E">// input file</span>
     * FileToken fileToken = new FileToken();
     * fileToken.tokenize(new FileInputStream(tsvFile), <span style="color: #553000">resource</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     ... = resource.getHeaderInfo();
     *     ... = resource.<span style="color: #AD4747">getValueList()</span>;
     * }, <span style="color: #553000">op</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> op.delimitateByTab().encodeAsUTF8().handleEmptyAsNull());
     * </pre>
     * @param ins The input stream for writing. This stream is closed after writing automatically. (NotNull)
     * @param oneArgLambda The callback for file-tokenizing. (NotNull)
     * @param opLambda The callback for option for file-tokenizing. (NotNull, Required{delimiter, encoding})
     * @throws FileNotFoundException When the file was not found.
     * @throws IOException When the file reading failed.
     * @throws FileTokenizingSQLHandlingFailureException When the SQL handling fails in the row handling process. 
     */
    public void tokenize(InputStream ins, FileTokenizingCallback oneArgLambda, FileTokenOptionCall<FileTokenizingOption> opLambda)
            throws FileNotFoundException, IOException {
        assertObjectNotNull("ins", ins);
        assertObjectNotNull("oneArgLambda(FileTokenizingCallback)", oneArgLambda);
        doTokenize(ins, oneArgLambda, opLambda);
    }

    protected void doTokenize(InputStream ins, FileTokenizingCallback handlingCall, FileTokenOptionCall<FileTokenizingOption> opLambda)
            throws UnsupportedEncodingException, IOException {
        final FileTokenizingOption option = createFileTokenizingOption(opLambda);
        final String delimiter = option.getDelimiter();
        final String encoding = option.getEncoding();
        assertObjectNotNull("delimiter", delimiter);
        assertStringNotNullAndNotTrimmedEmpty("encoding", encoding);

        String lineString = null;
        String preContinueString = "";
        final List<String> temporaryValueList = new ArrayList<String>();
        final List<String> filteredValueList = new ArrayList<String>();

        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(ins, encoding));

            final StringBuilder realRowStringSb = new StringBuilder();
            FileTokenizingHeaderInfo headerInfo = null;
            int count = -1;
            int rowNumber = 1;
            int lineNumber = 0;
            while (true) {
                ++count;
                if ("".equals(preContinueString)) {
                    lineNumber = count + 1;
                }

                lineString = br.readLine();
                if (lineString == null) {
                    break;
                }
                if (count == 0) {
                    if (option.isBeginFirstLine()) {
                        headerInfo = createFileTokenizingHeaderInfo(); // as empty
                    } else {
                        headerInfo = analyzeHeaderInfo(delimiter, lineString);
                        continue;
                    }
                }
                final String rowString;
                if (preContinueString.equals("")) {
                    rowString = lineString;
                    realRowStringSb.append(lineString);
                } else {
                    final String lineSeparator = getTokenizedRestoredLineSeparator();
                    rowString = preContinueString + lineSeparator + lineString;
                    realRowStringSb.append(lineSeparator).append(lineString);
                }
                final ValueLineInfo valueLineInfo = arrangeValueList(rowString, delimiter);
                final List<String> ls = valueLineInfo.getValueList();
                if (valueLineInfo.isContinueNextLine()) {
                    preContinueString = (String) ls.remove(ls.size() - 1);
                    temporaryValueList.addAll(ls);
                    continue;
                }
                temporaryValueList.addAll(ls);

                try {
                    final FileTokenizingRowResource resource = createFileTokenizingRowResource();
                    resource.setHeaderInfo(headerInfo);

                    if (option.isHandleEmptyAsNull()) {
                        for (final Iterator<String> ite = temporaryValueList.iterator(); ite.hasNext();) {
                            final String value = (String) ite.next();
                            if ("".equals(value)) {
                                filteredValueList.add(null);
                            } else {
                                filteredValueList.add(value);
                            }
                        }
                        resource.setValueList(filteredValueList);
                    } else {
                        resource.setValueList(temporaryValueList);
                    }

                    final String realRowString = realRowStringSb.toString();
                    realRowStringSb.setLength(0);
                    resource.setRowString(realRowString);
                    resource.setRowNumber(rowNumber);
                    resource.setLineNumber(lineNumber);
                    handlingCall.handleRow(resource);
                } finally {
                    ++rowNumber;
                    temporaryValueList.clear();
                    filteredValueList.clear();
                    preContinueString = "";
                }
            }
        } catch (SQLException e) {
            String msg = "SQL handling failed in the row handling process: option=" + option;
            throw new FileTokenizingSQLHandlingFailureException(msg, e);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ignored) {}
        }
    }

    protected FileTokenizingRowResource createFileTokenizingRowResource() {
        return new FileTokenizingRowResource();
    }

    protected String getTokenizedRestoredLineSeparator() {
        return "\n";
    }

    protected ValueLineInfo arrangeValueList(final String lineString, String delimiter) {
        final List<String> valueList = new ArrayList<String>();

        // Don't use split!
        //final String[] values = lineString.split(delimiter);
        final List<String> list = _lineToken.tokenize(lineString, op -> op.delimitateBy(delimiter));
        final String[] values = (String[]) list.toArray(new String[list.size()]);
        for (int i = 0; i < values.length; i++) {
            valueList.add(values[i]);
        }
        return arrangeValueList(valueList, delimiter);
    }

    protected ValueLineInfo arrangeValueList(List<String> valueList, String delimiter) {
        final ValueLineInfo valueLineInfo = createValueLineInfo();
        final List<String> resultList = new ArrayList<String>();
        String preString = "";
        for (int i = 0; i < valueList.size(); i++) {
            final String value = (String) valueList.get(i);
            if (value == null) {
                continue;
            }
            if (i == valueList.size() - 1) { // The last loop
                if (preString.equals("")) {
                    if (isFrontQOnly(value)) {
                        valueLineInfo.setContinueNextLine(true);
                        resultList.add(value);
                    } else if (isRearQOnly(value)) {
                        resultList.add(value);
                    } else if (isNotBothQ(value)) {
                        resultList.add(value);
                    } else {
                        resultList.add(removeDoubleQuotation(value));
                    }
                } else {
                    if (endsQuote(value, false)) {
                        resultList.add(removeDoubleQuotation(connectPreString(preString, delimiter, value)));
                    } else {
                        valueLineInfo.setContinueNextLine(true);
                        resultList.add(connectPreString(preString, delimiter, value));
                    }
                }
                break; // because it's the last loop
            }

            if (preString.equals("")) {
                if (isFrontQOnly(value)) {
                    preString = value;
                    continue;
                } else if (isRearQOnly(value)) {
                    preString = value;
                    continue;
                } else if (isNotBothQ(value)) {
                    resultList.add(value);
                } else {
                    resultList.add(removeDoubleQuotation(value));
                }
            } else {
                if (endsQuote(value, false)) {
                    resultList.add(removeDoubleQuotation(connectPreString(preString, delimiter, value)));
                } else {
                    preString = connectPreString(preString, delimiter, value);
                    continue;
                }
            }
            preString = "";
        }
        valueLineInfo.setValueList(resultList);
        return valueLineInfo;
    }

    protected ValueLineInfo createValueLineInfo() {
        return new ValueLineInfo();
    }

    protected String connectPreString(String preString, String delimiter, String value) {
        if (preString.equals("")) {
            return value;
        } else {
            return preString + delimiter + value;
        }
    }

    protected boolean isNotBothQ(final String value) {
        return !isQQ(value) && !value.startsWith("\"") && !endsQuote(value, false);
    }

    protected boolean isRearQOnly(final String value) {
        return !isQQ(value) && !value.startsWith("\"") && endsQuote(value, false);
    }

    protected boolean isFrontQOnly(final String value) {
        return !isQQ(value) && value.startsWith("\"") && !endsQuote(value, true);
    }

    protected boolean isQQ(final String value) {
        return value.equals("\"\"");
    }

    protected boolean endsQuote(String value, boolean startsQuote) {
        value = startsQuote ? value.substring(1) : value;
        final int length = value.length();
        int count = 0;
        for (int i = 0; i < length; i++) {
            char ch = value.charAt(length - (i + 1));
            if (ch == '\"') {
                ++count;
            } else {
                break;
            }
        }
        return count > 0 && isOddNumber(count);
    }

    protected boolean isOddNumber(int number) {
        return (number % 2) != 0;
    }

    protected String removeDoubleQuotation(String value) {
        if (!value.startsWith("\"") && !value.endsWith("\"")) {
            return value;
        }
        if (value.startsWith("\"")) {
            value = value.substring(1);
        }
        if (value.endsWith("\"")) {
            value = value.substring(0, value.length() - 1);
        }
        value = Srl.replace(value, "\"\"", "\"");
        return value;
    }

    protected String removeRightDoubleQuotation(String value) {
        if (value.endsWith("\"")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    protected FileTokenizingHeaderInfo analyzeHeaderInfo(String delimiter, String lineString) {
        // analyze column header like value list for delimiter in column name if quoted
        final ValueLineInfo lineInfo = arrangeValueList(lineString, delimiter);
        final FileTokenizingHeaderInfo headerInfo = new FileTokenizingHeaderInfo();
        headerInfo.acceptColumnNameList(lineInfo.getValueList().stream().map(vl -> {
            return vl.trim(); /* leading and trailing spaces is unneeded in header (non value) */
        }).collect(Collectors.toList()));
        headerInfo.setColumnNameRowString(lineString);
        return headerInfo;
    }

    protected FileTokenizingHeaderInfo createFileTokenizingHeaderInfo() {
        return new FileTokenizingHeaderInfo();
    }

    public static class ValueLineInfo {

        protected List<String> _valueList;
        protected boolean _continueNextLine;

        public List<String> getValueList() {
            return _valueList;
        }

        public void setValueList(List<String> valueList) {
            _valueList = valueList;
        }

        public boolean isContinueNextLine() {
            return _continueNextLine;
        }

        public void setContinueNextLine(boolean continueNextLine) {
            _continueNextLine = continueNextLine;
        }
    }

    // ===================================================================================
    //                                                                        Make (write)
    //                                                                        ============
    /**
     * Make (write) token file by row writer that accepts row resources.
     * <pre>
     * String tsvFile = ... <span style="color: #3F7E5E">// output file</span>
     * List&lt;String&gt; columnNameList = ... <span style="color: #3F7E5E">// columns for header</span>
     * FileToken fileToken = new FileToken();
     * fileToken.make(tsvFile, <span style="color: #553000">writer</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     for (Member member : ...) { <span style="color: #3F7E5E">// output data loop</span>
     *         List&lt;String&gt; valueList = ...; <span style="color: #3F7E5E">// convert the member to the row resource</span>
     *         writer.<span style="color: #AD4747">writeRow</span>(valueList); <span style="color: #3F7E5E">// Yes, you write!</span>
     *     }
     * }, <span style="color: #553000">op</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> op.delimitateByTab().encodeAsUTF8().headerInfo(columnNameList));
     * </pre>
     * @param filePath The path of token file to write. (NotNull)
     * @param oneArgLambda The callback for file-making with writer. (NotNull)
     * @param opLambda The callback for option for file-making. (NotNull, Required: delimiter, encoding)
     * @throws FileNotFoundException When the file was not found.
     * @throws IOException When the file writing failed.
     * @throws FileMakingInvalidValueCountException When the value count of the row does not match column count of header.
     * @throws FileMakingSQLHandlingFailureException When the SQL handling fails in the row writing process.
     */
    public void make(String filePath, FileMakingCallback oneArgLambda, FileTokenOptionCall<FileMakingOption> opLambda)
            throws FileNotFoundException, IOException {
        assertStringNotNullAndNotTrimmedEmpty("filePath", filePath);
        assertObjectNotNull("oneArgLambda(FileMakingCallback)", oneArgLambda);
        FileOutputStream fos = null;
        try {
            mkdirsIfNeeds(filePath);
            fos = new FileOutputStream(filePath);
            doMake(fos, oneArgLambda, opLambda);
        } finally {
            if (fos != null) {
                try {
                    fos.close(); // basically no needed but just in case
                } catch (IOException ignored) {}
            }
        }
    }

    protected void mkdirsIfNeeds(String filePath) {
        final String filtered = Srl.replace(filePath, "\\", "/");
        if (!filePath.contains("/")) {
            return;
        }
        final String dirPath = Srl.substringLastFront(filtered, "/");
        final File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Make (write) token file by row writer that accepts row resources. <br>
     * This method uses {@link OutputStreamWriter} and {@link BufferedWriter} that wrap the stream. <br>
     * And these objects are closed. (close() called finally)
     * <pre>
     * File tsvFile = ... <span style="color: #3F7E5E">// output file</span>
     * List&lt;String&gt; columnNameList = ... <span style="color: #3F7E5E">// columns for header</span>
     * FileToken fileToken = new FileToken();
     * fileToken.make(new FileOutputStream(tsvFile), <span style="color: #553000">writer</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     for (Member member : ...) { <span style="color: #3F7E5E">// output data loop</span>
     *         List&lt;String&gt; valueList = ...; <span style="color: #3F7E5E">// convert the member to the row resource</span>
     *         writer.<span style="color: #AD4747">writeRow</span>(valueList); <span style="color: #3F7E5E">// Yes, you write!</span>
     *     }
     * }, <span style="color: #553000">op</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> op.delimitateByTab().encodeAsUTF8().headerInfo(columnNameList));
     * </pre>
     * @param ous The output stream for writing. This stream is closed after writing automatically. (NotNull)
     * @param oneArgLambda The callback for file-making with writer. (NotNull)
     * @param opLambda The callback for option for file-making. (NotNull, Required: delimiter, encoding)
     * @throws FileNotFoundException When the file was not found.
     * @throws IOException When the file writing failed.
     * @throws FileMakingInvalidValueCountException When the value count of the row does not match column count of header.
     * @throws FileMakingSQLHandlingFailureException When the SQL handling fails in the row writing process.
     */
    public void make(OutputStream ous, FileMakingCallback oneArgLambda, FileTokenOptionCall<FileMakingOption> opLambda)
            throws FileNotFoundException, IOException {
        assertObjectNotNull("oneArgLambda(FileMakingCallback)", oneArgLambda);
        doMake(ous, oneArgLambda, opLambda);
    }

    protected void doMake(OutputStream ous, FileMakingCallback handlingCall, FileTokenOptionCall<FileMakingOption> opLambda)
            throws FileNotFoundException, IOException {
        assertObjectNotNull("ous", ous);
        assertObjectNotNull("callback", handlingCall);
        final FileMakingOption option = createFileMakingOption(opLambda);
        assertObjectNotNull("option", option);
        assertMakingDelimiter(option);
        assertMakingEncoding(option);

        final String lineSep = prepareWritingLineSeparator(option);
        Writer writer = null; // is interface not to use newLine() for fixed line separator
        try {
            writer = new BufferedWriter(new OutputStreamWriter(ous, option.getEncoding()));
            doWriteFirst(writer, option, lineSep);

            final Set<String> doneMarkSet = new HashSet<String>(2);

            // write header
            final FileMakingHeaderInfo headerInfo = option.getFileMakingHeaderInfo();
            if (headerInfo != null) {
                final List<String> columnNameList = headerInfo.getColumnNameList();
                doWriteHeader(writer, columnNameList, option, doneMarkSet);
            }

            // write data row
            callbackDataRowWriter(handlingCall, option, lineSep, writer, doneMarkSet);
            writer.flush();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    protected void doWriteFirst(Writer writer, FileMakingOption option, String lineSep) throws IOException {
        if (option.isWriteLineSeparatorFirst()) {
            writer.write(lineSep);
        }
    }

    protected FileMakingOption createFileMakingOption(FileTokenOptionCall<FileMakingOption> opLambda) {
        assertObjectNotNull("opLambda", opLambda);
        final FileMakingOption op = newFileMakingOption();
        opLambda.callback(op);
        return op;
    }

    protected FileMakingOption newFileMakingOption() {
        return new FileMakingOption();
    }

    protected void doWriteHeader(Writer writer, List<String> columnNameList, FileMakingOption option, Set<String> doneMarkSet)
            throws IOException {
        if (doneMarkSet.contains(HEADER_DONE_MARK)) { // basically no way but just in case
            return;
        }
        if (columnNameList != null && !columnNameList.isEmpty()) {
            actuallyWriteHeader(writer, columnNameList, option, doneMarkSet);
        }
    }

    protected void actuallyWriteHeader(Writer writer, List<String> columnNameList, FileMakingOption option, Set<String> doneMarkSet)
            throws IOException {
        final String columnHeaderString = _lineToken.make(columnNameList, lineOp -> {
            lineOp.delimitateBy(option.getDelimiter());
            lineOp.trimSpace(); /* trimming is header only */
            reflectQuoteMinimally(option, lineOp);
        });
        writer.write(columnHeaderString);
        doneMarkSet.add(HEADER_DONE_MARK);
        doneMarkSet.add(FIRST_LINE_DONE_MARK);
    }

    protected void callbackDataRowWriter(FileMakingCallback callback, FileMakingOption option, String lineSep, Writer writer,
            Set<String> doneMarkSet) throws IOException {
        final FileMakingRowResource resource = createFileMakingRowResource();
        try {
            callback.write(new FileMakingRowWriter() {
                public void writeRow(List<String> valueList) throws IOException {
                    writeRow(resource.acceptRow(valueList));
                }

                public void writeRow(Map<String, String> columnValueMap) throws IOException {
                    writeRow(resource.acceptRow(columnValueMap));
                }

                protected void writeRow(FileMakingRowResource resource) throws IOException {
                    assertRowResourceOfWriter(resource);
                    doWriteDataRow(writer, resource, option, lineSep, doneMarkSet);
                }

                protected void assertRowResourceOfWriter(FileMakingRowResource resource) {
                    if (resource == null || !resource.hasRowData()) {
                        String msg = "The argument 'resource' of row writer should not be null.";
                        throw new IllegalArgumentException(msg);
                    }
                }
            });
        } catch (SQLException e) {
            String msg = "SQL handling failed in the row writing process: option=" + option;
            throw new FileMakingSQLHandlingFailureException(msg, e);
        }
    }

    protected FileMakingRowResource createFileMakingRowResource() {
        return new FileMakingRowResource();
    }

    protected void doWriteDataRow(Writer writer, FileMakingRowResource resource, FileMakingOption option, String lineSep,
            Set<String> doneMarkSet) throws IOException {
        if (!resource.hasRowData()) {
            return;
        }
        // not Collection because order is important here
        final List<String> valueList;
        if (resource.hasValueList()) {
            valueList = resource.getValueList();
        } else if (resource.hasValueMap()) {
            final Map<String, String> valueMap = resource.getValueMap();
            if (!doneMarkSet.contains(HEADER_DONE_MARK)) {
                final List<String> columnNameList = new ArrayList<String>(valueMap.keySet());
                option.headerInfo(columnNameList);
                doWriteHeader(writer, columnNameList, option, doneMarkSet);
                doneMarkSet.add(HEADER_DONE_MARK);
            }
            final FileMakingHeaderInfo headerInfo = option.getFileMakingHeaderInfo();
            final List<String> columnNameList = headerInfo.getColumnNameList();
            valueList = new ArrayList<String>(columnNameList.size());
            for (String columnName : columnNameList) {
                valueList.add(valueMap.get(columnName));
            }
        } else { // no way
            String msg = "Either value list or value map is required: " + resource;
            throw new IllegalStateException(msg);
        }
        checkValueCount(option, valueList);
        final String lineString = _lineToken.make(valueList, lineOp -> prepareWritingLineOption(option, lineOp));
        final String actualLine;
        if (doneMarkSet.contains(FIRST_LINE_DONE_MARK)) { // second or more line
            actualLine = lineSep + lineString;
        } else {
            actualLine = lineString;
        }
        writer.write(actualLine);
        doneMarkSet.add(FIRST_LINE_DONE_MARK);
        resource.clear();
    }

    protected String prepareWritingLineSeparator(FileMakingOption option) {
        final String lineSep;
        if (option.getLineSeparator() != null && !option.getLineSeparator().equals("")) {
            lineSep = option.getLineSeparator();
        } else {
            lineSep = "\n"; // default
        }
        return lineSep;
    }

    protected void prepareWritingLineOption(FileMakingOption option, LineMakingOption lineOp) {
        // create line option here to recycle instance only for data row
        // (header has original line option so not use this)
        lineOp.delimitateBy(option.getDelimiter());
        reflectQuoteMinimally(option, lineOp);
    }

    protected void reflectQuoteMinimally(FileMakingOption option, LineMakingOption lineOp) {
        if (option.isQuoteMinimally()) {
            lineOp.quoteMinimally();
        } else {
            lineOp.quoteAll(); // default
        }
    }

    protected void checkValueCount(FileMakingOption option, List<String> valueList) {
        if (option.isSuppressValueCountCheck()) {
            return;
        }
        final FileMakingHeaderInfo headerInfo = option.getFileMakingHeaderInfo();
        if (headerInfo == null) {
            return;
        }
        final List<String> columnNameList = headerInfo.getColumnNameList();
        if (columnNameList == null || columnNameList.isEmpty()) {
            return;
        }
        final int columnSize = columnNameList.size();
        final int valueSize = valueList.size();
        if (columnSize != valueSize) {
            throwFileMakingInvalidValueCountException(columnNameList, valueList);
        }
    }

    protected void throwFileMakingInvalidValueCountException(List<String> columnNameList, List<String> valueList) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The value count of the row does not match column count of header.");
        br.addItem("Column List");
        br.addElement(columnNameList);
        br.addElement("column count: " + columnNameList.size());
        br.addItem("Value List");
        br.addElement(valueList);
        br.addElement("value count: " + valueList.size());
        final String msg = br.buildExceptionMessage();
        throw new FileMakingInvalidValueCountException(msg);
    }

    // ===================================================================================
    //                                                                       Assert Helper
    //                                                                       =============
    // -----------------------------------------------------
    //                                         Assert Option
    //                                         -------------
    protected void assertMakingDelimiter(FileMakingOption option) {
        final String delimiter = option.getDelimiter();
        if (delimiter != null) { // don't trim it (might be tab)
            return;
        }
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The option of delimiter for FileToken is required.");
        br.addItem("Advice");
        br.addElement("You should specify delimiter by option.");
        br.addElement("For example:");
        br.addElement("  (x): new FileMakingOption().encodeAsUTF8()");
        br.addElement("  (o): new FileMakingOption().delimitateByTab().encodeAsUTF8()");
        br.addItem("Option");
        br.addElement(option);
        final String msg = br.buildExceptionMessage();
        throw new FileMakingRequiredOptionNotFoundException(msg);
    }

    protected void assertMakingEncoding(FileMakingOption option) {
        final String encoding = option.getEncoding();
        if (encoding != null && encoding.trim().length() > 0) {
            return;
        }
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The option of encoding for FileToken is required.");
        br.addItem("Advice");
        br.addElement("You should specify delimiter by option.");
        br.addElement("For example:");
        br.addElement("  (x): new FileMakingOption().delimitateByTab()");
        br.addElement("  (o): new FileMakingOption().delimitateByTab().encodeAsUTF8()");
        br.addItem("Option");
        br.addElement(option);
        final String msg = br.buildExceptionMessage();
        throw new FileMakingRequiredOptionNotFoundException(msg);
    }

    // -----------------------------------------------------
    //                                         Assert Object
    //                                         -------------
    protected void assertObjectNotNull(String variableName, Object value) {
        if (variableName == null) {
            String msg = "The value should not be null: variableName=null value=" + value;
            throw new IllegalArgumentException(msg);
        }
        if (value == null) {
            String msg = "The value should not be null: variableName=" + variableName;
            throw new IllegalArgumentException(msg);
        }
    }

    // -----------------------------------------------------
    //                                         Assert String
    //                                         -------------
    protected void assertStringNotNullAndNotTrimmedEmpty(String variableName, String value) {
        assertObjectNotNull("variableName", variableName);
        assertObjectNotNull(variableName, value);
        if (value.trim().length() == 0) {
            String msg = "The value should not be empty: variableName=" + variableName + " value=" + value;
            throw new IllegalArgumentException(msg);
        }
    }
}