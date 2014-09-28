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
package org.seasar.dbflute.infra.reps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.infra.reps.exception.DfReplaceSchemaExecuteNotAllowedException;

/**
 * @author jflute
 * @since 1.0.4G (2013/07/13 Saturday)
 */
public class DfRepsExecuteLimitter {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _sqlRootDir;
    protected final String _sqlFileEncoding;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfRepsExecuteLimitter(String sqlRootDir, String sqlFileEncoding) {
        _sqlRootDir = sqlRootDir;
        _sqlFileEncoding = sqlFileEncoding;
    }

    // ===================================================================================
    //                                                                    Check Executable
    //                                                                    ================
    public void checkExecutableOrNot() {
        final DfRepsSchemaSqlDir schemaSqlDir = createRepsSchemaSqlDir();
        final List<File> sqlFileList = schemaSqlDir.collectReplaceSchemaSqlFileList();
        for (File sqlFile : sqlFileList) {
            final String text = readSqlFileText(sqlFile);
            if (text.trim().length() > 0) {
                return;
            }
        }
        throwReplaceSchemaExecuteNotAllowedException();
    }

    protected DfRepsSchemaSqlDir createRepsSchemaSqlDir() {
        return new DfRepsSchemaSqlDir(_sqlRootDir);
    }

    protected void throwReplaceSchemaExecuteNotAllowedException() {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Your ReplaceSchema execution was not allowed.");
        br.addItem("Advice");
        br.addElement("Not found SQL files for ReplaceSchema,");
        br.addElement("so your execution might be mistake...?");
        br.addItem("SQL Root Directory");
        br.addElement(_sqlRootDir);
        final String msg = br.buildExceptionMessage();
        throw new DfReplaceSchemaExecuteNotAllowedException(msg);
    }

    protected String readSqlFileText(File sqlFile) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(sqlFile), _sqlFileEncoding));
            final StringBuilder sb = new StringBuilder();
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            String msg = "Failed to read the SQL file for check:";
            msg = msg + " file=" + sqlFile + " encoding=" + _sqlFileEncoding;
            throw new IllegalStateException(msg);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}
