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
package org.seasar.dbflute.cbean.chelper;

/**
 * @author jflute
 */
public interface HpColumnSpHandler {

    // ===================================================================================
    //                                                                Column Specification
    //                                                                ====================
    HpSpecifiedColumn xspecifyColumn(String columnName); // internal

    boolean hasSpecifiedColumn();

    boolean isSpecifiedColumn(String columnName);

    // ===================================================================================
    //                                                                        Theme Column
    //                                                                        ============
    /**
     * Specify every column in the table. <br />
     * You cannot use normal SpecifyColumn with this method.
     * <p>no check of modified properties in entities when BatchUpdate.</p>
     */
    void everyColumn();

    boolean isSpecifiedEveryColumn();

    /**
     * Specify columns except record meta columns. <br />
     * You cannot use normal SpecifyColumn with this method. <br />
     * <p>Basically you don't need this when BatchUpdate
     * because record meta columns are automatically controlled.</p>
     */
    void exceptRecordMetaColumn();

    boolean isSpecifiedExceptColumn();

    // ===================================================================================
    //                                                              Synchronization QyCall
    //                                                              ======================
    boolean xhasSyncQyCall();
}