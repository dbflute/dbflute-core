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
package org.dbflute.exception;

/**
 * The exception of when the fetching is over safety size.
 * @author jflute
 */
public class FetchingOverSafetySizeException extends RuntimeException {

    /** The serial version UID for object serialization. (Default) */
    private static final long serialVersionUID = 1L;

    /** The max size of safety result. */
    protected int _safetyMaxResultSize;

    /** The executed dangerous SQL as displaySql. (NullAllowed: might be set in catch statement so writable) */
    protected String _dangerousDisplaySql;

    /**
     * Constructor.
     * @param msg The message of the exception. (NotNull)
     * @param safetyMaxResultSize The max size of safety result. (NotZero, ZotMinus)
     */
    public FetchingOverSafetySizeException(String msg, int safetyMaxResultSize) {
        super(msg);
        this._safetyMaxResultSize = safetyMaxResultSize;
    }

    /**
     * Constructor.
     * @param msg The message of the exception. (NotNull)
     * @param cause Throwable. (NullAllowed)
     * @param safetyMaxResultSize The max size of safety result. (NotZero, ZotMinus)
     */
    public FetchingOverSafetySizeException(String msg, Throwable cause, int safetyMaxResultSize) {
        super(msg, cause);
        this._safetyMaxResultSize = safetyMaxResultSize;
    }

    /**
     * Get the max size of safety result.
     * @return The max size of safety result. (Basically returns a plus value)
     */
    public int getSafetyMaxResultSize() {
        return _safetyMaxResultSize;
    }

    /**
     * Get the executed dangerous SQL as displaySql.
     * @return The string for SQL. (NullAllowed)
     */
    public String getDangerousDisplaySql() {
        return _dangerousDisplaySql;
    }

    /**
     * Get the executed dangerous SQL as displaySql.
     * @param dangerousDisplaySql The string for SQL. (NullAllowed)
     */
    public void setDangerousDisplaySql(String dangerousDisplaySql) {
        this._dangerousDisplaySql = dangerousDisplaySql;
    }
}
