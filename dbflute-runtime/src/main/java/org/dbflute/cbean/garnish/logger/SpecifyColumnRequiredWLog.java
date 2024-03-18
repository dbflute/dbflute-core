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
package org.dbflute.cbean.garnish.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 1.2.7 (2023/07/16 Sunday at roppongi japanese)
 */
public class SpecifyColumnRequiredWLog {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The logger instance for this class. (NotNull) */
    private static final Logger _log = LoggerFactory.getLogger(SpecifyColumnRequiredWLog.class);

    protected static boolean _warningLogLevelInfo;
    protected static boolean _loggingInHolidayMood;
    protected static boolean _locked = true;

    // ===================================================================================
    //                                                                     Warning Logging
    //                                                                     ===============
    public static void log(String msg, RuntimeException cause) { // very Internal
        if (_warningLogLevelInfo) {
            _log.info(msg, cause);
        } else {
            _log.warn(msg, cause);
        }
    }

    public static boolean isLogEnabled() { // very internal
        if (_loggingInHolidayMood) {
            return false;
        }
        if (_warningLogLevelInfo) {
            // if you treat framework warnings as info (when not fit with application warning)
            // #for_now jflute not support debug, because the warnings almost don't work when debug (local environment only) (2023/07/16)
            return _log.isInfoEnabled();
        } else { // basically here
            return _log.isWarnEnabled();
        }
    }

    // ===================================================================================
    //                                                                  Logging Adjustment
    //                                                                  ==================
    public static boolean isWarningLogLevelInfo() {
        return _warningLogLevelInfo;
    }

    public static void setWarningLogLevelInfo(boolean warningLogLevelInfo) {
        assertUnlocked();
        if (_log.isInfoEnabled()) {
            _log.info("...Setting warningLogLevelInfo: " + warningLogLevelInfo);
        }
        _warningLogLevelInfo = warningLogLevelInfo;
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
            _log.info("...Locking the log object for warning!");
        }
        _locked = true;
    }

    public static void unlock() {
        if (!_locked) {
            return;
        }
        if (_log.isInfoEnabled()) {
            _log.info("...Unlocking the log object for warning!");
        }
        _locked = false;
    }

    protected static void assertUnlocked() {
        if (!isLocked()) {
            return;
        }
        throw new IllegalStateException("The warning log is locked.");
    }
}
