/*
 * Copyright 2014-2015 the original author or authors.
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
package org.dbflute.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

/**
 * String Utility for Internal Programming of DBFlute.
 * @author jflute
 */
public class Srl {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final String HARF_LOWER_ALPHABET = "abcdefghijklmnopqrstuvwxyz";
    protected static final String HARF_NUMBER = "0123456789";
    protected static final Set<Character> _alphabetHarfCharSet;
    static {
        final Set<Character> setupSet = new HashSet<Character>();
        final StringBuilder sb = new StringBuilder();
        sb.append(HARF_LOWER_ALPHABET);
        sb.append(sb.toString().toUpperCase());
        char[] chAry = sb.toString().toCharArray();
        for (char ch : chAry) {
            setupSet.add(ch);
        }
        _alphabetHarfCharSet = Collections.unmodifiableSet(setupSet);
    }
    protected static final Set<Character> _alphabetHarfLowerCharSet;
    static {
        final Set<Character> setupSet = new HashSet<Character>();
        final StringBuilder sb = new StringBuilder();
        sb.append(HARF_LOWER_ALPHABET);
        char[] chAry = sb.toString().toCharArray();
        for (char ch : chAry) {
            setupSet.add(ch);
        }
        _alphabetHarfLowerCharSet = Collections.unmodifiableSet(setupSet);
    }
    protected static final Set<Character> _alphabetHarfUpperCharSet;
    static {
        final Set<Character> setupSet = new HashSet<Character>();
        final StringBuilder sb = new StringBuilder();
        sb.append(HARF_LOWER_ALPHABET.toUpperCase());
        char[] chAry = sb.toString().toCharArray();
        for (char ch : chAry) {
            setupSet.add(ch);
        }
        _alphabetHarfUpperCharSet = Collections.unmodifiableSet(setupSet);
    }
    protected static final Set<Character> _numberHarfCharSet;
    static {
        final Set<Character> setupSet = new HashSet<Character>();
        final String chStr = HARF_NUMBER;
        char[] chAry = chStr.toCharArray();
        for (char ch : chAry) {
            setupSet.add(ch);
        }
        _numberHarfCharSet = Collections.unmodifiableSet(setupSet);
    }
    protected static final Set<Character> _alphabetHarfNumberHarfCharSet;
    static {
        final Set<Character> setupSet = new HashSet<Character>();
        setupSet.addAll(_alphabetHarfCharSet);
        setupSet.addAll(_numberHarfCharSet);
        _alphabetHarfNumberHarfCharSet = Collections.unmodifiableSet(setupSet);
    }
    protected static final Set<Character> _alphabetNumberHarfLowerCharSet;
    static {
        final Set<Character> setupSet = new HashSet<Character>();
        setupSet.addAll(_alphabetHarfLowerCharSet);
        setupSet.addAll(_numberHarfCharSet);
        _alphabetNumberHarfLowerCharSet = Collections.unmodifiableSet(setupSet);
    }
    protected static final Set<Character> _alphabetNumberHarfUpperCharSet;
    static {
        final Set<Character> setupSet = new HashSet<Character>();
        setupSet.addAll(_alphabetHarfUpperCharSet);
        setupSet.addAll(_numberHarfCharSet);
        _alphabetNumberHarfUpperCharSet = Collections.unmodifiableSet(setupSet);
    }

    // ===================================================================================
    //                                                                        Null & Empty
    //                                                                        ============
    /**
     * Is the string null or empty string? 
     * @param str A judged string. (NullAllowed)
     * @return The determination.
     */
    public static boolean is_Null_or_Empty(final String str) {
        return str == null || str.length() == 0;
    }

    /**
     * Is the string null or trimmed-empty string? 
     * @param str A judged string. (NullAllowed)
     * @return The determination.
     */
    public static boolean is_Null_or_TrimmedEmpty(final String str) {
        return str == null || str.trim().length() == 0;
    }

    /**
     * Is the string not null and not empty string? 
     * @param str A judged string. (NullAllowed)
     * @return The determination.
     */
    public static boolean is_NotNull_and_NotEmpty(final String str) {
        return !is_Null_or_Empty(str);
    }

    /**
     * Is the string not null and not trimmed-empty string? 
     * @param str A judged string. (NullAllowed)
     * @return The determination.
     */
    public static boolean is_NotNull_and_NotTrimmedEmpty(final String str) {
        return !is_Null_or_TrimmedEmpty(str);
    }

    /**
     * Is the string empty string? 
     * @param str A judged string. (NullAllowed)
     * @return The determination.
     */
    public static boolean isEmpty(final String str) {
        return str != null && str.length() == 0;
    }

    /**
     * Is the string trimmed-empty string? 
     * @param str A judged string. (NullAllowed)
     * @return The determination.
     */
    public static boolean isTrimmedEmpty(final String str) {
        return str != null && str.trim().length() == 0;
    }

    // ===================================================================================
    //                                                                              Length
    //                                                                              ======
    public static int length(final String str) {
        assertStringNotNull(str);
        return str.length();
    }

    public static String cut(final String str, int length) {
        assertStringNotNull(str);
        return cut(str, length, null);
    }

    public static String cut(final String str, int length, String suffix) {
        assertStringNotNull(str);
        return str.length() > length ? (str.substring(0, length) + (suffix != null ? suffix : "")) : str;
    }

    // ===================================================================================
    //                                                                                Case
    //                                                                                ====
    public static String toLowerCase(final String str) {
        assertStringNotNull(str);
        return str.toLowerCase();
    }

    public static String toUpperCase(final String str) {
        assertStringNotNull(str);
        return str.toUpperCase();
    }

    // ===================================================================================
    //                                                                                Trim
    //                                                                                ====
    public static String trim(final String str) {
        return doTrim(str, null);
    }

    public static String trim(final String str, final String trimStr) {
        return doTrim(str, trimStr);
    }

    public static String ltrim(final String str) {
        return doLTrim(str, null);
    }

    public static String ltrim(final String str, final String trimStr) {
        return doLTrim(str, trimStr);
    }

    public static String rtrim(final String str) {
        return doRTrim(str, null);
    }

    public static String rtrim(final String str, final String trimStr) {
        return doRTrim(str, trimStr);
    }

    protected static String doTrim(final String str, final String trimStr) {
        return doRTrim(doLTrim(str, trimStr), trimStr);
    }

    protected static String doLTrim(final String str, final String trimStr) {
        assertStringNotNull(str);

        // for trim target same as String.trim()
        if (trimStr == null) {
            final String notTrimmedString = "a";
            final String trimmed = (str + notTrimmedString).trim();
            return trimmed.substring(0, trimmed.length() - notTrimmedString.length());
        }

        // for original trim target
        String trimmed = str;
        for (; trimmed.startsWith(trimStr);) {
            trimmed = substringFirstRear(trimmed, trimStr);
        }
        return trimmed;
    }

    protected static String doRTrim(final String str, final String trimStr) {
        assertStringNotNull(str);

        // for trim target same as String.trim()
        if (trimStr == null) {
            final String notTrimmedString = "a";
            return (notTrimmedString + str).trim().substring(notTrimmedString.length());
        }

        // for original trim target
        String trimmed = str;
        for (; trimmed.endsWith(trimStr);) {
            trimmed = substringLastFront(trimmed, trimStr);
        }
        return trimmed;
    }

    // ===================================================================================
    //                                                                             Replace
    //                                                                             =======
    public static String replace(String str, String fromStr, String toStr) {
        assertStringNotNull(str);
        assertFromStringNotNull(fromStr);
        assertToStringNotNull(toStr);
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

    public static String replaceBy(String str, Map<String, String> fromToMap) {
        assertStringNotNull(str);
        assertFromToMapNotNull(fromToMap);
        final Set<Entry<String, String>> entrySet = fromToMap.entrySet();
        for (Entry<String, String> entry : entrySet) {
            str = replace(str, entry.getKey(), entry.getValue());
        }
        return str;
    }

    public static String replaceScopeContent(String str, String fromStr, String toStr, String beginMark, String endMark) {
        final List<ScopeInfo> scopeList = extractScopeList(str, beginMark, endMark);
        if (scopeList.isEmpty()) {
            return str;
        }
        return scopeList.get(0).replaceContentOnBaseString(fromStr, toStr);
    }

    public static String replaceScopeInterspace(String str, String fromStr, String toStr, String beginMark, String endMark) {
        final List<ScopeInfo> scopeList = extractScopeList(str, beginMark, endMark);
        if (scopeList.isEmpty()) {
            return str;
        }
        return scopeList.get(0).replaceInterspaceOnBaseString(fromStr, toStr);
    }

    // ===================================================================================
    //                                                                               Split
    //                                                                               =====
    /**
     * @param str The split target string. (NotNull)
     * @param delimiter The delimiter for split. (NotNull)
     * @return The split list. (NotNull)
     */
    public static List<String> splitList(final String str, final String delimiter) {
        return doSplitList(str, delimiter, false);
    }

    /**
     * @param str The split target string. (NotNull)
     * @param delimiter The delimiter for split. (NotNull)
     * @return The split list that their elements is trimmed. (NotNull)
     */
    public static List<String> splitListTrimmed(final String str, final String delimiter) {
        return doSplitList(str, delimiter, true);
    }

    protected static List<String> doSplitList(final String str, final String delimiter, boolean trim) {
        assertStringNotNull(str);
        assertDelimiterNotNull(delimiter);
        final List<String> list = new ArrayList<String>();
        int elementIndex = 0;
        int delimiterIndex = str.indexOf(delimiter);
        while (delimiterIndex >= 0) {
            final String element = str.substring(elementIndex, delimiterIndex);
            list.add(trim ? element.trim() : element);
            elementIndex = delimiterIndex + delimiter.length();
            delimiterIndex = str.indexOf(delimiter, elementIndex);
        }
        final String element = str.substring(elementIndex);
        list.add(trim ? element.trim() : element);
        return list;
    }

    // ===================================================================================
    //                                                                             IndexOf
    //                                                                             =======
    /**
     * Get the index of the first-found delimiter.
     * <pre>
     * indexOfFirst("foo.bar/baz.qux", ".", "/")
     * returns the index of ".bar"
     * </pre>
     * @param str The target string. (NotNull)
     * @param delimiters The array of delimiters. (NotNull) 
     * @return The information of index. (NullAllowed: if delimiter not found)
     */
    public static IndexOfInfo indexOfFirst(final String str, final String... delimiters) {
        return doIndexOfFirst(false, str, delimiters);
    }

    /**
     * Get the index of the first-found delimiter ignoring case.
     * <pre>
     * indexOfFirst("foo.bar/baz.qux", "A", "U")
     * returns the index of "ar/baz..."
     * </pre>
     * @param str The target string. (NotNull)
     * @param delimiters The array of delimiters. (NotNull) 
     * @return The information of index. (NullAllowed: if delimiter not found)
     */
    public static IndexOfInfo indexOfFirstIgnoreCase(final String str, final String... delimiters) {
        return doIndexOfFirst(true, str, delimiters);
    }

    protected static IndexOfInfo doIndexOfFirst(final boolean ignoreCase, final String str, final String... delimiters) {
        return doIndexOf(ignoreCase, false, str, delimiters);
    }

    /**
     * Get the index of the last-found delimiter.
     * <pre>
     * indexOfLast("foo.bar/baz.qux", ".", "/")
     * returns the index of ".qux"
     * </pre>
     * @param str The target string. (NotNull)
     * @param delimiters The array of delimiters. (NotNull) 
     * @return The information of index. (NullAllowed: if delimiter not found)
     */
    public static IndexOfInfo indexOfLast(final String str, final String... delimiters) {
        return doIndexOfLast(false, str, delimiters);
    }

    /**
     * Get the index of the last-found delimiter ignoring case.
     * <pre>
     * indexOfLast("foo.bar/baz.qux", "A", "U")
     * returns the index of "ux"
     * </pre>
     * @param str The target string. (NotNull)
     * @param delimiters The array of delimiters. (NotNull) 
     * @return The information of index. (NullAllowed: if delimiter not found)
     */
    public static IndexOfInfo indexOfLastIgnoreCase(final String str, final String... delimiters) {
        return doIndexOfLast(true, str, delimiters);
    }

    protected static IndexOfInfo doIndexOfLast(final boolean ignoreCase, final String str, final String... delimiters) {
        return doIndexOf(ignoreCase, true, str, delimiters);
    }

    protected static IndexOfInfo doIndexOf(final boolean ignoreCase, final boolean last, final String str, final String... delimiters) {
        final String filteredStr;
        if (ignoreCase) {
            filteredStr = str.toLowerCase();
        } else {
            filteredStr = str;
        }
        int targetIndex = -1;
        String targetDelimiter = null;
        for (String delimiter : delimiters) {
            final String filteredDelimiter;
            if (ignoreCase) {
                filteredDelimiter = delimiter.toLowerCase();
            } else {
                filteredDelimiter = delimiter;
            }
            final int index;
            if (last) {
                index = filteredStr.lastIndexOf(filteredDelimiter);
            } else {
                index = filteredStr.indexOf(filteredDelimiter);
            }
            if (index < 0) {
                continue;
            }
            if (targetIndex < 0 || (last ? targetIndex < index : targetIndex > index)) {
                targetIndex = index;
                targetDelimiter = delimiter;
            }
        }
        if (targetIndex < 0) {
            return null;
        }
        final IndexOfInfo info = new IndexOfInfo();
        info.setBaseString(str);
        info.setIndex(targetIndex);
        info.setDelimiter(targetDelimiter);
        return info;
    }

    public static class IndexOfInfo {
        protected String _baseString;
        protected int _index;
        protected String _delimiter;

        public String substringFront() {
            return _baseString.substring(0, getIndex());
        }

        public String substringFrontTrimmed() {
            return substringFront().trim();
        }

        public String substringRear() {
            return _baseString.substring(getRearIndex());
        }

        public String substringRearTrimmed() {
            return substringRear().trim();
        }

        public int getRearIndex() {
            return _index + _delimiter.length();
        }

        public String getBaseString() {
            return _baseString;
        }

        public void setBaseString(String baseStr) {
            _baseString = baseStr;
        }

        public int getIndex() {
            return _index;
        }

        public void setIndex(int index) {
            _index = index;
        }

        public String getDelimiter() {
            return _delimiter;
        }

        public void setDelimiter(String delimiter) {
            _delimiter = delimiter;
        }
    }

    // ===================================================================================
    //                                                                           SubString
    //                                                                           =========
    /**
     * Extract sub-string by begin index. (skip front string)
     * <pre>
     * substring("flute", 2)
     * returns "ute"
     * </pre>
     * @param str The target string. (NotNull)
     * @param beginIndex The from-index. 
     * @return The part of string. (NotNull)
     */
    public static String substring(final String str, final int beginIndex) {
        assertStringNotNull(str);
        if (str.length() < beginIndex) {
            String msg = "The length of the string was smaller than the begin index:";
            msg = msg + " str=" + str + ", beginIndex=" + beginIndex;
            throw new StringIndexOutOfBoundsException(msg);
        }
        return str.substring(beginIndex);
    }

    /**
     * Extract sub-string by begin and end index. (get scope string)
     * <pre>
     * substring("flute", 1, 3)
     * returns "lu"
     * </pre>
     * @param str The target string. (NotNull)
     * @param beginIndex The from-index.
     * @param endIndex The to-index.
     * @return The part of string. (NotNull)
     */
    public static String substring(final String str, final int beginIndex, final int endIndex) {
        assertStringNotNull(str);
        if (str.length() < beginIndex) {
            String msg = "The length of the string was smaller than the begin index:";
            msg = msg + " str=" + str + " beginIndex=" + beginIndex + " endIndex=" + endIndex;
            throw new StringIndexOutOfBoundsException(msg);
        }
        if (str.length() < endIndex) {
            String msg = "The length of the string was smaller than the end index:";
            msg = msg + " str=" + str + " beginIndex=" + beginIndex + " endIndex=" + endIndex;
            throw new StringIndexOutOfBoundsException(msg);
        }
        if (beginIndex > endIndex) {
            String msg = "The begin index was larger than the end index:";
            msg = msg + " str=" + str + " beginIndex=" + beginIndex + " endIndex=" + endIndex;
            throw new StringIndexOutOfBoundsException(msg);
        }
        return str.substring(beginIndex, endIndex);
    }

    /**
     * Extract front sub-string by index.
     * <pre>
     * rearstring("flute", 2)
     * returns "fl"
     * </pre>
     * @param str The target string. (NotNull)
     * @param index The index from rear. 
     * @return The rear string. (NotNull)
     */
    public static String frontstring(final String str, final int index) {
        assertStringNotNull(str);
        if (str.length() < index) {
            String msg = "The length of the string was smaller than the index:";
            msg = msg + " str=" + str + " index=" + index;
            throw new StringIndexOutOfBoundsException(msg);
        }
        return str.substring(0, index);
    }

    /**
     * Extract rear sub-string by reverse index.
     * <pre>
     * rearstring("flute", 2)
     * returns "te"
     * </pre>
     * @param str The target string. (NotNull)
     * @param reverseIndex The index from rear. 
     * @return The rear string. (NotNull)
     */
    public static String rearstring(final String str, final int reverseIndex) {
        assertStringNotNull(str);
        if (str.length() < reverseIndex) {
            String msg = "The length of the string was smaller than the index:";
            msg = msg + " str=" + str + " reverseIndex=" + reverseIndex;
            throw new StringIndexOutOfBoundsException(msg);
        }
        return str.substring(str.length() - reverseIndex);
    }

    /**
     * Extract front sub-string from first-found delimiter.
     * <pre>
     * substringFirstFront("foo.bar/baz.qux", ".", "/")
     * returns "foo"
     * </pre>
     * @param str The target string. (NotNull)
     * @param delimiters The array of delimiters. (NotNull) 
     * @return The part of string. (NotNull: if delimiter not found, returns argument-plain string)
     */
    public static String substringFirstFront(final String str, final String... delimiters) {
        assertStringNotNull(str);
        return doSubstringFirstRear(false, false, false, str, delimiters);
    }

    /**
     * Extract front sub-string from first-found delimiter ignoring case.
     * <pre>
     * substringFirstFront("foo.bar/baz.qux", "A", "U")
     * returns "foo.b"
     * </pre>
     * @param str The target string. (NotNull)
     * @param delimiters The array of delimiters. (NotNull) 
     * @return The part of string. (NotNull: if delimiter not found, returns argument-plain string)
     */
    public static String substringFirstFrontIgnoreCase(final String str, final String... delimiters) {
        assertStringNotNull(str);
        return doSubstringFirstRear(false, false, true, str, delimiters);
    }

    /**
     * Extract rear sub-string from first-found delimiter.
     * <pre>
     * substringFirstRear("foo.bar/baz.qux", ".", "/")
     * returns "bar/baz.qux"
     * </pre>
     * @param str The target string. (NotNull)
     * @param delimiters The array of delimiters. (NotNull) 
     * @return The part of string. (NotNull: if delimiter not found, returns argument-plain string)
     */
    public static String substringFirstRear(String str, String... delimiters) {
        assertStringNotNull(str);
        return doSubstringFirstRear(false, true, false, str, delimiters);
    }

    /**
     * Extract rear sub-string from first-found delimiter ignoring case.
     * <pre>
     * substringFirstRear("foo.bar/baz.qux", "A", "U")
     * returns "ar/baz.qux"
     * </pre>
     * @param str The target string. (NotNull)
     * @param delimiters The array of delimiters. (NotNull) 
     * @return The part of string. (NotNull: if delimiter not found, returns argument-plain string)
     */
    public static String substringFirstRearIgnoreCase(String str, String... delimiters) {
        assertStringNotNull(str);
        return doSubstringFirstRear(false, true, true, str, delimiters);
    }

    /**
     * Extract front sub-string from last-found delimiter.
     * <pre>
     * substringLastFront("foo.bar/baz.qux", ".", "/")
     * returns "foo.bar/baz"
     * </pre>
     * @param str The target string. (NotNull)
     * @param delimiters The array of delimiters. (NotNull) 
     * @return The part of string. (NotNull: if delimiter not found, returns argument-plain string)
     */
    public static String substringLastFront(String str, String... delimiters) {
        assertStringNotNull(str);
        return doSubstringFirstRear(true, false, false, str, delimiters);
    }

    /**
     * Extract front sub-string from last-found delimiter ignoring case.
     * <pre>
     * substringLastFront("foo.bar/baz.qux", "A", "U")
     * returns "foo.bar/baz.q"
     * </pre>
     * @param str The target string. (NotNull)
     * @param delimiters The array of delimiters. (NotNull) 
     * @return The part of string. (NotNull: if delimiter not found, returns argument-plain string)
     */
    public static String substringLastFrontIgnoreCase(String str, String... delimiters) {
        assertStringNotNull(str);
        return doSubstringFirstRear(true, false, true, str, delimiters);
    }

    /**
     * Extract rear sub-string from last-found delimiter.
     * <pre>
     * substringLastRear("foo.bar/baz.qux", ".", "/")
     * returns "qux"
     * </pre>
     * @param str The target string. (NotNull)
     * @param delimiters The array of delimiters. (NotNull) 
     * @return The part of string. (NotNull: if delimiter not found, returns argument-plain string)
     */
    public static String substringLastRear(String str, String... delimiters) {
        assertStringNotNull(str);
        return doSubstringFirstRear(true, true, false, str, delimiters);
    }

    /**
     * Extract rear sub-string from last-found delimiter ignoring case.
     * <pre>
     * substringLastRear("foo.bar/baz.qux", "A", "U")
     * returns "x"
     * </pre>
     * @param str The target string. (NotNull)
     * @param delimiters The array of delimiters. (NotNull) 
     * @return The part of string. (NotNull: if delimiter not found, returns argument-plain string)
     */
    public static String substringLastRearIgnoreCase(String str, String... delimiters) {
        assertStringNotNull(str);
        return doSubstringFirstRear(true, true, true, str, delimiters);
    }

    protected static final String doSubstringFirstRear(final boolean last, final boolean rear, final boolean ignoreCase, final String str,
            String... delimiters) {
        assertStringNotNull(str);
        final IndexOfInfo info;
        if (ignoreCase) {
            if (last) {
                info = indexOfLastIgnoreCase(str, delimiters);
            } else {
                info = indexOfFirstIgnoreCase(str, delimiters);
            }
        } else {
            if (last) {
                info = indexOfLast(str, delimiters);
            } else {
                info = indexOfFirst(str, delimiters);
            }
        }
        if (info == null) {
            return str;
        }
        if (rear) {
            return str.substring(info.getIndex() + info.getDelimiter().length());
        } else {
            return str.substring(0, info.getIndex());
        }
    }

    // ===================================================================================
    //                                                                            Contains
    //                                                                            ========
    public static boolean contains(String str, String keyword) {
        return containsAll(str, keyword);
    }

    public static boolean containsIgnoreCase(String str, String keyword) {
        return containsAllIgnoreCase(str, keyword);
    }

    public static boolean containsAll(String str, String... keywords) {
        return doContainsAll(false, str, keywords);
    }

    public static boolean containsAllIgnoreCase(String str, String... keywords) {
        return doContainsAll(true, str, keywords);
    }

    protected static boolean doContainsAll(boolean ignoreCase, String str, String... keywords) {
        assertStringNotNull(str);
        if (keywords == null || keywords.length == 0) {
            return false;
        }
        final String filtered = ignoreCase ? str.toLowerCase() : str;
        for (String keyword : keywords) {
            if (ignoreCase) {
                keyword = keyword != null ? keyword.toLowerCase() : null;
            }
            if (keyword == null || !filtered.contains(keyword)) {
                return false;
            }
        }
        return true;
    }

    public static boolean containsAny(String str, String... keywords) {
        return doContainsAny(false, str, keywords);
    }

    public static boolean containsAnyIgnoreCase(String str, String... keywords) {
        return doContainsAny(true, str, keywords);
    }

    protected static boolean doContainsAny(boolean ignoreCase, String str, String... keywords) {
        assertStringNotNull(str);
        if (keywords == null || keywords.length == 0) {
            return false;
        }
        final String filtered = ignoreCase ? str.toLowerCase() : str;
        for (String keyword : keywords) {
            if (ignoreCase) {
                keyword = keyword != null ? keyword.toLowerCase() : null;
            }
            if (keyword != null && filtered.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsOrderedAll(String str, String... keywords) {
        return doContainsOrderedAll(false, str, keywords);
    }

    public static boolean containsOrderedAllIgnoreCase(String str, String... keywords) {
        return doContainsOrderedAll(true, str, keywords);
    }

    protected static boolean doContainsOrderedAll(boolean ignoreCase, String str, String... keywords) {
        assertStringNotNull(str);
        if (keywords == null || keywords.length == 0) {
            return false;
        }
        String current = ignoreCase ? str.toLowerCase() : str;
        for (String keyword : keywords) {
            if (ignoreCase) {
                keyword = keyword != null ? keyword.toLowerCase() : null;
            }
            if (keyword != null && current.contains(keyword)) {
                current = substringFirstRear(current, keyword);
            } else {
                return false;
            }
        }
        return true;
    }

    // -----------------------------------------------------
    //                                          List Element
    //                                          ------------
    public static boolean containsElement(Collection<String> strList, String element) {
        return containsElementAll(strList, element);
    }

    public static boolean containsElementIgnoreCase(Collection<String> strList, String element) {
        return containsElementAllIgnoreCase(strList, element);
    }

    public static boolean containsElementAll(Collection<String> strList, String... elements) {
        return doContainsElementAll(false, strList, elements);
    }

    public static boolean containsElementAllIgnoreCase(Collection<String> strList, String... elements) {
        return doContainsElementAll(true, strList, elements);
    }

    protected static boolean doContainsElementAll(boolean ignoreCase, Collection<String> strList, String... elements) {
        assertStringListNotNull(strList);
        assertElementVaryingNotNull(elements);
        return doContainsElement(true, ignoreCase, ListElementContainsType.EQUAL, strList, elements);
    }

    public static boolean containsElementAny(Collection<String> strList, String... elements) {
        return doContainsElementAny(false, strList, elements);
    }

    public static boolean containsElementAnyIgnoreCase(Collection<String> strList, String... elements) {
        return doContainsElementAny(true, strList, elements);
    }

    protected static boolean doContainsElementAny(boolean ignoreCase, Collection<String> strList, String... elements) {
        assertStringListNotNull(strList);
        assertElementVaryingNotNull(elements);
        return doContainsElement(false, ignoreCase, ListElementContainsType.EQUAL, strList, elements);
    }

    protected static boolean doContainsElement(boolean all, boolean ignoreCase, ListElementContainsType type, Collection<String> strList,
            String... elements) {
        assertStringListNotNull(strList);
        assertElementVaryingNotNull(elements);
        if (elements.length == 0) {
            return false;
        }
        for (String element : elements) {
            boolean exists = false;
            for (String current : strList) {
                final boolean result;
                if (ignoreCase) {
                    if (ListElementContainsType.PREFIX.equals(type)) {
                        result = current != null ? startsWithIgnoreCase(current, element) : false;
                    } else if (ListElementContainsType.SUFFIX.equals(type)) {
                        result = current != null ? endsWithIgnoreCase(current, element) : false;
                    } else if (ListElementContainsType.KEYWORD.equals(type)) {
                        result = current != null ? containsIgnoreCase(current, element) : false;
                    } else {
                        result = equalsIgnoreCase(current, element);
                    }
                } else {
                    if (ListElementContainsType.PREFIX.equals(type)) {
                        result = current != null ? startsWith(current, element) : false;
                    } else if (ListElementContainsType.SUFFIX.equals(type)) {
                        result = current != null ? endsWith(current, element) : false;
                    } else if (ListElementContainsType.KEYWORD.equals(type)) {
                        result = current != null ? contains(current, element) : false;
                    } else {
                        result = equalsPlain(current, element);
                    }
                }
                if (result) {
                    exists = true;
                }
            }
            if (all) {
                if (!exists) {
                    return false;
                }
            } else {
                if (exists) {
                    return true;
                }
            }
        }
        return all;
    }

    protected enum ListElementContainsType {
        EQUAL, KEYWORD, PREFIX, SUFFIX
    }

    // -----------------------------------------------------
    //                                          List Keyword
    //                                          ------------
    public static boolean containsKeyword(Collection<String> strList, String keyword) {
        return containsKeywordAll(strList, keyword);
    }

    public static boolean containsKeywordIgnoreCase(Collection<String> strList, String keyword) {
        return containsKeywordAllIgnoreCase(strList, keyword);
    }

    public static boolean containsKeywordAll(Collection<String> strList, String... keywords) {
        return doContainsKeywordAll(false, strList, keywords);
    }

    public static boolean containsKeywordAllIgnoreCase(Collection<String> strList, String... keywords) {
        return doContainsKeywordAll(true, strList, keywords);
    }

    protected static boolean doContainsKeywordAll(boolean ignoreCase, Collection<String> strList, String... keywords) {
        assertStringListNotNull(strList);
        assertKeywordVaryingNotNull(keywords);
        return doContainsElement(true, ignoreCase, ListElementContainsType.KEYWORD, strList, keywords);
    }

    public static boolean containsKeywordAny(Collection<String> strList, String... keywords) {
        return doContainsKeywordAny(false, strList, keywords);
    }

    public static boolean containsKeywordAnyIgnoreCase(Collection<String> strList, String... keywords) {
        return doContainsKeywordAny(true, strList, keywords);
    }

    protected static boolean doContainsKeywordAny(boolean ignoreCase, Collection<String> strList, String... keywords) {
        assertStringListNotNull(strList);
        assertKeywordVaryingNotNull(keywords);
        return doContainsElement(false, ignoreCase, ListElementContainsType.KEYWORD, strList, keywords);
    }

    // -----------------------------------------------------
    //                                           List Prefix
    //                                           -----------
    public static boolean containsPrefix(Collection<String> strList, String prefix) {
        return containsPrefixAll(strList, prefix);
    }

    public static boolean containsPrefixIgnoreCase(Collection<String> strList, String prefix) {
        return containsPrefixAllIgnoreCase(strList, prefix);
    }

    public static boolean containsPrefixAll(Collection<String> strList, String... prefixes) {
        return doContainsPrefixAll(false, strList, prefixes);
    }

    public static boolean containsPrefixAllIgnoreCase(Collection<String> strList, String... prefixes) {
        return doContainsPrefixAll(true, strList, prefixes);
    }

    protected static boolean doContainsPrefixAll(boolean ignoreCase, Collection<String> strList, String... prefixes) {
        assertStringListNotNull(strList);
        return doContainsElement(true, ignoreCase, ListElementContainsType.PREFIX, strList, prefixes);
    }

    public static boolean containsPrefixAny(Collection<String> strList, String... prefixes) {
        return doContainsPrefixAny(false, strList, prefixes);
    }

    public static boolean containsPrefixAnyIgnoreCase(Collection<String> strList, String... prefixes) {
        return doContainsPrefixAny(true, strList, prefixes);
    }

    protected static boolean doContainsPrefixAny(boolean ignoreCase, Collection<String> strList, String... prefixes) {
        assertStringListNotNull(strList);
        return doContainsElement(false, ignoreCase, ListElementContainsType.PREFIX, strList, prefixes);
    }

    // -----------------------------------------------------
    //                                           List Suffix
    //                                           -----------
    public static boolean containsSuffix(Collection<String> strList, String suffix) {
        return containsSuffixAll(strList, suffix);
    }

    public static boolean containsSuffixIgnoreCase(Collection<String> strList, String suffix) {
        return containsSuffixAllIgnoreCase(strList, suffix);
    }

    public static boolean containsSuffixAll(Collection<String> strList, String... suffixes) {
        return doContainsSuffixAll(false, strList, suffixes);
    }

    public static boolean containsSuffixAllIgnoreCase(Collection<String> strList, String... suffixes) {
        return doContainsSuffixAll(true, strList, suffixes);
    }

    protected static boolean doContainsSuffixAll(boolean ignoreCase, Collection<String> strList, String... suffixes) {
        assertStringListNotNull(strList);
        return doContainsElement(true, ignoreCase, ListElementContainsType.SUFFIX, strList, suffixes);
    }

    public static boolean containsSuffixAny(Collection<String> strList, String... suffixes) {
        return doContainsSuffixAny(false, strList, suffixes);
    }

    public static boolean containsSuffixAnyIgnoreCase(Collection<String> strList, String... suffixes) {
        return doContainsSuffixAny(true, strList, suffixes);
    }

    protected static boolean doContainsSuffixAny(boolean ignoreCase, Collection<String> strList, String... suffixes) {
        assertStringListNotNull(strList);
        return doContainsElement(false, ignoreCase, ListElementContainsType.SUFFIX, strList, suffixes);
    }

    // ===================================================================================
    //                                                                          StartsWith
    //                                                                          ==========
    public static final boolean startsWith(final String str, final String... prefixes) {
        return doStartsWith(false, str, prefixes);
    }

    public static final boolean startsWithIgnoreCase(final String str, final String... prefixes) {
        return doStartsWith(true, str, prefixes);
    }

    protected static final boolean doStartsWith(boolean ignoreCase, String str, final String... prefixes) {
        assertStringNotNull(str);
        if (prefixes == null || prefixes.length == 0) {
            return false;
        }
        final String filtered = ignoreCase ? str.toLowerCase() : str;
        for (String prefix : prefixes) {
            if (prefix != null && filtered.startsWith(ignoreCase ? prefix.toLowerCase() : prefix)) {
                return true;
            }
        }
        return false;
    }

    // ===================================================================================
    //                                                                            EndsWith
    //                                                                            ========
    public static final boolean endsWith(final String str, final String... suffixes) {
        return doEndsWith(false, str, suffixes);
    }

    public static final boolean endsWithIgnoreCase(final String str, final String... suffixes) {
        return doEndsWith(true, str, suffixes);
    }

    protected static final boolean doEndsWith(boolean ignoreCase, String str, final String... suffixes) {
        assertStringNotNull(str);
        if (suffixes == null || suffixes.length == 0) {
            return false;
        }
        final String filtered = ignoreCase ? str.toLowerCase() : str;
        for (String suffix : suffixes) {
            if (suffix != null && filtered.endsWith(ignoreCase ? suffix.toLowerCase() : suffix)) {
                return true;
            }
        }
        return false;
    }

    // ===================================================================================
    //                                                                          HasKeyword
    //                                                                          ==========
    public static final boolean hasKeywordAll(final String keyword, final String... strs) {
        return doHasKeywordAll(false, keyword, strs);
    }

    public static final boolean hasKeywordAllIgnoreCase(final String keyword, final String... strs) {
        return doHasKeywordAll(true, keyword, strs);
    }

    protected static final boolean doHasKeywordAll(boolean ignoreCase, String keyword, final String... strs) {
        assertKeywordNotNull(keyword);
        return doHasKeyword(true, ignoreCase, KeywordType.CONTAIN, keyword, strs);
    }

    public static final boolean hasKeywordAny(final String keyword, final String... strs) {
        return doHasKeywordAny(false, keyword, strs);
    }

    public static final boolean hasKeywordAnyIgnoreCase(final String keyword, final String... strs) {
        return doHasKeywordAny(true, keyword, strs);
    }

    protected static final boolean doHasKeywordAny(boolean ignoreCase, String keyword, final String... strs) {
        assertKeywordNotNull(keyword);
        return doHasKeyword(false, ignoreCase, KeywordType.CONTAIN, keyword, strs);
    }

    protected static final boolean doHasKeyword(boolean all, boolean ignoreCase, KeywordType type, String keyword, final String... strs) {
        assertKeywordNotNull(keyword);
        if (strs == null || strs.length == 0) {
            return false;
        }
        for (String str : strs) {
            final boolean result;
            if (ignoreCase) {
                if (KeywordType.PREFIX.equals(type)) {
                    result = str != null ? startsWithIgnoreCase(str, keyword) : false;
                } else if (KeywordType.SUFFIX.equals(type)) {
                    result = str != null ? endsWithIgnoreCase(str, keyword) : false;
                } else {
                    result = str != null ? containsIgnoreCase(str, keyword) : false;
                }
            } else {
                if (KeywordType.PREFIX.equals(type)) {
                    result = str != null ? startsWith(str, keyword) : false;
                } else if (KeywordType.SUFFIX.equals(type)) {
                    result = str != null ? endsWith(str, keyword) : false;
                } else {
                    result = str != null ? contains(str, keyword) : false;
                }
            }
            if (all) {
                if (!result) {
                    return false;
                }
            } else {
                if (result) {
                    return true;
                }
            }
        }
        return all;
    }

    protected enum KeywordType {
        CONTAIN, PREFIX, SUFFIX
    }

    public static final boolean hasPrefixAll(final String prefix, final String... strs) {
        return doHasPrefixAll(false, prefix, strs);
    }

    public static final boolean hasPrefixAllIgnoreCase(final String prefix, final String... strs) {
        return doHasPrefixAll(true, prefix, strs);
    }

    protected static final boolean doHasPrefixAll(boolean ignoreCase, String prefix, final String... strs) {
        assertPrefixNotNull(prefix);
        return doHasKeyword(true, ignoreCase, KeywordType.PREFIX, prefix, strs);
    }

    public static final boolean hasPrefixAny(final String prefix, final String... strs) {
        return doHasPrefixAny(false, prefix, strs);
    }

    public static final boolean hasPrefixAnyIgnoreCase(final String prefix, final String... strs) {
        return doHasPrefixAny(true, prefix, strs);
    }

    protected static final boolean doHasPrefixAny(boolean ignoreCase, String prefix, final String... strs) {
        assertPrefixNotNull(prefix);
        return doHasKeyword(false, ignoreCase, KeywordType.PREFIX, prefix, strs);
    }

    public static final boolean hasSuffixAll(final String suffix, final String... strs) {
        return doHasSuffixAll(false, suffix, strs);
    }

    public static final boolean hasSuffixAllIgnoreCase(final String suffix, final String... strs) {
        return doHasSuffixAll(true, suffix, strs);
    }

    protected static final boolean doHasSuffixAll(boolean ignoreCase, String suffix, final String... strs) {
        assertSuffixNotNull(suffix);
        return doHasKeyword(true, ignoreCase, KeywordType.SUFFIX, suffix, strs);
    }

    public static final boolean hasSuffixAny(final String suffix, final String... strs) {
        return doHasSuffixAny(false, suffix, strs);
    }

    public static final boolean hasSuffixAnyIgnoreCase(final String suffix, final String... strs) {
        return doHasSuffixAny(true, suffix, strs);
    }

    protected static final boolean doHasSuffixAny(boolean ignoreCase, String suffix, final String... strs) {
        assertSuffixNotNull(suffix);
        return doHasKeyword(false, ignoreCase, KeywordType.SUFFIX, suffix, strs);
    }

    // ===================================================================================
    //                                                                               Count
    //                                                                               =====
    public static int count(String str, String element) {
        return doCount(str, element, false);
    }

    public static int countIgnoreCase(String str, String element) {
        return doCount(str, element, true);
    }

    protected static int doCount(String str, String element, boolean ignoreCase) {
        assertStringNotNull(str);
        assertElementNotNull(element);
        String filteredStr = ignoreCase ? str.toLowerCase() : str;
        final String filteredElement = ignoreCase ? element.toLowerCase() : element;
        int count = 0;
        while (true) {
            final int index = filteredStr.indexOf(filteredElement);
            if (index < 0) {
                break;
            }
            filteredStr = filteredStr.substring(index + filteredElement.length());
            ++count;
        }
        return count;
    }

    // ===================================================================================
    //                                                                              Equals
    //                                                                              ======
    public static boolean equalsIgnoreCase(String str1, String... strs) {
        if (strs != null) {
            for (String element : strs) {
                if ((str1 != null && str1.equalsIgnoreCase(element)) || (str1 == null && element == null)) {
                    return true; // found
                }
            }
            return false;
        } else {
            return str1 == null; // if both are null, it means equal
        }
    }

    public static boolean equalsFlexible(String str1, String... strs) {
        if (strs != null) {
            str1 = str1 != null ? replace(str1, "_", "") : null;
            for (String element : strs) {
                element = element != null ? replace(element, "_", "") : null;
                if ((str1 != null && str1.equalsIgnoreCase(element)) || (str1 == null && element == null)) {
                    return true; // found
                }
            }
            return false;
        } else {
            return str1 == null; // if both are null, it means equal
        }
    }

    public static boolean equalsFlexibleTrimmed(String str1, String... strs) {
        str1 = str1 != null ? str1.trim() : null;
        if (strs != null) {
            String[] trimmedStrs = new String[strs.length];
            for (int i = 0; i < strs.length; i++) {
                final String element = strs[i];
                trimmedStrs[i] = element != null ? element.trim() : null;
            }
            return equalsFlexible(str1, trimmedStrs);
        } else {
            return equalsFlexible(str1, (String[]) null);
        }
    }

    public static boolean equalsPlain(String str1, String... strs) {
        if (strs != null) {
            for (String element : strs) {
                if ((str1 != null && str1.equals(element)) || (str1 == null && element == null)) {
                    return true; // found
                }
            }
            return false;
        } else {
            return str1 == null; // if both are null, it means equal
        }
    }

    // ===================================================================================
    //                                                                    Connect & Remove
    //                                                                    ================
    public static String connectByDelimiter(Collection<String> strList, String delimiter) {
        assertStringListNotNull(strList);
        return doConnectByDelimiter(strList, delimiter, null);
    }

    public static String connectByDelimiterQuoted(Collection<String> strList, String delimiter, String quotation) {
        assertStringListNotNull(strList);
        assertQuotationNotNull(quotation);
        return doConnectByDelimiter(strList, delimiter, quotation);
    }

    protected static String doConnectByDelimiter(Collection<String> strList, String delimiter, String quotation) {
        assertStringListNotNull(strList);
        final StringBuilder sb = new StringBuilder();
        for (String str : strList) {
            if (sb.length() > 0) {
                sb.append(delimiter);
            }
            sb.append(quotation != null ? quoteAnything(str != null ? str : "null", quotation) : str);
        }
        return sb.toString();
    }

    public static String connectPrefix(String str, String prefix, String delimiter) {
        assertStringNotNull(str);
        return is_NotNull_and_NotTrimmedEmpty(prefix) ? prefix + delimiter + str : str;
    }

    public static String connectSuffix(String str, String suffix, String delimiter) {
        assertStringNotNull(str);
        return is_NotNull_and_NotTrimmedEmpty(suffix) ? str + delimiter + suffix : str;
    }

    public static String removePrefix(String str, String prefix) {
        assertStringNotNull(str);
        return startsWith(str, prefix) ? substringFirstRear(str, prefix) : str;
    }

    public static String removePrefixIgnoreCase(String str, String prefix) {
        assertStringNotNull(str);
        return startsWithIgnoreCase(str, prefix) ? substringFirstRearIgnoreCase(str, prefix) : str;
    }

    public static String removeSuffix(String str, String suffix) {
        assertStringNotNull(str);
        return endsWith(str, suffix) ? substringLastFront(str, suffix) : str;
    }

    public static String removeSuffixIgnoreCase(String str, String suffix) {
        assertStringNotNull(str);
        return endsWithIgnoreCase(str, suffix) ? substringLastFrontIgnoreCase(str, suffix) : str;
    }

    // ===================================================================================
    //                                                                                Fill
    //                                                                                ====
    public static String rfill(String str, int size) {
        return doFill(str, size, false, ' ');
    }

    public static String rfill(String str, int size, Character addedChar) {
        return doFill(str, size, false, addedChar);
    }

    public static String lfill(String str, int size) {
        return doFill(str, size, true, ' ');
    }

    public static String lfill(String str, int size, Character addedChar) {
        return doFill(str, size, true, addedChar);
    }

    protected static String doFill(String str, int size, boolean left, Character addedChar) {
        assertStringNotNull(str);
        if (str.length() >= size) {
            return str;
        }
        final int addSize = size - str.length();
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < addSize; i++) {
            sb.append(addedChar);
        }
        if (left) {
            return sb + str;
        } else {
            return str + sb;
        }
    }

    // ===================================================================================
    //                                                                  Quotation Handling
    //                                                                  ==================
    public static boolean isQuotedAnything(String str, String quotation) {
        assertStringNotNull(str);
        assertQuotationNotNull(quotation);
        return isQuotedAnything(str, quotation, quotation);
    }

    public static boolean isQuotedAnything(String str, String beginMark, String endMark) {
        assertStringNotNull(str);
        assertBeginMarkNotNull(beginMark);
        assertEndMarkNotNull(endMark);
        return str.length() > 1 && str.startsWith(beginMark) && str.endsWith(endMark);
    }

    public static boolean isQuotedDouble(String str) {
        assertStringNotNull(str);
        return isQuotedAnything(str, "\"");
    }

    public static boolean isQuotedSingle(String str) {
        assertStringNotNull(str);
        return isQuotedAnything(str, "'");
    }

    public static String quoteAnything(String str, String quotation) {
        assertStringNotNull(str);
        assertQuotationNotNull(quotation);
        return quoteAnything(str, quotation, quotation);
    }

    public static String quoteAnything(String str, String beginMark, String endMark) {
        assertStringNotNull(str);
        assertBeginMarkNotNull(beginMark);
        assertEndMarkNotNull(endMark);
        return beginMark + str + endMark;
    }

    public static String quoteDouble(String str) {
        assertStringNotNull(str);
        return quoteAnything(str, "\"");
    }

    public static String quoteSingle(String str) {
        assertStringNotNull(str);
        return quoteAnything(str, "'");
    }

    public static String unquoteAnything(String str, String quotation) {
        assertStringNotNull(str);
        assertQuotationNotNull(quotation);
        return unquoteAnything(str, quotation, quotation);
    }

    public static String unquoteAnything(String str, String beginMark, String endMark) {
        assertStringNotNull(str);
        assertBeginMarkNotNull(beginMark);
        assertEndMarkNotNull(endMark);
        if (!isQuotedAnything(str, beginMark, endMark)) {
            return str;
        }
        str = substring(str, beginMark.length());
        str = substring(str, 0, str.length() - endMark.length());
        return str;
    }

    public static String unquoteDouble(String str) {
        assertStringNotNull(str);
        return unquoteAnything(str, "\"");
    }

    public static String unquoteSingle(String str) {
        assertStringNotNull(str);
        return unquoteAnything(str, "'");
    }

    // ===================================================================================
    //                                                                  Delimiter Handling
    //                                                                  ==================
    public static final List<DelimiterInfo> extractDelimiterList(final String str, final String delimiter) {
        assertStringNotNull(str);
        assertDelimiterNotNull(delimiter);
        final List<DelimiterInfo> delimiterList = new ArrayList<DelimiterInfo>();
        DelimiterInfo previous = null;
        String rear = str;
        while (true) {
            final int beginIndex = rear.indexOf(delimiter);
            if (beginIndex < 0) {
                break;
            }
            final DelimiterInfo info = new DelimiterInfo();
            info.setBaseString(str);
            info.setDelimiter(delimiter);
            final int absoluteIndex = (previous != null ? previous.getEndIndex() : 0) + beginIndex;
            info.setBeginIndex(absoluteIndex);
            info.setEndIndex(absoluteIndex + delimiter.length());
            if (previous != null) {
                info.setPrevious(previous);
                previous.setNext(info);
            }
            delimiterList.add(info);
            previous = info;
            rear = str.substring(info.getEndIndex());
            continue;
        }
        return delimiterList;
    }

    public static class DelimiterInfo {
        protected String _baseString;
        protected int _beginIndex;
        protected int _endIndex;
        protected String _delimiter;
        protected DelimiterInfo _previous;
        protected DelimiterInfo _next;

        public String substringInterspaceToPrevious() {
            int previousIndex = -1;
            if (_previous != null) {
                previousIndex = _previous.getBeginIndex();
            }
            if (previousIndex >= 0) {
                return _baseString.substring(previousIndex + _previous.getDelimiter().length(), _beginIndex);
            } else {
                return _baseString.substring(0, _beginIndex);
            }
        }

        public String substringInterspaceToNext() {
            int nextIndex = -1;
            if (_next != null) {
                nextIndex = _next.getBeginIndex();
            }
            if (nextIndex >= 0) {
                return _baseString.substring(_endIndex, nextIndex);
            } else {
                return _baseString.substring(_endIndex);
            }
        }

        @Override
        public String toString() {
            return _delimiter + ":(" + _beginIndex + ", " + _endIndex + ")";
        }

        public String getBaseString() {
            return _baseString;
        }

        public void setBaseString(String baseStr) {
            this._baseString = baseStr;
        }

        public int getBeginIndex() {
            return _beginIndex;
        }

        public void setBeginIndex(int beginIndex) {
            this._beginIndex = beginIndex;
        }

        public int getEndIndex() {
            return _endIndex;
        }

        public void setEndIndex(int endIndex) {
            this._endIndex = endIndex;
        }

        public String getDelimiter() {
            return _delimiter;
        }

        public void setDelimiter(String delimiter) {
            this._delimiter = delimiter;
        }

        public DelimiterInfo getPrevious() {
            return _previous;
        }

        public void setPrevious(DelimiterInfo previous) {
            this._previous = previous;
        }

        public DelimiterInfo getNext() {
            return _next;
        }

        public void setNext(DelimiterInfo next) {
            this._next = next;
        }
    }

    public static <ELEMENT> String delimit(Collection<ELEMENT> list, Function<ELEMENT, String> oneArgInLambda, String delimiter) {
        final StringBuilder sb = new StringBuilder();
        int index = 0;
        for (ELEMENT element : list) {
            if (index > 0) {
                sb.append(delimiter);
            }
            sb.append(oneArgInLambda.apply(element));
            ++index;
        }
        return sb.toString();
    }

    // ===================================================================================
    //                                                                      Scope Handling
    //                                                                      ==============
    public static final ScopeInfo extractScopeFirst(final String str, final String beginMark, final String endMark) {
        final List<ScopeInfo> scopeList = doExtractScopeList(str, beginMark, endMark, true);
        if (scopeList == null || scopeList.isEmpty()) {
            return null;
        }
        if (scopeList.size() > 1) {
            String msg = "This method should extract only one scope: " + scopeList;
            throw new IllegalStateException(msg);
        }
        return scopeList.get(0);
    }

    public static final ScopeInfo extractScopeLast(final String str, final String beginMark, final String endMark) {
        final List<ScopeInfo> scopeList = doExtractScopeList(str, beginMark, endMark, false);
        if (scopeList == null || scopeList.isEmpty()) {
            return null;
        }
        return scopeList.get(scopeList.size() - 1);
    }

    public static final List<ScopeInfo> extractScopeList(final String str, final String beginMark, final String endMark) {
        final List<ScopeInfo> scopeList = doExtractScopeList(str, beginMark, endMark, false);
        return scopeList != null ? scopeList : new ArrayList<ScopeInfo>();
    }

    protected static final List<ScopeInfo> doExtractScopeList(final String str, final String beginMark, final String endMark,
            final boolean firstOnly) {
        assertStringNotNull(str);
        assertBeginMarkNotNull(beginMark);
        assertEndMarkNotNull(endMark);
        List<ScopeInfo> resultList = null;
        ScopeInfo previous = null;
        String rear = str;
        while (true) {
            final int beginIndex = rear.indexOf(beginMark);
            if (beginIndex < 0) {
                break;
            }
            rear = rear.substring(beginIndex); // scope begins
            if (rear.length() <= beginMark.length()) {
                break;
            }
            rear = rear.substring(beginMark.length()); // skip begin-mark
            final int endIndex = rear.indexOf(endMark);
            if (endIndex < 0) {
                break;
            }
            final String scope = beginMark + rear.substring(0, endIndex + endMark.length());
            final ScopeInfo info = new ScopeInfo();
            info.setBaseString(str);
            final int absoluteIndex = (previous != null ? previous.getEndIndex() : 0) + beginIndex;
            info.setBeginIndex(absoluteIndex);
            info.setEndIndex(absoluteIndex + scope.length());
            info.setBeginMark(beginMark);
            info.setEndMark(endMark);
            info.setContent(rtrim(ltrim(scope, beginMark), endMark));
            info.setScope(scope);
            if (previous != null) {
                info.setPrevious(previous);
                previous.setNext(info);
            }
            if (resultList == null) {
                resultList = new ArrayList<ScopeInfo>(); // lazy load
            }
            resultList.add(info);
            if (previous == null && firstOnly) {
                break;
            }
            previous = info;
            rear = str.substring(info.getEndIndex());
        }
        return resultList; // nullable if not found to suppress unneeded ArrayList creation
    }

    public static final ScopeInfo extractScopeWide(final String str, final String beginMark, final String endMark) {
        assertStringNotNull(str);
        assertBeginMarkNotNull(beginMark);
        assertEndMarkNotNull(endMark);
        final IndexOfInfo first = indexOfFirst(str, beginMark);
        if (first == null) {
            return null;
        }
        final IndexOfInfo last = indexOfLast(str, endMark);
        if (last == null) {
            return null;
        }
        final String content = str.substring(first.getIndex() + first.getDelimiter().length(), last.getIndex());
        final ScopeInfo info = new ScopeInfo();
        info.setBaseString(str);
        info.setBeginIndex(first.getIndex());
        info.setEndIndex(last.getIndex());
        info.setBeginMark(beginMark);
        info.setEndMark(endMark);
        info.setContent(content);
        info.setScope(beginMark + content + endMark);
        return info;
    }

    public static class ScopeInfo {
        protected String _baseString;
        protected int _beginIndex;
        protected int _endIndex;
        protected String beginMark;
        protected String endMark;
        protected String _content;
        protected String _scope;
        protected ScopeInfo _previous;
        protected ScopeInfo _next;

        public boolean isBeforeScope(int index) {
            return index < _beginIndex;
        }

        public boolean isInScope(int index) {
            return index >= _beginIndex && index <= _endIndex;
        }

        public String replaceContentOnBaseString(String toStr) {
            final List<ScopeInfo> scopeList = takeScopeList();
            final StringBuilder sb = new StringBuilder();
            for (ScopeInfo scope : scopeList) {
                sb.append(scope.substringInterspaceToPrevious());
                sb.append(scope.getBeginMark());
                sb.append(toStr);
                sb.append(scope.getEndMark());
                if (scope.getNext() == null) { // last
                    sb.append(scope.substringInterspaceToNext());
                }
            }
            return sb.toString();
        }

        public String replaceContentOnBaseString(String fromStr, String toStr) {
            final List<ScopeInfo> scopeList = takeScopeList();
            final StringBuilder sb = new StringBuilder();
            for (ScopeInfo scope : scopeList) {
                sb.append(scope.substringInterspaceToPrevious());
                sb.append(scope.getBeginMark());
                sb.append(replace(scope.getContent(), fromStr, toStr));
                sb.append(scope.getEndMark());
                if (scope.getNext() == null) { // last
                    sb.append(scope.substringInterspaceToNext());
                }
            }
            return sb.toString();
        }

        public String replaceInterspaceOnBaseString(String fromStr, String toStr) {
            final List<ScopeInfo> scopeList = takeScopeList();
            final StringBuilder sb = new StringBuilder();
            for (ScopeInfo scope : scopeList) {
                sb.append(replace(scope.substringInterspaceToPrevious(), fromStr, toStr));
                sb.append(scope.getScope());
                if (scope.getNext() == null) { // last
                    sb.append(replace(scope.substringInterspaceToNext(), fromStr, toStr));
                }
            }
            return sb.toString();
        }

        protected List<ScopeInfo> takeScopeList() {
            ScopeInfo scope = this;
            while (true) {
                final ScopeInfo previous = scope.getPrevious();
                if (previous == null) {
                    break;
                }
                scope = previous;
            }
            final List<ScopeInfo> scopeList = new ArrayList<ScopeInfo>();
            scopeList.add(scope);
            while (true) {
                final ScopeInfo next = scope.getNext();
                if (next == null) {
                    break;
                }
                scope = next;
                scopeList.add(next);
            }
            return scopeList;
        }

        public String substringInterspaceToPrevious() {
            int previousEndIndex = -1;
            if (_previous != null) {
                previousEndIndex = _previous.getEndIndex();
            }
            if (previousEndIndex >= 0) {
                return _baseString.substring(previousEndIndex, _beginIndex);
            } else {
                return _baseString.substring(0, _beginIndex);
            }
        }

        public String substringInterspaceToNext() {
            int nextBeginIndex = -1;
            if (_next != null) {
                nextBeginIndex = _next.getBeginIndex();
            }
            if (nextBeginIndex >= 0) {
                return _baseString.substring(_endIndex, nextBeginIndex);
            } else {
                return _baseString.substring(_endIndex);
            }
        }

        public String substringScopeToPrevious() {
            int previousBeginIndex = -1;
            if (_previous != null) {
                previousBeginIndex = _previous.getBeginIndex();
            }
            if (previousBeginIndex >= 0) {
                return _baseString.substring(previousBeginIndex, _endIndex);
            } else {
                return _baseString.substring(0, _endIndex);
            }
        }

        public String substringScopeToNext() {
            int nextEndIndex = -1;
            if (_next != null) {
                nextEndIndex = _next.getEndIndex();
            }
            if (nextEndIndex >= 0) {
                return _baseString.substring(_beginIndex, nextEndIndex);
            } else {
                return _baseString.substring(_beginIndex);
            }
        }

        @Override
        public String toString() {
            return _scope + ":(" + _beginIndex + ", " + _endIndex + ")";
        }

        public String getBaseString() {
            return _baseString;
        }

        public void setBaseString(String baseString) {
            this._baseString = baseString;
        }

        public int getBeginIndex() {
            return _beginIndex;
        }

        public void setBeginIndex(int beginIndex) {
            this._beginIndex = beginIndex;
        }

        public int getEndIndex() {
            return _endIndex;
        }

        public void setEndIndex(int endIndex) {
            this._endIndex = endIndex;
        }

        public String getBeginMark() {
            return beginMark;
        }

        public void setBeginMark(String beginMark) {
            this.beginMark = beginMark;
        }

        public String getEndMark() {
            return endMark;
        }

        public void setEndMark(String endMark) {
            this.endMark = endMark;
        }

        public String getContent() {
            return _content;
        }

        public void setContent(String content) {
            this._content = content;
        }

        public String getScope() {
            return _scope;
        }

        public void setScope(String scope) {
            this._scope = scope;
        }

        public ScopeInfo getPrevious() {
            return _previous;
        }

        public void setPrevious(ScopeInfo previous) {
            this._previous = previous;
        }

        public ScopeInfo getNext() {
            return _next;
        }

        public void setNext(ScopeInfo next) {
            this._next = next;
        }
    }

    public static String removeScope(final String str, final String beginMark, final String endMark) {
        assertStringNotNull(str);
        final StringBuilder sb = new StringBuilder();
        String rear = str;
        while (true) {
            final int beginIndex = rear.indexOf(beginMark);
            if (beginIndex < 0) {
                sb.append(rear);
                break;
            }
            final int endIndex = rear.indexOf(endMark);
            if (endIndex < 0) {
                sb.append(rear);
                break;
            }
            if (beginIndex > endIndex) {
                final int borderIndex = endIndex + endMark.length();
                sb.append(rear.substring(0, borderIndex));
                rear = rear.substring(borderIndex);
                continue;
            }
            sb.append(rear.substring(0, beginIndex));
            rear = rear.substring(endIndex + endMark.length());
        }
        return sb.toString();
    }

    // ===================================================================================
    //                                                                    Initial Handling
    //                                                                    ================
    public static String initCap(String str) {
        assertStringNotNull(str);
        if (is_Null_or_Empty(str)) {
            return str;
        }
        if (str.length() == 1) {
            return str.toUpperCase();
        }
        if (Character.isUpperCase(str.charAt(0))) {
            return str;
        }
        final char[] chars = str.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }

    public static String initCapTrimmed(String str) {
        assertStringNotNull(str);
        return initCap(str.trim());
    }

    public static String initUncap(String str) {
        assertStringNotNull(str);
        if (is_Null_or_Empty(str)) {
            return str;
        }
        if (str.length() == 1) {
            return str.toLowerCase();
        }
        if (Character.isLowerCase(str.charAt(0))) {
            return str;
        }
        final char[] chars = str.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }

    public static String initUncapTrimmed(String str) {
        assertStringNotNull(str);
        return initUncap(str.trim());
    }

    /**
     * Adjust initial character(s) as beans property. <br>
     * Basically same as initUncap() method except only when
     * it starts with two upper case character, for example, 'EMecha'
     * @param capitalizedName The capitalized name for beans property. (NotNull)
     * @return The name as beans property that initial is adjusted. (NotNull)
     */
    public static String initBeansProp(String capitalizedName) { // according to Java Beans rule
        assertObjectNotNull("capitalizedName", capitalizedName);
        if (is_Null_or_TrimmedEmpty(capitalizedName)) {
            return capitalizedName;
        }
        if (isInitTwoUpperCase(capitalizedName)) { // for example, 'EMecha'
            return capitalizedName;
        }
        return initUncap(capitalizedName);
    }

    public static boolean isInitLowerCase(String str) {
        assertStringNotNull(str);
        if (is_Null_or_Empty(str)) {
            return false;
        }
        return isLowerCase(str.charAt(0));
    }

    public static boolean isInitTwoLowerCase(String str) {
        assertStringNotNull(str);
        if (str.length() < 2) {
            return false;
        }
        return isLowerCase(str.charAt(0), str.charAt(1));
    }

    public static boolean isInitUpperCase(String str) {
        assertStringNotNull(str);
        if (is_Null_or_Empty(str)) {
            return false;
        }
        return isUpperCase(str.charAt(0));
    }

    public static boolean isInitTwoUpperCase(String str) {
        assertStringNotNull(str);
        if (str.length() < 2) {
            return false;
        }
        return isUpperCase(str.charAt(0), str.charAt(1));
    }

    // ===================================================================================
    //                                                                       Name Handling
    //                                                                       =============
    public static String camelize(String decamelName) {
        assertDecamelNameNotNull(decamelName);
        return doCamelize(decamelName, "_");
    }

    public static String camelize(String decamelName, String... delimiters) {
        assertDecamelNameNotNull(decamelName);
        String name = decamelName;
        for (String delimiter : delimiters) {
            name = doCamelize(name, delimiter);
        }
        return name;
    }

    protected static String doCamelize(String decamelName, String delimiter) {
        assertDecamelNameNotNull(decamelName);
        assertDelimiterNotNull(delimiter);
        if (is_Null_or_TrimmedEmpty(decamelName)) {
            return decamelName;
        }
        final StringBuilder sb = new StringBuilder();
        final List<String> partList = splitListTrimmed(decamelName, delimiter);
        for (String part : partList) {
            boolean allUpperCase = true;
            for (int i = 1; i < part.length(); ++i) {
                if (isLowerCase(part.charAt(i))) {
                    allUpperCase = false;
                }
            }
            if (allUpperCase) {
                part = part.toLowerCase();
            }
            sb.append(initCap(part));
        }
        return sb.toString();
    }

    // *DBFlute doesn't decamelize a table and column name
    // (allowed to convert decamel name to a camel name in this world)
    public static String decamelize(String camelName) {
        assertCamelNameNotNull(camelName);
        return doDecamelize(camelName, "_");
    }

    public static String decamelize(String camelName, String delimiter) {
        assertCamelNameNotNull(camelName);
        assertDelimiterNotNull(delimiter);
        return doDecamelize(camelName, delimiter);
    }

    protected static String doDecamelize(String camelName, String delimiter) {
        assertCamelNameNotNull(camelName);
        if (is_Null_or_TrimmedEmpty(camelName)) {
            return camelName;
        }
        if (camelName.length() == 1) {
            return camelName.toUpperCase();
        }
        final StringBuilder sb = new StringBuilder();
        boolean previousLower = false;
        int pos = 0;
        for (int i = 1; i < camelName.length(); i++) {
            final char currentChar = camelName.charAt(i);
            if (isUpperCase(currentChar)) {
                if (sb.length() > 0 && previousLower) { // check target length not to be FOO -> F_O_O
                    sb.append(delimiter);
                }
                sb.append(camelName.substring(pos, i).toUpperCase());
                pos = i;
                previousLower = false;
            } else if (isLowerCase(currentChar)) {
                previousLower = true;
            }
        }
        if (sb.length() > 0 && previousLower) {
            sb.append(delimiter);
        }
        sb.append(camelName.substring(pos, camelName.length()).toUpperCase());
        final String generated = sb.toString();
        return replace(generated, delimiter + delimiter, delimiter); // final adjustment
    }

    // ===================================================================================
    //                                                                        SQL Handling
    //                                                                        ============
    /**
     * Remove empty lines. <br>
     * And CR is removed.
     * @param str The target string. (NotNull)
     * @return The filtered string. (NotNull)
     */
    public static String removeEmptyLine(String str) {
        assertStringNotNull(str);
        final StringBuilder sb = new StringBuilder();
        final List<String> lineList = splitList(str, "\n");
        for (String line : lineList) {
            if (is_Null_or_TrimmedEmpty(line)) {
                continue; // skip
            }
            sb.append(removeCR(line)).append("\n");
        }
        final String filtered = sb.toString();
        return filtered.substring(0, filtered.length() - "\n".length());
    }

    /**
     * Remove block comments.
     * @param sql The string of SQL. (NotNull)
     * @return The filtered string. (NotNull)
     */
    public static String removeBlockComment(String sql) {
        assertSqlNotNull(sql);
        return removeScope(sql, "/*", "*/");
    }

    /**
     * Remove line comments. <br>
     * And CR is removed.
     * @param sql The string of SQL. (NotNull)
     * @return The filtered string. (NotNull)
     */
    public static String removeLineComment(String sql) { // with removing CR!
        assertSqlNotNull(sql);
        final StringBuilder sb = new StringBuilder();
        final List<String> lineList = splitList(sql, "\n");
        for (String line : lineList) {
            if (line == null) {
                continue;
            }
            line = removeCR(line); // remove CR!
            if (line.trim().startsWith("--")) {
                continue;
            }
            final List<DelimiterInfo> delimiterList = extractDelimiterList(line, "--");
            int realIndex = -1;
            indexLoop: for (DelimiterInfo delimiter : delimiterList) {
                final List<ScopeInfo> scopeList = extractScopeList(line, "/*", "*/");
                final int delimiterIndex = delimiter.getBeginIndex();
                for (ScopeInfo scope : scopeList) {
                    if (scope.isBeforeScope(delimiterIndex)) {
                        break;
                    }
                    if (scope.isInScope(delimiterIndex)) {
                        continue indexLoop;
                    }
                }
                // found
                realIndex = delimiterIndex;
            }
            if (realIndex >= 0) {
                line = line.substring(0, realIndex);
            }
            sb.append(line).append("\n");
        }
        final String filtered = sb.toString();
        return filtered.substring(0, filtered.length() - "\n".length());
    }

    protected static String removeCR(String str) {
        if (str == null) {
            return null;
        }
        return str.replaceAll("\r", "");
    }

    // ===================================================================================
    //                                                                     Indent Handling
    //                                                                     ===============
    public static String indent(int size) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            sb.append(" ");
        }
        return sb.toString();
    }

    public static String indent(int size, String str) {
        final List<String> lineList = splitList(removeCR(str), "\n");
        final StringBuilder sb = new StringBuilder();
        int index = 0;
        for (String element : lineList) {
            if (index > 0) {
                sb.append("\n");
            }
            sb.append(indent(size)).append(element);
            ++index;
        }
        return sb.toString();
    }

    // ===================================================================================
    //                                                                  Character Handling
    //                                                                  ==================
    public static boolean isAlphabetHarfAll(String str) {
        return isAnyCharAll(str, _alphabetHarfCharSet);
    }

    public static boolean isAlphabetHarfLowerAll(String str) {
        return isAnyCharAll(str, _alphabetHarfLowerCharSet);
    }

    public static boolean isAlphabetHarfUpperAll(String str) {
        return isAnyCharAll(str, _alphabetHarfUpperCharSet);
    }

    public static boolean isNumberHarfAll(String str) {
        return isAnyCharAll(str, _numberHarfCharSet);
    }

    public static boolean isAlphabetNumberHarfAll(String str) {
        return isAnyCharAll(str, _alphabetHarfNumberHarfCharSet);
    }

    public static boolean isAlphabetNumberHarfAllOr(String str, Character... addedChars) {
        final Set<Character> charSet = new HashSet<Character>(_alphabetHarfNumberHarfCharSet);
        for (Character ch : addedChars) {
            charSet.add(ch);
        }
        return isAnyCharAll(str, charSet);
    }

    public static boolean isAlphabetNumberHarfLowerAll(String str) {
        return isAnyCharAll(str, _alphabetNumberHarfLowerCharSet);
    }

    public static boolean isAlphabetNumberHarfUpperAll(String str) {
        return isAnyCharAll(str, _alphabetNumberHarfUpperCharSet);
    }

    protected static boolean isAnyCharAll(String str, Set<Character> charSet) {
        if (is_Null_or_Empty(str)) {
            return false;
        }
        final char[] chAry = str.toCharArray();
        for (int i = 0; i < chAry.length; i++) {
            final char ch = chAry[i];
            if (!charSet.contains(ch)) {
                return false;
            }
        }
        return true;
    }

    // -----------------------------------------------------
    //                                            Lower Case
    //                                            ----------
    protected static boolean isLowerCase(char ch) {
        return Character.isLowerCase(ch);
    }

    protected static boolean isLowerCase(char ch1, char ch2) {
        return isLowerCase(ch1) && isLowerCase(ch2);
    }

    public static boolean isLowerCaseAll(String str) {
        assertStringNotNull(str);
        final int length = str.length();
        if (length == 0) {
            return false;
        }
        for (int i = 0; i < length; ++i) {
            if (!isLowerCase(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isLowerCaseAny(String str) {
        assertStringNotNull(str);
        final int length = str.length();
        if (length == 0) {
            return false;
        }
        for (int i = 0; i < length; ++i) {
            if (isLowerCase(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    // -----------------------------------------------------
    //                                            Upper Case
    //                                            ----------
    protected static boolean isUpperCase(char ch) {
        return Character.isUpperCase(ch);
    }

    protected static boolean isUpperCase(char ch1, char ch2) {
        return isUpperCase(ch1) && isUpperCase(ch2);
    }

    public static boolean isUpperCaseAll(String str) {
        assertStringNotNull(str);
        final int length = str.length();
        if (length == 0) {
            return false;
        }
        for (int i = 0; i < length; ++i) {
            if (!isUpperCase(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isUpperCaseAny(String str) {
        assertStringNotNull(str);
        final int length = str.length();
        if (length == 0) {
            return false;
        }
        for (int i = 0; i < length; ++i) {
            if (isUpperCase(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    // ===================================================================================
    //                                                                       Assert Helper
    //                                                                       =============
    protected static void assertStringNotNull(String str) {
        assertObjectNotNull("str", str);
    }

    protected static void assertStringListNotNull(Collection<String> strList) {
        assertObjectNotNull("strList", strList);
    }

    protected static void assertElementNotNull(String element) {
        assertObjectNotNull("element", element);
    }

    protected static void assertElementVaryingNotNull(String[] elements) {
        assertObjectNotNull("elements", elements);
    }

    protected static void assertKeywordNotNull(String keyword) {
        assertObjectNotNull("keyword", keyword);
    }

    protected static void assertKeywordVaryingNotNull(String[] keywords) {
        assertObjectNotNull("keywords", keywords);
    }

    protected static void assertPrefixNotNull(String prefix) {
        assertObjectNotNull("prefix", prefix);
    }

    protected static void assertSuffixNotNull(String suffix) {
        assertObjectNotNull("suffix", suffix);
    }

    protected static void assertFromToMapNotNull(Map<String, String> fromToMap) {
        assertObjectNotNull("fromToMap", fromToMap);
    }

    protected static void assertDelimiterNotNull(String delimiter) {
        assertObjectNotNull("delimiter", delimiter);
    }

    protected static void assertFromStringNotNull(String fromStr) {
        assertObjectNotNull("fromStr", fromStr);
    }

    protected static void assertToStringNotNull(String toStr) {
        assertObjectNotNull("toStr", toStr);
    }

    protected static void assertQuotationNotNull(String quotation) {
        assertObjectNotNull("quotation", quotation);
    }

    protected static void assertBeginMarkNotNull(String beginMark) {
        assertObjectNotNull("beginMark", beginMark);
    }

    protected static void assertEndMarkNotNull(String endMark) {
        assertObjectNotNull("endMark", endMark);
    }

    protected static void assertDecamelNameNotNull(String decamelName) {
        assertObjectNotNull("decamelName", decamelName);
    }

    protected static void assertCamelNameNotNull(String camelName) {
        assertObjectNotNull("camelName", camelName);
    }

    protected static void assertSqlNotNull(String sql) {
        assertObjectNotNull("sql", sql);
    }

    /**
     * Assert that the object is not null.
     * @param variableName The check name of variable for message. (NotNull)
     * @param value The checked value. (NotNull)
     * @throws IllegalArgumentException When the argument is null.
     */
    protected static void assertObjectNotNull(String variableName, Object value) {
        if (variableName == null) {
            String msg = "The value should not be null: variableName=null value=" + value;
            throw new IllegalArgumentException(msg);
        }
        if (value == null) {
            String msg = "The value should not be null: variableName=" + variableName;
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Assert that the entity is not null and not trimmed empty.
     * @param variableName The check name of variable for message. (NotNull)
     * @param value The checked value. (NotNull)
     */
    protected static void assertStringNotNullAndNotTrimmedEmpty(String variableName, String value) {
        assertObjectNotNull("variableName", variableName);
        assertObjectNotNull("value", value);
        if (value.trim().length() == 0) {
            String msg = "The value should not be empty: variableName=" + variableName + " value=" + value;
            throw new IllegalArgumentException(msg);
        }
    }
}
