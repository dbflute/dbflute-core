/*
 * Copyright 2014-2023 the original author or authors.
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

import java.util.List;

/**
 * @author jflute
 */
public class DfDelimiterDataRegistrationFailureException extends RuntimeException {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final long serialVersionUID = 1L;
    public static final String RETRY_STORY_VARIABLE = "${retryStory}";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // for retry story, null allowed (not null when retry is requested and executed)
    protected DfDelimiterDataRegistrationRetryResource _retryResource;
    protected DfDelimiterDataRegistrationRowSnapshot _rowSnapshot;
    protected String _toldRetryStory; // exception message of retry exception

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfDelimiterDataRegistrationFailureException(String msg, Throwable e) {
        super(msg, e);
    }

    public DfDelimiterDataRegistrationFailureException retryIfNeeds(DfDelimiterDataRegistrationRetryResource retryResource) {
        _retryResource = retryResource;
        return this;
    }

    public DfDelimiterDataRegistrationFailureException snapshotRow(DfDelimiterDataRegistrationRowSnapshot rowSnapshot) {
        _rowSnapshot = rowSnapshot;
        return this;
    }

    public DfDelimiterDataRegistrationFailureException tellRetryStory(String toldRetryStory) {
        _toldRetryStory = toldRetryStory;
        return this;
    }

    // ===================================================================================
    //                                                                         Inner Class
    //                                                                         ===========
    public static class DfDelimiterDataRegistrationRetryResource {

        protected boolean _canBatchUpdate;
        protected int _committedRowCount;

        public DfDelimiterDataRegistrationRetryResource(boolean canBatchUpdate, int committedRowCount) {
            _canBatchUpdate = canBatchUpdate;
            _committedRowCount = committedRowCount;
        }

        public boolean canBatchUpdate() {
            return _canBatchUpdate;
        }

        public int getCommittedRowCount() {
            return _committedRowCount;
        }
    }

    public static class DfDelimiterDataRegistrationRowSnapshot { // instance of retry failure is used

        protected final List<String> _columnNameList; // not null, and basically not empty if execution exception
        protected final List<String> _columnValueList; // not null, and basically not empty if execution exception
        protected final int _currentRowNumber; // not zero if execution exception

        public DfDelimiterDataRegistrationRowSnapshot(List<String> columnNameList, List<String> columnValueList, int currentRowNumber) {
            _columnNameList = columnNameList;
            _columnValueList = columnValueList;
            _currentRowNumber = currentRowNumber;
        }

        public List<String> getColumnNameList() { // treated as null allowed just in case
            return _columnNameList;
        }

        public List<String> getColumnValueList() { // me too
            return _columnValueList;
        }

        public int getCurrentRowNumber() {
            return _currentRowNumber;
        }
    }

    // ===================================================================================
    //                                                                     Tricky Override
    //                                                                     ===============
    @Override
    public String getMessage() {
        final String plainMessage = super.getMessage();
        final String replacement = _toldRetryStory != null ? _toldRetryStory : "(no retry information)";
        return plainMessage.replace(RETRY_STORY_VARIABLE, replacement);
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public DfDelimiterDataRegistrationRetryResource getRetryResource() { // null allowed
        return _retryResource;
    }

    public DfDelimiterDataRegistrationRowSnapshot getRowSnapshot() { // null allowed
        return _rowSnapshot;
    }

    public String getToldRetryStory() { // null allowed
        return _toldRetryStory;
    }
}
