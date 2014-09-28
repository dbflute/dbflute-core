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
package org.seasar.dbflute.helper.mapstring;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.seasar.dbflute.exception.MapListStringDuplicateEntryException;
import org.seasar.dbflute.exception.MapListStringParseFailureException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;

/**
 * The string for map and list.
 * <pre>
 * e.g. map-string
 *   map:{key1=value1,key2=list:{value21,value22,value23},key3=map:{key31=value31}}
 * 
 * e.g. list-string
 *   list:{key1=value1,key2=list:{value21,value22,value23},key3=map:{key31=value31}}
 * </pre>
 * @author jflute
 */
public class MapListString {

    // this code was written when jflute was very young
    // (the code has small modifications only after created)

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The default mark of map value. */
    public static final String DEFAULT_MAP_MARK = "map:";

    /** The default mark of list value. */
    public static final String DEFAULT_LIST_MARK = "list:";

    /** The default control mark of start-brace. */
    public static final String DEFAULT_START_BRACE = "{";

    /** The default control mark of end-brace. */
    public static final String DEFAULT_END_BRACE = "}";

    /** The default control mark of delimiter. */
    public static final String DEFAULT_DELIMITER = ";";

    /** The default control mark of equal. */
    public static final String DEFAULT_EQUAL = "=";

    /** The escape character for control marks. */
    protected static final String ESCAPE_CHAR = "\\";

    /** The temporary mark of escaped escape character. */
    protected static final String ESCAPED_ESCAPE_MARK = "$$df:escapedEscape$$";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                             Component
    //                                             ---------
    /** The mark of map. (NotNull: but changeable) */
    protected String _mapMark;

    /** The mark of list. (NotNull: but changeable) */
    protected String _listMark;

    /** The control mark of start brace. (NotNull: but changeable) */
    protected String _startBrace;

    /** The control mark of end brace. (NotNull: but changeable) */
    protected String _endBrace;

    /** The control mark of delimiter. (NotNull: but changeable) */
    protected String _delimiter;

    /** The control mark of equal for map-string. (NotNull: but changeable) */
    protected String _equal;

    /** The escape character for control marks. (NotNull) */
    protected final String _escapeChar;

    // -----------------------------------------------------
    //                                             Temporary
    //                                             ---------
    /** The string of top (full) string as temporary variable for generation. (NullAllowed: depends on process) */
    protected String _topString;

    /** The string of remainder as temporary variable for generation. (NullAllowed: depends on process) */
    protected String _remainderString;

    // -----------------------------------------------------
    //                                                Option
    //                                                ------
    /** Does it check duplicate entry of map? */
    protected boolean _checkDuplicateEntry;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * Constructor setting as default. <br />
     * You can change marks by setters after creation.
     */
    public MapListString() {
        _mapMark = DEFAULT_MAP_MARK;
        _listMark = DEFAULT_LIST_MARK;
        _startBrace = DEFAULT_START_BRACE;
        _endBrace = DEFAULT_END_BRACE;
        _delimiter = DEFAULT_DELIMITER;
        _equal = DEFAULT_EQUAL;
        _escapeChar = ESCAPE_CHAR; // fixed for now
    }

    // ===================================================================================
    //                                                                        Build String
    //                                                                        ============
    /**
     * Build map-string from the map object.
     * @param map The map object that has string keys. (NotNull)
     * @return The string as map expression. (NotNull)
     */
    public String buildMapString(Map<String, ? extends Object> map) {
        final StringBuilder sb = new StringBuilder();
        @SuppressWarnings("unchecked")
        final Map<String, Object> casted = (Map<String, Object>) map;
        doBuildMapString(sb, casted, "", "    ");
        return sb.toString();
    }

    protected void doBuildMapString(StringBuilder sb, Map<String, Object> map, String preIndent, String curIndent) {
        sb.append(_mapMark).append(_startBrace);
        final Set<Entry<String, Object>> entrySet = map.entrySet();
        for (Entry<String, ? extends Object> entry : entrySet) {
            final String key = entry.getKey();
            final Object value = entry.getValue();
            sb.append(ln()).append(curIndent).append(_delimiter);
            sb.append(" ").append(escapeControlMark(key)).append(" ").append(_equal).append(" ");
            if (value instanceof Map<?, ?>) {
                @SuppressWarnings("unchecked")
                final Map<String, Object> valueMap = (Map<String, Object>) value;
                doBuildMapString(sb, valueMap, curIndent, calculateNextIndent(preIndent, curIndent));
            } else if (value instanceof List<?>) {
                @SuppressWarnings("unchecked")
                final List<Object> valueList = (List<Object>) value;
                doBuildListString(sb, valueList, curIndent, calculateNextIndent(preIndent, curIndent));
            } else {
                sb.append(escapeControlMark(value));
            }
        }
        sb.append(ln()).append(preIndent).append(_endBrace);
    }

    /**
     * Build list-string from the list object.
     * @param list The list object that has object elements. (NotNull)
     * @return The string as list expression. (NotNull)
     */
    public String buildListString(List<? extends Object> list) {
        final StringBuilder sb = new StringBuilder();
        @SuppressWarnings("unchecked")
        final List<Object> casted = (List<Object>) list;
        doBuildListString(sb, casted, "", "    ");
        return sb.toString();
    }

    protected void doBuildListString(StringBuilder sb, List<? extends Object> list, String preIndent, String curIndent) {
        sb.append(_listMark).append(_startBrace);
        for (Object value : list) {
            sb.append(ln()).append(curIndent).append(_delimiter);
            sb.append(" ");
            if (value instanceof Map<?, ?>) {
                @SuppressWarnings("unchecked")
                final Map<String, Object> valueMap = (Map<String, Object>) value;
                doBuildMapString(sb, valueMap, curIndent, calculateNextIndent(preIndent, curIndent));
            } else if (value instanceof List<?>) {
                @SuppressWarnings("unchecked")
                final List<Object> valueList = (List<Object>) value;
                doBuildListString(sb, valueList, curIndent, calculateNextIndent(preIndent, curIndent));
            } else {
                sb.append(escapeControlMark(value));
            }
        }
        sb.append(ln()).append(preIndent).append(_endBrace);
    }

    protected String calculateNextIndent(String preIndent, String curIndent) {
        final StringBuilder sb = new StringBuilder();
        final int indentLength = curIndent.length() - preIndent.length();
        for (int i = 0; i < indentLength; i++) {
            sb.append(" ");
        }
        return curIndent + sb.toString();
    }

    // ===================================================================================
    //                                                                     Generate Object
    //                                                                     ===============
    /**
     * Generate map object from the map-string.
     * @param mapString The string as map expression. (NotNull)
     * @return The generated map. (NotNull)
     */
    public Map<String, Object> generateMap(String mapString) {
        assertMapString(mapString);

        _topString = mapString;
        _remainderString = mapString;

        removeBothSideSpaceAndTabAndNewLine();
        removePrefixMapMarkAndStartBrace();

        final Map<String, Object> generatedMap = newStringObjectMap();
        parseRemainderMapString(generatedMap);
        if (!"".equals(_remainderString)) {
            throwMapStringUnneededStringFoundException(mapString, generatedMap);
        }
        return generatedMap;
    }

    protected void throwMapStringUnneededStringFoundException(String mapString, Map<String, Object> generatedMap) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Final remainderString should be empty string but ...");
        br.addItem("Remainder String");
        br.addElement(_remainderString);
        br.addItem("Map String");
        br.addElement(mapString);
        br.addItem("Generated Map");
        br.addElement(generatedMap);
        prepareControlMarkMessage(br);
        final String msg = br.buildExceptionMessage();
        throw new MapListStringParseFailureException(msg);
    }

    /**
     * Generate map object from list-string.
     * @param listString The string as list expression. (NotNull)
     * @return The generated list. (NotNull)
     */
    public List<Object> generateList(String listString) {
        assertListString(listString);

        _topString = listString;
        _remainderString = listString;

        removeBothSideSpaceAndTabAndNewLine();
        removePrefixListMarkAndStartBrace();

        final List<Object> generatedList = newObjectList();
        parseRemainderListString(generatedList);
        if (!"".equals(_remainderString)) {
            throwListStringUnneededStringFoundException(listString, generatedList);
        }
        return generatedList;
    }

    protected void throwListStringUnneededStringFoundException(String listString, List<Object> generatedList) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Final remainderString should be empty string but ...");
        br.addItem("Remainder String");
        br.addElement(_remainderString);
        br.addItem("List String");
        br.addElement(listString);
        br.addItem("Generated List");
        br.addElement(generatedList);
        prepareControlMarkMessage(br);
        final String msg = br.buildExceptionMessage();
        throw new MapListStringParseFailureException(msg);
    }

    // ===================================================================================
    //                                                                               Parse
    //                                                                               =====
    /**
     * Parse the current remainder string as map.
     * @param currentMap The current map made by parse process. (NotNull)
     */
    protected void parseRemainderMapString(final Map<String, Object> currentMap) {
        while (true) {
            if (initializeAtLoopBeginning()) {
                return;
            }

            // *** now, _remainderString should starts with the key of the map ***

            final int equalIndex = indexOfEqual();
            assertMapStringEqualIndex(_remainderString, equalIndex, _topString, currentMap);
            final String mapKey = _remainderString.substring(0, equalIndex).trim();
            removePrefixTargetIndexPlus(equalIndex, _equal.length());
            removeBothSideSpaceAndTabAndNewLine();

            // *** now, _remainderString should starts with the value of the map ***

            if (isStartsWithMapPrefix(_remainderString)) {
                removePrefixMapMarkAndStartBrace();
                parseRemainderMapString(setupNestMap(currentMap, mapKey));
                if (closeAfterParseNestMapList()) {
                    return;
                }
                continue;
            }

            if (isStartsWithListPrefix(_remainderString)) {
                removePrefixListMarkAndStartBrace();
                parseRemainderListString(setupNestList(currentMap, mapKey));
                if (closeAfterParseNestMapList()) {
                    return;
                }
                continue;
            }

            final int delimiterIndex = indexOfDelimiter();
            final int endBraceIndex = indexOfEndBrace();
            assertMapStringEndBraceIndex(_remainderString, endBraceIndex, _topString, currentMap);

            if (delimiterIndex >= 0 && delimiterIndex < endBraceIndex) { // delimiter exists
                // e.g. value1 ; key2=value2}
                final String mapValue = _remainderString.substring(0, delimiterIndex);
                registerToMap(currentMap, filterMapListKey(mapKey), filterMapListValue(mapValue));

                // because the map element continues since the delimiter,
                // skip the delimiter and continue the loop
                removePrefixTargetIndexPlus(delimiterIndex, _delimiter.length());
                continue;
            }

            // e.g. value1} ; key2=value2}
            final String mapValue = _remainderString.substring(0, endBraceIndex);
            registerToMap(currentMap, filterMapListKey(mapKey), filterMapListValue(mapValue));

            // analyzing map is over, so close and return.
            closeByEndBraceIndex(endBraceIndex);
            return;
        }
    }

    /**
     * Parse remainder list string.
     * @param currentList current list.
     */
    protected void parseRemainderListString(final List<Object> currentList) {
        while (true) {
            if (initializeAtLoopBeginning()) {
                return;
            }

            // *** now, _remainderString should starts with the value of the list ***

            if (isStartsWithMapPrefix(_remainderString)) {
                removePrefixMapMarkAndStartBrace();
                parseRemainderMapString(setupNestMap(currentList));
                if (closeAfterParseNestMapList()) {
                    return;
                }
                continue;
            }

            if (isStartsWithListPrefix(_remainderString)) {
                removePrefixListMarkAndStartBrace();
                parseRemainderListString(setupNestList(currentList));
                if (closeAfterParseNestMapList()) {
                    return;
                }
                continue;
            }

            final int delimiterIndex = indexOfDelimiter();
            final int endBraceIndex = indexOfEndBrace();
            assertListStringEndBraceIndex(_remainderString, endBraceIndex, _topString, currentList);

            if (delimiterIndex >= 0 && delimiterIndex < endBraceIndex) { // delimiter exists
                // e.g. value1 ; value2 ; value3}
                final String listValue = _remainderString.substring(0, delimiterIndex);
                currentList.add(filterMapListValue(listValue));

                // because the list element continues since the delimiter,
                // skip the delimiter and continue the loop.
                removePrefixTargetIndexPlus(delimiterIndex, _delimiter.length());
                continue;
            }

            // e.g. value1}, value2, }
            final String listValue = _remainderString.substring(0, endBraceIndex);
            currentList.add(filterMapListValue(listValue));

            // analyzing list is over, so close and return
            closeByEndBraceIndex(endBraceIndex);
            return;
        }
    }

    /**
     * Initialize at loop beginning.
     * @return Is is end?
     */
    protected boolean initializeAtLoopBeginning() {
        // remove prefix delimiter (result string is always trimmed)
        removePrefixAllDelimiter();

        if (_remainderString.equals("")) { // analyzing is over
            return true;
        }
        if (isStartsWithEndBrace(_remainderString)) { // analyzing current map is over
            removePrefixEndBrace();
            return true;
        }
        return false;
    }

    /**
     * Close after parse nest map list.
     * @return Is is closed?
     */
    protected boolean closeAfterParseNestMapList() {
        if (isStartsWithEndBrace(_remainderString)) {
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
        _remainderString = _remainderString.substring(endBraceIndex);
        removePrefixEndBrace();
    }

    protected int indexOfStartBrace() {
        return findIndexOfControlMark(_remainderString, _startBrace);
    }

    protected int indexOfEndBrace() {
        return findIndexOfControlMark(_remainderString, _endBrace);
    }

    protected int indexOfDelimiter() {
        return findIndexOfControlMark(_remainderString, _delimiter);
    }

    protected int indexOfEqual() {
        return findIndexOfControlMark(_remainderString, _equal);
    }

    protected int findIndexOfControlMark(String remainderString, String controlMark) {
        String current = remainderString;
        if (isEscapeCharEscape()) {
            final String escapedEscapeChar = toEscapedMark(_escapeChar);
            current = replace(current, escapedEscapeChar, buildLengthSpace(escapedEscapeChar));
        }
        int baseIndex = 0;
        while (true) {
            final int index = current.indexOf(controlMark);
            if (index < 0) { // not found
                return index;
            }
            if (index > 0) {
                final String lastChar = current.substring(index - 1, index);
                if (_escapeChar.equals(lastChar)) { // escaped
                    final int nextIndex = index + _escapeChar.length();
                    baseIndex = baseIndex + nextIndex;
                    current = current.substring(nextIndex);
                    continue;
                }
            }
            return baseIndex + index; // found
        }
    }

    protected String buildLengthSpace(String value) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            sb.append(" ");
        }
        return sb.toString();
    }

    // ===================================================================================
    //                                                                              Remove
    //                                                                              ======
    /**
     * Remove prefix map-mark and start-brace.
     */
    protected void removePrefixMapMarkAndStartBrace() {
        removePrefix(_mapMark + _startBrace);
    }

    /**
     * Remove prefix list-mark and start-brace.
     */
    protected void removePrefixListMarkAndStartBrace() {
        removePrefix(_listMark + _startBrace);
    }

    /**
     * Remove prefix delimiter.
     */
    protected void removePrefixDelimiter() {
        removePrefix(_delimiter);
    }

    /**
     * Remove prefix end-brace.
     */
    protected void removePrefixEndBrace() {
        removePrefix(_endBrace);
    }

    /**
     * Remove prefix as mark.
     * @param prefixString The string for prefix. (NotNull)
     */
    protected void removePrefix(String prefixString) {
        if (_remainderString == null) {
            final String notice = "The remainderString should not be null.";
            throwMapListStringPrefixFailureException(notice, prefixString, _topString);
        }
        if (prefixString == null) {
            final String notice = "The prefixString should not be null.";
            throwMapListStringPrefixFailureException(notice, prefixString, _topString);
        }

        removeBothSideSpaceAndTabAndNewLine();

        // deep (or unneeded?) check
        if (_remainderString.length() < prefixString.length()) {
            final String notice = "The remainderString length shuold be greater than the prefixString length.";
            throwMapListStringPrefixFailureException(notice, prefixString, _topString);
        }
        if (!_remainderString.startsWith(prefixString)) {
            final String notice = "The remainderString shuold start with the prefixString.";
            throwMapListStringPrefixFailureException(notice, prefixString, _topString);
        }

        _remainderString = _remainderString.substring(prefixString.length());
        removeBothSideSpaceAndTabAndNewLine();
    }

    protected void throwMapListStringPrefixFailureException(String notice, String prefixString, String mapString) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice(notice);
        br.addItem("Remainder String");
        br.addElement(_remainderString);
        br.addItem("Prefix String");
        br.addElement(prefixString);
        br.addItem("MapList String");
        br.addElement(mapString);
        prepareControlMarkMessage(br);
        final String msg = br.buildExceptionMessage();
        throw new MapListStringParseFailureException(msg);
    }

    /**
     * Remove prefix and all delimiters.
     */
    protected void removePrefixAllDelimiter() {
        removeBothSideSpaceAndTabAndNewLine();

        while (true) {
            if (!isStartsWithDelimiter(_remainderString)) {
                break;
            }

            if (isStartsWithDelimiter(_remainderString)) {
                removePrefixDelimiter();
                removeBothSideSpaceAndTabAndNewLine();
            }
        }
    }

    /**
     * Remove both side space and tab and new-line.
     */
    protected void removeBothSideSpaceAndTabAndNewLine() {
        _remainderString = _remainderString.trim();
    }

    /**
     * Remove prefix by the index and plus count.
     * @param index The base index. (NotMinus)
     * @param plusCount The plus count for index. (NotMinus)
     */
    protected void removePrefixTargetIndexPlus(int index, int plusCount) {
        _remainderString = _remainderString.substring(index + plusCount);
    }

    // ===================================================================================
    //                                                                              Filter
    //                                                                              ======
    protected String filterMapListKey(String value) {
        if (value == null) {
            return null;
        }
        value = value.trim();
        value = unescapeControlMark(value);
        return (("".equals(value) || "null".equals(value)) ? null : value);
    }

    protected String filterMapListValue(String value) {
        if (value == null) {
            return null;
        }
        value = value.trim();
        value = unescapeControlMark(value);
        return (("".equals(value) || "null".equals(value)) ? null : value);
    }

    // ===================================================================================
    //                                                                       Determination
    //                                                                       =============
    /**
     * Does it start with the map-prefix?
     * @param targetString The target string to determine. (NotNull)
     * @return The determination, true or false.
     */
    protected boolean isStartsWithMapPrefix(String targetString) {
        if (targetString == null) {
            String msg = "The argument 'targetString' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        targetString = targetString.trim();
        return targetString.startsWith(getMapPrefix());
    }

    protected String getMapPrefix() {
        return _mapMark + _startBrace;
    }

    /**
     * Does it start with the list-prefix?
     * @param targetString The target-string to determine. (NotNull)
     * @return The determination, true or false.
     */
    protected boolean isStartsWithListPrefix(String targetString) {
        if (targetString == null) {
            String msg = "The argument 'targetString' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        targetString = targetString.trim();
        return targetString.startsWith(_listMark + _startBrace);
    }

    protected String getListPrefix() {
        return _listMark + _startBrace;
    }

    /**
     * Does it start with the delimiter?
     * @param targetString The target string to determine. (NotNull)
     * @return The determination, true or false.
     */
    protected boolean isStartsWithDelimiter(String targetString) {
        if (targetString == null) {
            String msg = "The argument 'targetString' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        targetString = targetString.trim();
        return targetString.startsWith(_delimiter);
    }

    /**
     * Does it start with end-brace?
     * @param targetString The target string to determine. (NotNull)
     * @return The determination, true or false.
     */
    protected boolean isStartsWithEndBrace(String targetString) {
        if (targetString == null) {
            String msg = "The argument 'targetString' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        targetString = targetString.trim();
        return targetString.startsWith(_endBrace);
    }

    /**
     * Does it end with end-brace?
     * @param targetString The target string to determine. (NotNull)
     * @return The determination, true or false.
     */
    protected boolean isEndsWithEndBrace(String targetString) {
        if (targetString == null) {
            String msg = "The argument 'targetString' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        targetString = targetString.trim();
        return targetString.endsWith(_endBrace);
    }

    // ===================================================================================
    //                                                                       Setup MapList
    //                                                                       =============
    /**
     * Set up new-created nest map as element of the current map.
     * @param currentMap the current map to set up. (NotNull)
     * @param mapKey The key of nest map. (NotNull)
     * @return The new-created nest map. (NotNull)
     */
    protected Map<String, Object> setupNestMap(Map<String, Object> currentMap, String mapKey) {
        final Map<String, Object> nestMap = newStringObjectMap();
        registerToMap(currentMap, filterMapListKey(mapKey), nestMap);
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
        registerToMap(currentMap, filterMapListKey(mapKey), nestList);
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

    /**
     * New string-object map.
     * @return The new-created map. (NotNull)
     */
    protected Map<String, Object> newStringObjectMap() {
        return new LinkedHashMap<String, Object>();
    }

    /**
     * New object-type list.
     * @return The new-created list. (NotNull)
     */
    protected List<Object> newObjectList() {
        return new ArrayList<Object>();
    }

    // ===================================================================================
    //                                                                    Map Registration
    //                                                                    ================
    protected void registerToMap(Map<String, Object> currentMap, String key, Object element) {
        doCheckDuplicateEntryIfNeeds(currentMap, key, element);
        currentMap.put(key, element);
    }

    protected void doCheckDuplicateEntryIfNeeds(Map<String, Object> currentMap, String key, Object element) {
        if (isCheckDuplicateEntry()) {
            if (currentMap.containsKey(key)) {
                throwMapStringDuplicateEntryFoundException(currentMap, key, element);
            }
        }
    }

    protected void throwMapStringDuplicateEntryFoundException(Map<String, Object> currentMap, String key, Object element) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Duplicate entry in the map string.");
        br.addItem("MapList String");
        br.addElement(_topString);
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
        prepareControlMarkMessage(br);
        final String msg = br.buildExceptionMessage();
        throw new MapListStringDuplicateEntryException(msg);
    }

    // ===================================================================================
    //                                                                              Escape
    //                                                                              ======
    protected String escapeControlMark(Object value) {
        if (value == null) {
            return null;
        }
        String filtered = value.toString();
        if (isEscapeCharEscape()) {
            filtered = replace(filtered, _escapeChar, toEscapedMark(_escapeChar));
        }
        filtered = replace(filtered, _startBrace, toEscapedMark(_startBrace));
        filtered = replace(filtered, _endBrace, toEscapedMark(_endBrace));
        filtered = replace(filtered, _delimiter, toEscapedMark(_delimiter));
        filtered = replace(filtered, _equal, toEscapedMark(_equal));
        return filtered;
    }

    protected String unescapeControlMark(String value) {
        if (value == null) {
            return null;
        }
        String filtered = value;
        final String escapedEscapeMark = ESCAPED_ESCAPE_MARK;
        if (isEscapeCharEscape()) {
            filtered = replace(filtered, toEscapedMark(_escapeChar), escapedEscapeMark);
        }
        filtered = replace(filtered, toEscapedMark(_startBrace), _startBrace);
        filtered = replace(filtered, toEscapedMark(_endBrace), _endBrace);
        filtered = replace(filtered, toEscapedMark(_delimiter), _delimiter);
        filtered = replace(filtered, toEscapedMark(_equal), _equal);
        if (isEscapeCharEscape()) {
            filtered = replace(filtered, escapedEscapeMark, _escapeChar);
        }
        return filtered;
    }

    protected String toEscapedMark(String mark) {
        return _escapeChar + mark;
    }

    protected boolean isEscapeCharEscape() {
        // escape for escape char is unsupported (unneeded)
        // so fixedly returns false
        //
        // compatibility is treated as important here
        //  o "\\n = \n" in convertValueMap.dfprop can directly work
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
    //                                                                       Assert Helper
    //                                                                       =============
    protected void assertMapString(String mapString) {
        if (mapString == null) {
            final String notice = "The map string should not be null.";
            throwMapStringBasicFailureException(notice, mapString);
        }
        mapString = mapString.trim();
        if (!isStartsWithMapPrefix(mapString)) {
            final String notice = "The map string should start with '" + getMapPrefix() + "'.";
            throwMapStringBasicFailureException(notice, mapString);
        }
        if (!isEndsWithEndBrace(mapString)) {
            final String notice = "The map string should end with '" + _endBrace + "'.";
            throwMapStringBasicFailureException(notice, mapString);
        }
        final int startBraceCount = getControlMarkCount(mapString, _startBrace);
        final int endBraceCount = getControlMarkCount(mapString, _endBrace);
        if (startBraceCount != endBraceCount) {
            throwMapStringDifferentCountBracesException(mapString, startBraceCount, endBraceCount);
        }
    }

    protected void throwMapStringBasicFailureException(String notice, String mapString) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice(notice);
        br.addItem("Remainder String");
        br.addElement(_remainderString);
        br.addItem("Map String");
        br.addElement(mapString);
        prepareControlMarkMessage(br);
        final String msg = br.buildExceptionMessage();
        throw new MapListStringParseFailureException(msg);
    }

    protected void throwMapStringDifferentCountBracesException(String mapString, int startCount, int endCount) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Different count between start braces and end braces.");
        br.addItem("Advice");
        br.addElement("Make sure braces on your map-list string.");
        br.addElement("For example:");
        br.addElement("  (o): map:{ foo = bar }");
        br.addElement("  (o): map:{ foo = map:{ bar = qux } }");
        br.addElement("  (x): map:{ foo = ");
        br.addElement("  (x): map:{ foo = map:{ }");
        br.addElement("");
        br.addElement("map-list string can escape control marks");
        br.addElement("so pay attention to last char of value like this:");
        br.addElement("  (x): map:{ foo = C:\\foo\\bar\\}  // last '}' escaped by escape char");
        br.addElement("  (o): map:{ foo = C:\\foo\\bar\\ } // space helps you at the case");
        br.addItem("Map String");
        br.addElement(mapString);
        br.addItem("Brace Count");
        br.addElement("start: " + startCount);
        br.addElement("end: " + endCount);
        prepareControlMarkMessage(br);
        final String msg = br.buildExceptionMessage();
        throw new MapListStringParseFailureException(msg);
    }

    protected void assertListString(String listString) {
        if (listString == null) {
            final String notice = "The list string should not be null.";
            throwListStringBasicFailureException(notice, listString);
        }
        listString = listString.trim();
        if (!isStartsWithListPrefix(listString)) {
            final String notice = "The list string should start with '" + getListPrefix() + "'.";
            throwListStringBasicFailureException(notice, listString);
        }
        if (!isEndsWithEndBrace(listString)) {
            final String notice = "The list string should end with '" + _endBrace + "'.";
            throwListStringBasicFailureException(notice, listString);
        }
        final int startBraceCount = getControlMarkCount(listString, _startBrace);
        final int endBraceCount = getControlMarkCount(listString, _endBrace);
        if (startBraceCount != endBraceCount) {
            throwListStringDifferentCountBracesException(listString, startBraceCount, endBraceCount);
        }
    }

    protected void throwListStringBasicFailureException(String notice, String listString) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice(notice);
        br.addItem("Remainder String");
        br.addElement(_remainderString);
        br.addItem("List String");
        br.addElement(listString);
        prepareControlMarkMessage(br);
        final String msg = br.buildExceptionMessage();
        throw new MapListStringParseFailureException(msg);
    }

    protected void throwListStringDifferentCountBracesException(String listString, int startCount, int endCount) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Different count between start braces and end braces.");
        br.addItem("List String");
        br.addElement(listString);
        br.addItem("Brace Count");
        br.addElement("Start: " + startCount);
        br.addElement("End: " + endCount);
        prepareControlMarkMessage(br);
        final String msg = br.buildExceptionMessage();
        throw new MapListStringParseFailureException(msg);
    }

    protected int getControlMarkCount(String targetString, String controlMark) {
        int result = 0;
        String current = targetString;
        while (true) {
            final int index = findIndexOfControlMark(current, controlMark);
            if (index < 0) {
                break;
            }
            result++;
            current = current.substring(index + controlMark.length());
        }
        if (result == 0) {
            result = -1;
        }
        return result;
    }

    protected void assertMapStringEqualIndex(String remainderString, int equalIndex, String mapString,
            Map<String, Object> currentMap) {
        if (remainderString == null) {
            final String notice = "The remainderString should not be null:";
            throwMapStringEqualFailureException(notice, remainderString, equalIndex, mapString, currentMap);
        }
        if (equalIndex < 0) {
            final String notice = "Not found the equal mark in the map.";
            throwMapStringEqualFailureException(notice, remainderString, equalIndex, mapString, currentMap);
        }
        // deep (or unneeded?) check (written by younger jflute)
        if (remainderString.length() < equalIndex) {
            final String notice = "The remainderString length should be greater than equalIndex:";
            throwMapStringEqualFailureException(notice, remainderString, equalIndex, mapString, currentMap);
        }
        final String extractedMark = remainderString.substring(equalIndex, equalIndex + _equal.length());
        if (!extractedMark.equals(_equal)) {
            final String notice = "The remainderString should have equal mark at equalIndex:";
            throwMapStringEqualFailureException(notice, remainderString, equalIndex, mapString, currentMap);
        }
    }

    protected void throwMapStringEqualFailureException(String notice, String remainderMapString, int equalIndex,
            String mapString, Map<String, Object> currentMap) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice(notice);
        br.addItem("Remainder String");
        br.addElement(remainderMapString);
        br.addItem("Equal Index");
        br.addElement(equalIndex);
        br.addItem("Whole Map String");
        br.addElement(mapString);
        br.addItem("Making Map");
        br.addElement(currentMap);
        prepareControlMarkMessage(br);
        final String msg = br.buildExceptionMessage();
        throw new MapListStringParseFailureException(msg);
    }

    protected void assertMapStringEndBraceIndex(String remainderString, int endBraceIndex, String mapString,
            Map<String, Object> currentMap) {
        if (remainderString == null) {
            final String notice = "The remainderString should not be null:";
            throwMapStringEndBraceFailureException(notice, remainderString, endBraceIndex, mapString, currentMap);
        }
        if (endBraceIndex < 0) {
            final String notice = "Not found the end brace.";
            throwMapStringEndBraceFailureException(notice, remainderString, endBraceIndex, mapString, currentMap);
        }
        // deep (or unneeded?) check (written by younger jflute)
        if (remainderString.length() < endBraceIndex) {
            final String notice = "The remainderString length should be greater than endMarkIndex:";
            throwMapStringEndBraceFailureException(notice, remainderString, endBraceIndex, mapString, currentMap);
        }
        final String extractedMark = remainderString.substring(endBraceIndex, endBraceIndex + _endBrace.length());
        if (!extractedMark.equals(_endBrace)) {
            final String notice = "The remainderString should have end brace at the endMarkIndex:";
            throwMapStringEndBraceFailureException(notice, remainderString, endBraceIndex, mapString, currentMap);
        }
    }

    protected void assertListStringEndBraceIndex(String remainderString, int endBraceIndex, String listString,
            List<?> currentList) {
        if (remainderString == null) {
            final String notice = "The remainderString should not be null:";
            throwListStringEndBraceFailureException(notice, remainderString, endBraceIndex, listString, currentList);
        }
        if (endBraceIndex < 0) {
            final String notice = "Not found the end brace.";
            throwListStringEndBraceFailureException(notice, remainderString, endBraceIndex, listString, currentList);
        }
        // deep (or unneeded?) check (written by younger jflute)
        if (remainderString.length() < endBraceIndex) {
            final String notice = "The remainderString length should be greater than endMarkIndex:";
            throwListStringEndBraceFailureException(notice, remainderString, endBraceIndex, listString, currentList);
        }
        final String extractedMark = remainderString.substring(endBraceIndex, endBraceIndex + _endBrace.length());
        if (!extractedMark.equals(_endBrace)) {
            final String notice = "The remainderString should have end brace at the endMarkIndex:";
            throwListStringEndBraceFailureException(notice, remainderString, endBraceIndex, listString, currentList);
        }
    }

    protected void throwMapStringEndBraceFailureException(String notice, String remainderMapString, int equalIndex,
            String mapString, Map<String, Object> currentMap) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice(notice);
        br.addItem("Remainder Map String");
        br.addElement(remainderMapString);
        br.addItem("EndBrace Index");
        br.addElement(equalIndex);
        br.addItem("Whole Map String");
        br.addElement(mapString);
        br.addItem("Making Map");
        br.addElement(currentMap);
        prepareControlMarkMessage(br);
        final String msg = br.buildExceptionMessage();
        throw new MapListStringParseFailureException(msg);
    }

    protected void throwListStringEndBraceFailureException(String notice, String remainderMapString, int equalIndex,
            String listString, List<?> currentList) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice(notice);
        br.addItem("Remainder List String");
        br.addElement(remainderMapString);
        br.addItem("EndBrace Index");
        br.addElement(equalIndex);
        br.addItem("Whole List String");
        br.addElement(listString);
        br.addItem("Making List");
        br.addElement(currentList);
        prepareControlMarkMessage(br);
        final String msg = br.buildExceptionMessage();
        throw new MapListStringParseFailureException(msg);
    }

    protected void prepareControlMarkMessage(final ExceptionMessageBuilder br) {
        br.addItem("Control Marks");
        br.addElement(_startBrace + " " + _endBrace + " " + _delimiter + " " + _equal);
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String replace(String str, String fromStr, String toStr) {
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

    protected String lnd() {
        return ln() + "    ";
    }

    protected String ln() {
        return "\n";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setMapMark(String mapMark) {
        _mapMark = mapMark;
    }

    public void setListMark(String listMark) {
        _listMark = listMark;
    }

    public void setStartBrace(String startBrace) {
        _startBrace = startBrace;
    }

    public void setEndBrace(String endBrace) {
        _endBrace = endBrace;
    }

    public void setDelimiter(String delimiter) {
        _delimiter = delimiter;
    }

    public void setEqual(String equal) {
        _equal = equal;
    }

    // -----------------------------------------------------
    //                                                Option
    //                                                ------
    /**
     * Check duplicate entry of map. (throws exception if found)
     * @return this;
     */
    public MapListString checkDuplicateEntry() {
        _checkDuplicateEntry = true;
        return this;
    }

    public boolean isCheckDuplicateEntry() {
        return _checkDuplicateEntry;
    }
}