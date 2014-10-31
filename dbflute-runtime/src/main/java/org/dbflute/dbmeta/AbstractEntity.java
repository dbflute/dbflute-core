/*
 * Copyright 2014-2014 the original author or authors.
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.dbflute.Entity;
import org.dbflute.FunCustodial;
import org.dbflute.dbmeta.accessory.DerivedMappable;
import org.dbflute.dbmeta.accessory.EntityDerivedMap;
import org.dbflute.dbmeta.accessory.EntityModifiedProperties;
import org.dbflute.dbmeta.accessory.EntityUniqueDrivenProperties;
import org.dbflute.jdbc.ClassificationMeta;
import org.dbflute.optional.OptionalScalar;
import org.dbflute.twowaysql.DisplaySqlBuilder;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.Srl;

/**
 * The abstract class of entity.
 * @author jflute
 * @since 1.1.0 (2014/10/19 Sunday)
 */
public abstract class AbstractEntity implements Entity, DerivedMappable, Serializable, Cloneable {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The serial version UID for object serialization. (Default) */
    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                              Internal
    //                                              --------
    /** The unique-driven properties for this entity. (NotNull) */
    protected final EntityUniqueDrivenProperties __uniqueDrivenProperties = newUniqueDrivenProperties();

    /** The modified properties for this entity. (NotNull) */
    protected final EntityModifiedProperties __modifiedProperties = newModifiedProperties();

    /** The modified properties for this entity. (NullAllowed: normally null, created when specified-check) */
    protected EntityModifiedProperties __specifiedProperties;

    /** The map of derived value, key is alias name. (NullAllowed: lazy-loaded) */
    protected EntityDerivedMap __derivedMap;

    /** Is the entity created by DBFlute select process? */
    protected boolean __createdBySelect;

    // ===================================================================================
    //                                                                              DBMeta
    //                                                                              ======
    // TODO jflute 
    //protected abstract boolean isDomainEntity();
    
    // ===================================================================================
    //                                                                    Derived Mappable
    //                                                                    ================
    /** {@inheritDoc} */
    public void registerDerivedValue(String aliasName, Object selectedValue) {
        if (__derivedMap == null) {
            __derivedMap = newDerivedMap();
        }
        __derivedMap.registerDerivedValue(aliasName, selectedValue);
    }

    /** {@inheritDoc} */
    public <VALUE> OptionalScalar<VALUE> derived(String aliasName, Class<VALUE> propertyType) {
        if (__derivedMap == null) {
            // process of finding has existence check of the alias
            // so if called here, exception in the map
            __derivedMap = newDerivedMap();
        }
        return __derivedMap.findDerivedValue(this, aliasName, propertyType);
    }

    protected EntityDerivedMap newDerivedMap() {
        return new EntityDerivedMap();
    }

    // ===================================================================================
    //                                                                 Modified Properties
    //                                                                 ===================
    // -----------------------------------------------------
    //                                              Modified
    //                                              --------
    /** {@inheritDoc} */
    public Set<String> mymodifiedProperties() {
        return __modifiedProperties.getPropertyNames();
    }

    /** {@inheritDoc} */
    public void clearModifiedInfo() {
        __modifiedProperties.clear();
    }

    /** {@inheritDoc} */
    public boolean hasModification() {
        return !__modifiedProperties.isEmpty();
    }

    protected EntityModifiedProperties newModifiedProperties() {
        return new EntityModifiedProperties();
    }

    protected void registerModifiedProperty(String propertyName) {
        __modifiedProperties.addPropertyName(propertyName);
        registerSpecifiedProperty(propertyName); // synchronize if exists, basically for user's manual call
    }

    // -----------------------------------------------------
    //                                             Specified
    //                                             ---------
    /** {@inheritDoc} */
    public void modifiedToSpecified() {
        if (__modifiedProperties.isEmpty()) {
            // basically no way when called in Framework (because called when SpecifyColumn exists)
            return;
        }
        __specifiedProperties = newModifiedProperties();
        __specifiedProperties.accept(__modifiedProperties);
    }

    /** {@inheritDoc} */
    public Set<String> myspecifiedProperties() {
        if (__specifiedProperties != null) {
            return __specifiedProperties.getPropertyNames();
        }
        return DfCollectionUtil.emptySet();
    }

    public void clearSpecifiedInfo() {
        if (__specifiedProperties != null) {
            __specifiedProperties.clear();
        }
    }

    protected void checkSpecifiedProperty(String propertyName) {
        FunCustodial.checkSpecifiedProperty(this, propertyName, __specifiedProperties);
    }

    protected void registerSpecifiedProperty(String propertyName) { // basically called by modified property registration
        if (__specifiedProperties != null) { // normally false, true if e.g. setting after selected
            __specifiedProperties.addPropertyName(propertyName);
        }
    }

    // ===================================================================================
    //                                                                         Primary Key
    //                                                                         ===========
    /** {@inheritDoc} */
    public Set<String> myuniqueDrivenProperties() {
        return __uniqueDrivenProperties.getPropertyNames();
    }

    protected EntityUniqueDrivenProperties newUniqueDrivenProperties() {
        return new EntityUniqueDrivenProperties();
    }

    // ===================================================================================
    //                                                                     Birthplace Mark
    //                                                                     ===============
    /** {@inheritDoc} */
    public void markAsSelect() {
        __createdBySelect = true;
    }

    /** {@inheritDoc} */
    public boolean createdBySelect() {
        return __createdBySelect;
    }

    // ===================================================================================
    //                                                                      Classification
    //                                                                      ==============
    protected <NUMBER extends Number> NUMBER toNumber(Object obj, Class<NUMBER> type) {
        return FunCustodial.toNumber(obj, type);
    }

    protected Boolean toBoolean(Object obj) {
        return FunCustodial.toBoolean(obj);
    }

    // ===================================================================================
    //                                                                   Referrer Property
    //                                                                   =================
    protected <ELEMENT> List<ELEMENT> newReferrerList() {
        return new ArrayList<ELEMENT>();
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    // -----------------------------------------------------
    //                                              equals()
    //                                              --------
    /**
     * Determine the object is equal with this. <br>
     * If primary-keys or columns of the other are same as this one, returns true.
     * @param obj The object as other entity. (NullAllowed: if null, returns false fixedly)
     * @return Comparing result.
     */
    public boolean equals(Object obj) {
        return obj != null && doEquals(obj);
    }

    /**
     * @param obj The object as other entity. (NotNull)
     * @return The determination, true or false.
     */
    protected abstract boolean doEquals(Object obj);

    protected boolean xSV(Object v1, Object v2) {
        return FunCustodial.isSameValue(v1, v2);
    }

    // -----------------------------------------------------
    //                                            hashCode()
    //                                            ----------
    /**
     * Calculate the hash-code from primary-keys or columns.
     * @return The hash-code from primary-key or columns.
     */
    public int hashCode() {
        return doHashCode(17);
    }

    /**
     * @param initial The initial value to calculate hash-code.
     * @return The calculated hash-code.
     */
    protected abstract int doHashCode(int initial);

    protected int xCH(int hs, Object vl) {
        return FunCustodial.calculateHashcode(hs, vl);
    }

    /** {@inheritDoc} */
    public int instanceHash() {
        return super.hashCode();
    }

    // -----------------------------------------------------
    //                                            toString()
    //                                            ----------
    /**
     * Convert to display string of entity's data. (no relation data)
     * @return The display string of all columns and relation existences. (NotNull)
     */
    @Override
    public String toString() {
        return buildDisplayString(FunCustodial.toClassTitle(this), true, true);
    }

    protected String xbRDS(Entity et, String name) { // buildRelationDisplayString()
        // another overload method exists in template
        return et.buildDisplayString(name, true, true);
    }

    /** {@inheritDoc} */
    public String toStringWithRelation() {
        final StringBuilder sb = new StringBuilder();
        sb.append(toString());
        sb.append(doBuildStringWithRelation("\n  ")); // line and two spaces indent
        return sb.toString();
    }

    protected abstract String doBuildStringWithRelation(String li);

    /** {@inheritDoc} */
    public String buildDisplayString(String name, boolean column, boolean relation) {
        StringBuilder sb = new StringBuilder();
        if (name != null) {
            sb.append(name).append(column || relation ? ":" : "");
        }
        if (column) {
            sb.append(doBuildColumnString(", ")); // e.g. 1, Stojkovic, Pixy, ...
        }
        if (relation) {
            sb.append(doBuildRelationString(",")); // e.g. (memberStatus,memberServiceAsOne)
        }
        sb.append("@").append(Integer.toHexString(hashCode()));
        return sb.toString();
    }

    protected abstract String doBuildColumnString(String dm);

    protected abstract String doBuildRelationString(String dm);

    protected String xfUD(Date date) { // formatUtilDate()
        return FunCustodial.toStringDate(date, myutilDatePattern(), mytimeZone());
    }

    protected String myutilDatePattern() {
        // actually, Oracle's date needs time-parts
        // but not important point and LocalDate is main since 1.1
        // so simple logic here
        return DisplaySqlBuilder.DEFAULT_DATE_FORMAT; // as default
    }

    protected TimeZone mytimeZone() {
        return null; // as default
    }

    protected String xfBA(byte[] bytes) { // formatByteArray()
        return FunCustodial.toStringBytes(bytes);
    }

    protected Object xfND(Object obj) { // formatNormalData()
        if (obj == null) {
            return null;
        }
        if (!(obj instanceof String)) {
            return obj;
        }
        String filteredStr = (String) obj;
        filteredStr = mycutLargeStringForToString(filteredStr);
        filteredStr = myremoveLineSepForToString(filteredStr);
        return filteredStr;
    }

    protected String mycutLargeStringForToString(String filteredStr) {
        final int actualSize = filteredStr.length();
        final int limit = mylargeStringForToStringLimit();
        if (actualSize > limit) {
            // e.g. {sea, land, long text now...(length:182), iks, amphi}
            filteredStr = Srl.cut(filteredStr, limit) + "...(length:" + actualSize + ")";
        }
        return filteredStr;
    }

    protected int mylargeStringForToStringLimit() {
        return 140;
    }

    protected String myremoveLineSepForToString(String filteredStr) {
        final String cr = "\r";
        final String lf = "\n";
        if (Srl.containsAny(filteredStr, cr, lf)) {
            return Srl.replace(Srl.replace(filteredStr, cr, "\\r"), lf, "\\n"); // remove lines
        }
        return filteredStr;
    }

    // -----------------------------------------------------
    //                                               clone()
    //                                               -------
    /**
     * Clone entity instance using super.clone(). (shallow copy) 
     * @return The cloned instance of this entity. (NotNull)
     * @throws IllegalStateException When it fails to clone the entity.
     */
    @Override
    public Entity clone() {
        try {
            return (Entity) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Failed to clone the entity: " + toString(), e);
        }
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    protected String convertEmptyToNull(String value) {
        return FunCustodial.convertEmptyToNull(value);
    }

    protected void checkClassificationCode(String columnDbName, ClassificationMeta meta, Object value) {
        FunCustodial.checkClassificationCode(this, columnDbName, meta, value);
    }
}
