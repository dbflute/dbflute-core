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
package org.dbflute.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 */
public class XLog {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The logger instance for this class. (NotNull) */
    private static final Logger _log = LoggerFactory.getLogger(XLog.class);

    protected static boolean _executeStatusLogLevelInfo;
    protected static boolean _loggingInHolidayMood;
    protected static boolean _locked = true;

    // ===================================================================================
    //                                                              Execute-Status Logging
    //                                                              ======================
    public static void log(String msg) { // very internal
        if (_executeStatusLogLevelInfo) {
            _log.info(msg);
        } else {
            _log.debug(msg);
        }
    }

    public static boolean isLogEnabled() { // very internal
        if (_loggingInHolidayMood) {
            return false;
        }
        if (_executeStatusLogLevelInfo) {
            return _log.isInfoEnabled();
        } else {
            return _log.isDebugEnabled();
        }
    }

    // ===================================================================================
    //                                                                  Logging Adjustment
    //                                                                  ==================
    public static boolean isExecuteStatusLogLevelInfo() {
        return _executeStatusLogLevelInfo;
    }

    public static void setExecuteStatusLogLevelInfo(boolean executeStatusLogLevelInfo) {
        assertUnlocked();
        if (_log.isInfoEnabled()) {
            _log.info("...Setting executeStatusLogLevelInfo: " + executeStatusLogLevelInfo);
        }
        _executeStatusLogLevelInfo = executeStatusLogLevelInfo;
        lock(); // auto-lock here, because of deep world
    }

    protected static boolean isLoggingInHolidayMood() {
        return _loggingInHolidayMood;
    }

    public static void setLoggingInHolidayMood(boolean loggingInHolidayMood) {
        assertUnlocked();
        if (_log.isInfoEnabled()) {
            _log.info("...Setting loggingInHolidayMood: " + loggingInHolidayMood);
        }
        _loggingInHolidayMood = loggingInHolidayMood;
        lock(); // auto-lock here, because of deep world
    }

    // ===================================================================================
    //                                                                        Logging Lock
    //                                                                        ============
    public static boolean isLocked() {
        return _locked;
    }

    public static void lock() {
        if (_locked) {
            return;
        }
        if (_log.isInfoEnabled()) {
            _log.info("...Locking the log object for execute status!");
        }
        _locked = true;
    }

    public static void unlock() {
        if (!_locked) {
            return;
        }
        if (_log.isInfoEnabled()) {
            _log.info("...Unlocking the log object for execute status!");
        }
        _locked = false;
    }

    protected static void assertUnlocked() {
        if (!isLocked()) {
            return;
        }
        throw new IllegalStateException("The execute-status log is locked.");
    }
}
