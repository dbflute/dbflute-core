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
    protected final Integer _batchUpdateCount; // keep Integer for compatible, just in case (2023/07/09)

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * Constructor without bean mask.
     * @param bean The instance of entity. (NotNull)
     * @param rows The count of actually-updated rows, returned by update process. (basically zero when entity update)
     * @param batchUpdateCount Actually-updated count in all rows, batch result total. (NotNull)
     */
    public BatchEntityAlreadyUpdatedException(Object bean, int rows, Integer batchUpdateCount) {
        this(bean, rows, batchUpdateCount, /*maskMan*/null);
    }

    // #needs_fix jflute rows means batchUpdateCount? (2023/07/09)
    /**
     * Constructor with bean mask.
     * @param bean The instance of entity. (NotNull)
     * @param rows The count of actually-updated rows, returned by update process. (basically zero when entity update)
     * @param batchUpdateCount Actually-updated count in all rows, batch result total. (NotNull)
     * @param maskMan The callback to mask the bean information on exception message. (NullAllowed: if null, show all)
     */
    public BatchEntityAlreadyUpdatedException(Object bean, int rows, Integer batchUpdateCount, AlreadyUpdatedBeanMaskMan maskMan) { // DBFlute uses
        super(bean, rows, maskMan);
        _batchUpdateCount = batchUpdateCount;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public Integer getBatchUpdateCount() {
        return _batchUpdateCount;
    }
}
