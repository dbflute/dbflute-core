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
package org.dbflute.bhv.writable.coins;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.dbflute.Entity;
import org.dbflute.dbmeta.DBMeta;
import org.dbflute.dbmeta.info.ColumnInfo;
import org.dbflute.util.DfTypeUtil;

/**
 * @author jflute
 * @since 1.2.0 (2019/04/30 Tuesday at sheraton)
 */
public class DateUpdateAdjuster { // may be used in velocity template

    // ===================================================================================
    //                                                        DatetimePrecision Truncation
    //                                                        ============================
    /**
     * @param entity The entity to be adjusted here. (NotNull)
     */
    public void truncatePrecisionOfEntityProperty(Entity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("The argument 'entity' should not be null.");
        }
        final DBMeta dbmeta = entity.asDBMeta();
        final List<ColumnInfo> columnInfoList = dbmeta.getColumnInfoList();
        final boolean createdBySelect = entity.createdBySelect();
        if (createdBySelect) {
            entity.clearMarkAsSelect(); // to avoid non-specified column check
        }
        try {
            for (ColumnInfo columnInfo : columnInfoList) {
                if (columnInfo.isObjectNativeTypeDate()) {
                    // it does not need to read/write if LocalDate, but no-if to be simple
                    // (and read/write by columnInfo does not use reflection so no worry)
                    final Object dateValue = columnInfo.read(entity); // is date
                    if (dateValue != null) {
                        columnInfo.write(entity, doTruncatePrecisionIfHasTime(columnInfo, dateValue));
                    }
                }
            }
        } finally {
            if (createdBySelect) {
                entity.markAsSelect(); // restore
            }
        }
    }

    /**
     * @param columnInfo The object of column information. (NotNull)
     * @param value The target value which may be date-time. (NotNull)
     * @return The millisecond-truncated value (NotNull)
     */
    protected Object doTruncatePrecisionIfHasTime(ColumnInfo columnInfo, Object value) {
        // from condition's one, for independent
        // (it might be different logic between condition and updated values in future)
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
