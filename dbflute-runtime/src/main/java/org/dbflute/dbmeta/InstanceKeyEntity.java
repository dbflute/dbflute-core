/*
 * Copyright 2014-2017 the original author or authors.
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
package org.dbflute.dbmeta;

import java.util.Set;

import org.dbflute.Entity;

/**
 * @author jflute
 * @since 0.9.9.4B (2012/04/22 Saturday)
 */
public class InstanceKeyEntity implements Entity {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Entity _actualEntity;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public InstanceKeyEntity(Entity actualEntity) {
        _actualEntity = actualEntity;
    }

    // ===================================================================================
    //                                                                          Delegation
    //                                                                          ==========
    public DBMeta asDBMeta() {
        return _actualEntity.asDBMeta();
    }

    public String asTableDbName() {
        return _actualEntity.asTableDbName();
    }

    // ===================================================================================
    //                                                                 Modified Properties
    //                                                                 ===================
    public Set<String> mymodifiedProperties() {
        return _actualEntity.mymodifiedProperties();
    }

    public void mymodifyProperty(String propertyName) {
        _actualEntity.mymodifyProperty(propertyName);
    }

    public void mymodifyPropertyCancel(String propertyName) {
    }

    public void clearModifiedInfo() {
        _actualEntity.clearModifiedInfo();
    }

    public boolean hasModification() {
        return _actualEntity.hasModification();
    }

    public void modifiedToSpecified() {
        _actualEntity.modifiedToSpecified();
    }

    public Set<String> myspecifiedProperties() {
        return _actualEntity.myspecifiedProperties();
    }

    public void myspecifyProperty(String propertyName) {
        _actualEntity.myspecifyProperty(propertyName);
    }

    public void myspecifyPropertyCancel(String propertyName) {
        _actualEntity.myspecifyPropertyCancel(propertyName);
    }

    public void clearSpecifiedInfo() {
        _actualEntity.clearSpecifiedInfo();
    }

    // ===================================================================================
    //                                                                          Unique Key
    //                                                                          ==========
    public boolean hasPrimaryKeyValue() {
        return _actualEntity.hasPrimaryKeyValue();
    }

    public Set<String> myuniqueDrivenProperties() {
        return _actualEntity.myuniqueDrivenProperties();
    }

    public void myuniqueByProperty(String propertyName) {
        _actualEntity.myuniqueByProperty(propertyName);
    }

    public void myuniqueByPropertyCancel(String propertyName) {
        _actualEntity.myuniqueByPropertyCancel(propertyName);
    }

    public void clearUniqueDrivenInfo() {
        _actualEntity.clearUniqueDrivenInfo();
    }

    // ===================================================================================
    //                                                                      Classification
    //                                                                      ==============
    public void myunlockUndefinedClassificationAccess() {
        _actualEntity.myunlockUndefinedClassificationAccess();
    }

    public boolean myundefinedClassificationAccessAllowed() {
        return _actualEntity.myundefinedClassificationAccessAllowed();
    }

    // ===================================================================================
    //                                                                     Birthplace Mark
    //                                                                     ===============
    public void markAsSelect() {
        _actualEntity.markAsSelect();
    }

    public boolean createdBySelect() {
        return _actualEntity.createdBySelect();
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof InstanceKeyEntity)) {
            return false;
        }
        return _actualEntity == ((InstanceKeyEntity) obj)._actualEntity;
    }

    @Override
    public int hashCode() {
        return _actualEntity.instanceHash();
    }

    @Override
    public String toString() {
        return _actualEntity.toString();
    }

    public int instanceHash() {
        return _actualEntity.instanceHash();
    }

    public String toStringWithRelation() {
        return _actualEntity.toStringWithRelation();
    }

    public String buildDisplayString(String name, boolean column, boolean relation) {
        return _actualEntity.buildDisplayString(name, column, relation);
    }
}
