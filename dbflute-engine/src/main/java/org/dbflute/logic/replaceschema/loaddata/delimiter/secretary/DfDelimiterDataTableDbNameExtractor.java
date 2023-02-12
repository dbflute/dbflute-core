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
package org.dbflute.logic.replaceschema.loaddata.delimiter.secretary;

/**
 * @author jflute
 * @since 1.2.5 extracted from DfDelimiterDataWriterImpl (2021/01/20 Wednesday at roppongi japanese)
 */
public class DfDelimiterDataTableDbNameExtractor {

    protected final String _filePath; // not null

    public DfDelimiterDataTableDbNameExtractor(String filePath) {
        _filePath = filePath;
    }

    public String extractTableDbName() {
        final String tableDbName;
        {
            // slash is option, dot is required
            // e.g.
            //  ./HANGAR_MYSTIC.tsv => HANGAR_MYSTIC
            //  ./sea/HANGAR_MYSTIC.tsv => HANGAR_MYSTIC
            //  HANGAR_MYSTIC.tsv => HANGAR_MYSTIC
            //  ./sea-HANGAR_MYSTIC.tsv => HANGAR_MYSTIC
            String tmp = _filePath.substring(_filePath.lastIndexOf("/") + 1, _filePath.lastIndexOf("."));
            if (tmp.indexOf("-") >= 0) {
                // first hyphen is delimiter here (long specification so keep)
                // e.g.
                //  ./sea-lostriver-HANGAR_MYSTIC.tsv => lostriver-HANGAR_MYSTIC
                tmp = tmp.substring(tmp.indexOf("-") + "-".length());
            }
            tableDbName = tmp;
        }
        return tableDbName;
    }
}
