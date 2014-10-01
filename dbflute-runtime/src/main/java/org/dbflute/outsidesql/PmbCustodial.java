/*
 * Copyright 2014-2014 the original author or authors.
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
package org.dbflute.outsidesql;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.dbflute.cbean.coption.FromToOption;
import org.dbflute.exception.CharParameterShortSizeException;
import org.dbflute.exception.RequiredOptionNotFoundException;
import org.dbflute.system.DBFluteSystem;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.DfTypeUtil;
import org.dbflute.util.Srl;

/**
 * @author jflute
 */
public class PmbCustodial {

    // ===================================================================================
    //                                                                              String
    //                                                                              ======
    /**
     * @param value Query value. (NullAllowed)
     * @return Converted value. (NullAllowed)
     */
    public static String convertEmptyToNull(String value) {
        return Srl.isEmpty(value) ? null : value;
    }

    /**
     * @param parameterName The name of the parameter. (NotNull)
     * @param value The value of the parameter. (NullAllowed)
     * @param size The size of parameter type. (NullAllowed)
     * @param mode The handling mode. (NotNull)
     * @return The filtered value. (NullAllowed)
     */
    public static String handleShortChar(String parameterName, String value, Integer size, ShortCharHandlingMode mode) {
        if (parameterName == null || parameterName.trim().length() == 0) {
            String msg = "The argument 'parameterName' should not be null or empty:";
            msg = msg + " value=" + value + " size=" + size + " mode=" + mode;
            throw new IllegalArgumentException(msg);
        }
        if (mode == null) {
            String msg = "The argument 'mode' should not be null:";
            msg = msg + " parameterName=" + parameterName + " value=" + value + " size=" + size;
            throw new IllegalArgumentException(msg);
        }
        if (value == null) {
            return null;
        }
        if (size == null) {
            return value;
        }
        if (value.length() >= size) {
            return value;
        }
        if (mode.equals(ShortCharHandlingMode.RFILL)) {
            return Srl.rfill(value, size);
        } else if (mode.equals(ShortCharHandlingMode.LFILL)) {
            return Srl.lfill(value, size);
        } else if (mode.equals(ShortCharHandlingMode.EXCEPTION)) {
            String msg = "The size of the parameter '" + parameterName + "' should be " + size + ":";
            msg = msg + " value=[" + value + "] size=" + value.length();
            throw new CharParameterShortSizeException(msg);
        } else {
            return value;
        }
    }

    public static enum ShortCharHandlingMode {
        RFILL("R"), LFILL("L"), EXCEPTION("E"), NONE("N");
        private static final Map<String, ShortCharHandlingMode> _codeValueMap = new HashMap<String, ShortCharHandlingMode>();
        static {
            for (ShortCharHandlingMode value : values()) {
                _codeValueMap.put(value.code().toLowerCase(), value);
            }
        }
        protected final String _code;

        private ShortCharHandlingMode(String code) {
            _code = code;
        }

        public static ShortCharHandlingMode codeOf(Object code) {
            if (code == null) {
                return null;
            }
            if (code instanceof ShortCharHandlingMode) {
                return (ShortCharHandlingMode) code;
            }
            return _codeValueMap.get(code.toString().toLowerCase());
        }

        public String code() {
            return _code;
        }
    }

    // ===================================================================================
    //                                                                                Date
    //                                                                                ====
    public static Date toUtilDate(Object date, TimeZone specifiedZone) {
        // local date and sub class of date to pure date
        return DfTypeUtil.toDate(date, chooseRealTimeZone(specifiedZone));
    }

    @SuppressWarnings("unchecked")
    public static <DATE> DATE toLocalDate(Date date, Class<DATE> localType, TimeZone timeZone) {
        if (LocalDate.class.isAssignableFrom(localType)) {
            return (DATE) DfTypeUtil.toLocalDate(date, timeZone);
        } else if (LocalDateTime.class.isAssignableFrom(localType)) {
            return (DATE) DfTypeUtil.toLocalDateTime(date, timeZone);
        }
        return null; // unreachable
    }

    public static String formatUtilDate(Date date, String pattern, TimeZone specifiedZone) {
        return DfTypeUtil.toStringDate(date, pattern, chooseRealTimeZone(specifiedZone));
    }

    public static void assertFromToOptionValid(String name, FromToOption option) {
        if (option == null) {
            String msg = "The from-to option is required!";
            throw new RequiredOptionNotFoundException(msg);
        }
    }

    public static FromToOption createFromToOption(TimeZone specifiedZone) {
        return new FromToOption().zone(chooseRealTimeZone(specifiedZone));
    }

    public static TimeZone chooseRealTimeZone(TimeZone specifiedZone) {
        return specifiedZone != null ? specifiedZone : DBFluteSystem.getFinalTimeZone();
    }

    // ===================================================================================
    //                                                                             Various
    //                                                                             =======
    @SuppressWarnings("unchecked")
    public static <NUMBER extends Number> NUMBER toNumber(Object obj, Class<NUMBER> type) { // might be called by option handling
        return (NUMBER) DfTypeUtil.toNumber(obj, type);
    }

    public static Boolean toBoolean(Object obj) {
        return DfTypeUtil.toBoolean(obj);
    }

    public static String formatByteArray(byte[] bytes) {
        return "byte[" + (bytes != null ? String.valueOf(bytes.length) : "null") + "]";
    }

    @SuppressWarnings("unchecked")
    public static <ELEMENT> ArrayList<ELEMENT> newArrayList(ELEMENT... elements) { // might be called by option handling
        Object obj = DfCollectionUtil.newArrayList(elements);
        return (ArrayList<ELEMENT>) obj; // to avoid the warning between JDK6 and JDK7
    }
}
