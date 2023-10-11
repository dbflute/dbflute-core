/*
 * Copyright 2014-2023 the original author or authors.
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
package org.dbflute.cbean.sqlclause.select;

import java.util.List;

import org.dbflute.cbean.ConditionBean;
import org.dbflute.dbmeta.DBMeta;
import org.dbflute.dbmeta.info.ForeignInfo;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.2.7 (2023/07/25 Tuesday at ichihara)
 */
public class DreamSetupSelectSynchronizer {

    public void setupSelectDreamCruiseJourneyLogBook(DBMeta localDBMeta, ConditionBean departurePort, List<String> journeyLogBook) {
        // small waste exists but simple logic is best here
        for (String relationPath : journeyLogBook) {
            final List<String> relNoExpList = Srl.splitList(relationPath, "_"); // e.g. _2_5
            final StringBuilder sb = new StringBuilder();
            DBMeta currentMeta = localDBMeta;
            int index = 0;
            for (String relNoExp : relNoExpList) {
                if ("".equals(relNoExp)) {
                    continue;
                }
                final Integer relationNo = Integer.valueOf(relNoExp);
                final ForeignInfo foreignInfo = currentMeta.findForeignInfo(relationNo);
                final String foreignPropertyName = foreignInfo.getForeignPropertyName();
                if (index > 0) {
                    sb.append(".");
                }
                sb.append(foreignPropertyName);
                currentMeta = foreignInfo.getForeignDBMeta();
                ++index;
            }
            departurePort.invokeSetupSelect(sb.toString());
        }
    }
}
