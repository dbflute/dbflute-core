/*
 * Copyright 2014-2021 the original author or authors.
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
package org.dbflute;

import java.util.Set;

import org.dbflute.dbmeta.DBMeta;

/**
 * The interface of entity. <br>
 * And it's the landmark class for DBFlute.
 * @author jflute
 */
public interface Entity {

    // ===================================================================================
    //                                                                             DB Meta
    //                                                                             =======
    /**
     * Handle the meta as DBMeta, that has all info of the table.
     * @return The (singleton) instance of DB meta for the table. (NotNull)
     */
    DBMeta asDBMeta();

    /**
     * Handle the meta as table DB name, that can be identity of table.
     * @return The (fixed) DB name of the table. (NotNull)
     */
    String asTableDbName(); // not use 'get' for quiet

    // ===================================================================================
    //                                                                 Modified Properties
    //                                                                 ===================
    // -----------------------------------------------------
    //                                              Modified
    //                                              --------
    /**
     * Get the set of modified properties. (basically for Framework) <br>
     * The properties needs to be according to Java Beans rule. <br>
     * @return The set of property name for modified columns, read-only. (NotNull)
     */
    Set<String> mymodifiedProperties(); // 'my' take on unique-driven

    /**
     * Modify the property without setting value. (basically for Framework) <br>
     * The property name needs to be according to Java Beans rule.
     * @param propertyName The property name of modified column. (NotNull)
     */
    void mymodifyProperty(String propertyName);

    /**
     * Cancel the modified the property without resetting value. (basically for Framework) <br>
     * The property name needs to be according to Java Beans rule.
     * @param propertyName The property name of specified column. (NotNull)
     */
    void mymodifyPropertyCancel(String propertyName);

    /**
     * Clear the information of modified properties. (basically for Framework)
     */
    void clearModifiedInfo();

    /**
     * Does it have modifications of property names. (basically for Framework)
     * @return The determination, true or false.
     */
    boolean hasModification();

    // -----------------------------------------------------
    //                                             Specified
    //                                             ---------
    /**
     * Copy to modified properties to specified properties. <br>
     * It means non-specified columns are checked
     */
    void modifiedToSpecified();

    /**
     * Get the set of specified properties. (basically for Framework) <br>
     * The properties needs to be according to Java Beans rule.
     * @return The set of property name for specified columns, read-only. (NotNull: if empty, no check)
     */
    Set<String> myspecifiedProperties(); // 'my' take on unique-driven

    /**
     * Specify the property without setting value. (basically for Framework) <br>
     * The property name needs to be according to Java Beans rule.
     * @param propertyName The property name of specified column. (NotNull)
     */
    void myspecifyProperty(String propertyName); // e.g. called by null object handling

    /**
     * Cancel the specified the property without resetting value. (basically for Framework) <br>
     * The property name needs to be according to Java Beans rule.
     * @param propertyName The property name of specified column. (NotNull)
     */
    void myspecifyPropertyCancel(String propertyName);

    /**
     * Clear the information of specified properties. (basically for Framework) <br>
     * It means no check of access to non-specified columns. <br>
     * And you cannot use myspecifyProperty() without modifiedToSpecified() call.
     */
    void clearSpecifiedInfo();

    // ===================================================================================
    //                                                                        Key Handling
    //                                                                        ============
    /**
     * Does it have the value of primary keys?
     * @return The determination, true or false. (if all PK values are not null, returns true)
     */
    boolean hasPrimaryKeyValue();

    /**
     * Get the properties of unique-driven columns as unique-driven.
     * @return The set of property name for unique-driven columns, read-only. (NotNull)
     */
    Set<String> myuniqueDrivenProperties(); // prefix 'my' not to show when uniqueBy() completion

    /**
     * Treat the property as unique driven without setting value. (basically for Framework) <br>
     * The property name needs to be according to Java Beans rule.
     * @param propertyName The property name of unique-driven column. (NotNull)
     */
    void myuniqueByProperty(String propertyName);

    /**
     * Cancel the property as unique driven without resetting value. (basically for Framework) <br>
     * The property name needs to be according to Java Beans rule.
     * @param propertyName The property name of unique-driven column. (NotNull)
     */
    void myuniqueByPropertyCancel(String propertyName);

    /**
     * Clear the information of unique-driven properties. (basically for Framework)
     */
    void clearUniqueDrivenInfo();

    // ===================================================================================
    //                                                                      Classification
    //                                                                      ==============
    /**
     * Unlock the access to undefined classification code. (default is locked)
     * You can select undefined classification code from database. (and can update...)
     */
    void myunlockUndefinedClassificationAccess();

    /**
     * Does the access to undefined classification allowed?
     * @return The determination, true or false. (basically false)
     */
    boolean myundefinedClassificationAccessAllowed();

    // ===================================================================================
    //                                                                     Birthplace Mark
    //                                                                     ===============
    /**
     * Mark birthplace as select that means the entity is created by DBFlute select process. (basically for Framework) <br>
     * e.g. determine columns of batch insert
     */
    void markAsSelect();

    /**
     * Is the entity created by DBFlute select process? (basically for Framework)
     * @return The determination, true or false.
     */
    boolean createdBySelect();

    /**
     * Clear birthplace mark as created-by-select. (basically for Framework)
     */
    void clearMarkAsSelect();

    // ===================================================================================
    //                                                                    Extension Method
    //                                                                    ================
    /**
     * Calculate the hash-code, which is a default hash code, to identify the instance.
     * @return The hash-code from super.hashCode().
     */
    int instanceHash();

    /**
     * Convert the entity to display string with relation information.
     * @return The display string of basic informations with one-nested relation values. (NotNull)
     */
    String toStringWithRelation();

    /**
     * Build display string flexibly.
     * @param name The name for display. (NullAllowed: If it's null, it does not have a name)
     * @param column Does it contains column values or not?
     * @param relation Does it contains relation existences or not?
     * @return The display string for this entity. (NotNull)
     */
    String buildDisplayString(String name, boolean column, boolean relation);
}
