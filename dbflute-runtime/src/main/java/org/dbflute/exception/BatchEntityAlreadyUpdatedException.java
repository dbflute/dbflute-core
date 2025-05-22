/*
 * Copyright 2014-2025 the original author or authors.
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
 * The exception of when the entity has already been updated by other thread in batch update.
 * @author jflute
 */
public class BatchEntityAlreadyUpdatedException extends EntityAlreadyUpdatedException {

    /** The serial version UID for object serialization. (Default) */
    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // keep Integer for compatible, just in case (2023/07/09)
    protected final Integer _batchUpdateCount; // actual updated records, keep name for compatible
    protected final Integer _batchSize; // planed update records, no change by result, since 1.2.7

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    // rows are same as batchUpdateCount, make batch-only variables for easy-to-use
    // (that's excuse, rows and batchUpdateCount was ambiguous...m(_ _)m)
    /**
     * Constructor without bean mask.
     * @param bean The instance of entity. (NotNull)
     * @param rows The count of actually-updated rows, returned by update process. (basically zero when entity update)
     * @param batchUpdateCount Actually-updated count in all rows, batch result total. (NotNull)
     * @param batchSize Planed update records as batch, no change by result. (NotNull)
     */
    public BatchEntityAlreadyUpdatedException(Object bean, int rows, Integer batchUpdateCount, Integer batchSize) {
        this(bean, rows, batchUpdateCount, batchSize, /*maskMan*/null);
    }

    // #needs_fix jflute rows means batchUpdateCount? (2023/07/09)
    /**
     * Constructor with bean mask.
     * @param bean The instance of entity. (NotNull)
     * @param rows The count of actually-updated rows, returned by update process. (basically zero when entity update)
     * @param batchUpdateCount Actually-updated count in all rows, batch result total. (NotNull)
     * @param batchSize Planed update records as batch, no change by result. (NotNull)
     * @param maskMan The callback to mask the bean information on exception message. (NullAllowed: if null, show all)
     */
    public BatchEntityAlreadyUpdatedException(Object bean, int rows, Integer batchUpdateCount, Integer batchSize,
            AlreadyUpdatedBeanMaskMan maskMan) { // DBFlute uses
        super(bean, rows, maskMan);
        _batchUpdateCount = batchUpdateCount;
        _batchSize = batchSize;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public Integer getBatchUpdateCount() { // not null
        return _batchUpdateCount;
    }

    public Integer getBatchSize() { // not null
        return _batchSize;
    }
}
