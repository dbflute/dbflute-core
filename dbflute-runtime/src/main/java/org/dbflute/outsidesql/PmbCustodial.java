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
package org.dbflute.outsidesql;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.dbflute.FunCustodial;
import org.dbflute.cbean.coption.FromToOption;
import org.dbflute.cbean.coption.LikeSearchOption;
import org.dbflute.exception.IllegalOutsideSqlOperationException;
import org.dbflute.exception.RequiredOptionNotFoundException;
import org.dbflute.jdbc.ShortCharHandlingMode;
import org.dbflute.system.DBFluteSystem;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.DfTypeUtil;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.1.0 (2014/10/02 Thursday)
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
    public static String handleShortChar(String parameterName, String value, Integer size, PmbShortCharHandlingMode mode) {
        if (mode == null) {
            throw new IllegalArgumentException("The argument 'mode' should not be null: " + parameterName);
        }
        return FunCustodial.handleShortChar(parameterName, value, size, mode.toWrappedMode());
    }

    public static enum PmbShortCharHandlingMode {
        RFILL(ShortCharHandlingMode.RFILL) //
        , LFILL(ShortCharHandlingMode.LFILL) //
        , EXCEPTION(ShortCharHandlingMode.EXCEPTION) //
        , NONE(ShortCharHandlingMode.NONE);
        protected final ShortCharHandlingMode _mode;

        private PmbShortCharHandlingMode(ShortCharHandlingMode mode) {
            _mode = mode;
        }

        public ShortCharHandlingMode toWrappedMode() {
            return _mode;
        }
    }

    public static void assertLikeSearchOptionValid(String name, LikeSearchOption option) {
        if (option == null) {
            throw new RequiredOptionNotFoundException("The like-search option is required: " + name);
        }
        if (option.isSplit()) {
            String msg = "The split of like-search is NOT available on parameter-bean: " + name + ", " + option;
            throw new IllegalOutsideSqlOperationException(msg);
        }
    }

    public static String formatByteArray(byte[] bytes) {
        return "byte[" + (bytes != null ? String.valueOf(bytes.length) : "null") + "]";
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
        if (LocalDate.class.isAssignableFrom(localType)) { // #date_parade
            return (DATE) DfTypeUtil.toLocalDate(date, timeZone);
        } else if (LocalDateTime.class.isAssignableFrom(localType)) {
            return (DATE) DfTypeUtil.toLocalDateTime(date, timeZone);
        }
        return null; // unreachable
    }

    public static String formatUtilDate(Date date, TimeZone specifiedZone, String pattern) {
        final TimeZone realZone = chooseRealTimeZone(specifiedZone);
        final Locale realLocale = DBFluteSystem.getFinalLocale(); // no specified because of basically debug string
        return DfTypeUtil.toStringDate(date, realZone, pattern, realLocale);
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
    //                                                                  by Option Handling
    //                                                                  ==================
    @SuppressWarnings("unchecked")
    public static <NUMBER extends Number> NUMBER toNumber(Object obj, Class<NUMBER> type) { // might be called by option handling
        return (NUMBER) DfTypeUtil.toNumber(obj, type);
    }

    public static Boolean toBoolean(Object obj) {
        return DfTypeUtil.toBoolean(obj);
    }

    @SuppressWarnings("unchecked")
    public static <ELEMENT> ArrayList<ELEMENT> newArrayList(ELEMENT... elements) { // might be called by option handling
        Object obj = DfCollectionUtil.newArrayList(elements);
        return (ArrayList<ELEMENT>) obj; // to avoid the warning between JDK6 and JDK7
    }
}
