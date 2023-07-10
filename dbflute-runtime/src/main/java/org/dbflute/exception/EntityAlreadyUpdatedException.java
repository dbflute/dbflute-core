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
 * The exception of when the entity has already been updated by other thread.
 * @author jflute
 */
public class EntityAlreadyUpdatedException extends RuntimeException implements EntityBusinessException {

    /** The serial version UID for object serialization. (Default) */
    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // legacy of S2Dao
    private final Object _bean; // not null
    private final int _rows; // count of actually-updated rows

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * Constructor without bean mask.
     * @param bean The instance of updated entity containing new values. (NotNull)
     * @param rows The count of actually-updated rows, returned by update process. (basically zero when entity update)
     */
    public EntityAlreadyUpdatedException(Object bean, int rows) { // keep compatible for application use
        this(bean, rows, /*maskMan*/null);
    }

    /**
     * Constructor with bean mask.
     * @param bean The instance of updated entity containing new values. (NotNull)
     * @param rows The count of actually-updated rows, returned by update process. (basically zero when entity update)
     * @param maskMan The callback to mask the bean information on exception message. (NullAllowed: if null, show all)
     */
    public EntityAlreadyUpdatedException(Object bean, int rows, AlreadyUpdatedBeanMaskMan maskMan) { // DBFlute uses
        super("The entity has already been updated: rows=" + rows + ", bean=" + toBeanString(bean, maskMan), null);
        _bean = bean;
        _rows = rows;
    }

    private static Object toBeanString(Object bean, AlreadyUpdatedBeanMaskMan maskMan) {
        return maskMan != null ? maskMan.mask(bean) : bean;
    }

    /**
     * @author jflute
     */
    public static interface AlreadyUpdatedBeanMaskMan {

        /**
         * @param bean The bean object to be updated containing new values. (NotNull)
         * @return The masked string of the bean. (NullAllowed: if null, show "null")
         */
        String mask(Object bean);
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public Object getBean() {
        return _bean;
    }

    public int getRows() {
        return _rows;
    }
}
