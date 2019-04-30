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
 * The exception of when the paging is over safety size.
 * @author jflute
 */
public class PagingOverSafetySizeException extends RuntimeException {

    /** The serial version UID for object serialization. (Default) */
    private static final long serialVersionUID = 1L;

    /** The max size of safety result. */
    protected int _safetyMaxResultSize;

    /** The count of all records. */
    protected int _allRecordCount;

    /**
     * Constructor.
     * @param msg The message of the exception. (NotNull)
     * @param safetyMaxResultSize The max size of safety result. (NotZero, ZotMinus)
     */
    public PagingOverSafetySizeException(String msg, int safetyMaxResultSize) {
        super(msg);
        this._safetyMaxResultSize = safetyMaxResultSize;
    }

    /**
     * Constructor.
     * @param msg The message of the exception. (NotNull)
     * @param cause Throwable. (NullAllowed)
     * @param safetyMaxResultSize The max size of safety result. (NotZero, ZotMinus)
     */
    public PagingOverSafetySizeException(String msg, Throwable cause, int safetyMaxResultSize) {
        super(msg, cause);
        this._safetyMaxResultSize = safetyMaxResultSize;
    }

    /**
     * Constructor.
     * @param msg The message of the exception. (NotNull)
     * @param safetyMaxResultSize The max size of safety result. (NotZero, ZotMinus)
     * @param allRecordCount The count of all records. (NotZero, ZotMinus, GraeterThanMaxSize)
     */
    public PagingOverSafetySizeException(String msg, int safetyMaxResultSize, int allRecordCount) {
        super(msg);
        this._safetyMaxResultSize = safetyMaxResultSize;
        this._allRecordCount = allRecordCount;
    }

    /**
     * Constructor.
     * @param msg The message of the exception. (NotNull)
     * @param cause Throwable. (NullAllowed)
     * @param safetyMaxResultSize The max size of safety result. (NotZero, ZotMinus)
     * @param allRecordCount The count of all records. (NotZero, ZotMinus, GraeterThanMaxSize)
     */
    public PagingOverSafetySizeException(String msg, Throwable cause, int safetyMaxResultSize, int allRecordCount) {
        super(msg);
        this._safetyMaxResultSize = safetyMaxResultSize;
        this._allRecordCount = allRecordCount;
    }

    /**
     * Get the max size of safety result.
     * @return The max size of safety result. (Basically returns a plus value)
     */
    public int getSafetyMaxResultSize() {
        return _safetyMaxResultSize;
    }

    /**
     * Get the count of all records.
     * @return The count of all records. (If the value is minus, it means it's unknown)
     */
    public int getAllRecordCount() {
        return _allRecordCount;
    }
}
