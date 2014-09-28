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
package org.seasar.dbflute.helper.filesystem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.util.Srl;

/**
 * The file handling class for text IO.
 * <pre>
 * FileTextIO textIO = new FileTextIO().encodeAsUTF8();
 * textIO.read(...);
 * </pre>
 * @author jflute
 * @since 1.0.5K (2014/08/15 Friday)
 */
public class FileTextIO {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The encoding for the file. (Required) */
    protected String _encoding;

    // ===================================================================================
    //                                                                                Read
    //                                                                                ====
    /**
     * @param ins The input stream for the text. (NotNull)
     * @return The read text. (NotNull)
     */
    public String read(InputStream ins) {
        assertState();
        assertObjectNotNull("ins", ins);
        try {
            return readTextClosed(ins);
        } catch (IOException e) {
            return handleInputStreamReadFailureException(ins, e);
        }
    }

    /**
     * @param textPath The path to the text file. (NotNull)
     * @return The read text. (NotNull)
     */
    public String read(String textPath) {
        assertState();
        assertStringNotNullAndNotTrimmedEmpty("textPath", textPath);
        try {
            return readTextClosed(createFileInputStream(textPath));
        } catch (IOException e) {
            return handleTextFileReadFailureException(textPath, e);
        }
    }

    /**
     * @param textPath The path to the text file. (NotNull)
     * @param filter The filter of text line. (NotNull)
     * @return The filtered read text. (NotNull)
     */
    public String readFilteringLine(String textPath, FileTextLineFilter filter) {
        assertState();
        assertStringNotNullAndNotTrimmedEmpty("textPath", textPath);
        assertObjectNotNull("filter", filter);
        try {
            return filterAsLine(readTextClosed(createFileInputStream(textPath)), filter);
        } catch (IOException e) {
            return handleTextFileReadFailureException(textPath, e);
        }
    }

    /**
     * @param ins The input stream for the text. (NotNull)
     * @param filter The filter of text line. (NotNull)
     * @return The filtered read text. (NotNull)
     */
    public String readFilteringLine(InputStream ins, FileTextLineFilter filter) {
        assertState();
        assertObjectNotNull("ins", ins);
        assertObjectNotNull("filter", filter);
        try {
            return filterAsLine(readTextClosed(ins), filter);
        } catch (IOException e) {
            return handleInputStreamReadFailureException(ins, e);
        }
    }

    /**
     * @param textPath The path to the text file. (NotNull)
     * @param filter The filter of whole text. (NotNull)
     * @return The filtered read text. (NotNull)
     */
    public String readFilteringWhole(String textPath, FileTextWholeFilter filter) {
        assertState();
        assertStringNotNullAndNotTrimmedEmpty("textPath", textPath);
        assertObjectNotNull("filter", filter);
        try {
            return filterAsWhole(readTextClosed(createFileInputStream(textPath)), filter);
        } catch (IOException e) {
            return handleTextFileReadFailureException(textPath, e);
        }
    }

    /**
     * @param ins The input stream for the text. (NotNull)
     * @param filter The filter of whole text. (NotNull)
     * @return The filtered read text. (NotNull)
     */
    public String readFilteringWhole(InputStream ins, FileTextWholeFilter filter) {
        assertState();
        assertObjectNotNull("ins", ins);
        assertObjectNotNull("filter", filter);
        try {
            return filterAsWhole(readTextClosed(ins), filter);
        } catch (IOException e) {
            return handleInputStreamReadFailureException(ins, e);
        }
    }

    // ===================================================================================
    //                                                                             Rewrite
    //                                                                             =======
    /**
     * @param textPath The path to the text file. (NotNull)
     * @param filter The filter of text line. (NotNull)
     * @return The filtered written text. (NotNull)
     */
    public String rewriteFilteringLine(String textPath, FileTextLineFilter filter) {
        assertState();
        assertStringNotNullAndNotTrimmedEmpty("textPath", textPath);
        assertObjectNotNull("filter", filter);
        final String read = readFilteringLine(textPath, filter);
        write(textPath, read);
        return read;
    }

    /**
     * @param textPath The path to the text file. (NotNull)
     * @param filter The filter of whole text. (NotNull)
     * @return The filtered written text. (NotNull)
     */
    public String rewriteFilteringLine(String textPath, FileTextWholeFilter filter) {
        assertState();
        assertStringNotNullAndNotTrimmedEmpty("textPath", textPath);
        assertObjectNotNull("filter", filter);
        final String read = readFilteringWhole(textPath, filter);
        write(textPath, read);
        return read;
    }

    // ===================================================================================
    //                                                                               Write
    //                                                                               =====
    /**
     * @param ous The output stream for the text. (NotNull)
     * @param text The written text. (NotNull)
     */
    public void write(OutputStream ous, String text) {
        assertState();
        assertObjectNotNull("ous", ous);
        assertStringNotNullAndNotTrimmedEmpty("text", text);
        writeTextClosed(ous, text);
    }

    /**
     * @param textPath The path to the text file. (NotNull)
     * @param text The written text. (NotNull)
     */
    public void write(String textPath, String text) {
        assertState();
        assertStringNotNullAndNotTrimmedEmpty("textPath", textPath);
        assertStringNotNullAndNotTrimmedEmpty("text", text);
        writeTextClosed(createFileOutputStream(textPath), text);
    }

    /**
     * @param textPath The path to the text file. (NotNull)
     * @param text The written text. (NotNull)
     * @param filter The filter of text line. (NotNull)
     * @return The filtered written text. (NotNull)
     */
    public String writeFilteringLine(String textPath, String text, FileTextLineFilter filter) {
        assertState();
        assertStringNotNullAndNotTrimmedEmpty("textPath", textPath);
        assertStringNotNullAndNotTrimmedEmpty("text", text);
        final String filtered = filterAsLine(text, filter);
        writeTextClosed(createFileOutputStream(textPath), filtered);
        return filtered;
    }

    /**
     * @param ous The output stream for the text. (NotNull)
     * @param text The written text. (NotNull)
     * @param filter The filter of text line. (NotNull)
     * @return The filtered written text. (NotNull)
     */
    public String writeFilteringLine(OutputStream ous, String text, FileTextLineFilter filter) {
        assertState();
        assertObjectNotNull("ous", ous);
        assertStringNotNullAndNotTrimmedEmpty("text", text);
        final String filtered = filterAsLine(text, filter);
        writeTextClosed(ous, filtered);
        return filtered;
    }

    /**
     * @param textPath The path to the text file. (NotNull)
     * @param text The written text. (NotNull)
     * @param filter The filter of whole text. (NotNull)
     * @return The filtered written text. (NotNull)
     */
    public String writeFilteringLine(String textPath, String text, FileTextWholeFilter filter) {
        assertState();
        assertStringNotNullAndNotTrimmedEmpty("textPath", textPath);
        assertStringNotNullAndNotTrimmedEmpty("text", text);
        final String filtered = filterAsWhole(text, filter);
        writeTextClosed(createFileOutputStream(textPath), filtered);
        return filtered;
    }

    /**
     * @param ous The output stream for the text. (NotNull)
     * @param text The written text. (NotNull)
     * @param filter The filter of whole text. (NotNull)
     * @return The filtered written text. (NotNull)
     */
    public String writeFilteringLine(OutputStream ous, String text, FileTextWholeFilter filter) {
        assertState();
        assertObjectNotNull("ous", ous);
        assertStringNotNullAndNotTrimmedEmpty("text", text);
        final String filtered = filterAsWhole(text, filter);
        writeTextClosed(ous, filtered);
        return filtered;
    }

    // ===================================================================================
    //                                                                              Option
    //                                                                              ======
    /**
     * Encode file as UTF-8.
     * @return this. (NotNull)
     */
    public FileTextIO encodeAsUTF8() {
        _encoding = "UTF-8";
        return this;
    }

    /**
     * Encode file as Windows-31J.
     * @return this. (NotNull)
     */
    public FileTextIO encodeAsWindows31J() {
        _encoding = "Windows-31J";
        return this;
    }

    // ===================================================================================
    //                                                                       Stream Helper
    //                                                                       =============
    protected FileInputStream createFileInputStream(String textPath) {
        try {
            return new FileInputStream(textPath);
        } catch (FileNotFoundException e) {
            String msg = "Not found the text file: " + textPath;
            throw new IllegalStateException(msg, e);
        }
    }

    protected FileOutputStream createFileOutputStream(String textPath) {
        try {
            return new FileOutputStream(textPath);
        } catch (FileNotFoundException e) {
            String msg = "Not found the text file: " + textPath;
            throw new IllegalStateException(msg, e);
        }
    }

    protected byte[] readBytesClosed(InputStream ins) throws IOException {
        try {
            final byte[] buffer = new byte[8192];
            final ByteArrayOutputStream ous = new ByteArrayOutputStream();
            int next = 0;
            while ((next = ins.read(buffer, 0, buffer.length)) != -1) {
                ous.write(buffer, 0, next);
            }
            return ous.toByteArray();
        } finally {
            if (ins != null) {
                try {
                    ins.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    protected String readTextClosed(InputStream ins) throws IOException {
        final byte[] bytes = readBytesClosed(ins);
        try {
            return new String(bytes, _encoding);
        } catch (UnsupportedEncodingException e) {
            String msg = "Unknown encoding: " + _encoding;
            throw new IllegalStateException(msg, e);
        }
    }

    protected void writeTextClosed(OutputStream ous, String text) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(ous, _encoding));
            writer.write(text);
            writer.flush();
        } catch (IOException e) {
            handleOutputStreamWriteFailureException(ous, e);
        } finally {
            close(writer);
        }
    }

    protected void close(BufferedReader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException ignored) {
            }
        }
    }

    protected void close(BufferedWriter writer) {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException ignored) {
            }
        }
    }

    protected void handleOutputStreamWriteFailureException(OutputStream ous, IOException e) {
        String msg = "Failed to write the text to the output stream: " + ous;
        throw new IllegalStateException(msg, e);
    }

    protected void handleTextFileWriteFailureException(String textPath, IOException e) {
        String msg = "Failed to write the text file: " + textPath;
        throw new IllegalStateException(msg, e);
    }

    protected String handleInputStreamReadFailureException(InputStream ins, IOException e) {
        String msg = "Failed to read the input stream: " + ins;
        throw new IllegalStateException(msg, e);
    }

    protected String handleTextFileReadFailureException(String textPath, IOException e) {
        String msg = "Failed to read the text file: " + textPath;
        throw new IllegalStateException(msg, e);
    }

    // ===================================================================================
    //                                                                         Line Helper
    //                                                                         ===========
    protected String filterAsLine(String text, FileTextLineFilter filter) {
        final String cr = "\r";
        final String lf = "\n";
        final StringBuilder sb = new StringBuilder();
        final List<String> lineList = Srl.splitList(text, lf);
        final int lineCount = lineList.size();
        int index = 0;
        for (String line : lineList) {
            final String pureLine;
            final boolean hasCR;
            if (line.endsWith(cr)) {
                pureLine = Srl.substringLastFront(line, cr);
                hasCR = true;
            } else {
                pureLine = line;
                hasCR = false;
            }
            final String filteredLine = filter.filter(pureLine);
            if (filteredLine != null) {
                sb.append(filteredLine);
                if (index + 1 < lineCount) { // not last line
                    sb.append(hasCR ? cr : "").append(lf);
                }
            }
            ++index;
        }
        return sb.toString();
    }

    protected String filterAsWhole(String text, FileTextWholeFilter filter) {
        final String filtered = filter.filter(text);
        return filtered != null ? filtered : "";
    }

    // ===================================================================================
    //                                                                       Assert Helper
    //                                                                       =============
    protected void assertState() {
        if (_encoding == null || _encoding.trim().length() == 0) {
            final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
            br.addNotice("Not found the encoding for the file.");
            br.addItem("Advice");
            br.addElement("You should specify 'encoding' like this:");
            br.addElement("  (o):");
            br.addElement("    FileTextIO textIO = new FileTextIO().encodeAsUTF8();");
            final String msg = br.buildExceptionMessage();
            throw new IllegalStateException(msg);
        }
    }

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

    protected void assertStringNotNullAndNotTrimmedEmpty(String variableName, String value) {
        assertObjectNotNull("variableName", variableName);
        assertObjectNotNull(variableName, value);
        if (value.trim().length() == 0) {
            String msg = "The value should not be empty: variableName=" + variableName + " value=" + value;
            throw new IllegalArgumentException(msg);
        }
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setEncoding(String encoding) {
        _encoding = encoding;
    }
}
