/*
 * Copyright 2014-2021 the original author or authors.
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
package org.dbflute.cbean.garnish.datefitting;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

import org.dbflute.dbmeta.info.ColumnInfo;
import org.dbflute.util.DfTypeUtil;

/**
 * @author jflute
 * @since 1.2.0 (2019/04/30 Tuesday at sheraton)
 */
public class DateConditionAdjuster {

    /**
     * @param columnInfo The object of column information. (NotNull)
     * @param value The target value which may be date-time. (NotNull)
     * @return The millisecond-truncated or plain value (NotNull)
     */
    public Object truncatePrecisionIfHasTime(ColumnInfo columnInfo, Object value) {
        if (value == null) {
            throw new IllegalArgumentException("The argument 'value' should not be null.");
        }
        if (columnInfo.isObjectNativeTypeDate()) { // contains Java8 Dates
            final Integer datetimePrecision = columnInfo.getDatetimePrecision();
            if (datetimePrecision == null || datetimePrecision == 0) { // non-millisecond date-time
                if (value instanceof LocalDateTime) {
                    return ((LocalDateTime) value).truncatedTo(ChronoUnit.SECONDS); // means clear millisecond
                } else if (value instanceof LocalTime) {
                    return ((LocalTime) value).truncatedTo(ChronoUnit.SECONDS); // means clear millisecond
                } else if (value instanceof Date && !(value instanceof java.sql.Date)) {
                    final Calendar cal = DfTypeUtil.toCalendar(value);
                    DfTypeUtil.clearCalendarMillisecond(cal);
                    return DfTypeUtil.toDate(cal);
                }
            }
        }
        return value;
    }
}
