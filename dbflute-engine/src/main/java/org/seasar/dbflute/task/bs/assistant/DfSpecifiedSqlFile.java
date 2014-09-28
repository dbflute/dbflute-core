/*
 * Copyright 2004-2014 the Seasar Foundation and the Others.
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
package org.seasar.dbflute.task.bs.assistant;

import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.9.5.5 (2009/09/19 Saturday)
 */
public class DfSpecifiedSqlFile {

    private static final DfSpecifiedSqlFile _instance = new DfSpecifiedSqlFile();

    protected String _specifiedSqlFile;

    private DfSpecifiedSqlFile() {
    }

    public static DfSpecifiedSqlFile getInstance() {
        return _instance;
    }

    /**
     * @return The name of specified SQL file. (Null-able)
     */
    public String getSpecifiedSqlFile() {
        return _specifiedSqlFile;
    }

    public void setSpecifiedSqlFile(String specifiedSqlFile) {
        if (Srl.is_Null_or_TrimmedEmpty(specifiedSqlFile)) {
            return;
        }
        if (specifiedSqlFile.equals("${dfsql}")) {
            return;
        }
        _specifiedSqlFile = specifiedSqlFile;
    }
}
