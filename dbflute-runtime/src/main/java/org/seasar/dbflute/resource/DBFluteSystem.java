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
package org.seasar.dbflute.resource;

import java.sql.Timestamp;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author jflute
 */
public class DBFluteSystem {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance. */
    private static final Log _log = LogFactory.getLog(DBFluteSystem.class);

    // ===================================================================================
    //                                                                    Option Attribute
    //                                                                    ================
    protected static DBFluteCurrentProvider _currentProvider;

    protected static boolean _locked = true;

    // ===================================================================================
    //                                                                      Line Separator
    //                                                                      ==============
    public static String getBasicLn() {
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
    //                                                                        Current Time
    //                                                                        ============
    public static Date currentDate() {
        return new Date(currentTimeMillis());
    }

    public static Timestamp currentTimestamp() {
        return new Timestamp(currentTimeMillis());
    }

    public static long currentTimeMillis() {
        final long millis;
        if (_currentProvider != null) {
            millis = _currentProvider.currentTimeMillis();
        } else {
            millis = System.currentTimeMillis();
        }
        return millis;
    }

    public static interface DBFluteCurrentProvider {
        long currentTimeMillis();
    }

    // ===================================================================================
    //                                                                     Option Accessor
    //                                                                     ===============
    public static void xlock() {
        _locked = true;
    }

    public static void xunlock() {
        _locked = false;
    }

    protected static void assertUnlocked() {
        if (_locked) {
            String msg = "DBFluteSystem was locked.";
            throw new IllegalStateException(msg);
        }
    }

    public static void xsetDBFluteCurrentProvider(DBFluteCurrentProvider currentProvider) {
        assertUnlocked();
        if (_log.isInfoEnabled()) {
            _log.info("...Setting DBFluteCurrentProvider: " + currentProvider);
        }
        _currentProvider = currentProvider;
        xlock();
    }
}
