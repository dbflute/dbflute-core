/*
 * Copyright 2014-2019 the original author or authors.
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
package org.dbflute.dbway;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The definition of database.
 * @author jflute
 */
public enum DBDef {

    // ===================================================================================
    //                                                                                ENUM
    //                                                                                ====
    MySQL("mysql", null, new WayOfMySQL()) // supported
    , PostgreSQL("postgresql", "postgre", new WayOfPostgreSQL()) // supported
    , Oracle("oracle", null, new WayOfOracle()) // supported
    , DB2("db2", null, new WayOfDB2()) // supported
    , SQLServer("sqlserver", "mssql", new WayOfSQLServer()) // supported
    , H2("h2", null, new WayOfH2()) // supported
    , Derby("derby", null, new WayOfDerby()) // supported
    , SQLite("sqlite", null, new WayOfSQLite()) // sub supported
    , MSAccess("msaccess", null, new WayOfMSAccess()) // sub supported
    , Firebird("firebird", null, new WayOfFirebird()) // a-little-bit supported
    , Sybase("sybase", null, new WayOfSybase()) // a-little-bit supported
    , Unknown("unknown", null, new WayOfUnknown());

    // ===================================================================================
    //                                                                    Static Reference
    //                                                                    ================
    private static final Logger _log = LoggerFactory.getLogger(DBDef.class);

    // -----------------------------------------------------
    //                                            Code Value
    //                                            ----------
    private static final Map<String, DBDef> _codeValueMap = new HashMap<String, DBDef>();
    static {
        for (DBDef value : values()) {
            _codeValueMap.put(value.code().toLowerCase(), value);
        }
    }
    private static final Map<String, DBDef> _codeAliasValueMap = new HashMap<String, DBDef>();
    static {
        for (DBDef value : values()) {
            if (value.codeAlias() != null) {
                _codeAliasValueMap.put(value.codeAlias().toLowerCase(), value);
            }
        }
    }

    /**
     * @param code The code of the DB. (NullAllowed: If the code is null, it returns null)
     * @return The instance that has the code. (NullAllowed)
     */
    public static DBDef codeOf(String code) {
        if (code == null) {
            return null;
        }
        final String lowerCaseCode = code.toLowerCase();
        DBDef def = _codeValueMap.get(lowerCaseCode);
        if (def == null) {
            def = _codeAliasValueMap.get(lowerCaseCode);
        }
        return def;
    }

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The code of the DB. (NotNull) */
    private final String _code;

    /** The code alias of the DB. (NullAllowed) */
    private final String _codeAlias;

    /** The way of the DB. (NotNull) */
    private DBWay _dbway;

    /** Is this singleton world locked? e.g. you should unlock it to set your own DB-way. */
    private boolean _locked = true; // at first locked;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * @param code The code of the DB. (NotNull)
     * @param codeAlias The code alias of the DB. (NullAllowed)
     * @param codeAlias The DB-way of the DB. (NotNull)
     */
    private DBDef(String code, String codeAlias, DBWay dbway) {
        _code = code;
        _codeAlias = codeAlias;
        _dbway = dbway;
    }

    // ===================================================================================
    //                                                                      Basic Handling
    //                                                                      ==============
    /**
     * @return The code of the DB. (NotNull)
     */
    public String code() {
        return _code;
    }

    /**
     * @return The code alias of the DB. (NullAllowed)
     */
    private String codeAlias() {
        return _codeAlias;
    }

    /**
     * @return The DB-way instance of the DB. (NotNull)
     */
    public DBWay dbway() {
        return _dbway;
    }

    // ===================================================================================
    //                                                                          Switch Way
    //                                                                          ==========
    /**
     * Switch from the old DB-way to the specified DB-way for the DB. (automatically locked after setting) <br>
     * You should call this in application initialization if it needs.
     * @param dbway The new DB-way of the DB. (NotNull)
     */
    public void switchDBWay(DBWay dbway) {
        if (dbway == null) {
            String msg = "The argument 'dbway' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        assertUnlocked();
        final String oldName = _dbway.getClass().getSimpleName();
        if (_log.isInfoEnabled()) {
            _log.info("...Switching DB way from " + oldName + " to " + dbway);
        }
        _dbway = dbway;
        lock(); // auto-lock here, because of deep world
    }

    // ===================================================================================
    //                                                                     Definition Lock
    //                                                                     ===============
    /**
     * Lock this singleton world, e.g. not to set the DB-way.
     */
    public void lock() {
        if (_locked) {
            return;
        }
        if (_log.isInfoEnabled()) {
            _log.info("...Locking the singleton world of the DB definition!");
        }
        _locked = true;
    }

    /**
     * Unlock this singleton world, e.g. to set the DB-way.
     */
    public void unlock() {
        if (!_locked) {
            return;
        }
        if (_log.isInfoEnabled()) {
            _log.info("...Unlocking the singleton world of the DB definition!");
        }
        _locked = false;
    }

    /**
     * Is this singleton world locked?
     * @return The determination, true or false.
     */
    public boolean isLocked() {
        return _locked;
    }

    /**
     * Assert this is not locked.
     */
    protected void assertUnlocked() {
        if (!isLocked()) {
            return;
        }
        throw new IllegalStateException("The DB definition is locked.");
    }
}
