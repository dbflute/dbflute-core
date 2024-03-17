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
package org.dbflute.helper.dfmap;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dbflute.helper.dfmap.exception.DfMapDuplicateEntryException;
import org.dbflute.helper.dfmap.exception.DfMapParseFailureException;
import org.dbflute.helper.message.ExceptionMessageBuilder;

/**
 * The parser for DBFlute map style.
 * <pre>
 * e.g. map-string as one liner
 *   map:{key1=value1;key2=list:{value21;value22;value23},key3=map:{key31=value31}}
 * 
 * e.g. map-string as formatted
 *   map:{
 *       ; key1 = value1
 *       ; key2 = list:{ value21 ; value22 ; value23 }
 *       ; key3 = map:{ key31 ; value31 }
 *   }
 * 
 * e.g. list-string
 *   list:{key1=value1,key2=list:{value21,value22,value23},key3=map:{key31=value31}}
 * </pre>
 * 
 * <p>Detail Specification:</p>
 * <pre>
 * o all elements are trimmed e.g. map:{ sea   = land    } to {sea=land}
 * o empty or null value is treated as null e.g. map:{ sea = ; land = null } to both null
 * o empty elements are ignored e.g. list:{ ; sea ; ; land } to [sea, land]
 * o can escape control marks e.g. '{', ';' by escape character '\'
 * o but cannot escape escape character itself
 * o basically cannot change control mark, but you can override...
 * o can check duplicate entry of map key by option checkDuplicateEntry()
 * o can print as one liner by option printOneLiner()
 * o non-thread-safe, uses temporary variables
 * o but can recycle in same thread 
 * </pre>
 * @author jflute
 * @since 1.1.8 (2018/05/05 Saturday)
 */
public class DfMapStyle { // migrated MapListString, basically keeping compatible

    // this original code (MapListString) was written when jflute was very young
    // and migrated to this class with small refactoring for performance
    // all lines are very memorable...

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The object prefix of map. */
    public static final String MAP_PREFIX = "map:";

    /** The object prefix of list. */
    public static final String LIST_PREFIX = "list:";

    /** The control mark of begin-brace. */
    public static final String BEGIN_BRACE = "{";

    /** The control mark of end-brace. */
    public static final String END_BRACE = "}";

    /** The control mark of element-delimiter. */
    public static final String ELEMENT_DELIMITER = ";";

    /** The control mark of value-equal. */
    public static final String VALUE_EQUAL = "=";

    /** The escape character for control marks. */
    protected static final String ESCAPE_CHAR = "\\";

    /** The temporary mark of escaped escape character. */
    protected static final String ESCAPED_ESCAPE_MARK = "$$df:escapedEscape$$";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                         Object Prefix
    //                                         -------------
    /** The object prefix of map. e.g. "map:" (NotNull) */
    protected final String _mapPrefix;

    /** The object prefix of list. e.g. "list:" (NotNull) */
    protected final String _listPrefix;

    // -----------------------------------------------------
    //                                          Control Mark
    //                                          ------------
    /** The control mark of begin-brace. e.g. "{" (NotNull) */
    protected final String _beginBrace;

    /** The control mark of end-brace. e.g. "}" (NotNull) */
    protected final String _endBrace;

    /** The control mark of element-delimiter. e.g. ";" (NotNull) */
    protected final String _elementDelimiter;

    /** The control mark of value-equal. e.g. "=" (NotNull) */
    protected final String _valueEqual;

    /** The escape character for control marks. "\\" (NotNull) */
    protected final String _escapeChar;

    // -----------------------------------------------------
    //                                     Cached Expression
    //                                     -----------------
    /** The cached expression of begin-brace for map. e.g. "map:{" */
    protected final String _mappingBeginBrace;

    /** The cached expression of begin-brace for list. e.g. "list:{" */
    protected final String _listingBeginBrace;

    // -----------------------------------------------------
    //                                             Temporary
    //                                             ---------
    /** The whole map-string of logical remainder as temporary variable for parsing process. (NullAllowed: when no process) */
    protected String _remainderString;

    /** The current index of remainder string for parsing process. */
    protected int _currentRemainderIndex;

    // -----------------------------------------------------
    //                                                Option
    //                                                ------
    /** Does it check duplicate entry of map for generation? */
    protected boolean _checkDuplicateEntry;

    /** Does it print as one-liner for building? */
    protected boolean _printOneLiner;

    /** Is it without display side spaces? */
    protected boolean _withoutDisplaySideSpace;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * Constructor for settings as default.
     */
    public DfMapStyle() {
        this(MAP_PREFIX, LIST_PREFIX, BEGIN_BRACE, END_BRACE, ELEMENT_DELIMITER, VALUE_EQUAL, ESCAPE_CHAR);
    }

    // you can use as public in your constructor of sub-class
    protected DfMapStyle(String mapPrefix, String listPrefix, String beginBrace, String endBrace, String elementDelimiter,
            String valueEqual, String escapeChar) {
        assertStringNotNullAndNotTrimmedEmpty("mapPrefix", mapPrefix);
        assertStringNotNullAndNotTrimmedEmpty("listPrefix", listPrefix);
        assertStringNotNullAndNotTrimmedEmpty("beginBrace", beginBrace);
        assertStringNotNullAndNotTrimmedEmpty("endBrace", endBrace);
        assertStringNotNullAndNotTrimmedEmpty("delimiter", elementDelimiter);
        assertStringNotNullAndNotTrimmedEmpty("valueEqual", valueEqual);
        assertStringNotNullAndNotTrimmedEmpty("escapeChar", escapeChar);
        _mapPrefix = mapPrefix;
        _listPrefix = listPrefix;
        _beginBrace = beginBrace;
        _endBrace = endBrace;
        _elementDelimiter = elementDelimiter;
        _valueEqual = valueEqual;
        _escapeChar = escapeChar;
        _mappingBeginBrace = _mapPrefix + _beginBrace;
        _listingBeginBrace = _listPrefix + _beginBrace;
    }

    // -----------------------------------------------------
    //                                                Option
    //                                                ------
    /**
     * Check duplicate entry of map for generation. (throws exception if found)
     * @return this. (NotNull)
     */
    public DfMapStyle checkDuplicateEntry() {
        _checkDuplicateEntry = true;
        return this;
    }

    /**
     * Print as one liner for building.
     * @return this. (NotNull)
     */
    public DfMapStyle printOneLiner() {
        _printOneLiner = true;
        return this;
    }

    /**
     * Print without display side spaces. <br>
     * e.g. map:{ sea = mystic ; land = oneman } to map:{sea=mystic;land=oneman}.
     * @return this. (NotNull)
     */
    public DfMapStyle withoutDisplaySideSpace() {
        _withoutDisplaySideSpace = true;
        return this;
    }

    // ===================================================================================
    //                                                                    Object to String
    //                                                                    ================
    // -----------------------------------------------------
    //                                          to MapString
    //                                          ------------
    /**
     * Convert to map-string from the map object.
     * @param map The map object that has string keys. (NotNull, EmptyAllowed)
     * @return The string as map expression. (NotNull)
     */
    public String toMapString(Map<String, ? extends Object> map) {
        assertObjectNotNull("map", map);
        final StringBuilder sb = new StringBuilder();
        @SuppressWarnings("unchecked")
        final Map<String, Object> casted = (Map<String, Object>) map;
        final boolean printOneLiner = isPrintOneLiner();
        final boolean withoutDisplaySideSpace = isWithoutDisplaySideSpace();
        buildMapString(sb, casted, printOneLiner, "", printOneLiner ? "" : "    ", withoutDisplaySideSpace);
        return sb.toString();
    }

    protected void buildMapString(StringBuilder sb, Map<String, Object> map, boolean printOneLiner, String previousIndent,
            String currentIndent, boolean withoutDisplaySideSpace) {
        doBuildMapStringBegin(sb, map, printOneLiner, previousIndent, currentIndent);
        if (!map.isEmpty()) {
            int index = 0;
            for (Entry<String, ? extends Object> entry : map.entrySet()) {
                doBuildMapStringCurrentEntry(sb, printOneLiner, previousIndent, currentIndent, withoutDisplaySideSpace, index,
                        entry.getKey(), entry.getValue());
                ++index;
            }
            if (printOneLiner) {
                appendDisplaySideSpace(sb, withoutDisplaySideSpace);
            } else {
                sb.append(ln()).append(previousIndent);
            }
        }
        doBuildMapStringEnd(sb, map, printOneLiner, previousIndent, currentIndent);
    }

    protected void doBuildMapStringBegin(StringBuilder sb, Map<String, Object> map, boolean printOneLiner, String previousIndent,
            String currentIndent) {
        sb.append(_mapPrefix).append(_beginBrace);
    }

    protected void doBuildMapStringCurrentEntry(StringBuilder sb, boolean printOneLiner, String previousIndent, String currentIndent,
            boolean withoutDisplaySideSpace, int index, String key, Object value) {
        if (printOneLiner) {
            if (index > 0) {
                if (!isWithoutDisplaySideSpace()) {
                    sb.append(" ");
                }
                sb.append(_elementDelimiter);
            }
        } else {
            sb.append(ln()).append(currentIndent).append(_elementDelimiter);
        }
        appendDisplaySideSpace(sb, withoutDisplaySideSpace);
        sb.append(escapeControlMarkAsMap(key));
        appendDisplaySideSpace(sb, withoutDisplaySideSpace);
        sb.append(_valueEqual);
        if (!isWithoutDisplaySideSpace()) {
            sb.append(" ");
        }
        if (value instanceof Map<?, ?>) {
            @SuppressWarnings("unchecked")
            final Map<String, Object> valueMap = (Map<String, Object>) value;
            final String nextIndent = deriveNextIndentOfBuildingMapString(printOneLiner, previousIndent, currentIndent);
            buildMapString(sb, valueMap, printOneLiner, currentIndent, nextIndent, withoutDisplaySideSpace);
        } else if (value instanceof List<?>) {
            @SuppressWarnings("unchecked")
            final List<Object> valueList = (List<Object>) value;
            final String nextIndent = deriveNextIndentOfBuildingMapString(printOneLiner, previousIndent, currentIndent);
            buildListString(sb, valueList, printOneLiner, currentIndent, nextIndent, withoutDisplaySideSpace);
        } else {
            final Map<String, Object> resolvedMap = resolvePotentialMapOfBuildingMapString(value);
            if (resolvedMap != null) {
                final String nextIndent = deriveNextIndentOfBuildingMapString(printOneLiner, previousIndent, currentIndent);
                buildMapString(sb, resolvedMap, printOneLiner, currentIndent, nextIndent, withoutDisplaySideSpace);
            } else {
                sb.append(escapeControlMarkAsMap(value));
            }
        }
    }

    protected void doBuildMapStringEnd(StringBuilder sb, Map<String, Object> map, boolean printOneLiner, String previousIndent,
            String currentIndent) {
        sb.append(_endBrace);
    }

    // -----------------------------------------------------
    //                                         to ListString
    //                                         -------------
    /**
     * Convert to list-string from the list object.
     * @param list The list object that has object elements. (NotNull, EmptyAllowed)
     * @return The string as list expression. (NotNull)
     */
    public String toListString(List<? extends Object> list) {
        assertObjectNotNull("list", list);
        final StringBuilder sb = new StringBuilder();
        @SuppressWarnings("unchecked")
        final List<Object> casted = (List<Object>) list;
        final boolean printOneLiner = isPrintOneLiner();
        final boolean withoutDisplaySideSpace = isWithoutDisplaySideSpace();
        buildListString(sb, casted, printOneLiner, "", printOneLiner ? "" : "    ", withoutDisplaySideSpace);
        return sb.toString();
    }

    protected void buildListString(StringBuilder sb, List<? extends Object> list, boolean printOneLiner, String previousIndent,
            String currentIndent, boolean withoutDisplaySideSpace) {
        doBuildListStringBegin(sb, list, printOneLiner, previousIndent, currentIndent);
        if (!list.isEmpty()) {
            int index = 0;
            for (Object value : list) {
                doBuildListStringCurrentElement(sb, printOneLiner, previousIndent, currentIndent, withoutDisplaySideSpace, index, value);
                ++index;
            }
            if (printOneLiner) {
                appendDisplaySideSpace(sb, withoutDisplaySideSpace);
            } else {
                sb.append(ln()).append(previousIndent);
            }
        }
        doBuildListStringEnd(sb, list, printOneLiner, previousIndent, currentIndent);
    }

    protected void doBuildListStringBegin(StringBuilder sb, List<? extends Object> list, boolean printOneLiner, String previousIndent,
            String currentIndent) {
        sb.append(_listPrefix).append(_beginBrace);
    }

    protected void doBuildListStringCurrentElement(StringBuilder sb, boolean printOneLiner, String previousIndent, String currentIndent,
            boolean withoutDisplaySideSpace, int index, Object value) {
        if (printOneLiner) {
            if (index > 0) {
                appendDisplaySideSpace(sb, withoutDisplaySideSpace);
                sb.append(_elementDelimiter);
            }
        } else {
            sb.append(ln()).append(currentIndent).append(_elementDelimiter);
        }
        appendDisplaySideSpace(sb, withoutDisplaySideSpace);
        if (value instanceof Map<?, ?>) {
            @SuppressWarnings("unchecked")
            final Map<String, Object> valueMap = (Map<String, Object>) value;
            final String nextIndent = deriveNextIndentOfBuildingMapString(printOneLiner, previousIndent, currentIndent);
            buildMapString(sb, valueMap, printOneLiner, currentIndent, nextIndent, withoutDisplaySideSpace);
        } else if (value instanceof List<?>) {
            @SuppressWarnings("unchecked")
            final List<Object> valueList = (List<Object>) value;
            final String nextIndent = deriveNextIndentOfBuildingMapString(printOneLiner, previousIndent, currentIndent);
            buildListString(sb, valueList, printOneLiner, currentIndent, nextIndent, withoutDisplaySideSpace);
        } else {
            final Map<String, Object> resolvedMap = resolvePotentialMapOfBuildingMapString(value);
            if (resolvedMap != null) {
                final String nextIndent = deriveNextIndentOfBuildingMapString(printOneLiner, previousIndent, currentIndent);
                buildMapString(sb, resolvedMap, printOneLiner, currentIndent, nextIndent, withoutDisplaySideSpace);
            } else {
                sb.append(escapeControlMarkAsList(value));
            }
        }
    }

    protected void doBuildListStringEnd(StringBuilder sb, List<? extends Object> list, boolean printOneLiner, String previousIndent,
            String currentIndent) {
        sb.append(_endBrace);
    }

    // -----------------------------------------------------
    //                                          Assist Logic
    //                                          ------------
    protected String deriveNextIndentOfBuildingMapString(boolean printOneLiner, String previousIndent, String currentIndent) {
        return printOneLiner ? "" : generally_calculateNextIndent(previousIndent, currentIndent);
    }

    protected Map<String, Object> resolvePotentialMapOfBuildingMapString(Object value) { // value may be null
        // you can override for your bean that can be map like this:
        // e.g.
        //  if (value instanceof Something) {
        //      return ((Something) value).toMap();
        //  } else {
        //      return super.resolvePotentialMapOfBuildingMapString(value);
        //  }
        return null; // returning null means non-map value
    }

    protected void appendDisplaySideSpace(StringBuilder sb, boolean withoutDisplaySideSpace) {
        if (!withoutDisplaySideSpace) {
            sb.append(" ");
        }
    }

    // ===================================================================================
    //                                                                  Object from String
    //                                                                  ==================
    // -----------------------------------------------------
    //                                        from MapString
    //                                        --------------
    /**
     * Convert to map object from the map-string.
     * @param mapString The string as map expression. (NotNull, NotEmpty)
     * @return The map for the map-string, mutable for compatible. (NotNull, EmptyAllowed)
     */
    public Map<String, Object> fromMapString(String mapString) {
        assertStringNotNullAndNotTrimmedEmpty("mapString", mapString);
        beginRemainder(mapString);
        try {
            assertFirstMapString();
            ltrimRemainder();
            removePrefixMapingBeginBrace();

            final Map<String, Object> map = newStringObjectMap();
            parseMapString(map);

            assertLastRemainderAsMap(map);
            return map;
        } finally {
            endRemainder();
        }
    }

    // -----------------------------------------------------
    //                                          to MapString
    //                                          ------------
    /**
     * Convert to map object from list-string.
     * @param listString The string as list expression. (NotNull, NotEmpty)
     * @return The list for the list-string, mutable for compatible. (NotNull, EmptyAllowed)
     */
    public List<Object> fromListString(String listString) {
        assertStringNotNullAndNotTrimmedEmpty("listString", listString);
        beginRemainder(listString);
        try {
            assertFirstListString();
            ltrimRemainder();
            removePrefixListingBeginBrace();

            final List<Object> list = newObjectList();
            parseListString(list);

            assertLastRemainderAsList(list);
            return list;
        } finally {
            endRemainder();
        }
    }

    // ===================================================================================
    //                                                                               Parse
    //                                                                               =====
    /**
     * Parse the current remainder string as map.
     * @param currentMap The current map made by parse process. (NotNull)
     */
    protected void parseMapString(Map<String, Object> currentMap) {
        while (true) {
            if (!prepareLoopBeginning()) {
                return;
            }

            // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
            // here, remainder string should starts with the key of the map
            // _/_/_/_/_/_/_/_/_/_/

            final int equalIndex = indexOfEqual();
            assertMapStringEqualIndex(equalIndex, currentMap);
            final String mapKey = substringRemainderFront(equalIndex);
            removePrefixTargetIndexPlus(equalIndex, _valueEqual.length());
            ltrimRemainder();

            // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
            // here, remainder string should starts with the value of the map
            // _/_/_/_/_/_/_/_/_/_/

            if (startsWithMapBeginBrace()) {
                removePrefixMapingBeginBrace();
                parseMapString(setupNestMap(currentMap, mapKey));
                if (closeAfterParseNestMapList()) {
                    return;
                }
                continue;
            }

            if (startsWithListBeginBrace()) {
                removePrefixListingBeginBrace();
                parseListString(setupNestList(currentMap, mapKey));
                if (closeAfterParseNestMapList()) {
                    return;
                }
                continue;
            }

            final int delimiterIndex = indexOfDelimiter();
            final int endBraceIndex = indexOfEndBrace();
            assertMapStringEndBraceIndex(endBraceIndex, currentMap);

            if (delimiterIndex >= 0 && delimiterIndex < endBraceIndex) { // delimiter exists
                // e.g. value1 ; key2=value2}
                final String mapValue = substringRemainderFront(delimiterIndex);
                registerToMap(currentMap, filterParsedMapKey(mapKey), filterParsedMapValue(mapValue));

                // because the map element continues since the delimiter,
                // skip the delimiter and continue the loop
                removePrefixTargetIndexPlus(delimiterIndex, _elementDelimiter.length());
                continue;
            }

            // e.g. value1} ; key2=value2}
            final String mapValue = substringRemainderScope(_currentRemainderIndex, endBraceIndex);
            registerToMap(currentMap, filterParsedMapKey(mapKey), filterParsedMapValue(mapValue));

            // analyzing map is over, so close and return.
            closeByEndBraceIndex(endBraceIndex);
            return;
        }
    }

    /**
     * Parse the current remainder string as list.
     * @param currentList The current list made by parse process. (NotNull)
     */
    protected void parseListString(List<Object> currentList) {
        while (true) {
            if (!prepareLoopBeginning()) {
                return;
            }

            // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
            // here, remainder string should starts with the value of the list
            // _/_/_/_/_/_/_/_/_/_/

            if (startsWithMapBeginBrace()) {
                removePrefixMapingBeginBrace();
                parseMapString(setupNestMap(currentList));
                if (closeAfterParseNestMapList()) {
                    return;
                }
                continue;
            }

            if (startsWithListBeginBrace()) {
                removePrefixListingBeginBrace();
                parseListString(setupNestList(currentList));
                if (closeAfterParseNestMapList()) {
                    return;
                }
                continue;
            }

            final int delimiterIndex = indexOfDelimiter();
            final int endBraceIndex = indexOfEndBrace();
            assertListStringEndBraceIndex(endBraceIndex, currentList);

            if (delimiterIndex >= 0 && delimiterIndex < endBraceIndex) { // delimiter exists
                // e.g. value1 ; value2 ; value3}
                final String listValue = substringRemainderFront(delimiterIndex);
                currentList.add(filterParsedListValue(listValue));

                // because the list element continues since the delimiter,
                // skip the delimiter and continue the loop.
                removePrefixTargetIndexPlus(delimiterIndex, _elementDelimiter.length());
                continue;
            }

            // e.g. value1}, value2, }
            final String listValue = substringRemainderFront(endBraceIndex);
            currentList.add(filterParsedListValue(listValue));

            // analyzing list is over, so close and return
            closeByEndBraceIndex(endBraceIndex);
            return;
        }
    }

    /**
     * prepare loop beginning.
     * @return Does it loop next?
     */
    protected boolean prepareLoopBeginning() {
        removePrefixAllDelimiter();
        if (isRemainderOver()) { // analyzing is over
            return false;
        }
        if (startsWithEndBrace()) { // analyzing current map is over
            removePrefixEndBrace();
            return false;
        }
        return true;
    }

    /**
     * Close after parse nest map list.
     * @return Is is closed?
     */
    protected boolean closeAfterParseNestMapList() {
        if (startsWithEndBrace()) {
            removePrefixEndBrace();
            return true;
        }
        return false;
    }

    /**
     * Close by end-brace index.
     * @param endBraceIndex The index of end-brace. (NotMinus)
     */
    protected void closeByEndBraceIndex(int endBraceIndex) {
        _currentRemainderIndex = endBraceIndex;
        removePrefixEndBrace();
    }

    // ===================================================================================
    //                                                               Index of Control Mark
    //                                                               =====================
    protected int indexOfStartBrace() {
        return findIndexOfControlMark(_beginBrace);
    }

    protected int indexOfEndBrace() {
        return findIndexOfControlMark(_endBrace);
    }

    protected int indexOfDelimiter() {
        return findIndexOfControlMark(_elementDelimiter);
    }

    protected int indexOfEqual() {
        return findIndexOfControlMark(_valueEqual);
    }

    protected int findIndexOfControlMark(String controlMark) {
        String current = _remainderString;
        if (isEscapeCharEscape()) {
            // performance headache but basically it does not come here
            // because of unsupported escaping escape-char
            final String escapedEscapeChar = convertToEscapedMark(_escapeChar);
            current = generally_replace(current, escapedEscapeChar, convertToLengthSpace(escapedEscapeChar));
        }
        int currentBeginIndex = _currentRemainderIndex;
        while (true) {
            final int index = current.indexOf(controlMark, currentBeginIndex);
            if (index < 0) { // not found
                return index;
            }
            if (index > 0) {
                final String lastChar = current.substring(index - 1, index);
                if (_escapeChar.equals(lastChar)) { // escaped
                    final int nextIndex = index + _escapeChar.length();
                    currentBeginIndex = nextIndex;
                    continue;
                }
            }
            return index; // found
        }
    }

    protected String convertToLengthSpace(String value) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            sb.append(" ");
        }
        return sb.toString();
    }

    // ===================================================================================
    //                                                       Remove Prefix of Control Mark
    //                                                       =============================
    /**
     * Remove prefix and all delimiters.
     */
    protected void removePrefixAllDelimiter() {
        ltrimRemainder();
        while (true) {
            if (!startsWithDelimiter()) {
                break;
            }
            if (startsWithDelimiter()) {
                removePrefixDelimiter();
                ltrimRemainder();
            }
        }
    }

    /**
     * Remove prefix map-prefix and begin-brace.
     */
    protected void removePrefixMapingBeginBrace() {
        doRemovePrefix(_mappingBeginBrace);
    }

    /**
     * Remove prefix list-prefix and begin-brace.
     */
    protected void removePrefixListingBeginBrace() {
        doRemovePrefix(_listingBeginBrace);
    }

    /**
     * Remove prefix end-brace.
     */
    protected void removePrefixEndBrace() {
        doRemovePrefix(_endBrace);
    }

    /**
     * Remove prefix delimiter.
     */
    protected void removePrefixDelimiter() {
        doRemovePrefix(_elementDelimiter);
    }

    /**
     * Remove prefix as mark.
     * @param prefixString The string for prefix. (NotNull)
     */
    protected void doRemovePrefix(String prefixString) {
        assertRemovingPrefix(prefixString);
        final int nonTrimmedIndex = indexOfRemainderNonTrimmedChar();
        _currentRemainderIndex = nonTrimmedIndex + prefixString.length();
    }

    /**
     * Remove prefix by the index and plus count.
     * @param index The base index. (NotMinus)
     * @param plusCount The plus count for index. (NotMinus)
     */
    protected void removePrefixTargetIndexPlus(int index, int plusCount) {
        _currentRemainderIndex = index + plusCount;
    }

    // ===================================================================================
    //                                                       Starts/Ends with Control Mark
    //                                                       =============================
    /**
     * Does it start with the map-prefix?
     * @return The determination, true or false.
     */
    protected boolean startsWithMapBeginBrace() {
        final int index = indexOfRemainderNonTrimmedChar();
        return index >= 0 && _remainderString.startsWith(_mappingBeginBrace, index);
    }

    /**
     * Does it start with the list-prefix?
     * @return The determination, true or false.
     */
    protected boolean startsWithListBeginBrace() {
        final int index = indexOfRemainderNonTrimmedChar();
        return index >= 0 && _remainderString.startsWith(_listingBeginBrace, index);
    }

    /**
     * Does it start with the delimiter?
     * @return The determination, true or false.
     */
    protected boolean startsWithDelimiter() {
        final int index = indexOfRemainderNonTrimmedChar();
        return index >= 0 && _remainderString.startsWith(_elementDelimiter, index);
    }

    /**
     * Does it start with end-brace?
     * @return The determination, true or false.
     */
    protected boolean startsWithEndBrace() {
        final int index = indexOfRemainderNonTrimmedChar();
        return index >= 0 && _remainderString.startsWith(_endBrace, index);
    }

    protected boolean startsWithPrefix(String prefix) {
        final int index = indexOfRemainderNonTrimmedChar();
        return index >= 0 && _remainderString.startsWith(prefix, index);
    }

    /**
     * Does it end with end-brace?
     * @return The determination, true or false.
     */
    protected boolean endsWithEndBrace() {
        return generally_endsWith(_remainderString, _endBrace);
    }

    // ===================================================================================
    //                                                                          Map Object
    //                                                                          ==========
    // -----------------------------------------------------
    //                                              Register
    //                                              --------
    protected void registerToMap(Map<String, Object> currentMap, String key, Object element) {
        if (isCheckDuplicateEntry()) {
            assertDuplicateEntryInMap(currentMap, key, element);
        }
        currentMap.put(key, element);
    }

    protected void assertDuplicateEntryInMap(Map<String, Object> currentMap, String key, Object element) {
        if (currentMap.containsKey(key)) {
            throwDfMapDuplicateEntryException(currentMap, key, element);
        }
    }

    protected void throwDfMapDuplicateEntryException(Map<String, Object> currentMap, String key, Object element) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Duplicate entry in the map string.");
        br.addItem("Already Registered Entry");
        br.addElement(key);
        br.addElement(currentMap.get(key));
        br.addItem("New Duplicate Entry");
        br.addElement(key);
        if (element instanceof Map<?, ?>) {
            // might be lazy registered so might be empty (then not show)
            final Map<?, ?> map = (Map<?, ?>) element;
            if (!map.isEmpty()) {
                br.addElement(map);
            }
        } else {
            br.addElement(element);
        }
        setupMessageRemainderString(br);
        final String msg = br.buildExceptionMessage();
        throw new DfMapDuplicateEntryException(msg);
    }

    // -----------------------------------------------------
    //                                                Set up
    //                                                ------
    /**
     * Set up new-created nest map as element of the current map.
     * @param currentMap the current map to set up. (NotNull)
     * @param mapKey The key of nest map. (NotNull)
     * @return The new-created nest map. (NotNull)
     */
    protected Map<String, Object> setupNestMap(Map<String, Object> currentMap, String mapKey) {
        final Map<String, Object> nestMap = newStringObjectMap();
        registerToMap(currentMap, filterParsedMapKey(mapKey), nestMap);
        return nestMap;
    }

    /**
     * Set up new-created nest map as element of the current list.
     * @param currentList the current list to set up. (NotNull)
     * @return The new-created nest map. (NotNull)
     */
    protected Map<String, Object> setupNestMap(List<Object> currentList) {
        final Map<String, Object> nestMap = newStringObjectMap();
        currentList.add(nestMap);
        return nestMap;
    }

    /**
     * Set up new-created nest list as element of the current map.
     * @param currentMap the current map to set up. (NotNull)
     * @param mapKey The key of nest map. (NotNull)
     * @return The new-created nest list. (NotNull)
     */
    protected List<Object> setupNestList(Map<String, Object> currentMap, String mapKey) {
        final List<Object> nestList = newObjectList();
        registerToMap(currentMap, filterParsedMapKey(mapKey), nestList);
        return nestList;
    }

    /**
     * Set up new-created nest list as element of the current list.
     * @param currentList the current map to set up. (NotNull)
     * @return The new-created nest list. (NotNull)
     */
    protected List<Object> setupNestList(List<Object> currentList) {
        final List<Object> nestList = newObjectList();
        currentList.add(nestList);
        return nestList;
    }

    // -----------------------------------------------------
    //                                                Filter
    //                                                ------
    protected String filterParsedMapKey(String value) {
        if (value == null) {
            return null;
        }
        final String unescaped = unescapeControlMarkAsMap(value.trim());
        return doFilterParsedMapListNullExpression(unescaped);
    }

    protected String filterParsedMapValue(String value) {
        if (value == null) {
            return null;
        }
        final String unescaped = unescapeControlMarkAsMap(value.trim());
        return doFilterParsedMapListNullExpression(unescaped);
    }

    protected String filterParsedListValue(String value) {
        if (value == null) {
            return null;
        }
        final String unescaped = unescapeControlMarkAsList(value.trim());
        return doFilterParsedMapListNullExpression(unescaped);
    }

    protected String doFilterParsedMapListNullExpression(String filtered) {
        return ("".equals(filtered) || "null".equals(filtered)) ? null : filtered;
    }

    // -----------------------------------------------------
    //                                     Map/List Instance
    //                                     -----------------
    /**
     * New string-object map for map:{} of this style.
     * @return The new-created map. (NotNull)
     */
    public Map<String, Object> newStringObjectMap() { // public for client
        return new LinkedHashMap<String, Object>();
    }

    /**
     * New object-type list for list:{} of this style.
     * @return The new-created list. (NotNull)
     */
    public List<Object> newObjectList() { // public for client
        return new ArrayList<Object>();
    }

    // ===================================================================================
    //                                                                         Escape Mark
    //                                                                         ===========
    // -----------------------------------------------------
    //                                                Escape
    //                                                ------
    // escape is for e.g. toMapString(), toListString()
    /**
     * Escape control marks as plain string in the map key and value.
     * @param value The value, might contain control marks. (NullAllowed: if null, return null)
     * @return The escaped string of the value. (NullAllowed: when the value is null)
     */
    public String escapeControlMarkAsMap(Object value) { // public for tools
        return doEscapeControlMark(value, /*ignoreEqual*/false);
    }

    /**
     * Escape control marks as plain string in the list value.
     * @param value The value, might contain control marks. (NullAllowed: if null, return null)
     * @return The escaped string of the value. (NullAllowed: when the value is null)
     */
    public String escapeControlMarkAsList(Object value) { // public for tools
        return doEscapeControlMark(value, isIgnoreEqualAsEscapeControlMarkInList());
    }

    protected boolean isIgnoreEqualAsEscapeControlMarkInList() { // for e.g. toMapString(), toListString()
        // now the equal "=" in list value, e.g. list:{ sea => land }, is escaped in spite of unnecessary
        // I want to fix it but keep compatible to avoid rare case trouble so no change with extension point
        // (and also other abstractions exist in other case...)
        //
        // so all control marks are escaped in both map and list (is specification of MapStyle)
        // and small abstractions (receiving scope is wide) are allowed like this:
        //  o list:{ sea => land }, list:{ sea \=> land } are parsed as list:{ sea => land }
        //  o map:{key1=value1=value2} are parsed as map:{ key1 = value1=value2 }
        return false;
    }

    protected String doEscapeControlMark(Object value, boolean ignoreEqual) {
        if (value == null) {
            return null;
        }
        String filtered = value.toString();
        if (isEscapeCharEscape()) {
            filtered = generally_replace(filtered, _escapeChar, convertToEscapedMark(_escapeChar));
        }
        filtered = generally_replace(filtered, _beginBrace, convertToEscapedMark(_beginBrace));
        filtered = generally_replace(filtered, _endBrace, convertToEscapedMark(_endBrace));
        filtered = generally_replace(filtered, _elementDelimiter, convertToEscapedMark(_elementDelimiter));
        if (!ignoreEqual) {
            filtered = generally_replace(filtered, _valueEqual, convertToEscapedMark(_valueEqual));
        }
        return filtered;
    }

    // -----------------------------------------------------
    //                                              Unescape
    //                                              --------
    // unescape is for e.g. fromMapString(), fromListString()
    /**
     * Unescape control marks as plain string in the map key and value.
     * @param value The value, might contain escaped control marks. (NullAllowed: if null, return null)
     * @return The unescaped string of the value. (NullAllowed: when the value is null)
     */
    public String unescapeControlMarkAsMap(String value) { // public for tools
        return doUnescapeControlMark(value, /*ignoreEqual*/false);
    }

    /**
     * Unescape control marks as plain string in the list value.
     * @param value The value, might contain escaped control marks. (NullAllowed: if null, return null)
     * @return The unescaped string of the value. (NullAllowed: when the value is null)
     */
    public String unescapeControlMarkAsList(String value) { // public for tools
        return doUnescapeControlMark(value, isIgnoreEqualAsUnescapeControlMarkInList());
    }

    protected boolean isIgnoreEqualAsUnescapeControlMarkInList() { // for e.g. fromMapString(), fromListString()
        return false; // same reason as isIgnoreEqualAsEscapeControlMarkInList()
    }

    protected String doUnescapeControlMark(String value, boolean ignoreEqual) {
        if (value == null) {
            return null;
        }
        String filtered = value;
        final String escapedEscapeMark = ESCAPED_ESCAPE_MARK;
        if (isEscapeCharEscape()) {
            filtered = generally_replace(filtered, convertToEscapedMark(_escapeChar), escapedEscapeMark);
        }
        filtered = generally_replace(filtered, convertToEscapedMark(_beginBrace), _beginBrace);
        filtered = generally_replace(filtered, convertToEscapedMark(_endBrace), _endBrace);
        filtered = generally_replace(filtered, convertToEscapedMark(_elementDelimiter), _elementDelimiter);
        if (!ignoreEqual) {
            filtered = generally_replace(filtered, convertToEscapedMark(_valueEqual), _valueEqual);
        }
        if (isEscapeCharEscape()) {
            filtered = generally_replace(filtered, escapedEscapeMark, _escapeChar);
        }
        return filtered;
    }

    // -----------------------------------------------------
    //                                          Assist Logic
    //                                          ------------
    protected String convertToEscapedMark(String mark) {
        return _escapeChar + mark;
    }

    protected boolean isEscapeCharEscape() {
        // escape for escape char is unsupported (unneeded)
        // so fixedly returns false
        //
        // compatibility is treated as important here
        //  o "\\n = \n" in convertValueMap.dataprop can directly work
        //  o plain "\" is can directly work
        //
        // [specification]
        // escape char without control mark is treated as plain value
        //  e.g. "a\b\c"
        // 
        // previous escape char of control mark is always treated as escape char
        //  e.g. "\;"
        //
        // if any spaces between the escape char and the control mark exist,
        //  the escape char is plain value, e.g. "\ ;"
        //
        return false;
    }

    // ===================================================================================
    //                                                                      Parsing Assert
    //                                                                      ==============
    // -----------------------------------------------------
    //                                       First MapString
    //                                       ---------------
    protected void assertFirstMapString() {
        if (_remainderString == null) {
            throwDfMapStringBasicFailureException("The map-string should not be null.");
        }
        if (!startsWithMapBeginBrace()) {
            throwDfMapStringBasicFailureException("The map-string should start with '" + _mappingBeginBrace + "'.");
        }
        if (!endsWithEndBrace()) {
            throwDfMapStringBasicFailureException("The map-string should end with '" + _endBrace + "'.");
        }
        // #for_now difficult to count non-escaped marks without performance cost (also thinking "map:" in value) by jflute (2018/05/05)
        //final int beginBraceCount = countControlMarkInRemainder(_beginBrace);
        //final int endBraceCount = countControlMarkInRemainder(_endBrace);
        //if (beginBraceCount != endBraceCount) {
        //    throwDfMapStringDifferentCountBracesException(beginBraceCount, endBraceCount);
        //}
    }

    protected void throwDfMapStringBasicFailureException(String notice) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice(notice);
        setupMessageRemainderString(br);
        final String msg = br.buildExceptionMessage();
        throw new DfMapParseFailureException(msg);
    }

    protected void throwDfMapStringDifferentCountBracesException(int beginCount, int endCount) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Different count between begin braces and end braces.");
        br.addItem("Advice");
        br.addElement("Make sure braces on your map-string.");
        br.addElement("For example:");
        br.addElement("  (x): map:{ sea = ");
        br.addElement("  (x): map:{ sea = map:{ }");
        br.addElement("  (o): map:{ sea = land }");
        br.addElement("  (o): map:{ sea = map:{ land = piari } }");
        br.addElement("");
        br.addElement("While, map-string can escape control marks");
        br.addElement("so pay attention to last char of value like this:");
        br.addElement("  (x): map:{ sea = C:\\land\\piari\\}  // last '}' escaped by escape char");
        br.addElement("  (o): map:{ sea = C:\\land\\piari\\ } // space helps you at the case");
        br.addItem("Brace Count");
        br.addElement("begin: " + beginCount);
        br.addElement("end: " + endCount);
        setupMessageRemainderString(br);
        final String msg = br.buildExceptionMessage();
        throw new DfMapParseFailureException(msg);
    }

    // -----------------------------------------------------
    //                                      First ListString
    //                                      ----------------
    protected void assertFirstListString() {
        if (_remainderString == null) {
            throwListStringBasicFailureException("The list string should not be null.");
        }
        if (!startsWithListBeginBrace()) {
            throwListStringBasicFailureException("The list string should start with '" + _listingBeginBrace + "'.");
        }
        if (!endsWithEndBrace()) {
            throwListStringBasicFailureException("The list string should end with '" + _endBrace + "'.");
        }
        // #for_now difficult to count non-escaped marks without performance cost (also thinking "map:" in value) by jflute (2018/05/05)
        //final int startBraceCount = countControlMarkInRemainder(_beginBrace);
        //final int endBraceCount = countControlMarkInRemainder(_endBrace);
        //if (startBraceCount != endBraceCount) {
        //    throwDfListStringDifferentCountBracesException(startBraceCount, endBraceCount);
        //}
    }

    protected void throwListStringBasicFailureException(String notice) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice(notice);
        setupMessageRemainderString(br);
        final String msg = br.buildExceptionMessage();
        throw new DfMapParseFailureException(msg);
    }

    protected void throwDfListStringDifferentCountBracesException(int beginCount, int endCount) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Different count between begin braces and end braces.");
        br.addItem("Advice");
        br.addElement("Make sure braces on your list-string.");
        br.addElement("For example:");
        br.addElement("  (x): list:{ sea");
        br.addElement("  (x): list:{ sea ; map:{ }");
        br.addElement("  (o): list:{ sea ; land }");
        br.addElement("  (o): list:{ sea ; map:{ land = piari } }");
        br.addElement("");
        br.addElement("While, list-string can escape control marks");
        br.addElement("so pay attention to last char of value like this:");
        br.addElement("  (x): list:{ sea = C:\\land\\piari\\}  // last '}' escaped by escape char");
        br.addElement("  (o): list:{ sea = C:\\land\\piari\\ } // space helps you at the case");
        br.addItem("Brace Count");
        br.addElement("begin: " + beginCount);
        br.addElement("end: " + endCount);
        setupMessageRemainderString(br);
        final String msg = br.buildExceptionMessage();
        throw new DfMapParseFailureException(msg);
    }

    // #hope count non-escaped mark here without performance cost by jflute (2018/05/05)
    //protected int countControlMarkInRemainder(String controlMark) { // only used in exception so don't mind performance
    //    final String remainder = dangrous_substringRemainderString(_currentRemainderIndex);
    //    return Srl.count(remainder, controlMark); // expects tuning of Srl in the future
    //}

    // -----------------------------------------------------
    //                                      ValueEqual Index
    //                                      ----------------
    protected void assertMapStringEqualIndex(int valueEqualIndex, Map<String, Object> currentMap) {
        if (_remainderString == null) { // basically no way here, just in case
            final String notice = "The remainderString should not be null:";
            throwDfMapValueEqualFailureException(notice, valueEqualIndex, currentMap);
        }
        if (valueEqualIndex < 0) {
            final String notice = "Not found the value-equal mark in the map-string.";
            throwDfMapValueEqualFailureException(notice, valueEqualIndex, currentMap);
        }
        // following checks are for internal mistake
        final int logicalLength = lengthRemainder();
        if (logicalLength < (_remainderString.length() - valueEqualIndex)) { // deep (or unneeded?) check (written by younger jflute)
            final String notice = "The remainderString length should be greater than equalIndex:";
            throwDfMapValueEqualFailureException(notice, valueEqualIndex, currentMap);
        }
        final String extractedMark = substringRemainderScope(valueEqualIndex, valueEqualIndex + _valueEqual.length());
        if (!extractedMark.equals(_valueEqual)) { // different value-equal mark
            final String notice = "The remainderString should have value-equal mark at value-equal index:";
            throwDfMapValueEqualFailureException(notice, valueEqualIndex, currentMap);
        }
    }

    protected void throwDfMapValueEqualFailureException(String notice, int valueEqualIndex, Map<String, Object> currentMap) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice(notice);
        br.addItem("ValueEqual Index");
        br.addElement(valueEqualIndex);
        br.addItem("Making Map");
        br.addElement(currentMap);
        setupMessageRemainderString(br);
        final String msg = br.buildExceptionMessage();
        throw new DfMapParseFailureException(msg);
    }

    // -----------------------------------------------------
    //                                        EndBrace Index
    //                                        --------------
    protected void assertMapStringEndBraceIndex(int endBraceIndex, Map<String, Object> makingMap) {
        if (endBraceIndex < 0) {
            final String notice = "Not found the end-brace in the map-string.";
            throwDfMapStringEndBraceFailureException(notice, endBraceIndex, makingMap);
        }
        // following checks are for internal mistake
        if (_remainderString == null) {
            final String notice = "The remainderString should not be null:";
            throwDfMapStringEndBraceFailureException(notice, endBraceIndex, makingMap);
        }
        final int logicalLength = lengthRemainder();
        if (logicalLength < (_remainderString.length() - endBraceIndex)) {
            final String notice = "The remainderString length should be greater than end-brace index:";
            throwDfMapStringEndBraceFailureException(notice, endBraceIndex, makingMap);
        }
        substringRemainderScope(endBraceIndex, endBraceIndex + _endBrace.length());
        final String extractedMark = substringRemainderScope(endBraceIndex, endBraceIndex + _endBrace.length());
        if (!extractedMark.equals(_endBrace)) {
            final String notice = "The remainderString should have end-brace at the end-brace index:";
            throwDfMapStringEndBraceFailureException(notice, endBraceIndex, makingMap);
        }
    }

    protected void throwDfMapStringEndBraceFailureException(String notice, int endBraceIndex, Map<String, Object> currentMap) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice(notice);
        br.addItem("EndBrace Index");
        br.addElement(endBraceIndex);
        br.addItem("Making Map");
        br.addElement(currentMap);
        setupMessageRemainderString(br);
        final String msg = br.buildExceptionMessage();
        throw new DfMapParseFailureException(msg);
    }

    protected void assertListStringEndBraceIndex(int endBraceIndex, List<?> makingList) {
        if (endBraceIndex < 0) {
            final String notice = "Not found the end brace.";
            throwDfListStringEndBraceFailureException(notice, endBraceIndex, makingList);
        }
        // following checks are for internal mistake
        if (_remainderString == null) {
            final String notice = "The remainderString should not be null:";
            throwDfListStringEndBraceFailureException(notice, endBraceIndex, makingList);
        }
        final int logicalLength = lengthRemainder();
        if (logicalLength < (_remainderString.length() - endBraceIndex)) {
            final String notice = "The remainderString length should be greater than end-brace index:";
            throwDfListStringEndBraceFailureException(notice, endBraceIndex, makingList);
        }
        final String extractedMark = substringRemainderScope(endBraceIndex, endBraceIndex + _endBrace.length());
        if (!extractedMark.equals(_endBrace)) {
            final String notice = "The remainderString should have end-brace at the end-brace index:";
            throwDfListStringEndBraceFailureException(notice, endBraceIndex, makingList);
        }
    }

    protected void throwDfListStringEndBraceFailureException(String notice, int endBraceIndex, List<?> makingList) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice(notice);
        br.addItem("EndBrace Index");
        br.addElement(endBraceIndex);
        br.addItem("Making List");
        br.addElement(makingList);
        setupMessageRemainderString(br);
        final String msg = br.buildExceptionMessage();
        throw new DfMapParseFailureException(msg);
    }

    // -----------------------------------------------------
    //                                       Removing Prefix
    //                                       ---------------
    protected void assertRemovingPrefix(String prefixString) {
        if (_remainderString == null) {
            final String notice = "The remainderString should not be null.";
            throwDfMapRemovingPrefixFailureException(notice, prefixString);
        }
        if (prefixString == null) {
            final String notice = "The prefixString should not be null.";
            throwDfMapRemovingPrefixFailureException(notice, prefixString);
        }
        if (!startsWithPrefix(prefixString)) {
            final String notice = "The remainderString shuold start with the prefixString.";
            throwDfMapRemovingPrefixFailureException(notice, prefixString);
        }
    }

    protected void throwDfMapRemovingPrefixFailureException(String notice, String prefixString) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice(notice);
        br.addItem("Prefix String");
        br.addElement(prefixString);
        setupMessageRemainderString(br);
        final String msg = br.buildExceptionMessage();
        throw new DfMapParseFailureException(msg);
    }

    // -----------------------------------------------------
    //                                        Last Assertion
    //                                        --------------
    protected void assertLastRemainderAsMap(Map<String, Object> generatedMap) {
        if (indexOfRemainderNonTrimmedChar() >= 0) {
            throwDfMapStringUnneededStringFoundException(generatedMap);
        }
    }

    protected void throwDfMapStringUnneededStringFoundException(Map<String, Object> generatedMap) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The last remainderString as map should be empty string but ...");
        br.addItem("Generated Map");
        br.addElement(generatedMap);
        setupMessageRemainderString(br);
        final String msg = br.buildExceptionMessage();
        throw new DfMapParseFailureException(msg);
    }

    protected void assertLastRemainderAsList(List<Object> generatedList) {
        if (indexOfRemainderNonTrimmedChar() >= 0) {
            throwDfListStringUnneededStringFoundException(generatedList);
        }
    }

    protected void throwDfListStringUnneededStringFoundException(List<Object> generatedList) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The last remainderString as list should be empty string but ...");
        br.addItem("Generated List");
        br.addElement(generatedList);
        setupMessageRemainderString(br);
        final String msg = br.buildExceptionMessage();
        throw new DfMapParseFailureException(msg);
    }

    // -----------------------------------------------------
    //                                       Message Pattern
    //                                       ---------------
    protected void setupMessageRemainderString(ExceptionMessageBuilder br) {
        br.addItem("Remainder String");
        br.addElement(_remainderString);
        br.addItem("Current Remainder Index");
        if (_remainderString != null) { // checking just in case
            br.addElement(_currentRemainderIndex + " (max:" + _remainderString.length() + ")");
            if (_currentRemainderIndex >= 0 && _remainderString.length() >= _currentRemainderIndex) { // more check
                br.addElement(cut(_remainderString.substring(_currentRemainderIndex), 30, "..."));
            }
        } else {
            br.addElement(_currentRemainderIndex);
        }
        br.addItem("Defined Control Mark");
        br.addElement(_mapPrefix + " " + _listPrefix + " " + _beginBrace + " " + _endBrace + " " + _elementDelimiter + " " + _valueEqual);
    }

    protected String cut(String str, int length, String suffix) {
        assertObjectNotNull("str", str);
        return str.length() > length ? (str.substring(0, length) + (suffix != null ? suffix : "")) : str;
    }

    // ===================================================================================
    //                                                                    Remainder String
    //                                                                    ================
    protected void beginRemainder(String mapString) {
        _remainderString = mapString;
        _currentRemainderIndex = 0;
    }

    protected void endRemainder() {
        _remainderString = null;
        _currentRemainderIndex = 0;
    }

    protected int indexOfRemainderNonTrimmedChar() {
        return generally_indexOfNonTrimmedChar(_remainderString, _currentRemainderIndex);
    }

    protected boolean isRemainderOver() {
        if (_remainderString.length() == _currentRemainderIndex) { // may be almost here
            return true;
        }
        if (indexOfRemainderNonTrimmedChar() < 0) { // with loop so also check just-over before this
            return true;
        }
        return false;
    }

    protected int lengthRemainder() {
        return _remainderString.length() - _currentRemainderIndex;
    }

    protected void ltrimRemainder() {
        final int index = indexOfRemainderNonTrimmedChar();
        if (index > 0) {
            _currentRemainderIndex = index;
        }
    }

    protected String substringRemainderFront(int endIndex) {
        return _remainderString.substring(_currentRemainderIndex, endIndex);
    }

    protected String substringRemainderScope(int beginIndex, int endIndex) {
        return _remainderString.substring(beginIndex, endIndex);
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    // no use of instance variable (can be static, but non-static for overriding)
    protected String generally_replace(String str, String fromStr, String toStr) {
        StringBuilder sb = null; // lazy load
        int pos = 0;
        int pos2 = 0;
        do {
            pos = str.indexOf(fromStr, pos2);
            if (pos2 == 0 && pos < 0) { // first loop and not found
                return str; // without creating StringBuilder 
            }
            if (sb == null) {
                sb = new StringBuilder();
            }
            if (pos == 0) {
                sb.append(toStr);
                pos2 = fromStr.length();
            } else if (pos > 0) {
                sb.append(str.substring(pos2, pos));
                sb.append(toStr);
                pos2 = pos + fromStr.length();
            } else { // (pos < 0) second or after loop only
                sb.append(str.substring(pos2));
                return sb.toString();
            }
        } while (true);
    }

    protected boolean generally_endsWith(String str, String suffix) {
        int remainderIndex = generally_lastIndexOfNonTrimmedChar(str);
        for (int i = suffix.length() - 1; i >= 0; i--, remainderIndex--) {
            final char branceCh = suffix.charAt(i);
            final char remainderCh = str.charAt(remainderIndex);
            if (branceCh != remainderCh) {
                return false;
            }
        }
        return true;
    }

    protected int generally_indexOfNonTrimmedChar(String str, int offsetIndex) {
        for (int index = offsetIndex; index < str.length(); index++) {
            final char ch = str.charAt(index);
            if (ch > ' ') {
                return index;
            }
        }
        return -1;
    }

    protected int generally_lastIndexOfNonTrimmedChar(String str) {
        for (int index = str.length() - 1; index >= 0; index--) {
            final char ch = str.charAt(index);
            if (ch > ' ') {
                return index;
            }
        }
        return -1;
    }

    protected String generally_calculateNextIndent(String previousIndent, String currentIndent) {
        final StringBuilder sb = new StringBuilder();
        sb.append(currentIndent);
        final int indentLength = currentIndent.length() - previousIndent.length();
        for (int i = 0; i < indentLength; i++) {
            sb.append(" ");
        }
        return sb.toString();
    }

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

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public boolean isCheckDuplicateEntry() {
        return _checkDuplicateEntry;
    }

    public boolean isPrintOneLiner() {
        return _printOneLiner;
    }

    public boolean isWithoutDisplaySideSpace() {
        return _withoutDisplaySideSpace;
    }
}