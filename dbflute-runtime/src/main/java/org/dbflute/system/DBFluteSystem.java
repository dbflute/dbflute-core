/*
 * Copyright 2014-2014 the original author or authors.
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

import java.sql.Timestamp;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbflute.system.provider.DfCurrentDateProvider;
import org.dbflute.system.provider.DfFinalTimeZoneProvider;

/**
 * @author jflute
 */
public class DBFluteSystem {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The logger instance for this class. (NotNull) */
    private static final Log _log = LogFactory.getLog(DBFluteSystem.class);

    // ===================================================================================
    //                                                                    Option Attribute
    //                                                                    ================
    /**
     * The provider of current date for DBFlute system. <br />
     * e.g. AccessContext might use this (actually, very very rare case) <br />
     * (NullAllowed: if null, server date might be used)
     */
    protected static DfCurrentDateProvider _currentDateProvider;

    /**
     * The provider of final default time-zone for DBFlute system. <br />
     * e.g. DisplaySql, Date conversion, LocalDate mapping and so on... <br />
     * (NullAllowed: if null, server zone might be used)
     */
    protected static DfFinalTimeZoneProvider _finalTimeZoneProvider;

    /** Is this system adjustment locked? */
    protected static boolean _locked = true;

    // ===================================================================================
    //                                                                        Current Time
    //                                                                        ============
    /**
     * Get current date. (server date if no provider)
     * @return The new-created date instance as current date. (NotNull)
     */
    public static Date currentDate() {
        return new Date(currentTimeMillis());
    }

    /**
     * Get current time-stamp. (server date if no provider)
     * @return The new-created time-stamp instance as current date. (NotNull)
     */
    public static Timestamp currentTimestamp() {
        return new Timestamp(currentTimeMillis());
    }

    /**
     * Get current date as milliseconds. (server date if no provider)
     * @return The long value as milliseconds.
     */
    public static long currentTimeMillis() {
        final long millis;
        if (_currentDateProvider != null) {
            millis = _currentDateProvider.currentTimeMillis();
        } else {
            millis = System.currentTimeMillis();
        }
        return millis;
    }

    // ===================================================================================
    //                                                                      Final TimeZone
    //                                                                      ==============
    /**
     * Get the final default time-zone for DBFlute system. <br />
     * basically for e.g. DisplaySql, Date conversion, LocalDate mapping and so on...
     * @return The final default time-zone for DBFlute system. (NotNull: if no provider, server zone)
     */
    public static TimeZone getFinalTimeZone() {
        return _finalTimeZoneProvider != null ? _finalTimeZoneProvider.provide() : TimeZone.getDefault();
    }

    // ===================================================================================
    //                                                                      Line Separator
    //                                                                      ==============
    /**
     * Get basic line separator for DBFlute process.
     * @return The string of line separator. (NotNull)
     */
    public static String ln() {
        return "\n"; // LF is basic here
        // /- - - - - - - - - - - - - - - - - - - - - - - - - - -
        // The 'CR + LF' causes many trouble all over the world.
        //  e.g. Oracle stored procedure
        // - - - - - - - - - -/
    }

    // unused on DBFlute
    //public static String getSystemLn() {
    //    return System.getProperty("line.separator");
    //}

    // ===================================================================================
    //                                                                   System Adjustment
    //                                                                   =================
    // -----------------------------------------------------
    //                                                  Lock
    //                                                  ----
    public static void xlock() {
        if (_locked) {
            return;
        }
        if (_log.isInfoEnabled()) {
            _log.info("...Locking the DBFlute system");
        }
        _locked = true;
    }

    public static void xunlock() {
        if (!_locked) {
            return;
        }
        if (_log.isInfoEnabled()) {
            _log.info("...Unlocking the DBFlute system");
        }
        _locked = false;
    }

    public static boolean isLocked() {
        return _locked;
    }

    protected static void assertUnlocked() {
        if (_locked) {
            throw new IllegalStateException("*DBFluteSystem was locked.");
        }
    }

    // -----------------------------------------------------
    //                                          Current Date
    //                                          ------------
    public static boolean hasCurrentDateProvider() {
        return _currentDateProvider != null;
    }

    public DfCurrentDateProvider xgetCurrentDateProvider() {
        return _currentDateProvider;
    }

    public static void xsetCurrentDateProvider(DfCurrentDateProvider currentDateProvider) {
        assertUnlocked();
        if (_log.isInfoEnabled()) {
            _log.info("_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/");
            _log.info("...Setting currentDateProvider: " + currentDateProvider);
            _log.info("_/_/_/_/_/_/_/_/_/_/");
        }
        _currentDateProvider = currentDateProvider;
        xlock();
    }

    // -----------------------------------------------------
    //                                        Final TimeZone
    //                                        --------------
    public static boolean hasFinalTimeZoneProvider() {
        return _finalTimeZoneProvider != null;
    }

    public DfFinalTimeZoneProvider xgetFinalTimeZoneProvider() {
        return _finalTimeZoneProvider;
    }

    public static void xsetFinalTimeZoneProvider(DfFinalTimeZoneProvider finalTimeZoneProvider) {
        assertUnlocked();
        if (_log.isInfoEnabled()) {
            _log.info("_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/");
            _log.info("...Setting finalTimeZoneProvider: " + finalTimeZoneProvider);
            _log.info("_/_/_/_/_/_/_/_/_/_/");
        }
        _finalTimeZoneProvider = finalTimeZoneProvider;
        xlock();
    }
}
