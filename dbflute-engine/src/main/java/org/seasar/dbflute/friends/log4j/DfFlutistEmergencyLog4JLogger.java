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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.seasar.dbflute.resource.DBFluteSystem;
import org.seasar.dbflute.util.DfTypeUtil;

/**
 * The Log4j logger for emergency. (e.g. when an accident occurs in Log4j)
 * @author jflute
 * @since 0.9.8.2 (2011/04/09 Saturday)
 */
public class DfFlutistEmergencyLog4JLogger implements Log {

    // /- - - - - - - - - - - - - - - - - - -
    // DBFlute uses debug, info, warn, error
    // - - - - - - - - - -/

    public void trace(Object message) {
        dumpEmergencyLog(message);
    }

    public void trace(Object message, Throwable t) {
        dumpEmergencyLog(message, t);
    }

    public void debug(Object message) {
        dumpEmergencyLog(message);
    }

    public void debug(Object message, Throwable t) {
        dumpEmergencyLog(message, t);
    }

    public void info(Object message) {
        dumpEmergencyLog(message);
    }

    public void info(Object message, Throwable t) {
        dumpEmergencyLog(message, t);
    }

    public void warn(Object message) {
        dumpEmergencyLog(message);
    }

    public void warn(Object message, Throwable t) {
        dumpEmergencyLog(message, t);
    }

    public void error(Object message) {
        dumpEmergencyLog(message);
    }

    public void error(Object message, Throwable t) {
        dumpEmergencyLog(message, t);
    }

    public void fatal(Object message) {
        dumpEmergencyLog(message);
    }

    public void fatal(Object message, Throwable t) {
        dumpEmergencyLog(message, t);
    }

    public boolean isTraceEnabled() {
        return true;
    }

    public boolean isDebugEnabled() {
        return true;
    }

    public boolean isInfoEnabled() {
        return true;
    }

    public boolean isWarnEnabled() {
        return true;
    }

    public boolean isErrorEnabled() {
        return true;
    }

    public boolean isFatalEnabled() {
        return true;
    }

    // ===================================================================================
    //                                                                       Emergency Log
    //                                                                       =============
    public void dumpEmergencyLog(Object msg) {
        doDumpEmergencyLog(msg, null);
    }

    public void dumpEmergencyLog(Object msg, Throwable t) {
        doDumpEmergencyLog(msg, t);
    }

    protected void doDumpEmergencyLog(Object msg, Throwable t) {
        final File logFile = new File("./log/dbflute-emergency.log");
        BufferedWriter bw = null;
        try {
            final boolean append = true;
            final String encoding = "UTF-8";
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile, append), encoding));
            final String header = buildLoggingHeader();
            bw.write(header + msg + ln());
            printMessageToConsole(msg);
            if (t != null) {
                final String stackTraceString = extractStackTraceString(t);
                bw.write(header + stackTraceString + ln());
                printMessageToConsole(stackTraceString);
            }
        } catch (Exception ignored) {
            printMessageToConsole("Failed to dump to emergency log file: " + logFile);
            ignored.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    protected String buildLoggingHeader() {
        return DfTypeUtil.toString(new Date(), "yyyy-MM-dd HH:mm:ss") + " - ";
    }

    protected void printMessageToConsole(Object msg) {
        System.out.println(msg);
    }

    protected String extractStackTraceString(Throwable t) {
        StringWriter stringWriter = null;
        PrintWriter printWriter = null;
        try {
            stringWriter = new StringWriter();
            printWriter = new PrintWriter(stringWriter);
            t.printStackTrace(printWriter);
            final String trace = stringWriter.toString();
            return trace != null ? trace.trim() : null;
        } finally {
            if (stringWriter != null) {
                try {
                    stringWriter.close();
                } catch (IOException ignored) {
                }
            }
            if (printWriter != null) {
                printWriter.close();
            }
        }
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected static String ln() {
        return DBFluteSystem.getBasicLn();
    }
}
