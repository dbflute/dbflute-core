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
package org.seasar.dbflute.friends.log4j;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Layout;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.helpers.CountingQuietWriter;
import org.apache.log4j.helpers.LogLog;

/**
 * The appender using rolling-file for DBFlute.
 * @author jflute
 * @since 0.9.5.1 (2009/06/23 Tuesday)
 */
public class DfFlutistRollingFileAppender extends RollingFileAppender {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfFlutistRollingFileAppender() {
        super();
    }

    public DfFlutistRollingFileAppender(Layout layout, String filename, boolean append) throws IOException {
        super(layout, filename, append);
    }

    // ===================================================================================
    //                                                                    Rolling Override
    //                                                                    ================
    @Override
    public void rollOver() {
        if (qw != null) {
            LogLog.debug("rolling over count=" + ((CountingQuietWriter) qw).getCount());
        }
        LogLog.debug("maxBackupIndex=" + maxBackupIndex);

        if (maxBackupIndex > 0) {
            deleteOldest();
            moveNext();

            final File newestBackupFile = new File(buildBackupFileName(fileName, 1));
            closeFile();

            final File basicFile = new File(fileName);
            LogLog.debug("Renaming file " + basicFile + " to " + newestBackupFile);
            basicFile.renameTo(newestBackupFile);
        }

        try {
            this.setFile(fileName, false, bufferedIO, bufferSize);
        } catch (IOException e) {
            LogLog.error("setFile(" + fileName + ", false) call failed.", e);
        }
    }

    protected void deleteOldest() {
        final String oldestBackupFileName = buildBackupFileName(fileName, maxBackupIndex);
        final File oldestBackupFile = new File(oldestBackupFileName);
        if (oldestBackupFile.exists()) {
            oldestBackupFile.delete();
        }
    }

    protected void moveNext() {
        for (int i = maxBackupIndex - 1; i >= 1; i--) {
            final File fromFile = new File(buildBackupFileName(fileName, i));
            if (fromFile.exists()) {
                final File toFile = new File(buildBackupFileName(fileName, (i + 1)));
                LogLog.debug("Renaming file " + fromFile + " to " + toFile);
                fromFile.renameTo(toFile);
            }
        }
    }

    protected String buildBackupFileName(String baseFileName, int index) {
        final String logExt = ".log";
        if (baseFileName.endsWith(logExt) && baseFileName.length() > logExt.length()) {
            final int extIndex = baseFileName.lastIndexOf(logExt);
            return baseFileName.substring(0, extIndex) + "-backup" + index + logExt;
        } else {
            return baseFileName + "." + index;
        }
    }
}
