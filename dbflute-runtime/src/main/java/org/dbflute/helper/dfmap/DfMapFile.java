/*
 * Copyright 2014-2022 the original author or authors.
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
package org.dbflute.helper.dfmap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The parser of file as map style.
 * 
 * <p>Detail Specification:</p>
 * <pre>
 * o using DfMapStyle
 * o file encoding is fixedly UTF-8
 * o trimmed lines that start with '#' is treated as line comment
 * o basically thread-safe if no changing option in progress
 * </pre>
 * @author jflute
 * @since 1.1.8 (2018/05/05 Saturday)
 */
public class DfMapFile { // migrated MapListFile, basically keeping compatible

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final String UTF8_ENCODING = "UTF-8";
    public static final String LINE_COMMENT_MARK = "#"; // public for client

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _fileEncoding;
    protected final String _lineCommentMark;
    protected boolean _skipLineSeparator;
    protected boolean _checkDuplicateEntry;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * Constructor for settings as default.
     */
    public DfMapFile() {
        this(UTF8_ENCODING, LINE_COMMENT_MARK); // as default
    }

    protected DfMapFile(String fileEncoding, String lineCommentMark) {
        assertStringNotNullAndNotTrimmedEmpty("fileEncoding", fileEncoding);
        assertStringNotNullAndNotTrimmedEmpty("lineCommentMark", lineCommentMark);
        _fileEncoding = fileEncoding;
        _lineCommentMark = lineCommentMark;
    }

    // -----------------------------------------------------
    //                                                Option
    //                                                ------
    public DfMapFile skipLineSeparator() {
        _skipLineSeparator = true;
        return this;
    }

    public DfMapFile checkDuplicateEntry() {
        _checkDuplicateEntry = true;
        return this;
    }

    // ===================================================================================
    //                                                                                Map
    //                                                                               =====
    // -----------------------------------------------------
    //                                                 Read
    //                                                ------
    /**
     * Read the map string file. <br>
     * If the type of values is various type, this method is available. <br>
     * A trimmed line that starts with '#' is treated as line comment. <br>
     * This is the most basic method in the map-handling methods.
     * <pre>
     * map:{
     *     ; key1 = string-value1
     *     ; key2 = list:{element1 ; element2 }
     *     ; key3 = map:{key1 = value1 ; key2 = value2 }
     *     ; ... = ...
     * }
     * </pre>
     * @param ins The input stream for DBFlute property file, which is closed here. (NotNull)
     * @return The read map. (NotNull, EmptyAllowed)
     * @throws IOException When it fails to handle the IO.
     */
    public Map<String, Object> readMap(InputStream ins) throws IOException {
        assertObjectNotNull("ins", ins);
        final String mapString = readString(ins);
        final DfMapStyle mapStyle = createMapStyle();
        if (mapString.trim().length() > 0) {
            return mapStyle.fromMapString(mapString);
        } else { // empty
            return createResultMap(mapStyle);
        }
    }

    /**
     * Read the map string file as specified typed map value. <br>
     * If the type of all values is string map type, this method is available. <br>
     * A trimmed line that starts with '#' is treated as line comment.
     * <pre>
     * e.g.
     * map:{
     *     ; key1 = map:{string-key1 = string-value1 ; string-key2 = string-value2 }
     *     ; key2 = map:{string-key1 = string-value1 ; string-key2 = string-value2 }
     *     ; ... = map:{...}
     * }
     * </pre>
     * @param <VALUE> The type of value.
     * @param ins The input stream for DBFlute property file, which is closed here. (NotNull)
     * @param valueType The type of value to cast. (NotNull)
     * @return The read map whose values are typed. (NotNull, EmptyAllowed)
     * @throws IOException When it fails to handle the IO.
     */
    public <VALUE> Map<String, VALUE> readMap(InputStream ins, Class<VALUE> valueType) throws IOException {
        assertObjectNotNull("ins", ins);
        assertObjectNotNull("valueType", valueType);
        final String mapString = readString(ins);
        final DfMapStyle mapStyle = createMapStyle();
        if (mapString.trim().length() > 0) {
            final Map<String, Object> plainMap = mapStyle.fromMapString(mapString);
            @SuppressWarnings("unchecked")
            final Map<String, VALUE> resultMap = (Map<String, VALUE>) createResultMap(mapStyle);
            for (Entry<String, Object> entry : plainMap.entrySet()) {
                final Object value = entry.getValue();
                resultMap.put(entry.getKey(), value != null ? valueType.cast(value) : null);
            }
            return resultMap;
        } else { // empty
            @SuppressWarnings("unchecked")
            final Map<String, VALUE> emptyMap = (Map<String, VALUE>) createResultMap(mapStyle);
            return emptyMap;
        }
    }

    // -----------------------------------------------------
    //                                                 Write
    //                                                 -----
    public void writeMap(OutputStream ous, Map<String, ? extends Object> map) throws IOException {
        final DfMapStyle mapStyle = createMapStyle();
        final String mapString = mapStyle.toMapString(map);
        writeString(ous, mapString);
    }

    // ===================================================================================
    //                                                                               List
    //                                                                              ======
    // -----------------------------------------------------
    //                                                 Read
    //                                                ------
    /**
     * Read the list string file. <br>
     * If the type of values is various type, this method is available. <br>
     * A trimmed line that starts with '#' is treated as line comment. <br>
     * <pre>
     * list:{
     *     ; element1
     *     ; list:{element2-1 ; element2-2 }
     *     ; map:{key3-1 = value3-1 ; key3-2 = value3-2 }
     *     ; ...
     * }
     * </pre>
     * @param ins The input stream for DBFlute property file, which is closed here. (NotNull)
     * @return The read list. (NotNull, EmptyAllowed)
     * @throws IOException When it fails to handle the IO.
     */
    public List<Object> readList(InputStream ins) throws IOException {
        assertObjectNotNull("ins", ins);
        final String listString = readString(ins);
        final DfMapStyle mapStyle = createMapStyle();
        if (listString.trim().length() > 0) {
            return mapStyle.fromListString(listString);
        } else {
            return mapStyle.newObjectList();
        }
    }

    /**
     * Read the list string file, as specified typed list element. <br>
     * If the type of values is various type, this method is available. <br>
     * A trimmed line that starts with '#' is treated as line comment. <br>
     * <pre>
     * list:{
     *     ; string-value1
     *     ; string-value1
     *     ; ...
     * }
     * </pre>
     * @param <ELEMENT> The type of list element.
     * @param ins The input stream for DBFlute property file, which is closed here. (NotNull)
     * @param elementType The type of list element to cast. (NotNull)
     * @return The read list whose elements are typed. (NotNull, EmptyAllowed)
     * @throws IOException When it fails to handle the IO.
     */
    public <ELEMENT> List<ELEMENT> readList(InputStream ins, Class<ELEMENT> elementType) throws IOException {
        assertObjectNotNull("ins", ins);
        assertObjectNotNull("elementType", elementType);
        final String listString = readString(ins);
        final DfMapStyle mapStyle = createMapStyle();
        if (listString.trim().length() > 0) {
            final List<Object> plainList = mapStyle.fromListString(listString);
            @SuppressWarnings("unchecked")
            final List<ELEMENT> resultList = (List<ELEMENT>) createResultList(mapStyle);
            for (Object element : plainList) {
                resultList.add(element != null ? elementType.cast(element) : null);
            }
            return resultList;
        } else {
            @SuppressWarnings("unchecked")
            final List<ELEMENT> emptyList = (List<ELEMENT>) createResultList(mapStyle);
            return emptyList;
        }
    }

    // -----------------------------------------------------
    //                                                 Write
    //                                                 -----
    public void writeList(OutputStream ous, List<? extends Object> list) throws IOException {
        final DfMapStyle mapStyle = createMapStyle();
        final String listString = mapStyle.toListString(list);
        writeString(ous, listString);
    }

    // ===================================================================================
    //                                                                              String
    //                                                                              ======
    // -----------------------------------------------------
    //                                                  Read
    //                                                  ----
    /**
     * Read the string file. <br>
     * A trimmed line that starts with '#' is treated as line comment.
     * @param ins The input stream for DBFlute property file. (NotNull)
     * @return The read string. (NotNull)
     * @throws IOException When it fails to handle the IO.
     */
    public String readString(InputStream ins) throws IOException {
        assertObjectNotNull("ins", ins);
        final String encoding = _fileEncoding;
        final String lineCommentMark = _lineCommentMark;
        final StringBuilder sb = new StringBuilder();
        final boolean addLn = !_skipLineSeparator;
        InputStreamReader ir = null;
        BufferedReader br = null;
        try {
            ir = new InputStreamReader(ins, encoding);
            br = new BufferedReader(ir);

            int loopIndex = -1;
            int validlineCount = -1;
            boolean previousLineComment = false;
            while (true) {
                ++loopIndex;
                String lineString = br.readLine();
                if (lineString == null) {
                    if (previousLineComment && addLn) {
                        sb.append(ln()); // line separator adjustment
                    }
                    break;
                }
                if (loopIndex == 0) {
                    // it needs to before line comment process
                    // because the BOM character is not trimmed by trim()
                    lineString = removeInitialUnicodeBomIfNeeds(encoding, lineString);
                }
                // if the line is comment, skip to read
                if (lineString.trim().startsWith(lineCommentMark)) {
                    previousLineComment = true;
                    continue;
                }
                previousLineComment = false;
                ++validlineCount;
                if (validlineCount > 0 && addLn) {
                    sb.append(ln());
                }
                sb.append(lineString);
            }
        } catch (UnsupportedEncodingException e) {
            String msg = "The encoding is unsupported: " + encoding;
            throw new IllegalStateException(msg, e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ignored) {}
            }
        }
        return sb.toString();
    }

    // -----------------------------------------------------
    //                                                 Write
    //                                                 -----
    public void writeString(OutputStream ous, String str) throws IOException {
        assertObjectNotNull("ous", ous);
        assertObjectNotNull("str", str);
        final String encoding = _fileEncoding;
        OutputStreamWriter ow = null;
        BufferedWriter bw = null;
        try {
            ow = new OutputStreamWriter(ous, encoding);
            bw = new BufferedWriter(ow);
            bw.write(removeInitialUnicodeBomIfNeeds(encoding, str));
            bw.flush();
        } catch (UnsupportedEncodingException e) {
            String msg = "The encoding is unsupported: " + encoding;
            throw new IllegalStateException(msg, e);
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ignored) {}
            }
        }
    }

    // ===================================================================================
    //                                                                           Map Style
    //                                                                           =========
    protected DfMapStyle createMapStyle() {
        final DfMapStyle mapStyle = newMapStyle();
        if (_checkDuplicateEntry) {
            mapStyle.checkDuplicateEntry();
        }
        return mapStyle;
    }

    protected DfMapStyle newMapStyle() {
        return new DfMapStyle();
    }

    protected Map<String, Object> createResultMap(DfMapStyle mapStyle) {
        return mapStyle.newStringObjectMap();
    }

    protected List<Object> createResultList(DfMapStyle mapStyle) {
        return mapStyle.newObjectList();
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    protected String removeInitialUnicodeBomIfNeeds(String encoding, String value) {
        if (UTF8_ENCODING.equalsIgnoreCase(encoding) && value.length() > 0 && value.charAt(0) == '\uFEFF') {
            return value.substring(1);
        } else {
            return value;
        }
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String ln() {
        return "\n";
    }

    protected void assertStringNotNullAndNotTrimmedEmpty(String variableName, String value) {
        assertObjectNotNull("variableName", variableName);
        assertObjectNotNull(variableName, value);
        if (value.trim().length() == 0) {
            String msg = "The value should not be empty: variableName=" + variableName + " value=" + value;
            throw new IllegalArgumentException(msg);
        }
    }

    protected void assertObjectNotNull(String variableName, Object value) {
        if (variableName == null) {
            throw new IllegalArgumentException("The variableName should not be null.");
        }
        if (value == null) {
            throw new IllegalArgumentException("The argument '" + variableName + "' should not be null.");
        }
    }
}