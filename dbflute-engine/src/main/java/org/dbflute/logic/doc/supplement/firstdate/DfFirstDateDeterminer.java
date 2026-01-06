/*
 * Copyright 2014-2026 the original author or authors.
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
package org.dbflute.logic.doc.supplement.firstdate;

import java.util.Date;
import java.util.function.Supplier;

import org.dbflute.helper.HandyDate;

/**
 * @author jflute
 * @since 1.3.2 split from DfFirstDateAgent (2026/01/06 Tuesday at ichihara)
 */
public class DfFirstDateDeterminer {

    // e.g. firstDate is after:2018/05/03
    public boolean determineTableFirstDateAfter(Date tableFirstDate, Date targetAfterDate) {
        if (tableFirstDate != null) {
            // e.g.
            //  2018/05/03 12:34:56, 2018/05/03 00:00:00 => false
            //  2018/05/04 00:00:00, 2018/05/03 00:00:00 => true
            return isAfterWithoutTimepart(tableFirstDate, targetAfterDate);
        } else { // no new difference
            return true; // treated as new table
        }
    }

    public boolean determineColumnFirstDateAfter(Date columnFirstDate, Supplier<Date> tableFirstDateProvider, Date targetAfterDate) {
        if (columnFirstDate != null) {
            return isAfterWithoutTimepart(columnFirstDate, targetAfterDate);
        } else { // no new difference, means that it may be in new table difference
            final Date tableFirstDate = tableFirstDateProvider.get();
            if (tableFirstDate != null) { // so use table first date
                return isAfterWithoutTimepart(tableFirstDate, targetAfterDate);
            } else {
                return true; // treated as new column
            }
        }
    }

    protected boolean isAfterWithoutTimepart(Date firstDate, Date targetAfterDate) {
        return asNonTimeDate(firstDate).after(asNonTimeDate(targetAfterDate));
    }

    protected Date asNonTimeDate(Date comparingDate) {
        return new HandyDate(comparingDate).clearTimeParts().getDate();
    }
}
