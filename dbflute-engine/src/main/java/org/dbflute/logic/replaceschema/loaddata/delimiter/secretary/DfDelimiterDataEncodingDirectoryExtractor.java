/*
 * Copyright 2014-2024 the original author or authors.
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

import java.io.File;
import java.util.List;

import org.dbflute.util.DfCollectionUtil;

/**
 * @author jflute
 * @since 1.2.7 extracted from DfDelimiterDataHandlerImpl (2023/02/06 Monday at roppongi japanese)
 */
public class DfDelimiterDataEncodingDirectoryExtractor {

    protected final File _baseDir; // not null

    public DfDelimiterDataEncodingDirectoryExtractor(File baseDir) { // e.g. .../ut/reversetsv, .../ut/tsv
        _baseDir = baseDir;
    }

    public List<String> extractEncodingDirectoryList() { // not null, e.g. UTF-8, Shift_JIS
        final String[] dirs = _baseDir.list((dir, name) -> isTargetDirectory(name));
        if (dirs != null) {
            return DfCollectionUtil.newArrayList(dirs);
        } else {
            return DfCollectionUtil.emptyList();
        }
    }

    protected boolean isTargetDirectory(String name) {
        // traditional logic so keep it for now by jflute (2023/02/06)
        // (where is directory determination?)
        return !name.startsWith("."); // ignore e.g. .svn
    }
}
