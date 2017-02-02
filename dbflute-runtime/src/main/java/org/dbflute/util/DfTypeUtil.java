/*
 * Copyright 2014-2017 the original author or authors.
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.dbflute.system.DBFluteSystem;

/**
 * @author modified by jflute (originated in Seasar2)
 */
public final class DfTypeUtil {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String HYPHENED_DATE_PATTERN = "yyyy-MM-dd";
    public static final String SLASHED_DATE_PATTERN = "yyyy/MM/dd";
    public static final String COLONED_TIME_PATTERN = "HH:mm:ss";
    public static final String PLAIN_MILLIS_PATTERN = "SSS";
    public static final String HYPHENED_TIMESTAMP_PATTERN;
    static {
        HYPHENED_TIMESTAMP_PATTERN = HYPHENED_DATE_PATTERN + " " + COLONED_TIME_PATTERN + "." + PLAIN_MILLIS_PATTERN;
    }
    public static final String SLASHED_TIMESTAMP_PATTERN;
    static {
        SLASHED_TIMESTAMP_PATTERN = SLASHED_DATE_PATTERN + " " + COLONED_TIME_PATTERN + "." + PLAIN_MILLIS_PATTERN;
    }
    // *hyphen basis in program, so the default patterns are hyphened
    // while, slash basis in human view (so HandyDate#toDisp() uses slashed)
    public static final String DEFAULT_DATE_PATTERN = HYPHENED_DATE_PATTERN;
    public static final String DEFAULT_TIMESTAMP_PATTERN = HYPHENED_TIMESTAMP_PATTERN;
    public static final String DEFAULT_TIME_PATTERN = COLONED_TIME_PATTERN;

    protected static final String NULL = "null";
    protected static final String[] EMPTY_STRINGS = new String[] {};
    protected static final long GMT_AD_ORIGIN_MILLISECOND;
    static {
        final Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        cal.set(1, 0, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        // AD0001/01/01 00:00:00.000
        GMT_AD_ORIGIN_MILLISECOND = cal.getTimeInMillis();

        // *the value of millisecond may depend on JDK implementation
    }

    private static final char[] ENCODE_TABLE = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
            'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
            'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/' };

    private static final char PAD = '=';

    private static final byte[] DECODE_TABLE = new byte[128];
    static {
        for (int i = 0; i < DECODE_TABLE.length; i++) {
            DECODE_TABLE[i] = Byte.MAX_VALUE;
        }
        for (int i = 0; i < ENCODE_TABLE.length; i++) {
            DECODE_TABLE[ENCODE_TABLE[i]] = (byte) i;
        }
    }

    // ===================================================================================
    //                                                                              String
    //                                                                              ======
    // -----------------------------------------------------
    //                                            toString()
    //                                            ----------
    /**
     * Convert the object to the instance that is string.
     * <pre>
     * e.g.
     *  date       :: use the default time-zone.
     *  byte array :: encode as base64.
     *  exception  :: convert to stack-trace.
     * </pre>
     * @param obj The parsed object. (NullAllowed: if null, returns null, and keep empty)
     * @return The converted string. (NullAllowed: when the argument is null)
     */
    public static String toString(Object obj) {
        return doConvertToString(obj, (String) null);
    }

    /**
     * Convert the object to the instance that is string by the pattern.
     * <pre>
     * e.g.
     *  date       :: use the default time-zone.
     *  byte array :: encode as base64.
     *  exception  :: convert to stack-trace.
     * </pre>
     * @param obj The parsed object. (NullAllowed: if null, returns null, and keep empty)
     * @param pattern The pattern format to parse for e.g. number, date. (NotNull)
     * @return The converted string. (NullAllowed: when the argument is null)
     */
    public static String toString(Object obj, String pattern) {
        assertPatternNotNull("toString()", pattern);
        return doConvertToString(obj, pattern);
    }

    protected static String doConvertToString(Object obj, String pattern) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof String) {
            return (String) obj;
        } else if (obj instanceof Number) {
            return doConvertToStringNumber((Number) obj, pattern);
        } else if (obj instanceof LocalDate) {
            return doConvertToStringLocalDate((LocalDate) obj, pattern);
        } else if (obj instanceof LocalDateTime) {
            return doConvertToStringLocalDateTime((LocalDateTime) obj, pattern);
        } else if (obj instanceof LocalTime) {
            return doConvertToStringLocalTime((LocalTime) obj, pattern);
        } else if (obj instanceof Time) {
            final String realPattern = pattern != null ? pattern : DEFAULT_TIME_PATTERN;
            return doConvertToStringDate((Time) obj, (TimeZone) null, realPattern, (Locale) null);
        } else if (obj instanceof Date) {
            return doConvertToStringDate((Date) obj, (TimeZone) null, pattern, (Locale) null);
        } else if (obj instanceof Calendar) {
            return doConvertToStringDate(((Calendar) obj).getTime(), (TimeZone) null, pattern, (Locale) null);
        } else if (obj instanceof byte[]) {
            return encodeAsBase64((byte[]) obj);
        } else if (obj instanceof Throwable) {
            return toStringStackTrace((Throwable) obj);
        } else {
            return obj.toString();
        }
    }

    // -----------------------------------------------------
    //                                        Specified Type
    //                                        --------------
    /**
     * Convert the number to the instance that is string by the pattern.
     * @param number The parsed number to be string. (NullAllowed: if null, returns null)
     * @param pattern The pattern format to parse as number. (NotNull)
     * @return The converted string. (NullAllowed: when the argument is null)
     */
    public static String toStringNumber(Number number, String pattern) {
        assertPatternNotNull("toStringNumber()", pattern);
        return number != null ? doConvertToStringNumber(number, pattern) : null;
    }

    protected static String doConvertToStringNumber(Number value, String pattern) {
        if (pattern != null) {
            return createDecimalFormat(pattern).format(value);
        }
        return value.toString();
    }

    /**
     * Convert the local date to the instance that is string by the pattern.
     * @param date The parsed local date to be string. (NullAllowed: if null, returns null)
     * @param pattern The pattern format to parse as local date. (NotNull)
     * @return The converted string. (NullAllowed: when the argument is null)
     */
    public static String toStringDate(LocalDate date, String pattern) {
        assertPatternNotNull("toStringDate()", pattern);
        return date != null ? doConvertToStringLocalDate(date, pattern) : null;
    }

    protected static String doConvertToStringLocalDate(LocalDate value, String pattern) {
        final String realPattern = pattern != null ? pattern : DEFAULT_DATE_PATTERN;
        return value.format(DateTimeFormatter.ofPattern(realPattern, chooseRealLocale(null)));
    }

    /**
     * Convert the local date-time to the instance that is string by the pattern.
     * @param date The parsed local date-time to be string. (NullAllowed: if null, returns null)
     * @param pattern The pattern format to parse as local date-time. (NotNull)
     * @return The converted string. (NullAllowed: when the argument is null)
     */
    public static String toStringDate(LocalDateTime date, String pattern) {
        assertPatternNotNull("toStringDate()", pattern);
        return date != null ? doConvertToStringLocalDateTime(date, pattern) : null;
    }

    protected static String doConvertToStringLocalDateTime(LocalDateTime value, String pattern) {
        final String realPattern = pattern != null ? pattern : DEFAULT_TIMESTAMP_PATTERN; // only millisecond (not nanosecond) as default
        return value.format(DateTimeFormatter.ofPattern(realPattern, chooseRealLocale(null)));
    }

    /**
     * Convert the local time to the instance that is string by the pattern.
     * @param date The parsed local time to be string. (NullAllowed: if null, returns null)
     * @param pattern The pattern format to parse as local time. (NotNull)
     * @return The converted string. (NullAllowed: when the argument is null)
     */
    public static String toStringDate(LocalTime date, String pattern) {
        assertPatternNotNull("toStringDate()", pattern);
        return date != null ? doConvertToStringLocalTime(date, pattern) : null;
    }

    protected static String doConvertToStringLocalTime(LocalTime value, String pattern) {
        final String realPattern = pattern != null ? pattern : DEFAULT_TIME_PATTERN; // not use nanosecond part as default
        return value.format(DateTimeFormatter.ofPattern(realPattern, chooseRealLocale(null)));
    }

    public static String toStringDate(Date value, TimeZone timeZone, String pattern, Locale locale) {
        assertTimeZoneNotNull("toStringDate()", timeZone);
        assertPatternNotNull("toStringDate()", pattern);
        assertLocaleNotNull("toStringDate()", locale);
        return value != null ? doConvertToStringDate(value, timeZone, pattern, locale) : null;
    }

    protected static String doConvertToStringDate(Date value, TimeZone timeZone, String pattern, Locale locale) {
        final String realPattern;
        if (value instanceof Time && pattern == null) {
            realPattern = DEFAULT_TIME_PATTERN;
        } else {
            realPattern = pattern; // null or specified (if null, default pattern of formatter)
        }
        return doCreateDateFormat(realPattern, timeZone, locale, false).format(value);
    }

    public static String toStringStackTrace(Throwable cause) {
        StringWriter sw = null;
        try {
            sw = new StringWriter();
            cause.printStackTrace(new PrintWriter(sw));
            return sw.toString();
        } finally {
            if (sw != null) {
                try {
                    sw.close();
                } catch (IOException ignored) {}
            }
        }
    }

    // -----------------------------------------------------
    //                                           Class Title
    //                                           -----------
    /**
     * Convert the object to class title name.
     * <pre>
     * o com.example.Foo to Foo
     * o com.example.Foo$Bar to Foo$Bar
     * o com.example.Foo$1 to Foo$1
     * o Foo to Foo
     * o Foo$Bar to Foo$Bar
     * </pre>
     * If the object is Class, it uses Class.getName() as convert target string. <br>
     * If the object is String, it uses it directly as convert target string. <br>
     * If the object is the other object, it uses obj.getClass().getName() as convert target string.
     * @param obj The target object. String or Class are treated as special. (NullAllowed: if null, returns null)
     * @return The string as class title. (NullAllowed: when the argument is null)
     */
    public static String toClassTitle(Object obj) {
        if (obj == null) {
            return null;
        }
        final String fqcn;
        if (obj instanceof Class<?>) {
            fqcn = ((Class<?>) obj).getName();
        } else if (obj instanceof String) {
            fqcn = (String) obj;
        } else {
            fqcn = obj.getClass().getName();
        }
        if (fqcn == null || fqcn.trim().length() == 0) {
            return fqcn;
        }
        final int dotLastIndex = fqcn.lastIndexOf(".");
        if (dotLastIndex < 0) {
            return fqcn;
        }
        return fqcn.substring(dotLastIndex + ".".length());
    }

    // -----------------------------------------------------
    //                                                Encode
    //                                                ------
    public static String encodeAsBase64(final byte[] inData) {
        if (inData == null) {
            return null;
        }
        if (inData.length == 0) {
            return "";
        }
        final int mod = inData.length % 3;
        final int num = inData.length / 3;
        final char[] outData;
        if (mod != 0) {
            outData = new char[(num + 1) * 4];
        } else {
            outData = new char[num * 4];
        }
        for (int i = 0; i < num; i++) {
            encode(inData, i * 3, outData, i * 4);
        }
        switch (mod) {
        case 1:
            encode2pad(inData, num * 3, outData, num * 4);
            break;
        case 2:
            encode1pad(inData, num * 3, outData, num * 4);
            break;
        }
        return new String(outData);
    }

    public static byte[] decodeAsBase64(final String inData) {
        int num = (inData.length() / 4) - 1;
        int lastBytes = getLastBytes(inData);
        byte[] outData = new byte[num * 3 + lastBytes];
        for (int i = 0; i < num; i++) {
            decode(inData, i * 4, outData, i * 3);
        }
        switch (lastBytes) {
        case 1:
            decode1byte(inData, num * 4, outData, num * 3);
            break;
        case 2:
            decode2byte(inData, num * 4, outData, num * 3);
            break;
        default:
            decode(inData, num * 4, outData, num * 3);
        }
        return outData;
    }

    private static void encode(final byte[] inData, final int inIndex, final char[] outData, final int outIndex) {
        int i = ((inData[inIndex] & 0xff) << 16) + ((inData[inIndex + 1] & 0xff) << 8) + (inData[inIndex + 2] & 0xff);
        outData[outIndex] = ENCODE_TABLE[i >> 18];
        outData[outIndex + 1] = ENCODE_TABLE[(i >> 12) & 0x3f];
        outData[outIndex + 2] = ENCODE_TABLE[(i >> 6) & 0x3f];
        outData[outIndex + 3] = ENCODE_TABLE[i & 0x3f];
    }

    private static void encode2pad(final byte[] inData, final int inIndex, final char[] outData, final int outIndex) {
        int i = inData[inIndex] & 0xff;
        outData[outIndex] = ENCODE_TABLE[i >> 2];
        outData[outIndex + 1] = ENCODE_TABLE[(i << 4) & 0x3f];
        outData[outIndex + 2] = PAD;
        outData[outIndex + 3] = PAD;
    }

    private static void encode1pad(final byte[] inData, final int inIndex, final char[] outData, final int outIndex) {
        int i = ((inData[inIndex] & 0xff) << 8) + (inData[inIndex + 1] & 0xff);
        outData[outIndex] = ENCODE_TABLE[i >> 10];
        outData[outIndex + 1] = ENCODE_TABLE[(i >> 4) & 0x3f];
        outData[outIndex + 2] = ENCODE_TABLE[(i << 2) & 0x3f];
        outData[outIndex + 3] = PAD;
    }

    private static void decode(final String inData, final int inIndex, final byte[] outData, final int outIndex) {
        byte b0 = DECODE_TABLE[inData.charAt(inIndex)];
        byte b1 = DECODE_TABLE[inData.charAt(inIndex + 1)];
        byte b2 = DECODE_TABLE[inData.charAt(inIndex + 2)];
        byte b3 = DECODE_TABLE[inData.charAt(inIndex + 3)];
        outData[outIndex] = (byte) (b0 << 2 & 0xfc | b1 >> 4 & 0x3);
        outData[outIndex + 1] = (byte) (b1 << 4 & 0xf0 | b2 >> 2 & 0xf);
        outData[outIndex + 2] = (byte) (b2 << 6 & 0xc0 | b3 & 0x3f);
    }

    private static void decode1byte(final String inData, final int inIndex, final byte[] outData, final int outIndex) {
        byte b0 = DECODE_TABLE[inData.charAt(inIndex)];
        byte b1 = DECODE_TABLE[inData.charAt(inIndex + 1)];
        outData[outIndex] = (byte) (b0 << 2 & 0xfc | b1 >> 4 & 0x3);
    }

    private static void decode2byte(final String inData, final int inIndex, final byte[] outData, final int outIndex) {
        byte b0 = DECODE_TABLE[inData.charAt(inIndex)];
        byte b1 = DECODE_TABLE[inData.charAt(inIndex + 1)];
        byte b2 = DECODE_TABLE[inData.charAt(inIndex + 2)];
        outData[outIndex] = (byte) (b0 << 2 & 0xfc | b1 >> 4 & 0x3);
        outData[outIndex + 1] = (byte) (b1 << 4 & 0xf0 | b2 >> 2 & 0xf);
    }

    private static int getLastBytes(final String inData) {
        int len = inData.length();
        if (inData.charAt(len - 2) == PAD) {
            return 1;
        } else if (inData.charAt(len - 1) == PAD) {
            return 2;
        } else {
            return 3;
        }
    }

    // -----------------------------------------------------
    //                                                 Array
    //                                                 -----
    public static String[] emptyStrings() {
        return EMPTY_STRINGS;
    }

    // ===================================================================================
    //                                                                              Number
    //                                                                              ======
    /**
     * Convert to number object.
     * @param obj The resource of number. (NullAllowed: if null, returns null)
     * @param type The type of number. (NotNull)
     * @return The number object from resource. (NullAllowed: if type is not number, returns null)
     */
    public static Number toNumber(Object obj, Class<?> type) {
        if (obj == null) {
            return null;
        }
        // Integer, Long and BigDecimal are prior
        if (type == Integer.class) {
            return toInteger(obj);
        } else if (type == Long.class) {
            return toLong(obj);
        } else if (type == BigDecimal.class) {
            return toBigDecimal(obj);
        } else if (type == Double.class) {
            return toDouble(obj);
        } else if (type == Float.class) {
            return toFloat(obj);
        } else if (type == Short.class) {
            return toShort(obj);
        } else if (type == Byte.class) {
            return toByte(obj);
        } else if (type == BigInteger.class) {
            return toBigInteger(obj);
        }
        return null; // could not convert
    }

    // -----------------------------------------------------
    //                                             Normalize
    //                                             ---------
    protected static String normalize(String value) {
        return normalize(value, Locale.getDefault());
    }

    protected static String normalize(String value, Locale locale) {
        if (value == null) {
            return null;
        }
        final DecimalFormatSymbols symbols = getDecimalFormatSymbols(locale);
        final char groupingSep = symbols.getGroupingSeparator();
        final char decimalSep = symbols.getDecimalSeparator();
        final StringBuilder sb = new StringBuilder(20);
        for (int i = 0; i < value.length(); ++i) {
            char c = value.charAt(i);
            if (c == groupingSep) {
                continue;
            } else if (c == decimalSep) {
                c = '.';
            }
            sb.append(c);
        }
        return sb.toString();
    }

    // -----------------------------------------------------
    //                                          NumberFormat
    //                                          ------------
    public static DecimalFormat createDecimalFormat(String pattern) {
        final Locale realLocale = chooseRealLocale(null);
        final DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(realLocale);
        return new DecimalFormat(pattern, symbols);
    }

    // ===================================================================================
    //                                                                             Integer
    //                                                                             =======
    /**
     * @param obj The resource value to integer. (NullAllowed)
     * @return The value as integer. (NullAllowed: if null or empty, returns null)
     * @throws NumberFormatException When the object cannot be parsed.
     */
    public static Integer toInteger(Object obj) {
        return doConvertToInteger(obj, null);
    }

    public static Integer toInteger(Object obj, String pattern) {
        assertPatternNotNull("toInteger()", pattern);
        return doConvertToInteger(obj, pattern);
    }

    protected static Integer doConvertToInteger(Object obj, String pattern) {
        if (obj == null) {
            return null;
        } else if (obj instanceof Integer) {
            return (Integer) obj;
        } else if (obj instanceof Number) {
            return Integer.valueOf(((Number) obj).intValue());
        } else if (obj instanceof String) {
            return doParseStringAsInteger((String) obj);
        } else if (obj instanceof java.util.Date) {
            if (pattern != null) {
                final DateFormat dateFormat = createDateFormat(pattern);
                return Integer.valueOf(dateFormat.format(obj));
            }
            return Integer.valueOf((int) ((java.util.Date) obj).getTime());
        } else if (obj instanceof Boolean) {
            return ((Boolean) obj).booleanValue() ? Integer.valueOf(1) : Integer.valueOf(0);
        } else if (obj instanceof byte[]) {
            return toInteger(toSerializable((byte[]) obj)); // recursive
        } else {
            return doParseStringAsInteger(obj.toString());
        }
    }

    protected static Integer doParseStringAsInteger(String str) {
        if (str == null || str.trim().length() == 0) {
            return null;
        }
        return Integer.valueOf(normalize(str));
    }

    public static int toPrimitiveInt(Object obj) {
        return doConvertToPrimitiveInt(obj, null);
    }

    public static int toPrimitiveInt(Object obj, String pattern) {
        assertPatternNotNull("toPrimitiveInt()", pattern);
        return doConvertToPrimitiveInt(obj, pattern);
    }

    protected static int doConvertToPrimitiveInt(Object obj, String pattern) {
        final Integer wrapper = doConvertToInteger(obj, pattern);
        return wrapper != null ? wrapper.intValue() : 0;
    }

    // ===================================================================================
    //                                                                                Long
    //                                                                                ====
    /**
     * @param obj The resource value to long. (NullAllowed)
     * @return The value as long. (NullAllowed: if null or empty, returns null)
     * @throws NumberFormatException When the object cannot be parsed.
     */
    public static Long toLong(Object obj) {
        return doConvertToLong(obj, null);
    }

    public static Long toLong(Object obj, String pattern) {
        assertPatternNotNull("toLong()", pattern);
        return doConvertToLong(obj, pattern);
    }

    protected static Long doConvertToLong(Object obj, String pattern) {
        if (obj == null) {
            return null;
        } else if (obj instanceof Long) {
            return (Long) obj;
        } else if (obj instanceof Number) {
            return Long.valueOf(((Number) obj).longValue());
        } else if (obj instanceof String) {
            return doParseStringAsLong((String) obj);
        } else if (obj instanceof java.util.Date) {
            if (pattern != null) {
                final DateFormat dateFormat = createDateFormat(pattern);
                return Long.valueOf(dateFormat.format(obj));
            }
            return Long.valueOf(((java.util.Date) obj).getTime());
        } else if (obj instanceof Boolean) {
            return ((Boolean) obj).booleanValue() ? Long.valueOf(1) : Long.valueOf(0);
        } else if (obj instanceof byte[]) {
            return toLong(toSerializable((byte[]) obj)); // recursive
        } else {
            return doParseStringAsLong(obj.toString());
        }
    }

    protected static Long doParseStringAsLong(String str) {
        if (str == null || str.trim().length() == 0) {
            return null;
        }
        return Long.valueOf(normalize(str));
    }

    public static long toPrimitiveLong(Object obj) {
        return doConvertToPrimitiveLong(obj, null);
    }

    public static long toPrimitiveLong(Object obj, String pattern) {
        assertPatternNotNull("toPrimitiveLong()", pattern);
        return doConvertToPrimitiveLong(obj, pattern);
    }

    protected static long doConvertToPrimitiveLong(Object obj, String pattern) {
        final Long wrapper = doConvertToLong(obj, pattern);
        return wrapper != null ? wrapper.longValue() : 0L;
    }

    // ===================================================================================
    //                                                                              Double
    //                                                                              ======
    public static Double toDouble(Object obj) {
        return doConvertToDouble(obj, null);
    }

    public static Double toDouble(Object obj, String pattern) {
        assertPatternNotNull("toDouble()", pattern);
        return doConvertToDouble(obj, pattern);
    }

    protected static Double doConvertToDouble(Object obj, String pattern) {
        if (obj == null) {
            return null;
        } else if (obj instanceof Double) {
            return (Double) obj;
        } else if (obj instanceof Number) {
            return Double.valueOf(((Number) obj).doubleValue());
        } else if (obj instanceof String) {
            return doParseStringAsDouble((String) obj);
        } else if (obj instanceof java.util.Date) {
            if (pattern != null) {
                final DateFormat dateFormat = createDateFormat(pattern);
                return Double.valueOf(dateFormat.format(obj));
            }
            return Double.valueOf(((java.util.Date) obj).getTime());
        } else if (obj instanceof byte[]) {
            return toDouble(toSerializable((byte[]) obj)); // recursive
        } else {
            return doParseStringAsDouble(obj.toString());
        }
    }

    protected static Double doParseStringAsDouble(String str) {
        if (str == null || str.trim().length() == 0) {
            return null;
        }
        return Double.valueOf(normalize(str));
    }

    public static double toPrimitiveDouble(Object obj) {
        return doConvertToPrimitiveDouble(obj, null);
    }

    public static double toPrimitiveDouble(Object obj, String pattern) {
        assertPatternNotNull("toPrimitiveDouble()", pattern);
        return doConvertToPrimitiveDouble(obj, pattern);
    }

    protected static double doConvertToPrimitiveDouble(Object obj, String pattern) {
        final Double wrapper = doConvertToDouble(obj, pattern);
        return wrapper != null ? wrapper.doubleValue() : 0;
    }

    // ===================================================================================
    //                                                                               Float
    //                                                                               =====
    public static Float toFloat(Object obj) {
        return doConvertToFloat(obj, null);
    }

    public static Float toFloat(Object obj, String pattern) {
        assertPatternNotNull("toFloat()", pattern);
        return doConvertToFloat(obj, pattern);
    }

    protected static Float doConvertToFloat(Object obj, String pattern) {
        if (obj == null) {
            return null;
        } else if (obj instanceof Float) {
            return (Float) obj;
        } else if (obj instanceof Number) {
            return Float.valueOf(((Number) obj).floatValue());
        } else if (obj instanceof String) {
            return doParseStringAsFloat((String) obj);
        } else if (obj instanceof java.util.Date) {
            if (pattern != null) {
                final DateFormat dateFormat = createDateFormat(pattern);
                return Float.valueOf(dateFormat.format(obj));
            }
            return Float.valueOf(((java.util.Date) obj).getTime());
        } else if (obj instanceof byte[]) {
            return toFloat(toSerializable((byte[]) obj)); // recursive
        } else {
            return doParseStringAsFloat(obj.toString());
        }
    }

    protected static Float doParseStringAsFloat(String str) {
        if (str == null || str.trim().length() == 0) {
            return null;
        }
        return Float.valueOf(normalize(str));
    }

    public static float toPrimitiveFloat(Object obj) {
        return doConvertToPrimitiveFloat(obj, null);
    }

    public static float toPrimitiveFloat(Object obj, String pattern) {
        assertPatternNotNull("toPrimitiveFloat()", pattern);
        return doConvertToPrimitiveFloat(obj, pattern);
    }

    protected static float doConvertToPrimitiveFloat(Object obj, String pattern) {
        final Float wrapper = doConvertToFloat(obj, pattern);
        return wrapper != null ? wrapper.floatValue() : 0;
    }

    // ===================================================================================
    //                                                                               Short
    //                                                                               =====
    public static Short toShort(Object obj) {
        return doConvertToShort(obj, null);
    }

    public static Short toShort(Object obj, String pattern) {
        assertPatternNotNull("toShort()", pattern);
        return doConvertToShort(obj, pattern);
    }

    protected static Short doConvertToShort(Object obj, String pattern) {
        if (obj == null) {
            return null;
        } else if (obj instanceof Short) {
            return (Short) obj;
        } else if (obj instanceof Number) {
            return Short.valueOf(((Number) obj).shortValue());
        } else if (obj instanceof String) {
            return toShort((String) obj);
        } else if (obj instanceof java.util.Date) {
            if (pattern != null) {
                final DateFormat dateFormat = createDateFormat(pattern);
                return Short.valueOf(dateFormat.format(obj));
            }
            return Short.valueOf((short) ((java.util.Date) obj).getTime());
        } else if (obj instanceof Boolean) {
            return ((Boolean) obj).booleanValue() ? Short.valueOf((short) 1) : Short.valueOf((short) 0);
        } else if (obj instanceof byte[]) {
            return toShort(toSerializable((byte[]) obj)); // recursive
        } else {
            return toShort(obj.toString());
        }
    }

    protected static Short toShort(String str) {
        if (str == null || str.trim().length() == 0) {
            return null;
        }
        return Short.valueOf(normalize(str));
    }

    public static short toPrimitiveShort(Object obj) {
        return toPrimitiveShort(obj, null);
    }

    public static short toPrimitiveShort(Object obj, String pattern) {
        assertPatternNotNull("toPrimitiveShort()", pattern);
        return doConvertToPrimitiveShort(obj, pattern);
    }

    protected static short doConvertToPrimitiveShort(Object obj, String pattern) {
        final Short wrapper = doConvertToShort(obj, pattern);
        return wrapper != null ? wrapper.shortValue() : 0;
    }

    // ===================================================================================
    //                                                                                Byte
    //                                                                                ====
    public static Byte toByte(Object obj) {
        return doConvertToByte(obj, null);
    }

    public static Byte toByte(Object obj, String pattern) {
        assertPatternNotNull("toByte()", pattern);
        return doConvertToByte(obj, pattern);
    }

    protected static Byte doConvertToByte(Object obj, String pattern) {
        if (obj == null) {
            return null;
        } else if (obj instanceof Byte) {
            return (Byte) obj;
        } else if (obj instanceof Number) {
            return Byte.valueOf(((Number) obj).byteValue());
        } else if (obj instanceof String) {
            return toByte((String) obj);
        } else if (obj instanceof java.util.Date) {
            if (pattern != null) {
                final DateFormat dateFormat = createDateFormat(pattern);
                return Byte.valueOf(dateFormat.format(obj));
            }
            return Byte.valueOf((byte) ((java.util.Date) obj).getTime());
        } else if (obj instanceof Boolean) {
            return ((Boolean) obj).booleanValue() ? Byte.valueOf((byte) 1) : Byte.valueOf((byte) 0);
        } else if (obj instanceof byte[]) {
            return toByte(toSerializable((byte[]) obj)); // recursive
        } else {
            return toByte(obj.toString());
        }
    }

    protected static Byte toByte(String str) {
        if (str == null || str.trim().length() == 0) {
            return null;
        }
        return Byte.valueOf(normalize(str));
    }

    public static byte toPrimitiveByte(Object obj) {
        return doConvertToPrimitiveByte(obj, null);
    }

    public static byte toPrimitiveByte(Object obj, String pattern) {
        assertPatternNotNull("toPrimitiveByte()", pattern);
        return doConvertToPrimitiveByte(obj, pattern);
    }

    protected static byte doConvertToPrimitiveByte(Object obj, String pattern) {
        final Byte wrapper = doConvertToByte(obj, pattern);
        return wrapper != null ? wrapper.byteValue() : 0;
    }

    // -----------------------------------------------------
    //                                                 Bytes
    //                                                 -----
    public static byte[] toBytes(String str, String encoding) {
        if (str == null) {
            return null;
        }
        try {
            return str.getBytes(encoding);
        } catch (UnsupportedEncodingException e) {
            String msg = "The encoding is invalid: encoding=" + encoding + " str=" + str;
            throw new IllegalStateException(msg);
        }
    }

    // ===================================================================================
    //                                                                          BigDecimal
    //                                                                          ==========
    public static BigDecimal toBigDecimal(Object obj) {
        return doConvertToBigDecimal(obj, null);
    }

    public static BigDecimal toBigDecimal(Object obj, String pattern) {
        assertPatternNotNull("toBigDecimal()", pattern);
        return doConvertToBigDecimal(obj, pattern);
    }

    protected static BigDecimal doConvertToBigDecimal(Object obj, String pattern) {
        if (obj == null) {
            return null;
        } else if (obj instanceof BigDecimal) {
            final BigDecimal paramBigDecimal = (BigDecimal) obj;
            if (BigDecimal.class.equals(paramBigDecimal.getClass())) { // pure big-decimal
                return paramBigDecimal;
            } else { // sub class
                // because the big-decimal type is not final class.
                return new BigDecimal(paramBigDecimal.toPlainString());
            }
        } else if (obj instanceof java.util.Date) {
            if (pattern != null) {
                final DateFormat dateFormat = createDateFormat(pattern);
                return new BigDecimal(dateFormat.format(obj));
            }
            return BigDecimal.valueOf(((java.util.Date) obj).getTime());
        } else if (obj instanceof String) {
            final String str = (String) obj;
            if (str == null || str.trim().length() == 0) {
                return null;
            }
            return new BigDecimal(new BigDecimal(str).toPlainString());
        } else if (obj instanceof byte[]) {
            return toBigDecimal(toSerializable((byte[]) obj)); // recursive
        } else {
            return new BigDecimal(new BigDecimal(obj.toString()).toPlainString());
        }
    }

    // ===================================================================================
    //                                                                          BigInteger
    //                                                                          ==========
    public static BigInteger toBigInteger(Object obj) {
        return doConvertToBigInteger(obj, null);
    }

    public static BigInteger toBigInteger(Object obj, String pattern) {
        assertPatternNotNull("toBigInteger()", pattern);
        return doConvertToBigInteger(obj, pattern);
    }

    protected static BigInteger doConvertToBigInteger(Object obj, String pattern) {
        if (obj == null) {
            return null;
        } else if (obj instanceof BigInteger) {
            final BigInteger paramBigInteger = (BigInteger) obj;
            if (BigInteger.class.equals(paramBigInteger.getClass())) { // pure big-integer
                return paramBigInteger;
            } else { // sub class
                // because the big-integer type is not final class.
                return new BigDecimal(paramBigInteger).toBigInteger();
            }
        } else if (obj instanceof BigDecimal) {
            return ((BigDecimal) obj).toBigInteger();
        } else if (obj instanceof String) {
            final String str = (String) obj;
            if (str.trim().length() == 0) {
                return null;
            }
            return toBigDecimal(normalize(str)).toBigInteger();
        } else {
            Long lg = doConvertToLong(obj, pattern);
            if (lg == null) {
                return null;
            }
            return BigInteger.valueOf(lg.longValue());
        }
    }

    // ===================================================================================
    //                                                                            Time API
    //                                                                            ========
    // -----------------------------------------------------
    //                                             LocalDate
    //                                             ---------
    /**
     * Convert the object to the instance that is date for the default time-zone. <br>
     * This method uses default date pattern based on 'yyyy-MM-dd HH:mm:ss.SSS'
     * with flexible-parsing if the object is string type. <br>
     * 
     * <p>If string expression is specified, The year, month, ... parts are parsed from the string.
     * Then the time-zone is not used in conversion. It uses LocalDate.of().</p>
     * 
     * @param obj The object to be converted. (NullAllowed: if null or empty, returns null)
     * @return The local date. (NullAllowed: when the argument is null or empty)
     * @throws ParseDateException When it failed to parse the string to date.
     * @throws ParseDateNumberFormatException When it failed to format the elements as number.
     */
    public static LocalDate toLocalDate(Object obj) { // #date_parade
        return doConvertToLocalDate(obj, (TimeZone) null, (String) null, (Locale) null);
    }

    /**
     * Convert the object to the instance that is date for the default time-zone. <br>
     * This method uses the specified date pattern if the object is string type. 
     * 
     * <p>If string expression is specified, The year, month, ... parts are parsed from the string.
     * Then the time-zone is not used in conversion. It uses LocalDate.parse().</p>
     * 
     * @param obj The object to be converted. (NullAllowed: if null or empty, returns null)
     * @param pattern The pattern format to parse when the object is string. (NotNull)
     * @return The local date. (NullAllowed: when the argument is null or empty)
     * @throws ParseDateException When it failed to parse the string to date.
     * @throws ParseDateNumberFormatException When it failed to format the elements as number.
     */
    public static LocalDate toLocalDate(Object obj, String pattern) {
        assertPatternNotNull("toLocalDate()", pattern);
        return doConvertToLocalDate(obj, (TimeZone) null, pattern, (Locale) null);
    }

    /**
     * Convert the object to the instance that is date for the specified time-zone. <br>
     * This method uses default date pattern based on 'yyyy-MM-dd HH:mm:ss.SSS'
     * with flexible-parsing if the object is string type. <br>
     * 
     * <p>If string expression is specified, The year, month, ... parts are parsed from the string.
     * Then the time-zone is not used in conversion. It uses LocalDate.of(). and millisecond handling is following:</p>
     * 
     * @param obj The object to be converted. (NullAllowed: if null or empty, returns null)
     * @param timeZone The time-zone for the local date. (NotNull: no used when string is specified)
     * @return The local date. (NullAllowed: when the argument is null or empty)
     * @throws ParseDateException When it failed to parse the string to date.
     * @throws ParseDateNumberFormatException When it failed to format the elements as number.
     */
    public static LocalDate toLocalDate(Object obj, TimeZone timeZone) {
        assertTimeZoneNotNull("toLocalDate()", timeZone);
        return doConvertToLocalDate(obj, timeZone, (String) null, (Locale) null);
    }

    /**
     * Convert the object to the instance that is date for the specified time-zone. <br>
     * This method uses the specified date pattern if the object is string type. <br>
     * 
     * <p>If string expression is specified, The year, month, ... parts are parsed from the string.
     * Then the time-zone is not used in conversion. It uses LocalDate.parse().</p>
     * 
     * @param obj The object to be converted. (NullAllowed: if null or empty, returns null)
     * @param timeZone The time-zone for the local date. (NotNull: no used when string is specified)
     * @param pattern The pattern format to parse when the object is string. (NotNull)
     * @return The local date. (NullAllowed: when the argument is null or empty)
     * @throws ParseDateException When it failed to parse the string to date.
     * @throws ParseDateNumberFormatException When it failed to format the elements as number.
     */
    public static LocalDate toLocalDate(Object obj, TimeZone timeZone, String pattern) {
        assertTimeZoneNotNull("toLocalDate()", timeZone);
        assertPatternNotNull("toLocalDate()", pattern);
        return doConvertToLocalDate(obj, timeZone, pattern, (Locale) null);
    }

    /**
     * Convert the object to the instance that is date for the specified time-zone. <br>
     * This method uses the specified date pattern if the object is string type. <br>
     * 
     * <p>If string expression is specified, The year, month, ... parts are parsed from the string.
     * Then the time-zone is not used in conversion. It uses LocalDate.parse().</p>
     * 
     * @param obj The object to be converted. (NullAllowed: if null or empty, returns null)
     * @param timeZone The time-zone for the local date. (NotNull: no used when string is specified)
     * @param pattern The pattern format to parse when the object is string. (NotNull)
     * @param locale The locale for conversion from string. (NotNull)
     * @return The local date. (NullAllowed: when the argument is null or empty)
     * @throws ParseDateException When it failed to parse the string to date.
     * @throws ParseDateNumberFormatException When it failed to format the elements as number.
     */
    public static LocalDate toLocalDate(Object obj, TimeZone timeZone, String pattern, Locale locale) {
        assertTimeZoneNotNull("toLocalDate()", timeZone);
        assertPatternNotNull("toLocalDate()", pattern);
        assertLocaleNotNull("toLocalDate()", locale);
        return doConvertToLocalDate(obj, timeZone, pattern, locale);
    }

    protected static LocalDate doConvertToLocalDate(Object obj, TimeZone timeZone, String pattern, Locale locale) {
        if (obj instanceof String) {
            return doParseStringAsLocalDate((String) obj, pattern, locale); // no need time-zone
        } else if (obj instanceof LocalDate) {
            return (LocalDate) obj;
        } else if (obj instanceof LocalDateTime) {
            return ((LocalDateTime) obj).toLocalDate();
        }
        final TimeZone realZone = chooseRealZone(timeZone);
        final Date zonedResourceDate = toZonedResourceDate(obj, realZone);
        return zonedResourceDate != null ? toZonedDateTime(zonedResourceDate, realZone).toLocalDate() : null;
    }

    protected static LocalDate doParseStringAsLocalDate(String str, String pattern, Locale locale) {
        if (str == null || str.trim().length() == 0) {
            return null;
        }
        final boolean includeTime = false;
        final boolean includeMillis = false;
        final boolean keepMillisMore = false;
        if (pattern != null) {
            try {
                return LocalDate.parse(str, DateTimeFormatter.ofPattern(pattern, chooseRealLocale(locale)));
            } catch (DateTimeParseException e) {
                throw new ParseDateException("Failed to parse the expression: " + str + ", pattern=" + pattern, e);
            }
        } else {
            String filtered = filterDateStringValueFlexibly(str, includeTime, includeMillis, keepMillisMore);
            if (filtered.startsWith("-0000")) { // e.g. BC0001 converted to -0000
                filtered = Srl.ltrim(filtered, "-"); // local date cannot parse -0000
            }
            try {
                return LocalDate.parse(filtered);
            } catch (DateTimeParseException e) {
                throw new ParseDateException("Failed to parse the expression: " + filtered + ", before-filter=" + str, e);
            }
            // *cannot determine out-of-calendar by the exception from local date
        }
    }

    // -----------------------------------------------------
    //                                         LocalDateTime
    //                                         -------------
    /**
     * Convert the object to the instance that is date-time for the default time-zone. <br>
     * This method uses default date pattern based on 'yyyy-MM-dd HH:mm:ss.SSS'
     * with flexible-parsing if the object is string type. <br>
     * 
     * <p>If string expression is specified, The year, month, ... parts are parsed from the string.
     * Then the time-zone is not used in conversion. It uses LocalDateTime.of(). and millisecond handling is following:</p>
     * 
     * <pre>
     * e.g. millisecond and nanosecond handling
     *  "2014/10/28 12:34:56.789" :: same as LocalDateTime.of(2014, 10, 28, 12, 34, 56, 789000000)
     *  "2014/10/28 12:34:56.7" :: same as LocalDateTime.of(2014, 10, 28, 12, 34, 56, 007000000)
     *  "2014/10/28 12:34:56.78" :: same as LocalDateTime.of(2014, 10, 28, 12, 34, 56, 078000000)
     *  "2014/10/28 12:34:56.7891" :: same as LocalDateTime.of(2014, 10, 28, 12, 34, 56, 789100000)
     *  "2014/10/28 12:34:56.789123" :: same as LocalDateTime.of(2014, 10, 28, 12, 34, 56, 789123000)
     *  "2014/10/28 12:34:56.789123456" :: same as LocalDateTime.of(2014, 10, 28, 12, 34, 56, 789123456)
     * </pre>
     * @param obj The object to be converted. (NullAllowed: if null or empty, returns null)
     * @return The local date-time. (NullAllowed: when the argument is null or empty)
     * @throws ParseDateException When it failed to parse the string to date.
     * @throws ParseDateNumberFormatException When it failed to format the elements as number.
     */
    public static LocalDateTime toLocalDateTime(Object obj) {
        return doConvertToLocalDateTime(obj, (TimeZone) null, (String) null, (Locale) null);
    }

    /**
     * Convert the object to the instance that is date-time for the default time-zone. <br>
     * This method uses the specified date pattern if the object is string type. 
     * 
     * <p>If string expression is specified, The year, month, ... parts are parsed from the string.
     * Then the time-zone is not used in conversion. It uses LocalDateTime.parse().</p>
     * 
     * @param obj The object to be converted. (NullAllowed: if null or empty, returns null)
     * @param pattern The pattern format to parse when the object is string. (NotNull)
     * @return The local date-time. (NullAllowed: when the argument is null or empty)
     * @throws ParseDateException When it failed to parse the string to date.
     * @throws ParseDateNumberFormatException When it failed to format the elements as number.
     */
    public static LocalDateTime toLocalDateTime(Object obj, String pattern) {
        assertPatternNotNull("toLocalDateTime()", pattern);
        return doConvertToLocalDateTime(obj, (TimeZone) null, pattern, (Locale) null);
    }

    /**
     * Convert the object to the instance that is date-time for the specified time-zone. <br>
     * This method uses default date pattern based on 'yyyy-MM-dd HH:mm:ss.SSS'
     * with flexible-parsing if the object is string type. <br>
     * 
     * <p>If string expression is specified, The year, month, ... parts are parsed from the string.
     * Then the time-zone is not used in conversion. It uses LocalDateTime.of(). and millisecond handling is following:</p>
     * 
     * <pre>
     * e.g. millisecond and nanosecond handling
     *  "2014/10/28 12:34:56.789" :: same as LocalDateTime.of(2014, 10, 28, 12, 34, 56, 789000000)
     *  "2014/10/28 12:34:56.7" :: same as LocalDateTime.of(2014, 10, 28, 12, 34, 56, 007000000)
     *  "2014/10/28 12:34:56.78" :: same as LocalDateTime.of(2014, 10, 28, 12, 34, 56, 078000000)
     *  "2014/10/28 12:34:56.7891" :: same as LocalDateTime.of(2014, 10, 28, 12, 34, 56, 789100000)
     *  "2014/10/28 12:34:56.789123" :: same as LocalDateTime.of(2014, 10, 28, 12, 34, 56, 789123000)
     *  "2014/10/28 12:34:56.789123456" :: same as LocalDateTime.of(2014, 10, 28, 12, 34, 56, 789123456)
     * </pre>
     * @param obj The object to be converted. (NullAllowed: if null or empty, returns null)
     * @param timeZone The time-zone for the local date-time. (NotNull)
     * @return The local date-time. (NullAllowed: when the argument is null or empty)
     * @throws ParseDateException When it failed to parse the string to date.
     * @throws ParseDateNumberFormatException When it failed to format the elements as number.
     */
    public static LocalDateTime toLocalDateTime(Object obj, TimeZone timeZone) {
        assertTimeZoneNotNull("toLocalDateTime()", timeZone);
        return doConvertToLocalDateTime(obj, timeZone, (String) null, (Locale) null);
    }

    /**
     * Convert the object to the instance that is date-time for the specified time-zone. <br>
     * This method uses the specified date pattern if the object is string type.
     * 
     * <p>If string expression is specified, The year, month, ... parts are parsed from the string.
     * Then the time-zone is not used in conversion. It uses LocalDate.parse().</p>
     * 
     * @param obj The object to be converted. (NullAllowed: if null or empty, returns null)
     * @param timeZone The time-zone for the local date-time. (NotNull)
     * @param pattern The pattern format to parse when the object is string. (NotNull)
     * @return The local date-time. (NullAllowed: when the argument is null or empty)
     * @throws ParseDateException When it failed to parse the string to date.
     * @throws ParseDateNumberFormatException When it failed to format the elements as number.
     */
    public static LocalDateTime toLocalDateTime(Object obj, TimeZone timeZone, String pattern) {
        assertTimeZoneNotNull("toLocalDateTime()", timeZone);
        assertPatternNotNull("toLocalDateTime()", pattern);
        return doConvertToLocalDateTime(obj, timeZone, pattern, (Locale) null);
    }

    /**
     * Convert the object to the instance that is date-time for the specified time-zone. <br>
     * This method uses the specified date pattern if the object is string type.
     * 
     * <p>If string expression is specified, The year, month, ... parts are parsed from the string.
     * Then the time-zone is not used in conversion. It uses LocalDate.parse().</p>
     * 
     * @param obj The object to be converted. (NullAllowed: if null or empty, returns null)
     * @param timeZone The time-zone for the local date-time. (NotNull)
     * @param pattern The pattern format to parse when the object is string. (NotNull)
     * @param locale The locale for conversion from string. (NotNull)
     * @return The local date-time. (NullAllowed: when the argument is null or empty)
     * @throws ParseDateException When it failed to parse the string to date.
     * @throws ParseDateNumberFormatException When it failed to format the elements as number.
     */
    public static LocalDateTime toLocalDateTime(Object obj, TimeZone timeZone, String pattern, Locale locale) {
        assertTimeZoneNotNull("toLocalDateTime()", timeZone);
        assertPatternNotNull("toLocalDateTime()", pattern);
        assertLocaleNotNull("toLocalDateTime()", locale);
        return doConvertToLocalDateTime(obj, timeZone, pattern, locale);
    }

    protected static LocalDateTime doConvertToLocalDateTime(Object obj, TimeZone timeZone, String pattern, Locale locale) {
        if (obj instanceof String) {
            return doParseStringAsLocalDateTime((String) obj, pattern, locale); // no need time-zone
        } else if (obj instanceof LocalDate) {
            return ((LocalDate) obj).atTime(0, 0, 0);
        } else if (obj instanceof LocalDateTime) {
            return (LocalDateTime) obj;
        }
        final TimeZone realZone = chooseRealZone(timeZone);
        final Date zonedResourceDate = toZonedResourceDate(obj, realZone);
        return zonedResourceDate != null ? toZonedDateTime(zonedResourceDate, realZone).toLocalDateTime() : null;
    }

    protected static LocalDateTime doParseStringAsLocalDateTime(String str, String pattern, Locale locale) {
        if (str == null || str.trim().length() == 0) {
            return null;
        }
        final boolean includeTime = true;
        final boolean includeMillis = true;
        final boolean keepMillisMore = true; // local date can use nanosecond
        if (pattern != null) {
            try {
                return LocalDateTime.parse(str, DateTimeFormatter.ofPattern(pattern, chooseRealLocale(locale)));
            } catch (DateTimeParseException e) {
                throw new ParseDateException("Failed to parse the expression: " + str + ", pattern=" + pattern, e);
            }
        } else {
            final String filtered = filterDateStringValueFlexibly(str, includeTime, includeMillis, keepMillisMore);
            final LocalDate localDate = doParseStringAsLocalDate(filtered, null, locale);
            final LocalTime localTime = doParseStringAsLocalTime(filtered, null, locale);
            return LocalDateTime.of(localDate, localTime);
        }
    }

    // -----------------------------------------------------
    //                                             LocalTime
    //                                             ---------
    /**
     * Convert the object to the instance that is time for the default time-zone. <br>
     * This method uses default date pattern based on 'yyyy-MM-dd HH:mm:ss.SSS'
     * with flexible-parsing if the object is string type. <br>
     * 
     * <p>If string expression is specified, The year, month, ... parts are parsed from the string.
     * Then the time-zone is not used in conversion. It uses LocalTime.of(). and millisecond handling is following:</p>
     * 
     * <pre>
     * e.g. millisecond and nanosecond handling
     *  "2014/10/28 12:34:56.789" :: same as LocalTime.of(12, 34, 56, 789000000)
     *  "2014/10/28 12:34:56.7" :: same as LocalTime.of(34, 56, 007000000)
     *  "2014/10/28 12:34:56.78" :: same as LocalTime.of(12, 34, 56, 078000000)
     *  "2014/10/28 12:34:56.7891" :: same as LocalTime.of(12, 34, 56, 789100000)
     *  "2014/10/28 12:34:56.789123" :: same as LocalTime.of(12, 34, 56, 789123000)
     *  "2014/10/28 12:34:56.789123456" :: same as LocalTime.of(12, 34, 56, 789123456)
     * </pre>
     * @param obj The object to be converted. (NullAllowed: if null or empty, returns null)
     * @return The local time. (NullAllowed: when the argument is null or empty)
     * @throws ParseDateException When it failed to parse the string to date.
     * @throws ParseDateNumberFormatException When it failed to format the elements as number.
     */
    public static LocalTime toLocalTime(Object obj) {
        return doConvertToLocalTime(obj, (TimeZone) null, (String) null, (Locale) null);
    }

    /**
     * Convert the object to the instance that is date-time for the default time-zone. <br>
     * This method uses the specified date pattern if the object is string type. 
     * 
     * <p>If string expression is specified, The year, month, ... parts are parsed from the string.
     * Then the time-zone is not used in conversion. It uses LocalTime.parse().</p>
     * 
     * @param obj The object to be converted. (NullAllowed: if null or empty, returns null)
     * @param pattern The pattern format to parse when the object is string. (NotNull)
     * @return The local time. (NullAllowed: when the argument is null or empty)
     * @throws ParseDateException When it failed to parse the string to date.
     * @throws ParseDateNumberFormatException When it failed to format the elements as number.
     */
    public static LocalTime toLocalTime(Object obj, String pattern) {
        assertPatternNotNull("toLocalTime()", pattern);
        return doConvertToLocalTime(obj, (TimeZone) null, pattern, (Locale) null);
    }

    /**
     * Convert the object to the instance that is time for the specified time-zone. <br>
     * This method uses default date pattern based on 'yyyy-MM-dd HH:mm:ss.SSS'
     * with flexible-parsing if the object is string type. <br>
     * 
     * <p>If string expression is specified, The year, month, ... parts are parsed from the string.
     * Then the time-zone is not used in conversion. It uses LocalTime.of(). and millisecond handling is following:</p>
     * 
     * <pre>
     * e.g. millisecond and nanosecond handling
     *  "2014/10/28 12:34:56.789" :: same as LocalTime.of(12, 34, 56, 789000000)
     *  "2014/10/28 12:34:56.7" :: same as LocalTime.of(34, 56, 007000000)
     *  "2014/10/28 12:34:56.78" :: same as LocalTime.of(12, 34, 56, 078000000)
     *  "2014/10/28 12:34:56.7891" :: same as LocalTime.of(12, 34, 56, 789100000)
     *  "2014/10/28 12:34:56.789123" :: same as LocalTime.of(12, 34, 56, 789123000)
     *  "2014/10/28 12:34:56.789123456" :: same as LocalTime.of(12, 34, 56, 789123456)
     * </pre>
     * @param obj The object to be converted. (NullAllowed: if null or empty, returns null)
     * @param timeZone The time-zone for the local time. (NotNull)
     * @return The local time. (NullAllowed: when the argument is null or empty)
     * @throws ParseDateException When it failed to parse the string to date.
     * @throws ParseDateNumberFormatException When it failed to format the elements as number.
     */
    public static LocalTime toLocalTime(Object obj, TimeZone timeZone) {
        assertTimeZoneNotNull("toLocalTime()", timeZone);
        return doConvertToLocalTime(obj, timeZone, (String) null, (Locale) null);
    }

    /**
     * Convert the object to the instance that is date-time for the specified time-zone. <br>
     * This method uses the specified date pattern if the object is string type. 
     * 
     * <p>If string expression is specified, The year, month, ... parts are parsed from the string.
     * Then the time-zone is not used in conversion. It uses LocalTime.parse().</p>
     * 
     * @param obj The object to be converted. (NullAllowed: if null or empty, returns null)
     * @param timeZone The time-zone for the local time. (NotNull)
     * @param pattern The pattern format to parse when the object is string. (NotNull)
     * @return The local time. (NullAllowed: when the argument is null or empty)
     * @throws ParseDateException When it failed to parse the string to date.
     * @throws ParseDateNumberFormatException When it failed to format the elements as number.
     */
    public static LocalTime toLocalTime(Object obj, TimeZone timeZone, String pattern) {
        assertTimeZoneNotNull("toLocalTime()", timeZone);
        assertPatternNotNull("toLocalTime()", pattern);
        return doConvertToLocalTime(obj, timeZone, pattern, (Locale) null);
    }

    /**
     * Convert the object to the instance that is date-time for the specified time-zone. <br>
     * This method uses the specified date pattern if the object is string type. 
     * 
     * <p>If string expression is specified, The year, month, ... parts are parsed from the string.
     * Then the time-zone is not used in conversion. It uses LocalTime.parse().</p>
     * 
     * @param obj The object to be converted. (NullAllowed: if null or empty, returns null)
     * @param timeZone The time-zone for the local time. (NotNull)
     * @param pattern The pattern format to parse when the object is string. (NotNull)
     * @param locale The locale for conversion from string. (NotNull)
     * @return The local time. (NullAllowed: when the argument is null or empty)
     * @throws ParseDateException When it failed to parse the string to date.
     * @throws ParseDateNumberFormatException When it failed to format the elements as number.
     */
    public static LocalTime toLocalTime(Object obj, TimeZone timeZone, String pattern, Locale locale) {
        assertTimeZoneNotNull("toLocalTime()", timeZone);
        assertPatternNotNull("toLocalTime()", pattern);
        assertLocaleNotNull("toLocalTime()", locale);
        return doConvertToLocalTime(obj, timeZone, pattern, locale);
    }

    protected static LocalTime doConvertToLocalTime(Object obj, TimeZone timeZone, String pattern, Locale locale) {
        if (obj instanceof String) {
            return doParseStringAsLocalTime((String) obj, pattern, locale);
        } else if (obj instanceof LocalDateTime) {
            return ((LocalDateTime) obj).toLocalTime();
        } else if (obj instanceof LocalTime) {
            return (LocalTime) obj;
        }
        final TimeZone realZone = chooseRealZone(timeZone);
        final Date zonedResourceDate = toZonedResourceDate(obj, realZone);
        return zonedResourceDate != null ? toZonedDateTime(zonedResourceDate, realZone).toLocalTime() : null;
    }

    protected static LocalTime doParseStringAsLocalTime(String str, String pattern, Locale locale) {
        if (str == null || str.trim().length() == 0) {
            return null;
        }
        if (pattern != null) {
            try {
                return LocalTime.parse(str, DateTimeFormatter.ofPattern(pattern, chooseRealLocale(locale)));
            } catch (DateTimeParseException e) {
                throw new ParseDateException("Failed to parse the expression: " + str + " pattern=" + pattern, e);
            }
        }
        final boolean includeMillis = true;
        final boolean keepMillisMore = true; // local date can use nanosecond
        final String timePart = filterTimeStringValueFlexibly(str, includeMillis, keepMillisMore); // HH:mm:ss[.SSS...]
        try {
            // parse myself for millisecond and nanosecond handling
            final String timeDelim = ":";
            final String millisDelim = ".";
            final int hour = doParseStringAsInteger(Srl.substringFirstFront(timePart, timeDelim));
            final int minute = doParseStringAsInteger(Srl.substringFirstFront(Srl.substringFirstRear(timePart, timeDelim), timeDelim));
            final int second = doParseStringAsInteger(Srl.substringFirstFront(Srl.substringLastRear(timePart, timeDelim), millisDelim));
            final int nanos;
            if (timePart.contains(millisDelim)) {
                final String millisPart = Srl.substringFirstRear(timePart, millisDelim);
                nanos = doParseStringAsInteger(doConvertMillisToNanosString(millisPart));
            } else {
                nanos = 0;
            }
            try {
                return LocalTime.of(hour, minute, second, nanos); // no need time-zone
            } catch (DateTimeException e) {
                String msg = "Failed to convert to local time:";
                msg = msg + " " + hour + ", " + minute + ", " + second + ", " + nanos + ", timePart=" + timePart;
                throw new ParseDateException(msg, e);
            }
        } catch (RuntimeException e) {
            throw new ParseDateException("Failed to parse the expression: " + timePart + ", before-filter=" + str, e);
        }
    }

    protected static String doConvertMillisToNanosString(String millis) {
        if (millis.length() < 3) { // no way
            throw new IllegalArgumentException("The argument 'millis' needs at least 3 length: " + millis);
        }
        return Srl.rfill(millis, 9, '0');
    }

    // -----------------------------------------------------
    //                                         ZonedDateTime
    //                                         -------------
    /**
     * @param obj The object to be converted. (NullAllowed: if null or empty, returns null)
     * @param timeZone The time-zone for the local date. (NotNull)
     * @return The zoned date-time. (NullAllowed: when the argument is null or empty)
     */
    protected static ZonedDateTime toZonedDateTime(Object obj, TimeZone timeZone) {
        // internal function for now, needs to know zoned handling
        assertTimeZoneNotNull("toZonedDateTime()", timeZone);
        if (obj == null) {
            return null;
        }
        final Date zonedResourceDate = toZonedResourceDate(obj, timeZone);
        if (zonedResourceDate == null) {
            return null;
        }
        final ZoneId zoneId = timeZone.toZoneId();
        return ZonedDateTime.ofInstant(zonedResourceDate.toInstant(), zoneId);
    }

    protected static Date toZonedResourceDate(Object obj, TimeZone timeZone) {
        return toDate(obj, timeZone); // java.sql.Date does not support toInstant() so to pure date
    }

    // -----------------------------------------------------
    //                                          Any Handling
    //                                          ------------
    /**
     * Is the object local date or local date-time or local time?
     * @param obj The object to be judged. (NotNull)
     * @return The determination, true or false.
     */
    public static boolean isAnyLocalDate(Object obj) {
        return isLocalDateOrDateTime(obj) || obj instanceof LocalTime;
    }

    /**
     * Is the object local date or local date-time?
     * @param obj The object to be judged. (NotNull)
     * @return The determination, true or false.
     */
    public static boolean isLocalDateOrDateTime(Object obj) {
        return obj instanceof LocalDate || obj instanceof LocalDateTime;
    }

    /**
     * Is the type local date or local date-time or local time?
     * @param type The class type to be judged. (NotNull)
     * @return The determination, true or false.
     */
    public static boolean isAnyLocalDateType(Class<?> type) {
        return isLocalDateOrDateTimeType(type) || LocalTime.class.isAssignableFrom(type);
    }

    /**
     * Is the type local date or local date-time?
     * @param type The class type to be judged. (NotNull)
     * @return The determination, true or false.
     */
    public static boolean isLocalDateOrDateTimeType(Class<?> type) {
        return LocalDate.class.isAssignableFrom(type) || LocalDateTime.class.isAssignableFrom(type);
    }

    // -----------------------------------------------------
    //                                        AD/BC Handling
    //                                        --------------
    public static boolean isLocalDateAD(LocalDate date) {
        return date.getYear() > 0;
    }

    public static boolean isLocalDateBC(LocalDate date) {
        return date.getYear() <= 0; // 0 means BC0001 in local date
    }

    // ===================================================================================
    //                                                                          Point Date
    //                                                                          ==========
    /**
     * Convert to point date object.
     * @param <DATE> The type of date.
     * @param obj The resource of number. (NullAllowed: if null or empty, returns null)
     * @param type The type of number. (NotNull)
     * @return The point date object from resource. (NullAllowed: if type is not date, returns null)
     */
    @SuppressWarnings("unchecked")
    public static <DATE> DATE toPointDate(Object obj, Class<DATE> type) {
        if (obj == null) {
            return null;
        }
        if (java.sql.Date.class.isAssignableFrom(type)) { // #date_parade
            return (DATE) toSqlDate(obj);
        } else if (java.sql.Timestamp.class.isAssignableFrom(type)) {
            return (DATE) toTimestamp(obj);
        } else if (java.sql.Time.class.isAssignableFrom(type)) {
            return (DATE) toTime(obj);
        } else if (Date.class.isAssignableFrom(type)) {
            return (DATE) toDate(obj);
        } else if (LocalDate.class.isAssignableFrom(type)) {
            return (DATE) toLocalDate(obj);
        } else if (LocalDateTime.class.isAssignableFrom(type)) {
            return (DATE) toLocalDateTime(obj);
        } else if (LocalTime.class.isAssignableFrom(type)) {
            return (DATE) toLocalTime(obj);
        }
        return null; // could not convert
    }

    // ===================================================================================
    //                                                                          (util)Date
    //                                                                          ==========
    /**
     * Convert the object to the instance that is date for the default time-zone. <br>
     * Even if it's the sub class type, it returns a new instance. <br>
     * This method uses default date pattern based on 'yyyy-MM-dd HH:mm:ss.SSS'
     * with flexible-parsing if the object is string type.
     * @param obj The parsed object. (NullAllowed: if null or empty, returns null)
     * @return The instance of date. (NullAllowed: when the date is null or empty)
     * @throws ParseDateException When it failed to parse the string to date.
     * @throws ParseDateNumberFormatException When it failed to format the elements as number.
     * @throws ParseDateOutOfCalendarException When the date was out of calendar. (if BC, not thrown)
     */
    public static Date toDate(Object obj) {
        return doConvertToDate(obj, (TimeZone) null, (String) null, (Locale) null);
    }

    /**
     * Convert the object to the instance that is date for the specified time-zone. <br>
     * Even if it's the sub class type, it returns a new instance. <br>
     * This method uses default date pattern based on 'yyyy-MM-dd HH:mm:ss.SSS'
     * with flexible-parsing if the object is string type.
     * @param obj The parsed object. (NullAllowed: if null or empty, returns null)
     * @param timeZone The time-zone to parse the string expression. (NotNull)
     * @return The instance of date. (NullAllowed: when the date is null or empty)
     * @throws ParseDateException When it failed to parse the string to date.
     * @throws ParseDateNumberFormatException When it failed to format the elements as number.
     * @throws ParseDateOutOfCalendarException When the date was out of calendar. (if BC, not thrown)
     */
    public static Date toDate(Object obj, TimeZone timeZone) {
        assertTimeZoneNotNull("toDate()", timeZone);
        return doConvertToDate(obj, timeZone, (String) null, (Locale) null);
    }

    /**
     * Convert the object to the instance that is date. <br>
     * Even if it's the sub class type, it returns a new instance.
     * @param obj The parsed object. (NullAllowed: if null or empty, returns null)
     * @param pattern The pattern format to parse when the object is string. (NotNull)
     * @return The instance of date. (NullAllowed: when the date is null or empty)
     * @throws ParseDateException When it failed to parse the string to date.
     * @throws ParseDateNumberFormatException When it failed to format the elements as number.
     * @throws ParseDateOutOfCalendarException When the date was out of calendar. (if BC, not thrown)
     */
    public static Date toDate(Object obj, String pattern) {
        assertPatternNotNull("toDate()", pattern);
        return doConvertToDate(obj, (TimeZone) null, pattern, (Locale) null);
    }

    /**
     * Convert the object to the instance that is date. <br>
     * Even if it's the sub class type, it returns a new instance.
     * @param obj The parsed object. (NullAllowed: if null or empty, returns null)
     * @param pattern The pattern format to parse when the object is string. (NotNull)
     * @param locale The locale to parse the date expression. (NotNull)
     * @return The instance of date. (NullAllowed: when the date is null or empty)
     * @throws ParseDateException When it failed to parse the string to date.
     * @throws ParseDateNumberFormatException When it failed to format the elements as number.
     * @throws ParseDateOutOfCalendarException When the date was out of calendar. (if BC, not thrown)
     */
    public static Date toDate(Object obj, String pattern, Locale locale) {
        assertPatternNotNull("toDate()", pattern);
        assertLocaleNotNull("toDate()", locale);
        return doConvertToDate(obj, (TimeZone) null, pattern, locale);
    }

    /**
     * Convert the object to the instance that is date. <br>
     * Even if it's the sub class type, it returns a new instance.
     * @param obj The parsed object. (NullAllowed: if null or empty, returns null)
     * @param timeZone The time-zone to parse the string expression. (NotNull)
     * @param pattern The pattern format to parse when the object is string. (NotNull)
     * @param locale The locale to parse the date expression. (NotNull)
     * @return The instance of date. (NullAllowed: when the date is null or empty)
     * @throws ParseDateException When it failed to parse the string to date.
     * @throws ParseDateNumberFormatException When it failed to format the elements as number.
     * @throws ParseDateOutOfCalendarException When the date was out of calendar. (if BC, not thrown)
     */
    public static Date toDate(Object obj, TimeZone timeZone, String pattern, Locale locale) {
        assertTimeZoneNotNull("toDate()", timeZone);
        assertPatternNotNull("toDate()", pattern);
        assertLocaleNotNull("toDate()", locale);
        return doConvertToDate(obj, timeZone, pattern, locale);
    }

    protected static Date doConvertToDate(Object obj, TimeZone timeZone, String pattern, Locale locale) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof String) {
            return doParseStringAsDate((String) obj, timeZone, pattern, locale);
        } else if (obj instanceof LocalDate) {
            final LocalDate localDate = (LocalDate) obj;
            return doParseLocalDateAsDate(localDate, timeZone);
        } else if (obj instanceof LocalDateTime) {
            final LocalDateTime localDateTime = (LocalDateTime) obj;
            return doParseLocalDateTimeAsDate(localDateTime, timeZone);
        } else if (obj instanceof Date) {
            final Date paramDate = (Date) obj;
            if (Date.class.equals(paramDate.getClass())) { // pure date
                return paramDate;
            } else { // sub class (Date is not final class)
                return new Date(paramDate.getTime()); // returns copied pure date
            }
        } else if (obj instanceof Calendar) {
            return ((Calendar) obj).getTime();
        } else if (obj instanceof byte[]) {
            return toDate(toSerializable((byte[]) obj)); // recursive
        } else if (obj instanceof Long) {
            return new Date((Long) obj);
        } else {
            return doParseStringAsDate(obj.toString(), timeZone, pattern, locale);
        }
    }

    protected static Date doParseStringAsDate(String str, TimeZone timeZone, String pattern, Locale locale) {
        if (str == null || str.trim().length() == 0) {
            return null;
        }
        boolean strict;
        if (pattern == null || pattern.trim().length() == 0) { // flexibly
            final boolean includeTime = true; // date has time part
            final boolean includeMillis = true; // after all, includes when date too because date type can have millisecond formally
            final boolean keepMillisMore = false; // date cannot use nanosecond
            str = filterDateStringValueFlexibly(str, includeTime, includeMillis, keepMillisMore);
            strict = !str.startsWith("-"); // not BC
            pattern = DEFAULT_TIMESTAMP_PATTERN;
        } else {
            strict = true;
        }
        final DateFormat df = doCreateDateFormat(pattern, timeZone, locale, strict);
        try {
            return df.parse(str);
        } catch (ParseException e) {
            try {
                df.setLenient(true);
                df.parse(str); // no exception means illegal date
                String msg = "The date expression is out of calendar:";
                msg = msg + " string=" + str + " pattern=" + pattern + " timeZone=" + timeZone;
                throw new ParseDateOutOfCalendarException(msg, e);
            } catch (ParseException ignored) {
                String msg = "Failed to parse the string to date:";
                msg = msg + " string=" + str + " pattern=" + pattern + " timeZone=" + timeZone;
                throw new ParseDateException(msg, e);
            }
        }
    }

    protected static String filterDateStringValueFlexibly(final String pureStr //
            , boolean includeTime // HH:mm:ss
            , boolean includeMillis // .SSS
            , boolean keepMillisMore) { // .SSS...
        String value = pureStr;
        value = value.trim();

        final String dateLiteralPrefix = "date ";

        final boolean dateLiteral = value.startsWith(dateLiteralPrefix);
        if (dateLiteral) {
            value = value.substring(dateLiteralPrefix.length());
            value = value.trim();
        }

        final String adLatinPrefix = "AD";
        final String adLatinDotPrefix = "A.D.";
        final String bcLatinPrefix = "BC";
        final String bcLatinDotPrefix = "B.C.";
        boolean hasBcLatinPrefix = false;
        final String bcMinusPrefix = "-";

        // handling AD/BC prefix
        final boolean bc;
        {
            if (value.startsWith(adLatinPrefix)) {
                value = value.substring(adLatinPrefix.length());
                bc = false;
            } else if (value.startsWith(adLatinDotPrefix)) {
                value = value.substring(adLatinDotPrefix.length());
                bc = false;
            } else if (value.startsWith(bcLatinPrefix)) {
                value = value.substring(bcLatinPrefix.length());
                hasBcLatinPrefix = true;
                bc = true;
            } else if (value.startsWith(bcLatinDotPrefix)) {
                value = value.substring(bcLatinDotPrefix.length());
                hasBcLatinPrefix = true;
                bc = true;
            } else if (value.startsWith(bcMinusPrefix)) {
                value = value.substring(bcMinusPrefix.length());
                bc = true;
            } else {
                bc = false;
            }
            value = value.trim();
        }

        final String dateDlm = "-";

        // handling slash delimiter for yyyyMMdd
        value = value.replaceAll("/", dateDlm);

        // handling 'date 20090119' and 'date 8631230' and so on
        if (dateLiteral && value.length() <= 8 && !value.contains(dateDlm)) {
            if (value.length() >= 5) {
                value = resolveDateElementZeroPrefix(value, 8 - value.length());
                final String yyyy = value.substring(0, 4);
                final String mm = value.substring(4, 6);
                final String dd = value.substring(6, 8);
                value = yyyy + dateDlm + mm + dateDlm + dd;
            } else {
                return pureStr; // couldn't filter for example '1234'
            }
        }

        // check whether it can filter
        if (!value.contains("-") || (value.indexOf("-") == value.lastIndexOf("-"))) {
            return pureStr; // couldn't filter for example '123456789' and '1234-123'
        }

        // handling zero prefix
        final int yearEndIndex = value.indexOf(dateDlm);
        String yyyy = value.substring(0, yearEndIndex);
        yyyy = resolveDateElementZeroPrefix(yyyy, 4 - yyyy.length());
        if (bc) {
            final Integer yyyyInt = formatDateElementAsNumber(yyyy, "yyyy", pureStr);

            // DateFormat treats '-2007' as 'BC2008'
            // if 'BC2008', it needs to be converted to -2007 so adjust it
            // if '-2007', it is parsed as 'BC2008' so it does not need any adjustment
            yyyy = String.valueOf(yyyyInt - (hasBcLatinPrefix ? 1 : 0));
            yyyy = resolveDateElementZeroPrefix(yyyy, 4 - yyyy.length());
        } else {
            formatDateElementAsNumber(yyyy, "yyyy", pureStr); // check only
        }

        final String startsMon = value.substring(yearEndIndex + dateDlm.length());
        final int monthEndIndex = startsMon.indexOf(dateDlm);
        String mm = startsMon.substring(0, monthEndIndex);
        mm = resolveDateElementZeroPrefix(mm, 2 - mm.length());
        formatDateElementAsNumber(mm, "MM", pureStr); // check only

        final String dateTimeDlm = " ";
        final String dateTimeNextDlm = "T"; // for LocalDate
        final String timeDlm = ":";
        final String timeMilliDlm = ".";

        final String startsDay = startsMon.substring(monthEndIndex + dateDlm.length());
        int dayEndIndex = startsDay.indexOf(dateTimeDlm);
        final int dateTimeDlmLength;
        String dd;
        if (dayEndIndex >= 0) { // e.g. 2014/11/14 12:34:56
            dd = startsDay.substring(0, dayEndIndex);
            dateTimeDlmLength = dateTimeDlm.length();
        } else {
            dayEndIndex = startsDay.indexOf(dateTimeNextDlm);
            if (dayEndIndex >= 0) { // e.g. 2014/11/14T12:34:56
                dd = startsDay.substring(0, dayEndIndex);
                dateTimeDlmLength = dateTimeNextDlm.length();
            } else { // e.g. 2014/11/14
                dd = startsDay;
                dateTimeDlmLength = -1; // unused
            }
        }
        dd = resolveDateElementZeroPrefix(dd, 2 - dd.length());
        formatDateElementAsNumber(dd, "dd", pureStr); // check only
        final String yyyy_MM_dd = yyyy + dateDlm + mm + dateDlm + dd;

        if (includeTime) {
            if (dayEndIndex >= 0) { // has time parts
                String time = startsDay.substring(dayEndIndex + dateTimeDlmLength);

                // check whether it can filter
                if (!time.contains(timeDlm)) {
                    return pureStr; // couldn't filter for example '2009-12-12 123456'
                }
                if (time.indexOf(timeDlm) == time.lastIndexOf(timeDlm)) { // '2009-12-12 12:34'
                    time = time + ":00"; // to '2009-12-12 12:34:00' for parsing as time
                }
                value = yyyy_MM_dd + dateTimeDlm + handleTimeZeroPrefix(time, pureStr, includeMillis, keepMillisMore);
            } else {
                value = yyyy_MM_dd + dateTimeDlm + "00:00:00";
                if (includeMillis) {
                    value = value + timeMilliDlm + "000";
                }
            }
        } else { // no time-part
            value = yyyy_MM_dd;
        }
        return (bc ? bcMinusPrefix : "") + value;
    }

    protected static String handleTimeZeroPrefix(String time, String pureStr, boolean includeMillis, boolean keepMillisMore) {
        final String timeDlm = ":";
        final String timeMilliDlm = ".";

        final int hourEndIndex = time.indexOf(timeDlm);
        String hour = time.substring(0, hourEndIndex);
        hour = resolveDateElementZeroPrefix(hour, 2 - hour.length());
        formatDateElementAsNumber(hour, "HH", pureStr); // check only

        final String startsMin = time.substring(hourEndIndex + timeDlm.length());
        final int minEndIndex = startsMin.indexOf(timeDlm);
        String min = startsMin.substring(0, minEndIndex);
        min = resolveDateElementZeroPrefix(min, 2 - min.length());
        formatDateElementAsNumber(min, "mm", pureStr); // check only

        final String startsSec = startsMin.substring(minEndIndex + timeDlm.length());
        final int secEndIndex = startsSec.indexOf(timeMilliDlm);
        String sec = secEndIndex >= 0 ? startsSec.substring(0, secEndIndex) : startsSec;
        sec = resolveDateElementZeroPrefix(sec, 2 - sec.length());
        formatDateElementAsNumber(sec, "ss", pureStr); // check only

        String value = hour + timeDlm + min + timeDlm + sec;
        if (includeMillis) {
            if (secEndIndex >= 0) {
                String millis = startsSec.substring(secEndIndex + timeMilliDlm.length());
                millis = handleUTCMarkIfExists(millis, pureStr); // may be e.g. 12:34:56.789Z
                if (millis.length() > 3) { // keep or truncate details
                    millis = keepMillisMore ? millis : millis.substring(0, 3);
                } else { // add zero prefix
                    millis = resolveDateElementZeroPrefix(millis, 3 - millis.length());
                }
                formatDateElementAsNumber(millis, "SSS", pureStr); // check only
                value = value + timeMilliDlm + millis; // append millisecond
            } else {
                value = value + timeMilliDlm + "000";
            }
        }
        return value;
    }

    protected static String handleUTCMarkIfExists(String millis, String pureValue) {
        // quit automatic filter for safety, only improve error message
        //return millis.endsWith("Z") ? millis.substring(0, millis.length() - 1) : millis;
        if (millis.endsWith("Z")) {
            String msg = "Cannot parse millisecond part with UTC suffix 'Z' (e.g. ...12:34:56.789Z): " + pureValue;
            throw new ParseDateUTCSuffixException(msg);
        }
        return millis;
    }

    protected static Integer formatDateElementAsNumber(String str, String title, String pureValue) {
        try {
            return Integer.valueOf(str);
        } catch (NumberFormatException e) {
            String msg = "Failed to format " + title + " as number:";
            msg = msg + " " + title + "=" + str + " value=" + pureValue;
            throw new ParseDateNumberFormatException(msg, e);
        }
    }

    protected static String resolveDateElementZeroPrefix(String str, int count) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append("0");
        }
        return sb.toString() + str;
    }

    protected static String resolveDateElementZeroSuffix(String str, int count) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append("0");
        }
        return str + sb.toString();
    }

    public static class ParseDateException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public ParseDateException(String msg) {
            super(msg);
        }

        public ParseDateException(String msg, Exception e) {
            super(msg, e);
        }
    }

    public static class ParseDateNumberFormatException extends ParseDateException {
        private static final long serialVersionUID = 1L;

        public ParseDateNumberFormatException(String msg, Exception e) {
            super(msg, e);
        }
    }

    public static class ParseDateOutOfCalendarException extends ParseDateException {
        private static final long serialVersionUID = 1L;

        public ParseDateOutOfCalendarException(String msg, Exception e) {
            super(msg, e);
        }
    }

    public static class ParseDateUTCSuffixException extends ParseDateException {
        private static final long serialVersionUID = 1L;

        public ParseDateUTCSuffixException(String msg) {
            super(msg);
        }
    }

    // -----------------------------------------------------
    //                                         from Time API
    //                                         -------------
    protected static Date doParseLocalDateAsDate(LocalDate localDate, TimeZone timeZone) {
        if (localDate == null) {
            return null;
        }
        final LocalDateTime localDateTime = localDate.atTime(0, 0, 0, 0);
        return doParseLocalDateTimeAsDate(localDateTime, timeZone);
    }

    protected static Date doParseLocalDateTimeAsDate(LocalDateTime localDateTime, TimeZone timeZone) {
        if (localDateTime == null) {
            return null;
        }
        final TimeZone realZone = chooseRealZone(timeZone);
        final ZoneId zoneId = timeZone != null ? realZone.toZoneId() : ZoneId.systemDefault();
        return Date.from(localDateTime.toInstant(zoneId.getRules().getOffset(localDateTime)));
    }

    protected static Timestamp doParseLocalDateAsTimestamp(LocalDate localDate, TimeZone timeZone) {
        if (localDate == null) {
            return null;
        }
        final LocalDateTime localDateTime = localDate.atTime(0, 0, 0, 0);
        return doParseLocalDateTimeAsTimestamp(localDateTime, timeZone);
    }

    protected static Timestamp doParseLocalDateTimeAsTimestamp(LocalDateTime localDateTime, TimeZone timeZone) {
        if (localDateTime == null) {
            return null;
        }
        final TimeZone realZone = chooseRealZone(timeZone);
        final ZoneId zoneId = timeZone != null ? realZone.toZoneId() : ZoneId.systemDefault();
        return Timestamp.from(localDateTime.toInstant(zoneId.getRules().getOffset(localDateTime)));
    }

    // -----------------------------------------------------
    //                                        AD/BC Handling
    //                                        --------------
    public static boolean isDateAD(Date date) {
        return doJudgeDateAD(date, null);
    }

    public static boolean isDateAD(Date date, TimeZone timeZone) {
        assertTimeZoneNotNull("isDateAD()", timeZone);
        return doJudgeDateAD(date, timeZone);
    }

    protected static boolean doJudgeDateAD(Date date, TimeZone timeZone) {
        return date.getTime() >= getTimeZonedADOriginMillis(timeZone);
    }

    public static boolean isDateBC(Date date) {
        return doJudgeDateBC(date, null);
    }

    public static boolean isDateBC(Date date, TimeZone timeZone) {
        assertTimeZoneNotNull("isDateBC()", timeZone);
        return doJudgeDateBC(date, timeZone);
    }

    protected static boolean doJudgeDateBC(Date date, TimeZone timeZone) {
        return date.getTime() < getTimeZonedADOriginMillis(timeZone);
    }

    protected static long getTimeZonedADOriginMillis(TimeZone timeZone) {
        final int offset = chooseRealZone(timeZone).getOffset(GMT_AD_ORIGIN_MILLISECOND);
        return GMT_AD_ORIGIN_MILLISECOND - offset;
    }

    // ===================================================================================
    //                                                         Old Style Date Manipulation
    //                                                         ===========================
    // these mutable and only default time-zone methods are unsupported since 1.1
    // but only change modifier to protected because many tests use
    // you can use HandyDate instead of these methods, which can specify time-zone 
    // -----------------------------------------------------
    //                                              Add Date
    //                                              --------
    protected static void addDateYear(Date date, int years) {
        final Calendar cal = toCalendar(date);
        addCalendarYear(cal, years);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void addDateMonth(Date date, int months) {
        final Calendar cal = toCalendar(date);
        addCalendarMonth(cal, months);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void addDateDay(Date date, int days) {
        final Calendar cal = toCalendar(date);
        addCalendarDay(cal, days);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void addDateHour(Date date, int hours) {
        final Calendar cal = toCalendar(date);
        addCalendarHour(cal, hours);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void addDateMinute(Date date, int minutes) {
        final Calendar cal = toCalendar(date);
        addCalendarMinute(cal, minutes);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void addDateSecond(Date date, int seconds) {
        final Calendar cal = toCalendar(date);
        addCalendarSecond(cal, seconds);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void addDateMillisecond(Date date, int milliseconds) {
        final Calendar cal = toCalendar(date);
        addCalendarMillisecond(cal, milliseconds);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void addDateWeekOfMonth(Date date, int weeksOfMonth) {
        final Calendar cal = toCalendar(date);
        addCalendarWeek(cal, weeksOfMonth);
        date.setTime(cal.getTimeInMillis());
    }

    // -----------------------------------------------------
    //                                          Move-to Date
    //                                          ------------
    // not all methods are supported
    // because you should use calendar's methods basically
    // - - - - - - - - - - - - - - - - - - - - - - - -[Year]
    protected static void moveToDateYear(Date date, int year) {
        final Calendar cal = toCalendar(date);
        moveToCalendarYear(cal, year);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateYearJust(Date date) {
        final Calendar cal = toCalendar(date);
        moveToCalendarYearJust(cal);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateYearJust(Date date, int yearBeginMonth) {
        final Calendar cal = toCalendar(date);
        moveToCalendarYearJust(cal, yearBeginMonth);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateYearJustAdded(Date date, int year) {
        final Calendar cal = toCalendar(date);
        moveToCalendarYearJustAdded(cal, year);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateYearJustFor(Date date, int year) {
        final Calendar cal = toCalendar(date);
        moveToCalendarYearJustFor(cal, year);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateYearTerminal(Date date) {
        final Calendar cal = toCalendar(date);
        moveToCalendarYearTerminal(cal);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateYearTerminal(Date date, int yearBeginMonth) {
        final Calendar cal = toCalendar(date);
        moveToCalendarYearTerminal(cal, yearBeginMonth);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateYearTerminalAdded(Date date, int year) {
        final Calendar cal = toCalendar(date);
        moveToCalendarYearTerminalAdded(cal, year);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateYearTerminalFor(Date date, int year) {
        final Calendar cal = toCalendar(date);
        moveToCalendarYearTerminalFor(cal, year);
        date.setTime(cal.getTimeInMillis());
    }

    // - - - - - - - - - - - - - - - - - - - - - - - [Month]
    protected static void moveToDateMonth(Date date, int month) {
        final Calendar cal = toCalendar(date);
        moveToCalendarMonth(cal, month);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateMonthJust(Date date) {
        final Calendar cal = toCalendar(date);
        moveToCalendarMonthJust(cal);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateMonthJust(Date date, int monthBeginDay) {
        final Calendar cal = toCalendar(date);
        moveToCalendarMonthJust(cal, monthBeginDay);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateMonthAdded(Date date, int month) {
        final Calendar cal = toCalendar(date);
        moveToCalendarMonthJustAdded(cal, month);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateMonthFor(Date date, int month) {
        final Calendar cal = toCalendar(date);
        moveToCalendarMonthJustFor(cal, month);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateMonthTerminal(Date date) {
        final Calendar cal = toCalendar(date);
        moveToCalendarMonthTerminal(cal);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateMonthTerminal(Date date, int monthBeginDay) {
        final Calendar cal = toCalendar(date);
        moveToCalendarMonthTerminal(cal, monthBeginDay);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateMonthTerminalAdded(Date date, int month) {
        final Calendar cal = toCalendar(date);
        moveToCalendarMonthTerminalAdded(cal, month);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateMonthTerminalFor(Date date, int month) {
        final Calendar cal = toCalendar(date);
        moveToCalendarMonthTerminalFor(cal, month);
        date.setTime(cal.getTimeInMillis());
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - [Day]
    protected static void moveToDateDay(Date date, int day) {
        final Calendar cal = toCalendar(date);
        moveToCalendarDay(cal, day);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateDayJust(Date date) {
        final Calendar cal = toCalendar(date);
        moveToCalendarDayJust(cal);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateDayJust(Date date, int dayBeginHour) {
        final Calendar cal = toCalendar(date);
        moveToCalendarDayJust(cal, dayBeginHour);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateDayJustAdded(Date date, int day) {
        final Calendar cal = toCalendar(date);
        moveToCalendarDayJustAdded(cal, day);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateDayJustFor(Date date, int day) {
        final Calendar cal = toCalendar(date);
        moveToCalendarDayJustFor(cal, day);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateDayTerminal(Date date) {
        final Calendar cal = toCalendar(date);
        moveToCalendarDayTerminal(cal);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateDayTerminal(Date date, int dayBeginHour) {
        final Calendar cal = toCalendar(date);
        moveToCalendarDayTerminal(cal, dayBeginHour);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateDayTerminalAdded(Date date, int day) {
        final Calendar cal = toCalendar(date);
        moveToCalendarDayTerminalAdded(cal, day);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateDayTerminalFor(Date date, int day) {
        final Calendar cal = toCalendar(date);
        moveToCalendarDayTerminalFor(cal, day);
        date.setTime(cal.getTimeInMillis());
    }

    // - - - - - - - - - - - - - - - - - - - - - - - -[Hour]
    protected static void moveToDateHour(Date date, int hour) {
        final Calendar cal = toCalendar(date);
        moveToCalendarHour(cal, hour);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateHourJust(Date date) {
        final Calendar cal = toCalendar(date);
        moveToCalendarHourJust(cal);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateHourJustAdded(Date date, int hour) {
        final Calendar cal = toCalendar(date);
        moveToCalendarHourJustAdded(cal, hour);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateHourJustFor(Date date, int hour) {
        final Calendar cal = toCalendar(date);
        moveToCalendarHourJustFor(cal, hour);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateHourJustNoon(Date date) {
        final Calendar cal = toCalendar(date);
        moveToCalendarHourJustNoon(cal);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateHourTerminal(Date date) {
        final Calendar cal = toCalendar(date);
        moveToCalendarHourTerminal(cal);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateHourTerminalAdded(Date date, int hour) {
        final Calendar cal = toCalendar(date);
        moveToCalendarHourTerminalAdded(cal, hour);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateHourTerminalFor(Date date, int hour) {
        final Calendar cal = toCalendar(date);
        moveToCalendarHourTerminalFor(cal, hour);
        date.setTime(cal.getTimeInMillis());
    }

    // - - - - - - - - - - - - - - - - - - - - - - -[Minute]
    protected static void moveToDateMinute(Date date, int minute) {
        final Calendar cal = toCalendar(date);
        moveToCalendarMinute(cal, minute);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateMinuteJust(Date date) {
        final Calendar cal = toCalendar(date);
        moveToCalendarMinuteJust(cal);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateMinuteJustAdded(Date date, int minute) {
        final Calendar cal = toCalendar(date);
        moveToCalendarMinuteJustAdded(cal, minute);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateMinuteJustFor(Date date, int minute) {
        final Calendar cal = toCalendar(date);
        moveToCalendarMinuteJustFor(cal, minute);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateMinuteTerminal(Date date) {
        final Calendar cal = toCalendar(date);
        moveToCalendarMinuteTerminal(cal);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateMinuteTerminalAdded(Date date, int minute) {
        final Calendar cal = toCalendar(date);
        moveToCalendarMinuteTerminalAdded(cal, minute);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateMinuteTerminalFor(Date date, int minute) {
        final Calendar cal = toCalendar(date);
        moveToCalendarMinuteTerminalFor(cal, minute);
        date.setTime(cal.getTimeInMillis());
    }

    // - - - - - - - - - - - - - - - - - - - - - - -[Second]
    protected static void moveToDateSecond(Date date, int second) {
        final Calendar cal = toCalendar(date);
        moveToCalendarSecond(cal, second);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateSecondJust(Date date) {
        final Calendar cal = toCalendar(date);
        moveToCalendarSecondJust(cal);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateSecondJustAdded(Date date, int second) {
        final Calendar cal = toCalendar(date);
        moveToCalendarSecondJustAdded(cal, second);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateSecondJustFor(Date date, int second) {
        final Calendar cal = toCalendar(date);
        moveToCalendarSecondJustFor(cal, second);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateSecondTerminal(Date date) {
        final Calendar cal = toCalendar(date);
        moveToCalendarSecondTerminal(cal);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateSecondTerminalAdded(Date date, int second) {
        final Calendar cal = toCalendar(date);
        moveToCalendarSecondTerminalAdded(cal, second);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateSecondTerminalFor(Date date, int second) {
        final Calendar cal = toCalendar(date);
        moveToCalendarSecondTerminalFor(cal, second);
        date.setTime(cal.getTimeInMillis());
    }

    // - - - - - - - - - - - - - - - - - - - - - - - -[Week]
    protected static void moveToDateWeekJust(Date date, int weekStartDay) {
        final Calendar cal = toCalendar(date);
        moveToCalendarWeekJust(cal, weekStartDay);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateWeekTerminal(Date date, int weekStartDay) {
        final Calendar cal = toCalendar(date);
        moveToCalendarWeekTerminal(cal, weekStartDay);
        date.setTime(cal.getTimeInMillis());
    }

    // - - - - - - - - - - - - - - - - - - [Quarter of Year]
    protected static void moveToDateQuarterOfYearJust(Date date) {
        final Calendar cal = toCalendar(date);
        moveToCalendarQuarterOfYearJust(cal);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateQuarterOfYearJust(Date date, int yearBeginMonth) {
        final Calendar cal = toCalendar(date);
        moveToCalendarQuarterOfYearJust(cal, yearBeginMonth);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateQuarterOfYearJustFor(Date date, int quarterOfYear) {
        final Calendar cal = toCalendar(date);
        moveToCalendarQuarterOfYearJustFor(cal, quarterOfYear);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateQuarterOfYearJustFor(Date date, int quarterOfYear, int yearBeginMonth) {
        final Calendar cal = toCalendar(date);
        moveToCalendarQuarterOfYearJustFor(cal, quarterOfYear, yearBeginMonth);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateQuarterOfYearTerminal(Date date) {
        final Calendar cal = toCalendar(date);
        moveToCalendarQuarterOfYearTerminal(cal);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateQuarterOfYearTerminal(Date date, int yearBeginMonth) {
        final Calendar cal = toCalendar(date);
        moveToCalendarQuarterOfYearTerminal(cal, yearBeginMonth);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateQuarterOfYearTerminalFor(Date date, int quarterOfYear) {
        final Calendar cal = toCalendar(date);
        moveToCalendarQuarterOfYearTerminalFor(cal, quarterOfYear);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void moveToDateQuarterOfYearTerminalFor(Date date, int quarterOfYear, int yearBeginMonth) {
        final Calendar cal = toCalendar(date);
        moveToCalendarQuarterOfYearTerminalFor(cal, quarterOfYear, yearBeginMonth);
        date.setTime(cal.getTimeInMillis());
    }

    // -----------------------------------------------------
    //                                            Clear Date
    //                                            ----------
    protected static void clearDateTimeParts(Date date) {
        final Calendar cal = toCalendar(date);
        clearCalendarTimeParts(cal);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void clearDateMinuteWithRear(Date date) {
        final Calendar cal = toCalendar(date);
        clearCalendarMinuteWithRear(cal);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void clearDateSecondWithRear(Date date) {
        final Calendar cal = toCalendar(date);
        clearCalendarSecondWithRear(cal);
        date.setTime(cal.getTimeInMillis());
    }

    protected static void clearDateMillisecond(Date date) {
        final Calendar cal = toCalendar(date);
        clearCalendarMillisecond(cal);
        date.setTime(cal.getTimeInMillis());
    }

    // -----------------------------------------------------
    //                                            DateFormat
    //                                            ----------
    public static DateFormat createDateFormat(String pattern) { // as lenient
        assertPatternNotNull("createDateFormat()", pattern);
        return doCreateDateFormat(pattern, (TimeZone) null, (Locale) null, false);
    }

    public static DateFormat createDateFormat(TimeZone timeZone, String pattern, Locale locale, boolean strict) {
        assertPatternNotNull("createDateFormat()", pattern);
        assertTimeZoneNotNull("createDateFormat()", timeZone);
        assertLocaleNotNull("createDateFormat()", locale);
        return doCreateDateFormat(pattern, timeZone, locale, strict);
    }

    protected static DateFormat doCreateDateFormat(String pattern, TimeZone timeZone, Locale locale, boolean strict) {
        final String realPattern = pattern != null ? pattern : DEFAULT_TIMESTAMP_PATTERN;
        final Locale realLocale = chooseRealLocale(locale);
        final SimpleDateFormat sdf = new SimpleDateFormat(realPattern, realLocale);
        final TimeZone realZone = chooseRealZone(timeZone);
        sdf.setTimeZone(realZone);
        sdf.setLenient(!strict);
        return sdf;
    }

    // ===================================================================================
    //                                                                           Timestamp
    //                                                                           =========
    /**
     * Convert the object to the instance that is time-stamp. <br>
     * Even if it's the sub class type, it returns a new instance. <br>
     * This method uses default date pattern based on 'yyyy-MM-dd HH:mm:ss.SSS'
     * with flexible-parsing if the object is string type.
     * @param obj The parsed object. (NullAllowed: if null or empty, returns null)
     * @return The instance of time-stamp. (NullAllowed: if the value is null or empty, it returns null.)
     * @throws ParseTimestampException When it failed to parse the string to time-stamp.
     * @throws ParseTimestampNumberFormatException When it failed to format the elements as number.
     * @throws ParseTimestampOutOfCalendarException When the time-stamp was out of calendar. (if BC, not thrown)
     */
    public static Timestamp toTimestamp(Object obj) {
        return doConvertToTimestamp(obj, (TimeZone) null, (String) null, (Locale) null);
    }

    /**
     * Convert the object to the instance that is time-stamp. <br>
     * Even if it's the sub class type, it returns a new instance. <br>
     * This method uses default date pattern based on 'yyyy-MM-dd HH:mm:ss.SSS'
     * with flexible-parsing if the object is string type.
     * @param obj The parsed object. (NullAllowed: if null or empty, returns null)
     * @param timeZone The time-zone to parse the string expression. (NotNull)
     * @return The instance of time-stamp. (NullAllowed: if the value is null or empty, it returns null.)
     * @throws ParseTimestampException When it failed to parse the string to time-stamp.
     * @throws ParseTimestampNumberFormatException When it failed to format the elements as number.
     * @throws ParseTimestampOutOfCalendarException When the time-stamp was out of calendar. (if BC, not thrown)
     */
    public static Timestamp toTimestamp(Object obj, TimeZone timeZone) {
        assertTimeZoneNotNull("toTimestamp()", timeZone);
        return doConvertToTimestamp(obj, timeZone, (String) null, (Locale) null);
    }

    /**
     * Convert the object to the instance that is time-stamp. <br>
     * Even if it's the sub class type, it returns a new instance.
     * @param obj The parsed object. (NullAllowed: if null or empty, returns null)
     * @param pattern The pattern format to parse when the object is string. (NotNull)
     * @return The instance of time-stamp. (NullAllowed: if the value is null or empty, it returns null.)
     * @throws ParseTimestampException When it failed to parse the string to time-stamp.
     * @throws ParseTimestampNumberFormatException When it failed to format the elements as number.
     * @throws ParseTimestampOutOfCalendarException When the time-stamp was out of calendar. (if BC, not thrown)
     */
    public static Timestamp toTimestamp(Object obj, String pattern) {
        assertPatternNotNull("toTimestamp()", pattern);
        return doConvertToTimestamp(obj, (TimeZone) null, pattern, (Locale) null);
    }

    /**
     * Convert the object to the instance that is time-stamp. <br>
     * Even if it's the sub class type, it returns a new instance.
     * @param obj The parsed object. (NullAllowed: if null or empty, returns null)
     * @param pattern The pattern format to parse when the object is string. (NotNull)
     * @param locale The locale to parse the date expression. (NotNull)
     * @return The instance of time-stamp. (NullAllowed: if the value is null or empty, it returns null.)
     * @throws ParseTimestampException When it failed to parse the string to time-stamp.
     * @throws ParseTimestampNumberFormatException When it failed to format the elements as number.
     * @throws ParseTimestampOutOfCalendarException When the time-stamp was out of calendar. (if BC, not thrown)
     */
    public static Timestamp toTimestamp(Object obj, String pattern, Locale locale) {
        assertPatternNotNull("toTimestamp()", pattern);
        assertLocaleNotNull("toTimestamp()", locale);
        return doConvertToTimestamp(obj, (TimeZone) null, pattern, locale);
    }

    /**
     * Convert the object to the instance that is time-stamp. <br>
     * Even if it's the sub class type, it returns a new instance.
     * @param obj The parsed object. (NullAllowed: if null or empty, returns null)
     * @param timeZone The time-zone to parse the string expression. (NotNull)
     * @param pattern The pattern format to parse when the object is string. (NotNull)
     * @param locale The locale to parse the date expression. (NotNull)
     * @return The instance of time-stamp. (NullAllowed: if the value is null or empty, it returns null.)
     * @throws ParseTimestampException When it failed to parse the string to time-stamp.
     * @throws ParseTimestampNumberFormatException When it failed to format the elements as number.
     * @throws ParseTimestampOutOfCalendarException When the time-stamp was out of calendar. (if BC, not thrown)
     */
    public static Timestamp toTimestamp(Object obj, TimeZone timeZone, String pattern, Locale locale) {
        assertTimeZoneNotNull("toTimestamp()", timeZone);
        assertPatternNotNull("toTimestamp()", pattern);
        assertLocaleNotNull("toTimestamp()", locale);
        return doConvertToTimestamp(obj, timeZone, pattern, locale);
    }

    protected static Timestamp doConvertToTimestamp(Object obj, TimeZone timeZone, String pattern, Locale locale) {
        if (obj == null) {
            return null;
        } else if (obj instanceof Timestamp) {
            final Timestamp paramTimestamp = (Timestamp) obj;
            if (Timestamp.class.equals(paramTimestamp.getClass())) { // pure time-stamp
                return paramTimestamp;
            } else { // sub class
                // because the time-stamp type is not final class.
                return new Timestamp(paramTimestamp.getTime());
            }
        } else if (obj instanceof LocalDate) {
            final LocalDate localDate = (LocalDate) obj;
            return doParseLocalDateAsTimestamp(localDate, timeZone);
        } else if (obj instanceof LocalDateTime) {
            final LocalDateTime localDateTime = (LocalDateTime) obj;
            return doParseLocalDateTimeAsTimestamp(localDateTime, timeZone);
        } else if (obj instanceof Date) {
            return new Timestamp(((Date) obj).getTime());
        } else if (obj instanceof String) {
            return doParseStringAsTimestamp((String) obj, timeZone, pattern, locale);
        } else if (obj instanceof Calendar) {
            return new Timestamp(((Calendar) obj).getTime().getTime());
        } else if (obj instanceof byte[]) {
            return toTimestamp(toSerializable((byte[]) obj)); // recursive
        } else if (obj instanceof Long) {
            return new Timestamp((Long) obj);
        } else {
            return doParseStringAsTimestamp(obj.toString(), timeZone, pattern, locale);
        }
    }

    protected static Timestamp doParseStringAsTimestamp(String str, TimeZone timeZone, String pattern, Locale locale) {
        if (str == null || str.trim().length() == 0) {
            return null;
        }
        boolean strict;
        if (pattern == null || pattern.trim().length() == 0) { // flexibly
            str = filterTimestampStringValueFlexibly(str);
            strict = !str.startsWith("-"); // not BC
            pattern = DEFAULT_TIMESTAMP_PATTERN;
        } else {
            strict = true;
        }
        final DateFormat df = doCreateDateFormat(pattern, timeZone, locale, strict);
        try {
            return new Timestamp(df.parse(str).getTime());
        } catch (ParseException e) {
            try {
                df.setLenient(true);
                df.parse(str); // no exception means illegal date
                String msg = "The timestamp expression is out of calendar:";
                msg = msg + " string=" + str + " pattern=" + pattern;
                throw new ParseTimestampOutOfCalendarException(msg, e);
            } catch (ParseException ignored) {
                String msg = "Failed to parse the string to timestamp:";
                msg = msg + " string=" + str + " pattern=" + pattern;
                throw new ParseTimestampException(msg, e);
            }
        }
    }

    protected static String filterTimestampStringValueFlexibly(final String pureStr) {
        String str = pureStr;
        try {
            final boolean includeTime = true; // off course
            final boolean includeMilli = true; // off course
            final boolean keepMillisMore = false; // time-stamp cannot use nanosecond
            str = filterDateStringValueFlexibly(str, includeTime, includeMilli, keepMillisMore); // based on date way
        } catch (ParseDateNumberFormatException e) {
            String msg = "Failed to format the timestamp as number:";
            msg = msg + " value=" + pureStr;
            throw new ParseTimestampNumberFormatException(msg, e);
        }
        return str;
    }

    public static class ParseTimestampException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public ParseTimestampException(String msg, Exception e) {
            super(msg, e);
        }
    }

    public static class ParseTimestampOutOfCalendarException extends ParseTimestampException {
        private static final long serialVersionUID = 1L;

        public ParseTimestampOutOfCalendarException(String msg, Exception e) {
            super(msg, e);
        }
    }

    public static class ParseTimestampNumberFormatException extends ParseTimestampException {
        private static final long serialVersionUID = 1L;

        public ParseTimestampNumberFormatException(String msg, Exception e) {
            super(msg, e);
        }
    }

    // ===================================================================================
    //                                                                                Time
    //                                                                                ====
    /**
     * Convert the object to the instance that is time. <br>
     * Even if it's the sub class type, it returns a new instance. <br>
     * This method uses default time pattern based on 'HH:mm:ss'
     * with flexible-parsing if the object is string type.
     * @param obj The parsed object. (NullAllowed)
     * @return The instance of time. (NullAllowed: if the value is null or empty, it returns null.)
     * @throws ParseTimeException When it failed to parse the string to time.
     * @throws ParseTimeNumberFormatException When it failed to format the elements as number.
     * @throws ParseTimeOutOfCalendarException When the time is out of calendar.
     */
    public static Time toTime(Object obj) {
        return doConvertToTime(obj, (TimeZone) null, (String) null, (Locale) null);
    }

    /**
     * Convert the object to the instance that is time. <br>
     * Even if it's the sub class type, it returns a new instance. <br>
     * This method uses default time pattern based on 'HH:mm:ss'
     * with flexible-parsing if the object is string type.
     * @param obj The parsed object. (NullAllowed)
     * @param timeZone The time-zone to parse the string expression. (NotNull)
     * @return The instance of time. (NullAllowed: if the value is null or empty, it returns null.)
     * @throws ParseTimeException When it failed to parse the string to time.
     * @throws ParseTimeNumberFormatException When it failed to format the elements as number.
     * @throws ParseTimeOutOfCalendarException When the time is out of calendar.
     */
    public static Time toTime(Object obj, TimeZone timeZone) {
        assertTimeZoneNotNull("toTime()", timeZone);
        return doConvertToTime(obj, timeZone, (String) null, (Locale) null);
    }

    /**
     * Convert the object to the instance that is time. <br>
     * Even if it's the sub class type, it returns a new instance.
     * @param obj The parsed object. (NullAllowed)
     * @param pattern The pattern format to parse when the object is string. (NotNull)
     * @return The instance of time. (NullAllowed: if the value is null or empty, it returns null.)
     * @throws ParseTimeException When it failed to parse the string to time.
     * @throws ParseTimeNumberFormatException When it failed to format the elements as number.
     * @throws ParseTimeOutOfCalendarException When the time is out of calendar.
     */
    public static Time toTime(Object obj, String pattern) {
        assertPatternNotNull("toTime()", pattern);
        return doConvertToTime(obj, (TimeZone) null, pattern, (Locale) null);
    }

    /**
     * Convert the object to the instance that is time. <br>
     * Even if it's the sub class type, it returns a new instance.
     * @param obj The parsed object. (NullAllowed)
     * @param pattern The pattern format to parse when the object is string. (NotNull)
     * @param locale The locale to parse the date expression. (NotNull)
     * @return The instance of time. (NullAllowed: if the value is null or empty, it returns null.)
     * @throws ParseTimeException When it failed to parse the string to time.
     * @throws ParseTimeNumberFormatException When it failed to format the elements as number.
     * @throws ParseTimeOutOfCalendarException When the time is out of calendar.
     */
    public static Time toTime(Object obj, String pattern, Locale locale) {
        assertPatternNotNull("toTime()", pattern);
        assertLocaleNotNull("toTime()", locale);
        return doConvertToTime(obj, (TimeZone) null, pattern, locale);
    }

    /**
     * Convert the object to the instance that is time. <br>
     * Even if it's the sub class type, it returns a new instance.
     * @param obj The parsed object. (NullAllowed)
     * @param timeZone The time-zone to parse the string expression. (NotNull)
     * @param pattern The pattern format to parse when the object is string. (NotNull)
     * @param locale The locale to parse the date expression. (NotNull)
     * @return The instance of time. (NullAllowed: if the value is null or empty, it returns null.)
     * @throws ParseTimeException When it failed to parse the string to time.
     * @throws ParseTimeNumberFormatException When it failed to format the elements as number.
     * @throws ParseTimeOutOfCalendarException When the time is out of calendar.
     */
    public static Time toTime(Object obj, TimeZone timeZone, String pattern, Locale locale) {
        assertTimeZoneNotNull("toTime()", timeZone);
        assertPatternNotNull("toTime()", pattern);
        assertLocaleNotNull("toTime()", locale);
        return doConvertToTime(obj, timeZone, pattern, locale);
    }

    protected static Time doConvertToTime(Object obj, TimeZone timeZone, String pattern, Locale locale) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof String) {
            return doParseStringAsTime((String) obj, timeZone, pattern, locale);
        } else if (obj instanceof Time) {
            final Time paramTime = (Time) obj;
            if (Time.class.equals(paramTime.getClass())) { // pure time
                return paramTime;
            } else { // sub class
                // because the time type is not final class.
                return new Time(paramTime.getTime());
            }
        } else if (isAnyLocalDate(obj)) {
            final String localTimePattern = DEFAULT_TIME_PATTERN;
            final Locale realLocale = chooseRealLocale(locale);
            if (obj instanceof LocalDate) {
                final String strTime = ((LocalDate) obj).format(DateTimeFormatter.ofPattern(localTimePattern, realLocale));
                return doParseStringAsTime(strTime, timeZone, pattern, realLocale);
            } else if (obj instanceof LocalDateTime) {
                final String strTime = ((LocalDateTime) obj).format(DateTimeFormatter.ofPattern(localTimePattern, realLocale));
                return doParseStringAsTime(strTime, timeZone, pattern, realLocale);
            } else if (obj instanceof LocalTime) {
                final String strTime = ((LocalTime) obj).format(DateTimeFormatter.ofPattern(localTimePattern, realLocale));
                return doParseStringAsTime(strTime, timeZone, pattern, realLocale);
            } else { // no way
                throw new IllegalStateException("Unknown local date: type=" + obj.getClass() + ", value=" + obj);
            }
        } else if (obj instanceof Date) {
            final Date date = (Date) obj;
            final Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(Calendar.YEAR, 1970);
            cal.set(Calendar.MONTH, Calendar.JANUARY);
            cal.set(Calendar.DATE, 1);
            return new Time(cal.getTimeInMillis());
        } else if (obj instanceof Calendar) {
            final Calendar cal = (Calendar) obj;
            cal.set(Calendar.YEAR, 1970);
            cal.set(Calendar.MONTH, Calendar.JANUARY);
            cal.set(Calendar.DATE, 1);
            return new Time(cal.getTimeInMillis());
        } else if (obj instanceof byte[]) {
            return toTime(toSerializable((byte[]) obj)); // recursive
        } else if (obj instanceof Long) {
            return toTime(toDate((Long) obj));
        } else {
            return doParseStringAsTime(obj.toString(), timeZone, pattern, locale);
        }
    }

    protected static Time doParseStringAsTime(String str, TimeZone timeZone, String pattern, Locale locale) {
        if (str == null || str.trim().length() == 0) {
            return null;
        }
        if (pattern == null || pattern.trim().length() == 0) { // flexibly
            final boolean includeMillis = false; // time cannot use millisecond
            final boolean keepMillisMore = false; // time cannot use nanosecond
            str = filterTimeStringValueFlexibly(str, includeMillis, keepMillisMore);
            pattern = DEFAULT_TIME_PATTERN;
        }
        final DateFormat df = doCreateDateFormat(pattern, timeZone, locale, true);
        try {
            return new Time(df.parse(str).getTime());
        } catch (ParseException e) {
            try {
                df.setLenient(true);
                df.parse(str); // no exception means illegal date
                String msg = "The time expression is out of calendar:";
                msg = msg + " string=" + str + " pattern=" + pattern;
                throw new ParseTimeOutOfCalendarException(msg, e);
            } catch (ParseException ignored) {
                String msg = "Failed to parse the string to time:";
                msg = msg + " string=" + str + " pattern=" + pattern;
                throw new ParseTimeException(msg, e);
            }
        }
    }

    protected static String filterTimeStringValueFlexibly(String pureStr, boolean includeMillis, boolean keepMillisMore) {
        String str = pureStr;
        str = str.trim();
        final int dateEndIndex = str.indexOf(" ");
        if (dateEndIndex >= 0) {
            // '2008-12-12 12:34:56' to '12:34:56'
            final String time = str.substring(dateEndIndex + " ".length());
            try {
                str = handleTimeZeroPrefix(time, pureStr, includeMillis, keepMillisMore);
            } catch (ParseDateNumberFormatException e) {
                String msg = "Failed to format the time as number:";
                msg = msg + " value=" + pureStr;
                throw new ParseTimeNumberFormatException(msg, e);
            }
        }
        return str;
    }

    public static class ParseTimeException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public ParseTimeException(String msg, Exception e) {
            super(msg, e);
        }
    }

    public static class ParseTimeNumberFormatException extends ParseTimeException {
        private static final long serialVersionUID = 1L;

        public ParseTimeNumberFormatException(String msg, Exception e) {
            super(msg, e);
        }
    }

    public static class ParseTimeOutOfCalendarException extends ParseTimeException {
        private static final long serialVersionUID = 1L;

        public ParseTimeOutOfCalendarException(String msg, Exception e) {
            super(msg, e);
        }
    }

    // ===================================================================================
    //                                                                           (sql)Date
    //                                                                           =========
    /**
     * Convert the object to the instance that is SQL-date. <br>
     * Even if it's the sub class type, it returns a new instance. <br>
     * This method uses default date pattern based on 'yyyy-MM-dd'
     * with flexible-parsing if the object is string type.
     * @param obj The parsed object. (NullAllowed)
     * @return The instance of SQL date. (NullAllowed)
     * @throws ParseSqlDateException When it failed to parse the string to SQL date.
     * @throws ParseSqlDateNumberFormatException When it failed to format the elements as number.
     * @throws ParseSqlDateOutOfCalendarException When the time is out of calendar.
     */
    public static java.sql.Date toSqlDate(Object obj) {
        return doConvertToSqlDate(obj, (TimeZone) null, (String) null, (Locale) null);
    }

    /**
     * Convert the object to the instance that is SQL-date. <br>
     * Even if it's the sub class type, it returns a new instance. <br>
     * This method uses default date pattern based on 'yyyy-MM-dd'
     * with flexible-parsing if the object is string type.
     * @param obj The parsed object. (NullAllowed)
     * @param timeZone The time-zone to parse the string expression. (NotNull)
     * @return The instance of SQL date. (NullAllowed)
     * @throws ParseSqlDateException When it failed to parse the string to SQL date.
     * @throws ParseSqlDateNumberFormatException When it failed to format the elements as number.
     * @throws ParseSqlDateOutOfCalendarException When the time is out of calendar.
     */
    public static java.sql.Date toSqlDate(Object obj, TimeZone timeZone) {
        assertTimeZoneNotNull("toSqlDate()", timeZone);
        return doConvertToSqlDate(obj, timeZone, (String) null, (Locale) null);
    }

    /**
     * Convert the object to the instance that is SQL-date cleared seconds. <br>
     * Even if it's the sub class type, it returns a new instance.
     * @param obj The parsed object. (NullAllowed)
     * @param pattern The pattern format to parse when the object is string. (NotNull)
     * @return The instance of SQL date. (NullAllowed)
     * @throws ParseSqlDateException When it failed to parse the string to SQL date.
     * @throws ParseSqlDateNumberFormatException When it failed to format the elements as number.
     * @throws ParseSqlDateOutOfCalendarException When the time is out of calendar.
     */
    public static java.sql.Date toSqlDate(Object obj, String pattern) {
        assertPatternNotNull("toSqlDate()", pattern);
        return doConvertToSqlDate(obj, (TimeZone) null, pattern, (Locale) null);
    }

    /**
     * Convert the object to the instance that is SQL-date cleared seconds. <br>
     * Even if it's the sub class type, it returns a new instance.
     * @param obj The parsed object. (NullAllowed)
     * @param pattern The pattern format to parse when the object is string. (NotNull)
     * @param locale The locale to parse the date expression. (NotNull) 
     * @return The instance of SQL date. (NullAllowed)
     * @throws ParseSqlDateException When it failed to parse the string to SQL date.
     * @throws ParseSqlDateNumberFormatException When it failed to format the elements as number.
     * @throws ParseSqlDateOutOfCalendarException When the time is out of calendar.
     */
    public static java.sql.Date toSqlDate(Object obj, String pattern, Locale locale) {
        assertPatternNotNull("toSqlDate()", pattern);
        assertLocaleNotNull("toSqlDate()", locale);
        return doConvertToSqlDate(obj, (TimeZone) null, pattern, locale);
    }

    /**
     * Convert the object to the instance that is SQL-date cleared seconds. <br>
     * Even if it's the sub class type, it returns a new instance.
     * @param obj The parsed object. (NullAllowed)
     * @param timeZone The time-zone to parse the string expression. (NotNull)
     * @param pattern The pattern format to parse when the object is string. (NotNull)
     * @param locale The locale to parse the date expression. (NotNull)
     * @return The instance of SQL date. (NullAllowed)
     * @throws ParseSqlDateException When it failed to parse the string to SQL date.
     * @throws ParseSqlDateNumberFormatException When it failed to format the elements as number.
     * @throws ParseSqlDateOutOfCalendarException When the time is out of calendar.
     */
    public static java.sql.Date toSqlDate(Object obj, TimeZone timeZone, String pattern, Locale locale) {
        assertTimeZoneNotNull("toSqlDate()", timeZone);
        assertPatternNotNull("toSqlDate()", pattern);
        assertLocaleNotNull("toSqlDate()", locale);
        return doConvertToSqlDate(obj, timeZone, pattern, locale);
    }

    protected static java.sql.Date doConvertToSqlDate(Object obj, TimeZone timeZone, String pattern, Locale locale) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof java.sql.Date) {
            final java.sql.Date resultDate;
            final java.sql.Date paramSqlDate = (java.sql.Date) obj;
            if (java.sql.Date.class.equals(paramSqlDate.getClass())) { // pure SQL-date
                resultDate = paramSqlDate;
            } else { // sub class
                // because the SQL-date type is not final class.
                resultDate = new java.sql.Date(paramSqlDate.getTime());
            }
            clearDateTimeParts(resultDate);
            return resultDate;
        }
        final Date date;
        try {
            date = doConvertToDate(obj, timeZone, pattern, locale);
        } catch (ParseDateNumberFormatException e) {
            String msg = "Failed to format the time as number:";
            msg = msg + " obj=" + obj + " pattern=" + pattern + " timeZone=" + timeZone + " locale=" + locale;
            throw new ParseSqlDateNumberFormatException(msg, e);
        } catch (ParseDateOutOfCalendarException e) {
            String msg = "The SQL-date expression is out of calendar:";
            msg = msg + " obj=" + obj + " pattern=" + pattern + " timeZone=" + timeZone + " locale=" + locale;
            throw new ParseSqlDateOutOfCalendarException(msg, e);
        } catch (ParseDateException e) {
            String msg = "Failed to parse the object to SQL-date:";
            msg = msg + " obj=" + obj + " pattern=" + pattern + " timeZone=" + timeZone + " locale=" + locale;
            throw new ParseSqlDateException(msg, e);
        }
        if (date != null) {
            clearDateTimeParts(date);
            return new java.sql.Date(date.getTime());
        }
        return null;
    }

    public static class ParseSqlDateException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public ParseSqlDateException(String msg, Exception e) {
            super(msg, e);
        }
    }

    public static class ParseSqlDateNumberFormatException extends ParseSqlDateException {
        private static final long serialVersionUID = 1L;

        public ParseSqlDateNumberFormatException(String msg, Exception e) {
            super(msg, e);
        }
    }

    public static class ParseSqlDateOutOfCalendarException extends ParseSqlDateException {
        private static final long serialVersionUID = 1L;

        public ParseSqlDateOutOfCalendarException(String msg, Exception e) {
            super(msg, e);
        }
    }

    // ===================================================================================
    //                                                                            Calendar
    //                                                                            ========
    public static Calendar toCalendar(Object obj) {
        return doConvertToCalendar(obj, (TimeZone) null, (String) null, (Locale) null);
    }

    public static Calendar toCalendar(Object obj, TimeZone timeZone) {
        assertTimeZoneNotNull("toCalendar()", timeZone);
        return doConvertToCalendar(obj, timeZone, (String) null, (Locale) null);
    }

    public static Calendar toCalendar(Object obj, String pattern) {
        assertPatternNotNull("toCalendar()", pattern);
        return doConvertToCalendar(obj, (TimeZone) null, pattern, (Locale) null);
    }

    public static Calendar toCalendar(Object obj, String pattern, Locale locale) {
        assertPatternNotNull("toCalendar()", pattern);
        assertLocaleNotNull("toCalendar()", locale);
        return doConvertToCalendar(obj, (TimeZone) null, pattern, locale);
    }

    public static Calendar toCalendar(Object obj, TimeZone timeZone, String pattern, Locale locale) {
        assertTimeZoneNotNull("toCalendar()", timeZone);
        assertPatternNotNull("toCalendar()", pattern);
        assertLocaleNotNull("toCalendar()", locale);
        return doConvertToCalendar(obj, timeZone, pattern, locale);
    }

    protected static Calendar doConvertToCalendar(Object obj, TimeZone timeZone, String pattern, Locale locale) {
        if (obj instanceof Calendar) {
            final Calendar original = ((Calendar) obj);
            final Calendar cal = Calendar.getInstance(locale); // not use original's one, specified is prior
            cal.setTimeInMillis(original.getTimeInMillis());
            cal.setTimeZone(original.getTimeZone());
            return cal; // new instance
        }
        final TimeZone realZone = chooseRealZone(timeZone);
        final Date date;
        try {
            date = doConvertToDate(obj, timeZone, pattern, locale);
        } catch (ParseDateNumberFormatException e) {
            String msg = "Failed to format the calendar as number:";
            msg = msg + " obj=" + obj + " pattern=" + pattern + " timeZone=" + timeZone + " locale=" + locale;
            throw new ParseCalendarNumberFormatException(msg, e);
        } catch (ParseDateOutOfCalendarException e) {
            String msg = "The calendar expression is out of calendar:";
            msg = msg + " obj=" + obj + " pattern=" + pattern + " timeZone=" + timeZone + " locale=" + locale;
            throw new ParseCalendarOutOfCalendarException(msg, e);
        } catch (ParseDateException e) {
            String msg = "Failed to parse the object to calendar:";
            msg = msg + " obj=" + obj + " pattern=" + pattern + " timeZone=" + timeZone + " locale=" + locale;
            throw new ParseCalendarException(msg, e);
        }
        if (date != null) {
            final Calendar cal = Calendar.getInstance(realZone);
            cal.setTime(date);
            return cal;
        }
        return null;
    }

    public static class ParseCalendarException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public ParseCalendarException(String msg, Exception e) {
            super(msg, e);
        }
    }

    public static class ParseCalendarNumberFormatException extends ParseCalendarException {
        private static final long serialVersionUID = 1L;

        public ParseCalendarNumberFormatException(String msg, Exception e) {
            super(msg, e);
        }
    }

    public static class ParseCalendarOutOfCalendarException extends ParseCalendarException {
        private static final long serialVersionUID = 1L;

        public ParseCalendarOutOfCalendarException(String msg, Exception e) {
            super(msg, e);
        }
    }

    // -----------------------------------------------------
    //                                          Add Calendar
    //                                          ------------
    // HandydDate uses these methods
    public static void addCalendarYear(Calendar cal, int years) {
        cal.add(Calendar.YEAR, years);
    }

    public static void addCalendarMonth(Calendar cal, int months) {
        cal.add(Calendar.MONTH, months);
    }

    public static void addCalendarDay(Calendar cal, int days) {
        cal.add(Calendar.DAY_OF_MONTH, days);
    }

    public static void addCalendarHour(Calendar cal, int hours) {
        cal.add(Calendar.HOUR_OF_DAY, hours);
    }

    public static void addCalendarMinute(Calendar cal, int minutes) {
        cal.add(Calendar.MINUTE, minutes);
    }

    public static void addCalendarSecond(Calendar cal, int seconds) {
        cal.add(Calendar.SECOND, seconds);
    }

    public static void addCalendarMillisecond(Calendar cal, int milliseconds) {
        cal.add(Calendar.MILLISECOND, milliseconds);
    }

    public static void addCalendarWeek(Calendar cal, int weeks) {
        cal.add(Calendar.WEEK_OF_MONTH, weeks);
    }

    public static void addCalendarQuarterOfYear(Calendar cal, int quartersOfYear) {
        addCalendarMonth(cal, quartersOfYear * 3);
    }

    // -----------------------------------------------------
    //                                      Move-to Calendar
    //                                      ----------------
    // HandydDate uses these methods
    // - - - - - - - - - - - - - - - - - - - - - - - -[Year]
    public static void moveToCalendarYear(Calendar cal, int year) {
        assertArgumentNotZeroInteger("year", year);
        if (year < 0) {
            year++; // BC headache
        }
        cal.set(Calendar.YEAR, year);
    }

    public static void moveToCalendarYearJust(Calendar cal) { // 2011/01/01 00:00:00:000
        moveToCalendarYearJust(cal, cal.getActualMinimum(Calendar.MONTH) + 1); // zero origin headache
    }

    public static void moveToCalendarYearJust(Calendar cal, int yearBeginMonth) {
        assertArgumentNotZeroInteger("yearBeginMonth", yearBeginMonth);
        final int realBeginValue;
        if (yearBeginMonth >= 0) {
            realBeginValue = yearBeginMonth;
        } else {
            realBeginValue = (yearBeginMonth * -1); // remove minus
            addCalendarYear(cal, -1);
        }
        moveToCalendarMonth(cal, realBeginValue);
        moveToCalendarMonthJust(cal);
    }

    public static void moveToCalendarYearJustAdded(Calendar cal, int years) {
        addCalendarYear(cal, years);
        moveToCalendarYearJust(cal);
    }

    public static void moveToCalendarYearJustFor(Calendar cal, int year) {
        moveToCalendarYear(cal, year);
        moveToCalendarYearJust(cal);
    }

    public static void moveToCalendarYearTerminal(Calendar cal) { // 2011/12/31 23:59:59.999
        moveToCalendarYearTerminal(cal, cal.getActualMinimum(Calendar.MONTH) + 1); // zero origin headache
    }

    public static void moveToCalendarYearTerminal(Calendar cal, int yearBeginMonth) {
        moveToCalendarYearJust(cal, yearBeginMonth);
        addCalendarYear(cal, 1);
        addCalendarMillisecond(cal, -1);
    }

    public static void moveToCalendarYearTerminalAdded(Calendar cal, int years) {
        addCalendarYear(cal, years);
        moveToCalendarYearTerminal(cal);
    }

    public static void moveToCalendarYearTerminalFor(Calendar cal, int year) {
        moveToCalendarYearJustFor(cal, year);
        moveToCalendarYearTerminal(cal);
    }

    // - - - - - - - - - - - - - - - - - - - - - - - [Month]
    public static void moveToCalendarMonth(Calendar cal, int month) {
        assertArgumentNotMinusNotZeroInteger("month", month);
        cal.set(Calendar.MONTH, month - 1); // zero origin headache
    }

    public static void moveToCalendarMonthJust(Calendar cal) { // 2011/11/01 00:00:00.000
        moveToCalendarMonthJust(cal, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
    }

    public static void moveToCalendarMonthJust(Calendar cal, int monthBeginDay) {
        assertArgumentNotZeroInteger("monthBeginDay", monthBeginDay);
        final int realBeginValue;
        if (monthBeginDay >= 0) {
            realBeginValue = monthBeginDay;
        } else {
            realBeginValue = (monthBeginDay * -1); // remove minus
            addCalendarMonth(cal, -1);
        }
        moveToCalendarDay(cal, realBeginValue);
        moveToCalendarDayJust(cal);
    }

    public static void moveToCalendarMonthJustAdded(Calendar cal, int months) {
        addCalendarMonth(cal, months);
        moveToCalendarMonthJust(cal);
    }

    public static void moveToCalendarMonthJustFor(Calendar cal, int month) {
        moveToCalendarMonth(cal, month);
        moveToCalendarMonthJust(cal);
    }

    public static void moveToCalendarMonthTerminal(Calendar cal) { // 2011/11/30 23:59:59.999
        moveToCalendarMonthTerminal(cal, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
    }

    public static void moveToCalendarMonthTerminal(Calendar cal, int monthBeginDay) {
        moveToCalendarMonthJust(cal, monthBeginDay);
        addCalendarMonth(cal, 1);
        addCalendarMillisecond(cal, -1);
    }

    public static void moveToCalendarMonthTerminalAdded(Calendar cal, int months) {
        addCalendarMonth(cal, months);
        moveToCalendarMonthTerminal(cal);
    }

    public static void moveToCalendarMonthTerminalFor(Calendar cal, int month) {
        moveToCalendarMonthJustFor(cal, month);
        moveToCalendarMonthTerminal(cal);
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - [Day]
    public static void moveToCalendarDay(Calendar cal, int day) {
        assertArgumentNotMinusNotZeroInteger("day", day);
        cal.set(Calendar.DAY_OF_MONTH, day);
    }

    public static void moveToCalendarDayJust(Calendar cal) { // 2011/11/17 00:00:00.000
        moveToCalendarDayJust(cal, cal.getActualMinimum(Calendar.HOUR_OF_DAY));
    }

    public static void moveToCalendarDayJust(Calendar cal, int dayBeginHour) {
        final int realBeginValue;
        if (dayBeginHour >= 0) {
            realBeginValue = dayBeginHour;
        } else {
            realBeginValue = (dayBeginHour * -1); // remove minus
            addCalendarDay(cal, -1);
        }
        moveToCalendarHour(cal, realBeginValue);
        clearCalendarMinuteWithRear(cal);
    }

    public static void moveToCalendarDayJustAdded(Calendar cal, int days) {
        addCalendarDay(cal, days);
        moveToCalendarDayJust(cal);
    }

    public static void moveToCalendarDayJustFor(Calendar cal, int day) {
        moveToCalendarDay(cal, day);
        moveToCalendarDayJust(cal);
    }

    public static void moveToCalendarDayTerminal(Calendar cal) { // 2011/11/17 23:59:59.999
        moveToCalendarDayTerminal(cal, cal.getActualMinimum(Calendar.HOUR_OF_DAY));
    }

    public static void moveToCalendarDayTerminal(Calendar cal, int dayBeginHour) {
        moveToCalendarDayJust(cal, dayBeginHour);
        addCalendarDay(cal, 1);
        addCalendarMillisecond(cal, -1);
    }

    public static void moveToCalendarDayTerminalAdded(Calendar cal, int days) {
        addCalendarDay(cal, days);
        moveToCalendarDayTerminal(cal);
    }

    public static void moveToCalendarDayTerminalFor(Calendar cal, int day) {
        moveToCalendarDayJustFor(cal, day);
        moveToCalendarDayTerminal(cal);
    }

    // - - - - - - - - - - - - - - - - - - - - - - - -[Hour]
    public static void moveToCalendarHour(Calendar cal, int hour) {
        assertArgumentNotMinusInteger("hour", hour);
        cal.set(Calendar.HOUR_OF_DAY, hour);
    }

    public static void moveToCalendarHourJust(Calendar cal) { // 2011/11/17 11:00:00.000
        clearCalendarMinuteWithRear(cal);
    }

    public static void moveToCalendarHourJustAdded(Calendar cal, int hours) {
        addCalendarHour(cal, hours);
        moveToCalendarHourJust(cal);
    }

    public static void moveToCalendarHourJustFor(Calendar cal, int hour) {
        moveToCalendarHour(cal, hour);
        moveToCalendarHourJust(cal);
    }

    public static void moveToCalendarHourJustNoon(Calendar cal) {
        moveToCalendarHourJustFor(cal, 12);
    }

    public static void moveToCalendarHourTerminal(Calendar cal) { // 2011/11/17 11:59:59.999
        moveToCalendarMinute(cal, cal.getActualMaximum(Calendar.MINUTE));
        moveToCalendarMinuteTerminal(cal);
    }

    public static void moveToCalendarHourTerminalAdded(Calendar cal, int hours) {
        addCalendarHour(cal, hours);
        moveToCalendarHourTerminal(cal);
    }

    public static void moveToCalendarHourTerminalFor(Calendar cal, int hour) {
        moveToCalendarHourJustFor(cal, hour);
        moveToCalendarHourTerminal(cal);
    }

    // - - - - - - - - - - - - - - - - - - - - - - -[Minute]
    public static void moveToCalendarMinute(Calendar cal, int minute) {
        assertArgumentNotMinusInteger("minute", minute);
        cal.set(Calendar.MINUTE, minute);
    }

    public static void moveToCalendarMinuteJust(Calendar cal) {
        clearCalendarSecondWithRear(cal);
    }

    public static void moveToCalendarMinuteJustAdded(Calendar cal, int minutes) {
        addCalendarMinute(cal, minutes);
        moveToCalendarSecondJust(cal);
    }

    public static void moveToCalendarMinuteJustFor(Calendar cal, int minute) {
        moveToCalendarMinute(cal, minute);
        moveToCalendarSecondJust(cal);
    }

    public static void moveToCalendarMinuteTerminal(Calendar cal) {
        moveToCalendarSecond(cal, cal.getActualMaximum(Calendar.SECOND));
        moveToCalendarSecondTerminal(cal);
    }

    public static void moveToCalendarMinuteTerminalAdded(Calendar cal, int minutes) {
        addCalendarMinute(cal, minutes);
        moveToCalendarMinuteTerminal(cal);
    }

    public static void moveToCalendarMinuteTerminalFor(Calendar cal, int minute) {
        moveToCalendarMinuteJustFor(cal, minute);
        moveToCalendarMinuteTerminal(cal);
    }

    // - - - - - - - - - - - - - - - - - - - - - - -[Second]
    public static void moveToCalendarSecond(Calendar cal, int second) {
        assertArgumentNotMinusInteger("second", second);
        cal.set(Calendar.SECOND, second);
    }

    public static void moveToCalendarSecondJust(Calendar cal) {
        clearCalendarMillisecond(cal);
    }

    public static void moveToCalendarSecondJustAdded(Calendar cal, int seconds) {
        addCalendarSecond(cal, seconds);
        moveToCalendarSecondJust(cal);
    }

    public static void moveToCalendarSecondJustFor(Calendar cal, int second) {
        moveToCalendarSecond(cal, second);
        moveToCalendarSecondJust(cal);
    }

    public static void moveToCalendarSecondTerminal(Calendar cal) {
        moveToCalendarMillisecond(cal, cal.getActualMaximum(Calendar.MILLISECOND));
    }

    public static void moveToCalendarSecondTerminalAdded(Calendar cal, int seconds) {
        addCalendarSecond(cal, seconds);
        moveToCalendarSecondTerminal(cal);
    }

    public static void moveToCalendarSecondTerminalFor(Calendar cal, int second) {
        moveToCalendarSecondJustFor(cal, second);
        moveToCalendarSecondTerminal(cal);
    }

    // - - - - - - - - - - - - - - - - - - - - [Millisecond]
    public static void moveToCalendarMillisecond(Calendar cal, int millisecond) {
        assertArgumentNotMinusInteger("millisecond", millisecond);
        cal.set(Calendar.MILLISECOND, millisecond);
    }

    // - - - - - - - - - - - - - - - - - - - - - - - -[Week]
    public static void moveToCalendarWeekOfMonth(Calendar cal, int weekOfMonth) {
        assertArgumentNotMinusInteger("weekOfMonth", weekOfMonth);
        cal.set(Calendar.WEEK_OF_MONTH, weekOfMonth);
    }

    public static void moveToCalendarWeekOfYear(Calendar cal, int weekOfYear) {
        assertArgumentNotMinusInteger("weekOfYear", weekOfYear);
        cal.set(Calendar.WEEK_OF_YEAR, weekOfYear);
    }

    public static void moveToCalendarWeekJust(Calendar cal) {
        moveToCalendarWeekJust(cal, Calendar.SUNDAY); // as default
    }

    public static void moveToCalendarWeekJust(Calendar cal, int weekBeginDayOfWeek) {
        final int dayOfWeekDef = Calendar.DAY_OF_WEEK;
        final int currentDayOfWeek = cal.get(dayOfWeekDef);
        cal.set(dayOfWeekDef, weekBeginDayOfWeek);
        if (currentDayOfWeek < weekBeginDayOfWeek) {
            addCalendarWeek(cal, -1);
        }
        clearCalendarTimeParts(cal);
    }

    public static void moveToCalendarWeekTerminal(Calendar cal) {
        moveToCalendarWeekTerminal(cal, Calendar.SUNDAY);
    }

    public static void moveToCalendarWeekTerminal(Calendar cal, int weekBeginDayOfWeek) {
        final int dayOfWeekDef = Calendar.DAY_OF_WEEK;
        final int currentDayOfWeek = cal.get(dayOfWeekDef);
        cal.set(dayOfWeekDef, weekBeginDayOfWeek);
        if (currentDayOfWeek >= weekBeginDayOfWeek) {
            addCalendarWeek(cal, 1);
        }
        addCalendarDay(cal, -1);
        moveToCalendarDayTerminal(cal);
    }

    // - - - - - - - - - - - - - - - - - - [Quarter of Year]
    public static void moveToCalendarQuarterOfYearJust(Calendar cal) {
        moveToCalendarQuarterOfYearJust(cal, 1);
    }

    public static void moveToCalendarQuarterOfYearJust(Calendar cal, int yearBeginMonth) {
        assertArgumentNotMinusNotZeroInteger("yearBeginMonth", yearBeginMonth);
        int month = cal.get(Calendar.MONTH) + 1; // zero origin headache
        if (month < yearBeginMonth) {
            month = month + 12;
        }
        final int firstBeginMonth = yearBeginMonth;
        final int secondBeginMonth = firstBeginMonth + 3;
        final int thirdBeginMonth = secondBeginMonth + 3;
        final int fourthBeginMonth = thirdBeginMonth + 3;
        final int addedMonth;
        if (month >= firstBeginMonth && month <= (firstBeginMonth + 2)) {
            addedMonth = firstBeginMonth - month;
        } else if (month >= secondBeginMonth && month <= (secondBeginMonth + 2)) {
            addedMonth = secondBeginMonth - month;
        } else if (month >= thirdBeginMonth && month <= (thirdBeginMonth + 2)) {
            addedMonth = thirdBeginMonth - month;
        } else {
            addedMonth = fourthBeginMonth - month;
        }
        moveToCalendarMonthJustAdded(cal, addedMonth);
    }

    public static void moveToCalendarQuarterOfYearJustAdded(Calendar cal, int quartersOfYear) {
        moveToCalendarQuarterOfYearJustAdded(cal, quartersOfYear, 1);
    }

    public static void moveToCalendarQuarterOfYearJustAdded(Calendar cal, int quartersOfYear, int yearBeginMonth) {
        addCalendarQuarterOfYear(cal, quartersOfYear);
        moveToCalendarQuarterOfYearJust(cal, yearBeginMonth);
    }

    public static void moveToCalendarQuarterOfYearJustFor(Calendar cal, int quarterOfYear) {
        assertArgumentNotMinusNotZeroInteger("quarterOfYear", quarterOfYear);
        moveToCalendarQuarterOfYearJustFor(cal, quarterOfYear, 1);
    }

    public static void moveToCalendarQuarterOfYearJustFor(Calendar cal, int quarterOfYear, int yearBeginMonth) {
        assertArgumentNotMinusNotZeroInteger("quarterOfYear", quarterOfYear);
        final int month = cal.get(Calendar.MONTH) + 1; // zero origin headache
        if (month < yearBeginMonth) {
            addCalendarYear(cal, -1);
        }
        moveToCalendarYearJust(cal, yearBeginMonth);
        moveToCalendarMonthJustAdded(cal, ((quarterOfYear - 1) * 3));
    }

    public static void moveToCalendarQuarterOfYearTerminal(Calendar cal) {
        moveToCalendarQuarterOfYearTerminal(cal, 1);
    }

    public static void moveToCalendarQuarterOfYearTerminal(Calendar cal, int yearBeginMonth) {
        moveToCalendarQuarterOfYearJust(cal, yearBeginMonth);
        moveToCalendarMonthTerminalAdded(cal, 2);
    }

    public static void moveToCalendarQuarterOfYearTerminalAdded(Calendar cal, int quartersOfYear) {
        moveToCalendarQuarterOfYearTerminalAdded(cal, quartersOfYear, 1);
    }

    public static void moveToCalendarQuarterOfYearTerminalAdded(Calendar cal, int quartersOfYear, int yearBeginMonth) {
        addCalendarQuarterOfYear(cal, quartersOfYear);
        moveToCalendarQuarterOfYearTerminal(cal, yearBeginMonth);
    }

    public static void moveToCalendarQuarterOfYearTerminalFor(Calendar cal, int quarterOfYear) {
        moveToCalendarQuarterOfYearTerminalFor(cal, quarterOfYear, 1);
    }

    public static void moveToCalendarQuarterOfYearTerminalFor(Calendar cal, int quarterOfYear, int yearBeginMonth) {
        moveToCalendarQuarterOfYearJustFor(cal, quarterOfYear, yearBeginMonth);
        moveToCalendarMonthTerminalAdded(cal, 2);
    }

    // -----------------------------------------------------
    //                                           Clear Parts
    //                                           -----------
    // HandydDate uses these methods
    public static void clearCalendarTimeParts(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, cal.getActualMinimum(Calendar.HOUR_OF_DAY));
        clearCalendarMinuteWithRear(cal);
    }

    public static void clearCalendarMinuteWithRear(Calendar cal) {
        cal.set(Calendar.MINUTE, cal.getActualMinimum(Calendar.MINUTE));
        clearCalendarSecondWithRear(cal);
    }

    public static void clearCalendarSecondWithRear(Calendar cal) {
        cal.set(Calendar.SECOND, cal.getActualMinimum(Calendar.SECOND));
        clearCalendarMillisecond(cal);
    }

    public static void clearCalendarMillisecond(Calendar cal) {
        cal.set(Calendar.MILLISECOND, cal.getActualMinimum(Calendar.MILLISECOND));
    }

    // -----------------------------------------------------
    //                                              Localize
    //                                              --------
    public static Calendar localize(Calendar cal) {
        if (cal == null) {
            return cal;
        }
        final Calendar localCal = Calendar.getInstance();
        localCal.setTimeInMillis(cal.getTimeInMillis());
        return localCal;
    }

    // ===================================================================================
    //                                                                             Boolean
    //                                                                             =======
    public static Boolean toBoolean(Object obj) {
        if (obj == null) {
            return (Boolean) obj;
        } else if (obj instanceof Boolean) {
            return (Boolean) obj;
        } else if (obj instanceof Number) {
            final int num = ((Number) obj).intValue();
            if (num == 1) {
                return Boolean.TRUE;
            } else if (num == 0) {
                return Boolean.FALSE;
            } else {
                String msg = "Failed to parse the boolean number: number=" + num;
                throw new ParseBooleanException(msg);
            }
        } else if (obj instanceof String) {
            final String str = (String) obj;
            if ("true".equalsIgnoreCase(str)) {
                return Boolean.TRUE;
            } else if ("false".equalsIgnoreCase(str)) {
                return Boolean.FALSE;
            } else if (str.equalsIgnoreCase("1")) {
                return Boolean.TRUE;
            } else if (str.equalsIgnoreCase("0")) {
                return Boolean.FALSE;
            } else if (str.equalsIgnoreCase("t")) {
                return Boolean.TRUE;
            } else if (str.equalsIgnoreCase("f")) {
                return Boolean.FALSE;
            } else {
                String msg = "Failed to parse the boolean string: value=" + str;
                throw new ParseBooleanException(msg);
            }
        } else if (obj instanceof byte[]) {
            return toBoolean(toSerializable((byte[]) obj)); // recursive
        } else {
            return Boolean.FALSE; // couldn't parse
        }
    }

    public static boolean toPrimitiveBoolean(Object obj) {
        Boolean wrapper = toBoolean(obj);
        return wrapper != null ? wrapper.booleanValue() : false;
    }

    public static class ParseBooleanException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public ParseBooleanException(String msg) {
            super(msg);
        }
    }

    // ===================================================================================
    //                                                                                UUID
    //                                                                                ====
    public static UUID toUUID(Object obj) {
        if (obj == null) {
            return (UUID) obj;
        } else if (obj instanceof UUID) {
            return (UUID) obj;
        } else if (obj instanceof String) {
            return toUUIDFromString((String) obj);
        } else {
            return toUUIDFromString(obj.toString());
        }
    }

    protected static UUID toUUIDFromString(String str) {
        try {
            return UUID.fromString(str);
        } catch (RuntimeException e) {
            String msg = "Failed to parse the string as UUID:";
            msg = msg + "str=" + str;
            throw new ParseUUIDException(msg);
        }
    }

    public static class ParseUUIDException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public ParseUUIDException(String msg) {
            super(msg);
        }

        public ParseUUIDException(String msg, Throwable e) {
            super(msg, e);
        }
    }

    // ===================================================================================
    //                                                                              Binary
    //                                                                              ======
    public static byte[] toBinary(Serializable obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof byte[]) {
            return (byte[]) obj;
        }
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            try {
                return baos.toByteArray();
            } finally {
                oos.close();
            }
        } catch (Exception e) {
            String msg = "Failed to convert the object to binary: obj=" + obj;
            throw new IllegalStateException(msg, e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <SER extends Serializable> SER toSerializable(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        try {
            final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            final ObjectInputStream ois = new ObjectInputStream(bais);
            try {
                return (SER) ois.readObject();
            } finally {
                ois.close();
            }
        } catch (Exception e) {
            String msg = "Failed to convert the object to binary: bytes.length=" + bytes.length;
            throw new IllegalStateException(msg, e);
        }
    }

    // ===================================================================================
    //                                                                             Wrapper
    //                                                                             =======
    public static Object toWrapper(Object obj, Class<?> type) {
        if (type == int.class) {
            final Integer it = toInteger(obj);
            if (it != null) {
                return it;
            }
            return Integer.valueOf(0);
        } else if (type == double.class) {
            Double db = toDouble(obj);
            if (db != null) {
                return db;
            }
            return Double.valueOf(0);
        } else if (type == long.class) {
            Long lg = toLong(obj);
            if (lg != null) {
                return lg;
            }
            return Long.valueOf(0L);
        } else if (type == float.class) {
            Float ft = toFloat(obj);
            if (ft != null) {
                return ft;
            }
            return Float.valueOf(0);
        } else if (type == short.class) {
            Short st = toShort(obj);
            if (st != null) {
                return st;
            }
            return Short.valueOf((short) 0);
        } else if (type == boolean.class) {
            Boolean bl = toBoolean(obj);
            if (bl != null) {
                return bl;
            }
            return Boolean.FALSE;
        } else if (type == byte.class) {
            Byte bt = toByte(obj);
            if (bt != null) {
                return bt;
            }
            return Byte.valueOf((byte) 0);
        }
        return obj;
    }

    // ===================================================================================
    //                                                                      DBFlute System
    //                                                                      ==============
    protected static Locale chooseRealLocale(Locale locale) {
        return locale != null ? locale : DBFluteSystem.getFinalLocale();
    }

    protected static TimeZone chooseRealZone(TimeZone timeZone) {
        return timeZone != null ? timeZone : DBFluteSystem.getFinalTimeZone();
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    // -----------------------------------------------------
    //                                  DecimalFormatSymbols
    //                                  --------------------
    protected static Map<Locale, DecimalFormatSymbols> symbolsCache = new ConcurrentHashMap<Locale, DecimalFormatSymbols>();

    protected static DecimalFormatSymbols getDecimalFormatSymbols(Locale locale) {
        DecimalFormatSymbols symbols = (DecimalFormatSymbols) symbolsCache.get(locale);
        if (symbols == null) {
            symbols = new DecimalFormatSymbols(locale);
            symbolsCache.put(locale, symbols);
        }
        return symbols;
    }

    // -----------------------------------------------------
    //                                                String
    //                                                ------
    protected static String replace(String text, String from, String to) {
        if (text == null || from == null || to == null) {
            return null;
        }
        final StringBuilder sb = new StringBuilder();
        int pos = 0;
        int pos2 = 0;
        do {
            pos = text.indexOf(from, pos2);
            if (pos == 0) {
                sb.append(to);
                pos2 = from.length();
            } else if (pos > 0) {
                sb.append(text.substring(pos2, pos));
                sb.append(to);
                pos2 = pos + from.length();
            } else {
                sb.append(text.substring(pos2));
                return sb.toString();
            }
        } while (true);
    }

    protected static String[] split(final String str, final String delimiter) {
        if (str == null || str.trim().length() == 0) {
            return EMPTY_STRINGS;
        }
        final List<String> list = new ArrayList<String>();
        final StringTokenizer st = new StringTokenizer(str, delimiter);
        while (st.hasMoreElements()) {
            list.add(st.nextToken());
        }
        return (String[]) list.toArray(new String[list.size()]);
    }

    // ===================================================================================
    //                                                                       Assert Helper
    //                                                                       =============
    protected static void assertTimeZoneNotNull(String methodName, TimeZone timeZone) {
        if (timeZone == null) {
            String msg = "The argument 'timeZone' should not be null: method=" + methodName;
            throw new IllegalArgumentException(msg);
        }
    }

    protected static void assertPatternNotNull(String methodName, String pattern) {
        if (pattern == null) {
            String msg = "The argument 'pattern' should not be null: method=" + methodName;
            throw new IllegalArgumentException(msg);
        }
    }

    protected static void assertLocaleNotNull(String methodName, Locale locale) {
        if (locale == null) {
            String msg = "The argument 'locale' should not be null: method=" + methodName;
            throw new IllegalArgumentException(msg);
        }
    }

    protected static void assertArgumentNotZeroInteger(String name, int value) {
        if (value == 0) {
            String msg = "The argument '" + name + "' should be plus or minus value: " + value;
            throw new IllegalArgumentException(msg);
        }
    }

    protected static void assertArgumentNotMinusInteger(String name, int value) {
        if (value < 0) {
            String msg = "The argument '" + name + "' should be plus or zero value: " + value;
            throw new IllegalArgumentException(msg);
        }
    }

    protected static void assertArgumentNotMinusNotZeroInteger(String name, int value) {
        if (value <= 0) {
            String msg = "The argument '" + name + "' should be plus value: " + value;
            throw new IllegalArgumentException(msg);
        }
    }
}
